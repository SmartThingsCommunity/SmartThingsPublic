/**
 *  Talking Alarm Clock-Parent
 *
 *  Copyright © 2016 Michael Struck
 *  Version 2.2.5 4/21/16
 * 
 *  Version 2.0.0 - Initial release of parent/client app. 1.4.5 was released to SmartThings production
 *  Version 2.0.1 - Changed the default of new schedules to 'enabled'
 *  Version 2.1.0 - Added momentary switch as trigger to give alarm summary
 *  Version 2.1.1 - Added information in help about %people% variable
 *  Version 2.1.2 - Added trigger switches to the verbal summary information
 *  Version 2.1.3 - GUI cleanup
 *  Version 2.2.0 - Added icon to app about page
 *  Version 2.2.1 - Code optimization
 *  Version 2.2.2 - Added icons to the main menu
 *  Version 2.2.3 - Minor syntax updates
 *  Version 2.2.4 - Minor syntax change to support triggers for the alarm
 *  Version 2.2.5 - Minor GUI changes to accomodate new mobile app structure
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
definition(
    name: "Talking Alarm Clock",
    singleInstance: true,
    namespace: "MichaelStruck",
    author: "Michael Struck",
    description: "Control various waking schedules using a Sonos speaker as an alarm.",
    category: "My Apps",
    iconUrl: "https://raw.githubusercontent.com/MichaelStruck/SmartThings/master/Other-SmartApps/Talking-Alarm-Clock/Talkingclock.png",
    iconX2Url: "https://raw.githubusercontent.com/MichaelStruck/SmartThings/master/Other-SmartApps/Talking-Alarm-Clock/Talkingclock@2x.png",
	iconX3Url: "https://raw.githubusercontent.com/MichaelStruck/SmartThings/master/Other-SmartApps/Talking-Alarm-Clock/Talkingclock@2x.png"
    )
preferences {
    page name:"mainPage"
    page name:"pageAbout"
    page name:"pageSummary"
}
//Show main page
def mainPage() {
	dynamicPage(name: "mainPage", title: "Talking Alarm Clock Schedules", install: true, uninstall: false) {
		section {
            app(name: "childSchedules", appName: "Talking Alarm Clock-Schedule", namespace: "MichaelStruck", title: "Create New Alarm Schedule...", multiple: true, 
            	image: "https://raw.githubusercontent.com/MichaelStruck/SmartThingsPublic/master/img/add.png")
		}
		section([title:"Options", mobileOnly:true]) {
            href "pageSummary", title: "View/Configure Alarm Summaries", description: none, image: "https://raw.githubusercontent.com/MichaelStruck/SmartThings/master/img/settings.png"
			href "pageAbout", title: "About ${textAppName()}", description: "Tap to get application version, license, instructions or to remove the application",
            	image: "https://raw.githubusercontent.com/MichaelStruck/SmartThings/master/img/info.png"
		}
	}
}
def pageAbout(){
	dynamicPage(name: "pageAbout", uninstall: true) {
		section {
        	paragraph "${textAppName()}\n${textCopyright()}", image: "https://raw.githubusercontent.com/MichaelStruck/SmartThings/master/Other-SmartApps/Talking-Alarm-Clock/Talkingclock@2x.png"
        }
        section ("SmartApp Versions") {
    		paragraph textVersion()
        }
        section("Apache License"){    
        	paragraph textLicense()
    	}
    	section("Instructions") {
        	paragraph textHelp()
    	}
        section("Tap below to remove all schedules and application"){
        }
	}
}
def pageSummary(){
	dynamicPage(name: "pageSummary", title:"View/Configure Alarm Summaries", uninstall: false) {
        if (childApps.size()){
            childApps.each {child ->
                section("${child.label}"){
                    paragraph "${child.getAlarmDesc()}"
                    input "${child.id}", "bool", title: "Enable This Schedule", defaultValue: true
                }
            }
		}
        else {
        	section {
           		paragraph "There are currently no alarm schedules configured"
			}
        }
        section ("Voice Summary Settings") { 
			input "summaryEnable", "bool", title: "Enable Voice Summary From SmartApp Page", defaultValue: false,  submitOnChange: true
            input "summaryEnableSW", "bool", title: "Enable Voice Summary When Switches Turned On", defaultValue: false,  submitOnChange: true
            if (summaryEnableSW){
            	input "summarySwitch", "capability.switch", title: "Choose Switches To Activate Summary", multiple: true, required: false	
            }
            if (summaryEnable || summaryEnableSW ){
                input "summarySpeaker", "capability.musicPlayer", title: "Choose A Speaker", required: false 
                input "summaryVolume", "number", title: "Set The Summary Volume", description: "0-100%", required: false 
                input "summaryDisabled", "bool", title: "Include Disabled Alarms In Summary", defaultValue: false 
                input "summaryMode", "mode", title: "Speak Summary In The Following Modes...", multiple: true, required: false
            }
 		}
    }
}
def installed() {
    initialize()
}
def updated() {
	unsubscribe() 
    initialize()
}
def initialize() {
    childApps.each {child ->
		log.info "Installed Schedules: ${child.label}"
    }
    if (summaryEnable && summarySpeaker){
    	subscribe(app, summaryHandler)
    }
    if (summaryEnableSW && summarySpeaker && summarySwitch){
    	subscribe(summarySwitch, "switch.on", summaryHandler)
    }
}
//Common modules (for child app)
def getSchedStatus(appid){
    def result  = (settings."${appid}"!= null && settings."${appid}") || settings."${appid}" == null ? "true": false
}
def summaryHandler(evt){ 
 	def summaryMsg = ""
    if (!summaryMode || summaryMode.contains(location.mode)) { 
     	if (childApps.size()){
        	childApps.each {child ->
        		if (settings."${child.id}"){
                	summaryMsg += "The alarm named ${child.label}, set ${child.getAlarmMethod()}, is enabled. "
                }
                if (!settings."${child.id}" && summaryDisabled){
                	summaryMsg += " The alarm named ${child.label}, set ${child.getAlarmMethod()}, is disabled. "
                }
			}        
        }
        else {
        	summaryMsg = "There are no alarms currently scheduled." 
        }
        summaryMsg = childApps.size() && summaryMsg ? "The following is a summary of the alarm settings. ${summaryMsg}" : "There are no alarms currently enabled to summarize."
        log.debug "Summary message = ${summaryMsg}" 
 		def summarySound = textToSpeech(summaryMsg, true) 
     	if (summaryVolume) { 
     		summarySpeaker.setLevel(summaryVolume) 
 		} 
     	summarySpeaker.playTrack(summarySound.uri) 
 	} 
}
//Version/Copyright/Information/Help
private def textAppName() {
	def text = "Talking Alarm Clock"
}	
private def textVersion() {
    def version = "Parent App Version: 2.2.5 (04/21/2016)"
    def childCount = childApps.size()
    def childVersion = childCount ? childApps[0].textVersion() : "No alarm schedules installed"
    return "${version}\n${childVersion}"
}
private def textCopyright() {
    def text = "Copyright © 2016 Michael Struck"
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
		"Within each alarm schedule you create, choose a Sonos speaker, an alarm trigger and alarm type along with " +
		"switches, dimmers and thermostat to control when the alarm is triggered. Routines and modes can be triggered at alarm time. "+
		"You also have the option of setting up different alarm sounds, tracks and a personalized spoken greeting that can include a weather report. " +
		"Variables that can be used in the voice greeting include %people%, %day%, %time% and %date%.\n\n"+
		"From the main SmartApp convenience page, tapping the 'Talking Alarm Clock' icon (if enabled within the app) will "+ 
		"speak a summary of the alarms enabled or disabled without having to go into the application itself. This " + 
		"functionality is optional and can be configured from the Alarm Summary page. You may also use a real or virtual switch to get a summary." 
}