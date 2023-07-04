/**
 *  Nest Direct
 *
 *  Author: dianoga7@3dgo.net
 *  Code: https://github.com/smartthings-users/device-type.nest
 */
 
preferences {
    input("username", "text", title: "Username", description: "Your Nest username (usually an email address)")
    input("password", "password", title: "Password", description: "Your Nest password")
    input("serial", "text", title: "Serial #", description: "The serial number of your thermostat")
}

 // for the UI
metadata {
    definition (name: "Nest Thermostat", namespace: "juano2310", author: "juano23@gmail.com") {
        capability "Polling"
        capability "Relative Humidity Measurement"
        capability "Thermostat"
        capability "Temperature Measurement"

        attribute "presence", "string"

        command "away"
        command "present"
        command "setPresence"
        command "range"
    }

    simulator {
        // TODO: define status and reply messages here
    }

    tiles {
        valueTile("temperature", "device.temperature", width: 2, height: 2, canChangeIcon: true) {
            state("temperature", label: '${currentValue}°', unit:"F", backgroundColors: [
                    [value: 31, color: "#153591"],
                    [value: 44, color: "#1e9cbb"],
                    [value: 59, color: "#90d2a7"],
                    [value: 74, color: "#44b621"],
                    [value: 84, color: "#f1d801"],
                    [value: 95, color: "#d04e00"],
                    [value: 96, color: "#bc2323"]
                ]
            )
        }
        standardTile("thermostatMode", "device.thermostatMode", inactiveLabel: false) {
            state("range", label:'${name}', action:"thermostat.off", icon: "st.Weather.weather14", backgroundColor: '#714377')
            state("off", label:'${name}', action:"thermostat.cool", icon: "st.Outdoor.outdoor19")
            state("cool", label:'${name}', action:"thermostat.heat", icon: "st.Weather.weather7", backgroundColor: '#003CEC')
            state("heat", label:'${name}', action:"range", icon: "st.Weather.weather14", backgroundColor: '#E14902')
        }
        standardTile("thermostatFanMode", "device.thermostatFanMode", inactiveLabel: false, decoration: "flat") {
            state "auto", label:'${name}', action:"thermostat.fanOn", icon: "st.Appliances.appliances11"
            state "on", label:'${name}', action:"thermostat.fanCirculate", icon: "st.Appliances.appliances11"
            state "circulate", label:'${name}', action:"thermostat.fanAuto", icon: "st.Appliances.appliances11"
        }
        controlTile("coolSliderControl", "device.coolingSetpoint", "slider", height: 1, width: 2, inactiveLabel: false) {
            state "setCoolingSetpoint", label:'Set temperarure to', action:"thermostat.setCoolingSetpoint",
            backgroundColors:[
                [value: 31, color: "#153591"],
                [value: 44, color: "#1e9cbb"],
                [value: 59, color: "#90d2a7"],
                [value: 74, color: "#44b621"],
                [value: 84, color: "#f1d801"],
                [value: 95, color: "#d04e00"],
                [value: 96, color: "#bc2323"]
            ]
        }
        valueTile("coolingSetpoint", "device.coolingSetpoint", inactiveLabel: false, decoration: "flat") {
            state "default", label:'${currentValue}°', unit:"F", backgroundColor:"#ffffff", icon:"st.appliances.appliances8"
        }
        valueTile("heatingSetpoint", "device.heatingSetpoint", inactiveLabel: false, decoration: "flat") {
            state "default", label:'${currentValue}°', unit:"F", backgroundColor:"#ffffff", icon:"st.appliances.appliances8"
        }
        valueTile("humidity", "device.humidity", inactiveLabel: false) {
            state "default", label:'${currentValue}% Humidity', unit:"Humidity"
        }
        standardTile("presence", "device.presence", inactiveLabel: false, decoration: "flat") {
            state "present", label:'${name}', action:"away", icon: "st.Home.home2"
            state "away", label:'${name}', action:"present", icon: "st.Transportation.transportation5"
        }
        standardTile("refresh", "device.thermostatMode", inactiveLabel: false, decoration: "flat") {
            state "default", action:"polling.poll", icon:"st.secondary.refresh"
        }
        main "temperature"
        details(["temperature", "coolingSetpoint", "heatingSetpoint", "coolSliderControl", "heatSliderControl", "thermostatMode", "presence", "thermostatFanMode", "humidity", "refresh"])
    }
}

// parse events into attributes
def parse(String description) {

}

// handle commands
def setHeatingSetpoint(temp) {
    def latestThermostatMode = device.latestState('thermostatMode')
    
    if (temp) {
        if (latestThermostatMode.stringValue == 'range') {
            api('temperature', ['target_change_pending': true, 'target_temperature_low': fToC(temp)]) {
                sendEvent(name: 'heatingSetpoint', value: temp)
            }
        } else if (latestThermostatMode.stringValue == 'cool') {
            api('temperature', ['target_change_pending': true, 'target_temperature': fToC(temp)]) {
                sendEvent(name: 'heatingSetpoint', value: temp)
            }
        }
    }
}

def setCoolingSetpoint(temp) {
    def latestThermostatMode = device.latestState('thermostatMode')
    
    if (temp) {
        if (latestThermostatMode.stringValue == 'range') {
            api('temperature', ['target_change_pending': true, 'target_temperature_high': fToC(temp)]) {
                sendEvent(name: 'coolingSetpoint', value: temp)
            }
        } else if (latestThermostatMode.stringValue == 'cool') {
            api('temperature', ['target_change_pending': true, 'target_temperature': fToC(temp)]) {
                sendEvent(name: 'coolingSetpoint', value: temp)
            }
        }
    }
}

def off() {
    setThermostatMode('off')
}

def heat() {
    setThermostatMode('heat')
}

def emergencyHeat() {
    setThermostatMode('heat')
}

def cool() {
    setThermostatMode('cool')
}

def range() {
    setThermostatMode('range')
}

def setThermostatMode(mode) {
    mode = mode == 'emergency heat'? 'heat' : mode
    
    api('thermostat_mode', ['target_change_pending': true, 'target_temperature_type': mode]) {
        sendEvent(name: 'thermostatMode', value: mode)
        poll()
    }
}

def fanOn() {
    setThermostatFanMode('on')
}

def fanAuto() {
    setThermostatFanMode('auto')
}

def fanCirculate() {
    setThermostatFanMode('circulate')
}

def setThermostatFanMode(mode) {
    def modes = [
        on: ['fan_mode': 'on'],
        auto: ['fan_mode': 'auto'],
        circulate: ['fan_mode': 'duty-cycle', 'fan_duty_cycle': 900]
    ]

    api('fan_mode', modes.getAt(mode)) {
        sendEvent(name: 'thermostatFanMode', value: mode)
    }
}

def away() {
    setPresence('away')
}

def present() {
    setPresence('present')
}

def setPresence(status) {
    log.debug "Status: $status"
    api('presence', ['away': status == 'away', 'away_timestamp': new Date().getTime(), 'away_setter': 0]) {
        sendEvent(name: 'presence', value: status)
    }
}

def auto() {
    log.debug "Executing 'auto'"
}

def poll() {
    log.debug "Executing 'poll'"
    api('status', []) {
        data.device = it.data.device.getAt(settings.serial)
        data.shared = it.data.shared.getAt(settings.serial)
        data.structureId = it.data.link.getAt(settings.serial).structure.tokenize('.')[1]
        data.structure = it.data.structure.getAt(data.structureId)
                
        data.device.fan_mode = data.device.fan_mode == 'duty-cycle'? 'circulate' : data.device.fan_mode
        data.structure.away = data.structure.away ? 'away' : 'present'
        
        log.debug(data.shared)
        
        def humidity = data.device.current_humidity
        def temperature = Math.round(cToF(data.shared.current_temperature))
        def temperatureType = data.shared.target_temperature_type
        def fanMode = data.device.fan_mode
        
        sendEvent(name: 'humidity', value: humidity)
        sendEvent(name: 'temperature', value: temperature, state: temperatureType)
        sendEvent(name: 'thermostatFanMode', value: fanMode)
        sendEvent(name: 'thermostatMode', value: temperatureType)
        
        def targetTemperature = Math.round(cToF(data.shared.target_temperature))
        def heatingSetpoint = '--'
        def coolingSetpoint = '--'
        
        if (temperatureType == "cool") {
            coolingSetpoint = targetTemperature
        } else if (temperatureType == "heat") {
            heatingSetpoint = targetTemperature
        } else if (temperatureType == "range") {
            coolingSetpoint = Math.round(cToF(data.shared.target_temperature_high))
            heatingSetpoint = Math.round(cToF(data.shared.target_temperature_low))
        }
        
        sendEvent(name: 'coolingSetpoint', value: coolingSetpoint)
        sendEvent(name: 'heatingSetpoint', value: heatingSetpoint)
        sendEvent(name: 'presence', value: data.structure.away)
    }
}

def api(method, args = [], success = {}) {
    if(!isLoggedIn()) {
        log.debug "Need to login"
        login(method, args, success)
        return
    }

    def methods = [
        'status': [uri: "/v2/mobile/${data.auth.user}", type: 'get'],
        'fan_mode': [uri: "/v2/put/device.${settings.serial}", type: 'post'],
        'thermostat_mode': [uri: "/v2/put/shared.${settings.serial}", type: 'post'],
        'temperature': [uri: "/v2/put/shared.${settings.serial}", type: 'post'],
        'presence': [uri: "/v2/put/structure.${data.structureId}", type: 'post']
    ]

    def request = methods.getAt(method)

    log.debug "Logged in"
    doRequest(request.uri, args, request.type, success)
}

// Need to be logged in before this is called. So don't call this. Call api.
def doRequest(uri, args, type, success) {
    log.debug "Calling $type : $uri : $args"

    if(uri.charAt(0) == '/') {
        uri = "${data.auth.urls.transport_url}${uri}"
    }

    def params = [
        uri: uri,
        headers: [
            'X-nl-protocol-version': 1,
            'X-nl-user-id': data.auth.userid,
            'Authorization': "Basic ${data.auth.access_token}"
        ],
        body: args
    ]

    try {
        if(type == 'post') {
            httpPostJson(params, success)
        } else if (type == 'get') {
            httpGet(params, success)
        }
    } catch (Throwable e) {
        login()
    }
}

def login(method = null, args = [], success = {}) {
    def params = [
        uri: 'https://home.nest.com/user/login',
        body: [username: settings.username, password: settings.password]
    ]

    httpPost(params) {response ->
        data.auth = response.data
        data.auth.expires_in = Date.parse('EEE, dd-MMM-yyyy HH:mm:ss z', response.data.expires_in).getTime()
        log.debug data.auth

        api(method, args, success)
    }
}

def isLoggedIn() {
    if(!data.auth) {
        log.debug "No data.auth"
        return false
    }

    def now = new Date().getTime();
    return data.auth.expires_in > now
}

def cToF(temp) {
    return temp * 1.8 + 32
}

def fToC(temp) {
    return (temp - 32) / 1.8
}