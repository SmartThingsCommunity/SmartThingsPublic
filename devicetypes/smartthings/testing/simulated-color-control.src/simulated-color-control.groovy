metadata {
	definition (name: "Simulated Color Control", namespace: "smartthings/testing", author: "SmartThings") {
    	capability "Color Control"
	}

	simulator {
		// TODO: define status and reply messages here
	}

	tiles {
    	controlTile("rgbSelector", "device.color", "color", height: 3, width: 3, inactiveLabel: false) {
            state "color"
        }
        valueTile("saturation", "device.saturation", inactiveLabel: false, decoration: "flat") {
            state "saturation", label: 'Sat ${currentValue}    '
        }
        valueTile("hue", "device.hue", inactiveLabel: false, decoration: "flat") {
            state "hue", label: 'Hue ${currentValue}   '
        }
        main "rgbSelector"
        details(["rgbSelector", "saturation", "hue"])
	}
}

// parse events into attributes
def parse(String description) {
	log.debug "Parsing '${description}'"

}

def setSaturation(percent) {
	log.debug "Executing 'setSaturation'"
	sendEvent(name: "saturation", value: percent)
}

def setHue(percent) {
	log.debug "Executing 'setHue'"
	sendEvent(name: "hue", value: percent)
}
