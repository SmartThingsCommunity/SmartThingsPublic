import grails.converters.JSON

/**
*  JSON API Access App
*/

definition(
  name: "Bling_SmartApps",
  namespace: "BlingSmartApps",
  author: "Bling",
  description: "Return things as JSON",
  category: "My Apps",
  iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
  iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
  oauth: [displayName: "JSON API", displayLink: ""]
)


preferences {
  	section("Allow these things to be exposed via JSON...") {
    	input "switches", "capability.switch", title: "Switches", multiple: true, required: true, hideWhenEmpty: true

    	//input "temperatures", "capability.temperatureMeasurement", title: "Temp Measurements", multiple: true, required: false
    	//input "contacts", "capability.contactSensor", title: "Open/Closed Devices", multiple: true, required: false
    	//input "presences", "capability.presenceSensor", title: "Presence Sensors", multiple: true, required: false
    	//input "motions", "capability.motionSensor", title: "Motion Sensors", multiple: true, required: false
  	}
}

mappings {
 	path("/things") {
    	action: [
      		GET: "listThings"
    	]
  	}
}

def	listThings() {
	[
  		switches: switches.collect{device(it,"switch")}
  		/*
  		switches: switches.collect{device(it,"switch")},
  		temperatures: temperatures.collect{device(it,"temperature")},
  		contacts: contacts.collect{device(it,"contact")},
  		presences: presences.collect{device(it,"presence")},
  		motions: motions.collect{device(it,"motion")}
  		*/
	]
}

private device(it, type) {
	def device_state = [label:it.label, type:type, id:it.id, devtest:it]

	for (attribute in it.supportedAttributes) {
		device_state."${attribute}" = it.currentValue("${attribute}")
  	}

  	device_state ? device_state : null
}

def installed() {}

def updated() {}

