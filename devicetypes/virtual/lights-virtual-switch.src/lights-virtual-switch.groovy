/* virtual switch */


metadata {
	definition (name: "Lights Virtual Switch", namespace: "Virtual", author: "badgermanus@gmail.com") {
		capability "Switch"
        capability "Momentary"
        capability "Refresh"
        capability "Actuator"
		capability "Polling"
		capability "Sensor"
		capability "Relay Switch"
	}

    // simulator metadata
    simulator {
        status "on":  "command: 2003, payload: FF"
        status "off": "command: 2003, payload: 00"

        // reply messages
        reply "2001FF,delay 100,2502": "command: 2503, payload: FF"
        reply "200100,delay 100,2502": "command: 2503, payload: 00"
    }

    // tile definitions
    tiles {
        standardTile("switch", "device.switch", width: 2, height: 2, canChangeIcon: true) {
            state "on", label: '${name}', action: "switch.off", icon: "st.lights.light-bulb-off", backgroundColor: "#79b821"
            state "off", label: '${name}', action: "switch.on", icon: "st.lights.light-bulb-on", backgroundColor: "#ffffff"
        }
		//standardTile("push", "device.switch", inactiveLabel: false, decoration: "flat") {
        //   state "default", label:'Push', action:"mementary.push", icon: "st.secondary.refresh-icon"
        //}
		standardTile("refresh", "device.switch", inactiveLabel: false, decoration: "flat") {
            state "default", label:'Refresh', action:"device.refresh", icon: "st.secondary.refresh-icon"
        }

        main "switch"
        details(["switch","refresh"])
    }
}


// handle commands
def on() {
	log.debug "On"
    sendEvent (name: "switch", value: "on", isStateChange:true)
}

def off() {
	log.debug "Off"
    sendEvent (name: "switch", value: "off", isStateChange:true)
}
/*
def push() {
	log.debug "Push"
    sendEvent(name: "momentary", value: "push", isStateChange:true)
}*/