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
 *  Fibaro RGBW Controller (Advanced)
 *
 *  Author: Eric Maycock (erocm123)
 *  Date: 2016-11-02
 */
 
metadata {
	definition (name: "Fibaro RGBW Controller (Advanced)", namespace: "erocm123", author: "Eric Maycock", vid:"generic-rgbw-color-bulb") {
		capability "Switch Level"
		capability "Color Control"
		capability "Color Temperature"
		capability "Switch"
		capability "Refresh"
		capability "Actuator"
		capability "Sensor"
 
		command "reset"
		command "refresh"
        
        (1..5).each { n ->
			attribute "switch$n", "enum", ["on", "off"]
			command "on$n"
			command "off$n"
		}

        fingerprint deviceId: "0x1101", inClusters: "0x5E, 0x26, 0x33, 0x27, 0x2C, 0x2B, 0x70, 0x59, 0x85, 0x72, 0x86, 0x7A, 0x73, 0xEF, 0x5A, 0x82"

	}
    
    preferences {
        input description: "Create a custom program by modifying the settings below. This program can then be executed by using the \"custom\" button on the device page.", displayDuringSetup: false, type: "paragraph", element: "paragraph"

        input "transition", "enum", title: "Transition", defaultValue: 0, displayDuringSetup: false, required: false, options: [
                0:"Smooth",
                1073741824:"Flash",
                //3221225472:"Fade Out Fade In"
                ]
        input "count", "number", title: "Cycle Count (0 [unlimited])", defaultValue: 0, displayDuringSetup: false, required: false, range: "0..254"
        input "speed", "enum", title: "Color Change Speed", defaultValue: 0, displayDuringSetup: false, required: false, options: [
                0:"Fast",
                16:"Medium Fast",
                32:"Medium",
                64:"Medium Slow",
                128:"Slow"]
        input "speedLevel", "number", title: "Color Residence Time (1 [fastest], 254 [slowest])", defaultValue: "0", displayDuringSetup: true, required: false, range: "0..254"
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
            state "off", label: "fireplace", action: "on1", icon: "st.switches.switch.off", backgroundColor: "#ffffff", nextState:"on"
			state "on", label: "fireplace", action: "off1", icon: "st.switches.switch.on", backgroundColor: "#79b821", nextState:"off"
		}
        standardTile("switch2", "switch2", canChangeIcon: true, width: 2, height: 2) {
            state "off", label: "storm", action: "on2", icon: "st.switches.switch.off", backgroundColor: "#ffffff", nextState:"on"
			state "on", label: "storm", action: "off2", icon: "st.switches.switch.on", backgroundColor: "#79b821", nextState:"off"
		}
        standardTile("switch3", "switch3", canChangeIcon: true, width: 2, height: 2) {
            state "off", label: "deepfade", action: "on3", icon: "st.switches.switch.off", backgroundColor: "#ffffff", nextState:"on"
			state "on", label: "deepfade", action: "off3", icon: "st.switches.switch.on", backgroundColor: "#79b821", nextState:"off"
		}
        standardTile("switch4", "switch4", canChangeIcon: true, width: 2, height: 2) {
            state "off", label: "litefade", action: "on4", icon: "st.switches.switch.off", backgroundColor: "#ffffff", nextState:"on"
			state "on", label: "litefade", action: "off4", icon: "st.switches.switch.on", backgroundColor: "#79b821", nextState:"off"
		}
        standardTile("switch5", "switch5", canChangeIcon: true, width: 2, height: 2) {
			state "off", label: "police", action: "on5", icon: "st.switches.switch.off", backgroundColor: "#ffffff", nextState:"on"
            state "on", label: "police", action: "off5", icon: "st.switches.switch.on", backgroundColor: "#79b821", nextState:"off"
		}
    }

	main(["switch"])
	details(["switch", "levelSliderControl",
             "colorTempControl", "colorTempTile",
             "switch1", "switch2", "switch3",
             "switch4", "switch5", "switch6",
             "refresh", "configure" ])
}
 
def updated() {
	response(refresh())
}
 
def parse(description) {
	def result = null
	if (description != "updated") {
		def cmd = zwave.parse(description, [0x20: 1, 0x26: 3, 0x70: 1, 0x33:3])
		if (cmd) {
			result = zwaveEvent(cmd)
			log.debug("'$cmd' parsed to $result")
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
	//dimmerEvents(cmd) 
}
 
private dimmerEvents(physicalgraph.zwave.Command cmd) {
	def value = (cmd.value ? "on" : "off")
	def result = [createEvent(name: "switch", value: value, descriptionText: "$device.displayName was turned $value")]
	if (cmd.value) {
		result << createEvent(name: "level", value: cmd.value, unit: "%")
	}
    if (cmd.value == 0) toggleTiles("all")
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
      if (tile != value) { sendEvent(name: tile, value: "off") }
      else { sendEvent(name:tile, value:"on"); sendEvent(name:"switch", value:"on") }
   }
}
 
def on() {
    toggleTiles("all")
	commands([
		zwave.basicV1.basicSet(value: 0xFF),
		zwave.switchMultilevelV3.switchMultilevelGet(),
	], 2000)
}
 
def off() {
    toggleTiles("all")
	commands([
		zwave.basicV1.basicSet(value: 0x00),
		zwave.switchMultilevelV3.switchMultilevelGet(),
	], 2000)
}
 
def setLevel(level) {
    toggleTiles("all")
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
 
    toggleTiles("all")
	commands(result)
}
 
def setColorTemperature(percent) {
	if(percent > 99) percent = 99
	int warmValue = percent * 255 / 99
    toggleTiles("all")
    sendEvent(name: "colorTemperature", value: percent)
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
    toggleTiles("switch1")
	commands([
        zwave.configurationV1.configurationSet(scaledConfigurationValue: 6, parameterNumber: 72, size: 1),
	], 1500)
}

def on2() {
    log.debug "on2()"    
    toggleTiles("switch2")
	commands([
        zwave.configurationV1.configurationSet(scaledConfigurationValue: 7, parameterNumber: 72, size: 1),
	], 1500)
}

def on3() {
    log.debug "on3()"
    toggleTiles("switch3")
	commands([
        zwave.configurationV1.configurationSet(scaledConfigurationValue: 8, parameterNumber: 72, size: 1),
	], 1500)
}

def on4() {
    log.debug "on4()"
    toggleTiles("switch4")
	commands([
        zwave.configurationV1.configurationSet(scaledConfigurationValue: 9, parameterNumber: 72, size: 1),
	], 1500)
} 
 
def on5() {
    log.debug "on5()"
    toggleTiles("switch5")
	commands([
        zwave.configurationV1.configurationSet(scaledConfigurationValue: 10, parameterNumber: 72, size: 1),
	], 1500)
} 

def offCmd() {
    log.debug "offCmd()"
	commands([
        zwave.configurationV1.configurationSet(scaledConfigurationValue: 0, parameterNumber: 72, size: 1),
	], 1500)
}

def off1() { offCmd() }
def off2() { offCmd() }
def off3() { offCmd() }
def off4() { offCmd() }
def off5() { offCmd() }
def off6() { offCmd() }

def integer2Cmd(value, size) {
	switch(size) {
	case 1:
		[value]
    break
	case 2:
    	def short value1   = value & 0xFF
        def short value2 = (value >> 8) & 0xFF
        [value2, value1]
    break
    case 3:
    	def short value1   = value & 0xFF
        def short value2 = (value >> 8) & 0xFF
        def short value3 = (value >> 16) & 0xFF
        [value3, value2, value1]
    break
	case 4:
    	def short value1 = value & 0xFF
        def short value2 = (value >> 8) & 0xFF
        def short value3 = (value >> 16) & 0xFF
        def short value4 = (value >> 24) & 0xFF
		[value4, value3, value2, value1]
	break
	}
}


private calculateParameter(number) {
   long value = 0
   switch (number){
      case 37:
         value += settings.transition ? settings.transition.toLong() : 0
         value += 33554432 // Custom Mode 
         value += settings.count ? (settings.count.toLong() * 65536) : 0
         value += settings.speed ? (settings.speed.toLong() * 256) : 0
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
