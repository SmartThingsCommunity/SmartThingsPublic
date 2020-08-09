/*
	Device handle to setup a simulated switch with contact sensor to be used with other services like Alexa as an input sensor to trigger a routine.
	Turning switch On will trigger contact to close
    Turning switch Off will trigger contact to open
*/
metadata {
	// Define Device, Icon, and capabilities
    definition (name: "Simulated Contact Switch", namespace: "tamerkalla", author: "Tamer Kalla", deviceType:"switch", ocfDeviceType:"oic.d.switch") {
        capability "Actuator"
        capability "Sensor"
        capability "Switch"
        capability "Health Check"
        capability "Contact Sensor"
    }
	
    // Preferences
    preferences {}

	// Simulator Status and response
    simulator {
		status "open": "contact:open"
		status "closed": "contact:closed"
	}
    
    // GUI  Tiles (Some functionality doesn't apply in newer version of smartthings)
    tiles {
    	// Switch Device with each state and action
        standardTile("switch", "device.switch", width: 2, height: 2, canChangeIcon: true) {
            state "off", label: '${currentValue}', action: "on", icon: "st.switches.switch.off", backgroundColor: "#ffffff"
            state "on", label: '${currentValue}', action: "off", icon: "st.switches.switch.on", backgroundColor: "#00A0DC"
        }
        // Main Tile GUI devices to show
        main(["switch"])
        // Detailed GUI deveices Tiles
        details(["switch"])
    }
}

// On Action Handle
def on() {
	log.debug "Executing 'on'"
    sendEvent(name: "switch", value: "on", isStateChange: true)
    sendEvent(name: "contact", value: "closed", isStateChange: true)
}

// Off action Handle
def off() {
    sendEvent(name: "switch", value: "off", isStateChange: true)
    sendEvent(name: "contact", value: "open", isStateChange: true)
}

// Device Installed Handle
def installed() {
	log.trace "Executing 'installed'"
	initialize()
}

// Device Update Handle
def updated() {
	log.trace "Executing 'updated'"
	initialize()
}

// Initialize Device
private initialize() {
	log.trace "Executing 'initialize'"
	
    // Set Status and health to online
	sendEvent(name: "DeviceWatch-DeviceStatus", value: "online")
	sendEvent(name: "healthStatus", value: "online")
    // Set as an untracked cloud device
	sendEvent(name: "DeviceWatch-Enroll", value: [protocol: "cloud", scheme:"untracked"].encodeAsJson(), displayed: false)
    
    // Turn off device on initalization
    off()
}

// Parse events into attributes
def parse(String description) {
	log.debug "Parsing '${description}'"

}