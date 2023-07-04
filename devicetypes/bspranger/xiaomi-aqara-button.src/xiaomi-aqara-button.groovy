/**
 *  Aqara Button - models WXKG11LM (original & new revision) / WXKG12LM
 *  Device Handler for SmartThings - Firmware version 25.20 and newer ONLY
 *  Version 1.4.3
 *
 *  NOTE: Do NOT use this device handler on any SmartThings hub running Firmware 24.x and older
 *        Instead use the xiaomi-aqara-button-old-firmware device handler
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *	    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 *  Original device handler code by a4refillpad, adapted for use with Aqara model by bspranger, updated for changes in firmware 25.20 by veeceeoh
 *  Additional contributions to code by alecm, alixjg, bspranger, gn0st1c, foz333, jmagnuson, rinkek, ronvandegraaf, snalee, tmleafs, twonk, veeceeoh, & xtianpaiva
 *
 *  Notes on capabilities of the different models:
 *  Model WXKG11LM (original revision)
 *    - Single-click results in "button 1 pushed" event
 *    - Double-click results in "button 2 pushed" event
 *    - Triple-click results in "button 3 pushed" event
 *    - Quadruple-click results in button 4 "pushed" event
 *    - Any type of click results in custom "lastPressedCoRE" event for webCoRE use
 *  Model WXKG11LM (new revision):
 *    - Single-click results in "button 1 pushed" event
 *    - Hold for longer than 400ms results in "button 1 held" event
 *    - Double-click results in "button 2 pushed" event
 *    - Release after a hold results in "button 3 pushed" event
 *    - Single or double-click results in custom "lastPressedCoRE" event for webCoRE use
 *    - Hold results in custom "lastHeldCoRE" event for webCoRE use
 *    - Release results in custom "lastReleasedCoRE" event for webCoRE use
 *  Model WXKG12LM:
 *    - Single-click results in "button 1 pushed" event
 *    - Hold for longer than 400ms results in "button 1 held" event
 *    - Double-click results in "button 2 pushed" event
 *    - Shaking the button results in "button 3 pushed" event
 *    - Release after a hold results in "button 4 pushed" event
 *    - Single/double-click or shake results in custom "lastPressedCoRE" event for webCoRE use
 *    - Hold results in custom "lastHeldCoRE" event for webCoRE use
 *    - Release of button results in custom "lastReleasedCoRE" event for webCoRE use
 *
 *  Known issues:
 *  - As of March 2019, the SmartThings Samsung Connect mobile app does NOT support custom device handlers such as this one
 *  - The SmartThings Classic mobile app UI text/graphics is rendered differently on iOS vs Android devices - This is due to SmartThings, not this device handler
 *  - Pairing Xiaomi/Aqara devices can be difficult as they were not designed to use with a SmartThings hub.
 *  - The battery level is not reported at pairing. Wait for the first status report, 50-60 minutes after pairing.
 *  - Xiaomi devices do not respond to refresh requests
 *  - Most ZigBee repeater devices (generally mains-powered ZigBee devices) are NOT compatible with Xiaomi/Aqara devices, causing them to drop off the network.
 *    Only XBee ZigBee modules, the IKEA Tradfri Outlet / Tradfri Bulb, and ST user @iharyadi's custom multi-sensor ZigBee repeater device are confirmed to be compatible.
 *
 */

 import groovy.json.JsonOutput
 import physicalgraph.zigbee.zcl.DataType

metadata {
	definition (name: "Xiaomi Aqara Button", namespace: "bspranger", author: "bspranger", minHubCoreVersion: "000.022.0002", ocfDeviceType: "x.com.st.d.remotecontroller") {
		capability "Actuator"
		capability "Battery"
		capability "Button"
		capability "Configuration"
		capability "Health Check"
		capability "Holdable Button"
		capability "Momentary"
		capability "Sensor"

		attribute "lastCheckin", "string"
		attribute "lastCheckinCoRE", "string"
		attribute "lastHeld", "string"
		attribute "lastHeldCoRE", "string"
		attribute "lastPressed", "string"
		attribute "lastPressedCoRE", "string"
		attribute "lastReleased", "string"
		attribute "lastReleasedCoRE", "string"
		attribute "batteryRuntime", "string"
		attribute "buttonStatus", "enum", ["pushed", "held", "single-clicked", "double-clicked", "triple-clicked", "quadruple-clicked", "shaken", "released"]

		// Aqara Button - model WXKG11LM (original revision)
		fingerprint deviceId: "5F01", inClusters: "0000,FFFF,0006", outClusters: "0000,0004,FFFF", manufacturer: "LUMI", model: "lumi.sensor_switch.aq2", deviceJoinName: "Aqara Button WXKG11LM"
		// Aqara Button - model WXKG11LM (new revision)
		fingerprint deviceId: "5F01", inClusters: "0000,0012,0003", outClusters: "0000", manufacturer: "LUMI", model: "lumi.remote.b1acn01", deviceJoinName: "Aqara Button WXKG11LM r2"
		// Aqara Button - model WXKG12LM
		fingerprint deviceId: "5F01", inClusters: "0000,0001,0006,0012", outClusters: "0000", manufacturer: "LUMI", model: "lumi.sensor_switch.aq3", deviceJoinName: "Aqara Button WXKG12LM"
		fingerprint deviceId: "5F01", inClusters: "0000,0001,0006,0012", outClusters: "0000", manufacturer: "LUMI", model: "lumi.sensor_swit", deviceJoinName: "Aqara Button WXKG12LM"

		command "resetBatteryRuntime"
	}

	simulator {
		status "Press button": "on/off: 0"
		status "Release button": "on/off: 1"
	}

	tiles(scale: 2) {
		multiAttributeTile(name:"buttonStatus", type: "lighting", width: 6, height: 4, canChangeIcon: false) {
			tileAttribute ("device.buttonStatus", key: "PRIMARY_CONTROL") {
				attributeState("default", label:'Single-clicked', backgroundColor:"#00a0dc", icon:"https://raw.githubusercontent.com/bspranger/Xiaomi/master/images/ButtonPushed.png")
				attributeState("held", label:'Held', backgroundColor:"#00a0dc", icon:"https://raw.githubusercontent.com/bspranger/Xiaomi/master/images/ButtonPushed.png")
				attributeState("single-clicked", label:'Single-clicked', backgroundColor:"#00a0dc", icon:"https://raw.githubusercontent.com/bspranger/Xiaomi/master/images/ButtonPushed.png")
				attributeState("double-clicked", label:'Double-clicked', backgroundColor:"#00a0dc", icon:"https://raw.githubusercontent.com/bspranger/Xiaomi/master/images/ButtonPushed.png")
				attributeState("triple-clicked", label:'Triple-clicked', backgroundColor:"#00a0dc", icon:"https://raw.githubusercontent.com/bspranger/Xiaomi/master/images/ButtonPushed.png")
				attributeState("quadruple-clicked", label:'Quadruple-clicked', backgroundColor:"#00a0dc", icon:"https://raw.githubusercontent.com/bspranger/Xiaomi/master/images/ButtonPushed.png")
				attributeState("shaken", label:'Shaken', backgroundColor:"#00a0dc", icon:"https://raw.githubusercontent.com/bspranger/Xiaomi/master/images/ButtonPushed.png")
				attributeState("released", label:'Released', action: "momentary.push", backgroundColor:"#ffffff", icon:"https://raw.githubusercontent.com/bspranger/Xiaomi/master/images/ButtonReleased.png")
			}
			tileAttribute("device.lastPressed", key: "SECONDARY_CONTROL") {
				attributeState "lastPressed", label:'Last Pressed: ${currentValue}'
			}
		}
		valueTile("battery", "device.battery", decoration: "flat", inactiveLabel: false, width: 2, height: 2) {
			state "battery", label:'${currentValue}%', unit:"%", icon:"https://raw.githubusercontent.com/bspranger/Xiaomi/master/images/XiaomiBattery.png",
			backgroundColors:[
				[value: 10, color: "#bc2323"],
				[value: 26, color: "#f1d801"],
				[value: 51, color: "#44b621"]
			]
		}
		valueTile("lastCheckin", "device.lastCheckin", decoration: "flat", inactiveLabel: false, width: 4, height: 1) {
			state "lastCheckin", label:'Last Event:\n${currentValue}'
		}
		valueTile("batteryRuntime", "device.batteryRuntime", inactiveLabel: false, decoration: "flat", width: 4, height: 1) {
			state "batteryRuntime", label:'Battery Changed: ${currentValue}'
		}
		main (["buttonStatus"])
		details(["buttonStatus","battery","lastCheckin","batteryRuntime"])
	}

	preferences {
		//Date & Time Config
		input description: "", type: "paragraph", element: "paragraph", title: "DATE & CLOCK"
		input name: "dateformat", type: "enum", title: "Set Date Format\nUS (MDY) - UK (DMY) - Other (YMD)", description: "Date Format", options:["US","UK","Other"]
		input name: "clockformat", type: "bool", title: "Use 24 hour clock?"
		//Battery Reset Config
		input description: "If you have installed a new battery, the toggle below will reset the Changed Battery date to help remember when it was changed.", type: "paragraph", element: "paragraph", title: "CHANGED BATTERY DATE RESET"
		input name: "battReset", type: "bool", title: "Battery Changed?"
		//Advanced Settings
		input description: "Only change the settings below if you know what you're doing.", type: "paragraph", element: "paragraph", title: "ADVANCED SETTINGS"
		//Battery Voltage Range
		input description: "", type: "paragraph", element: "paragraph", title: "BATTERY VOLTAGE RANGE"
		input name: "voltsmax", type: "decimal", title: "Max Volts\nA battery is at 100% at __ volts\nRange 2.8 to 3.4", range: "2.8..3.4", defaultValue: 3
		input name: "voltsmin", type: "decimal", title: "Min Volts\nA battery is at 0% (needs replacing) at __ volts\nRange 2.0 to 2.7", range: "2..2.7", defaultValue: 2.5
		//Live Logging Message Display Config
		input description: "These settings affect the display of messages in the Live Logging tab of the SmartThings IDE.", type: "paragraph", element: "paragraph", title: "LIVE LOGGING"
		input name: "infoLogging", type: "bool", title: "Display info log messages?", defaultValue: true
		input name: "debugLogging", type: "bool", title: "Display debug log messages?"
	}
}

//adds functionality to press the center tile as a virtualApp Button
def push() {
	displayInfoLog(": Virtual App Button Pressed")
	sendEvent(mapButtonEvent(1))
}

// Parse incoming device messages to generate events
def parse(String description) {
	displayDebugLog(": Parsing '${description}'")
	def result = [:]

	// Any report - button press & Battery - results in a lastCheckin event and update to Last Checkin tile
	sendEvent(name: "lastCheckin", value: formatDate(), displayed: false)
	sendEvent(name: "lastCheckinCoRE", value: now(), displayed: false)

	// Send message data to appropriate parsing function based on the type of report
	if (description?.startsWith('on/off: ')) {
		// Model WXKG11LM (original revision) will produce this message on OLDER firmware prior to version 25.20
		// This device handler is NOT designed for use on firmware 24.x or earlier
		updateLastPressed("Pressed")
		result = mapButtonEvent(1)
		log.warn "It appears you may be using a SmartThings hub running firmware OLDER than 25.20"
		log.warn "This device handler is NOT compatible with firmware 24.x or earlier"
		log.warn "Please switch to the xiaomi-aqara-button-old-firmware device handler"
	} else if (description?.startsWith("read attr - raw: ")) {
		// Parse messages received on button press actions or on short-press of reset button
		result = parseReadAttrMessage(description)
	} else if (description?.startsWith('catchall:')) {
		// Parse catchall message to check for battery voltage report
		result = parseCatchAllMessage(description)
	}
	if (result != [:]) {
		displayDebugLog(": Creating event $result")
		return createEvent(result)
	} else
		return [:]
}

private Map parseReadAttrMessage(String description) {
	def cluster = description.split(",").find {it.split(":")[0].trim() == "cluster"}?.split(":")[1].trim()
	def attrId = description.split(",").find {it.split(":")[0].trim() == "attrId"}?.split(":")[1].trim()
	def valueHex = description.split(",").find {it.split(":")[0].trim() == "value"}?.split(":")[1].trim()
	Map resultMap = [:]

	if (cluster == "0006")
		// Process model WXKG11LM (original revision)
		resultMap = parse11LMMessage(attrId, Integer.parseInt(valueHex[0..1],16))
	else if (cluster == "0012")
		// Process model WXKG11LM (new revision) or WXKG12LM button messages
		resultMap = mapButtonEvent(Integer.parseInt(valueHex[2..3],16))
	// Process message containing model name and/or battery voltage report
	else if (cluster == "0000" && attrId == "0005")	{
		def data = ""
		def modelName = ""
		def model = valueHex
		if (valueHex.length() > 45) {
			model = valueHex.split("01FF")[0]
			data = valueHex.split("01FF")[1]
			if (data[4..7] == "0121") {
				def BatteryVoltage = (Integer.parseInt((data[10..11] + data[8..9]),16))
				resultMap = getBatteryResult(BatteryVoltage)
			}
		data = ", data: ${valueHex.split("01FF")[1]}"
		}

		// Parsing the model name
		for (int i = 0; i < model.length(); i+=2) {
			def str = model.substring(i, i+2);
			def NextChar = (char)Integer.parseInt(str, 16);
			modelName = modelName + NextChar
		}
		displayDebugLog(" reported model: $modelName$data")
	}
	return resultMap
}

// Parse WXKG11LM (original revision) button message: press, double-click, triple-click, & quad-click
private parse11LMMessage(attrId, value) {
	def messageType = [1: "single-clicked", 2: "double-clicked", 3: "triple-clicked", 4: "quadruple-clicked"]
	def result = [:]
	value = (attrId == "0000") ? 1 : value
	displayDebugLog(": attrID =  $attrId, value = $value")
	if (value <= 4) {
		def descText = " was ${messageType[value]} (Button $value pushed)"
		sendEvent(name: "buttonStatus", value: messageType[value], isStateChange: true, displayed: false)
		runIn(1, clearButtonStatus)
		updateLastPressed("Pressed")
		displayInfoLog(descText)
		result = [
			name: 'button',
			value: "pushed",
			data: [buttonNumber: value],
			isStateChange: true,
			descriptionText: "$device.displayName$descText"
		]
	} else
		displayDebugLog(": Button press message is unrecognized")
	return result
}

// Create map of values to be used for button events
private mapButtonEvent(value) {
	// WXKG11LM (new revision) message values: 0: hold, 1 = push, 2 = double-click, 255 = release
	// WXKG12LM message values: 1 = push, 2 = double-click, 16 = hold, 17 = release, 18 = shaken
	def messageType = [0: "held", 1: "single-clicked", 2: "double-clicked", 16: "held", 17: "released", 18: "shaken", 255: "released"]
	def eventType = [0: "held", 1: "pushed", 2: "pushed", 16: "held", 17: "pushed", 18: "pushed", 255: "pushed"]
	def buttonNum = [0: 1, 1: 1, 2: 2, 16: 1, 17: 4, 18: 3, 255: 3]
	if (value == 17 || value == 255) {
		updateLastPressed("Released")
	} else if (value == 0 || value == 16) {
		updateLastPressed("Held")
	} else if (value <= 18) {
		updateLastPressed("Pressed")
		if (eventType[value] == "pushed")
			runIn(1, clearButtonStatus)
	} else {
		displayDebugLog(": Button press message is unrecognized")
		return [:]
	}
	def descText = " was ${messageType[value]} (Button ${buttonNum[value]} ${eventType[value]})"
	displayInfoLog(descText)
	sendEvent(name: "buttonStatus", value: messageType[value], isStateChange: true, displayed: false)
	return [
		name: 'button',
		value: eventType[value],
		data: [buttonNumber: buttonNum[value]],
		descriptionText: "$device.displayName$descText",
		isStateChange: true
	]
}

// on any type of button pressed update lastHeld(CoRE), lastPressed(CoRE), or lastReleased(CoRE) to current date/time
def updateLastPressed(pressType) {
	displayDebugLog(": Setting Last $pressType to current date/time")
	sendEvent(name: "last${pressType}", value: formatDate(), displayed: false)
	sendEvent(name: "last${pressType}CoRE", value: now(), displayed: false)
}

def clearButtonStatus() {
	sendEvent(name: "buttonStatus", value: "released", isStateChange: true, displayed: false)
}

// Check catchall for battery voltage data to pass to getBatteryResult for conversion to percentage report
private Map parseCatchAllMessage(String description) {
	Map resultMap = [:]
	def catchall = zigbee.parseDescriptionAsMap(description)
	//displayDebugLog(": Zigbee parse of catchall = $catchall")
	//displayDebugLog(": Length of data payload = ${catchall.value.size()}")
	// Parse battery voltage data from catchall messages with payload value data larger than 10 bytes
	if ((catchall.attrId == "0005" || catchall.attrId == "FF01") && catchall.value.size() > 20) {
    // Battery voltage value is sent as INT16 in two bytes, #6 & #7, in large-endian (reverse) order
		def batteryString = catchall.data[7] + catchall.data[6]
		if (catchall.additionalAttrs && catchall.additionalAttrs.attrId[0] == "ff01")
			batteryString = catchall.unparsedData[7] + catchall.unparsedData[6]
		displayDebugLog(": Parsing battery voltage string $batteryString")
		resultMap = getBatteryResult(Integer.parseInt(batteryString,16))
	}
    else
    	displayDebugLog(": Catchall message does not contain usable data, no action taken")
	return resultMap
}

// Convert raw 4 digit integer voltage value into percentage based on minVolts/maxVolts range
private Map getBatteryResult(rawValue) {
	// raw voltage is normally supplied as a 4 digit integer that needs to be divided by 1000
	// but in the case the final zero is dropped then divide by 100 to get actual voltage value
	def rawVolts = rawValue / 1000
	def minVolts = voltsmin ? voltsmin : 2.5
	def maxVolts = voltsmax ? voltsmax : 3.0
	def pct = (rawVolts - minVolts) / (maxVolts - minVolts)
	def roundedPct = Math.min(100, Math.round(pct * 100))
	def descText = "Battery at ${roundedPct}% (${rawVolts} Volts)"
	displayInfoLog(": $descText")
	return [
		name: 'battery',
		value: roundedPct,
		unit: "%",
		isStateChange:true,
		descriptionText : "$device.displayName $descText"
	]
}

private def displayDebugLog(message) {
	if (debugLogging)
		log.debug "${device.displayName}${message}"
}

private def displayInfoLog(message) {
	if (infoLogging || state.prefsSetCount < 3)
		log.info "${device.displayName}${message}"
}

//Reset the date displayed in Battery Changed tile to current date
def resetBatteryRuntime(paired) {
	def newlyPaired = paired ? " for newly paired sensor" : ""
	sendEvent(name: "batteryRuntime", value: formatDate(true))
	displayInfoLog(": Setting Battery Changed to current date${newlyPaired}")
}

// installed() runs just after a sensor is paired using the "Add a Thing" method in the SmartThings mobile app
def installed() {
	state.prefsSetCount = 0
	displayInfoLog(": Installing")
	checkIntervalEvent("")
}

// configure() runs after installed() when a sensor is paired
def configure() {
	displayInfoLog(": Configuring")
	initialize()
	device.currentValue("numberOfButtons")?.times {
		sendEvent(name: "button", value: "pushed", data: [buttonNumber: it+1], displayed: false)
	}
	checkIntervalEvent("configured")
	return
}

// updated() will run twice every time user presses save in preference settings page
def updated() {
	displayInfoLog(": Updating preference settings")
	if (!state.prefsSetCount)
		state.prefsSetCount = 1
	else if (state.prefsSetCount < 3)
		state.prefsSetCount = state.prefsSetCount + 1
	initialize()
	if (battReset) {
		resetBatteryRuntime()
		device.updateSetting("battReset", false)
	}
	checkIntervalEvent("preferences updated")
	displayInfoLog(": Info message logging enabled")
	displayDebugLog(": Debug message logging enabled")
}

def initialize() {
	sendEvent(name: "DeviceWatch-Enroll", value: JsonOutput.toJson([protocol: "zigbee", scheme:"untracked"]), displayed: false)
	clearButtonStatus()
	if (!device.currentState('batteryRuntime')?.value)
		resetBatteryRuntime(true)
	if (!state.numButtons)
		setNumButtons()
}

def setNumButtons() {
	if (device.getDataValue("model")) {
		def modelName = device.getDataValue("model")
		def modelText = "Button WXKG12LM"
		state.numButtons = 4
		if (modelName.startsWith("lumi.sensor_switch.aq2")) {
			modelText = "Button WXKG11LM (original revision)"
		} else if (modelName.startsWith("lumi.remote.b1acn01")) {
			modelText = "Button WXKG11LM (new revision)"
			state.numButtons = 3
		}
		displayInfoLog(": Model is Aqara $modelText.")
		displayInfoLog(": Number of buttons set to ${state.numButtons}.")
		sendEvent(name: "numberOfButtons", value: state.numButtons)
	} else {
		displayInfoLog(": Model is unknown, so number of buttons is set to default of 4.")
		sendEvent(name: "numberOfButtons", value: 4)
	}
}

private checkIntervalEvent(text) {
	// Device wakes up every 1 hours, this interval allows us to miss one wakeup notification before marking offline
	if (text)
		displayInfoLog(": Set health checkInterval when ${text}")
	sendEvent(name: "checkInterval", value: 2 * 60 * 60 + 2 * 60, displayed: false, data: [protocol: "zigbee", hubHardwareId: device.hub.hardwareID])
}

def formatDate(batteryReset) {
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
		if (batteryReset)
			return new Date().format("MMM dd yyyy", correctedTimezone)
		else
			return new Date().format("EEE MMM dd yyyy ${timeString}", correctedTimezone)
	}
	else if (dateformat == "UK") {
		if (batteryReset)
			return new Date().format("dd MMM yyyy", correctedTimezone)
		else
			return new Date().format("EEE dd MMM yyyy ${timeString}", correctedTimezone)
	}
	else {
		if (batteryReset)
			return new Date().format("yyyy MMM dd", correctedTimezone)
		else
			return new Date().format("EEE yyyy MMM dd ${timeString}", correctedTimezone)
	}
}