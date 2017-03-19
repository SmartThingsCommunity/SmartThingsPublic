/**
 *  Nest Presence
 *	Author: Anthony S. (@tonesto7)
 *	Co-Authors: Ben W. (@desertBlade), Eric S. (@E_Sch)
 *
 *	Copyright (C) 2017 Anthony S., Ben W.
 * 	Licensing Info: Located at https://raw.githubusercontent.com/tonesto7/nest-manager/master/LICENSE.md
 */

import java.text.SimpleDateFormat

preferences {  }

def devVer() { return "4.5.1" }

// for the UI
metadata {
	definition (name: "${textDevName()}", namespace: "tonesto7", author: "DesertBlade") {

		capability "Presence Sensor"
		capability "Sensor"
		capability "Refresh"
		//capability "Health Check"

		command "setPresence"
		command "refresh"
		command "log"
		command "setHome"
		command "setAway"

		attribute "lastConnection", "string"
		attribute "apiStatus", "string"
		attribute "debugOn", "string"
		attribute "devTypeVer", "string"
		attribute "nestPresence", "string"
	}

	simulator {
		status "present": "presence: 1"
		status "not present": "presence: 0"
	}

	tiles(scale: 2) {
		standardTile("presence", "device.presence", width: 4, height: 4, canChangeBackground: true) {
			state("present", 	labelIcon:"st.presence.tile.mobile-present", 	backgroundColor:"#53a7c0", icon:"https://raw.githubusercontent.com/tonesto7/nest-manager/master/Images/Devices/nest_dev_pres_icon.png")
			state("not present",labelIcon:"st.presence.tile.mobile-not-present",backgroundColor:"#ebeef2", icon:"https://raw.githubusercontent.com/tonesto7/nest-manager/master/Images/Devices/nest_dev_away_icon.png")
		}
		standardTile("nestPresence", "device.nestPresence", width:2, height:2, decoration: "flat") {
			state "home",	action: "setPresence",	icon: "https://raw.githubusercontent.com/tonesto7/nest-manager/master/Images/Devices/pres_home_icon.png"
			state "away", 		action: "setPresence", 	icon: "https://raw.githubusercontent.com/tonesto7/nest-manager/master/Images/Devices/pres_away_icon.png"
			state "auto-away", 	action: "setPresence", 	icon: "https://raw.githubusercontent.com/tonesto7/nest-manager/master/Images/Devices/pres_autoaway_icon.png"
			state "unknown", 	action: "setPresence", 	icon: "st.unknown.unknown.unknown"
		}
		valueTile("lastUpdatedDt", "device.lastUpdatedDt", width: 4, height: 1, decoration: "flat", wordWrap: true) {
			state("default", label: 'Data Last Received:\n${currentValue}')
		}
		valueTile("apiStatus", "device.apiStatus", width: 2, height: 1, decoration: "flat", wordWrap: true) {
			state "ok", label: "API Status:\nOK"
			state "issue", label: "API Status:\nISSUE ", backgroundColor: "#FFFF33"
		}
		standardTile("refresh", "device.refresh", width:2, height:2, decoration: "flat") {
			state "default", action:"refresh.refresh", icon:"https://raw.githubusercontent.com/tonesto7/nest-manager/master/Images/Devices/refresh_icon.png"
		}
		valueTile("devTypeVer", "device.devTypeVer",  width: 2, height: 1, decoration: "flat") {
			state("default", label: 'Device Type:\nv${currentValue}')
		}
        htmlTile(name:"html", action: "getHtml", width: 6, height: 4, whitelist: ["raw.githubusercontent.com", "cdn.rawgit.com"])

		main ("presence")
		details ("presence", "nestPresence", "refresh", "html")
	}
}

mappings {
	path("/getHtml") {action: [GET: "getHtml"] }
}

void installed() {
	Logger("installed...")
	initialize()
	state?.isInstalled = true
}

def initialize() {
	LogAction("initialize")
	verifyHC()
}

void updated() {
	Logger("updated...")
	initialize()
}

def getHcTimeout() {
	def to = state?.hcTimeout
	return ((to instanceof Integer) ? to.toInteger() : 60)*60
}

void verifyHC() {
	def val = device.currentValue("checkInterval")
	def timeOut = getHcTimeout()
	if(!val || val.toInteger() != timeOut) {
		Logger("verifyHC: Updating Device Health Check Interval to $timeOut")
		sendEvent(name: "checkInterval", value: timeOut, data: [protocol: "cloud"], displayed: false)
	}
}

def ping() {
	Logger("ping...")
	keepAwakeEvent()
}

def parse(String description) {
	LogAction("Parsing '${description}'")
}

def configure() { }

def poll() {
	Logger("Polling parent...")
	parent.refresh(this)
}

def refresh() {
	poll()
}

// parent calls this method to queue data.
// goal is to return to parent asap to avoid execution timeouts

def generateEvent(Map eventData) {
	//log.trace("generateEvent Parsing data ${eventData}")
	def eventDR = [evt:eventData]
	runIn(3, "processEvent", [overwrite: true, data:eventDR] )
}

def processEvent(data) {
	if(state?.swVersion != devVer()) {
		initialize()
		state.swVersion = devVer()
	}
	def eventData = data?.evt
	state.remove("eventData")
	//log.trace("processEvent Parsing data ${eventData}")
	try {
		LogAction("------------START OF API RESULTS DATA------------", "warn")
		if(eventData) {
			state.showLogNamePrefix = eventData?.logPrefix == true ? true : false
			state.enRemDiagLogging = eventData?.enRemDiagLogging == true ? true : false
			if(eventData.hcTimeout && (state?.hcTimeout != eventData?.hcTimeout || !state?.hcTimeout)) {
				state.hcTimeout = eventData?.hcTimeout
				verifyHC()
			}
			state.nestTimeZone = eventData?.tz ?: null
			state.clientBl = eventData?.clientBl == true ? true : false
			state.mobileClientType = eventData?.mobileClientType
			state?.useMilitaryTime = !eventData?.mt ? false : true
			debugOnEvent(!eventData?.debug ? false : true)
			presenceEvent(eventData?.pres)
			apiStatusEvent((!eventData?.apiIssues ? false : true))
			deviceVerEvent(eventData?.latestVer.toString())
			if(eventData?.allowDbException) { state?.allowDbException = eventData?.allowDbException = false ? false : true }
			lastUpdatedEvent(true)
		}
		//This will return all of the devices state data to the logs.
		//log.debug "Device State Data: ${getState()}"
		return null
	}
	catch (ex) {
		log.error "generateEvent Exception:", ex
		exceptionDataHandler(ex.message, "generateEvent")
	}
}

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
	if(!tz) { Logger("getTimeZone: Hub or Nest TimeZone is not found...", "warn") }
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

def debugOnEvent(debug) {
	def val = device.currentState("debugOn")?.value
	def stateVal = debug ? "On" : "Off"
	state.debug = debug ? true : false
	if(!val.equals(stateVal)) {
		log.debug("UPDATED | debugOn: (${stateVal}) | Original State: (${val})")
		sendEvent(name: 'debugOn', value: stateVal, displayed: false)
	} else { LogAction("debugOn: (${stateVal}) | Original State: (${val})") }
}

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

def presenceEvent(presence) {
	def val = device.currentState("presence")?.value
	def pres = (presence == "home") ? "present" : "not present"
	def nestPres = !device.currentState("nestPresence") ? null : device.currentState("nestPresence")?.value.toString()
	def newNestPres = (presence == "home") ? "home" : ((presence == "auto-away") ? "auto-away" : "away")
	def statePres = state?.present
	state?.present = (pres == "present") ? true : false
	state?.nestPresence = newNestPres
	if(!val.equals(pres) || !nestPres.equals(newNestPres) || !nestPres) {
		Logger("UPDATED | Presence: ${pres} | Original State: ${val} | State Variable: ${statePres}")
		sendEvent(name: 'nestPresence', value: newNestPres, descriptionText: "Nest Presence is: ${newNestPres}", displayed: true, isStateChange: true )
		sendEvent(name: 'presence', value: pres, descriptionText: "Device is: ${pres}", displayed: true, isStateChange: true )
	} else { LogAction("Presence - Present: (${pres}) | Original State: (${val}) | State Variable: ${state?.present}") }
}

def apiStatusEvent(issue) {
	def curStat = device.currentState("apiStatus")?.value
	def newStat = issue ? "issue" : "ok"
	state?.apiStatus = newStat
	if(!curStat.equals(newStat)) {
		Logger("UPDATED | API Status is: (${newStat}) | Original State: (${curStat})")
		sendEvent(name: "apiStatus", value: newStat, descriptionText: "API Status is: ${newStat}", displayed: true, isStateChange: true, state: newStat)
	} else { LogAction("API Status is: (${newStat}) | Original State: (${curStat})") }
}

def getNestPresence() {
	return !device.currentState("nestPresence") ? "home" : device.currentState("nestPresence")?.value.toString()
}

def getPresence() {
	return !device.currentState("presence") ? "present" : device.currentState("presence").value.toString()
}

/************************************************************************************************
|									NEST PRESENCE FUNCTIONS										|
*************************************************************************************************/
void setPresence() {
	try {
		log.trace "setPresence()..."
		def pres = getNestPresence()
		log.trace "Current Nest Presence: ${pres}"
		if(pres == "auto-away" || pres == "away") { setHome() }
		else if (pres == "home") { setAway() }
	}
	catch (ex) {
		log.error "setPresence Exception:", ex
		exceptionDataHandler(ex.message, "setPresence")
	}
}

def setAway() {
	try {
		log.trace "setAway()..."
		parent.setStructureAway(this, "true")
		presenceEvent("away")
	}
	catch (ex) {
		log.error "setAway Exception:", ex
		exceptionDataHandler(ex.message, "setAway")
	}
}

def setHome() {
	try {
		log.trace "setHome()..."
		parent.setStructureAway(this, "false")
		presenceEvent("home")
	}
	catch (ex) {
		log.error "setHome Exception:", ex
		exceptionDataHandler(ex.message, "setHome")
	}
}

/************************************************************************************************
|										LOGGING FUNCTIONS										|
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
	if(state?.enRemDiagLogging) {
		parent.saveLogtoRemDiagStore(smsg, logType, "Presence DTH")
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
			parent?.sendChildExceptionData("presence", devVer(), msgString, methodName)
		}
	}
}

def incHtmlLoadCnt() { state?.htmlLoadCnt = (state?.htmlLoadCnt ? state?.htmlLoadCnt.toInteger()+1 : 1) }
def getMetricCntData() {
	return [presHtmlLoadCnt:(state?.htmlLoadCnt ?: 0)]
}

def getDtNow() {
	def now = new Date()
	return formatDt(now)
}

def formatDt(dt) {
	def tf = new SimpleDateFormat("E MMM dd HH:mm:ss z yyyy")
	if(getTimeZone()) { tf.setTimeZone(getTimeZone()) }
	else {
		Logger("SmartThings TimeZone is not found or is not set... Please Try to open your ST location and Press Save...", "warn")
	}
	return tf.format(dt)
}

def getTimeDiffSeconds(strtDate, stpDate=null, methName=null) {
	//LogTrace("[GetTimeDiffSeconds] StartDate: $strtDate | StopDate: ${stpDate ?: "Not Sent"} | MethodName: ${methName ?: "Not Sent"})")
	try {
		if(strtDate) {
			//if(strtDate?.contains("dtNow")) { return 10000 }
			def now = new Date()
			def stopVal = stpDate ? stpDate.toString() : formatDt(now)
			def startDt = Date.parse("E MMM dd HH:mm:ss z yyyy", strtDate)
			def stopDt = Date.parse("E MMM dd HH:mm:ss z yyyy", stopVal)
			def start = Date.parse("E MMM dd HH:mm:ss z yyyy", formatDt(startDt)).getTime()
			def stop = Date.parse("E MMM dd HH:mm:ss z yyyy", stopVal).getTime()
			def diff = (int) (long) (stop - start) / 1000
			//LogTrace("[GetTimeDiffSeconds] Results for '$methName': ($diff seconds)")
			return diff
		} else { return null }
	} catch (ex) {
		log.warn "getTimeDiffSeconds error: Unable to parse datetime..."
	}
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

def getCssData() {
	def cssData = null
	def htmlInfo = state?.htmlInfo
	state?.cssData = null
	if(htmlInfo?.cssUrl && htmlInfo?.cssVer) {
		//LogAction("getCssData: CSS Data is Missing | Loading Data from Source...")
		cssData = getFileBase64(htmlInfo.cssUrl, "text", "css")
		state?.cssData = cssData
		state?.cssVer = htmlInfo?.cssVer
	} else {
		//LogAction("getCssData: No Stored CSS Data Found for Device... Loading for Static URL...")
		cssData = getFileBase64(cssUrl(), "text", "css")
	}
	return cssData
}

def cssUrl()	 { return "https://raw.githubusercontent.com/tonesto7/nest-manager/master/Documents/css/ST-HTML.css" }

def getHtml() {
	try {
		def updateAvail = !state.updateAvailable ? "" : """<div class="greenAlertBanner">Device Update Available!</div>"""
		def clientBl = state?.clientBl ? """<div class="brightRedAlertBanner">Your Manager client has been blacklisted!\nPlease contact the Nest Manager developer to get the issue resolved!!!</div>""" : ""

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
			</body>
		</html>
		"""
		incHtmlLoadCnt()
		render contentType: "text/html", data: mainHtml, status: 200
	}
	catch (ex) {
		log.error "getHtml Exception:", ex
		exceptionDataHandler(ex.message, "getHtml")
	}
}

private def textDevName()   { return "Nest Presence${appDevName()}" }
private def appDevType()    { return false }
private def appDevName()    { return appDevType() ? " (Dev)" : "" }
