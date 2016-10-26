/**
 *  Copyright 2016 Eric Maycock
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
 *  Aeon RGBW Bulb (Advanced)
 *
 *  Author: Eric Maycock (erocm123)
 *  Date: 2016-10-25
 */
 
metadata {
	definition (name: "Aeon RGBW Bulb (Advanced)", namespace: "erocm123", author: "Eric Maycock") {
		capability "Switch Level"
		capability "Color Control"
		capability "Color Temperature"
		capability "Switch"
		capability "Refresh"
		capability "Actuator"
		capability "Sensor"
 
		command "reset"
		command "refresh"
        command "rainbow"
        
        (1..6).each { n ->
			attribute "switch$n", "enum", ["on", "off"]
			command "on$n"
			command "off$n"
		}

        fingerprint deviceId: "0x1101", inClusters: "0x5E, 0x26, 0x33, 0x27, 0x2C, 0x2B, 0x70, 0x59, 0x85, 0x72, 0x86, 0x7A, 0x73, 0xEF, 0x5A, 0x82"

	}
    
    preferences {
        input "transition", "enum", title: "Transition", defaultValue: 0, displayDuringSetup: false, required: false, options: [
                0:"Smooth",
                1073741824:"Fade"]
        input "brightness", "enum", title: "Brightness", defaultValue: 1, displayDuringSetup: false, required: false, options: [
                1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25,26,27,28,29,30,31,32,33,34,35,36,37,38,39,40,
                41,42,43,44,45,46,47,48,49,50,51,52,53,54,55,56,57,58,59,60,61,62,63,64,65,66,67,68,69,70,71,72,73,74,75,76,77,
                78,79,80,81,82,83,84,85,86,87,88,89,90,91,92,93,94,95,96,97,98,99]
        input "count", "enum", title: "Cycle Count (0 [unlimited])", defaultValue: 0, displayDuringSetup: false, required: false, options: [
                0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25,26,27,28,29,30,31,32,33,34,35,36,37,38,39,40,
                41,42,43,44,45,46,47,48,49,50,51,52,53,54,55,56,57,58,59,60,61,62,63,64,65,66,67,68,69,70,71,72,73,74,75,76,77,
                78,79,80,81,82,83,84,85,86,87,88,89,90,91,92,93,94,95,96,97,98,99,100,101,102,103,104,105,106,107,108,109,110,
                111,112,113,114,115,116,117,118,119,120,121,122,123,124,125,126,127,128,129,130,131,132,133,134,135,136,137,138,
                139,140,141,142,143,144,145,146,147,148,149,150,151,152,153,154,155,156,157,158,159,160,161,162,163,164,165,166,
                167,168,169,170,171,172,173,174,175,176,177,178,179,180,181,182,183,184,185,186,187,188,189,190,191,192,193,194,
                195,196,197,198,199,200,201,202,203,204,205,206,207,208,209,210,211,212,213,214,215,216,217,218,219,220,221,222,
                223,224,225,226,227,228,229,230,231,232,233,234,235,236,237,238,239,240,241,242,243,244,245,246,247,248,249,250,
                251,252,253,254]
        input "speed", "enum", title: "Color Change Speed", defaultValue: 0, displayDuringSetup: false, required: false, options: [
                0:"Slow",
                32:"Medium",
                64:"Fast"]
        input "speedLevel", "enum", title: "Color Change Speed Level (0 [constant], 1 [slowest], 30 [fastest])", defaultValue: "0", displayDuringSetup: true, required: false, options: [
                0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25,26,27,28,29,30]
        (1..8).each { i ->
        input "color$i", "enum", title: "Color $i", displayDuringSetup: false, required: false, options: [
                1:"Red",
                2:"Orange",
                3:"Yellow",
                4:"Green",
                5:"Cyan",
                6:"Blue",
                7:"Violet",
                8:"Pink"]
         }
    }
 
	simulator {
	}
 
	tiles (scale: 2){      
		multiAttributeTile(name:"switch", type: "lighting", width: 6, height: 4, canChangeIcon: true){
			tileAttribute ("device.switch", key: "PRIMARY_CONTROL") {
				attributeState "on", label:'${name}', action:"switch.off", icon:"st.lights.philips.hue-single", backgroundColor:"#79b821", nextState:"turningOff"
				attributeState "off", label:'${name}', action:"switch.on", icon:"st.lights.philips.hue-single", backgroundColor:"#ffffff", nextState:"turningOn"
				attributeState "turningOn", label:'${name}', action:"switch.off", icon:"st.lights.philips.hue-single", backgroundColor:"#79b821", nextState:"turningOff"
				attributeState "turningOff", label:'${name}', action:"switch.on", icon:"st.lights.philips.hue-single", backgroundColor:"#ffffff", nextState:"turningOn"
			}
			tileAttribute ("device.level", key: "SLIDER_CONTROL") {
				attributeState "level", action:"switch level.setLevel"
			}
			tileAttribute ("device.color", key: "COLOR_CONTROL") {
				attributeState "color", action:"setColor"
			}
        }

		standardTile("refresh", "device.switch", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
			state "default", label:"", action:"refresh.refresh", icon:"st.secondary.refresh"
		}
        standardTile("configure", "device.configure", inactiveLabel: false, width: 2, height: 2, decoration: "flat") {
			state "configure", label:'', action:"configuration.configure", icon:"st.secondary.configure"
		}
        valueTile("colorTempTile", "device.colorTemperature", decoration: "flat", height: 2, width: 2) {
        	state "colorTemperature", label:'${currentValue}%', backgroundColor:"#FFFFFF"
        }
        controlTile("colorTempControl", "device.colorTemperature", "slider", decoration: "flat", height: 2, width: 4, inactiveLabel: false) {
		    state "colorTemperature", action:"setColorTemperature"
	    }
		standardTile("switch1", "switch1", canChangeIcon: true, width: 2, height: 2) {
            state "off", label: "fade", action: "on1", icon: "st.switches.switch.off", backgroundColor: "#ffffff"
			state "on", label: "fade", action: "off1", icon: "st.switches.switch.on", backgroundColor: "#79b821"
		}
        standardTile("switch2", "switch2", canChangeIcon: true, width: 2, height: 2) {
            state "off", label: "flash", action: "on2", icon: "st.switches.switch.off", backgroundColor: "#ffffff"
			state "on", label: "flash", action: "off2", icon: "st.switches.switch.on", backgroundColor: "#79b821"
		}
        standardTile("switch3", "switch3", canChangeIcon: true, width: 2, height: 2) {
            state "off", label: "random", action: "on3", icon: "st.switches.switch.off", backgroundColor: "#ffffff"
			state "on", label: "random", action: "off3", icon: "st.switches.switch.on", backgroundColor: "#79b821"
		}
        standardTile("switch4", "switch4", canChangeIcon: true, width: 2, height: 2) {
            state "off", label: "police", action: "on4", icon: "st.switches.switch.off", backgroundColor: "#ffffff"
			state "on", label: "police", action: "off4", icon: "st.switches.switch.on", backgroundColor: "#79b821"
		}
        standardTile("switch5", "switch5", canChangeIcon: true, width: 2, height: 2) {
			state "off", label: "$n", action: "on5", icon: "st.switches.switch.off", backgroundColor: "#ffffff"
            state "on", label: "$n", action: "off5", icon: "st.switches.switch.on", backgroundColor: "#79b821"
		}
        standardTile("switch6", "switch6", canChangeIcon: true, width: 2, height: 2) {
            state "off", label: "custom", action: "on6", icon: "st.switches.switch.off", backgroundColor: "#ffffff", nextState:"on"
			state "on", label: "custom", action: "off6", icon: "st.switches.switch.on", backgroundColor: "#79b821", nextState:"off"
		}
    }

	main(["switch"])
	details(["switch", "levelSliderControl",
             "colorTempControl", "colorTempTile",
             "switch1", "switch2", "switch3",
             "switch4", /*"switch5",*/ "switch6",
             "refresh", "configure" ])

}
 
def updated() {
    log.debug calculateParameter(37)
    log.debug calculateParameter(38)
	response(refresh())
}
 
def parse(description) {
	def result = null
	if (description != "updated") {
		def cmd = zwave.parse(description, [0x20: 1, 0x26: 3, 0x70: 1, 0x33:3])
		if (cmd) {
			result = zwaveEvent(cmd)
			log.debug("'$description' parsed to $result")
		} else {
			log.debug("Couldn't zwave.parse '$description'")
		}
	}
	result
}
 
def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicReport cmd) {
	dimmerEvents(cmd)
}
 
def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicSet cmd) {
	dimmerEvents(cmd)
}
 
def zwaveEvent(physicalgraph.zwave.commands.switchmultilevelv3.SwitchMultilevelReport cmd) {
	dimmerEvents(cmd)
}
 
private dimmerEvents(physicalgraph.zwave.Command cmd) {
	def value = (cmd.value ? "on" : "off")
	def result = [createEvent(name: "switch", value: value, descriptionText: "$device.displayName was turned $value")]
	if (cmd.value) {
		result << createEvent(name: "level", value: cmd.value, unit: "%")
	}
	return result
}
 
def zwaveEvent(physicalgraph.zwave.commands.hailv1.Hail cmd) {
	response(command(zwave.switchMultilevelV1.switchMultilevelGet()))
}
 
def zwaveEvent(physicalgraph.zwave.commands.securityv1.SecurityMessageEncapsulation cmd) {
	def encapsulatedCommand = cmd.encapsulatedCommand([0x20: 1, 0x84: 1])
	if (encapsulatedCommand) {
		state.sec = 1
		def result = zwaveEvent(encapsulatedCommand)
		result = result.collect {
			if (it instanceof physicalgraph.device.HubAction && !it.toString().startsWith("9881")) {
				response(cmd.CMD + "00" + it.toString())
			} else {
				it
			}
		}
		result
	}
}

def zwaveEvent(physicalgraph.zwave.commands.configurationv1.ConfigurationReport cmd) {
    if (cmd.parameterNumber == 37) { 
       if (cmd.configurationValue[0] == 0) toggleTiles("all")
    } else {
       log.debug "${device.displayName} parameter '${cmd.parameterNumber}' with a byte size of '${cmd.size}' is set to '${cmd.configurationValue}'"
    }
}
 
 
def zwaveEvent(physicalgraph.zwave.Command cmd) {
	def linkText = device.label ?: device.name
	[linkText: linkText, descriptionText: "$linkText: $cmd", displayed: false]
}

private toggleTiles(value) {
   def tiles = ["switch1", "switch2", "switch3", "switch4", "switch5", "switch6"]
   tiles.each {tile ->
      if (tile != value) sendEvent(name: tile, value: "off")
   }
}
 
def on() {
	commands([
		zwave.basicV1.basicSet(value: 0xFF),
		zwave.switchMultilevelV3.switchMultilevelGet(),
	], 3500)
}
 
def off() {
	commands([
		zwave.basicV1.basicSet(value: 0x00),
		zwave.switchMultilevelV3.switchMultilevelGet(),
	], 3500)
}
 
def setLevel(level) {
	setLevel(level, 1)
}
 
def setLevel(level, duration) {
	if(level > 99) level = 99
	commands([
		zwave.switchMultilevelV3.switchMultilevelSet(value: level, dimmingDuration: duration),
		zwave.switchMultilevelV3.switchMultilevelGet(),
	], (duration && duration < 12) ? (duration * 1000) : 3500)
}
 
def refresh() {
	commands([
		zwave.switchMultilevelV3.switchMultilevelGet(),
        zwave.configurationV1.configurationGet(parameterNumber: 37),
	], 1000)
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
    def warmWhite = 0
    def coldWhite = 0
	log.debug "setColor: ${value}"
    //log.debug "${value.hue}"
	if (value.hue && value.saturation) {
        log.debug "setting color with hue & saturation"
        def hue = value.hue ?: device.currentValue("hue")
		def saturation = value.saturation ?: device.currentValue("saturation")
		if(hue == null) hue = 13
		if(saturation == null) saturation = 13
		def rgb = huesatToRGB(hue as Integer, saturation as Integer)
    	if ( value.hue == 53 && value.saturation == 91 ) {
            Random rand = new Random()
            int max = 100
            hue = rand.nextInt(max+1)
            rgb = huesatToRGB(hue as Integer, saturation as Integer)
    	}
        else if ( value.hue == 23 && value.saturation == 56 ) {
        	def level = 255
        	if ( value.level != null ) level = value.level * 0.01 * 255
            warmWhite = level
            coldWhite = 0
            rgb[0] = 0
            rgb[1] = 0
            rgb[2] = 0
    	}
        else {
    		if ( value.hue > 5 && value.hue < 100 ) hue = value.hue - 5 else hue = 1
            rgb = huesatToRGB(hue as Integer, saturation as Integer)
    	}
		result << zwave.switchColorV3.switchColorSet(red: rgb[0], green: rgb[1], blue: rgb[2], warmWhite:warmWhite, coldWhite:coldWhite)
        if(value.level != null && value.level > 1){
        	if(value.level > 99) value.level = 99
			result << zwave.switchMultilevelV3.switchMultilevelSet(value: value.level, dimmingDuration: 3500)
            result << zwave.switchMultilevelV3.switchMultilevelGet()
        }
	}
    else if (value.hex) {
		def c = value.hex.findAll(/[0-9a-fA-F]{2}/).collect { Integer.parseInt(it, 16) }
		result << zwave.switchColorV3.switchColorSet(red:c[0], green:c[1], blue:c[2], warmWhite:0, coldWhite:0)
	} 
 	result << zwave.basicV1.basicSet(value: 0xFF)
	if(value.hue) sendEvent(name: "hue", value: value.hue)
	if(value.hex) sendEvent(name: "color", value: value.hex)
	if(value.switch) sendEvent(name: "switch", value: value.switch)
	if(value.saturation) sendEvent(name: "saturation", value: value.saturation)
 
	commands(result)
}
 
def setColorTemperature(percent) {
	if(percent > 99) percent = 99
	int warmValue = percent * 255 / 99
	command(zwave.switchColorV3.switchColorSet(red:0, green:0, blue:0, warmWhite:warmValue, coldWhite:(255 - warmValue)))
}
 
def reset() {
	log.debug "reset()"
	sendEvent(name: "color", value: "#ffffff")
	setColorTemperature(99)
}
 
private command(physicalgraph.zwave.Command cmd) {
	if (state.sec) {
		zwave.securityV1.securityMessageEncapsulation().encapsulate(cmd).format()
	} else {
		cmd.format()
	}
}
 
private commands(commands, delay=500) {
	delayBetween(commands.collect{ command(it) }, delay)
}
 
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

def on1() {
    log.debug "on1()"
    sendEvent(name:"switch1", value:"on")
    toggleTiles("switch1")
	commands([
        zwave.configurationV1.configurationSet(scaledConfigurationValue: 16777258, parameterNumber: 37, size: 4),
	], 3500)
}

def on2() {
    log.debug "on2()"
    sendEvent(name:"switch2", value:"on")
    toggleTiles("switch2")
	commands([
        zwave.configurationV1.configurationSet(scaledConfigurationValue: 1090519050, parameterNumber: 37, size: 4),
	], 3500)
}

def on3() {
    log.debug "on3()"
    sendEvent(name:"switch3", value:"on")
    toggleTiles("switch3")
	commands([
        zwave.configurationV1.configurationSet(scaledConfigurationValue: 50331658, parameterNumber: 37, size: 4),
	], 3500)
}

def on4() {
    log.debug "on4()"
    sendEvent(name:"switch4", value:"on")
    toggleTiles("switch4")
	commands([
        zwave.configurationV1.configurationSet(scaledConfigurationValue: 1633771873, parameterNumber: 38, size: 4),
        zwave.configurationV1.configurationSet(scaledConfigurationValue: 1113784321, parameterNumber: 37, size: 4),
	], 3500)
} 
 
def on6() {
    log.debug "on6()"
    sendEvent(name:"switch6", value:"on")
    toggleTiles("switch6")
	commands([
        zwave.configurationV1.configurationSet(scaledConfigurationValue: calculateParameter(38), parameterNumber: 38, size: 4),
        zwave.configurationV1.configurationSet(scaledConfigurationValue: calculateParameter(37), parameterNumber: 37, size: 4),
	], 3500)
} 

def offCmd() {
    log.debug "offCmd()"
	commands([
        zwave.configurationV1.configurationSet(scaledConfigurationValue: 0, parameterNumber: 37, size: 4),
        zwave.configurationV1.configurationGet(parameterNumber: 37),
	], 3500)
}

def off1() { offCmd() }
def off2() { offCmd() }
def off3() { offCmd() }
def off4() { offCmd() }
def off5() { offCmd() }
def off6() { offCmd() }



private calculateParameter(number) {
   long value = 0
   switch (number){
      case 37:
         value += settings.transition ? settings.transition.toLong() : 0
         value += 33554432 // Custom Mode
         value += settings.brightness ? (settings.brightness.toLong() * 65536) : 0
         value += settings.count ? (settings.count.toLong() * 256) : 0
         value += settings.speed ? settings.speed.toLong() : 0
         value += settings.speedLevel ? settings.speedLevel.toLong() : 0
      break
      case 38:
         value += settings.color1 ? (settings.color1.toLong() * 1) : 0
         value += settings.color2 ? (settings.color2.toLong() * 16) : 0
         value += settings.color3 ? (settings.color3.toLong() * 256) : 0
         value += settings.color4 ? (settings.color4.toLong() * 4096) : 0
         value += settings.color5 ? (settings.color5.toLong() * 65536) : 0
         value += settings.color6 ? (settings.color6.toLong() * 1048576) : 0
         value += settings.color7 ? (settings.color7.toLong() * 16777216) : 0
         value += settings.color8 ? (settings.color8.toLong() * 268435456) : 0
      break
    }
    return value
}