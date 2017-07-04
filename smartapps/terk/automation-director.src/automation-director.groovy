/**
 *  Copyright 2016  by Terk
 *
 *	Automation Director version 1.6
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *	  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 *  Version History:
 *  1.6 Added the ability to have the Audio Notification (AEON Labs Doorbell) play only once in a time period for door openings or any other trigger, this has been tested with contact sensors, light switches, motion, presence, and illuminance so far
 *  1.5 Beta
 *		Added the ability to turn a light on via a switch on, motion active, contact open, or button pushed/held and have it turn off after a specified amount of time
 *			If a light was already on before the event happened the light will remain on after the timer period finishes,
 *				except if there was still motion when the timer finishes then it will wait for the specified amount of time again
 *				and when there is no longer motion it will turn off all of the lights it turned on which are set to turn off after a certain amount of time
 *			If a light is turned off and back on during the timer period the light will remain on after the timer period finishes
 *			If you add multiple motion sensors with the same lights then motion must not be detected on any of the motion sensors for the lights to be turned off
 *		Corrected a motion restriction setting
 *		Added the ability to select multiple sensors instead of just one for each requirement in the restriction setting, if you select multiple contacts they all must be in the required state
 *		Added the AllPresent option for presence and beacon sensors
 *  1.4 Fixed button held
 *  1.3 Added the ability to disable the instance of the SmartApp
 *  1.2 Added additional requirement options
 *  1.1 The smartapp now only asks for the numberofbuttons and if it supports holdableButton if the information isn't provided by the DTH
 *  1.0 Initial release
 */

definition(
	name: "Automation Director",
	namespace: "Terk",
	author: "Terk",
	description: "Allows most triggers to be set to effect almost anything",
	category: "My Apps",
	iconUrl: "https://s3.amazonaws.com/smartthings-device-icons/Entertainment/entertainment1-icn@2x.png",
	iconX2Url: "https://s3.amazonaws.com/smartthings-device-icons/Entertainment/entertainment1-icn@3x.png",
)

preferences {
	page name: "Triggers", title: "Choose what will trigger the actions", install: false, uninstall: true, nextPage: "Actions"
	page name: "Actions", title: "Choose the actions to make available", install: false, uninstall: true, nextPage: "Assignments"
	page name: "Assignments", title: "Assign the actions to the triggers", install: false, uninstall: true, nextPage: "NametheApp"
	page name: "NametheApp", title: "Name the SmartApp", install: true, uninstall: true
	page(name: "certainTime")
	page(name: "SensorLabels")
	page(name: "PresenceLabels")
	page(name: "SafetyLabels")
	page(name: "EnergyLabels")
	page(name: "ControllerLabels")
	page(name: "OtherLabels")
	page(name: "AlarmLabels")
	page(name: "NotificationLabels")
	page(name: "DoorLockLabels")
	page(name: "ImageLabels")
	page(name: "SwitchOutletRelayLabels")
}

def installed() {
	log.debug "Installed with settings: ${settings}"
	initialize()
}

def updated() {
	log.debug "Updated with settings: ${settings}"
	unsubscribe()
	initialize()
}

def initialize() {
	if (!overrideLabel) {
		app.updateLabel("Automation Director")
	}
	if (Button) {
		subscribe(Button, "button.pushed", DoButtonPushed)
		subscribe(Button, "button.held", DoButtonHeld)
	}
	if (Contact) {
		subscribe(Contact, "contact.open", DoContactOpen)
		subscribe(Contact, "contact.closed", DoContactClosed)
	}
	if (Switch) {
		subscribe(Switch, "switch.on", DoSwitchOn)
		subscribe(Switch, "switch.off", DoSwitchOff)
	}
	if (Acceleration) {
		subscribe(Acceleration, "acceleration.active", DoAccelerationActive)
		subscribe(Acceleration, "acceleration.inactive", DoAccelerationInactive)
	}
	if (Motion) {
		subscribe(Motion, "motion.active", DoMotionActive)
		subscribe(Motion, "motion.inactive", DoMotionInactive)
	}
	if (Tamper) {
		subscribe(Tamper, "tamper.clear", DoTamperClear)
		subscribe(Tamper, "tamper.detected", DoTamperDetected)
	}
	if (Shock) {
		subscribe(Shock, "shock.clear", DoShockClear)
		subscribe(Shock, "shock.detected", DoShockDetected)
	}
	if (Sleep) {
		subscribe(Sleep, "sleeping.not sleeping", DoSleepNotSleeping)
		subscribe(Sleep, "sleeping.sleeping", DoSleepSleeping)
	}
	if (Sound) {
		subscribe(Sound, "sound.detected", DoSoundDetected)
		subscribe(Sound, "sound.not detected", DoSoundNotDetected)
	}
	if (Touch) {
		subscribe(Touch, "touch.touched", DoTouched)
	}
	if (Water) {
		subscribe(Water, "water.dry", DoWaterDry)
		subscribe(Water, "water.wet", DoWaterWet)
	}
	if (Beacon) {
		subscribe(Beacon, "presence.not present", DoBeaconNotPresent)
		subscribe(Beacon, "presence.present", DoBeaconPresent)
	}
	if (Presence) {
		subscribe(Presence, "presence.not present", DoPresenceNotPresent)
		subscribe(Presence, "presence.present", DoPresencePresent)
	}
	if (CODetector) {
		subscribe(CODetector, "carbonMonoxide.clear", DoCODetectorClear)
		subscribe(CODetector, "carbonMonoxide.detected", DoCODetectorDetected)
		subscribe(CODetector, "carbonMonoxide.tested", DoCODetectorTested)
	}
	if (Smoke) {
		subscribe(Smoke, "smoke.clear", DoSmokeClear)
		subscribe(Smoke, "smoke.detected", DoSmokeDetected)
		subscribe(Smoke, "smoke.tested", DoSmokeTested)
	}
	if (PowerSource) {
		subscribe(PowerSource, "powerSource.battery", DoPowerSourceBattery)
		subscribe(PowerSource, "powerSource.dc", DoPowerSourceDC)
		subscribe(PowerSource, "powerSource.mains", DoPowerSourceMains)
		subscribe(PowerSource, "powerSource.unknown", DoPowerSourceUnknown)
	}
	if (Door) {
		subscribe(Door, "door.closed", DoDoorClosed)
		subscribe(Door, "door.open", DoDoorOpen)
		subscribe(Door, "door.unknown", DoDoorUnknown)
	}
	if (Valve) {
		subscribe(Valve, "contact.closed", DoValveClosed)
		subscribe(Valve, "contact.open", DoValveOpen)
	}
	if (Shade) {
		subscribe(Shade, "windowShade.closed", DoShadeClosed)
		subscribe(Shade, "windowShade.open", DoShadeOpen)
		subscribe(Shade, "windowShade.partially open", DoShadePartiallyOpen)
		subscribe(Shade, "windowShade.unknown", DoShadeUnknown)
	}
	if (Temperature) {subscribe(Temperature, "temperature", DoTemperature)}
	if (PowerMeter) {subscribe(PowerMeter, "power", DoPowerMeter)}
	if (Battery) {subscribe(Battery, "battery", DoBattery)}
	if (Voltage) {subscribe(Voltage, "voltage", DoVoltage)}
	if (EnergyMeter) {subscribe(EnergyMeter, "energy", DoEnergyMeter)}
	if (CO2Measurement) {subscribe(CO2Measurement, "carbonDioxide", DoCO2Measurement)}
	if (Step) {
		subscribe(Step, "steps", DoStep)
		if (StepGoal) {subscribe(Step, "goal", DoStepGoal)}
	}
	if (Illuminance) {subscribe(Illuminance, "illuminance", DoIlluminance)}
	if (Humidity) {subscribe(Humidity, "humidity", DoHumidity)}
	if (UltravioletIndex) {subscribe(UltravioletIndex, "ultravioletIndex", DoUltravioletIndex)}
	if (phMeasurement) {subscribe(phMeasurement, "pH", DophMeasurement)}
	if (soundPressureLevel) {subscribe(soundPressureLevel, "soundPressureLevel", DosoundPressureLevel)}
}

def Triggers() {
	dynamicPage(name: "Triggers") {
		section() {
			input name: "InstanceEnabled", title: "Enable this SmartApp instance", type: "bool", required: false, defaultValue: true
		}
		section(title: "Button Triggers...", hidden: hideButtonTriggers(), hideable: true) {
			input "Button", "capability.button", title: "Button(s)", required: false, submitOnChange: true, multiple: true
		}
		section(title: "Sensor Triggers...", hidden: hideSensorTriggers(), hideable: true) {
			def SensorLabel = SensorTriggerLabel()
			href "SensorLabels", title: "Sensor triggers", description: SensorLabel ?: "Tap to set", state: SensorLabel ? "complete" : null
		}
		section(title: "Presence Triggers...", hidden: hidePresenceTriggers(), hideable: true) {
			def PresenceLabel = PresenceTriggerLabel()
			href "PresenceLabels", title: "Presence triggers", description: PresenceLabel ?: "Tap to set", state: PresenceLabel ? "complete" : null
		}
		section(title: "Safety Triggers...", hidden: hideSafetyTriggers(), hideable: true) {
			def SafetyLabel = SafetyTriggerLabel()
			href "SafetyLabels", title: "Safety triggers", description: SafetyLabel ?: "Tap to set", state: SafetyLabel ? "complete" : null
		}
		section(title: "Energy Triggers...", hidden: hideEnergyTriggers(), hideable: true) {
			def EnergyLabel = EnergyTriggerLabel()
			href "EnergyLabels", title: "Energy triggers", description: EnergyLabel ?: "Tap to set", state: EnergyLabel ? "complete" : null
		}
		section(title: "Switch,Door,Illuminance conroller Triggers...", hidden: hideSwitchDoorTriggers(), hideable: true) {
			def ControllerLabel = ControllerTriggerLabel()
			href "ControllerLabels", title: "Switch,Door,Illuminance triggers", description: ControllerLabel ?: "Tap to set", state: ControllerLabel ? "complete" : null
		}
		section(title: "Temp,Humidity,UV,ph,valve,shade Triggers...", hidden: hideOtherTriggers(), hideable: true) {
			def OtherLabel = OtherTriggerLabel()
			href "OtherLabels", title: "Other triggers", description: OtherLabel ?: "Tap to set", state: OtherLabel ? "complete" : null
		}
		section(title: "Restrict when to run...", hidden: hideOptionsSection(), hideable: true) {
			def timeLabel = timeIntervalLabel()
			href "certainTime", title: "Only during a certain time", description: timeLabel ?: "Tap to set", state: timeLabel ? "complete" : null
			input "days", "enum", title: "Only on certain days of the week", multiple: true, required: false, options: ["Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"]
			input "modes", "mode", title: "Only when mode is", multiple: true, required: false
			input "Illuminance_Required", "capability.illuminanceMeasurement", title: "Illuminance sensor above or below threshold", required: false, submitOnChange: true, multiple: true
			if (Illuminance_Required) {
				def OptionalText = ""
				if (Illuminance_Required.size() > 1) {OptionalText = "  When more than one sensor is selected they all must be in the state."}
				input "Illuminance_RequiredGreaterThan", "number", title: "Only when lux is >=${OptionalText}", required: false, range: "1..*", submitOnChange: true
				input "Illuminance_RequiredLessThan", "number", title: "Only when lux is <=${OptionalText}", required: false, range: "1..*", submitOnChange: true
				paragraph "Current lux ${Illuminance_Required.currentValue("illuminance")} would meet the requirements : ${CurrentIlluminance()}"
			}
			input "Contact_Required", "capability.contactSensor", title: "Only when a contact is in a certain state", required: false, submitOnChange: true, multiple: true
			if (Contact_Required) {
				def OptionalText = ""
				if (Contact_Required.size() > 1) {OptionalText = "  When more than one sensor is selected they all must be in the state."}
				input "Contact_state", "enum", title: "Which state?${OptionalText}", required: true, options: ["Open", "Closed"]
			}
			input "Switch_Required", "capability.switch", title: "Only when a switch is in a certain state", required: false, submitOnChange: true, multiple: true
			if (Switch_Required) {
				def OptionalText = ""
				if (Switch_Required.size() > 1) {OptionalText = "  When more than one sensor is selected they all must be in the state."}
				input "Switch_state", "enum", title: "Which state?${OptionalText}", required: true, options: ["On", "Off"]
			}
			input "Acceleration_Required", "capability.accelerationSensor", title: "Only when an acceleration sensor is in a certain state", required: false, submitOnChange: true, multiple: true
			if (Acceleration_Required) {
				def OptionalText = ""
				if (Acceleration_Required.size() > 1) {OptionalText = "  When more than one sensor is selected they all must be in the state."}
				input "Acceleration_state", "enum", title: "Which state?${OptionalText}", required: true, options: ["Active", "Inactive"]
			}
			input "Motion_Required", "capability.motionSensor", title: "Only when a motion sensor is in a certain state", required: false, submitOnChange: true, multiple: true
			if (Motion_Required) {
				def OptionalText = ""
				if (Motion_Required.size() > 1) {OptionalText = "  When more than one sensor is selected they all must be in the state."}
				input "Motion_state", "enum", title: "Which state?${OptionalText}", required: true, options: ["Active", "Inactive"]
			}
			input "Tamper_Required", "capability.tamperAlert", title: "Only when a tamper sensor is in a certain state", required: false, submitOnChange: true, multiple: true
			if (Tamper_Required) {
				def OptionalText = ""
				if (Tamper_Required.size() > 1) {OptionalText = "  When more than one sensor is selected they all must be in the state."}
				input "Tamper_state", "enum", title: "Which state?${OptionalText}", required: true, options: ["Clear", "Detected"]
			}
			input "Shock_Required", "capability.shockSensor", title: "Only when a shock sensor is in a certain state", required: false, submitOnChange: true, multiple: true
			if (Shock_Required) {
				def OptionalText = ""
				if (Shock_Required.size() > 1) {OptionalText = "  When more than one sensor is selected they all must be in the state."}
				input "Shock_state", "enum", title: "Which state?${OptionalText}", required: true, options: ["Clear", "Detected"]
			}
			input "Sleep_Required", "capability.sleepSensor", title: "Only when a sleep sensor is in a certain state", required: false, submitOnChange: true, multiple: true
			if (Sleep_Required) {
				def OptionalText = ""
				if (Sleep_Required.size() > 1) {OptionalText = "  When more than one sensor is selected they all must be in the state."}
				input "Sleep_state", "enum", title: "Which state?${OptionalText}", required: true, options: ["Not sleeping", "Sleeping"]
			}
			input "Sound_Required", "capability.soundSensor", title: "Only when a sound sensor is in a certain state", required: false, submitOnChange: true, multiple: true
			if (Sound_Required) {
				def OptionalText = ""
				if (Sound_Required.size() > 1) {OptionalText = "  When more than one sensor is selected they all must be in the state."}
				input "Sound_state", "enum", title: "Which state?${OptionalText}", required: true, options: ["Detected", "Not detected"]
			}
			input "Water_Required", "capability.waterSensor", title: "Only when a water sensor is in a certain state", required: false, submitOnChange: true, multiple: true
			if (Water_Required) {
				def OptionalText = ""
				if (Water_Required.size() > 1) {OptionalText = "  When more than one sensor is selected they all must be in the state."}
				input "Water_state", "enum", title: "Which state?${OptionalText}", required: true, options: ["Dry", "Wet"]
			}
			input "Beacon_Required", "capability.beacon", title: "Only when a beacon sensor is in a certain state", required: false, submitOnChange: true, multiple: true
			if (Beacon_Required) {
				def OptionalText = ""
				if (Beacon_Required.size() > 1) {OptionalText = "  When more than one sensor is selected they all must be in the state."}
				input "Beacon_state", "enum", title: "Which state?${OptionalText}", required: true, options: ["Not present", "Present"]
			}
			input "Presence_Required", "capability.presenceSensor", title: "Only when a presence sensor is in a certain state", required: false, submitOnChange: true, multiple: true
			if (Presence_Required) {
				def OptionalText = ""
				if (Presence_Required.size() > 1) {OptionalText = "  When more than one sensor is selected they all must be in the state."}
				input "Presence_state", "enum", title: "Which state?${OptionalText}", required: true, options: ["Not present", "Present"]
			}
			input "CODetector_Required", "capability.carbonMonoxideDetector", title: "Only when a CODetector sensor is in a certain state", required: false, submitOnChange: true, multiple: true
			if (CODetector_Required) {
				def OptionalText = ""
				if (CODetector_Required.size() > 1) {OptionalText = "  When more than one sensor is selected they all must be in the state."}
				input "CODetector_state", "enum", title: "Which state?${OptionalText}", required: true, options: ["Clear", "Detected", "Tested"]
			}
			input "Smoke_Required", "capability.smokeDetector", title: "Only when a smoke sensor is in a certain state", required: false, submitOnChange: true, multiple: true
			if (Smoke_Required) {
				def OptionalText = ""
				if (Smoke_Required.size() > 1) {OptionalText = "  When more than one sensor is selected they all must be in the state."}
				input "Smoke_state", "enum", title: "Which state?${OptionalText}", required: true, options: ["Clear", "Detected", "Tested"]
			}
			input "PowerSource_Required", "capability.powerSource", title: "Only when a PowerSource sensor is in a certain state", required: false, submitOnChange: true, multiple: true
			if (PowerSource_Required) {
				def OptionalText = ""
				if (PowerSource_Required.size() > 1) {OptionalText = "  When more than one sensor is selected they all must be in the state."}
				input "PowerSource_state", "enum", title: "Which state?${OptionalText}", required: true, options: ["Battery", "DC", "Mains", "Unknown"]
			}
			input "Door_Required", "capability.doorControl", title: "Only when an door sensor is in a certain state", required: false, submitOnChange: true, multiple: true
			if (Door_Required) {
				def OptionalText = ""
				if (Door_Required.size() > 1) {OptionalText = "  When more than one sensor is selected they all must be in the state."}
				input "Door_state", "enum", title: "Which state?${OptionalText}", required: true, options: ["Closed", "Open", "Unknown"]
			}
			input "Valve_Required", "capability.valve", title: "Only when an valve sensor is in a certain state", required: false, submitOnChange: true, multiple: true
			if (Valve_Required) {
				def OptionalText = ""
				if (Valve_Required.size() > 1) {OptionalText = "  When more than one sensor is selected they all must be in the state."}
				input "Valve_state", "enum", title: "Which state?${OptionalText}", required: true, options: ["Closed", "Open"]
			}
			input "Shade_Required", "capability.windowShade", title: "Only when an shade sensor is in a certain state", required: false, submitOnChange: true, multiple: true
			if (Shade_Required) {
				def OptionalText = ""
				if (Shade_Required.size() > 1) {OptionalText = "  When more than one sensor is selected they all must be in the state."}
				input "Shade_state", "enum", title: "Which state?${OptionalText}", required: true, options: ["Closed", "Open", "Partially open", "Unknown"]
			}
			input "Temperature_Required", "capability.temperatureMeasurement", title: "Temperature sensor above or below threshold", required: false, submitOnChange: true, multiple: true
			if (Temperature_Required) {
				def OptionalText = ""
				if (Temperature_Required.size() > 1) {OptionalText = "  When more than one sensor is selected they all must be in the state."}
				input "Temperature_RequiredGreaterThan", "number", title: "Only when temp is >=${OptionalText}", required: false, range: "1..*"
				input "Temperature_RequiredLessThan", "number", title: "Only when temp is <=${OptionalText}", required: false, range: "1..*"
			}
			input "PowerMeter_Required", "capability.powerMeter", title: "PowerMeter sensor above or below threshold", required: false, submitOnChange: true, multiple: true
			if (PowerMeter_Required) {
				def OptionalText = ""
				if (PowerMeter_Required.size() > 1) {OptionalText = "  When more than one sensor is selected they all must be in the state."}
				input "PowerMeter_RequiredGreaterThan", "number", title: "Only when PowerMeter is >=${OptionalText}", required: false, range: "1..*"
				input "PowerMeter_RequiredLessThan", "number", title: "Only when PowerMeter is <=${OptionalText}", required: false, range: "1..*"
			}
			input "Voltage_Required", "capability.voltageMeasurement", title: "Voltage sensor above or below threshold", required: false, submitOnChange: true, multiple: true
			if (Voltage_Required) {
				def OptionalText = ""
				if (Voltage_Required.size() > 1) {OptionalText = "  When more than one sensor is selected they all must be in the state."}
				input "Voltage_RequiredGreaterThan", "number", title: "Only when Voltage is >=${OptionalText}", required: false, range: "1..*"
				input "Voltage_RequiredLessThan", "number", title: "Only when Voltage is <=${OptionalText}", required: false, range: "1..*"
			}
			input "EnergyMeter_Required", "capability.energyMeter", title: "EnergyMeter sensor above or below threshold", required: false, submitOnChange: true, multiple: true
			if (EnergyMeter_Required) {
				def OptionalText = ""
				if (EnergyMeter_Required.size() > 1) {OptionalText = "  When more than one sensor is selected they all must be in the state."}
				input "EnergyMeter_RequiredGreaterThan", "number", title: "Only when EnergyMeter is >=${OptionalText}", required: false, range: "1..*"
				input "EnergyMeter_RequiredLessThan", "number", title: "Only when EnergyMeter is <=${OptionalText}", required: false, range: "1..*"
			}
			input "CO2Measurement_Required", "capability.carbonDioxideMeasurement", title: "CO2Measurement sensor above or below threshold", required: false, submitOnChange: true, multiple: true
			if (CO2Measurement_Required) {
				def OptionalText = ""
				if (CO2Measurement_Required.size() > 1) {OptionalText = "  When more than one sensor is selected they all must be in the state."}
				input "CO2Measurement_RequiredGreaterThan", "number", title: "Only when CO2Measurement is >=${OptionalText}", required: false, range: "1..*"
				input "CO2Measurement_RequiredLessThan", "number", title: "Only when CO2Measurement is <=${OptionalText}", required: false, range: "1..*"
			}
			input "Humidity_Required", "capability.relativeHumidityMeasurement", title: "Humidity sensor above or below threshold", required: false, submitOnChange: true, multiple: true
			if (Humidity_Required) {
				def OptionalText = ""
				if (Humidity_Required.size() > 1) {OptionalText = "  When more than one sensor is selected they all must be in the state."}
				input "Humidity_RequiredGreaterThan", "number", title: "Only when Humidity is >=${OptionalText}", required: false, range: "1..*"
				input "Humidity_RequiredLessThan", "number", title: "Only when Humidity is <=${OptionalText}", required: false, range: "1..*"
			}
			input "UltravioletIndex_Required", "capability.ultravioletIndex", title: "UltravioletIndex sensor above or below threshold", required: false, submitOnChange: true, multiple: true
			if (UltravioletIndex_Required) {
				def OptionalText = ""
				if (UltravioletIndex_Required.size() > 1) {OptionalText = "  When more than one sensor is selected they all must be in the state."}
				input "UltravioletIndex_RequiredGreaterThan", "number", title: "Only when UltravioletIndex is >=${OptionalText}", required: false, range: "1..*"
				input "UltravioletIndex_RequiredLessThan", "number", title: "Only when UltravioletIndex is <=${OptionalText}", required: false, range: "1..*"
			}
			input "phMeasurement_Required", "capability.phMeasurement", title: "phMeasurement sensor above or below threshold", required: false, submitOnChange: true, multiple: true
			if (phMeasurement_Required) {
				def OptionalText = ""
				if (phMeasurement_Required.size() > 1) {OptionalText = "  When more than one sensor is selected they all must be in the state."}
				input "phMeasurement_RequiredGreaterThan", "number", title: "Only when phMeasurement is >=${OptionalText}", required: false, range: "1..*"
				input "phMeasurement_RequiredLessThan", "number", title: "Only when phMeasurement is <=${OptionalText}", required: false, range: "1..*"
			}
			input "soundPressureLevel_Required", "capability.soundPressureLevel", title: "soundPressureLevel sensor above or below threshold", required: false, submitOnChange: true, multiple: true
			if (soundPressureLevel_Required) {
				def OptionalText = ""
				if (soundPressureLevel_Required.size() > 1) {OptionalText = "  When more than one sensor is selected they all must be in the state."}
				input "soundPressureLevel_RequiredGreaterThan", "number", title: "Only when soundPressureLevel is >=${OptionalText}", required: false, range: "1..*"
				input "soundPressureLevel_RequiredLessThan", "number", title: "Only when soundPressureLevel is <=${OptionalText}", required: false, range: "1..*"
			}
		}
	}
}

def certainTime() {
	dynamicPage(name:"certainTime",title: "Only during a certain time", uninstall: false) {
		section() {
			input "startingX", "enum", title: "Starting at", options: ["A specific time", "Sunrise", "Sunset"], submitOnChange: true, required: false
			if(startingX in [null, "A specific time"]) input "starting", "time", title: "Start time", required: false
			else if(startingX == "Sunrise") input "startSunriseOffset", "number", range: "*..*", title: "Offset in minutes (+/-)", required: false
			else input "startSunsetOffset", "number", range: "*..*", title: "Offset in minutes (+/-)", required: false
		}
		
		section() {
			input "endingX", "enum", title: "Ending at", options: ["A specific time", "Sunrise", "Sunset"], submitOnChange: true, required: false
			if(endingX in [null, "A specific time"]) input "ending", "time", title: "End time", required: false
			else if(endingX == "Sunrise") input "endSunriseOffset", "number", range: "*..*", title: "Offset in minutes (+/-)", required: false
			else input "endSunsetOffset", "number", range: "*..*", title: "Offset in minutes (+/-)", required: false
		}
	}
}

def SensorLabels() {
	dynamicPage(name:"SensorLabels",title: "Sensor to use as trigger...", uninstall: false) {
		section(title: "Acceleration Triggers...", hidden: hideAccelerationTriggers(), hideable: true) {
			input "Acceleration", "capability.accelerationSensor", title: "Acceleration sensor(s)", required: false, submitOnChange: true, multiple: true
		}
		section(title: "Contact Triggers...", hidden: hideContactTriggers(), hideable: true) {
			input "Contact", "capability.contactSensor", title: "Contact sensor(s)", required: false, submitOnChange: true, multiple: true
		}
		section(title: "Motion Triggers...", hidden: hideMotionTriggers(), hideable: true) {
			input "Motion", "capability.motionSensor", title: "Motion sensor(s)", required: false, submitOnChange: true, multiple: true
		}
		section(title: "Tamper Triggers...", hidden: hideTamperTriggers(), hideable: true) {
			input "Tamper", "capability.tamperAlert", title: "Tamper sensor(s)", required: false, submitOnChange: true, multiple: true
		}
		section(title: "Shock Triggers...", hidden: hideShockTriggers(), hideable: true) {
			input "Shock", "capability.shockSensor", title: "Shock sensor(s)", required: false, submitOnChange: true, multiple: true
		}
		section(title: "Sleep Triggers...", hidden: hideSleepTriggers(), hideable: true) {
			input "Sleep", "capability.sleepSensor", title: "Sleep sensor(s)", required: false, submitOnChange: true, multiple: true
		}
		section(title: "Sound Triggers...", hidden: hideSoundTriggers(), hideable: true) {
			input "Sound", "capability.soundSensor", title: "Sound sensor(s)", required: false, submitOnChange: true, multiple: true
		}
		section(title: "Touch Triggers...", hidden: hideTouchTriggers(), hideable: true) {
			input "Touch", "capability.touchSensor", title: "Touch sensor(s)", required: false, submitOnChange: true, multiple: true
		}
		section(title: "Step Triggers...", hidden: hideStepTriggers(), hideable: true) {
			input "Step", "capability.stepSensor", title: "Step sensor(s)", required: false, submitOnChange: true, multiple: true
			if (Step) {
				input name: "IndividualStep", title: "Set individual thresholds", type: "bool", required: false, submitOnChange: true
				if (IndividualStep) {
						for ( def i = 0; i < Step.size(); i++) {
							input "StepHighThreshold${Step[i].id}", "number", title: "${Step[i]} step high threshold", required: false, range: "1..*"
							input "StepLowThreshold${Step[i].id}", "number", title: "${Step[i]} step low threshold", required: false, range: "1..*"
							input "StepGoal${Step[i].id}", "number", title: "${Step[i]} step goal", required: false, range: "1..*"
						}
					} else {
						input "AllStepHighThreshold", "number", title: "All step sensor high threshold", required: false, range: "1..*"
						input "AllStepLowThreshold", "number", title: "All step sensor low threshold", required: false, range: "1..*"
						input "AllStepGoal", "number", title: "All step sensor goal", required: false, range: "1..*"
				}
			}
		}
	}
}

def PresenceLabels() {
	dynamicPage(name:"PresenceLabels",title: "Presence to use as trigger...", uninstall: false) {
		section(title: "Beacon Triggers...", hidden: hideBeaconTriggers(), hideable: true) {
			input "Beacon", "capability.beacon", title: "Beacon sensor(s)", required: false, submitOnChange: true, multiple: true
		}
		section(title: "Presence Triggers...", hidden: hideindPresenceTriggers(), hideable: true) {
			input "Presence", "capability.presenceSensor", title: "Presence sensor(s)", required: false, submitOnChange: true, multiple: true
		}
	}
}

def SafetyLabels() {
	dynamicPage(name:"SafetyLabels",title: "Safety device to use as trigger...", uninstall: false) {
		section(title: "CO2 Measurement Triggers...", hidden: hideCO2MeasurementTriggers(), hideable: true) {
			input "CO2Measurement", "capability.carbonDioxideMeasurement", title: "Carbon Dioxide measurement sensor(s)", required: false, submitOnChange: true, multiple: true
			if (CO2Measurement) {
				input name: "IndividualCO2Measurement", title: "Set individual thresholds", type: "bool", required: false, submitOnChange: true
				if (IndividualCO2Measurement) {
						for ( def i = 0; i < CO2Measurement.size(); i++) {input "CO2MeasurementHighThreshold${CO2Measurement[i].id}", "number", title: "${CO2Measurement[i]} high CO2 threshold", required: false, range: "1..*"}
 					} else {
						input "AllCO2MeasurementHighThreshold", "number", title: "All high CO2 threshold", required: false, range: "1..*"
				}
		   }
		}
		section(title: "CO Detector Triggers...", hidden: hideCODetectorTriggers(), hideable: true) {
			input "CODetector", "capability.carbonMonoxideDetector", title: "Carbon monoxide sensor(s)", required: false, submitOnChange: true, multiple: true
		}
		section(title: "Smoke Triggers...", hidden: hideSmokeTriggers(), hideable: true) {
			input "Smoke", "capability.smokeDetector", title: "Smoke detector(s)", required: false, submitOnChange: true, multiple: true
		}
		section(title: "Sound Pressure Level Triggers...", hidden: hidesoundPressureLevelTriggers(), hideable: true) {
			input "soundPressureLevel", "capability.soundPressureLevel", title: "soundPressureLevel sensor(s)", required: false, submitOnChange: true, multiple: true
			if (soundPressureLevel) {
				input name: "IndividualsoundPressureLevel", title: "Set individual thresholds", type: "bool", required: false, submitOnChange: true
				if (IndividualsoundPressureLevel) {
						for ( def i = 0; i < soundPressureLevel.size(); i++) {
							input "soundPressureLevelHighThreshold${soundPressureLevel[i].id}", "number", title: "${soundPressureLevel[i]} sound pressure level high threshold", required: false, range: "1..*"
							input "soundPressureLevelLowThreshold${soundPressureLevel[i].id}", "number", title: "${soundPressureLevel[i]} sound pressure level low threshold", required: false, range: "1..*"
						}
					} else {
						input "AllsoundPressureLevelHighThreshold", "number", title: "All sound pressure level high threshold", required: false, range: "1..*"
						input "AllsoundPressureLevelLowThreshold", "number", title: "All sound pressure level low threshold", required: false, range: "1..*"
				}
			}
		}
		section(title: "Water Triggers...", hidden: hideWaterTriggers(), hideable: true) {
			input "Water", "capability.waterSensor", title: "Water sensor(s)", required: false, submitOnChange: true, multiple: true
		}
	}
}

def EnergyLabels() {
	dynamicPage(name:"EnergyLabels",title: "Energy device to use as trigger...", uninstall: false) {
		section(title: "EnergyMeter Triggers...", hidden: hideEnergyMeterTriggers(), hideable: true) {
			input "EnergyMeter", "capability.energyMeter", title: "Energy meter(s)", required: false, submitOnChange: true, multiple: true
			if (EnergyMeter) {
				input name: "IndividualEnergyMeter", title: "Set individual thresholds", type: "bool", required: false, submitOnChange: true
				if (IndividualEnergyMeter) {
						for ( def i = 0; i < EnergyMeter.size(); i++) {
							input "EnergyMeterHighThreshold${EnergyMeter[i].id}", "number", title: "${EnergyMeter[i]} high energy usage threshold", required: false, range: "1..*"
							input "EnergyMeterLowThreshold${EnergyMeter[i].id}", "number", title: "${EnergyMeter[i]} low energy usage threshold", required: false, range: "1..*"
						}
					} else {
						input "AllEnergyMeterHighThreshold", "number", title: "Any high energy usage threshold", required: false, range: "1..*"
						input "AllEnergyMeterLowThreshold", "number", title: "Any low energy usage threshold", required: false, range: "1..*"
				}
			}
		}
		section(title: "PowerMeter Triggers...", hidden: hidePowerMeterTriggers(), hideable: true) {
			input "PowerMeter", "capability.powerMeter", title: "Power meter(s)", required: false, submitOnChange: true, multiple: true
			if (PowerMeter) {
				input name: "IndividualPowerMeter", title: "Set individual thresholds", type: "bool", required: false, submitOnChange: true
				if (IndividualPowerMeter) {
						for ( def i = 0; i < PowerMeter.size(); i++) {
							input "PowerMeterHighThreshold${PowerMeter[i].id}", "number", title: "${PowerMeter[i]} high power usage threshold", required: false, range: "1..*"
							input "PowerMeterLowThreshold${PowerMeter[i].id}", "number", title: "${PowerMeter[i]} low power usage threshold", required: false, range: "1..*"
						}
					} else {
						input "AllPowerMeterHighThreshold", "number", title: "Any high power usage threshold", required: false, range: "1..*"
						input "AllPowerMeterLowThreshold", "number", title: "Any low power usage threshold", required: false, range: "1..*"
				}
			}
		}
		section(title: "PowerSource Triggers...", hidden: hidePowerSourceTriggers(), hideable: true) {
			input "PowerSource", "capability.powerSource", title: "Power source(s)", required: false, submitOnChange: true, multiple: true
		}
		section(title: "Voltage Triggers...", hidden: hideVoltageTriggers(), hideable: true) {
			input "Voltage", "capability.voltageMeasurement", title: "Voltage sensor(s)", required: false, submitOnChange: true, multiple: true
			if (Voltage) {
				input name: "IndividualVoltage", title: "Set individual thresholds", type: "bool", required: false, submitOnChange: true
				if (IndividualVoltage) {
						for ( def i = 0; i < Voltage.size(); i++) {
							input "VoltageHighThreshold${Voltage[i].id}", "number", title: "${Voltage[i]} high voltage threshold", required: false, range: "1..*"
							input "VoltageLowThreshold${Voltage[i].id}", "number", title: "${Voltage[i]} low voltage threshold", required: false, range: "1..*"
						}
					} else {
						input "AllVoltageHighThreshold", "number", title: "Any high voltage threshold", required: false, range: "1..*"
						input "AllVoltageLowThreshold", "number", title: "Any low voltage threshold", required: false, range: "1..*"
				}
			}
		}
		section(title: "Battery Triggers...", hidden: hideBatteryTriggers(), hideable: true) {
			input "Battery", "capability.battery", title: "Battery(s)", required: false, submitOnChange: true, multiple: true
			if (Battery) {
				input name: "SendBatteryPush", title: "Send a Push message when threshold crossed", type: "bool", required: false
				input name: "IndividualBattery", title: "Set individual thresholds", type: "bool", required: false, submitOnChange: true
				if (IndividualBattery) {
						for ( def i = 0; i < Battery.size(); i++) {input "BatteryLowThreshold${Battery[i].id}", "number", title: "${Battery[i]} low battery threshold", required: false, range: "1..100"}
					} else {
						input "AllBatteryLowThreshold", "number", title: "Any battery low threshold", required: false, range: "1..100"
				}
				input name: "SendBatterySMS", title: "Send a SMS message when threshold crossed", type: "bool", required: false, submitOnChange: true
				if (SendBatterySMS) {
					input "SendBatterySMSPhone", "text", title: "Please enter the phone number(s) to use seperated by ;", required: true
				}
			}
		}
	}
}

def ControllerLabels() {
	dynamicPage(name:"ControllerLabels",title: "Controller to use as trigger...", uninstall: false) {
		section(title: "Switch Triggers...", hidden: hideSwitchTriggers(), hideable: true) {
			input "Switch", "capability.switch", title: "Switch(s) or light(s)", required: false, submitOnChange: true, multiple: true
		}
		section(title: "Illuminance Triggers...", hidden: hideIlluminanceTriggers(), hideable: true) {
			input "Illuminance", "capability.illuminanceMeasurement", title: "Illuminance sensor(s)", required: false, submitOnChange: true, multiple: true
			if (Illuminance) {
				input name: "IndividualIlluminance", title: "Set individual thresholds", type: "bool", required: false, submitOnChange: true
				if (IndividualIlluminance) {
						for ( def i = 0; i < Illuminance.size(); i++) {
							input "IlluminanceHighThreshold${Illuminance[i].id}", "number", title: "${Illuminance[i]} Illuminance high threshold (lux)", required: false, range: "1..*"
							input "IlluminanceLowThreshold${Illuminance[i].id}", "number", title: "${Illuminance[i]} Illuminance low threshold (lux)", required: false, range: "1..*"
						}
					} else {
						input "AllIlluminanceHighThreshold", "number", title: "All Illuminance sensor high threshold (lux)", required: false, range: "1..*"
						input "AllIlluminanceLowThreshold", "number", title: "All Illuminance sensor low threshold (lux)", required: false, range: "1..*"
				}
			}
		}
		section(title: "Door Triggers...", hidden: hideDoorTriggers(), hideable: true) {
			input "Door", "capability.doorControl", title: "Door control sensor(s)", required: false, submitOnChange: true, multiple: true
		}
	}
}

def OtherLabels() {
	dynamicPage(name:"OtherLabels",title: "Other device to use as trigger...", uninstall: false) {
		section(title: "Temperature Triggers...", hidden: hideTemperatureTriggers(), hideable: true) {
			input "Temperature", "capability.temperatureMeasurement", title: "Temperature sensor(s)", required: false, submitOnChange: true, multiple: true
			if (Temperature) {
				input name: "IndividualTemperature", title: "Set individual thresholds", type: "bool", required: false, submitOnChange: true
				if (IndividualTemperature) {
						for ( def i = 0; i < Temperature.size(); i++) {
							input "HighThreshold${Temperature[i].id}", "number", title: "${Temperature[i]} high temperature threshold", required: false, range: "*..*"
							input "LowThreshold${Temperature[i].id}", "number", title: "${Temperature[i]} low temperature threshold", required: false, range: "*..*"
						}
					} else {
						input "AllHighThreshold", "number", title: "All temperature sensor high threshold", required: false, range: "*..*"
						input "AllLowThreshold", "number", title: "All temperature sensor low threshold", required: false, range: "*..*"
				}
			}
		}
		section(title: "Humidity Triggers...", hidden: hideHumidityTriggers(), hideable: true) {
			input "Humidity", "capability.relativeHumidityMeasurement", title: "Humidity sensor(s)", required: false, submitOnChange: true, multiple: true
			if (Humidity) {
				input name: "IndividualHumidity", title: "Set individual thresholds", type: "bool", required: false, submitOnChange: true
				if (IndividualHumidity) {
						for ( def i = 0; i < Humidity.size(); i++) {
							input "HumidityHighThreshold${Humidity[i].id}", "number", title: "${Humidity[i]} humidity high threshold", required: false, range: "1..*"
							input "HumidityLowThreshold${Humidity[i].id}", "number", title: "${Humidity[i]} humidity low threshold", required: false, range: "1..*"
						}
					} else {
						input "AllHumidityHighThreshold", "number", title: "All humidity sensor high threshold", required: false, range: "1..*"
						input "AllHumidityLowThreshold", "number", title: "All humidity sensor low threshold", required: false, range: "1..*"
				}
			}
		}
		section(title: "UV Index Triggers...", hidden: hideUltravioletIndexTriggers(), hideable: true) {
			input "UltravioletIndex", "capability.ultravioletIndex", title: "Ultraviolet index sensor(s)", required: false, submitOnChange: true, multiple: true
			if (UltravioletIndex) {
				input name: "IndividualUltravioletIndex", title: "Set individual thresholds", type: "bool", required: false, submitOnChange: true
				if (IndividualUltravioletIndex) {
						for ( def i = 0; i < UltravioletIndex.size(); i++) {
							input "UltravioletIndexHighThreshold${UltravioletIndex[i].id}", "number", title: "${UltravioletIndex[i]} ultraviolet index high threshold", required: false, range: "1..*"
							input "UltravioletIndexLowThreshold${UltravioletIndex[i].id}", "number", title: "${UltravioletIndex[i]} ultraviolet index low threshold", required: false, range: "1..*"
						}
					} else {
						input "AllUltravioletIndexHighThreshold", "number", title: "All ultraviolet index sensor high threshold", required: false, range: "1..*"
						input "AllUltravioletIndexLowThreshold", "number", title: "All ultraviolet index sensor low threshold", required: false, range: "1..*"
				}
			}
		}
		section(title: "ph Measurement Triggers...", hidden: hidephMeasurementTriggers(), hideable: true) {
			input "phMeasurement", "capability.phMeasurement", title: "ph sensor(s)", required: false, submitOnChange: true, multiple: true
			if (phMeasurement) {
				input name: "IndividualphMeasurement", title: "Set individual thresholds", type: "bool", required: false, submitOnChange: true
				if (IndividualphMeasurement) {
						for ( def i = 0; i < phMeasurement.size(); i++) {
							input "phMeasurementHighThreshold${phMeasurement[i].id}", "number", title: "${phMeasurement[i]} ph high threshold", required: false, range: "*..*"
							input "phMeasurementLowThreshold${phMeasurement[i].id}", "number", title: "${phMeasurement[i]} ph low threshold", required: false, range: "*..*"
						}
					} else {
						input "AllphMeasurementHighThreshold", "number", title: "All ph sensor high threshold", required: false, range: "*..*"
						input "AllphMeasurementLowThreshold", "number", title: "All ph sensor low threshold", required: false, range: "*..*"
				}
			}
		}
		section(title: "Valve Triggers...", hidden: hideValveTriggers(), hideable: true) {
			input "Valve", "capability.valve", title: "Valve(s)", required: false, submitOnChange: true, multiple: true
		}
		section(title: "Shade Triggers...", hidden: hideShadeTriggers(), hideable: true) {
			input "Shade", "capability.windowShade", title: "Window shade(s)", required: false, submitOnChange: true, multiple: true
		}
	}
}

def Actions() {
	dynamicPage(name: "Actions") {
		section(title: "Alarm related options...", hidden: hideAllAlarmOptions(), hideable: true) {
			def AlarmLabel = AlarmActionsLabel()
			href "AlarmLabels", title: "Alarm actions", description: AlarmLabel ?: "Tap to set", state: AlarmLabel ? "complete" : null
		}
		section(title: "Notification options...", hidden: hideAllNotificationOptions(), hideable: true) {
			def NotificationLabel = NotificationActionsLabel()
			href "NotificationLabels", title: "Notification actions", description: NotificationLabel ?: "Tap to set", state: NotificationLabel ? "complete" : null
		}
		section(title: "Door and Lock options...", hidden: hideAllDoorLockOptions(), hideable: true) {
			def DoorLockLabel = DoorLockActionsLabel()
			href "DoorLockLabels", title: "Door & Lock actions", description: DoorLockLabel ?: "Tap to set", state: DoorLockLabel ? "complete" : null
		}
		section(title: "Still image options...", hidden: hideAllImageOptions(), hideable: true) {
			def ImageLabel = ImageActionsLabel()
			href "ImageLabels", title: "Image actions", description: ImageLabel ?: "Tap to set", state: ImageLabel ? "complete" : null
		}
		section(title: "Switch, outlet, & relay options...", hidden: hideAllSwitchOptions(), hideable: true) {
			def SwitchOutletRelayLabel = SwitchOutletRelayActionsLabel()
			href "SwitchOutletRelayLabels", title: "Switch, Outlet, & Relay actions", description: SwitchOutletRelayLabel ?: "Tap to set", state: SwitchOutletRelayLabel ? "complete" : null
		}
		section(title: "Water valve options...", hidden: hideValveOptions(), hideable: true) {
			input "Valves", "capability.valve", title: "Which Valve(s)?", multiple: true, required: false, submitOnChange: true
		}
		section(title: "Shade options...", hidden: hideShadeOptions(), hideable: true) {
			input "Shades", "capability.windowShade", title: "Which Shade(s)?", multiple: true, required: false, submitOnChange: true
		}
		section(title: "Speaker options...", hidden: hideSpeakerOptions(), hideable: true) {
			input "Speakers", "capability.musicPlayer", title: "Which Speaker(s)?", multiple: true, required: false, submitOnChange: true
			if (Speakers) {
				input "volumeOffset", "enum", title: "Speakers Volume Increment", options: [[5:"5%"],[4:"4%"],[3:"3%"],[2:"2%"],[1:"1%"]], defaultValue: "2"
			}
		}
	}
}

def AlarmLabels() {
	dynamicPage(name:"AlarmLabels",title: "Alarm actions...", uninstall: false) {
		section(title: "Change Modes...", hidden: hideChangeModesActions(), hideable: true) {
			input name: "ChangeModes", title: "Make mode changes available", type: "bool", required: false, submitOnChange: true
		}
		section(title: "Siren Actions...", hidden: hideSirensActions(), hideable: true) {
			input "Sirens", "capability.alarm", title: "Which Siren(s)?", multiple: true, required: false, submitOnChange: true
			if (Sirens) {
				paragraph "Note : Not all sirens differentiate between siren/strobe and will do both."
				input name: "Notifications", title: "Send push notification on activation", type: "bool", required: false, submitOnChange: true
				if (Notifications) {
					input name: "CustomPush", title: "Customize the message", type: "bool", required: false, submitOnChange: true
					if (CustomPush) {input "PushMessage", "text", title: "Please enter the message", required: true}
				}
				input name: "SMS", title: "Send SMS notification on activation", type: "bool", required: false, submitOnChange: true
				if (SMS) {
					input "PhoneNumber", "text", title: "Please enter the phone number(s) to use seperated by ;", required: true
					input name: "CustomSMS", title: "Customize the message", type: "bool", required: false, submitOnChange: true
					if (CustomSMS) {input "SMSMessage", "text", title: "Please enter the message", required: true}
				}
			}
		}
	}
}

def NotificationLabels() {
	dynamicPage(name:"NotificationLabels",title: "Notification actions...", uninstall: false) {
		section(title: "Push Notify Actions...", hidden: hidePushNotifyActions(), hideable: true) {
			input name: "PushNotify", title: "Send push notification", type: "bool", required: false, submitOnChange: true
			if (PushNotify) {
				paragraph "Deselect all Push notification actions assigned, before making changes."
				input "PushTextNumber", "number", title: "How many push messages do you want available?", submitOnChange: true
				for ( def i = 1; i <= PushTextNumber; i++) {
					input "PushTitle$i", "text", title: "Enter a name for the message", required: true, submitOnChange: true
					if (settings["PushTitle$i"]) {
						input "PushText${settings["PushTitle$i"].replaceAll(' ','')}", "text", title: "Enter the body of ${settings["PushTitle$i"]}", required: true
					}
				}
			}
		}
		section(title: "SMS Notify Actions...", hidden: hideSMSNotifyActions(), hideable: true) {
			input name: "SMSNotify", title: "Send SMS notification", type: "bool", required: false, submitOnChange: true
			if (SMSNotify) {
				paragraph "Deselect all SMS notification actions assigned, before making changes."
 				input "SMSTextNumber", "number", title: "How many SMS messages do you want available?", submitOnChange: true
				for ( def i = 1; i <= SMSTextNumber; i++) {
					input "SMSTitle$i", "text", title: "Enter a name for the message", required: true, submitOnChange: true
					if (settings["SMSTitle$i"]) {
						input "SMSText${settings["SMSTitle$i"].replaceAll(' ','')}", "text", title: "Enter the body of ${settings["SMSTitle$i"]}", required: true
						input "SMSPhoneNumber${settings["SMSTitle$i"].replaceAll(' ','')}", "text", title: "Enter the phone number(s) to use seperated by ;", required: true
					}
				}
			}
		}
		section(title: "Audio Notification Actions...", hidden: hideAudioNotifyActions(), hideable: true) {
			input "AudioNotify", "capability.audioNotification", title: "Which Audio Notification(s) (Doorbells)?", multiple: true, required: false, submitOnChange: true
			if (AudioNotify) {
				input name: "SpecificNotification", title: "Make a specific track available", type: "bool", required: false, submitOnChange: true
				input name: "NotifyOnce", title: "Only notify one time in a given period", type: "bool", required: false, submitOnChange: true
				if (NotifyOnce) {
					input name: "NotifyOnceinSeconds", title: "How many seconds before another ntification can be sent", type: "number", required: true, range: "1..86400"
				}
				if (SpecificNotification) {
					input name: "NumberofTracks", title: "How Many tracks do you have on your doorbell", type: "number", required: true, range: "1..99"
					paragraph "DTH https://community.smartthings.com/t/release-aeon-labs-aeotec-doorbell/39166 required for the following:"
					input name: "AudioNotifyAtVolume", title: "Specify volume", type: "bool", required: false, submitOnChange: true
					if (AudioNotifyAtVolume) {input "AudioNotifyVolume", "number", title: "Specify the volume", required: true, range: "1..10"}
					//input name: "AudioNotifyRepeat", title: "Repeat", type: "bool", required: false, submitOnChange: true
					//if (AudioNotifyRepeat) {input "AudioNotifyRepeatCount", "number", title: "How many times should it repeat?", required: true, range: "1..*"}
				}
			}
		}
	}
}

def DoorLockLabels() {
	dynamicPage(name:"DoorLockLabels",title: "Door & Lock actions...", uninstall: false) {
		section(title: "Garage Door Controller Actions...", hidden: hideGDCsActions(), hideable: true) {
			input "GDCs", "capability.garageDoorControl", title: "Which Garage Door Controller(s)?", multiple: true, required: false, submitOnChange: true
		}
		section(title: "Lock Actions...", hidden: hideLocksActions(), hideable: true) {
			input "Locks", "capability.lock", title: "Which Lock(s)?", multiple: true, required: false, submitOnChange: true
		}
		section(title: "Door Controller Actions...", hidden: hideDoorControlsActions(), hideable: true) {
			input "DoorControls", "capability.doorControl", title: "Which Door Controller(s)?", multiple: true, required: false, submitOnChange: true
		}
	}
}

def ImageLabels() {
	dynamicPage(name:"ImageLabels",title: "Image actions...", uninstall: false) {
		section() {
			input "Images", "capability.imageCapture", title: "Which Still Shot Image device(s)?", multiple: true, required: false, submitOnChange: true
			if (Images) {
				input name: "Burst", title: "Take a series of photos", type: "bool", required: false, submitOnChange: true
				if (Burst) {input "ImageNumber", "number", title: "How many photos should be taken consecutively?", required: false, range: "1..*"}
			}
		}
	}
}

def SwitchOutletRelayLabels() {
	dynamicPage(name:"SwitchOutletRelayLabels",title: "Switch, Outlet, & Relay actions...", uninstall: false) {
		section(title: "Bulb Actions...", hidden: hideBulbsActions(), hideable: true) {
			input "Bulbs", "capability.colorControl", title: "Which Corlor changing Bulb(s)?", multiple: true, required: false, submitOnChange: true
			if (Bulbs) {
				input name: "ColorAvailable", title: "Make colors available", type: "bool", required: false, submitOnChange: true
				input name: "CustomColor", title: "Input a custom color", type: "bool", required: false, submitOnChange: true
				if (CustomColor) {
					input "CustomHue", "number", title: "Input the custom hue (0-100)" , defaultValue: "0", range: "0..100"
					input "CustomSaturation", "number", title: "Input the custom saturation (0-100)" , defaultValue: "91", range: "0..100"
				}
				input "BulbsFav", "number", title: "Favorite bulb Level", defaultValue: "50", range: "0..100"
				boolean TempCapable = false
				boolean TempCapableReset = false
				Bulbs.each {
					TempCapableReset = false
					for ( def i = 0; i < it.capabilities.size(); i++) {
						if (it.capabilities[i].name as String == "Color Temperature") {
							TempCapable = true
							TempCapableReset = true
						}
					}
					if (CustomColor) {
						if (TempCapableReset) {
								paragraph "$it current color:hue=${it.currentValue("hue")} saturation=${it.currentValue("saturation")} temp=${it.currentValue("colorTemperature")}"
							} else {
								paragraph "$it current color:hue=${it.currentValue("hue")} saturation=${it.currentValue("saturation")}"
						}
						if (TempCapable) {input "CustomKalvins", "number", title: "Input the custom temperature (2700-6500)" , defaultValue: "2700", range: "2700..6500"}
					}
					if (TempCapable) {input "BulbKalvins", "number", title: "Favorite bulb temperature (2700-6500)" , defaultValue: "2700", range: "2700..6500"}
				}
			}
		}
		section(title: "Dimmer Actions...", hidden: hideDimmersActions(), hideable: true) {
			input "Dimmers", "capability.switchLevel", title: "Which Dimming Switch(s)?", multiple: true, required: false, submitOnChange: true
			if (Dimmers) {
				input "DimmersFav", "number", title: "Favorite dimmer Level", defaultValue: "50", range: "0..100"
				boolean TempCapable = false
				Dimmers.each {
					TempCapable = false
					for ( def i = 0; i < it.capabilities.size(); i++) {
						if (it.capabilities[i].name as String == "Color Temperature") {
							TempCapable = true
						}
					}
				}
				if (TempCapable) {input "DimmerKalvins", "number", title: "Favorite bulb temperature (2700-6500)" , defaultValue: "2700", range: "2700..6500"}
			}
		}
		section(title: "Switch Actions...", hidden: hideSwitchesActions(), hideable: true) {
			input "Switches", "capability.switch", title: "Which On/Off Switch(s)?", multiple: true, required: false, submitOnChange: true
		}
		section(title: "Outlet Actions...", hidden: hideOutletsActions(), hideable: true) {
			input "Outlets", "capability.outlet", title: "Which Outlet(s)?", multiple: true, required: false, submitOnChange: true
		}
		section(title: "Relay Actions...", hidden: hideRelaysActions(), hideable: true) {
			input "Relays", "capability.relaySwitch", title: "Which Relay Switch(s)?", multiple: true, required: false, submitOnChange: true
		}
	}
}

def Assignments() {
	dynamicPage(name: "Assignments") {
		def ActionstoTake = []
		if (ChangeModes) {
			ActionstoTake = ActionstoTake += "Alarm:Set:Away"
			ActionstoTake = ActionstoTake += "Alarm:Set:Home"
			ActionstoTake = ActionstoTake += "Alarm:Set:Night"
		}
		if (Sirens) {
			Sirens.each {
				ActionstoTake = ActionstoTake += "Sirens:$it:Siren"
				ActionstoTake = ActionstoTake += "Sirens:$it:strobe"
				ActionstoTake = ActionstoTake += "Sirens:$it:both"
				ActionstoTake = ActionstoTake += "Sirens:$it:off"
			}
			ActionstoTake = ActionstoTake += "Sirens:all:Siren"
			ActionstoTake = ActionstoTake += "Sirens:all:strobe"
			ActionstoTake = ActionstoTake += "Sirens:all:both"
			ActionstoTake = ActionstoTake += "Sirens:all:off"
		}
		if (PushNotify) {
			for ( def i = 1; i <= PushTextNumber; i++) {
				ActionstoTake = ActionstoTake += "Notify:${settings["PushTitle$i"].replaceAll(' ','')}:PushNotify"
			}
		}
		if (SMSNotify) {
			for ( def i = 1; i <= SMSTextNumber; i++) {
				ActionstoTake = ActionstoTake += "Notify:${settings["SMSTitle$i"].replaceAll(' ','')}:SMSNotify"
			}
		}
		if (AudioNotify) {
			AudioNotify.each {
				ActionstoTake = ActionstoTake += "AudioNotify:$it:Doorbell"
				ActionstoTake = ActionstoTake += "AudioNotify:$it:Siren"
				ActionstoTake = ActionstoTake += "AudioNotify:$it:Beep"
				for ( def i = 1; i <= NumberofTracks; i++) {
					ActionstoTake = ActionstoTake += "AudioNotify:$it:Track:$i"
				}
			}
			ActionstoTake = ActionstoTake += "AudioNotify:all:Doorbell"
			ActionstoTake = ActionstoTake += "AudioNotify:all:Siren"
			ActionstoTake = ActionstoTake += "AudioNotify:all:Beep"
			for ( def i = 1; i <= NumberofTracks; i++) {
				ActionstoTake = ActionstoTake += "AudioNotify:all:Track:$i"
			}
		}
		if (GDCs) {
			GDCs.each {
				ActionstoTake = ActionstoTake += "GDCs:$it:open"
				ActionstoTake = ActionstoTake += "GDCs:$it:close"
			}
			ActionstoTake = ActionstoTake += "GDCs:all:open"
			ActionstoTake = ActionstoTake += "GDCs:all:close"
		}
		if (Locks) {
			Locks.each {
				ActionstoTake = ActionstoTake += "Locks:$it:lock"
				ActionstoTake = ActionstoTake += "Locks:$it:unlock"
			}
			ActionstoTake = ActionstoTake += "Locks:all:lock"
			ActionstoTake = ActionstoTake += "Locks:all:unlock"
		}
		if (DoorControls) {
			DoorControls.each {
				ActionstoTake = ActionstoTake += "DoorControls:$it:open"
				ActionstoTake = ActionstoTake += "DoorControls:$it:close"
			}
			ActionstoTake = ActionstoTake += "DoorControls:all:open"
			ActionstoTake = ActionstoTake += "DoorControls:all:close"
		}
		if (Images) {
			Images.each {
				ActionstoTake = ActionstoTake += "Images:$it:take"
			}
			ActionstoTake = ActionstoTake += "Images:all:take"
		}
		if (Bulbs) {
			boolean TempCapable = false
			boolean TempCapableReset = false
			Bulbs.each {
				TempCapableReset = false
				ActionstoTake = ActionstoTake += "Bulbs:$it:on"
				ActionstoTake = ActionstoTake += "Bulbs:$it:off"
				ActionstoTake = ActionstoTake += "Bulbs:$it:toggle"
				ActionstoTake = ActionstoTake += "Bulbs:$it:brighter"
				ActionstoTake = ActionstoTake += "Bulbs:$it:dimmer"
				ActionstoTake = ActionstoTake += "Bulbs:$it:favLevel"
				ActionstoTake = ActionstoTake += "Bulbs:$it:full"
				for ( def i = 0; i < it.capabilities.size(); i++) {
					if (it.capabilities[i].name as String == "Color Temperature") {
						ActionstoTake = ActionstoTake += "Bulbs:$it:temperature"
						TempCapable = true
						TempCapableReset = true
					}
				}
				if (CustomColor) {ActionstoTake = ActionstoTake += "Bulbs:$it:CustomColor"}
				if (ColorAvailable) {
					ActionstoTake = ActionstoTake += "Bulbs:$it:Soft White"
					ActionstoTake = ActionstoTake += "Bulbs:$it:White"
					ActionstoTake = ActionstoTake += "Bulbs:$it:Daylight"
					ActionstoTake = ActionstoTake += "Bulbs:$it:Warm White"
					ActionstoTake = ActionstoTake += "Bulbs:$it:Red"
					ActionstoTake = ActionstoTake += "Bulbs:$it:Green"
					ActionstoTake = ActionstoTake += "Bulbs:$it:Blue"
					ActionstoTake = ActionstoTake += "Bulbs:$it:Yellow"
					ActionstoTake = ActionstoTake += "Bulbs:$it:Orange"
					ActionstoTake = ActionstoTake += "Bulbs:$it:Purple"
					ActionstoTake = ActionstoTake += "Bulbs:$it:Pink"
				}
			}
			ActionstoTake = ActionstoTake += "Bulbs:all:on"
			ActionstoTake = ActionstoTake += "Bulbs:all:off"
			ActionstoTake = ActionstoTake += "Bulbs:all:toggle"
			ActionstoTake = ActionstoTake += "Bulbs:all:brighter"
			ActionstoTake = ActionstoTake += "Bulbs:all:dimmer"
			ActionstoTake = ActionstoTake += "Bulbs:all:favLevel"
			ActionstoTake = ActionstoTake += "Bulbs:all:full"
			if (TempCapable) {ActionstoTake = ActionstoTake += "Bulbs:all:temperature"}
			if (CustomColor) {ActionstoTake = ActionstoTake += "Bulbs:all:CustomColor"}
			if (ColorAvailable) {
				ActionstoTake = ActionstoTake += "Bulbs:all:Soft White"
				ActionstoTake = ActionstoTake += "Bulbs:all:White"
				ActionstoTake = ActionstoTake += "Bulbs:all:Daylight"
				ActionstoTake = ActionstoTake += "Bulbs:all:Warm White"
				ActionstoTake = ActionstoTake += "Bulbs:all:Red"
				ActionstoTake = ActionstoTake += "Bulbs:all:Green"
				ActionstoTake = ActionstoTake += "Bulbs:all:Blue"
				ActionstoTake = ActionstoTake += "Bulbs:all:Yellow"
				ActionstoTake = ActionstoTake += "Bulbs:all:Orange"
				ActionstoTake = ActionstoTake += "Bulbs:all:Purple"
				ActionstoTake = ActionstoTake += "Bulbs:all:Pink"
			}
		}
		if (Dimmers) {
			boolean TempCapable = false
			Dimmers.each {
				TempCapable = false
				ActionstoTake = ActionstoTake += "Dimmers:$it:on"
				ActionstoTake = ActionstoTake += "Dimmers:$it:off"
				ActionstoTake = ActionstoTake += "Dimmers:$it:toggle"
				ActionstoTake = ActionstoTake += "Dimmers:$it:brighter"
				ActionstoTake = ActionstoTake += "Dimmers:$it:dimmer"
				ActionstoTake = ActionstoTake += "Dimmers:$it:favLevel"
				ActionstoTake = ActionstoTake += "Dimmers:$it:full"
				for ( def i = 0; i < it.capabilities.size(); i++) {
					if (it.capabilities[i].name as String == "Color Temperature") {
						ActionstoTake = ActionstoTake += "Dimmers:$it:temperature"
						TempCapable = true
					}
				}
			}
			ActionstoTake = ActionstoTake += "Dimmers:all:on"
			ActionstoTake = ActionstoTake += "Dimmers:all:off"
			ActionstoTake = ActionstoTake += "Dimmers:all:toggle"
			ActionstoTake = ActionstoTake += "Dimmers:all:brighter"
			ActionstoTake = ActionstoTake += "Dimmers:all:dimmer"
			ActionstoTake = ActionstoTake += "Dimmers:all:favLevel"
			ActionstoTake = ActionstoTake += "Dimmers:all:full"
			if (TempCapable) {ActionstoTake = ActionstoTake += "Dimmers:all:temperature"}
		}
		if (Switches) {
			Switches.each {
				ActionstoTake = ActionstoTake += "Switches:$it:on"
				ActionstoTake = ActionstoTake += "Switches:$it:off"
				ActionstoTake = ActionstoTake += "Switches:$it:toggle"
			}
			ActionstoTake = ActionstoTake += "Switches:all:on"
			ActionstoTake = ActionstoTake += "Switches:all:off"
			ActionstoTake = ActionstoTake += "Switches:all:toggle"
		}
		if (Outlets) {
			Outlets.each {
				ActionstoTake = ActionstoTake += "Outlets:$it:on"
				ActionstoTake = ActionstoTake += "Outlets:$it:off"
			}
			ActionstoTake = ActionstoTake += "Outlets:all:on"
			ActionstoTake = ActionstoTake += "Outlets:all:off"
		}
		if (Relays) {
			Relays.each {
				ActionstoTake = ActionstoTake += "Relays:$it:on"
				ActionstoTake = ActionstoTake += "Relays:$it:off"
			}
			ActionstoTake = ActionstoTake += "Relays:all:on"
			ActionstoTake = ActionstoTake += "Relays:all:off"
		}
		if (Valves) {
			Valves.each {
				ActionstoTake = ActionstoTake += "Valves:$it:open"
				ActionstoTake = ActionstoTake += "Valves:$it:close"
			}
			ActionstoTake = ActionstoTake += "Valves:all:open"
			ActionstoTake = ActionstoTake += "Valves:all:close"
		}
		if (Shades) {
			Shades.each {
				ActionstoTake = ActionstoTake += "Shades:$it:open"
				ActionstoTake = ActionstoTake += "Shades:$it:close"
				ActionstoTake = ActionstoTake += "Shades:$it:preset"
			}
			ActionstoTake = ActionstoTake += "Shades:all:open"
			ActionstoTake = ActionstoTake += "Shades:all:close"
			ActionstoTake = ActionstoTake += "Shades:all:preset"
		}
		if (Speakers) {
			Speakers.each {
				ActionstoTake = ActionstoTake += "Speakers:$it:play"
				ActionstoTake = ActionstoTake += "Speakers:$it:pause"
				ActionstoTake = ActionstoTake += "Speakers:$it:pp_toggle"
				ActionstoTake = ActionstoTake += "Speakers:$it:mute"
				ActionstoTake = ActionstoTake += "Speakers:$it:unmute"
				ActionstoTake = ActionstoTake += "Speakers:$it:next"
				ActionstoTake = ActionstoTake += "Speakers:$it:previous"
				ActionstoTake = ActionstoTake += "Speakers:$it:vol_up"
				ActionstoTake = ActionstoTake += "Speakers:$it:vol_down"
			}
		}
		if (Button) {
			section(title: "Configure Buttons...", hidden: hideButtonActions(), hideable: true) {
				for ( def h = 0; h < Button.size(); h++) {
					paragraph "${Button[h]}"
					boolean HoldSet = false
					for ( def i = 0; i < Button[h].capabilities.size(); i++) {
						if (Button[h].capabilities[i].name as String == "Holdable Button") {
							HoldSet = true
						}
					}
					if (Button[h].currentValue("numberOfButtons") == null) {input "NumberofButtons${Button[h].id}", "number", title: "Number of Buttons on ${Button[h]}", required: true, submitOnChange: true}
					if (!HoldSet) {input "Holdable${Button[h].id}", "bool", title: "Controller supports holding buttons", submitOnChange: true}
					int Num = 0
					if (settings["NumberofButtons${Button[h].id}"] != null) {Num = settings["NumberofButtons${Button[h].id}"] as Integer}
					if (Button[h].currentValue("numberOfButtons") != null) {Num = Button[h].currentValue("numberOfButtons") as Integer}
					for ( def i = 1; i <= Num; i++) {
						input "${Button[h].id}Button${i}Push", "enum", title: "${Button[h]} button $i Push", options: ActionstoTake, required: false, multiple: true, submitOnChange: true
						if (settings["${Button[h].id}Button${i}Push"]) {
							boolean AskForTime = false
							for ( def j = 0; j < settings["${Button[h].id}Button${i}Push"].size(); j++) {
								def Selection = settings["${Button[h].id}Button${i}Push"][j].tokenize(":")
								if (Selection[0] == "Switches" || Selection[0] == "Bulbs" || Selection[0] == "Dimmers") {
									if (Selection[2] == "on" || Selection[2] == "favLevel" || Selection[2] == "full") {
										def Name = Selection[1]
										if (Selection[1] == "all") {Name = "${Selection[0]}:${Selection[1]}"}
										input "TurnOffAfterTime${Button[h].id}Button${i}Push$Name", "bool", title: "Turn off `$Name` after some time", submitOnChange: true
										if (settings["TurnOffAfterTime${Button[h].id}Button${i}Push$Name"]) {AskForTime = true}
									}
								}
							}
							if (AskForTime) {input "TurnOffAfterMinutes${Button[h].id}Button${i}Push", "number", title: "Turn off after how many minutes", required: true, range: "1..*"}
						}
						if (settings["Holdable${Button[h].id}"] || HoldSet) {input "${Button[h].id}Button${i}Hold", "enum", title: "${Button[h]} button $i Long Push", options: ActionstoTake, required: false, multiple: true, submitOnChange: true}
						if (settings["${Button[h].id}Button${i}Hold"]) {
							boolean AskForTime = false
							for ( def j = 0; j < settings["${Button[h].id}Button${i}Hold"].size(); j++) {
								def Selection = settings["${Button[h].id}Button${i}Hold"][j].tokenize(":")
								if (Selection[0] == "Switches" || Selection[0] == "Bulbs" || Selection[0] == "Dimmers") {
									if (Selection[2] == "on" || Selection[2] == "favLevel" || Selection[2] == "full") {
										def Name = Selection[1]
										if (Selection[1] == "all") {Name = "${Selection[0]}:${Selection[1]}"}
										input "TurnOffAfterTime${Button[h].id}Button${i}Hold$Name", "bool", title: "Turn off `$Name` after some time", submitOnChange: true
										if (settings["TurnOffAfterTime${Button[h].id}Button${i}Hold$Name"]) {AskForTime = true}
									}
								}
							}
							if (AskForTime) {input "TurnOffAfterMinutes${Button[h].id}Button${i}Hold", "number", title: "Turn off after how many minutes", required: true, range: "1..*"}
						}
					}
				}
			}
		}
		if (Acceleration) {
			section(title: "Configure Acceleration...", hidden: hideAccelerationActions(), hideable: true) {
				for ( def i = 0; i < Acceleration.size(); i++) {
					input "AccelerationActive${Acceleration[i].id}", "enum", title: "${Acceleration[i]} acceleration Active", options: ActionstoTake, required: false, multiple: true
					input "AccelerationInactive${Acceleration[i].id}", "enum", title: "${Acceleration[i]} acceleration Inactive", options: ActionstoTake, required: false, multiple: true
				}
			}
		}
		if (Contact) {
			section(title: "Configure Contact...", hidden: hideContactActions(), hideable: true) {
				for ( def i = 0; i < Contact.size(); i++) {
					input "ContactClosed${Contact[i].id}", "enum", title: "${Contact[i]} contact Closed", options: ActionstoTake, required: false, multiple: true
					input "ContactOpen${Contact[i].id}", "enum", title: "${Contact[i]} contact Open", options: ActionstoTake, required: false, multiple: true, submitOnChange: true
					if (settings["ContactOpen${Contact[i].id}"]) {
						boolean AskForTime = false
						for ( def j = 0; j < settings["ContactOpen${Contact[i].id}"].size(); j++) {
							def Selection = settings["ContactOpen${Contact[i].id}"][j].tokenize(":")
							if (Selection[0] == "Switches" || Selection[0] == "Bulbs" || Selection[0] == "Dimmers") {
								if (Selection[2] == "on" || Selection[2] == "favLevel" || Selection[2] == "full") {
									def Name = Selection[1]
									if (Selection[1] == "all") {Name = "${Selection[0]}:${Selection[1]}"}
									input "TurnOffAfterTime${settings["ContactOpen${Contact[i].id}"]}$Name", "bool", title: "Turn off `$Name` after some time", submitOnChange: true
									if (settings["TurnOffAfterTime${settings["ContactOpen${Contact[i].id}"]}$Name"]) {AskForTime = true}
								}
							}
						}
						if (AskForTime) {input "TurnOffAfterMinutes${settings["ContactOpen${Contact[i].id}"]}", "number", title: "Turn off after how many minutes", required: true, range: "1..*"}
					}
				}
			}
		}
		if (Motion) {
			section(title: "Configure Motion...", hidden: hideMotionActions(), hideable: true) {
				for ( def i = 0; i < Motion.size(); i++) {
					input "MotionActive${Motion[i].id}", "enum", title: "${Motion[i]} motion Active", options: ActionstoTake, required: false, multiple: true, submitOnChange: true
					input "MotionInactive${Motion[i].id}", "enum", title: "${Motion[i]} motion Inactive", options: ActionstoTake, required: false, multiple: true
					if (settings["MotionActive${Motion[i].id}"]) {
						boolean AskForTime = false
						for ( def j = 0; j < settings["MotionActive${Motion[i].id}"].size(); j++) {
							def Selection = settings["MotionActive${Motion[i].id}"][j].tokenize(":")
							if (Selection[0] == "Switches" || Selection[0] == "Bulbs" || Selection[0] == "Dimmers") {
								if (Selection[2] == "on" || Selection[2] == "favLevel" || Selection[2] == "full") {
									def Name = Selection[1]
									if (Selection[1] == "all" || settings["MotionActive${Motion[i].id}"].size() > 1) {Name = "${Selection[0]}:${Selection[1]}"}
									input "TurnOffAfterTime${settings["MotionActive${Motion[i].id}"]}$Name", "bool", title: "Turn off `$Name` after some time", submitOnChange: true
									if (settings["TurnOffAfterTime${settings["MotionActive${Motion[i].id}"]}$Name"]) {AskForTime = true}
								}
							}
						}
						if (AskForTime) {input "TurnOffAfterMinutes${settings["MotionActive${Motion[i].id}"]}", "number", title: "Turn off after how many minutes", required: true, range: "1..*"}
					}
				}
			}
		}
		if (Tamper) {
			section(title: "Configure Tamper...", hidden: hideTamperActions(), hideable: true) {
				for ( def i = 0; i < Tamper.size(); i++) {
					input "TamperClear${Tamper[i].id}", "enum", title: "${Tamper[i]} tamper Clear", options: ActionstoTake, required: false, multiple: true
					input "TamperDetected${Tamper[i].id}", "enum", title: "${Tamper[i]} tamper Detected", options: ActionstoTake, required: false, multiple: true
				}
			}
		}
		if (Shock) {
			section(title: "Configure Shock...", hidden: hideShockActions(), hideable: true) {
				for ( def i = 0; i < Shock.size(); i++) {
					input "ShockClear${Shock[i].id}", "enum", title: "${Shock[i]} shock Clear", options: ActionstoTake, required: false, multiple: true
					input "ShockDetected${Shock[i].id}", "enum", title: "${Shock[i]} shock Detected", options: ActionstoTake, required: false, multiple: true
				}
		   }
		}
		if (Sleep) {
			section(title: "Configure Sleep...", hidden: hideSleepActions(), hideable: true) {
				for ( def i = 0; i < Sleep.size(); i++) {
					input "SleepNotSleeping${Sleep[i].id}", "enum", title: "${Sleep[i]} Not Sleeping", options: ActionstoTake, required: false, multiple: true
					input "SleepSleeping${Sleep[i].id}", "enum", title: "${Sleep[i]} Sleeping", options: ActionstoTake, required: false, multiple: true
				}
			}
		}
		if (Sound) {
			section(title: "Configure Sound...", hidden: hideSoundActions(), hideable: true) {
				for ( def i = 0; i < Sound.size(); i++) {
					input "SoundDetected${Sound[i].id}", "enum", title: "${Sound[i]} sound Detected", options: ActionstoTake, required: false, multiple: true
					input "SoundNotDetected${Sound[i].id}", "enum", title: "${Sound[i]} sound Not Detected", options: ActionstoTake, required: false, multiple: true
				}
			}
		}
		if (Touch) {
		section(title: "Configure Touch...", hidden: hideTouchActions(), hideable: true) {
				for ( def i = 0; i < Touch.size(); i++) {
					input "Touched${Touch[i].id}", "enum", title: "${Touch[i]} Touched", options: ActionstoTake, required: false, multiple: true
				}
			}
		}
		if (Water) {
			section(title: "Configure Water...", hidden: hideWaterActions(), hideable: true) {
				for ( def i = 0; i < Water.size(); i++) {
					input "WaterDry${Water[i].id}", "enum", title: "${Water[i]} Dry", options: ActionstoTake, required: false, multiple: true
					input "WaterWet${Water[i].id}", "enum", title: "${Water[i]} Wet", options: ActionstoTake, required: false, multiple: true
				}
			}
		}
		if (CODetector) {
			section(title: "Configure CODetector...", hidden: hideCODetectorActions(), hideable: true) {
				for ( def i = 0; i < CODetector.size(); i++) {
					input "CODetectorClear${CODetector[i].id}", "enum", title: "${CODetector[i]} CO Clear", options: ActionstoTake, required: false, multiple: true
					input "CODetectorDetected${CODetector[i].id}", "enum", title: "${CODetector[i]} CO Detected", options: ActionstoTake, required: false, multiple: true
					input "CODetectorTested${CODetector[i].id}", "enum", title: "${CODetector[i]} CO Tested", options: ActionstoTake, required: false, multiple: true
				}
			}
		}
		if (Smoke) {
			section(title: "Configure Smoke...", hidden: hideSmokeActions(), hideable: true) {
				for ( def i = 0; i < Smoke.size(); i++) {
					input "SmokeClear${Smoke[i].id}", "enum", title: "${Smoke[i]} smoke Clear", options: ActionstoTake, required: false, multiple: true
					input "SmokeDetected${Smoke[i].id}", "enum", title: "${Smoke[i]} smoke Detected", options: ActionstoTake, required: false, multiple: true
					input "SmokeTested${Smoke[i].id}", "enum", title: "${Smoke[i]} smoke Tested", options: ActionstoTake, required: false, multiple: true
				}
			}
		}
		if (CO2Measurement) {
			section(title: "Configure CO2Measurement...", hidden: hideCO2MeasurementActions(), hideable: true) {
				if (AllCO2MeasurementHighThreshold) {
						input "AllCO2MeasurementHighActions", "enum", title: "Any CO2 Measurement above threshold", options: ActionstoTake, required: false, multiple: true
					} else {
						for ( def i = 0; i < CO2Measurement.size(); i++) {
							if (settings["CO2MeasurementHighThreshold${CO2Measurement[i].id}"]) {input "CO2MeasurementHighActions${CO2Measurement[i].id}", "enum", title: "${CO2Measurement[i]} CO2 above threshold", options: ActionstoTake, required: false, multiple: true}
						}
				}
			}
		}
		if (Beacon) {
			section(title: "Configure Beacon...", hidden: hideBeaconActions(), hideable: true) {
 				for ( def i = 0; i < Beacon.size(); i++) {
					input "BeaconNotPresent${Beacon[i].id}", "enum", title: "${Beacon[i]} Not Present", options: ActionstoTake, required: false, multiple: true
					input "BeaconPresent${Beacon[i].id}", "enum", title: "${Beacon[i]} Present", options: ActionstoTake, required: false, multiple: true
				}
				input "BeaconAllAway", "enum", title: "All Beacons Away", options: ActionstoTake, required: false, multiple: true
				input "BeaconAllPresent", "enum", title: "All Beacons Present", options: ActionstoTake, required: false, multiple: true
			}
		}
		if (Presence) {
			section(title: "Configure Presence...", hidden: hidePresenceActions(), hideable: true) {
				for ( def i = 0; i < Presence.size(); i++) {
					input "PresenceNotPresent${Presence[i].id}", "enum", title: "${Presence[i]} Not Present", options: ActionstoTake, required: false, multiple: true
					input "PresencePresent${Presence[i].id}", "enum", title: "${Presence[i]} Present", options: ActionstoTake, required: false, multiple: true
				}
				input "PresenceAllAway", "enum", title: "All Presence devices Away", options: ActionstoTake, required: false, multiple: true
				input "PresenceAllPresent", "enum", title: "All Presence devices Present", options: ActionstoTake, required: false, multiple: true
			}
		}
		if (PowerSource) {
			section(title: "Configure PowerSource...", hidden: hidePowerSourceActions(), hideable: true) {
				for ( def i = 0; i < PowerSource.size(); i++) {
					input "PowerSourceBattery${PowerSource[i].id}", "enum", title: "${PowerSource[i]} running on Battery", options: ActionstoTake, required: false, multiple: true
					input "PowerSourceDC${PowerSource[i].id}", "enum", title: "${PowerSource[i]} running on DC", options: ActionstoTake, required: false, multiple: true
					input "PowerSourceMains${PowerSource[i].id}", "enum", title: "${PowerSource[i]} running on Mains", options: ActionstoTake, required: false, multiple: true
					input "PowerSourceUnknown${PowerSource[i].id}", "enum", title: "${PowerSource[i]} running on Unknown", options: ActionstoTake, required: false, multiple: true
				}
			}
		}
		if (PowerMeter) {
			section(title: "Configure PowerMeter...", hidden: hidePowerMeterActions(), hideable: true) {
				if (AllPowerMeterHighThreshold || AllPowerMeterLowThreshold) {
						if (AllPowerMeterHighThreshold) {input "AllPowerMeterHighActions", "enum", title: "Any high power usage", options: ActionstoTake, required: false, multiple: true}
						if (AllPowerMeterLowThreshold) {input "AllPowerMeterLowActions", "enum", title: "Any low power usage", options: ActionstoTake, required: false, multiple: true}
					} else {
						for ( def i = 0; i < PowerMeter.size(); i++) {
							if (settings["PowerMeterHighThreshold${PowerMeter[i].id}"]) {input "PowerMeterHighActions${PowerMeter[i].id}", "enum", title: "${PowerMeter[i]} power usage high", options: ActionstoTake, required: false, multiple: true}
							if (settings["PowerMeterBottomThreshold${PowerMeter[i].id}"]) {input "PowerMeterLowActions${PowerMeter[i].id}", "enum", title: "${PowerMeter[i]} power usage low", options: ActionstoTake, required: false, multiple: true}
						}
				}
			}
		}
		if (Battery) {
			section(title: "Configure Battery...", hidden: hideBatteryActions(), hideable: true) {
				if (AllBatteryLowThreshold) {
						input "AllBatteryLowActions", "enum", title: "Any battery low", options: ActionstoTake, required: false, multiple: true
					} else {
						for ( def i = 0; i < Battery.size(); i++) {
							if (settings["BatteryLowThreshold${Battery[i].id}"]) {input "BatteryLowActions${Battery[i].id}", "enum", title: "${Battery[i]} battery low", options: ActionstoTake, required: false, multiple: true}
						}
				}
			}
		}
		if (Voltage) {
			section(title: "Configure Voltage...", hidden: hideVoltageActions(), hideable: true) {
				if (AllVoltageHighThreshold || AllVoltageLowThreshold) {
						if (AllVoltageHighThreshold) {input "AllVoltageHighActions", "enum", title: "Any high voltage", options: ActionstoTake, required: false, multiple: true}
						if (AllVoltageLowThreshold) {input "AllVoltageLowActions", "enum", title: "Any low voltage", options: ActionstoTake, required: false, multiple: true}
					} else {
						for ( def i = 0; i < Voltage.size(); i++) {
							if (settings["VoltageHighThreshold${Voltage[i].id}"]) {input "VoltageHighActions${Voltage[i].id}", "enum", title: "${Voltage[i]} voltage high", options: ActionstoTake, required: false, multiple: true}
							if (settings["VoltageBottomThreshold${Voltage[i].id}"]) {input "VoltageLowActions${Voltage[i].id}", "enum", title: "${Voltage[i]} voltage low", options: ActionstoTake, required: false, multiple: true}
						}
				}
			}
		}
		if (EnergyMeter) {
			section(title: "Configure EnergyMeter...", hidden: hideEnergyMeterActions(), hideable: true) {
				if (AllEnergyMeterHighThreshold || AllEnergyMeterLowThreshold) {
						if (AllEnergyMeterHighThreshold) {input "AllEnergyMeterHighActions", "enum", title: "Any high energy usage", options: ActionstoTake, required: false, multiple: true}
						if (AllEnergyMeterLowThreshold) {input "AllEnergyMeterLowActions", "enum", title: "Any low energy usage", options: ActionstoTake, required: false, multiple: true}
					} else {
						for ( def i = 0; i < EnergyMeter.size(); i++) {
							if (settings["EnergyMeterHighThreshold${EnergyMeter[i].id}"]) {input "EnergyMeterHighActions${EnergyMeter[i].id}", "enum", title: "${EnergyMeter[i]} energy usage high", options: ActionstoTake, required: false, multiple: true}
							if (settings["EnergyMeterBottomThreshold${EnergyMeter[i].id}"]) {input "EnergyMeterLowActions${EnergyMeter[i].id}", "enum", title: "${EnergyMeter[i]} energy usage low", options: ActionstoTake, required: false, multiple: true}
						}
				}
			}
		}
		if (Door) {
			section(title: "Configure Doors...", hidden: hideDoorActions(), hideable: true) {
				for ( def i = 0; i < Door.size(); i++) {
					input "DoorClosed${Door[i].id}", "enum", title: "${Door[i]} Closed", options: ActionstoTake, required: false, multiple: true
					input "DoorOpen${Door[i].id}", "enum", title: "${Door[i]} Open", options: ActionstoTake, required: false, multiple: true
					input "DoorUnknown${Door[i].id}", "enum", title: "${Door[i]} Unknown", options: ActionstoTake, required: false, multiple: true
				}
			}
		}
		if (Switch) {
			section(title: "Configure Switches...", hidden: hideSwitchActions(), hideable: true) {
				for ( def i = 0; i < Switch.size(); i++) {
					input "SwitchOff${Switch[i].id}", "enum", title: "${Switch[i]} Off", options: ActionstoTake, required: false, multiple: true
					input "SwitchOn${Switch[i].id}", "enum", title: "${Switch[i]} On", options: ActionstoTake, required: false, multiple: true, submitOnChange: true
					if (settings["SwitchOn${Switch[i].id}"]) {
						boolean AskForTime = false
						for ( def j = 0; j < settings["SwitchOn${Switch[i].id}"].size(); j++) {
							def Selection = settings["SwitchOn${Switch[i].id}"][j].tokenize(":")
							if (Selection[0] == "Switches" || Selection[0] == "Bulbs" || Selection[0] == "Dimmers") {
								if (Selection[2] == "on" || Selection[2] == "favLevel" || Selection[2] == "full") {
									def Name = Selection[1]
									if (Selection[1] == "all" || settings["SwitchOn${Switch[i].id}"].size() > 1) {Name = "${Selection[0]}:${Selection[1]}"}
									input "TurnOffAfterTime${settings["SwitchOn${Switch[i].id}"]}$Name", "bool", title: "Turn off `$Name` after some time", submitOnChange: true
									if (settings["TurnOffAfterTime${settings["SwitchOn${Switch[i].id}"]}$Name"]) {AskForTime = true}
								}
							}
						}
						if (AskForTime) {input "TurnOffAfterMinutes${settings["SwitchOn${Switch[i].id}"]}", "number", title: "Turn off after how many minutes", required: true, range: "1..*"}
					}
				}
			}
		}
		if (Temperature) {
			section(title: "Configure Temperature...", hidden: hideTemperatureActions(), hideable: true) {
				if (AllHighThreshold || AllLowThreshold) {
						if (AllHighThreshold) {input "AllHighActions", "enum", title: "Any high temp", options: ActionstoTake, required: false, multiple: true}
						if (AllLowThreshold) {input "AllLowActions", "enum", title: "Any low temp", options: ActionstoTake, required: false, multiple: true}
					} else {
						for ( def i = 0; i < Temperature.size(); i++) {
							if (settings["HighThreshold${Temperature[i].id}"]) {input "HighActions${Temperature[i].id}", "enum", title: "${Temperature[i]} high temp", options: ActionstoTake, required: false, multiple: true}
							if (settings["LowThreshold${Temperature[i].id}"]) {input "LowActions${Temperature[i].id}", "enum", title: "${Temperature[i]} low temp", options: ActionstoTake, required: false, multiple: true}
						}
				}
			}
		}
		if (Valve) {
			section(title: "Configure Valve...", hidden: hideValveActions(), hideable: true) {
 				for ( def i = 0; i < Valve.size(); i++) {
					input "ValveClosed${Valve[i].id}", "enum", title: "${Valve[i]} Closed", options: ActionstoTake, required: false, multiple: true
					input "ValveOpen${Valve[i].id}", "enum", title: "${Valve[i]} Open", options: ActionstoTake, required: false, multiple: true
				}
			}
		}
		if (Shade) {
			section(title: "Configure Shade...", hidden: hideShadeActions(), hideable: true) {
				for ( def i = 0; i < Shade.size(); i++) {
					input "ShadeClosed${Shade[i].id}", "enum", title: "${Shade[i]} Closed", options: ActionstoTake, required: false, multiple: true
					input "ShadeOpen${Shade[i].id}", "enum", title: "${Shade[i]} Open", options: ActionstoTake, required: false, multiple: true
					input "ShadePartiallyOpen${Shade[i].id}", "enum", title: "${Shade[i]} Partially Open", options: ActionstoTake, required: false, multiple: true
					input "ShadeUnknown${Shade[i].id}", "enum", title: "${Shade[i]} Unknown", options: ActionstoTake, required: false, multiple: true
				}
			}
		}
		if (Step) {
			section(title: "Configure Step...", hidden: hideStepActions(), hideable: true) {
				if (AllStepHighThreshold || AllStepLowThreshold || AllStepGoal) {
						if (AllStepHighThreshold) {input "AllStepHighActions", "enum", title: "Any step high", options: ActionstoTake, required: false, multiple: true}
						if (AllStepLowThreshold) {input "AllStepLowActions", "enum", title: "Any step low", options: ActionstoTake, required: false, multiple: true}
						if (AllStepGoal) {input "AllStepGoalActions", "enum", title: "Any step goal", options: ActionstoTake, required: false, multiple: true}
					} else {
						for ( def i = 0; i < Step.size(); i++) {
							if (settings["StepHighThreshold${Step[i].id}"]) {input "StepHighActions${Step[i].id}", "enum", title: "${Step[i]} steps high", options: ActionstoTake, required: false, multiple: true}
							if (settings["StepBottomThreshold${Step[i].id}"]) {input "StepLowActions${Step[i].id}", "enum", title: "${Step[i]} steps low", options: ActionstoTake, required: false, multiple: true}
							if (settings["StepGoal${Step[i].id}"]) {input "StepGoalActions${Step[i].id}", "enum", title: "${Step[i]} steps goal reached", options: ActionstoTake, required: false, multiple: true}
						}
				}
			}
		}
		if (Illuminance) {
			section(title: "Configure Illuminance...", hidden: hideIlluminanceActions(), hideable: true) {
				if (AllIlluminanceHighThreshold || AllIlluminanceLowThreshold) {
						if (AllIlluminanceHighThreshold) {input "AllIlluminanceHighActions", "enum", title: "Any illuminance high", options: ActionstoTake, required: false, multiple: true}
						if (AllIlluminanceLowThreshold) {input "AllIlluminanceLowActions", "enum", title: "Any illuminance low", options: ActionstoTake, required: false, multiple: true}
					} else {
						for ( def i = 0; i < Illuminance.size(); i++) {
							if (settings["IlluminanceHighThreshold${Illuminance[i].id}"]) {input "IlluminanceHighActions${Illuminance[i].id}", "enum", title: "${Illuminance[i]} illuminance high", options: ActionstoTake, required: false, multiple: true}
							if (settings["IlluminanceBottomThreshold${Illuminance[i].id}"]) {input "IlluminanceLowActions${Illuminance[i].id}", "enum", title: "${Illuminance[i]} illuminance low", options: ActionstoTake, required: false, multiple: true}
						}
				}
			}
		}
		if (Humidity) {
			section(title: "Configure Humidity...", hidden: hideHumidityActions(), hideable: true) {
				if (AllHumidityHighThreshold || AllHumidityLowThreshold) {
						if (AllHumidityHighThreshold) {input "AllHumidityHighActions", "enum", title: "Any humidity high", options: ActionstoTake, required: false, multiple: true}
						if (AllHumidityLowThreshold) {input "AllHumidityLowActions", "enum", title: "Any humidity low", options: ActionstoTake, required: false, multiple: true}
					} else {
						for ( def i = 0; i < Humidity.size(); i++) {
							if (settings["HumidityHighThreshold${Humidity[i].id}"]) {input "HumidityHighActions${Humidity[i].id}", "enum", title: "${Humidity[i]} humidity high", options: ActionstoTake, required: false, multiple: true}
							if (settings["HumidityBottomThreshold${Humidity[i].id}"]) {input "HumidityLowActions${Humidity[i].id}", "enum", title: "${Humidity[i]} humidity low", options: ActionstoTake, required: false, multiple: true}
						}
				}
			}
		}
		if (soundPressureLevel) {
			section(title: "Configure Sound Pressure Level...", hidden: hidesoundPressureLevelActions(), hideable: true) {
				if (AllsoundPressureLevelHighThreshold || AllsoundPressureLevelLowThreshold) {
						if (AllsoundPressureLevelHighThreshold) {input "AllsoundPressureLevelHighActions", "enum", title: "Any sound pressure level high", options: ActionstoTake, required: false, multiple: true}
						if (AllsoundPressureLevelLowThreshold) {input "AllsoundPressureLevelLowActions", "enum", title: "Any sound pressure level low", options: ActionstoTake, required: false, multiple: true}
					} else {
						for ( def i = 0; i < soundPressureLevel.size(); i++) {
							if (settings["soundPressureLevelHighThreshold${soundPressureLevel[i].id}"]) {input "soundPressureLevelHighActions${soundPressureLevel[i].id}", "enum", title: "${soundPressureLevel[i]} sound pressure level high", options: ActionstoTake, required: false, multiple: true}
							if (settings["soundPressureLevelBottomThreshold${soundPressureLevel[i].id}"]) {input "soundPressureLevelLowActions${soundPressureLevel[i].id}", "enum", title: "${soundPressureLevel[i]} sound pressure level low", options: ActionstoTake, required: false, multiple: true}
						}
				}
			}
		}
		if (UltravioletIndex) {
			section(title: "Configure UV Index...", hidden: hideUltravioletIndexActions(), hideable: true) {
				if (AllUltravioletIndexHighThreshold || AllUltravioletIndexLowThreshold) {
						if (AllUltravioletIndexHighThreshold) {input "AllUltravioletIndexHighActions", "enum", title: "Any ultraviolet index high", options: ActionstoTake, required: false, multiple: true}
						if (AllUltravioletIndexLowThreshold) {input "AllUltravioletIndexLowActions", "enum", title: "Any ultraviolet index low", options: ActionstoTake, required: false, multiple: true}
					} else {
						for ( def i = 0; i < UltravioletIndex.size(); i++) {
							if (settings["UltravioletIndexHighThreshold${UltravioletIndex[i].id}"]) {input "UltravioletIndexHighActions${UltravioletIndex[i].id}", "enum", title: "${UltravioletIndex[i]} ultraviolet index high", options: ActionstoTake, required: false, multiple: true}
							if (settings["UltravioletIndexBottomThreshold${UltravioletIndex[i].id}"]) {input "UltravioletIndexLowActions${UltravioletIndex[i].id}", "enum", title: "${UltravioletIndex[i]} ultraviolet index low", options: ActionstoTake, required: false, multiple: true}
						}
				}
			}
		}
		if (phMeasurement) {
			section(title: "Configure ph Measurement...", hidden: hidephMeasurementActions(), hideable: true) {
				if (AllphMeasurementHighThreshold || AllphMeasurementLowThreshold) {
						if (AllphMeasurementHighThreshold) {input "AllphMeasurementHighActions", "enum", title: "Any ph index high", options: ActionstoTake, required: false, multiple: true}
						if (AllphMeasurementLowThreshold) {input "AllphMeasurementLowActions", "enum", title: "Any ph low", options: ActionstoTake, required: false, multiple: true}
					} else {
						for ( def i = 0; i < phMeasurement.size(); i++) {
							if (settings["phMeasurementHighThreshold${phMeasurement[i].id}"]) {input "phMeasurementHighActions${phMeasurement[i].id}", "enum", title: "${phMeasurement[i]} ph high", options: ActionstoTake, required: false, multiple: true}
							if (settings["phMeasurementBottomThreshold${phMeasurement[i].id}"]) {input "phMeasurementLowActions${phMeasurement[i].id}", "enum", title: "${phMeasurement[i]} ph low", options: ActionstoTake, required: false, multiple: true}
						}
				}
			}
		}
	}
}

def NametheApp() {
	def DefaultName = "Automation Director"
	if (!overrideLabel) {
		//log.debug "will set default label of $DefaultName"
		app.updateLabel(DefaultName)
	}
	if (app.label != "Automation Director") {DefaultName = app.label}
	dynamicPage(name: "NametheApp") {
		if (overrideLabel) {
				section("Automation name") {
					label title: "Enter custom name", defaultValue: DefaultName, required: false
				}
			} else {
				section("Automation name") {
					paragraph app.label
				}
		}
		section {
			input "overrideLabel", "bool", title: "Edit automation name", defaultValue: "false", required: "false", submitOnChange: true
		}
	}
}

def DoContactOpen(evt) {
	if (allOk) {
		def Changed
		def Number
		def SelectionArray = [][]
		for ( def i = 0; i < Contact.size(); i++) {
			if ( evt.deviceId == Contact[i].id ) {
				Changed = Contact[i]
				Number = i
			}
		}
		if (settings["ContactOpen${evt.deviceId}"]) {
			log.debug "Device that triggered the event: ${Changed.displayName} | Number = $Number | Actions = ${settings["ContactOpen${evt.deviceId}"]}"
			for ( def i = 0; i < settings["ContactOpen${evt.deviceId}"].size(); i++) {
				SelectionArray[i] = settings["ContactOpen${evt.deviceId}"][i].tokenize(":")
				//log.debug "DeviceType = ${SelectionArray[i][0]},Device = ${SelectionArray[i][1]},Action = ${SelectionArray[i][2]}"
				SwitchSelection(SelectionArray[i],[evt.deviceId,null,Changed,"opened","open"])
			}
		}
	}
}

def DoContactClosed(evt) {
	if (allOk) {
		def Changed
		def Number
		def SelectionArray = [][]
		for ( def i = 0; i < Contact.size(); i++) {
			if ( evt.deviceId == Contact[i].id ) {
				Changed = Contact[i]
				Number = i
			}
		}
		if (settings["ContactClosed${evt.deviceId}"]) {
			log.debug "Device that triggered the event: ${Changed.displayName} | Number = $Number | Actions = ${settings["ContactClosed${evt.deviceId}"]}"
			for ( def i = 0; i < settings["ContactClosed${evt.deviceId}"].size(); i++) {
				SelectionArray[i] = settings["ContactClosed${evt.deviceId}"][i].tokenize(":")
				SwitchSelection(SelectionArray[i],[evt.deviceId,null,Changed,"closed",null])
			}
		}
	}
}

def DoSwitchOn(evt) {
	if (allOk) {
		def Changed
		def Number
		def SelectionArray = [][]
		for ( def i = 0; i < Switch.size(); i++) {
			if ( evt.deviceId == Switch[i].id ) {
				Changed = Switch[i]
				Number = i
			}
		}
		if (settings["SwitchOn${evt.deviceId}"]) {
			log.debug "Device that triggered the event: ${Changed.displayName} | Number = $Number | Actions = ${settings["SwitchOn${evt.deviceId}"]}"
			for ( def i = 0; i < settings["SwitchOn${evt.deviceId}"].size(); i++) {
				SelectionArray[i] = settings["SwitchOn${evt.deviceId}"][i].tokenize(":")
				SwitchSelection(SelectionArray[i],[evt.deviceId,null,Changed,"on",null])
			}
		}
	}
}

def DoSwitchOff(evt) {
	if (allOk) {
		def Changed
		def Number
		def SelectionArray = [][]
		for ( def i = 0; i < Switch.size(); i++) {
			if ( evt.deviceId == Switch[i].id ) {
				Changed = Switch[i]
				Number = i
			}
		}
		if (settings["SwitchOff${evt.deviceId}"]) {
			log.debug "Device that triggered the event: ${Changed.displayName} | Number = $Number | Actions = ${settings["SwitchOff${evt.deviceId}"]}"
			for ( def i = 0; i < settings["SwitchOff${evt.deviceId}"].size(); i++) {
				SelectionArray[i] = settings["SwitchOff${evt.deviceId}"][i].tokenize(":")
				SwitchSelection(SelectionArray[i],[evt.deviceId,null,Changed,"off",null])
			}
		}
	}
}

def DoAccelerationActive(evt) {
	if (allOk) {
		def Changed
		def Number
		def SelectionArray = [][]
		for ( def i = 0; i < Acceleration.size(); i++) {
			if ( evt.deviceId == Acceleration[i].id ) {
				Changed = Acceleration[i]
				Number = i
			}
		}
		if (settings["AccelerationActive${evt.deviceId}"]) {
			log.debug "Device that triggered the event: ${Changed.displayName} | Number = $Number | Actions = ${settings["AccelerationActive${evt.deviceId}"]}"
			for ( def i = 0; i < settings["AccelerationActive${evt.deviceId}"].size(); i++) {
				SelectionArray[i] = settings["AccelerationActive${evt.deviceId}"][i].tokenize(":")
				SwitchSelection(SelectionArray[i],[evt.deviceId,null,Changed,"active",null])
			}
		}
	}
}

def DoAccelerationInactive(evt) {
	if (allOk) {
		def Changed
		def Number
		def SelectionArray = [][]
		for ( def i = 0; i < Acceleration.size(); i++) {
			if ( evt.deviceId == Acceleration[i].id ) {
				Changed = Acceleration[i]
				Number = i
			}
		}
		if (settings["AccelerationInactive${evt.deviceId}"]) {
			log.debug "Device that triggered the event: ${Changed.displayName} | Number = $Number | Actions = ${settings["AccelerationInactive${evt.deviceId}"]}"
			for ( def i = 0; i < settings["AccelerationInactive${evt.deviceId}"].size(); i++) {
				SelectionArray[i] = settings["AccelerationInactive${evt.deviceId}"][i].tokenize(":")
				SwitchSelection(SelectionArray[i],[evt.deviceId,null,Changed,"inactive",null])
			}
		}
	}
}

def DoMotionActive(evt) {
	if (allOk) {
		def Changed
		def Number
		def SelectionArray = [][]
		for ( def i = 0; i < Motion.size(); i++) {
			if ( evt.deviceId == Motion[i].id ) {
				Changed = Motion[i]
				Number = i
			}
		}
		if (settings["MotionActive${evt.deviceId}"]) {
			log.debug "Device that triggered the event: ${Changed.displayName} | Number = $Number | Actions = ${settings["MotionActive${evt.deviceId}"]}"
			for ( def i = 0; i < settings["MotionActive${evt.deviceId}"].size(); i++) {
				SelectionArray[i] = settings["MotionActive${evt.deviceId}"][i].tokenize(":")
				SwitchSelection(SelectionArray[i],[evt.deviceId,null,Changed,"detected motion",null])
			}
		}
	}
}

def DoMotionInactive(evt) {
	if (allOk) {
		def Changed
		def Number
		def SelectionArray = [][]
		for ( def i = 0; i < Motion.size(); i++) {
			if ( evt.deviceId == Motion[i].id ) {
				Changed = Motion[i]
				Number = i
			}
		}
		if (settings["MotionInactive${evt.deviceId}"]) {
			log.debug "Device that triggered the event: ${Changed.displayName} | Number = $Number | Actions = ${settings["MotionInactive${evt.deviceId}"]}"
			for ( def i = 0; i < settings["MotionInactive${evt.deviceId}"].size(); i++) {
				SelectionArray[i] = settings["MotionInactive${evt.deviceId}"][i].tokenize(":")
				SwitchSelection(SelectionArray[i],[evt.deviceId,null,Changed,"motion has stopped",null])
			}
		}
	}
}

def DoTamperClear(evt) {
	if (allOk) {
		def Changed
		def Number
		def SelectionArray = [][]
		for ( def i = 0; i < Tamper.size(); i++) {
			if ( evt.deviceId == Tamper[i].id ) {
				Changed = Tamper[i]
				Number = i
			}
		}
		if (settings["TamperClear${evt.deviceId}"]) {
			log.debug "Device that triggered the event: ${Changed.displayName} | Number = $Number | Actions = ${settings["TamperClear${evt.deviceId}"]}"
			for ( def i = 0; i < settings["TamperClear${evt.deviceId}"].size(); i++) {
				SelectionArray[i] = settings["TamperClear${evt.deviceId}"][i].tokenize(":")
				SwitchSelection(SelectionArray[i],[evt.deviceId,null,Changed,"clear",null])
			}
		}
	}
}

def DoTamperDetected(evt) {
	if (allOk) {
		def Changed
		def Number
		def SelectionArray = [][]
		for ( def i = 0; i < Tamper.size(); i++) {
			if ( evt.deviceId == Tamper[i].id ) {
				Changed = Tamper[i]
				Number = i
			}
		}
		if (settings["TamperDetected${evt.deviceId}"]) {
			log.debug "Device that triggered the event: ${Changed.displayName} | Number = $Number | Actions = ${settings["TamperDetected${evt.deviceId}"]}"
			for ( def i = 0; i < settings["TamperDetected${evt.deviceId}"].size(); i++) {
				SelectionArray[i] = settings["TamperDetected${evt.deviceId}"][i].tokenize(":")
				SwitchSelection(SelectionArray[i],[evt.deviceId,null,Changed,"detected",null])
			}
		}
	}
}

def DoShockClear(evt) {
	if (allOk) {
		def Changed
		def Number
		def SelectionArray = [][]
		for ( def i = 0; i < Shock.size(); i++) {
			if ( evt.deviceId == Shock[i].id ) {
				Changed = Shock[i]
				Number = i
			}
		}
		if (settings["ShockClear${evt.deviceId}"]) {
			log.debug "Device that triggered the event: ${Changed.displayName} | Number = $Number | Actions = ${settings["ShockClear${evt.deviceId}"]}"
			for ( def i = 0; i < settings["ShockClear${evt.deviceId}"].size(); i++) {
				SelectionArray[i] = settings["ShockClear${evt.deviceId}"][i].tokenize(":")
				SwitchSelection(SelectionArray[i],[evt.deviceId,null,Changed,"clear",null])
			}
		}
	}
}

def DoShockDetected(evt) {
	if (allOk) {
		def Changed
		def Number
		def SelectionArray = [][]
		for ( def i = 0; i < Shock.size(); i++) {
			if ( evt.deviceId == Shock[i].id ) {
				Changed = Shock[i]
				Number = i
			}
		}
		if (settings["ShockDetected${evt.deviceId}"]) {
			log.debug "Device that triggered the event: ${Changed.displayName} | Number = $Number | Actions = ${settings["ShockDetected${evt.deviceId}"]}"
			for ( def i = 0; i < settings["ShockDetected${evt.deviceId}"].size(); i++) {
				SelectionArray[i] = settings["ShockDetected${evt.deviceId}"][i].tokenize(":")
				SwitchSelection(SelectionArray[i],[evt.deviceId,null,Changed,"detected",null])
			}
		}
	}
}

def DoSleepNotSleeping(evt) {
	if (allOk) {
		def Changed
		def Number
		def SelectionArray = [][]
		for ( def i = 0; i < Sleep.size(); i++) {
			if ( evt.deviceId == Sleep[i].id ) {
				Changed = Sleep[i]
				Number = i
			}
		}
		if (settings["SleepNotSleeping${evt.deviceId}"]) {
			log.debug "Device that triggered the event: ${Changed.displayName} | Number = $Number | Actions = ${settings["SleepNotSleeping${evt.deviceId}"]}"
			for ( def i = 0; i < settings["SleepNotSleeping${evt.deviceId}"].size(); i++) {
				SelectionArray[i] = settings["SleepNotSleeping${evt.deviceId}"][i].tokenize(":")
				SwitchSelection(SelectionArray[i],[evt.deviceId,null,Changed,"not sleeping",null])
			}
		}
	}
}

def DoSleepSleeping(evt) {
	if (allOk) {
		def Changed
		def Number
		def SelectionArray = [][]
		for ( def i = 0; i < Sleep.size(); i++) {
			if ( evt.deviceId == Sleep[i].id ) {
				Changed = Sleep[i]
				Number = i
			}
		}
		if (settings["SleepSleeping${evt.deviceId}"]) {
			log.debug "Device that triggered the event: ${Changed.displayName} | Number = $Number | Actions = ${settings["SleepSleeping${evt.deviceId}"]}"
			for ( def i = 0; i < settings["SleepSleeping${evt.deviceId}"].size(); i++) {
				SelectionArray[i] = settings["SleepSleeping${evt.deviceId}"][i].tokenize(":")
				SwitchSelection(SelectionArray[i],[evt.deviceId,null,Changed,"sleeping",null])
			}
		}
	}
}

def DoSoundDetected(evt) {
	if (allOk) {
		def Changed
		def Number
		def SelectionArray = [][]
		for ( def i = 0; i < Sound.size(); i++) {
			if ( evt.deviceId == Sound[i].id ) {
				Changed = Sound[i]
				Number = i
			}
		}
		if (settings["SoundDetected${evt.deviceId}"]) {
			log.debug "Device that triggered the event: ${Changed.displayName} | Number = $Number | Actions = ${settings["SoundDetected${evt.deviceId}"]}"
			for ( def i = 0; i < settings["SoundDetected${evt.deviceId}"].size(); i++) {
				SelectionArray[i] = settings["SoundDetected${evt.deviceId}"][i].tokenize(":")
				SwitchSelection(SelectionArray[i],[evt.deviceId,null,Changed,"detected",null])
			}
		}
	}
}

def DoSoundNotDetected(evt) {
	if (allOk) {
		def Changed
		def Number
		def SelectionArray = [][]
		for ( def i = 0; i < Sound.size(); i++) {
			if ( evt.deviceId == Sound[i].id ) {
				Changed = Sound[i]
				Number = i
			}
		}
		if (settings["SoundNotDetected${evt.deviceId}"]) {
			log.debug "Device that triggered the event: ${Changed.displayName} | Number = $Number | Actions = ${settings["SoundNotDetected${evt.deviceId}"]}"
			for ( def i = 0; i < settings["SoundNotDetected${evt.deviceId}"].size(); i++) {
				SelectionArray[i] = settings["SoundNotDetected${evt.deviceId}"][i].tokenize(":")
				SwitchSelection(SelectionArray[i],[evt.deviceId,null,Changed,"not detected",null])
			}
		}
	}
}

def DoTouched(evt) {
	if (allOk) {
		def Changed
		def Number
		def SelectionArray = [][]
		for ( def i = 0; i < Touch.size(); i++) {
			if ( evt.deviceId == Touch[i].id ) {
				Changed = Touch[i]
				Number = i
			}
		}
		if (settings["Touched${evt.deviceId}"]) {
			log.debug "Device that triggered the event: ${Changed.displayName} | Number = $Number | Actions = ${settings["Touched${evt.deviceId}"]}"
			for ( def i = 0; i < settings["Touched${evt.deviceId}"].size(); i++) {
				SelectionArray[i] = settings["Touched${evt.deviceId}"][i].tokenize(":")
				SwitchSelection(SelectionArray[i],[evt.deviceId,null,Changed,"touched",null])
			}
		}
	}
}

def DoWaterDry(evt) {
	if (allOk) {
		def Changed
		def Number
		def SelectionArray = [][]
		for ( def i = 0; i < Water.size(); i++) {
			if ( evt.deviceId == Water[i].id ) {
				Changed = Water[i]
				Number = i
			}
		}
		if (settings["WaterDry${evt.deviceId}"]) {
			log.debug "Device that triggered the event: ${Changed.displayName} | Number = $Number | Actions = ${settings["WaterDry${evt.deviceId}"]}"
			for ( def i = 0; i < settings["WaterDry${evt.deviceId}"].size(); i++) {
				SelectionArray[i] = settings["WaterDry${evt.deviceId}"][i].tokenize(":")
				SwitchSelection(SelectionArray[i],[evt.deviceId,null,Changed,"dry",null])
			}
		}
	}
}

def DoWaterWet(evt) {
	if (allOk) {
		def Changed
		def Number
		def SelectionArray = [][]
		for ( def i = 0; i < Water.size(); i++) {
			if ( evt.deviceId == Water[i].id ) {
				Changed = Water[i]
				Number = i
			}
		}
		if (settings["WaterWet${evt.deviceId}"]) {
			log.debug "Device that triggered the event: ${Changed.displayName} | Number = $Number | Actions = ${settings["WaterWet${evt.deviceId}"]}"
			for ( def i = 0; i < settings["WaterWet${evt.deviceId}"].size(); i++) {
				SelectionArray[i] = settings["WaterWet${evt.deviceId}"][i].tokenize(":")
				SwitchSelection(SelectionArray[i],[evt.deviceId,null,Changed,"wet",null])
			}
		}
	}
}

def DoBeaconNotPresent(evt) {
	if (allOk) {
		def Changed
		def Number
		def SelectionArray = [][]
		for ( def i = 0; i < Beacon.size(); i++) {
			if ( evt.deviceId == Beacon[i].id ) {
				Changed = Beacon[i]
				Number = i
			}
		}
		if (settings["BeaconNotPresent${evt.deviceId}"]) {
			log.debug "Device that triggered the event: ${Changed.displayName} | Number = $Number | Actions = ${settings["BeaconNotPresent${evt.deviceId}"]}"
			for ( def i = 0; i < settings["BeaconNotPresent${evt.deviceId}"].size(); i++) {
				SelectionArray[i] = settings["BeaconNotPresent${evt.deviceId}"][i].tokenize(":")
				SwitchSelection(SelectionArray[i],[evt.deviceId,null,Changed,"has left",null])
			}
		}
		if (settings["BeaconAllAway"]) {
			boolean AllAway = true
			Beacon.each {if (it.currentValue("presence") == "present") {AllAway = false}}
			if (AllAway) {
				log.debug "Device that triggered the event: ${Changed.displayName} | Number = $Number | Actions = $BeaconAllAway"
				for ( def i = 0; i < BeaconAllAway.size(); i++) {
					SelectionArray[i] = BeaconAllAway[i].tokenize(":")
					SwitchSelection(SelectionArray[i],[evt.deviceId,null,Changed,"has left",null])
				}
			}
		}
	}
}

def DoBeaconPresent(evt) {
	if (allOk) {
		def Changed
		def Number
		def SelectionArray = [][]
		for ( def i = 0; i < Beacon.size(); i++) {
			if ( evt.deviceId == Beacon[i].id ) {
				Changed = Beacon[i]
				Number = i
			}
		}
		if (settings["BeaconPresent${evt.deviceId}"]) {
			log.debug "Device that triggered the event: ${Changed.displayName} | Number = $Number | Actions = ${settings["BeaconPresent${evt.deviceId}"]}"
			for ( def i = 0; i < settings["BeaconPresent${evt.deviceId}"].size(); i++) {
				SelectionArray[i] = settings["BeaconPresent${evt.deviceId}"][i].tokenize(":")
				SwitchSelection(SelectionArray[i],[evt.deviceId,null,Changed,"has arrived",null])
			}
		}
		if (settings["BeaconAllPresent"]) {
			boolean AllPresent = true
			Beacon.each {if (it.currentValue("presence") != "present") {AllPresent = false}}
			if (AllPresent) {
				log.debug "Device that triggered the event: ${Changed.displayName} | Number = $Number | Actions = $PresenceAllPresent"
				for ( def i = 0; i < BeaconAllPresent.size(); i++) {
					SelectionArray[i] = BeaconAllPresent[i].tokenize(":")
					SwitchSelection(SelectionArray[i],[evt.deviceId,null,Changed,"has arrived",null])
				}
			}
		}
	}
}

def DoPresenceNotPresent(evt) {
	if (allOk) {
		def Changed
		def Number
		def SelectionArray = [][]
		for ( def i = 0; i < Presence.size(); i++) {
			if ( evt.deviceId == Presence[i].id ) {
				Changed = Presence[i]
				Number = i
			}
		}
		if (settings["PresenceNotPresent${evt.deviceId}"]) {
			log.debug "Device that triggered the event: ${Changed.displayName} | Number = $Number | Actions = ${settings["PresenceNotPresent${evt.deviceId}"]}"
			for ( def i = 0; i < settings["PresenceNotPresent${evt.deviceId}"].size(); i++) {
				SelectionArray[i] = settings["PresenceNotPresent${evt.deviceId}"][i].tokenize(":")
				SwitchSelection(SelectionArray[i],[evt.deviceId,null,Changed,"has left",null])
			}
		}
		if (settings["PresenceAllAway"]) {
			boolean AllAway = true
			Presence.each {if (it.currentValue("presence") == "present") {AllAway = false}}
			if (AllAway) {
				log.debug "Device that triggered the event: ${Changed.displayName} | Number = $Number | Actions = $PresenceAllAway"
				for ( def i = 0; i < PresenceAllAway.size(); i++) {
					SelectionArray[i] = PresenceAllAway[i].tokenize(":")
					SwitchSelection(SelectionArray[i],[evt.deviceId,null,Changed,"has left",null])
				}
			}
		}
	}
}

def DoPresencePresent(evt) {
	if (allOk) {
		def Changed
		def Number
		def SelectionArray = [][]
		for ( def i = 0; i < Presence.size(); i++) {
			if ( evt.deviceId == Presence[i].id ) {
				Changed = Presence[i]
				Number = i
			}
		}
		if (settings["PresencePresent${evt.deviceId}"]) {
			log.debug "Found the device that triggered the event: ${Changed.displayName} | Number = $Number | Actions = ${settings["PresencePresent${evt.deviceId}"]}"
			for ( def i = 0; i < settings["PresencePresent${evt.deviceId}"].size(); i++) {
				SelectionArray[i] = settings["PresencePresent${evt.deviceId}"][i].tokenize(":")
				SwitchSelection(SelectionArray[i],[evt.deviceId,null,Changed,"has arrived",null])
			}
		}
		if (settings["PresenceAllPresent"]) {
			boolean AllPresent = true
			Presence.each {if (it.currentValue("presence") != "present") {AllPresent = false}}
			if (AllPresent) {
				log.debug "Device that triggered the event: ${Changed.displayName} | Number = $Number | Actions = $PresenceAllPresent"
				for ( def i = 0; i < PresenceAllPresent.size(); i++) {
					SelectionArray[i] = PresenceAllPresent[i].tokenize(":")
					SwitchSelection(SelectionArray[i],[evt.deviceId,null,Changed,"has arrived",null])
				}
			}
		}
	}
}

def DoCODetectorClear(evt) {
	if (allOk) {
		def Changed
		def Number
		def SelectionArray = [][]
		for ( def i = 0; i < CODetector.size(); i++) {
			if ( evt.deviceId == CODetector[i].id ) {
				Changed = CODetector[i]
				Number = i
			}
		}
		if (settings["CODetectorClear${evt.deviceId}"]) {
			log.debug "Device that triggered the event: ${Changed.displayName} | Number = $Number | Actions = ${settings["CODetectorClear${evt.deviceId}"]}"
			for ( def i = 0; i < settings["CODetectorClear${evt.deviceId}"].size(); i++) {
				SelectionArray[i] = settings["CODetectorClear${evt.deviceId}"][i].tokenize(":")
				SwitchSelection(SelectionArray[i],[evt.deviceId,null,Changed,"clear",null])
			}
		}
	}
}

def DoCODetectorDetected(evt) {
	if (allOk) {
		def Changed
		def Number
		def SelectionArray = [][]
		for ( def i = 0; i < CODetector.size(); i++) {
			if ( evt.deviceId == CODetector[i].id ) {
				Changed = CODetector[i]
				Number = i
			}
		}
		if (settings["CODetectorDetected${evt.deviceId}"]) {
			log.debug "Device that triggered the event: ${Changed.displayName} | Number = $Number | Actions = ${settings["CODetectorDetected${evt.deviceId}"]}"
			for ( def i = 0; i < settings["CODetectorDetected${evt.deviceId}"].size(); i++) {
				SelectionArray[i] = settings["CODetectorDetected${evt.deviceId}"][i].tokenize(":")
				SwitchSelection(SelectionArray[i],[evt.deviceId,null,Changed,"detected",null])
			}
		}
	}
}

def DoCODetectorTested(evt) {
	if (allOk) {
		def Changed
		def Number
		def SelectionArray = [][]
		for ( def i = 0; i < CODetector.size(); i++) {
			if ( evt.deviceId == CODetector[i].id ) {
				Changed = CODetector[i]
				Number = i
			}
		}
		if (settings["CODetectorTested${evt.deviceId}"]) {
			log.debug "Device that triggered the event: ${Changed.displayName} | Number = $Number | Actions = ${settings["CODetectorTested${evt.deviceId}"]}"
			for ( def i = 0; i < settings["CODetectorTested${evt.deviceId}"].size(); i++) {
				SelectionArray[i] = settings["CODetectorTested${evt.deviceId}"][i].tokenize(":")
				SwitchSelection(SelectionArray[i],[evt.deviceId,null,Changed,"tested",null])
			}
		}
	}
}

def DoSmokeClear(evt) {
	if (allOk) {
		def Changed
		def Number
		def SelectionArray = [][]
		for ( def i = 0; i < Smoke.size(); i++) {
			if ( evt.deviceId == Smoke[i].id ) {
				Changed = Smoke[i]
				Number = i
			}
		}
		if (settings["SmokeClear${evt.deviceId}"]) {
			log.debug "Device that triggered the event: ${Changed.displayName} | Number = $Number | Actions = ${settings["SmokeClear${evt.deviceId}"]}"
			for ( def i = 0; i < settings["SmokeClear${evt.deviceId}"].size(); i++) {
				SelectionArray[i] = settings["SmokeClear${evt.deviceId}"][i].tokenize(":")
				SwitchSelection(SelectionArray[i],[evt.deviceId,null,Changed,"clear",null])
			}
		}
	}
}

def DoSmokeDetected(evt) {
	if (allOk) {
		def Changed
		def Number
		def SelectionArray = [][]
		for ( def i = 0; i < Smoke.size(); i++) {
			if ( evt.deviceId == Smoke[i].id ) {
				Changed = Smoke[i]
				Number = i
			}
		}
		if (settings["SmokeDetected${evt.deviceId}"]) {
			log.debug "Device that triggered the event: ${Changed.displayName} | Number = $Number | Actions = ${settings["SmokeDetected${evt.deviceId}"]}"
			for ( def i = 0; i < settings["SmokeDetected${evt.deviceId}"].size(); i++) {
				SelectionArray[i] = settings["SmokeDetected${evt.deviceId}"][i].tokenize(":")
				SwitchSelection(SelectionArray[i],[evt.deviceId,null,Changed,"detected",null])
			}
		}
	}
}

def DoSmokeTested(evt) {
	if (allOk) {
		def Changed
		def Number
		def SelectionArray = [][]
		for ( def i = 0; i < Smoke.size(); i++) {
			if ( evt.deviceId == Smoke[i].id ) {
				Changed = Smoke[i]
				Number = i
			}
		}
		if (settings["SmokeTested${evt.deviceId}"]) {
			log.debug "Device that triggered the event: ${Changed.displayName} | Number = $Number | Actions = ${settings["SmokeTested${evt.deviceId}"]}"
			for ( def i = 0; i < settings["SmokeTested${evt.deviceId}"].size(); i++) {
				SelectionArray[i] = settings["SmokeTested${evt.deviceId}"][i].tokenize(":")
				SwitchSelection(SelectionArray[i],[evt.deviceId,null,Changed,"tested",null])
			}
		}
	}
}

def DoPowerSourceBattery(evt) {
	if (allOk) {
		def Changed
		def Number
		def SelectionArray = [][]
		for ( def i = 0; i < PowerSource.size(); i++) {
			if ( evt.deviceId == PowerSource[i].id ) {
				Changed = PowerSource[i]
				Number = i
			}
		}
		if (settings["PowerSourceBattery${evt.deviceId}"]) {
			log.debug "Device that triggered the event: ${Changed.displayName} | Number = $Number | Actions = ${settings["PowerSourceBattery${evt.deviceId}"]}"
			for ( def i = 0; i < settings["PowerSourceBattery${evt.deviceId}"].size(); i++) {
				SelectionArray[i] = settings["PowerSourceBattery${evt.deviceId}"][i].tokenize(":")
				SwitchSelection(SelectionArray[i],[evt.deviceId,null,Changed,"battery",null])
			}
		}
	}
}

def DoPowerSourceDC(evt) {
	if (allOk) {
		def Changed
		def Number
		def SelectionArray = [][]
		for ( def i = 0; i < PowerSource.size(); i++) {
			if ( evt.deviceId == PowerSource[i].id ) {
				Changed = PowerSource[i]
				Number = i
			}
		}
		if (settings["PowerSourceDC${evt.deviceId}"]) {
			log.debug "Device that triggered the event: ${Changed.displayName} | Number = $Number | Actions = ${settings["PowerSourceDC${evt.deviceId}"]}"
			for ( def i = 0; i < settings["PowerSourceDC${evt.deviceId}"].size(); i++) {
				SelectionArray[i] = settings["PowerSourceDC${evt.deviceId}"][i].tokenize(":")
				SwitchSelection(SelectionArray[i],[evt.deviceId,null,Changed,"dc",null])
			}
		}
	}
}

def DoPowerSourceMains(evt) {
	if (allOk) {
		def Changed
		def Number
		def SelectionArray = [][]
		for ( def i = 0; i < PowerSource.size(); i++) {
			if ( evt.deviceId == PowerSource[i].id ) {
				Changed = PowerSource[i]
				Number = i
			}
		}
		if (settings["PowerSourceMains${evt.deviceId}"]) {
			log.debug "Device that triggered the event: ${Changed.displayName} | Number = $Number | Actions = ${settings["PowerSourceMains${evt.deviceId}"]}"
			for ( def i = 0; i < settings["PowerSourceMains${evt.deviceId}"].size(); i++) {
				SelectionArray[i] = settings["PowerSourceMains${evt.deviceId}"][i].tokenize(":")
				SwitchSelection(SelectionArray[i],[evt.deviceId,null,Changed,"mains",null])
			}
		}
	}
}

def DoPowerSourceUnknown(evt) {
	if (allOk) {
		def Changed
		def Number
		def SelectionArray = [][]
		for ( def i = 0; i < PowerSource.size(); i++) {
			if ( evt.deviceId == PowerSource[i].id ) {
				Changed = PowerSource[i]
				Number = i
			}
		}
		if (settings["PowerSourceUnknown${evt.deviceId}"]) {
			log.debug "Device that triggered the event: ${Changed.displayName} | Number = $Number | Actions = ${settings["PowerSourceUnknown${evt.deviceId}"]}"
			for ( def i = 0; i < settings["PowerSourceUnknown${evt.deviceId}"].size(); i++) {
				SelectionArray[i] = settings["PowerSourceUnknown${evt.deviceId}"][i].tokenize(":")
				SwitchSelection(SelectionArray[i],[evt.deviceId,null,Changed,"unknown",null])
			}
		}
	}
}

def DoDoorClosed(evt) {
	if (allOk) {
		def Changed
		def Number
		def SelectionArray = [][]
		for ( def i = 0; i < Door.size(); i++) {
			if ( evt.deviceId == Door[i].id ) {
				Changed = Door[i]
				Number = i
			}
		}
		if (settings["DoorClosed${evt.deviceId}"]) {
			log.debug "Device that triggered the event: ${Changed.displayName} | Number = $Number | Actions = ${settings["DoorClosed${evt.deviceId}"]}"
			for ( def i = 0; i < settings["DoorClosed${evt.deviceId}"].size(); i++) {
				SelectionArray[i] = settings["DoorClosed${evt.deviceId}"][i].tokenize(":")
				SwitchSelection(SelectionArray[i],[evt.deviceId,null,Changed,"closed",null])
			}
		}
	}
}

def DoDoorOpen(evt) {
	if (allOk) {
		def Changed
		def Number
		def SelectionArray = [][]
		for ( def i = 0; i < Door.size(); i++) {
			if ( evt.deviceId == Door[i].id ) {
				Changed = Door[i]
				Number = i
			}
		}
		if (settings["DoorOpen${evt.deviceId}"]) {
			log.debug "Device that triggered the event: ${Changed.displayName} | Number = $Number | Actions = ${settings["DoorOpen${evt.deviceId}"]}"
			for ( def i = 0; i < settings["DoorOpen${evt.deviceId}"].size(); i++) {
				SelectionArray[i] = settings["DoorOpen${evt.deviceId}"][i].tokenize(":")
				SwitchSelection(SelectionArray[i],[evt.deviceId,null,Changed,"open",null])
			}
		}
	}
}

def DoDoorUnknown(evt) {
	if (allOk) {
		def Changed
		def Number
		def SelectionArray = [][]
		for ( def i = 0; i < Door.size(); i++) {
			if ( evt.deviceId == Door[i].id ) {
				Changed = Door[i]
				Number = i
			}
		}
		if (settings["DoorUnknown${evt.deviceId}"]) {
			log.debug "Device that triggered the event: ${Changed.displayName} | Number = $Number | Actions = ${settings["DoorUnknown${evt.deviceId}"]}"
			for ( def i = 0; i < settings["DoorUnknown${evt.deviceId}"].size(); i++) {
				SelectionArray[i] = settings["DoorUnknown${evt.deviceId}"][i].tokenize(":")
				SwitchSelection(SelectionArray[i],[evt.deviceId,null,Changed,"unknown",null])
			}
		}
	}
}

def DoValveClosed(evt) {
	if (allOk) {
		def Changed
		def Number
		def SelectionArray = [][]
		for ( def i = 0; i < Valve.size(); i++) {
			if ( evt.deviceId == Valve[i].id ) {
				Changed = Valve[i]
				Number = i
			}
		}
		if (settings["ValveClosed${evt.deviceId}"]) {
			log.debug "Device that triggered the event: ${Changed.displayName} | Number = $Number | Actions = ${settings["ValveClosed${evt.deviceId}"]}"
			for ( def i = 0; i < settings["ValveClosed${evt.deviceId}"].size(); i++) {
				SelectionArray[i] = settings["ValveClosed${evt.deviceId}"][i].tokenize(":")
				SwitchSelection(SelectionArray[i],[evt.deviceId,null,Changed,"closed",null])
			}
		}
	}
}

def DoValveOpen(evt) {
	if (allOk) {
		def Changed
		def Number
		def SelectionArray = [][]
		for ( def i = 0; i < Valve.size(); i++) {
			if ( evt.deviceId == Valve[i].id ) {
				Changed = Valve[i]
				Number = i
			}
		}
		if (settings["ValveOpen${evt.deviceId}"]) {
			log.debug "Device that triggered the event: ${Changed.displayName} | Number = $Number | Actions = ${settings["ValveOpen${evt.deviceId}"]}"
			for ( def i = 0; i < settings["ValveOpen${evt.deviceId}"].size(); i++) {
				SelectionArray[i] = settings["ValveOpen${evt.deviceId}"][i].tokenize(":")
				SwitchSelection(SelectionArray[i],[evt.deviceId,null,Changed,"open",null])
			}
		}
	}
}

def DoShadeClosed(evt) {
	if (allOk) {
		def Changed
		def Number
		def SelectionArray = [][]
		for ( def i = 0; i < Shade.size(); i++) {
			if ( evt.deviceId == Shade[i].id ) {
				Changed = Shade[i]
				Number = i
			}
		}
		if (settings["ShadeClosed${evt.deviceId}"]) {
			log.debug "Device that triggered the event: ${Changed.displayName} | Number = $Number | Actions = ${settings["ShadeClosed${evt.deviceId}"]}"
			for ( def i = 0; i < settings["ShadeClosed${evt.deviceId}"].size(); i++) {
				SelectionArray[i] = settings["ShadeClosed${evt.deviceId}"][i].tokenize(":")
				SwitchSelection(SelectionArray[i],[evt.deviceId,null,Changed,"closed",null])
			}
		}
	}
}

def DoShadeOpen(evt) {
	if (allOk) {
		def Changed
		def Number
		def SelectionArray = [][]
		for ( def i = 0; i < Shade.size(); i++) {
			if ( evt.deviceId == Shade[i].id ) {
				Changed = Shade[i]
				Number = i
			}
		}
		if (settings["ShadeOpen${evt.deviceId}"]) {
			log.debug "Device that triggered the event: ${Changed.displayName} | Number = $Number | Actions = ${settings["ShadeOpen${evt.deviceId}"]}"
			for ( def i = 0; i < settings["ShadeOpen${evt.deviceId}"].size(); i++) {
				SelectionArray[i] = settings["ShadeOpen${evt.deviceId}"][i].tokenize(":")
				SwitchSelection(SelectionArray[i],[evt.deviceId,null,Changed,"open",null])
			}
		}
	}
}

def DoShadePartiallyOpen(evt) {
	if (allOk) {
		def Changed
		def Number
		def SelectionArray = [][]
		for ( def i = 0; i < Shade.size(); i++) {
			if ( evt.deviceId == Shade[i].id ) {
				Changed = Shade[i]
				Number = i
			}
		}
		if (settings["ShadePartiallyOpen${evt.deviceId}"]) {
			log.debug "Device that triggered the event: ${Changed.displayName} | Number = $Number | Actions = ${settings["ShadePartiallyOpen${evt.deviceId}"]}"
			for ( def i = 0; i < settings["ShadePartiallyOpen${evt.deviceId}"].size(); i++) {
				SelectionArray[i] = settings["ShadePartiallyOpen${evt.deviceId}"][i].tokenize(":")
				SwitchSelection(SelectionArray[i],[evt.deviceId,null,Changed,"partially open",null])
			}
		}
	}
}

def DoShadeUnknown(evt) {
	if (allOk) {
		def Changed
		def Number
		def SelectionArray = [][]
		for ( def i = 0; i < Shade.size(); i++) {
			if ( evt.deviceId == Shade[i].id ) {
				Changed = Shade[i]
				Number = i
			}
		}
		if (settings["ShadeUnknown${evt.deviceId}"]) {
			log.debug "Device that triggered the event: ${Changed.displayName} | Number = $Number | Actions = ${settings["ShadeUnknown${evt.deviceId}"]}"
			for ( def i = 0; i < settings["ShadeUnknown${evt.deviceId}"].size(); i++) {
				SelectionArray[i] = settings["ShadeUnknown${evt.deviceId}"][i].tokenize(":")
				SwitchSelection(SelectionArray[i],[evt.deviceId,null,Changed,"unknown",null])
			}
		}
	}
}

def DoTemperature(evt) {
	if (allOk) {
		def Changed
		def Number
		def SelectionArray = [][]
		for ( def i = 0; i < Temperature.size(); i++) {
			if ( evt.deviceId == Temperature[i].id ) {
				Changed = Temperature[i]
				Number = i
			}
		}
		int CurrentTemp = Changed.currentValue("temperature") as Integer
		if (settings["HighActions${evt.deviceId}"]) {
			int HighSetting = settings["HighThreshold${evt.deviceId}"] as Integer
			if (CurrentTemp > HighSetting) {
				log.debug "Device that triggered the event: ${Changed.displayName} | Number = $Number | Actions = ${settings["HighThreshold${evt.deviceId}"]}"
				for ( def i = 0; i < settings["HighActions${evt.deviceId}"].size(); i++) {
					SelectionArray[i] = settings["HighActions${evt.deviceId}"][i].tokenize(":")
					SwitchSelection(SelectionArray[i],[evt.deviceId,null,Changed,CurrentTemp,null])
				}
			}
		}
		if (settings["LowActions${evt.deviceId}"]) {
			int LowSetting = settings["LowThreshold${evt.deviceId}"] as Integer
			if (CurrentTemp < LowSetting) {
				log.debug "Device that triggered the event: ${Changed.displayName} | Number = $Number | Actions = ${settings["LowThreshold${evt.deviceId}"]}"
				for ( def i = 0; i < settings["LowActions${evt.deviceId}"].size(); i++) {
					SelectionArray[i] = settings["LowActions${evt.deviceId}"][i].tokenize(":")
					SwitchSelection(SelectionArray[i],[evt.deviceId,null,Changed,CurrentTemp,null])
				}
			}
		}
		if (AllHighActions) {
			int HighSetting = AllHighThreshold as Integer
			if (CurrentTemp > HighSetting) {
				log.debug "Device that triggered the event: ${Changed.displayName} | Number = $Number | Actions = $AllHighActions"
				for ( def i = 0; i < AllHighActions.size(); i++) {
					SelectionArray[i] = AllHighActions[i].tokenize(":")
					SwitchSelection(SelectionArray[i],[evt.deviceId,null,Changed,CurrentTemp,null])
				}
			}
		}
		if (AllLowActions) {
			int LowSetting = AllLowThreshold as Integer
			if (CurrentTemp < LowSetting) {
				log.debug "Device that triggered the event: ${Changed.displayName} | Number = $Number | Actions = $AllLowActions"
				for ( def i = 0; i < AllLowActions.size(); i++) {
					SelectionArray[i] = AllLowActions[i].tokenize(":")
					SwitchSelection(SelectionArray[i],[evt.deviceId,null,Changed,CurrentTemp,null])
				}
			}
		}
	}
}

def DoPowerMeter(evt) {
	if (allOk) {
		def Changed
		def Number
		def SelectionArray = [][]
		for ( def i = 0; i < PowerMeter.size(); i++) {
			if ( evt.deviceId == PowerMeter[i].id ) {
				Changed = PowerMeter[i]
				Number = i
			}
		}
		int Currentstate = Changed.currentValue("power") as Integer
		if (settings["PowerMeterHighActions${evt.deviceId}"]) {
			int HighSetting = settings["PowerMeterHighThreshold${evt.deviceId}"] as Integer
			if (Currentstate > HighSetting) {
				log.debug "Device that triggered the event: ${Changed.displayName} | Number = $Number | Actions = ${settings["PowerMeterHighActions${evt.deviceId}"]}"
				for ( def i = 0; i < settings["PowerMeterHighActions${evt.deviceId}"].size(); i++) {
					SelectionArray[i] = settings["PowerMeterHighActions${evt.deviceId}"][i].tokenize(":")
					SwitchSelection(SelectionArray[i],[evt.deviceId,null,Changed,Currentstate,null])
				}
			}
		}
		if (settings["PowerMeterLowActions${evt.deviceId}"]) {
			int LowSetting = settings["PowerMeterLowThreshold${evt.deviceId}"] as Integer
			if (Currentstate < LowSetting) {
				log.debug "Device that triggered the event: ${Changed.displayName} | Number = $Number | Actions = ${settings["PowerMeterLowActions${evt.deviceId}"]}"
				for ( def i = 0; i < settings["PowerMeterLowActions${evt.deviceId}"].size(); i++) {
					SelectionArray[i] = settings["PowerMeterLowActions${evt.deviceId}"][i].tokenize(":")
					SwitchSelection(SelectionArray[i],[evt.deviceId,null,Changed,Currentstate,null])
				}
			}
		}
		if (AllPowerMeterHighActions) {
			int HighSetting = AllPowerMeterHighThreshold as Integer
			if (Currentstate > HighSetting) {
				log.debug "Device that triggered the event: ${Changed.displayName} | Number = $Number | Actions = $AllPowerMeterHighActions"
				for ( def i = 0; i < AllPowerMeterHighActions.size(); i++) {
					SelectionArray[i] = AllPowerMeterHighActions[i].tokenize(":")
					SwitchSelection(SelectionArray[i],[evt.deviceId,null,Changed,Currentstate,null])
				}
			}
		}
		if (AllPowerMeterLowActions) {
			int LowSetting = AllPowerMeterLowThreshold as Integer
			if (Currentstate < LowSetting) {
				log.debug "Device that triggered the event: ${Changed.displayName} | Number = $Number | Actions = $AllPowerMeterLowActions"
				for ( def i = 0; i < AllPowerMeterLowActions.size(); i++) {
					SelectionArray[i] = AllPowerMeterLowActions[i].tokenize(":")
					SwitchSelection(SelectionArray[i],[evt.deviceId,null,Changed,Currentstate,null])
				}
			}
		}
	}
}

def DoBattery(evt) {
	if (allOk) {
		def Changed
		def Number
		def SelectionArray = [][]
		for ( def i = 0; i < Battery.size(); i++) {
			if ( evt.deviceId == Battery[i].id ) {
				Changed = Battery[i]
				Number = i
			}
		}
		int Currentstate = Changed.currentValue("battery") as Integer
		if (settings["BatteryLowActions${evt.deviceId}"]) {
			int LowSetting = settings["BatteryLowThreshold${evt.deviceId}"] as Integer
			if (Currentstate < LowSetting) {
				log.debug "Device that triggered the event: ${Changed.displayName} | Number = $Number | Actions = ${settings["BatteryLowActions${evt.deviceId}"]}"
				if (SendBatteryPush) sendPush("$Changed battery level is $Currentstate which is below the threshold you have set")
				if (SendBatterySMSPhone != null) {
					if ( SendBatterySMSPhone.indexOf(";") > 1){
							def SendBatterySMSPhone = SendBatterySMSPhone.split(";")
							for ( def i = 0; i < SendBatterySMSPhone.size(); i++) {
								log.debug "Sending an SMS to ${SendBatterySMSPhone[i]}"
								sendSms(SendBatterySMSPhone[i] as String, SMSMessage ?:"Siren & Strobe activated")
							}
						} else {
							log.debug "Sending an SMS to ${SendBatterySMSPhone}"
							sendSms(SendBatterySMSPhone as String, "$Changed battery level is $Currentstate which is below the threshold you have set")
					}
				}
				for ( def i = 0; i < settings["BatteryLowActions${evt.deviceId}"].size(); i++) {
					SelectionArray[i] = settings["BatteryLowActions${evt.deviceId}"][i].tokenize(":")
					SwitchSelection(SelectionArray[i],[evt.deviceId,null,Changed,Currentstate,null])
				}
			}
		}
		if (AllBatteryLowActions) {
			int LowSetting = AllBatteryLowThreshold as Integer
			if (Currentstate < LowSetting) {
				log.debug "Device that triggered the event: ${Changed.displayName} | Number = $Number | Actions = $AllBatteryLowActions}"
				if (SendBatteryPush) sendPush("$Changed battery level is $Currentstate which is below the threshold you have set")
				if (SendBatterySMSPhone != null) {
					if ( SendBatterySMSPhone.indexOf(";") > 1){
							def SendBatterySMSPhone = SendBatterySMSPhone.split(";")
							for ( def i = 0; i < SendBatterySMSPhone.size(); i++) {
								log.debug "Sending an SMS to ${SendBatterySMSPhone[i]}"
								sendSms(SendBatterySMSPhone[i] as String, SMSMessage ?:"Siren & Strobe activated")
							}
						} else {
							log.debug "Sending an SMS to ${SendBatterySMSPhone}"
							sendSms(SendBatterySMSPhone as String, "$Changed battery level is $Currentstate which is below the threshold you have set")
					}
				}
				for ( def i = 0; i < AllBatteryLowActions.size(); i++) {
					SelectionArray[i] = AllBatteryLowActions[i].tokenize(":")
					SwitchSelection(SelectionArray[i],[evt.deviceId,null,Changed,Currentstate,null])
				}
			}
		}
	}
}

def DoVoltage(evt) {
	if (allOk) {
		def Changed
		def Number
		def SelectionArray = [][]
		for ( def i = 0; i < Voltage.size(); i++) {
			if ( evt.deviceId == Voltage[i].id ) {
				Changed = Voltage[i]
				Number = i
			}
		}
		int Currentstate = Changed.currentValue("voltage") as Integer
		if (settings["VoltageHighActions${evt.deviceId}"]) {
			int HighSetting = settings["VoltageHighThreshold${evt.deviceId}"] as Integer
			if (Currentstate > HighSetting) {
				log.debug "Device that triggered the event: ${Changed.displayName} | Number = $Number | Actions = ${settings["VoltageHighActions${evt.deviceId}"]}"
				for ( def i = 0; i < settings["VoltageHighActions${evt.deviceId}"].size(); i++) {
					SelectionArray[i] = settings["VoltageHighActions${evt.deviceId}"][i].tokenize(":")
					SwitchSelection(SelectionArray[i],[evt.deviceId,null,Changed,Currentstate,null])
				}
			}
		}
		if (settings["VoltageLowActions${evt.deviceId}"]) {
			int LowSetting = settings["VoltageLowThreshold${evt.deviceId}"] as Integer
			if (Currentstate < LowSetting) {
				log.debug "Device that triggered the event: ${Changed.displayName} | Number = $Number | Actions = ${settings["VoltageLowActions${evt.deviceId}"]}"
				for ( def i = 0; i < settings["VoltageLowActions${evt.deviceId}"].size(); i++) {
					SelectionArray[i] = settings["VoltageLowActions${evt.deviceId}"][i].tokenize(":")
					SwitchSelection(SelectionArray[i],[evt.deviceId,null,Changed,Currentstate,null])
				}
			}
		}
		if (AllVoltageHighActions) {
			int HighSetting = AllVoltageHighThreshold as Integer
			if (Currentstate > HighSetting) {
				log.debug "Device that triggered the event: ${Changed.displayName} | Number = $Number | Actions = $AllVoltageHighActions"
				for ( def i = 0; i < AllVoltageHighActions.size(); i++) {
					SelectionArray[i] = AllVoltageHighActions[i].tokenize(":")
					SwitchSelection(SelectionArray[i],[evt.deviceId,null,Changed,Currentstate,null])
				}
			}
		}
		if (AllVoltageLowActions) {
			int LowSetting = AllVoltageLowThreshold as Integer
			if (Currentstate < LowSetting) {
				log.debug "Device that triggered the event: ${Changed.displayName} | Number = $Number | Actions = $AllVoltageLowActions"
				for ( def i = 0; i < AllVoltageLowActions.size(); i++) {
					SelectionArray[i] = AllVoltageLowActions[i].tokenize(":")
					SwitchSelection(SelectionArray[i],[evt.deviceId,null,Changed,Currentstate,null])
				}
			}
		}
	}
}

def DoEnergyMeter(evt) {
log.debug "Energy Meter"
	if (allOk) {
		def Changed
		def Number
		def SelectionArray = [][]
		for ( def i = 0; i < EnergyMeter.size(); i++) {
			if ( evt.deviceId == EnergyMeter[i].id ) {
				Changed = EnergyMeter[i]
				Number = i
			}
		}
		int Currentstate = Changed.currentValue("energy") as Integer
		if (settings["EnergyMeterHighActions${evt.deviceId}"]) {
			int HighSetting = settings["EnergyMeterHighThreshold${evt.deviceId}"] as Integer
			if (Currentstate > HighSetting) {
				log.debug "Device that triggered the event: ${Changed.displayName} | Number = $Number | Actions = ${settings["EnergyMeterHighActions${evt.deviceId}"]}"
				for ( def i = 0; i < settings["EnergyMeterHighActions${evt.deviceId}"].size(); i++) {
					SelectionArray[i] = settings["EnergyMeterHighActions${evt.deviceId}"][i].tokenize(":")
					SwitchSelection(SelectionArray[i],[evt.deviceId,null,Changed,Currentstate,null])
				}
			}
		}
		if (settings["EnergyMeterLowActions${evt.deviceId}"]) {
			int LowSetting = settings["EnergyMeterLowThreshold${evt.deviceId}"] as Integer
			if (Currentstate < LowSetting) {
				log.debug "Device that triggered the event: ${Changed.displayName} | Number = $Number | Actions = ${settings["EnergyMeterLowActions${evt.deviceId}"]}"
				for ( def i = 0; i < settings["EnergyMeterLowActions${evt.deviceId}"].size(); i++) {
					SelectionArray[i] = settings["EnergyMeterLowActions${evt.deviceId}"][i].tokenize(":")
					SwitchSelection(SelectionArray[i],[evt.deviceId,null,Changed,Currentstate,null])
				}
			}
		}
		if (AllEnergyMeterHighActions) {
			int HighSetting = AllEnergyMeterHighThreshold as Integer
			if (Currentstate > HighSetting) {
				log.debug "Device that triggered the event: ${Changed.displayName} | Number = $Number | Actions = $AllEnergyMeterHighActions"
				for ( def i = 0; i < AllEnergyMeterHighActions.size(); i++) {
					SelectionArray[i] = AllEnergyMeterHighActions[i].tokenize(":")
					SwitchSelection(SelectionArray[i],[evt.deviceId,null,Changed,Currentstate,null])
				}
			}
		}
		if (AllEnergyMeterLowActions) {
			int LowSetting = AllEnergyMeterLowThreshold as Integer
			if (Currentstate < LowSetting) {
				log.debug "Device that triggered the event: ${Changed.displayName} | Number = $Number | Actions = $AllEnergyMeterLowActions"
				for ( def i = 0; i < AllEnergyMeterLowActions.size(); i++) {
					SelectionArray[i] = AllEnergyMeterLowActions[i].tokenize(":")
					SwitchSelection(SelectionArray[i],[evt.deviceId,null,Changed,Currentstate,null])
				}
			}
		}
	}
}

def DoCO2Measurement(evt) {
	if (allOk) {
		def Changed
		def Number
		def SelectionArray = [][]
		for ( def i = 0; i < CO2Measurement.size(); i++) {
			if ( evt.deviceId == CO2Measurement[i].id ) {
				Changed = CO2Measurement[i]
				Number = i
			}
		}
		int Currentstate = Changed.currentValue("carbonDioxide") as Integer
		if (settings["CO2MeasurementHighActions${evt.deviceId}"]) {
			int HighSetting = settings["CO2MeasurementHighThreshold${evt.deviceId}"] as Integer
			if (Currentstate > HighSetting) {
				log.debug "Device that triggered the event: ${Changed.displayName} | Number = $Number | Actions = ${settings["CO2MeasurementHighActions${evt.deviceId}"]}"
				for ( def i = 0; i < settings["CO2MeasurementHighActions${evt.deviceId}"].size(); i++) {
					SelectionArray[i] = settings["CO2MeasurementHighActions${evt.deviceId}"][i].tokenize(":")
					SwitchSelection(SelectionArray[i],[evt.deviceId,null,Changed,Currentstate,null])
				}
			}
		}
		if (AllCO2MeasurementHighActions) {
			int HighSetting = AllCO2MeasurementHighThreshold as Integer
			if (Currentstate > HighSetting) {
				log.debug "Device that triggered the event: ${Changed.displayName} | Number = $Number | Actions = $AllCO2MeasurementHighActions"
				for ( def i = 0; i < AllCO2MeasurementHighActions.size(); i++) {
					SelectionArray[i] = AllCO2MeasurementHighActions[i].tokenize(":")
					SwitchSelection(SelectionArray[i],[evt.deviceId,null,Changed,Currentstate,null])
				}
			}
		}
	}
}

def DoStep(evt) {
	if (allOk) {
		def Changed
		def Number
		def SelectionArray = [][]
		for ( def i = 0; i < Step.size(); i++) {
			if ( evt.deviceId == Step[i].id ) {
				Changed = Step[i]
				Number = i
			}
		}
		int Currentstate = Changed.currentValue("steps") as Integer
		if (settings["StepHighActions${evt.deviceId}"]) {
			int HighSetting = settings["StepHighThreshold${evt.deviceId}"] as Integer
			if (Currentstate > HighSetting) {
				log.debug "Device that triggered the event: ${Changed.displayName} | Number = $Number | Actions = ${settings["StepHighActions${evt.deviceId}"]}"
				for ( def i = 0; i < settings["StepHighActions${evt.deviceId}"].size(); i++) {
					SelectionArray[i] = settings["StepHighActions${evt.deviceId}"][i].tokenize(":")
					SwitchSelection(SelectionArray[i],[evt.deviceId,null,Changed,Currentstate,null])
				}
			}
		}
		if (settings["StepLowActions${evt.deviceId}"]) {
			int LowSetting = settings["StepLowThreshold${evt.deviceId}"] as Integer
			if (Currentstate < LowSetting) {
				log.debug "Device that triggered the event: ${Changed.displayName} | Number = $Number | Actions = ${settings["StepLowActions${evt.deviceId}"]}"
				for ( def i = 0; i < settings["StepLowActions${evt.deviceId}"].size(); i++) {
					SelectionArray[i] = settings["StepLowActions${evt.deviceId}"][i].tokenize(":")
					SwitchSelection(SelectionArray[i],[evt.deviceId,null,Changed,Currentstate,null])
				}
			}
		}
		if (AllStepHighActions) {
			int HighSetting = AllStepHighThreshold as Integer
			if (Currentstate > HighSetting) {
				log.debug "Device that triggered the event: ${Changed.displayName} | Number = $Number | Actions = $AllStepHighActions"
				for ( def i = 0; i < AllStepHighActions.size(); i++) {
					SelectionArray[i] = AllStepHighActions[i].tokenize(":")
					SwitchSelection(SelectionArray[i],[evt.deviceId,null,Changed,Currentstate,null])
				}
			}
		}
		if (AllStepLowActions) {
			int LowSetting = AllStepLowThreshold as Integer
			if (Currentstate < LowSetting) {
				log.debug "Device that triggered the event: ${Changed.displayName} | Number = $Number | Actions = $AllStepLowActions"
				for ( def i = 0; i < AllStepLowActions.size(); i++) {
					SelectionArray[i] = AllStepLowActions[i].tokenize(":")
					SwitchSelection(SelectionArray[i],[evt.deviceId,null,Changed,Currentstate,null])
				}
			}
		}
	}
}

def DoStepGoal(evt) {
	if (allOk) {
		def Changed
		def Number
		def SelectionArray = [][]
		for ( def i = 0; i < Step.size(); i++) {
			if ( evt.deviceId == Step[i].id ) {
				Changed = Step[i]
				Number = i
			}
		}
		int Currentstate = Changed.currentValue("goal") as Integer
		if (settings["StepGoalActions${evt.deviceId}"]) {
			int HighSetting = settings["StepGoal${evt.deviceId}"] as Integer
			if (Currentstate >= HighSetting) {
				log.debug "Device that triggered the event: ${Changed.displayName} | Number = $Number | Actions = ${settings["StepGoalActions${evt.deviceId}"]}"
				for ( def i = 0; i < settings["StepGoalActions${evt.deviceId}"].size(); i++) {
					SelectionArray[i] = settings["StepGoalActions${evt.deviceId}"][i].tokenize(":")
					SwitchSelection(SelectionArray[i],[evt.deviceId,null,Changed,Currentstate,null])
				}
			}
		}
		if (AllStepGoalActions) {
			int HighSetting = AllStepGoalThreshold as Integer
			if (Currentstate >= HighSetting) {
				log.debug "Device that triggered the event: ${Changed.displayName} | Number = $Number | Actions = $AllStepGoalActions"
				for ( def i = 0; i < AllStepGoalActions.size(); i++) {
					SelectionArray[i] = AllStepGoalActions[i].tokenize(":")
					SwitchSelection(SelectionArray[i],[evt.deviceId,null,Changed,Currentstate,null])
				}
			}
		}
	}
}

def DoIlluminance(evt) {
	if (allOk) {
		def Changed
		def Number
		def SelectionArray = [][]
		for ( def i = 0; i < Illuminance.size(); i++) {
			if ( evt.deviceId == Illuminance[i].id ) {
				Changed = Illuminance[i]
				Number = i
			}
		}
		int Currentstate = Changed.currentValue("illuminance") as Integer
		if (settings["IlluminanceHighActions${evt.deviceId}"]) {
			int HighSetting = settings["IlluminanceHighThreshold${evt.deviceId}"] as Integer
			if (Currentstate > HighSetting) {
				log.debug "Device that triggered the event: ${Changed.displayName} | Number = $Number | Actions = ${settings["IlluminanceHighActions${evt.deviceId}"]}"
				for ( def i = 0; i < settings["IlluminanceHighActions${evt.deviceId}"].size(); i++) {
					SelectionArray[i] = settings["IlluminanceHighActions${evt.deviceId}"][i].tokenize(":")
					SwitchSelection(SelectionArray[i],[evt.deviceId,null,Changed,"lux",null])
				}
			}
		}
		if (settings["IlluminanceLowActions${evt.deviceId}"]) {
			int LowSetting = settings["IlluminanceLowThreshold${evt.deviceId}"] as Integer
			if (Currentstate < LowSetting) {
				log.debug "Device that triggered the event: ${Changed.displayName} | Number = $Number | Actions = ${settings["IlluminanceLowActions${evt.deviceId}"]}"
				for ( def i = 0; i < settings["IlluminanceLowActions${evt.deviceId}"].size(); i++) {
					SelectionArray[i] = settings["IlluminanceLowActions${evt.deviceId}"][i].tokenize(":")
					SwitchSelection(SelectionArray[i],[evt.deviceId,null,Changed,"lux",null])
				}
			}
		}
		if (AllIlluminanceHighActions) {
			int HighSetting = AllIlluminanceHighThreshold as Integer
			if (Currentstate > HighSetting) {
				log.debug "Device that triggered the event: ${Changed.displayName} | Number = $Number | Actions = $AllIlluminanceHighActions"
				for ( def i = 0; i < AllIlluminanceHighActions.size(); i++) {
					SelectionArray[i] = AllIlluminanceHighActions[i].tokenize(":")
					SwitchSelection(SelectionArray[i],[evt.deviceId,null,Changed,"lux",null])
				}
			}
		}
		if (AllIlluminanceLowActions) {
			int LowSetting = AllIlluminanceLowThreshold as Integer
			if (Currentstate < LowSetting) {
				log.debug "Device that triggered the event: ${Changed.displayName} | Number = $Number | Actions = $AllIlluminanceLowActions"
				for ( def i = 0; i < AllIlluminanceLowActions.size(); i++) {
					SelectionArray[i] = AllIlluminanceLowActions[i].tokenize(":")
					SwitchSelection(SelectionArray[i],[evt.deviceId,null,Changed,"lux",null])
				}
			}
		}
	}
}

def DoHumidity(evt) {
	if (allOk) {
		def Changed
		def Number
		def SelectionArray = [][]
		for ( def i = 0; i < Humidity.size(); i++) {
			if ( evt.deviceId == Humidity[i].id ) {
				Changed = Humidity[i]
				Number = i
			}
		}
		int Currentstate = Changed.currentValue("humidity") as Integer
		if (settings["HumidityHighActions${evt.deviceId}"]) {
			int HighSetting = settings["HumidityHighThreshold${evt.deviceId}"] as Integer
			if (Currentstate > HighSetting) {
				log.debug "Device that triggered the event: ${Changed.displayName} | Number = $Number | Actions = ${settings["HumidityHighActions${evt.deviceId}"]}"
				for ( def i = 0; i < settings["HumidityHighActions${evt.deviceId}"].size(); i++) {
					SelectionArray[i] = settings["HumidityHighActions${evt.deviceId}"][i].tokenize(":")
					SwitchSelection(SelectionArray[i],[evt.deviceId,null,Changed,"%",null])
				}
			}
		}
		if (settings["HumidityLowActions${evt.deviceId}"]) {
			int LowSetting = settings["HumidityLowThreshold${evt.deviceId}"] as Integer
			if (Currentstate < LowSetting) {
				log.debug "Device that triggered the event: ${Changed.displayName} | Number = $Number | Actions = ${settings["HumidityLowActions${evt.deviceId}"]}"
				for ( def i = 0; i < settings["HumidityLowActions${evt.deviceId}"].size(); i++) {
					SelectionArray[i] = settings["HumidityLowActions${evt.deviceId}"][i].tokenize(":")
					SwitchSelection(SelectionArray[i],[evt.deviceId,null,Changed,"%",null])
				}
			}
		}
		if (AllHumidityHighActions) {
			int HighSetting = AllHumidityHighThreshold as Integer
			if (Currentstate > HighSetting) {
				log.debug "Device that triggered the event: ${Changed.displayName} | Number = $Number | Actions = $AllHumidityHighActions"
				for ( def i = 0; i < AllHumidityHighActions.size(); i++) {
					SelectionArray[i] = AllHumidityHighActions[i].tokenize(":")
					SwitchSelection(SelectionArray[i],[evt.deviceId,null,Changed,"%",null])
				}
			}
		}
		if (AllHumidityLowActions) {
			int LowSetting = AllHumidityLowThreshold as Integer
			if (Currentstate < LowSetting) {
				log.debug "Device that triggered the event: ${Changed.displayName} | Number = $Number | Actions = $AllHumidityLowActions"
				for ( def i = 0; i < AllHumidityLowActions.size(); i++) {
					SelectionArray[i] = AllHumidityLowActions[i].tokenize(":")
					SwitchSelection(SelectionArray[i],[evt.deviceId,null,Changed,"%",null])
				}
			}
		}
	}
}

def DoUltravioletIndex(evt) {
	if (allOk) {
		def Changed
		def Number
		def SelectionArray = [][]
		for ( def i = 0; i < UltravioletIndex.size(); i++) {
			if ( evt.deviceId == UltravioletIndex[i].id ) {
				Changed = UltravioletIndex[i]
				Number = i
			}
		}
		int Currentstate = Changed.currentValue("ultravioletIndex") as Integer
		if (settings["UltravioletIndexHighActions${evt.deviceId}"]) {
			int HighSetting = settings["UltravioletIndexHighThreshold${evt.deviceId}"] as Integer
			if (Currentstate > HighSetting) {
				log.debug "Device that triggered the event: ${Changed.displayName} | Number = $Number | Actions = ${settings["UltravioletIndexHighActions${evt.deviceId}"]}"
				for ( def i = 0; i < settings["UltravioletIndexHighActions${evt.deviceId}"].size(); i++) {
					SelectionArray[i] = settings["UltravioletIndexHighActions${evt.deviceId}"][i].tokenize(":")
					SwitchSelection(SelectionArray[i],[evt.deviceId,null,Changed,Currentstate,null])
				}
			}
		}
		if (settings["UltravioletIndexLowActions${evt.deviceId}"]) {
			int LowSetting = settings["UltravioletIndexLowThreshold${evt.deviceId}"] as Integer
			if (Currentstate < LowSetting) {
				log.debug "Device that triggered the event: ${Changed.displayName} | Number = $Number | Actions = ${settings["UltravioletIndexLowActions${evt.deviceId}"]}"
				for ( def i = 0; i < settings["UltravioletIndexLowActions${evt.deviceId}"].size(); i++) {
					SelectionArray[i] = settings["UltravioletIndexLowActions${evt.deviceId}"][i].tokenize(":")
					SwitchSelection(SelectionArray[i],[evt.deviceId,null,Changed,Currentstate,null])
				}
			}
		}
		if (AllUltravioletIndexHighActions) {
			int HighSetting = AllUltravioletIndexHighThreshold as Integer
			if (Currentstate > HighSetting) {
				log.debug "Device that triggered the event: ${Changed.displayName} | Number = $Number | Actions = $AllUltravioletIndexHighActions"
				for ( def i = 0; i < AllUltravioletIndexHighActions.size(); i++) {
					SelectionArray[i] = AllUltravioletIndexHighActions[i].tokenize(":")
					SwitchSelection(SelectionArray[i],[evt.deviceId,null,Changed,Currentstate,null])
				}
			}
		}
		if (AllUltravioletIndexLowActions) {
			int LowSetting = AllUltravioletIndexLowThreshold as Integer
			if (Currentstate < LowSetting) {
				log.debug "Device that triggered the event: ${Changed.displayName} | Number = $Number | Actions = $AllUltravioletIndexLowActions"
				for ( def i = 0; i < AllUltravioletIndexLowActions.size(); i++) {
					SelectionArray[i] = AllUltravioletIndexLowActions[i].tokenize(":")
					SwitchSelection(SelectionArray[i],[evt.deviceId,null,Changed,Currentstate,null])
				}
			}
		}
	}
}

def DophMeasurement(evt) {
	if (allOk) {
		def Changed
		def Number
		def SelectionArray = [][]
		for ( def i = 0; i < phMeasurement.size(); i++) {
			if ( evt.deviceId == phMeasurement[i].id ) {
				Changed = phMeasurement[i]
				Number = i
			}
		}
		int Currentstate = Changed.currentValue("phMeasurement") as Integer
		if (settings["phMeasurementHighActions${evt.deviceId}"]) {
			int HighSetting = settings["phMeasurementHighThreshold${evt.deviceId}"] as Integer
			if (Currentstate > HighSetting) {
				log.debug "Device that triggered the event: ${Changed.displayName} | Number = $Number | Actions = ${settings["phMeasurementHighActions${evt.deviceId}"]}"
				for ( def i = 0; i < settings["phMeasurementHighActions${evt.deviceId}"].size(); i++) {
					SelectionArray[i] = settings["phMeasurementHighActions${evt.deviceId}"][i].tokenize(":")
					SwitchSelection(SelectionArray[i],[evt.deviceId,null,Changed,Currentstate,null])
				}
			}
		}
		if (settings["phMeasurementLowActions${evt.deviceId}"]) {
			int LowSetting = settings["phMeasurementLowThreshold${evt.deviceId}"] as Integer
			if (Currentstate < LowSetting) {
				log.debug "Device that triggered the event: ${Changed.displayName} | Number = $Number | Actions = ${settings["phMeasurementLowActions${evt.deviceId}"]}"
				for ( def i = 0; i < settings["phMeasurementLowActions${evt.deviceId}"].size(); i++) {
					SelectionArray[i] = settings["phMeasurementLowActions${evt.deviceId}"][i].tokenize(":")
					SwitchSelection(SelectionArray[i],[evt.deviceId,null,Changed,Currentstate,null])
				}
			}
		}
		if (AllphMeasurementHighActions) {
			int HighSetting = AllphMeasurementHighThreshold as Integer
			if (Currentstate > HighSetting) {
				log.debug "Device that triggered the event: ${Changed.displayName} | Number = $Number | Actions = $AllphMeasurementHighActions"
				for ( def i = 0; i < AllphMeasurementHighActions.size(); i++) {
					SelectionArray[i] = AllphMeasurementHighActions[i].tokenize(":")
					SwitchSelection(SelectionArray[i],[evt.deviceId,null,Changed,Currentstate,null])
				}
			}
		}
		if (AllphMeasurementLowActions) {
			int LowSetting = AllphMeasurementLowThreshold as Integer
			if (Currentstate < LowSetting) {
				log.debug "Device that triggered the event: ${Changed.displayName} | Number = $Number | Actions = $AllphMeasurementLowActions"
				for ( def i = 0; i < AllphMeasurementLowActions.size(); i++) {
					SelectionArray[i] = AllphMeasurementLowActions[i].tokenize(":")
					SwitchSelection(SelectionArray[i],[evt.deviceId,null,Changed,Currentstate,null])
				}
			}
		}
	}
}

def DosoundPressureLevel(evt) {
	if (allOk) {
		def Changed
		def Number
		def SelectionArray = [][]
		for ( def i = 0; i < soundPressureLevel.size(); i++) {
			if ( evt.deviceId == soundPressureLevel[i].id ) {
				Changed = soundPressureLevel[i]
				Number = i
			}
		}
		int Currentstate = Changed.currentValue("soundPressureLevel") as Integer
		if (settings["soundPressureLevelHighActions${evt.deviceId}"]) {
			int HighSetting = settings["soundPressureLevelHighThreshold${evt.deviceId}"] as Integer
			if (Currentstate > HighSetting) {
				log.debug "Device that triggered the event: ${Changed.displayName} | Number = $Number | Actions = ${settings["soundPressureLevelHighActions${evt.deviceId}"]}"
				for ( def i = 0; i < settings["soundPressureLevelHighActions${evt.deviceId}"].size(); i++) {
					SelectionArray[i] = settings["soundPressureLevelHighActions${evt.deviceId}"][i].tokenize(":")
					SwitchSelection(SelectionArray[i],[evt.deviceId,null,Changed,Currentstate,null])
				}
			}
		}
		if (settings["soundPressureLevelLowActions${evt.deviceId}"]) {
			int LowSetting = settings["soundPressureLevelLowThreshold${evt.deviceId}"] as Integer
			if (Currentstate < LowSetting) {
				log.debug "Device that triggered the event: ${Changed.displayName} | Number = $Number | Actions = ${settings["soundPressureLevelLowActions${evt.deviceId}"]}"
				for ( def i = 0; i < settings["soundPressureLevelLowActions${evt.deviceId}"].size(); i++) {
					SelectionArray[i] = settings["soundPressureLevelLowActions${evt.deviceId}"][i].tokenize(":")
					SwitchSelection(SelectionArray[i],[evt.deviceId,null,Changed,Currentstate,null])
				}
			}
		}
		if (AllsoundPressureLevelHighActions) {
			int HighSetting = AllsoundPressureLevelHighThreshold as Integer
			if (Currentstate > HighSetting) {
				log.debug "Device that triggered the event: ${Changed.displayName} | Number = $Number | Actions = $AllsoundPressureLevelHighActions"
				for ( def i = 0; i < AllsoundPressureLevelHighActions.size(); i++) {
					SelectionArray[i] = AllsoundPressureLevelHighActions[i].tokenize(":")
					SwitchSelection(SelectionArray[i],[evt.deviceId,null,Changed,Currentstate,null])
				}
			}
		}
		if (AllsoundPressureLevelLowActions) {
			int LowSetting = AllsoundPressureLevelLowThreshold as Integer
			if (Currentstate < LowSetting) {
				log.debug "Device that triggered the event: ${Changed.displayName} | Number = $Number | Actions = $AllsoundPressureLevelLowActions"
				for ( def i = 0; i < AllsoundPressureLevelLowActions.size(); i++) {
					SelectionArray[i] = AllsoundPressureLevelLowActions[i].tokenize(":")
					SwitchSelection(SelectionArray[i],[evt.deviceId,null,Changed,Currentstate,null])
				}
			}
		}
	}
}

def DoButtonPushed(evt) {
	if (allOk) {
		def buttonNumber = parseJson(evt.data)?.buttonNumber
		def Changed
		def Number
		def SelectionArray = [][]
		for ( def i = 0; i < Button.size(); i++) {
			if ( evt.deviceId == Button[i].id ) {
				Changed = Button[i]
				Number = i
			}
		}
		if (settings["${evt.deviceId}Button${buttonNumber}Push"]) {
			log.debug "Device that triggered the event: ${Changed.displayName} | Number = $Number | Button $buttonNumber Pushed | Command = ${settings["${evt.deviceId}Button${buttonNumber}Push"]}"
			for ( def i = 0; i < settings["${evt.deviceId}Button${buttonNumber}Push"].size(); i++) {
				SelectionArray[i] = settings["${evt.deviceId}Button${buttonNumber}Push"][i].tokenize(":")
				//log.debug "DeviceType = ${SelectionArray[i][0]},Device = ${SelectionArray[i][1]},Action = ${SelectionArray[i][2]}"
				SwitchSelection(SelectionArray[i],[evt.deviceId, buttonNumber,Changed,"pushed",null])
			}
		}
	}
}

def DoButtonHeld(evt) {
	if (allOk) {
		def buttonNumber = parseJson(evt.data)?.buttonNumber
		def Changed
		def Number
		def SelectionArray = [][]
		for ( def i = 0; i < Button.size(); i++) {
			if ( evt.deviceId == Button[i].id ) {
				Changed = Button[i]
				Number = i
			}
		}
		if (settings["${evt.deviceId}Button${buttonNumber}Hold"]) {
			log.debug "Device that triggered the event: ${Changed.displayName} | Number = $Number | Button $buttonNumber Held | Command = ${settings["${evt.deviceId}Button${buttonNumber}Hold"]}"
			for ( def i = 0; i < settings["${evt.deviceId}Button${buttonNumber}Hold"].size(); i++) {
				SelectionArray[i] = settings["${evt.deviceId}Button${buttonNumber}Hold"][i].tokenize(":")
				SwitchSelection(SelectionArray[i],[evt.deviceId, buttonNumber,Changed,"held",null])
			}		 
		}
	}
}

def SwitchSelection(Selection,InfoArray) {
	/*
	def RuninParams = [DeviceType: Selection[0],Device: Selection[1],Action: "off"]
	if (Params) {
		Selection[0] = Params.DeviceType
		Selection[1] = Params.Device
		Selection[2] = Params.Action
		log.debug "Params.Action = ${Params.Action}"
	}*/
	def hueColor = 0
	def saturation = 100
	switch (Selection[2]) {
		case "on":
			if (Selection[0] == "Bulbs") {
				boolean SwitchChanged = false
				boolean MotionChanged = false
				boolean ContactChanged = false
				boolean ButtonPushed = false
				boolean ButtonHeld = false
				Bulbs.each {
					if ("$it" == "${Selection[1]}" || "${Selection[1]}" == "all") {
						if (it.currentValue("switch") != "on") {
							log.debug "Turning $it on"
							it.on()
							it.setLevel(100)
							if (Switch) {
								if (settings["TurnOffAfterMinutes${settings["SwitchOn${InfoArray[0]}"]}"]) {
									SwitchChanged = true
									log.debug "Setting timer for ${settings["TurnOffAfterMinutes${settings["SwitchOn${InfoArray[0]}"]}"]} minute(s) to change $it to off"
								}
							}
							if (Motion) {
								if (settings["TurnOffAfterMinutes${settings["MotionActive${InfoArray[0]}"]}"]) {
									MotionChanged = true
									log.debug "Setting timer for ${settings["TurnOffAfterMinutes${settings["MotionActive${InfoArray[0]}"]}"]} minute(s) to change $it to off"
									//atomicState.FirstRun = true
								}
							}
							if (Contact) {
								if (settings["TurnOffAfterMinutes${settings["ContactOpen${InfoArray[0]}"]}"]) {
									ContactChanged = true
									log.debug "Setting timer for ${settings["TurnOffAfterMinutes${settings["ContactOpen${InfoArray[0]}"]}"]} minute(s) to change $it to off"
								}
							}
							if (Button) {
								if (settings["TurnOffAfterMinutes${InfoArray[0]}Button${InfoArray[1]}Push"]) {
									ButtonPushed = true
									log.debug "Setting timer for ${settings["TurnOffAfterMinutes${InfoArray[0]}Button${InfoArray[1]}Push"]} minute(s) to change $it to off"
								}
								if (settings["TurnOffAfterMinutes${InfoArray[0]}Button${InfoArray[1]}Hold"]) {
									ButtonHeld = true
									log.debug "Setting timer for ${settings["TurnOffAfterMinutes${InfoArray[0]}Button${InfoArray[1]}Hold"]} minute(s) to change $it to off"
								}
							}
						}
					}
				}
				if (SwitchChanged) {runIn(settings["TurnOffAfterMinutes${settings["SwitchOn${InfoArray[0]}"]}"]*60, TurnOffBulbs_Switch)}
				if (MotionChanged) {runIn(settings["TurnOffAfterMinutes${settings["MotionActive${InfoArray[0]}"]}"]*60, TurnOffBulbs_Motion)}
				if (ContactChanged) {runIn(settings["TurnOffAfterMinutes${settings["ContactOpen${InfoArray[0]}"]}"]*60, TurnOffBulbs_Contact)}
				if (ButtonPushed) {runIn(settings["TurnOffAfterMinutes${InfoArray[0]}Button${InfoArray[1]}Push"]*60, TurnOffBulbs_Push)}
				if (ButtonHeld) {runIn(settings["TurnOffAfterMinutes${InfoArray[0]}Button${InfoArray[1]}Hold"]*60, TurnOffBulbs_Hold)}
			}
			if (Selection[0] == "Dimmers") {
				boolean SwitchChanged = false
				boolean MotionChanged = false
				boolean ContactChanged = false
				boolean ButtonPushed = false
				boolean ButtonHeld = false
				Dimmers.each {
					if ("$it" == "${Selection[1]}" || "${Selection[1]}" == "all") {
						if (it.currentValue("switch") != "on") {
							log.debug "Turning $it on"
							it.on()
							it.setLevel(100)
							if (Switch) {
								if (settings["TurnOffAfterMinutes${settings["SwitchOn${InfoArray[0]}"]}"]) {
									SwitchChanged = true
									log.debug "Setting timer for ${settings["TurnOffAfterMinutes${settings["SwitchOn${InfoArray[0]}"]}"]} minute(s) to change $it to off"
								}
							}
							if (Motion) {
								if (settings["TurnOffAfterMinutes${settings["MotionActive${InfoArray[0]}"]}"]) {
									MotionChanged = true
									log.debug "Setting timer for ${settings["TurnOffAfterMinutes${settings["MotionActive${InfoArray[0]}"]}"]} minute(s) to change $it to off"
									//atomicState.FirstRun = true
								}
							}
							if (Contact) {
								if (settings["TurnOffAfterMinutes${settings["ContactOpen${InfoArray[0]}"]}"]) {
									ContactChanged = true
									log.debug "Setting timer for ${settings["TurnOffAfterMinutes${settings["ContactOpen${InfoArray[0]}"]}"]} minute(s) to change $it to off"
								}
							}
							if (Button) {
								if (settings["TurnOffAfterMinutes${InfoArray[0]}Button${InfoArray[1]}Push"]) {
									ButtonPushed = true
									log.debug "Setting timer for ${settings["TurnOffAfterMinutes${InfoArray[0]}Button${InfoArray[1]}Push"]} minute(s) to change $it to off"
								}
								if (settings["TurnOffAfterMinutes${InfoArray[0]}Button${InfoArray[1]}Hold"]) {
									ButtonHeld = true
									log.debug "Setting timer for ${settings["TurnOffAfterMinutes${InfoArray[0]}Button${InfoArray[1]}Hold"]} minute(s) to change $it to off"
								}
							}
						}
					}
				}
				if (SwitchChanged) {runIn(settings["TurnOffAfterMinutes${settings["SwitchOn${InfoArray[0]}"]}"]*60, TurnOffDimmers_Switch)}
				if (MotionChanged) {runIn(settings["TurnOffAfterMinutes${settings["MotionActive${InfoArray[0]}"]}"]*60, TurnOffDimmers_Motion)}
				if (ContactChanged) {runIn(settings["TurnOffAfterMinutes${settings["ContactOpen${InfoArray[0]}"]}"]*60, TurnOffDimmers_Contact)}
				if (ButtonPushed) {runIn(settings["TurnOffAfterMinutes${InfoArray[0]}Button${InfoArray[1]}Push"]*60, TurnOffDimmers_Push)}
				if (ButtonHeld) {runIn(settings["TurnOffAfterMinutes${InfoArray[0]}Button${InfoArray[1]}Hold"]*60, TurnOffDimmers_Hold)}
			}
			if (Selection[0] == "Switches") {
				boolean SwitchChanged = false
				boolean MotionChanged = false
				boolean ContactChanged = false
				boolean ButtonPushed = false
				boolean ButtonHeld = false
				Switches.each {
					if ("$it" == "${Selection[1]}" || "${Selection[1]}" == "all") {
						if (it.currentValue("switch") != "on") {
							log.debug "Turning $it on"
							it.on()
							if (Switch) {
								if (settings["TurnOffAfterMinutes${settings["SwitchOn${InfoArray[0]}"]}"]) {
									SwitchChanged = true
									log.debug "Setting timer for ${settings["TurnOffAfterMinutes${settings["SwitchOn${InfoArray[0]}"]}"]} minute(s) to change $it to off"
								}
							}
							if (Motion) {
								if (settings["TurnOffAfterMinutes${settings["MotionActive${InfoArray[0]}"]}"]) {
									MotionChanged = true
									log.debug "Setting timer for ${settings["TurnOffAfterMinutes${settings["MotionActive${InfoArray[0]}"]}"]} minute(s) to change $it to off"
									//atomicState.FirstRun = true
								}
							}
							if (Contact) {
								if (settings["TurnOffAfterMinutes${settings["ContactOpen${InfoArray[0]}"]}"]) {
									ContactChanged = true
									log.debug "Setting timer for ${settings["TurnOffAfterMinutes${settings["ContactOpen${InfoArray[0]}"]}"]} minute(s) to change $it to off"
								}
							}
							if (Button) {
								if (settings["TurnOffAfterMinutes${InfoArray[0]}Button${InfoArray[1]}Push"]) {
									ButtonPushed = true
									log.debug "Setting timer for ${settings["TurnOffAfterMinutes${InfoArray[0]}Button${InfoArray[1]}Push"]} minute(s) to change $it to off"
								}
								if (settings["TurnOffAfterMinutes${InfoArray[0]}Button${InfoArray[1]}Hold"]) {
									ButtonHeld = true
									log.debug "Setting timer for ${settings["TurnOffAfterMinutes${InfoArray[0]}Button${InfoArray[1]}Hold"]} minute(s) to change $it to off"
								}
							}
						}
					}
				}
				if (SwitchChanged) {runIn(settings["TurnOffAfterMinutes${settings["SwitchOn${InfoArray[0]}"]}"]*60, TurnOffSwitches_Switch)}
				if (MotionChanged) {runIn(settings["TurnOffAfterMinutes${settings["MotionActive${InfoArray[0]}"]}"]*60, TurnOffSwitches_Motion)}
				if (ContactChanged) {runIn(settings["TurnOffAfterMinutes${settings["ContactOpen${InfoArray[0]}"]}"]*60, TurnOffSwitches_Contact)}
				if (ButtonPushed) {runIn(settings["TurnOffAfterMinutes${InfoArray[0]}Button${InfoArray[1]}Push"]*60, TurnOffSwitches_Push)}
				if (ButtonHeld) {runIn(settings["TurnOffAfterMinutes${InfoArray[0]}Button${InfoArray[1]}Hold"]*60, TurnOffSwitches_Hold)}
			}
			if (Selection[0] == "Outlets") {
				if ("${Selection[1]}" != "all") {
						Outlets.each {
							if ("$it" == "${Selection[1]}") {log.debug "Turning $it on"}
							if ("$it" == "${Selection[1]}") {it.on()}
						}
					} else {
						log.debug "Turning All Outlets on"
						Outlets.on()
				}
			}
			if (Selection[0] == "Relays") {
				if ("${Selection[1]}" != "all") {
						Relays.each {
							if ("$it" == "${Selection[1]}") {log.debug "Turning $it on"}
							if ("$it" == "${Selection[1]}") {it.on()}
						}
					} else {
						log.debug "Turning All Relays on"
						Relays.on()
				}
			}
		break
		case "off":
			if (Selection[0] == "Bulbs") {
				if ("${Selection[1]}" != "all") {
						Bulbs.each {
							if ("$it" == "${Selection[1]}") {log.debug "Turning $it off"}
							if ("$it" == "${Selection[1]}") {it.off()}
						}
					} else {
						log.debug "Turning All Bulbs off"
						Bulbs.off()
				}
			}
			if (Selection[0] == "Dimmers") {
				if ("${Selection[1]}" != "all") {
						Dimmers.each {
							if ("$it" == "${Selection[1]}") {log.debug "Turning $it off"}
							if ("$it" == "${Selection[1]}") {it.off()}
						}
					} else {
						log.debug "Turning All Dimmers off"
						Dimmers.off()
				}
			}
			if (Selection[0] == "Switches") {
				if ("${Selection[1]}" != "all") {
						Switches.each {
							if ("$it" == "${Selection[1]}") {log.debug "Turning $it off"}
							if ("$it" == "${Selection[1]}") {it.off()}
						}
					} else {
						log.debug "Turning All Switches off"
						Switches.off()
				}
			}
			if (Selection[0] == "Sirens") {
				if ("${Selection[1]}" != "all") {
						Sirens.each {
							if ("$it" == "${Selection[1]}") {log.debug "Stopping $it"}
							if ("$it" == "${Selection[1]}") {it.off()}
						}
					} else {
						log.debug "Stopping All Sirens"
						Sirens.off()
				}
			}
			if (Selection[0] == "Outlets") {
				if ("${Selection[1]}" != "all") {
						Outlets.each {
							if ("$it" == "${Selection[1]}") {log.debug "Turning $it off"}
							if ("$it" == "${Selection[1]}") {it.off()}
						}
					} else {
						log.debug "Turning All Outlets off"
						Outlets.off()
				}
			}
			 if (Selection[0] == "Relays") {
				if ("${Selection[1]}" != "all") {
						Relays.each {
							if ("$it" == "${Selection[1]}") {log.debug "Turning $it off"}
							if ("$it" == "${Selection[1]}") {it.off()}
						}
					} else {
						log.debug "Turning All Relays off"
						Relays.off()
				}
			}
		break
		case "Doorbell":
			def Now = new Date()
			if ("${Selection[1]}" != "all") {
					AudioNotify.each {
						if ("$it" == "${Selection[1]}") {
							if (NotifyOnce) {
									boolean EventFound = false
									boolean TriggerEventFound = false
									def Events = it.eventsBetween(new Date(Now.getTime() - NotifyOnceinSeconds*600 - 600), new Date(Now.getTime() - 6000), [max: 20])
									if (Events.size() == 0) {
											log.debug "Activating Doorbell on $it"
											it.on()
										} else {
											for ( def k =  Events.size(); k >= 0; k--) {
												if (Events[k] != null) {if (Events[k].descriptionText != null) {if (Events[k].descriptionText.endsWith('status is play')) {EventFound = true}}}
											}
											if (EventFound) {
												def TriggerEvents = InfoArray[2].eventsBetween(new Date(Now.getTime() - NotifyOnceinSeconds*1000 - 600), new Date(Now.getTime()), [max: 20])//1000 = 1 second
												if (InfoArray[4] == null) {for ( def k =  TriggerEvents.size(); k >= 1; k--) {if (TriggerEvents[k] != null) {if (TriggerEvents[k].descriptionText != null) {if (TriggerEvents[k].descriptionText.endsWith("${InfoArray[3]}")) {TriggerEventFound = true}}}}}
												if (InfoArray[4] != null) {for ( def k =  TriggerEvents.size(); k >= 1; k--) {if (TriggerEvents[k] != null) {if (TriggerEvents[k].descriptionText != null) {if (TriggerEvents[k].descriptionText.endsWith("${InfoArray[3]}") || TriggerEvents[k].descriptionText.endsWith("${InfoArray[4]}")) {TriggerEventFound = true}}}}}
											}
											if (!TriggerEventFound) {EventFound = false}
											if (!EventFound) {
													log.debug "Activating Doorbell on $it"
													it.on()
												} else {
													if (!TriggerEventFound) {
														log.debug "Event details = ${Events.descriptionText} | Triggered by = ${InfoArray[2]} being ${InfoArray[3]}"
														log.debug "Not changing $it since it has already played once in the last $NotifyOnceinSeconds seconds."
													}
													if (TriggerEventFound) {log.debug "Not changing $it since it has already played once in the last $NotifyOnceinSeconds seconds due to ${InfoArray[2]} being ${InfoArray[3]}."}
											}
									}
								} else {
									log.debug "Activating Doorbell on $it"
									it.on()
							}
						}
					}
				} else {
					AudioNotify.each {
						if (NotifyOnce) {
								boolean EventFound = false
								boolean TriggerEventFound = false
								def Events = it.eventsBetween(new Date(Now.getTime() - NotifyOnceinSeconds*600 - 600), new Date(Now.getTime() - 6000), [max: 20])
								if (Events.size() == 0) {
										log.debug "Activating Doorbell on $it"
										it.on()
									} else {
										for ( def k =  Events.size(); k >= 0; k--) {
											if (Events[k] != null) {if (Events[k].descriptionText != null) {if (Events[k].descriptionText.endsWith('status is play')) {EventFound = true}}}
										}
										if (EventFound) {
											def TriggerEvents = InfoArray[2].eventsBetween(new Date(Now.getTime() - NotifyOnceinSeconds*1000 - 600), new Date(Now.getTime()), [max: 20])//1000 = 1 second
											if (InfoArray[4] == null) {for ( def k =  TriggerEvents.size(); k >= 1; k--) {if (TriggerEvents[k] != null) {if (TriggerEvents[k].descriptionText != null) {if (TriggerEvents[k].descriptionText.endsWith("${InfoArray[3]}")) {TriggerEventFound = true}}}}}
											if (InfoArray[4] != null) {for ( def k =  TriggerEvents.size(); k >= 1; k--) {if (TriggerEvents[k] != null) {if (TriggerEvents[k].descriptionText != null) {if (TriggerEvents[k].descriptionText.endsWith("${InfoArray[3]}") || TriggerEvents[k].descriptionText.endsWith("${InfoArray[4]}")) {TriggerEventFound = true}}}}}
										}
										if (!TriggerEventFound) {EventFound = false}
										if (!EventFound) {
												log.debug "Activating Doorbell on $it"
												it.on()
											} else {
												if (!TriggerEventFound) {
													log.debug "Event details = ${Events.descriptionText} | Triggered by = ${InfoArray[2]} being ${InfoArray[3]}"
													log.debug "Not changing $it since it has already played once in the last $NotifyOnceinSeconds seconds."
												}
												if (TriggerEventFound) {log.debug "Not changing $it since it has already played once in the last $NotifyOnceinSeconds seconds due to ${InfoArray[2]} being ${InfoArray[3]}."}
										}
								}
							} else {
								log.debug "Activating Doorbell on $it"
								it.on()
						}
					}
			}
		break
		case "Beep":
			def Now = new Date()
			if ("${Selection[1]}" != "all") {
					AudioNotify.each {
						if ("$it" == "${Selection[1]}") {
							if (NotifyOnce) {
									boolean EventFound = false
									boolean TriggerEventFound = false
									def Events = it.eventsBetween(new Date(Now.getTime() - NotifyOnceinSeconds*600 - 600), new Date(Now.getTime() - 6000), [max: 20])
									if (Events.size() == 0) {
											log.debug "Activating Beep on $it"
											it.beep()
										} else {
											for ( def k =  Events.size(); k >= 0; k--) {
												if (Events[k] != null) {if (Events[k].descriptionText != null) {if (Events[k].descriptionText.endsWith('status is play')) {EventFound = true}}}
											}
											if (EventFound) {
												def TriggerEvents = InfoArray[2].eventsBetween(new Date(Now.getTime() - NotifyOnceinSeconds*1000 - 600), new Date(Now.getTime()), [max: 20])//1000 = 1 second
												if (InfoArray[4] == null) {for ( def k =  TriggerEvents.size(); k >= 1; k--) {if (TriggerEvents[k] != null) {if (TriggerEvents[k].descriptionText != null) {if (TriggerEvents[k].descriptionText.endsWith("${InfoArray[3]}")) {TriggerEventFound = true}}}}}
												if (InfoArray[4] != null) {for ( def k =  TriggerEvents.size(); k >= 1; k--) {if (TriggerEvents[k] != null) {if (TriggerEvents[k].descriptionText != null) {if (TriggerEvents[k].descriptionText.endsWith("${InfoArray[3]}") || TriggerEvents[k].descriptionText.endsWith("${InfoArray[4]}")) {TriggerEventFound = true}}}}}
											}
											if (!TriggerEventFound) {EventFound = false}
											if (!EventFound) {
													log.debug "Activating Beep on $it"
													it.beep()
												} else {
													if (!TriggerEventFound) {
														log.debug "Event details = ${Events.descriptionText} | Triggered by = ${InfoArray[2]} being ${InfoArray[3]}"
														log.debug "Not changing $it since it has already played once in the last $NotifyOnceinSeconds seconds."
													}
													if (TriggerEventFound) {log.debug "Not changing $it since it has already played once in the last $NotifyOnceinSeconds seconds due to ${InfoArray[2]} being ${InfoArray[3]}."}
											}
									}
								} else {
									log.debug "Activating Beep on $it"
									it.beep()
							}
						}
					}
				} else {
					AudioNotify.each {
						if (NotifyOnce) {
								boolean EventFound = false
								boolean TriggerEventFound = false
								def Events = it.eventsBetween(new Date(Now.getTime() - NotifyOnceinSeconds*600 - 600), new Date(Now.getTime() - 6000), [max: 20])
								if (Events.size() == 0) {
										log.debug "Activating Beep on $it"
										it.beep()
									} else {
										for ( def k =  Events.size(); k >= 0; k--) {
											if (Events[k] != null) {if (Events[k].descriptionText != null) {if (Events[k].descriptionText.endsWith('status is play')) {EventFound = true}}}
										}
										if (EventFound) {
											def TriggerEvents = InfoArray[2].eventsBetween(new Date(Now.getTime() - NotifyOnceinSeconds*1000 - 600), new Date(Now.getTime()), [max: 20])//1000 = 1 second
											if (InfoArray[4] == null) {for ( def k =  TriggerEvents.size(); k >= 1; k--) {if (TriggerEvents[k] != null) {if (TriggerEvents[k].descriptionText != null) {if (TriggerEvents[k].descriptionText.endsWith("${InfoArray[3]}")) {TriggerEventFound = true}}}}}
											if (InfoArray[4] != null) {for ( def k =  TriggerEvents.size(); k >= 1; k--) {if (TriggerEvents[k] != null) {if (TriggerEvents[k].descriptionText != null) {if (TriggerEvents[k].descriptionText.endsWith("${InfoArray[3]}") || TriggerEvents[k].descriptionText.endsWith("${InfoArray[4]}")) {TriggerEventFound = true}}}}}
										}
										if (!TriggerEventFound) {EventFound = false}
										if (!EventFound) {
												log.debug "Activating Beep on $it"
												it.beep()
											} else {
												if (!TriggerEventFound) {
													log.debug "Event details = ${Events.descriptionText} | Triggered by = ${InfoArray[2]} being ${InfoArray[3]}"
													log.debug "Not changing $it since it has already played once in the last $NotifyOnceinSeconds seconds."
												}
												if (TriggerEventFound) {log.debug "Not changing $it since it has already played once in the last $NotifyOnceinSeconds seconds due to ${InfoArray[2]} being ${InfoArray[3]}."}
										}
								}
							} else {
								log.debug "Activating Beep on $it"
								it.beep()
						}
					}
			}
		break
		case "Track":
			def Now = new Date()
			if ("${Selection[1]}" != "all") {
					AudioNotify.each {
						if ("$it" == "${Selection[1]}") {
							if (NotifyOnce) {
									boolean EventFound = false
									boolean TriggerEventFound = false
									def Events = it.eventsBetween(new Date(Now.getTime() - NotifyOnceinSeconds*600 - 600), new Date(Now.getTime() - 6000), [max: 20])
									if (Events.size() == 0) {
											if (!AudioNotifyAtVolume && !AudioNotifyRepeat) {
												log.debug "Activating track ${Selection[3]} on $it"
												it.playTrack(Selection[3])
											}
											if (!AudioNotifyAtVolume && AudioNotifyRepeat) {
												log.debug "Activating track ${Selection[3]} on $it $AudioNotifyRepeatCount times"
												it.playRepeatTrack(Selection[3], AudioNotifyRepeatCount)
											}
											if (AudioNotifyAtVolume && !AudioNotifyRepeat) {
												log.debug "Activating track ${Selection[3]} on $it at volume $AudioNotifyVolume"
												it.playTrackAtVolume(Selection[3], AudioNotifyVolume)
											}
											if (AudioNotifyAtVolume && AudioNotifyRepeat) {
												log.debug "Activating track ${Selection[3]} on $it at volume $AudioNotifyVolume $AudioNotifyRepeatCount times"
												it.playRepeatTrackAtVolume(Selection[3], AudioNotifyVolume, AudioNotifyRepeatCount)
											}
										} else {
											for ( def k =  Events.size(); k >= 0; k--) {
												if (Events[k] != null) {if (Events[k].descriptionText != null) {if (Events[k].descriptionText.endsWith('status is play')) {EventFound = true}}}
											}
											if (EventFound) {
												//log.debug "-----Looking at events for ${InfoArray[2]}"
												def TriggerEvents = InfoArray[2].eventsBetween(new Date(Now.getTime() - NotifyOnceinSeconds*1000 - 600), new Date(Now.getTime()), [max: 20])//1000 = 1 second
												//log.debug "Time Between ${new Date(Now.getTime() - NotifyOnceinSeconds*1000 - 600)} and ${new Date(Now.getTime())}"
												for ( def k =  TriggerEvents.size(); k >= 1; k--) {
													if (InfoArray[4] == null) {if (TriggerEvents[k] != null) {if (TriggerEvents[k].descriptionText != null) {if (TriggerEvents[k].descriptionText.endsWith("${InfoArray[3]}")) {TriggerEventFound = true}}}}
													if (InfoArray[4] != null) {if (TriggerEvents[k] != null) {if (TriggerEvents[k].descriptionText != null) {if (TriggerEvents[k].descriptionText.endsWith("${InfoArray[3]}") || TriggerEvents[k].descriptionText.endsWith("${InfoArray[4]}")) {TriggerEventFound = true}}}}
													if (TriggerEvents[k] != null) {if (TriggerEvents[k].descriptionText != null) {log.trace "*****Event details $k= ${TriggerEvents[k].descriptionText} @ ${TriggerEvents[k].date}"}}
												}
											}
											//log.debug "-----TriggerEventFound = $TriggerEventFound"
											if (!TriggerEventFound) {EventFound = false}
											if (!EventFound) {
													if (!AudioNotifyAtVolume && !AudioNotifyRepeat) {
														log.debug "Activating track ${Selection[3]} on $it"
														it.playTrack(Selection[3])
													}
													if (!AudioNotifyAtVolume && AudioNotifyRepeat) {
														log.debug "Activating track ${Selection[3]} on $it $AudioNotifyRepeatCount times"
														it.playRepeatTrack(Selection[3], AudioNotifyRepeatCount)
													}
													if (AudioNotifyAtVolume && !AudioNotifyRepeat) {
														log.debug "Activating track ${Selection[3]} on $it at volume $AudioNotifyVolume"
														it.playTrackAtVolume(Selection[3], AudioNotifyVolume)
													}
													if (AudioNotifyAtVolume && AudioNotifyRepeat) {
														log.debug "Activating track ${Selection[3]} on $it at volume $AudioNotifyVolume $AudioNotifyRepeatCount times"
														it.playRepeatTrackAtVolume(Selection[3], AudioNotifyVolume, AudioNotifyRepeatCount)
													}
												} else {
													if (!TriggerEventFound) {
														log.debug "Event details = ${Events.descriptionText} | Triggered by = ${InfoArray[2]} being ${InfoArray[3]}"
														log.debug "Not changing $it since it has already played once in the last $NotifyOnceinSeconds seconds."
													}
													if (TriggerEventFound) {log.debug "Not changing $it since it has already played once in the last $NotifyOnceinSeconds seconds due to ${InfoArray[2]} being ${InfoArray[3]}."}
											}
									}
								} else {
									if (!AudioNotifyAtVolume && !AudioNotifyRepeat) {
										log.debug "Activating track ${Selection[3]} on $it"
										it.playTrack(Selection[3])
									}
									if (!AudioNotifyAtVolume && AudioNotifyRepeat) {
										log.debug "Activating track ${Selection[3]} on $it $AudioNotifyRepeatCount times"
										it.playRepeatTrack(Selection[3], AudioNotifyRepeatCount)
									}
									if (AudioNotifyAtVolume && !AudioNotifyRepeat) {
										log.debug "Activating track ${Selection[3]} on $it at volume $AudioNotifyVolume"
										it.playTrackAtVolume(Selection[3], AudioNotifyVolume)
									}
									if (AudioNotifyAtVolume && AudioNotifyRepeat) {
										log.debug "Activating track ${Selection[3]} on $it at volume $AudioNotifyVolume $AudioNotifyRepeatCount times"
										it.playRepeatTrackAtVolume(Selection[3], AudioNotifyVolume, AudioNotifyRepeatCount)
									}
							}
						}
					}
				} else {
					AudioNotify.each {
						if (NotifyOnce) {
								boolean EventFound = false
								boolean TriggerEventFound = false
								def Events = it.eventsBetween(new Date(Now.getTime() - NotifyOnceinSeconds*600 - 600), new Date(Now.getTime() - 6000), [max: 20])
								if (Events.size() == 0) {
										if (!AudioNotifyAtVolume && !AudioNotifyRepeat) {
											log.debug "Activating track ${Selection[3]} on $it"
											it.playTrack(Selection[3])
										}
										if (!AudioNotifyAtVolume && AudioNotifyRepeat) {
											log.debug "Activating track ${Selection[3]} on $it $AudioNotifyRepeatCount times"
											it.playRepeatTrack(Selection[3], AudioNotifyRepeatCount)
										}
										if (AudioNotifyAtVolume && !AudioNotifyRepeat) {
											log.debug "Activating track ${Selection[3]} on $it at volume $AudioNotifyVolume"
											it.playTrackAtVolume(Selection[3], AudioNotifyVolume)
										}
										if (AudioNotifyAtVolume && AudioNotifyRepeat) {
											log.debug "Activating track ${Selection[3]} on $it at volume $AudioNotifyVolume $AudioNotifyRepeatCount times"
											it.playRepeatTrackAtVolume(Selection[3], AudioNotifyVolume, AudioNotifyRepeatCount)
										}
									} else {
										for ( def k =  Events.size(); k >= 0; k--) {
											if (Events[k] != null) {if (Events[k].descriptionText != null) {if (Events[k].descriptionText.endsWith('status is play')) {EventFound = true}}}
										}
										if (EventFound) {
											//log.debug "-----Looking at events for ${InfoArray[2]}"
											def TriggerEvents = InfoArray[2].eventsBetween(new Date(Now.getTime() - NotifyOnceinSeconds*1000 - 600), new Date(Now.getTime()), [max: 20])//1000 = 1 second
											//log.debug "Time Between ${new Date(Now.getTime() - NotifyOnceinSeconds*1000 - 600)} and ${new Date(Now.getTime())}"
											for ( def k =  TriggerEvents.size(); k >= 1; k--) {
												if (InfoArray[4] == null) {if (TriggerEvents[k] != null) {if (TriggerEvents[k].descriptionText != null) {if (TriggerEvents[k].descriptionText.endsWith("${InfoArray[3]}")) {TriggerEventFound = true}}}}
												if (InfoArray[4] != null) {if (TriggerEvents[k] != null) {if (TriggerEvents[k].descriptionText != null) {if (TriggerEvents[k].descriptionText.endsWith("${InfoArray[3]}") || TriggerEvents[k].descriptionText.endsWith("${InfoArray[4]}")) {TriggerEventFound = true}}}}
												if (TriggerEvents[k] != null) {if (TriggerEvents[k].descriptionText != null) {log.trace "*****Event details $k= ${TriggerEvents[k].descriptionText} @ ${TriggerEvents[k].date}"}}
											}
										}
										//log.debug "-----TriggerEventFound = $TriggerEventFound"
										if (!TriggerEventFound) {EventFound = false}
										if (!EventFound) {
												if (!AudioNotifyAtVolume && !AudioNotifyRepeat) {
													log.debug "Activating track ${Selection[3]} on $it"
													it.playTrack(Selection[3])
												}
												if (!AudioNotifyAtVolume && AudioNotifyRepeat) {
													log.debug "Activating track ${Selection[3]} on $it $AudioNotifyRepeatCount times"
													it.playRepeatTrack(Selection[3], AudioNotifyRepeatCount)
												}
												if (AudioNotifyAtVolume && !AudioNotifyRepeat) {
													log.debug "Activating track ${Selection[3]} on $it at volume $AudioNotifyVolume"
													it.playTrackAtVolume(Selection[3], AudioNotifyVolume)
												}
												if (AudioNotifyAtVolume && AudioNotifyRepeat) {
													log.debug "Activating track ${Selection[3]} on $it at volume $AudioNotifyVolume $AudioNotifyRepeatCount times"
													it.playRepeatTrackAtVolume(Selection[3], AudioNotifyVolume, AudioNotifyRepeatCount)
												}
											} else {
												if (!TriggerEventFound) {
													log.debug "Event details = ${Events.descriptionText} | Triggered by = ${InfoArray[2]} being ${InfoArray[3]}"
													log.debug "Not changing $it since it has already played once in the last $NotifyOnceinSeconds seconds."
												}
												if (TriggerEventFound) {log.debug "Not changing $it since it has already played once in the last $NotifyOnceinSeconds seconds due to ${InfoArray[2]} being ${InfoArray[3]}."}
										}
								}
							} else {
								if (!AudioNotifyAtVolume && !AudioNotifyRepeat) {
									log.debug "Activating track ${Selection[3]} on $it"
									it.playTrack(Selection[3])
								}
								if (!AudioNotifyAtVolume && AudioNotifyRepeat) {
									log.debug "Activating track ${Selection[3]} on $it $AudioNotifyRepeatCount times"
									it.playRepeatTrack(Selection[3], AudioNotifyRepeatCount)
								}
								if (AudioNotifyAtVolume && !AudioNotifyRepeat) {
									log.debug "Activating track ${Selection[3]} on $it at volume $AudioNotifyVolume"
									it.playTrackAtVolume(Selection[3], AudioNotifyVolume)
								}
								if (AudioNotifyAtVolume && AudioNotifyRepeat) {
									log.debug "Activating track ${Selection[3]} on $it at volume $AudioNotifyVolume $AudioNotifyRepeatCount times"
									it.playRepeatTrackAtVolume(Selection[3], AudioNotifyVolume, AudioNotifyRepeatCount)
								}
						}
					}
			}
		break
		case "Away":
			setLocationMode("Away")
		break
		case "Home":
			setLocationMode("Home")
		break
		case "Night":
			setLocationMode("Night")
		break
		case "PushNotify":
			for ( def i = 1; i <= PushTextNumber; i++) {
				if ("${settings["PushTitle$i"].replaceAll(' ','')}"  == "${Selection[1]}") {
					log.debug "Sending ${settings["PushTitle$i"]} push message"
					sendPush(settings["PushText${settings["PushTitle$i"].replaceAll(' ','')}"])
				}
			}
		break
		case "SMSNotify":
			for ( def i = 1; i <= SMSTextNumber; i++) {
				if ("${settings["SMSTitle$i"].replaceAll(' ','')}"  == "${Selection[1]}") {
					if (settings["SMSPhoneNumber${settings["SMSTitle$i"].replaceAll(' ','')}"] != null) {
						if ( settings["SMSPhoneNumber${settings["SMSTitle$i"].replaceAll(' ','')}"].indexOf(";") > 1){
								def PhoneNumber = settings["SMSPhoneNumber${settings["SMSTitle$i"].replaceAll(' ','')}"].split(";")
								for ( def j = 0; j < PhoneNumber.size(); j++) {
									log.debug "Sending ${settings["SMSTitle$i"]} SMS message to ${PhoneNumber[j]}"
									sendSms(PhoneNumber[j] as String, settings["SMSText${settings["SMSTitle$i"].replaceAll(' ','')}"])
								}
							} else {
								log.debug "Sending ${settings["SMSTitle$i"]} SMS message to ${settings["SMSPhoneNumber${settings["SMSTitle$i"].replaceAll(' ','')}"]}"
								sendSms(settings["SMSPhoneNumber${settings["SMSTitle$i"].replaceAll(' ','')}"] as String, settings["SMSText${settings["SMSTitle$i"].replaceAll(' ','')}"])
						}
					}
				}
			}
		break
		case "Siren":
			if (Selection[0] == "AudioNotify") {
				def Now = new Date()
				if ("${Selection[1]}" != "all") {
						AudioNotify.each {
							if ("$it" == "${Selection[1]}") {
								if (NotifyOnce) {
										boolean EventFound = false
										boolean TriggerEventFound = false
										def Events = it.eventsBetween(new Date(Now.getTime() - NotifyOnceinSeconds*600 - 600), new Date(Now.getTime() - 6000), [max: 20])
										if (Events.size() == 0) {
												log.debug "Activating Siren on $it"
												it.both()
											} else {
												for ( def k =  Events.size(); k >= 0; k--) {
													if (Events[k] != null) {if (Events[k].descriptionText != null) {if (Events[k].descriptionText.endsWith('status is play')) {EventFound = true}}}
												}
												if (EventFound) {
													def TriggerEvents = InfoArray[2].eventsBetween(new Date(Now.getTime() - NotifyOnceinSeconds*1000 - 600), new Date(Now.getTime()), [max: 20])//1000 = 1 second
													for ( def k =  TriggerEvents.size(); k >= 1; k--) {if (TriggerEvents[k] != null) {if (TriggerEvents[k].descriptionText != null) {if (TriggerEvents[k].descriptionText.endsWith("${InfoArray[3]}")) {TriggerEventFound = true}}}}
												}
												if (!TriggerEventFound) {EventFound = false}
												if (!EventFound) {
														log.debug "Activating Siren on $it"
														it.both()
													} else {
														if (!TriggerEventFound) {
															log.debug "Event details = ${Events.descriptionText} | Triggered by = ${InfoArray[2]} being ${InfoArray[3]}"
															log.debug "Not changing $it since it has already played once in the last $NotifyOnceinSeconds seconds."
														}
														if (TriggerEventFound) {log.debug "Not changing $it since it has already played once in the last $NotifyOnceinSeconds seconds due to ${InfoArray[2]} being ${InfoArray[3]}."}
												}
										}
									} else {
										log.debug "Activating Siren on $it"
										it.both()
								}
							}
						}
					} else {
						AudioNotify.each {
							if (NotifyOnce) {
									boolean EventFound = false
									boolean TriggerEventFound = false
									def Events = it.eventsBetween(new Date(Now.getTime() - NotifyOnceinSeconds*600 - 600), new Date(Now.getTime() - 6000), [max: 20])
									if (Events.size() == 0) {
											log.debug "Activating Siren on $it"
											it.both()
										} else {
											for ( def k =  Events.size(); k >= 0; k--) {
												if (Events[k] != null) {if (Events[k].descriptionText != null) {if (Events[k].descriptionText.endsWith('status is play')) {EventFound = true}}}
											}
											if (EventFound) {
												def TriggerEvents = InfoArray[2].eventsBetween(new Date(Now.getTime() - NotifyOnceinSeconds*1000 - 600), new Date(Now.getTime()), [max: 20])//1000 = 1 second
												for ( def k =  TriggerEvents.size(); k >= 1; k--) {if (TriggerEvents[k] != null) {if (TriggerEvents[k].descriptionText != null) {if (TriggerEvents[k].descriptionText.endsWith("${InfoArray[3]}")) {TriggerEventFound = true}}}}
											}
											if (!TriggerEventFound) {EventFound = false}
											if (!EventFound) {
													log.debug "Activating Siren on $it"
													it.both()
												} else {
													if (!TriggerEventFound) {
														log.debug "Event details = ${Events.descriptionText} | Triggered by = ${InfoArray[2]} being ${InfoArray[3]}"
														log.debug "Not changing $it since it has already played once in the last $NotifyOnceinSeconds seconds."
													}
													if (TriggerEventFound) {log.debug "Not changing $it since it has already played once in the last $NotifyOnceinSeconds seconds due to ${InfoArray[2]} being ${InfoArray[3]}."}
											}
									}
								} else {
									log.debug "Activating Siren on $it"
									it.both()
							}
						}
				}
			}
			if (Selection[0] == "Sirens") {
				if ("${Selection[1]}" != "all") {
						Sirens.each {
							if ("$it" == "${Selection[1]}") {log.debug "Sounding $it"}
							if ("$it" == "${Selection[1]}") {it.siren()}
						}
					} else {
						log.debug "Sounding All"
						Sirens.siren()
				}
				if (Notifications?.toBoolean()) sendPush(PushMessage ?: "Siren activated" )
				if (PhoneNumber != null) {
					if ( PhoneNumber.indexOf(";") > 1){
							def PhoneNumber = PhoneNumber.split(";")
							for ( def i = 0; i < PhoneNumber.size(); i++) {
								log.debug "Sending an SMS to ${PhoneNumber[i]}"
								sendSms(PhoneNumber[i] as String, SMSMessage ?:"Siren activated")
							}
						} else {
							log.debug "Sending an SMS to ${PhoneNumber}"
							sendSms(PhoneNumber as String, SMSMessage ?:"Siren activated")
					}
				}
			}
		break
		case "strobe":
			if ("${Selection[1]}" != "all") {
					Sirens.each {
						if ("$it" == "${Selection[1]}") {log.debug "Strobing $it"}
						if ("$it" == "${Selection[1]}") {it.strobe()}
					}
				} else {
					log.debug "Strobing All Sirens"
					Sirens.strobe()
			}
			if (Notifications?.toBoolean()) sendPush(PushMessage ?: "Strobe activated" )
			if (PhoneNumber != null) {
				if ( PhoneNumber.indexOf(";") > 1){
						def PhoneNumber = PhoneNumber.split(";")
						for ( def i = 0; i < PhoneNumber.size(); i++) {
							log.debug "Sending an SMS to ${PhoneNumber[i]}"
							sendSms(PhoneNumber[i] as String, SMSMessage ?:"Strobe activated")
						}
					} else {
						log.debug "Sending an SMS to ${PhoneNumber}"
						sendSms(PhoneNumber as String, SMSMessage ?:"Strobe activated")
				}
			}
		break
		case "both":
			if ("${Selection[1]}" != "all") {
					Sirens.each {
						if ("$it" == "${Selection[1]}") {log.debug "Sounding & Strobing $it"}
						if ("$it" == "${Selection[1]}") {it.both()}
					}
				} else {
					log.debug "Sounding & Strobing All Sirens"
					Sirens.both()
			}
			if (Notifications?.toBoolean()) sendPush(PushMessage ?: "Siren & Strobe activated" )
			if (PhoneNumber != null) {
				if ( PhoneNumber.indexOf(";") > 1){
						def PhoneNumber = PhoneNumber.split(";")
						for ( def i = 0; i < PhoneNumber.size(); i++) {
							log.debug "Sending an SMS to ${PhoneNumber[i]}"
							sendSms(PhoneNumber[i] as String, SMSMessage ?:"Siren & Strobe activated")
						}
					} else {
						log.debug "Sending an SMS to ${PhoneNumber}"
						sendSms(PhoneNumber as String, SMSMessage ?:"Siren & Strobe activated")
				}
			}
		break
		case "toggle":
			toggle_on_off(Selection[1],Selection[0])
		break
		case "brighter":
			adjustbrightness(Selection[1],Selection[0], true, false)
		break
		case "dimmer":
			adjustbrightness(Selection[1],Selection[0], false, false)
		break
		case "CustomColor":
			ChangeColor(Selection[1],Selection[0],CustomHue,CustomSaturation)
			Bulbs.each {
				for ( def i = 0; i < it.capabilities.size(); i++) {
					if (it.capabilities[i].name as String == "Color Temperature") {
						if ("${Selection[1]}" != "all") {
								if ("$it" == "${Selection[1]}") {
									log.debug "Changing $it to $CustomKalvins"
									it.setColorTemperature(CustomKalvins)
								}
							} else {
								log.debug "Changing $it to $CustomKalvins"
								it.setColorTemperature(CustomKalvins)
						}
					}
				}
			}
		break;
		case "White":
			hueColor = 52
			saturation = 19
			ChangeColor(Selection[1],Selection[0],hueColor,saturation)
		break;
		case "Daylight":
			hueColor = 53
			saturation = 91
			ChangeColor(Selection[1],Selection[0],hueColor,saturation)
		break;
		case "Soft White":
			hueColor = 23
			saturation = 56
			ChangeColor(Selection[1],Selection[0],hueColor,saturation)
		break;
		case "Warm White":
			hueColor = 20
			saturation = 80
			ChangeColor(Selection[1],Selection[0],hueColor,saturation)
		break;
		case "Blue":
			hueColor = 70
			ChangeColor(Selection[1],Selection[0],hueColor,saturation)
		break;
		case "Green":
			hueColor = 39
			ChangeColor(Selection[1],Selection[0],hueColor,saturation)
		break;
		case "Yellow":
			hueColor = 25
			ChangeColor(Selection[1],Selection[0],hueColor,saturation)
			break;
		case "Orange":
			hueColor = 10
			ChangeColor(Selection[1],Selection[0],hueColor,saturation)
		break;
		case "Purple":
			hueColor = 75
			ChangeColor(Selection[1],Selection[0],hueColor,saturation)
		break;
		case "Pink":
			hueColor = 83
			ChangeColor(Selection[1],Selection[0],hueColor,saturation)
		break;
		case "Red":
			hueColor = 100
			ChangeColor(Selection[1],Selection[0],hueColor,saturation)
		break;
		case "favLevel":
			if (InfoArray) {
					Favorite(Selection[1],Selection[0],InfoArray)
				} else {
					Favorite(Selection[1],Selection[0],null)
			}
		break
		case "full":
			if (Selection[0] == "Bulbs") {
				boolean SwitchChanged = false
				boolean MotionChanged = false
				boolean ContactChanged = false
				boolean ButtonPushed = false
				boolean ButtonHeld = false
				Bulbs.each {
					if ("$it" == "${Selection[1]}" || "${Selection[1]}" == "all") {
						if (it.currentValue("switch") != "on") {
							log.debug "Turning $it to full"
							it.setLevel(100)
							if (Switch) {
								if (settings["TurnOffAfterMinutes${settings["SwitchOn${InfoArray[0]}"]}"]) {
									SwitchChanged = true
									log.debug "Setting timer for ${settings["TurnOffAfterMinutes${settings["SwitchOn${InfoArray[0]}"]}"]} minute(s) to change $it to off"
								}
							}
							if (Motion) {
								if (settings["TurnOffAfterMinutes${settings["MotionActive${InfoArray[0]}"]}"]) {
									MotionChanged = true
									log.debug "Setting timer for ${settings["TurnOffAfterMinutes${settings["MotionActive${InfoArray[0]}"]}"]} minute(s) to change $it to off"
									//atomicState.FirstRun = true
								}
							}
							if (Contact) {
								if (settings["TurnOffAfterMinutes${settings["ContactOpen${InfoArray[0]}"]}"]) {
									ContactChanged = true
									log.debug "Setting timer for ${settings["TurnOffAfterMinutes${settings["ContactOpen${InfoArray[0]}"]}"]} minute(s) to change $it to off"
								}
							}
							if (Button) {
								if (settings["TurnOffAfterMinutes${InfoArray[0]}Button${InfoArray[1]}Push"]) {
									ButtonPushed = true
									log.debug "Setting timer for ${settings["TurnOffAfterMinutes${InfoArray[0]}Button${InfoArray[1]}Push"]} minute(s) to change $it to off"
								}
								if (settings["TurnOffAfterMinutes${InfoArray[0]}Button${InfoArray[1]}Hold"]) {
									ButtonHeld = true
									log.debug "Setting timer for ${settings["TurnOffAfterMinutes${InfoArray[0]}Button${InfoArray[1]}Hold"]} minute(s) to change $it to off"
								}
							}
						}
					}
				}
				if (SwitchChanged) {runIn(settings["TurnOffAfterMinutes${settings["SwitchOn${InfoArray[0]}"]}"]*60, TurnOffBulbs_Switch)}
				if (MotionChanged) {runIn(settings["TurnOffAfterMinutes${settings["MotionActive${InfoArray[0]}"]}"]*60, TurnOffBulbs_Motion)}
				if (ContactChanged) {runIn(settings["TurnOffAfterMinutes${settings["ContactOpen${InfoArray[0]}"]}"]*60, TurnOffBulbs_Contact)}
				if (ButtonPushed) {runIn(settings["TurnOffAfterMinutes${InfoArray[0]}Button${InfoArray[1]}Push"]*60, TurnOffBulbs_Push)}
				if (ButtonHeld) {runIn(settings["TurnOffAfterMinutes${InfoArray[0]}Button${InfoArray[1]}Hold"]*60, TurnOffBulbs_Hold)}
			}
			if (Selection[0] == "Dimmers") {
				boolean SwitchChanged = false
				boolean MotionChanged = false
				boolean ContactChanged = false
				boolean ButtonPushed = false
				boolean ButtonHeld = false
				Dimmers.each {
					if ("$it" == "${Selection[1]}" || "${Selection[1]}" == "all") {
						if (it.currentValue("switch") != "on") {
							log.debug "Turning $it to full"
							it.setLevel(100)
							if (Switch) {
								if (settings["TurnOffAfterMinutes${settings["SwitchOn${InfoArray[0]}"]}"]) {
									SwitchChanged = true
									log.debug "Setting timer for ${settings["TurnOffAfterMinutes${settings["SwitchOn${InfoArray[0]}"]}"]} minute(s) to change $it to off"
								}
							}
							if (Motion) {
								if (settings["TurnOffAfterMinutes${settings["MotionActive${InfoArray[0]}"]}"]) {
									MotionChanged = true
									log.debug "Setting timer for ${settings["TurnOffAfterMinutes${settings["MotionActive${InfoArray[0]}"]}"]} minute(s) to change $it to off"
									//atomicState.FirstRun = true
								}
							}
							if (Contact) {
								if (settings["TurnOffAfterMinutes${settings["ContactOpen${InfoArray[0]}"]}"]) {
									ContactChanged = true
									log.debug "Setting timer for ${settings["TurnOffAfterMinutes${settings["ContactOpen${InfoArray[0]}"]}"]} minute(s) to change $it to off"
								}
							}
							if (Button) {
								if (settings["TurnOffAfterMinutes${InfoArray[0]}Button${InfoArray[1]}Push"]) {
									ButtonPushed = true
									log.debug "Setting timer for ${settings["TurnOffAfterMinutes${InfoArray[0]}Button${InfoArray[1]}Push"]} minute(s) to change $it to off"
								}
								if (settings["TurnOffAfterMinutes${InfoArray[0]}Button${InfoArray[1]}Hold"]) {
									ButtonHeld = true
									log.debug "Setting timer for ${settings["TurnOffAfterMinutes${InfoArray[0]}Button${InfoArray[1]}Hold"]} minute(s) to change $it to off"
								}
							}
						}
					}
				}
				if (SwitchChanged) {runIn(settings["TurnOffAfterMinutes${settings["SwitchOn${InfoArray[0]}"]}"]*60, TurnOffDimmers_Switch)}
				if (MotionChanged) {runIn(settings["TurnOffAfterMinutes${settings["MotionActive${InfoArray[0]}"]}"]*60, TurnOffDimmers_Motion)}
				if (ContactChanged) {runIn(settings["TurnOffAfterMinutes${settings["ContactOpen${InfoArray[0]}"]}"]*60, TurnOffDimmers_Contact)}
				if (ButtonPushed) {runIn(settings["TurnOffAfterMinutes${InfoArray[0]}Button${InfoArray[1]}Push"]*60, TurnOffDimmers_Push)}
				if (ButtonHeld) {runIn(settings["TurnOffAfterMinutes${InfoArray[0]}Button${InfoArray[1]}Hold"]*60, TurnOffDimmers_Hold)}
			}
		break
		case "temperature":
			if (Selection[0] == "Bulbs") {
				Bulbs.each {
					for ( def i = 0; i < it.capabilities.size(); i++) {
						if (it.capabilities[i].name as String == "Color Temperature") {
							if ("${Selection[1]}" != "all") {
									if ("$it" == "${Selection[1]}") {
										log.debug "Changing $it to $BulbKalvins"
										it.setColorTemperature(BulbKalvins)
									}
								} else {
									log.debug "Changing $it to $BulbKalvins"
									it.setColorTemperature(BulbKalvins)
							}
						}
					}
				}
			}
			if (Selection[0] == "Dimmers") {
				Dimmers.each {
					for ( def i = 0; i < it.capabilities.size(); i++) {
						if (it.capabilities[i].name as String == "Color Temperature") {
							if ("${Selection[1]}" != "all") {
									if ("$it" == "${Selection[1]}") {
										log.debug "Changing $it to $DimmerKalvins"
										it.setColorTemperature(DimmerKalvins)
									}
								} else {
									log.debug "Changing $it to $DimmerKalvins"
									it.setColorTemperature(DimmerKalvins)
							}
						}
					}
				}
			}
		break
		case "open":
			if (Selection[0] == "GDCs") {
				if ("${Selection[1]}" != "all") {
						GDCs.each {
							if ("$it" == "${Selection[1]}") {log.debug "Opening $it"}
							if ("$it" == "${Selection[1]}") {it.open()}
						}
					} else {
						log.debug "Opening All garage doors"
						GDCs.open()
				}
			}
			if (Selection[0] == "DoorControls") {
				if ("${Selection[1]}" != "all") {
						DoorControls.each {
							if ("$it" == "${Selection[1]}") {log.debug "Opening $it"}
							if ("$it" == "${Selection[1]}") {it.open()}
						}
					} else {
						log.debug "Opening All door controllers"
						DoorControls.open()
				}
			}
			if (Selection[0] == "Valves") {
				if ("${Selection[1]}" != "all") {
						Valves.each {
							if ("$it" == "${Selection[1]}") {log.debug "Opening $it"}
							if ("$it" == "${Selection[1]}") {it.open()}
						}
					} else {
						log.debug "Opening All Valves"
						Valves.open()
				}
			}
			if (Selection[0] == "Shades") {
				if ("${Selection[1]}" != "all") {
						Shades.each {
							if ("$it" == "${Selection[1]}") {log.debug "Opening $it"}
							if ("$it" == "${Selection[1]}") {it.open()}
						}
					} else {
						log.debug "Opening All Shades"
						Shades.open()
				}
			}
		break
		case "close":
			if (Selection[0] == "GDCs") {
				if ("${Selection[1]}" != "all") {
						GDCs.each {
							if ("$it" == "${Selection[1]}") {log.debug "Closing $it"}
							if ("$it" == "${Selection[1]}") {it.close()}
						}
					} else {
						log.debug "Closing All garage doors"
						GDCs.close()
				}
			}
			if (Selection[0] == "DoorControls") {
				if ("${Selection[1]}" != "all") {
						DoorControls.each {
							if ("$it" == "${Selection[1]}") {log.debug "Closing $it"}
							if ("$it" == "${Selection[1]}") {it.close()}
						}
					} else {
						log.debug "Closing All door controllers"
						DoorControls.close()
				}
			}
			if (Selection[0] == "Valves") {
				if ("${Selection[1]}" != "all") {
						Valves.each {
							if ("$it" == "${Selection[1]}") {log.debug "Closing $it"}
							if ("$it" == "${Selection[1]}") {it.close()}
						}
					} else {
						log.debug "Closing All Valves"
						Valves.close()
				}
			}
			if (Selection[0] == "Shades") {
				if ("${Selection[1]}" != "all") {
						Shades.each {
							if ("$it" == "${Selection[1]}") {log.debug "Closing $it"}
							if ("$it" == "${Selection[1]}") {it.close()}
						}
					} else {
						log.debug "Closing All Shades"
						Shades.close()
				}
			}
		break
		case "preset":
			if (Selection[0] == "Shades") {
				if ("${Selection[1]}" != "all") {
						Shades.each {
							if ("$it" == "${Selection[1]}") {log.debug "Setting $it to preset"}
							if ("$it" == "${Selection[1]}") {it.presetPosition()}
						}
					} else {
						log.debug "Setting All Shades to preset"
						Shades.presetPosition()
				}
			}
		break
		case "lock":
			if (Selection[0] == "Locks") {
				if ("${Selection[1]}" != "all") {
						Locks.each {
							if ("$it" == "${Selection[1]}") {log.debug "Locking $it"}
							if ("$it" == "${Selection[1]}") {it.lock()}
						}
					} else {
						log.debug "Locking All"
						Locks.lock()
				}
			}
		break
		case "unlock":
			if (Selection[0] == "Locks") {
				if ("${Selection[1]}" != "all") {
						Locks.each {
							if ("$it" == "${Selection[1]}") {log.debug "Unlocking $it"}
							if ("$it" == "${Selection[1]}") {it.unlock()}
						}
					} else {
						log.debug "Unlocking All"
						Locks.unlock()
				}
			}
		break
		case "take":
			if (Selection[0] == "Images") {
				if ("${Selection[1]}" != "all") {
						Images.each {
							if ("$it" == "${Selection[1]}") {
								if (ImageNumber) {
										//ImageNumber = ImageNumber as Integer
										log.debug "Taking $ImageNumber shots with $it"
										for ( def i = 1; i <= ImageNumber; i++) {
											it.take()
										}
									} else {
										log.debug "Taking a still shot with $it"
										it.take()
								}
							}
						}
					} else {
						if (ImageNumber) {
								//def ImageNumber = ImageNumber as Integer
								log.debug "Taking $ImageNumber shots with $Images"
								for ( def i = 1; i <= ImageNumber; i++) {
									Images.take()
								}
							} else {
								log.debug "Taking a still shot with $Images"
								Images.take()
						}
				}
			}
		break
		case "play":
			if (Selection[0] == "Speakers") {
				if ("${Selection[1]}" != "all") {
						Speakers.each {
							if ("$it" == "${Selection[1]}") {log.debug "Playing $it"}
							if ("$it" == "${Selection[1]}") {it.play()}
						}
					} else {
						log.debug "Playing All"
						Speakers.play()
				}
			}
		break
		case "pause":
			if (Selection[0] == "Speakers") {
				if ("${Selection[1]}" != "all") {
						Speakers.each {
							if ("$it" == "${Selection[1]}") {log.debug "Pausing $it"}
							if ("$it" == "${Selection[1]}") {it.pause()}
						}
					} else {
						log.debug "Pausing All"
						Speakers.pause()
				}
 			}
	   break
		case "pp_toggle":
			if (Selection[0] == "Speakers") {togglePlayPause(Selection[1])}
		break
		case "mute":
			if (Selection[0] == "Speakers") {
				if ("${Selection[1]}" != "all") {
						Speakers.each {
							if ("$it" == "${Selection[1]}") {log.debug "Muting $it"}
							if ("$it" == "${Selection[1]}") {it.mute()}
						}
					} else {
						log.debug "Muting All"
						Speakers.mute()
				}
 			}
		break
		case "unmute":
			if (Selection[0] == "Speakers") {
				if ("${Selection[1]}" != "all") {
						Speakers.each {
							if ("$it" == "${Selection[1]}") {log.debug "Unmuting $it"}
							if ("$it" == "${Selection[1]}") {it.unmute()}
						}
					} else {
						log.debug "Unmuting All"
						Speakers.unmute()
				}
 			}
		break
		case "next":
			if (Selection[0] == "Speakers") {
				if ("${Selection[1]}" != "all") {
						Speakers.each {
							if ("$it" == "${Selection[1]}") {log.debug " Moving $it to next track"}
							if ("$it" == "${Selection[1]}") {it.nextTrack()}
						}
					} else {
						log.debug "Moving All to next track"
						Speakers.nextTrack()
				}
 			}
		break
		case "previous":
			if (Selection[0] == "Speakers") {
				if ("${Selection[1]}" != "all") {
						Speakers.each {
							if ("$it" == "${Selection[1]}") {log.debug " Moving $it to previous track"}
							if ("$it" == "${Selection[1]}") {it.previousTrack()}
						}
					} else {
						log.debug "Moving All to previous track"
						Speakers.previousTrack()
				}
 			}
		break
		case "vol_up":
			if (Selection[0] == "Speakers") {adjustVolume(Selection[1],true, false)}
		break
		case "vol_down":
			if (Selection[0] == "Speakers") {adjustVolume(Selection[1],false, false)}
		break
	}
}

def ChangeColor(selection,DeviceName,hueColor,saturation) {
	if (selection != "all") {
			Bulbs.each {
				//def currentLevel = it.currentValue("level")
				if ("$it" == selection) {
					def value = [switch: "on", hue: hueColor, saturation: saturation]//, level: currentLevel as Integer ?: 100
					log.debug "$it, setColor($value)"
					it.setColor(value)
				}
			}
		} else {
			def value = [switch: "on", hue: hueColor, saturation: saturation]
			log.debug "All, setColor($value)"
			Bulbs.setColor(value)
	}
}

def toggle_on_off(selection,DeviceName) {
	if (DeviceName == "Bulbs") {
		if (selection != "all") {
				Bulbs.each {
					if ("$it" == selection) {log.debug "Toggling on/off $it"}
					if ("$it" == selection) {
						def currentStatus = it.currentValue("switch")
						log.debug "currentStatus = $currentStatus"
						if (currentStatus == "on") {
								options ? it.off(options) : it.off()
							} else {
								options ? it.on(options) : it.on()
						}
					}
				}
			} else {
				log.debug "Toggling on/off All"
				Bulbs.each {
					def currentStatus = it.currentValue("switch")
					log.debug "currentStatus = $currentStatus"
					if (currentStatus == "on") {
							options ? it.off(options) : it.off()
						} else {
							options ? it.on(options) : it.on()
					}
				}
		}
	}
	if (DeviceName == "Dimmers") {
		if (selection != "all") {
				Dimmers.each {
					if ("$it" == selection) {log.debug "Toggling on/off $it"}
					if ("$it" == selection) {
						def currentStatus = it.currentValue("switch")
						log.debug "currentStatus = $currentStatus"
						if (currentStatus == "on") {
								options ? it.off(options) : it.off()
							} else {
								options ? it.on(options) : it.on()
						}
					}
				}
			} else {
				log.debug "Toggling on/off All"
				Dimmers.each {
					def currentStatus = it.currentValue("switch")
					log.debug "currentStatus = $currentStatus"
					if (currentStatus == "on") {
							options ? it.off(options) : it.off()
						} else {
							options ? it.on(options) : it.on()
					}
				}
		}
	}
	if (DeviceName == "Switches") {
		if (selection != "all") {
				Switches.each {
					if ("$it" == selection) {log.debug "Toggling on/off $it"}
					if ("$it" == selection) {
						def currentStatus = it.currentValue("switch")
						log.debug "currentStatus = $currentStatus"
						if (currentStatus == "on") {
								options ? it.off(options) : it.off()
							} else {
								options ? it.on(options) : it.on()
						}
					}
				}
			} else {
				log.debug "Toggling on/off All"
				Switches.each {
					def currentStatus = it.currentValue("switch")
					log.debug "currentStatus = $currentStatus"
					if (currentStatus == "on") {
							options ? it.off(options) : it.off()
						} else {
							options ? it.on(options) : it.on()
					}
				}
		}
	}
}

def Favorite(selection,DeviceName,InfoArray) {
	if (DeviceName == "Bulbs") {
		boolean SwitchChanged = false
		boolean MotionChanged = false
		boolean ContactChanged = false
		boolean ButtonPushed = false
		boolean ButtonHeld = false
		Bulbs.each {
			if ("$it" == selection || selection == "all") {
				if (it.currentValue("switch") != "on") {
					log.debug "Set $it to $BulbsFav"
					it.setLevel(BulbsFav as Integer)
					if (Switch) {
						if (settings["TurnOffAfterMinutes${settings["SwitchOn${InfoArray[0]}"]}"]) {
							SwitchChanged = true
							log.debug "Setting timer for ${settings["TurnOffAfterMinutes${settings["SwitchOn${InfoArray[0]}"]}"]} minute(s) to change $it to off"
						}
					}
					if (Motion) {
						if (settings["TurnOffAfterMinutes${settings["MotionActive${InfoArray[0]}"]}"]) {
							MotionChanged = true
							log.debug "Setting timer for ${settings["TurnOffAfterMinutes${settings["MotionActive${InfoArray[0]}"]}"]} minute(s) to change $it to off"
							//atomicState.FirstRun = true
						}
					}
					if (Contact) {
						if (settings["TurnOffAfterMinutes${settings["ContactOpen${InfoArray[0]}"]}"]) {
							ContactChanged = true
							log.debug "Setting timer for ${settings["TurnOffAfterMinutes${settings["ContactOpen${InfoArray[0]}"]}"]} minute(s) to change $it to off"
						}
					}
					if (Button) {
						if (settings["TurnOffAfterMinutes${InfoArray[0]}Button${InfoArray[1]}Push"]) {
							ButtonPushed = true
							log.debug "Setting timer for ${settings["TurnOffAfterMinutes${InfoArray[0]}Button${InfoArray[1]}Push"]} minute(s) to change $it to off"
						}
						if (settings["TurnOffAfterMinutes${InfoArray[0]}Button${InfoArray[1]}Hold"]) {
							ButtonHeld = true
							log.debug "Setting timer for ${settings["TurnOffAfterMinutes${InfoArray[0]}Button${InfoArray[1]}Hold"]} minute(s) to change $it to off"
						}
					}
				}
			}
		}
		if (SwitchChanged) {runIn(settings["TurnOffAfterMinutes${settings["SwitchOn${InfoArray[0]}"]}"]*60, TurnOffBulbs_Switch)}
		if (MotionChanged) {runIn(settings["TurnOffAfterMinutes${settings["MotionActive${InfoArray[0]}"]}"]*60, TurnOffBulbs_Motion)}
		if (ContactChanged) {runIn(settings["TurnOffAfterMinutes${settings["ContactOpen${InfoArray[0]}"]}"]*60, TurnOffBulbs_Contact)}
		if (ButtonPushed) {runIn(settings["TurnOffAfterMinutes${InfoArray[0]}Button${InfoArray[1]}Push"]*60, TurnOffBulbs_Push)}
		if (ButtonHeld) {runIn(settings["TurnOffAfterMinutes${InfoArray[0]}Button${InfoArray[1]}Hold"]*60, TurnOffBulbs_Hold)}
	}
	if (DeviceName == "Dimmers") {
		boolean SwitchChanged = false
		boolean MotionChanged = false
		boolean ContactChanged = false
		boolean ButtonPushed = false
		boolean ButtonHeld = false
		Dimmers.each {
			if ("$it" == selection || selection == "all") {
				if (it.currentValue("switch") != "on") {
					log.debug "Set $it to $DimmersFav"
					it.setLevel(DimmersFav as Integer)
					if (Switch) {
						if (settings["TurnOffAfterMinutes${settings["SwitchOn${InfoArray[0]}"]}"]) {
							SwitchChanged = true
							log.debug "Setting timer for ${settings["TurnOffAfterMinutes${settings["SwitchOn${InfoArray[0]}"]}"]} minute(s) to change $it to off"
						}
					}
					if (Motion) {
						if (settings["TurnOffAfterMinutes${settings["MotionActive${InfoArray[0]}"]}"]) {
							MotionChanged = true
							log.debug "Setting timer for ${settings["TurnOffAfterMinutes${settings["MotionActive${InfoArray[0]}"]}"]} minute(s) to change $it to off"
							//atomicState.FirstRun = true
						}
					}
					if (Contact) {
						if (settings["TurnOffAfterMinutes${settings["ContactOpen${InfoArray[0]}"]}"]) {
							ContactChanged = true
							log.debug "Setting timer for ${settings["TurnOffAfterMinutes${settings["ContactOpen${InfoArray[0]}"]}"]} minute(s) to change $it to off"
						}
					}
					if (Button) {
						if (settings["TurnOffAfterMinutes${InfoArray[0]}Button${InfoArray[1]}Push"]) {
							ButtonPushed = true
							log.debug "Setting timer for ${settings["TurnOffAfterMinutes${InfoArray[0]}Button${InfoArray[1]}Push"]} minute(s) to change $it to off"
						}
						if (settings["TurnOffAfterMinutes${InfoArray[0]}Button${InfoArray[1]}Hold"]) {
							ButtonHeld = true
							log.debug "Setting timer for ${settings["TurnOffAfterMinutes${InfoArray[0]}Button${InfoArray[1]}Hold"]} minute(s) to change $it to off"
						}
					}
				}
			}
		}
		if (SwitchChanged) {runIn(settings["TurnOffAfterMinutes${settings["SwitchOn${InfoArray[0]}"]}"]*60, TurnOffDimmers_Switch)}
		if (MotionChanged) {runIn(settings["TurnOffAfterMinutes${settings["MotionActive${InfoArray[0]}"]}"]*60, TurnOffDimmers_Motion)}
		if (ContactChanged) {runIn(settings["TurnOffAfterMinutes${settings["ContactOpen${InfoArray[0]}"]}"]*60, TurnOffDimmers_Contact)}
		if (ButtonPushed) {runIn(settings["TurnOffAfterMinutes${InfoArray[0]}Button${InfoArray[1]}Push"]*60, TurnOffDimmers_Push)}
		if (ButtonHeld) {runIn(settings["TurnOffAfterMinutes${InfoArray[0]}Button${InfoArray[1]}Hold"]*60, TurnOffDimmers_Hold)}
	}
}

def adjustbrightness(selection,DeviceName, boolean up, boolean doubleSpeed) {
	if (DeviceName == "Bulbs") {
		if (selection != "all") {
				log.debug "Brighten $selection"
				Bulbs.each {
					if ("$it" == selection) {
						def changeAmount = 10 * (doubleSpeed ? 2 : 1)
						def currentLevel = it.currentValue("level")
						if(up) {
								log.debug "Brighten $it to ${currentLevel + changeAmount}"
								it.setLevel(currentLevel + changeAmount)
							} else {
								log.debug "Brighten $it to ${currentLevel - changeAmount}"
								it.setLevel(currentLevel - changeAmount)
						}
					}
				}
			} else {
				log.debug "Brighten All"
				Bulbs.each {
					def changeAmount = 10 * (doubleSpeed ? 2 : 1)
					def currentLevel = it.currentValue("level")
					if(up) {
							log.debug "Brighten $it to ${currentLevel + changeAmount}"
							it.setLevel(currentLevel + changeAmount)
						} else {
							log.debug "Brighten $it to ${currentLevel - changeAmount}"
							it.setLevel(currentLevel - changeAmount)
					}
				}
		}
	}
	if (DeviceName == "Dimmers") {
		if (selection != "all") {
				log.debug "Brighten $selection"
				Dimmers.each {
					if ("$it" == selection) {log.debug "Brighten $it"}
					if ("$it" == selection) {
						def changeAmount = 10 * (doubleSpeed ? 2 : 1)
						def currentLevel = it.currentValue("level")
						if(up) {
								log.debug "Brighten $it to ${currentLevel + changeAmount}"
								it.setLevel(currentLevel + changeAmount)
							} else {
								log.debug "Brighten $it to ${currentLevel - changeAmount}"
								it.setLevel(currentLevel - changeAmount)
						}
					}
				}
			} else {
				log.debug "Brighten All"
				Dimmers.each {
					def changeAmount = 10 * (doubleSpeed ? 2 : 1)
					def currentLevel = it.currentValue("level")
					if(up) {
							log.debug "Brighten $it to ${currentLevel + changeAmount}"
							it.setLevel(currentLevel + changeAmount)
						} else {
							log.debug "Brighten $it to ${currentLevel - changeAmount}"
							it.setLevel(currentLevel - changeAmount)
					}
				}
		}
	}
}

def togglePlayPause(selection) {
	if (selection != "all") {
			Speakers.each {
				if ("$it" == selection) {log.debug "Toggling Play/Pause on $it"}
				if ("$it" == selection) {
					def currentStatus = it.currentValue("status")
					log.debug "currentStatus = $currentStatus"
					if (currentStatus == "playing") {
							options ? it.pause(options) : it.pause()
						} else {
							options ? it.play(options) : it.play()
					}
				}
			}
		} else {
			log.debug "Toggling Play/Pause on All"
			Speakers.each {
				def currentStatus = it.currentValue("status")
				log.debug "currentStatus = $currentStatus"
				if (currentStatus == "playing") {
						options ? it.pause(options) : it.pause()
					} else {
						options ? it.play(options) : it.play()
				}
			}
	}
}

def adjustVolume(selection, boolean up, boolean doubleSpeed) {
	if (selection != "all") {
			Speakers.each {
			if ("$it" == selection) {
					def changeAmount = (volumeOffset as Integer ?: 5) * (doubleSpeed ? 2 : 1)
					def currentVolume = it.currentValue("level")
					if(up) {
							log.debug "Turning up the volume on $it"
							it.setLevel(currentVolume + changeAmount)
						} else {
							log.debug "Turning down the volume on $it"
							it.setLevel(currentVolume - changeAmount)
					}
				}
			}
		} else {
			Speakers.each {
				def changeAmount = (volumeOffset as Integer ?: 5) * (doubleSpeed ? 2 : 1)
				def currentVolume = it.currentValue("level")
				if(up) {
						log.debug "Turning up the volume on All"
						it.setLevel(currentVolume + changeAmount)
					} else {
						log.debug "Turning up the volume on All"
						it.setLevel(currentVolume - changeAmount)
				}
			}
	}
}

private hideAllAlarmOptions() {
	(ChangeModes || Sirens) ? false : true
}

private hideAllDoorLockOptions() {
	(GDCs || Locks) ? false : true
}

private hideAllImageOptions() {
	(Images) ? false : true
}

private hideAllSwitchOptions() {
	(Bulbs || Dimmers || Switches || Outlets || Relays) ? false : true
}

private hideValveOptions() {
	(Valves) ? false : true
}

private hideShadeOptions() {
	(Shades) ? false : true
}

private hideSpeakerOptions() {
	(Speakers) ? false : true
}

private hideButtonTriggers() {
	(Button) ? false : true
}

private hideSensorTriggers() {
	(Acceleration || Contact || Motion || Tamper || Shock || Sleep || Sound || Step || Touch) ? false : true
}

private hidePresenceTriggers() {
	(Beacon || Presence) ? false : true
}

private hideSafetyTriggers() {
	(CO2Measurement || CODetector || Smoke || soundPressureLevel || Water) ? false : true
}

private hideEnergyTriggers() {
	(Battery || EnergyMeter || PowerMeter || PowerSource || voltage) ? false : true
}

private hideOtherTriggers() {
	(Temperature || Valve || Shade || Humidity || UltravioletIndex || phMeasurement) ? false : true
}

private hideAllNotificationOptions() {
	(PushNotify || SMSNotify || AudioNotify) ? false : true
}

private hideSwitchDoorTriggers() {
	(Switch || Door || Illuminance) ? false : true
}

private hideAccelerationTriggers() {
	(Acceleration) ? false : true
}

private hideContactTriggers() {
	(Contact) ? false : true
}

private hideMotionTriggers() {
	(Motion) ? false : true
}

private hideTamperTriggers() {
	(Tamper) ? false : true
}

private hideShockTriggers() {
	(Shock) ? false : true
}

private hideSleepTriggers() {
	(Sleep) ? false : true
}

private hideSoundTriggers() {
	(Sound) ? false : true
}

private hideStepTriggers() {
	(Step) ? false : true
}

private hideTouchTriggers() {
	(Touch) ? false : true
}

private hideWaterTriggers() {
	(Water) ? false : true
}

private hideBeaconTriggers() {
	(Beacon) ? false : true
}

private hideindPresenceTriggers() {
	(Presence) ? false : true
}

private hideCO2MeasurementTriggers() {
	(CO2Measurement) ? false : true
}

private hideCODetectorTriggers() {
	(CODetector) ? false : true
}

private hideSmokeTriggers() {
	(Smoke) ? false : true
}

private hidesoundPressureLevelTriggers() {
	(soundPressureLevel) ? false : true
}

private hideEnergyMeterTriggers() {
	(EnergyMeter) ? false : true
}

private hidePowerMeterTriggers() {
	(PowerMeter) ? false : true
}

private hidePowerSourceTriggers() {
	(PowerSource) ? false : true
}

private hideVoltageTriggers() {
	(Voltage) ? false : true
}

private hideBatteryTriggers() {
	(Battery) ? false : true
}

private hideSwitchTriggers() {
	(Switch) ? false : true
}

private hideIlluminanceTriggers() {
	(Illuminance) ? false : true
}

private hideDoorTriggers() {
	(Door) ? false : true
}

private hideTemperatureTriggers() {
	(Temperature) ? false : true
}

private hideHumidityTriggers() {
	(Humidity) ? false : true
}

private hideUltravioletIndexTriggers() {
	(UltravioletIndex) ? false : true
}

private hidephMeasurementTriggers() {
	(phMeasurement) ? false : true
}

private hideValveTriggers() {
	(Valve) ? false : true
}

private hideShadeTriggers() {
	(Shade) ? false : true
}

private hideChangeModesActions() {
	(ChangeModes) ? false : true
}

private hideSirensActions() {
	(Sirens) ? false : true
}

private hidePushNotifyActions() {
	(PushNotify) ? false : true
}

private hideSMSNotifyActions() {
	(SMSNotify) ? false : true
}

private hideGDCsActions() {
	(GDCs) ? false : true
}

private hideLocksActions() {
	(Locks) ? false : true
}

private hideDoorControlsActions() {
	(DoorControls) ? false : true
}

private hideBulbsActions() {
	(Bulbs) ? false : true
}

private hideDimmersActions() {
	(Dimmers) ? false : true
}

private hideSwitchesActions() {
	(Switches) ? false : true
}

private hideOutletsActions() {
	(Outlets) ? false : true
}

private hideRelaysActions() {
	(Relays) ? false : true
}

private hideAudioNotifyActions() {
	(AudioNotify) ? false : true
}

// execution filter methods
private getAllOk() {
	modeOk && daysOk && timeOk && InstanceEnabled && CurrentIlluminance() && CurrentContact() && CurrentSwitch() && CurrentAcceleration() && CurrentMotion() && CurrentTamper() && CurrentShock() && CurrentSleep() && CurrentSound() && CurrentWater() && CurrentBeacon() && CurrentPresence() && CurrentCODetector() && CurrentSmoke() && CurrentPowerSource() && CurrentDoor() && CurrentValve() && CurrentShade() && CurrentTemperature() && CurrentPowerMeter() && CurrentVoltage() && CurrentEnergyMeter() && CurrentCO2Measurement() && CurrentHumidity() && CurrentUltravioletIndex() && CurrentphMeasurement() && CurrentsoundPressureLevel()
}

private getModeOk() {
	def result = !modes || modes.contains(location.mode)
//	log.trace "modeOk = $result"
	return result
}

private getDaysOk() {
	def result = true
	if (days) {
		def df = new java.text.SimpleDateFormat("EEEE")
		if (location.timeZone) {
			df.setTimeZone(location.timeZone)
		}
		else {
			df.setTimeZone(TimeZone.getTimeZone("America/New_York"))
		}
		def day = df.format(new Date())
		result = days.contains(day)
	}
//	log.trace "daysOk = $result"
	return result
}

private getTimeOk() {
	def result = true
	if ((starting && ending) ||
	(starting && endingX in ["Sunrise", "Sunset"]) ||
	(startingX in ["Sunrise", "Sunset"] && ending) ||
	(startingX in ["Sunrise", "Sunset"] && endingX in ["Sunrise", "Sunset"])) {
		def currTime = now()
		def start = null
		def stop = null
		def s = getSunriseAndSunset(zipCode: zipCode, sunriseOffset: startSunriseOffset, sunsetOffset: startSunsetOffset)
		if(startingX == "Sunrise") start = s.sunrise.time
		else if(startingX == "Sunset") start = s.sunset.time
		else if(starting) start = timeToday(starting,location.timeZone).time
		s = getSunriseAndSunset(zipCode: zipCode, sunriseOffset: endSunriseOffset, sunsetOffset: endSunsetOffset)
		if(endingX == "Sunrise") stop = s.sunrise.time
		else if(endingX == "Sunset") stop = s.sunset.time
		else if(ending) stop = timeToday(ending,location.timeZone).time
		result = start < stop ? currTime >= start && currTime <= stop : currTime <= stop || currTime >= start
	}
//	log.trace "getTimeOk = $result"
	return result
}

private hhmm(time, fmt = "h:mm a") {
	def t = timeToday(time, location.timeZone)
	def f = new java.text.SimpleDateFormat(fmt)
	f.setTimeZone(location.timeZone ?: timeZone(time))
	f.format(t)
}

private hideOptionsSection() {
	(starting || ending || days || modes || startingX || endingX || Illuminance_Required || Contact_Required || Switch_Required || Acceleration_Required || Motion_Required || Tamper_Required || Shock_Required || Sleep_Required || Sound_Required || Water_Required || Beacon_Required || Presence_Required || CODetector_Required || Smoke_Required || PowerSource_Required || Door_Required || Valve_Required || Shade_Required || Temperature_Required || PowerMeter_Required || Voltage_Required || EnergyMeter_Required || CO2Measurement_Required || Step_Required || Humidity_Required || UltravioletIndex_Required || phMeasurement_Required || soundPressureLevel_Required) ? false : true
}

private hideButtonActions() {
	boolean Hidden = true
	for ( def h = 0; h < Button.size(); h++) {
		int Num = 0
		if (settings["NumberofButtons${Button[h].id}"] != null) {Num = settings["NumberofButtons${Button[h].id}"] as Integer}
		if (Button[h].currentValue("numberOfButtons") != null) {Num = Button[h].currentValue("numberOfButtons") as Integer}
		for ( def i = 1; i <= Num; i++) {
			if (settings["${Button[h].id}Button${i}Push"] || settings["${Button[h].id}Button${i}Hold"]) {Hidden = false}
		}
	}
	Hidden
}

private hideAccelerationActions() {
	boolean Hidden = true
	for ( def i = 0; i < Acceleration.size(); i++) {
		if (settings["AccelerationActive${Acceleration[i].id}"] || settings["AccelerationInactive${Acceleration[i].id}"]) {Hidden = false}
	}
	Hidden
}

private hideContactActions() {
	boolean Hidden = true
	for ( def i = 0; i < Contact.size(); i++) {
		if (settings["ContactClosed${Contact[i].id}"] || settings["ContactOpen${Contact[i].id}"]) {Hidden = false}
	}
	Hidden
}

private hideMotionActions() {
	boolean Hidden = true
	for ( def i = 0; i < Motion.size(); i++) {
		if (settings["MotionActive${Motion[i].id}"] || settings["MotionInactive${Motion[i].id}"]) {Hidden = false}
	}
	Hidden
}

private hideTamperActions() {
	boolean Hidden = true
	for ( def i = 0; i < Tamper.size(); i++) {
		if (settings["TamperClear${Tamper[i].id}"] || settings["TamperDetected${Tamper[i].id}"]) {Hidden = false}
	}
	Hidden
}

private hideShockActions() {
	boolean Hidden = true
	for ( def i = 0; i < Shock.size(); i++) {
		if (settings["ShockClear${Shock[i].id}"] || settings["ShockDetected${Shock[i].id}"]) {Hidden = false}
	}
	Hidden
}

private hideSleepActions() {
	boolean Hidden = true
	for ( def i = 0; i < Sleep.size(); i++) {
		if (settings["SleepNotSleeping${Sleep[i].id}"] || settings["SleepSleeping${Sleep[i].id}"]) {Hidden = false}
	}
	Hidden
}

private hideSoundActions() {
	boolean Hidden = true
	for ( def i = 0; i < Sound.size(); i++) {
		if (settings["SoundDetected${Sound[i].id}"] || settings["SoundNotDetected${Sound[i].id}"]) {Hidden = false}
	}
	Hidden
}

private hideTouchActions() {
	boolean Hidden = true
	for ( def i = 0; i < Touch.size(); i++) {
		if (settings["Touched${Touch[i].id}"]) {Hidden = false}
	}
	Hidden
}

private hideWaterActions() {
	boolean Hidden = true
	for ( def i = 0; i < Water.size(); i++) {
		if (settings["WaterDry${Water[i].id}"] || settings["WaterWet${Water[i].id}"]) {Hidden = false}
	}
	Hidden
}

private hideCODetectorActions() {
	boolean Hidden = true
	for ( def i = 0; i < CODetector.size(); i++) {
		if (settings["CODetectorClear${CODetector[i].id}"] || settings["CODetectorDetected${CODetector[i].id}"] || settings["CODetectorTested${CODetector[i].id}"]) {Hidden = false}
	}
	Hidden
}

private hideSmokeActions() {
	boolean Hidden = true
	for ( def i = 0; i < Smoke.size(); i++) {
		if (settings["SmokeClear${Smoke[i].id}"] || settings["SmokeDetected${Smoke[i].id}"] || settings["SmokeTested${Smoke[i].id}"]) {Hidden = false}
	}
	Hidden
}

private hideCO2MeasurementActions() {
	boolean Hidden = true
	if (AllCO2MeasurementHighActions) {
			Hidden = false
		} else {
			for ( def i = 0; i < CO2Measurement.size(); i++) {
				if (settings["CO2MeasurementHighActions${CO2Measurement[i].id}"]) {Hidden = false}
			}
	}
	Hidden
}

private hideBeaconActions() {
	boolean Hidden = true
	for ( def i = 0; i < Beacon.size(); i++) {
		if (settings["BeaconNotPresent${Beacon[i].id}"] || settings["BeaconPresent${Beacon[i].id}"]) {Hidden = false}
	}
	if (BeaconAllAway || BeaconAllPresent) {Hidden = false}
	Hidden
}

private hidePresenceActions() {
	boolean Hidden = true
	for ( def i = 0; i < Presence.size(); i++) {
		if (settings["PresenceNotPresent${Presence[i].id}"] || settings["PresencePresent${Presence[i].id}"]) {Hidden = false}
	}
	if (PresenceAllAway || PresenceAllPresent) {Hidden = false}
	Hidden
}

private hidePowerSourceActions() {
	boolean Hidden = true
	for ( def i = 0; i < PowerSource.size(); i++) {
		if (settings["PowerSourceBattery${PowerSource[i].id}"] || settings["PowerSourceDC${PowerSource[i].id}"] || settings["PowerSourceMains${PowerSource[i].id}"] || settings["PowerSourceUnknown${PowerSource[i].id}"]) {Hidden = false}
	}
	Hidden
}

private hidePowerMeterActions() {
	boolean Hidden = true
	if (AllPowerMeterHighActions || AllPowerMeterLowActions) {
			Hidden = false
		} else {
			for ( def i = 0; i < PowerMeter.size(); i++) {
				if (settings["PowerMeterHighActions${PowerMeter[i].id}"] || settings["PowerMeterLowActions${PowerMeter[i].id}"]) {Hidden = false}
			}
	}
	Hidden
}

private hideBatteryActions() {
	boolean Hidden = true
	if (AllBatteryLowActions) {
			Hidden = false
		} else {
			for ( def i = 0; i < Battery.size(); i++) {
				if (settings["BatteryLowActions${Battery[i].id}"]) {Hidden = false}
			}
	}
	Hidden
}

private hideVoltageActions() {
	boolean Hidden = true
	if (AllVoltageHighActions || AllVoltageLowActions) {
			Hidden = false
		} else {
			for ( def i = 0; i < Voltage.size(); i++) {
				if (settings["VoltageHighActions${Voltage[i].id}"] || settings["VoltageLowActions${Voltage[i].id}"]) {Hidden = false}
			}
	}
	Hidden
}

private hideEnergyMeterActions() {
	boolean Hidden = true
	if (AllEnergyMeterHighActions || AllEnergyMeterLowActions) {
			Hidden = false
		} else {
			for ( def i = 0; i < EnergyMeter.size(); i++) {
				if (settings["EnergyMeterHighActions${EnergyMeter[i].id}"] || settings["EnergyMeterLowActions${EnergyMeter[i].id}"]) {Hidden = false}
			}
	}
	Hidden
}

private hideDoorActions() {
	boolean Hidden = true
	for ( def i = 0; i < Door.size(); i++) {
		if (settings["DoorClosed${Door[i].id}"] || settings["DoorOpen${Door[i].id}"] || settings["DoorUnknown${Door[i].id}"]) {Hidden = false}
	}
	Hidden
}

private hideSwitchActions() {
	boolean Hidden = true
	for ( def i = 0; i < Switch.size(); i++) {
		if (settings["SwitchOff${Switch[i].id}"] || settings["SwitchOn${Switch[i].id}"]) {Hidden = false}
	}
	Hidden
}

private hideTemperatureActions() {
	boolean Hidden = true
	if (AllHighActions || AllLowActions) {
			Hidden = false
		} else {
			for ( def i = 0; i < Temperature.size(); i++) {
				if (settings["HighActions${Temperature[i].id}"] || settings["LowActions${Temperature[i].id}"]) {Hidden = false}
			}
	}
	Hidden
}

private hideValveActions() {
	boolean Hidden = true
	for ( def i = 0; i < Valve.size(); i++) {
		if (settings["ValveClosed${Valve[i].id}"] || settings["ValveOpen${Valve[i].id}"]) {Hidden = false}
	}
	Hidden
}

private hideShadeActions() {
	boolean Hidden = true
	for ( def i = 0; i < Shade.size(); i++) {
		if (settings["ShadeClosed${Shade[i].id}"] || settings["ShadeOpen${Shade[i].id}"] || settings["ShadePartiallyOpen${Shade[i].id}"] || settings["ShadeUnknown${Shade[i].id}"]) {Hidden = false}
	}
	Hidden
}

private hideStepActions() {
	boolean Hidden = true
	if (AllStepHighActions || AllStepLowActions || AllStepGoalActions) {
			Hidden = false
		} else {
			for ( def i = 0; i < Step.size(); i++) {
				if (settings["StepHighActions${Step[i].id}"] || settings["StepLowActions${Step[i].id}"] || settings["StepGoalActions${Step[i].id}"]) {Hidden = false}
			}
	}
	Hidden
}

private hideIlluminanceActions() {
	boolean Hidden = true
	if (AllIlluminanceHighActions || AllIlluminanceLowActions) {
			Hidden = false
		} else {
			for ( def i = 0; i < Illuminance.size(); i++) {
				if (settings["IlluminanceHighActions${Illuminance[i].id}"] || settings["IlluminanceLowActions${Illuminance[i].id}"]) {Hidden = false}
			}
	}
	Hidden
}

private hideHumidityActions() {
	boolean Hidden = true
	if (AllHumidityHighActions || AllHumidityLowActions) {
			Hidden = false
		} else {
			for ( def i = 0; i < Humidity.size(); i++) {
				if (settings["HumidityHighActions${Humidity[i].id}"] || settings["HumidityLowActions${Humidity[i].id}"]) {Hidden = false}
			}
	}
	Hidden
}

private hidesoundPressureLevelActions() {
	boolean Hidden = true
	if (AllsoundPressureLevelHighActions || AllsoundPressureLevelLowActions) {
			Hidden = false
		} else {
			for ( def i = 0; i < soundPressureLevel.size(); i++) {
				if (settings["soundPressureLevelHighActions${soundPressureLevel[i].id}"] || settings["soundPressureLevelLowActions${soundPressureLevel[i].id}"]) {Hidden = false}
			}
	}
	Hidden
}

private hideUltravioletIndexActions() {
	boolean Hidden = true
	if (AllUltravioletIndexHighActions || AllUltravioletIndexLowActions) {
			Hidden = false
		} else {
			for ( def i = 0; i < UltravioletIndex.size(); i++) {
				if (settings["UltravioletIndexHighActions${UltravioletIndex[i].id}"] || settings["UltravioletIndexLowActions${UltravioletIndex[i].id}"]) {Hidden = false}
			}
	}
	Hidden
}

private hidephMeasurementActions() {
	boolean Hidden = true
	if (AllphMeasurementHighActions || AllphMeasurementLowActions) {
			Hidden = false
		} else {
			for ( def i = 0; i < phMeasurement.size(); i++) {
				if (settings["phMeasurementHighActions${phMeasurement[i].id}"] || settings["phMeasurementLowActions${phMeasurement[i].id}"]) {Hidden = false}
			}
	}
	Hidden
}

private offset(value) {
	def result = value ? ((value > 0 ? "+" : "") + value + " min") : ""
}

private timeIntervalLabel() {
	def result = ""
	if (startingX == "Sunrise" && endingX == "Sunrise") result = "Sunrise" + offset(startSunriseOffset) + " to Sunrise" + offset(endSunriseOffset)
	else if (startingX == "Sunrise" && endingX == "Sunset") result = "Sunrise" + offset(startSunriseOffset) + " to Sunset" + offset(endSunsetOffset)
	else if (startingX == "Sunset" && endingX == "Sunrise") result = "Sunset" + offset(startSunsetOffset) + " to Sunrise" + offset(endSunriseOffset)
	else if (startingX == "Sunset" && endingX == "Sunset") result = "Sunset" + offset(startSunsetOffset) + " to Sunset" + offset(endSunsetOffset)
	else if (startingX == "Sunrise" && ending) result = "Sunrise" + offset(startSunriseOffset) + " to " + hhmm(ending, "h:mm a z")
	else if (startingX == "Sunset" && ending) result = "Sunset" + offset(startSunsetOffset) + " to " + hhmm(ending, "h:mm a z")
	else if (starting && endingX == "Sunrise") result = hhmm(starting) + " to Sunrise" + offset(endSunriseOffset)
	else if (starting && endingX == "Sunset") result = hhmm(starting) + " to Sunset" + offset(endSunsetOffset)
	else if (starting && ending) result = hhmm(starting) + " to " + hhmm(ending, "h:mm a z")
}

private SensorTriggerLabel() {
	def result = ""
	def list = ""
	if (Acceleration) {list = list += Acceleration}
	if (Contact) {list = list += Contact}
	if (Motion) {list = list += Motion}
	if (Tamper) {list = list += Tamper}
	if (Shock) {list = list += Shock}
	if (Sleep) {list = list += Sleep}
	if (Sound) {list = list += Sound}
	if (Step) {list = list += Step}
	if (Touch) {list = list += Touch}
	result = list
}

private PresenceTriggerLabel() {
	def result = ""
	def list = ""
	if (Beacon) {list = list += Beacon}
	if (Presence) {list = list += Presence}
	result = list
}

private SafetyTriggerLabel() {
	def result = ""
	def list = ""
	if (CO2Measurement) {list = list += CO2Measurement}
	if (CODetector) {list = list += CODetector}
	if (Smoke) {list = list += Smoke}
	if (soundPressureLevel) {list = list += soundPressureLevel}
	if (Water) {list = list += Water}
	result = list
}

private EnergyTriggerLabel() {
	def result = ""
	def list = ""
	if (EnergyMeter) {list = list += EnergyMeter}
	if (PowerMeter) {list = list += PowerMeter}
	if (PowerSource) {list = list += PowerSource}
	if (Voltage) {list = list += Voltage}
	if (Battery) {list = list += Battery}
	result = list
}

private ControllerTriggerLabel() {
	def result = ""
	def list = ""
	if (Switch) {list = list += Switch}
	if (Illuminance) {list = list += Illuminance}
	if (Door) {list = list += Door}
	result = list
}

private OtherTriggerLabel() {
	def result = ""
	def list = ""
	if (Temperature) {list = list += Temperature}
	if (Humidity) {list = list += Humidity}
	if (UltravioletIndex) {list = list += UltravioletIndex}
	if (phMeasurement) {list = list += phMeasurement}
	if (Valve) {list = list += Valve}
	if (Shade) {list = list += Shade}
	result = list
}

private AlarmActionsLabel() {
	def result = ""
	def list = ""
	if (ChangeModes) {list = list += ChangeModes}
	if (Sirens) {list = list += Sirens}
	result = list
}

private NotificationActionsLabel() {
	def result = ""
	def list = ""
	if (PushNotify) {
		for ( def i = 1; i <= PushTextNumber; i++) {
			list = list += "${settings["PushTitle$i"]},"
		}
	}
	if (SMSNotify) {
		for ( def i = 1; i <= SMSTextNumber; i++) {
			list = list += "${settings["SMSTitle$i"]},"
		}
	}
	if (AudioNotify) {list = list += "AudioNotify"}
	result = list
}

private DoorLockActionsLabel() {
	def result = ""
	def list = ""
	if (GDCs) {list = list += GDCs}
	if (Locks) {list = list += Locks}
	if (DoorControls) {list = list += DoorControls}
	result = list
}

private ImageActionsLabel() {
	def result = ""
	def list = ""
	if (Images) {list = list += Images}
	result = list
}

private SwitchOutletRelayActionsLabel() {
	def result = ""
	def list = ""
	if (Bulbs) {list = list += Bulbs}
	if (Dimmers) {list = list += Dimmers}
	if (Switches) {list = list += Switches}
	if (Outlets) {list = list += Outlets}
	if (Relays) {list = list += Relays}
	result = list
}

private ButtonAssignmentsLabel() {
	def result = ""
	def list = ""
	result = list
}

private CurrentIlluminance() {
	if (Illuminance_RequiredLessThan || Illuminance_RequiredGreaterThan) {
			boolean OkToRun = true
			for ( def i = 0; i < Illuminance_Required.size(); i++) {
			int Currentstate = Illuminance_Required[i].currentValue("illuminance") as Integer
				log.debug "Currentstate = $Currentstate | Illuminance_RequiredLessThan = ${Illuminance_RequiredLessThan} | Illuminance_RequiredGreaterThan = ${Illuminance_RequiredGreaterThan}"
				if (Illuminance_RequiredLessThan) {if (Currentstate > Illuminance_RequiredLessThan) {OkToRun = false}}
				if (Illuminance_RequiredGreaterThan) {if (Currentstate < Illuminance_RequiredGreaterThan) {OkToRun = false}}
			}
			log.debug "Illuminance OkToRun = $OkToRun"
			return OkToRun
		} else {
			return true
	}
}

private CurrentTemperature() {
	if (Temperature_RequiredLessThan || Temperature_RequiredGreaterThan) {
			boolean OkToRun = true
			for ( def i = 0; i < Temperature_Required.size(); i++) {
				int Currentstate = Temperature_Required[i].currentValue("temperature") as Integer
				log.debug "Currentstate = $Currentstate | Temperature_RequiredLessThan = $Temperature_RequiredLessThan | Temperature_RequiredGreaterThan = $Temperature_RequiredGreaterThan"
				if (Temperature_RequiredLessThan) {if (Currentstate > Temperature_RequiredLessThan) {OkToRun = false}}
				if (Temperature_RequiredGreaterThan) {if (Currentstate < Temperature_RequiredGreaterThan) {OkToRun = false}}
			}
			log.debug "Temperature OkToRun = $OkToRun"
			return OkToRun
		} else {
			return true
	}
}

private CurrentPowerMeter() {
	if (PowerMeter_RequiredLessThan || PowerMeter_RequiredGreaterThan) {
			boolean OkToRun = true
			for ( def i = 0; i < PowerMeter_Required.size(); i++) {
				int Currentstate = PowerMeter_Required[i].currentValue("power") as Integer
				log.debug "Currentstate = $Currentstate | PowerMeter_RequiredLessThan = $PowerMeter_RequiredLessThan | PowerMeter_RequiredGreaterThan = $PowerMeter_RequiredGreaterThan"
				if (PowerMeter_RequiredLessThan) {if (Currentstate > PowerMeter_RequiredLessThan) {OkToRun = false}}
				if (PowerMeter_RequiredGreaterThan) {if (Currentstate < PowerMeter_RequiredGreaterThan) {OkToRun = false}}
			}
			log.debug "PowerMeter OkToRun = $OkToRun"
			return OkToRun
		} else {
			return true
	}
}

private CurrentVoltage() {
	if (Voltage_RequiredLessThan || Voltage_RequiredGreaterThan) {
			boolean OkToRun = true
			for ( def i = 0; i < Voltage_Required.size(); i++) {
				int Currentstate = Voltage_Required[i].currentValue("voltage") as Integer
				log.debug "Currentstate = $Currentstate | Voltage_RequiredLessThan = $Voltage_RequiredLessThan | Voltage_RequiredGreaterThan = $Voltage_RequiredGreaterThan"
				if (Voltage_RequiredLessThan) {if (Currentstate > Voltage_RequiredLessThan) {OkToRun = false}}
				if (Voltage_RequiredGreaterThan) {if (Currentstate < Voltage_RequiredGreaterThan) {OkToRun = false}}
			}
			log.debug "Voltage OkToRun = $OkToRun"
			return OkToRun
		} else {
			return true
	}
}

private CurrentEnergyMeter() {
	if (EnergyMeter_RequiredLessThan || EnergyMeter_RequiredGreaterThan) {
			boolean OkToRun = true
			for ( def i = 0; i < EnergyMeter_Required.size(); i++) {
				int Currentstate = EnergyMeter_Required[i].currentValue("energy") as Integer
				log.debug "Currentstate = $Currentstate | EnergyMeter_RequiredLessThan = $EnergyMeter_RequiredLessThan | EnergyMeter_RequiredGreaterThan = $EnergyMeter_RequiredGreaterThan"
				if (EnergyMeter_RequiredLessThan) {if (Currentstate > EnergyMeter_RequiredLessThan) {OkToRun = false}}
				if (EnergyMeter_RequiredGreaterThan) {if (Currentstate < EnergyMeter_RequiredGreaterThan) {OkToRun = false}}
			}
			log.debug "EnergyMeter OkToRun = $OkToRun"
			return OkToRun
		} else {
			return true
	}
}

private CurrentCO2Measurement() {
	if (CO2Measurement_RequiredLessThan || CO2Measurement_RequiredGreaterThan) {
			boolean OkToRun = true
			for ( def i = 0; i < CO2Measurement_Required.size(); i++) {
				int Currentstate = CO2Measurement_Required[i].currentValue("carbonDioxide") as Integer
				log.debug "Currentstate = $Currentstate | CO2Measurement_RequiredLessThan = $CO2Measurement_RequiredLessThan | CO2Measurement_RequiredGreaterThan = $CO2Measurement_RequiredGreaterThan"
				if (CO2Measurement_RequiredLessThan) {if (Currentstate > CO2Measurement_RequiredLessThan) {OkToRun = false}}
				if (CO2Measurement_RequiredGreaterThan) {if (Currentstate < CO2Measurement_RequiredGreaterThan) {OkToRun = false}}
			}
			log.debug "CO2Measurement OkToRun = $OkToRun"
			return OkToRun
		} else {
			return true
	}
}

private CurrentHumidity() {
	if (Humidity_RequiredLessThan || Humidity_RequiredGreaterThan) {
			boolean OkToRun = true
			for ( def i = 0; i < Humidity_Required.size(); i++) {
				int Currentstate = Humidity_Required[i].currentValue("humidity") as Integer
				log.debug "Currentstate = $Currentstate | Humidity_RequiredLessThan = $Humidity_RequiredLessThan | Humidity_RequiredGreaterThan = $Humidity_RequiredGreaterThan"
				if (Humidity_RequiredLessThan) {if (Currentstate > Humidity_RequiredLessThan) {OkToRun = false}}
				if (Humidity_RequiredGreaterThan) {if (Currentstate < Humidity_RequiredGreaterThan) {OkToRun = false}}
			}
			log.debug "Humidity OkToRun = $OkToRun"
			return OkToRun
		} else {
			return true
	}
}

private CurrentUltravioletIndex() {
	if (UltravioletIndex_RequiredLessThan || UltravioletIndex_RequiredGreaterThan) {
			boolean OkToRun = true
			for ( def i = 0; i < UltravioletIndex_Required.size(); i++) {
				int Currentstate = UltravioletIndex_Required[i].currentValue("ultravioletIndex") as Integer
				log.debug "Currentstate = $Currentstate | UltravioletIndex_RequiredLessThan = $UltravioletIndex_RequiredLessThan | UltravioletIndex_RequiredGreaterThan = $UltravioletIndex_RequiredGreaterThan"
				if (UltravioletIndex_RequiredLessThan) {if (Currentstate > UltravioletIndex_RequiredLessThan) {OkToRun = false}}
				if (UltravioletIndex_RequiredGreaterThan) {if (Currentstate < UltravioletIndex_RequiredGreaterThan) {OkToRun = false}}
			}
			log.debug "UltravioletIndex OkToRun = $OkToRun"
			return OkToRun
		} else {
			return true
	}
}

private CurrentphMeasurement() {
	if (phMeasurement_RequiredLessThan || phMeasurement_RequiredGreaterThan) {
			boolean OkToRun = true
			for ( def i = 0; i < phMeasurement_Required.size(); i++) {
				int Currentstate = phMeasurement_Required[i].currentValue("pH") as Integer
				log.debug "Currentstate = $Currentstate | phMeasurement_RequiredLessThan = $phMeasurement_RequiredLessThan | phMeasurement_RequiredGreaterThan = $phMeasurement_RequiredGreaterThan"
				if (phMeasurement_RequiredLessThan) {if (Currentstate > phMeasurement_RequiredLessThan) {OkToRun = false}}
				if (phMeasurement_RequiredGreaterThan) {if (Currentstate < phMeasurement_RequiredGreaterThan) {OkToRun = false}}
			}
			log.debug "phMeasurement OkToRun = $OkToRun"
			return OkToRun
		} else {
			return true
	}
}

private CurrentsoundPressureLevel() {
	if (soundPressureLevel_RequiredLessThan || soundPressureLevel_RequiredGreaterThan) {
			boolean OkToRun = true
			for ( def i = 0; i < soundPressureLevel_Required.size(); i++) {
				int Currentstate = soundPressureLevel_Required[i].currentValue("soundPressureLevel") as Integer
				log.debug "Currentstate = $Currentstate | soundPressureLevel_RequiredLessThan = $soundPressureLevel_RequiredLessThan | soundPressureLevel_RequiredGreaterThan = $soundPressureLevel_RequiredGreaterThan"
				if (soundPressureLevel_RequiredLessThan) {if (Currentstate > soundPressureLevel_RequiredLessThan) {OkToRun = false}}
				if (soundPressureLevel_RequiredGreaterThan) {if (Currentstate < soundPressureLevel_RequiredGreaterThan) {OkToRun = false}}
			}
			log.debug "soundPressureLevel OkToRun = $OkToRun"
			return OkToRun
		} else {
			return true
	}
}

private CurrentContact() {
	if (Contact_Required) {
			boolean OkToRun = true
			for ( def i = 0; i < Contact_Required.size(); i++) {
				def Currentstate = Contact_Required[i].currentValue("contact")
				log.debug "Currentstate for ${Contact_Required[i]} = ${Currentstate.toLowerCase()} | Required state = ${Contact_state.toLowerCase()}"
				if (Currentstate.toLowerCase() != Contact_state.toLowerCase()) {OkToRun = false}
			}
			log.debug "Contact OkToRun = $OkToRun"
			return OkToRun
		} else {
			return true
	}
}

private CurrentSwitch() {
	if (Switch_Required) {
			boolean OkToRun = true
			for ( def i = 0; i < Switch_Required.size(); i++) {
				def Currentstate = Switch_Required[i].currentValue("switch")
				log.debug "Currentstate for ${Switch_Required[i]} = ${Currentstate.toLowerCase()} | Required state = ${Switch_state.toLowerCase()}"
				if (Currentstate.toLowerCase() != Switch_state.toLowerCase()) {OkToRun = false}
			}
			log.debug "Switch OkToRun = $OkToRun"
			return OkToRun
		} else {
			return true
	}
}

private CurrentAcceleration() {
	if (Acceleration_Required) {
			boolean OkToRun = true
			for ( def i = 0; i < Acceleration_Required.size(); i++) {
				def Currentstate = Acceleration_Required[i].currentValue("acceleration")
				log.debug "Currentstate for ${Acceleration_Required[i]} = ${Currentstate.toLowerCase()} | Required state = ${Acceleration_state.toLowerCase()}"
				if (Currentstate.toLowerCase() != Acceleration_state.toLowerCase()) {OkToRun = false}
			}
			log.debug "Acceleration OkToRun = $OkToRun"
			return OkToRun
		} else {
			return true
	}
}

private CurrentMotion() {
	if (Motion_Required) {
			boolean OkToRun = true
			for ( def i = 0; i < Motion_Required.size(); i++) {
				def Currentstate = Motion_Required[i].currentValue("motion")
				log.debug "Currentstate for ${Motion_Required[i]} = ${Currentstate.toLowerCase()} | Required state = ${Motion_state.toLowerCase()}"
				if (Currentstate.toLowerCase() != Motion_state.toLowerCase()) {OkToRun = false}
			}
			log.debug "Motion OkToRun = $OkToRun"
			return OkToRun
		} else {
			return true
	}
}

private CurrentTamper() {
	if (Tamper_Required) {
			boolean OkToRun = true
			for ( def i = 0; i < Tamper_Required.size(); i++) {
				def Currentstate = Tamper_Required[i].currentValue("tamper")
				log.debug "Currentstate for ${Tamper_Required[i]} = ${Currentstate.toLowerCase()} | Required state = ${Tamper_state.toLowerCase()}"
				if (Currentstate.toLowerCase() != Tamper_state.toLowerCase()) {OkToRun = false}
			}
			log.debug "Tamper OkToRun = $OkToRun"
			return OkToRun
		} else {
			return true
	}
}

private CurrentShock() {
	if (Shock_Required) {
			boolean OkToRun = true
			for ( def i = 0; i < Shock_Required.size(); i++) {
				def Currentstate = Shock_Required[i].currentValue("shock")
				log.debug "Currentstate for ${Shock_Required[i]} = ${Currentstate.toLowerCase()} | Required state = ${Shock_state.toLowerCase()}"
				if (Currentstate.toLowerCase() != Shock_state.toLowerCase()) {OkToRun = false}
			}
			log.debug "Shock OkToRun = $OkToRun"
			return OkToRun
		} else {
			return true
	}
}

private CurrentSleep() {
	if (Sleep_Required) {
			boolean OkToRun = true
			for ( def i = 0; i < Sleep_Required.size(); i++) {
				def Currentstate = Sleep_Required[i].currentValue("sleeping")
				log.debug "Currentstate for ${Sleep_Required[i]} = ${Currentstate.toLowerCase()} | Required state = ${Sleep_state.toLowerCase()}"
				if (Currentstate.toLowerCase() != Sleep_state.toLowerCase()) {OkToRun = false}
			}
			log.debug "Sleep OkToRun = $OkToRun"
			return OkToRun
		} else {
			return true
	}
}

private CurrentSound() {
	if (Sound_Required) {
			boolean OkToRun = true
			for ( def i = 0; i < Sound_Required.size(); i++) {
				def Currentstate = Sound_Required[i].currentValue("sound")
				log.debug "Currentstate for ${Sound_Required[i]} = ${Currentstate.toLowerCase()} | Required state = ${Sound_state.toLowerCase()}"
				if (Currentstate.toLowerCase() != Sound_state.toLowerCase()) {OkToRun = false}
			}
			log.debug "Sound OkToRun = $OkToRun"
			return OkToRun
		} else {
			return true
	}
}

private CurrentWater() {
	if (Water_Required) {
			boolean OkToRun = true
			for ( def i = 0; i < Water_Required.size(); i++) {
				def Currentstate = Water_Required[i].currentValue("water")
				log.debug "Currentstate for ${Water_Required[i]} = ${Currentstate.toLowerCase()} | Required state = ${Water_state.toLowerCase()}"
				if (Currentstate.toLowerCase() != Water_state.toLowerCase()) {OkToRun = false}
			}
			log.debug "Water OkToRun = $OkToRun"
			return OkToRun
		} else {
			return true
	}
}

private CurrentBeacon() {
	if (Beacon_Required) {
			boolean OkToRun = true
			for ( def i = 0; i < Beacon_Required.size(); i++) {
				def Currentstate = Beacon_Required[i].currentValue("presence")
				log.debug "Currentstate for ${Beacon_Required[i]} = ${Currentstate.toLowerCase()} | Required state = ${Beacon_state.toLowerCase()}"
				if (Currentstate.toLowerCase() != Beacon_state.toLowerCase()) {OkToRun = false}
			}
			log.debug "Beacon OkToRun = $OkToRun"
			return OkToRun
		} else {
			return true
	}
}

private CurrentPresence() {
	if (Presence_Required) {
			boolean OkToRun = true
			for ( def i = 0; i < Presence_Required.size(); i++) {
				def Currentstate = Presence_Required[i].currentValue("presence")
				log.debug "Currentstate for ${Presence_Required[i]} = ${Currentstate.toLowerCase()} | Required state = ${Presence_state.toLowerCase()}"
				if (Currentstate.toLowerCase() != Presence_state.toLowerCase()) {OkToRun = false}
			}
			log.debug "Presence OkToRun = $OkToRun"
			return OkToRun
		} else {
			return true
	}
}

private CurrentCODetector() {
	if (CODetector_Required) {
			boolean OkToRun = true
			for ( def i = 0; i < CODetector_Required.size(); i++) {
				def Currentstate = CODetector_Required[i].currentValue("carbonMonoxide")
				log.debug "Currentstate for ${CODetector_Required[i]} = ${Currentstate.toLowerCase()} | Required state = ${CODetector_state.toLowerCase()}"
				if (Currentstate.toLowerCase() != CODetector_state.toLowerCase()) {OkToRun = false}
			}
			log.debug "CODetector OkToRun = $OkToRun"
			return OkToRun
		} else {
			return true
	}
}

private CurrentSmoke() {
	if (Smoke_Required) {
			boolean OkToRun = true
			for ( def i = 0; i < Smoke_Required.size(); i++) {
				def Currentstate = Smoke_Required[i].currentValue("smoke")
				log.debug "Currentstate for ${Smoke_Required[i]} = ${Currentstate.toLowerCase()} | Required state = ${Smoke_state.toLowerCase()}"
				if (Currentstate.toLowerCase() != Smoke_state.toLowerCase()) {OkToRun = false}
			}
			log.debug "Smoke OkToRun = $OkToRun"
			return OkToRun
		} else {
			return true
	}
}

private CurrentPowerSource() {
	if (PowerSource_Required) {
			boolean OkToRun = true
			for ( def i = 0; i < PowerSource_Required.size(); i++) {
				def Currentstate = PowerSource_Required[i].currentValue("powerSource")
				log.debug "Currentstate for ${PowerSource_Required[i]} = ${Currentstate.toLowerCase()} | Required state = ${PowerSource_state.toLowerCase()}"
				if (Currentstate.toLowerCase() != PowerSource_state.toLowerCase()) {OkToRun = false}
			}
			log.debug "PowerSource OkToRun = $OkToRun"
			return OkToRun
		} else {
			return true
	}
}

private CurrentDoor() {
	if (Door_Required) {
			boolean OkToRun = true
			for ( def i = 0; i < Door_Required.size(); i++) {
				def Currentstate = Door_Required[i].currentValue("door")
				log.debug "Currentstate for ${Door_Required[i]} = ${Currentstate.toLowerCase()} | Required state = ${Door_state.toLowerCase()}"
				if (Currentstate.toLowerCase() != Door_state.toLowerCase()) {OkToRun = false}
			}
			log.debug "Door OkToRun = $OkToRun"
			return OkToRun
		} else {
			return true
	}
}

private CurrentValve() {
	if (Valve_Required) {
			boolean OkToRun = true
			for ( def i = 0; i < Valve_Required.size(); i++) {
				def Currentstate = Valve_Required[i].currentValue("contact")
				log.debug "Currentstate for ${Valve_Required[i]} = ${Currentstate.toLowerCase()} | Required state = ${Valve_state.toLowerCase()}"
				if (Currentstate.toLowerCase() != Valve_state.toLowerCase()) {OkToRun = false}
			}
			log.debug "Valve OkToRun = $OkToRun"
			return OkToRun
		} else {
			return true
	}
}

private CurrentShade() {
	if (Shade_Required) {
			boolean OkToRun = true
			for ( def i = 0; i < Shade_Required.size(); i++) {
				def Currentstate = Shade_Required[i].currentValue("windowShade")
				log.debug "Currentstate for ${Shade_Required[i]} = ${Currentstate.toLowerCase()} | Required state = ${Shade_state.toLowerCase()}"
				if (Currentstate.toLowerCase() != Shade_state.toLowerCase()) {OkToRun = false}
			}
			log.debug "Shade OkToRun = $OkToRun"
			return OkToRun
		} else {
			return true
	}
}

def TurnOffBulbs_Switch() {
	if (Switch) {
		for ( def i = 0; i < Switch.size(); i++) {
			if (settings["SwitchOn${Switch[i].id}"]) {
				for ( def j = 0; j < settings["SwitchOn${Switch[i].id}"].size(); j++) {
					def Selection = settings["SwitchOn${Switch[i].id}"][j].tokenize(":")
					if (settings["TurnOffAfterMinutes${settings["SwitchOn${Switch[i].id}"]}"]) {
						def Now = new Date()
						if (Bulbs) {
							Bulbs.each {
								if ("$it" == "${Selection[1]}" || "${Selection[1]}" == "all") {
									boolean PriorFound = false
									def PriorEvents = it.eventsBetween(new Date(Now.getTime() - 300*60000), new Date(Now.getTime() - settings["TurnOffAfterMinutes${settings["SwitchOn${Switch[i].id}"]}"]*60000 - 18000), [max: 50])
									for ( def k = PriorEvents.size(); k >= 0; k--) {
										if (PriorEvents[k] != null) {
											if (PriorEvents[k].descriptionText != null) {
												if (PriorEvents[k].descriptionText.endsWith('switch is on')) {PriorFound = true}
												if (PriorEvents[k].descriptionText.endsWith('switch is off')) {PriorFound = false}
											}
										}
									}
									log.debug "$it was on = $PriorFound"
									if (!PriorFound) {
											def Events = it.eventsBetween(new Date(Now.getTime() - settings["TurnOffAfterMinutes${settings["SwitchOn${Switch[i].id}"]}"]*60000 - 600), new Date(Now.getTime() - 1*600), [max: 20])
											if (Events.size() == 0) {
													log.debug "Turning $it off"
													it.off()
												} else {
													boolean Found = false
													for ( def k =  Events.size(); k >= 0; k--) {
														if (Events[k] != null) {if (Events[k].descriptionText != null) {if (Events[k].descriptionText.endsWith('switch is on') || Events[k].descriptionText.endsWith('switch is off')) {Found = true}}}
													}
													if (!Found) {
															log.debug "Turning $it off"
															it.off()
														} else {
															log.debug "Not changing $it as it's state was changed during the timer event."
															log.debug "Event details = ${Events.descriptionText}"
													}
											}
										} else {
											log.debug "Not changing $it as it was on before the timer event started."
									}
								}
							}
						}
					}
				}
			}
		}
	}
}

def TurnOffDimmers_Switch() {
	if (Switch) {
		for ( def i = 0; i < Switch.size(); i++) {
			if (settings["SwitchOn${Switch[i].id}"]) {
				for ( def j = 0; j < settings["SwitchOn${Switch[i].id}"].size(); j++) {
					def Selection = settings["SwitchOn${Switch[i].id}"][j].tokenize(":")
					if (settings["TurnOffAfterMinutes${settings["SwitchOn${Switch[i].id}"]}"]) {
						def Now = new Date()
						if (Dimmers) {
							Dimmers.each {
								if ("$it" == "${Selection[1]}" || "${Selection[1]}" == "all") {
									boolean PriorFound = false
									def PriorEvents = it.eventsBetween(new Date(Now.getTime() - 300*60000), new Date(Now.getTime() - settings["TurnOffAfterMinutes${settings["SwitchOn${Switch[i].id}"]}"]*60000 - 18000), [max: 50])
									for ( def k = PriorEvents.size(); k >= 0; k--) {
										if (PriorEvents[k] != null) {
											if (PriorEvents[k].descriptionText != null) {
												if (PriorEvents[k].descriptionText.endsWith('switch is on')) {PriorFound = true}
												if (PriorEvents[k].descriptionText.endsWith('switch is off')) {PriorFound = false}
											}
										}
									}
									log.debug "$it was on = $PriorFound"
									if (!PriorFound) {
											def Events = it.eventsBetween(new Date(Now.getTime() - settings["TurnOffAfterMinutes${settings["SwitchOn${Switch[i].id}"]}"]*60000 - 600), new Date(Now.getTime() - 1*600), [max: 20])
											if (Events.size() == 0) {
													log.debug "Turning $it off"
													it.off()
												} else {
													boolean Found = false
													for ( def k =  Events.size(); k >= 0; k--) {
														if (Events[k] != null) {if (Events[k].descriptionText != null) {if (Events[k].descriptionText.endsWith('switch is on') || Events[k].descriptionText.endsWith('switch is off')) {Found = true}}}
													}
													if (!Found) {
															log.debug "Turning $it off"
															it.off()
														} else {
															log.debug "Not changing $it as it's state was changed during the timer event."
															log.debug "Event details = ${Events.descriptionText}"
													}
											}
										} else {
											log.debug "Not changing $it as it was on before the timer event started."
									}
								}
							}
						}
					}
				}
			}
		}
	}
}

def TurnOffSwitches_Switch() {
	if (Switch) {
		for ( def i = 0; i < Switch.size(); i++) {
			if (settings["SwitchOn${Switch[i].id}"]) {
				for ( def j = 0; j < settings["SwitchOn${Switch[i].id}"].size(); j++) {
					def Selection = settings["SwitchOn${Switch[i].id}"][j].tokenize(":")
					if (settings["TurnOffAfterMinutes${settings["SwitchOn${Switch[i].id}"]}"]) {
						def Now = new Date()
						if (Switches) {
							Switches.each {
								if ("$it" == "${Selection[1]}" || "${Selection[1]}" == "all") {
									boolean PriorFound = false
									def PriorEvents = it.eventsBetween(new Date(Now.getTime() - 300*60000), new Date(Now.getTime() - settings["TurnOffAfterMinutes${settings["SwitchOn${Switch[i].id}"]}"]*60000 - 18000), [max: 50])
									for ( def k = PriorEvents.size(); k >= 0; k--) {
										if (PriorEvents[k] != null) {
											if (PriorEvents[k].descriptionText != null) {
												if (PriorEvents[k].descriptionText.endsWith('switch is on')) {PriorFound = true}
												if (PriorEvents[k].descriptionText.endsWith('switch is off')) {PriorFound = false}
											}
										}
									}
									log.debug "$it was on = $PriorFound"
									if (!PriorFound) {
											def Events = it.eventsBetween(new Date(Now.getTime() - settings["TurnOffAfterMinutes${settings["SwitchOn${Switch[i].id}"]}"]*60000 - 600), new Date(Now.getTime() - 1*600), [max: 20])
											if (Events.size() == 0) {
													log.debug "Turning $it off"
													it.off()
												} else {
													boolean Found = false
													for ( def k =  Events.size(); k >= 0; k--) {
														if (Events[k] != null) {if (Events[k].descriptionText != null) {if (Events[k].descriptionText.endsWith('switch is on') || Events[k].descriptionText.endsWith('switch is off')) {Found = true}}}
													}
													if (!Found) {
															log.debug "Turning $it off"
															it.off()
														} else {
															log.debug "Not changing $it as it's state was changed during the timer event."
															log.debug "Event details = ${Events.descriptionText}"
													}
											}
										} else {
											log.debug "Not changing $it as it was on before the timer event started."
									}
								}
							}
						}
					}
				}
			}
		}
	}
}

def TurnOffBulbs_Motion() {
	if (Motion) {
		if (Bulbs) {
			def Now = new Date()
			Bulbs.each {
				boolean PriorFound = false
				boolean OKToTurnOff = false
				boolean MotionWasActive = false
				int Minutes = 0
				for ( def i = 0; i < Motion.size(); i++) {
					if (settings["MotionActive${Motion[i].id}"]) {
						for ( def j = 0; j < settings["MotionActive${Motion[i].id}"].size(); j++) {
							def Selection = settings["MotionActive${Motion[i].id}"][j].tokenize(":")
							if (settings["TurnOffAfterMinutes${settings["MotionActive${Motion[i].id}"]}"]) {
								if ("$it" == "${Selection[1]}" || "${Selection[1]}" == "all") {
									//if (atomicState.FirstRun) {
											PriorFound = false
											def PriorEvents = it.eventsBetween(new Date(Now.getTime() - 300*60000), new Date(Now.getTime() - settings["TurnOffAfterMinutes${settings["MotionActive${Motion[i].id}"]}"]*60000 - 18000), [max: 50])
											for ( def k = PriorEvents.size(); k >= 0; k--) {
												if (PriorEvents[k] != null) {
													if (PriorEvents[k].descriptionText != null) {
														if (PriorEvents[k].descriptionText.endsWith('switch is on')) {PriorFound = true}
														if (PriorEvents[k].descriptionText.endsWith('switch is off')) {PriorFound = false}
													}
												}
											}
											log.debug "$it was on = $PriorFound"
										//} else {
										//	PriorFound = false
									//}
									if (!PriorFound) {
											def Events = it.eventsBetween(new Date(Now.getTime() - settings["TurnOffAfterMinutes${settings["MotionActive${Motion[i].id}"]}"]*60000 - 600), new Date(Now.getTime() - 1*600), [max: 20])
											if (Motion[i].currentValue("motion") == "active") {
													log.debug "Reseting timer for ${settings["TurnOffAfterMinutes${settings["MotionActive${Motion[i].id}"]}"]} minutes since motion was still detected."
													MotionWasActive = true
													Minutes = settings["TurnOffAfterMinutes${settings["MotionActive${Motion[i].id}"]}"]
												} else {
													if (Events.size() == 0) {
															if (!MotionWasActive) {OKToTurnOff = true}
														} else {
															boolean Found = false
															for ( def k =  Events.size(); k >= 0; k--) {
																if (Events[k] != null) {if (Events[k].descriptionText != null) {if (Events[k].descriptionText.endsWith('switch is on') || Events[k].descriptionText.endsWith('switch is off')) {Found = true}}}
															}
															if (!Found) {
																	if (!MotionWasActive) {OKToTurnOff = true}
																} else {
																	log.debug "Not changing $it as it's state was changed during the timer event."
																	log.debug "Event details = ${Events.descriptionText}"
															}
													}
											}
										} else {
											log.debug "Not changing $it as it was on before the timer event started."
									}
								}
							}
						}
					}
				}
				if (OKToTurnOff) {
					log.debug "Turning $it off"
					it.off()
				}
			}
		}
		if (MotionWasActive) {
			//atomicState.FirstRun = false
			runIn(Minutes * 60, TurnOffBulbs_Motion)
		}
	}
}

def TurnOffDimmers_Motion() {
	if (Motion) {
		if (Dimmers) {
			def Now = new Date()
			Dimmers.each {
				boolean PriorFound = false
				boolean OKToTurnOff = false
				boolean MotionWasActive = false
				int Minutes = 0
				for ( def i = 0; i < Motion.size(); i++) {
					if (settings["MotionActive${Motion[i].id}"]) {
						for ( def j = 0; j < settings["MotionActive${Motion[i].id}"].size(); j++) {
							def Selection = settings["MotionActive${Motion[i].id}"][j].tokenize(":")
							if (settings["TurnOffAfterMinutes${settings["MotionActive${Motion[i].id}"]}"]) {
								if ("$it" == "${Selection[1]}" || "${Selection[1]}" == "all") {
									//if (atomicState.FirstRun) {
											PriorFound = false
											def PriorEvents = it.eventsBetween(new Date(Now.getTime() - 300*60000), new Date(Now.getTime() - settings["TurnOffAfterMinutes${settings["MotionActive${Motion[i].id}"]}"]*60000 - 18000), [max: 50])
											for ( def k = PriorEvents.size(); k >= 0; k--) {
												if (PriorEvents[k] != null) {
													if (PriorEvents[k].descriptionText != null) {
														if (PriorEvents[k].descriptionText.endsWith('switch is on')) {PriorFound = true}
														if (PriorEvents[k].descriptionText.endsWith('switch is off')) {PriorFound = false}
													}
												}
											}
											log.debug "$it was on = $PriorFound"
										//} else {
										//	PriorFound = false
									//}
									if (!PriorFound) {
											def Events = it.eventsBetween(new Date(Now.getTime() - settings["TurnOffAfterMinutes${settings["MotionActive${Motion[i].id}"]}"]*60000 - 600), new Date(Now.getTime() - 1*600), [max: 20])
											if (Motion[i].currentValue("motion") == "active") {
													log.debug "Reseting timer for ${settings["TurnOffAfterMinutes${settings["MotionActive${Motion[i].id}"]}"]} minutes since motion was still detected."
													MotionWasActive = true
													Minutes = settings["TurnOffAfterMinutes${settings["MotionActive${Motion[i].id}"]}"]
												} else {
													if (Events.size() == 0) {
															if (!MotionWasActive) {OKToTurnOff = true}
														} else {
															boolean Found = false
															for ( def k =  Events.size(); k >= 0; k--) {
																if (Events[k] != null) {if (Events[k].descriptionText != null) {if (Events[k].descriptionText.endsWith('switch is on') || Events[k].descriptionText.endsWith('switch is off')) {Found = true}}}
															}
															if (!Found) {
																	if (!MotionWasActive) {OKToTurnOff = true}
																} else {
																	log.debug "Not changing $it as it's state was changed during the timer event."
																	log.debug "Event details = ${Events.descriptionText}"
															}
													}
											}
										} else {
											log.debug "Not changing $it as it was on before the timer event started."
									}
								}
							}
						}
					}
				}
				if (OKToTurnOff) {
					log.debug "Turning $it off"
					it.off()
				}
			}
		}
		if (MotionWasActive) {
			//atomicState.FirstRun = false
			runIn(Minutes * 60, TurnOffDimmers_Motion)
		}
	}
}

def TurnOffSwitches_Motion() {
	if (Motion) {
		boolean MotionWasActive = false
		int Minutes = 0
		if (Switches) {
			def Now = new Date()
			Switches.each {
				boolean PriorFound = false
				boolean OKToTurnOff = false
				for ( def i = 0; i < Motion.size(); i++) {
					if (settings["MotionActive${Motion[i].id}"]) {
						for ( def j = 0; j < settings["MotionActive${Motion[i].id}"].size(); j++) {
							def Selection = settings["MotionActive${Motion[i].id}"][j].tokenize(":")
							if (settings["TurnOffAfterMinutes${settings["MotionActive${Motion[i].id}"]}"]) {
								if ("$it" == "${Selection[1]}" || "${Selection[1]}" == "all") {
									//if (atomicState.FirstRun) {
											PriorFound = false
											def PriorEvents = it.eventsBetween(new Date(Now.getTime() - 300*60000), new Date(Now.getTime() - settings["TurnOffAfterMinutes${settings["MotionActive${Motion[i].id}"]}"]*60000 - 18000), [max: 50])
											for ( def k = PriorEvents.size(); k >= 0; k--) {
												if (PriorEvents[k] != null) {
													if (PriorEvents[k].descriptionText != null) {
														if (PriorEvents[k].descriptionText.endsWith('switch is on')) {PriorFound = true}
														if (PriorEvents[k].descriptionText.endsWith('switch is off')) {PriorFound = false}
													}
												}
											}
											log.debug "$it was on = $PriorFound"
										//} else {
										//	PriorFound = false
									//}
									if (!PriorFound) {
											def Events = it.eventsBetween(new Date(Now.getTime() - settings["TurnOffAfterMinutes${settings["MotionActive${Motion[i].id}"]}"]*60000 - 600), new Date(Now.getTime() - 1*600), [max: 20])
											if (Motion[i].currentValue("motion") == "active") {
													log.debug "Reseting timer for ${settings["TurnOffAfterMinutes${settings["MotionActive${Motion[i].id}"]}"]} minutes since motion was still detected on ${Motion[i]}."
													MotionWasActive = true
													Minutes = settings["TurnOffAfterMinutes${settings["MotionActive${Motion[i].id}"]}"]
												} else {
													if (Events.size() == 0) {
															if (!MotionWasActive) {OKToTurnOff = true}
														} else {
															boolean Found = false
															for ( def k =  Events.size(); k >= 0; k--) {
																if (Events[k] != null) {if (Events[k].descriptionText != null) {if (Events[k].descriptionText.endsWith('switch is on') || Events[k].descriptionText.endsWith('switch is off')) {Found = true}}}
															}
															if (!Found) {
																	if (!MotionWasActive) {OKToTurnOff = true}
																} else {
																	log.debug "Not changing $it as it's state was changed during the timer event."
																	log.debug "Event details = ${Events.descriptionText}"
															}
													}
											}
										} else {
											log.debug "Not changing $it as it was on before the timer event started."
									}
								}
							}
						}
					}
				}
				if (OKToTurnOff) {
					log.debug "Turning $it off"
					it.off()
				}
			}
		}
		if (MotionWasActive) {
			//atomicState.FirstRun = false
			log.debug "Starting timer for $Minutes minutes."
			runIn(Minutes * 60, TurnOffMotion)
		}
	}
}

def TurnOffMotion() {
	if (Motion) {
		boolean MotionWasActive = false
		int Minutes = 0
		if (Switches) {
			def Now = new Date()
			Switches.each {
				boolean PriorFound = false
				boolean OKToTurnOff = false
				for ( def i = 0; i < Motion.size(); i++) {
					if (settings["MotionActive${Motion[i].id}"]) {
						for ( def j = 0; j < settings["MotionActive${Motion[i].id}"].size(); j++) {
							def Selection = settings["MotionActive${Motion[i].id}"][j].tokenize(":")
							if (settings["TurnOffAfterMinutes${settings["MotionActive${Motion[i].id}"]}"]) {
								if ("$it" == "${Selection[1]}" || "${Selection[1]}" == "all") {
									def Events = it.eventsBetween(new Date(Now.getTime() - settings["TurnOffAfterMinutes${settings["MotionActive${Motion[i].id}"]}"]*60000 - 600), new Date(Now.getTime() - 1*600), [max: 20])
									if (Motion[i].currentValue("motion") == "active") {
											log.debug "Reseting timer for ${settings["TurnOffAfterMinutes${settings["MotionActive${Motion[i].id}"]}"]} minutes since motion was still detected on ${Motion[i]}."
											MotionWasActive = true
											Minutes = settings["TurnOffAfterMinutes${settings["MotionActive${Motion[i].id}"]}"]
										} else {
											if (Events.size() == 0) {
													if (!MotionWasActive) {OKToTurnOff = true}
												} else {
													boolean Found = false
													for ( def k =  Events.size(); k >= 0; k--) {
														if (Events[k] != null) {if (Events[k].descriptionText != null) {if (Events[k].descriptionText.endsWith('switch is on') || Events[k].descriptionText.endsWith('switch is off')) {Found = true}}}
													}
													if (!Found) {
															if (!MotionWasActive) {OKToTurnOff = true}
														} else {
															log.debug "Not changing $it as it's state was changed during the timer event."
															log.debug "Event details = ${Events.descriptionText}"
													}
											}
									}
								}
							}
						}
					}
				}
				if (OKToTurnOff) {
					log.debug "Turning $it off"
					it.off()
				}
			}
		}
		if (MotionWasActive) {
			//atomicState.FirstRun = false
			log.debug "Starting timer for $Minutes minutes."
			runIn(Minutes * 60, TurnOffMotion)
		}
	}
}

def TurnOffBulbs_Contact() {
	if (Contact) {
		for ( def i = 0; i < Contact.size(); i++) {
			if (settings["ContactOpen${Contact[i].id}"]) {
				for ( def j = 0; j < settings["ContactOpen${Contact[i].id}"].size(); j++) {
					def Selection = settings["ContactOpen${Contact[i].id}"][j].tokenize(":")
					if (settings["TurnOffAfterMinutes${settings["ContactOpen${Contact[i].id}"]}"]) {
						def Now = new Date()
						if (Bulbs) {
							Bulbs.each {
								if ("$it" == "${Selection[1]}" || "${Selection[1]}" == "all") {
									boolean PriorFound = false
									def PriorEvents = it.eventsBetween(new Date(Now.getTime() - 300*60000), new Date(Now.getTime() - settings["TurnOffAfterMinutes${settings["ContactOpen${Contact[i].id}"]}"]*60000 - 18000), [max: 50])
									for ( def k = PriorEvents.size(); k >= 0; k--) {
										if (PriorEvents[k] != null) {
											if (PriorEvents[k].descriptionText != null) {
												if (PriorEvents[k].descriptionText.endsWith('switch is on')) {PriorFound = true}
												if (PriorEvents[k].descriptionText.endsWith('switch is off')) {PriorFound = false}
											}
										}
									}
									log.debug "$it was on = $PriorFound"
									if (!PriorFound) {
											def Events = it.eventsBetween(new Date(Now.getTime() - settings["TurnOffAfterMinutes${settings["ContactOpen${Contact[i].id}"]}"]*60000 - 600 - 600), new Date(Now.getTime() - 1*600), [max: 20])
											if (Events.size() == 0) {
													log.debug "Turning $it off"
													it.off()
												} else {
													boolean Found = false
													for ( def k =  Events.size(); k >= 0; k--) {
														if (Events[k] != null) {if (Events[k].descriptionText != null) {if (Events[k].descriptionText.endsWith('switch is on') || Events[k].descriptionText.endsWith('switch is off')) {Found = true}}}
													}
													if (!Found) {
															log.debug "Turning $it off"
															it.off()
														} else {
															log.debug "Not changing $it as it's state was changed during the timer event."
															log.debug "Event details = ${Events.descriptionText}"
													}
											}
										} else {
											log.debug "Not changing $it as it was on before the timer event started."
									}
								}
							}
						}
					}
				}
			}
		}
	}
}

def TurnOffDimmers_Contact() {
	if (Contact) {
		for ( def i = 0; i < Contact.size(); i++) {
			if (settings["ContactOpen${Contact[i].id}"]) {
				for ( def j = 0; j < settings["ContactOpen${Contact[i].id}"].size(); j++) {
					def Selection = settings["ContactOpen${Contact[i].id}"][j].tokenize(":")
					if (settings["TurnOffAfterMinutes${settings["ContactOpen${Contact[i].id}"]}"]) {
						def Now = new Date()
						if (Dimmers) {
							Dimmers.each {
								if ("$it" == "${Selection[1]}" || "${Selection[1]}" == "all") {
									boolean PriorFound = false
									def PriorEvents = it.eventsBetween(new Date(Now.getTime() - 300*60000), new Date(Now.getTime() - settings["TurnOffAfterMinutes${settings["ContactOpen${Contact[i].id}"]}"]*60000 - 18000), [max: 50])
									for ( def k = PriorEvents.size(); k >= 0; k--) {
										if (PriorEvents[k] != null) {
											if (PriorEvents[k].descriptionText != null) {
												if (PriorEvents[k].descriptionText.endsWith('switch is on')) {PriorFound = true}
												if (PriorEvents[k].descriptionText.endsWith('switch is off')) {PriorFound = false}
											}
										}
									}
									log.debug "$it was on = $PriorFound"
									if (!PriorFound) {
											def Events = it.eventsBetween(new Date(Now.getTime() - settings["TurnOffAfterMinutes${settings["ContactOpen${Contact[i].id}"]}"]*60000 - 600), new Date(Now.getTime() - 1*600), [max: 20])
											if (Events.size() == 0) {
													log.debug "Turning $it off"
													it.off()
												} else {
													boolean Found = false
													for ( def k =  Events.size(); k >= 0; k--) {
														if (Events[k] != null) {if (Events[k].descriptionText != null) {if (Events[k].descriptionText.endsWith('switch is on') || Events[k].descriptionText.endsWith('switch is off')) {Found = true}}}
													}
													if (!Found) {
															log.debug "Turning $it off"
															it.off()
														} else {
															log.debug "Not changing $it as it's state was changed during the timer event."
															log.debug "Event details = ${Events.descriptionText}"
													}
											}
										} else {
											log.debug "Not changing $it as it was on before the timer event started."
									}
								}
							}
						}
					}
				}
			}
		}
	}
}

def TurnOffSwitches_Contact() {
	if (Contact) {
		for ( def i = 0; i < Contact.size(); i++) {
			if (settings["ContactOpen${Contact[i].id}"]) {
				for ( def j = 0; j < settings["ContactOpen${Contact[i].id}"].size(); j++) {
					def Selection = settings["ContactOpen${Contact[i].id}"][j].tokenize(":")
					if (settings["TurnOffAfterMinutes${settings["ContactOpen${Contact[i].id}"]}"]) {
						def Now = new Date()
						if (Switches) {
							Switches.each {
								if ("$it" == "${Selection[1]}" || "${Selection[1]}" == "all") {
									boolean PriorFound = false
									def PriorEvents = it.eventsBetween(new Date(Now.getTime() - 300*60000), new Date(Now.getTime() - settings["TurnOffAfterMinutes${settings["ContactOpen${Contact[i].id}"]}"]*60000 - 18000), [max: 50])
									for ( def k = PriorEvents.size(); k >= 0; k--) {
										if (PriorEvents[k] != null) {
											if (PriorEvents[k].descriptionText != null) {
												if (PriorEvents[k].descriptionText.endsWith('switch is on')) {PriorFound = true}
												if (PriorEvents[k].descriptionText.endsWith('switch is off')) {PriorFound = false}
											}
										}
									}
									log.debug "$it was on = $PriorFound"
									if (!PriorFound) {
											def Events = it.eventsBetween(new Date(Now.getTime() - settings["TurnOffAfterMinutes${settings["ContactOpen${Contact[i].id}"]}"]*60000 - 600), new Date(Now.getTime() - 1*600), [max: 20])
											if (Events.size() == 0) {
													log.debug "Turning $it off"
													it.off()
												} else {
													boolean Found = false
													for ( def k =  Events.size(); k >= 0; k--) {
														if (Events[k] != null) {if (Events[k].descriptionText != null) {if (Events[k].descriptionText.endsWith('switch is on') || Events[k].descriptionText.endsWith('switch is off')) {Found = true}}}
													}
													if (!Found) {
															log.debug "Turning $it off"
															it.off()
														} else {
															log.debug "Not changing $it as it's state was changed during the timer event."
															log.debug "Event details = ${Events.descriptionText}"
													}
											}
										} else {
											log.debug "Not changing $it as it was on before the timer event started."
									}
								}
							}
						}
					}
				}
			}
		}
	}
}

def TurnOffBulbs_Push() {
	if (Button) {
		for ( def h = 0; h < Button.size(); h++) {
			int Num = 0
			if (settings["NumberofButtons${Button[h].id}"] != null) {Num = settings["NumberofButtons${Button[h].id}"] as Integer}
			if (Button[h].currentValue("numberOfButtons") != null) {Num = Button[h].currentValue("numberOfButtons") as Integer}
			for ( def i = 1; i <= Num; i++) {
				if (settings["${Button[h].id}Button${i}Push"]) {
					for ( def j = 0; j < settings["${Button[h].id}Button${i}Push"].size(); j++) {
						def Selection = settings["${Button[h].id}Button${i}Push"][j].tokenize(":")
						if (settings["TurnOffAfterMinutes${Button[h].id}Button${i}Push"]) {
							def Now = new Date()
							if (Bulbs) {
								Bulbs.each {
									if ("$it" == "${Selection[1]}" || "${Selection[1]}" == "all") {
										boolean PriorFound = false
										def PriorEvents = it.eventsBetween(new Date(Now.getTime() - 300*60000), new Date(Now.getTime() - settings["TurnOffAfterMinutes${Button[h].id}Button${i}Push"]*60000 - 18000), [max: 50])
										for ( def k = PriorEvents.size(); k >= 0; k--) {
											if (PriorEvents[k] != null) {
												if (PriorEvents[k].descriptionText != null) {
													if (PriorEvents[k].descriptionText.endsWith('switch is on')) {PriorFound = true}
													if (PriorEvents[k].descriptionText.endsWith('switch is off')) {PriorFound = false}
												}
											}
										}
										log.debug "$it was on = $PriorFound"
										if (!PriorFound) {
												def Events = it.eventsBetween(new Date(Now.getTime() - settings["TurnOffAfterMinutes${Button[h].id}Button${i}Push"]*60000 - 600), new Date(Now.getTime() - 1*600), [max: 20])
												if (Events.size() == 0) {
														log.debug "Turning $it off"
														it.off()
													} else {
														boolean Found = false
														for ( def k =  Events.size(); k >= 0; k--) {
															if (Events[k] != null) {if (Events[k].descriptionText != null) {if (Events[k].descriptionText.endsWith('switch is on') || Events[k].descriptionText.endsWith('switch is off')) {Found = true}}}
														}
														if (!Found) {
																log.debug "Turning $it off"
																it.off()
															} else {
																log.debug "Not changing $it as it's state was changed during the timer event."
																log.debug "Event details = ${Events.descriptionText}"
														}
												}
											} else {
												log.debug "Not changing $it as it was on before the timer event started."
										}
									}
								}
							}
						}
					}
				}
			}
		}
	}
}

def TurnOffDimmers_Push() {
	if (Button) {
		for ( def h = 0; h < Button.size(); h++) {
			int Num = 0
			if (settings["NumberofButtons${Button[h].id}"] != null) {Num = settings["NumberofButtons${Button[h].id}"] as Integer}
			if (Button[h].currentValue("numberOfButtons") != null) {Num = Button[h].currentValue("numberOfButtons") as Integer}
			for ( def i = 1; i <= Num; i++) {
				if (settings["${Button[h].id}Button${i}Push"]) {
					for ( def j = 0; j < settings["${Button[h].id}Button${i}Push"].size(); j++) {
						def Selection = settings["${Button[h].id}Button${i}Push"][j].tokenize(":")
						if (settings["TurnOffAfterMinutes${Button[h].id}Button${i}Push"]) {
							def Now = new Date()
							if (Dimmers) {
								Dimmers.each {
									if ("$it" == "${Selection[1]}" || "${Selection[1]}" == "all") {
										boolean PriorFound = false
										def PriorEvents = it.eventsBetween(new Date(Now.getTime() - 300*60000), new Date(Now.getTime() - settings["TurnOffAfterMinutes${Button[h].id}Button${i}Push"]*60000 - 18000), [max: 50])
										for ( def k = PriorEvents.size(); k >= 0; k--) {
											if (PriorEvents[k] != null) {
												if (PriorEvents[k].descriptionText != null) {
													if (PriorEvents[k].descriptionText.endsWith('switch is on')) {PriorFound = true}
													if (PriorEvents[k].descriptionText.endsWith('switch is off')) {PriorFound = false}
												}
											}
										}
										log.debug "$it was on = $PriorFound"
										if (!PriorFound) {
												def Events = it.eventsBetween(new Date(Now.getTime() - settings["TurnOffAfterMinutes${Button[h].id}Button${i}Push"]*60000 - 600), new Date(Now.getTime() - 1*600), [max: 20])
												if (Events.size() == 0) {
														log.debug "Turning $it off"
														it.off()
													} else {
														boolean Found = false
														for ( def k =  Events.size(); k >= 0; k--) {
															if (Events[k] != null) {if (Events[k].descriptionText != null) {if (Events[k].descriptionText.endsWith('switch is on') || Events[k].descriptionText.endsWith('switch is off')) {Found = true}}}
														}
														if (!Found) {
																log.debug "Turning $it off"
																it.off()
															} else {
																log.debug "Not changing $it as it's state was changed during the timer event."
																log.debug "Event details = ${Events.descriptionText}"
														}
												}
											} else {
												log.debug "Not changing $it as it was on before the timer event started."
										}
									}
								}
							}
						}
					}
				}
			}
		}
	}
}

def TurnOffSwitches_Push() {
	if (Button) {
		for ( def h = 0; h < Button.size(); h++) {
			int Num = 0
			if (settings["NumberofButtons${Button[h].id}"] != null) {Num = settings["NumberofButtons${Button[h].id}"] as Integer}
			if (Button[h].currentValue("numberOfButtons") != null) {Num = Button[h].currentValue("numberOfButtons") as Integer}
			for ( def i = 1; i <= Num; i++) {
				if (settings["${Button[h].id}Button${i}Push"]) {
					for ( def j = 0; j < settings["${Button[h].id}Button${i}Push"].size(); j++) {
						def Selection = settings["${Button[h].id}Button${i}Push"][j].tokenize(":")
						if (settings["TurnOffAfterMinutes${Button[h].id}Button${i}Push"]) {
							def Now = new Date()
							if (Switches) {
								Switches.each {
									if ("$it" == "${Selection[1]}" || "${Selection[1]}" == "all") {
										boolean PriorFound = false
										def PriorEvents = it.eventsBetween(new Date(Now.getTime() - 300*60000), new Date(Now.getTime() - settings["TurnOffAfterMinutes${Button[h].id}Button${i}Push"]*60000 - 18000), [max: 50])
										for ( def k = PriorEvents.size(); k >= 0; k--) {
											if (PriorEvents[k] != null) {
												if (PriorEvents[k].descriptionText != null) {
													if (PriorEvents[k].descriptionText.endsWith('switch is on')) {PriorFound = true}
													if (PriorEvents[k].descriptionText.endsWith('switch is off')) {PriorFound = false}
												}
											}
										}
										log.debug "$it was on = $PriorFound"
										if (!PriorFound) {
												def Events = it.eventsBetween(new Date(Now.getTime() - settings["TurnOffAfterMinutes${Button[h].id}Button${i}Push"]*60000 - 600), new Date(Now.getTime() - 1*600), [max: 20])
												if (Events.size() == 0) {
														log.debug "Turning $it off"
														it.off()
													} else {
														boolean Found = false
														for ( def k =  Events.size(); k >= 0; k--) {
															if (Events[k] != null) {if (Events[k].descriptionText != null) {if (Events[k].descriptionText.endsWith('switch is on') || Events[k].descriptionText.endsWith('switch is off')) {Found = true}}}
														}
														if (!Found) {
																log.debug "Turning $it off"
																it.off()
															} else {
																log.debug "Not changing $it as it's state was changed during the timer event."
																log.debug "Event details = ${Events.descriptionText}"
														}
												}
											} else {
												log.debug "Not changing $it as it was on before the timer event started."
										}
									}
								}
							}
						}
					}
				}
			}
		}
	}
}

def TurnOffBulbs_Hold() {
	if (Button) {
		for ( def h = 0; h < Button.size(); h++) {
			int Num = 0
			if (settings["NumberofButtons${Button[h].id}"] != null) {Num = settings["NumberofButtons${Button[h].id}"] as Integer}
			if (Button[h].currentValue("numberOfButtons") != null) {Num = Button[h].currentValue("numberOfButtons") as Integer}
			for ( def i = 1; i <= Num; i++) {
				if (settings["${Button[h].id}Button${i}Hold"]) {
					for ( def j = 0; j < settings["${Button[h].id}Button${i}Hold"].size(); j++) {
						def Selection = settings["${Button[h].id}Button${i}Hold"][j].tokenize(":")
						if (settings["TurnOffAfterMinutes${Button[h].id}Button${i}Hold"]) {
							def Now = new Date()
							if (Bulbs) {
								Bulbs.each {
									if ("$it" == "${Selection[1]}" || "${Selection[1]}" == "all") {
										boolean PriorFound = false
										def PriorEvents = it.eventsBetween(new Date(Now.getTime() - 300*60000), new Date(Now.getTime() - settings["TurnOffAfterMinutes${Button[h].id}Button${i}Hold"]*60000 - 18000), [max: 50])
										for ( def k = PriorEvents.size(); k >= 0; k--) {
											if (PriorEvents[k] != null) {
												if (PriorEvents[k].descriptionText != null) {
													if (PriorEvents[k].descriptionText.endsWith('switch is on')) {PriorFound = true}
													if (PriorEvents[k].descriptionText.endsWith('switch is off')) {PriorFound = false}
												}
											}
										}
										log.debug "$it was on = $PriorFound"
										if (!PriorFound) {
												def Events = it.eventsBetween(new Date(Now.getTime() - settings["TurnOffAfterMinutes${Button[h].id}Button${i}Hold"]*60000 - 600), new Date(Now.getTime() - 1*600), [max: 20])
												if (Events.size() == 0) {
														log.debug "Turning $it off"
														it.off()
													} else {
														boolean Found = false
														for ( def k =  Events.size(); k >= 0; k--) {
															if (Events[k] != null) {if (Events[k].descriptionText != null) {if (Events[k].descriptionText.endsWith('switch is on') || Events[k].descriptionText.endsWith('switch is off')) {Found = true}}}
														}
														if (!Found) {
																log.debug "Turning $it off"
																it.off()
															} else {
																log.debug "Not changing $it as it's state was changed during the timer event."
																log.debug "Event details = ${Events.descriptionText}"
														}
												}
											} else {
												log.debug "Not changing $it as it was on before the timer event started."
										}
									}
								}
							}
						}
					}
				}
			}
		}
	}
}

def TurnOffDimmers_Hold() {
	if (Button) {
		for ( def h = 0; h < Button.size(); h++) {
			int Num = 0
			if (settings["NumberofButtons${Button[h].id}"] != null) {Num = settings["NumberofButtons${Button[h].id}"] as Integer}
			if (Button[h].currentValue("numberOfButtons") != null) {Num = Button[h].currentValue("numberOfButtons") as Integer}
			for ( def i = 1; i <= Num; i++) {
				if (settings["${Button[h].id}Button${i}Hold"]) {
					for ( def j = 0; j < settings["${Button[h].id}Button${i}Hold"].size(); j++) {
						def Selection = settings["${Button[h].id}Button${i}Hold"][j].tokenize(":")
						if (settings["TurnOffAfterMinutes${Button[h].id}Button${i}Hold"]) {
							def Now = new Date()
							if (Dimmers) {
								Dimmers.each {
									if ("$it" == "${Selection[1]}" || "${Selection[1]}" == "all") {
										boolean PriorFound = false
										def PriorEvents = it.eventsBetween(new Date(Now.getTime() - 300*60000), new Date(Now.getTime() - settings["TurnOffAfterMinutes${Button[h].id}Button${i}Hold"]*60000 - 18000), [max: 50])
										for ( def k = PriorEvents.size(); k >= 0; k--) {
											if (PriorEvents[k] != null) {
												if (PriorEvents[k].descriptionText != null) {
													if (PriorEvents[k].descriptionText.endsWith('switch is on')) {PriorFound = true}
													if (PriorEvents[k].descriptionText.endsWith('switch is off')) {PriorFound = false}
												}
											}
										}
										log.debug "$it was on = $PriorFound"
										if (!PriorFound) {
												def Events = it.eventsBetween(new Date(Now.getTime() - settings["TurnOffAfterMinutes${Button[h].id}Button${i}Hold"]*60000 - 600), new Date(Now.getTime() - 1*600), [max: 20])
												if (Events.size() == 0) {
														log.debug "Turning $it off"
														it.off()
													} else {
														boolean Found = false
														for ( def k =  Events.size(); k >= 0; k--) {
															if (Events[k] != null) {if (Events[k].descriptionText != null) {if (Events[k].descriptionText.endsWith('switch is on') || Events[k].descriptionText.endsWith('switch is off')) {Found = true}}}
														}
														if (!Found) {
																log.debug "Turning $it off"
																it.off()
															} else {
																log.debug "Not changing $it as it's state was changed during the timer event."
																log.debug "Event details = ${Events.descriptionText}"
														}
												}
											} else {
												log.debug "Not changing $it as it was on before the timer event started."
										}
									}
								}
							}
						}
					}
				}
			}
		}
	}
}

def TurnOffSwitches_Hold() {
	if (Button) {
		for ( def h = 0; h < Button.size(); h++) {
			int Num = 0
			if (settings["NumberofButtons${Button[h].id}"] != null) {Num = settings["NumberofButtons${Button[h].id}"] as Integer}
			if (Button[h].currentValue("numberOfButtons") != null) {Num = Button[h].currentValue("numberOfButtons") as Integer}
			for ( def i = 1; i <= Num; i++) {
				if (settings["${Button[h].id}Button${i}Hold"]) {
					for ( def j = 0; j < settings["${Button[h].id}Button${i}Hold"].size(); j++) {
						def Selection = settings["${Button[h].id}Button${i}Hold"][j].tokenize(":")
						if (settings["TurnOffAfterMinutes${Button[h].id}Button${i}Hold"]) {
							def Now = new Date()
							if (Switches) {
								Switches.each {
									if ("$it" == "${Selection[1]}" || "${Selection[1]}" == "all") {
										boolean PriorFound = false
										def PriorEvents = it.eventsBetween(new Date(Now.getTime() - 300*60000), new Date(Now.getTime() - settings["TurnOffAfterMinutes${Button[h].id}Button${i}Hold"]*60000 - 18000), [max: 50])
										for ( def k = PriorEvents.size(); k >= 0; k--) {
											if (PriorEvents[k] != null) {
												if (PriorEvents[k].descriptionText != null) {
													if (PriorEvents[k].descriptionText.endsWith('switch is on')) {PriorFound = true}
													if (PriorEvents[k].descriptionText.endsWith('switch is off')) {PriorFound = false}
												}
											}
										}
										log.debug "$it was on = $PriorFound"
										if (!PriorFound) {
												def Events = it.eventsBetween(new Date(Now.getTime() - settings["TurnOffAfterMinutes${Button[h].id}Button${i}Hold"]*60000 - 600), new Date(Now.getTime() - 1*600), [max: 20])
												if (Events.size() == 0) {
														log.debug "Turning $it off"
														it.off()
													} else {
														boolean Found = false
														for ( def k =  Events.size(); k >= 0; k--) {
															if (Events[k] != null) {if (Events[k].descriptionText != null) {if (Events[k].descriptionText.endsWith('switch is on') || Events[k].descriptionText.endsWith('switch is off')) {Found = true}}}
														}
														if (!Found) {
																log.debug "Turning $it off"
																it.off()
															} else {
																log.debug "Not changing $it as it's state was changed during the timer event."
																log.debug "Event details = ${Events.descriptionText}"
														}
												}
											} else {
												log.debug "Not changing $it as it was on before the timer event started."
										}
									}
								}
							}
						}
					}
				}
			}
		}
	}
}
