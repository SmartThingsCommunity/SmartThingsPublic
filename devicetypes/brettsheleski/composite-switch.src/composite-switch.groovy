metadata {
    definition(name: "Composite Switch", namespace: "BrettSheleski", author: "Brett Sheleski") {
		capability "Switch"
    }

	tiles {
		multiAttributeTile(name:"switch", type: "generic", width: 6, height: 4, canChangeIcon: true){
			tileAttribute ("device.switch", key: "PRIMARY_CONTROL") {
				attributeState "on", label: '${name}', action: "off", icon: "st.switches.switch.on", backgroundColor: "#79b821"
				attributeState "off", label: '${name}', action: "on", icon: "st.switches.switch.off", backgroundColor: "#ffffff"
			}
		}

		main "switch"
		details(["switch"])
	}

    preferences {
        input "onButton", "capability.momentary", title: "On Button", description: "The button to press when going to the ON state", displayDuringSetup: true
        input "offButton", "capability.momentary", title: "Off Button", description: "The button to press when going to the OFF state", displayDuringSetup: true
    }
}

def on(){
	log.debug "ON"
    
    onButton.push()
	sendEvent(name: "switch", value: "on")
}

def off(){
	log.debug "OFF"
    
    offButton.push()
	sendEvent(name: "switch", value: "off")
}