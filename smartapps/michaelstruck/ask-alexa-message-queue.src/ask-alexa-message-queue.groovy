/**
 *  Ask Alexa Message Queue Extension
 *
 *  Copyright © 2017 Michael Struck
 *  Version 1.0.4 7/8/17
 * 
 *  Version 1.0.0 (3/31/17) - Initial release
 *  Version 1.0.1 (4/12/17) - Refresh macro list after update from child app (for partner integration)
 *  Version 1.0.2 (5/30/17) - Added "overwrite:[true/false]" and "notifyOnly:[true/false] parameters to message queue functions, 
 *  added sound effects alerting, Alexa notification placeholder, option to suppress time/date from Message Queue playback
 *  Version 1.0.3 (6/12/17) - Added logging feature for added partner usage
 *  Version 1.0.4 (7/8/17) - Added REST URL access to Message Queue
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
    name: "Ask Alexa Message Queue",
    namespace: "MichaelStruck",
    author: "Michael Struck",
    description: "Extension Application of Ask Alexa. Do not install directly from the Marketplace",
    category: "My Apps",
    parent: "MichaelStruck:Ask Alexa",
    iconUrl: "https://raw.githubusercontent.com/MichaelStruck/SmartThingsPublic/master/img/ext.png",
    iconX2Url: "https://raw.githubusercontent.com/MichaelStruck/SmartThingsPublic/master/img/ext@2x.png",
    iconX3Url: "https://raw.githubusercontent.com/MichaelStruck/SmartThingsPublic/master/img/ext@2x.png",
    )
preferences {
    page name:"mainPage"
    page name:"pageMQURL"
}
//Show main page
def mainPage() {
	dynamicPage(name: "mainPage", title:"Ask Alexa Message Queue Options", install: true, uninstall: true) {
        section {
        	label title:"Message Queue Name (Required)", required: true, image: parent.imgURL() + "mailbox.png"
        }   
        section ("Message queue options"){ 
        	input "msgQueueOrder", "enum", title: "Message Play Back Order (Alexa)", options:[0:"Oldest to newest", 1:"Newest to oldest"], defaultValue: 0 
            input "msgQueueDateSuppress", "bool", title: "Remove Time/Date From Message Review", defaultValue: false
        }
        section ("Message notification - Alexa", hideable: true, hidden: true){
			input "msgQueueNotifyAlexa", "bool", title: "Alexa Notifications (Audio and Visual)", defaultValue: false
            paragraph "This function is not yet available - Coming soon!"            
        }
        section ("Message notification - audio", hideable: true, hidden: !(mqSpeaker||mqSynth)){
        	input "mqSpeaker", "capability.musicPlayer", title: "Choose Speakers", multiple: true, required: false, submitOnChange: true
            if (mqSpeaker) input "mqVolume", "number", title: "Speaker Volume", description: "0-100%", range:"0..100", required: false
            input "mqSynth", "capability.speechSynthesis", title: "Choose Voice Synthesis Devices", multiple: true, required: false, hideWhenEmpty: true
            if (mqSpeaker) input "mqAlertType", "enum", title:"Notification Type...",options:[0: "Verbal Notification and Message", 1: "Verbal Notification Only", 2: "Message Only", 3:"Notification Sound Effect"], defaultValue:0 , submitOnChange: true
			if (mqSpeaker && mqAlertType != "3") input "mqAppendSound", "bool", title: "Prepend Sound To Verbal Notification", defaultValue: false, submitOnChange: true
            if (mqSpeaker && (mqAlertType == "3" || mqAppendSound)) input "mqAlertSound", "enum", title: "Sound Effect", required: mqAlertType == "3" ? true : false, options: parent.soundFXList(), submitOnChange: true
            if (mqSpeaker && (mqAlertType == "3" || mqAppendSound) && mqAlertSound=="custom") input "mqAlertCustom", "text", title:"URL/Location Of Custom Sound (Less Than 10 Seconds)...", required: false
            if (mqSpeaker || mqSynth) input "restrictAudio", "bool", title: "Apply Restrictions To Audio Notification", defaultValue: false, submitOnChange: true
		}
        section ("Message notification - visual", hideable: true, hidden:!(msgQueueNotifyLightsOn || msgQueueNotifycLightsOn)){
            input "msgQueueNotifyLightsOn", "capability.switch", title: "Turn On Lights When Messages Present", required:false, multiple:true, submitOnChange: true
            input "msgQueueNotifycLightsOn", "capability.colorControl", title: "Turn On/Set Colored Lights When Messages Present", required:false, multiple:true, submitOnChange: true
            if (msgQueueNotifycLightsOn) {
            	input "msgQueueNotifyColor", "enum", title: "Set Color of Message Notification", options: parent.STColors().name, multiple:false, required:false
            	input "msgQueueNotifyLevel", "number", title: "Set Level of Message Notification", defaultValue:50, required:false, range: "0..100"
            }
            if (msgQueueNotifyLightsOn || msgQueueNotifycLightsOn) {
            	input "msgQueueNotifyLightsOff", "bool", title: "Turn Off Lights When Message Queue Empty", defaultValue: false
				input "restrictVisual", "bool", title: "Apply Restrictions To Visual Notification", defaultValue: false, submitOnChange: true
            }
        }
        section ("Message notification - mobile", hideable: true, hidden:!(mqContacts||mqSMS||mqPush||mqFeed)){
        	input ("mqContacts", "contact", title: "Send Notifications To...", required: false, submitOnChange: true) {
				input "mqSMS", "phone", title: "Send SMS Message To (Phone Number)...", required: false, submitOnChange: true
				input "mqPush", "bool", title: "Send Push Message", defaultValue: false, submitOnChange: true
            }
        	input "mqFeed", "bool", title: "Post To Notification Feed", defaultValue: false, submitOnChange: true
        	if (mqFeed || mqSMS || mqPush || mqContacts) input "restrictMobile", "bool", title: "Apply Restrictions To Mobile Notification", defaultValue: false, submitOnChange: true
        }
        if (restrictMobile || restrictVisual || restrictAudio){
            section("Message queue restrictions", hideable: true, hidden: !(runDay || timeStart || timeEnd || runMode || runPeople)) {            
				input "runDay", "enum", options: ["Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"], title: "Only Certain Days Of The Week...",  multiple: true, required: false, image: parent.imgURL() + "calendar.png"
				href "timeIntervalInput", title: "Only During Certain Times...", description: parent.getTimeLabel(timeStart, timeEnd), state: (timeStart || timeEnd ? "complete":null), image: parent.imgURL() + "clock.png"
				input "runMode", "mode", title: "Only In The Following Modes...", multiple: true, required: false, image: parent.imgURL() + "modes.png"
                input "runPeople", "capability.presenceSensor", title: "Only When Present...", multiple: true, required: false, submitOnChange: true, image: parent.imgURL() + "people.png"
				if (runPeople && runPeople.size()>1) input "runPresAll", "bool", title: "Off=Any Present; On=All Present", defaultValue: false
            }
        }
        section ("REST URL for this message queue", hideable: true, hidden:true){
        	href "pageMQURL", title:"Tap To Send REST URL For This Message Queue To Live Logging", description: none, image:parent.imgURL()+"info.png"
        }
        section("Tap below to remove this message queue"){ }
	}
}
def pageMQURL(){
    dynamicPage(name: "pageMQURL", install: false, uninstall: false) {
    	section{
        	paragraph "Please check your Live Logging to copy this URL to your messaging application", image: parent.imgURL()+"info.png"
         	paragraph "${parent.getExtAddr(app.id)}"
        	log.info "Message Queue URL: " + parent.getExtAddr(app.id)
		}
	}
}
page(name: "timeIntervalInput", title: "Only during a certain time") {
	section {
		input "timeStart", "time", title: "Starting", required: false
		input "timeEnd", "time", title: "Ending", required: false
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
	sendLocationEvent(name: "askAlexaMQ", value: "refresh", data: [queues: parent.getMQListID(false)] , isStateChange: true, descriptionText: "Ask Alexa message queue list refresh")
}
//Main Handlers
def msgHandler(date, descriptionText, unit, value, overwrite, expires, notifyOnly, suppressTimeDate, trackDelete) {
    if (!state.msgQueue) state.msgQueue=[]
    if (overwrite && parent.msgQueueDelete && parent.msgQueueDelete.contains(app.id)) msgDeleteHandler(unit, value)
    else if (overwrite) log.debug "An overwrite command was issued from '${value}', however, the option to allow deletions was not enabled for the '${app.label}' queue."
    if (!notifyOnly) log.debug "New message added to the '${app.label}' message queue from: " + value
	if (!notifyOnly) state.msgQueue<<["date":date.getTime(),"appName":value,"msg":descriptionText,"id":unit,"expires":expires,"suppressTimeDate": suppressTimeDate, "trackDelete":trackDelete] 
    if (mqSpeaker && mqVolume && ((restrictAudio && getOkToRun())||!restrictAudio)) {
		def msgVoice, msgSFX
		if (mqAlertType ==~/0|1|2/) {
			def msgTxt= !mqAlertType ||mqAlertType as int ==0 || mqAlertType as int ==1 ? "New message received in primary message queue from : " + value : ""
			if (!mqAlertType || mqAlertType ==~/0|2/) msgTxt += msgTxt ? ": "+ descriptionText : descriptionText
			msgVoice = textToSpeech (msgTxt, true)
		}
		if (mqAlertType == "3" || mqAppendSound) msgSFX = parent.sfxLookup(mqAlertSound)
		mqSpeaker?.setLevel(mqVolume as int)            
		if (mqAlertType != "3" && !mqAppendSound) mqSpeaker?.playTrack (msgVoice.uri)
		if (mqAlertType == "3") mqSpeaker?.playTrack (msgSFX.uri)
		if (mqAlertType != "3" && mqAppendSound)  mqSpeaker?.playSoundAndTrack(msgSFX.uri,msgSFX.duration,msgVoice.uri)
	}
    if (mqSynth && ((restrictAudio && getOkToRun())||!restrictAudio)) mqSynth?.speak(msgTxt)
    if (mqPush || mqSMS || mqContacts && ((restrictMobile && getOkToRun())||!restrictMobile)){
    	def mqMsg = "New message received by Ask Alexa in the '${app.label}' message queue from : " + value + ": "+ descriptionText
    	parent.sendMSG(mqSMS, mqMsg , mqPush, mqContacts)
	}
    if (mqFeed && ((restrictMobile && getOkToRun())||!restrictMobile)) sendNotificationEvent("New message received by Ask Alexa in the '${app.label}' message queue from : " + value + ": "+ descriptionText)
    if (msgQueueNotifyLightsOn && ((restrictVisual && getOkToRun())||!restrictVisual)) msgQueueNotifyLightsOn?.on()
    if (msgQueueNotifycLightsOn && (msgQueueNotifyColor || msgQueueNotifyLevel) && ((restrictVisual && getOkToRun())||!restrictVisual)) {
        def level = !msgQueueNotifyLevel || msgQueueNotifyLevel < 0 ? 50 : msgQueueNotifyLevel >100 ? 100 : msgQueueNotifyLevel as int
        msgQueueNotifyColor ? parent.setColoredLights(msgQueueNotifycLightsOn, msgQueueNotifyColor, msgQueueNotifyLevel) : msgQueueNotifycLightsOn?.setLevel(level)
	}
}
def msgDeleteHandler(unit, value){
	if (state.msgQueue && state.msgQueue.size()>0){
		if (unit && value){
			log.debug value + " is requesting to delete messages from the '${app.label}' message queue."
            def deleteList = state.msgQueue.findAll{it.appName==value && it.id==unit && it.trackDelete}
			state.msgQueue.removeAll{it.appName==value && it.id==unit}
            if (deleteList){
            	deleteList.each{
                	sendLocationEvent(name:"askAlexaMQ", value: "${it.appName}.${it.id}",isStateChange: true, data:[[deleteType: "delete"],[queue:app.label]], descriptionText:"Ask Alexa deleted messages from the '${app.label}' message queue")
                }
            }
            if (msgQueueNotifyLightsOn && msgQueueNotifyLightsOff && !state.msgQueue) msgQueueNotifyLightsOn?.off()
            if (msgQueueNotifycLightsOn && msgQueueNotifyLightsOff && !state.msgQueue) msgQueueNotifycLightsOn?.off()
		}
		else log.debug "Incorrect delete parameters sent to '${app.label}' message queue. Nothing was deleted"
	} 
	else log.debug "The '${app.label}' message queue is empty. No messages were deleted."
}
//Message Queue Reply
def msgQueueReply(cmd){
	log.debug "-'${app.label}' Message Queue Response-"
    log.debug "Message Queue Command: " + cmd
    String result = ""
	purgeMQ()
    def msgCount = state.msgQueue ? state.msgQueue.size() : 0, msgS= msgCount==0 || msgCount>1 ? " messages" : " message"
	if (cmd =~/play|open|undefined/){
      	if (msgCount==0) result = "You don't have any messages in the '${app.label}' queue. %M%"
        else {
        	result = "You have " + msgCount + msgS + " in the ${app.label} queue: "
            state.msgQueue.sort({it.date})
            state.msgQueue.reverse(msgQueueOrder as int? true : false)
            state.msgQueue.each{
            	def msgData= parent.timeDate(it.date), msgTimeDate = msgQueueDateSuppress || it.suppressTimeDate ? "" : "${msgData.msgDay} at ${msgData.msgTime}, "
            	result += "${msgTimeDate}'${it.appName}' posted the message: '${it.msg}'. "
			}
			result +="%M%"
		}
	}
	else if (cmd =~ /clear|delete|erase/) {
		qDelete()
        result="I have deleted all of the messages from the '${app.label}' message queue. %M%"
	}
	else result="For the '${app.label}' message queue, be sure to give a 'play' or 'delete' command. %1%"
    return result 
}
//Called from parent app
def qSize(){
	purgeMQ()
    return state.msgQueue ? state.msgQueue.size(): 0
}
def MQGUI(){
    def msgRpt = ""
	state.msgQueue.sort({it.date})
	state.msgQueue.reverse(msgQueueOrder as int? true : false)
	state.msgQueue.each{
    	def msgData= parent.timeDate(it.date)
        msgRpt += "● ${msgData.msgDay} at ${msgData.msgTime} From: '${it.appName}' : '${it.msg}'\n"
	}
    return msgRpt
}
def qDelete() {
	def deleteList = state.msgQueue.findAll{it.trackDelete}
	state.msgQueue =[]
    if (deleteList){
    	deleteList.each{
			sendLocationEvent(name:"askAlexaMQ", value: "${it.appName}.${it.id}",isStateChange: true, data:[[deleteType: "delete all"],[queue:app.label]], descriptionText:"Ask Alexa deleted all messages from the '${app.label}' message queue")
        }
	}
	if (msgQueueNotifyLightsOn && msgQueueNotifyLightsOff) msgQueueNotifyLightsOn?.off()
    if (msgQueueNotifycLightsOn && msgQueueNotifyLightsOff) msgQueueNotifycLightsOn?.off()
}
def purgeMQ(){
	if (!state.msgQueue) state.msgQueue=[]
    log.debug "Ask Alexa is purging expired messages from the '${app.label}' Message Queue."
    def deleteList = state.msgQueue.findAll{it.expires !=0 && now() > it.expires && it.trackDelete}
	state.msgQueue.removeAll{it.expires !=0 && now() > it.expires}
    if (deleteList){
    	deleteList.each{
			sendLocationEvent(name:"askAlexaMQ", value: "${it.appName}.${it.id}",isStateChange: true, data:[[deleteType: "expire"],[queue:app.label]], descriptionText:"Ask Alexa expired messages from the '${app.label}' message queue")
        }
	}
    if (!state.msgQueue.size()){
    	if (msgQueueNotifyLightsOn && msgQueueNotifyLightsOff) msgQueueNotifyLightsOn?.off()
    	if (msgQueueNotifycLightsOn && msgQueueNotifyLightsOff) msgQueueNotifycLightsOn?.off()
	}
}
//Common Code
def getOkToRun(){ def result = (!runMode || runMode.contains(location.mode)) && parent.getDayOk(runDay) && parent.getTimeOk(timeStart,timeEnd) && parent.getPeopleOk(runPeople,runPresAll) }
//Version/Copyright/Information/Help
private versionInt(){ return 104 }
private def textAppName() { return "Ask Alexa Message Queue" }	
private def textVersion() { return "Message Queue Version: 1.0.4 (07/08/2017)" }