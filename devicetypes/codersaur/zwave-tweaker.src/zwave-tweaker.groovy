/*****************************************************************************************************************
 *  Copyright: David Lomas (codersaur)
 *
 *  Name: Z-Wave Tweaker
 *
 *  Author: David Lomas (codersaur)
 *
 *  Date: 2017-03-16
 *
 *  Version: 0.08
 *
 *  Source: https://github.com/codersaur/SmartThings/tree/master/devices/zwave-tweaker
 *
 *  Author: David Lomas (codersaur)
 *
 *  Description: A SmartThings device handler to assist with tweaking Z-Wave devices.
 *
 *  For full information, including installation instructions, examples, and version history, see:
 *   https://github.com/codersaur/SmartThings/tree/master/devices/zwave-tweaker
 *
 *  License:
 *   Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *   on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *   for the specific language governing permissions and limitations under the License.
 *
 *****************************************************************************************************************/
metadata {
    definition (name: "Z-Wave Tweaker", namespace: "codersaur", author: "David Lomas") {
        capability "Actuator"
        capability "Sensor"

        // Custom Attributes:
        attribute "syncPending", "number" // Number of config items that need to be synced with the physical device.

        // Custom Commands:
        command "scanGeneral"
        command "scanAssocGroups"
        command "scanEndpoints"
        command "scanParams"
        command "scanActuator"
        command "scanSensor"
        command "printGeneral"
        command "printAssocGroups"
        command "printCommands"
        command "printEndpoints"
        command "printParams"
        command "printActuator"
        command "printSensor"
        command "sync"
        command "cleanUp"

    }

    tiles(scale: 2) {

        standardTile("main", "main", decoration: "flat", width: 2, height: 2) {
            state "default", label:'Tweaker', action:"", backgroundColor:"#e86d13", icon:"st.secondary.tools"
        }
        standardTile("print", "print", decoration: "flat", width: 2, height: 2) {
            state "default", label:'print', action:"print"
        }
        standardTile("scanGeneral", "scanGeneral", decoration: "flat", width: 2, height: 2) {
            state "default", label:'Scan General', action:"scanGeneral", icon: "https://raw.githubusercontent.com/codersaur/SmartThings/master/icons/tile_2x2_wifi.png"
        }
        standardTile("scanAssocGroups", "scanAssocGroups", decoration: "flat", width: 2, height: 2) {
            state "default", label:'Scan Assoc Grps', action:"scanAssocGroups", icon: "https://raw.githubusercontent.com/codersaur/SmartThings/master/icons/tile_2x2_wifi.png"
        }
        standardTile("scanEndpoints", "scanEndpoints", decoration: "flat", width: 2, height: 2) {
            state "default", label:'Scan Endpoints', action:"scanEndpoints", icon: "https://raw.githubusercontent.com/codersaur/SmartThings/master/icons/tile_2x2_wifi.png"
        }
        standardTile("scanParams", "scanParams", decoration: "flat", width: 2, height: 2) {
            state "default", label:'Scan Params', action:"scanParams", icon: "https://raw.githubusercontent.com/codersaur/SmartThings/master/icons/tile_2x2_wifi.png"
        }
        standardTile("scanActuator", "scanActuator", decoration: "flat", width: 2, height: 2) {
            state "default", label:'Scan Actuator', action:"scanActuator", icon: "https://raw.githubusercontent.com/codersaur/SmartThings/master/icons/tile_2x2_wifi.png"
        }
        standardTile("scanSensor", "scanSensor", decoration: "flat", width: 2, height: 2) {
            state "default", label:'Scan Sensor', action:"scanSensor", icon: "https://raw.githubusercontent.com/codersaur/SmartThings/master/icons/tile_2x2_wifi.png"
        }
        standardTile("printGeneral", "printGeneral", decoration: "flat", width: 2, height: 2) {
            state "default", label:'Print General', action:"printGeneral", icon: "https://raw.githubusercontent.com/codersaur/SmartThings/master/icons/tile_2x2_document.png"
        }
        standardTile("printAssocGroups", "printAssocGroups", decoration: "flat", width: 2, height: 2) {
            state "default", label:'Print Assoc Grps', action:"printAssocGroups", icon: "https://raw.githubusercontent.com/codersaur/SmartThings/master/icons/tile_2x2_document.png"
        }
        standardTile("printEndpoints", "printEndpoints", decoration: "flat", width: 2, height: 2) {
            state "default", label:'Print Endpoints', action:"printEndpoints", icon: "https://raw.githubusercontent.com/codersaur/SmartThings/master/icons/tile_2x2_document.png"
        }
        standardTile("printParams", "printParams", decoration: "flat", width: 2, height: 2) {
            state "default", label:'Print Params', action:"printParams", icon: "https://raw.githubusercontent.com/codersaur/SmartThings/master/icons/tile_2x2_document.png"
        }
        standardTile("printActuator", "printActuator", decoration: "flat", width: 2, height: 2) {
            state "default", label:'Print Actuator', action:"printActuator", icon: "https://raw.githubusercontent.com/codersaur/SmartThings/master/icons/tile_2x2_document.png"
        }
        standardTile("printSensor", "printSensor", decoration: "flat", width: 2, height: 2) {
            state "default", label:'Print Sensor', action:"printSensor", icon: "https://raw.githubusercontent.com/codersaur/SmartThings/master/icons/tile_2x2_document.png"
        }
        standardTile("printCommands", "printCommands", decoration: "flat", width: 2, height: 2) {
            state "default", label:'Print Commands', action:"printCommands", icon: "https://raw.githubusercontent.com/codersaur/SmartThings/master/icons/tile_2x2_document.png"
        }
        standardTile("syncPending", "syncPending", decoration: "flat", width: 2, height: 2) {
            state "default", label:'Sync Pending', backgroundColor:"#FF6600", action:"sync", icon: "https://raw.githubusercontent.com/codersaur/SmartThings/master/icons/tile_2x2_cycle.png"
            state "0", label:'Synced', backgroundColor:"#79b821", icon: "https://raw.githubusercontent.com/codersaur/SmartThings/master/icons/tile_2x2_tick.png"
        }
        standardTile("cleanUp", "cleanUp", decoration: "flat", width: 2, height: 2) {
            state "default", label:'Clean Up', action:"cleanUp", icon: "https://raw.githubusercontent.com/codersaur/SmartThings/master/icons/tile_2x2_cycle.png"
        }

        main(["main"])
        details([
            "scanGeneral",    "scanAssocGroups",   "scanEndpoints",
            "scanParams",     "scanActuator",      "scanSensor",
            "printGeneral",   "printAssocGroups",  "printEndpoints",
            "printParams",    "printActuator",     "printSensor",
            "printCommands",  "syncPending",       "cleanUp"
        ])
    }

    preferences {

        section { // GENERAL:
            input (
                type: "paragraph",
                element: "paragraph",
                title: "GENERAL:",
                description: "General device handler settings."
            )

            input (
                name: "zwtLoggingLevelIDE",
                title: "IDE Live Logging Level:\nMessages with this level and higher will be logged to the IDE.",
                type: "enum",
                options: [
                    "3" : "Info",
                    "4" : "Debug",
                    "5" : "Trace"
                ],
                defaultValue: "3",
                required: false
            )

        }

        section { // SCAN RANGES:
            input (
                type: "paragraph",
                element: "paragraph",
                title: "SCAN RANGES:",
                description: "Configure the scanning range for association groups, endpoints, and parameters."
            )

            input (
                name: "zwtAssocGroupsScanStart",
                title: "Association Groups Scan Range (Start):",
                type: "number",
                range: "0..255",
                required: false
            )

            input (
                name: "zwtAssocGroupsScanStop",
                title: "Association Groups Scan Range (Stop):",
                type: "number",
                range: "0..255",
                required: false
            )

            input (
                name: "zwtEndpointsScanStart",
                title: "Endpoints Scan Range (Start):",
                type: "number",
                range: "0..127",
                required: false
            )

            input (
                name: "zwtEndpointsScanStop",
                title: "Endpoints Scan Range (Stop):",
                type: "number",
                range: "0..127",
                required: false
            )

            input (
                name: "zwtParamsScanStart",
                title: "Parameters Scan Range (Start):",
                type: "number",
                range: "0..65535",
                required: false
            )

            input (
                name: "zwtParamsScanStop",
                title: "Parameters Scan Range (Stop):",
                type: "number",
                range: "0..65535",
                required: false
            )
        }

        section { // CONFIGURE AN ASSOCIATION GROUP:
            input (
                type: "paragraph",
                element: "paragraph",
                title: "CONFIGURE ASSOCIATION GROUP:",
                description: "Use these settings to configure the members of an association group."
            )

            input (
                name: "zwtAssocGroupId",
                title: "Association Group ID:",
                type: "number",
                range: "0..255",
                required: false
            )

            input (
                name: "zwtAssocGroupMembers",
                title: "Association Group Members:\n" +
                       "Enter a comma-delimited list of destinations (node IDs and/or endpoint IDs). " +
                       "All IDs must be in hexadecimal format. E.g.:\n" +
                       "Node destinations: '11, 0F'\n" +
                       "Endpoint destinations: '1C:1, 1C:2'",
                type: "text",
                required: false
            )
            
            input (
                name: "zwtAssocGroupCc",
                title: "Command Class:",
                type: "enum",
                options: [
                    "0" : "Auto-detect",
                    "1" : "(Single-channel) Association (0x85)",
                    "2" : "Multi-Channel Association (0x8E)"
                ],
                required: false
            )

        }

        section { // CONFIGURE A PARAMETER:
            input (
                type: "paragraph",
                element: "paragraph",
                title: "CONFIGURE A PARAMETER:",
                description: "Use these settings to configure the value of a device parameter."
            )

            input (
                name: "zwtParamId",
                title: "Parameter ID:",
                type: "number",
                range: "0..65536",
                required: false
            )

            input (
                name: "zwtParamValue",
                title: "Parameter Value:",
                type: "number",
                range: "-2147483648..2147483647",
                required: false
            )

        }

        section { // OTHER:
            input type: "paragraph",
                element: "paragraph",
                title: " CONFIGURE OTHER SETTINGS:",
                description: "Other miscellaneous settings."

            input (
                name: "zwtProtectLocal",
                title: "Local Protection: Prevent unintentional control (e.g. by a child) by disabling the physical switches:",
                type: "enum",
                options: [
                    "0" : "Unprotected",
                    "1" : "Protection by sequence",
                    "2" : "No operation possible"
                ],
                required: false
            )

            input (
                name: "zwtProtectRF",
                title: "RF Protection: Applies to Z-Wave commands sent from hub or other devices:",
                type: "enum",
                options: [
                    "0" : "Unprotected",
                    "1" : "No RF control",
                    "2" : "No RF response"
                ],
                required: false
            )

            input (
                name: "zwtSwitchAllMode",
                title: "ALL ON/ALL OFF Function:\nResponse to SWITCH_ALL_SET commands.",
                type: "enum",
                options: [
                    "0" : "0: All ON not active, All OFF not active",
                    "1" : "1: All ON not active, All OFF active",
                    "2" : "2: All ON active, All OFF not active",
                    "255" : "255: All ON active, All OFF active"],
                required: false
            )
        }

        section {

            input (
                type: "paragraph",
                element: "paragraph",
                title: "ORIGINAL SETTINGS:",
                description: "Do not delete any setting values below this line! They belong to the original device " +
                "handler and will be reinstated when the original device handler is restored."
            )




        }

    }

}

/*****************************************************************************************************************
 *  SmartThings System Commands:
 *****************************************************************************************************************/

/**
 *  updated()
 *
 *  Runs when the user hits "Done" from Settings page.
 *
 *  Action: Trigger sync of selected parameter and/or association group.
 *
 *  Note: Weirdly, update() seems to be called twice. So execution is aborted if there was a previous execution
 *  within two seconds. See: https://community.smartthings.com/t/updated-being-called-twice/62912
 **/
def updated() {
    logger("updated()","trace")

    def cmds = []

    if (!state.updatedLastRanAt || now() >= state.updatedLastRanAt + 2000) {
        state.updatedLastRanAt = now()

        // Logging Level:
        state.loggingLevelIDE = (settings.zwtLoggingLevelIDE) ? settings.zwtLoggingLevelIDE.toInteger() : 3

        // Run initialisation checks
        if (!state.zwtInitialised) { initialise() }

        // Check if an association group needs to be synced:
        if (settings.zwtAssocGroupId != null) {
            // Get MaxSupportedNodes if already in metadata store:
            def assocGroupMd = state.zwtAssocGroupsMd.find( { it.id == settings.zwtAssocGroupId.toInteger() } )
            def maxNodes = (assocGroupMd?.maxNodesSupported) ? assocGroupMd?.maxNodesSupported : 256
            state.zwtAssocGroupTarget = [
                id: settings.zwtAssocGroupId.toInteger(),
                nodes: parseAssocGroupInput(settings.zwtAssocGroupMembers,maxNodes),
                commandClass: (settings.zwtAssocGroupCc) ? settings.zwtAssocGroupCc.toInteger() : 0
            ]
        }
        else {
            state.zwtAssocGroupTarget = null
        }

        // Check if a parameter needs to be synced:
        if ((settings.zwtParamId != null) & (settings.zwtParamValue != null)) {
            state.zwtParamTarget = [
                id: settings.zwtParamId.toInteger(),
                scaledConfigurationValue: settings.zwtParamValue.toInteger()
            ]
        }
        else {
            state.zwtParamTarget = null
        }

        sync()

        // Other commands...?

        return sendCommands(cmds)
    }
    else {
        logger("updated(): Ran within last 2 seconds so aborting.","debug")
    }
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
 *  Note: There is no longer any need to check if description == "updated".
 *
 *  Parameters:
 *   String      description        The raw message from the device.
 **/
def parse(description) {
    logger("parse(): Parsing raw message: ${description}","trace")

    def result = []
    if (!state.zwtCommandsMd) state.zwtCommandsMd = []

    def cmd = zwave.parse(description, getCommandClassVersions())
    if (cmd) {
        result += zwaveEvent(cmd)
    }
    else {
        logger("parse(): Could not parse raw message: ${description}","error")

        // Extract details from raw description to add to command meta-data cache:
        if (description.contains("command: ")) {
            def index = description.indexOf("command: ") + 9
            cmd = [
                commandClassId: Integer.parseInt(description.substring(index, index +2),16), // Parse as hex.
                commandId: Integer.parseInt(description.substring(index +2, index +4),16) // Parse as hex.
            ]
        }
    }

    // Update commands meta-data cache:
    cacheCommandMd(cmd, description)

    return result
}

/*****************************************************************************************************************
 *  Z-wave Event Handlers.
 *****************************************************************************************************************/

/**
 *  zwaveEvent( COMMAND_CLASS_BASIC (0x20) : BASIC_REPORT (0x03) )
 *
 *  The Basic Report command is used to advertise the status of the primary functionality of the device.
 *
 *  Action: Log info message.
 *
 *  cmd attributes:
 *    Short    value
 *      0x00       = Off
 *      0x01..0x63 = 0..100%
 *      0xFE       = Unknown
 *      0xFF       = On
 *
 *  Example:
 **/
def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicReport cmd) {
    logger("zwaveEvent(): Basic Report received: ${cmd}","info")
}

/**
 *  zwaveEvent( COMMAND_CLASS_SWITCH_BINARY (0x25) : SWITCH_BINARY_REPORT (0x03) )
 *
 *  The Binary Switch Report command  is used to advertise the status of a device with On/Off or Enable/Disable
 *  capability.
 *
 *  Action: Log info message.
 *
 *  cmd attributes:
 *    Short  value  0xFF for on, 0x00 for off
 **/
def zwaveEvent(physicalgraph.zwave.commands.switchbinaryv1.SwitchBinaryReport cmd) {
    logger("zwaveEvent(): Switch Binary Report received: ${cmd}","info")
}

/**
 *  zwaveEvent( COMMAND_CLASS_SWITCH_ALL (0x27) : SWITCH_ALL_REPORT (0x03) )
 *
 *  The All Switch Report Command is used to report if the device is included or excluded from the all on/all off
 *  functionality.
 *
 *  Action: Log an info message.
 *
 *  cmd attributes:
 *    Short    mode
 *      0   = MODE_EXCLUDED_FROM_THE_ALL_ON_ALL_OFF_FUNCTIONALITY
 *      1   = MODE_EXCLUDED_FROM_THE_ALL_ON_FUNCTIONALITY_BUT_NOT_ALL_OFF
 *      2   = MODE_EXCLUDED_FROM_THE_ALL_OFF_FUNCTIONALITY_BUT_NOT_ALL_ON
 *      255 = MODE_INCLUDED_IN_THE_ALL_ON_ALL_OFF_FUNCTIONALITY
 **/
def zwaveEvent(physicalgraph.zwave.commands.switchallv1.SwitchAllReport cmd) {
    logger("zwaveEvent(): Switch All Report received: ${cmd}","trace")

    def msg = ""
    switch (cmd.mode) {
            case 0:
                msg = "Device is excluded from the all on/all off functionality."
                break

            case 1:
                msg = "Device is excluded from the all on functionality but not all off."
                break

            case 2:
                msg = "Device is excluded from the all off functionality but not all on."
                break

            default:
                msg = "Device is included in the all on/all off functionality."
                break
    }
    logger("Switch All Mode: ${cmd.mode}: ${msg}","info")

    // Cache in GeneralMd:
    state.zwtGeneralMd.switchAllModeId = cmd.mode
    state.zwtGeneralMd.switchAllModeDesc = msg

    updateSyncPending()
}

/**
 *  zwaveEvent( COMMAND_CLASS_SENSOR_MULTILEVEL_V5 (0x31) : SENSOR_MULTILEVEL_REPORT_V5 (0x05) )
 *
 *  The Multilevel Sensor Report Command is used by a multilevel sensor to advertise a sensor reading.
 *
 *  Action: Cache SensorType and log an info message.
 *
 *  Note: SmartThings does not yet have capabilities corresponding to all possible sensor types, therefore
 *  some of the event types raised below are non-standard.
 *
 *  cmd attributes:
 *    Short         precision           Indicates the number of decimals.
 *                                      E.g. The decimal value 1025 with precision 2 is therefore equal to 10.25.
 *    Short         scale               Indicates what unit the sensor uses.
 *    BigDecimal    scaledSensorValue   Sensor value as a double.
 *    Short         sensorType          Sensor Type (8 bits).
 *    List<Short>   sensorValue         Sensor value as an array of bytes.
 *    Short         size                Indicates the number of bytes used for the sensor value.
 **/
def zwaveEvent(physicalgraph.zwave.commands.sensormultilevelv5.SensorMultilevelReport cmd) {
    logger("zwaveEvent(): SensorMultilevelReport received: ${cmd}","trace")

    def map = [ sensorType: cmd.sensorType, scale: cmd.scale, value: cmd.scaledSensorValue.toString() ]

    // Sensor Types up to V4 only, there are further sensor types up to V10 defined.
    switch (cmd.sensorType) {
        case 1:  // Air Temperature (V1)
            map.name = "temperature"
            map.unit = (cmd.scale == 1) ? "F" : "C"
            break

        case 2:  // General Purpose (V1)
            map.name = "value"
            map.unit = (cmd.scale == 1) ? "" : "%"
            break

        case 3:  // Luminance (V1)
            map.name = "illuminance"
            map.unit = (cmd.scale == 1) ? "lux" : "%"
            break

        case 4:  // Power (V2)
            map.name = "power"
            map.unit = (cmd.scale == 1) ? "Btu/h" : "W"
            break

        case 5:  // Humidity (V2)
            map.name = "humidity"
            map.unit = (cmd.scale == 1) ? "g/m^3" : "%"
            break

        case 6:  // Velocity (V2)
            map.name = "velocity"
            map.unit = (cmd.scale == 1) ? "mph" : "m/s"
            break

        case 7:  // Direction (V2)
            map.name = "direction"
            map.unit = ""
            break

        case 8:  // Atmospheric Pressure (V2)
        case 9:  // Barometric Pressure (V2)
            map.name = "pressure"
            map.unit = (cmd.scale == 1) ? "inHg" : "kPa"
            break

        case 0xA:  // Solar Radiation (V2)
            map.name = "radiation"
            map.unit = "W/m^3"
            break

        case 0xB:  // Dew Point (V2)
            map.name = "dewPoint"
            map.unit = (cmd.scale == 1) ? "F" : "C"
            break

        case 0xC:  // Rain Rate (V2)
            map.name = "rainRate"
            map.unit = (cmd.scale == 1) ? "in/h" : "mm/h"
            break

        case 0xD:  // Tide Level (V2)
            map.name = "tideLevel"
            map.unit = (cmd.scale == 1) ? "ft" : "m"
            break

        case 0xE:  // Weight (V3)
            map.name = "weight"
            map.unit = (cmd.scale == 1) ? "lbs" : "kg"
            break

        case 0xF:  // Voltage (V3)
            map.name = "voltage"
            map.unit = (cmd.scale == 1) ? "mV" : "V"
            break

        case 0x10:  // Current (V3)
            map.name = "current"
            map.unit = (cmd.scale == 1) ? "mA" : "A"
            break

        case 0x11:  // Carbon Dioxide Level (V3)
            map.name = "carbonDioxide"
            map.unit = "ppm"
            break

        case 0x12:  // Air Flow (V3)
            map.name = "fluidFlow"
            map.unit = (cmd.scale == 1) ? "cfm" : "m^3/h"
            break

        case 0x13:  // Tank Capacity (V3)
            map.name = "fluidVolume"
            map.unit = (cmd.scale == 0) ? "ltr" : (cmd.scale == 1) ? "m^3" : "gal"
            break

        case 0x14:  // Distance (V3)
            map.name = "distance"
            map.unit = (cmd.scale == 0) ? "m" : (cmd.scale == 1) ? "cm" : "ft"
            break

        default:
            logger("zwaveEvent(): SensorMultilevelReport with unhandled sensorType: ${cmd}","warn")
            map.name = "unknown"
            map.unit = "unknown"
            break
    }

    logger("New multilevel sensor report: Name: ${map.name}, Value: ${map.value}, Unit: ${map.unit}","info")

    // Update meta-data cache:
    if (state.zwtSensorMultilevelReportsMd?.find( { it.sensorType == map.sensorType } )) { // Known SensorMultilevelReport type, so update attributes.
        state.zwtSensorMultilevelReportsMd?.collect {
            if (it.sensorType == map.sensorType) {
                it.scale = map.scale
                it.name = map.name
                it.unit = map.unit
                it.lastValue = map.value
            }
        }
    }
    else { // New SensorMultilevelReport type:
        logger("zwaveEvent(): New SensorMultilevelReport type discovered.","debug")
        state.zwtSensorMultilevelReportsMd << [
                sensorType: map.sensorType,
                scale: map.scale,
                name: map.name,
                unit: map.unit,
                lastValue: map.value
        ]
    }
}

/**
 *  zwaveEvent( COMMAND_CLASS_METER_V3 (0x32) : METER_REPORT_V3 (0x02) )
 *
 *  The Meter Report Command is used to advertise a meter reading.
 *
 *  Action: Cache meter report in state.zwtMeterReportsMd, and log info message.
 *
 *  cmd attributes:
 *    Integer        deltaTime                   Time in seconds since last report.
 *    Short          meterType                   Specifies the type of metering device.
 *      0x00 = Unknown
 *      0x01 = Electric meter
 *      0x02 = Gas meter
 *      0x03 = Water meter
 *    List<Short>    meterValue                  Meter value as an array of bytes.
 *    Double         scaledMeterValue            Meter value as a double.
 *    List<Short>    previousMeterValue          Previous meter value as an array of bytes.
 *    Double         scaledPreviousMeterValue    Previous meter value as a double.
 *    Short          size                        The size of the array for the meterValue and previousMeterValue.
 *    Short          scale                       Indicates what unit the sensor uses (dependent on meterType).
 *    Short          precision                   The decimal precision of the values.
 *    Short          rateType                    Specifies if it is import or export values to be read.
 *      0x01 = Import (consumed)
 *      0x02 = Export (produced)
 *    Boolean        scale2                      ???
 **/
def zwaveEvent(physicalgraph.zwave.commands.meterv3.MeterReport cmd) {
    logger("zwaveEvent(): Meter Report received: ${cmd}","trace")

    def result = []
    def map = [meterType: cmd.meterType, scale: cmd.scale, value: cmd.scaledMeterValue]

    switch (cmd.meterType) {
        case 1:  // Electric meter:
            map.meterTypeName = 'Electric'
            switch (cmd.scale) {
                case 0:  // Accumulated Energy (kWh):
                    map.scaleName = 'Accumulated Energy'
                    map.unit = 'kWh'
                    break

                case 1:  // Accumulated Energy (kVAh):
                    map.scaleName = 'Accumulated Energy'
                    map.unit = 'kVAh'
                    break

                case 2:  // Instantaneous Power (Watts):
                    map.scaleName = 'Instantaneous Power'
                    map.unit = 'W'
                    break

                case 3:  // Accumulated Pulse Count:
                    map.scaleName = 'Accumulated Electricity Pulse Count'
                    map.unit = ''
                    break

                case 4:  // Instantaneous Voltage (Volts):
                    map.scaleName = 'Instantaneous Voltage'
                    map.unit = 'V'
                    break

                 case 5:  // Instantaneous Current (Amps):
                    map.scaleName = 'Instantaneous Current'
                    map.unit = 'A'
                    break

                 case 6:  // Instantaneous Power Factor:
                    map.scaleName = 'Instantaneous Power Factor'
                    map.unit = ''
                    break

                default:
                    map.scaleName = 'Unknown'
                    map.unit = 'Unknown'
                    break
            }
            break

        case 2:  // Gas meter:
            map.meterTypeName = 'Gas'
            switch (cmd.scale) {
                case 0:  // Accumulated Gas Volume (m^3):
                    map.scaleName = 'Accumulated Gas Volume'
                    map.unit = 'm^3'
                    break

                case 1:  // Accumulated Gas Volume (ft^3):
                    map.scaleName = 'Accumulated Gas Volume'
                    map.unit = 'ft^3'
                    break

                case 3:  // Accumulated Pulse Count:
                    map.scaleName = 'Accumulated Gas Pulse Count'
                    map.unit = ''
                    break

                default:
                    map.scaleName = 'Unknown'
                    map.unit = 'Unknown'
                    break
            }
            break

        case 3:  // Water meter:
            map.meterTypeName = 'Water'
            switch (cmd.scale) {
                case 0:  // Accumulated Water Volume (m^3):
                    map.scaleName = 'Accumulated Water Volume'
                    map.unit = 'm^3'
                    break

                case 1:  // Accumulated Water Volume (ft^3):
                    map.scaleName = 'Accumulated Water Volume'
                    map.unit = 'ft^3'
                    break

                case 2:  // Accumulated Water Volume (US gallons):
                    map.scaleName = 'Accumulated Water Volume'
                    map.unit = 'gal'
                    break

                case 3:  // Accumulated Pulse Count:
                    map.scaleName = 'Accumulated Water Pulse Count'
                    map.unit = ''
                    break

                default:
                    map.scaleName = 'Unknown'
                    map.unit = 'Unknown'
                    break
            }
            break

        default:
            map.meterTypeName = 'Unknown'
            map.scaleName = 'Unknown'
            map.unit = 'Unknown'
            break
    }

    logger("New meter report: ${map.scaleName}: ${map.value} ${map.unit}", (map.scaleName == 'Unknown') ? "warn" : "info")

    // Update meta-data cache:
    if (state.zwtMeterReportsMd?.find( { it.meterType == map.meterType & it.scale == map.scale } )) { // Known MeterReport type, so update attributes.
        state.zwtMeterReportsMd?.collect {
            if (it.meterType == map.meterType & it.scale == map.scale) {
                it.meterTypeName = map.meterTypeName
                it.scaleName = map.scaleName
                it.unit = map.unit
                it.lastValue = map.value
            }
        }
    }
    else { // New MeterReport type:
        logger("zwaveEvent(): New MeterReport type discovered.","debug")
        state.zwtMeterReportsMd << [
                meterType: map.meterType,
                meterTypeName: map.meterTypeName,
                scale: map.scale,
                scaleName: map.scaleName,
                unit: map.unit,
                lastValue: map.value
        ]
    }

}

/**
 *  zwaveEvent( COMMAND_CLASS_CRC16_ENCAP (0x56) : CRC_16_ENCAP (0x01) )
 *
 *  The CRC-16 Encapsulation Command Class is used to encapsulate a command with an additional CRC-16 checksum
 *  to ensure integrity of the payload. The purpose for this command class is to ensure a higher integrity level
 *  of payloads carrying important data.
 *
 *  Action: Extract the encapsulated command and pass to zwaveEvent().
 *
 *  Note: Validation of the checksum is not necessary as this is performed by the hub.
 *
 *  cmd attributes:
 *    Integer      checksum      Checksum.
 *    Short        command       Command identifier of the embedded command.
 *    Short        commandClass  Command Class identifier of the embedded command.
 *    List<Short>  data          Embedded command data.
 *
 *  Example: Crc16Encap(checksum: 125, command: 2, commandClass: 50, data: [33, 68, 0, 0, 0, 194, 0, 0, 77])
 **/
def zwaveEvent(physicalgraph.zwave.commands.crc16encapv1.Crc16Encap cmd) {
    logger("zwaveEvent(): CRC-16 Encapsulation Command received: ${cmd}","trace")

    def versions = getCommandClassVersions()
    def version = versions[cmd.commandClass as Integer]
    def ccObj = version ? zwave.commandClass(cmd.commandClass, version) : zwave.commandClass(cmd.commandClass)
    def encapsulatedCommand = ccObj?.command(cmd.command)?.parse(cmd.data)
    // TO DO: It should be possible to replace the lines above with this line soon...
    //def encapsulatedCommand = cmd.encapsulatedCommand(getCommandClassVersions())
    if (!encapsulatedCommand) {
        logger("zwaveEvent(): Could not extract command from ${cmd}","error")
    } else {
        cacheCommandMd(encapsulatedCommand, "CRC_16_ENCAP")
        return zwaveEvent(encapsulatedCommand)
    }
}

/**
 *  zwaveEvent( COMMAND_CLASS_ASSOCIATION_GRP_INFO (0x59) : ASSOCIATION_GROUP_NAME_REPORT (0x02) )
 *
 *  The Association Group Name Report command is used to advertise the name of an association group.
 *
 *  Action: Store the group name in state.zwtAssocGroupsMd
 *
 *  cmd attributes:
 *    Short        groupingIdentifier
 *    Short        lengthOfName
 *    List<Short>  name
 *
 *  Example: AssociationGroupNameReport(groupingIdentifier: 1, lengthOfName: 8, name: [76, 105, 102, 101, 108, 105, 110, 101])
 **/
def zwaveEvent(physicalgraph.zwave.commands.associationgrpinfov1.AssociationGroupNameReport cmd) {
    logger("zwaveEvent(): Association Group Name Report received: ${cmd}","trace")

    def name = new String(cmd.name as byte[])
    logger("Association Group #${cmd.groupingIdentifier} has name: ${name}","info")

    if(state.zwtAssocGroupsMd.find( { it.id == cmd.groupingIdentifier } )) {
        state.zwtAssocGroupsMd.collect {
            if (it.id == cmd.groupingIdentifier) {
                it.name = name
            }
        }
    }
    else { // Add new group, but don't trigger sync.
        state.zwtAssocGroupsMd << [id: cmd.groupingIdentifier, name: new String(cmd.name as byte[])]
    }
}

/**
 *  zwaveEvent( COMMAND_CLASS_MULTI_CHANNEL_V3 (0x60) : MULTI_CHANNEL_CAPABILITY_REPORT_V3 (0x0A) )
 *
 *  The Multi Channel Capability Report command is used to advertise the generic and specific device class of the
 *  End Point and the supported command classes.
 *
 *  Action: Cache meta-data in state.zwtEndpointsMd, and log an info message.
 *
 *  cmd attributes:
 *    List<Short>  commandClass         The command classes implemented by the device for this endpoint.
 *    Boolean      dynamic              True if the endpoint is dynamic.
 *    Short        endPoint             Endpoint ID. (0-127)
 *    Short        genericDeviceClass   The Generic Device Class of the advertised endpoint.
 *    Short        specificDeviceClass  The Specific Device Class of the advertised endpoint.
 *
 *  Example: MultiChannelCapabilityReport(commandClass: [37, 50], dynamic: false, endPoint: 1,
 *   genericDeviceClass: 16, specificDeviceClass: 1)
 **/
def zwaveEvent(physicalgraph.zwave.commands.multichannelv3.MultiChannelCapabilityReport cmd) {
    logger("zwaveEvent(): Multi Channel Capability Report received: ${cmd}","info")

    // Add Endpoint to meta-data cache:
    if (state.zwtEndpointsMd?.find( { it.id == cmd.endPoint } )) { // Known endpoint:
        state.zwtEndpointsMd.collect {
            if (it.id == cmd.endPoint) {
                it.dynamic = cmd.dynamic
                it.genericDeviceClass = cmd.genericDeviceClass
                it.specificDeviceClass = cmd.specificDeviceClass
                it.commandClasses = cmd.commandClass
            }
        }
    }
    else { // New Endpoint:
        logger("zwaveEvent(): New endpoint discovered.","debug")
        state.zwtEndpointsMd << [
            id: cmd.endPoint,
            dynamic: cmd.dynamic,
            genericDeviceClass: cmd.genericDeviceClass,
            specificDeviceClass: cmd.specificDeviceClass,
            commandClasses: cmd.commandClass
        ]
    }
}

/**
 *  zwaveEvent( COMMAND_CLASS_MULTI_CHANNEL_V3 (0x60) : MULTI_CHANNEL_CMD_ENCAP_V3 (0x0D) )
 *
 *  The Multi Channel Command Encapsulation command is used to encapsulate commands. Any command supported by
 *  a Multi Channel End Point may be encapsulated using this command.
 *
 *  Action: Extract the encapsulated command and pass to the appropriate zwaveEvent() handler.
 *
 *  cmd attributes:
 *    Boolean      bitAddress           Set to true if multicast addressing is used.
 *    Short        command              Command identifier of the embedded command.
 *    Short        commandClass         Command Class identifier of the embedded command.
 *    Short        destinationEndPoint  Destination End Point.
 *    List<Short>  parameter            Carries the parameter(s) of the embedded command.
 *    Short        sourceEndPoint       Source End Point.
 *
 *  Example: MultiChannelCmdEncap(bitAddress: false, command: 1, commandClass: 32, destinationEndPoint: 0,
 *            parameter: [0], sourceEndPoint: 1)
 **/
def zwaveEvent(physicalgraph.zwave.commands.multichannelv3.MultiChannelCmdEncap cmd) {
    logger("zwaveEvent(): Multi Channel Command Encapsulation command received: ${cmd}","info")

    // Add Endpoint to meta-data cache:
    if (!state.zwtEndpointsMd?.find( { it.id == cmd.sourceEndPoint } )) { // New Endpoint:
        logger("zwaveEvent(): New endpoint discovered.","debug")
        state.zwtEndpointsMd << [id: cmd.sourceEndPoint]
        sendCommands([zwave.multiChannelV3.multiChannelCapabilityGet(endPoint: cmd.sourceEndPoint)])
    }

    def encapsulatedCommand = cmd.encapsulatedCommand(getCommandClassVersions())
    if (!encapsulatedCommand) {
        logger("zwaveEvent(): Could not extract command from ${cmd}","error")
    } else {
        cacheCommandMd(encapsulatedCommand, "MULTI_CHANNEL_CMD_ENCAP", cmd.sourceEndPoint, cmd.destinationEndPoint)
        return zwaveEvent(encapsulatedCommand)
    }
}

/**
 *  zwaveEvent( COMMAND_CLASS_CONFIGURATION_V2 (0x70) : CONFIGURATION_REPORT_V2 (0x03) )
 *
 *  The Configuration Report Command is used to advertise the actual value of the advertised parameter.
 *
 *  Action: Store the value in the parameter cache, update syncPending, and log an info message.
 *
 *  Note: Ideally, we want to update the corresponding preference value shown on the Settings GUI, however this
 *  is not possible due to security restrictions in the SmartThings platform.
 *
 *  cmd attributes:
 *    List<Short>  configurationValue  Value of parameter (byte array).
 *    Short        parameterNumber     Parameter ID.
 *    Short        size                Size of parameter's value (bytes).
 *
 *  Example: ConfigurationReport(configurationValue: [10], parameterNumber: 0, reserved11: 0,
 *            scaledConfigurationValue: 10, size: 1)
 **/
def zwaveEvent(physicalgraph.zwave.commands.configurationv2.ConfigurationReport cmd) {
    logger("zwaveEvent(): Configuration Report received: ${cmd}","trace")

    logger("Parameter #${cmd.parameterNumber}: Size: ${cmd.size}, Value: ${cmd.scaledConfigurationValue}","info")

    if (state.zwtParamsMd.find( { it.id == cmd.parameterNumber } )) { // Parameter is already known, so update attributes.
        state.zwtParamsMd.collect {
            if (it.id == cmd.parameterNumber) {
                it.scaledConfigurationValue = cmd.scaledConfigurationValue
                it.size = cmd.size
            }
        }
    }
    else { // new parameter
        logger("zwaveEvent(): New parameter discovered.","debug")
        state.zwtParamsMd << [id: cmd.parameterNumber, scaledConfigurationValue: cmd.scaledConfigurationValue, size: cmd.size]
        // Trigger sync() again if this is the target parameter:
        if ( cmd.parameterNumber == state.zwtParamTarget?.id ) { sync() }
    }

    updateSyncPending()
}

/**
 *  zwaveEvent( COMMAND_CLASS_NOTIFICATION_V3 (0x71) : NOTIFICATION_REPORT_V3 (0x05) )
 *
 *  The Notification Report Command is used to advertise notification information.
 *
 *  Action: Log info message.
 *
 *  cmd attributes:
 *    Short        event                  Event Type (see code below).
 *    List<Short>  eventParameter         Event Parameter(s) (depends on Event type).
 *    Short        eventParametersLength  Length of eventParameter.
 *    Short        notificationStatus     The notification reporting status of the device (depends on push or pull model).
 *    Short        notificationType       Notification Type (see code below).
 *    Boolean      sequence
 *    Short        v1AlarmLevel           Legacy Alarm Level from Alarm CC V1.
 *    Short        v1AlarmType            Legacy Alarm Type from Alarm CC V1.
 *    Short        zensorNetSourceNodeId  Source node ID
 *
 *  Example: NotificationReport(event: 8, eventParameter: [], eventParametersLength: 0, notificationStatus: 255,
 *    notificationType: 8, reserved61: 0, sequence: false, v1AlarmLevel: 0, v1AlarmType: 0, zensorNetSourceNodeId: 0)
 **/
def zwaveEvent(physicalgraph.zwave.commands.notificationv3.NotificationReport cmd) {
    logger("zwaveEvent(): Notification Report received: ${cmd}","info")

    def map = [
        notificationType: cmd.notificationType,
        notificationTypeName: "Unknown",
        event: cmd.event,
        eventName: "Unknown"
    ]

    switch (cmd.notificationType) {
        case 1:  // Smoke Alarm:
            map.notificationTypeName = "Smoke Alarm"
            break
        case 2:  // CO Alarm:
            map.notificationTypeName = "CO Alarm"
            break
        case 3:  // CO2 Alarm:
            map.notificationTypeName = "CO2 Alarm"
            break

        case 4:  // Heat Alarm:
            map.notificationTypeName = "Heat Alarm"
            switch (cmd.event) {
                case 0:  // Previous Events cleared:
                    map.eventName = "Previous Events cleared"
                    break

                case 1:  // Overheat detected:
                case 2:  // Overheat detected, Unknown Location:
                    map.eventName = "Overheat Detected"
                    break

                case 3:  // Rapid Temperature Rise:
                case 4:  // Rapid Temperature Rise, Unknown Location:
                    map.eventName = "Rapid temperature rise detected"
                    break

                case 5:  // Underheat detected:
                case 6:  // Underheat detected, Unknown Location:
                     map.eventName = "Underheat Detected"
                     break

                default:
                    break
            }
            break

        case 5:  // Water Alarm:
            map.notificationTypeName = "Water Alarm"
            break


        case 8:  // Power Management:
            map.notificationTypeName = "Power Management"
            switch (cmd.event) {
                case 0:  // Previous Events cleared:
                   map.eventName = "Previous Events cleared"
                   break

                case 1:  // Mains Connected:
                   map.eventName = "Mains Connected"
                   break

                case 2:  // AC Mains Disconnected:
                   map.eventName = "AC Mains Disconnected"
                   break

                case 3:  // AC Mains Re-connected:
                   map.eventName = "AC Mains Re-connected"
                   break

                case 4:  // Surge:
                   map.eventName = "Surge detected"
                   break

                case 5:  // Voltage Drop:
                    map.eventName = "Voltage drop detected"
                    break

                case 6:  // Over-current:
                    map.eventName = "Over-current detected"
                    break

                 case 7:  // Over-Voltage:
                    map.eventName = "Over-voltage detected"
                    break

                 case 8:  // Overload:
                    map.eventName = "Overload detected"
                    break

                 case 9:  // Load Error:
                    map.eventName = "Load Error detected"
                    break

                default:
                    break
            }
            break

        case 9:  // System:
            map.notificationTypeName = "System Alarm"
            switch (cmd.event) {
                case 0:  // Previous Events cleared:
                    map.eventName = "Previous Events cleared"
                    break

                case 1:  // Harware Failure:
                case 3:  // Harware Failure (with manufacturer proprietary failure code):
                    map.eventName = "Harware Failure"
                    break

                case 2:  // Software Failure:
                case 4:  // Software Failure (with manufacturer proprietary failure code):
                    map.eventName = "Software Failure"
                    break

                case 6:  // Tampering:
                    map.eventName = "Tampering Detected"
                    break

                default:
                    break
            }
            break

        default:
            logger("zwaveEvent(): Notification Report recieved with unhandled notificationType: ${cmd}","warn")
            break
    }

    logger("New notification report: NotificationName: ${map.notificationTypeName}, EventName: ${map.eventName}","info")

    // Update meta-data cache:
    if (state.zwtNotificationReportsMd?.find( { it.notificationType == map.notificationType & it.event == map.event } )) { // Known NotificationReport type, so update attributes.
        state.zwtNotificationReportsMd?.collect {
            if (it.notificationType == map.notificationType & it.event == map.event) {
                it.notificationTypeName = map.notificationTypeName
                it.eventName = map.eventName
            }
        }
    }
    else { // New NotificationReport type:
        logger("zwaveEvent(): New SensorMultilevelReport type discovered.","debug")
        state.zwtNotificationReportsMd << [
                notificationType: map.notificationType,
                event: map.event,
                notificationTypeName: map.notificationTypeName,
                eventName: map.eventName
        ]
    }
}

/**
 *  zwaveEvent( COMMAND_CLASS_MANUFACTURER_SPECIFIC_V2 (0x72) : MANUFACTURER_SPECIFIC_REPORT_V2 (0x05) )
 *
 *  Manufacturer-Specific Reports are used to advertise manufacturer-specific information, such as product number
 *  and serial number.
 *
 *  Action: Log info message.
 *
 *  Example: ManufacturerSpecificReport(manufacturerId: 153, manufacturerName: GreenWave Reality Inc.,
 *   productId: 2, productTypeId: 2)
 **/
def zwaveEvent(physicalgraph.zwave.commands.manufacturerspecificv2.ManufacturerSpecificReport cmd) {
    logger("zwaveEvent(): Manufacturer-Specific Report received: ${cmd}","trace")

    // Display as hex strings:
    def manufacturerIdDisp = String.format("%04X",cmd.manufacturerId)
    def productIdDisp = String.format("%04X",cmd.productId)
    def productTypeIdDisp = String.format("%04X",cmd.productTypeId)

    logger("Manufacturer-Specific Report: Manufacturer ID: ${manufacturerIdDisp}, Manufacturer Name: ${cmd.manufacturerName}" +
    ", Product Type ID: ${productTypeIdDisp}, Product ID: ${productIdDisp}","info")

    state.zwtGeneralMd.manufacturerId = manufacturerIdDisp
    state.zwtGeneralMd.manufacturerName = manufacturerName
    state.zwtGeneralMd.productTypeId = productTypeIdDisp
    state.zwtGeneralMd.productId = productIdDisp
}

/**
 *  zwaveEvent( COMMAND_CLASS_POWERLEVEL (0x73) : POWERLEVEL_REPORT (0x03) )
 *
 *  The Powerlevel Report is used to advertise the current RF transmit power of the device.
 *
 *  Action: Log an info message.
 *
 *  cmd attributes:
 *    Short  powerLevel  The current power level indicator value in effect on the node
 *    Short  timeout     The time in seconds the node has at Power level before resetting to normal Power level.
 *
 *  Example: PowerlevelReport(powerLevel: 0, timeout: 0)
 **/
def zwaveEvent(physicalgraph.zwave.commands.powerlevelv1.PowerlevelReport cmd) {
    logger("zwaveEvent(): Powerlevel Report received: ${cmd}","trace")
    def power = (cmd.powerLevel > 0) ? "minus${cmd.powerLevel}dBm" : "NormalPower"
    logger("Powerlevel Report: Power: ${power}, Timeout: ${cmd.timeout}","info")
    state.zwtGeneralMd.powerlevel = power
}

/**
 *  zwaveEvent( COMMAND_CLASS_PROTECTION_V2 (0x75) : PROTECTION_REPORT_V2 (0x03) )
 *
 *  The Protection Report is used to report the protection state of a device.
 *  I.e. measures to prevent unintentional control (e.g. by a child).
 *
 *  Action: Log info message.
 *
 *  cmd attributes:
 *    Short  localProtectionState  Local protection state (i.e. physical switches/buttons)
 *    Short  rfProtectionState     RF protection state.
 *
 *  Example: ProtectionReport(localProtectionState: 0, reserved01: 0, reserved11: 0, rfProtectionState: 0)
 **/
def zwaveEvent(physicalgraph.zwave.commands.protectionv2.ProtectionReport cmd) {
    logger("zwaveEvent(): Protection Report received: ${cmd}","trace")

    state.zwtGeneralMd.protectionLocalId = cmd.localProtectionState
    state.zwtGeneralMd.protectionRFId = cmd.rfProtectionState

    def lp, rfp = ""

    switch(cmd.localProtectionState)  {
        case 0:
            lp = "Unprotected"
            break
        case 1:
            lp = "Protection by sequence"
            break
        case 2:
            lp = "No operation possible"
            break
        default:
            lp = "Unknwon"
            break

    }

    switch(cmd.rfProtectionState)  {
        case 0:
            rfp = "Unprotected"
            break
        case 1:
            rfp = "No RF Control"
            break
        case 2:
            rfp = "No RF Response"
            break
        default:
            rfp = "Unknwon"
            break
    }

    logger("Protection Report: Local: ${cmd.localProtectionState} (${lp}), RF: ${cmd.rfProtectionState} (${rfp})","info")

    state.zwtGeneralMd.protectionLocalDesc = lp
    state.zwtGeneralMd.protectionRFDesc = rfp

    updateSyncPending()
}

/**
 *  zwaveEvent( COMMAND_CLASS_FIRMWARE_UPDATE_MD_V2 (0x7A) : FIRMWARE_MD_REPORT_V2 (0x02) )
 *
 *  The Firmware Meta Data Report Command is used to advertise the status of the current firmware in the device.
 *
 *  Action: Log info message.
 *
 *  cmd attributes:
 *    Integer  checksum        Checksum of the firmware image.
 *    Integer  firmwareId      Firware ID (this is not the firmware version).
 *    Integer  manufacturerId  Manufacturer ID.
 *
 *  Example: FirmwareMdReport(checksum: 50874, firmwareId: 274, manufacturerId: 271)
 **/
def zwaveEvent(physicalgraph.zwave.commands.firmwareupdatemdv2.FirmwareMdReport cmd) {
    logger("zwaveEvent(): Firmware Metadata Report received: ${cmd}","trace")

    // Display as hex strings:
    def firmwareIdDisp = String.format("%04X",cmd.firmwareId)
    def checksumDisp = String.format("%04X",cmd.checksum)

    logger("Firmware Metadata Report: Firmware ID: ${firmwareIdDisp}, Checksum: ${checksumDisp}","info")
    state.zwtGeneralMd.firmwareId = firmwareIdDisp
    state.zwtGeneralMd.firmwareChecksum = checksumDisp
}

/**
 *  zwaveEvent( COMMAND_CLASS_ASSOCIATION_V2 (0x85) : ASSOCIATION_REPORT_V2 (0x03) )
 *
 *  The Association Report command is used to advertise the current destination nodes of a given association group.
 *
 *  Action: Cache value and log info message only.
 *
 *  Note: Ideally, we want to update the corresponding preference value shown on the Settings GUI, however this
 *  is not possible due to security restrictions in the SmartThings platform.
 *
 *  Example: AssociationReport(groupingIdentifier: 1, maxNodesSupported: 1, nodeId: [1], reportsToFollow: 0)
 **/
def zwaveEvent(physicalgraph.zwave.commands.associationv2.AssociationReport cmd) {
    logger("zwaveEvent(): Association Report received: ${cmd}","trace")

    logger("Association Group #${cmd.groupingIdentifier} contains nodes: ${toHexString(cmd.nodeId)} (hexadecimal format)","info")

    if (state.zwtAssocGroupsMd.find( { it.id == cmd.groupingIdentifier } )) { // Group is already known, so update attributes.
        state.zwtAssocGroupsMd.collect {
            if (it.id == cmd.groupingIdentifier) {
                it.maxNodesSupported = cmd.maxNodesSupported
                it.nodes = cmd.nodeId
            }
        }
    }
    else { // New group:
        logger("zwaveEvent(): New association group discovered.","debug")
        state.zwtAssocGroupsMd << [id: cmd.groupingIdentifier, maxNodesSupported: cmd.maxNodesSupported, nodes: cmd.nodeId]
        // Trigger sync() again if this is the target association group:
        if ( cmd.groupingIdentifier == state.zwtAssocGroupTarget?.id ) { sync() }
    }

    updateSyncPending()
}

/**
 *  zwaveEvent( COMMAND_CLASS_VERSION (0x86) : VERSION_REPORT (0x12) )
 *
 *  The Version Report Command is used to advertise the library type, protocol version, and application version.

 *  Action: Log an info message.
 *
 *  cmd attributes:
 *    Short  applicationSubVersion
 *    Short  applicationVersion
 *    Short  zWaveLibraryType
 *    Short  zWaveProtocolSubVersion
 *    Short  zWaveProtocolVersion
 *
 *  Example: VersionReport(applicationSubVersion: 4, applicationVersion: 3, zWaveLibraryType: 3,
 *   zWaveProtocolSubVersion: 5, zWaveProtocolVersion: 4)
 **/
def zwaveEvent(physicalgraph.zwave.commands.versionv1.VersionReport cmd) {
    logger("zwaveEvent(): Version Report received: ${cmd}","trace")

    def zWaveLibraryTypeDisp  = String.format("%02X",cmd.zWaveLibraryType)
    def zWaveLibraryTypeDesc  = ""
    switch(cmd.zWaveLibraryType) {
        case 1:
            zWaveLibraryTypeDesc = "Static Controller"
            break

        case 2:
            zWaveLibraryTypeDesc = "Controller"
            break

        case 3:
            zWaveLibraryTypeDesc = "Enhanced Slave"
            break

        case 4:
            zWaveLibraryTypeDesc = "Slave"
            break

        case 5:
            zWaveLibraryTypeDesc = "Installer"
            break

        case 6:
            zWaveLibraryTypeDesc = "Routing Slave"
            break

        case 7:
            zWaveLibraryTypeDesc = "Bridge Controller"
            break

        case 8:
            zWaveLibraryTypeDesc = "Device Under Test (DUT)"
            break

        case 0x0A:
            zWaveLibraryTypeDesc = "AV Remote"
            break

        case 0x0B:
            zWaveLibraryTypeDesc = "AV Device"
            break

        default:
            zWaveLibraryTypeDesc = "N/A"
    }

    def applicationVersionDisp = String.format("%d.%02d",cmd.applicationVersion,cmd.applicationSubVersion)
    def zWaveProtocolVersionDisp = String.format("%d.%02d",cmd.zWaveProtocolVersion,cmd.zWaveProtocolSubVersion)

    logger("Version Report: Application Version: ${applicationVersionDisp}, " +
           "Z-Wave Protocol Version: ${zWaveProtocolVersionDisp}, " +
           "Z-Wave Library Type: ${zWaveLibraryTypeDisp} (${zWaveLibraryTypeDesc})","info")

    // Store in GeneralMd cache:
    state.zwtGeneralMd.applicationVersion = applicationVersionDisp
    state.zwtGeneralMd.zWaveProtocolVersion = zWaveProtocolVersionDisp
    state.zwtGeneralMd.zWaveLibraryType = "${zWaveLibraryTypeDisp} (${zWaveLibraryTypeDesc})"

}

/**
 *  zwaveEvent( COMMAND_CLASS_INDICATOR (0x87) : INDICATOR_REPORT (0x03) )
 *
 *  The Indicator Report command is used to advertise the state of an indicator.
 *
 *  Action: Log info message.
 *
 *  cmd attributes:
 *    Short value  Indicator status.
 *      0x00       = Off/Disabled
 *      0x01..0x63 = Indicator Range.
 *      0xFF       = On/Enabled.
 *
 *  Example: IndicatorReport(value: 0)
 **/
def zwaveEvent(physicalgraph.zwave.commands.indicatorv1.IndicatorReport cmd) {
    logger("zwaveEvent(): Indicator Report received: ${cmd}","info")
}

/**
 *  zwaveEvent( COMMAND_CLASS_MULTI_CHANNEL_ASSOCIATION_V2 (0x8E) : ASSOCIATION_REPORT_V2 (0x03) )
 *
 *  The Multi-channel Association Report command is used to advertise the current destinations of a given
 *  association group (nodes and endpoints).
 *
 *  Action: Store the destinations in the zwtAssocGroup cache, update syncPending, and log an info message.
 *
 *  Note: Ideally, we want to update the corresponding preference value shown on the Settings GUI, however this
 *  is not possible due to security restrictions in the SmartThings platform.
 *
 *  Example: MultiChannelAssociationReport(groupingIdentifier: 2, maxNodesSupported: 8, nodeId: [9,0,1,1,2,3],
 *            reportsToFollow: 0)
 **/
def zwaveEvent(physicalgraph.zwave.commands.multichannelassociationv2.MultiChannelAssociationReport cmd) {
    logger("zwaveEvent(): Multi-Channel Association Report received: ${cmd}","trace")

    logger("Association Group #${cmd.groupingIdentifier} contains destinations: ${toHexString(cmd.nodeId)} (hexadecimal format)","info")

    if (state.zwtAssocGroupsMd.find( { it.id == cmd.groupingIdentifier } )) { // Group is already known, so update attributes.
        state.zwtAssocGroupsMd.collect {
            if (it.id == cmd.groupingIdentifier) {
                it.nodes = cmd.nodeId
                if (cmd.maxNodesSupported > 0) { // Assoc Group supports MultiChannel only if maxNodesSupported > 0.
                    it.multiChannel = true
                    it.maxNodesSupported = cmd.maxNodesSupported
                }
            }
        }
    }
    else { // New group:
        logger("zwaveEvent(): New association group discovered.","debug")
        def newAssocGroup = [id: cmd.groupingIdentifier, nodes: cmd.nodeId]
        if (cmd.maxNodesSupported > 0) {
            newAssocGroup.multiChannel = true
            newAssocGroup.maxNodesSupported = cmd.maxNodesSupported
        }
        state.zwtAssocGroupsMd << newAssocGroup
        // Trigger sync() again if this is the target association group:
        if ( cmd.groupingIdentifier == state.zwtAssocGroupTarget?.id ) { sync() }
    }

    updateSyncPending()
}

/**
 *  zwaveEvent( COMMAND_CLASS_SECURITY (0x98) : SECURITY_MESSAGE_ENCAPSULATION (0x81) )
 *
 *  The Security Message Encapsulation command is used to encapsulate Z-Wave commands using AES-128.
 *
 *  Action: Extract the encapsulated command and pass to the appropriate zwaveEvent() handler.
 *    Set state.useSecurity flag to true.
 *
 *  cmd attributes:
 *    List<Short> commandByte         Parameters of the encapsulated command.
 *    Short   commandClassIdentifier  Command Class ID of the encapsulated command.
 *    Short   commandIdentifier       Command ID of the encapsulated command.
 *    Boolean secondFrame             Indicates if first or second frame.
 *    Short   sequenceCounter
 *    Boolean sequenced               True if the command is transmitted using multiple frames.
 **/
def zwaveEvent(physicalgraph.zwave.commands.securityv1.SecurityMessageEncapsulation cmd) {
    logger("zwaveEvent(): Security Encapsulated Command received: ${cmd}","trace")

    state.useSecurity = true

    def encapsulatedCommand = cmd.encapsulatedCommand(getCommandClassVersions())
    if (encapsulatedCommand) {
        cacheCommandMd(encapsulatedCommand, "SECURITY_MESSAGE_ENCAPSULATION")
        return zwaveEvent(encapsulatedCommand)
    } else {
        logger("zwaveEvent(): Unable to extract security encapsulated command from: ${cmd}","error")
    }
}

/**
 *  zwaveEvent( COMMAND_CLASS_SECURITY (0x98) : SECURITY_COMMANDS_SUPPORTED_REPORT (0x03) )
 *
 *  The Security Commands Supported Report command advertises which command classes are supported using security
 *  encapsulation.
 *
 *  Action: Log an info message. Set state.useSecurity flag to true.
 *
 *  cmd attributes:
 *    List<Short>  commandClassControl
 *    List<Short>  commandClassSupport
 *    Short        reportsToFollow
 *
 *  Exmaple: SecurityCommandsSupportedReport(commandClassControl: [43],
 *   commandClassSupport: [32, 90, 133, 38, 142, 96, 112, 117, 39], reportsToFollow: 0)
 **/
def zwaveEvent(physicalgraph.zwave.commands.securityv1.SecurityCommandsSupportedReport cmd) {
    logger("zwaveEvent(): Security Commands Supported Reportreceived: ${cmd}","trace")

    state.useSecurity = true
    state.zwtGeneralMd.securityCommandClassSupport = cmd.commandClassSupport.sort()
    state.zwtGeneralMd.securityCommandClassControl = cmd.commandClassControl.sort()

    logger("Command classes supported with security encapsulation: ${toCcNames(state.zwtGeneralMd.securityCommandClassSupport, true)}","info")
    logger("Command classes supported for CONTROL with security encapsulation: ${toCcNames(state.zwtGeneralMd.securityCommandClassControl, true)}","info")
}

/**
 *  zwaveEvent( DEFAULT CATCHALL )
 *
 *  Called for all commands that aren't handled above.
 **/
def zwaveEvent(physicalgraph.zwave.Command cmd) {
    logger("zwaveEvent(): No handler for command: ${cmd}","warn")
}

/*****************************************************************************************************************
 *  Commands:
 *****************************************************************************************************************/

/**
 *  initialise()
 *
 *  Sets up meta-data caches, parses fingerprint, and determines if the device is using security encapsulation.
 **/
private initialise() {
    logger("initialise()","trace")

    // Initialise meta-data stores if they don't exist:
    if (!state.zwtGeneralMd) state.zwtGeneralMd = [:] // Map!
    if (!state.zwtCommandsMd) state.zwtCommandsMd = []
    if (!state.zwtAssocGroupsMd) state.zwtAssocGroupsMd = []
    if (!state.zwtEndpointsMd) state.zwtEndpointsMd = []
    if (!state.zwtParamsMd) state.zwtParamsMd = []
    if (!state.zwtMeterReportsMd) state.zwtMeterReportsMd = []
    if (!state.zwtNotificationReportsMd) state.zwtNotificationReportsMd = []
    if (!state.zwtSensorMultilevelReportsMd) state.zwtSensorMultilevelReportsMd = []

    // Parse fingerprint for supported command classes:
    def ccIds = []
    if (getZwaveInfo()?.cc) {
        logger("Device has new-style fingerprint: ${device.rawDescription}","info")
        ccIds = getZwaveInfo()?.cc + getZwaveInfo()?.sec
    }
    else {
        logger("Device has legacy fingerprint: ${device.rawDescription}","info")
        // Look for hexadecimal numbers (0x##) but remove the first one, which will be deviceID.
        ccIds = device.rawDescription.findAll(/0x\p{XDigit}+/)
        if (ccIds.size() > 0) { ccIds.remove(0) }
    }
    ccIds.removeAll([null])
    state.zwtGeneralMd.commandClassIds = ccIds.sort().collect { Integer.parseInt(it.replace("0x",""),16) } // Parse hex strings to ints.
    state.zwtGeneralMd.commandClassNames = toCcNames(state.zwtGeneralMd.commandClassIds,true) // Parse Ids to names.
    logger("Supported Command Classes: ${state.zwtGeneralMd.commandClassNames}","info")

    // Check zwaveInfo to see if device is using security:
    if (getZwaveInfo()?.zw?.contains("s")) {
        logger("Device is securly paired. Using secure commands.","info")
        state.useSecurity = true
    }

    // Send a secured command, to double-check security.
    def cmds = []
    cmds << zwave.securityV1.securityMessageEncapsulation().encapsulate(zwave.securityV1.securityCommandsSupportedGet()).format()

    sendCommands(cmds,200)
    state.zwtInitialised = true
}

/**
 *  scanGeneral()
 *
 *  Scans for common device attributes such as battery/firmware/version etc.
 **/
private scanGeneral() {
    logger("scanGeneral(): Scanning for common device attributes.","info")

    if (!state.zwtInitialised) { initialise() }

    def cmds = []

    cmds << zwave.batteryV1.batteryGet()
    cmds << zwave.firmwareUpdateMdV2.firmwareMdGet()
    cmds << zwave.manufacturerSpecificV2.manufacturerSpecificGet()
    cmds << zwave.powerlevelV1.powerlevelGet()
    cmds << zwave.protectionV2.protectionGet()
    cmds << zwave.switchAllV1.switchAllGet()
    cmds << zwave.versionV1.versionGet()
    cmds << zwave.wakeUpV1.wakeUpIntervalGet()

    sendCommands(cmds,800)
}

/**
 *  scanAssocGroups()
 *
 *  Scans for association groups. If a group is already known, it is not scanned again.
 **/
private scanAssocGroups() {
    logger("scanAssocGroups(): Scanning Association Groups.","trace")

    if (!state.zwtInitialised) { initialise() }

    // Check the device supports ASSOCIATION or MULTI_CHANNEL_ASSOCIATION, warn if it doesn't.
    if (!state.zwtGeneralMd?.commandClassIds.find( {it == 0x85 || it == 0x8E }) ) {
        logger("sync(): Device does not appear to support ASSOCIATION or MULTI_CHANNEL_ASSOCIATION command classes.","warn")
    }

    def cmds = []

    def start = (settings.zwtAssocGroupsScanStart) ? settings.zwtAssocGroupsScanStart.toInteger() : 0
    def stop = (settings.zwtAssocGroupsScanStop) ? settings.zwtAssocGroupsScanStop.toInteger() : 10

    logger("scanAssocGroups(): Scanning Association Groups (#${start} to #${stop}).","info")
    (start..stop).each { i ->
        if (!state.zwtAssocGroupsMd.find( { it.id == i } )) {
            cmds << zwave.multiChannelAssociationV2.multiChannelAssociationGet(groupingIdentifier: i)
            cmds << zwave.associationV2.associationGet(groupingIdentifier: i)
            cmds << zwave.associationGrpInfoV1.associationGroupNameGet(groupingIdentifier: i)
        }
        else if (!state.zwtAssocGroupsMd.find( { it.id == i } )?.name) {
            cmds << zwave.associationGrpInfoV1.associationGroupNameGet(groupingIdentifier: i)
        }
    }

    sendCommands(cmds,800)
}

/**
 *  scanEndpoints()
 *
 *  Scans for endpoints in multi-channel devices.
 **/
private scanEndpoints() {
    logger("scanEndpoints(): Scanning for Endpoints.","trace")

    if (!state.zwtInitialised) { initialise() }

    // Check the device supports MULTI_CHANNEL, warn if it doesn't.
    if (!state.zwtGeneralMd?.commandClassIds.find( {it == 0x60 }) ) {
        logger("sync(): Device does not appear to support MULTI_CHANNEL command classes.","warn")
    }

    def cmds = []
    //cmds << zwave.multiChannelV3.multiChannelEndPointGet()
    // Returns: MultiChannelEndPointReport(dynamic: false, endPoints: 3, identical: true, res00: 0, res11: false)
    // Only really useful to tell us if the device is using dynamic endpoints.

    // Using multiChannelEndPointFind(genericDeviceClass: 255, specificDeviceClass: 255) is supposed to return all endpoints
    //cmds << zwave.multiChannelV3.multiChannelEndPointFind(genericDeviceClass: 255, specificDeviceClass: 255)
    // However, typically we get back MultiChannelEndPointFindReport(genericDeviceClass: 255, reportsToFollow: 0, specificDeviceClass: 255)
    // which doesn't list any endpoints, so it's not very useful.

    def start = (settings.zwtEndpointsScanStart) ? settings.zwtEndpointsScanStart.toInteger() : 0
    def stop = (settings.zwtEndpointsScanStop) ? settings.zwtEndpointsScanStop.toInteger() : 10

    logger("scanEndpoints(): Scanning for Endpoints (#${start} to #${stop}).","info")
    (start..stop).each {
        cmds << zwave.multiChannelV3.multiChannelCapabilityGet(endPoint: it)
    }

    sendCommands(cmds,800)
}

/**
 *  scanParams()
 *
 *  Scans for device parameters. If a parameter is already known, it is not scanned again.
 **/
private scanParams() {
    logger("scanParams(): Scanning Device Parameters.","trace")

    if (!state.zwtInitialised) { initialise() }

    // Check the device supports CONFIGURATION, warn if it doesn't.
    if (!state.zwtGeneralMd?.commandClassIds.find( {it == 0x70}) ) {
        logger("sync(): Device does not appear to support the CONFIGURATION command class.","warn")
    }

    def cmds = []

    // Use BulkGet (few devices support this).
    //cmds << zwave.configurationV2.configurationBulkGet(numberOfParameters: 10, parameterOffset: 1)

    //Try a CONFIGURATION_NAME_GET (there is no class for configurationV3 yet, so have to build raw command:
    //cmds << "988100" + "700A0001" // This is COMMAND_CLASS_CONFIGURATION, CONFIGURATION_NAME_GET, Parameter 01 (16-bit).

    def start = (settings.zwtParamsScanStart) ? settings.zwtParamsScanStart.toInteger() : 0
    def stop = (settings.zwtParamsScanStop) ? settings.zwtParamsScanStop.toInteger() : 20

    logger("scanParams(): Scanning Device Parameters (#${start} to #${stop}).","info")
    (start..stop).each { i ->
        if (!state.zwtParamsMd.find( { it.id == i } )) {
            cmds << zwave.configurationV2.configurationGet(parameterNumber: i)
        }
    }

    sendCommands(cmds,800)
}

/**
 *  scanActuator()
 *
 *  Scans for common actuator attributes, such as switch and lock state.
 **/
private scanActuator() {
    logger("scanActuator(): Scanning for common actuator values.","info")

    if (!state.zwtInitialised) { initialise() }

    def cmds = []

    cmds << zwave.basicV1.basicGet()
    cmds << zwave.doorLockV1.doorLockOperationGet()
    cmds << zwave.indicatorV1.indicatorGet()
    cmds << zwave.lockV1.lockGet()
    cmds << zwave.switchBinaryV1.switchBinaryGet()
    cmds << zwave.switchColorV3.switchColorGet()
    cmds << zwave.switchMultilevelV2.switchMultilevelGet()

    sendCommands(cmds,800)
}

/**
 *  scanSensor()
 *
 *  Scans for common sensor attributes, such as meter, sensorBinary, sensorMultilevel.
 *
 *  Note: To save time, only scans using command classes that the device advertises. Plus, only the primary
 *   node is scanned.
 *
 *  To Do: Scan all endpoints of a multi-channel device.
 **/
private scanSensor() {
    logger("scanSensor(): Scanning for common sensor types (this can take several minutes to complete).","info")

    if (!state.zwtInitialised) { initialise() }

    def cmds = []

    // sensorBinary:
    if (state.zwtGeneralMd?.commandClassIds.find( {it == 0x30 }) ) {
        logger("scanSensor(): Scanning sensorBinary sensorTypes:","info")
        cmds << zwave.sensorBinaryV2.sensorBinarySupportedGetSensor()
        (0..13).each { sT -> // Scan SensorTypes 0-13 (i.e. all up to V2).
            cmds << zwave.sensorBinaryV2.sensorBinaryGet(sensorType: sT)
        }
    }

    // sensorMultilevel:
    if (state.zwtGeneralMd?.commandClassIds.find( {it == 0x31 }) ) {
        logger("scanSensor(): Scanning sensorMultilevel sensorTypes:","info")
        // These are relatively new and not widely supported:
        cmds << zwave.sensorMultilevelV5.sensorMultilevelSupportedGetSensor()
        cmds << zwave.sensorMultilevelV5.sensorMultilevelSupportedGetScale()
        // So we brute-force scan:
        (0..31).each { sT -> // Scan SensorTypes 0-31 (i.e. all up to V5).
            //(0..3).each { s -> // Scan scales 0-3
                cmds << zwave.sensorMultilevelV5.sensorMultilevelGet(sensorType: sT)
            //}
        }
    }

    // meter:
    if (state.zwtGeneralMd?.commandClassIds.find( {it == 0x32 }) ) {
        logger("scanSensor(): Scanning meter scales:","info")
        (0..6).each { cmds << zwave.meterV3.meterGet(scale: it) }
    }

    // alarm/notification:
    if (state.zwtGeneralMd?.commandClassIds.find( {it == 0x71 }) ) {
        logger("scanSensor(): Scanning alarm/notification:","info")
        cmds << zwave.notificationV3.notificationSupportedGet()
        (0..11).each { nT -> // Scan notificationTypes 0-11.
            cmds << zwave.notificationV3.notificationGet(notificationType: nT)
        }
    }

    sendCommands(cmds,800)
}

/**
 *  printGeneral()
 *
 *  Outputs general/summary information to the IDE Log.
 **/
private printGeneral() {
    logger("printGeneral(): Printing general info.","trace")

    def output = "General Information:"
    def pageSize = 10
    def itemCount = 0

    output += "\nDevice Name: ${device?.displayName}"
    output += "\nRaw Description: ${device?.rawDescription}"
    output += "\nSupported Command Classes: ${state.zwtGeneralMd?.commandClassNames}"

    if (state.useSecurity) {
        output += "\nSecurity: Device is paired securely."
        output += "\n => Command classes supported with security encapsulation: ${toCcNames(state.zwtGeneralMd?.securityCommandClassSupport, true)}"
        output += "\n => Command classes supported for CONTROL with security encapsulation: ${toCcNames(state.zwtGeneralMd?.securityCommandClassControl, true)}"
    }
    else {
        output += "\nSecurity: Device is not using security."
    }

    output += "\nManufacturer ID: ${state.zwtGeneralMd?.manufacturerId}"
    output += "\nManufacturer Name: ${state.zwtGeneralMd?.manufacturerName}"
    output += "\nProduct Type ID: ${state.zwtGeneralMd?.productTypeId}"
    output += "\nProduct ID: ${state.zwtGeneralMd?.productId}"

    output += "\nFirmware Metadata: Firmware ID: ${state.zwtGeneralMd?.firmwareId}, Checksum: ${state.zwtGeneralMd?.firmwareChecksum}"
    output += "\nApplication (Firmware) Version: ${state.zwtGeneralMd?.applicationVersion}"
    output += "\nZ-Wave Protocol Version: ${state.zwtGeneralMd?.zWaveProtocolVersion}"
    output += "\nZ-Wave Library Type: ${state.zwtGeneralMd?.zWaveLibraryType}"

    output += "\nPowerlevel: ${state.zwtGeneralMd?.powerlevel}"
    output += "\nProtection Mode: [ Local: ${state.zwtGeneralMd?.protectionLocalId} (${state.zwtGeneralMd?.protectionLocalDesc}), "
    output += "RF: ${state.zwtGeneralMd?.protectionRFId} (${state.zwtGeneralMd?.protectionRFDesc}) ]"
    output += "\nSwitch_All Mode: ${state.zwtGeneralMd?.switchAllModeId} (${state.zwtGeneralMd?.switchAllModeDesc})"

    logger(output,"info")

    output  = "Discovery Stats:"
    output += "\nNumber of association groups discovered: ${state.zwtAssocGroupsMd?.size()} [Print Assoc Groups]"
    output += "\nNumber of endpoints discovered: ${state.zwtEndpointsMd?.size()} [Print Endpoints]"
    output += "\nNumber of parameters discovered: ${state.zwtParamsMd?.size()} [Print Parameters]"
    output += "\nNumber of unique command types received: ${state.zwtCommandsMd?.size()} [Print Commands]"
    output += "\nNumber of MeterReport types discovered: ${state.zwtMeterReportsMd?.size()} [Print Sensor]"
    output += "\nNumber of NotificationReport types discovered: ${state.zwtNotificationReportsMd?.size()} [Print Sensor]"
    output += "\nNumber of SensorMultilevelReport types discovered: ${state.zwtSensorMultilevelReportsMd?.size()} [Print Sensor]"

    logger(output,"info")
}

/**
 *  printAssocGroups()
 *
 *  Outputs association group information to the IDE Log.
 **/
private printAssocGroups() {
    logger("printAssocGroups(): Printing association groups.","trace")

    def output = ""
    def pageSize = 10
    def itemCount = 0

    output = "Association groups [${state.zwtAssocGroupsMd?.size()}]:"
    state.zwtAssocGroupsMd?.sort( { it.id } ).each {
        def assocGroup = it.clone() // Make copy (don't want to turn the orginal to strings)
        if (assocGroup.nodes) { assocGroup.nodes = toHexString(assocGroup.nodes) }
        output += "\nAssociation Group #${assocGroup.id}: ${assocGroup.sort()}"
        itemCount++
        if (itemCount >= pageSize) {
            logger(output,"info")
            output = ""
            itemCount = 0
        }
    }
    logger(output,"info")

}

/**
 *  printEndpoints()
 *
 *  Outputs endpoint information to the IDE Log.
 **/
private printEndpoints() {
    logger("printEndpoints(): Printing endpoints.","trace")

    def output = ""
    def pageSize = 5
    def itemCount = 0

    output = "Endpoints [${state.zwtEndpointsMd?.size()}]:"
    state.zwtEndpointsMd?.sort( { it.id } ).each {
        def eP = it.clone() // Make copy (don't want to turn the orginal to strings)
        if (eP.commandClasses) {
            eP.commandClassNames = toCcNames(eP.commandClasses,true)
            eP.commandClasses = toHexString(eP.commandClasses,2,true)
        }
        output += "\nEndpoint #${eP.id}: [id: ${eP.id}, dynamic: ${eP.dynamic}, "
        output += "genericDeviceClass: ${eP.genericDeviceClass}, specificDeviceClass: ${eP.specificDeviceClass}, "
        output += "Supported Commands: ${eP.commandClassNames} ]"
        itemCount++
        if (itemCount >= pageSize) {
            logger(output,"info")
            output = ""
            itemCount = 0
        }
    }
    logger(output,"info")
}

/**
 *  printParams()
 *
 *  Outputs parameter information to the IDE Log.
 **/
private printParams() {
    logger("printParams(): Printing parameters.","trace")

    def output = ""
    def pageSize = 20
    def itemCount = 0

    output = "Parameters [${state.zwtParamsMd?.size()}]:"
    state.zwtParamsMd?.sort( { it.id } ).each {
        output += "\nParameter #${it.id}: ${it.sort()}"
        itemCount++
        if (itemCount >= pageSize) {
            logger(output,"info")
            output = ""
            itemCount = 0
        }
    }
    logger(output,"info")
}

/**
 *  printActuator()
 *
 *  Outputs actuator information to the IDE Log.
 *  No meta-data about actuator commands is currently stored, so just call printCommands().
 **/
private printActuator() {
    logger("printSensor(): Printing actuator information.","trace")
    printCommands()
}

/**
 *  printSensor()
 *
 *  Outputs sensor information to the IDE Log.
 **/
private printSensor() {
    logger("printSensor(): Printing sensor information.","trace")

    def output = ""
    def pageSize = 10
    def itemCount = 0

    // SensorMultilevelReports:
    output = "SensorMultilevelReport types [${state.zwtSensorMultilevelReportsMd?.size()}]:"
    state.zwtSensorMultilevelReportsMd?.sort( { it.sensorType } ).each {
        output += "\nSensorMultilevelReport: ${it.sort()}"
        itemCount++
        if (itemCount >= pageSize) {
            logger(output,"info")
            output = ""
            itemCount = 0
        }
    }
    logger(output,"info")
    output = ""
    itemCount = 0

    // MeterReports:
    output = "MeterReport types [${state.zwtMeterReportsMd?.size()}]:"
    state.zwtMeterReportsMd?.sort( { a,b -> a.meterType <=> b.meterType ?: a.scale <=> b.scale } ).each { // Sort by meterType, then scale.
        output += "\nMeterReport: ${it.sort()}"
        itemCount++
        if (itemCount >= pageSize) {
            logger(output,"info")
            output = ""
            itemCount = 0
        }
    }
    logger(output,"info")
    output = ""
    itemCount = 0

    // alarm/notification:
    output = "NotificationReport types [${state.zwtNotificationReportsMd?.size()}]:"
    state.zwtNotificationReportsMd?.sort( { a,b -> a.notificationType <=> b.notificationType ?: a.event <=> b.event } ).each { // Sort by notificationType, then event.
        output += "\nNotificationReport: ${it}"
        itemCount++
        if (itemCount >= pageSize) {
            logger(output,"info")
            output = ""
            itemCount = 0
        }
    }
    logger(output,"info")
}

/**
 *  printCommands()
 *
 *  Outputs information about all unique command types received to the IDE Log.
 **/
private printCommands() {
    logger("printCommands(): Printing commands.","trace")

    def output = ""
    def pageSize = 5
    def itemCount = 0

    output = "Command types [${state.zwtCommandsMd?.size()}]:"
    state.zwtCommandsMd?.sort( { a,b -> a.commandClassId <=> b.commandClassId ?: a.commandId <=> b.commandId } ).each {
        def command = it.clone() // Make copy (don't want to turn the orginal to strings)
        if (command.commandClassId) { command.commandClassId = toHexString(command.commandClassId,2,true) }
        if (command.commandId) { command.commandId = toHexString(command.commandId,2,true) }

        output += "\nCommand: [commandClassId: ${command.commandClassId}, commandClassName: ${command.commandClassName}, " +
        "commandID: ${command.commandId}, description: ${command.description}]\n" +
        " => Example: ${command.parsedCmd}"
        if (command.sourceEndpoint) { output += "\nsourceEndpoint: ${command.sourceEndpoint}, destinationEndpoint ${command.destinationEndpoint}"}
        itemCount++
        if (itemCount >= pageSize) {
            logger(output,"info")
            output = ""
            itemCount = 0
        }
    }
    logger(output,"info")
}

/**
 *  cleanUp()
 *
 *  Cleans up the device handler state, ready for reinstatement of the original device handler.
 **/
private cleanUp() {
    logger("cleanUp(): Cleaning up device state.","info")

    state.remove("zwtInitialised")
    state.remove("zwtGeneralMd")
    state.remove("zwtCommandsMd")
    state.remove("zwtAssocGroupsMd")
    state.remove("zwtEndpointsMd")
    state.remove("zwtParamsMd")
    state.remove("zwtMeterReportsMd")
    state.remove("zwtNotificationReportsMd")
    state.remove("zwtSensorMultilevelReportsMd")
    state.remove("zwtAssocGroupTarget")
    state.remove("zwtParamTarget")

    device?.updateSetting("zwtLoggingLevelIDE", null)
    device?.updateSetting("zwtAssocGroupsScanStart", null)
    device?.updateSetting("zwtAssocGroupsScanStop", null)
    device?.updateSetting("zwtEndpointsScanStart", null)
    device?.updateSetting("zwtEndpointsScanStop", null)
    device?.updateSetting("zwtParamsScanStart", null)
    device?.updateSetting("zwtParamsScanStop", null)
    device?.updateSetting("zwtAssocGroupId", null)
    device?.updateSetting("zwtAssocGroupMembers", null)
    device?.updateSetting("zwtAssocGroupCc", null)
    device?.updateSetting("zwtParamId", null)
    device?.updateSetting("zwtParamValue", null)
    device?.updateSetting("zwtProtectLocal", null)
    device?.updateSetting("zwtProtectRF", null)
    device?.updateSetting("zwtSwitchAllMode", null)

}

/*****************************************************************************************************************
 *  Private Helper Functions:
 *****************************************************************************************************************/

/**
 *  encapCommand(cmd)
 *
 *  Applies security or CRC16 encapsulation to a command as needed.
 *  Returns a physicalgraph.zwave.Command.
 **/
private encapCommand(physicalgraph.zwave.Command cmd) {
    if (state.useSecurity) {
        return zwave.securityV1.securityMessageEncapsulation().encapsulate(cmd)
    }
    else if (state.useCrc16) {
        return zwave.crc16EncapV1.crc16Encap().encapsulate(cmd)
    }
    else {
        return cmd
    }
}

/**
 *  prepCommands(cmds, delay=200)
 *
 *  Converts a list of commands (and delays) into a HubMultiAction object, suitable for returning via parse().
 *  Uses encapCommand() to apply security or CRC16 encapsulation as needed.
 **/
private prepCommands(cmds, delay=200) {
    return response(delayBetween(cmds.collect{ (it instanceof physicalgraph.zwave.Command ) ? encapCommand(it).format() : it },delay))
}

/**
 *  sendCommands(cmds, delay=200)
 *
 *  Sends a list of commands directly to the device using sendHubCommand.
 *  Uses encapCommand() to apply security or CRC16 encapsulation as needed.
 **/
private sendCommands(cmds, delay=200) {
    sendHubCommand( cmds.collect{ (it instanceof physicalgraph.zwave.Command ) ? response(encapCommand(it)) : response(it) }, delay)
}

/**
 *  logger()
 *
 *  Wrapper function for all logging. Simplified for this device handler.
 **/
private logger(msg, level = "debug") {

    switch(level) {
        case "error":
            if (state.loggingLevelIDE >= 1) log.error msg
            break

        case "warn":
            if (state.loggingLevelIDE >= 2) log.warn msg
            break

        case "info":
            if (state.loggingLevelIDE >= 3) log.info msg
            break

        case "debug":
            if (state.loggingLevelIDE >= 4) log.debug msg
            break

        case "trace":
            if (state.loggingLevelIDE >= 5) log.trace msg
            break

        default:
            log.debug msg
            break
    }
}

/**
 *  parseAssocGroupInput(string, maxNodes)
 *
 *  Converts a comma-delimited string of destinations (nodes and endpoints) into an array suitable for passing to
 *  multiChannelAssociationSet(). All numbers are interpreted as hexadecimal. Anything that's not a valid node or
 *  endpoint is discarded (warn). If the list has more than maxNodes, the extras are discarded (warn).
 *
 *  Example input strings:
 *    "9,A1"      = Nodes: 9 & 161 (no multi-channel endpoints)            => Output: [9, 161]
 *    "7,8:1,8:2" = Nodes: 7, Endpoints: Node8:endpoint1 & node8:endpoint2 => Output: [7, 0, 8, 1, 8, 2]
 */
private parseAssocGroupInput(string, maxNodes) {
    logger("parseAssocGroupInput(): Parsing Association Group Nodes: ${string}","trace")

    // First split into nodes and endpoints. Count valid entries as we go.
    if (string) {
        def nodeList = string.split(',')
        def nodes = []
        def endpoints = []
        def count = 0

        nodeList = nodeList.each { node ->
            node = node.trim()
            if ( count >= maxNodes) {
                logger("parseAssocGroupInput(): Number of nodes and endpoints is greater than ${maxNodes}! The following node was discarded: ${node}","warn")
            }
            else if (node.matches("\\p{XDigit}+")) { // There's only hexadecimal digits = nodeId
                def nodeId = Integer.parseInt(node,16)  // Parse as hex
                if ( (nodeId > 0) & (nodeId < 256) ) { // It's a valid nodeId
                    nodes << nodeId
                    count++
                }
                else {
                    logger("parseAssocGroupInput(): Invalid nodeId: ${node}","warn")
                }
            }
            else if (node.matches("\\p{XDigit}+:\\p{XDigit}+")) { // endpoint e.g. "0A:2"
                def endpoint = node.split(":")
                def nodeId = Integer.parseInt(endpoint[0],16) // Parse as hex
                def endpointId = Integer.parseInt(endpoint[1],16) // Parse as hex
                if ( (nodeId > 0) & (nodeId < 256) & (endpointId > 0) & (endpointId < 256) ) { // It's a valid endpoint
                    endpoints.addAll([nodeId,endpointId])
                    count++
                }
                else {
                    logger("parseAssocGroupInput(): Invalid endpoint: ${node}","warn")
                }
            }
            else {
                logger("parseAssocGroupInput(): Invalid nodeId: ${node}","warn")
            }
        }

        return (endpoints) ? nodes + [0] + endpoints : nodes
    }
    else {
        return []
    }
}

/**
 *  toCcNames()
 *
 *  Convert a list of integers to a list of Z-Wave Command Class Names.
 *
 *  incId  If true, will append the CC Id. E.g. "CC_NAME (0xAB)"
 **/
private toCcNames(input, incId = false) {

    def names = getCommandClassNames()

    if (input instanceof Collection) {
        def out  = []
        input.each { out.add( names.get(it, 'UNKNOWN') + ((incId) ? " (${toHexString(it,2,true)})" : "") ) }
        return out
    }
    else {
        return names.get(input, 'UNKNOWN') + ((incId) ? " (${toHexString(it,2,true)})" : "")
    }
}

/**
 *  toHexString()
 *
 *  Convert a list of integers to a list of hex strings.
 **/
private toHexString(input, size = 2, usePrefix = false) {

    def pattern = (usePrefix) ? "0x%0${size}X" : "%0${size}X"

    if (input instanceof Collection) {
        def hex  = []
        input.each { hex.add(String.format(pattern, it)) }
        return hex.toString()
    }
    else {
        return String.format(pattern, input)
    }
}

/**
 *  sync()
 *
 *  Manages synchronisation of association groups and parameters with the physical device.
 *  The syncPending attribute advertises remaining number of sync operations.
 **/
private sync() {
    logger("sync(): Syncing.","trace")

    def cmds = []
    def syncPending = 0

    // Association Group:
    if (state.zwtAssocGroupTarget != null) { // There's an association group to sync.

        // Check the device supports ASSOCIATION or MULTI_CHANNEL_ASSOCIATION, warn if it doesn't.
        if (!state.zwtGeneralMd?.commandClassIds.find( {it == 0x85 || it == 0x8E }) ) {
            logger("sync(): Device does not appear to support ASSOCIATION or MULTI_CHANNEL_ASSOCIATION command classes.","warn")
        }

        // Check AssocGroupMd for this group:
        def assocGroupMd = state.zwtAssocGroupsMd.find( { it.id == state.zwtAssocGroupTarget.id } )

        if (!assocGroupMd?.maxNodesSupported) { // Unknown Assocation Group. Request info. Sync will be resumed on receipt of an association report.
            logger("sync(): Unknown association group. Requesting more info.","info")
            cmds << zwave.multiChannelAssociationV2.multiChannelAssociationGet(groupingIdentifier: state.zwtAssocGroupTarget.id) // Try multi-channel first.
            cmds << zwave.associationV2.associationGet(groupingIdentifier: state.zwtAssocGroupTarget.id)
            cmds << zwave.associationGrpInfoV1.associationGroupNameGet(groupingIdentifier: state.zwtAssocGroupTarget.id)
        }
        else  {

            // Request additional information about the group if it's missing:
            if (!assocGroupMd.name) {
                logger("sync(): Requesting association group name.","info")
                cmds << zwave.associationGrpInfoV1.associationGroupNameGet([groupingIdentifier: state.zwtAssocGroupTarget.id])
            }

            // Determine whether to use multi-channel
            def useMultiChannel = false
            switch (state.zwtAssocGroupTarget.commandClass) {
                case 0: // Auto-detect:
                    if (assocGroupMd.multiChannel || state.zwtAssocGroupTarget.nodes.contains(0) ) {
                        useMultiChannel = true
                    }
                break

                case 1: // Force (Single-channel) Association:
                    useMultiChannel = false
                    if (state.zwtAssocGroupTarget.nodes.contains(0)) {
                        logger("sync(): Using (Single-channel) Association commands will not work with multi-channel endpoint destinations: ${toHexString(state.zwtAssocGroupTarget.nodes)}","warn")
                    }
                break

                case 2: // Force Multi-channel Association:
                    useMultiChannel = true
                break
            }

            if (useMultiChannel) {
                logger("sync(): Syncing Association Group #${state.zwtAssocGroupTarget.id} using Multi-Channel Association commands. New Destinations: ${toHexString(state.zwtAssocGroupTarget.nodes)}","info")
                cmds << zwave.multiChannelAssociationV2.multiChannelAssociationGet(groupingIdentifier: state.zwtAssocGroupTarget.id)
                cmds << zwave.multiChannelAssociationV2.multiChannelAssociationRemove(groupingIdentifier: state.zwtAssocGroupTarget.id, nodeId: []) // Remove All
                cmds << zwave.multiChannelAssociationV2.multiChannelAssociationSet(groupingIdentifier: state.zwtAssocGroupTarget.id, nodeId: state.zwtAssocGroupTarget.nodes)
                cmds << zwave.multiChannelAssociationV2.multiChannelAssociationGet(groupingIdentifier: state.zwtAssocGroupTarget.id)
            }
            else { // Use Association:
                logger("sync(): Syncing Association Group #${state.zwtAssocGroupTarget.id} using (Single-channel) Association commands. New Destinations: ${toHexString(state.zwtAssocGroupTarget.nodes)}","info")
                cmds << zwave.associationV2.associationGet(groupingIdentifier: state.zwtAssocGroupTarget.id) // Get original value:
                cmds << zwave.associationV2.associationRemove(groupingIdentifier: state.zwtAssocGroupTarget.id, nodeId: []) // Remove All
                cmds << zwave.associationV2.associationSet(groupingIdentifier: state.zwtAssocGroupTarget.id, nodeId: state.zwtAssocGroupTarget.nodes)
                cmds << zwave.associationV2.associationGet(groupingIdentifier: state.zwtAssocGroupTarget.id) // Get new value
             }
        }

        syncPending++
    }

    // Parameter:
    if (state.zwtParamTarget != null) { // There's a parameter to sync.

        // Check the device supports CONFIGURATION, warn if it doesn't.
        if (!state.zwtGeneralMd?.commandClassIds.find( {it == 0x70}) ) {
            logger("sync(): Device does not appear to support the CONFIGURATION command class.","warn")
        }

        // Check Meta-data for this parameter:
        def paramMd = state.zwtParamsMd.find( { it.id == state.zwtParamTarget.id } )

        if (!paramMd?.size) { // Unknown Parameter. Request a configurationReport. Sync will be resumed on receipt of a configuration report.
            logger("sync(): Unknown parameter. Requesting more info.","info")
            cmds << zwave.configurationV2.configurationGet(parameterNumber: state.zwtParamTarget.id)
        }
        else {
            // Check that target value is within size range.
            def unsignedMax = (256 ** paramMd.size) -1
            def signedMax = (256 ** paramMd.size)/2 -1
            def signedMin = -signedMax -1

            if (state.zwtParamTarget.scaledConfigurationValue > unsignedMax || state.zwtParamTarget.scaledConfigurationValue < signedMin) {
                logger("sync(): Target value for parameter #${state.zwtParamTarget.id} is out of range! " +
                "Parameter Size: ${paramMd.size}, Max Value: ${unsignedMax}, Min Value: ${signedMin}, New Value: ${state.zwtParamTarget.scaledConfigurationValue}","warn")
            }
            else {
                if (state.zwtParamTarget.scaledConfigurationValue > signedMax) {
                    def newTarget = state.zwtParamTarget.scaledConfigurationValue - (256 ** paramMd.size)
                    logger("sync(): Target value for parameter #${state.zwtParamTarget.id} is out of range for a signed value. " +
                    "Interpretting value as unsigned and converting from ${state.zwtParamTarget.scaledConfigurationValue} to ${newTarget}","warn")
                    state.zwtParamTarget.scaledConfigurationValue = newTarget
                }
            logger("sync(): Syncing parameter #${state.zwtParamTarget.id}: Size: ${paramMd.size}, New Value: ${state.zwtParamTarget.scaledConfigurationValue}","info")
            cmds << zwave.configurationV2.configurationGet(parameterNumber: state.zwtParamTarget.id) // Get current value.
            cmds << zwave.configurationV2.configurationSet(parameterNumber: state.zwtParamTarget.id, size: paramMd.size, scaledConfigurationValue: state.zwtParamTarget.scaledConfigurationValue)
            cmds << zwave.configurationV2.configurationGet(parameterNumber: state.zwtParamTarget.id) // Confirm new value.
            }
        }
        syncPending++
    }

    // Protection:
    def protectLocalTarget = (settings.zwtProtectLocal != null) ? settings.zwtProtectLocal.toInteger() : (state.zwtGeneralMd?.protectionLocalId ?:0)
    def protectRFTarget    = (settings.zwtProtectRF != null) ? settings.zwtProtectRF.toInteger() : (state.zwtGeneralMd?.protectionRFId ?:0)
    if (settings.zwtProtectLocal != null || settings.zwtProtectRF != null) {
        logger("sync(): Syncing Protection Mode: Local: ${protectLocalTarget}, RF: ${protectRFTarget}","info")
        // Check the device supports Protection, warn if it doesn't.
        if (!state.zwtGeneralMd?.commandClassIds.find( {it == 0x75}) ) {
            logger("sync(): Device does not appear to support the PROTECTION command class.","warn")
        }
        // Send the commands, regardless:
        cmds << zwave.protectionV2.protectionSet(localProtectionState : protectLocalTarget, rfProtectionState: protectRFTarget)
        cmds << zwave.protectionV2.protectionGet()
        syncPending++
    }

    // Switch_All:
    if (settings.zwtSwitchAllMode  != null) {
        logger("sync(): Syncing Switch_All Mode: ${settings.zwtSwitchAllMode}","info")
        // Check the device supports SWITCH_ALL, warn if it doesn't.
        if (!state.zwtGeneralMd?.commandClassIds.find( {it == 0x27}) ) {
            logger("sync(): Device does not appear to support the SWITCH_ALL command class.","warn")
        }
        // Send the commands, regardless:
        cmds << zwave.switchAllV1.switchAllSet(mode: settings.zwtSwitchAllMode.toInteger())
        cmds << zwave.switchAllV1.switchAllGet()
        syncPending++
    }

    sendEvent(name: "syncPending", value: syncPending, displayed: false)
    sendCommands(cmds,800)
}

/**
 *  updateSyncPending()
 *
 *  Updates syncPending attribute, which advertises remaining number of sync operations.
 **/
private updateSyncPending() {

    def syncPending = 0

    if (state.zwtAssocGroupTarget  != null) { // There's an association group target to sync.
        def cachedNodes = state.zwtAssocGroupsMd.find( { it.id == state.zwtAssocGroupTarget.id } )?.nodes
        def targetNodes = state.zwtAssocGroupTarget.nodes

        if ( cachedNodes != targetNodes ) {
            syncPending++
        }
    }

    if (state.zwtParamTarget  != null) { // There's a parameter to sync.
        if ( state.zwtParamsMd?.find( { it.id == state.zwtParamTarget.id } )?.scaledConfigurationValue != state.zwtParamTarget.scaledConfigurationValue) {
            syncPending++
        }
    }

    // Protection:
    def protectLocalTarget = (settings.zwtProtectLocal != null) ? settings.zwtProtectLocal.toInteger() : state.zwtGeneralMd?.protectionLocalId
    def protectRFTarget = (settings.zwtProtectRF != null) ? settings.zwtProtectRF.toInteger() : state.zwtGeneralMd?.protectionRFId
    if (state.zwtGeneralMd?.protectionLocalId != protectLocalTarget || state.zwtGeneralMd?.protectionRFId != protectRFTarget) {
        syncPending++
    }

    // Switch_All:
    if ( (settings.zwtSwitchAllMode != null) & (state.zwtGeneralMd.switchAllModeId != settings.zwtSwitchAllMode?.toInteger()) ) {
        syncPending++
    }

    logger("updateSyncPending(): syncPending: ${syncPending}", "debug")
    if ((syncPending == 0) & (device.latestValue("syncPending") > 0)) logger("Sync Complete.", "info")
    sendEvent(name: "syncPending", value: syncPending, displayed: false)
}

/**
 *  cacheCommandMd()
 *
 *  Caches command meta-data.
 *  Translates commandClassId to a name, however commandId is not translated (the lookup would be too much code).
 **/
private cacheCommandMd(cmd, description = "", sourceEndpoint = "", destinationEndpoint = "") {

    // Update commands meta-data cache:
    if (state.zwtCommandsMd?.find( { it.commandClassId == cmd.commandClassId & it.commandId == cmd.commandId } )) { // Known command type.
        state.zwtCommandsMd?.collect {
            if (it.commandClassId == cmd.commandClassId & it.commandId == cmd.commandId) {
                it.description = description
                it.parsedCmd = cmd.toString()
                if (sourceEndpoint) {it.sourceEndpoint = sourceEndpoint}
                if (destinationEndpoint) {it.destinationEndpoint = destinationEndpoint}
            }
        }
    }
    else { // New command type:
        logger("zwaveEvent(): New command type discovered.","debug")
        def commandMd = [
            commandClassId: cmd.commandClassId,
            commandClassName: toCcNames(cmd.commandClassId.toInteger()),
            commandId: cmd.commandId,
            description: description,
            parsedCmd: cmd.toString()
        ]
        if (sourceEndpoint) {commandMd.sourceEndpoint = sourceEndpoint}
        if (destinationEndpoint) {commandMd.destinationEndpoint = destinationEndpoint}

        state.zwtCommandsMd << commandMd
    }

}

/*****************************************************************************************************************
 *  Static Matadata Functions:
 *****************************************************************************************************************/

/**
 *  getCommandClassVersions()
 *
 *  Returns a map of the command class versions supported by the device. Used by parse() and zwaveEvent() to
 *  extract encapsulated commands from MultiChannelCmdEncap, MultiInstanceCmdEncap, SecurityMessageEncapsulation,
 *  and Crc16Encap messages.
 **/
private getCommandClassVersions() {
    return [
        0x20: 1, // Basic V1
        0x25: 1, // Switch Binary V1
        0x26: 2, // Switch Multilvel V2
        0x27: 1, // Switch All V1
        0x2B: 1, // Scene Activation V1
        0x30: 2, // Sensor Binary V2
        0x31: 5, // Sensor Multilevel V5
        0x32: 3, // Meter V3
        0x33: 3, // Switch Color V3
        0x56: 1, // CRC16 Encapsulation V1
        0x59: 1, // Association Grp Info
        0x60: 3, // Multi Channel V3
        0x62: 1, // Door Lock V1
        0x70: 2, // Configuration V2
        0x71: 1, // Alarm (Notification) V1
        0x72: 2, // Manufacturer Specific V2
        0x73: 1, // Powerlevel V1
        0x75: 2, // Protection V2
        0x76: 1, // Lock V1
        0x84: 1, // Wake Up V1
        0x85: 2, // Association V2
        0x86: 1, // Version V1
        0x8E: 2, // Multi Channel Association V2
        0x87: 1, // Indicator V1
        0x98: 1  // Security V1
   ]
}

/**
 *  getCommandClassNames()
 *
 *  Returns a map of command class names. Used by toCcNames().
 **/
private getCommandClassNames() {
    return [
        0x00: 'NO_OPERATION',
        0x20: 'BASIC',
        0x21: 'CONTROLLER_REPLICATION',
        0x22: 'APPLICATION_STATUS',
        0x23: 'ZIP',
        0x24: 'SECURITY_PANEL_MODE',
        0x25: 'SWITCH_BINARY',
        0x26: 'SWITCH_MULTILEVEL',
        0x27: 'SWITCH_ALL',
        0x28: 'SWITCH_TOGGLE_BINARY',
        0x29: 'SWITCH_TOGGLE_MULTILEVEL',
        0x2A: 'CHIMNEY_FAN',
        0x2B: 'SCENE_ACTIVATION',
        0x2C: 'SCENE_ACTUATOR_CONF',
        0x2D: 'SCENE_CONTROLLER_CONF',
        0x2E: 'SECURITY_PANEL_ZONE',
        0x2F: 'SECURITY_PANEL_ZONE_SENSOR',
        0x30: 'SENSOR_BINARY',
        0x31: 'SENSOR_MULTILEVEL',
        0x32: 'METER',
        0x33: 'SWITCH_COLOR',
        0x34: 'NETWORK_MANAGEMENT_INCLUSION',
        0x35: 'METER_PULSE',
        0x36: 'BASIC_TARIFF_INFO',
        0x37: 'HRV_STATUS',
        0x38: 'THERMOSTAT_HEATING',
        0x39: 'HRV_CONTROL',
        0x3A: 'DCP_CONFIG',
        0x3B: 'DCP_MONITOR',
        0x3C: 'METER_TBL_CONFIG',
        0x3D: 'METER_TBL_MONITOR',
        0x3E: 'METER_TBL_PUSH',
        0x3F: 'PREPAYMENT',
        0x40: 'THERMOSTAT_MODE',
        0x41: 'PREPAYMENT_ENCAPSULATION',
        0x42: 'THERMOSTAT_OPERATING_STATE',
        0x43: 'THERMOSTAT_SETPOINT',
        0x44: 'THERMOSTAT_FAN_MODE',
        0x45: 'THERMOSTAT_FAN_STATE',
        0x46: 'CLIMATE_CONTROL_SCHEDULE',
        0x47: 'THERMOSTAT_SETBACK',
        0x48: 'RATE_TBL_CONFIG',
        0x49: 'RATE_TBL_MONITOR',
        0x4A: 'TARIFF_CONFIG',
        0x4B: 'TARIFF_TBL_MONITOR',
        0x4C: 'DOOR_LOCK_LOGGING',
        0x4D: 'NETWORK_MANAGEMENT_BASIC',
        0x4E: 'SCHEDULE_ENTRY_LOCK',
        0x4F: 'ZIP_6LOWPAN',
        0x50: 'BASIC_WINDOW_COVERING',
        0x51: 'MTP_WINDOW_COVERING',
        0x52: 'NETWORK_MANAGEMENT_PROXY',
        0x53: 'SCHEDULE',
        0x54: 'NETWORK_MANAGEMENT_PRIMARY',
        0x55: 'TRANSPORT_SERVICE',
        0x56: 'CRC_16_ENCAP',
        0x57: 'APPLICATION_CAPABILITY',
        0x58: 'ZIP_ND',
        0x59: 'ASSOCIATION_GRP_INFO',
        0x5A: 'DEVICE_RESET_LOCALLY',
        0x5B: 'CENTRAL_SCENE',
        0x5C: 'IP_ASSOCIATION',
        0x5D: 'ANTITHEFT',
        0x5E: 'ZWAVEPLUS_INFO',
        0x5F: 'ZIP_GATEWAY',
        0x60: 'MULTI_CHANNEL',
        0x61: 'ZIP_PORTAL',
        0x62: 'DOOR_LOCK',
        0x63: 'USER_CODE',
        0x64: 'HUMIDITY_CONTROL_SETPOINT',
        0x65: 'DMX',
        0x66: 'BARRIER_OPERATOR',
        0x67: 'NETWORK_MANAGEMENT_INSTALLATION_MAINTENANCE',
        0x68: 'ZIP_NAMING',
        0x69: 'MAILBOX',
        0x6A: 'WINDOW_COVERING',
        0x6B: 'IRRIGATION',
        0x6C: 'SUPERVISION',
        0x6D: 'HUMIDITY_CONTROL_MODE',
        0x6E: 'HUMIDITY_CONTROL_OPERATING_STATE',
        0x6F: 'ENTRY_CONTROL',
        0x70: 'CONFIGURATION',
        0x71: 'NOTIFICATION',
        0x72: 'MANUFACTURER_SPECIFIC',
        0x73: 'POWERLEVEL',
        0x74: 'INCLUSION_CONTROLLER',
        0x75: 'PROTECTION',
        0x76: 'LOCK',
        0x77: 'NODE_NAMING',
        0x7A: 'FIRMWARE_UPDATE_MD',
        0x7B: 'GROUPING_NAME',
        0x7C: 'REMOTE_ASSOCIATION_ACTIVATE',
        0x7D: 'REMOTE_ASSOCIATION',
        0x80: 'BATTERY',
        0x81: 'CLOCK',
        0x82: 'HAIL',
        0x84: 'WAKE_UP',
        0x85: 'ASSOCIATION',
        0x86: 'VERSION',
        0x87: 'INDICATOR',
        0x88: 'PROPRIETARY',
        0x89: 'LANGUAGE',
        0x8A: 'TIME',
        0x8B: 'TIME_PARAMETERS',
        0x8C: 'GEOGRAPHIC_LOCATION',
        0x8E: 'MULTI_CHANNEL_ASSOCIATION',
        0x8F: 'MULTI_CMD',
        0x90: 'ENERGY_PRODUCTION',
        0x91: 'MANUFACTURER_PROPRIETARY',
        0x92: 'SCREEN_MD',
        0x93: 'SCREEN_ATTRIBUTES',
        0x94: 'SIMPLE_AV_CONTROL',
        0x95: 'AV_CONTENT_DIRECTORY_MD',
        0x96: 'AV_RENDERER_STATUS',
        0x97: 'AV_CONTENT_SEARCH_MD',
        0x98: 'SECURITY',
        0x99: 'AV_TAGGING_MD',
        0x9A: 'IP_CONFIGURATION',
        0x9B: 'ASSOCIATION_COMMAND_CONFIGURATION',
        0x9C: 'SENSOR_ALARM',
        0x9D: 'SILENCE_ALARM',
        0x9E: 'SENSOR_CONFIGURATION',
        0x9F: 'SECURITY_2',
        0xEF: 'MARK',
        0xF0: 'NON_INTEROPERABLE'
    ]
}