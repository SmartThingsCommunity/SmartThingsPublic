/**
 *
 *  Aeon Multisensor 6 (Advanced)
 *   
 *	github: Eric Maycock (erocm123)
 *	Date: 2020-08-10
 *	Copyright Eric Maycock
 *
 *  Code has elements from other community sources @CyrilPeponnet, @Robert_Vandervoort. Greatly reworked and 
 *  optimized for improved battery life (hopefully) :) and ease of advanced configuration. I tried to get it
 *  as feature rich as possible. 
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
 *  2020-08-10: Fix for missing "maxFirmware()" method. 
 *
 *  2019-10-28: Adjusting method of determining firmware specific settings. 
 *
 *  2018-07-20: Added a "region override" option to customize which region the firmware is from (EU, US, AU) if 
 *              the device handler can't detect it on its own. 
 */

 metadata {
	definition (name: "Aeon Multisensor 6 (Advanced)", namespace: "erocm123", author: "Eric Maycock", vid:"generic-motion-7") {
		capability "Motion Sensor"
		capability "Acceleration Sensor"
		capability "Temperature Measurement"
		capability "Relative Humidity Measurement"
		capability "Illuminance Measurement"
		capability "Ultraviolet Index" 
		capability "Configuration"
		capability "Sensor"
		capability "Battery"
        capability "Refresh"
        capability "Tamper Alert"
        capability "Health Check"
        
        command "resetBatteryRuntime"
        command "resetTamperAlert"
		
        attribute   "needUpdate", "string"
        
        fingerprint deviceId: "0x2101", inClusters: "0x5E,0x86,0x72,0x59,0x85,0x73,0x71,0x84,0x80,0x30,0x31,0x70,0x98,0x7A,0x5A" // 1.07 & 1.08 Secure
        fingerprint deviceId: "0x2101", inClusters: "0x5E,0x86,0x72,0x59,0x85,0x73,0x71,0x84,0x80,0x30,0x31,0x70,0x7A,0x5A" // 1.07 & 1.08
        
        fingerprint deviceId: "0x2101", inClusters: "0x5E,0x86,0x72,0x59,0x85,0x73,0x71,0x84,0x80,0x30,0x31,0x70,0x7A", outClusters: "0x5A" // 1.06
        
        fingerprint mfr:"0086", prod:"0002", model:"0064", deviceJoinName: "Aeon MultiSensor 6" //EU //Aeotec MultiSensor 6
	fingerprint mfr:"0086", prod:"0102", model:"0064", deviceJoinName: "Aeon MultiSensor 6" //US //Aeotec MultiSensor 6
	fingerprint mfr:"0086", prod:"0202", model:"0064", deviceJoinName: "Aeon MultiSensor 6" //AU //Aeotec MultiSensor 6
	
	}
    preferences {
        input description: "Once you change values on this page, the corner of the \"configuration\" icon will change orange until all configuration parameters are updated.", title: "Settings", displayDuringSetup: false, type: "paragraph", element: "paragraph"
		generate_preferences(configuration_model())
    }
	simulator {
	}
	tiles (scale: 2) {
		multiAttributeTile(name:"temperature", type:"generic", width:6, height:4) {
			tileAttribute("device.temperature", key: "PRIMARY_CONTROL") {
            	attributeState "temperature", label:'${currentValue}°', icon:"st.motion.motion.inactive", backgroundColors:[
                	[value: 31, color: "#153591"],
                    [value: 44, color: "#1e9cbb"],
                    [value: 59, color: "#90d2a7"],
					[value: 74, color: "#44b621"],
					[value: 84, color: "#f1d801"],
					[value: 95, color: "#d04e00"],
					[value: 96, color: "#bc2323"]
				]
            }
            tileAttribute ("statusText", key: "SECONDARY_CONTROL") {
				attributeState "statusText", label:'${currentValue}'
			}
		}
        standardTile("motion","device.motion", inactiveLabel: false, width: 2, height: 2) {
                state "inactive",label:'no motion',icon:"st.motion.motion.inactive",backgroundColor:"#ffffff"
                state "active",label:'motion',icon:"st.motion.motion.active",backgroundColor:"#00a0dc"
		}

		valueTile("humidity","device.humidity", inactiveLabel: false, width: 2, height: 2) {
           	state "humidity",label:'${currentValue} % RH'
		}
		valueTile("illuminance", "device.illuminance", inactiveLabel: false, width: 2, height: 2) {
           state "luminosity", label:'${currentValue} LUX', unit:"lux", 
                backgroundColors:[
                	[value: 0, color: "#000000"],
                    [value: 1, color: "#060053"],
                    [value: 3, color: "#3E3900"],
                    [value: 12, color: "#8E8400"],
					[value: 24, color: "#C5C08B"],
					[value: 36, color: "#DAD7B6"],
					[value: 128, color: "#F3F2E9"],
                    [value: 1000, color: "#FFFFFF"]
				]
		}
        
		valueTile(
        	"ultravioletIndex","device.ultravioletIndex", inactiveLabel: false, width: 2, height: 2) {
				state "ultravioletIndex",label:'${currentValue} UV INDEX',unit:""
		}
		standardTile("acceleration", "device.acceleration", inactiveLabel: false, width: 2, height: 2) {
			state("inactive", label:'clear', icon:"st.motion.acceleration.inactive", backgroundColor:"#ffffff")
            state("active", label:'tamper', icon:"st.motion.acceleration.active", backgroundColor:"#f39c12")
		}
        standardTile("tamper", "device.tamper", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
			state("clear", label:'clear', icon:"st.contact.contact.closed", backgroundColor:"#cccccc", action: "resetTamperAlert")
            state("detected", label:'tamper', icon:"st.contact.contact.open", backgroundColor:"#e86d13", action: "resetTamperAlert")
		}
		valueTile("battery", "device.battery", inactiveLabel: false, width: 2, height: 2) {
			state "battery", label:'${currentValue}% battery', unit:""
		}
        valueTile("batteryTile", "device.batteryTile", inactiveLabel: false, width: 2, height: 2) {
			state "batteryTile", label:'${currentValue}', unit:""
		}
        valueTile(
			"currentFirmware", "device.currentFirmware", inactiveLabel: false, width: 2, height: 2) {
			state "currentFirmware", label:'Firmware: v${currentValue}', unit:""
		}
        standardTile("refresh", "device.switch", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
			state "default", label:'', action:"refresh.refresh", icon:"st.secondary.refresh"
		}
        standardTile("configure", "device.needUpdate", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
            state "NO" , label:'', action:"configuration.configure", icon:"st.secondary.configure"
            state "YES", label:'', action:"configuration.configure", icon:"https://github.com/erocm123/SmartThingsPublic/raw/master/devicetypes/erocm123/qubino-flush-1d-relay.src/configure@2x.png"
        }
        standardTile(
			"batteryRuntime", "device.batteryRuntime", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
			state "batteryRuntime", label:'Battery: ${currentValue} Double tap to reset counter', unit:"", action:"resetBatteryRuntime"
		}
        standardTile(
			"statusText2", "device.statusText2", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
			state "statusText2", label:'${currentValue}', unit:"", action:"resetBatteryRuntime"
		}
        
		main([
        	"temperature", "motion"
            ])
		details([
        	"temperature",
            "humidity","illuminance","ultravioletIndex",
            "motion","tamper","batteryTile", 
            "refresh", "configure", "statusText2", 
            ])
	}
}

private getMaxFirmware() {
	return 1.13
}

def parse(String description)
{
	def result = []
    switch(description){
        case ~/Err 106.*/:
			state.sec = 0
			result = createEvent( name: "secureInclusion", value: "failed", isStateChange: true,
			descriptionText: "This sensor failed to complete the network security key exchange. If you are unable to control it via SmartThings, you must remove it from your network and add it again.")
        break
		case "updated":
        	result = createEvent( name: "Inclusion", value: "paired", isStateChange: true,
			descriptionText: "Update is hit when the device is paired")
            result << response(zwave.wakeUpV1.wakeUpIntervalSet(seconds: 3600, nodeid:zwaveHubNodeId).format())
            result << response(zwave.batteryV1.batteryGet().format())
            result << response(zwave.versionV1.versionGet().format())
            result << response(zwave.manufacturerSpecificV2.manufacturerSpecificGet().format())
            result << response(configure())
        break
        default:
			def cmd = zwave.parse(description, [0x31: 5, 0x30: 2, 0x84: 1])
			if (cmd) {
                try {
				result += zwaveEvent(cmd)
                } catch (e) {
                log.debug "error: $e cmd: $cmd description $description"
                }
			}
        break
	}
    
    if(state.batteryRuntimeStart != null){
        sendEvent(name:"batteryRuntime", value:getBatteryRuntime(), displayed:false)
        if (device.currentValue('currentFirmware') != null){
            sendEvent(name:"statusText2", value: "Firmware: v${device.currentValue('currentFirmware')} - Battery: ${getBatteryRuntime()} Double tap to reset", displayed:false)
        } else {
            sendEvent(name:"statusText2", value: "Battery: ${getBatteryRuntime()} Double tap to reset", displayed:false)
        }
    } else {
        state.batteryRuntimeStart = now()
    }
    
    def statusTextmsg = ""
    result.each {
        if ((it instanceof Map) == true && it.find{ it.key == "name" }?.value == "humidity") {
            statusTextmsg = "${it.value}% RH - ${device.currentValue('illuminance')? device.currentValue('illuminance') : "0%"} LUX - ${device.currentValue('ultravioletIndex')? device.currentValue('ultravioletIndex') : "0"} UV"
        }
        if ((it instanceof Map) == true && it.find{ it.key == "name" }?.value == "illuminance") {
            statusTextmsg = "${device.currentValue('humidity')? device.currentValue('humidity') : "0"}% RH - ${it.value} LUX - ${device.currentValue('ultravioletIndex')? device.currentValue('ultravioletIndex') : "0"} UV"
        }
        if ((it instanceof Map) == true && it.find{ it.key == "name" }?.value == "ultravioletIndex") {
            statusTextmsg = "${device.currentValue('humidity')? device.currentValue('humidity') : "0"}% RH - ${device.currentValue('illuminance')? device.currentValue('illuminance') : "0"} LUX - ${it.value} UV"
        }
    }
    if (statusTextmsg != "") sendEvent(name:"statusText", value:statusTextmsg, displayed:false)

	if ( result[0] != null ) { result }
}

def zwaveEvent(physicalgraph.zwave.commands.securityv1.SecurityMessageEncapsulation cmd) {
	def encapsulatedCommand = cmd.encapsulatedCommand([0x31: 5, 0x30: 2, 0x84: 1])
	state.sec = 1
	if (encapsulatedCommand) {
		zwaveEvent(encapsulatedCommand)
	} else {
		log.warn "Unable to extract encapsulated cmd from $cmd"
		createEvent(descriptionText: cmd.toString())
	}
}

def zwaveEvent(physicalgraph.zwave.commands.securityv1.SecurityCommandsSupportedReport cmd) {
	response(configure())
}

def zwaveEvent(physicalgraph.zwave.commands.configurationv2.ConfigurationReport cmd) {
    if (cmd.parameterNumber.toInteger() == 81 && cmd.configurationValue == [255]) {
        update_current_properties([parameterNumber: "81", configurationValue: [1]])
        logging("${device.displayName} parameter '${cmd.parameterNumber}' with a byte size of '${cmd.size}' is set to '1'")
    } else {
        update_current_properties(cmd)
        logging("${device.displayName} parameter '${cmd.parameterNumber}' with a byte size of '${cmd.size}' is set to '${cmd2Integer(cmd.configurationValue)}'")
    }
}

def zwaveEvent(physicalgraph.zwave.commands.wakeupv1.WakeUpIntervalReport cmd)
{
	logging("WakeUpIntervalReport ${cmd.toString()}")
    state.wakeInterval = cmd.seconds
}

def zwaveEvent(physicalgraph.zwave.commands.batteryv1.BatteryReport cmd) {
    logging("Battery Report: $cmd")
    def events = []
	def map = [ name: "battery", unit: "%" ]
	if (cmd.batteryLevel == 0xFF) {
		map.value = 1
		map.descriptionText = "${device.displayName} battery is low"
		map.isStateChange = true
	} else {
		map.value = cmd.batteryLevel
	}
    if(settings."101" == null || settings."101" == "241") {
        try {
            events << createEvent([name: "batteryTile", value: "Battery ${map.value}%", displayed:false])
        } catch (e) {
            logging("$e")
        }
    }
    events << createEvent(map)
    
    state.lastBatteryReport = now()
    return events
}

def zwaveEvent(physicalgraph.zwave.commands.sensormultilevelv5.SensorMultilevelReport cmd)
{
	def map = [:]
	switch (cmd.sensorType) {
		case 1:
			map.name = "temperature"
			def cmdScale = cmd.scale == 1 ? "F" : "C"
            state.realTemperature = convertTemperatureIfNeeded(cmd.scaledSensorValue, cmdScale, cmd.precision)
			map.value = getAdjustedTemp(state.realTemperature)
			map.unit = getTemperatureScale()
            logging("Temperature Report: $map.value")
			break;
		case 3:
			map.name = "illuminance"
            state.realLuminance = cmd.scaledSensorValue.toInteger()
			map.value = getAdjustedLuminance(cmd.scaledSensorValue.toInteger())
			map.unit = "lux"
            logging("Illuminance Report: $map.value")
			break;
        case 5:
			map.name = "humidity"
            state.realHumidity = cmd.scaledSensorValue.toInteger()
			map.value = getAdjustedHumidity(cmd.scaledSensorValue.toInteger())
			map.unit = "%"
            logging("Humidity Report: $map.value")
			break;
		case 27:
        	map.name = "ultravioletIndex"
            state.realUV = cmd.scaledSensorValue.toInteger()
            map.value = getAdjustedUV(cmd.scaledSensorValue.toInteger())
            map.unit = ""
            logging("UV Report: $map.value")
            break;
		default:
			map.descriptionText = cmd.toString()
	}
    
    //def request = update_needed_settings()
    
    //if(request != []){
        //return [response(commands(request)), createEvent(map)]
    //} else {
        return createEvent(map)
    //}

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
    logging("SensorBinaryReport: $cmd")
	motionEvent(cmd.sensorValue)
}

def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicSet cmd) {
    logging("BasicSet: $cmd")
	motionEvent(cmd.value)
}

def zwaveEvent(physicalgraph.zwave.commands.notificationv3.NotificationReport cmd) {
    logging("NotificationReport: $cmd")
	def result = []
	if (cmd.notificationType == 7) {
		switch (cmd.event) {
			case 0:
				//result << motionEvent(0)
				result << createEvent(name: "tamper", value: "clear", descriptionText: "$device.displayName tamper cleared")
                result << createEvent(name: "acceleration", value: "inactive", descriptionText: "$device.displayName tamper cleared", displayed:false)
				break
			case 3:
				result << createEvent(name: "tamper", value: "detected", descriptionText: "$device.displayName was tampered")
                result << createEvent(name: "acceleration", value: "active", descriptionText: "$device.displayName was moved", displayed:false)
				break
			case 7:
				//result << motionEvent(1)
				break
		}
	} else {
        logging("Need to handle this cmd.notificationType: ${cmd.notificationType}")
		result << createEvent(descriptionText: cmd.toString(), isStateChange: false)
	}
	result
}

def zwaveEvent(physicalgraph.zwave.commands.wakeupv1.WakeUpNotification cmd)
{
    logging("Device ${device.displayName} woke up")
    
    def request = update_needed_settings()

    if(request != []){
       response(commands(request) + ["delay 5000", zwave.wakeUpV1.wakeUpNoMoreInformation().format()])
    } else {
       logging("No commands to send")
       response([zwave.wakeUpV1.wakeUpNoMoreInformation().format()])
    }
}

def zwaveEvent(physicalgraph.zwave.commands.versionv1.VersionReport cmd) {
    logging(cmd)
    if(cmd.applicationVersion && cmd.applicationSubVersion) {
        state.rawFW = "${cmd.applicationVersion}.${cmd.applicationSubVersion.toString().padLeft(2,'0')}"
        state.needfwUpdate = "false"
        updateDataValue("firmware", "${state.rawFW}${getOverride()}")
        createEvent(name: "currentFirmware", value: "${state.rawFW}${getOverride()}")
    }
}

def zwaveEvent(physicalgraph.zwave.Command cmd) {
    logging("Unknown Z-Wave Command: ${cmd.toString()}")
}

def refresh() {
   	logging("$device.displayName refresh()")

    def request = []
    if (state.lastRefresh != null && now() - state.lastRefresh < 5000) {
        logging("Refresh Double Press")
        state.currentProperties."111" = null
        state.wakeInterval = null
        def configuration = parseXml(configuration_model())
        configuration.Value.each
        {
            if ( "${it.@setting_type}" == "zwave" ) {
                request << zwave.configurationV1.configurationGet(parameterNumber: "${it.@index}".toInteger())
            }
        } 
        request << zwave.versionV1.versionGet()
        request << zwave.wakeUpV1.wakeUpIntervalGet()
    } else {
        request << zwave.batteryV1.batteryGet()
        request << zwave.sensorMultilevelV5.sensorMultilevelGet(sensorType:1, scale:1)
        request << zwave.sensorMultilevelV5.sensorMultilevelGet(sensorType:3, scale:1)
        request << zwave.sensorMultilevelV5.sensorMultilevelGet(sensorType:5, scale:1)
        request << zwave.sensorMultilevelV5.sensorMultilevelGet(sensorType:27, scale:1)
    }

    state.lastRefresh = now()
    
    commands(request)
}

def ping() {
   	logging("$device.displayName ping()")

    def request = []
    request << zwave.sensorMultilevelV5.sensorMultilevelGet(sensorType:1, scale:1)
    request << zwave.sensorMultilevelV5.sensorMultilevelGet(sensorType:3, scale:1)
    request << zwave.sensorMultilevelV5.sensorMultilevelGet(sensorType:5, scale:1)
    request << zwave.sensorMultilevelV5.sensorMultilevelGet(sensorType:27, scale:1)
    
    commands(request)
}

def configure() {
    state.enableDebugging = settings.enableDebugging
    logging("Configuring Device For SmartThings Use")
    def cmds = []

    cmds = update_needed_settings()
    
    if (cmds != []) commands(cmds)
}

def updated()
{
    state.enableDebugging = settings.enableDebugging
    sendEvent(name: "checkInterval", value: 6 * 60 * 60 + 2 * 60, displayed: false, data: [protocol: "zwave", hubHardwareId: device.hub.hardwareID])
    logging("updated() is being called")
    if(settings."101" != null && settings."101" == "240") { 
        sendEvent(name:"batteryTile", value: "USB Powered", displayed:false)
    } else {
        try {
            sendEvent(name:"batteryTile", value: "Battery ${(device.currentValue("battery") == null ? '?' : device.currentValue("battery"))}%", displayed:false)
        } catch (e) {
            logging("$e")
            sendEvent(name:"battery", value: "100", displayed:false)
            sendEvent(name:"batteryTile", value: "Battery ${(device.currentValue("battery") == null ? '?' : device.currentValue("battery"))}%", displayed:false)
        }
    }

    state.needfwUpdate = ""
    
    if (state.realTemperature != null) sendEvent(name:"temperature", value: getAdjustedTemp(state.realTemperature))
    if (state.realHumidity != null) sendEvent(name:"humidity", value: getAdjustedHumidity(state.realHumidity))
    if (state.realLuminance != null) sendEvent(name:"illuminance", value: getAdjustedLuminance(state.realLuminance))
    if (state.realUV != null) sendEvent(name:"ultravioletIndex", value: getAdjustedUV(state.realUV))
    
    def cmds = update_needed_settings()
    
    if (device.currentValue("battery") == null) cmds << zwave.batteryV1.batteryGet()
    if (device.currentValue("temperature") == null) cmds << zwave.sensorMultilevelV5.sensorMultilevelGet(sensorType:1, scale:1)
    if (device.currentValue("humidity") == null) cmds << zwave.sensorMultilevelV5.sensorMultilevelGet(sensorType:3, scale:1)
    if (device.currentValue("illuminance") == null) cmds << zwave.sensorMultilevelV5.sensorMultilevelGet(sensorType:5, scale:1)
    if (device.currentValue("ultravioletIndex") == null) cmds << zwave.sensorMultilevelV5.sensorMultilevelGet(sensorType:27, scale:1)
    
    if (state.rawFW) {
        updateDataValue("firmware", "${state.rawFW}${getOverride()}")
        sendEvent(name: "currentFirmware", value: "${state.rawFW}${getOverride()}")
    } else if (device.currentValue("currentFirmware") != null) {
        updateDataValue("firmware", "${device.currentValue("currentFirmware") - ~/[A-Za-z]+/}${getOverride()}")
        sendEvent(name: "currentFirmware", value: "${device.currentValue("currentFirmware") - ~/[A-Za-z]+/}${getOverride()}")
    }
    
    if(state.batteryRuntimeStart != null){
        sendEvent(name:"batteryRuntime", value:getBatteryRuntime(), displayed:false)
        if (device.currentValue('currentFirmware') != null){
            sendEvent(name:"statusText2", value: "Firmware: v${device.currentValue('currentFirmware')} - Battery: ${getBatteryRuntime()} Double tap to reset", displayed:false)
        } else {
            sendEvent(name:"statusText2", value: "Battery: ${getBatteryRuntime()} Double tap to reset", displayed:false)
        }
    } else {
        state.batteryRuntimeStart = now()
    }
    
    sendEvent(name:"needUpdate", value: device.currentValue("needUpdate"), displayed:false, isStateChange: true)
    
    response(commands(cmds))
}

private getOverride() {
    switch(regionOverride) {
    case "0":
        return location.getTemperatureScale() == 'C' ? 'EU':''
    break
    case "1":
        return ""
    break
    case "2":
        return "EU"
    break
    case "3":
        return "AU"
    break
    default:
        return location.getTemperatureScale() == 'C' ? 'EU':''
    break
    }
}

def resetTamperAlert() {
    sendEvent(name: "tamper", value: "clear", descriptionText: "$device.displayName tamper cleared")
    sendEvent(name: "acceleration", value: "inactive", descriptionText: "$device.displayName tamper cleared")
    sendEvent(name: "motion", value: "inactive", descriptionText: "$device.displayName motion has stopped")
}

def convertParam(number, value) {
	switch (number){
        case 41:
            //Parameter difference between firmware versions
        	if (settings."41"?.toInteger() != null && device.currentValue("currentFirmware") != null) {
                    if (device.currentValue("currentFirmware")?.replaceAll("[A-Za-z]", "")?.toFloat() > 1.09) {
                        if (value < 10) {
                            logging("Firmware 1.09+ does not allow a setting below 10. If this parameter needs to be updated it will be set to the lowest possible value (10)")
                            if (getOverride() == "EU")
                                (10 * 65536) + 256
                            else
                                (10 * 65536) + 512
                        } else {
                            if (getOverride() == "EU")
                                (value * 65536) + 256
                            else
                                (value * 65536) + 512
                        }
                    } else if (device.currentValue("currentFirmware")?.replaceAll("[A-Za-z]", "")?.toFloat() >= 1.07 && device.currentValue("currentFirmware")?.replaceAll("[A-Za-z]", "")?.toFloat() <= 1.09) {
                        if (device.currentValue("currentFirmware")?.replaceAll("[A-Za-z]", "")?.toFloat() == 1.09 && value < 10) {
                            logging("Firmware 1.09+ does not allow a setting below 10. If this parameter needs to be updated it will be set to the lowest possible value (10)")
                            if (getOverride() == "EU")
                                (10 * 256) + 1
                            else
                                (10 * 256) + 2
                        } else {
                            if (getOverride() == "EU")
                                (value * 256) + 1
                            else 
                                (value * 256) + 2
                        }
                    } else {
                        value
                    }
                } else {
                    value
                }
        break
        case 45:
            //Parameter difference between firmware versions
        	if (settings."45"?.toInteger() != null && device.currentValue("currentFirmware") != null && device.currentValue("currentFirmware") != "1.08")
            	2
            else
                value
        break
        case 101:
        	if (settings."40"?.toInteger() != null) {
                if (settings."40"?.toInteger() == 1) {
                   0
                } else {
                   value
                }	
            } else {
                241
            }
        break
    	case 201:
        	if (value < 0)
            	256 + value
        	else if (value > 100)
            	value - 256
            else
            	value
        break
        case 202:
        	if (value < 0)
            	256 + value
        	else if (value > 100)
            	value - 256
            else
            	value
        break
        case 203:
            if (value < 0)
            	65536 + value
        	else if (value > 1000)
            	value - 65536
            else
            	value
        break
        case 204:
        	if (value < 0)
            	256 + value
        	else if (value > 100)
            	value - 256
            else
            	value
        break
        default:
        	value
        break
    }
}

def update_current_properties(cmd)
{
    def currentProperties = state.currentProperties ?: [:]
    
    currentProperties."${cmd.parameterNumber}" = cmd.configurationValue

    if (settings."${cmd.parameterNumber}" != null)
    {   
            if (convertParam("${cmd.parameterNumber}".toInteger(), settings."${cmd.parameterNumber}".toInteger()) == cmd2Integer(cmd.configurationValue))
            {
                sendEvent(name:"needUpdate", value:"NO", displayed:false, isStateChange: true)
            }
            else
            {
                sendEvent(name:"needUpdate", value:"YES", displayed:false, isStateChange: true)
            }
    }

    state.currentProperties = currentProperties
}

def update_needed_settings()
{
    def cmds = []
    def currentProperties = state.currentProperties ?: [:]
     
    def configuration = parseXml(configuration_model())
    def isUpdateNeeded = "NO"
    
    if(!state.needfwUpdate || state.needfwUpdate == "") {
       logging("Requesting device firmware version")
       cmds << zwave.versionV1.versionGet()
    }

    if (state.currentProperties?."252" != [0]) {
        logging("Unlocking configuration.")
        cmds << zwave.configurationV1.configurationSet(configurationValue: integer2Cmd(0, 1), parameterNumber: 252, size: 1)
        cmds << zwave.configurationV1.configurationGet(parameterNumber: 252)
    }

    if(state.wakeInterval == null || state.wakeInterval != getAdjustedWake()){
        logging("Setting Wake Interval to ${getAdjustedWake()}")
        cmds << zwave.wakeUpV1.wakeUpIntervalSet(seconds: getAdjustedWake(), nodeid:zwaveHubNodeId)
        cmds << zwave.wakeUpV1.wakeUpIntervalGet()
    }

    configuration.Value.each
    {     
        if ("${it.@setting_type}" == "zwave"){
            if (currentProperties."${it.@index}" == null)
            {
                if (device.currentValue("currentFirmware") == null || "${it.@fw}".indexOf(device.currentValue("currentFirmware")) >= 0 || device.currentValue("currentFirmware")?.replaceAll("[A-Za-z]", "")?.toFloat() > maxFirmware){
                    isUpdateNeeded = "YES"
                    logging("Current value of parameter ${it.@index} is unknown")
                    cmds << zwave.configurationV1.configurationGet(parameterNumber: it.@index.toInteger())
                }
            } 
            else if (settings."${it.@index}" != null && cmd2Integer(currentProperties."${it.@index}") != convertParam(it.@index.toInteger(), settings."${it.@index}".toInteger()))
            { 
                if (device.currentValue("currentFirmware") == null || "${it.@fw}".indexOf(device.currentValue("currentFirmware")) >= 0 || device.currentValue("currentFirmware")?.replaceAll("[A-Za-z]", "")?.toFloat() > maxFirmware){
                    isUpdateNeeded = "YES"

                    logging("Parameter ${it.@index} will be updated to " + convertParam(it.@index.toInteger(), settings."${it.@index}".toInteger()))
                    
                    if (it.@index == "41") {
                        if (device.currentValue("currentFirmware") != null && device.currentValue("currentFirmware")?.replaceAll("[A-Za-z]", "")?.toFloat() == 1.06) {
                            cmds << zwave.configurationV1.configurationSet(configurationValue: integer2Cmd(convertParam(it.@index.toInteger(), settings."${it.@index}".toInteger()), 2), parameterNumber: it.@index.toInteger(), size: 2)
                        } else if (device.currentValue("currentFirmware") != null && device.currentValue("currentFirmware")?.replaceAll("[A-Za-z]", "")?.toFloat() > 1.09) {
                            cmds << zwave.configurationV1.configurationSet(configurationValue: integer2Cmd(convertParam(it.@index.toInteger(), settings."${it.@index}".toInteger()), 4), parameterNumber: it.@index.toInteger(), size: 4)
                        } else {
                            cmds << zwave.configurationV1.configurationSet(configurationValue: integer2Cmd(convertParam(it.@index.toInteger(), settings."${it.@index}".toInteger()), 3), parameterNumber: it.@index.toInteger(), size: 3)
                        }
                    } else {
                        cmds << zwave.configurationV1.configurationSet(configurationValue: integer2Cmd(convertParam(it.@index.toInteger(), settings."${it.@index}".toInteger()), it.@byteSize.toInteger()), parameterNumber: it.@index.toInteger(), size: it.@byteSize.toInteger())
                    }

                    cmds << zwave.configurationV1.configurationGet(parameterNumber: it.@index.toInteger())
                }
            } 
        }
    }
    
    sendEvent(name:"needUpdate", value: isUpdateNeeded, displayed:false, isStateChange: true)
    return cmds
}

/**
* Convert 1 and 2 bytes values to integer
*/
def cmd2Integer(array) { 
try {
switch(array.size()) {
	case 1:
		array[0]
    break
	case 2:
    	((array[0] & 0xFF) << 8) | (array[1] & 0xFF)
    break
    case 3:
    	((array[0] & 0xFF) << 16) | ((array[1] & 0xFF) << 8) | (array[2] & 0xFF)
    break
	case 4:
    	((array[0] & 0xFF) << 24) | ((array[1] & 0xFF) << 16) | ((array[2] & 0xFF) << 8) | (array[3] & 0xFF)
	break
}
}catch (e) {
log.debug "Error: cmd2Integer $e"
}
}

def integer2Cmd(value, size) {
    try{
	switch(size) {
	case 1:
		[value]
    break
	case 2:
    	def short value1   = value & 0xFF
        def short value2 = (value >> 8) & 0xFF
        [value2, value1]
    break
    case 3:
    	def short value1   = value & 0xFF
        def short value2 = (value >> 8) & 0xFF
        def short value3 = (value >> 16) & 0xFF
        [value3, value2, value1]
    break
	case 4:
    	def short value1 = value & 0xFF
        def short value2 = (value >> 8) & 0xFF
        def short value3 = (value >> 16) & 0xFF
        def short value4 = (value >> 24) & 0xFF
		[value4, value3, value2, value1]
	break
	}
    } catch (e) {
        log.debug "Error: integer2Cmd $e Value: $value"
    }
}

private command(physicalgraph.zwave.Command cmd) {
    
	if (state.sec && cmd.toString() != "WakeUpIntervalGet()") {
		zwave.securityV1.securityMessageEncapsulation().encapsulate(cmd).format()
	} else {
		cmd.format()
	}
}

private commands(commands, delay=1000) {
	delayBetween(commands.collect{ command(it) }, delay)
}

def generate_preferences(configuration_model)
{
    def configuration = parseXml(configuration_model)
   
    configuration.Value.each
    {
        switch(it.@type)
        {   
            case ["byte","short","four"]:
                input "${it.@index}", "number",
                    title:"${it.@label}\n" + "${it.Help}",
                    range: "${it.@min}..${it.@max}",
                    defaultValue: "${it.@value}",
                    displayDuringSetup: "${it.@displayDuringSetup}"
            break
            case "list":
                def items = []
                it.Item.each { items << ["${it.@value}":"${it.@label}"] }
                input "${it.@index}", "enum",
                    title:"${it.@label}\n" + "${it.Help}",
                    defaultValue: "${it.@value}",
                    displayDuringSetup: "${it.@displayDuringSetup}",
                    options: items
            break
            case "decimal":
               input "${it.@index}", "decimal",
                    title:"${it.@label}\n" + "${it.Help}",
                    range: "${it.@min}..${it.@max}",
                    defaultValue: "${it.@value}",
                    displayDuringSetup: "${it.@displayDuringSetup}"
            break
            case "boolean":
               input "${it.@index}", "boolean",
                    title:"${it.@label}\n" + "${it.Help}",
                    defaultValue: "${it.@value}",
                    displayDuringSetup: "${it.@displayDuringSetup}"
            break
        }  
    }
}

private getBatteryRuntime() {
   def currentmillis = now() - state.batteryRuntimeStart
   def days=0
   def hours=0
   def mins=0
   def secs=0
   secs = (currentmillis/1000).toInteger() 
   mins=(secs/60).toInteger() 
   hours=(mins/60).toInteger() 
   days=(hours/24).toInteger() 
   secs=(secs-(mins*60)).toString().padLeft(2, '0') 
   mins=(mins-(hours*60)).toString().padLeft(2, '0') 
   hours=(hours-(days*24)).toString().padLeft(2, '0') 
 

  if (days>0) { 
      return "$days days and $hours:$mins:$secs"
  } else {
      return "$hours:$mins:$secs"
  }
}

private getRoundedInterval(number) {
    double tempDouble = (number / 60)
    if (tempDouble == tempDouble.round())
       return (tempDouble * 60).toInteger()
    else 
       return ((tempDouble.round() + 1) * 60).toInteger()
}

private getAdjustedWake(){
    def wakeValue
    if (device.currentValue("currentFirmware") != null && settings."101" != null && settings."111" != null){
        if (device.currentValue("currentFirmware") == "1.08"){
            if (settings."101".toInteger() == 241){   
                if (settings."111".toInteger() <= 3600){
                    wakeValue = getRoundedInterval(settings."111")
                } else {
                    wakeValue = 3600
                }
            } else {
                wakeValue = 1800
            }
        } else {
            if (settings."101".toInteger() == 241){   
                if (settings."111".toInteger() <= 3600){
                    wakeValue = getRoundedInterval(settings."111")
                } else {
                    wakeValue = getRoundedInterval(settings."111".toInteger() / 2)
                }
            } else {
                wakeValue = 240
            }
        }
    } else {
        wakeValue = 3600
    }
    return wakeValue.toInteger()
}

private getAdjustedTemp(value) {
    
    value = Math.round((value as Double) * 100) / 100

	if (settings."201") {
	   return value =  value + Math.round(settings."201" * 100) /100
	} else {
       return value
    }
    
}

private getAdjustedHumidity(value) {
    
    value = Math.round((value as Double) * 100) / 100

	if (settings."202") {
	   return value =  value + Math.round(settings."202" * 100) /100
	} else {
       return value
    }
    
}

private getAdjustedLuminance(value) {
    
    value = Math.round((value as Double) * 100) / 100

	if (settings."203") {
	   return value =  value + Math.round(settings."203" * 100) /100
	} else {
       return value
    }
    
}

private getAdjustedUV(value) {
    
    value = Math.round((value as Double) * 100) / 100

	if (settings."204") {
	   return value =  value + Math.round(settings."204" * 100) /100
	} else {
       return value
    }
    
}

def resetBatteryRuntime() {
    if (state.lastReset != null && now() - state.lastReset < 5000) {
        logging("Reset Double Press")
        state.batteryRuntimeStart = now()
        //updateStatus()
    }
    state.lastReset = now()
}

private updateStatus(){
   def result = []
   if(state.batteryRuntimeStart != null){
        sendEvent(name:"batteryRuntime", value:getBatteryRuntime(), displayed:false)
        if (device.currentValue('currentFirmware') != null){
            sendEvent(name:"statusText2", value: "Firmware: v${device.currentValue('currentFirmware')} - Battery: ${getBatteryRuntime()} Double tap to reset", displayed:false)
        } else {
            sendEvent(name:"statusText2", value: "Battery: ${getBatteryRuntime()} Double tap to reset", displayed:false)
        }
    } else {
        state.batteryRuntimeStart = now()
    }

    String statusText = ""
    if(device.currentValue('humidity') != null)
        statusText = "RH ${device.currentValue('humidity')}% - "
    if(device.currentValue('illuminance') != null)
        statusText = statusText + "LUX ${device.currentValue('illuminance')} - "
    if(device.currentValue('ultravioletIndex') != null)
        statusText = statusText + "UV ${device.currentValue('ultravioletIndex')} - "
        
    if (statusText != ""){
        statusText = statusText.substring(0, statusText.length() - 2)
        sendEvent(name:"statusText", value: statusText, displayed:false)
    }
}

private def logging(message) {
    if (state.enableDebugging == null || state.enableDebugging == "true") log.debug "$message"
}

def configuration_model()
{
'''
<configuration>
    <Value type="list" index="101" label="Battery or USB?" min="240" max="241" value="241" byteSize="4" setting_type="zwave" fw="1.06,1.07,1.08,1.09,1.10,1.11,1.12,1.13,1.06EU,1.07EU,1.08EU,1.09EU,1.10EU,1.11EU,1.12EU,1.13EU" displayDuringSetup="true">
    <Help>
Is the device powered by battery or usb?
    </Help>
        <Item label="Battery" value="241" />
        <Item label="USB" value="240" />
  </Value>
  <Value type="list" index="40" label="Enable selective reporting?" min="0" max="1" value="0" byteSize="1" setting_type="zwave" fw="1.06,1.07,1.08,1.09,1.10,1.11,1.12,1.13,1.06EU,1.07EU,1.08EU,1.09EU,1.10EU,1.11EU,1.12EU,1.13EU">
    <Help>
Enable/disable the selective reporting only when measurements reach a certain threshold or percentage set below. This is used to reduce network traffic.
Default: No (Enable for Better Battery Life)
    </Help>
        <Item label="No" value="0" />
        <Item label="Yes" value="1" />
  </Value>
  <Value type="short" byteSize="2" index="41" label="Temperature Threshold" min="1" max="2120" value="20" setting_type="zwave" fw="1.06,1.07,1.08,1.09,1.10,1.11,1.12,1.13,1.06EU,1.07EU,1.08EU,1.09EU,1.10EU,1.11EU,1.12EU,1.13EU">
    <Help>
Threshold change in temperature to induce an automatic report. 
Range: 1~2120 (Firmware 1.09+ 10~2120)
Default: 20
Note:
Only used if selective reporting is enabled.
1. The unit is Fahrenheit for US version, Celsius for EU/AU version.
2. The value contains one decimal point. E.g. if the value is set to 20, the threshold value =2.0 ℃ (EU/AU version) or 2.0 ℉ (US version). When the current temperature gap is more then 2.0, which will induce a temperature report to be sent out.
    </Help>
  </Value>
  <Value type="byte" byteSize="1" index="42" label="Humidity Threshold" min="1" max="255" value="10" setting_type="zwave" fw="1.06,1.07,1.08,1.09,1.10,1.11,1.12,1.13,1.06EU,1.07EU,1.08EU,1.09EU,1.10EU,1.11EU,1.12EU,1.13EU">
    <Help>
Threshold change in humidity to induce an automatic report.
Range: 1~255.
Default: 10
Note:
Only used if selective reporting is enabled.
1. The unit is %.
2. The default value is 10, which means that if the current humidity gap is more than 10%, it will send out a humidity report.
    </Help>
  </Value>
  <Value type="short" byteSize="2" index="43" label="Luminance Threshold" min="1" max="30000" value="100" setting_type="zwave" fw="1.06,1.07,1.08,1.09,1.10,1.11,1.12,1.13,1.06EU,1.07EU,1.08EU,1.09EU,1.10EU,1.11EU,1.12EU,1.13EU">
    <Help>
Threshold change in luminance to induce an automatic report.
Range: 1~30000.
Default: 100
Note:
Only used if selective reporting is enabled.
    </Help>
  </Value>
  <Value type="byte" byteSize="1" index="44" label="Battery Threshold" min="1" max="99" value="10" setting_type="zwave" fw="1.06,1.07,1.08,1.09,1.10,1.11,1.12,1.13,1.06EU,1.07EU,1.08EU,1.09EU,1.10EU,1.11EU,1.12EU,1.13EU">
    <Help>
Threshold change in battery level to induce an automatic report.
Range: 1~99.
Default: 10
Note:
Only used if selective reporting is enabled.
1. The unit is %.
2. The default value is 10, which means that if the current battery level gap is more than 10%, it will send out a battery report.
    </Help>
  </Value>
  <Value type="byte" byteSize="1" index="45" label="Ultraviolet Threshold" min="1" max="11" value="2" setting_type="zwave" fw="1.06,1.07,1.08,1.09,1.10,1.11,1.12,1.13,1.06EU,1.07EU,1.08EU,1.09EU,1.10EU,1.11EU,1.12EU,1.13EU">
    <Help>
Threshold change in ultraviolet to induce an automatic report.
Range: 1~11.
Default: 2
Note: Firmware 1.06 and 1.07 only support a value of 2.
Only used if selective reporting is enabled.
    </Help>
  </Value>
  <Value type="short" byteSize="2" index="3" label="PIR reset time" min="10" max="3600" value="240" setting_type="zwave" fw="1.06,1.07,1.08,1.09,1.10,1.11,1.12,1.13,1.06EU,1.07EU,1.08EU,1.09EU,1.10EU,1.11EU,1.12EU,1.13EU" displayDuringSetup="true">
    <Help>
Number of seconds to wait to report motion cleared after a motion event if there is no motion detected.
Range: 10~3600.
Default: 240 (4 minutes)
Note:
(1), The time unit is seconds if the value range is in 10 to 255.
(2), If the value range is in 256 to 3600, the time unit will be minute and its value should follow the below rules:
a), Interval time =Value/60, if the interval time can be divided by 60 and without remainder.
b), Interval time= (Value/60) +1, if the interval time can be divided by 60 and has remainder.
    </Help>
  </Value>
    <Value type="byte" byteSize="1" index="4" label="PIR motion sensitivity" min="0" max="5" value="5" setting_type="zwave" fw="1.06,1.07,1.08,1.09,1.10,1.11,1.12,1.13,1.06EU,1.07EU,1.08EU,1.09EU,1.10EU,1.11EU,1.12EU,1.13EU" displayDuringSetup="true">
    <Help>
A value from 0-5, from disabled to high sensitivity
Range: 0~5
Default: 5
    </Help>
  </Value>
    <Value type="byte" byteSize="4" index="111" label="Reporting Interval" min="5" max="2678400" value="3600" setting_type="zwave" fw="1.06,1.07,1.08,1.09,1.10,1.11,1.12,1.13,1.06EU,1.07EU,1.08EU,1.09EU,1.10EU,1.11EU,1.12EU,1.13EU" displayDuringSetup="true">
    <Help>
The interval time of sending reports in Report group 1
Range: 30~
Default: 3600 seconds
Note:
The unit of interval time is in seconds. Minimum interval time is 30 seconds when USB powered and 240 seconds (4 minutes) when battery powered.
    </Help>
  </Value>
  <Value type="decimal" byteSize="1" index="201" label="Temperature offset" min="*" max="*" value="">
    <Help>
Range: None
Default: 0
Note: 
1. The calibration value = standard value - measure value.
E.g. If measure value =85.3F and the standard value = 83.2F, so the calibration value = 83.2F - 85.3F = -2.1F.
If the measure value =60.1F and the standard value = 63.2F, so the calibration value = 63.2F - 60.1℃ = 3.1F. 
    </Help>
  </Value>
  <Value type="byte" byteSize="1" index="202" label="Humidity offset" min="*" max="*" value="">
    <Help>
Range: None
Default: 0
Note:
The calibration value = standard value - measure value.
E.g. If measure value = 80RH and the standard value = 75RH, so the calibration value = 75RH – 80RH = -5RH.
If the measure value = 85RH and the standard value = 90RH, so the calibration value = 90RH – 85RH = 5RH. 
    </Help>
  </Value>
    <Value type="byte" byteSize="2" index="203" label="Luminance offset" min="*" max="*" value="">
    <Help>
Range: None
Default: 0
Note:
The calibration value = standard value - measure value.
E.g. If measure value = 800Lux and the standard value = 750Lux, so the calibration value = 750 – 800 = -50.
If the measure value = 850Lux and the standard value = 900Lux, so the calibration value = 900 – 850 = 50.
    </Help>
  </Value>
    <Value type="byte" byteSize="1" index="204" label="Ultraviolet offset" min="*" max="*" value="">
    <Help>
Range: None
Default: 0
Note:
The calibration value = standard value - measure value.
E.g. If measure value = 9 and the standard value = 8, so the calibration value = 8 – 9 = -1.
If the measure value = 7 and the standard value = 9, so the calibration value = 9 – 7 = 2. 
    </Help>
  </Value>
  <Value type="list" index="5" label="Command Option" min="1" max="2" value="1" byteSize="1" setting_type="zwave" fw="1.06,1.07,1.08,1.09,1.10,1.11,1.12,1.13,1.06EU,1.07EU,1.08EU,1.09EU,1.10EU,1.11EU,1.12EU,1.13EU">
    <Help>
Which command should be sent when the motion sensor is triggered
Default: Basic Set
    </Help>
        <Item label="Basic Set" value="1" />
        <Item label="Sensor Binary" value="2" />
  </Value>
  <Value type="list" index="81" label="LED Options" min="0" max="1" value="0" byteSize="1" setting_type="zwave" fw="1.08,1.09,1.10,1.11,1.12,1.13,1.08EU,1.09EU,1.10EU,1.11EU,1.12EU,1.13EU">
    <Help>
Choose how the LED functions. (Option 1, 2 firmware v1.08+, Option 1, 2, 3 firmware v1.10+)
Default: Enabled
    </Help>
        <Item label="Fully Enabled" value="0" />
        <Item label="Disable When Motion" value="1" />
        <Item label="Fully Disabled" value="2" />
  </Value>
  <Value type="byte" index="8" label="Stay Awake Time?" min="8" max="255" value="30" byteSize="1" setting_type="zwave" fw="1.08,1.09,1.10,1.11,1.12,1.13,1.08EU,1.09EU,1.10EU,1.11EU,1.12EU,1.13EU">
    <Help>
Set the timeout of awake after the Wake Up CC is sent out. (Works on Firmware v1.08 only)
Range: 8~255
Default: 30
Note: May help if config parameters aren't making it before device goes back to sleep.
    </Help>
  </Value>
<Value type="list" index="regionOverride" label="Region Override" min="0" max="3" value="0" setting_type="preference" fw="1.06,1.07,1.08,1.09,1.10,1.11,1.12,1.13,1.06EU,1.07EU,1.08EU,1.09EU,1.10EU,1.11EU,1.12EU,1.13EU">
    <Help>
The device handler tries to automatically detect the region of your firmware (US, EU, AU). If it is detecting it incorrectly you can change it here.
Default: Off
    </Help>
        <Item label="Off" value="0" />
        <Item label="US" value="1" />
        <Item label="EU" value="2" />
        <Item label="AU" value="3" />
  </Value>
  <Value type="boolean" index="enableDebugging" label="Enable Debug Logging?" value="true" setting_type="preference" fw="1.06,1.07,1.08,1.09,1.10,1.11,1.12,1.13,1.06EU,1.07EU,1.08EU,1.09EU,1.10EU,1.11EU,1.12EU,1.13EU">
    <Help>

    </Help>
  </Value>
</configuration>
'''
}
