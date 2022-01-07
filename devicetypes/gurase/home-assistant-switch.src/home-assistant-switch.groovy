/**
 *  Home Assistant Switch
 *
 *  Copyright 2017 Grace Mann
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
	definition (name: "Home Assistant Switch", namespace: "gurase", author: "Grace Mann") {
		capability "Actuator"
		capability "Light"
        capability "Polling"
		capability "Refresh"
		capability "Sensor"
		capability "Switch"
	}


	simulator { }

	tiles(scale: 2) {
        multiAttributeTile(name:"rich-control", type: "switch", canChangeIcon: true){
            tileAttribute ("device.switch", key: "PRIMARY_CONTROL") {
                 attributeState "on", label:'${name}', action:"switch.off", icon:"st.Home.home30", backgroundColor:"#00A0DC", nextState:"turningOff"
                 attributeState "off", label:'${name}', action:"switch.on", icon:"st.Home.home30", backgroundColor:"#ffffff", nextState:"turningOn"
                 attributeState "turningOn", label:'${name}', action:"switch.off", icon:"st.Home.home30", backgroundColor:"#00A0DC", nextState:"turningOff"
                 attributeState "turningOff", label:'${name}', action:"switch.on", icon:"st.Home.home30", backgroundColor:"#ffffff", nextState:"turningOn"
                 attributeState "offline", label:'${name}', icon:"st.Home.home30", backgroundColor:"#cccccc"
 			}
        }

        standardTile("switch", "device.switch", width: 2, height: 2, canChangeIcon: true) {
            state "on", label:'${name}', action:"switch.off", icon:"st.Home.home30", backgroundColor:"#00A0DC", nextState:"turningOff"
            state "off", label:'${name}', action:"switch.on", icon:"st.Home.home30", backgroundColor:"#ffffff", nextState:"turningOn"
            state "turningOn", label:'${name}', action:"switch.off", icon:"st.Home.home30", backgroundColor:"#00A0DC", nextState:"turningOff"
            state "turningOff", label:'${name}', action:"switch.on", icon:"st.Home.home30", backgroundColor:"#ffffff", nextState:"turningOn"
            state "offline", label:'${name}', icon:"st.Home.home30", backgroundColor:"#cccccc"
        }

        standardTile("refresh", "device.switch", inactiveLabel: false, height: 2, width: 2, decoration: "flat") {
            state "default", label:"", action:"refresh.refresh", icon:"st.secondary.refresh"
        }

        main(["switch"])
        details(["rich-control", "refresh"])
    }
}

// handle commands
def poll() {
	parent.poll()
}

def refresh() {
	poll()
}

def on() {
	if (parent.postService("/api/services/homeassistant/turn_on", ["entity_id": device.deviceNetworkId])) {
    	sendEvent(name: "switch", value: "on")
    }
}

def off() {
	if (parent.postService("/api/services/homeassistant/turn_off", ["entity_id": device.deviceNetworkId])) {
    	sendEvent(name: "switch", value: "off")
    }
}