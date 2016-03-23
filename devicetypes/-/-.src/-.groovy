/**
 *가상조명1 
 *
 *  Copyright 2016 김은경
 *
 */

metadata {
	definition (name: "조명센서", namespace: "은경이네", author: "김은경") {
		capability "Color Control"

		attribute "삼성조명", "string"

		command "삼성조명"
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
	// TODO: handle 'hue' attribute
	// TODO: handle 'saturation' attribute
	// TODO: handle 'color' attribute
	// TODO: handle '삼성조명' attribute

}

// handle commands
def setHue() {
	log.debug "Executing 'setHue'"
	// TODO: handle 'setHue' command
}

def setSaturation() {
	log.debug "Executing 'setSaturation'"
	// TODO: handle 'setSaturation' command
}

def setColor() {
	log.debug "Executing 'setColor'"
	// TODO: handle 'setColor' command
}

def 삼성조명() {
	log.debug "Executing '삼성조명'"
	// TODO: handle '삼성조명' command
}