/**
 *  MyQ (Connect)
 *
 *  Copyright 2015 Jason Mok
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
 *  Last Updated : 7/15/2015
 *
 */
definition(
	name: "MyQ (Connect)",
	namespace: "copy-ninja",
	author: "Jason Mok",
	description: "Connect MyQ to control your devices",
	category: "SmartThings Labs",
	iconUrl:   "http://smartthings.copyninja.net/icons/MyQ@1x.png",
	iconX2Url: "http://smartthings.copyninja.net/icons/MyQ@2x.png",
	iconX3Url: "http://smartthings.copyninja.net/icons/MyQ@3x.png"
)

preferences {
	page(name: "prefLogIn", title: "MyQ")    
	page(name: "prefListDevices", title: "MyQ")
}

/* Preferences */
def prefLogIn() {
	def showUninstall = username != null && password != null 
	return dynamicPage(name: "prefLogIn", title: "Connect to MyQ", nextPage:"prefListDevices", uninstall:showUninstall, install: false) {
		section("Login Credentials"){
			input("username", "email", title: "Username", description: "MyQ Username (email address)")
			input("password", "password", title: "Password", description: "MyQ password")
		}
		section("Gateway Brand"){
			input(name: "brand", title: "Gateway Brand", type: "enum",  metadata:[values:["Liftmaster","Chamberlain","Craftsman"]] )
		}
		section("Advanced Options"){
			input(name: "polling", title: "Server Polling (in Minutes)", type: "int", description: "in minutes", defaultValue: "5" )
			input(name: "contactSensorTrigger", title: "Contact Sensor to trigger refresh ", type: "capability.contactSensor", required: "false", multiple: "true")
			input(name: "accelerationSensorTrigger", title: "Acceleration Sensor to trigger refresh ", type: "capability.accelerationSensor", required: "false", multiple: "true")
		}
	}
}

def prefListDevices() {
	if (forceLogin()) {
		def doorList = getDoorList()
		def lightList = getLightList()
		if ((doorList) || (lightList)) {
			return dynamicPage(name: "prefListDevices",  title: "Devices", install:true, uninstall:true) {
				if (doorList) {
					section("Select which garage door/gate to use"){
						input(name: "doors", type: "enum", required:false, multiple:true, metadata:[values:doorList])
					}
				} 
				if (lightList) {
					section("Select which light controller to use"){
						input(name: "lights", type: "enum", required:false, multiple:true, metadata:[values:lightList])
					}
				} 
			}
		} else {
			def devList = getDeviceList()
			return dynamicPage(name: "prefListDevices",  title: "Error!", install:true, uninstall:true) {
				section(""){
					paragraph "Could not find any supported device(s). Please report to author about these devices: " +  devList
				}
			}
		}  
	} else {
		return dynamicPage(name: "prefListDevices",  title: "Error!", install:false, uninstall:true) {
			section(""){
				paragraph "The username or password you entered is incorrect. Try again. " 
			}
		}  
	}
}

/* Initialization */
def installed() { initialize() }

def updated() { 
	unsubscribe()
	initialize() 
}

def uninstalled() {
	unsubscribe()
	unschedule()
	getAllChildDevices().each { deleteChildDevice(it.deviceNetworkId) }
}	

def initialize() {    
	login()
    
	// Get initial device status in state.data
	state.polling = [ last: 0, rescheduler: now() ]
	state.data = [:]
    
	// Create selected devices
	def doorsList = getDoorList()
	def lightsList = getLightList()
	def selectedDevices = [] + getSelectedDevices("doors") + getSelectedDevices("lights")
  
	selectedDevices.each { 
		if (!getChildDevice(it)) {
			if (it.contains("GarageDoorOpener")) { addChildDevice("copy-ninja", "MyQ Garage Door Opener", it, null, ["name": "MyQ: " + doorsList[it]]) }
			if (it.contains("LightController"))  { addChildDevice("copy-ninja", "MyQ Light Controller", it, null, ["name": "MyQ: " + lightsList[it]]) }
		} 
	}

	// Remove unselected devices
	def deleteDevices = (selectedDevices) ? (getChildDevices().findAll { !selectedDevices.contains(it.deviceNetworkId) }) : getAllChildDevices()
	deleteDevices.each { deleteChildDevice(it.deviceNetworkId) }

	
	//Subscribes to sunrise and sunset event to trigger refreshes
	subscribe(location, "sunrise", runRefresh)
	subscribe(location, "sunset", runRefresh)
	subscribe(location, "mode", runRefresh)
	subscribe(location, "sunriseTime", runRefresh)
	subscribe(location, "sunsetTime", runRefresh)
    
	//Subscribe to events from contact sensor
	if (settings.contactSensorTrigger) {
		subscribe(settings.contactSensorTrigger, "contact", runRefresh)
	}
	
	//Subscribe to events from contact sensor
	if (settings.threeAxisSensorTrigger) {
		subscribe(settings.accelerationSensorTrigger, "acceleration", runRefresh)
	}
    
	// Run refresh after installation
	runRefresh()
}

def getSelectedDevices( settingsName ) { 
	def selectedDevices = [] 
	(!settings.get(settingsName))?:((settings.get(settingsName)?.getAt(0)?.size() > 1)  ? settings.get(settingsName)?.each { selectedDevices.add(it) } : selectedDevices.add(settings.get(settingsName))) 
	return selectedDevices 
} 

/* Access Management */
private forceLogin() {
	//Reset token and expiry
	state.session = [ brandID: 0, brandName: settings.brand, securityToken: null, expiration: 0 ]
	state.polling = [ last: 0, rescheduler: now() ]
	state.data = [:]
	return doLogin()
}

private login() { return (!(state.session.expiration > now())) ? doLogin() : true }

private doLogin() { 
	apiGet("/api/user/validate", [username: settings.username, password: settings.password] ) { response ->
		if (response.status == 200) {
			if (response.data.SecurityToken != null) {
				state.session.brandID = response.data.BrandId
				state.session.brandName = response.data.BrandName
				state.session.securityToken = response.data.SecurityToken
				state.session.expiration = now() + 150000
				return true
			} else {
				return false
			}
		} else {
			return false
		}
	} 	
}

// Listing all the garage doors you have in MyQ
private getDoorList() { 	    
	def deviceList = [:]
	apiGet("/api/v4/userdevicedetails/get", []) { response ->
		if (response.status == 200) {
			response.data.Devices.each { device ->
				// 2 = garage door, 5 = gate, 7 = MyQGarage(no gateway), 17 = Garage Door Opener WGDO
				if (device.MyQDeviceTypeId == 2||device.MyQDeviceTypeId == 5||device.MyQDeviceTypeId == 7||device.MyQDeviceTypeId == 17) {
					def dni = [ app.id, "GarageDoorOpener", device.MyQDeviceId ].join('|')
					device.Attributes.each { 
						if (it.AttributeDisplayName=="desc")	deviceList[dni] = it.Value
						if (it.AttributeDisplayName=="doorstate") { 
							state.data[dni] = [ status: it.Value, lastAction: it.UpdatedTime ]
						}
					}                    
				}
			}
		}
	}    
	return deviceList
}

// Listing all the light controller you have in MyQ
private getLightList() { 	    
	def deviceList = [:]
	apiGet("/api/v4/userdevicedetails/get", []) { response ->
		if (response.status == 200) {
			response.data.Devices.each { device ->
				if (device.MyQDeviceTypeId == 3) {
					def dni = [ app.id, "LightController", device.MyQDeviceId ].join('|')
					device.Attributes.each { 
						if (it.AttributeDisplayName=="desc") { deviceList[dni] = it.Value }
						if (it.AttributeDisplayName=="lightstate") {  state.data[dni] = [ status: it.Value ] }
					}                    
				}
			}
		}
	}    
	return deviceList
}

private getDeviceList() { 	    
	def deviceList = []
	apiGet("/api/v4/userdevicedetails/get", []) { response ->
		if (response.status == 200) {
			response.data.Devices.each { device ->
				log.debug "MyQDeviceTypeId : " + device.MyQDeviceTypeId.toString()
				if (!(device.MyQDeviceTypeId == 1||device.MyQDeviceTypeId == 2||device.MyQDeviceTypeId == 3||device.MyQDeviceTypeId == 5||device.MyQDeviceTypeId == 7)) {
					deviceList.add( device.MyQDeviceTypeId.toString() + "|" + device.TypeID )
				}
			}
		}
	}    
	return deviceList
}

/* api connection */
// get URL 
private getApiURL() {
	if (settings.brand == "Craftsman") {
		return "https://craftexternal.myqdevice.com"
	} else {
		return "https://myqexternal.myqdevice.com"
	}
}

private getApiAppID() {
	if (settings.brand == "Craftsman") {
		return "QH5AzY8MurrilYsbcG1f6eMTffMCm3cIEyZaSdK/TD/8SvlKAWUAmodIqa5VqVAs"
	} else {
		return "JVM/G9Nwih5BwKgNCjLxiFUQxQijAebyyg8QUHr7JOrP+tuPb8iHfRHKwTmDzHOu"
	}
}
	
// HTTP GET call
private apiGet(apiPath, apiQuery = [], callback = {}) {	
	// set up query
	apiQuery = [ appId: getApiAppID() ] + apiQuery
	if (state.session.securityToken) { apiQuery = apiQuery + [SecurityToken: state.session.securityToken ] }
       
	try {
		httpGet([ uri: getApiURL(), path: apiPath, query: apiQuery ]) { response -> callback(response) }
	}	catch (Error e)	{
		log.debug "API Error: $e"
	}
}

// HTTP PUT call
private apiPut(apiPath, apiBody = [], callback = {}) {    
	// set up body
	apiBody = [ ApplicationId: getApiAppID() ] + apiBody
	if (state.session.securityToken) { apiBody = apiBody + [SecurityToken: state.session.securityToken ] }
    
	// set up query
	def apiQuery = [ appId: getApiAppID() ]
	if (state.session.securityToken) { apiQuery = apiQuery + [SecurityToken: state.session.securityToken ] }
    
	try {
		httpPut([ uri: getApiURL(), path: apiPath, contentType: "application/json; charset=utf-8", body: apiBody, query: apiQuery ]) { response -> callback(response) }
	} catch (Error e)	{
		log.debug "API Error: $e"
	}
}

// Updates data for devices
private updateDeviceData() {    
	// automatically checks if the token has expired, if so login again
	if (login()) {       
		// set polling states
		state.polling["last"] = now()
	
		// Get all the door information, updated to state.data
		return (getDoorList()||getLightList())? true : false
	} else {
		return false
	}
}

/* for SmartDevice to call */
// Refresh data
def refresh() {   
	//force devices to poll to get the latest status
	if (updateDeviceData()) { 
		log.info "Refreshing data..."
		// get all the children and send updates
		getAllChildDevices().each { 
			//log.debug "Polling " + it.deviceNetworkId
			it.updateDeviceStatus(state.data[it.deviceNetworkId].status)
			if (it.deviceNetworkId.contains("GarageDoorOpener")) {
				it.updateDeviceLastActivity(state.data[it.deviceNetworkId].lastAction.toLong())
			}
		}
	}    
	
	//schedule the rescheduler to schedule refresh ;)
	if ((state.polling["rescheduler"]?:0) + 2400000 < now()) {
		log.info "Scheduling Auto Rescheduler.."
		runEvery30Minutes(runRefresh)
		state.polling["rescheduler"] = now()
	}
}

// Get Device ID
def getChildDeviceID(child) {
	return child.device.deviceNetworkId.split("\\|")[2]
}

// Get single device status
def getDeviceStatus(child) {
	return state.data[child.device.deviceNetworkId].status
}

// Get single device last activity
def getDeviceLastActivity(child) {
	return state.data[child.device.deviceNetworkId].lastAction.toLong()
}

// Send command to start or stop
def sendCommand(child, attributeName, attributeValue) {
	if (login()) {	    	
		//Send command
		apiPut("/api/v4/deviceattribute/putdeviceattribute", [ MyQDeviceId: getChildDeviceID(child), AttributeName: attributeName, AttributeValue: attributeValue ]) 	
		return true
	} 
}

def runRefresh(evt) {
	if (evt) { 
		log.info "Event " + evt.displayName + " triggered refresh" 
		runIn(30, delayedRefresh) //schedule a refresh 
	}
	log.info "Last refresh was "  + ((now() - state.polling["last"])/60000) + " minutes ago"
	// Reschedule if  didn't update for more than 5 minutes plus specified polling
	if ((((state.polling["last"]?:0) + (((settings.polling.toInteger() > 0 )? settings.polling.toInteger() : 1) * 60000) + 300000) < now()) && canSchedule()) {
		log.info "Scheduling Auto Refresh.."
		schedule("* */" + ((settings.polling.toInteger() > 0 )? settings.polling.toInteger() : 1) + " * * * ?", refresh)
	}
	
	// Force Refresh NOWWW!!!!
	refresh()
	
	if (!evt)  state.polling["rescheduler"] = now() //Update rescheduler's last run
}

def delayedRefresh() { 
	log.info "Delayed refresh triggered"
	refresh() 
}