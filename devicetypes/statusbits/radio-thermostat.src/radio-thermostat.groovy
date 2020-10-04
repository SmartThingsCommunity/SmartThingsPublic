/**
 *  Radio Thermostat CT50 WiFi driver.
 *
 *  Author: Statusbits.com. Hubitat conversion by cometfish
 *
 *  --------------------------------------------------------------------------
 *
 *  Copyright © 2014 Statusbits.com
 *
 *  This program is free software: you can redistribute it and/or modify it
 *  under the terms of the GNU General Public License as published by the Free
 *  Software Foundation, either version 3 of the License, or (at your option)
 *  any later version.
 *
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *  or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 *  for more details.
 *
 *  You should have received a copy of the GNU General Public License along
 *  with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 *  --------------------------------------------------------------------------
 *
 */
metadata {
    definition (name:"Radio Thermostat", namespace:"statusbits", author:"geko@statusbits.com", importUrl: "https://github.com/cometfish/hubitat_driver_radio_thermostat/master/radio_thermostat.groovy") {
        capability "Initialize"
        capability "Thermostat"
        capability "TemperatureMeasurement"
        capability "Sensor"
        capability "Refresh"
        capability "Polling"
        
        attribute "temperature", "number"
        attribute "coolingSetpoint", "number"
		attribute "heatingSetpoint", "number"
		attribute "schedule", "json_object"
		attribute "supportedThermostatFanModes", "enum", ["on", "circulate", "auto"]
		attribute "supportedThermostatModes", "enum", ["auto", "off", "heat", "emergency heat", "cool"]
		attribute "thermostatFanMode", "enum", ["on", "circulate", "auto"]
		attribute "thermostatMode", "enum", ["auto", "off", "heat", "emergency heat", "cool"]
		attribute "thermostatOperatingState", "enum", ["heating", "pending cool", "pending heat", "vent economizer", "idle", "cooling", "fan only"]
		attribute "thermostatSetpoint", "number"

		command "auto"
		command "cool"
		command "emergencyHeat"
		command "fanAuto"
		command "fanCirculate"
		command "fanOn"
		command "heat"
		command "off"

        // Custom attributes
        attribute "fanState", "string"      // Fan operating state. Values: "on", "off"
        attribute "hold", "string"          // Target temperature Hold status. Values: "on", "off"
        attribute "connection", "string"    // Connection status string

        // Custom commands
        command "temperatureUp"
        command "temperatureDown"
        command "holdOn"
        command "holdOff"
        command "setTimeToCurrent"
    }
}

preferences {
    input("confIpAddr", "string", title:"Thermostat IP Address",
        required:true)

    input("confTcpPort", "number", title:"Thermostat TCP Port (default: 80)",
        required:true, defaultValue:80)

    input("pollingInterval", "number", title:"Polling interval in minutes (1 - 59)",
        required:true, defaultValue:5)
}

import groovy.json.JsonSlurper

def installed() {
    //log.debug "installed()"

    printTitle()
    initialize() 
}
def initialize() {
    // Initialize attributes to default values
    sendEvent(name:'temperature', value:70, isStateChange: true)
    sendEvent(name:'heatingSetpoint', value:70, isStateChange: true)
    sendEvent(name:'coolingSetpoint', value:72, isStateChange: true)
    sendEvent(name: "thermostatSetpoint", value: 72, isStateChange: true)
    sendEvent(name:'thermostatMode', value:'off', isStateChange: true)
    sendEvent(name:'thermostatFanMode', value:'auto', isStateChange: true)
    sendEvent(name: "supportedThermostatFanModes", value: ["on", "auto"], isStateChange: true)
	sendEvent(name: "supportedThermostatModes", value: ["off", "auto", "heat", "cool"], isStateChange: true)
	
    sendEvent(name:'thermostatOperatingState', value:'idle', isStateChange: true)
    updateDataValue("lastRunningMode", "heat")
    
    sendEvent(name:'fanState', value:'off', isStateChange: true)
    sendEvent(name:'hold', value:'off', isStateChange: true)
    sendEvent(name:'connection', value:'disconnected', isStateChange: true)
}

def updated() {
	//log.debug "updated with settings: ${settings}"

    printTitle()
    unschedule()

    if (device.currentValue('connection') == null) {
        sendEvent([name:'connection', value:'disconnected', displayed:false])
    }

    if (!settings.confIpAddr) {
	    log.warn "IP address is not set!"
        return
    }

    def port = settings.confTcpPort
    if (!port) {
	    log.warn "Using default TCP port 80!"
        port = 80
    }

    def dni = createDNI(settings.confIpAddr, port)
    device.deviceNetworkId = dni
    state.dni = dni
    state.hostAddress = "${settings.confIpAddr}:${settings.confTcpPort}"
    state.requestTime = 0
    state.responseTime = 0

    startPollingTask()
    //STATE()
}

def pollingTask() {
    //log.debug "pollingTask()"

    state.lastPoll = now()

    // Check connection status
    def requestTime = state.requestTime ?: 0
    def responseTime = state.responseTime ?: 0
    if (requestTime && (requestTime - responseTime) > 5000) {
        log.warn "No connection!"
        sendEvent([
            name:           'connection',
            value:          'disconnected',
            isStateChange:  true,
            displayed:      true
        ])
    }

    def updated = state.updated ?: 0
    if ((now() - updated) > 10000) {
        sendHubCommand(apiGet("/tstat"))
    }
}

def parse(String message) {
    //log.debug "parse(${message})"

    def msg = stringToMap(message)
    if (msg.headers) {
        // parse HTTP response headers
        def headers = new String(msg.headers.decodeBase64())
        def parsedHeaders = parseHttpHeaders(headers)
        //log.debug "parsedHeaders: ${parsedHeaders}"
        if (parsedHeaders.status != 200) {
            log.error "Server error: ${parsedHeaders.reason}"
            return null
        }

        // parse HTTP response body
        if (!msg.body) {
            log.error "HTTP response has no body"
            return null
        }

        def body = new String(msg.body.decodeBase64())
        def slurper = new JsonSlurper()
        def tstat = slurper.parseText(body)
        return parseTstatData(tstat)
    } else if (msg.containsKey("simulator")) {
        // simulator input
        return parseTstatData(msg)
    }

    return null
}

// thermostat.setThermostatMode
def setThermostatMode(mode) {
    //log.debug "setThermostatMode(${mode})"

    switch (mode) {
    case "off":             return off()
    case "heat":            return heat()
    case "cool":            return cool()
    case "auto":            return auto()
    case "emergency heat":  return emergencyHeat()
    }

    log.error "Invalid thermostat mode: \'${mode}\'"
    return null
}

// thermostat.off
def off() {
    //log.debug "off()"

    if (device.currentValue("thermostatMode") == "off") {
        return null
    }

    log.info "Setting thermostat mode to 'off'"
    sendEvent([name:"thermostatMode", value:"off"])

    return writeTstatValue('tmode', 0)
}

// thermostat.heat
def heat() {
    //log.debug "heat()"

    if (device.currentValue("thermostatMode") == "heat") {
        return null
    }

    log.info "Setting thermostat mode to 'heat'"
    sendEvent([name:"thermostatMode", value:"heat"])

    return writeTstatValue('tmode', 1)
}

// thermostat.cool
def cool() {
    //log.debug "cool()"

    if (device.currentValue("thermostatMode") == "cool") {
        return null
    }

    log.info "Setting thermostat mode to 'cool'"
    sendEvent([name:"thermostatMode", value:"cool"])

    return writeTstatValue('tmode', 2)
}

// thermostat.auto
def auto() {
    //log.debug "auto()"

    if (device.currentValue("thermostatMode") == "auto") {
        return null
    }

    log.info "Setting thermostat mode to 'auto'"
    sendEvent([name:"thermostatMode", value:"auto"])

    return writeTstatValue('tmode', 3)
}

// thermostat.emergencyHeat
def emergencyHeat() {
    //log.debug "emergencyHeat()"
    log.warn "'emergency heat' mode is not supported"
    return null
}

// thermostat.setThermostatFanMode
def setThermostatFanMode(fanMode) {
    //log.debug "setThermostatFanMode(${fanMode})"

    switch (fanMode) {
    case "auto":        return fanAuto()
    case "circulate":   return fanCirculate()
    case "on":          return fanOn()
    }

    log.error "Invalid fan mode: \'${fanMode}\'"
    return null
}

// thermostat.fanAuto
def fanAuto() {
    //log.debug "fanAuto()"

    if (device.currentValue("thermostatFanMode") == "auto") {
        return null
    }

    log.info "Setting fan mode to 'auto'"
    sendEvent([name:"thermostatFanMode", value:"auto"])

    return writeTstatValue('fmode', 0)
}

// thermostat.fanCirculate
def fanCirculate() {
    //log.debug "fanCirculate()")
    log.warn "Fan 'Circulate' mode is not supported"
    return null
}

// thermostat.fanOn
def fanOn() {
    //log.debug "fanOn()"

    if (device.currentValue("thermostatFanMode") == "on") {
        return null
    }

    log.info "Setting fan mode to 'on'"
    sendEvent([name:"thermostatFanMode", value:"on"])

    return writeTstatValue('fmode', 2)
}

// thermostat.setHeatingSetpoint
def setHeatingSetpoint(temp) {
    //log.debug "setHeatingSetpoint(${temp})"

    double minT = 36.0
    double maxT = 94.0
    def scale = getTemperatureScale()
    double t = (scale == "C") ? temperatureCtoF(temp) : temp

    t = t.round()
    if (t < minT) {
        log.warn "Cannot set heating target below ${minT} °F."
        return null
    } else if (t > maxT) {
        log.warn "Cannot set heating target above ${maxT} °F."
        return null
    }

    log.info "Setting heating setpoint to ${t} °F"
    double tval = (scale == "C") ? temperatureFtoC(t) : t
    def ev = [
        name:   "heatingSetpoint",
        value:  tval,
        unit:   scale,
    ]

    sendEvent(ev)
    if (device.currentValue("thermostatMode") == "heat") {
        sendEvent(name: "thermostatSetpoint", value: tval, isStateChange: true)
    }

    return writeTstatValue('it_heat', t)
}

// thermostat.setCoolingSetpoint
def setCoolingSetpoint(temp) {
    //log.debug "setCoolingSetpoint(${temp})"

    double minT = 36.0
    double maxT = 94.0
    def scale = getTemperatureScale()
    double t = (scale == "C") ? temperatureCtoF(temp) : temp

    t = t.round()
    if (t < minT) {
        log.warn "Cannot set cooling target below ${minT} °F."
        return null
    } else if (t > maxT) {
        log.warn "Cannot set cooling target above ${maxT} °F."
        return null
    }

    log.info "Setting cooling setpoint to ${t} °F"

    double tval = (scale == "C") ? temperatureFtoC(t) : t;
    def ev = [
        name:   "coolingSetpoint",
        value:  tval,
        unit:   scale,
    ]

    sendEvent(ev)
    if (device.currentValue("thermostatMode") == "heat") {
        sendEvent(name: "thermostatSetpoint", value: tval, isStateChange: true)
    }

    return writeTstatValue('it_cool', t)
}

// Custom command
def temperatureUp() {
    //log.debug "temperatureUp()"

    def step = (getTemperatureScale() == "C") ? 0.5 : 1
    def mode = device.currentValue("thermostatMode")
    if (mode == "heat") {
        def t = device.currentValue("heatingSetpoint")?.toFloat()
        if (!t) {
            log.error "Cannot get current heating setpoint."
            return null
        }
        return setHeatingSetpoint(t + step)
    } else if (mode == "cool") {
        def t = device.currentValue("coolingSetpoint")?.toFloat()
        if (!t) {
            log.error "Cannot get current cooling setpoint."
            return null
        } 
        return setCoolingSetpoint(t + step)
    } else {
        log.warn "Cannot change temperature while in '${mode}' mode."
        return null
    }
}

// Custom command
def temperatureDown() {
    //log.debug "temperatureDown()"

    def step = (getTemperatureScale() == "C") ? 0.5 : 1
    def mode = device.currentValue("thermostatMode")
    if (mode == "heat") {
        def t = device.currentValue("heatingSetpoint")?.toFloat()
        if (!t) {
            log.error "Cannot get current heating setpoint."
            return null
        }
 
        return setHeatingSetpoint(t - step)
    } else if (mode == "cool") {
        def t = device.currentValue("coolingSetpoint")?.toFloat()
        if (!t) {
            log.error "Cannot get current cooling setpoint."
            return null
        }
 
        return setCoolingSetpoint(t - step)
    } else {
        log.warn "Cannot change temperature while in '${mode}' mode."
        return null
    }
}

// Custom command
def holdOn() {
    //log.debug "holdOn()"

    if (device.currentValue("hold") == "on") {
        return null
    }

    log.info "Setting temperature hold to 'on'"
    sendEvent([name:"hold", value:"on"])

    return writeTstatValue("hold", 1)
}

// Custom command
def holdOff() {
    //log.debug "holdOff()"

    if (device.currentValue("hold") == "off") {
        return null
    }

    log.info "Setting temperature hold to 'off'"
    sendEvent([name:"hold", value:"off"])

    return writeTstatValue("hold", 0)
}

// Custom command
def setTimeToCurrent() {
    Date date = new Date()
  
    log.info "Setting current time"
    
    int dow = date[Calendar.DAY_OF_WEEK] - 2
    if (dow<0)
        dow = dow + 7
    apiPost("/tstat/time", "{\"hour\":" + date.format("H") + ", \"minute\": " + date.format("m") + ", \"day\":" + dow + "}")

}

// polling.poll 
def poll() {
    //log.debug "poll()"
    return refresh()
}

// refresh.refresh
def refresh() {
    //log.debug "refresh()"
    //STATE()

    if (!state.dni) {
	    log.warn "DNI is not set! Please enter device IP address and port in settings."
        sendEvent([
            name:           'connection',
            value:          'disconnected',
            isStateChange:  true,
            displayed:      false
        ])

        return null
    }

    def interval = getPollingInterval() * 60
    def elapsed =  (now() - state.lastPoll) / 1000
    if (elapsed > (interval + 300)) {
        log.warn "Restarting polling task..."
        unschedule()
        startPollingTask()
    }

    return apiGet("/tstat")
}

private getPollingInterval() {
    def minutes = settings.pollingInterval?.toInteger()
    if (!minutes) {
	    log.warn "Using default polling interval: 5!"
        minutes = 5
    } else if (minutes < 1) {
        minutes = 1
    } else if (minutes > 59) {
        minutes = 59
    }

    return minutes
}

private startPollingTask() {
    //log.debug "startPollingTask()"

    pollingTask()

    Random rand = new Random(now())
    def seconds = rand.nextInt(60)
    def sched = "${seconds} 0/${getPollingInterval()} * * * ?"

    //log.debug "Scheduling polling task with \"${sched}\""
    schedule(sched, pollingTask)
}

private apiGet(String path) {
    //log.debug "apiGet(${path})"

    if (!updateDNI()) {
        return null
    }

    state.requestTime = now()

    def headers = [
        HOST:       state.hostAddress,
        Accept:     "*/*"
    ]

    def httpRequest = [
        method:     'GET',
        path:       path,
        headers:    headers
    ]

    return new hubitat.device.HubAction(httpRequest)
}

private apiPost(String path, data) {
    //log.debug "apiPost(${path}, ${data})"

    if (!updateDNI()) {
        return null
    }

    state.requestTime = now()

    def headers = [
        HOST:       state.hostAddress,
        Accept:     "*/*"
    ]

    def httpRequest = [
        method:     'POST',
        path:       path,
        headers:    headers,
        body:       data
    ]

    return new hubitat.device.HubAction(httpRequest)
}

private def writeTstatValue(name, value) {
    //log.debug "writeTstatValue(${name}, ${value})"

    def json = "{\"${name}\": ${value}}"
    def hubActions = [
        apiPost("/tstat", json),
        delayHubAction(1500),
        apiGet("/tstat")
    ]

    return hubActions
}

private def delayHubAction(ms) {
    return new hubitat.device.HubAction("delay ${ms}")
}

private parseHttpHeaders(String headers) {
    def lines = headers.readLines()
    def status = lines.remove(0).split()

    def result = [
        protocol:   status[0],
        status:     status[1].toInteger(),
        reason:     status[2]
    ]

    return result
}

private def parseTstatData(Map tstat) {
    log.trace "tstat data: ${tstat}"

    state.responseTime = now()

    def events = []
    if (tstat.containsKey("error_msg")) {
        log.error "Thermostat error: ${tstat.error_msg}"
        return null
    }

    if (tstat.containsKey("success")) {
        // this is POST response - ignore
        return null
    }

    if (tstat.containsKey("temp")) {
        events << createEvent([
            name:   "temperature",
            value:  scaleTemperature(tstat.temp.toFloat()),
            unit:   getTemperatureScale(),
        ])
    }
    mode = device.currentValue("thermostatMode")
    if (tstat.containsKey("tmode")) {
        mode = parseThermostatMode(tstat.tmode)
        events << createEvent([
            name:   "thermostatMode",
            value:  mode
        ])
    }

    if (tstat.containsKey("t_cool")) {
        tval = scaleTemperature(tstat.t_cool.toFloat())
        events << createEvent([
            name:   "coolingSetpoint",
            value:  tval,
            unit:   getTemperatureScale(),
        ])
        if (mode == "cool") {
            events << createEvent([
                name:   "thermostatSetpoint",
                value:  tval,
                unit:   getTemperatureScale(),
            ])
        }
    }

    if (tstat.containsKey("t_heat")) {
        tval = scaleTemperature(tstat.t_heat.toFloat())
        events << createEvent([
            name:   "heatingSetpoint",
            value:  tval,
            unit:   getTemperatureScale(),
        ])
        if (mode == "heat") {
            events << createEvent([
                name:   "thermostatSetpoint",
                value:  tval,
                unit:   getTemperatureScale(),
            ])
        }
    }

    if (tstat.containsKey("tstate")) {
        events << createEvent([
            name:   "thermostatOperatingState",
            value:  parseThermostatState(tstat.tstate)
        ])
    }

    if (tstat.containsKey("fstate")) {
        events << createEvent([
            name:   "fanState",
            value:  parseFanState(tstat.fstate)
        ])
    }

    if (tstat.containsKey("fmode")) {
        events << createEvent([
            name:   "thermostatFanMode",
            value:  parseFanMode(tstat.fmode)
        ])
    }

    if (tstat.containsKey("hold")) {
        events << createEvent([
            name:   "hold",
            value:  parseThermostatHold(tstat.hold)
        ])
    }

    events << createEvent([
        name:           'connection',
        value:          'connected',
        isStateChange:  true,
        displayed:      false
    ])

    state.updated = now()

    //log.debug "events: ${events}"
    return events
}

private def parseThermostatState(val) {
    def values = [
        "idle",     // 0
        "heating",  // 1
        "cooling"   // 2
    ]

    return values[val.toInteger()]
}

private def parseFanState(val) {
    def values = [
        "off",      // 0
        "on"        // 1
    ]

    return values[val.toInteger()]
}

private def parseThermostatMode(val) {
    def values = [
        "off",      // 0
        "heat",     // 1
        "cool",     // 2
        "auto"      // 3
    ]

    return values[val.toInteger()]
}

private def parseFanMode(val) {
    def values = [
        "auto",     // 0
        "circulate",// 1 (not supported by CT30)
        "on"        // 2
    ]

    return values[val.toInteger()]
}

private def parseThermostatHold(val) {
    def values = [
        "off",      // 0
        "on"        // 1
    ]

    return values[val.toInteger()]
}

private def scaleTemperature(Double temp) {
    if (getTemperatureScale() == "C") {
        return temperatureFtoC(temp)
    }

    return temp.round(1)
}

private def temperatureCtoF(Double tempC) {
    Double t = (tempC * 1.8) + 32
    return t.round(1)
}

private def temperatureFtoC(Double tempF) {
    Double t = (tempF - 32) / 1.8
    return t.round(1)
}

private String createDNI(ipaddr, port) {
    //log.debug "createDNI(${ipaddr}, ${port})"

    def hexIp = ipaddr.tokenize('.').collect {
        String.format('%02X', it.toInteger())
    }.join()

    def hexPort = String.format('%04X', port.toInteger())
 
    return "${hexIp}:${hexPort}"
}

private updateDNI() {
    if (!state.dni) {
	    log.warn "DNI is not set! Please enter device IP address and port in settings."
        return false
    }
 
    if (state.dni != device.deviceNetworkId) {
	    log.warn "Invalid DNI: ${device.deviceNetworkId}!"
        device.deviceNetworkId = state.dni
    }

    return true
}

private def printTitle() {
    log.info "Radio Thermostat. ${textVersion()}. ${textCopyright()}"
}

private def textVersion() {
    return "Version 2.0.1 (12/20/2016)"
}

private def textCopyright() {
    return "Copyright © 2014 Statusbits.com"
}

private def STATE() {
    log.trace "state: ${state}"
    log.trace "deviceNetworkId: ${device.deviceNetworkId}"
    log.trace "temperature: ${device.currentValue("temperature")}"
    log.trace "heatingSetpoint: ${device.currentValue("heatingSetpoint")}"
    log.trace "coolingSetpoint: ${device.currentValue("coolingSetpoint")}"
    log.trace "thermostatOperatingState: ${device.currentValue("thermostatOperatingState")}"
    log.trace "thermostatMode: ${device.currentValue("thermostatMode")}"
    log.trace "thermostatFanMode: ${device.currentValue("thermostatFanMode")}"
    log.trace "fanState: ${device.currentValue("fanState")}"
    log.trace "hold: ${device.currentValue("hold")}"
    log.trace "connection: ${device.currentValue("connection")}"
}