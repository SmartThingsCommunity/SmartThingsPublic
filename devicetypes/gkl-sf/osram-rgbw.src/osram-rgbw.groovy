/**
 *  Osram Lightify RGBW A19/BR30 US version (HA) DTH rev 01/19/2018
 *
 *  by gkl_sf
 *
 *  set default color/level code by ranga
 *
 *  To set default initial (power-on) color/level:
 *  - set your preferred color/level
 *  - wait for few seconds, then tap the Set Default tile
 *  - wait 3-5 minutes for the process to complete (do NOT switch off or change any settings during this time)
 *  - the main (on/off) tile will turn orange with "WAIT" status during this period; if it does not reset after 3-5 minutes, tap the refresh tile 
 *  - after that, you can try switching power off and on to see if the new color/level is set correctly
 *  - may need to upgrade firmware (via ST OTA) for this to work
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
 *  Parts copyright 2015 SmartThings
 *
**/

metadata {
    definition (name: "Osram RGBW", namespace: "gkl-sf", author: "gkl_sf") {

        capability "Actuator"
        capability "Color Control"
        capability "Color Temperature"
        capability "Configuration"
        capability "Polling"
        capability "Refresh"
        capability "Switch"
        capability "Switch Level"
        
        command "temp2700"
        command "temp3000"
        command "temp6500"
        
        command "setHue360", ["number"]

        command "pulseOn"
        command "pulseOff"
        
        command "blinkOn"
        command "blinkOff"
        
        command "loopOn"
        command "loopOff"
        
        command "setDefaultColor"
	    
	attribute "colorName", "string"
        attribute "colorMode", "string"    
        
        fingerprint profileId: "0104", inClusters: "0000,0003,0004,0005,0006,0008,0300,0B04,FC0F", outClusters: "0019", manufacturer: "OSRAM", model: "LIGHTIFY A19 RGBW", deviceJoinName: "Osram Lightify A19 RGBW"
        fingerprint profileId: "0104", inClusters: "0000,0003,0004,0005,0006,0008,0300,0B04,FC0F", outClusters: "0019", manufacturer: "OSRAM", model: "LIGHTIFY BR RGBW", deviceJoinName: "Osram Lightify LED BR30 RGBW"
    }

    preferences {            
       input(
             "switchTransition",
             "number",
             title: "Dim duration for On/Off",
             range: "0..10",
             description: "0-10 seconds",
             defaultValue: 2,
             required: false,
             displayDuringSetup: true
            )
       input(
             "levelTransition",
             "number",
             title: "Dim duration for level change",
             range: "0..10",
             description: "0-10 seconds",
             defaultValue: 4,
             required: false,
             displayDuringSetup: true
            )
       input(
             "colorTransition",
             "number",
             title: "Time to transition color",
             range: "0..10",
             description: "0-10 seconds",
             defaultValue: 2,
             required: false,
             displayDuringSetup: true
            )             
 	   input(
             "pulseDuration",
             "number",
             title: "Pulse dim up/down duration",
             range: "1..10",
             description: "1-10 seconds",
             defaultValue: 4,
             required: false,
             displayDuringSetup: true
            )
 	   input(
             "loopRate",
             "number",
             title: "Color loop rate in steps per second",
             range: "1..20",
             description: "range 1-25",
             defaultValue: 5,
             required: false,
             displayDuringSetup: true
            )           
    }       
    
    tiles(scale: 2) {
        multiAttributeTile(name:"switch", type: "lighting", width: 6, height: 4, canChangeIcon: true) {
        
            tileAttribute ("device.switch", key: "PRIMARY_CONTROL") {
                attributeState "on", label:'${name}', action:"off", icon:"st.lights.philips.hue-single", backgroundColor:"#79b821", nextState:"turningOff"
                attributeState "off", label:'${name}', action:"on", icon:"st.lights.philips.hue-single", backgroundColor:"#ffffff", nextState:"turningOn"
                attributeState "turningOn", label:'Turning...', action:"on", icon:"st.lights.philips.hue-single", backgroundColor:"#ffffff", nextState:"turningOn"
                attributeState "turningOff", label:'Turning...', action:"off", icon:"st.lights.philips.hue-single", backgroundColor:"#79b821", nextState:"turningOff"
                attributeState "wait", label:'Wait...', icon:"st.lights.philips.hue-single", backgroundColor:"#ffa81e"                
            }           
            tileAttribute ("device.level", key: "SLIDER_CONTROL") {
                attributeState "level", action:"setLevel"
            }
            tileAttribute ("device.color", key: "COLOR_CONTROL") {
                attributeState "color", action:"setColor"
            }
        }
                       
        controlTile("hueSliderControl", "device.hue", "slider", width: 4, height: 2, range: "(0..360)") {
            state "hue", action: "setHue360"
        }
        valueTile("hue", "device.hue", width: 2, height: 2) {
            state "hue", label:'Hue\n${currentValue}°',
            backgroundColors:[
                    [value: 0, color: "ff0000"],
                    [value: 8, color: "ff3800"],
                    [value: 21, color: "ff6700"],
                    [value: 27, color: "ffbf00"],
                    [value: 41, color: "ffff00"],
                    [value: 55, color: "dfff00"],
                    [value: 95, color: "00ff00"],
                    [value: 125, color: "00ff6f"],
                    [value: 137, color: "00ffff"],
                    [value: 165, color: "007fff"],
                    [value: 206, color: "0000ff"],
                    [value: 246, color: "8f00ff"],
                    [value: 276, color: "ff00ff"],
                    [value: 316, color: "ff007f"],
                    [value: 341, color: "ffc0cb"],
                    [value: 351, color: "dc143c"],
                    [value: 357, color: "ff0000"],
                    [value: 360, color: "ff0000"]                    
		    ]               
        }

        valueTile("saturation", "device.saturation", width: 3, height: 1) {
            state "saturation", label:'Saturation ${currentValue}%'
        }        
        valueTile("colorName", "device.colorName", width: 3, height: 1) {
            state "colorName", label:'${currentValue}'  
        }         
        
        
        controlTile("colorTempSliderControl", "device.colorTemperature", "slider", width: 4, height: 2, range:"(2700..6500)") {
            state "colorTemp", action:"color temperature.setColorTemperature"
        }
        valueTile("colorTemp", "device.colorTemperature", width: 2, height: 2) {
            state "colorTemp", label: 'Temp\n${currentValue} K'           
        }

        standardTile("temp2700", "device.temp2700", decoration: "flat", width: 2, height: 2) {
            state "default", label:'Incandescent', action: "temp2700", icon: "st.Lighting.light13"
        }        
        standardTile("temp3000", "device.temp3000", decoration: "flat", width: 2, height: 2) {
            state "default", label:'Halogen', action: "temp3000", icon: "st.lights.philips.hue-single"
        }
        standardTile("temp6500", "device.temp6500", decoration: "flat", width: 2, height: 2) {
            state "default", label:'Daylight', action: "temp6500", icon: "st.Weather.weather14"
        }         
                
        standardTile("pulse", "device.pulse", decoration: "flat", width: 2, height: 2) {
            state "off", label:'Pulse', action: "pulseOn", icon: "st.Lighting.light11", backgroundColor:"#ffffff"
            state "on", label:'Pulse', action: "pulseOff", icon: "st.Lighting.light11", backgroundColor:"#dcdcdc"
        }
        standardTile("blink", "device.blink", decoration: "flat", width: 2, height: 2) {
            state "off", label:'Blink', action: "blinkOn", icon: "st.Lighting.light11", backgroundColor:"#ffffff"
            state "on", label:'Blink', action: "blinkOff", icon: "st.Lighting.light11", backgroundColor:"#dcdcdc"
        }        
        standardTile("colorLoop", "device.colorLoop", decoration: "flat", width: 2, height: 2) {
            state "off", label:'Color Loop', action: "loopOn", icon: "st.Kids.kids2", backgroundColor:"#ffffff"
            state "on", label:'Color Loop', action: "loopOff", icon: "st.Kids.kids2", backgroundColor:"#dcdcdc"
        }
       
        standardTile("refresh", "device.refresh", decoration: "flat", width: 2, height: 2) {
            state "refresh", label:'', action:"refresh.refresh", icon:"st.secondary.refresh"
        }
        
        standardTile("configure", "device.configure", decoration: "flat", height: 2, width: 2) {
			state "configure", label:'', action:"configure", icon:"st.secondary.configure"
		}

        standardTile("defaultColor", "device.defaultColor", decoration: "flat", width: 2, height: 2) {
            state "default", label:'Set Default', action: "setDefaultColor", icon: "st.Lighting.light13"
        }        
        
        main(["switch"])
        details(["switch", "hueSliderControl", "hue", "saturation", "colorName", "colorTempSliderControl", "colorTemp", "temp2700", "temp3000", "temp6500", "pulse", "blink", "colorLoop", "refresh", "configure", "defaultColor"])
    }
}

private getON_OFF_CLUSTER() { 6 }
private getLEVEL_CONTROL_CLUSTER() { 8 }
private getCOLOR_CONTROL_CLUSTER() { 0x0300 }

private getHUE_COMMAND() { 0 }
private getSATURATION_COMMAND() { 3 }
private getHUE_SATURATION_COMMAND() { 6 }

private getATTRIBUTE_HUE() { 0 }
private getATTRIBUTE_SATURATION() { 1 }
private getATTRIBUTE_COLOR_TEMPERATURE() { 7 }
private getATTRIBUTE_COLOR_MODE() { 8 }

private getDEFAULT_LEVEL_TRANSITION() {"2800"} //4 secs (little endian)
private getDEFAULT_COLOR_TRANSITION() {"1400"} //2 secs (little endian)
private getDEFAULT_PULSE_DURATION() {"2800"} //4 secs (little endian)
private getDEFAULT_LOOP_RATE() {"05"} //5 steps per sec


def parse(String description) {
    
    def result = zigbee.getEvent(description)
    def cmds = []
    
    if (result) {
        cmds << createEvent(result)
        
        if (device.currentValue("pulse") == "on" && result.name == "level") {
            if (!state.pulseDuration) state.pulseDuration = DEFAULT_PULSE_DURATION
            if (result.value == 5) cmds << new physicalgraph.device.HubAction("st cmd 0x${device.deviceNetworkId} ${endpointId} 8 4 {fb ${state.pulseDuration}}")
            else if (result.value == 99) cmds << new physicalgraph.device.HubAction("st cmd 0x${device.deviceNetworkId} ${endpointId} 8 4 {0d ${state.pulseDuration}}")            
        }
        else if (result.name == "colorTemperature") {
            if (device.currentValue("colorMode") == "W") {
                def tempName = getTempName(result.value)
                cmds << createEvent(name: "colorName", value: tempName, displayed: false)
            }    
        }
    }        
    else {
        def zigbeeMap = zigbee.parseDescriptionAsMap(description)
        if (zigbeeMap?.clusterInt == COLOR_CONTROL_CLUSTER && device.currentValue("switch") == "on") {        
            if (zigbeeMap.attrInt == ATTRIBUTE_HUE) {
                def hueValue = Math.round(zigbee.convertHexToInt(zigbeeMap.value) / 254 * 360)
                def colorName = getColorName(hueValue)
                cmds << createEvent(name: "hue", value: hueValue, displayed: false)
                cmds << createEvent(name: "colorName", value: colorName, displayed: false)
            }            
            else if (zigbeeMap.attrInt == ATTRIBUTE_SATURATION) {
                def saturationValue = Math.round(zigbee.convertHexToInt(zigbeeMap.value) / 254 * 100)
                cmds << createEvent(name: "saturation", value: saturationValue, displayed: false)
            }
            else if (zigbeeMap.attrInt == ATTRIBUTE_COLOR_MODE) {
                if (zigbeeMap.value == "00") {
                    cmds << createEvent(name: "colorMode", value: "RGB", displayed: false)
                }
                else if (zigbeeMap.value == "02") {
                    cmds << createEvent(name: "colorMode", value: "W", displayed: false)
                }
            }               
        }
        else if (zigbeeMap?.clusterInt == 0x8021) {
            log.debug "*** received Configure Reporting response: ${zigbeeMap.data}"
        }
        else { log.debug "*** unparsed response: ${zigbeeMap}" }
    }
    
    return cmds
}

def updated() {

    if (state.updatedTime) {
        if ((state.updatedTime + 5000) > now()) return null
    }
    state.updatedTime = now()

    log.debug "--- Updated with: ${settings}"

    String switchTransition
    if (settings.switchTransition) {
        switchTransition = hex((settings.switchTransition * 10),4) //OnOffTransitionTime in 1/10th sec (big endian)
    }
    else {
        switchTransition = "0014" //2 seconds (big endian)
    }    
    
    if (settings.levelTransition) {
        state.levelTransition = swapEndianHex(hex((settings.levelTransition * 10),4))
    }
    else {
        state.levelTransition = "2800" //4 seconds
    }    
    
    if (settings.colorTransition) {
        state.colorTransition = swapEndianHex(hex((settings.colorTransition * 10),4))
    }
    else {
        state.colorTransition = "1400" //2 seconds
    }

    if (settings.pulseDuration) {
        state.pulseDuration = swapEndianHex(hex((settings.pulseDuration * 10),4))
    }
    else {
        state.pulseDuration = "2800" //4 seconds
    }    
    
    if (settings.loopRate) {
        state.loopRate = hex((settings.loopRate),2)
    }
    else {
        state.loopRate = "05"
    }
    
    return new physicalgraph.device.HubAction("st wattr 0x${device.deviceNetworkId} ${endpointId} 8 0x0010 0x21 {${switchTransition}}")  // on/off dim duration  
}


def refresh() {
    [
        "st rattr 0x${device.deviceNetworkId} ${endpointId} 6 0", "delay 500", //on-off
        "st rattr 0x${device.deviceNetworkId} ${endpointId} 8 0", "delay 500", //level
        "st rattr 0x${device.deviceNetworkId} ${endpointId} 0x0300 0", "delay 500", //hue
        "st rattr 0x${device.deviceNetworkId} ${endpointId} 0x0300 1", "delay 500", //sat
        "st rattr 0x${device.deviceNetworkId} ${endpointId} 0x0300 7", "delay 500", //color temp
        "st rattr 0x${device.deviceNetworkId} ${endpointId} 0x0300 8" //color mode
    ]
}

def configure() {    
    zigbee.onOffConfig() +
    zigbee.levelConfig() +
    zigbee.colorTemperatureConfig() +
	[        
        //hue
        "zcl global send-me-a-report 0x0300 0 0x20 1 3600 {01}", "delay 500",
        "send 0x${device.deviceNetworkId} ${endpointId} 1", "delay 1000",

        //saturation
        "zcl global send-me-a-report 0x0300 1 0x20 1 3600 {01}", "delay 500",
        "send 0x${device.deviceNetworkId} ${endpointId} 1", "delay 1500",

        //color mode
        "zcl global send-me-a-report 0x0300 8 0x30 1 3600 {}", "delay 500",
        "send 0x${device.deviceNetworkId} ${endpointId} 1", "delay 1500",        
        
        "zdo bind 0x${device.deviceNetworkId} ${endpointId} 1 0x0300 {${device.zigbeeId}} {}", "delay 500"
	] +
    zigbee.writeAttribute(LEVEL_CONTROL_CLUSTER, 0x0010, 0x21, "0014") //OnOffTransitionTime in 1/10th sec, set to 2 sec, note big endian
}

def on() {
    zigbee.on()
}

def off() {
    pulseOff()
    zigbee.off()
}

def setColorTemperature(value) {
    def cmds = []
    cmds << sendEvent(name: "colorMode", value: "W", displayed: false)    
    cmds << zigbee.setColorTemperature(value)
    cmds
}

def setLevel(value, duration = settings.levelTransition) { //duration in seconds
	if (value == 0) off()
    else zigbee.setLevel(value,duration)
}

def setColor(value) {
    if (!state.colorTransition) state.colorTransition = DEFAULT_COLOR_TRANSITION
    def cmds = []
    if (device.currentValue("switch") == "off") {
        cmds << zigbee.on()
        cmds << "delay 500"
    }
    def scaledHueValue = zigbee.convertToHexString(Math.round(value.hue * 0xfe / 100), 2)
    def scaledSatValue = zigbee.convertToHexString(Math.round(value.saturation * 0xfe / 100), 2)
    cmds << sendEvent(name: "saturation", value: value.saturation, displayed: false)
    cmds << sendEvent(name: "colorMode", value: "RGB", displayed: false)
    cmds << zigbee.command(COLOR_CONTROL_CLUSTER, HUE_SATURATION_COMMAND, scaledHueValue, scaledSatValue, state.colorTransition)
    cmds    
}

def setHue(value) { //accepts hue values 0-100, doesn't change saturation
    def cmds = []
    if (!state.colorTransition) state.colorTransition = DEFAULT_COLOR_TRANSITION    
    def scaledHueValue = zigbee.convertToHexString(Math.round(value * 0xfe / 100), 2)
    cmds << sendEvent(name: "colorMode", value: "RGB", displayed: false)
    cmds << zigbee.command(COLOR_CONTROL_CLUSTER, HUE_COMMAND, scaledHueValue, "00", state.colorTransition)
    cmds
}

def setHue360(value) { //accepts hue values 0-360 and sets saturation to 100%
    if (!state.colorTransition) state.colorTransition = DEFAULT_COLOR_TRANSITION    
    def scaledHueValue = zigbee.convertToHexString(Math.round(value * 0xfe / 360), 2)
    def cmds = []
    cmds << sendEvent(name: "saturation", value: 100, displayed: false)
    cmds << sendEvent(name: "colorMode", value: "RGB", displayed: false)
    cmds << zigbee.command(COLOR_CONTROL_CLUSTER, HUE_SATURATION_COMMAND, scaledHueValue, "fe", state.colorTransition)
    cmds
}

def setSaturation(value) {
    if (!state.colorTransition) state.colorTransition = DEFAULT_COLOR_TRANSITION    
    def scaledSatValue = zigbee.convertToHexString(Math.round(value * 0xfe / 100), 2)
    def cmds = []
    cmds << sendEvent(name: "saturation", value: value, displayed: false)
    cmds << sendEvent(name: "colorMode", value: "RGB", displayed: false)
    cmds << zigbee.command(COLOR_CONTROL_CLUSTER, SATURATION_COMMAND, scaledSatValue, state.colorTransition)
    cmds
}

def loopOn() {
    if (!state.loopRate) state.loopRate = DEFAULT_LOOP_RATE    
    def cmds = []
    cmds << zigbee.command(COLOR_CONTROL_CLUSTER, SATURATION_COMMAND, "fe", "1400") //set saturation to 100% over 2 sec    
    cmds << sendEvent(name: "colorLoop", value: "on", descriptionText: "Color Loop started", displayed: true, isChange: true)
    cmds << sendEvent(name: "colorMode", value: "RGB", displayed: false)
    cmds << zigbee.command(COLOR_CONTROL_CLUSTER, 0x01, "01", state.loopRate) //move hue command is 0x01, up is "01", rate is steps per sec
    cmds
}

def loopOff() {
    def cmds = []
    cmds << sendEvent(name: "colorLoop", value: "off", descriptionText: "Color Loop stopped", displayed: true, isChange: true)
    cmds << zigbee.command(COLOR_CONTROL_CLUSTER, 0x01, "00") //move hue command is 0x01, stop is "00"
    cmds
}

def pulseOn() {
    def cmds = []
    cmds << sendEvent(name: "pulse", value: "on", descriptionText: "Pulse mode set to On", displayed: true, isChange: true)
    cmds << zigbee.setLevel(95,0) //in case the level is already 99, since level needs to change to initiate the pulse cycling
    cmds << "delay 100"
    cmds << zigbee.setLevel(99,0)
    cmds
}

def pulseOff() {
    sendEvent(name: "pulse", value: "off", descriptionText: "Pulse mode set to Off", displayed: true, isChange: true)
}

def blinkOn() {
    def cmds = []
    cmds << sendEvent(name: "blink", value: "on", descriptionText: "Blink mode set to On", displayed: true, isChange: true)    
    cmds << zigbee.command(3, 0x00, "100e") //payload is time in secs to continue blinking (set to 3600 secs)
    cmds
}

def blinkOff() {
    def cmds = []
    cmds << sendEvent(name: "blink", value: "off", descriptionText: "Blink mode set to Off", displayed: true, isChange: true)  
    cmds << zigbee.command(3, 0x00, "0000")
    cmds
}

def temp2700() {
    setColorTemperature(2700)
}

def temp3000() {
    setColorTemperature(3000)
}

def temp6500() {
    setColorTemperature(6500)
}

def setDefaultColor() {
	log.info "Setting default color"
    def cmds = 
    [
    sendEvent(name: "switch", value: "wait", descriptionText: "Setting default color/level", displayed: true, isChange: true),
    "st cmd 0x${device.deviceNetworkId} ${endpointId} 0xFC0F 0x01 {}",
    "delay 180000"
    ]
    return cmds + refresh()
}

private getEndpointId() {
	new BigInteger(device.endpointId, 16).toString()
}

private hex(value, width=2) {
	def result = new BigInteger(Math.round(value).toString()).toString(16)
	while (result.size() < width) {
		result = "0" + result
	}
	return result
}

private String swapEndianHex(String hex) {
	reverseArray(hex.decodeHex()).encodeHex()
}

private byte[] reverseArray(byte[] array) {
    byte tmp;
    tmp = array[1];
    array[1] = array[0];
    array[0] = tmp;
    return array
}

private getTempName(value) {
    String tempName
    if (value < 3000) tempName = "Incandescent"
    else if (value < 3300) tempName = "Halogen"
    else if (value < 5000) tempName = "Cool White"
    else if (value <= 6500) tempName = "Daylight"
    else tempName = "White Light Mode"
    return tempName
}

//color name for saturation 100%
private getColorName(hueValue) {
    String colorName
    if (hueValue >= 0 && hueValue <= 7) colorName = "Red"
    else if (hueValue >= 8 && hueValue <= 20) colorName = "Red-Orange"
    else if (hueValue >= 21 && hueValue <= 26) colorName = "Orange"
    else if (hueValue >= 27 && hueValue <= 40) colorName = "Orange-Yellow"
    else if (hueValue >= 41 && hueValue <= 54) colorName = "Yellow"
    else if (hueValue >= 55 && hueValue <= 94) colorName = "Yellow-Green"
    else if (hueValue >= 95 && hueValue <= 124) colorName = "Green"
    else if (hueValue >= 125 && hueValue <= 136) colorName = "Green-Cyan"
    else if (hueValue >= 137 && hueValue <= 164) colorName = "Cyan"
    else if (hueValue >= 165 && hueValue <= 205) colorName = "Cyan-Blue"
    else if (hueValue >= 206 && hueValue <= 243) colorName = "Blue"
    else if (hueValue >= 244 && hueValue <= 272) colorName = "Blue-Magenta"
    else if (hueValue >= 273 && hueValue <= 320) colorName = "Magenta"
    else if (hueValue >= 321 && hueValue <= 349) colorName = "Magenta-Pink"
    else if (hueValue == 350) colorName = "Pink"
    else if (hueValue >= 351 && hueValue <= 356) colorName = "Pink-Red"
    else if (hueValue >= 357 && hueValue <= 360) colorName = "Red"
    else colorName = "Color Mode"
    return colorName
}