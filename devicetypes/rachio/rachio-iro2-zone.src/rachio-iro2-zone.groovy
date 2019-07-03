/**
 *  Rachio IRO2 Zone Device Handler
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
    definition (name: "Rachio Zone", namespace: "rachio", author: "Rachio") {
        capability "Refresh"
        capability "Switch"
        capability "Actuator"
        capability "Valve"
        capability "Sensor"
        capability "Health Check"

        attribute "zoneNumber", "number"
        attribute "zoneName", "string"
        attribute "watering", "string"

        attribute "zoneSquareFeet", "number"
        attribute "zoneWaterTime", "number"
        attribute "zoneTotalDuration", "number"
        attribute "rootZoneDepth", "number"
        attribute "availableWater", "string"

        attribute "efficiency", "string"
        attribute "maxRuntime", "number"
        attribute "saturatedDepthOfWater", "string"
        attribute "depthOfWater", "string"

        //current_schedule data
        attribute "scheduleType", "string"
        
        attribute "zoneDuration", "number"
        attribute "zoneElapsed", "number"
        attribute "zoneStartDate", "string"
        attribute "zoneCycleCount", "number"

        //custom nozzle data
        attribute "nozzleName", "string"

        //custom soil data
        attribute "soilName", "string"

        //custom slope data
        attribute "slopeName", "string"

        //custom crop data
        attribute "cropName", "string"

        //custom shade data
        attribute "shadeName", "string"
        attribute "inStandby", "string"
        attribute "lastUpdatedDt", "string"

        command "stopWatering"
        command "decZoneWaterTime"
        command "incZoneWaterTime"
        command "setZoneWaterTime", ["number"]
        command "startZone"

        command "open"
        command "close"
        command "pause"
    }

    simulator {
        // TODO: define status and reply messages here
    }

    tiles (scale: 2){
        multiAttributeTile(name: "valveTile", type: "generic", width: 6, height: 4) {
            tileAttribute("device.watering", key: "PRIMARY_CONTROL" ) {
                attributeState "off", label: 'Off', action: "open", icon: "st.valves.water.closed", backgroundColor: "#ffffff", nextState:"updating"
                attributeState "offline", label: 'Offline', icon: "st.valves.water.closed", backgroundColor: "#cccccc"
                attributeState "disabled", label: 'Disabled', icon: "st.valves.water.closed", backgroundColor: "#cccccc"
                attributeState "standby", label: 'Standby Mode', icon: "st.valves.water.closed", backgroundColor: "#cccccc"
                attributeState "on", label: 'Watering', action: "close", icon: "st.valves.water.open", backgroundColor: "#00a0dc", nextState: "updating"
                attributeState "updating", label:"Working"
            }
            tileAttribute("device.zoneRunStatus", key: "SECONDARY_CONTROL") {
                attributeState("default", label:'${currentValue}')
            }
        }
        valueTile("zoneName", "device.zoneName", inactiveLabel: true, width: 3, height: 1, decoration: "flat", wordWrap: true) {
            state("default", label: 'Zone:\n${currentValue}')
        }

        valueTile("blank11", "device.blank", width: 1, height: 1, decoration: "flat") {
            state("default", label: '')
        }
        
        //zone Water time control
        valueTile("scheduleType", "device.scheduleType", width: 2, height: 1, decoration: "flat", wordWrap: true) {
            state("default", label: 'Schedule Type:\n${currentValue}')
        }
        valueTile("efficiency", "device.efficiency", width: 2, height: 1, decoration: "flat", wordWrap: true) {
            state("default", label: 'Efficiency:\n${currentValue}')
        }
        valueTile("zoneSquareFeet", "device.zoneSquareFeet", inactiveLabel: false, width: 2 , height: 1, decoration: "flat") {
            state "default", label: 'Zone Area\n${currentValue} sq ft'
        }
        standardTile("leftZoneTimeButton", "device.zoneWaterTime", inactiveLabel: false, decoration: "flat") {
            state "default", action:"decZoneWaterTime", icon:"st.thermostat.thermostat-left"
        }
        valueTile("zoneWaterTime", "device.zoneWaterTime", width: 2, height: 1, decoration: "flat") {
            state "default", label:'Manual Zone Time:\n${currentValue} Minutes'
        }
        controlTile("zoneWaterTimeSliderTile", "device.zoneWaterTime", "slider", width: 4, height: 1, range:'(0..60)') {
            state "default", label: 'Manual Zone Time', action:"setZoneWaterTime"
        }
        standardTile("rightZoneTimeButton", "device.zoneWaterTime", inactiveLabel: false, decoration: "flat") {
            state "default", action:"incZoneWaterTime", icon:"st.thermostat.thermostat-right"
        }
        valueTile("zoneWaterTimeVal", "device.zoneWaterTime", inactiveLabel: false, width: 2 , height: 1, decoration: "flat") {
            state "default", label: 'Water Time\n${currentValue} Minutes'
        }
        valueTile("startZoneTile", "device.zoneWaterTime", inactiveLabel: false, width: 2 , height: 1, decoration: "flat") {
            state "default", label: 'Run This Zone\n${currentValue} Minutes', action:'startZone'
        }

        //nozzle Tiles
        valueTile("nozzleName", "device.nozzleName", inactiveLabel: true, width: 2, height: 1, decoration: "flat") {
            state "default", label: 'Nozzle:\n${currentValue}'
        }
        //Soil Tiles
        valueTile("soilName", "device.soilName", inactiveLabel: true, width: 2, height: 1, decoration: "flat") {
            state "default", label: 'Soil:\n${currentValue}'
        }
        //Slope Tiles
        valueTile("slopeName", "device.slopeName", inactiveLabel: true, width: 2, height: 1, decoration: "flat") {
            state "default", label: 'Slope:\n${currentValue}'
        }
        //Crop Tiles
        valueTile("cropName", "device.cropName", inactiveLabel: true, width: 2, height: 1, decoration: "flat") {
            state "default", label: 'Crop:\n${currentValue}'
        }
        //Shade Tiles
        valueTile("shadeName", "device.shadeName", inactiveLabel: true, width: 2, height: 1, decoration: "flat") {
            state "default", label: 'Shade:\n${currentValue}'
        }

        standardTile("refresh", "device.power", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
            state "default", label:'', action:"refresh.refresh", icon:"st.secondary.refresh"
        }
        standardTile("zoneImage", "device.zoneImage", inactiveLabel: false, width: 1, height: 1, decoration: "flat") {
            state "default", label: '', icon: "http://media.rach.io/images/zone/default/default_zone.jpg"
        }
    }
    main "valveTile"
    details(["valveTile", "zoneImage", "zoneName", "scheduleType", "nozzleName", "soilName", "slopeName", "cropName", "shadeName", "zoneSquareFeet", "leftZoneTimeButton", "zoneWaterTime", "rightZoneTimeButton", "startZoneTile", "lastUpdatedDt", "refresh"])
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
//    updateDataValue("model", "${device?.name}${gen ? " ($gen)" : ""}")
}

void installed() {
    state.isInstalled = true
    initialize()
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
    if (results) {
        if (!results.data?.enabled) {
            sendEvent(name: 'zoneRunStatus', value: "Disabled", displayed: true)
            sendEvent(name: 'watering', value: "disabled", displayed: false)
            sendEvent(name: "DeviceWatch-DeviceStatus", value: "offline", displayed: false)
            return
        }
        // log.debug results
        state.pauseInStandby = (results.pauseInStandby == true)
        state.zoneId = results.data?.id
        state.deviceId = results.devId ? results.devId.toString() : null
        state.zoneNum = results.data?.zoneNumber
        zoneNameEvent(results.data?.name)
        zoneNumEvent(results.data?.zoneNumber)
        state.zoneImageUrl = results.data?.imageUrl

        if (results.status == "ONLINE") {
            state.inStandby = results.standby
            sendEvent(name: 'inStandby', value: results.standby, displayed: true)
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
        if(!device.currentValue("zoneWaterTime")) {
            setZoneWaterTime(parent?.settings?.defaultZoneTime.toInteger())
        }

        availableWaterEvent(results.data?.availableWater)
        rootZoneDepthEvent(results.data?.rootZoneDepth)
        zoneSquareFeetEvent(results.data?.yardAreaSquareFeet)
        zoneTotalDurationEvent(results.data?.runtime)
        saturatedDepthOfWaterEvent(results.data?.saturatedDepthOfWater)
        depthOfWaterEvent(results.data?.depthOfWater)
        maxRuntimeEvent(results.data?.maxRuntime)
        efficiencyEvent(results.data?.efficiency)
        scheduleDataEvent(results.schedData)
        customNozzleDataEvent(results.data?.customNozzle)
        customSoilDataEvent(results.data?.customSoil)
        customSlopeDataEvent(results.data?.customSlope)
        customCropDataEvent(results.data?.customCrop)
        customShadeDataEvent(results.data?.customShade)
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
    log.trace "Watering (Offline)"
    sendEvent(name: 'watering', value: "offline", displayed: true)
    sendEvent(name: 'valve', value: "closed", displayed: false)
    sendEvent(name: 'switch', value: "off", displayed: false)
    sendEvent(name: "DeviceWatch-DeviceStatus", value: "offline", displayed: false)
    sendEvent(name: 'zoneRunStatus', value: "Device is Offline", displayed: false)
}

def markStandby() {
    log.trace "Watering (Standby Mode)"
    sendEvent(name: 'watering', value: "standby", displayed: true)
    sendEvent(name: 'valve', value: "closed", displayed: false)
    sendEvent(name: 'switch', value: "off", displayed: false)
    sendEvent(name: 'zoneRunStatus', value: "Device in Standby Mode", displayed: false)
}

def isWateringEvent(status, zoneId) {
    //log.trace "isWateringEvent..."
    def isOn = (status == "PROCESSING" && device.deviceNetworkId == zoneId)
    def newState = isOn ? "on" : "off"
    log.debug "Watering ${newState}"
    sendEvent(name: 'watering', value: newState, displayed: true)
    sendEvent(name: 'switch', value: newState, displayed: false)
    sendEvent(name: 'valve', value: (isOn ? "open" : "closed"), displayed: false)
}

def zoneTotalDurationEvent(val) {
    def newState = val ? val.toInteger() : 0
    log.debug "Zone Total Duration Value ${newState}"
    sendEvent(name:'zoneTotalDuration', value: newState, displayed: true)
}

def saturatedDepthOfWaterEvent(val) {
    def newState = val ? val.toDouble() : 0.0
    log.debug "Saturated Depth Of Water Value ${newState}"
    sendEvent(name:'saturatedDepthOfWater', value: newState, displayed: true)
}

def depthOfWaterEvent(val) {
    def newState = val ? val.toDouble() : 0.0
    log.debug "Depth Of Water Value ${newState}"
    sendEvent(name:'depthOfWater', value: newState, displayed: true)
}

def efficiencyEvent(val) {
    def newState = val ? val.toDouble() : 0.0
    log.debug "Efficiency Value ${newState}"
    sendEvent(name:'efficiency', value: newState, displayed: false)
}

def maxRuntimeEvent(val) {
    def newState = val ? val.toInteger() : 0
    log.debug "Max Runtime Value ${newState}"
    sendEvent(name:'maxRuntime', value: newState, displayed: false)
}

def availableWaterEvent(val) {
    def newState = val ? val.toDouble() : 0.0
    log.debug "Available Water Value ${newState}"
    sendEvent(name:'availableWater', value: newState, displayed: true)
}

def zoneSquareFeetEvent(val) {
    def newState = val ? val.toInteger() : 0
    log.debug "Zone Area Square Feet ${newState}"
    sendEvent(name:'zoneSquareFeet', value: newState, displayed: true)
}

def rootZoneDepthEvent(val) {
    def newState = val ? val.toInteger() : 0
    log.debug "Root Zone Depth Value ${newState}"
    sendEvent(name:'rootZoneDepth', value: newState, displayed: false)
}

def zoneNumEvent(val) {
    def newState = val ? val.toInteger() : 0
    log.debug "Zone Number ${newState}"
    sendEvent(name:'zoneNumber', value: newState, displayed: true)
}

def zoneNameEvent(val) {
    def newState = val ? val.toString() : "unknown"
    log.debug "Zone Name ${newState}"
    sendEvent(name:'zoneName', value: newState, displayed: true)
}

def setZoneWaterTime(timeVal) {
    def newVal = timeVal ? timeVal.toInteger() : parent?.settings?.defaultZoneTime.toInteger()
    log.debug "Manual Zone Water Time ${newVal}"
    sendEvent(name: 'zoneWaterTime', value: newVal, displayed: true)
}

def fmtString(str) {
    if (str) {
        def out = []
        def tmp = str?.replaceAll("_", " ")?.toLowerCase()?.split(" ")
        tmp?.each { out.push(it?.toString().capitalize()) }
        return out.join(" ")
    }
    return null
}
def scheduleDataEvent(data) {
    //log.trace "scheduleDataEvent($data)..."
    state.curSchedData = data
    def curSchedType = !data?.type ? "off" : data?.type?.toString().toLowerCase()
    state.curSchedType = curSchedType
    state.curScheduleId = !data?.scheduleId ? null : data?.scheduleId
    state.curScheduleRuleId = !data?.scheduleRuleId ? null : data?.scheduleRuleId

    def zoneId = !data?.zoneId ? null : data?.zoneId
    def zoneStartDate = (zoneId == device.deviceNetworkId && data?.zoneStartDate) ? data?.zoneStartDate : null
    def zoneDuration = (zoneId == device.deviceNetworkId && data?.zoneDuration) ? data?.zoneDuration : null
    def timeDiff = data?.zoneStartDate ? GetTimeValDiff(data?.zoneStartDate.toLong()) : 0
    def elapsedDuration = data?.zoneStartDate ? getDurationMinDesc(Math.round(timeDiff)) : 0
    def wateringDuration = zoneDuration ? getDurationMinDesc(zoneDuration) : 0
    def zoneRunStatus = ((!zoneStartDate && !zoneDuration) || (zoneId != device.deviceNetworkId)) ?
            "Status: Idle" : "${curSchedType == "automatic" ? "Scheduled" : "Manual"} Watering: ${elapsedDuration} of ${wateringDuration} Minutes"    
    def zoneCycleCount = (zoneId != device.deviceNetworkId && !data?.totalCycleCount) ? 0 : data?.totalCycleCount

    sendEvent(name: 'scheduleType', value: curSchedType?.capitalize(), displayed: true)
    sendEvent(name: 'zoneDuration', value: zoneDuration, displayed: true)

    sendEvent(name: 'zoneElapsed', value: (zoneDuration && timeDiff && timeDiff > 0 ? timeDiff : null), displayed: false)
    if(!state.inStandby && (device.currentValue("watering") != "offline")) {
        log.debug "ZoneRunStatus ${zoneRunStatus}"
        sendEvent(name: 'zoneRunStatus', value: zoneRunStatus, displayed: true)
    }
    sendEvent(name: 'zoneCycleCount', value: zoneCycleCount, displayed: true)
    sendEvent(name: 'isCycling', value: ((zoneId == device.deviceNetworkId && data?.cycling) ? "True" : "False"), displayed: true)
    sendEvent(name: 'zoneStartDate', value: (zoneStartDate ? epochToDt(zoneStartDate).toString() : "Not Active"), displayed: true)
}

def customNozzleDataEvent(data) {
    //log.trace "customNozzleDataEvent($data)"
    if(data?.name) {
        sendEvent(name:'nozzleName', value: fmtString(data.name).toString(), displayed: true)
    }
}

def customSoilDataEvent(data) {
    //log.trace "customSoilDataEvent($data)"
    if (data?.name) {
        sendEvent(name:'soilName', value: fmtString(data.name).toString(), displayed: true)
    }
}

def customSlopeDataEvent(data) {
    //log.trace "customSlopeDataEvent($data)"
    sendEvent(name:'slopeName', value: (data?.name ? fmtString(data.name).toString() : "N/A"), displayed: true)
}

def customCropDataEvent(data) {
    //log.trace "customCropDataEvent($data)"
    if (data?.name) {
        sendEvent(name:'cropName', value: fmtString(data?.name).toString(), displayed: true)
    }
}

def customShadeDataEvent(data) {
    //log.trace "customShadeDataEvent($data)"
    if (data?.name) {
        sendEvent(name:'shadeName', value: fmtString(data?.name).toString(), displayed: true)
    }
}

def refresh() {
    //log.trace "refresh..."
    parent?.poll(this)
}

def incZoneWaterTime() {
    // log.debug "Decrease Zone Runtime"
    if (device.currentValue("DeviceWatch-DeviceStatus") == "online") {
        def value = device.latestValue('zoneWaterTime')
        setZoneWaterTime(value + 1)
    }
}

def decZoneWaterTime() {
    // log.debug "Increase Zone Runtime"
    if (device.currentValue("DeviceWatch-DeviceStatus") == "online") {
        def value = device.latestValue('zoneWaterTime')
        setZoneWaterTime(value - 1)
    }
}

def isCmdOk2Run() {
    //log.trace "isCmdOk2Run..."
    if (device.currentValue("DeviceWatch-DeviceStatus") == "offline") {
        log.warn "Skipping the request... Because the zone is unable to send commands while it's in an Offline State."
        return false
    }
    if(state.pauseInStandby && state.inStandby) {
        log.warn "Skipping the request... Because the zone is unable to send commands while the controller is in standby mode."
        return false
    }
    return true
}

def startZone() {
    log.trace "startZone()..."
    if (isCmdOk2Run()) {
        def zoneNum = device.latestValue('zoneNumber')
        def waterTime = device.latestValue('zoneWaterTime')
        log.debug("Starting Watering for Zone (${zoneNum}) for (${waterTime}) Minutes")
        if (parent?.startZone(this, state.deviceId, zoneNum, waterTime)) {
            log.debug "runThisZone was Sent Successfully"
            sendEvent(name:'watering', value: "on", displayed: true)
            sendEvent(name:'switch', value: "on", displayed: false)
            sendEvent(name:'valve', value: "open", displayed: false)
        } else {
            markOffLine()
        }
    }
}

def on() {
    log.trace "zone on..."
    if (isCmdOk2Run()) {
        if (device.currentValue("switch") == "off") {
            open()
        } else {
            log.debug "Zone is Already ON... Ignoring.."
        }
    }
}

def off() {
    log.trace "zone off..."
    if (device.currentValue("switch") == "on") {
        close()
    } else {
        log.debug "Zone is Already OFF... Ignoring..."
    }
}

def open() {
    log.trace "Zone open()..."
    if (isCmdOk2Run()) {
        if (device.currentValue("valve") == "closed") {
            startZone()
        } else {
            log.debug "Valve is Already Open... Ignoring..."
        }
    }
}

def close() {
    log.trace "Zone close()..."
    if (device.currentValue("valve") == "open") {
        if (parent?.off(this, state.deviceId)) {
            log.info "Zone was Stopped Successfully..."
            sendEvent(name:'watering', value: "off", displayed: true)
            sendEvent(name:'switch', value: "off", displayed: false)
            sendEvent(name:'valve', value: "closed", displayed: false)
        }
    } else {
        log.debug "Valve is Already Closed... Ignoring..."
    }
}

// To be used directly by smart apps
def stopWatering() {
    log.trace "stopWatering"
    close()
}

def getDtNow() {
    def now = new Date()
    return formatDt(now, false)
}

def epochToDt(val) {
    if (val) {
        return formatDt(new Date(val))
    }
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
        //log.debug "diff: $diff"
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
