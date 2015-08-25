definition(
    name: "Alarm Disarm",
    namespace: "",
    author: "Scott Windmiller",
    description: "Virtual Switch to disarm alarm",
    category: "Safety & Security",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png")

preferences {
    section("Select Switch to monitor"){
        input "theSwitch", "capability.switch"
    }
}

def installed() {
    log.debug "Installed with settings: ${settings}"
    initialize()
}

def updated(settings) {
    log.debug "Updated with settings: ${settings}"
    unsubscribe()
    initialize()
}

def offHandler(evt) {
   if (location.mode == "Home Day" || location.mode == "Home Night")
    {
  if(getSunriseAndSunset().sunrise.time < now() && 
                 getSunriseAndSunset().sunset.time > now()){
        log.debug "Daytime"
            setLocationMode("Away Day")
            }
            else {
     log.debug "Nighttime"
            setLocationMode("Away Night")
            }
     log.debug "Received off from ${theSwitch}"
     }
}

def onHandler(evt) {
if(getSunriseAndSunset().sunrise.time < now() && 
                 getSunriseAndSunset().sunset.time > now()){
    log.debug "Daytime"
            setLocationMode("Home Day")
            }
            else {
    log.debug "Nighttime"
            setLocationMode("Home Night")
            }
    log.debug "Received on from ${theSwitch}"
}
def modeChangeHandler(evt) {

   if (evt.value == "Home Day" || evt.value == "Home Night")
    {
    log.debug "Changed to Disarmed"
        theSwitch.on()
    }
    else {
    log.debug "Changed to Armed"
        theSwitch.off()
    }
}


def initialize() {
    subscribe(theSwitch, "switch.On", onHandler)
    subscribe(theSwitch, "switch.Off", offHandler)
    subscribe(location, modeChangeHandler)

}