/**
 *  BIG TALKER -- Version 1.1.5 -- A SmartApp for SmartThings Home Automation System
 *  Copyright 2014-2016 - rayzur@rayzurbock.com - Brian S. Lowrance
 *  For the latest version, development and test releases visit http://www.github.com/rayzurbock
 *
 *  This SmartApp is free. Donations to support development efforts are accepted via: 
 *      -- Paypal at: rayzur@rayzurbock.com
 *      -- Paypal Donation (for supporters without a Paypal account): https://www.paypal.com/cgi-bin/webscr?cmd=_s-xclick&hosted_button_id=WKB9N9MPUGTZS
 *      -- Square Marketplace at: https://squareup.com/market/brian-lowrance#category-a58f6ff3-7380-471b-8432-7e5881654e2c
 *
 *-------------------------------------------------------------------------------------------------------------------
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *-------------------------------------------------------------------------------------------------------------------
 *  If modifying this project, please keep the above header in tact.
 *
 */
 
definition(
    name: "Big Talker",
    namespace: "rayzurbock",
    author: "rayzur@rayzurbock.com",
    description: "Let's talk about mode changes, switches, motions, and so on.",
    category: "Fun & Social",
    iconUrl: "http://rayzurbock.com/ST/icons/BigTalker-115.png",
    iconX2Url: "http://rayzurbock.com/ST/icons/BigTalker@2x-115.png",
    iconX3Url: "http://rayzurbock.com/ST/icons/BigTalker@2x-115.png")


preferences {
    page(name: "pageStart")
    page(name: "pageStatus")
    page(name: "pageTalkNow")
    page(name: "pageConfigureSpeechDeviceType")
    page(name: "pageConfigureDefaults")
    page(name: "pageConfigureEvents")
    page(name: "pageConfigMotion")
    page(name: "pageConfigSwitch")
    page(name: "pageConfigPresence")
    page(name: "pageConfigLock")
    page(name: "pageConfigContact")
    page(name: "pageConfigMode")
    page(name: "pageConfigThermostat")
	page(name: "pageConfigAcceleration")
    page(name: "pageConfigWater")
    page(name: "pageConfigSmoke")
    page(name: "pageConfigButton")
    page(name: "pageConfigTime")
    page(name: "pageHelpPhraseTokens")
//End preferences
}

def pageStart(){
    if (checkConfig()) { 
        // Do nothing here, but run checkConfig() 
    } 
    dynamicPage(name: "pageStart", title: "Big Talker", install: false, uninstall: state.installed){
        section(){
            if (!(state.configOK)) { 
                href "pageConfigureSpeechDeviceType", title:"Configure", description:"Tap to configure"
            } else {
                href "pageStatus", title:"Status", description:"Tap to view status"
                href "pageConfigureDefaults", title: "Configure Defaults", description: "Tap to configure defaults"
                href "pageConfigureEvents", title: "Configure Events", description: "Tap to configure events"
                href "pageTalkNow", title:"Talk Now", description:"Tap to setup talk now" 
            }
        }
        section("About"){
            def AboutApp = ""
            AboutApp += 'Big Talker is a SmartApp that can make your house talk depending on various triggered events.\n\n'
            AboutApp += 'Pair with a SmartThings compatible audio device such as Sonos, Ubi, LANnouncer, VLC Thing (running on your computer or Raspberry Pi) or a DLNA device using the "Generic MediaRenderer" SmartApp/Device!\n\n'
            AboutApp += 'You can contribute to the development of this SmartApp by making a PayPal donation to rayzur@rayzurbock.com or visit http://rayzurbock.com/store\n\n'
            if (!(state.appversion == null)){ 
                AboutApp += "Big Talker ${state.appversion}\nhttp://www.github.com/rayzurbock\n" 
            } else {
                AboutApp += 'Big Talker \nhttp://www.github.com/rayzurbock\n'
            }
            paragraph(AboutApp)
        }
    }
}

def pageStatus(){
    //dynamicPage(name: "pageStatus", title: "Big Talker is configured as follows:", nextPage: "pageConfigure"){
    dynamicPage(name: "pageStatus", title: "Big Talker is configured as follows:", install: false, uninstall: false){
        String enabledDevices = ""
        
        //BEGIN STATUS DEFAULTS
        enabledDevices = "Speech Device Mode:\n"
        enabledDevices += "   "
        if (state.speechDeviceType == "capability.musicPlayer") {
            enabledDevices += "musicPlayer (Sonos, VLCThing, Generic DLNA)"
        }
        if (state.speechDeviceType == "capability.speechSynthesis") {
            enabledDevices += "speechSynthesis (Ubi, LANnouncer)"
        }
        enabledDevices += "\n\n"
        enabledDevices += "Default Speech Devices:\n"
        enabledDevices += "   "
        settings.speechDeviceDefault.each(){
            enabledDevices += "${it.displayName},"
        }
        enabledDevices += "\n\n"
        if (settings.speechVolume && state.speechDeviceType == "capability.musicPlayer") {
            enabledDevices += "Adjust Volume To: ${settings.speechVolume}%"
            enabledDevices += "\n\n"
        }
        enabledDevices += "Default Modes:\n"
        enabledDevices += "   "
        settings.speechModesDefault.each(){
            enabledDevices += "${it},"
        }
        if (settings.defaultStartTime) {
            enabledDevices += "\n\n"
            def defStartTime = getTimeFromDateString(settings.defaultStartTime, true)
            def defEndTime =  getTimeFromDateString(settings.defaultEndTime, true)
            enabledDevices += "Default Allowed Talk Time:\n ${defStartTime} - ${defEndTime}"
        }
        enabledDevices += "\n\n"
        enabledDevices += "Hub ZipCode* for Weather: ${location.zipCode}\n"
        enabledDevices += "*SmartThings uses GPS to ZipCode conversion; May not be exact"
        
        section ("Defaults:"){
            paragraph enabledDevices
        }
        enabledDevices = ""
        //END STATUS DEFAULTS
  
        //BEGIN STATUS TIME SCHEDULED EVENTS GROUP 1
        if (settings.timeSlotTime1){
            enabledDevices += "AT: ${getTimeFromDateString(settings.timeSlotTime1, true)} \n"
            enabledDevices += "ON: \n"
            enabledDevices += "   "
            def i = 0
            timeSlotDays1.each() {
                enabledDevices += "${it},"
                i += 1
                if (i == 3) { enabledDevices += "\n   " }
            }
            enabledDevices += "\n"
            enabledDevices += "SAY: \n"
            enabledDevices += "   ${timeSlotOnTime1}\n"
            if (settings.timeSlotSpeechDevice1) {
                enabledDevices += "Custom Speech Device(s):\n"
                enabledDevices += "   "
                settings.timeSlotSpeechDevice1.each() {
                    enabledDevices += "${it.displayName},"
                    enabledDevices += "\n\n"
                }
            }
            if (settings.timeSlotModes1) {
                enabledDevices += "Custom mode(s):\n"
                enabledDevices += "   "
                settings.timeSlotModes1.each() {
                    enabledDevices += "${it},"
                }
            }
            if (!(enabledDevices == "")) {
                section ("Time Schedule 1:"){
                    paragraph enabledDevices
                }
            }
            enabledDevices = ""
        }
        //END STATUS TIME SCHEDULED EVENTS GROUP 1
        //BEGIN STATUS TIME SCHEDULED EVENTS GROUP 2
        if (settings.timeSlotTime2){
            enabledDevices += "AT: ${getTimeFromDateString(settings.timeSlotTime2, true)} \n"
            enabledDevices += "ON: \n"
            enabledDevices += "   "
            def i = 0
            timeSlotDays2.each() {
                enabledDevices += "${it},"
                i += 1
                if (i == 3) { enabledDevices += "\n   " }
            }
            enabledDevices += "\n"
            enabledDevices += "SAY: \n"
            enabledDevices += "   ${timeSlotOnTime2}\n"
            if (settings.timeSlotSpeechDevice2) {
                enabledDevices += "Custom Speech Device(s):\n"
                enabledDevices += "   "
                settings.timeSlotSpeechDevice2.each() {
                    enabledDevices += "${it.displayName},"
                    enabledDevices += "\n\n"
                }
            }
            if (settings.timeSlotModes2) {
                enabledDevices += "Custom mode(s):\n"
                enabledDevices += "   "
                settings.timeSlotModes2.each() {
                    enabledDevices += "${it},"
                }
            }
            if (!(enabledDevices == "")) {
                section ("Time Schedule 2:"){
                    paragraph enabledDevices
                }
            }
            enabledDevices = ""
        }
        //END STATUS TIME SCHEDULED EVENTS GROUP 2
        //BEGIN STATUS TIME SCHEDULED EVENTS GROUP 3
        if (settings.timeSlotTime3){
            enabledDevices += "AT: ${getTimeFromDateString(settings.timeSlotTime3, true)} \n"
            enabledDevices += "ON: \n"
            enabledDevices += "   "
            def i = 0
            timeSlotDays3.each() {
                enabledDevices += "${it},"
                i += 1
                if (i == 3) { enabledDevices += "\n   " }
            }
            enabledDevices += "\n"
            enabledDevices += "SAY: \n"
            enabledDevices += "   ${timeSlotOnTime3}\n"
            if (settings.timeSlotSpeechDevice3) {
                enabledDevices += "Custom Speech Device(s):\n"
                enabledDevices += "   "
                settings.timeSlotSpeechDevice3.each() {
                    enabledDevices += "${it.displayName},"
                    enabledDevices += "\n\n"
                }
            }
            if (settings.timeSlotModes3) {
                enabledDevices += "Custom mode(s):\n"
                enabledDevices += "   "
                settings.timeSlotModes3.each() {
                    enabledDevices += "${it},"
                }
            }
            if (!(enabledDevices == "")) {
                section ("Time Schedule 3:"){
                    paragraph enabledDevices
                }
            }
            enabledDevices = ""
        }
        //END STATUS TIME SCHEDULED EVENTS GROUP 3
  
        //BEGIN STATUS CONFIG MOTION GROUP 1
        if (settings.motionDeviceGroup1) {
            enabledDevices += "Devices:  \n"
            enabledDevices += "   "
            settings.motionDeviceGroup1.each() {
                enabledDevices += "${it.displayName},"
            }
            enabledDevices += "\n"
            if (settings.motionTalkActive1) {
                enabledDevices += "Say on active:\n ${settings.motionTalkActive1}\n\n"
            }
            if (settings.motionTalkInactive1) {
                enabledDevices += "Say on inactive:\n ${settings.motionTalkInactive1}\n\n"
            }
            if (settings.motionSpeechDevice1) {
                enabledDevices += "Custom Speech Device(s):\n"
                enabledDevices += "   "
                settings.motionSpeechDevice1.each() {
                    enabledDevices += "${it.displayName},"
                }
                enabledDevices += "\n\n"
            }
            if (settings.motionModes1) {
                enabledDevices += "Custom mode(s):\n"
                enabledDevices += "   "
                settings.motionModes1.each() {
                    enabledDevices += "${it},"
                }
                enabledDevices += "\n\n"
            }
            if (settings.motionStartTime1) {
                def customStartTime = getTimeFromDateString(settings.motionStartTime1, true)
                def customEndTime = getTimeFromDateString(settings.motionEndTime1, true)
                enabledDevices += "Custom Allowed Talk Time:\n ${customStartTime} - ${customEndTime}"
                customStartTime = ""
                customEndTime = ""
            }
            if (!(enabledDevices == "")) {
                section ("Motion Sensor Group 1:"){
                    paragraph enabledDevices
                }
            }
            enabledDevices = ""
        }
        //END STATUS CONFIG MOTION GROUP 1
        //BEGIN STATUS CONFIG MOTION GROUP 2
        if (settings.motionDeviceGroup2) {
            enabledDevices += "Devices:  \n"
            enabledDevices += "   "
            settings.motionDeviceGroup2.each() {
                enabledDevices += "${it.displayName},"
            }
            enabledDevices += "\n"
            if (settings.motionTalkActive2) {
                enabledDevices += "Say on active:\n ${settings.motionTalkActive2}\n\n"
            }
            if (settings.motionTalkInactive2) {
                enabledDevices += "Say on inactive:\n ${settings.motionTalkInactive2}\n\n"
            }
            if (settings.motionSpeechDevice2) {
                enabledDevices += "Custom Speech Device(s):\n"
                enabledDevices += "   "
                settings.motionSpeechDevice2.each() {
                    enabledDevices += "${it.displayName},"
                }
                enabledDevices += "\n\n"
            }
            if (settings.motionModes2) {
                enabledDevices += "Custom mode(s):\n"
                enabledDevices += "   "
                settings.motionModes2.each() {
                    enabledDevices += "${it},"
                }
                enabledDevices += "\n\n"
            }
            if (settings.motionStartTime2) {
                def customStartTime = getTimeFromDateString(settings.motionStartTime2, true)
                def customEndTime = getTimeFromDateString(settings.motionEndTime2, true)
                enabledDevices += "Custom Allowed Talk Time:\n ${customStartTime} - ${customEndTime}"
                customStartTime = ""
                customEndTime = ""
            }
            if (!(enabledDevices == "")) {
                section ("Motion Sensor Group 2:"){
                    paragraph enabledDevices
                }
            }
            enabledDevices = ""
        }
        //END STATUS CONFIG MOTION GROUP 2
        //BEGIN STATUS CONFIG MOTION GROUP 3
        if (settings.motionDeviceGroup3) {
            enabledDevices += "Devices:  \n"
            enabledDevices += "   "
            settings.motionDeviceGroup3.each() {
                enabledDevices += "${it.displayName},"
            }
            enabledDevices += "\n"
            if (settings.motionTalkActive3) {
                enabledDevices += "Say on active:\n ${settings.motionTalkActive3}\n\n"
            }
            if (settings.motionTalkInactive3) {
                enabledDevices += "Say on inactive:\n ${settings.motionTalkInactive3}\n\n"
            }
            if (settings.motionSpeechDevice3) {
                enabledDevices += "Custom Speech Device(s):\n"
                enabledDevices += "   "
                settings.motionSpeechDevice3.each() {
                    enabledDevices += "${it.displayName},"
                }
                enabledDevices += "\n\n"
            }
            if (settings.motionModes3) {
                enabledDevices += "Custom mode(s):\n"
                enabledDevices += "   "
                settings.motionModes3.each() {
                    enabledDevices += "${it},"
                }
                enabledDevices += "\n\n"
            }
            if (settings.motionStartTime3) {
                def customStartTime = getTimeFromDateString(settings.motionStartTime3, true)
                def customEndTime = getTimeFromDateString(settings.motionEndTime3, true)
                enabledDevices += "Custom Allowed Talk Time:\n ${customStartTime} - ${customEndTime}"
                customStartTime = ""
                customEndTime = ""
            }
            if (!(enabledDevices == "")) {
                section ("Motion Sensor Group 3:"){
                    paragraph enabledDevices
                }
            }
            enabledDevices = ""
        }
        //END STATUS CONFIG MOTION GROUP 3
        
        //BEGIN STATUS CONFIG SWITCH GROUP 1
        if (settings.switchDeviceGroup1) {
            enabledDevices += "Devices:  \n"
            enabledDevices += "   "
            settings.switchDeviceGroup1.each() {
                enabledDevices += "${it.displayName},"
            }
            enabledDevices += "\n\n"
            if (settings.switchTalkOn1) {
                enabledDevices += "Say when switched ON:\n ${settings.switchTalkOn1}\n\n"
            }
            if (settings.switchTalkOff1) {
                enabledDevices += "Say when switched OFF:\n ${settings.switchTalkOff1}\n\n"
            }
            if (settings.switchSpeechDevice1) {
                enabledDevices += "Custom Speech Device(s):\n"
                enabledDevices += "   "
                settings.switchSpeechDevice1.each() {
                    enabledDevices += "${it.displayName},"
                }
                enabledDevices += "\n\n"
            }
            if (settings.switchModes1) {
                enabledDevices += "Custom mode(s):\n"
                enabledDevices += "   "
                settings.switchModes1.each() {
                    enabledDevices += "${it},"
                }
                enabledDevices += "\n\n"
            }
            if (settings.switchStartTime1) {
                def customStartTime = getTimeFromDateString(settings.switchStartTime1, true)
                def customEndTime = getTimeFromDateString(settings.switchEndTime1, true)
                enabledDevices += "Custom Allowed Talk Time:\n ${customStartTime} - ${customEndTime}"
                customStartTime = ""
                customEndTime = ""
            }
            if (!(enabledDevices == "")) {
                section ("Switch Group 1:"){
                    paragraph enabledDevices
                }
            }
            enabledDevices = ""
        }
        //END STATUS CONFIG SWITCH GROUP 1
        //BEGIN STATUS CONFIG SWITCH GROUP 2
        if (settings.switchDeviceGroup2) {
            enabledDevices += "Devices:  \n"
            enabledDevices += "   "
            settings.switchDeviceGroup2.each() {
                enabledDevices += "${it.displayName},"
            }
            enabledDevices += "\n\n"
            if (settings.switchTalkOn2) {
                enabledDevices += "Say when switched ON:\n ${settings.switchTalkOn2}\n\n"
            }
            if (settings.switchTalkOff1) {
                enabledDevices += "Say when switched OFF:\n ${settings.switchTalkOff2}\n\n"
            }
            if (settings.switchSpeechDevice2) {
                enabledDevices += "Custom Speech Device(s):\n"
                enabledDevices += "   "
                settings.switchSpeechDevice2.each() {
                    enabledDevices += "${it.displayName},"
                }
                enabledDevices += "\n\n"
            }
            if (settings.switchModes2) {
                enabledDevices += "Custom mode(s):\n"
                enabledDevices += "   "
                settings.switchModes2.each() {
                    enabledDevices += "${it},"
                }
                enabledDevices += "\n\n"
            }
            if (settings.switchStartTime2) {
                def customStartTime = getTimeFromDateString(settings.switchStartTime2, true)
                def customEndTime = getTimeFromDateString(settings.switchEndTime2, true)
                enabledDevices += "Custom Allowed Talk Time:\n ${customStartTime} - ${customEndTime}"
                customStartTime = ""
                customEndTime = ""
            }
            if (!(enabledDevices == "")) {
                section ("Switch Group 2:"){
                    paragraph enabledDevices
                }
            }
            enabledDevices = ""
        }
        //END STATUS CONFIG SWITCH GROUP 2
        //BEGIN STATUS CONFIG SWITCH GROUP 3
        if (settings.switchDeviceGroup3) {
            enabledDevices += "Devices:  \n"
            enabledDevices += "   "
            settings.switchDeviceGroup3.each() {
                enabledDevices += "${it.displayName},"
            }
            enabledDevices += "\n\n"
            if (settings.switchTalkOn3) {
                enabledDevices += "Say when switched ON:\n ${settings.switchTalkOn3}\n\n"
            }
            if (settings.switchTalkOff3) {
                enabledDevices += "Say when switched OFF:\n ${settings.switchTalkOff3}\n\n"
            }
            if (settings.switchSpeechDevice3) {
                enabledDevices += "Custom Speech Device(s):\n"
                enabledDevices += "   "
                settings.switchSpeechDevice3.each() {
                    enabledDevices += "${it.displayName},"
                }
                enabledDevices += "\n\n"
            }
            if (settings.switchModes3) {
                enabledDevices += "Custom mode(s):\n"
                enabledDevices += "   "
                settings.switchModes3.each() {
                    enabledDevices += "${it},"
                }
                enabledDevices += "\n\n"
            }
            if (settings.switchStartTime3) {
                def customStartTime = getTimeFromDateString(settings.switchStartTime3, true)
                def customEndTime = getTimeFromDateString(settings.switchEndTime3, true)
                enabledDevices += "Custom Allowed Talk Time:\n ${customStartTime} - ${customEndTime}"
                customStartTime = ""
                customEndTime = ""
            }
            if (!(enabledDevices == "")) {
                section ("Switch Group 3:"){
                    paragraph enabledDevices
                }
            }
            enabledDevices = ""
        }
        //END STATUS CONFIG SWITCH GROUP 3
        
        //BEGIN STATUS CONFIG PRESENCE GROUP 1
        if (settings.presDeviceGroup1) {
            enabledDevices += "Devices:  \n"
            enabledDevices += "   "
            settings.presDeviceGroup1.each() {
                enabledDevices += "${it.displayName},"
            }
            enabledDevices += "\n\n"
            if (settings.presTalkOnArrive1) {
                enabledDevices += "Say on arrive:\n ${settings.presTalkOnArrive1}\n\n"
            }
            if (settings.presTalkOnLeave1) {
                enabledDevices += "Say on leave:\n ${settings.presTalkOnLeave1}\n\n"
            }
            if (settings.presSpeechDevice1) {
                enabledDevices += "Custom Speech Device(s):\n"
                enabledDevices += "   "
                settings.presSpeechDevice1.each() {
                    enabledDevices += "${it.displayName},"
                }
                enabledDevices += "\n\n"
            }
            if (settings.presModes1) {
                enabledDevices += "Custom mode(s):\n"
                enabledDevices += "   "
                settings.presModes1.each() {
                    enabledDevices += "${it},"
                }
                enabledDevices += "\n\n"
            }
            if (settings.presStartTime1) {
                def customStartTime = getTimeFromDateString(settings.presStartTime1, true)
                def customEndTime = getTimeFromDateString(settings.presEndTime1, true)
                enabledDevices += "Custom Allowed Talk Time:\n ${customStartTime} - ${customEndTime}"
                customStartTime = ""
                customEndTime = ""
            }
            if (!(enabledDevices == "")) {
                section ("Presence Group 1:"){
                    paragraph enabledDevices
                }
            }
            enabledDevices = ""
        }
        //END STATUS CONFIG PRESENCE GROUP 1
        //BEGIN STATUS CONFIG PRESENCE GROUP 2
        if (settings.presDeviceGroup2) {
            enabledDevices += "Devices:  \n"
            enabledDevices += "   "
            settings.presDeviceGroup2.each() {
                enabledDevices += "${it.displayName},"
            }
            enabledDevices += "\n\n"
            if (settings.presTalkOnArrive2) {
                enabledDevices += "Say on arrive:\n ${settings.presTalkOnArrive2}\n\n"
            }
            if (settings.presTalkOnLeave2) {
                enabledDevices += "Say on leave:\n ${settings.presTalkOnLeave2}\n\n"
            }
            if (settings.presSpeechDevice2) {
                enabledDevices += "Custom Speech Device(s):\n"
                enabledDevices += "   "
                settings.presSpeechDevice2.each() {
                    enabledDevices += "${it.displayName},"
                }
                enabledDevices += "\n\n"
            }
            if (settings.presModes2) {
                enabledDevices += "Custom mode(s):\n"
                enabledDevices += "   "
                settings.presModes2.each() {
                    enabledDevices += "${it},"
                }
                enabledDevices += "\n\n"
            }
            if (settings.presStartTime2) {
                def customStartTime = getTimeFromDateString(settings.presStartTime2, true)
                def customEndTime = getTimeFromDateString(settings.presEndTime2, true)
                enabledDevices += "Custom Allowed Talk Time:\n ${customStartTime} - ${customEndTime}"
                customStartTime = ""
                customEndTime = ""
            }
            if (!(enabledDevices == "")) {
                section ("Presence Group 2:"){
                    paragraph enabledDevices
                }
            }
            enabledDevices = ""
        }
        //END STATUS CONFIG PRESENCE GROUP 2
        //BEGIN STATUS CONFIG PRESENCE GROUP 3
        if (settings.presDeviceGroup3) {
            enabledDevices += "Devices:  \n"
            enabledDevices += "   "
            settings.presDeviceGroup3.each() {
                enabledDevices += "${it.displayName},"
            }
            enabledDevices += "\n\n"
            if (settings.presTalkOnArrive3) {
                enabledDevices += "Say on arrive:\n ${settings.presTalkOnArrive3}\n\n"
            }
            if (settings.presTalkOnLeave3) {
                enabledDevices += "Say on leave:\n ${settings.presTalkOnLeave3}\n\n"
            }
            if (settings.presSpeechDevice3) {
                enabledDevices += "Custom Speech Device(s):\n"
                enabledDevices += "   "
                settings.presSpeechDevice3.each() {
                    enabledDevices += "${it.displayName},"
                }
                enabledDevices += "\n\n"
            }
            if (settings.presModes3) {
                enabledDevices += "Custom mode(s):\n"
                enabledDevices += "   "
                settings.presModes3.each() {
                    enabledDevices += "${it},"
                }
                enabledDevices += "\n\n"
            }
            if (settings.presStartTime3) {
                def customStartTime = getTimeFromDateString(settings.presStartTime3, true)
                def customEndTime = getTimeFromDateString(settings.presEndTime3, true)
                enabledDevices += "Custom Allowed Talk Time:\n ${customStartTime} - ${customEndTime}"
                customStartTime = ""
                customEndTime = ""
            }
            if (!(enabledDevices == "")) {
                section ("Presence Group 3:"){
                    paragraph enabledDevices
                }
            }
            enabledDevices = ""
        }
        //END STATUS CONFIG PRESENCE GROUP 3
        
        //BEGIN STATUS CONFIG LOCK GROUP 1
        if (settings.lockDeviceGroup1) {
            enabledDevices += "Devices:  \n"
            enabledDevices += "   "
            settings.lockDeviceGroup1.each() {
                enabledDevices += "${it.displayName},"
            }
            enabledDevices += "\n\n"
            if (settings.lockTalkOnLock1) {
                enabledDevices += "Say when locked:\n ${settings.lockTalkOnLock1}\n\n"
            }
            if (settings.lockTalkOnUnlock1) {
                enabledDevices += "Say when unlocked:\n ${settings.lockTalkOnUnlock1}\n\n"
            }
            if (settings.lockSpeechDevice1) {
                enabledDevices += "Custom Speech Device(s):\n"
                enabledDevices += "   "
                settings.lockSpeechDevice1.each() {
                    enabledDevices += "${it.displayName},"
                }
                enabledDevices += "\n\n"
            }
            if (settings.lockModes1) {
                enabledDevices += "Custom mode(s):\n"
                enabledDevices += "   "
                settings.lockModes1.each() {
                    enabledDevices += "${it},"
                }
                enabledDevices += "\n\n"
            }
            if (settings.lockStartTime1) {
                def customStartTime = getTimeFromDateString(settings.lockStartTime1, true)
                def customEndTime = getTimeFromDateString(settings.lockEndTime1, true)
                enabledDevices += "Custom Allowed Talk Time:\n ${customStartTime} - ${customEndTime}"
                customStartTime = ""
                customEndTime = ""
            }
            if (!(enabledDevices == "")) {
                section ("Lock Group 1:"){
                    paragraph enabledDevices
                }
            }
            enabledDevices = ""
        }
        //END STATUS CONFIG LOCK GROUP 1
        //BEGIN STATUS CONFIG LOCK GROUP 2
        if (settings.lockDeviceGroup2) {
            enabledDevices += "Devices:  \n"
            enabledDevices += "   "
            settings.lockDeviceGroup2.each() {
                enabledDevices += "${it.displayName},"
            }
            enabledDevices += "\n\n"
            if (settings.lockTalkOnLock2) {
                enabledDevices += "Say when locked:\n ${settings.lockTalkOnLock2}\n\n"
            }
            if (settings.lockTalkOnUnlock2) {
                enabledDevices += "Say when unlocked:\n ${settings.lockTalkOnUnlock2}\n\n"
            }
            if (settings.lockSpeechDevice2) {
                enabledDevices += "Custom Speech Device(s):\n"
                enabledDevices += "   "
                settings.lockSpeechDevice2.each() {
                    enabledDevices += "${it.displayName},"
                }
                enabledDevices += "\n\n"
            }
            if (settings.lockModes2) {
                enabledDevices += "Custom mode(s):\n"
                enabledDevices += "   "
                settings.lockModes2.each() {
                    enabledDevices += "${it},"
                }
                enabledDevices += "\n\n"
            }
            if (settings.lockStartTime2) {
                def customStartTime = getTimeFromDateString(settings.lockStartTime2, true)
                def customEndTime = getTimeFromDateString(settings.lockEndTime2, true)
                enabledDevices += "Custom Allowed Talk Time:\n ${customStartTime} - ${customEndTime}"
                customStartTime = ""
                customEndTime = ""
            }
            if (!(enabledDevices == "")) {
                section ("Lock Group 2:"){
                    paragraph enabledDevices
                }
            }
            enabledDevices = ""
        }
        //END STATUS CONFIG LOCK GROUP 2
        //BEGIN STATUS CONFIG LOCK GROUP 3
        if (settings.lockDeviceGroup3) {
            enabledDevices += "Devices:  \n"
            enabledDevices += "   "
            settings.lockDeviceGroup3.each() {
                enabledDevices += "${it.displayName},"
            }
            enabledDevices += "\n\n"
            if (settings.lockTalkOnLock3) {
                enabledDevices += "Say when locked:\n ${settings.lockTalkOnLock1}\n\n"
            }
            if (settings.lockTalkOnUnlock3) {
                enabledDevices += "Say when unlocked:\n ${settings.lockTalkOnUnlock1}\n\n"
            }
            if (settings.lockSpeechDevice3) {
                enabledDevices += "Custom Speech Device(s):\n"
                enabledDevices += "   "
                settings.lockSpeechDevice3.each() {
                    enabledDevices += "${it.displayName},"
                }
                enabledDevices += "\n\n"
            }
            if (settings.lockModes3) {
                enabledDevices += "Custom mode(s):\n"
                enabledDevices += "   "
                settings.lockModes3.each() {
                    enabledDevices += "${it},"
                }
                enabledDevices += "\n\n"
            }
            if (settings.lockStartTime3) {
                def customStartTime = getTimeFromDateString(settings.lockStartTime3, true)
                def customEndTime = getTimeFromDateString(settings.lockEndTime3, true)
                enabledDevices += "Custom Allowed Talk Time:\n ${customStartTime} - ${customEndTime}"
                customStartTime = ""
                customEndTime = ""
            }
            if (!(enabledDevices == "")) {
                section ("Lock Group 3:"){
                    paragraph enabledDevices
                }
            }
            enabledDevices = ""
        }
        //END STATUS CONFIG LOCK GROUP 3
        
        //BEGIN STATUS CONFIG CONTACT GROUP 1
        if (settings.contactDeviceGroup1) {
            enabledDevices += "Devices:  \n"
            enabledDevices += "   "
            settings.contactDeviceGroup1.each() {
                enabledDevices += "${it.displayName},"
            }
            enabledDevices += "\n\n"
            if (settings.contactTalkOnOpen1) {
                enabledDevices += "Say when opened:\n ${settings.contactTalkOnOpen1}\n\n"
            }
            if (settings.contactTalkOnClose1) {
                enabledDevices += "Say when closed:\n ${settings.contactTalkOnClose1}\n\n"
            }
            if (settings.contactSpeechDevice1) {
                enabledDevices += "Custom Speech Device(s):\n"
                enabledDevices += "   "
                settings.contactSpeechDevice1.each() {
                    enabledDevices += "${it.displayName},"
                }
                enabledDevices += "\n\n"
            }
            if (settings.contactModes1) {
                enabledDevices += "Custom mode(s):\n"
                enabledDevices += "   "
                settings.contactModes1.each() {
                    enabledDevices += "${it},"
                }
            }
            if (settings.contactStartTime1) {
                def customStartTime = getTimeFromDateString(settings.contactStartTime1, true)
                def customEndTime = getTimeFromDateString(settings.contactEndTime1, true)
                enabledDevices += "Custom Allowed Talk Time:\n ${customStartTime} - ${customEndTime}"
                customStartTime = ""
                customEndTime = ""
            }
            if (!(enabledDevices == "")) {
                section ("Contact Group 1:"){
                    paragraph enabledDevices
                }
            }
            enabledDevices = ""
        }
        //END STATUS CONFIG CONTACT GROUP 1
        //BEGIN STATUS CONFIG CONTACT GROUP 2
        if (settings.contactDeviceGroup2) {
            enabledDevices += "Devices:  \n"
            enabledDevices += "   "
            settings.contactDeviceGroup2.each() {
                enabledDevices += "${it.displayName},"
            }
            enabledDevices += "\n\n"
            if (settings.contactTalkOnOpen2) {
                enabledDevices += "Say when opened:\n ${settings.contactTalkOnOpen2}\n\n"
            }
            if (settings.contactTalkOnClose2) {
                enabledDevices += "Say when closed:\n ${settings.contactTalkOnClose2}\n\n"
            }
            if (settings.contactSpeechDevice2) {
                enabledDevices += "Custom Speech Device(s):\n"
                enabledDevices += "   "
                settings.contactSpeechDevice2.each() {
                    enabledDevices += "${it.displayName},"
                }
                enabledDevices += "\n\n"
            }
            if (settings.contactModes2) {
                enabledDevices += "Custom mode(s):\n"
                enabledDevices += "   "
                settings.contactModes2.each() {
                    enabledDevices += "${it},"
                }
                enabledDevices += "\n\n"
            }
            if (settings.contactStartTime2) {
                def customStartTime = getTimeFromDateString(settings.contactStartTime2, true)
                def customEndTime = getTimeFromDateString(settings.contactEndTime2, true)
                enabledDevices += "Custom Allowed Talk Time:\n ${customStartTime} - ${customEndTime}"
                customStartTime = ""
                customEndTime = ""
            }
            if (!(enabledDevices == "")) {
                section ("Contact Group 2:"){
                    paragraph enabledDevices
                }
            }
            enabledDevices = ""
        }
        //END STATUS CONFIG CONTACT GROUP 2
        //BEGIN STATUS CONFIG CONTACT GROUP 3
        if (settings.contactDeviceGroup3) {
            enabledDevices += "Devices:  \n"
            enabledDevices += "   "
            settings.contactDeviceGroup3.each() {
                enabledDevices += "${it.displayName},"
            }
            enabledDevices += "\n\n"
            if (settings.contactTalkOnOpen3) {
                enabledDevices += "Say when opened:\n ${settings.contactTalkOnOpen3}\n\n"
            }
            if (settings.contactTalkOnClose3) {
                enabledDevices += "Say when closed:\n ${settings.contactTalkOnClose3}\n\n"
            }
            if (settings.contactSpeechDevice3) {
                enabledDevices += "Custom Speech Device(s):\n"
                enabledDevices += "   "
                settings.contactSpeechDevice3.each() {
                    enabledDevices += "${it.displayName},"
                }
                enabledDevices += "\n\n"
            }
            if (settings.contactModes3) {
                enabledDevices += "Custom mode(s):\n"
                enabledDevices += "   "
                settings.contactModes3.each() {
                    enabledDevices += "${it},"
                }
                enabledDevices += "\n\n"
            }
            if (settings.contactStartTime3) {
                def customStartTime = getTimeFromDateString(settings.contactStartTime3, true)
                def customEndTime = getTimeFromDateString(settings.contactEndTime3, true)
                enabledDevices += "Custom Allowed Talk Time:\n ${customStartTime} - ${customEndTime}"
                customStartTime = ""
                customEndTime = ""
            }
            if (!(enabledDevices == "")) {
                section ("Contact Group 3:"){
                    paragraph enabledDevices
                }
            }
            enabledDevices = ""
        }
        //END STATUS CONFIG CONTACT GROUP 3
        
        //BEGIN STATUS CONFIG MODE CHANGE GROUP 1
        if (settings.modePhraseGroup1) {
            enabledDevices += "Modes:  \n"
            enabledDevices += "   "
            settings.modePhraseGroup1.each() {
                enabledDevices += "${it},"
            }
            enabledDevices += "\n\n"
            if (settings.modeExcludePhraseGroup1) {
                enabledDevices += "Remain silent if mode is changed from:\n "
                enabledDevices += "   "
                settings.modeExcludePhraseGroup1.each(){
                    enabledDevices += "${it},"
                }
                enabledDevices += "\n\n"
            }            
            if (settings.contactTalkOnOpen1) {
                enabledDevices += "Say when changed:\n ${settings.TalkOnModeChange1}\n\n"
            }
            if (settings.modeSpeechDevice1) {
                enabledDevices += "Custom Speech Device(s):\n"
                enabledDevices += "   "
                settings.contactSpeechDevice1.each() {
                    enabledDevices += "${it.displayName},"
                }
                enabledDevices += "\n\n"
            }
            if (settings.modeStartTime1) {
                def customStartTime = getTimeFromDateString(settings.modeStartTime1, true)
                def customEndTime = getTimeFromDateString(settings.modeEndTime1, true)
                enabledDevices += "Custom Allowed Talk Time:\n ${customStartTime} - ${customEndTime}"
                customStartTime = ""
                customEndTime = ""
            }
            if (!(enabledDevices == "")) {
                section ("Mode Change:"){
                    paragraph enabledDevices
                }
            }
            enabledDevices = ""
        }
        //END STATUS CONFIG MODE CHANGE GROUP 1
        
        //BEGIN STATUS CONFIG THERMOSTAT GROUP 1
        if (settings.thermostatDeviceGroup1) {
            enabledDevices += "Devices:  \n"
            enabledDevices += "   "
            settings.thermostatDeviceGroup1.each() {
                enabledDevices += "${it.displayName},"
            }
            enabledDevices += "\n\n"
            if (settings.thermostatTalkOnIdle1) {
                enabledDevices += "Say when Idle:\n ${settings.thermostatTalkOnIdle1}\n\n"
            }
            if (settings.thermostatTalkOnHeating1) {
                enabledDevices += "Say when Heating:\n ${settings.thermostatTalkOnHeating1}\n\n"
            }
            if (settings.thermostatTalkOnCooling1) {
                enabledDevices += "Say when Cooling:\n ${settings.thermostatTalkOnCooling1}\n\n"
            }
            if (settings.thermostatTalkOnFan1) {
                enabledDevices += "Say when Fan:\n ${settings.thermostatTalkOnFan1}\n\n"
            }
            if (settings.thermostatSpeechDevice1) {
                enabledDevices += "Custom Speech Device(s):\n"
                enabledDevices += "   "
                settings.thermostatSpeechDevice1.each() {
                    enabledDevices += "${it.displayName},"
                }
                enabledDevices += "\n\n"
            }
            if (settings.thermostatModes1) {
                enabledDevices += "Custom mode(s):\n"
                enabledDevices += "   "
                settings.thermostatModes1.each() {
                    enabledDevices += "${it},"
                }
                enabledDevices += "\n\n"
            }
            if (settings.thermostatStartTime1) {
                def customStartTime = getTimeFromDateString(settings.thermostatStartTime1, true)
                def customEndTime = getTimeFromDateString(settings.thermostatEndTime1, true)
                enabledDevices += "Custom Allowed Talk Time:\n ${customStartTime} - ${customEndTime}"
                customStartTime = ""
                customEndTime = ""
            }
            if (!(enabledDevices == "")) {
                section ("Thermostat Group 1:"){
                    paragraph enabledDevices
                }
            }
            enabledDevices = ""
        }
        //END STATUS CONFIG THERMOSTAT GROUP 1
        
        //BEGIN STATUS CONFIG ACCELERATION GROUP 1
        if (settings.accelerationDeviceGroup1) {
            enabledDevices += "Devices:  \n"
            enabledDevices += "   "
            settings.accelerationDeviceGroup1.each() {
                enabledDevices += "${it.displayName},"
            }
            enabledDevices += "\n\n"
            if (settings.accelerationTalkOnActive1) {
                enabledDevices += "Say when acceleration activated:\n ${settings.accelerationTalkOnActive1}\n\n"
            }
            if (settings.accelerationTalkOnInactive1) {
                enabledDevices += "Say when acceleration stops:\n ${settings.accelerationTalkOnInactive1}\n\n"
            }
            if (settings.accelerationSpeechDevice1) {
                enabledDevices += "Custom Speech Device(s):\n"
                enabledDevices += "   "
                settings.accelerationSpeechDevice1.each() {
                    enabledDevices += "${it.displayName},"
                }
                enabledDevices += "\n\n"
            }
            if (settings.accelerationModes1) {
                enabledDevices += "Custom mode(s):\n"
                enabledDevices += "   "
                settings.accelerationModes1.each() {
                    enabledDevices += "${it},"
                }
                enabledDevices += "\n\n"
            }
            if (settings.accelerationStartTime1) {
                def customStartTime = getTimeFromDateString(settings.accelerationStartTime1, true)
                def customEndTime = getTimeFromDateString(settings.accelerationEndTime1, true)
                enabledDevices += "Custom Allowed Talk Time:\n ${customStartTime} - ${customEndTime}"
                customStartTime = ""
                customEndTime = ""
            }
            if (!(enabledDevices == "")) {
                section ("Acceleration Group 1:"){
                    paragraph enabledDevices
                }
            }
            enabledDevices = ""
        }
        //END STATUS CONFIG ACCELERATION GROUP 1
        //BEGIN STATUS CONFIG ACCELERATION GROUP 2
        if (settings.accelerationDeviceGroup2) {
            enabledDevices += "Devices:  \n"
            enabledDevices += "   "
            settings.accelerationDeviceGroup2.each() {
                enabledDevices += "${it.displayName},"
            }
            enabledDevices += "\n\n"
            if (settings.accelerationTalkOnActive2) {
                enabledDevices += "Say when acceleration activated:\n ${settings.accelerationTalkOnActive2}\n\n"
            }
            if (settings.accelerationTalkOnInactive2) {
                enabledDevices += "Say when acceleration stops:\n ${settings.accelerationTalkOnInactive2}\n\n"
            }
            if (settings.accelerationSpeechDevice2) {
                enabledDevices += "Custom Speech Device(s):\n"
                enabledDevices += "   "
                settings.accelerationSpeechDevice2.each() {
                    enabledDevices += "${it.displayName},"
                }
                enabledDevices += "\n\n"
            }
            if (settings.accelerationModes2) {
                enabledDevices += "Custom mode(s):\n"
                enabledDevices += "   "
                settings.accelerationModes2.each() {
                    enabledDevices += "${it},"
                }
                enabledDevices += "\n\n"
            }
            if (settings.accelerationStartTime2) {
                def customStartTime = getTimeFromDateString(settings.accelerationStartTime2, true)
                def customEndTime = getTimeFromDateString(settings.accelerationEndTime2, true)
                enabledDevices += "Custom Allowed Talk Time:\n ${customStartTime} - ${customEndTime}"
                customStartTime = ""
                customEndTime = ""
            }
            if (!(enabledDevices == "")) {
                section ("Acceleration Group 2:"){
                    paragraph enabledDevices
                }
            }
            enabledDevices = ""
        }
        //END STATUS CONFIG ACCELERATION GROUP 2
        //BEGIN STATUS CONFIG ACCELERATION GROUP 3
        if (settings.accelerationDeviceGroup3) {
            enabledDevices += "Devices:  \n"
            enabledDevices += "   "
            settings.accelerationDeviceGroup3.each() {
                enabledDevices += "${it.displayName},"
            }
            enabledDevices += "\n\n"
            if (settings.accelerationTalkOnActive3) {
                enabledDevices += "Say when acceleration activated:\n ${settings.accelerationTalkOnActive3}\n\n"
            }
            if (settings.accelerationTalkOnInactive3) {
                enabledDevices += "Say when acceleration stops:\n ${settings.accelerationTalkOnInactive3}\n\n"
            }
            if (settings.accelerationSpeechDevice3) {
                enabledDevices += "Custom Speech Device(s):\n"
                enabledDevices += "   "
                settings.accelerationSpeechDevice3.each() {
                    enabledDevices += "${it.displayName},"
                }
                enabledDevices += "\n\n"
            }
            if (settings.accelerationModes3) {
                enabledDevices += "Custom mode(s):\n"
                enabledDevices += "   "
                settings.accelerationModes3.each() {
                    enabledDevices += "${it},"
                }
                enabledDevices += "\n\n"
            }
            if (settings.accelerationStartTime3) {
                def customStartTime = getTimeFromDateString(settings.accelerationStartTime3, true)
                def customEndTime = getTimeFromDateString(settings.accelerationEndTime3, true)
                enabledDevices += "Custom Allowed Talk Time:\n ${customStartTime} - ${customEndTime}"
                customStartTime = ""
                customEndTime = ""
            }
            if (!(enabledDevices == "")) {
                section ("Acceleration Group 3:"){
                    paragraph enabledDevices
                }
            }
            enabledDevices = ""
        }
        //END STATUS CONFIG ACCELERATION GROUP 3
        
        //BEGIN STATUS CONFIG WATER GROUP 1
        if (settings.waterDeviceGroup1) {
            enabledDevices += "Devices:  \n"
            enabledDevices += "   "
            settings.waterDeviceGroup1.each() {
                enabledDevices += "${it.displayName},"
            }
            enabledDevices += "\n\n"
            if (settings.waterTalkOnWet1) {
                enabledDevices += "Say this when wet:\n ${settings.waterTalkOnWet1}\n\n"
            }
            if (settings.waterTalkOnWet1) {
                enabledDevices += "Say this when dry:\n ${settings.waterTalkOnDry1}\n\n"
            }
            if (settings.waterSpeechDevice1) {
                enabledDevices += "Custom Speech Device(s):\n"
                enabledDevices += "   "
                settings.waterSpeechDevice1.each() {
                    enabledDevices += "${it.displayName},"
                }
                enabledDevices += "\n\n"
            }
            if (settings.waterModes1) {
                enabledDevices += "Custom mode(s):\n"
                enabledDevices += "   "
                settings.waterModes1.each() {
                    enabledDevices += "${it},"
                }
                enabledDevices += "\n\n"
            }
            if (settings.waterStartTime1) {
                def customStartTime = getTimeFromDateString(settings.waterStartTime1, true)
                def customEndTime = getTimeFromDateString(settings.waterEndTime1, true)
                enabledDevices += "Custom Allowed Talk Time:\n ${customStartTime} - ${customEndTime}"
                customStartTime = ""
                customEndTime = ""
            }
            if (!(enabledDevices == "")) {
                section ("Water Group 1:"){
                    paragraph enabledDevices
                }
            }
            enabledDevices = ""
        }
        //END STATUS CONFIG WATER GROUP 1
        //BEGIN STATUS CONFIG WATER GrOUP 2
        if (settings.waterDeviceGroup2) {
            enabledDevices += "Devices:  \n"
            enabledDevices += "   "
            settings.waterDeviceGroup2.each() {
                enabledDevices += "${it.displayName},"
            }
            enabledDevices += "\n\n"
            if (settings.waterTalkOnWet2) {
                enabledDevices += "Say this when wet:\n ${settings.waterTalkOnWet2}\n\n"
            }
            if (settings.waterTalkOnWet2) {
                enabledDevices += "Say this when dry:\n ${settings.waterTalkOnDry2}\n\n"
            }
            if (settings.waterSpeechDevice2) {
                enabledDevices += "Custom Speech Device(s):\n"
                enabledDevices += "   "
                settings.waterSpeechDevice2.each() {
                    enabledDevices += "${it.displayName},"
                }
                enabledDevices += "\n\n"
            }
            if (settings.waterModes2) {
                enabledDevices += "Custom mode(s):\n"
                enabledDevices += "   "
                settings.waterModes2.each() {
                    enabledDevices += "${it},"
                }
                enabledDevices += "\n\n"
            }
            if (settings.waterStartTime2) {
                def customStartTime = getTimeFromDateString(settings.waterStartTime2, true)
                def customEndTime = getTimeFromDateString(settings.waterEndTime2, true)
                enabledDevices += "Custom Allowed Talk Time:\n ${customStartTime} - ${customEndTime}"
                customStartTime = ""
                customEndTime = ""
            }
            if (!(enabledDevices == "")) {
                section ("Water Group 2:"){
                    paragraph enabledDevices
                }
            }
            enabledDevices = ""
        }
        //END STATUS CONFIG WATER GROUP 2
        //BEGIN STATUS CONFIG WATER GROUP 3
        if (settings.waterDeviceGroup3) {
            enabledDevices += "Devices:  \n"
            enabledDevices += "   "
            settings.waterDeviceGroup3.each() {
                enabledDevices += "${it.displayName},"
            }
            enabledDevices += "\n\n"
            if (settings.waterTalkOnWet3) {
                enabledDevices += "Say this when wet:\n ${settings.waterTalkOnWet3}\n\n"
            }
            if (settings.waterTalkOnWet3) {
                enabledDevices += "Say this when dry:\n ${settings.waterTalkOnDry3}\n\n"
            }
            if (settings.waterSpeechDevice3) {
                enabledDevices += "Custom Speech Device(s):\n"
                enabledDevices += "   "
                settings.waterSpeechDevice3.each() {
                    enabledDevices += "${it.displayName},"
                }
                enabledDevices += "\n\n"
            }
            if (settings.waterModes3) {
                enabledDevices += "Custom mode(s):\n"
                enabledDevices += "   "
                settings.waterModes3.each() {
                    enabledDevices += "${it},"
                }
                enabledDevices += "\n\n"
            }
            if (settings.waterStartTime3) {
                def customStartTime = getTimeFromDateString(settings.waterStartTime3, true)
                def customEndTime = getTimeFromDateString(settings.waterEndTime3, true)
                enabledDevices += "Custom Allowed Talk Time:\n ${customStartTime} - ${customEndTime}"
                customStartTime = ""
                customEndTime = ""
            }
            if (!(enabledDevices == "")) {
                section ("Water Group 3:"){
                    paragraph enabledDevices
                }
            }
            enabledDevices = ""
        }
        //END STATUS CONFIG WATER GROUP 3
        
        //BEGIN STATUS CONFIG SMOKE GROUP 1
        if (settings.smokeDeviceGroup1) {
            enabledDevices += "Devices:  \n"
            enabledDevices += "   "
            settings.smokeDeviceGroup1.each() {
                enabledDevices += "${it.displayName},"
            }
            enabledDevices += "\n\n"
            if (settings.smokeTalkOnDetect1) {
                enabledDevices += "Say this when smoke detected:\n ${settings.smokeTalkOnDetect1}\n\n"
            }
            if (settings.smokeTalkOnClear1) {
                enabledDevices += "Say this when smoke cleared:\n ${settings.smokeTalkOnClear1}\n\n"
            }
            if (settings.smokeTalkOnTest1) {
                enabledDevices += "Say this when smoke tested:\n ${settings.smokeTalkOnTest1}\n\n"
            }
            if (settings.smokeSpeechDevice1) {
                enabledDevices += "Custom Speech Device(s):\n"
                enabledDevices += "   "
                settings.smokeSpeechDevice1.each() {
                    enabledDevices += "${it.displayName},"
                }
                enabledDevices += "\n\n"
            }
            if (settings.smokeModes1) {
                enabledDevices += "Custom mode(s):\n"
                enabledDevices += "   "
                settings.smokeModes1.each() {
                    enabledDevices += "${it},"
                }
                enabledDevices += "\n\n"
            }
            if (settings.smokeStartTime1) {
                def customStartTime = getTimeFromDateString(settings.smokeStartTime1, true)
                def customEndTime = getTimeFromDateString(settings.smokeEndTime1, true)
                enabledDevices += "Custom Allowed Talk Time:\n ${customStartTime} - ${customEndTime}"
                customStartTime = ""
                customEndTime = ""
            }
            if (!(enabledDevices == "")) {
                section ("Smoke Group 1:"){
                    paragraph enabledDevices
                }
            }
            enabledDevices = ""
        }
        //END STATUS CONFIG SMOKE GROUP 1
        //BEGIN STATUS CONFIG SMOKE GROUP 2
        if (settings.smokeDeviceGroup2) {
            enabledDevices += "Devices:  \n"
            enabledDevices += "   "
            settings.smokeDeviceGroup2.each() {
                enabledDevices += "${it.displayName},"
            }
            enabledDevices += "\n\n"
            if (settings.smokeTalkOnDetect2) {
                enabledDevices += "Say this when smoke detected:\n ${settings.smokeTalkOnDetect2}\n\n"
            }
            if (settings.smokeTalkOnClear2) {
                enabledDevices += "Say this when smoke cleared:\n ${settings.smokeTalkOnClear2}\n\n"
            }
            if (settings.smokeTalkOnTest2) {
                enabledDevices += "Say this when smoke tested:\n ${settings.smokeTalkOnTest2}\n\n"
            }
            if (settings.smokeSpeechDevice2) {
                enabledDevices += "Custom Speech Device(s):\n"
                enabledDevices += "   "
                settings.smokeSpeechDevice2.each() {
                    enabledDevices += "${it.displayName},"
                }
                enabledDevices += "\n\n"
            }
            if (settings.smokeModes2) {
                enabledDevices += "Custom mode(s):\n"
                enabledDevices += "   "
                settings.smokeModes2.each() {
                    enabledDevices += "${it},"
                }
                enabledDevices += "\n\n"
            }
            if (settings.smokeStartTime2) {
                def customStartTime = getTimeFromDateString(settings.smokeStartTime2, true)
                def customEndTime = getTimeFromDateString(settings.smokeEndTime2, true)
                enabledDevices += "Custom Allowed Talk Time:\n ${customStartTime} - ${customEndTime}"
                customStartTime = ""
                customEndTime = ""
            }
            if (!(enabledDevices == "")) {
                section ("Smoke Group 2:"){
                    paragraph enabledDevices
                }
            }
            enabledDevices = ""
        }
        //END STATUS CONFIG SMOKE GROUP 2
        //BEGIN STATUS CONFIG SMOKE GROUP 3
        if (settings.smokeDeviceGroup3) {
            enabledDevices += "Devices:  \n"
            enabledDevices += "   "
            settings.smokeDeviceGroup3.each() {
                enabledDevices += "${it.displayName},"
            }
            enabledDevices += "\n\n"
            if (settings.smokeTalkOnDetect3) {
                enabledDevices += "Say this when smoke detected:\n ${settings.smokeTalkOnDetect3}\n\n"
            }
            if (settings.smokeTalkOnClear3) {
                enabledDevices += "Say this when smoke cleared:\n ${settings.smokeTalkOnClear3}\n\n"
            }
            if (settings.smokeTalkOnTest3) {
                enabledDevices += "Say this when smoke tested:\n ${settings.smokeTalkOnTest3}\n\n"
            }
            if (settings.smokeSpeechDevice3) {
                enabledDevices += "Custom Speech Device(s):\n"
                enabledDevices += "   "
                settings.smokeSpeechDevice3.each() {
                    enabledDevices += "${it.displayName},"
                }
                enabledDevices += "\n\n"
            }
            if (settings.smokeModes3) {
                enabledDevices += "Custom mode(s):\n"
                enabledDevices += "   "
                settings.smokeModes3.each() {
                    enabledDevices += "${it},"
                }
                enabledDevices += "\n\n"
            }
            if (settings.smokeStartTime3) {
                def customStartTime = getTimeFromDateString(settings.smokeStartTime3, true)
                def customEndTime = getTimeFromDateString(settings.smokeEndTime3, true)
                enabledDevices += "Custom Allowed Talk Time:\n ${customStartTime} - ${customEndTime}"
                customStartTime = ""
                customEndTime = ""
            }
            if (!(enabledDevices == "")) {
                section ("Smoke Group 3:"){
                    paragraph enabledDevices
                }
            }
            enabledDevices = ""
        }
        //END STATUS CONFIG SMOKE GROUP 3
        
        //BEGIN STATUS CONFIG BUTTON GROUP 1
        if (settings.buttonDeviceGroup1) {
            enabledDevices += "Devices:  \n"
            enabledDevices += "   "
            settings.buttonDeviceGroup1.each() {
                enabledDevices += "${it.displayName},"
            }
            enabledDevices += "\n\n"
            if (settings.buttonTalkOnDetect1) {
                enabledDevices += "Say this when button pressed:\n ${settings.buttonTalkOnPress1}\n\n"
            }
            if (settings.buttonSpeechDevice1) {
                enabledDevices += "Custom Speech Device(s):\n\n"
                enabledDevices += "   "
                settings.buttonSpeechDevice1.each() {
                    enabledDevices += "${it.displayName},"
                }
                enabledDevices += "\n\n"
            }
            if (settings.buttonModes1) {
                enabledDevices += "Custom mode(s):\n"
                enabledDevices += "   "
                settings.buttonModes1.each() {
                    enabledDevices += "${it},"
                }
                enabledDevices += "\n\n"
            }
            if (settings.buttonStartTime1) {
                def customStartTime = getTimeFromDateString(settings.buttonStartTime1, true)
                def customEndTime = getTimeFromDateString(settings.buttonEndTime1, true)
                enabledDevices += "Custom Allowed Talk Time:\n ${customStartTime} - ${customEndTime}"
                customStartTime = ""
                customEndTime = ""
            }
            if (!(enabledDevices == "")) {
                section ("Button Group 1:"){
                    paragraph enabledDevices
                }
            }
            enabledDevices = ""
        }
        //END STATUS CONFIG BUTTON GROUP 1
        //BEGIN STATUS CONFIG BUTTON GROUP 2
        if (settings.buttonDeviceGroup2) {
            enabledDevices += "Devices:  \n"
            enabledDevices += "   "
            settings.buttonDeviceGroup2.each() {
                enabledDevices += "${it.displayName},"
            }
            enabledDevices += "\n\n"
            if (settings.buttonTalkOnDetect2) {
                enabledDevices += "Say this when button pressed:\n ${settings.buttonTalkOnPress2}\n\n"
            }
            if (settings.buttonSpeechDevice2) {
                enabledDevices += "Custom Speech Device(s):\n\n"
                enabledDevices += "   "
                settings.buttonSpeechDevice2.each() {
                    enabledDevices += "${it.displayName},"
                }
                enabledDevices += "\n\n"
            }
            if (settings.buttonModes2) {
                enabledDevices += "Custom mode(s):\n"
                enabledDevices += "   "
                settings.buttonModes2.each() {
                    enabledDevices += "${it},"
                }
                enabledDevices += "\n\n"
            }
            if (settings.buttonStartTime2) {
                def customStartTime = getTimeFromDateString(settings.buttonStartTime2, true)
                def customEndTime = getTimeFromDateString(settings.buttonEndTime2, true)
                enabledDevices += "Custom Allowed Talk Time:\n ${customStartTime} - ${customEndTime}"
                customStartTime = ""
                customEndTime = ""
            }
            if (!(enabledDevices == "")) {
                section ("Button Group 2:"){
                    paragraph enabledDevices
                }
            }
            enabledDevices = ""
        }
        //END STATUS CONFIG BUTTON GROUP 2
        //BEGIN STATUS CONFIG BUTTON GROUP 3
        if (settings.buttonDeviceGroup3) {
            enabledDevices += "Devices:  \n"
            enabledDevices += "   "
            settings.buttonDeviceGroup3.each() {
                enabledDevices += "${it.displayName},"
            }
            enabledDevices += "\n\n"
            if (settings.buttonTalkOnDetect3) {
                enabledDevices += "Say this when button pressed:\n ${settings.buttonTalkOnPress3}\n\n"
            }
            if (settings.buttonSpeechDevice3) {
                enabledDevices += "Custom Speech Device(s):\n\n"
                enabledDevices += "   "
                settings.buttonSpeechDevice3.each() {
                    enabledDevices += "${it.displayName},"
                }
                enabledDevices += "\n\n"
            }
            if (settings.buttonModes3) {
                enabledDevices += "Custom mode(s):\n"
                enabledDevices += "   "
                settings.buttonModes3.each() {
                    enabledDevices += "${it},"
                }
                enabledDevices += "\n\n"
            }
            if (settings.buttonStartTime3) {
                def customStartTime = getTimeFromDateString(settings.buttonStartTime3, true)
                def customEndTime = getTimeFromDateString(settings.buttonEndTime3, true)
                enabledDevices += "Custom Allowed Talk Time:\n ${customStartTime} - ${customEndTime}"
                customStartTime = ""
                customEndTime = ""
            }
            if (!(enabledDevices == "")) {
                section ("Button Group 3:"){
                    paragraph enabledDevices
                }
            }
            enabledDevices = ""
        }
        //END STATUS CONFIG BUTTON GROUP 3
    }
}

def pageTalkNow(){
    dynamicPage(name: "pageTalkNow", title: "Talk Now", install: false, uninstall: false){
        section(""){
            paragraph ("Speak the following phrase:\nNote: must differ from the last spoken phrase\n")
            input name: "speechTalkNow", type: text, title: "Speak phrase", required: false, submitOnChange: true
            input name: "talkNowSpeechDevice", type: state.speechDeviceType, title: "Talk with these text-to-speech devices", multiple: true, required: (!(settings.speechTalkNow == null)), submitOnChange: true
            //LOGDEBUG("previoustext=${state.lastTalkNow} New=${settings.speechTalkNow}")
            if ((!(state.lastTalkNow == settings.speechTalkNow)) && (settings.talkNowSpeechDevice)){
                //Say stuff!
                def customevent = [displayName: 'BigTalker:TalkNow', name: 'TalkNow', value: 'TalkNow']
                Talk(settings.speechTalkNow, settings.talkNowSpeechDevice, customevent)
                state.lastTalkNow = settings.speechTalkNow
            }
        }
        section("Help"){
            href "pageHelpPhraseTokens", title:"Phrase Tokens", description:"Tap for a list of phrase tokens"
        }
    }
}

def pageHelpPhraseTokens(){
    dynamicPage(name: "pageHelpPhraseTokens", title: "Available Phrase Tokens", install: false, uninstall:false){
       section("The following tokens can be used in your event phrases and will be replaced as listed:"){
       	   def AvailTokens = ""
           AvailTokens += "%devicename% = Triggering devices display name\n\n"
           AvailTokens += "%devicetype% = Triggering device type; motion, switch, etc\n\n"
           AvailTokens += "%locationname% = Hub location name; home, work, etc\n\n"
           AvailTokens += "%lastmode% = Last hub mode; home, away, etc\n\n"
           AvailTokens += "%mode% = Current hub mode; home, away, etc\n\n"
           AvailTokens += "%time% = Current hub time; HH:mm am/pm\n\n"
           AvailTokens += "%weathercurrent% = Current weather based on hub location\n\n"
           AvailTokens += "%weathercurrent(00000)% = Current weather* based on custom zipcode (replace 00000)\n\n"
           AvailTokens += "%weathertoday% = Today's weather forecast* based on hub location\n\n"
           AvailTokens += "%weathertoday(00000)% = Today's weather forecast* based on custom zipcode (replace 00000)\n\n"
           AvailTokens += "%weathertonight% = Tonight's weather forecast* based on hub location\n\n"
           AvailTokens += "%weathertonight(00000)% = Tonight's weather* forecast based on custom zipcode (replace 00000)\n\n"
           AvailTokens += "%weathertomorrow% = Tomorrow's weather forecast* based on hub location\n\n"
           AvailTokens += "%weathertomorrow(00000)% = Tomorrow's weather forecast* based on custom zipcode (replace 00000)\n\n"
           AvailTokens += "\n*Weather forecasts provided by Weather Underground"
           paragraph(AvailTokens)
       }
   }
}

def pageConfigureSpeechDeviceType(){
    if (!(state.installed == true)) { state.installed = false; state.speechDeviceType = "capability.musicPlayer"}
    dynamicPage(name: "pageConfigureSpeechDeviceType", title: "Configure", nextPage: "pageConfigureDefaults", install: false, uninstall: false) {
        //section ("Speech Device Type Support"){
        section (){
            paragraph "${app.label} can support either 'Music Player' or 'Speech Synthesis' devices."
            paragraph "'Music Player' typically supports devices such as Sonos, VLCThing, Generic Media Renderer.\n\n'Speech Synthesis' typically supports devices such as Ubi and LANnouncer.\n\nThis setting cannot be changed without reinstalling ${app.label}."
            input "speechDeviceType", "bool", title: "ON=Music Player\nOFF=Speech Synthesis", required: true, defaultValue: true, submitOnChange: true
            paragraph "\nClick Next (top right) to continue configuration...\n"
            if (speechDeviceType == true || speechDeviceType == null) {state.speechDeviceType = "capability.musicPlayer"}
            if (speechDeviceType == false) {state.speechDeviceType = "capability.speechSynthesis"}
        }
    }
//End pageConfigureSpeechDeviceType()
}

def pageConfigureDefaults(){
    if (!(state.installed == true)) { 
       state.dynPageProperties = [
            name:      "pageConfigureDefaults",
            title:     "Configure Defaults",
            install:   false,
            uninstall: false,
            nextPage:  "pageConfigureEvents"
        ]
    } else {
       state.dynPageProperties = [
            name:      "pageConfigureDefaults",
            title:     "Configure Defaults",
            install:   true,
            uninstall: true
        ]
    }
    return dynamicPage(state.dynPageProperties) {
    //dynamicPage(name: "pageConfigureDefaults", title: "Configure Defaults", nextPage: "${myNextPage}", install: false, uninstall: false) {
        section("Talk with:"){
           if (state.speechDeviceType == null || state.speechDeviceType == "") { state.speechDeviceType = "capability.musicPlayer" }
           input "speechDeviceDefault", state.speechDeviceType, title: "Talk with these text-to-speech devices (default)", multiple: true, required: true, submitOnChange: false
        }
        if (state.speechDeviceType == "capability.musicPlayer") {
            section ("Adjust volume during announcement (optional; Supports: Sonos, VLC-Thing):"){
                input "speechVolume", "number", title: "Set volume to (1-100%):", required: false
            }
        }
        section ("Talk only while in these modes:"){
            input "speechModesDefault", "mode", title: "Talk only while in these modes (default)", multiple: true, required: true, submitOnChange: false
        }
        section ("Only between these times:"){
            input "defaultStartTime", "time", title: "Don't talk before: ", required: false, submitOnChange: true
            input "defaultEndTime", "time", title: "Don't talk after: ", required: (!(settings.defaultStartTime == null)), submitOnChange: true
        }
        section(){
            input "debugmode", "bool", title: "Enable debug logging", required: true, defaultValue: false
        }
    }
}

def pageConfigureEvents(){
    dynamicPage(name: "pageConfigureEvents", title: "Configure Events", install: true, uninstall: false) {
        section("Talk on events:") {
            if (settings.timeSlotTime1 || settings.timeSlotTime2 || settings.timeSlotTime3) {
                href "pageConfigTime", title: "Time", description: "Tap to modify"
            } else {
                href "pageConfigTime", title: "Time", description: "Tap to configure"
            }
            if (settings.motionDeviceGroup1 || settings.motionDeviceGroup2 || settings.motionDeviceGroup3) {
                href "pageConfigMotion", title:"Motion", description:"Tap to modify"
            } else {
                href "pageConfigMotion", title:"Motion", description:"Tap to configure"
            }
            if (settings.switchDeviceGroup1 || settings.switchDeviceGroup2 || settings.switchDeviceGroup3) {
                href "pageConfigSwitch", title:"Switch", description:"Tap to modify"
            } else {
                href "pageConfigSwitch", title:"Switch", description:"Tap to configure"
            }
            if (settings.presDeviceGroup1 || settings.presDeviceGroup2 || settings.presDeviceGroup3) {
                href "pageConfigPresence", title:"Presence", description:"Tap to modify"
            } else {
                href "pageConfigPresence", title:"Presence", description:"Tap to configure"
            }
            if (settings.lockDeviceGroup1 || settings.lockDeviceGroup2 || settings.lockDeviceGroup3) {
                href "pageConfigLock", title:"Lock", description:"Tap to modify"
            } else {
                href "pageConfigLock", title:"Lock", description:"Tap to configure"
            }
            if (settings.contactDeviceGroup1 || settings.contactDeviceGroup2 || settings.contactDeviceGroup3) {
                href "pageConfigContact", title:"Contact", description:"Tap to modify"
            } else {
                href "pageConfigContact", title:"Contact", description:"Tap to configure"
            }
            if (settings.modePhraseGroup1 || settings.modePhraseGroup2 || settings.modePhraseGroup3) {
                href "pageConfigMode", title:"Mode", description:"Tap to modify"
            } else {
                href "pageConfigMode", title:"Mode", description:"Tap to configure"
            }
            if (settings.thermostatDeviceGroup1 || settings.thermostatDeviceGroup2 || settings.thermostatDeviceGroup3) {
                href "pageConfigThermostat", title:"Thermostat", description:"Tap to modify"
            } else {
                href "pageConfigThermostat", title:"Thermostat", description:"Tap to configure"
            }
            if (settings.accelerationDeviceGroup1 || settings.accelerationDeviceGroup2 || settings.accelerationDeviceGroup3) {
                href "pageConfigAcceleration", title: "Acceleration", description:"Tap to modify"
            } else {
                href "pageConfigAcceleration", title: "Acceleration", description:"Tap to configure"
            }
            if (settings.waterDeviceGroup1 || settings.waterDeviceGroup2 || settings.waterDeviceGroup3) {
                href "pageConfigWater", title: "Water", description:"Tap to modify"
            } else {
                href "pageConfigWater", title: "Water", description:"Tap to configure"
            }
            if (settings.smokeDeviceGroup1 || settings.smokeDeviceGroup2 || settings.smokeDeviceGroup3) {
                href "pageConfigSmoke", title: "Smoke", description:"Tap to modify"
            } else { 
                href "pageConfigSmoke", title: "Smoke", description:"Tap to configure"
            }
            if (settings.buttonDeviceGroup1 || settings.buttonDeviceGroup2 || settings.buttonDeviceGroup3) {
                href "pageConfigButton", title: "Button", description:"Tap to configure"
            } else {
                href "pageConfigButton", title: "Button", description:"Tap to configure"
            }
        }
    }
}

def pageConfigMotion(){
    dynamicPage(name: "pageConfigMotion", title: "Configure talk on motion", install: false, uninstall: false) {
        section("Motion Sensor Group 1"){
            def defaultSpeechActive1 = ""
            def defaultSpeechInactive1 = ""
            if (!motionDeviceGroup1) {
                defaultSpeechActive1 = "%devicename% is now %devicechange%"
                defaultSpeechInactive1 = "%devicename% is now %devicechange%"
            }
            input name: "motionDeviceGroup1", type: "capability.motionSensor", title: "Motion Sensor(s)", required: false, multiple: true
            input name: "motionTalkActive1", type: "text", title: "Say this on motion active:", required: false, defaultValue: defaultSpeechActive1
            input name: "motionTalkInactive1", type: "text", title: "Say this on motion inactive:", required: false, defaultValue: defaultSpeechInactive1
            input name: "motionSpeechDevice1", type: state.speechDeviceType, title: "Talk with these text-to-speech devices (overrides default)", multiple: true, required: false
            input name: "motionModes1", type: "mode", title: "Talk when in these mode(s) (overrides default)", multiple: true, required: false
            input name: "motionStartTime1", type: "time", title: "Don't talk before (overrides default)", required: false, submitOnChange: true
            input name: "motionEndTime1", type: "time", title: "Don't talk after (overrides default)", required: (!(settings.motionStartTime1 == null))
        }
        section("Motion Sensor Group 2"){
            input name: "motionDeviceGroup2", type: "capability.motionSensor", title: "Motion Sensor(s)", required: false, multiple: true
            input name: "motionTalkActive2", type: "text", title: "Say this on motion active:", required: false
            input name: "motionTalkInactive2", type: "text", title: "Say this on motion inactive:", required: false
            input name: "motionSpeechDevice2", type: state.speechDeviceType, title: "Talk with these text-to-speech devices (overrides default)", multiple: true, required: false
            input name: "motionModes2", type: "mode", title: "Talk when in these mode(s) (overrides default)", multiple: true, required: false
            input name: "motionStartTime2", type: "time", title: "Don't talk before (overrides default)", required: false, submitOnChange: true
            input name: "motionEndTime2", type: "time", title: "Don't talk after (overrides default)", required: (!(settings.motionStartTime2 == null))
        }
        section("Motion Sensor Group 3"){
            input name: "motionDeviceGroup3", type: "capability.motionSensor", title: "Motion Sensor(s)", required: false, multiple: true
            input name: "motionTalkActive3", type: "text", title: "Say this on motion active:", required: false
            input name: "motionTalkInactive3", type: "text", title: "Say this on motion inactive:", required: false
            input name: "motionSpeechDevice3", type: state.speechDeviceType, title: "Talk with these text-to-speech devices (overrides default)", multiple: true, required: false
            input name: "motionModes3", type: "mode", title: "Talk when in these mode(s) (overrides default)", multiple: true, required: false
            input name: "motionStartTime3", type: "time", title: "Don't talk before (overrides default)", required: false, submitOnChange: true
            input name: "motionEndTime3", type: "time", title: "Don't talk after (overrides default)", required: (!(settings.motionStartTime3 == null))
        }
        section("Help"){
            href "pageHelpPhraseTokens", title:"Phrase Tokens", description:"Tap for a list of phrase tokens"
        }
    }
//End pageConfigMotion()
}

def pageConfigSwitch(){
    dynamicPage(name: "pageConfigSwitch", title: "Configure talk on switch", install: false, uninstall: false) {
        section("Switch Group 1"){
            def defaultSpeechOn1 = ""
            def defaultSpeechOff1 = ""
            if (!switchDeviceGroup1) {
                defaultSpeechOn1 = "%devicename% is now %devicechange%"
                defaultSpeechOff1 = "%devicename% is now %devicechange%"
            }
            input name: "switchDeviceGroup1", type: "capability.switch", title: "Switch(es)", required: false, multiple: true
            input name: "switchTalkOn1", type: "text", title: "Say this when switch is turned ON:", required: false, defaultValue: defaultSpeechOn1
            input name: "switchTalkOff1", type: "text", title: "Say this when switch is turned OFF:", required: false, defaultValue: defaultSpeechOff1
            input name: "switchSpeechDevice1", type: state.speechDeviceType, title: "Talk with these text-to-speech devices (overrides default)", multiple: true, required: false
            input name: "switchModes1", type: "mode", title: "Talk when in these mode(s) (overrides default)", multiple: true, required: false
            input name: "switchStartTime1", type: "time", title: "Don't talk before (overrides default)", required: false, submitOnChange: true
            input name: "switchEndTime1", type: "time", title: "Don't talk after (overrides default)", required: (!(settings.switchStartTime1 == null))
        }
        section("Switch Group 2"){
            input name: "switchDeviceGroup2", type: "capability.switch", title: "Switch(es)", required: false, multiple: true
            input name: "switchTalkOn2", type: "text", title: "Say this when switch is turned ON:", required: false
            input name: "switchTalkOff2", type: "text", title: "Say this when switch is turned OFF:", required: false
            input name: "switchSpeechDevice2", type: state.speechDeviceType, title: "Talk with these text-to-speech devices (overrides default)", multiple: true, required: false
            input name: "switchModes2", type: "mode", title: "Talk when in these mode(s) (overrides default)", multiple: true, required: false
            input name: "switchStartTime2", type: "time", title: "Don't talk before (overrides default)", required: false, submitOnChange: true
            input name: "switchEndTime2", type: "time", title: "Don't talk after (overrides default)", required: (!(settings.switchStartTime2 == null))
        }
        section("Switch Group 3"){
            input name: "switchDeviceGroup3", type: "capability.switch", title: "Switch(es)", required: false, multiple: true
            input name: "switchTalkOn3", type: "text", title: "Say this when switch is turned ON:", required: false
            input name: "switchTalkOff3", type: "text", title: "Say this when switch is turned OFF:", required: false
            input name: "switchSpeechDevice3", type: state.speechDeviceType, title: "Talk with these text-to-speech devices (overrides default)", multiple: true, required: false
            input name: "switchModes3", type: "mode", title: "Talk when in these mode(s) (overrides default)", multiple: true, required: false
            input name: "switchStartTime3", type: "time", title: "Don't talk before (overrides default)", required: false, submitOnChange: true
            input name: "switchEndTime3", type: "time", title: "Don't talk after (overrides default)", required: (!(settings.switchStartTime3 == null))
        }
        section("Help"){
            href "pageHelpPhraseTokens", title:"Phrase Tokens", description:"Tap for a list of phrase tokens"
        }
    }
//End pageConfigSwitch()
}

def pageConfigPresence(){
    dynamicPage(name: "pageConfigPresence", title: "Configure talk on presence", install: false, uninstall: false) {
        section("Presence Group 1"){
            def defaultSpeechArrive1 = ""
            def defaultSpeechLeave1 = ""
            if (!presDeviceGroup1) {
                defaultSpeechArrive1 = "%devicename% has arrived"
                defaultSpeechLeave1 = "%devicename% has left"
            }
            input name: "presDeviceGroup1", type: "capability.presenceSensor", title: "Presence Sensor(s)", required: false, multiple: true
            input name: "presTalkOnArrive1", type: "text", title: "Say this when someone arrives:", required: false, defaultValue: defaultSpeechArrive1
            input name: "presTalkOnLeave1", type: "text", title: "Say this when someone leaves:", required: false, defaultValue: defaultSpeechLeave1
            input name: "presSpeechDevice1", type: state.speechDeviceType, title: "Talk with these text-to-speech devices (overrides default)", multiple: true, required: false
            input name: "presModes1", type: "mode", title: "Talk when in these mode(s) (overrides default)", multiple: true, required: false
            input name: "presStartTime1", type: "time", title: "Don't talk before (overrides default)", required: false, submitOnChange: true
            input name: "presEndTime1", type: "time", title: "Don't talk after (overrides default)", required: (!(settings.presStartTime1 == null))
        }
        section("Presence Group 2"){
            input name: "presDeviceGroup2", type: "capability.presenceSensor", title: "Presence Sensor(s)", required: false, multiple: true
            input name: "presTalkOnArrive2", type: "text", title: "Say this when someone arrives:", required: false
            input name: "presTalkOnLeave2", type: "text", title: "Say this when someone leaves:", required: false
            input name: "presSpeechDevice2", type: state.speechDeviceType, title: "Talk with these text-to-speech devices (overrides default)", multiple: true, required: false
            input name: "presModes2", type: "mode", title: "Talk when in these mode(s) (overrides default)", multiple: true, required: false
            input name: "presStartTime2", type: "time", title: "Don't talk before (overrides default)", required: false, submitOnChange: true
            input name: "presEndTime2", type: "time", title: "Don't talk after (overrides default)", required: (!(settings.presStartTime2 == null))
        }
        section("Presence Group 3"){
            input name: "presDeviceGroup3", type: "capability.presenceSensor", title: "Presence Sensor(s)", required: false, multiple: true
            input name: "presTalkOnArrive3", type: "text", title: "Say this when someone arrives:", required: false
            input name: "presTalkOnLeave3", type: "text", title: "Say this when someone leaves:", required: false
            input name: "presSpeechDevice3", type: state.speechDeviceType, title: "Talk with these text-to-speech devices (overrides default)", multiple: true, required: false
            input name: "presModes3", type: "mode", title: "Talk when in these mode(s) (overrides default)", multiple: true, required: false
            input name: "presStartTime3", type: "time", title: "Don't talk before (overrides default)", required: false, submitOnChange: true
            input name: "presEndTime3", type: "time", title: "Don't talk after (overrides default)", required: (!(settings.presStartTime3 == null))
        }
        section("Help"){
            href "pageHelpPhraseTokens", title:"Phrase Tokens", description:"Tap for a list of phrase tokens"
        }
    }
//End pageConfigPresence()
}

def pageConfigLock(){
    dynamicPage(name: "pageConfigLock", title: "Configure talk on lock", install: false, uninstall: false) {
        section("Lock Group 1"){
            def defaultSpeechUnlock1 = ""
            def defaultSpeechLock1 = ""
            if (!lockDeviceGroup1) {
                defaultSpeechUnlock1 = "%devicename% is now %devicechange%"
                defaultSpeechLock1 = "%devicename% is now %devicechange%"
            }
            input name: "lockDeviceGroup1", type: "capability.lock", title: "Lock(s)", required: false, multiple: true
            input name: "lockTalkOnUnlock1", type: "text", title: "Say this when unlocked:", required: false, defaultValue: defaultSpeechUnlock1
            input name: "lockTalkOnLock1", type: "text", title: "Say this when locked:", required: false, defaultValue: defaultSpeechLock1
            input name: "lockSpeechDevice1", type: state.speechDeviceType, title: "Talk with these text-to-speech devices (overrides default)", multiple: true, required: false
            input name: "lockModes1", type: "mode", title: "Talk when in these mode(s) (overrides default)", multiple: true, required: false
            input name: "lockStartTime1", type: "time", title: "Don't talk before (overrides default)", required: false, submitOnChange: true
            input name: "lockEndTime1", type: "time", title: "Don't talk after (overrides default)", required: (!(settings.lockStartTime1 == null))
        }
        section("Lock Group 2"){
            input name: "lockDeviceGroup2", type: "capability.lock", title: "Lock(s)", required: false, multiple: true
            input name: "lockTalkOnUnlock2", type: "text", title: "Say this when unlocked:", required: false
            input name: "lockTalkOnLock2", type: "text", title: "Say this when locked:", required: false
            input name: "lockSpeechDevice2", type: state.speechDeviceType, title: "Talk with these text-to-speech devices (overrides default)", multiple: true, required: false
            input name: "lockModes2", type: "mode", title: "Talk when in these mode(s) (overrides default)", multiple: true, required: false
            input name: "lockStartTime2", type: "time", title: "Don't talk before (overrides default)", required: false, submitOnChange: true
            input name: "lockEndTime2", type: "time", title: "Don't talk after (overrides default)", required: (!(settings.lockStartTime2 == null))
        }
        section("Lock Group 3"){
            input name: "lockDeviceGroup3", type: "capability.lock", title: "Lock(s)", required: false, multiple: true
            input name: "lockTalkOnUnlock3", type: "text", title: "Say this when unlocked:", required: false
            input name: "lockTalkOnLock3", type: "text", title: "Say this when locked:", required: false
            input name: "lockSpeechDevice3", type: state.speechDeviceType, title: "Talk with these text-to-speech devices (overrides default)", multiple: true, required: false
            input name: "lockModes3", type: "mode", title: "Talk when in these mode(s) (overrides default)", multiple: true, required: false
            input name: "lockStartTime3", type: "time", title: "Don't talk before (overrides default)", required: false, submitOnChange: true
            input name: "lockEndTime3", type: "time", title: "Don't talk after (overrides default)", required: (!(settings.lockStartTime3 == null))
        }
        section("Help"){
            href "pageHelpPhraseTokens", title:"Phrase Tokens", description:"Tap for a list of phrase tokens"
        }
    }
//End pageConfigLock()
}

def pageConfigContact(){
    dynamicPage(name: "pageConfigContact", title: "Configure talk on contact sensor", install: false, uninstall: false) {
        section("Contact Group 1"){
            def defaultSpeechOpen1 = ""
            def defaultSpeechClose1 = ""
            if (!contactDeviceGroup1) {
                defaultSpeechOpen1 = "%devicename% is now %devicechange%"
                defaultSpeechClose1 = "%devicename% is now %devicechange%"
            }
            input name: "contactDeviceGroup1", type: "capability.contactSensor", title: "Contact sensor(s)", required: false, multiple: true
            input name: "contactTalkOnOpen1", type: "text", title: "Say this when opened:", required: false, defaultValue: defaultSpeechOpen1
            input name: "contactTalkOnClose1", type: "text", title: "Say this when closed:", required: false, defaultValue: defaultSpeechClose1
            input name: "contactSpeechDevice1", type: state.speechDeviceType, title: "Talk with these text-to-speech devices (overrides default)", multiple: true, required: false
            input name: "contactModes1", type: "mode", title: "Talk when in these mode(s) (overrides default)", multiple: true, required: false
            input name: "contactStartTime1", type: "time", title: "Don't talk before (overrides default)", required: false, submitOnChange: true
            input name: "contactEndTime1", type: "time", title: "Don't talk after (overrides default)", required: (!(settings.contactStartTime1 == null))
        }
        section("Contact Group 2"){
            input name: "contactDeviceGroup2", type: "capability.contactSensor", title: "Contact sensor(s)", required: false, multiple: true
            input name: "contactTalkOnOpen2", type: "text", title: "Say this when opened:", required: false
            input name: "contactTalkOnClose2", type: "text", title: "Say this when closed:", required: false
            input name: "contactSpeechDevice2", type: state.speechDeviceType, title: "Talk with these text-to-speech devices (overrides default)", multiple: true, required: false
            input name: "contactModes2", type: "mode", title: "Talk when in these mode(s) (overrides default)", multiple: true, required: false
            input name: "contactStartTime2", type: "time", title: "Don't talk before (overrides default)", required: false, submitOnChange: true
            input name: "contactEndTime2", type: "time", title: "Don't talk after (overrides default)", required: (!(settings.contactStartTime2 == null))
        }
        section("Contact Group 3"){
            input name: "contactDeviceGroup3", type: "capability.contactSensor", title: "Contact sensor(s)", required: false, multiple: true
            input name: "contactTalkOnOpen3", type: "text", title: "Say this when opened:", required: false
            input name: "contactTalkOnClose3", type: "text", title: "Say this when closed:", required: false
            input name: "contactSpeechDevice3", type: state.speechDeviceType, title: "Talk with these text-to-speech devices (overrides default)", multiple: true, required: false
            input name: "contactModes3", type: "mode", title: "Talk when in these mode(s) (overrides default)", multiple: true, required: false
            input name: "contactStartTime3", type: "time", title: "Don't talk before (overrides default)", required: false, submitOnChange: true
            input name: "contactEndTime3", type: "time", title: "Don't talk after (overrides default)", required: (!(settings.contactStartTime3 == null))
        }
        section("Help"){
            href "pageHelpPhraseTokens", title:"Phrase Tokens", description:"Tap for a list of phrase tokens"
        }
    }
//End pageConfigContact()
}

def pageConfigMode(){
    def locationmodes = []
    location.modes.each(){
       locationmodes += it
    }
    LOGDEBUG("locationmodes=${locationmodes}")
    dynamicPage(name: "pageConfigMode", title: "Configure talk on home mode change", install: false, uninstall: false) {
        section("Mode Group 1"){
            def defaultSpeechMode1 = ""
            if (!modePhraseGroup1) {
                defaultSpeechMode1 = "%locationname% mode has changed from %lastmode% to %mode%"
            }
            input name: "modePhraseGroup1", type:"mode", title:"When mode changes to: ", required:false, multiple:true, submitOnChange:false
            input name: "modeExcludePhraseGroup1", type: "mode", title: "But not when changed from (optional): ", required: false, multiple: true
            input name: "TalkOnModeChange1", type: "text", title: "Say this when home mode is changed", required: false, defaultValue: defaultSpeechMode1
            input name: "modePhraseSpeechDevice1", type: state.speechDeviceType, title: "Talk with these text-to-speech devices (overrides default)", multiple: true, required: false
            input name: "modeStartTime1", type: "time", title: "Don't talk before (overrides default)", required: false, submitOnChange: true
            input name: "modeEndTime1", type: "time", title: "Don't talk after (overrides default)", required: (!(settings.modeStartTime1 == null))
        }
        section("Help"){
            href "pageHelpPhraseTokens", title:"Phrase Tokens", description:"Tap for a list of phrase tokens"
        }
    }
//End pageConfigMode()
}

def pageConfigThermostat(){
    dynamicPage(name: "pageConfigThermostat", title: "Configure talk when thermostat state is:", install: false, uninstall: false) {
        section("Thermostat Group 1"){
            def defaultSpeechIdle1 = ""
            def defaultSpeechHeating1 = ""
            def defaultSpeechCooling1 = ""
            def defaultSpeechFan1 = ""
            if (!thermostatDeviceGroup1) {
                defaultSpeechIdle1 = "%devicename% is now off"
                defaultSpeechHeating1 = "%devicename% is now heating"
                defaultSpeechCooling1 = "%devicename% is now cooling"
                defaultSpeechFan1 = "%devicename% is now circulating fan"
            }
            input name: "thermostatDeviceGroup1", type: "capability.thermostat", title: "Thermostat(s)", required: false, multiple: true
            input name: "thermostatTalkOnIdle1", type: "text", title: "Say this on change to Idle:", required: false, defaultValue: defaultSpeechIdle1
            input name: "thermostatTalkOnHeating1", type: "text", title: "Say this on change to heating:", required: false, defaultValue: defaultSpeechHeating1
            input name: "thermostatTalkOnCooling1", type: "text", title: "Say this on change to cooling:", required: false, defaultValue: defaultSpeechCooling1
            input name: "thermostatTalkOnFan1", type: "text", title: "Say this on change to fan only:", required: false, defaultValue: defaultSpeechFan1
            input name: "thermostatSpeechDevice1", type: state.speechDeviceType, title: "Talk with these text-to-speech devices (overrides default)", multiple: true, required: false
            input name: "thermostatModes1", type: "mode", title: "Talk when in these mode(s) (overrides default)", multiple: true, required: false
            input name: "thermostatStartTime1", type: "time", title: "Don't talk before (overrides default)", required: false, submitOnChange: true
            input name: "thermostatEndTime1", type: "time", title: "Don't talk after (overrides default)", required: (!(settings.thermostatStartTime1 == null))
        }
        section("Help"){
            href "pageHelpPhraseTokens", title:"Phrase Tokens", description:"Tap for a list of phrase tokens"
        }
    }
//End pageConfigThermostat()
}

def pageConfigAcceleration(){
    dynamicPage(name: "pageConfigAcceleration", title: "Configure talk on acceleration", install: false, uninstall: false) {
        section("Acceleration Group 1"){
            def defaultSpeechActive1 = ""
            def defaultSpeechInactive1 = ""
            if (!accelerationDeviceGroup1) {
                defaultSpeechActive1 = "%devicename% acceleration %devicechange%"
                defaultSpeechInactive1 = "%devicename% acceleration is no longer active"
            }
            input name: "accelerationDeviceGroup1", type: "capability.accelerationSensor", title: "Acceleration sensor(s)", required: false, multiple: true
            input name: "accelerationTalkOnActive1", type: "text", title: "Say this when activated:", required: false, defaultValue: defaultSpeechActive1
            input name: "accelerationTalkOnInactive1", type: "text", title: "Say this when inactivated:", required: false, defaultValue: defaultSpeechInactive1
            input name: "accelerationSpeechDevice1", type: state.speechDeviceType, title: "Talk with these text-to-speech devices (overrides default)", multiple: true, required: false
            input name: "accelerationModes1", type: "mode", title: "Talk when in these mode(s) (overrides default)", multiple: true, required: false
            input name: "accelerationStartTime1", type: "time", title: "Don't talk before (overrides default)", required: false, submitOnChange: true
            input name: "accelerationEndTime1", type: "time", title: "Don't talk after (overrides default)", required: (!(settings.accelerationStartTime1 == null))
        }
        section("Acceleration Group 2"){
            input name: "accelerationDeviceGroup2", type: "capability.accelerationSensor", title: "Acceleration sensor(s)", required: false, multiple: true
            input name: "accelerationTalkOnActive2", type: "text", title: "Say this when activated:", required: false
            input name: "accelerationTalkOnInactive2", type: "text", title: "Say this when inactivated:", required: false
            input name: "accelerationSpeechDevice2", type: state.speechDeviceType, title: "Talk with these text-to-speech devices (overrides default)", multiple: true, required: false
            input name: "accelerationModes2", type: "mode", title: "Talk when in these mode(s) (overrides default)", multiple: true, required: false
            input name: "accelerationStartTime2", type: "time", title: "Don't talk before (overrides default)", required: false, submitOnChange: true
            input name: "accelerationEndTime2", type: "time", title: "Don't talk after (overrides default)", required: (!(settings.accelerationStartTime2 == null))
        }
        section("Acceleration Group 3"){
            input name: "accelerationDeviceGroup3", type: "capability.accelerationSensor", title: "Acceleration sensor(s)", required: false, multiple: true
            input name: "accelerationTalkOnActive3", type: "text", title: "Say this when activated:", required: false
            input name: "accelerationTalkOnInactive3", type: "text", title: "Say this when inactivated:", required: false
            input name: "accelerationSpeechDevice3", type: state.speechDeviceType, title: "Talk with these text-to-speech devices (overrides default)", multiple: true, required: false
            input name: "accelerationModes3", type: "mode", title: "Talk when in these mode(s) (overrides default)", multiple: true, required: false
            input name: "accelerationStartTime3", type: "time", title: "Don't talk before (overrides default)", required: false, submitOnChange: true
            input name: "accelerationEndTime3", type: "time", title: "Don't talk after (overrides default)", required: (!(settings.accelerationStartTime3 == null))
        }
        section("Help"){
            href "pageHelpPhraseTokens", title:"Phrase Tokens", description:"Tap for a list of phrase tokens"
        }
    }
//End pageConfigAcceleration()
}

def pageConfigWater(){
    dynamicPage(name: "pageConfigWater", title: "Configure talk on water", install: false, uninstall: false) {
        section("Water Group 1"){
            def defaultSpeechWet1 = ""
            def defaultSpeechDry1 = ""
            if (!waterDeviceGroup1) {
                defaultSpeechWet1 = "%devicename% is %devicechange%"
                defaultSpeechDry1 = "%devicename% is %devicechange%"
            }
            input name: "waterDeviceGroup1", type: "capability.waterSensor", title: "Water sensor(s)", required: false, multiple: true
            input name: "waterTalkOnWet1", type: "text", title: "Say this when wet:", required: false, defaultValue: defaultSpeechWet1
            input name: "waterTalkOnDry1", type: "text", title: "Say this when dry:", required: false, defaultValue: defaultSpeechDry1
            input name: "waterSpeechDevice1", type: state.speechDeviceType, title: "Talk with these text-to-speech devices (overrides default)", multiple: true, required: false
            input name: "waterModes1", type: "mode", title: "Talk when in these mode(s) (overrides default)", multiple: true, required: false
            input name: "waterStartTime1", type: "time", title: "Don't talk before (overrides default)", required: false, submitOnChange: true
            input name: "waterEndTime1", type: "time", title: "Don't talk after (overrides default)", required: (!(settings.waterStartTime1 == null))
        }
        section("Water Group 2"){
            input name: "waterDeviceGroup2", type: "capability.waterSensor", title: "Water sensor(s)", required: false, multiple: true
            input name: "waterTalkOnWet2", type: "text", title: "Say this when wet:", required: false
            input name: "waterTalkOnDry2", type: "text", title: "Say this when dry:", required: false
            input name: "waterSpeechDevice2", type: state.speechDeviceType, title: "Talk with these text-to-speech devices (overrides default)", multiple: true, required: false
            input name: "waterModes2", type: "mode", title: "Talk when in these mode(s) (overrides default)", multiple: true, required: false
            input name: "waterStartTime2", type: "time", title: "Don't talk before (overrides default)", required: false, submitOnChange: true
            input name: "waterEndTime2", type: "time", title: "Don't talk after (overrides default)", required: (!(settings.waterStartTime2 == null))
        }
        section("Water Group 3"){
            input name: "waterDeviceGroup3", type: "capability.waterSensor", title: "Water sensor(s)", required: false, multiple: true
            input name: "waterTalkOnWet3", type: "text", title: "Say this when wet:", required: false
            input name: "waterTalkOnDry3", type: "text", title: "Say this when dry:", required: false
            input name: "waterSpeechDevice3", type: state.speechDeviceType, title: "Talk with these text-to-speech devices (overrides default)", multiple: true, required: false
            input name: "waterModes3", type: "mode", title: "Talk when in these mode(s) (overrides default)", multiple: true, required: false
            input name: "waterStartTime3", type: "time", title: "Don't talk before (overrides default)", required: false, submitOnChange: true
            input name: "waterEndTime3", type: "time", title: "Don't talk after (overrides default)", required: (!(settings.waterStartTime3 == null))
        }
        section("Help"){
            href "pageHelpPhraseTokens", title:"Phrase Tokens", description:"Tap for a list of phrase tokens"
        }
    }
//End pageConfigWater()
}

def pageConfigSmoke(){
    dynamicPage(name: "pageConfigSmoke", title: "Configure talk on smoke", install: false, uninstall: false) {
        section("Smoke Group 1"){
            def defaultSpeechDetect1 = ""
            def defaultSpeechClear1 = ""
            def defaultSpeechTest1 = ""
            if (!smokeDeviceGroup1) {
                defaultSpeechDetect1 = "Smoke, %devicename% has detected smoke"
                defaultSpeechClear1 = "Smoke, %devicename% has cleared smoke alert"
                defaultSpeechTest1 = "Smoke, %devicename% has been tested"
            }
            input name: "smokeDeviceGroup1", type: "capability.smokeDetector", title: "Smoke detector(s)", required: false, multiple: true
            input name: "smokeTalkOnDetect1", type: "text", title: "Say this when detected:", required: false, defaultValue: defaultSpeechDetect1
            input name: "smokeTalkOnClear1", type: "text", title: "Say this when cleared:", required: false, defaultValue: defaultSpeechClear1
            input name: "smokeTalkOnTest1", type: "text", title: "Say this when tested:", required: false, defaultValue: defaultSpeechTest1
            input name: "smokeSpeechDevice1", type: state.speechDeviceType, title: "Talk with these text-to-speech devices (overrides default)", multiple: true, required: false
            input name: "smokeModes1", type: "mode", title: "Talk when in these mode(s) (overrides default)", multiple: true, required: false
            input name: "smokeStartTime1", type: "time", title: "Don't talk before (overrides default)", required: false, submitOnChange: true
            input name: "smokeEndTime1", type: "time", title: "Don't talk after (overrides default)", required: (!(settings.smokeStartTime1 == null))
        }
        section("Smoke Group 2"){
            input name: "smokeDeviceGroup2", type: "capability.smokeDetector", title: "Smoke detector(s)", required: false, multiple: true
            input name: "smokeTalkOnDetect2", type: "text", title: "Say this when detected:", required: false
            input name: "smokeTalkOnClear2", type: "text", title: "Say this when cleared:", required: false
            input name: "smokeTalkOnTest2", type: "text", title: "Say this when tested:", required: false
            input name: "smokeSpeechDevice2", type: state.speechDeviceType, title: "Talk with these text-to-speech devices (overrides default)", multiple: true, required: false
            input name: "smokeModes2", type: "mode", title: "Talk when in these mode(s) (overrides default)", multiple: true, required: false
            input name: "smokeStartTime2", type: "time", title: "Don't talk before (overrides default)", required: false, submitOnChange: true
            input name: "smokeEndTime2", type: "time", title: "Don't talk after (overrides default)", required: (!(settings.smokeStartTime2 == null))
        }
        section("Smoke Group 3"){
            input name: "smokeDeviceGroup3", type: "capability.smokeDetector", title: "Smoke detector(s)", required: false, multiple: true
            input name: "smokeTalkOnDetect3", type: "text", title: "Say this when detected:", required: false
            input name: "smokeTalkOnClear3", type: "text", title: "Say this when cleared:", required: false
            input name: "smokeTalkOnTest3", type: "text", title: "Say this when tested:", required: false
            input name: "smokeSpeechDevice3", type: state.speechDeviceType, title: "Talk with these text-to-speech devices (overrides default)", multiple: true, required: false
            input name: "smokeModes3", type: "mode", title: "Talk when in these mode(s) (overrides default)", multiple: true, required: false
            input name: "smokeStartTime3", type: "time", title: "Don't talk before (overrides default)", required: false, submitOnChange: true
            input name: "smokeEndTime3", type: "time", title: "Don't talk after (overrides default)", required: (!(settings.smokeStartTime3 == null))
        }
        section("Help"){
            href "pageHelpPhraseTokens", title:"Phrase Tokens", description:"Tap for a list of phrase tokens"
        }
    }
//End pageConfigSmoke()
}

def pageConfigButton(){
    dynamicPage(name: "pageConfigButton", title: "Configure talk on button press", install: false, uninstall: false) {
        section("Button Group 1"){
            def defaultSpeechButton1 = ""
            if (!buttonDeviceGroup1) {
                defaultSpeechButton1 = "%devicename% button pressed"
            }
            input name: "buttonDeviceGroup1", type: "capability.button", title: "Button(s)", required: false, multiple: true
            input name: "buttonTalkOnPress1", type: "text", title: "Say this when pressed:", required: false, defaultValue: defaultSpeechButton1
            input name: "buttonSpeechDevice1", type: state.speechDeviceType, title: "Talk with these text-to-speech devices (overrides default)", multiple: true, required: false
            input name: "buttonModes1", type: "mode", title: "Talk when in these mode(s) (overrides default)", multiple: true, required: false
            input name: "buttonStartTime1", type: "time", title: "Don't talk before (overrides default)", required: false, submitOnChange: true
            input name: "buttonEndTime1", type: "time", title: "Don't talk after (overrides default)", required: (!(settings.buttonStartTime1 == null))
        }
        section("Button Group 2"){
            input name: "buttonDeviceGroup2", type: "capability.button", title: "Button(s)", required: false, multiple: true
            input name: "buttonTalkOnPress2", type: "text", title: "Say this when pressed:", required: false
            input name: "buttonSpeechDevice2", type: state.speechDeviceType, title: "Talk with these text-to-speech devices (overrides default)", multiple: true, required: false
            input name: "buttonModes2", type: "mode", title: "Talk when in these mode(s) (overrides default)", multiple: true, required: false
            input name: "buttonStartTime2", type: "time", title: "Don't talk before (overrides default)", required: false, submitOnChange: true
            input name: "buttonEndTime2", type: "time", title: "Don't talk after (overrides default)", required: (!(settings.buttonStartTime2 == null))
        }
        section("Button Group 3"){
            input name: "buttonDeviceGroup3", type: "capability.button", title: "Button(s)", required: false, multiple: true
            input name: "buttonTalkOnPress3", type: "text", title: "Say this when pressed:", required: false
            input name: "buttonSpeechDevice3", type: state.speechDeviceType, title: "Talk with these text-to-speech devices (overrides default)", multiple: true, required: false
            input name: "buttonModes3", type: "mode", title: "Talk when in these mode(s) (overrides default)", multiple: true, required: false
            input name: "buttonStartTime3", type: "time", title: "Don't talk before (overrides default)", required: false, submitOnChange: true
            input name: "buttonEndTime3", type: "time", title: "Don't talk after (overrides default)", required: (!(settings.buttonStartTime3 == null))
        }
        section("Help"){
            href "pageHelpPhraseTokens", title:"Phrase Tokens", description:"Tap for a list of phrase tokens"
        }
    }
//End pageConfigSmoke()
}

def pageConfigTime(){
    dynamicPage(name: "pageConfigTime", title: "Configure talk at specific time(s)", install: false, uninstall: false) {
        section("Time Slot 1"){
            input name: "timeSlotDays1", type: "enum", title: "Which day(s)", required: false, options: ["Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"], multiple: true
            input name: "timeSlotTime1", type: "time", title: "Time of day", required: false
            input name: "timeSlotOnTime1", type: "text", title: "Say on schedule:", required: false
            input name: "timeSlotSpeechDevice1", type: state.speechDeviceType, title: "Talk with these text-to-speech devices (overrides default)", multiple: true, required: false
            input name: "timeSlotModes1", type: "mode", title: "Talk when in these mode(s) (overrides default)", multiple: true, required: false
        }
        section("Time Slot 2"){
            input name: "timeSlotDays2", type: "enum", title: "Which day(s)", required: false, options: ["Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"], multiple: true
            input name: "timeSlotTime2", type: "time", title: "Time of day", required: false
            input name: "timeSlotOnTime2", type: "text", title: "Say on schedule:", required: false
            input name: "timeSlotSpeechDevice2", type: state.speechDeviceType, title: "Talk with these text-to-speech devices (overrides default)", multiple: true, required: false
            input name: "timeSlotModes2", type: "mode", title: "Talk when in these mode(s) (overrides default)", multiple: true, required: false
        }
        section("Time Slot 3"){
            input name: "timeSlotDays3", type: "enum", title: "Which day(s)", required: false, options: ["Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"], multiple: true
            input name: "timeSlotTime3", type: "time", title: "Time of day", required: false
            input name: "timeSlotOnTime3", type: "text", title: "Say on schedule:", required: false
            input name: "timeSlotSpeechDevice3", type: state.speechDeviceType, title: "Talk with these text-to-speech devices (overrides default)", multiple: true, required: false
            input name: "timeSlotModes3", type: "mode", title: "Talk when in these mode(s) (overrides default)", multiple: true, required: false
        }
        section("Help"){
            href "pageHelpPhraseTokens", title:"Phrase Tokens", description:"Tap for a list of phrase tokens"
        }
    }
}

def installed() {
	state.installed = true
    //LOGTRACE("Installed with settings: ${settings}")
    LOGTRACE("Installed")
	initialize()
    myRunIn(60, poll)
//End installed()
}

def updated() {
    unschedule()
    state.installed = true
	//LOGTRACE("Updated with settings: ${settings}")
    LOGTRACE("Updated settings")
    unsubscribe()
    initialize()
    myRunIn(60, poll)
//End updated()
}

def checkConfig() {
    def configErrorList = ""
    if (!(state.speechDeviceType)){
       state.speechDeviceType = "capability.musicPlayer" //Set a default if the app was update and didn't contain settings.speechDeviceType
    }
    if (!(settings.speechDeviceDefault)){
        configErrorList += "  ** Default speech device(s) not selected,"
    }
    if (!(state.installed == true)) {
	    configErrorList += "  ** state.installed not True,"
	}
    if (!(configErrorList == "")) { 
        LOGDEBUG ("checkConfig() returning FALSE (${configErrorList})")
        state.configOK = false
        return false //Errors occurred.  Config check failed.
    } else {
        LOGDEBUG ("checkConfig() returning TRUE (${configErrorList})")
        state.configOK = true
        return true
    }
}

def initialize() {
    setAppVersion()
    if (!(checkConfig())) { 
        def msg = ""
        msg = "ERROR: App not properly configured!  Can't start.\n"
        msg += "ERRORs:\n${state.configErrorList}"
        LOGTRACE(msg)
        sendNotificationEvent(msg)
        return //App not properly configured, exit, don't subscribe
    }
    
    //WORKAROUND FOR "TimeoutException" Issue. Occurs when scheduling/subscribing to a lot of events possibly too soon after unschedule()/unsubscribe(); run in a seperate thread outside of initialize.
        //Reference:  http://community.smartthings.com/t/unschedule-api-execution-time-too-long/11232/15
    //schedule(now() + 30000, initSubscribe)
    //if (canSchedule()) {
    //    def initDelay=20
    //    runIn(initDelay, initSubscribe)
    //    runIn(initDelay+10, initSchedule)
    //    LOGDEBUG ("Scheduled initSubscribe() in ${initDelay} seconds")
    //} else {
    //    msg = "ERROR: Cannot schedule initSubscribe(); Only 4 schedules can run at a time per SmartThings!"
    //    LOGTRACE(msg)
    //    sendNotification(msg)
    //}
    initSchedule()
    initSubscribe()
    LOGTRACE("Initialized")
    sendNotificationEvent("${app.label.replace(" ","").toUpperCase()}: Settings activated")
    state.lastMode = location.mode
    state.lastTalkNow = settings.speechTalkNow
//End initialize()
}

def initSubscribe(){
    //NOTICE: Only call from initialize()!
    LOGDEBUG ("BEGIN initSubscribe()")
    //Subscribe Motions
    if (motionDeviceGroup1) { subscribe(motionDeviceGroup1, "motion", onMotion1Event) }
    if (motionDeviceGroup2) { subscribe(motionDeviceGroup2, "motion", onMotion2Event) }
    if (motionDeviceGroup3) { subscribe(motionDeviceGroup3, "motion", onMotion3Event) }
    //Subscribe Switches
    if (switchDeviceGroup1) { subscribe(switchDeviceGroup1, "switch", onSwitch1Event) }
    if (switchDeviceGroup2) { subscribe(switchDeviceGroup2, "switch", onSwitch2Event) }
    if (switchDeviceGroup3) { subscribe(switchDeviceGroup3, "switch", onSwitch3Event) }
    //Subscribe Presence
    if (presDeviceGroup1) { subscribe(presDeviceGroup1, "presence", onPresence1Event) }
    if (presDeviceGroup2) { subscribe(presDeviceGroup2, "presence", onPresence2Event) }
    if (presDeviceGroup3) { subscribe(presDeviceGroup3, "presence", onPresence3Event) }
    //Subscribe Lock
    if (lockDeviceGroup1) { subscribe(lockDeviceGroup1, "lock", onLock1Event) }
    if (lockDeviceGroup2) { subscribe(lockDeviceGroup2, "lock", onLock2Event) }
    if (lockDeviceGroup3) { subscribe(lockDeviceGroup3, "lock", onLock3Event) }
    //Subscribe Contact
    if (contactDeviceGroup1) { subscribe(contactDeviceGroup1, "contact", onContact1Event) }
    if (contactDeviceGroup2) { subscribe(contactDeviceGroup2, "contact", onContact2Event) }
    if (contactDeviceGroup3) { subscribe(contactDeviceGroup3, "contact", onContact3Event) }
    def contact1StartTime = settings.contact1StartTime ?: null
    //Subscribe Thermostat
    if (thermostatDeviceGroup1) { subscribe(thermostatDeviceGroup1, "thermostatOperatingState", onThermostat1Event) }
    if (thermostatDeviceGroup2) { subscribe(thermostatDeviceGroup2, "thermostatOperatingState", onThermostat2Event) }
    if (thermostatDeviceGroup3) { subscribe(thermostatDeviceGroup3, "thermostatOperatingState", onThermostat3Event) }
    //Subscribe Acceleration
    if (accelerationDeviceGroup1) { subscribe(accelerationDeviceGroup1, "acceleration", onAcceleration1Event) }
    if (accelerationDeviceGroup2) { subscribe(accelerationDeviceGroup2, "acceleration", onAcceleration2Event) }
    if (accelerationDeviceGroup3) { subscribe(accelerationDeviceGroup3, "acceleration", onAcceleration3Event) }
    //Subscribe Water
    if (waterDeviceGroup1) { subscribe(waterDeviceGroup1, "water", onWater1Event) }
    if (waterDeviceGroup2) { subscribe(waterDeviceGroup2, "water", onWater2Event) }
    if (waterDeviceGroup3) { subscribe(waterDeviceGroup3, "water", onWater3Event) }
    //Subscribe Smoke
    if (smokeDeviceGroup1) { subscribe(smokeDeviceGroup1, "smoke", onSmoke1Event) }
    if (smokeDeviceGroup2) { subscribe(smokeDeviceGroup2, "smoke", onSmoke2Event) }
    if (smokeDeviceGroup3) { subscribe(smokeDeviceGroup3, "smoke", onSmoke3Event) }
    //Subscribe Button
    if (buttonDeviceGroup1) { subscribe(buttonDeviceGroup1, "button", onButton1Event) }
    if (buttonDeviceGroup2) { subscribe(buttonDeviceGroup2, "button", onButton2Event) }
    if (buttonDeviceGroup3) { subscribe(buttonDeviceGroup3, "button", onButton3Event) }
    //Subscribe Mode
    if (modePhraseGroup1) { subscribe(location, onModeChangeEvent) }
    
    LOGDEBUG ("END initSubscribe()")
}

def initSchedule(){
    LOGDEBUG ("BEGIN initSchedule()")
    //Subscribe Schedule
    if (timeSlotTime1) { schedule(timeSlotTime1, onSchedule1Event) }
    if (timeSlotTime2) { schedule(timeSlotTime2, onSchedule2Event) }
    if (timeSlotTime3) { schedule(timeSlotTime3, onSchedule3Event) }
    LOGDEBUG ("END initSchedule()")
}

//BEGIN HANDLE TIME SCHEDULE
def onSchedule1Event(){
    processScheduledEvent(1, timeSlotTime1, timeSlotDays1)
}
def onSchedule2Event(){
    processScheduledEvent(2, timeSlotTime2, timeSlotDays2)
}
def onSchedule3Event(){
    processScheduledEvent(3, timeSlotTime3, timeSlotDays3)
}

def processScheduledEvent(index, eventtime, alloweddays){
    def calendar = Calendar.getInstance()
	calendar.setTimeZone(location.timeZone)
	def today = calendar.get(Calendar.DAY_OF_WEEK)
    def todayStr = ""
    def dayMatch = false
    switch (today) {
        case Calendar.MONDAY: todayStr = "MONDAY"; break
		case Calendar.TUESDAY:  todayStr = "TUESDAY"; break
		case Calendar.WEDNESDAY:  todayStr = "WEDNESDAY"; break
		case Calendar.THURSDAY:  todayStr = "THURSDAY"; break
        case Calendar.FRIDAY:  todayStr = "FRIDAY"; break
        case Calendar.SATURDAY:  todayStr = "SATURDAY"; break
        case Calendar.SUNDAY:  todayStr = "SUNDAY"; break
    }
    //LOGDEBUG("today=${today}, MON=${Calendar.MONDAY},TUE=${Calendar.TUESDAY},WED=${Calendar.WEDNESDAY},THUR=${Calendar.THURSDAY},FRI=${Calendar.FRIDAY},SAT=${Calendar.SATURDAY},SUN=${Calendar.SUNDAY}")
    def timeNow = getTimeFromDateString(eventtime, true)
    LOGDEBUG("(onScheduledEvent): ${timeNow}, ${index}, ${todayStr.toUpperCase()}, ${alloweddays.each(){return it.toUpperCase()}}")
    alloweddays.each(){
        if (todayStr.toUpperCase() == it.toUpperCase()) {
            LOGDEBUG("Time and day match schedule")
            dayMatch = true
            if (!(modeAllowed("timeSlot",index))) { 
               LOGDEBUG("Remain silent while in mode ${location.mode}")
               return
            }
            if (index == 1) { state.TalkPhrase = settings.timeSlotOnTime1; state.speechDevice = timeSlotSpeechDevice1 }
            if (index == 2) { state.TalkPhrase = settings.timeSlotOnTime2; state.speechDevice = timeSlotSpeechDevice2 }
            if (index == 3) { state.TalkPhrase = settings.timeSlotOnTime3; state.speechDevice = timeSlotSpeechDevice3 }
            def customevent = [displayName: 'BigTalker:OnSchedule', name: 'OnSchedule', value: "${todayStr}@${timeNow}"]
            state.TalkPhrase = processPhraseVariables(state.TalkPhrase, customevent)
            Talk(state.TalkPhrase, state.speechDevice, customevent)
        }
    }
    if (!dayMatch) { LOGDEBUG("Time matches, but day does not match schedule; remaining silent") }
    state.TalkPhrase = null
    state.speechDevice = null
}

//BEGIN HANDLE MOTIONS
def onMotion1Event(evt){
    processMotionEvent(1, evt)
}
def onMotion2Event(evt){
    processMotionEvent(2, evt)
}
def onMotion3Event(evt){
    processMotionEvent(3, evt)
}

def processMotionEvent(index, evt){
    LOGDEBUG("(onMotionEvent): ${evt.name}, ${index}, ${evt.value}")
    //Are we in an allowed time period?
    if (!(timeAllowed("motion",index))) {
        LOGDEBUG("Remain silent in current time period")
        return
    }
    //Are we in a talking mode?
    if (!(modeAllowed("motion",index))) { 
        LOGDEBUG("Remain silent while in mode ${location.mode}")
        return
    }
    state.TalkPhrase = null
    state.speechDevice = null
    if (evt.value == "active") {
        if (index == 1) { state.TalkPhrase = settings.motionTalkActive1; state.speechDevice = motionSpeechDevice1}
        if (index == 2) { state.TalkPhrase = settings.motionTalkActive2; state.speechDevice = motionSpeechDevice2}
        if (index == 3) { state.TalkPhrase = settings.motionTalkActive3; state.speechDevice = motionSpeechDevice3}
        Talk(state.TalkPhrase, state.speechDevice, evt)
    }
    if (evt.value == "inactive") {
        if (index == 1) { state.TalkPhrase = settings.motionTalkInactive1; state.speechDevice = motionSpeechDevice1}
        if (index == 2) { state.TalkPhrase = settings.motionTalkInactive2; state.speechDevice = motionSpeechDevice2}
        if (index == 3) { state.TalkPhrase = settings.motionTalkInactive3; state.speechDevice = motionSpeechDevice3}
        Talk(state.TalkPhrase, state.speechDevice, evt)
    }
    state.TalkPhrase = null
    state.speechDevice = null
}
//END HANDLE MOTIONS


//BEGIN HANDLE SWITCHES
def onSwitch1Event(evt){
    processSwitchEvent(1, evt)
}

def onSwitch2Event(evt){
    processSwitchEvent(2, evt)
}

def onSwitch3Event(evt){
    processSwitchEvent(3, evt)
}

def processSwitchEvent(index, evt){
    LOGDEBUG("(onSwitchEvent): ${evt.name}, ${index}, ${evt.value}")
    //Are we in an allowed time period?
    if (!(timeAllowed("switch",index))) {
        LOGDEBUG("Remain silent in current time period")
        return
    }
    //Are we in a talking mode?
    if (!(modeAllowed("switch",index))) { 
        LOGDEBUG("Remain silent while in mode ${location.mode}")
        return
    }
    state.TalkPhrase = null
    state.speechDevice = null
    if (evt.value == "on") {
        if (index == 1) { state.TalkPhrase = settings.switchTalkOn1; state.speechDevice = switchSpeechDevice1}
        if (index == 2) { state.TalkPhrase = settings.switchTalkOn2; state.speechDevice = switchSpeechDevice2}
        if (index == 3) { state.TalkPhrase = settings.switchTalkOn3; state.speechDevice = switchSpeechDevice3}
        Talk(state.TalkPhrase, state.speechDevice, evt)
    }
    if (evt.value == "off") {
        if (index == 1) { state.TalkPhrase = settings.switchTalkOff1; state.speechDevice = switchSpeechDevice1}
        if (index == 2) { state.TalkPhrase = settings.switchTalkOff2; state.speechDevice = switchSpeechDevice2}
        if (index == 3) { state.TalkPhrase = settings.switchTalkOff3; state.speechDevice = switchSpeechDevice3}
        Talk(state.TalkPhrase, state.speechDevice, evt)
    }
    state.TalkPhrase = null
    state.speechDevice = null
}
//END HANDLE SWITCHES

//BEGIN HANDLE PRESENCE
def onPresence1Event(evt){
    processPresenceEvent(1, evt)
}
def onPresence2Event(evt){
    processPresenceEvent(2, evt)
}
def onPresence3Event(evt){
    processPresenceEvent(3, evt)
}

def processPresenceEvent(index, evt){
    LOGDEBUG("(onPresenceEvent): ${evt.name}, ${index}, ${evt.value}")
    //Are we in an allowed time period?
    if (!(timeAllowed("presence",index))) {
        LOGDEBUG("Remain silent in current time period")
        return
    }
    //Are we in a talking mode?
    if (!(modeAllowed("presence",index))) { 
        LOGDEBUG("Remain silent while in mode ${location.mode}")
        return
    }
    state.TalkPhrase = null
    state.speechDevice = null
    if (evt.value == "present") {
        if (index == 1) { state.TalkPhrase = settings.presTalkOnArrive1; state.speechDevice = presSpeechDevice1}
        if (index == 2) { state.TalkPhrase = settings.presTalkOnArrive2; state.speechDevice = presSpeechDevice2}
        if (index == 3) { state.TalkPhrase = settings.presTalkOnArrive3; state.speechDevice = presSpeechDevice3}
        Talk(state.TalkPhrase, state.speechDevice, evt)
    }
    if (evt.value == "not present") {
        if (index == 1) { state.TalkPhrase = settings.presTalkOnLeave1; state.speechDevice = presSpeechDevice1}
        if (index == 2) { state.TalkPhrase = settings.presTalkOnLeave2; state.speechDevice = presSpeechDevice2}
        if (index == 3) { state.TalkPhrase = settings.presTalkOnLeave3; state.speechDevice = presSpeechDevice3}
        Talk(state.TalkPhrase, state.speechDevice, evt)
    }
    state.TalkPhrase = null
    state.speechDevice = null
}
//END HANDLE PRESENCE

//BEGIN HANDLE LOCK
def onLock1Event(evt){
    LOGDEBUG("onLock1Event(evt) ${evt.value}")
    processLockEvent(1, evt)
}
def onLock2Event(evt){
    processLockEvent(2, evt)
}
def onLockEvent(evt){
    processLockEvent(3, evt)
}

def processLockEvent(index, evt){
    LOGDEBUG("(onLockEvent): ${evt.name}, ${index}, ${evt.value}")
    //Are we in an allowed time period?
    if (!(timeAllowed("lock",index))) {
        LOGDEBUG("Remain silent in current time period")
        return
    }
    //Are we in a talking mode?
    if (!(modeAllowed("lock",index))) { 
        LOGDEBUG("Remain silent while in mode ${location.mode}")
        return
    }
    state.TalkPhrase = null
    state.speechDevice = null
    if (evt.value == "locked") {
        if (index == 1) { state.TalkPhrase = settings.lockTalkOnLock1; state.speechDevice = lockSpeechDevice1}
        if (index == 2) { state.TalkPhrase = settings.lockTalkOnLock2; state.speechDevice = lockSpeechDevice2}
        if (index == 3) { state.TalkPhrase = settings.lockTalkOnLock3; state.speechDevice = lockSpeechDevice3}
        Talk(state.TalkPhrase, state.speechDevice, evt)
    }
    if (evt.value == "unlocked") {
        if (index == 1) { state.TalkPhrase = settings.lockTalkOnUnlock1; state.speechDevice = lockSpeechDevice1}
        if (index == 2) { state.TalkPhrase = settings.lockTalkOnUnlock2; state.speechDevice = lockSpeechDevice2}
        if (index == 3) { state.TalkPhrase = settings.lockTalkOnUnlock3; state.speechDevice = lockSpeechDevice3}
        Talk(state.TalkPhrase, state.speechDevice, evt)
    }
    state.TalkPhrase = null
    state.speechDevice = null
}
//END HANDLE LOCK

//BEGIN HANDLE CONTACT
def onContact1Event(evt){
    processContactEvent(1, evt)
}
def onContact2Event(evt){
    processContactEvent(2, evt)
}
def onContactEvent(evt){
    processContactEvent(3, evt)
}

def processContactEvent(index, evt){
    LOGDEBUG("(onContactEvent): ${evt.name}, ${index}, ${evt.value}")
    //Are we in an allowed time period?
    if (!(timeAllowed("contact",index))) {
        LOGDEBUG("Remain silent in current time period")
        return
    }
    //Are we in a talking mode?
    if (!(modeAllowed("contact",index))) { 
        LOGDEBUG("Remain silent while in mode ${location.mode}")
        return
    }
    state.TalkPhrase = null
    state.speechDevice = null
    if (evt.value == "open") {
        if (index == 1) { state.TalkPhrase = settings.contactTalkOnOpen1; state.speechDevice = contactSpeechDevice1}
        if (index == 2) { state.TalkPhrase = settings.contactTalkOnOpen2; state.speechDevice = contactSpeechDevice2}
        if (index == 3) { state.TalkPhrase = settings.contactTalkOnOpen3; state.speechDevice = contactSpeechDevice3}
        Talk(state.TalkPhrase, state.speechDevice, evt)
    }
    if (evt.value == "closed") {
        if (index == 1) { state.TalkPhrase = settings.contactTalkOnClose1; state.speechDevice = contactSpeechDevice1}
        if (index == 2) { state.TalkPhrase = settings.contactTalkOnClose2; state.speechDevice = contactSpeechDevice2}
        if (index == 3) { state.TalkPhrase = settings.contactTalkOnClose3; state.speechDevice = contactSpeechDevice3}
        Talk(state.TalkPhrase, state.speechDevice, evt)
    }
    state.TalkPhrase = null
    state.speechDevice = null
}
//END HANDLE CONTACT

//BEGIN MODE CHANGE
def onModeChangeEvent(evt){
    processModeChangeEvent(1, evt)
}
def processModeChangeEvent(index, evt){
    LOGDEBUG("(onModeEvent): Last Mode: ${state.lastMode}, New Mode: ${location.mode}")
    //Are we in an allowed time period?
    if (!(timeAllowed("mode",index))) {
        LOGDEBUG("Remain silent in current time period")
        state.lastMode = location.mode
        return
    }
    if (settings.modePhraseGroup1.contains(location.mode)){
        if (!settings.modeExcludePhraseGroup1 == null){
            //settings.modeExcluePhraseGroup1 is not empty
            if (!(settings.modeExcludePhraseGroup1.contains(state.lastMode))) {
                //If we are not coming from an exclude mode, Talk.
                state.TalkPhrase = null
                state.speechDevice = null
                state.TalkPhrase = settings.TalkOnModeChange1; state.speechDevice = modePhraseSpeechDevice1
                Talk(state.TalkPhrase, state.speechDevice, evt)
                state.TalkPhrase = null
                state.speechDevice = null
            } else {
                LOGDEBUG("Mode change silent due to exclusion configuration (${state.lastMode} >> ${location.mode})")
            }
        } else {
            //settings.modeExcluePhraseGroup1 is empty, no exclusions, Talk.
            state.TalkPhrase = null
            state.speechDevice = null
            state.TalkPhrase = settings.TalkOnModeChange1; state.speechDevice = modePhraseSpeechDevice1
            Talk(state.TalkPhrase, state.speechDevice, evt)
            state.TalkPhrase = null
            state.speechDevice = null
        }
    }
    state.lastMode = location.mode
}
//END MODE CHANGE

//BEGIN HANDLE THERMOSTAT
def onThermostat1Event(evt){
    processThermostatEvent(1, evt)
}
def onThermostat2Event(evt){
    processThermostatEvent(2, evt)
}
def onThermostatEvent(evt){
    processThermostatEvent(3, evt)
}

def processThermostatEvent(index, evt){
    LOGDEBUG("(onThermostatEvent): ${evt.name}, ${index}, ${evt.value}")
    //Are we in an allowed time period?
    if (!(timeAllowed("thermostat",index))) {
        LOGDEBUG("Remain silent in current time period")
        return
    }
    //Are we in a talking mode?
    if (!(modeAllowed("thermostat",index))) { 
        LOGDEBUG("Remain silent while in mode ${location.mode}")
        return
    }
    state.TalkPhrase = null
    state.speechDevice = null
    if (evt.value == "idle") {
        if (index == 1) { state.TalkPhrase = settings.thermostatTalkOnIdle1; state.speechDevice = thermostatSpeechDevice1}
        if (index == 2) { state.TalkPhrase = settings.thermostatTalkOnIdle2; state.speechDevice = thermostatSpeechDevice2}
        if (index == 3) { state.TalkPhrase = settings.thermostatTalkOnIdle3; state.speechDevice = thermostatSpeechDevice3}
        Talk(state.TalkPhrase, state.speechDevice, evt)
    }
    if (evt.value == "heating") {
        if (index == 1) { state.TalkPhrase = settings.thermostatTalkOnHeating1; state.speechDevice = thermostatSpeechDevice1}
        if (index == 2) { state.TalkPhrase = settings.thermostatTalkOnHeating2; state.speechDevice = thermostatSpeechDevice2}
        if (index == 3) { state.TalkPhrase = settings.thermostatTalkOnHeating3; state.speechDevice = thermostatSpeechDevice3}
        Talk(state.TalkPhrase, state.speechDevice, evt)
    }
    if (evt.value == "cooling") {
        if (index == 1) { state.TalkPhrase = settings.thermostatTalkOnCooling1; state.speechDevice = thermostatSpeechDevice1}
        if (index == 2) { state.TalkPhrase = settings.thermostatTalkOnCooling2; state.speechDevice = thermostatSpeechDevice2}
        if (index == 3) { state.TalkPhrase = settings.thermostatTalkOnCooling3; state.speechDevice = thermostatSpeechDevice3}
        Talk(state.TalkPhrase, state.speechDevice, evt)
    }
    if (evt.value == "fan only") {
        if (index == 1) { state.TalkPhrase = settings.thermostatTalkOnFan1; state.speechDevice = thermostatSpeechDevice1}
        if (index == 2) { state.TalkPhrase = settings.thermostatTalkOnFan2; state.speechDevice = thermostatSpeechDevice2}
        if (index == 3) { state.TalkPhrase = settings.thermostatTalkOnFan3; state.speechDevice = thermostatSpeechDevice3}
        Talk(state.TalkPhrase, state.speechDevice, evt)
    }

    state.TalkPhrase = null
    state.speechDevice = null
}
//END HANDLE THERMOSTAT

//BEGIN HANDLE ACCELERATION
def onAcceleration1Event(evt){
    processAccelerationEvent(1, evt)
}
def onAcceleration2Event(evt){
    processAccelerationEvent(2, evt)
}
def onAcceleration3Event(evt){
    processAccelerationEvent(3, evt)
}

def processAccelerationEvent(index, evt){
    LOGDEBUG("(onAccelerationEvent): ${evt.name}, ${index}, ${evt.value}")
    //Are we in an allowed time period?
    if (!(timeAllowed("acceleration",index))) {
        LOGDEBUG("Remain silent in current time period")
        return
    }
    //Are we in a talking mode?
    if (!(modeAllowed("acceleration",index))) { 
        LOGDEBUG("Remain silent while in mode ${location.mode}")
        return
    }
    state.TalkPhrase = null
    state.speechDevice = null
    if (evt.value == "active") {
        if (index == 1) { state.TalkPhrase = settings.accelerationTalkOnActive1; state.speechDevice = accelerationSpeechDevice1}
        if (index == 2) { state.TalkPhrase = settings.accelerationTalkOnActive2; state.speechDevice = accelerationSpeechDevice2}
        if (index == 3) { state.TalkPhrase = settings.accelerationTalkOnActive3; state.speechDevice = accelerationSpeechDevice3}
        Talk(state.TalkPhrase, state.speechDevice, evt)
    }
    if (evt.value == "inactive") {
        if (index == 1) { state.TalkPhrase = settings.accelerationTalkOnInactive1; state.speechDevice = accelerationSpeechDevice1}
        if (index == 2) { state.TalkPhrase = settings.accelerationTalkOnInactive2; state.speechDevice = accelerationSpeechDevice2}
        if (index == 3) { state.TalkPhrase = settings.accelerationTalkOnInactive3; state.speechDevice = accelerationSpeechDevice3}
        Talk(state.TalkPhrase, state.speechDevice, evt)
    }
    state.TalkPhrase = null
    state.speechDevice = null
}
//END HANDLE ACCELERATION

//BEGIN HANDLE WATER
def onWater1Event(evt){
    processWaterEvent(1, evt)
}
def onWater2Event(evt){
    processWaterEvent(2, evt)
}
def onWater3Event(evt){
    processWaterEvent(3, evt)
}

def processWaterEvent(index, evt){
    LOGDEBUG("(onWaterEvent): ${evt.name}, ${index}, ${evt.value}")
    //Are we in an allowed time period?
    if (!(timeAllowed("water",index))) {
        LOGDEBUG("Remain silent in current time period")
        return
    }
    //Are we in a talking mode?
    if (!(modeAllowed("water",index))) { 
        LOGDEBUG("Remain silent while in mode ${location.mode}")
        return
    }
    state.TalkPhrase = null
    state.speechDevice = null
    if (evt.value == "wet") {
        if (index == 1) { state.TalkPhrase = settings.waterTalkOnWet1; state.speechDevice = waterSpeechDevice1}
        if (index == 2) { state.TalkPhrase = settings.waterTalkOnWet2; state.speechDevice = waterSpeechDevice2}
        if (index == 3) { state.TalkPhrase = settings.waterTalkOnWet3; state.speechDevice = waterSpeechDevice3}
        Talk(state.TalkPhrase, state.speechDevice, evt)
    }
    if (evt.value == "dry") {
        if (index == 1) { state.TalkPhrase = settings.waterTalkOnDry1; state.speechDevice = waterSpeechDevice1}
        if (index == 2) { state.TalkPhrase = settings.waterTalkOnDry2; state.speechDevice = waterSpeechDevice2}
        if (index == 3) { state.TalkPhrase = settings.waterTalkOnDry3; state.speechDevice = waterSpeechDevice3}
        Talk(state.TalkPhrase, state.speechDevice, evt)
    }
    state.TalkPhrase = null
    state.speechDevice = null
}
//END HANDLE WATER

//BEGIN HANDLE SMOKE
def onSmoke1Event(evt){
    processSmokeEvent(1, evt)
}
def onSmoke2Event(evt){
    processSmokeEvent(2, evt)
}
def onSmoke3Event(evt){
    processSmokeEvent(3, evt)
}

def processSmokeEvent(index, evt){
    LOGDEBUG("(onSmokeEvent): ${evt.name}, ${index}, ${evt.value}")
    //Are we in an allowed time period?
    if (!(timeAllowed("smoke",index))) {
        LOGDEBUG("Remain silent in current time period")
        return
    }
    //Are we in a talking mode?
    if (!(modeAllowed("smoke",index))) { 
        LOGDEBUG("Remain silent while in mode ${location.mode}")
        return
    }
    state.TalkPhrase = null
    state.speechDevice = null
    if (evt.value == "detected") {
        if (index == 1) { state.TalkPhrase = settings.smokeTalkOnDetect1; state.speechDevice = smokeSpeechDevice1}
        if (index == 2) { state.TalkPhrase = settings.smokeTalkOnDetect2; state.speechDevice = smokeSpeechDevice2}
        if (index == 3) { state.TalkPhrase = settings.smokeTalkOnDetect3; state.speechDevice = smokeSpeechDevice3}
        Talk(state.TalkPhrase, state.speechDevice, evt)
    }
    if (evt.value == "clear") {
        if (index == 1) { state.TalkPhrase = settings.smokeTalkOnClear1; state.speechDevice = smokeSpeechDevice1}
        if (index == 2) { state.TalkPhrase = settings.smokeTalkOnClear2; state.speechDevice = smokeSpeechDevice2}
        if (index == 3) { state.TalkPhrase = settings.smokeTalkOnClear3; state.speechDevice = smokeSpeechDevice3}
        Talk(state.TalkPhrase, state.speechDevice, evt)
    }
    if (evt.value == "tested") {
        if (index == 1) { state.TalkPhrase = settings.smokeTalkOnTest1; state.speechDevice = smokeSpeechDevice1}
        if (index == 2) { state.TalkPhrase = settings.smokeTalkOnTest2; state.speechDevice = smokeSpeechDevice2}
        if (index == 3) { state.TalkPhrase = settings.smokeTalkOnTest3; state.speechDevice = smokeSpeechDevice3}
        Talk(state.TalkPhrase, state.speechDevice, evt)
    }
    state.TalkPhrase = null
    state.speechDevice = null
}
//END HANDLE SMOKE

//BEGIN HANDLE BUTTON
def onButton1Event(evt){
    processButtonEvent(1, evt)
}
def onButton2Event(evt){
    processButtonEvent(2, evt)
}
def onButton3Event(evt){
    processButtonEvent(3, evt)
}

def processButtonEvent(index, evt){
    LOGDEBUG("(onButtonEvent): ${evt.name}, ${index}, ${evt.value}")
    //Are we in an allowed time period?
    if (!(timeAllowed("button",index))) {
        LOGDEBUG("Remain silent in current time period")
        return
    }
    //Are we in a talking mode?
    if (!(modeAllowed("button",index))) { 
        LOGDEBUG("Remain silent while in mode ${location.mode}")
        return
    }
    state.TalkPhrase = null
    state.speechDevice = null
    if (index == 1) { state.TalkPhrase = settings.buttonTalkOnPress1; state.speechDevice = buttonSpeechDevice1}
    if (index == 2) { state.TalkPhrase = settings.buttonTalkOnPress2; state.speechDevice = buttonSpeechDevice2}
    if (index == 3) { state.TalkPhrase = settings.buttonTalkOnPress3; state.speechDevice = buttonSpeechDevice3}
    Talk(state.TalkPhrase, state.speechDevice, evt)
    state.TalkPhrase = null
    state.speechDevice = null
}
//END HANDLE BUTTON

def processPhraseVariables(phrase, evt){
    def zipCode = location.zipCode
    if (phrase.toLowerCase().contains(" percent ")) { phrase = phrase.replace(" percent ","%") }
    if (phrase.toLowerCase().contains("%devicename%")) {phrase = phrase.toLowerCase().replace('%devicename%', evt.displayName)}  //User given name of the device
    if (phrase.toLowerCase().contains("%devicetype%")) {phrase = phrase.toLowerCase().replace('%devicetype%', evt.name)}  //Device type: motion, switch, etc...
    if (phrase.toLowerCase().contains("%devicechange%")) {phrase = phrase.toLowerCase().replace('%devicechange%', evt.value)}  //State change that occurred: on/off, active/inactive, etc...
    if (phrase.toLowerCase().contains("%locationname%")) {phrase = phrase.toLowerCase().replace('%locationname%', location.name)}
    if (phrase.toLowerCase().contains("%lastmode%")) {phrase = phrase.toLowerCase().replace('%lastmode%', state.lastMode)}
    if (phrase.toLowerCase().contains("%mode%")) {phrase = phrase.toLowerCase().replace('%mode%', location.mode)}
    if (phrase.toLowerCase().contains("%time%")) {phrase = phrase.toLowerCase().replace('%time%', getTimeFromCalendar(false,true))}
    if (phrase.toLowerCase().contains("%weathercurrent%")) {phrase = phrase.toLowerCase().replace('%weathercurrent%', getWeather("current", zipCode)); phrase = adjustWeatherPhrase(phrase)}
    if (phrase.toLowerCase().contains("%weathertoday%")) {phrase = phrase.toLowerCase().replace('%weathertoday%', getWeather("today", zipCode)); phrase = adjustWeatherPhrase(phrase)}
    if (phrase.toLowerCase().contains("%weathertonight%")) {phrase = phrase.toLowerCase().replace('%weathertonight%', getWeather("tonight", zipCode));phrase = adjustWeatherPhrase(phrase)}
    if (phrase.toLowerCase().contains("%weathertomorrow%")) {phrase = phrase.toLowerCase().replace('%weathertomorrow%', getWeather("tomorrow", zipCode));phrase = adjustWeatherPhrase(phrase)}
    if (phrase.toLowerCase().contains("%weathercurrent(")) {
        if (phrase.toLowerCase().contains(")%")) {
            def phraseZipStart = (phrase.toLowerCase().indexOf("%weathercurrent(") + 16)
            def phraseZipEnd = (phrase.toLowerCase().indexOf(")%"))
            zipCode = phrase.substring(phraseZipStart, phraseZipEnd)
            LOGDEBUG("Custom zipCode: ${zipCode}")
            phrase = phrase.toLowerCase().replace("%weathercurrent(${zipCode})%", getWeather("current", zipCode))
            phrase = adjustWeatherPhrase(phrase.toLowerCase())
        } else {
            phrase = "Custom Zip Code format error in request for current weather"
        }
    }
    if (phrase.toLowerCase().contains("%weathertoday(")) {
        if (phrase.contains(")%")) {
            def phraseZipStart = (phrase.toLowerCase().indexOf("%weathertoday(") + 14)
            def phraseZipEnd = (phrase.toLowerCase().indexOf(")%"))
            zipCode = phrase.substring(phraseZipStart, phraseZipEnd)
            LOGDEBUG("Custom zipCode: ${zipCode}")
            phrase = phrase.toLowerCase().replace("%weathertoday(${zipCode})%", getWeather("today", zipCode))
            phrase = adjustWeatherPhrase(phrase.toLowerCase())
        } else {
            phrase = "Custom Zip Code format error in request for today's weather"
        }
    }
    if (phrase.toLowerCase().contains("%weathertonight(")) {
        if (phrase.contains(")%")) {
            def phraseZipStart = (phrase.toLowerCase().indexOf("%weathertonight(") + 16)
            def phraseZipEnd = (phrase.toLowerCase().indexOf(")%"))
            zipCode = phrase.substring(phraseZipStart, phraseZipEnd)
            LOGDEBUG("Custom zipCode: ${zipCode}")
            phrase = phrase.toLowerCase().replace("%weathertonight(${zipCode})%", getWeather("tonight", zipCode))
            phrase = adjustWeatherPhrase(phrase)
        } else {
            phrase = "Custom Zip Code format error in request for tonight's weather"
        }
    }
    if (phrase.toLowerCase().contains("%weathertomorrow(")) {
        if (phrase.contains(")%")) {
            def phraseZipStart = (phrase.toLowerCase().indexOf("%weathertomorrow(") + 17)
            def phraseZipEnd = (phrase.toLowerCase().indexOf(")%"))
            zipCode = phrase.substring(phraseZipStart, phraseZipEnd)
            LOGDEBUG("Custom zipCode: ${zipCode}")
            phrase = phrase.toLowerCase().replace("%weathertomorrow(${zipCode})%", getWeather("tomorrow", zipCode))
            phrase = adjustWeatherPhrase(phrase)
        } else {
            phrase = "Custom ZipCode format error in request for tomorrow's weather"
        }
    }
    if (state.speechDeviceType == "capability.speechSynthesis"){
        //ST TTS Engine pronunces "Dash", so only convert for speechSynthesis devices (LANnouncer)
        if (phrase.contains(",")) { phrase = phrase.replace(","," - ") }
        //if (phrase.contains(".")) { phrase = phrase.replace("."," - ") }
    }
    if (phrase.contains('"')) { phrase = phrase.replace('"',"") }
    if (phrase.contains("'")) { phrase = phrase.replace("'","") }
    if (phrase.contains("10S")) { phrase = phrase.replace("10S","tens") }
    if (phrase.contains("20S")) { phrase = phrase.replace("20S","twenties") }
    if (phrase.contains("30S")) { phrase = phrase.replace("30S","thirties") }
    if (phrase.contains("40S")) { phrase = phrase.replace("40S","fourties") }
    if (phrase.contains("50S")) { phrase = phrase.replace("50S","fifties") }
    if (phrase.contains("60S")) { phrase = phrase.replace("60S","sixties") }
    if (phrase.contains("70S")) { phrase = phrase.replace("70S","seventies") }
    if (phrase.contains("80S")) { phrase = phrase.replace("80S","eighties") }
    if (phrase.contains("90S")) { phrase = phrase.replace("90S","nineties") }
    if (phrase.contains("100S")) { phrase = phrase.replace("100S","one hundreds") }
    if (phrase.contains("%")) { phrase = phrase.replace("%"," percent ") }
    return phrase
}

def adjustWeatherPhrase(phraseIn){
    def phraseOut = ""
    phraseOut = phraseIn.toUpperCase()
    phraseOut = phraseOut.replace(" N ", " North ")
    phraseOut = phraseOut.replace(" S ", " South ")
    phraseOut = phraseOut.replace(" E ", " East ")
    phraseOut = phraseOut.replace(" W ", " West ")
    phraseOut = phraseOut.replace(" NNE ", " North Northeast ")
    phraseOut = phraseOut.replace(" NNW ", " North Northwest ")
    phraseOut = phraseOut.replace(" SSE ", " South Southeast ")
    phraseOut = phraseOut.replace(" SSW ", " South Southwest ")
    phraseOut = phraseOut.replace(" ENE ", " East Northeast ")
    phraseOut = phraseOut.replace(" ESE ", " East Southeast ")
    phraseOut = phraseOut.replace(" WNW ", " West Northeast ")
    phraseOut = phraseOut.replace(" WSW ", " West Southwest ")
    phraseOut = phraseOut.replace(" MPH", " Miles Per Hour")
    phraseOut = phraseOut.replace(" MM)", " Milimeters ")
    return phraseOut
}

def Talk(phrase, customSpeechDevice, evt){
    def currentSpeechDevices = []
    if (state.speechDeviceType == "capability.musicPlayer"){
        state.sound = ""
        state.ableToTalk = false
        if (!(phrase == null)) {
            phrase = processPhraseVariables(phrase, evt)
            LOGTRACE("TALK(${evt.name}) |mP| >> ${phrase}")
            try {
                state.sound = textToSpeech(phrase instanceof List ? phrase[0] : phrase) 
                state.ableToTalk = true
            } catch(e) {
                LOGERROR("ST Platform issue (textToSpeech)? ${e}")
                //Try Again
                try {
                    LOGTRACE("Trying textToSpeech function again...")
                    state.sound = textToSpeech(phrase instanceof List ? phrase[0] : phrase)
                    state.ableToTalk = true
                } catch(ex) {
                    LOGERROR("ST Platform issue (textToSpeech)? I tried textToSpeech() twice, SmartThings wouldn't convert/process.  I give up, Sorry..")
                    sendNotificationEvent("ST Platform issue? textToSpeech() failed.")
                    sendNotification("BigTalker couldn't announce: ${phrase}")
                }
            }
            unschedule("poll")
            LOGDEBUG("Delaying polling for 120 seconds")
            myRunIn(120, poll)
            if (state.ableToTalk){
                state.sound.duration = (state.sound.duration.toInteger() + 5).toString()  //Try to prevent cutting out, add seconds to the duration
                if (!(customSpeechDevice == null)) {
                    currentSpeechDevices = customSpeechDevice
                } else {
                    //Use Default Speech Device
                    currentSpeechDevices = settings.speechDeviceDefault
                }
                LOGTRACE("Last poll: ${state.lastPoll}")
                //Iterate Speech Devices and talk
		        def attrs = currentSpeechDevices.supportedAttributes
                currentSpeechDevices.each(){
            	    //if (state.speechDeviceType == "capability.musicPlayer"){
                	    LOGDEBUG("attrs=${attrs}")
                	    def currentStatus = it.latestValue('status')
                	    def currentTrack = it.latestState("trackData")?.jsonValue
                	    def currentVolume = it.latestState("level")?.integerValue ? it.currentState("level")?.integerValue : 0
                	    LOGDEBUG("currentStatus:${currentStatus}")
                	    LOGDEBUG("currentTrack:${currentTrack}")
                	    LOGDEBUG("currentVolume:${currentVolume}")
                        LOGDEBUG("Sound: ${state.sound.uri} , ${state.sound.duration}")
                	    if (settings.speechVolume) { LOGTRACE("${it.displayName} | Volume: ${currentVolume}, Desired Volume: ${settings.speechVolume}") }
                	    if (!(settings.speechVolume)) { LOGTRACE("${it.displayName} | Volume: ${currentVolume}") }
                	    if (!(currentTrack == null)){
                    	    //currentTrack has data
                            if (!(currentTrack?.status == null)) { LOGTRACE("mP | ${it.displayName} | Current Status: ${currentStatus}, CurrentTrack: ${currentTrack}, CurrentTrack.Status: ${currentTrack.status}.") }
                    	    if (currentTrack?.status == null) { LOGTRACE("mP | ${it.displayName} | Current Status: ${currentStatus}, CurrentTrack: ${currentTrack}.") }
                    	    if (currentStatus == 'playing' || currentTrack?.status == 'playing') {
    	                        LOGTRACE("${it.displayName} | cT<>null | cS/cT=playing | Sending playTrackAndResume().")
        	                    if (settings.speechVolume) { 
                	                if (settings.speechVolume == currentVolume){it.playTrackAndResume(state.sound.uri, state.sound.duration)}
                                    if (!(settings.speechVolume == currentVolume)){it.playTrackAndResume(state.sound.uri, state.sound.duration, settings.speechVolume)}
                    	        } else { 
                            	    if (currentVolume > 50) { it.playTrackAndResume(state.sound.uri, state.sound.duration) }
                            	    if (currentVolume == 0) { it.playTrackAndResume(state.sound.uri, state.sound.duration, 75) }
                        	    }
                    	    } else
                    	    {
                        	    LOGTRACE("mP | ${it.displayName} | cT<>null | cS/cT<>playing | Sending playTrackAndRestore().")
                        	    if (settings.speechVolume) { 
	                                if (settings.speechVolume == currentVolume){it.playTrackAndRestore(state.sound.uri, state.sound.duration)}
                                    if (!(settings.speechVolume == currentVolume)){it.playTrackAndRestore(state.sound.uri, state.sound.duration, settings.speechVolume)}
	                            } else { 
            	                    if (currentVolume > 50) { it.playTrackAndRestore(state.sound.uri, state.sound.duration) }
                	                if (currentVolume == 0) { it.playTrackAndRestore(state.sound.uri, state.sound.duration, 75) }
                    	        }
                    	    }
                	    } else {
                    	    //currentTrack doesn't have data or is not supported on this device
                            if (!(currentStatus == null)) {
                    	        LOGTRACE("mP | ${it.displayName} | (2) Current Status: ${currentStatus}.")
                                if (currentStatus == "disconnected") {
	                                //VLCThing?
    	                            LOGTRACE("mP | ${it.displayName} | cT=null | cS=disconnected | Sending playTrackAndResume().")
	                                if (settings.speechVolume) { 
                    	                if (settings.speechVolume == currentVolume){it.playTrackAndResume(state.sound.uri, state.sound.duration)}
                                        if (!(settings.speechVolume == currentVolume)){it.playTrackAndResume(state.sound.uri, state.sound.duration, settings.speechVolume)}
                        	        } else { 
                                        if (currentVolume > 50) { it.playTrackAndResume(state.sound.uri, state.sound.duration) }
                	                    if (currentVolume == 0) { it.playTrackAndResume(state.sound.uri, state.sound.duration, 75) }
                            	        it.playTrackAndResume(state.sound.uri, state.sound.duration, settings.speechVolume)
                        	        }
                    	        } else {
    	                            if (currentStatus == "playing") {
            	                        LOGTRACE("mP | ${it.displayName} | cT=null | cS=playing | Sending playTrackAndResume().")
                	                    if (settings.speechVolume) { 
                        	                if (settings.speechVolume == currentVolume){it.playTrackAndResume(state.sound.uri, state.sound.duration)}
                                            if (!(settings.speechVolume == currentVolume)){it.playTrackAndResume(state.sound.uri, state.sound.duration, settings.speechVolume)}
                            	        } else { 
        	                                if (currentVolume > 50) { it.playTrackAndResume(state.sound.uri, state.sound.duration) }
            	                            if (currentVolume == 0) { it.playTrackAndResume(state.sound.uri, state.sound.duration, 75) }
                	                    }
                    	            } else {
                            	        LOGTRACE("mP | ${it.displayName} | cT=null | cS<>playing | Sending playTrackAndRestore().")
                            	        if (settings.speechVolume) { 
                                	        if (settings.speechVolume == currentVolume){it.playTrackAndRestore(state.sound.uri, state.sound.duration)}
                                            if (!(settings.speechVolume == currentVolume)){it.playTrackAndRestore(state.sound.uri, state.sound.duration, settings.speechVolume)}
                            	        } else { 
	                                        if (currentVolume > 50) { it.playTrackAndRestore(state.sound.uri, state.sound.duration) }
    	                                    if (currentVolume == 0) { it.playTrackAndRestore(state.sound.uri, state.sound.duration, 75) }
        	                            }
            	                    }
                	            }
                            } else {
                                //currentTrack and currentStatus are both null
                                LOGTRACE("mP | ${it.displayName} | (3) cT=null | cS=null | Sending playTrackAndRestore().")
                                if (settings.speechVolume) { 
                                    if (settings.speechVolume == currentVolume){it.playTrackAndRestore(state.sound.uri, state.sound.duration)}
                                    if (!(settings.speechVolume == currentVolume)){it.playTrackAndRestore(state.sound.uri, state.sound.duration, settings.speechVolume)}
                                } else { 
	                                if (currentVolume > 50) { it.playTrackAndRestore(state.sound.uri, state.sound.duration) }
    	                            if (currentVolume == 0) { it.playTrackAndRestore(state.sound.uri, state.sound.duration, 75) }
        	                    }
                            }
                	    }
                    } //currentSpeechDevices.each()
            	} //state.ableToTalk
            } //!phrase == null
        } else {
            //capability.speechSynthesis is in use
            if (!(phrase == null)) {
                phrase = processPhraseVariables(phrase, evt)
                LOGTRACE("TALK(${evt.name}) |sS| >> ${phrase}")
                if (!(customSpeechDevice == null)) {
                    currentSpeechDevices = customSpeechDevice
                } else {
                    //Use Default Speech Device
                    currentSpeechDevices = settings.speechDeviceDefault
                }
                //Iterate Speech Devices and talk
		        def attrs = currentSpeechDevices.supportedAttributes
                currentSpeechDevices.each(){
	                LOGTRACE("sS | ${it.displayName} | Sending speak().")
	                it.speak(phrase)
                }
    	    } //!phrase == null
        } //state.speechDeviceType
}//Talk()

def timeAllowed(devicetype,index){
    def now = new Date()
    //Check Default Setting
    //devicetype = mode, motion, switch, presence, lock, contact, thermostat, acceleration, water, smoke, button
    switch (devicetype) {
        case "mode":
            if (index == 1 && (!(settings.modeStartTime1 == null))) {
                if (timeOfDayIsBetween(settings.modeStartTime1, settings.modeEndTime1, now, location.timeZone)) { return true } else { return false }
            }
        case "motion":
            if (index == 1 && (!(settings.motionStartTime1 == null))) {
                if (timeOfDayIsBetween(settings.motionStartTime1, settings.motionEndTime1, now, location.timeZone)) { return true } else { return false }
            }
            if (index == 2 && (!(settings.motionStartTime2 == null))) {
                if (timeOfDayIsBetween(settings.motionStartTime2, settings.motionEndTime2, now, location.timeZone)) { return true } else { return false }
            }
            if (index == 3 && (!(settings.motionStartTime3 == null))) {
                if (timeOfDayIsBetween(settings.motionStartTime3, settings.motionEndTime3, now, location.timeZone)) { return true } else { return false }
            }
        case "switch":
            if (index == 1 && (!(settings.switchStartTime1 == null))) {
                    if (timeOfDayIsBetween(settings.switchStartTime1, settings.switchEndTime1, now, location.timeZone)) { return true } else { return false }
            }
            if (index == 2 && (!(settings.switchStartTime2 == null))) {
                if (timeOfDayIsBetween(settings.switchStartTime2, settings.switchEndTime2, now, location.timeZone)) { return true } else { return false }
            }
            if (index == 3 && (!(settings.switchStartTime3 == null))) {
                if (timeOfDayIsBetween(settings.switchStartTime3, settings.switchEndTime3, now, location.timeZone)) { return true } else { return false }
            }
        case "presence":
            if (index == 1 && (!(settings.presenceStartTime1 == null))) {
                if (timeOfDayIsBetween(settings.presenceStartTime1, settings.presenceEndTime1, now, location.timeZone)) { return true } else { return false }
            }
            if (index == 2 && (!(settings.presenceStartTime2 == null))) {
                if (timeOfDayIsBetween(settings.presenceStartTime2, settings.presenceEndTime2, now, location.timeZone)) { return true } else { return false }
            }
            if (index == 3 && (!(settings.presenceStartTime3 == null))) {
                if (timeOfDayIsBetween(settings.presenceStartTime3, settings.presenceEndTime3, now, location.timeZone)) { return true } else { return false }
            }
        case "lock":
            if (index == 1 && (!(settings.lockStartTime1 == null))) {
                if (timeOfDayIsBetween(settings.lockStartTime1, settings.lockEndTime1, now, location.timeZone)) { return true } else { return false }
            }
            if (index == 2 && (!(settings.lockStartTime2 == null))) {
                if (timeOfDayIsBetween(settings.lockStartTime2, settings.lockEndTime2, now, location.timeZone)) { return true } else { return false }
            }
            if (index == 3 && (!(settings.lockStartTime3 == null))) {
                if (timeOfDayIsBetween(settings.lockStartTime3, settings.lockEndTime3, now, location.timeZone)) { return true } else { return false }
            }
        case "contact":
            if (index == 1 && (!(settings.contactStartTime1 == null))) {
                if (timeOfDayIsBetween(settings.contactStartTime1, settings.contactEndTime1, now, location.timeZone)) { return true } else { return false }
            }
            if (index == 2 && (!(settings.contactStartTime2 == null))) {
                if (timeOfDayIsBetween(settings.contactStartTime2, settings.contactEndTime2, now, location.timeZone)) { return true } else { return false }
            }
            if (index == 3 && (!(settings.contactStartTime3 == null))) {
                if (timeOfDayIsBetween(settings.contactStartTime3, settings.contactEndTime3, now, location.timeZone)) { return true } else { return false }
            }
        case "thermostat":
            if (index == 1 && (!(settings.thermostatStartTime1 == null))) {
                if (timeOfDayIsBetween(settings.thermostatStartTime1, settings.thermostatEndTime1, now, location.timeZone)) { return true } else { return false }
            }
            if (index == 2 && (!(settings.thermostatStartTime2 == null))) {
                if (timeOfDayIsBetween(settings.thermostatStartTime2, settings.thermostatEndTime2, now, location.timeZone)) { return true } else { return false }
            }
            if (index == 3 && (!(settings.thermostatStartTime3 == null))) {
                if (timeOfDayIsBetween(settings.thermostatStartTime3, settings.thermostatEndTime3, now, location.timeZone)) { return true } else { return false }
            }
        case "acceleration":
            if (index == 1 && (!(settings.accelerationStartTime1 == null))) {
                if (timeOfDayIsBetween(settings.accelerationStartTime1, settings.accelerationEndTime1, now, location.timeZone)) { return true } else { return false }
            }
            if (index == 2 && (!(settings.accelerationStartTime2 == null))) {
                if (timeOfDayIsBetween(settings.accelerationStartTime2, settings.accelerationEndTime2, now, location.timeZone)) { return true } else { return false }
            }
            if (index == 3 && (!(settings.accelerationStartTime3 == null))) {
                if (timeOfDayIsBetween(settings.accelerationStartTime3, settings.accelerationEndTime3, now, location.timeZone)) { return true } else { return false }
            }
        case "water":
            if (index == 1 && (!(settings.waterStartTime1 == null))) {
                if (timeOfDayIsBetween(settings.waterStartTime1, settings.waterEndTime1, now, location.timeZone)) { return true } else { return false }
            }
            if (index == 2 && (!(settings.waterStartTime2 == null))) {
                    if (timeOfDayIsBetween(settings.waterStartTime2, settings.waterEndTime2, now, location.timeZone)) { return true } else { return false }
                }
            if (index == 3 && (!(settings.waterStartTime3 == null))) {
                if (timeOfDayIsBetween(settings.waterStartTime3, settings.waterEndTime3, now, location.timeZone)) { return true } else { return false }
            }
        case "smoke":
            if (index == 1 && (!(settings.smokeStartTime1 == null))) {
                if (timeOfDayIsBetween(settings.smokeStartTime1, settings.smokeEndTime1, now, location.timeZone)) { return true } else { return false }
            }
            if (index == 2 && (!(settings.smokeStartTime2 == null))) {
                if (timeOfDayIsBetween(settings.smokeStartTime2, settings.smokeEndTime2, now, location.timeZone)) { return true } else { return false }
            }
            if (index == 3 && (!(settings.smokeStartTime3 == null))) {
                if (timeOfDayIsBetween(settings.smokeStartTime3, settings.smokeEndTime3, now, location.timeZone)) { return true } else { return false }
            }
        case "button":
            if (index == 1 && (!(settings.buttonStartTime1 == null))) {
                if (timeOfDayIsBetween(settings.buttonStartTime1, settings.buttonEndTime1, now, location.timeZone)) { return true } else { return false }
            }
            if (index == 2 && (!(settings.buttonStartTime2 == null))) {
                if (timeOfDayIsBetween(settings.buttonStartTime2, settings.buttonEndTime2, now, location.timeZone)) { return true } else { return false }
            }
            if (index == 3 && (!(settings.buttonStartTime3 == null))) {
                if (timeOfDayIsBetween(settings.buttonStartTime3, settings.buttonEndTime3, now, location.timeZone)) { return true } else { return false }
            }
    }
    
    //No overrides have returned True, process Default
    if (settings.defaultStartTime == null) { 
    	return true 
    } else {
        if (timeOfDayIsBetween(settings.defaultStartTime, settings.defaultEndTime, now, location.timeZone)) { return true } else { return false }
    }
}

def modeAllowed(devicetype,index) {
    //Determine if we are allowed to speak in our current mode based on the calling device or default setting
    //devicetype = motion, switch, presence, lock, contact, thermostat, acceleration, water, smoke, button
    switch (devicetype) {
        case "motion":
            if (index == 1) {
                //Motion Group 1
                if (settings.motionModes1) {
                    if (settings.motionModes1.contains(location.mode)) {
                        //Custom mode for this event is in use and we are in one of those modes
                        return true
                    } else {
                        //Custom mode for this event is in use and we are not in one of those modes
                        return false
                    }
                } else {
                    return (settings.speechModesDefault.contains(location.mode)) //True if we are in an allowed Default mode, False if not
                }
            }
            if (index == 2) {
                //Motion Group 2
                if (settings.motionModes2) {
                    if (settings.motionModes2.contains(location.mode)) {
                        //Custom mode for this event is in use and we are in one of those modes
                        return true
                    } else {
                        //Custom mode for this event is in use and we are not in one of those modes
                        return false
                    }
                } else {
                    return (settings.speechModesDefault.contains(location.mode)) //True if we are in an allowed Default mode, False if not
                }
            }
            if (index == 3) {
                //Motion Group 3
                if (settings.motionModes3) {
                    if (settings.motionModes3.contains(location.mode)) {
                        //Custom mode for this event is in use and we are in one of those modes
                        return true
                    } else {
                        //Custom mode for this event is in use and we are not in one of those modes
                        return false
                    }
                } else {
                    return (settings.speechModesDefault.contains(location.mode)) //True if we are in an allowed Default mode, False if not
                }
            }
        //End: case "motion"
        case "switch":
            if (index == 1) {
                //Switch Group 1
                if (settings.switchModes1) {
                    if (settings.switchModes1.contains(location.mode)) {
                        //Custom mode for this event is in use and we are in one of those modes
                        return true
                    } else {
                        //Custom mode for this event is in use and we are not in one of those modes
                        return false
                    }
                } else {
                    return (settings.speechModesDefault.contains(location.mode)) //True if we are in an allowed Default mode, False if not
                }
            }
            if (index == 2) {
                //Switch Group 2
                if (settings.switchModes2) {
                    if (settings.switchModes2.contains(location.mode)) {
                        //Custom mode for this event is in use and we are in one of those modes
                        return true
                    } else {
                        //Custom mode for this event is in use and we are not in one of those modes
                        return false
                    }
                } else {
                    return (settings.speechModesDefault.contains(location.mode)) //True if we are in an allowed Default mode, False if not
                }
            }
            if (index == 3) {
                //Switch Group 3
                if (settings.switchModes3) {
                    if (settings.switchModes3.contains(location.mode)) {
                        //Custom mode for this event is in use and we are in one of those modes
                        return true
                    } else {
                        //Custom mode for this event is in use and we are not in one of those modes
                        return false
                    }
                } else {
                    return (settings.speechModesDefault.contains(location.mode)) //True if we are in an allowed Default mode, False if not
                }
            }
        //End: case "switch"
        case "presence":
            if (index == 1) {
                //Presence Group 1
                if (settings.presenceModes1) {
                    if (settings.presenceModes1.contains(location.mode)) {
                        //Custom mode for this event is in use and we are in one of those modes
                        return true
                    } else {
                        //Custom mode for this event is in use and we are not in one of those modes
                        return false
                    }
                } else {
                    return (settings.speechModesDefault.contains(location.mode)) //True if we are in an allowed Default mode, False if not
                }
            }
            if (index == 2) {
                //Presence Group 2
                if (settings.presenceModes2) {
                    if (settings.presenceModes2.contains(location.mode)) {
                        //Custom mode for this event is in use and we are in one of those modes
                        return true
                    } else {
                        //Custom mode for this event is in use and we are not in one of those modes
                        return false
                    }
                } else {
                    return (settings.speechModesDefault.contains(location.mode)) //True if we are in an allowed Default mode, False if not
                }
            }
            if (index == 3) {
                //Presence Group 3
                if (settings.presenceModes3) {
                    if (settings.presenceModes3.contains(location.mode)) {
                        //Custom mode for this event is in use and we are in one of those modes
                        return true
                    } else {
                        //Custom mode for this event is in use and we are not in one of those modes
                        return false
                    }
                } else {
                    return (settings.speechModesDefault.contains(location.mode)) //True if we are in an allowed Default mode, False if not
                }
            }
        //End: case "presence"
        case "lock":
            if (index == 1) {
                //Lock Group 1
                if (settings.lockModes1) {
                    if (settings.lockModes1.contains(location.mode)) {
                        //Custom mode for this event is in use and we are in one of those modes
                        return true
                    } else {
                        //Custom mode for this event is in use and we are not in one of those modes
                        return false
                    }
                } else {
                    return (settings.speechModesDefault.contains(location.mode)) //True if we are in an allowed Default mode, False if not
                }
            }
            if (index == 2) {
                //Lock Group 2
                if (settings.lockModes2) {
                    if (settings.lockModes2.contains(location.mode)) {
                        //Custom mode for this event is in use and we are in one of those modes
                        return true
                    } else {
                        //Custom mode for this event is in use and we are not in one of those modes
                        return false
                    }
                } else {
                    return (settings.speechModesDefault.contains(location.mode)) //True if we are in an allowed Default mode, False if not
                }
            }
            if (index == 3) {
                //Lock Group 3
                if (settings.lockModes3) {
                    if (settings.lockModes3.contains(location.mode)) {
                        //Custom mode for this event is in use and we are in one of those modes
                        return true
                    } else {
                        //Custom mode for this event is in use and we are not in one of those modes
                        return false
                    }
                } else {
                    return (settings.speechModesDefault.contains(location.mode)) //True if we are in an allowed Default mode, False if not
                }
            }
        //End: case "lock"
        case "contact":
            if (index == 1) {
                //Contact Group 1
                if (settings.contactModes1) {
                    if (settings.contactModes1.contains(location.mode)) {
                        //Custom mode for this event is in use and we are in one of those modes
                        return true
                    } else {
                        //Custom mode for this event is in use and we are not in one of those modes
                        return false
                    }
                } else {
                    return (settings.speechModesDefault.contains(location.mode)) //True if we are in an allowed Default mode, False if not
                }
            }
            if (index == 2) {
                //Contact Group 2
                if (settings.contactModes2) {
                    if (settings.contactModes2.contains(location.mode)) {
                        //Custom mode for this event is in use and we are in one of those modes
                        return true
                    } else {
                        //Custom mode for this event is in use and we are not in one of those modes
                        return false
                    }
                } else {
                    return (settings.speechModesDefault.contains(location.mode)) //True if we are in an allowed Default mode, False if not
                }
            }
            if (index == 3) {
                //Contact Group 3
                if (settings.contactModes3) {
                    if (settings.contactModes3.contains(location.mode)) {
                        //Custom mode for this event is in use and we are in one of those modes
                        return true
                    } else {
                        //Custom mode for this event is in use and we are not in one of those modes
                        return false
                    }
                } else {
                    return (settings.speechModesDefault.contains(location.mode)) //True if we are in an allowed Default mode, False if not
                }
            }
        //End: case "contact"
        case "thermostat":
            if (index == 1) {
                //Thermostat Group 1
                if (settings.thermostatModes1) {
                    if (settings.thermostatModes1.contains(location.mode)) {
                        //Custom mode for this event is in use and we are in one of those modes
                        return true
                    } else {
                        //Custom mode for this event is in use and we are not in one of those modes
                        return false
                    }
                } else {
                    return (settings.speechModesDefault.contains(location.mode)) //True if we are in an allowed Default mode, False if not
                }
            }
            if (index == 2) {
                //Thermostat Group 2
                if (settings.thermostatModes2) {
                    if (settings.thermostatModes2.contains(location.mode)) {
                        //Custom mode for this event is in use and we are in one of those modes
                        return true
                    } else {
                        //Custom mode for this event is in use and we are not in one of those modes
                        return false
                    }
                } else {
                    return (settings.speechModesDefault.contains(location.mode)) //True if we are in an allowed Default mode, False if not
                }
            }
            if (index == 3) {
                //Thermostat Group 3
                if (settings.thermostatModes3) {
                    if (settings.thermostatModes3.contains(location.mode)) {
                        //Custom mode for this event is in use and we are in one of those modes
                        return true
                    } else {
                        //Custom mode for this event is in use and we are not in one of those modes
                        return false
                    }
                } else {
                    return (settings.speechModesDefault.contains(location.mode)) //True if we are in an allowed Default mode, False if not
                }
            }
        //End: case "thermostat"
        case "acceleration":
            if (index == 1) {
                //Acceleration Group 1
                if (settings.accelerationModes1) {
                    if (settings.accelerationModes1.contains(location.mode)) {
                        //Custom mode for this event is in use and we are in one of those modes
                        return true
                    } else {
                        //Custom mode for this event is in use and we are not in one of those modes
                        return false
                    }
                } else {
                    return (settings.speechModesDefault.contains(location.mode)) //True if we are in an allowed Default mode, False if not
                }
            }
            if (index == 2) {
                //Acceleration Group 2
                if (settings.accelerationModes2) {
                    if (settings.accelerationModes2.contains(location.mode)) {
                        //Custom mode for this event is in use and we are in one of those modes
                        return true
                    } else {
                        //Custom mode for this event is in use and we are not in one of those modes
                        return false
                    }
                } else {
                    return (settings.speechModesDefault.contains(location.mode)) //True if we are in an allowed Default mode, False if not
                }
            }
            if (index == 3) {
                //Acceleration Group 3
                if (settings.accelerationModes3) {
                    if (settings.accelerationModes3.contains(location.mode)) {
                        //Custom mode for this event is in use and we are in one of those modes
                        return true
                    } else {
                        //Custom mode for this event is in use and we are not in one of those modes
                        return false
                    }
                } else {
                    return (settings.speechModesDefault.contains(location.mode)) //True if we are in an allowed Default mode, False if not
                }
            }
        //End: case "acceleration"
        case "water":
            if (index == 1) {
                //Water Group 1
                if (settings.waterModes1) {
                    if (settings.waterModes1.contains(location.mode)) {
                        //Custom mode for this event is in use and we are in one of those modes
                        return true
                    } else {
                        //Custom mode for this event is in use and we are not in one of those modes
                        return false
                    }
                } else {
                    return (settings.speechModesDefault.contains(location.mode)) //True if we are in an allowed Default mode, False if not
                }
            }
            if (index == 2) {
                //Water Group 2
                if (settings.waterModes2) {
                    if (settings.waterModes2.contains(location.mode)) {
                        //Custom mode for this event is in use and we are in one of those modes
                        return true
                    } else {
                        //Custom mode for this event is in use and we are not in one of those modes
                        return false
                    }
                } else {
                    return (settings.speechModesDefault.contains(location.mode)) //True if we are in an allowed Default mode, False if not
                }
            }
            if (index == 3) {
                //Water Group 3
                if (settings.waterModes3) {
                    if (settings.waterModes3.contains(location.mode)) {
                        //Custom mode for this event is in use and we are in one of those modes
                        return true
                    } else {
                        //Custom mode for this event is in use and we are not in one of those modes
                        return false
                    }
                } else {
                    return (settings.speechModesDefault.contains(location.mode)) //True if we are in an allowed Default mode, False if not
                }
            }
        //End: case "water"
        case "smoke":
            if (index == 1) {
                //Smoke Group 1
                if (settings.smokeModes1) {
                    if (settings.smokeModes1.contains(location.mode)) {
                        //Custom mode for this event is in use and we are in one of those modes
                        return true
                    } else {
                        //Custom mode for this event is in use and we are not in one of those modes
                        return false
                    }
                } else {
                    return (settings.speechModesDefault.contains(location.mode)) //True if we are in an allowed Default mode, False if not
                }
            }
            if (index == 2) {
                //Smoke Group 2
                if (settings.smokeModes2) {
                    if (settings.smokeModes2.contains(location.mode)) {
                        //Custom mode for this event is in use and we are in one of those modes
                        return true
                    } else {
                        //Custom mode for this event is in use and we are not in one of those modes
                        return false
                    }
                } else {
                    return (settings.speechModesDefault.contains(location.mode)) //True if we are in an allowed Default mode, False if not
                }
            }
            if (index == 3) {
                //Smoke Group 3
                if (settings.smokeModes3) {
                    if (settings.smokeModes3.contains(location.mode)) {
                        //Custom mode for this event is in use and we are in one of those modes
                        return true
                    } else {
                        //Custom mode for this event is in use and we are not in one of those modes
                        return false
                    }
                } else {
                    return (settings.speechModesDefault.contains(location.mode)) //True if we are in an allowed Default mode, False if not
                }
            }
        //End: case "smoke"
        case "button":
            if (index == 1) {
                //Button Group 1
                if (settings.buttonModes1) {
                    if (settings.buttonModes1.contains(location.mode)) {
                        //Custom mode for this event is in use and we are in one of those modes
                        return true
                    } else {
                        //Custom mode for this event is in use and we are not in one of those modes
                        return false
                    }
                } else {
                    return (settings.speechModesDefault.contains(location.mode)) //True if we are in an allowed Default mode, False if not
                }
            }
            if (index == 2) {
                //Button Group 2
                if (settings.buttonModes2) {
                    if (settings.buttonModes2.contains(location.mode)) {
                        //Custom mode for this event is in use and we are in one of those modes
                        return true
                    } else {
                        //Custom mode for this event is in use and we are not in one of those modes
                        return false
                    }
                } else {
                    return (settings.speechModesDefault.contains(location.mode)) //True if we are in an allowed Default mode, False if not
                }
            }
            if (index == 3) {
                //Button Group 3
                if (settings.buttonModes3) {
                    if (settings.buttonModes3.contains(location.mode)) {
                        //Custom mode for this event is in use and we are in one of those modes
                        return true
                    } else {
                        //Custom mode for this event is in use and we are not in one of those modes
                        return false
                    }
                } else {
                    return (settings.speechModesDefault.contains(location.mode)) //True if we are in an allowed Default mode, False if not
                }
            }
        //End: case "button"
        case "timeSlot":
            if (index == 1) {
                //TimeSlot Group 1
                if (settings.timeSlotModes1) {
                    if (settings.timeSlotModes1.contains(location.mode)) {
                        //Custom mode for this event is in use and we are in one of those modes
                        return true
                    } else {
                        //Custom mode for this event is in use and we are not in one of those modes
                        return false
                    }
                } else {
                    return (settings.speechModesDefault.contains(location.mode)) //True if we are in an allowed Default mode, False if not
                }
            }
            if (index == 2) {
                //TimeSlot Group 2
                if (settings.timeSlotModes2) {
                    if (settings.timeSlotModes2.contains(location.mode)) {
                        //Custom mode for this event is in use and we are in one of those modes
                        return true
                    } else {
                        //Custom mode for this event is in use and we are not in one of those modes
                        return false
                    }
                } else {
                    return (settings.speechModesDefault.contains(location.mode)) //True if we are in an allowed Default mode, False if not
                }
            }
            if (index == 3) {
                //TimeSlot Group 3
                if (settings.timeSlotModes3) {
                    if (settings.timeSlotModes3.contains(location.mode)) {
                        //Custom mode for this event is in use and we are in one of those modes
                        return true
                    } else {
                        //Custom mode for this event is in use and we are not in one of those modes
                        return false
                    }
                } else {
                    return (settings.speechModesDefault.contains(location.mode)) //True if we are in an allowed Default mode, False if not
                }
            }
        //End: case "timeSlot"
    } //End: switch (devicetype)
}

def getTimeFromDateString(inputtime, includeAmPm){
    //I couldn't find the way to do this in ST / Groovy, so I made my own function
    //Obtains the time from a supplied specifically formatted date string (ie: from a preference of type "time")
    //LOGDEBUG "InputTime: ${inputtime}"
    def outputtime = inputtime
    def am_pm = "??"
    outputtime = inputtime.substring(11,16)
    if (includeAmPm) {
        if ((outputtime.substring(0,2)).toInteger() < 12) { 
            am_pm = "am" 
        } else { 
            am_pm = "pm"
            def newHH = ((outputtime.substring(0,2)).toInteger() - 12)
            outputtime = newHH + outputtime.substring(2,5)
        }
        outputtime += am_pm
    }
    //LOGDEBUG "OutputTime: ${outputtime}"
    return outputtime
}

def getTimeFromCalendar(includeSeconds, includeAmPm){
    //Obtains the current time:  HH:mm:ss am/pm
    def calendar = Calendar.getInstance()
	calendar.setTimeZone(location.timeZone)
	def timeHH = calendar.get(Calendar.HOUR)
    def timemm = calendar.get(Calendar.MINUTE)
    def timess = calendar.get(Calendar.SECOND)
    def timeampm = calendar.get(Calendar.AM_PM) ? "pm" : "am"
    def timestring = "${timeHH}:${timemm}"
    if (includeSeconds) { timestring += ":${timess}" }
    if (includeAmPm) { timestring += " ${timeampm}" }
    return timestring
}

//myRunIn from ST:Geko / Statusbits SmartAlarm app http://statusbits.github.io/smartalarm/
private def myRunIn(delay_s, func) {
    //LOGDEBUG("myRunIn(${delay_s},${func})")

    if (delay_s > 0) {
        def tms = now() + (delay_s * 1000)
        def date = new Date(tms)
        runOnce(date, func)
        //LOGDEBUG("'${func}' scheduled to run at ${date}")
    }
}

def TalkQueue(phrase, customSpeechDevice, evt){
    //IN DEVELOPMENT
    // Already talking or just recently (within x seconds) started talking
    // Queue up current request(s), give time for current action to complete, then speak and flush queue
    LOGDEBUG("TALKQUEUE()")
}

def getWeather(mode, zipCode) {
    //Function derived from "Sonos Weather Forecast" SmartApp by Smartthings (modified)
    LOGDEBUG("Processing: getWeather(${mode},${zipCode})")
	def weather = getWeatherFeature("forecast", zipCode)
	def current = getWeatherFeature("conditions", zipCode)
	def isMetric = location.temperatureScale == "C"
	def delim = ""
	def sb = new StringBuilder()
	if (mode == "current") {
			if (isMetric) {
               	sb << "The current temperature is ${Math.round(current.current_observation.temp_c)} degrees."
            }
            else {
               	sb << "The current temperature is ${Math.round(current.current_observation.temp_f)} degrees."
            }
			delim = " "
	} //mode == current
    else if (mode == "today") {
		sb << delim
		sb << "Today's forecast is "
		if (isMetric) {
           	sb << weather.forecast.txt_forecast.forecastday[0].fcttext_metric 
        }
        else {
           	sb << weather.forecast.txt_forecast.forecastday[0].fcttext
        }
	} //mode == today
	else if (mode == "tonight") {
        sb << delim
		sb << "Tonight will be "
		if (isMetric) {
          	sb << weather.forecast.txt_forecast.forecastday[1].fcttext_metric 
        }
        else {
        	sb << weather.forecast.txt_forecast.forecastday[1].fcttext
        }
	} //mode == tonight
	else if (mode == "tomorrow") {
		sb << delim
		sb << "Tomorrow will be "
		if (isMetric) {
           	sb << weather.forecast.txt_forecast.forecastday[2].fcttext_metric 
        }
        else {
          	sb << weather.forecast.txt_forecast.forecastday[2].fcttext
        }
	} //mode == tomorrow
    else {
        sb < "ERROR: Requested weather mode was not recognized."
    }//mode = unknown
	def msg = sb.toString()
    msg = msg.replaceAll(/([0-9]+)C/,'$1 degrees celsius')
    msg = msg.replaceAll(/([0-9]+)F/,'$1 degrees fahrenheit')
    LOGDEBUG("msg = ${msg}")
	return(msg)
}

def poll(){
    if (state.speechDeviceType == "capability.musicPlayer") {
        LOGDEBUG("Polling speech device(s) for latest status")
        if (state?.polledDevices == null) { state.polledDevices = "!" }
        try {
            if (!(settings?.speechDeviceDefault == null)) {dopoll(settings.speechDeviceDefault)}
            if (!(settings?.motionSpeechDevice1 == null)) {dopoll(settings.motionSpeechDevice1)}
            if (!(settings?.motionSpeechDevice2 == null)) {dopoll(settings.motionSpeechDevice2)}
            if (!(settings?.motionSpeechDevice3 == null)) {dopoll(settings.motionSpeechDevice3)}
            if (!(settings?.switchSpeechDevice1 == null)) {dopoll(settings.switchSpeechDevice1)}
            if (!(settings?.switchSpeechDevice2 == null)) {dopoll(settings.switchSpeechDevice2)}
            if (!(settings?.switchSpeechDevice3 == null)) {dopoll(settings.switchSpeechDevice3)}
            if (!(settings?.presSpeechDevice1 == null)) {dopoll(settings.presSpeechDevice1)}
            if (!(settings?.presSpeechDevice2 == null)) {dopoll(settings.presSpeechDevice2)}
            if (!(settings?.presSpeechDevice3 == null)) {dopoll(settings.presSpeechDevice3)}
            if (!(settings?.lockSpeechDevice1 == null)) {dopoll(settings.lockSpeechDevice1)}
            if (!(settings?.lockSpeechDevice2 == null)) {dopoll(settings.lockSpeechDevice2)}
            if (!(settings?.lockSpeechDevice3 == null)) {dopoll(settings.lockSpeechDevice3)}
            if (!(settings?.contactSpeechDevice1 == null)) {dopoll(settings.contactSpeechDevice1)}
            if (!(settings?.contactSpeechDevice2 == null)) {dopoll(settings.contactSpeechDevice2)}
            if (!(settings?.contactSpeechDevice3 == null)) {dopoll(settings.contactSpeechDevice3)}
            if (!(settings?.modePhraseSpeechDevice1 == null)) {dopoll(settings.modePhraseSpeechDevice1)}
            if (!(settings?.thermostatSpeechDevice1 == null)) {dopoll(settings.thermostatSpeechDevice1)}
            if (!(settings?.accelerationSpeechDevice1 == null)) {dopoll(settings.accelerationSpeechDevice1)}
            if (!(settings?.accelerationSpeechDevice2 == null)) {dopoll(settings.accelerationSpeechDevice2)}
            if (!(settings?.accelerationSpeechDevice3 == null)) {dopoll(settings.accelerationSpeechDevice3)}
            if (!(settings?.waterSpeechDevice1 == null)) {dopoll(settings.waterSpeechDevice1)}
            if (!(settings?.waterSpeechDevice2 == null)) {dopoll(settings.waterSpeechDevice2)}
            if (!(settings?.waterSpeechDevice3 == null)) {dopoll(settings.waterSpeechDevice3)}
            if (!(settings?.smokeSpeechDevice1 == null)) {dopoll(settings.smokeSpeechDevice1)}
            if (!(settings?.smokeSpeechDevice2 == null)) {dopoll(settings.smokeSpeechDevice2)}
            if (!(settings?.smokeSpeechDevice3 == null)) {dopoll(settings.smokeSpeechDevice3)}
            if (!(settings?.buttonSpeechDevice1 == null)) {dopoll(settings.buttonSpeechDevice1)}
            if (!(settings?.buttonSpeechDevice2 == null)) {dopoll(settings.buttonSpeechDevice2)}
            if (!(settings?.buttonSpeechDevice3 == null)) {dopoll(settings.buttonSpeechDevice3)}
            if (!(settings?.timeslotSpeechDevice1 == null)) {dopoll(settings.timeslotSpeechDevice1)}
            if (!(settings?.timeslotSpeechDevice2 == null)) {dopoll(settings.timeslotSpeechDevice2)}
            if (!(settings?.timeslotSpeechDevice3 == null)) {dopoll(settings.timeslotSpeechDevice3)}
        } catch(e) {
            LOGERROR("One of your speech devices is not responding.  Poll failed.")
        }
        state.polledDevices = "!"
        state.lastPoll = getTimeFromCalendar(true,true)
        myRunIn(60, poll)
    }
}
def dopoll(pollSpeechDevice){
    pollSpeechDevice.each(){
        if (!(state.polledDevices.find(",${it.displayName}"))) {
            state.polledDevices = "${state.polledDevices},${it.displayName}"
            //LOGDEBUG("Polling: ${it.displayName}")
            state.refresh = false
            state.poll = false
            try {
                //LOGTRACE("refresh()")
                it.refresh()
                state.refresh = true
            }
            catch (ex) {
                //LOGDEBUG("ERROR(informational): it.refresh: ${ex}")
            }
            if (!state.refresh) {
                try {
                    //LOGTRACE("poll()")
                    it.poll()
                    state.poll = true
                }
                catch (ex) {
                    //LOGDEBUG ("ERROR(informational): it.poll: ${ex}")
                }
            }
        }
    }
}

def LOGDEBUG(txt){
    if (settings.debugmode) { log.debug("${app.label.replace(" ","").toUpperCase()}(${state.appversion}) || ${txt}") }
}
def LOGTRACE(txt){
    log.trace("${app.label.replace(" ","").toUpperCase()}(${state.appversion}) || ${txt}")
}
def LOGERROR(txt){
    log.error("${app.label.replace(" ","").toUpperCase()}(${state.appversion}) || ERROR: ${txt}")
}

def setAppVersion(){
    state.appversion = "1.1.5"
}