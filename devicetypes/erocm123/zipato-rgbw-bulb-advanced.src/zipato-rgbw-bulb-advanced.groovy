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
 *  Zipato RGBW Bulb (Advanced)
 *
 *  Author: Eric Maycock (erocm123)
 *  Date: 2016-11-09
 */
 
metadata {
	definition (name: "Zipato RGBW Bulb (Advanced)", namespace: "erocm123", author: "Eric Maycock", vid:"generic-rgbw-color-bulb") {
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

        fingerprint mfr: "0131", prod: "0002", model: "0002", deviceJoinName: "Zipato Bulb"
              
        fingerprint deviceId: "0x1101", inClusters: "0x5E,0x26,0x85,0x72,0x33,0x70,0x86,0x73,0x59,0x5A,0x7A"

	}
    
    preferences {
        input description: "Once you change values on this page, the corner of the \"configuration\" icon will change orange until all configuration parameters are updated.", title: "Settings", displayDuringSetup: false, type: "paragraph", element: "paragraph"
		generate_preferences(configuration_model())
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
        valueTile("colorTempTile", "device.colorTemperature", height: 2, width: 2) {
        	state "colorTemperature", label:'${currentValue}%', backgroundColor:"#FFFFFF"
        }
        controlTile("colorTempControl", "device.colorTemperature", "slider", decoration: "flat", height: 2, width: 4, inactiveLabel: false) {
		    state "colorTemperature", action:"setColorTemperature"
	    }
		standardTile("switch1", "switch1", canChangeIcon: true, decoration: "flat", width: 2, height: 2) {
            state "off", label: "fast", action: "on1", icon: "st.switches.switch.off", backgroundColor: "#cccccc"
			state "on", label: "fast", action: "off1", icon: "st.switches.switch.on", backgroundColor: "#00a0dc"
		}
        standardTile("switch2", "switch2", canChangeIcon: true, decoration: "flat", width: 2, height: 2) {
            state "off", label: "slow", action: "on2", icon: "st.switches.switch.off", backgroundColor: "#cccccc"
			state "on", label: "slow", action: "off2", icon: "st.switches.switch.on", backgroundColor: "#00a0dc"
		}
        standardTile("switch3", "switch3", canChangeIcon: true, decoration: "flat", width: 2, height: 2) {
            state "off", label: "r.fast", action: "on3", icon: "st.switches.switch.off", backgroundColor: "#cccccc"
			state "on", label: "r.fast", action: "off3", icon: "st.switches.switch.on", backgroundColor: "#00a0dc"
		}
        standardTile("switch4", "switch4", canChangeIcon: true, decoration: "flat", width: 2, height: 2) {
            state "off", label: "r.slow", action: "on4", icon: "st.switches.switch.off", backgroundColor: "#cccccc"
			state "on", label: "r.slow", action: "off4", icon: "st.switches.switch.on", backgroundColor: "#00a0dc"
		}
        standardTile("switch5", "switch5", canChangeIcon: true, decoration: "flat", width: 2, height: 2) {
			state "off", label: "custom", action: "on5", icon: "st.switches.switch.off", backgroundColor: "#cccccc"
            state "on", label: "custom", action: "off5", icon: "st.switches.switch.on", backgroundColor: "#00a0dc"
		}
        standardTile("switch6", "switch6", canChangeIcon: true, decoration: "flat", width: 2, height: 2) {
			state "off", label: "off", action: "on6", icon: "st.switches.switch.off", backgroundColor: "#cccccc"
            state "on", label: "off", action: "off6", icon: "st.switches.switch.on", backgroundColor: "#00a0dc"
		}
        valueTile(
			"currentFirmware", "device.currentFirmware", inactiveLabel: false, width: 2, height: 2) {
			state "currentFirmware", label:'Firmware: v${currentValue}', unit:""
		}
    }

	main(["switch"])
	details(["switch", "levelSliderControl",
             "colorTempControl", "colorTempTile",
             "switch1", "switch2", "switch3",
             "switch4", "switch5", "switch6",
             "refresh", "configure" ])
}
 
/**
* Triggered when Done button is pushed on Preference Pane
*/
def updated()
{
	state.enableDebugging = settings.enableDebugging
    state.sec = zwaveInfo.zw.endsWith("s")? 1:0
    logging("updated() is being called")
    sendEvent(name: "checkInterval", value: 2 * 6 * 60 * 60 + 5 * 60, displayed: false, data: [protocol: "zwave", hubHardwareId: device.hub.hardwareID])
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
    if (cmd.parameterNumber == 4) { 
       if (cmd.configurationValue[0] == 0) toggleTiles("all")
    } else {
       update_current_properties(cmd)
       logging("${device.displayName} parameter '${cmd.parameterNumber}' with a byte size of '${cmd.size}' is set to '${cmd.configurationValue}'")
    }
    
} 

def zwaveEvent(physicalgraph.zwave.commands.firmwareupdatemdv2.FirmwareMdReport cmd){
    logging("Firmware Report ${cmd.toString()}")
    def firmwareVersion
    switch(cmd.checksum){
       default:
          firmwareVersion = cmd.checksum
    }
    state.needfwUpdate = "false"
    updateDataValue("firmware", firmwareVersion.toString())
    createEvent(name: "currentFirmware", value: firmwareVersion)
}
 
def zwaveEvent(physicalgraph.zwave.Command cmd) {
    logging("Unhandled: $cmd")
	def linkText = device.label ?: device.name
	[linkText: linkText, descriptionText: "$linkText: $cmd", displayed: false]
}

private toggleTiles(value) {
   def tiles = ["switch1", "switch2", "switch3", "switch4", "switch5"]
   tiles.each {tile ->
      if (tile != value) { sendEvent(name: tile, value: "off") }
      else { sendEvent(name:tile, value:"on") }
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
        zwave.configurationV1.configurationGet(parameterNumber: 4),
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
    
    def cmds = commands(result)

    if (device.currentValue("switch") != "on") {
        log.debug "Bulb is off. Turning on"
        cmds = [command(zwave.basicV1.basicSet(value: 0xFF))] + cmds + [command(zwave.basicV1.basicGet())]
    }

	if(value.hue) sendEvent(name: "hue", value: value.hue)
	if(value.hex) sendEvent(name: "color", value: value.hex)
	if(value.switch) sendEvent(name: "switch", value: value.switch)
	if(value.saturation) sendEvent(name: "saturation", value: value.saturation)
 
    toggleTiles("all")

	return cmds
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
    if (device.currentValue('switch') != "on") cmds << zwave.basicV1.basicSet(value: 0xFF)
    cmds << zwave.configurationV1.configurationSet(configurationValue: integer2Cmd(1, 1), parameterNumber: 3, size: 1)
    cmds << zwave.configurationV1.configurationSet(configurationValue: integer2Cmd(0, 1), parameterNumber: 5, size: 1)
    cmds << zwave.configurationV1.configurationSet(configurationValue: integer2Cmd(255, 1), parameterNumber: 4, size: 1)
    cmds << zwave.configurationV1.configurationGet(parameterNumber: 4)
    cmds << zwave.basicV1.basicGet()
	commands(cmds)
}

def on2() {
    logging("on2()")  
    toggleTiles("switch2")
    def cmds = []
    if (device.currentValue('switch') != "on") cmds << zwave.basicV1.basicSet(value: 0xFF)
    cmds << zwave.configurationV1.configurationSet(configurationValue: integer2Cmd(25, 1), parameterNumber: 3, size: 1)
    cmds << zwave.configurationV1.configurationSet(configurationValue: integer2Cmd(0, 1), parameterNumber: 5, size: 1)
    cmds << zwave.configurationV1.configurationSet(configurationValue: integer2Cmd(255, 1), parameterNumber: 4, size: 1)
    cmds << zwave.configurationV1.configurationGet(parameterNumber: 4)
    cmds << zwave.basicV1.basicGet()
	commands(cmds)
}

def on3() {
    logging("on3()")
    toggleTiles("switch3")
    def cmds = []
    if (device.currentValue('switch') != "on") cmds << zwave.basicV1.basicSet(value: 0xFF)
    cmds << zwave.configurationV1.configurationSet(configurationValue: integer2Cmd(1, 1), parameterNumber: 3, size: 1)
    cmds << zwave.configurationV1.configurationSet(configurationValue: integer2Cmd(1, 1), parameterNumber: 5, size: 1)
    cmds << zwave.configurationV1.configurationSet(configurationValue: integer2Cmd(255, 1), parameterNumber: 4, size: 1)
    cmds << zwave.configurationV1.configurationGet(parameterNumber: 4)
    cmds << zwave.basicV1.basicGet()
	commands(cmds)
}

def on4() {
    logging("on4()")
    toggleTiles("switch4")
    def cmds = []
    if (device.currentValue('switch') != "on") cmds << zwave.basicV1.basicSet(value: 0xFF)
    cmds << zwave.configurationV1.configurationSet(configurationValue: integer2Cmd(25, 1), parameterNumber: 3, size: 1)
    cmds << zwave.configurationV1.configurationSet(configurationValue: integer2Cmd(1, 1), parameterNumber: 5, size: 1)
    cmds << zwave.configurationV1.configurationSet(configurationValue: integer2Cmd(255, 1), parameterNumber: 4, size: 1)
    cmds << zwave.configurationV1.configurationGet(parameterNumber: 4)
    cmds << zwave.basicV1.basicGet()
	commands(cmds)
} 

def on5() {
    logging("on5()")
    toggleTiles("switch5")
    def cmds = []
    if (device.currentValue('switch') != "on") cmds << zwave.basicV1.basicSet(value: 0xFF)
    cmds << zwave.configurationV1.configurationSet(configurationValue: integer2Cmd(settings."3".toInteger(), 1), parameterNumber: 3, size: 1)
    cmds << zwave.configurationV1.configurationSet(configurationValue: integer2Cmd(settings."5".toInteger(), 1), parameterNumber: 5, size: 1)
    cmds << zwave.configurationV1.configurationSet(configurationValue: integer2Cmd(settings."4".toInteger(), 1), parameterNumber: 4, size: 1)
    cmds << zwave.configurationV1.configurationGet(parameterNumber: 4)
    cmds << zwave.switchMultilevelV3.switchMultilevelGet()
	commands(cmds)
} 

def on6() {
    logging("on6()")
    offCmd()
} 

def offCmd() {
    logging("offCmd()")
    toggleTiles("all")
    def cmds = []
    cmds << zwave.configurationV1.configurationSet(scaledConfigurationValue: 0, parameterNumber: 4, size: 1)
    cmds << zwave.configurationV1.configurationGet(parameterNumber: 4)
    cmds << zwave.basicV1.basicGet()
    commands(cmds)
}

def off1() { offCmd() }
def off2() { offCmd() }
def off3() { offCmd() }
def off4() { offCmd() }
def off5() { offCmd() }
def off6() { offCmd() }

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
       cmds << zwave.firmwareUpdateMdV2.firmwareMdGet()
    }    
    
    configuration.Value.each
    {     
        if ("${it.@setting_type}" == "zwave"){
            if (currentProperties."${it.@index}" == null)
            {
                isUpdateNeeded = "YES"
                logging("Current value of parameter ${it.@index} is unknown")
                cmds << zwave.configurationV1.configurationGet(parameterNumber: it.@index.toInteger())
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
  <Value type="byte" byteSize="1" index="1" label="Color Temperature" min="1" max="99" value="50" setting_type="zwave" fw="">
    <Help>
Adjust color temperature. Values range from 1 to 99 where 1 means full cold white and 99 means full warm white.
Range: 1~99
Default: 50 
    </Help>
  </Value>
  <Value type="byte" byteSize="1" index="2" label="Shock Sensor Sensitivity" min="0" max="31" value="16" setting_type="zwave" fw="">
    <Help>
Adjust shock sensor sensitivity. Values range from 0 to 31 where 0 means minimum sensitivity and 31 means maximum sensitivity 
Range: 0~31
Default: 16
    </Help>
  </Value>
  <Value type="byte" byteSize="1" index="3" label="Strobe Light Interval" min="0" max="25" value="0" setting_type="zwave" fw="">
    <Help>
Adjust strobe light interval. Values range from 0 to 25 in intervals of 100 milliseconds
Range: 0~25
Default: 0
    </Help>
  </Value>
    <Value type="byte" byteSize="1" index="4" label="Strobe Light Pulse Count" min="0" max="255" value="0" setting_type="preference" fw="">
    <Help>
Adjust strobe light pulse count. Values range from 0 to 250 and a special value 255 which sets infinite flashing.
Range: 0~250, 255 (Infinite)
Default: 0
    </Help>
  </Value>
      <Value type="list" byteSize="1" index="5" label="Random Strobe Colors" min="0" max="1" value="0" setting_type="zwave" fw="">
    <Help>
Range: 0~1
Default: 0 (Disabled)
    </Help>
    <Item label="Disable" value="0" />
    <Item label="Enable" value="1" />
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
    <Value type="boolean" index="enableDebugging" label="Enable Debug Logging?" value="true" setting_type="preference" fw="">
    <Help>

    </Help>
  </Value>
</configuration>
'''
}
