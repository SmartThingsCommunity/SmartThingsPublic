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
	}

	simulator {
		// TODO: define status and reply messages here
	}

	standardTile("switch", "device.switch", width: 2, height: 2, canChangeIcon: true) {
		state "on", label:'${name}', action:"switch.off", icon:"st.lights.philips.hue-single", backgroundColor:"#79b821"
		state "off", label:'${name}', action:"switch.on", icon:"st.lights.philips.hue-single", backgroundColor:"#ffffff"
	}
	standardTile("refresh", "device.switch", inactiveLabel: false, decoration: "flat") {
		state "default", label:"", action:"refresh.refresh", icon:"st.secondary.refresh"
	}
	standardTile("nextLevel", "device.level", inactiveLabel: false, decoration: "flat") {
		state "default", label:'Level', action:"nextLevel", icon:"st.illuminance.illuminance.light"
	}
	controlTile("rgbSelector", "device.color", "color", height: 3, width: 3, inactiveLabel: false) {
		state "color", action:"setAdjustedColor"
	}
	controlTile("levelSliderControl", "device.level", "slider", height: 1, width: 2, inactiveLabel: false) {
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
	details(["switch", "levelSliderControl", "rgbSelector", "refresh", "nextLevel"])

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
def on() {
	parent.on(this)
	sendEvent(name: "switch", value: "on")
}

def off() {
	parent.off(this)
	sendEvent(name: "switch", value: "off")
}

def nextLevel() {
	def level = device.latestValue("level") as Integer ?: 0
	if (level < 100) {
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

def setColor(value) {
	log.debug "setColor: ${value}"
	parent.setColor(this, value)
	sendEvent(name: "hue", value: value.hue)
	sendEvent(name: "saturation", value: value.saturation)
	if (value.level) {
		sendEvent(name: "level", value: value.level)
	}
	if (value.switch) {
		sendEvent(name: "switch", value: value.switch)
	}
}

def setAdjustedColor(value) {
	log.debug "setAdjustedColor: ${value}"
	def adjusted = value + [:]
	adjusted.hue = adjustOutgoingHue(value.hue)
	adjusted.level = null // needed because color picker always sends 100
	setColor(adjusted)
}

def save() {
	log.debug "Executing 'save'"
}

def refresh() {
	log.debug "Executing 'refresh'"
	parent.poll()
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
