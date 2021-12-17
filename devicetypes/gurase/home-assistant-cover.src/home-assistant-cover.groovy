/**
 *  Home Assistant Cover
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
	definition (name: "Home Assistant Cover", namespace: "gurase", author: "Grace Mann") {
		capability "Actuator"
		capability "Polling"
		capability "Refresh"
		capability "Sensor"
		capability "Switch"
		capability "Switch Level"
		capability "Window Shade"
	}


	simulator { }

	tiles(scale: 2) {
    	multiAttributeTile(name:"shade", type: "lighting", width: 6, height: 4, canChangeIcon: true){
			tileAttribute ("device.windowShade", key: "PRIMARY_CONTROL") {
				attributeState "open", label:'${name}', action:"close", icon:"st.Home.home9", backgroundColor:"#00A0DC", nextState:"closing"
				attributeState "closed", label:'${name}', action:"open", icon:"st.Home.home9", backgroundColor:"#ffffff", nextState:"opening"
				attributeState "opening", label:'${name}', action:"open", icon:"st.Home.home9", backgroundColor:"#00A0DC", nextState:"closing"
				attributeState "closing", label:'${name}', action:"close", icon:"st.Home.home9", backgroundColor:"#ffffff", nextState:"opening"
			}
			
            tileAttribute ("device.level", key: "SLIDER_CONTROL") {
				attributeState "level", action:"presetPosition"
			}
		}
    
		standardTile("refresh", "device.switch", width: 2, height: 2, inactiveLabel: false, decoration: "flat") {
			state "default", label:'', action:"refresh.refresh", icon:"st.secondary.refresh"
		}
        
        /*valueTile("shadeLevel", "device.level", width: 2, height: 1) {
        	state "level", label: 'Shade is ${currentValue}% up'
        }
        
        controlTile("levelSliderControl", "device.level", "slider", width: 4, height: 1) {
        	state "level", action:"presetPosition"
        }

		valueTile("level", "device.level", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
			state "level", label:'${currentValue} %', unit:"%", backgroundColor:"#ffffff"
		}*/

        main(["shade"])
    	details(["shade", "shadeLevel", "levelSliderControl", "level", "refresh"])

	}
}

// handle commands
def poll() {
	parent.poll()
}

def refresh() {
	poll()
}

def open() {
	if (parent.postService("/api/services/cover/open_cover", ["entity_id": device.deviceNetworkId])) {
    	sendEvent(name: "switch", value: "on")
    }
}

def close() {
	if (parent.postService("/api/services/cover/close_cover", ["entity_id": device.deviceNetworkId])) {
    	sendEvent(name: "switch", value: "off")
    }
}

def presetPosition(percent) {
	def state = (percent == 0 ? "off" : "on")
    
    if (parent.postService("/api/services/cover/set_cover_position", ["entity_id": device.deviceNetworkId, "position": percent])) {
    	sendEvent(name: "level", value: percent)
    	sendEvent(name: "switch.setLevel", value: percent)
        sendEvent(name: "switch", value: state)
    }
}

def on() {
	open()
}

def off() {
	close()
}

def setLevel(percent) {
	presetPosition(percent)
}