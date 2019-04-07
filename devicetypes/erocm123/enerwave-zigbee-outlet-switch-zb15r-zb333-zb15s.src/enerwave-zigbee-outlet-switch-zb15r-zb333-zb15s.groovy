metadata {
	// Automatically generated. Make future change here.
	definition (name: "Enerwave Zigbee Outlet/Switch ZB15R/ZB333/ZB15S", namespace: "erocm123", author: "Enerwave Home Automation", vid:"generic-switch") {
		capability "Actuator"
		capability "Switch"
		capability "Sensor"
        capability "Refresh"

		fingerprint profileId: "0104", inClusters: "0000,0003,0006", outClusters: "0019"
	}

	// simulator metadata
	simulator {
		// status messages
		status "on": "on/off: 1"
		status "off": "on/off: 0"

		// reply messages
		reply "zcl on-off on": "on/off: 1"
		reply "zcl on-off off": "on/off: 0"
	}

	// UI tile definitions
	tiles {
		standardTile("switch", "device.switch", width: 2, height: 2, canChangeIcon: true) {
			state "off", label: '${name}', action: "switch.on", icon: "st.switches.switch.off", backgroundColor: "#ffffff"
			state "on", label: '${name}', action: "switch.off", icon: "st.switches.switch.on", backgroundColor: "#79b821"
		}
         standardTile("refresh", "device.switch", inactiveLabel: false, decoration: "flat") {
            state "default", label:'', action:"refresh.refresh", icon:"st.secondary.refresh"
        }

		main "switch"
		details (["switch","refresh"])
	}
}

// Parse incoming device messages to generate events
def parse(String description) {
	/*if (description?.startsWith("catchall: 0104 000A")) {
		log.debug "Dropping catchall for SmartPower Outlet"
		return []
	}*/
    //log.debug description
    if (description?.startsWith("catchall:")) {
    	log.trace description
		def msg = zigbee.parse(description)
		log.trace msg
		log.trace "data: $msg.data"
        if(msg.command == 0x01 && msg.clusterId == 0x0006 && msg.data[0] == 0x00 && msg.data[1] == 0x00){
            def name = "switch" 
            def value = (msg.data[4] != 0 ? "on" : "off") 
            log.debug"name:$name,value:$value"
            def result = createEvent(name: name, value: value)
            return result
        }else if(msg.command == 0x0b && msg.clusterId == 0x0006){
        	def name = "switch" 
            def value = (msg.data[0] != 0 ? "on" : "off") 
            log.debug"name:$name,value:$value"
            def result = createEvent(name: name, value: value)
            return result
        }
	}else {
    	log.debug "parse description: $description"
		def name = description?.startsWith("on/off: ") ? "switch" : null
		def value = name == "switch" ? (description?.endsWith(" 1") ? "on" : "off") : null
		def result = createEvent(name: name, value: value)
		log.debug "Parse returned ${result?.descriptionText}"
		return result
	}
}

def refresh() {
	log.debug "refresh()"
    "st rattr 0x${device.deviceNetworkId} 1 0x0006 0x0000"
}

// Commands to device
def on() {
	log.debug "on()"
	"st cmd 0x${device.deviceNetworkId} 1 6 1 {}"
}

def off() {
	log.debug "off()"
	"st cmd 0x${device.deviceNetworkId} 1 6 0 {}"
}
