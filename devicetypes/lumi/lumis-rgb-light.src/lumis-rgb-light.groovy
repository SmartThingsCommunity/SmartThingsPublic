
metadata {
	definition (
		name: "Lumi's RGB Light",
		namespace: "lumi",
		author: "phuclm", 
        preference: "smartthings lightingDeviceTile") {

        capability "Actuator"
        capability "Configuration"
		capability "Color Control"
		capability "Switch"
		capability "Refresh"
        
		capability "Sensor"

		command "reset"
        
        fingerprint profileId: "0104", inClusters: "0000,0003,0004,0005,0006,0008,0300", manufacturer: "Lumi", model: "LM-RGB"
        fingerprint profileId: "0104", inClusters: "0000,0003,0004,0005,0006,0008,0300", manufacturer: "Lumi R&D", model: "LM-RGB"
	}
    
	tiles(scale: 1) {
    	standardTile("switch", "device.switch", width: 1, height: 1, canChangeIcon: true) {
			state "on", label:'${name}', action:"switch.off", icon:"st.lights.philips.hue-multi", backgroundColor:"#79b821", nextState:"off"
			state "off", label:'${name}', action:"switch.on", icon:"st.lights.philips.hue-multi", backgroundColor:"#ffffff", nextState:"on"
		}
        
    	controlTile("rgbSelector", "device.color", "color", height: 3, width: 3, inactiveLabel: false) {
			state "color", action:"setColor"
		}
        valueTile("color", "device.color", inactiveLabel: false, decoration: "flat", width: 3, height: 1) {
			state "color", label: '${currentValue}'
		}

		standardTile("reset", "device.reset", inactiveLabel: false, decoration: "flat", width: 1, height: 1) {
			state "reset", label:"Reset Color", action:"reset", icon:"st.lights.philips.hue-single", defaultState: true
		}
		standardTile("refresh", "device.switch", inactiveLabel: false, decoration: "flat", width: 1, height: 1) {
			state "refresh", label:"", action:"refresh.refresh", icon:"st.secondary.refresh", defaultState: true
		}

		main(["switch"])
		details(["switch", "refresh", "reset", "rgbSelector", "color"])
	}
}

// parse events into attributes
def parse(description) 
{
	/*log.debug "parse() - $description"*/
}

// handle commands
def on() 
{
	log.debug "on"
    
    def command = ["st wattr 0x${device.deviceNetworkId} 0x01 0x0006 0x0000 0x0010 {01}", "delay 200", sendColorValuesToDevice(state?.rgbColor)]
}

def off() 
{
	log.debug "off"
    
    def command = ["st wattr 0x${device.deviceNetworkId} 0x01 0x0006 0x0000 0x0010 {00}", "delay 200"]
}

def updateColorTitle(value)
{    
	if (value.hex != null)
    	sendEvent(name: "color", value: value?.hex)
}

def sendColorValuesToDevice(value)
{
	log.debug "sendColorValuesToDevice: ${value}" 
    
    def command = [
    	"st wattr 0x${device.deviceNetworkId} 0x01 0x0300 0x0034 0x0020 {${zigbee.convertToHexString(value?.red, 2)}}", "delay 200",
    	"st wattr 0x${device.deviceNetworkId} 0x01 0x0300 0x0038 0x0020 {${zigbee.convertToHexString(value?.green, 2)}}", "delay 200",
       	"st wattr 0x${device.deviceNetworkId} 0x01 0x0300 0x003c 0x0020 {${zigbee.convertToHexString(value?.blue, 2)}}"
    ]
    return command
}

def storeColorValues(value)
{
	state.rgbColor.red = value.red
    state.rgbColor.green = value.green
   	state.rgbColor.blue = value.blue
}

def setColor(value) 
{
    updateColorTitle(value)
    
    storeColorValues(value)
    
    sendColorValuesToDevice(value)
    
}

def configure() 
{
	state.rgbColor = [red:255 , green:255 , blue:255]
	reset() 
}

def refresh() {
	log.debug "Executing 'refresh'"
    state.rgbColor = [red:255 , green:255 , blue:255]
}

def reset() 
{
	log.debug "Executing 'reset'"
    
	setColor([red:255, green:255, blue:255, hex:"#ffffff"])
}

/*
def rgbToHSV(red, green, blue) {
	float r = red / 255f
	float g = green / 255f
	float b = blue / 255f
	float max = [r, g, b].max()
	float delta = max - [r, g, b].min()
	def hue = 13
	def saturation = 0
	if (max && delta) {
		saturation = 100 * delta / max
		if (r == max) {
			hue = ((g - b) / delta) * 100 / 6
		} else if (g == max) {
			hue = (2 + (b - r) / delta) * 100 / 6
		} else {
			hue = (4 + (r - g) / delta) * 100 / 6
		}
	}
	[hue: hue, saturation: saturation, value: max * 100]
}

def huesatToRGB(float hue, float sat) {
	while(hue >= 100) hue -= 100
	int h = (int)(hue / 100 * 6)
	float f = hue / 100 * 6 - h
	int p = Math.round(255 * (1 - (sat / 100)))
	int q = Math.round(255 * (1 - (sat / 100) * f))
	int t = Math.round(255 * (1 - (sat / 100) * (1 - f)))
	switch (h) {
		case 0: return [255, t, p]
		case 1: return [q, 255, p]
		case 2: return [p, 255, t]
		case 3: return [p, q, 255]
		case 4: return [t, p, 255]
		case 5: return [255, p, q]
	}
}
*/