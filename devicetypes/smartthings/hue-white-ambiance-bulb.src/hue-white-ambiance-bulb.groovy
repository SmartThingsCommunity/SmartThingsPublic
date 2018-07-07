//DEPRECATED. INTEGRATION MOVED TO SUPER LAN CONNECT

/**
 *  Hue White Ambiance Bulb
 *
 *  Philips Hue Type "Color Temperature Light"
 *
 *  Author: SmartThings
 */

// for the UI
metadata {
    // Automatically generated. Make future change here.
    definition (name: "Hue White Ambiance Bulb", namespace: "smartthings", author: "SmartThings") {
        capability "Switch Level"
        capability "Actuator"
        capability "Color Temperature"
        capability "Switch"
        capability "Refresh"
        capability "Health Check"
        capability "Light"

        command "refresh"
    }

    simulator {
        // TODO: define status and reply messages here
    }

    tiles (scale: 2){
        multiAttributeTile(name:"rich-control", type: "lighting", width: 6, height: 4, canChangeIcon: true){
            tileAttribute ("device.switch", key: "PRIMARY_CONTROL") {
                attributeState "on", label:'${name}', action:"switch.off", icon:"st.lights.philips.hue-single", backgroundColor:"#00A0DC", nextState:"turningOff"
                attributeState "off", label:'${name}', action:"switch.on", icon:"st.lights.philips.hue-single", backgroundColor:"#ffffff", nextState:"turningOn"
                attributeState "turningOn", label:'${name}', action:"switch.off", icon:"st.lights.philips.hue-single", backgroundColor:"#00A0DC", nextState:"turningOff"
                attributeState "turningOff", label:'${name}', action:"switch.on", icon:"st.lights.philips.hue-single", backgroundColor:"#ffffff", nextState:"turningOn"
            }
            tileAttribute ("device.level", key: "SLIDER_CONTROL") {
                attributeState "level", action:"switch level.setLevel", range:"(0..100)"
            }
        }

        controlTile("colorTempSliderControl", "device.colorTemperature", "slider", width: 4, height: 2, inactiveLabel: false, range:"(2200..6500)") {
            state "colorTemperature", action:"color temperature.setColorTemperature"
        }

        valueTile("colorTemp", "device.colorTemperature", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
            state "colorTemperature", label: 'WHITES'
        }

        standardTile("refresh", "device.refresh", height: 2, width: 2, inactiveLabel: false, decoration: "flat") {
            state "default", label:"", action:"refresh.refresh", icon:"st.secondary.refresh"
        }

        main(["rich-control"])
        details(["rich-control", "colorTempSliderControl", "colorTemp", "refresh"])
    }
}

def initialize() {
	sendEvent(name: "DeviceWatch-Enroll", value: "{\"protocol\": \"LAN\", \"scheme\":\"untracked\", \"hubHardwareId\": \"${device.hub.hardwareID}\"}", displayed: false)
}

void installed() {
    log.debug "installed()"
    initialize()
}

def updated() {
    log.debug "updated()"
    initialize()
}

// parse events into attributes
def parse(description) {
    log.debug "parse() - $description"
    def results = []

    def map = description
    if (description instanceof String)  {
        log.debug "Hue Ambience Bulb stringToMap - ${map}"
        map = stringToMap(description)
    }

    if (map?.name && map?.value) {
        results << createEvent(name: "${map?.name}", value: "${map?.value}")
    }
    results
}

// handle commands
void on() {
    log.trace parent.on(this)
}

void off() {
    log.trace parent.off(this)
}

void setLevel(percent) {
    log.debug "Executing 'setLevel'"
    if (percent != null && percent >= 0 && percent <= 100) {
        log.trace parent.setLevel(this, percent)
    } else {
        log.warn "$percent is not 0-100"
    }
}

void setColorTemperature(value) {
    if (value) {
        log.trace "setColorTemperature: ${value}k"
        log.trace parent.setColorTemperature(this, value)
    } else {
        log.warn "Invalid color temperature"
    }
}

void refresh() {
    log.debug "Executing 'refresh'"
    parent.manualRefresh()
}

