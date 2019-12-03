/*****************************************************************************************************************
 *  Copyright: Nick Veenstra
 *
 *  Name: GreenWave PowerNode 6 Child Device
 *
 *  Date: 2018-01-04
 *
 *  Version: 1.00
 *
 *  Source and info: https://github.com/CopyCat73/SmartThings/tree/master/devicetypes/copycat73/greenwave-powernode-6-child-device.src
 *
 *  Author: Nick Veenstra
 *  Thanks to Eric Maycock for code inspiration 
 *
 *  Description: Device handler for the GreenWave PowerNode (multi socket) Z-Wave power outlet child nodes
 *
 *  License:
 *   Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *   on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *   for the specific language governing permissions and limitations under the License.
 *
 *****************************************************************************************************************/
metadata {
	definition (name: "GreenWave PowerNode 6 Child Device", namespace: "copycat73", author: "Nick Veenstra") {
		capability "Switch"
        capability "Energy Meter"
        capability "Power Meter"
        capability "Refresh"
        
        attribute "lastupdate", "string"
        command "reset"
	}

	tiles(scale: 2) {
        multiAttributeTile(name:"switch", type: "lighting", width: 6, height: 4, canChangeIcon: true){
            tileAttribute ("device.switch", key: "PRIMARY_CONTROL") {
                attributeState "off", label:'${name}', action:'switch.on', icon:"st.switches.switch.off", backgroundColor:"#ffffff", nextState:"turningOn"
                attributeState "on", label:'${name}', action:'switch.off', icon:"st.switches.switch.on", backgroundColor:"#00a0dc", nextState:"turningOff"
                attributeState "turningOff", label:'${name}', action:'switch.on', icon:"st.switches.switch.off", backgroundColor:"#ffffff", nextState:"turningOn"
                attributeState "turningOn", label:'${name}', action:'switch.off', icon:"st.switches.switch.on", backgroundColor:"#00a0dc", nextState:"turningOff"
            }
            tileAttribute ("statusText", key: "SECONDARY_CONTROL") {
                attributeState "statusText", label:'${currentValue}'
            }
        }
        valueTile("power", "device.power", width: 3, height: 1,decoration: "flat") {
						state "default", label:'${currentValue} W' 
		}
		valueTile("energy", "device.energy", width: 3, height: 1, decoration: "flat") {
			state "default", label:'${currentValue} kWh'
		}
        standardTile("refresh", "device.refresh", width: 2, height: 2, inactiveLabel: false, decoration: "flat") {
 			state "default", action:"refresh.refresh", icon:"st.secondary.refresh"
 		}  
                
        standardTile("reset", "device.reset", , width: 2, height: 2, inactiveLabel: false, decoration: "flat") {
			state "default", label:"reset kWh", action:"reset"
		}
  		valueTile("lastupdate", "lastupdate", width: 2, height: 2, inactiveLabel: false) { 			
          state "default", label:"Last updated: " + '${currentValue}' 		
		}                  
        main (["switch"])
 		details(["switch", "power","energy", "lastupdate", "refresh", "reset"])
	}

}

def installed() {
    log.debug "Greenwave child installed"
}

def updated() {
    log.debug "Greenwave child updated"
    //log.debug "device name $device.label"
    parent.updateChildLabel(splitChannel(device.deviceNetworkId))
    
}

def on() {
	parent.switchOn(splitChannel(device.deviceNetworkId))
}

def off() {
	parent.switchOff(splitChannel(device.deviceNetworkId))
}

def refresh() {
	log.debug "refresh called for $device.deviceNetworkId"
	parent.pollNode(splitChannel(device.deviceNetworkId))
}

def reset() {
	log.debug "reset called for $device.deviceNetworkId"
	parent.resetNode(splitChannel(device.deviceNetworkId))
}

private splitChannel(String channel) {
    channel.split("-e")[-1] as Integer
}