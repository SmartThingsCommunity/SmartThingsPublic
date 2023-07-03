metadata {
	definition (name: "MCOHome Touch Panel Switch", namespace: "automateasia", author: "edwin") {
		capability "Switch"
		capability "Refresh"
		attribute "switch", "string"
	}

	// TODO: To build simulator code
	simulator {
	}

	tiles {
		standardTile("switch", "device.switch", canChangeIcon: true) {
			state("on", label: "Switch", action: "off", icon: "st.Home.home30", backgroundColor: "#79b821")
			state("off", label: "Switch", action: "on", icon: "st.Home.home30", backgroundColor: "#ffffff")
		}
	
		main "switch"
	}
}

void sendEventOn() {
	sendEvent(name: "switch", value: "on", isStateChange: true, display: false)
}

void sendEventOff() {
	sendEvent(name: "switch", value: "off", isStateChange: true, display: false)
}

void on() {
	parent.childOn(device.deviceNetworkId)
}

void off() {
	parent.childOff(device.deviceNetworkId)
}

void refresh() {
	parent.childRefresh(device.deviceNetworkId)
}