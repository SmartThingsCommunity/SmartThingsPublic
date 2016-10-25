metadata {
	// Automatically generated. Make future change here.
	definition (name: "MY Zigbee Dimmer255", namespace: "smartthings", author: "SmartThings") {
		capability "Actuator"
		capability "Switch"
		capability "Sensor"
                capability "Switch Level"
                capability "Refresh"

		fingerprint profileId: "0104", inClusters: "0000,0003,0006,0008", outClusters: "0019"
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
                controlTile("levelSliderControl", "device.level", "slider", height: 1, width: 3, inactiveLabel: false) {
	            state "level", action:"switch level.setLevel"
		}
                valueTile("level", "device.level", inactiveLabel: false, decoration: "flat") {
		    state "level", label:'${currentValue} %', unit:"%", backgroundColor:"#ffffff"
		}

		main "switch"
		details (["switch","refresh","level","levelSliderControl"])
	}
}

// Parse incoming device messages to generate events
def parse(String description) {
	/*if (description?.startsWith("catchall: 0104 000A")) {
		log.debug "Dropping catchall for SmartPower Outlet"
		return []
	}*/
    log.trace description
    if (description?.startsWith("catchall:")) {
		def msg = zigbee.parse(description)
		log.trace msg
		log.trace "data: $msg.data"
        if(msg.command == 0x01){
        	if(msg.clusterId == 0x0006 && msg.data[0] == 0x00 && msg.data[1] == 0x00){
                def name = "switch" 
                def value = (msg.data[4] != 0 ? "on" : "off") 
                log.debug"name:$name,value:$value"
                def result = createEvent(name: name, value: value)
                return result
            }
        }else if(msg.command == 0x0b && msg.clusterId == 0x0006){
        	def name = "switch" 
            def value = (msg.data[0] != 0 ? "on" : "off") 
            log.debug"name:$name,value:$value"
            def result = createEvent(name: name, value: value)
            return result
        }
	}else if(description?.startsWith("read attr")){
    	def descMap = parseDescriptionAsMap(description)
		log.debug "Read attr: $description"
    	if(descMap.cluster == "0008" && descMap.attrId == "0000"){
        	def name = "level" 
            def value = Integer.parseInt(descMap.value, 16) 
            value = Math.round(value * 100/255)
            log.debug"name:$name,value:$value"
            def result = createEvent(name: name, value: value)
            return result
        }
    }/*else {
    	log.debug "parse description: $description"
		def name = description?.startsWith("on/off: ") ? "switch" : null
		def value = name == "switch" ? (description?.endsWith(" 1") ? "on" : "off") : null
		def result = createEvent(name: name, value: value)
		log.debug "Parse returned ${result?.descriptionText}"
		return result
	}*/
}

def parseDescriptionAsMap(description) {
	(description - "read attr - ").split(",").inject([:]) { map, param ->
		def nameAndValue = param.split(":")
		map += [(nameAndValue[0].trim()):nameAndValue[1].trim()]
	}
}

def refresh() {
	log.debug "refresh()"
    def cmds = []
    cmds << "st rattr 0x${device.deviceNetworkId} 1 0x0006 0x0000"
    cmds << "delay 200"
    cmds << "st rattr 0x${device.deviceNetworkId} 1 0x0008 0x0000"
}

// Commands to device
def on() {
	log.debug "on()"
    def cmds = []
	cmds << "st cmd 0x${device.deviceNetworkId} 1 6 1 {}"
    cmds << "delay 5000"
    cmds << "st rattr 0x${device.deviceNetworkId} 1 0x0008 0x0000"
}

def off() {
	log.debug "off()"
    def cmds = []
	cmds << "st cmd 0x${device.deviceNetworkId} 1 6 0 {}"
    cmds << "delay 5000"
    cmds << "st rattr 0x${device.deviceNetworkId} 1 0x0008 0x0000"
}

def setLevel(value) {
	log.trace "setLevel($value)"
	def cmds = []

	if (value == 0) {
		sendEvent(name: "switch", value: "off")
		//cmds << "st cmd 0x${device.deviceNetworkId} 1 6 0 {}"
	}
	else if (device.latestValue("switch") == "off") {
        sendEvent(name: "switch", value: "on")
        //cmds << "st cmd 0x${device.deviceNetworkId} 1 6 1 {}"
        
	}
    //def transitionTime = Math.round(Math.abs(((device.currentValue("level")?:0) as int) - (value as int))*0.3)
    //def transitionTime2 = hexString(transitionTime>>8)
    //def transitionTime1 = hexString(transitionTime%256)
	sendEvent(name: "level", value: value)
    def level = hexString(Math.round(value * 255/100))
	cmds << "st cmd 0x${device.deviceNetworkId} 1 8 4 {${level} FFFF}"
    cmds << "delay 5000"
    cmds << "st rattr 0x${device.deviceNetworkId} 1 0x0008 0x0000"

	log.debug cmds
	cmds
}
