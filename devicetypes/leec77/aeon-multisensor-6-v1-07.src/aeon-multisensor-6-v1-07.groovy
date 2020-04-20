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
 */

metadata {
	definition (name: "Aeon Multisensor 6 (V1.07)", namespace: "LeeC77", author: "LeeC77") {
		capability "Motion Sensor"
		capability "Temperature Measurement"
		capability "Relative Humidity Measurement"
		capability "Illuminance Measurement"
		capability "Ultraviolet Index"
		capability "Configuration"
		capability "Sensor"
		capability "Battery"
		capability "Health Check"
		capability "Power Source"
		capability "Tamper Alert"

		attribute "batteryStatus", "string"

		fingerprint deviceId: "0x2101", inClusters: "0x5E,0x86,0x72,0x59,0x85,0x73,0x71,0x84,0x80,0x30,0x31,0x70,0x7A", outClusters: "0x5A"
		fingerprint deviceId: "0x2101", inClusters: "0x5E,0x86,0x72,0x59,0x85,0x73,0x71,0x84,0x80,0x30,0x31,0x70,0x7A,0x5A"
		fingerprint mfr:"0086", prod:"0102", model:"0064", deviceJoinName: "Aeon Labs MultiSensor 6"
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
// LC added 5 10 and 15 seconds option
	preferences {
    	
			input description: "Please consult AEOTEC MULTISENSOR 6 operating manual for advanced setting options. You can skip this configuration to use default settings",
				title: "Advanced Configuration", displayDuringSetup: true, type: "paragraph", element: "paragraph"
			input "motionDelayTime", "enum", title: "Motion Sensor Delay Time",
				options: ["10 seconds", "15 seconds","20 seconds", "40 seconds", "1 minute", "2 minutes", "3 minutes", "4 minutes"], defaultValue: "${motionDelayTime}", displayDuringSetup: true
			input "motionSensitivity", "enum", title: "Motion Sensor Sensitivity", options: ["off","minimum","normal","maximum"], defaultValue: "${motionSensitivity}", displayDuringSetup: true
// LC Added select for interval or selective reporting		
        	input "reportSelective", "enum", title: "Selective or Interval repoting", options: [0:"interval",1:"selective"], displayDuringSetup: true
// LC Added  seeting up of groups in reports + extended timer settings to each group           
        	def options =[128:"Luminance",64:"Humidity",32:"Temperature",16:"Ultraviolet",1:"Battery",96: "Humidity & Temperature",144: "Luminance & Ultraviolet" ]
			input "groupOne", "enum", title: "Group One report includes", options: options, displayDuringSetup: true  
            input "reportIntervalG1", "enum", title: "Group One report interval",
				options: ["5 seconds", "10 seconds", "15 seconds" ,"20 seconds", "40 seconds", "1 minute", "2 minutes", "3 minutes", "4 minutes", "5 minutes", "8 minutes", "15 minutes", "30 minutes", "1 hour", "6 hours", "12 hours", "18 hours", "24 hours"], defaultValue: "${reportInterval}", displayDuringSetup: true
        	input "groupTwo", "enum", title: "Group Two report includes", options: options, displayDuringSetup: true
            input "reportIntervalG2", "enum", title: "Group Two report interval",
				options: ["5 seconds", "10 seconds", "15 seconds" ,"20 seconds", "40 seconds", "1 minute", "2 minutes", "3 minutes", "4 minutes", "5 minutes", "8 minutes", "15 minutes", "30 minutes", "1 hour", "6 hours", "12 hours", "18 hours", "24 hours"], defaultValue: "${reportInterval}", displayDuringSetup: true
			input "groupThree", "enum", title: "Group Three report includes", options: options, displayDuringSetup: true
            input "reportIntervalG3", "enum", title: "Group Three report interval",
				options: ["5 seconds", "10 seconds", "15 seconds" ,"20 seconds", "40 seconds", "1 minute", "2 minutes", "3 minutes", "4 minutes", "5 minutes", "8 minutes", "15 minutes", "30 minutes", "1 hour", "6 hours", "12 hours", "18 hours", "24 hours"], defaultValue: "${reportInterval}", displayDuringSetup: true
//LC Added  report threshold set up
		    input "deltaTemp", "enum", title: "Temperature change to trigger selective report", 
            	options: [0:"0.0°",512:"0.2°",1024:"0.4°",1536:"0.6°",2048:"0.8°",2560:"1.0°",3072:"1.2°",3584:"1.4°",4096:"1.6°",4608:"1.8°",5120:"2.0°"], displayDuringSetup: true
			input "deltaHumid", "enum", title: "RH change to trigger selective report", 
            	options: [1:"1%",2:"2%",5:"5%",10:"10%",15:"15%°",20:"20%"], displayDuringSetup: true
            input "deltaLux", "enum", title: "Lux change to trigger selective report", 
            	options: [5:"5",10:"10",50:"50",100:"100",150:"150",200:"200",250:"250"], displayDuringSetup: true
            input "deltaBattery", "enum", title: "Battery change to trigger selective report", 
            	options: [1:"1%",2:"2%",5:"5%",10:"10%",15:"15%°",20:"20%"], displayDuringSetup: true
            input "deltaUV", "enum", title: "UV change to trigger selective report", 
            	options: [1:"1",2:"2",5:"5",10:"10",15:"15",20:"20",25:"25"], displayDuringSetup: true
        
// LC Added temp calibration and unit setting.
		
        	input "tempunit", "enum", title: "Unit for Calibartion", options: ["°C", "°F",], defaultValue: "${tempunit}", displayDuringSetup: true
        	input "tempcal", "enum", title: "Temperature Calibration (measured - true)",
				options: ["-5.0", "-4.5", "-4.0", "-3.5", "-3.0", "-2.5", "-2.0", "-1.5", "-1.0", "-0.5", "0.0", "0.5", "1.0", "1.5", "2.0", "2.5", "3.0", "3.5", "4.0", "4.5", "5.0"], defaultValue: "0.0", displayDuringSetup: true
        
    }

	tiles(scale: 2) {
		multiAttributeTile(name:"motion", type: "generic", width: 6, height: 4){
			tileAttribute ("device.motion", key: "PRIMARY_CONTROL") {
				attributeState "active", label:'motion', icon:"st.motion.motion.active", backgroundColor:"#00A0DC"
				attributeState "inactive", label:'no motion', icon:"st.motion.motion.inactive", backgroundColor:"#cccccc"
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
			state "illuminance", label:'${currentValue} lux', unit:""
		}

		valueTile("ultravioletIndex", "device.ultravioletIndex", inactiveLabel: false, width: 2, height: 2) {
			state "ultravioletIndex", label:'${currentValue} UV index', unit:""
		}

		valueTile("battery", "device.battery", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
			state "battery", label:'${currentValue}% battery', unit:""
		}

		valueTile("batteryStatus", "device.batteryStatus", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
			state "batteryStatus", label:'${currentValue}', unit:""
		}

		valueTile("powerSource", "device.powerSource", height: 2, width: 2, decoration: "flat") {
			state "powerSource", label:'${currentValue} powered', backgroundColor:"#ffffff"
		}
		valueTile("tamper", "device.tamper", height: 2, width: 2, decoration: "flat") {
			state "clear", label:'tamper clear', backgroundColor:"#ffffff"
			state "detected", label:'tampered', backgroundColor:"#ff0000"
		}

		main(["motion", "temperature", "humidity", "illuminance", "ultravioletIndex"])
		details(["motion", "temperature", "humidity", "illuminance", "ultravioletIndex", "batteryStatus", "tamper"])
	}
}

def installed(){
// Device-Watch simply pings if no device events received for 122min(checkInterval)
	sendEvent(name: "checkInterval", value: 2 * 60 * 60 + 2 * 60, displayed: false, data: [protocol: "zwave", hubHardwareId: device.hub.hardwareID])

	sendEvent(name: "tamper", value: "clear", displayed: false)
}

def updated() {
// Device-Watch simply pings if no device events received for 122min(checkInterval)
	sendEvent(name: "checkInterval", value: 2 * 60 * 60 + 2 * 60, displayed: false, data: [protocol: "zwave", hubHardwareId: device.hub.hardwareID])
	//log.debug "Updated with settings: ${settings}"
	//log.debug "${device.displayName} is now ${device.latestValue("powerSource")}"

	def powerSource = device.latestValue("powerSource")

	if (!powerSource) { // Check to see if we have updated to new powerSource attr
		def powerSupply = device.latestValue("powerSupply")

		if (powerSupply) {
			powerSource = (powerSupply == "Battery") ? "battery" : "dc"

			sendEvent(name: "powerSource", value: powerSource, displayed: false)
		}
	}
	
	if (powerSource == "battery") {
		setConfigured("false") //wait until the next time device wakeup to send configure command after user change preference
	} else { // We haven't identified the power supply, or the power supply is USB, so configure
		response(configure())
	}
}

def parse(String description) {
	// log.debug "parse() >> description: $description"   // <---- LC
	def result = null
	if (description.startsWith("Err 106")) {
		//log.debug "parse() >> Err 106"
		result = createEvent( name: "secureInclusion", value: "failed", isStateChange: true,
				descriptionText: "This sensor failed to complete the network security key exchange. If you are unable to control it via SmartThings, you must remove it from your network and add it again.")
	} else if (description != "updated") {
		//log.debug "parse() >> zwave.parse(description)"
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
	def result = [createEvent(descriptionText: "${device.displayName} woke up", isStateChange: false)]
	def cmds = []
	if (!isConfigured()) {
		//log.debug("late configure")
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
	//log.debug "encapsulated: ${encapsulatedCommand}"
	if (encapsulatedCommand) {
		zwaveEvent(encapsulatedCommand)
	} else {
		//log.warn "Unable to extract encapsulated cmd from $cmd"
		createEvent(descriptionText: cmd.toString())
	}
}

def zwaveEvent(physicalgraph.zwave.commands.securityv1.SecurityCommandsSupportedReport cmd) {
	//log.info "Executing zwaveEvent 98 (SecurityV1): 03 (SecurityCommandsSupportedReport) with cmd: $cmd"
	state.sec = 1
}

def zwaveEvent(physicalgraph.zwave.commands.securityv1.NetworkKeyVerify cmd) {
	state.sec = 1
	//log.info "Executing zwaveEvent 98 (SecurityV1): 07 (NetworkKeyVerify) with cmd: $cmd (node is securely included)"
	def result = [createEvent(name:"secureInclusion", value:"success", descriptionText:"Secure inclusion was successful", isStateChange: true)]
	result
}

def zwaveEvent(physicalgraph.zwave.commands.manufacturerspecificv2.ManufacturerSpecificReport cmd) {
	//log.info "Executing zwaveEvent 72 (ManufacturerSpecificV2) : 05 (ManufacturerSpecificReport) with cmd: $cmd"
	//log.debug "manufacturerId:   ${cmd.manufacturerId}"
	//log.debug "manufacturerName: ${cmd.manufacturerName}"
	//log.debug "productId:        ${cmd.productId}"
	//log.debug "productTypeId:    ${cmd.productTypeId}"
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
	if (device.latestValue("powerSource") != "dc"){
		result << createEvent(name: "batteryStatus", value: "${map.value}% battery", displayed: false)
	}
	result
}

def zwaveEvent(physicalgraph.zwave.commands.sensormultilevelv5.SensorMultilevelReport cmd){
	def map = [:]
	switch (cmd.sensorType) {
		case 1:
			map.name = "temperature"
			def cmdScale = cmd.scale == 1 ? "F" : "C"
			map.value = convertTemperatureIfNeeded(cmd.scaledSensorValue, cmdScale, cmd.precision)
			map.unit = getTemperatureScale()
			break
		case 3:
			map.name = "illuminance"
			map.value = cmd.scaledSensorValue.toInteger()
			map.unit = "lux"
			break
		case 5:
			map.name = "humidity"
			map.value = cmd.scaledSensorValue.toInteger()
			map.unit = "%"
			break
		case 0x1B:
			map.name = "ultravioletIndex"
			map.value = cmd.scaledSensorValue.toInteger()
			break
		default:
			map.descriptionText = cmd.toString()
	}
    log.info "${map.name}: ${map.value}${map.unit}"
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
	def result = []
	if (cmd.notificationType == 7) {
		switch (cmd.event) {
			case 0:
				result << motionEvent(0)
				result << createEvent(name: "tamper", value: "clear")
				break
			case 3:
				result << createEvent(name: "tamper", value: "detected", descriptionText: "$device.displayName was tampered")
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
	//log.debug "ConfigurationReport: $cmd"
	def result = []
	def value
	if (cmd.parameterNumber == 9 && cmd.configurationValue[0] == 0) {
		value = "dc"
		if (!isConfigured()) {
			//log.debug("ConfigurationReport: configuring device")
			result << response(configure())
		}
		result << createEvent(name: "batteryStatus", value: "USB Cable", displayed: false)
		result << createEvent(name: "powerSource", value: value, displayed: false)
	}else if (cmd.parameterNumber == 9 && cmd.configurationValue[0] == 1) {
		value = "battery"
		result << createEvent(name: "powerSource", value: value, displayed: false)
	} else if (cmd.parameterNumber == 101){
		result << response(configure())
	}
	result
}

def zwaveEvent(physicalgraph.zwave.Command cmd) {
	//log.debug "General zwaveEvent cmd: ${cmd}"
	createEvent(descriptionText: cmd.toString(), isStateChange: false)
}

/**
 * PING is used by Device-Watch in attempt to reach the Device
 * */
def ping() {
	secure(zwave.batteryV1.batteryGet())
}

def configure() {
	log.info ("Multisensor configured")
	// This sensor joins as a secure device if you double-click the button to include it
	//log.debug "${device.displayName} is configuring its settings"
	def request = []

	//1. set association groups for hub
	request << zwave.associationV1.associationSet(groupingIdentifier:1, nodeId:zwaveHubNodeId)

	request << zwave.associationV1.associationSet(groupingIdentifier:2, nodeId:zwaveHubNodeId)

	//2. automatic report flags
	// param 101 -103 [4 bytes] 128: light sensor, 64 humidity, 32 temperature sensor, 16 ultraviolet sensor, 1 battery sensor -> send command 227 to get all reports
	request << zwave.configurationV1.configurationSet(parameterNumber: 101, size: 4, scaledConfigurationValue: groupOne.toInteger() ?: 96) //association group 1 (Humidity & Temp)

	request << zwave.configurationV1.configurationSet(parameterNumber: 102, size: 4, scaledConfigurationValue: groupTwo.toInteger() ?: 1) //association group 2 (Battery)
   
    // LC Set up third group with humidity, temperature and ultraviolet
    // param 103
    request << zwave.configurationV1.configurationSet(parameterNumber: 103, size: 4, scaledConfigurationValue: groupThree.toInteger() ?: 144 ) //association group 3 (Luminance & Ultraviolet)
    
    ////

	//3. no-motion report x seconds after motion stops (default 20 secs)
	request << zwave.configurationV1.configurationSet(parameterNumber: 3, size: 2, scaledConfigurationValue: timeOptionValueMap[motionDelayTime] ?: 20)
    
    // LC
    // para 64 Unit for automatic temperature report °C or °F
    //request << zwave.configurationV1.configurationSet(parameterNumber: 64, size: 1, scaledConfigurationValue: 01) // Set to °C reporting
    // No need to set as ouput is automatically adjusted to local preferences within this App
    /*
    // LC
    // para 81 Enable PIR LED Blinking not supported by version 1.07 (1.08)
    request << zwave.configurationV1.configurationSet(parameterNumber: 81, size: 1, scaledConfigurationValue:1) // Enable
    request << zwave.configurationV1.configurationSet(parameterNumber: 81, size: 1, scaledConfigurationValue:0) // Disable
             	tempunit == "°F" ? 2: 1)
    */
	//4. motionSensitivity 4 levels: 3-normal (default), 5-maximum, 1-minimum 0-off // LC updated
	request << zwave.configurationV1.configurationSet(parameterNumber: 4, size: 1,  // parameter number changed from 6 to 4 (6 not in handbook)
			scaledConfigurationValue:
            	motionSensitivity == "off" ? 0 :  
					motionSensitivity == "normal" ? 3 :  
						motionSensitivity == "maximum" ? 5 : 
							motionSensitivity == "minimum" ? 1 : 3) 

	//5. report every x minutes (threshold reports don't work on battery power, default 8 mins)
	request << zwave.configurationV1.configurationSet(parameterNumber: 111, size: 4, scaledConfigurationValue: timeOptionValueMap[reportIntervalG1] ?: (8*60)) //association group 1
	// LC Report second group at interval 
	request << zwave.configurationV1.configurationSet(parameterNumber: 112, size: 4, scaledConfigurationValue: timeOptionValueMap[reportIntervalG2] ?: (6*60*60))  //association group 2 (6 hours)
    // LC Report third group at interval 
    request << zwave.configurationV1.configurationSet(parameterNumber: 113, size: 4, scaledConfigurationValue: timeOptionValueMap[reportIntervalG3] ?: (8*60))  //association group 3 (8 mins)
    ///
    

	//6. report automatically on threshold change
	//request << zwave.configurationV1.configurationSet(parameterNumber: 40, size: 1, scaledConfigurationValue: 1) // 0 = off 1 = on
    
    request << zwave.configurationV1.configurationSet(parameterNumber: 40, size: 1, scaledConfigurationValue: reportSelective.toInteger() ?: 0) // default to off
    
    
    //LC set up change reporting
    if (tempunit =="°C"){request << zwave.configurationV1.configurationSet(parameterNumber: 41, size: 3, scaledConfigurationValue: deltaTemp.toInteger()+1 ?: 513)} //LC 513 Report temperature only if changes by 0.2 Ox 00 02 01
    if (tempunit =="°F"){request << zwave.configurationV1.configurationSet(parameterNumber: 41, size: 3, scaledConfigurationValue: deltaTemp.toInteger()+2 ?: 514)} //LC Report temperature only if changes by 0.2 Ox 00 02 02
    // First octet is alway 00, second is temp diff. with one decimal place (20 = 2.0 02 = 0.2), third octet is 01 for C 02 for F
    //request << zwave.configurationV1.configurationSet(parameterNumber: 41, size: 2, scaledConfigurationValue: 02) //LC Report temperature only if changes by 0.2 line above is from later manual
    request << zwave.configurationV1.configurationSet(parameterNumber: 42, size: 1, scaledConfigurationValue: deltaHumid.toInteger() ?:10) //LC Report humidity only if changes by 10%
    //request << zwave.configurationV1.configurationSet(parameterNumber: 42, size: 2, scaledConfigurationValue: 1000) //LC Report humidity only if changes by 10% line above is from later manual
    request << zwave.configurationV1.configurationSet(parameterNumber: 43, size: 2, scaledConfigurationValue: deltaLux.toInteger() ?:10) //LC Report lux only if changes by 10LUX
    request << zwave.configurationV1.configurationSet(parameterNumber: 44, size: 2, scaledConfigurationValue: deltaBattery.toInteger() ?:10) //LC Report battery only if changes by 10%
    //Following command is specified as 1 byte in later manuals could possibly need to change it
	request << zwave.configurationV1.configurationSet(parameterNumber: 45, size: 1, scaledConfigurationValue: deltaUV.toInteger() ?:2) //LC Report uv only if changes by 2
    ///
    // LC : Temp cal high byte + unit C or F low byte
    // param 201
    def calTemp
    if (tempunit =="°C"){
    	calTemp= calOptionValueMap[tempcal]*256+01
        log.trace ("${tempcal}°C calibration applied")
    } 
    if (tempunit =="°F"){
    	calTemp= calOptionValueMap[tempcal]*256+02
        log.trace ("${tempcal}°F calibration applied")
    } // LC : Temp cal high byte + unit C or F low byte
    request << zwave.configurationV1.configurationSet(parameterNumber: 201, size: 2, scaledConfigurationValue: calTemp ?:01) // Temperature calibration
    getCalOptionValueMap()
    
	//7. query sensor data
	request << zwave.batteryV1.batteryGet()
	request << zwave.sensorBinaryV2.sensorBinaryGet(sensorType: 0x0C) //motion
	request << zwave.sensorMultilevelV5.sensorMultilevelGet(sensorType: 0x01) //temperature
	request << zwave.sensorMultilevelV5.sensorMultilevelGet(sensorType: 0x03) //illuminance
	request << zwave.sensorMultilevelV5.sensorMultilevelGet(sensorType: 0x05) //humidity
	request << zwave.sensorMultilevelV5.sensorMultilevelGet(sensorType: 0x1B) //ultravioletIndex
	request << zwave.configurationV1.configurationGet(parameterNumber: 9)

	setConfigured("true")

	commands(request) + ["delay 20000", zwave.wakeUpV1.wakeUpNoMoreInformation().format()]
}
// LC added 5 10 and 15 seconds option
private def getTimeOptionValueMap() { [
        "5 seconds" : 5,
        "10 seconds" : 10,
        "15 seconds" : 15,
        "20 seconds" : 20,
		"40 seconds" : 40,
		"1 minute"   : 60,
		"2 minutes"  : 2*60,
		"3 minutes"  : 3*60,
		"4 minutes"  : 4*60,
		"5 minutes"  : 5*60,
		"8 minutes"  : 8*60,
		"15 minutes" : 15*60,
		"30 minutes" : 30*60,
		"1 hours"    : 1*60*60,
		"6 hours"    : 6*60*60,
		"12 hours"   : 12*60*60,
		"18 hours"   : 6*60*60,
		"24 hours"   : 24*60*60,
]}
private def getCalOptionValueMap(){[ // LC: used to calibrate temperature 
	"-5.0" : 206,
    "-4.5" : 211,
	"-4.0" : 216, //-40
    "-3.5" : 221, //-35
    "-3.0" : 226,
    "-2.5" : 231,
    "-2.0" : 236,
    "-1.5" : 241,
    "-1.0" : 246,
    "-0.5" : 251,
     "0.0" : 0,
     "0.5" : 5,  //0.5
     "1.0" : 10, //10
     "1.5" : 15,
     "2.0" : 20,
     "2.5" : 25,
     "3.0" : 30,
     "3.5" : 35,
     "4.0" : 40,
     "4.5" : 45,
     "5.0" : 50,
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

private commands(commands, delay=200) {
	//log.info "sending commands: ${commands}"
	delayBetween(commands.collect{ command(it) }, delay)
}