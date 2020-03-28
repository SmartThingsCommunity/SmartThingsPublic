/**
 *  LIFX Color Bulb
 *
 *  Copyright 2015 LIFX
 *
 */
metadata {
    definition (name: "LIFX Color Bulb", namespace: "smartthings", author: "LIFX", ocfDeviceType: "oic.d.light", cloudDeviceHandler: "smartthings.cdh.handlers.LifxLightHandler") {
        capability "Actuator"
        capability "Color Control"
        capability "Color Temperature"
        capability "Switch"
        capability "Switch Level" // brightness
        capability "Refresh"
        capability "Sensor"
        capability "Health Check"
        capability "Light"
    }

    simulator {

    }

    tiles(scale: 2) {
        multiAttributeTile(name:"switch", type: "lighting", width: 6, height: 4, canChangeIcon: true){
            tileAttribute ("device.switch", key: "PRIMARY_CONTROL") {
                attributeState "on", label:'${name}', action:"switch.off", icon:"http://hosted.lifx.co/smartthings/v1/196xOn.png", backgroundColor:"#00A0DC", nextState:"turningOff"
                attributeState "off", label:'${name}', action:"switch.on", icon:"http://hosted.lifx.co/smartthings/v1/196xOff.png", backgroundColor:"#ffffff", nextState:"turningOn"
                attributeState "turningOn", label:'Turning on', action:"switch.off", icon:"http://hosted.lifx.co/smartthings/v1/196xOn.png", backgroundColor:"#00A0DC", nextState:"turningOff"
                attributeState "turningOff", label:'Turning off', action:"switch.on", icon:"http://hosted.lifx.co/smartthings/v1/196xOff.png", backgroundColor:"#ffffff", nextState:"turningOn"
            }

            tileAttribute ("device.level", key: "SLIDER_CONTROL") {
                attributeState "level", action:"switch level.setLevel"
            }

            tileAttribute ("device.color", key: "COLOR_CONTROL") {
                attributeState "color", action:"setColor"
            }

            tileAttribute ("device.model", key: "SECONDARY_CONTROL") {
                attributeState "model", label: '${currentValue}'
            }
        }

        standardTile("refresh", "device.switch", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
            state "default", label:"", action:"refresh.refresh", icon:"st.secondary.refresh"
        }

        valueTile("null", "device.switch", inactiveLabel: false, decoration: "flat") {
            state "default", label:''
        }

        controlTile("colorTempSliderControl", "device.colorTemperature", "slider", height: 2, width: 4, inactiveLabel: false, range:"(2700..9000)") {
            state "colorTemp", action:"color temperature.setColorTemperature"
        }

        valueTile("colorTemp", "device.colorTemperature", inactiveLabel: false, decoration: "flat", height: 2, width: 2) {
            state "colorTemp", label: '${currentValue}K'
        }

        main "switch"
        details(["switch", "colorTempSliderControl", "colorTemp", "refresh"])
    }
}

def initialize() {
    sendEvent(name: "DeviceWatch-Enroll", value: "{\"protocol\": \"cloud\", \"scheme\":\"untracked\", \"hubHardwareId\": \"${device?.hub?.hardwareID}\"}", displayed: false)
}

void installed() {
    log.debug "installed()"
    initialize()
}

def updated() {
    log.debug "updated()"
    initialize()
}

// handle commands
def setHue(percentage) {
    log.debug "setHue ${percentage}"
    parent.logErrors(logObject: log) {
        def resp = parent.apiPUT("/lights/${selector()}/state", [color: "hue:${percentage * 3.6}", power: "on"])
        if (resp.status < 300 && resp.data.results.status[0] == "ok") {
            sendEvent(name: "hue", value: percentage)
            sendEvent(name: "switch", value: "on")
        } else {
            log.error("Bad setHue result: [${resp.status}] ${resp.data}")
        }
    }
    return []
}

def setSaturation(percentage) {
    log.debug "setSaturation ${percentage}"
    parent.logErrors(logObject: log) {
        def resp = parent.apiPUT("/lights/${selector()}/state", [color: "saturation:${percentage / 100}", power: "on"])
        if (resp.status < 300 && resp.data.results.status[0] == "ok") {
            sendEvent(name: "saturation", value: percentage)
            sendEvent(name: "switch", value: "on")
        } else {
            log.error("Bad setSaturation result: [${resp.status}] ${resp.data}")
        }
    }
    return []
}

def setColor(Map color) {
    log.debug "setColor ${color}"
    def attrs = []
    def events = []
    color.each { key, value ->
        switch (key) {
            case "hue":
                attrs << "hue:${value * 3.6}"
                events << createEvent(name: "hue", value: value)
                break
            case "saturation":
                attrs << "saturation:${value / 100}"
                events << createEvent(name: "saturation", value: value)
                break
            case "colorTemperature":
                attrs << "kelvin:${value}"
                events << createEvent(name: "colorTemperature", value: value)
                break
        }
    }
    parent.logErrors(logObject:log) {
        def resp = parent.apiPUT("/lights/${selector()}/state", [color: attrs.join(" "), power: "on"])
        if (resp.status < 300 && resp.data.results.status[0] == "ok") {
            if (color.hex)
                sendEvent(name: "color", value: color.hex)
            sendEvent(name: "switch", value: "on")
            events.each { sendEvent(it) }
        } else {
            log.error("Bad setColor result: [${resp.status}] ${resp.data}")
        }
    }
    return []
}

def setLevel(percentage, rate = null) {
    log.debug "setLevel ${percentage}"
    if (percentage < 1 && percentage > 0) {
        percentage = 1 // clamp to 1%
    } else {
        try {
            percentage = Math.round(percentage)
        } catch (Exception ex) {
            log.error "Caught exception while converting value '$percentage' to integer: $ex"
            percentage = 0
        }
    }
    log.debug "setlevel: using percentage value of $percentage"

    if (percentage == 0) {
        return off() // if the brightness is set to 0, just turn it off
    }
    parent.logErrors(logObject:log) {
        def resp = parent.apiPUT("/lights/${selector()}/state", ["brightness": percentage / 100, "power": "on"])
        if (resp.status < 300 && resp.data.results.status[0] == "ok") {
            sendEvent(name: "level", value: percentage)
            sendEvent(name: "switch", value: "on")
        } else {
            log.error("Bad setLevel result: [${resp.status}] ${resp.data}")
			sendEvent(name: "level", value: device.currentValue("level"), isStateChange: true, displayed: false)
			sendEvent(name: "switch.setLevel", value: device.currentValue("level"), isStateChange: true, displayed: false)
        }
    }
    return []
}

def setColorTemperature(kelvin) {
    log.debug "Executing 'setColorTemperature' to ${kelvin}"
    parent.logErrors() {
        def resp = parent.apiPUT("/lights/${selector()}/state", [color: "kelvin:${kelvin}", power: "on"])
        if (resp.status < 300 && resp.data.results.status[0] == "ok") {
            sendEvent(name: "colorTemperature", value: kelvin)
            sendEvent(name: "color", value: "#ffffff")
            sendEvent(name: "saturation", value: 0)
        } else {
            log.error("Bad setColorTemperature result: [${resp.status}] ${resp.data}")
        }
    }
    return []
}

def on() {
	log.debug "Device setOn"
	parent.logErrors() {
    	def resp = parent.apiPUT("/lights/${selector()}/state", [power: "on"])
		if (resp.status < 300 && resp.data.results.status[0] == "ok") {
			sendEvent(name: "switch", value: "on")
		}
	}
	return []
}

def off() {
	log.debug "Device setOff"
	parent.logErrors() {
        def resp = parent.apiPUT("/lights/${selector()}/state", [power: "off"])
		if (resp.status < 300 && resp.data.results.status[0] == "ok") {
			sendEvent(name: "switch", value: "off")
		}
	}
	return []
}

def refresh() {
    log.debug "Executing 'refresh'"

    def resp = parent.apiGET("/lights/${selector()}")
    if (resp.status == 404) {
        state.online = false
        sendEvent(name: "DeviceWatch-DeviceStatusUpdate", value: "offline", displayed: false)
         log.warn "$device is Offline"
        return []
    } else if (resp.status != 200) {
        log.error("Unexpected result in refresh(): [${resp.status}] ${resp.data}")
        return []
    }
    def data = resp.data[0]
    log.debug("Data: ${data}")

    sendEvent(name: "label", value: data.label)
    sendEvent(name: "level", value: Math.round((data.brightness ?: 1) * 100))
    sendEvent(name: "switch.setLevel", value: Math.round((data.brightness ?: 1) * 100))
    sendEvent(name: "switch", value: data.power)
    sendEvent(name: "color", value: colorUtil.hslToHex((data.color.hue / 3.6) as int, (data.color.saturation * 100) as int))
    sendEvent(name: "hue", value: data.color.hue / 3.6)
    sendEvent(name: "saturation", value: data.color.saturation * 100)
    sendEvent(name: "colorTemperature", value: data.color.kelvin)
    sendEvent(name: "model", value: data.product.name)

    if (data.connected) {
        sendEvent(name: "DeviceWatch-DeviceStatus", value: "online", displayed: false)
        log.debug "$device is Online"
    } else {
        sendEvent(name: "DeviceWatch-DeviceStatus", value: "offline", displayed: false)
        log.warn "$device is Offline"
}
}

def selector() {
    if (device.deviceNetworkId.contains(":")) {
        return device.deviceNetworkId
    } else {
        return "id:${device.deviceNetworkId}"
    }
}
