/**
 *  Copyright 2017 ckpt-martin
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
 *  eCozy ZigBee Thermostat
 *
 *  Author: ckpt-martin
 *
 *  Date: 2017-11-30
 */
 

metadata {
	definition (name: "eCozy ZigBee Thermostat", namespace: "ckpt-martin", author: "ckpt-martin") {
        capability "Thermostat"
        capability "Temperature Measurement"
        capability "Battery"
        capability "Actuator"
        capability "Polling"
        capability "Refresh"
        capability "Sensor"
        capability "Configuration"
        
        command "modeHeat"
        command "modeOff"
        command "modeAuto"
        command "increaseHeatSetpoint"
        command "decreaseHeatSetpoint"

		fingerprint profileId: "0104", endpointId: "03", inClusters: " 0000,0001,0003,000A,0020,0201,0204", outClusters: "0402", manufacturer: "eCozy", model: "Thermostat"
	}

preferences {
 		input("unitformat", "enum", title: "What unit format do you want to display temperature in SmartThings? (NOTE: Thermostat displays Celsius regardless.)", options: ["Celsius", "Fahrenheit"], defaultValue: "Celsius", required: false, displayDuringSetup: false)
        input("lock", "enum", title: "Display Lock?", options: ["No", "Temperature", "Touchscreen"], defaultValue: "No", required: false, displayDuringSetup: false)
        input("tempcal", "enum", title: "Temperature adjustment.", options: ["+2.5", "+2.0", "+1.5", "+1.0", "+0.5", "0", "-0.5", "-1.0", "-1.5", "-2.0", "-2.5"], defaultValue: "0", required: false, displayDuringSetup: false)
}

	// simulator metadata
	simulator { }

	tiles(scale : 2) {
		multiAttributeTile(name:"thermostatMulti", type:"thermostat", width:6, height:4) {
			tileAttribute("device.temperature", key: "PRIMARY_CONTROL") {
				attributeState("default", label:'${currentValue}°')
			}
            tileAttribute("device.heatingSetpoint", key: "VALUE_CONTROL", label:'${currentValue}°') {
				attributeState("VALUE_UP", action:"increaseHeatSetpoint")
                attributeState("VALUE_DOWN", action:"decreaseHeatSetpoint")
			}
			tileAttribute("device.batteryState", key: "SECONDARY_CONTROL") {
                attributeState("battery_ok", label:'Battery OK', icon:"st.arlo.sensor_battery_4")
                attributeState("battery_low", label:'BATTERY LOW!', icon:"st.arlo.sensor_battery_1")
			}
			tileAttribute("device.thermostatOperatingState", key: "OPERATING_STATE") {
				attributeState("off", backgroundColor:"#44b621", label:'Off')
				attributeState("idle", backgroundColor:"#44b621", label:'Idle')
				attributeState("heating", backgroundColor:"#ffa81e", label:'Heating')
			}
		}
        standardTile("configure", "device.configure", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
			state "configure", label:"", action:"configuration.configure", icon:"st.secondary.configure"
		}
        standardTile("refresh", "device.refresh", decoration: "flat", width: 2, height: 2) {
            state "default", action:"refresh", icon:"st.secondary.refresh"
        }
        valueTile("hwver", "device.hwver", decoration: "flat", width: 1, height: 1) {
            state "default", label:'HW Version\n${currentValue}'
        }
        valueTile("swver", "device.swver", decoration: "flat", width: 1, height: 1) {
            state "default", label:'SW Version\n${currentValue}'
        }
        valueTile("placeholder", "device.placeholder", decoration: "flat", width: 2, height: 1) {
            state "default", label:""
        }
        standardTile("setThermostatMode", "device.thermostatMode", decoration: "flat", width: 2, height: 2) {
            state "auto", action:"modeOff", icon:"st.thermostat.heat-auto", nextState: "off"
            state "off", action:"modeHeat", icon:"st.thermostat.heating-cooling-off", nextState: "heat"
            state "heat", action:"modeAuto", icon:"st.thermostat.heat", nextState: "auto"
        }
        main ("thermostatMulti")
        details(["thermostatMulti", "hwver", "swver", "configure", "refresh", "placeholder"])
    }
}

// parse events into attributes
def parse(String description) {
	log.debug "Parse description $description"
	def map = [:]
	if (description?.startsWith("read attr -")) {
		def descMap = parseDescriptionAsMap(description)
		log.debug "Desc Map: $descMap"
		if (descMap.cluster == "0201" && descMap.attrId == "0000")
        {
			log.debug "TEMP: $descMap.value"
			map.name = "temperature"
			map.value = getTemperature(descMap.value)
            if (descMap.value == "8000")	// 0x8000 invalid temperature
            {
            	map.value = "--"
            }
		}
        else if (descMap.cluster == "0001" && descMap.attrId == "0020")
        {
			log.debug "BATTERY VOLTAGE: $descMap.value"
			map.name = "batteryState"
			def batteryVoltage = getBatteryVoltage(descMap.value)
			log.debug "BATTERY VOLTAGE: $batteryVoltage"
            if (batteryVoltage < 25)
            {
                map.value = "battery_low"
            }
            else {
                map.value = "battery_ok"
            }
		}
        else if (descMap.cluster == "0000" && descMap.attrId == "0001")
        {
			log.debug "APPLICATION VERSION: $descMap.value"
			map.name = "swver"
			map.value = descMap.value
		}
        else if (descMap.cluster == "0000" && descMap.attrId == "0003")
        {
			log.debug "HW VERSION: $descMap.value"
			map.name = "hwver"
			map.value = descMap.value
		}
        else if (descMap.cluster == "0201" && descMap.attrId == "0010")
        {
			log.debug "TEMP CALIBRATION: $descMap.value"
		}
        else if (descMap.cluster == "0201" && descMap.attrId == "0012")
        {
			log.debug "HEATING SETPOINT: $descMap.value"
			map.name = "heatingSetpoint"
			map.value = getTemperature(descMap.value)
            if (descMap.value == "8000")		//0x8000
            {
                map.value = "--"
            }
		}
        else if (descMap.cluster == "0201" && descMap.attrId == "0008")
        {
        	log.debug "THERMOSTAT STATE: $descMap.value"
            map.name = "thermostatOperatingState"
            if (descMap.value == "00")
            {
            	map.value = "off"
            }
            else if (descMap.value < "10")
            {
            	map.value = "idle"
            }
            else
            {
            	map.value = "heating"
            }
        }
        else if (descMap.cluster == "0201" && descMap.attrId == "001c")
        {
        	log.debug "THERMOSTAT MODE: $descMap.value"
            map.name = "thermostatMode"
            if (descMap.value == "00")
            {
            	map.value = "off"
            }
            else if (descMap.value == "01")
            {
            	map.value = "auto"
            }
            else if (descMap.value == "04")
            {
            	map.value = "heat"
            }
        }
        else if (descMap.cluster == "0204" && descMap.attrId == "0001")
        {
			log.debug "LOCK DISPLAY MODE: $descMap.value"
			map.name = "lockMode"
            if (descMap.value == "00")
            {
				map.value = "unlocked"
            }
            else if (descMap.value == "02")
            {
				map.value = "templock"
            }
            else if (descMap.value == "04")
            {
				map.value = "off"
            }
            else if (descMap.value == "05")
            {
				map.value = "off"
            }
		}
	}

	def result = null
	if (map) {
		result = createEvent(map)
	}
	log.debug "Parse returned $map"
	return result
}

def parseDescriptionAsMap(description) {
	(description - "read attr - ").split(",").inject([:]) { map, param ->
		def nameAndValue = param.split(":")
		map += [(nameAndValue[0].trim()):nameAndValue[1].trim()]
	}
}

def getBatteryVoltage(value) {
	if (value != null) {
    	log.debug("value $value")
		return Math.round(Integer.parseInt(value, 16))
	}
}

def getTemperature(value) {
	if (value != null) {
    	log.debug("value $value")
		def celsius = Integer.parseInt(value, 16) / 100
		if (settings.unitformat == "Fahrenheit") {
			return Math.round(celsiusToFahrenheit(celsius))
		} else {
			return Math.round(celsius)
		}
	}
}

def quickSetHeat(degrees) {
	setHeatingSetpoint(degrees)
}

def setHeatingSetpoint(degrees) {
	if (degrees != null) {
		def degreesInteger = Math.round(degrees)
        int temp;
	    temp = (Math.round(degrees * 2)) / 2

		log.debug "setHeatingSetpoint({$temp} ${temperatureScale})"
        def celsius = (settings.unitformat == "Fahrenheit") ? (fahrenheitToCelsius(temp)).round : temp
		def cmds =
	        zigbee.writeAttribute(0x201, 0x12, 0x29, hex(celsius * 100)) +
	        zigbee.readAttribute(0x201, 0x12) +	//Read Heat Setpoint
	        zigbee.readAttribute(0x201, 0x08)	//Read PI Heat demand
		fireCommand(cmds)
	}
}

def increaseHeatSetpoint()
{
    def currentMode = device.currentState("thermostatMode")?.value
    if (currentMode != "off")
    {
		float currentSetpoint = device.currentValue("heatingSetpoint")
    	float maxSetpoint
    	float step

    	if (settings.unitformat == "Fahrenheit")
    	{
        	maxSetpoint = 86
        	step = 1
    	}
    	else
    	{
        	maxSetpoint = 30
        	step = 1
    	}

        if (currentSetpoint < maxSetpoint)
        {
            currentSetpoint = currentSetpoint + step
            quickSetHeat(currentSetpoint)
        }
    }
}

def decreaseHeatSetpoint()
{
    def currentMode = device.currentState("thermostatMode")?.value
    if (currentMode != "off")
    {
        float currentSetpoint = device.currentValue("heatingSetpoint")
        float minSetpoint
        float step

        if (settings.unitformat == "Fahrenheit")
        {
            minSetpoint = 41
            step = 1
        }
        else
        {
            minSetpoint = 5
            step = 1
        }

    	if (currentSetpoint > minSetpoint)
    	{
        	currentSetpoint = currentSetpoint - step
        	quickSetHeat(currentSetpoint)
    	}
    }
}

def modeHeat() {
	log.debug "modeHeat"
	sendEvent("name":"thermostatMode", "value":"heat")
    zigbee.writeAttribute(0x201, 0x001C, 0x30, 0x04)
}

def modeOff() {
	log.debug "modeOff"
	sendEvent("name":"thermostatMode", "value":"off")
    zigbee.writeAttribute(0x201, 0x001C, 0x30, 0x00)
}

def modeAuto() {
	log.debug "modeAuto"
	sendEvent("name":"thermostatMode", "value":"auto")
    zigbee.writeAttribute(0x201, 0x001C, 0x30, 0x01)
}

def configure() {
	def cmds =
        //Cluster ID (0x0201 = Thermostat Cluster), Attribute ID, Data Type, Payload (Min report, Max report, On change trigger)
        zigbee.configureReporting(0x0201, 0x0000, 0x29, 30, 0, 0x0064) + 	//Attribute ID 0x0000 = local temperature, Data Type: S16BIT
    	zigbee.configureReporting(0x0201, 0x0012, 0x29, 30, 0, 0x0064) +  	//Attribute ID 0x0012 = occupied heat setpoint, Data Type: S16BIT
        zigbee.configureReporting(0x0201, 0x001C, 0x30, 1, 0, 1) +   	//Attribute ID 0x001C = system mode, Data Type: 8 bits enum
        zigbee.configureReporting(0x0201, 0x0008, 0x20, 300, 7200, 0x05)   //Attribute ID 0x0008 = pi heating demand, Data Type: U8BIT
        
        //Cluster ID (0x0001 = Power)
        zigbee.configureReporting(0x0001, 0x0020, 0x20, 600, 21600, 0x01) 	//Attribute ID 0x0020 = battery voltage, Data Type: U8BIT
	log.info "configure() --- cmds: $cmds"
    return refresh() + cmds
}

def refresh() {
	def cmds =
        //Read the configured variables
		zigbee.readAttribute(0x001, 0x0020) +	//Read BatteryVoltage
		zigbee.readAttribute(0x201, 0x0000) +	//Read LocalTemperature
		zigbee.readAttribute(0x201, 0x0008) +	//Read PIHeatingDemand
    	zigbee.readAttribute(0x201, 0x0012) +	//Read OccupiedHeatingSetpoint
        zigbee.readAttribute(0x201, 0x001C) +	//Read SystemMode
        zigbee.readAttribute(0x201, 0x001E) +	//Read ThermostatRunningMode
        zigbee.readAttribute(0x201, 0x0010) +	//Read LocalTemperatureCalibration
        zigbee.readAttribute(0x201, 0x0080) +	//Read OpenWindowSensor
        zigbee.readAttribute(0x000, 0x0003) +	//Read HW Version
        zigbee.readAttribute(0x000, 0x0001) +	//Read Application Version
    	zigbee.readAttribute(0x204, 0x0001)		//Read KeypadLockout

	// Other Available Attributes
        //zigbee.readAttribute(0x000, 0x0000) +	//Read ZCLVersion
        //zigbee.readAttribute(0x000, 0x0001) +	//Read ApplicationVersion
        //zigbee.readAttribute(0x000, 0x0002) +	//Read StackVersion
        //zigbee.readAttribute(0x000, 0x0003) +	//Read HWVersion
        //zigbee.readAttribute(0x000, 0x0004) +	//Read ManufacturerName
        //zigbee.readAttribute(0x000, 0x0005) +	//Read ModelIdentifier
        //zigbee.readAttribute(0x000, 0x0006) +	//Read DateCode
        //zigbee.readAttribute(0x000, 0x0007) +	//Read PowerSource
        //zigbee.readAttribute(0x000, 0x0010) +	//Read LocationDescription
        //zigbee.readAttribute(0x000, 0x0011) +	//Read PhysicalEnvironment
        //zigbee.readAttribute(0x000, 0x0012) +	//Read DeviceEnabled
        //zigbee.readAttribute(0x000, 0x0014) +	//Read DisableLocalConfig
		//zigbee.readAttribute(0x020, 0x0000) +	//Read Check-inInterval
        //zigbee.readAttribute(0x020, 0x0001) +	//Read LongPollInterval
        //zigbee.readAttribute(0x020, 0x0002) +	//Read ShortPollInterval
        //zigbee.readAttribute(0x020, 0x0003) +	//Read FastPollTimeout
        //zigbee.readAttribute(0x020, 0x0004) +	//Read Check-inIntervalMin
        //zigbee.readAttribute(0x020, 0x0005) +	//Read LongPollIntervalMin
        //zigbee.readAttribute(0x020, 0x0006) +	//Read FastPollTimeoutMax
        //zigbee.readAttribute(0x00a, 0x0000) +	//Read Time
        //zigbee.readAttribute(0x00a, 0x0001) +	//Read TimeStatus
        //zigbee.readAttribute(0x00a, 0x0002) +	//Read TimeZone
        //zigbee.readAttribute(0x00a, 0x0003) +	//Read DstStart
        //zigbee.readAttribute(0x00a, 0x0004) +	//Read DstEnd
        //zigbee.readAttribute(0x00a, 0x0005) +	//Read DstShift
        //zigbee.readAttribute(0x00a, 0x0006) +	//Read StandardTime
        //zigbee.readAttribute(0x00a, 0x0007) +	//Read LocalTime
        //zigbee.readAttribute(0x00a, 0x0008) +	//Read LastSetTime
        //zigbee.readAttribute(0x00a, 0x0009) +	//Read ValidUntilTime
		//zigbee.readAttribute(0x001, 0x0030) +	//Read BatteryManufacturer
		//zigbee.readAttribute(0x001, 0x0031) +	//Read BatterySize
		//zigbee.readAttribute(0x001, 0x0032) +	//Read BatteryAHrRating
		//zigbee.readAttribute(0x001, 0x0033) +	//Read BatteryQuantity
		//zigbee.readAttribute(0x001, 0x0034) +	//Read BatteryRatedVoltage
		//zigbee.readAttribute(0x001, 0x0035) +	//Read BatteryAlarmMask
		//zigbee.readAttribute(0x001, 0x0036) +	//Read BatteryVoltageMinThreshold
        //zigbee.readAttribute(0x201, 0x0030) +	//Read SetpointChangeSource
        //zigbee.readAttribute(0x201, 0x0032) +	//Read SetpointChangeSourceTimestamp
		//zigbee.readAttribute(0x201, 0x0003) +	//Read AbsMinHeatSetpointLimit
		//zigbee.readAttribute(0x201, 0x0004) +	//Read AbsMaxHeatSetpointLimit
    	//zigbee.readAttribute(0x201, 0x0015) +	//Read MinHeatSetpointLimit
    	//zigbee.readAttribute(0x201, 0x0016) +	//Read MaxHeatSetpointLimit
    	//zigbee.readAttribute(0x201, 0x0019) +	//Read MinSetpointDeadBand
    	//zigbee.readAttribute(0x201, 0x001A) +	//Read RemoteSensing
    	//zigbee.readAttribute(0x201, 0x001B) +	//Read ControlSequenceOfOperation
        //zigbee.readAttribute(0x201, 0x0020) +	//Read StartOfWeek
        //zigbee.readAttribute(0x201, 0x0021) +	//Read NumberOfWeeklyTransitions
        //zigbee.readAttribute(0x201, 0x0022) +	//Read NumberOfDailyTransitions
        //zigbee.readAttribute(0x201, 0x0023) +	//Read TemperatureSetpointHold
        //zigbee.readAttribute(0x201, 0x0031) +	//Read SetpointChangeAmount
	log.info "refresh() --- cmds: $cmds"
    return cmds
}

def updated() {
	log.debug "updated called"
    def tempmode = 0x00
    def lockmode = 0x00
    def tempadjust = 0x00
    def windowmode = 0x01
    def firemode = 0x00

	log.info "lock : $settings.lock"
    if (settings.lock == "Temperature") {
    	log.info "Temperature lock selected"
        lockmode = 0x02
    }
    else if (settings.lock == "Touchscreen") {
    	log.info "Touchscreen lock selected"
        lockmode = 0x05
    }
    else {
    	log.info "No lock selected"
        lockmode = 0x00
        settings.lock = "No"
    }
    
	log.info "unitformat : $settings.unitformat"
    if (settings.unitformat == "Fahrenheit") {
    	log.info "Fahrenheit selected"
    }
    else {
    	log.info "Celsius set"
        settings.unitformat = "Celsius"
    }
    
	log.info "tempcal : $settings.tempcal"
    if (settings.tempcal == "+2.5") {
    	log.info "Temperature adjusted +2.5 degrees"
        tempadjust = 0x19
    }
    else if (settings.tempcal == "+2.0") {
    	log.info "Temperature adjusted +2.0 degrees"
        tempadjust = 0x14
    }
    else if (settings.tempcal == "+1.5") {
    	log.info "Temperature adjusted +1.5 degrees"
        tempadjust = 0x0f
    }
    else if (settings.tempcal == "+1.0") {
    	log.info "Temperature adjusted +1.0 degrees"
        tempadjust = 0x0a
    }
    else if (settings.tempcal == "+0.5") {
    	log.info "Temperature adjusted +0.5 degrees"
        tempadjust = 0x05
    }
    else if (settings.tempcal == "-0.5") {
    	log.info "Temperature adjusted -0.5 degrees"
        tempadjust = 0xd3
    }
    else if (settings.tempcal == "-1.0") {
    	log.info "Temperature adjusted -1.0 degrees"
        tempadjust = 0xd8
    }
    else if (settings.tempcal == "-1.5") {
    	log.info "Temperature adjusted -1.5 degrees"
        tempadjust = 0xed
    }
    else if (settings.tempcal == "-2.0") {
    	log.info "Temperature adjusted -2.0 degrees"
        tempadjust = 0xe2
    }
    else if (settings.tempcal == "-2.5") {
    	log.info "Temperature adjusted -2.5 degrees"
        tempadjust = 0xe7
    }
    else {
    	log.info "No temperature adjustment"
        tempadjust = 0x00
        settings.tempcal = "0"
    }

	log.info "openwindow detect : $settings.openwindow"
    if (settings.openwindow == "No") {
    	log.info "Open Window Detection disabled"
    }
    else {
    	log.info "Open Window Detection enabled"
        settings.openwindow = "Yes"
    }
    
//	log.info "firedetect : $settings.firedetect"
//    if (settings.firedetect == "Yes") {
//    	log.info "Fire Detection enabled"
//    }
//    else {
//    	log.info "Fire Detection disabled"
//        settings.firedetect = "No"
//    }
    def cmds = 
    	zigbee.writeAttribute(0x204, 0x0001, 0x30, lockmode) +
        zigbee.readAttribute(0x204, 0x0001) +
    	zigbee.writeAttribute(0x201, 0x0010, 0x28, tempadjust) +
        zigbee.readAttribute(0x201, 0x0010)
    log.info "updated() --- cmds: $cmds"
	fireCommand(cmds)
}

private fireCommand(List commands) {
    if (commands != null && commands.size() > 0) {
        log.trace("Executing commands:" + commands)
        for (String value : commands){
            sendHubCommand([value].collect {new physicalgraph.device.HubAction(it)})
        }
    }
}

private hex(value) {
	new BigInteger(Math.round(value).toString()).toString(16)
}
