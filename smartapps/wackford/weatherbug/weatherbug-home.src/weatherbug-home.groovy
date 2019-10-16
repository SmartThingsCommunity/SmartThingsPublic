/**
 *  WeatherBug Home
 *
 *  Copyright 2015 WeatherBug
 *
 */
definition(
    name: "WeatherBug Home",
    namespace: "WeatherBug",
    author: "WeatherBug Home",
    description: "WeatherBug Home",
    category: "My Apps",
    iconUrl: "http://stg.static.myenergy.enqa.co/apps/wbhc/v2/images/weatherbughomemedium.png",
    iconX2Url: "http://stg.static.myenergy.enqa.co/apps/wbhc/v2/images/weatherbughomemedium.png",
    iconX3Url: "http://stg.static.myenergy.enqa.co/apps/wbhc/v2/images/weatherbughome.png",
    oauth: [displayName: "WeatherBug Home", displayLink: "http://weatherbughome.com/"])


preferences {
        section("Select thermostats") {
        input "thermostatDevice", "capability.thermostat", multiple: true
    }
}

mappings {
  path("/appInfo") {    action: [      GET: "getAppInfo"    ]  }
  path("/getLocation") {    action: [      GET: "getLoc"    ]  }
  path("/currentReport/:id") {    action: [      GET: "getCurrentReport"    ]  }
  path("/setTemp/:temp/:id") {    action: [      POST: "setTemperature", GET: "setTemperature"    ]  }
}

/**
 * This API call will be leveraged by a WeatherBug Home Service to retrieve
 * data from the installed SmartApp, including the location data, and 
 * a list of the devices that were authorized to be accessed.  The WeatherBug
 * Home Service will leverage this data to represent the connected devices as well as their
 * location and associated the data with a WeatherBug user account.
 * Privacy Policy: http://weatherbughome.com/privacy/
 * @return Location, including id, latitude, longitude, zip code, and name, and the list of devices
 */
def getAppInfo() {
   def devices = thermostatDevice
   def lat = location.latitude
   def lon = location.longitude
   if(!(devices instanceof Collection))
   {
     devices = [devices]
   }
   return [
   			Id: UUID.randomUUID().toString(),
            Code: 200,
            ErrorMessage: null,
   			Result: [ "Devices": devices, 
   			"Location":[
            	"Id": location.id, 
            	"Latitude":lat,
            	"Longitude":lon,
            	"ZipCode":location.zipCode,
            	"Name":location.name
                ]
            ]
         ]
}

/**
 * This API call will be leveraged by a WeatherBug Home Service to retrieve
 * location data from the installed SmartApp.  The WeatherBug
 * Home Service will leverage this data to associate the location to a WeatherBug Home account
 * Privacy Policy: http://weatherbughome.com/privacy/
 *
 * @return Location, including id, latitude, longitude, zip code, and name
 */
def getLoc() {
   return [
   		Id: UUID.randomUUID().toString(),
        Code: 200,
        ErrorMessage: null,
        Result: [
        "Id": location.id, 
        "Latitude":location.latitude,
        "Longitude":location.longitude,
        "ZipCode":location.zipCode,
        "Name":location.name]
    ]
}

/**
 * This API call will be leveraged by a WeatherBug Home Service to retrieve
 * thermostat data and store it for display to a WeatherBug user.
 * Privacy Policy: http://weatherbughome.com/privacy/
 *
 * @param id The id of the device to get data for
 * @return Thermostat data including temperature, set points, running modes, and operating states
 */
def getCurrentReport() {
	log.debug "device id parameter=" + params.id
    def unixTime = (int)((new Date().getTime() / 1000))
    def device = thermostatDevice.find{ it.id == params.id}
    
    if(device == null)
    {
    	return [
        	Id: UUID.randomUUID().toString(),
            Code: 404,
            ErrorMessage: "Device not found. id=" + params.id,
            Result: null
        ]
    }
    return [
        	Id: UUID.randomUUID().toString(),
            Code: 200,
            ErrorMessage: null,
            Result: [
                DeviceId: device.id, 
                LocationId: location.id, 
                ReportType: 2, 
                ReportList: [ 
                    [Key: "Temperature", Value: GetOrDefault(device, "temperature")], 
                	[Key: "ThermostatSetpoint", Value: GetOrDefault(device, "thermostatSetpoint")],
                    [Key: "CoolingSetpoint", Value: GetOrDefault(device, "coolingSetpoint")],
                    [Key: "HeatingSetpoint", Value: GetOrDefault(device, "heatingSetpoint")],
                    [Key: "ThermostatMode", Value: GetOrDefault(device, "thermostatMode")],
                    [Key: "ThermostatFanMode", Value: GetOrDefault(device, "thermostatFanMode")],
                    [Key: "ThermostatOperatingState", Value: GetOrDefault(device, "thermostatOperatingState")]
                ],
                UnixTime: unixTime
            ]
        ]
}

/**
 * This API call will be leveraged by a WeatherBug Home Service to set
 * the thermostat setpoint.
 * Privacy Policy: http://weatherbughome.com/privacy/
 *
 * @param id The id of the device to set
 * @return Indication of whether the operation succeeded or failed
 
def setTemperature() {
	log.debug "device id parameter=" + params.id
	def device = thermostatDevice.find{ it.id == params.id}
    if(device != null)
    {
        def mode = device.latestState('thermostatMode').stringValue
        def value = params.temp as Integer
        log.trace "Suggested temperature: $value, $mode"
        if ( mode == "cool")
            device.setCoolingSetpoint(value)
        else if ( mode == "heat")
            device.setHeatingSetpoint(value)           
        return [
        	Id: UUID.randomUUID().toString(),
            Code: 200,
            ErrorMessage: null,
            Result: null
        ]
    }
    return [
        	Id: UUID.randomUUID().toString(),
            Code : 404,
            ErrorMessage: "Device not found. id=" + params.id,
            Result: null
        ]
}
*/


def installed() {
	log.debug "Installed with settings: ${settings}"
	initialize()
}

/**
 * The updated event will be pushed to a WeatherBug Home Service to notify the system to take appropriate action.
 * Data that will be sent includes the list of devices, and location data
 * Privacy Policy: http://weatherbughome.com/privacy/
 */
def updated() {
	log.debug "Updated with settings: ${settings}"
    log.debug "Updated with state: ${state}"
	log.debug "Updated with location ${location} ${location.id} ${location.name}"
    unsubscribe()
    initialize()
	def postParams = [
		uri: 'https://smartthingsrec.api.earthnetworks.com/api/v1/receive/smartapp/update',
		body:  [ 
			"Devices": devices, 
			"Location":[
				"Id": location.id, 
				"Latitude":location.latitude,
				"Longitude":location.longitude,
				"ZipCode":location.zipCode,
				"Name":location.name
			]
	   ]
    ]
    sendToWeatherBug(postParams)
}

/*
* Subscribe to changes on the thermostat attributes
*/
def initialize() {
	log.trace "initialize enter"
    subscribe(thermostatDevice, "heatingSetpoint", pushLatest)
    subscribe(thermostatDevice, "coolingSetpoint", pushLatest)
    subscribe(thermostatDevice, "thermostatSetpoint", pushLatest)
    subscribe(thermostatDevice, "thermostatMode", pushLatest)
    subscribe(thermostatDevice, "thermostatFanMode", pushLatest)
    subscribe(thermostatDevice, "thermostatOperatingState", pushLatest)
    subscribe(thermostatDevice, "temperature", pushLatest)
}

/**
 * The uninstall event will be pushed to a WeatherBug Home Service to notify the system to take appropriate action.
 * Data that will be sent includes the list of devices, and location data
 * Privacy Policy: http://weatherbughome.com/privacy/
 */
def uninstalled() {
	log.trace "uninstall entered"
    def postParams = [
    	uri: 'https://smartthingsrec.api.earthnetworks.com/api/v1/receive/smartapp/delete',
        body:  [ 
        	"Devices": devices, 
            "Location":[
                "Id": location.id, 
                "Latitude":location.latitude,
                "Longitude":location.longitude,
                "ZipCode":location.zipCode,
                "Name":location.name
            ]
       ]
    ]
    sendToWeatherBug(postParams)
}

/**
 * This method will push the latest thermostat data to the WeatherBug Home Service so it can store
 * and display the data to the WeatherBug user.  Data pushed includes the thermostat data as well
 * as location id.
 * Privacy Policy: http://weatherbughome.com/privacy/
 */
def pushLatest(evt) {
	def unixTime = (int)((new Date().getTime() / 1000))
    def device = thermostatDevice.find{ it.id == evt.deviceId}
    def postParams = [
        uri: 'https://smartthingsrec.api.earthnetworks.com/api/v1/receive',
        body: [
        	DeviceId: evt.deviceId, 
            LocationId: location.id,
            ReportType: 2, 
            ReportList: [ 
        		[Key: "Temperature", Value: GetOrDefault(device, "temperature")], 
                [Key: "ThermostatSetpoint", Value: GetOrDefault(device, "thermostatSetpoint")],
        		[Key: "CoolingSetpoint", Value: GetOrDefault(device, "coolingSetpoint")],
        		[Key: "HeatingSetpoint", Value: GetOrDefault(device, "heatingSetpoint")],
                [Key: "ThermostatMode", Value: GetOrDefault(device, "thermostatMode")],
                [Key: "ThermostatFanMode", Value: GetOrDefault(device, "thermostatFanMode")],
                [Key: "ThermostatOperatingState", Value: GetOrDefault(device, "thermostatOperatingState")]
        	], 
            UnixTime: unixTime
        ]
    ]
    log.debug postParams
    sendToWeatherBug(postParams)
}

/*
* This method attempts to get the value of a device attribute, but if an error occurs null is returned
* @return The device attribute value, or null
*/
def GetOrDefault(device, attrib)
{
	def val
 	try{
    	val = device.latestValue(attrib)
    
    }catch(ex)
    {
        log.debug "Failed to get attribute " + attrib + " from device " + device
        val = null
    }
    return val
}

/*
* Convenience method that sends data to WeatherBug, logging any exceptions that may occur
* Privacy Policy: http://weatherbughome.com/privacy/
*/
def sendToWeatherBug(postParams)
{
 	try{
    	log.debug postParams
        httpPostJson(postParams) { resp ->
        	resp.headers.each {
           log.debug "${it.name} : ${it.value}"
        }
        log.debug "response contentType: ${resp.contentType}"
        log.debug "response data: ${resp.data}"
        }
        log.debug "Communication with WeatherBug succeeded";
    
    }catch(ex)
    {
        log.debug "Communication with WeatherBug failed.\n${ex}";
    }
}