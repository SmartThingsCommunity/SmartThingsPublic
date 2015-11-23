/**
 *  Hue Bridge
 *
 *  Author: SmartThings
 */
// for the UI
metadata {
	// Automatically generated. Make future change here.
	definition (name: "Hue Bridge", namespace: "smartthings", author: "SmartThings") {
		attribute "serialNumber", "string"
		attribute "networkAddress", "string"
	}

	simulator {
		// TODO: define status and reply messages here
	}

	tiles {
		standardTile("icon", "icon", width: 1, height: 1, canChangeIcon: false, inactiveLabel: true, canChangeBackground: false) {
			state "default", label: "Hue Bridge", action: "", icon: "st.Lighting.light99-hue", backgroundColor: "#FFFFFF"
		}
		valueTile("serialNumber", "device.serialNumber", decoration: "flat", height: 1, width: 2, inactiveLabel: false) {
			state "default", label:'SN: ${currentValue}'
		}
		valueTile("networkAddress", "device.networkAddress", decoration: "flat", height: 1, width: 2, inactiveLabel: false) {
			state "default", label:'${currentValue}', height: 1, width: 2, inactiveLabel: false
		}

		main (["icon"])
		details(["networkAddress","serialNumber"])
	}
}

// parse events into attributes
def parse(description) {
	log.debug "Parsing '${description}'"
	def results = []
	def result = parent.parse(this, description)

	if (result instanceof physicalgraph.device.HubAction){
		log.trace "HUE BRIDGE HubAction received -- DOES THIS EVER HAPPEN?"
		results << result
	} else if (description == "updated") {
		//do nothing
		log.trace "HUE BRIDGE was updated"
	} else {
		log.trace "HUE BRIDGE, OTHER"
		def map = description
		if (description instanceof String)  {
			map = stringToMap(description)
		}
		if (map?.name && map?.value) {
			log.trace "HUE BRIDGE, GENERATING EVENT: $map.name: $map.value"
			results << createEvent(name: "${map?.name}", value: "${map?.value}")
		}
		else {
			log.trace "HUE BRIDGE, OTHER"
			def msg = parseLanMessage(description)
			if (msg.body) {
				def contentType = msg.headers["Content-Type"]
				if (contentType?.contains("json")) {
					def bulbs = new groovy.json.JsonSlurper().parseText(msg.body)
					if (bulbs.state) {
						log.warn "NOT PROCESSED: $msg.body"
					}
					else {
						log.debug "HUE BRIDGE, GENERATING BULB LIST EVENT: $bulbs"
                        sendEvent(name: "bulbList", value: device.hub.id, isStateChange: true, data: bulbs, displayed: false)
					}
				}
				else if (contentType?.contains("xml")) {
					log.debug "HUE BRIDGE, SWALLOWING BRIDGE DESCRIPTION RESPONSE -- BRIDGE ALREADY PRESENT"
				}
			}
		}
	}
	results
}
