metadata {
    definition(name: "Sonoff-Tasmota", namespace: "BrettSheleski", author: "Brett Sheleski") {
		capability "Switch"
		capability "Polling"
		capability "Refresh"
    }

	// UI tile definitions
    tiles(scale: 2) {
		multiAttributeTile(name:"switch", type: "lighting", width: 6, height: 4, canChangeIcon: true){
			tileAttribute ("device.switch", key: "PRIMARY_CONTROL") {
				attributeState "on", label: '${name}', action: "switch.off", icon: "st.switches.switch.on", backgroundColor: "#79b821"
				attributeState "off", label: '${name}', action: "switch.on", icon: "st.switches.switch.off", backgroundColor: "#ffffff"
			}
		}

		standardTile("refresh", "device.switch", width: 2, height: 2, inactiveLabel: false, decoration: "flat") {
			state "default", label:'', action:"refresh.refresh", icon:"st.secondary.refresh"
		}

		main "switch"
		details(["switch","refresh"])
	}

    preferences {
		input(name: "ipAddress", type: "string", title: "IP Address", description: "IP Address of Sonoff", displayDuringSetup: true, required: true)
		input(name: "port", type: "number", title: "Port", description: "Port", displayDuringSetup: true, required: true, defaultValue: 80)
		
		section("Sonoff Host") {
			
		}

		section("Authentication") {
			input(name: "username", type: "string", title: "Username", description: "Username", displayDuringSetup: false, required: false)
			input(name: "password", type: "password", title: "Password", description: "Password", displayDuringSetup: false, required: false)
		}
    }
}

def parse(String description) {
    log.debug "parse()"

	def STATUS_PREFIX = "STATUS = ";
	def RESULT_PREFIX = "RESULT = ";

	def message = parseLanMessage(description);
    
	if (message?.body?.startsWith(STATUS_PREFIX)) {
		def statusJson = message.body.substring(STATUS_PREFIX.length())

		parseStatus(statusJson);
	}
	else if (message?.body?.startsWith(RESULT_PREFIX)) {
		def resultJson = message.body.substring(RESULT_PREFIX.length())

		parseResult(resultJson);
	}
}

def parseStatus(String json){
	log.debug "status: $json"

	def status = new groovy.json.JsonSlurper().parseText(json);

	def isOn = status.Status.Power == 1;

	setSwitchState(isOn);
}

def parseResult(String json){
	log.debug "result: $json"

	def result = new groovy.json.JsonSlurper().parseText(json);

	def isOn = result.POWER == "ON";
	
	setSwitchState(isOn);
}

def setSwitchState(Boolean on){
	log.debug "The lights are " + (on ? "ON" : "OFF")

	sendEvent(name: "switch", value: on ? "on" : "off");
}


def toggle(){
	log.debug "TOGGLE"
    sendCommand("Power", "Toggle");
}

def on(){
	log.debug "ON"
    sendCommand("Power", "On");
}

def off(){
	log.debug "OFF"
    sendCommand("Power", "Off");
}

def poll(){
	log.debug "POLL"

	requestStatus()

}

def refresh(){
	log.debug "REFRESH"

	requestStatus();
}

def requestStatus(){
	log.debug "getStatus()"

	def result = sendCommand("Status", null);

	return result;
}

private def sendCommand(String command, String payload){

    log.debug "sendCommand(${command}:${payload})"

    def hosthex = convertIPtoHex(ipAddress);
    def porthex = convertPortToHex(port);

    device.deviceNetworkId = "$hosthex:$porthex";

	def path = "/cm"

	if (payload){
		path += "?cmnd=${command}%20${payload}"
	}
	else{
		path += "?cmnd=${command}"
	}

	if (username){
		path += "&user=${username}"

		if (password){
			path += "&password=${password}"
		}
	}

    def result = new physicalgraph.device.HubAction(
        method: "GET",
        path: path,
        headers: [
            HOST: "${ipAddress}:${port}"
        ]
    )

    log.debug result

    return result
}

private String convertIPtoHex(ipAddress) { 
    String hex = ipAddress.tokenize( '.' ).collect {  String.format( '%02x', it.toInteger() ) }.join()
    log.debug "IP address entered is $ipAddress and the converted hex code is $hex"
    return hex

}

private String convertPortToHex(port) {
	String hexport = port.toString().format('%04x', port.toInteger())
    log.debug hexport
    return hexport
}
