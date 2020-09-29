/**
 *  Viconics/Schneider Room Controller
 *
 *  Copyright 2020
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 * 	  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 */
import groovy.json.JsonOutput
import physicalgraph.zigbee.zcl.DataType
metadata {
	definition(name: "Viconics Schneider Room Controller", namespace: "smartthings", author: "SmartThings", ocfDeviceType: "oic.d.thermostat", mcdSync: true) {

		capability "Actuator"
		capability "Sensor"
		capability "Temperature Measurement"
		capability "Relative Humidity Measurement"
		capability "Thermostat"
		capability "Thermostat Mode"
		capability "Fan Speed"
		capability "Thermostat Fan Mode"
		capability "Thermostat Cooling Setpoint"
		capability "Thermostat Heating Setpoint"
		capability "Thermostat Operating State"
		capability "Configuration"
		capability "Health Check"
		capability "Refresh"

		//Viconics VT8350
		fingerprint manufacturer: "Viconics", model: "254-143", deviceJoinName: "Viconics Thermostat", mnmn: "SmartThings", vid: "SmartThings-smartthings-Viconics_Schneider_Room_Controller_Fan" // VT8350 Low Voltage Fan Coil Controller and Zone Controller, Raw Description 0A 0104 0301 00 0A 0201 0202 0405 0402 0406 0204 0000 0004 0003 0005 0B 0201 0202 0405 0402 0406 0204 0000 0004 0003 0005 0500
		//fingerprint profileId: "0104", inClusters: "0000,0003,0201,0202,0204,0405", outClusters: "0402,0405", manufacturer: "Viconics", model: "254-143", deviceJoinName: "VT8350"
	}
}

private getTHERMOSTAT_CLUSTER() { 0x0201 }
private getTHERMOSTAT_UI_CONFIGURATION_CLUSTER() { 0x0204 }
private getRELATIVE_HUMIDITY_CLUSTER() {0x405}
private getRELATIVE_HUMIDITY_MEASURED_VALUE() {0x0000}
private getTEMPERATURE_DISPLAY_MODE() {0x0000}
private getLOCAL_TEMPERATURE() {0x0000}
private getCOOLING_SETPOINT() { 0x0011 }
private getHEATING_SETPOINT() { 0x0012 }
private getCOOLING_SETPOINT_UNOCCUPIED() { 0x0013 }
private getHEATING_SETPOINT_UNOCCUPIED() { 0x0014 }
private getCUSTOM_HUMIDITY() { 0x07a6 }
private getCUSTOM_THERMOSTAT_MODE() { 0x0687 }
private getCUSTOM_FAN_SPEED() { 0x0688 }
private getCUSTOM_FAN_MODE() { 0x0698 }
private getCUSTOM_OCCUPANCY() { 0x0c50 }
private getCUSTOM_THERMOSTAT_OPERATING_STATE() { 0x06BF }
private getUNOCCUPIED_SETPOINT_CHILD_DEVICE_ID() {1}
private getTHERMOSTAT_MODE_OFF() { 0x00 }
private getTHERMOSTAT_MODE_AUTO() { 0x01 }
private getTHERMOSTAT_MODE_COOL() { 0x03 }
private getTHERMOSTAT_MODE_HEAT() { 0x04 }

private getFAN_MODE_MAP() {
	[
			"04":"on",
			"05":"auto"
	]
}

private getTHERMOSTAT_MODE_MAP() {
	[
			"00":"off",
			"01":"auto",
			"02":"cool",
			"03":"heat",
			"04":"heat"
	]
}

private getTHERMOSTAT_OPERATING_STATE_MAP() {
	[
			"00":"idle",
			"01":"cooling",
			"02":"heating"
	]
}

def installed() {
	log.debug "installed"

	sendEvent(name: "checkInterval", value: 2 * 60 * 60 + 2 * 60, displayed: false, data: [protocol: "zigbee", hubHardwareId: device.hub.hardwareID, offlinePingable: "1"])
	state.supportedFanModes = ["on", "auto"]
	state.supportedThermostatModes = ["off", "auto", "cool", "heat"]

	sendEvent(name: "supportedThermostatFanModes", value: JsonOutput.toJson(state.supportedFanModes), displayed: false)
	sendEvent(name: "supportedThermostatModes", value: JsonOutput.toJson(state.supportedThermostatModes), displayed: false)
	sendEvent(name: "coolingSetpointRange", value: coolingSetpointRange, displayed: false)
	sendEvent(name: "heatingSetpointRange", value: heatingSetpointRange, displayed: false)
}

private void createChildThermostat() {
	log.debug "Creating child thermostat to handle unoccupied cooling/heating setpoints"
	def label = "Unoccupied setpoints"
	def childName = "${device.displayName} " + label

	def child = addChildDevice("Child Setpoints", "${device.deviceNetworkId}:1", device.hubId,
			[completedSetup: true, label: childName, isComponent: true, componentName: "childSetpoints", componentLabel: label]
	)

	child.sendEvent(name: "coolingSetpoint", value: 20.0, unit: "C")
	child.sendEvent(name: "heatingSetpoint", value: 21.0, unit: "C")
	log.debug "child.inspect() ${child}"
}


def parse(String description) {
	def eventMap = zigbee.getEvent(description)
	def result = []

	if (description?.startsWith("humidity: ")){
		// Viconics VT8350 humidity reports are parsed as floating point numbers (range 0 - 1%)
		def humidityVal = (description - "humidity: " - "%").trim()
		if (humidityVal.isNumber()) {
			humidityVal = new BigDecimal(humidityVal) * 100
		}
		eventMap.name 	= "humidity"
		eventMap.value 	= humidityVal
		eventMap.unit 	= "%"
	}
	/*} else if (eventMap) {
		log.debug "eventMap: ${eventMap.inspect()}"
		// Viconics VT8350 humidity reports are parsed as floating point numbers (range 0 - 1%)
		if(eventMap.name == "humidity") {
			eventMap.value = eventMap.value * 100
		}
		result = createEvent(eventMap)
	}*/ else {
		eventMap = [:]
		def descMap = zigbee.parseDescriptionAsMap(description)

		if((descMap.clusterInt == THERMOSTAT_CLUSTER && descMap.attrId)){
			def attributeInt = zigbee.convertHexToInt(descMap.attrId)

			if (attributeInt == COOLING_SETPOINT) {
				log.debug "COOLING SETPOINT OCCUPIED, descMap.value: ${descMap.value}"
				eventMap.name = "coolingSetpoint"
				eventMap.value = getTemperature(descMap.value)
				eventMap.unit = temperatureScale
			} else if (attributeInt == HEATING_SETPOINT) {
				log.debug "HEATING SETPOINT OCCUPIED, descMap.value: ${descMap.value}"
				eventMap.name = "heatingSetpoint"
				eventMap.value = getTemperature(descMap.value)
				eventMap.unit = temperatureScale
			} else if(attributeInt == COOLING_SETPOINT_UNOCCUPIED) {
				log.debug "COOLING SETPOINT UNOCCUPIED, descMap.value: ${descMap.value}"
				def childEvent = [:]
				childEvent.name = "coolingSetpoint"
				childEvent.value = getTemperature(descMap.value)
				childEvent.unit = temperatureScale
				sendEventToChild(UNOCCUPIED_SETPOINT_CHILD_DEVICE_ID, childEvent)
			} else if (attributeInt == HEATING_SETPOINT_UNOCCUPIED) {
				log.debug "HEATING SETPOINT UNOCCUPIED, descMap.value: ${descMap.value}"
				def childEvent = [:]
				childEvent.name = "heatingSetpoint"
				childEvent.value = getTemperature(descMap.value)
				childEvent.unit = temperatureScale
				sendEventToChild(UNOCCUPIED_SETPOINT_CHILD_DEVICE_ID, childEvent)
			} else if (attributeInt == LOCAL_TEMPERATURE) {
				log.debug "LOCAL TEMPERATURE, descMap.value: ${descMap.value}"
				eventMap.name = "temperature"
				eventMap.value = getTemperature(descMap.value)
				eventMap.unit = temperatureScale
			} else if (attributeInt == CUSTOM_HUMIDITY) {
				log.debug "CUSTOM HUMIDITY, descMap.value: ${descMap.value}"
				eventMap.name = "humidity"
				eventMap.value = Integer.parseInt(descMap.value, 16)
				eventMap.unit = "%"
			} else if (attributeInt == CUSTOM_FAN_MODE) {
				// this device doesn't report fan mode concrete values (it responds for query, but it didn't send any values)
				log.debug "CUSTOM FAN MODE, descMap.value: ${descMap.value}"
			} else if (attributeInt == CUSTOM_FAN_SPEED) {
				// VT8350 reports fan speed 3 as AUTO
				log.debug "CUSTOM FAN SPEED, descMap.value: ${descMap.value}"
				def sliderValue = mapFanSpeedSliderValue(descMap.value)
				if (sliderValue < 4) {
					eventMap.name = "fanSpeed"
					eventMap.value = sliderValue
					result << createEvent([name:"thermostatFanMode", value: "on", data: [supportedThermostatFanModes: state.supportedFanModes]])
				} else {
					result << createEvent([name:"thermostatFanMode", value: "auto", data:[supportedThermostatFanModes: state.supportedFanModes]])
				}
			} else if (attributeInt == CUSTOM_THERMOSTAT_MODE) {
				log.debug "CUSTOM THERMOSTAT MODE, descMap.value: ${descMap.value}"
				eventMap.name = "thermostatMode"
				eventMap.value = THERMOSTAT_MODE_MAP[descMap.value]
				eventMap.data = [supportedThermostatModes: state.supportedThermostatModes]
			} else if (attributeInt == CUSTOM_THERMOSTAT_OPERATING_STATE) {
				log.debug "CUSTOM THERMOSTAT OPERATING STATE, descMap.value: ${descMap.value}"
				eventMap.name = "thermostatOperatingState"
				eventMap.value = THERMOSTAT_OPERATING_STATE_MAP[descMap.value]
			} else if(attributeInt == CUSTOM_OCCUPANCY) {
				log.debug "CUSTOM OCCUPANCY, descMap.value: ${descMap.value}"
			} else {
				log.debug "descMap.inspect(): ${descMap.inspect()}"
			}
		}
	}
	result << createEvent(eventMap)
	//log.debug "Description ${description} parsed to ${result}"
	return result
}

private sendEventToChild(childNumber, event) {
	def child = childDevices?.find { getChildId(it.deviceNetworkId) == childNumber }

	if (child) {
		log.debug "Sending ${event.name} event to $child.displayName"
		child?.sendEvent(event)
	} else {
		log.debug "Child device $childNumber not found!"
	}
}

def setCoolingSetpoint(degrees) {
	setSetpoint(degrees, COOLING_SETPOINT)
}

def setHeatingSetpoint(degrees) {
	setSetpoint(degrees, HEATING_SETPOINT)
}

def setChildCoolingSetpoint(deviceNetworkId, degrees) {
	log.debug "deviceNetworkId: ${deviceNetworkId} degrees: ${degrees}"
	def switchId = getChildId(deviceNetworkId)
	if (switchId != null) {
		setSetpoint(degrees, COOLING_SETPOINT_UNOCCUPIED)
	}
}

def setChildHeatingSetpoint(deviceNetworkId, degrees) {
	log.debug "deviceNetworkId: ${deviceNetworkId} degrees: ${degrees}"

	def switchId = getChildId(deviceNetworkId)
	if (switchId != null) {
		setSetpoint(degrees, HEATING_SETPOINT_UNOCCUPIED)
	}
}

def getChildId(deviceNetworkId) {
	def split = deviceNetworkId?.split(":")
	return (split.length > 1) ? split[1] as Integer : null
}

def setSetpoint(degrees, setpointAttr) {
	log.debug "degrees: ${degrees}, setpointAttr: ${setpointAttr}"
	if (degrees != null && setpointAttr != null) {
		log.debug "temperatureScale: ${temperatureScale}"
		def celsius = (temperatureScale == "C") ? degrees : fahrenheitToCelsius(degrees)
		celsius = (celsius as Double).round(2)

		delayBetween([
				zigbee.writeAttribute(THERMOSTAT_CLUSTER, setpointAttr, DataType.INT16, zigbee.convertToHexString(celsius * 100)),
				zigbee.readAttribute(THERMOSTAT_CLUSTER, setpointAttr)
		], 500)
	}
}

def setThermostatFanMode(mode) {
	if (state.supportedFanModes?.contains(mode)) {
		switch (mode) {
			case "on":
				setFanSpeed(1)
				break
			case "auto":
				setFanSpeed(4)
				break
		}
	} else {
		log.debug "Unsupported fan mode $mode"
	}
}

def setFanSpeed(speed) {
	log.debug "setFanSpeed: ${speed}"

	if (speed == 0 || speed >= 4) { //if by any chance user selects 0 or a value higher than 3, it fan will be set to AUTO
		speed = 3
	} else {
		speed = speed - 1
	}
	delayBetween([
			zigbee.writeAttribute(THERMOSTAT_CLUSTER, CUSTOM_FAN_SPEED, DataType.ENUM8, speed),
			zigbee.readAttribute(THERMOSTAT_CLUSTER, CUSTOM_THERMOSTAT_MODE),
			zigbee.readAttribute(THERMOSTAT_CLUSTER, CUSTOM_FAN_SPEED)
	], 500)
}

def setThermostatMode(mode) {
	log.debug "set mode $mode (supported ${state.supportedThermostatModes})"
	if (state.supportedThermostatModes?.contains(mode)) {
		switch (mode) {
			case "auto":
				auto()
				break
			case "cool":
				cool()
				break
			case "heat":
				heat()
				break
			case "off":
				off()
				break
		}
	} else {
		log.debug "Unsupported mode $mode"
	}
}

def off() {
	delayBetween([
			zigbee.writeAttribute(THERMOSTAT_CLUSTER, CUSTOM_THERMOSTAT_MODE, DataType.ENUM8, THERMOSTAT_MODE_OFF),
			zigbee.readAttribute(THERMOSTAT_CLUSTER, CUSTOM_THERMOSTAT_MODE)
	], 500)
}

def auto() {
	delayBetween([
			zigbee.writeAttribute(THERMOSTAT_CLUSTER, CUSTOM_THERMOSTAT_MODE, DataType.ENUM8, THERMOSTAT_MODE_AUTO),
			zigbee.readAttribute(THERMOSTAT_CLUSTER, CUSTOM_THERMOSTAT_MODE)
	], 500)
}

def cool() {
	delayBetween([
			zigbee.writeAttribute(THERMOSTAT_CLUSTER, CUSTOM_THERMOSTAT_MODE, DataType.ENUM8, THERMOSTAT_MODE_COOL),
			zigbee.readAttribute(THERMOSTAT_CLUSTER, CUSTOM_THERMOSTAT_MODE)
	], 500)
}

def heat() {
	delayBetween([
			zigbee.writeAttribute(THERMOSTAT_CLUSTER, CUSTOM_THERMOSTAT_MODE, DataType.ENUM8, THERMOSTAT_MODE_HEAT),
			zigbee.readAttribute(THERMOSTAT_CLUSTER, CUSTOM_THERMOSTAT_MODE)
	], 500)
}

def ping() {
	log.debug "ping"
	refresh()
}

def refresh() {
	log.debug "refresh"
	getRefreshCommands()
}

def getRefreshCommands() {
	def refreshCommands = []

	refreshCommands += zigbee.readAttribute(RELATIVE_HUMIDITY_CLUSTER, RELATIVE_HUMIDITY_MEASURED_VALUE)
	refreshCommands += zigbee.readAttribute(THERMOSTAT_CLUSTER, CUSTOM_HUMIDITY)

	refreshCommands += zigbee.readAttribute(THERMOSTAT_CLUSTER, LOCAL_TEMPERATURE)
	refreshCommands += zigbee.readAttribute(THERMOSTAT_CLUSTER, COOLING_SETPOINT)
	refreshCommands += zigbee.readAttribute(THERMOSTAT_CLUSTER, HEATING_SETPOINT)
	refreshCommands += zigbee.readAttribute(THERMOSTAT_CLUSTER, COOLING_SETPOINT_UNOCCUPIED)
	refreshCommands += zigbee.readAttribute(THERMOSTAT_CLUSTER, HEATING_SETPOINT_UNOCCUPIED)

	refreshCommands += zigbee.readAttribute(THERMOSTAT_CLUSTER, CUSTOM_FAN_SPEED)
	refreshCommands += zigbee.readAttribute(THERMOSTAT_CLUSTER, CUSTOM_FAN_MODE)
	refreshCommands += zigbee.readAttribute(THERMOSTAT_CLUSTER, CUSTOM_THERMOSTAT_MODE) // formerly THERMOSTAT MODE: 0x001C
	refreshCommands += zigbee.readAttribute(THERMOSTAT_CLUSTER, CUSTOM_THERMOSTAT_OPERATING_STATE)

	refreshCommands += zigbee.readAttribute(THERMOSTAT_CLUSTER, 0x0650)	//occupancy command
	refreshCommands += zigbee.readAttribute(THERMOSTAT_CLUSTER, CUSTOM_OCCUPANCY)
	refreshCommands += zigbee.readAttribute(THERMOSTAT_UI_CONFIGURATION_CLUSTER, TEMPERATURE_DISPLAY_MODE)

	refreshCommands
}

def configure() {
	log.debug "Configuration"

	if(!childDevices) {
		createChildThermostat()
	}

	def configurationCommands = []
	// todo: check if following binding is necessary here
	configurationCommands += "zdo bind 0x${device.deviceNetworkId} 1 0xA 0x201 {${device.zigbeeId}} {}"
	configurationCommands += "zdo bind 0x${device.deviceNetworkId} 1 0xA 0x405 {${device.zigbeeId}} {}"

	configurationCommands += zigbee.configureReporting(THERMOSTAT_CLUSTER, CUSTOM_THERMOSTAT_MODE, DataType.ENUM8, 1, 300, 1) //formerly THERMOSTAT MODE: 0x001C
	configurationCommands += zigbee.configureReporting(THERMOSTAT_CLUSTER, LOCAL_TEMPERATURE, DataType.INT16, 10, 60, 50)
	configurationCommands += zigbee.configureReporting(THERMOSTAT_CLUSTER, COOLING_SETPOINT, DataType.INT16, 1, 300, 10)
	configurationCommands += zigbee.configureReporting(THERMOSTAT_CLUSTER, HEATING_SETPOINT, DataType.INT16, 1, 300, 10)
	configurationCommands += zigbee.configureReporting(THERMOSTAT_CLUSTER, COOLING_SETPOINT_UNOCCUPIED, DataType.INT16, 1, 300, 10)
	configurationCommands += zigbee.configureReporting(THERMOSTAT_CLUSTER, HEATING_SETPOINT_UNOCCUPIED, DataType.INT16, 1, 300, 10)
	//configurationCommands += zigbee.configureReporting(THERMOSTAT_CLUSTER, 0x0A58, 0x10, 1, 300, 1) //GFan
	configurationCommands += zigbee.configureReporting(THERMOSTAT_CLUSTER, CUSTOM_FAN_MODE, DataType.ENUM8, 1, 300, 1)
	configurationCommands += zigbee.configureReporting(THERMOSTAT_CLUSTER, CUSTOM_FAN_SPEED, DataType.ENUM8, 1, 300, 1)
	configurationCommands += zigbee.configureReporting(THERMOSTAT_CLUSTER, CUSTOM_THERMOSTAT_OPERATING_STATE, DataType.ENUM8, 1, 300, 1)
	configurationCommands += zigbee.configureReporting(THERMOSTAT_CLUSTER, CUSTOM_OCCUPANCY, DataType.ENUM8, 1, 300, 1)
	configurationCommands += zigbee.configureReporting(THERMOSTAT_UI_CONFIGURATION_CLUSTER, TEMPERATURE_DISPLAY_MODE, DataType.ENUM8, 1, 300, 1)
	configurationCommands += zigbee.configureReporting(THERMOSTAT_CLUSTER, CUSTOM_HUMIDITY, DataType.UINT16, 60, 300, 5)
	configurationCommands += zigbee.configureReporting(RELATIVE_HUMIDITY_CLUSTER, RELATIVE_HUMIDITY_MEASURED_VALUE, DataType.UINT16, 60, 300, 5)

	delayBetween(getRefreshCommands()+configurationCommands)
}

def isViconicsVT8350() {
	device.getDataValue("model") == "254-143" // Viconics VT8350 Low Voltage Fan Coil Controller and Zone Controller
}

def getCoolingSetpointRange() {
	(getTemperatureScale() == "C") ? [12, 37.5] : [54, 100]
}
def getHeatingSetpointRange() {
	(getTemperatureScale() == "C") ? [4.5, 32] : [40, 90]
}

def getTemperature(value) {
	if (value != null) {
		def celsius = Integer.parseInt(value, 16) / 100
		if (temperatureScale == "C") {
			return celsius//Math.round(celsius)
		} else {
			return celsiusToFahrenheit(celsius)//Math.round(celsiusToFahrenheit(celsius))
		}
	}
}

def mapFanSpeedSliderValue(rawValue) {
	log.debug "mapFanSpeedSliderValue: ${rawValue}"
	//Map current fan value to the Fan Speed slider
	def resultValue

	switch(rawValue) {
		case "00": // low
			resultValue = 1
			break
		case "01": // medium
			resultValue = 2
			break
		case "02": // high
			resultValue = 3
			break
		case "03": // auto
		default:
			resultValue = 4
			break
	}
	resultValue
}