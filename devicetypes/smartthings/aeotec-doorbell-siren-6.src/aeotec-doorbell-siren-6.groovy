/**
 * 	Aeotec Doorbell 6
 *
 * 	Copyright 2020 SmartThings
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
	definition(name: "Aeotec Doorbell Siren 6", namespace: "smartthings", author: "SmartThings", mcdSync: true) {
		capability "Actuator"
		capability "Health Check"
		capability "Tamper Alert"
		capability "Alarm"
		capability "Chime"

		fingerprint mfr: "0371", prod: "0003", model: "00A2", deviceJoinName: "Aeotec Doorbell", ocfDeviceType: "x.com.st.d.doorbell" //EU //Aeotec Doorbell 6
		fingerprint mfr: "0371", prod: "0103", model: "00A2", deviceJoinName: "Aeotec Doorbell", ocfDeviceType: "x.com.st.d.doorbell" //US //Aeotec Doorbell 6
		fingerprint mfr: "0371", prod: "0003", model: "00A4", deviceJoinName: "Aeotec Siren", ocfDeviceType: "x.com.st.d.siren" //EU //Aeotec Siren 6
		fingerprint mfr: "0371", prod: "0103", model: "00A4", deviceJoinName: "Aeotec Siren", ocfDeviceType: "x.com.st.d.siren" //US //Aeotec Siren 6
		fingerprint mfr: "0371", prod: "0203", model: "00A4", deviceJoinName: "Aeotec Siren", ocfDeviceType: "x.com.st.d.siren" //AU //Aeotec Siren 6
		fingerprint mfr: "0371", prod: "0203", model: "00A2", deviceJoinName: "Aeotec Doorbell", ocfDeviceType: "x.com.st.d.doorbell" //AU //Aeotec Doorbell 6
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
		standardTile("refresh", "command.refresh", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
			state "refresh", label: '', action: "refresh.refresh", icon: "st.secondary.refresh-icon"
		}

		main "alarm"
		details(["alarm", "off", "tamper", "refresh"])
	}
}

private getNumberOfSounds() {
	def numberOfSounds = [
			"0003" : 8, //Aeotec Doorbell/Siren EU
			"0103" : 8, //Aeotec Doorbell/Siren US
			"0203" : 8 //Aeotec Doorbell/Siren AU
	]
	return numberOfSounds[zwaveInfo.prod] ?: 1
}

def installed() {
	initialize()
	sendEvent(name: "alarm", value: "off", isStateChange: true, displayed: false)
	sendEvent(name: "chime", value: "off", isStateChange: true, displayed: false)
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

def zwaveEvent(physicalgraph.zwave.commands.multichannelv3.MultiChannelCmdEncap cmd) {
	if (cmd.commandClass == 0x6C && cmd.parameter.size >= 4) { // Supervision encapsulated Message
		// Supervision header is 4 bytes long, two bytes dropped here are the latter two bytes of the supervision header
		cmd.parameter = cmd.parameter.drop(2)
		// Updated Command Class/Command now with the remaining bytes
		cmd.commandClass = cmd.parameter[0]
		cmd.command = cmd.parameter[1]
		cmd.parameter = cmd.parameter.drop(2)
	}
	def encapsulatedCommand = cmd.encapsulatedCommand()
	def endpoint = cmd.sourceEndPoint
	if (endpoint == state.lastTriggeredSound && encapsulatedCommand != null) {
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

def chime() {
	on()
}

def ping() {
	def cmds = [
			encap(zwave.basicV1.basicGet())
	]
	sendHubCommand(cmds)
}

def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicReport cmd) {
	if (cmd.value == 0) {
		keepChildrenOnline()
		sendEvent(name: "alarm", value: "off")
		sendEvent(name: "chime", value: "off")
	}
}

def refresh() {
	ping()
}

private addChildren(numberOfSounds) {
	for (def endpoint : 2..numberOfSounds) {
		try {
			String childDni = "${device.deviceNetworkId}:$endpoint"

			addChildDevice("Aeotec Doorbell Siren Child", childDni, device.getHub().getId(), [
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
		if (endpoint && endpoint > 1) {
			cmd = zwave.multiChannelV3.multiChannelCmdEncap(destinationEndPoint: endpoint).encapsulate(cmd)
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
				sendEvent(name: "tamper", value: "detected")
				sendEvent(name: "alarm", value: "both")
				runIn(2, "clearTamperAndAlarm")
				break
			case 0x01: //ON
				if (state.lastTriggeredSound == 1) {
					sendEvent(name: "chime", value: "chime")
					sendEvent(name: "alarm", value: "both")
				} else {
					setActiveSound(state.lastTriggeredSound)
				}
				break
			case 0x00: //OFF
				resetActiveSound()
				sendEvent(name: "tamper", value: "clear")
				sendEvent(name: "alarm", value: "off")
				sendEvent(name: "chime", value: "off")
				break
		}
	}
}

def clearTamperAndAlarm() {
	sendEvent(name: "tamper", value: "clear")
	sendEvent(name: "alarm", value: "off")
}

def setOnChild(deviceDni) {
	resetActiveSound()
	sendHubCommand encap(zwave.basicV1.basicSet(value: 0xFF), channelNumber(deviceDni))
	state.lastTriggeredSound = channelNumber(deviceDni)
	setActiveSound(state.lastTriggeredSound)
}

def setOffChild(deviceDni) {
	sendHubCommand encap(zwave.basicV1.basicSet(value: 0x00), channelNumber(deviceDni))
}

def resetActiveSound() {
	if (state.lastTriggeredSound > 1) {
		String childDni = "${device.deviceNetworkId}:$state.lastTriggeredSound"
		def child = childDevices.find { it.deviceNetworkId == childDni }

		setOffChild(childDni)
		child?.sendEvent([name: "chime", value: "off"])
		child?.sendEvent([name: "alarm", value: "off"])
	} else {
		sendHubCommand(onOffCmd(0x00))
	}
	sendEvent([name: "alarm", value: "off"])
	sendEvent([name: "chime", value: "off"])
}

def setActiveSound(soundId) {
	String childDni = "${device.deviceNetworkId}:${soundId}"
	def child = childDevices.find { it.deviceNetworkId == childDni }
	child?.sendEvent(name: "chime", value: "chime")
	child?.sendEvent(name: "alarm", value: "both")
}

def keepChildrenOnline() {
	/*
	Method to make children online when checkInterval will be called.
	*/
	for (def i : 2..numberOfSounds) {
		def soundNumber = i
		String childDni = "${device.deviceNetworkId}:$soundNumber"
		def child = childDevices.find { it.deviceNetworkId == childDni }
		child?.sendEvent(name: "chime", value: "off")
		child?.sendEvent(name: "alarm", value: "off")
	}
}
