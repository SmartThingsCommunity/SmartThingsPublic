/**
 *  Fan Switch
 *
 *  Author: SmartThings
 *
 *  Date: 2013-05-01
 */
metadata {
	
	definition (name: " High Med Low Fan Switch ", namespace: "Virtual", author: "SmartThings") {
		capability "Actuator"
		capability "Switch"
		capability "Momentary"
		capability "Sensor"
        attribute "speed", "string"
        command "high"
        command "med"
        command "low"
	}

	// simulator metadata
	simulator {
	}

	// UI tile definitions
	tiles {
		standardTile("speed", "device.speed", width: 2, height: 2, canChangeIcon: true) {
			state "high", label: 'High', action: "med", backgroundColor: "#53a7c0", nextState: "med"
			state "med", label: 'Med', action: "low", backgroundColor: "#53a7c0" , nextState: "low" 
            state "low", label: 'Low', action: "off", backgroundColor: "#53a7c0", nextState: "off"
            state "off", label: 'Off', action: "high", backgroundColor: "#ffffff", nextState: "high"
		}
		main "speed"
		details "speed"
	}
}

def parse(String description) {
}

def push() {
	log.debug "Push"
    //sendEvent(name: "switch", value: "on", isStateChange: true, display: false)
	//sendEvent(name: "switch", value: "off", isStateChange: true, display: false)
	sendEvent(name: "momentary", value: "pushed", isStateChange: true)
    log.debug "device.speed: ${device.speed}"
}

def low() {
	log.debug "Push low"
    sendEvent(name: "speed", value: "low", isStateChange: true)
    //push()
}

def med() {
	log.debug "Push med"
    sendEvent(name: "speed", value: "med", isStateChange: true)    
    //push()
}
def high() {
	log.debug "Push high"
    sendEvent(name: "speed", value: "high", isStateChange: true)
    //push()
}

def off() {
	log.debug "Push Off"
    sendEvent(name: "speed", value: "off", isStateChange: true)
    //push()
}
