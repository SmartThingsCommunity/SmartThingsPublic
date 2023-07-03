metadata {
	definition(name: "Tasmota-Fan", namespace: "BrettSheleski", author: "Brett Sheleski", ocfDeviceType: "oic.d.fan") {
		capability "Switch"
        capability "Switch Level"
        capability "Momentary"
        capability "Fan Speed"

        command "low"
        command "medium"
        command "high"
        command "raiseFanSpeed"
		command "lowerFanSpeed"
	}

	// UI tile definitions
	tiles(scale: 2) {
        multiAttributeTile(name:"fanSpeed", type:"generic", width:6, height:4) {
            tileAttribute("device.fanSpeed", key: "PRIMARY_CONTROL") {
                attributeState "0", label: "off", action: "momentary.push", backgroundColor: "#ffffff"
				attributeState "1", label: "low", action: "momentary.push", backgroundColor: "#00a0dc"
				attributeState "2", label: "medium", action: "momentary.push", backgroundColor: "#00a0dc"
				attributeState "3", label: "high", action: "momentary.push", backgroundColor: "#00a0dc"
            }
            tileAttribute("device.switch", key: "SECONDARY_CONTROL") {
                attributeState "on", label:'${name}', action:"momentary.push", icon:"st.thermostat.fan-on", backgroundColor:"#00A0DC", nextState:"turningOff"
                attributeState "off", label:'${name}', action:"momentary.push", icon:"st.thermostat.fan-off", backgroundColor:"#ffffff", nextState:"turningOn"
                attributeState "turningOn", label:'…', action:"momentary.push", backgroundColor:"#79b821", nextState:"turningOff"
                attributeState "turningOff", label:'…', action:"momentary.push", backgroundColor:"#ffffff", nextState:"turningOn"
            }
            tileAttribute("device.fanSpeed", key: "VALUE_CONTROL") {
                attributeState "VALUE_UP", action: "raiseFanSpeed"
                attributeState "VALUE_DOWN", action: "lowerFanSpeed"
            }
             tileAttribute("device.level", key: "SLIDER_CONTROL") {
                attributeState "level", action:"switch level.setLevel", defaultState: true
            }
        }

		main "fanSpeed"
		details(["fanSpeed"])
	}
    preferences {
		section("Main") {
     		input(name: "onSpeed", type: "number", title: "On Speed", description: "Speed the fan should be set to when turned on", displayDuringSetup: true, required: false, defaultValue: 2)
		}
	}
}

def initializeChild(Map options){
    // when the 'Master' device creates child devices, this method is called passing configuration
}

def updateStatus(status){
    // when the 'Master' device refreshes it passes the retrieved status to all children, thus calling this method
    // update the status of this device accordingly
}

def push() {
    def speed = device.latestValue("fanSpeed");

    if (speed == 0){    	
        on();
    }
    else{
        off();
    }
}

def on() {
    log.debug "ON"
    def speed = device.latestValue("fanSpeed");

    if (speed == 0){
        setFanSpeed(device.latestValue("onSpeed") as Integer);
    }
}

def off() {
    log.debug "OFF"
    setFanSpeed(0);
}


def low(){
    setFanSpeed(1);
}

def medium(){
    setFanSpeed(2);
}

def high(){
    setFanSpeed(3);
}

def setLevel(value) {
	// if 0 -> off
    
    // if < 33 -> low
    
    // if < 66 -> medium
    
    // else high
    
    if (value == 0){
    	off();
    }
    else if (value <= 33){
    	low();
    }
    else if (value <= 66){
    	medium();
    }
    else{
    	high();
    }
}

def raiseFanSpeed() {
	setFanSpeed(Math.min((device.currentValue("fanSpeed") as Integer) + 1, 3))
}

def lowerFanSpeed() {
	setFanSpeed(Math.max((device.currentValue("fanSpeed") as Integer) - 1, 0))
}

def setFanSpeed(int speed){

	if (speed < 0)
    {
    	speed = 0;
    }
    else if (speed > 3)
    {
    	speed = 3;
    }

    log.debug "Setting Fan Speed to: $speed"

	def commandName = "FanSpeed";
	def payload = "$speed";

	log.debug "COMMAND: $commandName ($payload)"

	def command = parent.createCommand(commandName, payload, "setFanSpeedCallback");;

    sendHubCommand(command);
}

def setFanSpeedCallback(physicalgraph.device.HubResponse response){
    def speed = response.json.FanSpeed;

    if (speed > 0){
        sendEvent(name : "onSpeed", value: speed);
    }

    sendEvent(name: "fanSpeed", value: speed);
    sendEvent(name: "switch", value: speed > 0 ? "on" : "off");
    
    def switchLevel = 0;
    
    if (speed == 0){
    	switchLevel = 0;
    }
    else if (speed == 1){
    	switchLevel = 33;
    }
    else if (speed == 2){
    	switchLevel = 66;
    }
    else{
    	switchLevel = 100;
    }
    
    sendEvent(name: "level", value: switchLevel);
    //sendEvent(name: "level", value: "$switchLevel", isStateChange: true, displayed: false)
	//sendEvent(name: "level", value: device.currentValue("level"), isStateChange: true, displayed: false)
}