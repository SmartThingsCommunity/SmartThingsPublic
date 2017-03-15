/**
 *  Slickspaces
 *
 *  Copyright 2016 Mathew Hunter
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *  v0.19 -- split myPlug and mySwitch to two separate classes
 *	v0.20 -- added status to the getDevices to simplify the UI for www.slickspaces.com
 *  v0.21 -- added motion sensor and CO to sensor preference section
 *  v0.22 -- added routines/current mode
 *  v0.23 -- added child smartapp - Slickspaces Lock Manager
 */

definition(
    name: "Slickspaces v0.25",
    namespace: "Slickspaces",
    author: "Mathew Hunter",
    description: "Slickspaces Rental Management",
    category: "Convenience",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    oauth: true)

preferences {
    section("Door Locks") {
        input "myLock", "capability.lock", title: "Smart Lock", required: false, multiple: true
        
        // TODO: put inputs here
    }
    section("Ecobee Thermostats") {
        input "myThermostat", "device.ecobeeThermostat", title: "Smart Thermostat", required: false, multiple: true
    }
    section("Smart Sensors") {
        input "mySensor", "capability.contactSensor", title: "Door/Window Sensors", required: false, multiple: true
        input "myMotion", "capability.motionSensor", title: "Motion Sensors", required: false, multiple: true
        input "myBattery", "capability.battery", title: "Battery Operated Devices", required: false, multiple: true
        input "myCO2", "capability.carbonDioxideMeasurement", title: "CO2 Sensors", required: false, multiple: true
        input "mySmoke", "capability.smoke", title: "Smoke Detectors", required: false, multiple: true
        input "myTemperature", "capability.temperatureMeasurement", title: "Temperature Sensors", required: false, multiple: true
 	}
    section("Remotes") {
        input "myButton", "capability.button", title: "Remote Control", required: false, multiple: true
 	}
     section("Light Dimmers"){
        input "myDimmer", "capability.switchLevel", title: "Smart Dimmers", required: false, multiple: true
    }
    section("Switches (Lights)"){
        input "mySwitch", "capability.switch", title: "Smart Lights (no dimmer)", required: false, multiple: true
    }
    section("Switches (Electrical Plugs)"){
    	input "myPlug", "capability.switch", title: "Smart Plugs", required: false, multiple: true
    }
    section("Music Player") {
        input "myMusic", "capability.musicPlayer", title: "Music Player", required: false, multiple: true
    }
    section("Addons") {
    	app(name: "LockManager", appName: "Slickspaces Lock Manager", namespace: "Slickspaces", title: "Slickspaces Lock Manager", multiple: true)
    }
   
}

mappings {
	//working on it
    path("/refresh-all"){
        action:[
            GET:"getAllDeviceStatus"
        ]
    }
    //TODO
    path("/refresh/:deviceType/:id"){
        action:[
                GET:"getDeviceStatus"
        ]
    }
    //done
    path("/getdevices"){
        action:[
            GET:"getDevices"
        ]
    }
    path("/getlocks"){
    	action:[
        	GET:"getLocks"
        ]
    }
    //done
    path("/getroutines"){
        action:[
            GET:"getRoutines"
        ]
    }
    path("/executeroutine/:routine"){
    	action:[
        	GET:"executeRoutine"
        ]
    }
    //lock manager mappings
	path("/getlockusers/:id"){
    	action:[
        	GET:"getLockUsers"
        ]
    }
    path("/setlockcode/:id/:code/:date"){
    	action:[
        	POST:"setLockCode"
        ]
    }
    path("/deletelockcode/:id/:codeID"){
    	action:[
        	POST:"deleteLockCode"
        ]
    }
    path("/deletealllockcodes/:id"){
    	action:[
        	POST:"deleteAllLockCodes"
        ]
    }
    path("/poll/:id"){
    	action:[
       		GET:"pollLocks"
       	]
    //end of lock manager maps
    path("/verifytoken"){
    	action:[
        	GET:"verifyToken"
        ]
    }
    path("/gettimezone"){
    	action:[
       		GET:"getTimezone"
       	]
    }
	path("/:deviceType/:id/:action"){
        action:[
            POST:"startAction"
        ]
    }
    path("/unschedule"){
    	action:[
    		POST:"startUnschedule"    
        ]
    }
    path("/timestamp/:timestamp"){
    	action:[
        	GET: "timestamp"
        ]
    }
    
    
    
}

}

def timestamp(){
	def timestamp = params.timestamp
    def resp = []
    def myResponse = [st_timestampwithoffset:dateTimezoneOffset(timestamp),timestampraw:timestamp, date:new Date()]
    resp << myResponse
    return resp
}

def startUnschedule(){
	unschedule()
}

def getLocks(){
	def deviceAttributes
	def resp = []
	if (myLock){
    	myLock.each {
        	deviceAttributes = deviceStatus("lock",it.id)
            def myResponse = [device_id:it.id,status:deviceAttributes]
            resp << myResponse
        }
    }
    return resp
}

def verifyToken(){
	return [verifyToken:"Success"]
}

def deleteLockCodeAt(deleteDate, lockID, codeID){
	log.debug "start of deleteLockCodeAt with vals, applied offset date: ${deleteDate}, ${lockID}, ${codeID}"
	def lockMgr = findChildAppByName("Slickspaces Lock Manager")
	def endDate = new Date(deleteDate)
    log.debug "Setting schedule for code delete on created groovy date ${endDate}, for ${lockID}, ${codeID}"
	runOnce(endDate, deleteLockCodeNow, [overwrite: false, data:[lockID:lockID, codeID:codeID]])
}

def setLockCodeAt(setDate, lockID, codeID, code){
	log.debug "start of setLockCodeAt with vals, applied offset date: ${deleteDate}, ${lockID}, ${codeID}"
	def lockMgr = findChildAppByName("Slickspaces Lock Manager")
	def startDate = new Date(setDate)
    log.debug "Setting schedule for code set on created groovy date ${startDate}, for ${lockID}, ${codeID}, ${code}"
    runOnce(startDate, setLockCodeNow, [overwrite: false,data:[lockID:lockID,codeID:codeID,code:code]])
}

def dateTimezoneOffset(longDate){
	longDate = longDate.toLong() - location.timeZone.rawOffset
    return longDate
}

def getLockUsers(){
	def lockMgr = findChildAppByName("Slickspaces Lock Manager")
	def lockID = params.id
    if (lockMgr){
    	return lockMgr.getLockUsers(lockID)
    }
    return [error:"lock manager not installed"]
}

def pollLocks() {
	log.debug "arrived at pollLocks"
	def id = params.id
	def lockMgr = findChildAppByName("Slickspaces Lock Manager")
  	log.debug "id is ${id} and lock manager app is ${lockMgr}"
    def result = lockMgr.pollLocks(id)
    log.info "result was ${result}"
    return result
}

def setLockCodeNow(data){
	log.info "made it to setLockCodeNow!"
    log.info "is there a map? ${data["lockID"]}"
	def lockMgr = findChildAppByName("Slickspaces Lock Manager")
    if (lockMgr){
        lockMgr.setLockCode(data["lockID"],data["codeID"],data["code"])
	}
}
def deleteLockCodeNow(data){
	log.info "made it to deleteLockCodeNow!"
    log.info "is there a delete map? ${data["lockID"]}"
	def lockMgr = findChildAppByName("Slickspaces Lock Manager")
    if (lockMgr){
        lockMgr.deleteLockCode(data["lockID"],data["codeID"])
	}
}

def setLockCode(){
	log.info "made it to setLockCode"
	def lockMgr = findChildAppByName("Slickspaces Lock Manager")
    def arrDateRange = params.date.split('-')
	def lockID = params.id
    def paramCode = params.code
    def arrValues = paramCode.split('-')
    def codeID = arrValues[0].toInteger()
    def code = arrValues[1]
    def date = params.date
    def response = [:]
    
    log.debug "${lockID} ${codeID} ${code} ${date} original dates submitted: ${arrDateRange[0]} ${arrDateRange[1]}"
    
    if (arrDateRange[0].toLong() > 0){
    	log.debug "start date for set lock ${dateTimezoneOffset(arrDateRange[0])}"
        def startDate = setLockCodeAt(dateTimezoneOffset(arrDateRange[0]), lockID, codeID, code)
        response << [startdate: startDate]
    }    
    if (arrDateRange[1].toLong() > 0) {
    	log.debug "end date for set lock ${arrDateRange[1]}"
        def endDate = deleteLockCodeAt(dateTimezoneOffset(arrDateRange[1]), lockID, codeID)
        response << [enddate: endDate]
    }
    
    if(arrDateRange[0].toLong() == 0) {
    	log.debug "setting lock code immediately"
    	response << lockMgr.setLockCode(lockID, codeID, code)
    }
    
    return response
}

def deleteLockCode(){
	log.debug "made it to deleteLockCode"
	def lockMgr = findChildAppByName("Slickspaces Lock Manager")
    log.debug lockMgr
	def lockID = params.id
    def codeID = params.codeID.toInteger()
    
    if (lockMgr){
    	return lockMgr.deleteLockCode(lockID, codeID)
    }
    
   	return [error:"lock manager not installed"]
}

def deleteAllLockCodes(){
	def lockID = params.id
	log.debug "deleting all lock codes for ${lockID}"
    def lockMgr = findChildAppByName("Slickspaces Lock Manager")
    if (lockMgr){
    	return lockMgr.deleteAllLockCodes(lockID)
    }
    return [error:"Lock not found"]
}

//gets the status of all devices, returns to the web requestor the attributes of all devices as JSON (used as part of the web api)
def getAllDeviceStatus(){
	//mySwitch, myDimmer, myThermostat, mySensor, myMusic, myLock
    def deviceAttributes = []
    def resp = []

    if (myThermostat){
    	myThermostat.each {
        	deviceAttributes = deviceStatus("thermostat",it.id)
            def myResponse = [device_id:it.id,status:deviceAttributes]
            resp << myResponse
        }
    }
    if (mySwitch){
    	mySwitch.each {
        	deviceAttributes = deviceStatus("switch",it.id)
            def myResponse = [device_id:it.id,status:deviceAttributes]
            resp << myResponse
        }
    }
    if (myPlug){
    	myPlug.each {
        	deviceAttributes = deviceStatus("switch",it.id)
            def myResponse = [device_id:it.id,status:deviceAttributes]
            resp << myResponse
        }
    }
    if (myDimmer){
    	myDimmer.each {
        	deviceAttributes = deviceStatus("dimmer",it.id)
            def myResponse = [device_id:it.id,status:deviceAttributes]
            resp << myResponse
        }
    }
    if (mySensor){
    	mySensor.each {
        	deviceAttributes = deviceStatus("sensor",it.id)
            def myResponse = [device_id:it.id,status:deviceAttributes]
            resp << myResponse
        }
    }
    if (myLock){
    	myLock.each {
        	deviceAttributes = deviceStatus("lock",it.id)
            def myResponse = [device_id:it.id,status:deviceAttributes]
            resp << myResponse
        }
    }
    return resp
}

//gets the attributes of a particular device by type and device ID (used as part of the web api after sending an action to verify completion)
def getDeviceStatus() {
	def deviceType = params.deviceType
    def deviceID = params.id
    def deviceAttributes = []
    def resp = []
    log.debug "$deviceID"
    deviceAttributes = deviceStatus(deviceType,deviceID)
    resp << [completed: deviceAttributes]
    return resp
}

// takes a deviceType and array of device ID's and returns their attributes as result
def deviceStatus(deviceType, deviceID){
	log.debug "$deviceID"
	def resp
    def myDevice
    switch (deviceType){
    	case "thermostat":
        	myDevice = myThermostat.find { it.id == deviceID}
            resp = [currentTemperature: myDevice.currentTemperature,
                    coolingSetpoint: myDevice.currentCoolingSetpoint,
                    heatingSetpoint: myDevice.currentHeatingSetpoint,
                    deviceTemperatureUnit: myDevice.currentDeviceTemperatureUnit,
                    thermostatSetpoint: myDevice.currentThermostatSetPoint,
                    thermostatMode: myDevice.currentThermostatMode,
                    thermostatFanMode: myDevice.currentThermostatFanMode,
                    thermostatStatus: myDevice.currentThermostatStatus,
                    humidity: myDevice.currentHumidity]
        	break
        case "sensor":
        	myDevice = mySensor.find { it.id == deviceID}
            resp = [contact: myDevice.currentValue("contact")]
        	break
        case "switch":
        	myDevice = mySwitch.find { it.id == deviceID}
            resp = [switch: myDevice.currentValue("switch")]
        	break
        case "plug":
        	myDevice = myPlug.find { it.id == deviceID}
            resp = [plug: myDevice.currentValue("switch")]
        	break
        case "lock":
        	myDevice = myLock.find { it.id == deviceID}
            resp = [lock: myDevice.currentLock]
        	break
        case "music":
        	myDevice = myMusic.find { it.id == deviceID}
            resp = [status: myDevice.currentStatus,
            		level: myDevice.currentLevel,
                    trackDescription: myDevice.currentTrackDescription,
                    trackData: myDevice.currentTrackData,
                    mute: myDevice.currentMute]
        	break
        case "dimmer":
        	myDevice = myDimmer.find { it.id == deviceID}
            resp = [switch: myDevice.currentSwitch,
            		level: myDevice.currentLevel]
			break
        default:
        	log.debug "no devices found of type $deviceType"
            resp = [error: "no device type found of type $deviceType"]
    }
    return resp
}

def startAction() {
    def action = params.action
    def deviceID = params.id
    def deviceType = params.deviceType
    def result

    switch (deviceType){
        case "lock":
            def targetDevice = myLock.find{ it.id == deviceID }
            if (targetDevice){
                result = invokeLockAction(targetDevice, action)
            }
            break
        case "switch":
            def targetDevice = mySwitch.find{ it.id == deviceID}
            result = invokeSwitchAction(targetDevice, action)
            break
        case "plug":
        	def targetDevice = myPlug.find{ it.id == deviceID}
            result = invokeSwitchAction(targetDevice, action)
            break
        case "dimmer":
            def targetDevice = myDimmer.find{ it.id == deviceID}
            if (action.isInteger()){
            	result = invokeDimmerAction(targetDevice, action.toInteger())
            }else{
            	result = invokeDimmerAction(targetDevice, action)
            }
			break
        case "button":
            def targetDevice = myButton.find{ it.id == deviceID}
            result = invokeButtonAction(targetDevice, action)
            break
        case "music":
            def targetDevice = myMusic.find{ it.id == deviceID}
            result = invokeMusicAction(targetDevice, action)
            break
            //working on stat
        case "thermostat":
            def targetDevice = myThermostat.find{ it.id == deviceID}
            result = invokeThermostatAction(targetDevice, action)
            break
        case "sensor":
            result = [Code:"1", Message: "No Actions available for Sensors"]
            break
        default:
            log.debug "No device found for $deviceType"
            result = [Code:"1", Message:"No Device Found for $deviceType"]
    }
    return [completed: result]

}

def invokeSwitchAction(device, action){
    def result
    switch (action){
        case "toggleSwitch":
            result = toggleSwitch(device)
            break
        default:
            result = "no action, $action"
            log.debug "no action"
    }
    return result
}

def invokeThermostatAction(device, action){
	def result
    switch (action){
    	case "resume":
        	device.resumeProgram()
            result = action
            break
        case "auto":
            device.auto()
            result = action
            break
        case "heat":
            device.heat()
            result = action
            break
        case "cool":
            device.cool()
            result = action
            break
        case "off":
            device.off()
            result = action
            break
        case "emergency heat":
            device.emergencyHeat()
            result = action
            break
        case "fanauto":
            device.fanAuto()
            result = action
            break
        case "fanon":
            device.fanOn()
            result = action
            break
        case "fancirculate":
            device.fanCirculate()
            result = action
            break
        case ~/heat-(.*)/:
            def values = action.tokenize('-')
            def temp = values[1]
            result = "set heat point failed"
            if (temp.isDouble()){
            	temp = temp.toDouble()/10
                device.setHeatingSetpoint(temp)
                result = temp
            }
            break
        case ~/cool-(.*)/:
            def values = action.tokenize('-')
            def temp = values[1]
            result = "set cool point failed"
            if (temp.isDouble()){
            	temp = temp.toDouble()/10
                device.setCoolingSetpoint(temp)
                result = temp
            }
            break
        default:
            log.debug "no action"
    }
}

def invokeButtonAction(device, action){
    switch (action){
        case "s1":
            break
        case "s2":
            break
        case "s3":
            break
        case "s4":
            break
        case "l1":
            break
        case "l2":
            break
        case "l3":
            break
        case "l4":
               break
        default:
            log.debug "no action"
    }
}

def invokeMusicAction(device, action){
    def result = action
    switch (action){
        case "play":
            device.play()
            result = action
            break
        case "stop":
            device.stop()
            break
        case "pause":
            device.pause()
            result = action
            break
        case "next":
            device.nextTrack()
            result = action
            break
        case "previous":
            device.previousTrack()
            result = action
            break
        case 0..100:
            device.setLevel(action)
            result = action
            break
        case "mute":
            device.mute()
            result = action
            break
        case "unmute":
            device.unmute()
            result = action
            break
        default:
            log.debug "no action"
            result = "no action found, $action"
    }
    return result
}

def invokeDimmerAction(device, action){
    def result
    log.info "performing ${action}"
        switch (action){
            case "toggleSwitch":
                result = toggleSwitch(device)
                break
            case 0..100:
                device.setLevel(action)
                result = action
                break
            default:
                result = "No Action Found Matching $action"
                log.debug "no action"
        }
    return result
}

def invokeLockAction(device, action){
    def result
    switch (action){
        case "lock":
            device.lock()
            result = "locked"
            break
        case "unlock":
            device.unlock()
            result = "unlocked"
            break
        default:
            result = "no action found, $action"
            log.debug "no action"
    }
    return result
}

def toggleSwitch(device){
	def result
    if (device.currentSwitch == "on") {
        device.off()
        result = "off"
    }else{
        device.on()
        result = "on"
    }
    return result
}


def getDevices(){
    def resp = []
    if (myButton){
        myButton.each {
            resp << [device: it, class:'button']
        }
    }
    if (myThermostat){
    	myThermostat.each {
        	resp << [device: it, 
            class:'thermostat', 
            temp:it.currentTemperature, 
            mode: it.currentThermostatMode, 
            fan: it.currentThermostatFanMode, 
            tempUnit: it.currentDeviceTemperatureUnit, 
            humidity: it.currentHumidity, 
            heatSetpoint: it.currentHeatingSetpoint,
            coolSetpoint: it.currentCoolingSetpoint,
            minCoolSetpoint: it.currentMinCoolingSetpoint,
            maxCoolSetpoint: it.currentMaxCoolingSetpoint,
            minHeatSetpoint: it.currentMinHeatingSetpoint,
            maxHeatSetpoint: it.currentMaxHeatingSetpoint
            ]
        }
    }
    if (mySwitch){
    	mySwitch.each {
        	resp << [device: it, class:'switch', status:it.currentSwitch]
        }
    }
    if (myPlug){
    	myPlug.each {
        	resp << [device: it, class:'plug', status:it.currentSwitch]
        }
    }
    if (myDimmer){
    	myDimmer.each {
        	resp << [device: it, class:'dimmer', status:it.currentSwitch, level:it.currentLevel]
        }
    }
    if (mySensor){
    	mySensor.each {
        	resp << [device: it, class:'sensor', contact: it.currentContact]
        }
    }
    if (myMotion){
    	myMotion.each{
        	resp << [device: it, class:'sensor', motion: it.currentMotion]
        }
    }
    if (myTemperature){
    	myTemperature.each{ 
        	resp << [device: it, class:'sensor', temp: it.currentTemperature] //, attributes:attrs]
        }
    }
    if (myCO2) {
    	myCO2.each{
        	resp << [device: it, class:'sensor', co2: it.currentCarbonDioxide]
        }
    }
    if (myBattery){
    	myBattery.each{
        	resp << [device: it, class:'sensor', battery: it.currentBattery]
        }
    }
    if (mySmoke){
    	mySmoke.each{ smoke->
        	resp << [device: smoke, class:'sensor', smoke: smoke.currentSmoke]
        }
    }
    if (myLock){
        myLock.each {
        	resp << [device: it, class:'lock', status: it.currentLock]
        }
    }
    return resp
}

def getRoutines(){
	def resp = []
	def routines = location.helloHome?.getPhrases()*.label
    def currentMode = location.getCurrentMode().name
    resp << [currentMode:currentMode, routines:routines]
    log.debug("routines available ${routines}")
    log.debug("current mode is ${currentMode}")
    return resp
}

def executeRoutine(){
	def routine = params.routine
	location.helloHome?.execute(routine)
    return [completed: routine]
}

def installed() {
    log.debug "Installed with settings: ${settings}"
    initialize()
}

def updated() {
    log.debug "Updated with settings: ${settings}"
    unsubscribe()
    initialize()
}

def initialize() {
	
}
