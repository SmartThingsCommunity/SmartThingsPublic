metadata {
    definition(name: "Sonoff-Tasmota Discovery Device", namespace: "BrettSheleski", author: "Brett Sheleski") {
		capability "Actuator"

		command "discover"
    }

	 tiles(scale: 2) {
		

		standardTile("discover", "discover", width: 2, height: 2) {
			state "discover", label: 'Discover', action: "discover", icon: "st.secondary.refresh", backgroundColor: "#ffffff"
		}


		main "discover"
	}
}

def parse(String description) {
    log.debug "parse()"

	def message = parseLanMessage(description);
    
	if (message != null && message.body != null) {

        def jsonParser = new groovy.json.JsonSlurper();
        def settingsLine;
        def allSettings = [:];

        def lines = message.body.split('\n');

        lines.each {
            settingsLine = jsonParser.parseText(it);

            allSettings << settingsLine;
        }

        parent.discoverCompleted(allSettings);
	}
}

def discover(){
    log.debug "DISCOVER"
    sendCommand("Status" , "0")
}


private def sendCommand(String command, String payload){

    log.debug "sendCommand(${command}:${payload})"

	def ipAddress = parent.ipAddress;
	def port = 80;
    def hosthex = convertIPtoHex(ipAddress);
    def porthex = convertPortToHex(port);

	def deviceNetworkId = "$hosthex:$porthex"

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

	sendHubCommand(new physicalgraph.device.HubAction("""GET $path HTTP/1.1
HOST: ${ipAddress}:${port}

""", physicalgraph.device.Protocol.LAN, "${deviceNetworkId}"))
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