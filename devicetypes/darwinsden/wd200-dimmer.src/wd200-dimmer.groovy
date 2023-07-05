/**
 *  HomeSeer HS-WD200+
 *
 *  Copyright 2018, 2019, 2020 HomeSeer, DarwinsDen.com
 *
 *  Modified from the work by DarwinsDen device handler for the WD100 version 1.03
 *
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
 *	Author: HomeSeer, darwin@darwinsden.com
 *
 *	Changelog:
 *  2.1       22-Aug-2020 Added button value support for new app automations. Added preference option to choose between 'classic' 12 button multi-tap 
 *                        actions or one button supporting all 12 multi-tap values. Updated preference settings to no longer require configure button. 
 *  1.1.dd.1  13-Aug-2020 Updates to better support the new SmartThings app. Thank you @mikerossman. (darwin@darwinsden.com)
 *  1.0.dd.9  13-Feb-2019 Added dummy setLevel command with duration for compatibility with HA Bridge, others? (darwin@darwinsden.com)
 *  1.0.dd.8  28-Jul-2018 Additional protection against floating point default preference values. (darwin@darwinsden.com)
 *  1.0.dd.6  27-Jul-2018 Added call to set led flash rate and added protection against floating point default preference values (darwin@darwinsden.com)
 *  1.0.dd.5  26-Mar-2018 Corrected issues: 1) Turning off all LEDs did not return switch to Normal mode,
 *                        2) Turning off last lit LED would set Normal mode, but leave LED state as on (darwin@darwinsden.com)
 *  1.0.dd.4  28-Feb-2018 Updated all LED option to use LED=0 (8 will be depricated) and increased delay by 50ms (darwin@darwinsden.com)
 *  1.0.dd.3  19-Feb-2018 Corrected bit-wise blink off operator (darwin@darwinsden.com)
 *  1.0.dd.2  16-Feb 2018 Added button number labels to virtual buttons and reduced size (darwin@darwinsden.com)
 *  1.0.dd.1  15-Feb 2018 Added option to set all LED's simultaneously(darwin@darwinsden.com)
 *  1.0	      Jan    2017 Initial Version
 *
 */
metadata {
    definition(name: "WD200+ Dimmer", namespace: "darwinsden", author: "support@homeseer.com") {
        capability "Switch Level"
        capability "Actuator"
        capability "Indicator"
        capability "Switch"
        capability "Polling"
        capability "Refresh"
        capability "Sensor"
        capability "Button"
        capability "Configuration"

        command "tapUp2"
        command "tapDown2"
        command "tapUp3"
        command "tapDown3"
        command "tapUp4"
        command "tapDown4"
        command "tapUp5"
        command "tapDown5"
        command "holdUp"
        command "holdDown"
        command "setStatusLed"
        command "setSwitchModeNormal"
        command "setSwitchModeStatus"
        command "setDefaultColor"
        command "setBlinkDurationMilliseconds"

        fingerprint mfr: "000C", prod: "4447", model: "3036"
    }

    simulator {
        status "on": "command: 2003, payload: FF"
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

    preferences {
        input "doubleTapToFullBright", "bool", title: "Double-Tap Up sets to full brightness", defaultValue: false, displayDuringSetup: true, required: false
        input "singleTapToFullBright", "bool", title: "Single-Tap Up sets to full brightness", defaultValue: false, displayDuringSetup: true, required: false
        input "doubleTapDownToDim", "bool", title: "Double-Tap Down sets to 25% level", defaultValue: false, displayDuringSetup: true, required: false
        input "reverseSwitch", "bool", title: "Reverse Switch", defaultValue: false, displayDuringSetup: true, required: false
        input "bottomled", "bool", title: "Bottom LED On if Load is Off", defaultValue: false, displayDuringSetup: true, required: false
        input("localcontrolramprate", "number", title: "Local Ramp Rate: Duration (0-90)(1=1 sec) [default: 3]", defaultValue: 3, range: "0..90", required: false)
        input("remotecontrolramprate", "number", title: "Remote Ramp Rate: duration (0-90)(1=1 sec) [default: 3]", defaultValue: 3, range: "0..90", required: false)
        input("color", "enum", title: "Default LED Color", options: ["White", "Red", "Green", "Blue", "Magenta", "Yellow", "Cyan"], description: "Select Color", required: false)
        input"classicBtns", "bool", title: "Use classic 12 button mapping for multi-tap and hold events instead of default one button, 12 action mapping",
           defaultValue: false, displayDuringSetup: true, required: false
    }

    tiles(scale: 2) {
        multiAttributeTile(name: "switch", type: "lighting", width: 6, height: 4, canChangeIcon: true) {
            tileAttribute("device.switch", key: "PRIMARY_CONTROL") {
                attributeState "on", label: '${name}', action: "switch.off", icon: "st.Home.home30", backgroundColor: "#79b821", nextState: "turningOff"
                attributeState "off", label: '${name}', action: "switch.on", icon: "st.Home.home30", backgroundColor: "#ffffff", nextState: "turningOn"
                attributeState "turningOn", label: '${name}', action: "switch.off", icon: "st.Home.home30", backgroundColor: "#79b821", nextState: "turningOff"
                attributeState "turningOff", label: '${name}', action: "switch.on", icon: "st.Home.home30", backgroundColor: "#ffffff", nextState: "turningOn"
            }
            tileAttribute("device.level", key: "SLIDER_CONTROL") {
                attributeState "level", action: "switch level.setLevel"
            }
            tileAttribute("device.status", key: "SECONDARY_CONTROL") {
                attributeState("default", label: '${currentValue}', unit: "")
            }
        }

        standardTile("refresh", "device.switch", width: 2, height: 2, inactiveLabel: false, decoration: "flat") {
            state "default", label: '', action: "refresh.refresh", icon: "st.secondary.refresh"
        }

        valueTile("firmwareVersion", "device.firmwareVersion", width: 2, height: 2, decoration: "flat", inactiveLabel: false) {
            state "default", label: '${currentValue}'
        }

        valueTile("level", "device.level", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
            state "level", label: '${currentValue} %', unit: "%", backgroundColor: "#ffffff"
        }

        valueTile("tapUp2", "device.button", width: 1, height: 1, decoration: "flat") {
            state "default", label: "Btn-1 UpX2", backgroundColor: "#ffffff", action: "tapUp2"
        }

        valueTile("tapDown2", "device.button", width: 1, height: 1, decoration: "flat") {
            state "default", label: "Btn-2 DnX2", backgroundColor: "#ffffff", action: "tapDown2"
        }

        valueTile("tapUp3", "device.button", width: 1, height: 1, decoration: "flat") {
            state "default", label: "Btn-3 UpX3", backgroundColor: "#ffffff", action: "tapUp3"
        }

        valueTile("tapDown3", "device.button", width: 1, height: 1, decoration: "flat") {
            state "default", label: "Btn-4 DnX3", backgroundColor: "#ffffff", action: "tapDown3"
        }
        valueTile("tapUp1", "device.button", width: 1, height: 1, decoration: "flat") {
            state "default", label: "Btn-7 UpX1", backgroundColor: "#ffffff", action: "tapUp1"
        }

        valueTile("tapDown1", "device.button", width: 1, height: 1, decoration: "flat") {
            state "default", label: "Btn-8 DnX1", backgroundColor: "#ffffff", action: "tapDown1"
        }

        valueTile("tapUp4", "device.button", width: 1, height: 1, decoration: "flat") {
            state "default", label: "Btn-9 UpX4", backgroundColor: "#ffffff", action: "tapUp4"
        }

        valueTile("tapDown4", "device.button", width: 1, height: 1, decoration: "flat") {
            state "default", label: "Btn-10 DnX4", backgroundColor: "#ffffff", action: "tapDown4"
        }

        valueTile("tapUp5", "device.button", width: 1, height: 1, decoration: "flat") {
            state "default", label: "Btn-11 UpX5", backgroundColor: "#ffffff", action: "tapUp5"
        }

        valueTile("tapDown5", "device.button", width: 1, height: 1, decoration: "flat") {
            state "default", label: "Btn-12 DnX5", backgroundColor: "#ffffff", action: "tapDown5"
        }

        valueTile("holdUp", "device.button", width: 1, height: 1, decoration: "flat") {
            state "default", label: "Btn-5 Hld-Up", backgroundColor: "#ffffff", action: "holdUp"
        }

        valueTile("holdDown", "device.button", width: 1, height: 1, decoration: "flat") {
            state "default", label: "Btn-6 Hld-Dn", backgroundColor: "#ffffff", action: "holdDown"
        }

        main(["switch"])

        details(["switch", "tapUp2", "tapDown2", "tapUp3", "tapDown3", "holdUp", "holdDown", "tapUp1", "tapDown1", "tapUp4", "tapDown4", "tapUp5", "tapDown5", "level", "firmwareVersion", "refresh"])
    }
}

def parse(String description) {
    def result = null
    //log.debug(description)
    if (description != "updated") {
        def cmd = zwave.parse(description, [0x20: 1, 0x26: 1, 0x70: 1])
        if (cmd) {
            result = zwaveEvent(cmd)
        }
    }
    /*
    if (!result) {
        log.debug "Parse returned ${result} for command ${cmd}"
    } else {
        log.debug "Parse returned ${result}"
    }
    */
    return result
}

def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicReport cmd) {
    dimmerEvents(cmd)
}

def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicSet cmd) {
    dimmerEvents(cmd)
}

def zwaveEvent(physicalgraph.zwave.commands.switchmultilevelv1.SwitchMultilevelReport cmd) {
    dimmerEvents(cmd)
}

def zwaveEvent(physicalgraph.zwave.commands.switchmultilevelv1.SwitchMultilevelSet cmd) {
    dimmerEvents(cmd)
}

private dimmerEvents(physicalgraph.zwave.Command cmd) {
    def value = (cmd.value ? "on" : "off")
    def result = [createEvent(name: "switch", value: value)]
    state.lastLevel = cmd.value
    if (cmd.value && cmd.value <= 100) {
        result << createEvent(name: "level", value: cmd.value, unit: "%")
    }
    return result
}

def zwaveEvent(physicalgraph.zwave.commands.configurationv1.ConfigurationReport cmd) {
    log.debug "ConfigurationReport $cmd"
    def value = "when off"
    if (cmd.configurationValue[0] == 1) {
        value = "when on"
    }
    if (cmd.configurationValue[0] == 2) {
        value = "never"
    }
    createEvent([name: "indicatorStatus", value: value])
}

def zwaveEvent(physicalgraph.zwave.commands.hailv1.Hail cmd) {
    createEvent([name: "hail", value: "hail", descriptionText: "Switch button was pressed", displayed: false])
}

def zwaveEvent(physicalgraph.zwave.commands.manufacturerspecificv2.ManufacturerSpecificReport cmd) {
    //log.debug "manufacturerId:   ${cmd.manufacturerId}"
    //log.debug "manufacturerName: ${cmd.manufacturerName}"
    state.manufacturer = cmd.manufacturerName
    //log.debug "productId:        ${cmd.productId}"
    //log.debug "productTypeId:    ${cmd.productTypeId}"
    def msr = String.format("%04X-%04X-%04X", cmd.manufacturerId, cmd.productTypeId, cmd.productId)
    updateDataValue("MSR", msr)
    setFirmwareVersion()
    createEvent([descriptionText: "$device.displayName MSR: $msr", isStateChange: false])
}

def zwaveEvent(physicalgraph.zwave.commands.versionv1.VersionReport cmd) {
    //updateDataValue("applicationVersion", "${cmd.applicationVersion}")
    log.debug("received Version Report")
    //log.debug "applicationVersion:      ${cmd.applicationVersion}"
    //log.debug "applicationSubVersion:   ${cmd.applicationSubVersion}"
    state.firmwareVersion = cmd.applicationVersion + '.' + cmd.applicationSubVersion
    //log.debug "zWaveLibraryType:        ${cmd.zWaveLibraryType}"
    //log.debug "zWaveProtocolVersion:    ${cmd.zWaveProtocolVersion}"
    //log.debug "zWaveProtocolSubVersion: ${cmd.zWaveProtocolSubVersion}"
    setFirmwareVersion()
    createEvent([descriptionText: "Firmware V" + state.firmwareVersion, isStateChange: false])
}

def zwaveEvent(physicalgraph.zwave.commands.firmwareupdatemdv2.FirmwareMdReport cmd) {
    log.debug("received Firmware Report")
    //log.debug "checksum:       ${cmd.checksum}"
    //log.debug "firmwareId:     ${cmd.firmwareId}"
    //log.debug "manufacturerId: ${cmd.manufacturerId}" [: ]
}

def zwaveEvent(physicalgraph.zwave.commands.switchmultilevelv1.SwitchMultilevelStopLevelChange cmd) {
    [createEvent(name: "switch", value: "on"), response(zwave.switchMultilevelV1.switchMultilevelGet().format())]
}

def zwaveEvent(physicalgraph.zwave.Command cmd) {
    // Handles all Z-Wave commands we aren't interested in
    [: ]
}

def on() {
    sendEvent(btnResponse("up", "digital"))
    delayBetween([
        zwave.basicV1.basicSet(value: 0xFF).format(),
        zwave.switchMultilevelV1.switchMultilevelGet().format()
    ], 5000)
}

def off() {
    sendEvent(btnResponse("down", "digital"))
    delayBetween([
        zwave.basicV1.basicSet(value: 0x00).format(),
        zwave.switchMultilevelV1.switchMultilevelGet().format()
    ], 5000)
}

def setLevel(value) {
    //log.debug "setLevel >> value: $value"
    def valueaux = value as Integer
    def level = Math.max(Math.min(valueaux, 99), 0)
    if (level > 0) {
        sendEvent(name: "switch", value: "on")
    } else {
        sendEvent(name: "switch", value: "off")
    }
    sendEvent(name: "level", value: level, unit: "%")
    def result = []

    result += response(zwave.basicV1.basicSet(value: level))
    result += response("delay 5000")
    result += response(zwave.switchMultilevelV1.switchMultilevelGet())
    result += response("delay 5000")
    result += response(zwave.switchMultilevelV1.switchMultilevelGet())
}

// dummy setLevel command with duration for compatibility with Home Assistant Bridge (others?)
def setLevel(value, duration) {
    setLevel(value)
}

/*
 *  Set dimmer to status mode, then set the color of the individual LED
 *
 *  led = 1-7
 *  color = 0=0ff
 *          1=red
 *          2=green
 *          3=blue
 *          4=magenta
 *          5=yellow
 *          6=cyan
 *          7=white
 */

def setBlinkDurationMilliseconds(newBlinkDuration) {
    def cmds = []
    if (0 < newBlinkDuration && newBlinkDuration < 25500) {
        log.debug "setting blink duration to: ${newBlinkDuration} ms"
        state.blinkDuration = newBlinkDuration.toInteger() / 100
        log.debug "blink duration config parameter 30 is: ${state.blinkDuration}"
        cmds << zwave.configurationV2.configurationSet(configurationValue: [state.blinkDuration.toInteger()], parameterNumber: 30, size: 1).format()
    } else {
        log.debug "commanded blink duration ${newBlinkDuration} is outside range 0 .. 25500 ms"
    }
    return cmds
}

def setStatusLed(led, color, blink) {
    def cmds = []
    if (state.statusled1 == null) {
        state.statusled1 = 0
        state.statusled2 = 0
        state.statusled3 = 0
        state.statusled4 = 0
        state.statusled5 = 0
        state.statusled6 = 0
        state.statusled7 = 0
        state.blinkval = 0
    }

    /* set led # and color */
    switch (led) {
        case 1:
            state.statusled1 = color
            break
        case 2:
            state.statusled2 = color
            break
        case 3:
            state.statusled3 = color
            break
        case 4:
            state.statusled4 = color
            break
        case 5:
            state.statusled5 = color
            break
        case 6:
            state.statusled6 = color
            break
        case 7:
            state.statusled7 = color
            break
        case 0:
        case 8:
            // Special case - all LED's
            state.statusled1 = color
            state.statusled2 = color
            state.statusled3 = color
            state.statusled4 = color
            state.statusled5 = color
            state.statusled6 = color
            state.statusled7 = color
            break

    }

    if (state.statusled1 == 0 && state.statusled2 == 0 && state.statusled3 == 0 && state.statusled4 == 0 && state.statusled5 == 0 && state.statusled6 == 0 && state.statusled7 == 0) {
        // no LEDS are set, put back to NORMAL mode
        cmds << zwave.configurationV2.configurationSet(configurationValue: [0], parameterNumber: 13, size: 1).format()
    } else {
        // at least one LED is set, put to status mode
        cmds << zwave.configurationV2.configurationSet(configurationValue: [1], parameterNumber: 13, size: 1).format()
    }

    if (led == 8 | led == 0) {
        for (def ledToChange = 1; ledToChange <= 7; ledToChange++) {
            // set color for all LEDs
            cmds << zwave.configurationV2.configurationSet(configurationValue: [color], parameterNumber: ledToChange + 20, size: 1).format()
        }
    } else {
        // set color for specified LED
        cmds << zwave.configurationV2.configurationSet(configurationValue: [color], parameterNumber: led + 20, size: 1).format()
    }

    // check if LED should be blinking
    def blinkval = state.blinkval

    if (blink) {
        switch (led) {
            case 1:
                blinkval = blinkval | 0x1
                break
            case 2:
                blinkval = blinkval | 0x2
                break
            case 3:
                blinkval = blinkval | 0x4
                break
            case 4:
                blinkval = blinkval | 0x8
                break
            case 5:
                blinkval = blinkval | 0x10
                break
            case 6:
                blinkval = blinkval | 0x20
                break
            case 7:
                blinkval = blinkval | 0x40
                break
            case 0:
            case 8:
                blinkval = 0x7F
                break
        }
        cmds << zwave.configurationV2.configurationSet(configurationValue: [blinkval], parameterNumber: 31, size: 1).format()
        state.blinkval = blinkval
        // set blink frequency if not already set, 5=500ms
        if (state.blinkDuration == null | state.blinkDuration < 0 | state.blinkDuration > 255) {
            cmds << zwave.configurationV2.configurationSet(configurationValue: [5], parameterNumber: 30, size: 1).format()
        }
    } else {

        switch (led) {
            case 1:
                blinkval = blinkval & 0xFE
                break
            case 2:
                blinkval = blinkval & 0xFD
                break
            case 3:
                blinkval = blinkval & 0xFB
                break
            case 4:
                blinkval = blinkval & 0xF7
                break
            case 5:
                blinkval = blinkval & 0xEF
                break
            case 6:
                blinkval = blinkval & 0xDF
                break
            case 7:
                blinkval = blinkval & 0xBF
                break
            case 0:
            case 8:
                blinkval = 0
                break
        }
        cmds << zwave.configurationV2.configurationSet(configurationValue: [blinkval], parameterNumber: 31, size: 1).format()
        state.blinkval = blinkval
    }
    delayBetween(cmds, 150)
}

/*
 * Set Dimmer to Normal dimming mode (exit status mode)
 *
 */
def setSwitchModeNormal() {
    def cmds = []
    cmds << zwave.configurationV2.configurationSet(configurationValue: [0], parameterNumber: 13, size: 1).format()
    delayBetween(cmds, 500)
}

/*
 * Set Dimmer to Status mode (exit normal mode)
 *
 */
def setSwitchModeStatus() {
    def cmds = []
    cmds << zwave.configurationV2.configurationSet(configurationValue: [1], parameterNumber: 13, size: 1).format()
    delayBetween(cmds, 500)
}

/*
 * Set the color of the LEDS for normal dimming mode, shows the current dim level
 */
def setDefaultColor(color) {
    def cmds = []
    cmds << zwave.configurationV2.configurationSet(configurationValue: [color], parameterNumber: 14, size: 1).format()
    delayBetween(cmds, 500)
}


def poll() {
    zwave.switchMultilevelV1.switchMultilevelGet().format()
}

def refresh() {
    log.debug "refresh() called"
    configure()
}

def zwaveEvent(physicalgraph.zwave.commands.centralscenev1.CentralSceneNotification cmd) {
    //log.debug("sceneNumber: ${cmd.sceneNumber} keyAttributes: ${cmd.keyAttributes}")
    def result = []

    switch (cmd.sceneNumber) {
        case 1:
            // Up
            switch (cmd.keyAttributes) {
                case 0:
                    // Press Once
                    result += createEvent(btnResponse("up", "physical"))
                    result += createEvent([name: "switch", value: "on", type: "physical"])

                    if (singleTapToFullBright) {
                        result += setLevel(99)
                        result += response("delay 5000")
                        result += response(zwave.switchMultilevelV1.switchMultilevelGet())
                    }
                    break
                case 1:
                    result = createEvent([name: "switch", value: "on", type: "physical"])
                    break
                case 2:
                    // Hold
                    result += createEvent(btnResponse("up_hold", "physical"))
                    result += createEvent([name: "switch", value: "on", type: "physical"])
                    break
                case 3:
                    // 2 Times
                    result += createEvent(btnResponse("up_2x", "physical"))
                    if (doubleTapToFullBright) {
                        result += setLevel(99)
                        result += response("delay 5000")
                        result += response(zwave.switchMultilevelV1.switchMultilevelGet())
                    }
                    break
                case 4:
                    // 3 times
                    result = createEvent(btnResponse("up_3x", "physical"))
                    break
                case 5:
                    // 4 times
                    result = createEvent(btnResponse("up_4x", "physical"))
                    break
                case 6:
                    // 5 times
                    result = createEvent(btnResponse("up_5x", "physical"))
                    break
                default:
                    log.debug("unexpected up press keyAttribute: $cmd.keyAttributes")
            }
            break

        case 2:
            // Down
            switch (cmd.keyAttributes) {
                case 0:
                    // Press Once
                    result += createEvent(btnResponse("down", "physical"))
                    result += createEvent([name: "switch", value: "off", type: "physical"])
                    break
                case 1:
                    result = createEvent([name: "switch", value: "off", type: "physical"])
                    break
                case 2:
                    // Hold
                    result += createEvent(btnResponse("down_hold", "physical"))
                    result += createEvent([name: "switch", value: "off", type: "physical"])
                    break
                case 3:
                    // 2 Times
                    result += createEvent(btnResponse("down_2x", "physical"))
                    if (doubleTapDownToDim) {
                        result += setLevel(25)
                        result += response("delay 5000")
                        result += response(zwave.switchMultilevelV1.switchMultilevelGet())
                    }
                    break
                case 4:
                    // 3 Times
                    result = createEvent(btnResponse("down_3x", "physical"))
                    break
                case 5:
                    // 4 Times
                    result = createEvent(btnResponse("down_4x", "physical"))
                    break
                case 6:
                    // 5 Times
                    result = createEvent(btnResponse("down_5x", "physical"))
                    break
                default:
                    log.debug("unexpected down press keyAttribute: $cmd.keyAttributes")
            }
            break

        default:
            // unexpected case
            log.debug("unexpected scene: $cmd.sceneNumber")
    }
    return result
}

def btnResponse(action, buttonType) {
    sendEvent(name: "status", value: action)
    def eventBtn = 1 
    def eventAction = action
    if (classicBtns) {
      // overrride default buttons
      eventBtn = classicBtn[action]  
      eventAction = "pushed"
    }
    [name: "button", value: eventAction, data: [buttonNumber: eventBtn], descriptionText: action, displayed: false, isStateChange: true, type: buttonType]
}

def tapUp1() {
    sendEvent(btnResponse("up", "digital"))
}

def tapDown1() {
    sendEvent(btnResponse("down", "digital"))
}

def tapUp2() {
    sendEvent(btnResponse("up_2x", "digital"))
}

def tapDown2() {
    sendEvent(btnResponse("down_2x", "digital"))
}

def tapUp3() {
    sendEvent(btnResponse("up_3x", "digital"))
}

def tapDown3() {
    sendEvent(btnResponse("down_3x", "digital"))
}

def tapUp4() {
    sendEvent(btnResponse("up_4x", "digital"))
}

def tapDown4() {
    sendEvent(btnResponse("down_4x", "digital"))
}

def tapUp5() {
    sendEvent(btnResponse("up_5x", "digital"))
}

def tapDown5() {
    sendEvent(btnResponse("down_5x", "digital"))
}

def holdUp() {
    sendEvent(btnResponse("up_hold", "digital"))
}

def holdDown() {
    sendEvent(btnResponse("down_hold", "digital"))
}

def setFirmwareVersion() {
    def versionInfo = ''
    if (state.manufacturer) {
        versionInfo = state.manufacturer + ' '
    }
    if (state.firmwareVersion) {
        versionInfo = versionInfo + "Firmware V" + state.firmwareVersion
    } else {
        versionInfo = versionInfo + "Firmware unknown"
    }
    log.debug "Firmware: ${versionInfo}"
    sendEvent(name: "firmwareVersion", value: versionInfo, isStateChange: true, displayed: false)
}

def configureButtons() {
    def numBtns
    def btnValues
    if (classicBtns) {
      log.debug "setting classic 12 button mapping"
      numBtns = 12
      btnValues = ["pushed"].encodeAsJSON()
    } else {
      log.debug "setting default 1 button mapping"
      numBtns = 1
      btnValues = ["down", "down_hold", "down_2x", "down_3x", "down_4x", "down_5x", "up", "up_hold", "up_2x", "up_3x", "up_4x", "up_5x"].encodeAsJSON()
    }
    sendEvent(name: "numberOfButtons", value: numBtns, displayed: false)
    sendEvent(name: "supportedButtonValues", value: btnValues, displayed: false)
}

def configure() {
    log.debug("configure() called")
    configureButtons()
    def cmds = []
    cmds << setDimRatePrefs()
    cmds << zwave.switchMultilevelV1.switchMultilevelGet().format()
    cmds << zwave.manufacturerSpecificV1.manufacturerSpecificGet().format()
    cmds << zwave.versionV1.versionGet().format()
}

def setDimRatePrefs() {
    log.debug("set prefs")
    def cmds = []

    if (color) {
        switch (color) {
            case "White":
                cmds << zwave.configurationV2.configurationSet(configurationValue: [0], parameterNumber: 14, size: 1).format()
                break
            case "Red":
                cmds << zwave.configurationV2.configurationSet(configurationValue: [1], parameterNumber: 14, size: 1).format()
                break
            case "Green":
                cmds << zwave.configurationV2.configurationSet(configurationValue: [2], parameterNumber: 14, size: 1).format()
                break
            case "Blue":
                cmds << zwave.configurationV2.configurationSet(configurationValue: [3], parameterNumber: 14, size: 1).format()
                break
            case "Magenta":
                cmds << zwave.configurationV2.configurationSet(configurationValue: [4], parameterNumber: 14, size: 1).format()
                break
            case "Yellow":
                cmds << zwave.configurationV2.configurationSet(configurationValue: [5], parameterNumber: 14, size: 1).format()
                break
            case "Cyan":
                cmds << zwave.configurationV2.configurationSet(configurationValue: [6], parameterNumber: 14, size: 1).format()
                break
        }
    }

    if (localcontrolramprate != null) {
        //log.debug localcontrolramprate
        def localRamprate = Math.max(Math.min(localcontrolramprate.toInteger(), 90), 0)
        cmds << zwave.configurationV2.configurationSet(configurationValue: [localRamprate.toInteger()], parameterNumber: 12, size: 1).format()
    }

    if (remotecontrolramprate != null) {
        //log.debug remotecontrolramprate
        def remoteRamprate = Math.max(Math.min(remotecontrolramprate.toInteger(), 90), 0)
        cmds << zwave.configurationV2.configurationSet(configurationValue: [remoteRamprate.toInteger()], parameterNumber: 11, size: 1).format()
    }

    if (reverseSwitch) {
        cmds << zwave.configurationV2.configurationSet(configurationValue: [1], parameterNumber: 4, size: 1).format()
    } else {
        cmds << zwave.configurationV2.configurationSet(configurationValue: [0], parameterNumber: 4, size: 1).format()
    }

    if (bottomled) {
        cmds << zwave.configurationV2.configurationSet(configurationValue: [0], parameterNumber: 3, size: 1).format()
    } else {
        cmds << zwave.configurationV2.configurationSet(configurationValue: [1], parameterNumber: 3, size: 1).format()
    }

    //Enable the following configuration gets to verify configuration in the logs
    //cmds << zwave.configurationV1.configurationGet(parameterNumber: 7).format()
    //cmds << zwave.configurationV1.configurationGet(parameterNumber: 8).format()
    //cmds << zwave.configurationV1.configurationGet(parameterNumber: 9).format()
    //cmds << zwave.configurationV1.configurationGet(parameterNumber: 10).format()

    return cmds
}

def updated() {
    def cmds=configure()
    response(delayBetween(cmds, 1000))
}

private getClassicBtn() {
   ["up_2x":1, "down_2x":2,"up_3x":3,"down_3x":4, "up_hold":5, "down_hold":6, "up":7, "down":8, "up_4x":9, "down_4x":10, "up_5x":11, "down_5x":12]
}