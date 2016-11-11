metadata {
	definition (name: "Securifi SZ-PIR02", namespace: "sozonoff", author: "Sozonoff") {
		capability "Motion Sensor"
		capability "Sensor"
        capability "Configuration"
        
        attribute "tamperSwitch","ENUM",["open","closed"]
                
        command "enrollResponse"
        
		fingerprint endpointId: '08', profileId: '0104', inClusters: "0000,0003,0500", outClusters: "0003"
	}

	// simulator metadata
	simulator {
		status "active": "zone report :: type: 19 value: 0031"
		status "inactive": "zone report :: type: 19 value: 0030"
	}

	// UI tile definitions
	tiles {
		standardTile("motion", "device.motion", width: 2, height: 2) {
			state("active", label:'motion', icon:"st.motion.motion.active", backgroundColor:"#53a7c0")
			state("inactive", label:'no motion', icon:"st.motion.motion.inactive", backgroundColor:"#ffffff")
		}
        
        standardTile("tamperSwitch", "device.tamperSwitch", width: 1, height: 1) {
			state("open", label:'${name}', icon:"st.contact.contact.open", backgroundColor:"#ffa81e")
			state("closed", label:'${name}', icon:"st.contact.contact.closed", backgroundColor:"#79b821")
		}        
		
		main (["motion"])
		details(["motion","tamperSwitch"])
	}
}

def configure() {
	log.debug("** PIR02 ** configure called for device with network ID ${device.deviceNetworkId}")
    
	String zigbeeId = swapEndianHex(device.hub.zigbeeId)
	log.debug "Configuring Reporting, IAS CIE, and Bindings."
	def configCmds = [
    	"zcl global write 0x500 0x10 0xf0 {${zigbeeId}}", "delay 200",
		"send 0x${device.deviceNetworkId} 1 1", "delay 1500",
        
        "zcl global send-me-a-report 1 0x20 0x20 0x3600 0x3600 {01}", "delay 200",
        "send 0x${device.deviceNetworkId} 1 1", "delay 1500",
        
		"zdo bind 0x${device.deviceNetworkId} 1 1 0x001 {${device.zigbeeId}} {}", "delay 1500",
        
        "raw 0x500 {01 23 00 00 00}", "delay 200",
        "send 0x${device.deviceNetworkId} 1 1", "delay 1500",
	]
    return configCmds // send refresh cmds as part of config
}

def enrollResponse() {
	log.debug "Sending enroll response"
    [	
    	
	"raw 0x500 {01 23 00 00 00}", "delay 200",
    "send 0x${device.deviceNetworkId} 1 1"
        
    ]
}

// Parse incoming device messages to generate events
def parse(String description) {
	log.debug("** PIR02 parse received ** ${description}")
    def result = []        
	Map map = [:]
    
    if (description?.startsWith('zone status')) {
	    map = parseIasMessage(description)
    }
 
	log.debug "Parse returned $map"
    map.each { k, v ->
    	log.debug("sending event ${v}")
        sendEvent(v)
    }
    
//	def result = map ? createEvent(map) : null
    
    if (description?.startsWith('enroll request')) {
    	List cmds = enrollResponse()
        log.debug "enroll response: ${cmds}"
        result = cmds?.collect { new physicalgraph.device.HubAction(it) }
    }
    return result
}

private Map parseIasMessage(String description) {
    List parsedMsg = description.split(' ')
    String msgCode = parsedMsg[2]
    
    Map resultMap = [:]
    switch(msgCode) {
        case '0x0030': // Closed/No Motion/Dry
            log.debug 'no motion'
            resultMap["motion"] = [name: "motion", value: "inactive"]
            resultMap["tamperSwitch"] = getContactResult("closed")            
            break

        case '0x0031': // Open/Motion/Wet
            log.debug 'motion'
            resultMap["motion"] = [name: "motion", value: "active"]
            resultMap["tamperSwitch"] = getContactResult("closed")            
            break

        case '0x0032': // Tamper Alarm
        	log.debug 'motion with tamper alarm'
            resultMap["motion"] = [name: "motion", value: "active"]
            resultMap["tamperSwitch"] = getContactResult("open")            
            break

        case '0x0034': // Supervision Report
        	log.debug 'no motion with tamper alarm'
            resultMap["motion"] = [name: "motion", value: "inactive"]
            resultMap["tamperSwitch"] = getContactResult("open")            
            break

        case '0x0035': // Restore Report
        	log.debug 'motion with tamper alarm'
            resultMap["motion"] = [name: "motion", value: "active"]
            resultMap["tamperSwitch"] = getContactResult("open") 
            break

        case '0x0036': // Trouble/Failure
        	log.debug 'msgCode 36 not handled yet'
            break

        case '0x0038': // Test Mode
            break
    }
    return resultMap
}

private Map getContactResult(value) {
	log.debug "Tamper Switch Status ${value}"
	def linkText = getLinkText(device)
	def descriptionText = "${linkText} was ${value == 'open' ? 'opened' : 'closed'}"
	return [
		name: 'tamperSwitch',
		value: value,
		descriptionText: descriptionText
	]
}


private hex(value) {
	new BigInteger(Math.round(value).toString()).toString(16)
}

private String swapEndianHex(String hex) {
    reverseArray(hex.decodeHex()).encodeHex()
}

private byte[] reverseArray(byte[] array) {
    int i = 0;
    int j = array.length - 1;
    byte tmp;
    while (j > i) {
        tmp = array[j];
        array[j] = array[i];
        array[i] = tmp;
        j--;
        i++;
    }
    return array
}