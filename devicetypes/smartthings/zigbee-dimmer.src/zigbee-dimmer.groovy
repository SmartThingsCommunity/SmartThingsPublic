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
	definition (name: "ZigBee Dimmer", namespace: "smartthings", author: "SmartThings", ocfDeviceType: "oic.d.light", runLocally: true, minHubCoreVersion: '000.019.00012', executeCommandsLocally: true, genericHandler: "Zigbee") {
		capability "Actuator"
		capability "Configuration"
		capability "Refresh"
		capability "Switch"
		capability "Switch Level"
		capability "Health Check"
		capability "Light"

		// Generic
		fingerprint profileId: "0104", deviceId: "0101", inClusters: "0006, 0008", deviceJoinName: "Light" //Generic Dimmable Light

		// AduroSmart
		fingerprint profileId: "0104", inClusters: "0000, 0003, 0004, 0005, 0006, 0008, 1000", outClusters: "0019", deviceId: "0101", manufacturer: "AduroSmart Eria", model: "AD-DimmableLight3001", deviceJoinName: "Eria Light" //Eria ZigBee Dimmable Bulb

		// Aurora
		fingerprint profileId: "0104", inClusters: "0000, 0003, 0004, 0005, 0006, 0008", outClusters: "0019", manufacturer: "Aurora", model: "LCBulb01UK", deviceJoinName: "AOne Dimmer Switch", ocfDeviceType: "oic.d.switch" //Aurora AOne Control Dimmer (120w)
		fingerprint profileId: "0104", inClusters: "0000, 0003, 0004, 0005, 0006, 0008, 0300", outClusters: "0003", manufacturer: "Aurora", model: "Dimmer", deviceJoinName: "AOne Dimmer Switch", ocfDeviceType: "oic.d.switch" //Aurora AOne Control Dimmer (320w)
		fingerprint profileId: "0104", inClusters: "0000, 0003, 0004, 0005, 0006, 0008, 0009", outClusters: "0019", manufacturer: "Aurora", model: "FWMPROZXBulb50AU", deviceJoinName: "Aurora Light" //Aurora MPro
		fingerprint profileId: "0104", inClusters: "0000, 0004, 0003, 0006, 0008, 0005, FFFF, 1000", outClusters: "0019", manufacturer: "Aurora", model: "FWBulb51AU", deviceJoinName: "Aurora Light" //Aurora Smart Dimmable
		fingerprint profileId: "0104", inClusters: "0000, 0003, 0004, 0005, 0006, 0008", outClusters: "0019", manufacturer: "Aurora", model: "FWStrip50AU", deviceJoinName: "Aurora Light" //Aurora Dimmable Strip Controller
		fingerprint profileId: "0104", inClusters: "0000, 0003, 0004, 0005, 0006, 0008, 0B05, 1000, FEDC", outClusters: "000A, 0019", manufacturer: "Aurora", model: "FWGU10Bulb50AU", deviceJoinName: "Aurora Light" //Aurora Smart Dimmable GU10
		fingerprint profileId: "0104", inClusters: "0000, 0003, 0004, 0005, 0006, 0008", outClusters: "0019", manufacturer: "Aurora", model: "NPD3032", deviceJoinName: "Aurora Dimmer Switch", ocfDeviceType: "oic.d.switch" //Aurora In-line Dimmer
		fingerprint profileId: "0104", inClusters: "0000, 0003, 0004, 0005, 0006, 0008", outClusters: "0019", manufacturer: "Aurora", model: "WallDimmerMaster", deviceJoinName: "Aurora Dimmer Switch", ocfDeviceType: "oic.d.switch" //Aurora Smart Rotary Dimmer
		fingerprint profileId: "0104", inClusters: "0000, 0003, 0004, 0005, 0006, 0008, 0B05 ,1000, FEDC", outClusters: "000A, 0019", manufacturer: "Aurora", model: "FWST64Bulb50AU", deviceJoinName: "Aurora Light" //Aurora Dimmable Filament Vintage ST64
		fingerprint profileId: "0104", inClusters: "0000, 0003, 0004, 0005, 0006, 0008, 0B05 ,1000, FEDC", outClusters: "000A, 0019", manufacturer: "Aurora", model: "FWG125Bulb50AU", deviceJoinName: "Aurora Light" //Aurora Dimmable Filament Vintage G125
		fingerprint profileId: "0104", inClusters: "0000, 0003, 0004, 0005, 0006, 0008, 0B05 ,1000, FEDC", outClusters: "000A, 0019", manufacturer: "Aurora", model: "FWA60Bulb50AU", deviceJoinName: "Aurora Light" //Aurora Dimmable Filament Vintage GLS

		//CWD
		// raw description "01 0104 0101 01 09 0000 0003 0004 0005 0006 0008 0B05 1000 FC82 02 000A 0019"
		fingerprint manufacturer: "CWD", model: "ZB.A806Edim-A001", deviceJoinName: "CWD Light" //model: "E27 dim", brand: "Collingwood"
		// raw description "01 0104 0101 01 09 0000 0003 0004 0005 0006 0008 0B05 1000 FC82 02 000A 0019"
		fingerprint manufacturer: "CWD", model: "ZB.A806Bdim-A001", deviceJoinName: "CWD Light" //model: "BC dim", brand: "Collingwood"
		// raw description "01 0104 0101 01 09 0000 0003 0004 0005 0006 0008 0B05 1000 FC82 02 000A 0019"
		fingerprint manufacturer: "CWD", model: "ZB.M350dim-A001", deviceJoinName: "CWD Light" //model: "GU10 dim", brand: "Collingwood"

		// IKEA
		fingerprint manufacturer: "IKEA of Sweden", model: "TRADFRI bulb E27 WW 806lm", deviceJoinName: "IKEA Light" // raw description 01 0104 0101 01 07 0000 0003 0004 0005 0006 0008 1000 04 0005 0019 0020 1000 //IKEA TRÅDFRI LED Bulb
		fingerprint manufacturer: "IKEA of Sweden", model: "TRADFRI bulb E27 WW clear 250lm", deviceJoinName: "IKEA Light" //raw desc: 01 0104 0101 01 07 0000 0003 0004 0005 0006 0008 1000 04 0005 0019 0020 1000 //IKEA TRÅDFRI LED Bulb

		// INGENIUM
		fingerprint profileId: "0104", inClusters: "0000, 0003, 0004, 0005, 0006, 0008, 0301, FC01", manufacturer: "ubisys", model: "D1 (5503)", deviceJoinName: "INGENIUM Light" //INGENIUM ZB Universal Dimming Module ZBM01d
		fingerprint profileId: "0104", inClusters: "0000, 0003, 0004, 0005, 0006, 0008, 1000", outClusters: "0019", manufacturer: "Megaman", model: "AD-DimmableLight3001", deviceJoinName: "INGENIUM Light" //INGENIUM ZB LED Classic

		// Innr
		fingerprint profileId: "0104", inClusters: "0000, 0003, 0004, 0005, 0006, 0008, 0B05, 1000, FC82", outClusters: "0019", manufacturer: "innr", model: "RF 263", deviceJoinName: "Innr Light" //Innr Smart Filament Bulb Vintage
		fingerprint profileId: "0104", inClusters: "0000, 0003, 0004, 0005, 0006, 0008, 0B05, 1000, FC82", outClusters: "0019", manufacturer: "innr", model: "BF 263", deviceJoinName: "Innr Light" //Innr Smart Filament Bulb Vintage
		fingerprint profileId: "0104", inClusters: "0000, 0003, 0004, 0005, 0006, 0008, 0B05, 1000, FC82", outClusters: "0019", manufacturer: "innr", model: "BF 265", deviceJoinName: "Innr Light" //Innr Smart Filament Bulb
		fingerprint profileId: "0104", inClusters: "0000, 0003, 0004, 0005, 0006, 0008, 0B05, 1000", outClusters: "0019", manufacturer: "innr", model: "AE 260", deviceJoinName: "Innr Light" //Innr Smart Bulb
		fingerprint profileId: "0104", inClusters: "0000, 0003, 0004, 0005, 0006, 0008, 0B05, 1000", outClusters: "0019", manufacturer: "innr", model: "BE 220", deviceJoinName: "Innr Light" //Innr Smart Flood Light White
		fingerprint manufacturer: "innr", model: "RF 265", deviceJoinName: "Innr Light" // raw description: 01 0104 0101 01 09 0000 0003 0004 0005 0006 0008 0B05 1000 FC82 01 0019 //Innr Smart Filament Bulb White
		fingerprint profileId: "0104", inClusters: "0000, 0003, 0004, 0005, 0006, 0008, 1000", outClusters: "0019", manufacturer: "innr", model: "RB 265", deviceJoinName: "Innr Light" //Innr Smart Bulb White
		fingerprint profileId: "0104", inClusters: "0000, 0003, 0004, 0005, 0006, 0008, 1000", outClusters: "0019", manufacturer: "innr", model: "RB 245", deviceJoinName: "Innr Light" //Innr Smart Candle White
		fingerprint profileId: "0104", inClusters: "0000, 0003, 0004, 0005, 0006, 0008, 1000", outClusters: "0019", manufacturer: "innr", model: "RS 225", deviceJoinName: "Innr Light" //Innr Smart Spot White
		fingerprint manufacturer: "innr", model: "RF 261", deviceJoinName: "Innr Light" // Innr Smart filament globe vintage E27 (RF 261)  profileId: "0104", inClusters: "0000, 0003, 0004, 0005, 0006, 0008, 0B05, 1000, FC82", outClusters: "0019" //Light
		fingerprint manufacturer: "innr", model: "RF 264", deviceJoinName: "Innr Light" // Innr Smart filament edison vintage E27 (RF 264) profileId: "0104", inClusters: "0000, 0003, 0004, 0005, 0006, 0008, 0B05, 1000, FC82", outClusters: "0019" //Light

		// Leedarson
		fingerprint profileId: "0104", inClusters: "0000, 0003, 0004, 0005, 0006, 0008, 0B05, 1000, FEDC", outClusters: "000A, 0019", manufacturer: "Smarthome", model: "S111-201A", deviceJoinName: "Leedarson Light" //Leedarson Dimmable White Bulb A19
		fingerprint profileId: "0104", inClusters: "0000, 0004, 0003, 0006, 0008, 0005, FFFF, 1000", outClusters: "0019", manufacturer: "LDS", model: "ZBT-DIMLight-GLS0000", deviceJoinName: "Light" //Smart Bulb
		fingerprint profileId: "0104", inClusters: "0000, 0003, 0004, 0005, 0006, 0008", outClusters: "0019", manufacturer: "LDS", model: "ZHA-DIMLight-GLS0000", deviceJoinName: "Light" //Smart Bulb
		fingerprint profileId: "0104", inClusters: "0000, 0003, 0004, 0005, 0006, 0008, 1000", outClusters: "0019", manufacturer: "LDS", model: "ZBT-DIMLight-GLS", deviceJoinName: "Light" //Smart Bulb
		fingerprint profileId: "0104", inClusters: "0000, 0003, 0004, 0005, 0006, 0008, 1000", outClusters: "0019", manufacturer: "LDS", model: "ZBT-DIMLight-GLS0044", deviceJoinName: "Light" //Smart Bulb

		// Leviton
		fingerprint profileId: "0104", inClusters: "0000, 0003, 0004, 0005, 0006, 0008", outClusters: "0003, 0006, 0008, 0019, 0406", manufacturer: "Leviton", model: "DL6HD", deviceJoinName: "Leviton Dimmer Switch", ocfDeviceType: "oic.d.switch" //Leviton Dimmer Switch
		fingerprint profileId: "0104", inClusters: "0000, 0003, 0004, 0005, 0006, 0008", outClusters: "0003, 0006, 0008, 0019, 0406", manufacturer: "Leviton", model: "DL3HL", deviceJoinName: "Leviton Dimmer Switch", ocfDeviceType: "oic.d.switch" //Leviton Lumina RF Plug-In Dimmer
		fingerprint profileId: "0104", inClusters: "0000, 0003, 0004, 0005, 0006, 0008", outClusters: "0003, 0006, 0008, 0019, 0406", manufacturer: "Leviton", model: "DL1KD", deviceJoinName: "Leviton Dimmer Switch", ocfDeviceType: "oic.d.switch" //Leviton Lumina RF Dimmer Switch
		fingerprint profileId: "0104", inClusters: "0000, 0003, 0004, 0005, 0006, 0008", outClusters: "0003, 0006, 0008, 0019, 0406", manufacturer: "Leviton", model: "ZSD07", deviceJoinName: "Leviton Dimmer Switch", ocfDeviceType: "oic.d.switch" //Leviton Lumina RF 0-10V Dimming Wall Switch

		// LINKIND
		fingerprint profileId: "0104", inClusters: "0000, 0003, 0004, 0005, 0006, 0008, 0B05, 1000, FC82", outClusters: "000A, 0019", manufacturer: "lk", model: "ZBT-DIMLight-GLS0010", deviceJoinName: "Linkind Light" //Linkind Dimmable A19 Bulb

		// Müller Licht
		fingerprint profileId: "0104", inClusters: "0000, 0003, 0004, 0005, 0006, 0008, 1000", outClusters: "0019", manufacturer: "MLI", model: "ZBT-DimmableLight", deviceJoinName: "Tint Light" //Müller Licht Tint Bulb Dimming

		// OSRAM/Sylvania
		fingerprint profileId: "0104", inClusters: "0000, 0003, 0004, 0005, 0006, 0008, 0B04, FC0F", outClusters: "0019", manufacturer: "OSRAM", model: "LIGHTIFY A19 ON/OFF/DIM", deviceJoinName: "SYLVANIA Light" //SYLVANIA Smart A19 Soft White
		fingerprint profileId: "0104", inClusters: "0000, 0003, 0004, 0005, 0006, 0008, FC0F", outClusters: "0019", manufacturer: "OSRAM", model: "LIGHTIFY A19 ON/OFF/DIM 10 Year", deviceJoinName: "SYLVANIA Light" //SYLVANIA Smart 10-Year A19
		fingerprint profileId: "0104", inClusters: "0000, 0003, 0004, 0005, 0006, 0008, 0B05", outClusters: "0019", manufacturer: "OSRAM SYLVANIA", model: "iQBR30", deviceJoinName: "SYLVANIA Light" //Sylvania Ultra iQ
		fingerprint profileId: "0104", inClusters: "0000, 0003, 0004, 0005, 0006, 0008, FC0F", outClusters: "0019", manufacturer: "OSRAM", model: "LIGHTIFY PAR38 ON/OFF/DIM", deviceJoinName: "SYLVANIA Light" //SYLVANIA Smart PAR38 Soft White
		fingerprint profileId: "0104", inClusters: "0000, 0003, 0004, 0005, 0006, 0008, 0B04, FC0F", outClusters: "0019", manufacturer: "OSRAM", model: "LIGHTIFY BR ON/OFF/DIM", deviceJoinName: "SYLVANIA Light" //SYLVANIA Smart BR30 Soft White
		fingerprint profileId: "0104", inClusters: "0000, 0003, 0004, 0005, 0006, 0008, 0300, 0B04, 0B05, FC01, FC08", outClusters: "0003, 0019", manufacturer: "LEDVANCE", model: "A19 W 10 year", deviceJoinName: "SYLVANIA Light" //SYLVANIA Smart 10Y A19 Soft White
		fingerprint profileId: "0104", inClusters: "0000, 0003, 0004, 0005, 0006, 0008, 0300, 0B05, FC01", outClusters: "0003, 0019", manufacturer: "LEDVANCE", model: "BR30 W 10 year", deviceJoinName: "SYLVANIA Light" //SYLVANIA Smart 10Y BR30 Soft White
		fingerprint profileId: "0104", inClusters: "0000, 0003, 0004, 0005, 0006, 0008, 0300, 0B05, FC01", outClusters: "0003, 0019", manufacturer: "LEDVANCE", model: "PAR38 W 10 year", deviceJoinName: "SYLVANIA Light" //SYLVANIA Smart 10Y PAR38 Soft White

		// Ozom
		fingerprint profileId: "0104", inClusters: "0000, 0003, 0004, 0005, 0006, 0008", outClusters: "0019", manufacturer: "LEEDARSON LIGHTING", model: "M350ST-W1R-01", deviceJoinName: "OZOM Light" //OZOM Dimmable LED Smart Light

		// Sengled
		fingerprint profileId: "0104", inClusters: "0000, 0003, 0004, 0005, 0006, 0008, 0702, 0B05", outClusters: "0019", manufacturer: "sengled", model: "E11-G13", deviceJoinName: "Sengled Light" //Sengled Element Classic
		fingerprint profileId: "0104", inClusters: "0000, 0003, 0004, 0005, 0006, 0008, 0702, 0B05", outClusters: "0019", manufacturer: "sengled", model: "E11-G14", deviceJoinName: "Sengled Light" //Sengled Element Classic
		fingerprint profileId: "0104", inClusters: "0000, 0003, 0004, 0005, 0006, 0008, 0702, 0B05", outClusters: "0019", manufacturer: "sengled", model: "E11-G23", deviceJoinName: "Sengled Light" //Sengled Element Classic
		fingerprint profileId: "0104", inClusters: "0000, 0003, 0004, 0005, 0006, 0008, 0702, 0B05", outClusters: "0019", manufacturer: "sengled", model: "E11-G33", deviceJoinName: "Sengled Light" //Sengled Element Classic
		fingerprint profileId: "0104", inClusters: "0000, 0003, 0004, 0005, 0006, 0008, 0702, 0B05", outClusters: "0019", manufacturer: "sengled", model: "E12-N13", deviceJoinName: "Sengled Light" //Sengled Element Classic
		fingerprint profileId: "0104", inClusters: "0000, 0003, 0004, 0005, 0006, 0008, 0702, 0B05", outClusters: "0019", manufacturer: "sengled", model: "E12-N14", deviceJoinName: "Sengled Light" //Sengled Element Classic
		fingerprint profileId: "0104", inClusters: "0000, 0003, 0004, 0005, 0006, 0008, 0702, 0B05", outClusters: "0019", manufacturer: "sengled", model: "E12-N15", deviceJoinName: "Sengled Light" //Sengled Element Classic
		fingerprint profileId: "0104", inClusters: "0000, 0003, 0004, 0005, 0006, 0008, 0702, 0B05", outClusters: "0019", manufacturer: "sengled", model: "E11-N13", deviceJoinName: "Sengled Light" //Sengled Element Classic
		fingerprint profileId: "0104", inClusters: "0000, 0003, 0004, 0005, 0006, 0008, 0702, 0B05", outClusters: "0019", manufacturer: "sengled", model: "E11-N14", deviceJoinName: "Sengled Light" //Sengled Element Classic
		fingerprint profileId: "0104", inClusters: "0000, 0003, 0004, 0005, 0006, 0008, 0300, 0702, 0B05", outClusters: "0019", manufacturer: "sengled", model: "E1A-AC2", deviceJoinName: "Sengled Light" //Sengled DownLight
		fingerprint profileId: "0104", inClusters: "0000, 0003, 0004, 0005, 0006, 0008, 0702, 0B05", outClusters: "0019", manufacturer: "sengled", model: "E11-N13A", deviceJoinName: "Sengled Light" //Sengled Extra Bright Soft White
		fingerprint profileId: "0104", inClusters: "0000, 0003, 0004, 0005, 0006, 0008, 0702, 0B05", outClusters: "0019", manufacturer: "sengled", model: "E11-N14A", deviceJoinName: "Sengled Light" //Sengled Extra Bright Daylight
		fingerprint profileId: "0104", inClusters: "0000, 0003, 0004, 0005, 0006, 0008, 0702, 0B05", outClusters: "0019", manufacturer: "sengled", model: "E21-N13A", deviceJoinName: "Sengled Light" //Sengled Soft White
		fingerprint profileId: "0104", inClusters: "0000, 0003, 0004, 0005, 0006, 0008, 0702, 0B05", outClusters: "0019", manufacturer: "sengled", model: "E21-N14A", deviceJoinName: "Sengled Light" //Sengled Daylight
		fingerprint profileId: "0104", inClusters: "0000, 0003, 0004, 0005, 0006, 0008, 0702, 0B05", outClusters: "0019", manufacturer: "sengled", model: "E11-U21U31", deviceJoinName: "Sengled Light" //Sengled Element Touch
		fingerprint profileId: "0104", inClusters: "0000, 0003, 0004, 0005, 0006, 0008, 0702, 0B05, FC01", outClusters: "0019", manufacturer: "sengled", model: "E13-A21", deviceJoinName: "Sengled Light" //Sengled LED Flood Light
		fingerprint profileId: "0104", inClusters: "0000, 0003, 0004, 0005, 0006, 0008, 0702, 0B05", outClusters: "0019", manufacturer: "sengled", model: "E11-N1G", deviceJoinName: "Sengled Light" //Sengled Smart LED Vintage Edison Bulb

		// Sinope Technologies
		fingerprint manufacturer: "Sinope Technologies", model: "DM2550ZB", deviceJoinName: "Sinope Dimmer Switch DM2550ZB" //DM2550ZB

		// SmartThings
		fingerprint profileId: "0104", inClusters: "0000, 0003, 0004, 0005, 0006, 0008, 0B05, 1000, FEDC", outClusters: "000A, 0019", manufacturer: "LDS", model: "ZBT-DIMLight-GLS0006", deviceJoinName: "Light" //Smart Bulb

		// Wemo
		fingerprint profileId: "0104", inClusters: "0000, 0003, 0004, 0005, 0006, 0008, FF00", outClusters: "0019", manufacturer: "MRVL", model: "MZ100", deviceJoinName: "Wemo Light" //Wemo Bulb

		// Enbrighten/Jasco
		fingerprint manufacturer: "Jasco Products", model: "43096", deviceJoinName: "Enbrighten Dimmer", ocfDeviceType: "oic.d.switch" //Enbrighten, Plug-in Smart Dimmer, 43096, Raw Description: 01 0104 0101 00 07 0000 0003 0004 0005 0006 0008 0B05 02 000A 0019
		fingerprint manufacturer: "Jasco Products", model: "43090", deviceJoinName: "Enbrighten Dimmer", ocfDeviceType: "oic.d.switch" //Enbrighten, In-Wall Smart Dimmer, Toggle. 43090, Raw Description: 01 0104 0101 00 07 0000 0003 0004 0005 0006 0008 0B05 02 000A 0019
		fingerprint manufacturer: "Jasco Products", model: "43080", deviceJoinName: "Enbrighten Dimmer", ocfDeviceType: "oic.d.switch" //Enbrighten, In-Wall Smart Dimmer, 43080, Raw Description: 01 0104 0101 00 07 0000 0003 0004 0005 0006 0008 0B05 02 000A 0019
	}

	tiles(scale: 2) {
		multiAttributeTile(name:"switch", type: "lighting", width: 6, height: 4, canChangeIcon: true){
			tileAttribute ("device.switch", key: "PRIMARY_CONTROL") {
				attributeState "on", label:'${name}', action:"switch.off", icon:"st.switches.light.on", backgroundColor:"#00A0DC", nextState:"turningOff"
				attributeState "off", label:'${name}', action:"switch.on", icon:"st.switches.light.off", backgroundColor:"#ffffff", nextState:"turningOn"
				attributeState "turningOn", label:'${name}', action:"switch.off", icon:"st.switches.light.on", backgroundColor:"#00A0DC", nextState:"turningOff"
				attributeState "turningOff", label:'${name}', action:"switch.on", icon:"st.switches.light.off", backgroundColor:"#ffffff", nextState:"turningOn"
			}
			tileAttribute ("device.level", key: "SLIDER_CONTROL") {
				attributeState "level", action:"switch level.setLevel"
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
		if (event.name=="level" && event.value==0) {}
		else {
			sendEvent(event)
		}
	} else {
		def descMap = zigbee.parseDescriptionAsMap(description)
		if (descMap && descMap.clusterInt == 0x0006 && descMap.commandInt == 0x07) {
			if (descMap.data[0] == "00") {
				log.debug "ON/OFF REPORTING CONFIG RESPONSE: " + cluster
				sendEvent(name: "checkInterval", value: 60 * 12, displayed: false, data: [protocol: "zigbee", hubHardwareId: device.hub.hardwareID])
			} else {
				log.warn "ON/OFF REPORTING CONFIG FAILED- error code:${cluster.data[0]}"
			}
		} else if (isSengled() && descMap && descMap.clusterInt == 0x0008 && descMap.attrInt == 0x0000) {
			// This is being done because the sengled element touch/classic incorrectly uses the value 0xFF for the max level.
			// Per the ZCL spec for the UINT8 data type 0xFF is an invalid value, and 0xFE should be the max.  Here we
			// manually handle the invalid attribute value since it will be ignored by getEvent as an invalid value.
			// We also set the level of the bulb to 0xFE so future level reports will be 0xFE until it is changed by
			// something else.
			if (descMap.value.toUpperCase() == "FF") {
				descMap.value = "FE"
			}
			sendHubCommand(zigbee.command(zigbee.LEVEL_CONTROL_CLUSTER, 0x00, "FE0000").collect { new physicalgraph.device.HubAction(it) }, 0)
			sendEvent(zigbee.getEventFromAttrData(descMap.clusterInt, descMap.attrInt, descMap.encoding, descMap.value))
		} else if (isSinope() && description?.startsWith("read attr -")) {
            def result = []
            result += createCustomMap(descMap)

            // In the possibility of multiple attributes being reported in the same message, all the attributes will be in the same description. 
			// the first attribute will be in the fields regularly used, but the otter attributes will be in the "additionalAttrs". 
			// They should all be treated in the following part.
            if(descMap.additionalAttrs)
            {
                def mapAdditionnalAttrs = descMap.additionalAttrs
                mapAdditionnalAttrs.each{add ->
                    traceEvent(settings.logFilter,"parse> mapAdditionnalAttributes : ( ${add} )",settings.trace)
                    add.cluster = descMap.cluster
                    result += createCustomMap(add)
                }
            }
		} else {
			log.warn "DID NOT PARSE MESSAGE for description : $description"
			log.debug "${descMap}"
		}
	}
}

private def parseDescriptionAsMap(description) {
    traceEvent(settings.logFilter, "parsing MAP ...", settings.trace, get_LOG_DEBUG())
	(description - "read attr - ").split(",").inject([:]) 
    {
    	map, param ->
		def nameAndValue = param.split(":")
		map += [(nameAndValue[0].trim()):nameAndValue[1].trim()]
	}
}

private def createCustomMap(descMap) {
    def result = null
	def map = [:]

        if(descMap.cluster == "0000" && descMap.attrId == "0001")
        {
            traceEvent(settings.logFilter, "Parsing SwBuild Attribute", settings.trace, get_LOG_DEBUG())
            map.name = "swBuild"
            map.value = zigbee.convertHexToInt(descMap.value)
            sendEvent(name: map.name, value: map.value)
        }
    return result
}

def updated() {
    
	if (!state.updatedLastRanAt || now() >= state.updatedLastRanAt + 2000)
    {
		state.updatedLastRanAt = now()   
            
        def cmds = []
        if(checkSoftVersion() == true)
        {
            def MinLight = (MinimalIntensityParam)?MinimalIntensityParam.toInteger():0
            def Time = getTiming(MinLight)
            traceEvent(settings.logFilter, "Set timing to: $Time", settings.trace, get_LOG_DEBUG())
            cmds += zigbee.writeAttribute(0xff01, 0x0055, 0x21, Time)
                
        }
        else
        {
            traceEvent(settings.logFilter, "Minimal intensity is not supported by the device", settings.trace, get_LOG_DEBUG())
        }

        if(LedIntensityParam){
            cmds += zigbee.writeAttribute(0xff01, 0x0052, 0x20, LedIntensityParam)//MaxIntensity On
            cmds += zigbee.writeAttribute(0xff01, 0x0053, 0x20, LedIntensityParam)//MaxIntensity Off
        }
        else{ // set to default
            cmds += zigbee.writeAttribute(0xff01, 0x0052, 0x20, 50)//MaxIntensity On
            cmds += zigbee.writeAttribute(0xff01, 0x0053, 0x20, 50)//MaxIntensity Off
        }
        sendZigbeeCommands(cmds)

	}
	else {
        traceEvent(settings.logFilter, "updated(): Ran within last 2 seconds so aborting", settings.trace, get_LOG_TRACE())
	}
}

def off() {
	zigbee.off()
}

def on() {
	zigbee.on()
}

def setLevel(value, rate = null) {
	def additionalCmds = []
	if (device.getDataValue("model") == "iQBR30" && value.toInteger() > 0) { // Handle iQ bulb not following spec
		additionalCmds = zigbee.on()
	} else if (isMRVL()) { // Handle marvel stack not reporting
		additionalCmds = refresh()
	} else if (isLeviton()) {
		additionalCmds = zigbee.levelRefresh()
	}
	zigbee.setLevel(value) + additionalCmds
}
/**
 * PING is used by Device-Watch in attempt to reach the Device
 * */
def ping() {
	return zigbee.onOffRefresh()
}

def refresh() {
	if (isSinope()) {
		def cmds = []
		cmds += zigbee.readAttribute(0x0006, 0x0000) //read on/off
		cmds += zigbee.readAttribute(0x0008, 0x0000) //read level
		cmds += zigbee.readAttribute(0x0000, 0x0001) //read software version
		cmds += zigbee.configureReporting(0x0006, 0x0000, 0x10, 0, 599, null) //configure reporting on/off
		cmds += zigbee.configureReporting(0x0008, 0x0000, 0x20, 3, 602, 0x01) //configure reporting level
		if(checkSoftVersion() == true) {//if the minimal intensity is supported
			cmds += zigbee.writeAttribute(0xff01, 0x0055, 0x21, getTiming((MinimalIntensityParam)?MinimalIntensityParam.toInteger():0))
		}
    	return sendZigbeeCommands(cmds)
	} else {
		zigbee.onOffRefresh() + zigbee.levelRefresh()
	}
}

def installed() {
	if ((isMRVL() && (device.getDataValue("model") == "MZ100")) || isOsram() || isOsramSylvania()) {
		if ((device.currentState("level")?.value == null) || (device.currentState("level")?.value == 0)) {
			sendEvent(name: "level", value: 100)
		}
	} else if (isSinope()) {
		def cmds = []
    	cmds += zigbee.readAttribute(0x0006, 0x0000) //read on/off
    	cmds += zigbee.readAttribute(0x0008, 0x0000) //read level
    	cmds += zigbee.readAttribute(0x0000, 0x0001) //read software version
    	cmds += zigbee.configureReporting(0x0006, 0x0000, 0x10, 0, 599, null) //configure reporting on/off
    	cmds += zigbee.configureReporting(0x0008, 0x0000, 0x20, 3, 602, 0x01) //configure reporting level
    	if(checkSoftVersion() == true){//if the minimal intensity is supported
    		cmds += zigbee.writeAttribute(0xff01, 0x0055, 0x21, getTiming((MinimalIntensityParam)?MinimalIntensityParam.toInteger():0))
    	}
    	return sendZigbeeCommands(cmds)
	}
}

def isLeviton() {
	device.getDataValue("manufacturer") == "Leviton"
}

def isMRVL() {
	device.getDataValue("manufacturer") == "MRVL"
}

def isOsram() {
	device.getDataValue("manufacturer") == "OSRAM"
}

def isOsramSylvania() {
	device.getDataValue("manufacturer") == "OSRAM SYLVANIA"
}

def isSengled() {
	device.getDataValue("manufacturer") == "sengled"
}

def isSinope() {
	device.getDataValue("manufacturer") == "Sinope Technologies"
}

def configure() {
	if (isSinope) {
		traceEvent(settings.logFilter, "Configuring Reporting and Bindings", settings.trace, get_LOG_DEBUG())

		//allow 30 minutes without reveiving any on/off report
		sendEvent(name: "checkInterval", value: 2 * 10 * 60 + 1 * 60, displayed: false, data: [protocol: "zigbee", hubHardwareId: device.hub.hardwareID])

		return  zigbee.configureReporting(0x0006, 0x0000, 0x10, 0, 599, null) + //configure reporting on/off
				zigbee.configureReporting(0x0008, 0x0000, 0x20, 3, 602, 0x01) + //configure reporting level
				zigbee.readAttribute(0x0006, 0x0000) + //read on/off
				zigbee.readAttribute(0x0008, 0x0000) + //read level
				zigbee.readAttribute(0x0000, 0x0001) //read software version
	} else {
		log.debug "Configuring Reporting and Bindings."
		// Device-Watch allows 2 check-in misses from device + ping (plus 1 min lag time)
		// enrolls with default periodic reporting until newer 5 min interval is confirmed
		sendEvent(name: "checkInterval", value: 2 * 10 * 60 + 1 * 60, displayed: false, data: [protocol: "zigbee", hubHardwareId: device.hub.hardwareID])

		// OnOff minReportTime 0 seconds, maxReportTime 5 min. Reporting interval if no activity
		refresh() + zigbee.onOffConfig(0, 300) + zigbee.levelConfig()
	}
}

private int getTiming(def setting) {
	//getTiming is used to get the minimal time associated with the parameter "minimalIntensityParam"
	def Timing
    	switch(setting)
    	{
    	case(1):
       		Timing = 100
       		break;
    	case(2):
       		Timing = 250
    		break;    
    	case(3):
       		Timing = 500
    		break;
    	case(4):
       		Timing = 750
    		break;
    	case(5):
       		Timing = 1000
    		break;
    	case(6):
       		Timing = 1250
    		break;
    	case(7):
       		Timing = 1500
    		break;
    	case(8):
       		Timing = 1750
    		break;
    	case(9):
       		Timing = 2000
    		break;
    	case(10):
       		Timing = 2250
    		break;
    	default:
       		Timing = 600
       		break;
    	}
        return Timing
}

private boolean checkSoftVersion()
{
	def version
    def versionMin = "106" //the first version to support the minimal intensity is the version 106
    def Build = device.currentState("swBuild")?.value
    traceEvent(settings.logFilter, "soft version: $Build", settings.trace, get_LOG_DEBUG())
    
    if(Build > versionMin)//if the version is under 107, the minimal light intensity is not supported.
    {
        traceEvent(settings.logFilter, "intensity supported", settings.trace, get_LOG_DEBUG())
    	version = true
    }
    else
    {
        traceEvent(settings.logFilter, "intensity not supported", settings.trace, get_LOG_DEBUG())
        version = false
    }
    return version
}

private void sendZigbeeCommands(cmds, delay = 1000) {
	cmds.removeAll { it.startsWith("delay") }
	// convert each command into a HubAction
	cmds = cmds.collect { new physicalgraph.device.HubAction(it) }
	sendHubCommand(cmds, delay)
}

private int get_LOG_ERROR() {
	return 1
}
private int get_LOG_WARN() {
	return 2
}
private int get_LOG_INFO() {
	return 3
}
private int get_LOG_DEBUG() {
	return 4
}
private int get_LOG_TRACE() {
	return 5
}

def traceEvent(logFilter, message, displayEvent = false, traceLevel = 4, sendMessage = true) {
	int LOG_ERROR = get_LOG_ERROR()
	int LOG_WARN = get_LOG_WARN()
	int LOG_INFO = get_LOG_INFO()
	int LOG_DEBUG = get_LOG_DEBUG()
	int LOG_TRACE = get_LOG_TRACE()

	if (displayEvent || traceLevel < 4) {
		switch (traceLevel) {
			case LOG_ERROR:
				log.error "${message}"
				break
			case LOG_WARN:
				log.warn "${message}"
				break
			case LOG_INFO:
				log.info "${message}"
				break
			case LOG_TRACE:
				log.trace "${message}"
				break
			case LOG_DEBUG:
			default:
				log.debug "${message}"
				break
		}
	}
}