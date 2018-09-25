metadata {
	definition(name: "Tasmota-Fan", namespace: "BrettSheleski", author: "Brett Sheleski", ocfDeviceType: "oic.d.smartplug") {
		capability "Switch" // on(), off() commands
        capability "Momentary" // push() command

        command "setFanSpeed0"
        command "setFanSpeed1"
        command "setFanSpeed2"
        command "setFanSpeed3"
        command "raiseFanSpeed"
		command "lowerFanSpeed"

        // According to 
        //    https://docs.smartthings.com/en/latest/capabilities-reference.html#id33 
        //    and 
        //    https://smartthings.developer.samsung.com/develop/api-ref/capabilities.html#Fan-Speed
        // this capability is currently only proposed.
        // Therefore I am not sure if this capability is even supported (yet)
        capability "Fan Speed" 

        // if the above capability is already working, the following attribute definition may not be necessary
        //attribute "fanSpeed", "number"
	}

	// UI tile definitions
	tiles(scale: 2) {
		multiAttributeTile(name: "fanSpeed", type: "generic", width: 6, height: 4, canChangeIcon: true) {
			tileAttribute("device.fanSpeed", key: "PRIMARY_CONTROL") {
				attributeState "0", label: "off", action: "switch.on", icon: "st.thermostat.fan-off", backgroundColor: "#ffffff"
				attributeState "1", label: "low", action: "switch.off", icon: "st.thermostat.fan-on", backgroundColor: "#00a0dc"
				attributeState "2", label: "medium", action: "switch.off", icon: "st.thermostat.fan-on", backgroundColor: "#00a0dc"
				attributeState "3", label: "high", action: "switch.off", icon: "st.thermostat.fan-on", backgroundColor: "#00a0dc"
			}
			tileAttribute("device.fanSpeed", key: "VALUE_CONTROL") {
				attributeState "VALUE_UP", action: "raiseFanSpeed"
				attributeState "VALUE_DOWN", action: "lowerFanSpeed"
			}
		}

		main "switch"
		details(["switch", "fanSpeed"])
	}
}

def initializeChild(Map options){
    // when the 'Master' device creates child devices, this method is called passing configuration
}

def updateStatus(status){
    // when the 'Master' device refreshes it passes the retrieved status to all children, thus calling this method
    // update the status of this device accordingly
}

def push(){
    // if on, then off()
    // else setFanSpeed(lastFanSpeed)

    def speed = device.latestValue("fanSpeed");


    if (speed == 0){
    	
        on();
    }
    else{
    	
        off();
    }
}

def on(){
log.debug "turning on"
    def speed = device.latestValue("fanSpeed");

    if (speed == 0){
        setFanSpeed(state.lastFanSpeed);
    }
}

def off(){
log.debug "turning off"
    fanSpeed(0);
}

def setFanSpeed0(){
    setFanSpeed(0);
}

def setFanSpeed1(){
    setFanSpeed(1);
}

def setFanSpeed2(){
    setFanSpeed(2);
}

def setFanSpeed3(){
    setFanSpeed(3);
}

def raiseFanSpeed() {
	setFanSpeed(Math.min((device.currentValue("fanSpeed") as Integer) + 1, 3))
}

def lowerFanSpeed() {
	setFanSpeed(Math.max((device.currentValue("fanSpeed") as Integer) - 1, 0))
}

def setFanSpeed(int speed){
    log.debug "Setting Fan Speed to: $speed"

	def commandName = "FanSpeed";
	def payload = speed;

	log.debug "COMMAND: $commandName ($payload)"

	def command = parent.createCommand(commandName, payload, "setFanSpeedCallback");;

    sendHubCommand(command);
}

def setFanSpeedCallback(physicalgraph.device.HubResponse response){
    def speed = response.json.FanSpeed;

    if (speed > 0){
        state.lastFanSpeed = speed;
    }

    sendEvent(name: "fanSpeed", value: speed)
    sendEvent(name: "switch", value: speed > 0 ? "on" : "off")
}