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
    def on = response.json.POWER == "ON";

    setSwitchState(on);
}

def updateStatus(status){

	def powerMask = 0b0001;

	powerMask = powerMask << (state.powerChannel - 1);

	def on = (powerMask & status.Status.Power);

	setSwitchState(on);
}

def setSwitchState(on){
	log.debug "Setting switch to ${on ? 'ON' : 'OFF'}"

	sendEvent(name: "switch", value: on ? "on" : "off")
}