import groovy.json.JsonBuilder
import java.security.MessageDigest
import java.math.BigInteger

definition(
    name: "NbPower Smart Home",
    namespace: "nbpowersmarthome",
    author: "BlueSpursDev",
    description: "Provides endpoints for NB Power Smart Home mobile app to control devices, create scenes and automations",
    category: "Convenience",
    iconUrl: "https://s3.ca-central-1.amazonaws.com/smart-home-nbp/smartthings/icons/nbpower_smart_icon@1x.png",
    iconX2Url: "https://s3.ca-central-1.amazonaws.com/smart-home-nbp/smartthings/icons/nbpower_smart_icon@2x.png",
    iconX3Url: "https://s3.ca-central-1.amazonaws.com/smart-home-nbp/smartthings/icons/nbpower_smart_icon@2x.png")
    {
        appSetting "BSC_callback_url"
        appSetting "BSC_credentials"
        appSetting "BSC_post_events"
        appSetting "log_level"
    }

//all API endpoints are defined here
mappings {
    // devices  
    path("/devices") 							{   action: [   GET: "listDevices"        														]}
    path("/devices/:id") 						{  	action: [   GET: "listDevices"        														]}
    path("/devices/commands")					{   action: [	POST: "sendDevicesCommands"         											]}
}

// our capabilities list
private def getCapabilities() {
    [   
        ["capability.sensor", "Sensor", "sensors", ["presence","motion","contact"]],
        ["capability.switch", "Switches/Lights", "switches", "switch"],
        ["capability.thermostat", "Thermostat", "thermostats", ["coolingSetpoint","heatingSetpoint","thermostatFanMode","thermostatMode","thermostatOperatingState","thermostatSetpoint","minHeatingSetpoint","maxHeatingSetpoint","heatingSetpointRangeLow","heatingSetpointRangeHigh"] ],
        ["capability.waterSensor", "Water Sensor", "waterSensors", "water"],
    ]  
}

// Approved Commands for device functions, if it's not in this list, it will not get called, regardless of what is sent.
private def getApprovedCommands() {
    ["on","off","toggle","setLevel","setColor","setHue","setSaturation","setColorTemperature","open","close","windowShade.open","windowShade.close","windowShade.presetPosition","lock","unlock","take","alarm.off","alarm.strobe","alarm.siren","alarm.both","thermostat.off","thermostat.heat","thermostat.cool","thermostat.auto","thermostat.emergencyHeat","thermostat.quickSetHeat","thermostat.quickSetCool","thermostat.setHeatingSetpoint","thermostat.setCoolingSetpoint","thermostat.setThermostatMode","fanOn","fanCirculate","fanAuto","setThermostatFanMode","play","pause","stop","nextTrack","previousTrack","mute","unmute","musicPlayer.setLevel","playText","playTextAndRestore","playTextAndResume","playTrack","playTrackAtVolume","playTrackAndRestore","playTrackAndResume","setTrack","setLocalLevel","resumeTrack","restoreTrack","speak","startActivity","getCurrentActivity","getAllActivities","push","beep","refresh","poll","low","med","high","left","right","up","down","home","presetOne","presetTwo","presetThree","presetFour","presetFive","presetSix","presetSeven","presetEight","presetCommand","startLoop","stopLoop","setLoopTime","setDirection","alert", "setAdjustedColor","allOn","allOff","deviceNotification", "setSchedule", "setTimeRemaining"]
}

// Map of commands and the data type expected to conform input values to.
private def getSecondaryType() {
    ["setLevel": Integer, "playText": String, "playTextAndResume": String, "playTextAndRestore": String, "playTrack" : String, "playTrackAndResume" : String, "playTrackAndRestore": String, "setColor": Map, "setHue": Integer, "setSaturation": Integer, "setColorTemperature": Integer, "startActivity": String, "restoreTrack" :String, "resumeTrack": String, "setTrack": String, "deviceNotification": String, "speak" : String, "setCoolingSetpoint": Integer, "setHeatingSetpoint": Float, "setSchedule": JSON, "setThermostatFanMode": String, "setThermostatMode": String, "setTimeRemaining": Integer ]
}

preferences {
    section("Allow Endpoint to Control These Things by Their Capabilities (You only need to choose one capability to get access to full device, however, selecting all capabilities will not create duplicate devices...") {
        for (cap in capabilities) {
            input cap[2], cap[0], title: "Select ${cap[1]} Devices", multiple:true, required: false
        }
    }

}

def installed() {
    initialize()
}

def updated() {
    unsubscribe()
    initialize()
}

// Called on installed or updated from mobile app or oauth flow. 
def initialize() {
    debug("Initialize called")
    //init updates state var if null
    if (!state.updates) state.updates = []
    //loop through our capabilities list and subscribe to all devices if capability has something to subscribe to and route to eventHandler
    for (cap in capabilities) {
        if(cap[3] != "") {
            if(settings[cap[2]]) {
            	//if single attribute
                if (cap[3] instanceof String) {
                	subscribe(settings[cap[2]], cap[3], eventHandler)
                    debug("Subscribing single '${cap[2]}' for '${cap[3]}'")
                } else { //assume a map of attributes
            		cap[3].each {
            			subscribe(settings[cap[2]], it, eventHandler)
                        debug("Subscribing multi '${cap[2]}' for '${it}'")
                	}	
                }
            }
        }
    }
}

/****************************
* Device API Commands
****************************/

/**
* Gets Subscribed Devices for location, if params.id is provided, get details for that device
*
* @param params.id is the device id
* @return renders json
*/
def listDevices() {
	debug("listDevices called")
    def id = params?.id
    // if there is an id parameter, list only that device. Otherwise list all devices in location
    if(id) {
        def device = findDevice(id)    
        render contentType: "text/json", data: new JsonBuilder(deviceItem(device, true)).toPrettyString()
    } else {
        def result = []
        result << allSubscribed.collect{deviceItem(it, true)}                
        render contentType: "text/json", data: new JsonBuilder(result[0]).toPrettyString()
    }
}

/**
* Executes Command for list of Device Ids for location
*
* @param params.ids is a list of the device ids
* @return renders json
*/
def sendDevicesCommands() {
	debug("sendDevicesCommands called")
    def group = request.JSON?.group
    def results = []
    group.each {
        def device = findDevice(it?.id) 
        if(device) {
            if(!(it.value instanceof Integer) && !it.value) {
                if (approvedCommands.contains(it.command)) {
                    debug("Sending command ${it.command} to Device id ${it.id}")
                    log.debug(it.command)
                    device."$it.command"()  
                    results << [ id : it.id, status : "success", command : it.command, state: deviceItem(device, true) ]
                }
            } else {                
                def commandType = secondaryType.find { i -> i.key == it.command.toString()}?.value
                debug(commandType)
                def secondary = it.value.asType(commandType)
                debug("Sending command ${it.command} to Device id ${it.id} with value ${it.value}")
                device."$it.command"(secondary)
                results << [ id : it.id, status : "success", command : it.command, value : it.value, state: deviceItem(device, true) ]
            }
        } else {
            results << [ id : it.id, status : "not found" ]
        }
    }
    render contentType: "text/json", data: new JsonBuilder(results).toPrettyString()
}

/**
* Get the updates from state variable and returns them
*
* @return renders json
*/
def updates() {
	debug("updates called")
    //render out json of all updates since last html loaded
    render contentType: "text/json", data: new JsonBuilder(state.updates).toPrettyString()
}

/**
* Handles the subscribed event and updates state variable
*
* @param evt is the event object
*/
def eventHandler(evt) {
    logField(evt) { it.toString() }
}

/****************************
* Private Methods
****************************/

/**
* WebHook API Call on Subscribed Change 
*
* @param evt is the event object, c is a Closure
*/
private logField(evt, Closure c) {
	debug("logField called")

    def event_id = "${evt.id}".toString()
    debug("The source of this event is ${evt.source} and it was $event_id")

    if (appSettings.BSC_post_events == '1') {

        MessageDigest digest = MessageDigest.getInstance("MD5")
        def secret_key = "$appSettings.BSC_credentials $event_id $evt.deviceId $evt.isoDate $evt.name - $evt.value"
        digest.update(secret_key.bytes)
        def event_md5 = new BigInteger(1, digest.digest()).toString(16).padLeft(32, '0')

        def params = [
            uri: appSettings.BSC_callback_url,
            body: [
                source: "smart_things", 
                event_id: event_id,
                location_id: evt.locationId, 
                device_id: evt.deviceId, 
                event_type: evt.name, 
                value: evt.value, 
                event_date: evt.isoDate, 
                units: evt.unit, 
                event_source: evt.source, 
                state_changed: evt.isStateChange()
            ],
            headers: [
                "Authorization": event_md5,
            ],
            contentType: "application/json; charset=utf-8"
        ]

        try {
            debug("Calling $params")

            httpPostJson(params) { 
                debug(evt.name+" Event data successfully posted")
            }
        } catch (e) {
            log.debug "something went wrong: $e"
        }
            
    }
}

/**
* Builds a map of all subscribed devices and returns a unique list of devices
*
* @return returns a unique list of devices
*/
private getAllSubscribed() {
	debug("getAllSubscribed called")
    def dev_list = []
    capabilities.each { 
        dev_list << settings[it[2]] 
    }
    return dev_list?.findAll()?.flatten().unique { it.id }
}

/**
* finds a device by id in subscribed capabilities
*
* @param id is a device uuid
* @return device object
*/
def findDevice(id) {
	debug("findDevice called")
    def device = null
    capabilities.find { 
        settings[it[2]].find { d ->
            if (d.id == id) {
                device = d
                return true
            }

        }
    }
    return device
}

/**
* Builds a map of device items
*
* @param device object and s true/false
* @return a map of device details
*/
private item(device, s) {
	debug("item called")
    device && s ? [device_id: device.id, 
                   label: device.displayName, 
                   name: s.name, value: s.value, 
                   date: s.date, stateChange: s.stateChange, 
                   eventSource: s.eventSource] : null
}

/**
* Builds a map of device details including attributes
*
* @param device is the device object, explodedView is true/false
* @return device details
*/
private deviceItem(device, explodedView) {
	debug("deviceItem called")
    if (!device) return null
    def results = [:]
    ["id", "name", "displayName"].each {
        results << [(it) : device."$it"]
    }

    if(explodedView) {
        def attrsAndVals = [:]
        device.supportedAttributes?.each {
        	def attribs = ["currentValue" : device.currentValue(it.name), "dataType" : it.dataType]
            
            if(it.values) {
                def vals = []
                it.values.each { v ->
                    vals << v
                }
                attribs << [ "values" : vals]
            }
            attrsAndVals[it.name] = attribs
        }
        results << ["attributes" : attrsAndVals]
        
        def caps = [:]
        device.capabilities?.each {
            caps[it.name] = [:] 
            def attribs = [:]
            it.attributes.each { i -> 
                def attrib = [:]
                attrib["dataType"] = i.dataType 
                if(i.values) {
                	def vals = []
                    i.values.each { v ->
                    	vals << v
                    }
                    attrib["values"] = vals
                }

                attribs[i.name] = attrib
            }
            caps[it.name]["attributes"] = attribs 

            def comms = [:]
            it.commands.each { i -> 
                i.each { c ->
                    def comname = c.getName()
                    comms[comname] = c.getArguments()
                }
            }
            caps[it.name]["commands"] = comms 
        }
        results << ["capabilities" : caps] 
        
        def cmds = []
        device.supportedCommands?.each {
            cmds << it.name
        }
        results << ["commands" : cmds] 
    }
    results
}

/**
* Builds a map of event details based on event
*
* @param evt is the event object
* @return a map of event details
*/
private eventJson(evt) {
	debug("eventJson called")
    def update = [:]
    update.id = evt.deviceId
    update.name = evt.name
    //find device by id
    def device = findDevice(evt.deviceId)
    def attrsAndVals = []
        device.supportedAttributes?.each {
        	def attribs = ["name" : (it.name), "currentValue" : device.currentValue(it.name), "dataType" : it.dataType]
            attrsAndVals << attribs
        }
    update.attributes =   attrsAndVals
    //update.value = evt.value
    update.name = evt.displayName
    update.date = evt.isoDate
    return update
}

/**
* Gets the weather feature based on location / zipcode
*
* @param feature is the weather parameter to get
* @return weather information
*/
private get(feature) {
	debug("get called")
    getWeatherFeature(feature, zipCode)
}

//Debug Router to log events if logging is turned on
def debug(evt) {
	if (appSettings.log_level == 'debug') {
    	log.debug evt
    }
}