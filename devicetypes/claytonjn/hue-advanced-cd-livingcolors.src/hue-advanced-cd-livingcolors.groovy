/**
 *  Hue Advanced -CD- LivingColors
 *
 *  Philips Hue Type "Color Light"
 *
 *  Author: claytonjn
 */

// for the UI
metadata {
	// Automatically generated. Make future change here.
	definition (name: "Hue Advanced -CD- LivingColors", namespace: "claytonjn", author: "claytonjn") {
		capability "Switch Level"
		capability "Actuator"
		capability "Color Control"
		capability "Switch"
		capability "Refresh"
		capability "Sensor"
		capability "Health Check"

		command "setAdjustedColor"
        command "reset"
        command "refresh"
		command "setTransitionTime"
		command "alert"
		command "colorloopOn"
		command "colorloopOff"
		command "bri_inc"
		command "sat_inc"
		command "hue_inc"
		command "ct_inc"
		command "xy_inc"
		command "enableCDBrightness"
        command "disableCDBrightness"
		command "enableCDColor"
		command "disableCDColor"
		command "tileSetLevel"
		command "tileSetAdjustedColor"
        command "tileReset"

		attribute "transitionTime", "NUMBER"
		attribute "xy", "json_object"
		attribute "effect", "enum", ["none", "colorloop"]
		attribute "colormode", "enum", ["hs", "xy"]
		attribute "reachable", "enum", ["true", "false"]
		attribute "cdBrightness", "enum", ["true", "false"]
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
				attributeState "level", action:"tileSetLevel", range:"(0..100)"
            }
			tileAttribute ("device.color", key: "COLOR_CONTROL") {
				attributeState "color", action:"tileSetAdjustedColor"
			}
		}

		standardTile("reset", "device.reset", height: 2, width: 2, inactiveLabel: false, decoration: "flat") {
			state "default", label:"Reset", action:"tileReset", icon:"st.lights.philips.hue-single"
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

		standardTile("effectControl", "device.effect", height: 2, width: 2, inactiveLabel: false, decoration: "flat") {
			state "none", label:"Colorloop Off", action:"colorloopOn", nextState: "updating", icon:"https://raw.githubusercontent.com/claytonjn/SmartThingsPublic/Hue-Advanced-Development/smartapp-icons/hue-advanced/png/colorloop-off.png"
			state "colorloop", label:"Colorloop On", action:"colorloopOff", nextState: "updating", icon:"https://raw.githubusercontent.com/claytonjn/SmartThingsPublic/Hue-Advanced-Development/smartapp-icons/hue-advanced/png/colorloop-on.png"
			state "updating", label:"Working", icon: "st.secondary.secondary"
		}

		valueTile("colormode", "device.colormode", inactiveLabel: false, decoration: "flat", width: 4, height: 1) {
			state "default", label: 'Colormode: ${currentValue}'
		}

		valueTile("reachable", "device.reachable", inactiveLabel: false, decoration: "flat", width: 4, height: 1) {
			state "true", label: 'Reachable'
			state "false", label: 'Not Reachable!'
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
		details(["rich-control", "transitionTimeSliderControl", "transTime", "cdBrightnessControl", "cdColorControl", "effectControl", "colormode", "reachable", "reset", "refresh"])
	}
}

void installed() {
	sendEvent(name: "checkInterval", value: 60 * 12, data: [protocol: "lan", hubHardwareId: device.hub.hardwareID], displayed: false)
}

// parse events into attributes
def parse(description) {
	log.debug "parse() - $description"
	def results = []

	def map = description
	if (description instanceof String)  {
		log.debug "Hue Advanced -CD- LivingColors stringToMap - ${map}"
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

	colorloopOff()
	log.trace parent.on(this, transitionTime, deviceType)
}

void off(transitionTime = device.currentValue("transitionTime")) {
	if(transitionTime == null) { transitionTime = device.currentValue("transitionTime") ?: parent.getSelectedTransition() ?: 1 }

	colorloopOff()
	log.trace parent.off(this, transitionTime, deviceType)
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
	        log.trace parent.setLevel(this, percent, transitionTime, deviceType)
			if(disableCDB == true) { disableCDBrightness() }
		}
    }
}

void setSaturation(percent, transitionTime = device.currentValue("transitionTime"), disableCDC = false) {
	if(transitionTime == null) { transitionTime = device.currentValue("transitionTime") ?: parent.getSelectedTransition() ?: 1 }

	colorloopOff()
    log.debug "Executing 'setSaturation'"
    if (verifyPercent(percent)) {
        log.tracep arent.setSaturation(this, percent, transitionTime, deviceType)
		if(disableCDC == true) { disableCDColor() }
    }
}

void setHue(percent, transitionTime = device.currentValue("transitionTime"), disableCDC = false) {
	if(transitionTime == null) { transitionTime = device.currentValue("transitionTime") ?: parent.getSelectedTransition() ?: 1 }

	colorloopOff()
    log.debug "Executing 'setHue'"
    if (verifyPercent(percent)) {
        log.trace parent.setHue(this, percent, transitionTime, deviceType)
		if(disableCDC == true) { disableCDColor() }
    }
}

void setColor(value) {
	colorloopOff()
    def events = []
    def validValues = [:]

	if (value.transitionTime) { validValues.transitionTime = value.transitionTime }
	else {
		def transitionTime = (device.currentValue("transitionTime")) ?: parent.getSelectedTransition() ?: 3
		validValues.transitionTime = transitionTime
	}
    if (verifyPercent(value.hue)) {
        validValues.hue = value.hue
    }
    if (verifyPercent(value.saturation)) {
        validValues.saturation = value.saturation
    }
	if (value.xy != null) {
		if (value.xy[0] < 0 || value.xy[0] > 1 || value.xy[1] < 0 || value.xy[1] > 1) {
			log.warn "$value.xy is not a valid color"
		} else if (verifyPercent(value.level)) {
			value.xy[0] = value.xy[0].round(4)
			value.xy[1] = value.xy[1].round(4)
			validValues.xy = value.xy
		}
	}
    if (value.hex != null) {
        if (value.hex ==~ /^\#([A-Fa-f0-9]){6}$/) {
            validValues.hex = value.hex
        } else {
            log.warn "$value.hex is not a valid color"
        }
    }
    if (verifyPercent(value.level) && value.level > 0) {
        validValues.level = value.level
    }
    if (value.switch == "off" || (value.level != null && value.level <= 0)) {
        validValues.switch = "off"
    } else {
        validValues.switch = "on"
    }
	if (value.disableCDBrightness == true) { disableCDBrightness() }
	if (value.disableCDColor == true) { disableCDColor() }
    if (!validValues.isEmpty()) {
        log.trace parent.setColor(this, validValues, deviceType)
    }
}

private tileReset() {
    reset(null, true)
}

void reset(transitionTime = device.currentValue("transitionTime"), disableCDB = false, disableCDC = false) {
    if(transitionTime == null) { transitionTime = device.currentValue("transitionTime") ?: parent.getSelectedTransition() ?: 1 }

    log.debug "Executing 'reset'"
    def value = [level:100, saturation:18, hue:8, transitiontime:transitionTime, disableCDBrightness:disableCDB, disableCDColor:disableCDC]
	colorloopOff()
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
        // Needed because color picker always sends 100
        adjusted.level = null
        setColor(adjusted)
    } else {
        log.warn "Invalid color input $value"
    }
}

void refresh() {
    log.debug "Executing 'refresh'"
    parent.manualRefresh()
}

void alert(alert) {
	log.debug "Executing 'alert'"
	parent.setAlert(this, alert, deviceType)
}

void colorloopOn() {
	log.debug "Executing 'colorloopOn'"
	if(device.latestValue("switch") != "on") { on() }
	parent.setEffect(this, "colorloop", deviceType)
}

void colorloopOff() {
	log.debug "Executing 'colorloopOff'"
	parent.setEffect(this, "none", deviceType)
}

void bri_inc(value) {
	log.debug "Executing 'bri_inc'"
	parent.bri_inc(this, value, deviceType)
}

void sat_inc(value) {
	log.debug "Executing 'sat_inc'"
	parent.sat_inc(this, value, deviceType)
}

void hue_inc(value) {
	log.debug "Executing 'hue_inc'"
	parent.hue_inc(this, value, deviceType)
}

void ct_inc(value) {
	log.debug "Executing 'ct_inc'"
	parent.ct_inc(this, value, deviceType)
}

void xy_inc(x, y) {
	x = x.round(4)
	y = y.round(4)
	log.debug "Executing 'xy_inc'"
	parent.xy_inc(this, [x, y], deviceType)
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

def ping() {
    log.debug "${parent.ping(this)}"
}

void initialize(deviceType) {
	setTransitionTime(parent.getSelectedTransition())
	sendEvent(name: "cdBrightness", value: "true", displayed: false)
	sendEvent(name: "cdColor", value: "true", displayed: false)
}

def getDeviceType() { return "lights" }

void setHADeviceHandler(circadianDaylightIntegration) {
	if (circadianDaylightIntegration == true) {
		setDeviceType("Hue Advanced -CD- LivingColors")
	} else {
		setDeviceType("Hue Advanced LivingColors")
	}
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