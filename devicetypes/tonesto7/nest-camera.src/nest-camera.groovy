/**
 *  Nest Cam
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
 */

import java.text.SimpleDateFormat

preferences { }

def devVer() { return "2.1.0" }

metadata {
	definition (name: "${textDevName()}", author: "Anthony S.", namespace: "tonesto7") {
		capability "Sensor"
		capability "Switch"
		//capability "Motion Sensor"
		//capability "Sound Sensor"
		capability "Refresh"
		capability "Notification"
		//capability "Image Capture"
		//capability "Video Camera"
		//capability "Video Capture"
		capability "Health Check"

		command "refresh"
		command "poll"
		command "log", ["string","string"]
		command "streamingOn"
		command "streamingOff"
		command "chgStreaming"

		attribute "softwareVer", "string"
		attribute "lastConnection", "string"
		attribute "lastOnline", "string"
		attribute "lastUpdateDt", "string"
		attribute "activityZoneName", "string"
		attribute "isStreaming", "string"
		attribute "audioInputEnabled", "string"
		attribute "videoHistoryEnabled", "string"
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
			tileAttribute("device.switch5", key: "CAMERA_STATUS") {
				attributeState("on", label: "Active", icon: "st.camera.dlink-indoor", action: "vidOff", backgroundColor: "#79b821", defaultState: true)
				attributeState("off", label: "Inactive", icon: "st.camera.dlink-indoor", action: "vidOn", backgroundColor: "#ffffff")
				attributeState("restarting", label: "Connecting", icon: "st.camera.dlink-indoor", backgroundColor: "#53a7c0")
				attributeState("unavailable", label: "Unavailable", icon: "st.camera.dlink-indoor", action: "refresh.refresh", backgroundColor: "#F22000")
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
			tileAttribute("device.betaLogo", key: "BETA_LOGO") {
				attributeState("betaLogo", label: "", value: "", defaultState: true)
			}
		}

		standardTile("isStreamingStatus", "device.isStreaming", width: 2, height: 2, decoration: "flat") {
			state("on", label: "Streaming", action: "chgStreaming", nextState: "updating", icon: "https://raw.githubusercontent.com/tonesto7/nest-manager/master/Images/App/camera_green_icon.png", backgroundColor: "#79b821")
			state("off", label: "Off", action: "chgStreaming", nextState: "updating", icon: "https://raw.githubusercontent.com/tonesto7/nest-manager/master/Images/App/camera_gray_icon.png", backgroundColor: "#ffffff")
			state("updating", label:"", icon: "https://raw.githubusercontent.com/tonesto7/nest-manager/master/Images/Devices/cmd_working.png")
			state("unavailable", label: "Unavailable", icon: "https://raw.githubusercontent.com/tonesto7/nest-manager/master/Images/App/camera_red_icon.png", backgroundColor: "#F22000")
		}
		standardTile("isStreaming", "device.isStreaming", width: 2, height: 2, decoration: "flat") {
			state("on", action: "chgStreaming", nextState: "updating", icon: "https://raw.githubusercontent.com/tonesto7/nest-manager/master/Images/Devices/camera_stream_btn_icon.png")
			state("off", action: "chgStreaming", nextState: "updating", icon: "https://raw.githubusercontent.com/tonesto7/nest-manager/master/Images/Devices/camera_off_btn_icon.png")
			state("updating", label:"", icon: "https://raw.githubusercontent.com/tonesto7/nest-manager/master/Images/Devices/cmd_working.png")
			state("unavailable", icon: "https://raw.githubusercontent.com/tonesto7/nest-manager/master/Images/Devices/camera_offline_btn_icon.png")
		}
		carouselTile("cameraDetails", "device.image", width: 4, height: 4) { }
		/*standardTile("take", "device.image", width: 2, height: 2, canChangeIcon: false, inactiveLabel: true, canChangeBackground: false) {
			state "take", label: "Take", action: "Image Capture.take", icon: "st.camera.camera", backgroundColor: "#FFFFFF"
			//state "taking", label:'Taking', action: "", icon: "st.camera.take-photo", backgroundColor: "#53a7c0"
			//state "image", label: "Take", action: "Image Capture.take", icon: "st.camera.camera", backgroundColor: "#FFFFFF", nextState:"taking"
		}*/
		standardTile("motion", "device.motion", width: 2, height: 2) {
			state "active", label:'motion', icon:"st.motion.motion.active", backgroundColor:"#53a7c0"
			state "inactive", label:'no motion', icon:"st.motion.motion.inactive", backgroundColor:"#ffffff"
		}
		standardTile("sound", "device.sound", width: 2, height: 2) {
			state "detected", label:'Noise', icon:"st.sound.sound.detected", backgroundColor:"#53a7c0"
			state "not detected", label:'Quiet', icon:"st.sound.sound.notdetected", backgroundColor:"#ffffff"
		}
		standardTile("filler", "device.filler", width: 2, height: 2){
			state("default", label:'')
		}
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
			state "default", label: 'refresh', action:"refresh.refresh", icon:"st.secondary.refresh-icon"
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
		htmlTile(name:"devCamHtml", action: "getCamHtml", width: 6, height: 11, whitelist: ["raw.githubusercontent.com", "cdn.rawgit.com"])

		main "isStreamingStatus"
		//details(["devCamHtml", "isStreaming", "take", "refresh", "motion", "cameraDetails", "sound"])
		details(["devCamHtml", "isStreaming", "refresh"])
	}
}

mappings {
	path("/getCamHtml") {action: [GET: "getCamHtml"]}
}

def initialize() {
	log.info "Nest Camera ${textVersion()} ${textCopyright()}"
	poll()
}

void installed() {
	Logger("installed...")
    verifyHC()
}

void verifyHC() {
	def val = device.currentValue("checkInterval")
	def timeOut = state?.hcTimeout ?: 35
	if(!val || val.toInteger() != timeOut) {
		Logger("verifyHC: Updating Device Health Check Interval to $timeOut")
		sendEvent(name: "checkInterval", value: 60 * timeOut.toInteger(), data: [protocol: "cloud"], displayed: false)
	}
}

def ping() {
	Logger("ping...")
	refresh()
}

def parse(String description) {
	LogAction("Parsing '${description}'", "debug")
}

def poll() {
	Logger("polling parent...")
	parent.refresh(this)
}

def refresh() {
	Logger("refreshing parent...")
	poll()
}

// parent calls this method to queue data.
// goal is to return to parent asap to avoid execution timeouts

def generateEvent(Map eventData) {
	//log.trace("generateEvent Parsing data ${eventData}")
	state.eventData = eventData
	runIn(3, "processEvent", [overwrite: true] )
}

def processEvent() {
	if(state?.swVersion != devVer()) {
		installed()
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
			state?.useMilitaryTime = eventData?.mt ? true : false
            state.clientBl = eventData?.clientBl == true ? true : false
			state.mobileClientType = eventData?.mobileClientType
			state.nestTimeZone = eventData?.tz ?: null
			isStreamingEvent(results?.is_streaming)
			videoHistEnabledEvent(results?.is_video_history_enabled?.toString())
			publicShareEnabledEvent(results?.is_public_share_enabled?.toString())
			if(!results?.last_is_online_change) { lastCheckinEvent(null) }
			else { lastCheckinEvent(results?.last_is_online_change?.toString()) }
			if(eventData?.htmlInfo) { state?.htmlInfo = eventData?.htmlInfo }
			if(eventData?.allowDbException) { state?.allowDbException = eventData?.allowDbException = false ? false : true }
			apiStatusEvent(eventData?.apiIssues)
			debugOnEvent(eventData?.debug ? true : false)
			onlineStatusEvent(results?.is_online?.toString())
			audioInputEnabledEvent(results?.is_audio_input_enabled?.toString())
			softwareVerEvent(results?.software_version?.toString())
			if(results?.activity_zones) { state?.activityZones = results?.activity_zones }
			if(results?.public_share_url) { state?.public_share_url = results?.public_share_url }
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

def isStreamingEvent(isStreaming) {
	//log.trace "isStreamingEvent($isStreaming)..."
	def isOn = device.currentState("isStreaming")?.value
	def isOnline = device.currentState("onlineStatus")?.value
	def val = (isStreaming.toBoolean() == true) ? "on" : (!isOnline == "Online" ? "unavailable" : "off")
	state?.isStreaming = val == "on" ? true : false
	if(!isOn.equals(val)) {
		Logger("UPDATED | Streaming Video is: (${val}) | Original State: (${isOn})")
		sendEvent(name: "isStreaming", value: val, descriptionText: "Streaming Video is: ${val}", displayed: true, isStateChange: true, state: val)
	} else { LogAction("Streaming Video Status is: (${val}) | Original State: (${isOn})") }
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
	def tf = new SimpleDateFormat("E MMM dd HH:mm:ss z yyyy")
		tf.setTimeZone(getTimeZone())
	def curStartDt = device?.currentState("lastEventStart")?.value ? tf?.format(Date.parse("E MMM dd HH:mm:ss z yyyy", device?.currentState("lastEventStart")?.value.toString())) : null
	def curEndDt = device?.currentState("lastEventEnd")?.value ? tf?.format(Date.parse("E MMM dd HH:mm:ss z yyyy", device?.currentState("lastEventEnd")?.value.toString())) : null
	def newStartDt = data?.start_time ? tf.format(Date.parse("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", data?.start_time.toString())) : "Not Available"
	def newEndDt = data?.end_time ? tf.format(Date.parse("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", data?.end_time.toString())) : "Not Available"
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


/************************************************************************************************
|									DEVICE COMMANDS     										|
*************************************************************************************************/
def chgStreaming() {
	def cur = latestValue("isStreaming").toString()
	if(cur == "on" || cur == "unavailable" || !cur) {
		streamingOn(true)
	} else {
		streamingOff(true)
	}
}

def streamingOn(manChg=false) {
	try {
		log.trace "streamingOn..."
		if(parent?.setCamStreaming(this, "true")) {
			sendEvent(name: "isStreaming", value: "on", descriptionText: "Streaming Video is: on", displayed: true, isStateChange: true, state: "on")
			if(manChg) { incManStreamChgCnt() }
			else { incProgStreamChgCnt() }
		}

	} catch (ex) {
		log.error "streamingOn Exception:", ex
		exceptionDataHandler(ex.message, "streamingOn")
	}
}

def streamingOff(manChg=false) {
	try {
		log.trace "streamingOff..."
		if(parent?.setCamStreaming(this, "false")) {
			sendEvent(name: "isStreaming", value: "off", descriptionText: "Streaming Video is: off", displayed: true, isStateChange: true, state: "off")
			if(manChg) { incManStreamChgCnt() }
			else { incProgStreamChgCnt() }
		}
	} catch (ex) {
		log.error "streamingOff Exception:", ex
		exceptionDataHandler(ex.message, "streamingOff")
	}
}

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
			camManStrChgCnt:(state?.manStreamChgCnt ?: 0), camProgStrChgCnt:(state?.progStreamChgCnt ?: 0), camHtmlLoadedCnt:(state?.htmlLoadCnt ?: 0)//,
			//camVidBtnTapCnt:(state?.videoBtnTapCnt ?: 0), camImgBtnTapCnt:(state?.imageBtnTapCnt ?: 0), camEvtBtnTapCnt:(state?.eventBtnTapCnt ?: 0),
			//camInfoBtnCnt:(state?.infoBtnTapCnt ?: 0)
			]
}

/************************************************************************************************
|										OTHER METHODS     										|
*************************************************************************************************/

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
		cssData = getFileBase64(cssUrl(), "text", "css")
	}
	return cssData
}

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
		exceptionDataHandler(ex.message, "getCamUUID")
	}
}

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

def getCamBtnJsData() {
	def data =
	"""
	  function toggle_visibility(id) {
		var id = document.getElementById(id);

		var divsToHide = document.getElementsByClassName("hideable");

		for (var i = 0; i < divsToHide.length; i++) {
		  divsToHide[i].style.display = "none";
		}

		id.style.display = 'block'
	  }
	"""
}

def getCamHtml() {
	try {
		// These are used to determine the URL for the nest cam stream
		def updateAvail = !state.updateAvailable ? "" : "<h3>Device Update Available!</h3>"
		def clientBl = state?.clientBl ? """<h3>Your Manager client has been blacklisted!\nPlease contact the Nest Manager developer to get the issue resolved!!!</h3>""" : ""
		def pubVidUrl = state?.public_share_url
		def camHtml = (pubVidUrl || state?.isStreaming) ? showCamHtml() : hideCamHtml()

		def mainHtml = """
		<!DOCTYPE html>
		<html>
			<head>
				<meta charset="utf-8"/>
				<meta http-equiv="cache-control" content="max-age=0"/>
				<meta http-equiv="cache-control" content="no-cache"/>
				<meta http-equiv="expires" content="0"/>
				<meta http-equiv="expires" content="Tue, 01 Jan 1980 1:00:00 GMT"/>
				<meta http-equiv="pragma" content="no-cache"/>
				<meta name="viewport" content="width = device-width, user-scalable=no, initial-scale=1.0">
				<link rel="stylesheet prefetch" href="${getCssData()}"/>
			</head>
			<body>
				${clientBl}
				${updateAvail}

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
	def pubVidId = getPublicVideoId()
	def camUUID = getCamUUID(pubVidId)
	def apiServer = getCamApiServer(camUUID)
	def liveStreamURL = getLiveStreamHost(camUUID)
	def camImgUrl = "${apiServer}/get_image?uuid=${camUUID}&width=410"
	def camPlaylistUrl = "https://${liveStreamURL}/nexus_aac/${camUUID}/playlist.m3u8"

	def animationUrl = state?.animation_url ? getImgBase64(state?.animation_url, 'gif') : null
	def pubSnapUrl = state?.snapshot_url ? getImgBase64(state?.snapshot_url,'jpeg') : null

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
		</div>
	"""
}

def hideCamHtml() {
	def data = ""
	if(state?.isStreaming == false) {
		data = """<br></br><h3 style="font-size: 22px; font-weight: bold; text-align: center; background: #00a1db; color: #f5f5f5;">Video Streaming is Currently Off...</h3>"""
	} else {
		data = """<br></br><h3>Unable to Display Video Stream!!!\nPlease make sure that public streaming is enabled for this camera under https://home.nest.com</h3>"""
	}
	return data
}

private def textDevName()   { return "Nest Camera${appDevName()}" }
private def appDevType()    { return false }
private def appDevName()    { return appDevType() ? " (Dev)" : "" }
