// Express Controls EZMultiPli Multi-sensor 
// Motion Sensor - Temperature - Light level - 8 Color Indicator LED - Z-Wave Range Extender - Wall Powered
// driver for SmartThings
// The EZMultiPli is also known as the HSM200 from HomeSeer.com
//
// v0.1.0 - DrZWave - chose better icons, Got color LED to work - first fully functional version
// v0.0.9 - jrs - got the temp and luminance to work. Motion works. Debugging the color wheel.
// v0.0.8 - DrZWave 2/25/2015 - change the color control to be tiles since there are only 8 colors.
// v0.0.7 - jrs - 02/23/2015 - Jim Sulin

metadata {
	definition (name: "EZmultiPli", namespace: "DrZWave", author: "Eric Ryherd", oauth: true) {
	capability "Motion Sensor"
	capability "Temperature Measurement"
	capability "Illuminance Measurement"
	capability "Switch"
	capability "Color Control"
	capability "Configuration"
    command "setColor"

	fingerprint deviceId: "0x0701", inClusters: "0x5E, 0x71, 0x31, 0x33, 0x72, 0x86, 0x59, 0x85, 0x70, 0x77, 0x5A, 0x7A, 0x73, 0xEF, 0x20" 
	} // end definition

	simulator {
		// messages the device returns in response to commands it receives
		status "motion"     : "command: 7105000000FF07, payload: 07"
		status "no motion"  : "command: 7105000000FF07, payload: 00"

		for (int i = 0; i <= 100; i += 20) {
			status "temperature ${i}F": new physicalgraph.zwave.Zwave().sensorMultilevelV5.sensorMultilevelReport(
				scaledSensorValue: i, precision: 1, sensorType: 1, scale: 1).incomingMessage()
		}
		for (int i = 0; i <= 100; i += 20) {
			status "luminance ${i} %": new physicalgraph.zwave.Zwave().sensorMultilevelV5.sensorMultilevelReport(
				scaledSensorValue: i, precision: 0, sensorType: 3).incomingMessage()
		}

		controlTile("rgbSelector", "device.color", "color", height: 3, width: 3, inactiveLabel: false) {
		            state "color", action:"setColor"
	    }

	} //end simulator

	tiles {
		standardTile("motion", "device.motion", width: 2, height: 2, canChangeIcon: true, canChangeBackground: true) {
			state "active", label:'motion', icon:"st.motion.motion.active", backgroundColor:"#53a7c0"
			state "inactive", label:'no motion', icon:"st.motion.motion.inactive", backgroundColor:"#ffffff"
		}
		standardTile("switch", "device.switch", canChangeIcon: true) {
			state "on", label:'${name}', action:"switch.off", icon:"st.switches.light.on", backgroundColor:"#79b821", nextState:"turningOff"
			state "off", label:'${name}', action:"switch.on", icon:"st.switches.light.off", backgroundColor:"#ffffff", nextState:"turningOn"
			state "turningOn", label:'${name}', icon:"st.switches.light.on", backgroundColor:"#79b821"
			state "turningOff", label:'${name}', icon:"st.switches.light.off", backgroundColor:"#ffffff"
		}
		valueTile("temperature", "device.temperature") {
			state "temperature", label:'${currentValue}°', unit:"F", icon:"st.Weather.weather2", // would be better if the units would switch to the desired units of the system (imperial or metric)
			backgroundColors:[
				[value: 0,  color: "#1010ff"], // blue=cold
				[value: 65, color: "#a0a0f0"],
				[value: 70, color: "#e0e050"],
				[value: 75, color: "#f0d030"], // yellow
				[value: 80, color: "#fbf020"],
				[value: 85, color: "#fbdc01"],
				[value: 90, color: "#fb3a01"],
				[value: 95, color: "#fb0801"]  // red=hot
			]
		} // icons to use would be st.Weather.weather2 or st.alarm.temperature.normal - see http://scripts.3dgo.net/smartthings/icons/ for a list of icons
		valueTile("illuminance", "device.illuminance", inactiveLabel: false) {
// jrs 4/7/2015 - Null on display
//			state "luminosity", label:'${currentValue} ${unit}'
			state "luminosity", label:'${currentValue}%', unit:"%", icon:"st.illuminance.illuminance.bright",
			backgroundColors:[
				[value: 25, color: "#404040"],
				[value: 50, color: "#808080"],
				[value: 75, color: "#a0a0a0"],
				[value: 90, color: "#e0e0e0"]
			]
		}
// jrs 4/7/2015 
		controlTile("rgbSelector", "device.color", "color", height: 1, width: 1) {
			state "color", action:"color control.setColor"
		}

		standardTile("configure", "device.configure", inactiveLabel: false, decoration: "flat") {
			state "configure", label:'', action:"configuration.configure", icon:"st.secondary.configure"
		}

		main ("motion")
		details(["motion", "temperature", "illuminance", "configure", "switch", "rgbSelector"])
	}  // end tiles
    
	preferences {
    	input "OnTime",  "number", title: "No Motion Interval", description: "N minutes lights stay on after no motion detected [0, 1-127]", defaultValue: 10, displayDuringSetup: true, required: false
        input "OnLevel", "number", title: "Dimmer Onlevel", description: "Dimmer OnLevel for associated node 2 lights [0, 1-99, -1]", defaultValue: -1, displayDuringSetup: true, required: false
        input "LiteMin", "number", title: "Luminance Report Frequency", description: "Luminance report sent every N minutes", defaultValue: 60, displayDuringSetup: true, required: false
        input "TempMin", "number", title: "Temperature Report Frequency", description: "Temperature report sent every N minutes", defaultValue: 60, displayDuringSetup: true, required: false
        input "TempAdj", "number", title: "Temperature Calibration", description: "Adjust temperature up/down N tenths of a degree F [(-127)-(+128)]", defaultValue: 0, displayDuringSetup: true, required: false
  }

} // end metadata


// Parse incoming device messages from device to generate events
def parse(String description){
	def result = []
	def cmd = zwave.parse(description, [0x31: 5]) // 0x31=SensorMultilevel which we force to be version 5
	if (cmd) {
		result << createEvent(zwaveEvent(cmd))
	}
	log.debug "Parse returned ${result}"
	return result
}


// Event Generation
def zwaveEvent(physicalgraph.zwave.commands.sensormultilevelv5.SensorMultilevelReport cmd){
	def map = [:]
	switch (cmd.sensorType) {
		case 0x01:				// SENSOR_TYPE_TEMPERATURE_VERSION_1
			def cmdScale = cmd.scale == 1 ? "F" : "C"
			map.value = convertTemperatureIfNeeded(cmd.scaledSensorValue, cmdScale, cmd.precision)
			map.unit = getTemperatureScale()
			map.name = "temperature"
			log.debug "Temperature report"
			break;
		case 0x03 :				// SENSOR_TYPE_LUMINANCE_VERSION_1
			map.value = cmd.scaledSensorValue.toInteger().toString()
			map.unit = "%"
			map.name = "illuminance"
			log.debug "Luminance report"
			break;
	}
	return map
}


def zwaveEvent(physicalgraph.zwave.commands.notificationv3.NotificationReport cmd) {
	def map = [:]
	if (cmd.notificationType==0x07) {	// NOTIFICATION_TYPE_BURGLAR
    		if (cmd.event==0x07 || cmd.event==0x08) {
			map.name = "motion"
            	map.value = "active"
			map.descriptionText = "$device.displayName motion detected"
            	log.debug "motion recognized"
		} else if (cmd.event==0) {
			map.name = "motion"
            	map.value = "inactive"
			map.descriptionText = "$device.displayName no motion detected"
            	log.debug "No motion recognized"
    		}
	} 
	if (map.name != "motion") {
    		log.debug "unmatched parameters for cmd: ${cmd.toString()}}"
	}
	return map
}

def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicReport cmd) {
    [name: "switch", value: cmd.value ? "on" : "off", type: "digital"]
}


def on() {
	log.debug "Turning Light 'on'"
	delayBetween([
		zwave.basicV1.basicSet(value: 0xFF).format(),
		zwave.basicV1.basicGet().format()
	], 500)
}

def off() {
	log.debug "Turning Light 'off'"
	delayBetween([
		zwave.basicV1.basicSet(value: 0x00).format(),
		zwave.basicV1.basicGet().format()
	], 500)
}


def setColor(value) {
    log.debug "setColor() : ${value} and red value is= ${value.red}"
    def cmds = []
    def myred=value.red >128 ? 255 : 0	// the EZMultiPli has just on/off for each of the 3 channels RGB so convert the 0-255 value into 0 or 255.
    def mygreen=value.green >128 ? 255 : 0
    def myblue=value.blue>128 ? 255 : 0
//			 cmds << zwave.colorControlV1.stateSet(stateDataLength: 3, VariantGroup1: [0x02, myred], VariantGroup2:[ 0x03, mygreen], VariantGroup3:[0x04,myblue]).format() // ST support for this command as of 2015/02/23 does not support the color IDs so this command cannot be used.
// So instead we'll use these commands to hack around the lack of support of the above command
	cmds << zwave.basicV1.basicSet(value: 0x00).format() // As of 2015/02/23 ST is not supporting stateSet properly but found this hack that works. 
	if (myred!=0) {
       	cmds << zwave.colorControlV1.startCapabilityLevelChange(capabilityId: 0x02, startState: myred, ignoreStartState: True, updown: True).format()
		cmds << zwave.colorControlV1.stopStateChange(capabilityId: 0x02).format()
    }
    if (mygreen!=0) {
	 	cmds << zwave.colorControlV1.startCapabilityLevelChange(capabilityId: 0x03, startState: mygreen, ignoreStartState: True, updown: True).format()
	 	cmds << zwave.colorControlV1.stopStateChange(capabilityId: 0x03).format()
    }
    if (myblue!=0) {
		cmds << zwave.colorControlV1.startCapabilityLevelChange(capabilityId: 0x04, startState: myblue, ignoreStartState: True, updown: True).format()
		cmds << zwave.colorControlV1.stopStateChange(capabilityId: 0x04).format()
    }
    delayBetween(cmds, 100)
}

def zwaveEvent(physicalgraph.zwave.Command cmd) {
	// Handles all Z-Wave commands we aren't interested in
	[:]
}

// ensure we are passing acceptable param values for LiteMin & TempMin configs
def checkLiteTempInput(value) {
	if (value == null) {
    	value=60
    }
    def liteTempVal = value.toInteger()
    switch (liteTempVal) {
      case { it < 0 }:
        return 60			// bad value, set to default
        break
      case { it > 127 }:
        return 127			// bad value, greater then MAX, set to MAX
        break
      default:
        return liteTempVal	// acceptable value
    }
}

// ensure we are passing acceptable param value for OnTime config
def checkOnTimeInput(value) {
	if (value == null) {
    	value=10
    }
    def onTimeVal = value.toInteger()
    switch (onTimeVal) {
      case { it < 0 }:
        return 10			// bad value set to default
        break
      case { it > 127 }:
        return 127			// bad value, greater then MAX, set to MAX
        break
      default:
        return onTimeVal	// acceptable value
    }
}

// ensure we are passing acceptable param value for OnLevel config
def checkOnLevelInput(value) {
	if (value == null) {
    	value=99
    }  
    def onLevelVal = value.toInteger()
    switch (onLevelVal) {
      case { it < -1 }:
        return -1			// bad value set to default
        break
      case { it > 99 }:
        return 99			// bad value, greater then MAX, set to MAX
        break
      default:
        return onLevelVal	// acceptable value
    }
}


// ensure we are passing an acceptable param value for TempAdj configs
def checkTempAdjInput(value) {
	if (value == null) {
    	value=0
    }
	def tempAdjVal = value.toInteger()
    switch (tempAdjVal) {
      case { it < -127 }:
        return 0			// bad value, set to default
        break
      case { it > 128 }:
        return 128			// bad value, greater then MAX, set to MAX
        break
      default:
        return tempAdjVal	// acceptable value
    }
}


def configure() {
	log.debug "OnTime=${settings.OnTime} OnLevel=${settings.OnLevel}"
	def cmd = delayBetween([
		zwave.configurationV1.configurationSet(parameterNumber: 1, size: 1, configurationValue: [checkOnTimeInput(settings.OnTime)]).format(),
		zwave.configurationV1.configurationSet(parameterNumber: 2, size: 1, configurationValue: [checkOnLevelInput(settings.OnLevel)]).format(),
		zwave.configurationV1.configurationSet(parameterNumber: 3, size: 1, configurationValue: [checkLiteTempInput(settings.LiteMin)]).format(),
		zwave.configurationV1.configurationSet(parameterNumber: 4, size: 1, configurationValue: [checkLiteTempInput(settings.TempMin)]).format(),
		zwave.configurationV1.configurationSet(parameterNumber: 5, size: 1, configurationValue: [checkTempAdjInput(settings.TempAdj)]).format()
	], 100)
	log.debug cmd
	cmd
}