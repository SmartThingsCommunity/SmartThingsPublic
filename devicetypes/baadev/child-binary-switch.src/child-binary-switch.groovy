/**
 *  Child Binary Switch
 *
 *  Copyright 2018 Alexander Belov
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
	definition (name: "Child Binary Switch", namespace: "baadev", author: "Alexander Belov") {

        capability "Refresh"
        
        command "update" 
        
        attribute "lastUpdated", "String"
        attribute "switchState", "String"
	}
    preferences {
            input "switchReverse", "enum", title: "Switch state revers from ON to OFF and from OFF to ON", description: "", defaultValue: "1", required: true, multiple: false, options:[["1":"none"], ["2":"Reverse on"]], displayDuringSetup: false
    }
	tiles(scale: 2) {
        standardTile("switchLogo", "device.switchLogo", inactiveLabel: true, decoration: "flat", width: 1, height: 1) {
            state "default", label:'', icon: "http://cdn.device-icons.smartthings.com/Home/home30-icn@2x.png"
        }
        valueTile("lastUpdated", "device.lastUpdated", decoration: "flat", width: 5, height: 1) {
        	state "default", label:'Last updated ${currentValue}'
        }
		multiAttributeTile(name: "switchState", type: "generic", width: 6, height: 4, canChangeIcon: true) {
			tileAttribute("device.switchState", key: "PRIMARY_CONTROL") {
				attributeState("switchState", label: '${currentValue}', defaultState: true, backgroundColor: "#44b621")
			}
            tileAttribute("device.refresh", inactiveLabel: false, key: "SECONDARY_CONTROL") {
            	attributeState("refresh", label: '', action:"refresh.refresh", icon:"st.secondary.refresh")
            }
		}
    }
}

// parse events into attributes
def parse(def description) {
    def cmd = zwave.parse(description)
    
	if (description.startsWith("Err")) {
		result = createEvent(descriptionText: description, isStateChange:true)
	} else if (description != "updated") {
        zwaveEvent(cmd)
        
        def nowDay = new Date().format("MMM dd", location.timeZone)
    	def nowTime = new Date().format("h:mm a", location.timeZone)
    	sendEvent(name: "lastUpdated", value: nowDay + " at " + nowTime, displayed: false)
    }
}

def zwaveEvent(physicalgraph.zwave.commands.switchbinaryv1.SwitchBinaryReport cmd) {
	def switchState = null
    log.debug cmd.value
    
    if (switchReverse == 1) {
        switchState = cmd.value > 0 ? "ON" : "OFF"    
    }
    if (switchReverse == 2) {
        switchState = cmd.value > 0 ? "OFF" : "ON"  
    }
    
    sendEvent(name: "switchState", value: switchState)
}

def refresh() {
	def cmds = []
	cmds << zwave.switchBinaryV1.switchBinaryGet().format()
    cmds << zwave.switchBinaryV1.switchBinarySet(switchValue: 255).format()
	log.debug "exec refresh: ${cmds}"
	return cmds
}
def update() {
	def cmds = []
	cmds << command(zwave.switchBinaryV1.switchBinaryGet())
    cmds << command(zwave.switchBinaryV1.switchBinarySet(switchValue: FF))
	log.debug "exec refresh: ${cmds}"
}