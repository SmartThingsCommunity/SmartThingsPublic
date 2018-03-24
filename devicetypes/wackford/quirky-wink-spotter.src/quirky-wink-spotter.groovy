/**
 *  Quirky-Wink-Spotter-Device.groovy
 *
 *  Author: todd@wackford.net
 *  Date: 2014-02-19
 *
 *****************************************************************
 *     Setup Namespace, capabilities, attributes and commands
 *****************************************************************
 * Namespace:			"wackford"
 *
 * Capabilities:		"Polling"
 *						"Battery"
 *						"Temperature Measurement"
 *						"Acceleration Sensor"
 *						"Refresh"
 *						"Motion Sensor"
 *						"Relative Humidity Measurement"
 *
 * Custom Attributes:	"sound"
 *						"light"
 *						"powerSource"
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
	definition(name:"Quirky Wink Spotter", namespace:"wackford", author:"Todd Wackford") {

		capability "Polling"
		capability "Battery"
		capability "Temperature Measurement"
		capability "Acceleration Sensor"
		capability "Refresh"
		capability "Motion Sensor"
		capability "Relative Humidity Measurement"
		capability "Sensor"

		attribute "sound", "enum", ["active","inactive"]
		attribute "light", "enum", ["active","inactive"]
		attribute "powerSource", "enum", ["powered","battery"]
	}

	tiles {
		standardTile("acceleration", "device.acceleration", width: 2, height: 2, canChangeIcon: true) {
			state "active", label:'active', icon:"st.motion.acceleration.active", backgroundColor:"#00A0DC"
			state "inactive", label:'inactive', icon:"st.motion.acceleration.inactive", backgroundColor:"#cccccc"
		}
		valueTile("temperature", "device.temperature", canChangeIcon: false)
			{
				state("temperature", label : '${currentValue}°', unit : "F",
					backgroundColors:[
						[value: 31, color: "#153591"],
						[value: 44, color: "#1e9cbb"],
						[value: 59, color: "#90d2a7"],
						[value: 74, color: "#44b621"],
						[value: 84, color: "#f1d801"],
						[value: 95, color: "#d04e00"],
						[value: 96, color: "#bc2323"]
					]
				)
			}
		valueTile("humidity", "device.humidity", inactiveLabel: false, canChangeIcon: false) {
			state "humidity", label:'${currentValue}% RH', unit:""
		}
		standardTile("sound", "device.sound", inactiveLabel: false) {
			state "active", label: "noise", unit:"", icon: "st.alarm.beep.beep", backgroundColor: "#00A0DC"
			state "inactive", label: "quiet", unit:"", icon: "st.alarm.beep.beep", backgroundColor: "#cccccc"
		}
		standardTile("light", "device.light", inactiveLabel: false, canChangeIcon: true) {
			state "active", label: "light", unit:"", icon: "st.illuminance.illuminance.bright", backgroundColor: "#00A0DC"
			state "inactive", label: "dark", unit:"", icon: "st.illuminance.illuminance.dark", backgroundColor: "#B2B2B2"
		}
		valueTile("battery", "device.battery", decoration: "flat", inactiveLabel: false, canChangeIcon: false) {
			state "battery", label: '${currentValue}% battery'
		}
		standardTile("powerSource", "device.powerSource", inactiveLabel: false, canChangeIcon: true) {
			state "powered", label: "powered", icon: "st.switches.switch.on", backgroundColor: "#79b821"
			state "battery", label: "battery", icon: "st.switches.switch.on", backgroundColor: "#ffa81e"
		}
		standardTile("refresh", "device.temperature", inactiveLabel: false, decoration: "flat") {
			state "default", action:"refresh.refresh", icon:"st.secondary.refresh"
		}
	}
	main(["acceleration", "temperature", "humidity", "sound", "light", "powerSource"])
	details(["acceleration", "temperature", "humidity", "sound", "light", "powerSource", "battery", "refresh" ])
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
