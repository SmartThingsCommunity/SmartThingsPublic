  
//working Flume smartapp

/**
 *  Flume Water Flow SM
 *  Smart App/ Service Manager for Flume Water Flow Meter
 *  This will create a companion Device Handler for the Flume device
 *  Version 1.0
 *
 *  You MUST enter the API Key value via 'App Settings'->'Settings'->'api_key' in IDE by editing this SmartApp code
 *  This key is provided by Flume upon request
 *
 *  Copyright 2019 Ulices Soriano
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
import groovy.json.*
import groovy.time.*
import groovy.time.TimeCategory
 
definition(
    name: "Flume Water Flow SM",
    namespace: "getterdone",
    author: "Ulices Soriano",
    description: "Service Manager for cloud-based API for Flume Water Flow meter",
    category: "My Apps",
    iconUrl: "https://windsurfer99.github.io/ST_Flume-Water-Flow/tap-water-icon-128.png",
    iconX2Url: "https://windsurfer99.github.io/ST_Flume-Water-Flow/tap-water-icon-256.png",
    iconX3Url: "https://windsurfer99.github.io/ST_Flume-Water-Flow/tap-water-icon-256.png",
 	singleInstance: true) 
	
	
{
    appSetting "FlumeAPI_Key"
    appSetting "FlumeAPI_Secret"
}


preferences {
	page(name: "pageOne", title: "Options", uninstall: true, install: true) {
		section("Inputs") {
        		paragraph ("You MUST set the 'API Key' and 'API Secret' via App Settings in IDE")
            		label (title: "Assign a name for Service Manager", required: true, multiple: true)
							input(
					name: "username",
					type: "email",
					required: true,
					title: "Email Address"
			)
			input(
					name: "password",
					type: "password",
					required: true,
					title: "Password"
			)
            		/* input (name: "Flume_awayModes", type: "mode", title: "Enter SmartThings modes when water meter should be Away", 
                    		multiple: true, required: false) */
            		input (name: "userFlume_locName", type: "text", title: "Enter Flume location name assigned to Flume flow meter", 
                    		multiple: false, required: true)
                    input (name: "configLoggingLevelIDE",
                        title: "IDE Live Logging Level:\nMessages with this level and higher will be logged to the IDE.",
                        type: "enum",
                        options: [
                            "0" : "None",
                            "1" : "Error",
                            "2" : "Warn",
                            "3" : "Info",
                            "4" : "Debug",
                            "5" : "Trace"
                        ],
                        defaultValue: "3",
                        displayDuringSetup: true,
                        required: false
                    )
					
		}
	}
}

def getApiBase() { return "https://api.flumetech.com/" }

private String FlumeAPIKey() {return appSettings.FlumeAPI_Key}
private String FlumeAPISecret() {return appSettings.FlumeAPI_Secret}

//required methods
def installed() {
	//log.debug "Flume SM installed with settings: ${settings}"
    state.enteredLocName =  userFlume_locName//save off the location name entered by user
    runIn(3, "initialize")
}

def updated() {
    if (state.enteredLocName != userFlume_locName) { //if location name changed, need to make a new device
    	logger("Flume SM updated() called- new device with settings: ${settings}","trace")
        unsubscribe()
        cleanup()
    	runIn(10, "initialize") //deleteChildDevice seems to take a while to delete; wait before re-creating
    } else {
	   	state.loggingLevelIDE = (settings.configLoggingLevelIDE) ? settings.configLoggingLevelIDE.toInteger() : 3
		logger("Flume SM updated() called- same name, no new device with settings: ${settings}","info")
    }
}

def initialize() {
    state.loggingLevelIDE = (settings.configLoggingLevelIDE) ? settings.configLoggingLevelIDE.toInteger() : 3
    logger("Flume SM initialize() called with settings: ${settings}","trace")
	// get the value of api key
	def mySecret = FlumeAPISecret()  //appSettings.api_key
    if (mySecret.length() <20) {
    	logger("Flume SM initialize- api_secret value not set properly in IDE: ${mySecret}","error")
    }
	state.flumeUserId = getflumeUserId()
	state.flumeDeviceId  = getflumeDeviceId()
    state.Flume_location = null
    state.childDevice = null
    state.inAlert = false
    /* state.homeAway = "home" */
    subscribe(location, "mode"/* , modeChangeHandler */)
    initFlume_locations(flumeUserId) //determine Flume location to use
    log.debug("initialize()FLOW state.Flume_location = ${state.Flume_location}")
    
    if (state.Flume_location) { 
         log.debug("we have a device; put it into initial state")      
        def eventData = [name: "water", value: "dry"]
        //log.debug("state.Flume_location?.id ===${state.Flume_location?.id}")
          //def idToString = (state.Flume_location?.id).toString()
          log.debug("inside initialize state.flumeDeviceId '${state.flumeDeviceId}'")
        def existingDevice = getChildDevice(state.flumeDeviceId)//idToString)              
        existingDevice?.generateEvent(eventData) //this was off the hold time?? now back on not sure what is happening
        state.inAlert =  false
       schedule("0 0/3 * * * ?", pollSLAlert) //Poll Flume cloud for leak alert //change 0 0/3 to 2  0/2 for two minutes
        runIn(8,"initDevice") //update once things are initialized
    }
    
}

def getDefaultHeaders() {
	return [
		"User-Agent": "okhttp/3.2.0",
		"Content-Type": "application/json"
	]
}

def getDefaultAuthHeaders() {
	def headers = getDefaultHeaders()
    //log.debug("Getting myToken to use in getDefaultAuthHeaders() ${state?.myToken}")
	headers["Authorization"] = "Bearer ${state?.myToken[0]}"
	return headers
}


def login() {
	def flumeUserId = null;
	def body = new groovy.json.JsonOutput().toJson([
			"grant_type":"password",
			"client_id":FlumeAPIKey(),
			"client_secret":FlumeAPISecret(),
			"username"   : settings.username,
			"password": settings.password			
	])
	def params = [
		uri: getApiBase(),
		path: "oauth/token",
		headers: getDefaultHeaders(),
		body: body
	]

	try {
		//log.debug("getJWT: Trying to login.")
		httpPostJson(params) { resp ->
			if (resp?.status == 200) {
				//log.debug("getJWT: Successful")
                //log.debug("get resp.data: ${resp.data}")
                //log.debug("getsize: ${resp.data.getAt('data').size()}")
                //log.debug("getAt: ${resp.data.getAt('data').access_token}")
              // DecodedJWT jwt = JWT.decode(response.getInstance().getAccessToken())
                state.myToken = resp.data.getAt('data').access_token
                //log.debug("getToken: ${state.myToken}")
				state.jwt = resp.data.jwt
				def jsonSlurper = new JsonSlurper()
                //log.debug("getsize myToken: ${state.myToken.size()}")
                //log.debug("getsize myToken: ${state.myToken.size()}")
				def parsedJwt = /*resp.data.jwt*/state.myToken[0].tokenize(".")[1]
				parsedJwt = new String(parsedJwt?.decodeBase64(), "UTF-8")
				parsedJwt = jsonSlurper.parseText(parsedJwt)
				flumeUserId = parsedJwt?.user_id
				state.jwtExpireTime = parsedJwt?.exp
				state.flumeUserId = flumeUserId
			} else {
				log.error("getJWT: 'Unexpected' Response: ${resp?.status}: ${resp?.data}")
			}
		}
	} catch (ex) {
		log.error("getJWT Exception:", ex)
	}
	//log.debug("myFlumeID = ${state.flumeUserId}")
	return state.flumeUserId//flumeUserId
}

def isLoggedIn() {
	if (state?.flumeUserId == null || state?.jwtExpireTime == null || state?.jwtExpireTime <= (new Date().getTime() / 1000l)) {
		return false
	}
	return true
}


def getflumeUserId() {
	if (!isLoggedIn()) {
		return login()
	}
	return state.flumeUserId
}

def getflumeDeviceId(){
	if (!isLoggedIn()) {
		return login()
	}
	return state.flumeDeviceId
}

def uninstalled() {
    logger("Flume SM uninstalled() called","trace")
    cleanup()
}


//remove things
def cleanup() {
    logger("Flume SM cleanup() called","trace")
    def Flume_Devices = getChildDevices()
    Flume_Devices.each {
    	logger("Flume SM cleanup- deleting SL deviceNetworkID: ${it.deviceNetworkId}","info")
    	try {
            deleteChildDevice(it.deviceNetworkId)
        }
    	catch (e) {
    		logger("Flume SM cleanup- caught and ignored deleting child device: {it.deviceNetworkId}: $e","info")
       	}
    }
    state.Flume_location = null
    state.childDevice = null
    state.inAlert = false
}

//Handler for schedule; determine if there are any alerts

def pollSLAlert() {
    logger("Flume SM pollSLAlert() called","trace")
     //def idToString = (state.Flume_location?.id).toString()
    def existingDevice = getChildDevice(state.flumeDeviceId)//idToString)
	if (state.Flume_location){
       def params = [
            uri:  getApiBase(),
			path: "users/${flumeUserId}/notifications",         
			headers: getDefaultAuthHeaders(),
    ]
        try {
            httpGet(params) {resp ->
    			logger("Flume SM pollSLAlert resp.data: ${resp.data}","debug")
                def resp_data = resp.data
                
                log.debug ("resp_data line 262 begin ${resp_data} end")
               
                	def myLowWaterFlowAlert = null
                    def myLowWaterFlowAlertMessage = null
                    def Flume_locationsAlert 
                   resp_data.data.message.each{ tempMessage->
                   log.debug (tempMessage)
                   if(tempMessage.contains("Low Flow Leak")){
                   myLowWaterFlowAlertMessage = tempMessage //"Low Water Alert True"
                   myLowWaterFlowAlert = true
                   Flume_locationsAlert = "Low Flow Leak"
                   }
                   
                   }
               
				 log.debug ("resp_data line 277 begin '${myLowWaterFlowAlertMessage}' end ")
                      
                if (myLowWaterFlowAlert) {
                    //send wet event to child device handler every poll to ensure not lost due to handler pausing
    				logger("Flume SM pollSLAlert Alert0 received: ${Flume_locationsAlert}; call changeWaterToWet","info")
                    existingDevice?.changeWaterToWet()
                    state.inAlert =  true
                } else {
                    if (state.inAlert){
                        //alert removed, send dry event to child device handler only once
    					logger("Flume SM pollSLAlert Alert0 deactivated ; call changeWaterToDry","info")
                        existingDevice?.changeWaterToDry()
                        state.inAlert =  false
                    }
                }
            }
        } catch (e) {
    		logger("Flume SM pollSLAlert error retrieving alerts: $e","error")
        }
    }
}


//callback in order to initialize device
def initDevice() {
    logger("Flume SM initDevice() called","trace")
	determineFlows()
    /* determinehomeAway() */
    //def idToString = (state.Flume_location?.id).toString()
  def existingDevice = getChildDevice(state.flumeDeviceId)//idToString)//idToString)
 existingDevice?.refresh()
}


/*def now = new Date()
def date = new Date()
sdf = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss")
println sdf.format(date)//log.debug(now.format("yyyyMMdd-HH:mm:ss.SSS", TimeZone.getTimeZone('UTC')))
*/
//"2016-04-04 01:00:00",

//determine flow totals from cloud
def determineFlows() {
def today = null
def lastHour = null
def last24Hours = null 
def adjustedDate = null
use (groovy.time.TimeCategory) {
def date = new Date()
def sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
adjustedDate = date-4.hours
today = sdf.format(adjustedDate)
lastHour = sdf.format(adjustedDate-120.minutes)
last24Hours = sdf.format(adjustedDate-24.hours)

}

log.debug("todayTime '${today}'")
log.debug("lastHourTime '${lastHour}'")
log.debug("last24HoursTime '${last24Hours}'")



	 def body = new groovy.json.JsonOutput().toJson([
           
           queries:[ 
           
           [
            request_id: "today",
            bucket: "DAY",
            since_datetime: today,
            ],
            
            [
            request_id: "thisMonth",
            bucket: "MON",
            since_datetime: today,
            ],
            
            [
            request_id: "thisYear",
            bucket: "YR",
            since_datetime: today,
            ],
            
            
            [
             request_id: "lastHour",
             operation: "SUM",
            bucket: "MIN",
            since_datetime: lastHour,
            ],
            
            [
             request_id: "last24Hours",
             operation: "SUM",
            bucket: "HR",
            since_datetime: last24Hours,
            ],
            
            [
             request_id: "currentMin",
            bucket: "MIN",
            since_datetime: today,
            ]
            
   		 ],
    ])
    //debug stuff json
   // def bodyString = new groovy.json.JsonOutput().prettyPrint(body)
	  //log.debug("body output'${body}'")
      //log.debug("bodyString output '${bodyString}'")
      
    logger("Flume SM determineFlows() called","trace")
    //def idToString = (state.Flume_location?.id).toString()
    def existingDevice = getChildDevice(state.flumeDeviceId) //idToString) //need to do the ? to see what it does
  	log.debug("determineFlows(): state.flumeDeviceId '${state.flumeDeviceId}'")
	if (existingDevice){
       def params = [
            uri:  getApiBase(),
			path: "users/${flumeUserId}/devices/${state.flumeDeviceId}/query",         
			headers: getDefaultAuthHeaders(),
            body: body //bodyString
    ]
    //log.debug("try params output'${params}'")
        try {
        		                
                httpPostJson(params) {resp ->
                    def resp_data = resp.data
                
                    if (resp.status == 200){//successful retrieve
                    
                    log.debug("resp_data success 200 '${resp_data}'")
                    //log.debug("resp_data.data'${resp_data.data}'")
                   // log.debug("resp_data.data.today.value '${resp_data.data.today.value}'")        //.value returns [[0]] .value[0] returns [0]
                     }
                    else{
                     log.debug("fail httpPostJson(params)")
                    }
                    
                //this should have worked? log.debug("resp.status '${resp.status}'")
    			//logger("Flume SM determineFlows resp.data: ${resp.data}","debug")
                
                         
               state.todayFlow = (resp_data.data.today.value[0][0]).toInteger()
               log.debug(" state.todayFlow '${ state.todayFlow}'")
               
               state.thisMonthFlow = (resp_data.data.getAt('thisMonth').getAt('value')[0][0]).toInteger()  //resp_data.data.thisMonth.value
                 log.debug("state.thisMonthFlow  '${state.thisMonthFlow }'")
               
               state.thisYearFlow = (resp_data.data.thisYear.value[0][0]).toInteger()
                 log.debug("state.thisYearFlow '${state.thisYearFlow}'")
                //state.unitsFlow = resp_data?.units
                
            }
        } catch (e) {
    		logger("Flume SM determineFlows error retrieving summary data e= ${e}","error")
            
            state.todayFlow = 0
            state.thisMonthFlow = 0
            state.thisYearFlow = 0
            
            state.unitsFlow = "gallons"
            
        }
    }
}
 
//Get desired location from Flume cloud based on user's entered location's name
def initFlume_locations(flumeUserId) {
	//log.debug("initFlume_locations(flumeUserId) = ${flumeUserId}")
    logger("Flume SM initFlume_locations() called","trace")
    /*
        def qs = new groovy.json.JsonOutput().toJson(
        	[
                "user": false,
                "location": true
             ])
    log.debug("qs ${qs}")*/
    def params = [
            uri:  getApiBase(),
			path: "users/${flumeUserId}/devices",  
            query: [
           		 "user": false,
                "location": true
            ],
            headers: getDefaultAuthHeaders()
            ]
    state.Flume_location = null
	state.flumeDeviceId = null
log.debug("params ${params}")
	try {
        httpGet(params) {resp ->
            ////log.debug("resp.size() === ${resp.size()} ") 
            def resp_data = resp.data
          //  def Flume_locations1 = resp_data.data[1]
          //  log.debug("resp '${resp}'")
           // log.debug("Flume_locations1 '${Flume_locations1}'")
           // log.debug("resp_data '${resp_data}'")
            def Flume_locations0 = resp_data.data[0]
			state.flumeDeviceId = Flume_locations0.id
			log.debug("Flume_locations0 '${Flume_locations0}'")
			log.debug("flumeDeviceId '${state.flumeDeviceId}'")
           // log.debug("Flume_locations0.location '${Flume_locations0.location}'")
			log.debug("Flume_locations0.location.name '${Flume_locations0.location.name}'")
           // log.debug("resp.data.data.location '${resp.data.data.location}'")
            
            def ttl = resp_data.count
    
             resp.data.data.each{Flume_loc->
     
           		def tempLocationName = Flume_loc.location.name
                log.debug("tempLocationName '${tempLocationName}'")
         		
                if (tempLocationName.equalsIgnoreCase(userFlume_locName)) { //Let user enter without worrying about case
                     state.Flume_location = Flume_loc  //resp.data.data[myCounter]//Flume_loc //all data resp
                }
            }
            
             //log.debug("final loop count value ${myCounter} ")
            if (!state.Flume_location) {
		    	logger("Flume SM in initFlume_locations- Flume location name: ${userFlume_locName} not found!","warn")
            } else {
		                
                //create device handler for this location (device)
                //def idToString = (state.Flume_location.id).toString()
                def existingDevice = getChildDevice(state.flumeDeviceId)//                idToString)   //state.Flume_location.locationId)
				//log.debug("line 416 existingDevice getChildDevice state.Flume_location.locationId=== ${state.Flume_location.get('id')}") //state.Flume_location.locationId}")
                if(!existingDevice) {
                   def childDevice = addChildDevice("getterdone", "Flume Water Flow DH", state.flumeDeviceId /*idToString*/, null, [name: "Flume Water Flow DH", 
                        label: "Flume Water Flow DH", completedSetup: true])
		    		//logger("Flume SM initFlume_locations- device created with Id: ${state.Flume_location.get('id')/*locationId*/} for Flume_location: ${state.Flume_location.get('name')/*name*/}","info")
					////log.debug("Flume SM initFlume_locations- device created with Id: ${state.Flume_location.get('id')/*locationId*/} for Flume_location: ${state.Flume_location.get('name')/*name*/} ") 
                } else {
		    		logger("Flume SM initFlume_locations- device not created; already exists: ${existingDevice.getDeviceNetworkId()}","warn")
                }
            }
       } 
    } catch (e) {
		logger("Flume SM error in initFlume_locations retrieving locations: $e","error")
    }
}

// Child called methods

// return current flow totals, etc.
Map retrievecloudData() {
	logger("Flume SM retrievecloudData() called","trace")
    //get latest data from cloud
  
   determineFlows()
   pollSLAlert()
	return ["todayFlow":state.todayFlow, "thisMonthFlow":state.thisMonthFlow, 
      "thisYearFlow":state.thisYearFlow,  "inAlert":state.inAlert]
}

//delete child device; called by child device to remove itself. Seems unnecessary but documentation says to do this
def	deleteSmartLabsDevice(deviceid) {
	logger("Flume SM deleteSmartLabsDevice() called with deviceid: ${deviceid}","trace")
    def Flume_Devices = getChildDevices()
    Flume_Devices?.each {
    	if (it.deviceNetworkId == deviceid) {
			logger("Flume SM deleteSmartLabsDevice- deleting SL deviceNetworkID: ${it.deviceNetworkId}","info")
            try {
                deleteChildDevice(it.deviceNetworkId)
                sendEvent(name: "DeviceDelete", value: "${it.deviceNetworkId} deleted")
            }
            catch (e) {
				logger("Flume SM deleteSmartLabsDevice- caught and ignored deleting child device: {it.deviceNetworkId} during cleanup: $e","info")
            }
        }
    }
}

/**
 *  logger()
 *
 *  Wrapper function for all logging. Thanks codersaur.
 **/
private logger(msg, level = "debug") {

    switch(level) {
        case "error":
            if (state.loggingLevelIDE >= 1) log.error msg
            break

        case "warn":
            if (state.loggingLevelIDE >= 2) log.warn msg
            break

        case "info":
            if (state.loggingLevelIDE >= 3) log.info msg
            break

        case "debug":
            if (state.loggingLevelIDE >= 4) //log.debug msg
            break

        case "trace":
            if (state.loggingLevelIDE >= 5) log.trace msg
            break

        default:
            //log.debug msg
            break
    }
}