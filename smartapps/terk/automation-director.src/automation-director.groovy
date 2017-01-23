/**
 *  Copyright 2016  by Terk
 *
 *	Automation Director version 1.4
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
	if (PowerMeter) {subscribe(PowerMeter, "powerMeter", DoPowerMeter)}
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
		section(title: "Switch/Door conroller Triggers...", hidden: hideSwitchDoorTriggers(), hideable: true) {
			def ControllerLabel = ControllerTriggerLabel()
			href "ControllerLabels", title: "Switch/Door triggers", description: ControllerLabel ?: "Tap to set", state: ControllerLabel ? "complete" : null
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
			input "Illuminance_Required", "capability.illuminanceMeasurement", title: "Illuminance sensor above or below threshold", required: false, submitOnChange: true, multiple: false
			if (Illuminance_Required) {
				input "Illuminance_RequiredGreaterThan", "number", title: "Only when lux is >=", required: false, range: "1..*"
				input "Illuminance_RequiredLessThan", "number", title: "Only when lux is <=", required: false, range: "1..*"
				paragraph "Current lux ${Illuminance_Required.currentValue("illuminance")} would meet the requirements : ${CurrentIlluminance()}"
			}
			input "Contact_Required", "capability.contactSensor", title: "Only when a contact is in a certain state", required: false, submitOnChange: true, multiple: false
			if (Contact_Required) {
				input "Contact_state", "enum", title: "Which state?", required: true, options: ["Open", "Closed"]
			}
			input "Switch_Required", "capability.switch", title: "Only when a switch is in a certain state", required: false, submitOnChange: true, multiple: false
			if (Switch_Required) {
				input "Switch_state", "enum", title: "Which state?", required: true, options: ["On", "Off"]
			}
			input "Acceleration_Required", "capability.accelerationSensor", title: "Only when an acceleration sensor is in a certain state", required: false, submitOnChange: true, multiple: false
			if (Acceleration_Required) {
				input "Acceleration_state", "enum", title: "Which state?", required: true, options: ["Active", "Inactive"]
			}
			input "Motion_Required", "capability.motionSensor", title: "Only when a motion sensor is in a certain state", required: false, submitOnChange: true, multiple: false
			if (Motion_Required) {
				input "Motion_state", "enum", title: "Which state?", required: true, options: ["Active", "Inactive"]
			}
			input "Tamper_Required", "capability.tamperAlert", title: "Only when a tamper sensor is in a certain state", required: false, submitOnChange: true, multiple: false
			if (Tamper_Required) {
				input "Tamper_state", "enum", title: "Which state?", required: true, options: ["Clear", "Detected"]
			}
			input "Shock_Required", "capability.shockSensor", title: "Only when a shock sensor is in a certain state", required: false, submitOnChange: true, multiple: false
			if (Shock_Required) {
				input "Shock_state", "enum", title: "Which state?", required: true, options: ["Clear", "Detected"]
			}
			input "Sleep_Required", "capability.sleepSensor", title: "Only when a sleep sensor is in a certain state", required: false, submitOnChange: true, multiple: false
			if (Sleep_Required) {
				input "Sleep_state", "enum", title: "Which state?", required: true, options: ["Not sleeping", "Sleeping"]
			}
			input "Sound_Required", "capability.soundSensor", title: "Only when a sound sensor is in a certain state", required: false, submitOnChange: true, multiple: false
			if (Sound_Required) {
				input "Sound_state", "enum", title: "Which state?", required: true, options: ["Detected", "Not detected"]
			}
			input "Water_Required", "capability.waterSensor", title: "Only when a water sensor is in a certain state", required: false, submitOnChange: true, multiple: false
			if (Water_Required) {
				input "Water_state", "enum", title: "Which state?", required: true, options: ["Dry", "Wet"]
			}
			input "Beacon_Required", "capability.beacon", title: "Only when a beacon sensor is in a certain state", required: false, submitOnChange: true, multiple: false
			if (Beacon_Required) {
				input "Beacon_state", "enum", title: "Which state?", required: true, options: ["Not present", "Present"]
			}
			input "Presence_Required", "capability.presenceSensor", title: "Only when a presence sensor is in a certain state", required: false, submitOnChange: true, multiple: false
			if (Presence_Required) {
				input "Presence_state", "enum", title: "Which state?", required: true, options: ["Not present", "Present"]
			}
			input "CODetector_Required", "capability.carbonMonoxideDetector", title: "Only when a CODetector sensor is in a certain state", required: false, submitOnChange: true, multiple: false
			if (CODetector_Required) {
				input "CODetector_state", "enum", title: "Which state?", required: true, options: ["Clear", "Detected", "Tested"]
			}
			input "Smoke_Required", "capability.smokeDetector", title: "Only when a smoke sensor is in a certain state", required: false, submitOnChange: true, multiple: false
			if (Smoke_Required) {
				input "Smoke_state", "enum", title: "Which state?", required: true, options: ["Clear", "Detected", "Tested"]
			}
			input "PowerSource_Required", "capability.powerSource", title: "Only when a PowerSource sensor is in a certain state", required: false, submitOnChange: true, multiple: false
			if (PowerSource_Required) {
				input "PowerSource_state", "enum", title: "Which state?", required: true, options: ["Battery", "DC", "Mains", "Unknown"]
			}
			input "Door_Required", "capability.doorControl", title: "Only when an door sensor is in a certain state", required: false, submitOnChange: true, multiple: false
			if (Door_Required) {
				input "Door_state", "enum", title: "Which state?", required: true, options: ["Closed", "Open", "Unknown"]
			}
			input "Valve_Required", "capability.valve", title: "Only when an valve sensor is in a certain state", required: false, submitOnChange: true, multiple: false
			if (Valve_Required) {
				input "Valve_state", "enum", title: "Which state?", required: true, options: ["Closed", "Open"]
			}
			input "Shade_Required", "capability.windowShade", title: "Only when an shade sensor is in a certain state", required: false, submitOnChange: true, multiple: false
			if (Shade_Required) {
				input "Shade_state", "enum", title: "Which state?", required: true, options: ["Closed", "Open", "Partially open", "Unknown"]
			}
			input "Temperature_Required", "capability.temperatureMeasurement", title: "Temperature sensor above or below threshold", required: false, submitOnChange: true, multiple: false
			if (Temperature_Required) {
				input "Temperature_RequiredGreaterThan", "number", title: "Only when temp is >=", required: false, range: "1..*"
				input "Temperature_RequiredLessThan", "number", title: "Only when temp is <=", required: false, range: "1..*"
			}
			input "PowerMeter_Required", "capability.powerMeter", title: "PowerMeter sensor above or below threshold", required: false, submitOnChange: true, multiple: false
			if (PowerMeter_Required) {
				input "PowerMeter_RequiredGreaterThan", "number", title: "Only when PowerMeter is >=", required: false, range: "1..*"
				input "PowerMeter_RequiredLessThan", "number", title: "Only when PowerMeter is <=", required: false, range: "1..*"
			}
			input "Voltage_Required", "capability.voltageMeasurement", title: "Voltage sensor above or below threshold", required: false, submitOnChange: true, multiple: false
			if (Voltage_Required) {
				input "Voltage_RequiredGreaterThan", "number", title: "Only when Voltage is >=", required: false, range: "1..*"
				input "Voltage_RequiredLessThan", "number", title: "Only when Voltage is <=", required: false, range: "1..*"
			}
			input "EnergyMeter_Required", "capability.energyMeter", title: "EnergyMeter sensor above or below threshold", required: false, submitOnChange: true, multiple: false
			if (EnergyMeter_Required) {
				input "EnergyMeter_RequiredGreaterThan", "number", title: "Only when EnergyMeter is >=", required: false, range: "1..*"
				input "EnergyMeter_RequiredLessThan", "number", title: "Only when EnergyMeter is <=", required: false, range: "1..*"
			}
			input "CO2Measurement_Required", "capability.carbonDioxideMeasurement", title: "CO2Measurement sensor above or below threshold", required: false, submitOnChange: true, multiple: false
			if (CO2Measurement_Required) {
				input "CO2Measurement_RequiredGreaterThan", "number", title: "Only when CO2Measurement is >=", required: false, range: "1..*"
				input "CO2Measurement_RequiredLessThan", "number", title: "Only when CO2Measurement is <=", required: false, range: "1..*"
			}
			input "Humidity_Required", "capability.relativeHumidityMeasurement", title: "Humidity sensor above or below threshold", required: false, submitOnChange: true, multiple: false
			if (Humidity_Required) {
				input "Humidity_RequiredGreaterThan", "number", title: "Only when Humidity is >=", required: false, range: "1..*"
				input "Humidity_RequiredLessThan", "number", title: "Only when Humidity is <=", required: false, range: "1..*"
			}
			input "UltravioletIndex_Required", "capability.ultravioletIndex", title: "UltravioletIndex sensor above or below threshold", required: false, submitOnChange: true, multiple: false
			if (UltravioletIndex_Required) {
				input "UltravioletIndex_RequiredGreaterThan", "number", title: "Only when UltravioletIndex is >=", required: false, range: "1..*"
				input "UltravioletIndex_RequiredLessThan", "number", title: "Only when UltravioletIndex is <=", required: false, range: "1..*"
			}
			input "phMeasurement_Required", "capability.phMeasurement", title: "phMeasurement sensor above or below threshold", required: false, submitOnChange: true, multiple: false
			if (phMeasurement_Required) {
				input "phMeasurement_RequiredGreaterThan", "number", title: "Only when phMeasurement is >=", required: false, range: "1..*"
				input "phMeasurement_RequiredLessThan", "number", title: "Only when phMeasurement is <=", required: false, range: "1..*"
			}
			input "soundPressureLevel_Required", "capability.soundPressureLevel", title: "soundPressureLevel sensor above or below threshold", required: false, submitOnChange: true, multiple: false
			if (soundPressureLevel_Required) {
				input "soundPressureLevel_RequiredGreaterThan", "number", title: "Only when soundPressureLevel is >=", required: false, range: "1..*"
				input "soundPressureLevel_RequiredLessThan", "number", title: "Only when soundPressureLevel is <=", required: false, range: "1..*"
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
				ActionstoTake = ActionstoTake += "Sirens:$it:siren"
				ActionstoTake = ActionstoTake += "Sirens:$it:strobe"
				ActionstoTake = ActionstoTake += "Sirens:$it:both"
				ActionstoTake = ActionstoTake += "Sirens:$it:off"
			}
			ActionstoTake = ActionstoTake += "Sirens:all:siren"
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
			section(title: "Configure Buttons...", hidden: true, hideable: true) {
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
					if (settings["NumberofButtons${Button[h].id}"] != null) {
						def Num = settings["NumberofButtons${Button[h].id}"] as Integer
						for ( def i = 1; i <= Num; i++) {
							input "${Button[h].id}Button${i}Push", "enum", title: "${Button[h]} button $i Push", options: ActionstoTake, required: false, multiple: true
							if (settings["Holdable${Button[h].id}"] || HoldSet) {input "${Button[h].id}Button${i}Hold", "enum", title: "${Button[h]} button $i Long Push", options: ActionstoTake, required: false, multiple: true}
						}
					}
					if (Button[h].currentValue("numberOfButtons") != null) {
						def Num = Button[h].currentValue("numberOfButtons") as Integer
						for ( def i = 1; i <= Num; i++) {
							input "${Button[h].id}Button${i}Push", "enum", title: "${Button[h]} button $i Push", options: ActionstoTake, required: false, multiple: true
							if (settings["Holdable${Button[h].id}"] || HoldSet) {input "${Button[h].id}Button${i}Hold", "enum", title: "${Button[h]} button $i Long Push", options: ActionstoTake, required: false, multiple: true}
						}
					}
				}
			}
		}
		if (Acceleration) {
			section(title: "Configure Acceleration...", hidden: true, hideable: true) {
				for ( def i = 0; i < Acceleration.size(); i++) {
					input "AccelerationActive${Acceleration[i].id}", "enum", title: "${Acceleration[i]} acceleration Active", options: ActionstoTake, required: false, multiple: true
					input "AccelerationInactive${Acceleration[i].id}", "enum", title: "${Acceleration[i]} acceleration Inactive", options: ActionstoTake, required: false, multiple: true
				}
			}
		}
		if (Contact) {
			section(title: "Configure Contact...", hidden: true, hideable: true) {
				for ( def i = 0; i < Contact.size(); i++) {
					input "ContactClosed${Contact[i].id}", "enum", title: "${Contact[i]} contact Closed", options: ActionstoTake, required: false, multiple: true
					input "ContactOpen${Contact[i].id}", "enum", title: "${Contact[i]} contact Open", options: ActionstoTake, required: false, multiple: true
				}
			}
		}
		if (Motion) {
			section(title: "Configure Motion...", hidden: true, hideable: true) {
				for ( def i = 0; i < Motion.size(); i++) {
					input "MotionActive${Motion[i].id}", "enum", title: "${Motion[i]} motion Active", options: ActionstoTake, required: false, multiple: true
					input "MotionInactive${Motion[i].id}", "enum", title: "${Motion[i]} motion Inactive", options: ActionstoTake, required: false, multiple: true
				}
			}
		}
		if (Tamper) {
			section(title: "Configure Tamper...", hidden: true, hideable: true) {
				for ( def i = 0; i < Tamper.size(); i++) {
					input "TamperClear${Tamper[i].id}", "enum", title: "${Tamper[i]} tamper Clear", options: ActionstoTake, required: false, multiple: true
					input "TamperDetected${Tamper[i].id}", "enum", title: "${Tamper[i]} tamper Detected", options: ActionstoTake, required: false, multiple: true
				}
			}
		}
		if (Shock) {
			section(title: "Configure Shock...", hidden: true, hideable: true) {
				for ( def i = 0; i < Shock.size(); i++) {
					input "ShockClear${Shock[i].id}", "enum", title: "${Shock[i]} shock Clear", options: ActionstoTake, required: false, multiple: true
					input "ShockDetected${Shock[i].id}", "enum", title: "${Shock[i]} shock Detected", options: ActionstoTake, required: false, multiple: true
				}
		   }
		}
		if (Sleep) {
			section(title: "Configure Sleep...", hidden: true, hideable: true) {
				for ( def i = 0; i < Sleep.size(); i++) {
					input "SleepNotSleeping${Sleep[i].id}", "enum", title: "${Sleep[i]} Not Sleeping", options: ActionstoTake, required: false, multiple: true
					input "SleepSleeping${Sleep[i].id}", "enum", title: "${Sleep[i]} Sleeping", options: ActionstoTake, required: false, multiple: true
				}
			}
		}
		if (Sound) {
			section(title: "Configure Sound...", hidden: true, hideable: true) {
				for ( def i = 0; i < Sound.size(); i++) {
					input "SoundDetected${Sound[i].id}", "enum", title: "${Sound[i]} sound Detected", options: ActionstoTake, required: false, multiple: true
					input "SoundNotDetected${Sound[i].id}", "enum", title: "${Sound[i]} sound Not Detected", options: ActionstoTake, required: false, multiple: true
				}
			}
		}
		if (Touch) {
		section(title: "Configure Touch...", hidden: true, hideable: true) {
				for ( def i = 0; i < Touch.size(); i++) {
					input "Touched${Touch[i].id}", "enum", title: "${Touch[i]} Touched", options: ActionstoTake, required: false, multiple: true
				}
			}
		}
		if (Water) {
			section(title: "Configure Water...", hidden: true, hideable: true) {
				for ( def i = 0; i < Water.size(); i++) {
					input "WaterDry${Water[i].id}", "enum", title: "${Water[i]} Dry", options: ActionstoTake, required: false, multiple: true
					input "WaterWet${Water[i].id}", "enum", title: "${Water[i]} Wet", options: ActionstoTake, required: false, multiple: true
				}
			}
		}
		if (CODetector) {
			section(title: "Configure CODetector...", hidden: true, hideable: true) {
				for ( def i = 0; i < CODetector.size(); i++) {
					input "CODetectorClear${CODetector[i].id}", "enum", title: "${CODetector[i]} CO Clear", options: ActionstoTake, required: false, multiple: true
					input "CODetectorDetected${CODetector[i].id}", "enum", title: "${CODetector[i]} CO Detected", options: ActionstoTake, required: false, multiple: true
					input "CODetectorTested${CODetector[i].id}", "enum", title: "${CODetector[i]} CO Tested", options: ActionstoTake, required: false, multiple: true
				}
			}
		}
		if (Smoke) {
			section(title: "Configure Smoke...", hidden: true, hideable: true) {
				for ( def i = 0; i < Smoke.size(); i++) {
					input "SmokeClear${Smoke[i].id}", "enum", title: "${Smoke[i]} smoke Clear", options: ActionstoTake, required: false, multiple: true
					input "SmokeDetected${Smoke[i].id}", "enum", title: "${Smoke[i]} smoke Detected", options: ActionstoTake, required: false, multiple: true
					input "SmokeTested${Smoke[i].id}", "enum", title: "${Smoke[i]} smoke Tested", options: ActionstoTake, required: false, multiple: true
				}
			}
		}
		if (CO2Measurement) {
			section(title: "Configure CO2Measurement...", hidden: true, hideable: true) {
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
			section(title: "Configure Beacon...", hidden: true, hideable: true) {
 				for ( def i = 0; i < Beacon.size(); i++) {
					input "BeaconNotPresent${Beacon[i].id}", "enum", title: "${Beacon[i]} Not Present", options: ActionstoTake, required: false, multiple: true
					input "BeaconPresent${Beacon[i].id}", "enum", title: "${Beacon[i]} Present", options: ActionstoTake, required: false, multiple: true
				}
					input "BeaconAllAway", "enum", title: "All Beacons Away", options: ActionstoTake, required: false, multiple: true
//					input "BeaconAllPresent", "enum", title: "All Beacons Present", options: ActionstoTake, required: false, multiple: true
			}
		}
		if (Presence) {
			section(title: "Configure Presence...", hidden: true, hideable: true) {
				for ( def i = 0; i < Presence.size(); i++) {
					input "PresenceNotPresent${Presence[i].id}", "enum", title: "${Presence[i]} Not Present", options: ActionstoTake, required: false, multiple: true
					input "PresencePresent${Presence[i].id}", "enum", title: "${Presence[i]} Present", options: ActionstoTake, required: false, multiple: true
				}
					input "PresenceAllAway", "enum", title: "All Presence devices Away", options: ActionstoTake, required: false, multiple: true
//					input "PresenceAllPresent", "enum", title: "All Presence devices Present", options: ActionstoTake, required: false, multiple: true
			}
		}
		if (PowerSource) {
			section(title: "Configure PowerSource...", hidden: true, hideable: true) {
				for ( def i = 0; i < PowerSource.size(); i++) {
					input "PowerSourceBattery${PowerSource[i].id}", "enum", title: "${PowerSource[i]} running on Battery", options: ActionstoTake, required: false, multiple: true
					input "PowerSourceDC${PowerSource[i].id}", "enum", title: "${PowerSource[i]} running on DC", options: ActionstoTake, required: false, multiple: true
					input "PowerSourceMains${PowerSource[i].id}", "enum", title: "${PowerSource[i]} running on Mains", options: ActionstoTake, required: false, multiple: true
					input "PowerSourceUnknown${PowerSource[i].id}", "enum", title: "${PowerSource[i]} running on Unknown", options: ActionstoTake, required: false, multiple: true
				}
			}
		}
		if (PowerMeter) {
			section(title: "Configure PowerMeter...", hidden: true, hideable: true) {
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
			section(title: "Configure Battery...", hidden: true, hideable: true) {
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
			section(title: "Configure Voltage...", hidden: true, hideable: true) {
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
			section(title: "Configure EnergyMeter...", hidden: true, hideable: true) {
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
			section(title: "Configure Doors...", hidden: true, hideable: true) {
				for ( def i = 0; i < Door.size(); i++) {
					input "DoorClosed${Door[i].id}", "enum", title: "${Door[i]} Closed", options: ActionstoTake, required: false, multiple: true
					input "DoorOpen${Door[i].id}", "enum", title: "${Door[i]} Open", options: ActionstoTake, required: false, multiple: true
					input "DoorUnknown${Door[i].id}", "enum", title: "${Door[i]} Unknown", options: ActionstoTake, required: false, multiple: true
				}
			}
		}
		if (Switch) {
			section(title: "Configure Switches...", hidden: true, hideable: true) {
				for ( def i = 0; i < Switch.size(); i++) {
					input "SwitchOff${Switch[i].id}", "enum", title: "${Switch[i]} Off", options: ActionstoTake, required: false, multiple: true
					input "SwitchOn${Switch[i].id}", "enum", title: "${Switch[i]} On", options: ActionstoTake, required: false, multiple: true
				}
			}
		}
		if (Temperature) {
			section(title: "Configure Temperature...", hidden: true, hideable: true) {
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
			section(title: "Configure Valve...", hidden: true, hideable: true) {
 				for ( def i = 0; i < Valve.size(); i++) {
					input "ValveClosed${Valve[i].id}", "enum", title: "${Valve[i]} Closed", options: ActionstoTake, required: false, multiple: true
					input "ValveOpen${Valve[i].id}", "enum", title: "${Valve[i]} Open", options: ActionstoTake, required: false, multiple: true
				}
			}
		}
		if (Shade) {
			section(title: "Configure Shade...", hidden: true, hideable: true) {
				for ( def i = 0; i < Shade.size(); i++) {
					input "ShadeClosed${Shade[i].id}", "enum", title: "${Shade[i]} Closed", options: ActionstoTake, required: false, multiple: true
					input "ShadeOpen${Shade[i].id}", "enum", title: "${Shade[i]} Open", options: ActionstoTake, required: false, multiple: true
					input "ShadePartiallyOpen${Shade[i].id}", "enum", title: "${Shade[i]} Partially Open", options: ActionstoTake, required: false, multiple: true
					input "ShadeUnknown${Shade[i].id}", "enum", title: "${Shade[i]} Unknown", options: ActionstoTake, required: false, multiple: true
				}
			}
		}
		if (Step) {
			section(title: "Configure Step...", hidden: true, hideable: true) {
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
			section(title: "Configure Illuminance...", hidden: true, hideable: true) {
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
			section(title: "Configure Humidity...", hidden: true, hideable: true) {
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
			section(title: "Configure Sound Pressure Level...", hidden: true, hideable: true) {
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
			section(title: "Configure UV Index...", hidden: true, hideable: true) {
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
			section(title: "Configure ph Measurement...", hidden: true, hideable: true) {
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
		log.debug "will set default label of $DefaultName"
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
				log.debug "DeviceType = ${SelectionArray[i][0]},Device = ${SelectionArray[i][1]},Action = ${SelectionArray[i][2]}"
				SwitchSelection(SelectionArray[i])
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
			log.debug "Device that triggered the event: ${Changed.displayName} Number = $Number | Actions = ${settings["ContactClosed${evt.deviceId}"]}"
			for ( def i = 0; i < settings["ContactClosed${evt.deviceId}"].size(); i++) {
				SelectionArray[i] = settings["ContactClosed${evt.deviceId}"][i].tokenize(":")
				log.debug "DeviceType = ${SelectionArray[i][0]},Device = ${SelectionArray[i][1]},Action = ${SelectionArray[i][2]}"
				SwitchSelection(SelectionArray[i])
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
			log.debug "Device that triggered the event: ${Changed.displayName} Number = $Number | Actions = ${settings["SwitchOn${evt.deviceId}"]}"
			for ( def i = 0; i < settings["SwitchOn${evt.deviceId}"].size(); i++) {
				SelectionArray[i] = settings["SwitchOn${evt.deviceId}"][i].tokenize(":")
				log.debug "DeviceType = ${SelectionArray[i][0]},Device = ${SelectionArray[i][1]},Action = ${SelectionArray[i][2]}"
				SwitchSelection(SelectionArray[i])
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
			log.debug "Device that triggered the event: ${Changed.displayName} Number = $Number | Actions = ${settings["SwitchOff${evt.deviceId}"]}"
			for ( def i = 0; i < settings["SwitchOff${evt.deviceId}"].size(); i++) {
				SelectionArray[i] = settings["SwitchOff${evt.deviceId}"][i].tokenize(":")
				log.debug "DeviceType = ${SelectionArray[i][0]},Device = ${SelectionArray[i][1]},Action = ${SelectionArray[i][2]}"
				SwitchSelection(SelectionArray[i])
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
			log.debug "Device that triggered the event: ${Changed.displayName} Number = $Number | Actions = ${settings["AccelerationActive${evt.deviceId}"]}"
			for ( def i = 0; i < settings["AccelerationActive${evt.deviceId}"].size(); i++) {
				SelectionArray[i] = settings["AccelerationActive${evt.deviceId}"][i].tokenize(":")
				log.debug "DeviceType = ${SelectionArray[i][0]},Device = ${SelectionArray[i][1]},Action = ${SelectionArray[i][2]}"
				SwitchSelection(SelectionArray[i])
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
			log.debug "Device that triggered the event: ${Changed.displayName} Number = $Number | Actions = ${settings["AccelerationInactive${evt.deviceId}"]}"
			for ( def i = 0; i < settings["AccelerationInactive${evt.deviceId}"].size(); i++) {
				SelectionArray[i] = settings["AccelerationInactive${evt.deviceId}"][i].tokenize(":")
				log.debug "DeviceType = ${SelectionArray[i][0]},Device = ${SelectionArray[i][1]},Action = ${SelectionArray[i][2]}"
				SwitchSelection(SelectionArray[i])
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
			log.debug "Device that triggered the event: ${Changed.displayName} Number = $Number | Actions = ${settings["MotionActive${evt.deviceId}"]}"
			for ( def i = 0; i < settings["MotionActive${evt.deviceId}"].size(); i++) {
				SelectionArray[i] = settings["MotionActive${evt.deviceId}"][i].tokenize(":")
				log.debug "DeviceType = ${SelectionArray[i][0]},Device = ${SelectionArray[i][1]},Action = ${SelectionArray[i][2]}"
				SwitchSelection(SelectionArray[i])
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
			log.debug "Device that triggered the event: ${Changed.displayName} Number = $Number | Actions = ${settings["MotionInactive${evt.deviceId}"]}"
			for ( def i = 0; i < settings["MotionInactive${evt.deviceId}"].size(); i++) {
				SelectionArray[i] = settings["MotionInactive${evt.deviceId}"][i].tokenize(":")
				log.debug "DeviceType = ${SelectionArray[i][0]},Device = ${SelectionArray[i][1]},Action = ${SelectionArray[i][2]}"
				SwitchSelection(SelectionArray[i])
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
			log.debug "Device that triggered the event: ${Changed.displayName} Number = $Number | Actions = ${settings["TamperClear${evt.deviceId}"]}"
			for ( def i = 0; i < settings["TamperClear${evt.deviceId}"].size(); i++) {
				SelectionArray[i] = settings["TamperClear${evt.deviceId}"][i].tokenize(":")
				log.debug "DeviceType = ${SelectionArray[i][0]},Device = ${SelectionArray[i][1]},Action = ${SelectionArray[i][2]}"
				SwitchSelection(SelectionArray[i])
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
			log.debug "Device that triggered the event: ${Changed.displayName} Number = $Number | Actions = ${settings["TamperDetected${evt.deviceId}"]}"
			for ( def i = 0; i < settings["TamperDetected${evt.deviceId}"].size(); i++) {
				SelectionArray[i] = settings["TamperDetected${evt.deviceId}"][i].tokenize(":")
				log.debug "DeviceType = ${SelectionArray[i][0]},Device = ${SelectionArray[i][1]},Action = ${SelectionArray[i][2]}"
				SwitchSelection(SelectionArray[i])
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
			log.debug "Device that triggered the event: ${Changed.displayName} Number = $Number | Actions = ${settings["ShockClear${evt.deviceId}"]}"
			for ( def i = 0; i < settings["ShockClear${evt.deviceId}"].size(); i++) {
				SelectionArray[i] = settings["ShockClear${evt.deviceId}"][i].tokenize(":")
				log.debug "DeviceType = ${SelectionArray[i][0]},Device = ${SelectionArray[i][1]},Action = ${SelectionArray[i][2]}"
				SwitchSelection(SelectionArray[i])
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
			log.debug "Device that triggered the event: ${Changed.displayName} Number = $Number | Actions = ${settings["ShockDetected${evt.deviceId}"]}"
			for ( def i = 0; i < settings["ShockDetected${evt.deviceId}"].size(); i++) {
				SelectionArray[i] = settings["ShockDetected${evt.deviceId}"][i].tokenize(":")
				log.debug "DeviceType = ${SelectionArray[i][0]},Device = ${SelectionArray[i][1]},Action = ${SelectionArray[i][2]}"
				SwitchSelection(SelectionArray[i])
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
			log.debug "Device that triggered the event: ${Changed.displayName} Number = $Number | Actions = ${settings["SleepNotSleeping${evt.deviceId}"]}"
			for ( def i = 0; i < settings["SleepNotSleeping${evt.deviceId}"].size(); i++) {
				SelectionArray[i] = settings["SleepNotSleeping${evt.deviceId}"][i].tokenize(":")
				log.debug "DeviceType = ${SelectionArray[i][0]},Device = ${SelectionArray[i][1]},Action = ${SelectionArray[i][2]}"
				SwitchSelection(SelectionArray[i])
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
			log.debug "Device that triggered the event: ${Changed.displayName} Number = $Number | Actions = ${settings["SleepSleeping${evt.deviceId}"]}"
			for ( def i = 0; i < settings["SleepSleeping${evt.deviceId}"].size(); i++) {
				SelectionArray[i] = settings["SleepSleeping${evt.deviceId}"][i].tokenize(":")
				log.debug "DeviceType = ${SelectionArray[i][0]},Device = ${SelectionArray[i][1]},Action = ${SelectionArray[i][2]}"
				SwitchSelection(SelectionArray[i])
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
			log.debug "Device that triggered the event: ${Changed.displayName} Number = $Number | Actions = ${settings["SoundDetected${evt.deviceId}"]}"
			for ( def i = 0; i < settings["SoundDetected${evt.deviceId}"].size(); i++) {
				SelectionArray[i] = settings["SoundDetected${evt.deviceId}"][i].tokenize(":")
				log.debug "DeviceType = ${SelectionArray[i][0]},Device = ${SelectionArray[i][1]},Action = ${SelectionArray[i][2]}"
				SwitchSelection(SelectionArray[i])
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
			log.debug "Device that triggered the event: ${Changed.displayName} Number = $Number | Actions = ${settings["SoundNotDetected${evt.deviceId}"]}"
			for ( def i = 0; i < settings["SoundNotDetected${evt.deviceId}"].size(); i++) {
				SelectionArray[i] = settings["SoundNotDetected${evt.deviceId}"][i].tokenize(":")
				log.debug "DeviceType = ${SelectionArray[i][0]},Device = ${SelectionArray[i][1]},Action = ${SelectionArray[i][2]}"
				SwitchSelection(SelectionArray[i])
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
			log.debug "Device that triggered the event: ${Changed.displayName} Number = $Number | Actions = ${settings["Touched${evt.deviceId}"]}"
			for ( def i = 0; i < settings["Touched${evt.deviceId}"].size(); i++) {
				SelectionArray[i] = settings["Touched${evt.deviceId}"][i].tokenize(":")
				log.debug "DeviceType = ${SelectionArray[i][0]},Device = ${SelectionArray[i][1]},Action = ${SelectionArray[i][2]}"
				SwitchSelection(SelectionArray[i])
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
			log.debug "Device that triggered the event: ${Changed.displayName} Number = $Number | Actions = ${settings["WaterDry${evt.deviceId}"]}"
			for ( def i = 0; i < settings["WaterDry${evt.deviceId}"].size(); i++) {
				SelectionArray[i] = settings["WaterDry${evt.deviceId}"][i].tokenize(":")
				log.debug "DeviceType = ${SelectionArray[i][0]},Device = ${SelectionArray[i][1]},Action = ${SelectionArray[i][2]}"
				SwitchSelection(SelectionArray[i])
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
			log.debug "Device that triggered the event: ${Changed.displayName} Number = $Number | Actions = ${settings["WaterWet${evt.deviceId}"]}"
			for ( def i = 0; i < settings["WaterWet${evt.deviceId}"].size(); i++) {
				SelectionArray[i] = settings["WaterWet${evt.deviceId}"][i].tokenize(":")
				log.debug "DeviceType = ${SelectionArray[i][0]},Device = ${SelectionArray[i][1]},Action = ${SelectionArray[i][2]}"
				SwitchSelection(SelectionArray[i])
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
			log.debug "Device that triggered the event: ${Changed.displayName} Number = $Number | Actions = ${settings["BeaconNotPresent${evt.deviceId}"]}"
			for ( def i = 0; i < settings["BeaconNotPresent${evt.deviceId}"].size(); i++) {
				SelectionArray[i] = settings["BeaconNotPresent${evt.deviceId}"][i].tokenize(":")
				log.debug "DeviceType = ${SelectionArray[i][0]},Device = ${SelectionArray[i][1]},Action = ${SelectionArray[i][2]}"
				SwitchSelection(SelectionArray[i])
			}
		}
		if (settings["BeaconAllAway"]) {
			boolean AllAway = true
			Beacon.each {
				//log.debug "$it = ${it.currentValue("presence")}"
				if (it.currentValue("presence") == "present") {AllAway = false}
			}
			//log.debug "AllAway = $AllAway"
			if (AllAway) {
				log.debug "Device that triggered the event: ${Changed.displayName} | Number = $Number | Actions = $BeaconAllAway"
				for ( def i = 0; i < BeaconAllAway.size(); i++) {
					SelectionArray[i] = BeaconAllAway[i].tokenize(":")
					log.debug "DeviceType = ${SelectionArray[i][0]},Device = ${SelectionArray[i][1]},Action = ${SelectionArray[i][2]}"
					SwitchSelection(SelectionArray[i])
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
			log.debug "Device that triggered the event: ${Changed.displayName} Number = $Number | Actions = ${settings["BeaconPresent${evt.deviceId}"]}"
			for ( def i = 0; i < settings["BeaconPresent${evt.deviceId}"].size(); i++) {
				SelectionArray[i] = settings["BeaconPresent${evt.deviceId}"][i].tokenize(":")
				log.debug "DeviceType = ${SelectionArray[i][0]},Device = ${SelectionArray[i][1]},Action = ${SelectionArray[i][2]}"
				SwitchSelection(SelectionArray[i])
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
				log.debug "DeviceType = ${SelectionArray[i][0]},Device = ${SelectionArray[i][1]},Action = ${SelectionArray[i][2]}"
				SwitchSelection(SelectionArray[i])
			}
		}
		if (settings["PresenceAllAway"]) {
			boolean AllAway = true
			Presence.each {
				//log.debug "$it = ${it.currentValue("presence")}"
				if (it.currentValue("presence") == "present") {AllAway = false}
			}
			//log.debug "AllAway = $AllAway"
			if (AllAway) {
				log.debug "Device that triggered the event: ${Changed.displayName} | Number = $Number | Actions = $PresenceAllAway"
				for ( def i = 0; i < PresenceAllAway.size(); i++) {
					SelectionArray[i] = PresenceAllAway[i].tokenize(":")
					log.debug "DeviceType = ${SelectionArray[i][0]},Device = ${SelectionArray[i][1]},Action = ${SelectionArray[i][2]}"
					SwitchSelection(SelectionArray[i])
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
			log.debug "Found the device that triggered the event: ${Changed.displayName} Number = $Number | Actions = ${settings["PresencePresent${evt.deviceId}"]}"
			for ( def i = 0; i < settings["PresencePresent${evt.deviceId}"].size(); i++) {
				SelectionArray[i] = settings["PresencePresent${evt.deviceId}"][i].tokenize(":")
				log.debug "DeviceType = ${SelectionArray[i][0]},Device = ${SelectionArray[i][1]},Action = ${SelectionArray[i][2]}"
				SwitchSelection(SelectionArray[i])
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
			log.debug "Device that triggered the event: ${Changed.displayName} Number = $Number | Actions = ${settings["CODetectorClear${evt.deviceId}"]}"
			for ( def i = 0; i < settings["CODetectorClear${evt.deviceId}"].size(); i++) {
				SelectionArray[i] = settings["CODetectorClear${evt.deviceId}"][i].tokenize(":")
				log.debug "DeviceType = ${SelectionArray[i][0]},Device = ${SelectionArray[i][1]},Action = ${SelectionArray[i][2]}"
				SwitchSelection(SelectionArray[i])
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
			log.debug "Device that triggered the event: ${Changed.displayName} Number = $Number | Actions = ${settings["CODetectorDetected${evt.deviceId}"]}"
			for ( def i = 0; i < settings["CODetectorDetected${evt.deviceId}"].size(); i++) {
				SelectionArray[i] = settings["CODetectorDetected${evt.deviceId}"][i].tokenize(":")
				log.debug "DeviceType = ${SelectionArray[i][0]},Device = ${SelectionArray[i][1]},Action = ${SelectionArray[i][2]}"
				SwitchSelection(SelectionArray[i])
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
			log.debug "Device that triggered the event: ${Changed.displayName} Number = $Number | Actions = ${settings["CODetectorTested${evt.deviceId}"]}"
			for ( def i = 0; i < settings["CODetectorTested${evt.deviceId}"].size(); i++) {
				SelectionArray[i] = settings["CODetectorTested${evt.deviceId}"][i].tokenize(":")
				log.debug "DeviceType = ${SelectionArray[i][0]},Device = ${SelectionArray[i][1]},Action = ${SelectionArray[i][2]}"
				SwitchSelection(SelectionArray[i])
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
			log.debug "Device that triggered the event: ${Changed.displayName} Number = $Number | Actions = ${settings["SmokeClear${evt.deviceId}"]}"
			for ( def i = 0; i < settings["SmokeClear${evt.deviceId}"].size(); i++) {
				SelectionArray[i] = settings["SmokeClear${evt.deviceId}"][i].tokenize(":")
				log.debug "DeviceType = ${SelectionArray[i][0]},Device = ${SelectionArray[i][1]},Action = ${SelectionArray[i][2]}"
				SwitchSelection(SelectionArray[i])
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
			log.debug "Device that triggered the event: ${Changed.displayName} Number = $Number | Actions = ${settings["SmokeDetected${evt.deviceId}"]}"
			for ( def i = 0; i < settings["SmokeDetected${evt.deviceId}"].size(); i++) {
				SelectionArray[i] = settings["SmokeDetected${evt.deviceId}"][i].tokenize(":")
				log.debug "DeviceType = ${SelectionArray[i][0]},Device = ${SelectionArray[i][1]},Action = ${SelectionArray[i][2]}"
				SwitchSelection(SelectionArray[i])
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
			log.debug "Device that triggered the event: ${Changed.displayName} Number = $Number | Actions = ${settings["SmokeTested${evt.deviceId}"]}"
			for ( def i = 0; i < settings["SmokeTested${evt.deviceId}"].size(); i++) {
				SelectionArray[i] = settings["SmokeTested${evt.deviceId}"][i].tokenize(":")
				log.debug "DeviceType = ${SelectionArray[i][0]},Device = ${SelectionArray[i][1]},Action = ${SelectionArray[i][2]}"
				SwitchSelection(SelectionArray[i])
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
			log.debug "Device that triggered the event: ${Changed.displayName} Number = $Number | Actions = ${settings["PowerSourceBattery${evt.deviceId}"]}"
			for ( def i = 0; i < settings["PowerSourceBattery${evt.deviceId}"].size(); i++) {
				SelectionArray[i] = settings["PowerSourceBattery${evt.deviceId}"][i].tokenize(":")
				log.debug "DeviceType = ${SelectionArray[i][0]},Device = ${SelectionArray[i][1]},Action = ${SelectionArray[i][2]}"
				SwitchSelection(SelectionArray[i])
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
			log.debug "Device that triggered the event: ${Changed.displayName} Number = $Number | Actions = ${settings["PowerSourceDC${evt.deviceId}"]}"
			for ( def i = 0; i < settings["PowerSourceDC${evt.deviceId}"].size(); i++) {
				SelectionArray[i] = settings["PowerSourceDC${evt.deviceId}"][i].tokenize(":")
				log.debug "DeviceType = ${SelectionArray[i][0]},Device = ${SelectionArray[i][1]},Action = ${SelectionArray[i][2]}"
				SwitchSelection(SelectionArray[i])
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
			log.debug "Device that triggered the event: ${Changed.displayName} Number = $Number | Actions = ${settings["PowerSourceMains${evt.deviceId}"]}"
			for ( def i = 0; i < settings["PowerSourceMains${evt.deviceId}"].size(); i++) {
				SelectionArray[i] = settings["PowerSourceMains${evt.deviceId}"][i].tokenize(":")
				log.debug "DeviceType = ${SelectionArray[i][0]},Device = ${SelectionArray[i][1]},Action = ${SelectionArray[i][2]}"
				SwitchSelection(SelectionArray[i])
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
			log.debug "Device that triggered the event: ${Changed.displayName} Number = $Number | Actions = ${settings["PowerSourceUnknown${evt.deviceId}"]}"
			for ( def i = 0; i < settings["PowerSourceUnknown${evt.deviceId}"].size(); i++) {
				SelectionArray[i] = settings["PowerSourceUnknown${evt.deviceId}"][i].tokenize(":")
				log.debug "DeviceType = ${SelectionArray[i][0]},Device = ${SelectionArray[i][1]},Action = ${SelectionArray[i][2]}"
				SwitchSelection(SelectionArray[i])
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
			log.debug "Device that triggered the event: ${Changed.displayName} Number = $Number | Actions = ${settings["DoorClosed${evt.deviceId}"]}"
			for ( def i = 0; i < settings["DoorClosed${evt.deviceId}"].size(); i++) {
				SelectionArray[i] = settings["DoorClosed${evt.deviceId}"][i].tokenize(":")
				log.debug "DeviceType = ${SelectionArray[i][0]},Device = ${SelectionArray[i][1]},Action = ${SelectionArray[i][2]}"
				SwitchSelection(SelectionArray[i])
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
			log.debug "Device that triggered the event: ${Changed.displayName} Number = $Number | Actions = ${settings["DoorOpen${evt.deviceId}"]}"
			for ( def i = 0; i < settings["DoorOpen${evt.deviceId}"].size(); i++) {
				SelectionArray[i] = settings["DoorOpen${evt.deviceId}"][i].tokenize(":")
				log.debug "DeviceType = ${SelectionArray[i][0]},Device = ${SelectionArray[i][1]},Action = ${SelectionArray[i][2]}"
				SwitchSelection(SelectionArray[i])
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
			log.debug "Device that triggered the event: ${Changed.displayName} Number = $Number | Actions = ${settings["DoorUnknown${evt.deviceId}"]}"
			for ( def i = 0; i < settings["DoorUnknown${evt.deviceId}"].size(); i++) {
				SelectionArray[i] = settings["DoorUnknown${evt.deviceId}"][i].tokenize(":")
				log.debug "DeviceType = ${SelectionArray[i][0]},Device = ${SelectionArray[i][1]},Action = ${SelectionArray[i][2]}"
				SwitchSelection(SelectionArray[i])
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
			log.debug "Device that triggered the event: ${Changed.displayName} Number = $Number | Actions = ${settings["ValveClosed${evt.deviceId}"]}"
			for ( def i = 0; i < settings["ValveClosed${evt.deviceId}"].size(); i++) {
				SelectionArray[i] = settings["ValveClosed${evt.deviceId}"][i].tokenize(":")
				log.debug "DeviceType = ${SelectionArray[i][0]},Device = ${SelectionArray[i][1]},Action = ${SelectionArray[i][2]}"
				SwitchSelection(SelectionArray[i])
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
			log.debug "Device that triggered the event: ${Changed.displayName} Number = $Number | Actions = ${settings["ValveOpen${evt.deviceId}"]}"
			for ( def i = 0; i < settings["ValveOpen${evt.deviceId}"].size(); i++) {
				SelectionArray[i] = settings["ValveOpen${evt.deviceId}"][i].tokenize(":")
				log.debug "DeviceType = ${SelectionArray[i][0]},Device = ${SelectionArray[i][1]},Action = ${SelectionArray[i][2]}"
				SwitchSelection(SelectionArray[i])
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
			log.debug "Device that triggered the event: ${Changed.displayName} Number = $Number | Actions = ${settings["ShadeClosed${evt.deviceId}"]}"
			for ( def i = 0; i < settings["ShadeClosed${evt.deviceId}"].size(); i++) {
				SelectionArray[i] = settings["ShadeClosed${evt.deviceId}"][i].tokenize(":")
				log.debug "DeviceType = ${SelectionArray[i][0]},Device = ${SelectionArray[i][1]},Action = ${SelectionArray[i][2]}"
				SwitchSelection(SelectionArray[i])
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
			log.debug "Device that triggered the event: ${Changed.displayName} Number = $Number | Actions = ${settings["ShadeOpen${evt.deviceId}"]}"
			for ( def i = 0; i < settings["ShadeOpen${evt.deviceId}"].size(); i++) {
				SelectionArray[i] = settings["ShadeOpen${evt.deviceId}"][i].tokenize(":")
				log.debug "DeviceType = ${SelectionArray[i][0]},Device = ${SelectionArray[i][1]},Action = ${SelectionArray[i][2]}"
				SwitchSelection(SelectionArray[i])
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
			log.debug "Device that triggered the event: ${Changed.displayName} Number = $Number | Actions = ${settings["ShadePartiallyOpen${evt.deviceId}"]}"
			for ( def i = 0; i < settings["ShadePartiallyOpen${evt.deviceId}"].size(); i++) {
				SelectionArray[i] = settings["ShadePartiallyOpen${evt.deviceId}"][i].tokenize(":")
				log.debug "DeviceType = ${SelectionArray[i][0]},Device = ${SelectionArray[i][1]},Action = ${SelectionArray[i][2]}"
				SwitchSelection(SelectionArray[i])
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
			log.debug "Device that triggered the event: ${Changed.displayName} Number = $Number | Actions = ${settings["ShadeUnknown${evt.deviceId}"]}"
			for ( def i = 0; i < settings["ShadeUnknown${evt.deviceId}"].size(); i++) {
				SelectionArray[i] = settings["ShadeUnknown${evt.deviceId}"][i].tokenize(":")
				log.debug "DeviceType = ${SelectionArray[i][0]},Device = ${SelectionArray[i][1]},Action = ${SelectionArray[i][2]}"
				SwitchSelection(SelectionArray[i])
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
				for ( def i = 0; i < settings["HighActions${evt.deviceId}"].size(); i++) {
					SelectionArray[i] = settings["HighActions${evt.deviceId}"][i].tokenize(":")
					log.debug "DeviceType = ${SelectionArray[i][0]},Device = ${SelectionArray[i][1]},Action = ${SelectionArray[i][2]}"
					SwitchSelection(SelectionArray[i])
				}
			}
		}
		if (settings["LowActions${evt.deviceId}"]) {
			int LowSetting = settings["LowThreshold${evt.deviceId}"] as Integer
			if (CurrentTemp < LowSetting) {
				for ( def i = 0; i < settings["LowActions${evt.deviceId}"].size(); i++) {
					SelectionArray[i] = settings["LowActions${evt.deviceId}"][i].tokenize(":")
					log.debug "DeviceType = ${SelectionArray[i][0]},Device = ${SelectionArray[i][1]},Action = ${SelectionArray[i][2]}"
					SwitchSelection(SelectionArray[i])
				}
			}
		}
		if (AllHighActions) {
			int HighSetting = AllHighThreshold as Integer
			if (CurrentTemp > HighSetting) {
				for ( def i = 0; i < AllHighActions.size(); i++) {
					SelectionArray[i] = AllHighActions[i].tokenize(":")
					log.debug "DeviceType = ${SelectionArray[i][0]},Device = ${SelectionArray[i][1]},Action = ${SelectionArray[i][2]}"
					SwitchSelection(SelectionArray[i])
				}
			}
		}
		if (AllLowActions) {
			int LowSetting = AllLowThreshold as Integer
			if (CurrentTemp < LowSetting) {
				for ( def i = 0; i < AllLowActions.size(); i++) {
					SelectionArray[i] = AllLowActions[i].tokenize(":")
					log.debug "DeviceType = ${SelectionArray[i][0]},Device = ${SelectionArray[i][1]},Action = ${SelectionArray[i][2]}"
					SwitchSelection(SelectionArray[i])
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
				for ( def i = 0; i < settings["PowerMeterHighActions${evt.deviceId}"].size(); i++) {
					SelectionArray[i] = settings["PowerMeterHighActions${evt.deviceId}"][i].tokenize(":")
					log.debug "DeviceType = ${SelectionArray[i][0]},Device = ${SelectionArray[i][1]},Action = ${SelectionArray[i][2]}"
					SwitchSelection(SelectionArray[i])
				}
			}
		}
		if (settings["PowerMeterLowActions${evt.deviceId}"]) {
			int LowSetting = settings["PowerMeterLowThreshold${evt.deviceId}"] as Integer
			if (Currentstate < LowSetting) {
				for ( def i = 0; i < settings["PowerMeterLowActions${evt.deviceId}"].size(); i++) {
					SelectionArray[i] = settings["PowerMeterLowActions${evt.deviceId}"][i].tokenize(":")
					log.debug "DeviceType = ${SelectionArray[i][0]},Device = ${SelectionArray[i][1]},Action = ${SelectionArray[i][2]}"
					SwitchSelection(SelectionArray[i])
				}
			}
		}
		if (AllPowerMeterHighActions) {
			int HighSetting = AllPowerMeterHighThreshold as Integer
			if (CurrentTemp > HighSetting) {
				for ( def i = 0; i < AllPowerMeterHighActions.size(); i++) {
					SelectionArray[i] = AllPowerMeterHighActions[i].tokenize(":")
					log.debug "DeviceType = ${SelectionArray[i][0]},Device = ${SelectionArray[i][1]},Action = ${SelectionArray[i][2]}"
					SwitchSelection(SelectionArray[i])
				}
			}
		}
		if (AllPowerMeterLowActions) {
			int LowSetting = AllPowerMeterLowThreshold as Integer
			if (CurrentTemp < LowSetting) {
				for ( def i = 0; i < AllPowerMeterLowActions.size(); i++) {
					SelectionArray[i] = AllPowerMeterLowActions[i].tokenize(":")
					log.debug "DeviceType = ${SelectionArray[i][0]},Device = ${SelectionArray[i][1]},Action = ${SelectionArray[i][2]}"
					SwitchSelection(SelectionArray[i])
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
					log.debug "DeviceType = ${SelectionArray[i][0]},Device = ${SelectionArray[i][1]},Action = ${SelectionArray[i][2]}"
					SwitchSelection(SelectionArray[i])
				}
			}
		}
		if (AllBatteryLowActions) {
			int LowSetting = AllBatteryLowThreshold as Integer
			if (Currentstate < LowSetting) {
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
					log.debug "DeviceType = ${SelectionArray[i][0]},Device = ${SelectionArray[i][1]},Action = ${SelectionArray[i][2]}"
					SwitchSelection(SelectionArray[i])
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
				for ( def i = 0; i < settings["VoltageHighActions${evt.deviceId}"].size(); i++) {
					SelectionArray[i] = settings["VoltageHighActions${evt.deviceId}"][i].tokenize(":")
					log.debug "DeviceType = ${SelectionArray[i][0]},Device = ${SelectionArray[i][1]},Action = ${SelectionArray[i][2]}"
					SwitchSelection(SelectionArray[i])
				}
			}
		}
		if (settings["VoltageLowActions${evt.deviceId}"]) {
			int LowSetting = settings["VoltageLowThreshold${evt.deviceId}"] as Integer
			if (Currentstate < LowSetting) {
				for ( def i = 0; i < settings["VoltageLowActions${evt.deviceId}"].size(); i++) {
					SelectionArray[i] = settings["VoltageLowActions${evt.deviceId}"][i].tokenize(":")
					log.debug "DeviceType = ${SelectionArray[i][0]},Device = ${SelectionArray[i][1]},Action = ${SelectionArray[i][2]}"
					SwitchSelection(SelectionArray[i])
				}
			}
		}
		if (AllVoltageHighActions) {
			int HighSetting = AllVoltageHighThreshold as Integer
			if (CurrentTemp > HighSetting) {
				for ( def i = 0; i < AllVoltageHighActions.size(); i++) {
					SelectionArray[i] = AllVoltageHighActions[i].tokenize(":")
					log.debug "DeviceType = ${SelectionArray[i][0]},Device = ${SelectionArray[i][1]},Action = ${SelectionArray[i][2]}"
					SwitchSelection(SelectionArray[i])
				}
			}
		}
		if (AllVoltageLowActions) {
			int LowSetting = AllVoltageLowThreshold as Integer
			if (CurrentTemp < LowSetting) {
				for ( def i = 0; i < AllVoltageLowActions.size(); i++) {
					SelectionArray[i] = AllVoltageLowActions[i].tokenize(":")
					log.debug "DeviceType = ${SelectionArray[i][0]},Device = ${SelectionArray[i][1]},Action = ${SelectionArray[i][2]}"
					SwitchSelection(SelectionArray[i])
				}
			}
		}
	}
}

def DoEnergyMeter(evt) {
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
				for ( def i = 0; i < settings["EnergyMeterHighActions${evt.deviceId}"].size(); i++) {
					SelectionArray[i] = settings["EnergyMeterHighActions${evt.deviceId}"][i].tokenize(":")
					log.debug "DeviceType = ${SelectionArray[i][0]},Device = ${SelectionArray[i][1]},Action = ${SelectionArray[i][2]}"
					SwitchSelection(SelectionArray[i])
				}
			}
		}
		if (settings["EnergyMeterLowActions${evt.deviceId}"]) {
			int LowSetting = settings["EnergyMeterLowThreshold${evt.deviceId}"] as Integer
			if (Currentstate < LowSetting) {
				for ( def i = 0; i < settings["EnergyMeterLowActions${evt.deviceId}"].size(); i++) {
					SelectionArray[i] = settings["EnergyMeterLowActions${evt.deviceId}"][i].tokenize(":")
					log.debug "DeviceType = ${SelectionArray[i][0]},Device = ${SelectionArray[i][1]},Action = ${SelectionArray[i][2]}"
					SwitchSelection(SelectionArray[i])
				}
			}
		}
		if (AllEnergyMeterHighActions) {
			int HighSetting = AllEnergyMeterHighThreshold as Integer
			if (CurrentTemp > HighSetting) {
				for ( def i = 0; i < AllEnergyMeterHighActions.size(); i++) {
					SelectionArray[i] = AllEnergyMeterHighActions[i].tokenize(":")
					log.debug "DeviceType = ${SelectionArray[i][0]},Device = ${SelectionArray[i][1]},Action = ${SelectionArray[i][2]}"
					SwitchSelection(SelectionArray[i])
				}
			}
		}
		if (AllEnergyMeterLowActions) {
			int LowSetting = AllEnergyMeterLowThreshold as Integer
			if (CurrentTemp < LowSetting) {
				for ( def i = 0; i < AllEnergyMeterLowActions.size(); i++) {
					SelectionArray[i] = AllEnergyMeterLowActions[i].tokenize(":")
					log.debug "DeviceType = ${SelectionArray[i][0]},Device = ${SelectionArray[i][1]},Action = ${SelectionArray[i][2]}"
					SwitchSelection(SelectionArray[i])
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
				for ( def i = 0; i < settings["CO2MeasurementHighActions${evt.deviceId}"].size(); i++) {
					SelectionArray[i] = settings["CO2MeasurementHighActions${evt.deviceId}"][i].tokenize(":")
					log.debug "DeviceType = ${SelectionArray[i][0]},Device = ${SelectionArray[i][1]},Action = ${SelectionArray[i][2]}"
					SwitchSelection(SelectionArray[i])
				}
			}
		}
		if (AllCO2MeasurementHighActions) {
			int HighSetting = AllCO2MeasurementHighThreshold as Integer
			if (Currentstate > HighSetting) {
				for ( def i = 0; i < AllCO2MeasurementHighActions.size(); i++) {
					SelectionArray[i] = AllCO2MeasurementHighActions[i].tokenize(":")
					log.debug "DeviceType = ${SelectionArray[i][0]},Device = ${SelectionArray[i][1]},Action = ${SelectionArray[i][2]}"
					SwitchSelection(SelectionArray[i])
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
				for ( def i = 0; i < settings["StepHighActions${evt.deviceId}"].size(); i++) {
					SelectionArray[i] = settings["StepHighActions${evt.deviceId}"][i].tokenize(":")
					log.debug "DeviceType = ${SelectionArray[i][0]},Device = ${SelectionArray[i][1]},Action = ${SelectionArray[i][2]}"
					SwitchSelection(SelectionArray[i])
				}
			}
		}
		if (settings["StepLowActions${evt.deviceId}"]) {
			int LowSetting = settings["StepLowThreshold${evt.deviceId}"] as Integer
			if (Currentstate < LowSetting) {
				for ( def i = 0; i < settings["StepLowActions${evt.deviceId}"].size(); i++) {
					SelectionArray[i] = settings["StepLowActions${evt.deviceId}"][i].tokenize(":")
					log.debug "DeviceType = ${SelectionArray[i][0]},Device = ${SelectionArray[i][1]},Action = ${SelectionArray[i][2]}"
					SwitchSelection(SelectionArray[i])
				}
			}
		}
		if (AllStepHighActions) {
			int HighSetting = AllStepHighThreshold as Integer
			if (Currentstate > HighSetting) {
				for ( def i = 0; i < AllStepHighActions.size(); i++) {
					SelectionArray[i] = AllStepHighActions[i].tokenize(":")
					log.debug "DeviceType = ${SelectionArray[i][0]},Device = ${SelectionArray[i][1]},Action = ${SelectionArray[i][2]}"
					SwitchSelection(SelectionArray[i])
				}
			}
		}
		if (AllStepLowActions) {
			int LowSetting = AllStepLowThreshold as Integer
			if (Currentstate < LowSetting) {
				for ( def i = 0; i < AllStepLowActions.size(); i++) {
					SelectionArray[i] = AllStepLowActions[i].tokenize(":")
					log.debug "DeviceType = ${SelectionArray[i][0]},Device = ${SelectionArray[i][1]},Action = ${SelectionArray[i][2]}"
					SwitchSelection(SelectionArray[i])
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
				for ( def i = 0; i < settings["StepGoalActions${evt.deviceId}"].size(); i++) {
					SelectionArray[i] = settings["StepGoalActions${evt.deviceId}"][i].tokenize(":")
					log.debug "DeviceType = ${SelectionArray[i][0]},Device = ${SelectionArray[i][1]},Action = ${SelectionArray[i][2]}"
					SwitchSelection(SelectionArray[i])
				}
			}
		}
		if (AllStepGoalActions) {
			int HighSetting = AllStepGoalThreshold as Integer
			if (Currentstate >= HighSetting) {
				for ( def i = 0; i < AllStepGoalActions.size(); i++) {
					SelectionArray[i] = AllStepGoalActions[i].tokenize(":")
					log.debug "DeviceType = ${SelectionArray[i][0]},Device = ${SelectionArray[i][1]},Action = ${SelectionArray[i][2]}"
					SwitchSelection(SelectionArray[i])
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
				for ( def i = 0; i < settings["IlluminanceHighActions${evt.deviceId}"].size(); i++) {
					SelectionArray[i] = settings["IlluminanceHighActions${evt.deviceId}"][i].tokenize(":")
					log.debug "DeviceType = ${SelectionArray[i][0]},Device = ${SelectionArray[i][1]},Action = ${SelectionArray[i][2]}"
					SwitchSelection(SelectionArray[i])
				}
			}
		}
		if (settings["IlluminanceLowActions${evt.deviceId}"]) {
			int LowSetting = settings["IlluminanceLowThreshold${evt.deviceId}"] as Integer
			if (Currentstate < LowSetting) {
				for ( def i = 0; i < settings["IlluminanceLowActions${evt.deviceId}"].size(); i++) {
					SelectionArray[i] = settings["IlluminanceLowActions${evt.deviceId}"][i].tokenize(":")
					log.debug "DeviceType = ${SelectionArray[i][0]},Device = ${SelectionArray[i][1]},Action = ${SelectionArray[i][2]}"
					SwitchSelection(SelectionArray[i])
				}
			}
		}
		if (AllIlluminanceHighActions) {
			int HighSetting = AllIlluminanceHighThreshold as Integer
			if (Currentstate > HighSetting) {
				for ( def i = 0; i < AllIlluminanceHighActions.size(); i++) {
					SelectionArray[i] = AllIlluminanceHighActions[i].tokenize(":")
					log.debug "DeviceType = ${SelectionArray[i][0]},Device = ${SelectionArray[i][1]},Action = ${SelectionArray[i][2]}"
					SwitchSelection(SelectionArray[i])
				}
			}
		}
		if (AllIlluminanceLowActions) {
			int LowSetting = AllIlluminanceLowThreshold as Integer
			if (Currentstate < LowSetting) {
				for ( def i = 0; i < AllIlluminanceLowActions.size(); i++) {
					SelectionArray[i] = AllIlluminanceLowActions[i].tokenize(":")
					log.debug "DeviceType = ${SelectionArray[i][0]},Device = ${SelectionArray[i][1]},Action = ${SelectionArray[i][2]}"
					SwitchSelection(SelectionArray[i])
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
				for ( def i = 0; i < settings["HumidityHighActions${evt.deviceId}"].size(); i++) {
					SelectionArray[i] = settings["HumidityHighActions${evt.deviceId}"][i].tokenize(":")
					log.debug "DeviceType = ${SelectionArray[i][0]},Device = ${SelectionArray[i][1]},Action = ${SelectionArray[i][2]}"
					SwitchSelection(SelectionArray[i])
				}
			}
		}
		if (settings["HumidityLowActions${evt.deviceId}"]) {
			int LowSetting = settings["HumidityLowThreshold${evt.deviceId}"] as Integer
			if (Currentstate < LowSetting) {
				for ( def i = 0; i < settings["HumidityLowActions${evt.deviceId}"].size(); i++) {
					SelectionArray[i] = settings["HumidityLowActions${evt.deviceId}"][i].tokenize(":")
					log.debug "DeviceType = ${SelectionArray[i][0]},Device = ${SelectionArray[i][1]},Action = ${SelectionArray[i][2]}"
					SwitchSelection(SelectionArray[i])
				}
			}
		}
		if (AllHumidityHighActions) {
			int HighSetting = AllHumidityHighThreshold as Integer
			if (Currentstate > HighSetting) {
				for ( def i = 0; i < AllHumidityHighActions.size(); i++) {
					SelectionArray[i] = AllHumidityHighActions[i].tokenize(":")
					log.debug "DeviceType = ${SelectionArray[i][0]},Device = ${SelectionArray[i][1]},Action = ${SelectionArray[i][2]}"
					SwitchSelection(SelectionArray[i])
				}
			}
		}
		if (AllHumidityLowActions) {
			int LowSetting = AllHumidityLowThreshold as Integer
			if (Currentstate < LowSetting) {
				for ( def i = 0; i < AllHumidityLowActions.size(); i++) {
					SelectionArray[i] = AllHumidityLowActions[i].tokenize(":")
					log.debug "DeviceType = ${SelectionArray[i][0]},Device = ${SelectionArray[i][1]},Action = ${SelectionArray[i][2]}"
					SwitchSelection(SelectionArray[i])
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
				for ( def i = 0; i < settings["UltravioletIndexHighActions${evt.deviceId}"].size(); i++) {
					SelectionArray[i] = settings["UltravioletIndexHighActions${evt.deviceId}"][i].tokenize(":")
					log.debug "DeviceType = ${SelectionArray[i][0]},Device = ${SelectionArray[i][1]},Action = ${SelectionArray[i][2]}"
					SwitchSelection(SelectionArray[i])
				}
			}
		}
		if (settings["UltravioletIndexLowActions${evt.deviceId}"]) {
			int LowSetting = settings["UltravioletIndexLowThreshold${evt.deviceId}"] as Integer
			if (Currentstate < LowSetting) {
				for ( def i = 0; i < settings["UltravioletIndexLowActions${evt.deviceId}"].size(); i++) {
					SelectionArray[i] = settings["UltravioletIndexLowActions${evt.deviceId}"][i].tokenize(":")
					log.debug "DeviceType = ${SelectionArray[i][0]},Device = ${SelectionArray[i][1]},Action = ${SelectionArray[i][2]}"
					SwitchSelection(SelectionArray[i])
				}
			}
		}
		if (AllUltravioletIndexHighActions) {
			int HighSetting = AllUltravioletIndexHighThreshold as Integer
			if (Currentstate > HighSetting) {
				for ( def i = 0; i < AllUltravioletIndexHighActions.size(); i++) {
					SelectionArray[i] = AllUltravioletIndexHighActions[i].tokenize(":")
					log.debug "DeviceType = ${SelectionArray[i][0]},Device = ${SelectionArray[i][1]},Action = ${SelectionArray[i][2]}"
					SwitchSelection(SelectionArray[i])
				}
			}
		}
		if (AllUltravioletIndexLowActions) {
			int LowSetting = AllUltravioletIndexLowThreshold as Integer
			if (Currentstate < LowSetting) {
				for ( def i = 0; i < AllUltravioletIndexLowActions.size(); i++) {
					SelectionArray[i] = AllUltravioletIndexLowActions[i].tokenize(":")
					log.debug "DeviceType = ${SelectionArray[i][0]},Device = ${SelectionArray[i][1]},Action = ${SelectionArray[i][2]}"
					SwitchSelection(SelectionArray[i])
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
				for ( def i = 0; i < settings["phMeasurementHighActions${evt.deviceId}"].size(); i++) {
					SelectionArray[i] = settings["phMeasurementHighActions${evt.deviceId}"][i].tokenize(":")
					log.debug "DeviceType = ${SelectionArray[i][0]},Device = ${SelectionArray[i][1]},Action = ${SelectionArray[i][2]}"
					SwitchSelection(SelectionArray[i])
				}
			}
		}
		if (settings["phMeasurementLowActions${evt.deviceId}"]) {
			int LowSetting = settings["phMeasurementLowThreshold${evt.deviceId}"] as Integer
			if (Currentstate < LowSetting) {
				for ( def i = 0; i < settings["phMeasurementLowActions${evt.deviceId}"].size(); i++) {
					SelectionArray[i] = settings["phMeasurementLowActions${evt.deviceId}"][i].tokenize(":")
					log.debug "DeviceType = ${SelectionArray[i][0]},Device = ${SelectionArray[i][1]},Action = ${SelectionArray[i][2]}"
					SwitchSelection(SelectionArray[i])
				}
			}
		}
		if (AllphMeasurementHighActions) {
			int HighSetting = AllphMeasurementHighThreshold as Integer
			if (Currentstate > HighSetting) {
				for ( def i = 0; i < AllphMeasurementHighActions.size(); i++) {
					SelectionArray[i] = AllphMeasurementHighActions[i].tokenize(":")
					log.debug "DeviceType = ${SelectionArray[i][0]},Device = ${SelectionArray[i][1]},Action = ${SelectionArray[i][2]}"
					SwitchSelection(SelectionArray[i])
				}
			}
		}
		if (AllphMeasurementLowActions) {
			int LowSetting = AllphMeasurementLowThreshold as Integer
			if (Currentstate < LowSetting) {
				for ( def i = 0; i < AllphMeasurementLowActions.size(); i++) {
					SelectionArray[i] = AllphMeasurementLowActions[i].tokenize(":")
					log.debug "DeviceType = ${SelectionArray[i][0]},Device = ${SelectionArray[i][1]},Action = ${SelectionArray[i][2]}"
					SwitchSelection(SelectionArray[i])
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
				for ( def i = 0; i < settings["soundPressureLevelHighActions${evt.deviceId}"].size(); i++) {
					SelectionArray[i] = settings["soundPressureLevelHighActions${evt.deviceId}"][i].tokenize(":")
					log.debug "DeviceType = ${SelectionArray[i][0]},Device = ${SelectionArray[i][1]},Action = ${SelectionArray[i][2]}"
					SwitchSelection(SelectionArray[i])
				}
			}
		}
		if (settings["soundPressureLevelLowActions${evt.deviceId}"]) {
			int LowSetting = settings["soundPressureLevelLowThreshold${evt.deviceId}"] as Integer
			if (Currentstate < LowSetting) {
				for ( def i = 0; i < settings["soundPressureLevelLowActions${evt.deviceId}"].size(); i++) {
					SelectionArray[i] = settings["soundPressureLevelLowActions${evt.deviceId}"][i].tokenize(":")
					log.debug "DeviceType = ${SelectionArray[i][0]},Device = ${SelectionArray[i][1]},Action = ${SelectionArray[i][2]}"
					SwitchSelection(SelectionArray[i])
				}
			}
		}
		if (AllsoundPressureLevelHighActions) {
			int HighSetting = AllsoundPressureLevelHighThreshold as Integer
			if (Currentstate > HighSetting) {
				for ( def i = 0; i < AllsoundPressureLevelHighActions.size(); i++) {
					SelectionArray[i] = AllsoundPressureLevelHighActions[i].tokenize(":")
					log.debug "DeviceType = ${SelectionArray[i][0]},Device = ${SelectionArray[i][1]},Action = ${SelectionArray[i][2]}"
					SwitchSelection(SelectionArray[i])
				}
			}
		}
		if (AllsoundPressureLevelLowActions) {
			int LowSetting = AllsoundPressureLevelLowThreshold as Integer
			if (Currentstate < LowSetting) {
				for ( def i = 0; i < AllsoundPressureLevelLowActions.size(); i++) {
					SelectionArray[i] = AllsoundPressureLevelLowActions[i].tokenize(":")
					log.debug "DeviceType = ${SelectionArray[i][0]},Device = ${SelectionArray[i][1]},Action = ${SelectionArray[i][2]}"
					SwitchSelection(SelectionArray[i])
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
			//log.debug "Device that triggered the event: ${Changed.displayName} | Number = $evt.deviceId | Button $buttonNumber Pushed | Command = ${settings["${evt.deviceId}Button${buttonNumber}Push"]}"
			for ( def i = 0; i < settings["${evt.deviceId}Button${buttonNumber}Push"].size(); i++) {
				SelectionArray[i] = settings["${evt.deviceId}Button${buttonNumber}Push"][i].tokenize(":")
				log.debug "DeviceType = ${SelectionArray[i][0]},Device = ${SelectionArray[i][1]},Action = ${SelectionArray[i][2]}"
				SwitchSelection(SelectionArray[i])
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
			//log.debug "Device that triggered the event: ${Changed.displayName} | Number = $Number | Button $buttonNumber Held | Command = ${settings["${evt.deviceId}Button${buttonNumber}Hold"]}"
			for ( def i = 0; i < settings["${evt.deviceId}Button${buttonNumber}Hold"].size(); i++) {
				SelectionArray[i] = settings["${evt.deviceId}Button${buttonNumber}Hold"][i].tokenize(":")
				log.debug "DeviceType = ${SelectionArray[i][0]},Device = ${SelectionArray[i][1]},Action = ${SelectionArray[i][2]}"
				SwitchSelection(SelectionArray[i])
			}		 
		}
	}
}

def SwitchSelection(Selection) {
	def hueColor = 0
	def saturation = 100
	log.debug "Selection[2] = ${Selection[2]}"
	switch (Selection[2]) {
		case "on":
			if (Selection[0] == "Bulbs") {
				if ("${Selection[1]}" != "all") {
						Bulbs.each {
							if ("$it" == "${Selection[1]}") {log.debug "Turning $it on"}
							if ("$it" == "${Selection[1]}") {
								it.on()
								it.setLevel(100)
							}
						}
					} else {
						log.debug "Turning All Bulbs on"
						Bulbs.on()
						Bulbs.setLevel(100)
				}
			}
			if (Selection[0] == "Dimmers") {
				if ("${Selection[1]}" != "all") {
						Dimmers.each {
							if ("$it" == "${Selection[1]}") {log.debug "Turning $it on"}
							if ("$it" == "${Selection[1]}") {
								it.on()
								it.setLevel(100)
							}
						}
					} else {
						log.debug "Turning All Dimmers on"
						Dimmers.on()
						Dimmers.setLevel(100)
				}
			}
			if (Selection[0] == "Switches") {
				if ("${Selection[1]}" != "all") {
						Switches.each {
							if ("$it" == "${Selection[1]}") {log.debug "Turning $it on"}
							if ("$it" == "${Selection[1]}") {it.on()}
						}
					} else {
						log.debug "Turning All Switches on"
						Switches.on()
				}
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
			if ("${Selection[1]}" != "all") {
					AudioNotify.each {
						if ("$it" == "${Selection[1]}") {
							log.debug "Activating doorbell on $it"
							it.on()
						}
					}
				} else {
					log.debug "Activating Doorbell on All"
					AudioNotify.on()
			}
		break
		case "Siren":
			if ("${Selection[1]}" != "all") {
					AudioNotify.each {
						if ("$it" == "${Selection[1]}") {
							log.debug "Activating siren on $it"
							it.both()
						}
					}
				} else {
					log.debug "Activating Siren on All"
					AudioNotify.both()
			}
		break
		case "Beep":
			if ("${Selection[1]}" != "all") {
					AudioNotify.each {
						if ("$it" == "${Selection[1]}") {
							log.debug "Activating beep on $it"
							it.beep()
						}
					}
				} else {
					log.debug "Activating Beep on All"
					AudioNotify.beep()
			}
		break
		case "Track":
			if ("${Selection[1]}" != "all") {
					AudioNotify.each {
						if ("$it" == "${Selection[1]}") {
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
				} else {
					log.debug "Activating track ${Selection[1]} on All"
							if (!AudioNotifyAtVolume && !AudioNotifyRepeat) {
								log.debug "Activating track ${Selection[3]} on All"
								AudioNotify.playTrack(Selection[3])
							}
							if (!AudioNotifyAtVolume && AudioNotifyRepeat) {
								log.debug "Activating track ${Selection[3]} on All $AudioNotifyRepeatCount times"
								AudioNotify.playRepeatTrack(Selection[3], AudioNotifyRepeatCount)
							}
							if (AudioNotifyAtVolume && !AudioNotifyRepeat) {
								log.debug "Activating track ${Selection[3]} on All at volume $AudioNotifyVolume"
								AudioNotify.playTrackAtVolume(Selection[3], AudioNotifyVolume)
							}
							if (AudioNotifyAtVolume && AudioNotifyRepeat) {
								log.debug "Activating track ${Selection[3]} on All at volume $AudioNotifyVolume $AudioNotifyRepeatCount times"
								AudioNotify.playRepeatTrackAtVolume(Selection[3], AudioNotifyVolume, AudioNotifyRepeatCount)
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
		case "siren":
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
			Favorite(Selection[1],Selection[0])
		break
		case "full":
			if (Selection[0] == "Bulbs") {
				if ("${Selection[1]}" != "all") {
						Bulbs.each {
							if ("$it" == "${Selection[1]}") {log.debug "Turning $it to full"}
							if ("$it" == "${Selection[1]}") {it.setLevel(100)}
						}
					} else {
						log.debug "Turning All Bulbs to full"
						Bulbs.setLevel(100)
				}
			}
			if (Selection[0] == "Dimmers") {
				if ("${Selection[1]}" != "all") {
						Dimmers.each {
							if ("$it" == "${Selection[1]}") {log.debug "Turning $it to full"}
							if ("$it" == "${Selection[1]}") {it.setLevel(100)}
						}
					} else {
						log.debug "Turning All Dimmers to full"
						Dimmers.setLevel(100)
				}
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

def Favorite(selection,DeviceName) {
	if (DeviceName == "Bulbs") {
		if (selection != "all") {
			Bulbs.each {
				if ("$it" == selection) {log.debug "Set $it to $BulbsFav"}
				if ("$it" == selection) {it.setLevel(BulbsFav as Integer)}
			}
		} else {
			log.debug "Set All to $BulbsFav"
			Bulbs.setLevel(BulbsFav as Integer)
		}
	}
	if (DeviceName == "Dimmers") {
		if (selection != "all") {
			Dimmers.each {
				if ("$it" == selection) {log.debug "Set $it to $DimmersFav"}
				if ("$it" == selection) {it.setLevel(DimmersFav as Integer)}
			}
		} else {
			log.debug "Set All to $DimmersFav"
			Dimmers.setLevel(DimmersFav as Integer)
		}
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
			boolean OkToRun = false
			int Currentstate = Illuminance_Required.currentValue("illuminance") as Integer
			log.debug "Currentstate = $Currentstate | Illuminance_RequiredLessThan = $Illuminance_RequiredLessThan | Illuminance_RequiredGreaterThan = $Illuminance_RequiredGreaterThan"
			if (Illuminance_RequiredLessThan && Illuminance_RequiredGreaterThan) {
					if (Currentstate <= Illuminance_RequiredLessThan && Currentstate >= Illuminance_RequiredGreaterThan) {OkToRun = true}
				} else {
					if (Illuminance_RequiredLessThan) {
						if (Currentstate <= Illuminance_RequiredLessThan) {OkToRun = true}
					}
					if (Illuminance_RequiredGreaterThan) {
						if (Currentstate >= Illuminance_RequiredGreaterThan) {OkToRun = true}
					}
			}
			log.debug "Illuminance OkToRun = $OkToRun"
			return OkToRun
		} else {
			return true
	}
}

private CurrentTemperature() {
	if (Temperature_RequiredLessThan || Temperature_RequiredGreaterThan) {
			boolean OkToRun = false
			int Currentstate = Temperature_Required.currentValue("temperature") as Integer
			log.debug "Currentstate = $Currentstate | Temperature_RequiredLessThan = $Temperature_RequiredLessThan | Temperature_RequiredGreaterThan = $Temperature_RequiredGreaterThan"
			if (Temperature_RequiredLessThan && Temperature_RequiredGreaterThan) {
					if (Currentstate <= Temperature_RequiredLessThan && Currentstate >= Temperature_RequiredGreaterThan) {OkToRun = true}
				} else {
					if (Temperature_RequiredLessThan) {
						if (Currentstate <= Temperature_RequiredLessThan) {OkToRun = true}
					}
					if (Temperature_RequiredGreaterThan) {
						if (Currentstate >= Temperature_RequiredGreaterThan) {OkToRun = true}
					}
			}
			log.debug "Temperature OkToRun = $OkToRun"
			return OkToRun
		} else {
			return true
	}
}

private CurrentPowerMeter() {
	if (PowerMeter_RequiredLessThan || PowerMeter_RequiredGreaterThan) {
			boolean OkToRun = false
			int Currentstate = PowerMeter_Required.currentValue("power") as Integer
			log.debug "Currentstate = $Currentstate | PowerMeter_RequiredLessThan = $PowerMeter_RequiredLessThan | PowerMeter_RequiredGreaterThan = $PowerMeter_RequiredGreaterThan"
			if (PowerMeter_RequiredLessThan && PowerMeter_RequiredGreaterThan) {
					if (Currentstate <= PowerMeter_RequiredLessThan && Currentstate >= PowerMeter_RequiredGreaterThan) {OkToRun = true}
				} else {
					if (PowerMeter_RequiredLessThan) {
						if (Currentstate <= PowerMeter_RequiredLessThan) {OkToRun = true}
					}
					if (PowerMeter_RequiredGreaterThan) {
						if (Currentstate >= PowerMeter_RequiredGreaterThan) {OkToRun = true}
					}
			}
			log.debug "PowerMeter OkToRun = $OkToRun"
			return OkToRun
		} else {
			return true
	}
}

private CurrentVoltage() {
	if (Voltage_RequiredLessThan || Voltage_RequiredGreaterThan) {
			boolean OkToRun = false
			int Currentstate = Voltage_Required.currentValue("voltage") as Integer
			log.debug "Currentstate = $Currentstate | Voltage_RequiredLessThan = $Voltage_RequiredLessThan | Voltage_RequiredGreaterThan = $Voltage_RequiredGreaterThan"
			if (Voltage_RequiredLessThan && Voltage_RequiredGreaterThan) {
					if (Currentstate <= Voltage_RequiredLessThan && Currentstate >= Voltage_RequiredGreaterThan) {OkToRun = true}
				} else {
					if (Voltage_RequiredLessThan) {
						if (Currentstate <= Voltage_RequiredLessThan) {OkToRun = true}
					}
					if (Voltage_RequiredGreaterThan) {
						if (Currentstate >= Voltage_RequiredGreaterThan) {OkToRun = true}
					}
			}
			log.debug "Voltage OkToRun = $OkToRun"
			return OkToRun
		} else {
			return true
	}
}

private CurrentEnergyMeter() {
	if (EnergyMeter_RequiredLessThan || EnergyMeter_RequiredGreaterThan) {
			boolean OkToRun = false
			int Currentstate = EnergyMeter_Required.currentValue("energy") as Integer
			log.debug "Currentstate = $Currentstate | EnergyMeter_RequiredLessThan = $EnergyMeter_RequiredLessThan | EnergyMeter_RequiredGreaterThan = $EnergyMeter_RequiredGreaterThan"
			if (EnergyMeter_RequiredLessThan && EnergyMeter_RequiredGreaterThan) {
					if (Currentstate <= EnergyMeter_RequiredLessThan && Currentstate >= EnergyMeter_RequiredGreaterThan) {OkToRun = true}
				} else {
					if (EnergyMeter_RequiredLessThan) {
						if (Currentstate <= EnergyMeter_RequiredLessThan) {OkToRun = true}
					}
					if (EnergyMeter_RequiredGreaterThan) {
						if (Currentstate >= EnergyMeter_RequiredGreaterThan) {OkToRun = true}
					}
			}
			log.debug "EnergyMeter OkToRun = $OkToRun"
			return OkToRun
		} else {
			return true
	}
}

private CurrentCO2Measurement() {
	if (CO2Measurement_RequiredLessThan || CO2Measurement_RequiredGreaterThan) {
			boolean OkToRun = false
			int Currentstate = CO2Measurement_Required.currentValue("carbonDioxide") as Integer
			log.debug "Currentstate = $Currentstate | CO2Measurement_RequiredLessThan = $CO2Measurement_RequiredLessThan | CO2Measurement_RequiredGreaterThan = $CO2Measurement_RequiredGreaterThan"
			if (CO2Measurement_RequiredLessThan && CO2Measurement_RequiredGreaterThan) {
					if (Currentstate <= CO2Measurement_RequiredLessThan && Currentstate >= CO2Measurement_RequiredGreaterThan) {OkToRun = true}
				} else {
					if (CO2Measurement_RequiredLessThan) {
						if (Currentstate <= CO2Measurement_RequiredLessThan) {OkToRun = true}
					}
					if (CO2Measurement_RequiredGreaterThan) {
						if (Currentstate >= CO2Measurement_RequiredGreaterThan) {OkToRun = true}
					}
			}
			log.debug "CO2Measurement OkToRun = $OkToRun"
			return OkToRun
		} else {
			return true
	}
}

private CurrentHumidity() {
	if (Humidity_RequiredLessThan || Humidity_RequiredGreaterThan) {
			boolean OkToRun = false
			int Currentstate = Humidity_Required.currentValue("humidity") as Integer
			log.debug "Currentstate = $Currentstate | Humidity_RequiredLessThan = $Humidity_RequiredLessThan | Humidity_RequiredGreaterThan = $Humidity_RequiredGreaterThan"
			if (Humidity_RequiredLessThan && Humidity_RequiredGreaterThan) {
					if (Currentstate <= Humidity_RequiredLessThan && Currentstate >= Humidity_RequiredGreaterThan) {OkToRun = true}
				} else {
					if (Humidity_RequiredLessThan) {
						if (Currentstate <= Humidity_RequiredLessThan) {OkToRun = true}
					}
					if (Humidity_RequiredGreaterThan) {
						if (Currentstate >= Humidity_RequiredGreaterThan) {OkToRun = true}
					}
			}
			log.debug "Humidity OkToRun = $OkToRun"
			return OkToRun
		} else {
			return true
	}
}

private CurrentUltravioletIndex() {
	if (UltravioletIndex_RequiredLessThan || UltravioletIndex_RequiredGreaterThan) {
			boolean OkToRun = false
			int Currentstate = UltravioletIndex_Required.currentValue("ultravioletIndex") as Integer
			log.debug "Currentstate = $Currentstate | UltravioletIndex_RequiredLessThan = $UltravioletIndex_RequiredLessThan | UltravioletIndex_RequiredGreaterThan = $UltravioletIndex_RequiredGreaterThan"
			if (UltravioletIndex_RequiredLessThan && UltravioletIndex_RequiredGreaterThan) {
					if (Currentstate <= UltravioletIndex_RequiredLessThan && Currentstate >= UltravioletIndex_RequiredGreaterThan) {OkToRun = true}
				} else {
					if (UltravioletIndex_RequiredLessThan) {
						if (Currentstate <= UltravioletIndex_RequiredLessThan) {OkToRun = true}
					}
					if (UltravioletIndex_RequiredGreaterThan) {
						if (Currentstate >= UltravioletIndex_RequiredGreaterThan) {OkToRun = true}
					}
			}
			log.debug "UltravioletIndex OkToRun = $OkToRun"
			return OkToRun
		} else {
			return true
	}
}

private CurrentphMeasurement() {
	if (phMeasurement_RequiredLessThan || phMeasurement_RequiredGreaterThan) {
			boolean OkToRun = false
			int Currentstate = phMeasurement_Required.currentValue("pH") as Integer
			log.debug "Currentstate = $Currentstate | phMeasurement_RequiredLessThan = $phMeasurement_RequiredLessThan | phMeasurement_RequiredGreaterThan = $phMeasurement_RequiredGreaterThan"
			if (phMeasurement_RequiredLessThan && phMeasurement_RequiredGreaterThan) {
					if (Currentstate <= phMeasurement_RequiredLessThan && Currentstate >= phMeasurement_RequiredGreaterThan) {OkToRun = true}
				} else {
					if (phMeasurement_RequiredLessThan) {
						if (Currentstate <= phMeasurement_RequiredLessThan) {OkToRun = true}
					}
					if (phMeasurement_RequiredGreaterThan) {
						if (Currentstate >= phMeasurement_RequiredGreaterThan) {OkToRun = true}
					}
			}
			log.debug "phMeasurement OkToRun = $OkToRun"
			return OkToRun
		} else {
			return true
	}
}

private CurrentsoundPressureLevel() {
	if (soundPressureLevel_RequiredLessThan || soundPressureLevel_RequiredGreaterThan) {
			boolean OkToRun = false
			int Currentstate = soundPressureLevel_Required.currentValue("soundPressureLevel") as Integer
			log.debug "Currentstate = $Currentstate | soundPressureLevel_RequiredLessThan = $soundPressureLevel_RequiredLessThan | soundPressureLevel_RequiredGreaterThan = $soundPressureLevel_RequiredGreaterThan"
			if (soundPressureLevel_RequiredLessThan && soundPressureLevel_RequiredGreaterThan) {
					if (Currentstate <= soundPressureLevel_RequiredLessThan && Currentstate >= soundPressureLevel_RequiredGreaterThan) {OkToRun = true}
				} else {
					if (soundPressureLevel_RequiredLessThan) {
						if (Currentstate <= soundPressureLevel_RequiredLessThan) {OkToRun = true}
					}
					if (soundPressureLevel_RequiredGreaterThan) {
						if (Currentstate >= soundPressureLevel_RequiredGreaterThan) {OkToRun = true}
					}
			}
			log.debug "soundPressureLevel OkToRun = $OkToRun"
			return OkToRun
		} else {
			return true
	}
}

private CurrentContact() {
	if (Contact_Required) {
			boolean OkToRun = false
			def Currentstate = Contact_Required.currentValue("contact")
			log.debug "Currentstate = ${Currentstate.toLowerCase()} | Contact_state = ${Contact_state.toLowerCase()}"
			if (Currentstate.toLowerCase() == Contact_state.toLowerCase()) {OkToRun = true}
			log.debug "Contact OkToRun = $OkToRun"
			return OkToRun
		} else {
			return true
	}
}

private CurrentSwitch() {
	if (Switch_Required) {
			boolean OkToRun = false
			def Currentstate = Switch_Required.currentValue("switch")
			log.debug "Currentstate = ${Currentstate.toLowerCase()} | Switch_state = ${Switch_state.toLowerCase()}"
			if (Currentstate.toLowerCase() == Switch_state.toLowerCase()) {OkToRun = true}
			log.debug "Switch OkToRun = $OkToRun"
			return OkToRun
		} else {
			return true
	}
}

private CurrentAcceleration() {
	if (Acceleration_Required) {
			boolean OkToRun = false
			def Currentstate = Acceleration_Required.currentValue("acceleration")
			log.debug "Currentstate = ${Currentstate.toLowerCase()} | Acceleration_state = ${Acceleration_state.toLowerCase()}"
			if (Currentstate.toLowerCase() == Acceleration_state.toLowerCase()) {OkToRun = true}
			log.debug "Acceleration OkToRun = $OkToRun"
			return OkToRun
		} else {
			return true
	}
}

private CurrentMotion() {
	if (Motion_Required) {
			boolean OkToRun = false
			def Currentstate = Motion_Required.currentValue("motionSensor")
			log.debug "Currentstate = ${Currentstate.toLowerCase()} | Motion_state = ${Motion_state.toLowerCase()}"
			if (Currentstate.toLowerCase() == Motion_state.toLowerCase()) {OkToRun = true}
			log.debug "Motion OkToRun = $OkToRun"
			return OkToRun
		} else {
			return true
	}
}

private CurrentTamper() {
	if (Tamper_Required) {
			boolean OkToRun = false
			def Currentstate = Tamper_Required.currentValue("tamper")
			log.debug "Currentstate = ${Currentstate.toLowerCase()} | Tamper_state = ${Tamper_state.toLowerCase()}"
			if (Currentstate.toLowerCase() == Tamper_state.toLowerCase()) {OkToRun = true}
			log.debug "Tamper OkToRun = $OkToRun"
			return OkToRun
		} else {
			return true
	}
}

private CurrentShock() {
	if (Shock_Required) {
			boolean OkToRun = false
			def Currentstate = Shock_Required.currentValue("shock")
			log.debug "Currentstate = ${Currentstate.toLowerCase()} | Shock_state = ${Shock_state.toLowerCase()}"
			if (Currentstate.toLowerCase() == Shock_state.toLowerCase()) {OkToRun = true}
			log.debug "Shock OkToRun = $OkToRun"
			return OkToRun
		} else {
			return true
	}
}

private CurrentSleep() {
	if (Sleep_Required) {
			boolean OkToRun = false
			def Currentstate = Sleep_Required.currentValue("sleeping")
			log.debug "Currentstate = ${Currentstate.toLowerCase()} | Sleep_state = ${Sleep_state.toLowerCase()}"
			if (Currentstate.toLowerCase() == Sleep_state.toLowerCase()) {OkToRun = true}
			log.debug "Sleep OkToRun = $OkToRun"
			return OkToRun
		} else {
			return true
	}
}

private CurrentSound() {
	if (Sound_Required) {
			boolean OkToRun = false
			def Currentstate = Sound_Required.currentValue("sound")
			log.debug "Currentstate = ${Currentstate.toLowerCase()} | Sound_state = ${Sound_state.toLowerCase()}"
			if (Currentstate.toLowerCase() == Sound_state.toLowerCase()) {OkToRun = true}
			log.debug "Sound OkToRun = $OkToRun"
			return OkToRun
		} else {
			return true
	}
}

private CurrentWater() {
	if (Water_Required) {
			boolean OkToRun = false
			def Currentstate = Water_Required.currentValue("water")
			log.debug "Currentstate = ${Currentstate.toLowerCase()} | Water_state = ${Water_state.toLowerCase()}"
			if (Currentstate.toLowerCase() == Water_state.toLowerCase()) {OkToRun = true}
			log.debug "Water OkToRun = $OkToRun"
			return OkToRun
		} else {
			return true
	}
}

private CurrentBeacon() {
	if (Beacon_Required) {
			boolean OkToRun = false
			def Currentstate = Beacon_Required.currentValue("presence")
			log.debug "Currentstate = ${Currentstate.toLowerCase()} | Beacon_state = ${Beacon_state.toLowerCase()}"
			if (Currentstate.toLowerCase() == Beacon_state.toLowerCase()) {OkToRun = true}
			log.debug "Beacon OkToRun = $OkToRun"
			return OkToRun
		} else {
			return true
	}
}

private CurrentPresence() {
	if (Presence_Required) {
			boolean OkToRun = false
			def Currentstate = Presence_Required.currentValue("presence")
			log.debug "Currentstate = ${Currentstate.toLowerCase()} | Presence_state = ${Presence_state.toLowerCase()}"
			if (Currentstate.toLowerCase() == Presence_state.toLowerCase()) {OkToRun = true}
			log.debug "Presence OkToRun = $OkToRun"
			return OkToRun
		} else {
			return true
	}
}

private CurrentCODetector() {
	if (CODetector_Required) {
			boolean OkToRun = false
			def Currentstate = CODetector_Required.currentValue("carbonMonoxide")
			log.debug "Currentstate = ${Currentstate.toLowerCase()} | CODetector_state = ${CODetector_state.toLowerCase()}"
			if (Currentstate.toLowerCase() == CODetector_state.toLowerCase()) {OkToRun = true}
			log.debug "CODetector OkToRun = $OkToRun"
			return OkToRun
		} else {
			return true
	}
}

private CurrentSmoke() {
	if (Smoke_Required) {
			boolean OkToRun = false
			def Currentstate = Smoke_Required.currentValue("smoke")
			log.debug "Currentstate = ${Currentstate.toLowerCase()} | Smoke_state = ${Smoke_state.toLowerCase()}"
			if (Currentstate.toLowerCase() == Smoke_state.toLowerCase()) {OkToRun = true}
			log.debug "Smoke OkToRun = $OkToRun"
			return OkToRun
		} else {
			return true
	}
}

private CurrentPowerSource() {
	if (PowerSource_Required) {
			boolean OkToRun = false
			def Currentstate = PowerSource_Required.currentValue("powerSource")
			log.debug "Currentstate = ${Currentstate.toLowerCase()} | PowerSource_state = ${PowerSource_state.toLowerCase()}"
			if (Currentstate.toLowerCase() == PowerSource_state.toLowerCase()) {OkToRun = true}
			log.debug "PowerSource OkToRun = $OkToRun"
			return OkToRun
		} else {
			return true
	}
}

private CurrentDoor() {
	if (Door_Required) {
			boolean OkToRun = false
			def Currentstate = Door_Required.currentValue("door")
			log.debug "Currentstate = ${Currentstate.toLowerCase()} | Door_state = ${Door_state.toLowerCase()}"
			if (Currentstate.toLowerCase() == Door_state.toLowerCase()) {OkToRun = true}
			log.debug "Door OkToRun = $OkToRun"
			return OkToRun
		} else {
			return true
	}
}

private CurrentValve() {
	if (Valve_Required) {
			boolean OkToRun = false
			def Currentstate = Valve_Required.currentValue("contact")
			log.debug "Currentstate = ${Currentstate.toLowerCase()} | Valve_state = ${Valve_state.toLowerCase()}"
			if (Currentstate.toLowerCase() == Valve_state.toLowerCase()) {OkToRun = true}
			log.debug "Valve OkToRun = $OkToRun"
			return OkToRun
		} else {
			return true
	}
}

private CurrentShade() {
	if (Shade_Required) {
			boolean OkToRun = false
			def Currentstate = Shade_Required.currentValue("windowShade")
			log.debug "Currentstate = ${Currentstate.toLowerCase()} | Shade_state = ${Shade_state.toLowerCase()}"
			if (Currentstate.toLowerCase() == Shade_state.toLowerCase()) {OkToRun = true}
			log.debug "Shade OkToRun = $OkToRun"
			return OkToRun
		} else {
			return true
	}
}
