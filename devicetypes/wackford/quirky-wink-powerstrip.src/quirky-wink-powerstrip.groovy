/*  Quirky-Wink-Powerstrip-Device.groovy
 *
 *  Author: todd@wackford.net
 *  Date: 2014-01-28
 *
 *****************************************************************
 *     Setup Namespace, acpabilities, attributes and commands
 *****************************************************************
 * Namespace:			"wackford"
 *
 * Capabilities:		"switch"
 *						"polling"
 *						"refresh"
 *
 * Custom Attributes:	"none"
 *
 * Custom Commands:		"none"
 *
 *****************************************************************
 *                       Changes
 *****************************************************************
 *
 *  Change 1:	2014-03-10
 *				Documented Header
 *
 *****************************************************************
 *                       Code
 *****************************************************************
 */
// for the UI
metadata {

	definition(name:"Quirky Wink Powerstrip", namespace:"wackford", author:"Todd Wackford") {

		capability "Switch"
		capability "Polling"
		capability "Refresh"
		capability "Sensor"
		capability "Actuator"
	}

	simulator {
		// TODO: define status and reply messages here
	}

	tiles {
		standardTile("switch", "device.switch", width: 2, height: 2, canChangeIcon: true) {
			state "off", label: '${name}', action: "switch.on", icon: "st.switches.switch.off", backgroundColor: "#ffffff"
			state "on", label: '${name}', action: "switch.off", icon: "st.switches.switch.on", backgroundColor: "#00A0DC"
		}
		standardTile("refresh", "device.refresh", inactiveLabel: false, decoration: "flat") {
			state "default", action:"refresh.refresh", icon:"st.secondary.refresh"
		}
	}
	main(["switch"])
	details(["switch", "refresh" ])
}


// parse events into attributes
def parse(description) {
	log.debug "parse() - $description"
	def results = []

	if (description?.name && description?.value)
	{
		results << sendEvent(name: "${description?.name}", value: "${description?.value}")
	}
}


// handle commands
def on() {
	log.debug "Executing 'on'"
	log.debug this
	parent.on(this)
}

def off() {
	log.debug "Executing 'off'"
	parent.off(this)
}

def poll() {
	log.debug "Executing 'poll'"
	parent.pollOutlet(this)
}

def refresh() {
	log.debug "Executing 'refresh'"
	poll()
}
