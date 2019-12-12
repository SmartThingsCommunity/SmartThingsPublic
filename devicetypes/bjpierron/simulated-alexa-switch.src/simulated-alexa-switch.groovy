metadata {

    definition (name: "Simulated Alexa Switch", namespace: "bjpierron", author: "bjpierron") {
        capability "Switch"
        capability "Sensor"
        capability "Actuator"
        capability "Contact Sensor"	    		
    }
    
    simulator {
		status "open": "contact:open"
		status "closed": "contact:closed"
	}

    tiles {
        standardTile("switch", "device.switch", width: 2, height: 2, canChangeIcon: true) {
            state "off", label: '${currentValue}', action: "switch.on", icon: "st.switches.switch.off", backgroundColor: "#ffffff"
            state "on", label: '${currentValue}', action: "switch.off", icon: "st.switches.switch.on", backgroundColor: "#00A0DC"
        }
        main "switch"
        details(["switch"])
    }
}

def parse(description) {
}

def on() {
    sendEvent(name: "switch", value: "on")
    sendEvent(name: "contact", value: "open")
}

def off() {
    sendEvent(name: "switch", value: "off")
    sendEvent(name: "contact", value: "closed")
}