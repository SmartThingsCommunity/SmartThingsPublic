/**
 *  Device Type Definition File
 *
 *  Device Type:		Fibaro RGBW Controller
 *  File Name:			fibaro-rgbw-controller.groovy
 *	Initial Release:	2015-01-04
 *	Author:				Todd Wackford
 *  Email:				todd@wackford.net
 *
 *  Copyright 2015 SmartThings
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

	definition (name: "Fibaro RGBW Controller", namespace: "smartthings", author: "Todd Wackford") {
		capability "Switch Level"
		capability "Actuator"
		capability "Switch"
		capability "Polling"
		capability "Refresh"
		capability "Sensor"
		capability "Configuration"
		capability "Color Control"
        capability "Power Meter"

        command "getDeviceData"
        command "softwhite"
        command "daylight"
        command "warmwhite"
        command "red"
        command "green"
        command "blue"
        command "cyan"
        command "magenta"
        command "orange"
        command "purple"
        command "yellow"
        command "white"
        command "fireplace"
        command "storm"
        command "deepfade"
        command "litefade"
        command "police"
        command "setAdjustedColor"
        command "setWhiteLevel"
        command "test"

        attribute "whiteLevel", "string"

		fingerprint deviceId: "0x1101", inClusters: "0x27,0x72,0x86,0x26,0x60,0x70,0x32,0x31,0x85,0x33"
	}

    simulator {
    	status "on":  "command: 2003, payload: FF"
    	status "off": "command: 2003, payload: 00"
    	status "09%": "command: 2003, payload: 09"
    	status "10%": "command: 2003, payload: 0A"
    	status "33%": "command: 2003, payload: 21"
    	status "66%": "command: 2003, payload: 42"
    	status "99%": "command: 2003, payload: 63"

     	// reply messages
     	reply "2001FF,delay 5000,2602": "command: 2603, payload: FF"
     	reply "200100,delay 5000,2602": "command: 2603, payload: 00"
     	reply "200119,delay 5000,2602": "command: 2603, payload: 19"
     	reply "200132,delay 5000,2602": "command: 2603, payload: 32"
     	reply "20014B,delay 5000,2602": "command: 2603, payload: 4B"
     	reply "200163,delay 5000,2602": "command: 2603, payload: 63"
	}

    tiles {
		controlTile("rgbSelector", "device.color", "color", height: 3, width: 3, inactiveLabel: false) {
            state "color", action:"setAdjustedColor"
		}
		controlTile("levelSliderControl", "device.level", "slider", height: 1, width: 2, inactiveLabel: false, range:"(0..100)") {
			state "level", action:"switch level.setLevel"
		}
		controlTile("whiteSliderControl", "device.whiteLevel", "slider", height: 1, width: 3, inactiveLabel: false) {
            state "whiteLevel", action:"setWhiteLevel", label:'White Level'
        }
		standardTile("switch", "device.switch", width: 1, height: 1, canChangeIcon: true) {
        	state "on", label:'${name}', action:"switch.off", icon:"st.illuminance.illuminance.bright", backgroundColor:"#00A0DC", nextState:"turningOff"
            state "off", label:'${name}', action:"switch.on", icon:"st.illuminance.illuminance.dark", backgroundColor:"#ffffff", nextState:"turningOn"
            state "turningOn", label:'${name}', icon:"st.illuminance.illuminance.bright", backgroundColor:"#00A0DC"
            state "turningOff", label:'${name}', icon:"st.illuminance.illuminance.dark", backgroundColor:"#ffffff"
        }
        valueTile("power", "device.power", decoration: "flat") {
            state "power", label:'${currentValue} W'
        }
        standardTile("configure", "device.configure", inactiveLabel: false, decoration: "flat") {
			state "configure", label:'', action:"configuration.configure", icon:"st.secondary.configure"
		}
        standardTile("refresh", "device.switch", height: 1, inactiveLabel: false, decoration: "flat") {
            state "default", label:"", action:"refresh.refresh", icon:"st.secondary.refresh"
        }
        standardTile("softwhite", "device.softwhite", height: 1, inactiveLabel: false, canChangeIcon: false) {
            state "offsoftwhite", label:"soft white", action:"softwhite", icon:"st.illuminance.illuminance.dark", backgroundColor:"#D8D8D8"
            state "onsoftwhite", label:"soft white", action:"softwhite", icon:"st.illuminance.illuminance.bright", backgroundColor:"#FFF1E0"
        }
        standardTile("daylight", "device.daylight", height: 1, inactiveLabel: false, canChangeIcon: false) {
            state "offdaylight", label:"daylight", action:"daylight", icon:"st.illuminance.illuminance.dark", backgroundColor:"#D8D8D8"
            state "ondaylight", label:"daylight", action:"daylight", icon:"st.illuminance.illuminance.bright", backgroundColor:"#FFFFFB"
        }
        standardTile("warmwhite", "device.warmwhite", height: 1, inactiveLabel: false, canChangeIcon: false) {
            state "offwarmwhite", label:"warm white", action:"warmwhite", icon:"st.illuminance.illuminance.dark", backgroundColor:"#D8D8D8"
            state "onwarmwhite", label:"warm white", action:"warmwhite", icon:"st.illuminance.illuminance.bright", backgroundColor:"#FFF4E5"
        }
        standardTile("red", "device.red", height: 1, inactiveLabel: false, canChangeIcon: false) {
            state "offred", label:"red", action:"red", icon:"st.illuminance.illuminance.dark", backgroundColor:"#D8D8D8"
            state "onred", label:"red", action:"red", icon:"st.illuminance.illuminance.bright", backgroundColor:"#FF0000"
        }
        standardTile("green", "device.green", height: 1, inactiveLabel: false, canChangeIcon: false) {
            state "offgreen", label:"green", action:"green", icon:"st.illuminance.illuminance.dark", backgroundColor:"#D8D8D8"
            state "ongreen", label:"green", action:"green", icon:"st.illuminance.illuminance.bright", backgroundColor:"#00FF00"
        }
        standardTile("blue", "device.blue", height: 1, inactiveLabel: false, canChangeIcon: false) {
            state "offblue", label:"blue", action:"blue", icon:"st.illuminance.illuminance.dark", backgroundColor:"#D8D8D8"
            state "onblue", label:"blue", action:"blue", icon:"st.illuminance.illuminance.bright", backgroundColor:"#0000FF"
        }
        standardTile("cyan", "device.cyan", height: 1, inactiveLabel: false, canChangeIcon: false) {
            state "offcyan", label:"cyan", action:"cyan", icon:"st.illuminance.illuminance.dark", backgroundColor:"#D8D8D8"
            state "oncyan", label:"cyan", action:"cyan", icon:"st.illuminance.illuminance.bright", backgroundColor:"#00FFFF"
        }
        standardTile("magenta", "device.magenta", height: 1, inactiveLabel: false, canChangeIcon: false) {
            state "offmagenta", label:"magenta", action:"magenta", icon:"st.illuminance.illuminance.dark", backgroundColor:"#D8D8D8"
            state "onmagenta", label:"magenta", action:"magenta", icon:"st.illuminance.illuminance.bright", backgroundColor:"#FF00FF"
        }
        standardTile("orange", "device.orange", height: 1, inactiveLabel: false, canChangeIcon: false) {
            state "offorange", label:"orange", action:"orange", icon:"st.illuminance.illuminance.dark", backgroundColor:"#D8D8D8"
            state "onorange", label:"orange", action:"orange", icon:"st.illuminance.illuminance.bright", backgroundColor:"#FF6600"
        }
        standardTile("purple", "device.purple", height: 1, inactiveLabel: false, canChangeIcon: false) {
            state "offpurple", label:"purple", action:"purple", icon:"st.illuminance.illuminance.dark", backgroundColor:"#D8D8D8"
            state "onpurple", label:"purple", action:"purple", icon:"st.illuminance.illuminance.bright", backgroundColor:"#BF00FF"
        }
        standardTile("yellow", "device.yellow", height: 1, inactiveLabel: false, canChangeIcon: false) {
            state "offyellow", label:"yellow", action:"yellow", icon:"st.illuminance.illuminance.dark", backgroundColor:"#D8D8D8"
            state "onyellow", label:"yellow", action:"yellow", icon:"st.illuminance.illuminance.bright", backgroundColor:"#FFFF00"
        }
        standardTile("white", "device.white", height: 1, inactiveLabel: false, canChangeIcon: false) {
            state "offwhite", label:"White", action:"white", icon:"st.illuminance.illuminance.dark", backgroundColor:"#D8D8D8"
            state "onwhite", label:"White", action:"white", icon:"st.illuminance.illuminance.bright", backgroundColor:"#FFFFFF"
        }
        standardTile("fireplace", "device.fireplace", height: 1, inactiveLabel: false, canChangeIcon: false) {
            state "offfireplace", label:"Fire Place", action:"fireplace", icon:"st.illuminance.illuminance.dark", backgroundColor:"#D8D8D8"
            state "onfireplace", label:"Fire Place", action:"fireplace", icon:"st.illuminance.illuminance.bright", backgroundColor:"#FFFFFF"
        }
        standardTile("storm", "device.storm", height: 1, inactiveLabel: false, canChangeIcon: false) {
            state "offstorm", label:"storm", action:"storm", icon:"st.illuminance.illuminance.dark", backgroundColor:"#D8D8D8"
            state "onstorm", label:"storm", action:"storm", icon:"st.illuminance.illuminance.bright", backgroundColor:"#FFFFFF"
        }
        standardTile("deepfade", "device.deepfade", height: 1, inactiveLabel: false, canChangeIcon: false) {
            state "offdeepfade", label:"deep fade", action:"deepfade", icon:"st.illuminance.illuminance.dark", backgroundColor:"#D8D8D8"
            state "ondeepfade", label:"deep fade", action:"deepfade", icon:"st.illuminance.illuminance.bright", backgroundColor:"#FFFFFF"
        }
        standardTile("litefade", "device.litefade", height: 1, inactiveLabel: false, canChangeIcon: false) {
            state "offlitefade", label:"lite fade", action:"litefade", icon:"st.illuminance.illuminance.dark", backgroundColor:"#D8D8D8"
            state "onlitefade", label:"lite fade", action:"litefade", icon:"st.illuminance.illuminance.bright", backgroundColor:"#FFFFFF"
        }
        standardTile("police", "device.police", height: 1, inactiveLabel: false, canChangeIcon: false) {
            state "offpolice", label:"police", action:"police", icon:"st.illuminance.illuminance.dark", backgroundColor:"#D8D8D8"
            state "onpolice", label:"police", action:"police", icon:"st.illuminance.illuminance.bright", backgroundColor:"#FFFFFF"
        }
        controlTile("saturationSliderControl", "device.saturation", "slider", height: 1, width: 2, inactiveLabel: false) {
			state "saturation", action:"color control.setSaturation"
		}
		valueTile("saturation", "device.saturation", inactiveLabel: false, decoration: "flat") {
			state "saturation", label: 'Sat ${currentValue}    '
		}
		controlTile("hueSliderControl", "device.hue", "slider", height: 1, width: 2, inactiveLabel: false) {
			state "hue", action:"color control.setHue"
		}
		valueTile("hue", "device.hue", inactiveLabel: false, decoration: "flat") {
			state "hue", label: 'Hue ${currentValue}   '
		}

        main(["switch"])
        details(["switch",
                 "levelSliderControl",
                 "rgbSelector",
                 "whiteSliderControl",
                 /*"softwhite",
                 "daylight",
                 "warmwhite",
                 "red",
                 "green",
                 "blue",
                 "white",
                 "cyan",
                 "magenta",
                 "orange",
                 "purple",
                 "yellow",
                 "fireplace",
                 "storm",
                 "deepfade",
                 "litefade",
                 "police",
                 "power",
                 "configure",*/
                 "refresh"])
	}
}

def setAdjustedColor(value) {
	log.debug "setAdjustedColor: ${value}"

    toggleTiles("off") //turn off the hard color tiles

    def level = device.latestValue("level")
    if(level == null)
    	level = 50
    log.debug "level is: ${level}"
    value.level = level

	def c = hexToRgb(value.hex)
	value.rh = hex(c.r * (level/100))
	value.gh = hex(c.g * (level/100))
	value.bh = hex(c.b * (level/100))

    setColor(value)
}

def setColor(value) {
	log.debug "setColor: ${value}"
    log.debug "hue is: ${value.hue}"
    log.debug "saturation is: ${value.saturation}"

    if (value.size() < 8)
    	toggleTiles("off")

    if (( value.size() == 2) && (value.hue != null) && (value.saturation != null)) { //assuming we're being called from outside of device (App)
    	def rgb = hslToRGB(value.hue, value.saturation, 0.5)
        value.hex = rgbToHex(rgb)
        value.rh = hex(rgb.r)
        value.gh = hex(rgb.g)
        value.bh = hex(rgb.b)
    }

    if ((value.size() == 3) && (value.hue != null) && (value.saturation != null) && (value.level)) { //user passed in a level value too from outside (App)
    	def rgb = hslToRGB(value.hue, value.saturation, 0.5)
        value.hex = rgbToHex(rgb)
        value.rh = hex(rgb.r * value.level/100)
        value.gh = hex(rgb.g * value.level/100)
        value.bh = hex(rgb.b * value.level/100)
    }

    if (( value.size() == 1) && (value.hex)) { //being called from outside of device (App) with only hex
		def rgbInt = hexToRgb(value.hex)
        value.rh = hex(rgbInt.r)
        value.gh = hex(rgbInt.g)
        value.bh = hex(rgbInt.b)
    }

    if (( value.size() == 2) && (value.hex) && (value.level)) { //being called from outside of device (App) with only hex and level

        def rgbInt = hexToRgb(value.hex)
        value.rh = hex(rgbInt.r * value.level/100)
        value.gh = hex(rgbInt.g * value.level/100)
        value.bh = hex(rgbInt.b * value.level/100)
    }

    if (( value.size() == 1) && (value.colorName)) { //being called from outside of device (App) with only color name
        def colorData = getColorData(value.colorName)
        value.rh = colorData.rh
        value.gh = colorData.gh
        value.bh = colorData.bh
        value.hex = "#${value.rh}${value.gh}${value.bh}"
    }

    if (( value.size() == 2) && (value.colorName) && (value.level)) { //being called from outside of device (App) with only color name and level
		def colorData = getColorData(value.colorName)
        value.rh = hex(colorData.r * value.level/100)
        value.gh = hex(colorData.g * value.level/100)
        value.bh = hex(colorData.b * value.level/100)
        value.hex = "#${hex(colorData.r)}${hex(colorData.g)}${hex(colorData.b)}"
    }

    if (( value.size() == 3) && (value.red != null) && (value.green != null) && (value.blue != null)) { //being called from outside of device (App) with only color values (0-255)
        value.rh = hex(value.red)
        value.gh = hex(value.green)
        value.bh = hex(value.blue)
        value.hex = "#${value.rh}${value.gh}${value.bh}"
    }

    if (( value.size() == 4) && (value.red != null) && (value.green != null) && (value.blue != null) && (value.level)) { //being called from outside of device (App) with only color values (0-255) and level
        value.rh = hex(value.red * value.level/100)
        value.gh = hex(value.green * value.level/100)
        value.bh = hex(value.blue * value.level/100)
        value.hex = "#${hex(value.red)}${hex(value.green)}${hex(value.blue)}"
    }

    if(value.hue) {
        sendEvent(name: "hue", value: value.hue, displayed: false)
    }
    if(value.saturation) {
        sendEvent(name: "saturation", value: value.saturation, displayed: false)
    }
    if(value.hex?.trim()) {
        sendEvent(name: "color", value: value.hex, displayed: false)
    }
    if (value.level) {
        sendEvent(name: "level", value: value.level)
    }
    if (value.switch?.trim()) {
        sendEvent(name: "switch", value: value.switch)
    }

    sendRGB(value.rh, value.gh, value.bh)
}

def setLevel(level, rate = null) {
	log.debug "setLevel($level)"

	if (level == 0) { off() }
	else if (device.latestValue("switch") == "off") { on() }

    def colorHex = device.latestValue("color")
    if (colorHex == null)
		colorHex = "#FFFFFF"

    def c = hexToRgb(colorHex)

    def r = hex(c.r * (level/100))
    def g = hex(c.g * (level/100))
    def b = hex(c.b * (level/100))

	sendEvent(name: "level", value: level)
    sendEvent(name: "setLevel", value: level, displayed: false)
	sendRGB(r, g, b)
}


def setWhiteLevel(value) {
	log.debug "setWhiteLevel: ${value}"
    def level = Math.min(value as Integer, 99)
    level = 255 * level/99 as Integer
    def channel = 0

	if (device.latestValue("switch") == "off") { on() }

    sendEvent(name: "whiteLevel", value: value)
    sendWhite(channel, value)
}

def sendWhite(channel, value) {
	def whiteLevel = hex(value)
    def cmd = [String.format("3305010${channel}${whiteLevel}%02X", 50)]
    cmd
}

def sendRGB(redHex, greenHex, blueHex) {
    def cmd = [String.format("33050302${redHex}03${greenHex}04${blueHex}%02X", 100),]
    cmd
}


def sendRGBW(redHex, greenHex, blueHex, whiteHex) {
    def cmd = [String.format("33050400${whiteHex}02${redHex}03${greenHex}04${blueHex}%02X", 100),]
    cmd
}


def configure() {
	log.debug "Configuring Device For SmartThings Use"



    def cmds = []

    // send associate to group 3 to get sensor data reported only to hub
    cmds << zwave.associationV2.associationSet(groupingIdentifier:5, nodeId:[zwaveHubNodeId]).format()


    //cmds << sendEvent(name: "level", value: 50)
    //cmds << on()
    //cmds << doColorButton("Green")
    delayBetween(cmds, 500)

}

def parse(String description) {
	//log.debug "description: ${description}"
	def item1 = [
		canBeCurrentState: false,
		linkText: getLinkText(device),
		isStateChange: false,
		displayed: false,
		descriptionText: description,
		value:  description
	]
	def result
	def cmd = zwave.parse(description, [0x20: 1, 0x26: 1, 0x70: 2, 0x72: 2, 0x60: 3, 0x33: 2, 0x32: 3, 0x31:2, 0x30: 2, 0x86: 1, 0x7A: 1])

    if (cmd) {
        if ( cmd.CMD != "7006" ) {
        	result = createEvent(cmd, item1)
        }
	}
	else {
		item1.displayed = displayed(description, item1.isStateChange)
		result = [item1]
	}
	//log.debug "Parse returned ${result?.descriptionText}"
	result
}

def getDeviceData() {
    def cmd = []

    cmd << response(zwave.manufacturerSpecificV2.manufacturerSpecificGet())
    cmd << response(zwave.versionV1.versionGet())
	cmd << response(zwave.firmwareUpdateMdV1.firmwareMdGet())

    delayBetween(cmd, 500)
}

def createEvent(physicalgraph.zwave.commands.manufacturerspecificv2.ManufacturerSpecificReport cmd, Map item1) {
    log.debug "manufacturerName: ${cmd.manufacturerName}"
	log.debug "manufacturerId:   ${cmd.manufacturerId}"
    log.debug "productId:        ${cmd.productId}"
    log.debug "productTypeId:    ${cmd.productTypeId}"
}

def createEvent(physicalgraph.zwave.commands.versionv1.VersionReport cmd, Map item1) {
    updateDataValue("applicationVersion", "${cmd.applicationVersion}")
    log.debug "applicationVersion:      ${cmd.applicationVersion}"
    log.debug "applicationSubVersion:   ${cmd.applicationSubVersion}"
    log.debug "zWaveLibraryType:        ${cmd.zWaveLibraryType}"
    log.debug "zWaveProtocolVersion:    ${cmd.zWaveProtocolVersion}"
    log.debug "zWaveProtocolSubVersion: ${cmd.zWaveProtocolSubVersion}"
}

def createEvent(physicalgraph.zwave.commands.firmwareupdatemdv1.FirmwareMdReport cmd, Map item1) {
    log.debug "checksum:       ${cmd.checksum}"
    log.debug "firmwareId:     ${cmd.firmwareId}"
    log.debug "manufacturerId: ${cmd.manufacturerId}"
}

def zwaveEvent(physicalgraph.zwave.commands.colorcontrolv1.CapabilityReport cmd, Map item1) {

	log.debug "In CapabilityReport"
}

def createEvent(physicalgraph.zwave.commands.multichannelv3.MultiChannelCmdEncap cmd, Map item1) {
	def encapsulatedCommand = cmd.encapsulatedCommand([0x26: 1, 0x30: 2, 0x32: 2, 0x33: 2]) // can specify command class versions here like in zwave.parse
	//log.debug ("Command from endpoint ${cmd.sourceEndPoint}: ${encapsulatedCommand}")
	if ((cmd.sourceEndPoint >= 1) && (cmd.sourceEndPoint <= 5)) { // we don't need color report
    	//don't do anything
    } else {
    	if (encapsulatedCommand) {
			zwaveEvent(encapsulatedCommand)
        }
	}
}

def createEvent(physicalgraph.zwave.commands.basicv1.BasicReport cmd, Map item1) {
     def result = doCreateEvent(cmd, item1)
     for (int i = 0; i < result.size(); i++) {
          result[i].type = "physical"
     }
     result
}

def createEvent(physicalgraph.zwave.commands.basicv1.BasicSet cmd, Map item1) {
     def result = doCreateEvent(cmd, item1)
     for (int i = 0; i < result.size(); i++) {
          result[i].type = "physical"
     }
     result
}

def createEvent(physicalgraph.zwave.commands.sensormultilevelv2.SensorMultilevelReport cmd, Map item1) {
    def result = [:]
    if ( cmd.sensorType == 4 ) { //power level comming in
   		result.name = "power"
    	result.value = cmd.scaledSensorValue
    	result.descriptionText = "$device.displayName power usage is ${result.value} watt(s)"
        result.isStateChange
        sendEvent(name: result.name, value: result.value, displayed: false)
    }
    result
}

def createEvent(physicalgraph.zwave.commands.switchmultilevelv1.SwitchMultilevelStartLevelChange cmd, Map item1) {
     []
}

def createEvent(physicalgraph.zwave.commands.switchmultilevelv1.SwitchMultilevelStopLevelChange cmd, Map item1) {
     [response(zwave.basicV1.basicGet())]
}

def createEvent(physicalgraph.zwave.commands.switchmultilevelv3.SwitchMultilevelSet cmd, Map item1) {
     def result = doCreateEvent(cmd, item1)
     for (int i = 0; i < result.size(); i++) {
          result[i].type = "physical"
     }
     result
}

def createEvent(physicalgraph.zwave.commands.switchmultilevelv1.SwitchMultilevelReport cmd, Map item1) {
     def result = doCreateEvent(cmd, item1)
     result[0].descriptionText = "${item1.linkText} is ${item1.value}"
     result[0].handlerName = cmd.value ? "statusOn" : "statusOff"
     for (int i = 0; i < result.size(); i++) {
          result[i].type = "digital"
     }
     result
}

def doCreateEvent(physicalgraph.zwave.Command cmd, Map item1) {
     def result = [item1]

     item1.name = "switch"
     item1.value = cmd.value ? "on" : "off"
     item1.handlerName = item1.value
     item1.descriptionText = "${item1.linkText} was turned ${item1.value}"
     item1.canBeCurrentState = true
     item1.isStateChange = isStateChange(device, item1.name, item1.value)
     item1.displayed = item1.isStateChange

     if (cmd.value >= 5) {
          def item2 = new LinkedHashMap(item1)
          item2.name = "level"
          item2.value = (cmd.value == 99 ? 100 : cmd.value) as String
          item2.unit = "%"
          item2.descriptionText = "${item1.linkText} dimmed ${item2.value} %"
          item2.canBeCurrentState = true
          item2.isStateChange = isStateChange(device, item2.name, item2.value)
          item2.displayed = false
          result << item2
     }
     result
}

def zwaveEvent(physicalgraph.zwave.commands.configurationv1.ConfigurationReport cmd, item1) {
	log.debug "${device.displayName} parameter '${cmd.parameterNumber}' with a byte size of '${cmd.size}' is set to '${cmd.configurationValue}'"
}
/*
def zwaveEvent(physicalgraph.zwave.commands.configurationv1.ConfigurationReport cmd) {
     log.debug "Report: $cmd"
     def value = "when off"
     if (cmd.configurationValue[0] == 1) {value = "when on"}
     if (cmd.configurationValue[0] == 2) {value = "never"}
     [name: "indicatorStatus", value: value, displayed: false]
}
*/
def createEvent(physicalgraph.zwave.Command cmd,  Map map) {
     // Handles any Z-Wave commands we aren't interested in
     log.debug "UNHANDLED COMMAND $cmd"
}

def on() {
	log.debug "on()"
	sendEvent(name: "switch", value: "on")
	delayBetween([zwave.basicV1.basicSet(value: 0xFF).format(),
    			  zwave.switchMultilevelV1.switchMultilevelGet().format()], 5000)
}

def off() {
	log.debug "off()"
	sendEvent(name: "switch", value: "off")
	delayBetween ([zwave.basicV1.basicSet(value: 0x00).format(), zwave.switchMultilevelV1.switchMultilevelGet().format()], 5000)
}


def poll() {
    zwave.switchMultilevelV1.switchMultilevelGet().format()
}

def refresh() {
	def cmd = []
	cmd << response(zwave.switchMultilevelV1.switchMultilevelGet().format())
    delayBetween(cmd, 500)
}

 /**
 * This method will allow the user to update device parameters (behavior) from an app.
 * A "Zwave Tweaker" app will be developed as an interface to do this. Or the user can
 * write his/her own app to envoke this method. No type or value checking is done to
 * compare to what device capability or reaction. It is up to user to read OEM
 * documentation prio to envoking this method.
 *
 * <p>THIS IS AN ADVANCED OPERATION. USE AT YOUR OWN RISK! READ OEM DOCUMENTATION!
 *
 * @param List[paramNumber:80,value:10,size:1]
 *
 *
 * @return none
 */
def updateZwaveParam(params) {
	if ( params ) {
        def pNumber = params.paramNumber
        def pSize	= params.size
        def pValue	= [params.value]
        log.debug "Updating ${device.displayName} parameter number '${pNumber}' with value '${pValue}' with size of '${pSize}'"

		def cmds = []
        cmds << zwave.configurationV1.configurationSet(configurationValue: pValue, parameterNumber: pNumber, size: pSize).format()

        cmds << zwave.configurationV1.configurationGet(parameterNumber: pNumber).format()
        delayBetween(cmds, 1500)
    }
}

def test() {
	//def value = [:]
    //value = [hue: 0, saturation: 100, level: 5]
    //value = [red: 255, green: 0, blue: 255, level: 60]
	//setColor(value)

    def cmd = []

    if ( !state.cnt ) {
    	state.cnt = 6
    } else {
    	state.cnt = state.cnt + 1
    }

    if ( state.cnt > 10 )
    	state.cnt = 6

    // run programmed light show
	cmd << zwave.configurationV1.configurationSet(configurationValue: [state.cnt], parameterNumber: 72, size: 1).format()
    cmd << zwave.configurationV1.configurationGet(parameterNumber: 72).format()

    delayBetween(cmd, 500)

}

def colorNameToRgb(color) {

	final colors = [
        [name:"Soft White",	r: 255, g: 241, b: 224	],
        [name:"Daylight", 	r: 255, g: 255, b: 251	],
        [name:"Warm White", r: 255, g: 244, b: 229	],

        [name:"Red", 		r: 255, g: 0,	b: 0	],
		[name:"Green", 		r: 0, 	g: 255,	b: 0	],
        [name:"Blue", 		r: 0, 	g: 0,	b: 255	],

		[name:"Cyan", 		r: 0, 	g: 255,	b: 255	],
        [name:"Magenta", 	r: 255, g: 0,	b: 33	],
        [name:"Orange", 	r: 255, g: 102, b: 0	],

        [name:"Purple", 	r: 170, g: 0,	b: 255	],
		[name:"Yellow", 	r: 255, g: 255, b: 0	],
        [name:"White", 		r: 255, g: 255, b: 255	]
	]

    def colorData = [:]
    colorData = colors.find { it.name == color }

    colorData
}

private hex(value, width=2) {
	def s = new BigInteger(Math.round(value).toString()).toString(16)
	while (s.size() < width) {
		s = "0" + s
	}
	s
}

def hexToRgb(colorHex) {
	def rrInt = Integer.parseInt(colorHex.substring(1,3),16)
    def ggInt = Integer.parseInt(colorHex.substring(3,5),16)
    def bbInt = Integer.parseInt(colorHex.substring(5,7),16)

    def colorData = [:]
    colorData = [r: rrInt, g: ggInt, b: bbInt]
    colorData
}

def rgbToHex(rgb) {
    def r = hex(rgb.r)
    def g = hex(rgb.g)
    def b = hex(rgb.b)
    def hexColor = "#${r}${g}${b}"

    hexColor
}

def hslToRGB(float var_h, float var_s, float var_l) {
	float h = var_h / 100
    float s = var_s / 100
    float l = var_l

    def r = 0
    def g = 0
    def b = 0

	if (s == 0) {
    	r = l * 255
        g = l * 255
        b = l * 255
	} else {
    	float var_2 = 0
    	if (l < 0.5) {
        	var_2 = l * (1 + s)
        } else {
        	var_2 = (l + s) - (s * l)
        }

        float var_1 = 2 * l - var_2

        r = 255 * hueToRgb(var_1, var_2, h + (1 / 3))
        g = 255 * hueToRgb(var_1, var_2, h)
        b = 255 * hueToRgb(var_1, var_2, h - (1 / 3))
    }

    def rgb = [:]
    rgb = [r: r, g: g, b: b]

    rgb
}

def hueToRgb(v1, v2, vh) {
	if (vh < 0) { vh += 1 }
	if (vh > 1) { vh -= 1 }
	if ((6 * vh) < 1) { return (v1 + (v2 - v1) * 6 * vh) }
    if ((2 * vh) < 1) { return (v2) }
    if ((3 * vh) < 2) { return (v1 + (v2 - v1) * ((2 / 3 - vh) * 6)) }
    return (v1)
}

def rgbToHSL(rgb) {
	def r = rgb.r / 255
    def g = rgb.g / 255
    def b = rgb.b / 255
    def h = 0
    def s = 0
    def l = 0

    def var_min = [r,g,b].min()
    def var_max = [r,g,b].max()
    def del_max = var_max - var_min

    l = (var_max + var_min) / 2

    if (del_max == 0) {
            h = 0
            s = 0
    } else {
    	if (l < 0.5) { s = del_max / (var_max + var_min) }
        else { s = del_max / (2 - var_max - var_min) }

        def del_r = (((var_max - r) / 6) + (del_max / 2)) / del_max
        def del_g = (((var_max - g) / 6) + (del_max / 2)) / del_max
        def del_b = (((var_max - b) / 6) + (del_max / 2)) / del_max

        if (r == var_max) { h = del_b - del_g }
        else if (g == var_max) { h = (1 / 3) + del_r - del_b }
        else if (b == var_max) { h = (2 / 3) + del_g - del_r }

		if (h < 0) { h += 1 }
        if (h > 1) { h -= 1 }
	}
    def hsl = [:]
    hsl = [h: h * 100, s: s * 100, l: l]

    hsl
}

def getColorData(colorName) {
	log.debug "getColorData: ${colorName}"

    def colorRGB = colorNameToRgb(colorName)
    def colorHex = rgbToHex(colorRGB)
	def colorHSL = rgbToHSL(colorRGB)

    def colorData = [:]
    colorData = [h: colorHSL.h,
    			 s: colorHSL.s,
                 l: device.latestValue("level"),
                 r: colorRGB.r,
                 g: colorRGB.g,
                 b: colorRGB.b,
                 rh: hex(colorRGB.r),
                 gh: hex(colorRGB.g),
                 bh: hex(colorRGB.b),
                 hex: colorHex,
                 alpha: 1]

     colorData
}

def doColorButton(colorName) {
    log.debug "doColorButton: '${colorName}()'"

    if (device.latestValue("switch") == "off") { on() }

    def level = device.latestValue("level")
    def maxLevel = hex(99)

    toggleTiles(colorName.toLowerCase().replaceAll("\\s",""))

    if 		( colorName == "Fire Place" ) 	{ updateZwaveParam([paramNumber:72, value:6,  size:1]) }
	else if ( colorName == "Storm" ) 		{ updateZwaveParam([paramNumber:72, value:7,  size:1]) }
    else if ( colorName == "Deep Fade" ) 	{ updateZwaveParam([paramNumber:72, value:8,  size:1]) }
    else if ( colorName == "Lite Fade" ) 	{ updateZwaveParam([paramNumber:72, value:9,  size:1]) }
    else if ( colorName == "Police" ) 		{ updateZwaveParam([paramNumber:72, value:10, size:1]) }
    else if ( colorName == "White" ) 		{ String.format("33050400${maxLevel}02${hex(0)}03${hex(0)}04${hex(0)}%02X", 100) }
    else if ( colorName == "Daylight" ) 	{ String.format("33050400${maxLevel}02${maxLevel}03${maxLevel}04${maxLevel}%02X", 100) }
    else {
		def c = getColorData(colorName)
		def newValue = ["hue": c.h, "saturation": c.s, "level": level, "red": c.r, "green": c.g, "blue": c.b, "hex": c.hex, "alpha": c.alpha]
    	setColor(newValue)
    	def r = hex(c.r * (level/100))
    	def g = hex(c.g * (level/100))
    	def b = hex(c.b * (level/100))
        def w = hex(0) //to turn off white channel with toggling tiles
		sendRGBW(r, g, b, w)
    }
}

def toggleTiles(color) {
	state.colorTiles = []
	if ( !state.colorTiles ) {
    	state.colorTiles = ["softwhite","daylight","warmwhite","red","green","blue","cyan","magenta","orange","purple","yellow","white","fireplace","storm","deepfade","litefade","police"]
    }

    def cmds = []

    state.colorTiles.each({
    	if ( it == color ) {
        	log.debug "Turning ${it} on"
            cmds << sendEvent(name: it, value: "on${it}", displayed: True, descriptionText: "${device.displayName} ${color} is 'ON'", isStateChange: true)
        } else {
        	//log.debug "Turning ${it} off"
        	cmds << sendEvent(name: it, value: "off${it}", displayed: false)
        }
    })

    delayBetween(cmds, 2500)
}

// rows of buttons
def softwhite() { doColorButton("Soft White") }
def daylight()  { doColorButton("Daylight") }
def warmwhite() { doColorButton("Warm White") }

def red() 		{ doColorButton("Red") }
def green() 	{ doColorButton("Green") }
def blue() 		{ doColorButton("Blue") }

def cyan() 		{ doColorButton("Cyan") }
def magenta()	{ doColorButton("Magenta") }
def orange() 	{ doColorButton("Orange") }

def purple()	{ doColorButton("Purple") }
def yellow() 	{ doColorButton("Yellow") }
def white() 	{ doColorButton("White") }

def fireplace() { doColorButton("Fire Place") }
def storm() 	{ doColorButton("Storm") }
def deepfade() 	{ doColorButton("Deep Fade") }

def litefade() 	{ doColorButton("Lite Fade") }
def police() 	{ doColorButton("Police") }
