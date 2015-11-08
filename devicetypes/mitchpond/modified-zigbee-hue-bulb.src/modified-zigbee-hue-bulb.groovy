/* Philips Hue (via Zigbee)

    
*/

metadata {
	// Automatically generated. Make future change here.
	definition (name: "Modified Zigbee Hue Bulb", namespace: "mitchpond", author: "Mitch Pond") {
		capability "Switch Level"
		capability "Actuator"
		capability "Color Control"
		capability "Switch"
		capability "Configuration"
		capability "Polling"
		capability "Refresh"
        
        attribute "loop", "string"

		command "setAdjustedColor"
        command "startColorLoop"
        command "stopColorLoop"
        command "identify"

		fingerprint profileId: "C05E", inClusters: "0000,0003,0004,0005,0006,0008,0300,1000", outClusters: "0019"
	}

	// UI tile definitions
	tiles (scale: 2) {
    	multiAttributeTile(name: "switch", type: "lighting", width:6, height:4, canChangeIcon: true) {
        	tileAttribute ("device.switch", key: "PRIMARY_CONTROL") {
				attributeState "on", label:'${name}', action:"switch.off", icon:"st.lights.philips.hue-single", backgroundColor:"#79b821", nextState:"turningOff"
				attributeState "off", label:'${name}', action:"switch.on", icon:"st.lights.philips.hue-single", backgroundColor:"#ffffff", nextState:"turningOn"
				attributeState "turningOn", label:'${name}', action:"switch.off", icon:"st.lights.philips.hue-single", backgroundColor:"#79b821", nextState:"turningOff"
				attributeState "turningOff", label:'${name}', action:"switch.on", icon:"st.lights.philips.hue-single", backgroundColor:"#ffffff", nextState:"turningOn"
			}
        	tileAttribute("device.level", key: "SLIDER_CONTROL") {
				attributeState "level", action:"switch level.setLevel"
            }
            tileAttribute("device.color", key: "COLOR_CONTROL") {
            	attributeState "color", action:"setAdjustedColor"
            }
        }
		standardTile("switch", "device.switch", width: 1, height: 1, canChangeIcon: true) {
			state "off", label: '${name}', action: "switch.on", icon: "st.switches.switch.off", backgroundColor: "#ffffff"
			state "on", label: '${name}', action: "switch.off", icon: "st.switches.switch.on", backgroundColor: "#79b821"
		}
		standardTile("refresh", "device.switch", inactiveLabel: false, decoration: "flat") {
			state "default", label:"", action:"refresh.refresh", icon:"st.secondary.refresh"
		}
        standardTile("loopButton", "device.loop", width: 1, height: 1) {
        	state "off", label: "Color Loop", action: "startColorLoop", icon:"st.Food & Dining.dining18", backgroundColor: "#ffffff"
            state "on", label: "Color Loop", action: "stopColorLoop", icon:"st.Food & Dining.dining18", backgroundColor: "#79b821"
        } 
        standardTile("identify", "device.switch", width: 1, height: 1) {
        	state "default", action: "identify", icon:"st.Food & Dining.dining19", label: "Identify"
        }

		main(["switch"])
		details(["switch", "refresh", "identify", "loopButton"])
	}
    preferences {
    	input "fadeTimeInput", "number", title: "Color fade duration", description: "Enter the length of time to fade (sec)", min: 1
    }
}

// Parse incoming device messages to generate events
def parse(String description) {
	log.debug description
    log.debug "Parsed: ${zigbee.parse(description)}"
    log.debug "Parsed text ${zigbee.parse(description)?.text}"
    
	if (description?.startsWith("catchall:")) {
		def msg = zigbee.parse(description)
		log.trace msg
		log.trace "data: $msg.data"
	}
	else {
		def name = description?.startsWith("on/off: ") ? "switch" : null
		def value = name == "switch" ? (description?.endsWith(" 1") ? "on" : "off") : null
		def result = createEvent(name: name, value: value)
		log.debug "Parse returned ${result?.descriptionText}"
		return result
	}
}

def configure() {

	String zigbeeId = swapEndianHex(device.hub.zigbeeId)
	log.debug "Confuguring Reporting and Bindings."
	def configCmds = [
        //Switch Reporting
        "zcl global send-me-a-report 6 0 0x10 0 3600 {01}", "delay 500",
        "send 0x${device.deviceNetworkId} 1 1", "delay 1000",

        //Level Control Reporting
        "zcl global send-me-a-report 8 0 0x20 5 3600 {0010}", "delay 200",
        "send 0x${device.deviceNetworkId} 1 1", "delay 1500",

        "zdo bind 0x${device.deviceNetworkId} 1 1 6 {${device.zigbeeId}} {}", "delay 1000",
		"zdo bind 0x${device.deviceNetworkId} 1 1 8 {${device.zigbeeId}} {}", "delay 500",
	]
    return configCmds + refresh() // send refresh cmds as part of config
}

def refresh() {
	[
	"st rattr 0x${device.deviceNetworkId} 1 6 0", "delay 500",
    "st rattr 0x${device.deviceNetworkId} 1 8 0", "delay 500",
    "st wattr 0x${device.deviceNetworkId} 1 8 0x10 0x21 {${state.dOnOff}}"
    ]
}

def on() {
	log.debug "on()"
	sendEvent(name: "switch", value: "on")
	"st cmd 0x${device.deviceNetworkId} ${endpointId} 6 1 {}"
}

def off() {
	log.debug "off()"
	sendEvent(name: "switch", value: "off")
	"st cmd 0x${device.deviceNetworkId} ${endpointId} 6 0 {}"
}

def setHue(value) {
	def max = 0xfe
	log.trace "setHue($value)"
	sendEvent(name: "hue", value: value)
	def scaledValue = Math.round(value * max / 100.0)
	def cmd = "st cmd 0x${device.deviceNetworkId} ${endpointId} 0x300 0x00 {${hex(scaledValue)} 00 0000}"
	//log.info cmd
	cmd
}

def setAdjustedColor(value) {
	log.debug "setAdjustedColor: ${value}"
	def adjusted = value + [:]
	adjusted.hue = adjustOutgoingHue(value.hue)
	adjusted.level = null // needed because color picker always sends 100
	setColor(adjusted)
}

def setColor(value){
	log.trace "setColor($value)"
	def max = 0xfe

	sendEvent(name: "hue", value: value.hue)
	sendEvent(name: "saturation", value: value.saturation)
	def scaledHueValue = Math.round(value.hue * max / 100.0)
	def scaledSatValue = Math.round(value.saturation * max / 100.0)

	def cmd = []
	if (value.switch != "off" && device.latestValue("switch") == "off") {
		cmd << "st cmd 0x${device.deviceNetworkId} ${endpointId} 6 1 {}"
		cmd << "delay 150"
	}
    
	log.debug "${fadeTimeInput}"
	cmd << "st cmd 0x${device.deviceNetworkId} ${endpointId} 0x300 0x06 {${hex(scaledHueValue)} ${hex(scaledSatValue)} ${hex(fadeTimeInput,4)}}"

	if (value.level != null) {
		cmd << "delay 150"
		cmd.addAll(setLevel(value.level))
	}

	if (value.switch == "off") {
		cmd << "delay 150"
		cmd << off()
	}
	log.info cmd
	cmd
}

def setSaturation(value) {
	def max = 0xfe
	log.trace "setSaturation($value)"
	sendEvent(name: "saturation", value: value)
	def scaledValue = Math.round(value * max / 100.0)
	def cmd = "st cmd 0x${device.deviceNetworkId} ${endpointId} 0x300 0x03 {${hex(scaledValue)} 0000}"
	log.info cmd
	cmd
}

def identify() {
	log.debug "Ident requested"
	"st cmd 0x${device.deviceNetworkId} ${endpointId} 0x0000 0x40 {0100}"
}

def fadeOff() {
	"st cmd 0x${device.deviceNetworkId} ${endpointId} 0x0006 0x40 {0100}"
}

def instantOff() {
	"st cmd 0x${device.deviceNetworkId} ${endpointId} 0x0006 0x40 {0001}"
}

def timedOn() {
	//the middle four bytes of the payload represent the time to wait in 1/20s of a second
	"st cmd 0x${device.deviceNetworkId} ${endpointId} 0x0006 0x42 {00 ${hex(100,4)} 0000}"
}

def startColorLoop() {
	sendEvent(name: "loop", value: "on")
	"st cmd 0x${device.deviceNetworkId} ${endpointId} 0x0300 0x44 {01 02 01 0000 0000}"
}

def stopColorLoop() {
	sendEvent(name: "loop", value: "off")
	"st cmd 0x${device.deviceNetworkId} ${endpointId} 0x0300 0x44 {01 00 00 0000 0000}"
}

def poll(){
	log.debug "Poll is calling refresh"
	refresh()
}

def setLevel(value) {
	log.trace "setLevel($value)"
	def cmds = []

	if (value == 0) {
		sendEvent(name: "switch", value: "off")
		cmds << "st cmd 0x${device.deviceNetworkId} ${endpointId} 6 0 {}"
	}
	else if (device.latestValue("switch") == "off") {
		sendEvent(name: "switch", value: "on")
	}

	sendEvent(name: "level", value: value)
	def level = new BigInteger(Math.round(value * 255 / 100).toString()).toString(16)
	cmds << "st cmd 0x${device.deviceNetworkId} ${endpointId} 8 4 {${level} 0000}"

	//log.debug cmds
	cmds
}

private getEndpointId() {
	new BigInteger(device.endpointId, 16).toString()
}

private hex(value, width=2) {
	def s = new BigInteger(Math.round(new Double(value)).toString()).toString(16)
    while (s.size() < 2) {
    	s = "0" + s
    }
	while (s.size() < width) {
		s = s + "0"
	}
	s
}

private adjustOutgoingHue(percent) {
	def adjusted = percent
	if (percent > 31) {
		if (percent < 63.0) {
			adjusted = percent + (7 * (percent -30 ) / 32)
		}
		else if (percent < 73.0) {
			adjusted = 69 + (5 * (percent - 62) / 10)
		}
		else {
			adjusted = percent + (2 * (100 - percent) / 28)
		}
	}
	log.info "percent: $percent, adjusted: $adjusted"
	adjusted
}