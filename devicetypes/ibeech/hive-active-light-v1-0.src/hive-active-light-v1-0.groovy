/**
 *  Hive Active Light V1.1b
 *
 *  Copyright 2016 Tom Beech
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
 * 23.11.16 - Made change to ensure that setting the brightness higher than 1 also sends the 'ON' command. Some smartapps turn bulbs on by setting the brightness to >0
 * 23.11.16 - Fixed setLevel so that it updates the devices switch state if it turned the light on or off
 * 24.11.16 - Added support for when a bulb is physicall powered off
 * 30.05.17 - Update for Hive Beekeeper API
 * 31.05.17 - Bug fix. Refresh bug prevents installation of Hive devices.
 */

metadata {
    definition (name: "Hive Active Light V1.0", namespace: "ibeech", author: "Tom Beech") {
        capability "Polling"
        capability "Switch"
        capability "Switch Level"
        capability "Refresh"
		capability "Actuator"
        capability "Sensor"                
    }

    simulator {
    }

	tiles (scale: 2){
		multiAttributeTile(name:"switch", type: "lighting", width: 6, height: 4, canChangeIcon: true){
			tileAttribute ("device.switch", key: "PRIMARY_CONTROL") {
				attributeState "on", label:'${name}', action:"switch.off", icon:"http://hosted.lifx.co/smartthings/v1/196xOn.png", backgroundColor:"#79b821", nextState:"turningOff"
				attributeState "off", label:'${name}', action:"switch.on", icon:"http://hosted.lifx.co/smartthings/v1/196xOff.png", backgroundColor:"#ffffff", nextState:"turningOn"
				attributeState "turningOn", label:'${name}', action:"switch.off", icon:"http://hosted.lifx.co/smartthings/v1/196xOn.png", backgroundColor:"#fffA62", nextState:"turningOff"
				attributeState "turningOff", label:'${name}', action:"switch.on", icon:"http://hosted.lifx.co/smartthings/v1/196xOff.png", backgroundColor:"#fffA62", nextState:"turningOn"
			}
			tileAttribute ("device.level", key: "SLIDER_CONTROL") {
				attributeState "level", action:"switch level.setLevel"
			}
			tileAttribute ("device.group", key: "SECONDARY_CONTROL") {
				attributeState "group", label: '${currentValue}'
			}
		}  
       
       	standardTile("refresh", "device.switch", width: 1, height: 1, inactiveLabel: false, decoration: "flat") {
            state "default", label:"", action:"refresh.refresh", icon:"st.secondary.refresh"
        }
        
        main(["switch"])
        details(["switch", "levelSliderControl", "refresh"])
    }
}

//parse events into attributes
def parse(value) {
    log.debug "Parsing '${value}' for ${device.deviceNetworkId}"
}

def setLevel(double value) {
    
    def val = String.format("%.0f", value)
    
    log.debug "Setting level to $val"
        
    def resp = parent.apiPOST("/nodes/warmwhitelight/${device.deviceNetworkId}", [brightness: val])
    
    if(resp.status == 404) {
		// Bulb has reported it is offline, poll for more details
        poll()
    } else { 
    
    	sendEvent(name: 'level', value: val)
    }
}

def on() {    
    def resp = parent.apiPOST("/nodes/warmwhitelight/${device.deviceNetworkId}", [status: "ON"])
    
    if(resp.status == 404) {
		// Bulb has reported it is offline, poll for more details
        poll()
    } else {    
    	sendEvent(name: 'switch', value: "on")
    }
}

def off() {

    def resp = parent.apiPOST("/nodes/warmwhitelight/${device.deviceNetworkId}", [status: "OFF"])
    
    if(resp.status == 404) {
		// Bulb has reported it is offline, poll for more details
        poll()
    } else { 
    	sendEvent(name: 'switch', value: "off")
    }
}

def installed() {
	log.debug "Executing 'installed'"
}

def refresh() {
    poll()
}

def poll() {
  log.debug "Executing 'poll'"
	def currentDevice = parent.getDeviceStatus(device.deviceNetworkId)
	if (currentDevice == []) {
		return []
	}
    log.debug "$device.name status: $currentDevice"
                        
            def state = currentDevice.state.status
            def brightness =  currentDevice.state.brightness
            def presence = currentDevice.props.online

            //brightness = String.format("%.0f", brightness)
            //temperature = String.format("%.0f", temperature)

            log.debug "State: $state"
            log.debug "Brightness: $brightness"
            log.debug "Presence: $presence"

            if(presence == "ABSENT") {
                // Bulb is not present (i.e. turned off at the switch or removed)
                sendEvent(name: 'switch', value: "off")	        
            } else {    	
                sendEvent(name: 'switch', value: state.toLowerCase())
            }

	    if(brightness) {
                sendEvent(name: 'level', value: brightness)
	    }
            
            return;
}