/**
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
 *  Z-Wave Metering Dimmer
 *
 *  Copyright 2014 SmartThings
 * 
 * modified by lg kahn 2015-09 version 2
 * modified to support the modes of the aeon smart switch 6, night light, 
 * brief and default (light follows the power usage)
 * also added alternate dimmer slider control, and factory reset and attempted to add 
 * 
 * also added voltage report, and configure to set all this
 * also now reports amperage as well.Contray to the documentation amperage is
 * coming accross in scale 5.
 * also change default timeout for reporting from 10 minutes to 3 minutes.
 * for apis see http://www.pepper1.net/zwavedb/device/722
 * got color working problem was you cannot have anything after the setconfig line in the color fx
 * also got rgb slider working. Note it only controls brightness in energy or momentary mode, not night light mode.
 * this is by design (for some reason but not sure why they did it.. see above document).
 * so now everything appears to be working.
 * version 2.1 work around changes in the new firmware that was stopping it from turning off. 
*/
metadata {
	definition (name: "LGK Aeon Dimming Metering Smart Switch 6 V2", namespace: "smartthings", author: "lg kahn") {
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
       command "setColor"
       
	   command "reset"
       command "mySetLevel"
       command "factoryReset"
       attribute "deviceMode", "String"
  
	   fingerprint inClusters: "0x26,0x32"
	}

	simulator {
		status "on":  "command: 2603, payload: FF"
		status "off": "command: 2603, payload: 00"
		status "09%": "command: 2603, payload: 09"
		status "10%": "command: 2603, payload: 0A"
		status "33%": "command: 2603, payload: 21"
		status "66%": "command: 2603, payload: 42"
		status "99%": "command: 2603, payload: 63"

		for (int i = 0; i <= 10000; i += 1000) {
			status "power  ${i} W": new physicalgraph.zwave.Zwave().meterV1.meterReport(
				scaledMeterValue: i, precision: 3, meterType: 4, scale: 2, size: 4).incomingMessage()
		}
		for (int i = 0; i <= 100; i += 10) {
			status "energy  ${i} kWh": new physicalgraph.zwave.Zwave().meterV1.meterReport(
				scaledMeterValue: i, precision: 3, meterType: 0, scale: 0, size: 4).incomingMessage()
		}

		["FF", "00", "09", "0A", "21", "42", "63"].each { val ->
			reply "2001$val,delay 100,2602": "command: 2603, payload: $val"
		}
	}

	tiles {
		standardTile("switch", "device.switch", width: 2, height: 2, canChangeIcon: true) {
			state "on", label:'${name}', action:"switch.off", icon:"st.switches.switch.on", backgroundColor:"#79b821", nextState:"turningOff"
			state "off", label:'${name}', action:"switch.on", icon:"st.switches.switch.off", backgroundColor:"#ffffff", nextState:"turningOn"
			state "turningOn", label:'${name}', icon:"st.switches.switch.on", backgroundColor:"#79b821"
			state "turningOff", label:'${name}', icon:"st.switches.switch.off", backgroundColor:"#ffffff"
		}
		valueTile("power", "device.power") {
			state "default", label:'${currentValue} W'
		}
		valueTile("energy", "device.energy") {
			state "default", label:'${currentValue} kWh'
		}
		standardTile("reset", "device.energy", inactiveLabel: false, decoration: "flat") {
			state "default", label:'reset', action:"reset"
		}
		controlTile("levelSliderControl", "device.level", "slider", height: 1, width: 3, inactiveLabel: false) {
			state "level", action:"switch level.setLevel"
		}
		standardTile("refresh", "device.switch", inactiveLabel: false, decoration: "flat") {
			state "default", label:"", action:"refresh.refresh", icon:"st.secondary.refresh"
		}
        
        controlTile("rgbSelector", "device.color", "color", height: 3, width: 3, inactiveLabel: false) {
		state "color", action:"setColor"
	}

       standardTile("deviceMode", "deviceMode", inactiveLabel: false, canChangeIcon: true, canChangeBackground: true) {
   			state "energy", label:'energy', action:"momentary", icon: "http://mail.lgk.com/aeonv6orange.png"
            state "momentary", label:'momentary', action:"nightLight", icon: "http://mail.lgk.com/aeonv6white.png"
            state "nightLight", label:'NightLight', action:"energy", icon: "http://mail.lgk.com/aeonv6blue.png"
      
        }

	valueTile("voltage", "device.voltage") {
	    state "default", label:'${currentValue} v'
	}
	
    valueTile("amperage", "device.amperage") {
	    state "default", label:'${currentValue} a'
	}
	standardTile("configure", "device.power", inactiveLabel: false, decoration: "flat") {
	    state "configure", label:'', action:"configuration.configure", icon:"st.secondary.configure"
	}
	main(["switch","power","energy","voltage","amperage"] )
		details(["switch", "power", "energy", "deviceMode","amperage","voltage","refresh","reset","configure",
    				"rgbSelector"])
  
}

}


// parse events into attributes
def parse(String description) {
	def result = null
   // log.debug "in parse got message string : '$description'"
    
	if (description != "updated") {
		def cmd = zwave.parse(description, [0x20: 1, 0x26: 3, 0x70: 1, 0x32:3])
       // log.debug "got command = '$cmd'"
      
		if (cmd) {
			result = zwaveEvent(cmd)
	        //log.debug("'$description' parsed to $result")
		} else {
			log.debug("Couldn't zwave.parse '$description'")
		}
	}
    result
}

def updated()
{
log.debug "in updated"
state.currentColor = 0

    state.onOffDisabled = ("true" == disableOnOff)
    state.display = ("true" == displayEvents)
    state.debug = ("true" == debugOutput)
    if (state.debug) log.debug "updated(disableOnOff: ${disableOnOff}(${state.onOffDisabled}), reportInterval: ${reportInterval}, displayEvents: ${displayEvents}, switchAll: ${switchAll}, debugOutput: ${debugOutput}(${state.debug}))"
    response(configure())
}

def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicReport cmd) {
//log.debug "in basic report"
	dimmerEvents(cmd)
}

def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicSet cmd) {
//log.debug "in basic set"
	dimmerEvents(cmd)
}

def zwaveEvent(physicalgraph.zwave.commands.switchmultilevelv3.SwitchMultilevelReport cmd) {
//log.debug "in multi level report"
	dimmerEvents(cmd)
}

def dimmerEvents(physicalgraph.zwave.Command cmd) {

//log.debug "in zwave cmd handler dimmer events cmd.value = $cmd.value"
// lgk ignore dimming events as not working anyway 0 = off 255 = on .
	def result = []
	def value = ""
    
  if (cmd.value == 0 || cmd.value == 255)
   {
	if (cmd.value == 255) 
      value = "on"
     if (cmd.value == 0)
      value = "off"

  //  log.debug "value = $value level = $cmd.value"
	def switchEvent = createEvent(name: "switch", value: value, descriptionText: "$device.displayName was turned $value")
	result << switchEvent
	if (cmd.value) {
		result << createEvent(name: "level", value: cmd.value, unit: "%")
	}
	if (switchEvent.isStateChange) {
		result << response(["delay 3000", zwave.meterV2.meterGet(scale: 2).format()])
	}
	return result
}
 return null
}

def zwaveEvent(physicalgraph.zwave.commands.meterv3.MeterReport cmd) {
//log.debug "in meter report cmd = '$cmd'"

	if (cmd.meterType == 1) {
		if (cmd.scale == 0) {
         log.debug " got kwh $cmd.scaledMeterValue"
			return createEvent(name: "energy", value: cmd.scaledMeterValue, unit: "kWh")
		} else if (cmd.scale == 1) {
			return createEvent(name: "energy", value: cmd.scaledMeterValue, unit: "kVAh")
		} else if (cmd.scale == 2) {
            log.debug " got wattage $cmd.scaledMeterValue"
			return createEvent(name: "power", value: Math.round(cmd.scaledMeterValue), unit: "W")
		} else if (cmd.scale == 4) { //Volts
            log.debug " got voltage $cmd.scaledMeterValue"
           return createEvent(name: "voltage", value: Math.round(cmd.scaledMeterValue), unit: "V")
		} else if (cmd.scale == 5) { //amps scale 5 is amps even though not documented
            log.debug " got amperage = $cmd.scaledMeterValue"
           return createEvent(name: "amperage", value: cmd.scaledMeterValue, unit: "A")
		}
        
        else {
			return createEvent(name: "electric", value: cmd.scaledMeterValue, unit: ["pulses", "V", "A", "R/Z", ""][cmd.scale - 3])
		}
	}
}


def zwaveEvent(physicalgraph.zwave.commands.configurationv1.ConfigurationReport cmd) {
//
}


def on() {
log.debug "in on"
	delayBetween([
		zwave.basicV1.basicSet(value: 0xFF).format(),
		zwave.switchMultilevelV1.switchMultilevelGet().format(),
	], 5000)
   
}

def off() {
log.debug "in off"
	delayBetween([
		zwave.basicV1.basicSet(value: 0x00).format(),
		zwave.switchMultilevelV1.switchMultilevelGet().format(),
	], 5000)
}


def poll() {
 //log.debug "in poll"
	delayBetween([
		zwave.switchBinaryV1.switchBinaryGet().format(),
		zwave.meterV2.meterGet(scale: 0).format(),
        zwave.meterV2.meterGet(scale: 1).format(),
		zwave.meterV2.meterGet(scale: 2).format(),
        zwave.meterV2.meterGet(scale: 4).format(),
	],1000)
}


def refresh() {
 log.debug "in refresh"
	delayBetween([
		zwave.switchMultilevelV1.switchMultilevelGet().format(),
		zwave.meterV2.meterGet(scale: 0).format(),
		zwave.meterV2.meterGet(scale: 1).format(),
        zwave.meterV2.meterGet(scale: 2).format(),
		zwave.meterV2.meterGet(scale: 4).format(),
	], 1000)
}

def setLevel(level) {
log.debug "in setlevel level = $level"
    mySetLevel(level) 
}


def nightLight() {
log.debug "in set nightlight mode" 
     sendEvent(name: "deviceMode", value: "nightLight", displayed: true)
      setDeviceMode(2)
}

def energy() {
log.debug "in set energy mode"
     sendEvent(name: "deviceMode", value: "energy", displayed: true)
      setDeviceMode(0)
}

def momentary() {

log.debug "in momentary mode"  
    sendEvent(name: "deviceMode", value: "momentary", displayed: true)
     setDeviceMode(1) 
}

def setDeviceMode(mode) {    

 log.debug "set current mode to '$mode'"
zwave.configurationV1.configurationSet(parameterNumber: 0x51, size: 1, scaledConfigurationValue: mode).format()

}


def configure()
{

log.debug "in configure initializing stuff"

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
	log.debug "setting configuration"
    
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
    0x6F    4       600     The time interval in seconds for sending Report group 1 (Valid values 0x01-0x7FFFFFFF).
    0x70    4       600     The time interval in seconds for sending Report group 2 (Valid values 0x01-0x7FFFFFFF).
    0x71    4       600     The time interval in seconds for sending Report group 3 (Valid values 0x01-0x7FFFFFFF).
    0xFE    2       0       Device Tag
    0xFF    1       N/A     Reset to factory default setting
    0x51    1       0       mode 0 - energy, 1 - momentary indicator, 2 - night light
    0x53    4       0      hex value ffffff00 .. only night light mode 
    0x54    1   dimmer level 0 -100
    0x21    4    alternate rgb color level ie res,blue,green,red ie 00ffffff
    
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
	zwave.configurationV1.configurationSet(parameterNumber: 0x5B, size: 2, scaledConfigurationValue: 2).format(),	//Minimum change in wattage for a REPORT to be sent (Valid values 0 - 60000)
	zwave.configurationV1.configurationSet(parameterNumber: 0x5C, size: 1, scaledConfigurationValue: 2).format(),	//Minimum change in percentage for a REPORT to be sent (Valid values 0 - 100)
	//zwave.configurationV1.configurationSet(parameterNumber: 0x65, size: 4, scaledConfigurationValue: 14).format(),	//Which reports need to send in Report group 1
	zwave.configurationV1.configurationSet(parameterNumber: 0x65, size: 4, scaledConfigurationValue: 2).format(),	//Which reports need to send in Report group 1
    zwave.configurationV1.configurationSet(parameterNumber: 0x66, size: 4, scaledConfigurationValue: 3).format(),	//Which reports need to send in Report group 2
	zwave.configurationV1.configurationSet(parameterNumber: 0x67, size: 4, scaledConfigurationValue: 0).format(),	//Which reports need to send in Report group 3
	zwave.configurationV1.configurationSet(parameterNumber: 0x6F, size: 4, scaledConfigurationValue: 120).format(),	// change reporting time to two minutes from default 10 minutes
 	zwave.configurationV1.configurationSet(parameterNumber: 0x70, size: 4, scaledConfigurationValue: 120).format(),	//change reporting time to two minutes from default 10 minutes
 	zwave.configurationV1.configurationSet(parameterNumber: 0x71, size: 4, scaledConfigurationValue: 120).format(),	//change reporting time to two minutes from default 10 minutes
	//zwave.configurationV1.configurationSet(parameterNumber: 0x54, size: 1, scaledConfigurationValue: 0).format(),	//dimmer o

 ])
    
}


/**
 *  reset - Resets the devices energy usage meter and attempt to reset device
 *
 *  Defined by the custom command "reset"
 */
def reset() {
    factoryReset()
    return [
	zwave.meterV3.meterReset().format(),
	zwave.meterV3.meterGet(scale: 0).format(), //kWh
    zwave.meterV3.meterGet(scale: 1).format(),
    zwave.meterV3.meterGet(scale: 2).format(),
    zwave.meterV3.meterGet(scale: 4).format()
    ]
}

def setColor(colormap) {

	//log.debug "in set color colormap = ${colormap}"
	log.debug " in setColor: hex =  ${colormap.hex}"
   // log.debug "red = ${colormap.red}"
   // log.debug "green = ${colormap.green}"
   // log.debug "blue = ${colormap.blue}"
         
   if (colormap.hex)
   { 
   		sendEvent(name: "color", value: colormap.hex)
     	zwave.configurationV1.configurationSet(parameterNumber: 0x53, size: 3, configurationValue: [colormap.red, colormap.green, colormap.blue]).format()
	}
}

def mySetLevel(newlevel) {
log.debug "in set setlevel newlevel = '$newlevel'"

/*
Paramter No.
84 
Size
1 
Default
50 

Name
Configure the brightness level of RGB LED 

Description
Configure the brightness level of RGB LED (0%-100%) when it is in Energy Mode/momentary indicate mode.  

Type
range 

Values
0 % - 100 %    ->    Define a brightness level.  
 */
 
 def ledBright = 50
    	if (newlevel) {
        	ledBright=newlevel.toInteger()
		 sendEvent(name: "level", value: ledBright, displayed: true)	 
         delayBetween([
  zwave.configurationV1.configurationSet(parameterNumber: 84, size: 3, configurationValue: [ledBright,ledBright,ledBright]),
  zwave.configurationV1.configurationGet(parameterNumber: 84)
 ])
  }
 
}

def factoryReset()

{

  
/*
Paramter No.
255 
Size
4 

 

Values

1    ->    Resets all configuration parameters to default setting. 
1431655765    ->    Reset the product to default factory setting and be excluded from the Z-wave network. 
 */
  
log.debug "in factory reset"

zwave.configurationV1.configurationSet(parameterNumber: 0xFF, size: 4, scaledConfigurationValue: 1).format()	//factory reset
configure()
}


