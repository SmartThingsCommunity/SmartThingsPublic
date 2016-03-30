/**
 *  Momentary Button Tile
 *
 *  Author: SmartThings
 *
 *  Date: 2013-05-01
 */
metadata {
	// Automatically generated. Make future change here.
	definition (name: " High Med Low Fan ", namespace: "Virtual", author: "SmartThings") {
		capability "Actuator"
		capability "Switch"
		capability "Momentary"
		capability "Sensor"
	}

	// simulator metadata
	simulator {
	}

	// UI tile definitions
	tiles {
		standardTile("switch", "device.switch", width: 2, height: 2, canChangeIcon: true) {
			state "Low", label: 'High', action: "momentary.push", backgroundColor: "#53a7c0", nextState: "Med"
			state "Med", label: 'Med', action: "momentary.push", backgroundColor: "#53a7c0" , nextState: "Low" 
            state "High", label: 'Low', action: "momentary.push", backgroundColor: "#53a7c0", nextState: "Off"
            state "Off", label: 'Off', action: "momentary.push", backgroundColor: "#ffffff", nextState: "Low"
		}
		main "switch"
		details "switch"
	}
}

def parse(String description) {
}

def push() {
	log.debug "Push"
    //sendEvent(name: "switch", value: "on", isStateChange: true, display: false)
	//sendEvent(name: "switch", value: "off", isStateChange: true, display: false)
	sendEvent(name: "momentary", value: "pushed", isStateChange: true)
}
/*
def Low() {
	log.debug "Push low"
    push()
}

def Med() {
	log.debug "Push med"
    push()
}
def High() {
	log.debug "Push high"
    push()
}

def Off() {
	log.debug "Push Off"
    push()
}*/