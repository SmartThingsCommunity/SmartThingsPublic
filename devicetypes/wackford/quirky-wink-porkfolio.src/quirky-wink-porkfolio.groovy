/* Quirky-Wink-Porkfolio-Device.groovy
 *
 *  Author: todd@wackford.net
 *  Date: 2014-02-22
 *
 *****************************************************************
 *     Setup Namespace, acpabilities, attributes and commands
 *****************************************************************
 * Namespace:			"wackford"
 *
 * Capabilities:		"acceleration"
 *						"battery"
 *						"polling"
 *						"refresh"
 *
 * Custom Attributes:	"balance"
 *						"goal"
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
metadata {

	definition(name:"Quirky Wink Porkfolio", namespace:"wackford", author:"Todd Wackford") {

		capability "Acceleration Sensor"
		capability "Battery"
		capability "Polling"
		capability "Refresh"
		capability "Sensor"

		attribute "balance", "string"
		attribute "goal", "string"
	}

	tiles {
		standardTile("acceleration", "device.acceleration", width: 2, height: 2, canChangeIcon: true) {
			state "inactive", label:'pig secure', icon:"st.motion.acceleration.inactive", backgroundColor:"#44b621"
			state "active", label:'pig alarm', icon:"st.motion.acceleration.active", backgroundColor:"#FF1919"
		}
		standardTile("balance", "device.balance", inactiveLabel: false, canChangeIcon: true) {
			state "balance", label:'${currentValue}', unit:"", icon:"st.Food & Dining.dining18"
		}
		standardTile("goal", "device.goal", inactiveLabel: false, decoration: "flat", canChangeIcon: true) {
			state "goal", label:'${currentValue} goal', unit:"", icon:"st.Weather.weather2"
		}
		standardTile("refresh", "device.refresh", inactiveLabel: false, decoration: "flat") {
			state "default", action:"refresh.refresh", icon:"st.secondary.refresh"
		}
	}
	main(["acceleration", "balance"])
	details(["acceleration", "balance", "goal", "refresh" ])
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
	log.debug "Executing 'poll'"
	parent.poll(this)
}

def refresh() {
	log.debug "Executing 'refresh'"
	parent.poll(this)
}
