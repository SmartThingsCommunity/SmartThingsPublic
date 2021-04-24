/**
 *  Momentary Button Tile-Google 
 *
 *  Copyright 2016 Michael Struck
 *  Version 1.0.0 10/16/16
 *
 *  Version 1.0.0 Initial release
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
	definition (name: "Momentary Button Tile-Google", namespace: "MichaelStruck", author: "SmartThings") {
		capability "Actuator"
		capability "Switch"
		capability "Momentary"
		capability "Sensor"
        
		attribute "about", "string"
	}

	// simulator metadata
	simulator {
	}
	// UI tile definitions
	tiles(scale: 2) {
		multiAttributeTile(name: "switch", type: "generic", width: 6, height: 4, canChangeIcon: true, canChangeBackground: true) {
			tileAttribute("device.switch", key: "PRIMARY_CONTROL") {
				attributeState "off", label: 'push', action: "momentary.push", backgroundColor: "#ffffff",icon: "https://raw.githubusercontent.com/MichaelStruck/SmartThingsPublic/master/devicetypes/michaelstruck/google-switch.src/GHH-Off.png", nextState: "on"
				attributeState "on", label: 'push', action: "momentary.push", backgroundColor: "#79b821",icon: "https://raw.githubusercontent.com/MichaelStruck/SmartThingsPublic/master/devicetypes/michaelstruck/google-switch.src/GHH-On.png"
			}
        }
        valueTile("aboutTxt", "device.about", inactiveLabel: false, decoration: "flat", width: 6, height:2) {
            state "default", label:'${currentValue}'
		}
        main "switch"
		details (["switch","aboutTxt"])
	}
}
def installed() {
	showVersion()	
}

def parse(String description) {
}

def push() {
	sendEvent(name: "switch", value: "on", isStateChange: true, display: false)
	sendEvent(name: "switch", value: "off", isStateChange: true, display: false)
	sendEvent(name: "momentary", value: "pushed", isStateChange: true)
    showVersion()
}

def on() {
	push()
}

def off() {
	push()
}
def showVersion(){
	def versionTxt = "${appName()}: ${versionNum()}\n"
    try {if (parent.getSwitchAbout()){versionTxt += parent.getSwitchAbout()}}
    catch(e){versionTxt +="Installed from the SmartThings IDE"}
	sendEvent (name: "about", value:versionTxt) 
}
def versionNum(){
	def txt = "1.0.0 (10/16/16)"
}
def appName(){
	def txt="Momentary Button Tile"
}

drag_handle