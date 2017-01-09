/**
 *	MiHome (Connect)
 *
 *  Copyright 2016 Alex Lee Yuk Cheung
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
 *	VERSION HISTORY
 *
 *	09.01.2017: 2.0c - Added support for MiHome House Monitor
 *  12.12.2016:	2.0b - Null issues when a device has been forced removed.
 *  23.11.2016:	2.0 - Remove extra logging.
 *
 *	10.11.2016:	2.0 BETA Release 6 - Merge Light Switch and Adapter functionality into one device type.
 *	10.11.2016: 2.0 BETA Release 5.4 - Remove unecessary catch blocks for fixed executeAction() errors.
 *	10.11.2016: 2.0 BETA Release 5.3 - Suppress random executeAction() errors.
 *
 *	09.11.2016: 2.0 BETA Release 5.2 - 4 gang device detection fix.
 *	09.11.2016: 2.0 BETA Release 5.1 - Try and reduce chances of executeAction() errors.
 *	09.11.2016: 2.0 BETA Release 5 - Add 4 Gang Extension compatibility.
 *
 *	08.11.2016: 2.0 BETA Release 4 - Add Energy Monitor Device compatibility. Separate Adapter and Adapter Plus devices.
 *	08.11.2016: 2.0 BETA Release 3 - Add Motion Sensor Device compatibility. Detect standard MiHome adapters.
 *
 *	06.11.2016: 2.0 BETA Release 2 - Fix issue identifying MiHome adapters.
 *	06.11.2016:	2.0 BETA Release 1 - Enable MiHome Connect to manage other MiHome devices. Update framework to match other alyc100 connect apps.
 *
 *	31.01.2016: 1.0.4 - Move external icon references into Github
 *	31.01.2016: 1.0.3b - Added icons to MiHome device list.
 *	31.01.2016: 1.0.3 - Bug fix to refresh schedule job.
 *
 *	17.01.2016: 1.0.2 - Bug fix when device has been manually deleted.
 *
 *	10.01.2016: 1.0.1 - Improve messaging for connection process.
 *
 *  09.01.2016: 1.0 - Initial Release
 *
 */
definition(
		name: "MiHome (Connect)",
		namespace: "alyc100",
		author: "Alex Lee Yuk Cheung",
		description: "Connect your MiHome devices to SmartThings.",
		iconUrl: "https://raw.githubusercontent.com/alyc100/SmartThingsPublic/master/smartapps/alyc100/mihome-icon-89db7a9bfb5c8b066ffb4e50c8d68235.png",
		iconX2Url: "https://raw.githubusercontent.com/alyc100/SmartThingsPublic/master/smartapps/alyc100/mihome-icon-89db7a9bfb5c8b066ffb4e50c8d68235.png",
        singleInstance: true
) 

preferences {
	page(name:"firstPage", title:"MiHome Device Setup", content:"firstPage", install: true)
    page(name: "loginPAGE")
    page(name: "selectDevicePAGE")
}

def apiURL(path = '/') 			 { return "https://mihome4u.co.uk/api/v1${path}" }

def firstPage() {
	if (username == null || username == '' || password == null || password == '') {
		return dynamicPage(name: "firstPage", title: "", install: true, uninstall: true) {
			section {
    			headerSECTION()
                href("loginPAGE", title: null, description: authenticated() ? "Authenticated as " +username : "Tap to enter MiHome account crednentials", state: authenticated())
  			}
    	}
    }
    else
    {
        return dynamicPage(name: "firstPage", title: "", install: true, uninstall: true) {
			section {
            	headerSECTION()
                href("loginPAGE", title: null, description: authenticated() ? "Authenticated as " +username : "Tap to enter MiHome account crednentials", state: authenticated())
            }
            if (stateTokenPresent()) {           	
                section ("Choose your MiHome devices:") {
					href("selectDevicePAGE", title: null, description: devicesSelected() ? getDevicesSelectedString() : "Tap to select MiHome devices", state: devicesSelected())
        		}
            } else {
            	section {
            		paragraph "There was a problem connecting to MiHome. Check your user credentials and error logs in SmartThings web console.\n\n${state.loginerrors}"
           		}
           }
    	}
    }
}

def loginPAGE() {
	if (username == null || username == '' || password == null || password == '') {
		return dynamicPage(name: "loginPAGE", title: "Login", uninstall: false, install: false) {
    		section { headerSECTION() }
        	section { paragraph "Enter your MiHome account credentials below to enable SmartThings and MiHome integration." }
    		section {
    			input("username", "text", title: "Username", description: "Your MiHome username (usually an email address)", required: true)
				input("password", "password", title: "Password", description: "Your MiHome password", required: true, submitOnChange: true)
  			}   	
    	}
    }
    else {
    	getMiHomeAccessToken()
        dynamicPage(name: "loginPAGE", title: "Login", uninstall: false, install: false) {
    		section { headerSECTION() }
        	section { paragraph "Enter your MiHome account credentials below to enable SmartThings and MiHome integration." }
    		section("MiHome Credentials:") {
				input("username", "text", title: "Username", description: "Your MiHome username (usually an email address)", required: true)
				input("password", "password", title: "Password", description: "Your MiHome password", required: true, submitOnChange: true)	
			}    	
    	
    		if (stateTokenPresent()) {
        		section {
                	paragraph "You have successfully connected to MiHome. Click 'Done' to select your MiHome devices."
  				}
        	}
        	else {
        		section {
            		paragraph "There was a problem connecting to MiHome. Check your user credentials and error logs in SmartThings web console.\n\n${state.loginerrors}"
           		}
        	}
        }
    }
}

def selectDevicePAGE() {
	updateDevices()
	dynamicPage(name: "selectDevicePAGE", title: "Devices", uninstall: false, install: false) {
  	section { headerSECTION() }
    section("Select your devices:") {
			input "selectedETRVs", "enum", image: "https://raw.githubusercontent.com/alyc100/SmartThingsPublic/master/smartapps/alyc100/mihome4-01bc8a0e478b385df3248b55cc2df7ca.png", required:false, title:"Select MiHome eTRV Devices \n(${state.miETRVDevices.size() ?: 0} found)", multiple:true, options:state.miETRVDevices
			input "selectedLights", "enum", image: "https://raw.githubusercontent.com/alyc100/SmartThingsPublic/master/smartapps/alyc100/mihome3_switch.png", required:false, title:"Select MiHome Light Devices \n(${state.miLightDevices.size() ?: 0} found)", multiple:true, options:state.miLightDevices
            input "selectedAdapters", "enum", image: "https://raw.githubusercontent.com/alyc100/SmartThingsPublic/master/smartapps/alyc100/mihome-adapter.png", required:false, title:"Select MiHome Adapter Devices \n(${state.miAdapterDevices.size() ?: 0} found)", multiple:true, options:state.miAdapterDevices
            input "selectedAdapterPluses", "enum", image: "https://raw.githubusercontent.com/alyc100/SmartThingsPublic/master/smartapps/alyc100/mihome-monitor.png", required:false, title:"Select MiHome Adapter Plus Devices \n(${state.miAdapterPlusDevices.size() ?: 0} found)", multiple:true, options:state.miAdapterPlusDevices
            input "selected4GangExtensions", "enum", image: "https://raw.githubusercontent.com/alyc100/SmartThingsPublic/master/smartapps/alyc100/mihome-extension.png", required:false, title:"Select MiHome 4 Gang Extension Devices \n(${state.mi4GangExtensionDevices.size() ?: 0} found)", multiple:true, options:state.mi4GangExtensionDevices
            input "selectedMonitors", "enum", image: "https://raw.githubusercontent.com/alyc100/SmartThingsPublic/master/smartapps/alyc100/mihome5-adapter.png", required:false, title:"Select MiHome Monitor Devices \n(${state.miMonitorDevices.size() ?: 0} found)", multiple:true, options:state.miMonitorDevices
			input "selectedMotions", "enum", image: "https://raw.githubusercontent.com/alyc100/SmartThingsPublic/master/smartapps/alyc100/mihome-motion-sensor-ir.png", required:false, title:"Select MiHome Motion Sensors \n(${state.miMotionSensors.size() ?: 0} found)", multiple:true, options:state.miMotionSensors
    }
  }
}

def headerSECTION() {
	return paragraph (image: "https://raw.githubusercontent.com/alyc100/SmartThingsPublic/master/smartapps/alyc100/mihome-icon-89db7a9bfb5c8b066ffb4e50c8d68235.png",
                  "${textVersion()}")
}  

def stateTokenPresent() {
	return state.miHomeAccessToken != null && state.miHomeAccessToken != ''
}

def authenticated() {
	return (state.miHomeAccessToken != null && state.miHomeAccessToken != '') ? "complete" : null
}

def devicesSelected() {
	return (selectedETRVs || selectedLights || selectedAdapters || selectedAdapterPluses || selected4GangExtensions || selectedMonitors || selectedMotions) ? "complete" : null
}

def getDevicesSelectedString() {
	if (state.miETRVDevices == null || state.miLightDevices == null || state.miAdapterDevices == null || state.miAdapterPlusDevices == null || state.mi4GangExtensionDevices == null || state.miMonitorDevices == null || state.miMotionSensors == null) {
    	updateDevices()
  	}
    
	def listString = ""
	selectedETRVs.each { childDevice ->
    	if (state.miETRVDevices[childDevice] != null) listString += state.miETRVDevices[childDevice] + "\n"
  	}
  	selectedLights.each { childDevice ->
  		if (state.miLightDevices[childDevice] != null) listString += state.miLightDevices[childDevice] + "\n"
	}
	selectedAdapters.each { childDevice ->
		if (state.miAdapterDevices[childDevice] != null) listString += state.miAdapterDevices[childDevice] + "\n"
  	}
    selectedAdapterPluses.each { childDevice ->
		if (state.miAdapterPlusDevices[childDevice] != null) listString += state.miAdapterPlusDevices[childDevice] + "\n"
  	}
    selected4GangExtensions.each { childDevice ->
    	if (state.mi4GangExtensionDevices[childDevice] != null) listString += state.mi4GangExtensionDevices[childDevice] + "\n"
    }
    selectedMonitors.each { childDevice ->
    	if (state.miMonitorDevices[childDevice] != null) listString += state.miMonitorDevices[childDevice] + "\n"
    }
    selectedMotions.each { childDevice ->
    	if (state.miMotionSensors[childDevice] != null) listString += state.miMotionSensors[childDevice] + "\n"
    }
  	return listString
}

// App lifecycle hooks

def installed() {
	log.debug "installed"
	initialize()
	// Check for new devices and remove old ones every 3 hours
	runEvery3Hours('updateDevices')
    // execute refresh method every minute
    schedule("0 0/1 * * * ?", refreshDevices)
}

// called after settings are changed
def updated() {
	log.debug "updated"
	initialize()
    unschedule('refreshDevices')
    schedule("0 0/1 * * * ?", refreshDevices)
}

def uninstalled() {
	log.info("Uninstalling, removing child devices...")
	unschedule()
	removeChildDevices(getChildDevices())
}

private removeChildDevices(devices) {
	devices.each {
		deleteChildDevice(it.deviceNetworkId) // 'it' is default
	}
}

// called after Done is hit after selecting a Location
def initialize() {
	log.debug "initialize"
	if (selectedETRVs) {
		addETRV()
	}
	if (selectedLights) {
		addLight()
	}
    if (selectedAdapters) {
    	addAdapter()
    }
    if (selectedAdapterPluses) {
    	addAdapterPlus()
    }
    if (selected4GangExtensions) {
    	add4GangExtension()
    }
    if (selectedMonitors) {
    	addMonitor()
    }
    if (selectedMotions) {
    	addMotion()
    }
    
    def devices = getChildDevices()
	devices.each {
    	log.debug "Refreshing device $it.name"
        it.refresh()
	}
}


def updateDevices() {
	if (!state.devices) {
		state.devices = [:]
	}
	def devices = devicesList()
  	state.miETRVDevices = [:]
  	state.miLightDevices = [:]
    state.miAdapterDevices = [:]
    state.miAdapterPlusDevices = [:]
    state.mi4GangExtensionDevices = [:]
    state.miMotionSensors = [:]
    state.miMonitorDevices = [:]

    def selectors = []
	devices.each { device ->
        if (device.device_type == 'etrv') {
			log.debug "Identified: device ${device.id}: ${device.device_type}: ${device.label}: ${device.target_temperature}: ${device.last_temperature}: ${device.voltage}"
            selectors.add("${device.id}")
            def value = "${device.label} eTRV"
			def key = device.id
			state.miETRVDevices["${key}"] = value
            
            //Update names of devices with MiHome
     		def childDevice = getChildDevice("${device.id}")
     		if (childDevice) {
     			//Update name of device if different.
     			if(childDevice.name != device.label + " eTRV") {
					childDevice.name = device.label + " eTRV"
					log.debug "Device's name has changed."
				}
     		}
    	}
        else if (device.device_type == 'light') {
        	log.debug "Identified: device ${device.id}: ${device.device_type}: ${device.label}"
            selectors.add("${device.id}")
            def value = "${device.label} Light Switch"
			def key = device.id
			state.miLightDevices["${key}"] = value
            
            //Update names of devices with MiHome
     		def childDevice = getChildDevice("${device.id}")
     		if (childDevice) {
     			//Update name of device if different.
     			if(childDevice.name != device.label + " Light Switch") {
					childDevice.name = device.label + " Light Switch"
					log.debug "Device's name has changed."
				}
     		}
        }
        else if (device.device_type == 'legacy') {
        	log.debug "Identified: device ${device.id}: ${device.device_type}: ${device.label}"
            selectors.add("${device.id}")
            def value = "${device.label} Adapter"
			def key = device.id
			state.miAdapterDevices["${key}"] = value
            
            //Update names of devices with MiHome
     		def childDevice = getChildDevice("${device.id}")
     		if (childDevice) {
     			//Update name of device if different.
     			if(childDevice.name != device.label + " Adapter") {
					childDevice.name = device.label + " Adapter"
					log.debug "Device's name has changed."
				}
     		}
        }
       	else if (device.device_type == 'control') {
        	log.debug "Identified: device ${device.id}: ${device.device_type}: ${device.label}"
            selectors.add("${device.id}")
            def value = "${device.label} Adapter Plus"
			def key = device.id
			state.miAdapterPlusDevices["${key}"] = value
            
            //Update names of devices with MiHome
     		def childDevice = getChildDevice("${device.id}")
     		if (childDevice) {
     			//Update name of device if different.
     			if(childDevice.name != device.label + " Adapter Plus") {
					childDevice.name = device.label + " Adapter Plus"
					log.debug "Device's name has changed."
				}
     		}
        }
        else if (device.device_type == 'fourgang') {
        	log.debug "Identified: device ${device.id}: ${device.device_type}: ${device.label}"
            def value = "${device.label} 4 Gang Extension"
			def key = device.id
			state.mi4GangExtensionDevices["${key}"] = value
            
            //Update names of devices with MiHome
            0.upto(3, {
            	selectors.add("${device.id}/${it}")
   				def childDevice = getChildDevice("${device.id}/${it}")
                
                if (childDevice) {
     				//Update name of device if different.
     				if(childDevice.name != device.label + " 4 Gang Extension [Socket ${it + 1}]") {
						childDevice.name = device.label + " 4 Gang Extension [Socket ${it + 1}]"
						log.debug "Device's name has changed."
					}
     			}
            })
        }
        else if (device.device_type == 'monitor' || device.device_type == 'house') {
        	log.debug "Identified: device ${device.id}: ${device.device_type}: ${device.label}"
            selectors.add("${device.id}")
            def value = "${device.label} Monitor"
			def key = device.id
			state.miMonitorDevices["${key}"] = value
            
            //Update names of devices with MiHome
     		def childDevice = getChildDevice("${device.id}")
     		if (childDevice) {
     			//Update name of device if different.
     			if(childDevice.name != device.label + " Monitor") {
					childDevice.name = device.label + " Monitor"
					log.debug "Device's name has changed."
				}
     		}
        }
        else if (device.device_type == 'motion') {
        	log.debug "Identified: device ${device.id}: ${device.device_type}: ${device.label}"
            selectors.add("${device.id}")
            def value = "${device.label} Motion Sensor"
			def key = device.id
			state.miMotionSensors["${key}"] = value
            
            //Update names of devices with MiHome
     		def childDevice = getChildDevice("${device.id}")
     		if (childDevice) {
     			//Update name of device if different.
     			if(childDevice.name != device.label + " Motion Sensor") {
					childDevice.name = device.label + " Motion Sensor"
					log.debug "Device's name has changed."
				}
     		}
        }
    }
   	log.debug selectors
   	//Remove devices if does not exist on the MiHome platform
   	getChildDevices().findAll { !selectors.contains("${it.deviceNetworkId}") }.each {
		log.info("Deleting ${it.deviceNetworkId}")
        try {
			deleteChildDevice(it.deviceNetworkId)
        } catch (physicalgraph.exception.NotFoundException e) {
        	log.info("Could not find ${it.deviceNetworkId}. Assuming manually deleted.")
        } catch (physicalgraph.exception.ConflictException ce) {
        	log.info("Device ${it.deviceNetworkId} in use. Please manually delete.")
        }
	}  
}

def addETRV() {
	updateDevices()

	selectedETRVs.each { device ->

        def childDevice = getChildDevice("${device}")
        if (!childDevice && state.miETRVDevices[device] != null) {
    		log.info("Adding device ${device}: ${state.miETRVDevices[device]}")

        	def data = [
                	name: state.miETRVDevices[device],
					label: state.miETRVDevices[device]
				]
            childDevice = addChildDevice(app.namespace, "MiHome eTRV", "$device", null, data)

			log.debug "Created ${state.miETRVDevices[device]} with id: ${device}"
		} else {
			log.debug "found ${state.miETRVDevices[device]} with id ${device} already exists"
		}

	}
}

def addLight() {
	updateDevices()

	selectedLights.each { device ->

        def childDevice = getChildDevice("${device}")

        if (!childDevice && state.miLightDevices[device] != null) {
    		log.info("Adding device ${device}: ${state.miLightDevices[device]}")
            def data = [
                	name: state.miLightDevices[device],
					label: state.miLightDevices[device]
				]
            childDevice = addChildDevice(app.namespace, "MiHome Adapter", "$device", null, data)

			log.debug "Created ${state.miLightDevices[device]} with id: ${device}"
		} else {
			log.debug "found ${state.miLightDevices[device]} with id ${device} already exists"
		}

	}
}

def addAdapter() {
	updateDevices()

	selectedAdapters.each { device ->

        def childDevice = getChildDevice("${device}")

        if (!childDevice && state.miAdapterDevices[device] != null) {
    		log.info("Adding device ${device}: ${state.miAdapterDevices[device]}")

        	def data = [
                	name: state.miAdapterDevices[device],
					label: state.miAdapterDevices[device]
				]
            childDevice = addChildDevice(app.namespace, "MiHome Adapter", "$device", null, data)

			log.debug "Created ${state.miAdapterDevices[device]} with id: ${device}"
		} else {
			log.debug "found ${state.miAdapterDevices[device]} with id ${device} already exists"
		}

	}
}

def addAdapterPlus() {
	updateDevices()

	selectedAdapterPluses.each { device ->

        def childDevice = getChildDevice("${device}")

        if (!childDevice && state.miAdapterPlusDevices[device] != null) {
    		log.info("Adding device ${device}: ${state.miAdapterPlusDevices[device]}")

        	def data = [
                	name: state.miAdapterPlusDevices[device],
					label: state.miAdapterPlusDevices[device]
				]
            childDevice = addChildDevice(app.namespace, "MiHome Adapter Plus", "$device", null, data)

			log.debug "Created ${state.miAdapterPlusDevices[device]} with id: ${device}"
		} else {
			log.debug "found ${state.miAdapterPlusDevices[device]} with id ${device} already exists"
		}

	}
}

def add4GangExtension() {
	updateDevices()

	selected4GangExtensions.each { device ->
    	0.upto(3, {
            def childDevice = getChildDevice("${device}/${it}")
            
            if (!childDevice && state.mi4GangExtensionDevices[device] != null) {
    			log.info("Adding device ${device}/${it}: ${state.mi4GangExtensionDevices[device]} [Socket ${it + 1}]")

        		def data = [
                	name: "${state.mi4GangExtensionDevices[device]} [Socket ${it + 1}]",
					label: "${state.mi4GangExtensionDevices[device]} [Socket ${it + 1}]"
				]
            	childDevice = addChildDevice(app.namespace, "MiHome Adapter", "${device}/${it}", null, data)

				log.debug "Created ${state.mi4GangExtensionDevices[device]} [Socket ${it + 1}] with id: ${device}/${it}"
			} else {
				log.debug "found ${state.mi4GangExtensionDevices[device]} [Socket ${it + 1}] with id ${device}/${it} already exists"
			}
		})
	}
}

def addMonitor() {
	updateDevices()

	selectedMonitors.each { device ->

        def childDevice = getChildDevice("${device}")

        if (!childDevice && state.miMonitorDevices[device] != null) {
    		log.info("Adding device ${device}: ${state.miMonitorDevices[device]}")

        	def data = [
                	name: state.miMonitorDevices[device],
					label: state.miMonitorDevices[device]
				]
            childDevice = addChildDevice(app.namespace, "MiHome Monitor", "$device", null, data)\

			log.debug "Created ${state.miMonitorDevices[device]} with id: ${device}"
		} else {
			log.debug "found ${state.miMonitorDevices[device]} with id ${device} already exists"
		}

	}
}

def addMotion() {
	updateDevices()

	selectedMotions.each { device ->

        def childDevice = getChildDevice("${device}")

        if (!childDevice && state.miMotionSensors[device] != null) {
    		log.info("Adding device ${device}: ${state.miMotionSensors[device]}")

        	def data = [
                	name: state.miMotionSensors[device],
					label: state.miMotionSensors[device]
				]
            childDevice = addChildDevice(app.namespace, "MiHome Motion Sensor", "$device", null, data)

			log.debug "Created ${state.miMotionSensors[device]} with id: ${device}"
		} else {
			log.debug "found ${state.miMotionSensors[device]} with id ${device} already exists"
		}

	}
}

def refreshDevices() {
	log.info("Executing refreshDevices...")
    if (atomicState.refreshCounter == null || atomicState.refreshCounter >= 5) {
    	atomicState.refreshCounter = 0
    } else {
    	atomicState.refreshCounter = atomicState.refreshCounter + 1
    }
	getChildDevices().each { device ->
    	if (atomicState.refreshCounter == 5) {
        	log.info("Refreshing device ${device.name} ...")
            try {
    			device.refresh()
        	} catch (e) {
        		//WORKAROUND - Catch unexplained exception when refreshing devices.
        		logResponse(e.response)
        	}
        } else if (device.name.contains("Monitor") || device.name.contains("Motion Sensor") || device.name.contains("Adapter Plus")) {
        	log.info("Refreshing device ${device.name}...")
			device.refresh()
        }
	}
}

def devicesList() {
	logErrors([]) {
		def resp = apiGET("/subdevices/list")
		if (resp.status == 200) {
        	
			return resp.data.data
		} else {
			log.error("Non-200 from device list call. ${resp.status} ${resp.data}")
			return []
		}
	}
}

def getMiHomeAccessToken() {   
	def resp = apiGET("/users/profile")
    if (resp.status == 200) {
			state.miHomeAccessToken = resp.data.data.api_key
    		log.debug "miHomeAccessToken: $resp.data.data.api_key"  
		} else {
			log.error("Non-200 from device list call. ${resp.status} ${resp.data}")
			return []
		}
}

def apiGET(path) {
	try {   			
        httpGet(uri: apiURL(path), headers: apiRequestHeaders()) {response ->
			logResponse(response)
			return response
		}
	} catch (groovyx.net.http.HttpResponseException e) {
		logResponse(e.response)
		return e.response
	}
}

def apiPOST(path, body = [:]) {
	try {
		log.debug("Beginning API POST: ${path}, ${body}")
		httpGet(uri: apiURL(path), body: new groovy.json.JsonBuilder(body).toString(), headers: apiRequestHeaders() ) {response ->
			logResponse(response)
			return response
		}
	} catch (groovyx.net.http.HttpResponseException e) {
		logResponse(e.response)
		return e.response
	}
}

Map apiRequestHeaders() {
	def userpassascii = "${username}:${password}"
    if (state.miHomeAccessToken != null && state.miHomeAccessToken != '') {
    	userpassascii = "${username}:${state.miHomeAccessToken}"
    }
  	def userpass = "Basic " + userpassascii.encodeAsBase64().toString()
        
	return ["User-Agent": "SmartThings Integration",
            "Authorization": "$userpass"
	]
}

def logResponse(response) {
	log.info("Status: ${response.status}")
	//log.info("Body: ${response.data}")
}

def logErrors(options = [errorReturn: null, logObject: log], Closure c) {
	try {
		return c()
	} catch (groovyx.net.http.HttpResponseException e) {
		options.logObject.error("got error: ${e}, body: ${e.getResponse().getData()}")
		if (e.statusCode == 401) { // token is expired
			state.remove("miHomeAccessToken")
			options.logObject.warn "Access token is not valid"
		}
		return options.errorReturn
	} catch (java.net.SocketTimeoutException e) {
		options.logObject.warn "Connection timed out, not much we can do here"
		return options.errorReturn
	}
}

private def textVersion() {
    def text = "MiHome (Connect)\nVersion: 2.0c\nDate: 09012017(2240)"
}

private def textCopyright() {
    def text = "Copyright © 2017 Alex Lee Yuk Cheung"
}