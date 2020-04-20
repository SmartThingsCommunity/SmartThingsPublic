/**
 *  Qubino Weatherstation
 *	Device Handler 
 *	Version 1.0
 *  Date: 16.12.2016
 *	Author: Kristjan Jam&scaron;ek (Kjamsek), Goap d.o.o.
 *  Copyright 2016 Kristjan Jam&scaron;ek
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
 * - DEVICE HANDLER FOR QUBINO WEATHERSTATION Z-WAVE DEVICE -  
 *	The handler supports all unsecure functions of the Qubino Weatherstation device. Configuration parameters and
 *	association groups can be set in the device's preferences screen, but they are applied on the device only after
 *	pressing the 'Set configuration' and 'Set associations' buttons on the bottom of the details view. 
 *
 *	This device handler supports data values that are currently not implemented as capabilities, so custom attribute 
 *	states are used. Please use a SmartApp that supports custom attribute monitoring with this device in your rules.
 *	By default the 'Temperature Measurement' and 'Relative Humidity Measurement' capability states are mirrored from
 *	the custom attribute state values 'temperatureCh1' and 'humidityCh1'.
 *
 *	This device handler uses hubAction in configure() method in order to set a MultiChannel Lifeline association to the
 *	device, so reports from all endpoints can be received. 
 *
 *
 *	CHANGELOG:
 *	0.99: Final release code cleanup and commenting
 *	1.00: Added comments to code for readability
 */
metadata {
	definition (name: "Qubino Weatherstation", namespace: "Goap", author: "Kristjan Jam&scaron;ek") {
		capability "Configuration" //Needed for configure() function to set MultiChannel Lifeline Association Set
		capability "Temperature Measurement" //Used on main tile, needed in order to have the device appear in capabilities lists, mirrors temperatureCh1 attribute states
		capability "Relative Humidity Measurement" //Needed in order to have the device appear in capabilities lists, mirrors humidityCh1 attribute states
		capability "Sensor" // - Tagging capability
		
		attribute "temperatureCh1", "number" // temperature attribute for Ch1 Temperature reported by device's endpoint 1
		attribute "windDirection", "number" // wind direction attribute for Wind Gauge - Direction reported by device's endpoint 2
		attribute "windVelocity", "number" // wind velocity attribute for Wind Gauge - Velocity reported by device's endpoint 3
		attribute "windGust", "number" // wind gust velocity attribute for Wind Gauge - Wind gust reported by device's endpoint 4
		attribute "windTemperature", "number" // wind temperature attribute for Wind Gauge - Temperature reported by device's endpoint 5
		attribute "windChillTemperature", "number" // wind chill temperature attribute for Wind Gauge - Wind Chill reported by device's endpoint 6
		attribute "rainRate", "number" // rain rate attribute for Rain Sensor data reported by device's endpoint 7
		attribute "humidityCh1", "number" // humidity attribute for Ch1 Humidity reported by device's endpoint 8
		attribute "temperatureCh2", "number" // temperature attribute for Ch2 Temperature reported by device's endpoint 9
		attribute "humidityCh2", "number" // humidity attribute for Ch2 Humidity reported by device's endpoint 10
		
		attribute "setConfigParams", "string" // attribute for tile element for setConfigurationParams command
		attribute "setAssocGroups", "string" // attribute for tile element for setAssociations command
		
		command "setConfigurationParams" // command to issue Configuration Set command sequence according to user's preferences
		command "setAssociations" // command to issue Association Set command sequence according to user's preferences

        fingerprint mfr:"0159", prod:"0007", model:"0053"  //Manufacturer Information values for Qubino Weatherstation
	}


	simulator {
		// TESTED WITH PHYSICAL DEVICE - UNNEEDED
	}

	tiles(scale: 2) {
		valueTile("temperature", "device.temperature") {
			state("temperature", label:'${currentValue} °', unit:"°", icon: "st.Weather.weather2", backgroundColors: [
				// Celsius Color Range
				[value: 0, color: "#153591"],
				[value: 7, color: "#1e9cbb"],
				[value: 15, color: "#90d2a7"],
				[value: 23, color: "#44b621"],
				[value: 29, color: "#f1d801"],
				[value: 33, color: "#d04e00"],
				[value: 36, color: "#bc2323"],
				// Fahrenheit Color Range
				[value: 40, color: "#153591"],
				[value: 44, color: "#1e9cbb"],
				[value: 59, color: "#90d2a7"],
				[value: 74, color: "#44b621"],
				[value: 84, color: "#f1d801"],
				[value: 92, color: "#d04e00"],
				[value: 96, color: "#bc2323"]
			])
		}		
		valueTile("humidity", "device.humidity") {
			state("humidity", label:'${currentValue} %', unit:"%", display:false)
		}		
		standardTile("temperatureCh1", "device.temperatureCh1", width: 3, height: 3) {
			state("temperatureCh1", label:'Temperature Ch1: ${currentValue} °', unit:'°', icon: 'st.Weather.weather2')
		}
		standardTile("windDirection", "device.windDirection", width: 3, height: 3) {
			state("windDirection", label:'Wind Direction: ${currentValue} °', unit: "°", icon: 'st.Outdoor.outdoor20')
		}
		standardTile("windVelocity", "device.windVelocity", width: 3, height: 3) {
			state("windVelocity", label:'Wind Velocity: ${currentValue} m/s', unit:"m/s", icon: 'st.Weather.weather1')
		}
		standardTile("windGust", "device.windGust", width: 3, height: 3) {
			state("windGust", label:'Wind Gust: ${currentValue} m/s', unit:"m/s", icon: 'st.Weather.weather1')
		}
		standardTile("windTemperature", "device.windTemperature", width: 3, height: 3) {
			state("windTemperature", label:'Wind Temperature: ${currentValue} °', unit:'°', icon: 'st.Weather.weather2')
		}
		standardTile("windChillTemperature", "device.windChillTemperature", width: 3, height: 3) {
			state("windChillTemperature", label:'Wind Chill: ${currentValue} °', unit:'°', icon: 'st.Weather.weather2')
		}
		standardTile("rainRate", "device.rainRate", width: 3, height: 3) {
			state("rainRate", label:'Rain Sensor: ${currentValue} mm/h', unit:"mm/h", icon: 'st.Weather.weather10')
		}
		standardTile("humidityCh1", "device.humidityCh1", width: 3, height: 3) {
			state("humidityCh1", label:'Humidity Ch1: ${currentValue} %', unit:"%", icon: 'st.Weather.weather12')
		}
		standardTile("temperatureCh2", "device.temperatureCh2", width: 3, height: 3) {
			state("temperatureCh2", label:'Temperature Ch2: ${currentValue} °', unit:'°', icon: 'st.Weather.weather2')
		}
		standardTile("humidityCh2", "device.humidityCh2", width: 3, height: 3) {
			state("humidityCh2", label:'Humidity Ch2: ${currentValue} %', unit:"%", icon: 'st.Weather.weather12')
		}
		standardTile("setConfigParams", "device.setConfigParams", decoration: "flat", width: 3, height: 3) {
			state("setConfigParams", label:'Set configuration', action:'setConfigurationParams', icon: "st.secondary.tools")
		}
		standardTile("setAssocGroups", "device.setAssocGroups", decoration: "flat", width: 3, height: 3) {
			state("setAssocGroups", label:'Set associations', action:'setAssociations', icon: "st.secondary.tools")
		}
		
		main("temperature")
		details(["temperatureCh1", "humidityCh1", "windDirection", "windVelocity", "windGust", "windTemperature", "windChillTemperature", "rainRate", "temperatureCh2", "humidityCh2", "setConfigParams", "setAssocGroups"])

	}
	preferences {
/**
*			--------	CONFIGURATION PARAMETER SECTION	--------
*/
				input name: "param1", type: "number", range: "0..8800", required: false,
					title: "1. Wind Gauge, Wind Gust Top Value. " +
						   "If the Wind Gust is higher than the parameter value, the device triggers an association.\n" +
						   "Available settings:\n" +
						   "0 ... 8800 = value from 0,00 m/s to 88,00 m/s,\n" +
						   "Default value: 1000 (10,00 m/s)."
				input name: "param2", type: "number", range: "0..30000", required: false,
					title: "2. Rain Gauge, Rain Rate Top Value. " +
						   "If the Rain Sensor Rain Rate is higher than the parameter value, the device triggers an association.\n" +
						   "Available settings:\n" +
						   "0 ... 30000 = value from 0,00 mm/h to 300,00 mm/h,\n" +
						   "Default value: 200 (2,00 mm/h)."          
				input name: "param3", type: "enum", required: false,
					options: ["0" : "0",
							  "1" : "1"],
					title: "3. Wind Gauge, Wind Gust. " +
						   "Available settings:\n" +
						   "0 = If the Wind Gust is higher than the parameter number 1 value, then the device sends a Basic Set 0x00,\n" +
						   "1 = If the Wind Gust is higher than the parameter number 1 value, then the device sends a Basic Set 0xFF,\n" +
						   "Default value: 1."
				input name: "param4", type: "enum", required: false,
					options: ["0" : "0",
							  "1" : "1"],
					title: "4. Rain Gauge, Rain Rate. " +
						   "Available settings:\n" +
						   "0 = If the Rain Rate is higher than the parameter number 2 value, then the device sends a Basic Set 0x00,\n" +
						   "1 = If the Rain Rate is higher than the parameter number 2 value, then the device sends a Basic Set 0xFF,\n" +
						   "Default value: 1."	
				input name: "param5", type: "enum", required: false,
					options: ["0" : "0",
							  "1" : "1"],
					title: "5. Endpoint 1 - Unsolicited Report enable/disable. " +
						   "Available settings:\n" +
						   "0 = Unsolicited Report disabled,\n" +
						   "1 = Unsolicited Report enabled,\n" +
						   "Default value: 1."
				input name: "param6", type: "enum", required: false,
					options: ["0" : "0",
							  "1" : "1"],
					title: "6. Endpoint 2 - Unsolicited Report enable/disable. " +
						   "Available settings:\n" +
						   "0 = Unsolicited Report disabled,\n" +
						   "1 = Unsolicited Report enabled,\n" +
						   "Default value: 1."
				input name: "param7", type: "enum", required: false,
					options: ["0" : "0",
							  "1" : "1"],
					title: "7. Endpoint 3 - Unsolicited Report enable/disable. " +
						   "Available settings:\n" +
						   "0 = Unsolicited Report disabled,\n" +
						   "1 = Unsolicited Report enabled,\n" +
						   "Default value: 1."
				input name: "param8", type: "enum", required: false,
					options: ["0" : "0",
							  "1" : "1"],
					title: "8. Endpoint 4 - Unsolicited Report enable/disable. " +
						   "Available settings:\n" +
						   "0 = Unsolicited Report disabled,\n" +
						   "1 = Unsolicited Report enabled,\n" +
						   "Default value: 1."
				input name: "param9", type: "enum", required: false,
					options: ["0" : "0",
							  "1" : "1"],
					title: "9. Endpoint 5 - Unsolicited Report enable/disable. " +
						   "Available settings:\n" +
						   "0 = Unsolicited Report disabled,\n" +
						   "1 = Unsolicited Report enabled,\n" +
						   "Default value: 1."
				input name: "param10", type: "enum", required: false,
					options: ["0" : "0",
							  "1" : "1"],
					title: "10. Endpoint 6 - Unsolicited Report enable/disable. " +
						   "Available settings:\n" +
						   "0 = Unsolicited Report disabled,\n" +
						   "1 = Unsolicited Report enabled,\n" +
						   "Default value: 1."
				input name: "param11", type: "enum", required: false,
					options: ["0" : "0",
							  "1" : "1"],
					title: "11. Endpoint 7 - Unsolicited Report enable/disable. " +
						   "Available settings:\n" +
						   "0 = Unsolicited Report disabled,\n" +
						   "1 = Unsolicited Report enabled,\n" +
						   "Default value: 1."
				input name: "param12", type: "enum", required: false,
					options: ["0" : "0",
							  "1" : "1"],
					title: "12. Endpoint 8 - Unsolicited Report enable/disable. " +
						   "Available settings:\n" +
						   "0 = Unsolicited Report disabled,\n" +
						   "1 = Unsolicited Report enabled,\n" +
						   "Default value: 1."
				input name: "param13", type: "enum", required: false,
					options: ["0" : "0",
							  "1" : "1"],
					title: "13. Endpoint 9 - Unsolicited Report enable/disable. " +
						   "Available settings:\n" +
						   "0 = Unsolicited Report disabled,\n" +
						   "1 = Unsolicited Report enabled,\n" +
						   "Default value: 1."
				input name: "param14", type: "enum", required: false,
					options: ["0" : "0",
							  "1" : "1"],
					title: "14. Endpoint 10 - Unsolicited Report enable/disable. " +
						   "Available settings:\n" +
						   "0 = Unsolicited Report disabled,\n" +
						   "1 = Unsolicited Report enabled,\n" +
						   "Default value: 1."
				input name: "param15", type: "enum", required: false,
					options: ["0" : "0",
							  "1" : "1"],
					title: "15. Random ID enable/disable " +
						   "Available settings:\n" +
						   "0 = Random ID disabled,\n" +
						   "1 = Random ID enabled,\n" +
						   "Default value: 0."
/**
*			--------	ASSOCIATION GROUP SECTION	--------
*/
				input name: "assocGroup2", type: "text", required: false,
					title: "Association group 2: \n" +
						   "Basic On/Off command will be sent to associated nodes when the Wind Gust of the Wind Gauge exceeds the Configuration parameter 1 value. \n" +
						   "NOTE: Insert the node Id value of the devices you wish to associate this group with. Multiple nodeIds can also be set at once by separating individual values by a comma (2,3,...)."
						   
				input name: "assocGroup3", type: "text", required: false,
					title: "Association group 3: \n" +
						   "Basic On/Off command will be sent to associated nodes when the Rain Rate exceeds the Configuration parameter 2 value. \n" +
						   "NOTE: Insert the node Id value of the devices you wish to associate this group with. Multiple nodeIds can also be set at once by separating individual values by a comma (2,3,...)."
	}
}
/**
*	--------	HELPER METHODS SECTION	--------
*/
/**
 * Converts a list of String type node id values to Integer type.
 *
 * @param stringList - a list of String type node id values.
 * @return stringList - a list of Integer type node id values.
*/
def convertStringListToIntegerList(stringList){
	for(int i=0;i<stringList.size();i++){
		stringList[i] = stringList[i].toInteger()
	}
	return stringList
}
/**
 * Converts temperature values to fahrenheit or celsius scales accordign to user's setting.
 *
 * @param scaleParam user set scale parameter.
 * @param encapCmd received temperature parsed value.
 * @return String type value of the converted temperature value.
*/
def convertDegrees(scaleParam, encapCmd){
	switch (scaleParam) {
		default:
				break;
		case "F":
				if(encapCmd.scale == 1){
					return encapCmd.scaledSensorValue.toString()
				}else{
					return (encapCmd.scaledSensorValue * 9 / 5 + 32).toString()
				}
				break;
		case "C":
				if(encapCmd.scale == 0){
					return encapCmd.scaledSensorValue.toString()
				}else{
					return (encapCmd.scaledSensorValue * 9 / 5 + 32).toString()
				}
				break;
	}
}
/*
*	--------	HANDLE COMMANDS SECTION	--------
*/
/**
 * Configuration capability command handler that executes after module inclusion to remove existing singlechannel association Lifeline and replace it
 * with a multichannel Lifeline association setting for node id 1 and endpoint 1, which enables modules to report multichannel encapsulated frames.
 *
 * @param void
 * @return List of commands that will be executed in sequence with 500 ms delay inbetween.
*/
def configure() {
	log.debug "Qubino Weatherstation: configure()"
	/** In this method we first clear the association group 1 that is set by SmartThings automatically.
	* Afterwards we use physicalgraph.device.HubAction("8E0101000101") as a workaround for SmartThings' MultiChannel Association Command Class implementation,
	* where we cannot set a node id and endpoint id. The hardcoded parameter "8E0101000101" should be interpreted in sequence as hexadecimal byte values:
	* 8E010100 - Command class and command values, along with the field for singlechannel node id omitted
	* 0101 - These two byte values represent the multichannel node id 01 followed by the endpoint id 01 (in general this value can be left as is unless 
	*		 multiple subordinated controllers are included in your network. In that case please adjust these two value to the correct id.
	*/
	def assocCmds = []
	assocCmds << zwave.associationV1.associationRemove(groupingIdentifier:1, nodeId:zwaveHubNodeId).format()
	assocCmds << new physicalgraph.device.HubAction("8E0101000101")
	return delayBetween(assocCmds, 500)
}
/**
 * setAssociations command handler that sets user selected association groups. In case no node id is insetred the group is instead cleared.
 * Lifeline association hidden from user influence by design.
 *
 * @param void
 * @return List of Association commands that will be executed in sequence with 500 ms delay inbetween.
*/
def setAssociations() {
	log.debug "Qubino Weatherstation: setAssociations()"
	def assocSet = []
	if(settings.assocGroup2 != null){
		def group2parsed = settings.assocGroup2.tokenize(",")
		group2parsed = convertStringListToIntegerList(group2parsed)
		assocSet << zwave.associationV1.associationSet(groupingIdentifier:2, nodeId:group2parsed).format()
	}else{
		assocSet << zwave.associationV1.associationRemove(groupingIdentifier:2, nodeId: 0).format()
	}
	if(settings.assocGroup3 != null){
		def group3parsed = settings.assocGroup3.tokenize(",")
		group3parsed = convertStringListToIntegerList(group3parsed)
		assocSet << zwave.associationV1.associationSet(groupingIdentifier:3, nodeId:group3parsed).format()
	}else{
		assocSet << zwave.associationV1.associationRemove(groupingIdentifier:3, nodeId: 0).format()
	}
	return delayBetween(assocSet, 500)
	
}
/**
 * setConfigurationParams command handler that sets user selected configuration parameters on the device. 
 * In case no value is set for a specific parameter the method skips setting that parameter.
 * Secure mdoe setting hidden from user influence by design.
 *
 * @param void
 * @return List of Configuration Set commands that will be executed in sequence with 500 ms delay inbetween.
*/
def setConfigurationParams() {
	log.debug "Qubino Weatherstation: setConfigurationParams()"
	def configSequence = []
	if(settings.param1 != null){
		configSequence << zwave.configurationV1.configurationSet(parameterNumber: 1, size: 2, scaledConfigurationValue: settings.param1.toInteger()).format()
	}
	if(settings.param2 != null){
		configSequence << zwave.configurationV1.configurationSet(parameterNumber: 2, size: 2, scaledConfigurationValue: settings.param2.toInteger()).format()
	}
	if(settings.param3 != null){
		configSequence << zwave.configurationV1.configurationSet(parameterNumber: 3, size: 1, scaledConfigurationValue: settings.param3.toInteger()).format()
	}
	if(settings.param4 != null){
		configSequence << zwave.configurationV1.configurationSet(parameterNumber: 4, size: 1, scaledConfigurationValue: settings.param4.toInteger()).format()
	}
	if(settings.param5 != null){
		configSequence << zwave.configurationV1.configurationSet(parameterNumber: 5, size: 1, scaledConfigurationValue: settings.param5.toInteger()).format()
	}
	if(settings.param6 != null){
		configSequence << zwave.configurationV1.configurationSet(parameterNumber: 6, size: 1, scaledConfigurationValue: settings.param6.toInteger()).format()
	}
	if(settings.param7 != null){
		configSequence << zwave.configurationV1.configurationSet(parameterNumber: 7, size: 1, scaledConfigurationValue: settings.param7.toInteger()).format()
	}
	if(settings.param8 != null){
		configSequence << zwave.configurationV1.configurationSet(parameterNumber: 8, size: 1, scaledConfigurationValue: settings.param8.toInteger()).format()
	}
	if(settings.param9 != null){
		configSequence << zwave.configurationV1.configurationSet(parameterNumber: 9, size: 1, scaledConfigurationValue: settings.param9.toInteger()).format()
	}
	if(settings.param10 != null){
		configSequence << zwave.configurationV1.configurationSet(parameterNumber: 10, size: 1, scaledConfigurationValue: settings.param10.toInteger()).format()
	}
	if(settings.param11 != null){
		configSequence << zwave.configurationV1.configurationSet(parameterNumber: 11, size: 1, scaledConfigurationValue: settings.param11.toInteger()).format()
	}
	if(settings.param12 != null){
		configSequence << zwave.configurationV1.configurationSet(parameterNumber: 12, size: 1, scaledConfigurationValue: settings.param12.toInteger()).format()
	}
	if(settings.param13 != null){
		configSequence << zwave.configurationV1.configurationSet(parameterNumber: 13, size: 1, scaledConfigurationValue: settings.param13.toInteger()).format()
	}
	if(settings.param14 != null){
		configSequence << zwave.configurationV1.configurationSet(parameterNumber: 14, size: 1, scaledConfigurationValue: settings.param14.toInteger()).format()
	}
	if(settings.param15 != null){
		configSequence << zwave.configurationV1.configurationSet(parameterNumber: 15, size: 1, scaledConfigurationValue: settings.param15.toInteger()).format()
	}	
	return delayBetween(configSequence, 500)
}
/*
*	--------	EVENT PARSER SECTION	--------
*/
/**
 * parse function takes care of parsing received bytes and passing them on to event methods.
 *
 * @param description String type value of the received bytes.
 * @return Parsed result of the received bytes.
*/
def parse(String description) {
	log.debug "Qubino Weatherstation: Parsing '${description}'"
	def result = null
    def cmd = zwave.parse(description)
    if (cmd) {
		result = zwaveEvent(cmd)
        log.debug "Parsed ${cmd} to ${result.inspect()}"
    } else {
		log.debug "Non-parsed event: ${description}"
    }
    return result
}
/**
 * Event method for MultiChannelCmdEncap encapsulation frames.
 *
 * @param cmd Type physicalgraph.zwave.commands.multichannelv3.MultiChannelCmdEncap received command.
 * @return List of events that will update UI elements for data display.
*/
def zwaveEvent(physicalgraph.zwave.commands.multichannelv3.MultiChannelCmdEncap cmd) {
	def encapsulatedCommand = cmd.encapsulatedCommand([0x30: 1, 0x31: 1])
	def tempScale = location.temperatureScale
	def resultEvents = []
	switch (cmd.sourceEndPoint) {
		default:
				break;
		case 1:
				resultEvents << createEvent(name:"temperatureCh1", value: convertDegrees(tempScale,encapsulatedCommand), unit:"°"+location.temperatureScale, descriptionText: "Temperature Ch1: "+convertDegrees(tempScale,encapsulatedCommand)+"°"+location.temperatureScale)
				resultEvents << createEvent(name:"temperature", value: convertDegrees(tempScale,encapsulatedCommand), unit:"°"+location.temperatureScale, descriptionText: "Temperature Ch1: "+convertDegrees(tempScale,encapsulatedCommand)+"°"+location.temperatureScale, displayed: false)
				break;
		case 2:
				resultEvents << createEvent(name:"windDirection", value: encapsulatedCommand.scaledSensorValue.toString(), unit:"°", descriptionText: "Wind Direction: "+encapsulatedCommand.scaledSensorValue.toString()+" °")
				break;
		case 3:
				resultEvents << createEvent(name:"windVelocity", value: encapsulatedCommand.scaledSensorValue.toString(), unit:"m/s", descriptionText: "Wind Velocity: "+encapsulatedCommand.scaledSensorValue.toString()+" m/s")
				break;
		case 4:
				resultEvents << createEvent(name:"windGust", value: encapsulatedCommand.scaledSensorValue.toString(), unit:"m/s", descriptionText: "Wind Gust: "+encapsulatedCommand.scaledSensorValue.toString()+" m/s")
				break;
		case 5:
				resultEvents << createEvent(name:"windTemperature", value: convertDegrees(tempScale,encapsulatedCommand), unit:"°"+location.temperatureScale, descriptionText: "Wind Temperature: "+convertDegrees(tempScale,encapsulatedCommand)+" °"+location.temperatureScale)
				break;
		case 6:
				resultEvents << createEvent(name:"windChillTemperature", value: convertDegrees(tempScale,encapsulatedCommand), unit:"°"+location.temperatureScale, descriptionText: "Wind Chill: "+convertDegrees(tempScale,encapsulatedCommand)+" °"+location.temperatureScale)
				break;
		case 7:
				resultEvents << createEvent(name:"rainRate", value: encapsulatedCommand.scaledSensorValue.toString(), unit:"mm/h", descriptionText: "Rain Sensor: "+encapsulatedCommand.scaledSensorValue.toString()+" mm/h")
				break;
		case 8:
				resultEvents << createEvent(name:"humidity", value: encapsulatedCommand.scaledSensorValue.toString(), unit:"%", descriptionText: "Humidity Ch1: "+encapsulatedCommand.scaledSensorValue.toString()+" %", displayed: false)
				resultEvents << createEvent(name:"humidityCh1", value: encapsulatedCommand.scaledSensorValue.toString(), unit:"%", descriptionText: "Humidity Ch1: "+encapsulatedCommand.scaledSensorValue.toString()+" %")
				break;
		case 9:
				resultEvents << createEvent(name:"temperatureCh2", value: convertDegrees(tempScale,encapsulatedCommand), unit:"°"+location.temperatureScale, descriptionText: "Temperature Ch2: "+convertDegrees(tempScale,encapsulatedCommand)+" °"+location.temperatureScale)
				break;
		case 10:
				resultEvents << createEvent(name:"humidityCh2", value: encapsulatedCommand.scaledSensorValue.toString(), unit:"%", descriptionText: "Humidity Ch2: "+encapsulatedCommand.scaledSensorValue.toString()+" %")
				break;
	}
	return resultEvents
}
