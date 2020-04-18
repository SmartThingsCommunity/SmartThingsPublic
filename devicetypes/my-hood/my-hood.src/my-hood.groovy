/**
 *  My Hood
 *
 *  Copyright 2020 Bojan H&auml;ubi
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 */
metadata {
	definition (name: "My Hood", namespace: "My Hood", author: "Bojan H&auml;ubi", cstHandler: true) {
		capability "Audio Stream"
		capability "Audio Volume"
		capability "Media Controller"
		capability "Media Group"
		capability "Media Input Source"
		capability "Media Playback"
		capability "Media Presets"
		capability "Media Track Control"
		capability "TV"
		capability "Tv Channel"
		capability "Video Stream"
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
	// TODO: handle 'uri' attribute
	// TODO: handle 'volume' attribute
	// TODO: handle 'activities' attribute
	// TODO: handle 'currentActivity' attribute
	// TODO: handle 'groupRole' attribute
	// TODO: handle 'groupPrimaryDeviceId' attribute
	// TODO: handle 'groupId' attribute
	// TODO: handle 'groupVolume' attribute
	// TODO: handle 'groupMute' attribute
	// TODO: handle 'inputSource' attribute
	// TODO: handle 'supportedInputSources' attribute
	// TODO: handle 'playbackStatus' attribute
	// TODO: handle 'supportedPlaybackCommands' attribute
	// TODO: handle 'presets' attribute
	// TODO: handle 'supportedTrackControlCommands' attribute
	// TODO: handle 'volume' attribute
	// TODO: handle 'channel' attribute
	// TODO: handle 'power' attribute
	// TODO: handle 'picture' attribute
	// TODO: handle 'sound' attribute
	// TODO: handle 'movieMode' attribute
	// TODO: handle 'tvChannel' attribute
	// TODO: handle 'tvChannelName' attribute
	// TODO: handle 'stream' attribute

}

// handle commands
def startAudio() {
	log.debug "Executing 'startAudio'"
	// TODO: handle 'startAudio' command
}

def stopAudio() {
	log.debug "Executing 'stopAudio'"
	// TODO: handle 'stopAudio' command
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

def startActivity() {
	log.debug "Executing 'startActivity'"
	// TODO: handle 'startActivity' command
}

def setGroupVolume() {
	log.debug "Executing 'setGroupVolume'"
	// TODO: handle 'setGroupVolume' command
}

def groupVolumeUp() {
	log.debug "Executing 'groupVolumeUp'"
	// TODO: handle 'groupVolumeUp' command
}

def groupVolumeDown() {
	log.debug "Executing 'groupVolumeDown'"
	// TODO: handle 'groupVolumeDown' command
}

def setGroupMute() {
	log.debug "Executing 'setGroupMute'"
	// TODO: handle 'setGroupMute' command
}

def muteGroup() {
	log.debug "Executing 'muteGroup'"
	// TODO: handle 'muteGroup' command
}

def unmuteGroup() {
	log.debug "Executing 'unmuteGroup'"
	// TODO: handle 'unmuteGroup' command
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

def fastForward() {
	log.debug "Executing 'fastForward'"
	// TODO: handle 'fastForward' command
}

def rewind() {
	log.debug "Executing 'rewind'"
	// TODO: handle 'rewind' command
}

def playPreset() {
	log.debug "Executing 'playPreset'"
	// TODO: handle 'playPreset' command
}

def nextTrack() {
	log.debug "Executing 'nextTrack'"
	// TODO: handle 'nextTrack' command
}

def previousTrack() {
	log.debug "Executing 'previousTrack'"
	// TODO: handle 'previousTrack' command
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

def setTvChannelName() {
	log.debug "Executing 'setTvChannelName'"
	// TODO: handle 'setTvChannelName' command
}

def startStream() {
	log.debug "Executing 'startStream'"
	// TODO: handle 'startStream' command
}

def stopStream() {
	log.debug "Executing 'stopStream'"
	// TODO: handle 'stopStream' command
}