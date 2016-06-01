/*
 *  Quirky-Wink-Nimbus-Device.groovy
 *
 *  Author: todd@wackford.net
 *  Date: 2014-02-22
 *
 *****************************************************************
 *     Setup Namespace, acpabilities, attributes and commands
 *****************************************************************
 * Namespace:			"wackford"
 *
 * Capabilities:		"polling"
 *						"refresh"
 *
 * Custom Attributes:	"dial"
 *						"info"
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

	definition(name:"Quirky Wink Nimbus", namespace:"wackford", author:"Todd Wackford") {

		capability "Polling"
		capability "Refresh"
		capability "Sensor"

		attribute "dial", "string"
		attribute "info", "string"
	}

	tiles {
		standardTile("dial", "device.dial", width: 2, height: 2){
			state("dial", label : '${currentValue}', unit : "", icon:"st.custom.quirky.quirky-device" )
		}
		valueTile("info", "device.info", inactiveLabel: false, decoration: "flat") {
			state "info", label:'Dial is displaying ${currentValue}', unit:""
		}
		standardTile("refresh", "device.refresh", inactiveLabel: false, decoration: "flat") {
			state "default", action:"refresh.refresh", icon:"st.secondary.refresh"
		}
	}
	main(["dial"])
	details(["dial","info","refresh" ])
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


def poll() {
	log.debug "Nimbus executing 'pollNimbus'"
	parent.pollNimbus(this)
}

def refresh() {
	log.debug "Nimbus executing 'refresh'"
	parent.pollNimbus(this)
}