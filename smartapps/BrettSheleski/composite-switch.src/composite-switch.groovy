definition(
    name: "Composite Switch",
    namespace: "BrettSheleski",
    author: "Brett Sheleski",
    description: "Make a switch from two buttons",
    category: "SmartThings Labs",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/App-BigButtonsAndSwitches.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/App-BigButtonsAndSwitches@2x.png"
)

preferences {
        input "onButton", "capability.momentary", title: "On Button", required: true
        input "offButton", "capability.momentary", title: "Off Button", required: true
        input "theSwitch", "capability.switch", title: "The Switch", required: true
}

def installed() {
	log.debug "Installed with settings: ${settings}"
	initialize()
}

def updated() {
	log.debug "Updated with settings: ${settings}"
	initialize()
}

def uninstalled() {
    removeChildDevices(getChildDevices())
}

def initialize(){
/*
	def namespace = app.namespace
	def deviceName = "Dummy Switch"
	def theHubId = location.hubs[0].id
    def deviceId = "${app.id}-switch"
    def theSwitch = getSwitch()

    if (theSwitch){
        log.debug "FOUND child device found for ${childDevice.deviceNetworkId}"
    }
    else{
    	log.debug "NOT FOUND child device, creating one..."

        def deviceMap = [completedSetup: false]

        deviceMap['name'] = app.label + " - Switch";
        deviceMap['label'] = deviceMap['name'];
        
        theSwitch = addChildDevice(namespace, deviceName, deviceId, theHubId, deviceMap)
        log.debug "Switch Created"
    }
    */

    subscribe(onButton, "momentary.push", handleOnButtonPush)
    subscribe(offButton, "momentary.push", handleOffButtonPush)
    subscribe(theSwitch, "switch", handleSwitchEvent)
}

def getSwitch(){
    /*
    def deviceId = "${app.id}-switch"
    def theSwitch = getChildDevice(deviceId);
    */
    return theSwitch
}

def handleOnButtonPush(){
    log.debug "Handle ON BUTTON push"

    //def theSwitch = getSwitch();

    if (theSwitch){
        //sendEvent(theSwitch, [name : "switch", value: "on"])
        //theSwitch.on();
    }
}

def handleOffButtonPush(){
    log.debug "Handle OFF BUTTON push"

    //def theSwitch = getSwitch();

    if (theSwitch){
        //sendEvent(theSwitch, [name : "switch", value: "off"])
        //theSwitch.off();
    }
}

def handleSwitchEvent(evt){
    log.debug "Handle SWITCH (${evt.value})"

    def isOn = evt.value == "on";

    if (isOn){
        onButton.push();
    }
    else{
        offButton.push();
    }
}
