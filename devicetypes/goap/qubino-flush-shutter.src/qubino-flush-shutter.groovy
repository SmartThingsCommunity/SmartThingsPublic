/**
 *  Qubino Flush Shutter
 *	Device Handler 
 *	Version 1.0
 *  Date: 10.8.2017
 *	Author: Kristjan Jam&scaron;ek (Kjamsek), Goap d.o.o.
 *  Copyright 2017 Kristjan Jam&scaron;ek
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
 *
 * |---------------------------- DEVICE HANDLER FOR QUBINO FLUSH SHUTTER Z-WAVE DEVICE -------------------------------------------------------|  
 *	The handler supports all unsecure functions of the Qubino Flush Shutter device. Configuration parameters and
 *	association groups can be set in the device's preferences screen, but they are applied on the device only after
 *	pressing the 'Set configuration' and 'Set associations' buttons on the bottom of the details view. 
 *	
 *	The Calibrate button issues a command to calibrate blinds. The Reset Power command resets accumulated power measurements. 
 *	The Refresh power button is used to request and display kWH measurements from the device.
 *
 *	This device handler supports data values that are currently not implemented as capabilities, so custom attribute 
 *	states are used. Please use a SmartApp that supports custom attribute monitoring with this device in your rules.
 * |-----------------------------------------------------------------------------------------------------------------------------------------------|
 *
 *
 *	TO-DO:
 *  - Implement secure mode
 *  - Implement Temperature Sensor functionality once inclusion in full configuration works
 *
 *	CHANGELOG:
 *	0.99: Final release code cleanup and commenting
 *	1.00: Added comments to code for readability
 *  1.10: Added Stop button to stop vertical axis motion
 *  1.11: Added switch capability for access the device via Google Home
 *  1.20: Layout change and Dedicated open/close and preset1,2,3 buttons
 */
metadata {
	definition (name: "Qubino Flush Shutter", namespace: "Goap", author: "Kristjan Jam&scaron;ek") {
		capability "Actuator"
		capability "Window Shade"
		capability "Switch"//Needed for show in google home
		capability "Switch Level"
		capability "Power Meter"
		
		capability "Configuration" //Needed for configure() function to set any specific configurations
		//capability "Temperature Measurement" //This capability is valid for devices with temperature sensors connected
		
		attribute "kwhConsumption", "number" //attribute used to store and display power consumption in KWH
		attribute "venetianLevel", "number" //attribute used to control and store venetian blinds level		
		attribute "venetianState", "string" //attribute for the binary control element of the venetian blinds control
		attribute "preset1", "number"
		attribute "preset2", "number"
		attribute "preset3", "number"

		command "setConfiguration" //command to issue Configuration Set commands to the module according to user preferences
		command "setAssociation" //command to issue Association Set commands to the modules according to user preferences
		command "refreshPowerConsumption" //command to issue Meter Get requests for KWH measurements from the device, W are already shown as part of Pwer Meter capability
		command "resetPower" //command to issue Meter Reset commands to reset accumulated pwoer measurements
		command "calibrate" //command to calibrate the shutter module
		command "stop" //command to stop the vertical blind movement
        command "preset1"
        command "preset2"
        command "preset3"
        
		//command "setSlatLevel" //command to issue slat tilting controls
		//command "openSlats" //command to set maximum level for slats
		//command "closeSlats" //command to set minimum level for slats
		
        fingerprint mfr:"0159", prod:"0003", model:"0052"  //Manufacturer Information value for Qubino Flush Shutter
	}


	simulator {
		// TESTED WITH PHYSICAL DEVICE - UNNEEDED
	}

	tiles(scale: 2) {
		multiAttributeTile(name:"shade", type: "generic", width: 6, height: 4, canChangeIcon: true){
			tileAttribute ("device.windowShade", key: "PRIMARY_CONTROL") {
				attributeState "open", label:'${name}', action:"close", icon:"st.Home.home9", backgroundColor:"#79b821", nextState:"closing"
				attributeState "partially open", label:'${name}', action:"open", icon:"st.Home.home9", backgroundColor:"#b77600", nextState:"opening"
				attributeState "closed", label:'${name}', action:"open", icon:"st.Home.home9", backgroundColor:"#ffffff", nextState:"opening"
				attributeState "opening", label:'${name}', action:"close", icon:"st.Home.home9", backgroundColor:"#79b821", nextState:"open"
				attributeState "closing", label:'${name}', action:"open", icon:"st.Home.home9", backgroundColor:"#ffffff", nextState:"closed"
			}
			tileAttribute ("device.level", key: "SLIDER_CONTROL") {
				attributeState "level", action:"switch level.setLevel"
			}
            tileAttribute ("device.power", key: "SECONDARY_CONTROL") {
				attributeState "power", label:'Power level: ${currentValue} W', icon: "st.Appliances.appliances17"
			}
	    }
        /*
		standardTile("venetianLabel", "device.venetianLabel", decoration: "flat", width: 6, height: 2) {
			state("venetianLabel", label:'SLAT TILT CONTROLS:')
		}
		multiAttributeTile(name:"venetianTile", type: "generic", width: 6, height: 4){
			tileAttribute ("venetianState", key: "PRIMARY_CONTROL") {
				attributeState "Slats open", label:'${name}', action:"closeSlats", backgroundColor:"#79b821", nextState:"Slats closing"
				attributeState "Slats closed", label:'${name}', action:"openSlats", backgroundColor:"#ffffff", nextState:"Slats opening"
				attributeState "Slats opening", label:'${name}', action:"closeSlats", backgroundColor:"#79b821", nextState:"Slats open"
				attributeState "Slats closing", label:'${name}', action:"openSlats", backgroundColor:"#ffffff", nextState:"Slats closed"
			}
			tileAttribute ("venetianLevel", key: "SLIDER_CONTROL") {
				attributeState "venetianLevel", action:"setSlatLevel"
			}
	    }
        */
		standardTile("open", "device.open", decoration: "flat", width: 2, height: 2) {
			state("open", label:'OPEN', action:'open', icon: "st.doors.garage.garage-opening")
		}
		standardTile("stop", "device.stop", decoration: "flat", width: 2, height: 2) {
			state("stop", label:'STOP', action:'stop', icon: "st.Transportation.transportation13")
		}
 		standardTile("close", "device.close", decoration: "flat", width: 2, height: 2) {
			state("close", label:'CLOSE', action:'close', icon: "st.doors.garage.garage-closing")
		}
       
		standardTile("preset1", "device.preset1", decoration: "flat", width: 2, height: 2) {
			state("preset1", label:'${currentValue}%', action:'preset1', backgroundColors: [
				[value: 20, color: "#333333"],
				[value: 99, color: "#cccccc"]
			])
		}
		standardTile("preset2", "device.preset2", decoration: "flat", width: 2, height: 2) {
			state("preset2", label:'${currentValue}%', action:'preset2', backgroundColors: [
				[value: 20, color: "#333333"],
				[value: 99, color: "#cccccc"]
			])
		}
		standardTile("preset3", "device.preset3", decoration: "flat", width: 2, height: 2) {
			state("preset3", label:'${currentValue}%', action:'preset3', , backgroundColors: [
				[value: 20, color: "#333333"],
				[value: 99, color: "#cccccc"]
			])
		}

		standardTile("power", "device.power", decoration: "flat", width: 3, height: 2) {
			state("power", label:'${currentValue} W', icon: 'st.Appliances.appliances17')
		}
		standardTile("kwhConsumption", "device.kwhConsumption", decoration: "flat", width: 3, height: 2) {
			state("kwhConsumption", label:'${currentValue} kWh', icon: 'st.Appliances.appliances17')
		}

		standardTile("resetPower", "device.resetPower", decoration: "flat", width: 3, height: 1) {
			state("resetPower", label:'Reset Power', action:'resetPower')
		}
		standardTile("refreshPowerConsumption", "device.refreshPowerConsumption", decoration: "flat", width: 3, height: 1) {
			state("refreshPowerConsumption", label:'Refresh power', action:'refreshPowerConsumption')
		}
		/* //THIS VERSION DOESN?T SUPPORT TEMPERATURE SENSORS YET
		standardTile("temperature", "device.temperature", width: 6, height: 3) {
			state("temperature", label:'${currentValue} ${unit}', unit:'°', icon: 'st.Weather.weather2', backgroundColors: [
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
		*/
		standardTile("setConfiguration", "device.setConfiguration", decoration: "flat", width: 3, height: 1) {
			state("setConfiguration", label:'Set Configuration', action:'setConfiguration')
		}
		standardTile("setAssociation", "device.setAssociation", decoration: "flat", width: 3, height: 1) {
			state("setAssociation", label:'Set Associations', action:'setAssociation')
		}
		standardTile("calibrate", "device.calibrate", decoration: "flat", width: 6, height: 1) {
			state("calibrate", label:'Calibrate', action:'calibrate')
		}

		main("shade")
		details(["shade", "open", "stop", "close"/*, "venetianLabel", "venetianTile"*/, "preset1", "preset2", "preset3", "power", "kwhConsumption", "resetPower", "refreshPowerConsumption", "setConfiguration", "setAssociation", "calibrate"])
	}
	preferences {
				input (
					type: "paragraph",
					element: "paragraph",
					title: "GENERAL SETTINGS:",
					description: "General settings."
				)
                input name: "preset1level", type: "number", required: false,
                	title: "Preset#1 level:", range: "0..99", defaultValue: 25
                input name: "preset2level", type: "number", required: false,
                	title: "Preset#2 level:", range: "0..99", defaultValue: 50
                input name: "preset3level", type: "number", required: false,
                	title: "Preset#3 level:", range: "0..99", defaultValue: 75
/**
*			--------	CONFIGURATION PARAMETER SECTION	--------
*/
				input (
					type: "paragraph",
					element: "paragraph",
					title: "CONFIGURATION PARAMETERS:",
					description: "Configuration parameter settings."
				)
				input name: "param10", type: "enum", required: false,
					options: ["0" : "0 - ALL ON is not active, ALL OFF is not active",
							  "1" : "1 - ALL ON is not active, ALL OFF active",
							  "2" : "2 - ALL ON active, ALL OFF is not active",
							  "255" : "255 - ALL ON active, ALL OFF active"],
					title: "10. Activate / deactivate functions ALL ON / ALL OFF.\n " +
						   "Available settings:\n" +
							"255 - ALL ON active, ALL OFF active.\n" +
							"0 - ALL ON is not active, ALL OFF is not active.\n" +
							"1 - ALL ON is not active, ALL OFF active.\n" +
							"2 - ALL ON active, ALL OFF is not active.\n" +
							"Default value: 255.\n" +
							"Flush Shutter module responds to commands ALL ON / ALL OFF that may be sent by the main controller or by other controller belonging to the system."
				
				input name: "param40", type: "number", range: "0..100", required: false,
					title: "40. Power reporting in Watts on power change.\n" +
						   "Set value means percentage, set value from 0 - 100 = 0% - 100%.\n" +
						   "Available settings:\n" +
							"0 - reporting disabled.\n" +
							"1 - 100 = 1% - 100% Reporting enabled. Power report is send (push) only when actual power in Watts in real time changes for more than set percentage comparing to previous actual power in Watts, step is 1%.\n" +
							"Default value: 5." +
							"NOTE: if power changed is less than 1W, the report is not send (pushed), independent of percentage set."
							
				input name: "param42", type: "number", range: "0..32767", required: false,
					title: "42. Power reporting in Watts by time interval.\n" +
						   "Set value means time interval (0 - 32767) in seconds, when power report is send.\n" +
						   "Available settings:\n" +
							"0 - reporting disabled.\n" +
							"1 - 32767 = 1 second - 32767 seconds. Reporting enabled. Power report is send with time interval set by entered value.\n" +
							"Default value: 0."

				input name: "param71", type: "enum", required: false,
					options: ["0" : "0 - Shutter mode.",
							  "1" : "1 - Venetian mode."],
					title: "71. Operating modes.\n " +
						   "This parameter defines selection between two available operating modes.\n" +
						   "Available settings:\n" +
							"0 - Shutter mode.\n" +
							"1 - Venetian mode.\n" +
							"Default value: 0.\n" +
							"NOTE: After parameter change, first exclude module (without setting parameters to default value) then wait at least 30s and then re include the module!"
				
				input name: "param72", type: "number", range: "0..32767", required: false,
					title: "72. Slats tilting full turn time.\n" +
						   "This parameter defines the time necessary for slats to make full turn (180 degrees).\n" +
						   "Available settings:\n" +
						   "0 - tilting time disabled.\n" +
						   "1 - 32767 = 0,001 seconds - 327,67 seconds.\n" +
						   "Default value: 150 (1,5 seconds).\n" +
						   "NOTE: If time set is too high, this will result that after full turn, Shutter will start move up or down, for time remaining."

				input name: "param73", type: "enum", required: false,
					options: ["0" : "0 - Slats return to previously set position only in case of Z-wave control (not valid for limit switch positions).",
							  "1" : "1 - Slats return to previously set position in case of Z-wave control, push-button operation or when the lower limit switch is reached."],
					title: "73. Slats position.\n " +
						   "This parameter defines slats position after up/down movement through Z-wave or push-buttons.\n" +
						   "Available settings:\n" +
							"0 - Slats return to previously set position only in case of Z-wave control (not valid for limit switch positions).\n" +
							"1 - Slats return to previously set position in case of Z-wave control, push-button operation or when the lower limit switch is reached.\n" +
							"Default value: 1."
							
				input name: "param74", type: "number", range: "0..32767", required: false,
					title: "74. Motor moving up/down time.\n" +
						   "This parameter defines Shutter motor moving time of complete opening or complete closing.\n" +
						   "Available settings:\n" +
						   "0 - moving time disabled (working with limit switches).\n" +
						   "1 - 32767 = 0,1 seconds - 3276,7 seconds.\n" +
						   "Default value: 0.\n" +
						   "NOTE: Important is that the reference position to manually set moving time is always Shutter lower position!"

				input name: "param76", type: "number", range: "0..127", required: false,
					title: "76. Motor operation detection.\n" +
						   "Power threshold to be interpreted when motor reach the limit switch.\n" +
						   "Available settings:\n" +
						   "0 - 127 = 1 - 127 W. The value 0 means reaching a limit switch will not be detected.\n" +
						   "Default value: 30 = 30W."
				
				input name: "param78", type: "enum", required: false,
					options: ["0" : "0 - No calibration.",
							  "1" : "1 - Calibrate shutter."],
					title: "78. Forced Shutter calibration.\n " +
						   "By modifying the parameters setting from 0 to 1 a Shutter enters the calibration mode..\n" +
						   "Available settings:\n" +
							"0 - No calibration.\n" +
							"1 - Start calibration process (when calibration process is finished, completing full cycle - up, down and up, set the parameter 78 (Forced Shutter calibration)value back to 0.\n" +
							"Default value: 0."
							
				input name: "param85", type: "number", range: "0..50", required: false,
					title: "85. Power consumption max delay time.\n" +
						   "This parameter defines the max time before motor power consumption is read after one of the relays is switched ON. If there is no power consumption during this max time (motor not connected, damaged or requires higher time to start, motor in end position) the relay will switch OFF. Time is defined by entering it manually\n" +
						   "Available settings:\n" +
						   "0 - Time is set automatically.\n" +
						   "3 - 50 = 0,3 seconds - 5 seconds (100ms resolution).\n" +
						   "Default value: 30 = 3 seconds."
				
				input name: "param90", type: "number", range: "1..30", required: false,
					title: "90. Time delay for next motor movement.\n" +
						   "This parameter defines the minimum time delay between next motor movement (minimum time between switching motor off and on again).\n" +
						   "Available settings:\n" +
						   "1 - 30 = 0,1 seconds - 3 seconds (100ms resolution).\n" +
						   "Default value: 5 = 500 miliseconds."
					
				input name: "param110", type: "number", range: "1..32536", required: false,
					title: "110. Temperature sensor offset settings.\n" +
						   "Set value is added or subtracted to actual measured value by sensor..\n" +
						   "Available settings:\n" +
							"32536 - offset is 0.0C.\n" +
							"From 1 to 100 - value from 0.1 °C to 10.0 °C is added to actual measured temperature.\n" +
							"From 1001 to 1100 - value from -0.1 °C to -10.0 °C is subtracted to actual measured temperature.\n" +
							"Default value: 32536."

				input name: "param120", type: "number", range: "0..127", required: false,
					title: "120. Digital temperature sensor reporting.\n" +
						   "If digital temperature sensor is connected, module reports measured temperature on temperature change defined by this parameter.\n" +
						   "Available settings:\n" +
							"0 - Reporting disabled.\n" +
							"1 - 127 = 0,1°C - 12,7°C, step is 0,1°C.\n" +
							"Default value: 5 = 0,5°C change."

				input name: "param249", type: "enum", required: false,
					options: ["0" : "0 – Disable reporting.",
							  "1" : "1 – Enable reporting."],
					title: "249. Enable/Disable Reporting on Set command.\n" +
						   "Available settings:\n" +
							"0 – Disable reporting.\n" +
							"1 – Enable reporting.\n" +
							"Default value: 1."
			
/**
*			--------	ASSOCIATION GROUP SECTION	--------
*/
				input (
					type: "paragraph",
					element: "paragraph",
					title: "ASSOCIATION GROUPS:",
					description: "Association group settings."
				)
				input name: "assocGroup2", type: "text", required: false,
					title: "Association group 2: \n" +
						   "Basic on/off (triggered at change of the input I1 state and reflecting its state) up to 16 nodes.\n" +
						   "NOTE: Insert the node Id value of the devices you wish to associate this group with. Multiple nodeIds can also be set at once by separating individual values by a comma (2,3,...)."
						   
				input name: "assocGroup3", type: "text", required: false,
					title: "Association group 3: \n" +
						   "Basic on/off (triggered at change of the input I2 state and reflecting its state) up to 16 nodes." +
						   "NOTE: Insert the node Id value of the devices you wish to associate this group with. Multiple nodeIds can also be set at once by separating individual values by a comma (2,3,...)."
						   
				input name: "assocGroup4", type: "text", required: false,
					title: "Association group 4: \n" +
						   "basic on/off (triggered at sensing moving direction of roller: up=FF, down=0) up to 16 nodes." +
						   "NOTE: Insert the node Id value of the devices you wish to associate this group with. Multiple nodeIds can also be set at once by separating individual values by a comma (2,3,...)."
						   
				input name: "assocGroup5", type: "text", required: false,
					title: "Association group 5: \n" +
						   "Basic on/off (triggered at reaching roller position: bottom=FF, top=0) up to 16 nodes.\n" +
						   "NOTE: Insert the node Id value of the devices you wish to associate this group with. Multiple nodeIds can also be set at once by separating individual values by a comma (2,3,...)."
						   
				input name: "assocGroup6", type: "text", required: false,
					title: "Association group 6: \n" +
						   "Basic on/off (triggered at reaching roller position: bottom=FF, not bottom=0) up to 16 nodes.\n" +
						   "NOTE: Insert the node Id value of the devices you wish to associate this group with. Multiple nodeIds can also be set at once by separating individual values by a comma (2,3,...)."
						   
				input name: "assocGroup7", type: "text", required: false,
					title: "Association group 7: \n" +
						   "Multilevel set (triggered at changes of value of the Flush Shutter position) up to 16 nodes.\n" +
						   "NOTE: Insert the node Id value of the devices you wish to associate this group with. Multiple nodeIds can also be set at once by separating individual values by a comma (2,3,...)."
						   
				input name: "assocGroup8", type: "text", required: false,
					title: "Association group 8: \n" +
						   "Multilevel set (triggered at changes of value of slats tilting position) up to 16 nodes.\n" +
						   "NOTE: Insert the node Id value of the devices you wish to associate this group with. Multiple nodeIds can also be set at once by separating individual values by a comma (2,3,...)."
						   
				input name: "assocGroup9", type: "text", required: false,
					title: "Association group 9: \n" +
						   "Multilevel sensor report (triggered at change of temperature sensor) up to 16 nodes.\n" +
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
	log.debug stringList
	if(stringList != null){
		for(int i=0;i<stringList.size();i++){
			stringList[i] = stringList[i].toInteger()
		}
	}
	return stringList
}
/**
 * Converts temperature values to fahrenheit or celsius scales according to user's setting.
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
 * Configuration capability command handler.
 *
 * @param void
 * @return List of commands that will be executed in sequence with 500 ms delay inbetween.
*/
def configure() {
	log.debug "Qubino Flush Shutter: configure()"
	state.isMcDevice = false
	log.debug state.isMcDevice
	def assocCmds = []
	assocCmds << zwave.associationV1.associationRemove(groupingIdentifier:1).format()
	assocCmds << zwave.associationV1.associationSet(groupingIdentifier:1, nodeId:zwaveHubNodeId).format()
	assocCmds << zwave.multiChannelV3.multiChannelEndPointGet().format()
	return delayBetween(assocCmds, 500)
}

def installed() {
	log.debug "Qubino Flush Shutter: installed()"
    updated()
}

def updated() {
	log.debug "Qubino Flush Shutter: updated()"
    def level1=preset1level
    if(!level1) { level1=25 }
    sendEvent(name: "preset1", value: level1, displayed: false)
    def level2=preset2level
    if(!level2) { level2=50 }
    sendEvent(name: "preset2", value: level2, displayed: false)
    def level3=preset3level
    if(!level3) { level3=75 }
    sendEvent(name: "preset3", value: level3, displayed: false)
}

def preset1() {
	def level=preset1level
    if (!level) { level=25 }
	log.debug "Qubino Flush Shutter: preset1(${level})"
   	setLevel(level)
}

def preset2() {
	def level=preset2level
    if (!level) { level=50 }
	log.debug "Qubino Flush Shutter: preset2(${level})"
   	setLevel(level)
}

def preset3() {
	def level=preset3level
    if (!level) { level=75 }
	log.debug "Qubino Flush Shutter: preset3(${level})"
   	setLevel(level)
}


def on() {
	log.debug "Qubino Flush Shutter: on()"
	open()
}

def off() {
	log.debug "Qubino Flush Shutter: off()"
	close()
}

/**
 * Stop command handler. Issues StopLevelChange when operating as singlechannel device handler.
 *
 * @param void
 * @return List of commands that will be executed in sequence with 500 ms delay inbetween.
*/
def stop() {
	log.debug "Qubino Flush Shutter: stop()"
	def cmds = []
	cmds << zwave.switchMultilevelV3.switchMultilevelStopLevelChange().format()
	cmds << zwave.switchMultilevelV3.switchMultilevelGet().format()
	return delayBetween(cmds, 1000)
}
/**
 * stopSlats command handler. Issues StopLevelChange to endpoint 2 of the device when operating as multichannel device handler.
 *
 * @param void
 * @return List of commands that will be executed in sequence with 500 ms delay inbetween.
*/
def stopSlats(){
	log.debug "Qubino Flush Shutter: stopSlats()"
	def cmds = []
	cmds << zwave.multiChannelV3.multiChannelCmdEncap(sourceEndPoint:1, destinationEndPoint:2).encapsulate(zwave.switchMultilevelV3.switchMultilevelStopLevelChange()).format()
	cmds << zwave.multiChannelV3.multiChannelCmdEncap(sourceEndPoint:1, destinationEndPoint:2).encapsulate(zwave.switchMultilevelV3.switchMultilevelGet()).format()
	return delayBetween(cmds, 500)
}
/**
 * Switch capability command handler for ON state. It issues a Switch Multilevel Set command with value 0xFF and instantaneous duration.
 * This command is followed by a Switch Multilevel Get command, that updates the actual state of the main shutter control.
 *		
 * @param void
 * @return void.
*/
def open() {
	log.debug "Qubino Flush Shutter: open()"
	return zwave.switchMultilevelV3.switchMultilevelSet(value: 0xFF, dimmingDuration: 0x00).format()
}
/**
 * Window Shade capability command handler for closed state. It issues a Switch Multilevel Set command with value 0x00 and instantaneous duration.
 *		
 * @param void
 * @return void.
*/
def close() {
	log.debug "Qubino Flush Shutter: close()"
	return zwave.switchMultilevelV3.switchMultilevelSet(value: 0x00, dimmingDuration: 0x00).format()
}
/**
 * Command handler for open slats command. It issues a Switch Multilevel Set command with value 0xFF to endpoint 2 of the device and instantaneous duration.
 *		
 * @param void
 * @return void.
*/
def openSlats(){
	log.debug "Qubino Flush Shutter: openSlats()"
	//zwave.multiChannelV3.multiChannelCmdEncap(sourceEndPoint:1, destinationEndPoint:2).encapsulate(zwave.switchMultilevelV3.switchMultilevelSet(value: 0xFF, dimmingDuration: 0x00)).format()//change regarding full open state, will revert after discussion with sigma
	log.debug settings.param72
	if(settings.param72 == null){
		return delayBetween([
			zwave.multiChannelV3.multiChannelCmdEncap(sourceEndPoint:1, destinationEndPoint:2).encapsulate(zwave.switchMultilevelV3.switchMultilevelSet(value: 0x63, dimmingDuration: 0x00)).format(),
			zwave.multiChannelV3.multiChannelCmdEncap(sourceEndPoint:1, destinationEndPoint:2).encapsulate(zwave.switchMultilevelV3.switchMultilevelGet()).format()
		], 1500)
	}else{
		def tempTurningTime = settings.param72.toInteger()*10
		return delayBetween([
			zwave.multiChannelV3.multiChannelCmdEncap(sourceEndPoint:1, destinationEndPoint:2).encapsulate(zwave.switchMultilevelV3.switchMultilevelSet(value: 0x63, dimmingDuration: 0x00)).format(),
			zwave.multiChannelV3.multiChannelCmdEncap(sourceEndPoint:1, destinationEndPoint:2).encapsulate(zwave.switchMultilevelV3.switchMultilevelGet()).format()
		], tempTurningTime)
	}
}
/**
 * Command handler for close slats command. It issues a Switch Multilevel Set command with value 0x00 to endpoint 2 of the device and instantaneous duration.
 *		
 * @param void
 * @return void.
*/
def closeSlats(){
	log.debug "Qubino Flush Shutter: closeSlats()"
	//zwave.multiChannelV3.multiChannelCmdEncap(sourceEndPoint:1, destinationEndPoint:2).encapsulate(zwave.switchMultilevelV3.switchMultilevelSet(value: 0x00, dimmingDuration: 0x00)).format()
	log.debug settings.param72
	if(settings.param72 == null){
		return delayBetween([
			zwave.multiChannelV3.multiChannelCmdEncap(sourceEndPoint:1, destinationEndPoint:2).encapsulate(zwave.switchMultilevelV3.switchMultilevelSet(value: 0x00, dimmingDuration: 0x00)).format(),
			zwave.multiChannelV3.multiChannelCmdEncap(sourceEndPoint:1, destinationEndPoint:2).encapsulate(zwave.switchMultilevelV3.switchMultilevelGet()).format()
		], 1500)
	}else{
		def tempTurningTime = settings.param72.toInteger()*10
		return delayBetween([
			zwave.multiChannelV3.multiChannelCmdEncap(sourceEndPoint:1, destinationEndPoint:2).encapsulate(zwave.switchMultilevelV3.switchMultilevelSet(value: 0x00, dimmingDuration: 0x00)).format(),
			zwave.multiChannelV3.multiChannelCmdEncap(sourceEndPoint:1, destinationEndPoint:2).encapsulate(zwave.switchMultilevelV3.switchMultilevelGet()).format()
		], tempTurningTime)
	}
}
/**
 * Command handler for setting the slat tilt level. It issues a Switch Multilevel Set command with specified value and instantaneous duration.
 * We need to limit the max value to 99% by Z-Wave protocol definitions.
 * 
 * @param void
 * @return void.
*/
def setSlatLevel(level) {
	log.debug "Qubino Flush Shutter: setSlatLevel()"
	log.debug level
	if(level > 99) level = 99
	//zwave.multiChannelV3.multiChannelCmdEncap(sourceEndPoint:1, destinationEndPoint:2).encapsulate(zwave.switchMultilevelV3.switchMultilevelSet(value: level, dimmingDuration: 0x00)).format()
	if(settings.param72 == null){
		return delayBetween([
			zwave.multiChannelV3.multiChannelCmdEncap(sourceEndPoint:1, destinationEndPoint:2).encapsulate(zwave.switchMultilevelV3.switchMultilevelSet(value: level, dimmingDuration: 0x00)).format(),
			zwave.multiChannelV3.multiChannelCmdEncap(sourceEndPoint:1, destinationEndPoint:2).encapsulate(zwave.switchMultilevelV3.switchMultilevelGet()).format()
		], 1500)
	}else{
		def tempTurningTime = settings.param72.toInteger()*10
		return delayBetween([
			zwave.multiChannelV3.multiChannelCmdEncap(sourceEndPoint:1, destinationEndPoint:2).encapsulate(zwave.switchMultilevelV3.switchMultilevelSet(value: level, dimmingDuration: 0x00)).format(),
			zwave.multiChannelV3.multiChannelCmdEncap(sourceEndPoint:1, destinationEndPoint:2).encapsulate(zwave.switchMultilevelV3.switchMultilevelGet()).format()
		], tempTurningTime)
	}
}
/**
 * Command handler for calibrating the shutter module. It does the same as setting configuration parameter 78 to 1. Available on the details page for convenience.
 *		
 * @param void
 * @return void.
*/
def calibrate() {
	log.debug "Qubino Flush Shutter: calibrate()"
	zwave.configurationV1.configurationSet(parameterNumber: 78, size: 1, scaledConfigurationValue: 1).format()
}

/**
 * Window Shade capability command handler for a blinds level state. It issues a Switch Multilevel Set command with value contained in the parameter value and instantaneous duration.
 * We need to limit the max value to 99% by Z-Wave protocol definitions.
 *		
 * @param level The desired value of the dimmer we are trying to set.
 * @return void.
*/
def setLevel(level) {
	log.debug "Qubino Flush Shutter: setLevel()"
	if(level > 99) level = 99
	zwave.switchMultilevelV3.switchMultilevelSet(value: level, dimmingDuration: 0x00).format()
}

/**
 * Refresh Power Consumption command handler for updating the cumulative consumption fields in kWh. It will issue a Meter Get command with scale parameter set to kWh.
 *		
 * @param void.
 * @return void.
*/
def refreshPowerConsumption() {
	log.debug "Qubino Flush Shutter: refreshPowerConsumption()"
	delayBetween([
		zwave.meterV2.meterGet(scale: 0).format(),
		zwave.meterV2.meterGet(scale: 2).format(),
        zwave.switchMultilevelV3.switchMultilevelGet().format()
    ], 1000)
}
/**
 * Reset Power Consumption command handler for resetting the cumulative consumption fields in kWh. It will issue a Meter Reset command followed by Meter Get commands for active and accumulated power.
 *		
 * @param void.
 * @return void.
*/
def resetPower() {
	log.debug "Qubino Flush Shutter: resetPower()"
	zwave.meterV2.meterReset()
	delayBetween([
		zwave.meterV2.meterReset(),
		zwave.meterV2.meterGet(scale: 0).format(),
		zwave.meterV2.meterGet(scale: 2).format()
    ], 1000)
}

/**
 * setAssociations command handler that sets user selected association groups. In case no node id is insetred the group is instead cleared.
 * Lifeline association hidden from user influence by design.
 *
 * @param void
 * @return List of Association commands that will be executed in sequence with 500 ms delay inbetween.
*/

def setAssociation() {
	log.debug "Qubino Flush Shutter: setAssociation()"
	def assocSet = []
	if(settings.assocGroup2 != null){
		def group2parsed = settings.assocGroup2.tokenize(",")
		if(group2parsed == null){
			assocSet << zwave.associationV1.associationSet(groupingIdentifier:2, nodeId:settings.assocGroup2).format()
		}else{
			group2parsed = convertStringListToIntegerList(group2parsed)
			assocSet << zwave.associationV1.associationSet(groupingIdentifier:2, nodeId:group2parsed).format()
		}
	}else{
		assocSet << zwave.associationV2.associationRemove(groupingIdentifier:2).format()
	}
	if(settings.assocGroup3 != null){
		def group3parsed = settings.assocGroup3.tokenize(",")
		if(group3parsed == null){
			assocSet << zwave.associationV1.associationSet(groupingIdentifier:3, nodeId:settings.assocGroup3).format()
		}else{
			group3parsed = convertStringListToIntegerList(group3parsed)
			assocSet << zwave.associationV1.associationSet(groupingIdentifier:3, nodeId:group3parsed).format()
		}
	}else{
		assocSet << zwave.associationV2.associationRemove(groupingIdentifier:3).format()
	}
	if(settings.assocGroup4 != null){
		def group4parsed = settings.assocGroup4.tokenize(",")
		if(group4parsed == null){
			assocSet << zwave.associationV1.associationSet(groupingIdentifier:4, nodeId:settings.assocGroup4).format()
		}else{
			group4parsed = convertStringListToIntegerList(group4parsed)
			assocSet << zwave.associationV1.associationSet(groupingIdentifier:4, nodeId:group4parsed).format()
		}
	}else{
		assocSet << zwave.associationV2.associationRemove(groupingIdentifier:4).format()
	}
	if(settings.assocGroup5 != null){
		def group5parsed = settings.assocGroup5.tokenize(",")
		if(group5parsed == null){
			assocSet << zwave.associationV1.associationSet(groupingIdentifier:5, nodeId:settings.assocGroup5).format()
		}else{
			group5parsed = convertStringListToIntegerList(group5parsed)
			assocSet << zwave.associationV1.associationSet(groupingIdentifier:5, nodeId:group5parsed).format()
		}
	}else{
		assocSet << zwave.associationV2.associationRemove(groupingIdentifier:5).format()
	}
	if(settings.assocGroup6 != null){
		def group6parsed = settings.assocGroup6.tokenize(",")
		if(group6parsed == null){
			assocSet << zwave.associationV1.associationSet(groupingIdentifier:6, nodeId:settings.assocGroup6).format()
		}else{
			group6parsed = convertStringListToIntegerList(group6parsed)
			assocSet << zwave.associationV1.associationSet(groupingIdentifier:6, nodeId:group6parsed).format()
		}
	}else{
		assocSet << zwave.associationV2.associationRemove(groupingIdentifier:6).format()
	}
	if(settings.assocGroup7 != null){
		def group7parsed = settings.assocGroup7.tokenize(",")
		if(group7parsed == null){
			assocSet << zwave.associationV1.associationSet(groupingIdentifier:7, nodeId:settings.assocGroup7).format()
		}else{
			group7parsed = convertStringListToIntegerList(group7parsed)
			assocSet << zwave.associationV1.associationSet(groupingIdentifier:7, nodeId:group7parsed).format()
		}
	}else{
		assocSet << zwave.associationV2.associationRemove(groupingIdentifier:7).format()
	}
	if(settings.assocGroup8 != null){
		def group8parsed = settings.assocGroup8.tokenize(",")
		if(group8parsed == null){
			assocSet << zwave.associationV1.associationSet(groupingIdentifier:8, nodeId:settings.assocGroup8).format()
		}else{
			group8parsed = convertStringListToIntegerList(group8parsed)
			assocSet << zwave.associationV1.associationSet(groupingIdentifier:8, nodeId:group8parsed).format()
		}
	}else{
		assocSet << zwave.associationV2.associationRemove(groupingIdentifier:8).format()
	}
	if(settings.assocGroup9 != null){
		def group9parsed = settings.assocGroup9.tokenize(",")
		if(group9parsed == null){
			assocSet << zwave.associationV1.associationSet(groupingIdentifier:9, nodeId:settings.assocGroup9).format()
		}else{
			group9parsed = convertStringListToIntegerList(group9parsed)
			assocSet << zwave.associationV1.associationSet(groupingIdentifier:9, nodeId:group9parsed).format()
		}
	}else{
		assocSet << zwave.associationV2.associationRemove(groupingIdentifier:9).format()
	}
	if(assocSet.size() > 0){
		return delayBetween(assocSet, 500)
	}
}

/**
 * setConfigurationParams command handler that sets user selected configuration parameters on the device. 
 * In case no value is set for a specific parameter the method skips setting that parameter.
 * Secure mode setting hidden from user influence by design.
 *
 * @param void
 * @return List of Configuration Set commands that will be executed in sequence with 500 ms delay inbetween.
*/

def setConfiguration() {
	log.debug "Qubino Flush Shutter: setConfiguration()"
	def configSequence = []
	if(settings.param10 != null){
		configSequence << zwave.configurationV1.configurationSet(parameterNumber: 10, size: 2, scaledConfigurationValue: settings.param10.toInteger()).format()
	}
	if(settings.param40 != null){
		configSequence << zwave.configurationV1.configurationSet(parameterNumber: 40, size: 1, scaledConfigurationValue: settings.param40.toInteger()).format()
	}
	if(settings.param42 != null){
		configSequence << zwave.configurationV1.configurationSet(parameterNumber: 42, size: 2, scaledConfigurationValue: settings.param42.toInteger()).format()
	}
	if(settings.param71 != null){
		configSequence << zwave.configurationV1.configurationSet(parameterNumber: 71, size: 1, scaledConfigurationValue: settings.param71.toInteger()).format()
	}
	if(settings.param72 != null){
		configSequence << zwave.configurationV1.configurationSet(parameterNumber: 72, size: 2, scaledConfigurationValue: settings.param72.toInteger()).format()
	}
	if(settings.param73 != null){
		configSequence << zwave.configurationV1.configurationSet(parameterNumber: 73, size: 1, scaledConfigurationValue: settings.param73.toInteger()).format()
	}
	if(settings.param74 != null){
		configSequence << zwave.configurationV1.configurationSet(parameterNumber: 74, size: 2, scaledConfigurationValue: settings.param74.toInteger()).format()
	}
	if(settings.param76 != null){
		configSequence << zwave.configurationV1.configurationSet(parameterNumber: 76, size: 1, scaledConfigurationValue: settings.param76.toInteger()).format()
	}
	if(settings.param78 != null){
		configSequence << zwave.configurationV1.configurationSet(parameterNumber: 78, size: 1, scaledConfigurationValue: settings.param78.toInteger()).format()
	}
	if(settings.param85 != null){
		configSequence << zwave.configurationV1.configurationSet(parameterNumber: 85, size: 1, scaledConfigurationValue: settings.param85.toInteger()).format()
	}
	if(settings.param90 != null){
		configSequence << zwave.configurationV1.configurationSet(parameterNumber: 90, size: 1, scaledConfigurationValue: settings.param90.toInteger()).format()
	}
	if(settings.param110 != null){
		configSequence << zwave.configurationV1.configurationSet(parameterNumber: 110, size: 2, scaledConfigurationValue: settings.param110.toInteger()).format()
	}
	if(settings.param120 != null){
		configSequence << zwave.configurationV1.configurationSet(parameterNumber: 120, size: 1, scaledConfigurationValue: settings.param120.toInteger()).format()
	}
	if(configSequence.size() > 0){
		return delayBetween(configSequence, 500)
	}
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
	log.debug "Qubino Flush Shutter: Parsing '${description}'"
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
 * Event handler for received Sensor Multilevel Report frames. These are for the temperature sensor connected to TS connector.
 *
 * @param cmd communication frame
 * @return Event that updates the temperature values with received values.
*/
def zwaveEvent(physicalgraph.zwave.commands.sensormultilevelv5.SensorMultilevelReport cmd){
	log.debug "Qubino Flush Shutter: SensorMultilevelReport handler fired"
	def resultEvents = []
	resultEvents << createEvent(name:"temperature", value: convertDegrees(location.temperatureScale,cmd), unit:"°"+location.temperatureScale, descriptionText: "Temperature: "+convertDegrees(location.temperatureScale,cmd)+"°"+location.temperatureScale, isStateChange: true)
	return resultEvents
}
/**
 * Event handler for received Switch Multilevel Report frames.
 *
 * @param cmd communication frame
 * @return List of events to update the ON / OFF and analogue control elements with received values.
*/
def zwaveEvent(physicalgraph.zwave.commands.switchmultilevelv3.SwitchMultilevelReport cmd){
	log.debug "Qubino Flush Shutter: firing switch multilevel event"
	def result = []
    def currentState = device.currentState("windowShade")
    def currentSwitch = device.currentState("switch")
    def currentLevel = device.currentState("level")
    def desiredState = "closed"
    if (cmd.value<99 && cmd.value>0) {
        desiredState = "partially open"
    } else if (cmd.value >= 99) {
        desiredState = "open"
    }
    def desiredSwitch = cmd.value ? "on" : "off"
    result << createEvent(name:"switch", value: desiredSwitch, isStateChange: (currentSwitch?.value!=desiredSwitch))
    result << createEvent(name:"windowShade", value: desiredState, isStateChange: (currentState?.value!=desiredState))
	if(cmd.value > 99){
		result << createEvent(name:"level", value: cmd.value, unit:"%", descriptionText:"${device.displayName} is uncalibrated! Please press calibrate!", isStateChange: true)
	}else{
		result << createEvent(name:"level", value: cmd.value, unit:"%", descriptionText:"${device.displayName} moved to ${cmd.value==99 ? 100 : cmd.value}%", isStateChange: (currentLevel?.value.toInteger()!=cmd.value.toInteger()))
	}
	return result
}
/**
 * Event handler for received MC Encapsulated Switch Multilevel Report frames.
 *
 * @param cmd communication frame, command mc encapsulated communication frame; needed to distinguish sources
 * @return List of events to update the ON / OFF and analogue control elements with received values.
*/
def zwaveEvent(physicalgraph.zwave.commands.switchmultilevelv3.SwitchMultilevelReport cmd, physicalgraph.zwave.commands.multichannelv3.MultiChannelCmdEncap command){
	log.debug "Qubino Flush Shutter: firing MC switch multilevel event"
	def result = []
	switch(command.sourceEndPoint){
		case 1:
            def currentState = device.currentState("windowShade")
            def currentSwitch = device.currentState("switch")
            def currentLevel = device.currentState("level")
            def desiredState = "closed"
            if (cmd.value<99 && cmd.value>0) {
                desiredState = "partially open"
            } else if (cmd.value >= 99) {
                desiredState = "open"
            }
            def desiredSwitch = cmd.value ? "on" : "off"
            result << createEvent(name:"switch", value: desiredSwitch, isStateChange: (currentSwitch?.value!=desiredSwitch))
            result << createEvent(name:"windowShade", value: desiredState, isStateChange: (currentState?.value!=desiredState))
            if(cmd.value > 99){
                result << createEvent(name:"level", value: cmd.value, unit:"%", descriptionText:"${device.displayName} is uncalibrated! Please press calibrate!", isStateChange: true)
            }else{
                result << createEvent(name:"level", value: cmd.value, unit:"%", descriptionText:"${device.displayName} moved to ${cmd.value==99 ? 100 : cmd.value}%", isStateChange: (currentLevel?.value.toInteger()!=cmd.value.toInteger()))
            }
		break;
		case 2:
			log.debug "Received command from EP2"
			log.debug cmd
			result << createEvent(name:"venetianState", value: cmd.value ? "Slats open" : "Slats closed", isStateChange: true)
			if(cmd.value > 99){
				result << createEvent(name:"venetianLevel", value: cmd.value, unit:"%", descriptionText:"${device.displayName} is uncalibrated! Please press calibrate!", isStateChange: true)
			}else{
				result << createEvent(name:"venetianLevel", value: cmd.value, unit:"%", descriptionText:"${device.displayName} tilted slats to ${cmd.value==99 ? 100 : cmd.value}%", isStateChange: true)
			}
		break;
	}
	return result
}
/**
 * Event handler for received MC Encapsulated Meter Report frames.
 *
 * @param cmd communication frame, command mc encapsulated communication frame; needed to distinguish sources
 * @return List of events to update the power consumption elements with received values.
*/
def zwaveEvent(physicalgraph.zwave.commands.meterv3.MeterReport cmd, physicalgraph.zwave.commands.multichannelv3.MultiChannelCmdEncap command){
	log.debug "Qubino Flush Shutter: firing MC Encap meter report event"
	def result = []
	switch(cmd.scale){
		case 0:
			result << createEvent(name:"kwhConsumption", value: cmd.scaledMeterValue, unit:"kWh", descriptionText:"${device.displayName} consumed ${cmd.scaledMeterValue} kWh", isStateChange: true)
			break;
		case 2:
			result << createEvent(name:"power", value: cmd.scaledMeterValue, unit:"W", descriptionText:"${device.displayName} consumes ${cmd.scaledMeterValue} W", isStateChange: true)
			break;
	}
	return result
}
/**
 * Event handler for received Meter Report frames. Used for displaying W and kWh measurements.
 *
 * @param void
 * @return Power consumption event for W data or kwhConsumption event for kWh data.
*/
def zwaveEvent(physicalgraph.zwave.commands.meterv3.MeterReport cmd) {
	log.debug "Qubino Flush Shutter: firing meter report event"
	def result = []
	switch(cmd.scale){
		case 0:
    		def currentPower = device.currentState("kwhConsumption")
			result << createEvent(name:"kwhConsumption", value: cmd.scaledMeterValue, unit:"kWh", descriptionText:"${device.displayName} consumed ${cmd.scaledMeterValue} kWh", isStateChange: (cmd.scaledMeterValue?.toDouble() != currentPower?.value?.toDouble()))
			break;
		case 2:
    		def currentPower = device.currentState("power")
			result << createEvent(name:"power", value: cmd.scaledMeterValue, unit:"W", descriptionText:"${device.displayName} consumes ${cmd.scaledMeterValue} W", isStateChange: (cmd.scaledMeterValue?.toDouble() != currentPower?.value?.toDouble()))
			break;
	}
	return result
}

/**
 * Event handler for received Configuration Report frames. Used for debugging purposes. 
 *
 * @param void
 * @return void.
*/
def zwaveEvent(physicalgraph.zwave.commands.configurationv2.ConfigurationReport cmd){
	log.debug "Qubino Flush Shutter: firing configuration report event"
	log.debug cmd.configurationValue
}
/**
 * Event handler for received Basic Report frames. ST sends these for some reason every 10 seconds. 
 *
 * @param cmd communication frame
 * @return Main roller level state events.
*/
def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicReport cmd){
    def currentState = device.currentState("windowShade")
    def currentSwitch = device.currentState("switch")
    def currentLevel = device.currentState("level")
    def desiredState = "closed"
    if (cmd.value<99 && cmd.value>0) {
    	desiredState = "partially open"
    } else if (cmd.value >= 99) {
    	desiredState = "open"
    }
    def desiredSwitch = cmd.value ? "on" : "off"
	log.debug "Qubino Flush Shutter: firing basic report event (currentState: ${currentState?.value}, currentLevel: ${currentLevel?.value}, desiredState: $desiredState, desiredLevel: ${cmd.value})"
	def result = []
    result << createEvent(name:"switch", value: desiredSwitch, isStateChange: (currentSwitch?.value!=desiredSwitch))
    result << createEvent(name:"windowShade", value: desiredState, isStateChange: (currentState?.value!=desiredState))
    if(cmd.value > 99){
        result << createEvent(name:"level", value: cmd.value, unit:"%", descriptionText:"${device.displayName} is uncalibrated! Please press calibrate!", isStateChange: true)
    }else{
        result << createEvent(name:"level", value: cmd.value, unit:"%", descriptionText:"${device.displayName} moved to ${cmd.value==99 ? 100 : cmd.value}%", isStateChange: (currentLevel?.value.toInteger()!=cmd.value.toInteger()))
    }
	return result
}
/**
 * Event handler for received MultiChannelEndPointReport commands. Used to distinguish when the device is in singlechannel or multichannel configuration. 
 *
 * @param cmd communication frame
 * @return commands to set up a MC Lifeline association.
*/
def zwaveEvent(physicalgraph.zwave.commands.multichannelv3.MultiChannelEndPointReport cmd){
	log.debug "Qubino Flush Shutter: firing MultiChannelEndPointReport"
	if(cmd.endPoints > 0){
		state.isMcDevice = true;
	}
	def cmds = []
	cmds << response(zwave.associationV1.associationRemove(groupingIdentifier:1).format())
	//cmds << "delay 500"
	cmds << response(zwave.multiChannelAssociationV2.multiChannelAssociationSet(groupingIdentifier: 1, nodeId: [0,zwaveHubNodeId,1]).format())
	return cmds
}
/**
 * Event handler for received MultiChannelCapabilityReport commands. Used to distinguish when the device is in singlechannel or multichannel configuration. 
 *
 * @param cmd communication frame
 * @return commands to set up a MC Lifeline association.
*/
def zwaveEvent(physicalgraph.zwave.commands.multichannelv3.MultiChannelCapabilityReport cmd){
	log.debug "Qubino Flush Shutter: firing MultiChannelCapabilityReport"
	state.isMcDevice = true
	def cmds = []
	cmds << response(zwave.associationV1.associationRemove(groupingIdentifier:1).format())
	//cmds << "delay 500"
	cmds << response(zwave.multiChannelAssociationV2.multiChannelAssociationSet(groupingIdentifier: 1, nodeId: [0,zwaveHubNodeId,1]).format())
	return cmds
}
/**
 * Event handler for received Multi Channel Encapsulated commands.
 *
 * @param cmd encapsulated communication frame
 * @return parsed event.
*/
def zwaveEvent(physicalgraph.zwave.commands.multichannelv3.MultiChannelCmdEncap cmd){
	log.debug "Qubino Flush Shutter: firing MC Encapsulation event"
	def encapsulatedCommand = cmd.encapsulatedCommand()
	//log.debug ("Command from endpoint ${cmd.sourceEndPoint}: ${encapsulatedCommand}")
	if (encapsulatedCommand) {
			return zwaveEvent(encapsulatedCommand, cmd)
	}
}