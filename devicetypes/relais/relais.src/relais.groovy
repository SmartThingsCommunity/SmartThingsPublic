metadata {

	definition (name: "Relais", author: "Erik Tellier", namespace:"Relais") {
 	    capability "Switch"
        command "on"
        command "off" 
	}
	
    preferences {
			input("DeviceIP", "string", title:"Addresse IP Relais (ESP-01)", description: "Exemple: 192.168.1.101", required: true, displayDuringSetup: true)
	}
            
	tiles(scale: 2) {
        standardTile("Trigger", "device.triggerswitch", width: 6, height: 6, canChangeIcon: true, canChangeBackground: true) {
			state "triggeroff", label:'OFF' , action: "on", icon: "st.illuminance.illuminance.dark", backgroundColor:"#ff0000", nextState: "envoi"
			state "triggeron", label: 'ON', action: "off", icon: "st.illuminance.illuminance.bright", backgroundColor: "#00cc33", nextState: "envoi"
			state "envoi", label: 'envoi', action: "", icon: "st.illuminance.illuminance.light", backgroundColor: "#ff6600"
        }
        
		main "Trigger"
		details(["Trigger"]) 
	}
}

def on() {
	sendEvent(name: "on", value: "on", isStateChange: true)
    sendEvent(name: "triggerswitch", value: "triggeron", isStateChange: true)
    state.Relais = "on";
	runCmdir("/command?Relais=on")
}

def off() {
	sendEvent(name: "off", value: "off", isStateChange: true)
    sendEvent(name: "triggerswitch", value: "triggeroff", isStateChange: true)
    state.Relais = "off";
	runCmdir("/command?Relais=off")
}

def runCmdir(String varCommand) {
	def host = DeviceIP
	device.deviceNetworkId = "Relais" // Ajout d'un nom unique 
	log.debug "Device ID = $device.deviceNetworkId"
	def headers = [:] 
	headers.put("HOST", "$host:81")
	headers.put("Content-Type", "application/x-www-form-urlencoded")
	log.debug "$headers"

	try {
		def hubAction = new physicalgraph.device.HubAction(
			method: "GET",
			path: varCommand,
			body: '',
			headers: headers
			)
		hubAction.options = [outputMsgToS3:false]
        log.debug "$hubAction"
		hubAction
	}
	catch (Exception e) {
		log.debug "Hit Exception $e on $hubAction"
	}
}