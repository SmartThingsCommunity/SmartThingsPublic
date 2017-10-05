metadata {
    definition(name: "Sonoff-Tasmota RF Bridge Button", namespace: "BrettSheleski", author: "Brett Sheleski") {
		capability "momentary"

		attribute "keyNumber", "number"
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
		input(name: "keyNumber", type: "number", title: "Key Number", description: "Key Number", displayDuringSetup: true, required: true)
    }

}

def push(){
	log.debug "OFF"

	def keyNumberString = device.latestValue("keyNumber").ToString();

	return sendCommand("RfKey", keyNumberString);
}

private def sendCommand(String command, String payload){

    log.debug "sendCommand(${command}:${payload})"

	def ipAddress = parent.ipAddress;
    def hosthex = convertIPtoHex(ipAddress);
    def porthex = convertPortToHex(port);

    //device.deviceNetworkId = "$hosthex:$porthex";

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