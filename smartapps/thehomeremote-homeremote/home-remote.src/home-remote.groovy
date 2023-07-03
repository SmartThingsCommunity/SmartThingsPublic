/**
 *  Home Remote
 *
 *  Copyright 2015 The Home Remote
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
 */
definition(
    name: "Home Remote",
    namespace: "thehomeremote.homeremote",
    author: "The Home Remote",
    description: "Web service that enables communication between the Home Remote app and a SmartThings hub.",
    category: "My Apps",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    oauth: [displayName: "The Home Remote", displayLink: "http://thehomeremote.com/"])


preferences {
    section() {
	input "accelerationSensors", "capability.accelerationSensor",title: "Acceleration Sensors", multiple: true, required: false
	input "alarms", "capability.alarm",title: "Alarms", multiple: true, required: false
	input "batteries", "capability.battery",title: "Batteries", multiple: true, required: false
	input "beacons", "capability.beacon",title: "Beacons", multiple: true, required: false
	input "buttonGroup", "capability.button",title: "Buttons", multiple: true, required: false
	input "carbonMonoxideDetectors", "capability.carbonMonoxideDetector",title: "CO Detectors", multiple: true, required: false
	input "colorControls", "capability.colorControl",title: "Color Lights", multiple: true, required: false
	input "contactSensors", "capability.contactSensor",title: "Contact Sensors", multiple: true, required: false
	input "doorControls", "capability.doorControl",title: "Door Controllers", multiple: true, required: false
	input "energyMeters", "capability.energyMeter",title: "Energy Meters", multiple: true, required: false
	input "illuminanceMeasurements", "capability.illuminanceMeasurement",title: "Illuminance Sensors", multiple: true, required: false
	input "imageCaptures", "capability.imageCapture",title: "Cameras", multiple: true, required: false
	input "locks", "capability.lock",title: "Locks", multiple: true, required: false
	input "mediaControllers", "capability.mediaController",title: "Media Controllers", multiple: true, required: false
	input "momentaries", "capability.momentary",title: "Momentary Buttons", multiple: true, required: false
	input "motionSensors", "capability.motionSensor",title: "Motion Sensors", multiple: true, required: false
	input "musicPlayers", "capability.musicPlayer",title: "Music Players", multiple: true, required: false
	input "powerMeters", "capability.powerMeter",title: "Power Meters", multiple: true, required: false
	input "presenceSensors", "capability.presenceSensor",title: "Presence Sensors", multiple: true, required: false
	input "relativeHumidityMeasurements", "capability.relativeHumidityMeasurement",title: "Humidity Sensors", multiple: true, required: false
	input "relaySwitches", "capability.relaySwitch",title: "Relays", multiple: true, required: false
	input "signalStrengths", "capability.signalStrength",title: "Signal Strengths", multiple: true, required: false
	input "sleepSensors", "capability.sleepSensor",title: "Sleep Sensors", multiple: true, required: false
	input "smokeDetectors", "capability.smokeDetector",title: "Smoke Detectors", multiple: true, required: false
	input "speechSyntheses", "capability.speechSynthesis",title: "Speech Syntheses", multiple: true, required: false
	input "stepSensors", "capability.stepSensor",title: "Step Sensors", multiple: true, required: false
	input "switches", "capability.switch",title: "Switches", multiple: true, required: false
	input "switchLevels", "capability.switchLevel",title: "Dimmers", multiple: true, required: false
	input "temperatureMeasurements", "capability.temperatureMeasurement",title: "Temperature Sensors", multiple: true, required: false
	input "thermostats", "capability.thermostat",title: "Thermostats", multiple: true, required: false
	input "threeAxes", "capability.threeAxis",title: "Three axis Sensors", multiple: true, required: false
	input "tones", "capability.tone",title: "Tones", multiple: true, required: false
	input "touchSensors", "capability.touchSensor",title: "Touch Sensors", multiple: true, required: false
	input "valves", "capability.valve",title: "Valves", multiple: true, required: false
	input "waterSensors", "capability.waterSensor",title: "Water Sensors", multiple: true, required: false
    }
}

mappings {
  path("/GetCurrentValues") {
    action: [
      GET: "getCurrentValues"
    ]
  }
  path("/GetCurrentValuesWithDisplayName") {
    action: [
      GET: "getCurrentValuesWithDisplayName"
    ]
  }
  path("/GetRoutines") {
    action: [
      GET: "getRoutines"
    ]
  }
  path("/ExecuteCommand") {
    action: [
      PUT: "executeCommand"
    ]
  }
  path("/ExecuteRoutine") {
    action: [
      PUT: "executeRoutine"
    ]
  }
}

def getCurrentValues() {
    def resp = []    
        
 	accelerationSensors.each {
      resp << [id: it.id, capability: "AccelerationSensor", attribute: "acceleration", value: it.currentValue("acceleration")]
    }

	alarms.each {
      resp << [id: it.id, capability: "Alarm", attribute: "alarm", value: it.currentValue("alarm")]
    }

	batteries.each {
      resp << [id: it.id, capability: "Battery", attribute: "battery", value: it.currentValue("battery")]
    }

	beacons.each {
      resp << [id: it.id, capability: "Beacon", attribute: "presence", value: it.currentValue("presence")]
    }

	buttonGroup.each {
      resp << [id: it.id, capability: "Button", attribute: "button", value: it.currentValue("button")]
    }

	carbonMonoxideDetectors.each {
      resp << [id: it.id, capability: "CarbonMonoxideDetector", attribute: "carbonMonoxide", value: it.currentValue("carbonMonoxide")]
    }

	colorControls.each {
      resp << [id: it.id, capability: "ColorControl", attribute: "hue", value: it.currentValue("hue")]
    }

	colorControls.each {
      resp << [id: it.id, capability: "ColorControl", attribute: "saturation", value: it.currentValue("saturation")]
    }

	colorControls.each {
      resp << [id: it.id, capability: "ColorControl", attribute: "color", value: it.currentValue("color")]
    }

	contactSensors.each {
      resp << [id: it.id, capability: "ContactSensor", attribute: "contact", value: it.currentValue("contact")]
    }

	doorControls.each {
      resp << [id: it.id, capability: "DoorControl", attribute: "door", value: it.currentValue("door")]
    }

	energyMeters.each {
      resp << [id: it.id, capability: "EnergyMeter", attribute: "energy", value: it.currentValue("energy")]
    }

	illuminanceMeasurements.each {
      resp << [id: it.id, capability: "IlluminanceMeasurement", attribute: "illuminance", value: it.currentValue("illuminance")]
    }

	imageCaptures.each {
      resp << [id: it.id, capability: "ImageCapture", attribute: "image", value: it.currentValue("image")]
    }

	locks.each {
      resp << [id: it.id, capability: "Lock", attribute: "lock", value: it.currentValue("lock")]
    }

	mediaControllers.each {
      resp << [id: it.id, capability: "MediaController", attribute: "activities", value: it.currentValue("activities")]
    }

	mediaControllers.each {
      resp << [id: it.id, capability: "MediaController", attribute: "currentActivity", value: it.currentValue("currentActivity")]
    }

	motionSensors.each {
      resp << [id: it.id, capability: "MotionSensor", attribute: "motion", value: it.currentValue("motion")]
    }

	musicPlayers.each {
      resp << [id: it.id, capability: "MusicPlayer", attribute: "status", value: it.currentValue("status")]
    }

	musicPlayers.each {
      resp << [id: it.id, capability: "MusicPlayer", attribute: "level", value: it.currentValue("level")]
    }

	musicPlayers.each {
      resp << [id: it.id, capability: "MusicPlayer", attribute: "trackDescription", value: it.currentValue("trackDescription")]
    }

	musicPlayers.each {
      resp << [id: it.id, capability: "MusicPlayer", attribute: "trackData", value: it.currentValue("trackData")]
    }

	musicPlayers.each {
      resp << [id: it.id, capability: "MusicPlayer", attribute: "mute", value: it.currentValue("mute")]
    }

	powerMeters.each {
      resp << [id: it.id, capability: "PowerMeter", attribute: "power", value: it.currentValue("power")]
    }

	presenceSensors.each {
      resp << [id: it.id, capability: "PresenceSensor", attribute: "presence", value: it.currentValue("presence")]
    }

	relativeHumidityMeasurements.each {
      resp << [id: it.id, capability: "RelativeHumidityMeasurement", attribute: "humidity", value: it.currentValue("humidity")]
    }

	relaySwitches.each {
      resp << [id: it.id, capability: "RelaySwitch", attribute: "switch", value: it.currentValue("switch")]
    }

	signalStrengths.each {
      resp << [id: it.id, capability: "SignalStrength", attribute: "lqi", value: it.currentValue("lqi")]
    }

	signalStrengths.each {
      resp << [id: it.id, capability: "SignalStrength", attribute: "rssi", value: it.currentValue("rssi")]
    }

	sleepSensors.each {
      resp << [id: it.id, capability: "SleepSensor", attribute: "sleeping", value: it.currentValue("sleeping")]
    }

	smokeDetectors.each {
      resp << [id: it.id, capability: "SmokeDetector", attribute: "smoke", value: it.currentValue("smoke")]
    }

	stepSensors.each {
      resp << [id: it.id, capability: "StepSensor", attribute: "steps", value: it.currentValue("steps")]
    }

	stepSensors.each {
      resp << [id: it.id, capability: "StepSensor", attribute: "goal", value: it.currentValue("goal")]
    }

	switches.each {
      resp << [id: it.id, capability: "Switch", attribute: "switch", value: it.currentValue("switch")]
    }

	switchLevels.each {
      resp << [id: it.id, capability: "SwitchLevel", attribute: "level", value: it.currentValue("level")]
    }

	temperatureMeasurements.each {
      resp << [id: it.id, capability: "TemperatureMeasurement", attribute: "temperature", value: it.currentValue("temperature")]
    }

	thermostats.each {
      resp << [id: it.id, capability: "Thermostat", attribute: "temperature", value: it.currentValue("temperature")]
    }

	thermostats.each {
      resp << [id: it.id, capability: "Thermostat", attribute: "heatingSetpoint", value: it.currentValue("heatingSetpoint")]
    }

	thermostats.each {
      resp << [id: it.id, capability: "Thermostat", attribute: "coolingSetpoint", value: it.currentValue("coolingSetpoint")]
    }

	//Commented out on 7/23/2016.   This randomly started throwing number format exceptions with either my ecobee or Lyric thermostat.
	//thermostats.each {
    //  resp << [id: it.id, capability: "Thermostat", attribute: "thermostatSetpoint", value: it.currentValue("thermostatSetpoint")]
    //}

	thermostats.each {
      resp << [id: it.id, capability: "Thermostat", attribute: "thermostatMode", value: it.currentValue("thermostatMode")]
    }

	thermostats.each {
      resp << [id: it.id, capability: "Thermostat", attribute: "thermostatFanMode", value: it.currentValue("thermostatFanMode")]
    }

	thermostats.each {
      resp << [id: it.id, capability: "Thermostat", attribute: "thermostatOperatingState", value: it.currentValue("thermostatOperatingState")]
    }

	threeAxes.each {
      resp << [id: it.id, capability: "ThreeAxis", attribute: "threeAxis", value: it.currentValue("threeAxis")]
    }

	touchSensors.each {
      resp << [id: it.id, capability: "TouchSensor", attribute: "touch", value: it.currentValue("touch")]
    }

	valves.each {
      resp << [id: it.id, capability: "Valve", attribute: "contact", value: it.currentValue("contact")]
    }

	waterSensors.each {
      resp << [id: it.id, capability: "WaterSensor", attribute: "water", value: it.currentValue("water")]
    }
   
   	//resp << [id: 0, capability: "Heartbeat", attribute: "heartbeat", value: String.valueOf(state.heartbeat)]
    
    state.heartbeat = !state.heartbeat
    
    return resp
}

def getCurrentValuesWithDisplayName() {
    def resp = []    
        
 	accelerationSensors.each {
      resp << [id: it.id, displayName: it.displayName, capability: "AccelerationSensor", attribute: "acceleration", value: it.currentValue("acceleration")]
    }

	alarms.each {
      resp << [id: it.id, displayName: it.displayName, capability: "Alarm", attribute: "alarm", value: it.currentValue("alarm")]
    }

	batteries.each {
      resp << [id: it.id, displayName: it.displayName, capability: "Battery", attribute: "battery", value: it.currentValue("battery")]
    }

	beacons.each {
      resp << [id: it.id, displayName: it.displayName, capability: "Beacon", attribute: "presence", value: it.currentValue("presence")]
    }

	buttonGroup.each {
      resp << [id: it.id, displayName: it.displayName, capability: "Button", attribute: "button", value: it.currentValue("button")]
    }

	carbonMonoxideDetectors.each {
      resp << [id: it.id, displayName: it.displayName, capability: "CarbonMonoxideDetector", attribute: "carbonMonoxide", value: it.currentValue("carbonMonoxide")]
    }

	colorControls.each {
      resp << [id: it.id, displayName: it.displayName, capability: "ColorControl", attribute: "hue", value: it.currentValue("hue")]
    }

	colorControls.each {
      resp << [id: it.id, displayName: it.displayName, capability: "ColorControl", attribute: "saturation", value: it.currentValue("saturation")]
    }

	colorControls.each {
      resp << [id: it.id, displayName: it.displayName, capability: "ColorControl", attribute: "color", value: it.currentValue("color")]
    }

	contactSensors.each {
      resp << [id: it.id, displayName: it.displayName, capability: "ContactSensor", attribute: "contact", value: it.currentValue("contact")]
    }

	doorControls.each {
      resp << [id: it.id, displayName: it.displayName, capability: "DoorControl", attribute: "door", value: it.currentValue("door")]
    }

	energyMeters.each {
      resp << [id: it.id, displayName: it.displayName, capability: "EnergyMeter", attribute: "energy", value: it.currentValue("energy")]
    }

	illuminanceMeasurements.each {
      resp << [id: it.id, displayName: it.displayName, capability: "IlluminanceMeasurement", attribute: "illuminance", value: it.currentValue("illuminance")]
    }

	imageCaptures.each {
      resp << [id: it.id, displayName: it.displayName, capability: "ImageCapture", attribute: "image", value: it.currentValue("image")]
    }

	locks.each {
      resp << [id: it.id, displayName: it.displayName, capability: "Lock", attribute: "lock", value: it.currentValue("lock")]
    }

	mediaControllers.each {
      resp << [id: it.id, displayName: it.displayName, capability: "MediaController", attribute: "activities", value: it.currentValue("activities")]
    }

	mediaControllers.each {
      resp << [id: it.id, displayName: it.displayName, capability: "MediaController", attribute: "currentActivity", value: it.currentValue("currentActivity")]
    }

	motionSensors.each {
      resp << [id: it.id, displayName: it.displayName, capability: "MotionSensor", attribute: "motion", value: it.currentValue("motion")]
    }

	musicPlayers.each {
      resp << [id: it.id, displayName: it.displayName, capability: "MusicPlayer", attribute: "status", value: it.currentValue("status")]
    }

	musicPlayers.each {
      resp << [id: it.id, displayName: it.displayName, capability: "MusicPlayer", attribute: "level", value: it.currentValue("level")]
    }

	musicPlayers.each {
      resp << [id: it.id, displayName: it.displayName, capability: "MusicPlayer", attribute: "trackDescription", value: it.currentValue("trackDescription")]
    }

	musicPlayers.each {
      resp << [id: it.id, displayName: it.displayName, capability: "MusicPlayer", attribute: "trackData", value: it.currentValue("trackData")]
    }

	musicPlayers.each {
      resp << [id: it.id, displayName: it.displayName, capability: "MusicPlayer", attribute: "mute", value: it.currentValue("mute")]
    }

	powerMeters.each {
      resp << [id: it.id, displayName: it.displayName, capability: "PowerMeter", attribute: "power", value: it.currentValue("power")]
    }

	presenceSensors.each {
      resp << [id: it.id, displayName: it.displayName, capability: "PresenceSensor", attribute: "presence", value: it.currentValue("presence")]
    }

	relativeHumidityMeasurements.each {
      resp << [id: it.id, displayName: it.displayName, capability: "RelativeHumidityMeasurement", attribute: "humidity", value: it.currentValue("humidity")]
    }

	relaySwitches.each {
      resp << [id: it.id, displayName: it.displayName, capability: "RelaySwitch", attribute: "switch", value: it.currentValue("switch")]
    }

	signalStrengths.each {
      resp << [id: it.id, displayName: it.displayName, capability: "SignalStrength", attribute: "lqi", value: it.currentValue("lqi")]
    }

	signalStrengths.each {
      resp << [id: it.id, displayName: it.displayName, capability: "SignalStrength", attribute: "rssi", value: it.currentValue("rssi")]
    }

	sleepSensors.each {
      resp << [id: it.id, displayName: it.displayName, capability: "SleepSensor", attribute: "sleeping", value: it.currentValue("sleeping")]
    }

	smokeDetectors.each {
      resp << [id: it.id, displayName: it.displayName, capability: "SmokeDetector", attribute: "smoke", value: it.currentValue("smoke")]
    }

	stepSensors.each {
      resp << [id: it.id, displayName: it.displayName, capability: "StepSensor", attribute: "steps", value: it.currentValue("steps")]
    }

	stepSensors.each {
      resp << [id: it.id, displayName: it.displayName, capability: "StepSensor", attribute: "goal", value: it.currentValue("goal")]
    }

	switches.each {
      resp << [id: it.id, displayName: it.displayName, capability: "Switch", attribute: "switch", value: it.currentValue("switch")]
    }

	switchLevels.each {
      resp << [id: it.id, displayName: it.displayName, capability: "SwitchLevel", attribute: "level", value: it.currentValue("level")]
    }

	temperatureMeasurements.each {
      resp << [id: it.id, displayName: it.displayName, capability: "TemperatureMeasurement", attribute: "temperature", value: it.currentValue("temperature")]
    }

	thermostats.each {
      resp << [id: it.id, displayName: it.displayName, capability: "Thermostat", attribute: "temperature", value: it.currentValue("temperature")]
    }

	thermostats.each {
      resp << [id: it.id, displayName: it.displayName, capability: "Thermostat", attribute: "heatingSetpoint", value: it.currentValue("heatingSetpoint")]
    }

	thermostats.each {
      resp << [id: it.id, displayName: it.displayName, capability: "Thermostat", attribute: "coolingSetpoint", value: it.currentValue("coolingSetpoint")]
    }

	//Commented out on 7/23/2016.   This randomly started throwing number format exceptions with either my ecobee or Lyric thermostat.
	//thermostats.each {
    //  resp << [id: it.id, displayName: it.displayName, capability: "Thermostat", attribute: "thermostatSetpoint", value: it.currentValue("thermostatSetpoint")]
    //}

	thermostats.each {
      resp << [id: it.id, displayName: it.displayName, capability: "Thermostat", attribute: "thermostatMode", value: it.currentValue("thermostatMode")]
    }

	thermostats.each {
      resp << [id: it.id, displayName: it.displayName, capability: "Thermostat", attribute: "thermostatFanMode", value: it.currentValue("thermostatFanMode")]
    }

	thermostats.each {
      resp << [id: it.id, displayName: it.displayName, capability: "Thermostat", attribute: "thermostatOperatingState", value: it.currentValue("thermostatOperatingState")]
    }

	threeAxes.each {
      resp << [id: it.id, displayName: it.displayName, capability: "ThreeAxis", attribute: "threeAxis", value: it.currentValue("threeAxis")]
    }

	touchSensors.each {
      resp << [id: it.id, displayName: it.displayName, capability: "TouchSensor", attribute: "touch", value: it.currentValue("touch")]
    }

	valves.each {
      resp << [id: it.id, displayName: it.displayName, capability: "Valve", attribute: "contact", value: it.currentValue("contact")]
    }

	waterSensors.each {
      resp << [id: it.id, displayName: it.displayName, capability: "WaterSensor", attribute: "water", value: it.currentValue("water")]
    }
   
    momentaries.each {
      resp << [id: it.id, displayName: it.displayName, capability: "Momentary", attribute: "", value: ""]
    }
   
    //resp << [id: 0, displayName: "Heartbeat", capability: "Heartbeat", attribute: "heartbeat", value: state.heartbeat]
    
    return resp
}

def getDevices(capability){

	def result

	switch (capability) {
    	case "Alarm":
        	result = alarms
        	break
    	case "ColorControl":
        	result = colorControls
        	break
    	case "DoorControl":
        	result = doorControls
        	break
        case "ImageCapture":
        	result = imageCaptures
        	break
    	case "Lock":
        	result = locks
        	break
        case "MediaController":
        	result = mediaControllers
        	break
    	case "Momentary":
        	result = momentaries
        	break
        case "MusicPlayer":
        	result = musicPlayers
        	break
        case "RelaySwitch":
        	result = relaySwitches
        	break
    	case "SpeechSynthesis":
        	result = speechSyntheses
        	break
    	case "Switch":
        	result = switches
        	break
        case "SwitchLevel":
        	result = switchLevels
        	break
    	case "Thermostat":
        	result = thermostats
        	break
		case "ThermostatCoolingSetpoint":
        	result = thermostatCoolingSetpoints
        	break
    	case "ThermostatFanMode":
        	result = thermostatFanModes
        	break
        case "ThermostatHeatingSetpoint":
        	result = thermostatHeatingSetpoints
        	break
    	case "ThermostatMode":
        	result = thermostatModes
        	break
    	case "Tone":
        	result = tones
        	break
        case "Valve":
        	result = valves
        	break     
    	default:
        	result = valves
	}
	return result
}

def getDoorControlCommand(value){
	def result
	switch (value) {
        case "closed":
        	result = "close"
        	break
    	case "open":
        	result = "open"
        	break
    	default:
        	result = value
	}    
	return result
}

def getLockCommand(value){
	def result
	switch (value) {
        case "locked":
        	result = "lock"
        	break
    	case "unlocked":
        	result = "unlock"
        	break
    	default:
        	result = value
	}    
	return result
}

def getMuteCommand(value){
	def result
	switch (value) {
        case "muted":
        	result = "mute"
        	break
    	case "unmuted":
        	result = "unmute"
        	break
    	default:
        	result = value
	}    
	return result
}

def getContactCommand(value){
	def result
	switch (value) {
        case "closed":
        	result = "close"
        	break
    	case "open":
        	result = "open"
        	break
    	default:
        	result = value
	}    
	return result
}

def getRoutines() {
	return location.helloHome?.getPhrases()*.label
}

def getThermostatFanModeCommand(value){
	def result
	switch (value) {
        case "on":
        	result = "fanOn"
        	break
    	case "auto":
        	result = "fanAuto"
        	break
    	case "circulate":
        	result = "fanCirculate"
        	break
    	default:
        	result = value
	}    
	return result
}

void executeCommand() {    
    def deviceId = request.JSON?.deviceId
    def capability = request.JSON?.capability
    def attribute = request.JSON?.attribute 
    def value = request.JSON?.value    
    if (deviceId) {
		def devices = getDevices(capability)
        def command
        def valueIsParameter = false
        def valueIsInteger = false
        switch (attribute) {    	       	
        	case "hue":
        		command = "setHue"
                valueIsParameter = true
                valueIsInteger = true
        		break
    		case "saturation":
        		command = "setSaturation"
                valueIsParameter = true
                valueIsInteger = true
        		break
    		case "color":
        		command = "setColor"
                def rgb = hexToRgb(value)
                def hsv = rgbToHSV(rgb)
                value = [hue:hsv.h.toInteger(), saturation:hsv.s.toInteger()]
                valueIsParameter = true				
        		break
			case "level":
        		command = "setLevel"
                valueIsParameter = true
                valueIsInteger = true
        		break            
        	case "heatingSetpoint":
        		command = "setHeatingSetpoint"
                valueIsParameter = true
        		break    
       		case "coolingSetpoint":
        		command = "setCoolingSetpoint"
                valueIsParameter = true
        		break
          	case "currentActivity":
        		command = "startActivity"
                valueIsParameter = true
        		break  
 	  		case "door":
        		command = getDoorControlCommand(value)
        		break    
      		case "lock":
        		command = getLockCommand(value)
        		break           
      		case "mute":
        		command = getMuteCommand(value)
        		break         
      		case "thermostatFanMode":
        		command = getThermostatFanModeCommand(value)
        		break         
       		case "thermostatMode":
        		if (value == "emergency heat") {
                	command = "emergencyHeat"
           		}
                else
                {
                    command = value
                }
        		break     
      		case "contact":
        		command = getContactCommand(value)
        		break
    		default:
        		command = value
		}
        devices.each {
        	if (it.id == deviceId) {
                // check that the device supports the specified command
                // If not, return an error using httpError, providing a HTTP status code.
            	if (!it.hasCommand(command)) {
                	httpError(501, "$command is not a valid command for the device")
            	}                
                if(valueIsParameter){
                	if(valueIsInteger){
                		it."$command"(value as int)
                	}
                	else{
                		it."$command"(value)
                	}   
                }
                else{
                	it."$command"()
                }   
           }       
        }
    }
}

void executeRoutine() {   
	def routine = request.JSON?.routine
    location.helloHome?.execute(routine)
}

def rgbToHSV(rgb) {
	def r = rgb.r / 255
    def g = rgb.g / 255
    def b = rgb.b / 255
    def h = 0
    def s = 0
    def v = 0
    
    def var_min = [r,g,b].min()
    def var_max = [r,g,b].max()
    def del_max = var_max - var_min
    
    v = var_max
    
    if (del_max == 0) {
            h = 0
            s = 0
    } else {
        def del_r = (((var_max - r) / 6) + (del_max / 2)) / del_max
        def del_g = (((var_max - g) / 6) + (del_max / 2)) / del_max
        def del_b = (((var_max - b) / 6) + (del_max / 2)) / del_max 
     	
        s = del_max / var_max;
        
        if (r == var_max) { h = del_b - del_g } 
        else if (g == var_max) { h = (1 / 3) + del_r - del_b } 
        else { h = (2 / 3) + del_g - del_r }
        
		if (h < 0) { h += 1 }
        if (h > 1) { h -= 1 }
	}
    def hsv = [:]    
    hsv = [h: h * 100, s: s * 100, v: v]
    
    hsv
}

def hexToRgb(colorHex) {
	def rrInt = Integer.parseInt(colorHex.substring(1,3),16)
    def ggInt = Integer.parseInt(colorHex.substring(3,5),16)
    def bbInt = Integer.parseInt(colorHex.substring(5,7),16)
    
    def colorData = [:]
    colorData = [r: rrInt, g: ggInt, b: bbInt]
    colorData
}

def installed() {
	state.heartbeat = false
}

def updated() {
	state.heartbeat = false
}