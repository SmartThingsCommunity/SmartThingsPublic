/**
 *  Rachio Sprinkler Controller Device Handler
 *
 *  Copyright\u00A9 2017, 2018 Franz Garsombke
 *  Written by Anthony Santilli (@tonesto7)
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

import java.text.SimpleDateFormat

def devVer() { return "2.0.0" }

metadata {
    definition (name: "Rachio Sprinkler Controller", namespace: "rachio", author: "Rachio") {
        capability "Refresh"
        capability "Switch"
        capability "Actuator"
        capability "Valve"
        capability "Sensor"
        capability "Health Check"

        attribute "hardwareModel", "string"
        attribute "hardwareDesc", "string"
        attribute "activeZoneCnt", "number"
        attribute "controllerOn", "string"

        attribute "rainDelay","number"
        attribute "watering", "string"

        //current_schedule data
        attribute "scheduleType", "string"
        attribute "curZoneRunStatus", "string"
        
        attribute "curZoneName", "string"
        attribute "curZoneNumber", "number"
        attribute "curZoneDuration", "number"
        attribute "curZoneStartDate", "string"
        attribute "curZoneIsCycling", "string"
        attribute "curZoneCycleCount", "number"
        attribute "curZoneWaterTime", "number"
        attribute "rainDelayStr", "string"
        attribute "standbyMode", "string"

        attribute "lastUpdatedDt", "string"

        command "stopWatering"
        command "setRainDelay", ["number"]

        command "doSetRainDelay"
        command "decreaseRainDelay"
        command "increaseRainDelay"
        command "setZoneWaterTime", ["number"]
        command "decZoneWaterTime"
        command "incZoneWaterTime"
        command "runAllZones"
        command "standbyOn"
        command "standbyOff"
        //command "pauseScheduleRun"

        command "open"
        command "close"
        //command "pause"
    }

    simulator {
        // TODO: define status and reply messages here
    }

    tiles (scale: 2){
        multiAttributeTile(name: "valveTile", type: "generic", width: 6, height: 4) {
            tileAttribute("device.watering", key: "PRIMARY_CONTROL" ) {
                attributeState "off", label: 'Off', action: "runAllZones", icon: "st.valves.water.closed", backgroundColor: "#ffffff", nextState:"on"
                attributeState "offline", label: 'Offline', icon: "st.valves.water.closed", backgroundColor: "#cccccc"
                attributeState "standby", label: 'Standby Mode', icon: "st.valves.water.closed", backgroundColor: "#cccccc"
                attributeState "on", label: 'Watering', action: "close", icon: "st.valves.water.open", backgroundColor: "#00a0dc", nextState: "off"
            }
            tileAttribute("device.curZoneRunStatus", key: "SECONDARY_CONTROL") {
                attributeState("default", label:'${currentValue}')
            }
        }
        standardTile("hardwareModel", "device.hardwareModel", inactiveLabel: false, width: 2, height: 2, decoration: "flat") {
            state "default", icon: ""
            state "8ZoneV1", icon: "https://s3-us-west-2.amazonaws.com/rachio-media/smartthings/8zone_v1.png"
            state "16ZoneV1", icon: "https://s3-us-west-2.amazonaws.com/rachio-media/smartthings/8zone_v1.png"
            state "8ZoneV2", icon: "https://raw.githubusercontent.com/tonesto7/rachio-manager/master/images/rachio_gen2.png"
            state "16ZoneV2", icon: "https://raw.githubusercontent.com/tonesto7/rachio-manager/master/images/rachio_gen2.png"
            state "8ZoneV3", icon: "https://raw.githubusercontent.com/tonesto7/rachio-manager/master/images/rachio_gen3.png"
            state "16ZoneV3", icon: "https://raw.githubusercontent.com/tonesto7/rachio-manager/master/images/rachio_gen3.png"
        }
        valueTile("hardwareDesc", "device.hardwareDesc", inactiveLabel: false, width: 4, height: 1, decoration: "flat") {
            state "default", label: 'Model:\n${currentValue}'
        }
        valueTile("activeZoneCnt", "device.activeZoneCnt", inactiveLabel: true, width: 4, height: 1, decoration: "flat") {
            state "default", label: 'Active Zones:\n${currentValue}'
        }
        valueTile("controllerOn", "device.controllerOn", inactiveLabel: true, width: 2, height: 1, decoration: "flat") {
            state "default", label: 'Online Status:\n${currentValue}'
        }
        valueTile("controllerRunStatus", "device.controllerRunStatus", inactiveLabel: true, width: 4, height: 2, decoration: "flat") {
            state "default", label: '${currentValue}'
        }
        valueTile("blank", "device.blank", width: 2, height: 1, decoration: "flat") {
            state("default", label: '')
        }
        standardTile("switch", "device.switch", inactiveLabel: false, decoration: "flat") {
            state "off", icon: "st.switch.off"
            state "on", action: "stopWatering", icon: "st.switch.on"
        }
        valueTile("pauseScheduleRun", "device.scheduleTypeBtnDesc", inactiveLabel: false, decoration: "flat", width: 2, height: 1) {
            state "default", label: '${currentValue}', action: "pauseScheduleRun"
        }

        // Rain Delay Control
        standardTile("leftButtonControl", "device.rainDelay", inactiveLabel: false, decoration: "flat") {
            state "default", action:"decreaseRainDelay", icon:"st.thermostat.thermostat-left"
        }
        valueTile("rainDelay", "device.rainDelay", width: 2, height: 1, decoration: "flat") {
            state "default", label:'Rain Delay:\n${currentValue} Days'
        }
        standardTile("rightButtonControl", "device.rainDelay", inactiveLabel: false, decoration: "flat") {
            state "default", action:"increaseRainDelay", icon:"st.thermostat.thermostat-right"
        }
        valueTile("applyRainDelay", "device.rainDelayStr", width: 2, height: 1, inactiveLabel: false, decoration: "flat") {
            state "default", label: '${currentValue}', action:'doSetRainDelay'
        }

        //zone Water time control
        valueTile("lastWateredDesc", "device.lastWateredDesc", width: 4, height: 1, decoration: "flat", wordWrap: true) {
            state("default", label: 'Last Watered:\n${currentValue}')
        }
        standardTile("leftZoneTimeButton", "device.curZoneWaterTime", inactiveLabel: false, decoration: "flat") {
            state "default", action:"decZoneWaterTime", icon:"st.thermostat.thermostat-left"
        }
        valueTile("curZoneWaterTime", "device.curZoneWaterTime", width: 2, height: 1, decoration: "flat") {
            state "default", label:'Manual Zone Time:\n${currentValue} Minutes'
        }
        standardTile("rightZoneTimeButton", "device.curZoneWaterTime", inactiveLabel: false, decoration: "flat") {
            state "default", action:"incZoneWaterTime", icon:"st.thermostat.thermostat-right"
        }
        valueTile("runAllZonesTile", "device.curZoneWaterTime", inactiveLabel: false, width: 2 , height: 1, decoration: "flat") {
            state("default", label: 'Run All Zones\n${currentValue} Minutes', action:'runAllZones')
        }
        standardTile("standbyMode", "device.standbyMode", decoration: "flat", wordWrap: true, width: 2, height: 2) {
            state "on", label:'Turn Standby Off', action:"standbyOff", nextState: "false", icon: "http://cdn.device-icons.smartthings.com/sonos/play-icon@2x.png"
            state "off", label:'Turn Standby On', action:"standbyOn", nextState: "true", icon: "http://cdn.device-icons.smartthings.com/sonos/pause-icon@2x.png"
        }
        standardTile("refresh", "device.power", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
            state "default", label:'', action:"refresh.refresh", icon:"st.secondary.refresh"
        }
    }
    main "valveTile"
    details(["valveTile", "hardwareModel", "hardwareDesc", "activeZoneCnt", "curZoneIsCyclingTile", "leftButtonControl", "rainDelay", "rightButtonControl", "applyRainDelay",
            "leftZoneTimeButton", "curZoneWaterTime", "rightZoneTimeButton", "runAllZonesTile", "lastUpdatedDt", "standbyMode", "refresh"])
}

def getAppImg(imgName) {
    return "https://raw.githubusercontent.com/tonesto7/rachio-manager/master/images/$imgName"
}

// parse events into attributes
def parse(String description) {
    log.debug "Parsing '${description}'"
}

def initialize() {
    sendEvent(name: "DeviceWatch-Enroll", value: groovy.json.JsonOutput.toJson(["protocol":"cloud", "scheme":"untracked"]), displayed: false)
    
    verifyDataAttr()
}

def verifyDataAttr() {
    updateDataValue("HealthEnrolled", "true")
    updateDataValue("manufacturer", "Rachio")
// getDevGeneration is not defined in the connect app...
//    def gen = state.deviceId ? parent?.getDevGeneration(state.deviceId) : null
//    updateDataValue("model", "${device.name}${gen ? " ($gen)" : ""}")
}

void installed() {
    initialize()
    state.isInstalled = true
    sendEvent(name: "DeviceWatch-DeviceStatus", value: "online", displayed: false, isStateChange: true)
}

void updated() {
    initialize()
}

// NOP implementation of ping as health check only calls this for tracked devices
// But as capability defines this method it's implemented to avoid MissingMethodException
def ping() {
    log.info "unexpected ping call from health check"
}

def generateEvent(Map results) {
    if (!state.swVersion || state.swVersion != devVer()) {
        initialize()
        state.swVersion = devVer()
    }
    //log.warn "---------------START OF API RESULTS DATA----------------"
    if (results) {
        // log.debug results
        state.deviceId = device.deviceNetworkId
        state.pauseInStandby = (results.pauseInStandby == true)
        hardwareModelEvent(results.data?.model)
        activeZoneCntEvent(results.data?.zones)
        controllerOnEvent(results.data?.on)

        if (results.status == "ONLINE") {
            state.inStandby = results.standby
            sendEvent(name: 'standbyMode', value: (results.standby?.toString() == "true" ? "on": "off"), displayed: true)
            sendEvent(name: "DeviceWatch-DeviceStatus", value: "online", displayed: false)
            if (results.standby == true && results.pauseInStandby == true) {
                markStandby()
            } else {
                isWateringEvent(results.schedData?.status, results.schedData?.zoneId)
            }
            lastUpdatedEvent()
        } else {
            markOffLine()
        }
        if (!device.currentValue("curZoneWaterTime")) {
            setZoneWaterTime(parent?.settings?.defaultZoneTime.toInteger())
        }
        scheduleDataEvent(results.schedData, results.data.zones, results.rainDelay)
        rainDelayValEvent(results.rainDelay)
    }
}

def getDurationDesc(long secondsCnt) {
    int seconds = secondsCnt %60
    secondsCnt -= seconds
    long minutesCnt = secondsCnt / 60
    long minutes = minutesCnt % 60
    minutesCnt -= minutes
    long hoursCnt = minutesCnt / 60
    return "${minutes} min ${(seconds >= 0 && seconds < 10) ? "0${seconds}" : "${seconds}"} sec"
}

def getDurationMinDesc(long secondsCnt) {
    int seconds = secondsCnt %60
    secondsCnt -= seconds
    long minutesCnt = secondsCnt / 60
    long minutes = minutesCnt % 60
    minutesCnt -= minutes
    long hoursCnt = minutesCnt / 60
    return "${minutes}"
}

def lastUpdatedEvent() {
    state.lastUpdatedDt = formatDt(new Date())?.toString()
    sendEvent(name: 'lastUpdatedDt', value: state.lastUpdatedDt, displayed: false)
}

def markOffLine() {
    log.debug("Watering is set to (Offline)")
    sendEvent(name: 'watering', value: "offline", displayed: true)
    sendEvent(name: 'valve', value: "closed", displayed: false)
    sendEvent(name: 'switch', value: "off", displayed: false)
    sendEvent(name: 'curZoneRunStatus', value: "Device is Offline", displayed: false)
    sendEvent(name: "DeviceWatch-DeviceStatus", value: "offline", displayed: false)
}

def markStandby() {
    log.debug("Watering set to (Standby Mode)")
    sendEvent(name: 'watering', value: "standby", displayed: true)
    sendEvent(name: 'valve', value: "closed", displayed: false)
    sendEvent(name: 'switch', value: "off", displayed: false)
    sendEvent(name: 'curZoneRunStatus', value: "Device in Standby Mode", displayed: false)
}

def isWateringEvent(status, zoneId) {
    //log.trace "isWateringEvent..."
    def curState = device.currentValue("watering")
    def isOn = (status == "PROCESSING")
    def newState = isOn ? "on" : "off"
    parent?.setWateringDeviceState(device.deviceNetworkId, isOn)
    if(curState != newState) {
        log.debug("UPDATED: Watering (${newState}) | Previous: (${curState})")
        sendEvent(name: 'watering', value: newState, displayed: true)
        sendEvent(name: 'switch', value: newState, displayed: false)
        sendEvent(name: 'valve', value: (isOn ? "open" : "closed"), displayed: false)
        if(curState != null) {
            parent?.handleWateringSched(device.deviceNetworkId, isOn)
        }
    }
}

def hardwareModelEvent(val) {
    def newModel = null    // Should these be assigned a defalt value e.g. 'Unknow' ?
    def newDesc = null
    switch(val) {
        case "GENERATION1_8ZONE":
            newModel = "8ZoneV1"
            newDesc = "8-Zone (Gen 1)"
            break
        case "GENERATION1_16ZONE":
            newModel = "16ZoneV1"
            newDesc = "16-Zone (Gen 1)"
            break
        case "GENERATION2_8ZONE":
            newModel = "8ZoneV2"
            newDesc = "8-Zone (Gen 2)"
            break
        case "GENERATION2_16ZONE":
            newModel = "16ZoneV2"
            newDesc = "16-Zone (Gen 2)"
            break
        case "GENERATION3_8ZONE":
            newModel = "8ZoneV3"
            newDesc = "8-Zone (Gen 3)"
            break
        case "GENERATION3_16ZONE":
            newModel = "16ZoneV3"
            newDesc = "16-Zone (Gen 3)"
            break
    }
    log.debug "Controller Model ${newModel}"
    sendEvent(name: 'hardwareModel', value: newModel, displayed: true)

    log.debug "UPDATED: Controller Description ${newDesc}"
    sendEvent(name: 'hardwareDesc', value: newDesc, displayed: true)
}

def activeZoneCntEvent(zData) {
    def zoneCnt = 0
    if (zData) {
        zData.each { z -> if(z?.enabled.toString() == "true") { zoneCnt = zoneCnt+1 } }
    }
    log.debug "Active Zone Count ${zoneCnt}"
    sendEvent(name: 'activeZoneCnt', value: zoneCnt, displayed: true)
}

def controllerOnEvent(val) {
    log.debug "Controller On Status ${newState}"
    sendEvent(name: 'controllerOn', value: newState, displayed: true)
}

def lastWateredDateEvent(val, dur) {
    def newState = "${epochToDt(val)}"
    def newDesc = "${epochToDt(val)}\nDuration: ${getDurationDesc(dur?.toLong())}"
    log.debug "Last Watered Date ${newState}"
    sendEvent(name: 'lastWateredDt', value: newState, displayed: true)
    sendEvent(name: 'lastWateredDesc', value: newDesc, displayed: false)
}

def rainDelayValEvent(val) {
    def newState = val ? val : 0
    log.debug("Rain Delay Value ${newState}")
    sendEvent(name: 'rainDelay', value: newState, displayed: true)
    setRainDelayString(newState)
}

def setZoneWaterTime(timeVal) {
    def newVal = timeVal ? timeVal.toInteger() : parent?.settings?.defaultZoneTime.toInteger()
    log.debug("Manual Zone Water Time (${newVal})")
    sendEvent(name: 'curZoneWaterTime', value: newVal, displayed: true)
}

def scheduleDataEvent(sData, zData, rainDelay) {
    //log.trace "scheduleDataEvent($data)..."
    state.schedData = sData
    state.zoneData = zData
    state.rainData = rainDelay
    //def curSchedTypeBtnDesc = (!curSchedType || curSchedType in ["off", "manual"]) ? "Pause Disabled" : "Pause Schedule"
    state.curSchedType = !sData?.type ? "Off" : sData?.type?.toString().capitalize()
    state.curScheduleId = !sData?.scheduleId ? null : sData?.scheduleId
    state.curScheduleRuleId = !sData?.scheduleRuleId ? null : sData?.scheduleRuleId
    def zoneData = sData && zData ? getZoneData(zData, sData?.zoneId) : null
    def zoneId = !zoneData ? null : sData?.zoneId
    def zoneName = !zoneData ? null : zoneData?.name
    def zoneNum = !zoneData ? null : zoneData?.zoneNumber

    def zoneStartDate = sData?.zoneStartDate ? sData?.zoneStartDate : null
    def zoneDuration = sData?.zoneDuration ? sData?.zoneDuration : null
    
    def timeDiff = sData?.zoneStartDate ? GetTimeValDiff(sData?.zoneStartDate.toLong()) : 0
    def elapsedDuration = sData?.zoneStartDate ? getDurationMinDesc(Math.round(timeDiff)) : 0
    def wateringDuration = zoneDuration ? getDurationMinDesc(zoneDuration) : 0
    def zoneRunStatus = ((!zoneStartDate && !zoneDuration) || !zoneId ) ? "Status: Idle" : "${zoneName}: (${elapsedDuration} of ${wateringDuration} Minutes)"

    def zoneCycleCount = !sData?.totalCycleCount ? 0 : sData?.totalCycleCount
    def zoneIsCycling =  !sData?.cycling ? false : sData?.cycling
    def wateringVal = device.currentValue("watering")
    log.debug("ScheduleType ${state.curSchedType}")
    sendEvent(name: 'scheduleType', value: state.curSchedType, displayed: true)
    if(!state.inStandby && wateringVal != "offline" && isStateChange(device, "curZoneRunStatus", zoneRunStatus)) {
        log.debug("UPDATED: ZoneRunStatus (${zoneRunStatus})")
        sendEvent(name: 'curZoneRunStatus', value: zoneRunStatus, displayed: false)
    }
    log.debug("Active Zone Duration (${zoneDuration})")
    sendEvent(name: 'curZoneDuration', value: zoneDuration?.toString(), displayed: true)

    log.debug("Current Zone Name (${zoneName})")
    sendEvent(name: 'curZoneName', value: zoneName?.toString(), displayed: true)

    log.debug("Active Zone Number (${zoneNum})")
    sendEvent(name: 'curZoneNumber', value: zoneNum, displayed: true)
    log.debug("Zone Cycle Count (${zoneCycleCount})")
    sendEvent(name: 'curZoneCycleCount', value: zoneCycleCount, displayed: true)

    sendEvent(name: 'curZoneIsCycling', value: zoneIsCycling?.toString().capitalize(), displayed: true)

    log.debug("Zone StartDate (${(zoneStartDate ? epochToDt(zoneStartDate).toString() : "Not Active")})")
    sendEvent(name: 'curZoneStartDate', value: (zoneStartDate ? epochToDt(zoneStartDate).toString() : "Not Active"), displayed: true)
}

def getZoneData(zData, zId) {
    if (zData && zId) {
        return zData.find { it?.id == zId }
    }
}

def incZoneWaterTime() {
    // log.debug("Decrease Zone Runtime");
    def value = device.latestValue('curZoneWaterTime')
    setZoneWaterTime(value + 1)
}

def decZoneWaterTime() {
    // log.debug("Increase Zone Runtime");
    def value = device.latestValue('curZoneWaterTime')
    setZoneWaterTime(value - 1)
}

def setRainDelayString( rainDelay) {
    def rainDelayStr = "No Rain Delay";
    if( rainDelay > 0) {
        rainDelayStr = "Rain Delayed";
    }
    sendEvent(name: "rainDelayStr", value: rainDelayStr)
}

def doSetRainDelay() {
    def value = device.latestValue('rainDelay')
    log.debug "Set Rain Delay ${value}"
    if (parent?.setRainDelay(this, state.deviceId, value)) {
        setRainDelayString(value)
    } else {
        markOffLine()
    }
    
}

def updateRainDelay(value) {
    log.debug "Update ${value}" 
    if (value > 7) {
        value = 7;
    } else if (value < 0) {
        value = 0
    }
    sendEvent(name: "rainDelayStr", value: "Set New Rain Delay")
    sendEvent(name: 'rainDelay', value: value, displayed: true)
}

def increaseRainDelay() {
    log.debug "Increase Rain Delay"
    def value = device.latestValue('rainDelay')
    updateRainDelay(value + 1)
}

def decreaseRainDelay() {
    log.debug "Decrease Rain Delay"
    def value = device.latestValue('rainDelay')
    updateRainDelay(value - 1)
}

def refresh() {
    //log.trace "refresh..."
    parent?.poll(this)
}

def isCmdOk2Run() {
    //log.trace "isCmdOk2Run..."
    if (device.currentValue("DeviceWatch-DeviceStatus") == "online") {
        if (!(state.pauseInStandby && state.inStandby)) {
            return true
        }
        log.warn "Skipping the request... Because the controller is unable to send commands while it is in standby mode!!!"
    } else {
        log.warn "Skipping the request... Because the zone is unable to send commands while it's in an Offline State."
    }
    return false
}

def runAllZones() {
    log.trace "runAllZones..."
    if (isCmdOk2Run()) {
        def waterTime = device.latestValue('curZoneWaterTime')
        log.debug "Sending Run All Zones for (${waterTime} Minutes)"
        if (!parent?.runAllZones(this, state.deviceId, waterTime)) {
            markOffLine()
        }
    }
}

def pauseScheduleRun() {
    log.trace "pauseScheduleRun... NOT AVAILABLE YET!!!"
    if (state.curSchedType == "automatic") {
        parent?.pauseScheduleRun(this)
    }
}

def standbyOn() {
    log.trace "standbyOn..."
    if (device.currentValue("watering") == "offline") {
        log.debug "Device is currently Offline... Ignoring..."
    } else if (device.currentValue("standbyMode") == "on") {
        log.debug "Device is Already in Standby... Ignoring..."
    } else {
        if (parent?.standbyOn(this, state.deviceId)) {
            sendEvent(name: 'standbyMode', value: "on", displayed: true)
        }
    }
}

def standbyOff() {
    log.trace "standbyOff..."
    def inStandby = device.currentValue("standbyMode") == "on" ? true : false
    if (device.currentValue("watering") == "offline") {
        log.debug "Device is currently Offline... Ignoring..."
    } else if (device.currentValue("standbyMode") == "on") {
        if (parent?.standbyOff(this, state.deviceId)) {
            sendEvent(name: 'standbyMode', value: "off", displayed: true)
        }
    } else {
        log.debug "Device is Already out of Standby... Ignoring..."
    }
}

def on() {
    log.trace "on..."
    if (isCmdOk2Run()) {
        if (device.currentValue("switch") == "off") {
            open()
        } else {
            log.debug "Switch is Already ON... Ignoring..."
        }
    }
}

def off() {
    log.trace "off..."
    if (device.currentValue("switch") == "on") {
        close()
    } else {
        log.debug "Switch is Already OFF... Ignoring..."
    }
}

def open() {
    log.debug "open command is not currently supported by the controller device..."
}

def close() {
    log.trace "close()..."
    if (device.currentValue("valve") == "open") {
        if (parent?.off(this, state.deviceId)) {
            sendEvent(name:'watering', value: "off", displayed: true)
            sendEvent(name:'switch', value: "off", displayed: false)
            sendEvent(name:'valve', value: "closed", displayed: false)
        } else {
            log.trace "close(). marking offline"
            markOffLine()
        }
    } else {
        log.debug "Close command Ignored... The Valve is Already Closed"
    }
}

// To be used directly by smart apps
def stopWatering() {
    log.trace "stopWatering"
    close()
}

def setRainDelay(rainDelay) {
    sendEvent("name":"rainDelay", "value": value)
    parent?.setRainDelay(this, value)
}

def getDtNow() {
    def now = new Date()
    return formatDt(now, false)
}

def epochToDt(val) {
    return formatDt(new Date(val))
}

def formatDt(dt, mdy = true) {
    def formatVal = mdy ? "MMM d, yyyy - h:mm:ss a" : "E MMM dd HH:mm:ss z yyyy"
    def tf = new SimpleDateFormat(formatVal)
    if (location?.timeZone) {
        tf.setTimeZone(location?.timeZone)
    }
    return tf.format(dt)
}

//Returns time differences is seconds
def GetTimeValDiff(timeVal) {
    try {
        def start = new Date(timeVal).getTime()
        def now = new Date().getTime()
        def diff = (int) (long) (now - start) / 1000
        return diff
    }
    catch (ex) {
        log.error "GetTimeValDiff Exception: ${ex}"
        return 1000
    }
}

def getTimeDiffSeconds(strtDate, stpDate=null) {
    if((strtDate && !stpDate) || (strtDate && stpDate)) {
        def now = new Date()
        def stopVal = stpDate ? stpDate.toString() : formatDt(now, false)
        def start = Date.parse("E MMM dd HH:mm:ss z yyyy", strtDate).getTime()
        def stop = Date.parse("E MMM dd HH:mm:ss z yyyy", stopVal).getTime()
        def diff = (int) (long) (stop - start) / 1000
        return diff
    } else {
        return null
    }
}
