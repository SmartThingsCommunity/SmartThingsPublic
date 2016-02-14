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
 *  - Handle deduping event calls.
 *  - Add pretty descriptions for displaying sub page settings to help navigate easier (summary view).
 *  - Add per device or button event Mode/Day/Time settings.
 *  - Add clean up of settings when preferences are changed, devices removed, etc (phantom actions).
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
        	input(name: "btnDevice", type: "capability.button", title: "Button Device", required: true, multiple: false, submitOnChange: true) 
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
        //section { paragraph "${settings}" }
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
        	input(name: getPrefName(getButtonId(), getButtonEvent(), "Devices"), type: "capability.switch", title: "Switches & Dimmers", required: true, multiple: true, submitOnChange: true)
        }
        settings[getPrefName(getButtonId(), getButtonEvent(), "Devices")].each { device ->
	        section (hideable: true, hidden: false, "Configure ${device?.displayName} Actions") {
                input(name: getPrefName(getButtonId(), getButtonEvent(), device.id, "power"), type: "enum", title: "Power", options: ["None","Toggle","On","Off"], defaultValue: "None", required: true, multiple: false)
             	if (device.hasCapability("Switch Level")) {
                    input(name: getPrefName(getButtonId(), getButtonEvent(), device.id, "dimmer"), type: "enum", title: "Dimming", options: ["None","Sequential","Set Level"], defaultValue: "None", required: true, multiple: false, submitOnChange: true)
                    if (settings[getPrefName(getButtonId(), getButtonEvent(), device.id, "dimmer")] == "Set Level") {
                    	input(name: getPrefName(getButtonId(), getButtonEvent(), device.id, "dimmerLevel"), type: "number", title: "Dimmer Level", range: "0..100", defaultValue: 100, required: true, multiple: false) 
                    }
                    if (settings[getPrefName(getButtonId(), getButtonEvent(), device.id, "dimmer")] == "Sequential") {
                    	input(name: getPrefName(getButtonId(), getButtonEvent(), device.id, "dimmerDirection"), type: "enum", title: "Dimming Direction", options: ["Up","Down"], defaultValue: "Up", required: true, multiple: false)
                        input(name: getPrefName(getButtonId(), getButtonEvent(), device.id, "dimmerStep"), type: "number", title: "Dimming Step (Increase/Decrease By)", range: "0..100", defaultValue: 10, required: true, multiple: false)
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
	subscribe(btnDevice, "button", btnEvent)
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
    
    if (dedupe())
    	return

    def actions = [actionPower, actionDimmer]

	// Devices
    getDevices(btnId, btnEvent).each { device ->
        actions.each { action ->
        	"$action"(btnId, btnEvent, device)
        }
    }
}

def dedupe() {
	// TODO Handle multiple events being fired from one button press
	false
}

// Actions
def actionPower(btnId, btnEvent, device) {
	def prefPower = getPrefValue(btnId, btnEvent, device.id, "power")
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
	def prefDimmer = getPrefValue(btnId, btnEvent, device.id, "dimmer")
    if (prefDimmer == "None" || !device.hasCapability("Switch Level"))
    	return
    else if (prefDimmer == "Set Level") {
    	def prefDimmerLevel = getPrefValue(btnId, btnEvent, device.id, "dimmerLevel")
        if (prefDimmerLevel)
        	device.setLevel(prefDimmerLevel)
    } else if (prefDimmer == "Sequential") {
    	def prefDimmerDirection = getPrefValue(btnId, btnEvent, device.id, "dimmerDirection")
    	def prefDimmerStep = getPrefValue(btnId, btnEvent, device.id, "dimmerStep")
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
	(btnDevice?.currentValue("numButtons") ?: "0")?.toInteger()
}

def persistPrefParams(params) {
    if (params?.pref)
    	state.pref = params.pref
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

def getPrefButtonDescription(btnId) {
	// TODO Give a description of contents
	"Click to view"
}

def getPrefActionDescription(btnId, btnEvent) {
	// TODO Give a description of contents
    "Click to view"
}

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

def getDevices(btnId, btnEvent) {
	getPrefValue(btnId, btnEvent, "Devices")
}