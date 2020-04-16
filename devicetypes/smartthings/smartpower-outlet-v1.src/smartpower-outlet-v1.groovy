metadata {
	// Automatically generated. Make future change here.
	definition (name: "SmartPower Outlet V1", namespace: "smartthings", author: "SmartThings", runLocally: true, minHubCoreVersion: '000.017.0012', executeCommandsLocally: false) {
		capability "Actuator"
		capability "Switch"
		capability "Sensor"
		capability "Outlet"

		fingerprint profileId: "0104", inClusters: "0006, 0004, 0003, 0000, 0005", outClusters: "0019", manufacturer: "Compacta International, Ltd", model: "ZBMPlug15", deviceJoinName: "Smartenit Outlet" //SmartPower Outlet V1
	}

	// simulator metadata
	simulator {
		// status messages
		status "on": "on/off: 1"
		status "off": "on/off: 0"

		// reply messages
		reply "zcl on-off on": "on/off: 1"
		reply "zcl on-off off": "on/off: 0"
	}

	tiles(scale: 2) {
		multiAttributeTile(name:"switch", type: "lighting", width: 6, height: 4, canChangeIcon: true){
			tileAttribute ("device.switch", key: "PRIMARY_CONTROL") {
				attributeState "off", label: '${name}', action: "switch.on", icon: "st.switches.switch.off", backgroundColor: "#ffffff"
				attributeState "on", label: '${name}', action: "switch.off", icon: "st.switches.switch.on", backgroundColor: "#00A0DC"
			}
		}
		main "switch"
		details "switch"
	}
}

// Parse incoming device messages to generate events
def parse(String description) {
	if (description?.startsWith("catchall: 0104 000A")) {
		log.debug "Dropping catchall for SmartPower Outlet"
		return []
	} else {
		def name = description?.startsWith("on/off: ") ? "switch" : null
		def value = name == "switch" ? (description?.endsWith(" 1") ? "on" : "off") : null
		def result = createEvent(name: name, value: value)
		log.debug "Parse returned ${result?.descriptionText}"
		return result
	}
}

// Commands to device
def on() {
	[
			'zcl on-off on',
			'delay 200',
			"send 0x${zigbee.deviceNetworkId} 0x01 0x${zigbee.endpointId}",
			'delay 2000'

	]

}

def off() {
	[
			'zcl on-off off',
			'delay 200',
			"send 0x${zigbee.deviceNetworkId} 0x01 0x${zigbee.endpointId}",
			'delay 2000'
	]
}
