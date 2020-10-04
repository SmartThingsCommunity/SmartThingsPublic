/**
 *  Copyright 2017 Tim Brayshaw
 */
definition(
    name: "Arrival Lights",
    namespace: "twisty",
    author: "Tim Brayshaw",
    description: "Turn on lights when someone arrives, and turn them off again after a set number of minutes you specify.",
    category: "Safety & Security",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Meta/light_outlet.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Meta/light_outlet@2x.png"
)

preferences {
   section("When I arrive..."){
       input "presences", "capability.presenceSensor", title: "Who?", multiple: true
   }
   section("These devices are switched on...") {
       input "switches", "capability.switch", title: "Choose switches or lights:", multiple: true
   }
   section("Turn off again after...") {
       input "minutesLater", "number", title: "Minutes:", defaultValue: 5
   }
}

def installed() {
    log.debug "Installed with settings: ${settings}"
    init()
}

def updated() {
    log.debug "Updated with settings: ${settings}"
    unsubscribe()
    init()
}

def init() {
    def switchStates = [:]
    def switchLevels = [:]
    for (device in switches) {
        def key = device.getId()
        if (device.hasCapability("Switch Level")) {
            switchLevels[key] = device.currentValue("level")
        }
        switchStates[key] = device.currentValue("switch")
    }
    state.initialSwitchStates = switchStates
    state.initialSwitchLevels = switchLevels
    subscribe(presences, "presence", presenceHandler)
}

def presenceHandler(evt) {
    log.debug "presenceHandler $evt.name: $evt.value"
    if(evt.value == "present") {
        log.debug "A presence has arrived"
        turnOnSwitches()
    }
    def presenceValue = presences.find{item -> item.currentPresence == "present"}
    if (!presenceValue) {
        log.debug "Everyone's away."
    }
}

def restoreSwitches() {
    for (device in switches) {
        def key = device.getId()
        def thisSwitchInitialState = state.initialSwitchStates[key]
        if (thisSwitchInitialState == "on") {
            log.debug "Leaving switch on. ${device}"
            def thisSwitchInitialLevel = state.initialSwitchLevels[key]
            if (thisSwitchInitialLevel) {
                log.debug "Restoring level to: ${thisSwitchInitialLevel}"
                device.setLevel(thisSwitchInitialLevel)
            }
        } else {
            log.debug "Turning switch off. ${device}"
            device.off()
        }
    }
}

def turnOnSwitches() {
    for (device in switches) {
        log.debug "Turning on switch. ${device}"
        device.on()
        if (device.hasCapability("Switch Level")) {
            def level = 100
            log.debug "Setting level to: ${level}"
            device.setLevel(level)
        }
    }
    def delay = minutesLater * 60
    log.debug "Restoring switches in ${minutesLater} minutes (${delay} seconds)"
    runIn(delay, restoreSwitches)
}