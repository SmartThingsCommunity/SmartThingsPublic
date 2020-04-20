/**
 *  Christine 
 *
 *  Copyright 2017 Christine Pesonen
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
metadata {
	definition (name: "Christine ", namespace: "CAP187", author: "Christine Pesonen") {
		capability "Acceleration Sensor"
		capability "Actuator"
		capability "Air Conditioner Mode"
		capability "Air Quality Sensor"
		capability "Alarm"
		capability "Alarm System"
		capability "Alarm System Arm Only"
		capability "Astronomical Data"
		capability "Audio Mute"
		capability "Audio Notification"
		capability "Audio Track Addressing"
		capability "Audio Track Data"
		capability "Audio Volume"
		capability "Battery"
		capability "Beacon"
		capability "Bridge"
		capability "Buffered Video Capture"
		capability "Bulb"
		capability "Button"
		capability "Bypassable"
		capability "Carbon Dioxide Measurement"
		capability "Carbon Monoxide Detector"
		capability "Color Control"
		capability "Color Temperature"
		capability "Configuration"
		capability "Consumable"
		capability "Contact Sensor"
		capability "Dishwasher Mode"
		capability "Dishwasher Operating State"
		capability "Dishwasher Operational State"
		capability "Door Control"
		capability "Dryer Mode"
		capability "Dryer Operating State"
		capability "Dryer Operational State"
		capability "Dust Sensor"
		capability "Energy Meter"
		capability "Estimated Time Of Arrival"
		capability "Execute"
		capability "Fan Speed"
		capability "Filter State"
		capability "Filter Status"
		capability "Garage Door Control"
		capability "Geolocation"
		capability "Health Check"
		capability "Holdable Button"
		capability "Illuminance Measurement"
		capability "Image Capture"
		capability "Indicator"
		capability "Infrared Level"
		capability "Light"
		capability "Location Mode"
		capability "Lock"
		capability "Lock Codes"
		capability "Lock Only"
		capability "Media Controller"
		capability "Media Input Source"
		capability "Media Playback"
		capability "Media Playback Repeat"
		capability "Media Playback Shuffle"
		capability "Media Track Control"
		capability "Momentary"
		capability "Motion Sensor"
		capability "Music Player"
		capability "Network Meter"
		capability "Notification"
		capability "Ocf"
		capability "Odor Sensor"
		capability "Outlet"
		capability "Oven Mode"
		capability "Oven Operating State"
		capability "Oven Operational State"
		capability "Oven Setpoint"
		capability "Panic Alarm"
		capability "pH Measurement"
		capability "Polling"
		capability "Power"
		capability "Power Meter"
		capability "Power Source"
		capability "Presence Sensor"
		capability "Rapid Cooling"
		capability "Refresh"
		capability "Refrigeration"
		capability "Refrigeration Setpoint"
		capability "Relative Humidity Measurement"
		capability "Relay Switch"
		capability "Remote Control Status"
		capability "Robot Cleaner Cleaning Mode"
		capability "Robot Cleaner Movement"
		capability "Robot Cleaner State"
		capability "Robot Cleaner Turbo Mode"
		capability "Samsung TV"
		capability "Security System"
		capability "Sensor"
		capability "Shock Sensor"
		capability "Signal Strength"
		capability "Sleep Sensor"
		capability "Smoke Detector"
		capability "Sound Pressure Level"
		capability "Sound Sensor"
		capability "Speech Recognition"
		capability "Speech Synthesis"
		capability "Step Sensor"
		capability "Switch"
		capability "Switch Level"
		capability "Tamper Alert"
		capability "Temperature Alarm"
		capability "Temperature Measurement"
		capability "Test Capability"
		capability "Thermostat"
		capability "Thermostat Cooling Setpoint"
		capability "Thermostat Fan Mode"
		capability "Thermostat Heating Setpoint"
		capability "Thermostat Mode"
		capability "Thermostat Operating State"
		capability "Thermostat Schedule"
		capability "Thermostat Setpoint"
		capability "Three Axis"
		capability "Timed Session"
		capability "Tone"
		capability "Touch Sensor"
		capability "TV"
		capability "TV Channel"
		capability "Ultraviolet Index"
		capability "Valve"
		capability "Video Camera"
		capability "Video Capture"
		capability "Video Stream"
		capability "Voltage Measurement"
		capability "Washer Mode"
		capability "Washer Operating State"
		capability "Washer Operational State"
		capability "Water Sensor"
		capability "Window Shade"
		capability "Zw Multichannel"
	}


	simulator {
		// TODO: define status and reply messages here
	}

	tiles {
		// TODO: define your main and details tiles here
	}
}

// parse events into attributes
def parse(String description) {
	log.debug "Parsing '${description}'"
	// TODO: handle 'acceleration' attribute
	// TODO: handle 'airConditionerMode' attribute
	// TODO: handle 'airQuality' attribute
	// TODO: handle 'alarm' attribute
	// TODO: handle 'alarmSystemStatus' attribute
	// TODO: handle 'alarmSystemStatus' attribute
	// TODO: handle 'sunrise' attribute
	// TODO: handle 'sunset' attribute
	// TODO: handle 'sunriseTime' attribute
	// TODO: handle 'sunsetTime' attribute
	// TODO: handle 'mute' attribute
	// TODO: handle 'audioTrackData' attribute
	// TODO: handle 'volume' attribute
	// TODO: handle 'battery' attribute
	// TODO: handle 'presence' attribute
	// TODO: handle 'clip' attribute
	// TODO: handle 'switch' attribute
	// TODO: handle 'button' attribute
	// TODO: handle 'numberOfButtons' attribute
	// TODO: handle 'bypassStatus' attribute
	// TODO: handle 'carbonDioxide' attribute
	// TODO: handle 'carbonMonoxide' attribute
	// TODO: handle 'hue' attribute
	// TODO: handle 'saturation' attribute
	// TODO: handle 'color' attribute
	// TODO: handle 'colorTemperature' attribute
	// TODO: handle 'consumableStatus' attribute
	// TODO: handle 'contact' attribute
	// TODO: handle 'dishwasherMode' attribute
	// TODO: handle 'machineState' attribute
	// TODO: handle 'supportedMachineStates' attribute
	// TODO: handle 'dishwasherJobState' attribute
	// TODO: handle 'remainingTime' attribute
	// TODO: handle 'machineState' attribute
	// TODO: handle 'supportedMachineStates' attribute
	// TODO: handle 'dishwasherJobState' attribute
	// TODO: handle 'remainingTime' attribute
	// TODO: handle 'door' attribute
	// TODO: handle 'dryerMode' attribute
	// TODO: handle 'machineState' attribute
	// TODO: handle 'supportedMachineStates' attribute
	// TODO: handle 'dryerJobState' attribute
	// TODO: handle 'remainingTime' attribute
	// TODO: handle 'machineState' attribute
	// TODO: handle 'supportedMachineStates' attribute
	// TODO: handle 'dryerJobState' attribute
	// TODO: handle 'remainingTime' attribute
	// TODO: handle 'dustLevel' attribute
	// TODO: handle 'fineDustLevel' attribute
	// TODO: handle 'energy' attribute
	// TODO: handle 'eta' attribute
	// TODO: handle 'data' attribute
	// TODO: handle 'fanSpeed' attribute
	// TODO: handle 'filterLifeRemaining' attribute
	// TODO: handle 'filterStatus' attribute
	// TODO: handle 'door' attribute
	// TODO: handle 'latitude' attribute
	// TODO: handle 'longitude' attribute
	// TODO: handle 'method' attribute
	// TODO: handle 'accuracy' attribute
	// TODO: handle 'altitudeAccuracy' attribute
	// TODO: handle 'heading' attribute
	// TODO: handle 'speed' attribute
	// TODO: handle 'lastUpdateTime' attribute
	// TODO: handle 'checkInterval' attribute
	// TODO: handle 'DeviceWatch-DeviceStatus' attribute
	// TODO: handle 'button' attribute
	// TODO: handle 'numberOfButtons' attribute
	// TODO: handle 'illuminance' attribute
	// TODO: handle 'image' attribute
	// TODO: handle 'indicatorStatus' attribute
	// TODO: handle 'infraredLevel' attribute
	// TODO: handle 'switch' attribute
	// TODO: handle 'mode' attribute
	// TODO: handle 'lock' attribute
	// TODO: handle 'lock' attribute
	// TODO: handle 'codeChanged' attribute
	// TODO: handle 'lockCodes' attribute
	// TODO: handle 'scanCodes' attribute
	// TODO: handle 'codeLength' attribute
	// TODO: handle 'maxCodes' attribute
	// TODO: handle 'maxCodeLength' attribute
	// TODO: handle 'minCodeLength' attribute
	// TODO: handle 'codeReport' attribute
	// TODO: handle 'lock' attribute
	// TODO: handle 'activities' attribute
	// TODO: handle 'currentActivity' attribute
	// TODO: handle 'inputSource' attribute
	// TODO: handle 'supportedInputSources' attribute
	// TODO: handle 'level' attribute
	// TODO: handle 'playbackStatus' attribute
	// TODO: handle 'playbackRepeatMode' attribute
	// TODO: handle 'playbackShuffle' attribute
	// TODO: handle 'motion' attribute
	// TODO: handle 'status' attribute
	// TODO: handle 'level' attribute
	// TODO: handle 'trackDescription' attribute
	// TODO: handle 'trackData' attribute
	// TODO: handle 'mute' attribute
	// TODO: handle 'uplinkSpeed' attribute
	// TODO: handle 'downlinkSpeed' attribute
	// TODO: handle 'n' attribute
	// TODO: handle 'icv' attribute
	// TODO: handle 'dmv' attribute
	// TODO: handle 'di' attribute
	// TODO: handle 'pi' attribute
	// TODO: handle 'mnmn' attribute
	// TODO: handle 'mnml' attribute
	// TODO: handle 'mnmo' attribute
	// TODO: handle 'mndt' attribute
	// TODO: handle 'mnpv' attribute
	// TODO: handle 'mnos' attribute
	// TODO: handle 'mnhw' attribute
	// TODO: handle 'mnfv' attribute
	// TODO: handle 'mnsl' attribute
	// TODO: handle 'st' attribute
	// TODO: handle 'vid' attribute
	// TODO: handle 'odorLevel' attribute
	// TODO: handle 'switch' attribute
	// TODO: handle 'ovenMode' attribute
	// TODO: handle 'machineState' attribute
	// TODO: handle 'supportedMachineStates' attribute
	// TODO: handle 'ovenJobState' attribute
	// TODO: handle 'remainingTime' attribute
	// TODO: handle 'operationTime' attribute
	// TODO: handle 'machineState' attribute
	// TODO: handle 'supportedMachineStates' attribute
	// TODO: handle 'ovenJobState' attribute
	// TODO: handle 'remainingTime' attribute
	// TODO: handle 'operationTime' attribute
	// TODO: handle 'ovenSetpoint' attribute
	// TODO: handle 'panicAlarm' attribute
	// TODO: handle 'pH' attribute
	// TODO: handle 'powerSource' attribute
	// TODO: handle 'power' attribute
	// TODO: handle 'powerSource' attribute
	// TODO: handle 'presence' attribute
	// TODO: handle 'rapidCooling' attribute
	// TODO: handle 'rapidCooling' attribute
	// TODO: handle 'rapidFreezing' attribute
	// TODO: handle 'defrost' attribute
	// TODO: handle 'refrigerationSetpoint' attribute
	// TODO: handle 'humidity' attribute
	// TODO: handle 'switch' attribute
	// TODO: handle 'remoteControlEnabled' attribute
	// TODO: handle 'robotCleanerCleaningMode' attribute
	// TODO: handle 'robotCleanerMovement' attribute
	// TODO: handle 'supportedRobotCleanerStates' attribute
	// TODO: handle 'robotCleanerCleaningState' attribute
	// TODO: handle 'robotCleanerControlState' attribute
	// TODO: handle 'robotCleanerTurboState' attribute
	// TODO: handle 'robotCleanerTurboMode' attribute
	// TODO: handle 'volume' attribute
	// TODO: handle 'mute' attribute
	// TODO: handle 'pictureMode' attribute
	// TODO: handle 'soundMode' attribute
	// TODO: handle 'switch' attribute
	// TODO: handle 'messageButton' attribute
	// TODO: handle 'securitySystemStatus' attribute
	// TODO: handle 'alarm' attribute
	// TODO: handle 'shock' attribute
	// TODO: handle 'lqi' attribute
	// TODO: handle 'rssi' attribute
	// TODO: handle 'sleeping' attribute
	// TODO: handle 'smoke' attribute
	// TODO: handle 'soundPressureLevel' attribute
	// TODO: handle 'sound' attribute
	// TODO: handle 'phraseSpoken' attribute
	// TODO: handle 'steps' attribute
	// TODO: handle 'goal' attribute
	// TODO: handle 'switch' attribute
	// TODO: handle 'level' attribute
	// TODO: handle 'tamper' attribute
	// TODO: handle 'temperatureAlarm' attribute
	// TODO: handle 'temperature' attribute
	// TODO: handle 'temperature' attribute
	// TODO: handle 'heatingSetpoint' attribute
	// TODO: handle 'coolingSetpoint' attribute
	// TODO: handle 'thermostatSetpoint' attribute
	// TODO: handle 'thermostatMode' attribute
	// TODO: handle 'thermostatFanMode' attribute
	// TODO: handle 'thermostatOperatingState' attribute
	// TODO: handle 'schedule' attribute
	// TODO: handle 'coolingSetpointRange' attribute
	// TODO: handle 'heatingSetpointRange' attribute
	// TODO: handle 'supportedThermostatFanModes' attribute
	// TODO: handle 'supportedThermostatModes' attribute
	// TODO: handle 'thermostatSetpointRange' attribute
	// TODO: handle 'coolingSetpoint' attribute
	// TODO: handle 'coolingSetpointRange' attribute
	// TODO: handle 'thermostatFanMode' attribute
	// TODO: handle 'supportedThermostatFanModes' attribute
	// TODO: handle 'heatingSetpoint' attribute
	// TODO: handle 'heatingSetpointRange' attribute
	// TODO: handle 'thermostatMode' attribute
	// TODO: handle 'supportedThermostatModes' attribute
	// TODO: handle 'thermostatOperatingState' attribute
	// TODO: handle 'schedule' attribute
	// TODO: handle 'thermostatSetpoint' attribute
	// TODO: handle 'thermostatSetpointRange' attribute
	// TODO: handle 'threeAxis' attribute
	// TODO: handle 'sessionStatus' attribute
	// TODO: handle 'timeRemaining' attribute
	// TODO: handle 'touch' attribute
	// TODO: handle 'volume' attribute
	// TODO: handle 'channel' attribute
	// TODO: handle 'power' attribute
	// TODO: handle 'picture' attribute
	// TODO: handle 'sound' attribute
	// TODO: handle 'movieMode' attribute
	// TODO: handle 'tvChannel' attribute
	// TODO: handle 'ultravioletIndex' attribute
	// TODO: handle 'contact' attribute
	// TODO: handle 'valve' attribute
	// TODO: handle 'camera' attribute
	// TODO: handle 'statusMessage' attribute
	// TODO: handle 'mute' attribute
	// TODO: handle 'settings' attribute
	// TODO: handle 'clip' attribute
	// TODO: handle 'stream' attribute
	// TODO: handle 'stream' attribute
	// TODO: handle 'voltage' attribute
	// TODO: handle 'ovenMode' attribute
	// TODO: handle 'machineState' attribute
	// TODO: handle 'supportedMachineStates' attribute
	// TODO: handle 'washerJobState' attribute
	// TODO: handle 'remainingTime' attribute
	// TODO: handle 'machineState' attribute
	// TODO: handle 'supportedMachineStates' attribute
	// TODO: handle 'washerJobState' attribute
	// TODO: handle 'remainingTime' attribute
	// TODO: handle 'water' attribute
	// TODO: handle 'windowShade' attribute
	// TODO: handle 'epEvent' attribute
	// TODO: handle 'epInfo' attribute

}

// handle commands
def setAirConditionerMode() {
	log.debug "Executing 'setAirConditionerMode'"
	// TODO: handle 'setAirConditionerMode' command
}

def off() {
	log.debug "Executing 'off'"
	// TODO: handle 'off' command
}

def strobe() {
	log.debug "Executing 'strobe'"
	// TODO: handle 'strobe' command
}

def siren() {
	log.debug "Executing 'siren'"
	// TODO: handle 'siren' command
}

def both() {
	log.debug "Executing 'both'"
	// TODO: handle 'both' command
}

def sendEvent(alarmSystemStatus,off)() {
	log.debug "Executing 'sendEvent(alarmSystemStatus,off)'"
	// TODO: handle 'sendEvent(alarmSystemStatus,off)' command
}

def sendEvent(alarmSystemStatus,stay)() {
	log.debug "Executing 'sendEvent(alarmSystemStatus,stay)'"
	// TODO: handle 'sendEvent(alarmSystemStatus,stay)' command
}

def sendEvent(alarmSystemStatus,away)() {
	log.debug "Executing 'sendEvent(alarmSystemStatus,away)'"
	// TODO: handle 'sendEvent(alarmSystemStatus,away)' command
}

def sendEvent(alarmSystemStatus,stay)() {
	log.debug "Executing 'sendEvent(alarmSystemStatus,stay)'"
	// TODO: handle 'sendEvent(alarmSystemStatus,stay)' command
}

def sendEvent(alarmSystemStatus,away)() {
	log.debug "Executing 'sendEvent(alarmSystemStatus,away)'"
	// TODO: handle 'sendEvent(alarmSystemStatus,away)' command
}

def sendEvent(sunrise)() {
	log.debug "Executing 'sendEvent(sunrise)'"
	// TODO: handle 'sendEvent(sunrise)' command
}

def sendEvent(sunset)() {
	log.debug "Executing 'sendEvent(sunset)'"
	// TODO: handle 'sendEvent(sunset)' command
}

def sendEvent(sunriseTime)() {
	log.debug "Executing 'sendEvent(sunriseTime)'"
	// TODO: handle 'sendEvent(sunriseTime)' command
}

def sendEvent(sunsetTime)() {
	log.debug "Executing 'sendEvent(sunsetTime)'"
	// TODO: handle 'sendEvent(sunsetTime)' command
}

def setMute() {
	log.debug "Executing 'setMute'"
	// TODO: handle 'setMute' command
}

def mute() {
	log.debug "Executing 'mute'"
	// TODO: handle 'mute' command
}

def unmute() {
	log.debug "Executing 'unmute'"
	// TODO: handle 'unmute' command
}

def playTrack() {
	log.debug "Executing 'playTrack'"
	// TODO: handle 'playTrack' command
}

def playTrackAndResume() {
	log.debug "Executing 'playTrackAndResume'"
	// TODO: handle 'playTrackAndResume' command
}

def playTrackAndRestore() {
	log.debug "Executing 'playTrackAndRestore'"
	// TODO: handle 'playTrackAndRestore' command
}

def setAudioTrack() {
	log.debug "Executing 'setAudioTrack'"
	// TODO: handle 'setAudioTrack' command
}

def setVolume() {
	log.debug "Executing 'setVolume'"
	// TODO: handle 'setVolume' command
}

def volumeUp() {
	log.debug "Executing 'volumeUp'"
	// TODO: handle 'volumeUp' command
}

def volumeDown() {
	log.debug "Executing 'volumeDown'"
	// TODO: handle 'volumeDown' command
}

def capture() {
	log.debug "Executing 'capture'"
	// TODO: handle 'capture' command
}

def off() {
	log.debug "Executing 'off'"
	// TODO: handle 'off' command
}

def on() {
	log.debug "Executing 'on'"
	// TODO: handle 'on' command
}

def setHue() {
	log.debug "Executing 'setHue'"
	// TODO: handle 'setHue' command
}

def setSaturation() {
	log.debug "Executing 'setSaturation'"
	// TODO: handle 'setSaturation' command
}

def setColor() {
	log.debug "Executing 'setColor'"
	// TODO: handle 'setColor' command
}

def setColorTemperature() {
	log.debug "Executing 'setColorTemperature'"
	// TODO: handle 'setColorTemperature' command
}

def configure() {
	log.debug "Executing 'configure'"
	// TODO: handle 'configure' command
}

def setConsumableStatus() {
	log.debug "Executing 'setConsumableStatus'"
	// TODO: handle 'setConsumableStatus' command
}

def setDishwasherMode() {
	log.debug "Executing 'setDishwasherMode'"
	// TODO: handle 'setDishwasherMode' command
}

def setMachineState() {
	log.debug "Executing 'setMachineState'"
	// TODO: handle 'setMachineState' command
}

def setMachineState() {
	log.debug "Executing 'setMachineState'"
	// TODO: handle 'setMachineState' command
}

def open() {
	log.debug "Executing 'open'"
	// TODO: handle 'open' command
}

def close() {
	log.debug "Executing 'close'"
	// TODO: handle 'close' command
}

def setDryerMode() {
	log.debug "Executing 'setDryerMode'"
	// TODO: handle 'setDryerMode' command
}

def setMachineState() {
	log.debug "Executing 'setMachineState'"
	// TODO: handle 'setMachineState' command
}

def setMachineState() {
	log.debug "Executing 'setMachineState'"
	// TODO: handle 'setMachineState' command
}

def execute() {
	log.debug "Executing 'execute'"
	// TODO: handle 'execute' command
}

def setFanSpeed() {
	log.debug "Executing 'setFanSpeed'"
	// TODO: handle 'setFanSpeed' command
}

def open() {
	log.debug "Executing 'open'"
	// TODO: handle 'open' command
}

def close() {
	log.debug "Executing 'close'"
	// TODO: handle 'close' command
}

def ping() {
	log.debug "Executing 'ping'"
	// TODO: handle 'ping' command
}

def take() {
	log.debug "Executing 'take'"
	// TODO: handle 'take' command
}

def indicatorWhenOn() {
	log.debug "Executing 'indicatorWhenOn'"
	// TODO: handle 'indicatorWhenOn' command
}

def indicatorWhenOff() {
	log.debug "Executing 'indicatorWhenOff'"
	// TODO: handle 'indicatorWhenOff' command
}

def indicatorNever() {
	log.debug "Executing 'indicatorNever'"
	// TODO: handle 'indicatorNever' command
}

def setInfraredLevel() {
	log.debug "Executing 'setInfraredLevel'"
	// TODO: handle 'setInfraredLevel' command
}

def off() {
	log.debug "Executing 'off'"
	// TODO: handle 'off' command
}

def on() {
	log.debug "Executing 'on'"
	// TODO: handle 'on' command
}

def setMode() {
	log.debug "Executing 'setMode'"
	// TODO: handle 'setMode' command
}

def lock() {
	log.debug "Executing 'lock'"
	// TODO: handle 'lock' command
}

def unlock() {
	log.debug "Executing 'unlock'"
	// TODO: handle 'unlock' command
}

def lock() {
	log.debug "Executing 'lock'"
	// TODO: handle 'lock' command
}

def unlock() {
	log.debug "Executing 'unlock'"
	// TODO: handle 'unlock' command
}

def setCode() {
	log.debug "Executing 'setCode'"
	// TODO: handle 'setCode' command
}

def deleteCode() {
	log.debug "Executing 'deleteCode'"
	// TODO: handle 'deleteCode' command
}

def requestCode() {
	log.debug "Executing 'requestCode'"
	// TODO: handle 'requestCode' command
}

def reloadAllCodes() {
	log.debug "Executing 'reloadAllCodes'"
	// TODO: handle 'reloadAllCodes' command
}

def unlockWithTimeout() {
	log.debug "Executing 'unlockWithTimeout'"
	// TODO: handle 'unlockWithTimeout' command
}

def setCodeLength() {
	log.debug "Executing 'setCodeLength'"
	// TODO: handle 'setCodeLength' command
}

def nameSlot() {
	log.debug "Executing 'nameSlot'"
	// TODO: handle 'nameSlot' command
}

def updateCodes() {
	log.debug "Executing 'updateCodes'"
	// TODO: handle 'updateCodes' command
}

def lock() {
	log.debug "Executing 'lock'"
	// TODO: handle 'lock' command
}

def startActivity() {
	log.debug "Executing 'startActivity'"
	// TODO: handle 'startActivity' command
}

def setInputSource() {
	log.debug "Executing 'setInputSource'"
	// TODO: handle 'setInputSource' command
}

def setPlaybackStatus() {
	log.debug "Executing 'setPlaybackStatus'"
	// TODO: handle 'setPlaybackStatus' command
}

def play() {
	log.debug "Executing 'play'"
	// TODO: handle 'play' command
}

def pause() {
	log.debug "Executing 'pause'"
	// TODO: handle 'pause' command
}

def stop() {
	log.debug "Executing 'stop'"
	// TODO: handle 'stop' command
}

def setPlaybackRepeatMode() {
	log.debug "Executing 'setPlaybackRepeatMode'"
	// TODO: handle 'setPlaybackRepeatMode' command
}

def setPlaybackShuffle() {
	log.debug "Executing 'setPlaybackShuffle'"
	// TODO: handle 'setPlaybackShuffle' command
}

def nextTrack() {
	log.debug "Executing 'nextTrack'"
	// TODO: handle 'nextTrack' command
}

def previousTrack() {
	log.debug "Executing 'previousTrack'"
	// TODO: handle 'previousTrack' command
}

def push() {
	log.debug "Executing 'push'"
	// TODO: handle 'push' command
}

def play() {
	log.debug "Executing 'play'"
	// TODO: handle 'play' command
}

def pause() {
	log.debug "Executing 'pause'"
	// TODO: handle 'pause' command
}

def stop() {
	log.debug "Executing 'stop'"
	// TODO: handle 'stop' command
}

def nextTrack() {
	log.debug "Executing 'nextTrack'"
	// TODO: handle 'nextTrack' command
}

def playTrack() {
	log.debug "Executing 'playTrack'"
	// TODO: handle 'playTrack' command
}

def setLevel() {
	log.debug "Executing 'setLevel'"
	// TODO: handle 'setLevel' command
}

def mute() {
	log.debug "Executing 'mute'"
	// TODO: handle 'mute' command
}

def previousTrack() {
	log.debug "Executing 'previousTrack'"
	// TODO: handle 'previousTrack' command
}

def unmute() {
	log.debug "Executing 'unmute'"
	// TODO: handle 'unmute' command
}

def setTrack() {
	log.debug "Executing 'setTrack'"
	// TODO: handle 'setTrack' command
}

def resumeTrack() {
	log.debug "Executing 'resumeTrack'"
	// TODO: handle 'resumeTrack' command
}

def restoreTrack() {
	log.debug "Executing 'restoreTrack'"
	// TODO: handle 'restoreTrack' command
}

def deviceNotification() {
	log.debug "Executing 'deviceNotification'"
	// TODO: handle 'deviceNotification' command
}

def postOcfCommand() {
	log.debug "Executing 'postOcfCommand'"
	// TODO: handle 'postOcfCommand' command
}

def off() {
	log.debug "Executing 'off'"
	// TODO: handle 'off' command
}

def on() {
	log.debug "Executing 'on'"
	// TODO: handle 'on' command
}

def setOvenMode() {
	log.debug "Executing 'setOvenMode'"
	// TODO: handle 'setOvenMode' command
}

def setMachineState() {
	log.debug "Executing 'setMachineState'"
	// TODO: handle 'setMachineState' command
}

def stop() {
	log.debug "Executing 'stop'"
	// TODO: handle 'stop' command
}

def setMachineState() {
	log.debug "Executing 'setMachineState'"
	// TODO: handle 'setMachineState' command
}

def stop() {
	log.debug "Executing 'stop'"
	// TODO: handle 'stop' command
}

def setOvenSetpoint() {
	log.debug "Executing 'setOvenSetpoint'"
	// TODO: handle 'setOvenSetpoint' command
}

def poll() {
	log.debug "Executing 'poll'"
	// TODO: handle 'poll' command
}

def setRapidCooling() {
	log.debug "Executing 'setRapidCooling'"
	// TODO: handle 'setRapidCooling' command
}

def refresh() {
	log.debug "Executing 'refresh'"
	// TODO: handle 'refresh' command
}

def setRapidCooling() {
	log.debug "Executing 'setRapidCooling'"
	// TODO: handle 'setRapidCooling' command
}

def setRapidFreezing() {
	log.debug "Executing 'setRapidFreezing'"
	// TODO: handle 'setRapidFreezing' command
}

def setDefrost() {
	log.debug "Executing 'setDefrost'"
	// TODO: handle 'setDefrost' command
}

def setRefrigerationSetpoint() {
	log.debug "Executing 'setRefrigerationSetpoint'"
	// TODO: handle 'setRefrigerationSetpoint' command
}

def on() {
	log.debug "Executing 'on'"
	// TODO: handle 'on' command
}

def off() {
	log.debug "Executing 'off'"
	// TODO: handle 'off' command
}

def setRobotCleanerCleaningMode() {
	log.debug "Executing 'setRobotCleanerCleaningMode'"
	// TODO: handle 'setRobotCleanerCleaningMode' command
}

def setRobotCleanerMovement() {
	log.debug "Executing 'setRobotCleanerMovement'"
	// TODO: handle 'setRobotCleanerMovement' command
}

def stop() {
	log.debug "Executing 'stop'"
	// TODO: handle 'stop' command
}

def setRobotCleaningState() {
	log.debug "Executing 'setRobotCleaningState'"
	// TODO: handle 'setRobotCleaningState' command
}

def setRobotControlState() {
	log.debug "Executing 'setRobotControlState'"
	// TODO: handle 'setRobotControlState' command
}

def setRobotTurboState() {
	log.debug "Executing 'setRobotTurboState'"
	// TODO: handle 'setRobotTurboState' command
}

def setRobotCleanerTurboMode() {
	log.debug "Executing 'setRobotCleanerTurboMode'"
	// TODO: handle 'setRobotCleanerTurboMode' command
}

def volumeUp() {
	log.debug "Executing 'volumeUp'"
	// TODO: handle 'volumeUp' command
}

def volumeDown() {
	log.debug "Executing 'volumeDown'"
	// TODO: handle 'volumeDown' command
}

def setVolume() {
	log.debug "Executing 'setVolume'"
	// TODO: handle 'setVolume' command
}

def mute() {
	log.debug "Executing 'mute'"
	// TODO: handle 'mute' command
}

def unmute() {
	log.debug "Executing 'unmute'"
	// TODO: handle 'unmute' command
}

def setPictureMode() {
	log.debug "Executing 'setPictureMode'"
	// TODO: handle 'setPictureMode' command
}

def setSoundMode() {
	log.debug "Executing 'setSoundMode'"
	// TODO: handle 'setSoundMode' command
}

def on() {
	log.debug "Executing 'on'"
	// TODO: handle 'on' command
}

def off() {
	log.debug "Executing 'off'"
	// TODO: handle 'off' command
}

def showMessage() {
	log.debug "Executing 'showMessage'"
	// TODO: handle 'showMessage' command
}

def armStay() {
	log.debug "Executing 'armStay'"
	// TODO: handle 'armStay' command
}

def armAway() {
	log.debug "Executing 'armAway'"
	// TODO: handle 'armAway' command
}

def disarm() {
	log.debug "Executing 'disarm'"
	// TODO: handle 'disarm' command
}

def speak() {
	log.debug "Executing 'speak'"
	// TODO: handle 'speak' command
}

def on() {
	log.debug "Executing 'on'"
	// TODO: handle 'on' command
}

def off() {
	log.debug "Executing 'off'"
	// TODO: handle 'off' command
}

def setLevel() {
	log.debug "Executing 'setLevel'"
	// TODO: handle 'setLevel' command
}

def setHeatingSetpoint() {
	log.debug "Executing 'setHeatingSetpoint'"
	// TODO: handle 'setHeatingSetpoint' command
}

def setCoolingSetpoint() {
	log.debug "Executing 'setCoolingSetpoint'"
	// TODO: handle 'setCoolingSetpoint' command
}

def off() {
	log.debug "Executing 'off'"
	// TODO: handle 'off' command
}

def heat() {
	log.debug "Executing 'heat'"
	// TODO: handle 'heat' command
}

def emergencyHeat() {
	log.debug "Executing 'emergencyHeat'"
	// TODO: handle 'emergencyHeat' command
}

def cool() {
	log.debug "Executing 'cool'"
	// TODO: handle 'cool' command
}

def setThermostatMode() {
	log.debug "Executing 'setThermostatMode'"
	// TODO: handle 'setThermostatMode' command
}

def fanOn() {
	log.debug "Executing 'fanOn'"
	// TODO: handle 'fanOn' command
}

def fanAuto() {
	log.debug "Executing 'fanAuto'"
	// TODO: handle 'fanAuto' command
}

def fanCirculate() {
	log.debug "Executing 'fanCirculate'"
	// TODO: handle 'fanCirculate' command
}

def setThermostatFanMode() {
	log.debug "Executing 'setThermostatFanMode'"
	// TODO: handle 'setThermostatFanMode' command
}

def auto() {
	log.debug "Executing 'auto'"
	// TODO: handle 'auto' command
}

def setSchedule() {
	log.debug "Executing 'setSchedule'"
	// TODO: handle 'setSchedule' command
}

def setCoolingSetpoint() {
	log.debug "Executing 'setCoolingSetpoint'"
	// TODO: handle 'setCoolingSetpoint' command
}

def fanOn() {
	log.debug "Executing 'fanOn'"
	// TODO: handle 'fanOn' command
}

def fanAuto() {
	log.debug "Executing 'fanAuto'"
	// TODO: handle 'fanAuto' command
}

def fanCirculate() {
	log.debug "Executing 'fanCirculate'"
	// TODO: handle 'fanCirculate' command
}

def setThermostatFanMode() {
	log.debug "Executing 'setThermostatFanMode'"
	// TODO: handle 'setThermostatFanMode' command
}

def setHeatingSetpoint() {
	log.debug "Executing 'setHeatingSetpoint'"
	// TODO: handle 'setHeatingSetpoint' command
}

def off() {
	log.debug "Executing 'off'"
	// TODO: handle 'off' command
}

def heat() {
	log.debug "Executing 'heat'"
	// TODO: handle 'heat' command
}

def emergencyHeat() {
	log.debug "Executing 'emergencyHeat'"
	// TODO: handle 'emergencyHeat' command
}

def cool() {
	log.debug "Executing 'cool'"
	// TODO: handle 'cool' command
}

def auto() {
	log.debug "Executing 'auto'"
	// TODO: handle 'auto' command
}

def setThermostatMode() {
	log.debug "Executing 'setThermostatMode'"
	// TODO: handle 'setThermostatMode' command
}

def setSchedule() {
	log.debug "Executing 'setSchedule'"
	// TODO: handle 'setSchedule' command
}

def setTimeRemaining() {
	log.debug "Executing 'setTimeRemaining'"
	// TODO: handle 'setTimeRemaining' command
}

def start() {
	log.debug "Executing 'start'"
	// TODO: handle 'start' command
}

def stop() {
	log.debug "Executing 'stop'"
	// TODO: handle 'stop' command
}

def pause() {
	log.debug "Executing 'pause'"
	// TODO: handle 'pause' command
}

def cancel() {
	log.debug "Executing 'cancel'"
	// TODO: handle 'cancel' command
}

def beep() {
	log.debug "Executing 'beep'"
	// TODO: handle 'beep' command
}

def volumeUp() {
	log.debug "Executing 'volumeUp'"
	// TODO: handle 'volumeUp' command
}

def volumeDown() {
	log.debug "Executing 'volumeDown'"
	// TODO: handle 'volumeDown' command
}

def channelUp() {
	log.debug "Executing 'channelUp'"
	// TODO: handle 'channelUp' command
}

def channelDown() {
	log.debug "Executing 'channelDown'"
	// TODO: handle 'channelDown' command
}

def setTvChannel() {
	log.debug "Executing 'setTvChannel'"
	// TODO: handle 'setTvChannel' command
}

def channelUp() {
	log.debug "Executing 'channelUp'"
	// TODO: handle 'channelUp' command
}

def channelDown() {
	log.debug "Executing 'channelDown'"
	// TODO: handle 'channelDown' command
}

def open() {
	log.debug "Executing 'open'"
	// TODO: handle 'open' command
}

def close() {
	log.debug "Executing 'close'"
	// TODO: handle 'close' command
}

def on() {
	log.debug "Executing 'on'"
	// TODO: handle 'on' command
}

def off() {
	log.debug "Executing 'off'"
	// TODO: handle 'off' command
}

def mute() {
	log.debug "Executing 'mute'"
	// TODO: handle 'mute' command
}

def unmute() {
	log.debug "Executing 'unmute'"
	// TODO: handle 'unmute' command
}

def flip() {
	log.debug "Executing 'flip'"
	// TODO: handle 'flip' command
}

def capture() {
	log.debug "Executing 'capture'"
	// TODO: handle 'capture' command
}

def startStream() {
	log.debug "Executing 'startStream'"
	// TODO: handle 'startStream' command
}

def stopStream() {
	log.debug "Executing 'stopStream'"
	// TODO: handle 'stopStream' command
}

def setWasherMode() {
	log.debug "Executing 'setWasherMode'"
	// TODO: handle 'setWasherMode' command
}

def setMachineState() {
	log.debug "Executing 'setMachineState'"
	// TODO: handle 'setMachineState' command
}

def setMachineState() {
	log.debug "Executing 'setMachineState'"
	// TODO: handle 'setMachineState' command
}

def open() {
	log.debug "Executing 'open'"
	// TODO: handle 'open' command
}

def close() {
	log.debug "Executing 'close'"
	// TODO: handle 'close' command
}

def presetPosition() {
	log.debug "Executing 'presetPosition'"
	// TODO: handle 'presetPosition' command
}

def enableEpEvents() {
	log.debug "Executing 'enableEpEvents'"
	// TODO: handle 'enableEpEvents' command
}

def epCmd() {
	log.debug "Executing 'epCmd'"
	// TODO: handle 'epCmd' command
}