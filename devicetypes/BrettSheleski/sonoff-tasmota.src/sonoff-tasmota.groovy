metadata {
    definition(name: "Sonoff-Tasmota", namespace: "BrettSheleski", author: "Brett Sheleski") {
		capability "Switch"

        attribute "ipAddress", "string"
    }

	// UI tile definitions
    tiles(scale: 2) {
		multiAttributeTile(name:"switch", type: "lighting", width: 6, height: 4, canChangeIcon: true){
			tileAttribute ("device.switch", key: "PRIMARY_CONTROL") {
				attributeState "on", label: '${name}', action: "switch.off", icon: "st.switches.switch.on", backgroundColor: "#79b821"
				attributeState "off", label: '${name}', action: "switch.on", icon: "st.switches.switch.off", backgroundColor: "#ffffff"
			}
		}

		standardTile("indicator", "device.indicatorStatus", width: 2, height: 2, inactiveLabel: false, decoration: "flat") {
			state "when off", action:"indicator.indicatorWhenOn", icon:"st.indicators.lit-when-off"
			state "when on", action:"indicator.indicatorNever", icon:"st.indicators.lit-when-on"
			state "never", action:"indicator.indicatorWhenOff", icon:"st.indicators.never-lit"
		}
		standardTile("refresh", "device.switch", width: 2, height: 2, inactiveLabel: false, decoration: "flat") {
			state "default", label:'', action:"refresh.refresh", icon:"st.secondary.refresh"
		}

		main "switch"
		details(["switch","refresh","indicator"])
	}

     preferences {
        input "rokuIpAddress", "string", title: "IP Address", description: "IP Address of Roku", displayDuringSetup: true, required: true
        input "rokuPort", "string", title: "Port", description: "Port", displayDuringSetup: true, defaultValue: "8060", required: true
    }
}

def parse(String description) {
    log.debug "parse()"
}

def toggle(){
    sendCommand("Power", "Toggle");
}

def on(){
    sendCommand("Power", "On");
}

def off(){
    sendCommand("Power", "Off");
}

private def sendCommand(String command, String payload){

    log.debug "remoteKeyPress(${key})"

    def hosthex = convertIPtoHex(rokuIpAddress)
    def porthex = convertPortToHex(rokuPort)

    device.deviceNetworkId = "$hosthex:$porthex" 

    def result = new physicalgraph.device.HubAction(
        method: "POST",
        path: "/keypress/" + key,
        headers: [
            HOST: "${rokuIpAddress}:${rokuPort}"
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
	String hexport = port.toString().format( '%04x', port.toInteger() )
    log.debug hexport
    return hexport
}

def on(){
    keyPress_Play()
}

def off(){
    keyPress_Play()
}