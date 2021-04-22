/**
 *  Copyright 2019 Inovelli / Eric Maycock
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
 *  Inovelli Bulb Multi-Color LZW42
 *
 *  Author: Eric Maycock
 *  Date: 2019-9-9
 *  updated by bcopeland 1/7/2020 
 *		Added color pre-staging option
 *		Added power restored memory configuration
 *		Added debug logging configuration
 *		Fixed color setting 
 *		Fixed color temperature setting 
 *		Fixed reporting 
 *		Removed SmartThings related code
 *		Added importURL
 *		Added color name
 *	updated by bcopeland 1/9/2020
 *		added firmware version reporting
 *		fix for scene capture and level in setcolor
 *	updated by bcopeland 1/10/2020
 *		fix for hsl level from received color report
 *  updated by bcopeland 1/21/2020
 *		fixes for reported bugs 
 *		correct comand class versions to match what the hardware supports
 *		add z-wave color component ids manually as it didnt seem to match in correct command class version from he
 *  updated by erocm123
 *    porting over to SmartThings
 */


metadata {
	definition (name: "Inovelli Bulb Multi-Color LZW42", namespace: "InovelliUSA", author: "InovelliUSA", importUrl: "https://raw.githubusercontent.com/InovelliUSA/Hubitat/master/Drivers/inovelli-bulb-multi-color-lzw42.src/inovelli-bulb-multi-color-lzw42.groovy") {
		capability "Switch Level"
		capability "Color Control"
		capability "Color Temperature"
		capability "Switch"
		capability "Refresh"
		capability "Actuator"
		capability "Sensor"
		capability "Health Check"
		capability "Configuration"
		capability "ColorMode"

		attribute "colorName", "String"
		attribute "firmware", "String"

        fingerprint mfr: "031E", prod: "0005", model: "0001", deviceJoinName: "Inovelli Bulb Multi-Color"
        fingerprint deviceId: "0x1101", inClusters: "0x5E,0x85,0x59,0x86,0x72,0x5A,0x33,0x26,0x70,0x27,0x98,0x73,0x7A"
        fingerprint deviceId: "0x1101", inClusters: "0x5E,0x98,0x86,0x85,0x59,0x72,0x73,0x33,0x26,0x70,0x27,0x5A,0x7A" //Secure
	}
	preferences {
        	// added for official hubitat standards
			input name: "colorStaging", type: "bool", description: "", title: "Enable color pre-staging", defaultValue: false
			input name: "logEnable", type: "bool", description: "", title: "Enable Debug Logging", defaultVaule: true
			input name: "bulbMemory", type: "enum", title: "Power outage state", options: [0:"Return to last ON state",1:"Return to last state (ON or OFF)"], defaultValue: 0
	}
	tiles(scale: 2) {
		multiAttributeTile(name:"switch", type: "lighting", width: 1, height: 1, canChangeIcon: true) {
			tileAttribute("device.switch", key: "PRIMARY_CONTROL") {
				attributeState("on", label:'${name}', action:"switch.off", icon:"st.lights.philips.hue-single", backgroundColor:"#00a0dc", nextState:"turningOff")
				attributeState("off", label:'${name}', action:"switch.on", icon:"st.lights.philips.hue-single", backgroundColor:"#ffffff", nextState:"turningOn")
				attributeState("turningOn", label:'${name}', action:"switch.off", icon:"st.lights.philips.hue-single", backgroundColor:"#00a0dc", nextState:"turningOff")
				attributeState("turningOff", label:'${name}', action:"switch.on", icon:"st.lights.philips.hue-single", backgroundColor:"#ffffff", nextState:"turningOn")
			}

			tileAttribute ("device.level", key: "SLIDER_CONTROL") {
				attributeState "level", action:"switch level.setLevel"
			}

			tileAttribute ("device.color", key: "COLOR_CONTROL") {
				attributeState "color", action:"color control.setColor"
			}
		}
	}

	controlTile("colorTempSliderControl", "device.colorTemperature", "slider", width: 4, height: 2, inactiveLabel: false, range:"(2700..6500)") {
		state "colorTemperature", action:"color temperature.setColorTemperature"
	}
    standardTile("refresh", "device.switch", width: 2, height: 2, inactiveLabel: false, decoration: "flat") {
			state "default", label:'', action:"refresh.refresh", icon:"st.secondary.refresh"
		}

	main(["switch"])
	details(["switch", "levelSliderControl", "rgbSelector", "colorTempSliderControl", "refresh"])
	
}

private getCOLOR_TEMP_MIN() { 2700 }
private getCOLOR_TEMP_MAX() { 6500 }
private getWARM_WHITE_CONFIG() { 0x51 }
private getCOLD_WHITE_CONFIG() { 0x52 }
private getRED() { "red" }
private getGREEN() { "green" }
private getBLUE() { "blue" }
private getWARM_WHITE() { "warmWhite" }
private getCOLD_WHITE() { "coldWhite" }
private getRGB_NAMES() { [RED, GREEN, BLUE] }
private getWHITE_NAMES() { [WARM_WHITE, COLD_WHITE] }
private getZWAVE_COLOR_COMPONENT_ID() { [warmWhite: 0, coldWhite: 1, red: 2, green: 3, blue: 4] }

def logsOff(){
    log.warn "debug logging disabled..."
    device.updateSetting("logEnable",[value:"false",type:"bool"])
}

def updated() {
	log.info "updated().."
	log.warn "debug logging is: ${logEnable}"
	log.warn "color staging is: ${colorStaging}"
    def cmds = []
	if (!state.powerStateMem) initializeVars()
	if (state.powerStateMem?.toInteger() != bulbMemory?.toInteger()) cmds << configure() 
	if (logEnable) runIn(1800,logsOff)
    cmds << refresh()
	response(cmds)
}

def installed() {
	if (logEnable) log.debug "installed()..."
	initializeVars()
	sendEvent(name: "checkInterval", value: 1860, displayed: false, data: [protocol: "zwave", hubHardwareId: device.hub.hardwareID, offlinePingable: "0"])
	sendEvent(name: "level", value: 100, unit: "%")
	sendEvent(name: "colorTemperature", value: COLOR_TEMP_MIN)
	sendEvent(name: "color", value: "#000000")
	sendEvent(name: "hue", value: 0)
	sendEvent(name: "saturation", value: 0)
}

def initializeVars() {
	if (!state.colorReceived) state.colorReceived = [red: null, green: null, blue: null, warmWhite: null, coldWhite: null]
	if (!state.powerStateMem) state.powerStateMem=0
}

def configure() {
	def cmds = []
	cmds << zwave.configurationV1.configurationSet([scaledConfigurationValue: bulbMemory?bulbMemory.toInteger():0, parameterNumber: 2, size:1])
	cmds << zwave.configurationV1.configurationGet([parameterNumber: 2])
	commands(cmds)
}

def parse(description) {
	def result = null
	if (description != "updated") {
        def cmd
        try {
		    cmd = zwave.parse(description,[0x33:2,0x26:2,0x86:2,0x70:1])
        } catch (e) {
            //log.debug "An exception was caught $e"
        }
		if (cmd) {
			result = zwaveEvent(cmd)
			if (logEnable) log.debug("'$description' parsed to $result")
		} else {
			if (logEnable) log.debug("Couldn't zwave.parse '$description'")
		}
	}
	result
}

def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicReport cmd) {
    if (logEnable) log.debug cmd
	dimmerEvents(cmd)
}

def zwaveEvent(physicalgraph.zwave.commands.versionv1.VersionReport cmd) {
    if (debugEnable) log.debug "${device.label?device.label:device.name}: ${cmd}"
    if(cmd.applicationVersion != null && cmd.applicationSubVersion != null) {
	    def firmware = "${cmd.applicationVersion}.${cmd.applicationSubVersion.toString().padLeft(2,'0')}"
        if (infoEnable) log.info "${device.label?device.label:device.name}: Firmware report received: ${firmware}"
        state.needfwUpdate = "false"
        createEvent(name: "firmware", value: "${firmware}")
    } else if(cmd.firmware0Version != null && cmd.firmware0SubVersion != null) {
	    def firmware = "${cmd.firmware0Version}.${cmd.firmware0SubVersion.toString().padLeft(2,'0')}"
        if (infoEnable != false) log.info "${device.label?device.label:device.name}: Firmware report received: ${firmware}"
        state.needfwUpdate = "false"
        createEvent(name: "firmware", value: "${firmware}")
    }
}

def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicSet cmd) {
	dimmerEvents(cmd)
}

def zwaveEvent(physicalgraph.zwave.commands.switchmultilevelv2.SwitchMultilevelReport cmd) {
    if (logEnable) log.debug cmd
	unschedule(offlinePing)
	dimmerEvents(cmd)
}

def zwaveEvent(physicalgraph.zwave.commands.switchcolorv2.SwitchColorReport cmd) {
	if (!state.colorReceived) initializeVars()
	if (logEnable) log.debug "got SwitchColorReport: $cmd"
	state.colorReceived[cmd.colorComponent] = cmd.value
	def result = []
	// Check if we got all the RGB color components
	if (RGB_NAMES.every { state.colorReceived[it] != null }) {
		def colors = RGB_NAMES.collect { state.colorReceived[it] }
		if (logEnable) log.debug "colors: $colors"
		// Send the color as hex format
		def hexColor = "#" + colors.collect { Integer.toHexString(it).padLeft(2, "0") }.join("")
		result << createEvent(name: "color", value: hexColor)
		// Send the color as hue and saturation
		def hsv = rgbToHSV(*colors)
		result << createEvent(name: "hue", value: hsv.hue)
		result << createEvent(name: "saturation", value: hsv.saturation)
		
		if ((hsv[0] > 0) && (hsv[1] > 0)) {
			setGenericName(hsv[0])
			result << createEvent(name: "level", value: hsv[2].round())
		}
		// Reset the values
		RGB_NAMES.collect { state.colorReceived[it] = null}
	}
	// Check if we got all the color temperature values
	if (WHITE_NAMES.every { state.colorReceived[it] != null}) {
		def warmWhite = state.colorReceived[WARM_WHITE]
		def coldWhite = state.colorReceived[COLD_WHITE]
		if (logEnable) log.debug "warmWhite: $warmWhite, coldWhite: $coldWhite"
		if (warmWhite == 0 && coldWhite == 0) {
			result = createEvent(name: "colorTemperature", value: COLOR_TEMP_MIN)
		} else {
			def parameterNumber = warmWhite ? WARM_WHITE_CONFIG : COLD_WHITE_CONFIG
			result << response(command(zwave.configurationV1.configurationGet([parameterNumber: parameterNumber])))
			result << response(command(zwave.switchMultilevelV2.switchMultilevelGet()))
		}
		// Reset the values
		WHITE_NAMES.collect { state.colorReceived[it] = null }
	}
	result
}

private dimmerEvents(physicalgraph.zwave.Command cmd) {
	def value = (cmd.value ? "on" : "off")
	def result = [createEvent(name: "switch", value: value, descriptionText: "$device.displayName was turned $value")]
	if (cmd.value) {
		result << createEvent(name: "level", value: cmd.value == 99 ? 100 : cmd.value , unit: "%")
	}
	return result
}

def zwaveEvent(physicalgraph.zwave.commands.securityv1.SecurityMessageEncapsulation cmd) {
	def encapsulatedCommand = cmd.encapsulatedCommand([0x33:2,0x26:2,0x86:2,0x70:1])
	if (encapsulatedCommand) {
		zwaveEvent(encapsulatedCommand)
	} else {
		log.warn "Unable to extract encapsulated cmd from $cmd"
		createEvent(descriptionText: cmd.toString())
	}
}

def zwaveEvent(physicalgraph.zwave.commands.configurationv1.ConfigurationReport cmd) {
	if (logEnable) log.debug "got ConfigurationReport: $cmd"
	def result = null
	if (cmd.parameterNumber == WARM_WHITE_CONFIG || cmd.parameterNumber == COLD_WHITE_CONFIG) {
		result = createEvent(name: "colorTemperature", value: cmd.scaledConfigurationValue)
		setGenericTempName(cmd.scaledConfigurationValue)
	}
		if (cmd.parameterNumber == 0x02) {
		state.powerStateMem = cmd.scaledConfigurationValue
	}
	result
}

def zwaveEvent(physicalgraph.zwave.Command cmd) {
	def linkText = device.label ?: device.name
	[linkText: linkText, descriptionText: "$linkText: $cmd", displayed: false]
}

def buildOffOnEvent(cmd){
	[zwave.basicV1.basicSet(value: cmd), zwave.switchMultilevelV2.switchMultilevelGet()]
}

def rgbToHSV(red, green, blue) {
	def hex = colorUtil.rgbToHex(red as int, green as int, blue as int)
	def hsv = colorUtil.hexToHsv(hex)
	return [hue: hsv[0], saturation: hsv[1], value: hsv[2]]
}

def huesatToRGB(hue, sat) {
	def color = colorUtil.hsvToHex(Math.round(hue) as int, Math.round(sat) as int)
	return colorUtil.hexToRgb(color)
}

def on() {
	commands(buildOffOnEvent(0xFF), 5000)
}

def off() {
	commands(buildOffOnEvent(0x00), 5000)
}

def refresh() {
	commands([zwave.switchMultilevelV2.switchMultilevelGet()] + queryAllColors() + zwave.versionV1.versionGet())
}

def ping() {
	if (logEnable) log.debug "ping().."
	unschedule(offlinePing)
	runEvery30Minutes(offlinePing)
	command(zwave.switchMultilevelV2.switchMultilevelGet())
}

def offlinePing() {
	if (logEnable) log.debug "offlinePing()..."
	sendHubCommand(new physicalgraph.device.HubAction(command(zwave.switchMultilevelV2.switchMultilevelGet())))
}

def setLevel(level) {
	setLevel(level, 1)
}

def setLevel(level, duration) {
	if (logEnable) log.debug "setLevel($level, $duration)"
	if(level > 99) level = 99
	commands([
		zwave.switchMultilevelV2.switchMultilevelSet(value: level, dimmingDuration: duration),
		zwave.switchMultilevelV2.switchMultilevelGet()
	], (duration && duration < 12) ? (duration * 1000).toLong() : 3500)
}

def setSaturation(percent) {
	if (logEnable) log.debug "setSaturation($percent)"
	setColor([saturation: percent, hue: device.currentValue("hue"), level: device.currentValue("level")])
}

def setHue(value) {
	if (logEnable) log.debug "setHue($value)"
	setColor([hue: value, saturation: 100, level: device.currentValue("level")])
}

def setColor(value) {
	if (value.hue == null || value.saturation == null) return
	if (logEnable) log.debug "setColor($value)"
	def result = []
	//def rgb = hsvToRGB([value.hue, value.saturation, value.level?value.level:device.currentValue("level")?device.currentValue("level"):100])
    def rgb = huesatToRGB(value.hue, value.saturation)
    log.debug "r:" + rgb[0] + ", g: " + rgb[1] +", b: " + rgb[2]
	result << zwave.switchColorV2.switchColorSet(red: rgb[0], green: rgb[1], blue: rgb[2], warmWhite:0, coldWhite:0)
	if ((device.currentValue("switch") != "on") && (!colorStaging)){
		if (logEnable) log.debug "Bulb is off. Turning on"
 		result << zwave.basicV1.basicSet(value: 0xFF)
        result << zwave.switchMultilevelV2.switchMultilevelGet()
	}
    commands(result+queryAllColors())
}

def setColorTemperature(temp) {
	if (logEnable) log.debug "setColorTemperature($temp)"
	def warmValue = temp < 5000 ? 255 : 0
	def coldValue = temp >= 5000 ? 255 : 0
	def parameterNumber = temp < 5000 ? WARM_WHITE_CONFIG : COLD_WHITE_CONFIG
	def cmds = []
	cmds << zwave.switchColorV2.switchColorSet(red: 0, green: 0, blue:0, warmWhite: warmValue, coldWhite: coldValue)
	if (temp < COLOR_TEMP_MIN) temp = 2700
	if (temp > COLOR_TEMP_MAX) temp = 6500
	cmds << zwave.configurationV1.configurationSet([scaledConfigurationValue: temp, parameterNumber: parameterNumber, size:2])
	if ((device.currentValue("switch") != "on") && (!colorStaging)) {
		if (logEnable) log.debug "Bulb is off. Turning on"
		cmds << zwave.basicV1.basicSet(value: 0xFF)
		cmds << zwave.switchMultilevelV2.switchMultilevelGet()
	}
	commands(cmds + queryAllColors())
}

private queryAllColors() {
	def colors = WHITE_NAMES + RGB_NAMES
	colors.collect { zwave.switchColorV2.switchColorGet(colorComponent: it, colorComponentId: ZWAVE_COLOR_COMPONENT_ID[it])}
}

private secEncap(physicalgraph.zwave.Command cmd) {
	zwave.securityV1.securityMessageEncapsulation().encapsulate(cmd).format()
}

private crcEncap(physicalgraph.zwave.Command cmd) {
	zwave.crc16EncapV1.crc16Encap().encapsulate(cmd).format()
}

private command(physicalgraph.zwave.Command cmd) {
	if (getDataValue("zwaveSecurePairingComplete") == "true") {
		return zwave.securityV1.securityMessageEncapsulation().encapsulate(cmd).format()
    } else {
		return cmd.format()
    }	
}

private commands(commands, delay=200) {
	delayBetween(commands.collect{ command(it) }, delay)
}

def zwaveEvent(physicalgraph.zwave.commands.associationv2.AssociationReport cmd) {
    def temp = []
    if (cmd.nodeId != []) {
       cmd.nodeId.each {
          temp += it.toString().format( '%02x', it.toInteger() ).toUpperCase()
       }
    } 
    state."actualAssociation${cmd.groupingIdentifier}" = temp
    if (logEnable) log.debug "Associations for Group ${cmd.groupingIdentifier}: ${temp}"
    updateDataValue("associationGroup${cmd.groupingIdentifier}", "$temp")
}

def zwaveEvent(physicalgraph.zwave.commands.associationv2.AssociationGroupingsReport cmd) {
    sendEvent(name: "groups", value: cmd.supportedGroupings)
    if (logEnable) log.debug "Supported association groups: ${cmd.supportedGroupings}"
    state.associationGroups = cmd.supportedGroupings
}

def setDefaultAssociations() {
    def hubitatHubID = zwaveHubNodeId.toString().format( '%02x', zwaveHubNodeId )
    state.defaultG1 = [hubitatHubID]
    state.defaultG2 = []
    state.defaultG3 = []
}

def setAssociationGroup(group, nodes, action, endpoint = null){
    if (!state."desiredAssociation${group}") {
        state."desiredAssociation${group}" = nodes
    } else {
        switch (action) {
            case 0:
                state."desiredAssociation${group}" = state."desiredAssociation${group}" - nodes
            break
            case 1:
                state."desiredAssociation${group}" = state."desiredAssociation${group}" + nodes
            break
        }
    }
}

def processAssociations(){
   def cmds = []
   setDefaultAssociations()
   def associationGroups = 5
   if (state.associationGroups) {
       associationGroups = state.associationGroups
   } else {
       if (logEnable) log.debug "Getting supported association groups from device"
       cmds <<  zwave.associationV2.associationGroupingsGet().format()
   }
   for (int i = 1; i <= associationGroups; i++){
      if(state."actualAssociation${i}" != null){
         if(state."desiredAssociation${i}" != null || state."defaultG${i}") {
            def refreshGroup = false
            ((state."desiredAssociation${i}"? state."desiredAssociation${i}" : [] + state."defaultG${i}") - state."actualAssociation${i}").each {
                if (logEnable) log.debug "Adding node $it to group $i"
                cmds << zwave.associationV2.associationSet(groupingIdentifier:i, nodeId:Integer.parseInt(it,16)).format()
                refreshGroup = true
            }
            ((state."actualAssociation${i}" - state."defaultG${i}") - state."desiredAssociation${i}").each {
                if (logEnable) log.debug "Removing node $it from group $i"
                cmds << zwave.associationV2.associationRemove(groupingIdentifier:i, nodeId:Integer.parseInt(it,16)).format()
                refreshGroup = true
            }
            if (refreshGroup == true) cmds << zwave.associationV2.associationGet(groupingIdentifier:i)
            else log.debug "There are no association actions to complete for group $i"
         }
      } else {
         log.debug "Association info not known for group $i. Requesting info from device."
         cmds << zwave.associationV2.associationGet(groupingIdentifier:i).format()
      }
   }
   return cmds
}


def setGenericTempName(temp){
    if (!temp) return
    def genericName
    def value = temp.toInteger()
    if (value <= 2000) genericName = "Sodium"
    else if (value <= 2100) genericName = "Starlight"
    else if (value < 2400) genericName = "Sunrise"
    else if (value < 2800) genericName = "Incandescent"
    else if (value < 3300) genericName = "Soft White"
    else if (value < 3500) genericName = "Warm White"
    else if (value < 4150) genericName = "Moonlight"
    else if (value <= 5000) genericName = "Horizon"
    else if (value < 5500) genericName = "Daylight"
    else if (value < 6000) genericName = "Electronic"
    else if (value <= 6500) genericName = "Skylight"
    else if (value < 20000) genericName = "Polar"
    def descriptionText = "${device.getDisplayName()} color is ${genericName}"
    if (txtEnable) log.info "${descriptionText}"
	sendEvent(name: "colorMode", value: "CT", descriptionText: "${device.getDisplayName()} color mode is CT")
    sendEvent(name: "colorName", value: genericName ,descriptionText: descriptionText)
}



def setGenericName(hue){
    def colorName
    hue = hue.toInteger()
    hue = (hue * 3.6)
    switch (hue.toInteger()){
        case 0..15: colorName = "Red"
            break
        case 16..45: colorName = "Orange"
            break
        case 46..75: colorName = "Yellow"
            break
        case 76..105: colorName = "Chartreuse"
            break
        case 106..135: colorName = "Green"
            break
        case 136..165: colorName = "Spring"
            break
        case 166..195: colorName = "Cyan"
            break
        case 196..225: colorName = "Azure"
            break
        case 226..255: colorName = "Blue"
            break
        case 256..285: colorName = "Violet"
            break
        case 286..315: colorName = "Magenta"
            break
        case 316..345: colorName = "Rose"
            break
        case 346..360: colorName = "Red"
            break
    }
    def descriptionText = "${device.getDisplayName()} color is ${colorName}"
    if (txtEnable) log.info "${descriptionText}"
	sendEvent(name: "colorMode", value: "RGB", descriptionText: "${device.getDisplayName()} color mode is RGB")
    sendEvent(name: "colorName", value: colorName ,descriptionText: descriptionText)
}