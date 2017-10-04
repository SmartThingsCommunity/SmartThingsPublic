metadata {
    definition(name: "Sonoff-Tasmota RF Bridge Button", namespace: "BrettSheleski", author: "Brett Sheleski") {
		capability "Switch"
    }

	// UI tile definitions
    tiles(scale: 2) {
		multiAttributeTile(name:"switch", type: "generic", width: 6, height: 4, canChangeIcon: true){
			tileAttribute ("device.switch", key: "PRIMARY_CONTROL") {
				attributeState "on", label: '${name}', action: "switch.off", icon: "st.switches.switch.on", backgroundColor: "#79b821"
				attributeState "off", label: '${name}', action: "switch.on", icon: "st.switches.switch.off", backgroundColor: "#ffffff"
			}
		}

		main "switch"
	}

    preferences {
		input(name: "ipAddress", type: "string", title: "IP Address", description: "IP Address of Sonoff", displayDuringSetup: true, required: true)
		input(name: "port", type: "number", title: "Port", description: "Port", displayDuringSetup: true, required: true, defaultValue: 80)

		input(name: "onKey", type: "number", title: "On Key", description: "On Key", displayDuringSetup: true, required: true, defaultValue: 1)
		input(name: "offKey", type: "number", title: "Off Key", description: "Off Key", displayDuringSetup: true, required: true, defaultValue: 2)
		
		section("Authentication") {
			input(name: "username", type: "string", title: "Username", description: "Username", displayDuringSetup: false, required: false)
			input(name: "password", type: "password", title: "Password", description: "Password", displayDuringSetup: false, required: false)
		}
    }
}


def setSwitchState(Boolean on){
	log.debug "The switch is " + (on ? "ON" : "OFF")

	sendEvent(name: "switch", value: on ? "on" : "off");
}

def on(){
	log.debug "ON"
	setSwitchState(true);
	return sendCommand("RfKey$onKey", null);
}

def off(){
	log.debug "OFF"
	setSwitchState(false);
	return sendCommand("RfKey$offKey", null);
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