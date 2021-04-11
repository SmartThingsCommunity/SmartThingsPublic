metadata {
    definition(name: "Dummy Switch", namespace: "BrettSheleski", author: "Brett Sheleski") {
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
}

def on(){
	log.debug "ON"
    
	sendEvent(name: "switch", value: "on");
}

def off(){
	log.debug "OFF"
    
	sendEvent(name: "switch", value: "off");
}