/**
 *  Lloyds Banking Group Connect & Protect
 *
 *  Copyright 2016 Domotz
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
 */

definition(
        name: "Lloyds Banking Group Connect & Protect",
        namespace: "domotz.dev",
        author: "Domotz",
        description: "The Lloyds Connect & Protect SmartApp is a bridge between SmartThings Cloud and Lloyds Banking Group to enable advanced connected device monitoring and alerting features to be included in the offering to their customers for the connected home service",
        category: "Convenience",
        singleInstance: true,
        iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
        iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
        iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
        oauth: [displayName: "Lloyds Banking Group Connect & Protect", displayLink: ""]) {
    appSetting "endpointRetrievalUrl"
    appSetting "xApiKey"
}

def getSupportedTypes() {
    return [
            [obj: switches, name: "switches", attribute: "switch", capability: "switch", title: "Switches"],
            [obj: motions, name: "motions", attribute: "motion", capability: "motionSensor", title: "Motion Sensors"],
            [obj: temperature, name: "temperature", attribute: "temperature", capability: "temperatureMeasurement", title: "Temperature Sensors"],
            [obj: contact, name: "contact", attribute: "contact", capability: "contactSensor", title: "Contact Sensors"],
            [obj: presence, name: "presence", attribute: "presence", capability: "presenceSensor", title: "Presence Sensors"],
            [obj: water, name: "water", attribute: "water", capability: "waterSensor", title: "Water Sensors"],
            [obj: smoke, name: "smoke", attribute: "smoke", capability: "smokeDetector", title: "Smoke Sensors"],
            [obj: battery, name: "battery", attribute: "battery", capability: "battery", title: "Batteries"]
    ]
}

preferences {
    section("Allow Lloyds Banking Group Connect & Protect service to monitor these devices") {
        for (type in getSupportedTypes()) {
            input type.get("name"), "capability.${type.get('capability')}", title: type.get('title'), multiple: true
        }
    }
}

def installed() {
    initialize()
}

def updated() {
    unsubscribe()
    initialize()
    hubUpdateHandler()

}

def getRequestHeaders() {
    return ['Accept': '*/*', 'X-API-KEY': appSettings.xApiKey]
}

def subscribeToDeviceEvents() {
    for (type in getSupportedTypes()) {
        subscribe(type.get("obj"), "${type.get("attribute")}", genericDeviceEventHandler)
    }
}

def initialize() {
    if (atomicState.endpoint != null) {
        log.debug "Detected endpoint: ${atomicState.endpoint}"
        subscribeToDeviceEvents()
        subscribe(location, "routineExecuted", modeChangeHandler)
        subscribe(location, "mode", modeChangeHandler)
    } else {
        log.debug "There is no endpoint, requesting domotz for a new one"
        requestNewEndpoint()
    }
}


def getHubLocation() {
    def location_info = [:]
    location_info['uid'] = location.id
    //location_info['hubs'] = location.hubs
    location_info['latitude'] = location.latitude
    location_info['longitude'] = location.longitude
    location_info['current_mode'] = location.mode
    //location_info['modes'] = location.modes
    location_info['name'] = location.name
    location_info['temperature_scale'] = location.temperatureScale
    location_info['version'] = location.version
    location_info['channel_name'] = location.channelName
    location_info['zip_code'] = location.zipCode
    log.debug "Triggered getHubLocation with properties: ${location_info}"
    return location_info
}

def modeChangeHandler(evt) {
    log.debug "mode changed to ${evt.value}"
    def url = null
    if (atomicState.endpoint != null) {
        url = atomicState.endpoint + '/hub-change'

        httpPutJson(
                uri: url,
                body: getHubLocation(),
                headers: getRequestHeaders()
        )
    } else {
        log.debug "There is no endpoint, requesting domotz for a new one"
        requestNewEndpoint()
    }

}

def genericDeviceEventHandler(event) {
    log.debug "Device Event Handler, event properties: ${event.getProperties().toString()}"
    log.debug "Device Event Handler, value: ${event.value}"
    def resp = [:]
    def url = null
    def device = null

    device = getDevice(event.device, resp)

    url = atomicState.endpoint + "/device/" + device.provider_uid + "/${event.name}"

    log.debug "Device Event Handler, put url: ${url}"
    log.debug "Device Event Handler, put body: value: ${event.value}\ndate: ${event.isoDate}"
    httpPutJson(
            uri: url,
            body: [
                    "value": event.value,
                    "time" : event.isoDate
            ],
            headers: getRequestHeaders()
    )
}


def hubUpdateHandler() {
    log.debug "Hub Update Handler, with settings: ${settings}"
    def url = null
    def deviceList = [:]

    if (atomicState.endpoint != null) {
        url = atomicState.endpoint + '/device-list'
        log.debug "Hub Update Event Handler, put url: ${url}"
        deviceList = getDeviceList()
        httpPutJson(
                uri: url,
                body: deviceList,
                headers: getRequestHeaders()
        )
    } else {
        log.debug "There is no endpoint, requesting domotz for a new one"
        requestNewEndpoint()
    }
}



def getDeviceList() {
    try {

        def resp = [:]
        def attribute = null

        for (type in getSupportedTypes()) {
            type.get("obj").each {
                device = getDevice(it, resp)
                attribute = type.get("attribute")
                if (it.currentState(attribute)) {
                    device['attributes'][attribute] = [
                            "value": it.currentState(attribute).value,
                            "time" : it.currentState(attribute).getIsoDate(),
                            "unit" : it.currentState(attribute).unit
                    ]
                }

            }
        }
        return resp
    } catch (e) {
        log.debug("caught exception", e)
        return [:]
    }
}

def getDevice(it, resp) {
    if (resp[it.id]) {
        return resp[it.id]
    }
    resp[it.id] = [name: it.name, display_name: it.displayName, provider_uid: it.id, type: it.typeName, label: it.label, manufacturer_name: it.manufacturerName, model: it.modelName, attributes: [:]]

}

def activateMonitoring(resp) {
    unsubscribe()
    log.debug "Event monitoring activated for endpoint: ${request.JSON.endpoint}"
    atomicState.endpoint = request.JSON.endpoint
    log.debug "Event monitoring activated for endpoint: ${atomicState.endpoint}"
    initialize()
}

def deactivateMonitoring() {
    log.debug "Event monitoring deactivated."
    atomicState.endpoint = null
    unsubscribe()
}

def requestNewEndpoint() {
    log.debug "Requesting a new endpoint."
    def hubId = location.id
    def params = [
            uri    : "${appSettings.endpointRetrievalUrl}/${hubId}/endpoint",
            headers: getRequestHeaders()
    ]

    try {
        httpGet(params) { response ->
            log.debug "Request was successful, received endpoint: ${response.data.endpoint}"
            atomicState.endpoint = response.data.endpoint
            subscribeToDeviceEvents()
        }
    }
    catch (e) {
        log.debug "Unable to retrieve the endpoint"
    }

}

def handleClientUninstall() {
	log.info("Deactivated from client")
	try {
		app.delete()
	} catch (e) {
		unschedule()
		unsubscribe()
		httpError(500, "An error occurred during deleting SmartApp: ${e}")
	}
}

mappings {
    path("/device") {
        action:
        [
                GET: getDeviceList
        ]
    }
    path("/location") {
        action:
        [
                GET: getHubLocation
        ]
    }
    path("/monitoring") {
        action:
        [
                POST  : activateMonitoring,
                DELETE: deactivateMonitoring
        ]
    }
    path("/uninstall") {
        action
        [GET: handleClientUninstall]
    }
}