/**
 *  Wink Relay Switch
 *  Description: Child device handler for the Wink Relay device. Controls the individual relay switches. 
 *    MUST be installed alongside the parent DTH.
 *
 *  Copyright 2018 Josh Lyon
 *
 */
metadata {
	definition (name: "Wink Relay Switch", namespace: "joshualyon", author: "Josh Lyon") {
		capability "Switch"
		command "toggle"
	}

	tiles {
		standardTile("switch", "device.switch", width: 2, height: 2, decoration: "flat") {
            state "off", label: ' ', action: "on",
                    icon: "st.switches.switch.off", backgroundColor: "#ffffff"
            state "on", label: ' ', action: "off",
                    icon: "st.switches.switch.on", backgroundColor: "#00a0dc"
        }
	}
}

// parse events into attributes
def parse(String description) {
	log.debug "Parsing '${description}'"
	// TODO: handle 'switch' attribute

}

def getTileLabel(){
	if(device != null){
    	return device.label
    }
}

// handle commands
def on() {
    return parent.relayOn(device.deviceNetworkId)
}

def off() {
	return parent.relayOff(device.deviceNetworkId)
}

def toggle() {
	return parent.relayToggle(device.deviceNetworkId)
}