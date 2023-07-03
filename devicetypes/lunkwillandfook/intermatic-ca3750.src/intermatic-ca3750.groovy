/**
 *  Intermatic CA3750
 *
 *  Copyright 2015 Jeremy Huckeba
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
	// Automatically generated. Make future change here.
	definition (name: "Intermatic CA3750", namespace: "LunkwillAndFook", author: "Jeremy Huckeba") {
		capability "Switch"
		capability "Polling"
		capability "Refresh"

		fingerprint inClusters: "0x91 0x73 0x72 0x86 0x60 0x25 0x27"
        
		attribute "switch1", "string"
		attribute "switch2", "string"

		command "on"
		command "off"
		command "on2"
		command "off2"
	}
    
	simulator {
		// TODO: define status and reply messages here
	}
    
	// tile definitions
    tiles {
        standardTile("switch1", "device.switch", width: 2, height: 2, canChangeIcon: true) {
            state "on", label: '${name}', action: "off", icon: "st.switches.switch.on", backgroundColor: "#79b821"
            state "off", label: '${name}', action: "on", icon: "st.switches.switch.off", backgroundColor: "#ffffff"
        }
        standardTile("switch2", "device.switch2",canChangeIcon: false) {
            state "on", label: "", action: "off2", icon: "st.switches.switch.on", backgroundColor: "#79b821"
            state "off", label: "", action: "on2", icon: "st.switches.switch.off", backgroundColor: "#ffffff"
        }
        standardTile("refresh", "device.switch", inactiveLabel: false, decoration: "flat") {
            state "default", label:"", action:"refresh", icon:"st.secondary.refresh"
        }
        
        main "switch1"

        details(["switch1","switch2","refresh"])
    }
}

//0 0 0x1001 0 0 0 7 0x91 0x73 0x72 0x86 0x60 0x25 0x27
//Intermatic CA3750

//0x72 
//0x91 
// 0x25: switch binary
// 0x32: meter
// 0x27: switch all
// 0x60: multi-channel
// 0x70: configuration
// 0x72 Manufacturer Specific
// 0x73 Power Level
// 0x85: association
// 0x86: version
// 0x91 Manufacturer Proprietary
// 0xEF: mark
// 0x82: hail

def parse(String description) {
    def results = []
    def cmd = zwave.parse(description, [0x60:1, 0x25:1, 0x32:1, 0x70:1 , 0x72:1, 0x73:1, 0x91:1 ])
    if (cmd) { results = createEvent(zwaveEvent(cmd)) }
    return results
}

def zwaveEvent(physicalgraph.zwave.commands.switchbinaryv1.SwitchBinaryReport cmd) {
    log.debug "Switch BINARY (button) - $cmd ${cmd?.value} "
    def map = []; def value
    if(cmd.value==255) { value="on" } else { value="off" }
    map = [name: "switch", value:value, type: "digital"]
	map
}

def zwaveEvent(physicalgraph.zwave.commands.multiinstancev1.MultiInstanceCmdEncap cmd) {
    log.debug "Miv1 $cmd - $cmd?.instance - $cmd?.commandClass"

    def map = [ name: "switch$cmd.instance" ]
    if (cmd.commandClass == 37){
        if (cmd.parameter == [0]) {
            map.value = "off"
        }
        if (cmd.parameter == [255]) {
            map.value = "on"
        }

        map
    }
}

def zwaveEvent(physicalgraph.zwave.commands.manufacturerspecificv1.ManufacturerSpecificReport cmd) {
    log.debug "mi v1 specific report $cmd - $cmd?.instance - $cmd?.commandClass"
}

def zwaveEvent(physicalgraph.zwave.commands.configurationv1.ConfigurationReport cmd) {
	log.debug "Configuration Report for parameter ${cmd.parameterNumber}: Value is ${cmd.configurationValue} Size is ${cmd.size}"
}

def zwaveEvent(physicalgraph.zwave.Command cmd) {
        // Handles all Z-Wave commands we arent interested in 
        [:]
    log.debug "Capture All $cmd"
}

def poll() {
	log.debug "Polling Switch - $device.label"
	delayBetween([
        zwave.switchBinaryV1.switchBinaryGet().format(),
    	zwave.multiChannelV3.multiInstanceCmdEncap(instance: 1).encapsulate(zwave.switchBinaryV1.switchBinaryGet()).format(),
    	zwave.multiChannelV3.multiInstanceCmdEncap(instance: 2).encapsulate(zwave.switchBinaryV1.switchBinaryGet()).format(),
		zwave.manufacturerSpecificV1.manufacturerSpecificGet().format(),
	], 100)
}

def refresh() {
	log.debug "----------------- Refresh again requested $device.label -----------------" 
	delayBetween([
        zwave.switchBinaryV1.switchBinaryGet().format(),
    	zwave.multiChannelV3.multiInstanceCmdEncap(instance: 1).encapsulate(zwave.switchBinaryV1.switchBinaryGet()).format(),
    	zwave.multiChannelV3.multiInstanceCmdEncap(instance: 2).encapsulate(zwave.switchBinaryV1.switchBinaryGet()).format(),
		zwave.manufacturerSpecificV1.manufacturerSpecificGet().format(),
	], 100)
}

def setlevel1(value) { setleveX(1, value) }; def setlevel2(value) { setlevelX(2, value) }

def on() {
	delayBetween([
		zwave.multiChannelV3.multiInstanceCmdEncap(instance:1, commandClass:37, command:1, parameter:[255]).format(),
        zwave.switchBinaryV1.switchBinaryGet().format(),
    	zwave.multiChannelV3.multiInstanceCmdEncap(instance: 1).encapsulate(zwave.switchBinaryV1.switchBinaryGet()).format(),
    	zwave.multiChannelV3.multiInstanceCmdEncap(instance: 2).encapsulate(zwave.switchBinaryV1.switchBinaryGet()).format(),
    ], 100)
}

def off() {
	delayBetween([
        zwave.multiChannelV3.multiInstanceCmdEncap(instance:1, commandClass:37, command:1, parameter:[0]).format(),
        zwave.switchBinaryV1.switchBinaryGet().format(),
        zwave.multiChannelV3.multiInstanceCmdEncap(instance: 1).encapsulate(zwave.switchBinaryV1.switchBinaryGet()).format(),
        zwave.multiChannelV3.multiInstanceCmdEncap(instance: 2).encapsulate(zwave.switchBinaryV1.switchBinaryGet()).format(),
    ], 100)
}

def on2() {
	delayBetween([
        zwave.multiChannelV3.multiInstanceCmdEncap(instance:2, commandClass:37, command:1, parameter:[255]).format(),
        zwave.switchBinaryV1.switchBinaryGet().format(),
        zwave.multiChannelV3.multiInstanceCmdEncap(instance: 1).encapsulate(zwave.switchBinaryV1.switchBinaryGet()).format(),
        zwave.multiChannelV3.multiInstanceCmdEncap(instance: 2).encapsulate(zwave.switchBinaryV1.switchBinaryGet()).format(),
    ], 100)
}

def off2() {
	delayBetween([
		zwave.multiChannelV3.multiInstanceCmdEncap(instance:2, commandClass:37, command:1, parameter:[0]).format(),
        zwave.switchBinaryV1.switchBinaryGet().format(),
        zwave.multiChannelV3.multiInstanceCmdEncap(instance: 1).encapsulate(zwave.switchBinaryV1.switchBinaryGet()).format(),
    	zwave.multiChannelV3.multiInstanceCmdEncap(instance: 2).encapsulate(zwave.switchBinaryV1.switchBinaryGet()).format(),
    ], 100)
}

def configure() {
	log.debug "Executing configure"
    def switchAllmode
    if(switchAll=="true") { switchAllmode = 255 } else { switchAllmode=0 }
    log.debug "SW All - $switchAllmode $switchAll"
}

