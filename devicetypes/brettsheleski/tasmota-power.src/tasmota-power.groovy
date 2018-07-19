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

		valueTile("powerChannel", "powerChannel", width: 6, height: 1) {
			state "powerChannel", label: 'Channel ${currentValue}', backgroundColor: "#ffffff"
		}

		valueTile("gpio", "gpio", width: 6, height: 1) {
			state "gpio", label: 'GPIO ${gpio}', backgroundColor: "#ffffff"
		}

		main "switch"
		details(["switch", "powerChannel", "gpio"])
	}

	preferences {
		section("Main") {
            input(name: "powerChannel", type: "number", title: "Power Channel", description: "", displayDuringSetup: true, required: true)
			input(name: "gpio", type: "number", title: "GPIO", description: "", displayDuringSetup: false, required: false)
		}
	}
}

def initializeChild(Map options){
	log.debug "OPTIONS: $options"

	sendEvent(name : "powerChannel", value: options["powerChannel"]);
	sendEvent(name : "gpio", value: options["gpio"]);
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

	def channel = device.latestValue("powerChannel")
	def commandName = "Power$channel";
	def payload = power;

	log.debug "COMMAND: $commandName ($payload)"

	def command = parent.createCommand(commandName, payload, "setPowerCallback");;

    sendHubCommand(command);
}

def setPowerCallback(physicalgraph.device.HubResponse response){
	
	def channel = device.latestValue("powerChannel")
	
	log.debug "Finished Setting power (channel: $channel), JSON: ${response.json}"

    def on = response.json."POWER${channel}" == "ON";

	if ("$channel" == "1"){
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

	def powerChannel = device.latestValue("powerChannel");

	powerMask = powerMask << ("$powerChannel".toInteger() - 1); // shift the bits over 

	def on = (powerMask & status.Status.Power);

	setSwitchState(on);
}

def setSwitchState(on){
	log.debug "Setting switch to ${on ? 'ON' : 'OFF'}"

	sendEvent(name: "switch", value: on ? "on" : "off")
}