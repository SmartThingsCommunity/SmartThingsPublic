/**
 *  smartelligynt smartthings event collector
 *
 *  Copyright 2017 Rahul
 *
 */
definition(
    name: "smartelligynt smartthings event collector",
    namespace: "smartelligynt",
    author: "Rahul",
    description: "samsung smartthings event collector",
    category: "SmartThings Labs",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    oauth: [displayName: "smartelligynt", displayLink: "http://www.smartelligynt.com"])

import groovy.json.JsonSlurper

preferences {
	section("Select your devices to monitor with smartelligynt") {
		        input "switches", "capability.switch", title: "Switches", multiple: true, required: false
                input "contacts", "capability.contactSensor", title: "Contact Sensors", multiple: true, required: false
                input "accelerometers", "capability.accelerationSensor", title: "Accelerometers", multiple: true, required: false



	}
}

def installed() {
	log.debug "Installed with settings: ${settings}"

	initialize()
}

def updated() {
	log.debug "Updated with settings: ${settings}"

	unsubscribe()
	initialize()
}

def initialize() {
	// TODO: subscribe to attributes, devices, locations, etc.
    log.debug "Updated with settings: ${settings}"

    subscribeToDeviceEvents()
}

def subscribeToDeviceEvents() {
	log.debug "Updated with settings: ${switches}"
	if (null  != accelerometers) {
		subscribe(accelerometers, "acceleration", handleEvents)
	}
    if (null  != switches) {
		subscribe(switches, "switches", handleEvents)
	}
    if (null  != contacts) {
		subscribe(contacts, "contacts", handleEvents)
	}
    

}


def handleEvents(evt) {
	log.debug "$evt.displayName($evt.name:$evt.unit) $evt.value"

	if (evt.unit != null) {
		eventName = "$evt.displayName(${evt.name}_$evt.unit)"
	}
	def eventValue = "$evt.value"

	postEvents(eventName, eventValue)
}

def postEvents(eventName, eventValue)
{
	def epoch = now() / 1000

	def jsonSlurper = new JsonSlurper()
    def txt = "{ \"en\": \"$eventName\", \"ev\" : \"$eventValue\", \"et\" : $epoch }"
	def object = jsonSlurper.parseText(txt)

	def params = [
    	uri: "http://collect.smartelligynt.com/api/events/",
    	body: 
       		object
    
	]

	try {
    	httpPostJson(params) { resp ->
        resp.headers.each {
            log.debug "${it.name} : ${it.value}"
        }
        log.debug  "response status code: ${resp.status}"
        log.debug "response data: ${resp.data}"

    }
	} catch (e) {
    	log.debug "something went wrong: $e"
	}



}

// TODO : Implement mappings
// TODO: implement event handlers