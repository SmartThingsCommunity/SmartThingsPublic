metadata {
	definition (name: "Securifi SZ-DWS02 Door Window Sensor", namespace: "jfurtner", author: "jfurtner") {
		capability "Contact Sensor"
		capability "Sensor"
        capability "Configuration"
        //capability "Speech Recognition"
        capability "Tamper Alert"
        
        command "enrollResponse"
        
		fingerprint endpointId: '08', profileId: '0104', inClusters: "0000,0003,0500", outClusters: "0003"
	}

	// simulator metadata
	simulator {
		status "active": "zone status 0x0031"
		status "inactive": "zone status 0x0030"
	}

	// UI tile definitions
	tiles(scale:2) {
		standardTile("contact", "device.contact", width: 4, height: 2) {
			state("open", label:'open', icon:"st.contact.contact.open", backgroundColor:"#53a7c0")
			state("closed", label:'closed', icon:"st.contact.contact.closed", backgroundColor:"#ffffff")
		}
        
        standardTile("tamperSwitch", "device.tamper", width: 2, height: 2) {
			state("detected", label:'${name}', icon:"st.contact.tamper.open", backgroundColor:"#ffa81e")
			state("clear", label:'${name}', icon:"st.contact.contact.closed", backgroundColor:"#79b821")
		}
        
        valueTile("logEvent", "device.phraseSpoken", width: 6, height: 2, decoration: 'flat', inactiveLabel: true) {
        	state("val", label:'${currentValue}')
        }
		
		main ("contact")
		details([
        	"contact"
            ,"tamperSwitch"
            ,"logEvent"
        	])
	}
}

def configure() {
	logDebug("** DWS02 ** configure called for device with network ID ${device.deviceNetworkId}")
    
	String zigbeeId = swapEndianHex(device.hub.zigbeeId)
	logDebug "Configuring Reporting, IAS CIE, and Bindings."
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
	logDebug "Sending enroll response"
    [	
    	
	"raw 0x500 {01 23 00 00 00}", "delay 200",
    "send 0x${device.deviceNetworkId} 1 1"
        
    ]
}

def logDebug(String message) {
	log.debug(message)
    //sendEvent(name: 'phraseSpoken', value: message)
}

// Parse incoming device messages to generate events
def parse(String description) {
	logDebug("** DWS02 parse received ** ${description}")
    def result = []        
	Map map = [:]
    
    if (description?.startsWith('zone status')) {
	    map = parseIasMessage(description)
    }
 
	//logDebug "Parse returned $map"
    map.each { k, v ->
    	//logDebug("sending event ${v}")
        sendEvent(v)
    }
    
//	def result = map ? createEvent(map) : null
    
    if (description?.startsWith('enroll request')) {
    	List cmds = enrollResponse()
        logDebug "enroll response: ${cmds}"
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
            //logDebug 'closed'
            resultMap["contact"] = getContactResult("closed")
            resultMap["tamper"] = getTamperResult('clear')
            break

        case '0x0031': // Open/Motion/Wet
            //logDebug 'open'
            resultMap["contact"] = getContactResult("open")
            resultMap["tamper"] = getTamperResult('clear')
            break

        case '0x0032': // Tamper Alarm
        	//logDebug 'open with tamper alarm'
            resultMap["contact"] = getContactResult("open")
            resultMap["tamper"] = getTamperResult('detected')
            break

        case '0x0034': // Supervision Report
        	//logDebug 'closed with tamper alarm'
            resultMap["contact"] = getContactResult("closed")
            resultMap["tamper"] = getTamperResult('detected')
            break

        case '0x0035': // Restore Report
        	//logDebug 'open with tamper alarm'
            resultMap["contact"] = getContactResult("open")
            resultMap["tamper"] = getTamperResult('detected')
            break

//        case '0x0036': // Trouble/Failure
//        	logDebug 'msgCode 36 not handled yet'
//            break

        default:
        	logDebug "msgCode ${msgCode}: ${description}"
        	break
    }
    return resultMap
}

private Map getContactResult(value) {
	return [
		name: 'contact',
		value: value
	]
}

private Map getTamperResult(value) {
	//logDebug "Tamper Switch Status ${value}"
	def linkText = getLinkText(device)
	def descriptionText = "${linkText} was tampered: ${value == 'detected' ? 'yes' : 'no'}"
	return [
		name: 'tamper',
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