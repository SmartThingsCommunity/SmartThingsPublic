/**
 *  Hue Advanced Lux Light
 *
 *  Philips Hue Type "Dimmable Light"
 *
 *  Author: claytonjn
 */
// for the UI
metadata {
	// Automatically generated. Make future change here.
	definition (name: "Hue Advanced Lux Light", namespace: "claytonjn", author: "claytonjn") {
		capability "Switch Level"
		capability "Actuator"
		capability "Switch"
		capability "Refresh"
		capability "Sensor"
		capability "Health Check"
		capability "Light"

        command "reset"
		command "refresh"
		command "setTransitionTime"
		command "alert"
		command "bri_inc"

		attribute "transitionTime", "NUMBER"
		attribute "reachable", "enum", ["true", "false"]
	}

	simulator {
		// TODO: define status and reply messages here
	}

    tiles(scale: 2) {
        multiAttributeTile(name:"rich-control", type: "lighting", canChangeIcon: true){
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

        controlTile("levelSliderControl", "device.level", "slider", height: 1, width: 2, inactiveLabel: false, range:"(0..100)") {
            state "level", action:"switch level.setLevel"
        }

		standardTile("reset", "device.reset", height: 2, width: 2, inactiveLabel: false, decoration: "flat") {
			state "default", label:"Reset", action:"reset", icon:"st.lights.philips.hue-single"
		}

        standardTile("refresh", "device.switch", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
            state "default", label:"", action:"refresh.refresh", icon:"st.secondary.refresh"
        }

		controlTile("transitionTimeSliderControl", "device.transitionTime", "slider", width: 4, height: 1, inactiveLabel: false, range:"(0..10)") {
			state "setTransitionTime", action: "setTransitionTime"
		}
		valueTile("transTime", "device.transitionTime", inactiveLabel: false, decoration: "flat", width: 2, height: 1) {
			state "transitionTime", label: 'Transition:              ${currentValue} s'
		}

		valueTile("reachable", "device.reachable", inactiveLabel: false, decoration: "flat", width: 2, height: 1) {
			state "true", label: 'Reachable'
			state "false", label: 'Not Reachable!'
		}

        main(["rich-control"])
        details(["rich-control", "transitionTimeSliderControl", "transTime", "reachable", "reset", "refresh"])
    }
}

void installed() {
	sendEvent(name: "DeviceWatch-Enroll", value: "{\"protocol\": \"LAN\", \"scheme\":\"untracked\", \"hubHardwareId\": \"${device.hub.hardwareID}\"}", displayed: false)
}

// parse events into attributes
def parse(description) {
	log.debug "parse() - $description"
	def results = []

	def map = description
	if (description instanceof String)  {
		log.debug "Hue Advanced Lux Light stringToMap - ${map}"
		map = stringToMap(description)
	}

	if (map?.name && map?.value) {
		results << createEvent(name: "${map?.name}", value: "${map?.value}")
	}
	results
}

// handle commands
void setTransitionTime(transitionTime) {
	log.trace "Transition time set to ${transitionTime}"
	sendEvent(name: "transitionTime", value: transitionTime)
}

void on(transitionTime = device.currentValue("transitionTime")) {
	if(transitionTime == null) { transitionTime = device.currentValue("transitionTime") ?: parent.getSelectedTransition() ?: 1 }

	log.trace parent.on(this, transitionTime, deviceType)
}

void off(transitionTime = device.currentValue("transitionTime")) {
	if(transitionTime == null) { transitionTime = device.currentValue("transitionTime") ?: parent.getSelectedTransition() ?: 1 }

	log.trace parent.off(this, transitionTime, deviceType)
}

void setLevel(percent, transitionTime = device.currentValue("transitionTime")) {
	if(transitionTime == null) { transitionTime = device.currentValue("transitionTime") ?: parent.getSelectedTransition() ?: 1 }

	log.debug "Executing 'setLevel'"
    if (percent != null && percent >= 0 && percent <= 100) {
		if (percent == 0) {
			off()
		} else {
			parent.setLevel(this, percent, transitionTime, deviceType)
		}
	} else {
    	log.warn "$percent is not 0-100"
    }
}

void reset(transitionTime = device.currentValue("transitionTime")) {
	if(transitionTime == null) { transitionTime = device.currentValue("transitionTime") ?: parent.getSelectedTransition() ?: 1 }

    log.debug "Executing 'reset'"
	setLevel(100, transitionTime)
    parent.poll()
}

void refresh() {
	log.debug "Executing 'refresh'"
	parent.manualRefresh()
}

void alert(alert) {
	log.debug "Executing 'alert'"
	parent.setAlert(this, alert, deviceType)
}

void bri_inc(value) {
	log.debug "Executing 'bri_inc'"
	parent.bri_inc(this, value, deviceType)
}

void initialize(deviceType) {
	setTransitionTime(parent.getSelectedTransition())
}

def getDeviceType() { return "lights" }

void setHADeviceHandler(circadianDaylightIntegration) {
	if (circadianDaylightIntegration == true) {
		setDeviceType("Hue Advanced -CD- Lux Light")
	} else {
		setDeviceType("Hue Advanced Lux Light")
	}
}