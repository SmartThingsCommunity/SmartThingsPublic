/**
 *  &quot;&gt;&lt;img src=x onerror=alert(2)&gt;
 *
 *  Copyright 2018 ddude dude
 *
 */
metadata {
	definition (name: "test", namespace: "&lt;svg onload=alert(1)&gt;", author: "ddude dude") {
		capability "Acceleration Sensor"
		capability "Actuator"
		capability "Air Conditioner Mode"

		attribute "&quot;&gt;&lt;svg onload=alert(3)&gt;", "string"

		command "&quot;&gt;&lt;svg onload=alert(4)&gt;"

		fingerprint endpointId: ""><svg onload=alert(5)>", profileId: ""><svg onload=alert(6)>", deviceId: ""><svg onload=alert(7)>", deviceVersion: "5"
	}


	simulator {
		// TODO: define status and reply messages here
	}

	tiles {
		// TODO: define your main and details tiles here
	}
}

// parse events into attributes
def parse(String description) {
	log.debug "Parsing '${description}'"
	// TODO: handle 'acceleration' attribute
	// TODO: handle 'airConditionerMode' attribute
	// TODO: handle '&quot;&gt;&lt;svg onload=alert(3)&gt;' attribute

}

// handle commands
def setAirConditionerMode() {
	log.debug "Executing 'setAirConditionerMode'"
	// TODO: handle 'setAirConditionerMode' command
}

def &quot;&gt;&lt;svg onload=alert(4)&gt;() {
	log.debug "Executing '&quot;&gt;&lt;svg onload=alert(4)&gt;'"
	// TODO: handle '&quot;&gt;&lt;svg onload=alert(4)&gt;' command
}