/**
 *  groveStreams
 *
 *  Copyright 2014 Yves Racine
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
 *  Based on code from Jason Steele & Minollo
 */
definition(
	name: "groveStreams",
	namespace: "yracine",
	author: "Yves Racine",
	description: "Log to groveStreams and send data streams based on devices selection",
	category: "My Apps",
	iconUrl: "https://s3.amazonaws.com/smartapp-icons/Partner/ecobee.png",
	iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Partner/ecobee@2x.png"
)



preferences {
	section("About") {
		paragraph "groveStreams, the smartapp that sends your device states to groveStreams for data correlation"
		paragraph "Version 1.9\n\n" +
			"If you like this app, please support the developer via PayPal:\n\nyracine@yahoo.com\n\n" +
			"Copyright©2014 Yves Racine"
		href url: "http://github.com/yracine", style: "embedded", required: false, title: "More information...",
		description: "http://github.com/yracine/device-type.myecobee/blob/master/README.md"
	}
	section("Log devices...") {
		input "temperatures", "capability.temperatureMeasurement", title: "Temperatures", required: false, multiple: true
		input "thermostats", "capability.thermostat", title: "Thermostats", required: false, multiple: true
		input "ecobees", "capability.thermostat", title: "Ecobees", required: false, multiple: true
		input "detectors", "capability.smokeDetector", title: "Smoke/CarbonMonoxide Detectors", required: false, multiple: true
		input "humidities", "capability.relativeHumidityMeasurement", title: "Humidity sensors", required: false, multiple: true
		input "waters", "capability.waterSensor", title: "Water sensors", required: false, multiple: true
		input "illuminances", "capability.illuminanceMeasurement", title: "Illuminance sensor", required: false, multiple: true
		input "locks", "capability.lock", title: "Locks", required: false, multiple: true
		input "contacts", "capability.contactSensor", title: "Doors open/close", required: false, multiple: true
		input "accelerations", "capability.accelerationSensor", title: "Accelerations", required: false, multiple: true
		input "motions", "capability.motionSensor", title: "Motions", required: false, multiple: true
		input "presence", "capability.presenceSensor", title: "Presence", required: false, multiple: true
		input "switches", "capability.switch", title: "Switches", required: false, multiple: true
		input "batteries", "capability.battery", title: "Battery-powered devices", required: false, multiple: true
		input "powers", "capability.powerMeter", title: "Power Meters", required: false, multiple: true
		input "energys", "capability.energyMeter", title: "Energy Meters", required: false, multiple: true

	}

	section("GroveStreams Feed PUT API key...") {
		input "channelKey", "text", title: "API key"
	}
	section("Sending data at which interval in minutes (default=5)?") {
		input "givenInterval", "number", title: 'Send Data Interval', required: false
	}
}

def installed() {
	initialize()
}

def updated() {
	unsubscribe()
	unschedule()
	initialize()
}

def initialize() {
	subscribe(temperatures, "temperature", handleTemperatureEvent)
	subscribe(humidities, "relativeHumidityMeasurement", handleHumidityEvent)
	subscribe(waters, "water", handleWaterEvent)
	subscribe(waters, "water", handleWaterEvent)
	subscribe(detectors, "smoke", handleSmokeEvent)
	subscribe(detectors, "carbonMonoxide", handleCarbonMonoxideEvent)
	subscribe(illuminances, "illuminanceMeasurement", handleIlluminanceEvent)
	subscribe(contacts, "contact", handleContactEvent)
	subscribe(locks, "lock", handleLockEvent)
	subscribe(accelerations, "acceleration", handleAccelerationEvent)
	subscribe(motions, "motion", handleMotionEvent)
	subscribe(presence, "presence", handlePresenceEvent)
	subscribe(switches, "switch", handleSwitchEvent)
	subscribe(batteries, "battery", handleBatteryEvent)
	subscribe(powers, "power", handlePowerEvent)
	subscribe(energys, "energy", handleEnergyEvent)
	subscribe(energys, "cost", handleCostEvent)
	subscribe(thermostats, "heatingSetpoint", handleHeatingSetpointEvent)
	subscribe(thermostats, "coolingSetpoint", handleCoolingSetpointEvent)
	subscribe(thermostats, "thermostatMode", handleThermostatModeEvent)
	subscribe(thermostats, "fanMode", handleFanModeEvent)
	subscribe(thermostats, "thermostatOperatingState", handleThermostatOperatingStateEvent)
	subscribe(ecobees, "dehumidifierMode", handleDehumidifierModeEvent)
	subscribe(ecobees, "equipmentStatus", handleEquipmentStatusEvent)
	subscribe(ecobees, "dehumidifierLevel", handleDehumidifierLevelEvent)
	subscribe(ecobees, "humidifierMode", handleHumidifierModeEvent)
	subscribe(ecobees, "humidifierLevel", handleHumidifierLevelEvent)
	subscribe(ecobees, "fanMinOnTime", handleFanMinOnTimeEvent)
	subscribe(ecobees, "ventilatorMode", handleVentilatorModeEvent)
	subscribe(ecobees, "ventilatorMinOnTime", handleVentilatorMinOnTimeEvent)
	subscribe(ecobees, "programScheduleName", handleProgramNameEvent)
	subscribe(ecobees, "auxHeat1RuntimeDaily", handleEcobeeDailyStats)
	subscribe(ecobees, "auxHeat2RuntimeDaily", handleEcobeeDailyStats)
	subscribe(ecobees, "auxHeat3RuntimeDaily", handleEcobeeDailyStats)
	subscribe(ecobees, "compCool1RuntimeDaily", handleEcobeeDailyStats)
	subscribe(ecobees, "compCool2RuntimeDaily", handleEcobeeDailyStats)
	subscribe(ecobees, "fanRuntimeDaily", handleEcobeeDailyStats)
	subscribe(ecobees, "humidifierRuntimeDaily", handleEcobeeDailyStats)
	subscribe(ecobees, "dehumidifierRuntimeDaily", handleEcobeeDailyStats)
	subscribe(ecobees, "ventilatorRuntimeDaily", handleEcobeeDailyStats)
	subscribe(ecobees, "presence", handlePresenceEvent)
	state.queue = []
	Integer delay = (givenInterval) ?: 5

	schedule("0 0/${delay} * * * ?", processQueue)
}

def handleTemperatureEvent(evt) {
	queueValue(evt) {
		it.toString()
	}
}

def handleHumidityEvent(evt) {
	queueValue(evt) {
		it.toString()
	}
}

def handleHeatingSetpointEvent(evt) {
	queueValue(evt) {
		it.toString()
	}
}
def handleCoolingSetpointEvent(evt) {
	queueValue(evt) {
		it.toString()
	}
}

def handleThermostatModeEvent(evt) {
	queueValue(evt) {
		it.toString()
	}
}
def handleFanModeEvent(evt) {
	queueValue(evt) {
		it.toString()
	}
}
def handleHumidifierModeEvent(evt) {
	queueValue(evt) {
		it.toString()
	}
}
def handleHumidifierLevelEvent(evt) {
	queueValue(evt) {
		it.toString()
	}
}
def handleDehumidifierModeEvent(evt) {
	queueValue(evt) {
		it.toString()
	}
}
def handleDehumidifierLevelEvent(evt) {
	queueValue(evt) {
		it.toString()
	}
}
def handleVentilatorModeEvent(evt) {
	queueValue(evt) {
		it.toString()
	}
}
def handleFanMinOnTimeEvent(evt) {
	queueValue(evt) {
		it.toString()
	}
}
def handleVentilatorMinOnTimeEvent(evt) {
	queueValue(evt) {
		it.toString()
	}
}

def handleThermostatOperatingStateEvent(evt) {
	queueValue(evt) {
		it == "idle" ? 0 : (it == 'fan only') ? 1 : (it == 'heating') ? 2 : 3
	}

}
def handleEcobeeDailyStats(evt) {
	queueValue(evt) {
		it.toString()
	}

}
def handleEquipmentStatusEvent(evt) {
	queueValue(evt) {
		it.toString()
	}
}

def handleProgramNameEvent(evt) {
	queueValue(evt) {
		it.toString()
	}
}

def handleWaterEvent(evt) {
	queueValue(evt) {
		it.toString()
	}
}
def handleSmokeEvent(evt) {
	queueValue(evt) {
		it.toString()
	}
}
def handleCarbonMonoxideEvent(evt) {
	queueValue(evt) {
		it.toString()
	}
}

def handleIlluminanceEvent(evt) {
	queueValue(evt) {
		it.toString()
	}
}

def handleLockEvent(evt) {
	queueValue(evt) {
		it == "locked" ? 1 : 0
	}
}

def handleBatteryEvent(evt) {
	queueValue(evt) {
		it.toString()
	}
}

def handleContactEvent(evt) {
	queueValue(evt) {
		it == "open" ? 1 : 0
	}
}

def handleAccelerationEvent(evt) {
	queueValue(evt) {
		it == "active" ? 1 : 0
	}
}

def handleMotionEvent(evt) {
	queueValue(evt) {
		it == "active" ? 1 : 0
	}
}

def handlePresenceEvent(evt) {
	queueValue(evt) {
		it == "present" ? 1 : 0
	}
}

def handleSwitchEvent(evt) {
	queueValue(evt) {
		it == "on" ? 1 : 0
	}
}

def handlePowerEvent(evt) {
	if (evt.value) {
		queueValue(evt) {
			it.toString()
		}
	}
}

def handleEnergyEvent(evt) {
	if (evt.value) {
		queueValue(evt) {
			it.toString()
		}
	}
}
def handleCostEvent(evt) {
	if (evt.value) {
		queueValue(evt) {
			it.toString()
		}
	}
}

private queueValue(evt, Closure convert) {
	def jsonPayload = [compId: evt.displayName, streamId: evt.name, data: convert(evt.value), time: now()]
	log.debug "Appending to queue ${jsonPayload}"

	state.queue << jsonPayload
}

def processQueue() {
	def url = "https://grovestreams.com/api/feed?api_key=${channelKey}"
	log.debug "processQueue"
	if (state.queue != []) {
		log.debug "Events: ${state.queue}"

		try {
			httpPutJson([uri: url, body: state.queue]) {
				response ->
					if (response.status != 200) {
						log.debug "GroveStreams logging failed, status = ${response.status}"
					} else {
						log.debug "GroveStreams accepted event(s)"
						state.queue = []
					}
			}
		} catch (e) {
			def errorInfo = "Error sending value: ${e}"
			log.error errorInfo
		}
	}
}