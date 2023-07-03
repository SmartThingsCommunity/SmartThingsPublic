/**
 *  Nest Protect
 *	Author: Anthony S. (@tonesto7)
 *	Co-Authors: Ben W. (@desertblade), Eric S. (@E_Sch)
 *
 *	Copyright (C) 2017 Anthony S.
 * 	Licensing Info: Located at https://raw.githubusercontent.com/tonesto7/nest-manager/master/LICENSE.md
 */

import java.text.SimpleDateFormat

preferences { }

def devVer() { return "5.3.4" }

metadata {
	definition (name: "${textDevName()}", author: "Anthony S.", namespace: "tonesto7") {
		//capability "Polling"
		capability "Actuator"
		capability "Sensor"
		capability "Battery"
		capability "Smoke Detector"
		capability "Power Source"
		capability "Carbon Monoxide Detector"
		capability "Refresh"
		capability "Health Check"

		command "refresh"
		command "poll"
		command "log", ["string","string"]
		command "runSmokeTest"
		command "runCoTest"
		command "runBatteryTest"

		attribute "devVer", "string"
		attribute "alarmState", "string"
		attribute "batteryState", "string"
		attribute "battery", "string"
		attribute "uiColor", "string"
		attribute "softwareVer", "string"
		attribute "lastConnection", "string"
		attribute "lastUpdateDt", "string"
		attribute "lastTested", "string"
		attribute "isTesting", "string"
		attribute "apiStatus", "string"
		attribute "debugOn", "string"
		attribute "devTypeVer", "string"
		attribute "onlineStatus", "string"
		attribute "carbonMonoxide", "string"
		attribute "smoke", "string"
		attribute "nestCarbonMonoxide", "string"
		attribute "powerSourceNest", "string"
		attribute "nestSmoke", "string"
	}

	tiles(scale: 2) {
		multiAttributeTile(name:"alarmState", type:"generic", width:6, height:4) {
			tileAttribute("device.alarmState", key: "PRIMARY_CONTROL") {
				attributeState("default", label:'--', icon: "st.unknown.unknown.unknown")
				attributeState("ok", label:"clear", icon:"st.alarm.smoke.clear", backgroundColor:"#ffffff")
				attributeState("smoke-warning", label:"SMOKE!\nWARNING", icon:"st.alarm.smoke.smoke", backgroundColor:"#e8d813")
				attributeState("smoke-emergency", label:"SMOKE!", icon:"st.alarm.smoke.smoke", backgroundColor:"#e86d13")
				attributeState("co-warning", label:"CO!\nWARNING!", icon:"st.alarm.carbon-monoxide.carbon-monoxide", backgroundColor:"#e8d813")
				attributeState("co-emergency", label:"CO!", icon:"st.alarm.carbon-monoxide.carbon-monoxide", backgroundColor:"#e86d13")
			}
			tileAttribute("device.batteryState", key: "SECONDARY_CONTROL") {
				attributeState("default", label:'unknown', icon: "st.unknown.unknown.unknown")
				attributeState("ok", label: "Battery: OK", backgroundColor: "#00a0dc",
					icon: "https://raw.githubusercontent.com/tonesto7/nest-manager/master/Images/Devices/battery_ok_v.png")
				attributeState("replace", label: "Battery: REPLACE!", backgroundColor: "#e86d13",
					icon: "https://raw.githubusercontent.com/tonesto7/nest-manager/master/Images/Devices/battery_low_v.png")
			}
		}
		standardTile("main2", "device.alarmState", width: 2, height: 2) {
			state("default", label:'--', icon: "st.unknown.unknown.unknown")
			state("ok", label:"clear", backgroundColor:"#ffffff",
				icon:"https://raw.githubusercontent.com/tonesto7/nest-manager/master/Images/Devices/alarm_clear.png")
			state("smoke-warning", label:"SMOKE!\nWARNING", backgroundColor:"#e8d813",
				icon:"https://raw.githubusercontent.com/tonesto7/nest-manager/master/Images/Devices/smoke_warn.png")
			state("smoke-emergency", label:"SMOKE!", backgroundColor:"#e8d813",
				icon:"https://raw.githubusercontent.com/tonesto7/nest-manager/master/Images/Devices/smoke_emergency.png")
			state("co-warning", label:"CO!\nWARNING!", backgroundColor:"#e86d13",
				icon:"https://raw.githubusercontent.com/tonesto7/nest-manager/master/Images/Devices/co_warn.png")
			state("co-emergency", label:"CO!", backgroundColor:"#e86d13",
				icon:"https://raw.githubusercontent.com/tonesto7/nest-manager/master/Images/Devices/co_emergency.png")
		}
		standardTile("smoke", "device.nestSmoke", width: 2, height: 2) {
			state("default", label:'unknown', icon: "st.unknown.unknown.unknown")
			state("ok", icon:"https://raw.githubusercontent.com/tonesto7/nest-manager/master/Images/Devices/smoke_clear.png")
			state("warning", icon:"https://raw.githubusercontent.com/tonesto7/nest-manager/master/Images/Devices/smoke_warn.png")
			state("emergency", icon:"https://raw.githubusercontent.com/tonesto7/nest-manager/master/Images/Devices/smoke_emergency.png")
		}
		standardTile("carbonMonoxide", "device.nestCarbonMonoxide", width: 2, height: 2){
			state("default", label:'unknown', icon: "st.unknown.unknown.unknown")
			state("ok", icon:"https://raw.githubusercontent.com/tonesto7/nest-manager/master/Images/Devices/co_clear.png")
			state("warning", icon:"https://raw.githubusercontent.com/tonesto7/nest-manager/master/Images/Devices/co_warn.png")
			state("emergency", icon:"https://raw.githubusercontent.com/tonesto7/nest-manager/master/Images/Devices/co_emergency.png")
		}
		 standardTile("batteryState", "device.batteryState", width: 2, height: 2){
			state("default", label:'unknown')
			state("ok", icon: "https://raw.githubusercontent.com/tonesto7/nest-manager/master/Images/Devices/battery_ok.png")
			state("replace", icon: "https://raw.githubusercontent.com/tonesto7/nest-manager/master/Images/Devices/battery_low.png")
		}
		standardTile("filler", "device.filler", width: 2, height: 2){
			state("default", label:'')
		}
		valueTile("onlineStatus", "device.onlineStatus", width: 2, height: 1, wordWrap: true, decoration: "flat") {
			state("default", label: 'Network Status:\n${currentValue}')
		}
		valueTile("uiColor", "device.uiColor", inactiveLabel: false, width: 2, height: 1, decoration: "flat", wordWrap: true) {
			state("default", label: 'UI Color:\n${currentValue}')
		}
		valueTile("softwareVer", "device.softwareVer", inactiveLabel: false, width: 3, height: 1, decoration: "flat", wordWrap: true) {
			state("default", label: 'Firmware:\nv${currentValue}')
		}
		valueTile("lastConnection", "device.lastConnection", inactiveLabel: false, width: 3, height: 1, decoration: "flat", wordWrap: true) {
			state("default", label: 'Protect Last Checked-In:\n${currentValue}')
		}
		valueTile("lastTested", "device.lastTested", inactiveLabel: false, width: 3, height: 1, decoration: "flat", wordWrap: true) {
			state("default", label: 'Last Manual Test:\n${currentValue}')
		}
		standardTile("refresh", "device.refresh", width:2, height:2, decoration: "flat") {
			state "default", action:"refresh.refresh", icon:"https://raw.githubusercontent.com/tonesto7/nest-manager/master/Images/Devices/refresh_icon.png"
		}
		valueTile("lastUpdatedDt", "device.lastUpdatedDt", width: 3, height: 1, decoration: "flat", wordWrap: true) {
			state("default", label: 'Data Last Received:\n${currentValue}')
		}
		valueTile("devTypeVer", "device.devTypeVer",  width: 3, height: 1, decoration: "flat") {
			state("default", label: 'Device Type:\nv${currentValue}')
		}
		valueTile("apiStatus", "device.apiStatus", width: 2, height: 1, decoration: "flat", wordWrap: true) {
			state "ok", label: "API Status:\nOK"
			state "issue", label: "API Status:\nISSUE ", backgroundColor: "#FFFF33"
		}
		valueTile("debugOn", "device.debugOn", width: 2, height: 1, decoration: "flat") {
			state "true", 	label: 'Debug:\n${currentValue}'
			state "false", 	label: 'Debug:\n${currentValue}'
		}
		valueTile("remind", "device.blah", inactiveLabel: false, width: 6, height: 2, decoration: "flat", wordWrap: true) {
			state("default", label: 'Reminder:\nHTML Content is Available in SmartApp')
		}
		htmlTile(name:"devInfoHtml", action: "getInfoHtml", width: 6, height: 8)

		main "main2"
		// details(["alarmState", "devInfoHtml","remind", "refresh"])
		details(["alarmState", "smoke", "batteryState", "carbonMonoxide", "onlineStatus","debugOn",  "apiStatus",  "lastConnection", "lastUpdatedDt", "lastTested","devTypeVer",  "softwareVer","remind", "refresh"])
   }
}

mappings {
	path("/getInfoHtml") {action: [GET: "getInfoHtml"]}
}

def initialize() {
	Logger("initialized...")
	state?.healthInRepair = false
	if (!state.updatedLastRanAt || now() >= state.updatedLastRanAt + 2000) {
		state.updatedLastRanAt = now()
		verifyHC()
		state?.isInstalled = true
	} else {
		log.trace "initialize(): Ran within last 2 seconds - SKIPPING"
	}
}

void installed() {
	Logger("installed...")
	runIn(5, "initialize", [overwrite: true])
}

void updated() {
	Logger("updated...")
	runIn(5, "initialize", [overwrite: true])
}

def useTrackedHealth() { return state?.useTrackedHealth ?: false }

def getHcTimeout() {
	def toBatt = state?.hcBattTimeout
	def toWire = state?.hcWireTimeout
	return ((device.currentValue("powerSourceNest") == "wired") ? (toWire instanceof Integer ? toWire : 35) : (toBatt instanceof Integer ? toBatt : 1500))*60
}

void verifyHC() {
	if(useTrackedHealth()) {
		def timeOut = getHcTimeout()
		if(!val || val.toInteger() != timeOut) {
			Logger("verifyHC: Updating Device Health Check Interval to $timeOut")
			sendEvent(name: "checkInterval", value: timeOut, data: [protocol: "cloud"], displayed: false)
		}
	} else {
		sendEvent(name: "DeviceWatch-Enroll", value: groovy.json.JsonOutput.toJson(["protocol":"cloud", "scheme":"untracked"]), displayed: false)
	}
	repairHealthStatus(null)
}

def modifyDeviceStatus(status) {
	if(status == null) { return }
	def val = status.toString() == "offline" ? "offline" : "online"
	if(val != getHealthStatus(true)) {
		sendEvent(name: "DeviceWatch-DeviceStatus", value: val.toString(), displayed: false, isStateChange: true)
		Logger("UPDATED: DeviceStatus Event: '$val'")
	}
}

def ping() {
//	if(useTrackedHealth()) {
		Logger("ping...")
		keepAwakeEvent()
//	}
}

def keepAwakeEvent() {
	def lastDt = state?.lastUpdatedDtFmt
	if(lastDt) {
		def ldtSec = getTimeDiffSeconds(lastDt)
		//log.debug "ldtSec: $ldtSec"
		if(ldtSec < 1900) {
			poll()
		}
	}
}

void repairHealthStatus(data) {
	Logger("repairHealthStatus($data)")
	if(state?.hcRepairEnabled != false) {
		if(data?.flag) {
			sendEvent(name: "DeviceWatch-DeviceStatus", value: "online", displayed: false, isStateChange: true)
			state?.healthInRepair = false
		} else {
			state.healthInRepair = true
			sendEvent(name: "DeviceWatch-DeviceStatus", value: "offline", displayed: false, isStateChange: true)
			runIn(7, repairHealthStatus, [data: [flag: true]])
		}
	}
}

def parse(String description) {
	LogAction("Parsing '${description}'")
}

void poll() {
	Logger("polling parent...")
	parent.refresh(this)
}

void refresh() {
	poll()
}

void runSmokeTest() {
	log.trace("runSmokeTest()")
	//values from nest are ok, warning, emergency
	try {
		testingStateEvent("true")
		carbonSmokeStateEvent("ok", "emergency")
		schedEndTest()
	} catch (ex) {
		log.error "runSmokeTest Exception:", ex
		exceptionDataHandler(ex.message, "runSmokeTest")
	}
}

void runCoTest() {
	log.trace("runCoTest()")
	try {
		//values from nest are ok, warning, emergency
		testingStateEvent("true")
		carbonSmokeStateEvent("emergency", "ok")
		schedEndTest()
	} catch (ex) {
		log.error "runCoTest Exception:", ex
		exceptionDataHandler(ex.message, "runCoTest")
	}
}

void runBatteryTest() {
	log.trace("runBatteryTest()")
	try {
		//values from nest are ok, replace
		testingStateEvent("true")
		batteryStateEvent("replace")
		schedEndTest()
	} catch (ex) {
		log.error "runBatteryTest Exception:", ex
		exceptionDataHandler(ex.message, "runBatteryTest")
	}
}

void schedEndTest() {
	try {
		runIn(5, "endTest", [overwrite: true])
		refresh()  // this typically takes more than 5 seconds to complete
	} catch (ex) {
		log.error "schedEndTest Exception:", ex
		exceptionDataHandler(ex.message, "schedEndTest")
	}
}

void endTest() {
	try {
		carbonSmokeStateEvent("ok", "ok")
		batteryStateEvent("ok")
		testingStateEvent("false")
		refresh()
	} catch (ex) {
		log.error "endTest Exception:", ex
		exceptionDataHandler(ex.message, "endTest")
	}
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
		state?.shownChgLog = false
		state.androidDisclaimerShown = false
	}
	def eventData = data?.evt
	state.remove("eventData")

	//log.trace("processEvent Parsing data ${eventData}")
	try {
		LogAction("------------START OF API RESULTS DATA------------", "warn")
		if(eventData) {
			def results = eventData?.data
			state.isBeta = eventData?.isBeta == true ? true : false
			state.hcRepairEnabled = eventData?.hcRepairEnabled == true ? true : false
			state.restStreaming = eventData?.restStreaming == true ? true : false
			state.showLogNamePrefix = eventData?.logPrefix == true ? true : false
			state.enRemDiagLogging = eventData?.enRemDiagLogging == true ? true : false
			state.healthMsg = eventData?.healthNotify == true ? true : false
//			if(useTrackedHealth()) {
				if((eventData.hcBattTimeout && (state?.hcBattTimeout != eventData?.hcBattTimeout || !state?.hcBattTimeout)) || (eventData.hcWireTimeout && (state?.hcWireTimeout != eventData?.hcWireTimeout || !state?.hcWireTimeout))) {
					state.hcBattTimeout = eventData?.hcBattTimeout
					state.hcWireTimeout = eventData?.hcWireTimeout
					verifyHC()
				}
//			}
			state?.useMilitaryTime = eventData?.mt ? true : false
			state.clientBl = eventData?.clientBl == true ? true : false
			state.mobileClientType = eventData?.mobileClientType
			state.nestTimeZone = eventData?.tz ?: null
			state?.showProtActEvts = eventData?.showProtActEvts ? true : false
			carbonSmokeStateEvent(results?.co_alarm_state.toString(),results?.smoke_alarm_state.toString())
			if(!results?.last_connection) { lastCheckinEvent(null, null) }
			else { lastCheckinEvent(results?.last_connection, results?.is_online.toString()) }
			lastTestedEvent(results?.last_manual_test_time)
			apiStatusEvent(eventData?.apiIssues)
			debugOnEvent(eventData?.debug ? true : false)
			//onlineStatusEvent(results?.is_online.toString())
			batteryStateEvent(results?.battery_health.toString())
			testingStateEvent(results?.is_manual_test_active.toString())
			uiColorEvent(results?.ui_color_state.toString())
			softwareVerEvent(results?.software_version.toString())
			deviceVerEvent(eventData?.latestVer.toString())
			state?.devBannerData = eventData?.devBannerData ?: null
			if(eventData?.htmlInfo) { state?.htmlInfo = eventData?.htmlInfo }
			if(eventData?.allowDbException) { state?.allowDbException = eventData?.allowDbException = false ? false : true }
			determinePwrSrc()

			lastUpdatedEvent(true)
			checkHealth()
		}
		return null
	}
	catch (ex) {
		log.error "generateEvent Exception:", ex
		exceptionDataHandler(ex.message, "generateEvent")
	}
}

def getDtNow() {
	def now = new Date()
	return formatDt(now)
}

def formatDt(dt) {
	def tf = new java.text.SimpleDateFormat("E MMM dd HH:mm:ss z yyyy")
	if(getTimeZone()) { tf.setTimeZone(getTimeZone()) }
	else {
		LogAction("SmartThings TimeZone is not set; Please open your ST location and Press Save", "warn", true)
	}
	return tf.format(dt)
}

def getTimeDiffSeconds(strtDate, stpDate=null, methName=null) {
	//LogTrace("[GetTimeDiffSeconds] StartDate: $strtDate | StopDate: ${stpDate ?: "Not Sent"} | MethodName: ${methName ?: "Not Sent"})")
	try {
		if((strtDate && !stpDate) || (strtDate && stpDate)) {
			def now = new Date()
			def stopVal = stpDate ? stpDate.toString() : formatDt(now)
			def startDt = Date.parse("E MMM dd HH:mm:ss z yyyy", strtDate)
			def stopDt = Date.parse("E MMM dd HH:mm:ss z yyyy", stopVal)
			def start = Date.parse("E MMM dd HH:mm:ss z yyyy", formatDt(startDt)).getTime()
			def stop = Date.parse("E MMM dd HH:mm:ss z yyyy", stopVal).getTime()
			def diff = (int) (long) (stop - start) / 1000
			return diff
		} else { return null }
	} catch (ex) {
		log.warn "getTimeDiffSeconds error: Unable to parse datetime..."
	}
}

def getStateSize()      { return state?.toString().length() }
def getStateSizePerc()  { return (int) ((stateSize/100000)*100).toDouble().round(0) }
def getDevTypeId() { return device?.getTypeId() }

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
	if(!tz) { log.warn "getTimeZone: Hub or Nest TimeZone is not found ..." }
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
	if(isStateChange(device, "devVer", dVer.toString())) {
		sendEvent(name: 'devVer', value: dVer, displayed: false)
	}
	if(isStateChange(device, "devTypeVer", newData.toString())) {
		Logger("UPDATED | Device Type Version is: (${newData}) | Original State: (${curData})")
		sendEvent(name: 'devTypeVer', value: newData, displayed: false)
	} else { LogAction("Device Type Version is: (${newData}) | Original State: (${curData})") }
}

def lastCheckinEvent(checkin, isOnline) {
	def formatVal = state?.useMilitaryTime ? "MMM d, yyyy - HH:mm:ss" : "MMM d, yyyy - h:mm:ss a"
	def tf = new SimpleDateFormat(formatVal)
	tf.setTimeZone(getTimeZone())

	def lastChk = device.currentState("lastConnection")?.value
	def lastConnSeconds = lastChk ? getTimeDiffSeconds(lastChk) : 9000   // try not to disrupt running average for pwr determination

	def prevOnlineStat = device.currentState("onlineStatus")?.value

	def hcTimeout = getHcTimeout()
	def curConn = checkin ? "${tf.format(Date.parse("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", checkin))}" : "Not Available"
	def curConnFmt = checkin ? "${formatDt(Date.parse("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", checkin))}" : "Not Available"
	def curConnSeconds = (checkin && curConnFmt != "Not Available") ? getTimeDiffSeconds(curConnFmt) : 3000

	def onlineStat = isOnline.toString() == "true" ? "online" : "offline"
	LogAction("lastCheckinEvent($checkin, $isOnline) | onlineStatus: $onlineStat | lastConnSeconds: $lastConnSeconds | hcTimeout: ${hcTimeout} | curConnSeconds: ${curConnSeconds}")
	if(hcTimeout && isOnline.toString() == "true" && curConnSeconds > hcTimeout && lastConnSeconds > hcTimeout) {
		onlineStat = "offline"
		LogAction("lastCheckinEvent: UPDATED onlineStatus: $onlineStat")
	}

	state?.lastConnection = curConn?.toString()
	if(isStateChange(device, "lastConnection", curConnFmt.toString())) {
		LogAction("UPDATED | Last Nest Check-in was: (${curConnFmt}) | Original State: (${lastChk})")
		sendEvent(name: 'lastConnection', value: curConnFmt?.toString(), displayed: state?.showProtActEvts, isStateChange: true)
		if(lastConnSeconds >= 0 && onlineStat == "online") { addCheckinTime(lastConnSeconds) }
	} else { LogAction("Last Nest Check-in was: (${curConnFmt}) | Original State: (${lastChk})") }

	state?.onlineStatus = onlineStat
	modifyDeviceStatus(onlineStat)
	if(isStateChange(device, "onlineStatus", onlineStat.toString())) {
		Logger("UPDATED | Online Status is: (${onlineStat}) | Original State: (${prevOnlineStat})")
		sendEvent(name: "onlineStatus", value: onlineStat, descriptionText: "Online Status is: ${onlineStat}", displayed: state?.showProtActEvts, isStateChange: true, state: onlineStat)
	} else { LogAction("Online Status is: (${onlineStat}) | Original State: (${prevOnlineStat})") }
}

def addCheckinTime(val) {
	def list = state?.checkinTimeList ?: []
	def listSize = 12
	if(list?.size() < listSize) {
		list.push(val)
	}
	else if(list?.size() > listSize) {
		def nSz = (list?.size()-listSize) + 1
		def nList = list?.drop(nSz)
		nList?.push(val)
		list = nList
	}
	else if(list?.size() == listSize) {
		def nList = list?.drop(1)
		nList?.push(val)
		list = nList
	}
	if(list) { state?.checkinTimeList = list }
}

def determinePwrSrc() {
	if(!state?.checkinTimeList) { state?.checkinTimeList = [] }
	def checkins = state?.checkinTimeList
	def checkinAvg = checkins?.size() ? ( checkins?.sum()?.div(checkins?.size()))?.toDouble()?.round(0).toInteger() : null //
	if(checkins?.size() > 7) {
		if(checkinAvg && checkinAvg < 10000) {
			powerTypeEvent(true)
		} else { powerTypeEvent() }
	}
	//log.debug "checkins: $checkins | Avg: $checkinAvg"
}

def powerTypeEvent(wired=false) {
	def curVal = device.currentState("powerSourceNest")?.value
	def newValSt = wired == true ? "wired" : "battery"
	def newVal = wired == true ? "mains" : "battery"
	state?.powerSource = newValSt
	if(isStateChange(device, "powerSource", newVal) || isStateChange(device, "powerSourceNest", newValSt)) {
		Logger("UPDATED | The Device's Power Source is: (${newVal}) | Original State: (${curVal})")
		sendEvent(name: 'powerSource', value: newVal, displayed: true, isStateChange: true)
		sendEvent(name: 'powerSourceNest', value: newValSt, displayed: true, isStateChange: true)
		verifyHC()
	} else { LogAction("The Device's Power Source is: (${newVal}) | Original State: (${curVal})") }
}

def lastTestedEvent(dt) {
	def lastTstVal = device.currentState("lastTested")?.value
	def formatVal = state?.useMilitaryTime ? "MMM d, yyyy - HH:mm:ss" : "MMM d, yyyy - h:mm:ss a"
	def tf = new SimpleDateFormat(formatVal)
	tf.setTimeZone(getTimeZone())
	def lastTest = !dt ? "No Test Recorded" : "${tf?.format(Date.parse("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", dt))}"
	state?.lastTested = lastTest
	if(isStateChange(device, "lastTested", lastTest.toString())) {
		Logger("UPDATED | Last Manual Test was: (${lastTest}) | Original State: (${lastTstVal})")
		sendEvent(name: 'lastTested', value: lastTest, displayed: true, isStateChange: true)
	} else { LogAction("Last Manual Test was: (${lastTest}) | Original State: (${lastTstVal})") }
}

def softwareVerEvent(ver) {
	def verVal = device.currentState("softwareVer")?.value
	state?.softwareVer = ver
	if(isStateChange(device, "softwareVer", ver.toString())) {
		Logger("UPDATED | Firmware Version: (${ver}) | Original State: (${verVal})")
		sendEvent(name: 'softwareVer', value: ver, descriptionText: "Firmware Version is now v${ver}", displayed: false)
	} else { LogAction("Firmware Version: (${ver}) | Original State: (${verVal})") }
}

def debugOnEvent(debug) {
	def val = device.currentState("debugOn")?.value
	def dVal = debug ? "On" : "Off"
	state?.debugStatus = dVal
	state?.debug = debug.toBoolean() ? true : false
	if(isStateChange(device, "debugOn", dVal.toString())) {
		Logger("UPDATED | Device Debug Logging is: (${dVal}) | Original State: (${val})")
		sendEvent(name: 'debugOn', value: dVal, displayed: false)
	} else { LogAction("Device Debug Logging is: (${dVal}) | Original State: (${val})") }
}

def apiStatusEvent(issue) {
	def curStat = device.currentState("apiStatus")?.value
	def newStat = issue ? "Has Issue" : "Good"
	state?.apiStatus = newStat
	if(isStateChange(device, "apiStatus", newStat.toString())) {
		Logger("UPDATED | API Status is: (${newStat.toString().capitalize()}) | Original State: (${curStat.toString().capitalize()})")
		sendEvent(name: "apiStatus", value: newStat, descriptionText: "API Status is: ${newStat}", displayed: true, isStateChange: true, state: newStat)
	} else { LogAction("API Status is: (${newStat}) | Original State: (${curStat})") }
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

def uiColorEvent(color) {
	def colorVal = device.currentState("uiColor")?.value
	if(isStateChange(device, "uiColor", color.toString())) {
		Logger("UI Color is: (${color}) | Original State: (${colorVal})")
		sendEvent(name:'uiColor', value: color.toString(), displayed: false, isStateChange: true)
	} else { LogAction("UI Color: (${color}) | Original State: (${colorVal})") }
}

def batteryStateEvent(batt) {
	def stbattery = (batt == "replace") ? 5 : 100
	def battVal = device.currentState("batteryState")?.value
	def stbattVal = device.currentState("battery")?.value
	state?.battVal = batt
	if(isStateChange(device, "batteryState", batt.toString()) || !stbattVal) {
		Logger("Battery is: ${batt} | Original State: (${battVal})")
		sendEvent(name:'batteryState', value: batt, descriptionText: "Nest Battery status is: ${batt}", displayed: true, isStateChange: true)
		sendEvent(name:'battery', value: stbattery, descriptionText: "Battery is: ${stbattery}", displayed: true, isStateChange: true)
	} else { LogAction("Battery State: (${batt}) | Original State: (${battVal})") }
}

def testingStateEvent(test) {
	def testVal = device.currentState("isTesting")?.value
	if(isStateChange(device, "isTesting", test.toString())) {
		Logger("Testing State: (${test}) | Original State: (${testVal})")
		//Not displaying the results of this, not sure if it is truly needed
		sendEvent(name:'isTesting', value: test, descriptionText: "Manual test: ${test}", displayed: true, isStateChange: true)
	} else { LogAction("Testing State: (${test}) | Original State: (${testVal})") }
}

def carbonSmokeStateEvent(coState, smokeState) {
	//values in ST are tested, clear, detected
	//values from nest are ok, warning, emergency
	def carbonVal = device.currentState("nestCarbonMonoxide")?.value
	def smokeVal = device.currentState("nestSmoke")?.value
	def testVal = device.currentState("isTesting")?.value

	def alarmStateST = "ok"
	def smokeValStr = "clear"
	def carbonValStr = "clear"

	if (smokeState == "emergency" || smokeState == "warning") {
		alarmStateST = smokeState == "emergency" ? "smoke-emergency" : "smoke-warning"
		smokeValStr = "detected"
	}
	if (coState == "emergency" || coState == "warning") {
		alarmStateST = coState == "emergency" ? "co-emergency" : "co-warning"
		carbonValStr = "detected"
	}
	if(isStateChange(device, "nestSmoke", smokeState.toString())) {
		Logger("Nest Smoke State is: (${smokeState.toString().toUpperCase()}) | Original State: (${smokeVal.toString().toUpperCase()})")
		sendEvent( name: 'nestSmoke', value: smokeState, descriptionText: "Nest Smoke Alarm: ${smokeState}", type: "physical", displayed: true, isStateChange: true )
		sendEvent( name: 'smoke', value: smokeValStr, descriptionText: "Smoke Alarm: ${smokeState} Testing: ${testVal}", type: "physical", displayed: true, isStateChange: true )
	} else { LogAction("Smoke State: (${smokeState.toString().toUpperCase()}) | Original State: (${smokeVal.toString().toUpperCase()})") }
	if(isStateChange(device, "nestCarbonMonoxide", coState.toString())) {
		Logger("Nest CO State is : (${coState.toString().toUpperCase()}) | Original State: (${carbonVal.toString().toUpperCase()})")
		sendEvent( name: 'nestCarbonMonoxide', value: coState, descriptionText: "Nest CO Alarm: ${coState}", type: "physical", displayed: true, isStateChange: true )
		sendEvent( name: 'carbonMonoxide', value: carbonValStr, descriptionText: "CO Alarm: ${coState} Testing: ${testVal}", type: "physical", displayed: true, isStateChange: true )
	} else { LogAction("CO State: (${coState.toString().toUpperCase()}) | Original State: (${carbonVal.toString().toUpperCase()})") }

	//log.info "alarmState: ${alarmStateST} (Nest Smoke: ${smokeState.toString().capitalize()} | Nest CarbonMonoxide: ${coState.toString().capitalize()})"
	if(isStateChange(device, "alarmState", alarmStateST)) {
		sendEvent( name: 'alarmState', value: alarmStateST, descriptionText: "Alarm: ${alarmStateST} (Smoke/CO: ${smokeState}/${coState})", type: "physical", displayed: state?.showProtActEvts )
	}
}

def getHealthStatus(lower=false) {
	def res = device?.getStatus()
	if(lower) { return res.toString().toLowerCase() }
	return res.toString()
}

def healthNotifyOk() {
	def lastDt = state?.lastHealthNotifyDt
	if(lastDt) {
		def ldtSec = getTimeDiffSeconds(lastDt)
		if(ldtSec < 600) {
			return false
		}
	}
	return true
}

def checkHealth() {
	def isOnline = (getHealthStatus() == "ONLINE") ? true : false
	if(isOnline || state?.healthMsg != true || state?.healthInRepair == true) { return }
	if(healthNotifyOk()) {
		def now = new Date()
		parent?.deviceHealthNotify(this, isOnline)
		state.lastHealthNotifyDt = formatDt(now)
	}
}

/************************************************************************************************
|										LOGGING FUNCTIONS										|
*************************************************************************************************/
def lastN(String input, n) {
  return n > input?.size() ? null : n ? input[-n..-1] : ''
}

void Logger(msg, logType = "debug") {
	def smsg = state?.showLogNamePrefix ? "${device.displayName}: ${msg}" : "${msg}"
	def theId = lastN(device.getId().toString(),5)
	if(state?.enRemDiagLogging) {
		parent.saveLogtoRemDiagStore(smsg, logType, "Protect-${theId}")
	} else {
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
	if(msg && methodName) {
		def msgString = "${msg}"
		parent?.sendChildExceptionData("protect", devVer(), msgString, methodName)
	}
}

def incHtmlLoadCnt() 	{ state?.htmlLoadCnt = (state?.htmlLoadCnt ? state?.htmlLoadCnt.toInteger()+1 : 1) }
def incInfoBtnTapCnt()	{ state?.infoBtnTapCnt = (state?.infoBtnTapCnt ? state?.infoBtnTapCnt.toInteger()+1 : 1); return ""; }
def getMetricCntData() {
	return [protHtmlLoadCnt:(state?.htmlLoadCnt ?: 0)]//, protInfoBtnTapCnt:(state?.infoBtnTapCnt ?: 0)]
}

def getCarbonImg(b64=true) {
	def carbonVal = device.currentState("nestCarbonMonoxide")?.value
	//values in ST are tested, clear, detected
	//values from nest are ok, warning, emergency
	def img = ""
	def caption = "${carbonVal ? carbonVal?.toString().toUpperCase() : ""}"
	def captionClass = ""
	switch(carbonVal) {
		case "warning":
			img = b64 ? getFileBase64(getImg("co2_warn_status.png"), "image", "png") : getImg("co2_warn_status.png")
			captionClass = "alarmWarnCap"
			break
		case "emergency":
			img = b64 ? getFileBase64(getImg("co2_emergency_status.png"), "image", "png") : getImg("co2_emergency_status.png")
			captionClass = "alarmEmerCap"
			break
		default:
			img = b64 ? getFileBase64(getImg("co2_clear_status.png"), "image", "png") : getImg("co2_clear_status.png")
			captionClass = "alarmClearCap"
			break
	}
	return ["img":img, "caption": caption, "captionClass":captionClass]
}

def getSmokeImg(b64=true) {
	def smokeVal = device.currentState("nestSmoke")?.value
	//values in ST are tested, clear, detected
	//values from nest are ok, warning, emergency
	def img = ""
	def caption = "${smokeVal ? smokeVal?.toString().toUpperCase() : ""}"
	def captionClass = ""
	switch(smokeVal) {
		case "warning":
			img = b64 ? getFileBase64(getImg("smoke_warn_status.png"), "image", "png") : getImg("smoke_warn_status.png")
			captionClass = "alarmWarnCap"
			break
		case "emergency":
			img = b64 ? getFileBase64(getImg("smoke_emergency_status.png"), "image", "png") : getImg("smoke_emergency_status.png")
			captionClass = "alarmEmerCap"
			break
		default:
			img = b64 ? getFileBase64(getImg("smoke_clear_status.png"), "image", "png") : getImg("smoke_clear_status.png")
			captionClass = "alarmClearCap"
			break
	}
	return ["img":img, "caption": caption, "captionClass":captionClass]
}

def getImg(imgName) {
	if(imgName) {
		return imgName ? "https://cdn.rawgit.com/tonesto7/nest-manager/master/Images/Devices/$imgName" : ""
	} else {
		log.error "getImg Error: Missing imgName value..."
	}
}

def getWebData(params, desc, text=true) {
	try {
		Logger("getWebData: ${desc} data", "info")
		httpGet(params) { resp ->
			if(resp.data) {
				if(text) {
					return resp?.data?.text.toString()
				} else { return resp?.data }
			}
		}
	}
	catch (ex) {
		if(ex instanceof groovyx.net.http.HttpResponseException) {
			Logger("${desc} file not found", "warn")
		} else {
			log.error "getWebData(params: $params, desc: $desc, text: $text) Exception:", ex
		}
		//sendExceptionData(ex, "getWebData")
		return "${label} info not found"
	}
}
def gitRepo()		{ return "tonesto7/nest-manager"}
def gitBranch()		{ return state?.isBeta ? "beta" : "master" }
def gitPath()		{ return "${gitRepo()}/${gitBranch()}"}
def devVerInfo()	{ return getWebData([uri: "https://raw.githubusercontent.com/${gitPath()}/Data/changelog_prot.txt", contentType: "text/plain; charset=UTF-8"], "changelog") }

def getFileBase64(url, preType, fileType) {
	try {
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
	catch (ex) {
		log.error "getFileBase64 Exception:", ex
		exceptionDataHandler(ex.message, "getFileBase64")
	}
}

def getCssData() {
	def cssData = null
	def htmlInfo = state?.htmlInfo
	if(htmlInfo?.cssUrl && htmlInfo?.cssVer) {
		cssData = getFileBase64(htmlInfo.cssUrl, "text", "css")
		state?.cssVer = htmlInfo?.cssVer
	} else {
		cssData = getFileBase64(cssUrl(), "text", "css")
	}
	return cssData
}

def cssUrl()	 { return "https://raw.githubusercontent.com/tonesto7/nest-manager/master/Documents/css/ST-HTML.min.css" }

def disclaimerMsg() {
	if(!state?.disclaimerMsgShown) {
		state.disclaimerMsgShown = true
		return """<div class="orangeAlertBanner">Safety Disclaimer!\nUsing your Nest Protect with SmartThings will not allow for realtime alerts of Fire and Carbon Monoxide!!!</div>"""
	} else { return "" }
}

def androidDisclaimerMsg() {
	if(state?.mobileClientType == "android" && !state?.androidDisclaimerShown) {
		state.androidDisclaimerShown = true
		return """<div class="androidAlertBanner">FYI... The Android Client has a bug with reloading the HTML a second time.\nIt will only load once!\nYou will be required to completely close the client and reload to view the content again!!!</div>"""
	} else { return "" }
}

def getChgLogHtml() {
	def chgStr = ""
	if(!state?.shownChgLog == true) {
		chgStr = """
			<script src="https://cdnjs.cloudflare.com/ajax/libs/jquery/3.1.1/jquery.min.js"></script>
			<script src="https://cdnjs.cloudflare.com/ajax/libs/vex-js/3.1.0/js/vex.combined.min.js"></script>
			<script>
				\$(document).ready(function() {
				    vex.dialog.alert({
						unsafeMessage: `<h3 style="background-color: transparent;">What\'s New with the Protect</h3>
						<div style="padding: 0 5px 0 5px; text-align: left;">
							${devVerInfo()}
						</div>`
				    , className: 'vex-theme-top'})
				});
			</script>
		"""
		state?.shownChgLog = true
	}
	return chgStr
}

def hasHtml() { return true }

def getInfoHtml() {
	try {
		def battImg = (state?.battVal == "low") ? "<img class='battImg' src=\"${getFileBase64(getImg("battery_low_h.png"), "image", "png")}\">" :
				"<img class='battImg' src=\"${getFileBase64(getImg("battery_ok_h.png"), "image", "png")}\">"

		def testVal = device.currentState("isTesting")?.value
		def testModeHTML = (testVal.toString() == "true") ? "<h3>Test Mode</h3>" : ""
		def updateAvail = !state.updateAvailable ? "" : """<div class="greenAlertBanner">Device Update Available!</div>"""
		def clientBl = state?.clientBl ? """<div class="brightRedAlertBanner">Your Manager client has been blacklisted!\nPlease contact the Nest Manager developer to get the issue resolved!!!</div>""" : ""

		def devBrdCastData = state?.devBannerData ?: null
		def devBrdCastHtml = ""
		if(devBrdCastData) {
			def curDt = Date.parse("E MMM dd HH:mm:ss z yyyy", getDtNow())
			def expDt = Date.parse("E MMM dd HH:mm:ss z yyyy", devBrdCastData?.expireDt.toString())
			if(curDt < expDt) {
				devBrdCastHtml = """
					<div class="orangeAlertBanner">
						<div>Message from the Developer:</div>
						<div style="font-size: 4.6vw;">${devBrdCastData?.message}</div>
					</div>
				"""
			}
		}

		def smokeImg = getSmokeImg()
		def carbonImg = getCarbonImg()
		def html = """
		<!DOCTYPE html>
		<html>
			<head>
				<meta http-equiv="cache-control" content="max-age=0"/>
				<meta http-equiv="cache-control" content="no-cache"/>
				<meta http-equiv="expires" content="0"/>
				<meta http-equiv="expires" content="Tue, 01 Jan 1980 1:00:00 GMT"/>
				<meta http-equiv="pragma" content="no-cache"/>
				<meta name="viewport" content="width = device-width, user-scalable=no, initial-scale=1.0">
                <script type="text/javascript" src="${getFileBase64("https://cdnjs.cloudflare.com/ajax/libs/jquery/3.1.1/jquery.min.js", "text", "javascript")}"></script>

				<link rel="stylesheet prefetch" href="${getCssData()}"/>
				<link rel="stylesheet" href="${getFileBase64("https://cdnjs.cloudflare.com/ajax/libs/vex-js/3.1.0/css/vex.min.css", "text", "css")}" />
				<link rel="stylesheet" href="${getFileBase64("https://cdnjs.cloudflare.com/ajax/libs/vex-js/3.1.0/css/vex-theme-top.min.css", "text", "css")}" />
				<style>
				</style>
			</head>
			<body>
			  ${getChgLogHtml()}
			  ${disclaimerMsg()}
			  ${androidDisclaimerMsg()}
			  ${devBrdCastHtml}
			  ${testModeHTML}
			  ${clientBl}
			  ${updateAvail}
			  <div style="padding: 10px;">
				  <section class="sectionBg">
					  <h3>Alarm Status</h3>
					  <table class="devInfo">
					    <col width="48%">
					    <col width="48%">
					    <thead>
						  <th>Smoke Detector</th>
						  <th>Carbon Monoxide</th>
					    </thead>
					    <tbody>
						  <tr>
						    <td>
								<img class='alarmImg' src="${smokeImg?.img}">
								<span class="${smokeImg?.captionClass}">${smokeImg?.caption}</span>
							</td>
						    <td>
								<img class='alarmImg' src="${carbonImg?.img}">
								<span class="${carbonImg?.captionClass}">${carbonImg?.caption}</span>
							</td>
						  </tr>
					    </tbody>
					  </table>
				  </section>
				  <br>
				  <section class="sectionBg">
				  	<h3>Device Info</h3>
					<table class="devInfo">
						<col width="33%">
						<col width="33%">
						<col width="33%">
						<thead>
						  <th>Network Status</th>
						  <th>Power Type</th>
						  <th>API Status</th>
						</thead>
						<tbody>
						  <tr>
						  <td${state?.onlineStatus != "online" ? """ class="redText" """ : ""}>${state?.onlineStatus.toString().capitalize()}</td>
						  <td>${state?.powerSource.toString().capitalize()}</td>
						  <td${state?.apiStatus != "Good" ? """ class="orangeText" """ : ""}>${state?.apiStatus}</td>
						  </tr>
						</tbody>
					</table>
				</section>
				<section class="sectionBg">
					<table class="devInfo">
						<col width="40%">
						<col width="20%">
						<col width="40%">
						<thead>
						  <th>Firmware Version</th>
						  <th>Debug</th>
						  <th>Device Type</th>
						</thead>
						<tbody>
						  <tr>
							<td>v${state?.softwareVer.toString()}</td>
							<td>${state?.debugStatus}</td>
							<td>${state?.devTypeVer.toString()}</td>
						  </tr>
						</tbody>
				  	</table>
				  </section>
				  <section class="sectionBg">
	  				<table class="devInfo">
					  <thead>
						<th>Last Check-In</th>
						<th>Data Last Received</th>
					  </thead>
					  <tbody>
						<tr>
						  <td class="dateTimeText">${state?.lastConnection.toString()}</td>
						  <td class="dateTimeText">${state?.lastUpdatedDt.toString()}</td>
						</tr>
					  </tbody>
					</table>
				  </section>
				</div>
			  <script>
				  function reloadProtPage() {
					  // var url = "https://" + window.location.host + "/api/devices/${device?.getId()}/getInfoHtml"
					  // window.location = url;
					  window.location.reload();
				  }
			  </script>
			  <div class="pageFooterBtn">
				  <button type="button" class="btn btn-info pageFooterBtn" onclick="reloadProtPage()">
					<span>&#10227;</span> Refresh
				  </button>
			  </div>
			</body>
		</html>
		"""
		incHtmlLoadCnt()
		render contentType: "text/html", data: html, status: 200
	}
	catch (ex) {
		log.error "getInfoHtml Exception:", ex
		exceptionDataHandler(ex.message, "getInfoHtml")
	}
}

def getDeviceTile(devNum) {
	try {
		def battImg = (state?.battVal == "low") ? "<img class='battImg' src=\"${getImg("battery_low_h.png")}\">" :
				"<img class='battImg' src=\"${getImg("battery_ok_h.png")}\">"

		def testVal = device.currentState("isTesting")?.value
		def testModeHTML = (testVal.toString() == "true") ? "<h3>Test Mode</h3>" : ""
		def updateAvail = !state.updateAvailable ? "" : """<div class="greenAlertBanner">Device Update Available!</div>"""
		def clientBl = state?.clientBl ? """<div class="brightRedAlertBanner">Your Manager client has been blacklisted!\nPlease contact the Nest Manager developer to get the issue resolved!!!</div>""" : ""

		def smokeImg = getSmokeImg(false)
		def carbonImg = getCarbonImg(false)
		def html = """
		  ${testModeHTML}
		  ${clientBl}
		  ${updateAvail}
		  <div class="device">
			  <section class="sectionBgTile">
				  <h3>Alarm Status</h3>
				  <table class="devInfoTile">
				    <col width="48%">
				    <col width="48%">
				    <thead>
					  <th>Smoke Detector</th>
					  <th>Carbon Monoxide</th>
				    </thead>
				    <tbody>
					  <tr>
					    <td>
							<img class='alarmImg' src="${smokeImg?.img}">
							<span class="${smokeImg?.captionClass}">${smokeImg?.caption}</span>
						</td>
					    <td>
							<img class='alarmImg' src="${carbonImg?.img}">
							<span class="${carbonImg?.captionClass}">${carbonImg?.caption}</span>
						</td>
					  </tr>
				    </tbody>
				  </table>
			  </section>
			  <br>
			  <section class="sectionBgTile">
			  	<h3>Device Info</h3>
				<table class="devInfoTile">
					<col width="33%">
					<col width="33%">
					<col width="33%">
					<thead>
					  <th>Network Status</th>
					  <th>Power Type</th>
					  <th>API Status</th>
					</thead>
					<tbody>
					  <tr>
					  <td${state?.onlineStatus != "online" ? """ class="redText" """ : ""}>${state?.onlineStatus.toString().capitalize()}</td>
					  <td>${state?.powerSource != null ? state?.powerSource.toString().capitalize() : "Not Available Yet"}</td>
					  <td${state?.apiStatus != "Good" ? """ class="orangeText" """ : ""}>${state?.apiStatus}</td>
					  </tr>
					</tbody>
				</table>
			</section>
			<section class="sectionBgTile">
				<table class="devInfoTile">
					<col width="40%">
					<col width="20%">
					<col width="40%">
					<thead>
					  <th>Firmware Version</th>
					  <th>Debug</th>
					  <th>Device Type</th>
					</thead>
					<tbody>
					  <tr>
						<td>v${state?.softwareVer.toString()}</td>
						<td>${state?.debugStatus}</td>
						<td>${state?.devTypeVer.toString()}</td>
					  </tr>
					</tbody>
			  	</table>
			  </section>
			  <section class="sectionBgTile">
  				<table class="devInfoTile">
				  <thead>
					<th>Last Check-In</th>
					<th>Data Last Received</th>
				  </thead>
				  <tbody>
					<tr>
					  <td class="dateTimeTextTile">${state?.lastConnection.toString()}</td>
					  <td class="dateTimeTextTile">${state?.lastUpdatedDt.toString()}</td>
					</tr>
				  </tbody>
				</table>
			  </section>
			</div>
		"""
		return html
	}
	catch (ex) {
		log.error "getDeviceTile Exception:", ex
		exceptionDataHandler(ex.message, "getInfoHtml")
	}
}


private def textDevName()   { return "Nest Protect${appDevName()}" }
private def appDevType()    { return false }
private def appDevName()    { return appDevType() ? " (Dev)" : "" }