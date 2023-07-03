metadata {
    definition(name: "Sonoff-Tasmota RF Bridge Button", namespace: "BrettSheleski", author: "Brett Sheleski") {
		capability "momentary"

		attribute "keyNumber", "number"
		attribute "parentName", "string"
		
		command "initChild"
		command "clear"
		command "learn"
		command "sendDefault"
    }

	// UI tile definitions
    tiles(scale: 2) {
		multiAttributeTile(name:"switch", type: "generic", width: 6, height: 4, canChangeIcon: true){
			tileAttribute ("device.keyNumber", key: "PRIMARY_CONTROL") {
				attributeState "device.keyNumber", label: 'PUSH', action: "momentary.push", icon: "st.switches.switch.on", backgroundColor: "#ffffff"
			}
		}

		standardTile("clear", "clear", width: 2, height: 2) {
			state "clear", label: 'Clear', action: "clear", icon: "st.secondary.refresh", backgroundColor: "#ffffff"
		}

		standardTile("learn", "learn", width: 2, height: 2) {
			state "learn", label: 'Learn', action: "learn", icon: "st.secondary.refresh", backgroundColor: "#ffffff"
		}

		standardTile("sendDefault", "sendDefault", width: 2, height: 2) {
			state "sendDefault", label: 'Send Default', action: "sendDefault", backgroundColor: "#ffffff"
		}

		valueTile("parentName", "parentName", width: 3, height: 1) {
			state "parentName", label: '${currentValue}', backgroundColor: "#ffffff"
		}

		valueTile("keyNumber", "keyNumber", width: 3, height: 1) {
			state "keyNumber", label: 'Key ${currentValue}', backgroundColor: "#ffffff"
		}

		main "switch"
	}
}

def initChild(Map childSettings){
	sendEvent(name: "keyNumber", value: childSettings.keyNumber)
	sendEvent(name: "parentName", value: parent?.name)
}

def clear(){
	return sendKeyCommand("3");
}

def learn(){
	return sendKeyCommand("2");
}

def sendDefault(){
	return sendKeyCommand("1");
}

def push(){
	return sendKeyCommand(null);
}

def sendKeyCommand(String payload){
	def theKeyNumber = device.latestValue("keyNumber");

	def keyNumberString = "${theKeyNumber}";

	return sendCommand("RfKey${theKeyNumber}", payload);
}

private def sendCommand(String command, String payload){

    log.debug "sendCommand(${command}:${payload})"

	def ipAddress = parent.ipAddress;
	def port = 80;
    def hosthex = convertIPtoHex(ipAddress);
    def porthex = convertPortToHex(port);

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