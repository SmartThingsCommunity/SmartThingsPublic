/* 
 *	Aeon Labs Smart Energy Switch - DSC06
 *
 *  Copyright 2015 Elastic Development
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
 *  The latest version of this file can be found at:
 *  https://github.com/jpansarasa/SmartThings/blob/master/DeviceTypes/AeotecHDSS.groovy
 *
 *  Revision History
 *  ----------------
 *
 *  2015-01-21: Version: 1.0.0
 *  Initial Revision
 *
 *  2015-01-24: Version: 1.1.0
 *  Added updated() method
 *  Moved to boolean values in state rather than call toBoolean() on preference
 *  Cleaner logic for on() and off()
 *
 *  2015-01-27: Version: 1.2.0
 *  Added preference to turn on/off displaying of power events in the activity log
 *
 *  Developer's Notes
 *  Raw Description	0 0 0x1001 0 0 0 a 0x25 0x31 0x32 0x27 0x70 0x85 0x72 0x86 0xEF 0x82
 *  Z-Wave Supported Command Classes:
 *  Code Name					Version
 *  ==== ======================================	=======
 *  0x25 COMMAND_CLASS_SWITCH_BINARY		V1
 *  0x31 COMMAND_CLASS_SENSOR_MULTILEVEL	V5
 *  0x32 COMMAND_CLASS_METER			V3
 *  0x70 COMMAND_CLASS_CONFIGURATION		V1
 *  ---- ---- Supported but unimplemented -----	--
 *  0x27 COMMAND_CLASS_SWITCH_ALL		V1
 *  0x85 COMMAND_CLASS_ASSOCIATION		V2
 *  0x72 COMMAND_CLASS_MANUFACTURER_SPECIFIC	V2
 *  0x86 COMMAND_CLASS_VERSION			V2
 *  0xEF COMMAND_CLASS_MARK			V1
 *  0x82 COMMAND_CLASS_HAIL			V1
 */
metadata {
    definition (name: "Aeon Labs Smart Energy Switch", namespace: "elasticdev", author: "James P") {
	capability "Switch"
	capability "Energy Meter"
	capability "Power Meter"
	capability "Configuration"
	capability "Sensor"
	capability "Actuator"
	capability "Polling"
	capability "Refresh"

	command "reset"

	fingerprint deviceId: "0x1001", inClusters: "0x25, 0x31, 0x32, 0x27, 0x70, 0x85, 0x72, 0x86, 0xEF, 0x82"
    }

    // simulator metadata
    simulator {
	status "on":  "command: 2003, payload: FF"
	status "off": "command: 2003, payload: 00"

	for (int i = 0; i <= 10000; i += 1000) {
	    status "power  ${i} W": 
		    new physicalgraph.zwave.Zwave().meterV1.meterReport(scaledMeterValue: i, precision: 3, meterType: 4, scale: 2, size: 4).incomingMessage()
	}
	for (int i = 0; i <= 100; i += 10) {
	    status "energy  ${i} kWh":
		    new physicalgraph.zwave.Zwave().meterV1.meterReport(scaledMeterValue: i, precision: 3, meterType: 0, scale: 0, size: 4).incomingMessage()
	}

	// reply messages
	reply "2001FF,delay 100,2502": "command: 2503, payload: FF"
	reply "200100,delay 100,2502": "command: 2503, payload: 00"
    }

    // tile definitions
    tiles {
	standardTile("switch", "device.switch", width: 2, height: 2, canChangeIcon: true) {
	    state "on", label: '${name}', action: "switch.off", icon: "st.switches.switch.on", backgroundColor: "#79b821"
	    state "off", label: '${name}', action: "switch.on", icon: "st.switches.switch.off", backgroundColor: "#ffffff"
	}
	valueTile("power", "device.power", decoration: "flat") {
	    state "default", label:'${currentValue} W'
	}
	valueTile("energy", "device.energy", decoration: "flat") {
	    state "default", label:'${currentValue} kWh'
	}
	standardTile("reset", "device.energy", inactiveLabel: false, decoration: "flat") {
	    state "default", label:'reset kWh', action:"reset", icon:"st.secondary.refresh-icon"
	}
	standardTile("configure", "device.power", inactiveLabel: false, decoration: "flat") {
	    state "configure", label:'', action:"configuration.configure", icon:"st.secondary.configure"
	}
	standardTile("refresh", "device.power", inactiveLabel: false, decoration: "flat") {
	    state "default", label:'', action:"refresh.refresh", icon:"st.secondary.refresh"
	}

	main (["switch","energy","power"])
	details(["switch","power","energy","reset","configure","refresh"])
    }

    preferences {
        input "disableOnOff", "boolean", 
                title: "Disable On/Off?", 
                defaultValue: false, 
                displayDuringSetup: true
        input "reportInterval", "number", 
                title: "Report Interval", 
                description: "The time interval in minutes for sending device reports", 
                defaultValue: 1, 
                required: false, 
                displayDuringSetup: true
	input "displayEvents", "boolean", 
		title: "Display power events in the Activity log? ", 
		defaultValue: true, 
		displayDuringSetup: true
        input "switchAll", "enum", 
                title: "Respond to switch all?", 
                description: "How does the switch respond to the 'Switch All' command", 
                options:["Disabled", "Off Enabled", "On Enabled", "On And Off Enabled"], 
                defaultValue: "On And Off Enabled", 
                required:false, 
                displayDuringSetup: true
        input "debugOutput", "boolean", 
                title: "Enable debug logging?", 
                defaultValue: false, 
                displayDuringSetup: true
    }
}

/********************************************************************************
 *	Methods																		*
 ********************************************************************************/

/**
 *  updated - Called when the preferences of the device type are changed
 */
def updated() {
    state.onOffDisabled = ("true" == disableOnOff)
    state.display = ("true" == displayEvents)
    state.debug = ("true" == debugOutput)
    if (state.debug) log.debug "updated(disableOnOff: ${disableOnOff}(${state.onOffDisabled}), reportInterval: ${reportInterval}, displayEvents: ${displayEvents}, switchAll: ${switchAll}, debugOutput: ${debugOutput}(${state.debug}))"
    response(configure())
}

/**
 *  parse - Called when messages from a device are received from the hub
 *
 *  The parse method is responsible for interpreting those messages and returning Event definitions.
 *
 *  String	description		The message from the device
 */
def parse(String description) {
    if (state.debug) log.debug "Parse(description: \"${description}\")"

    def event = null

    // The first parameter is the description string
    // The second parameter is a map that specifies the version of each command to use
    def cmd = zwave.parse(description, [0x20 : 1, 0x25 : 1, 0x31 : 5, 0x32 : 3, 0x27 : 1, 0x70 : 1, 0x85 : 2, 0x72 : 2, 0x86 : 2, 0x82 : 1])

    if (cmd) {
        event = createEvent(zwaveEvent(cmd))
    }
    if (state.debug) log.debug "Parse returned ${event?.inspect()}"
    return event
}

/**
 *  on - Turns on the switch
 *
 *  Required for the "Switch" capability
 */
def on() {
    if (state.onOffDisabled) {
        if (state.debug) log.debug "On/Off disabled"
        delayBetween([
	    zwave.basicV1.basicGet().format(),
    	    zwave.switchBinaryV1.switchBinaryGet().format()
        ], 5)
    }
    else {
        delayBetween([
	    zwave.basicV1.basicSet(value: 0xFF).format(),
            zwave.switchBinaryV1.switchBinaryGet().format()
	])
    }
}

/**
 *  off - Turns off the switch
 *
 *  Required for the "Switch" capability
 */
def off() {
    if (state.onOffDisabled) {
        if (state.debug) log.debug "On/Off disabled"
        delayBetween([
	    zwave.basicV1.basicGet().format(),
    	    zwave.switchBinaryV1.switchBinaryGet().format()
        ], 5)
    }
    else {
        delayBetween([
	    zwave.basicV1.basicSet(value: 0x00).format(),
            zwave.switchBinaryV1.switchBinaryGet().format()
	])
    }
}

/**
 *  poll - Polls the device
 *
 *  Required for the "Polling" capability
 */
def poll() {
    delayBetween([
	zwave.basicV1.basicGet().format(),
	zwave.switchBinaryV1.switchBinaryGet().format(),
	zwave.meterV3.meterGet(scale: 0).format(),	//kWh
	zwave.meterV3.meterGet(scale: 2).format()	//Wattage
    ])
}

/**
 *  refresh - Refreshed values from the device
 *
 *  Required for the "Refresh" capability
 */
def refresh() {
    delayBetween([
	zwave.basicV1.basicGet().format(),
	zwave.switchBinaryV1.switchBinaryGet().format(),
	zwave.sensorMultilevelV5.sensorMultilevelGet(sensorType: 4, scale: 0).format(),
	zwave.meterV3.meterGet(scale: 0).format(),	//kWh
	zwave.meterV3.meterGet(scale: 2).format()	//Wattage
    ])
}

/**
 *  reset - Resets the devices energy usage meter
 *
 *  Defined by the custom command "reset"
 */
def reset() {
    return [
	zwave.meterV3.meterReset().format(),
	zwave.meterV3.meterGet(scale: 0).format() //kWh
    ]
}

/**
 *  configure - Configures the parameters of the device
 *
 *  Required for the "Configuration" capability
 */
def configure() {
    //Get the values from the preferences section
    def reportIntervalSecs = 60;
    if (reportInterval) {
	reportIntervalSecs = 60 * reportInterval.toInteger()
    }
	
    def switchAllMode = physicalgraph.zwave.commands.switchallv1.SwitchAllSet.MODE_INCLUDED_IN_THE_ALL_ON_ALL_OFF_FUNCTIONALITY
    if (switchAll == "Disabled") {
	switchAllMode = physicalgraph.zwave.commands.switchallv1.SwitchAllSet.MODE_EXCLUDED_FROM_THE_ALL_ON_ALL_OFF_FUNCTIONALITY
    }
    else if (switchAll == "Off Enabled") {
	switchAllMode = physicalgraph.zwave.commands.switchallv1.SwitchAllSet.MODE_EXCLUDED_FROM_THE_ALL_ON_FUNCTIONALITY_BUT_NOT_ALL_OFF
    }
    else if (switchAll == "On Enabled") {
	switchAllMode = physicalgraph.zwave.commands.switchallv1.SwitchAllSet.MODE_EXCLUDED_FROM_THE_ALL_OFF_FUNCTIONALITY_BUT_NOT_ALL_ON
    }

	if (state.debug) log.debug "configure(reportIntervalSecs: ${reportIntervalSecs}, switchAllMode: ${switchAllMode})"
	
    /***************************************************************
    Device specific configuration parameters
    ----------------------------------------------------------------
    Param   Size    Default Description
    ------- ------- ------- ----------------------------------------
    0x01    1       0       The content of "Multilevel Sensor Report Command" after SE receives "Multilevel Sensor Get Command".
    0x02    1       N/A     Make SE blink
    0x50    1       0       Enable to send notifications to associated devices in Group 1 when load changes (0=nothing, 1=hail CC, 2=basic CC report)
    0x5A    1       0       Enables/disables parameter 0x5A and 0x5B below
    0x5B    2       50      The value here represents minimum change in wattage (in terms of wattage) for a REPORT to be sent (default 50W, size 2 bytes).
    0x5C    1       10      The value here represents minimum change in wattage (in terms of percentage) for a REPORT to be sent (default 10%, size 1 byte).
    0x64    1       N/A     Set 0x65-0x67 to default
    0x65    4       8       Which reports need to send in Report group 1
    0x66    4       0       Which reports need to send in Report group 2
    0x67    4       0       Which reports need to send in Report group 3
    0x6E    1       N/A     Set 0x6F-0x71 to default.
    0x6F    4       600     The time interval in seconds for sending Report group 1 (Valid values 0x01‐0x7FFFFFFF).
    0x70    4       600     The time interval in seconds for sending Report group 2 (Valid values 0x01‐0x7FFFFFFF).
    0x71    4       600     The time interval in seconds for sending Report group 3 (Valid values 0x01‐0x7FFFFFFF).
    0xFE    2       0       Device Tag
    0xFF    1       N/A     Reset to factory default setting
    
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
	zwave.switchAllV1.switchAllSet(mode: switchAllMode).format(),
	zwave.configurationV1.configurationSet(parameterNumber: 0x50, size: 1, scaledConfigurationValue: 2).format(),	//Enable to send notifications to associated devices when load changes (0=nothing, 1=hail CC, 2=basic CC report)
	zwave.configurationV1.configurationSet(parameterNumber: 0x5A, size: 1, scaledConfigurationValue: 1).format(),	//Enables parameter 0x5B and 0x5C (0=disabled, 1=enabled)
	zwave.configurationV1.configurationSet(parameterNumber: 0x5B, size: 2, scaledConfigurationValue: 5).format(),	//Minimum change in wattage for a REPORT to be sent (Valid values 0 - 60000)
	zwave.configurationV1.configurationSet(parameterNumber: 0x5C, size: 1, scaledConfigurationValue: 5).format(),	//Minimum change in percentage for a REPORT to be sent (Valid values 0 - 100)
	zwave.configurationV1.configurationSet(parameterNumber: 0x65, size: 4, scaledConfigurationValue: 14).format(),	//Which reports need to send in Report group 1
	zwave.configurationV1.configurationSet(parameterNumber: 0x6F, size: 4, scaledConfigurationValue: reportIntervalSecs).format(),	//Send Report to group 1 for this interval (Valid values 0x01‐0x7FFFFFFF)
    ])
}

/********************************************************************************
 *	Event Handlers																*
 ********************************************************************************/

/**
 *   Default event handler -  Called for all unhandled events
 */
def zwaveEvent(physicalgraph.zwave.Command cmd) {
    if (state.debug) {
        log.debug "Unhandled: $cmd"
        createEvent(descriptionText: "${device.displayName}: ${cmd}")
    }
    else {
        [:]
    }
}

/**
 *  COMMAND_CLASS_BASIC (0x20)
 *
 *  Short	value	0xFF for on, 0x00 for off
 */
def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicSet cmd) 
{
    if (state.debug) log.debug "BasicSet(value:${cmd.value})"
    createEvent(name: "switch", value: cmd.value ? "on" : "off", type: "physical", displayed: true, isStateChange: true)
}

/**
 *  COMMAND_CLASS_BASIC (0x20)
 *
 *  Short	value	0xFF for on, 0x00 for off
 */
def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicReport cmd)
{
    if (state.debug) log.debug "BasicReport(value:${cmd.value})"
    createEvent(name: "switch", value: cmd.value ? "on" : "off", type: "physical")
}

/**
 *  COMMAND_CLASS_SWITCH_BINARY (0x25)
 *
 *  Short	value	0xFF for on, 0x00 for off
 */
def zwaveEvent(physicalgraph.zwave.commands.switchbinaryv1.SwitchBinarySet cmd)
{
    if (state.debug) log.debug "SwitchBinarySet(value:${cmd.value})"
    createEvent(name: "switch", value: cmd.value ? "on" : "off", type: "digital", displayed: true, isStateChange: true)
}

/**
 *  COMMAND_CLASS_SWITCH_BINARY (0x25)
 *
 *  Short	value	0xFF for on, 0x00 for off
 */
def zwaveEvent(physicalgraph.zwave.commands.switchbinaryv1.SwitchBinaryReport cmd)
{
    if (state.debug) log.debug "SwitchBinaryReport(value:${cmd.value})"
    createEvent(name: "switch", value: cmd.value ? "on" : "off", type: "digital")
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
    def meterTypes = ["Unknown", "Electric", "Gas", "Water"]
    def electricNames = ["energy", "energy", "power", "count",  "voltage", "current", "powerFactor",  "unknown"]
    def electricUnits = ["kWh",    "kVAh",   "W",     "pulses", "V",       "A",       "Power Factor", ""]

    if (state.debug) log.debug "MeterReport(deltaTime:${cmd.deltaTime} secs, meterType:${meterTypes[cmd.meterType]}, meterValue:${cmd.scaledMeterValue}, previousMeterValue:${cmd.scaledPreviousMeterValue}, scale:${electricNames[cmd.scale]}(${cmd.scale}), precision:${cmd.precision}, rateType:${cmd.rateType})"

    //NOTE ScaledPreviousMeterValue does not always contain a value
    def previousValue = cmd.scaledPreviousMeterValue ?: 0

    def map = [ name: electricNames[cmd.scale], unit: electricUnits[cmd.scale], displayed: state.display]
    switch(cmd.scale) {
        case 0: //kWh
	    previousValue = device.currentValue("energy") ?: cmd.scaledPreviousMeterValue ?: 0
            map.value = cmd.scaledMeterValue
            break;
        case 1: //kVAh
            map.value = cmd.scaledMeterValue
            break;
        case 2: //Watts
            previousValue = device.currentValue("power") ?: cmd.scaledPreviousMeterValue ?: 0
            map.value = Math.round(cmd.scaledMeterValue)
            break;
        case 3: //pulses
            map.value = Math.round(cmd.scaledMeterValue)
            break;
        case 4: //Volts
            previousValue = device.currentValue("voltage") ?: cmd.scaledPreviousMeterValue ?: 0
            map.value = cmd.scaledMeterValue
            break;
        case 5: //Amps
            previousValue = device.currentValue("current") ?: cmd.scaledPreviousMeterValue ?: 0
            map.value = cmd.scaledMeterValue
            break;
        case 6: //Power Factor
        case 7: //Unknown
            map.value = cmd.scaledMeterValue
            break;
        default:
            break;
    }
    //Check if the value has changed my more than 5%, if so mark as a stateChange
    map.isStateChange = ((cmd.scaledMeterValue - previousValue).abs() > (cmd.scaledMeterValue * 0.05))

    createEvent(map)
}

/**
 *  COMMAND_CLASS_SENSOR_MULTILEVEL  (0x31)
 *	
 *  Short	sensorType	Supported Sensor: 0x04 (power Sensor)
 *  Short	scale		Supported scale:  0x00 (W) and 0x01 (BTU/h)   
 */
def zwaveEvent(physicalgraph.zwave.commands.sensormultilevelv5.SensorMultilevelReport cmd)
{
    if (state.debug) log.debug "SensorMultilevelReport(sensorType:${cmd.sensorType}, scale:${cmd.scale}, precision:${cmd.precision}, scaledSensorValue:${cmd.scaledSensorValue}, sensorValue:${cmd.sensorValue}, size:${cmd.size})"

    def map = [ value: cmd.scaledSensorValue, displayed: false]
    switch(cmd.sensorType) {
        case physicalgraph.zwave.commands.sensormultilevelv5.SensorMultilevelReport.SENSOR_TYPE_POWER_VERSION_2: 	// 4
            map.name = "power"
            map.unit = cmd.scale ? "BTU/h" : "W"
            map.value = Math.round(cmd.scaledSensorValue)
            break;
        case physicalgraph.zwave.commands.sensormultilevelv5.SensorMultilevelReport.SENSOR_TYPE_VOLTAGE_VERSION_3:	// 15
            map.name = "voltage"
            map.unit = cmd.scale ? "mV" : "V"
            break;
        case physicalgraph.zwave.commands.sensormultilevelv5.SensorMultilevelReport.SENSOR_TYPE_CURRENT_VERSION_3:	// 16
            map.name = "current"
            map.unit = cmd.scale ? "mA" : "A"
            break;
        default:
            map.name = "unknown sensor ($cmd.sensorType)"
            break;
    }
    createEvent(map)
}
//EOF