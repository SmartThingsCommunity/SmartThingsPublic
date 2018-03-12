/**
 *  Sensative Strips Multi-Sensor
 *  Device Handler by Jonathan Bjarnason & Dhiraj Paryani
 *
 *  Date: 2017-02-08
 *  Supported Command Classes per device specs
 *  
 *         Association v2
 *         Association Group Information
 *         Battery
 *         Configuration
 *         Device Reset Local
 *         Manufacturer Specific
 *         Sensor Multilevel v5
 *         Notification v4
 *         Powerlevel
 *         Version v2
 *         Wake Up v2
 *         ZWavePlus Info v2
 *
 *   Parm Size Description                                   Value
 *      2    1 LED alarm event reporting                     1 (Default)-On, 0-Off
 *      3    1 Temperature & Light reporting frequency       1 (Default)-Normal, 2-Frequent
 *      4    1 Temperature Reporting                         1 (Default)-On, 0-Off (Does not affect temperature alarms)
 *      5    1 Temperature Reporting Unit                    0 (Default)-Celcius, 1-Fahrenheit
 *      6    1 Temperature Alarms       	                 0 (Default)-Off, 1-On
 *      7    1 High temperature alarm level                  60 (Default)-accepts values from -20 to 60 (in Celcius) 
 *      8    1 Low temperature alarm level                  -20 (Default)-accepts values from -20 to 60 (in Celcius)
 *      9    1 Ambient Light Reporting                       1 (Default)-On, 0-Off, 1-On, 2-Report only when levels defined in parameter 10 & 11 are passed.
 *      10   4 High ambient light report level               40000 (Default)--accepts values from 3 to 64000
 *      11   4 Low ambient light report level                5000 (Default)-accepts values from 1 to 42000 (Must be significantly lower than parameter 10)
 *      12   1 Leakage Alarm                                 1 (Default)-On, 0-Off *Strips Comfort Pre-Set to Off.
 *      13   1 Leakage Alarm Level                           10 (Default)-accepts values from 1 to 100 (1-almost dry, 100-wet)
 *      14   1 Moisture Reporting Period                     0 (Default)-Off, accepts values from 0 to 120 (Hours between reports)
 * 
 *    This device handler will just override the smartthings default wakeup interval of 4 hours and set to 24 hours (manufacturer default)
 *      and check the battery once a day (no sooner than every 23 hours)
 */

metadata {
	definition (name: "Strips Multi-Sensor", namespace: "sensative", author: "Dhiraj Paryani") {
		capability "Battery"
		capability "Configuration"
		capability "Illuminance Measurement"
		capability "Sensor"
		capability "Tamper Alert"
		capability "Temperature Measurement"
		capability "Water Sensor"
        capability "Refresh"
			attribute "needUpdate", "string"
        	attribute "tamper" , "string"
        
	//don't know yet the new fingerprint
	fingerprint mfr:"019A", prod:"0003", model:"000A", deviceJoinName:"Strips Multi-Sensor"
	fingerprint deviceId:"0x2101", inClusters: "0x5E,0x85,0x59,0x86,0x72,0x31,0x5A,0x73,0x80,0x70,0x71,0x84"
	fingerprint cc: "0x5E,0x85,0x59,0x86,0x72,0x31,0x5A,0x73,0x80,0x70,0x71,0x84", mfr:"019A", prod:"0003", model:"000A", deviceJoinName:"Strips Multi-Sensor"
	}

	preferences {
    	section ("configuration settings") {         
		input(
			title : "Changes in Settings marked with  *  will not take effect until the next device Wake Up."
			,description : null
			,type : "paragraph")
        input "wakeupInterval","enum",
			title: "* Device Wake Up Interval",
			description: "24 hours",
			defaultValue: "86400",
			required: false,
			displayDuringSetup: false,
			options: buildInterval()    
        input "cLedReportEvent", "bool", 
			title: "* LED alarm event reporting",
			description: "Alarm Event LED Feedback",			
			defaultValue: true,
			displayDuringSetup: false
		input "cReportingFrequency", "enum", 
			title: "* Temperature & Light reporting frequency",
			description: "Normal",
			options:["normal": "Normal", "frequent": "Frequent"],
			defaultValue: "normal",
			displayDuringSetup: false
        input "cTemperatureReporting", "bool", 
			title: "* Temperature reporting",			
			defaultValue: true,
			displayDuringSetup: false    
        input "cTemperatureUnit", "enum", 
			title: "* Temperature reporting unit",
			description: "Celsius",
			options:["celsius": "Celsius", "fahrenheit": "Fahrenheit"],
			defaultValue: "Celsius",
			displayDuringSetup: false
        input "cTemperatureAlarms", "bool", 
			title: "* Temperature alarms",						
			defaultValue: false,
			displayDuringSetup: false                
        input "cTemperatureHighAlarmLevel","enum",
			title: "* High temperature alarm level",
			description: "60°C / 140°F",
			defaultValue: "60",
			required: false,
			displayDuringSetup: false,
			options: temperatureLevelInterval()        
        input "cTemperatureLowAlarmLevel","enum",
			title: "* Low temperature alarm level",
			description: "-20°C / -4°F",
			defaultValue: "-20",
			required: false,
			displayDuringSetup: false,
			options: temperatureLevelInterval()
        input "cAmbientLightReporting", "enum", 
			title: "* Ambient light reporting",
            description: "on",
			options:["off": "Off", "on": "On", "report only when levels defined in parameter 10 & 11 are passed": "Report only when levels defined in parameter 10 & 11 are passed"],
			defaultValue: "on",
			displayDuringSetup: false 
        input "cHighAmbientLightLevel","enum",
			title: "* High ambient light report level (Parameter 10)",
			description: "40000",
			defaultValue: "40000",
			required: false,
			displayDuringSetup: false,
			options: highAmbientLightLevelInterval()        
        input "cLowAmbientLightLevel","enum",
			title: "* Low ambient light report level (Parameter 11)",
			description: "5000",
            defaultValue: "5000",
			required: false,
			displayDuringSetup: false,
			options: lowAmbientLightLevelInterval()    
        input "cLeakageAlarm", "bool", 
			title: "* Leakage alarm",
			defaultValue: true,
			displayDuringSetup: false
        input "cLeakageAlarmLevel","enum",
			title: "* Leakage alarm level",
			description: "10%",
			defaultValue: "10",
			required: false,
			displayDuringSetup: false,
			options: leakageLevelInterval()
        input "cMoistureReportingPeriod","enum",
			title: "* Moisture reporting period",
			description: "Disabled",
			defaultValue: "0",
			required: false,
			displayDuringSetup: false,
			options: moistureReportingInterval()           
		}
    }

	tiles(scale: 2) {		
        valueTile("temperature", "device.temperature", inactiveLabel: false, width: 2, height: 2) {
			state "temperature" , label:'${currentValue} °', backgroundColor:"#FFCC7B"
        }    
        valueTile("illuminance", "device.illuminance", inactiveLabel: false, width: 2, height: 2) {
			state "illuminance" , label:'${currentValue} lux', backgroundColor:"#D3D3D3"
		}
        valueTile("moisture", "device.moisture", inactiveLabel: false, width: 2, height: 2) {
			state "moisture" , label:'${currentValue} %', backgroundColor:"#00A0DC"
		}
		standardTile("tempAlarm", "device.tempAlarm", inactiveLabel: false, width: 3, height: 3) {
			state "tempAlarm", label:'${currentValue}', action:"resetTempAlarm", backgroundColor:"#FFCC7B"
		}
        standardTile("leakageAlarm", "device.leakageAlarm", inactiveLabel: false, width: 3, height: 3) {
			state "leakageAlarm", label:'${currentValue}', action:"resetLeakageAlarm", backgroundColor:"#00A0DC"
		}
		standardTile("needUpdate", "device.needUpdate", inactiveLabel: false, width: 2, height: 2) {
			state "NO" , label:'Synced', backgroundColor:"#B1D57D"
			state "YES", label:'Pending changes', backgroundColor:"#FFCC7B"
            state "3", label:'Starting in 3...', backgroundColor:"#FFCC7B"
            state "2", label:'Starting in 3...', backgroundColor:"#FFCC7B"
            state "1", label:'Starting in 3...', backgroundColor:"#FFCC7B"
            state "0", label:'Sending changes...', backgroundColor:"#FFCC7B"
        }
        standardTile("refresh", "device.switch", inactiveLabel: false, decoration: "flat", height: 2, width: 2) {
        	state "refresh", label:'Send changes', action:"refresh.refresh", icon:"st.secondary.refresh"
        }    
		standardTile("battery", "device.battery",  inactiveLabel: false, width: 2, height: 2) {
			state "battery", label:'Battery ${currentValue}%'
		}

		main (["temperature"])
		details(["temperature","illuminance", "moisture", "tempAlarm", "leakageAlarm" ,"battery", "needUpdate", "refresh"])
	}

	simulator {
		// messages the device returns in response to commands it receives
		status "wake up" : "command: 9881, payload: 00 84 07"
		status "battery (100%)" : "command: 9881, payload: 00 80 03 64"
		status "battery low" : "command: 9881, payload: 00 80 03 FF"
	}
}

def configure() {
	def commands = []  
	state.lastupdate = now()
	sendEvent(name: "tempAlarm", value: "No\nHeating\nAlarm", displayed: false)
    sendEvent(name: "leakageAlarm", value: "No\nLeakage\nAlarm", displayed: false)
    sendEvent(name: "moisture", value: "0", displayed: false)

	log.debug "Listing all device parameters and defaults since this is a new inclusion"
	commands << zwave.manufacturerSpecificV1.manufacturerSpecificGet().format()
	commands << zwave.versionV1.versionGet().format()
	commands << zwave.batteryV1.batteryGet().format()	
    /*commands << zwave.configurationV1.configurationGet(parameterNumber: 2).format()
    commands << zwave.configurationV1.configurationGet(parameterNumber: 3).format()
	commands << zwave.configurationV1.configurationGet(parameterNumber: 4).format()
    commands << zwave.configurationV1.configurationGet(parameterNumber: 5).format()
	commands << zwave.configurationV1.configurationGet(parameterNumber: 6).format()    
    commands << zwave.configurationV1.configurationGet(parameterNumber: 7).format()
	commands << zwave.configurationV1.configurationGet(parameterNumber: 8).format()    
    commands << zwave.configurationV1.configurationGet(parameterNumber: 9).format()
	commands << zwave.configurationV1.configurationGet(parameterNumber: 10).format()    
    commands << zwave.configurationV1.configurationGet(parameterNumber: 11).format()
	commands << zwave.configurationV1.configurationGet(parameterNumber: 12).format() 
    commands << zwave.configurationV1.configurationGet(parameterNumber: 13).format()
	commands << zwave.configurationV1.configurationGet(parameterNumber: 14).format()*/ /*Doesn't add value*/
    commands << zwave.wakeUpV2.wakeUpIntervalSet(seconds: 86400, nodeid:zwaveHubNodeId).format()
	commands << zwave.wakeUpV2.wakeUpIntervalGet().format()
	commands << zwave.wakeUpV2.wakeUpNoMoreInformation().format()
	delayBetween(commands, 1500)
}

private getCommandClassVersions() {
	[
		0x71: 4,  // Notification
		0x5E: 2,  // ZwaveplusInfo
		0x59: 1,  // AssociationGrpInfo
		0x85: 2,  // Association
		0x80: 1,  // Battery
		0x70: 1,  // Configuration
		0x5A: 1,  // DeviceResetLocally
		0x72: 1,  // ManufacturerSpecific
        0x31: 5,  // MultiLevel Sensor
		0x73: 1,  // Powerlevel
		0x84: 2,  // WakeUp
		0x86: 2,  // Version
	]
}

// Parse incoming device messages to generate events
def parse(String description) {
	def result = []
	def cmd    
	if (description.startsWith("Err 106")) {
		state.sec = 0
		result = createEvent( name: "secureInclusion", value: "failed", eventType: "ALERT",
				descriptionText: "This sensor failed to complete the network security key exchange. If you are unable to control it via SmartThings, you must remove it from your network and add it again.")
	} else if (description.startsWith("Err")) {
		result = createEvent(descriptionText: "$device.displayName $description", isStateChange: true)
	} else {
		//cmd = zwave.parse(description, commandClassVersions)
        cmd = zwave.parse(description, [0x71:3])
		if (cmd) {
			result = zwaveEvent(cmd)
		}
	}
	if (result instanceof List) {
		result = result.flatten()
	}
	log.debug "Parsed '$description' to $result"    
	return result
}

/**
* Triggered when Done button is pushed on Preference Pane
*/
def updated()
{
	if(now() - state.lastupdate > 3000){
		def isUpdateNeeded = "NO"
        
		if(wakeupInterval != null && state.wakeupInterval != wakeupInterval) {isUpdateNeeded = "YES"}
        if(cLedReportEvent != null && state.cLedReportEvent != cLedReportEvent) {isUpdateNeeded = "YES"}
        if(cReportingFrequency != null && state.cReportingFrequency != cReportingFrequency) {isUpdateNeeded = "YES"}
        if(cTemperatureReporting != null && state.cTemperatureReporting != cTemperatureReporting) {isUpdateNeeded = "YES"}
        if(cTemperatureUnit != null && state.cTemperatureUnit != cTemperatureUnit) {isUpdateNeeded = "YES"}
		if(cTemperatureAlarms != null && state.cTemperatureAlarms != cTemperatureAlarms) {isUpdateNeeded = "YES"}
		if(cTemperatureHighAlarmLevel != null && state.cTemperatureHighAlarmLevel != cTemperatureHighAlarmLevel) {isUpdateNeeded = "YES"}		
		if(cTemperatureLowAlarmLevel != null && state.cTemperatureLowAlarmLevel != cTemperatureLowAlarmLevel) {isUpdateNeeded = "YES"}
        if(cAmbientLightReporting != null && state.cAmbientLightReporting != cAmbientLightReporting) {isUpdateNeeded = "YES"}        
        if(cHighAmbientLightLevel != null && state.cHighAmbientLightLevel != cHighAmbientLightLevel) {isUpdateNeeded = "YES"}
		if(cLowAmbientLightLevel != null && state.cLowAmbientLightLevel != cLowAmbientLightLevel) {isUpdateNeeded = "YES"}
		if(cLeakageAlarm != null && state.cLeakageAlarm != cLeakageAlarm) {isUpdateNeeded = "YES"}		
		if(cLeakageAlarmLevel != null && state.cLeakageAlarmLevel != cLeakageAlarmLevel) {isUpdateNeeded = "YES"}
        if(cMoistureReportingPeriod != null && state.cMoistureReportingPeriod != cMoistureReportingPeriod) {isUpdateNeeded = "YES"}
		state.lastupdate = now()
		sendEvent(name:"needUpdate", value: isUpdateNeeded, displayed:false, isStateChange: true)
	}
}

/**
* Only device parameter changes require a state change 
*/
def update_settings()
{
	def cmds = []
	def isUpdateNeeded = "NO"
    
    //threeSeconds()
    //twoSeconds()
    //oneSecond()
    //zeroSeconds()

	if (state.wakeupInterval != wakeupInterval){
		cmds << zwave.wakeUpV2.wakeUpIntervalSet(seconds: wakeupInterval.toInteger(), nodeid:zwaveHubNodeId).format()
		cmds << "delay 1000"
		cmds << zwave.wakeUpV2.wakeUpIntervalGet().format()
	}
    if (cLedReportEvent != state.cLedReportEvent){
		cmds << zwave.configurationV1.configurationSet(parameterNumber: 2, size: 1, configurationValue: [cLedReportEvent == true ? 1 : 0]).format()
		cmds << "delay 1000"
		cmds << zwave.configurationV1.configurationGet(parameterNumber: 2).format()
	}
    if (cReportingFrequency != state.cReportingFrequency){
		cmds << zwave.configurationV1.configurationSet(parameterNumber: 3, size: 1, configurationValue: [cReportingFrequency == "frequent" ? 2 : 1]).format()
		cmds << "delay 1000"
		cmds << zwave.configurationV1.configurationGet(parameterNumber: 3).format()
	}
    if (cTemperatureReporting != state.cTemperatureReporting){
		cmds << zwave.configurationV1.configurationSet(parameterNumber: 4, size: 1, configurationValue: [cTemperatureReporting == true ? 1 : 0]).format()
		cmds << "delay 1000"
		cmds << zwave.configurationV1.configurationGet(parameterNumber: 4).format()
	}
    if (cTemperatureUnit != state.cTemperatureUnit){
		cmds << zwave.configurationV1.configurationSet(parameterNumber: 5, size: 1, configurationValue: [cTemperatureUnit == "fahrenheit" ? 1 : 0]).format()
		cmds << "delay 1000"
		cmds << zwave.configurationV1.configurationGet(parameterNumber: 5).format()
	}
    if (cTemperatureAlarms != state.cTemperatureAlarms){
		cmds << zwave.configurationV1.configurationSet(parameterNumber: 6, size: 1, configurationValue: [cTemperatureAlarms == true ? 1 : 0]).format()
		cmds << "delay 1000"
		cmds << zwave.configurationV1.configurationGet(parameterNumber: 6).format()
	}    
    if (cTemperatureHighAlarmLevel != state.cTemperatureHighAlarmLevel){
		cmds << zwave.configurationV1.configurationSet(parameterNumber: 7, size: 1, configurationValue: [cTemperatureHighAlarmLevel.toInteger()]).format()
		cmds << "delay 1000"
		cmds << zwave.configurationV1.configurationGet(parameterNumber: 7).format()
	}
    if (cTemperatureLowAlarmLevel != state.cTemperatureLowAlarmLevel){
		cmds << zwave.configurationV1.configurationSet(parameterNumber: 8, size: 1, configurationValue: [cTemperatureLowAlarmLevel.toInteger()]).format()
		cmds << "delay 1000"
		cmds << zwave.configurationV1.configurationGet(parameterNumber: 8).format()
	}
    if (cAmbientLightReporting != state.cAmbientLightReporting){
		cmds << zwave.configurationV1.configurationSet(parameterNumber: 9, size: 1, configurationValue: [cAmbientLightReporting.toInteger()]).format()
		cmds << "delay 1000"
		cmds << zwave.configurationV1.configurationGet(parameterNumber: 9).format()
	}
    if (cHighAmbientLightLevel != state.cHighAmbientLightLevel){
		cmds << zwave.configurationV1.configurationSet(parameterNumber: 10, size: 4, configurationValue: [cHighAmbientLightLevel.toInteger()]).format()
		cmds << "delay 1000"
		cmds << zwave.configurationV1.configurationGet(parameterNumber: 10).format()
	}
    if (cLowAmbientLightLevel != state.cLowAmbientLightLevel){
		cmds << zwave.configurationV1.configurationSet(parameterNumber: 11, size: 4, configurationValue: [cLowAmbientLightLevel.toInteger()]).format()
		cmds << "delay 1000"
		cmds << zwave.configurationV1.configurationGet(parameterNumber: 11).format()
	}
    if (cLeakageAlarm != state.cLeakageAlarm){
		cmds << zwave.configurationV1.configurationSet(parameterNumber: 12, size: 1, configurationValue: [cLeakageAlarm == true ? 1 : 0]).format()
		cmds << "delay 1000"
		cmds << zwave.configurationV1.configurationGet(parameterNumber: 12).format()
	}
    if (cLeakageAlarmLevel != state.cLeakageAlarmLevel){
		cmds << zwave.configurationV1.configurationSet(parameterNumber: 13, size: 1, configurationValue: [cLeakageAlarmLevel.toInteger()]).format()
		cmds << "delay 1000"
		cmds << zwave.configurationV1.configurationGet(parameterNumber: 13).format()
	}
    if (cMoistureReportingPeriod != state.cMoistureReportingPeriod){
		cmds << zwave.configurationV1.configurationSet(parameterNumber: 14, size: 1, configurationValue: [cMoistureReportingPeriod.toInteger()]).format()
		cmds << "delay 1000"
		cmds << zwave.configurationV1.configurationGet(parameterNumber: 14).format()
	}
    
	cmds << "delay 1000"
	sendEvent(name:"needUpdate", value: isUpdateNeeded, displayed:false, isStateChange: true)
	return cmds
}			


def zwaveEvent(physicalgraph.zwave.commands.securityv1.SecurityMessageEncapsulation cmd) {
	def encapsulatedCommand = cmd.encapsulatedCommand(commandClassVersions)
	log.debug "encapsulated: $encapsulatedCommand"
	if (encapsulatedCommand) {
		state.sec = 1
		return zwaveEvent(encapsulatedCommand)
	} else {
		log.warn "Unable to extract encapsulated cmd from $cmd"
		return [createEvent(descriptionText: cmd.toString())]
	}
}

def zwaveEvent(physicalgraph.zwave.commands.sensoralarmv1.SensorAlarmReport cmd) {
	return sensorValueEvent(cmd.sensorState)
}

def zwaveEvent(physicalgraph.zwave.commands.configurationv1.ConfigurationReport cmd) {
	def result = []
    def value = cmd.scaledConfigurationValue == 1 ? true : false
	if (cmd.parameterNumber == 9) {                
                log.debug "cmd.parameterNumber: $cmd.parameterNumber"
                state.cAmbientLightReporting = value;
    			result << createEvent(name: "cAmbientLightReporting", value: value, isStateChange: true, displayed: false)                    
    } else if (cmd.parameterNumber == 12) {
    		    log.debug "cmd.parameterNumber: $cmd.parameterNumber"
                state.cLeakageAlarm = value;
				result << createEvent(name: "cLeakageAlarm", value: value, isStateChange: true, displayed: false)                
	}
    return result
}

def zwaveEvent(physicalgraph.zwave.commands.notificationv3.NotificationReport cmd) {
	def result = []
    log.debug "cmd.notificationType: $cmd.notificationType"
    log.debug "cmd.event: $cmd.event"
	if (cmd.notificationType == 0x05 && cmd.event == 0x02) {
                log.debug "cmd.notificationType: $cmd.notificationType"
				def timeString1 = new Date().format("MMM d", location.timeZone)
				def timeString2 = new Date().format("hh:mm", location.timeZone)
				result << createEvent(name: "leakageAlarm", value: "Detected Leakage\n${timeString1}\n${timeString2}", descriptionText: "$device.displayName detected leakage at ${timeString1} ${timeString2}", isStateChange: true)
    } else if (cmd.notificationType == 0x05 && cmd.event == 0x00) {
				def timeString1 = new Date().format("MMM d", location.timeZone)
				def timeString2 = new Date().format("hh:mm", location.timeZone)
				result << createEvent(name: "leakageAlarm", value: "Leakage ended at \n${timeString1}\n${timeString2}", descriptionText: "$device.displayName leakage ended at ${timeString1} ${timeString2}", isStateChange: true)	
	} else if (cmd.notificationType == 0x04 && cmd.event == 0x06) {
				def timeString1 = new Date().format("MMM d", location.timeZone)
				def timeString2 = new Date().format("hh:mm", location.timeZone)
				result << createEvent(name: "tempAlarm", value: "Detected Low Temp\n${timeString1}\n${timeString2}", descriptionText: "$device.displayName detected low temperature at ${timeString1} ${timeString2}", isStateChange: true)	   
    } else if (cmd.notificationType == 0x04 && cmd.event == 0x02) {
				def timeString1 = new Date().format("MMM d", location.timeZone)
				def timeString2 = new Date().format("hh:mm", location.timeZone)
				result << createEvent(name: "tempAlarm", value: "Detected High Temp\n${timeString1}\n${timeString2}", descriptionText: "$device.displayName detected high temperature at ${timeString1} ${timeString2}", isStateChange: true)	   
    } else if (cmd.notificationType == 0x04 && cmd.event == 0x00) {
				def timeString1 = new Date().format("MMM d", location.timeZone)
				def timeString2 = new Date().format("hh:mm", location.timeZone)
				result << createEvent(name: "tempAlarm", value: "Temp restored at\n${timeString1}\n${timeString2}", descriptionText: "$device.displayName restored temperature at ${timeString1} ${timeString2}", isStateChange: true)	   
    } else if (cmd.notificationType == 0x07) {
		if (cmd.event == 0x00) {
			if (cmd.eventParametersLength == 0 || cmd.eventParameter[0] != 3) {
				result << createEvent(descriptionText: "$device.displayName covering replaced", isStateChange: true, displayed: false)
			} else {
				result << sensorValueEvent(0)
			}
		} else if (cmd.event == 0x01 || cmd.event == 0x02) {
			result << sensorValueEvent(1)
		} else if (cmd.event == 0x03) {
			result << createEvent(descriptionText: "$device.displayName covering was removed", isStateChange: true)
			if (!device.currentState("ManufacturerCode")) {
				result << response(secure(zwave.manufacturerSpecificV1.manufacturerSpecificGet()))
			}	
		} else if (cmd.event == 0x05 || cmd.event == 0x06) {
			result << createEvent(descriptionText: "$device.displayName detected glass breakage", isStateChange: true)
		} else {
			result << createEvent(descriptionText: "$device.displayName event $cmd.event ${cmd.eventParameter.inspect()}", isStateChange: true, displayed: false)
		}
	} else if (cmd.notificationType) {
			result << createEvent(descriptionText: "$device.displayName notification $cmd.notificationType event $cmd.event ${cmd.eventParameter.inspect()}", isStateChange: true, displayed: false)
	} else {
			def value = cmd.v1AlarmLevel == 255 ? "active" : cmd.v1AlarmLevel ?: "inactive"
			result << createEvent(name: "alarm $cmd.v1AlarmType", value: value, isStateChange: true, displayed: false)
	}
	return result
}

def zwaveEvent(physicalgraph.zwave.commands.wakeupv2.WakeUpNotification cmd) {
	def event = createEvent(name: "WakeUp", value: "Auto Wakeup", descriptionText: "${device.displayName} woke up", isStateChange: true, displayed: true)
	def cmds = []

	if (!device.currentState("ManufacturerCode")) {
		cmds << zwave.manufacturerSpecificV1.manufacturerSpecificGet().format()
		cmds << "delay 2000"
	}
	if (!state.lastbat || now() - state.lastbat > 23*60*60*1000) {  //check no sooner than once every 23 hours (once a day)
		log.debug "checking battery"
		event.descriptionText += ", requesting battery"
		cmds << zwave.batteryV1.batteryGet().format()
		cmds << "delay 2000"
	} else {
		log.debug "not checking battery, was updated ${(now() - state.lastbat)/60000 as int} min ago"
	}
	if (device.currentValue("needUpdate") == "YES") { cmds += update_settings() }
	cmds << zwave.wakeUpV2.wakeUpNoMoreInformation().format()
	return [event, response(cmds)]
}

def zwaveEvent(physicalgraph.zwave.commands.batteryv1.BatteryReport cmd) {
  def map = [ name: "battery", unit: "%" ]
	if (cmd.batteryLevel == 0xFF) {
		map.value = 1
		map.descriptionText = "${device.displayName} has a low battery"
		map.isStateChange = true
	} else {
		map.value = cmd.batteryLevel
	}
	def event = createEvent(map)
	map.isStateChange = true
	state.lastbat = now()
	return [event]
}

def zwaveEvent(physicalgraph.zwave.commands.manufacturerspecificv1.ManufacturerSpecificReport cmd) {
	def result = []
	def manufacturerCode = String.format("%04X", cmd.manufacturerId)
	def productTypeCode = String.format("%04X", cmd.productTypeId)
	def productCode = String.format("%04X", cmd.productId)
	def wirelessConfig = "ZWP"

	updateDataValue("Manufacturer", cmd.manufacturerName)
	updateDataValue("Manufacturer ID", manufacturerCode)
	updateDataValue("Product Type", productTypeCode)
	updateDataValue("Product Code", productCode)
	return result
}

def zwaveEvent(physicalgraph.zwave.Command cmd) {
	return [createEvent(descriptionText: "$device.displayName: $cmd", displayed: false)]
}

private secure(physicalgraph.zwave.Command cmd) {
	if (state.sec == 0) {  // default to secure
		cmd.format()
	} else {
		zwave.securityV1.securityMessageEncapsulation().encapsulate(cmd).format()
	}
}

private secureSequence(commands, delay=200) {
	delayBetween(commands.collect{ secure(it) }, delay)
}

def buildInterval() {
	def intervalList = []
   	intervalList << [ "0" : "Disabled" ]
	intervalList << [ "1800" : "30 minutes" ]
	intervalList << [ "3600" : "1 hour" ]
	intervalList << [ "7200" : "2 hours" ]
	intervalList << [ "10800" : "3 hours" ]
	intervalList << [ "14400" : "4 hours" ]
	intervalList << [ "18000" : "5 hours" ]
	intervalList << [ "21600" : "6 hours" ]
	intervalList << [ "36000" : "10 hours" ]
	intervalList << [ "43200" : "12 hours" ]
	intervalList << [ "86400" : "24 hours" ]
}

def temperatureLevelInterval() {
	def intervalList = []
   	intervalList << [ "-20" : "-20°C / -4°F" ]
	intervalList << [ "-15" : "-15°C / 5°F" ]
	intervalList << [ "-10" : "-10°C / 14°F" ]
	intervalList << [ "-5" : "-5°C / 23°F" ]
	intervalList << [ "0" : "0°C / 32°F" ]
	intervalList << [ "5" : "5°C / 41°F" ]
	intervalList << [ "10" : "10°C / 50°F" ]
	intervalList << [ "15" : "15°C / 59°F" ]
	intervalList << [ "20" : "20°C / 68°F" ]
	intervalList << [ "25" : "25°C / 77°F" ]
	intervalList << [ "30" : "30°C / 86°F" ]
    intervalList << [ "35" : "35°C / 95°F" ]
	intervalList << [ "40" : "40°C / 104°F" ]
	intervalList << [ "45" : "45°C / 113°F" ]
	intervalList << [ "50" : "50°C / 122°F" ]
	intervalList << [ "55" : "55°C / 131°F" ]
	intervalList << [ "60" : "60°C / 140°F" ]
}

def highAmbientLightLevelInterval() {
	def intervalList = []
   	intervalList << [ "3" : "3" ]	
	intervalList << [ "100" : "100" ]
	intervalList << [ "500" : "500" ]
	intervalList << [ "1000" : "1000" ]
	intervalList << [ "5000" : "5000" ]
	intervalList << [ "10000" : "10000" ]	
	intervalList << [ "20000" : "20000" ]	
	intervalList << [ "30000" : "30000" ]    
	intervalList << [ "40000" : "40000" ]
	intervalList << [ "50000" : "50000" ]
	intervalList << [ "60000" : "60000" ]
	intervalList << [ "64000" : "64000" ]
}

def lowAmbientLightLevelInterval() {
	def intervalList = []
    intervalList << [ "1" : "1" ]	
	intervalList << [ "100" : "100" ]
	intervalList << [ "500" : "500" ]
	intervalList << [ "1000" : "1000" ]
	intervalList << [ "5000" : "5000" ]
	intervalList << [ "10000" : "10000" ]	
	intervalList << [ "20000" : "20000" ]	
	intervalList << [ "30000" : "30000" ]    
	intervalList << [ "40000" : "40000" ]
	intervalList << [ "42000" : "42000" ]
}

def leakageLevelInterval() {
//to be double-checked
	def intervalList = []
   	intervalList << [ "1" : "1% - Dry" ]
    intervalList << [ "25" : "25%" ]
	intervalList << [ "50" : "50%" ]
	intervalList << [ "100" : "100% - Soaking wet" ]
}

def moistureReportingInterval()  {
	def intervalList = []
   	intervalList << [ "0" : "Disabled" ]
	intervalList << [ "1" : "1 hour" ]
	intervalList << [ "2" : "2 hours" ]
    intervalList << [ "3" : "3 hours" ]
	intervalList << [ "4" : "4 hours" ]
    intervalList << [ "8" : "8 hours" ]
    intervalList << [ "12" : "12 hours" ]
    intervalList << [ "24" : "1 day" ]
    intervalList << [ "48" : "2 days" ]
    intervalList << [ "120" : "5 days" ]    
}


def doWakeup() {	
	def cmds = []
	cmds << "delay 2000"
	if (device.currentValue("needUpdate") == "YES") { cmds += update_settings() }
	cmds << zwave.wakeUpV2.wakeUpNoMoreInformation().format()
	return response(cmds)
}

def resetTempAlarm() {
 sendEvent(name: "tempAlarm", value: "No\nHeating\nAlarm", displayed: false)
}

def resetLeakageAlarm() {
 sendEvent(name: "leakageAlarm", value: "No\nLeakage\nAlarm", displayed: false)
}

def refresh() {
 sendEvent(name: "tempAlarm", value: "No\nHeating\nAlarm", displayed: false)
 sendEvent(name: "leakageAlarm", value: "No\nLeakage\nAlarm", displayed: false)
 sendEvent(name: "moisture", value: "0", displayed: false)

 doWakeup()
}

def zwaveEvent(physicalgraph.zwave.commands.versionv1.VersionReport cmd) {
	def appversion = String.format("%02d.%02d", cmd.applicationVersion, cmd.applicationSubVersion)
	def zprotoversion = String.format("%d.%02d", cmd.zWaveProtocolVersion, cmd.zWaveProtocolSubVersion)
	updateDataValue("zWave Library", cmd.zWaveLibraryType.toString())
	updateDataValue("Firmware", appversion)
	updateDataValue("zWave Version", zprotoversion)
	sendEvent(name: "Firmware", value: appversion, displayed: true)
}

def zwaveEvent(physicalgraph.zwave.commands.wakeupv2.WakeUpIntervalReport cmd) {
	state.wakeupInterval = cmd.seconds.toString()
	sendEvent(name: "wakeupInterval", value: state.wakeupInterval, displayed: true)
}

def zwaveEvent(physicalgraph.zwave.commands.sensormultilevelv5.SensorMultilevelReport cmd)
{
        def map = [ displayed: true, value: cmd.scaledSensorValue.toString() ]
        switch (cmd.sensorType) {
                case 1:
                        map.name = "temperature"
                        map.unit = cmd.scale == 1 ? "F" : "C"
                        break;
                case 3:
                        map.name = "illuminance"
                        map.value = cmd.scaledSensorValue.toInteger().toString()
                        map.unit = "lux"
                        break;

                case 0x1F:
                        map.name = "moisture"
                        map.value = cmd.scaledSensorValue.toInteger().toString()
                        map.unit = cmd.scale == 0 ? "%" : ""
                        break;
        }
        createEvent(map)
}

def threeSeconds() {
	def cmds = []
    cmds << zwave.wakeUpV2.wakeUpIntervalSet(seconds: wakeupInterval.toInteger(), nodeid:zwaveHubNodeId).format()
	cmds << "delay 1000"
	cmds << zwave.wakeUpV2.wakeUpIntervalGet().format()	
	sendEvent(name:"needUpdate", value: "3", displayed:false, isStateChange: true)
}

def twoSeconds() {
	def cmds = []
    cmds << zwave.wakeUpV2.wakeUpIntervalSet(seconds: wakeupInterval.toInteger(), nodeid:zwaveHubNodeId).format()
	cmds << "delay 1000"
	cmds << zwave.wakeUpV2.wakeUpIntervalGet().format()	
	sendEvent(name:"needUpdate", value: "2", displayed:false, isStateChange: true)
}

def oneSecond() {
	def cmds = []
    cmds << zwave.wakeUpV2.wakeUpIntervalSet(seconds: wakeupInterval.toInteger(), nodeid:zwaveHubNodeId).format()
	cmds << "delay 1000"
	cmds << zwave.wakeUpV2.wakeUpIntervalGet().format()	
	sendEvent(name:"needUpdate", value: "1", displayed:false, isStateChange: true)
}

def zeroSecond() {
	def cmds = []
    cmds << zwave.wakeUpV2.wakeUpIntervalSet(seconds: wakeupInterval.toInteger(), nodeid:zwaveHubNodeId).format()
	cmds << "delay 1000"
	cmds << zwave.wakeUpV2.wakeUpIntervalGet().format()	
	sendEvent(name:"needUpdate", value: "0", displayed:false, isStateChange: true)
}