/**
 *  Hue Advanced -CD- White Ambiance Bulb
 *
 *  Philips Hue Type "Color Temperature Light"
 *
 *  Author: claytonjn
 */

// for the UI
metadata {
    // Automatically generated. Make future change here.
    definition (name: "Hue Advanced -CD- White Ambiance Bulb", namespace: "claytonjn", author: "claytonjn") {
        capability "Switch Level"
        capability "Actuator"
        capability "Color Temperature"
        capability "Switch"
        capability "Refresh"

        command "reset"
        command "refresh"
        command "setTransitionTime"
        command "alert"
        command "colorloopOn"
        command "colorloopOff"
        command "bri_inc"
        command "ct_inc"
        command "enableCDBrightness"
        command "disableCDBrightness"
		command "enableCDColor"
		command "disableCDColor"
		command "tileSetLevel"
		command "tileReset"
		command "tileSetColorTemperature"

        attribute "transitionTime", "NUMBER"
        attribute "effect", "enum", ["none", "colorloop"]
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
                attributeState "level", action:"switch level.tileSetLevel", range:"(0..100)"
            }
        }

        controlTile("colorTempSliderControl", "device.colorTemperature", "slider", width: 4, height: 1, inactiveLabel: false, range:"(2000..6500)") {
            state "colorTemperature", action:"tileSetColorTemperature"
        }
        valueTile("colorTemp", "device.colorTemperature", inactiveLabel: false, decoration: "flat", width: 2, height: 1) {
            state "colorTemperature", label: 'Temperature: ${currentValue} K'
        }

        standardTile("reset", "device.reset", height: 2, width: 2, inactiveLabel: false, decoration: "flat") {
			state "default", label:"Reset", action:"tileReset", icon:"st.lights.philips.hue-single"
		}

        standardTile("refresh", "device.refresh", height: 2, width: 2, inactiveLabel: false, decoration: "flat") {
            state "default", label:"", action:"refresh.refresh", icon:"st.secondary.refresh"
        }

        controlTile("transitionTimeSliderControl", "device.transitionTime", "slider", width: 4, height: 1, inactiveLabel: false, range: "(0..10)") {
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

        valueTile("reachable", "device.reachable", inactiveLabel: false, decoration: "flat", width: 2, height: 1) {
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
        details(["rich-control", "colorTempSliderControl", "colorTemp", "transitionTimeSliderControl", "transTime", "cdBrightnessControl", "cdColorControl", "effectControl", "reachable", "reset", "refresh"])
    }
}

// parse events into attributes
def parse(description) {
    log.debug "parse() - $description"
    def results = []

    def map = description
    if (description instanceof String)  {
        log.debug "Hue Advanced -CD- White Ambience Bulb stringToMap - ${map}"
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
    sendEvent(name: "switch", value: "on")
}

void off(transitionTime = device.currentValue("transitionTime")) {
    if(transitionTime == null) { transitionTime = device.currentValue("transitionTime") ?: parent.getSelectedTransition() ?: 1 }

    colorloopOff()
    log.trace parent.off(this, transitionTime, deviceType)
    sendEvent(name: "switch", value: "off")
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
	        parent.setLevel(this, percent, transitionTime, deviceType)
            if(disableCDB == true) { disableCDBrightness() }
	        sendEvent(name: "level", value: percent, descriptionText: "Level has changed to ${percent}%")
			sendEvent(name: "switch", value: "on")
		}
    }
}

private tileSetColorTemperature(value) {
	setColorTemperature(value, null, true)
}

void setColorTemperature(value, transitionTime = device.currentValue("transitionTime"), disableCDC = false) {
	if(transitionTime == null) { transitionTime = device.currentValue("transitionTime") ?: parent.getSelectedTransition() ?: 1 }

    colorloopOff()
    if (value >= 0) {
        value = Math.round(value) as Integer
        log.trace "setColorTemperature: ${value}k"
        parent.setColorTemperature(this, value, transitionTime, deviceType)
        if(disableCDC == true) { disableCDColor() }
        sendEvent(name: "colorTemperature", value: value)
        sendEvent(name: "switch", value: "on")
    } else {
        log.warn "Invalid color temperature"
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
	sendEvent(name: "effect", value: "colorloop", descriptionText: "Colorloop has been turned on")
}

void colorloopOff() {
	log.debug "Executing 'colorloopOff'"
	parent.setEffect(this, "none", deviceType)
	sendEvent(name: "effect", value: "none", descriptionText: "Colorloop has been turned off")
}

void bri_inc(value) {
	log.debug "Executing 'bri_inc'"
	parent.bri_inc(this, value, deviceType)
}

void ct_inc(value) {
	log.debug "Executing 'ct_inc'"
	parent.ct_inc(this, value, deviceType)
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
    sendEvent(name: "cdBrightness", value: "true", displayed: false)
	sendEvent(name: "cdColor", value: "true", displayed: false)
}

def getDeviceType() { return "lights" }

void setHADeviceHandler(circadianDaylightIntegration) {
	if (circadianDaylightIntegration == true) {
		setDeviceType("Hue Advanced -CD- White Ambiance Bulb")
	} else {
		setDeviceType("Hue Advanced White Ambiance Bulb")
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