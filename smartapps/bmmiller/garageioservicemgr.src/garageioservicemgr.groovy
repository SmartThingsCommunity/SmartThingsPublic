/**
 *  GarageioServiceMgr v1.3 - 2016-03-10
 *		
 * 		Changelog
 *			v1.3 	- Added watchdogTask() to restart polling if it stops, inspiration from Pollster
 *			v1.2 	- Added multiAttributeTile() to make things look prettier. No functionality change.
 *			v1.1.1 	- Tiny fix for service manager failing to complete
 *			v1.1   	- GarageioServiceMgr() and Device Handler impplemented to handle ChildDevice creation, deletion, 
 *				   	polling, and ST suggested implementation.
 *			v1.0   	- GarageioInit() implementation to handle polling in a Smart App, left this way for quite a while
 *			v0.1   	- Initial working integration
 *
 *  Copyright 2016 Brandon Miller
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
 
import groovy.json.JsonSlurper
 
definition(
    name: "GarageioServiceMgr",
    namespace: "bmmiller",
    author: "Brandon Miller",
    description: "Initializes and sets up child devices (doors) and polls Garageio devices (doors)",
    category: "My Apps",
    iconUrl: "https://raw.githubusercontent.com/bmmiller/SmartThings/master/devicetype.garageio/garageio.png",
    iconX2Url: "https://raw.githubusercontent.com/bmmiller/SmartThings/master/devicetype.garageio/garageio.png",
    iconX3Url: "https://raw.githubusercontent.com/bmmiller/SmartThings/master/devicetype.garageio/garageio.png")


preferences {
	page(name: "about", title: "About", nextPage: "authInfo")
	page(name: "authInfo", title: "Garageio", nextPage:"deviceList")
	page(name: "deviceList", title: "Garageio Controlled Doors", content:"GarageioDeviceList")         
    page(name: "otherSettings", title: "Other Settings", content:"otherSettings", install:true)
}

def about() {
 	dynamicPage(name: "about", install: false, uninstall: true) {
 		section("About") {	
			paragraph "GarageioServiceMgr, the smartapp that initializes your Garageio device (doors) and polls them on a regular basis"
			paragraph "Version 1.3\n\n" +
				"If you like this app, please support the developer via PayPal:\nbmmiller@gmail.com\n\n" +
				"Copyright©2016 Brandon Miller"
			href url: "http://github.com/bmmiller", style: "embedded", required: false, title: "More information..."
		} 
	}        
}

def authInfo() {
	dynamicPage(name: "authInfo") {
    	section("Provide login information") {
    		input("email_address", "text", title: "Username", description: "Your Garageio username")
    		input("password", "password", title: "Password", description: "Your Garageio password")
		}
    }
}

def GarageioDeviceList() {
	log.trace "GarageioDeviceList()"
    
    def garageioDoors = getGarageioDoors()
    
    log.trace "device list: $garageioDoors"
    
    def p = dynamicPage(name: "deviceList", title: "Select Your Doors(s)",nextPage:"otherSettings") {
		section(""){
			paragraph "Tap below to see the list of Garageio Controlled Doors available on your Garageio Device and select the ones you want to connect to SmartThings."
			input(name: "GarageioDoors", title:"", type: "enum", required:true, multiple:true, description: "Tap to choose", metadata:[values:garageioDoors])
		}
	}

	log.debug "list p: $p"
	return p    	     
}

def getGarageioDoors() {
	def GARAGEIO_SUCCESS=200
    
	log.debug "Getting Garageio doors list"
    
    if (atomicState.token == null || atomicState.userid == null)
    	getAuthToken()
       
    def doors = [:]
    
    try 
    {
    	httpGet("${get_URI_ROOT()}/SyncController.php?auth_token=" + atomicState.token + "&user_id=" + atomicState.userid)
        	{ response ->            
            	if (response.status == GARAGEIO_SUCCESS)
                {                                            
                    response.data.data.devices[0].doors.each {
						def name = it.name
                        def dni = it.id                       
						doors[dni] = name
					}                    
                }
    		}            
    }
    catch (e) {
		state?.msg= "exception $e while getting list of Doors" 
		log.error state.msg        
    }      

	return doors
}

def otherSettings() {
	dynamicPage(name: "otherSettings", title: "Other Settings", install: true, uninstall: false) {
    	section("Polling at which interval in minutes (range=[5..59],default=30 min.)?") {
			input "givenInterval", "number", title:"Interval", required: false
		}
		section("Notifications") {
			input "sendPushMessage", "enum", title: "Send a push notification?", metadata: [values: ["Yes", "No"]], required:
				false
			input "phoneNumber", "phone", title: "Send a text message?", required: false
		}
		section([mobileOnly:true]) {
			label title: "Assign a name for this SmartApp", required: false
		}
	}
}

def getAuthToken() {
	atomicState.token = null
    atomicState.userid = null

    def params = [
        uri: "${get_URI_ROOT()}/AuthController.php",
        body: [email_address: email_address, password: password]
    ]
	
    httpPost(params) {response -> 
    	atomicState.token = response.data.data[0].authentication_token
    	atomicState.userid = response.data.userid 
        //return response.data
    }
}

def installed() {
	log.debug "Installed with settings: ${settings}"

	initialize()
}

def updated() {
	log.debug "Updated with settings: ${settings}"

	unsubscribe()
	unschedule()    
	initialize()
}

private def delete_child_devices() {
	def delete
    
	// Delete any that are no longer in settings
	if (!GarageioDoors) {
		log.debug "delete_child_devices> deleting all Garageio Doors"
		delete = getAllChildDevices()
	} else {
		delete = getChildDevices().findAll { !GarageioDoors.contains(it.deviceNetworkId) }
		log.debug "delete_child_devices> deleting ${delete.size()} Garageio Doors"	
	}

	try { 
		delete.each { deleteChildDevice(it.deviceNetworkId) }
	} catch (e) {
		log.debug "delete_child_devices> exception $e while deleting ${delete.size()} Garageio Doors"
	}	
}

private def create_child_devices() {
	def devices = GarageioDoors.collect { dni ->
    
        def d = getChildDevice(dni)
        log.debug "create_child_devices> looping thru Garageio Doors, found id $dni"

        if(!d) {                     
            def labelName = "Garageio ${dni}"
            log.debug "create_child_devices> about to create child device with id $dni"
            d = addChildDevice(getChildNamespace(), getChildName(), dni, null,
                               [label: "${labelName}"])            
            log.debug "create_child_devices> created ${d.displayName} with id $dni"

        } else {
            log.debug "create_child_devices> found ${d.displayName} with id $dni already exists"
        }
	}

	log.debug "create_child_devices> created ${devices.size()} Garageio door"
}

def initialize() {
	log.debug "initialize"
    atomicState.lastpoll = 0
	state?.exceptionCount = 0
	state?.msg=null    
	delete_child_devices()	
	create_child_devices() 
    
    Integer delay = givenInterval ?: 30 // By default, do it every 30 min.
	if ((delay < 5) || (delay>59)) {
		state?.msg= "GarageioServiceMgr> scheduling interval not in range (${delay} min), exiting..."
		log.debug state.msg
		runIn(30, "sendMsgWithDelay")
 		return
	}
	schedule("0 0/${delay} * * * ?", takeAction)
    schedule("0 1/15 * * * ?", watchdogTask)
}

def takeAction() {
	log.trace "takeAction> begin"
    def MAX_EXCEPTION_COUNT=5    
	def msg
    String exceptionCheck
    
	def devices = GarageioDoors.collect { dni ->
    
		def d = getChildDevice(dni)      
		        
		log.debug "takeAction> looping thru Garageio Doors, found id $dni, about to poll"
		try {
			d.poll() 
            exceptionCheck = d.currentVerboseTrace.toString()
			if (((exceptionCheck.contains("exception") || (exceptionCheck.contains("error"))) && 
				(!exceptionCheck.contains("Java.util.concurrent.TimeoutException")))) {  
				// check if there is any exception reported in the verboseTrace associated to the device (except the ones linked to rate limiting).
				state.exceptionCount=state.exceptionCount+1    
				log.error "found exception after polling, exceptionCount= ${state?.exceptionCount}: $exceptionCheck"
            } else {             
				// reset exception counter            
				state?.exceptionCount=0   
			} 
		} catch (e) {       
            //state?.exceptionCount=state?.exceptionCount+1
			//log.error "GarageioServiceMgr> exception $e while trying to poll the device $dni, exceptionCount= ${state?.exceptionCount}" 
		}    
        
        if (state?.exceptionCount>=MAX_EXCEPTION_COUNT) {
			// need to re-authenticate again    
			atomicState.token = null                    
			msg = "GarageioServiceMgr> too many exceptions/errors or unauthorized exception, $exceptionCheck (${state?.exceptionCount} errors), need to re-authenticate at Garageio..." 
			log.error msg
			send msg
           getAuthToken()
		}
	}
    log.trace "takeAction> end"
}

def pollChildren() { 
	def GARAGEIO_SUCCESS=200
    def GARAGEIO_AUTH_ERROR=401

	if (atomicState.token == null || atomicState.userId == null)
    	getAuthToken()
        
    atomicState.lastpoll = now()
    
    def args = "auth_token=" + atomicState.token + "&user_id=" + atomicState.userid   
    def devicePollParams = [
        uri: "${get_URI_ROOT()}/SyncController.php?${args}",
        Accept: "application/json",
		charset: "UTF-8"
    ]
        
    try 
    {
    	httpGet(devicePollParams)
        	{ response -> 
            	if (response.status == GARAGEIO_SUCCESS) { 
                	log.debug response.data
                	return response.data.data.devices[0].doors 
                } else if (response.status == GARAGEIO_AUTH_ERROR) {
                	// Need to re-auth
                    state?.msg="Need to re-authenticate, calling getAuthToken()"
                    log.error state.msg
                    getAuthToken()
                }
            }            
    }
    catch (e) { 	
		state?.msg= "exception $e while getting list of Doors" 
		log.error state.msg        
    }
}

def push(doorId, changeState) {
	log.debug "push(): Push call for doorId: ${doorId}"
	if (atomicState.token == null || atomicState.userid == null)
    {
        getAuthToken()
    }
    def params = [
        uri: "${get_URI_ROOT()}/ToggleController.php",
        headers: [ "Content-Type": "application/x-www-form-urlencoded" ],
        body: [ 
            auth_token: atomicState.token, 
            user_id: atomicState.userid, 
            door_id: doorId, 
            door_state: changeState 
        ]
    ]

    httpPost(params) { response->
        log.debug response.data
        
        if (response.data.code == 401)
        	getAuthToken()
            
        return response.data.code
    }
	
}

def watchdogTask() {
    LOG("watchdogTask()")

    if (settings.givenInterval && atomicState.lastpoll) {
        def t = now() - atomicState.lastpoll
        if (t > (settings.givenInterval * 120000)) {
            log.warn "GarageioServiceMgr is toast. Restarting..."
            sendNotification("GarageioServiceMgr is toast. Restarting...")
            updated()
            return
        }
    }  
}

def getChildName() { "Garageio Device" }

def getChildNamespace() { "bmmiller" }

def get_URI_ROOT() { "https://garageio.com/api/controllers" }

def uninstalled() {
    removeChildDevices(getChildDevices())
}

private removeChildDevices(delete) {
    delete.each {
        deleteChildDevice(it.deviceNetworkId)
    }
}