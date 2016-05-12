/**
 *  Copyright 2015 SmartThings
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 */
metadata {
	definition (name: "Fibaro Dimmer 2", namespace: "smartthings", author: "Rajiv, Elnar Hajiyev") {
		capability "Energy Meter"
		capability "Actuator"
		capability "Switch"
		capability "Power Meter"
		capability "Polling"
		capability "Refresh"
		capability "Sensor"
		capability "Configuration"
        capability "Switch Level"
        
        //Extending Fibaro Dimmer 2 devices with scene attribute
        attribute "scene", "number"

		command "reset"
        command "configureAfterSecure"

        fingerprint deviceId: "0x1001", inClusters: "0x5E, 0x20, 0x86, 0x72, 0x26, 0x5A, 0x59, 0x85, 0x73, 0x98, 0x7A, 0x56, 0x70, 0x31, 0x32, 0x8E, 0x60, 0x75, 0x71, 0x27, 0x22, 0xEF, 0x2B"
	}

	// simulator metadata
	simulator {
    	status "on":  "command: 2003, payload: FF"
		status "off": "command: 2003, payload: 00"
		status "09%": "command: 2003, payload: 09"
		status "10%": "command: 2003, payload: 0A"
		status "33%": "command: 2003, payload: 21"
		status "66%": "command: 2003, payload: 42"
		status "99%": "command: 2003, payload: 63"

		for (int i = 0; i <= 10000; i += 1000) {
			status "power  ${i} W": new physicalgraph.zwave.Zwave().meterV3.meterReport(
				scaledMeterValue: i, precision: 3, meterType: 4, scale: 2, size: 4).incomingMessage()
		}
		for (int i = 0; i <= 100; i += 10) {
			status "energy  ${i} kWh": new physicalgraph.zwave.Zwave().meterV3.meterReport(
				scaledMeterValue: i, precision: 3, meterType: 0, scale: 0, size: 4).incomingMessage()
		}
        
        ["FF", "00", "09", "0A", "21", "42", "63"].each { val ->
			reply "2001$val,delay 100,2602": "command: 2603, payload: $val"
		}
	}

	// tile definitions
    
	tiles(scale: 2) {
    	multiAttributeTile(name:"switch", type: "lighting", width: 6, height: 4, canChangeIcon: true){
			tileAttribute ("device.switch", key: "PRIMARY_CONTROL") {
				attributeState "on", label:'${name}', action:"switch.off", icon:"st.switches.switch.on", backgroundColor:"#79b821", nextState:"turningOff"
				attributeState "off", label:'${name}', action:"switch.on", icon:"st.switches.switch.off", backgroundColor:"#ffffff", nextState:"turningOn"
				attributeState "turningOn", label:'${name}', action:"switch.off", icon:"st.switches.switch.on", backgroundColor:"#79b821", nextState:"turningOff"
				attributeState "turningOff", label:'${name}', action:"switch.on", icon:"st.switches.switch.off", backgroundColor:"#ffffff", nextState:"turningOn"
			}
            tileAttribute ("device.level", key: "SLIDER_CONTROL") {
				attributeState "level", action:"switch level.setLevel"
			}
		}
		valueTile("power", "device.power", decoration: "flat", width: 2, height: 2) {
			state "default", label:'${currentValue} W'
		}
		valueTile("energy", "device.energy", decoration: "flat", width: 2, height: 2) {
			state "default", label:'${currentValue} kWh'
		}
		standardTile("reset", "device.energy", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
			state "default", label:'reset kWh', action:"reset"
		}    
		standardTile("configureAfterSecure", "device.configure", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
			state "configure", label:'', action:"configureAfterSecure", icon:"st.secondary.configure"
		}
		standardTile("refresh", "device.power", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
			state "default", label:'', action:"refresh.refresh", icon:"st.secondary.refresh"
		}

		main(["switch","power","energy"])
		details(["switch","power","energy","configureAfterSecure","refresh","reset"])
	}
    
    preferences {
        def paragraph = "GROUP 0 - The Dimmer 2 behavior - Basic functionalities"
        input name: "param1", type: "number", range: "1..98", defaultValue: "1", required: true,
            title: paragraph + "\n\n" +
                   "1. Minimum brightness level. " +
                   "This parameter is set automatically during the calibration process. " +
                   "The parameter can be changed manually after the calibration.\n" +
                   "Available settings: 1-98 - percentage level of brightness.\n" +
                   "Default value: 1."

        input name: "param2", type: "number", range: "2..99", defaultValue: "99", required: true,
            title: "2. Maximum brightness level. " +
                   "This parameter is set automatically during the calibration process. " +
                   "The parameter can be changed manually after the calibration.\n" +
                   "Available settings: 2-99 - percentage level of brightness.\n" +
                   "Default value: 99."

        input name: "param3", type: "number", range: "1..99", defaultValue: "1", required: true,
            title: "3. Incandescence level of dimmable compact fluorescent lamps. " +
                   "Virtual value set as a percentage level between parameters MIN (1%) and MAX (99%). " +
                   "The Dimmer 2 will set to this value after first switch on. " +
                   "It is required for warming up and switching dimmable compact fluorescent lamps and certain types of light sources.\n" +
                   "Available settings: 1-99 - percentage level of brightness.\n" +
                   "Default value: 1."

        input name: "param4", type: "number", range: "0..255", defaultValue: "0", required: true,
            title: "4. Incandescence time of dimmable compact  fluorescent lamps. " +
                   "This parameter determines the time required for switching compact  fluorescent lamps and certain types of light sources. " +
                   "Setting this parameter to 0 will disable the incandescence functionality.\n" +
                   "Available settings: 0-255 (0-25.5s).\n" +
                   "Default value: 0."

        input name: "param5", type: "number", range: "1..99", defaultValue: "1", required: true,
            title: "5. Automatic control - dimming step size. " +
                   "This parameter defines the percentage value of dimming step during the automatic control.\n" +
                   "Available settings: 1-99 - dimming step percentage value.\n" +
                   "Default value: 1."

        input name: "param6", type: "number", range: "0..255", defaultValue: "1", required: true,
            title: "6. Automatic control - time of a dimming step. " +
                   "This parameter defines the time of single dimming step set in parameter 5 during the automatic control.\n" +
                   "Available settings: 0-255 (0-2.55s, in 10ms steps).\n" +
                   "Default value: 1."

        input name: "param7", type: "number", range: "1..99", defaultValue: "1", required: true,
            title: "7. Manual control - dimming step size. " +
                   "This parameter defines the percentage value of dimming step during the manual control.\n" +
                   "Available settings: 1-99 - dimming step percentage value.\n" +
                   "Default value: 1."

        input name: "param8", type: "number", range: "0..255", defaultValue: "5", required: true,
            title: "8. Manual control - time of a dimming step. " +
                   "This parameter defines the time of single dimming step set in parameter 7 during the manual control.\n" +
                   "Available settings: 0-255 (0-2.55s, in 10ms steps).\n" +
                   "Default value: 5."

        input name: "param9", type: "number", range: "0..1", defaultValue: "1", required: true,
            title: "9. State of the device after a power failure. " +
                   "The Dimmer 2 will return to the last state before power failure.\n" +
                   "Available settings:\n" +
                   "0 = the Dimmer 2 does not save the state before a power failure, it returns to „off” position,\n" +
                   "1 = the Dimmer 2 restores its state before power failure.\n" +
                   "Default value: 1."

        input name: "param10", type: "number", range: "0..32767", defaultValue: "0", required: true,
            title: "10. Timer functionality (auto - off). " +
                   "This parameter allows to automatically switch off the device after specified time from switching on the light source. " +
                   "It may be useful when the Dimmer 2 is installed in the stairway.\n" +
                   "Available settings: 0 - Function disabled,\n1-32767 - time to turn off measured in seconds (1s-9.1h).\n" +
                   "Default value: 0."

        input name: "param11", type: "enum", defaultValue: "255", required: true,
            options: ["0" : "0",
                      "1" : "1",
                      "2" : "2",
                      "255" : "255"],
            title: "11. ALL ON/ALL OFF function. " +
                   "Parameter allows for activation/deactivation of Z-Wave commands enabling/disabling all devices located in direct range of the main controller.\n" +
                   "Available settings:\n" +
                   "0 = All ON not active, All OFF not active,\n" +
                   "1 = All ON not active, All OFF active,\n" +
                   "2 = All ON active, All OFF not active,\n" +
                   "255 = All ON active, All OFF active.\n" +
                   "Default value: 255."

        input name: "param13", type: "number", range: "0..2", defaultValue: "0", required: true,
            title: "13. Force auto-calibration. " +
                   "Changing value of this parameter will force the calibration process. " +
                   "During the calibration parameter is set to 1 or 2 and switched to 0 upon completion.\n" +
                   "Available settings:\n" +
                   "0 = readout,\n" +
                   "1 = force auto-calibration of the load without FIBARO Bypass 2,\n" +
                   "2 = force auto-calibration of the load with FIBARO Bypass 2.\n" +
                   "Default value: 0."

        input name: "param15", type: "number", range: "0..99", defaultValue: "30", required: true,
            title: "15. Burnt out bulb detection. " +
                   "Function based on the sudden power variation of a specific value, interpreted as a LOAD ERROR.\n" +
                   "Available settings:\n0 - function disabled,\n" +
                   "1-99 - percentage value of power variation, compared to standard power consumption, measured during the calibration procedure (to be interpreted as load error/burnt out bulb).\n" +
                   "Default value: 30."

        input name: "param16", type: "number", range: "0..255", defaultValue: "5", required: true,
            title: "16. Time delay of a burnt out bulb (parameter 15) or overload (parameter 39) detection. " +
                   "Time of delay (in seconds) for power variation detection, interpreted as a LOAD ERROR or OVERLOAD detection (too much power connected to the Dimmer 2).\n" +
                   "Available settings:\n0 - detection of a burnt out bulb disabled,\n1-255 - delay time in seconds.\n" +
                   "Default value: 5."

        input name: "param19", type: "number", range: "0..99", defaultValue: "0", required: true,
            title: "19. Forced switch on brightness level. " +
                   "If the parameter is active, switching on the Dimmer 2 (S1 single click) will always set this brightness level.\n" +
                   "Available settings:\n0 - function disabled,\n1-99 - percentage level of brightness.\n" +
                   "Default value: 0."

        paragraph = "GROUP 20 - Dimmer 2 operation - Switches"
        input name: "param20", type: "number", range: "0..2", defaultValue: "0", required: true,
             title: paragraph + "\n\n" +
                    "20. Switch type. " +
                    "Choose between momentary, toggle and roller blind switch.\n" +
                    "Available settings:\n" +
                    "0 = momentary switch,\n1 = toggle switch,\n2 = roller blind switch - two switches operate the Dimmer 2 (S1 to brighten, S2 to dim).\n" +
                    "Default value: 0."

        input name: "param21", type: "number", range: "0..1", defaultValue: "0", required: true,
             title: "21. The value sent to associated devices on single click. " +
                    "This parameter defines the value sent to devices associated with Dimmer 2 after its enabling.\n" +
                    "Available settings:\n" +
                    "0 = 0xFF value is sent, which will set associated devices to their last saved state,\n" +
                    "1 = current Dimmer 2 state is sent, which will synchronize brightness level of associated devices (other dimmers for example).\n" +
                    "Default value: 0."

        input name: "param22", type: "number", range: "0..1", defaultValue: "0", required: true,
             title: "22. Assign toggle switch status to the device status. " +
                    "By default each change of toggle switch position results in action of Dimmer 2 (switch on/off) regardless the physical connection of contacts.\n" +
                    "Available settings:\n" +
                    "0 = device changes status on switch status change,\n1 = device status is synchronized with switch status.\n" +
                    "Default value: 0."

        input name: "param23", type: "number", range: "0..1", defaultValue: "1", required: true,
             title: "23. Double click option - set the brightness level to MAX.\n" +
                    "Available settings:\n" +
                    "0 = double click disabled,\n" +
                    "1 = double click enabled.\n" +
                    "Default value: 1."

        input name: "param24", type: "enum", defaultValue: "0", required: true,
             options: ["0" : "0",
                       "1" : "1",
                       "2" : "2",
                       "4" : "4",
                       "8" : "8",
                       "16" : "16"],
             title: "24. Command frames sent in 2nd and 3rd association groups (S1 associations). " +
                    "Parameter determines, which actions will not result in sending frames to association groups.\n" +
                    "Available settings:\n" +
                    "0 = all actions send to association groups,\n" +
                    "1 = do not send when switching ON (single click),\n" +
                    "2 = do not send when switching OFF (single click),\n" +
                    "4 = do not send when changing dimming level (holding and releasing),\n" +
                    "8 = do not send on double click,\n" +
                    "16 = send 0xFF value on double click.\n" +
                    "Default value: 0."

        input name: "param25", type: "enum", defaultValue: "0", required: true,
             options: ["0" : "0",
                       "1" : "1",
                       "2" : "2",
                       "4" : "4",
                       "8" : "8",
                       "16" : "16"],
             title: "25. Command frames sent in 4th and 5th association groups (S2 associations). " +
                    "Parameter determines, which actions will not result in sending frames to association groups.\n" +
                    "Available settings:\n" +
                    "0 = all actions send to association groups,\n" +
                    "1 = do not send when switching ON (single click),\n" +
                    "2 = do not send when switching OFF (single click),\n" +
                    "4 = do not send when changing dimming level (holding and releasing),\n" +
                    "8 = do not send on double click,\n" +
                    "16 = send 0xFF value on double click.\n" +
                    "Default value: 0."

        input name: "param26", type: "number", range: "0..1", defaultValue: "0", required: true,
             title: "26. The function of 3-way switch. " +
                    "Switch no. 2 controls the Dimmer 2 additionally (in 3-way switch mode). Function disabled for parameter 20 set to 2 (roller blind switch).\n" +
                    "Available settings:\n" +
                    "0 = 3-way switch function for S2 disabled,\n" +
                    "1 = 3-way switch function for S2 enabled.\n" +
                    "Default value: 0."

        input name: "param27", type: "enum", defaultValue: "15", required: true,
             options: ["0" : "0",
                       "1" : "1",
                       "2" : "2",
                       "4" : "4",
                       "8" : "8",
                       "15" : "15"],
             title: "27. Associations in Z-Wave network security mode. " +
                    "This parameter defines how commands are sent in speci ed association groups: as secure or non-secure. " +
                    "Parameter is active only in Z-Wave network security mode. It does not apply to 1st lifeline group.\n" +
                    "Available settings:\n" +
                    "0 = all groups (II-V) sent as non-secure,\n" +
                    "1 = 2nd group sent as secure,\n" +
                    "2 = 3rd group sent as secure,\n" +
                    "4 = 4th group sent as secure,\n" +
                    "8 = 5th group sent as secure,\n" +
                    "15 = all groups (II-V) sent as secure.\n" +
                    "Default value: 15."

        input name: "param28", type: "number", range: "0..1", defaultValue: "0", required: true,
             title: "28. Scene activation functionality.\n" +
                    "Available settings:\n" +
                    "0 = functionality deactivated,\n" +
                    "1 = functionality activated.\n" +
                    "Default value: 0.\n\n" +
                    "SCENE ID depends on the switch type configurations.\n" +
                    "Momentary switches:\n" +
                    "SCENE ID: S1 input:\n\t16 : 1 x click\n\t14 : 2 x click\n\t-- : 3 x click\n\t12 : hold\n\t13 : release\n" +
                    "SCENE ID: S2 input:\n\t26 : 1 x click\n\t24 : 2 x click\n\t25 : 3 x click\n\t22 : hold\n\t23 : release\n\n" +
                    "Toggle switches:\n" +
                    "SCENE ID: S1 input:\n\t10 : OFF to ON\n\t11 : ON to OFF\n\t14 : 2 x click\n\t-- : 3 x click\n" +
                    "SCENE ID: S2 input:\n\t20 : OFF to ON\n\t21 : ON to OFF\n\t24 : 2 x click\n\t25 : 3 x click\n\n" +
                    "Roller blinds switches:\n" +
                    "SCENE ID: S1 input:\n\t10 : turn ON (1 x click)\n\t13 : release\n\t14 : 2 x click\n\t-- : 3 x click\n\t17 : brightening\n" +
                    "SCENE ID: S2 input:\n\t11 : turn OFF (1 x click)\n\t13 : release\n\t14 : 2 x click\n\t15 : 3 x click\n\t18 : dimming"

        input name: "param29", type: "number", range: "0..1", defaultValue: "0", required: true,
             title: "29. Switch functionality of S1 and S2. " +
                    "This parameter allows for switching the role of keys connected to S1 and S2 without changes in connection.\n" +
                    "Available settings:\n" +
                    "0 = standard mode,\n" +
                    "1 = S1 operates as S2, S2 operates as S1.\n" +
                    "Default value: 0."

        paragraph = "GROUP 30 - Dimmer 2 operation - Advanced functionality"
        input name: "param30", type: "number", range: "0..2", defaultValue: "2", required: true,
             title: paragraph + "\n\n" +
                    "30. Load control mode. " +
                    "This parameter allows to set the desired load control mode. " +
                    "The de- vice automatically adjusts correct control mode, but the installer may force its change using this parameter. " +
                    "Forced auto-calibration will set this parameter’s value to 2.\n" +
                    "Available settings:\n" +
                    "0 = forced leading edge control,\n" +
                    "1 = forced trailing edge control,\n" +
                    "2 = control mode selected automatically (based on auto-calibration).\n" +
                    "Default value: 2."

        input name: "param32", type: "number", range: "0..2", defaultValue: "2", required: true,
             title: "32. On/Off mode. " +
                    "This mode is necessary while connecting non-dimmable light sources. " +
                    "Setting this parameter to 1 automatically ignores brightening/dimming time settings. " +
                    "Forced auto-calibration will set this parameter’s value to 2.\n" +
                    "Available settings:\n" +
                    "0 = on/off mode disabled (dimming is possible),\n" +
                    "1 = on/off mode enabled (dimming is not possible),\n" +
                    "2 = mode selected automatically.\n" +
                    "Default value: 2."

        input name: "param34", type: "number", range: "0..2", defaultValue: "1", required: true,
             title: "34. Soft-Start functionality. " +
                    "Time required to warm up the  lament of halogen bulb.\n" +
                    "Available settings:\n" +
                    "0 = no soft-start,\n" +
                    "1 = short soft-start (0.1s),\n" +
                    "2 = long soft-start (0.5s).\n" +
                    "Default value: 1."

        input name: "param35", type: "number", range: "0..4", defaultValue: "1", required: true,
             title: "35. Auto-calibration after power on. " +
                    "This parameter determines the trigger of auto-calibration procedure, e.g. power on, load error, etc.\n" +
                    "Available settings:\n" +
                    "0 = No auto-calibration of the load after power on,\n" +
                    "1 = Auto-calibration performed after first power on,\n" +
                    "2 = Auto-calibration performed after each power on,\n" +
                    "3 = Auto-calibration performed after first power on or after each LOAD ERROR alarm (no load, load failure, burnt out bulb), if parameter 37 is set to 1 also after alarms: SURGE (Dimmer 2 output overvoltage) and OVERCURRENT (Dimmer 2 output overcurrent),\n" +
                    "4 = Auto-calibration performed after each power on or after each LOAD ERROR alarm (no load, load failure, burnt out bulb), if parameter 37 is set to 1 also after alarms: SURGE (Dimmer 2 output overvoltage) and OVERCURRENT (Dimmer 2 output overcurrent).\n" +
                    "Default value: 1."

        input name: "param37", type: "number", range: "0..1", defaultValue: "1", required: true,
             title: "37. Behaviour of the Dimmer 2 after OVERCURRENT or SURGE. " +
                    "Occuring of errors related to surge or overcurrent results in turning off the output to prevent possible malfunction. " +
                    "By default the device performs three attempts to turn on the load (useful in case of momentary, short failures of the power supply).\n" +
                    "Available settings:\n" +
                    "0 = device permanently disabled until re-ena- bling by command or external switch,\n" +
                    "1 = three attempts to turn on the load.\n" +
                    "Default value: 1."

        input name: "param39", type: "number", range: "0..350", defaultValue: "250", required: true,
             title: "39. Power limit - OVERLOAD. " +
                    "Reaching the defined value will result in turning off the load. " +
                    "Additional apparent power limit of 350VA is active by default.\n" +
                    "Available settings:\n0 - functionality disabled,\n1-350 - 1-350W.\n" +
                    "Default value: 250."

        paragraph = "GROUP 40 - Dimmer 2 operation - Alarms"
        input name: "param40", type: "number", range: "0..3", defaultValue: "3", required: true,
             title: paragraph + "\n\n" +
                    "40. Response to General Purpose Alarm.\n" +
                    "Available settings:\n" +
                    "0 = No reaction,\n" +
                    "1 = Turn on the load,\n" +
                    "2 = Turn off the load,\n" +
                    "3 = Load blinking.\n" +
                    "Default value: 3."

        input name: "param41", type: "number", range: "0..3", defaultValue: "2", required: true,
             title: "41. Response to Water Flooding Alarm.\n" +
                    "Available settings:\n" +
                    "0 = No reaction,\n" +
                    "1 = Turn on the load,\n" +
                    "2 = Turn off the load,\n" +
                    "3 = Load blinking.\n" +
                    "Default value: 2."

        input name: "param42", type: "number", range: "0..3", defaultValue: "3", required: true,
             title: "42. Response to Smoke, CO or CO2 Alarm.\n" +
                    "Available settings:\n" +
                    "0 = No reaction,\n" +
                    "1 = Turn on the load,\n" +
                    "2 = Turn off the load,\n" +
                    "3 = Load blinking.\n" +
                    "Default value: 3."

        input name: "param43", type: "number", range: "0..3", defaultValue: "1", required: true,
             title: "43. Response to Temperature Alarm.\n" +
                    "Available settings:\n" +
                    "0 = No reaction,\n" +
                    "1 = Turn on the load,\n" +
                    "2 = Turn off the load,\n" +
                    "3 = Load blinking.\n" +
                    "Default value: 1."

        input name: "param44", type: "number", range: "1..32767", defaultValue: "600", required: true,
             title: "44. Time of alarm state.\n" +
                    "Available settings: 1-32767 (1-32767 seconds).\n" +
                    "Default value: 600."

        paragraph = "Alarm settings - reports"
        input name: "param45", type: "number", range: "0..1", defaultValue: "1", required: true,
             title: paragraph + "\n\n" +
                    "45. OVERLOAD alarm report (load power consumption too high).\n" +
                    "Available settings:\n" +
                    "0 = No reaction,\n" +
                    "1 = Send an alarm frame.\n" +
                    "Default value: 1."

        input name: "param46", type: "number", range: "0..1", defaultValue: "1", required: true,
             title: "46. LOAD ERROR alarm report (no load, load failure, burnt out bulb).\n" +
                    "Available settings:\n" +
                    "0 = No reaction,\n" +
                    "1 = Send an alarm frame.\n" +
                    "Default value: 1."

        input name: "param47", type: "number", range: "0..1", defaultValue: "1", required: true,
             title: "47. OVERCURRENT alarm report (short circuit, burnt out bulb causing overcurrent).\n" +
                    "Available settings:\n" +
                    "0 = No reaction,\n" +
                    "1 = Send an alarm frame.\n" +
                    "Default value: 1."

        input name: "param48", type: "number", range: "0..1", defaultValue: "1", required: true,
             title: "48. SURGE alarm report (Dimmer 2 output overvoltage).\n" +
                    "Available settings:\n" +
                    "0 = No reaction,\n" +
                    "1 = Send an alarm frame.\n" +
                    "Default value: 1."

        input name: "param49", type: "number", range: "0..1", defaultValue: "1", required: true,
             title: "49. OVERHEAT (critical temperature) and VOLTAGE DROP (low voltage) alarm report.\n" +
                    "Available settings:\n" +
                    "0 = No reaction,\n" +
                    "1 = Send an alarm frame.\n" +
                    "Default value: 1."

        paragraph = "GROUP 50 - Active power and energy reports"
        input name: "param50", type: "number", range: "0..100", defaultValue: "10", required: true,
             title: paragraph + "\n\n" +
                    "50. Active power reports. " +
                    "The parameter defines the power level change that will result in a new power report being sent. " +
                    "The value is a percentage of the previous report.\n" +
                    "Available settings:\n0 - power reports disabled,\n1-100 (1-100%) - power report threshold.\n" +
                    "Default value: 10."

        input name: "param52", type: "number", range: "0..32767", defaultValue: "3600", required: true,
             title: "52. Periodic active power and energy reports. " +
                    "Parameter 52 defines a time period between consecutive reports. Timer is reset and counted from zero after each report.\n" +
                    "Available settings:\n0 - periodic reports disabled,\n1-32767 (1-32767 seconds).\n" +
                    "Default value: 3600."

        input name: "param53", type: "number", range: "0..255", defaultValue: "10", required: true,
             title: "53. Energy reports. " +
                    "Energy level change which will result in sending a new energy report.\n" +
                    "Available settings:\n0 - energy reports disabled,\n1-255 (0.01-2.55 kWh) - report triggering threshold.\n" +
                    "Default value: 10."

        input name: "param54", type: "number", range: "0..1", defaultValue: "0", required: true,
             title: "54. Self-measurement. " +
                    "The Dimmer 2 may include active power and energy consumed by itself in reports sent to the main controller.\n" +
                    "Available settings:\n" +
                    "0 = Self-measurement inactive,\n" +
                    "1 = Self-measurement active.\n" +
                    "Default value: 0."

        input name: "param58", type: "number", range: "0..2", defaultValue: "0", required: true,
             title: "58. Method of calculating the active power. This parameter defines how to calculate active power. " +
                    "It is useful in a case of 2-wire connection with light sources other than resistive.\n" +
                    "Available settings:\n" +
                    "0 = measurement based on the standard algorithm,\n" +
                    "1 = approximation based on the calibration data,\n" +
                    "2 = approximation based on the control angle.\n" +
                    "Default value: 0."

        input name: "param59", type: "number", range: "0..500", defaultValue: "0", required: true,
             title: "59. Approximated power at the maximum brightness level. " +
                    "This parameter determines the approximate value of the power that will be reported by the device at its maximum brightness level.\n" +
                    "Available settings: 0-500 (0-500W) - power consumed by the load at the maximum brightness level.\n" +
                    "Default value: 0."
        
        input name: "paramAssociationGroup1", type: "bool", defaultValue: true, required: true,
             title: "The Dimmer 2 provides the association of five groups.\n\n" +
                    "1st Association Group „Lifeline”,\n" +
                    "Default value: true"
                    
        input name: "paramAssociationGroup2", type: "bool", defaultValue: true, required: true,
             title: "2nd Association Group „On/Off (S1)”,\n" +
                    "Default value: true"
                    
        input name: "paramAssociationGroup3", type: "bool", defaultValue: false, required: true,
             title: "3rd Association Group „Dimmer (S1)”,\n" +
                    "Default value: false"
                    
        input name: "paramAssociationGroup4", type: "bool", defaultValue: false, required: true,
             title: "4th Association Group „On/Off (S2)”,\n" +
                    "Default value: false"
                    
        input name: "paramAssociationGroup5", type: "bool", defaultValue: false, required: true,
             title: "5th Association Group „Dimmer (S2)”.\n" +
                    "Default value: false"
    }
}

def parse(String description) {
	log.trace(description)
    log.debug("RAW command: $description")
	def result = null
    
    if (description != "updated") {
		def cmd = zwave.parse(description.replace("98C1", "9881"), [0x20: 1, 0x26: 3, 0x32: 3, 0x25: 1, 0x98: 1, 0x70: 1, 0x85: 2, 0x9B: 1, 0x90: 1, 0x73: 1, 0x30: 1, 0x28: 1, 0x72: 1])
		if (cmd) {
			result = zwaveEvent(cmd)
		}
    }
    log.debug "Parsed '${description}' to ${result.inspect()}"
	return result
}

def zwaveEvent(physicalgraph.zwave.commands.sceneactivationv1.SceneActivationSet cmd) {
	log.debug( "Scene ID: $cmd.sceneId")
    log.debug( "Dimming Duration: $cmd.dimmingDuration")
    
    sendEvent(name: "scene", value: "$cmd.sceneId", data: [switchType: "$settings.param20"], descriptionText: "Scene id $cmd.sceneId was activated", isStateChange: true)
    log.debug( "Scene id $cmd.sceneId was activated" )
}

// Devices that support the Security command class can send messages in an encrypted form;
// they arrive wrapped in a SecurityMessageEncapsulation command and must be unencapsulated
def zwaveEvent(physicalgraph.zwave.commands.securityv1.SecurityMessageEncapsulation cmd) {
	log.trace(cmd)
	def encapsulatedCommand = cmd.encapsulatedCommand([0x20: 1, 0x26: 3, 0x32: 3, 0x25: 1, 0x98: 1, 0x70: 1, 0x85: 2, 0x9B: 1, 0x90: 1, 0x73: 1, 0x30: 1, 0x28: 1, 0x72: 1]) // can specify command class versions here like in zwave.parse
	if (encapsulatedCommand) {
		return zwaveEvent(encapsulatedCommand)
	} else {
		log.warn "Unable to extract encapsulated cmd from $cmd"
		createEvent(descriptionText: cmd.toString())
	}
}

def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicReport cmd) {
	log.trace(cmd)
	dimmerEvents(cmd)
}

def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicSet cmd) {
	log.trace(cmd)
	//dimmerEvents(cmd)
}
def zwaveEvent(physicalgraph.zwave.commands.switchmultilevelv3.SwitchMultilevelReport cmd) {
	log.trace(cmd)
	dimmerEvents(cmd)
}

def zwaveEvent(physicalgraph.zwave.commands.switchbinaryv1.SwitchBinaryReport cmd)
{
	log.trace(cmd)
	dimmerEvents(cmd)
}


def dimmerEvents(physicalgraph.zwave.Command cmd) {
	log.trace(cmd)
	def result = []
	def value = (cmd.value ? "on" : "off")
	def switchEvent = createEvent(name: "switch", value: value, descriptionText: "$device.displayName was turned $value")
	result << switchEvent
	if (cmd.value) {
		result << createEvent(name: "level", value: cmd.value, unit: "%")
	}
	if (switchEvent.isStateChange) {
		result << response(["delay 3000", zwave.meterV2.meterGet(scale: 2).format()])
	}
	return result
}

def zwaveEvent(physicalgraph.zwave.commands.meterv3.MeterReport cmd) {
	log.trace(cmd)
	if (cmd.meterType == 1) {
		if (cmd.scale == 0) {
			return createEvent(name: "energy", value: cmd.scaledMeterValue, unit: "kWh")
		} else if (cmd.scale == 1) {
			return createEvent(name: "energy", value: cmd.scaledMeterValue, unit: "kVAh")
		} else if (cmd.scale == 2) {
			return createEvent(name: "power", value: Math.round(cmd.scaledMeterValue), unit: "W")
		} else {
			return createEvent(name: "electric", value: cmd.scaledMeterValue, unit: ["pulses", "V", "A", "R/Z", ""][cmd.scale - 3])
		}
	}
}

def zwaveEvent(physicalgraph.zwave.Command cmd) {
	log.trace(cmd)
	log.debug "No handler for $cmd"
	// Handles all Z-Wave commands we aren't interested in
	createEvent(descriptionText: cmd.toString(), isStateChange: false)
}


def on() {
	log.trace("on")
	secureSequence([
			zwave.basicV1.basicSet(value: 0xFF),
            zwave.switchMultilevelV1.switchMultilevelGet()
	])
}

def off() {
	log.trace("off")
	secureSequence([
			zwave.basicV1.basicSet(value: 0x00),
            zwave.switchMultilevelV1.switchMultilevelGet()
	])
}

def poll() {
	log.trace("poll")
	secureSequence([
		zwave.meterV2.meterGet(scale: 0),
		zwave.meterV2.meterGet(scale: 2)
	])
}

def refresh() {
	log.trace("trace")
	secureSequence([
		zwave.meterV2.meterGet(scale: 0),
		zwave.meterV2.meterGet(scale: 2)
	])
}

def reset() {
	log.trace("reset")
	return secureSequence([
    	zwave.switchMultilevelV1.switchMultilevelGet(),
		zwave.meterV2.meterReset(),
		zwave.meterV2.meterGet(scale: 0),
        zwave.meterV2.meterGet(scale: 2)
	])
}

def setLevel(level) {
	log.trace("setlevel")
	if(level > 99) level = 99
	secureSequence([
		zwave.basicV1.basicSet(value: level),
		zwave.switchMultilevelV1.switchMultilevelGet()
	], 5000)
}

def configureAfterSecure() {
    log.debug "configureAfterSecure()"
        def cmds = secureSequence([
            zwave.configurationV1.configurationSet(parameterNumber: 1, size: 1, scaledConfigurationValue: param1.toInteger()),
            zwave.configurationV1.configurationSet(parameterNumber: 2, size: 1, scaledConfigurationValue: param2.toInteger()),
            zwave.configurationV1.configurationSet(parameterNumber: 3, size: 1, scaledConfigurationValue: param3.toInteger()),
            zwave.configurationV1.configurationSet(parameterNumber: 4, size: 2, scaledConfigurationValue: param4.toInteger()),
            zwave.configurationV1.configurationSet(parameterNumber: 5, size: 1, scaledConfigurationValue: param5.toInteger()),
            zwave.configurationV1.configurationSet(parameterNumber: 6, size: 2, scaledConfigurationValue: param6.toInteger()),
            zwave.configurationV1.configurationSet(parameterNumber: 7, size: 1, scaledConfigurationValue: param7.toInteger()),
            zwave.configurationV1.configurationSet(parameterNumber: 8, size: 2, scaledConfigurationValue: param8.toInteger()),
            zwave.configurationV1.configurationSet(parameterNumber: 9, size: 1, scaledConfigurationValue: param9.toInteger()),
            zwave.configurationV1.configurationSet(parameterNumber: 10, size: 2, scaledConfigurationValue: param10.toInteger()),
            zwave.configurationV1.configurationSet(parameterNumber: 11, size: 2, scaledConfigurationValue: param11.toInteger()),
            zwave.configurationV1.configurationSet(parameterNumber: 13, size: 1, scaledConfigurationValue: param13.toInteger()),
            zwave.configurationV1.configurationSet(parameterNumber: 15, size: 1, scaledConfigurationValue: param15.toInteger()),
            zwave.configurationV1.configurationSet(parameterNumber: 16, size: 2, scaledConfigurationValue: param16.toInteger()),
            zwave.configurationV1.configurationSet(parameterNumber: 19, size: 1, scaledConfigurationValue: param19.toInteger()),
            zwave.configurationV1.configurationSet(parameterNumber: 20, size: 1, scaledConfigurationValue: param20.toInteger()),
            zwave.configurationV1.configurationSet(parameterNumber: 21, size: 1, scaledConfigurationValue: param21.toInteger()),
            zwave.configurationV1.configurationSet(parameterNumber: 22, size: 1, scaledConfigurationValue: param22.toInteger()),
            zwave.configurationV1.configurationSet(parameterNumber: 23, size: 1, scaledConfigurationValue: param23.toInteger()),
            zwave.configurationV1.configurationSet(parameterNumber: 24, size: 1, scaledConfigurationValue: param24.toInteger()),
            zwave.configurationV1.configurationSet(parameterNumber: 25, size: 1, scaledConfigurationValue: param25.toInteger()),
            zwave.configurationV1.configurationSet(parameterNumber: 26, size: 1, scaledConfigurationValue: param26.toInteger()),
            zwave.configurationV1.configurationSet(parameterNumber: 27, size: 1, scaledConfigurationValue: param27.toInteger()),
            zwave.configurationV1.configurationSet(parameterNumber: 28, size: 1, scaledConfigurationValue: param28.toInteger()),
            zwave.configurationV1.configurationSet(parameterNumber: 29, size: 1, scaledConfigurationValue: param29.toInteger()),
            zwave.configurationV1.configurationSet(parameterNumber: 30, size: 1, scaledConfigurationValue: param30.toInteger()),
            zwave.configurationV1.configurationSet(parameterNumber: 32, size: 1, scaledConfigurationValue: param32.toInteger()),
            zwave.configurationV1.configurationSet(parameterNumber: 34, size: 1, scaledConfigurationValue: param34.toInteger()),
            zwave.configurationV1.configurationSet(parameterNumber: 35, size: 1, scaledConfigurationValue: param35.toInteger()),
            zwave.configurationV1.configurationSet(parameterNumber: 37, size: 1, scaledConfigurationValue: param37.toInteger()),
            zwave.configurationV1.configurationSet(parameterNumber: 39, size: 2, scaledConfigurationValue: param39.toInteger()),
            zwave.configurationV1.configurationSet(parameterNumber: 40, size: 1, scaledConfigurationValue: param40.toInteger()),
            zwave.configurationV1.configurationSet(parameterNumber: 41, size: 1, scaledConfigurationValue: param41.toInteger()),
            zwave.configurationV1.configurationSet(parameterNumber: 42, size: 1, scaledConfigurationValue: param42.toInteger()),
            zwave.configurationV1.configurationSet(parameterNumber: 43, size: 1, scaledConfigurationValue: param43.toInteger()),
            zwave.configurationV1.configurationSet(parameterNumber: 44, size: 2, scaledConfigurationValue: param44.toInteger()),
            zwave.configurationV1.configurationSet(parameterNumber: 45, size: 1, scaledConfigurationValue: param45.toInteger()),
            zwave.configurationV1.configurationSet(parameterNumber: 46, size: 1, scaledConfigurationValue: param46.toInteger()),
            zwave.configurationV1.configurationSet(parameterNumber: 47, size: 1, scaledConfigurationValue: param47.toInteger()),
            zwave.configurationV1.configurationSet(parameterNumber: 48, size: 1, scaledConfigurationValue: param48.toInteger()),
            zwave.configurationV1.configurationSet(parameterNumber: 49, size: 1, scaledConfigurationValue: param49.toInteger()),
            zwave.configurationV1.configurationSet(parameterNumber: 50, size: 1, scaledConfigurationValue: param50.toInteger()),
            zwave.configurationV1.configurationSet(parameterNumber: 52, size: 2, scaledConfigurationValue: param52.toInteger()),
            zwave.configurationV1.configurationSet(parameterNumber: 53, size: 2, scaledConfigurationValue: param53.toInteger()),
            zwave.configurationV1.configurationSet(parameterNumber: 54, size: 1, scaledConfigurationValue: param54.toInteger()),
            zwave.configurationV1.configurationSet(parameterNumber: 58, size: 1, scaledConfigurationValue: param58.toInteger()),
            zwave.configurationV1.configurationSet(parameterNumber: 59, size: 2, scaledConfigurationValue: param59.toInteger())
        ])
        
        // Register for Group 1
        if(paramAssociationGroup1) {
        	cmds << secure(zwave.associationV2.associationSet(groupingIdentifier:1, nodeId: [zwaveHubNodeId]))
        }
        else {
        	cmds << secure(zwave.associationV2.associationRemove(groupingIdentifier:1, nodeId: [zwaveHubNodeId]))
        }
        // Register for Group 2
        if(paramAssociationGroup2) {
        	cmds << secure(zwave.associationV2.associationSet(groupingIdentifier:2, nodeId: [zwaveHubNodeId]))
        }
        else {
        	cmds << secure(zwave.associationV2.associationRemove(groupingIdentifier:2, nodeId: [zwaveHubNodeId]))
        }
        // Register for Group 3
        if(paramAssociationGroup3) {
        	cmds << secure(zwave.associationV2.associationSet(groupingIdentifier:3, nodeId: [zwaveHubNodeId]))
        }
        else {
        	cmds << secure(zwave.associationV2.associationRemove(groupingIdentifier:3, nodeId: [zwaveHubNodeId]))
        }
        // Register for Group 4
        if(paramAssociationGroup4) {
        	cmds << secure(zwave.associationV2.associationSet(groupingIdentifier:4, nodeId: [zwaveHubNodeId]))
        }
        else {
        	cmds << secure(zwave.associationV2.associationRemove(groupingIdentifier:4, nodeId: [zwaveHubNodeId]))
        }
        // Register for Group 5
        if(paramAssociationGroups5) {
        	cmds << secure(zwave.associationV2.associationSet(groupingIdentifier:5, nodeId: [zwaveHubNodeId]))
        }
        else {
        	cmds << secure(zwave.associationV2.associationRemove(groupingIdentifier:5, nodeId: [zwaveHubNodeId]))
        }
        
	cmds
}

def configure() {
	// Wait until after the secure exchange for this
    log.debug "configure()"
}

def updated() {
	log.debug "updated()"
	response(["delay 2000"] + configureAfterSecure() + refresh())
}

private secure(physicalgraph.zwave.Command cmd) {
	log.trace(cmd)
	zwave.securityV1.securityMessageEncapsulation().encapsulate(cmd).format()
}

private secureSequence(commands, delay=200) {
	log.debug "$commands"
	delayBetween(commands.collect{ secure(it) }, delay)
}