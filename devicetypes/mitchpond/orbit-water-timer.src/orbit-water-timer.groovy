metadata {
	// Automatically generated. Make future change here.
	definition (name: "Orbit Water Timer", namespace: "mitchpond", author: "Mitch Pond") {
		capability "Actuator"
		capability "Switch"
		capability "Sensor"
        capability "Configuration"
        capability "Refresh"
        
        command "test"
        command "identify"
        command "getClusters"
        
        attribute "pendingCommand", "string"

		fingerprint endpointId: "01", profileId: "0104", inClusters: "0000,0001,0003,0020,0006,0201", outClusters: "000A,0019"
	}

	// UI tile definitions
	tiles {
		standardTile("switch", "device.switch", width: 2, height: 2, canChangeIcon: true) {
			state "off", label: '${name}', action: "switch.on", icon: "st.switches.switch.off", nextState: "turningOn", backgroundColor: "#ffffff"
            state "turningOn", label: '${name}', action: "switch.off", icon: "st.switches.switch.on", nextState: "turningOff", backgroundColor: "#79b821"
            state "turningOff", label: '${name}', action: "switch.on", icon: "st.switches.switch.off", nextState: "turningOn", backgroundColor: "#ffffff"
			state "on", label: '${name}', action: "switch.off", icon: "st.switches.switch.on", nextState: "turningOff", backgroundColor: "#79b821"
		}
        valueTile("battery", "device.battery", decoration: "flat") {
			state "battery", label:'${currentValue}% battery', unit:""
		}
        standardTile("configure", "device.configure", decoration: "flat") {
			state "configure", label:'', action:"configuration.configure", icon:"st.secondary.configure"
		}
        standardTile("refresh", "command.refresh", decoration: "flat") {
        	state "default", label: '', action: 'refresh.refresh', icon: 'st.secondary.refresh'
        }
        

		main "switch"
		details (["switch","battery","refresh","configure"])
	}
}

// Parse incoming device messages to generate events
def parse(String description) {
	log.debug("Raw: $description")
    
    if (description?.startsWith("catchall:")) {
    	def report = zigbee.parse(description)
    	switch(report.clusterId) {
    		case 0x0020:
       		 	log.debug "Received Poll Control packet"
            	refresh()				//do something just to show that we can!
            	break
        	case 0x0006:
        		return (report.data).equals([0x01,0x00])? createEvent(name: 'switch', value: 'on') :
                		((report.data).equals([0x00, 0x00, 0x00, 0x10, 0x01]) && (report.command == 0x01))? createEvent(name: 'switch', value: 'on') :
                		(report.data).equals([0x00,0x00])? createEvent(name: 'switch', value: 'off') :
                        ((report.data).equals([0x00, 0x00, 0x00, 0x10, 0x00]) && (report.command == 0x01))? createEvent(name: 'switch', value: 'off') : null
               	break
        }
    }
    else if (description?.startsWith('read attr -')) {
		return parseReportAttributeMessage(description)
    }
    //log.debug(report)
    
    
}

//Perform initial device setup and binding
def configure(){
	log.debug("Running configure for Orbit timer...")
    
    [
    "zcl global send-me-a-report 0x06 0x00 0x10 0 3600 {}", "delay 200",
	"send 0x${device.deviceNetworkId} 1 1", "delay 500",
    
    //"st cmd 0x${device.deviceNetworkId} 1 0x20 0x02 {14}", "delay 500",				//set Long Poll Interval
    "st wattr 0x${device.deviceNetworkId} 1 0x20 0x00 0x23 {240}", "delay 500",
    
    "zdo bind 0x${device.deviceNetworkId} 1 1 6 {${device.zigbeeId}} {}", "delay 500",		//device reports back success
    "zdo bind 0x${device.deviceNetworkId} 1 1 0 {${device.zigbeeId}} {}", "delay 500",
    "zdo bind 0x${device.deviceNetworkId} 1 1 1 {${device.zigbeeId}} {}", "delay 500"
    ]
    refresh()
}

/* successful attribute reads
//battery voltage
"st rattr 0x${device.deviceNetworkId} 1 0x01 0x0020", "delay 500",

//cluster 20 poll control check-in is being sent by device. Should be able to alter polling duration and start fast poll



*/

def test(){
	[
    //"raw 0x000A {D8CE3955 e2 00 00 00 01}","delay 500",
    //"send 0x${device.deviceNetworkId} 1 1",
    //"st rattr 0x${device.deviceNetworkId} 1 0x01 0x0020", "delay 500",					//works!
    //"st rattr 0x${device.deviceNetworkId} 1 0x000A 0x0005", "delay 500", 					//unsupp attrib
	//"st wattr 0x${device.deviceNetworkId} 1 0x000A 0x0000 0xe2 {D8CE3955}","delay 500", 	//no response
    //"zcl global discover 0x0A 0x00 20","delay 200", 										//did nothing
    //"send 0x${device.deviceNetworkId} 1 1", "delay 1000",
    "st wattr 0x${device.deviceNetworkId} 1 0x20 0x00 0x23 {F0}", "delay 500"
    ]
    //getClusters()
    //def report = zigbee.parse('catchall: 0104 0020 01 01 0140 00 20C4 00 00 0000 04 01')
    //log.debug report
    //log.debug "Command is: ${report.command}"
    //log.debug((report.command) == 0x01)
}
def identify() {"st cmd 0x${device.deviceNetworkId} 1 3 0 {0A 00}"}
def getClusters() { "zdo active 0x${device.deviceNetworkId}" }

def refresh() {
	delayBetween([
					"st rattr 0x${device.deviceNetworkId} 1 0x01 0x0020",
    				"st rattr 0x${device.deviceNetworkId} 1 0x06 0x0000",
    				"st rattr 0x${device.deviceNetworkId} 1 0x20 0x0000",
    				"st rattr 0x${device.deviceNetworkId} 1 0x20 0x0001",
    				"st rattr 0x${device.deviceNetworkId} 1 0x20 0x0002",
    				"st rattr 0x${device.deviceNetworkId} 1 0x20 0x0003"
                    ],750)
}
/* Implement a queuing system. This might get overly complicated.. Maybe not needed....
// Sets the pendingCommand attribute to turnOn
def turnOn() {
	sendEvent(name: "pendingCommand", value: "on")
}

def turnOff() {
	sendEvent(name: "pendingCommand", value: "off")
}
*/

// Commands to device
def on() {
	'zcl on-off on'
}

def off() {
	'zcl on-off off'
}

private parseReportAttributeMessage(String description) {
	Map descMap = (description - "read attr - ").split(",").inject([:]) { map, param ->
		def nameAndValue = param.split(":")
		map += [(nameAndValue[0].trim()):nameAndValue[1].trim()]
	}
	//log.debug "Desc Map: $descMap"

	def results = []
    
	if (descMap.cluster == "0001" && descMap.attrId == "0020") {
		log.debug "Received battery level report"
		results = createEvent(getBatteryResult(Integer.parseInt(descMap.value, 16)))
	}

	return results
}

//Converts the battery level response into a percentage to display in ST
//and creates appropriate message for given level

private getBatteryResult(rawValue) {
	def linkText = getLinkText(device)

	def result = [name: 'battery']

	def volts = rawValue / 10
	def descriptionText
	if (volts > 3.5) {
		result.descriptionText = "${linkText} battery has too much power (${volts} volts)."
	}
	else {
		def minVolts = 2.1
		def maxVolts = 3.0
		def pct = (volts - minVolts) / (maxVolts - minVolts)
		result.value = Math.min(100, (int) pct * 100)
		result.descriptionText = "${linkText} battery was ${result.value}%"
	}

	return result
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
