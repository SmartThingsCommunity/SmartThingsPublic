/**
 *  Copyright 2015 SmartThings
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 *    Nexia Thermostat Service Manager
 *
 *    Author: Trent Foley
 *    Date: 2016-01-19
 */
definition(
    name: "Nexia Thermostat Manager",
    namespace: "trentfoley",
    author: "Trent Foley",
    description: "Connect your Nexia thermostat to SmartThings.",
    category: "Convenience",
    iconUrl: "http://lh4.ggpht.com/oMx3-nlICwLmUxpDhTXWsZ6Ocuzu9P2yfz9jpXBx1rhrW_Vcj94kPl2M9ooApckK6TM1=w60",
    iconX2Url: "https://www.trane.com/content/dam/Trane/residential/products/nexia/medium/TR_Nexia%20-%20Medium.jpg",
    iconX3Url: "https://www.trane.com/content/dam/Trane/residential/products/nexia/medium/TR_Nexia%20-%20Medium.jpg",
    singleInstance: true
) { }

preferences {
    section("Nexia Auth") {
        input "username", "text", title: "Username"
        input "password", "password", title: "Password"
    }
}

def getChildNamespace() { "trentfoley" }
def getChildName() { "Nexia Thermostat" }
def getServerUrl() { "https://www.mynexia.com/" }

private debugEvent(message, displayEvent = false) {
    def results = [
        name: "debug",
        descriptionText: message,
        displayed: displayEvent
    ]
    log.debug "${results}"
    sendEvent(results)
}

private errorEvent(message, displayEvent = true) {
    def results = [
        name: "error",
        descriptionText: message,
        displayed: displayEvent
    ]
    log.error "${results}"
    sendEvent(results)
}

def installed() {
    debugEvent("installed()")
    initialize()
}

def updated() {
    debugEvent("updated()")
    unsubscribe()
    initialize()
}

def initialize() {
    debugEvent("initialize()")
    
    // Ensure authenticated
    refreshAuthToken()
    
    // Get list of thermostats and ensure child devices
    def homeParams = [
        method: 'GET',
        uri: serverUrl,
        headers: defaultHeaders
    ]

    debugEvent("Nexia Home Request Parameters: ${homeParams}")

    try {
        httpGet(homeParams) { homeResp ->
            // html / body / div id=footer-wrapper / div id=content / div id=content_sidebar / nav / ul / li / a id=climate_link
            // Recursive search for climate/index link.  Should be more robust to Nexia DOM changes
            homeResp.data[0].children().each{
                searchForClimate(it)
            }
        }
    }
    catch(Exception e) {
        errorEvent("Caught exception determining thermostats path: ${e}")
    }
    
    // Get list of thermostats and ensure child devices
    requestThermostats { thermostatsResp ->
        def devices = thermostatsResp.data.collect { stat ->
            debugEvent("Found thermostat with ID: ${stat.id}")
            
            //Check for Multiple Zones
            def dni = getDeviceNetworkId(stat.id)
            def device = null;
            if(stat.zones.size > 1) {
                stat.zones.each {
                    dni = getDeviceNetworkId(stat.id + "_" + it.id)
                    device = addMultipleDevices(dni, it.name)
                }
            }
            else {
                dni = getDeviceNetworkId(stat.id)
                device = addMultipleDevices(dni, stat.name)
            }
            device.refresh()
            return device
        }

        debugEvent("Discovered ${devices.size()} thermostats")
        
        //devices.each { it.refresh() }
    }
}

private def addMultipleDevices(dni, statname) {
    def device = getChildDevice(dni)
    if(!device) {
        device = addChildDevice(childNamespace, childName, dni, null, [ label: "${childName} (${statname})" ])
        debugEvent("Created ${device.displayName} with device network id: ${dni}")
    } else {
        debugEvent("Found already existing ${device.displayName} with device network id: ${dni}")
    }
    return device
}


private searchForClimate(httpNode) {
    if(httpNode != null && !(httpNode instanceof String)) {
        if(httpNode.attributes()["href"] != null) {
            if(httpNode.attributes()["href"].matches("(?i).*climate/index.*"))
            {
                state.thermostatsPath = httpNode.attributes()["href"].replace("climate/index", "xxl_thermostats")
                state.zonesPath = httpNode.attributes()["href"].replace("climate/index", "xxl_zones")
                debugEvent("ThermostatsPath = ${state.thermostatsPath}")
            }
            }
        if(httpNode.children() != null) {
            httpNode.children().each {
                if(it!=null)
                    searchForClimate(it)
            }
        }
    }
}

private searchForAuthToken(httpNode) {
    if(httpNode != null && !(httpNode instanceof String)) {
        if(httpNode.attributes()["name"] != null) {
            if(httpNode.attributes()["name"]=="authenticity_token")
            {
                state.AuthToken = httpNode.attributes()["value"]
            }
            }
        if(httpNode.children() != null) {
            httpNode.children().each {
                if(it!=null)
                    searchForAuthToken(it)
            }
        }
    }
}

private String getDeviceNetworkId(def statId) {
    return [ app.id, statId ].join('.')
}

private updateCookies(groovyx.net.http.HttpResponseDecorator response) {
    response.getHeaders('Set-Cookie').each {
        def cookieValue = it.value.split(';')[0]
        def cookieName = cookieValue.split('=')[0]
        // if (!state.cookies) { state.cookies = [:] }
        
        debugEvent("Updating cookie: ${cookieValue}")
        
        state.cookies[(cookieName)] = cookieValue
    }
}

def getDefaultHeaders() {
    def headers = [
        'Accept': 'text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8',
        'Accept-Encoding': 'gzip, deflate',
        'Accept-Language': 'en-US,en,q=0.8',
        'Cache-Control': 'max-age=0',
        'Connection': 'keep-alive',
        'User-Agent': 'Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/47.0.2526.111 Safari/537.36'
    ]

    def cookieString = state.cookies?.collect { entry -> entry.value }?.join('; ');
    if (cookieString) { headers.Cookie = cookieString }
    return headers
}

private refreshAuthToken() {
    debugEvent("refreshAuthToken()")

    // Initialize / clear any existing cookies
    state.cookies = [:]

    def loginParams = [
        method: 'GET',
        uri: serverUrl,
        path: "/login",
        headers: defaultHeaders
    ]

    debugEvent("Login Params: ${loginParams}")
    
    try {
         httpGet(loginParams) { loginResp ->
            updateCookies(loginResp)
            // html / body   / div id=content / div id=external-wrapper / div id=external-content / div id=login-form / form / div / input name=authenticity_token
            // OLD def authenticityToken = loginResp.data[0].children[1].children[1].children[0].children[0].children[1].children[2].children[0].children[1].attributes()["value"]
            //def authenticityToken = loginResp.data[0].children[1].children[1].children[0].children[0].children[0].children[0].children[2].children[0].children[1].attributes()["value"]
            // Recursive search for authenticity token.  Should be more robust to Nexia DOM changes
            searchForAuthToken(loginResp.data[0])
            def authenticityToken = state.AuthToken
            debugEvent("Authenticity Token: ${authenticityToken}")
            
            def sessionParams = [
                method: 'POST',
                uri: serverUrl,
                path: '/session',
                requestContentType: 'application/x-www-form-urlencoded',
                headers: defaultHeaders,
                body: [
                    'utf8': '✓',
                    'authenticity_token': authenticityToken,
                    'login': settings.username,
                    'password': settings.password
                ]
            ]

            debugEvent("Session Parameters: ${sessionParams}")

            httpPost(sessionParams) { sessionResp ->
                if (sessionResp.status != 302) { throw new Exception("Did not receive expected response status code.  Expected 302, actual ${sessionResp.status}") }
                updateCookies(sessionResp)
            }
        }
    }
    catch(Exception e) {
        errorEvent("Caught exception refreshing auth token: ${e}")
    }
}

private requestThermostats(Closure closure) {
    debugEvent("requestThermostats(${state.thermostatsPath})")
    
    def thermostatsParams = [
        uri: serverUrl,
        path: state.thermostatsPath,
        headers: defaultHeaders
    ]

    debugEvent("Thermostat Params: ${thermostatsParams}")
    
    try {
        httpGet(thermostatsParams) { resp ->
            if (resp.status == 200) {
                closure(resp)
            } else if (resp.status == 302) { // Redirect to login page due to session expiration
                refreshAuthToken()
                requestThermostats(closure)
            } else {
                throw new Exception("Unexpected status while requesting thermostats: ${resp.status}")
            }
        }
    }
    catch(Exception e) {
        errorEvent("Caught exception requesting thermostats: ${e}")
    }
}

private requestThermostat(deviceNetworkId, Closure closure) {
    debugEvent("Requesting ${deviceNetworkId}")
    requestThermostats { resp ->
        def stat = resp.data.find { it -> getDeviceNetworkId(it.id) == deviceNetworkId }
        if (!stat) {
                errorEvent("ERROR: Device connection removed? No data found for ${deviceNetworkId} after polling")
        } else {
                debugEvent("Thermostat request succeeded: ${stat}")
                closure(stat)
        }
    }
}

// Poll Child is invoked from the Child Device itself as part of the Poll Capability
def pollChild(child) {
    //if zoned, take off zone id... performs a repetitive update due to zoning, fix later
    def deviceNetworkId = ((child.device.deviceNetworkId).split('_'))[0]
    def zonedBool = ((child.device.deviceNetworkId).split('_')).size()
    debugEvent("ZoneBool ${zonedBool} pollChild(${deviceNetworkId})")

    def statData = [:]

    requestThermostat(deviceNetworkId) { stat ->
        def zone = stat.zones[0]
        if(zonedBool > 1) {
            def zoneNetworkId = ((child.device.deviceNetworkId).split('_'))[1]
            zone = stat.zones.find {it.id == zoneNetworkId.toInteger()}
        }
        
        def systemStatusToOperatingStateMapping = [
            "System Idle": "idle",
            "Waiting...": "pending ${zone.zone_mode.toLowerCase()}",
            "Heating": "heating",
            "Cooling": "cooling",
            "Fan Running": "fan only"
        ]

        statData = [
            temperature: zone.temperature.toInteger(),
            heatingSetpoint: zone.heating_setpoint.toInteger(),
            coolingSetpoint: zone.cooling_setpoint.toInteger(),
            thermostatSetpoint: ((zone.zone_mode == "COOL") ? zone.cooling_setpoint : zone.heating_setpoint).toInteger(),
            // TODO: handle case for "emergency heat"
            thermostatMode: zone.requested_zone_mode.toLowerCase(), // "auto" "emergency heat" "heat" "off" "cool"
            thermostatFanMode: stat.fan_mode,  // "auto" "on" "circulate"
            thermostatOperatingState: systemStatusToOperatingStateMapping[stat.system_status], // "heating" "idle" "pending cool" "vent economizer" "cooling" "pending heat" "fan only"
            systemStatus: stat.system_status,
            activeMode: zone.zone_mode.toLowerCase(),
            emergencyHeatSupported: stat.emergency_heat_supported,
            humidity: (stat.current_relative_humidity * 100).toInteger(),
            outdoorTemperature: stat.raw_outdoor_temperature.toInteger()
        ]
    }
    
    return statData
}

// updateType can be: "setpoints", "zone_mode"
private updateZone(zone, updateType) {
    debugEvent("updateZone(${zone.id}, ${updateType})")
    def requestParams = [
        uri: serverUrl,
        path: "${state.zonesPath}/${zone.id}/${updateType}",
        headers: defaultHeaders,
        body: zone
    ]

    httpPutJson(requestParams) { resp ->
        if (resp.status == 200) {
            debugEvent("Zone update suceeded")
        } else {
            throw new Exception("Unexpected status while attempting to update zone: ${resp.status}")
        }
    }
}

// updateType can be: "fan_mode"
private updateThermostat(stat, updateType) {
    debugEvent("updateThermostat(${stat.id}, ${updateType})")
    def requestParams = [
        uri: serverUrl,
        path: "${state.thermostatsPath}/${stat.id}/${updateType}",
        headers: defaultHeaders,
        body: stat
    ]

    httpPutJson(requestParams) { resp ->
        if (resp.status == 200) {
            debugEvent("Thermostat update suceeded")
        } else {
            throw new Exception("Unexpected status while attempting to update thermostat: ${resp.status}")
        }
    }
}

def setHeatingSetpoint(child, degreesF) {
    def deviceNetworkId = ((child.device.deviceNetworkId).split('_'))[0]
    def zonedBool = ((child.device.deviceNetworkId).split('_')).size()
    debugEvent("setHeatingSetpoint(${deviceNetworkId}, ${degreesF})")
    
    requestThermostat(deviceNetworkId) { stat ->
        def zone = stat.zones[0]
        if(zonedBool > 1) {
            def zoneNetworkId = ((child.device.deviceNetworkId).split('_'))[1]
            zone = stat.zones.find {it.id == zoneNetworkId.toInteger()}
        }
        zone.heating_setpoint = degreesF
        zone.heating_integer = "${degreesF.toInteger()}"
        zone.heating_decimal = ""
        zone.cooling_setpoint = zone.cooling_setpoint
        zone.cooling_integer = "${zone.cooling_setpoint}"
        zone.cooling_decimal = ""
        
        updateZone(zone, "setpoints")
    }
}

def setCoolingSetpoint(child, degreesF) {
    def deviceNetworkId = ((child.device.deviceNetworkId).split('_'))[0]
    def zonedBool = ((child.device.deviceNetworkId).split('_')).size()
    debugEvent("setCoolingSetpoint(${deviceNetworkId}, ${degreesF})")
    
    requestThermostat(deviceNetworkId) { stat ->
        def zone = stat.zones[0]
        if(zonedBool > 1) {
            def zoneNetworkId = ((child.device.deviceNetworkId).split('_'))[1]
            zone = stat.zones.find {it.id == zoneNetworkId.toInteger()}
        }
        zone.heating_setpoint = zone.heating_setpoint
        zone.heating_integer = "${zone.heating_setpoint.toInteger()}"
        zone.heating_decimal = ""
        zone.cooling_setpoint = degreesF
        zone.cooling_integer = "${degreesF.toInteger()}"
        zone.cooling_decimal = ""
        
        updateZone(zone, "setpoints")
    }
}

def setThermostatMode(child, value) {
    def deviceNetworkId = ((child.device.deviceNetworkId).split('_'))[0]
    def zonedBool = ((child.device.deviceNetworkId).split('_')).size()
    debugEvent("setThermostatMode(${deviceNetworkId}, ${value})")
    
    requestThermostat(deviceNetworkId) { stat ->
        def zone = stat.zones[0]
        if(zonedBool > 1) {
            def zoneNetworkId = ((child.device.deviceNetworkId).split('_'))[1]
            zone = stat.zones.find {it.id == zoneNetworkId.toInteger()}
        }
        zone.requested_zone_mode = value.toUpperCase()
        updateZone(zone, "zone_mode")
    }
}

def setThermostatFanMode(child, value) {
    def deviceNetworkId = ((child.device.deviceNetworkId).split('_'))[0]
    def zonedBool = ((child.device.deviceNetworkId).split('_')).size()
    debugEvent("setThermostatFanMode(${deviceNetworkId}, ${value})")
    
    requestThermostat(deviceNetworkId) { stat ->
        stat.fan_mode = value
        updateThermostat(stat, "fan_mode")
    }
}