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
 *  Modified by eric.ycs@gmail.com
 */
metadata
{
	definition (name: "Aeon Multisensor 4 G5", namespace: "ericyew", author: "ericyew")
    {
		capability "Motion Sensor"
		capability "Temperature Measurement"
		capability "Relative Humidity Measurement"
		capability "Illuminance Measurement"
		capability "Configuration"
        command "logconfig"
		capability "Sensor"
		capability "Battery"
        capability "Refresh"

		fingerprint deviceId: "0x0701", inClusters: "0x5E,0x86,0x72,0x59,0x85,0x73,0x71,0x84,0x80,0x30,0x31,0x70,0x98,0x7A", outClusters:"0x5A"
	}
    
    preferences
    {        
    	//motion
        //input "MotionStopMinutes", "number", title: "Minutes until no-motion is reported:", required: true, displayDuringSetup: true, defaultValue: 1
        input "MotionStopSeconds", "number", title: "Seconds until no-motion is reported:", required: true, displayDuringSetup: true, defaultValue: 60

		//group 1
        input "ReportInterval1", "number", title: "Report Group 1 items every x minutes:", required: true, displayDuringSetup: true, defaultValue: 30
        input "ReportTemperature1", "bool", title: "Report temperature in Group 1?", required: true, displayDuringSetup: true, defaultValue: true
        input "ReportHumidity1", "bool", title: "Report humidity in Group 1?", required: true, displayDuringSetup: true, defaultValue: true
        input "ReportIlluminance1", "bool", title: "Report illuminance in Group 1?", required: true, displayDuringSetup: true, defaultValue: true
        input "ReportBattery1", "bool", title: "Report battery level in Group 1?", required: true, displayDuringSetup: true, defaultValue: true
	     
        //battery
        input "ReportLowBattery", "bool", title: "Report low battery warnings", required: true, displayDuringSetup: true, defaultValue: true
    }

	simulator
    {
		status "no motion" : "command: 9881, payload: 00300300"
		status "motion"    : "command: 9881, payload: 003003FF"

		for (int i = 0; i <= 100; i += 20) {
			status "temperature ${i}F": new physicalgraph.zwave.Zwave().securityV1.securityMessageEncapsulation().encapsulate(
				new physicalgraph.zwave.Zwave().sensorMultilevelV2.sensorMultilevelReport(
					scaledSensorValue: i, precision: 1, sensorType: 1, scale: 1)
				).incomingMessage()
		}

		for (int i = 0; i <= 100; i += 20) {
			status "humidity ${i}%":  new physicalgraph.zwave.Zwave().securityV1.securityMessageEncapsulation().encapsulate(
				new physicalgraph.zwave.Zwave().sensorMultilevelV2.sensorMultilevelReport(scaledSensorValue: i, sensorType: 5)
			).incomingMessage()
		}

		for (int i in [0, 20, 89, 100, 200, 500, 1000]) {
			status "illuminance ${i} lux":  new physicalgraph.zwave.Zwave().securityV1.securityMessageEncapsulation().encapsulate(
				new physicalgraph.zwave.Zwave().sensorMultilevelV2.sensorMultilevelReport(scaledSensorValue: i, sensorType: 3)
			).incomingMessage()
		}

		for (int i in [0, 5, 10, 15, 50, 99, 100]) {
			status "battery ${i}%":  new physicalgraph.zwave.Zwave().securityV1.securityMessageEncapsulation().encapsulate(
				new physicalgraph.zwave.Zwave().batteryV1.batteryReport(batteryLevel: i)
			).incomingMessage()
		}
		status "low battery alert":  new physicalgraph.zwave.Zwave().securityV1.securityMessageEncapsulation().encapsulate(
			new physicalgraph.zwave.Zwave().batteryV1.batteryReport(batteryLevel: 255)
		).incomingMessage()

		status "wake up" : "command: 8407, payload: "
	}

	tiles(scale: 2)
    {
		multiAttributeTile(name:"motion", type: "generic", width: 6, height: 4){
			tileAttribute ("device.motion", key: "PRIMARY_CONTROL") {
				attributeState "active", label:'motion', icon:"st.motion.motion.active", backgroundColor:"#53a7c0"
				attributeState "inactive", label:'no motion', icon:"st.motion.motion.inactive", backgroundColor:"#ffffff"
			}
			tileAttribute ("device.battery", key: "SECONDARY_CONTROL") {
				attributeState "battery", label:'${currentValue}% battery'
			}
		}
		valueTile("temperature", "device.temperature", inactiveLabel: false, width: 2, height: 2) {
			state "temperature", label:'${currentValue}°',
			backgroundColors:[
				[value: 32, color: "#153591"],
				[value: 44, color: "#1e9cbb"],
				[value: 59, color: "#90d2a7"],
				[value: 74, color: "#44b621"],
				[value: 84, color: "#f1d801"],
				[value: 92, color: "#d04e00"],
				[value: 98, color: "#bc2323"]
			]
		}
		valueTile("humidity", "device.humidity", inactiveLabel: false, width: 2, height: 2) {
			state "humidity", label:'${currentValue}% humidity', unit:""
		}
		valueTile("illuminance", "device.illuminance", inactiveLabel: false, width: 2, height: 2) {
			state "luminosity", label:'${currentValue} lux', unit:"lux"
		}
        standardTile("configure", "device.configure", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
			state "configure", label:'', action:"configure", icon:"st.secondary.configure"
		}
        standardTile("logconfig", "device.logconfig", width: 2, height: 2)
        {
        	state "default", label: "logs", action: "logconfig" //, inactiveLabel: true
        }
		standardTile("refresh", "device.refresh", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
			state "default", label:'', action:"refresh.refresh", icon:"st.secondary.refresh"
		}

		main(["motion", "temperature", "humidity", "illuminance"])
		details(["motion", "temperature", "humidity", "illuminance", "battery", "configure", "logconfig", "refresh"])
	}
}

def parse(String description)
{
	def result = null
	if (description == "updated") {
		result = null
	} else {
		def cmd = zwave.parse(description, [0x31: 5, 0x30: 2, 0x70: 1, 0x84: 2])
		if (cmd) {
            result = zwaveEvent(cmd)
		}
	}
	//log.debug "Parsed '${description}' to ${result.inspect()}"
	return result
}

def zwaveEvent(physicalgraph.zwave.commands.wakeupv1.WakeUpNotification cmd)
{
	def result = [createEvent(descriptionText: "${device.displayName} woke up", isStateChange: false)]

	if (!isConfigured()) {
		// we're still in the process of configuring a newly joined device
		log.debug("not sending wakeUpNoMoreInformation yet")
        //result += response(["delay 20000"] + configure())
	} else {
		result += response(zwave.wakeUpV1.wakeUpNoMoreInformation())
	}
	return result
}

def zwaveEvent(physicalgraph.zwave.commands.securityv1.SecurityMessageEncapsulation cmd) {
	def encapsulatedCommand = cmd.encapsulatedCommand([0x31: 5, 0x30: 2, 0x70: 1, 0x84: 2])
	// log.debug "encapsulated: ${encapsulatedCommand}"
	if (encapsulatedCommand) {
		zwaveEvent(encapsulatedCommand)
	} else {
		log.warn "Unable to extract encapsulated cmd from $cmd"
		createEvent(descriptionText: cmd.toString())
	}
}

def zwaveEvent(physicalgraph.zwave.commands.securityv1.SecurityCommandsSupportedReport cmd) {
    // log.debug "Received SecurityCommandsSupportedReport"
	response(["delay 10000"] + configure())
}

def zwaveEvent(physicalgraph.zwave.commands.batteryv1.BatteryReport cmd) {
	if((cmd.batteryLevel == 0x00 || cmd.batteryLevel == 0xFF) && !ReportLowBattery) { return }
	def map = [ name: "battery", unit: "%" ]
	if (cmd.batteryLevel == 0xFF) {
		map.value = 1
		map.descriptionText = "${device.displayName} battery is low"
		map.isStateChange = true
	} else {
		map.value = cmd.batteryLevel
	}
	state.lastbatt = new Date().time
	createEvent(map)
}

def zwaveEvent(physicalgraph.zwave.commands.sensormultilevelv5.SensorMultilevelReport cmd)
{
	def map = [:]
	switch (cmd.sensorType) {
		case 1:
			map.name = "temperature"
			def cmdScale = cmd.scale == 1 ? "F" : "C"
			map.value = convertTemperatureIfNeeded(cmd.scaledSensorValue, cmdScale, cmd.precision)
			map.unit = getTemperatureScale()
			break;
		case 3:
			map.name = "illuminance"
			map.value = cmd.scaledSensorValue.toInteger()
			map.unit = "lux"
			break;
		case 5:
			map.name = "humidity"
			map.value = cmd.scaledSensorValue.toInteger()
			map.unit = "%"
			break;
		default:
			map.descriptionText = cmd.toString()
	}
	createEvent(map)
}

def motionEvent(value) {
	def map = [name: "motion"]
	if (value) {
		map.value = "active"
		map.descriptionText = "$device.displayName detected motion"
	} else {
		map.value = "inactive"
		map.descriptionText = "$device.displayName motion has stopped"
	}
	createEvent(map)
}

def zwaveEvent(physicalgraph.zwave.commands.sensorbinaryv2.SensorBinaryReport cmd) {
	motionEvent(cmd.sensorValue)
}

def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicSet cmd) {
	motionEvent(cmd.value)
}

def zwaveEvent(physicalgraph.zwave.commands.notificationv3.NotificationReport cmd) {
	if (cmd.notificationType == 7 && cmd.event == 7) {
		motionEvent(cmd.notificationStatus)
	} else {
		createEvent(descriptionText: cmd.toString(), isStateChange: false)
	}
}

def zwaveEvent(physicalgraph.zwave.commands.configurationv1.ConfigurationReport cmd) {
	def hexval = cmd.configurationValue.collect { String.format( '%02x', it.toInteger() ) }.join()
    def decval = Integer.parseInt(hexval,16)
    def precval = decval / 256.0
    def minval = (decval / 60).toInteger()
    def secval = decval - (minval * 60)
	switch(cmd.parameterNumber)
    {
    	case 3: //motion stop seconds
        	log.info "Will transmit no-motion report after ${minval} miuntes and ${secval} seconds of no motion."
        	break
        case 4: //motion enabled
        	if(decval == 1)
            {
            	log.info "Motion sensor is enabled."
            }
            else
            {
            	log.info "Motion sensor is disabled."
            }
        	break
        case 5: //motion commandset
        	if(deval == 1)
            {
            	log.info "Sending 'basic set' commands for motion."
            }
            else if(decval == 2)
            {
            	log.info "Sending 'sensor binary report' commands for motion."
            }
            else
            {
            	log.info "Unxpected value for motion commandset. Currently 0x${hexval}"
            }
        	break
        case 40: //realtime threshold reporting enabled
        	if(decval == 1)
            {
            	log.info "Realtime (threshold) reporting is enabled."
            }
            else
            {
            	log.info "Realtime (threshold) reporting is disabled."
            }
        	break
        case 41: //temperature threshold
        	def fer_thresh = precval * 1.8
        	log.info "Temperature threshold is ${precval}C or ${fer_thresh}F."
        	break
        case 42: //humidity threshold
        	log.info "Humidity threshold is ${precval}%."
        	break
        case 43: //luminance threshold
        	log.info "Luminance threshold is ${precval} LUX."
        	break
        case 44: //battery threshold
        	log.info "Battery threshold is ${precval}%."
        	break
        case 101: //group 1 interval
        	log.info "Group 1 will report every ${minval} miuntes and ${secval} seconds."
        	break
        case 102: //group 2 interval
        	log.info "Group 2 will report every ${minval} miuntes and ${secval} seconds."
        	break
        case 103: //group 3 interval
        	log.info "Group 3 will report every ${minval} miuntes and ${secval} seconds."
        	break
        case 111: //group 1 members
        	log.info "Group 1 report members are ${ReportItemsToString(decval)}"
        	break
        case 112: //group 2 members
        	log.info "Group 2 report members are ${ReportItemsToString(decval)}"
        	break
        case 113: //group 3 members
        	log.info "Group 3 report members are ${ReportItemsToString(decval)}"
        	break
        case 252: //config lock
        	if(decval == 1)
            {
            	log.info "Configuration settings are locked."
            }
            else
            {
            	log.info "Configuration settings are unlocked."
            }
        	break
    	default:
        	log.info "Parameter #${cmd.parameterNumber} = 0x${hexval}"
        	break
    }
}

def zwaveEvent(physicalgraph.zwave.Command cmd) {
	createEvent(descriptionText: cmd.toString(), isStateChange: false)
}

def ReportItemsToString(int report_items)
{
	def ret = ""
    if((report_items & 128) > 0)
    {
	    def item_name = "Luminance"
    	if(ret == "")
        {
        	ret = item_name
        }
        else
        {
        	ret = "${ret}, ${item_name}"
        }
    }
    if((report_items & 64) > 0)
    {
	    def item_name = "Humidity"
    	if(ret == "")
        {
        	ret = item_name
        }
        else
        {
        	ret = "${ret}, ${item_name}"
        }
    }
    if((report_items & 32) > 0)
    {
	    def item_name = "Temperature"
    	if(ret == "")
        {
        	ret = item_name
        }
        else
        {
        	ret = "${ret}, ${item_name}"
        }
    }
    if((report_items & 1) > 0)
    {
	    def item_name = "Battery"
    	if(ret == "")
        {
        	ret = item_name
        }
        else
        {
        	ret = "${ret}, ${item_name}"
        }
    }
    return ret
}

def BuildReportGroupConfig(i)
{
	int report_interval = 30
   	int report_items = 225

    if(report_interval == 0 || report_items == 0)
    {
    	log.debug "Report Group ${i} - No Report"
        return [
            zwave.configurationV1.configurationSet(parameterNumber: 100 + i, size: 4, scaledConfigurationValue: 0),
            zwave.configurationV1.configurationSet(parameterNumber: 110 + i, size: 4, scaledConfigurationValue: 30 * 60)
        ]
    }

    log.debug "Report Group ${i} - Report ${report_items} every ${report_interval} minutes"
    return [
    	zwave.configurationV1.configurationSet(parameterNumber: 100 + i, size: 4, scaledConfigurationValue: report_items),
        zwave.configurationV1.configurationSet(parameterNumber: 110 + i, size: 4, scaledConfigurationValue: report_interval * 60)
    ]
}

def configure()
{
	log.debug "configure()"
    
    //calculate parameter values
    int motion_stop_value = 60
    
    //show values being sent
    log.debug "Report no motion after ${motion_stop_value} seconds"
    
    def request = GetSensorUpdates() +
	[
       	//unlock config in case it is locked
		zwave.configurationV1.configurationSet(parameterNumber: 252, size: 1, scaledConfigurationValue: 0)
    ] + 
	[   
		// send temperature, humidity, and illuminance every 8 minutes
        zwave.configurationV1.configurationSet(parameterNumber: 101, size: 4, scaledConfigurationValue: 224),
        zwave.configurationV1.configurationSet(parameterNumber: 111, size: 4, scaledConfigurationValue: 480), 
		// send battery every hour
		zwave.configurationV1.configurationSet(parameterNumber: 102, size: 4, scaledConfigurationValue: 1),
		zwave.configurationV1.configurationSet(parameterNumber: 112, size: 4, scaledConfigurationValue: 3600),
		// Group 3
		zwave.configurationV1.configurationSet(parameterNumber: 103, size: 4, scaledConfigurationValue: 0),
		zwave.configurationV1.configurationSet(parameterNumber: 113, size: 4, scaledConfigurationValue: 1800),
	] + 
	[
		// enable motion reporting
		zwave.configurationV1.configurationSet(parameterNumber: 4, size: 1, scaledConfigurationValue: 1),
        
		// send no-motion report x seconds after motion stops
		zwave.configurationV1.configurationSet(parameterNumber: 3, size: 2, scaledConfigurationValue: motion_stop_value),

		// send binary sensor report instead of basic set for motion
		zwave.configurationV1.configurationSet(parameterNumber: 5, size: 1, scaledConfigurationValue: 2),

		// disable notification-style motion events
		zwave.notificationV3.notificationSet(notificationType: 7, notificationStatus: 0)
    ]
    
    setConfigured()
	
    //return secureSequence(request) //+ ["delay 20000", zwave.wakeUpV1.wakeUpNoMoreInformation().format()]
    return secureSequence(request, 2000)
}

def setConfigured()
{
	device.updateDataValue("configured", "true")
}

def isConfigured()
{
	Boolean configured = device.getDataValue(["configured"]) as Boolean
	return configured
}

private secure(physicalgraph.zwave.Command cmd) {
	return zwave.securityV1.securityMessageEncapsulation().encapsulate(cmd).format()
}

private secureSequence(commands, delay=200)
{
	return delayBetween(commands.collect{ secure(it) }, delay)
}

def GetSensorUpdates()
{
	return [
    	//sensor updates
		zwave.sensorMultilevelV5.sensorMultilevelGet(sensorType: 1), //temperature
		zwave.sensorMultilevelV5.sensorMultilevelGet(sensorType: 3), //light
		zwave.sensorMultilevelV5.sensorMultilevelGet(sensorType: 5), //humidity
		zwave.sensorBinaryV2.sensorBinaryGet(sensorType: 12), //motion
		zwave.batteryV1.batteryGet() //battery
    ]
}

def GetCurrentConfig()
{
	return [
		zwave.configurationV1.configurationGet(parameterNumber: 3),
		zwave.configurationV1.configurationGet(parameterNumber: 4),
		zwave.configurationV1.configurationGet(parameterNumber: 5),
		zwave.configurationV1.configurationGet(parameterNumber: 40),
		zwave.configurationV1.configurationGet(parameterNumber: 41),
		zwave.configurationV1.configurationGet(parameterNumber: 42),
		zwave.configurationV1.configurationGet(parameterNumber: 43),
		zwave.configurationV1.configurationGet(parameterNumber: 44),
		zwave.configurationV1.configurationGet(parameterNumber: 101),
		zwave.configurationV1.configurationGet(parameterNumber: 102),
		zwave.configurationV1.configurationGet(parameterNumber: 103),
		zwave.configurationV1.configurationGet(parameterNumber: 111),
		zwave.configurationV1.configurationGet(parameterNumber: 112),
		zwave.configurationV1.configurationGet(parameterNumber: 113),
		zwave.configurationV1.configurationGet(parameterNumber: 252)
    ]
}

def refresh()
{
	log.debug "Sensor data refresh initiated"
	return secureSequence(GetSensorUpdates(), 1000)
}

def logconfig()
{
	log.debug "Getting current configuration"
    return secureSequence(GetCurrentConfig(), 2000)
}
