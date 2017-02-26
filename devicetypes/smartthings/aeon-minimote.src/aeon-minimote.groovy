/**
 *  Copyright 2015 SmartThings
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
	definition (name: "Aeon Minimote", namespace: "smartthings", author: "SmartThings") {
		capability "Actuator"
		capability "Button"
		capability "Configuration"
		capability "Sensor"

		fingerprint deviceId: "0x0101", inClusters: "0x86,0x72,0x70,0x9B", outClusters: "0x26,0x2B"
		fingerprint deviceId: "0x0101", inClusters: "0x86,0x72,0x70,0x9B,0x85,0x84", outClusters: "0x26" // old style with numbered buttons
	}

	simulator {
		status "button 1 pushed":  "command: 2001, payload: 01"
		status "button 1 held":  "command: 2001, payload: 15"
		status "button 2 pushed":  "command: 2001, payload: 29"
		status "button 2 held":  "command: 2001, payload: 3D"
		status "button 3 pushed":  "command: 2001, payload: 51"
		status "button 3 held":  "command: 2001, payload: 65"
		status "button 4 pushed":  "command: 2001, payload: 79"
		status "button 4 held":  "command: 2001, payload: 8D"
		status "wakeup":  "command: 8407, payload: "
	}
	tiles {
		standardTile("button", "device.button", width: 2, height: 2) {
			state "default", label: "", icon: "st.unknown.zwave.remote-controller", backgroundColor: "#ffffff"
		}
		main "button"
		details(["button"])
	}
}

def parse(String description) {
	def results = []
	if (description.startsWith("Err")) {
	    results = createEvent(descriptionText:description, displayed:true)
	} else {
		def cmd = zwave.parse(description, [0x2B: 1, 0x80: 1, 0x84: 1])
		if(cmd) results += zwaveEvent(cmd)
		if(!results) results = [ descriptionText: cmd, displayed: false ]
	}
	// log.debug("Parsed '$description' to $results")
	return results
}

def zwaveEvent(physicalgraph.zwave.commands.wakeupv1.WakeUpNotification cmd) {
	def results = [createEvent(descriptionText: "$device.displayName woke up", isStateChange: false)]

    results += configurationCmds().collect{ response(it) }
	results << response(zwave.wakeUpV1.wakeUpNoMoreInformation().format())

	return results
}

def buttonEvent(button, held) {
	button = button as Integer
	if (held) {
		createEvent(name: "button", value: "held", data: [buttonNumber: button], descriptionText: "$device.displayName button $button was held", isStateChange: true)
	} else {
		createEvent(name: "button", value: "pushed", data: [buttonNumber: button], descriptionText: "$device.displayName button $button was pushed", isStateChange: true)
	}
}

def zwaveEvent(physicalgraph.zwave.commands.sceneactivationv1.SceneActivationSet cmd) {
	Integer button = ((cmd.sceneId + 1) / 2) as Integer
	Boolean held = !(cmd.sceneId % 2)
	buttonEvent(button, held)
}

def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicSet cmd) {
	Integer button = (cmd.value / 40 + 1) as Integer
	Boolean held = (button * 40 - cmd.value) <= 20
	buttonEvent(button, held)
}

def zwaveEvent(physicalgraph.zwave.Command cmd) {
	[ descriptionText: "$device.displayName: $cmd", linkText:device.displayName, displayed: false ]
}

def configurationCmds() {
	def cmds = []
	def hubId = zwaveHubNodeId
	(1..4).each { button ->
		cmds << zwave.configurationV1.configurationSet(parameterNumber: 240+button, scaledConfigurationValue: 1).format()
	}
	(1..4).each { button ->
		cmds << zwave.configurationV1.configurationSet(parameterNumber: (button-1)*40, configurationValue: [hubId, (button-1)*40 + 1, 0, 0]).format()
		cmds << zwave.configurationV1.configurationSet(parameterNumber: (button-1)*40 + 20, configurationValue: [hubId, (button-1)*40 + 21, 0, 0]).format()
	}
	cmds
}

def configure() {
	def cmds = configurationCmds()
	log.debug("Sending configuration: $cmds")
	return cmds
}