
metadata {
	// Automatically generated. Make future change here.
	definition(name: "Auto-Closing Window Sensor", namespace: "BrettSheleski", author: "Brett Sheleski", ocfDeviceType: "oic.d.smartplug") {
		capability "Contact Sensor"

		command "open"
		command "close"
	}

	simulator {
		status "open": "contact:open"
		status "closed": "contact:closed"
	}

	tiles {
		standardTile("contact", "device.contact", width: 2, height: 2) {
			state("closed", label:'${name}', icon:"st.contact.contact.closed", backgroundColor:"#00A0DC", action: "open")
			state("open", label:'${name}', icon:"st.contact.contact.open", backgroundColor:"#e86d13", action: "close")
		}
		main "contact"
		details "contact"
	}
}

def installed() {
	initialize()
}

def updated() {
	initialize()
}

private initialize() {

}

def parse(String description) {
}

def open() {
	log.trace "open()"
	sendEvent(name: "contact", value: "open")

	runin(5, close)
}

def close() {
	log.trace "close()"
    sendEvent(name: "contact", value: "closed")
}