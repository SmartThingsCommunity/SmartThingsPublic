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

metadata {
	definition(name: "ZLL White Color Temperature Bulb", namespace: "smartthings", author: "SmartThings", ocfDeviceType: "oic.d.light", runLocally: true, minHubCoreVersion: '000.021.00001', executeCommandsLocally: true, genericHandler: "ZLL") {

		capability "Actuator"
		capability "Color Temperature"
		capability "Configuration"
		capability "Polling"
		capability "Refresh"
		capability "Switch"
		capability "Switch Level"
		capability "Health Check"

		attribute "colorName", "string"

		fingerprint profileId: "C05E", inClusters: "0000, 0003, 0004, 0005, 0006, 0008, 0300, 1000, 0B04, FC0F", outClusters: "0019", "manufacturer": "OSRAM", "model": "Classic A60 TW", deviceJoinName: "OSRAM LIGHTIFY LED Classic A60 Tunable White"
		fingerprint profileId: "C05E", inClusters: "0000, 0003, 0004, 0005, 0006, 0008, 0300, 1000, FC0F", outClusters: "0019", "manufacturer": "OSRAM", "model": "PAR16 50 TW", deviceJoinName: "OSRAM LIGHTIFY LED PAR16 50 Tunable White"
		fingerprint profileId: "C05E", inClusters: "0000, 0003, 0004, 0005, 0006, 0008, 0300, 1000, 0B04, FC0F", outClusters: "0019", manufacturer: "OSRAM", model: "Classic B40 TW - LIGHTIFY", deviceJoinName: "OSRAM LIGHTIFY Classic B40 Tunable White"
		fingerprint profileId: "C05E", inClusters: "0000, 0003, 0004, 0005, 0006, 0008, 0300, 1000, FC0F", outClusters: "0019", manufacturer: "OSRAM", model: "CLA60 TW OSRAM", deviceJoinName: "OSRAM LIGHTIFY LED Classic A60 Tunable White"
		fingerprint profileId: "C05E", inClusters: "0000, 0003, 0004, 0005, 0006, 0008, 0300, 1000", outClusters: "0019", manufacturer: "Philips", model: "LTW001", deviceJoinName: "Philips Hue White Ambiance A19"
		fingerprint profileId: "C05E", inClusters: "0000, 0003, 0004, 0005, 0006, 0008, 0300, 1000", outClusters: "0019", manufacturer: "Philips", model: "LTW004", deviceJoinName: "Philips Hue White Ambiance A19"
		fingerprint profileId: "C05E", inClusters: "0000, 0003, 0004, 0005, 0006, 0008, 0300, 1000", outClusters: "0019", manufacturer: "Philips", model: "LTW010", deviceJoinName: "Philips Hue White Ambiance A19"
		fingerprint profileId: "C05E", inClusters: "0000, 0003, 0004, 0005, 0006, 0008, 0300, 1000", outClusters: "0019", manufacturer: "Philips", model: "LTW011", deviceJoinName: "Philips Hue White Ambiance BR30"
		fingerprint profileId: "C05E", inClusters: "0000, 0003, 0004, 0005, 0006, 0008, 0300, 1000", outClusters: "0019", manufacturer: "Philips", model: "LTW012", deviceJoinName: "Philips Hue White Ambiance Candle"
		fingerprint profileId: "C05E", inClusters: "0000, 0003, 0004, 0005, 0006, 0008, 0300, 1000", outClusters: "0019", manufacturer: "Philips", model: "LTW013", deviceJoinName: "Philips Hue White Ambiance Spot"
		fingerprint profileId: "C05E", inClusters: "0000, 0003, 0004, 0005, 0006, 0008, 0300, 1000", outClusters: "0019", manufacturer: "Philips", model: "LTW014", deviceJoinName: "Philips Hue White Ambiance Spot"
		fingerprint profileId: "C05E", inClusters: "0000, 0003, 0004, 0005, 0006, 0008, 0300, 1000", outClusters: "0019", manufacturer: "Philips", model: "LTW015", deviceJoinName: "Philips Hue White Ambiance A19"
		fingerprint profileId: "C05E", inClusters: "0000, 0003, 0004, 0005, 0006, 0008, 0300, 0B05, 1000", outClusters: "0005, 0019, 0020, 1000", manufacturer: "IKEA of Sweden", model: "FLOALT panel WS 30x30", deviceJoinName: "IKEA FLOALT Panel"
		fingerprint profileId: "C05E", inClusters: "0000, 0003, 0004, 0005, 0006, 0008, 0300, 0B05, 1000", outClusters: "0005, 0019, 0020, 1000", manufacturer: "IKEA of Sweden", model: "SURTE door WS 38x64", deviceJoinName: "IKEA SURTE Panel"
	}

	// UI tile definitions
	tiles(scale: 2) {
		multiAttributeTile(name: "switch", type: "lighting", width: 6, height: 4, canChangeIcon: true) {
			tileAttribute("device.switch", key: "PRIMARY_CONTROL") {
				attributeState "on", label: '${name}', action: "switch.off", icon: "st.switches.light.on", backgroundColor: "#00A0DC", nextState: "turningOff"
				attributeState "off", label: '${name}', action: "switch.on", icon: "st.switches.light.off", backgroundColor: "#ffffff", nextState: "turningOn"
				attributeState "turningOn", label: '${name}', action: "switch.off", icon: "st.switches.light.on", backgroundColor: "#00A0DC", nextState: "turningOff"
				attributeState "turningOff", label: '${name}', action: "switch.on", icon: "st.switches.light.off", backgroundColor: "#ffffff", nextState: "turningOn"
			}
			tileAttribute("device.level", key: "SLIDER_CONTROL") {
				attributeState "level", action: "switch level.setLevel"
			}
			tileAttribute("colorName", key: "SECONDARY_CONTROL") {
				attributeState "colorName", label: '${currentValue}'
			}
		}

		standardTile("refresh", "device.refresh", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
			state "default", label: "", action: "refresh.refresh", icon: "st.secondary.refresh"
		}

		controlTile("colorTempSliderControl", "device.colorTemperature", "slider", width: 4, height: 2, inactiveLabel: false, range: "(2700..6500)") {
			state "colorTemperature", action: "color temperature.setColorTemperature"
		}
		valueTile("colorTemp", "device.colorTemperature", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
			state "colorTemperature", label: '${currentValue} K'
		}

		main(["switch"])
		details(["switch", "colorTempSliderControl", "colorTemp", "refresh"])
	}
}

// Globals
private getMOVE_TO_COLOR_TEMPERATURE_COMMAND() { 0x0A }

private getCOLOR_CONTROL_CLUSTER() { 0x0300 }

private getATTRIBUTE_COLOR_TEMPERATURE() { 0x0007 }

// Parse incoming device messages to generate events
def parse(String description) {
	log.debug "description is $description"
	def event = zigbee.getEvent(description)
	if (event) {
		if (event.name == "colorTemperature") {
			setGenericName(event.value)
		}
		sendEvent(event)
	} else {
		log.warn "DID NOT PARSE MESSAGE for description : $description"
		log.debug zigbee.parseDescriptionAsMap(description)
	}
}

def off() {
	zigbee.off() + ["delay 1500"] + zigbee.onOffRefresh()
}

def on() {
	zigbee.on() + ["delay 1500"] + zigbee.onOffRefresh()
}

def setLevel(value, rate = null) {
	zigbee.setLevel(value) + zigbee.onOffRefresh() + zigbee.levelRefresh()
}

def refresh() {
	zigbee.onOffRefresh() +
			zigbee.levelRefresh() +
			zigbee.colorTemperatureRefresh() +
			zigbee.onOffConfig() +
			zigbee.levelConfig()
}

def poll() {
	zigbee.onOffRefresh() + zigbee.levelRefresh() + zigbee.colorTemperatureRefresh()
}

/**
 * PING is used by Device-Watch in attempt to reach the Device
 * */
def ping() {
	return zigbee.levelRefresh()
}

def healthPoll() {
	log.debug "healthPoll()"
	def cmds = poll()
	cmds.each { sendHubCommand(new physicalgraph.device.HubAction(it)) }
}

def configureHealthCheck() {
	Integer hcIntervalMinutes = 12
	if (!state.hasConfiguredHealthCheck) {
		log.debug "Configuring Health Check, Reporting"
		unschedule("healthPoll", [forceForLocallyExecuting: true])
		runEvery5Minutes("healthPoll", [forceForLocallyExecuting: true])
		// Device-Watch allows 2 check-in misses from device
		sendEvent(name: "checkInterval", value: hcIntervalMinutes * 60, displayed: false, data: [protocol: "zigbee", hubHardwareId: device.hub.hardwareID])
		state.hasConfiguredHealthCheck = true
	}
}

def configure() {
	log.debug "configure()"
	configureHealthCheck()
	refresh()
}

def updated() {
	log.debug "updated()"
	configureHealthCheck()
}

def setColorTemperature(value) {
	value = value as Integer
	def tempInMired = Math.round(1000000 / value)
	def finalHex = zigbee.swapEndianHex(zigbee.convertToHexString(tempInMired, 4))

	zigbee.command(COLOR_CONTROL_CLUSTER, MOVE_TO_COLOR_TEMPERATURE_COMMAND, "$finalHex 0000") +
			zigbee.readAttribute(COLOR_CONTROL_CLUSTER, ATTRIBUTE_COLOR_TEMPERATURE)
}

//Naming based on the wiki article here: http://en.wikipedia.org/wiki/Color_temperature
def setGenericName(value) {
	if (value != null) {
		def genericName = ""
		if (value < 3300) {
			genericName = "Soft White"
		} else if (value < 4150) {
			genericName = "Moonlight"
		} else if (value <= 5000) {
			genericName = "Cool White"
		} else {
			genericName = "Daylight"
		}
		sendEvent(name: "colorName", value: genericName)
	}
}
