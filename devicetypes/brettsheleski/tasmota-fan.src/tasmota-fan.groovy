metadata {
	definition(name: "Tasmota-Fan", namespace: "BrettSheleski", author: "Brett Sheleski", ocfDeviceType: "oic.d.smartplug") {
		capability "Switch" // on(), off() commands
        capability "Momentary" // push() command

        command "setFanSpeed0"
        command "setFanSpeed1"
        command "setFanSpeed2"
        command "setFanSpeed3"

        // According to 
        //    https://docs.smartthings.com/en/latest/capabilities-reference.html#id33 
        //    and 
        //    https://smartthings.developer.samsung.com/develop/api-ref/capabilities.html#Fan-Speed
        // this capability is currently only proposed.
        // Therefore I am not sure if this capability is even supported (yet)
        capability "FanSpeed" 

        // if the above capability is already working, the following attribute definition may not be necessary
        attribute "fanSpeed", "number"
	}

	// UI tile definitions
	tiles(scale: 2) {
		multiAttributeTile(name:"switch", type: "generic", width: 6, height: 4, canChangeIcon: true){
			tileAttribute ("device.switch", key: "PRIMARY_CONTROL") {
				attributeState "on", label: '${name}', action: "momentary.push", icon: "http://cdn.device-icons.smartthings.com/Lighting/light24-icn@2x.png", backgroundColor: "#79b821"
				attributeState "off", label: '${name}', action: "momentary.push", icon: "http://cdn.device-icons.smartthings.com/Lighting/light24-icn@2x.png", backgroundColor: "#ffffff"
			}
		}

        valueTile("fanSpeed", "fanSpeed", width: 3, height: 1) {
			state "fanSpeed", label: 'Fan Speed: ${currentValue}', backgroundColor: "#ffffff"
		}

        // TODO: Add tiles to set fan speed to 0 (off), 1, 2, 3

		main "name"
		details(["name", "fanSpeed"])
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

    def speed = device.latestValue("fanSpeed");

    if (speed == 0){
        setFanSpeed(state.lastFanSpeed);
    }
}

def off(){
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