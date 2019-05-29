metadata {
	// Automatically generated. Make future change here.
	definition (name: "CT101 Thermostat", namespace: "Brad Kowal", author: "Brad Kowal") {
		capability "Actuator"
		capability "Temperature Measurement"
		capability "Relative Humidity Measurement"
		capability "Thermostat"
		capability "Battery"
		capability "Refresh"
		capability "Sensor"
		capability "Health Check"
		
		attribute "thermostatFanState", "string"

		command "switchMode"
		command "switchFanMode"
		command "lowerHeatingSetpoint"
		command "raiseHeatingSetpoint"
		command "lowerCoolSetpoint"
		command "raiseCoolSetpoint"
		command "poll"

		fingerprint deviceId: "0x08", inClusters: "0x43,0x40,0x44,0x31,0x80,0x85,0x60"
		fingerprint mfr:"0098", prod:"6401", model:"0107", deviceJoinName: "2Gig CT100 Programmable Thermostat"
		fingerprint mfr:"0098", prod:"6501", model:"000C", deviceJoinName: "Radio Thermostat CT101"
        fingerprint mfr:"0098", prod:"6501", model:"00FD", deviceJoinName: "Radio Thermostat CT101"
	}

	tiles {
		multiAttributeTile(name:"temperature", type:"thermostat", width:6, height:4,canChangeIcon: true) {
			tileAttribute("device.temperature", key: "PRIMARY_CONTROL") {
				attributeState("temperature", label:'${currentValue}°', unit:"dF", icon: "st.Weather.weather7",
					backgroundColors:[
							// Fahrenheit
							[value: 40, color: "#153591"],
							[value: 44, color: "#1e9cbb"],
							[value: 59, color: "#90d2a7"],
							[value: 74, color: "#44b621"],
							[value: 84, color: "#f1d801"],
							[value: 95, color: "#d04e00"],
							[value: 96, color: "#bc2323"]
					]
				)
			}
			tileAttribute("device.batteryIcon", key: "SECONDARY_CONTROL") {
				attributeState "ok_battery", label:'${currentValue}%', icon:"st.arlo.sensor_battery_4"
				attributeState "low_battery", label:'Low Battery', icon:"st.arlo.sensor_battery_0"}
	        tileAttribute("device.coolingSetpoint", key: "VALUE_CONTROL") {
       			attributeState("VALUE_UP", action: "raiseCoolSetpoint")
       			attributeState("VALUE_DOWN", action: "lowerCoolSetpoint")}
    		tileAttribute("device.thermostatOperatingState", key: "OPERATING_STATE") {
        			attributeState("idle", backgroundColor:"#00A0DC")
        			attributeState("heating", backgroundColor:"#e86d13")
        			attributeState("cooling", backgroundColor:"#00A0DC")}
    		tileAttribute("device.thermostatMode", key: "THERMOSTAT_MODE") {
                    attributeState("off", label:'${name}')
                    attributeState("heat", label:'${name}')
                    attributeState("cool", label:'${name}')
                    attributeState("auto", label:'${name}')}
//    		tileAttribute("device.heatingSetpoint", key: "HEATING_SETPOINT") {
//        			attributeState("heatingSetpoint", label:'${currentValue}', unit:"dF", defaultState: true)}
    		tileAttribute("device.coolingSetpoint", key: "COOLING_SETPOINT") {
        			attributeState("coolingSetpoint", label:'${currentValue}', unit:"dF", defaultState: true)}
        	}
		standardTile("mode", "device.thermostatMode", width:2, height:2, inactiveLabel: false, canChangeIcon: true) {
            state "off", label:'${name}', action:"switchMode", nextState:"to_heat", icon: "st.Outdoor.outdoor19"
            state "heat", label:'${name}', action:"switchMode", nextState:"to_cool", icon: "st.Weather.weather14", backgroundColor: '#E14902'
            state "cool", label:'${name}', action:"switchMode", nextState:"...", icon: "st.Weather.weather7", backgroundColor: '#003CEC'
            state "auto", label:'${name}', action:"switchMode", nextState:"...", icon: "st.Home.home1"
    		state "emergencyHeat", label:'${name}', action:"switchMode", nextState:"...", icon: "st.Weather.weather2", backgroundColor: '#E11102'
            state "to_heat", label: "heat", action:"switchMode", nextState:"to_cool"
            state "to_cool", label: "cool", action:"switchMode", nextState:"..."
            state "...", label: "...", action:"off", nextState:"off"
		}
		standardTile("fanMode", "device.thermostatFanMode", width:2, height:2, inactiveLabel: false, decoration: "flat") {
            state "fanAuto", label:'${name}', action:"switchFanMode", icon: "st.Appliances.appliances11"
            state "fanOn", label:'${name}', action:"switchFanMode", icon: "st.Appliances.appliances11", backgroundColor: '#02E181'
            state "fanCirculate", label:'${name}', action:"switchFanMode", icon: "st.Appliances.appliances11", backgroundColor: '#02D2E1'
		}
		standardTile("humidity", "device.humidity", width:2, height:2, inactiveLabel: false, decoration: "flat") {
			state "humidity", label:'${currentValue}%', icon:"st.Weather.weather12"
		}
		standardTile("lowerHeatingSetpoint", "device.heatingSetpoint", width:2, height:1, inactiveLabel: false, decoration: "flat") {
			state "heatingSetpoint", action:"lowerHeatingSetpoint", icon:"st.thermostat.thermostat-left"
		}
		valueTile("heatingSetpoint", "device.heatingSetpoint", width:2, height:1, inactiveLabel: false, decoration: "flat") {
			state "heatingSetpoint", label:'${currentValue}° heat', backgroundColor:"#ffffff"
		}
		standardTile("raiseHeatingSetpoint", "device.heatingSetpoint", width:2, height:1, inactiveLabel: false, decoration: "flat") {
			state "heatingSetpoint", action:"raiseHeatingSetpoint", icon:"st.thermostat.thermostat-right"
		}
		standardTile("lowerCoolSetpoint", "device.coolingSetpoint", width:2, height:1, inactiveLabel: false, decoration: "flat") {
			state "coolingSetpoint", action:"lowerCoolSetpoint", icon:"st.thermostat.thermostat-left"
		}
		valueTile("coolingSetpoint", "device.coolingSetpoint", width:2, height:1, inactiveLabel: false, decoration: "flat") {
			state "coolingSetpoint", label:'${currentValue}° cool', backgroundColor:"#ffffff"
		}
		standardTile("raiseCoolSetpoint", "device.heatingSetpoint", width:2, height:1, inactiveLabel: false, decoration: "flat") {
			state "heatingSetpoint", action:"raiseCoolSetpoint", icon:"st.thermostat.thermostat-right"
		}
		standardTile("thermostatOperatingState", "device.thermostatOperatingState", width: 2, height:2, decoration: "flat") {
			state "thermostatOperatingState", label:'${currentValue}', backgroundColor:"#ffffff"
		}
		standardTile("refresh", "device.thermostatMode", width:2, height:2, inactiveLabel: false, decoration: "flat") {
			state "default", action:"refresh.refresh", icon:"st.secondary.refresh"
            state "default", action:"polling.poll", icon:"st.secondary.refresh"
		}
		main "temperature"
		details(["temperature", "lowerHeatingSetpoint", "heatingSetpoint", "raiseHeatingSetpoint", "lowerCoolSetpoint",
				"coolingSetpoint", "raiseCoolSetpoint", "mode", "fanMode", "humidity", "thermostatOperatingState", "refresh"])	
	}
}

def installed() {
	// Configure device
	def cmds = [new physicalgraph.device.HubAction(zwave.associationV1.associationSet(groupingIdentifier:1, nodeId:[zwaveHubNodeId]).format()),
			new physicalgraph.device.HubAction(zwave.manufacturerSpecificV2.manufacturerSpecificGet().format())]
	sendHubCommand(cmds)
	runIn(3, "initialize", [overwrite: true])  // Allow configure command to be sent and acknowledged before proceeding
}

def updated() {
	// If not set update ManufacturerSpecific data
	if (!getDataValue("manufacturer")) {
		sendHubCommand(new physicalgraph.device.HubAction(zwave.manufacturerSpecificV2.manufacturerSpecificGet().format()))
		runIn(2, "initialize", [overwrite: true])  // Allow configure command to be sent and acknowledged before proceeding
	} else {
		initialize()
	}
}

def initialize() {
	unschedule()
	// Device-Watch simply pings if no device events received for 32min(checkInterval)
	sendEvent(name: "checkInterval", value: 2 * 15 * 60 + 2 * 60, displayed: false, data: [protocol: "zwave", hubHardwareId: device.hub.hardwareID])
	// Poll device for additional data that will be updated by refresh tile
	unschedule()
    pollDevice()
}

def configure() {
	def cmds = []
	/*
	Configuration of reporting values. Bitmask based on:
	1	TEMPERATURE (CC_SENSOR_MULTILEVEL)
	2	SETPOINT H
	4	SETPOINT C
	8	MODE
	16	FANMODE
	32	FANSTATE
	64	OPERATING STATE
	128	SCHEDENABLE
	256	SETBACK
	512	RUNHOLD
	1024	DISPLAYLOCK
	8192	BATTERY
	16384	MECH STATUS
	32768	SCP STATUS
	*/
	cmds << zwave.configurationV1.configurationSet(parameterNumber: 23, size: 2, scaledConfigurationValue: 8319).format()}

def parse(String description){
	def result = null
	if (description == "updated") {
	} else {
		def zwcmd = zwave.parse(description, [0x42:2, 0x43:2, 0x31: 2, 0x60: 3])
		if (zwcmd) {
			result = zwaveEvent(zwcmd)
			// Check battery level at least once every 2 days
			if (!state.lastbatt || now() - state.lastbatt > 48*60*60*1000) {
				sendHubCommand(new physicalgraph.device.HubAction(zwave.batteryV1.batteryGet().format()))
			}
		} else {
			log.debug "$device.displayName couldn't parse $description"
		}
	}
	if (!result) {
		return []
	}
	return [result]
}

def zwaveEvent(physicalgraph.zwave.commands.multichannelv3.MultiInstanceCmdEncap cmd) {
	def encapsulatedCommand = cmd.encapsulatedCommand([0x31: 3])
	if (encapsulatedCommand) {
		zwaveEvent(encapsulatedCommand)
	}
}

// Event Generation
def zwaveEvent(physicalgraph.zwave.commands.thermostatsetpointv2.ThermostatSetpointReport cmd) {
	def sendCmd = []
	def unit = getTemperatureScale()
	def cmdScale = cmd.scale == 1 ? "F" : "C"
	def setpoint = getTempInLocalScale(cmd.scaledValue, cmdScale)
	def heatingSetpoint = (setpoint == "heatingSetpoint") ? value : getTempInLocalScale("heatingSetpoint")
	def coolingSetpoint = (setpoint == "coolingSetpoint") ? value : getTempInLocalScale("coolingSetpoint")
	def mode = device.currentValue("thermostatMode")

	// Save device scale, precision, scale as they are used when enforcing setpoint limits
	if (cmd.setpointType == 1 || cmd.setpointType == 2) {
		state.size = cmd.size
		state.scale = cmd.scale
		state.precision = cmd.precision
	}
	switch (cmd.setpointType) {
		case 1: // "heatingSetpoint"
			state.deviceHeatingSetpoint = cmd.scaledValue
			if (state.targetHeatingSetpoint) {
				state.targetHeatingSetpoint = null
				sendEvent(name: "heatingSetpoint", value: setpoint, unit: getTemperatureScale())
			} else if (mode != "cool") {
				// if mode is cool heatingSetpoint can't be changed on device, disregard update
				// else update heatingSetpoint and enforce limits on coolingSetpoint
				updateEnforceSetpointLimits("heatingSetpoint", setpoint)
			}
			break;
		case 2: // "coolingSetpoint"
			state.deviceCoolingSetpoint = cmd.scaledValue
			if (state.targetCoolingSetpoint) {
				state.targetCoolingSetpoint = null
				sendEvent(name: "coolingSetpoint", value: setpoint, unit: getTemperatureScale())
			} else if (mode != "heat" || mode != "emergency heat") {
				// if mode is heat or  emergency heat coolingSetpoint can't be changed on device, disregard update
				// else update coolingSetpoint and enforce limits on heatingSetpoint
				updateEnforceSetpointLimits("coolingSetpoint", setpoint)
			}
			break;
		default:
			log.debug "unknown setpointType $cmd.setpointType"
			return
	}

	// So we can respond with same format
	state.size = cmd.size
	state.scale = cmd.scale
	state.precision = cmd.precision
	// Make sure return value is not result from above expresion
	return 0

}

//def zwaveEvent(physicalgraph.zwave.commands.sensormultilevelv2.SensorMultilevelReport cmd) {
//	def map = [:]
//	if (cmd.sensorType == 1) {
//		map.name = "temperature"
//		map.unit = getTemperatureScale()
//		map.value = getTempInLocalScale(cmd.scaledSensorValue, (cmd.scale == 1 ? "F" : "C"))
//		updateThermostatSetpoint(null, null)
//	} else if (cmd.sensorType == 5) {
//		map.name = "humidity"
//		map.unit = "%"
//		map.value = cmd.scaledSensorValue
//	}
//	sendEvent(map)
// }

def zwaveEvent(physicalgraph.zwave.commands.sensormultilevelv3.SensorMultilevelReport cmd) {
	def map = [:]
	if (cmd.sensorType == 1) {
		map.name = "temperature"
		map.unit = getTemperatureScale()
		map.value = getTempInLocalScale(cmd.scaledSensorValue, (cmd.scale == 1 ? "F" : "C"))
		updateThermostatSetpoint(null, null)
	} else if (cmd.sensorType == 5) {
		map.value = cmd.scaledSensorValue
		map.unit = "%"
		map.name = "humidity"
	}
	sendEvent(map)
}

def zwaveEvent(physicalgraph.zwave.commands.thermostatoperatingstatev2.ThermostatOperatingStateReport cmd) {
	def map = [name: "thermostatOperatingState"]
	switch (cmd.operatingState) {
		case physicalgraph.zwave.commands.thermostatoperatingstatev2.ThermostatOperatingStateReport.OPERATING_STATE_IDLE:
			map.value = "idle"
			break
		case physicalgraph.zwave.commands.thermostatoperatingstatev2.ThermostatOperatingStateReport.OPERATING_STATE_HEATING:
			map.value = "heating"
			break
		case physicalgraph.zwave.commands.thermostatoperatingstatev2.ThermostatOperatingStateReport.OPERATING_STATE_COOLING:
			map.value = "cooling"
			break
		case physicalgraph.zwave.commands.thermostatoperatingstatev2.ThermostatOperatingStateReport.OPERATING_STATE_FAN_ONLY:
			map.value = "fan only"
			break
		case physicalgraph.zwave.commands.thermostatoperatingstatev2.ThermostatOperatingStateReport.OPERATING_STATE_PENDING_HEAT:
			map.value = "pending heat"
			break
		case physicalgraph.zwave.commands.thermostatoperatingstatev2.ThermostatOperatingStateReport.OPERATING_STATE_PENDING_COOL:
			map.value = "pending cool"
			break
		case physicalgraph.zwave.commands.thermostatoperatingstatev2.ThermostatOperatingStateReport.OPERATING_STATE_VENT_ECONOMIZER:
			map.value = "vent economizer"
			break
	}
	sendEvent(map)
    	// Makes sure we have the correct thermostat mode
	sendHubCommand(new physicalgraph.device.HubAction(zwave.thermostatModeV2.thermostatModeGet().format()))
	createEvent(map)
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
	sendEvent(map)
}

def zwaveEvent(physicalgraph.zwave.commands.thermostatmodev2.ThermostatModeReport cmd) {
	def map = [name: "thermostatMode", data:[supportedThermostatModes: state.supportedModes]]
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
	sendEvent(map)
	// Now that mode and temperature is known we can request setpoints in correct order
	// Also makes sure operating state is in sync as it isn't being reported when changed
	def cmds = []
	def heatingSetpoint = getTempInLocalScale("heatingSetpoint")
	def coolingSetpoint = getTempInLocalScale("coolingSetpoint")
	def currentTemperature = getTempInLocalScale("temperature")
	cmds << new physicalgraph.device.HubAction(zwave.thermostatOperatingStateV1.thermostatOperatingStateGet().format())
	if (map.value == "cool" || ((map.value == "auto" || map.value == "off") && (currentTemperature > (heatingSetpoint + coolingSetpoint)/2))) {
		// request cooling setpoint first
		cmds << new physicalgraph.device.HubAction(zwave.thermostatSetpointV1.thermostatSetpointGet(setpointType: 2).format()) // CoolingSetpoint
		cmds << new physicalgraph.device.HubAction(zwave.thermostatSetpointV1.thermostatSetpointGet(setpointType: 1).format()) // HeatingSetpoint
	} else {
		// request heating setpoint first
		cmds << new physicalgraph.device.HubAction(zwave.thermostatSetpointV1.thermostatSetpointGet(setpointType: 1).format()) // HeatingSetpoint
		cmds << new physicalgraph.device.HubAction(zwave.thermostatSetpointV1.thermostatSetpointGet(setpointType: 2).format()) // CoolingSetpoint
	}
	sendHubCommand(cmds)
}

def zwaveEvent(physicalgraph.zwave.commands.thermostatfanmodev3.ThermostatFanModeReport cmd) {
	def map = [name: "thermostatFanMode", data:[supportedThermostatFanModes: state.supportedFanModes]]
	switch (cmd.fanMode) {
		case physicalgraph.zwave.commands.thermostatfanmodev3.ThermostatFanModeReport.FAN_MODE_AUTO_LOW:
			map.value = "auto"
			break
		case physicalgraph.zwave.commands.thermostatfanmodev3.ThermostatFanModeReport.FAN_MODE_LOW:
			map.value = "on"
			break
		case physicalgraph.zwave.commands.thermostatfanmodev3.ThermostatFanModeReport.FAN_MODE_CIRCULATION:
			map.value = "circulate"
			break
	}
	sendEvent(map)
}

def zwaveEvent(physicalgraph.zwave.commands.thermostatmodev2.ThermostatModeSupportedReport cmd) {
	def supportedModes = []
	if(cmd.heat) { supportedModes << "heat" }
	if(cmd.cool) { supportedModes << "cool" }
	// Make sure off is before auto, this ensures the right setpoint is used based on current temperature when auto is set
	if(cmd.off) { supportedModes << "off" }
	if(cmd.auto) { supportedModes << "auto" }
	if(cmd.auxiliaryemergencyHeat) { supportedModes << "emergency heat" }

	state.supportedModes = supportedModes
	sendEvent(name: "supportedThermostatModes", value: supportedModes, displayed: false)
}

def zwaveEvent(physicalgraph.zwave.commands.thermostatfanmodev3.ThermostatFanModeSupportedReport cmd) {
	def supportedFanModes = []
	if(cmd.auto) { supportedFanModes << "auto" }
	if(cmd.low) { supportedFanModes << "on" }
	if(cmd.circulation) { supportedFanModes << "circulate" }

	state.supportedFanModes = supportedFanModes
    createEvent(name: "supportedThermostatFanModes", value: supportedFanModes, displayed: false)
	sendEvent(name: "supportedThermostatFanModes", value: supportedFanModes, displayed: false)
}

def zwaveEvent(physicalgraph.zwave.commands.batteryv1.BatteryReport cmd) {
    def nowTime = new Date().time
    state.lastBatteryGet = nowTime
    def map = [ name: "battery", unit: "%" ]
    map.displayed = true
    map.isStateChange = true
    if (cmd.batteryLevel == 0xFF || cmd.batteryLevel == 0) {
        map.value = 1
        map.descriptionText = "battery is low!"
    } else {
        map.value = cmd.batteryLevel
    }
    map
}

def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicReport cmd) {
	log.debug "Zwave BasicReport: $cmd"
}

def zwaveEvent(physicalgraph.zwave.Command cmd) {
	log.warn "Unexpected zwave command $cmd"
}
def refresh() {
	// Only allow refresh every 2 minutes to prevent flooding the Zwave network
	def timeNow = now()
	if (!state.refreshTriggeredAt || (2 * 60 * 1000 < (timeNow - state.refreshTriggeredAt))) {
		state.refreshTriggeredAt = timeNow
		// refresh will request battery, prevent multiple request by setting lastbatt now
		state.lastbatt = timeNow
		// use runIn with overwrite to prevent multiple DTH instances run before state.refreshTriggeredAt has been saved
		runIn(2, "pollDevice", [overwrite: true])
	}
}

def poll() {
	delayBetween([
		zwave.sensorMultilevelV3.sensorMultilevelGet().format(), // current temperature
		zwave.thermostatSetpointV1.thermostatSetpointGet(setpointType: 1).format(),
		zwave.thermostatSetpointV1.thermostatSetpointGet(setpointType: 2).format(),
		zwave.thermostatModeV2.thermostatModeGet().format(),
		zwave.thermostatFanModeV3.thermostatFanModeGet().format(),
		zwave.thermostatOperatingStateV1.thermostatOperatingStateGet().format()
	], 2300)
	refresh()
}



def pollDevice() {
	def cmds = []
	cmds << new physicalgraph.device.HubAction(zwave.thermostatModeV2.thermostatModeSupportedGet().format())
	cmds << new physicalgraph.device.HubAction(zwave.thermostatFanModeV3.thermostatFanModeSupportedGet().format())
	cmds << new physicalgraph.device.HubAction(zwave.thermostatModeV2.thermostatModeGet().format())
	cmds << new physicalgraph.device.HubAction(zwave.thermostatFanModeV3.thermostatFanModeGet().format())
	cmds << new physicalgraph.device.HubAction(zwave.sensorMultilevelV2.sensorMultilevelGet().format()) // current temperature
	cmds << new physicalgraph.device.HubAction(zwave.thermostatOperatingStateV1.thermostatOperatingStateGet().format())
	cmds << new physicalgraph.device.HubAction(zwave.thermostatSetpointV1.thermostatSetpointGet(setpointType: 1).format())
	cmds << new physicalgraph.device.HubAction(zwave.thermostatSetpointV1.thermostatSetpointGet(setpointType: 2).format())
	cmds << new physicalgraph.device.HubAction(zwave.batteryV1.batteryGet().format())
	sendHubCommand(cmds, 1200)
}

def raiseHeatingSetpoint() {
	alterSetpoint(true, "heatingSetpoint")
}

def lowerHeatingSetpoint() {
	alterSetpoint(false, "heatingSetpoint")
}

def raiseCoolSetpoint() {
	alterSetpoint(true, "coolingSetpoint")
}

def lowerCoolSetpoint() {
	alterSetpoint(false, "coolingSetpoint")
}

// Adjusts nextHeatingSetpoint either .5° C/1° F) if raise true/false
def alterSetpoint(raise, setpoint) {
	def locationScale = getTemperatureScale()
	def deviceScale = (state.scale == 1) ? "F" : "C"
	def heatingSetpoint = getTempInLocalScale("heatingSetpoint")
	def coolingSetpoint = getTempInLocalScale("coolingSetpoint")
	def targetValue = (setpoint == "heatingSetpoint") ? heatingSetpoint : coolingSetpoint
	def delta = (locationScale == "F") ? 1 : 0.5
	targetValue += raise ? delta : - delta

	def data = enforceSetpointLimits(setpoint, [targetValue: targetValue, heatingSetpoint: heatingSetpoint, coolingSetpoint: coolingSetpoint], raise)
	// update UI without waiting for the device to respond, this to give user a smoother UI experience
	// also, as runIn's have to overwrite and user can change heating/cooling setpoint separately separate runIn's have to be used
	if (data.targetHeatingSetpoint) {
		sendEvent("name": "heatingSetpoint", "value": getTempInLocalScale(data.targetHeatingSetpoint, deviceScale),
				unit: getTemperatureScale(), eventType: "ENTITY_UPDATE", displayed: false)
	}
	if (data.targetCoolingSetpoint) {
		sendEvent("name": "coolingSetpoint", "value": getTempInLocalScale(data.targetCoolingSetpoint, deviceScale),
				unit: getTemperatureScale(), eventType: "ENTITY_UPDATE", displayed: false)
	}
	if (data.targetHeatingSetpoint && data.targetCoolingSetpoint) {
		runIn(5, "updateHeatingSetpoint", [data: data, overwrite: true])
	} else if (setpoint == "heatingSetpoint" && data.targetHeatingSetpoint) {
		runIn(5, "updateHeatingSetpoint", [data: data, overwrite: true])
	} else if (setpoint == "coolingSetpoint" && data.targetCoolingSetpoint) {
		runIn(5, "updateCoolingSetpoint", [data: data, overwrite: true])
	}
}

def updateHeatingSetpoint(data) {
	updateSetpoints(data)
}

def updateCoolingSetpoint(data) {
	updateSetpoints(data)
}

def updateEnforceSetpointLimits(setpoint, setpointValue) {
	def heatingSetpoint = (setpoint == "heatingSetpoint") ? setpointValue : getTempInLocalScale("heatingSetpoint")
	def coolingSetpoint = (setpoint == "coolingSetpoint") ? setpointValue : getTempInLocalScale("coolingSetpoint")

	sendEvent(name: setpoint, value: setpointValue, unit: getTemperatureScale(), displayed: false)
	updateThermostatSetpoint(setpoint, setpointValue)
	// Enforce coolingSetpoint limits, as device doesn't
	def data = enforceSetpointLimits(setpoint, [targetValue: setpointValue,
		heatingSetpoint: heatingSetpoint, coolingSetpoint: coolingSetpoint])
	if (setpoint == "heatingSetpoint" && data.targetHeatingSetpoint) {
		data.targetHeatingSetpoint = null
	} else if (setpoint == "coolingSetpoint" && data.targetCoolingSetpoint) {
		data.targetCoolingSetpoint = null
	}
	if (data.targetHeatingSetpoint != null || data.targetCoolingSetpoint != null) {
		updateSetpoints(data)
	}
}

def enforceSetpointLimits(setpoint, data, raise = null) {
	def locationScale = getTemperatureScale()
	def deviceScale = (state.scale == 1) ? "F" : "C"
	// min/max with 3°F/2°C deadband consideration
	def minSetpoint = (setpoint == "heatingSetpoint") ?  getTempInDeviceScale(35, "F") :  getTempInDeviceScale(38, "F")
	def maxSetpoint = (setpoint == "heatingSetpoint") ?  getTempInDeviceScale(92, "F") :  getTempInDeviceScale(95, "F")
	def deadband = (deviceScale == "F") ? 3 : 2
	def delta = (locationScale == "F") ? 1 : 0.5
	def targetValue = getTempInDeviceScale(data.targetValue, locationScale)
	def heatingSetpoint = null
	def coolingSetpoint = null

	// Enforce min/mix for setpoints
	if (targetValue > maxSetpoint) {
		heatingSetpoint = (setpoint == "heatingSetpoint") ? maxSetpoint : getTempInDeviceScale(data.heatingSetpoint, locationScale)
		coolingSetpoint = (setpoint == "heatingSetpoint") ? maxSetpoint + deadband : maxSetpoint
	} else if (targetValue < minSetpoint) {
		heatingSetpoint = (setpoint == "coolingSetpoint") ? minSetpoint - deadband : minSetpoint
		coolingSetpoint = (setpoint == "coolingSetpoint") ? minSetpoint : getTempInDeviceScale(data.coolingSetpoint, locationScale)
	}
	// Enforce deadband between setpoints
	if (setpoint == "heatingSetpoint" && !coolingSetpoint) {
		// Note if new value is same as old value we need to move it in the direction the user wants to change it, 1°F or 0.5°C,
		heatingSetpoint = (targetValue != getTempInDeviceScale(data.heatingSetpoint, locationScale) || !raise) ?
				targetValue : (raise ? targetValue + delta : targetValue - delta)
		coolingSetpoint = (heatingSetpoint + deadband > getTempInDeviceScale(data.coolingSetpoint, locationScale)) ? heatingSetpoint + deadband : null
	}
	if (setpoint == "coolingSetpoint" && !heatingSetpoint) {
		coolingSetpoint = (targetValue != getTempInDeviceScale(data.coolingSetpoint, locationScale) || !raise) ?
				targetValue : (raise ? targetValue + delta : targetValue - delta)
		heatingSetpoint = (coolingSetpoint - deadband < getTempInDeviceScale(data.heatingSetpoint, locationScale)) ? coolingSetpoint - deadband : null
	}
	return [targetHeatingSetpoint: heatingSetpoint, targetCoolingSetpoint: coolingSetpoint]
}

def setHeatingSetpoint(degrees) {
	if (degrees) {
		state.heatingSetpoint = degrees.toDouble()
		runIn(2, "updateSetpoints", [overwrite: true])
	}
}

def setCoolingSetpoint(degrees) {
	if (degrees) {
		state.coolingSetpoint = degrees.toDouble()
		runIn(2, "updateSetpoints", [overwrite: true])
	}
}

def updateSetpoints() {
	def deviceScale = (state.scale == 1) ? "F" : "C"
	def data = [targetHeatingSetpoint: null, targetCoolingSetpoint: null]
	def heatingSetpoint = getTempInLocalScale("heatingSetpoint")
	def coolingSetpoint = getTempInLocalScale("coolingSetpoint")
	if (state.heatingSetpoint) {
		data = enforceSetpointLimits("heatingSetpoint", [targetValue: state.heatingSetpoint,
				heatingSetpoint: heatingSetpoint, coolingSetpoint: coolingSetpoint])
	}
	if (state.coolingSetpoint) {
		heatingSetpoint = data.targetHeatingSetpoint ? getTempInLocalScale(data.targetHeatingSetpoint, deviceScale) : heatingSetpoint
		coolingSetpoint = data.targetCoolingSetpoint ? getTempInLocalScale(data.targetCoolingSetpoint, deviceScale) : coolingSetpoint
		data = enforceSetpointLimits("coolingSetpoint", [targetValue: state.coolingSetpoint,
				heatingSetpoint: heatingSetpoint, coolingSetpoint: coolingSetpoint])
		data.targetHeatingSetpoint = data.targetHeatingSetpoint ?: heatingSetpoint
	}
	state.heatingSetpoint = null
	state.coolingSetpoint = null
	updateSetpoints(data)
}

def updateSetpoints(data) {
	unschedule("updateSetpoints")
	def cmds = []
	if (data.targetHeatingSetpoint) {
		state.targetHeatingSetpoint = data.targetHeatingSetpoint
		cmds << new physicalgraph.device.HubAction(zwave.thermostatSetpointV1.thermostatSetpointSet(
					setpointType: 1, scale: state.scale, precision: state.precision, scaledValue: data.targetHeatingSetpoint).format())
	}
	if (data.targetCoolingSetpoint) {
		state.targetCoolingSetpoint = data.targetCoolingSetpoint
		cmds << new physicalgraph.device.HubAction(zwave.thermostatSetpointV1.thermostatSetpointSet(
					setpointType: 2, scale: state.scale, precision: state.precision, scaledValue: data.targetCoolingSetpoint).format())
	}
	// Also make sure temperature and operating state is in sync
	cmds << new physicalgraph.device.HubAction(zwave.multiChannelV3.multiInstanceCmdEncap(instance: 1).encapsulate(zwave.sensorMultilevelV3.sensorMultilevelGet()).format()) // temperature
	cmds << new physicalgraph.device.HubAction(zwave.thermostatOperatingStateV1.thermostatOperatingStateGet().format())
	if (data.targetHeatingSetpoint) {
		cmds << new physicalgraph.device.HubAction(zwave.thermostatSetpointV1.thermostatSetpointGet(setpointType: 1).format())
	}
	if (data.targetCoolingSetpoint) {
		cmds << new physicalgraph.device.HubAction(zwave.thermostatSetpointV1.thermostatSetpointGet(setpointType: 2).format())
	}
	sendHubCommand(cmds)
}

// thermostatSetpoint is not displayed by any tile as it can't be predictable calculated due to
// the device's quirkiness but it is defined by the capability so it must be set, set it to the most likely value
def updateThermostatSetpoint(setpoint, value) {
	def scale = getTemperatureScale()
	def heatingSetpoint = (setpoint == "heatingSetpoint") ? value : getTempInLocalScale("heatingSetpoint")
	def coolingSetpoint = (setpoint == "coolingSetpoint") ? value : getTempInLocalScale("coolingSetpoint")
	def mode = device.currentValue("thermostatMode")
	def thermostatSetpoint = heatingSetpoint    // corresponds to (mode == "heat" || mode == "emergency heat")
	if (mode == "cool") {
		thermostatSetpoint = coolingSetpoint
	} else if (mode == "auto" || mode == "off") {
		// Set thermostatSetpoint to the setpoint closest to the current temperature
		def currentTemperature = getTempInLocalScale("temperature")
		if (currentTemperature > (heatingSetpoint + coolingSetpoint)/2) {
			thermostatSetpoint = coolingSetpoint
		}
	}
	sendEvent(name: "thermostatSetpoint", value: thermostatSetpoint, unit: getTemperatureScale())
}

/**
 * PING is used by Device-Watch in attempt to reach the Device
 * */
def ping() {
	log.debug "ping() called"
	// Just get Operating State as it is not reported when it changes and there's no need to flood more commands
	sendHubCommand(new physicalgraph.device.HubAction(zwave.thermostatOperatingStateV1.thermostatOperatingStateGet().format()))
}

def switchMode() {
	def currentMode = device.currentValue("thermostatMode")
	def supportedModes = state.supportedModes
	// Old version of supportedModes was as string, make sure it gets updated
	if (supportedModes && supportedModes.size() && supportedModes[0].size() > 1) {
		def next = { supportedModes[supportedModes.indexOf(it) + 1] ?: supportedModes[0] }
		def nextMode = next(currentMode)
		runIn(2, "setGetThermostatMode", [data: [nextMode: nextMode], overwrite: true])
	} else {
		log.warn "supportedModes not defined"
		getSupportedModes()
	}
}

def switchToMode(nextMode) {
	def supportedModes = state.supportedModes
	// Old version of supportedModes was as string, make sure it gets updated
	if (supportedModes && supportedModes.size() && supportedModes[0].size() > 1) {
		if (supportedModes.contains(nextMode)) {
			runIn(2, "setGetThermostatMode", [data: [nextMode: nextMode], overwrite: true])
		} else {
			log.debug("ThermostatMode $nextMode is not supported by ${device.displayName}")
		}
	} else {
		log.warn "supportedModes not defined"
		getSupportedModes()
	}
}

def getSupportedModes() {
	def cmds = []
	cmds << new physicalgraph.device.HubAction(zwave.thermostatModeV2.thermostatModeSupportedGet().format())
	sendHubCommand(cmds)
}

def switchFanMode() {
	def currentMode = device.currentValue("thermostatFanMode")
	def supportedFanModes = state.supportedFanModes
	// Old version of supportedFanModes was as string, make sure it gets updated
	if (supportedFanModes && supportedFanModes.size() && supportedFanModes[0].size() > 1) {
		def next = { supportedFanModes[supportedFanModes.indexOf(it) + 1] ?: supportedFanModes[0] }
		def nextMode = next(currentMode)
		runIn(2, "setGetThermostatFanMode", [data: [nextMode: nextMode], overwrite: true])
	} else {
		log.warn "supportedFanModes not defined"
		getSupportedFanModes()
	}
}

def switchToFanMode(nextMode) {
	def supportedFanModes = state.supportedFanModes
	// Old version of supportedFanModes was as string, make sure it gets updated
	if (supportedFanModes && supportedFanModes.size() && supportedFanModes[0].size() > 1) {
		if (supportedFanModes.contains(nextMode)) {
			runIn(2, "setGetThermostatFanMode", [data: [nextMode: nextMode], overwrite: true])
		} else {
			log.debug("FanMode $nextMode is not supported by ${device.displayName}")
		}
	} else {
		log.warn "supportedFanModes not defined"
		getSupportedFanModes()
	}
}

def getSupportedFanModes() {
	def cmds = [new physicalgraph.device.HubAction(zwave.thermostatFanModeV3.thermostatFanModeSupportedGet().format())]
	sendHubCommand(cmds)
}

def getModeMap() { [
	"off": 0,
	"heat": 1,
	"cool": 2,
	"auto": 3,
	"emergency heat": 4
]}

def setThermostatMode(String value) {
	switchToMode(value)
}

def setGetThermostatMode(data) {
	def cmds = [new physicalgraph.device.HubAction(zwave.thermostatModeV2.thermostatModeSet(mode: modeMap[data.nextMode]).format()),
			new physicalgraph.device.HubAction(zwave.thermostatModeV2.thermostatModeGet().format())]
	sendHubCommand(cmds)
}

def getFanModeMap() { [
	"auto": 0,
	"on": 1,
	"circulate": 6
]}

def setThermostatFanMode(String value) {
	switchToFanMode(value)
}

def setGetThermostatFanMode(data) {
	def cmds = [new physicalgraph.device.HubAction(zwave.thermostatFanModeV3.thermostatFanModeSet(fanMode: fanModeMap[data.nextMode]).format()),
			new physicalgraph.device.HubAction(zwave.thermostatFanModeV3.thermostatFanModeGet().format())]
	sendHubCommand(cmds)
}

def off() {
	switchToMode("off")
}

def heat() {
	switchToMode("heat")
}

def emergencyHeat() {
	switchToMode("emergency heat")
}

def cool() {
	switchToMode("cool")
}

def auto() {
	switchToMode("auto")
}

def fanOn() {
	switchToFanMode("on")
}

def fanAuto() {
	switchToFanMode("auto")
}

def fanCirculate() {
	switchToFanMode("circulate")
}

private getTimeAndDay() {
	def timeNow = now()
	// Need to check that location have timeZone as SC may have created the location without setting it
	// Don't update clock more than once a day
	if (location.timeZone && (!state.timeClockSet || (24 * 60 * 60 * 1000 < (timeNow - state.timeClockSet)))) {
		def currentDate = Calendar.getInstance(location.timeZone)
		state.timeClockSet = timeNow
		return [hour: currentDate.get(Calendar.HOUR_OF_DAY), minute: currentDate.get(Calendar.MINUTE), weekday: currentDate.get(Calendar.DAY_OF_WEEK)]
	}
}

// Get stored temperature from currentState in current local scale
def getTempInLocalScale(state) {
	def temp = device.currentState(state)
	if (temp && temp.value && temp.unit) {
		return getTempInLocalScale(temp.value.toBigDecimal(), temp.unit)
	}
	return 0
}

// get/convert temperature to current local scale
def getTempInLocalScale(temp, scale) {
	if (temp && scale) {
		def scaledTemp = convertTemperatureIfNeeded(temp.toBigDecimal(), scale).toDouble()
		return (getTemperatureScale() == "F" ? scaledTemp.round(0).toInteger() : roundC(scaledTemp))
	}
	return 0
}

def getTempInDeviceScale(state) {
	def temp = device.currentState(state)
	if (temp && temp.value && temp.unit) {
		return getTempInDeviceScale(temp.value.toBigDecimal(), temp.unit)
	}
	return 0
}

def getTempInDeviceScale(temp, scale) {
	if (temp && scale) {
		def deviceScale = (state.scale == 1) ? "F" : "C"
		return (deviceScale == scale) ? temp :
				(deviceScale == "F" ? celsiusToFahrenheit(temp).toDouble().round(0).toInteger() : roundC(fahrenheitToCelsius(temp)))
	}
	return 0
}

def roundC (tempC) {
	return (Math.round(tempC.toDouble() * 2))/2
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
        sendEvent(name: "SetClock", value: "setting clock to ${nowCal.getTime().format("EEE MMM dd yyyy HH:mm:ss z", location.timeZone)}", displayed: true, isStateChange: true)
		zwave.clockV1.clockSet(hour: nowCal.get(Calendar.HOUR_OF_DAY), minute: nowCal.get(Calendar.MINUTE), weekday: nowCal.get(Calendar.DAY_OF_WEEK)).format()
    } else "delay 87"
}