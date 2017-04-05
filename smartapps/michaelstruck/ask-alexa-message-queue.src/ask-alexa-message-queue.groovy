/**
 *  Ask Alexa Message Queue Extension
 *
 *  Copyright © 2017 Michael Struck
 *  Version 1.0.0 3/31/17
 * 
 *  Version 1.0.0 - Initial release
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
    iconUrl: "https://raw.githubusercontent.com/MichaelStruck/SmartThingsPublic/master/smartapps/michaelstruck/ask-alexa-message-queue.src/ext.png",
    iconX2Url: "https://raw.githubusercontent.com/MichaelStruck/SmartThingsPublic/master/smartapps/michaelstruck/ask-alexa-message-queue.src/ext@2x.png",
    iconX3Url: "https://raw.githubusercontent.com/MichaelStruck/SmartThingsPublic/master/smartapps/michaelstruck/ask-alexa-message-queue.src/ext@2x.png",
    )
preferences {
    page name:"mainPage"
}
//Show main page
def mainPage() {
	dynamicPage(name: "mainPage", title:"Ask Alexa Message Queue Options", install: true, uninstall: true) {
        section {
        	label title:"Message Queue Name (Required)", required: true, image: parent.imgURL() + "mailbox.png"
        }   
        section ("Message queue options"){ input "msgQueueOrder", "enum", title: "Message Play Back Order (Alexa)", options:[0:"Oldest to newest", 1:"Newest to oldest"], defaultValue: 0 }
        section ("Message notification - Audio", hideable: true, hidden: !(mqSpeaker||mqSynth)){
        	input "mqSpeaker", "capability.musicPlayer", title: "Choose Speakers", multiple: true, required: false, submitOnChange: true
            if (mqSpeaker) input "mqVolume", "number", title: "Speaker Volume", description: "0-100%", required: false
            input "mqSynth", "capability.speechSynthesis", title: "Choose Voice Synthesis Devices", multiple: true, required: false, hideWhenEmpty: true, submitOnChange: true
            if (mqSpeaker) input "mqAlertType", "enum", title:"Notification Type...", options:[0: "Notification and Message", 1: "Notification Only", 2: "Message Only"], defaultValue:0
			if (mqSpeaker || mqSynth) input "restrictAudio", "bool", title: "Apply Restrictions To Audio Notification", defaultValue: false, submitOnChange: true
        }
        section ("Message Notification - Visual", hideable: true, hidden:!(msgQueueNotifyLightsOn || msgQueueNotifycLightsOn)){
            input "msgQueueNotifyLightsOn", "capability.switch", title: "Turn On Lights When Messages Present", required:false, multiple:true, submitOnChange: true
            input "msgQueueNotifycLightsOn", "capability.colorControl", title: "Turn On/Set Colored Lights When Messages Present", required:false, multiple:true, submitOnChange: true
            if (msgQueueNotifycLightsOn) {
            	input "msgQueueNotifyColor", "enum", title: "Set Color of Message Notification", options: parent.STColors().name, multiple:false, required:false
            	input "msgQueueNotifyLevel", "number", title: "Set Level of Message Notification", defaultValue:50, required:false
            }
            if (msgQueueNotifyLightsOn || msgQueueNotifycLightsOn) {
            	input "msgQueueNotifyLightsOff", "bool", title: "Turn Off Lights When Message Queue Empty", defaultValue: false
				input "restrictVisual", "bool", title: "Apply Restrictions To Visual Notification", defaultValue: false, submitOnChange: true
            }
        }
        section ("Message notification - Mobile", hideable: true, hidden:!(mqContacts||mqSMS||mqPush||mqFeed)){
        	input ("mqContacts", "contact", title: "Send Notifications To...", required: false, submitOnChange: true) {
				input "mqSMS", "phone", title: "Send SMS Message To (Phone Number)...", required: false, submitOnChange: true
				input "mqPush", "bool", title: "Send Push Message", defaultValue: false, submitOnChange: true
            }
        	input "mqFeed", "bool", title: "Post To Notification Feed", defaultValue: false, submitOnChange: true
        	if (mqFeed || mqSMS || mqPush || mqContacts) input "restrictMobile", "bool", title: "Apply Restrictions To Mobile Notification", defaultValue: false, submitOnChange: true
        }
        if (restrictMobile || restrictVisual || restrictAudio){
            section("Message Queue Restrictions", hideable: true, hidden: !(runDay || timeStart || timeEnd || runMode || runPeople)) {            
				input "runDay", "enum", options: ["Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"], title: "Only Certain Days Of The Week...",  multiple: true, required: false, image: parent.imgURL() + "calendar.png"
				href "timeIntervalInput", title: "Only During Certain Times...", description: parent.getTimeLabel(timeStart, timeEnd), state: (timeStart || timeEnd ? "complete":null), image: parent.imgURL() + "clock.png"
				input "runMode", "mode", title: "Only In The Following Modes...", multiple: true, required: false, image: parent.imgURL() + "modes.png"
                input "runPeople", "capability.presenceSensor", title: "Only When Present...", multiple: true, required: false, submitOnChange: true, image: parent.imgURL() + "people.png"
				if (runPeople && runPeople.size()>1) input "runPresAll", "bool", title: "Off=Any Present; On=All Present", defaultValue: false
            }
        }
        section("Tap below to remove this message queue"){ }
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
def initialize() { }
//Main Handlers
def msgHandler(date, descriptionText, unit, value) {
    if (!state.msgQueue) state.msgQueue=[]
    log.debug "New message added to the '${app.label}' message queue from: " + value
	state.msgQueue<<["date":date.getTime(),"appName":value,"msg":descriptionText,"id":unit] 
    if (mqSpeaker && mqVolume && ((restrictAudio && getOkToRun())||!restrictAudio)) {
    	def msgTxt= !mqAlertType ||mqAlertType as int ==0 || mqAlertType as int ==1 ? "New message received in the the '${app.label}' message queue from : " + value : ""
		if (!mqAlertType || mqAlertType as int ==0 || mqAlertType as int==2 ) msgTxt += msgTxt ? ": "+ descriptionText : descriptionText
        mqSpeaker?.setLevel(mqVolume as int)
        def msg = textToSpeech (msgTxt, true)
        mqSpeaker?.playTrack (msg.uri)
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
			state.msgQueue.removeAll{it.appName==value && it.id==unit}
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
	def msgCount = state.msgQueue ? state.msgQueue.size() : 0, msgS= msgCount==0 || msgCount>1 ? " messages" : " message"
	if (cmd =~/play|open|undefined/){
      	if (msgCount==0) result = "You don't have any messages in the '${app.label}' queue. %M%"
        else {
        	result = "You have " + msgCount + msgS + " in the ${app.label} queue: "
            state.msgQueue.sort({it.date})
            state.msgQueue.reverse(msgQueueOrder as int? true : false)
            state.msgQueue.each{
            	def msgData= parent.timeDate(it.date)
            	result += "${msgData.msgDay} at ${msgData.msgTime}, '${it.appName}' posted the message: '${it.msg}'. "
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
def qSize(){return state.msgQueue ? state.msgQueue.size(): 0}
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
	state.msgQueue =[] 
	if (msgQueueNotifyLightsOn && msgQueueNotifyLightsOff) msgQueueNotifyLightsOn?.off()
    if (msgQueueNotifycLightsOn && msgQueueNotifyLightsOff) msgQueueNotifycLightsOn?.off()
}
//Common Code
def getOkToRun(){ def result = (!runMode || runMode.contains(location.mode)) && parent.getDayOk(runDay) && parent.getTimeOk(timeStart,timeEnd) && parent.getPeopleOk(runPeople,runPresAll) }
//Version/Copyright/Information/Help
private versionInt(){ return 100 }
private def textAppName() { return "Ask Alexa Message Queue" }	
private def textVersion() { return "Message Queue Version: 1.0.0 (03/31/2017)" }
