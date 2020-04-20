/**
 *  Arduino Garage
 *
 *  Author: Marius Piedallu van Wyk
 *  Date: 2014-07-27
 */

metadata {
	// Automatically generated. Make future change here.
	definition (name: "Arduino Garage", namespace: "Lailoken", author: "Marius Piedallu van Wyk") {
		capability "Switch"
		// capability "Sensor"
        capability "Contact Sensor"

        attribute "contact",   "string"
        
        attribute "leftDoor",  "string"
        attribute "rightDoor", "string"
        attribute "backDoor",  "string"
        
		command "pushLeft"
		command "pushRight"        
    }
    
    simulator {
    }

	// Preferences

	// tile definitions
	tiles {
        standardTile("contact", "device.contact", width: 1, height: 1, canChangeIcon: true, canChangeBackground: true) {
			state "closed",  label: 'Closed',  icon: "st.doors.garage.garage-closed",  backgroundColor: "#79b821"
            state "open",    label: 'Open',    icon: "st.doors.garage.garage-open",    backgroundColor: "#ffa81e"
 		}
		standardTile("leftDoor", "device.leftDoor", width: 1, height: 1, canChangeIcon: true, canChangeBackground: true) {
			state "closed",  label: "Closed",  icon: "st.doors.garage.garage-closed",  backgroundColor: "#79b821", action: "pushLeft", nextState: "open"
            state "open",    label: "Open",    icon: "st.doors.garage.garage-open",    backgroundColor: "#ffa81e", action: "pushLeft", nextState: "toggle"
            state "toggle",  label: "Toggle",  icon: "st.doors.garage.garage-opening", backgroundColor: "#89C2E8", action: "pushLeft", nextState: "toggle"
 		}
        standardTile("rightDoor", "device.rightDoor", width: 1, height: 1, canChangeIcon: true, canChangeBackground: true) {
			state "closed",  label: "Closed",  icon: "st.doors.garage.garage-closed",  backgroundColor: "#79b821", action: "pushRight", nextState: "open"
            state "open",    label: "Open",    icon: "st.doors.garage.garage-open",    backgroundColor: "#ffa81e", action: "pushRight", nextState: "toggle"
            state "toggle",  label: "Toggle",  icon: "st.doors.garage.garage-opening", backgroundColor: "#89C2E8", action: "pushRight", nextState: "toggle"
 		}
        standardTile("backDoor", "device.backDoor", width: 1, height: 1, canChangeIcon: true, canChangeBackground: true) {
			state "closed",  label: "Closed",  icon: "st.contact.contact.closed",      backgroundColor: "#79b821"
            state "open",    label: "Open",    icon: "st.contact.contact.open",        backgroundColor: "#ffa81e"
 		}

		main(["contact"])
        
		details(["leftDoor", "rightDoor", "backDoor"])
	}

	preferences {
    }    
    
}

def parse(String description) {
	def msg = zigbee.parse(description)?.text
	log.debug "Parse got '${msg}'"
    
    def parts = msg.split(" ")
    def name  = parts.length>0?parts[0].trim():null
    def value = parts.length>1?parts[1].trim():null
    
	name = value != "ping" ? name : null
    
	def result
	if(name == "anyDoor") {
    	// Use anyDoor as the virtual contact sensor for whole space:
		result = createEvent(name: "contact", value: value)
    } else {
		result = createEvent(name: name, value: value)
    }
    
	log.debug result
    
	return result
}

def pushLeft() {
	log.debug "Left Button pressed"
    zigbee.smartShield(text: "pushLeft").format()
}

def pushRight() {
	log.debug "Right Button pressed"
    zigbee.smartShield(text: "pushRight").format()
}