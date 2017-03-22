/**
 *  White Bulb
 *
 *  Copyright 2017 WEI WEI
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
	definition (name: "yeelink.light.mono1", namespace: "com.yeelight.www", author: "WEI WEI") {
		capability "Switch"
		capability "Switch Level"
        capability "Actuator"
		capability "Refresh"
		capability "Sensor"
		capability "Health Check"
		capability "Light"  
        
        command "update"
	}


	tiles(scale: 2) {
		multiAttributeTile(name:"switch", type: "lighting", width: 6, height: 4, canChangeIcon: true){
			tileAttribute ("device.switch", key: "PRIMARY_CONTROL") {
				attributeState "on", label:'${name}', action:"switch.off", icon:"st.switches.light.on", backgroundColor:"#00A0DC", nextState:"turningOff"
				attributeState "off", label:'${name}', action:"switch.on", icon:"st.switches.light.off", backgroundColor:"#ffffff", nextState:"turningOn"
				attributeState "turningOn", label:'Turning on', action:"switch.off", icon:"st.switches.light.on", backgroundColor:"#00A0DC", nextState:"turningOff"
				attributeState "turningOff", label:'Turning off', action:"switch.on", icon:"st.switches.light.off", backgroundColor:"#ffffff", nextState:"turningOn"
			}

			tileAttribute ("device.level", key: "SLIDER_CONTROL") {
				attributeState "level", action:"switch level.setLevel"
			}
		}

		standardTile("refresh", "device.switch", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
			state "default", label:"", action:"refresh.refresh", icon:"st.secondary.refresh"
		}

		main "switch"
		details(["switch", "refresh"])
	}
}

void installed() {
    log.debug "installed"
	sendEvent(name: "DeviceWatch-Enroll", value: "{\"protocol\": \"cloud\", \"scheme\":\"untracked\", \"hubHardwareId\": \"${device?.hub?.hardwareID}\"}")
}


def setLevel(percentage) {
	log.debug "setLevel ${percentage}"
    
	if (percentage < 1 && percentage > 0) {
		percentage = 1 // clamp to 1%
	}
	if (percentage == 0) {
		return off() // if the brightness is set to 0, just turn it off
	}

    def resp = parent.setBright(device.deviceNetworkId, [level: percentage])
	if (resp == 0) {
		sendEvent(name: "level", value: percentage)
		sendEvent(name: "switch.setLevel", value: percentage)
		sendEvent(name: "switch", value: "on")
    } else {
    	log.debug("set level failed")
    }
	return []
}


def on() {
	log.debug "Device setOn"

    def resp = parent.setPower(device.deviceNetworkId, [power: "on"])
    if (resp == 0) {
		sendEvent(name: "switch", value: "on")
    } else {
    	log.debug("set power failed")
    }
	
	return []
}

def off() {
    def resp = parent.setPower(device.deviceNetworkId, [power: "off"])
    if (resp == 0) {
		sendEvent(name: "switch", value: "off")
    } else {
    	log.debug("set power failed")
    }
    
    return []
}

def refresh() {
	log.debug "Executing 'refresh'"
	
    def resp = parent.getProp(device.deviceNetworkId, [])
    if (resp.status == 0) { 
		sendEvent(name: "level", value: resp.bright)
		sendEvent(name: "switch.setLevel", value: resp.bright)
		sendEvent(name: "switch", value: resp.power)
		sendEvent(name: "colorTemperature", value: resp.ct)
        sendEvent(name: "DeviceWatch-DeviceStatus", value: "online", displayed: false)
		log.debug "$device is Online"
    } else {
		sendEvent(name: "DeviceWatch-DeviceStatus", value: "offline", displayed: false)
		log.warn "$device is Offline"
    }
}

def update(prop) {
    if (prop.online == true) {         
		sendEvent(name: "DeviceWatch-DeviceStatus", value: "online", displayed: false)
        if (prop.power) 
			sendEvent(name: "switch", value: prop.power)	
        if (prop.bright) {
			sendEvent(name: "level", value: prop.bright)
			sendEvent(name: "switch.setLevel", value: prop.bright)
        } 	
    } else {
		sendEvent(name: "DeviceWatch-DeviceStatus", value: "offline", displayed: false)
    }
}