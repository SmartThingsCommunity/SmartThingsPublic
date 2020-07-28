/**
 *  MATRIX ZDB5100
 *
 *  Copyright 2019 Kim T. Nielsen
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 */

import groovy.json.JsonOutput

metadata {
    definition(name: "MATRIX ZDB5100", namespace: "smartthings", author: "Kim T. Nielsen & Arturas Taskauskas", cstHandler: true, mcdSync: true) {
        capability "Actuator"
        capability "Light"
        capability "Switch"
        capability "Switch Level"
        capability "Color Control"
        capability "Configuration"
        capability "Button"

        // Custom Attributes:
        attribute "logMessage", "string"        // Important log messages.
        attribute "nightmode", "string"         // 'Enabled' or 'Disabled'.
        attribute "scene", "number"             // ID of last-activated scene.

        // Custom commands:
        command "toggleNightmode"

        fingerprint mfr: "0234", prod: "0003", model: "0121", deviceJoinName: "MATRIX ZDB5100"
    }

    simulator {
        // TODO: define status and reply messages here
    }

    tiles(scale: 2) {
        // Multi Tile:
        multiAttributeTile(name:"switch", type: "lighting", width: 6, height: 4, canChangeIcon: true) {
            tileAttribute("device.switch", key: "PRIMARY_CONTROL") {
                attributeState "on", label:'${name}', action:"switch.off", icon:"st.switches.switch.on", backgroundColor:"#79b821", nextState:"turningOff"
                attributeState "off", label:'${name}', action:"switch.on", icon:"st.switches.switch.off", backgroundColor:"#ffffff", nextState:"turningOn"
                attributeState "turningOn", label:'${name}', action:"switch.off", icon:"st.switches.switch.on", backgroundColor:"#79b821", nextState:"turningOff"
                attributeState "turningOff", label:'${name}', action:"switch.on", icon:"st.switches.switch.off", backgroundColor:"#ffffff", nextState:"turningOn"
            }
            tileAttribute("device.level", key: "SLIDER_CONTROL", range:"(0..100)") {
                attributeState "level", action:"setLevel"
            }
            tileAttribute("device.color", key: "COLOR_CONTROL") {
                //log.debug("Setting TILE ATTRIBUTE COLOR CONTROL")
                attributeState "color", action:"setColor"
            }

        }

        standardTile("nightmode", "device.nightmode", decoration: "flat", width: 6, height: 2) {
            state "default", label:'Night Mode ' + '${currentValue}', action:"toggleNightmode", icon:"st.Weather.weather4"
        }

        valueTile("scene", "device.scene", decoration: "flat", width: 2, height: 2) {
            state "default", label:'Scene: ${currentValue}'
        }

        main(["switch"])
        details([
            "switch",
            "rgbSelector",
            "nightmode"
        ])
    }

    preferences {
        section {  // NIGHTMODE:
            input type: "paragraph",
                element: "paragraph",
                title: "NIGHTMODE:",
                description: "Nightmode forces the buttons leds to a specific level (e.g. low-level during the night).\n" +
                "Nightmode can be enabled/disabled manually using the Nightmode tile, or scheduled below."

            input type: "number",
                name: "configNightmodeLevel",
                title: "Nightmode Level: The buttons led level when nightmode is enabled.",
                range: "1..100",
                required: true

            input type: "number",
                name: "configDaymodeLevel",
                title: "Daymode Level: The buttons led level when nightmode is disabled (day mode).",
                range: "1..100",
                required: true

            input type: "time",
                name: "configNightmodeStartTime",
                title: "Nightmode Start Time: Nightmode will be enabled every day at this time.",
                required: false

            input type: "time",
                name: "configNightmodeStopTime",
                title: "Nightmode Stop Time: Nightmode will be disabled every day at this time.",
                required: false
        }
        section {  // Enchanced LED:
            input type: "paragraph",
                element: "paragraph",
                title: "Enchanced LED control:",
                description: "If parameter number 10 is enabled, use this prefernces\n" +
                "in order to setup additional features of the LED's"

            input type: "number",
                name: "configEnchancedLedBlink",
                title: "LED BLINK: When this preference is set to 1, the LEDs will flash with the color levels from color control tile " +
                " ",
                range: "0..1",
                required: false
            input type: "number",
                name: "configEnchancedLedBlinkTurnOff",
                title: "Turn OFF: The command applies to lights in the off / passive LEDs (key not)activated). " +
                "",
                range: "0..1",
                required: false
            input type: "number",
                name: "configEnchancedLedBlinkTurnOn",
                title: "Turn ON: The command applies to light in the on / active mode LEDs (key on)." +
                "",
                range: "0..1",
                required: false

            input type: "number",
                name: "configEnchancedLedBlinkDirect",
                title: "Direct managment: The command applies to a temporary override of the LEDs that will be in effect until the next key activation (or a Z-Wave command)." +
                "",
                range: "0..1",
                required: false

            input type: "number",
                name: "configEnchancedLedBlinkTime",
                title: "LED blink time:  indicate the time of the blink rate (only valid if the Blink bit is set), the time is set for 0.1 second and the time is for the half period " +
                "(the time for light on or off). For example, the value 5 indicates 0.5 seconds, i.e. the LEDs are on for 0.5 seconds, off for 0.5seconds, ie a period of 1 second, corresponding to a 1 Hz flashing frequency..",
                range: "1..15",
                required: false

            input name: "configBtns", type: "enum", title: "Select to which button color change should apply to.", options: ["All", "Button 1", "Button 2", "Button 3", "Button 4"], defaultValue: "All", description: "Enter enum", required: false
        }

        generatePreferenceParameters()
    }
}

def installed() {
    //log.debug "MATRIX: installed() called"
    def numberOfButtons = 4
    createChildDevices()
    sendEvent(name: "numberOfButtons", value: numberOfButtons, displayed: true)
    sendEvent(name: "supportedButtonValues", value: ["pushed", "held", "released", "pushed_2x", "pushed_3x"].encodeAsJson(), displayed: true)
    sendEvent(name: "button", value: "pushed", data: [buttonNumber: 1], displayed: false)
}

def createChildDevices() {
    if (!childDevices) {
        state.oldLabel = device.label
        def child
        for (i in 1 .. 4) {
            child = addChildDevice("Child Button", "${device.deviceNetworkId}:${i}", device.hubId, [completedSetup: true, label: "${device.displayName} button ${i}",
                isComponent: true, componentName: "button$i", componentLabel: "Button $i"])
            child.sendEvent(name: "supportedButtonValues", value:["pushed", "held", "released", "pushed_2x", "pushed_3x"].encodeAsJson(), displayed: true)
            child.sendEvent(name: "button", value: "pushed", data: [buttonNumber: 1], descriptionText: "$child.displayName was pushed", isStateChange: true, displayed: true)
        }
    }
}

def getChildDevice(button) {
    String childDni = "${device.deviceNetworkId}:${button}"
    def child = childDevices.find { it.deviceNetworkId == childDni }
    if (!child) {
        log.error "Child device $childDni not found"
    }
    return child
}


/**
 *  parse()
 *
 *  Called when messages from the device are received by the hub. The parse method is responsible for interpreting
 *  those messages and returning event definitions (and command responses).
 *
 *  As this is a Z-wave device, zwave.parse() is used to convert the message into a command. The command is then
 *  passed to zwaveEvent(), which is overloaded for each type of command below.
 *
 *  Parameters:
 *   String      description        The raw message from the device.
 **/
def parse(String description) {
    log.debug "MATRIX: Parsing '${description}'"
    def result = null
    def cmd = zwave.parse(description)
    // ignoring basic report from root endpoint ....
    if (description.contains("command: 2003, payload: 63 63 00")) {
        log.debug "MATRIX: Ignored '${description}'"
        cmd = null
    }
    if (cmd) {
        result = zwaveEvent(cmd)
        log.debug "Parsed ${cmd} to ${result.inspect()}"
    } else {
        log.debug "Non-parsed event: ${description}"
    }
    return result
}



/*****************************************************************************************************************

 *  Z-wave Event Handlers.

 *****************************************************************************************************************/

/**
 * MultiChannelCmdEncap and MultiInstanceCmdEncap are ways that devices
 * can indicate that a message is coming from one of multiple subdevices
 * or "endpoints" that would otherwise be indistinguishable
 **/
def zwaveEvent(physicalgraph.zwave.commands.multichannelv3.MultiChannelCmdEncap cmd) {
    def encapsulatedCommand = cmd.encapsulatedCommand(getCommandClassVersions())

    if (!encapsulatedCommand) {
        log.debug("zwaveEvent(): Could not extract command from ${cmd}")
    } else {
        log.debug("Command from endpoint ${cmd.sourceEndPoint}: ${encapsulatedCommand}")

        if (encapsulatedCommand.toString().contains("BasicReport") && cmd.sourceEndPoint < 6) {} // Would like to ignore basic report for endpoints 1-5 as there are no elements to update at the moment.
        else {
            return zwaveEvent(encapsulatedCommand)
        }
    }
}

def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicReport cmd) {
    log.debug("MATRIX: Basic Report: value = ${cmd}")
    def result = []
    result << createEvent(name: "switch", value: cmd.value ? "on" : "off")
    return result
}

def zwaveEvent(physicalgraph.zwave.commands.centralscenev1.CentralSceneNotification cmd) {
	def listOfpushedValues=["pushed","released","held","pushed_2x","pushed_3x"] as String[]
    log.debug("MATRIX: Central Scene Event, sceneNumber: ${cmd.sceneNumber}, keyAttributes: ${cmd.keyAttributes}")
    log.debug("MATRIX: Central Scene Event, sceneNumber: ${cmd.sceneNumber}, keyAttributes: ${cmd.keyAttributes}")
    def result = []
    result << createEvent(name: "scene", value: "$cmd.sceneNumber", data: [keyAttributes: "$cmd.keyAttributes"], descriptionText: "Scene number ${cmd.sceneNumber} was activated", isStateChange: true)
    log.debug("Scene #${cmd.sceneNumber} was activated.") 
    
	def child = getChildDevice(cmd.sceneNumber)    			
    child?.sendEvent(name:"button", value:  listOfpushedValues[cmd.keyAttributes], data:[buttonNumber: 1], descriptionText: "${child.displayName} was ${listOfpushedValues[cmd.keyAttributes]}", isStateChange:true)
   
    result << createEvent(name:"button", value:listOfpushedValues[cmd.keyAttributes], data:[buttonNumber: (int)cmd.sceneNumber], descriptionText: "${device.displayName} button ${cmd.sceneNumber} was ${listOfpushedValues[cmd.keyAttributes]}", isStateChange:true) 
    
    return result
}

/**
 *  zwaveEvent( COMMAND_CLASS_SWITCH_MULTILEVEL V3 (0x26) : SWITCH_MULTILEVEL_REPORT )
 *
 *  The Switch Multilevel Report is used to advertise the status of a multilevel device.
 *
 *  Action: Pass command to dimmerEvent().
 *
 *  cmd attributes:
 *    Short    value
 *      0x00       = Off
 *      0x01..0x63 = 0..100%
 *      0xFE       = Unknown
 *      0xFF       = On [Deprecated]
 *
 *  Example: SwitchMultilevelReport(value: 1)
 **/
def zwaveEvent(physicalgraph.zwave.commands.switchmultilevelv3.SwitchMultilevelReport cmd) {
    log.debug("zwaveEvent(): Switch Multilevel Report received: ${cmd}")

    return dimmerEvent(cmd)
}

/**
 *  zwaveEvent( COMMAND_CLASS_CONFIGURATION V2 (0x70) : CONFIGURATION_REPORT )
 *
 *  The Configuration Report Command is used to advertise the actual value of the advertised parameter.
 *
 *  Action: Store the value in the parameter cache.
 *
 **/
def zwaveEvent(physicalgraph.zwave.commands.configurationv2.ConfigurationReport cmd) {
    log.debug("zwaveEvent(): Configuration Report received: ${cmd}")

    state."paramCache${cmd.parameterNumber}" = cmd.scaledConfigurationValue.toInteger()
    def paramName = getParametersMetadata().find( { it.id == cmd.parameterNumber }).name
    log.debug("Parameter #${cmd.parameterNumber} [${paramName}] has value: ${cmd.scaledConfigurationValue}")
}

/**
 *  zwaveEvent( COMMAND_CLASS_VERSION V1 (0x86) : VERSION_REPORT )
 *
 *  The Version Report Command is used to advertise the library type, protocol version, and application version.

 *  Action: Publish values as device 'data' and log an info message. No check is performed.
 *
 **/
def zwaveEvent(physicalgraph.zwave.commands.versionv1.VersionReport cmd) {
    log.debug("zwaveEvent(): Version Report received: ${cmd}")

    def zWaveLibraryTypeDisp  = String.format("%02X", cmd.zWaveLibraryType)

    def applicationVersionDisp = String.format("%d.%02d", cmd.applicationVersion, cmd.applicationSubVersion)
    def zWaveProtocolVersionDisp = String.format("%d.%02d", cmd.zWaveProtocolVersion, cmd.zWaveProtocolSubVersion)

    log.debug("Version Report: Application Version: ${applicationVersionDisp}, " +
              "Z-Wave Protocol Version: ${zWaveProtocolVersionDisp}, " +
              "Z-Wave Library Type: ${zWaveLibraryTypeDisp}")

    updateDataValue("applicationVersion", "${cmd.applicationVersion}")
    updateDataValue("applicationSubVersion", "${cmd.applicationSubVersion}")
    updateDataValue("zWaveLibraryType", "${zWaveLibraryTypeDisp}")
    updateDataValue("zWaveProtocolVersion", "${cmd.zWaveProtocolVersion}")
    updateDataValue("zWaveProtocolSubVersion", "${cmd.zWaveProtocolSubVersion}")
}

/**
 *  dimmerEvent()
 *
 *  Common handler for BasicReport, SwitchBinaryReport, SwitchMultilevelReport.
 *
 *  Action: Raise 'switch' and 'level' events.
 *   Restore pending level if dimmer has been switched on after nightmode has been disabled.
 *   
 **/
def dimmerEvent(physicalgraph.zwave.Command cmd) {
    def result = []

    // switch event:
    def switchValue = (cmd.value ? "on" : "off")

    def switchEvent = createEvent(name: "switch", value: switchValue)

    if (switchEvent.isStateChange) log.debug("Dimmer turned ${switchValue}.")
    result << switchEvent


    // level event:
    def levelValue = Math.round(cmd.value * 100 / 99)
    if (levelValue != 0 && levelValue != 1) {
        state.lastDimmerValue = levelValue
    }
    if (levelValue == 1) {
        levelValue = state.DIM_OFF_OFFSET
    }

    def levelEvent = createEvent(name: "level", value: levelValue, unit: "%") //+state.DIM_OFF_OFFSET
    if (levelEvent.isStateChange) log.debug("Dimmer level is ${levelValue}%")
    result << levelEvent

    return result
}


/*****************************************************************************************************************
 *  Capability-related Commands:
 *****************************************************************************************************************/

/**
 *  on()                        [Capability: Switch]
 *
 *  Turn the dimmer on.
 **/
def on() {
    def result = []
	if (state?.DIM_OFF_OFFSET > 0) {
	result << endpointCmd(zwave.switchMultilevelV3.switchMultilevelSet(value: state.lastDimmerValue), 5).format()
	} else {
	result << endpointCmd(zwave.basicV1.basicSet(value: 255), 5).format()
	}
    return response(delayBetween(result, 100))
}

/**
 *  off()                       [Capability: Switch]
 *
 *  Turn the dimmer off.
 **/
def off() {
    def result = []
    if (state?.DIM_OFF_OFFSET > 0) {
        log.debug("state.DIM_OFF_OFFSET>0)")
        result.remove(0)
        result << endpointCmd(zwave.switchMultilevelV3.switchMultilevelSet(value: state.DIM_OFF_OFFSET), 5).format()
    }
	else{
		result << endpointCmd(zwave.basicV1.basicSet(value: 0), 5).format()
	}
    return response(delayBetween(result, 100))
}


/**
 *  setLevel()                  [Capability: Switch Level]
 *
 *  Set the dimmer level.
 *
 *  Parameters:
 *   level    Target level (0-100%).
 **/
def setLevel(level) {
    log.debug("setLevel(${level})")

    if (level < 0) level = 0
    if (level > 100) level = 100

    log.debug("Setting dimmer to ${level}%")

    // Clear nightmodePendingLevel as it's been overridden.
    state.nightmodePendingLevel = 0

    // Convert from 0-100 to 0-99
    level = Math.round(level * 99 / 100)

    def result = []
    if (level != 0 && level != 1) {
        state.lastDimmerValue = level
    }
    result << endpointCmd(zwave.switchMultilevelV3.switchMultilevelSet(value: level), 5).format()
    return response(delayBetween(result, 1500))
}

def setColor(value) {
	
    log.debug("MATRIX: setColor(${value})")
    // enchancedLedControl << zwave.configurationV2.configurationGet(parameterNumber: 10)
    log.debug("MATRIX: enchancedLedControl(${state.enchancedLedControl})")
    def result = []
    def WW=0x10
	
    if (state.enchancedLedControl == 1) {
			WW = 0;
            if (settings.configEnchancedLedBlink?.toInteger())        WW |= 1 << 7;
            if (settings.configEnchancedLedBlinkTurnOff?.toInteger()) WW |= 1 << 6;
            if (settings.configEnchancedLedBlinkTurnOn?.toInteger())  WW |= 1 << 5;
            if (settings.configEnchancedLedBlinkDirect?.toInteger())  WW |= 1 << 4;
            WW += settings.configEnchancedLedBlinkTime?.toInteger();
			log.debug("MATRIX: WW=(${WW})")
			log.debug("MATRIX: WW=" + Integer.toBinaryString(WW))
    }
	
    if (value instanceof String) {
        if ( (value.contains('#') && value.length() == 7) || value.length() == 6) {    //if (value.hex)
            def colors = colorUtil.hexToRgb(value) // value.hex.findAll(/[0-9a-fA-F]{2}/).collect { Integer.parseInt(it, 16) }
            log.debug("Color: R:${c[0]}, G:${c[1]}, B:${c[2]}")
            sendEvent(name: "color", value: colors)
        }
		
    } else {
        def hue = value.hue ?: device.currentValue("hue")
        def saturation = value.saturation ?: device.currentValue("saturation")
        if (hue == null) hue = 13
        if (saturation == null) saturation = 13
        def colors = huesatToRGB((float)hue, (float)saturation)
        if (value.hue != null) sendEvent(name: "hue", value: value.hue)
        if (value.saturation != null) sendEvent(name: "saturation", value: value.saturation)
    }

    def lastColor = device.latestValue("color")
    log.debug("MATRIX: Lastcolor1(${lastColor})")
    log.debug("MATRIX: ENUMM   : (${settings.configBtns})")
    log.debug("MATRIX: ENUMM   : (${settings.configBtns.toString()})")
    if (settings.configBtns == "Button 1") {
		result << endpointCmd(zwave.switchColorV3.switchColorSet(warmWhite: WW, red:colors[0], green:colors[1], blue:colors[2]), 1).format()
    }
	
    else if (settings.configBtns == "Button 2") {
		result << endpointCmd(zwave.switchColorV3.switchColorSet(warmWhite: WW, red:colors[0], green:colors[1], blue:colors[2]), 2).format()
    }
	
    else if (settings.configBtns == "Button 3") {
		result << endpointCmd(zwave.switchColorV3.switchColorSet(warmWhite: WW, red:colors[0], green:colors[1], blue:colors[2]), 3).format()
    }
	
    else if (settings.configBtns == "Button 4") {
		result << endpointCmd(zwave.switchColorV3.switchColorSet(warmWhite: WW, red:colors[0], green:colors[1], blue:colors[2]), 4).format()
    }
	
	else
	{	
		result << endpointCmd(zwave.switchColorV3.switchColorSet(warmWhite: WW, red:colors[0], green:colors[1], blue:colors[2]), 1).format()
		result << endpointCmd(zwave.switchColorV3.switchColorSet(warmWhite: WW, red:colors[0], green:colors[1], blue:colors[2]), 2).format()
		result << endpointCmd(zwave.switchColorV3.switchColorSet(warmWhite: WW, red:colors[0], green:colors[1], blue:colors[2]), 3).format()
		result << endpointCmd(zwave.switchColorV3.switchColorSet(warmWhite: WW, red:colors[0], green:colors[1], blue:colors[2]), 4).format()
	}
	
    return response(delayBetween(result, 200))
	
}

/**
 *  enableNightmode(level)
 *
 *  Starts nightmode and set level to configNightmodeLevel
 *
 **/
def enableNightmode(level = -1) {
    // Clean level value:
    if (level == -1) level = settings.configNightmodeLevel.toInteger()
    if (level > 100) level = 100
    if (level < 1) level = 1

    log.debug("enableNightmode(): Setting level: ${level}")

    // convert from 0 - 100 to 0 - 255
    def v = Math.round(level * 255 / 100)

    def commands = []
    commands << endpointCmd(zwave.switchColorV3.switchColorSet(warmWhite: 64, red: v, green: v, blue: v), 1).format()
    commands << endpointCmd(zwave.switchColorV3.switchColorSet(warmWhite: 64, red: v, green: v, blue: v), 2).format()
    commands << endpointCmd(zwave.switchColorV3.switchColorSet(warmWhite: 64, red: v, green: v, blue: v), 3).format()
    commands << endpointCmd(zwave.switchColorV3.switchColorSet(warmWhite: 64, red: v, green: v, blue: v), 4).format()

    sendHubCommand(commands.collect { response(it) }, 200)

    state.nightmodeActive = true
    sendEvent(name: "nightmode", value: "Enabled", descriptionText: "Nightmode Enabled", isStateChange: true)
}

/**
 *  disableNightmode()
 *
 *  Stop nightmode and set level to configDaymodeLevel
 *
 *  triggered by schedule().
 **/
def disableNightmode(level = -1) {
    // Clean level value:
    if (level == -1) level = settings.configDaymodeLevel.toInteger()
    if (level > 100) level = 100
    if (level < 1) level = 1

    log.debug("disableNightmode(): Setting level: ${level}")

    // convert from 0 - 100 to 0 - 255
    def v = Math.round(level * 255 / 100)

    def commands = []
    commands << endpointCmd(zwave.switchColorV3.switchColorSet(warmWhite: 64, red: v, green: v, blue: v), 1).format()
    commands << endpointCmd(zwave.switchColorV3.switchColorSet(warmWhite: 64, red: v, green: v, blue: v), 2).format()
    commands << endpointCmd(zwave.switchColorV3.switchColorSet(warmWhite: 64, red: v, green: v, blue: v), 3).format()
    commands << endpointCmd(zwave.switchColorV3.switchColorSet(warmWhite: 64, red: v, green: v, blue: v), 4).format()

    sendHubCommand(commands.collect { response(it) }, 200)

    state.nightmodeActive = false
    sendEvent(name: "nightmode", value: "Disabled", descriptionText: "Nightmode Disabled", isStateChange: true)
}

/**
 *  toggleNightmode()
 **/
def toggleNightmode() {
    log.debug("toggleNightmode()")

    if (state.nightmodeActive) {
        sendEvent(name: "nightmode", value: "Disabled", descriptionText: "Nightmode Disabled", isStateChange: true)

        disableNightmode(configDaymodeLevel)
    } else {
        sendEvent(name: "nightmode", value: "Enabled", descriptionText: "Nightmode Enabled", isStateChange: true)

        enableNightmode(configNightmodeLevel)
    }
}


/**
 * Configuration capability command handler.
 *
 * @param void
 * @return List of commands that will be executed in sequence with 500 ms delay inbetween.
*/
def configure() {
    log.debug "MATRIX: configure()"

    def cmds = []
    //cmds << zwave.associationV1.associationRemove(groupingIdentifier:1, nodeId:zwaveHubNodeId).format()
    cmds << zwave.multiChannelAssociationV2.multiChannelAssociationSet(groupingIdentifier: 2, nodeId: [0, zwaveHubNodeId, 1]).format()
    cmds << zwave.multiChannelAssociationV2.multiChannelAssociationSet(groupingIdentifier: 5, nodeId: [0, zwaveHubNodeId, 1]).format()
    cmds << zwave.multiChannelAssociationV2.multiChannelAssociationSet(groupingIdentifier: 8, nodeId: [0, zwaveHubNodeId, 1]).format()
    cmds << zwave.multiChannelAssociationV2.multiChannelAssociationSet(groupingIdentifier: 11, nodeId: [0, zwaveHubNodeId, 1]).format()

    cmds << zwave.configurationV1.configurationSet(parameterNumber: 10, size: 1, scaledConfigurationValue: 0x01).format()
    cmds << zwave.configurationV1.configurationSet(parameterNumber: 14, size: 4, scaledConfigurationValue: 0xFF555500).format()
    cmds << zwave.configurationV1.configurationSet(parameterNumber: 22, size: 4, scaledConfigurationValue: 0x0000FF00).format()
    cmds << zwave.configurationV1.configurationSet(parameterNumber: 23, size: 4, scaledConfigurationValue: 0x7F7F7F00).format()
    cmds << zwave.configurationV1.configurationSet(parameterNumber: 30, size: 4, scaledConfigurationValue: 0x0000FF00).format()
    cmds << zwave.configurationV1.configurationSet(parameterNumber: 31, size: 4, scaledConfigurationValue: 0x7F7F7F00).format()
    cmds << zwave.configurationV1.configurationSet(parameterNumber: 38, size: 4, scaledConfigurationValue: 0x0000FF00).format()
    cmds << zwave.configurationV1.configurationSet(parameterNumber: 39, size: 4, scaledConfigurationValue: 0x7F7F7F00).format()
    cmds << zwave.configurationV1.configurationSet(parameterNumber: 46, size: 4, scaledConfigurationValue: 0x0000FF00).format()
    cmds << zwave.configurationV1.configurationSet(parameterNumber: 47, size: 4, scaledConfigurationValue: 0x7F7F7F00).format()

    return response(delayBetween(cmds, 1000))

}


/**
 *  sync()
 *
 *  Manages synchronisation of parameters, association groups, and protection state with the physical device.
 *  The syncPending attribute advertises remaining number of sync operations.
 *
 *  Does not return a list of commands, it sends them immediately using sendSecureSequence(). This is required if
 *  triggered by schedule().
 *
 *  Parameters:
 *   forceAll    Force all items to be synced, otherwise only changed items will be synced.
 **/
private sync(forceAll = false) {


    log.debug("sync(): Syncing configuration with the physical device.")

    def cmds = []

    if (forceAll){ // Clear all cached values.
        getParametersMetadata().findAll( { !it.readonly }).each {
            state."paramCache${it.id}" = null
        }
        //getAssocGroupsMd().each { state."assocGroupCache${it.id}" = null }
    }

    getParametersMetadata().findAll( { !it.readonly }).each{ // Exclude readonly parameters.
        if ((state."paramTarget${it.id}" != null) & (state."paramCache${it.id}" != state."paramTarget${it.id}")){
            cmds << zwave.configurationV2.configurationSet(parameterNumber: it.id, size: it.size, scaledConfigurationValue: state."paramTarget${it.id}".toInteger())
            cmds << zwave.configurationV2.configurationGet(parameterNumber: it.id)
            log.debug("sync(): Syncing parameter #${it.id} [${it.name}]: New Value: " + state."paramTarget${it.id}")

            if(it.id == 10){
                log.debug("sync(): We found parameter #10 ")
                if (state."paramTarget${it.id}".toInteger()==1) {
                        state.enchancedLedControl = 1
                        log.debug("sync(): ENABLE ENCHANCED LED ")
                } else {
                        state.enchancedLedControl = 0
                        log.debug("sync(): DISABLE ENCHANCED LED ")
                }
            }
            if(it.id == 5){
                log.debug("sync(): We found parameter #5 ")
                if (state."paramTarget${it.id}".toInteger()>0){
                    state.DIM_OFF_OFFSET = state."paramTarget${it.id}"
                    log.debug("sync(): Setting Dim OFF OFFSET ")
                } else {
                    state.DIM_OFF_OFFSET=0
                    log.debug("sync(): Removing Dim OFF OFFSET ")
                }
            }


        }
    }

    /*
    getAssocGroupsMd().each {
        def cachedNodes = state."assocGroupCache${it.id}"
        def targetNodes = state."assocGroupTarget${it.id}"

        if ( cachedNodes != targetNodes ) {
            // Display to user in hex format (same as IDE):
            def targetNodesHex  = []
            targetNodes.each { targetNodesHex.add(String.format("%02X", it)) }
            logger("sync(): Syncing Association Group #${it.id}: Destinations: ${targetNodesHex}","info")

            cmds << zwave.multiChannelAssociationV2.multiChannelAssociationRemove(groupingIdentifier: it.id, nodeId: []) // Remove All
            cmds << zwave.multiChannelAssociationV2.multiChannelAssociationSet(groupingIdentifier: it.id, nodeId: targetNodes)
            cmds << zwave.multiChannelAssociationV2.multiChannelAssociationGet(groupingIdentifier: it.id)
            syncPending++
        }
    }
    */

    sendHubCommand(cmds.collect { response(it) } , 1000) // Need a delay of at least 1000ms.
}





/**
 *  refreshConfig()
 *
 *  Request configuration reports from the physical device: [ Configuration, Association, Version, etc. ]
 *
 *  Really only needed at installation or when debugging, as sync will request the necessary reports when the
 *  configuration is changed.
 */
private refreshConfig() {
    log.debug("refreshConfig()")

    def cmds = []

    getParamsMd().each { cmds << zwave.configurationV2.configurationGet(parameterNumber: it.id) }
    //getAssocGroupsMd().each { cmds << zwave.multiChannelAssociationV2.multiChannelAssociationGet(groupingIdentifier: it.id) }

    cmds << zwave.versionV1.versionGet()

    sendHubCommand(cmds.collect { response(it) }, 1000) // Need a delay of at least 1000ms.
}

/**
 *  updated()
 *
 *  Runs when the user hits "Done" from Settings page.
 *
 *  Action: Process new settings, sync parameters and association group members with the physical device. Request
 *  Firmware Metadata, Manufacturer-Specific, and Version reports.
 *
 *  Note: Weirdly, update() seems to be called twice. So execution is aborted if there was a previous execution
 *  within two seconds.
 **/
def updated() {
    log.debug("updated()")
    createChildDevices()
    if (device.label != state.oldLabel) {
        childDevices.each {
            def segs = it.deviceNetworkId.split(":")
            def newLabel = "${device.displayName} button ${segs[-1]}"
            it.setLabel(newLabel)
        }
        state.oldLabel = device.label
    }

    def cmds = []

    if (!state.updatedLastRanAt || now()>= state.updatedLastRanAt + 2000) {
        state.updatedLastRanAt = now()

        // Manage Schedules:
        manageSchedules()

        // Update Parameter target values:
        getParametersMetadata().findAll( { !it.readonly }).each{  // Exclude readonly parameters.
            state."paramTarget${it.id}" = settings."configParam${it.id}"?.toInteger()
        }

        // Update Assoc Group target values:
        /*
        state.assocGroupTarget1 = [ zwaveHubNodeId ] // Assoc Group #1 is Lifeline and will contain controller only.
        getAssocGroupsMd().findAll( { it.id != 1} ).each {
            state."assocGroupTarget${it.id}" = parseAssocGroupInput(settings."configAssocGroup${it.id}", it.maxNodes)
        }
        */

        // Sync configuration with phyiscal device:
        sync(true)

        // Request device medadata (this just seems the best place to do it):
        cmds << zwave.versionV1.versionGet().format()

        return response(delayBetween(cmds, 1000))
    } else {
        log.debug("updated(): Ran within last 2 seconds so aborting.")
    }
}

/*****************************************************************************************************************
 *  Private Helper Functions:
 *****************************************************************************************************************/

/**
 *  endpointCmd(cmd, endpoint)
 *
 *  Encapsulate command using multiChannelCmdEncap.
 *  
 **/
private endpointCmd(physicalgraph.zwave.Command cmd, endpoint) {
    return zwave.multiChannelV3.multiChannelCmdEncap(sourceEndPoint:1, destinationEndPoint:endpoint).encapsulate(cmd)
}

private huesatToRGB(float hue, float sat) {
    while (hue >= 100) hue -= 100

    int h = (int)(hue / 100 * 6)
    float f = hue / 100 * 6 - h
    int p = Math.round(255 * (1 - (sat / 100)))
    int q = Math.round(255 * (1 - (sat / 100) * f))
    int t = Math.round(255 * (1 - (sat / 100) * (1 - f)))
    switch (h) 
    {
    case 0:
        return [255, t, p]
    case 1:
        return [q, 255, p]
    case 2:
        return [p, 255, t]
    case 3:
    return [p, q, 255]
    case 4:
        return [t, p, 255]
    case 5:
        return [255, p, q]
    }
}

/**
 *  manageSchedules()
 *
 *  Schedules/unschedules Nightmode.
 **/
private manageSchedules() {
    log.debug("manageSchedules()")

    if (configNightmodeStartTime) {
        schedule(configNightmodeStartTime, enableNightmode)
        log.debug("manageSchedules(): Nightmode scheduled to start at ${configNightmodeStartTime}")
    } else {
        try {
            unschedule("enableNightmode")
        } catch (e) {
            // Unschedule failed
        }
    }

    if (configNightmodeStopTime) {
        schedule(configNightmodeStopTime, disableNightmode)
        log.debug("manageSchedules(): Nightmode scheduled to stop at ${configNightmodeStopTime}")
    } else {
        try {
            unschedule("disableNightmode")
        } catch (e) {
            // Unschedule failed
        }
    }
}


private getButtonAttributesMap() {
    return  { [
            0: "pushed",
            1: "released",
            2: "held",
            3: "pushed_2x",
            4: "pushed_3x"
        ] }
}




/**
 *  generatePreferenceParameters()
 *
 *  Generates preferences (settings) for device parameters.
 **/
private generatePreferenceParameters() {
    section {
        input(
            type: "paragraph",
            element: "paragraph",
            title: "CONFIGURATION PARAMETERS:",
            description: "Configuration parameter settings. " +
            "Refer to the product documentation for a full description of each parameter."
            )

        getParametersMetadata().findAll( { !it.readonly }).each { // Exclude readonly parameters.
            def lb = (it.description.length() > 0) ? "\n" : ""

            switch (it.type) {
            case "number":
                input(
                    name:
                      "configParam${it.id}",
                    title:
                      "#${it.id}: ${it.name}: \n" + it.description + lb + "Default Value: ${it.defaultValue}",
                    type:
                      it.type,
                    range:
                      it.range,
                    required:
                      it.required
                    )
                break

            case "enum":
                input(
                    name:
                      "configParam${it.id}",
                    title:
                      "#${it.id}: ${it.name}: \n" + it.description + lb + "Default Value: ${it.defaultValue}",
                    type:
                      it.type,
                    options:
                      it.options,
                    required:
                      it.required
                    )
                break
            }
        }
    }
}

/*****************************************************************************************************************
 *  Static metadata functions:
 *
 *  These functions encapsulate metadata about the device.
 *****************************************************************************************************************/

/**
 *  getCommandClassVersions()
 *
 *  Returns a map of the command class versions supported by the device. Used by parse() and zwaveEvent() to
 *  extract encapsulated commands from MultiChannelCmdEncap and SecurityMessageEncapsulation
 *
 *  Reference: https://products.z-wavealliance.org/products/3399/classes
 **/
private getCommandClassVersions() {
    return [0x20: 1, // Basic V1
            0x26: 3, // Switch Multilevel V3
            0x5B: 1, // Central Scene V3
            0x59: 1, // Association Group Information V1 (Not handled, as no need)
            0x5A: 1, // Device Reset Locally V1
            0x60: 3, // Multi Channel V4 (Device supports V4, but SmartThings only supports V3)
            0x70: 2, // Configuration V2
            0x72: 2, // Manufacturer Specific V2
            0x73: 1, // Powerlevel V1
            0x7A: 2, // Firmware Update MD V3 (Device supports V3, but SmartThings only supports V2)
            0x85: 2, // Association V2
            0x86: 1, // Version V2 (Device supports V2, but SmartThings only supports V1)
            0x8E: 2, // Multi Channel Association V3 (Device supports V3, but SmartThings only supports V2)
            0x98: 1, // Security S0 V1
            0x9F: 1  // Security S2 V1
]
}

/**
 *  getParametersMetadata()
 *
 *  Returns configuration parameters metadata. 
 *
 *  Reference: https://products.z-wavealliance.org/products/3399/configs
 **/
private getParametersMetadata() {
    return [[id:  1, size: 1, type: "number", range: "0..15", defaultValue: "1", required: false, readonly: false,
        name: "Operating pushbutton(s) for dimmer",
        description: "This parameter specifies which pushbutton(s) that shall be used to control the built-in dimmer.\n" +
        "The parameter is a bitmask, so each of the values can be added together in order to have several pushbuttons to operate the dimmer.\n" +
        "Values:\n0 = No local operation of the dimmer.\n1 = Pushbutton 1 controls the dimmer.\n2 = Pushbutton 2 controls the dimmer.\n3 = Pushbutton 3 controls the dimmer.\n" +
        "4 = Pushbutton 4 controls the dimmer."], [id:  2, size: 1, type: "number", range: "0..255", defaultValue: "5", required: false, readonly: false,
        name: "Duration of dimming",
        description: "This parameter specifies the duration of a full regulation of the light from 0% to 100%.\n" +
        "A regulation of the light with 1% will take 1/100 of the specified duration. This is used when a pushbutton \n" +
        "is held-down for controlling the dimming, and when the dimming is fulfilled from other Z-Wave devices.\n" +
        "Values:\n0 = Immediately\n1 - 127 = Duration in seconds\n128 - 255 = Duration in minutes (minus 127) from 1  128 minutes, where 128 is 1 minute"], [id:  3, size: 1, type: "number", range: "0..255", defaultValue: "0", required: false, readonly: false,
        name: "Duration of on/off",
        description: "This parameter specifies the duration when turning the light on or off.\n" +
        "Values:\n0 = Immediately\n1 - 127 = Duration in seconds\n128 - 255 = Duration in minutes (minus 127) from 1  128 minutes, where 128 is 1 minute"], [id:  4, size: 1, type: "enum", defaultValue: "1", required: false, readonly: false,
        name: "Dimmer mode",
        description: "The dimmer can work in three different modes: on/off, leading edge or trailing edge.",
        options: ["0" : "0: No dimming, only on/off (0/100%)",
            "1" : "1: Trailing edge dimming",
            "2" : "2: Leading edge dimming"]], [id:  5, size: 1, type: "number", range: "0..99", defaultValue: "0", required: false, readonly: false,
        name: "Dimmer minimum level",
        description: "This parameter specifies the actual level of the dimmer output when set to 0%."], [id:  6, size: 1, type: "number", range: "0..99", defaultValue: "99", required: false, readonly: false,
        name: "Dimmer maximum level",
        description: "This parameter specifies the actual level of the dimmer output when set to 99%."], [id:  7, size: 1, type: "enum", defaultValue: "1", required: false, readonly: false,
        name: "Central Scene notifications",
        description: "This parameter can be used for disabling Central Scene notifications.",
        options: ["0" : "0: Notifications are disabled",
            "1" : "1: Notifications are enabled"]], [id:  8, size: 1, type: "enum", defaultValue: "1", required: false, readonly: false,
        name: "Double-activation functionality",
        description: "This parameter specifies the reaction when double-activating the pushbuttons.",
        options: ["0" : "0: Double-activation disabled",
            "1" : "1: Double-activation sets light to 100%"]], [id:  10, size: 1, type: "enum", defaultValue: "1", required: false, readonly: false,
        name: "Enhanced LED control",
        description: "This parameter can be used for enabling the enhanced LED control. See document about MATRIX enhanced LED control.",
        options: ["0" : "0: Enhanced LED control is disabled",
            "1" : "1: Enhanced LED control is enabled"]], [id:  11, size: 1, type: "number", range: "1..255", defaultValue: "5", required: false, readonly: false,
        name: "Pushbutton debounce timer",
        description: "Pushbutton input debounce time in 0.01 seconds resolution.\n" +
        "Values:\n1..255: 1  2.55 seconds. Default is 5, which equals to a debounce-filter on 50 milliseconds (0.05 seconds)."], [id:  12, size: 1, type: "number", range: "1..255", defaultValue: "20", required: false, readonly: false,
        name: "Pushbutton press threshold time",
        description: "Specifies the time that a pushbutton must be activated before it is detected as pressed. Resolution is in 0.01 seconds.\n" +
        "Values:\n1..255: 1  2.55 seconds. Default is 20, which equals to 200 milliseconds (0.2 seconds)."], [id:  13, size: 1, type: "number", range: "1..255", defaultValue: "50", required: false, readonly: false,
        name: "Pushbutton held threshold time",
        description: "Specifies the time that a pushbutton must have been activated before it is accepted as 'held-down'. Resolution is 0.01 seconds.\n" +
        "Values:\n1..255: 1  2.55 seconds. Default is 50, which equals to 500 milliseconds (0.5 seconds)."], [id:  14, size: 4, type: "number", range: "0..4294967040", defaultValue: "4283782400", required: false, readonly: false,
        name: "Global brightness control",
        description: "This parameter specifies a common brightness for each of the three base colours for all four pushbutton indicators.\n" +
        "Values:\n" +
        "Byte 1: Red brightness.\n 0  255. Brightness level for the red colour in the 4 indicator groups. (Default is 255)\n" +
        "Byte 2: Green brightness.\n 0 - 255. Brightness level for the green colour in the 4 indicator groups. (Default is 85)\n" +
        "Byte 3: Blue brightness.\n 0 - 255. Brightness level for the blue colour in the 4 indicator groups. (Default is 85)\n" +
        "Byte 4: Not used - must be set to 0"], [id:  15, size: 2, type: "number", range: "0..8191", defaultValue: "8191", required: false, readonly: false,
        name: "Associations groups, transmission when included secure",
        description: "This parameter specifies if commands are transmitted as a secure message for each of the association groups.\n" +
        "The values below are bitmasks and can be added up to select several options.\n" +
        "Values:\n" +
        "   0 = All messages in all groups are sent as insecure\n" +
        "   1 = Messages in association group 2 are sent as secure\n" +
        "   2 = Messages in association group 3 are sent as secure\n" +
        "   4 = Messages in association group 4 are sent as secure\n" +
        "   8 = Messages in association group 5 are sent as secure\n" +
        "  16 = Messages in association group 6 are sent as secure\n" +
        "  32 = Messages in association group 7 are sent as secure\n" +
        "  64 = Messages in association group 8 are sent as secure\n" +
        " 128 = Messages in association group 9 are sent as secure\n" +
        " 256 = Messages in association group 10 are sent as secure\n" +
        " 512 = Messages in association group 11 are sent as secure\n" +
        "1024 = Messages in association group 12 are sent as secure\n" +
        "2048 = Messages in association group 13 are sent as secure\n" +
        "4096 = Messages in association group 14 are sent as secure\n" +
        "8191 = Messages in all association groups are sent as secure. (Default)"], [id:  16, size: 1, type: "enum", defaultValue: "0", required: false, readonly: false,
        name: "Pushbutton 1 functionality",
        description: "This parameter specifies the functionality of pushbutton 1.",
        options: ["0" : "0: Standard toggle-function, the state is switched between on and off, dimming up and down (Default)",
            "1" : "1: Automatic turn off after the time has expired (staircase lighting function), the time is specified in the next configuration parameter",
            "2" : "2: Automatic turn on after the time has expired, the time is specified in the next configuration parameter",
            "3" : "3: Always turn off or dim down. Using this parameter, the pushbutton can only send off or dim down commands.\n" +
            "Use this in pair with another pushbutton with value 4 (see chapter 2.6 in the manual)",
            "4" : "4: Always turn on or dim up. Using this parameter, the pushbutton can only send on or dim up commands.\n" +
            "Use this in pair with another pushbutton with value 3 (see chapter 2.6 in the manual)"]], [id:  17, size: 2, type: "number", range: "0..43200", defaultValue: "300", required: false, readonly: false,
        name: "Timer value for pushbutton 1",
        description: "This parameter specifies the time used in previous configuration parameter. This parameter is only applicable if previous parameter has value 1 or 2.\n" +
        "Values:\n0..43200: Specifies the time in seconds. Default is 300 = 5 minutes."], [id:  18, size: 4, type: "number", range: "0..4294967295", defaultValue: "33488896", required: false, readonly: false,
        name: "Multilevel Switch Set values for pushbutton 1",
        description: "This parameter specifies the Multilevel Switch value sent on a single activation of button 1.\n" +
        "Values:\n" +
        "Byte 1: Disable/enable.\n 0 - 1. 1 = Enabled  a single activation will send commands to devices in association group 4. Devices will receive commands with the values set in Byte 2 and 3 (Default)\n" +
        "Byte 2: Upper switch value.\n 0 - 99, 255. When single pressing the pushbutton for ON, a Multilevel Switch Set with this value will be send to devices in association group 4 (Default = 255)\n" +
        "Byte 3: Lower switch value.\n 0 - 99. When single pressing the pushbutton for OFF, a Multilevel Switch Set with this value will be send to devices in association group 4 (Default = 0)\n" +
        "Byte 4: Not used - must be set to 0"], [id:  19, size: 1, type: "enum", defaultValue: "0", required: false, readonly: false,
        name: "Binary Switch Set support for pushbutton 1.",
        description: "This parameter specifies how received Binary Switch Set commands are handled",
        options: ["0" : "0: Binary Switch Set only controls the pushbutton indicator LEDs, ON/OFF (Default)",
            "1" : "1: Binary Switch Set controls the internal switch status and the pushbutton indicator LEDs",
            "2" : "2: Binary Switch Set is handled as if the user had activated the pushbutton, including transmission of commands via the association groups"]], [id:  20, size: 1, type: "enum", defaultValue: "7", required: false, readonly: false,
        name: "Pushbutton 1 LED indication",
        description: "This parameter specifies how the LED indication is controlled.",
        options: ["0" : "0: Internal LED control is disabled, only external commands control the indication",
            "1" : "1: The LED indication follows the switch status",
            "2" : "2: The LED indication follows the switch status  with inverted functionality",
            "3" : "3: Same as 1",
            "4" : "4: Same as 2",
            "5" : "5: The LED indication follows the status of the internal dimmer, ON or OFF",
            "6" : "6: The LED indication follows the status of the internal dimmer with inverted functionality, ON or OFF",
            "7" : "7: The LED indicator is ON for 5 seconds when the pushbutton is activated. (Default)"]], [id:  21, size: 1, type: "enum", defaultValue: "1", required: false, readonly: false,
        name: "Color commands for pushbutton 1 LED indication.",
        description: "This parameter specifies how Command Class Switch Color commands are handled",
        options: ["0" : "0: Direct control; the LED indication shows the received colour immediately, until the pushbutton is activated",
            "1" : "1: Color command sets the color for OFF indication (Default)",
            "2" : "2: Color command sets the color for ON indication"]], [id:  22, size: 4, type: "number", range: "0..4294967295", defaultValue: "32512", required: false, readonly: false,
        name: "ON indication RGB-saturation for pushbutton 1.",
        description: "This parameter specifies the saturation levels for the red, green and blue LEDs, when ON status is indicated. (Default is the ON-state indicated by a 50% blue colour).\n" +
        "Values:\n" +
        "Byte 1: Colour saturation, red.\n 0 - 255. Specifies the saturation for the red LEDs. (Default is 0)\n" +
        "Byte 2: Colour saturation, green.\n 0 - 255. Specifies the saturation for the green LEDs. (Default is 0)\n" +
        "Byte 3: Colour saturation, blue.\n 0 - 255. Specifies the saturation for the blue LEDs. (Default is 127)\n" +
        "Byte 4: LED control.\n 0 - 255. LED indicator control; see the application note about this parameter. (Default is 0)"], [id:  23, size: 4, type: "number", range: "0..4294967295", defaultValue: "32512", required: false, readonly: false,
        name: "OFF indication RGB-saturation for pushbutton 1.",
        description: "This parameter specifies the saturation levels for the red, green and blue LEDs, when OFF status is indicated. (Default is the OFF-state indicated as a low white light on 5%).\n" +
        "Values:\n" +
        "Byte 1: Colour saturation, red.\n 0 - 255. Specifies the saturation for the red LEDs. (Default is 47)\n" +
        "Byte 2: Colour saturation, green.\n 0 - 255. Specifies the saturation for the green LEDs. (Default is 47)\n" +
        "Byte 3: Colour saturation, blue.\n 0 - 255. Specifies the saturation for the blue LEDs. (Default is 47)\n" +
        "Byte 4: LED control.\n 0 - 255. LED indicator control; see the application note about this parameter. (Default is 0)"], [id:  24, size: 1, type: "enum", defaultValue: "0", required: false, readonly: false,
        name: "Pushbutton 2 functionality",
        description: "This parameter specifies the functionality of pushbutton 2.",
        options: ["0" : "0: Standard toggle-function, the state is switched between on and off, dimming up and down (Default)",
            "1" : "1: Automatic turn off after the time has expired (staircase lighting function), the time is specified in the next configuration parameter",
            "2" : "2: Automatic turn on after the time has expired, the time is specified in the next configuration parameter",
            "3" : "3: Always turn off or dim down. Using this parameter, the pushbutton can only send off or dim down commands.\n" +
            "Use this in pair with another pushbutton with value 4 (see chapter 2.6 in the manual)",
            "4" : "4: Always turn on or dim up. Using this parameter, the pushbutton can only send on or dim up commands.\n" +
            "Use this in pair with another pushbutton with value 3 (see chapter 2.6 in the manual)"]], [id:  25, size: 2, type: "number", range: "0..43200", defaultValue: "300", required: false, readonly: false,
        name: "Timer value for pushbutton 2",
        description: "This parameter specifies the time used in previous configuration parameter. This parameter is only applicable if previous parameter has value 1 or 2.\n" +
        "Values:\n0..43200: Specifies the time in seconds. Default is 300 = 5 minutes."], [id:  26, size: 4, type: "number", range: "0..4294967295", defaultValue: "33488896", required: false, readonly: false,
        name: "Multilevel Switch Set values for pushbutton 1",
        description: "This parameter specifies the Multilevel Switch value sent on a single activation of button 1.\n" +
        "Values:\n" +
        "Byte 1: Disable/enable.\n 0 - 1. 1 = Enabled  a single activation will send commands to devices in association group 4. Devices will receive commands with the values set in Byte 2 and 3 (Default)\n" +
        "Byte 2: Upper switch value.\n 0 - 99, 255. When single pressing the pushbutton for ON, a Multilevel Switch Set with this value will be send to devices in association group 4 (Default = 255)\n" +
        "Byte 3: Lower switch value.\n 0 - 99. When single pressing the pushbutton for OFF, a Multilevel Switch Set with this value will be send to devices in association group 4 (Default = 0)\n" +
        "Byte 4: Not used - must be set to 0"], [id:  27, size: 1, type: "enum", defaultValue: "0", required: false, readonly: false,
        name: "Binary Switch Set support for pushbutton 1.",
        description: "This parameter specifies how received Binary Switch Set commands are handled",
        options: ["0" : "0: Binary Switch Set only controls the pushbutton indicator LEDs, ON/OFF (Default)",
            "1" : "1: Binary Switch Set controls the internal switch status and the pushbutton indicator LEDs",
            "2" : "2: Binary Switch Set is handled as if the user had activated the pushbutton, including transmission of commands via the association groups"]], [id:  28, size: 1, type: "enum", defaultValue: "7", required: false, readonly: false,
        name: "Pushbutton 2 LED indication",
        description: "This parameter specifies how the LED indication is controlled.",
        options: ["0" : "0: Internal LED control is disabled, only external commands control the indication",
            "1" : "1: The LED indication follows the switch status",
            "2" : "2: The LED indication follows the switch status  with inverted functionality",
            "3" : "3: Same as 1",
            "4" : "4: Same as 2",
            "5" : "5: The LED indication follows the status of the internal dimmer, ON or OFF",
            "6" : "6: The LED indication follows the status of the internal dimmer with inverted functionality, ON or OFF",
            "7" : "7: The LED indicator is ON for 5 seconds when the pushbutton is activated. (Default)"]], [id:  29, size: 1, type: "enum", defaultValue: "1", required: false, readonly: false,
        name: "Color commands for pushbutton 2 LED indication.",
        description: "This parameter specifies how Command Class Switch Color commands are handled",
        options: ["0" : "0: Direct control; the LED indication shows the received colour immediately, until the pushbutton is activated",
            "1" : "1: Color command sets the color for OFF indication (Default)",
            "2" : "2: Color command sets the color for ON indication"]], [id:  30, size: 4, type: "number", range: "0..4294967295", defaultValue: "32512", required: false, readonly: false,
        name: "ON indication RGB-saturation for pushbutton 2.",
        description: "This parameter specifies the saturation levels for the red, green and blue LEDs, when ON status is indicated. (Default is the ON-state indicated by a 50% blue colour).\n" +
        "Values:\n" +
        "Byte 1: Colour saturation, red.\n 0 - 255. Specifies the saturation for the red LEDs. (Default is 0)\n" +
        "Byte 2: Colour saturation, green.\n 0 - 255. Specifies the saturation for the green LEDs. (Default is 0)\n" +
        "Byte 3: Colour saturation, blue.\n 0 - 255. Specifies the saturation for the blue LEDs. (Default is 127)\n" +
        "Byte 4: LED control.\n 0 - 255. LED indicator control; see the application note about this parameter. (Default is 0)"], [id:  31, size: 4, type: "number", range: "0..4294967295", defaultValue: "32512", required: false, readonly: false,
        name: "OFF indication RGB-saturation for pushbutton 2.",
        description: "This parameter specifies the saturation levels for the red, green and blue LEDs, when OFF status is indicated. (Default is the OFF-state indicated as a low white light on 5%).\n" +
        "Values:\n" +
        "Byte 1: Colour saturation, red.\n 0 - 255. Specifies the saturation for the red LEDs. (Default is 47)\n" +
        "Byte 2: Colour saturation, green.\n 0 - 255. Specifies the saturation for the green LEDs. (Default is 47)\n" +
        "Byte 3: Colour saturation, blue.\n 0 - 255. Specifies the saturation for the blue LEDs. (Default is 47)\n" +
        "Byte 4: LED control.\n 0 - 255. LED indicator control; see the application note about this parameter. (Default is 0)"], [id:  32, size: 1, type: "enum", defaultValue: "0", required: false, readonly: false,
        name: "Pushbutton 3 functionality",
        description: "This parameter specifies the functionality of pushbutton 2.",
        options: ["0" : "0: Standard toggle-function, the state is switched between on and off, dimming up and down (Default)",
            "1" : "1: Automatic turn off after the time has expired (staircase lighting function), the time is specified in the next configuration parameter",
            "2" : "2: Automatic turn on after the time has expired, the time is specified in the next configuration parameter",
            "3" : "3: Always turn off or dim down. Using this parameter, the pushbutton can only send off or dim down commands.\n" +
            "Use this in pair with another pushbutton with value 4 (see chapter 2.6 in the manual)",
            "4" : "4: Always turn on or dim up. Using this parameter, the pushbutton can only send on or dim up commands.\n" +
            "Use this in pair with another pushbutton with value 3 (see chapter 2.6 in the manual)"]], [id:  33, size: 2, type: "number", range: "0..43200", defaultValue: "300", required: false, readonly: false,
        name: "Timer value for pushbutton 3",
        description: "This parameter specifies the time used in previous configuration parameter. This parameter is only applicable if previous parameter has value 1 or 2.\n" +
        "Values:\n0..43200: Specifies the time in seconds. Default is 300 = 5 minutes."], [id:  34, size: 4, type: "number", range: "0..4294967295", defaultValue: "33488896", required: false, readonly: false,
        name: "Multilevel Switch Set values for pushbutton 3",
        description: "This parameter specifies the Multilevel Switch value sent on a single activation of button 1.\n" +
        "Values:\n" +
        "Byte 1: Disable/enable.\n 0 - 1. 1 = Enabled  a single activation will send commands to devices in association group 4. Devices will receive commands with the values set in Byte 2 and 3 (Default)\n" +
        "Byte 2: Upper switch value.\n 0 - 99, 255. When single pressing the pushbutton for ON, a Multilevel Switch Set with this value will be send to devices in association group 4 (Default = 255)\n" +
        "Byte 3: Lower switch value.\n 0 - 99. When single pressing the pushbutton for OFF, a Multilevel Switch Set with this value will be send to devices in association group 4 (Default = 0)\n" +
        "Byte 4: Not used - must be set to 0"], [id: 35, size: 1, type: "enum", defaultValue: "0", required: false, readonly: false,
        name: "Binary Switch Set support for pushbutton 3.",
        description: "This parameter specifies how received Binary Switch Set commands are handled",
        options: ["0" : "0: Binary Switch Set only controls the pushbutton indicator LEDs, ON/OFF (Default)",
            "1" : "1: Binary Switch Set controls the internal switch status and the pushbutton indicator LEDs",
            "2" : "2: Binary Switch Set is handled as if the user had activated the pushbutton, including transmission of commands via the association groups"]], [id:  36, size: 1, type: "enum", defaultValue: "7", required: false, readonly: false,
        name: "Pushbutton 3 LED indication",
        description: "This parameter specifies how the LED indication is controlled.",
        options: ["0" : "0: Internal LED control is disabled, only external commands control the indication",
            "1" : "1: The LED indication follows the switch status",
            "2" : "2: The LED indication follows the switch status  with inverted functionality",
            "3" : "3: Same as 1",
            "4" : "4: Same as 2",
            "5" : "5: The LED indication follows the status of the internal dimmer, ON or OFF",
            "6" : "6: The LED indication follows the status of the internal dimmer with inverted functionality, ON or OFF",
            "7" : "7: The LED indicator is ON for 5 seconds when the pushbutton is activated. (Default)"]], [id:  37, size: 1, type: "enum", defaultValue: "1", required: false, readonly: false,
        name: "Color commands for pushbutton 3 LED indication.",
        description: "This parameter specifies how Command Class Switch Color commands are handled",
        options: ["0" : "0: Direct control; the LED indication shows the received colour immediately, until the pushbutton is activated",
            "1" : "1: Color command sets the color for OFF indication (Default)",
            "2" : "2: Color command sets the color for ON indication"]], [id:  38, size: 4, type: "number", range: "0..4294967295", defaultValue: "32512", required: false, readonly: false,
        name: "ON indication RGB-saturation for pushbutton 3.",
        description: "This parameter specifies the saturation levels for the red, green and blue LEDs, when ON status is indicated. (Default is the ON-state indicated by a 50% blue colour).\n" +
        "Values:\n" +
        "Byte 1: Colour saturation, red.\n 0 - 255. Specifies the saturation for the red LEDs. (Default is 0)\n" +
        "Byte 2: Colour saturation, green.\n 0 - 255. Specifies the saturation for the green LEDs. (Default is 0)\n" +
        "Byte 3: Colour saturation, blue.\n 0 - 255. Specifies the saturation for the blue LEDs. (Default is 127)\n" +
        "Byte 4: LED control.\n 0 - 255. LED indicator control; see the application note about this parameter. (Default is 0)"], [id:  39, size: 4, type: "number", range: "0..4294967295", defaultValue: "32512", required: false, readonly: false,
        name: "OFF indication RGB-saturation for pushbutton 3.",
        description: "This parameter specifies the saturation levels for the red, green and blue LEDs, when OFF status is indicated. (Default is the OFF-state indicated as a low white light on 5%).\n" +
        "Values:\n" +
        "Byte 1: Colour saturation, red.\n 0 - 255. Specifies the saturation for the red LEDs. (Default is 47)\n" +
        "Byte 2: Colour saturation, green.\n 0 - 255. Specifies the saturation for the green LEDs. (Default is 47)\n" +
        "Byte 3: Colour saturation, blue.\n 0 - 255. Specifies the saturation for the blue LEDs. (Default is 47)\n" +
        "Byte 4: LED control.\n 0 - 255. LED indicator control; see the application note about this parameter. (Default is 0)"],  [id:  40, size: 1, type: "enum", defaultValue: "0", required: false, readonly: false,
        name: "Pushbutton 4 functionality",
        description: "This parameter specifies the functionality of pushbutton 2.",
        options: ["0" : "0: Standard toggle-function, the state is switched between on and off, dimming up and down (Default)",
            "1" : "1: Automatic turn off after the time has expired (staircase lighting function), the time is specified in the next configuration parameter",
            "2" : "2: Automatic turn on after the time has expired, the time is specified in the next configuration parameter",
            "3" : "3: Always turn off or dim down. Using this parameter, the pushbutton can only send off or dim down commands.\n" +
            "Use this in pair with another pushbutton with value 4 (see chapter 2.6 in the manual)",
            "4" : "4: Always turn on or dim up. Using this parameter, the pushbutton can only send on or dim up commands.\n" +
            "Use this in pair with another pushbutton with value 3 (see chapter 2.6 in the manual)"]], [id:  41, size: 2, type: "number", range: "0..43200", defaultValue: "300", required: false, readonly: false,
        name: "Timer value for pushbutton 4",
        description: "This parameter specifies the time used in previous configuration parameter. This parameter is only applicable if previous parameter has value 1 or 2.\n" +
        "Values:\n0..43200: Specifies the time in seconds. Default is 300 = 5 minutes."], [id:  42, size: 4, type: "number", range: "0..4294967295", defaultValue: "33488896", required: false, readonly: false,
        name: "Multilevel Switch Set values for pushbutton 4",
        description: "This parameter specifies the Multilevel Switch value sent on a single activation of button 1.\n" +
        "Values:\n" +
        "Byte 1: Disable/enable.\n 0 - 1. 1 = Enabled  a single activation will send commands to devices in association group 4. Devices will receive commands with the values set in Byte 2 and 3 (Default)\n" +
        "Byte 2: Upper switch value.\n 0 - 99, 255. When single pressing the pushbutton for ON, a Multilevel Switch Set with this value will be send to devices in association group 4 (Default = 255)\n" +
        "Byte 3: Lower switch value.\n 0 - 99. When single pressing the pushbutton for OFF, a Multilevel Switch Set with this value will be send to devices in association group 4 (Default = 0)\n" +
        "Byte 4: Not used - must be set to 0"], [id: 43, size: 1, type: "enum", defaultValue: "0", required: false, readonly: false,
        name: "Binary Switch Set support for pushbutton 4.",
        description: "This parameter specifies how received Binary Switch Set commands are handled",
        options: ["0" : "0: Binary Switch Set only controls the pushbutton indicator LEDs, ON/OFF (Default)",
            "1" : "1: Binary Switch Set controls the internal switch status and the pushbutton indicator LEDs",
            "2" : "2: Binary Switch Set is handled as if the user had activated the pushbutton, including transmission of commands via the association groups"]], [id:  44, size: 1, type: "enum", defaultValue: "7", required: false, readonly: false,
        name: "Pushbutton 4 LED indication",
        description: "This parameter specifies how the LED indication is controlled.",
        options: ["0" : "0: Internal LED control is disabled, only external commands control the indication",
            "1" : "1: The LED indication follows the switch status",
            "2" : "2: The LED indication follows the switch status  with inverted functionality",
            "3" : "3: Same as 1",
            "4" : "4: Same as 2",
            "5" : "5: The LED indication follows the status of the internal dimmer, ON or OFF",
            "6" : "6: The LED indication follows the status of the internal dimmer with inverted functionality, ON or OFF",
            "7" : "7: The LED indicator is ON for 5 seconds when the pushbutton is activated. (Default)"]], [id:  45, size: 1, type: "enum", defaultValue: "1", required: false, readonly: false,
        name: "Color commands for pushbutton 4 LED indication.",
        description: "This parameter specifies how Command Class Switch Color commands are handled",
        options: ["0" : "0: Direct control; the LED indication shows the received colour immediately, until the pushbutton is activated",
            "1" : "1: Color command sets the color for OFF indication (Default)",
            "2" : "2: Color command sets the color for ON indication"]], [id:  46, size: 4, type: "number", range: "0..4294967295", defaultValue: "32512", required: false, readonly: false,
        name: "ON indication RGB-saturation for pushbutton 4.",
        description: "This parameter specifies the saturation levels for the red, green and blue LEDs, when ON status is indicated. (Default is the ON-state indicated by a 50% blue colour).\n" +
        "Values:\n" +
        "Byte 1: Colour saturation, red.\n 0 - 255. Specifies the saturation for the red LEDs. (Default is 0)\n" +
        "Byte 2: Colour saturation, green.\n 0 - 255. Specifies the saturation for the green LEDs. (Default is 0)\n" +
        "Byte 3: Colour saturation, blue.\n 0 - 255. Specifies the saturation for the blue LEDs. (Default is 127)\n" +
        "Byte 4: LED control.\n 0 - 255. LED indicator control; see the application note about this parameter. (Default is 0)"], [id:  47, size: 4, type: "number", range: "0..4294967295", defaultValue: "32512", required: false, readonly: false,
        name: "OFF indication RGB-saturation for pushbutton 4.",
        description: "This parameter specifies the saturation levels for the red, green and blue LEDs, when OFF status is indicated. (Default is the OFF-state indicated as a low white light on 5%).\n" +
        "Values:\n" +
        "Byte 1: Colour saturation, red.\n 0 - 255. Specifies the saturation for the red LEDs. (Default is 47)\n" +
        "Byte 2: Colour saturation, green.\n 0 - 255. Specifies the saturation for the green LEDs. (Default is 47)\n" +
        "Byte 3: Colour saturation, blue.\n 0 - 255. Specifies the saturation for the blue LEDs. (Default is 47)\n" +
        "Byte 4: LED control.\n 0 - 255. LED indicator control; see the application note about this parameter. (Default is 0)"]
]


}


// THIS IS THE END OF THE FILE