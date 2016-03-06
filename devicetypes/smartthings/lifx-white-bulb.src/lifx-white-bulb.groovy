/**
 *  LIFX White Bulb
 *
 *  Copyright 2015 LIFX
 *
 */
metadata {
	definition (name: "LIFX White Bulb", namespace: "smartthings", author: "LIFX") {
		capability "Actuator"
		capability "Color Temperature"
		capability "Switch"
		capability "Switch Level" // brightness
		capability "Polling"
		capability "Refresh"
		capability "Sensor"
	}

	simulator {

	}

	tiles(scale: 2) {
		multiAttributeTile(name:"switch", type: "lighting", width: 6, height: 4, canChangeIcon: true){
			tileAttribute ("device.switch", key: "PRIMARY_CONTROL") {
				attributeState "unreachable", label: "?", action:"refresh.refresh", icon:"http://hosted.lifx.co/smartthings/v1/196xUnreachable.png", backgroundColor:"#666666"
				attributeState "on", label:'${name}', action:"switch.off", icon:"http://hosted.lifx.co/smartthings/v1/196xOn.png", backgroundColor:"#79b821", nextState:"turningOff"
				attributeState "off", label:'${name}', action:"switch.on", icon:"http://hosted.lifx.co/smartthings/v1/196xOff.png", backgroundColor:"#ffffff", nextState:"turningOn"
				attributeState "turningOn", label:'Turning on', action:"switch.off", icon:"http://hosted.lifx.co/smartthings/v1/196xOn.png", backgroundColor:"#79b821", nextState:"turningOff"
				attributeState "turningOff", label:'Turning off', action:"switch.on", icon:"http://hosted.lifx.co/smartthings/v1/196xOff.png", backgroundColor:"#ffffff", nextState:"turningOn"

			}
			tileAttribute ("device.level", key: "SLIDER_CONTROL") {
				attributeState "level", action:"switch level.setLevel"
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

// parse events into attributes
def parse(String description) {
	if (description == 'updated') {
		return // don't poll when config settings is being updated as it may time out
	}
	poll()
}

// handle commands
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
		def resp = parent.apiPUT("/lights/${selector()}/state", [brightness: percentage / 100, power: "on"])
		if (resp.status < 300) {
			sendEvent(name: "level", value: percentage)
			sendEvent(name: "switch.setLevel", value: percentage)
			sendEvent(name: "switch", value: "on")
		} else {
			log.error("Bad setLevel result: [${resp.status}] ${resp.data}")
		}
	}
	return []
}

def setColorTemperature(kelvin) {
	log.debug "Executing 'setColorTemperature' to ${kelvin}"
	parent.logErrors() {
		def resp = parent.apiPUT("/lights/${selector()}/state", [color: "kelvin:${kelvin}", power: "on"])
		if (resp.status < 300) {
			sendEvent(name: "colorTemperature", value: kelvin)
			sendEvent(name: "color", value: "#ffffff")
			sendEvent(name: "saturation", value: 0)
			sendEvent(name: "switch", value: "on")
		} else {
			log.error("Bad setColorTemperature result: [${resp.status}] ${resp.data}")
		}
	}
	return []
}

def on() {
	log.debug "Device setOn"
	parent.logErrors() {
		if (parent.apiPUT("/lights/${selector()}/state", [power: "on"]) != null) {
			sendEvent(name: "switch", value: "on")
		}
	}
	return []
}

def off() {
	log.debug "Device setOff"
	parent.logErrors() {
		if (parent.apiPUT("/lights/${selector()}/state", [power: "off"]) != null) {
			sendEvent(name: "switch", value: "off")
		}
	}
	return []
}

def poll() {
	log.debug "Executing 'poll' for ${device} ${this} ${device.deviceNetworkId}"
	def resp = parent.apiGET("/lights/${selector()}")
	if (resp.status == 404) {
		sendEvent(name: "switch", value: "unreachable")
		return []
	} else if (resp.status != 200) {
		log.error("Unexpected result in poll(): [${resp.status}] ${resp.data}")
		return []
	}
	def data = resp.data[0]

	sendEvent(name: "label", value: data.label)
	sendEvent(name: "level", value: Math.round((data.brightness ?: 1) * 100))
	sendEvent(name: "switch.setLevel", value: Math.round((data.brightness ?: 1) * 100))
	sendEvent(name: "switch", value: data.connected ? data.power : "unreachable")
	sendEvent(name: "colorTemperature", value: data.color.kelvin)
	sendEvent(name: "model", value: data.product.name)

	return []
}

def refresh() {
	log.debug "Executing 'refresh'"
	poll()
}

def selector() {
	if (device.deviceNetworkId.contains(":")) {
		return device.deviceNetworkId
	} else {
		return "id:${device.deviceNetworkId}"
	}
}
