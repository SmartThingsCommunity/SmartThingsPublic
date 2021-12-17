/**
 *  Home Assistant Light
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
	definition (name: "Home Assistant Light", namespace: "gurase", author: "Grace Mann") {
		capability "Actuator"
		capability "Color Control"
		capability "Color Temperature"
		capability "Light"
		capability "Polling"
        capability "Refresh"
		capability "Sensor"
		capability "Switch"
		capability "Switch Level"
	}


	simulator { }

	tiles (scale: 2){
		multiAttributeTile(name:"rich-control", type: "lighting", width: 6, height: 4, canChangeIcon: true){
			tileAttribute ("device.switch", key: "PRIMARY_CONTROL") {
				attributeState "on", label:'${name}', action:"switch.off", icon:"st.lights.philips.hue-single", backgroundColor:"#00A0DC", nextState:"turningOff"
				attributeState "off", label:'${name}', action:"switch.on", icon:"st.lights.philips.hue-single", backgroundColor:"#ffffff", nextState:"turningOn"
				attributeState "turningOn", label:'${name}', action:"switch.off", icon:"st.lights.philips.hue-single", backgroundColor:"#00A0DC", nextState:"turningOff"
				attributeState "turningOff", label:'${name}', action:"switch.on", icon:"st.lights.philips.hue-single", backgroundColor:"#ffffff", nextState:"turningOn"
			}
			tileAttribute ("device.level", key: "SLIDER_CONTROL") {
				attributeState "level", action:"switch level.setLevel", range:"(0..100)"
            }
			tileAttribute ("device.color", key: "COLOR_CONTROL") {
				attributeState "color", action:"setColor"
			}
		}

        controlTile("colorTempSliderControl", "device.colorTemperature", "slider", width: 4, height: 2, inactiveLabel: false, range:"(2000..6493)") {
            state "colorTemperature", action:"color temperature.setColorTemperature"
        }

        valueTile("colorTemp", "device.colorTemperature", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
            state "colorTemperature", label: 'WHITES'
        }

		standardTile("refresh", "device.refresh", height: 2, width: 2, inactiveLabel: false, decoration: "flat") {
			state "default", label:"", action:"refresh.refresh", icon:"st.secondary.refresh"
		}

		main(["rich-control"])
		details(["rich-control", "colorTempSliderControl", "colorTemp", "reset", "refresh"])
	}
}

def poll() {
	parent.poll()
}

def refresh() {
	poll()
}

def setColor(value) {
	def hex = colorUtil.hslToHex(value.hue as int, value.saturation as int)
    def rgb = value.hex ?: colorUtil.hexToRgb(hex)
    
    if (value.red && value.green && value.blue) {
    	rgb = [value.red, value.green, value.blue]
    }
    
    if (parent.postService("/api/services/light/turn_on", ["entity_id": device.deviceNetworkId, "rgb_color": rgb])) {
    	sendEvent(name: "color", value: hex)
        sendEvent(name: "switch", value: "on")
    }
}

def setColorTemperature(value) {
	if (parent.postService("/api/services/light/turn_on", ["entity_id": device.deviceNetworkId, "kelvin": value])) {
    	sendEvent(name: "colorTemperature", value: value)
        sendEvent(name: "switch", value: "on")
    }
}

def on() {
	if (parent.postService("/api/services/light/turn_on", ["entity_id": device.deviceNetworkId])) {
    	sendEvent(name: "switch", value: "on")
    }
}

def off() {
	if (parent.postService("/api/services/light/turn_off", ["entity_id": device.deviceNetworkId])) {
    	sendEvent(name: "switch", value: "off")
    }
}

def setLevel(percent) {
	def state = (percent == 0 ? "off" : "on")
    
	if (parent.postService("/api/services/light/turn_on", ["entity_id": device.deviceNetworkId, "brightness_pct": percent])) {
    	sendEvent(name: "level", value: percent)
    	sendEvent(name: "switch.setLevel", value: percent)
        sendEvent(name: "switch", value: state)
    }
}