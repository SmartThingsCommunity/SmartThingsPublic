/**
 *  Hue Advanced -CD- Light/Group
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
	definition (name: "Hue Advanced -CD- Light/Group", namespace: "claytonjn", author: "claytonjn") {
		capability "Switch Level"
		capability "Actuator"
		capability "Color Control"
		capability "Color Temperature"
		capability "Switch"
		capability "Refresh"
		capability "Sensor"
		capability "Health Check"
		capability "Light"

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
		command "tileSetColorTemperature"

		attribute "deviceSwitch", "enum", ["lightsOn", "lightsOff", "lightsTurningOn", "lightsTurningOff", "groupsOn", "groupsOff", "groupsTurningOn", "groupsTurningOff"]
		attribute "transitionTime", "NUMBER"
		attribute "xy", "json_object"
		attribute "effect", "enum", ["none", "colorloop"]
		attribute "colormode", "enum", ["hs", "xy", "ct"]
		attribute "reachable", "enum", ["true", "false"]
		attribute "cdBrightness", "enum", ["true", "false"]
		attribute "cdColor", "enum", ["true", "false"]
	}

	simulator {
		// TODO: define status and reply messages here
	}

	tiles (scale: 2){
		multiAttributeTile(name:"rich-control", type: "lighting", width: 6, height: 4, canChangeIcon: true){
			tileAttribute ("deviceSwitch", key: "PRIMARY_CONTROL") {
				attributeState "lightsOn", label:'ON', action:"switch.off", icon:"st.lights.philips.hue-single", backgroundColor:"#00A0DC", nextState:"lightsTurningOff"
				attributeState "lightsOff", label:'OFF', action:"switch.on", icon:"st.lights.philips.hue-single", backgroundColor:"#ffffff", nextState:"lightsTurningOn"
				attributeState "lightsTurningOn", label:'TURNING ON', action:"switch.off", icon:"st.lights.philips.hue-single", backgroundColor:"#00A0DC", nextState:"lightsTurningOff"
				attributeState "lightsTurningOff", label:'TURNING OFF', action:"switch.on", icon:"st.lights.philips.hue-single", backgroundColor:"#ffffff", nextState:"lightsTurningOn"
				attributeState "groupsOn", label:'ON', action:"switch.off", icon:"st.lights.philips.hue-multi", backgroundColor:"#00A0DC", nextState:"groupsTurningOff"
				attributeState "groupsOff", label:'OFF', action:"switch.on", icon:"st.lights.philips.hue-multi", backgroundColor:"#ffffff", nextState:"groupsTurningOn"
				attributeState "groupsTurningOn", label:'TURNING ON', action:"switch.off", icon:"st.lights.philips.hue-multi", backgroundColor:"#00A0DC", nextState:"groupsTurningOff"
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
            state "colorTemperature", label: 'Temperature: ${currentValue}K'
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
			state "transitionTime", label: 'Transition: ${currentValue}s'
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

		standardTile("cdBrightnessControl", "device.cdBrightness", height: 1, width: 3, inactiveLabel: false, decoration: "flat") {
			state "true", label:"Circadian Brightness On", action:"disableCDBrightness", nextState: "updating"
			state "false", label:"Circadian Brightness Off", action:"enableCDBrightness", nextState: "updating"
			state "updating", label:"Working"
		}

		standardTile("cdColorControl", "device.cdColor", height: 1, width: 3, inactiveLabel: false, decoration: "flat") {
			state "true", label:"Circadian Color On", action:"disableCDColor", nextState: "updating"
			state "false", label:"Circadian Color Off", action:"enableCDColor", nextState: "updating"
			state "updating", label:"Working"
		}

		main(["rich-control"])
		details(["rich-control", "colorTempSliderControl", "colorTemp", "transitionTimeSliderControl", "transTime", "cdBrightnessControl", "cdColorControl", "effectControl", "colormode", "reachable", "reset", "refresh"])
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
		log.debug "Hue Advanced -CD- Light/Group stringToMap - ${map}"
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

	colorloopOff()
	log.trace parent.on(this, transitionTime, state.deviceType)
}

void off(transitionTime = device.currentValue("transitionTime")) {
	if(transitionTime == null) { transitionTime = device.currentValue("transitionTime") ?: parent.getSelectedTransition() ?: 1 }

	colorloopOff()
	log.trace parent.off(this, transitionTime, state.deviceType)
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
	        log.trace parent.setLevel(this, percent, transitionTime, state.deviceType)
			if(disableCDB == true) { disableCDBrightness() }
		}
    }
}

void setSaturation(percent, transitionTime = device.currentValue("transitionTime"), disableCDC = false) {
	if(transitionTime == null) { transitionTime = device.currentValue("transitionTime") ?: parent.getSelectedTransition() ?: 1 }

    log.debug "Executing 'setSaturation'"
	colorloopOff()
    if (verifyPercent(percent)) {
        log.trace parent.setSaturation(this, percent, transitionTime, state.deviceType)
		if(disableCDC == true) { disableCDColor() }
    }
}

void setHue(percent, transitionTime = device.currentValue("transitionTime"), disableCDC = false) {
	if(transitionTime == null) { transitionTime = device.currentValue("transitionTime") ?: parent.getSelectedTransition() ?: 1 }

    log.debug "Executing 'setHue'"
	colorloopOff()
    if (verifyPercent(percent)) {
        log.trace parent.setHue(this, percent, transitionTime, state.deviceType)
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
        log.trace parent.setColor(this, validValues, state.deviceType)
    }
}

private tileReset() {
	reset(null, true, true)
}

void reset(transitionTime = device.currentValue("transitionTime"), disableCDB = false, disableCDC = false) {
	if(transitionTime == null) { transitionTime = device.currentValue("transitionTime") ?: parent.getSelectedTransition() ?: 1 }

    log.debug "Executing 'reset'"
	colorloopOff()
	setLevel(100, transitionTime, disableCDB)
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
        // Needed because color picker always sends 100
        adjusted.level = null
        setColor(adjusted)
    } else {
        log.warn "Invalid color input $value"
    }
}

private tileSetColorTemperature(value) {
	setColorTemperature(value, null, true)
}

void setColorTemperature(value, transitionTime = device.currentValue("transitionTime"), disableCDC = false) {
	if(transitionTime == null) { transitionTime = device.currentValue("transitionTime") ?: parent.getSelectedTransition() ?: 1 }
	colorloopOff()
    if (value >= 0) {
        log.trace "setColorTemperature: ${value}k"
        log.trace parent.setColorTemperature(this, value, transitionTime, state.deviceType)
		if(disableCDC == true) { disableCDColor() }
    } else {
        log.warn "Invalid color temperature $value"
    }
}

void refresh() {
    log.debug "Executing 'refresh'"
    parent?.manualRefresh()
}

void alert(alert) {
	log.debug "Executing 'alert'"
	parent.setAlert(this, alert, state.deviceType)
}

void colorloopOn() {
	log.debug "Executing 'colorloopOn'"
	if(device.latestValue("switch") != "on") { on() }
	parent.setEffect(this, "colorloop", state.deviceType)
}

void colorloopOff() {
	log.debug "Executing 'colorloopOff'"
	parent.setEffect(this, "none", state.deviceType)
}

void bri_inc(value) {
	log.debug "Executing 'bri_inc'"
	parent.bri_inc(this, value, state.deviceType)
}

void sat_inc(value) {
	log.debug "Executing 'sat_inc'"
	parent.sat_inc(this, value, state.deviceType)
}

void hue_inc(value) {
	log.debug "Executing 'hue_inc'"
	parent.hue_inc(this, value, state.deviceType)
}

void ct_inc(value) {
	log.debug "Executing 'ct_inc'"
	parent.ct_inc(this, value, state.deviceType)
}

void xy_inc(x, y) {
	x = x.round(4)
	y = y.round(4)
	log.debug "Executing 'xy_inc'"
	parent.xy_inc(this, [x, y], state.deviceType)
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

void setHADeviceHandler(circadianDaylightIntegration) {
	if (circadianDaylightIntegration == true) {
		setDeviceType("Hue Advanced -CD- Light/Group")
	} else {
		setDeviceType("Hue Advanced Light/Group")
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