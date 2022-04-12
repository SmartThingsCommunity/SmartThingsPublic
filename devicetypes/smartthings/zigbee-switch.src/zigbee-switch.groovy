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
		fingerprint profileId: "C05E", deviceId: "0000", inClusters: "0006", deviceJoinName: "Light", ocfDeviceType: "oic.d.light" //Generic On/Off Light
		fingerprint profileId: "0104", deviceId: "0103", inClusters: "0006", deviceJoinName: "Switch" //Generic On/Off Switch
		fingerprint profileId: "0104", deviceId: "010A", inClusters: "0006", deviceJoinName: "Outlet", ocfDeviceType: "oic.d.smartplug" //Generic On/Off Plug

		// Centralite
		fingerprint profileId: "0104", inClusters: "0000, 0003, 0004, 0005, 0006, 0B05", outClusters: "0003, 0006, 0019", manufacturer: "Centralite Systems", model: "4200-C", deviceJoinName: "Centralite Outlet", ocfDeviceType: "oic.d.smartplug" //Centralite Smart Outlet

		// eWeLink
		fingerprint profileId: "0104", inClusters: "0000, 0003, 0004, 0005, 0006", outClusters: "0000", manufacturer: "eWeLink", model: "SA-003-Zigbee", deviceJoinName: "eWeLink Outlet", ocfDeviceType: "oic.d.smartplug" //eWeLink SmartPlug (SA-003)
		fingerprint profileId: "0104", inClusters: "0000,0003,0004,00005,0006", outClusters: "0000", manufacturer: "eWeLink", model: "ZB-SW01", deviceJoinName: "eWeLink Switch"
		
		// Minoston
		fingerprint profileId: "0104", inClusters: "0000, 0003, 0004, 0005, 0006", outClusters: "0000", manufacturer: "Minoston", model: "ZB36S", deviceJoinName: "Minoston Outlet", ocfDeviceType: "oic.d.smartplug" //Minoston SmartPlug
		
		// LELLKI
		fingerprint profileId: "0104", inClusters: "0000,0003,0004,00005,0006", outClusters: "0000", manufacturer: "LELLKI", model: "JZ-ZB-001", deviceJoinName: "LELLKI Switch"
		
		// eZEX 1st Generation Switch
		fingerprint profileId: "0104", inClusters: "0000, 0003, 0004, 0006", outClusters: "0006, 000A, 0019", model: "E220-KR1N0Z0-HA", deviceJoinName: "eZEX Switch"
		// eZEX 2nd Generation Switch
		fingerprint profileId: "0104", inClusters: "0000, 0003, 0004, 0006", outClusters: "0006, 000A, 0019", model: "E220-KR1N0Z1-HA", deviceJoinName: "eZEX Switch"
		// eZEX 3rd Generation Switch
		fingerprint profileId: "0104", inClusters: "0000, 0003, 0004, 0006", outClusters: "0006, 000A, 0019", model: "E220-KR1N0Z2-HA", deviceJoinName: "eZEX Switch"

		// GDKES
		fingerprint profileId: "0104", inClusters: "0000, 0003, 0005, 0004, 0006", manufacturer: "REXENSE", model: "HY0001", deviceJoinName: "GDKES Switch" //GDKES Smart Switch
		fingerprint profileId: "0104", inClusters: "0000, 0003, 0004, 0005, 0006, 0702, 0B04", manufacturer: "REXENSE", model: "RH5006", deviceJoinName: "GDKES Outlet", ocfDeviceType: "oic.d.smartplug" //GDKES Smart Outlet (GDKES-016)
		fingerprint profileId: "0104", inClusters: "0000, 0003, 0004, 0005, 0006, 0702, 0B04", manufacturer: "REXENSE", model: "RH5005", deviceJoinName: "GDKES Outlet", ocfDeviceType: "oic.d.smartplug" //GDKES Smart Outlet (GDKES-015)

		// HEIMAN
		fingerprint profileId: "0104", inClusters: "0005, 0004, 0006", outClusters: "0003, 0019", manufacturer: "HEIMAN", model: "HS2SW1L-EFR-3.0", deviceJoinName: "HEIMAN Switch" //HEIMAN Smart Switch
		fingerprint profileId: "0104", inClusters: "0000, 0003, 0004, 0005, 0006, 0B05", outClusters: "0019", manufacturer: "HEIMAN", model: "HS6ESK-W-EF-3.0", deviceJoinName: "HEIMAN Outlet", ocfDeviceType: "oic.d.smartplug"  //HEIMAN Smart Outlet
		fingerprint profileId: "0104", inClusters: "0005, 0004, 0006", outClusters: "0003, 0019", manufacturer: "HEIMAN", model: "HS6SW1A-W-EF-3.0", deviceJoinName: "HEIMAN Switch" //HEIMAN Smart Switch
		
		// HONYAR
		fingerprint profileId: "0104", inClusters: "0000, 0003, 0004, 0005, 0006", manufacturer: "REX", model: "HY0095", deviceJoinName: "HONYAR Switch" //HONYAR Smart Switch

		// IKEA
		fingerprint profileId: "0104", inClusters: "0000, 0003, 0004, 0005, 0006, 0008, FC7C", outClusters: "0005, 0019, 0020", manufacturer:"IKEA of Sweden", model: "TRADFRI control outlet", deviceJoinName: "IKEA Outlet", ocfDeviceType: "oic.d.smartplug" //IKEA TRÅDFRI control outlet

		// INGENIUM
		fingerprint profileId: "C05E", inClusters: "0000, 0003, 0004, 0005, 0006, FFFF", outClusters: "0019", manufacturer: "MEGAMAN", model: "BSZTM005", deviceJoinName: "INGENIUM Switch" //INGENIUM ZB Mains Switching Module

		// Innr
		fingerprint profileId: "0104", inClusters: "0000, 0003, 0004, 0005, 0006, 0008, 0B05, 1000, FC82", outClusters: "000A, 0019", manufacturer: "innr", model: "SP 220", deviceJoinName: "Innr Outlet", ocfDeviceType: "oic.d.smartplug" //Innr Smart Plug
		fingerprint profileId: "0104", inClusters: "0000, 0003, 0004, 0005, 0006, 0008, 0B05, 1000, FC82", outClusters: "000A, 0019", manufacturer: "innr", model: "SP 222", deviceJoinName: "Innr Outlet", ocfDeviceType: "oic.d.smartplug" //Innr Smart Plug
		fingerprint profileId: "0104", inClusters: "0000, 0003, 0004, 0005, 0006, 0008, 0B05, 1000, FC82", outClusters: "000A, 0019", manufacturer: "innr", model: "SP 224", deviceJoinName: "Innr Outlet", ocfDeviceType: "oic.d.smartplug" //Innr Smart Plug

		// Lowes Iris
		fingerprint profileId: "0104", inClusters: "0000, 0003, 0006, 0402, 0B05, FC01, FC02", outClusters: "0003, 0019", manufacturer: "iMagic by GreatStar", model: "1113-S", deviceJoinName: "Iris Outlet", ocfDeviceType: "oic.d.smartplug" //Iris Smart Plug

		// Leviton
		fingerprint profileId: "0104", inClusters: "0000, 0003, 0006", outClusters: "0003, 0006, 0019, 0406", manufacturer: "Leviton", model: "ZSS-10", deviceJoinName: "Leviton Switch" //Leviton Switch
		fingerprint profileId: "0104", inClusters: "0000, 0003, 0006", outClusters: "000A", manufacturer: "HAI", model: "65A21-1", deviceJoinName: "Leviton Switch" //Leviton Wireless Load Control Module-30amp
		fingerprint profileId: "0104", inClusters: "0000, 0003, 0004, 0005, 0006", outClusters: "0003, 0006, 0008, 0019, 0406", manufacturer: "Leviton", model: "DL15A", deviceJoinName: "Leviton Outlet", ocfDeviceType: "oic.d.smartplug" //Leviton Lumina RF Plug-In Appliance Module
		fingerprint profileId: "0104", inClusters: "0000, 0003, 0004, 0005, 0006", outClusters: "0003, 0006, 0008, 0019, 0406", manufacturer: "Leviton", model: "DL15S", deviceJoinName: "Leviton Switch" //Leviton Lumina RF Switch
		fingerprint profileId: "0104", inClusters: "0000, 0003, 0004, 0005, 0006, 0B05", outClusters: "0019", manufacturer: "Leviton", model: "DG15S", deviceJoinName: "Leviton Switch" //Leviton Lumina RF Switch
		fingerprint manufacturer: "Leviton", model: "DG15A", deviceJoinName: "Leviton Outlet", ocfDeviceType: "oic.d.smartplug" //Leviton Zigbee Plug-In Switch DG15A, Raw Description: 01 0104 010A 00 06 0000 0003 0004 0005 0006 0B05 01 0019

		// NodOn
		fingerprint profileId: "0104", deviceId: "0002", inClusters: "0000, 0003, 0004, 0005, 0006, 0007, 1000, FC57", outClusters: "0019", manufacturer: "NodOn", model: "SIN-4-1-20", deviceJoinName: "NodOn Switch"
		fingerprint profileId: "0104", deviceId: "0002", inClusters: "0000, 0003, 0004, 0005, 0006, 0007, 1000, FC57", outClusters: "0019", manufacturer: "NodOn", model: "SIN-4-1-20_PRO", deviceJoinName: "NodOn Switch"
        
		// Orvibo
		fingerprint profileId: "0104", inClusters: "0000, 0005, 0004, 0006", outClusters: "0000", manufacturer: "ORVIBO", model: "095db3379e414477ba6c2f7e0c6aa026", deviceJoinName: "Orvibo Switch" //Orvibo Smart Switch
		fingerprint profileId: "0104", inClusters: "0000, 0005, 0004, 0006", outClusters: "0000", manufacturer: "ORVIBO", model: "fdd5fce51a164c7ab73b2f4d8d84c88e", deviceJoinName: "Orvibo Outlet", ocfDeviceType: "oic.d.smartplug" //Orvibo Smart Outlet

		// OSRAM/SYLVANIA
		fingerprint profileId: "C05E", inClusters: "0000, 0003, 0004, 0005, 0006, 1000, 0B04, FC0F", outClusters: "0019", manufacturer: "OSRAM", model: "Plug 01", deviceJoinName: "OSRAM Outlet", ocfDeviceType: "oic.d.smartplug" //OSRAM SMART+ Plug
		fingerprint profileId: "0104", inClusters: "0000, 0003, 0004, 0005, 0006, 0B05, FC01, FC08", outClusters: "0003, 0019", manufacturer: "LEDVANCE", model: "PLUG", deviceJoinName: "SYLVANIA Outlet", ocfDeviceType: "oic.d.smartplug" //SYLVANIA SMART+ Smart Plug

		// sengled
		fingerprint profileId: "0104", inClusters: "0000, 0003, 0004, 0005, 0006, 0B05", outClusters: "0019", manufacturer: "sengled", model: "E1C-NB6", deviceJoinName: "Sengled Outlet", ocfDeviceType: "oic.d.smartplug" //Sengled Element Outlet

		//Sinopé Technologies
		fingerprint manufacturer: "Sinope Technologies", model: "SP2600ZB", deviceJoinName: "Sinope Outlet", ocfDeviceType: "oic.d.smartplug"
		fingerprint manufacturer: "Sinope Technologies", model: "SP2610ZB", deviceJoinName: "Sinope Outlet", ocfDeviceType: "oic.d.smartplug"

		// SONOFF
		fingerprint profileId: "0104", inClusters: "0000, 0003, 0004, 0005, 0006", outClusters: "0000", manufacturer: "SONOFF", model: "BASICZBR3", deviceJoinName: "SONOFF Outlet", ocfDeviceType: "oic.d.smartplug" //SONOFF Basic (R3 Zigbee)
		fingerprint profileId: "0104", inClusters: "0000, 0003, 0004, 0005, 0006", outClusters: "0000", manufacturer: "SONOFF", model: "S31 Lite zb", deviceJoinName: "S31 Outlet", ocfDeviceType: "oic.d.smartplug" //S31 Lite zb
		fingerprint profileId: "0104", inClusters: "0000, 0003, 0004, 0005, 0006", outClusters: "1000", manufacturer: "SONOFF", model: "01MINIZB", deviceJoinName: "SONOFF 01MINIZB" //01MINIZB
		fingerprint profileId: "0104", inClusters: "0000, 0003, 0004, 0005, 0006, FC57", outClusters: "0019", manufacturer: "SONOFF", model: "S26R2ZB", deviceJoinName: "SONOFF Plug", ocfDeviceType: "oic.d.smartplug" //SONOFF S26R2 Plug
		fingerprint profileId: "0104", inClusters: "0000, 0003, 0004, 0005, 0006, FC57", outClusters: "0019", manufacturer: "SONOFF", model: "S40LITE", deviceJoinName: "SONOFF Plug", ocfDeviceType: "oic.d.smartplug" //SONOFF S40Lite Plug
		fingerprint profileId: "0104", inClusters: "0000, 0003, 0004, 0005, 0006, 0020, FC57", outClusters: "0019", manufacturer: "SONOFF", model: "ZBMINI-L", deviceJoinName: "SONOFF Switch" //SONOFF ZBMINI-L

		// Terncy
		fingerprint profileId: "0104", inClusters: "0000, 0003, 0006", outClusters: "0019", manufacturer: "", model: "TERNCY-LS01", deviceJoinName: "Terncy Switch" //Terncy Smart Light Socket

		// Third Reality
		fingerprint profileId: "0104", inClusters: "0000, 0006", outClusters: "0006, 0019", manufacturer: "Third Reality, Inc", model: "3RSS009Z", deviceJoinName: "ThirdReality Switch" //RealitySwitch-Gen3 Zigbee Mode
		fingerprint profileId: "0104", inClusters: "0000, 0003, 0004, 0005, 0006, 0019", manufacturer: "Third Reality, Inc", model: "3RSS008Z", deviceJoinName: "ThirdReality Switch" //RealitySwitch Plus
		fingerprint profileId: "0104", inClusters: "0000, 0003, 0004, 0005, 0006, 0019", manufacturer: "Third Reality, Inc", model: "3RSS007Z", deviceJoinName: "ThirdReality Switch" //RealitySwitch
		fingerprint profileId: "0104", deviceId: "0051", inClusters: "0000, 0003, 0004, 0005, 0006",outClusters: "0019", manufacturer: "Third Reality, Inc", model: "3RSP019BZ", deviceJoinName: "ThirdReality Plug", ocfDeviceType: "oic.d.smartplug" //RealityPlug

		// Dawon
		fingerprint profileId: "0104", inClusters: "0000, 0004, 0003, 0006, 0019, 0002, 0009", manufacturer: "DAWON_DNS", model: "PM-S140-ZB", deviceJoinName: "Dawon Switch" //DAWOS DNS In-Wall Switch PM-S140-ZB
		fingerprint profileId: "0104", inClusters: "0000, 0004, 0003, 0006, 0019, 0002, 0009", manufacturer: "DAWON_DNS", model: "PM-S140R-ZB", deviceJoinName: "Dawon Switch" //DAWOS DNS In-Wall Switch PM-S140R-ZB
		fingerprint profileId: "0104", inClusters: "0000, 0002, 0003, 0006", manufacturer: "DAWON_DNS", model: "PM-S150-ZB", deviceJoinName: "Dawon Switch" //DAWOS DNS In-Wall Switch PM-S150-ZB
		fingerprint profileId: "0104", inClusters: "0000, 0002, 0003, 0006", manufacturer: "DAWON_DNS", model: "ST-S150-ZB", deviceJoinName: "Dawon Switch" //DAWOS DNS In-Wall Switch ST-S150-ZB

		// Enbrighten/Jasco
		fingerprint manufacturer: "Jasco Products", model: "43100", deviceJoinName: "Enbrighten Switch" //Enbrighten, Plug-in Outdoor Smart Switch, 43100, Raw Description: 01 0104 0100 00 06 0000 0003 0004 0005 0006 0B05 02 000A 0019
		fingerprint manufacturer: "Jasco Products", model: "43084", deviceJoinName: "Enbrighten Switch" //Enbrighten, In-Wall Smart Switch Toggle, 43084, Raw Description: 01 0104 0100 00 06 0000 0003 0004 0005 0006 0B05 02 000A 0019
		fingerprint manufacturer: "Jasco Products", model: "43094", deviceJoinName: "Enbrighten Switch" //Enbrighten, Plug-in Smart Switch 43094, Raw Description: 01 0104 0100 00 06 0000 0003 0004 0005 0006 0B05 02 000A 0019
		fingerprint manufacturer: "Jasco Products", model: "43102", deviceJoinName: "Enbrighten Outlet", ocfDeviceType: "oic.d.smartplug" //Enbrighten, In-Wall Smart Outlet 43102, Raw Description: 01 0104 0100 00 06 0000 0003 0004 0005 0006 0B05 02 000A 0019
		fingerprint manufacturer: "Jasco Products", model: "43076", deviceJoinName: "Enbrighten Switch" //Enbrighten, In-Wall Smart Switch 43076, Raw Description: 01 0104 0100 00 06 0000 0003 0004 0005 0006 0B05 02 000A 0019
		
		// Focalcrest/Evvr
		fingerprint profileId: "0104", inClusters: "0000, 0003, 0004, 0005, 0006, 0019", outClusters: "0019", manufacturer: "Focalcrest", model: "SRB01", deviceJoinName: "Focalcrest Switch" // In-Wall Relay Switch
		fingerprint profileId: "0104", inClusters: "0000, 0003, 0004, 0005, 0006", outClusters: "0019", manufacturer: "EVVR", model: "SRB01A", deviceJoinName: "Evvr Switch" // Evvr IRS
		fingerprint profileId: "0104", inClusters: "0000, 0003, 0004, 0005, 0006", outClusters: "0019", manufacturer: "EVVR", model: "SRB02A", deviceJoinName: "Evvr Switch" // Evvr IRS Lite
		
		// Evvr
		fingerprint profileId: "0104", inClusters: "0000, 0003, 0004, 0005, 0006, 0019", outClusters: "0019", manufacturer: "Evvr", model: "SRB01", deviceJoinName: "Evvr Switch" // Evvr IRS
		fingerprint profileId: "0104", inClusters: "0000, 0003, 0004, 0005, 0006", outClusters: "0019", manufacturer: "Evvr", model: "SRB01A", deviceJoinName: "Evvr Switch" // Evvr IRS
		fingerprint profileId: "0104", inClusters: "0000, 0003, 0004, 0005, 0006", outClusters: "0019", manufacturer: "Evvr", model: "SRB02A", deviceJoinName: "Evvr Switch" // Evvr IRS Lite
        
		// SiHAS Switch
		fingerprint inClusters: "0000, 0003, 0006, 0019, ", outClusters: "0003,0004,0019", manufacturer: "ShinaSystem", model: "SBM300Z1", deviceJoinName: "SiHAS Switch"
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
