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
 *	Modified: 7-30-2018
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
        capability "Polling"
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
                attributeState "on", label: 'Watering', action: "close", icon: "st.valves.water.open", backgroundColor: "#00A7E1", nextState: "updating"
                attributeState "off", label: 'Off', action: "open", icon: "st.valves.water.closed", backgroundColor: "#7e7d7d", nextState:"updating"
                attributeState "offline", label: 'Offline', icon: "st.valves.water.closed", backgroundColor: "#FE2E2E"
                attributeState "standby", label: 'Standby Mode', icon: "st.valves.water.closed", backgroundColor: "#FFAE42"
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
    sendEvent(name: "DeviceWatch-DeviceStatus", value: "online", displayed: false, isStateChange: true)
    sendEvent(name: "DeviceWatch-Enroll", value: groovy.json.JsonOutput.toJson(["protocol":"cloud", "scheme":"untracked"]), displayed: false)
    verifyDataAttr()
}

def verifyDataAttr() {
    updateDataValue("HealthEnrolled", "true")
    updateDataValue("manufacturer", "Rachio")
    def gen = state?.deviceId ? parent?.getDevGeneration(state?.deviceId) : null
    updateDataValue("model", "${device?.name}${gen ? " ($gen)" : ""}")
}

void installed() {
    state.isInstalled = true
    initialize()
}

void updated() {
    initialize()
}

def generateEvent(Map results) {
    if(!state?.swVersion || state?.swVersion != devVer()) {
        initialize()
        state.swVersion = devVer()
    }
    if(results) {
        // log.debug results
        state?.pauseInStandby = (results?.pauseInStandby == true)
        state?.zoneId = results?.data?.id
        state?.deviceId = results?.devId ? results?.devId.toString() : null
        state?.zoneNum = results?.data?.zoneNumber

        zoneNameEvent(results?.data?.name)
        zoneNumEvent(results?.data?.zoneNumber)

        state?.zoneImageUrl = results?.data?.imageUrl
        def isOnline = results?.status == "ONLINE" ? true : false
        state?.isOnline = isOnline
        if(!isOnline) {
            markOffLine()
        } else {
            state?.inStandby = results?.standby
            if(isStateChange(device, "inStandby", results?.standby.toString())) {
                sendEvent(name: 'inStandby', value: results?.standby, displayed: true, isStateChange: true)
            }
            def isOn = isWatering(results?.schedData)
            if(results?.standby == true && results?.pauseInStandby == true) {
                markStandby()
            } else {
                isWateringEvent(results?.schedData?.status, results?.schedData?.zoneId)
            }
        }

        availableWaterEvent(results?.data?.availableWater)
        rootZoneDepthEvent(results?.data?.rootZoneDepth)
        zoneSquareFeetEvent(results?.data?.yardAreaSquareFeet)
        // lastWateredDurationEvent(results?.data?.lastWateredDuration)
        // lastWateredDateEvent(results?.data?.lastWateredDate, results?.data?.lastWateredDuration)
        zoneTotalDurationEvent(results?.data?.runtime)
        saturatedDepthOfWaterEvent(results?.data?.saturatedDepthOfWater)
        depthOfWaterEvent(results?.data?.depthOfWater)
        maxRuntimeEvent(results?.data?.maxRuntime)
        efficiencyEvent(results?.data?.efficiency)

        if(!device?.currentState("zoneWaterTime")?.value) {
            setZoneWaterTime(parent?.settings?.defaultZoneTime.toInteger())
        }

        scheduleDataEvent(results?.schedData)

        customNozzleDataEvent(results?.data?.customNozzle)
        customSoilDataEvent(results?.data?.customSoil)
        customSlopeDataEvent(results?.data?.customSlope)
        customCropDataEvent(results?.data?.customCrop)
        customShadeDataEvent(results?.data?.customShade)

        if(isOnline) { lastUpdatedEvent() }
    }
    return "hello from zone"
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
    def lastDt = formatDt(new Date())
    def lastUpd = device?.currentState("lastUpdatedDt")?.stringValue
    state?.lastUpdatedDt = lastDt?.toString()
    if(isStateChange(device, "lastUpdatedDt", lastDt.toString())) {
        // log.info "${device?.displayName} is (${state?.isOnline ? "Online and ${state?.inStandby ? "in Standby Mode" : "Active"}" : "OFFLINE"}) - Last Updated: (${lastDt})"
        sendEvent(name: 'lastUpdatedDt', value: lastDt?.toString(), displayed: false)
    }
}

def markOffLine() {
	if(isStateChange(device, "watering", "offline")) { 
		log.trace("UPDATED: Watering (Offline)")
		sendEvent(name: 'watering', value: "offline", displayed: true, isStateChange: true)
		sendEvent(name: 'valve', value: "closed", displayed: false, isStateChange: true)
		sendEvent(name: 'switch', value: "off", displayed: false, isStateChange: true)
	}
	if(isStateChange(device, "zoneRunStatus", "Device in Offline")) {
		sendEvent(name: 'zoneRunStatus', value: "Device is Offline", displayed: false, isStateChange: true)
	}
}

def markStandby() {
	if(isStateChange(device, "watering", "standby")) {
		log.trace("UPDATED: Watering (Standby Mode)")
		sendEvent(name: 'watering', value: "standby", displayed: true, isStateChange: true)
		sendEvent(name: 'valve', value: "closed", displayed: false, isStateChange: true)
		sendEvent(name: 'switch', value: "off", displayed: false, isStateChange: true)
	}
	if(isStateChange(device, "zoneRunStatus", "Device in Standby Mode")) {
		sendEvent(name: 'zoneRunStatus', value: "Device in Standby Mode", displayed: false, isStateChange: true)
	}
}

def isWateringEvent(status, zoneId) {
    //log.trace "isWateringEvent..."
    def curState = device?.currentState("watering")?.value.toString()
    def isOn = (status == "PROCESSING" && device?.deviceNetworkId == zoneId) ? true : false
    def newState = isOn ? "on" : "off"
    if(isStateChange(device, "watering", newState.toString())) {
        log.debug("UPDATED: Watering (${newState}) | Previous: (${curState})")
        sendEvent(name: 'watering', value: newState, displayed: true, isStateChange: true)
        sendEvent(name: 'switch', value: (isOn ? "on" : "off"), displayed: false, isStateChange: true)
        sendEvent(name: 'valve', value: (isOn ? "open" : "closed"), displayed: false, isStateChange: true)
    }
}

def lastWateredDateEvent(val, dur) {
    log.trace "lastWateredDateEvent($val, $dur)"
    if( val == null || dur == null) { return }
    def newState = "${epochToDt(val)}"
    def newDesc = "${epochToDt(val)}" //\nDuration: ${getDurationDesc(dur?.toLong())}"
    def curState = device?.currentState("lastWateredDt")?.value
    if(isStateChange(device, "lastWateredDt", newState.toString())) { 
        log.debug "UPDATED: Last Watered Date is (${newState}) | Previous: (${curState})"
        sendEvent(name: 'lastWateredDt', value: newState, displayed: true, isStateChange: true)
    }
    if(isStateChange(device, "lastWateredDesc", (newDesc ? newDesc.toString() : "Not Available"))) {
        sendEvent(name: 'lastWateredDesc', value: newDesc ?: "Not Available", displayed: false, isStateChange: true)
    }
}

def lastWateredDurationEvent(val) {
    def curState = device?.currentState("lastWateredDuration")?.value.toString()
    def newState = val ? val.toInteger() : 0
    if(isStateChange(device, "lastWateredDuration", newState.toString())) {
        log.debug("UPDATED: Last Watered Duration Value (${newState}) | Previous: (${curState})")
        sendEvent(name:'lastWateredDuration', value: newState, displayed: true)
    }
}

def zoneTotalDurationEvent(val) {
    def curState = device?.currentState("zoneTotalDuration")?.value.toString()
    def newState = val ? val.toInteger() : 0
    if(isStateChange(device, "zoneTotalDuration", newState.toString())) {
        log.debug("UPDATED: Zone Total Duration Value (${newState}) | Previous: (${curState})")
        sendEvent(name:'zoneTotalDuration', value: newState, displayed: true)
    }
}

def saturatedDepthOfWaterEvent(val) {
    def curState = device?.currentState("saturatedDepthOfWater")?.value.toString()
    def newState = val ? val.toDouble() : 0.0
    if(isStateChange(device, "saturatedDepthOfWater", newState.toString())) {
        log.debug("UPDATED: Saturated Depth Of Water Value (${newState}) | Previous: (${curState})")
        sendEvent(name:'saturatedDepthOfWater', value: newState, displayed: true)
    }
}

def depthOfWaterEvent(val) {
    def curState = device?.currentState("depthOfWater")?.value.toString()
    def newState = val ? val.toDouble() : 0.0
    if(isStateChange(device, "depthOfWater", newState.toString())) {
        log.debug("UPDATED: Depth Of Water Value (${newState}) | Previous: (${curState})")
        sendEvent(name:'depthOfWater', value: newState, displayed: true)
    }
}

def efficiencyEvent(val) {
    def curState = device?.currentState("efficiency")?.value.toString()
    def newState = val ? val.toDouble() : 0.0
    if(isStateChange(device, "efficiency", newState.toString())) {
        log.debug("UPDATED: Efficiency Value (${newState}) | Previous: (${curState})")
        sendEvent(name:'efficiency', value: newState, displayed: false)
    }
}

def maxRuntimeEvent(val) {
    def curState = device?.currentState("maxRuntime")?.value.toString()
    def newState = val ? val.toInteger() : 0
    if(isStateChange(device, "maxRuntime", newState.toString())) {
        log.debug("UPDATED: Max Runtime Value (${newState}) | Previous: (${curState})")
        sendEvent(name:'maxRuntime', value: newState, displayed: false)
    }
}

def availableWaterEvent(val) {
    def curState = device?.currentState("availableWater")?.value.toString()
    def newState = val ? val.toDouble() : 0.0
    if(isStateChange(device, "availableWater", newState.toString())) {
        log.debug("UPDATED: Available Water Value (${newState}) | Previous: (${curState})")
        sendEvent(name:'availableWater', value: newState, displayed: true)
    }
}

def zoneSquareFeetEvent(val) {
    def curState = device?.currentState("zoneSquareFeet")?.value.toString()
    def newState = val ? val.toInteger() : 0
    if(isStateChange(device, "zoneSquareFeet", newState.toString())) {
        log.debug("UPDATED: Zone Area Square Feet (${newState}) | Previous: (${curState})")
        sendEvent(name:'zoneSquareFeet', value: newState, displayed: true)
    }
}

def rootZoneDepthEvent(val) {
    def curState = device?.currentState("rootZoneDepth")?.value.toString()
    def newState = val ? val.toInteger() : 0
    if(isStateChange(device, "rootZoneDepth", newState.toString())) {
        log.debug("UPDATED: Root Zone Depth Value (${newState}) | Previous: (${curState})")
        sendEvent(name:'rootZoneDepth', value: newState, displayed: false)
    }
}

def zoneNumEvent(val) {
    def curState = device?.currentState("zoneNumber")?.value.toString()
    def newState = val ? val.toInteger() : 0
    if(isStateChange(device, "zoneNumber", newState.toString())) {
        log.debug("UPDATED: Zone Number (${newState}) | Previous: (${curState})")
        sendEvent(name:'zoneNumber', value: newState, displayed: true)
    }
}

def zoneNameEvent(val) {
    def curState = device?.currentState("zoneName")?.value.toString()
    def newState = val ? val.toString() : "unknown"
    if(isStateChange(device, "zoneName", newState.toString())) {
        log.debug("UPDATED: Zone Name (${newState}) | Previous: (${curState})")
        sendEvent(name:'zoneName', value: newState, displayed: true)
    }
}

def setZoneWaterTime(timeVal) {
    def curState = device?.currentState("zoneWaterTime")?.value.toString()
    def newVal = timeVal ? timeVal.toInteger() : parent?.settings?.defaultZoneTime.toInteger()
    if(isStateChange(device, "zoneWaterTime", newVal.toString())) {
        log.debug("UPDATED: Manual Zone Water Time (${newVal}) | Previous: (${curState})")
        sendEvent(name: 'zoneWaterTime', value: newVal, displayed: true)
    }
}

def fmtString(str) {
    if(!str) { return null }
    def out = []
    def tmp = str?.replaceAll("_", " ")?.toLowerCase()?.split(" ")
    tmp?.each { out.push(it?.toString().capitalize()) }
    return out.join(" ")
}
def scheduleDataEvent(data) {
    //log.trace "scheduleDataEvent($data)..."
    state?.curSchedData = data
    def curSchedType = !data?.type ? "off" : data?.type?.toString().toLowerCase()
    state.curSchedType = curSchedType
    state?.curScheduleId = !data?.scheduleId ? null : data?.scheduleId
    state?.curScheduleRuleId = !data?.scheduleRuleId ? null : data?.scheduleRuleId

    def zoneId = !data?.zoneId ? null : data?.zoneId
    def zoneStartDate = (zoneId == device?.deviceNetworkId && data?.zoneStartDate) ? data?.zoneStartDate : null
    def zoneDuration = (zoneId == device?.deviceNetworkId && data?.zoneDuration) ? data?.zoneDuration : null

    def timeDiff = data?.zoneStartDate ? GetTimeValDiff(data?.zoneStartDate.toLong()) : 0
    def elapsedDuration = data?.zoneStartDate ? getDurationMinDesc(Math.round(timeDiff)) : 0
    def wateringDuration = zoneDuration ? getDurationMinDesc(zoneDuration) : 0
    def zoneRunStatus = ((!zoneStartDate && !zoneDuration) || (zoneId != device?.deviceNetworkId)) ? "Status: Idle" : "${curSchedType == "automatic" ? "Scheduled " : "Manual "}Watering: ${elapsedDuration} of ${wateringDuration} Minutes"
    
    def zoneCycleCount = (zoneId != device?.deviceNetworkId && !data?.totalCycleCount) ? 0 : data?.totalCycleCount
    def isCycling =  (zoneId == device?.deviceNetworkId && data?.cycling) ? true : false
    def wateringVal = device?.currentState("watering")?.value
    if(isStateChange(device, "scheduleType", curSchedType?.toString().toLowerCase())) {
        sendEvent(name: 'scheduleType', value: curSchedType?.toString().toLowerCase()?.capitalize(), displayed: true, isStateChange: true)
    }
    if(isStateChange(device, "zoneDuration", zoneDuration.toString())) {
        sendEvent(name: 'zoneDuration', value: zoneDuration.toString(), displayed: true, isStateChange: true)
    }
    def zoneElapsed = zoneDuration && timeDiff && timeDiff > 0 ? timeDiff : null
    if(isStateChange(device, "zoneElapsed", zoneElapsed.toString())) {
        sendEvent(name: 'zoneElapsed', value: zoneElapsed.toString(), displayed: false, isStateChange: true)
    }
    if(!state?.inStandby && wateringVal != "offline" && isStateChange(device, "zoneRunStatus", zoneRunStatus.toString())) {
        log.info("UPDATED: ZoneRunStatus (${zoneRunStatus})")
        sendEvent(name: 'zoneRunStatus', value: zoneRunStatus.toString(), displayed: true, isStateChange: true)
    }
    if(isStateChange(device, "zoneCycleCount", zoneCycleCount.toString())) {
        sendEvent(name: 'zoneCycleCount', value: zoneCycleCount, displayed: true, isStateChange: true)
    }
    if(isStateChange(device, "isCycling", isCycling?.toString().capitalize())) {
        sendEvent(name: 'isCycling', value: isCycling?.toString().capitalize(), displayed: true, isStateChange: true)
    }
    if(isStateChange(device, "zoneStartDate", (zoneStartDate ? epochToDt(zoneStartDate).toString() : "Not Active"))) {
        sendEvent(name: 'zoneStartDate', value: (zoneStartDate ? epochToDt(zoneStartDate).toString() : "Not Active"), displayed: true, isStateChange: true)
    }
}

def isWatering(data) {
    def zoneId = !data?.zoneId ? null : data?.zoneId
    def zoneStartDate = (zoneId == device?.deviceNetworkId && data?.zoneStartDate) ? data?.zoneStartDate : null
    def zoneDuration = (zoneId == device?.deviceNetworkId && data?.zoneDuration) ? data?.zoneDuration : null
    return zoneStartDate && zoneDuration;
}

def customNozzleDataEvent(data) {
    //log.trace "customNozzleDataEvent($data)"
    if(data && data?.name && isStateChange(device, "nozzleName", fmtString(data?.name).toString())) {
        sendEvent(name:'nozzleName', value: fmtString(data?.name).toString(), displayed: true, isStateChange: true)
    }
}

def customSoilDataEvent(data) {
    //log.trace "customSoilDataEvent($data)"
    if(data && data?.name && isStateChange(device, "soilName", fmtString(data?.name).toString())) {
        sendEvent(name:'soilName', value: fmtString(data?.name).toString(), displayed: true, isStateChange: true)
    }
}

def customSlopeDataEvent(data) {
    //log.trace "customSlopeDataEvent($data)"
    def slope = data?.name ? fmtString(data?.name).toString() : "N/A"
    if(slope && isStateChange(device, "slopeName", slope.toString())) {
        sendEvent(name:'slopeName', value: slope.toString(), displayed: true, isStateChange: true)
    }
}

def customCropDataEvent(data) {
    //log.trace "customCropDataEvent($data)"
    if(data && data?.name && isStateChange(device, "cropName", fmtString(data?.name).toString())) {
        sendEvent(name:'cropName', value: fmtString(data?.name).toString(), displayed: true, isStateChange: true)
    }
}

def customShadeDataEvent(data) {
    //log.trace "customShadeDataEvent($data)"
    if(data && data?.name && isStateChange(device, "shadeName", fmtString(data?.name).toString())) {
        sendEvent(name:'shadeName', value: fmtString(data?.name).toString(), displayed: true, isStateChange: true)
    }
}

def refresh() {
    //log.trace "refresh..."
    poll()
}

void poll() {
    log.info("Requested Parent Poll...");
    parent?.poll(this)
}

def incZoneWaterTime() {
    // log.debug("Decrease Zone Runtime");
    def value = device.latestValue('zoneWaterTime')
    setZoneWaterTime(value + 1)
}

def decZoneWaterTime() {
    // log.debug("Increase Zone Runtime");
    def value = device.latestValue('zoneWaterTime')
    setZoneWaterTime(value - 1)
}

def isCmdOk2Run() {
    //log.trace "isCmdOk2Run..."
    if(state?.isOnline == false) {
        log.warn "Skipping the request... Because the zone is unable to send commands while it's in an Offline State."
        return false
    }
    if(state?.pauseInStandby == true && state?.inStandby == true) {
        log.warn "Skipping the request... Because the zone is unable to send commands while the controller is in standby mode."
        return false
    } else { return true }
}

def startZone() {
    log.trace "startZone()..."
    if(!isCmdOk2Run()) { return }
    def zoneNum = device?.latestValue('zoneNumber').toInteger()
    def waterTime = device?.latestValue('zoneWaterTime')
    log.debug("Starting Watering for Zone (${zoneNum}) for (${waterTime}) Minutes")
    def res = parent?.startZone(this, state?.deviceId, zoneNum, waterTime)
    if (res) {
        log.debug "runThisZone was Sent Successfully: ${res}"
        sendEvent(name:'watering', value: "on", displayed: true, isStateChange: true)
        sendEvent(name:'switch', value: "on", displayed: false, isStateChange: true)
        sendEvent(name:'valve', value: "open", displayed: false, isStateChange: true)
    }
    else {
        markOffLine()
    }
}

def on() {
    log.trace "zone on..."
    if(!isCmdOk2Run()) { return }
    def isOn = device?.currentState("switch")?.value.toString() == "on" ? true : false
    if (!isOn) { open() }
    else { log.info "Zone is Already ON... Ignoring..." }
}

def off() {
    log.trace "zone off..."
    //if(!isCmdOk2Run()) { return }
    def isOff = device?.currentState("switch")?.value.toString() == "off" ? true : false
    if (!isOff) { close() }
    else { log.info "Zone is Already OFF... Ignoring..." }
}

def open() {
    log.trace "Zone open()..."
    if(!isCmdOk2Run()) { return }
    def isOpen = device?.currentState("valve")?.value.toString() == "open" ? true : false
    if (!isOpen) {
        startZone()
    }
    else { log.info "Valve is Already Open... Ignoring..." }
}

def close() {
    log.trace "Zone close()..."
    //if(!isCmdOk2Run()) { return }
    def isClosed = device?.currentState("valve")?.value.toString() == "closed" ? true : false
    if (!isClosed) {
        def res = parent?.off(this, state?.deviceId)
        if (res) {
            log.info "Zone was Stopped Successfully..."
            sendEvent(name:'watering', value: "off", displayed: true, isStateChange: true)
            sendEvent(name:'switch', value: "off", displayed: false, isStateChange: true)
            sendEvent(name:'valve', value: "closed", displayed: false, isStateChange: true)
        }
    } else { log.info "Valve is Already Closed... Ignoring..." }
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
    if(val) { return formatDt(new Date(val)) }
}

def formatDt(dt, mdy = true) {
	def formatVal = mdy ? "MMM d, yyyy - h:mm:ss a" : "E MMM dd HH:mm:ss z yyyy"
	def tf = new SimpleDateFormat(formatVal)
	if(location?.timeZone) { tf.setTimeZone(location?.timeZone) }
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
	} else { return null }
}
