metadata {
	definition(name: "Tasmota-Power", namespace: "BrettSheleski", author: "Brett Sheleski", ocfDeviceType: "oic.d.smartplug") {
		capability "Momentary"
		capability "Switch"

	}

	// UI tile definitions
	tiles(scale: 2) {
		multiAttributeTile(name:"switch", type: "generic", width: 6, height: 4, canChangeIcon: true){
			tileAttribute ("device.switch", key: "PRIMARY_CONTROL") {
				attributeState "on", label: '${name}', action: "momentary.push", icon: "st.switches.switch.on", backgroundColor: "#79b821"
				attributeState "off", label: '${name}', action: "momentary.push", icon: "st.switches.switch.off", backgroundColor: "#ffffff"
			}
		}

		main "switch"
		details(["switch"])
	}
}

def initializeChild(Map options){
    state.powerChannel = options.powerChannel;
}

def on(){
    setPower("on")
}

def off(){
    setPower("off")
}

def push(){
    setPower("toggle")
}

def setPower(power){
	log.debug "Setting power to: $power"

	def command = parent.createCommand("Power${state.powerChannel}", power, "setPowerCallback");;

    sendHubCommand(command);
}

def setPowerCallback(physicalgraph.device.HubResponse response){
	log.debug "Finished Setting power, JSON: ${response.json}"

	def powerChannel = state.powerChannel;

    def on = response.json."POWER${powerChannel}" == "ON";

	if (powerChannel == 1){
		// if this is channel 1, there may not be any other channels.
		// In this case the property of the JSON response is just POWER (not POWER1)
		on = on || response.json.POWER == "ON";
	}

    setSwitchState(on);
}

def updateStatus(status){

	// Device power status(es) are reported back by the Status.Power property
	// The Status.Power property contains the on/off state of all channels (in case of a Sonoff 4CH or Sonoff Dual)
	// This is binary-encoded where each bit represents the on/off state of a particular channel
	// EG: 7 in binary is 0111.  In this case channels 1, 2, and 3 are ON and channel 4 is OFF

	def powerMask = 0b0001;

	powerMask = powerMask << (state.powerChannel - 1); // shift the bits over 

	def on = (powerMask & status.Status.Power);

	setSwitchState(on);
}

def setSwitchState(on){
	log.debug "Setting switch to ${on ? 'ON' : 'OFF'}"

	sendEvent(name: "switch", value: on ? "on" : "off")
}