/**
 *
 *  Adaptation of my Aeon SmartStrip handler that was a heavily modified version of the SmartThing provided Aeon handler
 *
 *  Supports a few things not found in other handlers:
 *  - Instant status updates when button on power strip is pressed
 *  - All On & All Off commands controlled by main switch
 *
 *  Copyright 2016 Eric Maycock
 *
 *  zooZ Power Strip ZEN20
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
	definition (name: "zooZ Power Strip", namespace: "erocm123", author: "Eric Maycock", vid:"generic-switch") {
		capability "Switch"
		capability "Refresh"
		capability "Configuration"
		capability "Actuator"
		capability "Sensor"
        capability "Health Check"

		(1..5).each { n ->
			attribute "switch$n", "enum", ["on", "off"]
			command "on$n"
			command "off$n"
		}

		fingerprint manufacturer: "015D", prod: "0651", model: "F51C"
        fingerprint deviceId: "0x1004", inClusters: "0x5E,0x85,0x59,0x5A,0x72,0x60,0x8E,0x73,0x27,0x25,0x86"
	}

	simulator {

	}
    
    preferences {
        input("enableDebugging", "boolean", title:"Enable Debugging", value:false, required:false, displayDuringSetup:false)
    }

	// tile definitions
	tiles(scale: 2) {
        multiAttributeTile(name:"switch", type: "lighting", width: 6, height: 4, canChangeIcon: true){
			tileAttribute ("device.switch", key: "PRIMARY_CONTROL") {
				attributeState "on", label: '${name}', action: "switch.off", icon: "st.switches.switch.on", backgroundColor: "#00a0dc"
				attributeState "off", label: '${name}', action: "switch.on", icon: "st.switches.switch.off", backgroundColor: "#ffffff"
			}
            tileAttribute ("statusText", key: "SECONDARY_CONTROL") {
           		attributeState "statusText", label:'${currentValue}'       		
            }
		}

		standardTile("refresh", "device.power", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
			state "default", label:'', action:"refresh.refresh", icon:"st.secondary.refresh"
		}
        standardTile("configure", "device.configure", inactiveLabel: false, width: 2, height: 2, decoration: "flat") {
			state "configure", label:'', action:"configuration.configure", icon:"st.secondary.configure"
		}
        valueTile("statusText", "statusText", inactiveLabel: false, width: 2, height: 2) {
			state "statusText", label:'${currentValue}', backgroundColor:"#ffffff"
		}

		(1..5).each { n ->
			standardTile("switch$n", "switch$n", canChangeIcon: true, decoration: "flat", width: 2, height: 2) {
				state "on", label: "switch$n", action: "off$n", icon: "st.switches.switch.on", backgroundColor: "#00a0dc"
				state "off", label: "switch$n", action: "on$n", icon: "st.switches.switch.off", backgroundColor: "#cccccc"
			}

		}

		main(["switch", "switch1", "switch2", "switch3", "switch4", "switch5"])
		details(["switch",
				 "switch1","switch2","switch3",
				 "switch4","switch5","refresh",
                 ])
	}
}

def parse(String description) {
	def result = []
	if (description.startsWith("Err")) {
		result = createEvent(descriptionText:description, isStateChange:true)
	} else if (description != "updated") {
		def cmd = zwave.parse(description, [0x60: 3, 0x32: 3, 0x25: 1, 0x20: 1])
        //log.debug "Command: ${cmd}"
		if (cmd) {
			result += zwaveEvent(cmd, null)
		}
	}
    
    def statusTextmsg = ""
    if (device.currentState('power') && device.currentState('energy')) statusTextmsg = "${device.currentState('power').value} W ${device.currentState('energy').value} kWh"
    sendEvent("name":"statusText", "value":statusTextmsg)
    
    //log.debug "parsed '${description}' to ${result.inspect()}"

	result
}

def endpointEvent(endpoint, map) {
    logging("endpointEvent($endpoint, $map)")
	if (endpoint) {
		map.name = map.name + endpoint.toString()
	}
	createEvent(map)
}

def zwaveEvent(physicalgraph.zwave.commands.multichannelv3.MultiChannelCmdEncap cmd, ep) {
    logging("MultiChannelCmdEncap")
	def encapsulatedCommand = cmd.encapsulatedCommand([0x32: 3, 0x25: 1, 0x20: 1])
	if (encapsulatedCommand) {
		if (encapsulatedCommand.commandClassId == 0x32) {
			// Metered outlets are numbered differently than switches
			Integer endpoint = cmd.sourceEndPoint
			if (endpoint > 2) {
				zwaveEvent(encapsulatedCommand, endpoint - 2)
			} else if (endpoint == 0) {
				zwaveEvent(encapsulatedCommand, 0)
			} else if (endpoint == 1 || endpoint == 2) {
                zwaveEvent(encapsulatedCommand, endpoint + 4)
            } else {
				log.debug "Ignoring metered outlet ${endpoint} msg: ${encapsulatedCommand}"
				[]
			}
		} else {
			zwaveEvent(encapsulatedCommand, cmd.sourceEndPoint as Integer)
		}
	}
}

def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicReport cmd, endpoint) {
    logging("BasicReport")
    def cmds = []
    (1..5).each { n ->
            cmds << encap(zwave.switchBinaryV1.switchBinaryGet(), n)
            cmds << "delay 1000"
    }

    return response(cmds)
}

def zwaveEvent(physicalgraph.zwave.commands.configurationv2.ConfigurationReport cmd) {
    logging("${device.displayName} parameter '${cmd.parameterNumber}' with a byte size of '${cmd.size}' is set to '${cmd.configurationValue}'")
}

def zwaveEvent(physicalgraph.zwave.commands.configurationv1.ConfigurationReport cmd) {
    logging("${device.displayName} parameter '${cmd.parameterNumber}' with a byte size of '${cmd.size}' is set to '${cmd.configurationValue}'")
}

def zwaveEvent(physicalgraph.zwave.commands.switchbinaryv1.SwitchBinaryReport cmd, endpoint) {
    logging("SwitchBinaryReport")
	def map = [name: "switch", value: (cmd.value ? "on" : "off")]
	def events = [endpointEvent(endpoint, map)]
	def cmds = []
	if (!endpoint && events[0].isStateChange) {
		events += (1..4).collect { ep -> endpointEvent(ep, map.clone()) }
		cmds << "delay 3000"
		cmds += delayBetween((1..4).collect { ep -> encap(zwave.meterV3.meterGet(scale: 2), ep) })
	} else {
        if (events[0].value == "on") {
            events += [endpointEvent(null, [name: "switch", value: "on"])]
        } else {
            def allOff = true
            (1..5).each { n ->
                if (n != endpoint) {
                    if (device.currentState("switch${n}").value != "off") allOff = false
                }
            }
            if (allOff) {
                    events += [endpointEvent(null, [name: "switch", value: "off"])]
            }
        }
        
    }
	if(cmds) events << response(cmds)
	events
}

def zwaveEvent(physicalgraph.zwave.commands.manufacturerspecificv2.ManufacturerSpecificReport cmd, ep) {
	updateDataValue("MSR", String.format("%04X-%04X-%04X", cmd.manufacturerId, cmd.productTypeId, cmd.productId))
	return null
}

def zwaveEvent(physicalgraph.zwave.Command cmd, ep) {
	logging("${device.displayName}: Unhandled ${cmd}" + (ep ? " from endpoint $ep" : ""))
}

def onOffCmd(value, endpoint = null) {
    logging("onOffCmd($value, $endpoint)")
	[
		encap(zwave.basicV1.basicSet(value: value), endpoint),
		"delay 500",
		encap(zwave.switchBinaryV1.switchBinaryGet(), endpoint),
	]
}

def on() { 
    def cmds = []
    cmds << zwave.switchAllV1.switchAllOn().format()
    cmds << "delay 1000"
    (1..5).each { endpoint ->
            cmds << encap(zwave.switchBinaryV1.switchBinaryGet(), endpoint)
    }
    return cmds
}

def off() { 
    def cmds = []
    cmds << zwave.switchAllV1.switchAllOff().format()
    (1..5).each { endpoint ->
            cmds << "delay 1000"
            cmds << encap(zwave.switchBinaryV1.switchBinaryGet(), endpoint)
    }
    return cmds
}

def on1() { onOffCmd(0xFF, 1) }
def on2() { onOffCmd(0xFF, 2) }
def on3() { onOffCmd(0xFF, 3) }
def on4() { onOffCmd(0xFF, 4) }
def on5() { onOffCmd(0xFF, 5) }

def off1() { onOffCmd(0, 1) }
def off2() { onOffCmd(0, 2) }
def off3() { onOffCmd(0, 3) }
def off4() { onOffCmd(0, 4) }
def off5() { onOffCmd(0, 5) }

def refresh() {
    logging("refresh()")
	def cmds = []
    
    (1..5).each { endpoint ->
            cmds << encap(zwave.switchBinaryV1.switchBinaryGet(), endpoint)
    }
    
    delayBetween(cmds, 1000)
}

def ping() {
    logging("ping()")
	refresh()
}

def configure() {
    state.enableDebugging = settings.enableDebugging
    logging("configure()")
    sendEvent(name: "checkInterval", value: 2 * 60 * 12 * 60 + 5 * 60, displayed: false, data: [protocol: "zwave", hubHardwareId: device.hub.hardwareID])
    def cmds = [
        zwave.versionV1.versionGet().format(),
        zwave.manufacturerSpecificV2.manufacturerSpecificGet().format(),
        zwave.firmwareUpdateMdV2.firmwareMdGet().format(),
    ]

	response(delayBetween(cmds, 1000))
}

def installed() {
    logging("installed()")
    configure()
}

def updated() {
    logging("updated()")
    configure()
}

private encap(cmd, endpoint) {
	if (endpoint) {
		zwave.multiChannelV3.multiChannelCmdEncap(destinationEndPoint:endpoint).encapsulate(cmd).format()
	} else {
		cmd.format()
	}
}

private def logging(message) {
    if (state.enableDebugging == "true") log.debug message
}
