/**
 *  Grandfather Sonos
 *
 *  Copyright 2015 Michael Struck
 *  Version 1.0.0 12/6/16
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
*
 *
 */
definition(
    name: "Grandfather Sonos",
    namespace: "MichaelStruck",
    author: "Michael Struck",
    description: "Chimes a Sonos speaker at the top of the hour.",
    category: "Convenience",
    iconUrl: "https://raw.githubusercontent.com/MichaelStruck/SmartThings/master/IFTTT-SmartApps/App1.png",
    iconX2Url: "https://raw.githubusercontent.com/MichaelStruck/SmartThings/master/IFTTT-SmartApps/App1@2x.png",
    iconX3Url: "https://raw.githubusercontent.com/MichaelStruck/SmartThings/master/IFTTT-SmartApps/App1@2x.png")

preferences {
    page name:"mainPage"
    page name:"pageAbout"
}
//Show main page
def mainPage() {
	dynamicPage(name: "mainPage", title: "Settings", install: true, uninstall: false) {
    	section {
            input "speakers", "capability.musicPlayer", title: "Choose Sonos Speakers to use...", multiple: true , required: true, image: imgURL() + "speaker.png"
            input "volume", "num", title: "Enter the volume of the speaker(s)", image: imgURL() + "volume.png", required: false
        }
        section("Restrictions") {            
			input "runDay", "enum", options: ["Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"], title: "Only Certain Days Of The Week...",  multiple: true, required: false, image: imgURL() + "calendar.png"
        	href "timeIntervalInput", title: "Only During Certain Times...", description: getTimeLabel(timeStart, timeEnd), state: greyOutState(timeStart, timeEnd,""), image: imgURL() + "clock.png"
            input "runMode", "mode", title: "Only In The Following Modes...", multiple: true, required: false, image: imgURL() + "modes.png"
		}
        section (" ") {
        	href "pageAbout", title: "About ${textAppName()}", description: "Tap to get application version, license, instructions or to remove the application", image: imgURL() + "info.png"
		}
	}
}
def pageAbout(){
	dynamicPage(name: "pageAbout", uninstall: true) {
		section {
        	paragraph "${textAppName()}\n${textCopyright()}", image: "https://raw.githubusercontent.com/MichaelStruck/SmartThingsPublic/master/smartapps/michaelstruck/alexa-helper.src/Alexa@2x.png"
        }
        section ("SmartApp/Switch Versions") {
    		paragraph "${textVersion()}"
        }    
        section ("Apache License") {
        	paragraph "${textLicense()}"
    	}
    	section("Instructions") {
        	paragraph textHelp()
    	}
        section("Tap below to remove all scenarios, switches and application"){
        }
	}
}
page(name: "timeIntervalInput", title: "Only during a certain time") {
	section {
		input "timeStart", "time", title: "Starting", required: false
		input "timeEnd", "time", title: "Ending", required: false
	}
} 
//-----------------------------------------------------------------------
def installed() {
	log.debug "Installed with settings: ${settings}"
	//subscribe(controlSwitch, "switch", "switchHandler")
}

def updated() {
	log.debug "Updated with settings: ${settings}"
	unsubscribe()
	//subscribe(controlSwitch, "switch", "switchHandler")
}
//-----------------------------------------------------------------------
def playChime() {

}
//-----------------------------------------------------------------------
def getTimeLabel(start, end){
	def timeLabel = "Tap to set"
    if(start && end) timeLabel = "Between " + parseDate("${start}", "h:mm a") + " and " +  parseDate("${end}", "h:mm a")
    else if (start) timeLabel = "Start at " + parseDate("${start}", "h:mm a")
    else if (end) timeLabel = "End at " + parseDate("${end}", "h:mm a")
	timeLabel	
}
def greyOutState(param1, param2, param3){def result = param1 || param2 || param3 ? "complete" : ""}
//-----------------------------------------------------------------------
def imgURL() { return "https://raw.githubusercontent.com/MichaelStruck/SmartThingsPublic/master/img/" }
//Version/Copyright/Information/Help
private def textAppName() { def text = "Grandfather Sonos"}	
private def textVersion() { def version = "Version 1.0.0 (12/06/2016)" }
private def textCopyright() {  def text = "Copyright © 2016 Michael Struck" }
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
private def textHelp() {return "Plays an hourly chime based on the time and restrictions. You're welcome, Andrea!" }