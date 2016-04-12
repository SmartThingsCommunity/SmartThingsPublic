/**
 *  EverSpring ST814
 *
 *  Copyright 2014 Ben (SmartThings) and Chad Monroe,
 * modified by lgkahn 2015, added parameters for timeout, and temp changes, 
 * also add color to battery percent, and icons for humidity and temp. Make primary temp display larger.
 * original timeout was 5 minutes .. This is too often when putting in cold environment like freezer.
 * That is why I made the timeout configurable. And also change the default to 180 minutes.
 * V4 some bug in recent release of smartthings ide made saving impossible if you make the parameters
 * required kept saying please fill out all fields, even though they were so changed it.
 *
 * Version 2. Just set my second one up and temp is innacurate so add offset temp and humidity to fix.
 * also limit temp to 1 place after the decimal. Also add more colors for temp for the lower ranges.
 *
 * version 3 lgk, added tile to show last update time, in case it is not working you will know at a glance.
 * Version 3a default values are not working correctly so put code in to set inputs if null, also put in
 * correct time zone ranges -12 to 14 according to wikipedia.
 *
 * lgk version 4 figured out how to do time without user input of time zone and works correctly for daylight saving etc.
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
	definition (name: "EverSpring ST814 V3", namespace: "lgkapps", author: "@Ben chad@monroe.io and lgkahn")
	{
		capability "Battery"
		capability "Temperature Measurement"
		capability "Relative Humidity Measurement"
		capability "Configuration"
		capability "Alarm"
		capability "Sensor"
        
        command "setBackLightLevel"
        attribute "lastUpdate", "string"

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
    input("TempChangeAmount", "number", title: "Temperature Change Amount?", range: "1..70",description: "The degrees the temperature must changes before a report is sent?", defaultValue: 2,required: false)
    input("HumidChangeAmount", "number", title: "Humidity Change Amount?",range: "5..70" ,description: "The percent the humidity must changes before a report is sent?", defaultValue: 5,required: false)
    input("ReportTime", "number", title: "Report Timeout Interval?", description: "The time in minutes after which an update is sent?", defaultValue: 180, required: false)
    input("TempOffset", "number", title: "Temperature Offset/Adjustment -10 to +10 in Degrees?",range: "-10..10", description: "If your temperature is innacurate this will offset/adjust it by this many degrees.", defaultValue: 0, required: false)
    input("HumidOffset", "number", title: "Humidity Offset/Adjustment -10 to +10 in percent?",range: "-10..10", description: "If your humidty is innacurate this will offset/adjust it by this percent.", defaultValue: 0, required: false)
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

		valueTile("status", "device.lastUpdate", width: 2, height: 1, decoration: "flat") {
			state "default", label: 'Last Update: ${currentValue}'
		}
	
		main( ["temperature", "humidity"] )
		details( ["temperature", "humidity", "alarm","battery", "configure","status"] )
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
           // log.debug "try4"
     
            BigDecimal offset = settings.TempOffset
            def startval =convertTemperatureIfNeeded(cmd.scaledSensorValue, cmd.scale == 1 ? "F" : "C", cmd.precision)
           log.debug "scaled scaled sensor value = $cmd.scaledSensorValue scale = $cmd.scale"
           log.debug "offset = $offset"
           log.debug "startval = $startval"
			def thetemp = startval as BigDecimal
            log.debug "the temp = $thetemp"
            def newValue = (Math.round(thetemp * 100) + (offset * 100)) / 100
            BigDecimal adjval = (thetemp + offset)
            def dispval =  String.format("%5.1f", adjval)
            def finalval = dispval as BigDecimal
            map.value = finalval
            //map.value = convertTemperatureIfNeeded(cmd.scaledSensorValue, cmd.scale == 1 ? "F" : "C", cmd.precision)
            map.unit = getTemperatureScale()
			map.name = "temperature"
         
            def now = new Date().format('MM/dd/yyyy h:mm a',location.timeZone)
            sendEvent(name: "lastUpdate", value: now, descriptionText: "Last Update: $now")

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

// default values not working trying to set implicitly if null

if (settings.ReportTime == null)
  settings.ReportTime = 180
if (settings.TempChangeAmount == null)
  settings.TempChangeAmount = 1
if (settings.HumidChangeAmount == null)
  settings.HumidChangeAmount = 5
if (settings.TempOffset == null)
  settings.TempOffset = 0
if (settings.HumidOffset == null)
  settings.HumidOffset = 0

  
log.debug "ST814: In configure timeout value = $settings.ReportTime"
log.debug "Temperature change value = $settings.TempChangeAmount"
log.debug "Humidity change value = $settings.HumidChangeAmount"
log.debug "Temperature adjust = $settings.TempOffset"
log.debug "Humidity adjust = $settings.HumidOffset"


     def now = new Date().format('MM/dd/yyyy h:mm a',location.timeZone)
sendEvent(name: "lastUpdate", value: now, descriptionText: "Configured: $now")
      
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
   
// lgk  update function ... ranges not working in inputs so still need this

def updated()
{
log.debug "in updated"
 
if (settings.ReportTime == null)
  settings.ReportTime = 180
if (settings.TempChangeAmount == null)
  settings.TempChangeAmount = 1
if (settings.HumidChangeAmount == null)
  settings.HumidChangeAmount = 5
if (settings.TempOffset == null)
  settings.TempOffset = 0
if (settings.HumidOffset == null)
  settings.HumidOffset = 0

  
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
 if (settings.TempOffset < -12)
  {
    settings.TempOffset = -12
    log.debug "Temperature Offset too low... resetting to -12"
    }
    
 if (settings.TempOffset > 14)
  {
    settings.TempOffset = 14
    log.debug "Temperature Adjusment too high ... resetting to 14"
    }
    
    response(configure())
}  
 
  