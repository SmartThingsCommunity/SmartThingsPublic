/**
 *  Copyright 2018 SmartThings
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
 *	CentraLite Thermostat
 *
 *	Author: SRPOL
 *	Date: 2018-10-15
 */

import physicalgraph.zigbee.zcl.DataType

metadata {
	definition (name: "Zigbee Thermostat", namespace: "smartthings", author: "SmartThings", mnmn: "SmartThings", vid: "generic-thermostat-1") {
		capability "Actuator"
		capability "Temperature Measurement"
		capability "Thermostat"
		capability "Thermostat Mode"
		capability "Thermostat Fan Mode"
		capability "Thermostat Cooling Setpoint"
		capability "Thermostat Heating Setpoint"
		capability "Thermostat Operating State"
		capability "Configuration"
		capability "Battery"
		capability "Power Source"
		capability "Health Check"
		capability "Refresh"
		capability "Sensor"

		command "lowerHeatingSetpoint"
		command "raiseHeatingSetpoint"
		command "lowerCoolSetpoint"
		command "raiseCoolSetpoint"

		fingerprint profileId: "0104", inClusters: "0000,0001,0003,0004,0005,0020,0201,0202,0204,0B05", outClusters: "000A, 0019",  manufacturer: "LUX", model: "KONOZ", deviceJoinName: "LUX KONOz Thermostat"
	}

	tiles {
		multiAttributeTile(name:"temperature", type:"generic", width:3, height:2, canChangeIcon: true) {
			tileAttribute("device.temperature", key: "PRIMARY_CONTROL") {
				attributeState("temperature", label:'${currentValue}°', icon: "st.alarm.temperature.normal",
						backgroundColors:[
								// Celsius
								[value: 0, color: "#153591"],
								[value: 7, color: "#1e9cbb"],
								[value: 15, color: "#90d2a7"],
								[value: 23, color: "#44b621"],
								[value: 28, color: "#f1d801"],
								[value: 35, color: "#d04e00"],
								[value: 37, color: "#bc2323"],
								// Fahrenheit
								[value: 40, color: "#153591"],
								[value: 44, color: "#1e9cbb"],
								[value: 59, color: "#90d2a7"],
								[value: 74, color: "#44b621"],
								[value: 84, color: "#f1d801"],
								[value: 95, color: "#d04e00"],
								[value: 96, color: "#bc2323"]
						]
				)
			}
		}
		standardTile("thermostatMode", "device.thermostatMode", width:2, height:2, inactiveLabel: false, decoration: "flat") {
			state "cool", action: "thermostatMode.off", icon: "st.thermostat.cool", nextState: "..."
			state "off", action: "thermostatMode.heat", icon: "st.thermostat.heating-cooling-off", nextState: "..."
			state "heat", action: "thermostatMode.cool", icon: "st.thermostat.heat", nextState: "..."
			state "...", label: "Updating...", nextState:"..."
		}
		standardTile("thermostatFanMode", "device.thermostatFanMode", width:2, height:2, inactiveLabel: false, decoration: "flat") {
			state "auto", action:"thermostatFanMode.fanOn", nextState:"...", icon: "st.thermostat.fan-auto"
			state "on", action:"thermostatFanMode.fanAuto", nextState:"...", icon: "st.thermostat.fan-on"
			state "...", label: "Updating...", nextState:"...", backgroundColor:"#ffffff"
		}
		standardTile("lowerHeatingSetpoint", "device.heatingSetpoint", width:2, height:1, inactiveLabel: false, decoration: "flat") {
			state "heatingSetpoint", action:"lowerHeatingSetpoint", icon:"st.thermostat.thermostat-left"
		}
		valueTile("heatingSetpoint", "device.heatingSetpoint", width:2, height:1, inactiveLabel: false, decoration: "flat") {
			state "heatingSetpoint", label:'${currentValue}° heat', backgroundColor:"#ffffff"
		}
		standardTile("raiseHeatingSetpoint", "device.heatingSetpoint", width:2, height:1, inactiveLabel: false, decoration: "flat") {
			state "heatingSetpoint", action:"raiseHeatingSetpoint", icon:"st.thermostat.thermostat-right"
		}
		standardTile("lowerCoolSetpoint", "device.coolingSetpoint", width:2, height:1, inactiveLabel: false, decoration: "flat") {
			state "coolingSetpoint", action:"lowerCoolSetpoint", icon:"st.thermostat.thermostat-left"
		}
		valueTile("coolingSetpoint", "device.coolingSetpoint", width:2, height:1, inactiveLabel: false, decoration: "flat") {
			state "coolingSetpoint", label:'${currentValue}° cool', backgroundColor:"#ffffff"
		}
		standardTile("raiseCoolSetpoint", "device.heatingSetpoint", width:2, height:1, inactiveLabel: false, decoration: "flat") {
			state "heatingSetpoint", action:"raiseCoolSetpoint", icon:"st.thermostat.thermostat-right"
		}
		standardTile("thermostatOperatingState", "device.thermostatOperatingState", width: 2, height:1, decoration: "flat") {
			state "thermostatOperatingState", label:'${currentValue}', backgroundColor:"#ffffff"
		}
		standardTile("refresh", "device.thermostatMode", width:2, height:1, inactiveLabel: false, decoration: "flat") {
			state "default", action:"refresh.refresh", icon:"st.secondary.refresh"
		}
		valueTile("powerSource", "device.powerSource", width: 2, heigh: 1, inactiveLabel: true, decoration: "flat") {
			state "powerSource", label: 'Power Source: ${currentValue}', backgroundColor: "#ffffff"
		}
		valueTile("battery", "device.battery", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
			state "battery", label:'${currentValue}% battery', unit:""
		}
		main "temperature"
		details(["temperature", "lowerHeatingSetpoint", "heatingSetpoint", "raiseHeatingSetpoint", "lowerCoolSetpoint",
				 "coolingSetpoint", "raiseCoolSetpoint", "thermostatMode", "thermostatFanMode", "thermostatOperatingState",
				 "refresh", "battery", "powerSource"])
	}
}

def parse(String description) {
	def map = zigbee.getEvent(description)
	def result
	if(!map) {
		result = parseAttrMessage(description)
	} else {
		log.warn "Unexpected event: ${map}"
	}
	log.debug "Description ${description} parsed to ${result}"
	return result
}

private parseAttrMessage(description) {
	def descMap = zigbee.parseDescriptionAsMap(description)
	log.debug "Desc Map: $descMap"
	def result = []
	List attrData = [[cluster: descMap.clusterInt, attribute: descMap.attrInt, value: descMap.value]]
	descMap.additionalAttrs.each {
		attrData << [cluster: descMap.clusterInt, attribute: it.attrInt, value: it.value]
	}
	attrData.each {
		def map = [:]
		if (it.cluster == THERMOSTAT_CLUSTER && it.attribute == LOCAL_TEMPERATURE) {
			log.debug "TEMP"
			map.name = "temperature"
			map.value = getTemperature(it.value)
			map.unit = temperatureScale
		} else if (it.cluster == THERMOSTAT_CLUSTER && it.attribute == COOLING_SETPOINT) {
			log.debug "COOLING SETPOINT"
			map.name = "coolingSetpoint"
			map.value = getTemperature(it.value)
			map.unit = temperatureScale
		} else if (it.cluster == THERMOSTAT_CLUSTER && it.attribute == HEATING_SETPOINT) {
			log.debug "HEATING SETPOINT"
			map.name = "heatingSetpoint"
			map.value = getTemperature(it.value)
			map.unit = temperatureScale
		} else if (it.cluster == THERMOSTAT_CLUSTER && (it.attribute == THERMOSTAT_MODE || it.attribute == THERMOSTAT_RUNNING_MODE)) {
			log.debug "MODE"
			map.name = "thermostatMode"
			map.value = THERMOSTAT_MODE_MAP[it.value]
		} else if (it.cluster == THERMOSTAT_CLUSTER && it.attribute == THERMOSTAT_RUNNING_STATE) {
			log.debug "RUNNING STATE"
			def binValue = extendString(bin(hexToInt(it.value)), 16, '0').reverse()
			map.name = "thermostatOperatingState"
			if(binValue[0] == "1") {
				map.value = "heating"
			} else if(binValue[1] == "1") {
				map.value = "cooling"
			} else {
				map.value = binValue[2] == "1" ? "fan only" : "idle"
			}
		} else if (it.cluster == FAN_CONTROL_CLUSTER && it.attribute == FAN_MODE) {
			log.debug "FAN MODE"
			map.name = "thermostatFanMode"
			map.value = FAN_MODE_MAP[it.value]
		} else if(it.cluster == zigbee.POWER_CONFIGURATION_CLUSTER && it.attribute == BATTERY_VOLTAGE) {
			map = getBatteryPercentage(Integer.parseInt(it.value, 16))
		} else if(it.cluster == zigbee.POWER_CONFIGURATION_CLUSTER && it.attribute == BATTERY_ALARM_STATE) {
			map = getPowerSource(it.value)
		}
		if(map) {
			result << createEvent(map)
		}
	}
	return result
}

def installed() {
	sendEvent(name: "checkInterval", value: 2 * 60 * 60 + 2 * 60, displayed: false, data: [protocol: "zigbee", hubHardwareId: device.hub.hardwareID, offlinePingable: "1"])
	refresh()
}

def refresh() {
	return zigbee.readAttribute(THERMOSTAT_CLUSTER, LOCAL_TEMPERATURE) +
			zigbee.readAttribute(THERMOSTAT_CLUSTER, COOLING_SETPOINT) +
			zigbee.readAttribute(THERMOSTAT_CLUSTER, HEATING_SETPOINT) +
			zigbee.readAttribute(THERMOSTAT_CLUSTER, THERMOSTAT_MODE) +
			zigbee.readAttribute(THERMOSTAT_CLUSTER, THERMOSTAT_RUNNING_STATE) +
			zigbee.readAttribute(FAN_CONTROL_CLUSTER, FAN_MODE) +
			zigbee.readAttribute(zigbee.POWER_CONFIGURATION_CLUSTER, BATTERY_VOLTAGE) +
			zigbee.readAttribute(zigbee.POWER_CONFIGURATION_CLUSTER, BATTERY_ALARM_STATE)
}

def ping() {
	refresh()
}

def configure() {
	def binding = zigbee.addBinding(THERMOSTAT_CLUSTER) + zigbee.addBinding(FAN_CONTROL_CLUSTER)
	def startValues = zigbee.writeAttribute(THERMOSTAT_CLUSTER, HEATING_SETPOINT, DataType.INT16, 0x07D0) +
			zigbee.writeAttribute(THERMOSTAT_CLUSTER, COOLING_SETPOINT, DataType.INT16, 0x0A28)

	return binding + startValues + zigbee.batteryConfig() + refresh()
}

def getBatteryPercentage(rawValue) {
	def result = [:]
	result.name = "battery"
	if(rawValue == 0) {
		sendEvent(name: "powerSource", value: "mains", descriptionText: "${device.displayName} is connected to mains")
		result.value = 100
		result.descriptionText = "${device.displayName} is powered by external source."
	} else {
		def volts = rawValue / 10
		def minVolts = 5
		def maxVolts = 6.5
		def pct = (volts - minVolts) / (maxVolts - minVolts)
		def roundedPct = Math.round(pct * 100)
		if (roundedPct < 0) {
			roundedPct = 0
		}
		result.value = Math.min(100, roundedPct)
		result.descriptionText = "${device.displayName} battery has ${result.value}%"
	}
	return result
}

def getTemperature(value) {
	if (value != null) {
		def celsius = Integer.parseInt(value, 16) / 100
		if (temperatureScale == "C") {
			return celsius
		} else {
			return Math.round(celsiusToFahrenheit(celsius))
		}
	}
}

def getPowerSource(value) {
	def result = [name: "powerSource"]
	switch(value) {
		case "40000000":
			result.value = "battery"
			result.descriptionText = "${device.displayName} is powered by batteries"
			break
		default:
			result.value = "mains"
			result.descriptionText = "${device.displayName} is connected to mains"
			break
	}
	return result
}

def setThermostatMode(value) {
	switch(value) {
		case "heat":
			heat()
			break
		case "cool":
			cool()
			break
		default:
			off()
	}
}

def off() {
	return zigbee.writeAttribute(THERMOSTAT_CLUSTER, THERMOSTAT_MODE, DataType.ENUM8, THERMOSTAT_MODE_OFF) +
			zigbee.readAttribute(THERMOSTAT_CLUSTER, THERMOSTAT_MODE)
}

def cool() {
	return zigbee.writeAttribute(THERMOSTAT_CLUSTER, THERMOSTAT_MODE, DataType.ENUM8, THERMOSTAT_MODE_COOL) +
			zigbee.readAttribute(THERMOSTAT_CLUSTER, THERMOSTAT_MODE)
}

def heat() {
	return zigbee.writeAttribute(THERMOSTAT_CLUSTER, THERMOSTAT_MODE, DataType.ENUM8, THERMOSTAT_MODE_HEAT) +
			zigbee.readAttribute(THERMOSTAT_CLUSTER, THERMOSTAT_MODE)
}

def fanAuto() {
	return zigbee.writeAttribute(FAN_CONTROL_CLUSTER, FAN_MODE, DataType.ENUM8, FAN_MODE_AUTO) +
			zigbee.readAttribute(FAN_CONTROL_CLUSTER, FAN_MODE)
}

def fanOn() {
	return zigbee.writeAttribute(FAN_CONTROL_CLUSTER, FAN_MODE, DataType.ENUM8, FAN_MODE_ON) +
			zigbee.readAttribute(FAN_CONTROL_CLUSTER, FAN_MODE)
}

def setCoolingSetpoint(degrees) {
	if (degrees != null) {
		def degreesInteger = Math.round(degrees)
		def celsius = (temperatureScale == "C") ? degreesInteger : (fahrenheitToCelsius(degreesInteger) as Double).round(2)
		return zigbee.writeAttribute(THERMOSTAT_CLUSTER, COOLING_SETPOINT, DataType.INT16, hex(celsius * 100)) +
				zigbee.readAttribute(THERMOSTAT_CLUSTER, COOLING_SETPOINT)
	}
}

def setHeatingSetpoint(degrees) {
	if (degrees != null) {
		def degreesInteger = Math.round(degrees)

		def celsius = (temperatureScale == "C") ? degreesInteger : (fahrenheitToCelsius(degreesInteger) as Double).round(2)
		return zigbee.writeAttribute(THERMOSTAT_CLUSTER, HEATING_SETPOINT, DataType.INT16, hex(celsius * 100)) +
				zigbee.readAttribute(THERMOSTAT_CLUSTER, HEATING_SETPOINT)
	}
}

def raiseHeatingSetpoint() {
	alterSetpoint(true, "heatingSetpoint")
}

def lowerHeatingSetpoint() {
	alterSetpoint(false, "heatingSetpoint")
}

def raiseCoolSetpoint() {
	alterSetpoint(true, "coolingSetpoint")
}

def lowerCoolSetpoint() {
	alterSetpoint(false, "coolingSetpoint")
}

/*
* This method uses Setpoint Raise/Lower Command
* MSB is responsible for choosing heating/cooling setpoint,
* 0x00 for heat, 0x01 for cool.
* LSB is signed 8-bit integer, which specifies with how many steps setpoint will be changed.
* One step: 0.1 C
 */
def alterSetpoint(raise, setpoint) {
	def MSB = (setpoint == "heatingSetpoint") ? "00" : "01"
	def LSB = raise ? "05" : "FB" // +0.5 C : -0.5 C
	def payload = MSB + LSB
	zigbee.command(THERMOSTAT_CLUSTER, SETPOINT_RAISE_LOWER_CMD, payload)
}

private hex(value) {
	return new BigInteger(Math.round(value).toString()).toString(16)
}

private bin(value) {
	return new BigInteger(Math.round(value).toString()).toString(2)
}

private hexToInt(value) {
	new BigInteger(value, 16)
}

private extendString(str, size, character) {
	return character * (size - str.length()) + str
}

private getTHERMOSTAT_CLUSTER() { 0x0201 }
private getLOCAL_TEMPERATURE() { 0x0000 }
private getCOOLING_SETPOINT() { 0x0011 }
private getHEATING_SETPOINT() { 0x0012 }
private getTHERMOSTAT_RUNNING_MODE() { 0x001E }
private getTHERMOSTAT_MODE() { 0x001C }
private getTHERMOSTAT_MODE_OFF() { 0x00 }
private getTHERMOSTAT_MODE_COOL() { 0x03 }
private getTHERMOSTAT_MODE_HEAT() { 0x04 }
private getTHERMOSTAT_MODE_MAP() { [
		"00":"off",
		"03":"cool",
		"04":"heat",
]}
private getTHERMOSTAT_RUNNING_STATE() { 0x0029 }
private getSETPOINT_RAISE_LOWER_CMD() { 0x00 }

private getFAN_CONTROL_CLUSTER() { 0x0202 }
private getFAN_MODE() { 0x0000 }
private getFAN_MODE_ON() { 0x04 }
private getFAN_MODE_AUTO() { 0x05 }
private getFAN_MODE_MAP() { [
		"04":"on",
		"05":"auto"
]}

private getBATTERY_VOLTAGE() { 0x0020 }
private getBATTERY_ALARM_STATE() { 0x003E }