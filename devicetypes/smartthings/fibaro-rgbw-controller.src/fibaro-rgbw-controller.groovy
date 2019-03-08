/**
 *  Device Type Definition File
 *
 *  Device Type:		Fibaro RGBW Controller
 *  File Name:			fibaro-rgbw-controller.groovy
 *	Initial Release:	2015-01-04
 *	Author:				Todd Wackford
 *  Email:				todd@wackford.net
 *
 *  Copyright 2019 SmartThings
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
		capability "Refresh"
		capability "Sensor"
		capability "Configuration"
		capability "Color Control"
        capability "Color Temperature"
        capability "Power Meter"
        capability "Health Check"

		fingerprint mfr: "010F", prod: "0900"
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
        tiles(scale: 2) {
            multiAttributeTile(name:"switch", type: "lighting", width: 3, height: 2, canChangeIcon: true) {
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
                    attributeState "color", action:"setColor"
                }
            }
        }
        controlTile("colorTempSliderControl", "device.colorTemperature", "slider", width: 1, height: 1, inactiveLabel: false, range:"(2700..6500)") {
            state "colorTemperature", action:"color temperature.setColorTemperature"
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

        main(["switch"])
        details(["switch",
                 "colorTempSliderControl",
                 "power",
                 "refresh"])
	}
}

def installed() {
    def cmds = []
    cmds << zwave.switchMultilevelV3.switchMultilevelGet()
}

def setColor(value) {
    def result = []
    log.debug "setColor: ${value}"
    if (value.hex) {
        def c = value.hex.findAll(/[0-9a-fA-F]{2}/).collect { Integer.parseInt(it, 16) }
        result << zwave.switchColorV3.switchColorSet(red:c[0], green:c[1], blue:c[2], warmWhite:0, coldWhite:0)
    } else {
        def rgb = huesatToRGB(value.hue, value.saturation)
        result << zwave.switchColorV3.switchColorSet(red: rgb[0], green: rgb[1], blue: rgb[2], warmWhite:0, coldWhite:0)
    }

    if(value.hue) sendEvent(name: "hue", value: value.hue)
    if(value.hex) sendEvent(name: "color", value: value.hex)
    if(value.switch) sendEvent(name: "switch", value: value.switch)
    if(value.saturation) sendEvent(name: "saturation", value: value.saturation)

    commands(result)
}

def setLevel(level, duration=null) {
    log.debug "setLevel >> value: $value"
    def valueaux = value as Integer
    def level = Math.max(Math.min(valueaux, 99), 0)
    delayBetween([zwave.basicV1.basicSet(value: level), zwave.switchMultilevelV1.switchMultilevelGet()], 2000)
}

private getCOLOR_TEMP_MAX() { 6500 }
private getCOLOR_TEMP_MIN() { 2700 }
private getCOLOR_TEMP_DIFF() { COLOR_TEMP_MAX - COLOR_TEMP_MIN }

def setColorTemperature(temp) {
    if(temp > COLOR_TEMP_MAX)
        temp = COLOR_TEMP_MAX
    else if(temp < COLOR_TEMP_MIN)
        temp = COLOR_TEMP_MIN
    log.debug "setColorTemperature($temp)"
    def warmValue = ((COLOR_TEMP_MAX - temp) / COLOR_TEMP_DIFF * 255) as Integer
    def coldValue = 255 - warmValue
    def cmds = [zwave.switchColorV3.switchColorSet(red: 0, green: 0, blue: 0, warmWhite: warmValue, coldWhite: coldValue)]
    sendEvent(name: "color temperature", value: temp)
    commands(cmds)
}

def configure() {
	log.debug "Configuring Device For SmartThings Use"
    def cmds = []

    sendEvent(name: "checkInterval", value: 60 * 60 * 12, displayed: false, data: [protocol: "zwave", hubHardwareId: device.hub.hardwareID])

    // send associate to group 3 to get sensor data reported only to hub
    cmds << zwave.associationV2.associationSet(groupingIdentifier:5, nodeId:[zwaveHubNodeId])

    delayBetween(cmds, 500)

}

def parse(description) {
    def result = null
    if (description.startsWith("Err 106")) {
        state.sec = 0
    } else if (description != "updated") {
        def cmd = zwave.parse(description)
        if (cmd) {
            result = zwaveEvent(cmd)
            log.debug("'$description' parsed to $result")
        } else {
            log.debug("Couldn't zwave.parse '$description'")
        }
    }
    result
}

def createEvent(physicalgraph.zwave.commands.multichannelv3.MultiChannelCmdEncap cmd, Map item1) {
	def encapsulatedCommand = cmd.encapsulatedCommand() // can specify command class versions here like in zwave.parse
	//log.debug ("Command from endpoint ${cmd.sourceEndPoint}: ${encapsulatedCommand}")
	if ((cmd.sourceEndPoint >= 1) && (cmd.sourceEndPoint <= 5)) { // we don't need color report
    	//don't do anything
    } else {
    	if (encapsulatedCommand) {
			zwaveEvent(encapsulatedCommand)
        }
	}
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

def zwaveEvent(physicalgraph.zwave.commands.switchmultilevelv3.SwitchMultilevelSet cmd) {
    dimmerEvents(cmd)
}

private dimmerEvents(physicalgraph.zwave.Command cmd) {
    def value = (cmd.value ? "on" : "off")
    def result = [createEvent(name: "switch", value: value)]
    if (cmd.value && cmd.value <= 100) {
        result << createEvent(name: "level", value: cmd.value == 99 ? 100 : cmd.value)
    }
    return result
}

def zwaveEvent(physicalgraph.zwave.commands.switchmultilevelv3.SwitchMultilevelStopLevelChange cmd) {
    response(zwave.basicV1.basicGet())
}

def zwaveEvent(physicalgraph.zwave.commands.configurationv2.ConfigurationReport cmd, item1) {
	log.debug "${device.displayName} parameter '${cmd.parameterNumber}' with a byte size of '${cmd.size}' is set to '${cmd.configurationValue}'"
}

def handleMeterReport(cmd){
    if (cmd.meterType == 1) {
        if (cmd.scale == 0) {
            createEvent(name: "energy", value: cmd.scaledMeterValue, unit: "kWh")
        } else if (cmd.scale == 1) {
            createEvent(name: "energy", value: cmd.scaledMeterValue, unit: "kVAh")
        } else if (cmd.scale == 2) {
            createEvent(name: "power", value: Math.round(cmd.scaledMeterValue), unit: "W")
        }
    }
}

def zwaveEvent(physicalgraph.zwave.commands.meterv3.MeterReport cmd) {
    log.debug "v3 Meter report: "+cmd
    handleMeterReport(cmd)
}

def zwaveEvent(physicalgraph.zwave.Command cmd) {
     // Handles any Z-Wave commands we aren't interested in
     log.debug "UNHANDLED COMMAND $cmd"
}

def on() {
	log.debug "on()"
	delayBetween([zwave.basicV1.basicSet(value: 0xFF),
    			  zwave.switchMultilevelV3.switchMultilevelGet()], 5000)
}

def off() {
	log.debug "off()"
	delayBetween ([zwave.basicV1.basicSet(value: 0x00), zwave.switchMultilevelV3.switchMultilevelGet()], 5000)
}


def ping() {
    [zwave.switchMultilevelV1.switchMultilevelGet(), zwave.meterV3.meterGet(scale: 2)]
}

def refresh() {
	def cmd = []
	cmd << response(zwave.switchMultilevelV1.switchMultilevelGet())
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
        cmds << zwave.configurationV1.configurationSet(configurationValue: pValue, parameterNumber: pNumber, size: pSize)

        cmds << zwave.configurationV1.configurationGet(parameterNumber: pNumber)
        delayBetween(cmds, 1500)
    }
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

private command(physicalgraph.zwave.Command cmd) {
    if (zwaveInfo.zw.endsWith("s")) {) {
        zwave.securityV1.securityMessageEncapsulation().encapsulate(cmd)
    } else {
        cmd
    }
}

private commands(commands, delay=200) {
    delayBetween(commands.collect{ command(it) }, delay)
}
