/**
 *  Nest Protect
<<<<<<< HEAD
 *	Authors: Anthony S. (@tonesto7), Ben W. (@desertblade), Eric S. (@E_Sch)
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
 *	Author: Anthony S. (@tonesto7)
 *	Co-Authors: Ben W. (@desertblade), Eric S. (@E_Sch)
 *
 *	Copyright (C) 2017 Anthony S.
 * 	Licensing Info: Located at https://raw.githubusercontent.com/tonesto7/nest-manager/master/LICENSE.md
>>>>>>> origin/master
 */

import java.text.SimpleDateFormat

preferences { }

<<<<<<< HEAD
def devVer() { return "4.1.0" }
=======
def devVer() { return "4.5.1" }
>>>>>>> origin/master

metadata {
	definition (name: "${textDevName()}", author: "Anthony S.", namespace: "tonesto7") {
		//capability "Polling"
		capability "Sensor"
		capability "Battery"
		capability "Smoke Detector"
		capability "Carbon Monoxide Detector"
		capability "Refresh"
<<<<<<< HEAD
		capability "Health Check"
=======
		//capability "Health Check"
>>>>>>> origin/master

		command "refresh"
		command "poll"
		command "log", ["string","string"]
		command "runSmokeTest"
		command "runCoTest"
		command "runBatteryTest"

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
<<<<<<< HEAD
=======
		attribute "powerSource", "string"
>>>>>>> origin/master
		attribute "nestSmoke", "string"
	}

	tiles(scale: 2) {
		multiAttributeTile(name:"alarmState", type:"generic", width:6, height:4) {
			tileAttribute("device.alarmState", key: "PRIMARY_CONTROL") {
				attributeState("default", label:'--', icon: "st.unknown.unknown.unknown")
				attributeState("ok", label:"clear", icon:"st.alarm.smoke.clear", backgroundColor:"#44B621")
				attributeState("smoke-warning", label:"SMOKE!\nWARNING", icon:"st.alarm.smoke.smoke", backgroundColor:"#e8d813")
				attributeState("smoke-emergency", label:"SMOKE!", icon:"st.alarm.smoke.smoke", backgroundColor:"#e86d13")
				attributeState("co-warning", label:"CO!\nWARNING!", icon:"st.alarm.carbon-monoxide.carbon-monoxide", backgroundColor:"#e8d813")
				attributeState("co-emergency", label:"CO!", icon:"st.alarm.carbon-monoxide.carbon-monoxide", backgroundColor:"#e86d13")
			}
			tileAttribute("device.batteryState", key: "SECONDARY_CONTROL") {
				attributeState("default", label:'unknown', icon: "st.unknown.unknown.unknown")
				attributeState("ok", label: "Battery: OK", backgroundColor: "#44B621",
					icon: "https://raw.githubusercontent.com/tonesto7/nest-manager/master/Images/Devices/battery_ok_v.png")
				attributeState("replace", label: "Battery: REPLACE!", backgroundColor: "#e86d13",
					icon: "https://raw.githubusercontent.com/tonesto7/nest-manager/master/Images/Devices/battery_low_v.png")
			}
		}
		standardTile("main2", "device.alarmState", width: 2, height: 2) {
			state("default", label:'--', icon: "st.unknown.unknown.unknown")
			state("ok", label:"clear", backgroundColor:"#44B621",
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
		valueTile("softwareVer", "device.softwareVer", inactiveLabel: false, width: 2, height: 1, decoration: "flat", wordWrap: true) {
			state("default", label: 'Firmware:\nv${currentValue}')
		}
		valueTile("lastConnection", "device.lastConnection", inactiveLabel: false, width: 3, height: 1, decoration: "flat", wordWrap: true) {
			state("default", label: 'Protect Last Checked-In:\n${currentValue}')
		}
		valueTile("lastTested", "device.lastTested", inactiveLabel: false, width: 3, height: 1, decoration: "flat", wordWrap: true) {
			state("default", label: 'Last Manual Test:\n${currentValue}')
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
			state "ok", label: "API Status:\nOK"
			state "issue", label: "API Status:\nISSUE ", backgroundColor: "#FFFF33"
		}
		valueTile("debugOn", "device.debugOn", width: 2, height: 1, decoration: "flat") {
			state "true", 	label: 'Debug:\n${currentValue}'
			state "false", 	label: 'Debug:\n${currentValue}'
		}
		htmlTile(name:"devInfoHtml", action: "getInfoHtml", width: 6, height: 6)

		main "main2"
		details(["alarmState", "devInfoHtml", "refresh"])
   }
}

mappings {
	path("/getInfoHtml") {action: [GET: "getInfoHtml"]}
}

def initialize() {
<<<<<<< HEAD
	Logger("Nest Protect ${textVersion()} ${textCopyright()}")
	poll()
=======
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
	def toBatt = state?.hcBattTimeout
	def toWire = state?.hcWireTimeout
	return ((device.currentValue("powerSource") == "wired") ? (toWire instanceof Integer ? toWire : 35) : (toBatt instanceof Integer ? toBatt : 1500))*60
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
	LogAction("Parsing '${description}'")
}

def poll() {
	Logger("polling parent...")
	parent.refresh(this)
}

def refresh() {
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
<<<<<<< HEAD
		installed()
=======
		initialize()
>>>>>>> origin/master
		state.swVersion = devVer()
	}
	def eventData = data?.evt
	state.remove("eventData")
	//log.trace("processEvent Parsing data ${eventData}")
	try {
		LogAction("------------START OF API RESULTS DATA------------", "warn")
		if(eventData) {
			def results = eventData?.data
<<<<<<< HEAD
			state?.useMilitaryTime = eventData?.mt ? true : false
            state.clientBl = eventData?.clientBl == true ? true : false
			state.mobileClientType = eventData?.mobileClientType
			state.showLogNamePrefix = eventData?.logPrefix == true ? true : false
			state.nestTimeZone = eventData?.tz ?: null
			state?.showProtActEvts = eventData?.showProtActEvts ? true : false
			carbonSmokeStateEvent(results?.co_alarm_state.toString(),results?.smoke_alarm_state.toString())
			if(!results?.last_connection) { lastCheckinEvent(null) }
			else { lastCheckinEvent(results?.last_connection) }
			lastTestedEvent(results?.last_manual_test_time)
			apiStatusEvent(eventData?.apiIssues)
			debugOnEvent(eventData?.debug ? true : false)
			onlineStatusEvent(results?.is_online.toString())
=======
			state.showLogNamePrefix = eventData?.logPrefix == true ? true : false
			state.enRemDiagLogging = eventData?.enRemDiagLogging == true ? true : false

			if((eventData.hcBattTimeout && (state?.hcBattTimeout != eventData?.hcBattTimeout || !state?.hcBattTimeout)) || (eventData.hcWireTimeout && (state?.hcWireTimeout != eventData?.hcWireTimeout || !state?.hcWireTimeout))) {
				state.hcBattTimeout = eventData?.hcBattTimeout
				state.hcWireTimeout = eventData?.hcWireTimeout
				verifyHC()
			}

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
>>>>>>> origin/master
			batteryStateEvent(results?.battery_health.toString())
			testingStateEvent(results?.is_manual_test_active.toString())
			uiColorEvent(results?.ui_color_state.toString())
			softwareVerEvent(results?.software_version.toString())
			deviceVerEvent(eventData?.latestVer.toString())
			if(eventData?.htmlInfo) { state?.htmlInfo = eventData?.htmlInfo }
			if(eventData?.allowDbException) { state?.allowDbException = eventData?.allowDbException = false ? false : true }
<<<<<<< HEAD

			lastUpdatedEvent()
		}

=======
			determinePwrSrc()

			lastUpdatedEvent() //I don't see a need for this any more
		}
>>>>>>> origin/master
		//This will return all of the devices state data to the logs.
		//log.debug "Device State Data: ${getState()}"
		return null
	}
	catch (ex) {
		log.error "generateEvent Exception:", ex
		exceptionDataHandler(ex.message, "generateEvent")
	}
}

<<<<<<< HEAD
=======
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

>>>>>>> origin/master
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
	if(!curData?.equals(newData)) {
		Logger("UPDATED | Device Type Version is: (${newData}) | Original State: (${curData})")
		sendEvent(name: 'devTypeVer', value: newData, displayed: false)
	} else { LogAction("Device Type Version is: (${newData}) | Original State: (${curData})") }
}

<<<<<<< HEAD
def lastCheckinEvent(checkin) {
	def formatVal = state?.useMilitaryTime ? "MMM d, yyyy - HH:mm:ss" : "MMM d, yyyy - h:mm:ss a"
	def tf = new SimpleDateFormat(formatVal)
	tf.setTimeZone(getTimeZone())
	def lastConn = checkin ? "${tf?.format(Date.parse("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", checkin))}" : "Not Available"
	def lastChk = device.currentState("lastConnection")?.value
	state?.lastConnection = lastConn?.toString()
	if(!lastChk.equals(lastConn?.toString())) {
		Logger("UPDATED | Last Nest Check-in was: (${lastConn}) | Original State: (${lastChk})")
		sendEvent(name: 'lastConnection', value: lastConn?.toString(), displayed: state?.showProtActEvts, isStateChange: true)
	} else { LogAction("Last Nest Check-in was: (${lastConn}) | Original State: (${lastChk})") }
=======
def lastCheckinEvent(checkin, isOnline) {
	//log.debug "lastCheckinEvent($checkin)"
	def formatVal = state?.useMilitaryTime ? "MMM d, yyyy - HH:mm:ss" : "MMM d, yyyy - h:mm:ss a"
	def lastChk = device.currentState("lastConnection")?.value
	def isOn = device.currentState("onlineStatus")?.value
	def onlineStat = isOn ? isOn.toString() : "Offline"

	def tf = new SimpleDateFormat(formatVal)
		tf.setTimeZone(getTimeZone())

	def hcTimeout = getHcTimeout()
	def lastConn = checkin ? "${tf.format(Date.parse("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", checkin))}" : "Not Available"
	def lastConnFmt = checkin ? "${formatDt(Date.parse("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", checkin))}" : "Not Available"
	def lastConnSeconds = checkin ? getTimeDiffSeconds(lastChk) : 3000

	state?.lastConnection = lastConn?.toString()
	if(isStateChange(device, "lastConnection", lastConnFmt.toString())) {
		Logger("UPDATED | Last Nest Check-in was: (${lastConnFmt}) | Original State: (${lastChk})")
		sendEvent(name: 'lastConnection', value: lastConnFmt?.toString(), displayed: state?.showProtActEvts, isStateChange: true)

		if(hcTimeout && lastConnSeconds >= 0) { onlineStat = lastConnSeconds < hcTimeout ? "Online" : "Offline" }
		//log.debug "lastConnSeconds: $lastConnSeconds"
		if(lastConnSeconds >=0) { addCheckinTime(lastConnSeconds) }
	} else { LogAction("Last Nest Check-in was: (${lastConnFmt}) | Original State: (${lastChk})") }
	if(isOnline != "true") { onlineStat = "Offline" }
	state?.onlineStatus = onlineStat
	if(isStateChange(device, "onlineStatus", onlineStat)) {
		Logger("UPDATED | Online Status is: (${onlineStat}) | Original State: (${isOn})")
		sendEvent(name: "onlineStatus", value: onlineStat, descriptionText: "Online Status is: ${onlineStat}", displayed: state?.showProtActEvts, isStateChange: true, state: onlineStat)
	} else { LogAction("Online Status is: (${onlineStat}) | Original State: (${isOn})") }
}

def addCheckinTime(val) {
	def list = state?.checkinTimeList ?: []
	def listSize = 7
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
	def checkinAvg = checkins?.size() ? (checkins?.sum()/checkins?.size()).toDouble().round(0).toInteger() : null
	if(checkinAvg && checkinAvg < 10000) {
		powerTypeEvent(true)
	} else { powerTypeEvent(false) }
	log.debug "checkins: $checkins | Avg: $checkinAvg"
}

def powerTypeEvent(wired) {
	def curVal = device.currentState("powerSource")?.value
	def newVal = wired == true ? "wired" : "battery"
	state?.powerSource = newVal
	if(isStateChange(device, "powerSource", newVal)) {
		Logger("UPDATED | The Device's Power Source is: (${newVal}) | Original State: (${curVal})")
		sendEvent(name: 'powerSource', value: newVal, displayed: true, isStateChange: true)
		verifyHC()
	} else { LogAction("The Device's Power Source is: (${newVal}) | Original State: (${curVal})") }
>>>>>>> origin/master
}

def lastTestedEvent(dt) {
	def lastTstVal = device.currentState("lastTested")?.value
	def formatVal = state?.useMilitaryTime ? "MMM d, yyyy - HH:mm:ss" : "MMM d, yyyy - h:mm:ss a"
	def tf = new SimpleDateFormat(formatVal)
	tf.setTimeZone(getTimeZone())
	def lastTest = !dt ? "No Test Recorded" : "${tf?.format(Date.parse("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", dt))}"
	state?.lastTested = lastTest
	if(!lastTstVal.equals(lastTest?.toString())) {
		Logger("UPDATED | Last Manual Test was: (${lastTest}) | Original State: (${lastTstVal})")
		sendEvent(name: 'lastTested', value: lastTest, displayed: true, isStateChange: true)
	} else { LogAction("Last Manual Test was: (${lastTest}) | Original State: (${lastTstVal})") }
}

def softwareVerEvent(ver) {
	def verVal = device.currentState("softwareVer")?.value
	state?.softwareVer = ver
	if(!verVal.equals(ver)) {
		Logger("UPDATED | Firmware Version: (${ver}) | Original State: (${verVal})")
		sendEvent(name: 'softwareVer', value: ver, descriptionText: "Firmware Version is now v${ver}", displayed: false)
	} else { LogAction("Firmware Version: (${ver}) | Original State: (${verVal})") }
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
	def newStat = issue ? "issue" : "ok"
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

>>>>>>> origin/master
def uiColorEvent(color) {
	def colorVal = device.currentState("uiColor")?.value
	if(!colorVal.equals(color)) {
		Logger("UI Color is: (${color}) | Original State: (${colorVal})")
		sendEvent(name:'uiColor', value: color.toString(), displayed: false, isStateChange: true)
	} else { LogAction("UI Color: (${color}) | Original State: (${colorVal})") }
}

<<<<<<< HEAD
def onlineStatusEvent(online) {
	def isOn = device.currentState("onlineStatus")?.value
	def val = online ? "Online" : "Offline"
	state?.onlineStatus = val
	if(!isOn.equals(val)) {
		Logger("UPDATED | Online Status is: (${val}) | Original State: (${isOn})")
		sendEvent(name: "onlineStatus", value: val, descriptionText: "Online Status is: ${val}", displayed: state?.showProtActEvts, isStateChange: true, state: val)
	} else { LogAction("Online Status is: (${val}) | Original State: (${isOn})") }
}

=======
>>>>>>> origin/master
def batteryStateEvent(batt) {
	def stbattery = (batt == "replace") ? 5 : 100
	def battVal = device.currentState("batteryState")?.value
	def stbattVal = device.currentState("battery")?.value
	state?.battVal = batt
	if(!battVal.equals(batt) || !stbattVal) {
		Logger("Battery is: ${batt} | Original State: (${battVal})")
		sendEvent(name:'batteryState', value: batt, descriptionText: "Nest Battery status is: ${batt}", displayed: true, isStateChange: true)
		sendEvent(name:'battery', value: stbattery, descriptionText: "Battery is: ${stbattery}", displayed: true, isStateChange: true)
	} else { LogAction("Battery State: (${batt}) | Original State: (${battVal})") }
}

def testingStateEvent(test) {
	def testVal = device.currentState("isTesting")?.value
	if(!testVal.equals(test)) {
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
	if(!smokeVal.equals(smokeState)) {
		Logger("Nest Smoke State is: (${smokeState.toString().toUpperCase()}) | Original State: (${smokeVal.toString().toUpperCase()})")
		sendEvent( name: 'nestSmoke', value: smokeState, descriptionText: "Nest Smoke Alarm: ${smokeState}", type: "physical", displayed: true, isStateChange: true )
		sendEvent( name: 'smoke', value: smokeValStr, descriptionText: "Smoke Alarm: ${smokeState} Testing: ${testVal}", type: "physical", displayed: true, isStateChange: true )
	} else { LogAction("Smoke State: (${smokeState.toString().toUpperCase()}) | Original State: (${smokeVal.toString().toUpperCase()})") }
	if(!carbonVal.equals(coState)) {
		Logger("Nest CO State is : (${coState.toString().toUpperCase()}) | Original State: (${carbonVal.toString().toUpperCase()})")
		sendEvent( name: 'nestCarbonMonoxide', value: coState, descriptionText: "Nest CO Alarm: ${coState}", type: "physical", displayed: true, isStateChange: true )
		sendEvent( name: 'carbonMonoxide', value: carbonValStr, descriptionText: "CO Alarm: ${coState} Testing: ${testVal}", type: "physical", displayed: true, isStateChange: true )
	} else { LogAction("CO State: (${coState.toString().toUpperCase()}) | Original State: (${carbonVal.toString().toUpperCase()})") }

	//log.info "alarmState: ${alarmStateST} (Nest Smoke: ${smokeState.toString().capitalize()} | Nest CarbonMonoxide: ${coState.toString().capitalize()})"
<<<<<<< HEAD
	sendEvent( name: 'alarmState', value: alarmStateST, descriptionText: "Alarm: ${alarmStateST} (Smoke/CO: ${smokeState}/${coState}) ( ${stvalStr} )", type: "physical", displayed: state?.showProtActEvts )
=======
	if(isStateChange(device, "alarmState", alarmStateST)) {
		sendEvent( name: 'alarmState', value: alarmStateST, descriptionText: "Alarm: ${alarmStateST} (Smoke/CO: ${smokeState}/${coState})", type: "physical", displayed: state?.showProtActEvts )
	}
>>>>>>> origin/master
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
<<<<<<< HEAD
=======
	if(state?.enRemDiagLogging) {
		parent.saveLogtoRemDiagStore(smsg, logType, "Protect DTH")
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

def getCarbonImg() {
	def carbonVal = device.currentState("nestCarbonMonoxide")?.value
	//values in ST are tested, clear, detected
	//values from nest are ok, warning, emergency
	switch(carbonVal) {
		case "warning":
			return getImgBase64(getImg("co_warn_tile.png"), "png")
			break
		case "emergency":
			return getImgBase64(getImg("co_emergency_tile.png"), "png")
			break
		default:
			return getImgBase64(getImg("co_clear_tile.png"), "png")
			break
	}
}

def getSmokeImg() {
	def smokeVal = device.currentState("nestSmoke")?.value
	//values in ST are tested, clear, detected
	//values from nest are ok, warning, emergency
	switch(smokeVal) {
		case "warning":
			return getImgBase64(getImg("smoke_warn_tile.png"), "png")
			break
		case "emergency":
			return getImgBase64(getImg("smoke_emergency_tile.png"), "png")
			break
		default:
			return getImgBase64(getImg("smoke_clear_tile.png"), "png")
			break
	}
}

def getImgBase64(url,type) {
	try {
		def params = [
			uri: url,
			contentType: 'image/$type'
		]
		httpGet(params) { resp ->
			if(resp.data) {
				def respData = resp?.data
				ByteArrayOutputStream bos = new ByteArrayOutputStream()
				int len
				int size = 2048
				byte[] buf = new byte[size]
				while ((len = respData.read(buf, 0, size)) != -1)
					   bos.write(buf, 0, len)
				buf = bos.toByteArray()
				//log.debug "buf: $buf"
				String s = buf?.encodeBase64()
				//log.debug "resp: ${s}"
				return s ? "data:image/${type};base64,${s.toString()}" : null
			}
		}
	}
	catch (ex) {
		log.error "getImgBase64 Exception: $ex", ex
		exceptionDataHandler(ex.message, "getImgBase64")
	}
}

def getTestImg(imgName) { return imgName ? "https://raw.githubusercontent.com/tonesto7/nest-manager/master/Images/Devices/Test/$imgName" : "" }
def getImg(imgName) {
	if(imgName) {
		return imgName ? "https://cdn.rawgit.com/tonesto7/nest-manager/master/Images/Devices/$imgName" : ""
	} else {
		log.error "getImg Error: Missing imgName value..."
	}
}

def getFileBase64(url,preType,fileType) {
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

def getCSS(){
	def params = [
		uri: state?.cssUrl.toString(),
		contentType: 'text/css'
	]
	httpGet(params)  { resp ->
		return resp?.data.text
	}
}

def getCssData() {
	def cssData = null
	def htmlInfo = state?.htmlInfo
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
=======
def cssUrl()	 { return "https://raw.githubusercontent.com/tonesto7/nest-manager/master/Documents/css/ST-HTML.css" }
>>>>>>> origin/master

def getInfoHtml() {
	try {
		def battImg = (state?.battVal == "low") ? "<img class='battImg' src=\"${getImgBase64(getImg("battery_low_h.png"), "png")}\">" :
				"<img class='battImg' src=\"${getImgBase64(getImg("battery_ok_h.png"), "png")}\">"

		def testVal = device.currentState("isTesting")?.value
		def testModeHTML = (testVal.toString() == "true") ? "<h3>Test Mode</h3>" : ""
<<<<<<< HEAD
		def updateAvail = !state.updateAvailable ? "" : "<h3>Device Update Available!</h3>"
		def clientBl = state?.clientBl ? """<h3>Your Manager client has been blacklisted!\nPlease contact the Nest Manager developer to get the issue resolved!!!</h3>""" : ""
=======
		def updateAvail = !state.updateAvailable ? "" : """<div class="greenAlertBanner">Device Update Available!</div>"""
		def clientBl = state?.clientBl ? """<div class="brightRedAlertBanner">Your Manager client has been blacklisted!\nPlease contact the Nest Manager developer to get the issue resolved!!!</div>""" : ""

>>>>>>> origin/master
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
<<<<<<< HEAD
				<link rel="stylesheet prefetch" href="${getCssData()}"/>
				<style>
                .modal {
                    display: none; /* Hidden by default */
                    position: fixed; /* Stay in place */
                    z-index: 1; /* Sit on top */
                    left: 0;
                    top: 0;
                    width: 100%; /* Full width */
                    height: 100%; /* Full height */
                    overflow: auto; /* Enable scroll if needed */
                    background-color: rgb(0,0,0); /* Fallback color */
                    background-color: rgba(0,0,0,0.4); /* Black w/ opacity */
                }
                .modal-content {
                    background-color: #fefefe;
                    margin: 5% auto;
                    padding: 20px;
                    border: 1px solid #888;
                    width: 80%;
                }
              </style>
			</head>
			<body>
			  ${clientBl}
			  ${updateAvail}
			  ${testModeHTML}
=======
                <script type="text/javascript" src="${getFileBase64("https://ajax.googleapis.com/ajax/libs/jquery/1.10.2/jquery.min.js", "text", "javascript")}"></script>
				<script type="text/javascript" src="${getFileBase64("https://cdnjs.cloudflare.com/ajax/libs/vex-js/3.0.0/js/vex.combined.min.js", "text", "javascript")}"></script>

				<link rel="stylesheet" href="${getFileBase64("https://cdnjs.cloudflare.com/ajax/libs/vex-js/3.0.0/css/vex.css", "text", "css")}" />
				<link rel="stylesheet" href="${getFileBase64("https://cdnjs.cloudflare.com/ajax/libs/vex-js/3.0.0/css/vex-theme-default.css", "text", "css")}" />
				<script>vex.defaultOptions.className = 'vex-theme-default'</script>
				<link rel="stylesheet prefetch" href="${getCssData()}"/>
				<style>
					.vex.vex-theme-default .vex-content {
						width: 98%; padding: 3px;
					}
				</style>
			</head>
			<body>
			  ${testModeHTML}
			  ${clientBl}
			  ${updateAvail}
>>>>>>> origin/master
			  <div class="row">
				<div class="offset-by-two four columns centerText">
				  <img class='alarmImg' src="${getCarbonImg()}">
				</div>
				<div class="four columns centerText">
				  <img class='alarmImg' src="${getSmokeImg()}">
				</div>
			  </div>
<<<<<<< HEAD
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
				  <th>Nest Last Checked-In</th>
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
				  <tbody>
					<tr>
					  <td><p class="centerText"><a class="more-info button">More Info</a></p></td>
					</tr>
				  </tbody>
				</table>
				<br></br>
			  <script>
				  \$('.more-info').click(function(){
					  vex.dialog.alert({ unsafeMessage: `
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
						  <table>
							<thead>
							  <th>Nest Last Checked-In</th>
							  <th>Data Last Received</th>
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
		render contentType: "text/html", data: html, status: 200
	}
	catch (ex) {
		log.error "getInfoHtml Exception:", ex
		exceptionDataHandler(ex.message, "getInfoHtml")
	}
}

private def textDevName()   { return "Nest Protect${appDevName()}" }
private def appDevType()    { return false }
private def appDevName()    { return appDevType() ? " (Dev)" : "" }
