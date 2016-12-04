/**
 *  Alexa Switch
 *
 *  Copyright 2016 Michael Struck
 *  Version 2.0.7 12/04/16
 *
 *  Version 1.0.0 - Initial release
 *  Version 1.1.0 - Updated the interface to better match SmartThings dimmers (thanks to @BoruGee)
 *  Version 2.0.0 - Updated to allow for sending state change regardless of switch/dimmer status;changed name of device
 *  Version 2.0.1 - No longer on/off commands sent to dimmer when level changes
 *  Version 2.0.2 - Added icons for the switch states
 *  Version 2.0.3 - Reverted back to original icons for better GUI experience
 *  Version 2.0.4 - Added dynamic feedback to user on code version of switch
 *  Version 2.0.5 - Remove state of dimmer level, allowing the switch to act as a pass through
 *  Version 2.0.6 - Added PNG style icons to better differenciate the Alexa Helper created devices
 *  Version 2.0.7 - Added capabilities Actuator and Sensor for compability with ActionTiles.
 * 
 *  Uses code from SmartThings
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
        definition (name: "Alexa Switch", namespace: "MichaelStruck", author: "SmartThings") {
        capability "Switch"
        capability "Switch Level"
		capability "Actuator"	//included to give compatibility with ActionTiles
        capability "Sensor"		//included to give compatibility with ActionTiles
        
		attribute "about", "string"
    }

	// simulator metadata
	simulator {
	}

	// UI tile definitions
	tiles(scale: 2) {
		multiAttributeTile(name: "switch", type: "lighting", width: 6, height: 4, canChangeIcon: true, canChangeBackground: true) {
			tileAttribute("device.switch", key: "PRIMARY_CONTROL") {
    			attributeState "off", label: '${name}', action: "switch.on", backgroundColor: "#ffffff",icon: "https://raw.githubusercontent.com/MichaelStruck/SmartThings/master/Other-SmartApps/AlexaHelper/AH-Off.png", nextState: "turningOn"
		      	attributeState "on", label: '${name}', action: "switch.off", backgroundColor: "#79b821",icon: "https://raw.githubusercontent.com/MichaelStruck/SmartThings/master/Other-SmartApps/AlexaHelper/AH-On.png",  nextState: "turningOff"
				attributeState "turningOff", label: '${name}', action: "switch.on",backgroundColor: "#ffffff", icon: "https://raw.githubusercontent.com/MichaelStruck/SmartThings/master/Other-SmartApps/AlexaHelper/AH-Off.png",  nextState: "turningOn"
		      	attributeState "turningOn", label: '${name}', action: "switch.off",backgroundColor: "#79b821", icon: "https://raw.githubusercontent.com/MichaelStruck/SmartThings/master/Other-SmartApps/AlexaHelper/AH-On.png", nextState: "turningOff"
        	}
        		tileAttribute("device.level", key: "SLIDER_CONTROL") {
            		attributeState "level", action:"switch level.setLevel"
        		}
        		tileAttribute("level", key: "SECONDARY_CONTROL") {
              		attributeState "level", label: 'Light dimmed to ${currentValue}%'
        		}    
		}
        valueTile("aboutTxt", "device.about", inactiveLabel: false, decoration: "flat", width: 6, height:2) {
            state "default", label:'${currentValue}'
		}
        valueTile("lValue", "device.level", inactiveLabel: true, height:2, width:2, decoration: "flat") {  
			state "levelValue", label:'${currentValue}%', unit:""
        }  
        standardTile("on", "device.switch", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
			state "default", label:'on', action:"switch.on", icon:"st.switches.light.on"
		}
		standardTile("off", "device.switch", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
			state "default", label:'off', action:"switch.off", icon:"st.switches.light.off"
		}
		main "switch"
		details(["switch","on","lValue","off","aboutTxt"])

	}
}
def installed() {
	showVersion()	
}
def parse(String description) {
}

def on() {
	sendEvent(name: "switch", value: "on",isStateChange: true)
    log.info "Alexa Switch sent 'On' command"
    showVersion()
}

def off() {
	sendEvent(name: "switch", value: "off",isStateChange: true)
    log.info "Alexa Switch sent 'Off' command"
    showVersion()
}

def setLevel(val){
    log.info "Alexa Switch set to $val"
    
    // make sure we don't drive switches past allowed values (command will hang device waiting for it to
    // execute. Never commes back)
    if (val < 0){
    	val = 0
    }
    
    if( val > 100){
    	val = 100
    }
    
    if (val == 0){ 
    	sendEvent(name:"level",value:val,isStateChange: true)
    }
    else
    {
    	sendEvent(name:"level",value:val,isStateChange: true)
    	sendEvent(name:"switch.setLevel",value:val,isStateChange: true)
    }
}
def showVersion(){
	def versionTxt = "${appName()}: ${versionNum()}\n"
    try {if (parent.getSwitchAbout()){versionTxt += parent.getSwitchAbout()}}
    catch(e){versionTxt +="Installed from the SmartThings IDE"}
	sendEvent (name: "about", value:versionTxt) 
}
def versionNum(){
	def txt = "2.0.7 (12/04/16)"
}
def appName(){
	def txt="Alexa Switch"
}