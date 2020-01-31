/**
 *	Copyright 2015 SmartThings
 *
 *	Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *	in compliance with the License. You may obtain a copy of the License at:
 *
 *		http://www.apache.org/licenses/LICENSE-2.0
 *
 *	Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *	on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *	for the specific language governing permissions and limitations under the License.
 *
 */

metadata {
	definition (name: "ZigBee Switch", namespace: "smartthings", author: "SmartThings", ocfDeviceType: "oic.d.switch", runLocally: true, minHubCoreVersion: '000.019.00012', executeCommandsLocally: true, genericHandler: "Zigbee") {
		capability "Actuator"
		capability "Configuration"
		capability "Refresh"
		capability "Switch"
		capability "Health Check"

		// Generic
		fingerprint profileId: "C05E", deviceId: "0000", inClusters: "0006", deviceJoinName: "Generic On/Off Light", ocfDeviceType: "oic.d.light"
		fingerprint profileId: "0104", deviceId: "0103", inClusters: "0006", deviceJoinName: "Generic On/Off Switch"
		fingerprint profileId: "0104", deviceId: "010A", inClusters: "0006", deviceJoinName: "Generic On/Off Plug", ocfDeviceType: "oic.d.smartplug"

		// Centralite
		fingerprint profileId: "0104", inClusters: "0000, 0003, 0004, 0005, 0006, 0B05", outClusters: "0003, 0006, 0019", manufacturer: "Centralite Systems", model: "4200-C", deviceJoinName: "Centralite Smart Outlet", ocfDeviceType: "oic.d.smartplug"

		// eWeLink
		fingerprint profileId: "0104", inClusters: "0000, 0003, 0004, 0005, 0006", outClusters: "0000", manufacturer: "eWeLink", model: "SA-003-Zigbee", deviceJoinName: "eWeLink SmartPlug (SA-003)", ocfDeviceType: "oic.d.smartplug"

		// EZEX
		fingerprint profileId: "0104", inClusters: "0000, 0003, 0004, 0006", outClusters: "0006, 000A, 0019", manufacturer: "EZEX", model: "E220-KR1N0Z0-HA", deviceJoinName: "EZEX Switch"

		// GDKES
		fingerprint profileId: "0104", inClusters: "0000, 0003, 0005, 0004, 0006", manufacturer: "REXENSE", model: "HY0001", deviceJoinName: "GDKES Smart Switch"
		fingerprint profileId: "0104", inClusters: "0000, 0003, 0004, 0005, 0006, 0702, 0B04", manufacturer: "REXENSE", model: "RH5006", deviceJoinName: "GDKES Smart Outlet (GDKES-016)", ocfDeviceType: "oic.d.smartplug"
		fingerprint profileId: "0104", inClusters: "0000, 0003, 0004, 0005, 0006, 0702, 0B04", manufacturer: "REXENSE", model: "RH5005", deviceJoinName: "GDKES Smart Outlet (GDKES-015)", ocfDeviceType: "oic.d.smartplug"

		// HEIMAN
		fingerprint profileId: "0104", inClusters: "0005, 0004, 0006", outClusters: "0003, 0019", manufacturer: "HEIMAN", model: "HS2SW1L-EFR-3.0", deviceJoinName: "HEIMAN Smart Switch"
		
		// HONYAR
		fingerprint profileId: "0104", inClusters: "0000, 0003, 0004, 0005, 0006", manufacturer: "REX", model: "HY0095", deviceJoinName: "HONYAR Smart Switch"

		// IKEA
		fingerprint profileId: "0104", inClusters: "0000, 0003, 0004, 0005, 0006, 0008, FC7C", outClusters: "0005, 0019, 0020", manufacturer:"IKEA of Sweden", model: "TRADFRI control outlet", deviceJoinName: "IKEA TRÅDFRI control outlet", ocfDeviceType: "oic.d.smartplug"

		// INGENIUM
		fingerprint profileId: "C05E", inClusters: "0000, 0003, 0004, 0005, 0006, FFFF", outClusters: "0019", manufacturer: "MEGAMAN", model: "BSZTM005", deviceJoinName: "INGENIUM ZB Mains Switching Module"

		// Innr
		fingerprint profileId: "0104", inClusters: "0000, 0003, 0004, 0005, 0006, 0008, 0B05, 1000, FC82", outClusters: "000A, 0019", manufacturer: "innr", model: "SP 222", deviceJoinName: "Innr Smart Plug", ocfDeviceType: "oic.d.smartplug"
		fingerprint profileId: "0104", inClusters: "0000, 0003, 0004, 0005, 0006, 0008, 0B05, 1000, FC82", outClusters: "000A, 0019", manufacturer: "innr", model: "SP 224", deviceJoinName: "Innr Smart Plug", ocfDeviceType: "oic.d.smartplug"

		// Lowes Iris
		fingerprint profileId: "0104", inClusters: "0000, 0003, 0006, 0402, 0B05, FC01, FC02", outClusters: "0003, 0019", manufacturer: "iMagic by GreatStar", model: "1113-S", deviceJoinName: "Iris Smart Plug", ocfDeviceType: "oic.d.smartplug"

		// Leviton
		fingerprint profileId: "0104", inClusters: "0000, 0003, 0006", outClusters: "0003, 0006, 0019, 0406", manufacturer: "Leviton", model: "ZSS-10", deviceJoinName: "Leviton Switch"
		fingerprint profileId: "0104", inClusters: "0000, 0003, 0006", outClusters: "000A", manufacturer: "HAI", model: "65A21-1", deviceJoinName: "Leviton Wireless Load Control Module-30amp"
		fingerprint profileId: "0104", inClusters: "0000, 0003, 0004, 0005, 0006", outClusters: "0003, 0006, 0008, 0019, 0406", manufacturer: "Leviton", model: "DL15A", deviceJoinName: "Leviton Lumina RF Plug-In Appliance Module", ocfDeviceType: "oic.d.smartplug"
		fingerprint profileId: "0104", inClusters: "0000, 0003, 0004, 0005, 0006", outClusters: "0003, 0006, 0008, 0019, 0406", manufacturer: "Leviton", model: "DL15S", deviceJoinName: "Leviton Lumina RF Switch"

		// Orvibo
		fingerprint profileId: "0104", inClusters: "0000, 0005, 0004, 0006", outClusters: "0000", manufacturer: "ORVIBO", model: "095db3379e414477ba6c2f7e0c6aa026", deviceJoinName: "Orvibo Smart Switch"
		fingerprint profileId: "0104", inClusters: "0000, 0005, 0004, 0006", outClusters: "0000", manufacturer: "ORVIBO", model: "fdd5fce51a164c7ab73b2f4d8d84c88e", deviceJoinName: "Orvibo Smart Outlet", ocfDeviceType: "oic.d.smartplug"

		// OSRAM/SYLVANIA
		fingerprint profileId: "C05E", inClusters: "0000, 0003, 0004, 0005, 0006, 1000, 0B04, FC0F", outClusters: "0019", manufacturer: "OSRAM", model: "Plug 01", deviceJoinName: "OSRAM SMART+ Plug", ocfDeviceType: "oic.d.smartplug"
		fingerprint profileId: "0104", inClusters: "0000, 0003, 0004, 0005, 0006, 0B05, FC01, FC08", outClusters: "0003, 0019", manufacturer: "LEDVANCE", model: "PLUG", deviceJoinName: "SYLVANIA SMART+ Smart Plug", ocfDeviceType: "oic.d.smartplug"

		// sengled
		fingerprint profileId: "0104", inClusters: "0000, 0003, 0004, 0005, 0006, 0B05", outClusters: "0019", manufacturer: "sengled", model: "E1C-NB6", deviceJoinName: "Sengled Element Outlet", ocfDeviceType: "oic.d.smartplug"

		// SONOFF
		fingerprint profileId: "0104", inClusters: "0000, 0003, 0004, 0005, 0006", outClusters: "0000", manufacturer: "SONOFF", model: "BASICZBR3", deviceJoinName: "SONOFF Basic (R3 Zigbee)", ocfDeviceType: "oic.d.smartplug"
		fingerprint profileId: "0104", inClusters: "0000, 0003, 0004, 0005, 0006", outClusters: "0000", manufacturer: "SONOFF", model: "S31 Lite zb", deviceJoinName: "S31 Lite zb", ocfDeviceType: "oic.d.smartplug"
		
		// Terncy
		fingerprint profileId: "0104", inClusters: "0000, 0003, 0006", outClusters: "0019", manufacturer: "", model: "TERNCY-LS01", deviceJoinName: "Terncy Smart Light Socket"

		// Third Reality
		fingerprint profileId: "0104", inClusters: "0000, 0003, 0004, 0005, 0006, 0019", manufacturer: "Third Reality, Inc", model: "3RSS008Z", deviceJoinName: "RealitySwitch Plus"
		fingerprint profileId: "0104", inClusters: "0000, 0003, 0004, 0005, 0006, 0019", manufacturer: "Third Reality, Inc", model: "3RSS007Z", deviceJoinName: "RealitySwitch"
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
				attributeState "on", label:'${name}', action:"switch.off", icon:"st.switches.light.on", backgroundColor:"#00A0DC", nextState:"turningOff"
				attributeState "off", label:'${name}', action:"switch.on", icon:"st.switches.light.off", backgroundColor:"#ffffff", nextState:"turningOn"
				attributeState "turningOn", label:'${name}', action:"switch.off", icon:"st.switches.light.on", backgroundColor:"#00A0DC", nextState:"turningOff"
				attributeState "turningOff", label:'${name}', action:"switch.on", icon:"st.switches.light.off", backgroundColor:"#ffffff", nextState:"turningOn"
			}
		}
		standardTile("refresh", "device.refresh", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
			state "default", label:"", action:"refresh.refresh", icon:"st.secondary.refresh"
		}
		main "switch"
		details(["switch", "refresh"])
	}
}

// Parse incoming device messages to generate events
def parse(String description) {
	log.debug "description is $description"
	def event = zigbee.getEvent(description)
	if (event) {
		sendEvent(event)
	}
	else {
		log.warn "DID NOT PARSE MESSAGE for description : $description"
		log.debug zigbee.parseDescriptionAsMap(description)
	}
}

def off() {
	zigbee.off()
}

def on() {
	zigbee.on()
}

/**
 * PING is used by Device-Watch in attempt to reach the Device
 * */
def ping() {
	return refresh()
}

def refresh() {
	zigbee.onOffRefresh() + zigbee.onOffConfig()
}

def configure() {
	// Device-Watch allows 2 check-in misses from device + ping (plus 2 min lag time)
	sendEvent(name: "checkInterval", value: 2 * 10 * 60 + 2 * 60, displayed: false, data: [protocol: "zigbee", hubHardwareId: device.hub.hardwareID])
	log.debug "Configuring Reporting and Bindings."
	zigbee.onOffRefresh() + zigbee.onOffConfig()
}
