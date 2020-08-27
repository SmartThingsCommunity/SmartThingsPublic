/**
 *  Parent_ST_Anything_Ethernet.groovy
 *
 *  Copyright 2017 Dan G Ogorchock 
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
 *  Change History:
 *
 *    Date        Who            What
 *    ----        ---            ----
 *    2017-02-08  Dan Ogorchock  Original Creation
 *    2017-02-12  Dan Ogorchock  Modified to work with Ethernet based devices instead of ThingShield
 *    2017-02-24  Dan Ogorchock  Created the new "Multiples" device handler as a new example
 *    2017-04-16  Dan Ogorchock  Updated to use the new Composite Device Handler feature
 *    2017-06-10  Dan Ogorchock  Added Dimmer Switch support
 *    2017-07-09  Dan Ogorchock  Added number of defined buttons tile
 *    2017-08-24  Allan (vseven) Change the way values are pushed to child devices to allow a event to be executed allowing future customization
 *    2007-09-24  Allan (vseven) Added RGB LED light support with a setColorRGB routine
 *    2017-10-07  Dan Ogorchock  Cleaned up formatting for readability
 *    2017-09-24  Allan (vseven) Added RGBW LED strip support with a setColorRGBW routine
 *    2017-12-29  Dan Ogorchock  Added WiFi RSSI value per request from ST user @stevesell
 *    2018-02-15  Dan Ogorchock  Added @saif76's Ultrasonic Sensor
 *    2018-02-25  Dan Ogorchock  Added Child Presence Sensor
 *    2018-03-03  Dan Ogorchock  Added Child Power Meter
 *    2018-06-05  Dan Ogorchock  Simplified Parent & Child Device Handlers
 *    2018-06-24  Dan Ogorchock  Added Child Servo
 *    2018-07-01  Dan Ogorchock  Added Pressure Measurement
 *    2018-08-06  Dan Ogorchock  Added MAC Address formatting before setting deviceNetworkID
 *    2019-02-05  Dan Ogorchock  Added Child Energy Meter
 *    2019-02-09  Dan Ogorchock  Attempt to prevent duplicate devices from being created
 *    2019-09-08  Dan Ogorchock  Minor tweak to Button logic due to changes in the the Arduino IS_Button.cpp code
 *    2019-10-31  Dan Ogorchock  Added Child Valve
 *    2020-04-11  Dan Ogorchock  Automatically configure the number of buttons
 *    2020-04-11  Dan Ogorchock  Added Delete All Children tile to assist in troubleshooting - uncomment tile and use with care if desired!!!
 *    2020-04-18  Dan Ogorchock  Added Presence Capability and tile to know if the ST_Anything device is online (present) or offline (not present)
 *    2020-04-18  Dan Ogorchock  Removed the Configuration capability and tile as it is no longer used
 *    2020-05-14  Dan Ogorchock  Removed 'defaultValue' fields on user unputs due to bug in ST Classic App for Android 
 *    2020-05-16  Dan Ogorchock  Added support for Sound Pressure Level device
 *
 *	
 */
 
metadata {
	definition (name: "Parent_ST_Anything_Ethernet", namespace: "ogiewon", author: "Dan Ogorchock") {
        //capability "Configuration"
        capability "Refresh"
        capability "Button"
        capability "Holdable Button"
        capability "Signal Strength"
        capability "Presence Sensor"  //used to determine is the Arduino microcontroller is still reporting data or not
        
        command "sendData", ["string"]
        //command "deleteAllChildDevices"
	}

    simulator {
    }

    // Preferences
	preferences {
    	input "ip", "text", title: "Arduino IP Address", description: "IP Address in form 192.168.1.226", required: true, displayDuringSetup: true
		input "port", "text", title: "Arduino Port", description: "port in form of 8090", required: true, displayDuringSetup: true
		input "mac", "text", title: "Arduino MAC Addr", description: "MAC Address in form of 02A1B2C3D4E5", required: true, displayDuringSetup: true
		input "timeOut", "number", title: "Timeout in Seconds", description: "Arduino max time (try 900)", range: "120..*", required: true, displayDuringSetup:true
	}

	// Tile Definitions
	tiles (scale: 2){
		standardTile("refresh", "device.refresh", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
			state "default", label:'Refresh', action: "refresh.refresh", icon: "st.secondary.refresh-icon"
		}

        valueTile("numberOfButtons", "device.numberOfButtons", inactiveLabel: false, width: 2, height: 2) {
			state "numberOfButtons", label:'${currentValue} buttons', unit:""
		}

		standardTile("presence", "device.presence", width: 2, height: 2, canChangeBackground: true) {
			state "present", labelIcon:"st.presence.tile.present", backgroundColor:"#53a7c0"
			state "not present", labelIcon:"st.presence.tile.not-present", backgroundColor:"#ebeef2"
		}

        valueTile("rssi", "device.rssi", width: 2, height: 2) {
			state("rssi", label:'RSSI ${currentValue}', unit:"",
				backgroundColors:[
					[value: -30, color: "#006600"],
					[value: -45, color: "#009900"],
					[value: -60, color: "#99cc00"],
					[value: -70, color: "#ff9900"],
					[value: -90, color: "#ff0000"]
				]
			)
		}
        
		standardTile("deleteChildren", "device.deleteChildren", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
			state "default", label:'Delete Children', action: "deleteAllChildDevices", icon: "st.Seasonal Fall.seasonal-fall-008"
		}


		childDeviceTiles("all")
	}
}

// parse events into attributes
def parse(String description) {
	//log.debug "Parsing '${description}'"
	def msg = parseLanMessage(description)
	def headerString = msg.header

	if (!headerString) {
		//log.debug "headerstring was null for some reason :("
    }

	def bodyString = msg.body

	if (bodyString) {
        log.debug "Parsing: $bodyString"
    	def parts = bodyString.split(" ")
    	def name  = parts.length>0?parts[0].trim():null
    	def value = parts.length>1?parts[1].trim():null
        
		def nameparts = name.split("\\d+", 2)
		def namebase = nameparts.length>0?nameparts[0].trim():null
        def namenum = name.substring(namebase.length()).trim()
		
        def results = []
 
        if (device.currentValue("presence") != "present") {
            sendEvent(name: "presence", value: "present", isStateChange: true, descriptionText: "New update received from Arduino device")
        }
        
		if (timeOut != null) {
            runIn(timeOut, timeOutArduino)
        } else {
           	log.info "Using 900 second default timeout.  Please set the timeout setting appropriately and then click save."
           	runIn(900, timeOutArduino)
        }
        
		if (name.startsWith("button")) {
			//log.debug "In parse:  name = ${name}, value = ${value}, btnName = ${name}, btnNum = ${namemun}"
             if (state.numButtons < namenum.toInteger()) {
                state.numButtons = namenum.toInteger()
                sendEvent(name: "numberOfButtons", value: state.numButtons)
            }
            
            if ((value == "pushed") || (value == "held")) {
                results = createEvent([name: namebase, value: value, data: [buttonNumber: namenum], descriptionText: "${namebase} ${namenum} was ${value} ", isStateChange: true, displayed: true])
                log.debug results
                return results
            }
            else
            {
                return
            }
        }

		if (name.startsWith("rssi")) {
			//log.debug "In parse: RSSI name = ${name}, value = ${value}"
           	results = createEvent(name: name, value: value, displayed: false)
            log.debug results
			return results
        }

        def isChild = containsDigit(name)
   		//log.debug "Name = ${name}, isChild = ${isChild}, namebase = ${namebase}, namenum = ${namenum}"      

		try {
            def childDevice = childDevices.find{it.deviceNetworkId == "${device.deviceNetworkId}-${name}"}
            //if (childDevice) log.debug "childDevice.deviceNetworkId = ${childDevice.deviceNetworkId}"
            
            //If a child should exist, but doesn't yet, automatically add it!            
        	if (isChild && childDevice == null) {
        		log.debug "isChild = true, but no child found - Auto Add it!"
            	//log.debug "    Need a ${namebase} with id = ${namenum}"            
            	childDevice = createChildDevice(namebase, namenum)
			}   
            
            if (childDevice != null) {
                //log.debug "parse() found child device ${childDevice.deviceNetworkId}"
                childDevice.parse("${namebase} ${value}")
				log.debug "${childDevice.deviceNetworkId} - name: ${namebase}, value: ${value}"
            }
            else  //must not be a child, perform normal update
            {
                results = createEvent(name: name, value: value)
                log.debug results
                return results
            }
		}
        catch (e) {
        	log.error "Error in parse() routine, error = ${e}"
        }
	}
}

private getHostAddress() {
    def ip = settings.ip
    def port = settings.port
    
    log.debug "Using ip: ${ip} and port: ${port} for device: ${device.id}"
    return ip + ":" + port
}

def sendData(message) {
    sendEthernet(message) 
}

def sendEthernet(message) {
	log.debug "Executing 'sendEthernet' ${message}"
	if (settings.ip != null && settings.port != null) {
        sendHubCommand(new physicalgraph.device.HubAction(
            method: "POST",
            path: "/${message}?",
            headers: [ HOST: "${getHostAddress()}" ]
        ))
    }
    else {
        state.alertMessage = "ST_Anything Parent Device has not yet been fully configured. Click the 'Gear' icon, enter data for all fields, and click 'Done'"
        runIn(2, "sendAlert")   
    }
}

def refresh() {
	log.debug "Executing 'refresh()'"
	sendEthernet("refresh")
}

def installed() {
	log.debug "Executing 'installed()'"
    if ( device.deviceNetworkId =~ /^[A-Z0-9]{12}$/)
    {
    }
    else
    {
        log.info "ST_Anything Parent Device has not yet been fully configured. Click the 'Gear' icon, enter data for all fields, and click 'Done'"
        //state.alertMessage = "ST_Anything Parent Device has not yet been fully configured. Click the 'Gear' icon, enter data for all fields, and click 'Done'"
        //runIn(2, "sendAlert")
    }

    state.numButtons = 0
    sendEvent(name: "numberOfButtons", value: state.numButtons)
}

def uninstalled() {
    deleteAllChildDevices()
}

def initialize() {
	log.debug "Executing 'initialize()'"
}

def updated() {
	if (!state.updatedLastRanAt || now() >= state.updatedLastRanAt + 5000) {
		state.updatedLastRanAt = now()
		log.debug "Executing 'updated()'"
    	runIn(3, "updateDeviceNetworkID")

        log.debug "Hub IP Address = ${device.hub.getDataValue("localIP")}"
        log.debug "Hub Port = ${device.hub.getDataValue("localSrvPortTCP")}"

        //Schedule inactivity timeout
        log.info "Device inactivity timer started for ${timeOut} seconds"
        runIn(timeOut, timeOutArduino)

	}
	else {
		//log.trace "updated(): Ran within last 5 seconds so aborting."
	}
}

def updateDeviceNetworkID() {
	log.debug "Executing 'updateDeviceNetworkID'"
    def formattedMac = mac.toUpperCase()
    formattedMac = formattedMac.replaceAll(":", "")
    if(device.deviceNetworkId!=formattedMac) {
        log.debug "setting deviceNetworkID = ${formattedMac}"
        device.setDeviceNetworkId("${formattedMac}")
	}
    //Need deviceNetworkID updated BEFORE we can create Child Devices
	//Have the Arduino send an updated value for every device attached.  This will auto-created child devices!
	refresh()
}

private createChildDevice(String deviceName, String deviceNumber) {
    def deviceHandlerName = ""
    if ( device.deviceNetworkId =~ /^[A-Z0-9]{12}$/) {
    
		log.trace "createChildDevice:  Creating Child Device '${device.displayName} (${deviceName}${deviceNumber})'"
        
		try {
        	
        	switch (deviceName) {
         		case "contact": 
                		deviceHandlerName = "Child Contact Sensor" 
                	break
         		case "switch": 
                		deviceHandlerName = "Child Switch" 
                	break
         		case "dimmerSwitch": 
                		deviceHandlerName = "Child Dimmer Switch" 
                	break
         		case "rgbSwitch": 
                		deviceHandlerName = "Child RGB Switch" 
                	break
         		case "generic": 
                		deviceHandlerName = "Child Generic Sensor" 
                	break
         		case "rgbwSwitch": 
                		deviceHandlerName = "Child RGBW Switch" 
                	break
         		case "relaySwitch": 
                		deviceHandlerName = "Child Relay Switch" 
                	break
         		case "temperature": 
                		deviceHandlerName = "Child Temperature Sensor" 
                	break
         		case "humidity": 
                		deviceHandlerName = "Child Humidity Sensor" 
                	break
         		case "motion": 
                		deviceHandlerName = "Child Motion Sensor" 
                	break
         		case "water": 
                		deviceHandlerName = "Child Water Sensor" 
                	break
         		case "illuminance": 
                		deviceHandlerName = "Child Illuminance Sensor" 
                	break
         		case "illuminancergb": 
                		deviceHandlerName = "Child IlluminanceRGB Sensor" 
                	break
         		case "voltage": 
                		deviceHandlerName = "Child Voltage Sensor" 
                	break
         		case "smoke": 
                		deviceHandlerName = "Child Smoke Detector" 
                	break    
         		case "carbonMonoxide": 
                		deviceHandlerName = "Child Carbon Monoxide Detector" 
                	break    
         		case "alarm": 
                		deviceHandlerName = "Child Alarm" 
                	break    
         		case "doorControl": 
                		deviceHandlerName = "Child Door Control" 
                	break
         		case "ultrasonic": 
                		deviceHandlerName = "Child Ultrasonic Sensor" 
                	break
         		case "presence": 
                		deviceHandlerName = "Child Presence Sensor" 
                	break
         		case "power": 
                		deviceHandlerName = "Child Power Meter" 
                	break
         		case "energy": 
                		deviceHandlerName = "Child Energy Meter" 
                	break
         		case "servo": 
                		deviceHandlerName = "Child Servo" 
                	break
         		case "pressure": 
                		deviceHandlerName = "Child Pressure Measurement" 
                	break
         		case "soundPressureLevel": 
                		deviceHandlerName = "Child Sound Pressure Level" 
                	break
         		case "valve": 
                		deviceHandlerName = "Child Valve" 
                	break        
			default: 
                		log.error "No Child Device Handler case for ${deviceName}"
      		}
            if (deviceHandlerName != "") {
                return addChildDevice(deviceHandlerName, "${device.deviceNetworkId}-${deviceName}${deviceNumber}", null,
         			[completedSetup: true, label: "${device.displayName} (${deviceName}${deviceNumber})", 
                	isComponent: false, componentName: "${deviceName}${deviceNumber}", componentLabel: "${deviceName} ${deviceNumber}"])
        	}   
    	} catch (e) {
        	log.error "${deviceName}${deviceNumber} child device creation of type '${deviceHandlerName}' failed with error = ${e}"
        	log.error "Please delete/remove the offending child device in the ST Classic App, and then click Refresh on the Parent Device to have the child created again." 
            
            //state.alertMessage = "Child device creation failed. Please make sure that the '${deviceHandlerName}' is installed and published."
	    	//runIn(2, "sendAlert")
    	}
	} else 
    {
        state.alertMessage = "ST_Anything Parent Device has not yet been fully configured. Click the 'Gear' icon, enter data for all fields, and click 'Done'"
        runIn(2, "sendAlert")
    }
}

private sendAlert() {
   sendEvent(
      descriptionText: state.alertMessage,
	  eventType: "ALERT",
	  name: "childDeviceCreation",
	  value: "failed",
	  displayed: true,
   )
}

private boolean containsDigit(String s) {
    boolean containsDigit = false;

    if (s != null && !s.isEmpty()) {
		//log.debug "containsDigit .matches = ${s.matches(".*\\d+.*")}"
		containsDigit = s.matches(".*\\d+.*")
    }
    return containsDigit
}

def timeOutArduino() {
    //If the timeout expires before being reset, mark this Parent Device as 'not present' to allow action to be taken
    log.info "No update received from Arduino device in past ${timeOut} seconds"
    sendEvent(name: "presence", value: "not present", isStateChange: true, descriptionText: "No update received from Arduino device in past ${timeOut} seconds")
}

def deleteAllChildDevices() {
    log.info "Deleting all Child Devices"
    getChildDevices().each {
        deleteChildDevice(it.deviceNetworkId)
    }
    state.numButtons = 0    
    sendEvent(name: "numberOfButtons", value: state.numButtons)
}

/*
def mywait(ms) {
    log.info "starting wait"
	def start = now()
	while (now() < start + ms) {
    	// hurry up and wait!
    }
    log.info "ending wait"
}
*/