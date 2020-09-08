/**
 *  Osram Lightify RGBW A19/BR30 US version (HA) DTH rev 01/19/2018
 *
 *  by Davin Dameron
 *
 *  based on work by gkl_sf and SmartThings
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
import groovy.transform.Field

@Field final Map      BLACK = [name: "Black", rgb: "#000000", h: 0, s: 0, l: 0]

@Field final IntRange PERCENT_RANGE = (0..100)

@Field final IntRange HUE_RANGE = PERCENT_RANGE
@Field final Integer  HUE_STEP = 5
@Field final IntRange SAT_RANGE = PERCENT_RANGE
@Field final Integer  SAT_STEP = 20
@Field final Integer  HUE_SCALE = 1000
@Field final Integer  COLOR_OFFSET = HUE_RANGE.getTo() * HUE_SCALE

@Field final IntRange COLOR_TEMP_RANGE = (2200..7000)
@Field final Integer  COLOR_TEMP_DEFAULT = COLOR_TEMP_RANGE.getFrom() + ((COLOR_TEMP_RANGE.getTo() - COLOR_TEMP_RANGE.getFrom())/2)
@Field final Integer  COLOR_TEMP_STEP = 50 // Kelvin
@Field final List     COLOR_TEMP_EXTRAS = []
@Field final List     COLOR_TEMP_LIST = buildColorTempList(COLOR_TEMP_RANGE, COLOR_TEMP_STEP, COLOR_TEMP_EXTRAS)

@Field final Map MODE = [
    COLOR:	"Color",
    WHITE:	"White",
    OFF: 	"Off"
]
metadata {
    definition (name: "Osram RGBW Bulb", namespace: "davindameron", author: "Davin Dameron", ocfDeviceType: "oic.d.light", mnmn:"SmartThings", vid:"generic-rgbw-color-bulb") {

        capability "Color Control"
        capability "Color Temperature"
        capability "Configuration"
        capability "Polling"
        capability "Refresh"
        capability "Switch"
        capability "Switch Level"
        

        command "pulseOn"
        command "pulseOff"
        
        command "blinkOn"
        command "blinkOn10"
        command "blinkOn85"
        command "blinkOff"
        
        command "loopOn"
        command "loopOff"
        
        command "setDefaultColor"
        
        command "setColorNoOn"
        
        command "setLoopRate", ["number"]
        
        command "setColorTransition", ["number"]

        
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
	input(name: "debugLogging", type: "boolean", title: "Turn on debug logging?", displayDuringSetup:true, required: false)

    }       
    
    tiles(scale: 2) {
    
        	standardTile("switch", "device.switch", decoration: "flat", width: 3, height: 3, canChangeIcon: true) {
	    state "off", label:'${name}', action: "switch.on", icon: "st.Lighting.light11", backgroundColor:"#ffffff"
	    state "on", label:'${name}', action: "switch.off", icon: "st.Lighting.light11", backgroundColor:"#00a0dc"
	}        
        standardTile("refresh", "device.refresh", decoration: "flat", width: 3, height: 3) {
            state "refresh", label:'', action:"refresh.refresh", icon:"st.secondary.refresh"
        }
	controlTile("rgbSelector", "device.color", "color", height: 3, width: 2,
	            inactiveLabel: false) {
	    state "color", action: "color control.setColor", label:'Ring Color'
	}

	controlTile("levelSliderControl", "device.level", "slider",
            height: 3, width: 2) {
    	state "level", action:"switch level.setLevel", label:'Ring Level'
	}

        controlTile("colorTempSliderControl", "device.colorTemperature", "slider", width: 2, height: 3, inactiveLabel: false, range:"(2700..6500)") {
            state "colorTemperature", action:"color temperature.setColorTemperature"
        }
        standardTile("colorLoop", "device.colorLoop", decoration: "flat", width: 2, height: 2) {
            state "off", label:'Color Loop', action: "loopOn", icon: "st.Kids.kids2", backgroundColor:"#ffffff"
            state "on", label:'Color Loop', action: "loopOff", icon: "st.Kids.kids2", backgroundColor:"#dcdcdc"
        }
//        standardTile("pulse", "device.pulse", decoration: "flat", width: 2, height: 2) {
//            state "off", label:'Pulse', action: "pulseOn", icon: "st.Lighting.light11", backgroundColor:"#ffffff"
//            state "on", label:'Pulse', action: "pulseOff", icon: "st.Lighting.light11", backgroundColor:"#dcdcdc"
//        }
//        standardTile("blink", "device.blink", decoration: "flat", width: 2, height: 2) {
//            state "off", label:'Blink', action: "blinkOn", icon: "st.Lighting.light11", backgroundColor:"#ffffff"
//            state "on", label:'Blink', action: "blinkOff", icon: "st.Lighting.light11", backgroundColor:"#dcdcdc"
//        }        
       
        
        standardTile("configure", "device.configure", decoration: "flat", height: 2, width: 2) {
			state "configure", label:'', action:"configure", icon:"st.secondary.configure"
		}

        standardTile("defaultColor", "device.defaultColor", decoration: "flat", width: 2, height: 2) {
            state "default", label:'Set Default', action: "setDefaultColor", icon: "st.Lighting.light13"
        }        
        
        main(["switch"])
        details(["switch", "refresh", "rgbSelector", "levelSliderControl", "colorTempSliderControl", "colorLoop", "configure", "defaultColor"])
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

private getMOVE_TO_HUE_AND_SATURATION_COMMAND() { 0x06 }

def doLogging(value){
	def debugLogging = debugLogging ?: settings?.debugLogging ?: device.latestValue("debugLogging");
	if (debugLogging=="true")
	{
		log.debug value;
	}
}

def parse(String description) {
     doLogging("parse($description)");
     doLogging("current hue:  ${device.currentValue("hue")}");
     doLogging("current saturation:  ${device.currentValue("saturation")}");
   
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
        //if (zigbeeMap?.clusterInt == COLOR_CONTROL_CLUSTER && device.currentValue("switch") == "on") {        
        if (zigbeeMap?.clusterInt == COLOR_CONTROL_CLUSTER) {        
            if (zigbeeMap.attrInt == ATTRIBUTE_HUE) {
                Integer hueValue = Math.round(zigbee.convertHexToInt(zigbeeMap.value) / 254 * 100)
                cmds << createEvent(name: "hue", value: hueValue, displayed: false)
                Integer boundedHue = boundInt(hueValue, PERCENT_RANGE)
                Integer boundedSaturation = boundInt(device.currentValue("saturation"), PERCENT_RANGE)
				String rgbHex = colorUtil.hsvToHex(boundedHue, boundedSaturation)
                cmds << createEvent(name: "color", value: rgbHex, displayed: false)
            }            
            else if (zigbeeMap.attrInt == ATTRIBUTE_SATURATION) {
                Integer saturationValue = Math.round(zigbee.convertHexToInt(zigbeeMap.value) / 254 * 100)
                cmds << createEvent(name: "saturation", value: saturationValue, displayed: false)
                Integer boundedHue = boundInt(device.currentValue("hue"), PERCENT_RANGE)
                Integer boundedSaturation = boundInt(saturationValue, PERCENT_RANGE)
				String rgbHex = colorUtil.hsvToHex(boundedHue, boundedSaturation)
                cmds << createEvent(name: "color", value: rgbHex, displayed: false)
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
            doLogging("*** received Configure Reporting response: ${zigbeeMap.data}");
        }
        else { doLogging("*** unparsed response: ${zigbeeMap}") }
    }
    
    return cmds
}

def updated() {

    if (state.updatedTime) {
        if ((state.updatedTime + 5000) > now()) return null
    }
    state.updatedTime = now()

    doLogging("--- Updated with: ${settings}");

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

def setLoopRate(def nValue) {
	settings.loopRate = nValue
    state.loopRate = hex((settings.loopRate),2)
}

def setColorTransition(def nValue) {
	settings.colorTransition = nValue
    state.colorTransition = swapEndianHex(hex((settings.colorTransition * 10),4))
    
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

def setLevel(value, duration = settings.levelTransition) { //duration in seconds
	if (value == 0) off()
    else zigbee.setLevel(value,duration)
}

def setColorTemperature(value) {
    doLogging("setColorTemperature($value)");
    value = value as Integer
    if (value < 2700)
    {
    	value = 2700
    }
    if (value > 6500)
    {
    	value = 6500
    }
    def tempInMired = Math.round(1000000 / value)
    def finalHex = zigbee.swapEndianHex(zigbee.convertToHexString(tempInMired, 4))

    zigbee.command(COLOR_CONTROL_CLUSTER, 0x0A, "$finalHex 0000") +
    zigbee.command(ON_OFF_CLUSTER, 0x01) +
    zigbee.readAttribute(COLOR_CONTROL_CLUSTER, ATTRIBUTE_COLOR_TEMPERATURE)
}

private getScaledHue(value) {
    zigbee.convertToHexString(Math.round(value * 0xfe / 100.0), 2)
}

private getScaledSaturation(value) {
    zigbee.convertToHexString(Math.round(value * 0xfe / 100.0), 2)
}

private Map buildColorHSMap(hue, saturation) {
	doLogging("Executing 'buildColorHSMap(${hue}, ${saturation})'");
    Map colorHSMap = [hue: 0, saturation: 0]
    try {
        colorHSMap.hue = hue.toFloat().toInteger()
        colorHSMap.saturation = saturation.toFloat().toInteger()
    } catch (NumberFormatException nfe) {
        doLogging("Couldn't transform one of hue ($hue) or saturation ($saturation) to integers: $nfe");
    }
    doLogging(colorHSMap);
    return colorHSMap
}

private List buildColorTempList(IntRange kRange, Integer kStep, List kExtras) {
    List colorTempList = [kRange.getFrom()] // start with range lower bound
    Integer kFirstNorm = kRange.getFrom() + kStep - (kRange.getFrom() % kStep) // find the first value within thr range which is a factor of kStep
    colorTempList += (kFirstNorm..kRange.getTo()).step(kStep) // now build the periodic list
    colorTempList << kRange.getTo() // include range upper bound
    colorTempList += kExtras // add in extra values
    return colorTempList.sort().unique() // sort and de-dupe
}

private Integer boundInt(Double value, IntRange theRange) {
    value = Math.max(theRange.getFrom(), value)
    value = Math.min(theRange.getTo(), value)
    return value.toInteger()
}

def setSaturation(saturationPercent) {
    doLogging("Executing 'setSaturation' ${saturationPercent}/100");
    Integer currentHue = device.currentValue("hue")
    setColor(currentHue, saturationPercent)
}

def setHue(huePercent) {
    doLogging("Executing 'setHue' ${huePercent}/100");
    Integer currentSaturation = device.currentValue("saturation")
    setColor(huePercent, currentSaturation)
}

def setColor(Integer huePercent, Integer saturationPercent) {
    doLogging("Executing 'setColor' from separate values hue: $huePercent, saturation: $saturationPercent");
    setColor(buildColorHSMap(huePercent, saturationPercent)) // call the capability version method overload
}

def setColor(String rgbHex) {
    doLogging("Executing 'setColor' from hex $rgbHex");
    if (hex == "#000000") {
        off()
    } else {
        List hsvList = colorUtil.hexToHsv(rgbHex)
        Map colorHSMap = buildColorHSMap(hsvList[0], hsvList[1])
        setColor(colorHSMap) // call the capability version method overload
    }
}

def setColor(Map value) {
    doLogging("setColor($value)");
    
    zigbee.on() +
    zigbee.command(COLOR_CONTROL_CLUSTER, MOVE_TO_HUE_AND_SATURATION_COMMAND,
    getScaledHue(value.hue), getScaledSaturation(value.saturation), "0000") +
    zigbee.readAttribute(COLOR_CONTROL_CLUSTER, ATTRIBUTE_SATURATION) +
    zigbee.readAttribute(COLOR_CONTROL_CLUSTER, ATTRIBUTE_HUE)
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

def blinkOn10() {
    def cmds = []
    //cmds << sendEvent(name: "blink", value: "on", descriptionText: "Blink mode set to On", displayed: true, isChange: true)    
    cmds << zigbee.command(3, 0x00, "0a00") //payload is time in secs to continue blinking (set to 3600 secs)
    cmds
}

def blinkOn85() {
    def cmds = []
    //cmds << sendEvent(name: "blink", value: "on", descriptionText: "Blink mode set to On", displayed: true, isChange: true)    
    cmds << zigbee.command(3, 0x00, "5500") //payload is time in secs to continue blinking (set to 3600 secs)
    cmds
}

def blinkOff() {
    def cmds = []
    cmds << sendEvent(name: "blink", value: "off", descriptionText: "Blink mode set to Off", displayed: true, isChange: true)  
    cmds << zigbee.command(3, 0x00, "0000")
    cmds
}

def setDefaultColor() {
	doLogging("Setting default color");
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