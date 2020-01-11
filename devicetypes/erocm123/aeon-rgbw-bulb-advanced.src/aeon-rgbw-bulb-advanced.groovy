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
 *  Date: 2018-05-02
 *  
 *  2018-05-02: huesatToRGB Changed method provided by daved314
 */
 
metadata {
	definition (name: "Aeon RGBW Bulb (Advanced)", namespace: "erocm123", author: "Eric Maycock", vid:"generic-rgbw-color-bulb") {
		capability "Switch Level"
		capability "Color Control"
		capability "Color Temperature"
		capability "Switch"
		capability "Refresh"
		capability "Actuator"
		capability "Sensor"
        capability "Configuration"
        capability "Health Check"
 
		command "reset"
		command "refresh"
        
        (1..6).each { n ->
			attribute "switch$n", "enum", ["on", "off"]
			command "on$n"
			command "off$n"
		}
        
        fingerprint mfr: "0086", prod: "0103", model: "0062", deviceJoinName: "Aeon RGBW Bulb"
        fingerprint mfr: "0086", prod: "0103", model: "0079", deviceJoinName: "Aeon RGBW LED Strip"

        fingerprint deviceId: "0x1101", inClusters: "0x5E, 0x26, 0x33, 0x27, 0x2C, 0x2B, 0x70, 0x59, 0x85, 0x72, 0x86, 0x7A, 0x73, 0xEF, 0x5A, 0x82"

	}
    
    preferences {
    
        input description: "Once you change values on this page, the corner of the \"configuration\" icon will change orange until all configuration parameters are updated.", title: "Settings", displayDuringSetup: false, type: "paragraph", element: "paragraph"
		generate_preferences(configuration_model())
        
        input description: "Create a custom program by modifying the settings below. This program can then be executed by using the \"custom\" button on the device page.", title: "Programs", displayDuringSetup: false, type: "paragraph", element: "paragraph"

        input "transition", "enum", title: "Transition", defaultValue: 0, displayDuringSetup: false, required: false, options: [
                0:"Smooth",
                1:"Flash",
                ]
        input "brightness", "number", title: "Brightness (Firmware 1.05)", defaultValue: 1, displayDuringSetup: false, required: false, range: "1..99"
        input "count", "number", title: "Cycle Count (0 [unlimited])", defaultValue: 0, displayDuringSetup: false, required: false, range: "0..254"
        input "speed", "enum", title: "Color Change Speed", defaultValue: 0, displayDuringSetup: false, required: false, options: [
                0:"Fast",
                1:"Medium Fast",
                2:"Medium",
                3:"Medium Slow",
                4:"Slow"]
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
				attributeState "on", label:'${name}', action:"switch.off", icon:"st.lights.philips.hue-single", backgroundColor:"#00a0dc", nextState:"turningOff"
				attributeState "off", label:'${name}', action:"switch.on", icon:"st.lights.philips.hue-single", backgroundColor:"#ffffff", nextState:"turningOn"
				attributeState "turningOn", label:'${name}', action:"switch.off", icon:"st.lights.philips.hue-single", backgroundColor:"#00a0dc", nextState:"turningOff"
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
        standardTile("configure", "device.needUpdate", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
            state "NO" , label:'', action:"configuration.configure", icon:"st.secondary.configure"
            state "YES", label:'', action:"configuration.configure", icon:"https://github.com/erocm123/SmartThingsPublic/raw/master/devicetypes/erocm123/qubino-flush-1d-relay.src/configure@2x.png"
        }
        valueTile("colorTempTile", "device.colorTemperature", decoration: "flat", height: 2, width: 2) {
        	state "colorTemperature", label:'${currentValue}%', backgroundColor:"#ffffff"
        }
        controlTile("colorTempControl", "device.colorTemperature", "slider", decoration: "flat", height: 2, width: 4, inactiveLabel: false) {
		    state "colorTemperature", action:"setColorTemperature"
	    }
		standardTile("switch1", "switch1", canChangeIcon: true, width: 2, height: 2, decoration: "flat") {
            state "off", label: "fade", action: "on1", icon: "st.switches.switch.off", backgroundColor: "#cccccc"
			state "on", label: "fade", action: "off1", icon: "st.switches.switch.on", backgroundColor: "#00a0dc"
		}
        standardTile("switch2", "switch2", canChangeIcon: true, width: 2, height: 2, decoration: "flat") {
            state "off", label: "flash", action: "on2", icon: "st.switches.switch.off", backgroundColor: "#cccccc"
			state "on", label: "flash", action: "off2", icon: "st.switches.switch.on", backgroundColor: "#00a0dc"
		}
        standardTile("switch3", "switch3", canChangeIcon: true, width: 2, height: 2, decoration: "flat") {
            state "off", label: "random", action: "on3", icon: "st.switches.switch.off", backgroundColor: "#cccccc"
			state "on", label: "random", action: "off3", icon: "st.switches.switch.on", backgroundColor: "#00a0dc"
		}
        standardTile("switch4", "switch4", canChangeIcon: true, width: 2, height: 2, decoration: "flat") {
            state "off", label: "police", action: "on4", icon: "st.switches.switch.off", backgroundColor: "#cccccc"
			state "on", label: "police", action: "off4", icon: "st.switches.switch.on", backgroundColor: "#00a0dc"
		}
        standardTile("switch5", "switch5", canChangeIcon: true, width: 2, height: 2, decoration: "flat") {
			state "off", label: "xmas", action: "on5", icon: "st.switches.switch.off", backgroundColor: "#cccccc"
            state "on", label: "xmas", action: "off5", icon: "st.switches.switch.on", backgroundColor: "#00a0dc"
		}
        standardTile("switch6", "switch6", canChangeIcon: true, width: 2, height: 2, decoration: "flat") {
            state "off", label: "custom", action: "on6", icon: "st.switches.switch.off", backgroundColor: "#cccccc"
			state "on", label: "custom", action: "off6", icon: "st.switches.switch.on", backgroundColor: "#00a0dc"
		}
        valueTile(
			"currentFirmware", "device.currentFirmware", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
			state "currentFirmware", label:'Firmware: v${currentValue}', unit:""
		}
    }

	main(["switch"])
	details(["switch", "levelSliderControl",
             "colorTempControl", "colorTempTile",
             "switch1", "switch2", "switch3",
             "switch4", "switch5", "switch6",
             "refresh", "configure", "currentFirmware" ])
}
 
/**
* Triggered when Done button is pushed on Preference Pane
*/
def updated()
{
	state.enableDebugging = settings.enableDebugging
    logging("updated() is being called")
    sendEvent(name: "checkInterval", value: 2 * 60 * 60 + 2 * 60, displayed: false, data: [protocol: "zwave", hubHardwareId: device.hub.hardwareID])
    state.needfwUpdate = ""
    
    def cmds = update_needed_settings()
    
    sendEvent(name:"needUpdate", value: device.currentValue("needUpdate"), displayed:false, isStateChange: true)
    
    if (cmds != []) response(commands(cmds))
}

def configure() {
	state.enableDebugging = settings.enableDebugging
    logging("Configuring Device For SmartThings Use")
    def cmds = []

    cmds = update_needed_settings()
    
    if (cmds != []) commands(cmds)
}

 
def parse(description) {
	def result = null
	if (description != "updated") {
		def cmd = zwave.parse(description, [0x20: 1, 0x26: 3, 0x70: 1, 0x33:3])
		if (cmd) {
			result = zwaveEvent(cmd)
			logging("'$cmd' parsed to $result")
		} else {
			logging("Couldn't zwave.parse '$description'")
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
	def encapsulatedCommand = cmd.encapsulatedCommand([0x20: 1, 0x31: 5, 0x30: 2, 0x84: 1, 0x70: 1])
	state.sec = 1
	if (encapsulatedCommand) {
		zwaveEvent(encapsulatedCommand)
	} else {
		log.warn "Unable to extract encapsulated cmd from $cmd"
		createEvent(descriptionText: cmd.toString())
	}
}

def zwaveEvent(physicalgraph.zwave.commands.securityv1.SecurityCommandsSupportedReport cmd) {
	response(configure())
}

def zwaveEvent(physicalgraph.zwave.commands.configurationv1.ConfigurationReport cmd) {
    if (cmd.parameterNumber == 37) { 
       if (cmd.configurationValue[0] == 0) toggleTiles("all")
    } else {
       update_current_properties(cmd)
       logging("${device.displayName} parameter '${cmd.parameterNumber}' with a byte size of '${cmd.size}' is set to '${cmd.configurationValue}'")
    }
} 

def zwaveEvent(physicalgraph.zwave.commands.versionv1.VersionReport cmd){
    logging("Version Report ${cmd.toString()} ---- ${cmd.applicationVersion}.${cmd.applicationSubVersion.toString().padLeft(2, '0')}")
    state.needfwUpdate = "false"
    updateDataValue("firmware", "${cmd.applicationVersion}.${cmd.applicationSubVersion.toString().padLeft(2, '0')}")
    createEvent(name: "currentFirmware", value: "${cmd.applicationVersion}.${cmd.applicationSubVersion.toString().padLeft(2, '0')}")
}

def zwaveEvent(physicalgraph.zwave.commands.manufacturerspecificv2.ManufacturerSpecificReport cmd) {
	def result = []

	def msr = String.format("%04X-%04X-%04X", cmd.manufacturerId, cmd.productTypeId, cmd.productId)
	log.debug "msr: $msr"
	updateDataValue("MSR", msr)

	result << createEvent(descriptionText: "$device.displayName MSR: $msr", isStateChange: false)
	result
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
		zwave.basicV1.basicGet(),
	])
}
 
def off() {
    toggleTiles("all")
	commands([
		zwave.basicV1.basicSet(value: 0x00),
		zwave.basicV1.basicGet(),
	])
}
 
def setLevel(level) {
    toggleTiles("all")
	setLevel(level, 1)
}
 
def setLevel(level, duration) {
	if(level > 99) level = 99
	commands([
		zwave.switchMultilevelV3.switchMultilevelSet(value: level, dimmingDuration: duration),
		zwave.basicV1.basicGet(),
	], (duration && duration < 12) ? (duration * 1000) : 3500)
}
 
def refresh() {
	commands([
		zwave.basicV1.basicGet(),
        zwave.configurationV1.configurationGet(parameterNumber: 37),
        zwave.manufacturerSpecificV2.manufacturerSpecificGet()
	])
}

def ping() {
    log.debug "ping()"
	refresh()
}
 
def setSaturation(percent) {
	logging("setSaturation($percent)")
	setColor(saturation: percent)
}
 
def setHue(value) {
	logging("setHue($value)")
	setColor(hue: value)
}

private getEnableRandomHue(){
    switch (settings.enableRandom) {
        case "0":
        return 23
        break
        case "1":
        return 52
        break
        case "2":
        return 53
        break
        case "3":
        return 20
        break
        default:
    
        break
    }
}

private getEnableRandomSat(){
    switch (settings.enableRandom) {
        case "0":
        return 56
        break
        case "1":
        return 19
        break
        case "2":
        return 91
        break
        case "3":
        return 80
        break
        default:
    
        break
    }
}
 
def setColor(value) {
	def result = []
    def warmWhite = 0
    def coldWhite = 0
	logging("setColor: ${value}")
	if (value.hue && value.saturation) {
        logging("setting color with hue & saturation")
        def hue = (value.hue != null) ? value.hue : 13
		def saturation = (value.saturation != null) ? value.saturation : 13
		def rgb = huesatToRGB(hue as Integer, saturation as Integer)
    	if ( settings.enableRandom && value.hue == getEnableRandomHue() && value.saturation == getEnableRandomSat() ) {
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
        if(value.level != null && value.level != 1.0){
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
    result << zwave.basicV1.basicGet()
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
	command(zwave.switchColorV3.switchColorSet(red:0, green:0, blue:0, warmWhite: (warmValue > 127 ? 255 : 0), coldWhite: (warmValue < 128? 255 : 0)))
    
}
 
def reset() {
	logging("reset()")
	sendEvent(name: "color", value: "#ffffff")
	setColorTemperature(1)
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
 
// huesatToRGB Changed method provided by daved314
def huesatToRGB(float hue, float sat) {
	if (hue <= 100) {
		hue = hue * 3.6
    }
    sat = sat / 100
    float v = 1.0
    float c = v * sat
    float x = c * (1 - Math.abs(((hue/60)%2) - 1))
    float m = v - c
    int mod_h = (int)(hue / 60)
    int cm = Math.round((c+m) * 255)
    int xm = Math.round((x+m) * 255)
    int zm = Math.round((0+m) * 255)
    switch(mod_h) {
    	case 0: return [cm, xm, zm]
       	case 1: return [xm, cm, zm]
        case 2: return [zm, cm, xm]
        case 3: return [zm, xm, cm]
        case 4: return [xm, zm, cm]
        case 5: return [cm, zm, xm]
	}   	
}

def on1() {
    logging("on1()")
    toggleTiles("switch1")
    def cmds = []
    if (device.currentValue("currentFirmware") == null || device.currentValue("currentFirmware") != "1.05") {
        cmds << zwave.configurationV1.configurationSet(scaledConfigurationValue: 16781342, parameterNumber: 37, size: 4)
    } else {
        cmds << zwave.configurationV1.configurationSet(scaledConfigurationValue: 50332416, parameterNumber: 38, size: 4)
        cmds << zwave.configurationV1.configurationSet(configurationValue: integer2Cmd(157483073 , 4), parameterNumber: 37, size: 4)
    }
    cmds << zwave.basicV1.basicGet()
	commands(cmds)
}

def on2() {
    logging("on2()")  
    toggleTiles("switch2")
    def cmds = []
    if (device.currentValue("currentFirmware") == null || device.currentValue("currentFirmware") != "1.05") {
        cmds << zwave.configurationV1.configurationSet(scaledConfigurationValue: 1090551813, parameterNumber: 37, size: 4)
    } else {
        cmds << zwave.configurationV1.configurationSet(scaledConfigurationValue: 2560, parameterNumber: 38, size: 4)
        cmds << zwave.configurationV1.configurationSet(configurationValue: integer2Cmd(1097007169, 4), parameterNumber: 37, size: 4)
    }
    cmds << zwave.basicV1.basicGet()
	commands(cmds)
}

def on3() {
    logging("on3()")
    toggleTiles("switch3")
    def cmds = []
    if (device.currentValue("currentFirmware") == null || device.currentValue("currentFirmware") != "1.05") {
        cmds << zwave.configurationV1.configurationSet(scaledConfigurationValue: 50335754, parameterNumber: 37, size: 4)
    } else {
        cmds << zwave.configurationV1.configurationSet(scaledConfigurationValue: 50332416, parameterNumber: 38, size: 4)
        cmds << zwave.configurationV1.configurationSet(configurationValue: integer2Cmd(191037440 , 4), parameterNumber: 37, size: 4)
    }
    cmds << zwave.basicV1.basicGet()
	commands(cmds)
}

def on4() {
    logging("on4()")
    toggleTiles("switch4")
    def cmds = []
    if (device.currentValue("currentFirmware") == null || device.currentValue("currentFirmware") != "1.05") {
        cmds << zwave.configurationV1.configurationSet(scaledConfigurationValue: 1633771873, parameterNumber: 38, size: 4)
        cmds << zwave.configurationV1.configurationSet(scaledConfigurationValue: 1113784321, parameterNumber: 37, size: 4)
    } else {
        cmds << zwave.configurationV1.configurationSet(configurationValue: integer2Cmd(256, 4), parameterNumber: 38, size: 4)
        cmds << zwave.configurationV1.configurationSet(configurationValue: integer2Cmd(369098752, 4), parameterNumber: 39, size: 4)
        cmds << zwave.configurationV1.configurationSet(configurationValue: integer2Cmd(1244790848, 4), parameterNumber: 37, size: 4)
    }
    cmds << zwave.basicV1.basicGet()
	commands(cmds)
} 

def on5() {
    logging("on5()")
    toggleTiles("switch5")
    def cmds = []
    if (device.currentValue("currentFirmware") == null || device.currentValue("currentFirmware") != "1.05") {
        cmds << zwave.configurationV1.configurationSet(scaledConfigurationValue: 65, parameterNumber: 38, size: 4)
        cmds << zwave.configurationV1.configurationSet(scaledConfigurationValue: 1107300372, parameterNumber: 37, size: 4)
    } else {
        cmds << zwave.configurationV1.configurationSet(configurationValue: integer2Cmd(768, 4), parameterNumber: 38, size: 4)
        cmds << zwave.configurationV1.configurationSet(configurationValue: integer2Cmd(335544320, 4), parameterNumber: 39, size: 4)
        cmds << zwave.configurationV1.configurationSet(configurationValue: integer2Cmd(1244790784, 4), parameterNumber: 37, size: 4)
    }
    cmds << zwave.basicV1.basicGet()
	commands(cmds)
} 
 
def on6() {
    logging("on6()")
    toggleTiles("switch6")
    def cmds = []
    if (device.currentValue("currentFirmware") == null || device.currentValue("currentFirmware") != "1.04") {
        cmds << zwave.configurationV1.configurationSet(configurationValue: integer2Cmd(calculateParameter(38), 4), parameterNumber: 38, size: 4)
        cmds << zwave.configurationV1.configurationSet(scaledConfigurationValue: calculateParameter(37), parameterNumber: 37, size: 4)
    } else {
        cmds << zwave.configurationV1.configurationSet(configurationValue: integer2Cmd(calculateParameter(39), 4), parameterNumber: 39, size: 4)
        cmds << zwave.configurationV1.configurationSet(configurationValue: integer2Cmd(calculateParameter(38), 4), parameterNumber: 38, size: 4)
        cmds << zwave.configurationV1.configurationSet(configurationValue: integer2Cmd(calculateParameter(37), 4), parameterNumber: 37, size: 4)
    }
    cmds << zwave.basicV1.basicGet()
	commands(cmds)
} 

def offCmd() {
    logging("offCmd()")
    def cmds = []
    if (device.currentValue("currentFirmware") == null || device.currentValue("currentFirmware") != "1.05") {
        cmds << zwave.configurationV1.configurationSet(scaledConfigurationValue: 0, parameterNumber: 37, size: 4)
        cmds << zwave.configurationV1.configurationGet(parameterNumber: 37)
    } else {
        cmds << zwave.configurationV1.configurationSet(scaledConfigurationValue: 2, parameterNumber: 36, size: 1)
        cmds << zwave.basicV1.basicGet()
	}
    commands(cmds)
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
      if (device.currentValue("currentFirmware") == null || device.currentValue("currentFirmware") == "1.04"){
         value += settings.transition ? settings.transition.toLong() * 1073741824 : 0
         value += 33554432 // Custom Mode 
         value += settings.count ? (settings.count.toLong() * 65536) : 0
         value += settings.speed ? (settings.speed.toLong() * 16 * 256) : 0
         value += settings.speedLevel ? settings.speedLevel.toLong() : 0
      } else {
         value += settings.transition ? settings.transition.toLong() * 1073741824 : 0
         value += 134217728 // Allow smooth adjustable speed
         value += 33554432 // Custom Mode
         value += settings.brightness ? (settings.brightness.toLong() * 65536) : 0
         value += settings.count ? (settings.count.toLong() * 256) : 0
         value += settings.speed ? (2 * 32) : 0
      }
      break
      case 38:
      if (device.currentValue("currentFirmware") == null || device.currentValue("currentFirmware") == "1.04"){
         value += settings.color1 ? (settings.color1.toLong() * 1) : 0
         value += settings.color2 ? (settings.color2.toLong() * 16) : 0
         value += settings.color3 ? (settings.color3.toLong() * 256) : 0
         value += settings.color4 ? (settings.color4.toLong() * 4096) : 0
         value += settings.color5 ? (settings.color5.toLong() * 65536) : 0
         value += settings.color6 ? (settings.color6.toLong() * 1048576) : 0
         value += settings.color7 ? (settings.color7.toLong() * 16777216) : 0
         value += settings.color8 ? (settings.color8.toLong() * 268435456) : 0
      } else {
         value += settings.transition == "1" ? 0 : (settings.speed.toLong() * 2 * 16777216) // Speed from off to on
         value += settings.transition == "1" ? 0 : (settings.speed.toLong() * 2 * 65536) // Speed from on to off
         value += settings.speedLevel ? (settings.speedLevel.toLong() * 256) : 5 // Pause time at on
         value += 0 // Pause time at off
      }
      break
      case 39:
         value += settings.color8 ? (settings.color8.toLong() * 1) : 0
         value += settings.color7 ? (settings.color7.toLong() * 16) : 0
         value += settings.color6 ? (settings.color6.toLong() * 256) : 0
         value += settings.color5 ? (settings.color5.toLong() * 4096) : 0
         value += settings.color4 ? (settings.color4.toLong() * 65536) : 0
         value += settings.color3 ? (settings.color3.toLong() * 1048576) : 0
         value += settings.color2 ? (settings.color2.toLong() * 16777216) : 0
         value += settings.color1 ? (settings.color1.toLong() * 268435456) : 0
      break
    }
    return value
}

def generate_preferences(configuration_model)
{
    def configuration = parseXml(configuration_model)
   
    configuration.Value.each
    {
        switch(it.@type)
        {   
            case ["byte","short","four"]:
                input "${it.@index}", "number",
                    title:"${it.@label}\n" + "${it.Help}",
                    range: "${it.@min}..${it.@max}",
                    defaultValue: "${it.@value}",
                    displayDuringSetup: "${it.@displayDuringSetup}"
            break
            case "list":
                def items = []
                it.Item.each { items << ["${it.@value}":"${it.@label}"] }
                input "${it.@index}", "enum",
                    title:"${it.@label}\n" + "${it.Help}",
                    defaultValue: "${it.@value}",
                    displayDuringSetup: "${it.@displayDuringSetup}",
                    options: items
            break
            case "decimal":
               input "${it.@index}", "decimal",
                    title:"${it.@label}\n" + "${it.Help}",
                    range: "${it.@min}..${it.@max}",
                    defaultValue: "${it.@value}",
                    displayDuringSetup: "${it.@displayDuringSetup}"
            break
            case "boolean":
               input "${it.@index}", "boolean",
                    title: it.@label != "" ? "${it.@label}\n" + "${it.Help}" : "" + "${it.Help}",
                    defaultValue: "${it.@value}",
                    displayDuringSetup: "${it.@displayDuringSetup}"
            break
        }  
    }
}

def update_current_properties(cmd)
{
    def currentProperties = state.currentProperties ?: [:]
    
    currentProperties."${cmd.parameterNumber}" = cmd.configurationValue

    if (settings."${cmd.parameterNumber}" != null)
    {
        if (convertParam(cmd.parameterNumber, settings."${cmd.parameterNumber}") == cmd2Integer(cmd.configurationValue))
        {
            sendEvent(name:"needUpdate", value:"NO", displayed:false, isStateChange: true)
        }
        else
        {
            sendEvent(name:"needUpdate", value:"YES", displayed:false, isStateChange: true)
        }
    }

    state.currentProperties = currentProperties
}

def update_needed_settings()
{
    def cmds = []
    def currentProperties = state.currentProperties ?: [:]
     
    def configuration = parseXml(configuration_model())
    def isUpdateNeeded = "NO"
    
    if(!state.needfwUpdate || state.needfwUpdate == ""){
       logging("Requesting device firmware version")
       cmds << zwave.versionV1.versionGet()
    }    
    
    configuration.Value.each
    {     
        if ("${it.@setting_type}" == "zwave"){
            if (currentProperties."${it.@index}" == null)
            {
                if (device.currentValue("currentFirmware") == null || "${it.@fw}".indexOf(device.currentValue("currentFirmware")) >= 0){
                    isUpdateNeeded = "YES"
                    logging("Current value of parameter ${it.@index} is unknown")
                    cmds << zwave.configurationV1.configurationGet(parameterNumber: it.@index.toInteger())
                }
            }
            else if ((settings."${it.@index}" != null || "${it.@type}" == "hidden") && cmd2Integer(currentProperties."${it.@index}") != convertParam(it.@index.toInteger(), settings."${it.@index}"? settings."${it.@index}" : "${it.@value}"))
            { 
                isUpdateNeeded = "YES"
                logging("Parameter ${it.@index} will be updated to " + convertParam(it.@index.toInteger(), settings."${it.@index}"? settings."${it.@index}" : "${it.@value}"))
                def convertedConfigurationValue = convertParam(it.@index.toInteger(), settings."${it.@index}"? settings."${it.@index}" : "${it.@value}")
                cmds << zwave.configurationV1.configurationSet(configurationValue: integer2Cmd(convertedConfigurationValue, it.@byteSize.toInteger()), parameterNumber: it.@index.toInteger(), size: it.@byteSize.toInteger())
                cmds << zwave.configurationV1.configurationGet(parameterNumber: it.@index.toInteger())
            } 
        }
    }
    
    sendEvent(name:"needUpdate", value: isUpdateNeeded, displayed:false, isStateChange: true)
    return cmds
}

def convertParam(number, value) {
    def parValue
	switch (number){
    	case 28:
            parValue = (value == "true" ? 1 : 0)
            parValue += (settings."fc_2" == "true" ? 2 : 0)
            parValue += (settings."fc_3" == "true" ? 4 : 0)
            parValue += (settings."fc_4" == "true" ? 8 : 0)
        break
        case 29:
            parValue = (value == "true" ? 1 : 0)
            parValue += (settings."sc_2" == "true" ? 2 : 0)
            parValue += (settings."sc_3" == "true" ? 4 : 0)
            parValue += (settings."sc_4" == "true" ? 8 : 0)
        break
        default:
        	parValue = value
        break
    }
    return parValue.toInteger()
}

private def logging(message) {
    if (state.enableDebugging == null || state.enableDebugging == "true") log.debug "$message"
}

/**
* Convert 1 and 2 bytes values to integer
*/
def cmd2Integer(array) { 

switch(array.size()) {
	case 1:
		array[0]
    break
	case 2:
    	((array[0] & 0xFF) << 8) | (array[1] & 0xFF)
    break
    case 3:
    	((array[0] & 0xFF) << 16) | ((array[1] & 0xFF) << 8) | (array[2] & 0xFF)
    break
	case 4:
    	((array[0] & 0xFF) << 24) | ((array[1] & 0xFF) << 16) | ((array[2] & 0xFF) << 8) | (array[3] & 0xFF)
	break
    }
}

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

def configuration_model()
{
'''
<configuration>
  <Value type="list" byteSize="1" index="20" label="Default State (Firmware v1.05)" min="0" max="2" value="0" setting_type="zwave" fw="1.05">
    <Help>
Default state of the bulb when power is applied.
Range: 0~2
Default: 0 (Previous)
    </Help>
    <Item label="Previous" value="0" />
    <Item label="Always On" value="1" />
    <Item label="Always Off" value="2" />
  </Value>
  <Value type="disabled" byteSize="1" index="32" label="Color Report" min="0" max="1" value="0" setting_type="zwave" fw="1.05">
    <Help>
Enable or disable the sending of a report when the bulb color is changed.
Range: 0~1
Default: 0 (Disabled)
    </Help>
    <Item label="Disable" value="0" />
    <Item label="Enable" value="1" />
  </Value>
    <Value type="list" byteSize="1" index="34" label="External Switch" min="0" max="1" value="0" setting_type="zwave" fw="1.04,1.05">
    <Help>
Enable or disable manually toggle the LED bulb ON and OFF using an external switch.
Range: 0~1
Default: 0 (Disabled)
    </Help>
    <Item label="Disable" value="0" />
    <Item label="Enable" value="1" />
  </Value>
  <Value type="disabled" byteSize="1" index="80" label="Status Report" min="0" max="2" value="2" setting_type="zwave" fw="1.04,1.05">
    <Help>
Enable or disable the sending of a report when the state of the bulb is changed.
Range: 0~2
Default: 2 (Basic CC Report)
    </Help>
    <Item label="Disable" value="0" />
    <Item label="Hail CC" value="1" />
    <Item label="Basic CC Report" value="2" />
  </Value>
    <Value type="list" byteSize="1" index="112" label="Dimmer Mode (Firmware v1.05)" min="0" max="3" value="0" setting_type="zwave" fw="1.04,1.05">
    <Help>
Range: 0~3
Default: 0 (Parabolic)
    </Help>
    <Item label="Parabolic Curve" value="0" />
    <Item label="Index Curve" value="1" />
    <Item label="(Parabolic + Index)/2" value="2" />
    <Item label="Linear" value="3" />
  </Value>
  <Value type="list" index="enableRandom" label="Enable Random Function" value="false" setting_type="preference" fw="1.04,1.05">
    <Help>
If this option is enabled, using the selected color preset in SmartApps such as Smart Lighting will result in a random color.
    </Help>
    <Item label="Soft White - Default" value="0" />
    <Item label="White - Concentrate" value="1" />
    <Item label="Daylight - Energize" value="2" />
    <Item label="Warm White - Relax" value="3" />
  </Value>
    <Value type="boolean" index="enableDebugging" label="Enable Debug Logging?" value="true" setting_type="preference" fw="1.04,1.05">
    <Help>

    </Help>
  </Value>
</configuration>
'''
}
