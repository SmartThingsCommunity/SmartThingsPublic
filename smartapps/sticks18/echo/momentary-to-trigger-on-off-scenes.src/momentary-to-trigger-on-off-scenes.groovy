definition(
name: "Momentary To Trigger On/Off Scenes",
namespace: "sticks18/echo",
author: "sgibson18@gmail.com",
parent: "sticks18/parent:Stateless Scene via Echo",
description: "Use a stateless momentary to allow voice commands to avoid sync issues",
category: "My Apps",
iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience%402x.png"
)

preferences {
	section("Select master momentary switch to monitor"){
		input "theSwitch", "capability.momentary", multiple: false, required: true
	}
section("Trigger this scene tile if master momentary is turned On"){
	input "switchesOn", "capability.momentary", multiple: true, required: false
}
section("Trigger this scene tile if master momentary is turned Off"){
	input "switchesOff", "capability.momentary", multiple: true, required: false
}
}

def installed()
{
initialize()
}

def updated()
{
	unsubscribe()
initialize()
}


def toggleHandler(evt) {
	log.info "Triggering ${evt.value} command"
(evt.value == "on" ? switchesOn?.on() : switchesOff?.on())   
}

def initialize() {
	subscribe(theSwitch, "pushtype", toggleHandler)
}