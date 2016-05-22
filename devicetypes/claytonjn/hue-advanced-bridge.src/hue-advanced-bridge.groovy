/**
 *  Hue Advanced Bridge
 *
 *  Author: claytonjn
 */
// for the UI
metadata {
	// Automatically generated. Make future change here.
	definition (name: "Hue Advanced Bridge", namespace: "claytonjn", author: "claytonjn") {
		capability "Refresh"

		command "refresh"

		attribute "serialNumber", "string"
		attribute "networkAddress", "string"
	}

	simulator {
		// TODO: define status and reply messages here
	}

	tiles(scale: 2) {
     	multiAttributeTile(name:"rich-control"){
			tileAttribute ("", key: "PRIMARY_CONTROL") {
	            attributeState "default", label: "Hue Advanced Bridge", action: "", icon: "st.Lighting.light99-hue", backgroundColor: "#F3C200"
			}
	        tileAttribute ("serialNumber", key: "SECONDARY_CONTROL") {
	            attributeState "default", label:'SN: ${currentValue}'
			}
        }
		valueTile("serialNumber", "device.serialNumber", decoration: "flat", height: 1, width: 4, inactiveLabel: false) {
			state "default", label:'SN: ${currentValue}'
		}
		valueTile("networkAddress", "device.networkAddress", decoration: "flat", height: 1, width: 4, inactiveLabel: false) {
			state "default", label:'IP: ${currentValue}'
		}

		standardTile("refresh", "device.refresh", height: 2, width: 2, inactiveLabel: false, decoration: "flat") {
			state "default", label:"", action:"refresh.refresh", icon:"st.secondary.refresh"
		}

		main (["rich-control"])
		details(["rich-control", "serialNumber", "refresh", "networkAddress"])
	}
}

// parse events into attributes
def parse(description) {
	log.debug "Parsing '${description}'"
	def results = []
	def result = parent.parse(this, description)
	if (result instanceof physicalgraph.device.HubAction){
		log.trace "HUE ADVANCED BRIDGE HubAction received -- DOES THIS EVER HAPPEN?"
		results << result
	} else if (description == "updated") {
		//do nothing
		log.trace "HUE ADVANCED BRIDGE was updated"
	} else {
		def map = description
		if (description instanceof String)  {
			map = stringToMap(description)
		}
		if (map?.name && map?.value) {
			log.trace "HUE ADVANCED BRIDGE, GENERATING EVENT: $map.name: $map.value"
			results << createEvent(name: "${map.name}", value: "${map.value}")
		} else {
        	log.trace "Parsing description"
			def msg = parseLanMessage(description)
			if (msg.body) {
				def contentType = msg.headers["Content-Type"]
				if (contentType?.contains("json")) {
					def devices = new groovy.json.JsonSlurper().parseText(msg.body)
					if (devices.state || devices.action) {
						log.info "Bridge response: $msg.body"
					} else {
						// Sending Bulbs List to parent"
                        if (parent.state.inBulbDiscovery)
                        	log.info parent.bulbListHandler(device.hub.id, msg.body)
					}
				}
				else if (contentType?.contains("xml")) {
					log.debug "HUE ADVANCED BRIDGE ALREADY PRESENT"
                    parent.hubVerification(device.hub.id, msg.body)
				}
			}
		}
	}
	results
}

void refresh() {
    log.debug "Executing 'refresh'"
    parent.manualRefresh()
}