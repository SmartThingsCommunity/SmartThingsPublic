/**
 *  Hue Bridge
 *
 *  Author: SmartThings
 */
 // for the UI
metadata {
	// Automatically generated. Make future change here.
	definition (name: "Hue Bridge", namespace: "smartthings", author: "SmartThings") {
    	capability "refresh"
        
    	attribute "networkAddress", "string"
        attribute "serialNumber", "string"
        
	}

	simulator {
		// TODO: define status and reply messages here
	}

<<<<<<< HEAD
	tiles {
		standardTile("icon", "icon", width: 1, height: 1, canChangeIcon: false, inactiveLabel: true, canChangeBackground: false) {
			state "default", label: "Hue Bridge", action: "", icon: "st.Lighting.light99-hue", backgroundColor: "#FFFFFF"
		}
=======
	tiles(scale: 2) {
     	multiAttributeTile(name:"rich-control"){
			tileAttribute ("", key: "PRIMARY_CONTROL") {
	            attributeState "default", label: "Hue Bridge", action: "", icon: "st.Lighting.light99-hue", backgroundColor: "#F3C200"
			}
	        tileAttribute ("serialNumber", key: "SECONDARY_CONTROL") {
	            attributeState "default", label:'SN: ${currentValue}'
			}
        }
<<<<<<< HEAD
<<<<<<< HEAD
>>>>>>> SmartThingsCommunity/master
=======
>>>>>>> SmartThingsCommunity/master
=======
>>>>>>> pr/27
		valueTile("serialNumber", "device.serialNumber", decoration: "flat", height: 1, width: 2, inactiveLabel: false) {
			state "default", label:'SN: ${currentValue}'
		}
		valueTile("networkAddress", "device.networkAddress", decoration: "flat", height: 1, width: 2, inactiveLabel: false) {
			state "default", label:'${currentValue}'
		}
		standardTile("refresh", "device.switch", inactiveLabel: false, decoration: "flat") {
			state "default", label:'', action:"refresh.refresh", icon:"st.secondary.refresh"
		}        

<<<<<<< HEAD
<<<<<<< HEAD
<<<<<<< HEAD
		main (["icon"])
		details(["networkAddress","serialNumber","refresh"])
=======
=======
>>>>>>> SmartThingsCommunity/master
=======
>>>>>>> pr/27
		main (["rich-control"])
		details(["rich-control", "networkAddress"])
>>>>>>> SmartThingsCommunity/master
	}
}

// parse events into attributes
def parse(description) {
	log.debug "Parsing '${description}'"
	def results = []
	def result = parent.parse(this, description)

	if (result instanceof physicalgraph.device.HubAction){
		results << result
	} else if (description == "updated") {
		//do nothing
		log.debug "Hue Bridge was updated"
	} else {
		def map = description
		if (description instanceof String)  {
			log.debug "Hue Bridge stringToMap - ${map}"
			map = stringToMap(description)
		}
		if (map?.name && map?.value) {
<<<<<<< HEAD
			results << createEvent(name: "${map?.name}", value: "${map?.value}")
=======
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
                    parent.hubVerification(device.hub.id, msg.body)
				}
			}
>>>>>>> SmartThingsCommunity/master
		}
	}
	results
}

def refresh() {
	def result = new physicalgraph.device.HubAction(
    	method: "GET",
		path:    "/api/${parent.username()}/lights",
		action:  action,
		body:    body,
		headers: [Host: device.currentValue("networkAddress")]
	)
	log.debug result
    result
}