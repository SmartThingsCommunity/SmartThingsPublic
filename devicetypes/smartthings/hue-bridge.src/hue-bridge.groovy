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

	tiles(scale: 2) {
     	multiAttributeTile(name:"rich-control"){
			tileAttribute ("device.switch", key: "PRIMARY_CONTROL") {
	            attributeState "default", label: "Hue Bridge", action: "", icon: "st.Lighting.light99-hue", backgroundColor: "#F3C200"
			}    
	        tileAttribute ("serialNumber", key: "SECONDARY_CONTROL") {
	            attributeState "default", label:'SN: ${currentValue}'
			}    
        }    
		standardTile("icon", "icon", width: 1, height: 1, canChangeIcon: false, inactiveLabel: true, canChangeBackground: false) {
			state "default", label: "Hue Bridge", action: "", icon: "st.Lighting.light99-hue", backgroundColor: "#FFFFFF"
		}
		valueTile("serialNumber", "device.serialNumber", decoration: "flat", height: 1, width: 2, inactiveLabel: false) {
			state "default", label:'SN: ${currentValue}'
		}
		valueTile("networkAddress", "device.networkAddress", decoration: "flat", height: 2, width: 4, inactiveLabel: false) {
			state "default", label:'${currentValue}', height: 1, width: 2, inactiveLabel: false
		}

		main (["icon"])
		details(["rich-control", "networkAddress"])
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
		def map = description
		if (description instanceof String)  {
			map = stringToMap(description)
		}
		if (map?.name && map?.value) {
			log.trace "HUE BRIDGE, GENERATING EVENT: $map.name: $map.value"
			results << createEvent(name: "${map.name}", value: "${map.value}")
		} else {
        	log.trace "Parsing description"
			def msg = parseLanMessage(description)
			if (msg.body) {
				def contentType = msg.headers["Content-Type"]
				if (contentType?.contains("json")) {
					def bulbs = new groovy.json.JsonSlurper().parseText(msg.body)
					if (bulbs.state) {
						log.info "Bridge response: $msg.body"
					} else {
						// Sending Bulbs List to parent"
                        if (parent.state.inBulbDiscovery)
                        	log.info parent.bulbListHandler(device.hub.id, msg.body)
					}
				}
				else if (contentType?.contains("xml")) {
					log.debug "HUE BRIDGE ALREADY PRESENT"
				}
			}
		}
	}
	results
}
