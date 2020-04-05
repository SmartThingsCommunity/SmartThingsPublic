metadata {
	// Automatically generated. Make future change here.
	definition (name: "Custom Thermostat with Temperature", namespace: "Thermostat", author: "RBoy") {
		capability "Actuator"
		capability "Temperature Measurement"
		capability "Relative Humidity Measurement"
		capability "Thermostat"
		capability "Configuration"
		capability "Polling"
		capability "Sensor"
		capability "Refresh"
		capability "Battery"
    
		attribute "thermostatFanState", "string"

		command "switchMode"
		command "switchFanMode"
        command "quickSetCool"
        command "quickSetHeat"
		command "refresh"
		command "heatLevelUp"
		command "heatLevelDown"
		command "coolLevelUp"
		command "coolLevelDown"
		
		fingerprint deviceId: "0x08"
		fingerprint inClusters: "0x43,0x40,0x44,0x31"
	}

	// simulator metadata
	simulator {
		status "off"			: "command: 4003, payload: 00"
		status "heat"			: "command: 4003, payload: 01"
		status "cool"			: "command: 4003, payload: 02"
		status "auto"			: "command: 4003, payload: 03"
		status "emergencyHeat"	: "command: 4003, payload: 04"

		status "fanAuto"		: "command: 4403, payload: 00"
		status "fanOn"			: "command: 4403, payload: 01"
		status "fanCirculate"	: "command: 4403, payload: 06"

		status "heat 60"        : "command: 4303, payload: 01 09 3C"
		status "heat 68"        : "command: 4303, payload: 01 09 44"
		status "heat 72"        : "command: 4303, payload: 01 09 48"

		status "cool 72"        : "command: 4303, payload: 02 09 48"
		status "cool 76"        : "command: 4303, payload: 02 09 4C"
		status "cool 80"        : "command: 4303, payload: 02 09 50"

		status "temp 58"        : "command: 3105, payload: 01 2A 02 44"
		status "temp 62"        : "command: 3105, payload: 01 2A 02 6C"
		status "temp 70"        : "command: 3105, payload: 01 2A 02 BC"
		status "temp 74"        : "command: 3105, payload: 01 2A 02 E4"
		status "temp 78"        : "command: 3105, payload: 01 2A 03 0C"
		status "temp 82"        : "command: 3105, payload: 01 2A 03 34"

		status "idle"			: "command: 4203, payload: 00"
		status "heating"		: "command: 4203, payload: 01"
		status "cooling"		: "command: 4203, payload: 02"
		status "fan only"		: "command: 4203, payload: 03"
		status "pending heat"	: "command: 4203, payload: 04"
		status "pending cool"	: "command: 4203, payload: 05"
		status "vent economizer": "command: 4203, payload: 06"

		// reply messages
		reply "2502": "command: 2503, payload: FF"
	}

	tiles {
		valueTile("temperature", "device.temperature", width: 2, height: 2) {
			state("temperature", label:'${currentValue}°',
				backgroundColors:[
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
	standardTile("mode", "device.thermostatMode", inactiveLabel: false, canChangeIcon: true) {
		state "off", label:'${name}', action:"switchMode", nextState:"to_heat", icon: "st.Outdoor.outdoor19"
		state "heat", label:'${name}', action:"switchMode", nextState:"to_cool", icon: "st.Weather.weather14", backgroundColor: '#E14902'
		state "cool", label:'${name}', action:"switchMode", nextState:"...", icon: "st.Weather.weather7", backgroundColor: '#003CEC'
		state "auto", label:'${name}', action:"switchMode", nextState:"...", icon: "st.Home.home1"
		state "emergencyHeat", label:'${name}', action:"switchMode", nextState:"...", icon: "st.Weather.weather2", backgroundColor: '#E11102'
		state "to_heat", label: "heat", action:"switchMode", nextState:"to_cool"
		state "to_cool", label: "cool", action:"switchMode", nextState:"..."
		state "...", label: "...", action:"off", nextState:"off"
	}
	standardTile("fanMode", "device.thermostatFanMode", inactiveLabel: false, canChangeIcon: true) {
		state "fanAuto", label:'${name}', action:"switchFanMode", icon: "st.Appliances.appliances11"
		state "fanOn", label:'${name}', action:"switchFanMode", icon: "st.Appliances.appliances11", backgroundColor: '#02E181'
		state "fanCirculate", label:'${name}', action:"switchFanMode", icon: "st.Appliances.appliances11", backgroundColor: '#02D2E1'
	}

		standardTile("heatLevelUp", "device.heatLevelUp", inactiveLabel: false, decoration: "flat") {
            state "heatLevelUp", label:'  ', action:"heatLevelUp", icon:"st.thermostat.thermostat-up"
		}
		standardTile("heatLevelDown", "device.heatLevelDown", inactiveLabel: false, decoration: "flat") {
            state "heatLevelDown", label:'  ', action:"heatLevelDown", icon:"st.thermostat.thermostat-down"
		}
		standardTile("coolLevelUp", "device.coolLevelUp", inactiveLabel: false, decoration: "flat") {
            state "coolLevelUp", label:'  ', action:"coolLevelUp", icon:"st.thermostat.thermostat-up"
		}
		standardTile("coolLevelDown", "device.coolLevelDown", inactiveLabel: false, decoration: "flat") {
            state "coolLevelDown", label:'  ', action:"coolLevelDown", icon:"st.thermostat.thermostat-down"
		}
		// END CUSTOMIZATION		
		valueTile("heatingSetpoint", "device.heatingSetpoint", inactiveLabel: false, decoration: "flat") {
			state "heat", label:'${currentValue}° heat', backgroundColor:"#ffffff"
		}
		valueTile("coolingSetpoint", "device.coolingSetpoint", inactiveLabel: false, decoration: "flat") {
			state "cool", label:'${currentValue}° cool', backgroundColor:"#ffffff"
		}
		standardTile("configure", "device.configure", inactiveLabel: false, decoration: "flat") {
			state "configure", label:'', action:"configuration.configure", icon:"st.secondary.configure"
		}
		standardTile("refresh", "command.refresh", inactiveLabel: false, decoration: "flat") {
			state "default", action:"refresh.refresh", icon:"st.secondary.refresh"
		}
		valueTile("battery", "device.battery", inactiveLabel: false, decoration: "flat") { 
			state "battery", label:'Battery ${currentValue}%', backgroundColor:"#ffffff" 
		}
		valueTile("humidity", "device.humidity", inactiveLabel: false, decoration: "flat") { 
			state "humidity", label:'Humidity ${currentValue}%', backgroundColor:"#ffffff"
		}
		main "temperature"
		details(["temperature", "mode", "fanMode", "heatLevelDown", "heatingSetpoint", "heatLevelUp", "coolLevelDown", "coolingSetpoint", "coolLevelUp", "battery", "humidity", "refresh", "configure"])
	}
}

def parse(String description)
{
	def map = createEvent(zwaveEvent(zwave.parse(description, [0x42:1, 0x43:2, 0x31: 3])))
	if (!map) {
		return null
	}

	def result = [map]
	if (map.isStateChange && map.name in ["heatingSetpoint","coolingSetpoint","thermostatMode"]) {
		def map2 = [
			name: "thermostatSetpoint",
			unit: getTemperatureScale()
		]
		if (map.name == "thermostatMode") {
			state.lastTriedMode = map.value
			if (map.value == "cool") {
				map2.value = device.latestValue("coolingSetpoint")
				log.info "THERMOSTAT, latest cooling setpoint = ${map2.value}"
			}
			else {
				map2.value = device.latestValue("heatingSetpoint")
				log.info "THERMOSTAT, latest heating setpoint = ${map2.value}"
			}
		}
		else {
			def mode = device.latestValue("thermostatMode")
			log.info "THERMOSTAT, latest mode = ${mode}"
			if ((map.name == "heatingSetpoint" && mode == "heat") || (map.name == "coolingSetpoint" && mode == "cool")) {
				map2.value = map.value
				map2.unit = map.unit
			}
		}
		if (map2.value != null) {
			log.debug "THERMOSTAT, adding setpoint event: $map"
			result << createEvent(map2)
		}
	} else if (map.name == "thermostatFanMode" && map.isStateChange) {
		state.lastTriedFanMode = map.value
	}
	log.debug "Parse returned $result"
	result
}

// Event Generation
def zwaveEvent(physicalgraph.zwave.commands.thermostatsetpointv2.ThermostatSetpointReport cmd)
{
	def cmdScale = cmd.scale == 1 ? "F" : "C"
	def map = [:]
	map.value = convertTemperatureIfNeeded(cmd.scaledValue, cmdScale, cmd.precision)
	map.unit = getTemperatureScale()
	map.displayed = false
	switch (cmd.setpointType) {
		case 1:
			map.name = "heatingSetpoint"
			break;
		case 2:
			map.name = "coolingSetpoint"
			break;
		default:
			return [:]
	}
	// So we can respond with same format
	state.size = cmd.size
	state.scale = cmd.scale
	state.precision = cmd.precision
	map
}

def zwaveEvent(physicalgraph.zwave.commands.sensormultilevelv3.SensorMultilevelReport cmd)
{
	def map = [:]
	if (cmd.sensorType == 1) {
		map.value = convertTemperatureIfNeeded(cmd.scaledSensorValue, cmd.scale == 1 ? "F" : "C", cmd.precision)
		map.unit = getTemperatureScale()
		map.name = "temperature"
	} else if (cmd.sensorType == 5) {
		map.value = cmd.scaledSensorValue
		map.unit = "%"
		map.name = "humidity"
	}
	map
}

def zwaveEvent(physicalgraph.zwave.commands.thermostatoperatingstatev1.ThermostatOperatingStateReport cmd)
{
	def map = [:]
	switch (cmd.operatingState) {
		case physicalgraph.zwave.commands.thermostatoperatingstatev1.ThermostatOperatingStateReport.OPERATING_STATE_IDLE:
			map.value = "idle"
			break
		case physicalgraph.zwave.commands.thermostatoperatingstatev1.ThermostatOperatingStateReport.OPERATING_STATE_HEATING:
			map.value = "heating"
			break
		case physicalgraph.zwave.commands.thermostatoperatingstatev1.ThermostatOperatingStateReport.OPERATING_STATE_COOLING:
			map.value = "cooling"
			break
		case physicalgraph.zwave.commands.thermostatoperatingstatev1.ThermostatOperatingStateReport.OPERATING_STATE_FAN_ONLY:
			map.value = "fan only"
			break
		case physicalgraph.zwave.commands.thermostatoperatingstatev1.ThermostatOperatingStateReport.OPERATING_STATE_PENDING_HEAT:
			map.value = "pending heat"
			break
		case physicalgraph.zwave.commands.thermostatoperatingstatev1.ThermostatOperatingStateReport.OPERATING_STATE_PENDING_COOL:
			map.value = "pending cool"
			break
		case physicalgraph.zwave.commands.thermostatoperatingstatev1.ThermostatOperatingStateReport.OPERATING_STATE_VENT_ECONOMIZER:
			map.value = "vent economizer"
			break
	}
	map.name = "thermostatOperatingState"
	map
}

def zwaveEvent(physicalgraph.zwave.commands.thermostatfanstatev1.ThermostatFanStateReport cmd) {
	def map = [name: "thermostatFanState", unit: ""]
	switch (cmd.fanOperatingState) {
		case 0:
			map.value = "idle"
			break
		case 1:
			map.value = "running"
			break
		case 2:
			map.value = "running high"
			break
	}
	map
}

def zwaveEvent(physicalgraph.zwave.commands.thermostatmodev2.ThermostatModeReport cmd) {
	def map = [:]
	switch (cmd.mode) {
		case physicalgraph.zwave.commands.thermostatmodev2.ThermostatModeReport.MODE_OFF:
			map.value = "off"
			break
		case physicalgraph.zwave.commands.thermostatmodev2.ThermostatModeReport.MODE_HEAT:
			map.value = "heat"
			break
		case physicalgraph.zwave.commands.thermostatmodev2.ThermostatModeReport.MODE_AUXILIARY_HEAT:
			map.value = "emergency heat"
			break
		case physicalgraph.zwave.commands.thermostatmodev2.ThermostatModeReport.MODE_COOL:
			map.value = "cool"
			break
		case physicalgraph.zwave.commands.thermostatmodev2.ThermostatModeReport.MODE_AUTO:
			map.value = "auto"
			break
	}
	map.name = "thermostatMode"
	map
}

def zwaveEvent(physicalgraph.zwave.commands.thermostatfanmodev3.ThermostatFanModeReport cmd) {
	def map = [:]
	switch (cmd.fanMode) {
		case physicalgraph.zwave.commands.thermostatfanmodev3.ThermostatFanModeReport.FAN_MODE_AUTO_LOW:
			map.value = "fanAuto"
			break
		case physicalgraph.zwave.commands.thermostatfanmodev3.ThermostatFanModeReport.FAN_MODE_LOW:
			map.value = "fanOn"
			break
		case physicalgraph.zwave.commands.thermostatfanmodev3.ThermostatFanModeReport.FAN_MODE_CIRCULATION:
			map.value = "fanCirculate"
			break
	}
	map.name = "thermostatFanMode"
	map.displayed = false
	map
}

def zwaveEvent(physicalgraph.zwave.commands.thermostatmodev2.ThermostatModeSupportedReport cmd) {
	def supportedModes = ""
	if(cmd.off) { supportedModes += "off " }
	if(cmd.heat) { supportedModes += "heat " }
	if(cmd.auxiliaryemergencyHeat) { supportedModes += "emergency heat " }
	if(cmd.cool) { supportedModes += "cool " }
	if(cmd.auto) { supportedModes += "auto " }

	state.supportedModes = supportedModes
}

def zwaveEvent(physicalgraph.zwave.commands.thermostatfanmodev3.ThermostatFanModeSupportedReport cmd) {
	def supportedFanModes = ""
	if(cmd.auto) { supportedFanModes += "fanAuto " }
	if(cmd.low) { supportedFanModes += "fanOn " }
	if(cmd.circulation) { supportedFanModes += "fanCirculate " }

	state.supportedFanModes = supportedFanModes
}

def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicReport cmd) {
	log.debug "Zwave event received: $cmd"
}

def zwaveEvent(physicalgraph.zwave.Command cmd) {
	log.warn "Unexpected zwave command $cmd"
}

// Command Implementations
def poll() {
	delayBetween([
		zwave.sensorMultilevelV3.sensorMultilevelGet().format(), // current temperature
		zwave.thermostatSetpointV1.thermostatSetpointGet(setpointType: 1).format(),
		zwave.thermostatSetpointV1.thermostatSetpointGet(setpointType: 2).format(),
		zwave.thermostatModeV2.thermostatModeGet().format(),
		zwave.thermostatFanModeV3.thermostatFanModeGet().format(),
		zwave.thermostatOperatingStateV1.thermostatOperatingStateGet().format(),
        getBattery(), // CUSTOMIZATION
        setClock(), // CUSTOMIZATION
        zwave.multiInstanceV1.multiInstanceCmdEncap(instance: 2).encapsulate(zwave.sensorMultilevelV3.sensorMultilevelGet()).format() // CT-100/101 Customization for Humidity
	], 2300)
}

def quickSetHeat(degrees) {
	setHeatingSetpoint(degrees, 1000)
}

def setHeatingSetpoint(degrees, delay = 30000) {
	setHeatingSetpoint(degrees.toDouble(), delay)
}

def setHeatingSetpoint(Double degrees, Integer delay = 30000) {
	log.trace "setHeatingSetpoint($degrees, $delay)"
	def deviceScale = state.scale ?: 1
	def deviceScaleString = deviceScale == 2 ? "C" : "F"
    def locationScale = getTemperatureScale()
	def p = (state.precision == null) ? 1 : state.precision

    def convertedDegrees
    if (locationScale == "C" && deviceScaleString == "F") {
    	convertedDegrees = celsiusToFahrenheit(degrees)
    } else if (locationScale == "F" && deviceScaleString == "C") {
    	convertedDegrees = fahrenheitToCelsius(degrees)
    } else {
    	convertedDegrees = degrees
    }

	delayBetween([
		zwave.thermostatSetpointV1.thermostatSetpointSet(setpointType: 1, scale: deviceScale, precision: p, scaledValue: convertedDegrees).format(),
		zwave.thermostatSetpointV1.thermostatSetpointGet(setpointType: 1).format()
	], delay)
}

def quickSetCool(degrees) {
	setCoolingSetpoint(degrees, 1000)
}

def setCoolingSetpoint(degrees, delay = 30000) {
	setCoolingSetpoint(degrees.toDouble(), delay)
}

def setCoolingSetpoint(Double degrees, Integer delay = 30000) {
    log.trace "setCoolingSetpoint($degrees, $delay)"
	def deviceScale = state.scale ?: 1
	def deviceScaleString = deviceScale == 2 ? "C" : "F"
    def locationScale = getTemperatureScale()
	def p = (state.precision == null) ? 1 : state.precision

    def convertedDegrees
    if (locationScale == "C" && deviceScaleString == "F") {
    	convertedDegrees = celsiusToFahrenheit(degrees)
    } else if (locationScale == "F" && deviceScaleString == "C") {
    	convertedDegrees = fahrenheitToCelsius(degrees)
    } else {
    	convertedDegrees = degrees
    }

	delayBetween([
		zwave.thermostatSetpointV1.thermostatSetpointSet(setpointType: 2, scale: deviceScale, precision: p,  scaledValue: convertedDegrees).format(),
		zwave.thermostatSetpointV1.thermostatSetpointGet(setpointType: 2).format()
	], delay)
}

def configure() {
	delayBetween([
		zwave.thermostatModeV2.thermostatModeSupportedGet().format(),
		zwave.thermostatFanModeV3.thermostatFanModeSupportedGet().format(),
		zwave.associationV1.associationSet(groupingIdentifier:1, nodeId:[zwaveHubNodeId]).format()
	], 2300)
}

def modes() {
	["off", "heat", "cool", "auto", "emergency heat"]
}

def switchMode() {
	def currentMode = device.currentState("thermostatMode")?.value
	def lastTriedMode = state.lastTriedMode ?: currentMode ?: "off"
	def supportedModes = getDataByName("supportedModes")
	def modeOrder = modes()
	def next = { modeOrder[modeOrder.indexOf(it) + 1] ?: modeOrder[0] }
	def nextMode = next(lastTriedMode)
	if (supportedModes?.contains(currentMode)) {
		while (!supportedModes.contains(nextMode) && nextMode != "off") {
			nextMode = next(nextMode)
		}
	}
	state.lastTriedMode = nextMode
	delayBetween([
		zwave.thermostatModeV2.thermostatModeSet(mode: modeMap[nextMode]).format(),
		zwave.thermostatModeV2.thermostatModeGet().format()
	], 1000)
}

def switchToMode(nextMode) {
	def supportedModes = getDataByName("supportedModes")
	if(supportedModes && !supportedModes.contains(nextMode)) log.warn "thermostat mode '$nextMode' is not supported"
	if (nextMode in modes()) {
		state.lastTriedMode = nextMode
		"$nextMode"()
	} else {
		log.debug("no mode method '$nextMode'")
	}
}

def switchFanMode() {
	def currentMode = device.currentState("thermostatFanMode")?.value
	def lastTriedMode = state.lastTriedFanMode ?: currentMode ?: "off"
	def supportedModes = getDataByName("supportedFanModes") ?: "fanAuto fanOn"
	def modeOrder = ["fanAuto", "fanCirculate", "fanOn"]
	def next = { modeOrder[modeOrder.indexOf(it) + 1] ?: modeOrder[0] }
	def nextMode = next(lastTriedMode)
	while (!supportedModes?.contains(nextMode) && nextMode != "fanAuto") {
		nextMode = next(nextMode)
	}
	switchToFanMode(nextMode)
}

def switchToFanMode(nextMode) {
	def supportedFanModes = getDataByName("supportedFanModes")
	if(supportedFanModes && !supportedFanModes.contains(nextMode)) log.warn "thermostat mode '$nextMode' is not supported"

	def returnCommand
	if (nextMode == "fanAuto") {
		returnCommand = fanAuto()
	} else if (nextMode == "fanOn") {
		returnCommand = fanOn()
	} else if (nextMode == "fanCirculate") {
		returnCommand = fanCirculate()
	} else {
		log.debug("no fan mode '$nextMode'")
	}
	if(returnCommand) state.lastTriedFanMode = nextMode
	returnCommand
}

def getDataByName(String name) {
	state[name] ?: device.getDataValue(name)
}

def getModeMap() { [
	"off": 0,
	"heat": 1,
	"cool": 2,
	"auto": 3,
	"emergency heat": 4
]}

def setThermostatMode(String value) {
	delayBetween([
		zwave.thermostatModeV2.thermostatModeSet(mode: modeMap[value]).format(),
		zwave.thermostatModeV2.thermostatModeGet().format()
	], standardDelay)
}

def getFanModeMap() { [
	"auto": 0,
	"on": 1,
	"circulate": 6
]}

def setThermostatFanMode(String value) {
	delayBetween([
		zwave.thermostatFanModeV3.thermostatFanModeSet(fanMode: fanModeMap[value]).format(),
		zwave.thermostatFanModeV3.thermostatFanModeGet().format()
	], standardDelay)
}

def off() {
	delayBetween([
		zwave.thermostatModeV2.thermostatModeSet(mode: 0).format(),
		zwave.thermostatModeV2.thermostatModeGet().format()
	], standardDelay)
}

def heat() {
	delayBetween([
		zwave.thermostatModeV2.thermostatModeSet(mode: 1).format(),
		zwave.thermostatModeV2.thermostatModeGet().format()
	], standardDelay)
}

def emergencyHeat() {
	delayBetween([
		zwave.thermostatModeV2.thermostatModeSet(mode: 4).format(),
		zwave.thermostatModeV2.thermostatModeGet().format()
	], standardDelay)
}

def cool() {
	delayBetween([
		zwave.thermostatModeV2.thermostatModeSet(mode: 2).format(),
		zwave.thermostatModeV2.thermostatModeGet().format()
	], standardDelay)
}

def auto() {
	delayBetween([
		zwave.thermostatModeV2.thermostatModeSet(mode: 3).format(),
		zwave.thermostatModeV2.thermostatModeGet().format()
	], standardDelay)
}

def fanOn() {
	delayBetween([
		zwave.thermostatFanModeV3.thermostatFanModeSet(fanMode: 1).format(),
		zwave.thermostatFanModeV3.thermostatFanModeGet().format()
	], standardDelay)
}

def fanAuto() {
	delayBetween([
		zwave.thermostatFanModeV3.thermostatFanModeSet(fanMode: 0).format(),
		zwave.thermostatFanModeV3.thermostatFanModeGet().format()
	], standardDelay)
}

def fanCirculate() {
	delayBetween([
		zwave.thermostatFanModeV3.thermostatFanModeSet(fanMode: 6).format(),
		zwave.thermostatFanModeV3.thermostatFanModeGet().format()
	], standardDelay)
}

private getStandardDelay() {
	1000
}



def zwaveEvent(physicalgraph.zwave.commands.sensormultilevelv2.SensorMultilevelReport cmd) {
    log.debug "SensorMultilevelReportV2 $cmd"
	
    def map = [:]
	if (cmd.sensorType == 1) {
		map.value = convertTemperatureIfNeeded(cmd.scaledSensorValue, cmd.scale == 1 ? "F" : "C", cmd.precision)
		map.unit = getTemperatureScale()
		map.name = "temperature"
	} else if (cmd.sensorType == 5) {
		map.value = cmd.scaledSensorValue
		map.unit = "%"
		map.name = "humidity"
            sendEvent(name: "HumidityLevel", value: "humidity is ${map.value}%")
	}
	map
}

def zwaveEvent(physicalgraph.zwave.commands.multiinstancev1.MultiInstanceCmdEncap cmd) {
    def encapsulatedCommand = cmd.encapsulatedCommand([0x31: 3])
    log.debug ("multiinstancev1.MultiInstanceCmdEncap: command from instance ${cmd.instance}: ${encapsulatedCommand}")
    if (encapsulatedCommand) {
        return zwaveEvent(encapsulatedCommand)
    }
}
def zwaveEvent(physicalgraph.zwave.commands.batteryv1.BatteryReport cmd) {
    def nowTime = new Date().time
    state.lastBatteryGet = nowTime
    def map = [ name: "battery", unit: "%" ]
    if (cmd.batteryLevel == 0xFF || cmd.batteryLevel == 0) {
        map.value = 1
        map.descriptionText = "battery is low!"
        map.isStateChange = true
        sendEvent(name: "BatteryLevel", value: "battery is low!")
    } else {
        map.value = cmd.batteryLevel
        map.isStateChange = true
        sendEvent(name: "BatteryLevel", value: "battery is $map.value%")
    }
    map
}

private getBattery() {	//once every 24 hours
	def nowTime = new Date().time
	def ageInMinutes = state.lastBatteryGet ? (nowTime - state.lastBatteryGet)/60000 : 1440
    log.debug "Battery report age: ${ageInMinutes} minutes"
    if (ageInMinutes >= 1440) {
        log.debug "Fetching fresh battery value"
		zwave.batteryV1.batteryGet().format()
    } else "delay 87"
}

private setClock() {	// once a day
	def nowTime = new Date().time
	def ageInMinutes = state.lastClockSet ? (nowTime - state.lastClockSet)/60000 : 1440
    log.debug "Clock set age: ${ageInMinutes} minutes"
    if (ageInMinutes >= 1440) {
		state.lastClockSet = nowTime
        def nowCal = Calendar.getInstance(location.timeZone) // get current location timezone
		log.debug "Setting clock to ${nowCal.getTime().format("EEE MMM dd yyyy HH:mm:ss z", location.timeZone)}"
        sendEvent(name: "SetClock", value: "setting clock to ${nowCal.getTime().format("EEE MMM dd yyyy HH:mm:ss z", location.timeZone)}")
		zwave.clockV1.clockSet(hour: nowCal.get(Calendar.HOUR_OF_DAY), minute: nowCal.get(Calendar.MINUTE), weekday: nowCal.get(Calendar.DAY_OF_WEEK)).format()
    } else "delay 87"
}

def refresh() {
    // Force a refresh
    log.info "Requested a refresh"
    state.lastBatteryGet = (new Date().time) - (1440 * 60000)
    state.lastClockSet = (new Date().time) - (1440 * 60000)
    poll()
}


def coolLevelUp() {
    def locationScale = getTemperatureScale()
    def maxTemp
    def minTemp
    if (locationScale == "C") {
    	maxTemp = 37 // Max Temp in C
        minTemp = 1 // Min Temp in C
    	log.trace "Location is in Celsius, MaxTemp $maxTemp, MinTemp $minTemp"
    } else {
    	maxTemp = 99 // Max temp in F
    	minTemp = 35 // Max temp in F
    	log.trace "Location is in Farenheit, MaxTemp $maxTemp, MinTemp $minTemp"
    }

    int nextLevel = device.currentValue("coolingSetpoint") + 1
    
    if( nextLevel > maxTemp) {
    	nextLevel = maxTemp
    }
    log.debug "Setting cool set point up to: ${nextLevel}"
    quickSetCool(nextLevel)
}

def coolLevelDown() {
    def locationScale = getTemperatureScale()
    def maxTemp
    def minTemp
    if (locationScale == "C") {
    	maxTemp = 37 // Max Temp in C
        minTemp = 1 // Min Temp in C
    	log.trace "Location is in Celsius, MaxTemp $maxTemp, MinTemp $minTemp"
    } else {
    	maxTemp = 99 // Max temp in F
    	minTemp = 35 // Max temp in F
    	log.trace "Location is in Farenheit, MaxTemp $maxTemp, MinTemp $minTemp"
    }

	int nextLevel = device.currentValue("coolingSetpoint") - 1
    
    if( nextLevel < minTemp) {
    	nextLevel = minTemp
    }
    log.debug "Setting cool set point down to: ${nextLevel}"
    quickSetCool(nextLevel)
}

def heatLevelUp() {
    def locationScale = getTemperatureScale()
    def maxTemp
    def minTemp
    if (locationScale == "C") {
    	maxTemp = 37 // Max Temp in C
        minTemp = 1 // Min Temp in C
    	log.trace "Location is in Celsius, MaxTemp $maxTemp, MinTemp $minTemp"
    } else {
    	maxTemp = 99 // Max temp in F
    	minTemp = 35 // Max temp in F
    	log.trace "Location is in Farenheit, MaxTemp $maxTemp, MinTemp $minTemp"
    }

    int nextLevel = device.currentValue("heatingSetpoint") + 1
    
    if( nextLevel > maxTemp) {
    	nextLevel = maxTemp
    }
    log.debug "Setting heat set point up to: ${nextLevel}"
    quickSetHeat(nextLevel)
}

def heatLevelDown() {
    def locationScale = getTemperatureScale()
    def maxTemp
    def minTemp
    if (locationScale == "C") {
    	maxTemp = 37 // Max Temp in C
        minTemp = 1 // Min Temp in C
    	log.trace "Location is in Celsius, MaxTemp $maxTemp, MinTemp $minTemp"
    } else {
    	maxTemp = 99 // Max temp in F
    	minTemp = 35 // Max temp in F
    	log.trace "Location is in Farenheit, MaxTemp $maxTemp, MinTemp $minTemp"
    }

    int nextLevel = device.currentValue("heatingSetpoint") - 1
    
    if( nextLevel < minTemp) {
    	nextLevel = minTemp
    }
    log.debug "Setting heat set point down to: ${nextLevel}"
    quickSetHeat(nextLevel)
}