metadata {
	definition (name: "Rooflight Controller", namespace: "CR76", author: "Cameron.Reid@Glazingvision.co.uk") {
        capability "Refresh"
        capability "Polling"
        capability "Switch"
        capability "Switch level"

        attribute "Rooflight", "string"
        attribute "Weather", "string"
        attribute "Thermostat", "string"
        
        command "Stop"
        
    	fingerprint profileId: "0104", inClusters: "0000,0006,0008"    
	}

	// simulator metadata
	simulator {
    }

	// UI tile definitions
	tiles {
		multiAttributeTile(name:"sliderTile", type: "generic", width:6, height:4) {
    		tileAttribute("device.Rooflight", key: "PRIMARY_CONTROL") {
        		attributeState "Open", label:'Open', action: "Switch.off", backgroundColor:"#79b821", nextState:"Closing"
        		attributeState "Closed", label:'Closed', action: "Switch.on", backgroundColor:"#ffffff", nextState:"Opening"
        		attributeState "Opening", label:'Opening', backgroundColor:"#79b821"
        		attributeState "Closing", label:'Closing', backgroundColor:"#ffffff"
    		}
        	tileAttribute ("device.Rooflight", key: "SECONDARY_CONTROL") {
				attributeState "Open", label: 'Push to close.', nextState:"Closing"
            	attributeState "Closed", label: 'Push to open.', nextState:"Opening"
            	attributeState "Opening", label: 'Skylight opening.'
            	attributeState "Closing", label: 'Skylight closing.'
            }
        }
        standardTile("Close", "device.Rooflight", inactiveLabel: false, decoration: "flat", width:2, height:2 ) {
			state "default", label: 'Close', action:"Switch.off", icon:"st.thermostat.thermostat-left"
		}
        standardTile("Stop", "device.level", inactiveLabel: false, decoration: "flat", width:2, height:2 ) {
			state "default", action:"Stop", icon:"st.sonos.stop-btn"
		}
        standardTile("Open", "device.Rooflight", inactiveLabel: false, decoration: "flat", width:2, height:2 ) {
			state "default", label: 'open', action:"Switch.on", icon:"st.thermostat.thermostat-right"
		}
        standardTile("Rain", "device.Weather", width: 2, height: 2) {
        	state "No", backgroundColor: "#ffffff"
    		state "Dry", label: 'Dry', icon:"st.Weather.weather14", backgroundColor: "#ffffff"
    		state "Raining", label: 'Raining', icon:"st.Weather.weather10", backgroundColor: "#153591"
		}
        standardTile("refresh", "device.refresh", inactiveLabel: false, decoration: "flat", width:2, height:2 ) {
			state "default", action:"refresh.refresh", icon:"st.secondary.refresh"
		}
        
		main ("sliderTile")
		details (["sliderTile","Close","Stop","Open","Rain","refresh"])
	}
}

// Parse incoming device messages to generate events
def parse(String description) {
    log.debug "Parse description $description"
    def name = null
    def value = null
 	
    if (description?.startsWith("catchall: 0104 0006 38")) {
        log.debug "On/Off command received from EP 38 open / close command"
        if (description?.endsWith("01 01 0000001000")) {
        	name = "Rooflight"
        	value = "Closed"
        }
        else if (description?.endsWith("01 01 0000001001")) {
        	name = "Rooflight"
            value = "Open"
        }
       else if (description?.endsWith("01 01 0000001003")) {
        	name = "Rooflight"
            value = "Opening"
       } 
       else if (description?.endsWith("01 01 0000001004")) {
        	name = "Rooflight"
            value = "Closing"
       } 
    }
    if (description?.startsWith("catchall: 0104 0006 39")) {
        log.debug "On/Off command received from EP 39 is rain sensor connected"
        if (description?.endsWith("01 01 0000001000")) {
        	name = "Weather"
            value = "No"
            state.tile = 1
        }
        else if (description?.endsWith("01 01 0000001001")) {
        	name = "Weather"
            value = "Dry"
            state.tile = 0
            log.debug "${state.tile}"
        }
    }
    if (description?.startsWith("catchall: 0104 0006 40"))  {
        log.debug "On/Off command received from EP 40 Thermostat"
        if (description?.endsWith("01 01 0000001000")){
        	name = "Switch"
            value = "on"
        }
        else if (description?.endsWith("01 01 0000001001")) {
        	name = "Switch"
            value = "off"
        }
    }
    if (description?.startsWith("catchall: 0104 0006 41")) {
        log.debug "On/Off command received from EP 41 rain sensor"
       	if (description?.endsWith("01 01 0000001000")) {
        	name = "Weather"
        	value = "Dry"
        }
        else if (description?.endsWith("01 01 0000001001")) {
        	name = "Weather"
        	value = "Raining"
        }
    }
    if(description?.startsWith('read attr -')) {
        def descMap = parseDescriptionAsMap(description) 						//Get level value.
        value = zigbee.convertHexToInt(descMap.value)
        log.debug " level = $value"
        name = "level"
    }
    def result = createEvent(name: name, value: value)
    log.debug "Parse returned ${result?.descriptionText}"
    return result 
}

def parseDescriptionAsMap(description) {
    (description - "read attr - ").split(",").inject([:]) {
    	map, param ->
        def nameAndValue = param.split(":")
        map += [(nameAndValue[0].trim()):nameAndValue[1].trim()]
    }
}

// Commands to device
def on() {
	log.debug "Rooflight Opening"
    def cmd = []
	cmd <<"st cmd 0x${device.deviceNetworkId} 0x38 0x0006 0x1 {}"
	cmd << "delay 250"
    cmd << zigbee.command(0x0008, 0x04, "64")
}

def off() {
	log.debug "Rooflight Closing"
    def cmd = []
	cmd << "st cmd 0x${device.deviceNetworkId} 0x38 0x0006 0x0 {}"
    cmd << "delay 250"
    cmd << zigbee.command(0x0008, 0x04, "00")
}

def Stop() {																//Send stop command in the level control cluster.
	log.debug "send stop"
	"st cmd 0x${device.deviceNetworkId} 0x38 0x0008 0x07 {}"
}

def poll() {
	log.debug "Poll is calling refresh"
	refresh()
}

def refresh() {
    log.debug "sending refresh command"
    log.debug "Tile State: ${state.tile}"
    def cmd = []
    if("${state.tile}" == "0") {											//If rain sensor is connected refresh rain sensor and rooflight position.
    	cmd << "st rattr 0x${device.deviceNetworkId} 0x38 0x0006 0x0000"	// Read on / off attribute at End point 0x38 Rooflight open / closed.
    	cmd << "delay 250"
        cmd << "st rattr 0x${device.deviceNetworkId} 0x38 0x0008 0x0000"	// Read Level position at End point 0x38 Rooflight Position.
        cmd << "delay 250"
    	cmd << "st rattr 0x${device.deviceNetworkId} 0x41 0x0006 0x0000"	// Read on / off attribute at End point 0x41 Rain sensor.
    }
    else {
        cmd << "st rattr 0x${device.deviceNetworkId} 0x38 0x0006 0x0000"	// Read on / off attribute at End point 0x38 Rooflight open / closed.
        cmd << "delay 250"
        cmd << "st rattr 0x${device.deviceNetworkId} 0x38 0x0008 0x0000"	// Read on / off attribute at End point 0x88 Rain sensor.
    }
}