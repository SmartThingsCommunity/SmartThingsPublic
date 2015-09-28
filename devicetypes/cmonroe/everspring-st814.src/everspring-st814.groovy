/**
 *  EverSpring ST814
 *
 *  Copyright 2014 Ben (SmartThings) and Chad Monroe
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
metadata 
{
	definition (name: "EverSpring ST814", namespace: "cmonroe", author: "@Ben chad@monroe.io")
	{
		capability "Battery"
		capability "Temperature Measurement"
		capability "Relative Humidity Measurement"
		capability "Configuration"
		capability "Alarm"
		capability "Sensor"

		fingerprint deviceId: "0x2101", inClusters: "0x31,0x60,0x86,0x72,0x85,0x84,0x80,0x70,0x20,0x71"
        
        /**
		 * 0x31: COMMAND_CLASS_SENSOR_MULTILEVEL_V2
		 * 0x60: COMMAND_CLASS_MULTI_CHANNEL_V2
		 * 0x86: COMMAND_CLASS_VERSION
		 * 0x72: COMMAND_CLASS_MANUFACTURER_SPECIFIC
		 * 0x85: COMMAND_CLASS_ASSOCIATION_V2
		 * 0x84: COMMAND_CLASS_WAKE_UP_V2
		 * 0x80: COMMAND_CLASS_BATTERY
		 * 0x70: COMMAND_CLASS_CONFIGURATION_V2
		 * 0x20: COMMAND_CLASS_BASIC
		 * 0x71: COMMAND_CLASS_ALARM
         **/
	}

	simulator 
	{
		/* messages the device returns in response to commands it receives */
		for( int i = 0; i <= 100; i += 20 ) 
		{
			status "temperature ${i}F": new physicalgraph.zwave.Zwave().sensorMultilevelV2.sensorMultilevelReport(
				scaledSensorValue: i, precision: 1, sensorType: 1, scale: 1).incomingMessage()
		}

		for( int i = 0; i <= 100; i += 20 ) 
		{
			status "humidity ${i}%": new physicalgraph.zwave.Zwave().sensorMultilevelV2.sensorMultilevelReport(
				scaledSensorValue: i, precision: 0, sensorType: 5).incomingMessage()
		}

		for( int i = 0; i <= 100; i += 20 ) 
		{
			status "battery ${i}%": new physicalgraph.zwave.Zwave().batteryV1.batteryReport(
				batteryLevel: i).incomingMessage()
		}
	}

	tiles 
	{
		valueTile( "temperature", "device.temperature", inactiveLabel: false ) 
		{
			state( "temperature", label:'${currentValue}°',
				backgroundColors:[
					[value: 31, color: "#153591"],
					[value: 44, color: "#1e9cbb"],
					[value: 59, color: "#90d2a7"],
					[value: 74, color: "#44b621"],
					[value: 84, color: "#f1d801"],
					[value: 95, color: "#d04e00"],
					[value: 96, color: "#bc2323"]
				]
			)
		}

		valueTile( "humidity", "device.humidity", inactiveLabel: false ) 
		{
			state( "humidity", label:'${currentValue}% humidity', unit:"" )
		}

		standardTile( "alarm", "device.alarm", inactiveLabel: false ) 
		{
			state( "ok", label:'BAT OK', action:'alarm.on', icon:"st.alarm.alarm.alarm", backgroundColor:"#ffffff" )
			state( "low", label:'BAT LOW', action:'alarm.off', icon:"st.alarm.alarm.alarm", backgroundColor:"#e86d13" )
		}

		valueTile( "battery", "device.battery", inactiveLabel: false, decoration: "flat" ) 
		{
			state( "battery", label:'${currentValue}% battery', unit:"" )
		}
		
		standardTile( "configure", "device.configure", inactiveLabel: false, decoration: "flat" ) 
		{
			state( "configure", label:'', action:"configuration.configure", icon:"st.secondary.configure" )
		}

		main( ["temperature", "humidity"] )
		details( ["temperature", "humidity", "alarm", "battery", "configure"] )
	}
}

/** 
 * parse incoming device messages and generate events
 **/
def parse(String description)
{
	def result = []
	def get_battery = false
	def cmd = null
    

	if ( description == "updated" )
	{
		log.debug "event description: ${description} - updating battery status"
		get_battery = true
	}
	else
	{
		cmd = zwave.parse( description, [0x20: 1, 0x31: 2, 0x70: 1, 0x71: 1, 0x80: 1, 0x84: 2, 0x85: 2] )
	}
    
	if ( cmd != null )
	{
		if ( cmd.CMD == "8407" ) 
		{
			result << zwaveEvent( cmd )

			log.debug "cmd.CMD=8407; result=${result} - updating battery status"
			get_battery = true
		}	
		else
		{
			result << createEvent( zwaveEvent( cmd ) )
		}
	}

	if( get_battery == true ) 
	{
		def last = device.currentState( "battery" )

		/* device wakes up roughly every hour */
		def age = last ? (new Date().time - last.date.time)/60000 : 10

		log.debug "Battery status was last checked ${age} minute(s) ago"

		/* don't check too often if woken up more frequently */
		if( age >= 10 ) 
		{
			log.debug "Battery status is outdated, requesting battery report"
			result << new physicalgraph.device.HubAction(zwave.batteryV1.batteryGet().format())
		}
		
		result << new physicalgraph.device.HubAction(zwave.wakeUpV1.wakeUpNoMoreInformation().format())
	}
	
	log.debug "Parse returned: ${result} for cmd=${cmd} description=${description}"
	return result
}

/**
 * event generation 
 **/
def zwaveEvent(physicalgraph.zwave.commands.wakeupv1.WakeUpNotification cmd)
{
	[descriptionText: "${device.displayName} woke up", isStateChange: false]
}

def zwaveEvent(physicalgraph.zwave.commands.wakeupv2.WakeUpNotification cmd)
{
	[descriptionText: "${device.displayName} woke up", isStateChange: false]
}

def zwaveEvent(physicalgraph.zwave.commands.alarmv1.AlarmReport cmd)
{
	def map = [:]
    
	log.debug "AlarmReport cmd: ${cmd.toString()}}"
    
    if(( cmd.alarmType == 2 ) && ( cmd.alarmLevel == 1 ))
	{
    	log.info "${device.displayName} powered up!"
        return map
 	}
	else
    {
		/* alarmType == 1 && alarmLevel == 255 means low battery, else ok */
		map.value = cmd.alarmLevel == 255 ? "low" : "ok"
		map.name = "alarm"
	}
	
	map
}

def zwaveEvent(physicalgraph.zwave.commands.sensormultilevelv2.SensorMultilevelReport cmd)
{
	log.debug "SensorMultilevelReport cmd: ${cmd.toString()}}"

	def map = [:]
	switch( cmd.sensorType ) 
	{
		case 1:
			/* temperature */
			def cmdScale = cmd.scale == 1 ? "F" : "C"
			map.value = convertTemperatureIfNeeded( cmd.scaledSensorValue, cmdScale, cmd.precision )
			map.unit = getTemperatureScale()
			map.name = "temperature"
			break;
		case 5:
			/* humidity */
			map.value = cmd.scaledSensorValue.toInteger().toString()
			map.unit = "%"
			map.name = "humidity"
			break;
	}
    
	map
}

def zwaveEvent(physicalgraph.zwave.commands.batteryv1.BatteryReport cmd) 
{
	def map = [:]
    
	map.name = "battery"
	map.value = cmd.batteryLevel > 0 ? cmd.batteryLevel.toString() : 1
	map.unit = "%"
	map.displayed = true
    
	map
}

def zwaveEvent(physicalgraph.zwave.Command cmd) 
{
	log.debug "Catchall reached for cmd: ${cmd.toString()}"
	[:]
}

def configure() 
{
	delayBetween([
       	/* report in every 5 minute(s) */
        zwave.configurationV1.configurationSet(parameterNumber: 6, size: 2, scaledConfigurationValue: 5).format(),
    	
    	/* report a temperature change of 2 degree C */
        zwave.configurationV1.configurationSet(parameterNumber: 7, size: 1, scaledConfigurationValue: 2).format(),
        
        /* report a humidity change of 5 percent */
        zwave.configurationV1.configurationSet(parameterNumber: 8, size: 1, scaledConfigurationValue: 5).format()
	]) 
}