/**
 *  Copyright 2015 SmartThings
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

def clientVersion() {
    return "1.0.1"
}
 
metadata {
	definition (name: "iCamera 1000", namespace: "dkmcgowan", author: "dkmcgowan") {
        capability "Refresh"
        capability "Video Camera"
        capability "Video Capture"
        capability "Configuration"
    
        command "startVideo"
	}
    
    preferences {
        input title: "", description: "iCamera 1000 Device Handler v${clientVersion()}", displayDuringSetup: true, type: "paragraph", element: "paragraph"
        input title: "", description: "NOTE: For live streaming to work your phone needs to be able to reach your camera directly using the IP Address/URL below", displayDuringSetup: true, type: "paragraph", element: "paragraph"
        input("ip", "string", title:"Camera IP Address/Public Hostname", description: "Camera IP Address or DNS Hostname", required: false, displayDuringSetup: true)
        input("port", "number", title:"Camera Port", description: "Camera Port", defaultValue: "80", required: false, displayDuringSetup: true)
        input("username", "string", title:"Camera Username (case sensitive)", description: "Camera Username (case sensitive)", required: false, displayDuringSetup: true)
        input("password", "password", title:"Camera Password (case sensitive)", description: "Camera Password (case sensitive)", required: false, displayDuringSetup: true)
        input title: "", description: "If your camera has a separate RTSP port configured then enter it here for live streaming (otherwise it defaults to the Camera Port above). Most cameras that have a separate RTSP port typically use port 554", displayDuringSetup: true, type: "paragraph", element: "paragraph"
        input("rtspport", "number", title:"RTSP Port", description: "RTSP Port", defaultValue: "554", required: false, displayDuringSetup: true)
        input title: "", description: "SmartTiles MJPEG Streaming\nIf your HD camera supports MJPEG, Enable this option to view the live stream in SmartTiles using this URL: http://USERNAME:PASSWORD@$IPADDRESS:PORT/img/video.mjpeg", displayDuringSetup: true, type: "paragraph", element: "paragraph"
        input("mjpeg", "bool", title:"Enable HD Camera MJPEG Stream", description: "MJPEG Streaming", required: true, displayDuringSetup: true)
	}

	tiles(scale: 2) {
        multiAttributeTile(name: "videoPlayer", type: "videoPlayer", width: 6, height: 4) {
            tileAttribute("device.camera", key: "CAMERA_STATUS") {
				attributeState("on", label: "Active", icon: "st.camera.dlink-hdpan", action: "", backgroundColor: "#79b821", defaultState: true)
				attributeState("off", label: "Inactive", icon: "st.camera.dlink-hdpan", action: "", backgroundColor: "#ffffff")
				attributeState("restarting", label: "Connecting", icon: "st.camera.dlink-hdpan", backgroundColor: "#53a7c0")
				attributeState("unavailable", label: "Click here to connect", icon: "st.camera.dlink-hdpan", action: "", backgroundColor: "#F22000")
			}

			tileAttribute("device.errorMessage", key: "CAMERA_ERROR_MESSAGE") {
				attributeState("errorMessage", label: "", value: "", defaultState: true)
			}

			tileAttribute("device.camera", key: "PRIMARY_CONTROL") {
				attributeState("on", label: "Active", icon: "st.camera.dlink-hdpan", backgroundColor: "#79b821", defaultState: true)
				attributeState("off", label: "Inactive", icon: "st.camera.dlink-hdpan", backgroundColor: "#ffffff")
				attributeState("restarting", label: "Connecting", icon: "st.camera.dlink-hdpan", backgroundColor: "#53a7c0")
				attributeState("unavailable", label: "Click here to connect", icon: "st.camera.dlink-hdpan", backgroundColor: "#F22000")
			}
            
            tileAttribute("device.startLive", key: "START_LIVE") {
				attributeState("live", action: "startVideo", defaultState: true)
			}

			tileAttribute("device.stream", key: "STREAM_URL") {
				attributeState("activeURL", defaultState: true)
			}
		}

        main "videoPlayer"
        details(["videoPlayer"])
	}
}

import groovy.json.JsonSlurper

def initialize() {
    log.trace "Initialize called settings: $settings"
	try {
		if (!state.init) {
			state.init = true
		}
        response(refresh())
	} catch (e) {
		log.warn "initialize() threw $e"
	}
}

def updated() {
	log.trace "Update called settings: $settings"
	try {
		if (!state.init) {
			state.init = true
		}
    	response(refresh())
	} catch (e) {
		log.warn "updated() threw $e"
	}
}

//START VIDEO
mappings {
    path("/getInHomeURL") {
        action:
            [GET: "getInHomeURL"]
    }
    
    path("/getOutHomeURL") {
        action:
            [GET: "getOutHomeURL"]
    }
}

def getInHomeURL() {
    log.trace "Called getInHomeURL, returning $state.uri"
    state.uri ? [InHomeURL: state.uri]: null
}

def getOutHomeURL() {
    log.trace "Called getOutHomeURL, returning $state.uri"
    state.uri ? [OutHomeURL: state.uri] : null
}

def setUri() {
	log.debug "Setting uri, $state.uri"
    if (mjpeg) {
        log.trace "Using MJPEG for low bitrate streaming"
        state.uri = "http://${username}:${password}@${ip}:${port}" + "/img/video.mjpeg"
    } else {
        log.trace "Using h.264 sub stream for low bitrate streaming"
        state.uri = "rtsp://${URLEncoder.encode(username)}:${URLEncoder.encode(password)}@${ip}:${rtspport ?: port}/img/media.sav"
    }
    sendEvent(name: "profile", value: "default", displayed: false)
}

def startVideo() {
    log.trace "Fetching video from: ${state.uri}"
    
    if (!state.uri) {
        refresh() // Initialize the camera
    }
    
	def dataLiveVideo = [
		OutHomeURL  : state.uri,
		InHomeURL   : state.uri,
		ThumbnailURL: "http://cdn.device-icons.smartthings.com/camera/dlink-hdpan@2x.png",
		cookie      : [key: "key", value: "value"]
	]

    log.trace "Video settings: $dataLiveVideo"
    
	def event = [
		name           : "stream",
		value          : groovy.json.JsonOutput.toJson(dataLiveVideo).toString(),
		data		   : groovy.json.JsonOutput.toJson(dataLiveVideo),
		descriptionText: "Starting the live video stream",
		eventType      : "VIDEO",
		displayed      : false,
		isStateChange  : true
	]
    
	sendEvent(event)
}
//END START VIDEO

def refresh() {
	log.trace "Refresh called. Settings -> $settings"
    state.cameraHost = null
    configure()
}

def configure() {
    log.trace "Configuration called"
    setUri()
}