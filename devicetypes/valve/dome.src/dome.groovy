/**
 *  
metadata {
	definition (name: "Dome", namespace: "Valve", author: "Paul Rollins") {
		capability "Valve"

		attribute "Water valve", "string"
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
	// TODO: handle 'valve' attribute
	// TODO: handle 'Water valve' attribute

}

// handle commands
def open() {
	log.debug "Executing 'open'"
	// TODO: handle 'open' command
}

def close() {
	log.debug "Executing 'close'"
	// TODO: handle 'close' command
}