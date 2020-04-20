/**
 *  JSON Complete API
 *
 *  Copyright 2016 Paul Lovelace
 *
 */
definition(
    name: "JSON Complete API",
    namespace: "pdlove",
    author: "Paul Lovelace",
    description: "API for JSON with complete set of devices",
    category: "SmartThings Labs",
    iconUrl:   "https://dl.dropboxusercontent.com/s/7gy9a43mqhwf2xr/json_icon%401x.png",
    iconX2Url: "https://dl.dropboxusercontent.com/s/nivk5n45yzz9c65/json_icon%402x.png",
    iconX3Url: "https://dl.dropboxusercontent.com/s/y1q39zx4enki7wl/json_icon%403x.png",
    oauth: true)


preferences {
    page(name: "copyConfig")
}

//When adding device groups, need to add here
def copyConfig() {
    if (!state.accessToken) {
        createAccessToken()
    }
    dynamicPage(name: "copyConfig", title: "Config", install:true, uninstall:true) {
        section("Select devices to include in the /devices API call") {
            paragraph "Version 0.4.1"
            input "deviceList", "capability.refresh", title: "Most Devices", multiple: true, required: false
            input "sensorList", "capability.sensor", title: "Sensor Devices", multiple: true, required: false
            input "switchList", "capability.switch", title: "All Switches", multiple: true, required: false
            paragraph "Devices Selected: ${deviceList ? deviceList?.size() : 0}\nSensors Selected: ${sensorList ? sensorList?.size() : 0}\nSwitches Selected: ${switchList ? switchList?.size() : 0}"
        }
        section() {
            paragraph "View this SmartApp's configuration to use it in other places."
            href url:"${apiServerUrl("/api/smartapps/installations/${app.id}/config?access_token=${state.accessToken}")}", style:"embedded", required:false, title:"Config", description:"Tap, select, copy, then click \"Done\""
        }

        section() {
        	paragraph "View the JSON generated from the installed devices."
            href url:"${apiServerUrl("/api/smartapps/installations/${app.id}/devices?access_token=${state.accessToken}")}", style:"embedded", required:false, title:"Device Results", description:"View accessories JSON"
        }
        section() {
        	paragraph "Enter the name you would like shown in the smart app list"
        	label title:"SmartApp Label (optional)", required: false 
        }
    }
}

def renderDevices() {
    def deviceData = []
        deviceList.each { 
        	deviceData << [name: it.displayName, deviceid: it.id, capabilities: deviceCapabilityList(it), commands: deviceCommandList(it), attributes: deviceAttributeList(it)]
	}    
        sensorList.each  { 
        	deviceData << [name: it.displayName, deviceid: it.id, capabilities: deviceCapabilityList(it), commands: deviceCommandList(it), attributes: deviceAttributeList(it)]
	}
        switchList.each  { 
        	deviceData << [name: it.displayName, deviceid: it.id, capabilities: deviceCapabilityList(it), commands: deviceCommandList(it), attributes: deviceAttributeList(it)]
	}
    return deviceData
}

def findDevice(paramid) {
	def device = deviceList.find { it.id == paramid }
  	if (device) return device
	device = sensorList.find { it.id == paramid }
	if (device) return device
  	device = switchList.find { it.id == paramid }

	return device
 }
//No more individual device group definitions after here.


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
	if(!state.accessToken) {
         createAccessToken()
    }
    registerAll()
	state.subscriptionRenewed = 0
}

def authError() {
    [error: "Permission denied"]
}

def renderConfig() {
    def configJson = new groovy.json.JsonOutput().toJson([
        description: "JSON API",
        platforms: [
            [
                platform: "SmartThings",
                name: "SmartThings",
                app_url: apiServerUrl("/api/smartapps/installations/"),
                app_id: app.id,
                access_token:  state.accessToken
            ]
        ],
    ])

    def configString = new groovy.json.JsonOutput().prettyPrint(configJson)
    render contentType: "text/plain", data: configString
}

def renderLocation() {
  	[
    	latitude: location.latitude,
    	longitude: location.longitude,
    	mode: location.mode,
    	name: location.name,
    	temperature_scale: location.temperatureScale,
    	zip_code: location.zipCode
  	]
}

def CommandReply(statusOut, messageOut) {
	def replyData =
    	[
        	status: statusOut,
            message: messageOut
        ]

    def replyJson    = new groovy.json.JsonOutput().toJson(replyData)
    render contentType: "application/json", data: replyJson
}

def deviceCommand() {
	log.info("Command Request")
	def device = findDevice(params.id)    
    def command = params.command
    
  	if (!device) {
		log.error("Device Not Found")
      	CommandReply("Failure", "Device Not Found")
  	} else if (!device.hasCommand(command)) {
      	log.error("Device "+device.displayName+" does not have the command "+command)
      	CommandReply("Failure", "Device "+device.displayName+" does not have the command "+command)
  	} else {
      	def value1 = request.JSON?.value1
      	def value2 = request.JSON?.value2
      	try {
      		if (value2) {
	       		device."$command"(value1,value2)
	    	} else if (value1) {
	    		device."$command"(value1)
	    	} else {
	    		device."$command"()
	    	}
        	log.info("Command Successful for Device "+device.displayName+", Command "+command)
        	CommandReply("Success", "Device "+device.displayName+", Command "+command)
      	} catch (e) {
      		log.error("Error Occurred For Device "+device.displayName+", Command "+command)
 	    	CommandReply("Failure", "Error Occurred For Device "+device.displayName+", Command "+command)
      	}
  	}
}

def deviceAttribute() {
	def device = findDevice(params.id)    
    def attribute = params.attribute
  	if (!device) {
    	httpError(404, "Device not found")
  	} else {
      	def currentValue = device.currentValue(attribute)
      	[currentValue: currentValue]
  	}
}

def deviceQuery() {
	def device = findDevice(params.id)    
    if (!device) { 
    	device = null
        httpError(404, "Device not found")
    } 
    
    if (result) {
    	def jsonData =
        	[
         		name: device.displayName,
            	deviceid: device.id,
            	capabilities: deviceCapabilityList(device),
            	commands: deviceCommandList(device),
            	attributes: deviceAttributeList(device)
         	]
    	def resultJson = new groovy.json.JsonOutput().toJson(jsonData)
    	render contentType: "application/json", data: resultJson
    }
}

def deviceCapabilityList(device) {
  	def i=0
  	device.capabilities.collectEntries { capability->
    	[
      		(capability.name):1
    	]
  	}
}

def deviceCommandList(device) {
  	def i=0
  	device.supportedCommands.collectEntries { command->
    	[
      		(command.name): (command.arguments)
    	]
  	}
}

def deviceAttributeList(device) {
  	device.supportedAttributes.collectEntries { attribute->
    	try {
      		[
        		(attribute.name): device.currentValue(attribute.name)
      		]
    	} catch(e) {
      		[
        		(attribute.name): null
      		]
    	}
  	}
}

def getAllData() {
	//Since we're about to send all of the data, we'll count this as a subscription renewal and clear out pending changes.
	state.subscriptionRenewed = now()
    state.devchanges = []


	def deviceData =
    [	location: renderLocation(),
        deviceList: renderDevices() ]
    def deviceJson = new groovy.json.JsonOutput().toJson(deviceData)
    render contentType: "application/json", data: deviceJson
}

def startSubscription() {
//This simply registers the subscription.
    state.subscriptionRenewed = now()
	def deviceJson = new groovy.json.JsonOutput().toJson([status: "Success"])
    render contentType: "application/json", data: deviceJson    
}

def endSubscription() {
//Because it takes too long to register for an api command, we don't actually unregister.
//We simply blank the devchanges and change the subscription renewal to two hours ago.
	state.devchanges = []
    state.subscriptionRenewed = 0
 	def deviceJson = new groovy.json.JsonOutput().toJson([status: "Success"])
    render contentType: "application/json", data: deviceJson     
}

def registerAll() {
//This has to be done at startup because it takes too long for a normal command.
	log.debug "Registering All Events"
    state.devchanges = []
	registerChangeHandler(deviceList)
	registerChangeHandler(sensorList)
	registerChangeHandler(switchList)
}

def registerChangeHandler(myList) {
	myList.each { myDevice ->
		def theAtts = myDevice.supportedAttributes
		theAtts.each {att ->
		    subscribe(myDevice, att.name, changeHandler)
    	log.debug "Registering ${myDevice.displayName}.${att.name}"
		}
	}
}

def changeHandler(evt) {
	//Only add to the state's devchanges if the endpoint has renewed in the last 10 minutes.
    if (state.subscriptionRenewed>(now()-(1000*60*10))) {
  		if (evt.isStateChange()) {
			state.devchanges << [device: evt.deviceId, attribute: evt.name, value: evt.value, date: evt.date]
      }
    } else if (state.subscriptionRenewed>0) { //Otherwise, clear it
    	log.debug "Endpoint Subscription Expired. No longer storing changes for devices."
        state.devchanges=[]
        state.subscriptionRenewed=0
    }
}

def getChangeEvents() {
    //Store the changes so we can swap it out very quickly and eliminate the possibility of losing any.
    //This is mainly to make this thread safe because I'm willing to bet that a change event can fire
    //while generating/sending the JSON.
    def oldchanges = state.devchanges
    state.devchanges=[]
    state.subscriptionRenewed = now()
	if (oldchanges.size()==0) {
        def deviceJson = new groovy.json.JsonOutput().toJson([status: "None"])
	    render contentType: "application/json", data: deviceJson    
    } else {
    	def changeJson = new groovy.json.JsonOutput().toJson([status: "Success", attributes:oldchanges])
    	render contentType: "application/json", data: changeJson
	}
}

mappings {
    if (!params.access_token || (params.access_token && params.access_token != state.accessToken)) {
        path("/devices")                        { action: [GET: "authError"] }
        path("/config")                         { action: [GET: "authError"] }
        path("/location")                       { action: [GET: "authError"] }
        path("/:id/command/:command")     		{ action: [POST: "authError"] }
        path("/:id/query")						{ action: [GET: "authError"] }
        path("/:id/attribute/:attribute") 		{ action: [GET: "authError"] }
        path("/subscribe")                      { action: [GET: "authError"] }
        path("/getUpdates")                     { action: [GET: "authError"] }
        path("/unsubscribe")                      { action: [GET: "authError"] }
    } else {
        path("/devices")                        { action: [GET: "getAllData"] }
        path("/config")                         { action: [GET: "renderConfig"]  }
        path("/location")                       { action: [GET: "renderLocation"] }
        path("/:id/command/:command")     		{ action: [POST: "deviceCommand"] }
        path("/:id/query")						{ action: [GET: "deviceQuery"] }
        path("/:id/attribute/:attribute") 		{ action: [GET: "deviceAttribute"] }
        path("/subscribe")                      { action: [GET: "startSubscription"] }
        path("/getUpdates")                     { action: [GET: "getChangeEvents"] }
        path("/unsubscribe")                      { action: [GET: "endSubscription"] }
    }
}