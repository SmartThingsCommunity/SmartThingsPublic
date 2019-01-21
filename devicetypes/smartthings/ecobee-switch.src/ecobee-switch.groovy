/**
 *  Ecobee Switch+
 *
 *  Copyright 2016 SmartThings
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
	definition (name: "Ecobee Switch", namespace: "smartthings", author: "SmartThings", ocfDeviceType: "oic.d.switch") {
		capability "Switch"
		capability "Refresh"
		capability "Sensor"
		capability "Health Check"
	}
    
	simulator {
		// TODO: define status and reply messages here
	}

    tiles(scale: 2) {
        multiAttributeTile(name:"rich-control", type: "generic", canChangeIcon: true){
            tileAttribute ("device.switch", key: "PRIMARY_CONTROL") {
                 attributeState "on", label:'${name}', action:"switch.off", icon:"st.Home.home30", backgroundColor:"#00a0dc", nextState:"turningOff"
                 attributeState "off", label:'${name}', action:"switch.on", icon:"st.Home.home30", backgroundColor:"#ffffff", nextState:"turningOn"
                 attributeState "turningOn", label:'${name}', action:"switch.off", icon:"st.Home.home30", backgroundColor:"#00a0dc", nextState:"turningOff"
                 attributeState "turningOff", label:'${name}', action:"switch.on", icon:"st.Home.home30", backgroundColor:"#ffffff", nextState:"turningOn"
            }
        }

        standardTile("switch", "device.switch", width: 2, height: 2, canChangeIcon: true) {
            state "on", label:'${name}', action:"switch.off", icon:"st.Home.home30", backgroundColor:"#00a0dc", nextState:"turningOff"
            state "off", label:'${name}', action:"switch.on", icon:"st.Home.home30", backgroundColor:"#ffffff", nextState:"turningOn"
            state "turningOn", label:'${name}', action:"switch.off", icon:"st.Home.home30", backgroundColor:"#00a0dc", nextState:"turningOff"
            state "turningOff", label:'${name}', action:"switch.on", icon:"st.Home.home30", backgroundColor:"#ffffff", nextState:"turningOn"
            state "offline", label:'${name}', icon:"st.Home.home30", backgroundColor:"#ff0000"
        }
        standardTile("refresh", "device.switch", inactiveLabel: false, height: 2, width: 2, decoration: "flat") {
            state "default", label:"", action:"refresh.refresh", icon:"st.secondary.refresh"
        }
        main(["switch"])
        details(["rich-control", "refresh"])
    }
}

// parse events into attributes
def parse(String description) {
    log.debug "Parsing '${description}'"
}

void initialize() {
    sendEvent(name: "DeviceWatch-Enroll", value: toJson([protocol: "cloud", scheme:"untracked"]), displayed: false) 	
}

void installed() {
    log.trace "[DTH] Executing installed() for device=${this.device.displayName}"
    initialize() 	
}

void updated() {
    log.trace "[DTH] Executing updated() for device=${this.device.displayName}"
    initialize() 	
}

//remove from the selected devices list in SM
void uninstalled() {
    log.trace "[DTH] Executing uninstalled() for device=${this.device.displayName}"
    parent?.purgeChildDevice(this)
}

def refresh() {
    log.trace "[DTH] Executing 'refresh' for ${this.device.displayName}"
    parent?.poll()
}

def on() {
    log.trace "[DTH] Executing 'on' for ${this.device.displayName}"
    boolean desiredState = true
    parent.controlSwitch( this.device.deviceNetworkId, desiredState )
}

def off() {
    log.trace "[DTH] Executing 'off' for ${this.device.displayName}"
    boolean desiredState = false
    parent.controlSwitch( this.device.deviceNetworkId, desiredState )
}

def toJson(Map m) {
    return groovy.json.JsonOutput.toJson(m)
}
