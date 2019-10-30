/**
 *  Copyright 2016 SmartThings
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
	definition(name: "ZLL Dimmer Bulb", namespace: "smartthings", author: "SmartThings", ocfDeviceType: "oic.d.light", runLocally: true, minHubCoreVersion: '000.021.00001', executeCommandsLocally: true, genericHandler: "ZLL") {
		capability "Actuator"
		capability "Configuration"
		capability "Polling"
		capability "Refresh"
		capability "Switch"
		capability "Switch Level"
		capability "Health Check"

		// Generic
		fingerprint profileId: "C05E", deviceId: "0100", inClusters: "0006, 0008", deviceJoinName: "Generic Dimmable Light"

		// AduroSmart
		fingerprint profileId: "C05E", inClusters: "0000, 0003, 0004, 0005, 0006, 0008, FFFF, 0019", outClusters: "0019", deviceId: "0100", manufacturer: "AduroSmart Eria", model: "ZLL-DimmableLight", deviceJoinName: "Eria ZLL Dimmable Bulb"

		// IKEA
		fingerprint inClusters: "0000, 0003, 0004, 0005, 0006, 0008, 0B05, 1000", outClusters: "0005, 0019, 0020, 1000", manufacturer: "IKEA of Sweden", model: "TRADFRI bulb E26 opal 1000lm", deviceJoinName: "IKEA TRÅDFRI LED Bulb"
		fingerprint profileId: "C05E", inClusters: "0000, 0003, 0004, 0005, 0006, 0008, 0B05, 1000", outClusters: "0005, 0019, 0020, 1000", manufacturer: "IKEA of Sweden", model: "TRADFRI bulb E12 W op/ch 400lm", deviceJoinName: "IKEA TRÅDFRI LED Bulb"
		fingerprint profileId: "C05E", inClusters: "0000, 0003, 0004, 0005, 0006, 0008, 0B05, 1000", outClusters: "0005, 0019, 0020, 1000", manufacturer: "IKEA of Sweden", model: "TRADFRI bulb E17 W op/ch 400lm", deviceJoinName: "IKEA TRÅDFRI LED Bulb"
		fingerprint profileId: "C05E", inClusters: "0000, 0003, 0004, 0005, 0006, 0008, 0B05, 1000", outClusters: "0005, 0019, 0020, 1000", manufacturer: "IKEA of Sweden", model: "TRADFRI bulb GU10 W 400lm", deviceJoinName: "IKEA TRÅDFRI LED Bulb"
		fingerprint profileId: "C05E", inClusters: "0000, 0003, 0004, 0005, 0006, 0008, 0B05, 1000", outClusters: "0005, 0019, 0020, 1000", manufacturer: "IKEA of Sweden", model: "TRADFRI bulb E27 W opal 1000lm", deviceJoinName: "IKEA TRÅDFRI LED Bulb"
		fingerprint profileId: "C05E", inClusters: "0000, 0003, 0004, 0005, 0006, 0008, 0B05, 1000", outClusters: "0005, 0019, 0020, 1000", manufacturer: "IKEA of Sweden", model: "TRADFRI bulb E14 W op/ch 400lm", deviceJoinName: "IKEA TRÅDFRI LED Bulb"
		fingerprint profileId: "C05E", inClusters: "0000, 0003, 0004, 0005, 0006, 0008, 0B05, 1000", outClusters: "0005, 0019, 0020, 1000", manufacturer: "IKEA of Sweden", model: "TRADFRI transformer 10W", deviceJoinName: "IKEA TRÅDFRI Driver for wireless control 10W"
		fingerprint profileId: "C05E", inClusters: "0000, 0003, 0004, 0005, 0006, 0008, 0B05, 1000", outClusters: "0005, 0019, 0020, 1000", manufacturer: "IKEA of Sweden", model: "TRADFRI Driver 10W", deviceJoinName: "IKEA TRÅDFRI Driver for wireless control 10W"
		fingerprint profileId: "C05E", inClusters: "0000, 0003, 0004, 0005, 0006, 0008, 0B05, 1000", outClusters: "0005, 0019, 0020, 1000", manufacturer: "IKEA of Sweden", model: "TRADFRI transformer 30W", deviceJoinName: "IKEA TRÅDFRI Driver for wireless control 30W"
		fingerprint profileId: "C05E", inClusters: "0000, 0003, 0004, 0005, 0006, 0008, 0B05, 1000", outClusters: "0005, 0019, 0020, 1000", manufacturer: "IKEA of Sweden", model: "TRADFRI Driver 30W", deviceJoinName: "IKEA TRÅDFRI Driver for wireless control 30W"

		// INGENIUM
		fingerprint profileId: "C05E", inClusters: "0000, 0003, 0004, 0005, 0006, 0008, FFFF", outClusters: "0019",manufacturer: "Megaman", model: "ZLL-DimmableLight", deviceJoinName: "INGENIUM ZB Dimmable Light"
		fingerprint profileId: "C05E", inClusters: "0000, 0003, 0004, 0005, 0006, 0008, FFFF", outClusters: "0019",manufacturer: "MEGAMAN", model: "BSZTM002", deviceJoinName: "INGENIUM ZB Dimmable A60 Bulb"
		fingerprint profileId: "C05E", inClusters: "0000, 0003, 0004, 0005, 0006, 0008, FFFF", outClusters: "0019",manufacturer: "MEGAMAN", model: "BSZTM003", deviceJoinName: "INGENIUM ZB Dimming Module"

		// Innr
		fingerprint profileId: "C05E", inClusters: "0000, 0003, 0004, 0005, 0006, 0008", outClusters: "0019", manufacturer: "innr", model: "RS 125", deviceJoinName: "Innr Smart Spot White"
		fingerprint profileId: "C05E", inClusters: "0000, 0003, 0004, 0005, 0006, 0008", outClusters: "0019", manufacturer: "innr", model: "RB 165", deviceJoinName: "Innr Smart Bulb White"
		fingerprint profileId: "C05E", inClusters: "0000, 0003, 0004, 0005, 0006, 0008", outClusters: "0019", manufacturer: "innr", model: "RB 175 W", deviceJoinName: "Innr Smart Bulb Warm Dimming"
		fingerprint profileId: "C05E", inClusters: "0000, 0003, 0004, 0005, 0006, 0008", outClusters: "0019", manufacturer: "innr", model: "RB 145", deviceJoinName: "Innr Smart Candle White"

		// OSRAM
		fingerprint profileId: "C05E", inClusters: "0000, 0003, 0004, 0005, 0006, 0008, 1000, 0B04, FC0F", outClusters: "0019", manufacturer: "OSRAM", model: "Classic A60 W clear", deviceJoinName: "OSRAM SMART+ LED Smart Connected Light"
		fingerprint profileId: "C05E", inClusters: "0000, 0003, 0004, 0005, 0006, 0008, 1000, 0B04, FC0F", outClusters: "0019", manufacturer: "OSRAM", model: "Classic A60 W clear - LIGHTIFY", deviceJoinName: "OSRAM SMART+ LED Smart Connected Light"
		fingerprint profileId: "C05E", inClusters: "0000, 0003, 0004, 0005, 0006, 0008, 1000, FC0F", outClusters: "0019", manufacturer: "OSRAM", model: "CLA60 OFD OSRAM", deviceJoinName: "OSRAM SMART+ LED Classic A60 Dimming"

		// Philips Hue
		fingerprint profileId: "C05E", inClusters: "0000, 0003, 0004, 0005, 0006, 0008, 1000", outClusters: "0019", manufacturer: "Philips", model: "LWB004", deviceJoinName: "Philips Hue White"
		fingerprint profileId: "C05E", inClusters: "0000, 0003, 0004, 0005, 0006, 0008, 1000", outClusters: "0019", manufacturer: "Philips", model: "LWB006", deviceJoinName: "Philips Hue White"
		fingerprint profileId: "C05E", inClusters: "0000, 0003, 0004, 0005, 0006, 0008, 1000", outClusters: "0019", manufacturer: "Philips", model: "LWB007", deviceJoinName: "Philips Hue White"
		fingerprint profileId: "C05E", inClusters: "0000, 0003, 0004, 0005, 0006, 0008, 1000", outClusters: "0019", manufacturer: "Philips", model: "LWB010", deviceJoinName: "Philips Hue White"
		fingerprint profileId: "C05E", inClusters: "0000, 0003, 0004, 0005, 0006, 0008, 1000", outClusters: "0019", manufacturer: "Philips", model: "LWB014", deviceJoinName: "Philips Hue White"

		// Sengled
		fingerprint profileId: "0104", inClusters: "0000, 0003, 0004, 0005, 0006, 0008, 0702, 0B05", outClusters: "0019", manufacturer: "sengled", model: "E14-U43", deviceJoinName: "Sengled E14-U43"
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
		}
		standardTile("refresh", "device.switch", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
			state "default", label: "", action: "refresh.refresh", icon: "st.secondary.refresh"
		}
		main "switch"
		details(["switch", "refresh"])
	}
}

// Parse incoming device messages to generate events
def parse(String description) {
	log.debug "description is $description"

	def resultMap = zigbee.getEvent(description)
	if (resultMap) {
		sendEvent(resultMap)
	} else {
		log.debug "DID NOT PARSE MESSAGE for description : $description"
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
	//adding refresh because of ZLL bulb not conforming to send-me-a-report
}

def refresh() {
	zigbee.onOffRefresh() + zigbee.levelRefresh()
}

def poll() {
	refresh()
}

/**
 * PING is used by Device-Watch in attempt to reach the Device
 * */
def ping() {
	return zigbee.levelRefresh()
}

def healthPoll() {
	log.debug "healthPoll()"
	def cmds = refresh()
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
	zigbee.onOffConfig() + zigbee.levelConfig() + zigbee.onOffRefresh() + zigbee.levelRefresh()
}

def updated() {
	log.debug "updated()"
	configureHealthCheck()
}