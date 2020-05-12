/**
 *  Copyright 2017 SmartThings
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
import physicalgraph.zigbee.zcl.DataType

metadata {
	definition (name: "ZLL RGBW Bulb", namespace: "smartthings", author: "SmartThings", ocfDeviceType: "oic.d.light", runLocally: true, minHubCoreVersion: '000.021.00001', executeCommandsLocally: true, genericHandler: "ZLL") {

		capability "Actuator"
		capability "Color Control"
		capability "Color Temperature"
		capability "Configuration"
		capability "Polling"
		capability "Refresh"
		capability "Switch"
		capability "Switch Level"
		capability "Health Check"
		capability "Light"

		attribute "colorName", "string"

		// Generic
		fingerprint profileId: "C05E", deviceId: "0210", inClusters: "0006, 0008, 0300", deviceJoinName: "Light" //Generic RGBW Light

		// AduroSmart
		fingerprint profileId: "C05E", inClusters: "0000, 0003, 0004, 0005, 0006, 0008, 0300, FFFF, 0019", outClusters: "0019", deviceId: "0210", manufacturer: "AduroSmart Eria", model: "ZLL-ExtendedColor", deviceJoinName: "Eria Light" //Eria ZLL RGBW Bulb

		// GLEDOPTO
		fingerprint manufacturer: "GLEDOPTO", model: "GL-C-008", deviceJoinName: "Gledopto Switch", ocfDeviceType: "oic.d.switch" // raw description 0B C05E 0210 02 07 0000 0003 0004 0005 0006 0008 0300 00 //Gledopto RGB+CCT LED Controller

		// INGENIUM
		fingerprint profileId: "C05E", inClusters: "0000, 0003, 0004, 0005, 0006, 0008, 0300, FFFF", outClusters: "0019", manufacturer: "Megaman", model: "ZLL-ExtendedColor", deviceJoinName: "INGENIUM Light" //INGENIUM ZB RGBW Light

		// Innr
		fingerprint profileId: "C05E", inClusters: "0000, 0003, 0004, 0005, 0006, 0008, 0300", outClusters: "0019", manufacturer: "innr", model: "RB 185 C", deviceJoinName: "Innr Light", mnmn: "SmartThings", vid: "generic-rgbw-color-bulb-2000K-6500K" //Innr Smart Bulb Color
		fingerprint profileId: "C05E", inClusters: "0000, 0003, 0004, 0005, 0006, 0008, 0300", outClusters: "0019", manufacturer: "innr", model: "FL 130 C", deviceJoinName: "Innr Light" //Innr Flex Light Color

		// OSRAM
		fingerprint profileId: "C05E", inClusters: "0000, 0003, 0004, 0005, 0006, 0008, 0300, 1000", outClusters: "0019", "manufacturer":"OSRAM", "model":"Classic A60 RGBW", deviceJoinName: "OSRAM Light" //OSRAM SMART+ LED Classic A60 RGBW
		fingerprint profileId: "C05E", inClusters: "0000, 0003, 0004, 0005, 0006, 0008, 0300, 0B04, FC0F", outClusters: "0019", manufacturer: "OSRAM", model: "PAR 16 50 RGBW - LIGHTIFY", deviceJoinName: "OSRAM Light" //OSRAM SMART+ RGBW PAR 16 50
		fingerprint profileId: "C05E", inClusters: "0000, 0003, 0004, 0005, 0006, 0008, 0300, 1000, FC0F", outClusters: "0019", manufacturer: "OSRAM", model: "CLA60 RGBW OSRAM", deviceJoinName: "OSRAM Light" //OSRAM SMART+ LED Classic A60 RGBW
		fingerprint profileId: "C05E", inClusters: "0000,0003,0004,0005,0006,0008,0300,1000,FC0F", outClusters: "0019", manufacturer: "OSRAM", model: "Flex RGBW", deviceJoinName: "OSRAM Light" //OSRAM SMART+ Flex RGBW
		fingerprint profileId: "C05E", inClusters: "0000,0003,0004,0005,0006,0008,0300,1000,FC0F", outClusters: "0019", manufacturer: "OSRAM", model: "Gardenpole RGBW-Lightify", deviceJoinName: "OSRAM Light" //OSRAM SMART+ Gardenpole RGBW
		fingerprint profileId: "C05E", inClusters: "0000,0003,0004,0005,0006,0008,0300,1000,FC0F", outClusters: "0019", manufacturer: "OSRAM", model: "LIGHTIFY Outdoor Flex RGBW", deviceJoinName: "OSRAM Light" //OSRAM SMART+ Outdoor Flex RGBW
		fingerprint profileId: "C05E", inClusters: "0000,0003,0004,0005,0006,0008,0300,1000,FC0F", outClusters: "0019", manufacturer: "OSRAM", model: "LIGHTIFY Indoor Flex RGBW", deviceJoinName: "OSRAM Light", mnmn: "SmartThings", vid: "generic-rgbw-color-bulb-2000K-6500K" //OSRAM SMART+ Indoor Flex RGBW

		// Philips Hue
		fingerprint profileId: "C05E", inClusters: "0000,0003,0004,0005,0006,0008,0300,1000", outClusters: "0019", manufacturer: "Philips", model: "LCT001", deviceJoinName: "Philips Light" //Philips Hue A19
		fingerprint profileId: "C05E", inClusters: "0000,0003,0004,0005,0006,0008,0300,1000", outClusters: "0019", manufacturer: "Philips", model: "LCT002", deviceJoinName: "Philips Light" //Philips Hue BR30
		fingerprint profileId: "C05E", inClusters: "0000,0003,0004,0005,0006,0008,0300,1000", outClusters: "0019", manufacturer: "Philips", model: "LCT003", deviceJoinName: "Philips Light" //Philips Hue GU10
		fingerprint profileId: "C05E", inClusters: "0000,0003,0004,0005,0006,0008,0300,1000", outClusters: "0019", manufacturer: "Philips", model: "LCT007", deviceJoinName: "Philips Light" //Philips Hue A19
		fingerprint profileId: "C05E", inClusters: "0000,0003,0004,0005,0006,0008,0300,1000", outClusters: "0019", manufacturer: "Philips", model: "LCT010", deviceJoinName: "Philips Light" //Philips Hue A19
		fingerprint profileId: "C05E", inClusters: "0000,0003,0004,0005,0006,0008,0300,1000", outClusters: "0019", manufacturer: "Philips", model: "LCT011", deviceJoinName: "Philips Light" //Philips Hue BR30
		fingerprint profileId: "C05E", inClusters: "0000,0003,0004,0005,0006,0008,0300,1000", outClusters: "0019", manufacturer: "Philips", model: "LCT012", deviceJoinName: "Philips Light" //Philips Hue Candle
		fingerprint profileId: "C05E", inClusters: "0000,0003,0004,0005,0006,0008,0300,1000", outClusters: "0019", manufacturer: "Philips", model: "LCT014", deviceJoinName: "Philips Light" //Philips Hue A19
		fingerprint profileId: "C05E", inClusters: "0000,0003,0004,0005,0006,0008,0300,1000", outClusters: "0019", manufacturer: "Philips", model: "LCT015", deviceJoinName: "Philips Light" //Philips Hue A19
		fingerprint profileId: "C05E", inClusters: "0000,0003,0004,0005,0006,0008,0300,1000", outClusters: "0019", manufacturer: "Philips", model: "LCT016", deviceJoinName: "Philips Light" //Philips Hue A19
		fingerprint profileId: "C05E", inClusters: "0000,0003,0004,0005,0006,0008,0300,1000", outClusters: "0019", manufacturer: "Philips", model: "LST001", deviceJoinName: "Philips Light" //Philips Hue Lightstrip
		fingerprint profileId: "C05E", inClusters: "0000,0003,0004,0005,0006,0008,0300,1000", outClusters: "0019", manufacturer: "Philips", model: "LST002", deviceJoinName: "Philips Light" //Philips Hue Lightstrip
		
		//XLSmart
		fingerprint profileId: "C05E", manufacturer: "GLEDOPTO", model: "GL-B-001Z", deviceJoinName: "XLSmart Light" //XLSmart E14 RGBW Light Bulb
	}

	// UI tile definitions
	tiles(scale: 2) {
		multiAttributeTile(name:"switch", type: "lighting", width: 6, height: 4, canChangeIcon: true){
			tileAttribute ("device.switch", key: "PRIMARY_CONTROL") {
				attributeState "on", label:'${name}', action:"switch.off", icon:"st.lights.philips.hue-single", backgroundColor:"#00A0DC", nextState:"turningOff"
				attributeState "off", label:'${name}', action:"switch.on", icon:"st.lights.philips.hue-single", backgroundColor:"#ffffff", nextState:"turningOn"
				attributeState "turningOn", label:'${name}', action:"switch.off", icon:"st.lights.philips.hue-single", backgroundColor:"#00A0DC", nextState:"turningOff"
				attributeState "turningOff", label:'${name}', action:"switch.on", icon:"st.lights.philips.hue-single", backgroundColor:"#ffffff", nextState:"turningOn"
			}
			tileAttribute ("device.level", key: "SLIDER_CONTROL") {
				attributeState "level", action:"switch level.setLevel"
			}
			tileAttribute ("device.color", key: "COLOR_CONTROL") {
				attributeState "color", action:"color control.setColor"
			}
		}
		controlTile("colorTempSliderControl", "device.colorTemperature", "slider", width: 4, height: 2, inactiveLabel: false, range:"(2700..6500)") {
			state "colorTemperature", action:"color temperature.setColorTemperature"
		}
		valueTile("colorName", "device.colorName", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
			state "colorName", label: '${currentValue}'
		}
		standardTile("refresh", "device.refresh", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
			state "default", label:"", action:"refresh.refresh", icon:"st.secondary.refresh"
		}

		main(["switch"])
		details(["switch", "colorTempSliderControl", "colorName", "refresh"])
	}
}

//Globals
private getATTRIBUTE_HUE() { 0x0000 }
private getATTRIBUTE_SATURATION() { 0x0001 }
private getHUE_COMMAND() { 0x00 }
private getSATURATION_COMMAND() { 0x03 }
private getMOVE_TO_HUE_AND_SATURATION_COMMAND() { 0x06 }
private getCOLOR_CONTROL_CLUSTER() { 0x0300 }
private getATTRIBUTE_COLOR_TEMPERATURE() { 0x0007 }
private getMOVE_TO_COLOR_TEMPERATURE_COMMAND() { 0x0A }

// Parse incoming device messages to generate events
def parse(String description) {
	log.debug "description is $description"

	def event = zigbee.getEvent(description)
	if (event) {
		log.debug event
		if (event.name == "level" && event.value == 0) {}
		else {
			if (event.name == "colorTemperature") {
				setGenericName(event.value)
			}
			sendEvent(event)
		}
	}
	else {
		def zigbeeMap = zigbee.parseDescriptionAsMap(description)
		log.trace "zigbeeMap : $zigbeeMap"

		if (zigbeeMap?.clusterInt == COLOR_CONTROL_CLUSTER && zigbeeMap.value != null) {
			if(zigbeeMap.attrInt == ATTRIBUTE_HUE){  //Hue Attribute
				def hueValue = Math.round(zigbee.convertHexToInt(zigbeeMap.value) / 0xfe * 100)
				sendEvent(name: "hue", value: hueValue, displayed:false)
			}
			else if(zigbeeMap.attrInt == ATTRIBUTE_SATURATION){ //Saturation Attribute
				def saturationValue = Math.round(zigbee.convertHexToInt(zigbeeMap.value) / 0xfe * 100)
				sendEvent(name: "saturation", value: saturationValue, displayed:false)
			}
		}
		else {
			log.info "DID NOT PARSE MESSAGE for description : $description"
		}
	}
}

def on() {
	zigbee.on() + ["delay 1500"] + zigbee.onOffRefresh()
}

def off() {
	zigbee.off() + ["delay 1500"] + zigbee.onOffRefresh()
}

def refresh() {
	refreshAttributes() + configureAttributes()
}

def poll() {
	configureHealthCheck()

	refreshAttributes()
}

def ping() {
	refreshAttributes()
}

def healthPoll() {
	log.debug "healthPoll()"
	def cmds = refreshAttributes()
	cmds.each{ sendHubCommand(new physicalgraph.device.HubAction(it))}
}

def configureHealthCheck() {
	if (!state.hasConfiguredHealthCheck) {
		log.debug "Configuring Health Check, Reporting"
		unschedule("healthPoll", [forceForLocallyExecuting: true])
		runEvery5Minutes("healthPoll", [forceForLocallyExecuting: true])
		state.hasConfiguredHealthCheck = true
	}
}

def configure() {
	log.debug "Configuring Reporting and Bindings."
	configureAttributes() + refreshAttributes()
}

def configureAttributes() {
	zigbee.onOffConfig() +
	zigbee.levelConfig()
}

def refreshAttributes() {
	zigbee.onOffRefresh() +
	zigbee.levelRefresh() +
	zigbee.colorTemperatureRefresh() +
	zigbee.readAttribute(COLOR_CONTROL_CLUSTER, ATTRIBUTE_HUE) +
	zigbee.readAttribute(COLOR_CONTROL_CLUSTER, ATTRIBUTE_SATURATION)
}

def updated() {
	sendEvent(name: "checkInterval", value: 2 * 10 * 60 + 1 * 60, displayed: false, data: [protocol: "zigbee", hubHardwareId: device.hub.hardwareID])
	configureHealthCheck()
}

def installed() {
	sendEvent(name: "checkInterval", value: 2 * 10 * 60 + 1 * 60, displayed: false, data: [protocol: "zigbee", hubHardwareId: device.hub.hardwareID])
	configureHealthCheck()

	if (isInnr185C()) {
		sendHubCommand(zigbee.command(COLOR_CONTROL_CLUSTER, MOVE_TO_HUE_AND_SATURATION_COMMAND, getScaledHue(0), getScaledSaturation(0), "0000"))
	}
}

def setColorTemperature(value) {
	value = value as Integer
	def tempInMired = Math.round(1000000 / value)
	def finalHex = zigbee.swapEndianHex(zigbee.convertToHexString(tempInMired, 4))

	zigbee.command(COLOR_CONTROL_CLUSTER, MOVE_TO_COLOR_TEMPERATURE_COMMAND, "$finalHex 0000") +
	["delay 1500"] +
	zigbee.readAttribute(COLOR_CONTROL_CLUSTER, ATTRIBUTE_COLOR_TEMPERATURE)
}

def setLevel(value, rate = null) {
	zigbee.setLevel(value) + zigbee.onOffRefresh() + zigbee.levelRefresh() //adding refresh because of ZLL bulb not conforming to send-me-a-report
}

private getScaledHue(value) {
	zigbee.convertToHexString(Math.round(value * 0xfe / 100.0), 2)
}

private getScaledSaturation(value) {
	zigbee.convertToHexString(Math.round(value * 0xfe / 100.0), 2)
}

def setColor(value){
	log.trace "setColor($value)"
	zigbee.on() +
	zigbee.command(COLOR_CONTROL_CLUSTER, MOVE_TO_HUE_AND_SATURATION_COMMAND,
		getScaledHue(value.hue), getScaledSaturation(value.saturation), "0000") +
	zigbee.onOffRefresh() +
	zigbee.readAttribute(COLOR_CONTROL_CLUSTER, ATTRIBUTE_HUE) +
	zigbee.readAttribute(COLOR_CONTROL_CLUSTER, ATTRIBUTE_SATURATION)
}

//Naming based on the wiki article here: http://en.wikipedia.org/wiki/Color_temperature
def setGenericName(value){
	if (value != null) {
		def genericName = "White"
		if (value < 3300) {
			genericName = "Soft White"
		} else if (value < 4150) {
			genericName = "Moonlight"
		} else if (value <= 5000) {
			genericName = "Cool White"
		} else if (value >= 5000) {
			genericName = "Daylight"
		}
		sendEvent(name: "colorName", value: genericName)
	}
}

def setHue(value) {
	//payload-> hue value, direction (00-> shortest distance), transition time (1/10th second)
	zigbee.command(COLOR_CONTROL_CLUSTER, HUE_COMMAND, getScaledHue(value), "00", "0000") +
	zigbee.readAttribute(COLOR_CONTROL_CLUSTER, ATTRIBUTE_HUE)
}

def setSaturation(value) {
	//payload-> sat value, transition time
	zigbee.command(COLOR_CONTROL_CLUSTER, SATURATION_COMMAND, getScaledSaturation(value), "0000") +
	zigbee.readAttribute(COLOR_CONTROL_CLUSTER, ATTRIBUTE_SATURATION)
}

private boolean isInnr185C() {
	device.getDataValue("model") == "RB 185 C"
}
