definition(
    name: "BigTalker2",
    namespace: "rayzurbock",
    author: "rayzur@rayzurbock.com",
    description: "Let's talk about mode changes, switches, motions, and so on.",
    category: "Fun & Social",
    singleInstance: true,
    iconUrl: "http://lowrance.cc/ST/icons/BigTalker-2.0.6.png",
    iconX2Url: "http://lowrance.cc/ST/icons/BigTalker@2x-2.0.6.png",
    iconX3Url: "http://lowrance.cc/ST/icons/BigTalker@2x-2.0.6.png")

preferences {
    page(name: "pageStart")
    page(name: "pageStatus")
    page(name: "pageTalkNow")
    page(name: "pageConfigureSpeechDeviceType")
    page(name: "pageConfigureDefaults")
    page(name: "pageHelpPhraseTokens")
//End preferences
}

def pageStart(){
    state.childAppName = "BigTalker2-Child"
    state.parentAppName = "BigTalker2"
    state.namespace = "rayzurbock"
	setAppVersion()
    state.supportedVoices = ["Ivy(en-us)","Joanna(en-us)","Joey(en-us)","Justin(en-us)","Kendra(en-us)","Kimberly(en-us)","Salli(en-us)","Amy(en-gb)","Brian(en-gb)","Emma(en-gb)","Miguel(es-us)","Penelope(es-us)"]
    if (checkConfig()) { 
        // Do nothing here, but run checkConfig() 
    } 
    dynamicPage(name: "pageStart", title: "Big Talker", install: false, uninstall: (app.getInstallationState() == "COMPLETE")){
        section(){
        	LOGDEBUG("install state=${app.getInstallationState()}.")
        	def mydebug_pollnow = ""
            if (!(state.configOK)) { 
                href "pageConfigureSpeechDeviceType", title:"Configure", description:"Tap to configure"
            } else {
                //V1Method href "pageConfigureEvents", title: "Configure Events", description: "Tap to configure events"
                href "pageStatus", title:"Status", description:"Tap to view status"
                href "pageConfigureDefaults", title: "Configure Defaults", description: "Tap to configure defaults"
				href "pageTalkNow", title:"Talk Now", description:"Tap to setup talk now" 
            }
        }
        section("Event Groups") {}
        section(){
        	def apps = getChildApps()?.sort{ it.label }
        	if (state.configOK) {
            	if (apps?.size() == 0) {
                	paragraph "You have not configured any event groups yet."
                    app(name: "BTEvt-", appName: state.childAppName, namespace: state.namespace, title: "Add Event Group", description: "Tap to configure event triggers", multiple: true, uninstall: false)
                } else {
            		app(name: "BTEvt-", appName: state.childAppName, namespace: state.namespace, title: "Add Event Group", description: "Tap to configure event triggers", multiple: true, uninstall: false)
                }
            }
        }
        section(){
        	if ((settings?.debugmode == true) && (state.speechDeviceType == "capability.musicPlayer") && (settings?.resumePlay == true)) {
            		input name: "debug_pollnow", type: "bool", title: "DEBUG: Poll Now (simply toggle)", multiple: false, required: false, submitOnChange: true, defaultValue: false
            		if (!(settings.debug_pollnow == mydebug_pollnow)) { poll() }
            }
        }
        section("About"){
            def AboutApp = ""
            AboutApp += 'Big Talker is a SmartApp that can make your house talk depending on various triggered events.\n\n'
            AboutApp += 'Pair with a SmartThings compatible audio device such as Sonos, Ubi, LANnouncer, VLC Thing (running on your computer or Raspberry Pi), a DLNA device using the "Generic MediaRenderer" SmartApp/Device and/or AskAlexa SmartApp\n\n'
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
        settings.speechDeviceDefault?.each(){
            enabledDevices += "${it.displayName},"
        }
        enabledDevices += "\n\n"
        if (settings.speechVolume && state.speechDeviceType == "capability.musicPlayer") {
            enabledDevices += "Adjust Volume To: ${settings.speechVolume}%"
            enabledDevices += "\n\n"
        }
        if (state.speechDeviceType == "capability.musicPlayer") {
        	enabledDevices += "Default Resume Audio: ${settings?.resumePlay}"
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
        	//NEEDS DEVELOPMENT
            paragraph enabledDevices
            //paragraph "Nothing else is viewable at this time. I'm working on the best method to expose this data from the child apps in one location, if possible."
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
            if (state.speechDeviceType == "capability.musicPlayer") {
        		enabledDevices += "Resume Audio: ${(!(settings.timeSlotResumePlay1 == null)) ? settings.timeSlotResumePlay1 : settings.resumePlay}"
            	enabledDevices += "\n\n"
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
            if (state.speechDeviceType == "capability.musicPlayer") {
        		enabledDevices += "Resume Audio: ${(!(settings.timeSlotResumePlay2 == null)) ? settings.timeSlotResumePlay2 : settings.resumePlay}"
            	enabledDevices += "\n\n"
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
            if (state.speechDeviceType == "capability.musicPlayer") {
        		enabledDevices += "Resume Audio: ${(!(settings.timeSlotResumePlay3 == null)) ? settings.timeSlotResumePlay3 : settings.resumePlay}"
            	enabledDevices += "\n\n"
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
            if (state.speechDeviceType == "capability.musicPlayer") {
        		enabledDevices += "Resume Audio: ${(!(settings.motionResumePlay1 == null)) ? settings.motionResumePlay1 : settings.resumePlay}"
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
            if (state.speechDeviceType == "capability.musicPlayer") {
        		enabledDevices += "Resume Audio: ${(!(settings.motionResumePlay2 == null)) ? settings.motionResumePlay2 : settings.resumePlay}"
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
            if (state.speechDeviceType == "capability.musicPlayer") {
        		enabledDevices += "Resume Audio: ${(!(settings.motionResumePlay3 == null)) ? settings.motionResumePlay3 : settings.resumePlay}"
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
            if (state.speechDeviceType == "capability.musicPlayer") {
        		enabledDevices += "Resume Audio: ${(!(settings.switchResumePlay1 == null)) ? settings.switchResumePlay1 : settings.resumePlay}"
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
            if (state.speechDeviceType == "capability.musicPlayer") {
        		enabledDevices += "Resume Audio: ${(!(settings.switchResumePlay2 == null)) ? settings.switchResumePlay2 : settings.resumePlay}"
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
            if (state.speechDeviceType == "capability.musicPlayer") {
        		enabledDevices += "Resume Audio: ${(!(settings.switchResumePlay3 == null)) ? settings.switchResumePlay3 : settings.resumePlay}"
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
            if (state.speechDeviceType == "capability.musicPlayer") {
        		enabledDevices += "Resume Audio: ${(!(settings.presResumePlay1 == null)) ? settings.presResumePlay1 : settings.resumePlay}"
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
            if (state.speechDeviceType == "capability.musicPlayer") {
        		enabledDevices += "Resume Audio: ${(!(settings.presResumePlay2 == null)) ? settings.presResumePlay2 : settings.resumePlay}"
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
            if (state.speechDeviceType == "capability.musicPlayer") {
        		enabledDevices += "Resume Audio: ${(!(settings.presResumePlay3 == null)) ? settings.presResumePlay3 : settings.resumePlay}"
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
            if (state.speechDeviceType == "capability.musicPlayer") {
        		enabledDevices += "Resume Audio: ${(!(settings.lockResumePlay1 == null)) ? settings.lockResumePlay1 : settings.resumePlay}"
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
            if (state.speechDeviceType == "capability.musicPlayer") {
        		enabledDevices += "Resume Audio: ${(!(settings.lockResumePlay2 == null)) ? settings.lockResumePlay2 : settings.resumePlay}"
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
            if (state.speechDeviceType == "capability.musicPlayer") {
        		enabledDevices += "Resume Audio: ${(!(settings.lockResumePlay3 == null)) ? settings.lockResumePlay3 : settings.resumePlay}"
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
            if (state.speechDeviceType == "capability.musicPlayer") {
        		enabledDevices += "Resume Audio: ${(!(settings.contactResumePlay1 == null)) ? settings.contactResumePlay1 : settings.resumePlay}"
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
            if (state.speechDeviceType == "capability.musicPlayer") {
        		enabledDevices += "Resume Audio: ${(!(settings.contactResumePlay2 == null)) ? settings.contactResumePlay2 : settings.resumePlay}"
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
            if (state.speechDeviceType == "capability.musicPlayer") {
        		enabledDevices += "Resume Audio: ${(!(settings.contactResumePlay3 == null)) ? settings.contactResumePlay3 : settings.resumePlay}"
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
            if (state.speechDeviceType == "capability.musicPlayer") {
        		enabledDevices += "Resume Audio: ${(!(settings.modePhraseResumePlay1 == null)) ? settings.modePhraseResumePlay1 : settings.resumePlay}"
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
            if (state.speechDeviceType == "capability.musicPlayer") {
        		enabledDevices += "Resume Audio: ${(!(settings.thermostatResumePlay1 == null)) ? settings.thermostatResumePlay1 : settings.resumePlay}"
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
            if (state.speechDeviceType == "capability.musicPlayer") {
        		enabledDevices += "Resume Audio: ${(!(settings.accelerationResumePlay1 == null)) ? settings.accelerationResumePlay1 : settings.resumePlay}"
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
            if (state.speechDeviceType == "capability.musicPlayer") {
        		enabledDevices += "Resume Audio: ${(!(settings.accelerationResumePlay2 == null)) ? settings.accelerationResumePlay2 : settings.resumePlay}"
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
            if (state.speechDeviceType == "capability.musicPlayer") {
        		enabledDevices += "Resume Audio: ${(!(settings.accelerationResumePlay3 == null)) ? settings.accelerationResumePlay3 : settings.resumePlay}"
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
            if (state.speechDeviceType == "capability.musicPlayer") {
        		enabledDevices += "Resume Audio: ${(!(settings.waterResumePlay1 == null)) ? settings.waterResumePlay1 : settings.resumePlay}"
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
            if (state.speechDeviceType == "capability.musicPlayer") {
        		enabledDevices += "Resume Audio: ${(!(settings.waterResumePlay2 == null)) ? settings.waterResumePlay2 : settings.resumePlay}"
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
            if (state.speechDeviceType == "capability.musicPlayer") {
        		enabledDevices += "Resume Audio: ${(!(settings.waterResumePlay3 == null)) ? settings.waterResumePlay3 : settings.resumePlay}"
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
            if (state.speechDeviceType == "capability.musicPlayer") {
        		enabledDevices += "Resume Audio: ${(!(settings.smokeResumePlay1 == null)) ? settings.smokeResumePlay1 : settings.resumePlay}"
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
            if (state.speechDeviceType == "capability.musicPlayer") {
        		enabledDevices += "Resume Audio: ${(!(settings.smokeResumePlay2 == null)) ? settings.smokeResumePlay2 : settings.resumePlay}"
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
            if (state.speechDeviceType == "capability.musicPlayer") {
        		enabledDevices += "Resume Audio: ${(!(settings.smokeResumePlay3 == null)) ? settings.smokeResumePlay3 : settings.resumePlay}"
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
            if (state.speechDeviceType == "capability.musicPlayer") {
        		enabledDevices += "Resume Audio: ${(!(settings.buttonResumePlay1 == null)) ? settings.buttonResumePlay1 : settings.resumePlay}"
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
            if (state.speechDeviceType == "capability.musicPlayer") {
        		enabledDevices += "Resume Audio: ${(!(settings.buttonResumePlay2 == null)) ? settings.buttonResumePlay2 : settings.resumePlay}"
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
            if (state.speechDeviceType == "capability.musicPlayer") {
        		enabledDevices += "Resume Audio: ${(!(settings.buttonResumePlay3 == null)) ? settings.buttonResumePlay3 : settings.resumePlay}"
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
        //BEGIN STATUS CONFIG SMART HOME MONITOR
        if (settings.SHMDeviceGroup1) {
            enabledDevices += "Smart Home Monitor Status Change:  "
            enabledDevices += "\n\n"
            if (settings.SHMTalkOnAway) {
                enabledDevices += "Say this when armed in Away mode:\n ${settings.SHMTalkOnAway}\n\n"
            }
			if (settings.SHMSpeechDeviceAway) {
                enabledDevices += "Custom Speech Device(s):\n\n"
                enabledDevices += "   "
                settings.SHAMSpeechDeviceAway.each() {
                    enabledDevices += "${it.displayName},"
                }
                enabledDevices += "\n\n"
            }
            if (state.speechDeviceType == "capability.musicPlayer") {
        		enabledDevices += "Resume Audio: ${(!(settings.SHMResumePlayAway == null)) ? settings.SHMResumePlayAway : settings.resumePlay}"
            	enabledDevices += "\n\n"
        	}
            if (settings.SHMModesAway) {
                enabledDevices += "Custom mode(s):\n"
                enabledDevices += "   "
                settings.SHMModesAway.each() {
                    enabledDevices += "${it},"
                }
                enabledDevices += "\n\n"
            }
            if (settings.SHMStartTimeAway) {
                def customStartTime = getTimeFromDateString(settings.SHMStartTimeAway, true)
                def customEndTime = getTimeFromDateString(settings.SHMEndTimeAway, true)
                enabledDevices += "Custom Allowed Talk Time:\n ${customStartTime} - ${customEndTime}"
                customStartTime = ""
                customEndTime = ""
            }
            if (!(enabledDevices == "")) {
                section ("Armed - Away:"){
                    paragraph enabledDevices
                }
            }
            enabledDevices = ""
            if (settings.SHMTalkOnHome) {
                enabledDevices += "Say this when armed in Home mode:\n ${settings.SHMTalkOnHome}\n\n"
            }
			if (settings.SHMSpeechDeviceHome) {
                enabledDevices += "Custom Speech Device(s):\n\n"
                enabledDevices += "   "
                settings.SHAMSpeechDeviceHome.each() {
                    enabledDevices += "${it.displayName},"
                }
                enabledDevices += "\n\n"
            }
            if (state.speechDeviceType == "capability.musicPlayer") {
        		enabledDevices += "Resume Audio: ${(!(settings.SHMResumePlayHome == null)) ? settings.SHMResumePlayHome : settings.resumePlay}"
            	enabledDevices += "\n\n"
        	}
            if (settings.SHMModesHome) {
                enabledDevices += "Custom mode(s):\n"
                enabledDevices += "   "
                settings.SHMModesHome.each() {
                    enabledDevices += "${it},"
                }
                enabledDevices += "\n\n"
            }
            if (settings.SHMStartTimeHome) {
                def customStartTime = getTimeFromDateString(settings.SHMStartTimeHome, true)
                def customEndTime = getTimeFromDateString(settings.SHMEndTimeHome, true)
                enabledDevices += "Custom Allowed Talk Time:\n ${customStartTime} - ${customEndTime}"
                customStartTime = ""
                customEndTime = ""
            }
            if (!(enabledDevices == "")) {
                section ("Armed - Home:"){
                    paragraph enabledDevices
                }
            }
            enabledDevices = ""
            if (settings.SHMTalkOnDisarm) {
                enabledDevices += "Say this when disarmed:\n ${settings.SHMTalkOnDisarm}\n\n"
            }
			if (settings.SHMSpeechDeviceDisarm) {
                enabledDevices += "Custom Speech Device(s):\n\n"
                enabledDevices += "   "
                settings.SHMSpeechDeviceDisarm.each() {
                    enabledDevices += "${it.displayName},"
                }
                enabledDevices += "\n\n"
            }
            if (state.speechDeviceType == "capability.musicPlayer") {
        		enabledDevices += "Resume Audio: ${(!(settings.SHMResumePlayDisarm == null)) ? settings.SHMResumePlayDisarm : settings.resumePlay}"
            	enabledDevices += "\n\n"
        	}
            if (settings.SHMModesDisarm) {
                enabledDevices += "Custom mode(s):\n"
                enabledDevices += "   "
                settings.SHMModesDisarm.each() {
                    enabledDevices += "${it},"
                }
                enabledDevices += "\n\n"
            }
            if (settings.SHMStartTimeDisarm) {
                def customStartTime = getTimeFromDateString(settings.SHMStartTimeDisarm, true)
                def customEndTime = getTimeFromDateString(settings.SHMEndTimeDisarm, true)
                enabledDevices += "Custom Allowed Talk Time:\n ${customStartTime} - ${customEndTime}"
                customStartTime = ""
                customEndTime = ""
            }
            if (!(enabledDevices == "")) {
                section ("Disarmed:"){
                    paragraph enabledDevices
                }
            }
            enabledDevices = ""
        }
        //END STATUS CONFIG SMART HOME MONITOR
    }
}

def pageTalkNow(){
    dynamicPage(name: "pageTalkNow", title: "Talk Now", install: false, uninstall: false){
        section(""){
        	def myTalkNowResume = false
            paragraph ("Speak the following phrase:\nNote: must differ from the last spoken phrase\n")
            if (state.speechDeviceType == "capability.musicPlayer") {
            	input name: "talkNowVolume", type: "number", title: "Set volume to (overrides default):", required: false, submitOnChange: true
            	input name: "talkNowResume", type: "bool", title: "Enable audio resume", multiple: true, required: false, submitOnChange: true, defaultValue: (settings?.resumePlay == false) ? false : true
                input name: "talkNowVoice", type: "enum", title: "Select custom voice:", options: state.supportedVoices, required: false, submitOnChange: true
                myTalkNowResume = settings.talkNowResume
            }
            input name: "speechTalkNow", type: text, title: "Speak phrase", required: false, submitOnChange: true
            input name: "talkNowSpeechDevice", type: state.speechDeviceType, title: "Talk with these text-to-speech devices", multiple: true, required: false, submitOnChange: true
            //LOGDEBUG("previoustext=${state.lastTalkNow} New=${settings.speechTalkNow}")
            if (((!(state.lastTalkNow == settings.speechTalkNow)) && (settings.talkNowSpeechDevice)) || (settings.speechTalkNow?.contains("%askalexa%"))){
                //Say stuff!
                if (state.speechDeviceType == "capability.musicPlayer") {
                	myTalkNowResume = (myTalkNowResume == "") ? settings.resumeAudio : true //use global setting if TalkNow is not set
                	if (settings?.talkNowResume == null) {mytalkNowResume = true}  //default to true if not set.
                }
                def customevent = [displayName: 'BigTalker:TalkNow', name: 'TalkNow', value: 'TalkNow', descriptionText: "Talk Now"]
                def myVolume = getDesiredVolume(settings?.talkNowVolume)
                def myVoice = getMyVoice(settings.talkNowVoice)
                //def myVoice = (!(talkNowVoice == null || talkNowVoice == "")) ? talkNowVoice : (settings?.speechVoice ? settings.speechVoice : "Sallie(en-us)")
                def personality = false
                LOGDEBUG ("TalkNow Voice=${myVoice}")
                Talk("Talk Now", settings.speechTalkNow, settings.talkNowSpeechDevice, myVolume, myTalkNowResume, personality, myVoice, customevent)
                state.lastTalkNow = settings.speechTalkNow
            }
        }
        section("Help"){
            href "pageHelpPhraseTokens", title:"Phrase Tokens", description:"Tap for a list of phrase tokens"
        }
    }
}

def getMyVoice(deviceVoice){
    def myVoice = "Not Used"
    if (state?.speechDeviceType == "capability.musicPlayer") {
    	log.debug "getMyVoice[parent]: deviceVoice=${deviceVoice ? deviceVoice : "Not selected"}"
        log.debug "getMyVoice[parent]: settings.speechVoice=${settings?.speechVoice}"
		myVoice = (!(deviceVoice == null || deviceVoice == "")) ? deviceVoice : (settings?.speechVoice ? settings?.speechVoice : "Salli(en-us)")
    }
    return myVoice
}

def pageHelpPhraseTokens(){
	//KEEP IN SYNC WITH CHILD!
    dynamicPage(name: "pageHelpPhraseTokens", title: "Available Phrase Tokens", install: false, uninstall:false){
       section("The following tokens can be used in your event phrases and will be replaced as listed:"){
       	   def AvailTokens = ""
           AvailTokens += "%askalexa% = Send phrase to AskAlexa SmartApp's message queue\n\n"
           AvailTokens += "%groupname% = Name that you gave for the event group\n\n"
           AvailTokens += "%date% = Current date; January 01\n\n"
           AvailTokens += "%day% = Current day; Monday\n\n"
           AvailTokens += "%devicename% = Triggering devices display name\n\n"
           AvailTokens += "%devicetype% = Triggering device type; motion, switch, etc\n\n"
           AvailTokens += "%devicechange% = State change that occurred; on/off, active/inactive, etc...\n\n"
           AvailTokens += "%description% = The description of the event that is to be displayed to the user in the mobile application. \n\n"
           AvailTokens += "%locationname% = Hub location name; home, work, etc\n\n"
           AvailTokens += "%lastmode% = Last hub mode; home, away, etc\n\n"
           AvailTokens += "%mode% = Current hub mode; home, away, etc\n\n"
           AvailTokens += "%mp3(url)% = Play hosted MP3 file; URL should be http://www.domain.com/path/file.mp3 \n"
           AvailTokens += "No other tokens or phrases can be used with %mp3(url)%\n\n"
           AvailTokens += "%time% = Current hub time; HH:mm am/pm\n\n"
           AvailTokens += "%shmstatus% = SmartHome Monitor Status (Disarmed, Armed Home, Armed Away)\n\n"
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
            paragraph "'Music Player' typically supports devices such as Sonos, VLCThing, Generic Media Renderer.\n\n'Speech Synthesis' typically supports devices such as Ubi and LANnouncer.\n\nIf only using with AskAlexa this setting can be ignored.\n\nThis setting cannot be changed without reinstalling ${app.label}."
            input "speechDeviceType", "bool", title: "ON=Music Player\nOFF=Speech Synthesis", required: true, defaultValue: true, submitOnChange: true
            paragraph "Click Next (top right) to continue configuration...\n"
            if (speechDeviceType == true) {state.speechDeviceType = "capability.musicPlayer"}
            if (speechDeviceType == false) {state.speechDeviceType = "capability.speechSynthesis"}
        }
    }
//End pageConfigureSpeechDeviceType()
}

def pageConfigureDefaults(){
    if (state?.installed == true) { 
       state.dynPageProperties = [
            name:      "pageConfigureDefaults",
            title:     "Configure Defaults",
            install:   false,
            uninstall: false,
            //nextPage:  "pageConfigureEvents"
        ]
    } else {
       state.dynPageProperties = [
            name:      "pageConfigureDefaults",
            title:     "Configure Defaults",
            install:   true,
            uninstall: false
        ]
    }
    return dynamicPage(state.dynPageProperties) {
    //dynamicPage(name: "pageConfigureDefaults", title: "Configure Defaults", nextPage: "${myNextPage}", install: false, uninstall: false) {
        section("Talk with:"){
           if (state.speechDeviceType == null || state.speechDeviceType == "") { state.speechDeviceType = "capability.musicPlayer" }
           input "speechDeviceDefault", state.speechDeviceType, title: "Talk with these text-to-speech devices (default)", multiple: true, required: false, submitOnChange: false
        }
        if (state.speechDeviceType == "capability.musicPlayer") {
            section ("Adjust volume during announcement (optional; Supports: Sonos, VLC-Thing):"){
            	input "speechMinimumVolume", "number", title: "Minimum volume for announcement (0-100%, Default: 50%):", required: false
                input "speechVolume", "number", title: "Set volume during announcement (0-100%):", required: false
                input "speechVoice", "enum", title: "Select voice:", options: state.supportedVoices, required: true, defaultValue: "Salli(en-us)"
            }
            section ("Attempt to resume playing audio (optional; Supports: Sonos, VLC-Thing):"){
            	input "resumePlay", "bool", title: "Resume Play:", required: true, defaultValue: true
                input "allowScheduledPoll", "bool", title: "Enable polling device status (recommended)", required: true, defaultValue: true
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
            input "personalityMode", "bool", title: "Allow Personality?", required: true, defaultValue: false
            input "debugmode", "bool", title: "Enable debug logging", required: true, defaultValue: false
        }
    }
}

def installed() {
	state.installed = true
    //LOGTRACE("Installed with settings: ${settings}")
    LOGTRACE("Installed (Parent Version: ${state.appVersion})")
	initialize()
    if (((settings?.allowScheduledPoll == true || state?.allowScheduledPoll == true)) || ((settings?.allowScheduledPoll == null) || (state?.allowScheduledPoll == null))){ 
    	myRunIn(60, poll) 
    }
//End installed()
}

def updated() {
    unschedule()
    state.installed = true
	//LOGTRACE("Updated with settings: ${settings}")
    LOGTRACE("Updated settings (Parent Version: ${state.appVersion})")
    unsubscribe()
    initialize()
    if (((settings?.allowScheduledPoll == true || state?.allowScheduledPoll == true)) || ((settings?.allowScheduledPoll == null) || (state?.allowScheduledPoll == null))){ 
    	myRunIn(60, poll) 
    }
//End updated()
}

def checkConfig() {
    def configErrorList = ""
    if (!(state.speechDeviceType)){
       state.speechDeviceType = "capability.musicPlayer" //Set a default if the app was update and didn't contain settings.speechDeviceType
    }
    if ((settings?.allowScheduledPoll == true) && (settings?.resumePlay == true)) { state.allowScheduledPoll = true }
    if ((settings?.allowScheduledPoll == null) || (settings?.resumePlay == null)) { state.allowScheduledPoll = true }
	if ((settings?.allowScheduledPoll == false) || (settings?.resumePlay == false)) { state.allowScheduledPoll = false}
//    if (!(settings.speechDeviceDefault)){
//        configErrorList += "  ** Default speech device(s) not selected,"
//    }
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
    if (!(checkConfig())) { 
        def msg = ""
        msg = "ERROR: App not properly configured!  Can't start.\n"
        msg += "ERRORs:\n${state.configErrorList}"
        LOGTRACE(msg)
        sendNotificationEvent(msg)
        state.polledDevices = ""
        return //App not properly configured, exit, don't subscribe
    }
    LOGTRACE("Initialized (Parent Version: ${state.appVersion})")
    sendNotificationEvent("${app.label.replace(" ","").toUpperCase()}: Settings activated")
    state.lastMode = location.mode
    state.lastTalkNow = settings.speechTalkNow
//End initialize()
}

def processPhraseVariables(appname, phrase, evt){
    try {
    	def zipCode = location.zipCode
    	def mp3Url = ""
    	if (phrase.toLowerCase().contains("%mp3(")) { 
    		if (phrase.toLowerCase().contains(".mp3)%")) {
            	def phraseMP3Start = (phrase.toLowerCase().indexOf("%mp3(") + 5)
            	def phraseMP3End = (phrase.toLowerCase().indexOf(".mp3)%"))
            	mp3Url = phrase.substring(phraseMP3Start, phraseMP3End)
            	LOGDEBUG("MP3 URL: ${mp3Url}")
            	phrase = phrase.replace("%mp3(","")
            	phrase = phrase.replace(".mp3)%", ".mp3")
            	phrase = phrase.replace (" ", "%20")
            	phrase = phrase.replace ("+", "%2B")
            	phrase = phrase.replace ("-", "%2D")
        	} else {
	            phrase = "Invalid M P 3 URL found in M P 3 token"
    	    }
	        return phrase
    	}
    	if (phrase.toLowerCase().contains(" percent ")) { phrase = phrase.replace(" percent ","%") }
    	if (phrase.toLowerCase().contains("%groupname%")) {
    		phrase = phrase.toLowerCase().replace('%groupname%', appname)
    	}
    	if (phrase.toLowerCase().contains("%devicename%")) {
	    	try {
    	    	phrase = phrase.toLowerCase().replace('%devicename%', evt.displayName)  //User given name of the device triggering the event
        	}
        	catch (ex) { 
        		LOGDEBUG("evt.displayName failed; trying evt.device.displayName")
        		try {
                	phrase = phrase.toLowerCase().replace('%devicename%', evt.device.displayName) //User given name of the device triggering the event
            	}
            	catch (ex2) {
	            	LOGDEBUG("evt.device.displayName filed; trying evt.device.name")
    	            try {
        	        	phrase = phrase.toLowerCase().replace('%devicename%', evt.device.name) //SmartThings name for the device triggering the event
            	    }
                	catch (ex3) {
                		LOGDEBUG("evt.device.name filed; Giving up")
                    	phrase = phrase.toLowerCase().replace('%devicename%', "Device Name Unknown")
                	}
            	}
       		}
    	}
    	if (phrase.toLowerCase().contains("%devicetype%")) {phrase = phrase.toLowerCase().replace('%devicetype%', evt.name)}  //Device type: motion, switch, etc...
    	if (phrase.toLowerCase().contains("%devicechange%")) {phrase = phrase.toLowerCase().replace('%devicechange%', evt.value)}  //State change that occurred: on/off, active/inactive, etc...
    	if (phrase.toLowerCase().contains("%description%")) {phrase = phrase.toLowerCase().replace('%description%', evt.descriptionText)}  //Description of the event which occurred via device-specific text`
    	if (phrase.toLowerCase().contains("%locationname%")) {phrase = phrase.toLowerCase().replace('%locationname%', location.name)}
    	if (phrase.toLowerCase().contains("%lastmode%")) {phrase = phrase.toLowerCase().replace('%lastmode%', state.lastMode)}
    	if (phrase.toLowerCase().contains("%mode%")) {phrase = phrase.toLowerCase().replace('%mode%', location.mode)}
    	if (phrase.toLowerCase().contains("%time%")) {
    		phrase = phrase.toLowerCase().replace('%time%', getTimeFromCalendar(false,true))
        	if ((phrase.toLowerCase().contains("00:")) && (phrase.toLowerCase().contains("am"))) {phrase = phrase.toLowerCase().replace('00:', "12:")}
        	if ((phrase.toLowerCase().contains("24:")) && (phrase.toLowerCase().contains("am"))) {phrase = phrase.toLowerCase().replace('24:', "12:")}
        	if ((phrase.toLowerCase().contains("0:")) && (!phrase.toLowerCase().contains("10:")) && (phrase.toLowerCase().contains("am"))) {phrase = phrase.toLowerCase().replace('0:', "12:")}
    	}
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
	            phrase = phrase.toLowerCase().replace("%weathercurrent(${zipCode.toLowerCase()})%", getWeather("current", zipCode.toLowerCase()))
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
	            phrase = phrase.toLowerCase().replace("%weathertoday(${zipCode.toLowerCase()})%", getWeather("today", zipCode.toLowerCase()))
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
	            phrase = phrase.toLowerCase().replace("%weathertonight(${zipCode.toLowerCase()})%", getWeather("tonight", zipCode.toLowerCase()))
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
	            phrase = phrase.toLowerCase().replace("%weathertomorrow(${zipCode.toLowerCase()})%", getWeather("tomorrow", zipCode.toLowerCase()))
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
	    if (phrase.toLowerCase().contains("%shmstatus%")) {
	    	def shmstatus = location.currentState("alarmSystemStatus")?.value
	        LOGDEBUG("SHMSTATUS=${shmstatus}")
			def shmmessage = [off : "Disarmed", away: "Armed, away", home: "Armed, home"][shmstatus] ?: shmstatus
	        LOGDEBUG("SHMMESSAGE=${shmmessage}")
	        phrase = phrase.replace("%shmstatus%", shmmessage)
	    }
	    if (phrase.contains('"')) { phrase = phrase.replace('"',"") }
	    if (phrase.contains("'")) { phrase = phrase.replace("'","") }
	    if (phrase.toLowerCase().contains("10s")) { phrase = phrase.toLowerCase().replace("10s","tens") }
	    if (phrase.toLowerCase().contains("20s")) { phrase = phrase.toLowerCase().replace("20s","twenties") }
	    if (phrase.toLowerCase().contains("30s")) { phrase = phrase.toLowerCase().replace("30s","thirties") }
	    if (phrase.toLowerCase().contains("40s")) { phrase = phrase.toLowerCase().replace("40s","fourties") }
	    if (phrase.toLowerCase().contains("50s")) { phrase = phrase.toLowerCase().replace("50s","fifties") }
	    if (phrase.toLowerCase().contains("60s")) { phrase = phrase.toLowerCase().replace("60s","sixties") }
	    if (phrase.toLowerCase().contains("70s")) { phrase = phrase.toLowerCase().replace("70s","seventies") }
	    if (phrase.toLowerCase().contains("80s")) { phrase = phrase.toLowerCase().replace("80s","eighties") }
	    if (phrase.toLowerCase().contains("90s")) { phrase = phrase.toLowerCase().replace("90s","nineties") }
	    if (phrase.toLowerCase().contains("100s")) { phrase = phrase.toLowerCase().replace("100s","one hundreds") }
	    if (phrase.toLowerCase().contains("%askalexa%")) {
	    	phrase=phrase.toLowerCase().replace("%askalexa%","")
	        if (!(phrase == "") && (!(phrase == null))){
	    		LOGTRACE("Sending to AskAlexa: ${phrase}.")
		        sendLocationEvent(name: "AskAlexaMsgQueue", value: "BigTalker", isStateChange: true, descriptionText: phrase)
	        }else{
	        	LOGERROR("Phrase only contained %askalexa%. Nothing to say/send.")
	        }
	    }
	    if (phrase.toLowerCase().contains("%date%")) {
	    	phrase=phrase.toLowerCase().replace("%date%",(new Date().format( 'MMMM dd' )))
	    }
	    if (phrase.toLowerCase().contains("%day%")) {
	    	phrase=phrase.toLowerCase().replace("%day%",(new Date().format('EEEE',location.timeZone)))
	    }
	    if (phrase.contains("%")) { phrase = phrase.replace("%"," percent ") }
	    return phrase
	} catch(ex) { 
		LOGTRACE("There was a problem processing your desired phrase: ${phrase}. ${ex}")
    	phrase = "Sorry, there was a problem processing your desired BigTalker phrase token."
    	return phrase
	}
}

def addPersonalityToPhrase(phrase, evt){
	LOGDEBUG("addPersonalityToPhrase(${phrase},${evt})")
    def response = new String[20]
    response[0] = ""
    def options = 0
    def genericresponse = new String[20]
    genericresponse[0] = ""
    def genericoptions = 0
    def myRandom = 0
    //SWITCHES BEGIN
    if (evt.value == "on") {
    	if (phrase.contains("light")){
        	options = 12
  			response[1] = "{POST}please don't forget to turn the light off"
           	response[2] = "{POST}night vision goggles would do the same but I guess they are more expensive."
            response[3] = "{POST}Thanks Thomas Edison!"
            response[4] = "{POST}Wow, this is bright!"
            response[5] = "{POST}Where are my sunglasses."
            response[6] = "{POST}there goes the electricity bill!"
            response[7] = "{POST}the same old thing everyday."
            response[8] = "{POST}It is about time it was awfully dark!"
            response[9] = "{POST}Glad you are here, I was lonely"
            response[10] = "{POST}It it time for us to play?"
            response[11] = "{PRE}Oh, Hi"
            response[12] = "{PRE}Oh, Hi there"
        } else {
        	//Something turned on, but it wasn't a light
        	options = 4
            response[1] = "{POST}there goes the electricity bill!"
            response[2] = "{POST}the same old thing everyday."
            response[3] = "{PRE}Oh, Hi"
            response[4] = "{PRE}Oh, Hi there"
        }
    }
    if (evt.value == "off") {
    	if (phrase.contains("light")){
        	options = 12
           	response[1] = "{POST}It's about time!"
            response[2] = "{POST}time to save some money!"
            response[3] = "{POST}wow, it's dark"
            response[4] = "{POST}going green are we?"
            response[5] = "{POST}I'll still be here, in the dark."
            response[6] = "{POST}Hey! You know I am afraid of the dark."
            response[7] = "{POST}Please don't leave me alone in the dark."
            response[8] = "{POST}Good thing you turned that off it was hurting my eyes!"
            response[8] = "{POST}You really like saving money!"
            response[10] = "{POST}Is it time to go to sleep?"
            response[11] = "{PRE}Oh, Hi"
            response[12] = "{PRE}Oh, Hi there"
        } else {
        	//Something turned off, but it wasn't a light
        	options = 5
        	response[1] = "{POST}It's about time!"
            response[2] = "{POST}time to save some money!"
            response[3] = "{POST}going green are we?"
            response[4] = "{PRE}Oh, Hi"
            response[5] = "{PRE}Oh, Hi there"
        }
    }
    //SWITCHES END
    def UseGenericRandom = 0
    myRandom = (new Random().nextInt(10))
    if (myRandom == 1 || myRandom == 4 || myRandom == 7) {
    	//GENERIC RESPONSES BEGIN
    	genericoptions = 4
    	genericresponse[1] = "{PRE}Hey there"
    	genericresponse[2] = "{PRE}Don't mean to bother but"
        genericresponse[3] = "{PRE}All I know is"
        genericresponse[4] = "{POST}that is all I know."
    	//GENERIC RESPONSES END
    	myRandom = (new Random().nextInt(genericoptions))
        LOGDEBUG("genericoptions=${genericoptions};myRandom=${myRandom};phrase=${genericresponse[myRandom]}")
    	if (genericresponse[myRandom].contains("{PRE}")) {
    		genericresponse[myRandom] = genericresponse[myRandom].replace("{PRE}", "")
        	phrase = genericresponse[myRandom] + ", " + phrase
    	}
    	if (genericresponse[myRandom].contains("{POST}")) {
    		genericresponse[myRandom] = genericresponse[myRandom].replace("{POST}", "")
        	phrase = phrase + ", " + genericresponse[myRandom]
    	}
        return phrase
    }
    if (options == 0) { return phrase }
    myRandom = (new Random().nextInt(options))
    LOGDEBUG("options=${options};myRandom=${myRandom};phrase=${response[myRandom]}")
    if (response[myRandom].contains("{PRE}")) {
    	response[myRandom] = response[myRandom].replace("{PRE}", "")
        phrase = response[myRandom] + ", " + phrase
    }
    if (response[myRandom].contains("{POST}")) {
    	response[myRandom] = response[myRandom].replace("{POST}", "")
        phrase = phrase + ", " + response[myRandom]
    }
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
    LOGDEBUG ("Adjust Weather: In=${phraseIn} Out=${phraseOut}")
    return phraseOut
}

def Talk(appname, phrase, customSpeechDevice, volume, resume, personality, voice, evt){
	def myDelay = 100
    def myVoice = settings?.speechVoice
    if (myVoice == "" || myVoice == null) { myVoice = "Salli(en-us)" } 
    if (!(voice == "" || voice == null)) { 
        myVoice = voice
    }
   	myVoice = myVoice.replace("(en-us)","")
   	myVoice = myVoice.replace("(en-gb)","")
   	myVoice = myVoice.replace("(es-us)","")
    if (state.speechDeviceType == "capability.musicPlayer") { 
    	myDelay = TalkQueue(appname, phrase, customSpeechDevice, volume, resume, personality, voice, evt) 
        state.lastTalkTime = now()
    }
	def currentSpeechDevices = []
   	def smartAppSpeechDevice = false
    def playAudioFile = false
   	def spoke = false
    LOGDEBUG ("TALK(app=${appname},customdevice=${customSpeechDevice},volume=${volume},resume=${resume},personality=${personality},myDelay=${myDelay},voice=${myVoice},evt=${evt},phrase=${phrase})")
   	if ((phrase?.toLowerCase())?.contains("%askalexa%")) {smartAppSpeechDevice = true}
   	if (!(phrase == null) && !(phrase == "")) {
		phrase = processPhraseVariables(appname, phrase, evt)
	    if (personality && !(phrase.toLowerCase().contains(".mp3"))) { phrase = addPersonalityToPhrase(phrase, evt) }
	}
	if (phrase == null || phrase == "") {
   		LOGERROR(processPhraseVariables(appname, "BigTalker - Check configuration. Phrase is empty for %devicename%", evt))
    	sendNotification(processPhraseVariables(appname, "BigTalker - Check configuration. Phrase is empty for %devicename%", evt))
	}
	if (resume == null) { resume = true }
	if ((state.speechDeviceType == "capability.musicPlayer") && (!( phrase==null ) && !(phrase==""))){
		state.sound = ""
		state.ableToTalk = false
		if (!(settings.speechDeviceDefault == null) || !(customSpeechDevice == null)) {
			LOGTRACE("TALK(${appname}.${evt.name})|mP@|${volume} >> ${phrase}")
            if (resume) { LOGTRACE("TALK(${appname}.${evt.name})|mP| Resume is desired") } else { LOGTRACE("TALK(${appname}.${evt.name})|mP| Resume is not desired") }
			if (!(phrase.toLowerCase().contains(".mp3"))){
            	try {
					state.sound = textToSpeech(phrase instanceof List ? phrase[0] : phrase, myVoice) 
					state.ableToTalk = true
				} catch(e) {
					LOGERROR("TALK(${appname}.${evt.name})|mP| ST Platform issue (textToSpeech)? ${e}")
					//Try Again
					try {
						LOGTRACE("TALK(${appname}.${evt.name})|mP| Trying textToSpeech function again...")
					state.sound = textToSpeech(phrase instanceof List ? phrase[0] : phrase, myVoice)
					state.ableToTalk = true
					} catch(ex) {
						LOGERROR("TALK(${appname}.${evt.name})|mP| ST Platform issue (textToSpeech)? I tried textToSpeech() twice, SmartThings wouldn't convert/process.  I give up, Sorry..")
						sendNotificationEvent("ST Platform issue? textToSpeech() failed.")
						sendNotification("BigTalker couldn't announce: ${phrase}")
					} //try again before final error(ableToTalk)
				} //try (ableToTalk)
            } else {
            	LOGTRACE("TALK(${appname}.${evt.name})|mP| MP3=${phrase}")
            	def sound = [uri:phrase, duration:10]
                state.sound = sound
                playAudioFile = true
                state.ableToTalk = true
                LOGTRACE("Sound=${state.sound}")
            }
   	        if ((state?.allowScheduledPoll == true || state?.allowScheduledPoll == null) && (resume)) {
				unschedule("poll")
				LOGDEBUG("TALK(${appname}.${evt.name})|mP| Delaying polling for 120 seconds")
				myRunIn(120, poll)
           	}
			if (state.ableToTalk){
				state.sound.duration = (state.sound.duration.toInteger() + 5).toString()  //Try to prevent cutting out, add seconds to the duration
				if (!(customSpeechDevice == null)) {
					currentSpeechDevices = customSpeechDevice
				} else {
					//Use Default Speech Device
					currentSpeechDevices = settings.speechDeviceDefault
				} //if (!(customSpeechDevice == null))
				LOGTRACE("TALK(${appname}.${evt.name})|mP| Last poll: ${state.lastPoll}")
				//Iterate Speech Devices and talk
				def attrs = currentSpeechDevices.supportedAttributes
				currentSpeechDevices.each(){
					LOGDEBUG("TALK(${appname}.${evt.name})|mP| attrs=${attrs}")
					def currentStatus = ""
                   	try {
                    	currentStatus = it?.latestValue("status")
   	                } catch (ex) { LOGDEBUG("ERROR getting device currentStatus") }
					def currentTrack = ""
           	        try {
              	    	currentTrack = it?.latestState("trackData")?.jsonValue
                   	} catch (ex) { LOGDEBUG("ERROR getting device currentTrack") }
					def currentVolume = 0 
                   	try {
                   		currentVolume = it?.latestState("level")?.integerValue ? it.latestState("level")?.integerValue : 0
                   	} catch (ex) { LOGDEBUG("ERROR getting device currentVolume") }
                   	def minimumVolume = 50
                   	if (settings?.speechMinimumVolume >= 0) {minimumVolume = settings.speechMinimumVolume}
                   	if (minimumVolume > 100) {minimumVolume = 100}
                   	def desiredVolume = volume
                   	//try {
                   	//	desiredVolume = settings?.speechVolume
                   	//} catch (ex) { LOGDEBUG("ERROR getting desired default volume"); desiredVolume = -1 }
                   	if (desiredVolume > 100) {desiredVolume = 100}
                   	LOGDEBUG("TALK(${appname}.${evt.name})|mP| currentStatus:${currentStatus}")
					LOGDEBUG("TALK(${appname}.${evt.name})|mP| currentTrack:${currentTrack}")
					LOGDEBUG("TALK(${appname}.${evt.name})|mP| currentVolume:${currentVolume}")
					LOGDEBUG("TALK(${appname}.${evt.name})|mP| Sound: ${state.sound.uri} , ${state.sound.duration}")
					if (desiredVolume > -1){ 
                   		LOGTRACE("TALK(${appname}.${evt.name})|mP| ${it.displayName} | Volume: ${currentVolume}, Desired Volume: ${desiredVolume}")
                   	} else {
	                    if (!(currentVolume >= minimumVolume)) {
   		                	LOGTRACE("TALK(${appname}.${evt.name})|mP| ${it.displayName} | Volume: ${currentVolume}, Minimum Volume: ${minimumVolume}; adjusting.")
       		            } else {
           		        	LOGTRACE("TALK(${appname}.${evt.name})|mP| ${it.displayName} | Volume: ${currentVolume}, Minimum Volume: ${minimumVolume}; acceptable.")
               		    }
                   	}
					if (!(currentTrack == null)){
						//currentTrack has data
						if (!(currentTrack?.status == null)) { LOGTRACE("TALK(${appname}.${evt.name})|mP| ${it.displayName} | Current Status: ${currentStatus}, CurrentTrack: ${currentTrack}, CurrentTrack.Status: ${currentTrack.status}.") }
						if (currentTrack?.status == null) { LOGTRACE("TALK(${appname}.${evt.name})|mP| ${it.displayName} | Current Status: ${currentStatus}, CurrentTrack: ${currentTrack}.") }
						if ((currentStatus == 'playing' || currentTrack?.status == 'playing') && (!((currentTrack?.status == 'stopped') || (currentTrack?.status == 'paused')))) {  //Give currentTrack.status presidence if it exists, it seems more accurate
							if (resume) {
                            	LOGTRACE ("Sending playTrackandResume() 1")
								LOGTRACE("TALK(${appname}.${evt.name})|mP| ${it?.displayName} | cT<>null | cS/cT=playing | Sending playTrackAndResume() | CVol=${currentVolume} | SVol=${desiredVolume}")
								if (desiredVolume > -1) { 
									if (desiredVolume == currentVolume){it.playTrackAndResume(state.sound.uri, state.sound.duration, [delay: myDelay])}
									if (!(desiredVolume == currentVolume)){it.playTrackAndResume(state.sound.uri, state.sound.duration, desiredVolume, [delay: myDelay])}
									spoke = true
								} else { 
									if (currentVolume >= minimumVolume) { it.playTrackAndResume(state.sound.uri, state.sound.duration, [delay: myDelay]) }
									if (currentVolume < minimumVolume) { it.playTrackAndResume(state.sound.uri, state.sound.duration, minimumVolume, [delay: myDelay]) }
									spoke = true
								} //if (desiredVolume)
							} else {
								//resume is not desired
								LOGTRACE ("Sending playTrackandRestore() 2 - ${it?.displayName} - cVol = ${currentVolume}")
                               	LOGTRACE("TALK(${appname}.${evt.name})|mP| ${it?.displayName} | cT<>null | cS/cT=playing | NoResume! | Sending playTrackAndRestore() | CVol=${currentVolume} | SVol=${desiredVolume}")
								if (desiredVolume > -1) { 
									if (desiredVolume == currentVolume){it.playTrackAndRestore(state.sound.uri, state.sound.duration, [delay: myDelay])}
									if (!(desiredVolume == currentVolume)){it.playTrackAndRestore(state.sound.uri, state.sound.duration, desiredVolume, [delay: myDelay])}
									spoke = true
								} else { 
									if (currentVolume >= minimumVolume) { it.playTrackAndRestore(state.sound.uri, state.sound.duration, [delay: myDelay]) }
									if (currentVolume < minimumVolume) { it.playTrackAndRestore(state.sound.uri, state.sound.duration, minimumVolume, [delay: myDelay]) }
									spoke = true
								} // if (desiredVolume)
							} // if (resume)	
						} else {
							if ((!currentTrack?.status == 'playing') && (currentStatus == 'playing')) {
								LOGDEBUG "TALK(${appname}.${evt.name})|mP| ${it?.displayName} | Discrepency in CS/CT, going with CT! | CS= ${currentStatus} CT=${currentTrack.status}"
							}
							LOGTRACE ("Sending playTrackandRestore() 3 - to ${it?.displayName} - cVol = ${currentVolume}")
                           	LOGTRACE("TALK(${appname}.${evt.name})|mP| ${it?.displayName} | cT<>null | cS/cT<>playing | Sending playTrackAndRestore() | CVol=${currentVolume} | SVol=${desiredVolume}")
							if (desiredVolume > -1) { 
								if (desiredVolume == currentVolume){it.playTrackAndRestore(state.sound.uri, state.sound.duration, [delay: myDelay])}
								if (!(desiredVolume == currentVolume)){it.playTrackAndRestore(state.sound.uri, state.sound.duration, desiredVolume, [delay: myDelay])}
								spoke = true
							} else { 
								if (currentVolume >= minimumVolume) { it.playTrackAndRestore(state.sound.uri, state.sound.duration, [delay: myDelay]) }
								if (currentVolume < minimumVolume) { it.playTrackAndRestore(state.sound.uri, state.sound.duration, minimumVolume, [delay: myDelay]) }
								spoke = true
							}// if (desiredVolume)
						}// if ((currentStatus == 'playing' || currentTrack?.status == 'playing') && (!((currentTrack?.status == 'stopped') || (currentTrack?.status == 'paused'))))
					} else {
						//currentTrack==null. currentTrack doesn't have data or is not supported on this device
						if (!(currentStatus == null)) {
							LOGTRACE("TALK(${appname}.${evt.name})|mP|  ${it?.displayName} | (2) Current Status: ${currentStatus}.")
							if (currentStatus == "disconnected") {
								//VLCThing?
								if (resume) {
									LOGTRACE ("Sending playTrackandResume() 4")
                   	                LOGTRACE("TALK(${appname}.${evt.name})|mP| ${it?.displayName} | cT=null | cS=disconnected | Sending playTrackAndResume() | CVol=${currentVolume} | SVol=${desiredVolume}")
									if (desiredVolume > -1) { 
										if (desiredVolume == currentVolume){it.playTrackAndResume(state.sound.uri, state.sound.duration, [delay: myDelay])}
										if (!(desiredVolume == currentVolume)){it.playTrackAndResume(state.sound.uri, state.sound.duration, desiredVolume, [delay: myDelay])}
										spoke = true
									} else { 
										if (currentVolume >= minimumVolume) { it.playTrackAndResume(state.sound.uri, state.sound.duration, [delay: myDelay]) }
										if (currentVolume < minimumVolume) { it.playTrackAndResume(state.sound.uri, state.sound.duration, minimumVolume, [delay: myDelay]) }
										spoke = true
									}
								} else {
									//resume is not desired
                       	            LOGTRACE ("Sending playTrackandRestore() 5")
									LOGTRACE("TALK(${appname}.${evt.name})|mP| ${it?.displayName} | cT=null | cS=disconnected | No Resume! | Sending playTrackAndRestore() | CVol=${currentVolume} | SVol=${desiredVolume}")
									if (desiredVolume > -1) { 
										if (desiredVolume == currentVolume){it.playTrackAndRestore(state.sound.uri, state.sound.duration, [delay: myDelay])}
										if (!(desiredVolume == currentVolume)){it.playTrackAndRestore(state.sound.uri, state.sound.duration, desiredVolume, [delay: myDelay])}
										spoke = true
									} else { 
										if (currentVolume >= minimumVolume) { it.playTrackAndRestore(state.sound.uri, state.sound.duration, [delay: myDelay]) }
										if (currentVolume < minimumVolume) { it.playTrackAndRestore(state.sound.uri, state.sound.duration, minimumVolume, [delay: myDelay]) }
										spoke = true
									}// if (desiredVolume)
								}// if (resume)
							} else {
								if (currentStatus == "playing") {
									if (resume) {
                                    	LOGTRACE ("Sending playTrackandResume() 6")
										LOGTRACE("TALK(${appname}.${evt.name})|mP| ${it?.displayName} | cT=null | cS=playing | Sending playTrackAndResume() | CVol=${currentVolume} | SVol=${desiredVolume}")
										if (desiredVolume > -1) { 
											if (desiredVolume == currentVolume){it.playTrackAndResume(state.sound.uri, state.sound.duration, [delay: myDelay])}
											if (!(desiredVolume == currentVolume)){it.playTrackAndResume(state.sound.uri, state.sound.duration, desiredVolume, [delay: myDelay])}
											spoke = true
										} else { 
											if (currentVolume >= minimumVolume) { it.playTrackAndResume(state.sound.uri, state.sound.duration, [delay: myDelay]) }
											if (currentVolume < minimumVolume) { it.playTrackAndResume(state.sound.uri, state.sound.duration, minimumVolume, [delay: myDelay]) }
											spoke = true
										}// if (desiredVolume)
									} else {
										//resume not desired
										LOGTRACE ("Sending playTrackandRestore() 7")
           	                            LOGTRACE("TALK(${appname}.${evt.name})|mP| ${it?.displayName} | cT=null | cS=playing | No Resume! | Sending playTrackAndRestore() | CVol=${currentVolume} | SVol=${desiredVolume}")
										if (desiredVolume > -1) { 
											if (desiredVolume == currentVolume){it.playTrackAndRestore(state.sound.uri, state.sound.duration, [delay: myDelay])}
											if (!(desiredVolume == currentVolume)){it.playTrackAndRestore(state.sound.uri, state.sound.duration, desiredVolume, [delay: myDelay])}
											spoke = true
										} else { 
											if (currentVolume >= minimumVolume) { it.playTrackAndRestore(state.sound.uri, state.sound.duration, [delay: myDelay]) }
											if (currentVolume < minimumVolume) { it.playTrackAndRestore(state.sound.uri, state.sound.duration, minimumVolume, [delay: myDelay]) }
											spoke = true
										}// if (desiredVolume)
									}// if (resume)
								} else {
									//currentStatus <> "playing"
                                    LOGTRACE ("Sending playTrackandRestore() 8")
									LOGTRACE("TALK(${appname}.${evt.name})|mP| ${it?.displayName} | cT=null | cS<>playing | Sending playTrackAndRestore() | CVol=${currentVolume} | SVol=${desiredVolume}")
									if (desiredVolume > -1) { 
										if (desiredVolume == currentVolume){it.playTrackAndRestore(state.sound.uri, state.sound.duration, [delay: myDelay])}
										if (!(desiredVolume == currentVolume)){it.playTrackAndRestore(state.sound.uri, state.sound.duration, desiredVolume, [delay: myDelay])}
										spoke = true
									} else { 
										if (currentVolume >= minimumVolume) { it.playTrackAndRestore(state.sound.uri, state.sound.duration, [delay: myDelay]) }
										if (currentVolume < minimumVolume) { it.playTrackAndRestore(state.sound.uri, state.sound.duration, minimumVolume, [delay: myDelay]) }
										spoke = true
									}// if (desiredVolume)
								}// if (currentStatus == "playing")
							}// if (currentStatus == "disconnected"))
						} else {
							//currentTrack and currentStatus are both null
							LOGTRACE ("Sending playTrackandRestore() 9")
       	                    LOGTRACE("TALK(${appname}.${evt.name})|mP| ${it.displayName} | (3) cT=null | cS=null | Sending playTrackAndRestore() | CVol=${currentVolume} | SVol=${desiredVolume}")
							if (desiredVolume > -1) { 
								if (desiredVolume == currentVolume){it.playTrackAndRestore(state.sound.uri, state.sound.duration, [delay: myDelay])}
								if (!(desiredVolume == currentVolume)){it.playTrackAndRestore(state.sound.uri, state.sound.duration, desiredVolume, [delay: myDelay])}
								spoke = true
							} else { 
								if (currentVolume >= minimumVolume) { it.playTrackAndRestore(state.sound.uri, state.sound.duration, [delay: myDelay]) }
								if (currentVolume < minimumVolume) { it.playTrackAndRestore(state.sound.uri, state.sound.duration, minimumVolume, [delay: myDelay]) }
								spoke = true
							} //if (desiredVolume)
						} //currentStatus == null
					} //currentTrack == null
				} //currentSpeechDevices.each()
			} //state.ableToTalk
		} //if (!(settings.speechDeviceDefault == null) || !(customSpeechDevice == null))
	}// if (state.speechDeviceType=="capability.musicPlayer")
	if ((state.speechDeviceType == "capability.speechSynthesis") && (!( phrase==null ) && !(phrase==""))){
		//capability.speechSynthesis is in use
		if (!(settings?.speechDeviceDefault == null) || !(customSpeechDevice == null)) {
			LOGTRACE("TALK(${appname}.${evt.name}) |sS| >> ${phrase}")
			if (!(customSpeechDevice == null)) {
				currentSpeechDevices = customSpeechDevice
			} else {
				//Use Default Speech Device
				currentSpeechDevices = settings.speechDeviceDefault
			}// If (!(customSpeechDevice == null))
			//Iterate Speech Devices and talk
			def attrs = currentSpeechDevices.supportedAttributes
			currentSpeechDevices.each(){
				// Determine device name either by it.displayName or it.device.displayName (whichever works)
				try {
					LOGTRACE("TALK(${appname}.${evt.name}) |sS| ${it.displayName} | Sending speak().")
				}
				catch (ex) {
					LOGDEBUG("TALK(${appname}.${evt.name}) |sS| it.displayName failed, trying it.device.displayName")
					try {
						LOGTRACE("TALK(${appname}.${evt.name}) |sS| ${it.device.displayName} | Sending speak().")
					}
					catch (ex2) {
						LOGDEBUG("TALK(${appname}.${evt.name}) |sS| it.device.displayName failed, trying it.device.name")
						LOGTRACE("TALK(${appname}.${evt.name}) |sS| ${it.device.name} | Sending speak().")
					}
				}
				spoke = true
				it.speak(phrase)
			}// currentSpeechDevices.each()
		} //if (!(settings.speechDeviceDefault == null) || !(customSpeechDevice == null))
	} //if (state.speechDeviceType == "capability.speechSynthesis")

	if ((!(smartAppSpeechDevice) && !(spoke)) && (!(phrase == null) && !(phrase == "")) && !(playAudioFile)) {
		//No musicPlayer, speechSynthesis, or smartAppSpeechDevices selected. No route to export speech!
		LOGTRACE("TALK(${appname}.${evt.name}) |ERROR| No selected speech device or smartAppSpeechDevice token in phrase. ${phrase}")
	} else {
    	if ((smartAppSpeechDevice && !spoke) && (!(phrase == null) && !(phrase == ""))){
			LOGTRACE("TALK(${appname}.${evt.name}) |sA| Sent to another smartApp.")
       	}
   	}
    phrase = ""
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
        case "SHM":
            if (index == 1 && (!(settings.SHMStartTimeAway == null))) {
                if (timeOfDayIsBetween(settings.SHMStartTimeAway, settings.SHMEndTimeAway, now, location.timeZone)) { return true } else { return false }
            }
            if (index == 2 && (!(settings.SHMStartTimeHome == null))) {
                if (timeOfDayIsBetween(settings.SHMStartTimeHome, settings.SHMEndTimeHome, now, location.timeZone)) { return true } else { return false }
            }
            if (index == 3 && (!(settings.SHMStartTimeDisarm == null))) {
                if (timeOfDayIsBetween(settings.SHMStartTimeDisarm, settings.SHMEndTimeDisarm, now, location.timeZone)) { return true } else { return false }
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
        case "SHM":
            if (index == 1) {
                //SHM Armed Away
                if (settings.SHMModesAway) {
                    if (settings.SHMModesAway.contains(location.mode)) {
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
                //SHM Armed Home
                if (settings.SHMModesHome) {
                    if (settings.SHMModesHome.contains(location.mode)) {
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
                //SHM Disarmed
                if (settings.SHMModesDisarm) {
                    if (settings.SHMModesDisarm.contains(location.mode)) {
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
        //End: case "SHM"
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

def TalkQueue(appname, phrase, customSpeechDevice, volume, resume, personality, voice, evt){
    //IN DEVELOPMENT
    // Already talking or just recently (within x seconds) started talking
    // Queue up current request(s), give time for current action to complete, then speak and flush queue
    def threshold = 0
    def minDelay = 6 //Minimum seconds between talking
    try {
    	if (!(state?.sound?.duration == null)) { 
    		threshold = state.sound.duration.toInteger() //Use the last musicPlayer sound duration from the last Talk call as the minimum delay
    	}
    } catch (exception) {
    	threshold = 10
    }
    def durationFromLastTalkReq = 9999
    //if (!(state.lastTalkTime == null)) { durationFromLastTalk = ((now() - state?.lastTalkTime)/1000).intValue() }
    if (!(state.lastTalkRequest == null)) { durationFromLastTalkReq = ((now() - state?.lastTalkRequest)/1000).intValue() }
    state.lastTalkRequest = now()
    def tooSoon = (durationFromLastTalkReq < threshold)
    def neededDelay = (((threshold - durationFromLastTalkReq) * 1000) + 1000)
    LOGDEBUG ("TALKQUEUE(Threshold=${threshold},DurationFromLastTalkReq=${durationFromLastTalkReq},lastTalkReq=${state.lastTalkRequest},lastTalkTime=${state.lastTalkTime}, TooSoon=${tooSoon}, Calc=${neededDelay}")
    if (tooSoon) {
    	if (neededDelay < 0) { 
        	neededDelay = 0 
        } else {
    		if (neededDelay < (minDelay * 1000)) { neededDelay = (minDelay * 1000) }
        }
    	LOGDEBUG("TALKQUEUE()-Spoke too recently; delaying ${(neededDelay / 1000)} seconds.")
        return neededDelay
    } else {
    	LOGDEBUG("TALKQUEUE()-OK to speak; (${(durationFromLastTalkReq)})")
    	return 0
    }
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
    if (settings?.resumePlay == true || settings?.resumePlay == null) {
		unschedule("poll")
    	//LOGDEBUG("poll() settings=${settings?.allowScheduledPoll}")
    	//LOGDEBUG("poll() state=${state?.allowScheduledPoll}")
    	//LOGDEBUG("poll() resumePlay=${settings?.resumePlay}")
    	if (((settings?.allowScheduledPoll == true || state?.allowScheduledPoll == true)) || ((settings?.allowScheduledPoll == null) || (state?.allowScheduledPoll == null))) {
	    	state.allowScheduledPoll = true
    	} else {
    		state.allowScheduledPoll = false
        	LOGDEBUG("Polling is not desired, disabling after this poll.")
    	}
    	if (state.speechDeviceType == "capability.musicPlayer") {
        	LOGDEBUG("Polling speech device(s) for latest status")
        	state.polledDevices = ""
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
        	state.lastPoll = getTimeFromCalendar(true,true)
        	//LOGDEBUG("poll: state.polledDevices == ${state?.polledDevices}")
        	if (!(state?.polledDevices == "")) {
	        	//Reschedule next poll
    	    	if (((settings?.allowScheduledPoll == true || state?.allowScheduledPoll == true)) || ((settings?.allowScheduledPoll == null) || (state?.allowScheduledPoll == null))) {
        	    	LOGDEBUG("Rescheduling Poll")
            	    myRunIn(60, poll) 
            	}
        	} else {
        		LOGDEBUG("No speech devices polled. Cancelling polling.")
        	}
    	}
	}
}
def dopoll(pollSpeechDevice){
    pollSpeechDevice.each(){
    	def devicename = ""
        try {
        	devicename = it.displayName
        } catch (ex) {}
        if (devicename == "") {
        	try {
            	devicename = it.device.displayName
            } catch (ex) {}
        }
        if (devicename == "") {
        	LOGERROR("dopoll(${pollSpeechDevice}) - Unable to get devicename")
        }
        if (!(state?.polledDevices?.find("|${devicename}|"))) {
            state.polledDevices = state?.polledDevices + "|${devicename}|"
            LOGDEBUG("dopoll(${devicename}) Polling ")
            state.refresh = false
            state.poll = false
            try {
                //LOGTRACE("refresh()")
                it.refresh()
                state.refresh = true
            }
            catch (ex) {
                LOGDEBUG("ERROR(informational): it.refresh: ${ex}")
                state.refresh = false
            }
            if (!state.refresh) {
                try {
                    //LOGTRACE("poll()")
                    it.poll()
                    state.poll = true
                    state.refresh = true
                }
                catch (ex) {
                    LOGDEBUG ("ERROR(informational): it.poll: ${ex}")
                    state.refresh = false
                }
            }
    	    LOGDEBUG("dopoll(${it.displayName})cS=${it?.latestValue('status')},cT=${it?.latestState("trackData")?.jsonValue?.status},cV=${it?.latestState("level")?.integerValue ? it?.latestState("level")?.integerValue : 0}")
            if (it?.latestValue('status') == "no_device_present") { LOGTRACE("During polling, the handler for ${devicename} indicated the device was not found.") } //VLCThing
        }
        LOGDEBUG("dopoll - polled devices: ${state?.polledDevices}")
    }
}

def getDesiredVolume(invol) {
	def globalVolume = settings?.speechVolume
    def globalMinimumVolume = settings?.speechMinimumVolume
    def myVolume = invol
    def finalVolume = -1
    if (myVolume > 0) { 
    	finalVolume = myVolume
	} else {
		if (globalVolume > 0) {
			finalVolume = globalVolume
		} else {
            if (globalMinimumVolume > 0) {
                finalVolume = globalMinimumVolume
            } else {
                finalVolume = 50 //Default if no volume parameters are set
            }
        }
	}
    if (state.speechDeviceType == "capability.musicPlayer") { 
    	LOGDEBUG("finalVolume: ${finalVolume}")
    }
    return finalVolume
}

def setLastMode(mode){
	state.lastMode = mode
}

def LOGDEBUG(txt){
	def msgfrom = "[PARENT] "
	if (txt?.contains("[CHILD:")) { msgfrom = "" }
    try {
    	if (settings.debugmode) { log.debug("${app.label.replace(" ","").toUpperCase()}(${state.appversion}) || ${msgfrom}${txt}") }
    } catch(ex) {
    	log.error("LOGDEBUG unable to output requested data!")
    }
}
def LOGTRACE(txt){
	def msgfrom = "[PARENT] "
    if (txt?.contains("[CHILD:")) { msgfrom = "" }
    try {
    	log.trace("${app.label.replace(" ","").toUpperCase()}(${state.appversion}) || ${msgfrom}${txt}")
    } catch(ex) {
    	log.error("LOGTRACE unable to output requested data!")
    }
}
def LOGERROR(txt){
	def msgfrom = "[PARENT] "
    if (txt?.contains("[CHILD:")) { msgfrom = "" }
    try {
    log.error("${app.label.replace(" ","").toUpperCase()}(${state.appversion}) || ${msgfrom}ERROR: ${txt}")
    } catch(ex) {
    	log.error("LOGERROR unable to output requested data!")
    }
}

def setAppVersion(){
    state.appversion = "P2.0.6"
}