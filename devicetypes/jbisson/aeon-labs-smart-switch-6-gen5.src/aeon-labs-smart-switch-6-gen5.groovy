/**
 *
 *  Aeon Smart Energy Switch 6 (gen5)
 *
 *  Copyright 2019 jbisson
 *  based on James P ('elasticdev'), Mr Lucky, lg kahn code.
 *
 *
 *  The brightness intensity only works in energy mode (does not work in nightlight mode)
 *  You can only change the led color in nightlight mode
 *
 *  The energy cost (current) represent the amount of money you'll pay if the current load stay the same all the time
 *  vs
 *  The energy cost (cumulative) is the amount of money you'll pay based on the amount of energy used between now and
 *  the last time you did a reset. You can clear this counter by hitting the reset icon.
 *  This will take into account the variable aspect of your energy consumption.
 *
 *
 *
 * If you are pairing in secure inclusive mode, make sure you turn it on in the config page.
 *
 *
 *  Revision History
 *  ==============================================
 *  2019-12-17 Version 5.3.2  Added Foxx Project (UK) device as part of the fingerprint
 *  2019-12-17 Version 5.3.1  Fixed IllegalArgumentException bug during setColor if coming from a webCore piston (reported by: MaxVonEvil)
 *  2019-03-25 Version 5.3.0  Fixed watt display and other small display tweaks
 *  2018-02-10 Version 5.2.1  Small Crash protection fix (reported by: dkorunic)
 *  2017-09-04 Version 5.2.0  Removed defaultValues on preference screen as a work-around for a platform bug, crash protection fixes
 *  2017-09-02 Version 5.1.6  More display clean-up and fixed reversed ranges on min watts/percent change in report prefs (Nezmo)
 *  2017-08-15 Version 5.1.5  Cleaned-up display for iOS and other display issues (Nezmo)
 *  2017-02-24 Version 5.1.4  Bug fixed around getDeviceInfo
 *  2017-01-21 Version 5.1.0  Added energy meter cost per hours/week/month/year feature, fixed display issues
 *  2016-11-13 Version 5.0.0  Added Z-Wave secure inclusion support (note that you'll need to manually set it up during configuration)
 *  2016-11-12 Version 4.0.5  Added AT&T rebrand fingerprint + added force refresh report notification update preference
 *  2016-08-31 Version 4.0.4  Fixed fingerprint number.
 *  2016-08-15 Version 4.0.3  Fixed setcolor logic when using the Color Control capability
 *  2016-08-12 Version 4.0.2  Added version in preference setting
 *  2016-08-11 Version 4.0.1  Added switch disabled visual on the main tile, added firmware version
 *  2016-08-11 Version 4.0.0  Added log preference, enable/disable switch preference, added dev documentation, changed fingerprint
 *  2016-08-08 Version 3.0.1  Adapt device handler to support gen5 version 3.1
 *  2016-08-08 Version 3.0
 *  2015-09-01 version 2 - lg kahn
 *
 *
 *  Developer's Notes
 *  Raw Description	0 0 0x1001 0 0 0 11 0x5E 0x25 0x26 0x33 0x70 0x27 0x32 0x81 0x85 0x59 0x72 0x86 0x7A 0x73 0xEF 0x5A 0x82
 *  Z-Wave Supported Command Classes:
 *  Code Name					Version
 *  ==== ======================================	=======
 *  0x5E COMMAND_CLASS_ZWAVE_PLUS_INFO
 *  0x25 COMMAND_CLASS_SWITCH_BINARY          V1                 Implemented
 *  0x26 COMMAND_CLASS_SWITCH_MULTILEVEL      V3 (new in gen5)   Implemented - not used
 *  0x32 COMMAND_CLASS_METER                  V3                 Implemented
 *  0x33 COMMAND_CLASS_COLOR                  V1 (new in gen5)   Not implemented
 *  0x70 COMMAND_CLASS_CONFIGURATION          V1                 Implemented
 *  0x27 COMMAND_CLASS_SWITCH_ALL             V1                 Not implemented
 *  0x81 COMMAND_CLASS_CLOCK                  V1 (new in gen5)   Not implemented
 *  0x85 COMMAND_CLASS_ASSOCIATION            V2                 Not implemented
 *  0x72 COMMAND_CLASS_MANUFACTURER_SPECIFIC  V2                 Implemented
 *  0x59 COMMAND_CLASS_ASSOCIATION_GRP_INFO   V1                 Not implemented
 *  0x86 COMMAND_CLASS_VERSION                V2                 Implemented
 *  0xEF COMMAND_CLASS_MARK                   V1                 Not implemented
 *  0x82 COMMAND_CLASS_HAIL                   V1                 Implemented - not used
 *  0x7A COMMAND_CLASS_FIRMWARE_UPDATE_MD_V2  V2 (new in gen5)   Implemented
 *  0x73 COMMAND_CLASS_POWERLEVEL             V1 (new in gen5)   Not implemented
 *  0x5A COMMAND_CLASS_DEVICE_RESET_LOCALLY   V1 (new in gen5)   Not implemented
 *  0x98 COMMAND_CLASS_SECURITY               V1 (For secure Inclusion) Implemented
 *
 */

def clientVersion() {
    return "5.3.2"
}

metadata {
    definition(name: "Aeon Labs Smart Switch 6 Gen5", namespace: "jbisson", author: "Jonathan Bisson") {
        capability "Switch"
        capability "Polling"
        capability "Power Meter"
        capability "Energy Meter"
        capability "Refresh"
        capability "Switch Level"
        capability "Sensor"
        capability "Actuator"
        capability "Configuration"
        capability "Color Control"

        command "energy"
        command "momentary"
        command "nightLight"

        command "reset"
        command "factoryReset"
        command "setBrightnessLevel"
        command "getDeviceInfo"

        attribute "deviceMode", "String"

        // Base on https://community.smartthings.com/t/new-z-wave-fingerprint-format/48204
        fingerprint mfr: "0086", prod: "0103", model: "0060" // Aeon brand
		fingerprint mfr: "0086", prod: "0003", model: "004B" // Foxx Project (UK)
        fingerprint mfr: "0134", prod: "0259", model: "0096" // AT&T rebrand
        fingerprint type: "1001", cc: "5E,25,26,33,70,27,32,81,85,59,72,86,7A,73", ccOut: "5A,82"
    }

    tiles(scale: 2) {
        multiAttributeTile(name: "mainPanel", type: "lighting", width: 6, height: 4, canChangeIcon: true) {
            tileAttribute("device.switch", key: "PRIMARY_CONTROL") {
                attributeState "on", label: '${name}', action: "switch.off", icon: "st.Appliances.appliances17", backgroundColor: "#00a0dc", nextState: "turningOff"
                attributeState "off", label: '${name}', action: "switch.on", icon: "st.Appliances.appliances17", backgroundColor: "#ffffff", nextState: "turningOn"
                attributeState "turningOn", label: '${name}', action: "switch.off", icon: "st.Appliances.appliances17", backgroundColor: "#00a0dc", nextState: "turningOff"
                attributeState "turningOff", label: '${name}', action: "switch.on", icon: "st.Appliances.appliances17", backgroundColor: "#ffffff", nextState: "turningOn"
            }
            tileAttribute("statusText3", key: "SECONDARY_CONTROL") {
                attributeState "statusText3", label: '${currentValue}'
            }
        }
        standardTile("deviceMode", "deviceMode", canChangeIcon: true, canChangeBackground: false, width: 2, height: 2) {
            state "energy", label: 'energy', action: "momentary", icon: "http://mail.lgk.com/aeonv6orange.png"
            state "momentary", label: 'momentary', action: "nightLight", icon: "http://mail.lgk.com/aeonv6white.png"
            state "nightLight", label: 'NightLight', action: "energy", icon: "http://mail.lgk.com/aeonv6blue.png"
        }

        valueTile("power", "device.power", width: 2, height: 1, decoration: "flat") {
            state "default", label: '${currentValue} W'
        }

        valueTile("energy", "device.energy", width: 2, height: 1, decoration: "flat") {
            state "default", label: '${currentValue} kWh'
        }

        valueTile("amperage", "device.amperage", width: 2, height: 1, decoration: "flat") {
            state "default", label: '${currentValue} A'
        }

        valueTile("voltage", "device.voltage", width: 4, height: 1, decoration: "flat") {
            state "default", label: '${currentValue} v'
        }

        valueTile("currentEnergyCostTxt", "currentEnergyCostTxt", width: 2, height: 1, decoration: "flat") {
            state "default", label: 'Energy Cost (Current):'
        }

        valueTile("currentEnergyCostHour", "currentEnergyCostHour", width: 1, height: 1, decoration: "flat") {
            state "default", label: 'Per\nHour\n$${currentValue}'
        }

        valueTile("currentEnergyCostWeek", "currentEnergyCostWeek", width: 1, height: 1, decoration: "flat") {
            state "default", label: 'Per\nWeek\n$${currentValue}'
        }

        valueTile("currentEnergyCostMonth", "currentEnergyCostMonth", width: 1, height: 1, decoration: "flat") {
            state "default", label: 'Per\nMonth\n$${currentValue}'
        }

        valueTile("currentEnergyCostYear", "currentEnergyCostYear", width: 1, height: 1, decoration: "flat") {
            state "default", label: 'Per\nYear\n$${currentValue}'
        }

        valueTile("cumulativeEnergyCostTxt", "cumulativeEnergyCostTxt", width: 2, height: 1, decoration: "flat") {
            state "default", label: 'Energy Cost (Cumulative)\nSince ${currentValue}:'
        }

        valueTile("cumulativeEnergyCostHour", "cumulativeEnergyCostHour", width: 1, height: 1, decoration: "flat") {
            state "default", label: 'Per\nHour \n$${currentValue}'
        }

        valueTile("cumulativeEnergyCostWeek", "cumulativeEnergyCostWeek", width: 1, height: 1, decoration: "flat") {
            state "default", label: 'Per\nWeek\n$${currentValue}'
        }

        valueTile("cumulativeEnergyCostMonth", "cumulativeEnergyCostMonth", width: 1, height: 1, decoration: "flat") {
            state "default", label: 'Per\nMonth\n$${currentValue}'
        }

        valueTile("cumulativeEnergyCostYear", "cumulativeEnergyCostYear", width: 1, height: 1, decoration: "flat") {
            state "default", label: 'Per\nYear \n$${currentValue}'
        }

        controlTile("levelSliderControl", "device.brightnessLevel", "slider", width: 2, height: 1) {
            state "level", action: "switch level.setLevel"
        }

        valueTile("levelSliderTxt", "device.brightnessLevel", decoration: "flat") {
            state "brightnessLevel", label: '${currentValue} %'
        }

        standardTile("refresh", "device.switch", decoration: "flat", width: 2, height: 2) {
            state "default", label: "", action: "refresh.refresh", icon: "st.secondary.refresh"
        }

        standardTile("reset", "device.energy", decoration: "flat", width: 2, height: 2) {
            state "default", label: 'reset', action: "reset", icon: "st.secondary.refresh-icon"
        }

        controlTile("rgbSelector", "device.color", "color", height: 3, width: 2) {
            state "color", action: "setColor"
        }

        standardTile("configure", "device.power", inactiveLabel: false, decoration: "flat", width: 1, height: 1) {
            state "configure", label: '', action: "configuration.configure", icon: "st.secondary.configure"
        }

        valueTile("deviceInfo", "deviceInfo", decoration: "flat", width: 6, height: 2) {
            state "default", label: '${currentValue}', action: "getDeviceInfo"
        }

        main(["mainPanel", "power", "voltage", "amperage"])
        details(["mainPanel", "deviceMode", "power", "amperage", "voltage",
                 "currentEnergyCostTxt", "currentEnergyCostHour", "currentEnergyCostWeek", "currentEnergyCostMonth", "currentEnergyCostYear",
                 "cumulativeEnergyCostTxt", "cumulativeEnergyCostHour", "cumulativeEnergyCostWeek", "cumulativeEnergyCostMonth", "cumulativeEnergyCostYear",
                 "rgbSelector", "levelSliderControl", "levelSliderTxt", "configure", "refresh", "reset", "deviceInfo"])
    }
}

preferences {
    input title: "", description: "Aeon Smart Switch 6 (gen5) v${clientVersion()}", displayDuringSetup: true, type: "paragraph", element: "paragraph"

    input name: "switchDisabled", type: "bool", title: "Disable switch on/off\n", defaultValue: "false", displayDuringSetup: true, required: true
    input name: "refreshInterval", type: "number", title: "Refresh interval \n\nSet the refresh time interval (seconds) between each report [Default (300)].\n", displayDuringSetup: true, required: true
    input name: "switchAll", type: "enum", title: "Respond to switch all?\n", description: "How does switch respond to the 'Switch All' command", options: ["Disabled", "Off Enabled", "On Enabled", "On and Off Enabled"], defaultValue: "On and Off Enabled", displayDuringSetup: true, required: false
    input name: "forceStateChangeOnReport", type: "bool", title: "Force state change when receiving a report ? If true, you'll always get notification even if report data doesn't change.\n", defaultValue: "false", displayDuringSetup: true, required: true
    input name: "secureInclusionOverride", type: "bool", title: "Is this device in secure inclusive mode?\n", defaultValue: "false", displayDuringSetup: true, required: true

    input name: "onlySendReportIfValueChange", type: "bool", title: "Only send report if value change (either in terms of wattage or a %)\n", defaultValue: "false", displayDuringSetup: true, required: true
    input title: "", description: "The next two parameters are only functional if the 'only send report' is set to true.", type: "paragraph", element: "paragraph", displayDuringSetup: true, required: true

    input name: "minimumChangeWatts", type: "number", title: "Minimum change in wattage for a report to be sent (0 - 60000) [Default (25)].\n", range: "0..60000", displayDuringSetup: true, required: true
    input name: "minimumChangePercent", type: "number", title: "Minimum change in percentage for a report to be sent (0 - 100) [Default (5)]\n", range: "0..100", displayDuringSetup: true, required: true

    input name: "costPerKwh", type: "decimal", title: "Cost per kWh (Used for energy cost /per kWh) [Default (0.12)]\n", displayDuringSetup: true, required: true

    input name: "includeWattInReport", type: "bool", title: "Include energy meter (W) in report?\n", defaultValue: "true", displayDuringSetup: true, required: true
    input name: "includeVoltageInReport", type: "bool", title: "Include voltage (V) in report?\n", defaultValue: "true", displayDuringSetup: true, required: true
    input name: "includeCurrentInReport", type: "bool", title: "Include current (A) in report?\n", defaultValue: "true", displayDuringSetup: true, required: true
    input name: "includeCurrentUsageInReport", type: "bool", title: "Include current usage (kWh) in report?\n", defaultValue: "true", displayDuringSetup: true, required: true

    input title: "", description: "Logging", type: "paragraph", element: "paragraph"
    input name: "isLogLevelTrace", type: "bool", title: "Show trace log level ?\n", defaultValue: "false", displayDuringSetup: true, required: true
    input name: "isLogLevelDebug", type: "bool", title: "Show debug log level ?\n", defaultValue: "true", displayDuringSetup: true, required: true
}

/*******************************************************************************
 * 	Z-WAVE PARSE / EVENTS                                                      *
 ******************************************************************************/

/**
 *  parse - Called when messages from a device are received from the hub
 *
 *  The parse method is responsible for interpreting those messages and returning Event definitions.
 *
 *  String	description		The message from the device
 */
def parse(String description) {
    def result = null
    logTrace "parse: '$description'"

    if (description != "updated") {
        if (description.contains("command: 5E02")) {
            logInfo "Ignoring command 5E02 has it's not supported by the platform."
            return
        }

        def cmd = zwave.parse(description, [0x98: 1, 0x20: 1, 0x26: 3, 0x70: 1, 0x32: 3])
        logTrace "cmd: '$cmd'"

        if (cmd) {
            result = zwaveEvent(cmd)
            //log.debug("'$description' parsed to $result $result?.name")
        } else {
            logError "Couldn't zwave.parse '$description'"
        }
    }

    updateStatus()
    result
}

/**
 *  COMMAND_CLASS_SECURITY (0x98)
 *
 *
 */
def zwaveEvent(physicalgraph.zwave.commands.securityv1.SecurityMessageEncapsulation cmd) {
    def encapsulatedCommand = cmd.encapsulatedCommand([0x20: 1, 0x26: 3, 0x70: 1, 0x32: 3])
    logTrace "secure cmd: '$cmd'"
    state.deviceInfo['secureInclusion'] = true;

    // can specify command class versions here like in zwave.parse
    if (encapsulatedCommand) {
        return zwaveEvent(encapsulatedCommand)
    } else {
        logError "Unable to extract encapsulated cmd from $cmd"
    }
}

/**
 *  COMMAND_CLASS_SECURITY (0x98)
 *
 *
 */
def zwaveEvent(physicalgraph.zwave.commands.securityv1.NetworkKeyVerify cmd) {
    log.debug "NetworkKeyVerify with cmd: $cmd (node is securely included)"

    //after device securely joined the network, call configure() to config device
    state.deviceInfo['secureInclusion'] = true;
    updateDeviceInfo()
}

/**
 *  COMMAND_CLASS_SWITCH_BINARY (0x25)
 *
 *  Short	value	0xFF for on, 0x00 for off
 */
def zwaveEvent(physicalgraph.zwave.commands.switchbinaryv1.SwitchBinarySet cmd) {
    createEvent(name: "switch", value: cmd.switchValue ? "on" : "off")

    //return createEvent(name: "switch", value: cmd.value ? "on" : "off")
}

/**
 *  COMMAND_CLASS_SWITCH_BINARY (0x25)
 *
 *  Short	value	0xFF for on, 0x00 for off
 */
def zwaveEvent(physicalgraph.zwave.commands.switchbinaryv1.SwitchBinaryReport cmd) {
    createEvent(name: "switch", value: cmd.value ? "on" : "off", displayed: false, isStateChange: true)
}

def zwaveEvent(physicalgraph.zwave.commands.sensormultilevelv5.SensorMultilevelReport cmd) {

}

/**
 *  COMMAND_CLASS_BASIC (0x20)
 *  This command is being ignored in secure inclusion mode.
 *
 *  Short	value	0xFF for on, 0x00 for off
 */
def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicReport cmd) {
    return createEvent(name: "switch", value: cmd.value ? "on" : "off", displayed: false)
}

/**
 *  COMMAND_CLASS_BASIC (0x20)
 *
 *  This command is being ignored in secure inclusion mode.
 *  Short	value	0xFF for on, 0x00 for off
 */
def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicSet cmd) {
    return createEvent(name: "switch", value: cmd.value ? "on" : "off")
}

/**
 *  COMMAND_CLASS_SWITCH_MULTILEVEL (0x26)
 *
 *  Short	value
 */
def zwaveEvent(physicalgraph.zwave.commands.switchmultilevelv3.SwitchMultilevelReport cmd) {

}

/**
 *  COMMAND_CLASS_METER (0x32)
 *
 *  Integer	deltaTime		    Time in seconds since last report
 *  Short	meterType		    Unknown = 0, Electric = 1, Gas = 2, Water = 3
 *  List<Short>	meterValue		    Meter value as an array of bytes
 *  Double	scaledMeterValue	    Meter value as a double
 *  List<Short>	previousMeterValue	    Previous meter value as an array of bytes
 *  Double	scaledPreviousMeterValue    Previous meter value as a double
 *  Short	size			    The size of the array for the meterValue and previousMeterValue
 *  Short	scale			    The scale of the values: "kWh"=0, "kVAh"=1, "Watts"=2, "pulses"=3, "Volts"=4, "Amps"=5, "Power Factor"=6, "Unknown"=7
 *  Short	precision		    The decimal precision of the values
 *  Short	rateType		    ???
 *  Boolean	scale2			    ???
 */
def zwaveEvent(physicalgraph.zwave.commands.meterv3.MeterReport cmd) {
    if (cmd.meterType == 1) {
        def eventList = []

        if ("$costPerKwh" == "null") {
            logError "costPerKwh is null, please go through the configuration page first"
            return
        }

        if (cmd.scale == 0) {
            logDebug " got kwh $cmd.scaledMeterValue"

            BigDecimal costDecimal = ( costPerKwh as BigDecimal )
            def batteryRunTimeHours = getBatteryRuntimeInHours()
            
			eventList.push(internalCreateEvent([name: "cumulativeEnergyCostTxt", value: getBatteryRuntime() + "\n" + cmd.scaledMeterValue + " kWh"]));
			eventList.push(internalCreateEvent([name: "cumulativeEnergyCostHour", value: String.format("%5.2f", cmd.scaledMeterValue / batteryRunTimeHours * costDecimal)]));
            eventList.push(internalCreateEvent([name: "cumulativeEnergyCostWeek", value: String.format("%5.2f", cmd.scaledMeterValue / batteryRunTimeHours * costDecimal * 24 * 7)]));
            eventList.push(internalCreateEvent([name: "cumulativeEnergyCostMonth", value: String.format("%5.2f", cmd.scaledMeterValue / batteryRunTimeHours * costDecimal * 24 * 30.42)]));
            eventList.push(internalCreateEvent([name: "cumulativeEnergyCostYear", value: String.format("%5.2f", cmd.scaledMeterValue / batteryRunTimeHours * costDecimal * 24 * 365)]));
        } else if (cmd.scale == 1) {
            logDebug " got kVAh $cmd.scaledMeterValue"
            eventList.push(internalCreateEvent([name: "energy", value: cmd.scaledMeterValue, unit: "kVAh"]));
        } else if (cmd.scale == 2) {
            logDebug " got wattage $cmd.scaledMeterValue"
            eventList.push(internalCreateEvent([name: "power", value: Math.round(cmd.scaledMeterValue), unit: "W"]));
            BigDecimal costDecimal = ( costPerKwh as BigDecimal )
            eventList.push(internalCreateEvent([name: "currentEnergyCostHour", value: String.format("%5.2f", (cmd.scaledMeterValue / 1000) * costDecimal)]));
            eventList.push(internalCreateEvent([name: "currentEnergyCostWeek", value: String.format("%5.2f", (cmd.scaledMeterValue / 1000) * 24 * 7 * costDecimal)]));
            eventList.push(internalCreateEvent([name: "currentEnergyCostMonth", value: String.format("%5.2f", (cmd.scaledMeterValue / 1000) * 24 * 30.42 * costDecimal)]));
            eventList.push(internalCreateEvent([name: "currentEnergyCostYear", value: String.format("%5.2f", (cmd.scaledMeterValue / 1000) * 24 * 365 * costDecimal)]));
        } else if (cmd.scale == 4) { // Volts
            logDebug " got voltage $cmd.scaledMeterValue"
            eventList.push(internalCreateEvent([name: "voltage", value: Math.round(cmd.scaledMeterValue), unit: "V"]));
        } else if (cmd.scale == 5) { //amps scale 5 is amps even though not documented
            logDebug " got amperage = $cmd.scaledMeterValue"
            eventList.push(internalCreateEvent([name: "amperage", value: cmd.scaledMeterValue, unit: "A"]));
        } else {
            eventList.push(internalCreateEvent([name: "electric", value: cmd.scaledMeterValue, unit: ["pulses", "V", "A", "R/Z", ""][cmd.scale - 3]]));
        }

        return eventList

    }
}

/**
 *  COMMAND_CLASS_CONFIGURATION (0x70)
 *
 *  List<Short>	configurationValue
 *  Short	parameterNumber
 *  Short	size
 */
def zwaveEvent(physicalgraph.zwave.commands.configurationv1.ConfigurationReport cmd) {
    logTrace "received ConfigurationReport for " + cmd.parameterNumber + " (hex:" + Integer.toHexString(cmd.parameterNumber) + ") cmd: " + cmd
    switch (cmd.parameterNumber) {
        case 0x51:
            logTrace "received device mode event"
            if (cmd.configurationValue[0] == 0) {
                return createEvent(name: "deviceMode", value: "energy", displayed: true)
            } else if (cmd.configurationValue[0] == 1) {
                return createEvent(name: "deviceMode", value: "momentary", displayed: true)
            } else if (cmd.configurationValue[0] == 2) {
                return createEvent(name: "deviceMode", value: "nightLight", displayed: true)
            }
            break;
        case 0x54:
            logTrace "received brightness level event"
            return createEvent(name: "level", value: cmd.configurationValue[0], displayed: true)
            break;
    }
}

/**
 *  COMMAND_CLASS_HAIL (0x82)
 *
 */
def zwaveEvent(physicalgraph.zwave.commands.hailv1.Hail cmd) {
    logDebug "Switch button was pressed"
    return createEvent(name: "hail", value: "hail", descriptionText: "Switch button was pressed")
}

/**
 *  COMMAND_CLASS_VERSION (0x86)
 *
 *  Short	applicationSubVersion
 *  Short	applicationVersion
 *  Short	zWaveLibraryType
 *  Short	zWaveProtocolSubVersion
 *  Short	zWaveProtocolVersion
 */
def zwaveEvent(physicalgraph.zwave.commands.versionv1.VersionReport cmd) {
    state.deviceInfo['applicationVersion'] = "${cmd.applicationVersion}"
    state.deviceInfo['applicationSubVersion'] = "${cmd.applicationSubVersion}"
    state.deviceInfo['zWaveLibraryType'] = "${cmd.zWaveLibraryType}"
    state.deviceInfo['zWaveProtocolVersion'] = "${cmd.zWaveProtocolVersion}"
    state.deviceInfo['zWaveProtocolSubVersion'] = "${cmd.zWaveProtocolSubVersion}"

    return updateDeviceInfo()
}

/**
 *  COMMAND_CLASS_MANUFACTURER_SPECIFIC (0x72)
 *
 *  Integer	manufacturerId
 *  Integer	productId
 *  Integer	productTypeId
 *
 */
def zwaveEvent(physicalgraph.zwave.commands.manufacturerspecificv2.ManufacturerSpecificReport cmd) {
    state.deviceInfo['manufacturerId'] = "${cmd.manufacturerId}"
    state.deviceInfo['manufacturerName'] = "${cmd.manufacturerName}"
    state.deviceInfo['productId'] = "${cmd.productId}"
    state.deviceInfo['productTypeId'] = "${cmd.productTypeId}"

    return updateDeviceInfo()
}

/**
 *  COMMAND_CLASS_MANUFACTURER_SPECIFIC (0x72)
 *
 * List<Short>	deviceIdData
 * Short	deviceIdDataFormat
 * Short	deviceIdDataLengthIndicator
 * Short	deviceIdType
 *
 */
def zwaveEvent(physicalgraph.zwave.commands.manufacturerspecificv2.DeviceSpecificReport cmd) {
    logTrace "deviceIdData:  	          ${cmd.deviceIdData}"
    logTrace "deviceIdDataFormat:         ${cmd.deviceIdDataFormat}"
    logTrace "deviceIdDataLengthIndicator:${cmd.deviceIdDataLengthIndicator}"
    logTrace "deviceIdType:               ${cmd.deviceIdType}"

    return updateDeviceInfo()
}

/**
 *  COMMAND_CLASS_FIRMWARE_UPDATE_MD_V2 (0x7a)
 *
 * Integer	checksum
 * Integer	firmwareId
 * Integer	manufacturerId
 *
 */
def zwaveEvent(physicalgraph.zwave.commands.firmwareupdatemdv2.FirmwareMdReport cmd) {
    state.deviceInfo['checksum'] = "${cmd.checksum}"
    state.deviceInfo['firmwareId'] = "${cmd.firmwareId}"

    return updateDeviceInfo()
}

/*******************************************************************************
 * 	CAPABILITITES                                                              *
 ******************************************************************************/

/**
 *  configure - Configures the parameters of the device
 *
 *  Required for the "Configuration" capability
 */
def configure() {
    logInfo "configure()"
    if ("$refreshInterval" == "null" || "$minimumChangeWatts" == "null") {
        logError "Some preferences are null, please go through the configuration page first"
        return
    }

    updateDeviceInfo()

    def switchAllMode = physicalgraph.zwave.commands.switchallv1.SwitchAllSet.MODE_INCLUDED_IN_THE_ALL_ON_ALL_OFF_FUNCTIONALITY
    if (switchAll == "Disabled") {
        switchAllMode = physicalgraph.zwave.commands.switchallv1.SwitchAllSet.MODE_EXCLUDED_FROM_THE_ALL_ON_ALL_OFF_FUNCTIONALITY
    } else if (switchAll == "Off Enabled") {
        switchAllMode = physicalgraph.zwave.commands.switchallv1.SwitchAllSet.MODE_EXCLUDED_FROM_THE_ALL_ON_FUNCTIONALITY_BUT_NOT_ALL_OFF
    } else if (switchAll == "On Enabled") {
        switchAllMode = physicalgraph.zwave.commands.switchallv1.SwitchAllSet.MODE_EXCLUDED_FROM_THE_ALL_OFF_FUNCTIONALITY_BUT_NOT_ALL_ON
    }

    logTrace "forceStateChangeOnReport value: " + forceStateChangeOnReport
    logTrace "switchAll value: " + switchAll

    def reportGroup;
    reportGroup = ("$includeVoltageInReport" == "true" ? 1 : 0)
    reportGroup += ("$includeCurrentInReport" == "true" ? 2 : 0)
    reportGroup += ("$includeWattInReport" == "true" ? 4 : 0)
    reportGroup += ("$includeCurrentUsageInReport" == "true" ? 8 : 0)

    logTrace "setting configuration refresh interval: " + new BigInteger("$refreshInterval")

    /***************************************************************
     Device specific configuration parameters
     ----------------------------------------------------------------
     Param   Size    Default Description
     ------- ------- ------- ----------------------------------------
     0x03 (3)    1       0   Current Overload Protection. Load will be closed when the Current overrun (US: 15.5A, other country: 16.2A) and the
     time more than 2 minutes (0=disabled, 1=enabled).
     0x14 (20)   1       0   Configure the output load status after re-power on (0=last status, 1=always on, 2=always off)
     0x21 (33)   4           Set the RGB LED color value for testing. alternate rgb color level ie res,blue,green,red ie 00ffffff
     0x50 (80)   1       0       Enable to send notifications to associated devices in Group 1 when load changes (0=nothing, 1=hail CC, 2=basic CC report)
     0x51 (81)   1       0       mode 0 - energy, 1 - momentary indicator, 2 - night light
     0x53 (83)   3       0      hex value ffffff00 .. only night light mode
     0x54 (84)   1       50       dimmer level 0 -100 (doesn't work in night light mode)
     0x5A (90)   1       1       Enables/disables parameter 0x5A and 0x5B below
     0x5B (91)   2       25      The value here represents minimum change in wattage (in terms of wattage) for a REPORT to be sent (default 50W, size 2 bytes).
     0x5C (92)   1       5      The value here represents minimum change in wattage (in terms of percentage) for a REPORT to be sent (default 10%, size 1 byte).
     0x65 (101)  4       0x00 00 00 04 Which reports need to send in Report group 1
     0x66 (102)  4       0x00 00 00 08 Which reports need to send in Report group 2
     0x67 (103)  4       0       Which reports need to send in Report group 3
     0x6F (111)  4       0x00 00 02 58 The time interval in seconds for sending Report group 1 (Valid values 0x01-0x7FFFFFFF).
     0x70 (112)  4       0x00 00 02 58 The time interval in seconds for sending Report group 2 (Valid values 0x01-0x7FFFFFFF).
     0x71 (113)  4       0x00 00 02 58 The time interval in seconds for sending Report group 3 (Valid values 0x01-0x7FFFFFFF).
     0xC8 (200)  1       0  Partner ID
     0xFC (252)  1       0  Enable/disable Configuration Locked (0 =disable, 1 =enable).
     0xFE (254)  2       0  Device Tag.
     0xFF (255)  1       N/A     Reset to factory default setting


     Configuration Values for parameters 0x65-0x67:
     BYTE  | 7  6  5  4  3  2  1  0
     ===============================
     MSB 0 | 0  0  0  0  0  0  0  0
     Val 1 | 0  0  0  0  0  0  0  0
     VAL 2 | 0  0  0  0  0  0  0  0
     LSB 3 | 0  0  0  0  A  B  C  0

     Bit A - Send Meter REPORT (for kWh) at the group time interval
     Bit B - Send Meter REPORT (for watt) at the group time interval
     Bit C - Automatically send(1) or don't send(0) Multilevel Sensor Report Command
     ***************************************************************/

    delayBetween([
            formatCommand(zwave.switchAllV1.switchAllSet(mode: switchAllMode)),
            formatCommand(zwave.configurationV1.configurationSet(parameterNumber: 0x50, size: 1, scaledConfigurationValue: 0)),    //Enable to send notifications to associated devices when load changes (0=nothing, 1=hail CC, 2=basic CC report)
            formatCommand(zwave.configurationV1.configurationSet(parameterNumber: 0x5A, size: 1, scaledConfigurationValue: ("$onlySendReportIfValueChange" == "true" ? 1 : 0))),    //Enables parameter 0x5B and 0x5C (0=disabled, 1=enabled)
            formatCommand(zwave.configurationV1.configurationSet(parameterNumber: 0x5B, size: 2, scaledConfigurationValue: new BigInteger("$minimumChangeWatts"))),    //Minimum change in wattage for a REPORT to be sent (Valid values 0 - 60000)
            formatCommand(zwave.configurationV1.configurationSet(parameterNumber: 0x5C, size: 1, scaledConfigurationValue: new BigInteger("$minimumChangePercent"))),    //Minimum change in percentage for a REPORT to be sent (Valid values 0 - 100)

            formatCommand(zwave.configurationV1.configurationSet(parameterNumber: 0x65, size: 4, scaledConfigurationValue: reportGroup)),    //Which reports need to send in Report group 1
            formatCommand(zwave.configurationV1.configurationSet(parameterNumber: 0x66, size: 4, scaledConfigurationValue: 0)),    //Which reports need to send in Report group 2
            formatCommand(zwave.configurationV1.configurationSet(parameterNumber: 0x67, size: 4, scaledConfigurationValue: 0)),    //Which reports need to send in Report group 3

            formatCommand(zwave.configurationV1.configurationSet(parameterNumber: 0x6F, size: 4, scaledConfigurationValue: new BigInteger("$refreshInterval"))),    // change reporting time
            formatCommand(zwave.configurationV1.configurationSet(parameterNumber: 0x70, size: 4, scaledConfigurationValue: new BigInteger(0xFFFFF))),
            formatCommand(zwave.configurationV1.configurationSet(parameterNumber: 0x71, size: 4, scaledConfigurationValue: new BigInteger(0xFFFFF))),

            formatCommand(zwave.configurationV1.configurationSet(parameterNumber: 0x3, size: 1, scaledConfigurationValue: 0)),      // Current Overload Protection.
    ], 200)
}

/**
 *  on - Turns on the switch
 *
 *  Required for the "Switch" capability
 */
def on() {
    if (switchDisabled) {
        logDebug "switch disabled, doing nothing"
        delayBetween([
                formatCommand(zwave.switchBinaryV1.switchBinaryGet())
        ], 200)
    } else {
        logDebug "switching it on"
        delayBetween([
                formatCommand(zwave.switchBinaryV1.switchBinarySet(switchValue: 0xFF)),
                formatCommand(zwave.switchBinaryV1.switchBinaryGet())
        ], 200)
    }
}

/**
 *  off - Turns off the switch
 *
 *  Required for the "Switch" capability
 */
def off() {
    if (switchDisabled) {
        logDebug "switch disabled, doing nothing"
        delayBetween([
                formatCommand(zwave.switchBinaryV1.switchBinaryGet())
        ], 200)
    } else {
        logDebug "switching it off"
        delayBetween([
                formatCommand(zwave.switchBinaryV1.switchBinarySet(switchValue: 0x00)),
                formatCommand(zwave.switchBinaryV1.switchBinaryGet())
        ], 200)
    }
}

/**
 *  poll - Polls the device
 *
 *  Required for the "Polling" capability
 */
def poll() {
    logTrace "poll()"

    delayBetween([
            formatCommand(zwave.switchBinaryV1.switchBinaryGet()),
            formatCommand(zwave.meterV3.meterGet(scale: 0)), // energy kWh
            formatCommand(zwave.meterV3.meterGet(scale: 1)), // energy kVAh
            formatCommand(zwave.meterV3.meterGet(scale: 2)), // watts
            formatCommand(zwave.meterV3.meterGet(scale: 4)), // volts
            formatCommand(zwave.meterV3.meterGet(scale: 5)), // amps
    ], 200)
}

/**
 *  refresh - Refreshed values from the device
 *
 *  Required for the "Refresh" capability
 */
def refresh() {
    logInfo "refresh()"
    updateDeviceInfo()

    sendEvent(name: "power", value: "0", displayed: true, unit: "W")
    sendEvent(name: "energy", value: "0", displayed: true, unit: "kWh")
    sendEvent(name: "amperage", value: "0", displayed: true, unit: "A")
    sendEvent(name: "voltage", value: "0", displayed: true, unit: "V")

    sendEvent(name: "currentEnergyCostHour", value: "0", displayed: true)
    sendEvent(name: "currentEnergyCostWeek", value: "0", displayed: true)
    sendEvent(name: "currentEnergyCostMonth", value: "0", displayed: true)
    sendEvent(name: "currentEnergyCostYear", value: "0", displayed: true)

    sendEvent(name: "cumulativeEnergyCostHour", value: "0", displayed: true)
    sendEvent(name: "cumulativeEnergyCostWeek", value: "0", displayed: true)
    sendEvent(name: "cumulativeEnergyCostMonth", value: "0", displayed: true)
    sendEvent(name: "cumulativeEnergyCostYear", value: "0", displayed: true)

    delayBetween([
            formatCommand(zwave.switchMultilevelV1.switchMultilevelGet()),
            formatCommand(zwave.meterV3.meterGet(scale: 0)), // energy kWh
            formatCommand(zwave.meterV3.meterGet(scale: 1)), // energy kVAh
            formatCommand(zwave.meterV3.meterGet(scale: 2)), // watts
            formatCommand(zwave.meterV3.meterGet(scale: 4)), // volts
            formatCommand(zwave.meterV3.meterGet(scale: 5)), // amps
            formatCommand(zwave.configurationV1.configurationGet(parameterNumber: 0x51)), // device state
            formatCommand(zwave.configurationV1.configurationGet(parameterNumber: 0x53)), // night light RGB value
            formatCommand(zwave.configurationV1.configurationGet(parameterNumber: 0x54)), // led brightness
    ], 200)
}

/**
 *  reset - Resets the devices energy usage meter and attempt to reset device
 *
 *  Required for the "Switch Level" capability
 */
def setLevel(level) {
    setBrightnessLevel(level)
}

/**
 *  Sets the color to the passed in maps values
 *
 *  Required for the "Color Control" capability
 */
def setColor(colormap) {
    logDebug " in setColor"

    if (colormap.hex == null && colormap.hue) {
        def hexColor = colorUtil.hslToHex(colormap.hue, colormap.saturation)
        logDebug " in setColor colormap = $hexColor"

        sendEvent(name: "color", value: hexColor)
        formatCommand(zwave.configurationV1.configurationSet(parameterNumber: 0x53, size: 3, configurationValue: colorUtil.hexToRgb(hexColor)))
    } else {
        logDebug " in setColor: hex = ${colormap.hex}"
        sendEvent(name: "color", value: colormap.hex)
		def hexColorList = colorUtil.hexToRgb(colormap.hex)
        formatCommand(zwave.configurationV1.configurationSet(parameterNumber: 0x53, size: 3, configurationValue: [hexColorList[0], hexColorList[1], hexColorList[2]]))
    }
}

/*******************************************************************************
 * 	Methods                                                                    *
 ******************************************************************************/

/**
 *  installed - Called when the device handling is being installed
 */
def installed() {
    logInfo "installed() called"

    if (state.deviceInfo == null) {
        state.deviceInfo = [:]
        state.deviceInfo['secureInclusion'] = false
    }

    // Call a reset upon install to clear all values.
    reset();

    updateDeviceInfo();
}

/**
 *  updated - Called when the preferences of the device type are changed
 */
def updated() {
    logInfo "updated()"

    updateStatus()
    //updatePowerStatus(0)
    response(configure())
}

/**
 *  reset - Resets the devices energy usage meter and attempt to reset device
 *
 *  Defined by the custom command "reset"
 */
def reset() {
    logInfo "reset()"
    state.energyMeterRuntimeStart = now()

    delayBetween([
            formatCommand(zwave.meterV3.meterReset()),
            formatCommand(zwave.meterV3.meterGet(scale: 0)), // energy kWh
            formatCommand(zwave.meterV3.meterGet(scale: 1)), // energy kVAh
            formatCommand(zwave.meterV3.meterGet(scale: 2)), // watts
            formatCommand(zwave.meterV3.meterGet(scale: 4)), // volts
            formatCommand(zwave.meterV3.meterGet(scale: 5)), // amps
    ], 200)
}

def factoryReset() {
    logDebug "factoryReset()"

    formatCommand(zwave.configurationV1.configurationSet(parameterNumber: 0xFF, size: 4, scaledConfigurationValue: 1))
    //factory reset
    configure()
}

def getDeviceInfo() {
    logDebug "getDeviceInfo()"

    delayBetween([
            formatCommand(zwave.versionV1.versionGet()),
            formatCommand(zwave.firmwareUpdateMdV2.firmwareMdGet()),
            //zwave.manufacturerSpecificV2.deviceSpecificGet().format(),
            formatCommand(zwave.manufacturerSpecificV2.manufacturerSpecificGet())
    ], 200)
}

private updateStatus() {
    def sinceTime = ''
    if (state.energyMeterRuntimeStart != null) {
        sinceTime = "${getBatteryRuntime()}"
    } else {
        sinceTime = now()
    }

    sendEvent(name: "statusText3", value: "Energy meter since: $sinceTime", displayed: false)
}

private updateDeviceInfo() {
    logInfo "updateDeviceInfo()"
	
    if (state.deviceInfo == null) {
        state.deviceInfo = [:]
	}

    def buffer = "Get Device Info";
    def newBuffer = null;

    def switchStatus = "SWITCH ENABLED\n"
    if (switchDisabled) {
        switchStatus = "SWITCH DISABLED\n"
    }

    if (state.deviceInfo['applicationVersion'] == null ||
        state.deviceInfo['manufacturerName'] == null) {
        getDeviceInfo()
    } else {
        newBuffer = "${switchStatus}"
    }

    if (state.deviceInfo['applicationVersion'] != null) {
        if (newBuffer == null) {
            newBuffer = "${switchStatus}"
        }

        newBuffer += "app Version: ${state.deviceInfo['applicationVersion']} Sub Version: ${state.deviceInfo['applicationSubVersion']}\n";
        newBuffer += "zWaveLibrary Type: ${state.deviceInfo['zWaveLibraryType']}\n";
        newBuffer += "zWaveProtocol Version: ${state.deviceInfo['zWaveProtocolVersion']} Sub Version: ${state.deviceInfo['zWaveProtocolSubVersion']}\n";
        newBuffer += "secure inclusion: ${state.deviceInfo['secureInclusion'] || secureInclusionOverride}\n";
    }

    if (state.deviceInfo['manufacturerName'] != null) {
        if (newBuffer == null) {
            newBuffer = "${switchStatus}"
        }

        newBuffer += "manufacturer Name: ${state.deviceInfo['manufacturerName']}\n";
        newBuffer += "manufacturer Id: ${state.deviceInfo['manufacturerId']}\n";
        newBuffer += "product Id: ${state.deviceInfo['productId']} Type Id: ${state.deviceInfo['productTypeId']}\n";
        newBuffer += "firmwareId: ${state.deviceInfo['firmwareId']} checksum: ${state.deviceInfo['checksum']}\n";
    }

    if (newBuffer == null) {
        newBuffer = buffer
    }

    return sendEvent(name: "deviceInfo", value: "$newBuffer", displayed: false)
}

private getBatteryRuntime() {
    def currentmillis = now() - state.energyMeterRuntimeStart
    def days = 0
    def hours = 0
    def mins = 0
    def secs = 0
    secs = (currentmillis / 1000).toInteger()
    mins = (secs / 60).toInteger()
    hours = (mins / 60).toInteger()
    days = (hours / 24).toInteger()
    secs = (secs - (mins * 60)).toString().padLeft(2, '0')
    mins = (mins - (hours * 60)).toString().padLeft(2, '0')
    hours = (hours - (days * 24)).toString().padLeft(2, '0')

    if (days > 0) {
        return "$days days and $hours:$mins:$secs"
    } else {
        return "$hours:$mins:$secs"
    }
}

private getBatteryRuntimeInHours() {
    def currentmillis = now() - state.energyMeterRuntimeStart
    def days = 0
    def hours = 0
    def mins = 0
    def secs = 0
    secs = (currentmillis / 1000)
    mins = (secs / 60)
    hours = (mins / 60)
    return hours
}

void logInfo(str) {
    log.info str
}

void logWarn(str) {
    log.warn str
}

void logError(str) {
    log.error str
}

void logDebug(str) {
    if (isLogLevelDebug) {
        log.debug str
    }
}

void logTrace(str) {
    if (isLogLevelTrace) {
        log.trace str
    }
}

def nightLight() {
    logDebug "in set nightlight mode"
    sendEvent(name: "deviceMode", value: "nightLight", displayed: true)
    setDeviceMode(2)
}

def energy() {
    logDebug "in set energy mode"
    sendEvent(name: "deviceMode", value: "energy", displayed: true)
    setDeviceMode(0)
}

def momentary() {
    logDebug "in momentary mode"
    sendEvent(name: "deviceMode", value: "momentary", displayed: true)
    setDeviceMode(1)
}

def setDeviceMode(mode) {
    logTrace "set current mode to '$mode'"
    formatCommand(zwave.configurationV1.configurationSet(parameterNumber: 0x51, size: 1, scaledConfigurationValue: mode))
}

def setBrightnessLevel(newLevel) {
    logDebug "in set setlevel newlevel = '$newLevel'"
    sendEvent(name: "brightnessLevel", value: newLevel.toInteger(), displayed: true)

    // There seems to have an error in the documentation where this config should be a size = 1
    formatCommand(zwave.configurationV1.configurationSet(parameterNumber: 0x54, size: 3, configurationValue: [newLevel, newLevel, newLevel]))
}

def formatCommand(physicalgraph.zwave.Command cmd) {
    if (isSecured()) {
        logTrace "Formatting secured command: ${cmd}"
        zwave.securityV1.securityMessageEncapsulation().encapsulate(cmd).format()
    } else {
        logTrace "Formatting unsecured command: ${cmd}"
        cmd.format()
    }
}

def isSecured() {
    (state.deviceInfo && state.deviceInfo['secureInclusion']) || secureInclusionOverride
}

def internalCreateEvent(event) {
    if (forceStateChangeOnReport) {
        event.isStateChange = true
    }

    return createEvent(event)
}