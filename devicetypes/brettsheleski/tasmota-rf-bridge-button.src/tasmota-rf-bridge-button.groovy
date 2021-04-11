metadata {
	definition(name: "Tasmota-RF-Bridge Button", namespace: "BrettSheleski", author: "Brett Sheleski", ocfDeviceType: "oic.d.smartplug") {
		// define appropriate capabilities, commands, etc. here
		capability "Momentary"

		attribute "keyNumber", "number"
		attribute "parentName", "string"
		
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

def initializeChild(Map options){
    // when the 'Master' device creates child devices, this method is called passing configuration

	sendEvent(name: "keyNumber", value: options?.keyNumber)
	sendEvent(name: "parentName", value: parent?.name)
}

def updateStatus(status){
    // when the 'Master' device refreshes it passes the retrieved status to all children, thus calling this method
    // update the status of this device accordingly

	// nothing to do here, carry on...
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

	def command = parent.createCommand("RfKey${theKeyNumber}", payload, null);;

    sendHubCommand(command);
}