/*
 *  Copyright 2018 SmartThings
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not
 *  use this file except in compliance with the License. You may obtain a copy
 *  of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 *  WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 *  License for the specific language governing permissions and limitations
 *  under the License.
 *  Date : 2018-11-28
 */

metadata {
	definition(name: "GoQual 3G Switch", namespace: "shinjjang", author: "shinjjang", ocfDeviceType: "oic.d.switch", mnmn: "SmartThings", vid: "generic-switch") {
		capability "Actuator"
		capability "Configuration"
		capability "Refresh"
		capability "Health Check"
		capability "Switch"

		command "childOn", ["string"]
		command "childOff", ["string"]

		fingerprint profileId: "0104", endpoint: "03", inClusters: "0006, 0000, 0003", outClusters: "0019", manufacturer: "", model: "", deviceJoinName: "GQ Switch"
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
    
	preferences {
		input name: "make", type: "bool", title: "2,3구 스위치 만들기"
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
	if(make){
		createChildDevices()
		updateDataValue("onOff", "catchall")
        device.updateSetting("make", false)

    }
}


def parse(String description) {
	log.debug "description is $description"
	Map map = zigbee.getEvent(description)
	if (map) {
		if (description?.startsWith('on/off')) {
			log.debug "receive on/off message without endpoint id"
			sendHubCommand(refresh().collect { new physicalgraph.device.HubAction(it) }, 0)
		} else {
			Map descMap = zigbee.parseDescriptionAsMap(description)
			log.debug "$descMap"

			if (descMap?.clusterId == "0006" && descMap.sourceEndpoint == "01") {
				sendEvent(map)
			} else if (descMap?.clusterId == "0006") {
				def childDevice = childDevices.find {
					it.deviceNetworkId == "$device.deviceNetworkId:${descMap.sourceEndpoint}"
				}
				if (childDevice) {
					childDevice.sendEvent(map)
				}
			}
		}
	}
}


private void createChildDevices() {
	def i = 2
    def j = 3
	addChildDevice("Child Switch Health", "${device.deviceNetworkId}:0${i}", device.hubId,
			[completedSetup: true, label: "${device.displayName.split("1")[-1]}${i}", isComponent : false])
	addChildDevice("Child Switch Health", "${device.deviceNetworkId}:0${j}", device.hubId,
			[completedSetup: true, label: "${device.displayName.split("1")[-1]}${j}", isComponent : false])
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
	zigbee.command(0x0006, 0x01, "", [destEndpoint: getChildEndpoint(dni)])
}

def childOff(String dni) {
	log.debug(" child off ${dni}")
	zigbee.command(0x0006, 0x00, "", [destEndpoint: getChildEndpoint(dni)])
}

/**
 * PING is used by Device-Watch in attempt to reach the Device
 * */
def ping() {
	return refresh()
}

def refresh() {
	return zigbee.readAttribute(0x0006, 0x0000, [destEndpoint: 0xFF])
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
	//the orvibo switch will send out device anounce message at ervery 2 mins as heart beat,setting 0x0099 to 1 will disable it.
	return zigbee.writeAttribute(0x0000, 0x0099, 0x20, 0x01, [mfgCode: 0x0000])
}