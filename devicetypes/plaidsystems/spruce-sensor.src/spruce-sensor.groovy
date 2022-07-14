/**
 *  Spruce Sensor -updated for new Samsung App
 *
 *  Copyright 2021 Plaid Systems
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

 -------6/2021 Updates--------
 - Update for 2021 Samsung SmartThings App
	
 -------8/2021 Updates--------
 - remove zigbeeNodeType from fingerprints

 */

import groovy.json.JsonOutput
import physicalgraph.zigbee.zcl.DataType

//dth version
def getVERSION() {"v1.0 6-2021"}
def getDEBUG() {true}
def getHC_INTERVAL_SECS() {3720}
def getMEASURED_VALUE_ATTRIBUTE() {0x0000}
def getCONFIGURE_REPORTING_RESPONSE_COMMAND() {0x07}

metadata {
	definition (name: "Spruce Sensor", namespace: "plaidsystems", author: "Plaid Systems", mnmn: "SmartThingsCommunity",
		mcdSync: true, vid: "4cff4731-67ce-310b-ada0-4d8e169a6df0") {

		capability "Sensor"
		capability "Temperature Measurement"
		capability "Relative Humidity Measurement"
		capability "Battery"
		capability "Health Check"
		capability "Configuration"
		capability "Refresh"

		attribute "reportingInterval", "NUMBER"

		//new release
		fingerprint manufacturer: "PLAID SYSTEMS", model: "PS-SPRZMS-01", deviceJoinName: "Spruce Irrigation" //Spruce Sensor
		fingerprint manufacturer: "PLAID SYSTEMS", model: "PS-SPRZMS-SLP1", deviceJoinName: "Spruce Irrigation" //Spruce Sensor
		fingerprint manufacturer: "PLAID SYSTEMS", model: "PS-SPRZMS-SLP3", deviceJoinName: "Spruce Irrigation" //Spruce Sensor
	}

	preferences {
		input description: "This feature allows you to correct any temperature variations by selecting an offset. Ex: If your sensor consistently reports a temp that's 5 degrees too warm, you'd enter \"-5\". If 3 degrees too cold, enter \"+3\".", displayDuringSetup: false, type: "paragraph", element: "paragraph", title: ""
		input "tempOffset", "number", title: "Temperature Offset", description: "Adjust temperature by this many degrees", range: "*..*", displayDuringSetup: false

		input description: "Gen 1 & 2 Sensors only: Measurement Interval 1-120 minutes (default: 10 minutes)", displayDuringSetup: false, type: "paragraph", element: "paragraph", title: ""
		input "interval", "number", title: "Measurement Interval", description: "Set how often you would like to check soil moisture in minutes", range: "1..120", defaultValue: 10, displayDuringSetup: false

		input title: "Version", description: VERSION, displayDuringSetup: true, type: "paragraph", element: "paragraph"
	}

}

// Parse incoming device messages to generate events
def parse(description) {

	def map
	if (description?.startsWith("read attr -")) {
		log.debug "read attr - ${description}"
		map = parseReportAttributeMessage(description)
	}
	else if (isSupportedDescription(description)) {
		log.debug "supported description: $description"
		map = parseSupportedMessage(description)
	}
	else if (description?.startsWith("catchall:")) {
		log.debug "catchall ${description}"
		map = parseCatchAllMessage(description)
	}
	else if (DEBUG) log.debug "uncaught ${description}"

	def result = map ? createEvent(map) : null

	//check for configuration change and send configuration change
	if (map && map.name == "temperature" && isIntervalChange()) result = ping()

	if (DEBUG) log.debug "parse result: $result"
	return result
}

private Map parseCatchAllMessage(String description) {
	Map resultMap = [:]
	def map = zigbee.parseDescriptionAsMap(description)

	def command = zigbee.convertHexToInt(map.command)
	def cluster = ( map.clusterId == null ? zigbee.convertHexToInt(map.cluster) : zigbee.convertHexToInt(map.clusterId) )
	def value = (map.value != null ? zigbee.convertHexToInt(map.value) : null)

	if (DEBUG) log.debug "command: ${command} cluster: ${cluster} value: ${value}"

	//check humidity configuration update is complete
	if (command == CONFIGURE_REPORTING_RESPONSE_COMMAND && cluster == zigbee.RELATIVE_HUMIDITY_CLUSTER){
		sendEvent(name: "reportingInterval", value: getReportInterval(), descriptionText: "Configuration Successful")
		sendEvent(name: "checkInterval", value: deviceWatchSeconds(), displayed: false, data: [protocol: "zigbee", hubHardwareId: device.hub.hardwareID])
		log.debug "config complete ${getReportInterval()}"
	}

	if (DEBUG) log.debug "no catchall found"
	return null

}

private Map parseReportAttributeMessage(String description) {
	def map = zigbee.parseDescriptionAsMap(description)

	def cluster = ( map.cluster != null ? zigbee.convertHexToInt(map.cluster) : null )
	def attribute = ( map.attrId != null ? zigbee.convertHexToInt(map.attrId) : null )
	def value = ( map.value != null ? zigbee.convertHexToInt(map.value) : null )

	if (cluster == zigbee.POWER_CONFIGURATION_CLUSTER && attribute == MEASURED_VALUE_ATTRIBUTE) {
		return getBatteryResult(value)
	}

	if (DEBUG) log.debug "no read attr found"
	return null
}

private Map parseSupportedMessage(String description) {

	//temperature
	if (description?.startsWith("temperature: ")) {
		def value = zigbee.parseHATemperatureValue(description, "temperature: ", getTemperatureScale())
		return getTemperatureResult(value)
	}

	//humidity
	if (description?.startsWith("humidity: ")) {
		def pct = (description - "humidity: " - "%").trim()
		if (pct.isNumber()) {
			def value = Math.round(new BigDecimal(pct)).toString()
			return getHumidityResult(value)
		}
	}
}


//----------------------event values-------------------------------//

private Map getHumidityResult(value) {
	log.debug "Humidity: $value"
	def linkText = getLinkText(device)

	return [
		name: "humidity",
		value: value,
		unit: "%",
		descriptionText: "${linkText} soil moisture is ${value}%"
	]
}

private Map getTemperatureResult(value) {
	log.debug "Temperature: $value"
	def linkText = getLinkText(device)

	if (tempOffset) {
		def offset = tempOffset as int
		def v = value as int
		value = v + offset
	}
	def descriptionText = "${linkText} is ${value}°${temperatureScale}"

	return [
		name: "temperature",
		value: value,
		descriptionText: descriptionText,
		unit: temperatureScale
	]
}

private Map getBatteryResult(value) {
	log.debug "Battery: $value"
	def linkText = getLinkText(device)

	def min = 2500
	def percent = (value - min) / 5
	percent = Math.max(0, Math.min(percent, 100.0))
	value = Math.round(percent)

	def descriptionText = "${linkText} battery is ${value}%"
	if (percent < 10) descriptionText = "${linkText} battery is getting low $percent %."

	return [
		name: "battery",
		value: value,
		descriptionText: descriptionText
	]
}


//----------------------configuration-------------------------------//

def installed() {
	//check every 62 minutes
	sendEvent(name: "checkInterval", value: deviceWatchSeconds(), displayed: false, data: [protocol: "zigbee", hubHardwareId: device.hub.hardwareID])
}

//when device preferences are changed
def updated() {
	if (DEBUG) log.debug "device updated"

	//set reportingInterval = 0 to trigger update
	if (isIntervalChange()) sendEvent(name: "reportingInterval", value: 0, descriptionText: "Settings changed and will update at next report. Measure interval set to ${getReportInterval()} mins")
}

//has interval been updated
def isIntervalChange() {
	if (DEBUG) log.debug "isIntervalChange ${getReportInterval()} ${device.latestValue("reportingInterval")}"
	return (getReportInterval() != device.latestValue("reportingInterval"))
}

//settings default interval
def getReportInterval() {
	return (interval != null ? interval : 10)
}

//Device-Watch every 62mins or settings interval + 120s
def deviceWatchSeconds() {
	def intervalSeconds = getReportInterval() * 60 + 2 * 60
	if (intervalSeconds < HC_INTERVAL_SECS) intervalSeconds = HC_INTERVAL_SECS
	return intervalSeconds
}

//ping
def ping() {
	if (DEBUG) log.debug "device health ping"

	List cmds = []
	if (isIntervalChange()) cmds = reporting()
	else cmds = refresh()

	return cmds?.collect { new physicalgraph.device.HubAction(it) }
}

//configure
def configure() {
	return reporting() + refresh()
}

//set reporting
def reporting() {
	//set min/max report from interval setting
	def minReport = getReportInterval()
	def maxReport = getReportInterval() * 61

	def reportingCmds = []
	reportingCmds += zigbee.configureReporting(zigbee.TEMPERATURE_MEASUREMENT_CLUSTER, MEASURED_VALUE_ATTRIBUTE, DataType.INT16, 1, 0, 0x01, [destEndpoint: 1])
	reportingCmds += zigbee.configureReporting(zigbee.RELATIVE_HUMIDITY_CLUSTER, MEASURED_VALUE_ATTRIBUTE, DataType.UINT16, minReport, maxReport, 0x6400, [destEndpoint: 1])
	reportingCmds += zigbee.configureReporting(zigbee.POWER_CONFIGURATION_CLUSTER, MEASURED_VALUE_ATTRIBUTE, DataType.UINT16, 0x0C, 0, 0x0500, [destEndpoint: 1])

	return reportingCmds
}

def refresh() {
	log.debug "refresh"
	def refreshCmds = []
	refreshCmds += zigbee.readAttribute(zigbee.TEMPERATURE_MEASUREMENT_CLUSTER, MEASURED_VALUE_ATTRIBUTE, [destEndpoint: 1])
	refreshCmds += zigbee.readAttribute(zigbee.RELATIVE_HUMIDITY_CLUSTER, MEASURED_VALUE_ATTRIBUTE, [destEndpoint: 1])
	refreshCmds += zigbee.readAttribute(zigbee.POWER_CONFIGURATION_CLUSTER, MEASURED_VALUE_ATTRIBUTE, [destEndpoint: 1])

	return refreshCmds
}