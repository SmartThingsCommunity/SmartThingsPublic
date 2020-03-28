/**
 *  Stateless On/Off Button Tile
 *
 *  Author: Ronald Gouldner
 *
 *  Date: 2015-05-14
 */
metadata {
	// Automatically generated. Make future change here.
	definition (name: "Stateless On-Off Button Tile", namespace: "gouldner", author: "Ronald Gouldner") {
		capability "Actuator"
		capability "Switch"
		capability "Sensor"
	}

	// simulator metadata
	simulator {
	}

	// UI tile definitions
	tiles {
		standardTile("button", "device.switch", width: 2, height: 2, canChangeIcon: true) {
			state "offReady", label: 'Off', action: "switch.on", icon: "st.switches.switch.off", backgroundColor: "#ffffff", nextState: "onReady"
			state "onReady", label: 'On', action: "switch.off", icon: "st.switches.switch.on", backgroundColor: "#79b821", nextState: "offReady"
			state "off", label: 'Off', action: "switch.on", icon: "st.switches.switch.off", backgroundColor: "#ffffff"
			state "on", label: 'On', action: "switch.off", icon: "st.switches.switch.on", backgroundColor: "#79b821"
		}
		main "button"
		details "button"
	}
}

def parse(String description) {
}

def on() {
    log.debug "Stateless On/Off Button Tile Virtual Switch ${device.name} turned on"
    sendEvent(name: "switch", value: "on")
    sendEvent(name: "switch", value: "onReady")
}

def off() {
    log.debug "Stateless On/Off Button Tile Virtual Switch ${device.name} turned off"
    sendEvent(name: "switch", value: "off")
    sendEvent(name: "switch", value: "offReady")
}