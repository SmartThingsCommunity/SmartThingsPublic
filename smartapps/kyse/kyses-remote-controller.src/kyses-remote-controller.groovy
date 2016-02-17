/**
 *  Kyse's Remote Controller
 *
 *  Copyright 2016 Jared Fisher
 *
 *  2/14/2016 - Initial Commit
 *
 *  Note: Works well with Kyse's Aeon Minimote, which is modified to give a nice 
 *  mobile app user interface for triggering the button events, and provides the
 *  number of buttons available to the device.
 *
 *  Todo List:
 *  - Handle deduping event calls. - In progress
 *  - Add pretty descriptions for displaying sub page settings to help navigate easier (summary view).
 *  - Add per device or button event Mode/Day/Time settings.
 *  - Add clean up of settings when preferences are changed, devices removed, etc (phantom actions). - In Progress
 *  - Add support for other capabilities/actions.
 *  - Add preference for specifying number of buttons on device for incompatible device handlers.
 */
definition(
    name: "Kyse's Remote Controller",
    namespace: "kyse",
    author: "Jared Fisher",
    description: "SmartApp to improve mapping and customization of remote buttons to device actions.",
    category: "Convenience",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/MiscHacking/remote.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/MiscHacking/remote@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/MiscHacking/remote@2x.png")


preferences {
	page(name: "pageSettingsMain")
    page(name: "pageConfigButton")
    page(name: "pageConfigEvent")
}

// Preferences
def pageSettingsMain() {
	dynamicPage(name: "pageSettingsMain", title: "Setup", install: true, uninstall: true) {
    	section ("Button Device") { 
        	input(name: getSettingName("remote"), type: "capability.button", title: "Button Device", required: true, multiple: false, submitOnChange: true) 
        }
        if (getNumButtons()) {
        	section ("Configure Buttons") {
                (1..getNumButtons()).each { btn ->
                	href(name: "pageConfigButton${btn}", title: "Button ${btn}", description: getPrefButtonDescription(btn), page: "pageConfigButton", params: [ pref: [btnId: "${btn}"]] )
                }
            }
        }
        section (hideable: true, hidden: false, "Other Settings") {
        	input(name: "dedupe", type: "number", title: "Dedupe time (miliseconds)", defaultValue: 1000, required: true, multiple: false)
        	label(name: "label", title: "Assign a name", required: false, multiple: false)
            //mode(title: "Set for specific mode(s)")
        }
        section { paragraph "Settings: ${settings}" }
	}
}

def pageConfigButton(params) {
	persistPrefParams(params)
    dynamicPage(name: "pageConfigButton", title: "Select Button ${getButtonIdStr()} Event", install: false, uninstall: false) {
    	section {
            ["pushed","held"].each { event ->
                href(name: "pageConfigEvent${getButtonIdStr()}${event}", title: event, description: getPrefActionDescription(getButtonId(), event), page: "pageConfigEvent", params: [ pref: [btnId: "${getButtonId()}", btnEvent: event]])
            }
        }
    }
}

def pageConfigEvent(params) {
	persistPrefParams(params)
	dynamicPage(name: "pageConfigEvent", title: "Configure Button ${getButtonIdStr()} ${getButtonEvent().capitalize()}", install: false, uninstall: false) {
    	section ("Devices") {
        	input(name: getSettingName(getButtonId(),getButtonEvent(),"devices"), type: "capability.switch", title: "Switches & Dimmers", required: true, multiple: true, submitOnChange: true)
        }
        settings[getSettingName(getButtonId(),getButtonEvent(),"devices")].each { device ->
	        section (hideable: true, hidden: false, "Configure ${device?.displayName} Actions") {
                input(name: getSettingName(getButtonId(),getButtonEvent(),device.id,"power"), type: "enum", title: "Power", options: ["None","Toggle","On","Off"], defaultValue: "None", required: true, multiple: false)
             	if (device.hasCapability("Switch Level")) {
                    input(name: getSettingName(getButtonId(),getButtonEvent(),device.id,"dimmer"), type: "enum", title: "Dimming", options: ["None","Sequential","Set Level"], defaultValue: "None", required: true, multiple: false, submitOnChange: true)
                    if (settings[getSettingName(getButtonId(),getButtonEvent(),device.id,"dimmer")] == "Set Level") {
                    	input(name: getSettingName(getButtonId(),getButtonEvent(),device.id,"dimmer","level"), type: "number", title: "Dimmer Level", range: "0..100", defaultValue: 100, required: true, multiple: false) 
                    }
                    if (settings[getSettingName(getButtonId(),getButtonEvent(),device.id,"dimmer")] == "Sequential") {
                    	input(name: getSettingName(getButtonId(),getButtonEvent(),device.id,"dimmer","direction"), type: "enum", title: "Dimming Direction", options: ["Up","Down"], defaultValue: "Up", required: true, multiple: false)
                        input(name: getSettingName(getButtonId(),getButtonEvent(),device.id,"dimmer","step"), type: "number", title: "Dimming Step (Increase/Decrease By)", range: "0..100", defaultValue: 10, required: true, multiple: false)
                    }
                }
            }
        }
    }
}

def installed() {
	log.debug "Installed with settings: ${settings}"
	initialize()
}

def updated() {
	unsubscribe()
	initialize()
}

def initialize() {
    cleanSettings()
	subscribe(getRemote(), "button", btnEvent)
}

// Handlers
def btnEvent(evt) {
	def btnId = evt.jsonData.buttonNumber
    def btnEvent = evt.value
	//log.debug "Button Event Fired: Button ${btnId} ${btnEvent}."
	
    if (btnId < 1 || btnId > getNumButtons() || !btnEvent) {
    	log.debug "Event Error: parameters out of bounds.  Button Number: ${btnId}, Action: ${btnEvent}."
        return
    }
    
    if (dedupe(btnId, btnEvent))
    	return

    def actions = ["actionPower", "actionDimmer"]

	// Devices
    getDevices(btnId, btnEvent).each { device ->
        actions.each { action ->
        	"$action"(btnId, btnEvent, device)
        }
    }
}

def dedupe(btnId, btnEvent) {
	// TODO Handle multiple events being fired from one button press
    log.debug "Dedupe: Button ${btnId} ${btnEvent}.  This message in place to assess need for deduping."
	false
}

// Actions
def actionPower(btnId, btnEvent, device) {
	def prefPower = getSetting(btnId,btnEvent,device.id,"power")
    if (prefPower == "None" || !device.hasCapability("Switch"))
    	return
    else if (prefPower == "Toggle")
    	if (device.currentSwitch == "on")
        	device.off()
        else
        	device.on()
    else
    	device."${prefPower.toLowerCase()}"()
}

def actionDimmer(btnId, btnEvent, device) {
	def prefDimmer = getSetting(btnId,btnEvent,device.id,"dimmer")
    if (prefDimmer == "None" || !device.hasCapability("Switch Level"))
    	return
    else if (prefDimmer == "Set Level") {
    	def prefDimmerLevel = getSetting(btnId,btnEvent,device.id,"dimmer","level")
        if (prefDimmerLevel)
        	device.setLevel(prefDimmerLevel)
    } else if (prefDimmer == "Sequential") {
    	def prefDimmerDirection = getSetting(btnId,btnEvent,device.id,"dimmer","direction")
    	def prefDimmerStep = getSetting(btnId,btnEvent,device.id,"dimmer","step")
        def currentLevel = device.currentLevel
        def newLevel = currentLevel
        if (prefDimmerDirection == "Up")
        	newLevel += prefDimmerStep
        else
        	newLevel -= prefDimmerStep
        newLevel = Math.max(0,Math.min(100,newLevel.toInteger()))
        device.setLevel(newLevel)
    }
}

// Helpers
def getNumButtons() {
	(getRemote()?.currentValue("numButtons") ?: "0")?.toInteger()
}

def getPrefButtonDescription(btnId) {
	// TODO Give a description of contents
	"Click to view"
}

def getPrefActionDescription(btnId, btnEvent) {
	// TODO Give a description of contents
    "Click to view"
}

// dynamicPage params persistence
def persistPrefParams(params) {
    if (params?.pref)
    	state.pref = params.pref
}

def cleanPrefParams() {
	if (params?.pref)
    	delete state.pref
}

def getButtonId() {
	state.pref?.btnId ?: 0
}

def getButtonIdStr() {
	state.pref?.btnId?.toString() ?: ""
}

def getButtonEvent() {
	state.pref?.btnEvent ?: ""
}

// Settings
def getPrefName(btnId, btnEvent, deviceId, actionName = null) {
	("btn${btnId}${btnEvent}${deviceId}${actionName ? "-" + actionName : ""}").toString()
}

def getPrefValue(btnId, btnEvent, deviceId, actionName = null) {
	//def name = getPrefName(btnId, btnEvent, deviceId, actionName)
	def value = settings[getPrefName(btnId, btnEvent, deviceId, actionName)]
    //log.debug "name: ${name}, value: ${value}"
    //log.debug "settings: ${settings}"
    value
}

def settingsMap() {
	// Anything past the action should include the previous setting name ie dimmer -> dimmerlevel
	[
    	remote: [dependency: null],
        devices: [depencency: null],
        power: [dependency: [name: "devices"]],
        dimmer: [dependency: [name: "devices"]],
        dimmerlevel: [dependency: [name: "dimmer", value: "Set Level"]],
        dimmerdirection: [dependency: [name: "dimmer", value: "Sequential"]],
        dimmerstep: [dependency: [name: "dimmer", value: "Sequential"]],
    ]
}

def cleanSettings() {
	cleanPrefParams()
    
    settings.each { key, value -> 
        if (!checkDependency(key, value))
        	delete settings[key]
    }
}

def checkDependency(key, value) {
	def paramMap = parseSettingName(key)
    def split = key.split("~")
	def mapName = "${->def size = split.size() > 4 ? -2..-1 : -1;[split[size]].flatten().join()}"
    def dependencyMap = settingsMap()[mapName]
    if (dependencyMap?.dependency) {
    	// Get the dependency setting (if it exists)
        def depCheck = split[0..-1]
        log.debug "${depCheck}"
    }
    true
}

def getSettingName(Object[] params) {
	"${->params*.toString()*.toLowerCase().join("~")}".toString()
}

def settingsIndexMap() {
	[0:"id",1:"event",2:"deviceId",3:"action",4:"option"]
}

def parseSettingName(name) {
	def map = [:]
    name.split("~").eachWithIndex { val, i ->
    	map.put(settingsIndexMap()[i], val)
    }
    map
}

def getSetting(Object[] params) {
	settings[getSettingName(params)]
}

def getRemote() {
	getSetting("remote")
}

def getDevices(btnId, btnEvent) {
	getSetting(btnId,btnEvent,"devices")
}
