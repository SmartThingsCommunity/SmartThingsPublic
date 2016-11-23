/**
 *  Hive Active Light Tunable V1.0.1
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
 */

metadata {
    definition (name: "Hive Active Light Tunable V1.0", namespace: "ibeech", author: "Tom Beech") {
        capability "Polling"
        capability "Switch"
        capability "Switch Level"
        capability "Refresh"
		capability "Color Temperature"
		capability "Actuator"
        capability "Sensor"
                
        attribute "colorName", "string"
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

        valueTile("colorName", "device.colorName", height: 2, width: 4, inactiveLabel: false, decoration: "flat") {
            state "colorName", label: '${currentValue}'
        }
		controlTile("colorTempSliderControl", "device.colorTemperature", "slider", height: 1, width: 6, inactiveLabel: false, range:"(2700..6500)") {
			state "colorTemp", action:"color temperature.setColorTemperature"
		}
        valueTile("colorTemp", "device.colorTemperature", inactiveLabel: false, decoration: "flat", height: 2, width: 2) {
			state "colorTemp", label: '${currentValue}K'
		}
       	standardTile("refresh", "device.switch", width: 2, height: 2, inactiveLabel: false, decoration: "flat") {
            state "default", label:"", action:"refresh.refresh", icon:"st.secondary.refresh"
        }
        
        main(["switch"])
        details(["switch", "levelSliderControl", "colorTempSliderControl", "colorTemp", "colorName", "refresh"])
    }
}

//parse events into attributes
def parse(value) {
    log.debug "Parsing '${value}' for ${device.deviceNetworkId}"
}

private getGenericName(value){
    def genericName = "Warm White"
    if(value < 2750){
        genericName = "Extra Warm White"
    } else if(value < 3300){
        genericName = "Warm White" 
    } else if(value < 4150){
        genericName = "Moonlight"
    } else if(value < 5000){
        genericName = "Daylight"
    } else if(value < 6500){
        genericName = "Cool Light"
    }
    
    genericName
}

def setColorTemperature(value) {
	log.debug "Executing 'setColorTemperature' to ${value}"
        
    def genericName = getGenericName(value)
    
    def args = [nodes: [[attributes: [colourTemperature: [targetValue: value], colourTemperatureTransitionTime: [targetValue: "1"]]]]]                
    def resp = parent.apiPUT("/nodes/${device.deviceNetworkId}", args)
    
    sendEvent(name: "colorTemperature", value: value)    
    sendEvent(name: "colorName", value: genericName)		
}

def setLevel(double value) {
    
    def val = String.format("%.0f", value)
    
    log.debug "Setting level to $val"
    
    def onOff = "ON"
    if(val == 0) {
    	onOff = "OFF"
    }
    
    def args = [nodes: [[attributes: [state: [targetValue: onOff]], brightness: [targetValue: val], brightnessTransitionTime: [targetValue: "1"]]]]                
    def resp = parent.apiPUT("/nodes/${device.deviceNetworkId}", args)
    
    sendEvent(name: 'level', value: val)
}

def on() {    
    def args = [nodes: [	[attributes: [state: [targetValue: "ON"]]]]]                
    def resp = parent.apiPUT("/nodes/${device.deviceNetworkId}", args)
    
    sendEvent(name: 'switch', value: "on")
}

def off() {

    def args = [nodes: [	[attributes: [state: [targetValue: "OFF"]]]]]                
    def resp = parent.apiPUT("/nodes/${device.deviceNetworkId}", args)
    
    sendEvent(name: 'switch', value: "off")
}

def installed() {
	log.debug "Executing 'installed'"
}

def refresh() {
    poll()
}

def poll() {
	def resp = parent.apiGET("/nodes/${device.deviceNetworkId}")
	if (resp.status != 200) {
		log.error("Unexpected result in poll(): [${resp.status}] ${resp.data}")
		return []
	}
	data.nodes = resp.data.nodes

	def state = data.nodes.attributes.state.reportedValue[0] 
	def temperature = data.nodes.attributes.colourTemperature.reportedValue[0]
	def brightness =  data.nodes.attributes.brightness.reportedValue[0]

	brightness = String.format("%.0f", brightness)
    temperature = String.format("%.0f", temperature)

	log.debug "State: $state"
    log.debug "Temperature: $temperature"
    log.debug "Brightness: $brightness"
    
	//sendEvent(name: 'level', value: brightness)
    
    sendEvent(name: 'switch', value: state.toLowerCase())
    sendEvent(name: 'level', value: brightness)
    
    def genericName = getGenericName(value)
    sendEvent(name: "colorName", value: genericName)
    sendEvent(name: "colorTemperature", value: temperature)
    
  
}
