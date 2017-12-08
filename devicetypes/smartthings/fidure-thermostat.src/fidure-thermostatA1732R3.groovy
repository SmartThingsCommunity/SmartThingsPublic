/**
 *  Copyright 2017 Fidure Corp.
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
 *  Fidure Thermostat, Based on ZigBee thermostat (SmartThings)
 *
 *	Author: Fidure
 *	Date: 2017-12-08
 */

import physicalgraph.zigbee.zcl.DataType

metadata {
	definition (
		name: "Fidure 2nd Generation Thermostat",
		namespace: "smartthings/tile-ux",
		author: "SmartThings/Fidure") {

		capability "Thermostat"
		capability "Relative Humidity Measurement"
		
		capability "Temperature Measurement"
		capability "Refresh"
		capability "Sensor"
		capability "Lock" // for locking the keypad on the device.  
		
		command "tempUp"
		command "tempDown"
		command "heatUp"
		command "heatDown"
		command "coolUp"
		command "coolDown"
		command "poll"

		command "setThermostatTime"
		command "setCoolLimit", ["number"]
		command "setHeatLimit", ["number"]
		command "setLockLevel", ["number"]
		command "turnProgOn"
		command "turnProgOff"
		command "turnHoldOn"
		command "turnHoldOff"

		fingerprint profileId: "0104", inClusters: "0000,0003,0004,0005,0201,0204,0400,0402,0405,0B05", outClusters: "000A, 0019"
		
		attribute "runningMode", "string"
		attribute "setpointHold", "string"
		attribute "prorgammingOperation", "string"
		attribute "setpointHoldDuration", "number"
		attribute "lockLevel", "number"
		attribute "coolingDemand", "number"
		attribute "heatingDemand", "number"
		attribute "modelIdentifier", "string"
		attribute "lastSeen","string"

	}

	preferences {
		input ("lock_level", "enum", title: "Thermostat Keypad Lock Level", description: "Setting the Lock Level allows you to restrict the on-device access.", options: ["Mode Only", "Setpoint","Full"], defaultValue: "Full")
	}

	tiles(scale: 2) {
		multiAttributeTile(name:"thermostatFull", type:"thermostat", width:6, height:4) {
			tileAttribute("device.temperature", key: "PRIMARY_CONTROL") {
				attributeState("temp", label:'${currentValue}°', icon: "st.alarm.temperature.normal",
					backgroundColors:[
							// Celsius
							[value: 0, color: "#153591"],
							[value: 7, color: "#1e9cbb"],
							[value: 15, color: "#90d2a7"],
							[value: 23, color: "#44b621"],
							[value: 28, color: "#f1d801"],
							[value: 35, color: "#d04e00"],
							[value: 37, color: "#bc2323"],
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
			tileAttribute("device.thermostatSetpoint", key: "VALUE_CONTROL") {
				attributeState("VALUE_UP", action: "tempUp")
				attributeState("VALUE_DOWN", action: "tempDown")
			}
			tileAttribute("device.humidity", key: "SECONDARY_CONTROL") {
				attributeState("humidity", label:'${currentValue}%', defaultState: true)
			}
			tileAttribute("device.thermostatOperatingState", key: "OPERATING_STATE") {
				attributeState("idle", backgroundColor:"#00A0DC")
				attributeState("heating", backgroundColor:"#e86d13")
				attributeState("cooling", backgroundColor:"#00A0DC")
			}
			tileAttribute("device.thermostatMode", key: "THERMOSTAT_MODE") {
				attributeState("off", label:'${name}')
				attributeState("heat", label:'${name}')
				attributeState("cool", label:'${name}')
				attributeState("auto", label:'${name}')
			}
			tileAttribute("device.heatingSetpoint", key: "HEATING_SETPOINT") {
				attributeState("heatingSetpoint", label:'${currentValue}', unit:"dF", defaultState: true)
			}
			tileAttribute("device.coolingSetpoint", key: "COOLING_SETPOINT") {
				attributeState("coolingSetpoint", label:'${currentValue}', unit:"dF", defaultState: true)
			}
		}
        valueTile("temperature", "device.temperature", width: 2, height: 2) {
			state("temperature", label:'${currentValue}', unit:"dF",
				backgroundColors:[
							// Celsius
							[value: 0, color: "#153591"],
							[value: 7, color: "#1e9cbb"],
							[value: 15, color: "#90d2a7"],
							[value: 23, color: "#44b621"],
							[value: 28, color: "#f1d801"],
							[value: 35, color: "#d04e00"],
							[value: 37, color: "#bc2323"],
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
		standardTile("mode", "device.thermostatMode", width: 2, height: 2, inactiveLabel: false, decoration: "flat") {
			state "updating", label:'Updating...', action:"", backgroundColor:"#880000", nextState:"off" 
			state "off", action:"thermostat.auto", backgroundColor:"#ffffff", nextState:"updating" , icon: "st.thermostat.heating-cooling-off"
			state "auto",action:"thermostat.cool", backgroundColor:"#00A0DC", nextState:"updating", icon: "st.thermostat.auto"
			state "cool", action:"thermostat.heat", backgroundColor:"#00A0DC", nextState:"updating", icon: "st.thermostat.cool"
			state "heat", action:"thermostat.off", backgroundColor:"#e86d13", nextState:"updating" , icon: "st.thermostat.heat"
		}
		standardTile("fanMode", "thermostatFanMode", width: 2, height: 2, inactiveLabel: false, decoration: "flat") {
			state "updating", label:'Updating...', action:"", backgroundColor:"#ffffff", nextState:"fanAuto" 
			state "fanAuto", action:"thermostat.fanOn", backgroundColor:"#ffffff" , nextState:"updating" , icon: "st.thermostat.fan-auto"
			state "fanOn", action:"thermostat.fanCirculate", backgroundColor:"#ffffff" ,  nextState:"updating" , icon: "st.thermostat.fan-on"
			state "fanCirculate", action:"thermostat.fanAuto", backgroundColor:"#ffffff" , nextState:"updating", icon: "st.thermostat.fan-circulate"
		}
		standardTile("operatingState", "device.thermostatOperatingState", width: 2, height: 2) {
			state "idle", label:'${name}', backgroundColor:"#ffffff"
			state "heating", backgroundColor:"#e86d13", icon:"st.thermostat.heating"
			state "cooling", backgroundColor:"#00A0DC", icon:"st.thermostat.cooling"
		}
		standardTile("toggle", "device.lock", width: 2, height: 2) {
			state "unknown", label:"Unknown", action:"lock", icon:"st.locks.lock.unknown", backgroundColor:"#ffffff", nextState:"locking"
			state "full", label:'Locked', action:"unlock", icon:"st.locks.lock.locked", backgroundColor:"#79b821", nextState:"unlocking"
			state "level4", label:'Level 4', action:"unlock", icon:"st.locks.lock.locked", backgroundColor:"#79b821", nextState:"unlocking"
			state "level3", label:'Level 3', action:"unlock", icon:"st.locks.lock.locked", backgroundColor:"#79b821", nextState:"unlocking"
			state "setpoint", label:'Setpoint', action:"unlock", icon:"st.locks.lock.locked", backgroundColor:"#79b821", nextState:"unlocking"
			state "modeonly", label:'Mode Only', action:"unlock", icon:"st.locks.lock.locked", backgroundColor:"#79b821", nextState:"unlocking"
			state "unlocked", label:'Unlocked', action:"lock", icon:"st.locks.lock.unlocked", backgroundColor:"#ffffff", nextState:"locking"
			state "locking", label:'Locking', icon:"st.locks.lock.locked", backgroundColor:"#79b821"
			state "unlocking", label:'Unlocking', icon:"st.locks.lock.unlocked", backgroundColor:"#ffffff"
			
		}
		standardTile("refresh", "device.temperature", width: 2, height: 2, inactiveLabel: false, decoration: "flat") {
			state "default", action:"refresh.refresh", icon:"st.secondary.refresh"
		}
		standardTile("hold", "setpointHold", width: 2, height: 2, decoration: "flat") {
			state "changing", label:'Updating...'
			state "On", label:'Hold ${currentValue}', action:"turnHoldOff", nextState:"changing"
			state "Off", label:'Hold ${currentValue}', action:"turnHoldOn", nextState:"changing"
		}
		standardTile("program", "prorgammingOperation", width: 2, height: 2, decoration: "flat") {
			state "changing", label:'Updating...'
			state "On", label:'Sched ${currentValue}', action:"turnProgOff", nextState:"changing"
			state "Off", label:'Sched ${currentValue}', action:"turnProgOn", nextState:"changing"
		}
		valueTile("humidity", "device.humidity", width: 2, height: 2, decoration: "flat") {
			state "humidity", label:'${currentValue}%', icon:"st.Weather.weather12"
		}
		valueTile("seen", "lastSeen", width: 2, height: 2, decoration: "flat") {
			state "seen", label:'last seen ${currentValue}'
		}
		main("temperature")
		details([
			"thermostatFull", 
			"mode", "fanMode", "operatingState",
			"toggle","program", "hold",
			"humidity","refresh", "seen"
		])
	}
}

def installed() {
	log.debug "installed"
	// checkInterval of 12 minutes.
	sendEvent(name: "checkInterval", value: 60 * 12, displayed: false, data: [protocol: "zigbee", hubHardwareId: device.hub.hardwareID],eventType: "ENTITY_UPDATE")
	state.refreshExtraAttributes = (state.refreshExtraAttributes?:0) - (25 * 60 * 60 * 1000)
	sendEvent(name: "supportedThermostatModes", value: getSupportedModes(), eventType: "ENTITY_UPDATE", displayed: false)
	sendEvent(name: "supportedThermostatFanModes", value: ["fanAuto", "fanOn", "fanCirculate"], eventType: "ENTITY_UPDATE", displayed: false)
	runIn(2, "configure", [overwrite: true])  // Allow configure command to be sent and acknowledged before proceeding
}

def updated() {
	log.debug "updated"
	
	// Make sure we poll all attributes from the device
	state.refreshExtraAttributes = (state.refreshExtraAttributes?:0) - (25 * 60 * 60 * 1000)
	// this may be the second call to configure, the overwrite should catch extra calls
	runIn(2, "configure", [overwrite: true])
}

def refresh(){
	log.trace "refreshing..."

	def cmds = []
           cmds += zigbee.readAttribute(0x0201, 0x0010, ["mfgCode": 0x1121]) 
		   cmds += zigbee.readAttribute(0x0405, 0x0000)
		   cmds += zigbee.readAttribute(0x0000, 0x0005) 
		   cmds += zigbee.readAttribute(0x0201, 0x0000) 
		   cmds += zigbee.readAttribute(0x0201, 0x0011) 
		   cmds += zigbee.readAttribute(0x0201, 0x0012) 
		   cmds += zigbee.readAttribute(0x0201, 0x001C) 
		   cmds += zigbee.readAttribute(0x0201, 0x001E) 
		   cmds += zigbee.readAttribute(0x0204, 0x0001) 
		   cmds += zigbee.readAttribute(0x0201, 0x0023) 
		   cmds += zigbee.readAttribute(0x0201, 0x0024) 
		   cmds += zigbee.readAttribute(0x0201, 0x0025) 
		   cmds += zigbee.readAttribute(0x0201, 0x0029) 
		   cmds += setThermostatTime()

	def timeNow = new Date().time
	if (!state.refreshExtraAttributes || (24 * 60 * 60 * 1000 < (timeNow - state.refreshExtraAttributes))) {
		state.refreshExtraAttributes = timeNow

		// also pull down the setpoint limits.
		cmds += zigbee.readAttribute(0x0201, 0x0015)
		cmds += zigbee.readAttribute(0x0201, 0x0016)
		cmds += zigbee.readAttribute(0x0201, 0x0017)
		cmds += zigbee.readAttribute(0x0201, 0x0018)
	}
	 
	 sendZigbeeCmds(cmds, 100)
}


def configure() {

	log.debug "configure"
	// set default min max guards
	setDefaultMinMax()
  
	def cmds = []
	cmds += // Bind the device so reporting can take place
	"zdo bind 0x${device.deviceNetworkId} 1 1 0x201 {${device.zigbeeId}} {}" 
	cmds += // Bind the device so humidity reporting can take place
	"zdo bind 0x${device.deviceNetworkId} 1 1 0x405 {${device.zigbeeId}} {}" 
	 // THERMOSTAT_PROGRAMMING_OPERATION_MODE
	cmds +=  zigbee.configureReporting(0x0201, 0x0025, 0x18, 2, 3600, null) 
	 // THERMOSTAT_RUNNING_MODE
	 cmds += zigbee.configureReporting(0x0201, 0x001E, 0x30, 2, 3600, null) 
	 //LOCAL_TEMPERATURE (0.1C delta)
	 cmds += zigbee.configureReporting(0x0201, 0x0000, 0x29, 60, 300, 100) 
	 // SYSTEM_MODE
	 cmds += zigbee.configureReporting(0x0201, 0x001C, 0x30, 2, 3600, null) 
	 // OCCUPIED_COOLING_SETPOINT
	 cmds += zigbee.configureReporting(0x0201, 0x0011, 0x29, 2, 3600, null) 
	// OCCUPIED_HEATING_SETPOINT
	 cmds += zigbee.configureReporting(0x0201, 0x0012, 0x29, 2, 3600, null) 
	// SETPOINT_HOLD
	cmds += zigbee.configureReporting(0x0201, 0x0023, 0x30, 2, 3600, null) 
	// SETPOINT_HOLD_DURATION
	cmds += zigbee.configureReporting(0x0201, 0x0024, 0x21, 2, 3600, 0x01) 
	// THERMOSTAT_RUNNING_STATE
	cmds += zigbee.configureReporting(0x0201, 0x0029, 0x19, 2, 300, 0x01) 
	// FAN_MODE_ATTRIBUTE
	cmds += zigbee.configureReporting(0x0201, 0x0010, 0x30, 2, 300, null, [mfgCode: 0x1121]) 
	// KEYPAD_LOCKOUT
	cmds += zigbee.configureReporting(0x0204, 0x0001, 0x30, 2, 3600, 0x01) 
	// HUMIDITY
	cmds += zigbee.configureReporting(0x0405, 0x0000, 0x21, 60, 600, 0x01) 
	
	cmds += setThermostatTime()
	
	sendZigbeeCmds(cmds, 100)

	runIn(5, "refresh", [overwrite: true]) 
}

def setDefaultMinMax(){
	device.updateDataValue("minheatSetpoint", "830") 
	device.updateDataValue("maxheatSetpoint", "3000") 
	device.updateDataValue("mincoolSetpoint", "1500") 
	device.updateDataValue("maxcoolSetpoint", "3780") 
}

def	turnProgOff() {	zigbee.writeAttribute(0x0201,0x0025, 0x18, 0) }
def	turnProgOn() { zigbee.writeAttribute(0x0201, 0x0025, 0x18, 1)}
def	turnHoldOn() { zigbee.writeAttribute(0x0201, 0x0023 , 0x30 , 1)}
def	turnHoldOff() {zigbee.writeAttribute(0x0201, 0x0023 , 0x30 , 0)}


def parse(String description) {
	Map[] maps = []
	Map map = [:]
	def evts = []

	log.debug "got parse: $description"
	sendEvent("name":"lastSeen", "value":  (new Date()).format("MM-dd HH:mm",location.timeZone) )

	//POSSIBLE ST BUG: for some reason humidity reports show up already parsed. but the % sign causes an error
	if (description?.startsWith('humidity:')){ 
		map.name = "humidity"
		map.value = description.split(":")[1] - "%"
	} else 
		map = zigbee.getEvent(description)

	if (!map)
	{
		if (description?.startsWith('catchall:')) {
			maps += parseCatchAllMessage(description)
		} else if (description?.startsWith('read attr -')) {
		maps += parseReportAttributeMessage(description)
		}
	} else maps += map

	log.debug "Parse returned $maps"
	maps.each { parsedMap ->
		evts += parsedMap ? createEvent(parsedMap) : [:]
	}

	// check clock sync
	getTimeAndDay()

	return evts.size()? evts : null
}

private getTimeAndDay() {
	def timeNow = now()
	// Need to check that location have timeZone as SC may have created the location without setting it
	// update clock every 4 hours (for units that don't have an RTC)
	if (location.timeZone && (!state.timeClockSet || (4 * 60 * 60 * 1000 < (timeNow - state.timeClockSet)))) {
		state.timeClockSet = timeNow
		log.debug "updating time: $timeNow"
		updateTime();
	}
}

private Map[] parseMultipleAttributes(String cluster, String rawZigBee) {
	Map[] result = []
	Map descMap = [:]
	Integer size

	while(rawZigBee.length() >= 8)
	{
		descMap["cluster"] = cluster
		descMap["attrId"] = reverseHex(rawZigBee.substring(0,4))
		rawZigBee = rawZigBee.substring(4)
		descMap["encoding"] = rawZigBee.substring(0,2)
		rawZigBee = rawZigBee.substring(2)
		size = Integer.parseInt("${descMap.encoding}", 16)
		size = DataType.getLength(size) * 2 // hex
		descMap["value"] = reverseHex(rawZigBee.substring(0, size))
		rawZigBee = rawZigBee.substring(size)
		result += descMap
		descMap = [:]
	}
	log.debug "multi attribute decode: $result"
	return result
}

private Map processSingleAttributeMessge(Map descMap){
	
	Map resultMap = [:]
	if (descMap.cluster == "0201") {
		resultMap = parseThermostatClusterAttr(descMap)
	}
	else if (descMap.cluster == "0405" && descMap.attrId == "0000") {
		resultMap.name = "humidity"
        resultMap.unit = "%"
		resultMap.value = Integer.parseInt("${descMap.encoding}", 16) / 100
	} else if (descMap.cluster == "0402" && descMap.attrId == "0000") {
		resultMap.name = "temperature"
		resultMap.value = getTemperature(descMap.value)
		resultMap.unit = getTemperatureScale()
	} else if (descMap.cluster == "0204" && descMap.attrId == "0001") {
		resultMap.name = "lock"
		resultMap.value =  Integer.parseInt("${descMap.value}", 16)
		sendEvent("name":"lockLevel", "value": resultMap.value)
		resultMap.value = getLockMap()[resultMap.value ?: 0]
	} else if (descMap.cluster == "0202" && descMap.attrId == "0000") {
		resultMap.name = "thermostatFanMode"
		resultMap.value = getFanModeMap()[descMap.value]
		resultMap.data = [supportedThermostatFanModes: ["fanAuto","fanOn","fanCirculate"]]
	} else if (descMap.cluster == "0000" && descMap.attrId == "0500") {
		resultMap.name = "modelIdentifier"
		resultMap.value = descMap.value
	}
	return resultMap
}

private Map getCharStringAttr(Map descMap) {
	 Integer idx = (descMap?.result == "success")? 20 : 18 // read attr has success but report attr does not.  Both parse the same
	 String hex = descMap.raw.substring(idx)
	 Integer len = Integer.parseInt(hex.substring(0, 2), 16)
	 StringBuilder output = new StringBuilder()
	 for (int i = 2; i < Math.min(len*2+2, hex.length()); i+=2) {
		String str = hex.substring(i, i+2)
		char c = (char) Integer.parseInt(str, 16)
		output.append(c)
	 }
	 
	 descMap.attrLength = len
	 descMap.value = (String) output
	 return descMap

}

private Map[] parseReportAttributeMessage(String description) {
//  POSSIBLE ST BUG: when there are multiple attributes in the same message read attr mangles the value.  so we need to parse the RAW from scratch
//	e.g. description = "read attr - raw: 82C101000022050000420C413137333052545F48413132, dni: 82C1, endpoint: 01, cluster: 0000, size: 22, attrId: 0005, result: success, encoding: 42, value: 413137333052545F48413132"

	Map descMap = (description - "read attr - ").split(",").inject([:]) { map, param ->
		def nameAndValue = param.split(":")
		map += [(nameAndValue[0].trim()):nameAndValue[1].trim()]
	}
	Integer type = Integer.parseInt("${descMap.encoding}", 16)
	Integer length = DataType.getLength(type)

	// POSSIBLE ST BUG: sometimes a Char String attribute like model identifier is read through the ST platform but ST does not (according to docs) process Char Strings
	if (0x42 == Integer.parseInt("${descMap.encoding}", 16))
	{
		descMap = getCharStringAttr(descMap)
		length = descMap.attrLength
	}
	
	Map[] descMaps = []

	if (descMap.value.length() > (length * 2)){
		descMaps += parseMultipleAttributes(descMap.cluster, descMap.raw.substring(12))
	}else {
		descMaps += descMap
	}

	Map[] resultMaps = []
	descMaps.each { map ->
		resultMaps += processSingleAttributeMessge(map)
	}
	return resultMaps
}

private Map parseThermostatClusterAttr(Map descMap){
	Map map = [:]
	switch(descMap.attrId.toLowerCase())
		{
		case "0000":
			map.name = "temperature"
			map.value = getTemperature(descMap.value)
			map.unit = getTemperatureScale()
		break
		case "0005":
		if (descMap.encoding ==  "23"){
			map.name = "holdExpiary"
			map.value = "${convertToTime(descMap.value).getTime()}"
		}
		break
		case "0007":
		map.name = "coolingDemand"
		map.value = Integer.parseInt("${descMap.value}", 16)
		break
		case "0008":
		map.name = "heatingDemand"
		map.value = Integer.parseInt("${descMap.value}", 16)
		break
		case "0010":
		Integer encoding = Integer.parseInt("${descMap.encoding}",16)
			if (encoding == 0x30) {
			map.name ="thermostatFanMode"
			map.value = getFanModeMap()[descMap.value]
			map.data = [supportedThermostatFanModes: ["fanAuto","fanOn","fanCirculate"]]
			}
		break
		case "0011":
			if (descMap.encoding ==  "29") {
			map.name = "coolingSetpoint"
			map.value = getTemperature(descMap.value)
			map.unit = getTemperatureScale()
			updateThermostatSetpoint(map.value, "cool")
		} // as opposed to private attr 0x11 (descMap.encoding ==  "23") 
		break
		case "0012":
			map.name = "heatingSetpoint"
			map.value = getTemperature(descMap.value)
			map.unit = getTemperatureScale()
			updateThermostatSetpoint(map.value, "heat")
		break
		case "0015": 
			device.updateDataValue("minheatSetpoint",  "${Integer.parseInt(descMap.value, 16)}")
		break
		case "0016": 
			device.updateDataValue("maxheatSetpoint",  "${Integer.parseInt(descMap.value, 16)}")
		break
		case "0017": 
			device.updateDataValue("mincoolSetpoint",  "${Integer.parseInt(descMap.value, 16)}")	
		break
		case "0018": 
			device.updateDataValue("maxcoolSetpoint",  "${Integer.parseInt(descMap.value, 16)}")	
		break
		case "001c":
			map.name = "thermostatMode"
			map.value = getModeMap()[descMap.value]
			map.isStateChange = true  // set to true to force update if switchMode failed and old mode is returned
			map.data = [supportedThermostatModes: getSupportedModes()]
		break
		case "001e":   
			map.name = "runningMode"
			map.value = getModeMap()[descMap.value]
		break
		case "0023":   
			map.name = "setpointHold"
			map.value = getOnOffMap()[descMap.value]
			sendEvent("name":"setpointHold", "value":getOnOffMap()[descMap.value])
		break
		case "0024":   
			map.name = "setpointHoldDuration"
			map.value = Integer.parseInt("${descMap.value}", 16)
		break
		case "0025":   
			map.name = "prorgammingOperation"
			map.value = getOnOffMap()[descMap.value]
			sendEvent("name":"prorgammingOperation", "value":getOnOffMap()[descMap.value])
		break
		case "0029":
			map.name = "thermostatOperatingState"
			map.value = getThermostatOperatingState(descMap.value)
		break
	}
	return map
}

private Map parseCatchAllMessage(String description) {
	Map resultMap = [:]
	def cluster = zigbee.parse(description)
	return resultMap
}

def setheatingSetpointAfterDelay(data){
	sendZigbeeCmds(data.c, 100)
}

def setcoolingSetpointAfterDelay(data){
	sendZigbeeCmds(data.c, 100)
}

def setSetpoint(Integer attrId, Double value){
	Integer zigBeeTemp = getCelsiusValueX100(value)
	def cmds = zigbee.writeAttribute(0x0201, attrId , 0x29 , zigBeeTemp) 
}

def setHeatingSetpoint(Double degrees) {
	sendEvent(name: "heatingSetpoint", value: degrees, eventType: "ENTITY_UPDATE")
	setSetpoint(0x12, degrees)
}

def setCoolingSetpoint(Double degrees) {
	sendEvent(name: "coolingSetpoint", value: degrees, eventType: "ENTITY_UPDATE")
	setSetpoint(0x11, degrees)
}

def setCoolLimit(Double value){
	Integer zigBeeTemp = getCelsiusValueX100(value)
	// minmum cool limit
	[ zigbee.writeAttribute(0x0201, 0x0017 , 0x29 , zigBeeTemp) ]
}

def setHeatLimit(Double value){
	Integer zigBeeTemp = getCelsiusValueX100(value)
	// maximum heat limit
	[ zigbee.writeAttribute(0x0201, 0x0016 , 0x29 , zigBeeTemp) ]
}

def setThermostatMode(String next) {
	def val = (getModeMap().find { it.value == next }?.key)?: "00"
	def mode = device.currentState("thermostatMode")?.value
	
	[ zigbee.writeAttribute(0x0201, 0x1C , 0x30 , val) + 
		"delay 300" +
	  zigbee.readAttribute(0x0201, 0x1C) 
	]
}

def setThermostatFanMode(String value) {
	def val = (getFanModeMap().find { it.value == value }?.key)?: "00"
	[ zigbee.writeAttribute(0x0201, 0x10 , 0x30 , val, ["mfgCode": 0x1121]) + 
	  "delay 300" +
	  zigbee.readAttribute(0x0201, 0x10, ["mfgCode": 0x1121]) ]
}

def off() { setThermostatMode("off") }
def heat() { setThermostatMode("heat") }
def auto() { setThermostatMode("auto") }
def cool() { setThermostatMode("cool") }

def fanOn() { setThermostatFanMode("fanOn") }
def fanAuto() { setThermostatFanMode("fanAuto") }
def fanCirculate() { setThermostatFanMode("fanCirculate") }

def tempUp()   { adjustSetpoint(5, "")      }
def tempDown() { adjustSetpoint(-5, "")     }
def heatUp()   { adjustSetpoint(5, "heat")  }
def heatDown() { adjustSetpoint(-5, "heat") }
def coolUp()   { adjustSetpoint(5, "cool")  }
def coolDown() { adjustSetpoint(-5, "cool") }

private updateThermostatSetpoint(Number data, String forMode){
	String mode = device.currentState("thermostatMode")?.value
	if (mode == "auto")
		mode = device.currentState("runningMode")?.value
	
	if (mode == forMode)
		sendEvent(name: "thermostatSetpoint", value: data, eventType: "ENTITY_UPDATE")
}

private adjustSetpoint(Number value, String mode) {
		
	if ("cool" != mode && "heat" != mode){
		mode = device.currentState("thermostatMode")?.value
		if (mode == "auto")
			mode = device.currentState("runningMode")?.value
	}
	
	//POSSIBLE ST BUG: ST platform seems to transmit the raiseLowerTemperature command 3 times over the air causing the setpoint to be 3x higher
	//  Instead of using the zigbee command for raising or lower setpoint, we'll fiddle with the setpoint and manually adjust

	//default to both heat and cool
	Integer attrId
	Number currentSetPoint
	Integer zigBeeTemp
	String attrName 
	if ("heat" == mode ){
		attrId = 0x12
		currentSetPoint = device.currentValue("heatingSetpoint")
		attrName = "heatingSetpoint"
	}
	else if ("cool" == mode){
		attrId = 0x11
		currentSetPoint = device.currentValue("coolingSetpoint")
		attrName = "coolingSetpoint"
	} else return

	// move up and down in exact steps of C or F
	// clean up the current set point
	if (getTemperatureScale() == "C") {
		value = (value / 10)
		currentSetPoint = roundtoHalves(currentSetPoint)
	}else { // Fahrenheit
		currentSetPoint = Math.round(currentSetPoint)
		value = (value / 5)
	}
	
	currentSetPoint += value

	if (!checkBoundary(mode, currentSetPoint))
		return;
	
	device.updateDataValue(attrName, "$currentSetPoint")
	sendEvent(name: attrName, value: currentSetPoint, unit: getTemperatureScale(), eventType: "ENTITY_UPDATE")
	updateThermostatSetpoint(currentSetPoint, mode)
	
	def cmds = setSetpoint(attrId, currentSetPoint)

	runIn(3, "set"+ attrName + "AfterDelay" , [overwrite: true, data: [c: cmds]])
	return null
}

def checkBoundary(mode, newValue){
	newValue = getCelsiusValueX100(newValue)
	def min = Double.parseDouble(device.getDataValue("min" + mode + "Setpoint")?: "800") 
	def max = Double.parseDouble(device.getDataValue("max" + mode + "Setpoint")?: "3000")
	
	if (newValue <= max && newValue >= min)
		return true;
	return false;
}

def sendZigBeCommands(data) {
	//log.trace "Sending ZigBee Commands: $data?.c[0]"
	sendZigbeeCmds(data.c[0], 400)
}

def sendZigbeeCmds(cmds, delay = 2000) {
	// remove zigbee library added "delay 2000" after each command
	// the new sendHubCommand won't honor these, instead it'll take the delay as argument

	cmds?.removeAll { it.startsWith("delay") }
	// convert each command into a HubAction
	cmds = cmds.collect { new physicalgraph.device.HubAction(it) }
	//log.trace "hub command: $cmds delay $delay"
	sendHubCommand(cmds, delay)
}
// ST platform currently does not have a time service.  This command allows for a smartApp to Manually update the time.
def setThermostatTime() {
	Date date = new Date()

	long millis = date.getTime() // Millis since Unix epoch
	millis -= 946684800000  // adjust for ZigBee EPOCH
	// adjust for time zone and DST offset
	millis += location.timeZone.getOffset(date.getTime())
	//convert to seconds
	millis /= 1000
	
	// POSSIBLE BUG:
	// write attribute does not pack int32u properly.  so we need to revese the endianness
	millis = zigbee.convertHexToInt(DataType.pack(millis, DataType.UINT32, true))
		
	zigbee.writeAttribute(0x201, 0x000F, 0x23, millis, [mfgCode: 0x1121])
}

def updateTime() {
	sendZigbeeCmds(setThermostatTime(), 100)
}

def poll() {
	log.trace "Poll..."
	refresh()
}

def lock(){	setLockLevel(getLockLevelMap()[settings.lock_level] ?: 5) }
def unlock() { setLockLevel(0) }

def setLockLevel(Integer level) {
	if (level < 0 || level > 5 )
		return

	[ 
	zigbee.writeAttribute(0x0204, 0x0001 , 0x30 , level) ,
	"delay 300",
	zigbee.readAttribute(0x0204, 0x0001)
	] 
}

// ----------------
// helper functions 
// ----------------
def getTemperature(value) { 
	Double celsius = Integer.parseInt(value, 16) / 100
	if(getTemperatureScale() == "C"){
		return celsius?.round(1)
	} else {
		return ((Double)celsiusToFahrenheit(celsius))?.round(1)
	}
}

private Number roundtoHalves(Number value) { Integer num = 0.49 + value * 2; return num /2; }
private Integer getCelsiusValueX100(Number value) {
	Integer degreesC 
	degreesC = (getTemperatureScale() == "C") ? (value * 100) : (fahrenheitToCelsius(value) * 100)
	return degreesC	
}
def getModeMap() { ["00":"off","01":"auto","03":"cool","04":"heat"]}
def getSupportedModes() { ["off", "heat", "cool", "auto"]}
def getFanModeMap() { ["00":"fanAuto", "01":"fanOn", "04":"fanCirculate", "02":"fanCirculate", "03":"fanCirculate", "05":"fanCirculate"] }
def getOnOffMap() {[	"00":"Off","01":"On"]}
def getLockMap(){[0:"unlocked",1:"modeonly",2:"setpoint",3:"level3",4:"level4",5:"full"]}
def getLockLevelMap(){["Unlocked":0,"Mode Only":1,"Setpoint":2,"Level3":3,"Level4":4,"Full":5]}
def getThermostatOperatingState(value){
	
	// 5 mapped to fan
	String[] m = [ "heating", "cooling", "fan", "Heat2", "Cool2", "Fan2", "Fan3"]
	String desc = 'idle'
	value = Integer.parseInt(''+value, 16)
	
	// give prioirty over heating and cooling
	for ( i in 7..0 ) {
		if (value & (1 << i))
			desc = m[i]
	}
	return desc
}
def reverseHex(String hex){ return (hex.length() > 2)? (reverseHex(hex.substring(2)) + hex.substring(0,2)) : hex }
// End of DTH