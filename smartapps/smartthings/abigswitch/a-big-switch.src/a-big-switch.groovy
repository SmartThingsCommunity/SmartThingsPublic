/**
 *  A Big Switch
 *
 *  Author: SmartThings
 *
 *  Date: 2013-05-01
 */
definition(
	name: "A Big Switch",
	namespace: "smartthings/abigswitch",
	parent: "smartthings/bigswitches:Big Switches",
	author: "SmartThings",
	description: "Turns on and off a collection of lights based on the state of a specific switch.",
	category: "Convenience",
	iconUrl: "https://s3.amazonaws.com/smartapp-icons/Meta/light_outlet.png",
	iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Meta/light_outlet@2x.png"
)

preferences {
	section("When this switch is turned on or off") {
		input "master", "capability.switch", title: "Where?"
	}
	section("Turn on or off all of these switches as well") {
		input "switches", "capability.switch", multiple: true, required: false
	}
	section("And turn off but not on all of these switches") {
		input "offSwitches", "capability.switch", multiple: true, required: false
	}
	section("And turn on but not off all of these switches") {
		input "onSwitches", "capability.switch", multiple: true, required: false
	}
}

def installed()
{
	subscribe(master, "switch.on", onHandler)
	subscribe(master, "switch.off", offHandler)
}

def updated()
{
	unsubscribe()
	subscribe(master, "switch.on", onHandler)
	subscribe(master, "switch.off", offHandler)
}

def onHandler(evt) {
	log.debug evt.value
	log.debug onSwitches()
	onSwitches()?.on()
}

def offHandler(evt) {
	log.debug evt.value
	log.debug offSwitches()
	offSwitches()?.off()
}

private onSwitches() {
	if(switches && onSwitches) { switches + onSwitches }
	else if(switches) { switches }
	else { onSwitches }
}

private offSwitches() {
	if(switches && offSwitches) { switches + offSwitches }
	else if(switches) { switches }
	else { offSwitches }
}
