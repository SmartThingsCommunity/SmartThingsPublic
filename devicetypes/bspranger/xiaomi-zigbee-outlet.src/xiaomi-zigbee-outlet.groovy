/**
 *  Xiaomi Smart Plug - model ZNCZ02LM
 *  Device Handler for SmartThings
 *  Version 1.2
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
 *  Based on original device handler by Lazcad / RaveTam
 *  Updates and contributions to code by a4refillpad, bspranger, marcos-mvs, mike-debney, Tiago_Goncalves, and veeceeoh
 *
 */

metadata {
	definition (name: "Xiaomi Zigbee Outlet", namespace: "bspranger", author: "bspranger") {
		capability "Actuator"
		capability "Configuration"
		capability "Refresh"
		capability "Switch"
		capability "Temperature Measurement"
		capability "Sensor"
		capability "Power Meter"
		capability "Energy Meter"

		attribute "lastCheckin", "string"
		attribute "lastCheckinDate", "String"
	}

	// simulator metadata
	simulator {
		// status messages
		status "on": "on/off: 1"
		status "off": "on/off: 0"
		// reply messages
		reply "zcl on-off on": "on/off: 1"
		reply "zcl on-off off": "on/off: 0"
	}

	tiles(scale: 2) {
		multiAttributeTile(name:"switch", type: "lighting", width: 6, height: 4, canChangeIcon: true){
			tileAttribute ("device.switch", key: "PRIMARY_CONTROL") {
				attributeState "on", label:'${name}', action:"switch.off", icon:"st.switches.light.on", backgroundColor:"#00a0dc", nextState:"turningOff"
				attributeState "off", label:'${name}', action:"switch.on", icon:"st.switches.light.off", backgroundColor:"#ffffff", nextState:"turningOn"
				attributeState "turningOn", label:'${name}', action:"switch.off", icon:"st.switches.light.on", backgroundColor:"#00a0dc", nextState:"turningOff"
				attributeState "turningOff", label:'${name}', action:"switch.on", icon:"st.switches.light.off", backgroundColor:"#ffffff", nextState:"turningOn"
			}
			tileAttribute("device.lastCheckin", key: "SECONDARY_CONTROL") {
				attributeState("default", label:'Last Update:\n ${currentValue}',icon: "st.Health & Wellness.health9")
			}
		}
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
		valueTile("power", "device.power", width: 2, height: 2) {
			state("power", label:'${currentValue}W', backgroundColors:[
				[value: 0, color: "#ffffff"],
				[value: 1, color: "#00a0dc"]
			])
		}
		valueTile("energy", "device.energy", width: 2, height: 2) {
			state("energy", label:'${currentValue}kWh')
		}
		standardTile("refresh", "device.refresh", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
			state "default", label:"", action:"refresh.refresh", icon:"st.secondary.refresh"
		}
		main (["switch", "power"])
			details(["switch", "power", "energy", "temperature", "refresh"])
	}

	preferences {
		//Temp Offset Config
		input description: "", type: "paragraph", element: "paragraph", title: "OFFSETS & UNITS"
		input "tempOffset", "decimal", title:"Temperature Offset", description:"Adjust temperature by this many degrees", range:"*..*"
		//Date & Time Config
		input description: "", type: "paragraph", element: "paragraph", title: "DATE & CLOCK"
		input name: "dateformat", type: "enum", title: "Set Date Format\n US (MDY) - UK (DMY) - Other (YMD)", description: "Date Format", options:["US","UK","Other"]
		input name: "clockformat", type: "bool", title: "Use 24 hour clock?"
	}
}

// Parse incoming device messages to generate events
def parse(String description) {
	log.debug "${device.displayName}: Parsing message: '${description}'"
	def value = zigbee.parse(description)?.text
	log.debug "${device.displayName}: Zigbee parse value: $value"
	Map map = [:]

	// Determine current time and date in the user-selected date format and clock style
	def now = formatDate()
	def nowDate = new Date(now).getTime()

	// The receipt of any message results in a lastCheckin (heartbeat) event
	sendEvent(name: "lastCheckin", value: now, displayed: false)
	sendEvent(name: "lastCheckinDate", value: nowDate, displayed: false)

	if (description?.startsWith('catchall:')) {
		map = parseCatchAllMessage(description)
	}
	else if (description?.startsWith('read attr -')) {
		map = parseReportAttributeMessage(description)
	}
	else if (description?.startsWith('on/off: ')){
		map = parseCustomMessage(description)
	}

	if (map) {
		log.debug "${device.displayName}: Creating event ${map}"
		return createEvent(map)
	} else
		return [:]
}

private Map parseCatchAllMessage(String description) {
	Map resultMap = [:]
	def zigbeeParse = zigbee.parse(description)
	log.debug "${device.displayName}: Catchall parsed as $cluster"

	if (zigbeeParse.clusterId == 0x0006 && zigbeeParse.command == 0x01){
		def onoff = zigbeeParse.data[-1]
		if (onoff == 1)
			resultMap = createEvent(name: "switch", value: "on")
		else if (onoff == 0)
			resultMap = createEvent(name: "switch", value: "off")
	}
	return resultMap
}

private Map parseReportAttributeMessage(String description) {
	Map descMap = (description - "read attr - ").split(",").inject([:]) {
		map, param ->
		def nameAndValue = param.split(":")
		map += [(nameAndValue[0].trim()):nameAndValue[1].trim()]
	}

	Map resultMap = [:]

	if (descMap.cluster == "0001" && descMap.attrId == "0020") {
		resultMap = getBatteryResult(convertHexToInt(descMap.value / 2))
	}
	if (descMap.cluster == "0002" && descMap.attrId == "0000") {
		def tempScale = getTemperatureScale()
		def tempValue = zigbee.parseHATemperatureValue("temperature: " + (convertHexToInt(descMap.value) / 2), "temperature: ", tempScale) + (tempOffset ? tempOffset : 0)
		resultMap = createEvent(name: "temperature", value: tempValue, unit: tempScale, translatable: true)
		log.debug "${device.displayName}: Reported temperature is ${resultMap.value}°$tempScale"
	}
	else if (descMap.cluster == "0008" && descMap.attrId == "0000") {
		resultMap = createEvent(name: "switch", value: "off")
	}
	else if (descMap.cluster == "000C" && descMap.attrId == "0055" && descMap.endpoint == "02") {
		def wattage_int = Long.parseLong(descMap.value, 16)
		def wattage = Float.intBitsToFloat(wattage_int.intValue())
		wattage = Math.round(wattage * 10) * 0.1
		resultMap = createEvent(name: "power", value: wattage, unit: 'W')
		log.debug "${device.displayName}: Reported power use is ${wattage}W"
	}
	else if (descMap.cluster == "000C" && descMap.attrId == "0055" && descMap.endpoint == "03") {
		def energy_int = Long.parseLong(descMap.value, 16)
		def energy = Float.intBitsToFloat(energy_int.intValue())
		energy = Math.round(energy * 100) * 0.0001
		resultMap = createEvent(name: "energy", value: energy, unit: 'kWh')
		log.debug "${device.displayName}: Reported energy usage is ${energy}kWh"
	}
	return resultMap
}

def off() {
	log.debug "${device.displayName}: Turning switch off"
	sendEvent(name: "switch", value: "off")
	"st cmd 0x${device.deviceNetworkId} 1 6 0 {}"
}

def on() {
	log.debug "${device.displayName}: Turning switch on"
	sendEvent(name: "switch", value: "on")
	"st cmd 0x${device.deviceNetworkId} 1 6 1 {}"
}

def refresh() {
	log.debug "${device.displayName}: Attempting to refresh all values"
	def refreshCmds = [
		"st rattr 0x${device.deviceNetworkId} 1 6 0", "delay 500",
		"st rattr 0x${device.deviceNetworkId} 1 6 0", "delay 250",
		"st rattr 0x${device.deviceNetworkId} 1 2 0", "delay 250",
		"st rattr 0x${device.deviceNetworkId} 1 1 0", "delay 250",
		"st rattr 0x${device.deviceNetworkId} 1 0 0", "delay 250",
		"st rattr 0x${device.deviceNetworkId} 2 0x000C 0x0055",
		"delay 250",
		"st rattr 0x${device.deviceNetworkId} 3 0x000C 0x0055"
	]
	return refreshCmds
}

private Map parseCustomMessage(String description) {
	def result
	if (description?.startsWith('on/off: ')) {
		if (description == 'on/off: 0')
			result = createEvent(name: "switch", value: "off")
		else if (description == 'on/off: 1')
			result = createEvent(name: "switch", value: "on")
	}
	return result
}

private Integer convertHexToInt(hex) {
	Integer.parseInt(hex,16)
}

def formatDate() {
	def correctedTimezone = ""
	def timeString = clockformat ? "HH:mm:ss" : "h:mm:ss aa"

	// If user's hub timezone is not set, display error messages in log and events log, and set timezone to GMT to avoid errors
	if (!(location.timeZone)) {
		correctedTimezone = TimeZone.getTimeZone("GMT")
		log.error "${device.displayName}: Time Zone not set, so GMT was used. Please set up your location in the SmartThings mobile app."
		sendEvent(name: "error", value: "", descriptionText: "ERROR: Time Zone not set, so GMT was used. Please set up your location in the SmartThings mobile app.")
	}
	else {
		correctedTimezone = location.timeZone
	}
	if (dateformat == "US" || dateformat == "" || dateformat == null) {
		return new Date().format("EEE MMM dd yyyy ${timeString}", correctedTimezone)
	}
	else if (dateformat == "UK") {
		return new Date().format("EEE dd MMM yyyy ${timeString}", correctedTimezone)
	}
	else {
		return new Date().format("EEE yyyy MMM dd ${timeString}", correctedTimezone)
	}
}