/**
 *  Hue Advanced -CD- Bloom
 *
 *  Philips Hue Type "Color Light"
 *
 *  Author: claytonjn
 */

// for the UI
metadata {
	// Automatically generated. Make future change here.
	definition (name: "Hue Advanced -CD- Bloom", namespace: "claytonjn", author: "claytonjn") {
		capability "Switch Level"
		capability "Actuator"
		capability "Color Control"
		capability "Switch"
		capability "Refresh"
		capability "Sensor"

		command "setAdjustedColor"
        command "reset"
        command "refresh"
		command "setTransitionTime"
		command "enableCDColor"
		command "disableCDColor"
		command "tileSetAdjustedColor"

		attribute "transitionTime", "NUMBER"
		attribute "cdColor", "enum", ["true", "false"]
	}

	simulator {
		// TODO: define status and reply messages here
	}

	tiles (scale: 2){
		multiAttributeTile(name:"rich-control", type: "lighting", width: 6, height: 4, canChangeIcon: true){
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
			tileAttribute ("device.color", key: "COLOR_CONTROL") {
				attributeState "color", action:"tileSetAdjustedColor"
			}
		}

		standardTile("reset", "device.reset", height: 2, width: 2, inactiveLabel: false, decoration: "flat") {
			state "default", label:"Reset Color", action:"reset", icon:"st.lights.philips.hue-single"
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

		valueTile("cdColorControl", "device.cdColor", inactiveLabel: false, decoration: "flat", width: 3, height: 1) {
			state "true", label: "Circadian Color On", action: "disableCDColor", nextState: "updating"
			state "false", label: "Circadian Color Off", action: "enableCDColor", nextState: "updating"
			state "updating", label: "Working"
		}

		main(["rich-control"])
		details(["rich-control", "transitionTimeSliderControl", "transTime", "cdColorControl", "reset", "refresh"])
	}
}

// parse events into attributes
def parse(description) {
	log.debug "parse() - $description"
	def results = []

	def map = description
	if (description instanceof String)  {
		log.debug "Hue Advanced Bloom stringToMap - ${map}"
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

void nextLevel(transitionTime = device.currentValue("transitionTime")) {
	if(transitionTime == null) { transitionTime = parent.getSelectedTransition() ?: 1 }

	def level = device.latestValue("level") as Integer ?: 0
	if (level <= 100) {
		level = Math.min(25 * (Math.round(level / 25) + 1), 100) as Integer
	}
	else {
		level = 25
	}
	setLevel(level, transitionTime)
}

void setLevel(percent, transitionTime = device.currentValue("transitionTime")) {
	if(transitionTime == null) { transitionTime = parent.getSelectedTransition() ?: 1 }

    log.debug "Executing 'setLevel'"
    if (verifyPercent(percent)) {
        parent.setLevel(this, percent, transitionTime, deviceType)
        sendEvent(name: "level", value: percent, descriptionText: "Level has changed to ${percent}%")
        sendEvent(name: "switch", value: "on")
    }
}

void setSaturation(percent, transitionTime = device.currentValue("transitionTime"), disableCDColor = false) {
	if(transitionTime == null) { transitionTime = parent.getSelectedTransition() ?: 1 }

    log.debug "Executing 'setSaturation'"
    if (verifyPercent(percent)) {
        parent.setSaturation(this, percent, transitionTime, deviceType)
		if(disableCDColor == true) { sendEvent(name: "cdColor", value: "false", descriptionText: "Circadian Color has been disabled") }
        sendEvent(name: "saturation", value: percent, displayed: false)
    }
}

void setHue(percent, transitionTime = device.currentValue("transitionTime"), disableCDColor = false) {
	if(transitionTime == null) { transitionTime = parent.getSelectedTransition() ?: 1 }

    log.debug "Executing 'setHue'"
    if (verifyPercent(percent)) {
        parent.setHue(this, percent, transitionTime, deviceType)
		if(disableCDColor == true) { sendEvent(name: "cdColor", value: "false", descriptionText: "Circadian Color has been disabled") }
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
    if (verifyPercent(value.level)) {
        events << createEvent(name: "level", value: value.level, descriptionText: "Level has changed to ${value.level}%")
        validValues.level = value.level
    }
    if (value.switch == "off" || (value.level != null && value.level <= 0)) {
        events << createEvent(name: "switch", value: "off")
        validValues.switch = "off"
    } else {
        events << createEvent(name: "switch", value: "on")
        validValues.switch = "on"
    }
	if (value.disableCDColor == true) { events << createEvent(name: "cdColor", value: "false", descriptionText: "Circadian Color has been disabled") }
    if (!events.isEmpty()) {
        parent.setColor(this, validValues, deviceType)
    }
    events.each {
        sendEvent(it)
    }
}

void reset() {
    log.debug "Executing 'reset'"
    def value = [level:100, saturation:18, hue:8]
    setAdjustedColor(value)
    parent.poll()
}

void tileSetAdjustedColor(value) {
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
	setTransitionTime(parent.getSelectedTransition())
	sendEvent(name: "cdColor", value: "true", displayed: false)
}

def getDeviceType() { return "lights" }

void enableCDColor() {
	log.debug "Executing 'enableCDColor'"
	sendEvent(name: "cdColor", value: "true", descriptionText: "Circadian Color has been enabled")
}

void disableCDColor() {
	log.debug "Executing 'disableCDColor'"
	sendEvent(name: "cdColor", value: "false", descriptionText: "Circadian Color has been disabled")
}