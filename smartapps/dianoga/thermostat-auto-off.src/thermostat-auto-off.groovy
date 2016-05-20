/**
*  HVAC Auto Off
*
*  Author: dianoga7@3dgo.net
*  Date: 2013-07-21
*
*  Additional code: Philip Rosenberg-Watt
*  Date: 2016-05-20
*/

// Automatically generated. Make future change here.
definition(
    name: "Thermostat Auto Off",
    namespace: "dianoga",
    author: "dianoga7@3dgo.net",
    description: "Automatically turn off thermostat when windows/doors open. Turn it back on when everything is closed up.",
    category: "Green Living",
    iconUrl: "http://cdn.device-icons.smartthings.com/Home/home1-icn.png",
    iconX2Url: "http://cdn.device-icons.smartthings.com/Home/home1-icn@2x.png",
    oauth: true
)


preferences {
    section("Control") {
        input "thermostat", "capability.thermostat", title: "Thermostat" 
    }
    section("Open/Close") {
        input "sensors", "capability.contactSensor", title: "Sensors", multiple: true 
        input "delay", "number", title: "Delay (seconds)"
    }
}


def installed() {
    log.debug "Installed with settings: ${settings}"

    initialize()
}


def updated() {
    log.debug "Updated with settings: ${settings}"

    unsubscribe()
    initialize()
}


def initialize() {
    subscribe(sensors, "contact", contactHandler)
}


def contactHandler(evt) {
    log.trace "contactHandler(${evt})"
    log.debug "Desc: ${evt.value}, state: ${state}"

    if (state.changed && evt.value == "closed") {
        log.debug "Closed event received, scheduling restoration..."
        runIn(delay, restore)
    } else if (!state.changed && evt.value == "open") {
        log.debug "Open event received. Scheduling shutdown..."
        runIn(delay, turnOff)
    }
}


def allClosed() {
    log.trace "allClosed()"

    def openSensors = sensors.findAll { it?.currentValue("contact") == "open" }
    def allClosed = (openSensors.size() == 0)

    log.debug "Number of open sensors: ${openSensors.size()}, returning ${allClosed}"
    return allClosed
}


def turnOff() {
    log.trace "turnOff()"

    if (!allClosed()) {
        log.debug "Turning off thermostat"
        state.thermostatMode = thermostat.currentValue("thermostatMode")
        thermostat.off()
        state.changed = true

        log.debug "State: $state"
    } else {
        log.debug "All sensors are closed. Doing nothing."
    }
}


def restore() {
    log.trace "restore()"


    if (allClosed()) {
        log.debug "Restoring thermostat to $state.thermostatMode"
        thermostat.setThermostatMode(state.thermostatMode)
        state.changed = false
    } else {
        log.debug "Not all sensors are closed. Doing nothing."
    }
}