metadata {
    definition(name: "Sonoff-Tasmota RF Bridge", namespace: "BrettSheleski", author: "Brett Sheleski") {
		capability "Switch"

		command "key1"
		command "key2"
		command "key3"
		command "key4"
		command "key5"
		command "key6"
		command "key7"
		command "key8"
		command "key9"
		command "key10"
		command "key11"
		command "key12"
		command "key13"
		command "key14"
		command "key15"
		command "key16"

		command "learn1"
		command "learn2"
		command "learn3"
		command "learn4"
		command "learn5"
		command "learn6"
		command "learn7"
		command "learn8"
		command "learn9"
		command "learn10"
		command "learn11"
		command "learn12"
		command "learn13"
		command "learn14"
		command "learn15"
		command "learn16"

		command "clear1"
		command "clear2"
		command "clear3"
		command "clear4"
		command "clear5"
		command "clear6"
		command "clear7"
		command "clear8"
		command "clear9"
		command "clear10"
		command "clear11"
		command "clear12"
		command "clear13"
		command "clear14"
		command "clear15"
		command "clear16"
    }

	// UI tile definitions
    tiles(scale: 2) {
		/*
		multiAttributeTile(name:"switch", type: "generic", width: 6, height: 4, canChangeIcon: true){
			tileAttribute ("device.switch", key: "PRIMARY_CONTROL") {
				attributeState "on", label: '${name}', action: "switch.off", icon: "st.switches.switch.on", backgroundColor: "#79b821"
				attributeState "off", label: '${name}', action: "switch.on", icon: "st.switches.switch.off", backgroundColor: "#ffffff"
			}
		}
		*/

		valueTile("label1", "device.integerFloat", width: 2, height: 2) {
			state "val", label:'Key 1', defaultState: true
		}

		standardTile("button1", "device.switch", width: 2, height: 2) {
			state "off", label: '', action: "key1", backgroundColor: "#ffffff"
		}

		standardTile("learnButton1", "device.switch", width: 2, height: 1) {
			state "off", label: 'Learn', action: "learn1", backgroundColor: "#ffffff"
		}

		standardTile("clearButton1", "device.switch", width: 2, height: 1) {
			state "off", label: 'Clear', action: "clear1", backgroundColor: "#ffffff"
		}



		valueTile("label2", "device.integerFloat", width: 2, height: 2) {
			state "val", label:'Key 2', defaultState: true
		}

		standardTile("button2", "device.switch", width: 2, height: 2) {
			state "off", label: '', action: "key2", backgroundColor: "#ffffff"
		}

		standardTile("learnButton2", "device.switch", width: 2, height: 1) {
			state "off", label: 'Learn', action: "learn2", backgroundColor: "#ffffff"
		}

		standardTile("clearButton2", "device.switch", width: 2, height: 1) {
			state "off", label: 'Clear', action: "clear2", backgroundColor: "#ffffff"
		}



		standardTile("button3", "device.switch", width: 3, height: 3) {
			state "off", label: '3', action: "key3", backgroundColor: "#ffffff"
		}

		standardTile("button4", "device.switch", width: 3, height: 3) {
			state "off", label: '4', action: "key4", backgroundColor: "#ffffff"
		}


		standardTile("button5", "device.switch", width: 3, height: 3) {
			state "off", label: '5', action: "key5", backgroundColor: "#ffffff"
		}

		standardTile("button6", "device.switch", width: 3, height: 3) {
			state "off", label: '6', action: "key6", backgroundColor: "#ffffff"
		}

		standardTile("button7", "device.switch", width: 3, height: 3) {
			state "off", label: '7', action: "key7", backgroundColor: "#ffffff"
		}

		standardTile("button8", "device.switch", width: 3, height: 3) {
			state "off", label: '8', action: "key8", backgroundColor: "#ffffff"
		}



		standardTile("button9", "device.switch", width: 3, height: 3) {
			state "off", label: '9', action: "key9", backgroundColor: "#ffffff"
		}

		standardTile("button10", "device.switch", width: 3, height: 3) {
			state "off", label: '10', action: "key10", backgroundColor: "#ffffff"
		}

		standardTile("button11", "device.switch", width: 3, height: 3) {
			state "off", label: '11', action: "key11", backgroundColor: "#ffffff"
		}

		standardTile("button12", "device.switch", width: 3, height: 3) {
			state "off", label: '12', action: "key12", backgroundColor: "#ffffff"
		}


		standardTile("button13", "device.switch", width: 3, height: 3) {
			state "off", label: '13', action: "key13", backgroundColor: "#ffffff"
		}

		standardTile("button14", "device.switch", width: 3, height: 3) {
			state "off", label: '14', action: "key14", backgroundColor: "#ffffff"
		}

		standardTile("button15", "device.switch", width: 3, height: 3) {
			state "off", label: '15', action: "key15", backgroundColor: "#ffffff"
		}

		standardTile("button16", "device.switch", width: 3, height: 3) {
			state "off", label: '16', action: "key16", backgroundColor: "#ffffff"
		}

		//main "switch"
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

def key1(){return sendKey(1);}
def key2(){return sendKey(2);}
def key3(){return sendKey(3);}
def key4(){return sendKey(4);}
def key5(){return sendKey(5);}
def key6(){return sendKey(6);}
def key7(){return sendKey(7);}
def key8(){return sendKey(8);}
def key9(){return sendKey(9);}
def key10(){return sendKey(10);}
def key11(){return sendKey(11);}
def key12(){return sendKey(12);}
def key13(){return sendKey(13);}
def key14(){return sendKey(14);}
def key15(){return sendKey(15);}
def key16(){return sendKey(16);}

def learn1(){return learnKey(1);}
def learn2(){return learnKey(2);}
def learn3(){return learnKey(3);}
def learn4(){return learnKey(4);}
def learn5(){return learnKey(5);}
def learn6(){return learnKey(6);}
def learn7(){return learnKey(7);}
def learn8(){return learnKey(8);}
def learn9(){return learnKey(9);}
def learn10(){return learnKey(10);}
def learn11(){return learnKey(11);}
def learn12(){return learnKey(12);}
def learn13(){return learnKey(13);}
def learn14(){return learnKey(14);}
def learn15(){return learnKey(15);}
def learn16(){return learnKey(16);}

def clear1(){return clearKey(1);}
def clear2(){return clearKey(2);}
def clear3(){return clearKey(3);}
def clear4(){return clearKey(4);}
def clear5(){return clearKey(5);}
def clear6(){return clearKey(6);}
def clear7(){return clearKey(7);}
def clear8(){return clearKey(8);}
def clear9(){return clearKey(9);}
def clear10(){return clearKey(10);}
def clear11(){return clearKey(11);}
def clear12(){return clearKey(12);}
def clear13(){return clearKey(13);}
def clear14(){return clearKey(14);}
def clear15(){return clearKey(15);}
def clear16(){return clearKey(16);}

def clearKey(int key){
	return sendCommand("RfKey$key", "3");
}

def sendKey(int key){
	return sendCommand("RfKey$key", null);
}

def learnKey(int key){
	return sendCommand("RfKey$key", "2");
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