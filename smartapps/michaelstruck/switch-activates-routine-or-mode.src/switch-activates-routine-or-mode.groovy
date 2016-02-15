/**
 *  Switch Activates Routine or Mode
 *
 *  Copyright 2015 Michael Struck
 *  Version 1.0.2 9/15/15
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
 *  Ties a routine to a switch's (virtual or real) on/off state. Perfect for use with IFTTT.
 *  Simple define a switch to be used, then tie the on/off state of the switch to a specific routine.
 *  Connect the switch to an IFTTT action, and the routine will fire with the switch state change.
 *
 *
 */
definition(
    name: "Switch Activates Routine or Mode",
    namespace: "MichaelStruck",
    author: "Michael Struck",
    description: "Ties a routine or mode to a switch's state. Perfect for use with IFTTT.",
    category: "Convenience",
    iconUrl: "https://raw.githubusercontent.com/MichaelStruck/SmartThings/master/IFTTT-SmartApps/App1.png",
    iconX2Url: "https://raw.githubusercontent.com/MichaelStruck/SmartThings/master/IFTTT-SmartApps/App1@2x.png",
    iconX3Url: "https://raw.githubusercontent.com/MichaelStruck/SmartThings/master/IFTTT-SmartApps/App1@2x.png")


preferences {
	page(name: "getPref")
}
	
def getPref() {    
    dynamicPage(name: "getPref", install:true, uninstall: true) {
    	section("Choose a switch to use...") {
			input "controlSwitch", "capability.switch", title: "Switch", multiple: false, required: true
    	}
   
    	def phrases = location.helloHome?.getPhrases()*.label
			if (phrases) {
        		phrases.sort()
				section("Perform which routine when...") {
					input "phrase_on", "enum", title: "Switch is on", options: phrases, required: false
					input "phrase_off", "enum", title: "Switch is off", options: phrases, required: false
				}
			}
		section("Change to which mode when...") {
			input "onMode", "mode", title: "Switch is on", required: false
			input "offMode", "mode", title: "Switch is off", required: false 
		}
		section([mobileOnly:true], "Options") {
			label(title: "Assign a name", required: false)
    		mode title: "Set for specific mode(s)", required: false
			href "pageAbout", title: "About ${textAppName()}", description: "Tap to get application version, license and instructions"
		}
    }
}

page(name: "pageAbout", title: "About ${textAppName()}") {
        section {
            paragraph "${textVersion()}\n${textCopyright()}\n\n${textLicense()}\n"
        }
        section("Instructions") {
            paragraph textHelp()
        }
}

def installed() {
	log.debug "Installed with settings: ${settings}"
	subscribe(controlSwitch, "switch", "switchHandler")
}

def updated() {
	log.debug "Updated with settings: ${settings}"
	unsubscribe()
	subscribe(controlSwitch, "switch", "switchHandler")
}

def switchHandler(evt) {
	if (evt.value == "on" && (phrase_on || onMode)) {
    	if (phrase_on){
        	location.helloHome.execute(settings.phrase_on)
        }
        if (onMode) {
        	changeMode(onMode)
        }
    } 
    else if (evt.value == "off" && (phrase_off || offMode)) {
    	if (phrase_off){
        	location.helloHome.execute(settings.phrase_off)
    	}
        if (offMode) {
        	changeMode(offMode)
        }
    }
}

def changeMode(newMode) {
	if (location.mode != newMode) {
		if (location.modes?.find{it.name == newMode}) {
			setLocationMode(newMode)
		} else {
			log.debug "Unable to change to undefined mode '${newMode}'"
		}
	}
}

//Version/Copyright/Information/Help

private def textAppName() {
	def text = "Switch Activates Routine or Mode"
}	

private def textVersion() {
    def text = "Version 1.0.2 (09/15/2015)"
}

private def textCopyright() {
    def text = "Copyright © 2015 Michael Struck"
}

private def textLicense() {
    def text =
		"Licensed under the Apache License, Version 2.0 (the 'License'); "+
		"you may not use this file except in compliance with the License. "+
		"You may obtain a copy of the License at"+
		"\n\n"+
		"    http://www.apache.org/licenses/LICENSE-2.0"+
		"\n\n"+
		"Unless required by applicable law or agreed to in writing, software "+
		"distributed under the License is distributed on an 'AS IS' BASIS, "+
		"WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. "+
		"See the License for the specific language governing permissions and "+
		"limitations under the License."
}

private def textHelp() {
	def text =
    	"Ties a routine or mode to a switch's (virtual or real) on/off state. Perfect for use with IFTTT. "+
		"Simple define a switch to be used, then tie the on/off state of the switch to a specific routine or mode. "+
		"Connect the switch to an IFTTT action, and the routine or mode will fire with the switch state change." 
}