/**
 *  Fibaro Wall Plug ZW3
 *
 *  Copyright 2016 Fibar Group S.A.
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
	definition (name: "Fibaro Wall Plug ZW3", namespace: "fibargroup", author: "Fibar Group S.A.") {
		capability "Actuator"
		capability "Configuration"
		capability "Energy Meter"
		capability "Power Meter"
		capability "Refresh"
		capability "Sensor"
		capability "Switch"
        
        command "reset"
        
        fingerprint deviceId: "0x1001", inClusters: "0x72, 0x86, 0x70, 0x85, 0x25, 0x9C, 0x71, 0x73, 0x32, 0x31, 0x7A", outClusters: "0x25" //v 2.2
        fingerprint deviceId: "0x1000", inClusters: "0x72, 0x86, 0x70, 0x85, 0x8E, 0x25, 0x73, 0x32, 0x31, 0x7A", outClusters: "0x25, 0x31, 0x32" //v2.4+
	}

	simulator {

	}

	tiles(scale: 2) {    
    	multiAttributeTile(name:"FGWP", type:"lighting", width:6, height:4) {//with generic type secondary control text is not displayed in Android app
        	tileAttribute("device.switch", key:"PRIMARY_CONTROL") {
            	attributeState("on", label: '${name}', action: "switch.off", icon:"st.switches.switch.on", backgroundColor:"#79b821")
            	attributeState("off", label: '${name}', action: "switch.on", icon:"st.switches.switch.off", backgroundColor:"#ffffff")   
            }
            
            tileAttribute("device.power", key:"SECONDARY_CONTROL") {
				attributeState("default", label:'${currentValue} W', backgroundColor:"#ffffff")
			}
        }
        
		valueTile("energy", "device.energy", width: 2, height: 2) {
			state "default", label:'${currentValue} kWh'
		}
		standardTile("reset", "device.energy", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
			state "default", label:'reset kWh', action:"reset"
		}
		standardTile("refresh", "device.power", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
			state "default", label:'', action:"refresh.refresh", icon:"st.secondary.refresh"
		}
                
        main "FGWP"
        details(["FGWP", "energy", "reset", "refresh"])
	}
}

// parse events into attributes
def parse(String description) {
	log.debug "Parsing '${description}'"

	def cmd = zwave.parse(description, [0x25: 1, 0x31: 2, 0x32: 1, 0x72: 1, 0x86: 1])

	if (cmd) {
    	log.debug "Parsed '${cmd}'"
        zwaveEvent(cmd)
    }
}

def zwaveEvent(physicalgraph.zwave.commands.manufacturerspecificv1.ManufacturerSpecificReport cmd) { 
	log.debug "manufacturerId:   ${cmd.manufacturerId}"
    log.debug "manufacturerName: ${cmd.manufacturerName}"
    log.debug "productId:        ${cmd.productId}"
    log.debug "productTypeId:    ${cmd.productTypeId}"
}

def zwaveEvent(physicalgraph.zwave.commands.versionv1.VersionReport cmd) {	
    updateDataValue("version", "${cmd.applicationVersion}.${cmd.applicationSubVersion}")
    log.debug "applicationVersion:      ${cmd.applicationVersion}"
    log.debug "applicationSubVersion:   ${cmd.applicationSubVersion}"
    log.debug "zWaveLibraryType:        ${cmd.zWaveLibraryType}"
    log.debug "zWaveProtocolVersion:    ${cmd.zWaveProtocolVersion}"
    log.debug "zWaveProtocolSubVersion: ${cmd.zWaveProtocolSubVersion}"
}

def zwaveEvent(physicalgraph.zwave.commands.meterv1.MeterReport cmd) {
	if (cmd.scale == 0) {
		createEvent(name: "energy", value: cmd.scaledMeterValue, unit: "kWh")
	} else if (cmd.scale == 2) {
		createEvent(name: "power", value: Math.round(cmd.scaledMeterValue), unit: "W")
	}
}

def zwaveEvent(physicalgraph.zwave.commands.sensormultilevelv2.SensorMultilevelReport cmd) {
	def map = [ displayed: true ]
    if (cmd.sensorType == 4) {
    	createEvent(name: "power", value: Math.round(cmd.scaledSensorValue), unit: "W")
    }
}

def zwaveEvent(physicalgraph.zwave.commands.switchbinaryv1.SwitchBinaryReport cmd) {
	createEvent(name: "switch", value: cmd.value ? "on" : "off", type: "digital")
}

def zwaveEvent(physicalgraph.zwave.Command cmd) {
	log.debug "$device.displayName: Unhandled: $cmd"
	[:]
}

// handle commands
def configure() {
	log.debug "Executing 'configure'"
    
    def cmds = []
    cmds += zwave.manufacturerSpecificV1.manufacturerSpecificGet()
    cmds += zwave.versionV1.versionGet()
    //this group is associated automatically upon inclusion, but it is not needed
    //group 3 should be set instead
    cmds += zwave.associationV2.associationRemove(groupingIdentifier:1, nodeId:[zwaveHubNodeId])
    cmds += zwave.associationV2.associationSet(groupingIdentifier:3, nodeId:[zwaveHubNodeId])
    cmds += zwave.meterV2.meterGet(scale:0)
    cmds += zwave.meterV2.meterGet(scale:2)
    cmds += zwave.switchBinaryV1.switchBinaryGet()
    
    sequence(cmds, 500)
}

def refresh() {
	log.debug "Executing 'refresh'"
    
	def cmds = []
    cmds += zwave.meterV2.meterGet(scale:0)
    cmds += zwave.meterV2.meterGet(scale:2)
    cmds += zwave.switchBinaryV1.switchBinaryGet()
    
    sequence(cmds, 500)
}

def reset() {
	log.debug "Executing 'reset'" 
    zwave.meterV2.meterReset().format()
}

def on() {
	log.debug "Executing 'on'"
    
    def cmds = []
    cmds += zwave.switchBinaryV1.switchBinarySet(switchValue: 0xFF)
    cmds += zwave.switchBinaryV1.switchBinaryGet()//for ZW3 version Report is not automatically sent after receiving Set frame
	
    sequence(cmds)
}

def off() {
	log.debug "Executing 'off'"
    
    def cmds = []
    cmds += zwave.switchBinaryV1.switchBinarySet(switchValue: 0x00)
    cmds += zwave.switchBinaryV1.switchBinaryGet()//for ZW3 version Report is not automatically sent after receiving Set frame
	
    sequence(cmds)
}

private sequence(commands, delay=200) {
	delayBetween(commands.collect{ it.format() }, delay)
}