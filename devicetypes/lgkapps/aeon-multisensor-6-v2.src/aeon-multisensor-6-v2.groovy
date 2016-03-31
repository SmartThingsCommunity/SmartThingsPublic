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
 * LGK V1 of multisensor 6 with customizeable settings , changed some timeouts, also changed tamper to vibration so we can
 * use that as well (based on stock device type and also some changes copied from Robert Vandervoort device type.
 * Changes
   1. changes colors of temp slightly, add colors to humidity, add color to battery level
   2. remove tamper and instead use feature as contact and acceleration ie vibration sensor
   3. slightly change reporting interval times. (also fix issue where 18 hours was wrong time)
   4. add last update time.. sets when configured and whenever temp is reported. 
      This is used so at a glance you see the last update time tile to know the device is still reporting easily without looking
      at the logs.
   5. add a temp and humidity offset -12 to 12 to enable tweaking the settings if they appear off.
   6. added power status tile to display, currently was here but not displayed.
   7. added configure and refresh tiles.
   8. also added refresh capability so refresh can be forced from other smartapps like pollster. (refresh not currently working all the time for some reason)
   9. changed the sensitivity to have more values than min max etc, now is integer 0 - 127. 
   10. fix uv setting which in one release was value of 2 now it is 16.
   11. added icons for temp and humidity
   12. also change the default wakeup time to be the same as the report interval, 
        otherwise when on battery it disregarded the report interval. (only if less than the default 5 minutes).
   13. added a config option for the min change needed to report temp changes and set it in parameter 41.
   14. incresed range and colors for lux values, as when mine is in direct sun outside it goes as high as 1900
   15. support for celsius added. set in input options.
*
* version 2.
 changed range of motion sensitivity to 0 -5 and location of setting. from 6 to 4. 
 also added checking of input values
 also added parameters for humidity change amount and illumination change amount to trigger report. Also fixed the size passed into these
 ... was passing as 1 .. size is actually 2.
 Also, set wakeup when on battery - parameter 2.
 make sure to reconfigure after updating.
 
 */

metadata {
	definition (name: "Aeon Multisensor 6 V2", namespace: "lgkapps", author: "lgkapps") {
		capability "Motion Sensor"
		capability "Temperature Measurement"
		capability "Relative Humidity Measurement"
		capability "Illuminance Measurement"
		capability "Ultraviolet Index"
		capability "Configuration"
		capability "Sensor"
		capability "Battery"
   		capability "Acceleration Sensor"
   		capability "Contact Sensor"
        capability "refresh"

//		attribute "tamper", "enum", ["detected", "clear"]
		attribute "batteryStatus", "string"
		attribute "powerSupply", "enum", ["USB Cable", "Battery"]
      
		fingerprint deviceId: "0x2101", inClusters: "0x5E,0x86,0x72,0x59,0x85,0x73,0x71,0x84,0x80,0x30,0x31,0x70,0x7A", outClusters: "0x5A"
	}

	simulator {
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

		for (int i in [0, 1, 2, 8, 12, 16, 20, 24, 30, 64, 82, 100, 200, 500, 1000, 1200, 1500, 2000]) {
			status "illuminance ${i} lux":  new physicalgraph.zwave.Zwave().securityV1.securityMessageEncapsulation().encapsulate(
				new physicalgraph.zwave.Zwave().sensorMultilevelV2.sensorMultilevelReport(scaledSensorValue: i, sensorType: 3)
			).incomingMessage()
		}

		for (int i in [0, 5, 10, 15, 50, 99, 100]) {
			status "battery ${i}%":  new physicalgraph.zwave.Zwave().securityV1.securityMessageEncapsulation().encapsulate(
				new physicalgraph.zwave.Zwave().batteryV1.batteryReport(batteryLevel: i)
			).incomingMessage()
		}
        
        	for (int i = 0; i <= 11; i += 1) {
			status "ultravioletultravioletIndex ${i}": new physicalgraph.zwave.Zwave().securityV1.securityMessageEncapsulation().encapsulate(
				new physicalgraph.zwave.Zwave().sensorMultilevelV2.sensorMultilevelReport(
                scaledSensorValue: i,
                sensorType: 27
                )
			).incomingMessage()
		}
        
		status "low battery alert":  new physicalgraph.zwave.Zwave().securityV1.securityMessageEncapsulation().encapsulate(
			new physicalgraph.zwave.Zwave().batteryV1.batteryReport(batteryLevel: 255)
		).incomingMessage()

		status "wake up" : "command: 8407, payload: "
	}

	preferences {
		//input description: "Please consult AEOTEC MULTISENSOR 6 operating manual for advanced setting options. You can skip this configuration to use default settings",
		//		title: "Advanced Configuration", displayDuringSetup: true, type: "paragraph", element: "paragraph"
		input "motionDelayTime", "enum", title: "Motion Sensor Delay Time?",
				options: ["20 seconds", "30 seconds", "1 minute", "2 minutes", "3 minutes", "4 minutes"], defaultValue: "1 minute", displayDuringSetup: true
		input "motionSensitivity", "number", title: "Motion Sensor Sensitivity? (0 = off, 1 = min., 3 = normal, 5 = max.)", range: "0..5", defaultValue: 3, displayDuringSetup: true
		input "reportInterval", "enum", title: "Sensors Report Interval?",
				options: ["20 seconds", "30 seconds", "1 minute", "2 minutes", "3 minutes", "4 minutes", "5 minutes", "10 minutes", "15 minutes", "30 minutes", "1 hour", "6 hours", "12 hours", "18 hours", "24 hours"], defaultValue: "5 minutes", displayDuringSetup: true
 		input("TempOffset", "number", title: "Temperature Offset/Adjustment -10 to +10 in Degrees?",range: "-10..10", description: "If your temperature is innacurate this will offset/adjust it by this many degrees.", defaultValue: 0, required: false, displayDuringSetup: true)
   		input("HumidOffset", "number", title: "Humidity Offset/Adjustment -10 to +10 in percent?",range: "-10..10", description: "If your humidty is innacurate this will offset/adjust it by this percent.", defaultValue: 0, required: false, displayDuringSetup: true)
   		input("tempScale", "enum", title: "Temperature Scale?", options: ["F","C"], defaltValue: "F", description: "What is your temperature scale?", displayDuringSetup: true)
     	input("TempChangeAmount", "number", title: "Temperature Change Amount (1 = .1 degree)?", range: "1..70",description: "The tenths of degrees the temperature must changes before a report is sent?", defaultValue: 2,required: false)
     	input("HumidChangeAmount", "number", title: "Humidity Change Amount (1 = .1 percent)?", range: "1..70",description: "The tenths of percent the humidity must changes before a report is sent?", defaultValue: 10,required: false)
     	input("IllumChangeAmount", "number", title: "Illumination Change Amount (1 = .1 lux)?", range: "1..70",description: "The tenths of lux the light level must changes before a report is sent?", defaultValue: 10,required: false)
}

	tiles(scale: 2) {
		multiAttributeTile(name:"motion", type: "generic", width: 6, height: 4){
			tileAttribute ("device.motion", key: "PRIMARY_CONTROL") {
				attributeState "active", label:'motion', icon:"st.motion.motion.active", backgroundColor:"#53a7c0"
				attributeState "inactive", label:'no motion', icon:"st.motion.motion.inactive", backgroundColor:"#ffffff"
			}
		}
		valueTile("temperature", "device.temperature", inactiveLabel: false, width: 2, height: 2) {
			state "temperature", label:'${currentValue}°',icon:"st.Weather.weather2",
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
		}
		valueTile("humidity", "device.humidity", inactiveLabel: false, width: 2, height: 2) {
			state "humidity", label:'Humidity\n${currentValue}%', unit:"",icon: "http://cdn.device-icons.smartthings.com/Weather/weather12-icn@2x.png",
              backgroundColors : [
                    [value: 01, color: "#724529"],
                    [value: 11, color: "#724529"],
                    [value: 21, color: "#724529"],
                    [value: 35, color: "#44b621"],
                    [value: 49, color: "#44b621"],
                    [value: 50, color: "#1e9cbb"]
         ]        
		}

		valueTile("illuminance", "device.illuminance", inactiveLabel: false, width: 2, height: 2) {
			state "illuminance", label:'${currentValue} ${unit}', unit:"lux", backgroundColors:[
                	[value: 0, color: "#000000"],
                    [value: 1, color: "#060053"],
                    [value: 3, color: "#3E3900"],
                    [value: 12, color: "#8E8400"],
					[value: 24, color: "#C5C08B"],
					[value: 36, color: "#DAD7B6"],
					[value: 128, color: "#F3F2E9"],
                    [value: 1000, color: "#f1d801"],
                    [value: 1500, color: "#d04e00"],
					[value: 2000, color: "#bc2323"]
               ]
		}

		valueTile("ultravioletIndex", "device.ultravioletIndex", inactiveLabel: false, width: 2, height: 2) {
			state "ultravioletIndex", label:'${currentValue} UV index', unit:""
		}

		valueTile("battery", "device.battery", inactiveLabel: false, width: 2, height: 2) {
			state "battery", label:'${currentValue}% battery', unit:"",
             backgroundColors : [
                    [value: 20, color: "#720000"],
                    [value: 40, color: "#724529"],
                    [value: 60, color: "#00cccc"],
                    [value: 80, color: "#00b621"],
                    [value: 90, color: "#009c00"],
                    [value: 100, color: "#00ff00"]
             ]
		}

		valueTile("batteryStatus", "device.batteryStatus", inactiveLabel: false, width: 2, height: 2) {
			state "batteryStatus", label:'${currentValue}', unit:"", 
              backgroundColors : [
                    [value: 20, color: "#720000"],
                    [value: 40, color: "#724529"],
                    [value: 60, color: "#00cccc"],
                    [value: 80, color: "#00b621"],
                    [value: 90, color: "#009c00"],
                    [value: 100, color: "#00ff00"]
             ]
		}

		valueTile("powerSupply", "device.powerSupply", height: 2, width: 2, decoration: "flat") {
			state "powerSupply", label:'${currentValue} powered', backgroundColor:"#ffffff"
		}


		valueTile("acceleration","device.acceleration", width: 2, height: 2){
				state "active", label:'${name}', icon:"st.motion.acceleration.active", backgroundColor:"#53a7c0"
				state "inactive", label:'${name}', icon:"st.motion.acceleration.inactive", backgroundColor:"#44b621"
		}

		standardTile("contact", "device.contact", width: 2, height: 2) {
			state "open", label: '${name}', icon: "st.contact.contact.open", backgroundColor: "#ffa81e"
			state "closed", label: '${name}', icon: "st.contact.contact.closed", backgroundColor: "#44b621"
		}

		valueTile("status", "device.lastUpdate", width: 4, height: 1,decoration: "flat") {
			state "default", label: 'Last Update: ${currentValue}'
		}
	
	standardTile( "configure", "device.configure", inactiveLabel: false, decoration: "flat", height: 2, width: 2 ) {
			state( "configure", label:'', action:"configure", icon:"st.secondary.configure" )
		}
           	
	standardTile("refresh", "device.temperature", inactiveLabel: false, decoration: "flat", height: 2, width: 2) {
			state "default", label:"", action:"refresh.refresh", icon:"st.secondary.refresh"
		}
        
		main(["motion", "temperature", "humidity", "illuminance", "ultravioletIndex", "acceleration",])
		details(["motion", "temperature", "humidity", "illuminance", "ultravioletIndex", "acceleration", "battery", "refresh", "configure", "powerSupply", "status"])
	}
}

def updated() {
	log.debug "In Updated with settings: ${settings}"
	log.debug "${device.displayName} is now ${device.latestValue("powerSupply")}"  
    
if (settings.motionDelayTime == null)
  settings.motionDelayTime = 60
if (settings.reportInterval == null)
  settings.reportInterval = 8*60
if (settings.motionSensitivity == null)
  settings.motionSensitivity = 3
if (settings.TempOffset == null)
  settings.TempOffset = 0
if (settings.HumidOffset == null)
  settings.HumidOffset = 0
if (settings.tempScale == null)
  settings.tempScale = "F"
if (settings.TempChangeAmount == null)
  settings.TempChangeAmount = 2
if (settings.HumidChangeAmount == null)
  settings.HumidChangeAmount = 10
if (settings.IllumChangeAmount == null)
  settings.IllumChangeAmount = 10
  
  if (settings.motionSensitivity < 0)
    {
      log.debug "Illegal motion sensitivity ... resetting to 0!"
      settings.motionSensitivity = 0
    }
    
   if (settings.motionSensitivity > 5)
    {
      log.debug "Illegal motion sensitivity ... resetting to 5!"
      settings.motionSensitivity = 5
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

     // fix temp offset
 if (settings.HumidOffset < -10)
  {
    settings.HumidOffset = -10
    log.debug "Humidity Offset too low... resetting to -10"
    }
    
 if (settings.HumidOffset > 10)
  {
    settings.HumidOffset = 10
    log.debug "Humidity Adjusment too high ... resetting to 10"
    }

    // fix sensitivities for report
 if (settings.TempChangeAmount < 1)
  {
    settings.TempChangeAmount = 1
    log.debug "Temperature reporting sensitivity too low... resetting to 1"
    }
    
 if (settings.TempChangeAmount > 70)
  {
    settings.TempChangeAmount = 70
    log.debug "Temperature reporting sensitivity too high... resetting to 70"
    }
 
    // fix sensitivities for report
 if (settings.IllumChangeAmount < 1)
  {
    settings.IllumChangeAmount = 1
    log.debug "Illumination reporting sensitivity too low... resetting to 1"
    }
    
 if (settings.IllumChangeAmount > 70)
  {
    settings.IllumChangeAmount = 70
    log.debug "Illumination reporting sensitivity too high... resetting to 70"
    }

   // fix sensitivities for report
 if (settings.HumidChangeAmount < 1)
  {
    settings.HumidChangeAmount = -10
    log.debug "Humidity reporting sensitivity too low... resetting to 1"
    }
    
 if (settings.HumidChangeAmount > 70)
  {
    settings.HumidChangeAmount = 70
    log.debug "Humidity reporting sensitivity too high... resetting to 70"
    }

 
	if (device.latestValue("powerSupply") == "USB Cable") {  //case1: USB powered
		response(configure())
	} else if (device.latestValue("powerSupply") == "Battery") {  //case2: battery powered
		// setConfigured("false") is used by WakeUpNotification
		setConfigured("false") //wait until the next time device wakeup to send configure command after user change preference
	} else { //case3: power source is not identified, ask user to properly pair the sensor again
		log.warn "power source is not identified, check it sensor is powered by USB, if so > configure()"
		def request = []
		request << zwave.configurationV1.configurationGet(parameterNumber: 101)
		response(commands(request))
	}
    return(configure())
}

def parse(String description) {
	log.debug "parse() >> description: ${description}"
	def result = null
	if (description.startsWith("Err 106")) {
		log.debug "parse() >> Err 106"
		result = createEvent( name: "secureInclusion", value: "failed", isStateChange: true,
				descriptionText: "This sensor failed to complete the network security key exchange. If you are unable to control it via SmartThings, you must remove it from your network and add it again.")
	} else if (description != "updated") {
		//log.debug "parse3() >> zwave.parse(description)"
		def cmd = zwave.parse(description, [0x31: 5, 0x30: 2, 0x84: 1])
		if (cmd) {
			result = zwaveEvent(cmd)
		}
	}
	//log.debug "After zwaveEvent(cmd) >> Parsed '${description}' to ${result.inspect()}"
	return result
}

//this notification will be sent only when device is battery powered
def zwaveEvent(physicalgraph.zwave.commands.wakeupv1.WakeUpNotification cmd) {
log.debug "device woke up!"
	def result = [createEvent(descriptionText: "${device.displayName} woke up", isStateChange: false)]
	def cmds = []
	if (!isConfigured()) {
		log.debug("late configure")
		result << response(configure())
	} else {
		//log.debug("Device has been configured sending >> wakeUpNoMoreInformation()")
		cmds << zwave.wakeUpV1.wakeUpNoMoreInformation().format()
		result << response(cmds)
	}
	result
}

def zwaveEvent(physicalgraph.zwave.commands.securityv1.SecurityMessageEncapsulation cmd) {
	def encapsulatedCommand = cmd.encapsulatedCommand([0x31: 5, 0x30: 2, 0x84: 1])
	state.sec = 1
	log.debug "encapsulated: ${encapsulatedCommand}"
	if (encapsulatedCommand) {
		zwaveEvent(encapsulatedCommand)
	} else {
		log.warn "Unable to extract encapsulated cmd from $cmd"
		createEvent(descriptionText: cmd.toString())
	}
}

def zwaveEvent(physicalgraph.zwave.commands.securityv1.SecurityCommandsSupportedReport cmd) {
	log.info "Executing zwaveEvent 98 (SecurityV1): 03 (SecurityCommandsSupportedReport) with cmd: $cmd"
	state.sec = 1
}

def zwaveEvent(physicalgraph.zwave.commands.securityv1.NetworkKeyVerify cmd) {
	state.sec = 1
	log.info "Executing zwaveEvent 98 (SecurityV1): 07 (NetworkKeyVerify) with cmd: $cmd (node is securely included)"
	def result = [createEvent(name:"secureInclusion", value:"success", descriptionText:"Secure inclusion was successful", isStateChange: true)]
	result
}

def zwaveEvent(physicalgraph.zwave.commands.manufacturerspecificv2.ManufacturerSpecificReport cmd) {
 log.debug "in manuf specific cmd = $cmd"
 
	log.info "Executing zwaveEvent 72 (ManufacturerSpecificV2) : 05 (ManufacturerSpecificReport) with cmd: $cmd"
	log.debug "manufacturerId:   ${cmd.manufacturerId}"
	log.debug "manufacturerName: ${cmd.manufacturerName}"
	log.debug "productId:        ${cmd.productId}"
	log.debug "productTypeId:    ${cmd.productTypeId}"
	def msr = String.format("%04X-%04X-%04X", cmd.manufacturerId, cmd.productTypeId, cmd.productId)
	updateDataValue("MSR", msr)
}

def zwaveEvent(physicalgraph.zwave.commands.batteryv1.BatteryReport cmd) {
	def result = []
	def map = [ name: "battery", unit: "%" ]
	if (cmd.batteryLevel == 0xFF) {
		map.value = 1
		map.descriptionText = "${device.displayName} battery is low"
		map.isStateChange = true
	} else {
		map.value = cmd.batteryLevel
	}
	state.lastbatt = now()
	result << createEvent(map)
	if (device.latestValue("powerSupply") != "USB Cable"){
		result << createEvent(name: "batteryStatus", value: "${map.value} % battery", displayed: false)
	}
	result
}

def zwaveEvent(physicalgraph.zwave.commands.sensormultilevelv5.SensorMultilevelReport cmd){
log.debug "\nin multi level report cmd = $cmd\n"
 
	def map = [:]
	switch (cmd.sensorType) {
		case 1:
            log.debug "raw temp = $cmd.scaledSensorValue"
          
             def now = new Date().format('MM/dd/yyyy h:mm a',location.timeZone)
      
            sendEvent(name: "lastUpdate", value: now, descriptionText: "Last Update: $now")
            
            BigDecimal offset = settings.TempOffset
            // now way to change to c in device so do differently
            def startval =convertTemperatureIfNeeded(cmd.scaledSensorValue, cmd.scale == 1 ? "F" : "C", cmd.precision)
            if (settings.tempScale == "C" && cmd.scale == 0)
               startval = cmd.scaledSensorValue
               
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
            map.unit = getTemperatureScale()
			map.name = "temperature"
			break
		case 3:
            log.debug "raw illuminance = $cmd.scaledSensorValue"
			map.name = "illuminance"
			map.value = cmd.scaledSensorValue.toInteger()
			map.unit = "lux"
			break
		case 5:
            log.debug "raw humidity = $cmd.scaledSensorValue"
            map.value = (cmd.scaledSensorValue.toInteger() + settings.HumidOffset)
			map.unit = "%"
			map.name = "humidity"
			break
		case 27:
            log.debug "raw uv index = $cmd.scaledSensorValue"
			map.name = "ultravioletIndex"
			map.value = cmd.scaledSensorValue.toInteger()
			break
		default:
			map.descriptionText = cmd.toString()
	}
	createEvent(map)
}

def motionEvent(value) {
	def map = [name: "motion"]
	if (value) {
        log.debug "motion active"
		map.value = "active"
		map.descriptionText = "$device.displayName detected motion"
	} else {
       log.debug "motion inactive"
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
	def result = []
	if (cmd.notificationType == 7) {
		switch (cmd.event) {
			case 0:
           		sendEvent(name: "contact", value: "closed", descriptionText: "$device.displayName is closed", displayed: true)
				result << motionEvent(0)
				//result << createEvent(name: "tamper", value: "clear", displayed: false)
               	result << createEvent(name: "acceleration", value: "inactive", descriptionText: "$device.displayName is inactive", displayed: true)
				break
			case 3:
           		sendEvent(name: "contact", value: "open", descriptionText: "$device.displayName is open", displayed: true)
				//result << createEvent(name: "tamper", value: "detected", descriptionText: "$device.displayName was tampered")
                result << createEvent(name: "acceleration", value: "active", descriptionText: "$device.displayName is active", displayed: true)
				break
			case 7:
				result << motionEvent(1)
				break
		}
	} else {
		log.warn "Need to handle this cmd.notificationType: ${cmd.notificationType}"
		result << createEvent(descriptionText: cmd.toString(), isStateChange: false)
	}
	result
}

def zwaveEvent(physicalgraph.zwave.commands.configurationv2.ConfigurationReport cmd) {
 log.debug "---CONFIGURATION REPORT V2--- ${device.displayName} parameter ${cmd.parameterNumber} with a byte size of ${cmd.size} is set to ${cmd.configurationValue}"

def result = []
	def value
	if (cmd.parameterNumber == 9 && cmd.configurationValue[0] == 0) {
		value = "USB Cable"
		if (!isConfigured()) {
			log.debug("ConfigurationReport: configuring device")
			result << response(configure())
		}
		result << createEvent(name: "batteryStatus", value: value, displayed: false)
		result << createEvent(name: "powerSupply", value: value, displayed: false)
	}else if (cmd.parameterNumber == 9 && cmd.configurationValue[0] == 1) {
		value = "Battery"
		result << createEvent(name: "powerSupply", value: value, displayed: false)
	} else if (cmd.parameterNumber == 101){
		result << response(configure())
	}
	result
}

def zwaveEvent(physicalgraph.zwave.Command cmd) {
	log.debug "General zwaveEvent cmd: ${cmd}"
	createEvent(descriptionText: cmd.toString(), isStateChange: false)
}

def configure() {
	// This sensor joins as a secure device if you double-click the button to include it
	log.debug "${device.displayName} is configuring its settings"

if (settings.motionDelayTime == null)
  settings.motionDelayTime = 60
if (settings.reportInterval == null)
  settings.reportInterval = 8*60
if (settings.motionSensitivity == null)
  settings.motionSensitivity = 3
if (settings.TempOffset == null)
  settings.TempOffset = 0
if (settings.HumidOffset == null)
  settings.HumidOffset = 0

if (settings.tempScale == null)
  settings.tempScale = "F"
if (settings.TempChangeAmount == null)
  settings.TempChangeAmount = 2
if (settings.HumidChangeAmount == null)
  settings.HumidChangeAmount = 10
if (settings.IllumChangeAmount == null)
  settings.IllumChangeAmount = 10
  

  if (settings.motionSensitivity < 0)
    {
      log.debug "Illegal motion sensitivity ... resetting to 0!"
      settings.motionSensitivity = 0
    }
    
   if (settings.motionSensitivity > 5)
    {
      log.debug "Illegal motion sensitivity ... resetting to 5!"
      settings.motionSensitivity = 5
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

     // fix temp offset
 if (settings.HumidOffset < -10)
  {
    settings.HumidOffset = -10
    log.debug "Humidity Offset too low... resetting to -10"
    }
    
 if (settings.HumidOffset > 10)
  {
    settings.HumidOffset = 10
    log.debug "Humidity Adjusment too high ... resetting to 10"
    }


    // fix sensitivities for report
 if (settings.TempChangeAmount < 1)
  {
    settings.TempChangeAmount = 1
    log.debug "Temperature reporting sensitivity too low... resetting to 1"
    }
    
 if (settings.TempChangeAmount > 70)
  {
    settings.TempChangeAmount = 70
    log.debug "Temperature reporting sensitivity too high... resetting to 70"
    }
 
    // fix sensitivities for report
 if (settings.IllumChangeAmount < 1)
  {
    settings.IllumChangeAmount = 1
    log.debug "Illumination reporting sensitivity too low... resetting to 1"
    }
    
 if (settings.IllumChangeAmount > 70)
  {
    settings.IllumChangeAmount = 70
    log.debug "Illumination reporting sensitivity too high... resetting to 70"
    }

   // fix sensitivities for report
 if (settings.HumidChangeAmount < 1)
  {
    settings.HumidChangeAmount = -10
    log.debug "Humidity reporting sensitivity too low... resetting to 1"
    }
    
 if (settings.HumidChangeAmount > 70)
  {
    settings.HumidChangeAmount = 70
    log.debug "Humidity reporting sensitivity too high... resetting to 70"
    }

log.debug "In configure report interval value = $reportInterval"
log.debug "Motion Delay Time = $motionDelayTime"
log.debug "Motion Sensitivity = $motionSensitivity"
log.debug "Temperature adjust = $TempOffset"
log.debug "Humidity adjust = $HumidOffset"
log.debug "temp scale = $tempScale"
log.debug "min temp change for reporting = $TempChangeAmount"
log.debug "min humidity change for reporting = $HumidChangeAmount"
log.debug "min illumination change for reporting = $IllumChangeAmount"

   def now = new Date().format('MM/dd/yyyy h:mm a',location.timeZone)
       
sendEvent(name: "lastUpdate", value: now, descriptionText: "Configured: $now")

setConfigured("true")      
def waketime

if (timeOptionValueMap[reportInterval] < 300)
  waketime = timeOptionValueMap[reportInterval] 
 else waketime = 300
 
 log.debug "wake time reset to $waketime"

def request = [
// set wakeup interval to report time otherwise it doesnt report in time

		zwave.wakeUpV1.wakeUpIntervalSet(seconds:waketime, nodeid:zwaveHubNodeId),
		
 
	//1. set association groups for hub
	zwave.associationV1.associationSet(groupingIdentifier:1, nodeId:zwaveHubNodeId),
	zwave.associationV1.associationSet(groupingIdentifier:2, nodeId:zwaveHubNodeId),

	//2. enable wakeup when on battery
	zwave.configurationV1.configurationSet(parameterNumber: 2, size: 1, scaledConfigurationValue: 1),

	//3. no-motion report x seconds after motion stops (default 60 secs)
	zwave.configurationV1.configurationSet(parameterNumber: 3, size: 2, scaledConfigurationValue: timeOptionValueMap[motionDelayTime] ?: 60),

	//4. 0 = disabled 1 -5 5 =max no longer in parameter 6 only 4.. change
    zwave.configurationV1.configurationSet(parameterNumber: 4, size: 1, scaledConfigurationValue: motionSensitivity),

	//2. automatic report flags
    // lgk change ultraviolet is 16 not 2 
	// param 101 -103 [4 bytes] 128: light sensor, 64 humidity, 32 temperature sensor, 16 ultraviolet sensor, 1 battery sensor -> send command 241 to get all reports
	zwave.configurationV1.configurationSet(parameterNumber: 101, size: 4, scaledConfigurationValue: 241), //association group 1
	zwave.configurationV1.configurationSet(parameterNumber: 102, size: 4, scaledConfigurationValue: 1), //association group 2
     
	//5. report every x minutes 
 	zwave.configurationV1.configurationSet(parameterNumber: 111, size: 4, scaledConfigurationValue:  timeOptionValueMap[reportInterval]), //association group 1
	zwave.configurationV1.configurationGet(parameterNumber: 0x6F),
    	        
    // battery report time.. too long at  every 6 hours change to 2 hours.
	zwave.configurationV1.configurationSet(parameterNumber: 112, size: 4, scaledConfigurationValue: 2*60*60),  //association group 2

	//6. report automatically on threshold change
	zwave.configurationV1.configurationSet(parameterNumber: 40, size: 1, scaledConfigurationValue: 1),

	// min change in temp to report
    zwave.configurationV1.configurationSet(parameterNumber: 41, size: 2, scaledConfigurationValue: TempChangeAmount),

	// min change in humidity to report
    zwave.configurationV1.configurationSet(parameterNumber: 42, size: 2, scaledConfigurationValue: HumidChangeAmount),

	// min change in illumination to report
    zwave.configurationV1.configurationSet(parameterNumber: 43, size: 2, scaledConfigurationValue: IllumChangeAmount),

	// send binary sensor report for motion
		zwave.configurationV1.configurationSet(parameterNumber: 0x05, size: 1, scaledConfigurationValue: 2),
	// Enable the function of touch sensor
        zwave.configurationV1.configurationSet(parameterNumber: 0x07, size: 1, scaledConfigurationValue: 1),
	
     // configure temp offset
     // these are done directly in the reporting
		//zwave.configurationV1.configurationSet(parameterNumber: 0xC9, size: 1, scaledConfigurationValue: 1),
		//zwave.configurationV1.configurationGet(parameterNumber: 0xC9),
		
     // configure humidity offset
	//  zwave.configurationV1.configurationSet(parameterNumber: 0xCA, size: 1, scaledConfigurationValue: 01),
	//  zwave.configurationV1.configurationGet(parameterNumber: 0xCA),
	
	//7. query sensor data
    zwave.batteryV1.batteryGet(),
	zwave.sensorBinaryV2.sensorBinaryGet(),
	zwave.sensorMultilevelV5.sensorMultilevelGet(sensorType: 1), //temperature
	zwave.sensorMultilevelV5.sensorMultilevelGet(sensorType: 3), //illuminance
	zwave.sensorMultilevelV5.sensorMultilevelGet(sensorType: 5), //humidity
	zwave.sensorMultilevelV5.sensorMultilevelGet(sensorType: 27) //ultravioletIndex

  ]
	commands(request) + ["delay 20000", zwave.wakeUpV1.wakeUpNoMoreInformation().format()]

}

def refresh() {
// refresh not working for now.
	// This sensor joins as a secure device if you double-click the button to include it
	log.debug "in refresh"

 	delayBetween([
		zwave.versionV1.versionGet(),
        zwave.firmwareUpdateMdV2.firmwareMdGet(),
        zwave.configurationV1.configurationGet(parameterNumber: 4),
        zwave.batteryV1.batteryGet().format(),
        zwave.sensorBinaryV2.sensorBinaryGet(), //motion
		zwave.sensorMultilevelV5.sensorMultilevelGet(sensorType: 1), //temperature
	    zwave.sensorMultilevelV5.sensorMultilevelGet(sensorType: 3), //illuminance
		zwave.sensorMultilevelV5.sensorMultilevelGet(sensorType: 5), //humidity
		zwave.sensorMultilevelV5.sensorMultilevelGet(sensorType: 27) //ultravioletIndex
 	], 1000)
}

private def getTimeOptionValueMap() { [
		"20 seconds" : 20,
		"30 seconds" : 30,
		"1 minute"   : 60,
		"2 minutes"  : 2*60,
		"3 minutes"  : 3*60,
		"4 minutes"  : 4*60,
		"5 minutes"  : 5*60,
		"10 minutes" : 10*60,
		"15 minutes" : 15*60,
		"30 minutes" : 30*60,
		"1 hours"    : 1*60*60,
		"6 hours"    : 6*60*60,
		"12 hours"   : 12*60*60,
		"18 hours"   : 18*60*60,
		"24 hours"   : 24*60*60,
]}

private setConfigured(configure) {
	updateDataValue("configured", configure)
}

private isConfigured() {
	getDataValue("configured") == "true"
}

private command(physicalgraph.zwave.Command cmd) {
	if (state.sec) {
		zwave.securityV1.securityMessageEncapsulation().encapsulate(cmd).format()
	} else {
		cmd.format()
	}
}

private commands(commands, delay=1000) {
	log.info "sending commands: ${commands}"
	delayBetween(commands.collect{ command(it) }, delay)
}


def zwaveEvent(physicalgraph.zwave.commands.versionv1.VersionCommandClassReport cmd) {
   log.debug "in version class report"
	//if (state.debug) 
    log.debug "---COMMAND CLASS VERSION REPORT V1--- ${device.displayName} has command class version: ${cmd.commandClassVersion} - payload: ${cmd.payload}"
}

def zwaveEvent(physicalgraph.zwave.commands.versionv1.VersionReport cmd) {
    log.debug "in version report"
	def fw = "${cmd.applicationVersion}.${cmd.applicationSubVersion}"
	updateDataValue("fw", fw)
	//if (state.debug) 
    log.debug "---VERSION REPORT V1--- ${device.displayName} is running firmware version: $fw, Z-Wave version: ${cmd.zWaveProtocolVersion}.${cmd.zWaveProtocolSubVersion}"
}


def zwaveEvent(physicalgraph.zwave.commands.configurationv1.ConfigurationReport cmd) {
    //if (state.debug) 
    log.debug "---CONFIGURATION REPORT V1--- ${device.displayName} parameter ${cmd.parameterNumber} with a byte size of ${cmd.size} is set to ${cmd.configurationValue}"
}