/**
 *  Hue Lux Bulb
 *
 *  Author: SmartThings
 */
// for the UI
metadata {
	// Automatically generated. Make future change here.
	definition (name: "Hue Lux Bulb", namespace: "smartthings", author: "SmartThings") {
		capability "Switch Level"
		capability "Actuator"
		capability "Switch"
		capability "Refresh"
		capability "Sensor"
       
        command "refresh"       
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
	controlTile("levelSliderControl", "device.level", "slider", height: 1, width: 2, inactiveLabel: false, range:"(0..100)") {
		state "level", action:"switch level.setLevel"
	}
	valueTile("level", "device.level", inactiveLabel: false, decoration: "flat") {
		state "level", label: 'Level ${currentValue}%'
	}

	main(["switch"])
	details(["switch", "levelSliderControl", "refresh"])

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

def setLevel(percent) {
	log.debug "Executing 'setLevel'"
	parent.setLevel(this, percent)
	sendEvent(name: "level", value: percent)
}

def refresh() {
	log.debug "Executing 'refresh'"
	parent.manualRefresh()
}