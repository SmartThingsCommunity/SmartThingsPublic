/**
 *  Sensibo (Connect)
 *
 *  Copyright 2015 Eric Gosselin
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

definition(
    name: "Sensibo (Connect)",
    namespace: "EricG66",
    author: "Eric Gosselin",
    description: "Connect your Sensibo Pod to SmartThings.",
    category: "Green Living",
    iconUrl: "https://image.ibb.co/f8gMFQ/on_color_large_sm.png",
    iconX2Url: "https://image.ibb.co/eOwA9k/on_color_large2x.png",
    iconX3Url: "https://image.ibb.co/cq9V9k/on_color_large3x.png",
    singleInstance: true) 

{
    appSetting "apikey"
}

preferences {
	page(name: "SelectAPIKey", title: "API Key", content: "setAPIKey", nextPage: "deviceList", install: false, uninstall: true)
	page(name: "deviceList", title: "Sensibo", content:"SensiboPodList", install:true, uninstall: true)
    page(name: "timePage")
    page(name: "timePageEvent")
}

def getServerUrl() { "https://home.sensibo.com" }
def getapikey() { apiKey }

def setAPIKey()
{
	log.debug "setAPIKey()"
    
    def pod = appSettings.apikey
    
    def p = dynamicPage(name: "SelectAPIKey", title: "Enter your API Key", uninstall: true) {
		section(""){
			paragraph "Please enter your API Key provided by Sensibo \n\nAvailable at: \nhttps://home.sensibo.com/me/api"
			input(name: "apiKey", title:"", type: "text", required:true, multiple:false, description: "", defaultValue: pod)
		}
	}
    return p
}

def SensiboPodList()
{
	log.debug "SensiboPodList()"

	def stats = getSensiboPodList()
	log.debug "device list: $stats"
    
	def p = dynamicPage(name: "deviceList", title: "Select Your Sensibo Pod", uninstall: true) {
		section(""){
			paragraph "Tap below to see the list of Sensibo Pods available in your Sensibo account and select the ones you want to connect to SmartThings."
			input(name: "SelectedSensiboPods", title:"Pods", type: "enum", required:true, multiple:true, description: "Tap to choose",  metadata:[values:stats])
		}

        section("Receive Pod sensors infos") {
        	input "boolnotifevery", "bool",submitOnChange: true, required: false, title: "Receive temperature, humidity and battery level notification every hour?"
            href(name: "toTimePageEvent",
                     page: "timePageEvent", title:"Only during a certain time", require: false)
        }

        section("Alert on sensors (threshold)") {
        	input "sendPushNotif", "bool",submitOnChange: true, required: false, title: "Receive alert on Sensibo Pod sensors based on threshold?"                       
        }

		if (sendPushNotif) {
           section("Select the temperature threshold",hideable: true) {
            	input "minTemperature", "decimal", title: "Min Temperature",required:false
            	input "maxTemperature", "decimal", title: "Max Temperature",required:false }
            section("Select the humidity threshold",hideable: true) {
            	input "minHumidity", "decimal", title: "Min Humidity level",required:false
            	input "maxHumidity", "decimal", title: "Max Humidity level",required:false }              
         
        	section("How frequently?") {
        		input(name:"days", title: "Only on certain days of the week", type: "enum", required:false, multiple: true, options: ["Monday", "Tuesday", "Wednesday","Thursday","Friday","Saturday","Sunday"])
        	}
        	section("") {
        		href(name: "toTimePage",
                	 page: "timePage", title:"Only during a certain time", require: false)
        	}
        }
        
	}
	return p
}

// page def must include a parameter for the params map!
def timePage() {
    dynamicPage(name: "timePage", uninstall: false, install: false, title: "Only during a certain time") {
      section("") {
        input(name: "startTime", title: "Starting at : ", required:false, multiple: false, type:"time",)
        input(name: "endTime", title: "Ending at : ", required:false, multiple: false, type:"time")
      }
   }
}

// page def must include a parameter for the params map!
def timePageEvent() {
    dynamicPage(name: "timePageEvent", uninstall: false, install: false, title: "Only during a certain time") {
      section("") {
        input(name: "startTimeEvent", title: "Starting at : ", required:false, multiple: false, type:"time",)
        input(name: "endTimeEvent", title: "Ending at : ", required:false, multiple: false, type:"time")
      }
   }
}

def getSensiboPodList()
{
	log.debug "getting device list"
       
    def deviceListParams = [
    uri: "${getServerUrl()}",
    path: "/api/v2/users/me/pods",
    requestContentType: "application/json",
    query: [apiKey:"${getapikey()}", type:"json",fields:"id,room" ]]

	def pods = [:]
	
    try {
      httpGet(deviceListParams) { resp ->
    	if(resp.status == 200)
			{
				resp.data.result.each { pod ->
                    def key = pod.id
                    def value = pod.room.name
                        
					pods[key] = value
				}
			}
	  }
    }
    catch(Exception e)
	{
		log.debug "Exception Get Json: " + e
		debugEvent ("Exception get JSON: " + e)
	}
    
    log.debug "Sensibo Pods: $pods"  
	
    return pods
}

def installed() {
	log.debug "Installed with settings: ${settings}"

	state.lastTemperaturePush = null
    state.lastHumidityPush = null
  
	initialize()
    
    def d = getAllChildDevices()

	if (boolnotifevery) {
    	//runEvery1Hour("hournotification")
        schedule("0 0 * * * ?", "hournotification")
	}
    
    //subscribe(d,"temperatureUnit",eTempUnitHandler)
    
    if (sendPushNotif) { 
    	subscribe(d, "temperature", eTemperatureHandler)
        subscribe(d, "humidity", eHumidityHandler)
    }
}

def updated() {
	log.debug "Updated with settings: ${settings}"

	unschedule()
    unsubscribe()
	
    state.lastTemperaturePush = null
    state.lastHumidityPush = null
    
    initialize()
    
    def d = getAllChildDevices()
    
    if (boolnotifevery) {
    	//runEvery1Hour("hournotification")
        schedule("0 0 * * * ?", "hournotification")
	}
    
    //subscribe(d,"temperatureUnit",eTempUnitHandler)
    
    if (sendPushNotif) {
    	subscribe(d, "temperature", eTemperatureHandler)
        subscribe(d, "humidity", eHumidityHandler)
    }
}

def hournotification() {
	def hour = new Date()
	def curHour = hour.format("HH:mm",location.timeZone)
	def curDay = hour.format("EEEE",location.timeZone)
	// Check the time Threshold
    def stext = ""
	if (startTimeEvent && endTimeEvent) {
 		def minHour = new Date().parse(smartThingsDateFormat(), startTimeEvent)
    	def endHour = new Date().parse(smartThingsDateFormat(), endTimeEvent)

    	def minHourstr = minHour.format("HH:mm",location.timeZone)
    	def maxHourstr = endHour.format("HH:mm",location.timeZone)

    	if (curHour >= minHourstr && curHour <= maxHourstr) 
    	{
    		def devices = getAllChildDevices()
            devices.each { d ->
                log.debug "Notification every hour for device: ${d.id}"
                def currentPod = d.displayName
                def currentTemperature = d.currentState("temperature").value
                def currentHumidity = d.currentState("humidity").value
                def currentBattery = d.currentState("voltage").value
                def sunit = d.currentState("temperatureUnit").value
                stext = "${currentPod} - Temperature: ${currentTemperature} ${sunit} Humidity: ${currentHumidity}% Battery: ${currentBattery}"    
                
                sendPush(stext)
            }
    	}
    }
    else {
    	 	def devices = getAllChildDevices()
            devices.each { d ->
                log.debug "Notification every hour for device: ${d.id}"
                def currentPod = d.displayName
                def currentTemperature = d.currentState("temperature").value
                def currentHumidity = d.currentState("humidity").value
                def currentBattery = d.currentState("voltage").value
                def sunit = d.currentState("temperatureUnit").value
                stext = "${currentPod} - Temperature: ${currentTemperature} ${sunit} Humidity: ${currentHumidity}% Battery: ${currentBattery}"    
                
                sendPush(stext)
            }
    }
}

//def switchesHandler(evt)
//{
//  if (evt.value == "on") {
//        log.debug "switch turned on!"
//    } else if (evt.value == "off") {
//        log.debug "switch turned off!"
//    }
//}

def eTempUnitHandler(evt)
{
	//refreshOneDevice(evt.device.displayName)
}

def eTemperatureHandler(evt){
	def currentTemperature = evt.device.currentState("temperature").value
    def currentPod = evt.device.displayName
    def hour = new Date()
    
    if (inDateThreshold(evt,"temperature") == true) {
        if(maxTemperature != null){
            if(currentTemperature.toDouble() > maxTemperature)
            {
            	def stext = "Temperature level is too high at ${currentPod} : ${currentTemperature}"
				sendEvent(name: "lastTemperaturePush", value: "${stext}",  displayed : "true", descriptionText:"${stext}")
                sendPush(stext)

                state.lastTemperaturePush = hour
            }
        }
        if(minTemperature != null) {
            if(currentTemperature.toDouble() < minTemperature)
            {	
            	def stext = "Temperature level is too low at ${currentPod} : ${currentTemperature}"
                sendEvent(name: "lastTemperaturePush", value: "${stext}",  displayed : "true", descriptionText:"${stext}")
                sendPush(stext)

                state.lastTemperaturePush = hour
            }
        }
    } 
}

def eHumidityHandler(evt){
	def currentHumidity = evt.device.currentState("humidity").value
    def currentPod = evt.device.displayName
    def hour = new Date()
    if (inDateThreshold(evt,"humidity") == true) { 
        if(maxHumidity != null){
            if(currentHumidity.toDouble() > maxHumidity)
            {   
            	def stext = "Humidity level is too high at ${currentPod} : ${currentHumidity}"
                sendEvent(name: "lastHumidityPush", value: "${stext}", displayed : "true", descriptionText:"${stext}")
                sendPush(stext)
                
                state.lastHumidityPush = hour
            }
        }
        if(minHumidity != null) {
            if(currentHumidity.toDouble() < minHumidity)
            {
            	def stext = "Humidity level is too low at ${currentPod} : ${currentHumidity}"
                sendEvent(name: "lastHumidityPush", value: "${stext}", displayed : "true", descriptionText:"${stext}")
                sendPush(stext)
                
                state.lastHumidityPush = hour
            }
        }
    }
}

public smartThingsDateFormat() { "yyyy-MM-dd'T'HH:mm:ss.SSSZ" }
public smartThingsDateFormatNoMilli() { "yyyy-MM-dd'T'HH:mm:ssZ" }

def canPushNotification(currentPod, hour,sType) {
    // Check if the client already received a push
    if (sType == "temperature") {
        if (sfrequency.toString().isInteger()) {
            if (state.lastTemperaturePush != null) {
                long unxNow = hour.time

                def before = new Date().parse(smartThingsDateFormatNoMilli(),state.lastTemperaturePush)
                long unxEnd = before.time
                
                unxNow = unxNow/1000
                unxEnd = unxEnd/1000
                def timeDiff = Math.abs(unxNow-unxEnd)
                timeDiff = timeDiff/60
                if (timeDiff <= sfrequency)
                {
                    return false
                }
            }
    	}
    }
    else {
        if (sfrequency.toString().isInteger()) {
            if (state.lastHumidityPush != null) {
                long unxNow = hour.time
                
                def before = new Date().parse(smartThingsDateFormatNoMilli(),state.lastHumidityPush)
                long unxEnd = before.time

                unxNow = unxNow/1000
                unxEnd = unxEnd/1000
                def timeDiff = Math.abs(unxNow-unxEnd)
                timeDiff = timeDiff/60

                if (timeDiff <= sfrequency)
                {
                    return false
                }
            }
    	}
   	}

    return true
}

def inDateThreshold(evt,sType) {
	def hour = new Date()
	def curHour = hour.format("HH:mm",location.timeZone)
	def curDay = hour.format("EEEE",location.timeZone)
    def currentPod = evt.device.displayName
     
    // Check if the client already received a push
    
    def result = canPushNotification(currentPod,hour, sType)
    if (!result) { 
        return false 
    }
   
    // Check the day of the week
    if (days != null && !days.contains(curDay)) {
    	return false
    }
    
	// Check the time Threshold
	if (startTime && endTime) {
 		def minHour = new Date().parse(smartThingsDateFormat(), startTime)
    	def endHour = new Date().parse(smartThingsDateFormat(), endTime)

    	def minHourstr = minHour.format("HH:mm",location.timeZone)
    	def maxHourstr = endHour.format("HH:mm",location.timeZone)

    	if (curHour >= minHourstr && curHour < maxHourstr) 
    	{
    		return true
    	}
    	else
    	{ 
	    	return false
	    }
    }
    return true
}

def refresh() {
	log.debug "refresh() called"

    unschedule()
    
	refreshDevices()
    runEvery15Minutes("refreshDevices")
}


def refreshOneDevice(dni) {
	log.debug "refreshOneDevice() called"
	def d = getChildDevice(dni)
	d.refresh()
}

def refreshDevices() {
	log.debug "refreshDevices() called"
	def devices = getAllChildDevices()
	devices.each { d ->
		log.debug "Calling refresh() on device: ${d.id}"
        
		d.refresh()
	}
}

def getChildNamespace() { "EricG66" }
def getChildTypeName() { "SensiboPod" }

def initialize() {
    log.debug "key "+ getapikey()
    
    atomicState.apikey = getapikey()
	
    log.debug "initialize"

	def devices = SelectedSensiboPods.collect { dni ->
		log.debug dni
		def d = getChildDevice(dni)

		if(!d)
			{
                
            	def name = getSensiboPodList().find( {key,value -> key == dni })
				log.debug "Pod : ${name.value} - Hub : ${location.hubs[0].name} - Type : " +  getChildTypeName() + " - Namespace : " + getChildNamespace()

				d = addChildDevice(getChildNamespace(), getChildTypeName(), dni, location.hubs[0].id, [
                	"label" : "Pod ${name.value}",
                    "name" : "Pod ${name.value}"
                    ])
                d.setIcon("on","on","https://image.ibb.co/bz9K25/on_color_large_on.png")
                d.setIcon("off","on","https://image.ibb.co/k5S6h5/on_color_large.png")
                d.save()              
                
				log.debug "created ${d.displayName} with id $dni"
			}
			else
			{
				log.debug "found ${d.displayName} with id $dni already exists"
			}

			return d
		}

	log.debug "created ${devices.size()} Sensibo Pod"

	def delete
	// Delete any that are no longer in settings
	if(!SelectedSensiboPods)
	{
		log.debug "delete Sensibo"
		delete = getAllChildDevices()
	}
	else
	{
		delete = getChildDevices().findAll { !SelectedSensiboPods.contains(it.deviceNetworkId) }
	}

	log.debug "deleting ${delete.size()} Sensibo"
	delete.each { deleteChildDevice(it.deviceNetworkId) }

	def PodList = getAllChildDevices()
	
    pollHandler()
    
    refreshDevices()
    
    runEvery15Minutes("refreshDevices")
}


// Subscribe functions

def OnOffHandler(evt) {
	log.debug "on activated "
    debugEvent(evt.value)
    
	//def name = evt.device.displayName

    if (sendPush) {
        if (evt.value == "on") {
            //sendPush("The ${name} is turned on!")
        } else if (evt.value == "off") {
            //sendPush("The ${name} is turned off!")
        }
    }
}

def getPollRateMillis() { return 45 * 1000 }

// Poll Child is invoked from the Child Device itself as part of the Poll Capability
def pollChild( child )
{
	log.debug "poll child"
	debugEvent ("poll child")
	def now = new Date().time

	debugEvent ("Last Poll Millis = ${atomicState.lastPollMillis}")
	def last = atomicState.lastPollMillis ?: 0
	def next = last + pollRateMillis

	log.debug "pollChild( ${child.device.deviceNetworkId} ): $now > $next ?? w/ current state: ${atomicState.sensibo}"
	debugEvent ("pollChild( ${child.device.deviceNetworkId} ): $now > $next ?? w/ current state: ${atomicState.sensibo}")

	//if( now > next )
	if( true ) // for now let's always poll/refresh
	{
		log.debug "polling children because $now > $next"
		debugEvent("polling children because $now > $next")

		pollChildren(child.device.deviceNetworkId)

		log.debug "polled children and looking for ${child.device.deviceNetworkId} from ${atomicState.sensibo}"
		debugEvent ("polled children and looking for ${child.device.deviceNetworkId} from ${atomicState.sensibo}")

		def currentTime = new Date().time
		debugEvent ("Current Time = ${currentTime}")
		atomicState.lastPollMillis = currentTime

		def tData = atomicState.sensibo[child.device.deviceNetworkId]
        
        if (tData == null) return
        
        log.debug  "DEBUG - TDATA" + tData
        debugEvent ("Error in Poll ${tData.data.Error}",false)
        //tData.Error = false
        //tData.data.Error = "Failed"
		if(tData.data.Error != "Success")
		{
			log.error "ERROR: Device connection removed? no data for ${child.device.deviceNetworkId} after polling"

			// TODO: flag device as in error state
			// child.errorState = true

			return null
		}

		tData.updated = currentTime
		
		return tData.data
	}
	else if(atomicState.sensibo[child.device.deviceNetworkId] != null)
	{
		log.debug "not polling children, found child ${child.device.deviceNetworkId} "

		def tData = atomicState.sensibo[child.device.deviceNetworkId]
		if(!tData.updated)
		{
			// we have pulled new data for this thermostat, but it has not asked us for it
			// track it and return the data
			tData.updated = new Date().time
			return tData.data
		}
		return null
	}
	else if(atomicState.sensibo[child.device.deviceNetworkId] == null)
	{
		log.error "ERROR: Device connection removed? no data for ${child.device.deviceNetworkId}"

		// TODO: flag device as in error state
		// child.errorState = true

		return null
	}
	else
	{
		// it's not time to poll again and this thermostat already has its latest values
	}

	return null
}

def setACStates(child,String PodUid, on, mode, targetTemperature, fanLevel, swingM, sUnit)
{
	log.debug "SetACStates for $PodUid ON: $on - MODE: $mode - Temp : $targetTemperature - FAN : $fanLevel - SWING MODE : $swingM - UNIT : $sUnit"
    
    //Return false if no values was read from Sensibo API
    if (on == "--") { return false }
    
    def OnOff = (on == "on") ? true : false
    //if (swingM == null) swingM = "stopped"
    
    log.debug "Target Temperature :" + targetTemperature
    
	def jsonRequestBody = '{"acState":{"on": ' + OnOff.toString() + ',"mode": "' + mode + '"'
    
    log.debug "Fan Level is :$fanLevel"
    log.debug "Swing is :$swingM"
    log.debug "Target Temperature is :$targetTemperature"
    
    if (fanLevel != "null") {
       log.debug "Fan Level info is present"
       jsonRequestBody += ',"fanLevel": "' + fanLevel + '"'
    }
    
    if (targetTemperature != 0) {
    	jsonRequestBody += ',"targetTemperature": '+ targetTemperature + ',"temperatureUnit": "' + sUnit + '"'       
    }
    if (swingM)
    {
        jsonRequestBody += ',"swing": "' + swingM + '"'
    }
    
    jsonRequestBody += '}}'
    
    log.debug "Mode Request Body = ${jsonRequestBody}"
	debugEvent ("Mode Request Body = ${jsonRequestBody}")

	def result = sendJson(PodUid, jsonRequestBody)

	if (result) {
		def tData = atomicState.sensibo[child.device.deviceNetworkId]
        
        if (tData == null) return false
        
		tData.data.fanLevel = fanLevel
        tData.data.thermostatFanMode = fanLevel
        tData.data.on = on
        tData.data.mode = mode
        log.debug "Thermostat mode " + on
        if (on=="off") {
        	tData.data.thermostatMode = "off"
        }
        else {
        	 tData.data.thermostatMode = mode
        }
        tData.data.targetTemperature = targetTemperature
        tData.data.coolingSetpoint = targetTemperature
        tData.data.heatingSetpoint = targetTemperature
        tData.data.thermostatSetpoint = targetTemperature
        tData.data.temperatureUnit = sUnit
        tData.data.swing = swingM
        tData.data.Error = "Success"
	}
    else {
    	def tData = atomicState.sensibo[child.device.deviceNetworkId]
        if (tData == null) return false
    	
        tData.data.Error = "Failed"
    }

	return(result)
}

//Get the capabilities of the A/C Unit
def getCapabilities(PodUid, mode)
{
	def data = [:]   
	def pollParams = [
    	uri: "${getServerUrl()}",
    	path: "/api/v2/pods/${PodUid}",
    	requestContentType: "application/json",
    	query: [apiKey:"${getapikey()}", type:"json", fields:"remoteCapabilities,productModel"]]
     try {

     	 httpGet(pollParams) { resp ->

         if (resp.data) {
         		log.debug "Status : " + resp.status
                if(resp.status == 200) {
                	//resp.data = [result: [remoteCapabilities: [modes: [heat: [swing: ["stopped", "fixedTop", "fixedMiddleTop", "fixedMiddle", "fixedMiddleBottom", "fixedBottom", "rangeTop", "rangeMiddle", "rangeBottom", "rangeFull"], temperatures: [C: ["isNative": true, "values": [16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30]], F: ["isNative": false, "values": [61, 63, 64, 66, 68, 70, 72, 73, 75, 77, 79, 81, 82, 84, 86]]], fanLevels: ["low", "medium", "high", "auto"]], fan: [swing: ["stopped", "fixedMiddleTop", "fixedMiddle", "fixedMiddleBottom", "fixedBottom", "rangeTop", "rangeMiddle", "rangeBottom", "rangeFull"], temperatures: [C: ["isNative": true, "values": [16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30]], F: ["isNative": false, "values": [61, 63, 64, 66, 68, 70, 72, 73, 75, 77, 79, 81, 82, 84, 86]]], fanLevels: ["low", "medium", "high", "auto"]], cool: [swing: ["stopped", "fixedTop", "fixedMiddleTop", "fixedMiddle", "fixedMiddleBottom", "fixedBottom", "rangeTop", "rangeMiddle", "rangeBottom", "rangeFull"], temperatures: ["C": ["isNative": true, "values": [16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30]], F: ["isNative": false, "values": [61, 63, 64, 66, 68, 70, 72, 73, 75, 77, 79, 81, 82, 84, 86]]], fanLevels: ["low", "high", "auto"]]]]]]
                    //resp.data = ["result": ["productModel": "skyv2", "remoteCapabilities": ["modes": ["dry": ["temperatures": ["C": ["isNative": false, "values": [17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30]], "F": ["isNative": true, "values": [62, 63, 64, 65, 66, 67, 68, 69, 70, 71, 72, 73, 74, 75, 76, 77, 78, 79, 80, 81, 82, 83, 84, 85, 86]]], "swing": ["stopped", "rangeFull"]], "auto": ["temperatures": ["C": ["isNative": false, "values": [17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30]], "F": ["isNative": true, "values": [62, 63, 64, 65, 66, 67, 68, 69, 70, 71, 72, 73, 74, 75, 76, 77, 78, 79, 80, 81, 82, 83, 84, 85, 86]]], "swing": ["stopped", "rangeFull"]], "heat": ["swing": ["stopped", "rangeFull"], "temperatures": ["C": ["isNative": false, "values": [17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30]], "F": ["isNative": true, "values": [62, 63, 64, 65, 66, 67, 68, 69, 70, 71, 72, 73, 74, 75, 76, 77, 78, 79, 80, 81, 82, 83, 84, 85, 86]]], "fanLevels": ["low", "medium", "high", "auto"]], "fan": ["swing": ["stopped", "rangeFull"], "temperatures": [], "fanLevels": ["low", "medium", "high", "auto"]], "cool": ["swing": ["stopped", "rangeFull"], "temperatures": ["C": ["isNative": false, "values": [17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30]], "F": ["isNative": true, "values": [62, 63, 64, 65, 66, 67, 68, 69, 70, 71, 72, 73, 74, 75, 76, 77, 78, 79, 80, 81, 82, 83, 84, 85, 86]]], "fanLevels": ["low", "medium", "high", "auto"]]]]]]
                    switch (mode){
                    	case "dry":
                        	data = [
                            	remoteCapabilities : resp.data.result.remoteCapabilities.modes.dry,
                                productModel : resp.data.result.productModel
                           	]	
                        	break
                        case "cool":
                       		data = [
                            	remoteCapabilities : resp.data.result.remoteCapabilities.modes.cool,
                                productModel : resp.data.result.productModel
                           	]	
                        	break
                        case "heat":
                       		data = [
                            	remoteCapabilities : resp.data.result.remoteCapabilities.modes.heat,
                                productModel : resp.data.result.productModel
                           	]	
                        	break
                        case "fan":
                       		data = [
                            	remoteCapabilities : resp.data.result.remoteCapabilities.modes.fan,
                                productModel : resp.data.result.productModel
                           	]	
                        	break
                        case "auto":
                       		data = [
                            	remoteCapabilities : resp.data.result.remoteCapabilities.modes.auto,
                                productModel : resp.data.result.productModel
                           	]	
                        	break
                        case "modes":
                       		data = [
                            	remoteCapabilities : resp.data.result.remoteCapabilities.modes,
                                productModel : resp.data.result.productModel
                           	]	
                        	break                        
                    }
                    return data
                }
                else {
                	log.debug "get remoteCapabilities Failed"
                    
                    data = [
						remoteCapabilities : "",
                        productModel : ""
                    ]                    
              		return data
                }
			}
         }
         return data
     }
     catch(Exception e) {     	
        log.debug "get remoteCapabilities Failed"
        
        data = [
        	remoteCapabilities : "",
            productModel : ""
        ]        
     	return data
     }
}

// Get the latest state from the Sensibo Pod
def getACState(PodUid)
{
	def data = [:]
	def pollParams = [
    	uri: "${getServerUrl()}",
    	path: "/api/v2/pods/${PodUid}/acStates",
    	requestContentType: "application/json",
    	query: [apiKey:"${getapikey()}", type:"json", limit:1, fields:"status,acState,device"]]
    
    try {
       httpGet(pollParams) { resp ->

			if (resp.data) {
				debugEvent ("Response from Sensibo GET = ${resp.data}")
				debugEvent ("Response Status = ${resp.status}")
			}
			
			if(resp.status == 200) {
            	resp.data.result.any { stat ->
                	
                	if (stat.status == "Success") {
                    	
                        log.debug "get ACState Success"
                        log.debug "PodUID : $PodUid : " + stat.acState
                        
                        def OnOff = stat.acState.on ? "on" : "off"
                        stat.acState.on = OnOff
						
						def stemp
                        if (stat.acState.targetTemperature == null) {
                          stemp = stat.device.measurements.temperature.toInteger()
                        }
                        else {
                          stemp = stat.acState.targetTemperature.toInteger()
                        }
                        
                        def tempUnit
                        if (stat.acState.temperatureUnit == null) {
                          tempUnit = stat.device.temperatureUnit
                        }
                        else {
                          tempUnit = stat.acState.temperatureUnit
                        }	
					
                        def tMode                        
                        if (OnOff=="off") {
        					tMode = "off"
        				}
				        else {
        	 				tMode = stat.acState.mode
                        }
                        
                        log.debug "product Model : " + stat.device.productModel
                        def battery = stat.device.productModel == "skyv1" ? "battery" : "mains"
                        
                        log.debug "swing Mode :" + stat.acState.swing
                        data = [
                            targetTemperature : stemp,
                            fanLevel : stat.acState.fanLevel,
                            mode : stat.acState.mode,
                            on : OnOff.toString(),
                            switch: OnOff.toString(),
                            thermostatMode: tMode,
                            thermostatFanMode : stat.acState.fanLevel,
                            coolingSetpoint : stemp,
                            heatingSetpoint : stemp,
                            thermostatSetpoint : stemp,
                            temperatureUnit : tempUnit,
                            swing : stat.acState.swing,
                            powerSource : battery,
                            productModel : stat.device.productModel,
                            firmwareVersion : stat.device.firmwareVersion,
                            Error : "Success"
                        ]

                        log.debug "On: ${data.on} targetTemp: ${data.targetTemperature} fanLevel: ${data.fanLevel} Thermostat mode: ${data.mode} swing: ${data.swing}"
                        return data
                	}
                    else { log.debug "get ACState Failed"}
               }
           }
           else {
           	  data = [
                 targetTemperature : "0",
                 fanLevel : "--",
                 mode : "--",
                 on : "--",
                 switch : "--",
                 thermostatMode: "--",
                 thermostatFanMode : "--",
                 coolingSetpoint : "0",
                 heatingSetpoint : "0",
                 thermostatSetpoint : "0",
                 temperatureUnit : "",
                 swing : "--",
                 powerSource : "",
                 productModel : "",
                 firmwareVersion : "",
                 Error : "Failed"
			  ]
              log.debug "get ACState Failed"
              return data
           }
       }
       return data
    }
    catch(Exception e)
	{
		log.debug "Exception Get Json: " + e
		debugEvent ("Exception get JSON: " + e)
		
        data = [
            targetTemperature : "0",
            fanLevel : "--",
            mode : "--",
            on : "--",
            switch : "--",
            thermostatMode: "--",
            thermostatFanMode : "--",
            coolingSetpoint : "0",
            heatingSetpoint : "0",
            thermostatSetpoint : "0",
            temperatureUnit : "",
            swing : "--",
            powerSource : "",
            productModel : "",
            firmwareVersion : "",
            Error : "Failed" 
		]
        log.debug "get ACState Failed"
        return data
	} 
}

// Send state to the Sensibo Pod
def sendJson(String PodUid, String jsonBody)
{
    log.debug "Request sent to Sensibo API(acStates) for PODUid : $PodUid - $jsonBody"
	def cmdParams = [
		uri: "${getServerUrl()}",
		path: "/api/v2/pods/${PodUid}/acStates",
		headers: ["Content-Type": "application/json"],
        query: [apiKey:"${getapikey()}", type:"json", fields:"acState"],
		body: jsonBody]

	def returnStatus = -1
    try{
       httpPost(cmdParams) { resp ->
			if(resp.status == 200) {
                log.debug "updated ${resp.data}"
				debugEvent("updated ${resp.data}")
				returnStatus = resp.status
				log.debug "Successful call to Sensibo API."
            }
           	else { log.debug "Failed call to Sensibo API." }
       }
    }
    catch(Exception e)
	{
		log.debug "Exception Sending Json: " + e
		debugEvent ("Exception Sending JSON: " + e)
		return false
	}
    
    if (returnStatus == 200)
		return true
	else
		return false
}

def pollChildren(PodUid)
{
	log.debug "polling children"
    
   def thermostatIdsString = PodUid

	log.debug "polling children: $thermostatIdsString"
    
	def pollParams = [
    	uri: "${getServerUrl()}",
    	path: "/api/v2/pods/${thermostatIdsString}/measurements",
    	requestContentType: "application/json",
    	query: [apiKey:"${getapikey()}", type:"json", fields:"batteryVoltage,temperature,humidity,time"]]

	debugEvent ("Before HTTPGET to Sensibo.")

	try{
		httpGet(pollParams) { resp ->

			if (resp.data) {
				debugEvent ("Response from Sensibo GET = ${resp.data}")
				debugEvent ("Response Status = ${resp.status}")
			}

			if(resp.status == 200) {
				log.debug "poll results returned"                                

                log.debug "DEBUG DATA RESULT" + resp.data.result
                
                if (resp.data.result == null || resp.data.result.empty) 
                {
                	log.debug "Cannot get measurement from the API, should ask Sensibo Support Team"
                	debugEvent ("Cannot get measurement from the API, should ask Sensibo Support Team",true)
                }
                
                def setTemp = getACState(thermostatIdsString)
                if (setTemp.Error != "Failed") {
                
				 atomicState.sensibo = resp.data.result.inject([:]) { collector, stat ->

					def dni = thermostatIdsString
					
					log.debug "updating dni $dni"
                    
                    def stemp = stat.temperature.toDouble().round(1)
                    def shumidify =  stat.humidity.toDouble().round()

                    if (setTemp.temperatureUnit == "F") {
                        stemp = cToF(stemp).round(1)
                    }

					def tMode                        
                    if (setTemp.on=="off") {
        				tMode = "off"
        			}
				    else {
        	 			tMode = setTemp.mode
                    }

					def battpourcentage = 100
                    def battVoltage = stat.batteryVoltage
                    
					if (battVoltage == null) 
                    {
                    	battVoltage = 3000
                    }                    
                    
                    if (battVoltage < 2850) battpourcentage = 10
                    if (battVoltage > 2850 && battVoltage < 2950) battpourcentage = 50
                    
					def data = [
						temperature: stemp,
						humidity: shumidify,
                        targetTemperature: setTemp.targetTemperature,
                        fanLevel: setTemp.fanLevel,
                        mode: setTemp.mode,
                        on: setTemp.on,
                        switch : setTemp.on,
                        thermostatMode: tMode,
                        thermostatFanMode: setTemp.fanLevel,
                        coolingSetpoint: setTemp.targetTemperature,
                        heatingSetpoint: setTemp.targetTemperature,
                        thermostatSetpoint: setTemp.targetTemperature,
                        temperatureUnit : setTemp.temperatureUnit,
                        voltage : battVoltage,
                        swing : setTemp.swing,
                        battery : battpourcentage,
                        powerSource : setTemp.powerSource,
                        productModel : setTemp.productModel,
                        firmwareVersion : setTemp.firmwareVersion,
                        Error: setTemp.Error
					]
                    
					debugEvent ("Event Data = ${data}",false)

					collector[dni] = [data:data]
                    
					return collector
				 }				
                }
                
				log.debug "updated ${atomicState.sensibo[thermostatIdsString].size()} stats: ${atomicState.sensibo[thermostatIdsString]}"
                debugEvent ("updated ${atomicState.sensibo[thermostatIdsString]}",false)
			}
			else
			{
				log.error "polling children & got http status ${resp.status}"		
			}
		}

	}
	catch(Exception e)
	{
		log.debug "___exception polling children: " + e
		debugEvent ("${e}")
	}
}

def pollHandler() {

	debugEvent ("in Poll() method.")
	
    // Hit the Sensibo API for update on all the Pod
	
    def PodList = getAllChildDevices()
    
    log.debug PodList
    PodList.each { 
    	log.debug "polling " + it.deviceNetworkId
        pollChildren(it.deviceNetworkId) }
	
    atomicState.sensibo.each {stat ->

		def dni = stat.key

		log.debug ("DNI = ${dni}")
		debugEvent ("DNI = ${dni}")

		def d = getChildDevice(dni)

		if(d)
		{        
			log.debug ("Found Child Device.")
			debugEvent ("Found Child Device.")
			debugEvent("Event Data before generate event call = ${stat}")
			log.debug atomicState.sensibo[dni]
			d.generateEvent(atomicState.sensibo[dni].data)
		}
	}
}

def debugEvent(message, displayEvent = false) {

	def results = [
		name: "appdebug",
		descriptionText: message,
		displayed: displayEvent
	]
	log.debug "Generating AppDebug Event: ${results}"
	sendEvent (results)

}

def cToF(temp) {
	return (temp * 1.8 + 32).toDouble()
}

def fToC(temp) {
	return ((temp - 32) / 1.8).toDouble()
}