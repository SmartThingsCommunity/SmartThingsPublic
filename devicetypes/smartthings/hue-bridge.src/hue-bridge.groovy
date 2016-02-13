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

	tiles {
		standardTile("icon", "icon", width: 1, height: 1, canChangeIcon: false, inactiveLabel: true, canChangeBackground: false) {
			state "default", label: "Hue Bridge", action: "", icon: "st.Lighting.light99-hue", backgroundColor: "#FFFFFF"
		}
		valueTile("serialNumber", "device.serialNumber", decoration: "flat", height: 1, width: 2, inactiveLabel: false) {
			state "default", label:'SN: ${currentValue}'
		}
		valueTile("networkAddress", "device.networkAddress", decoration: "flat", height: 1, width: 2, inactiveLabel: false) {
			state "default", label:'${currentValue}'
		}
		standardTile("refresh", "device.switch", inactiveLabel: false, decoration: "flat") {
			state "default", label:'', action:"refresh.refresh", icon:"st.secondary.refresh"
		}        

		main (["icon"])
		details(["networkAddress","serialNumber","refresh"])
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
			results << createEvent(name: "${map?.name}", value: "${map?.value}")
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