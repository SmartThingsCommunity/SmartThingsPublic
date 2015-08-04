/**
 *  Hue Bulb
 *
 *  Author: SmartThings
 */
// for the UI
metadata {
	// Automatically generated. Make future change here.
	definition (name: "Hue Bulb", namespace: "smartthings", author: "SmartThings") {
		capability "Switch Level"
		capability "Actuator"
		capability "Color Control"
		capability "Switch"
		capability "Refresh"
		capability "Sensor"

		command "setAdjustedColor"
        command "reset"        
        command "refresh"
	}

	simulator {
		// TODO: define status and reply messages here
	}

	standardTile("switch", "device.switch", width: 2, height: 2, canChangeIcon: true) {
		state "on", label:'${name}', action:"switch.off", icon:"st.lights.philips.hue-single", backgroundColor:"#79b821"
		state "off", label:'${name}', action:"switch.on", icon:"st.lights.philips.hue-single", backgroundColor:"#ffffff"
	}
	standardTile("reset", "device.reset", inactiveLabel: false, decoration: "flat") {
		state "default", label:"Color Reset", action:"reset", icon:"st.lights.philips.hue-single"
	}
	standardTile("refresh", "device.switch", inactiveLabel: false, decoration: "flat") {
		state "default", label:"", action:"refresh.refresh", icon:"st.secondary.refresh"
	}
	controlTile("rgbSelector", "device.color", "color", height: 3, width: 3, inactiveLabel: false) {
		state "color", action:"setAdjustedColor"
	}
	controlTile("levelSliderControl", "device.level", "slider", height: 1, width: 2, inactiveLabel: false, range:"(0..100)") {
		state "level", action:"switch level.setLevel"
	}
	valueTile("level", "device.level", inactiveLabel: false, decoration: "flat") {
		state "level", label: 'Level ${currentValue}%'
	}
	controlTile("saturationSliderControl", "device.saturation", "slider", height: 1, width: 2, inactiveLabel: false) {
		state "saturation", action:"color control.setSaturation"
	}
	valueTile("saturation", "device.saturation", inactiveLabel: false, decoration: "flat") {
		state "saturation", label: 'Sat ${currentValue}    '
	}
	controlTile("hueSliderControl", "device.hue", "slider", height: 1, width: 2, inactiveLabel: false) {
		state "hue", action:"color control.setHue"
	}
	valueTile("hue", "device.hue", inactiveLabel: false, decoration: "flat") {
		state "hue", label: 'Hue ${currentValue}   '
	}

	main(["switch"])
	details(["switch", "levelSliderControl", "rgbSelector", "refresh", "reset"])

}

// parse events into attributes
def parse(description) {
	log.debug "parse() - $description"
	def results = []
	def map = description
	if (description instanceof String)  {
		log.debug "Hue Bulb stringToMap - ${map}"
		map = stringToMap(description)
	}
	if (map?.name && map?.value) {
		results << createEvent(name: "${map?.name}", value: "${map?.value}")
	}
	results
}

// handle commands
def on(transition = "4") {
	log.trace parent.on(this,transition)
	sendEvent(name: "switch", value: "on")
}

def off(transition = "4") {
	log.trace parent.off(this,transition)
	sendEvent(name: "switch", value: "off")
}

def nextLevel() {
	def level = device.latestValue("level") as Integer ?: 0
	if (level <= 100) {
		level = Math.min(25 * (Math.round(level / 25) + 1), 100) as Integer
	}
	else {
		level = 25
	}
	setLevel(level)
}

def setLevel(percent) {
	log.debug "Executing 'setLevel'"
	parent.setLevel(this, percent)
	sendEvent(name: "level", value: percent)
}

def setSaturation(percent) {
	log.debug "Executing 'setSaturation'"
	parent.setSaturation(this, percent)
	sendEvent(name: "saturation", value: percent)
}

def setHue(percent) {
	log.debug "Executing 'setHue'"
	parent.setHue(this, percent)
	sendEvent(name: "hue", value: percent)
}

def setColor(value,alert = "none",transition = 4) {
	log.debug "setColor: ${value}, $this"
	parent.setColor(this, value, alert, transition)
	if (value.hue) { sendEvent(name: "hue", value: value.hue)}
	if (value.saturation) { sendEvent(name: "saturation", value: value.saturation)}
	if (value.hex) { sendEvent(name: "color", value: value.hex)}
	if (value.level) { sendEvent(name: "level", value: value.level)}
	if (value.switch) { sendEvent(name: "switch", value: value.switch)}
}

def reset() {
	log.debug "Executing 'reset'"
    def value = [level:100, hex:"#90C638", saturation:56, hue:23]
    setAdjustedColor(value)
	parent.poll()
}

def setAdjustedColor(value) {
	if (value) {
        log.trace "setAdjustedColor: ${value}"
        def adjusted = value + [:]
        adjusted.hue = adjustOutgoingHue(value.hue)
        // Needed because color picker always sends 100
        adjusted.level = null 
        setColor(adjusted)
    }
}

def refresh() {
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
