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
    parent.sendCommandFromChild("Power${state.powerChannel}", power, device.deviceNetworkId, "setPowerCallback");
}

def setPowerCallback(physicalgraph.device.HubResponse response){
	log.debug "Finished Setting power: $response"
    def on = response.json.POWER == "ON";

    sendEvent(name: "switch", value: on ? "on" : "off")
}