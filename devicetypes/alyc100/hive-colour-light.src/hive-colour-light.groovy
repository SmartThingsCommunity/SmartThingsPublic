/**
 *  Hive Active Colour Light V1.0
 *
 *  Copyright 2016 Tom Beech
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 */

metadata {
    definition (name: "Hive Active Colour Light V2.0", namespace: "ibeech", author: "Tom Beech") {
        capability "Polling"
        capability "Switch"
        capability "Switch Level"
        capability "Refresh"        
		capability "Color Control"
		capability "Color Temperature"
		capability "Actuator"
        capability "Sensor"
                
        attribute "colorName", "string"
    }

    simulator {
    }

	tiles (scale: 2){
		multiAttributeTile(name:"switch", type: "lighting", width: 6, height: 4, canChangeIcon: true){
			tileAttribute ("device.switch", key: "PRIMARY_CONTROL") {
				attributeState "on", label:'${name}', action:"switch.off", icon:"http://hosted.lifx.co/smartthings/v1/196xOn.png", backgroundColor:"#79b821", nextState:"turningOff"
				attributeState "off", label:'${name}', action:"switch.on", icon:"http://hosted.lifx.co/smartthings/v1/196xOff.png", backgroundColor:"#ffffff", nextState:"turningOn"
				attributeState "turningOn", label:'${name}', action:"switch.off", icon:"http://hosted.lifx.co/smartthings/v1/196xOn.png", backgroundColor:"#fffA62", nextState:"turningOff"
				attributeState "turningOff", label:'${name}', action:"switch.on", icon:"http://hosted.lifx.co/smartthings/v1/196xOff.png", backgroundColor:"#fffA62", nextState:"turningOn"
			}
			tileAttribute ("device.level", key: "SLIDER_CONTROL") {
				attributeState "level", action:"switch level.setLevel"
			}
			tileAttribute ("device.group", key: "SECONDARY_CONTROL") {
				attributeState "group", label: '${currentValue}'
			}
            tileAttribute ("device.color", key: "COLOR_CONTROL") {
        		attributeState "color", action:"setColor"
    		}
		}          
        
       	standardTile("refresh", "device.switch", width: 2, height: 2, inactiveLabel: false, decoration: "flat") {
            state "default", label:"", action:"refresh.refresh", icon:"st.secondary.refresh"
        }
        
        main(["switch"])
        details(["switch", "refresh"])
    }
}

//parse events into attributes
def parse(value) {
    log.debug "Parsing '${value}' for ${device.deviceNetworkId}"
}

def setColorTemperature(value) {
	log.debug "Executing 'setColorTemperature' to ${value}"
            
    def args = [nodes: [[attributes: [colourTemperature: [targetValue: value], colourTemperatureTransitionTime: [targetValue: "1"], colourMode: [targetValue: "TUNABLE"]]]]]                
    def resp = parent.apiPUT("/nodes/${device.deviceNetworkId}", args)

	if(resp.status == 404) {
		// Bulb has reported it is offline, poll for more details
        poll()
    } else { 
    	sendEvent(name: "colorTemperature", value: value)    	
        sendEvent(name: "hue", value: 0)
    	sendEvent(name: "saturation", value: 0)
    }
}

def setLevel(double value) {
    
    def val = String.format("%.0f", value)
    
    log.debug "Setting level to $val"
    
    def onOff = "ON"
    if(val == 0) {
    	onOff = "OFF"
    }
        
    def args = [nodes:[[attributes:[state:[targetValue:onOff],brightness:[targetValue:val],brightnessTransitionTime:[targetValue:"1"]]]]]    
    def resp = parent.apiPUT("/nodes/${device.deviceNetworkId}", args)
    
	if(resp.status == 404) {
		// Bulb has reported it is offline, poll for more details
        poll()
    } else {    
    	sendEvent(name: 'level', value: val)
    	sendEvent(name: 'switch', value: onOff.toLowerCase())
        log.debug "Level set"
    }
}

def setSaturation(percent) {
	log.debug "setSaturation($percent)"
	setColor(saturation: percent)
}

def setHue(value) {
	log.debug "setHue($value)"
	setColor(hue: value)
}

def setColor(value) {
	def result = []
	log.debug "setColor: ${value}"

    def hue = 0
    def sat = 0
    
    // If a HEX value was passed, just convert it and set the HSV values
	if (value.hex) {
		def c = value.hex.findAll(/[0-9a-fA-F]{2}/).collect { Integer.parseInt(it, 16) }
		
        // Set the desired colours
        def res = rgbToHSV(c[0],c[1],c[2])
                
        hue = res.hue
        sat = res.saturation
        
    // Otherwise, determine which values have been passed and set up the HSV values
	} else {
		hue = value.hue ?: device.currentValue("hue")
		saturation = value.saturation ?: device.currentValue("saturation")
		if(hue == null) hue = 13
		if(saturation == null) saturation = 13        
	}
    
    log.debug "Hue: ${hue}"
    log.debug "Sat: ${sat}"
    
    // SEND HTTP COMMAND TO SET COLOUR
    def args = ["nodes":[["attributes":["hsvHue":["targetValue":hue],"hsvSaturation":["targetValue":sat]/*,"hsvValue":["targetValue":100]*/,"colourMode":["targetValue":"COLOUR"]]]]]
    def resp = parent.apiPUT("/nodes/${device.deviceNetworkId}", args)
    
	if(resp.status == 404) {
		// Bulb has reported it is offline, poll for more details
        poll()
    } else {    
        if(value.hex) sendEvent(name: "color", value: value.hex)
        if(value.hue) sendEvent(name: "hue", value: value.hue)
        if(value.saturation) sendEvent(name: "saturation", value: value.saturation)
        if(value.switch) sendEvent(name: "switch", value: value.switch)
    }
}

def on() {    
    def args = [nodes: [	[attributes: [state: [targetValue: "ON"]]]]]                
    def resp = parent.apiPUT("/nodes/${device.deviceNetworkId}", args)
    
    if(resp.status == 404) {
		// Bulb has reported it is offline, poll for more details
        poll()
    } else { 
    	sendEvent(name: 'switch', value: "on")
    }
}

def off() {

    def args = [nodes: [	[attributes: [state: [targetValue: "OFF"]]]]]                
    def resp = parent.apiPUT("/nodes/${device.deviceNetworkId}", args)
    
    if(resp.status == 404) {
		// Bulb has reported it is offline, poll for more details
        poll()
    } else { 
    	sendEvent(name: 'switch', value: "off")
    }
}

def installed() {
	log.debug "Executing 'installed'"
}

def refresh() {
    poll()
}

def poll() {
	def resp = parent.apiGET("/nodes/${device.deviceNetworkId}")
	if (resp.status != 200) {
		log.error("Unexpected result in poll(): [${resp.status}] ${resp.data}")
		return []
	}
    
	data.nodes = resp.data.nodes

	def state = data.nodes.attributes.state.reportedValue[0] 
	def temperature = data.nodes.attributes.colourTemperature.reportedValue[0]
	def brightness =  data.nodes.attributes.brightness.reportedValue[0]
    def presence = data.nodes.attributes.presence.reportedValue[0]
	def hsvHue = data.nodes.attributes.hsvHue.reportedValue[0]
	def hsvSat = data.nodes.attributes.hsvSaturation.reportedValue[0]
	def hsvValue = data.nodes.attributes.hsvValue.reportedValue[0]

	brightness = String.format("%.0f", brightness)
    temperature = String.format("%.0f", temperature)

	log.debug "State: $state"
    log.debug "Temperature: $temperature"
    log.debug "Brightness: $brightness"
    log.debug "Presence: $presence"
    log.debug "HSV (Hue): $hsvHue"
    log.debug "HSV (Sat): $hsvSat"
    
	if(presence == "ABSENT") {
    	// Bulb is not present (i.e. turned off at the switch or removed)
    	sendEvent(name: 'switch', value: "off")	        
    } else {    	
        sendEvent(name: 'switch', value: state.toLowerCase())
    }
    
    sendEvent(name: 'level', value: brightness)
    sendEvent(name: "hue", value: hsvHue)
    sendEvent(name: "saturation", value: hsvSat)
}

def rgbToHSV(r, g, b) {

	double h, s, v;

    double min, max, delta;

    min = Math.min(Math.min(r, g), b);
    max = Math.max(Math.max(r, g), b);

    // V
    v = max;

     delta = max - min;

    // S
     if( max != 0 )
        s = delta / max;
     else {
        s = 0;
        h = -1;
        return [hue: h, saturation: s * 100, value: v]
     }

    // H
     if( r == max )
        h = ( g - b ) / delta; // between yellow & magenta
     else if( g == max )
        h = 2 + ( b - r ) / delta; // between cyan & yellow
     else
        h = 4 + ( r - g ) / delta; // between magenta & cyan

     h *= 60;    // degrees

    if( h < 0 )
        h += 360;

	[hue: h, saturation: s * 100, value: v]
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
