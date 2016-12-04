/**
 *  Google Switch
 *
 *  Copyright 2016 Michael Struck
 *  Version 1.0.0 12/4/16
 *
 *  Version 1.0.0 - Initial release
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
        definition (name: "Google Switch", namespace: "MichaelStruck", author: "SmartThings") {
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
    			attributeState "off", label: '${name}', action: "switch.on", backgroundColor: "#ffffff",icon: "https://raw.githubusercontent.com/MichaelStruck/SmartThingsPublic/master/devicetypes/michaelstruck/google-switch.src/GHH-Off.png", nextState: "turningOn"
		      	attributeState "on", label: '${name}', action: "switch.off", backgroundColor: "#79b821",icon: "https://raw.githubusercontent.com/MichaelStruck/SmartThingsPublic/master/devicetypes/michaelstruck/google-switch.src/GHH-On.png",  nextState: "turningOff"
				attributeState "turningOff", label: '${name}', action: "switch.on",backgroundColor: "#ffffff", icon: "https://raw.githubusercontent.com/MichaelStruck/SmartThingsPublic/master/devicetypes/michaelstruck/google-switch.src/GHH-Off.png",  nextState: "turningOn"
		      	attributeState "turningOn", label: '${name}', action: "switch.off",backgroundColor: "#79b821", icon: "https://raw.githubusercontent.com/MichaelStruck/SmartThingsPublic/master/devicetypes/michaelstruck/google-switch.src/GHH-On.png", nextState: "turningOff"
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
    log.info "Google Switch sent 'On' command"
    showVersion()
}

def off() {
	sendEvent(name: "switch", value: "off",isStateChange: true)
    log.info "Google Switch sent 'Off' command"
    showVersion()
}

def setLevel(val){
    log.info "Google Switch set to $val"
    
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
	def txt = "1.0.0 (12/04/16)"
}
def appName(){
	def txt="Google Switch"
}