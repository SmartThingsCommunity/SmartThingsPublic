metadata {
	definition (name: "Open/Closed Sensor", namespace: "ThirdReality", author: "ThirdReality", ocfDeviceType: "x.com.st.d.sensor.contact") {
		capability "Contact Sensor"
		capability "Sensor"

		//fingerprint profileId: "0104", deviceId: "0402", inClusters: "0000,0001,0003,0500", outClusters: "0000", deviceJoinName: "Open/Closed Sensor"
        fingerprint manufacturer:"THIRDREALITY", model:"3RMS16BZ", deviceJoinName: "Motion Sensor" 
        fingerprint manufacturer:"THIRDREALITY", model:"3RDS17BZ", deviceJoinName: "Door Sensor" 
        fingerprint manufacturer:"THIRDREALITY", model:"3RWS18BZ", deviceJoinName: "WaterLeak Senor" 
        
	}

	// simulator metadata
	simulator {
		// status messages
		status "open":   "zone report :: type: 19 value: 0031"
		status "closed": "zone report :: type: 19 value: 0030"
	}

	// UI tile definitions
	tiles {
		standardTile("contact", "device.contact", width: 2, height: 2) {
			state "open", label: '${name}', icon: "st.contact.contact.open", backgroundColor: "#e86d13"
			state "closed", label: '${name}', icon: "st.contact.contact.closed", backgroundColor: "#00A0DC"
		}

		main "contact"
		details "contact"
	}
}

// Parse incoming device messages to generate events
def parse(String description) {
	def resMap
	if (description.startsWith("zone")) {
		resMap = createEvent(name: "contact", value: zigbee.parseZoneStatus(description).isAlarm1Set() ? "open" : "closed")
	}

	log.debug "Parse returned $resMap"
	return resMap
}