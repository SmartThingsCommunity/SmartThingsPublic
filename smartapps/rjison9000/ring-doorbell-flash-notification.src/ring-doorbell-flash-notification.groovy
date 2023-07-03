definition(
    name: "Ring Doorbell Flash Notification",
    namespace: "rjison9000",
    author: "Robert Ison",
    description: "When someone rings the bell of your Ring doorbell, a light of your choosing will flash. It will end up in the same state as it started.",
    category: "My Apps",
    iconUrl: "http://cdn.device-icons.smartthings.com/Lighting/light11-icn.png",
    iconX2Url: "http://cdn.device-icons.smartthings.com/Lighting/light11-icn@2x.png",
    iconX3Url: "http://cdn.device-icons.smartthings.com/Lighting/light11-icn@2x.png")

preferences {
    section("Ring Doorbell") {
        input "ringbutton", "capability.button", required: true, title: "Select your Ring doorbell?"
    }

    section("Light to Flash") {
        input "theswitch", "capability.switch", required: true
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
    subscribe(ringbutton, "button", ringDetectedHandler)
}

void ringDetectedHandler(evt) {

    def switchState = theswitch.currentState("switch")
    log.debug "switchState.value $switchState.value"
    
    def switchlevelState = theswitch.currentState("level")
    log.debug "switchlevelState.value $switchlevelState.value"
   
    if (evt.value == 'pushed'){
        if (switchState.value == 'on') {
            theswitch.off()
            theswitch.on([delay: 1000])
            theswitch.off([delay: 2000])
            theswitch.on([delay: 3000])
            theswitch.off([delay: 4000])
            theswitch.on([delay: 5000])
            theswitch.off([delay: 6000])
            theswitch.on([delay: 7000])
        }else{
            theswitch.on()
            theswitch.off([delay: 1000])
            theswitch.on([delay: 2000])
            theswitch.off([delay: 3000])
            theswitch.on([delay: 4000])
            theswitch.off([delay: 5000])
            theswitch.on([delay: 6000])
            theswitch.off([delay: 7000])
        }
    }
}