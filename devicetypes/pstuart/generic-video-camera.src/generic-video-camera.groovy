/**
 *  Generic Video Camera
 *
 *  Copyright 2016 Patrick Stuart
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
	definition (name: "Generic Video Camera", namespace: "pstuart", author: "Patrick Stuart") {
		capability "Configuration"
		capability "Video Camera"
		capability "Video Capture"
		capability "Refresh"
		capability "Switch"

		// custom commands
		command "start"
	}

	simulator {
		// TODO: define status and reply messages here
	}

	tiles(scale: 2) {
		multiAttributeTile(name: "videoPlayer", type: "videoPlayer", width: 6, height: 4) {
			tileAttribute("device.switch", key: "CAMERA_STATUS") {
				attributeState("on", label: "Active", icon: "st.camera.dlink-indoor", action: "switch.off", backgroundColor: "#79b821", defaultState: true)
				attributeState("off", label: "Inactive", icon: "st.camera.dlink-indoor", action: "switch.on", backgroundColor: "#ffffff")
				attributeState("restarting", label: "Connecting", icon: "st.camera.dlink-indoor", backgroundColor: "#53a7c0")
				attributeState("unavailable", label: "Unavailable", icon: "st.camera.dlink-indoor", action: "refresh.refresh", backgroundColor: "#F22000")
			}

			tileAttribute("device.errorMessage", key: "CAMERA_ERROR_MESSAGE") {
				attributeState("errorMessage", label: "", value: "", defaultState: true)
			}

			tileAttribute("device.camera", key: "PRIMARY_CONTROL") {
				attributeState("on", label: "Active", icon: "st.camera.dlink-indoor", backgroundColor: "#79b821", defaultState: true)
				attributeState("off", label: "Inactive", icon: "st.camera.dlink-indoor", backgroundColor: "#ffffff")
				attributeState("restarting", label: "Connecting", icon: "st.camera.dlink-indoor", backgroundColor: "#53a7c0")
				attributeState("unavailable", label: "Unavailable", icon: "st.camera.dlink-indoor", backgroundColor: "#F22000")
			}

			tileAttribute("device.startLive", key: "START_LIVE") {
				attributeState("live", action: "start", defaultState: true)
			}

			tileAttribute("device.stream", key: "STREAM_URL") {
				attributeState("activeURL", defaultState: true)
			}
            /*
			tileAttribute("device.profile", key: "STREAM_QUALITY") {
				attributeState("1", label: "720p", action: "setProfileHD", defaultState: true)
				attributeState("2", label: "h360p", action: "setProfileSDH", defaultState: true)
				attributeState("3", label: "l360p", action: "setProfileSDL", defaultState: true)
			}	*/		
		}
		

		main("videoPlayer")
		details(["videoPlayer"])
	}
}

mappings {
   path("/getInHomeURL") {
       action:
       [GET: "getInHomeURL"]
   }
}


def installed() {
	configure()
}

def updated() {
	configure()
}
// parse events into attributes
def parse(String description) {
	log.debug "Parsing '${description}'"

}

// handle commands
def configure() {
	log.debug "Executing 'configure'"
    sendEvent(name:"switch", value: "on")
}

def start() {
	log.trace "start()"
	def dataLiveVideo = [
		OutHomeURL  : parent.state.CameraStreamPath,
		InHomeURL   : parent.state.CameraStreamPath,
		ThumbnailURL: "http://cdn.device-icons.smartthings.com/camera/dlink-indoor@2x.png",
		cookie      : [key: "key", value: "value"]
	]

	def event = [
		name           : "stream",
		value          : groovy.json.JsonOutput.toJson(dataLiveVideo).toString(),
		data		   : groovy.json.JsonOutput.toJson(dataLiveVideo),
		descriptionText: "Starting the livestream",
		eventType      : "VIDEO",
		displayed      : false,
		isStateChange  : true
	]
	sendEvent(event)
}

def getInHomeURL() {
	 [InHomeURL: parent.state.CameraStreamPath]
}