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

		fingerprint profileId: "0104", inClusters: "0000, 0005, 0004, 0006", outClusters: "0000", manufacturer: "ORVIBO", model: "074b3ffba5a045b7afd94c47079dd553", deviceJoinName: "Switch 1"
		fingerprint profileId: "0104", inClusters: "0006, 0005, 0004, 0000, 0003, 0B04, 0008", outClusters: "0019", manufacturer: "Aurora", model: "DoubleSocket50AU", deviceJoinName: "Aurora Smart Double Socket 1"
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
}

def updated() {
	log.debug "updated()"
	updateDataValue("onOff", "catchall")
}

def parse(String description) {
	log.debug "description is $description"
	Map eventMap = zigbee.getEvent(description)
	log.debug "eventMap is $eventMap"
	Map eventDescMap = zigbee.parseDescriptionAsMap(description)
	log.debug "eventDescMap is $eventDescMap"

	if (!eventMap && eventDescMap) {
		eventMap = [:]
		if (eventDescMap?.clusterId == zigbee.ONOFF_CLUSTER) {
			eventMap[name] = "switch"
			eventMap[value] = eventDescMap?.value
		}
	}

	if (eventMap.value) {
		if (eventDescMap?.sourceEndpoint == "01") {
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

private void createChildDevices() {
	def i = 2
	addChildDevice("Child Switch Health", "${device.deviceNetworkId}:0${i}", device.hubId,
		[completedSetup: true, label: "${device.displayName[0..-2]}${i}", isComponent: false])
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
		zigbee.onOffRefresh() + zigbee.readAttribute(zigbee.ONOFF_CLUSTER, 0x0000, [destEndpoint: 2])
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
		zigbee.writeAttribute(zigbee.BASIC_CLUSTER, 0x0099, 0x20, 0x01, [mfgCode: 0x0000])
	} else {
		// Aurora (and other devices supported by this DTH in the future)
		zigbee.onOffConfig(0, 120) + zigbee.configureReporting(zigbee.ONOFF_CLUSTER, 0x0000, 0x10, 0, 120, null, [destEndpoint: 0x02]) + refresh()
	}
}

private Boolean isOrvibo() {
	device.getDataValue("manufacturer") == "ORVIBO"
}

private Boolean isAurora() {
	device.getDataValue("manufacturer") == "Aurora"
}