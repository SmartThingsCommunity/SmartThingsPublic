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
			state "off", label: '', action: "key1", icon: "st.Lighting.light11", backgroundColor: "#ffffff"
		}

		valueTile("learnButton1", "device.switch", width: 2, height: 1) {
			state "off", label: 'Learn', action: "learn1", backgroundColor: "#ffffff"
		}

		valueTile("clearButton1", "device.switch", width: 2, height: 1) {
			state "off", label: 'Clear', action: "clear1", backgroundColor: "#ffffff"
		}


		valueTile("label2", "device.integerFloat", width: 2, height: 2) {
			state "val", label:'Key 2', defaultState: true
		}

		standardTile("button2", "device.switch", width: 2, height: 2) {
			state "off", label: '', action: "key2", icon: "st.Lighting.light11", backgroundColor: "#ffffff"
		}

		valueTile("learnButton2", "device.switch", width: 2, height: 1) {
			state "off", label: 'Learn', action: "learn2", backgroundColor: "#ffffff"
		}

		valueTile("clearButton2", "device.switch", width: 2, height: 1) {
			state "off", label: 'Clear', action: "clear2", backgroundColor: "#ffffff"
		}



		valueTile("label3", "device.integerFloat", width: 2, height: 2) {
			state "val", label:'Key 3', defaultState: true
		}

		standardTile("button3", "device.switch", width: 2, height: 2) {
			state "off", label: '', action: "key3", icon: "st.Lighting.light11", backgroundColor: "#ffffff"
		}

		valueTile("learnButton3", "device.switch", width: 2, height: 1) {
			state "off", label: 'Learn', action: "learn3", backgroundColor: "#ffffff"
		}

		valueTile("clearButton3", "device.switch", width: 2, height: 1) {
			state "off", label: 'Clear', action: "clear3", backgroundColor: "#ffffff"
		}

		valueTile("label4", "device.integerFloat", width: 2, height: 2) {
			state "val", label:'Key 4', defaultState: true
		}

		standardTile("button4", "device.switch", width: 2, height: 2) {
			state "off", label: '', action: "key4", icon: "st.Lighting.light11", backgroundColor: "#ffffff"
		}

		valueTile("learnButton4", "device.switch", width: 2, height: 1) {
			state "off", label: 'Learn', action: "learn4", backgroundColor: "#ffffff"
		}

		valueTile("clearButton4", "device.switch", width: 2, height: 1) {
			state "off", label: 'Clear', action: "clear4", backgroundColor: "#ffffff"
		}

		valueTile("label5", "device.integerFloat", width: 2, height: 2) {
			state "val", label:'Key 5', defaultState: true
		}

		standardTile("button5", "device.switch", width: 2, height: 2) {
			state "off", label: '', action: "key5", icon: "st.Lighting.light11", backgroundColor: "#ffffff"
		}

		valueTile("learnButton5", "device.switch", width: 2, height: 1) {
			state "off", label: 'Learn', action: "learn5", backgroundColor: "#ffffff"
		}

		valueTile("clearButton5", "device.switch", width: 2, height: 1) {
			state "off", label: 'Clear', action: "clear5", backgroundColor: "#ffffff"
		}

		valueTile("label6", "device.integerFloat", width: 2, height: 2) {
			state "val", label:'Key 6', defaultState: true
		}

		standardTile("button6", "device.switch", width: 2, height: 2) {
			state "off", label: '', action: "key6", icon: "st.Lighting.light11", backgroundColor: "#ffffff"
		}

		valueTile("learnButton6", "device.switch", width: 2, height: 1) {
			state "off", label: 'Learn', action: "learn6", backgroundColor: "#ffffff"
		}

		valueTile("clearButton6", "device.switch", width: 2, height: 1) {
			state "off", label: 'Clear', action: "clear6", backgroundColor: "#ffffff"
		}

		valueTile("label7", "device.integerFloat", width: 2, height: 2) {
			state "val", label:'Key 7', defaultState: true
		}

		standardTile("button7", "device.switch", width: 2, height: 2) {
			state "off", label: '', action: "key7", icon: "st.Lighting.light11", backgroundColor: "#ffffff"
		}

		valueTile("learnButton7", "device.switch", width: 2, height: 1) {
			state "off", label: 'Learn', action: "learn7", backgroundColor: "#ffffff"
		}

		valueTile("clearButton7", "device.switch", width: 2, height: 1) {
			state "off", label: 'Clear', action: "clear7", backgroundColor: "#ffffff"
		}

		valueTile("label8", "device.integerFloat", width: 2, height: 2) {
			state "val", label:'Key 8', defaultState: true
		}

		standardTile("button8", "device.switch", width: 2, height: 2) {
			state "off", label: '', action: "key8", icon: "st.Lighting.light11", backgroundColor: "#ffffff"
		}

		valueTile("learnButton8", "device.switch", width: 2, height: 1) {
			state "off", label: 'Learn', action: "learn8", backgroundColor: "#ffffff"
		}

		valueTile("clearButton8", "device.switch", width: 2, height: 1) {
			state "off", label: 'Clear', action: "clear8", backgroundColor: "#ffffff"
		}

		valueTile("label9", "device.integerFloat", width: 2, height: 2) {
			state "val", label:'Key 9', defaultState: true
		}

		standardTile("button9", "device.switch", width: 2, height: 2) {
			state "off", label: '', action: "key9", icon: "st.Lighting.light11", backgroundColor: "#ffffff"
		}

		valueTile("learnButton9", "device.switch", width: 2, height: 1) {
			state "off", label: 'Learn', action: "learn9", backgroundColor: "#ffffff"
		}

		valueTile("clearButton9", "device.switch", width: 2, height: 1) {
			state "off", label: 'Clear', action: "clear9", backgroundColor: "#ffffff"
		}

		valueTile("label10", "device.integerFloat", width: 2, height: 2) {
			state "val", label:'Key 10', defaultState: true
		}

		standardTile("button10", "device.switch", width: 2, height: 2) {
			state "off", label: '', action: "key10", icon: "st.Lighting.light11", backgroundColor: "#ffffff"
		}

		valueTile("learnButton10", "device.switch", width: 2, height: 1) {
			state "off", label: 'Learn', action: "learn10", backgroundColor: "#ffffff"
		}

		valueTile("clearButton10", "device.switch", width: 2, height: 1) {
			state "off", label: 'Clear', action: "clear10", backgroundColor: "#ffffff"
		}

		valueTile("label11", "device.integerFloat", width: 2, height: 2) {
			state "val", label:'Key 11', defaultState: true
		}

		standardTile("button11", "device.switch", width: 2, height: 2) {
			state "off", label: '', action: "key11", icon: "st.Lighting.light11", backgroundColor: "#ffffff"
		}

		valueTile("learnButton11", "device.switch", width: 2, height: 1) {
			state "off", label: 'Learn', action: "learn11", backgroundColor: "#ffffff"
		}

		valueTile("clearButton11", "device.switch", width: 2, height: 1) {
			state "off", label: 'Clear', action: "clear11", backgroundColor: "#ffffff"
		}

		valueTile("label12", "device.integerFloat", width: 2, height: 2) {
			state "val", label:'Key 12', defaultState: true
		}

		standardTile("button12", "device.switch", width: 2, height: 2) {
			state "off", label: '', action: "key12", icon: "st.Lighting.light11", backgroundColor: "#ffffff"
		}

		valueTile("learnButton12", "device.switch", width: 2, height: 1) {
			state "off", label: 'Learn', action: "learn12", backgroundColor: "#ffffff"
		}

		valueTile("clearButton12", "device.switch", width: 2, height: 1) {
			state "off", label: 'Clear', action: "clear12", backgroundColor: "#ffffff"
		}

		valueTile("label13", "device.integerFloat", width: 2, height: 2) {
			state "val", label:'Key 13', defaultState: true
		}

		standardTile("button13", "device.switch", width: 2, height: 2) {
			state "off", label: '', action: "key13", icon: "st.Lighting.light11", backgroundColor: "#ffffff"
		}

		valueTile("learnButton13", "device.switch", width: 2, height: 1) {
			state "off", label: 'Learn', action: "learn13", backgroundColor: "#ffffff"
		}

		valueTile("clearButton13", "device.switch", width: 2, height: 1) {
			state "off", label: 'Clear', action: "clear13", backgroundColor: "#ffffff"
		}

		valueTile("label14", "device.integerFloat", width: 2, height: 2) {
			state "val", label:'Key 14', defaultState: true
		}

		standardTile("button14", "device.switch", width: 2, height: 2) {
			state "off", label: '', action: "key14", icon: "st.Lighting.light11", backgroundColor: "#ffffff"
		}

		valueTile("learnButton14", "device.switch", width: 2, height: 1) {
			state "off", label: 'Learn', action: "learn14", backgroundColor: "#ffffff"
		}

		valueTile("clearButton14", "device.switch", width: 2, height: 1) {
			state "off", label: 'Clear', action: "clear14", backgroundColor: "#ffffff"
		}

		valueTile("label15", "device.integerFloat", width: 2, height: 2) {
			state "val", label:'Key 15', defaultState: true
		}

		standardTile("button15", "device.switch", width: 2, height: 2) {
			state "off", label: '', action: "key15", icon: "st.Lighting.light11", backgroundColor: "#ffffff"
		}

		valueTile("learnButton15", "device.switch", width: 2, height: 1) {
			state "off", label: 'Learn', action: "learn15", backgroundColor: "#ffffff"
		}

		valueTile("clearButton15", "device.switch", width: 2, height: 1) {
			state "off", label: 'Clear', action: "clear15", backgroundColor: "#ffffff"
		}

		valueTile("label16", "device.integerFloat", width: 2, height: 2) {
			state "val", label:'Key 16', defaultState: true
		}

		standardTile("button16", "device.switch", width: 2, height: 2) {
			state "off", label: '', action: "key16", icon: "st.Lighting.light11", backgroundColor: "#ffffff"
		}

		valueTile("learnButton16", "device.switch", width: 2, height: 1) {
			state "off", label: 'Learn', action: "learn16", backgroundColor: "#ffffff"
		}

		valueTile("clearButton16", "device.switch", width: 2, height: 1) {
			state "off", label: 'Clear', action: "clear16", backgroundColor: "#ffffff"
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