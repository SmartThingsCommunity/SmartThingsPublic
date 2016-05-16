/**
 *  Hue Advanced -CD- Bulb/Group
 *
 *  Philips Hue Type "Extended Color Light"
 *  Philips Hue Type "LightGroup"
 *  Philips Hue Type "Room"
 *
 *  Author: claytonjn
 */

// for the UI
metadata {
	// Automatically generated. Make future change here.
	definition (name: "Hue Advanced -CD- Bulb/Group", namespace: "claytonjn", author: "claytonjn") {
		capability "Switch Level"
		capability "Actuator"
		capability "Color Control"
		capability "Color Temperature"
		capability "Switch"
		capability "Refresh"
		capability "Sensor"

		command "setAdjustedColor"
        command "reset"
        command "refresh"
		command "setTransitionTime"
		command "enableCDBrightness"
        command "disableCDBrightness"
		command "enableCDColor"
		command "disableCDColor"
		command "tileSetLevel"
		command "tileSetAdjustedColor"
		command "tileReset"
		command "tileSetColorTemperature"

		attribute "deviceSwitch", "enum", ["lightsOn", "lightsOff", "lightsTurningOn", "lightsTurningOff", "groupsOn", "groupsOff", "groupsTurningOn", "groupsTurningOff"]
		attribute "transitionTime", "NUMBER"
		attribute "cdBrightness", "enum", ["true", "false"]
		attribute "cdColor", "enum", ["true", "false"]
	}

	simulator {
		// TODO: define status and reply messages here
	}

	tiles (scale: 2){
		multiAttributeTile(name:"rich-control", type: "lighting", width: 6, height: 4, canChangeIcon: true){
			tileAttribute ("deviceSwitch", key: "PRIMARY_CONTROL") {
				attributeState "lightsOn", label:'ON', action:"switch.off", icon:"st.lights.philips.hue-single", backgroundColor:"#79b821", nextState:"lightsTurningOff"
				attributeState "lightsOff", label:'OFF', action:"switch.on", icon:"st.lights.philips.hue-single", backgroundColor:"#ffffff", nextState:"lightsTurningOn"
				attributeState "lightsTurningOn", label:'TURNING ON', action:"switch.off", icon:"st.lights.philips.hue-single", backgroundColor:"#79b821", nextState:"lightsTurningOff"
				attributeState "lightsTurningOff", label:'TURNING OFF', action:"switch.on", icon:"st.lights.philips.hue-single", backgroundColor:"#ffffff", nextState:"lightsTurningOn"
				attributeState "groupsOn", label:'ON', action:"switch.off", icon:"st.lights.philips.hue-multi", backgroundColor:"#79b821", nextState:"groupsTurningOff"
				attributeState "groupsOff", label:'OFF', action:"switch.on", icon:"st.lights.philips.hue-multi", backgroundColor:"#ffffff", nextState:"groupsTurningOn"
				attributeState "groupsTurningOn", label:'TURNING ON', action:"switch.off", icon:"st.lights.philips.hue-multi", backgroundColor:"#79b821", nextState:"groupsTurningOff"
				attributeState "groupsTurningOff", label:'TURNING OFF', action:"switch.on", icon:"st.lights.philips.hue-multi", backgroundColor:"#ffffff", nextState:"groupsTurningOn"
			}
			tileAttribute ("device.level", key: "SLIDER_CONTROL") {
				attributeState "level", action:"tileSetLevel", range:"(0..100)"
            }
			tileAttribute ("device.color", key: "COLOR_CONTROL") {
				attributeState "color", action:"tileSetAdjustedColor"
			}
		}

        controlTile("colorTempSliderControl", "device.colorTemperature", "slider", width: 4, height: 1, inactiveLabel: false, range:"(2000..6500)") {
            state "colorTemperature", action:"tileSetColorTemperature"
        }
        valueTile("colorTemp", "device.colorTemperature", inactiveLabel: false, decoration: "flat", width: 2, height: 1) {
            state "colorTemperature", label: 'Temperature: ${currentValue} K'
        }

		standardTile("reset", "device.reset", height: 2, width: 2, inactiveLabel: false, decoration: "flat") {
			state "default", label:"Reset Color", action:"tileReset", icon:"st.lights.philips.hue-single"
		}

		standardTile("refresh", "device.refresh", height: 2, width: 2, inactiveLabel: false, decoration: "flat") {
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

		valueTile("cdColorControl", "device.cdColor", inactiveLabel: false, decoration: "flat", width: 3, height: 1) {
			state "true", label: "Circadian Color On", action: "disableCDColor", nextState: "updating"
			state "false", label: "Circadian Color Off", action: "enableCDColor", nextState: "updating"
			state "updating", label: "Working"
		}

		main(["rich-control"])
		details(["rich-control", "colorTempSliderControl", "colorTemp", "transitionTimeSliderControl", "transTime", "cdBrightnessControl", "cdColorControl", "reset", "refresh"])
	}
}

// parse events into attributes
def parse(description) {
	log.debug "parse() - $description"
	def results = []

	def map = description
	if (description instanceof String)  {
		log.debug "Hue Advanced Bulb/Group stringToMap - ${map}"
		map = stringToMap(description)
	}

	if (map?.name && map?.value) {
		results << createEvent(name: "${map?.name}", value: "${map?.value}")
		if (map?.name == "switch") { results << createEvent(name: "deviceSwitch", value: state.deviceType + map?.value.capitalize(), displayed: false) }
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

	log.trace parent.on(this, transitionTime, state.deviceType)
	sendEvent(name: "deviceSwitch", value: "${state.deviceType}On", displayed: false)
	sendEvent(name: "switch", value: "on")
}

void off(transitionTime = device.currentValue("transitionTime")) {
	if(transitionTime == null) { transitionTime = device.currentValue("transitionTime") ?: parent.getSelectedTransition() ?: 1 }

	log.trace parent.off(this, transitionTime, state.deviceType)
	sendEvent(name: "deviceSwitch", value: "${state.deviceType}Off", displayed: false)
	sendEvent(name: "switch", value: "off")
}

void nextLevel(transitionTime = device.currentValue("transitionTime"), disableCDB = false) {
	if(transitionTime == null) { transitionTime = device.currentValue("transitionTime") ?: parent.getSelectedTransition() ?: 1 }

	def level = device.latestValue("level") as Integer ?: 0
	if (level <= 100) {
		level = Math.min(25 * (Math.round(level / 25) + 1), 100) as Integer
	}
	else {
		level = 25
	}
	setLevel(level, transitionTime, disableCDB)
}

void tileSetLevel(percent) {
	setLevel(percent, null, true)
}

void setLevel(percent, transitionTime = device.currentValue("transitionTime"), disableCDB = false) {
	if(transitionTime == null) { transitionTime = device.currentValue("transitionTime") ?: parent.getSelectedTransition() ?: 1 }

    log.debug "Executing 'setLevel'"
    if (verifyPercent(percent)) {
		if (percent == 0) {
			off()
		} else {
	        parent.setLevel(this, percent, transitionTime, state.deviceType)
			if(disableCDB == true) { disableCDBrightness() }
	        sendEvent(name: "level", value: percent, descriptionText: "Level has changed to ${percent}%")
			sendEvent(name: "deviceSwitch", value: "${state.deviceType}On", displayed: false)
			sendEvent(name: "switch", value: "on")
		}
    }
}

void setSaturation(percent, transitionTime = device.currentValue("transitionTime"), disableCDC = false) {
	if(transitionTime == null) { transitionTime = device.currentValue("transitionTime") ?: parent.getSelectedTransition() ?: 1 }

    log.debug "Executing 'setSaturation'"
    if (verifyPercent(percent)) {
        parent.setSaturation(this, percent, transitionTime, state.deviceType)
		if(disableCDC == true) { disableCDColor() }
        sendEvent(name: "saturation", value: percent, displayed: false)
    }
}

void setHue(percent, transitionTime = device.currentValue("transitionTime"), disableCDC = false) {
	if(transitionTime == null) { transitionTime = device.currentValue("transitionTime") ?: parent.getSelectedTransition() ?: 1 }

    log.debug "Executing 'setHue'"
    if (verifyPercent(percent)) {
        parent.setHue(this, percent, transitionTime, state.deviceType)
		if(disableCDC == true) { disableCDColor() }
        sendEvent(name: "hue", value: percent, displayed: false)
    }
}

void setColor(value) {
    log.debug "setColor: ${value}, $this"
    def events = []
    def validValues = [:]

	if (value.transitionTime) { validValues.transitionTime = value.transitionTime }
	else {
		def transitionTime = (device.currentValue("transitionTime")) ?: parent.getSelectedTransition() ?: 3
		validValues.transitionTime = transitionTime
	}
    if (verifyPercent(value.hue)) {
        events << createEvent(name: "hue", value: value.hue, displayed: false)
        validValues.hue = value.hue
    }
    if (verifyPercent(value.saturation)) {
        events << createEvent(name: "saturation", value: value.saturation, displayed: false)
        validValues.saturation = value.saturation
    }
    if (value.hex != null) {
        if (value.hex ==~ /^\#([A-Fa-f0-9]){6}$/) {
            events << createEvent(name: "color", value: value.hex)
            validValues.hex = value.hex
        } else {
            log.warn "$value.hex is not a valid color"
        }
    }
    if (verifyPercent(value.level) && value.level > 0) {
        events << createEvent(name: "level", value: value.level, descriptionText: "Level has changed to ${value.level}%")
        validValues.level = value.level
    }
    if (value.switch == "off" || (value.level != null && value.level <= 0)) {
		events << createEvent(name: "deviceSwitch", value: "${state.deviceType}Off", displayed: false)
		events << createEvent(name: "switch", value: "off")
        validValues.switch = "off"
    } else {
		events << createEvent(name: "deviceSwitch", value: "${state.deviceType}On", displayed: false)
		events << createEvent(name: "switch", value: "on")
        validValues.switch = "on"
    }
	if (value.disableCDBrightness == true) { disableCDBrightness() }
	if (value.disableCDColor == true) { disableCDColor() }
    if (!events.isEmpty()) {
        parent.setColor(this, validValues, state.deviceType)
    }
    events.each {
        sendEvent(it)
    }
}

private tileReset() {
	reset(null, true)
}

void reset(transitionTime = device.currentValue("transitionTime"), disableCDC = false) {
	if(transitionTime == null) { transitionTime = device.currentValue("transitionTime") ?: parent.getSelectedTransition() ?: 1 }

    log.debug "Executing 'reset'"
    setColorTemperature(2710, transitionTime, disableCDC)
    parent.poll()
}

private tileSetAdjustedColor(value) {
	value.disableCDColor = true
	setAdjustedColor(value)
}

void setAdjustedColor(value) {
    if (value) {
        log.trace "setAdjustedColor: ${value}"
        def adjusted = value + [:]
        adjusted.hue = adjustOutgoingHue(value.hue)
        // Needed because color picker always sends 100
        adjusted.level = null
        setColor(adjusted)
    } else {
        log.warn "Invalid color input"
    }
}

private tileSetColorTemperature(value) {
	setColorTemperature(value, null, true)
}

void setColorTemperature(value, transitionTime = device.currentValue("transitionTime"), disableCDC = false) {
	if(transitionTime == null) { transitionTime = device.currentValue("transitionTime") ?: parent.getSelectedTransition() ?: 1 }

    if (value >= 0) {
        log.trace "setColorTemperature: ${value}k"
        parent.setColorTemperature(this, value, transitionTime, state.deviceType)
		if(disableCDC == true) { disableCDColor() }
        sendEvent(name: "colorTemperature", value: value)
		sendEvent(name: "deviceSwitch", value: "${state.deviceType}On", displayed: false)
		sendEvent(name: "switch", value: "on")
    } else {
        log.warn "Invalid color temperature"
    }
}

void refresh() {
    log.debug "Executing 'refresh'"
    parent.manualRefresh()
}

def adjustOutgoingHue(percent) {
	def adjusted = percent
	if (percent > 31) {
		if (percent < 63.0) {
			adjusted = percent + (7 * (percent -30 ) / 32)
		}
		else if (percent < 73.0) {
			adjusted = 69 + (5 * (percent - 62) / 10)
		}
		else {
			adjusted = percent + (2 * (100 - percent) / 28)
		}
	}
	log.info "percent: $percent, adjusted: $adjusted"
	adjusted
}

def verifyPercent(percent) {
    if (percent == null)
        return false
    else if (percent >= 0 && percent <= 100) {
        return true
    } else {
        log.warn "$percent is not 0-100"
        return false
    }
}

void initialize(deviceType) {
	state.deviceType = deviceType
	setTransitionTime(parent.getSelectedTransition())
	sendEvent(name: "cdBrightness", value: "true", displayed: false)
	sendEvent(name: "cdColor", value: "true", displayed: false)
}

void enableCDBrightness() {
	log.debug "Executing 'enableCDBrightness'"
	sendEvent(name: "cdBrightness", value: "true", descriptionText: "Circadian Brightness has been enabled")
}

void disableCDBrightness() {
	log.debug "Executing 'disableCDBrightness'"
	sendEvent(name: "cdBrightness", value: "false", descriptionText: "Circadian Brightness has been disabled")
}

void enableCDColor() {
	log.debug "Executing 'enableCDColor'"
	sendEvent(name: "cdColor", value: "true", descriptionText: "Circadian Color has been enabled")
}

void disableCDColor() {
	log.debug "Executing 'disableCDColor'"
	sendEvent(name: "cdColor", value: "false", descriptionText: "Circadian Color has been disabled")
}