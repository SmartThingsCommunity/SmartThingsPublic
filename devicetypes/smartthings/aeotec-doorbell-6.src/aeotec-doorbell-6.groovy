/**
 * 	Aeotec Doorbell 6
 *
 * 	Copyright 2019 SmartThings
 *
 * 	Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * 	in compliance with the License. You may obtain a copy of the License at:
 *
 * 		http://www.apache.org/licenses/LICENSE-2.0
 *
 * 	Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 * 	on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 * 	for the specific language governing permissions and limitations under the License.
 *
 */
metadata {
	definition(name: "Aeotec Doorbell 6", namespace: "SmartThings", author: "SmartThings", mcdSync: true,  mnmn: "SmartThings", vid: "generic-8-sound") {
		capability "Actuator"
		capability "Health Check"
		capability "Tamper Alert"
		capability "Alarm"
		capability "Chime"

		fingerprint mfr: "0371", prod: "0003", model: "00A2", deviceJoinName: "Aeotec Doorbell 6"
	}

	tiles {
		multiAttributeTile(name: "alarm", type: "generic", width: 6, height: 4) {
			tileAttribute("device.alarm", key: "PRIMARY_CONTROL") {
				attributeState "off", label: 'off', action: 'alarm.siren', icon: "st.alarm.alarm.alarm", backgroundColor: "#ffffff"
				attributeState "both", label: 'ring!', action: 'alarm.off', icon: "st.alarm.alarm.alarm", backgroundColor: "#0e7507"
			}
		}
		standardTile("off", "device.alarm", inactiveLabel: false, decoration: "flat") {
			state "default", label: '', action: "alarm.off", icon: "st.secondary.off"
		}
		valueTile("tamper", "device.tamper", height: 2, width: 2, decoration: "flat") {
			state "clear", label: 'tamper clear', backgroundColor: "#ffffff"
			state "detected", label: 'tampered', backgroundColor: "#ffffff"
		}

		main "alarm"
		details(["alarm", "off", "tamper"])
	}
}

private getNumberOfSounds() {
	def numberOfSounds = [
			"0003": 8 //Aeotec Doorbell 6
	]
	return numberOfSounds[zwaveInfo.prod] ?: 1
}

def installed() {
	initialize()
	sendEvent(name: "alarm", value: "off", isStateChange: true, displayed: false)
	sendEvent(name: "tamper", value: "clear", isStateChange: true, displayed: false)
}

def updated() {
	initialize()
}

def initialize() {
	if (!childDevices) {
		addChildren(numberOfSounds)
	}
	sendEvent(name: "checkInterval", value: 2 * 60 * 60 + 2 * 60, displayed: false, data: [protocol: "zwave", hubHardwareId: device.hub.hardwareID])
}

def parse(String description) {
	def result = null
	def cmd = zwave.parse(description)
	if (cmd) {
		result = zwaveEvent(cmd)
	}
	log.debug "Parse returned: ${result.inspect()}"
	return result
}

def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicReport cmd) {
	createEvent(name: "alarm", value: cmd.value ? "both" : "off")
}

def zwaveEvent(physicalgraph.zwave.commands.multichannelv3.MultiChannelCmdEncap cmd) {
	def encapsulatedCommand = cmd.encapsulatedCommand()
	def endpoint = cmd.sourceEndPoint
	if (endpoint == state.lastTriggeredSound) {
		zwaveEvent(encapsulatedCommand)
	}
}

def zwaveEvent(physicalgraph.zwave.commands.securityv1.SecurityMessageEncapsulation cmd) {
	def securedEncapsulatedCommand = cmd.securedEncapsulatedCommand()
	if (securedEncapsulatedCommand) {
		zwaveEvent(securedEncapsulatedCommand)
	} else {
		log.warn "Unable to extract encapsulated cmd from $cmd"
		createEvent(descriptionText: cmd.toString())
	}
}

private onOffCmd(value) {
	encap(zwave.basicV1.basicSet(value: value))
}

def on() {
	resetActiveSound()
	state.lastTriggeredSound = 1
	onOffCmd(0xFF)
}

def off() {
	state.lastTriggeredSound = 1
	onOffCmd(0x00)
}

def strobe() {
	on()
}

def siren() {
	on()
}

def both() {
	on()
}

def ping() {
	encap(zwave.basicV1.basicGet())
}

private addChildren(numberOfSounds) {
	for (def endpoint : 2..numberOfSounds) {
		try {
			String childDni = "${device.deviceNetworkId}:$endpoint"

			addChildDevice("Aeotec Child Siren", childDni, device.getHub().getId(), [
					completedSetup: true,
					label         : "$device.displayName Sound $endpoint",
					isComponent   : true,
					componentName : "sound$endpoint",
					componentLabel: "Sound $endpoint"
			])
		} catch (Exception e) {
			log.debug "Excep: ${e} "
		}
	}
}

private encap(cmd, endpoint = null) {
	if (cmd) {
		if (endpoint) {
			cmd = zwave.multiChannelV3.multiChannelCmdEncap(destinationEndPoint: channelNumber(endpoint)).encapsulate(cmd)
		}
		if (zwaveInfo.zw.contains("s")) {
			zwave.securityV1.securityMessageEncapsulation().encapsulate(cmd).format()
		} else {
			cmd.format()
		}
	}
}

def channelNumber(String dni) {
	dni[-1] as Integer
}

def zwaveEvent(physicalgraph.zwave.commands.notificationv3.NotificationReport cmd) {
	if (cmd.notificationStatus == 0xFF) {
		switch (cmd.event) {
			case 0x09: //TAMPER
				createEvent([name: "tamper", value: "detected"])
				break
			case 0x01: //ON
				if (state.lastTriggeredSound == 1) {
					createEvent([name: "alarm", value: "both"])
				} else {
					setActiveSound(state.lastTriggeredSound)
				}
				break
			case 0x00: //OFF
				resetActiveSound()
				createEvent([name: "tamper", value: "clear"])
				break
		}
	}
}

def setOnChild(deviceDni) {
	resetActiveSound()
	sendHubCommand encap(zwave.basicV1.basicSet(value: 0xFF), deviceDni)
	state.lastTriggeredSound = channelNumber(deviceDni)
	setActiveSound(state.lastTriggeredSound)
}

def setOffChild(deviceDni) {
	sendHubCommand encap(zwave.basicV1.basicSet(value: 0x00), deviceDni)
}

def resetActiveSound() {
	String childDni = "${device.deviceNetworkId}:$state.lastTriggeredSound"
	def child = childDevices.find { it.deviceNetworkId == childDni }
	child?.sendEvent([name: "chime", value: "off"])
	sendEvent([name: "alarm", value: "off"])
}

def setActiveSound(soundId) {
	String childDni = "${device.deviceNetworkId}:${soundId}"
	def child = childDevices.find { it.deviceNetworkId == childDni }
	child?.sendEvent([name: "chime", value: "chime"])
}