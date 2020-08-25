definition(
    name: "BigTalker2-Child",
    namespace: "rayzurbock",
    author: "rayzur@rayzurbock.com",
    description: "Do not install in the mobile app, Save don't publish (needed by BigTalker2)",
    category: "Fun & Social",
    parent: "rayzurbock:BigTalker2",
    iconUrl: "http://lowrance.cc/ST/icons/BigTalker-2.0.6.png",
    iconX2Url: "http://lowrance.cc/ST/icons/BigTalker@2x-2.0.6.png",
    iconX3Url: "http://lowrance.cc/ST/icons/BigTalker@2x-2.0.6.png")

preferences {
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
    page(name: "pageConfigSHM")
    page(name: "pageConfigPowerMeter")
    page(name: "pageConfigRoutine")
    page(name: "pageHelpPhraseTokens")
}

def pageConfigureEvents(){
    dynamicPage(name: "pageConfigureEvents", title: "Configure Events", install: (!(app?.getInstallationState == true)), uninstall: (app?.getInstallationState == true)) {
        section("Group Settings:"){
            label(name: "labelRequired", title: "Event Group Name:", defaultValue: "Change this", required: true, multiple: false)
            input(name: "groupEnabled", type: "boolean", title: "Enable Group", required: true, defaultValue: true)
        }
        section("Talk on events:") {
            if (settings.timeSlotTime1 || settings.timeSlotTime2 || settings.timeSlotTime3) {
                href "pageConfigTime", title: "Time", description: "Tap to modify", state:"complete"
            } else {
                href "pageConfigTime", title: "Time", description: "Tap to configure"
            }
            if (settings.motionDeviceGroup1 || settings.motionDeviceGroup2 || settings.motionDeviceGroup3) {
                href "pageConfigMotion", title:"Motion", description:"Tap to modify", state:"complete"
            } else {
                href "pageConfigMotion", title:"Motion", description:"Tap to configure"
            }
            if (settings.switchDeviceGroup1 || settings.switchDeviceGroup2 || settings.switchDeviceGroup3) {
                href "pageConfigSwitch", title:"Switch", description:"Tap to modify", state:"complete"
            } else {
                href "pageConfigSwitch", title:"Switch", description:"Tap to configure"
            }
            if (settings.presDeviceGroup1 || settings.presDeviceGroup2 || settings.presDeviceGroup3) {
                href "pageConfigPresence", title:"Presence", description:"Tap to modify", state:"complete"
            } else {
                href "pageConfigPresence", title:"Presence", description:"Tap to configure"
            }
            if (settings.lockDeviceGroup1 || settings.lockDeviceGroup2 || settings.lockDeviceGroup3) {
                href "pageConfigLock", title:"Lock", description:"Tap to modify", state:"complete"
            } else {
                href "pageConfigLock", title:"Lock", description:"Tap to configure"
            }
            if (settings.contactDeviceGroup1 || settings.contactDeviceGroup2 || settings.contactDeviceGroup3) {
                href "pageConfigContact", title:"Contact", description:"Tap to modify", state:"complete"
            } else {
                href "pageConfigContact", title:"Contact", description:"Tap to configure"
            }
            if (settings.modePhraseGroup1 || settings.modePhraseGroup2 || settings.modePhraseGroup3) {
                href "pageConfigMode", title:"Mode", description:"Tap to modify", state:"complete"
            } else {
                href "pageConfigMode", title:"Mode", description:"Tap to configure"
            }
            if (settings.thermostatDeviceGroup1 || settings.thermostatDeviceGroup2 || settings.thermostatDeviceGroup3) {
                href "pageConfigThermostat", title:"Thermostat", description:"Tap to modify", state:"complete"
            } else {
                href "pageConfigThermostat", title:"Thermostat", description:"Tap to configure"
            }
            if (settings.accelerationDeviceGroup1 || settings.accelerationDeviceGroup2 || settings.accelerationDeviceGroup3) {
                href "pageConfigAcceleration", title: "Acceleration", description:"Tap to modify", state:"complete"
            } else {
                href "pageConfigAcceleration", title: "Acceleration", description:"Tap to configure"
            }
            if (settings.waterDeviceGroup1 || settings.waterDeviceGroup2 || settings.waterDeviceGroup3) {
                href "pageConfigWater", title: "Water", description:"Tap to modify", state:"complete"
            } else {
                href "pageConfigWater", title: "Water", description:"Tap to configure"
            }
            if (settings.smokeDeviceGroup1 || settings.smokeDeviceGroup2 || settings.smokeDeviceGroup3) {
                href "pageConfigSmoke", title: "Smoke", description:"Tap to modify", state:"complete"
            } else { 
                href "pageConfigSmoke", title: "Smoke", description:"Tap to configure"
            }
            if (settings.buttonDeviceGroup1 || settings.buttonDeviceGroup2 || settings.buttonDeviceGroup3) {
                href "pageConfigButton", title: "Button", description:"Tap to modify", state:"complete"
            } else {
                href "pageConfigButton", title: "Button", description:"Tap to configure"
            }
            if (settings.SHMTalkOnHome || settings.SHMTalkOnAway || settings.SHMTalkOnDisarm) {
                href "pageConfigSHM", title: "Smart Home Monitor", description:"Tap to modify", state:"complete"
            } else {
                href "pageConfigSHM", title: "Smart Home Monitor", description:"Tap to configure"
            }
            if (settings.powerMeterDeviceGroup1 || settings.powerMeterDeviceGroup2 || settings.powerMeterDeviceGroup3) {
                href "pageConfigPowerMeter", title: "Power Meter", description:"Tap to modify", state:"complete"
            } else {
                href "pageConfigPowerMeter", title: "Power Meter", description:"Tap to configure"
            }
            if (settings.routineDeviceGroup1 || settings.routineDeviceGroup2 || settings.routineDeviceGroup3) {
                href "pageConfigRoutine", title: "Routine", description:"Tap to modify", state:"complete"
            } else {
                href "pageConfigRoutine", title: "Routine", description:"Tap to configure"
            }
        }
    }
}

def pageConfigMotion(){
    dynamicPage(name: "pageConfigMotion", title: "Configure talk on motion", install: false, uninstall: false) {
        section(){
            def defaultSpeechActive1 = ""
            def defaultSpeechInactive1 = ""
            if (state?.motionTestActive1 == null) { state.motionTestActive1 = false }
            if (state?.motionTestInactive1 == null) { state.motionTestInactive1 = false }
            if (!motionDeviceGroup1) {
                defaultSpeechActive1 = "%devicename% is now %devicechange%"
                defaultSpeechInactive1 = "%devicename% is now %devicechange%"
            }
            input name: "motionDeviceGroup1", type: "capability.motionSensor", title: "Motion Sensor(s)", required: false, multiple: true
            input name: "motionTalkActive1", type: "text", title: "Say this on motion active:", required: false, defaultValue: defaultSpeechActive1, submitOnChange: true
            input name: "motionTestActive1", type: "bool", title: "Toggle to test motion active phrase", required: false, defaultValue: false, submitOnChange: true
            input name: "motionTalkInactive1", type: "text", title: "Say this on motion inactive:", required: false, defaultValue: defaultSpeechInactive1, submitOnChange: true
            input name: "motionTestInactive1", type: "bool", title: "Toggle to test motion inactive phrase", required: false, defaultValue: false, submitOnChange: true
            input name: "motionPersonality1", type: "enum", title: "Allow Personality (overrides default)?:", required: false, options: ["Yes", "No"], submitOnChange: true
            input name: "motionSpeechDevice1", type: parent?.state?.speechDeviceType, title: "Talk with these text-to-speech devices (overrides default)", multiple: true, required: false, submitOnChange: true
            if (parent?.state?.speechDeviceType == "capability.musicPlayer") {
            	input name: "motionVolume1", type: "number", title: "Set volume to (overrides default):", required: false, submitOnChange: true
            	input name: "motionResumePlay1", type: "bool", title: "Attempt to resume playing audio?", required: false, defaultValue: (parent?.settings?.resumePlay == false) ? false : true, submitOnChange: true
                input name: "motionVoice1", type: "enum", title: "Voice (overrides default):", options: parent?.state?.supportedVoices, required: false, submitOnChange: true
            }
        }
        section("Restrictions"){
            input name: "motionModes1", type: "mode", title: "Talk when in these mode(s) (overrides default)", multiple: true, required: false
            input name: "motionStartTime1", type: "time", title: "Don't talk before (overrides default)", required: false, submitOnChange: true
            input name: "motionEndTime1", type: "time", title: "Don't talk after (overrides default)", required: (!(settings.motionStartTime1 == null))
            input name: "motionDays1", type: "enum", title: "Restrict to these day(s)", required: false, options: ["Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"], multiple: true
            //input name: "motionCount1", type: "number", title: "Do this only x times (next prompt)...", required: false, submitOnChange: true
            //input name: "motionCountUnit1", type:"enum", title: "... per ", required: settings.motionCount1, options: ["Minute", "Hour", "Day"]
            input name: "motionDisableSwitch1", type: "capability.switch", title: "Disable when this switch is off", required: false, multiple: false
            if (!(settings.motionTestActive1 == null) && !(settings.motionTestActive1 == state?.motionTestActive1)) {
            	def testevent = [displayName: 'BigTalker Motion', name: 'MotionActiveTest', value: 'Active']
                def myVoice = parent?.settings?.speechVoice
                if (settings?.motionVoice1) { myVoice = motionVoice1 }
            	sendTalk(app.label, settings.motionTalkActive1, motionSpeechDevice1, motionVolume1, motionResumePlay1, motionPersonality1, myVoice, testevent)
                state.motionTestActive1 = settings.motionTestActive1
            }
            if (!(settings.motionTestInactive1 == null) && !(settings.motionTestInactive1 == state?.motionTestInactive1)) {
            	def testevent = [displayName: 'BigTalker Motion', name: 'MotionInactiveTest', value: 'Inactive']
                def myVoice = parent?.settings?.speechVoice
                if (settings?.motionVoice1) { myVoice = motionVoice1 }
            	sendTalk(app.label, settings.motionTalkInactive1, motionSpeechDevice1, motionVolume1, motionResumePlay1, motionPersonality1, myVoice, testevent)
                state.motionTestInactive1 = settings.motionTestInactive1
            }
        }
        section("Help"){
            href "pageHelpPhraseTokens", title:"Phrase Tokens", description:"Tap for a list of phrase tokens"
        }
    }
//End pageConfigMotion()
}

def pageConfigSwitch(){
    dynamicPage(name: "pageConfigSwitch", title: "Configure talk on switch", install: false, uninstall: false) {
        section(){
            def defaultSpeechOn1 = ""
            def defaultSpeechOff1 = ""
            if (state?.switchTestOn1 == null) { state.switchTestOn1 = false }
            if (state?.switchTestOff1 == null) { state.switchTestOff1 = false }
            if (!switchDeviceGroup1) {
                defaultSpeechOn1 = "%devicename% is now %devicechange%"
                defaultSpeechOff1 = "%devicename% is now %devicechange%"
            }
            input name: "switchDeviceGroup1", type: "capability.switch", title: "Switch(es)", required: false, multiple: true
            input name: "switchTalkOn1", type: "text", title: "Say this when switch is turned ON:", required: false, defaultValue: defaultSpeechOn1, submitOnChange: true
            input name: "switchTestOn1", type: "bool", title: "Toggle to test switch ON phrase", required: false, defaultValue: false, submitOnChange: true
            input name: "switchTalkOff1", type: "text", title: "Say this when switch is turned OFF:", required: false, defaultValue: defaultSpeechOff1, submitOnChange: true
            input name: "switchTestOff1", type: "bool", title: "Toggle to test switch OFF phrase", required: false, defaultValue: false, submitOnChange: true
            input name: "switchPersonality1", type: "enum", title: "Allow Personality (overrides default)?:", required: false, options: ["Yes", "No"], submitOnChange: true
            input name: "switchSpeechDevice1", type: parent?.state?.speechDeviceType, title: "Talk with these text-to-speech devices (overrides default)", multiple: true, required: false, submitOnChange: true
            if (parent?.state?.speechDeviceType == "capability.musicPlayer") {
            	input name: "switchVolume1", type: "number", title: "Set volume to (overrides default):", required: false, submitOnChange: true
            	input name: "switchResumePlay1", type: "bool", title: "Attempt to resume playing audio?", required: false, defaultValue: (parent?.settings?.resumePlay == false) ? false : true, submitOnChange: true
                input name: "switchVoice1", type: "enum", title: "Voice (overrides default):", options: parent?.state?.supportedVoices, required: false, submitOnChange: true
            }
        }
        section("Restrictions"){
            input name: "switchModes1", type: "mode", title: "Talk when in these mode(s) (overrides default)", multiple: true, required: false
            input name: "switchStartTime1", type: "time", title: "Don't talk before (overrides default)", required: false, submitOnChange: true
            input name: "switchEndTime1", type: "time", title: "Don't talk after (overrides default)", required: (!(settings.switchStartTime1 == null))
            input name: "switchDays1", type: "enum", title: "Restrict to these day(s)", required: false, options: ["Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"], multiple: true
            input name: "switchDisableSwitch1", type: "capability.switch", title: "Disable when this switch is off", required: false, multiple: false
            if (!(settings.switchTestOn1 == null) && !(settings.switchTestOn1 == state?.switchTestOn1)) {
            	def testevent = [displayName: 'BigTalker Switch', name: 'SwitchOnTest', value: 'On']
                def myVoice = parent?.settings?.speechVoice
                if (settings?.switchVoice1) { myVoice = switchVoice1 }
            	sendTalk(app.label, settings.switchTalkOn1, switchSpeechDevice1, switchVolume1, switchResumePlay1, switchPersonality1, myVoice, testevent)
                state.switchTestOn1 = settings.switchTestOn1
            }
            if (!(settings.switchTestOff1 == null) && !(settings.switchTestOff1 == state?.switchTestOff1)) {
            	def testevent = [displayName: 'BigTalker Switch', name: 'SwitchOffTest', value: 'Off']
                def myVoice = parent?.settings?.speechVoice
                if (settings?.switchVoice1) { myVoice = switchVoice1 }
            	sendTalk(app.label, settings.switchTalkOff1, switchSpeechDevice1, switchVolume1, switchResumePlay1, switchPersonality1, myVoice, testevent)
                state.switchTestOff1 = settings.switchTestOff1
            }
        }
        section("Help"){
            href "pageHelpPhraseTokens", title:"Phrase Tokens", description:"Tap for a list of phrase tokens"
        }
    }
//End pageConfigSwitch()
}

def pageConfigPresence(){
    dynamicPage(name: "pageConfigPresence", title: "Configure talk on presence", install: false, uninstall: false) {
        section(){
            def defaultSpeechArrive1 = ""
            def defaultSpeechLeave1 = ""
            if (!presDeviceGroup1) {
                defaultSpeechArrive1 = "%devicename% has arrived"
                defaultSpeechLeave1 = "%devicename% has left"
            }
            input name: "presDeviceGroup1", type: "capability.presenceSensor", title: "Presence Sensor(s)", required: false, multiple: true
            input name: "presTalkOnArrive1", type: "text", title: "Say this when someone arrives:", required: false, defaultValue: defaultSpeechArrive1
            input name: "presTalkOnLeave1", type: "text", title: "Say this when someone leaves:", required: false, defaultValue: defaultSpeechLeave1
            input name: "presPersonality1", type: "enum", title: "Allow Personality (overrides default)?:", required: false, options: ["Yes", "No"]
            input name: "presSpeechDevice1", type: parent?.state?.speechDeviceType, title: "Talk with these text-to-speech devices (overrides default)", multiple: true, required: false
            if (parent?.state?.speechDeviceType == "capability.musicPlayer") {
            	input name: "presVolume1", type: "number", title: "Set volume to (overrides default):", required: false
            	input name: "presResumePlay1", type: "bool", title: "Attempt to resume playing audio?", required: false, defaultValue: (parent?.settings?.resumePlay == false) ? false : true
                input name: "presVoice1", type: "enum", title: "Voice (overrides default):", options: parent?.state?.supportedVoices, required: false, submitOnChange: true
            }
        }
        section("Restrictions"){
            input name: "presModes1", type: "mode", title: "Talk when in these mode(s) (overrides default)", multiple: true, required: false
            input name: "presStartTime1", type: "time", title: "Don't talk before (overrides default)", required: false, submitOnChange: true
            input name: "presEndTime1", type: "time", title: "Don't talk after (overrides default)", required: (!(settings.presStartTime1 == null))
            input name: "presDays1", type: "enum", title: "Restrict to these day(s)", required: false, options: ["Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"], multiple: true
            input name: "presDisableSwitch1", type: "capability.switch", title: "Disable when this switch is off", required: false, multiple: false
        }
        section("Help"){
            href "pageHelpPhraseTokens", title:"Phrase Tokens", description:"Tap for a list of phrase tokens"
        }
    }
//End pageConfigPresence()
}

def pageConfigLock(){
    dynamicPage(name: "pageConfigLock", title: "Configure talk on lock", install: false, uninstall: false) {
        section(){
            def defaultSpeechUnlock1 = ""
            def defaultSpeechLock1 = ""
            if (!lockDeviceGroup1) {
                defaultSpeechUnlock1 = "%devicename% is now %devicechange%"
                defaultSpeechLock1 = "%devicename% is now %devicechange%"
            }
            input name: "lockDeviceGroup1", type: "capability.lock", title: "Lock(s)", required: false, multiple: true
            input name: "lockTalkOnUnlock1", type: "text", title: "Say this when unlocked:", required: false, defaultValue: defaultSpeechUnlock1
            input name: "lockTalkOnLock1", type: "text", title: "Say this when locked:", required: false, defaultValue: defaultSpeechLock1
            input name: "lockPersonality1", type: "enum", title: "Allow Personality (overrides default)?:", required: false, options: ["Yes", "No"]
            input name: "lockSpeechDevice1", type: parent?.state?.speechDeviceType, title: "Talk with these text-to-speech devices (overrides default)", multiple: true, required: false
            if (parent?.state?.speechDeviceType == "capability.musicPlayer") {
            	input name: "lockVolume1", type: "number", title: "Set volume to (overrides default):", required: false
            	input name: "lockResumePlay1", type: "bool", title: "Attempt to resume playing audio?", required: false, defaultValue: (parent?.settings?.resumePlay == false) ? false : true
                input name: "lockVoice1", type: "enum", title: "Voice (overrides default):", options: parent?.state?.supportedVoices, required: false, submitOnChange: true
            }
        }
        section("Restrictions"){
            input name: "lockModes1", type: "mode", title: "Talk when in these mode(s) (overrides default)", multiple: true, required: false
            input name: "lockStartTime1", type: "time", title: "Don't talk before (overrides default)", required: false, submitOnChange: true
            input name: "lockEndTime1", type: "time", title: "Don't talk after (overrides default)", required: (!(settings.lockStartTime1 == null))
            input name: "lockDays1", type: "enum", title: "Restrict to these day(s)", required: false, options: ["Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"], multiple: true
            input name: "lockDisableSwitch1", type: "capability.switch", title: "Disable when this switch is off", required: false, multiple: false
        }
        section("Help"){
            href "pageHelpPhraseTokens", title:"Phrase Tokens", description:"Tap for a list of phrase tokens"
        }
    }
//End pageConfigLock()
}

def pageConfigContact(){
    dynamicPage(name: "pageConfigContact", title: "Configure talk on contact sensor", install: false, uninstall: false) {
        section(){
            def defaultSpeechOpen1 = ""
            def defaultSpeechClose1 = ""
            if (!contactDeviceGroup1) {
                defaultSpeechOpen1 = "%devicename% is now %devicechange%"
                defaultSpeechClose1 = "%devicename% is now %devicechange%"
            }
            input name: "contactDeviceGroup1", type: "capability.contactSensor", title: "Contact sensor(s)", required: false, multiple: true
            input name: "contactTalkOnOpen1", type: "text", title: "Say this when opened:", required: false, defaultValue: defaultSpeechOpen1
            input name: "contactTalkOnClose1", type: "text", title: "Say this when closed:", required: false, defaultValue: defaultSpeechClose1
            input name: "contactPersonality1", type: "enum", title: "Allow Personality (overrides default)?:", required: false, options: ["Yes", "No"]
            input name: "contactSpeechDevice1", type: parent?.state?.speechDeviceType, title: "Talk with these text-to-speech devices (overrides default)", multiple: true, required: false
            if (parent?.state?.speechDeviceType == "capability.musicPlayer") {
            	input name: "contactVolume1", type: "number", title: "Set volume to (overrides default):", required: false
                input name: "contactResumePlay1", type: "bool", title: "Attempt to resume playing audio?", required: false, defaultValue: (parent?.settings?.resumePlay == false) ? false : true
                input name: "contactVoice1", type: "enum", title: "Voice (overrides default):", options: parent?.state?.supportedVoices, required: false, submitOnChange: true
            }
        }
        section("Restrictions"){
            input name: "contactModes1", type: "mode", title: "Talk when in these mode(s) (overrides default)", multiple: true, required: false
            input name: "contactStartTime1", type: "time", title: "Don't talk before (overrides default)", required: false, submitOnChange: true
            input name: "contactEndTime1", type: "time", title: "Don't talk after (overrides default)", required: (!(settings.contactStartTime1 == null))
            input name: "contactDays1", type: "enum", title: "Restrict to these day(s)", required: false, options: ["Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"], multiple: true
            input name: "contactDisableSwitch1", type: "capability.switch", title: "Disable when this switch is off", required: false, multiple: false
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
    LOGDEBUG("locationmodes=${locationmodes}", true)
    dynamicPage(name: "pageConfigMode", title: "Configure talk on home mode change", install: false, uninstall: false) {
        section(){
            def defaultSpeechMode1 = ""
            if (!modePhraseGroup1) {
                defaultSpeechMode1 = "%locationname% mode has changed from %lastmode% to %mode%"
            }
            input name: "modePhraseGroup1", type:"mode", title:"When mode changes to: ", required:false, multiple:true, submitOnChange:false
            input name: "modeExcludePhraseGroup1", type: "mode", title: "But not when changed from (optional): ", required: false, multiple: true
            input name: "TalkOnModeChange1", type: "text", title: "Say this when home mode is changed", required: false, defaultValue: defaultSpeechMode1
            input name: "modePersonality1", type: "enum", title: "Allow Personality (overrides default)?:", required: false, options: ["Yes", "No"]
            input name: "modePhraseSpeechDevice1", type: parent?.state?.speechDeviceType, title: "Talk with these text-to-speech devices (overrides default)", multiple: true, required: false
            if (parent?.state?.speechDeviceType == "capability.musicPlayer") {
            	input name: "modePhraseVolume1", type: "number", title: "Set volume to (overrides default):", required: false
                input name: "modePhraseResumePlay1", type: "bool", title: "Attempt to resume playing audio?", required: false, defaultValue: (parent?.settings?.resumePlay == false) ? false : true
                input name: "modePhraseVoice1", type: "enum", title: "Voice (overrides default):", options: parent?.state?.supportedVoices, required: false, submitOnChange: true
            }
        }
        section("Restrictions"){
            input name: "modeStartTime1", type: "time", title: "Don't talk before (overrides default)", required: false, submitOnChange: true
            input name: "modeEndTime1", type: "time", title: "Don't talk after (overrides default)", required: (!(settings.modeStartTime1 == null))
            input name: "modeDays1", type: "enum", title: "Restrict to these day(s)", required: false, options: ["Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"], multiple: true
            input name: "modeDisableSwitch1", type: "capability.switch", title: "Disable when this switch is off", required: false, multiple: false
        }
        section("Help"){
            href "pageHelpPhraseTokens", title:"Phrase Tokens", description:"Tap for a list of phrase tokens"
        }
    }
//End pageConfigMode()
}

def pageConfigThermostat(){
    dynamicPage(name: "pageConfigThermostat", title: "Configure talk when thermostat state is:", install: false, uninstall: false) {
        section(){
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
            input name: "thermostatPersonality1", type: "enum", title: "Allow Personality (overrides default)?:", required: false, options: ["Yes", "No"]
            input name: "thermostatSpeechDevice1", type: parent?.state?.speechDeviceType, title: "Talk with these text-to-speech devices (overrides default)", multiple: true, required: false
            if (parent?.state?.speechDeviceType == "capability.musicPlayer") {
            	input name: "thermostatVolume1", type: "number", title: "Set volume to (overrides default):", required: false
            	input name: "thermostatResumePlay1", type: "bool", title: "Attempt to resume playing audio?", required: false, defaultValue: (parent?.settings?.resumePlay == false) ? false : true
                input name: "thermostatVoice1", type: "enum", title: "Voice (overrides default):", options: parent?.state?.supportedVoices, required: false, submitOnChange: true
            }
        }
        section("Restrictions"){
            input name: "thermostatModes1", type: "mode", title: "Talk when in these mode(s) (overrides default)", multiple: true, required: false
            input name: "thermostatStartTime1", type: "time", title: "Don't talk before (overrides default)", required: false, submitOnChange: true
            input name: "thermostatEndTime1", type: "time", title: "Don't talk after (overrides default)", required: (!(settings.thermostatStartTime1 == null))
            input name: "thermostatDays1", type: "enum", title: "Restrict to these day(s)", required: false, options: ["Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"], multiple: true
            input name: "thermostatDisableSwitch1", type: "capability.switch", title: "Disable when this switch is off", required: false, multiple: false
        }
        section("Help"){
            href "pageHelpPhraseTokens", title:"Phrase Tokens", description:"Tap for a list of phrase tokens"
        }
    }
//End pageConfigThermostat()
}

def pageConfigAcceleration(){
    dynamicPage(name: "pageConfigAcceleration", title: "Configure talk on acceleration", install: false, uninstall: false) {
        section(){
            def defaultSpeechActive1 = ""
            def defaultSpeechInactive1 = ""
            if (!accelerationDeviceGroup1) {
                defaultSpeechActive1 = "%devicename% acceleration %devicechange%"
                defaultSpeechInactive1 = "%devicename% acceleration is no longer active"
            }
            input name: "accelerationDeviceGroup1", type: "capability.accelerationSensor", title: "Acceleration sensor(s)", required: false, multiple: true
            input name: "accelerationTalkOnActive1", type: "text", title: "Say this when activated:", required: false, defaultValue: defaultSpeechActive1
            input name: "accelerationTalkOnInactive1", type: "text", title: "Say this when inactivated:", required: false, defaultValue: defaultSpeechInactive1
            input name: "accelerationPersonality1", type: "enum", title: "Allow Personality (overrides default)?:", required: false, options: ["Yes", "No"]
            input name: "accelerationSpeechDevice1", type: parent?.state?.speechDeviceType, title: "Talk with these text-to-speech devices (overrides default)", multiple: true, required: false
            if (parent?.state?.speechDeviceType == "capability.musicPlayer") {
            	input name: "accelerationVolume1", type: "number", title: "Set volume to (overrides default):", required: false
            	input name: "accelerationResumePlay1", type: "bool", title: "Attempt to resume playing audio?", required: false, defaultValue: (parent?.settings?.resumePlay == false) ? false : true
                input name: "accelerationVoice1", type: "enum", title: "Voice (overrides default):", options: parent?.state?.supportedVoices, required: false, submitOnChange: true
            }
        }
        section("Restrictions"){
            input name: "accelerationModes1", type: "mode", title: "Talk when in these mode(s) (overrides default)", multiple: true, required: false
            input name: "accelerationStartTime1", type: "time", title: "Don't talk before (overrides default)", required: false, submitOnChange: true
            input name: "accelerationEndTime1", type: "time", title: "Don't talk after (overrides default)", required: (!(settings.accelerationStartTime1 == null))
            input name: "accelerationDays1", type: "enum", title: "Restrict to these day(s)", required: false, options: ["Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"], multiple: true
            input name: "accelerationDisableSwitch1", type: "capability.switch", title: "Disable when this switch is off", required: false, multiple: false
        }
        section("Help"){
            href "pageHelpPhraseTokens", title:"Phrase Tokens", description:"Tap for a list of phrase tokens"
        }
    }
//End pageConfigAcceleration()
}

def pageConfigWater(){
    dynamicPage(name: "pageConfigWater", title: "Configure talk on water", install: false, uninstall: false) {
        section(){
            def defaultSpeechWet1 = ""
            def defaultSpeechDry1 = ""
            if (!waterDeviceGroup1) {
                defaultSpeechWet1 = "%devicename% is %devicechange%"
                defaultSpeechDry1 = "%devicename% is %devicechange%"
            }
            input name: "waterDeviceGroup1", type: "capability.waterSensor", title: "Water sensor(s)", required: false, multiple: true
            input name: "waterTalkOnWet1", type: "text", title: "Say this when wet:", required: false, defaultValue: defaultSpeechWet1
            input name: "waterTalkOnDry1", type: "text", title: "Say this when dry:", required: false, defaultValue: defaultSpeechDry1
            input name: "waterPersonality1", type: "enum", title: "Allow Personality (overrides default)?:", required: false, options: ["Yes", "No"]
            input name: "waterSpeechDevice1", type: parent?.state?.speechDeviceType, title: "Talk with these text-to-speech devices (overrides default)", multiple: true, required: false
            if (parent?.state?.speechDeviceType == "capability.musicPlayer") {
            	input name: "waterVolume1", type: "number", title: "Set volume to (overrides default):", required: false
                input name: "waterResumePlay1", type: "bool", title: "Attempt to resume playing audio?", required: false, defaultValue: (parent?.settings?.resumePlay == false) ? false : true
                input name: "waterVoice1", type: "enum", title: "Voice (overrides default):", options: parent?.state?.supportedVoices, required: false, submitOnChange: true
            }
        }
        section("Restrictions"){
            input name: "waterModes1", type: "mode", title: "Talk when in these mode(s) (overrides default)", multiple: true, required: false
            input name: "waterStartTime1", type: "time", title: "Don't talk before (overrides default)", required: false, submitOnChange: true
            input name: "waterEndTime1", type: "time", title: "Don't talk after (overrides default)", required: (!(settings.waterStartTime1 == null))
            input name: "waterDays1", type: "enum", title: "Restrict to these day(s)", required: false, options: ["Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"], multiple: true
            input name: "waterDisableSwitch1", type: "capability.switch", title: "Disable when this switch is off", required: false, multiple: false
        }
        section("Help"){
            href "pageHelpPhraseTokens", title:"Phrase Tokens", description:"Tap for a list of phrase tokens"
        }
    }
//End pageConfigWater()
}

def pageConfigSmoke(){
    dynamicPage(name: "pageConfigSmoke", title: "Configure talk on smoke", install: false, uninstall: false) {
        section(){
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
            input name: "smokePersonality1", type: "enum", title: "Allow Personality (overrides default)?:", required: false, options: ["Yes", "No"]
            input name: "smokeSpeechDevice1", type: parent?.state?.speechDeviceType, title: "Talk with these text-to-speech devices (overrides default)", multiple: true, required: false
            if (parent?.state?.speechDeviceType == "capability.musicPlayer") {
            	input name: "smokeVolume1", type: "number", title: "Set volume to (overrides default):", required: false
                input name: "smokeResumePlay1", type: "bool", title: "Attempt to resume playing audio?", required: false, defaultValue: (parent?.settings?.resumePlay == false) ? false : true
                input name: "smokeVoice1", type: "enum", title: "Voice (overrides default):", options: parent?.state?.supportedVoices, required: false, submitOnChange: true
            }
        }
        section("Restrictions"){
            input name: "smokeModes1", type: "mode", title: "Talk when in these mode(s) (overrides default)", multiple: true, required: false
            input name: "smokeStartTime1", type: "time", title: "Don't talk before (overrides default)", required: false, submitOnChange: true
            input name: "smokeEndTime1", type: "time", title: "Don't talk after (overrides default)", required: (!(settings.smokeStartTime1 == null))
            input name: "smokeDays1", type: "enum", title: "Restrict to these day(s)", required: false, options: ["Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"], multiple: true
            input name: "smokeDisableSwitch1", type: "capability.switch", title: "Disable when this switch is off", required: false, multiple: false
        }
        section("Help"){
            href "pageHelpPhraseTokens", title:"Phrase Tokens", description:"Tap for a list of phrase tokens"
        }
    }
//End pageConfigSmoke()
}

def pageConfigButton(){
    dynamicPage(name: "pageConfigButton", title: "Configure talk on button press", install: false, uninstall: false) {
        section(){
            def defaultSpeechButton1 = ""
            def defaultSpeechButtonHold1 = ""
            if (!buttonDeviceGroup1) {
                defaultSpeechButton1 = "%devicename% button pressed"
                defaultSpeechButtonHold1 = "%devicename% button held"
            }
            input name: "buttonDeviceGroup1", type: "capability.button", title: "Button(s)", required: false, multiple: true
            input name: "buttonTalkOnPress1", type: "text", title: "Say this when pressed:", required: false, defaultValue: defaultSpeechButton1
            input name: "buttonTalkOnHold1", type: "text", title: "Say this when held:", required: false, defaultValue: defaultSpeechButtonHold1
            input name: "buttonPersonality1", type: "enum", title: "Allow Personality (overrides default)?:", required: false, options: ["Yes", "No"]
            input name: "buttonSpeechDevice1", type: parent?.state?.speechDeviceType, title: "Talk with these text-to-speech devices (overrides default)", multiple: true, required: false
            if (parent?.state?.speechDeviceType == "capability.musicPlayer") {
            	input name: "buttonVolume1", type: "number", title: "Set volume to (overrides default):", required: false
                input name: "buttonResumePlay1", type: "bool", title: "Attempt to resume playing audio?", required: false, defaultValue: (parent?.settings?.resumePlay == false) ? false : true
                input name: "buttonVoice1", type: "enum", title: "Voice (overrides default):", options: parent?.state?.supportedVoices, required: false, submitOnChange: true
            }
        }
        section("Restrictions"){
            input name: "buttonModes1", type: "mode", title: "Talk when in these mode(s) (overrides default)", multiple: true, required: false
            input name: "buttonStartTime1", type: "time", title: "Don't talk before (overrides default)", required: false, submitOnChange: true
            input name: "buttonEndTime1", type: "time", title: "Don't talk after (overrides default)", required: (!(settings.buttonStartTime1 == null))
            input name: "buttonDays1", type: "enum", title: "Restrict to these day(s)", required: false, options: ["Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"], multiple: true
            input name: "buttonDisableSwitch1", type: "capability.switch", title: "Disable when this switch is off", required: false, multiple: false
        }
        section("Help"){
            href "pageHelpPhraseTokens", title:"Phrase Tokens", description:"Tap for a list of phrase tokens"
        }
    }
//End pageConfigButton()
}

def pageConfigSHM(){
    dynamicPage(name: "pageConfigSHM", title: "Configure talk on Smart Home Monitor status change", install: false, uninstall: false) {
    	section(){
        	input name: "SHMPersonality", type: "enum", title: "Allow Personality (overrides default)?:", required: false, options: ["Yes", "No"]
        }
        section("Smart Home Monitor - Armed, Away"){
            def defaultSpeechSHMAway = ""
            if ((!SHMTalkOnAway) && (!SHMTalkOnHome) && (!SHMTalkOnDisarm)) {
                defaultSpeechSHMAway = "Smart Home Monitor is now Armed in Away mode"
            }
            input name: "SHMTalkOnAway", type: "text", title: "Say this when Armed, Away:", required: false, defaultValue: defaultSpeechSHMAway
            input name: "SHMSpeechDeviceAway", type: parent?.state?.speechDeviceType, title: "Talk with these text-to-speech devices (overrides default)", multiple: true, required: false
            input name: "SHMDisableSwitch1", type: "capability.switch", title: "Disable when this switch is off", required: false, multiple: false
            if (parent?.state?.speechDeviceType == "capability.musicPlayer") {
            	input name: "SHMVolumeAway", type: "number", title: "Set volume to (overrides default):", required: false
            	input name: "SHMResumePlayAway", type: "bool", title: "Attempt to resume playing audio?", required: false, defaultValue: (parent?.settings?.resumePlay == false) ? false : true
                input name: "SHMVoiceAway", type: "enum", title: "Voice (overrides default):", options: parent?.state?.supportedVoices, required: false, submitOnChange: true
            }
        }
        section("Restrictions (Armed, Away)"){
            input name: "SHMModesAway", type: "mode", title: "Talk when in these mode(s) (overrides default)", multiple: true, required: false
            input name: "SHMStartTimeAway", type: "time", title: "Don't talk before (overrides default)", required: false, submitOnChange: true
            input name: "SHMEndTimeAway", type: "time", title: "Don't talk after (overrides default)", required: (!(settings.SHMStartTimeAway == null))
            input name: "SHMAwayDays1", type: "enum", title: "Restrict to these day(s)", required: false, options: ["Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"], multiple: true
        }
        section("Smart Home Monitor - Armed, Home"){
        	def defaultSpeechSHMHome = ""
            if ((!SHMTalkOnAway) && (!SHMTalkOnHome) && (!SHMTalkOnDisarm)) {
                defaultSpeechSHMHome = "Smart Home Monitor is now Armed in Home mode"
            }
            input name: "SHMTalkOnHome", type: "text", title: "Say this when Armed, Home:", required: false, defaultValue: defaultSpeechSHMHome
            input name: "SHMSpeechDeviceHome", type: parent?.state?.speechDeviceType, title: "Talk with these text-to-speech devices (overrides default)", multiple: true, required: false
            if (parent?.state?.speechDeviceType == "capability.musicPlayer") {
            	input name: "SHMVolumeHome", type: "number", title: "Set volume to (overrides default):", required: false
            	input name: "SHMResumePlayHome", type: "bool", title: "Attempt to resume playing audio?", required: false, defaultValue: (parent?.settings?.resumePlay == false) ? false : true
                input name: "SHMVoiceHome", type: "enum", title: "Voice (overrides default):", options: parent?.state?.supportedVoices, required: false, submitOnChange: true
            }
        }
        section("Restrictions (Armed, Home)"){
            input name: "SHMModesHome", type: "mode", title: "Talk when in these mode(s) (overrides default)", multiple: true, required: false
            input name: "SHMStartTimeHome", type: "time", title: "Don't talk before (overrides default)", required: false, submitOnChange: true
            input name: "SHMEndTimeHome", type: "time", title: "Don't talk after (overrides default)", required: (!(settings.SHMStartTimeHome == null))
            input name: "SHMHomeDays1", type: "enum", title: "Restrict to these day(s)", required: false, options: ["Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"], multiple: true
        }
        section("Smart Home Monitor - Disarmed"){
        	def defaultSpeechSHMDisarm = ""
            if ((!SHMTalkOnAway) && (!SHMTalkOnHome) && (!SHMTalkOnDisarm)) {
                defaultSpeechSHMDisarm = "Smart Home Monitor is now Disarmed"
            }
            input name: "SHMTalkOnDisarm", type: "text", title: "Say this when Disarmed:", required: false, defaultValue: defaultSpeechSHMDisarm
            input name: "SHMSpeechDeviceDisarm", type: parent?.state?.speechDeviceType, title: "Talk with these text-to-speech devices (overrides default)", multiple: true, required: false
            if (parent?.state?.speechDeviceType == "capability.musicPlayer") {
            	input name: "SHMVolumeDisarm", type: "number", title: "Set volume to (overrides default):", required: false
                input name: "SHMResumePlayDisarm", type: "bool", title: "Attempt to resume playing audio?", required: false, defaultValue: (parent?.settings?.resumePlay == false) ? false : true
                input name: "SHMVoiceDisarm", type: "enum", title: "Voice (overrides default):", options: parent?.state?.supportedVoices, required: false, submitOnChange: true
            }
        }
        section("Restrictions (Disarmed)"){
            input name: "SHMModesDisarm", type: "mode", title: "Talk when in these mode(s) (overrides default)", multiple: true, required: false
            input name: "SHMStartTimeDisarm", type: "time", title: "Don't talk before (overrides default)", required: false, submitOnChange: true
            input name: "SHMEndTimeDisarm", type: "time", title: "Don't talk after (overrides default)", required: (!(settings.SHMStartTimeDisarm == null))
            input name: "SHMDisarmDays1", type: "enum", title: "Restrict to these day(s)", required: false, options: ["Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"], multiple: true
        }
        section("Help"){
            href "pageHelpPhraseTokens", title:"Phrase Tokens", description:"Tap for a list of phrase tokens"
        }
    }
//End pageConfigSHM()
}

def pageConfigTime(){
    dynamicPage(name: "pageConfigTime", title: "Configure talk at specific time", install: false, uninstall: false) {
        section("Schedule 1"){
            input name: "timeSlotDays1", type: "enum", title: "Which day(s)", required: false, options: ["Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"], multiple: true
            input name: "timeSlotTime1", type: "time", title: "Time of day", required: false
            input name: "timeSlotOnTime1", type: "text", title: "Say on schedule:", required: false
            input name: "timeSlotPersonality1", type: "enum", title: "Allow Personality (overrides default)?:", required: false, options: ["Yes", "No"]
            input name: "timeSlotSpeechDevice1", type: parent?.state?.speechDeviceType, title: "Talk with these text-to-speech devices (overrides default)", multiple: true, required: false
            if (parent?.state?.speechDeviceType == "capability.musicPlayer") {
            	input name: "timeSlotVolume1", type: "number", title: "Set volume to (overrides default):", required: false
                input name: "timeSlotResumePlay1", type: "bool", title: "Attempt to resume playing audio?", required: false, defaultValue: (parent?.settings?.resumePlay == false) ? false : true
                input name: "timeSlotVoice1", type: "enum", title: "Voice (overrides default):", options: parent?.state?.supportedVoices, required: false, submitOnChange: true
            }
        }
        section("Restrictions (Schedule 1)"){
            input name: "timeSlotModes1", type: "mode", title: "Talk when in these mode(s) (overrides default)", multiple: true, required: false
            input name: "timeSlotDisableSwitch1", type: "capability.switch", title: "Disable when this switch is off", required: false, multiple: false
        }
		section("Schedule 2"){
            input name: "timeSlotDays2", type: "enum", title: "Which day(s)", required: false, options: ["Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"], multiple: true
            input name: "timeSlotTime2", type: "time", title: "Time of day", required: false
            input name: "timeSlotOnTime2", type: "text", title: "Say on schedule:", required: false
            input name: "timeSlotPersonality2", type: "enum", title: "Allow Personality (overrides default)?:", required: false, options: ["Yes", "No"]
            input name: "timeSlotSpeechDevice2", type: parent?.state?.speechDeviceType, title: "Talk with these text-to-speech devices (overrides default)", multiple: true, required: false
            if (parent?.state?.speechDeviceType == "capability.musicPlayer") {
            	input name: "timeSlotVolume2", type: "number", title: "Set volume to (overrides default):", required: false
                input name: "timeSlotResumePlay2", type: "bool", title: "Attempt to resume playing audio?", required: false, defaultValue: (parent?.settings?.resumePlay == false) ? false : true
                input name: "timeSlotVoice2", type: "enum", title: "Voice (overrides default):", options: parent?.state?.supportedVoices, required: false, submitOnChange: true
            }
        }
        section("Restrictions (Schedule 2)"){
            input name: "timeSlotModes2", type: "mode", title: "Talk when in these mode(s) (overrides default)", multiple: true, required: false
            input name: "timeSlotDisableSwitch2", type: "capability.switch", title: "Disable when this switch is off", required: false, multiple: false
        }
		section("Schedule 3"){
            input name: "timeSlotDays3", type: "enum", title: "Which day(s)", required: false, options: ["Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"], multiple: true
            input name: "timeSlotTime3", type: "time", title: "Time of day", required: false
            input name: "timeSlotOnTime3", type: "text", title: "Say on schedule:", required: false
            input name: "timeSlotPersonality3", type: "enum", title: "Allow Personality (overrides default)?:", required: false, options: ["Yes", "No"]
            input name: "timeSlotSpeechDevice3", type: parent?.state?.speechDeviceType, title: "Talk with these text-to-speech devices (overrides default)", multiple: true, required: false
            if (parent?.state?.speechDeviceType == "capability.musicPlayer") {
            	input name: "timeSlotVolume3", type: "number", title: "Set volume to (overrides default):", required: false
                input name: "timeSlotResumePlay3", type: "bool", title: "Attempt to resume playing audio?", required: false, defaultValue: (parent?.settings?.resumePlay == false) ? false : true
                input name: "timeSlotVoice3", type: "enum", title: "Voice (overrides default):", options: parent?.state?.supportedVoices, required: false, submitOnChange: true
            }
        }
        section("Restrictions (Schedule 3)"){
            input name: "timeSlotModes3", type: "mode", title: "Talk when in these mode(s) (overrides default)", multiple: true, required: false
            input name: "timeSlotDisableSwitch3", type: "capability.switch", title: "Disable when this switch is off", required: false, multiple: false
        }
        section("Help"){
            href "pageHelpPhraseTokens", title:"Phrase Tokens", description:"Tap for a list of phrase tokens"
        }
    }
}

def pageConfigPowerMeter(){
    dynamicPage(name: "pageConfigPowerMeter", title: "Configure talk on power consumption", install: false, uninstall: false) {
        section(){
            def defaultSpeechPowerMeterRise1 = ""
            def defaultSpeechPowerMeterNormal1 = ""
            def defaultSpeechPowerMeterFall1 = ""
            state.powerMeterState = ""
            if (!powerMeterDeviceGroup1) {
                defaultSpeechPowerMeterRise1 = "%devicename% power level is high at %value% watts"
                defaultSpeechPowerMeterNormal1 = "%devicename% power level is within normal range"
                defaultSpeechPowerMeterFall1 = "%devicename% power level is low at %value% watts"
            }
            input name: "powerMeterDeviceGroup1", type: "capability.powerMeter", title: "Power Meter(s)", required: false, multiple: true
            input name: "powerMeterTalkOnRise1", type: "text", title: "Say this if power rises above threshold:", required: false, defaultValue: defaultSpeechPowerMeterRise1, submitOnChange: true
            input name: "powerMeterTalkOnFall1", type: "text", title: "Say this if power falls below threshold:", required: false, defaultValue: defaultSpeechPowerMeterFall1, submitOnChange: true
            input name: "powerMeterTalkOnNormal1", type: "text", title: "Say this if power returns to normal:", required: false, defaultValue: defaultSpeechPowerMeterNormal1, submitOnChange: false
            input name: "powerMeterTalkOnRiseThold1", type: "number", title: "High energy usage threshold (watts):", required: powerMeterTalkOnRise1, defaultValue: defaultSpeechpowerMeterRise1
            input name: "powerMeterTalkOnFallThold1", type: "number", title: "Low energy usage threshold (watts):", required: powerMeterTalkOnFall1, defaultValue: defaultSpeechpowerMeterFall1
            input name: "powerMeterPersonality1", type: "enum", title: "Allow Personality (overrides default)?:", required: false, options: ["Yes", "No"]
            input name: "powerMeterSpeechDevice1", type: parent?.state?.speechDeviceType, title: "Talk with these text-to-speech devices (overrides default)", multiple: true, required: false
            if (parent?.state?.speechDeviceType == "capability.musicPlayer") {
            	input name: "powerMeterVolume1", type: "number", title: "Set volume to (overrides default):", required: false
                input name: "powerMeterResumePlay1", type: "bool", title: "Attempt to resume playing audio?", required: false, defaultValue: (parent?.settings?.resumePlay == false) ? false : true
                input name: "powerMeterVoice1", type: "enum", title: "Voice (overrides default):", options: parent?.state?.supportedVoices, required: false, submitOnChange: true
            }
        }
        section("Restrictions"){
            input name: "powerMeterModes1", type: "mode", title: "Talk when in these mode(s) (overrides default)", multiple: true, required: false
            input name: "powerMeterStartTime1", type: "time", title: "Don't talk before (overrides default)", required: false, submitOnChange: true
            input name: "powerMeterEndTime1", type: "time", title: "Don't talk after (overrides default)", required: (!(settings.waterStartTime1 == null))
            input name: "powerMeterDays1", type: "enum", title: "Restrict to these day(s)", required: false, options: ["Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"], multiple: true
            input name: "powerMeterDisableSwitch1", type: "capability.switch", title: "Disable when this switch is off", required: false, multiple: false
        }
        section("Help"){
            href "pageHelpPhraseTokens", title:"Phrase Tokens", description:"Tap for a list of phrase tokens"
        }
    }
//End pageConfigpowerMeter()
}

def pageConfigRoutine(){
    dynamicPage(name: "pageConfigRoutine", title: "Configure talk when routine runs", install: false, uninstall: false) {
        section(){
            def defaultSpeechRoutine1 = ""
            if (!routineDeviceGroup1) {
                defaultSpeechRoutine1 = "%routine% routine has been run"
            }
            def routines = location.helloHome?.getPhrases()*.label
            if (routines) {
            	// sort them alphabetically
            	routines.sort()
            }
            input name: "routineDeviceGroup1", type: "enum", title: "Routine", required: true, multiple: true, options: routines
            input name: "routineTalkOnRun1", type: "text", title: "Say this routine runs:", required: false, defaultValue: defaultSpeechRoutine1
            input name: "routinePersonality1", type: "enum", title: "Allow Personality (overrides default)?:", required: false, options: ["Yes", "No"]
            input name: "routineSpeechDevice1", type: parent?.state?.speechDeviceType, title: "Talk with these text-to-speech devices (overrides default)", multiple: true, required: false
            if (parent?.state?.speechDeviceType == "capability.musicPlayer") {
            	input name: "routineVolume1", type: "number", title: "Set volume to (overrides default):", required: false
                input name: "routineResumePlay1", type: "bool", title: "Attempt to resume playing audio?", required: false, defaultValue: (parent?.settings?.resumePlay == false) ? false : true
                input name: "routineVoice1", type: "enum", title: "Voice (overrides default):", options: parent?.state?.supportedVoices, required: false, submitOnChange: true
            }
        }
        section("Restrictions"){
            input name: "routineModes1", type: "mode", title: "Talk when in these mode(s) (overrides default)", multiple: true, required: false
            input name: "routineStartTime1", type: "time", title: "Don't talk before (overrides default)", required: false, submitOnChange: true
            input name: "routineEndTime1", type: "time", title: "Don't talk after (overrides default)", required: (!(settings.buttonStartTime1 == null))
            input name: "routineDays1", type: "enum", title: "Restrict to these day(s)", required: false, options: ["Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"], multiple: true
            input name: "routineDisableSwitch1", type: "capability.switch", title: "Disable when this switch is off", required: false, multiple: false
        }
        section("Help"){
            href "pageHelpPhraseTokens", title:"Phrase Tokens", description:"Tap for a list of phrase tokens"
        }
    }
//End pageConfigRoutine()
}


def pageHelpPhraseTokens(){
	//KEEP IN SYNC WITH PARENT!
    dynamicPage(name: "pageHelpPhraseTokens", title: "Available Phrase Tokens", install: false, uninstall:false){
       section("The following tokens can be used in your event phrases and will be replaced as listed:"){
       	   def AvailTokens = ""
           AvailTokens += "%askalexa% = Send phrase to AskAlexa SmartApp's message queue\n\n"
           AvailTokens += "%groupname% = Name that you gave for the event group\n\n"
           AvailTokens += "%date% = Current date; January 01 2018\n\n"
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
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

def initialize() {
    if (state.groupEnabled == "true" || state.groupEnabled == null) {
    	app.updateLabel("${app.label.replace("(disabled)","")}")
        initSchedule()
        initSubscribe()
        sendNotificationEvent("${app.label.replace(" ","").toUpperCase()}: Settings activated")
        state.lastMode = location.mode
        parent.setLastMode(location.mode)
    }
    LOGTRACE("Initialized (Parent Version: ${parent?.state?.appversion}; Child Version: ${state.appversion}; Group Enabled: ${settings.groupEnabled})")
//End initialize()
}
def updated() {
    state.groupEnabled = settings.groupEnabled
	setAppVersion()
	LOGTRACE("Updating settings (Parent Version: ${parent?.state?.appversion}; Child Version: ${state.appversion}; Group Enabled: ${state.groupEnabled})")
    state.installed = true
    unschedule()
    unsubscribe()
    if (state.groupEnabled == "true" || state.groupEnabled == null) { initialize() } else { app.updateLabel("${app.label} (disabled)") }
}
def installed() {
	setAppVersion()
	LOGTRACE("Installed")
}

def initSubscribe(){
    //NOTICE: Only call from initialize()!
    LOGDEBUG ("BEGIN initSubscribe()", true)
    //Subscribe Motions
    if (motionDeviceGroup1) { subscribe(motionDeviceGroup1, "motion", onMotion1Event) }
    //Subscribe Switches
    if (switchDeviceGroup1) { subscribe(switchDeviceGroup1, "switch", onSwitch1Event) }
    //Subscribe Presence
    if (presDeviceGroup1) { subscribe(presDeviceGroup1, "presence", onPresence1Event) }
    //Subscribe Lock
    if (lockDeviceGroup1) { subscribe(lockDeviceGroup1, "lock", onLock1Event) }
    //Subscribe Contact
    if (contactDeviceGroup1) { subscribe(contactDeviceGroup1, "contact", onContact1Event) }
    //Subscribe Thermostat
    if (thermostatDeviceGroup1) { subscribe(thermostatDeviceGroup1, "thermostatOperatingState", onThermostat1Event) }
    //Subscribe Acceleration
    if (accelerationDeviceGroup1) { subscribe(accelerationDeviceGroup1, "acceleration", onAcceleration1Event) }
    //Subscribe Water
    if (waterDeviceGroup1) { subscribe(waterDeviceGroup1, "water", onWater1Event) }
    //Subscribe Smoke
    if (smokeDeviceGroup1) { subscribe(smokeDeviceGroup1, "smoke", onSmoke1Event) }
    //Subscribe Button
    if (buttonDeviceGroup1) { subscribe(buttonDeviceGroup1, "button", onButton1Event) }
    //Subscribe SHM
    if (SHMTalkOnAway || SHMTalkOnHome || SHMTalkOnDisarm) { subscribe(location, "alarmSystemStatus", onSHMEvent) }
    //Subscribe PowerMeter
    if (powerMeterDeviceGroup1) { subscribe(powerMeterDeviceGroup1, "power", onPowerMeter1Event) }
    //Subscribe Routine
    if (routineDeviceGroup1) { subscribe(location, "routineExecuted", onRoutineEvent) }
    //Subscribe Mode
    if (modePhraseGroup1) { subscribe(location, onModeChangeEvent) }
    
    LOGDEBUG ("END initSubscribe()", true)
}

def initSchedule(){
    LOGDEBUG ("BEGIN initSchedule()", true)
    //Subscribe Schedule
    if (timeSlotTime1) { schedule(timeSlotTime1, onSchedule1Event) }
    if (timeSlotTime2) { schedule(timeSlotTime2, onSchedule2Event) }
    if (timeSlotTime3) { schedule(timeSlotTime3, onSchedule3Event) }
    LOGDEBUG ("END initSchedule()", true)
}
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//// PROCESS RESTRICTIONS

def processRestrictions(devicetype,index){
	def allowed = true
    if (!(processDisableSwitch(devicetype,index))){
    	LOGDEBUG("RESTRICTION: Disable switch is set to disable", true)
        allowed = false
    }
	if (!(timeAllowed(devicetype,index))) {
    	LOGDEBUG("RESTRICTION: Remain silent in current time period", true)
        allowed = false
    }
    if (!(devicetype == "mode")) {
    	if (!(modeAllowed(devicetype,index))) {
    		LOGDEBUG("RESTRICTION: Remain silent while in mode ${location.mode}", true)
        	allowed = false
    	}
    }
    if (!(dayAllowed(devicetype,index))) {
    	LOGDEBUG("RESTRICTION: Remain silent today", true)
        allowed = false
    }
    //if (!(processCountRestriction(devicetype,index))){
    //}
    return allowed
}

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
       case "switch":
            if (index == 1 && (!(settings.switchStartTime1 == null))) {
                    if (timeOfDayIsBetween(settings.switchStartTime1, settings.switchEndTime1, now, location.timeZone)) { return true } else { return false }
            }
       case "presence":
            if (index == 1 && (!(settings.presenceStartTime1 == null))) {
                if (timeOfDayIsBetween(settings.presenceStartTime1, settings.presenceEndTime1, now, location.timeZone)) { return true } else { return false }
            }
       case "lock":
            if (index == 1 && (!(settings.lockStartTime1 == null))) {
                if (timeOfDayIsBetween(settings.lockStartTime1, settings.lockEndTime1, now, location.timeZone)) { return true } else { return false }
            }
       case "contact":
            if (index == 1 && (!(settings.contactStartTime1 == null))) {
                if (timeOfDayIsBetween(settings.contactStartTime1, settings.contactEndTime1, now, location.timeZone)) { return true } else { return false }
            }
       case "thermostat":
            if (index == 1 && (!(settings.thermostatStartTime1 == null))) {
                if (timeOfDayIsBetween(settings.thermostatStartTime1, settings.thermostatEndTime1, now, location.timeZone)) { return true } else { return false }
            }
       case "acceleration":
            if (index == 1 && (!(settings.accelerationStartTime1 == null))) {
                if (timeOfDayIsBetween(settings.accelerationStartTime1, settings.accelerationEndTime1, now, location.timeZone)) { return true } else { return false }
            }
       case "water":
            if (index == 1 && (!(settings.waterStartTime1 == null))) {
                if (timeOfDayIsBetween(settings.waterStartTime1, settings.waterEndTime1, now, location.timeZone)) { return true } else { return false }
            }
       case "smoke":
            if (index == 1 && (!(settings.smokeStartTime1 == null))) {
                if (timeOfDayIsBetween(settings.smokeStartTime1, settings.smokeEndTime1, now, location.timeZone)) { return true } else { return false }
            }
       case "button":
            if (index == 1 && (!(settings.buttonStartTime1 == null))) {
                if (timeOfDayIsBetween(settings.buttonStartTime1, settings.buttonEndTime1, now, location.timeZone)) { return true } else { return false }
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
        case "powerMeter":
        	if (index == 1 && (!(settings.powerMeterStartTime1 == null))) {
            	if (timeOfDayIsBetween(settings.powerMeterStartTime1, settings.powerMeterEndTime1, now, location.timeZone)) { return true } else { return false }
            }
        case "routine":
        	if (index == 1 && (!(settings.routineStartTime1 == null))) {
            	if (timeOfDayIsBetween(settings.routineStartTime1, settings.routineEndTime1, now, location.timeZone)) { return true } else { return false }
            }
    }
    
    //No overrides have returned True, process Default
    if (parent?.settings?.defaultStartTime == null) { 
    	return true 
    } else {
        if (timeOfDayIsBetween(parent?.settings?.defaultStartTime, parent?.settings?.defaultEndTime, now, location.timeZone)) { return true } else { return false }
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
                    return (parent?.settings?.speechModesDefault.contains(location.mode)) //True if we are in an allowed Default mode, False if not
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
                    return (parent?.settings?.speechModesDefault.contains(location.mode)) //True if we are in an allowed Default mode, False if not
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
                    return (parent?.settings?.speechModesDefault.contains(location.mode)) //True if we are in an allowed Default mode, False if not
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
                    return (parent?.settings?.speechModesDefault.contains(location.mode)) //True if we are in an allowed Default mode, False if not
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
                    return (parent?.settings?.speechModesDefault.contains(location.mode)) //True if we are in an allowed Default mode, False if not
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
                    return (parent?.settings?.speechModesDefault.contains(location.mode)) //True if we are in an allowed Default mode, False if not
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
                    return (parent?.settings?.speechModesDefault.contains(location.mode)) //True if we are in an allowed Default mode, False if not
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
                    return (parent?.settings?.speechModesDefault.contains(location.mode)) //True if we are in an allowed Default mode, False if not
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
                    return (parent?.settings?.speechModesDefault.contains(location.mode)) //True if we are in an allowed Default mode, False if not
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
                    return (parent?.settings?.speechModesDefault.contains(location.mode)) //True if we are in an allowed Default mode, False if not
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
                    return (parent?.settings?.speechModesDefault.contains(location.mode)) //True if we are in an allowed Default mode, False if not
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
                    return (parent?.settings?.speechModesDefault.contains(location.mode)) //True if we are in an allowed Default mode, False if not
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
                    return (parent?.settings?.speechModesDefault.contains(location.mode)) //True if we are in an allowed Default mode, False if not
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
                    return (parent?.settings?.speechModesDefault.contains(location.mode)) //True if we are in an allowed Default mode, False if not
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
                    return (parent?.settings?.speechModesDefault.contains(location.mode)) //True if we are in an allowed Default mode, False if not
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
                    return (parent?.settings?.speechModesDefault.contains(location.mode)) //True if we are in an allowed Default mode, False if not
                }
            }
        //End: case "timeSlot"
        case "powerMeter":
            if (index == 1) {
                //Energy Meter Group 1
                if (settings.powerMeterModes1) {
                    if (settings.powerMeterModes1.contains(location.mode)) {
                        //Custom mode for this event is in use and we are in one of those modes
                        return true
                    } else {
                        //Custom mode for this event is in use and we are not in one of those modes
                        return false
                    }
                } else {
                    return (parent?.settings?.speechModesDefault.contains(location.mode)) //True if we are in an allowed Default mode, False if not
                }
            }
        //End: case "powerMeter"
        case "routine":
            if (index == 1) {
                //Routine Group 1
                if (settings.routineModes1) {
                    if (settings.routineModes1.contains(location.mode)) {
                        //Custom mode for this event is in use and we are in one of those modes
                        return true
                    } else {
                        //Custom mode for this event is in use and we are not in one of those modes
                        return false
                    }
                } else {
                    return (parent?.settings?.speechModesDefault.contains(location.mode)) //True if we are in an allowed Default mode, False if not
                }
            }
        //End: case "routine"
    } //End: switch (devicetype)
}

def dayAllowed(devicetype,index){
	def allowedDays = []
	switch(devicetype) {
    	case "timeSlot":
        	if (index == 1){
            	allowedDays = settings?.timeSlotDays1
                return processDayRestriction(allowedDays)
        	}
        	if (index == 2){
            	allowedDays = settings?.timeSlotDays2
                return processDayRestriction(allowedDays)
        	}
        	if (index == 3){
            	allowedDays = settings?.timeSlotDays3
                return processDayRestriction(allowedDays)
        	}
        case "motion":
        	if (index == 1){
            	allowedDays = settings?.motionDays1
                return processDayRestriction(allowedDays)
        	}
        case "switch":
        	if (index == 1){
            	allowedDays = settings?.switchDays1
                return processDayRestriction(allowedDays)
        	}
        case "presence":
        	if (index == 1){
            	allowedDays = settings?.presDays1
                return processDayRestriction(allowedDays)
        	}
        case "lock":
        	if (index == 1){
            	allowedDays = settings?.lockDays1
                return processDayRestriction(allowedDays)
        	}
        case "contact":
        	if (index == 1){
            	allowedDays = settings?.contactDays1
                return processDayRestriction(allowedDays)
        	}
        case "mode":
        	if (index == 1){
            	allowedDays = settings?.modeDays1
                return processDayRestriction(allowedDays)
        	}
        case "thermostat":
        	if (index == 1){
            	allowedDays = settings?.thermostatDays1
                return processDayRestriction(allowedDays)
        	}
        case "acceleration":
        	if (index == 1){
            	allowedDays = settings?.accelerationDays1
                return processDayRestriction(allowedDays)
        	}
        case "water":
        	if (index == 1){
            	allowedDays = settings?.waterDays1
                return processDayRestriction(allowedDays)
        	}
        case "smoke":
        	if (index == 1){
            	allowedDays = settings?.smokeDays1
                return processDayRestriction(allowedDays)
        	}
        case "button":
        	if (index == 1){
            	allowedDays = settings?.buttonDays1
                return processDayRestriction(allowedDays)
        	}
        case "SHM":
        	if (index == 1){
            	allowedDays = settings?.SHMAwayDays1
                return processDayRestriction(allowedDays)
        	}
            if (index == 2){
            	allowedDays = settings?.SHMHomeDays1
                return processDayRestriction(allowedDays)
        	}
            if (index == 3){
            	allowedDays = settings?.SHMDisarmDays1
                return processDayRestriction(allowedDays)
        	}
		case "powerMeter":
        	if (index == 1){
            	allowedDays = settings?.powerMeterDays1
                return processDayRestriction(allowedDays)
        	}
        case "routine":
        	if (index == 1){
            	allowedDays = settings?.routineDays1
                return processDayRestriction(allowedDays)
        	}
    }
}

def processDayRestriction(allowedDays){
    def todayStr = (new Date().format('EEEE', location.timeZone))
    //LOGDEBUG("Today=${todayStr}, Allowed:${allowedDays}", false)
    if (allowedDays == null || allowedDays == ""){
        return true
    }
    if (allowedDays.contains(todayStr)) {
        return true
    } else {
        return false
    }
}

def processCountRestriction(devicetype, index) {
// IN DEVELOPMENT 3/9/2018
		def maxCount = 0
        def countUnit = "None"
        switch(devicetype) {
        case "motion":
        	if (index == 1){
            	maxCount = settings?.motionCount1
                countUnit = settings?.motionCountUnit1
                //
                //switch(countUnit){
                //	case "Minute":
                //    	lastActive = getLastActivity()
                //    case "Hour": 
                //    case "Day": {}
                //}
                //return processDayRestriction(allowedDays)
                //
                return true // Return allow by default during initial development
        	}
    }
}

def processDisableSwitch(devicetype, index) {
	switch(devicetype){
    	case "timeSlot":
        	if (index == 1){
            	if (settings?.timeSlotDisableSwitch1?.currentSwitch == "on" || settings?.timeSlotDisableSwitch1 == null) { return true } else { return false }	
            }
            if (index == 2){
            	if (settings?.timeSlotDisableSwitch2?.currentSwitch == "on" || settings?.timeSlotDisableSwitch2 == null) { return true } else { return false }	
            }
            if (index == 3){
            	if (settings?.timeSlotDisableSwitch3?.currentSwitch == "on" || settings?.timeSlotDisableSwitch3 == null) { return true } else { return false }	
            }
    	case "motion":
        	if (index == 1){
            	if (settings?.motionDisableSwitch1?.currentSwitch == "on" || settings?.motionDisableSwitch1 == null) { return true } else { return false }
            }
       case "switch":
        	if (index == 1){
            	if (settings?.switchDisableSwitch1?.currentSwitch == "on" || settings?.switchDisableSwitch1 == null) { return true } else { return false }
            }
       case "presence":
        	if (index == 1){
            	if (settings?.presenceDisableSwitch1?.currentSwitch == "on" || settings?.presenceDisableSwitch1 == null) { return true } else { return false }
            }
       case "lock":
        	if (index == 1){
            	if (settings?.lockDisableSwitch1?.currentSwitch == "on" || settings?.lockDisableSwitch1 == null) { return true } else { return false }
            }
       case "contact":
        	if (index == 1){
            	if (settings?.contactDisableSwitch1?.currentSwitch == "on" || settings?.contactDisableSwitch1 == null) { return true } else { return false }
            }
       case "mode":
        	if (index == 1){
            	if (settings?.modeDisableSwitch1?.currentSwitch == "on" || settings?.modeDisableSwitch1 == null) { return true } else { return false }
            }
       case "acceleration":
        	if (index == 1){
            	if (settings?.accelerationDisableSwitch1?.currentSwitch == "on" || settings?.accelerationDisableSwitch1 == null) { return true } else { return false }
            }
       case "thermostat":
        	if (index == 1){
            	if (settings?.thermostatDisableSwitch1?.currentSwitch == "on" || settings?.thermostatDisableSwitch1 == null) { return true } else { return false }
            }
       case "water":
        	if (index == 1){
            	if (settings?.waterDisableSwitch1?.currentSwitch == "on" || settings?.waterDisableSwitch1 == null) { return true } else { return false }
            }
      case "smoke":
        	if (index == 1){
            	if (settings?.smokeDisableSwitch1?.currentSwitch == "on" || settings?.smokeDisableSwitch1 == null) { return true } else { return false }
            }
       case "button":
        	if (index == 1){
            	if (settings?.buttonDisableSwitch1?.currentSwitch == "on" || settings?.buttonDisableSwitch1 == null) { return true } else { return false }
            }
       case "SHM":
           	if (settings?.SHMDisableSwitch1?.currentSwitch == "on" || settings?.SHMDisableSwitch1 == null) { return true } else { return false }
       case "powerMeter":
        	if (index == 1){
            	if (settings?.powerMeterDisableSwitch1?.currentSwitch == "on" || settings?.powerMeterDisableSwitch1 == null) { return true } else { return false }
            }
       case "routine":
        	if (index == 1){
            	if (settings?.routineDisableSwitch1?.currentSwitch == "on" || settings?.routineDisableSwitch1 == null) { return true } else { return false }
            }
    }
}

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//// General Functions

def getTimeFromDateString(inputtime, includeAmPm){
    //I couldn't find the way to do this in ST / Groovy, so I made my own function
    //Obtains the time from a supplied specifically formatted date string (ie: from a preference of type "time")
    //LOGDEBUG "InputTime: ${inputtime}", true"$
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
    //LOGDEBUG "OutputTime: ${outputtime}", true"$
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
    //LOGDEBUG("myRunIn(${delay_s},${func})", true)

    if (delay_s > 0) {
        def tms = now() + (delay_s * 1000)
        def date = new Date(tms)
        runOnce(date, func)
        //LOGDEBUG("'${func}' scheduled to run at ${date}", true)
    }
}
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
// HANDLE EVENTS
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
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
	def resume = ""; resume = parent?.settings?.resumePlay
    if (resume == "" || resume == null) { resume = false }
    def personality = ""; personality = parent?.settings?.personalityMode; if (personality == "" || personality == null) { personality = false }
    def timeNow = getTimeFromCalendar(false, true)
    //def personality = false; personality = parent?.settings?.personalityMode
    //if (personality == "" || personality == null) { personality = false }
    def myVolume = -1
    def myVoice = ""
    if (index == 1) {myVoice = getMyVoice(settings?.timeSlotVoice1)}
    if (index == 2) {myVoice = getMyVoice(settings?.timeSlotVoice2)}
    if (index == 3) {myVoice = getMyVoice(settings?.timeSlotVoice3)}
    LOGDEBUG("(onScheduledEvent): ${timeNow}, ${index}, ${myVoice}", true)
    //Check Restrictions
    if (!(processRestrictions("timeSlot",index))){ return }
	if (parent?.state?.speechDeviceType == "capability.musicPlayer") {
		if (index == 1){if (!settings?.timeSlotResumePlay1 == null) { resume = settings.timeSlotResumePlay1 }}
		if (index == 2){if (!settings?.timeSlotResumePlay2 == null) { resume = settings.timeSlotResumePlay2 }}
		if (index == 3){if (!settings?.timeSlotResumePlay3 == null) { resume = settings.timeSlotResumePlay3 }}
	}
	if (resume == null) { resume = true } else { resume = false }
    if (index == 1 && settings?.timeSlotPersonality1 == "Yes") {personality = true}
    if (index == 1 && settings?.timeSlotPersonality1 == "No") {personality = false}
	if (index == 2 && settings?.timeSlotPersonality2 == "Yes") {personality = true}
    if (index == 2 && settings?.timeSlotPersonality2 == "No") {personality = false}
    if (index == 3 && settings?.timeSlotPersonality3 == "Yes") {personality = true}
    if (index == 3 && settings?.timeSlotPersonality3 == "No") {personality = false}  
	if (index == 1) { state.TalkPhrase = settings.timeSlotOnTime1; state.speechDevice = timeSlotSpeechDevice1; myVolume = getDesiredVolume(settings.timeSlotVolume1) }
	if (index == 2) { state.TalkPhrase = settings.timeSlotOnTime2; state.speechDevice = timeSlotSpeechDevice2; myVolume = getDesiredVolume(settings.timeSlotVolume2) }
	if (index == 3) { state.TalkPhrase = settings.timeSlotOnTime3; state.speechDevice = timeSlotSpeechDevice3; myVolume = getDesiredVolume(settings.timeSlotVolume3) }
	def customevent = [displayName: 'BigTalker:OnSchedule', name: 'OnSchedule', value: "${todayStr}@${timeNow}"]
	sendTalk(app.label,state.TalkPhrase, state.speechDevice, myVolume,resume, personality, myVoice, customevent)
	state.TalkPhrase = null
    state.speechDevice = null
}

//BEGIN HANDLE MOTIONS
def onMotion1Event(evt){
    processMotionEvent(1, evt)
}

def processMotionEvent(index, evt){
	def resume = ""; resume = parent?.settings?.resumePlay; if (resume == "") { resume = true }
    def personality = ""; personality = parent?.settings?.personalityMode; if (personality == "" || personality == null) { personality = false }
    def myVolume = -1
    def myVoice = getMyVoice(settings?.switchMotion1)
    LOGDEBUG("(onMotionEvent): ${evt.name}, ${index}, ${evt.value}, ${myVoice}", true)
    //Check Restrictions
    if (!(processRestrictions("motion",index))){ return }
    state.TalkPhrase = null
    state.speechDevice = null
	if (parent?.state?.speechDeviceType == "capability.musicPlayer") {
		if (index == 1) {
			if (!settings?.motionResumePlay1 == null) { resume = settings.motionResumePlay1 }
		}
        if (resume == null) { resume = true }
	} else { resume = false }
    if (settings?.motionPersonality1 == "Yes") {personality = true}
    if (settings?.motionPersonality1 == "No") {personality = false}
    if (evt.value == "active") {
        if (index == 1) { state.TalkPhrase = settings.motionTalkActive1; state.speechDevice = motionSpeechDevice1; myVolume = getDesiredVolume(settings.motionVolume1)}
        if (!(state?.TalkPhrase == null)) {sendTalk(app.label,state.TalkPhrase, state.speechDevice, myVolume, resume, personality, myVoice, evt)} else {LOGDEBUG("Not configured to speak for this event", true)}
    }
    if (evt.value == "inactive") {
        if (index == 1) { state.TalkPhrase = settings.motionTalkInactive1; state.speechDevice = motionSpeechDevice1; myVolume = getDesiredVolume(settings.motionVolume1)}
        if (!(state?.TalkPhrase == null)) {sendTalk(app.label,state.TalkPhrase, state.speechDevice, myVolume, resume, personality, myVoice, evt)} else {LOGDEBUG("Not configured to speak for this event", true)}
    }
    state.TalkPhrase = null
    state.speechDevice = null
}
//END HANDLE MOTIONS
//BEGIN HANDLE SWITCHES
def onSwitch1Event(evt){
    processSwitchEvent(1, evt)
}

def processSwitchEvent(index, evt){
	def resume = ""; resume = parent?.settings?.resumePlay; if (resume == "") { resume = true }
    def personality = ""; personality = parent?.settings?.personalityMode; if (personality == "" || personality == null) { personality = false }
    def myVolume = -1
    def myVoice = getMyVoice(settings?.switchVoice1)
    LOGDEBUG("(onSwitchEvent): ${evt.name}, ${index}, ${evt.value}, ${myVoice}", true)
    //Check Restrictions
    if (!(processRestrictions("switch",index))){ return }
    state.TalkPhrase = null
    state.speechDevice = null
	if (parent?.state?.speechDeviceType == "capability.musicPlayer") {
		if (index == 1) {
			if (!(settings?.switchResumePlay1 == null)) { resume = settings.switchResumePlay1 }
		}
        if (resume == null) { resume = true }
	} else { resume = false }
    if (settings?.switchPersonality1 == "Yes") {personality = true}
    if (settings?.switchPersonality1 == "No") {personality = false}
    if (evt.value == "on") {
        if (index == 1) { state.TalkPhrase = settings.switchTalkOn1; state.speechDevice = switchSpeechDevice1; myVolume = getDesiredVolume(settings.switchVolume1)}
        if (!(state?.TalkPhrase == null)) {sendTalk(app.label,state.TalkPhrase, state.speechDevice, myVolume, resume, personality, myVoice, evt)} else {LOGDEBUG("Not configured to speak for this event", true)}
    }
    if (evt.value == "off") {
        if (index == 1) { state.TalkPhrase = settings.switchTalkOff1; state.speechDevice = switchSpeechDevice1; myVolume = getDesiredVolume(settings.switchVolume1)}
        if (!(state?.TalkPhrase == null)) {sendTalk(app.label,state.TalkPhrase, state.speechDevice, myVolume, resume, personality, myVoice, evt)} else {LOGDEBUG("Not configured to speak for this event", true)}
    }
    state.TalkPhrase = null
    state.speechDevice = null
}
//END HANDLE SWITCHES
//BEGIN HANDLE PRESENCE
def onPresence1Event(evt){
    processPresenceEvent(1, evt)
}

def processPresenceEvent(index, evt){
	def resume = ""; resume = parent?.settings?.resumePlay; if (resume == "") { resume = true }
    def personality = ""; personality = parent?.settings?.personalityMode; if (personality == "" || personality == null) { personality = false }
    def myVolume = -1
    def myVoice = getMyVoice(settings?.presVoice1)
    LOGDEBUG("(onPresenceEvent): ${evt.name}, ${index}, ${evt.value}, ${myVoice}", true)
    //Check Restrictions
    if (!(processRestrictions("presence",index))){ return }
    state.TalkPhrase = null
    state.speechDevice = null
	if (parent?.state?.speechDeviceType == "capability.musicPlayer") {
		if (index == 1) {
			if (!settings?.presResumePlay1 == null) { resume = settings.presResumePlay1 }
		}
        if (resume == null) { resume = true }
	} else { resume = false }
    if (settings?.presPersonality1 == "Yes") {personality = true}
    if (settings?.presPersonality1 == "No") {personality = false}
    if (evt.value == "present") {
        if (index == 1) { state.TalkPhrase = settings.presTalkOnArrive1; state.speechDevice = presSpeechDevice1; myVolume = getDesiredVolume(settings.presVolume1)}
        if (!(state?.TalkPhrase == null)) {sendTalk(app.label,state.TalkPhrase, state.speechDevice, myVolume, resume, personality, myVoice, evt)} else {LOGDEBUG("Not configured to speak for this event", true)}
    }
    if (evt.value == "not present") {
        if (index == 1) { state.TalkPhrase = settings.presTalkOnLeave1; state.speechDevice = presSpeechDevice1; myVolume = getDesiredVolume(settings.presVolume1)}
        if (!(state?.TalkPhrase == null)) {sendTalk(app.label,state.TalkPhrase, state.speechDevice, myVolume, resume, personality, myVoice, evt)} else {LOGDEBUG("Not configured to speak for this event", true)}
    }
    state.TalkPhrase = null
    state.speechDevice = null
}
//END HANDLE PRESENCE

//BEGIN HANDLE LOCK
def onLock1Event(evt){
    LOGDEBUG("onLock1Event(evt) ${evt.value}", true)
    processLockEvent(1, evt)
}

def processLockEvent(index, evt){
	def resume = ""; resume = parent?.settings?.resumePlay; if (resume == "") { resume = true }
    def personality = ""; personality = parent?.settings?.personalityMode; if (personality == "" || personality == null) { personality = false }
    def myVolume = -1
    def myVoice = getMyVoice(settings?.lockVoice1)
    LOGDEBUG("(onLockEvent): ${evt.name}, ${index}, ${evt.value}, ${myVoice}", true)
    //Check Restrictions
    if (!(processRestrictions("lock",index))){ return }
    state.TalkPhrase = null
    state.speechDevice = null
	if (parent?.state?.speechDeviceType == "capability.musicPlayer") {
		if (index == 1) {
			if (!settings?.lockResumePlay1 == null) { resume = settings.lockResumePlay1 }
		}
        if (resume == null) { resume = true }
	} else { resume = false }
    if (settings?.lockPersonality1 == "Yes") {personality = true}
    if (settings?.lockPersonality1 == "No") {personality = false}
    if (evt.value == "locked") {
        if (index == 1) { state.TalkPhrase = settings.lockTalkOnLock1; state.speechDevice = lockSpeechDevice1; myVolume = getDesiredVolume(settings.lockVolume1)}
        if (!(state?.TalkPhrase == null)) {sendTalk(app.label,state.TalkPhrase, state.speechDevice, myVolume, resume, personality, myVoice, evt)} else {LOGDEBUG("Not configured to speak for this event", true)}
    }
    if (evt.value == "unlocked") {
        if (index == 1) { state.TalkPhrase = settings.lockTalkOnUnlock1; state.speechDevice = lockSpeechDevice1; myVolume = getDesiredVolume(settings.lockVolume1)}
        if (!(state?.TalkPhrase == null)) {sendTalk(app.label,state.TalkPhrase, state.speechDevice, myVolume, resume, personality, myVoice, evt)} else {LOGDEBUG("Not configured to speak for this event", true)}
    }
    state.TalkPhrase = null
    state.speechDevice = null
}
//END HANDLE LOCK

//BEGIN HANDLE CONTACT
def onContact1Event(evt){
    processContactEvent(1, evt)
}

def processContactEvent(index, evt){
	def resume = ""; resume = parent?.settings?.resumePlay; if (resume == "") { resume = true }
    def personality = ""; personality = parent?.settings?.personalityMode; if (personality == "" || personality == null) { personality = false }
    def myVolume = -1
    def myVoice = getMyVoice(settings?.contactVoice1)
    LOGDEBUG("(onContactEvent): ${evt.name}, ${index}, ${evt.value}, ${myVoice}", true)
    //Check Restrictions
    if (!(processRestrictions("contact",index))){ return }
    state.TalkPhrase = null
    state.speechDevice = null
	if (parent?.state?.speechDeviceType == "capability.musicPlayer") {
		if (index == 1) {
			if (!settings?.contactResumePlay1 == null) { resume = settings.contactResumePlay1 }
		}
        if (resume == null) { resume = true }
	} else { resume = false }
    if (settings?.contactPersonality1 == "Yes") {personality = true}
    if (settings?.contactPersonality1 == "No") {personality = false}
    if (evt.value == "open") {
        if (index == 1) { state.TalkPhrase = settings.contactTalkOnOpen1; state.speechDevice = contactSpeechDevice1; myVolume = getDesiredVolume(settings.contactVolume1)}
        if (!(state?.TalkPhrase == null)) {sendTalk(app.label,state.TalkPhrase, state.speechDevice, myVolume, resume, personality, myVoice, evt)} else {LOGDEBUG("Not configured to speak for this event", true)}
    }
    if (evt.value == "closed") {
        if (index == 1) { state.TalkPhrase = settings.contactTalkOnClose1; state.speechDevice = contactSpeechDevice1; myVolume = getDesiredVolume(settings.contactVolume1)}
        if (!(state?.TalkPhrase == null)) {sendTalk(app.label,state.TalkPhrase, state.speechDevice, myVolume, resume, personality, myVoice, evt)} else {LOGDEBUG("Not configured to speak for this event", true)}
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
	def resume = ""; resume = parent?.settings?.resumePlay; if (resume == "") { resume = true }
    def personality = ""; personality = parent?.settings?.personalityMode; if (personality == "" || personality == null) { personality = false }
    def myVolume = -1
    def myVoice = getMyVoice(settings?.modeVoice1)
    LOGDEBUG("(onModeEvent): Last Mode: ${state.lastMode}, New Mode: ${location.mode}, ${myVoice}", true)
    //Check Restrictions
    if (!(processRestrictions("mode",index))){ return }
	if (parent?.state?.speechDeviceType == "capability.musicPlayer") {
		if (index == 1) {
			if (!settings?.modePhraseResumePlay1 == null) { resume = settings.modePhraseResumePlay1 }
		}
        if (resume == null) { resume = true }
	} else { resume = false }
    if (settings?.modePersonality1 == "Yes") {personality = true}
    if (settings?.modePersonality1 == "No") {personality = false}
    if (settings.modePhraseGroup1.contains(location.mode)){
        if (!settings.modeExcludePhraseGroup1 == null){
            //settings.modeExcluePhraseGroup1 is not empty
            if (!(settings.modeExcludePhraseGroup1.contains(state.lastMode))) {
                //If we are not coming from an exclude mode, Talk.
                state.TalkPhrase = null
                state.speechDevice = null
                state.TalkPhrase = settings.TalkOnModeChange1; state.speechDevice = modePhraseSpeechDevice1; myVolume = getDesiredVolume(settings.modePhraseVolume1)
                if (!(state?.TalkPhrase == null)) {sendTalk(app.label,state.TalkPhrase, state.speechDevice, myVolume, resume, personality, myVoice, evt)} else {LOGDEBUG("Not configured to speak for this event", true)}
                state.TalkPhrase = null
                state.speechDevice = null
            } else {
                LOGDEBUG("Mode change silent due to exclusion configuration (${state.lastMode} >> ${location.mode})", true)
            }
        } else {
            //settings.modeExcluePhraseGroup1 is empty, no exclusions, Talk.
            state.TalkPhrase = null
            state.speechDevice = null
            state.TalkPhrase = settings.TalkOnModeChange1; state.speechDevice = modePhraseSpeechDevice1; myVolume = getDesiredVolume(settings.modePhraseVolume1)
            if (!(state?.TalkPhrase == null)) {sendTalk(app.label,state.TalkPhrase, state.speechDevice, myVolume, resume, personality, myVoice, evt)} else {LOGDEBUG("Not configured to speak for this event", true)}
            state.TalkPhrase = null
            state.speechDevice = null
        }
    }
    state.lastMode = location.mode
    parent.setLastMode(location.mode)
}
//END MODE CHANGE

//BEGIN HANDLE THERMOSTAT
def onThermostat1Event(evt){
    processThermostatEvent(1, evt)
}

def processThermostatEvent(index, evt){
	def resume = ""; resume = parent?.settings?.resumePlay; if (resume == "") { resume = true }
    def personality = ""; personality = parent?.settings?.personalityMode; if (personality == "" || personality == null) { personality = false }
    def myVolume = -1
    def myVoice = getMyVoice(settings?.thermostatVoice1)
    LOGDEBUG("(onThermostatEvent): ${evt.name}, ${index}, ${evt.value}, ${myVoice}", true)
    //Check Restrictions
    if (!(processRestrictions("thermostat",index))){ return }
    state.TalkPhrase = null
    state.speechDevice = null
	if (parent?.state?.speechDeviceType == "capability.musicPlayer") {
		if (index == 1) {
			if (!settings?.thermostatResumePlay1 == null) { resume = settings.thermostatResumePlay1 }
		}
        if (resume == null) { resume = true }
	} else { resume = false }
    if (settings?.thermostatPersonality1 == "Yes") {personality = true}
    if (settings?.thermostatPersonality1 == "No") {personality = false}
    if (evt.value == "idle") {
        if (index == 1) { state.TalkPhrase = settings.thermostatTalkOnIdle1; state.speechDevice = thermostatSpeechDevice1; myVolume = getDesiredVolume(settings.thermostatVolume1)}
        if (!(state?.TalkPhrase == null)) {sendTalk(app.label,state.TalkPhrase, state.speechDevice, myVolume, resume, personality, myVoice, evt)} else {LOGDEBUG("Not configured to speak for this event", true)}
    }
    if (evt.value == "heating") {
        if (index == 1) { state.TalkPhrase = settings.thermostatTalkOnHeating1; state.speechDevice = thermostatSpeechDevice1; myVolume = getDesiredVolume(settings.thermostatVolume1)}
        if (!(state?.TalkPhrase == null)) {sendTalk(app.label,state.TalkPhrase, state.speechDevice, myVolume, resume, personality, myVoice, evt)} else {LOGDEBUG("Not configured to speak for this event", true)}
    }
    if (evt.value == "cooling") {
        if (index == 1) { state.TalkPhrase = settings.thermostatTalkOnCooling1; state.speechDevice = thermostatSpeechDevice1; myVolume = getDesiredVolume(settings.thermostatVolume1)}
        if (!(state?.TalkPhrase == null)) {sendTalk(app.label,state.TalkPhrase, state.speechDevice, myVolume, resume, personality, myVoice, evt)} else {LOGDEBUG("Not configured to speak for this event", true)}
    }
    if (evt.value == "fan only") {
        if (index == 1) { state.TalkPhrase = settings.thermostatTalkOnFan1; state.speechDevice = thermostatSpeechDevice1; myVolume = getDesiredVolume(settings.thermostatVolume1)}
        if (!(state?.TalkPhrase == null)) {sendTalk(app.label,state.TalkPhrase, state.speechDevice, myVolume, resume, personality, myVoice, evt)} else {LOGDEBUG("Not configured to speak for this event", true)}
    }

    state.TalkPhrase = null
    state.speechDevice = null
}
//END HANDLE THERMOSTAT

//BEGIN HANDLE ACCELERATION
def onAcceleration1Event(evt){
    processAccelerationEvent(1, evt)
}

def processAccelerationEvent(index, evt){
	def resume = ""; resume = parent?.settings?.resumePlay; if (resume == "") { resume = true }
    def personality = ""; personality = parent?.settings?.personalityMode; if (personality == "" || personality == null) { personality = false }
    def myVolume = -1
    def myVoice = getMyVoice(settings?.accelerationVoice1)
    LOGDEBUG("(onAccelerationEvent): ${evt.name}, ${index}, ${evt.value}, ${myVoice}", true)
    //Check Restrictions
    if (!(processRestrictions("acceleration",index))){ return }
    state.TalkPhrase = null
    state.speechDevice = null
	if (parent?.state?.speechDeviceType == "capability.musicPlayer") {
		if (index == 1) {
			if (!settings?.accelerationResumePlay1 == null) { resume = settings.accelerationResumePlay1 }
		}
        if (resume == null) { resume = true }
	} else { resume = false }
    if (settings?.accelerationPersonality1 == "Yes") {personality = true}
    if (settings?.accelerationPersonality1 == "No") {personality = false}
    if (evt.value == "active") {
        if (index == 1) { state.TalkPhrase = settings.accelerationTalkOnActive1; state.speechDevice = accelerationSpeechDevice1; myVolume = getDesiredVolume(settings.accelerationVolume1)}
        if (!(state?.TalkPhrase == null)) {sendTalk(app.label,state.TalkPhrase, state.speechDevice, myVolume, resume, personality, myVoice, evt)} else {LOGDEBUG("Not configured to speak for this event", true)}
    }
    if (evt.value == "inactive") {
        if (index == 1) { state.TalkPhrase = settings.accelerationTalkOnInactive1; state.speechDevice = accelerationSpeechDevice1; myVolume = getDesiredVolume(settings.accelerationVolume1)}
        if (!(state?.TalkPhrase == null)) {sendTalk(app.label,state.TalkPhrase, state.speechDevice, myVolume, resume, personality, myVoice, evt)} else {LOGDEBUG("Not configured to speak for this event", true)}
    }
    state.TalkPhrase = null
    state.speechDevice = null
}
//END HANDLE ACCELERATION

//BEGIN HANDLE WATER
def onWater1Event(evt){
    processWaterEvent(1, evt)
}

def processWaterEvent(index, evt){
	def resume = ""; resume = parent?.settings?.resumePlay; if (resume == "") { resume = true }
    def personality = ""; personality = parent?.settings?.personalityMode; if (personality == "" || personality == null) { personality = false }
    def myVolume = -1
    def myVoice = getMyVoice(settings?.waterVoice1)
    LOGDEBUG("(onWaterEvent): ${evt.name}, ${index}, ${evt.value}, ${myVoice}", true)
    //Check Restrictions
    if (!(processRestrictions("water",index))){ return }
    state.TalkPhrase = null
    state.speechDevice = null
	if (parent?.state?.speechDeviceType == "capability.musicPlayer") {
		if (index == 1) {
			if (!settings?.waterResumePlay1 == null) { resume = settings.waterResumePlay1 }
		}
        if (resume == null) { resume = true }
	} else { resume = false }
    if (settings?.waterPersonality1 == "Yes") {personality = true}
    if (settings?.waterPersonality1 == "No") {personality = false}
    if (evt.value == "wet") {
        if (index == 1) { state.TalkPhrase = settings.waterTalkOnWet1; state.speechDevice = waterSpeechDevice1; myVolume = getDesiredVolume(settings.waterVolume1)}
        if (!(state?.TalkPhrase == null)) {sendTalk(app.label,state.TalkPhrase, state.speechDevice, myVolume, resume, personality, myVoice, evt)} else {LOGDEBUG("Not configured to speak for this event", true)}
    }
    if (evt.value == "dry") {
        if (index == 1) { state.TalkPhrase = settings.waterTalkOnDry1; state.speechDevice = waterSpeechDevice1; myVolume = getDesiredVolume(settings.waterVolume1)}
        if (!(state?.TalkPhrase == null)) {sendTalk(app.label,state.TalkPhrase, state.speechDevice, myVolume, resume, personality, myVoice, evt)} else {LOGDEBUG("Not configured to speak for this event", true)}
    }
    state.TalkPhrase = null
    state.speechDevice = null
}
//END HANDLE WATER

//BEGIN HANDLE SMOKE
def onSmoke1Event(evt){
    processSmokeEvent(1, evt)
}

def processSmokeEvent(index, evt){
	def resume = ""; resume = parent?.settings?.resumePlay; if (resume == "") { resume = true }
    def personality = ""; personality = parent?.settings?.personalityMode; if (personality == "" || personality == null) { personality = false }
    def myVolume = -1
    def myVoice = getMyVoice(settings?.smokeVoice1)
    LOGDEBUG("(onSmokeEvent): ${evt.name}, ${index}, ${evt.value}, ${myVoice}", true)
    //Check Restrictions
    if (!(processRestrictions("smoke",index))){ return }
    state.TalkPhrase = null
    state.speechDevice = null
	if (parent?.state?.speechDeviceType == "capability.musicPlayer") {
		if (index == 1) {
			if (!settings?.smokeResumePlay1 == null) { resume = settings.smokeResumePlay1 }
		}
        if (resume == null) { resume = true }
	} else { resume = false }
    if (settings?.smokePersonality1 == "Yes") {personality = true}
    if (settings?.smokePersonality1 == "No") {personality = false}
    if (evt.value == "detected") {
        if (index == 1) { state.TalkPhrase = settings.smokeTalkOnDetect1; state.speechDevice = smokeSpeechDevice1; myVolume = getDesiredVolume(settings.smokeVolume1)}
        if (!(state?.TalkPhrase == null)) {sendTalk(app.label,state.TalkPhrase, state.speechDevice, myVolume, resume, personality, myVoice, evt)} else {LOGDEBUG("Not configured to speak for this event", true)}
    }
    if (evt.value == "clear") {
        if (index == 1) { state.TalkPhrase = settings.smokeTalkOnClear1; state.speechDevice = smokeSpeechDevice1; myVolume = getDesiredVolume(settings.smokeVolume1)}
        if (!(state?.TalkPhrase == null)) {sendTalk(app.label,state.TalkPhrase, state.speechDevice, myVolume, resume, personality, myVoice, evt)} else {LOGDEBUG("Not configured to speak for this event", true)}
    }
    if (evt.value == "tested") {
        if (index == 1) { state.TalkPhrase = settings.smokeTalkOnTest1; state.speechDevice = smokeSpeechDevice1; myVolume = getDesiredVolume(settings.smokeVolume1)}
        if (!(state?.TalkPhrase == null)) {sendTalk(app.label,state.TalkPhrase, state.speechDevice, myVolume, resume, personality, myVoice, evt)} else {LOGDEBUG("Not configured to speak for this event", true)}
    }
    state.TalkPhrase = null
    state.speechDevice = null
}
//END HANDLE SMOKE

//BEGIN HANDLE BUTTON
def onButton1Event(evt){
    processButtonEvent(1, evt)
}

def processButtonEvent(index, evt){
	def resume = ""; resume = parent?.settings?.resumePlay; if (resume == "") { resume = true }
    def personality = ""; personality = parent?.settings?.personalityMode; if (personality == "" || personality == null) { personality = false }
    def myVolume = -1
    def myVoice = getMyVoice(settings?.buttonVoice1)
    LOGDEBUG("(onButtonEvent): ${evt.name}, ${index}, ${evt.value}, ${myVoice}", true)
	//Check Restrictions
    if (!(processRestrictions("button",index))){ return }
    state.TalkPhrase = null
    state.speechDevice = null
	if (parent?.state?.speechDeviceType == "capability.musicPlayer") {
		if (index == 1) {
			if (!settings?.buttonResumePlay1 == null) { resume = settings.buttonResumePlay1 }
		}
        if (resume == null) { resume = true }
	} else { resume = false }
    if (settings?.buttonPersonality1 == "Yes") {personality = true}
    if (settings?.buttonPersonality1 == "No") {personality = false}
    if (index == 1 && evt.value == "pushed") { state.TalkPhrase = settings.buttonTalkOnPress1; state.speechDevice = buttonSpeechDevice1; myVolume = getDesiredVolume(settings.buttonVolume1)}
    if (index == 1 && evt.value == "held") { state.TalkPhrase = settings.buttonTalkOnHold1; state.speechDevice = buttonSpeechDevice1; myVolume = getDesiredVolume(settings.buttonVolume1)}
    if (!(state?.TalkPhrase == null)) {sendTalk(app.label,state.TalkPhrase, state.speechDevice, myVolume, resume, personality, myVoice, evt)} else {LOGDEBUG("Not configured to speak for this event", true)}
    state.TalkPhrase = null
    state.speechDevice = null
}
//END HANDLE BUTTON

//BEGIN HANDLE SHM
def onSHMEvent(evt){
	if (evt.value == "away") {processSHMEvent(1, evt)}
    if (evt.value == "stay") {processSHMEvent(2, evt)}
    if (evt.value == "off") {processSHMEvent(3, evt)}
}

def processSHMEvent(index, evt){
	def resume = ""; resume = parent?.settings?.resumePlay; if (resume == "") { resume = true }
    def personality = ""; personality = parent?.settings?.personalityMode; if (personality == "" || personality == null) { personality = false }
    def myVolume = -1
    def myVoice = ""
    LOGDEBUG("(onSHMEvent): ${evt.name}, ${index}, ${evt.value}, NotSetYet", true)
	//Check Restrictions
    if (!(processRestrictions("SHM",index))){ return }
    state.TalkPhrase = null
    state.speechDevice = null
	if (parent?.state?.speechDeviceType == "capability.musicPlayer") {
		if (index == 1) {
			if (!settings?.SHMResumePlayAway == null) { resume = settings.SHMResumePlayAway }
            if (settings?.SHMVoiceAway) { myVoice = getMyVoice(settings?.SHMVoiceAway) }
		}
		if (index == 2) {
			if (!settings?.SHMResumePlayHome == null) { resume = settings.SHMResumePlayHome }
            if (settings?.SHMVoiceHome) { myVoice = getMyVoice(settings?.SHMVoiceHome) }
		}
		if (index == 3) {
			if (!settings?.SHMResumePlayDisarm == null) { resume = settings.SHMResumePlayDisarm }
            if (settings?.SHMVoiceDisarm) { myVoice = getMyVoice(settings?.SHMVoiceDisarm) }
		}
        if (resume == null) { resume = true }
	} else { resume = false }
    if (settings?.SHMPersonality == "Yes") {personality = true}
    if (settings?.SHMPersonality == "No") {personality = false}
    if (index == 1) {state.TalkPhrase = settings.SHMTalkOnAway; state.speechDevice = SHMSpeechDeviceAway; myVolume = getDesiredVolume(settings.SHMVolumeAway)}
    if (index == 2) {state.TalkPhrase = settings.SHMTalkOnHome; state.speechDevice = SHMSpeechDeviceHome; myVolume = getDesiredVolume(settings.SHMVolumeHome)}
    if (index == 3) {state.TalkPhrase = settings.SHMTalkOnDisarm; state.speechDevice = SHMSpeechDeviceDisarm; myVolume = getDesiredVolume(settings.SHMVolumeDisarm)}
    if (!(state?.TalkPhrase == null)) {sendTalk(app.label,state.TalkPhrase, state.speechDevice, myVolume, resume, personality, myVoice, evt)} else {LOGDEBUG("Not configured to speak for this event", true)}
    state.TalkPhrase = null
    state.speechDevice = null
}
//END HANDLE SHM

//BEGIN HANDLE ENERGY METER
def onPowerMeter1Event(evt){
    processPowerMeterEvent(1, evt)
}

def processPowerMeterEvent(index, evt){
	def resume = ""; resume = parent?.settings?.resumePlay; if (resume == "") { resume = true }
    def personality = ""; personality = parent?.settings?.personalityMode; if (personality == "" || personality == null) { personality = false }
    def myVolume = -1
    def myVoice = getMyVoice(settings?.buttonVoice1)
    def energySpeak = false
    def powerLevel = 0
    def deviceName = ""
    try {
		deviceName = evt.displayName  //User given name of the device triggering the event
    } catch (ex) { 
    	LOGDEBUG("processPowerMeterEvent() evt.displayName failed; trying evt.device.displayName", false)
        try {
        	deviceName = evt.device.displayName //User given name of the device triggering the event
		} catch (ex2) {
			LOGDEBUG("processPowerMeterEvent() evt.device.displayName filed; trying evt.device.name")
			try {
				deviceName = evt.device.name //SmartThings name for the device triggering the event
			} catch (ex3) {
				LOGDEBUG("processPowerMeterEvent() evt.device.name filed; Giving up")
				deviceName = "Unknown"
			}
		}
	}
    //powerLevel = Math.round(evt.value.toDouble()).toString()
    try {
    	powerLevel = evt?.value?.toDouble()?.trunc()?.toString()?.replace(".0","")
    } catch (err) {
    	powerLevel = evt?.value
    }
    LOGDEBUG("(onPowerMeterEvent): ${evt.name}, ${index}, ${evt.value}, ${powerLevel}, ${myVoice}", true)
	//Check Restrictions
    if (!(processRestrictions("powerMeter",index))){ return }
    state.TalkPhrase = null
    state.speechDevice = null
	if (parent?.state?.speechDeviceType == "capability.musicPlayer") {
		if (index == 1) {
			if (!settings?.powerMeterResumePlay1 == null) { resume = settings.powerMeterResumePlay1 }
		}
        if (resume == null) { resume = true }
	} else { resume = false }
    if (settings?.powerMeterPersonality1 == "Yes") {personality = true}
    if (settings?.powerMeterPersonality1 == "No") {personality = false}
    if (!(state?.powerMeterState.contains("|${deviceName}-"))) {
    	state.powerMeterState = "${state.powerMeterState}|${deviceName}-UNKNOWN|"
    }
    //HIGH
    if (powerLevel?.toDouble() > settings?.powerMeterTalkOnRiseThold1?.toDouble()) { 
    	if ((!(state?.powerMeterState?.contains("|${deviceName}-HIGH|"))) && energySpeak == false) { 
        	state.powerMeterState = state.powerMeterState.replace("|${deviceName}-UNKNOWN|","|${deviceName}-HIGH|")
            state.powerMeterState = state.powerMeterState.replace("|${deviceName}-NORMAL|","|${deviceName}-HIGH|")
            state.powerMeterState = state.powerMeterState.replace("|${deviceName}-LOW|","|${deviceName}-HIGH|")
            energySpeak = true 
        }
    }
    //NORMAL
    if (((powerLevel?.toDouble() > settings?.powerMeterTalkOnFallThold1?.toDouble()) || settings?.powerMeterTalkOnFallThold1?.toDouble() == 0) && (powerLevel?.toDouble() < settings?.powerMeterTalkOnRiseThold1?.toDouble())) { 
    	if ((!(state?.powerMeterState?.contains("|${deviceName}-NORMAL|"))) && energySpeak == false) { 
        	state.powerMeterState = state.powerMeterState.replace("|${deviceName}-UNKNOWN|","|${deviceName}-NORMAL|")
            state.powerMeterState = state.powerMeterState.replace("|${deviceName}-LOW|","|${deviceName}-NORMAL|")
            state.powerMeterState = state.powerMeterState.replace("|${deviceName}-HIGH|","|${deviceName}-NORMAL|")
            energySpeak = true 
        }
        if (settings?.powerMeterTalkOnFallThold1?.toDouble() == 0) { 
        	// If Low = 0, Then override LOW alert, we've returned to Normal (can't go lower).
        	state.powerMeterState = state.powerMeterState.replace("|${deviceName}-UNKNOWN|","|${deviceName}-NORMAL|")
            state.powerMeterState = state.powerMeterState.replace("|${deviceName}-HIGH|","|${deviceName}-NORMAL|")
        }
    }
    //LOW
    if ((powerLevel?.toDouble() < settings?.powerMeterTalkOnFallThold1?.toDouble()) && settings?.powerMeterTalkOnFallThold1?.toDouble() > 0) { 
    	if ((!(state?.powerMeterState?.contains("|${deviceName}-LOW|"))) && energySpeak == false) { 
        	state.powerMeterState = state.powerMeterState.replace("|${deviceName}-UNKNOWN|","|${deviceName}-LOW|")
            state.powerMeterState = state.powerMeterState.replace("|${deviceName}-NORMAL|","|${deviceName}-LOW|")
            state.powerMeterState = state.powerMeterState.replace("|${deviceName}-HIGH|","|${deviceName}-LOW|")
            energySpeak = true 
        }
    }
    if (index == 1 && state.powerMeterState.contains("|${deviceName}-HIGH|") && energySpeak) {state.TalkPhrase = settings.powerMeterTalkOnRise1; state.speechDevice = powerMeterSpeechDevice1; myVolume = getDesiredVolume(settings.powerMeterVolume1)}
    if (index == 1 && state.powerMeterState.contains("|${deviceName}-NORMAL|") && energySpeak) {state.TalkPhrase = settings.powerMeterTalkOnNormal1; state.speechDevice = powerMeterSpeechDevice1; myVolume = getDesiredVolume(settings.powerMeterVolume1)}
    if (index == 1 && state.powerMeterState.contains("|${deviceName}-LOW|") && energySpeak) {state.TalkPhrase = settings.powerMeterTalkOnFall1; state.speechDevice = powerMeterSpeechDevice1; myVolume = getDesiredVolume(settings.powerMeterVolume1)}
    LOGDEBUG("energySpeak=${energySpeak}, powerLevel=${powerLevel}, state.powerMeterState=${state.powerMeterState}", false)
    if (!(state?.TalkPhrase == null)) {
    	if (state.TalkPhrase.toLowerCase().contains("%value%")) { 
    		state.TalkPhrase = state.TalkPhrase.toLowerCase().replace("%value%",powerLevel)
    	}
    	sendTalk(app.label,state.TalkPhrase, state.speechDevice, myVolume, resume, personality, myVoice, evt)
    } else {
    	LOGDEBUG("Not configured to speak for this event", true)
    }
    state.TalkPhrase = null
    state.speechDevice = null
}
//END HANDLE ENERGY METER

//BEGIN HANDLE ROUTINE
def onRoutineEvent(evt){
	if (settings?.routineDeviceGroup1?.contains(evt.displayName)){
    	//Only process configured routines
    	processRoutineEvent(1, evt)
    }
}

def processRoutineEvent(index, evt){
	def resume = ""; resume = parent?.settings?.resumePlay; if (resume == "") { resume = true }
    def personality = ""; personality = parent?.settings?.personalityMode; if (personality == "" || personality == null) { personality = false }
    def myVolume = -1
    def myVoice = getMyVoice(settings?.routineVoice1)
    LOGDEBUG("(onRoutineEvent): ${evt.displayName}, ${index}, '${evt.displayName}' executed, ${myVoice}", true)
	//Check Restrictions
    if (!(processRestrictions("routine",index))){ return }
    state.TalkPhrase = null
    state.speechDevice = null
	if (parent?.state?.speechDeviceType == "capability.musicPlayer") {
		if (index == 1) {
			if (!settings?.routineResumePlay1 == null) { resume = settings.routineResumePlay1 }
		}
        if (resume == null) { resume = true }
	} else { resume = false }
    if (settings?.routinePersonality1 == "Yes") {personality = true}
    if (settings?.routinePersonality1 == "No") {personality = false}
    if (index == 1) { state.TalkPhrase = settings.routineTalkOnRun1; state.speechDevice = routineSpeechDevice1; myVolume = getDesiredVolume(settings.routineVolume1)}
    if (state.TalkPhrase.toLowerCase().contains("%routine%")) { 
    	state.TalkPhrase = state.TalkPhrase.toLowerCase().replace("%routine%",evt.displayName)
    }
    if (!(state?.TalkPhrase == null)) {sendTalk(app.label,state.TalkPhrase, state.speechDevice, myVolume, resume, personality, myVoice, evt)} else {LOGDEBUG("Not configured to speak for this event", true)}
    state.TalkPhrase = null
    state.speechDevice = null
}
//END HANDLE ROUTINE


///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
def getDesiredVolume(invol) {
	def globalVolume = parent?.settings?.speechVolume
    def globalMinimumVolume = parent?.settings?.speechMinimumVolume
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
    if (parent?.state?.speechDeviceType == "capability.musicPlayer") { 
    	LOGDEBUG("finalVolume: ${finalVolume}", true)
    }
    return finalVolume
}

def getMyVoice(deviceVoice){
    def myVoice = "Not Used"
    if (!(deviceVoice == null )) { myVoice = deviceVoice }
    if (parent?.state?.speechDeviceType == "capability.musicPlayer") {
    	log.debug "getMyVoice: deviceVoice=${myVoice}"
        log.debug "getMyVoice: settings.parent.speechVoice=${parent?.settings?.speechVoice}"
		myVoice = (!(deviceVoice == null || deviceVoice == "")) ? deviceVoice : (parent?.settings?.speechVoice ? parent?.settings?.speechVoice : "Salli(en-us)")
    }
    return myVoice
}

def sendTalk(appname, phrase, customSpeechDevice, volume, resume, personality, voice, evt){
    LOGDEBUG("parent.Talk(app=Me,customdevice=${customSpeechDevice},volume=${volume},resume=${resume},personality=${personality},voice=${myVoice},evt=${evt},phrase=${phrase})", false)
	parent.Talk(appname, phrase, customSpeechDevice, volume, resume, personality, voice, evt)
}

def LOGDEBUG(txt, send){
	if (send == true || send == null || send == "") { def sendToParent = true } else { def sendToParent = false }
	if (parent?.settings?.debugmode && sendToParent) { parent.LOGDEBUG("[CHILD:${app.label}] ${txt}", true) }
    try {
    	if (parent?.settings?.debugmode || sendToParent == false) { log.debug("${app.label.replace(" ","").toUpperCase()}(${state.appversion}) || ${txt}")}
    } catch(ex) {
    	log.error("LOGDEBUG unable to output requested data!", true)
    }
}
def LOGTRACE(txt){
	parent.LOGTRACE("[CHILD:${app.label}] ${txt}")
    try {
    	log.trace("${app.label.replace(" ","").toUpperCase()}(${state.appversion}) || ${txt}")
    } catch(ex) {
    	log.error("LOGTRACE unable to output requested data!")
    }
}
def LOGERROR(txt){
	parent.LOGERROR("[CHILD:${app.label}] ${txt}")
    try {
    log.error("${app.label.replace(" ","").toUpperCase()}(${state.appversion}) || ERROR: ${txt}")
    } catch(ex) {
    	log.error("LOGERROR unable to output requested data!")
    }
}

def setAppVersion(){
    state.appversion = "C2.0.6"
}