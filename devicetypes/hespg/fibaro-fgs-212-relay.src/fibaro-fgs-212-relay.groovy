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

	definition (name: "Fibaro FGS-212 Relay", namespace: "hespg", author: "hespg") {
		capability "Actuator"
		capability "Switch"
		capability "Polling"
		capability "Refresh"
		capability "Sensor"
		capability "Relay Switch"
        capability "Configuration"

        command 'switchToMomentary'
        command 'switchToToggle'
        command 'enableRollerShutter'
        command 'disableRollerShutter'

        attribute "switchType", "string"
        attribute "rollerShutter", "string"
		attribute "label", "string"


		fingerprint deviceId: "0x1001", inClusters: "0x20,0x25,0x27,0x72,0x86,0x70,0x85"
		fingerprint deviceId: "0x1003", inClusters: "0x25,0x2B,0x2C,0x27,0x75,0x73,0x70,0x86,0x72"
	}

	// simulator metadata
	simulator {
		status "on":  "command: 2003, payload: FF"
		status "off": "command: 2003, payload: 00"

		// reply messages
		reply "2001FF,delay 100,2502": "command: 2503, payload: FF"
		reply "200100,delay 100,2502": "command: 2503, payload: 00"
	}

	// tile definitions
	tiles(scale:2) {
		standardTile("switch", "device.switch", width: 6, height: 2, canChangeIcon: true, decoration: "flat") {
			state "on", label: '${name}', action: "switch.off", icon: "st.switches.switch.on", backgroundColor: "#00a0dc"
			state "off", label: '${name}', action: "switch.on", icon: "st.switches.switch.off", backgroundColor: "#ffffff"
		}
		standardTile("refresh", "device.switch", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
			state "default", label:'', action:"refresh.refresh", icon:"st.secondary.refresh"
		}
        valueTile("switchTypeLabel", "device.switch", width: 2, height: 1) {
    		state 'on', label: 'Button Type'
            state 'off', label: 'Button Type'
		}
        valueTile("refreshLabel", "device.switch", width: 2, height: 1) {
    		state 'on', label: 'Refresh'
            state 'off', label: 'Refresh'
		}
        valueTile("dimmerShutterLabel", "device.switch", width: 2, height: 1) {
    		state 'on', label: 'Dimmer/Shutter'
            state 'off', label: 'Dimmer/Shutter'
		}
        valueTile("autoOffLabel", "device.switch", width: 2, height: 1) {
    		state 'on', label: 'Auto Off'
            state 'off', label: 'Auto Off'
		}
        standardTile("switchTypeTile", "switchType", width: 2, height: 2, decoration: 'flat') {
        	state "momentary", label: "Push", action: "switchToToggle", icon:"https://cdn.rawgit.com/greghesp/fibaro-212-relay/896dc359/icons/Minimize%20Window%20Filled-50.png"
            state "toggle", label: "Toggle", action: "switchToMomentary", icon:"https://cdn.rawgit.com/greghesp/fibaro-212-relay/896dc359/icons/Switch%20On%20Filled-50.png"
        }
        standardTile("rollerShutterTile", "rollerShutter", width: 2, height: 2, decoration: 'flat') {
        	state "Disable", label: "Disabled", action: "enableRollerShutter", icon:"https://cdn.rawgit.com/greghesp/fibaro-212-relay/a503a019/icons/Blind%20Automatic%20Mode-Off.png"
            state "Enable", label: "Enabled", action: "disableRollerShutter", icon:"https://cdn.rawgit.com/greghesp/fibaro-212-relay/a503a019/icons/Blind%20Automatic%20Mode-On.png"
        }
		main "switch"
		details(["switch","switchTypeTile","refresh"])
	}
}

def installed() {
	zwave.manufacturerSpecificV1.manufacturerSpecificGet().format()
}

def parse(String description) {
	//Runs when device sends update
	def result = null
	def cmd = zwave.parse(description, [0x20: 1, 0x70: 1])
	if (cmd) {
		result = createEvent(zwaveEvent(cmd))
	}
	if (result?.name == 'hail' && hubFirmwareLessThan("000.011.00602")) {
		result = [result, response(zwave.basicV1.basicGet())]
		log.debug "Was hailed: requesting state update"
	} else {
		log.debug "Parse returned ${cmd}"
	}
	return result
}

def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicReport cmd) {
	log.debug('Basic Report');
    log.debug(cmd)
	[name: "switch", value: cmd.value ? "on" : "off", type: "physical"]
}

def zwaveEvent(physicalgraph.zwave.commands.switchbinaryv1.SwitchBinaryReport cmd) {
    // This runs when SmartThings makes a change
	log.debug('Binary report');
    log.debug(cmd);
	[name: "switch", value: cmd.value ? "on" : "off", type: "digital"]
}

def zwaveEvent(physicalgraph.zwave.commands.configurationv1.ConfigurationReport cmd) {
	log.debug('Config Report');
    log.debug(cmd);
	def value = "when off"
    
    //Setup blind control values here:
    
    if (cmd.configurationValue[0] == 0) {
    	log.debug('Device is set to Momentary')
        sendEvent(name: "switchType", value: "momentary")
        }
	if (cmd.configurationValue[0] == 1) {
   	 	value = "when on"
    	log.debug('Device is set to Toggle')
        sendEvent(name: "switchType", value: "toggle")
        }
	if (cmd.configurationValue[0] == 2) {value = "never"}
    log.debug "${switchType}"
	[name: "indicatorStatus", value: value, display: false]
}

def zwaveEvent(physicalgraph.zwave.commands.hailv1.Hail cmd) {
	log.debug('Hail');
    log.debug(cmd);
	[name: "hail", value: "hail", descriptionText: "Switch button was pressed", displayed: false]
}

def zwaveEvent(physicalgraph.zwave.commands.manufacturerspecificv2.ManufacturerSpecificReport cmd) {
	log.debug('Manufacturer Report');
    log.debug(cmd)
   }

def zwaveEvent(physicalgraph.zwave.Command cmd) {
	// Handles all Z-Wave commands we aren't interested in
    //log.debug(cmd)
	[:]
}

def on() {
	delayBetween([
		zwave.basicV1.basicSet(value: 0xFF).format(),
		zwave.switchBinaryV1.switchBinaryGet().format()
	])
}

def off() {
	delayBetween([
		zwave.basicV1.basicSet(value: 0x00).format(),
		zwave.switchBinaryV1.switchBinaryGet().format()
	])
}

def poll() {
	zwave.switchBinaryV1.switchBinaryGet().format()
    
}

def refresh() {
	delayBetween([
		zwave.switchBinaryV1.switchBinaryGet().format(),
		zwave.manufacturerSpecificV1.manufacturerSpecificGet().format(),
        zwave.manufacturerSpecificV2.manufacturerSpecificGet().format(),
	])
}

def switchToMomentary() {
		log.debug "Changing Switch Type to Momentary"
        delayBetween([
			zwave.configurationV1.configurationSet(configurationValue: [0], parameterNumber: 14, size: 1, ).format(),
            zwave.configurationV1.configurationGet(parameterNumber: 14).format()
        ], 500)
}

def switchToToggle() {
		log.debug "Changing Switch Type to Toggle"
        delayBetween([
			zwave.configurationV1.configurationSet(configurationValue: [1], parameterNumber: 14, size: 1, ).format(),
            zwave.configurationV1.configurationGet(parameterNumber: 14).format()
        ], 500)
}

def enableRollerShutter() {
		log.debug "Shutter function was enabled"
        delayBetween([
			zwave.configurationV1.configurationSet(configurationValue: [1], parameterNumber: 15, size: 1, ).format(),
            zwave.configurationV1.configurationGet(parameterNumber: 15).format()
        ], 500)
}

def disableRollerShutter() {
		log.debug "Shutter function was disabled"
        delayBetween([
			zwave.configurationV1.configurationSet(configurationValue: [0], parameterNumber: 15, size: 1, ).format(),
            zwave.configurationV1.configurationGet(parameterNumber: 15).format()
        ], 500)
}