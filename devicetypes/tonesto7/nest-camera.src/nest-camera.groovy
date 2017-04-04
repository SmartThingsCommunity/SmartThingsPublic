/**
 *  Nest Cam
<<<<<<< HEAD
 *	Authors: Anthony S. (@tonesto7), Ben W. (@desertblade), Eric S. (@E_Sch)
 *  A Big Thanks go out to Greg (@ghesp) for your help getting the video working.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this
 * software and associated documentation files (the "Software"), to deal in the Software
 * without restriction, including without limitation the rights to use, copy, modify,
 * merge, publish, distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to the following
 * conditions: The above copyright notice and this permission notice shall be included
 * in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A
 * PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF
 * CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE
 * OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
=======
 *	Authors: Anthony S. (@tonesto7)
 *	Contributors: Ben W. (@desertblade), Eric S. (@E_Sch)
 *  A Huge thanks goes out to Greg (@ghesp) for all of your help getting this working.
 *
 *	Copyright (C) 2017 Anthony S.
 * 	Licensing Info: Located at https://raw.githubusercontent.com/tonesto7/nest-manager/master/LICENSE.md
>>>>>>> origin/master
 */

import java.text.SimpleDateFormat

preferences { }

<<<<<<< HEAD
def devVer() { return "2.1.0" }
=======
def devVer() { return "2.5.1" }
>>>>>>> origin/master

metadata {
	definition (name: "${textDevName()}", author: "Anthony S.", namespace: "tonesto7") {
		capability "Sensor"
		capability "Switch"
		//capability "Motion Sensor"
		//capability "Sound Sensor"
		capability "Refresh"
		capability "Notification"
<<<<<<< HEAD
		//capability "Image Capture"
		//capability "Video Camera"
		//capability "Video Capture"
		capability "Health Check"
=======
		capability "Image Capture"
		capability "Video Camera"
		//capability "Video Capture"
		//capability "Health Check"
>>>>>>> origin/master

		command "refresh"
		command "poll"
		command "log", ["string","string"]
		command "streamingOn"
		command "streamingOff"
		command "chgStreaming"
<<<<<<< HEAD
=======
		command "cltLiveStreamStart"
		//command "testBtn"
>>>>>>> origin/master

		attribute "softwareVer", "string"
		attribute "lastConnection", "string"
		attribute "lastOnline", "string"
		attribute "lastUpdateDt", "string"
		attribute "activityZoneName", "string"
		attribute "isStreaming", "string"
		attribute "audioInputEnabled", "string"
		attribute "videoHistoryEnabled", "string"
<<<<<<< HEAD
=======
		attribute "minVideoHistoryHours", "string"
		attribute "maxVideoHistoryHours", "string"
>>>>>>> origin/master
		attribute "publicShareEnabled", "string"
		attribute "lastEventStart", "string"
		attribute "lastEventEnd", "string"
		attribute "apiStatus", "string"
		attribute "debugOn", "string"
		attribute "devTypeVer", "string"
		attribute "onlineStatus", "string"
	}

	simulator {
		// TODO: define status and reply messages here
	}

	tiles(scale: 2) {
		multiAttributeTile(name: "videoPlayer", type: "videoPlayer", width: 6, height: 4) {
<<<<<<< HEAD
			tileAttribute("device.switch5", key: "CAMERA_STATUS") {
				attributeState("on", label: "Active", icon: "st.camera.dlink-indoor", action: "vidOff", backgroundColor: "#79b821", defaultState: true)
				attributeState("off", label: "Inactive", icon: "st.camera.dlink-indoor", action: "vidOn", backgroundColor: "#ffffff")
				attributeState("restarting", label: "Connecting", icon: "st.camera.dlink-indoor", backgroundColor: "#53a7c0")
				attributeState("unavailable", label: "Unavailable", icon: "st.camera.dlink-indoor", action: "refresh.refresh", backgroundColor: "#F22000")
			}
=======
			tileAttribute("device.switch", key: "CAMERA_STATUS") {
				attributeState("on", label: "Active", icon: "st.camera.dlink-indoor", action: "switch.off", backgroundColor: "#79b821", defaultState: true)
				attributeState("off", label: "Inactive", icon: "st.camera.dlink-indoor", action: "switch.on", backgroundColor: "#ffffff")
				attributeState("restarting", label: "Connecting", icon: "st.camera.dlink-indoor", backgroundColor: "#53a7c0")
				attributeState("unavailable", label: "Unavailable", icon: "st.camera.dlink-indoor", action: "refresh.refresh", backgroundColor: "#F22000")
			}
			tileAttribute("device.errorMessage", key: "CAMERA_ERROR_MESSAGE") {
				attributeState("errorMessage", label: "", value: "", defaultState: true)
			}
>>>>>>> origin/master
			tileAttribute("device.camera", key: "PRIMARY_CONTROL") {
				attributeState("on", label: "Active", icon: "st.camera.dlink-indoor", backgroundColor: "#79b821", defaultState: true)
				attributeState("off", label: "Inactive", icon: "st.camera.dlink-indoor", backgroundColor: "#ffffff")
				attributeState("restarting", label: "Connecting", icon: "st.camera.dlink-indoor", backgroundColor: "#53a7c0")
				attributeState("unavailable", label: "Unavailable", icon: "st.camera.dlink-indoor", backgroundColor: "#F22000")
			}
			tileAttribute("device.startLive", key: "START_LIVE") {
<<<<<<< HEAD
				attributeState("live", action: "start", defaultState: true)
=======
				attributeState("live", action: "cltLiveStreamStart", defaultState: true)
>>>>>>> origin/master
			}
			tileAttribute("device.stream", key: "STREAM_URL") {
				attributeState("activeURL", defaultState: true)
			}
<<<<<<< HEAD
			tileAttribute("device.betaLogo", key: "BETA_LOGO") {
				attributeState("betaLogo", label: "", value: "", defaultState: true)
			}
		}

=======
			/*tileAttribute("device.betaLogo", key: "BETA_LOGO") {
				attributeState("betaLogo", label: "", value: "", defaultState: true)
			}*/
			/*tileAttribute("device.profile", key: "STREAM_QUALITY") {
				attributeState("1", label: "720p", action: "setProfileHD", defaultState: true)
				attributeState("2", label: "h360p", action: "setProfileSDH", defaultState: true)
				attributeState("3", label: "l360p", action: "setProfileSDL", defaultState: true)
			}*/
		}
>>>>>>> origin/master
		standardTile("isStreamingStatus", "device.isStreaming", width: 2, height: 2, decoration: "flat") {
			state("on", label: "Streaming", action: "chgStreaming", nextState: "updating", icon: "https://raw.githubusercontent.com/tonesto7/nest-manager/master/Images/App/camera_green_icon.png", backgroundColor: "#79b821")
			state("off", label: "Off", action: "chgStreaming", nextState: "updating", icon: "https://raw.githubusercontent.com/tonesto7/nest-manager/master/Images/App/camera_gray_icon.png", backgroundColor: "#ffffff")
			state("updating", label:"", icon: "https://raw.githubusercontent.com/tonesto7/nest-manager/master/Images/Devices/cmd_working.png")
<<<<<<< HEAD
=======
			state("offline", label: "Offline", icon: "https://raw.githubusercontent.com/tonesto7/nest-manager/master/Images/App/camera_red_icon.png", backgroundColor: "#F22000")
>>>>>>> origin/master
			state("unavailable", label: "Unavailable", icon: "https://raw.githubusercontent.com/tonesto7/nest-manager/master/Images/App/camera_red_icon.png", backgroundColor: "#F22000")
		}
		standardTile("isStreaming", "device.isStreaming", width: 2, height: 2, decoration: "flat") {
			state("on", action: "chgStreaming", nextState: "updating", icon: "https://raw.githubusercontent.com/tonesto7/nest-manager/master/Images/Devices/camera_stream_btn_icon.png")
			state("off", action: "chgStreaming", nextState: "updating", icon: "https://raw.githubusercontent.com/tonesto7/nest-manager/master/Images/Devices/camera_off_btn_icon.png")
<<<<<<< HEAD
			state("updating", label:"", icon: "https://raw.githubusercontent.com/tonesto7/nest-manager/master/Images/Devices/cmd_working.png")
			state("unavailable", icon: "https://raw.githubusercontent.com/tonesto7/nest-manager/master/Images/Devices/camera_offline_btn_icon.png")
		}
		carouselTile("cameraDetails", "device.image", width: 4, height: 4) { }
		/*standardTile("take", "device.image", width: 2, height: 2, canChangeIcon: false, inactiveLabel: true, canChangeBackground: false) {
			state "take", label: "Take", action: "Image Capture.take", icon: "st.camera.camera", backgroundColor: "#FFFFFF"
			//state "taking", label:'Taking', action: "", icon: "st.camera.take-photo", backgroundColor: "#53a7c0"
			//state "image", label: "Take", action: "Image Capture.take", icon: "st.camera.camera", backgroundColor: "#FFFFFF", nextState:"taking"
		}*/
=======
			state("updating", icon: "https://raw.githubusercontent.com/tonesto7/nest-manager/master/Images/Devices/cmd_working.png")
			state("offline", icon: "https://raw.githubusercontent.com/tonesto7/nest-manager/master/Images/App/camera_offline_btn_icon.png")
			state("unavailable", icon: "https://raw.githubusercontent.com/tonesto7/nest-manager/master/Images/Devices/camera_offline_btn_icon.png")
		}
		carouselTile("cameraDetails", "device.image", width: 6, height: 4) { }
		standardTile("take", "device.image", width: 2, height: 2, canChangeIcon: false, decoration: "flat", canChangeBackground: false) {
			state "take", action: "Image Capture.take", icon: "https://raw.githubusercontent.com/tonesto7/nest-manager/master/Images/Devices/camera_snapshot_icon.png", nextState:"taking"
			state "taking", icon: "https://raw.githubusercontent.com/tonesto7/nest-manager/master/Images/Devices/cmd_working.png"
			state "image", action: "Image Capture.take", icon: "https://raw.githubusercontent.com/tonesto7/nest-manager/master/Images/Devices/camera_snapshot_icon.png", nextState:"taking"
		}
>>>>>>> origin/master
		standardTile("motion", "device.motion", width: 2, height: 2) {
			state "active", label:'motion', icon:"st.motion.motion.active", backgroundColor:"#53a7c0"
			state "inactive", label:'no motion', icon:"st.motion.motion.inactive", backgroundColor:"#ffffff"
		}
		standardTile("sound", "device.sound", width: 2, height: 2) {
			state "detected", label:'Noise', icon:"st.sound.sound.detected", backgroundColor:"#53a7c0"
			state "not detected", label:'Quiet', icon:"st.sound.sound.notdetected", backgroundColor:"#ffffff"
		}
<<<<<<< HEAD
		standardTile("filler", "device.filler", width: 2, height: 2){
			state("default", label:'')
		}
=======
>>>>>>> origin/master
		valueTile("onlineStatus", "device.onlineStatus", width: 2, height: 1, wordWrap: true, decoration: "flat") {
			state("default", label: 'Network Status:\n${currentValue}')
		}
		valueTile("softwareVer", "device.softwareVer", inactiveLabel: false, width: 2, height: 1, decoration: "flat", wordWrap: true) {
			state("default", label: 'Firmware:\nv${currentValue}')
		}
		valueTile("lastConnection", "device.lastConnection", inactiveLabel: false, width: 3, height: 1, decoration: "flat", wordWrap: true) {
			state("default", label: 'Camera Last Checked-In:\n${currentValue}')
		}
		standardTile("refresh", "device.refresh", width:2, height:2, decoration: "flat") {
<<<<<<< HEAD
			state "default", label: 'refresh', action:"refresh.refresh", icon:"st.secondary.refresh-icon"
=======
			state "default", action:"refresh.refresh", icon:"https://raw.githubusercontent.com/tonesto7/nest-manager/master/Images/Devices/refresh_icon.png"
>>>>>>> origin/master
		}
		valueTile("lastUpdatedDt", "device.lastUpdatedDt", width: 4, height: 1, decoration: "flat", wordWrap: true) {
			state("default", label: 'Data Last Received:\n${currentValue}')
		}
		valueTile("devTypeVer", "device.devTypeVer",  width: 2, height: 1, decoration: "flat") {
			state("default", label: 'Device Type:\nv${currentValue}')
		}
		valueTile("apiStatus", "device.apiStatus", width: 2, height: 1, decoration: "flat", wordWrap: true) {
			state "Ok", label: "API Status:\nOK"
			state "Issue", label: "API Status:\nISSUE ", backgroundColor: "#FFFF33"
		}
		valueTile("debugOn", "device.debugOn", width: 2, height: 1, decoration: "flat") {
			state "true", 	label: 'Debug:\n${currentValue}'
			state "false", 	label: 'Debug:\n${currentValue}'
		}
<<<<<<< HEAD
		htmlTile(name:"devCamHtml", action: "getCamHtml", width: 6, height: 11, whitelist: ["raw.githubusercontent.com", "cdn.rawgit.com"])

		main "isStreamingStatus"
		//details(["devCamHtml", "isStreaming", "take", "refresh", "motion", "cameraDetails", "sound"])
		details(["devCamHtml", "isStreaming", "refresh"])
=======
		htmlTile(name:"devCamHtml", action: "getCamHtml", width: 6, height: 9, whitelist: ["raw.githubusercontent.com", "cdn.rawgit.com"])

		standardTile("test", "device.testBtn", width:2, height:2, decoration: "flat") {
			state "default", label: 'Test', action:"testBtn"
		}
		main "isStreamingStatus"
		//details(["devCamHtml", "isStreaming", "take", "refresh", "motion", "cameraDetails", "sound"])
		details(["videoPlayer", "isStreaming", "take", "refresh", "devCamHtml", "cameraDetails" ])
>>>>>>> origin/master
	}
}

mappings {
<<<<<<< HEAD
	path("/getCamHtml") {action: [GET: "getCamHtml"]}
}

def initialize() {
	log.info "Nest Camera ${textVersion()} ${textCopyright()}"
	poll()
=======
	path("/getInHomeURL") {action: [GET: "getInHomeURL"]}
	path("/getOutHomeURL") {action: [GET: "getOutHomeURL"]}
	path("/getCamHtml") {action: [GET: "getCamHtml"]}
}

def getInHomeURL() { return [InHomeURL: getCamPlaylistURL().toString()] }
def getOutHomeURL() { return [OutHomeURL: getCamPlaylistURL().toString()] }

def initialize() {
	Logger("initialize...")
	verifyHC()
	//poll()
>>>>>>> origin/master
}

void installed() {
	Logger("installed...")
<<<<<<< HEAD
    verifyHC()
=======
	initialize()
	state?.isInstalled = true
}

void updated() {
	Logger("updated...")
	initialize()
}

def getHcTimeout() {
	def to = state?.hcTimeout
	return ((to instanceof Integer) ? to.toInteger() : 60)*60
>>>>>>> origin/master
}

void verifyHC() {
	def val = device.currentValue("checkInterval")
<<<<<<< HEAD
	def timeOut = state?.hcTimeout ?: 35
	if(!val || val.toInteger() != timeOut) {
		Logger("verifyHC: Updating Device Health Check Interval to $timeOut")
		sendEvent(name: "checkInterval", value: 60 * timeOut.toInteger(), data: [protocol: "cloud"], displayed: false)
=======
	def timeOut = getHcTimeout()
	if(!val || val.toInteger() != timeOut) {
		Logger("verifyHC: Updating Device Health Check Interval to $timeOut")
		sendEvent(name: "checkInterval", value: timeOut, data: [protocol: "cloud"], displayed: false)
>>>>>>> origin/master
	}
}

def ping() {
	Logger("ping...")
<<<<<<< HEAD
	refresh()
=======
	keepAwakeEvent()
>>>>>>> origin/master
}

def parse(String description) {
	LogAction("Parsing '${description}'", "debug")
}

def poll() {
	Logger("polling parent...")
	parent.refresh(this)
}

def refresh() {
<<<<<<< HEAD
	Logger("refreshing parent...")
	poll()
}

=======
	//Logger("refreshing parent...")
	poll()
}

def cltLiveStreamStart() {
	//log.trace "video stream start()"
	def url = getCamPlaylistURL().toString()
	def imgUrl = "http://cdn.device-icons.smartthings.com/camera/dlink-indoor@2x.png"
	def dataLiveVideo = [OutHomeURL: url, InHomeURL: url, ThumbnailURL: imgUrl, cookie: [key: "key", value: "value"]]
	def evtData = groovy.json.JsonOutput.toJson(dataLiveVideo)
	sendEvent(name: "stream", value: evtData.toString(), data: evtData, descriptionText: "Starting the livestream", eventType: "VIDEO", displayed: false, isStateChange  : true)
}

>>>>>>> origin/master
// parent calls this method to queue data.
// goal is to return to parent asap to avoid execution timeouts

def generateEvent(Map eventData) {
	//log.trace("generateEvent Parsing data ${eventData}")
	state.eventData = eventData
	runIn(3, "processEvent", [overwrite: true] )
}

def processEvent() {
	if(state?.swVersion != devVer()) {
<<<<<<< HEAD
		installed()
=======
		initialize()
>>>>>>> origin/master
		state.swVersion = devVer()
	}
	def eventData = state?.eventData
	state.eventData = null
	//log.trace("processEvent Parsing data ${eventData}")
	try {
		LogAction("------------START OF API RESULTS DATA------------", "warn")
		if(eventData) {
			def results = eventData?.data
			//log.debug "results: $results"
			state.showLogNamePrefix = eventData?.logPrefix == true ? true : false
<<<<<<< HEAD
			state?.useMilitaryTime = eventData?.mt ? true : false
            state.clientBl = eventData?.clientBl == true ? true : false
			state.mobileClientType = eventData?.mobileClientType
			state.nestTimeZone = eventData?.tz ?: null
			isStreamingEvent(results?.is_streaming)
			videoHistEnabledEvent(results?.is_video_history_enabled?.toString())
			publicShareEnabledEvent(results?.is_public_share_enabled?.toString())
=======
			state.enRemDiagLogging = eventData?.enRemDiagLogging == true ? true : false
			if(eventData.hcTimeout && (state?.hcTimeout != eventData?.hcTimeout || !state?.hcTimeout)) {
				state.hcTimeout = eventData?.hcTimeout
				verifyHC()
			}
			state?.useMilitaryTime = eventData?.mt ? true : false
			state.clientBl = eventData?.clientBl == true ? true : false
			state.mobileClientType = eventData?.mobileClientType
			state.nestTimeZone = eventData?.tz ?: null

			publicShareUrlEvent(results?.public_share_url)
			onlineStatusEvent(results?.is_online?.toString())
			isStreamingEvent(results?.is_streaming)
			publicShareEnabledEvent(results?.is_public_share_enabled?.toString())
			videoHistEnabledEvent(results?.is_video_history_enabled?.toString())
>>>>>>> origin/master
			if(!results?.last_is_online_change) { lastCheckinEvent(null) }
			else { lastCheckinEvent(results?.last_is_online_change?.toString()) }
			if(eventData?.htmlInfo) { state?.htmlInfo = eventData?.htmlInfo }
			if(eventData?.allowDbException) { state?.allowDbException = eventData?.allowDbException = false ? false : true }
			apiStatusEvent(eventData?.apiIssues)
			debugOnEvent(eventData?.debug ? true : false)
<<<<<<< HEAD
			onlineStatusEvent(results?.is_online?.toString())
			audioInputEnabledEvent(results?.is_audio_input_enabled?.toString())
			softwareVerEvent(results?.software_version?.toString())
			if(results?.activity_zones) { state?.activityZones = results?.activity_zones }
			if(results?.public_share_url) { state?.public_share_url = results?.public_share_url }
=======
			audioInputEnabledEvent(results?.is_audio_input_enabled?.toString())
			softwareVerEvent(results?.software_version?.toString())
			if(results?.activity_zones) { state?.activityZones = results?.activity_zones }
>>>>>>> origin/master
			if(results?.snapshot_url) { state?.snapshot_url = results?.snapshot_url?.toString() }
			if(results?.app_url) { state?.app_url = results?.app_url?.toString() }
			if(results?.web_url) { state?.web_url = results?.web_url?.toString() }
			if(results?.last_event) {
				if(results?.last_event.start_time && results?.last_event.end_time) { lastEventDataEvent(results?.last_event) }
				//zoneMotionEvent(results?.last_event)
				//zoneSoundEvent(results?.last_event)
				if(results?.last_event?.activity_zone_ids) { activityZoneEvent(results?.last_event?.activity_zone_ids) }
				if(results?.last_event?.animated_image_url) { state?.animation_url = results?.last_event?.animated_image_url }
			}
			deviceVerEvent(eventData?.latestVer.toString())
<<<<<<< HEAD
=======
			vidHistoryTimeEvent()
>>>>>>> origin/master
			lastUpdatedEvent()
		}
		//log.debug "Device State Data: ${getState()}" //This will return all of the devices state data to the logs.
		return null
	}
	catch (ex) {
		log.error "generateEvent Exception:", ex
		exceptionDataHandler(ex.message, "generateEvent")
	}
}

def getStateSize()      { return state?.toString().length() }
def getStateSizePerc()  { return (int) ((stateSize/100000)*100).toDouble().round(0) }

def getDataByName(String name) {
	state[name] ?: device.getDataValue(name)
}

def getDeviceStateData() {
	return getState()
}

def getTimeZone() {
	def tz = null
	if (location?.timeZone) { tz = location?.timeZone }
	else { tz = state?.nestTimeZone ? TimeZone.getTimeZone(state?.nestTimeZone) : null }
	if(!tz) { Logger("getTimeZone: Hub or Nest TimeZone is not found ...", "warn") }
	return tz
}

def isCodeUpdateAvailable(newVer, curVer) {
	def result = false
	def latestVer
	def versions = [newVer, curVer]
	if(newVer != curVer) {
		latestVer = versions?.max { a, b ->
			def verA = a?.tokenize('.')
			def verB = b?.tokenize('.')
			def commonIndices = Math.min(verA?.size(), verB?.size())
			for (int i = 0; i < commonIndices; ++i) {
				//log.debug "comparing $numA and $numB"
				if (verA[i]?.toInteger() != verB[i]?.toInteger()) {
					return verA[i]?.toInteger() <=> verB[i]?.toInteger()
				}
			}
			verA?.size() <=> verB?.size()
		}
		result = (latestVer == newVer) ? true : false
	}
	//log.debug "type: $type | newVer: $newVer | curVer: $curVer | newestVersion: ${latestVer} | result: $result"
	return result
}

def deviceVerEvent(ver) {
	def curData = device.currentState("devTypeVer")?.value.toString()
	def pubVer = ver ?: null
	def dVer = devVer() ?: null
	def newData = isCodeUpdateAvailable(pubVer, dVer) ? "${dVer}(New: v${pubVer})" : "${dVer}" as String
	state?.devTypeVer = newData
	state?.updateAvailable = isCodeUpdateAvailable(pubVer, dVer)
	if(!curData?.equals(newData)) {
		Logger("UPDATED | Device Type Version is: (${newData}) | Original State: (${curData})")
		sendEvent(name: 'devTypeVer', value: newData, displayed: false)
	} else { LogAction("Device Type Version is: (${newData}) | Original State: (${curData})") }
}

def lastCheckinEvent(checkin) {
	def formatVal = state?.useMilitaryTime ? "MMM d, yyyy - HH:mm:ss" : "MMM d, yyyy - h:mm:ss a"
	def tf = new SimpleDateFormat(formatVal)
	tf.setTimeZone(getTimeZone())
	def lastConn = checkin ? tf?.format(Date.parse("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", checkin.toString())) : "Not Available"
	def lastChk = device.currentState("lastConnection")?.value
	state?.lastConnection = lastConn?.toString()
	if(!lastChk.equals(lastConn?.toString())) {
		Logger("UPDATED | Last Nest Check-in was: (${lastConn}) | Original State: (${lastChk})")
		sendEvent(name: 'lastConnection', value: lastConn?.toString(), displayed: state?.showProtActEvts, isStateChange: true)
	} else { LogAction("Last Nest Check-in was: (${lastConn}) | Original State: (${lastChk})") }
}

def lastOnlineEvent(dt) {
	def lastOnlVal = device.currentState("lastOnline")?.value
	def formatVal = state?.useMilitaryTime ? "MMM d, yyyy - HH:mm:ss" : "MMM d, yyyy - h:mm:ss a"
	def tf = new SimpleDateFormat(formatVal)
	tf.setTimeZone(getTimeZone())
	def lastOnl = !dt ? "Nothing To Show..." : tf?.format(Date.parse("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", dt.toString()))
	state?.lastOnl = lastOnl
	if(!lastOnlVal.equals(lastOnl?.toString())) {
		Logger("UPDATED | Last Online was: (${lastOnl}) | Original State: (${lastOnlVal})")
		sendEvent(name: 'lastOnline', value: lastOnl, displayed: false, isStateChange: true)
	} else { LogAction("Last Manual Test was: (${lastOnl}) | Original State: (${lastOnlVal})") }
}

<<<<<<< HEAD
=======
def onlineStatusEvent(online) {
	//log.trace "onlineStatusEvent($online)"
	def isOn = device.currentState("onlineStatus")?.value
	if(state?.camApiServerData && state?.camApiServerData?.items?.is_online[0]) { online = state?.camApiServerData?.items?.is_online[0] }
	def val = online.toString() == "true" ? "online" : "offline"
	state?.onlineStatus = val.toString().capitalize()
	state?.isOnline = (val == "online")
	// log.debug "onlineStatus: ${state?.isOnline} | val: $online"
	if(!isOn.equals(val.toString().capitalize())) {
		Logger("UPDATED | Online Status is: (${val.toString().capitalize()}) | Original State: (${isOn})")
		sendEvent(name: "onlineStatus", value: val.toString().capitalize(), descriptionText: "Online Status is: ${val.toString().capitalize()}", displayed: true, isStateChange: true, state: val.toString().capitalize())
	} else { LogAction("Online Status is: (${val.toString().capitalize()}) | Original State: (${isOn})") }
}

>>>>>>> origin/master
def isStreamingEvent(isStreaming) {
	//log.trace "isStreamingEvent($isStreaming)..."
	def isOn = device.currentState("isStreaming")?.value
	def isOnline = device.currentState("onlineStatus")?.value
<<<<<<< HEAD
	def val = (isStreaming.toBoolean() == true) ? "on" : (!isOnline == "Online" ? "unavailable" : "off")
	state?.isStreaming = val == "on" ? true : false
	if(!isOn.equals(val)) {
		Logger("UPDATED | Streaming Video is: (${val}) | Original State: (${isOn})")
		sendEvent(name: "isStreaming", value: val, descriptionText: "Streaming Video is: ${val}", displayed: true, isStateChange: true, state: val)
	} else { LogAction("Streaming Video Status is: (${val}) | Original State: (${isOn})") }
=======
	if(state?.camApiServerData && state?.camApiServerData?.items?.is_streaming[0]) { isStreaming = state?.camApiServerData?.items?.is_streaming[0] }
	def val = (isStreaming.toString() == "true") ? "on" : (isOnline.toString() != "Online" ? "offline" : "off")
	//log.debug "isStreaming: $val | isOnline: $isOnline"
	state?.isStreaming = (val == "on") ? true : false
	if(!isOn.equals(val)) {
		Logger("UPDATED | Camera Live Video Streaming is: (${val}) | Original State: (${isOn})")
		sendEvent(name: "isStreaming", value: val, descriptionText: "Camera Live Video Streaming is: ${val}", displayed: true, isStateChange: true, state: val)
		sendEvent(name: "switch", value: (val == "on" ? val : "off"))
	} else { LogAction("Camera Live Video Streaming is: (${val}) | Original State: (${isOn})") }
>>>>>>> origin/master
}

def audioInputEnabledEvent(on) {
	def isOn = device.currentState("audioInputEnabled")?.value
	def val = (on.toString() == "true") ? "Enabled" : "Disabled"
	state?.audioInputEnabled = val
	if(!isOn.equals(val)) {
		Logger("UPDATED | Audio Input Status is: (${val}) | Original State: (${isOn})")
		sendEvent(name: "audioInputEnabled", value: val, descriptionText: "Audio Input Status is: ${val}", displayed: true, isStateChange: true, state: val)
	} else { LogAction("Audio Input Status is: (${val}) | Original State: (${isOn})") }
}

def videoHistEnabledEvent(on) {
	def isOn = device.currentState("videoHistoryEnabled")?.value
	def val = (on.toString() == "true") ? "Enabled" : "Disabled"
	state?.videoHistoryEnabled = val
	if(!isOn.equals(val)) {
		Logger("UPDATED | Video History Status is: (${val}) | Original State: (${isOn})")
		sendEvent(name: "videoHistoryEnabled", value: val, descriptionText: "Video History Status is: ${val}", displayed: true, isStateChange: true, state: val)
	} else { LogAction("Video History Status is: (${val}) | Original State: (${isOn})") }
}

def publicShareEnabledEvent(on) {
	def isOn = device.currentState("publicShareEnabled")?.value
	def val = on ? "Enabled" : "Disabled"
	state?.publicShareEnabled = val
	if(!isOn.equals(val)) {
		Logger("UPDATED | Public Sharing Status is: (${val}) | Original State: (${isOn})")
		sendEvent(name: "publicShareEnabled", value: val, descriptionText: "Public Sharing Status is: ${val}", displayed: true, isStateChange: true, state: val)
	} else { LogAction("Public Sharing Status is: (${val}) | Original State: (${isOn})") }
}

def softwareVerEvent(ver) {
	def verVal = device.currentState("softwareVer")?.value
	state?.softwareVer = ver
	if(!verVal.equals(ver)) {
		Logger("UPDATED | Firmware Version: (${ver}) | Original State: (${verVal})")
		sendEvent(name: 'softwareVer', value: ver, descriptionText: "Firmware Version is now v${ver}", displayed: false)
	} else { LogAction("Firmware Version: (${ver}) | Original State: (${verVal})") }
}

def lastEventDataEvent(data) {
<<<<<<< HEAD
=======
	//log.trace "lastEventDataEvent($data)"
>>>>>>> origin/master
	def tf = new SimpleDateFormat("E MMM dd HH:mm:ss z yyyy")
		tf.setTimeZone(getTimeZone())
	def curStartDt = device?.currentState("lastEventStart")?.value ? tf?.format(Date.parse("E MMM dd HH:mm:ss z yyyy", device?.currentState("lastEventStart")?.value.toString())) : null
	def curEndDt = device?.currentState("lastEventEnd")?.value ? tf?.format(Date.parse("E MMM dd HH:mm:ss z yyyy", device?.currentState("lastEventEnd")?.value.toString())) : null
	def newStartDt = data?.start_time ? tf.format(Date.parse("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", data?.start_time.toString())) : "Not Available"
	def newEndDt = data?.end_time ? tf.format(Date.parse("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", data?.end_time.toString())) : "Not Available"
<<<<<<< HEAD
	def hasPerson = data?.has_person == "true" ? true : false
	//log.debug "curStartDt: $curStartDt | curEndDt: $curEndDt || newStartDt: $newStartDt | newEndDt: $newEndDt"
	state.lastEventStartDt = formatDt(Date.parse("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", data?.start_time.toString()), true)
	state.lastEventEndDt = formatDt(Date.parse("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", data?.end_time.toString()), true)
	state?.lastEventData = data

	if((curStartDt != newStartDt || curEndDt != newEndDt) && hasPerson) {
		Logger("UPDATED | Last Event Start Time: (${newStartDt}) | Original State: (${curStartDt})")
		sendEvent(name: 'lastEventStart', value: newStartDt, descriptionText: "Last Event Start is now ${newStartDt}", displayed: false)
		Logger("UPDATED | Last Event End Time: (${newEndDt}) | Original State: (${curEndDt})")
		sendEvent(name: 'lastEventEnd', value: newEndDt, descriptionText: "Last Event End is now ${newEndDt}", displayed: false)
=======

	def hasPerson = data?.has_person ? data?.has_person?.toBoolean() : false
	def hasMotion = data?.has_motion ? data?.has_motion?.toBoolean() : false
	def hasSound = data?.has_sound ? data?.has_sound?.toBoolean() : false

	//log.debug "curStartDt: $curStartDt | curEndDt: $curEndDt || newStartDt: $newStartDt | newEndDt: $newEndDt"

	state.lastEventDate = formatDt2(Date.parse("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", data?.start_time.toString()), "MMMMM d, yyyy").toString()
	state.lastEventTime = "${formatDt2(Date.parse("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", data?.start_time.toString()), "h:mm:ssa")} to ${formatDt2(Date.parse("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", data?.end_time.toString()), "h:mm:ssa")}"
	if(state?.lastEventData) { state.lastEventData == null }

	if(!state?.lastCamEvtData || (curStartDt != newStartDt || curEndDt != newEndDt) && (hasPerson || hasMotion || hasSound)) {
		Logger("UPDATED | Last Event Start Time: (${newStartDt}) | Original State: (${curStartDt})")
		sendEvent(name: 'lastEventStart', value: newStartDt, descriptionText: "Last Event Start is ${newStartDt}", displayed: false)
		Logger("UPDATED | Last Event End Time: (${newEndDt}) | Original State: (${curEndDt})")
		sendEvent(name: 'lastEventEnd', value: newEndDt, descriptionText: "Last Event End is ${newEndDt}", displayed: false)
		state.lastCamEvtData = ["startDt":newStartDt, "endDt":newEndDt, "hasMotion":hasMotion, "hasSound":hasSound, "hasPerson":hasPerson, "actZones":(data?.activity_zone_ids ?: null)]
>>>>>>> origin/master
	} else {
		LogAction("Last Event Start Time: (${newStartDt}) | Original State: (${curStartDt})")
		LogAction("Last Event End Time: (${newEndDt}) | Original State: (${curEndDt})")
	}
}

def zoneMotionEvent(data) {
	def tf = new SimpleDateFormat("E MMM dd HH:mm:ss z yyyy")
		tf.setTimeZone(getTimeZone())
	def nowDt = tf.format(new Date())
	def isMotion = device.currentState("motion")?.stringValue
	def isBtwn = false
	if(data?.start_time && data?.end_time) {
		def newStartDt = tf.format(Date.parse("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", data?.start_time.toString())) ?: "Not Available"
		def newEndDt = tf.format(Date.parse("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", data?.end_time.toString())) ?: "Not Available"
		isBtwn = (newStartDt && newEndDt) ? false :  isTimeBetween(newStartDt, newEndDt, nowDt, getTimeZone())
	}
	def val = ((data?.has_motion == "true") && isBtwn) ? "active" : "inactive"
	if(!isMotion.equals(val)) {
		Logger("UPDATED | Motion Sensor is: (${val}) | Original State: (${isMotion})")
		sendEvent(name: "motion", value: val, descriptionText: "Motion Sensor is: ${val}", displayed: true, isStateChange: true, state: val)
	} else { LogAction("Motion Sensor is: (${val}) | Original State: (${isMotion})") }
}

def zoneSoundEvent(data) {
	def tf = new SimpleDateFormat("E MMM dd HH:mm:ss z yyyy")
		tf.setTimeZone(getTimeZone())
	def nowDt = tf.format(new Date())
	def isSound = device.currentState("sound")?.stringValue
	def isBtwn = false
	if(data?.start_time && data?.end_time) {
		def newStartDt = tf.format(Date.parse("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", data?.start_time.toString())) ?: "Not Available"
		def newEndDt = tf.format(Date.parse("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", data?.end_time.toString())) ?: "Not Available"
		isBtwn = (newStartDt && newEndDt) ? false :  isTimeBetween(newStartDt, newEndDt, nowDt, getTimeZone())
	}
	def val = ((date?.has_sound == "true") && isBtwn) ? "detected" : "not detected"
	if(!isSound.equals(val)) {
		Logger("UPDATED | Sound Sensor is now: (${val}) | Original State: (${isSound})")
		sendEvent(name: "sound", value: val, descriptionText: "Sound Sensor is: ${val}", displayed: true, isStateChange: true, state: val)
	} else { LogAction("Sound Sensor is: (${val}) | Original State: (${isSound})") }
}

def activityZoneEvent(zones) {
	//log.trace "activityZoneEvent($zones)..."
}

def debugOnEvent(debug) {
	def val = device.currentState("debugOn")?.value
	def dVal = debug ? "On" : "Off"
	state?.debugStatus = dVal
	state?.debug = debug.toBoolean() ? true : false
	if(!val.equals(dVal)) {
		Logger("UPDATED | debugOn: (${dVal}) | Original State: (${val})")
		sendEvent(name: 'debugOn', value: dVal, displayed: false)
	} else { LogAction("debugOn: (${dVal}) | Original State: (${val})") }
}

def apiStatusEvent(issue) {
	def curStat = device.currentState("apiStatus")?.value
	def newStat = issue ? "Issues" : "Ok"
	state?.apiStatus = newStat
	if(!curStat.equals(newStat)) {
		Logger("UPDATED | API Status is: (${newStat}) | Original State: (${curStat})")
		sendEvent(name: "apiStatus", value: newStat, descriptionText: "API Status is: ${newStat}", displayed: true, isStateChange: true, state: newStat)
	} else { LogAction("API Status is: (${newStat}) | Original State: (${curStat})") }
}

<<<<<<< HEAD
def lastUpdatedEvent() {
	def now = new Date()
	def formatVal = state?.useMilitaryTime ? "MMM d, yyyy - HH:mm:ss" : "MMM d, yyyy - h:mm:ss a"
	def tf = new SimpleDateFormat(formatVal)
	tf.setTimeZone(getTimeZone())
	def lastDt = "${tf?.format(now)}"
	def lastUpd = device.currentState("lastUpdatedDt")?.value
	state?.lastUpdatedDt = lastDt?.toString()
	if(!lastUpd.equals(lastDt?.toString())) {
		LogAction("Last Parent Refresh time: (${lastDt}) | Previous Time: (${lastUpd})")
		sendEvent(name: 'lastUpdatedDt', value: lastDt?.toString(), displayed: false, isStateChange: true)
	}
}

def onlineStatusEvent(online) {
	def isOn = device.currentState("onlineStatus")?.value
	def val = online ? "Online" : "Offline"
	state?.onlineStatus = val
	if(!isOn.equals(val)) {
		Logger("UPDATED | Online Status is: (${val}) | Original State: (${isOn})")
		sendEvent(name: "onlineStatus", value: val, descriptionText: "Online Status is: ${val}", displayed: state?.showProtActEvts, isStateChange: true, state: val)
	} else { LogAction("Online Status is: (${val}) | Original State: (${isOn})") }
}

def getPublicVideoId() {
	try {
		if(state?.public_share_url) {
			def vidId = state?.public_share_url.tokenize('/')
			return vidId[3].toString()
		}
	} catch (ex) {
		log.error "getPublicVideoId Exception:", ex
	}
}

=======
def lastUpdatedEvent(sendEvt=false) {
	def now = new Date()
	def formatVal = state.useMilitaryTime ? "MMM d, yyyy - HH:mm:ss" : "MMM d, yyyy - h:mm:ss a"
	def tf = new SimpleDateFormat(formatVal)
		tf.setTimeZone(getTimeZone())
	def lastDt = "${tf?.format(now)}"
	state?.lastUpdatedDt = lastDt?.toString()
	state?.lastUpdatedDtFmt = formatDt(now)
	if(sendEvt) {
		LogAction("Last Parent Refresh time: (${lastDt}) | Previous Time: (${lastUpd})")
		sendEvent(name: 'lastUpdatedDt', value: formatDt(now)?.toString(), displayed: false, isStateChange: true)
	}
}

def keepAwakeEvent() {
	def lastDt = state?.lastUpdatedDtFmt
	if(lastDt) {
		def ldtSec = getTimeDiffSeconds(lastDt)
		log.debug "ldtSec: $ldtSec"
		if(ldtSec < 1900) {
			lastUpdatedEvent(true)
		} else { refresh() }
	} else { refresh() }
}

def vidHistoryTimeEvent() {
	if(!state?.camApiServerData) { return }
	def camData = state?.camApiServerData
	def newMin = (camData?.items?.hours_of_free_tier_history[0] > 3 ? camData?.items?.hours_of_free_tier_history[0] : 3)
	def newMax = (camData?.items?.hours_of_recording_max[0] > 3 ? camData?.items?.hours_of_recording_max[0] : 3)
	def curMin = device.currentState("minVideoHistoryHours")?.value
	def curMax = device.currentState("maxVideoHistoryHours")?.value
	state?.minVideoHistoryHours = newMin
	state?.maxVideoHistoryHours = newMax
	if((curMin.toString() != newMin.toString()) || (curMax.toString() != newMax.toString())) {
		Logger("UPDATED | Video Recording History Hours is Now: (Minimum: ${newMin} hours | Maximum: ${newMax} hours) | Original State: (Minimum: ${curMin} | Maximum: ${curMax})")
		sendEvent(name: "minVideoHistoryHours", value: newMin, descriptionText: "Minimum Video Recording History Hours is Now: (${newMin} hours)", displayed: false, isStateChange: true, state: newMin)
		sendEvent(name: "maxVideoHistoryHours", value: newMax, descriptionText: "Maximum Video Recording History Hours is Now: (${newMax} hours)", displayed: false, isStateChange: true, state: newMax)
	} else { LogAction("Video Recording History Hours is Now: (Minimum: ${newMin} hours | Maximum: ${newMax} hours) | Original State: (Minimum: ${curMin} | Maximum: ${curMax})") }
}

def publicShareUrlEvent(url) {
	//log.trace "publicShareUrlEvent($url)"
	if(url) {
		if(!state?.public_share_url) { state?.public_share_url = url }
		def pubVidId = getPublicVidID()
		def lastVidId = state?.lastPubVidId
		if(lastVidId == null || lastVidId.toString() != pubVidId.toString()) {
			state?.public_share_url = url
			state?.lastPubVidId = pubVidId
		}
		if(!state?.camUUID) { getCamUUID(pubVidId) }
		if(state?.camUUID) {
			def camData = getCamApiServerData(state?.camUUID)
			if(camData && state?.lastCamApiServerData != camData) { state?.lastCamApiServerData = camData }
		}
	} else {
		if(state?.pubVidId || state?.lastPubVidId || state?.camUUID || state?.camApiServerData) {
			state?.pubVidId = null
			state?.lastPubVidId = null
			state?.camUUID = null
			state?.camApiServerData = null
			Logger("Cleared Cached Camera State Info because Sharing has been disabled or the Public ID is no longer available...", "warn")
		}
	}
}

def getPublicVidID() {
	def id = null
	if(!state?.pubVidId && state?.public_share_url) {
		id = state?.public_share_url.tokenize('/')[3].toString()
		state?.pubVidId = id
	} else {
		id = state?.pubVidId
	}
	return id
}

def getRecTimeDesc(val) {
	def result = null
	if(val && val instanceof Integer) {
		if(val.toInteger() > 24) {
			def nVal = (val/24).toDouble().round(0)
			result = "${nVal.toInteger()} days"
		} else {
			result = "${val} hours"
		}
	}
	return result
}
>>>>>>> origin/master

/************************************************************************************************
|									DEVICE COMMANDS     										|
*************************************************************************************************/
<<<<<<< HEAD
def chgStreaming() {
	def cur = latestValue("isStreaming").toString()
	if(cur == "on" || cur == "unavailable" || !cur) {
		streamingOn(true)
	} else {
		streamingOff(true)
	}
}

def streamingOn(manChg=false) {
=======
void chgStreaming() {
	def cur = device?.currentState("isStreaming")?.value.toString()
	if(cur == "on" || cur == "unavailable" || !cur) {
		streamingOff(true)
	} else {
		streamingOn(true)
	}
}

void streamingOn(manChg=false) {
>>>>>>> origin/master
	try {
		log.trace "streamingOn..."
		if(parent?.setCamStreaming(this, "true")) {
			sendEvent(name: "isStreaming", value: "on", descriptionText: "Streaming Video is: on", displayed: true, isStateChange: true, state: "on")
<<<<<<< HEAD
=======
			sendEvent(name: "switch", value: "on")
>>>>>>> origin/master
			if(manChg) { incManStreamChgCnt() }
			else { incProgStreamChgCnt() }
		}

	} catch (ex) {
		log.error "streamingOn Exception:", ex
		exceptionDataHandler(ex.message, "streamingOn")
	}
}

<<<<<<< HEAD
def streamingOff(manChg=false) {
=======
void streamingOff(manChg=false) {
>>>>>>> origin/master
	try {
		log.trace "streamingOff..."
		if(parent?.setCamStreaming(this, "false")) {
			sendEvent(name: "isStreaming", value: "off", descriptionText: "Streaming Video is: off", displayed: true, isStateChange: true, state: "off")
<<<<<<< HEAD
=======
			sendEvent(name: "switch", value: "off")
>>>>>>> origin/master
			if(manChg) { incManStreamChgCnt() }
			else { incProgStreamChgCnt() }
		}
	} catch (ex) {
		log.error "streamingOff Exception:", ex
		exceptionDataHandler(ex.message, "streamingOff")
	}
}

<<<<<<< HEAD
def on() {
	streamingOn()
}

def off() {
	streamingOff()
}

def take() {
	try {
		def img = getImgBase64(state?.snapshot_url,'jpeg')
		//log.debug "img: $img"
		def list = state?.last5ImageData ?: []
		//log.debug "listIn: $list (${list?.size()})"
		def listSize = 4
		if(list?.size() < listSize) {
			list.push(img)
		}
		else if (list?.size() > listSize) {
			def nSz = (list?.size()-listSize) + 1
			//log.debug ">listSize: ($nSz)"
			def nList = list?.drop(nSz)
			//log.debug "nListIn: $list"
			nList?.push(img)
			//log.debug "nListOut: $nList"
			list = nList
		}
		else if (list?.size() == listSize) {
			def nList = list?.drop(1)
			nList?.push(img)
			list = nList
		}
		log.debug "img_list_size: ${list?.size()}"
		if(list) { state?.last5ImageData = list }
	}
	catch (ex) {
		log.error "take Exception:", ex
		exceptionDataHandler(ex.message, "take")
	}
=======
void on() {
	streamingOn()
}

void off() {
	streamingOff()
}

void take() {
	takePicture()
}

private getPictureName() {
	def pictureUuid = java.util.UUID.randomUUID().toString().replaceAll('-', '')
	getCamUUID(getPublicVidID()) + "_$pictureUuid" + ".jpg"
}

private getImageWidth() {
	return 1280
}

private takePicture() {
	try {
		if(state?.isOnline) {
			def imageBytes
			def params = [
				uri: state?.snapshot_url,
				requestContentType: "application/x-www-form-urlencoded"
			]
			httpGet(params) { resp ->
				imageBytes = resp?.data
				if (imageBytes) {
					storeImage(getPictureName(), imageBytes)
					return true
				}
			}
		}
	} catch (ex) {
		log.error "takePicture Exception: $ex"
		exceptionDataHandler(ex.message, "takePicture")
	}
	return false
>>>>>>> origin/master
}

/************************************************************************************************
|							EXCEPTION HANDLING & LOGGING FUNCTIONS								|
*************************************************************************************************/

void Logger(msg, logType = "debug") {
	def smsg = state?.showLogNamePrefix ? "${device.displayName}: ${msg}" : "${msg}"
	switch (logType) {
		case "trace":
			log.trace "${smsg}"
			break
		case "debug":
			log.debug "${smsg}"
			break
		case "info":
			log.info "${smsg}"
			break
		case "warn":
			log.warn "${smsg}"
			break
		case "error":
			log.error "${smsg}"
			break
		default:
			log.debug "${smsg}"
			break
	}
<<<<<<< HEAD
=======
	if(state?.enRemDiagLogging) {
		parent.saveLogtoRemDiagStore(smsg, logType, "Camera DTH")
	}
>>>>>>> origin/master
}

// Local Application Logging
void LogAction(msg, logType = "debug") {
	if(state?.debug) {
		Logger(msg, logType)
	}
}

 //This will Print logs from the parent app when added to parent method that the child calls
def log(message, level = "trace") {
	def smsg = "PARENT_Log>> " + message
	LogAction(smsg, level)
	return null // always child interface call with a return value
}

def exceptionDataHandler(msg, methodName) {
	if(state?.allowDbException == false) {
		return
	} else {
		if(msg && methodName) {
			def msgString = "${msg}"
			parent?.sendChildExceptionData("camera", devVer(), msgString, methodName)
		}
	}
}

def incHtmlLoadCnt() 		{ state?.htmlLoadCnt = (state?.htmlLoadCnt ? state?.htmlLoadCnt.toInteger()+1 : 1) }
def incManStreamChgCnt() 	{ state?.manStreamChgCnt = (state?.manStreamChgCnt ? state?.manStreamChgCnt.toInteger()+1 : 1) }
def incProgStreamChgCnt() 	{ state?.progStreamChgCnt = (state?.progStreamChgCnt ? state?.progStreamChgCnt.toInteger()+1 : 1) }
def incVideoBtnTapCnt()		{ state?.videoBtnTapCnt = (state?.videoBtnTapCnt ? state?.videoBtnTapCnt.toInteger()+1 : 1); return ""; }
def incImageBtnTapCnt()		{ state?.imageBtnTapCnt = (state?.imageBtnTapCnt ? state?.imageBtnTapCnt.toInteger()+1 : 1); return ""; }
def incEventBtnTapCnt()		{ state?.eventBtnTapCnt = (state?.eventBtnTapCnt ? state?.eventBtnTapCnt.toInteger()+1 : 1); return ""; }
def incInfoBtnTapCnt()		{ state?.infoBtnTapCnt = (state?.infoBtnTapCnt ? state?.infoBtnTapCnt.toInteger()+1 : 1); return ""; }

def getMetricCntData() {
	return 	[
<<<<<<< HEAD
			camManStrChgCnt:(state?.manStreamChgCnt ?: 0), camProgStrChgCnt:(state?.progStreamChgCnt ?: 0), camHtmlLoadedCnt:(state?.htmlLoadCnt ?: 0)//,
			//camVidBtnTapCnt:(state?.videoBtnTapCnt ?: 0), camImgBtnTapCnt:(state?.imageBtnTapCnt ?: 0), camEvtBtnTapCnt:(state?.eventBtnTapCnt ?: 0),
			//camInfoBtnCnt:(state?.infoBtnTapCnt ?: 0)
			]
}

=======
		camManStrChgCnt:(state?.manStreamChgCnt ?: 0), camProgStrChgCnt:(state?.progStreamChgCnt ?: 0), camHtmlLoadedCnt:(state?.htmlLoadCnt ?: 0)//,
		//camVidBtnTapCnt:(state?.videoBtnTapCnt ?: 0), camImgBtnTapCnt:(state?.imageBtnTapCnt ?: 0), camEvtBtnTapCnt:(state?.eventBtnTapCnt ?: 0),
		//camInfoBtnCnt:(state?.infoBtnTapCnt ?: 0)
	]
}

def testBtn() {

}
>>>>>>> origin/master
/************************************************************************************************
|										OTHER METHODS     										|
*************************************************************************************************/

<<<<<<< HEAD
=======
def getDtNow() {
	def now = new Date()
	return formatDt(now)
}

>>>>>>> origin/master
def formatDt(dt, mdy = false) {
	//log.trace "formatDt($dt, $mdy)..."
	def formatVal = mdy ? (state?.useMilitaryTime ? "MMM d, yyyy - HH:mm:ss" : "MMM d, yyyy - h:mm:ss a") : "E MMM dd HH:mm:ss z yyyy"
	def tf = new SimpleDateFormat(formatVal)
	if(getTimeZone()) { tf.setTimeZone(getTimeZone()) }
	else {
		Logger("SmartThings TimeZone is not found or is not set... Please Try to open your ST location and Press Save...")
	}
	return tf.format(dt)
}

<<<<<<< HEAD
=======
def formatDt2(dt, fmt=null) {
	//log.trace "formatDt($dt, $mdy)..."
	def tf = new SimpleDateFormat(fmt)
	if(getTimeZone()) { tf.setTimeZone(getTimeZone()) }
	else {
		Logger("SmartThings TimeZone is not found or is not set... Please Try to open your ST location and Press Save...")
	}
	return tf.format(dt)
}

>>>>>>> origin/master
def epochToTime(tm) {
	def tf = new SimpleDateFormat("h:mm a")
		tf?.setTimeZone(getTimeZone())
	return tf.format(tm)
}

def isTimeBetween(start, end, now, tz) {
	def startDt = Date.parse("E MMM dd HH:mm:ss z yyyy", start).getTime()
	def endDt = Date.parse("E MMM dd HH:mm:ss z yyyy", end).getTime()
	def nowDt = Date.parse("E MMM dd HH:mm:ss z yyyy", now).getTime()
	def result = false
	if(nowDt > startDt && nowDt < endDt) {
		result = true
	}
	//def result = timeOfDayIsBetween(startDt, endDt, nowDt, tz) ? true : false
	return result
}

def getImgBase64(url,type) {
	def params = [
		uri: url,
		contentType: 'image/$type'
	]
	httpGet(params) { resp ->
		if(resp.data) {
			def respData = resp?.data
			ByteArrayOutputStream bos = new ByteArrayOutputStream()
			int len
			int size = 3072
			byte[] buf = new byte[size]
			while ((len = respData.read(buf, 0, size)) != -1)
				bos.write(buf, 0, len)
			buf = bos.toByteArray()
			String s = buf?.encodeBase64()
			return s ? "data:image/${type};base64,${s.toString()}" : null
		}
	}
}

def getFileBase64(url,preType,fileType) {
	def params = [
		uri: url,
		contentType: '$preType/$fileType'
	]
	httpGet(params) { resp ->
		if(resp.data) {
			def respData = resp?.data
			ByteArrayOutputStream bos = new ByteArrayOutputStream()
			int len
			int size = 4096
			byte[] buf = new byte[size]
			while ((len = respData.read(buf, 0, size)) != -1)
				bos.write(buf, 0, len)
			buf = bos.toByteArray()
			//log.debug "buf: $buf"
			String s = buf?.encodeBase64()
			//log.debug "resp: ${s}"
			return s ? "data:${preType}/${fileType};base64,${s.toString()}" : null
		}
	}
}

def getImg(imgName) {
	return imgName ? "https://cdn.rawgit.com/tonesto7/nest-manager/master/Images/Devices/$imgName" : ""
}

def getCSS(url = null){
	def params = [
		uri: (!url ? "https://raw.githubusercontent.com/desertblade/ST-HTMLTile-Framework/master/css/smartthings.css" : url?.toString()),
		contentType: "text/css"
	]
	httpGet(params)  { resp ->
		return resp?.data.text
	}
}

def getJS(url){
	def params = [
		uri: url.toString(),
		contentType: 'text/javascript'
	]
	httpGet(params)  { resp ->
		//log.debug "JS Resp: ${resp?.data}"
		return resp?.data.text
	}
}

def getCssData() {
	def cssData = null
<<<<<<< HEAD
	if(htmlInfo?.cssUrl && htmlInfo?.cssVer) {
		if(state?.cssData) {
			if (state?.cssVer?.toInteger() == htmlInfo?.cssVer?.toInteger()) {
				LogAction("getCssData: CSS Data is Current | Loading Data from State...")
				cssData = state?.cssData
			} else if (state?.cssVer?.toInteger() < htmlInfo?.cssVer?.toInteger()) {
				LogAction("getCssData: CSS Data is Outdated | Loading Data from Source...")
				cssData = getFileBase64(htmlInfo.cssUrl, "text", "css")
				state.cssData = cssData
				state?.cssVer = htmlInfo?.cssVer
			}
		} else {
			LogAction("getCssData: CSS Data is Missing | Loading Data from Source...")
			cssData = getFileBase64(htmlInfo.cssUrl, "text", "css")
			state?.cssData = cssData
			state?.cssVer = htmlInfo?.cssVer
		}
	} else {
		LogAction("getCssData: No Stored CSS Data Found for Device... Loading for Static URL...")
=======
	def htmlInfo = state?.htmlInfo
	state?.cssData = null
	if(htmlInfo?.cssUrl && htmlInfo?.cssVer) {
		//LogAction("getCssData: CSS Data is Missing | Loading Data from Source...")
		cssData = getFileBase64(htmlInfo.cssUrl, "text", "css")
		state?.cssData = cssData
		state?.cssVer = htmlInfo?.cssVer
	} else {
		//LogAction("getCssData: No Stored CSS Data Found for Device... Loading for Static URL...")
>>>>>>> origin/master
		cssData = getFileBase64(cssUrl(), "text", "css")
	}
	return cssData
}

<<<<<<< HEAD
def cssUrl() { return "https://raw.githubusercontent.com/desertblade/ST-HTMLTile-Framework/master/css/smartthings.css" }

//this scrapes the public nest cam page for its unique id for using in render html tile
def getCamUUID(pubVidId) {
	try {
		if(pubVidId) {
			def params = [
				uri: "https://opengraph.io/api/1.0/site/https://video.nest.com/live/${pubVidId}"
			]
			httpGet(params) { resp ->
				def uuid = (resp?.data?.hybridGraph.image =~ /uuid=(\w*)/)[0][1]
				//log.debug "uuid: $uuid"
				return uuid ?: null
			}
		} else { Logger("getCamUUID PublicVideoId is missing....", "warn") }
	} catch (ex) {
		log.error "getCamUUID Exception:", ex
=======
def cssUrl()	 { return "https://raw.githubusercontent.com/tonesto7/nest-manager/master/Documents/css/ST-HTML.css" }

//this scrapes the public nest cam page for its unique id for using in render html tile
include 'asynchttp_v1' //<<<<<This is currently in Beta

def getCamUUID(pubVidId) {
	try {
		if(pubVidId) {
			if(!state?.lastPubVidId || !state?.camUUID || state?.lastPubVidId != pubVidId) {
				def params = [
					uri: "https://video.nest.com/live/${pubVidId}",
					requestContentType: 'text/html'
				  ]
				asynchttp_v1.get('camPageHtmlRespMethod', params)
			} else {
				return state?.camUUID
			}
		} else { Logger("getCamUUID: Your Camera's PublicVideoId was not found!!! Please make sure you have public video sharing enabled under your Cameras settings in the Nest Mobile App...", "warn") }
	} catch (ex) {
		log.error "getCamUUID Exception: $ex"
>>>>>>> origin/master
		exceptionDataHandler(ex.message, "getCamUUID")
	}
}

<<<<<<< HEAD
def getLiveStreamHost(camUUID) {
	try {
		if(camUUID) {
			def params = [
				uri: "https://www.dropcam.com/api/v1/cameras.get?id=${camUUID}",
			]
			httpGet(params) { resp ->
				def stream = resp?.data?.items.live_stream_host.toString().replaceAll("\\[|\\]", "")
				return stream ?: null
			}
		} else { Logger("getLiveStreamHost camUUID is missing....", "warn") }
	}
	catch (ex) {
		log.error "getLiveStreamHost Exception:", ex
		exceptionDataHandler(ex.message, "getLiveStreamHost")
	}
}

def getCamApiServer(camUUID) {
	try {
		if(camUUID) {
			def params = [
				uri: "https://www.dropcam.com/api/v1/cameras.get?id=${camUUID}",
			]
			httpGet(params)  { resp ->
				def apiServer = (resp?.data?.items.nexus_api_http_server)
				def apiServer1 = apiServer.toString().replaceAll("\\[|\\]", "")
				return apiServer1 ?: null
			}
		} else { Logger("getCamApiServer camUUID is missing....", "warn") }
	}
	catch (ex) {
		log.error "getCamApiServer Exception:", ex
		exceptionDataHandler(ex.message, "getCamApiServer")
	}
}

=======
def camPageHtmlRespMethod(response, data) {
	def rData = response.getData()
	def url = (rData =~ /<meta.*property="og:image".*content="(.*)".*/)[0][1]
	def uuid = (url =~ /(\?|\&)([^=]+)\=([^&]+)/)[0][3]
	//log.debug "UUID: ${uuid}"
	state.camUUID = uuid
}

def getCamApiServerData(camUUID) {
	try {
		if(camUUID) {
			def params = [
				uri: "https://www.dropcam.com/api/v1/cameras.get?id=${camUUID}"
			]
			httpGet(params)  { resp ->
				state?.camApiServerData = resp?.data
				return resp?.data ?: null
			}
		} else { Logger("getCamApiServerData camUUID is missing....", "warn") }
	}
	catch (ex) {
		log.error "getCamApiServerData Exception:", ex
		exceptionDataHandler(ex.message, "getCamApiServerData")
	}
}

def getStreamHostUrl() {
	if(!state?.camApiServerData) { return null }
	def res = state?.camApiServerData?.items?.live_stream_host
	def data = res.toString().replaceAll("\\[|\\]", "")
	//log.debug "getStreamHostUrl: $data"
	return data ?: null
}

def getCamPlaylistURL() {
	def hUrl = getStreamHostUrl()
	if(hUrl && state?.camUUID) { return "https://${hUrl}/nexus_aac/${state?.camUUID}/playlist.m3u8" }
	return null
}

def getCamApiServer() {
	if(!state?.camApiServerData) { return null }
	def res = state?.camApiServerData?.items?.nexus_api_http_server
	def data = res.toString().replaceAll("\\[|\\]", "")
	//log.debug "getCamApiServer: $data"
	return data ?: null
}

>>>>>>> origin/master
def getCamBtnJsData() {
	def data =
	"""
	  function toggle_visibility(id) {
		var id = document.getElementById(id);
<<<<<<< HEAD

		var divsToHide = document.getElementsByClassName("hideable");

		for (var i = 0; i < divsToHide.length; i++) {
		  divsToHide[i].style.display = "none";
		}

=======
		var divsToHide = document.getElementsByClassName("hideable");
		for (var i = 0; i < divsToHide.length; i++) {
		  divsToHide[i].style.display = "none";
		}
>>>>>>> origin/master
		id.style.display = 'block'
	  }
	"""
}

def getCamHtml() {
	try {
		// These are used to determine the URL for the nest cam stream
<<<<<<< HEAD
		def updateAvail = !state.updateAvailable ? "" : "<h3>Device Update Available!</h3>"
		def clientBl = state?.clientBl ? """<h3>Your Manager client has been blacklisted!\nPlease contact the Nest Manager developer to get the issue resolved!!!</h3>""" : ""
		def pubVidUrl = state?.public_share_url
		def camHtml = (pubVidUrl || state?.isStreaming) ? showCamHtml() : hideCamHtml()
=======
		def updateAvail = !state.updateAvailable ? "" : """<div class="greenAlertBanner">Device Update Available!</div>"""
		def clientBl = state?.clientBl ? """<div class="brightRedAlertBanner">Your Manager client has been blacklisted!\nPlease contact the Nest Manager developer to get the issue resolved!!!</div>""" : ""
		def pubVidUrl = state?.public_share_url
		def camHtml = (pubVidUrl && state?.camUUID && state?.isStreaming && state?.isOnline) ? showCamHtml() : hideCamHtml()
>>>>>>> origin/master

		def mainHtml = """
		<!DOCTYPE html>
		<html>
			<head>
				<meta charset="utf-8"/>
				<meta http-equiv="cache-control" content="max-age=0"/>
				<meta http-equiv="cache-control" content="no-cache"/>
<<<<<<< HEAD
				<meta http-equiv="expires" content="0"/>
				<meta http-equiv="expires" content="Tue, 01 Jan 1980 1:00:00 GMT"/>
				<meta http-equiv="pragma" content="no-cache"/>
				<meta name="viewport" content="width = device-width, user-scalable=no, initial-scale=1.0">
				<link rel="stylesheet prefetch" href="${getCssData()}"/>
=======
				<meta http-equiv="expires" content="Tue, 01 Jan 1980 1:00:00 GMT"/>
				<meta http-equiv="pragma" content="no-cache"/>
				<meta name="viewport" content="width=device-width, user-scalable=no, initial-scale=1.0">
				<link rel="stylesheet" href="${getCssData()}"/>

				<script type="text/javascript" src="${getFileBase64("https://ajax.googleapis.com/ajax/libs/jquery/1.10.2/jquery.min.js", "text", "javascript")}"></script>
				<script type="text/javascript" src="${getFileBase64("https://cdnjs.cloudflare.com/ajax/libs/vex-js/3.0.0/js/vex.combined.min.js", "text", "javascript")}"></script>

				<link rel="stylesheet" href="${getFileBase64("https://cdnjs.cloudflare.com/ajax/libs/vex-js/3.0.0/css/vex.css", "text", "css")}" />
				<link rel="stylesheet" href="${getFileBase64("https://cdnjs.cloudflare.com/ajax/libs/vex-js/3.0.0/css/vex-theme-default.css", "text", "css")}" />
				<link rel="stylesheet" href="${getFileBase64("https://cdnjs.cloudflare.com/ajax/libs/vex-js/3.0.0/css/vex-theme-top.css", "text", "css")}" />
				<script>vex.defaultOptions.className = 'vex-theme-default'</script>
				<script type="text/javascript">
					${getCamBtnJsData()}
				</script>
				<style>
					.vex.vex-theme-top .vex-content {
						width: 100%;
						padding: 3px;
					}
				</style>
>>>>>>> origin/master
			</head>
			<body>
				${clientBl}
				${updateAvail}
<<<<<<< HEAD

				${camHtml}

				<br></br>

				<table>
				  <col width="50%">
					<col width="50%">
					  <thead>
						<th>Last Event Start</th>
						<th>Last Event End</th>
					  </thead>
					  <tbody>
						<tr>
						  <td>${state?.lastEventStartDt}</td>
						  <td>${state?.lastEventEndDt}</td>
						</tr>
					  </tbody>
				</table>
				<table>
				  <col width="33%">
					<col width="33%">
					  <col width="33%">
						<thead>
						  <th>Public Video</th>
						  <th>Audio Input</th>
						  <th>Video History</th>
						</thead>
						<tbody>
						  <tr>
							<td>${state?.publicShareEnabled.toString()}</td>
							<td>${state?.audioInputEnabled.toString()}</td>
							<td>${state?.videoHistoryEnabled.toString()}</td>
						  </tr>
						</tbody>
				</table>
				<table>
				  <col width="50%">
					<col width="50%">
					  <thead>
						<th>Network Status</th>
						<th>API Status</th>
					  </thead>
					  <tbody>
						<tr>
						  <td>${state?.onlineStatus.toString()}</td>
						  <td>${state?.apiStatus}</td>
						</tr>
					  </tbody>
				</table>
				<table>
				  <tr>
					<th>Firmware Version</th>
					<th>Debug</th>
					<th>Device Type</th>
				  </tr>
				  <td>v${state?.softwareVer.toString()}</td>
				  <td>${state?.debugStatus}</td>
				  <td>${state?.devTypeVer.toString()}</td>
				</table>
				<table>
				  <thead>
					<th>Last Online Change</th>
					<th>Data Last Received</th>
				  </thead>
				  <tbody>
					<tr>
					  <td class="dateTimeText">${state?.lastConnection.toString()}</td>
					  <td class="dateTimeText">${state?.lastUpdatedDt.toString()}</td>
					</tr>
				  </tbody>
				</table>
=======
				<br></br>
				<table>
				  <col width="45%">
				  <col width="45%">
				  <tbody>
					<tr>
					  <td><a class="event-data button red">View\nLast Event</a></td>
					  <td><a class="other-info button">Show\nDevice Info</a></td>
					</tr>
				  </tbody>
				</table>
				<br></br>
				<script>
					\$('.event-data').click(function(){
						vex.dialog.alert({ unsafeMessage: `
							${camHtml}
						`})
					});

					\$('.other-info').click(function(){
						vex.dialog.alert({ unsafeMessage: `
						<table>
						  <col width="50%">
						  <col width="50%">
						  <thead>
							<th style="font-size: 16px;">Network Status</th>
							<th style="font-size: 16px;">API Status</th>
						  </thead>
						  <tbody>
							<tr>
							  <td>${state?.onlineStatus.toString()}</td>
							  <td>${state?.apiStatus}</td>
							</tr>
						  </tbody>
						</table>
						<table>
						  <tr>
							<th style="font-size: 16px;">Firmware Version</th>
							<th style="font-size: 16px;">Debug</th>
							<th style="font-size: 16px;">Device Type</th>
						  </tr>
						  <td>v${state?.softwareVer.toString()}</td>
						  <td>${state?.debugStatus}</td>
						  <td>${state?.devTypeVer.toString()}</td>
						</table>
						<table>
						  <col width="50%">
						  <col width="50%">
							<thead>
							  <th style="font-size: 16px;">Video History (Min.)</th>
							  <th style="font-size: 16px;">Video History (Max.)</th>
							</thead>
							<tbody>
							  <tr>
								<td>${getRecTimeDesc(state?.minVideoHistoryHours) ?: "Not Available"}</td>
								<td>${getRecTimeDesc(state?.maxVideoHistoryHours) ?: "Not Available"}</td>
							  </tr>
						  </tbody>
						</table>
						<table>
						  <col width="33%">
						  <col width="33%">
						  <col width="33%">
						  <thead>
							<th style="font-size: 16px;">Public Video</th>
							<th style="font-size: 16px;">Mic Status</th>
						  </thead>
						  <tbody>
							<tr>
							  <td>${state?.publicShareEnabled.toString()}</td>
							  <td>${state?.audioInputEnabled.toString()}</td>
							</tr>
						  </tbody>
						</table>
						<table>
						   <thead>
							 <th style="font-size: 16px;">Last Online Change</th>
							 <th style="font-size: 16px;">Data Last Received</th>
						   </thead>
						   <tbody>
							 <tr>
							   <td class="dateTimeText">${state?.lastConnection.toString()}</td>
							   <td class="dateTimeText">${state?.lastUpdatedDt.toString()}</td>
							 </tr>
						   </tbody>
						 </table>
					`})
					});
				</script>
>>>>>>> origin/master
			</body>
		</html>
		"""
		incHtmlLoadCnt()
		render contentType: "text/html", data: mainHtml, status: 200
	}
	catch (ex) {
		log.error "getCamHtml Exception:", ex
		exceptionDataHandler(ex.message, "getCamHtml")
	}
}

def showCamHtml() {
	def pubVidUrl = state?.public_share_url
<<<<<<< HEAD
	def pubVidId = getPublicVideoId()
	def camUUID = getCamUUID(pubVidId)
	def apiServer = getCamApiServer(camUUID)
	def liveStreamURL = getLiveStreamHost(camUUID)
=======
	if(!state?.camUUID) { getCamUUID(getPublicVidID()) }
	def camUUID = state?.camUUID
	if(camUUID && state?.camApiServerData == null) { getCamApiServerData(camUUID) }

	def apiServer = getCamApiServer()

	def liveStreamURL = getStreamHostUrl()
>>>>>>> origin/master
	def camImgUrl = "${apiServer}/get_image?uuid=${camUUID}&width=410"
	def camPlaylistUrl = "https://${liveStreamURL}/nexus_aac/${camUUID}/playlist.m3u8"

	def animationUrl = state?.animation_url ? getImgBase64(state?.animation_url, 'gif') : null
	def pubSnapUrl = state?.snapshot_url ? getImgBase64(state?.snapshot_url,'jpeg') : null

<<<<<<< HEAD
	def vidBtn = !liveStreamURL ? "" : """<a href="#" onclick="toggle_visibility('liveStream');" class="button yellow">Live Video</a>"""
	def imgBtn = !pubSnapUrl ? "" : """<a href="#" onclick="toggle_visibility('still');" class="button blue">Still Image</a>"""
	def lastEvtBtn = !animationUrl ? "" : """<a href="#" onclick="toggle_visibility('animation');" class="button red">Last Event</a>"""

	def data = """
		<script type="text/javascript">
			${getCamBtnJsData()}
		</script>
		<div class="hideable" id="liveStream">
			<video width="410" controls
				id="nest-video"
				class="video-js vjs-default-skin"
				poster="${camImgUrl}"
				data-video-url="${pubVidUrl}"
				data-video-title="">
				<source src="${camPlaylistUrl}" type="application/x-mpegURL">
			</video>
		</div>
		<div class="hideable" id="still" style="display:none">
			<img src="${pubSnapUrl}" width="100%"/>
		</div>
		<div class="hideable" id="animation" style="display:none">
			<img src="${animationUrl}" width="100%"/>
		</div>
		<div class="centerText">
		  ${vidBtn}
		  ${imgBtn}
		  ${lastEvtBtn}
=======
	def vidBtn = (!state?.isStreaming || !liveStreamURL) ? "" : """<a href="#" onclick="toggle_visibility('liveStream');" class="button yellow">Live Video</a>"""
	def imgBtn = (!state?.isStreaming || !pubSnapUrl) ? "" : """<a href="#" onclick="toggle_visibility('still');" class="button blue">Still Image</a>"""
	def lastEvtBtn = (!state?.isStreaming || !animationUrl) ? "" : """<a href="#" onclick="toggle_visibility('animation');" class="button red">Last Event</a>"""

	def data = """
		<div class="hideable" id="still">
			<h4 style="font-size: 18px; font-weight: bold; text-align: center; background: #00a1db; color: #f5f5f5; padding: 4px;">Still Image</h4>
			<img src="${pubSnapUrl}" width="100%"/>
			<h4 style="background: #696969; color: #f5f5f5; padding: 4px;">FYI: This image is only refreshed when this window is generated...</h4>
		</div>
		<div class="hideable" id="animation" style="display:none">
			<h4 style="font-size: 18px; font-weight: bold; text-align: center; background: #00a1db; color: #f5f5f5; padding: 4px;">Last Camera Event</h4>
			<img src="${animationUrl}" width="100%"/>
			<table>
			  <tbody>
				<tr>
				  <td>${state?.lastEventDate ?: "Not Available"}</td>
				  <td>${state?.lastEventTime ?: ""}</td>
				</tr>
			  </tbody>
			</table>
			<table>
			  <col width="33%">
			  <col width="33%">
			  <col width="33%">
			  <thead>
				<th style="font-size: 16px;">Had Person?</th>
				<th style="font-size: 16px;">Had Motion?</th>
				<th style="font-size: 16px;">Had Sound?</th>
			  </thead>
			  <tbody>
				<tr>
				  <td>${state?.lastCamEvtData?.hasPerson.toString().capitalize() ?: "False"}</td>
				  <td>${state?.lastCamEvtData?.hasMotion.toString().capitalize() ?: "False"}</td>
				  <td>${state?.lastCamEvtData?.hasSound.toString().capitalize() ?: "False"}</td>
				</tr>
			  </tbody>
			</table>
		</div>
		<br></br>
		<div class="centerText">
			<table>
			  <col width="48%">
			  <col width="48%">
			  <tbody>
				<tr>
				  <td>${imgBtn}</td>
				  <td>${lastEvtBtn}</td>
				</tr>
			  </tbody>
			</table>
>>>>>>> origin/master
		</div>
	"""
}

def hideCamHtml() {
<<<<<<< HEAD
	def data = ""
	if(state?.isStreaming == false) {
		data = """<br></br><h3 style="font-size: 22px; font-weight: bold; text-align: center; background: #00a1db; color: #f5f5f5;">Video Streaming is Currently Off...</h3>"""
	} else {
		data = """<br></br><h3>Unable to Display Video Stream!!!\nPlease make sure that public streaming is enabled for this camera under https://home.nest.com</h3>"""
	}
=======
	def data = "<br></br><br></br>"
	if(!state?.isStreaming && state?.isOnline) {
		data += """<h3 style="font-size: 22px; font-weight: bold; text-align: center; background: #00a1db; color: #f5f5f5;">Live Video Streaming is Currently Off</h3>
			<br></br><h3 style="font-size: 22px; font-weight: bold; text-align: center; background: #00a1db; color: #f5f5f5;">Please Turn it back on and refresh this page...</h3>"""
	}
	else if(!state?.camUUID) {
		data += """<h3>Camera ID Not Found...</h3><br></br><h3>If this is your First Try Please Refresh the Page!!!\nIf this message continues after a few minutes...Please verify public video streaming is enabled for this camera</h3>"""
	}
	else if(!state?.public_share_url) {
		data += """<h3>Unable to Display Video Stream</h3><br></br><h3>Please make sure that public video streaming is enabled for this camera under https://home.nest.com</h3>"""
	}
	else if(!state?.isOnline) {
		data += """<h3>This Camera is Currently Offline</h3><br></br><h3>Please verify it has a Wi-Fi connection and refresh this page...</h3>"""
	}
	else {
		data += """<h3>Unable to Display the Live Video Stream</h3><br></br><h3>An Unknown Issue has Occurred... Please consult the Live Logs in the SmartThings IDE</h3>"""
	}
	data += "<br></br><br></br>"
>>>>>>> origin/master
	return data
}

private def textDevName()   { return "Nest Camera${appDevName()}" }
private def appDevType()    { return false }
private def appDevName()    { return appDevType() ? " (Dev)" : "" }
