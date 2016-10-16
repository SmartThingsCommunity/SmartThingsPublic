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
		capability "Health Check"

		command "refresh"

		attribute "serialNumber", "string"
		attribute "networkAddress", "string"
		// Used to indicate if bridge is reachable or not, i.e. is the bridge connected to the network
		// Possible values "Online" or "Offline"
		attribute "status", "string"
		// Id is the number on the back of the hub, Hue uses last six digits of Mac address
		// This is also used in the Hue application as ID
		attribute "idNumber", "string"
	}

	simulator {
		// TODO: define status and reply messages here
	}

	tiles(scale: 2) {
     	multiAttributeTile(name:"rich-control"){
			tileAttribute ("device.status", key: "PRIMARY_CONTROL") {
				attributeState "Offline", label: '${currentValue}', action: "", icon: "st.Lighting.light99-hue", backgroundColor: "#ffffff"
	            attributeState "Online", label: '${currentValue}', action: "", icon: "st.Lighting.light99-hue", backgroundColor: "#79b821"
			}
		}
		valueTile("doNotRemove", "v", decoration: "flat", height: 1, width: 4, inactiveLabel: false) {
			state "default", label:'Hue devices will not work if removed!'
		}
		valueTile("serialNumber", "device.serialNumber", decoration: "flat", height: 1, width: 4, inactiveLabel: false) {
			state "default", label:'SN: ${currentValue}'
		}
		valueTile("idNumber", "device.idNumber", decoration: "flat", height: 1, width: 4, inactiveLabel: false) {
			state "default", label:'ID: ${currentValue}'
		}
		valueTile("networkAddress", "device.networkAddress", decoration: "flat", height: 1, width: 4, inactiveLabel: false) {
			state "default", label:'IP: ${currentValue}'
		}

		standardTile("refresh", "device.refresh", height: 4, width: 2, inactiveLabel: false, decoration: "flat") {
			state "default", label:"", action:"refresh.refresh", icon:"st.secondary.refresh"
		}

		main (["rich-control"])
		details(["rich-control", "doNotRemove", "serialNumber", "refresh", "idNumber", "networkAddress"])
	}
}

void installed() {
	sendEvent(name: "checkInterval", value: 60 * 12, data: [protocol: "lan"], displayed: false)
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
					}
				} else if (contentType?.contains("xml")) {
					log.debug "HUE ADVANCED BRIDGE ALREADY PRESENT"
                    parent.hubVerification(device.hub.id, msg.body)
				}
			}
		}
	}
	results
}

def ping() {
	log.debug "${parent.ping(this)}"
}

void refresh() {
    log.debug "Executing 'refresh'"
    parent.manualRefresh()
}

void setHADeviceHandler(circadianDaylightIntegration) {
	return
}