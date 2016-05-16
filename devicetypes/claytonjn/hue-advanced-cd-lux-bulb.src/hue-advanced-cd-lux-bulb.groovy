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
        command "enableCDBrightness"
        command "disableCDBrightness"
        command "tileSetLevel"

		attribute "transitionTime", "NUMBER"
        attribute "cdBrightness", "enum", ["true", "false"]
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
              attributeState "level", action:"tileSetLevel", range:"(0..100)"
            }
        }

        controlTile("levelSliderControl", "device.level", "slider", height: 1, width: 2, inactiveLabel: false, range:"(0..100)") {
            state "level", action:"tileSetLevel"
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

        valueTile("cdBrightnessControl", "device.cdBrightness", inactiveLabel: false, decoration: "flat", width: 3, height: 1) {
			state "true", label: "Circadian Brightness On", action: "disableCDBrightness", nextState: "updating"
			state "false", label: "Circadian Brightness Off", action: "enableCDBrightness", nextState: "updating"
			state "updating", label: "Working"
		}

        main(["rich-control"])
        details(["rich-control", "transitionTimeSliderControl", "transTime", "refresh", "cdBrightnessControl"])
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
	if(transitionTime == null) { transitionTime = device.currentValue("transitionTime") ?: parent.getSelectedTransition() ?: 1 }

	log.trace parent.on(this, transitionTime, deviceType)
	sendEvent(name: "switch", value: "on")
}

void off(transitionTime = device.currentValue("transitionTime")) {
	if(transitionTime == null) { transitionTime = device.currentValue("transitionTime") ?: parent.getSelectedTransition() ?: 1 }

	log.trace parent.off(this, transitionTime, deviceType)
	sendEvent(name: "switch", value: "off")
}

void tileSetLevel(percent) {
	setLevel(percent, null, true)
}

void setLevel(percent, transitionTime = device.currentValue("transitionTime"), disableCDB = false) {
	if(transitionTime == null) { transitionTime = device.currentValue("transitionTime") ?: parent.getSelectedTransition() ?: 1 }

	log.debug "Executing 'setLevel'"
    if (percent != null && percent >= 0 && percent <= 100) {
		if (percent == 0) {
			off()
		} else {
			parent.setLevel(this, percent, transitionTime, deviceType)
			if(disableCDB == true) { disableCDBrightness() }
			sendEvent(name: "level", value: percent)
			sendEvent(name: "switch", value: "on")
		}
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
	sendEvent(name: "cdBrightness", value: "true", displayed: false)
}

def getDeviceType() { return "lights" }

void enableCDBrightness() {
	log.debug "Executing 'enableCDBrightness'"
	sendEvent(name: "cdBrightness", value: "true", descriptionText: "Circadian Brightness has been enabled")
}

void disableCDBrightness() {
	log.debug "Executing 'disableCDBrightness'"
	sendEvent(name: "cdBrightness", value: "false", descriptionText: "Circadian Brightness has been disabled")
}