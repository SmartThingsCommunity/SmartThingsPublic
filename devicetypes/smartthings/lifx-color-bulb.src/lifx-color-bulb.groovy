/**
 *  LIFX Color Bulb
 *
 *  Copyright 2015 LIFX
 *
 */
metadata {
	definition (name: "LIFX Color Bulb", namespace: "smartthings", author: "LIFX") {
		capability "Actuator"
		capability "Color Control"
		capability "Color Temperature"
		capability "Switch"
		capability "Switch Level" // brightness
		capability "Polling"
		capability "Refresh"
		capability "Sensor"
	}

	simulator {
		// TODO: define status and reply messages here
	}

	tiles {
		standardTile("switch", "device.switch", width: 2, height: 2, canChangeIcon: true) {
			state "unreachable", label: "?", action:"refresh.refresh", icon:"st.switches.light.off", backgroundColor:"#666666"
			state "on", label:'${name}', action:"switch.off", icon:"st.switches.light.on", backgroundColor:"#79b821", nextState:"turningOff"
			state "off", label:'${name}', action:"switch.on", icon:"st.switches.light.off", backgroundColor:"#ffffff", nextState:"turningOn"
			state "turningOn", label:'Turning on', action:"switch.off", icon:"st.switches.light.on", backgroundColor:"#79b821", nextState:"turningOff"
			state "turningOff", label:'Turning off', action:"switch.on", icon:"st.switches.light.off", backgroundColor:"#ffffff", nextState:"turningOn"
		}
		standardTile("refresh", "device.switch", inactiveLabel: false, decoration: "flat") {
			state "default", label:"", action:"refresh.refresh", icon:"st.secondary.refresh"
		}
		valueTile("null", "device.switch", inactiveLabel: false, decoration: "flat") {
			state "default", label:''
		}

		controlTile("rgbSelector", "device.color", "color", height: 3, width: 3, inactiveLabel: false) {
			state "color", action:"setColor"
		}

		controlTile("levelSliderControl", "device.level", "slider", height: 1, width: 3, inactiveLabel: false, range:"(0..100)") {
			state "level", action:"switch level.setLevel"
		}
		valueTile("level", "device.level", inactiveLabel: false, icon: "st.illuminance.illuminance.light", decoration: "flat") {
			state "level", label: '${currentValue}%'
		}

		controlTile("colorTempSliderControl", "device.colorTemperature", "slider", height: 1, width: 2, inactiveLabel: false, range:"(2700..9000)") {
			state "colorTemp", action:"color temperature.setColorTemperature"
		}
		valueTile("colorTemp", "device.colorTemperature", inactiveLabel: false, decoration: "flat") {
			state "colorTemp", label: '${currentValue}K'
		}

		main(["switch"])
		details(["switch", "refresh", "level", "levelSliderControl", "rgbSelector", "colorTempSliderControl", "colorTemp"])
	}
}

// parse events into attributes
def parse(String description) {
	if (description == 'updated') {
		return // don't poll when config settings is being updated as it may time out
	}
	poll()
}

// handle commands
def setHue(percentage) {
	log.debug "setHue ${percentage}"
	parent.logErrors(logObject: log) {
		def resp = parent.apiPUT("/lights/${device.deviceNetworkId}/color", [color: "hue:${percentage * 3.6}"])
		if (resp.status < 300) {
			sendEvent(name: "hue", value: percentage)
			sendEvent(name: "switch", value: "on")
		} else {
			log.error("Bad setHue result: [${resp.status}] ${resp.data}")
		}
	}
}

def setSaturation(percentage) {
	log.debug "setSaturation ${percentage}"
	parent.logErrors(logObject: log) {
		def resp = parent.apiPUT("/lights/${device.deviceNetworkId}/color", [color: "saturation:${percentage / 100}"])
		if (resp.status < 300) {
			sendEvent(name: "saturation", value: percentage)
			sendEvent(name: "switch", value: "on")
		} else {
			log.error("Bad setSaturation result: [${resp.status}] ${resp.data}")
		}
	}
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
		def resp = parent.apiPUT("/lights/${device.deviceNetworkId}/color", [color: attrs.join(" ")])
		if (resp.status < 300) {
			sendEvent(name: "color", value: color.hex)
			sendEvent(name: "switch", value: "on")
			events.each { sendEvent(it) }
		} else {
			log.error("Bad setColor result: [${resp.status}] ${resp.data}")
		}
	}
}

def setLevel(percentage) {
	log.debug "setLevel ${percentage}"
	if (percentage < 1 && percentage > 0) {
		percentage = 1 // clamp to 1%
	}
	if (percentage == 0) {
		sendEvent(name: "level", value: 0) // Otherwise the level value tile does not update
		return off() // if the brightness is set to 0, just turn it off
	}
	parent.logErrors(logObject:log) {
		def resp = parent.apiPUT("/lights/${device.deviceNetworkId}/color", ["color": "brightness:${percentage / 100}"])
		if (resp.status < 300) {
			sendEvent(name: "level", value: percentage)
			sendEvent(name: "switch", value: "on")
		} else {
			log.error("Bad setLevel result: [${resp.status}] ${resp.data}")
		}
	}
}

def setColorTemperature(kelvin) {
	log.debug "Executing 'setColorTemperature' to ${kelvin}"
	parent.logErrors() {
		def resp = parent.apiPUT("/lights/${device.deviceNetworkId}/color", [color: "kelvin:${kelvin}"])
		if (resp.status < 300) {
			sendEvent(name: "colorTemperature", value: kelvin)
			sendEvent(name: "color", value: "#ffffff")
			sendEvent(name: "saturation", value: 0)
		} else {
			log.error("Bad setLevel result: [${resp.status}] ${resp.data}")
		}

	}
}

def on() {
	log.debug "Device setOn"
	parent.logErrors() {
		if (parent.apiPUT("/lights/${device.deviceNetworkId}/power", [state: "on"]) != null) {
			sendEvent(name: "switch", value: "on")
		}
	}
}

def off() {
	log.debug "Device setOff"
	parent.logErrors() {
		if (parent.apiPUT("/lights/${device.deviceNetworkId}/power", [state: "off"]) != null) {
			sendEvent(name: "switch", value: "off")
		}
	}
}

def poll() {
	log.debug "Executing 'poll' for ${device} ${this} ${device.deviceNetworkId}"
	def resp = parent.apiGET("/lights/${device.deviceNetworkId}")
	if (resp.status != 200) {
		log.error("Unexpected result in poll(): [${resp.status}] ${resp.data}")
		return []
	}
	def data = resp.data

	sendEvent(name: "level", value: sprintf("%.1f", (data.brightness ?: 1) * 100))
	sendEvent(name: "switch", value: data.connected ? data.power : "unreachable")
	sendEvent(name: "color", value: colorUtil.hslToHex((data.color.hue / 3.6) as int, (data.color.saturation * 100) as int))
	sendEvent(name: "hue",        value: data.color.hue / 3.6)
	sendEvent(name: "saturation", value: data.color.saturation * 100)
	sendEvent(name: "colorTemperature", value: data.color.kelvin)

	return []
}

def refresh() {
	log.debug "Executing 'refresh'"
	poll()
}