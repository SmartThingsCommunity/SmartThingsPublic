definition(
    name: "Light On When Motion",
    namespace: "keithsutherland10a",
    author: "Keith Sutherland",
    description: "Turn on Light when Motion Detected between Sunset and Sunrise.",
    category: "My Apps",
    iconUrl: "http://cdn.device-icons.smartthings.com/Lighting/light11-icn@2x.png",
    iconX2Url: "http://cdn.device-icons.smartthings.com/Lighting/light11-icn@2x.png",
    iconX3Url: "http://cdn.device-icons.smartthings.com/Lighting/light11-icn@2x.png")

preferences {
    section("Turn on when motion detected:") {
        input "themotion", "capability.motionSensor", required: true, title: "Where?"
    }
    section("Turn on this light") {
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
    subscribe(themotion, "motion.active", motionDetectedHandler)
    subscribe(themotion, "motion.inactive", motionStoppedHandler)
}

def sunsetTimeHandler(evt) {
    //when I find out the sunset time, schedule the lights to turn on with an offset
    scheduleTurnOn(evt.value)
}

def motionDetectedHandler(evt) {
    log.debug "motionDetectedHandler called: $evt"
    if (getSunriseAndSunset().sunset.time < now() || getSunriseAndSunset().sunrise.time > now()) {
       theswitch.on()
    }
}