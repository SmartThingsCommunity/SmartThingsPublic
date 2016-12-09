definition(
    name: "TotalConnect Sensor Updater",
    namespace: "cdatwood",
    author: "Chris Atwood",
    description: "Help refresh sensors attached to Total Connect in near real timea to register motion or open/closed doors. ",
    category: "Safety & Security",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    oauth: true)


preferences {
	section("Sensors to force refresh...") {
		input "contacts", "capability.contactSensor", title: "Which sensors?", multiple: true
	}
}

mappings {

	path("/contacts") {
		action: [
			GET: "listContacts"
		]
	}
	path("/contacts/:id") {
		action: [
			GET: "showContact"
		]
	}
	path("/contacts/:id/:command") {
		action: [
			GET: "updateContact"
		]
	}  
    
}

def installed() {
	subscribe(contacts, "refresh", deviceHandlerRefresh)
}

def updated() {}

//contact sensors
def listContacts() {
    log.debug "listing sensors for TotalConnect 2.0"
	contacts.collect{device(it,"contact")}
}

def showContact() {
	show(contacts, "contact")
}
def updateContact() {
	log.debug "starting update, request: params: ${params}"
	update(contacts)
}

def deviceHandlerRefresh(evt) {
    if (evt.value == "open") {
        log.debug "sensor is open!"
    } else if (evt.value == "closed") {
        log.debug "sensor is open!"
    }
}

private void update(devices) {
	log.debug "update, request: params: ${params}, devices: $devices.id"

		def device = devices.find { it.id == params.id }
		if (!device) {
			httpError(404, "Device not found")
		} else {
            	device.refresh()
                log.debug "device.displayName"
		[id: device.id, label: device.displayName, value: s, status: device.contactState]
}

    
}

private show(devices, type) {
	log.debug "listing TotalConnect 2.0 sensors"
	def device = devices.find { it.id == params.id }
	if (!device) {
		httpError(404, "Device not found")
	}
	else {
		def attributeName = type == "motionSensor" ? "motion" : type
		def s = device.contactState
        log.debug "device.displayName"
		[id: device.id, label: device.displayName, value: s, type: type]
	}
}


private device(it, type) {
	it ? [id: it.id, label: it.label, value: it.contactState, type: type] : null
}