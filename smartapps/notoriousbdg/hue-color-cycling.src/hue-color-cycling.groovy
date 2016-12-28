/**
 *  Hue Color Cycling SmartApp for SmartThings
 *
 *  Copyright (c) 2014 Brandon Gordon (https://github.com/notoriousbdg)
 *    Original Author: SmartThings
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
 *  Overview
 *  ----------------
 *  This SmartApp will cycle through Hue colors.
 *
 *  Install Steps
 *  ----------------
 *  1. Create new SmartApps at https://graph.api.smartthings.com/ide/apps using the SmartApp at https://github.com/notoriousbdg/SmartThings.HueColorCycle.
 *  2. Install the newly created SmartApp in the SmartThings mobile application.
 *  3. Configure the inputs to the SmartApp as prompted.
 *  4. Tap done.
 *  5. Enjoy...
 *
 *  Revision History
 *  ----------------
 *  2014-11-23  v0.0.1  Initial release
 *
 *  The latest version of this file can be found at:
 *    https://github.com/notoriousbdg/SmartThings.HueColorCycle
 *
 */

definition(
	name: "Hue Color Cycling",
	namespace: "notoriousbdg",
	author: "Brandon Gordon",
	description: "Cycles through multiple colors.",
	category: "Convenience",
	iconUrl: "https://s3.amazonaws.com/smartapp-icons/Partner/hue.png",
	iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Partner/hue@2x.png"
)

preferences {
	page(name: "mainPage", title: "Adjust the color of your Hue lights to match your mood.", install: true, uninstall: true)
}

def mainPage() {
	dynamicPage(name: "mainPage") {
		section("Control with switch..."){
			input "mySwitch", "capability.switch", title: "Switch?", required: true, multiple: false
		}
		section("Control these bulbs...") {
			input "hues", "capability.colorControl", title: "Which Hue Bulbs?", required:true, multiple:true
		}
		section("Options...") {
			input "lightLevel", "enum", title: "Light Level?", required: true, options: [[10:"10%"],[20:"20%"],[30:"30%"],[40:"40%"],[50:"50%"],[60:"60%"],[70:"70%"],[80:"80%"],[90:"90%"],[100:"100%"]]
			input "cycleSec", "integer", title: "Seconds between change?", required: true, defaultValue:5
			input "cycleStep", "integer", title: "Number of Hue steps per interval?", required: true, defaultValue:1
		}
		section([mobileOnly:true]) {
			label title: "Assign a name", required: false
			mode title: "Set for specific mode(s)", required: false
		}
	}
}

def installed() {
	log.debug "Installed with settings: ${settings}"
	subscribeToEvents()
}

def updated() {
	log.debug "Updated with settings: ${settings}"
	unsubscribe()
	unschedule()
	subscribeToEvents()
}

def subscribeToEvents() {
	subscribe(mySwitch, "switch.on",  eventHandlerOn)
	subscribe(mySwitch, "switch.off", eventHandlerOff)
	subscribe(hues,     "switch.on",  eventHandlerOn)
	subscribe(hues,     "switch.off", eventHandlerOff)
    if (mySwitch.switch == "on") {
    	myRunIn(1, "nextColor")
    }
}

def eventHandlerOn(evt) {
	log.trace "eventHandlerOn($evt.name: $evt.value)"
	hues?.on()
    //nextColor
    myRunIn(1, "nextColor")
}

def eventHandlerOff(evt) {
	log.trace "eventHandlerOff($evt.name: $evt.value)"
	unschedule()
    hues?.off()
}

def nextColor() {
	if (settings.cycleStep < 1) { settings.cycleStep = 1 }
	if (settings.cycleSec  < 1) { settings.cycleSec  = 1 }
	
	//Loop through Hue Colors (0-100)
    def previousHueColor = state.hueColor as Integer
    def hueColor = 0
    if (previousHueColor == null) {
    	hueColor = 0
    } else if (previousHueColor >= 0 && previousHueColor < 100) {
    	hueColor = (previousHueColor as Integer) + (settings.cycleStep as Integer)
    } else {
    	hueColor = 0
    }
	if (hueColor > 100) { hueColor = 0 }
	state.hueColor = hueColor
    
	def saturation = 100
	
	def newValue = [hue: hueColor, saturation: saturation, level: lightLevel as Integer ?: 100]
	log.debug "new value = $newValue"

	hues*.setColor(newValue)
    myRunIn(1, "nextColor")
}

private def myRunIn(delay_s, func) {
    if (delay_s > 0) {
        def tms = now() + (delay_s * 1000)
        def date = new Date(tms)
        runOnce(date, func)
        log.trace("runOnce() scheduled for ${date}")
    }
}