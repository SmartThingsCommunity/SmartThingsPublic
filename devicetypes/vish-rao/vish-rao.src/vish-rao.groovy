/**
 *  Vish Rao
 *
 *  Copyright 2018 Vish Rao
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
	definition (name: "Vish Rao", namespace: "Vish Rao", author: "Vish Rao") {
		capability "Acceleration Sensor"
		capability "Air Conditioner Mode"
		capability "Air Quality Sensor"
		capability "Alarm"
		capability "Alarm System"
		capability "Alarm System Arm Only"
		capability "Astronomical Data"
		capability "Audio Mute"
		capability "Audio Notification"
		capability "Audio Volume"
		capability "Battery"
		capability "Beacon"
		capability "Bridge"
		capability "Buffered Video Capture"
		capability "Bulb"
		capability "Button"
		capability "Carbon Dioxide Measurement"
		capability "Carbon Monoxide Detector"
		capability "Configuration"
		capability "Contact Sensor"
		capability "Dishwasher Mode"
		capability "Dishwasher Operating State"
		capability "Dishwasher Operational State"
		capability "Door Control"
		capability "Dryer Mode"
		capability "Dryer Operating State"
		capability "Dryer Operational State"
		capability "Image Capture"
		capability "Light"
		capability "Location Mode"
		capability "Media Controller"
		capability "Media Input Source"
		capability "Media Playback"
		capability "Motion Sensor"
		capability "Music Player"
		capability "Outlet"
		capability "Samsung TV"
		capability "Sensor"
		capability "Smoke Detector"
		capability "Switch Level"
		capability "Thermostat"
		capability "Thermostat Cooling Setpoint"
		capability "Thermostat Fan Mode"
		capability "Thermostat Heating Setpoint"
		capability "Thermostat Mode"
		capability "Thermostat Operating State"
		capability "Thermostat Schedule"
		capability "Thermostat Setpoint"
		capability "TV"
		capability "Tv Channel"
		capability "Video Capture"
		capability "Video Stream"
		capability "Washer Mode"
		capability "Water Sensor"
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
	// TODO: handle 'volume' attribute
	// TODO: handle 'battery' attribute
	// TODO: handle 'presence' attribute
	// TODO: handle 'clip' attribute
	// TODO: handle 'switch' attribute
	// TODO: handle 'button' attribute
	// TODO: handle 'numberOfButtons' attribute
	// TODO: handle 'supportedButtonValues' attribute
	// TODO: handle 'carbonDioxide' attribute
	// TODO: handle 'carbonMonoxide' attribute
	// TODO: handle 'contact' attribute
	// TODO: handle 'dishwasherMode' attribute
	// TODO: handle 'machineState' attribute
	// TODO: handle 'supportedMachineStates' attribute
	// TODO: handle 'dishwasherJobState' attribute
	// TODO: handle 'completionTime' attribute
	// TODO: handle 'machineState' attribute
	// TODO: handle 'supportedMachineStates' attribute
	// TODO: handle 'dishwasherJobState' attribute
	// TODO: handle 'completionTime' attribute
	// TODO: handle 'door' attribute
	// TODO: handle 'dryerMode' attribute
	// TODO: handle 'machineState' attribute
	// TODO: handle 'supportedMachineStates' attribute
	// TODO: handle 'dryerJobState' attribute
	// TODO: handle 'completionTime' attribute
	// TODO: handle 'machineState' attribute
	// TODO: handle 'supportedMachineStates' attribute
	// TODO: handle 'dryerJobState' attribute
	// TODO: handle 'remainingTime' attribute
	// TODO: handle 'image' attribute
	// TODO: handle 'switch' attribute
	// TODO: handle 'mode' attribute
	// TODO: handle 'activities' attribute
	// TODO: handle 'currentActivity' attribute
	// TODO: handle 'inputSource' attribute
	// TODO: handle 'supportedInputSources' attribute
	// TODO: handle 'playbackStatus' attribute
	// TODO: handle 'motion' attribute
	// TODO: handle 'status' attribute
	// TODO: handle 'level' attribute
	// TODO: handle 'trackDescription' attribute
	// TODO: handle 'trackData' attribute
	// TODO: handle 'mute' attribute
	// TODO: handle 'switch' attribute
	// TODO: handle 'volume' attribute
	// TODO: handle 'mute' attribute
	// TODO: handle 'pictureMode' attribute
	// TODO: handle 'soundMode' attribute
	// TODO: handle 'switch' attribute
	// TODO: handle 'messageButton' attribute
	// TODO: handle 'smoke' attribute
	// TODO: handle 'level' attribute
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
	// TODO: handle 'thermostatFanMode' attribute
	// TODO: handle 'supportedThermostatFanModes' attribute
	// TODO: handle 'heatingSetpoint' attribute
	// TODO: handle 'thermostatMode' attribute
	// TODO: handle 'supportedThermostatModes' attribute
	// TODO: handle 'thermostatOperatingState' attribute
	// TODO: handle 'schedule' attribute
	// TODO: handle 'thermostatSetpoint' attribute
	// TODO: handle 'volume' attribute
	// TODO: handle 'channel' attribute
	// TODO: handle 'power' attribute
	// TODO: handle 'picture' attribute
	// TODO: handle 'sound' attribute
	// TODO: handle 'movieMode' attribute
	// TODO: handle 'tvChannel' attribute
	// TODO: handle 'clip' attribute
	// TODO: handle 'stream' attribute
	// TODO: handle 'stream' attribute
	// TODO: handle 'washerMode' attribute
	// TODO: handle 'water' attribute

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

def configure() {
	log.debug "Executing 'configure'"
	// TODO: handle 'configure' command
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

def take() {
	log.debug "Executing 'take'"
	// TODO: handle 'take' command
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

def off() {
	log.debug "Executing 'off'"
	// TODO: handle 'off' command
}

def on() {
	log.debug "Executing 'on'"
	// TODO: handle 'on' command
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