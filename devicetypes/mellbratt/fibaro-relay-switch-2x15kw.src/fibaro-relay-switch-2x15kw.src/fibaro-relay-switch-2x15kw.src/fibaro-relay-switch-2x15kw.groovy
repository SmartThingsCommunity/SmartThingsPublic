/**
 *  Fibaro Relay Switch 2x1,5kW
 *
 *  Copyright 2014 Anders Mellbratt
 * 
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 
 *  Code from https://github.com/joeltamkin/smartthings-EnerwaveRSM2/blob/master/Enerwave%20ZWN-RSM2%20Device, also under Apache License
 *
 *	Also using code from https://github.com/jialong/smartthings/blob/master/device-types/aeon_smartstrip under MIT License, see below
 *
 *
 *  The MIT License (MIT)
 *  
 *  Copyright (c) 2014 Jialong Wu
 *  
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *  
 *  The above copyright notice and this permission notice shall be included in all
 *  copies or substantial portions of the Software.
 *
 *
 */
metadata {
	definition (name: "Fibaro Relay Switch 2x1,5kW", namespace: "mellbratt", author: "Anders Mellbratt") {
		capability "Polling"
		capability "Relay Switch"
        capability "Configuration"
		capability "Refresh"
        
        attribute "switch1", "string"
		attribute "switch2", "string"
        
        command "on1"
		command "off1"
		command "on2"
		command "off2"

		fingerprint inClusters: "0x72 0x86 0x70 0x85 0x8E 0x60 0x25 0x27 0x7A 0x73 0xEF 0x25 0x60", endpointId: "0", deviceId: "0x1001"
	}

	simulator {
		// TODO: define status and reply messages here
	}

	tiles {
		standardTile("switch1", "device.switch1", canChangeIcon: true) {
                        state "on", label: "switch1", action: "off1", icon: "st.switches.switch.on", backgroundColor: "#79b821"
                        state "off", label: "switch1", action: "on1", icon: "st.switches.switch.off", backgroundColor: "#ffffff"
                }
                
        standardTile("switch2", "device.switch2", canChangeIcon: true) {
                        state "on", label: "switch2", action: "off2", icon: "st.switches.switch.on", backgroundColor: "#79b821"
                        state "off", label: "switch2", action: "on2", icon: "st.switches.switch.off", backgroundColor: "#ffffff"
                }
                
        standardTile("refresh", "device.switch1", inactiveLabel: false, decoration: "flat") {
                        state "default", label:"", action:"refresh.refresh", icon:"st.secondary.refresh"
                }

        standardTile("configure", "device.switch2", inactiveLabel: false, decoration: "flat") {
        				state "default", label:"", action:"configure", icon:"st.secondary.configure"
                }


        main(["switch1", "switch2"])
        details(["switch1","switch2","refresh","configure"])
        
    }
}

// 0x25 - Switch binary
// 0x27 - Switch all
// 0x70 - Configuration
// 0x72
// 0x85 - Association
// 0x86 - Version
// 0x8E
// 0x7A
// 0x73
// 0xEF - Mark
// 0x60 - Multi channel


// parse events into attributes
def parse(String description) {
	log.debug "Parsing '${description}'"
    
	def result = null
	def cmd = zwave.parse(description, [0x60:3, 0x25:1, 0x70:1, 0x72:1])
	if (cmd) {
		result = createEvent(zwaveEvent(cmd))
	}
	if (result?.name == 'hail' && hubFirmwareLessThan("000.011.00602")) {
		result = [result, response(zwave.basicV1.basicGet())]
		log.debug "Was hailed: requesting state update"
	} else {
		log.debug "Parse returned ${result?.descriptionText}"
	}
	return result

}

//Reports


def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicReport cmd) {
        log.debug "BasicReport $cmd.value"
        def map = [name: "switch1", type: "physical"]
        if (cmd.value == 0) {
        	map.value = "off"
        }
        else if (cmd.value == 255) {
        	map.value = "on"
        }
        //refresh()
        return map
        
}


def zwaveEvent(physicalgraph.zwave.commands.switchbinaryv1.SwitchBinaryReport cmd) {
        [name: "switch", value: cmd.value ? "on" : "off", type: "digital"]
}

def zwaveEvent(physicalgraph.zwave.commands.multichannelv3.MultiChannelCmdEncap cmd) {
	 log.debug "MultiChannelCmdEncap $cmd"

    def map = [ name: "switch$cmd.sourceEndPoint" ]
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

// handle commands

def refresh() {
	delayBetween([
    	log.debug("refreshing s1"),
        zwave.multiChannelV3.multiChannelCmdEncap(sourceEndPoint:1, destinationEndPoint:1, commandClass:37, command:2).format(),
    	log.debug("refreshing s2"),
        zwave.multiChannelV3.multiChannelCmdEncap(sourceEndPoint:2, destinationEndPoint:2, commandClass:37, command:2).format(),
		//zwave.multiChannelV3.multiChannelCmdEncap(sourceEndPoint:1, destinationEndPoint:2, commandClass:37, command:2).format()
  	])  
}

def poll() {
	log.debug "Executing 'poll'"
	delayBetween([
		//zwave.switchBinaryV1.switchBinaryGet().format(),
		zwave.manufacturerSpecificV1.manufacturerSpecificGet().format()
	])
}

def configure() {
	log.debug "Executing 'configure'"
    delayBetween([
        zwave.configurationV1.configurationSet(parameterNumber:14, size:4, configurationValue:[0]).format()				// Activates momentary switch mode
    ])
}

// 0x72
def zwaveEvent(physicalgraph.zwave.commands.manufacturerspecificv2.ManufacturerSpecificReport cmd) {
	if (state.manufacturer != cmd.manufacturerName) {
		updateDataValue("Fibaro", cmd.manufacturerName)
	}
}

def zwaveEvent(physicalgraph.zwave.Command cmd) {
	// Handles all Z-Wave commands we aren't interested in
	[:]
}

//switch instance

def on(value) {
log.debug "value $value"
	delayBetween([
		zwave.multiChannelV3.multiChannelCmdEncap(instance: value, commandClass:37, command:1, parameter:[255]).format(),
		zwave.multiChannelV3.multiChannelCmdEncap(instance: value, commandClass:37, command:2).format()
	])
}

def off(value) {
log.debug "value $value"
	delayBetween([
		zwave.multiChannelV3.multiChannelCmdEncap(instance: value, commandClass:37, command:1, parameter:[0]).format(),
		zwave.multiChannelV3.multiChannelCmdEncap(instance: value, commandClass:37, command:2).format()
	])
}

def on1() {
	delayBetween([
		zwave.multiChannelV3.multiChannelCmdEncap(sourceEndPoint:1, destinationEndPoint:1, commandClass:37, command:1, parameter:[255]).format(),
		zwave.multiChannelV3.multiChannelCmdEncap(sourceEndPoint:1, destinationEndPoint:1, commandClass:37, command:2).format(),
		zwave.multiChannelV3.multiChannelCmdEncap(sourceEndPoint:2, destinationEndPoint:2, commandClass:37, command:2).format(),
	])
}

def off1() {
	delayBetween([
		zwave.multiChannelV3.multiChannelCmdEncap(sourceEndPoint:1, destinationEndPoint:1, commandClass:37, command:1, parameter:[0]).format(),
		zwave.multiChannelV3.multiChannelCmdEncap(sourceEndPoint:1, destinationEndPoint:1, commandClass:37, command:2).format(),
		zwave.multiChannelV3.multiChannelCmdEncap(sourceEndPoint:2, destinationEndPoint:2, commandClass:37, command:2).format(),

    ])
}

def on2() {
	delayBetween([
		zwave.multiChannelV3.multiChannelCmdEncap(sourceEndPoint:2, destinationEndPoint:2, commandClass:37, command:1, parameter:[255]).format(),
		zwave.multiChannelV3.multiChannelCmdEncap(sourceEndPoint:2, destinationEndPoint:2, commandClass:37, command:2).format(),
		zwave.multiChannelV3.multiChannelCmdEncap(sourceEndPoint:1, destinationEndPoint:1, commandClass:37, command:2).format()
    ])
}

def off2() {
	delayBetween([
		zwave.multiChannelV3.multiChannelCmdEncap(sourceEndPoint:2, destinationEndPoint:2, commandClass:37, command:1, parameter:[0]).format(),
		zwave.multiChannelV3.multiChannelCmdEncap(sourceEndPoint:2, destinationEndPoint:2, commandClass:37, command:2).format(),
		zwave.multiChannelV3.multiChannelCmdEncap(sourceEndPoint:1, destinationEndPoint:1, commandClass:37, command:2).format()
	])
}


