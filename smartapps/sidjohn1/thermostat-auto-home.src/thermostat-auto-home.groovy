/**
 *  Thermostat Auto Home
 *
 *  Author: sidjohn1@gmail.com
 *  Date: 2013-07-23
 */

// Automatically generated. Make future change here.
definition(
    name: "Thermostat Auto Home",
    namespace: "sidjohn1",
    author: "sidjohn1@gmail.com",
    description: "Simply marks any thermostat home after someone arrives. Great for Nest",
    category: "Green Living",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience%402x.png",
    oauth: false
)

preferences {

	section("When one of these people arrive at home") {
		input "presence1", "capability.presenceSensor", title: "Who?", multiple: true, required: true
	}
	section("Set this thermostat staus to home") {
		input "thermostat1", "capability.thermostat", title: "Which?", multiple: true, required: true
    }
}

def installed()
{
	subscribe(presence1, "presence", presenceHandler)
}

def updated()
{
	unsubscribe()
	subscribe(presence1, "presence", presenceHandler)
}

def presenceHandler(evt)
{
	log.debug "presenceHandler $evt.name: $evt.value"
	def current = presence1.currentValue("presence")
	log.debug current
	def presenceValue = presence1.find{it.currentPresence == "present"}
	log.debug presenceValue
	if(presenceValue){
    	thermostat1?.present()
		log.debug "SmartThings changed your thermostat to home because someone arrived home"
	}
}