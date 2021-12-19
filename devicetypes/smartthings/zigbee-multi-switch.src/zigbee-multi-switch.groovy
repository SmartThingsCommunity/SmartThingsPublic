/*
 *  Copyright 2018 SmartThings
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not
 *  use this file except in compliance with the License. You may obtain a copy
 *  of the License at:
 *
 *		http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 *  WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 *  License for the specific language governing permissions and limitations
 *  under the License.
 *  Author : Fen Mei / f.mei@samsung.com
 *  Date : 2018-08-29
 */

metadata {
	definition(name: "ZigBee Multi Switch", namespace: "smartthings", author: "SmartThings", ocfDeviceType: "oic.d.switch", mnmn: "SmartThings", vid: "generic-switch") {
		capability "Actuator"
		capability "Configuration"
		capability "Refresh"
		capability "Health Check"
		capability "Switch"

		command "childOn", ["string"]
		command "childOff", ["string"]

		// EZEX
		fingerprint profileId: "0104", inClusters: "0000, 0003, 0004, 0006", outClusters: "0006, 000A, 0019", model: "E220-KR2N0Z0-HA", deviceJoinName: "eZEX Switch 1" //EZEX Switch 1
		fingerprint profileId: "0104", inClusters: "0000, 0003, 0004, 0006", outClusters: "0006, 000A, 0019", model: "E220-KR3N0Z0-HA", deviceJoinName: "eZEX Switch 1" //EZEX Switch 1
		fingerprint profileId: "0104", inClusters: "0000, 0003, 0004, 0006", outClusters: "0006, 000A, 0019", model: "E220-KR4N0Z0-HA", deviceJoinName: "eZEX Switch 1" //EZEX Switch 1
		fingerprint profileId: "0104", inClusters: "0000, 0003, 0004, 0006", outClusters: "0006, 000A, 0019", model: "E220-KR5N0Z0-HA", deviceJoinName: "eZEX Switch 1" //EZEX Switch 1
		fingerprint profileId: "0104", inClusters: "0000, 0003, 0004, 0006", outClusters: "0006, 000A, 0019", model: "E220-KR6N0Z0-HA", deviceJoinName: "eZEX Switch 1" //EZEX Switch 1

		fingerprint profileId: "0104", inClusters: "0000, 0005, 0004, 0006", outClusters: "0000", manufacturer: "ORVIBO", model: "074b3ffba5a045b7afd94c47079dd553", deviceJoinName: "Orvibo Switch 1" //Orvibo 2 Gang Switch 1
		fingerprint profileId: "0104", inClusters: "0000, 0005, 0004, 0006", outClusters: "0000", manufacturer: "ORVIBO", model: "9f76c9f31b4c4a499e3aca0977ac4494", deviceJoinName: "Orvibo Switch 1" //Orvibo 3 Gang Switch 1
		fingerprint profileId: "0104", inClusters: "0000, 0003, 0005, 0004, 0006", manufacturer: "REXENSE", model: "HY0003", deviceJoinName: "GDKES Switch 1" //GDKES 3 Gang Switch 1
		fingerprint profileId: "0104", inClusters: "0000, 0003, 0005, 0004, 0006", manufacturer: "REXENSE", model: "HY0002", deviceJoinName: "GDKES Switch 1" //GDKES 2 Gang Switch 1
		fingerprint profileId: "0104", inClusters: "0000, 0003, 0004, 0005, 0006", manufacturer: "REX", model: "HY0097", deviceJoinName: "HONYAR Switch 1" //HONYAR 3 Gang Switch 1
		fingerprint profileId: "0104", inClusters: "0000, 0003, 0004, 0005, 0006", manufacturer: "REX", model: "HY0096", deviceJoinName: "HONYAR Switch 1" //HONYAR 2 Gang Switch 1
		fingerprint profileId: "0104", inClusters: "0005, 0004, 0006", outClusters: "0003, 0019", manufacturer: "HEIMAN", model: "HS2SW3L-EFR-3.0", deviceJoinName: "HEIMAN Switch 1" //HEIMAN 3 Gang Switch 1
		fingerprint profileId: "0104", inClusters: "0005, 0004, 0006", outClusters: "0003, 0019", manufacturer: "HEIMAN", model: "HS2SW2L-EFR-3.0", deviceJoinName: "HEIMAN Switch 1" //HEIMAN 2 Gang Switch 1
		fingerprint profileId: "0104", inClusters: "0005, 0004, 0006", outClusters: "0003, 0019", manufacturer: "HEIMAN", model: "HS6SW2A-W-EF-3.0", deviceJoinName: "HEIMAN Switch 1" //HEIMAN 2 Gang Switch 1
		fingerprint profileId: "0104", inClusters: "0005, 0004, 0006", outClusters: "0003, 0019", manufacturer: "HEIMAN", model: "HS6SW3A-W-EF-3.0", deviceJoinName: "HEIMAN Switch 1" //HEIMAN 3 Gang Switch 1

		// Dawon
		fingerprint profileId: "0104", inClusters: "0000, 0002, 0004, 0003, 0006, 0009, 0019", manufacturer: "DAWON_DNS", model: "PM-S240-ZB", deviceJoinName: "Dawon Switch 1" //DAWOS DNS In-Wall Switch PM-S240-ZB
		fingerprint profileId: "0104", inClusters: "0000, 0002, 0004, 0003, 0006, 0009, 0019", manufacturer: "DAWON_DNS", model: "PM-S240R-ZB", deviceJoinName: "Dawon Switch 1" //DAWOS DNS In-Wall Switch PM-S240R-ZB
		fingerprint profileId: "0104", inClusters: "0000, 0002, 0004, 0003, 0006, 0009, 0019", manufacturer: "DAWON_DNS", model: "PM-S340-ZB", deviceJoinName: "Dawon Switch 1" //DAWOS DNS In-Wall Switch PM-S340-ZB
		fingerprint profileId: "0104", inClusters: "0000, 0002, 0004, 0003, 0006, 0009, 0019", manufacturer: "DAWON_DNS", model: "PM-S340R-ZB", deviceJoinName: "Dawon Switch 1" //DAWOS DNS In-Wall Switch PM-S340R-ZB
		fingerprint profileId: "0104", inClusters: "0000, 0002,0003, 0006", manufacturer: "DAWON_DNS", model: "PM-S250-ZB", deviceJoinName: "Dawon Switch 1" //DAWOS DNS In-Wall Switch PM-S250-ZB
		fingerprint profileId: "0104", inClusters: "0000, 0002,0003, 0006", manufacturer: "DAWON_DNS", model: "PM-S350-ZB", deviceJoinName: "Dawon Switch 1" //DAWOS DNS In-Wall Switch PM-S350-ZB
		fingerprint profileId: "0104", inClusters: "0000, 0002,0003, 0006", manufacturer: "DAWON_DNS", model: "ST-S250-ZB", deviceJoinName: "Dawon Switch 1" //DAWOS DNS In-Wall Switch ST-S250-ZB
		fingerprint profileId: "0104", inClusters: "0000, 0002,0003, 0006", manufacturer: "DAWON_DNS", model: "ST-S350-ZB", deviceJoinName: "Dawon Switch 1" //DAWOS DNS In-Wall Switch ST-S350-ZB
		
		// eWeLink
		// Raw Description 01 0104 0100 00 05 0000 0003 0004 0005 0006 01 0000
		fingerprint manufacturer: "eWeLink", model: "ZB-SW02", deviceJoinName: "eWeLink Switch 1" //eWeLink 2 Gang Switch 1
		// Raw Description 01 0104 0100 00 05 0000 0003 0004 0005 0006 01 0000
		fingerprint manufacturer: "eWeLink", model: "ZB-SW03", deviceJoinName: "eWeLink Switch 1" //eWeLink 3 Gang Switch 1
		// Raw Description 01 0104 0100 00 05 0000 0003 0004 0005 0006 01 0000
		fingerprint manufacturer: "eWeLink", model: "ZB-SW04", deviceJoinName: "eWeLink Switch 1" //eWeLink 4 Gang Switch 1
		// Raw Description 01 0104 0100 00 05 0000 0003 0004 0005 0006 01 0000
		fingerprint manufacturer: "eWeLink", model: "ZB-SW05", deviceJoinName: "eWeLink Switch 1" //eWeLink 5 Gang Switch 1
		// Raw Description 01 0104 0100 00 05 0000 0003 0004 0005 0006 01 0000
		fingerprint manufacturer: "eWeLink", model: "ZB-SW06", deviceJoinName: "eWeLink Switch 1" //eWeLink 6 Gang Switch 1

		// LELLKI
		// Raw Description 01 0104 0100 00 05 0000 0003 0004 0005 0006 01 0000
		fingerprint manufacturer: "LELLKI", model: "JZ-ZB-002", deviceJoinName: "LELLKI Switch 1" //LELLKI 2 Gang Switch 1
		// Raw Description 01 0104 0100 00 05 0000 0003 0004 0005 0006 01 0000
		fingerprint manufacturer: "LELLKI", model: "JZ-ZB-003", deviceJoinName: "LELLKI Switch 1" //LELLKI 3 Gang Switch 1
		// Raw Description 01 0104 0100 00 05 0000 0003 0004 0005 0006 01 0000
		fingerprint manufacturer: "LELLKI", model: "JZ-ZB-004", deviceJoinName: "LELLKI Switch 1" //LELLKI 4 Gang Switch 1
		// Raw Description 01 0104 0100 00 05 0000 0003 0004 0005 0006 01 0000
		fingerprint manufacturer: "LELLKI", model: "JZ-ZB-005", deviceJoinName: "LELLKI Switch 1" //LELLKI 5 Gang Switch 1
		// Raw Description 01 0104 0100 00 05 0000 0003 0004 0005 0006 01 0000
		fingerprint manufacturer: "LELLKI", model: "JZ-ZB-006", deviceJoinName: "LELLKI Switch 1" //LELLKI 6 Gang Switch 1
		// SiHAS Switch (2~6 Gang)
		fingerprint inClusters: "0000, 0003, 0006, 0019, ", outClusters: "0003,0004,0019", manufacturer: "ShinaSystem", model: "SBM300Z2", deviceJoinName: "SiHAS Switch 1"
		fingerprint inClusters: "0000, 0003, 0006, 0019, ", outClusters: "0003,0004,0019", manufacturer: "ShinaSystem", model: "SBM300Z3", deviceJoinName: "SiHAS Switch 1"
		fingerprint inClusters: "0000, 0003, 0006, 0019, ", outClusters: "0003,0004,0019", manufacturer: "ShinaSystem", model: "SBM300Z4", deviceJoinName: "SiHAS Switch 1"
		fingerprint inClusters: "0000, 0003, 0006, 0019, ", outClusters: "0003,0004,0019", manufacturer: "ShinaSystem", model: "SBM300Z5", deviceJoinName: "SiHAS Switch 1"
		fingerprint inClusters: "0000, 0003, 0006, 0019, ", outClusters: "0003,0004,0019", manufacturer: "ShinaSystem", model: "SBM300Z6", deviceJoinName: "SiHAS Switch 1"
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
		multiAttributeTile(name: "switch", type: "lighting", width: 6, height: 4, canChangeIcon: true) {
			tileAttribute("device.switch", key: "PRIMARY_CONTROL") {
				attributeState "on", label: '${name}', action: "switch.off", icon: "st.switches.light.on", backgroundColor: "#00A0DC", nextState: "turningOff"
				attributeState "off", label: '${name}', action: "switch.on", icon: "st.switches.light.off", backgroundColor: "#ffffff", nextState: "turningOn"
				attributeState "turningOn", label: '${name}', action: "switch.off", icon: "st.switches.light.on", backgroundColor: "#00A0DC", nextState: "turningOff"
				attributeState "turningOff", label: '${name}', action: "switch.on", icon: "st.switches.light.off", backgroundColor: "#ffffff", nextState: "turningOn"
			}
		}
		standardTile("refresh", "device.refresh", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
			state "default", label: "", action: "refresh.refresh", icon: "st.secondary.refresh"
		}
		main "switch"
		details(["switch", "refresh"])
	}
}

def installed() {
	createChildDevices()
	updateDataValue("onOff", "catchall")
	refresh()
}

def updated() {
	log.debug "updated()"
	updateDataValue("onOff", "catchall")
	for (child in childDevices) {
		if (!child.deviceNetworkId.startsWith(device.deviceNetworkId) || //parent DNI has changed after rejoin
				!child.deviceNetworkId.split(':')[-1].startsWith('0')) {
			child.setDeviceNetworkId("${device.deviceNetworkId}:0${getChildEndpoint(child.deviceNetworkId)}")
		}
	}
	refresh()
}

def parse(String description) {
	Map eventMap = zigbee.getEvent(description)
	Map eventDescMap = zigbee.parseDescriptionAsMap(description)

	if (eventMap) {
		if (eventDescMap && (eventDescMap?.attrId == "0000" || eventDescMap?.command == "0B")) {//0x0000 : OnOff attributeId, 0x0B : default response command
			if (eventDescMap?.sourceEndpoint == "01" || eventDescMap?.endpoint == "01") {
				sendEvent(eventMap)
			} else {
				def childDevice = childDevices.find {
					it.deviceNetworkId == "$device.deviceNetworkId:${eventDescMap.sourceEndpoint}" || it.deviceNetworkId == "$device.deviceNetworkId:${eventDescMap.endpoint}"
				}
				if (childDevice) {
					childDevice.sendEvent(eventMap)
				} else {
					log.debug "Child device: $device.deviceNetworkId:${eventDescMap.sourceEndpoint} was not found"
				}
			}
		}
	}
}

private void createChildDevices() {    
	if (!childDevices) {
		def x = getChildCount()
		for (i in 2..x) {
			addChildDevice("Child Switch Health", "${device.deviceNetworkId}:0${i}", device.hubId,
				[completedSetup: true, label: "${device.displayName[0..-2]}${i}", isComponent: false])
		}
	}
}

private getChildEndpoint(String dni) {
	dni.split(":")[-1] as Integer
}

def on() {
	log.debug("on")
	zigbee.on()
}

def off() {
	log.debug("off")
	zigbee.off()
}

def childOn(String dni) {
	log.debug(" child on ${dni}")
	def childEndpoint = getChildEndpoint(dni)
	zigbee.command(zigbee.ONOFF_CLUSTER, 0x01, "", [destEndpoint: childEndpoint])
}

def childOff(String dni) {
	log.debug(" child off ${dni}")
	def childEndpoint = getChildEndpoint(dni)
	zigbee.command(zigbee.ONOFF_CLUSTER, 0x00, "", [destEndpoint: childEndpoint])
}

/**
 * PING is used by Device-Watch in attempt to reach the Device
 * */
def ping() {
	return refresh()
}

def refresh() {
	if (isOrvibo()) {
		zigbee.readAttribute(zigbee.ONOFF_CLUSTER, 0x0000, [destEndpoint: 0xFF])
	} else {
		def cmds = zigbee.onOffRefresh()
		def x = getChildCount()
		for (i in 2..x) {
			cmds += zigbee.readAttribute(zigbee.ONOFF_CLUSTER, 0x0000, [destEndpoint: i])
		}
		return cmds
	}
}

def poll() {
	refresh()
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
		unschedule("healthPoll")
		runEvery5Minutes("healthPoll")
		def healthEvent = [name: "checkInterval", value: hcIntervalMinutes * 60, displayed: false, data: [protocol: "zigbee", hubHardwareId: device.hub.hardwareID]]
		// Device-Watch allows 2 check-in misses from device
		sendEvent(healthEvent)
		childDevices.each {
			it.sendEvent(healthEvent)
		}
		state.hasConfiguredHealthCheck = true
	}
}

def configure() {
	log.debug "configure()"
	configureHealthCheck()

	if (isOrvibo()) {
		//the orvibo switch will send out device anounce message at ervery 2 mins as heart beat,setting 0x0099 to 1 will disable it.
		def cmds = zigbee.writeAttribute(zigbee.BASIC_CLUSTER, 0x0099, 0x20, 0x01, [mfgCode: 0x0000])
		cmds += refresh()
		return cmds
	} else {
		//other devices supported by this DTH in the future
		def cmds = zigbee.onOffConfig(0, 120)
		def x = getChildCount()
		for (i in 2..x) {
			cmds += zigbee.configureReporting(zigbee.ONOFF_CLUSTER, 0x0000, 0x10, 0, 120, null, [destEndpoint: i])
		}
		cmds += refresh()
		return cmds
	}
}

private Boolean isOrvibo() {
	device.getDataValue("manufacturer") == "ORVIBO"
}

private getChildCount() {
	switch (device.getDataValue("model")) {
		case "9f76c9f31b4c4a499e3aca0977ac4494":
		case "HY0003":
		case "HY0097":
		case "HS2SW3L-EFR-3.0":
		case "E220-KR3N0Z0-HA":
		case "ZB-SW03":
		case "JZ-ZB-003":
		case "PM-S340-ZB":
		case "PM-S340R-ZB":
		case "PM-S350-ZB":
		case "ST-S350-ZB":
		case "SBM300Z3":
		case "HS6SW3A-W-EF-3.0":
			return 3
		case "E220-KR4N0Z0-HA":
		case "ZB-SW04":
		case "JZ-ZB-004":
		case "SBM300Z4":
			return 4
		case "E220-KR5N0Z0-HA":
		case "ZB-SW05":
		case "JZ-ZB-005":
		case "SBM300Z5":
			return 5
		case "E220-KR6N0Z0-HA":
		case "ZB-SW06":
		case "JZ-ZB-006":
		case "SBM300Z6":
			return 6
		case "E220-KR2N0Z0-HA":
		case "SBM300Z2":
		default:
			return 2
	}
}
