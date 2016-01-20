/**
 *  EverSpring ST814
 *
 *  Copyright 2014 Ben (SmartThings) and Chad Monroe,
 * modified by lgkahn 2015, added parameters for timeout, and temp changes, 
 * also add color to battery percent, and icons for humidity and temp. Make primary temp display larger.
 * original timeout was 5 minutes .. This is too often when putting in cold environment like freezer.
 * That is why I made the timeout configurable. And also change the default to 180 minutes.
 *
 * Version 2. Just set my second one up and temp is innacurate so add offset temp and humidity to fix.
 * also limit temp to 1 place after the decimal. Also add more colors for temp for the lower ranges.
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
	definition (name: "EverSpring ST814", namespace: "lgkapps", author: "@Ben chad@monroe.io and lgkahn")
	{
		capability "Battery"
		capability "Temperature Measurement"
		capability "Relative Humidity Measurement"
		capability "Configuration"
		capability "Alarm"
		capability "Sensor"
        
        command "setBackLightLevel"

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

preferences {
    input("TempChangeAmount", "number", title: "Temperature Change Amount?",description: "The degrees the temperature must changes before a report is sent?", defaultValue: 2,required: true)
    input("HumidChangeAmount", "number", title: "Humidity Change Amount?",description: "The percent the humidity must changes before a report is sent?", defaultValue: 5,required: true)
    input("ReportTime", "number", title: "Report Timeout Interval?", description: "The time in minutes after which an update is sent?", defaultValue: 180, required: true)
    input("TempOffset", "number", title: "Temperature Offset/Adjustment -10 to +10 in Degrees?", description: "If your temperature is innacurate this will offset/adjust it by this many degrees.", defaultValue: 0, required: true)
    input("HumidOffset", "number", title: "Humidity Offset/Adjustment -10 to +10 in percent?", description: "If your humidty is innacurate this will offset/adjust it by this percent.", defaultValue: 0, required: true)
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
    	valueTile("temperature", "device.temperature", width: 2, height: 2) {
			state("temperature", label:'${currentValue}°',
                icon: "http://cdn.device-icons.smartthings.com/Weather/weather2-icn@2x.png",
				backgroundColors:[
                	[value: 1,  color: "#c8e3f9"],
                	[value: 10, color: "#dbdee2"],
                	[value: 20, color: "#c0d2e4"],
					[value: 32, color: "#153591"],
					[value: 44, color: "#1e9cbb"],
					[value: 59, color: "#90d2a7"],
					[value: 74, color: "#44b621"],
					[value: 84, color: "#f1d801"],
                    [value: 92, color: "#d04e00"],
					[value: 98, color: "#bc2323"]
	
				]
			)
		}
	
		valueTile("humidity", "device.humidity", inactiveLabel: false) {
			state "humidity", label:'Humidity\n${currentValue}%', unit:"",
              icon: "http://cdn.device-icons.smartthings.com/Weather/weather12-icn@2x.png",
               backgroundColors : [
                    [value: 01, color: "#724529"],
                    [value: 11, color: "#724529"],
                    [value: 21, color: "#724529"],
                    [value: 35, color: "#44b621"],
                    [value: 49, color: "#44b621"],
                    [value: 50, color: "#1e9cbb"]
         ]        
	}
    
		standardTile( "alarm", "device.alarm", inactiveLabel: false ) 
		{
			state( "ok", label:'BAT OK', action:'alarm.on', icon:"st.alarm.alarm.alarm", backgroundColor:"#00cc00" )
			state( "low", label:'BAT LOW', action:'alarm.off', icon:"st.alarm.alarm.alarm", backgroundColor:"#e86d13" )
		}

	valueTile("battery", "device.battery", inactiveLabel: false) {
			state "battery", label:'Battery\n${currentValue}%', unit:"",
             backgroundColors : [
                    [value: 20, color: "#720000"],
                    [value: 40, color: "#724529"],
                    [value: 60, color: "#00cccc"],
                    [value: 80, color: "#00b621"],
                    [value: 90, color: "#009c00"],
                    [value: 100, color: "#00ff00"]
             ]
		}
		standardTile( "configure", "device.configure", inactiveLabel: false, decoration: "flat" ) 
		{
			state( "configure", label:'', action:"configuration.configure", icon:"st.secondary.configure" )
		}

		main( ["temperature", "humidity"] )
		details( ["temperature", "humidity", "alarm","battery", "configure"] )
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
           // BigDecimal offset = settings.TempOffset
           // def startval =convertTemperatureIfNeeded(cmd.scaledSensorValue, cmd.scale == 1 ? "F" : "C", cmd.precision)
           // def thetemp = startval as BigDecimal
           // BigDecimal adjval = (thetemp + offset)
           // def dispval =  String.format("%5.1f", adjval)
            //map.value = dispval
            map.value = convertTemperatureIfNeeded(cmd.scaledSensorValue, cmd.scale == 1 ? "F" : "C", cmd.precision)
            map.unit = getTemperatureScale()
			map.name = "temperature"
			break;
		case 5:
			/* humidity */
            map.value = (cmd.scaledSensorValue.toInteger() + settings.HumidOffset)
			map.unit = "%"
			map.name = "humidity"
			break;
	}

	createEvent(map)
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
log.debug "In configure for st814 timeout value = $settings.ReportTime"
log.debug "temp change value = $settings.TempChangeAmount"
log.debug "humid change value = $settings.HumidChangeAmount"
log.debug "temp adjust = $settings.TempOffset"
log.debug "humid adjust = $settings.HumidOffset"

	delayBetween([
       	/* report in every 5 minute(s) -- lgk change all to use settings */
        /* lgk override to save battery set report as defined in preferences */
        zwave.configurationV1.configurationSet(parameterNumber: 6, size: 2, scaledConfigurationValue: settings.ReportTime).format(),

    	/* report a temperature change of 2 degree C */
        zwave.configurationV1.configurationSet(parameterNumber: 7, size: 1, scaledConfigurationValue: settings.TempChangeAmount).format(),

        /* report a humidity change of 5 percent */
        zwave.configurationV1.configurationSet(parameterNumber: 8, size: 1, scaledConfigurationValue: settings.HumidChangeAmount).format()
	]) 
    
}
   
// lgk  update function

def updated()
{
log.debug "in updated"
 
// fix interval if bad
if (settings.ReportTime <1)
  {
    settings.ReportTime =1
    log.debug "Time Interval too low... resetting to 1"
    }
    
 if (settings.ReportTime > 1439)
  {
    settings.ReportTime = 1439
    log.debug "Time Interval too high ... resetting to 1439"
    }
    
  // fix temp change amount
  
if (settings.TempChangeAmount < 1)
  {
    settings.TempChangeAmount = 1
    log.debug "Temperature Change Amount too low... resetting to 1"
    }
    
 if (settings.TempChangeAmount > 70)
  {
    settings.TempChangeAmount = 70
    log.debug "Temperature Change Amount too high ... resetting to 70"
    }
  
  // fix humidity change amount
 if (settings.HumidChangeAmount < 5)
  {
    settings.HumidChangeAmount = 5
    log.debug "Humidity Change Amount too low... resetting to 5"
    }
    
 if (settings.HumidChangeAmount > 70)
  {
    settings.HumidChangeAmount = 70
    log.debug "Humidity Change Amount too high ... resetting to 70"
    }
    
     // fix temp offset
 if (settings.TempOffset < -10)
  {
    settings.TempOffset = -10
    log.debug "Temperature Offset too low... resetting to -10"
    }
    
 if (settings.TempOffset > 10)
  {
    settings.TempOffset = 10
    log.debug "Temperature Adjusment too high ... resetting to 10"
    }
    
    response(configure())
}  
 
  