/**
 *  Hue Advanced -CD- Lux Bulb
 *
 *  Philips Hue Type "Dimmable Light"
 *
 *  Author: claytonjn
 */
// for the UI
metadata {
	// Automatically generated. Make future change here.
	definition (name: "Hue Advanced -CD- Lux Bulb", namespace: "claytonjn", author: "claytonjn") {
		capability "Switch Level"
		capability "Actuator"
		capability "Switch"
		capability "Refresh"
		capability "Sensor"

        command "refresh"
		command "setTransitionTime"

		attribute "transitionTime", "NUMBER"
	}

	simulator {
		// TODO: define status and reply messages here
	}

    tiles(scale: 2) {
        multiAttributeTile(name:"rich-control", type: "lighting", canChangeIcon: true){
            tileAttribute ("device.switch", key: "PRIMARY_CONTROL") {
              attributeState "on", label:'${name}', action:"switch.off", icon:"st.lights.philips.hue-single", backgroundColor:"#79b821", nextState:"turningOff"
              attributeState "off", label:'${name}', action:"switch.on", icon:"st.lights.philips.hue-single", backgroundColor:"#ffffff", nextState:"turningOn"
              attributeState "turningOn", label:'${name}', action:"switch.off", icon:"st.lights.philips.hue-single", backgroundColor:"#79b821", nextState:"turningOff"
              attributeState "turningOff", label:'${name}', action:"switch.on", icon:"st.lights.philips.hue-single", backgroundColor:"#ffffff", nextState:"turningOn"
            }
            tileAttribute ("device.level", key: "SLIDER_CONTROL") {
              attributeState "level", action:"switch level.setLevel", range:"(0..100)"
            }
            tileAttribute ("device.level", key: "SECONDARY_CONTROL") {
	            attributeState "level", label: 'Level ${currentValue}%'
			}
        }

        controlTile("levelSliderControl", "device.level", "slider", height: 1, width: 2, inactiveLabel: false, range:"(0..100)") {
            state "level", action:"switch level.setLevel"
        }

        standardTile("refresh", "device.switch", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
            state "default", label:"", action:"refresh.refresh", icon:"st.secondary.refresh"
        }

		controlTile("transitionTimeSliderControl", "device.transitionTime", "slider", width: 5, height: 1, inactiveLabel: false, range:"(0..10)") {
			state "setTransitionTime", action: "setTransitionTime"
		}
		valueTile("transTime", "device.transitionTime", inactiveLabel: false, decoration: "flat", width: 1, height: 1) {
			state "transitionTime", label: 'Transition Time: ${currentValue} s'
		}

        main(["rich-control"])
        details(["rich-control", "transitionTimeSliderControl", "transTime", "refresh"])
    }
}

// parse events into attributes
def parse(description) {
	log.debug "parse() - $description"
	def results = []

	def map = description
	if (description instanceof String)  {
		log.debug "Hue Advanced Lux Bulb stringToMap - ${map}"
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
	if(transitionTime == null) { transitionTime = parent.getSelectedTransition() ?: 1 }

	log.trace parent.on(this, transitionTime, deviceType)
	sendEvent(name: "switch", value: "on")
}

void off(transitionTime = device.currentValue("transitionTime")) {
	if(transitionTime == null) { transitionTime = parent.getSelectedTransition() ?: 1 }

	log.trace parent.off(this, transitionTime, deviceType)
	sendEvent(name: "switch", value: "off")
}

void setLevel(percent, transitionTime = device.currentValue("transitionTime")) {
	if(transitionTime == null) { transitionTime = parent.getSelectedTransition() ?: 1 }

	log.debug "Executing 'setLevel'"
    if (percent != null && percent >= 0 && percent <= 100) {
		parent.setLevel(this, percent, transitionTime, deviceType)
		sendEvent(name: "level", value: percent)
		sendEvent(name: "switch", value: "on")
	} else {
    	log.warn "$percent is not 0-100"
    }
}

void refresh() {
	log.debug "Executing 'refresh'"
	parent.manualRefresh()
}

void initialize(deviceType) {
	setTransitionTime(parent.getSelectedTransition())
}

def getDeviceType() { return "lights" }