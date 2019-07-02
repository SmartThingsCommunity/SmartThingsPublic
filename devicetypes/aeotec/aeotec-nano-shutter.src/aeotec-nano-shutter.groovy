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
 *  Notes - Edited and modifed from Z-Wave Secure Switch for a bare basic use of Aeotec Nano Shutter.
 */
metadata {
	definition(name: "Aeotec Nano Shutter", namespace: "Aeotec", author: "Chris Cheng", runLocally: false) {
		capability "Switch"
		capability "Refresh"
		capability "Polling"
		capability "Actuator"
		capability "Sensor"
		capability "Health Check"
        capability "Switch Level"
        
        command "stop" 
        command "open"
        command "close"

		fingerprint mfr: "0086", prod: "0103", model: "008D"
        inClusters: "5E,55,98,9F,6C"
        inClusters: "85,59,70,2C,2B,25,26,73,7A,86,72,5A"
	}

	simulator {
		status "Open": "command: 9881, payload: FF"
		status "Close": "command: 9881, payload: 00"

		reply "9881002001FF,delay 100,9881002502": "command: 9881, payload: FF"
		reply "988100200100,delay 100,9881002502": "command: 9881, payload: 00"
	}

	tiles {
		standardTile("open", "device.switch", canChangeIcon: false) {
			state "Open", label: 'Open', action: "close", icon: "st.Transportation.transportation13", backgroundColor: "#00a0dc", nextState: "Close"
            state "Close", label: 'Close', action: "open", icon: "st.Transportation.transportation14", backgroundColor: "#ffffff", nextState: "Open"
		}
        /*standardTile("close", "device.switch", canChangeIcon: false) {
			state "Close", label: 'Close', action: "close", icon: "st.Transportation.transportation14", backgroundColor: "#ffffff"
		}*/
        standardTile("stop", "device.switch", canChangeIcon: false) {
			state "default", label: 'Stop', action: "stop", icon: "st.switches.switch.stop", backgroundColor: "#00a0dc"
		}
		standardTile("refresh", "device.switch", inactiveLabel: false, decoration: "flat") {
			state "default", label: '', action: "refresh.refresh", icon: "st.secondary.refresh"
		}

		main (["open", "close", "stop"])
		details(["open", "close", "stop", "refresh"])
	}
}

def installed() {
	// Device-Watch simply pings if no device events received for checkInterval duration of 32min = 2 * 15min + 2min lag time
	sendEvent(name: "checkInterval", value: 2 * 15 * 60 + 2 * 60, displayed: false, data: [protocol: "zwave", hubHardwareId: device.hub.hardwareID, offlinePingable: "1"])
}

def updated() {
	response(refresh())
}

def parse(description) {
	def result = null
	if (description.startsWith("Err 106")) {
		result = createEvent(descriptionText: description, isStateChange: true)
	} else if (description != "updated") {
		def cmd = zwave.parse(description, [0x55: 1,0x98: 1,0x85: 2,0x59: 1,0x70: 2,0x2C: 1,0x2B: 1,0x25: 1,0x26: 3,0x73: 1,0x7A: 2,0x86: 1,0x72: 2,0x5A: 1])
		if (cmd) {
			result = zwaveEvent(cmd)
			log.debug("'$description' parsed to $result")
		} else {
			log.debug("Couldn't zwave.parse '$description'")
		}
	}
	result
}

/**
 *  COMMAND_CLASS_BASIC (0x20)
 *  This command is being ignored in secure inclusion mode.
 *
 *  Short	value	0xFF for on, 0x00 for off
 */

def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicReport cmd) {
	//createEvent(name: "on", value: cmd.value ? "up" : "down")
    //name: "switch", value: cmd.value ? "on" : "off", type: "physical"
    if (state.debug) log.debug "BasicReport(value:${cmd.value})"
    createEvent(name: "switch", value: cmd.value ? "Open" : "Close", type: "physical")
}

def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicSet cmd) {
	//createEvent(name: "off", value: cmd.value ? "up" : "down")
}

/**
 *  COMMAND_CLASS_SWITCH_BINARY (0x25)
 *
 *  Short	value	0xFF for on, 0x00 for off
 */

def zwaveEvent(physicalgraph.zwave.commands.switchbinaryv1.SwitchBinaryReport cmd) {
	//createEvent(name: "on", value: cmd.value ? "up" : "down")
    //name: "switch", value: cmd.value ? "on" : "off", type: "digital"
    if (state.debug) log.debug "BinaryReport(value:${cmd.value})"
    createEvent(name: "switch", value: cmd.value ? "Open" : "Close", type: "physical")
}

def zwaveEvent(physicalgraph.zwave.commands.hailv1.Hail cmd) {
	//createEvent(name: "hail", value: "hail", descriptionText: "Switch button was pressed", displayed: false)
}

def zwaveEvent(physicalgraph.zwave.commands.switchmultilevelv3.SwitchMultilevelReport cmd) {
	//dimmerEvents(cmd)
}

def zwaveEvent(physicalgraph.zwave.commands.securityv1.SecurityMessageEncapsulation cmd) {
	def encapsulatedCommand = cmd.encapsulatedCommand([0x55: 1,0x98: 1,0x85: 2,0x59: 1,0x70: 2,0x2C: 1,0x2B: 1,0x25: 1,0x26: 3,0x73: 1,0x7A: 2,0x86: 1,0x72: 2,0x5A: 1])
	if (encapsulatedCommand) {
		zwaveEvent(encapsulatedCommand)
	}
}

def zwaveEvent(physicalgraph.zwave.Command cmd) {
	log.debug "Unhandled: $cmd"
	null
}

def open() {
	//log.debug("up")
	commands([
		zwave.basicV1.basicSet(value: 0xFF),
		zwave.basicV1.basicGet()
	])
}

def close() {
	//log.debug("down")
	commands([
		zwave.basicV1.basicSet(value: 0x00),
		zwave.basicV1.basicGet()
	])
}

def stop() {
	//log.debug("stop")
	commands([
    	zwave.switchMultilevelV3.switchMultilevelStopLevelChange(),
        zwave.switchMultilevelV3.switchMultilevelGet()
    ])
}

def ping() {
	refresh()
}

def poll() {
	refresh()
}

def refresh() {
	commands([
		zwave.basicV1.basicGet(),
		zwave.configurationV1.configurationSet(parameterNumber: 85,size: 1, scaledConfigurationValue: 0), //set operation mode 1 (S1 = UP, S2 = DOWN)
		zwave.configurationV1.configurationSet(parameterNumber: 80,size: 1, scaledConfigurationValue: 1), //report Basic Report to update status when controlled via switch
        //zwave.configurationV1.configurationSet(parameterNumber: 120,size: 1, scaledConfigurationValue: 1),
        	//(Sets S1 external switch mode): 1 = reserved, 2 = 3-way switch mode, 3 = Push button mode, 4 = automatic identification mode 
        //zwave.configurationV1.configurationSet(parameterNumber: 121,size: 1, scaledConfigurationValue: 1),
        	//(Sets S1 external switch mode): 1 = reserved, 2 = 3-way switch mode, 3 = Push button mode, 4 = automatic identification mode 
        zwave.configurationV1.configurationSet(parameterNumber: 35,size: 1, scaledConfigurationValue: 30) //sets the total up/down time in seconds, this setting is used to calibrate the total up and down time.
	])
}

private command(physicalgraph.zwave.Command cmd) {
	if ((zwaveInfo.zw == null && state.sec != 0) || zwaveInfo?.zw?.contains("s")) {
		zwave.securityV1.securityMessageEncapsulation().encapsulate(cmd).format()
	} else {
		cmd.format()
	}
}

private commands(commands, delay = 200) {
	delayBetween(commands.collect { command(it) }, delay)
}