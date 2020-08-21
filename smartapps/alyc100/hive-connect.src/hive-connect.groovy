/**
 *  Hive (Connect)
 *
 *  Copyright 2015,2016 Alex Lee Yuk Cheung
 *	Hive Contact Sensor code portions contributed by Simon Green
 *  Hive Active Bulb code portions contributed by Tom Beech
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
 *  VERSION HISTORY
 *
 *  24.02.2016
 *  v2.0 BETA - New Hive Connect App
 *  v2.0.1 BETA - Fix bug for accounts that do not have capabilities attribute against thermostat.
 *	v2.1 - Improved authentication process and overhaul to UI. Added notification capability.
 *  v2.1.1 - Bug fix when initially selecting devices for the first time.
 *	v2.1.2 - Move external icon references into Github\
 *
 *	17.08.2016
 *  v2.1.3 - Fix null pointer on state variable corruption
 *	v2.1.3b - Fix device failure on API timeout
 *
 *	01.09.2016
 *	v2.2 - Integrate auto mode functionality from Auto Mode for Thermostat smart app
 *
 *  04.09.2016
 *	v2.3 - Added support for Hive Contact Sensor - Author: Simon Green
 *
 *	06.09.2016
 *	v2.3.1 - Improve device detection
 *
 *	10.09.2016
 *	v2.3.2 - Added notification option for maximum temperature threshold breach for Hive heating devices.
 *
 *  23.1.2016
 *  v2.4 - Added support for Hive Active Warm White and Hive Active Tunable Lights - Author: Tom Beech
 *	v2.4b - Minor UI fixes for bulb devices.
 *
 *  28.11.2016 
 *  v2.5 - Added support for Hive Active Plugs - Author: Tom Beech
 *		 - Refactor of device selecting string - Author: Tom Beech
 *		 - Review device naming and text consistency.
 *
 *	v2.5b - Shortern some device names.
 *
 * 	02.12.2016
 * 	v2.6 - Added support for Hive Active Colour Bulb - Author: Tom Beech
 *
 *	28.05.2017
 *	v2.7 - Support for new Hive Beekeeper API - Authors: Tom Beech, Alex Lee Yuk Cheung
 *		 - Removed support for Hive Contact Sensor. Zigbee integration by Simon Green is preferred option.
 *	v2.7b - Bug fix. Refresh bug prevents installation of Hive devices.
 *
 *	30.10.2017
 *	v3.0 - Support for Hive Active Light Colour Tuneable device.
 *
 *  4.10.2019
 *  v3.1 - Support for Hive Radiator TRV
 */
definition(
		name: "Hive (Connect)",
		namespace: "alyc100",
		author: "Alex Lee Yuk Cheung",
		description: "Connect your Hive devices to SmartThings.",
		iconUrl: "https://raw.githubusercontent.com/alyc100/SmartThingsPublic/master/smartapps/alyc100/10457773_334250273417145_3395772416845089626_n.png",
		iconX2Url: "https://raw.githubusercontent.com/alyc100/SmartThingsPublic/master/smartapps/alyc100/10457773_334250273417145_3395772416845089626_n.png",
    singleInstance: true
)

preferences {
	//startPage
	page(name: "startPage")

	//Connect Pages
	page(name:"mainPage", title:"Hive Device Setup", content:"mainPage", install: true)
	page(name: "loginPAGE")
	page(name: "selectDevicePAGE")
	page(name: "preferencesPAGE")
	page(name: "tmaPAGE")

	//Thermostat Mode Automation Pages
	page(name: "tmaConfigurePAGE")
}

def apiBeekeeperUKURL(path = '/') 			 { return "https://beekeeper-uk.hivehome.com:443/1.0${path}" }
def apiBeekeeperURL(path = '/') 			 { return "https://beekeeper.hivehome.com:443/1.0${path}" }

def startPage() {
	if (parent) {
		atomicState?.isParent = false
		tmaConfigurePAGE()
	} else {
		atomicState?.isParent = true
		mainPage()
	}
}

//Hive Connect App Pages

def mainPage() {
	log.debug "mainPage"
	if (username == null || username == '' || password == null || password == '') {
		return dynamicPage(name: "mainPage", title: "", install: true, uninstall: true) {
			section {
				headerSECTION()
				href("loginPAGE", title: null, description: authenticated() ? "Authenticated as " +username : "Tap to enter Hive credentials", state: authenticated())
			}
		}
	} else {
		log.debug "next phase"
		return dynamicPage(name: "mainPage", title: "", install: true, uninstall: true) {
			section {
				headerSECTION()
				href("loginPAGE", title: "Authenticated as", description: authenticated() ? username : "Tap to enter Hive credentials", state: authenticated())
			}
			if (stateTokenPresent()) {
				section ("Choose your devices:") {
					href("selectDevicePAGE", title: "Devices", description: devicesSelected() ? getDevicesSelectedString() : "Tap to select devices", state: devicesSelected())
				}
				section("Hive Mode Automations:") {
					href "tmaPAGE", title: "Hive Mode Automations...", description: (tmaDescription() ? tmaDescription() : "Tap to Configure..."), state: (tmaDescription() ? "complete" : null)
				}
				section ("Notifications:") {
					href("preferencesPAGE", title: null, description: preferencesSelected() ? getPreferencesString() : "Tap to configure notifications", state: preferencesSelected())
				}
			} else {
				section {
					paragraph "There was a problem connecting to Hive. Check your user credentials and error logs in SmartThings web console.\n\n${state.loginerrors}"
				}
			}
		}
	}
}

def headerSECTION() {
	return paragraph (image: "https://raw.githubusercontent.com/alyc100/SmartThingsPublic/master/smartapps/alyc100/10457773_334250273417145_3395772416845089626_n.png",
                  "Hive (Connect)\nVersion: 3.1\nDate: 04112019(1630)")
}

def stateTokenPresent() {
	return state.beekeeperAccessToken != null && state.beekeeperAccessToken != ''
}

def authenticated() {
	return (state.beekeeperAccessToken != null && state.beekeeperAccessToken != '') ? "complete" : null
}

def devicesSelected() {
	return (selectedHeating || selectedHotWater || selectedBulb || selectedTunableBulb || selectedActivePlug || selectedColourBulb) ? "complete" : null
}

def preferencesSelected() {
	return (sendPush || sendSMS != null) && (maxtemp != null || mintemp != null || sendBoost || sendOff || sendManual || sendSchedule || sendMaxThresholdBreach) ? "complete" : null
}

def tmaDescription() {
	def tmaApp = findChildAppByName( appName() )
	if(tmaApp) {
		def str = ""
		str += "Thermostat Automations:"

		childApps?.each { a ->
			def name = a?.getLabel()
			str += "\n• $name"
		}

		return str
	}
	return null
}


def getDevicesSelectedString() {
	if (state.hiveHeatingDevices == null ||
    	state.hiveHotWaterDevices == null || 
        state.hiveTunableBulbDevices == null || 
        state.hiveBulbDevices == null ||
        state.hiveActivePlugDevices == null ||
        state.hiveColourBulb == null) {
    	updateDevices()
  }
	def listString = ""
    
	selectedHeating.each { childDevice ->    
    	if (null != state.hiveHeatingDevices)
    		listString += "${state.hiveHeatingDevices[childDevice]}\n"
    }
  
	selectedHotWater.each { childDevice ->
      if (null != state.hiveHotWaterDevices) 
           	listString += "${state.hiveHotWaterDevices[childDevice]}\n"
	}
    
	selectedBulb.each { childDevice ->
        if (null != state.hiveBulbDevices)
            listString += "${state.hiveBulbDevices[childDevice]}\n"
	}
    
	selectedTunableBulb.each { childDevice ->		
		if (null != state.hiveTunableBulbDevices)
            listString += "${state.hiveTunableBulbDevices[childDevice]}\n"
	}    
    selectedActivePlug.each { childDevice ->		
		if (null != state.hiveActivePlugDevices)
            listString += "${state.hiveActivePlugDevices[childDevice]}\n"
	}
    selectedColourBulb.each {  childDevice ->		
		if (null != state.selectedColourBulb)
            listString += "${state.selectedColourBulb[childDevice]}\n"
	}
  
  	// Returns the completed list, and trims the last carrige return
	return listString.trim()
}


def getPreferencesString() {
	def listString = ""
  if (sendPush) listString += "Send Push, "
  if (sendSMS != null) listString += "Send SMS, "
  if (maxtemp != null) listString += "Max Temp: ${maxtemp}, "
  if (mintemp != null) listString += "Min Temp: ${mintemp}, "
  if (sendBoost) listString += "Boost, "
  if (sendOff) listString += "Off, "
  if (sendManual) listString += "Manual, "
  if (sendSchedule) listString += "Schedule, "
  if (sendMaxThresholdBreach) listString += "Max Temp Threshold Breach, "
  if (listString != "") listString = listString.substring(0, listString.length() - 2)
  return listString
}

def loginPAGE() {
	if (username == null || username == '' || password == null || password == '') {
		return dynamicPage(name: "loginPAGE", title: "Login", uninstall: false, install: false) {
			section { headerSECTION() }
			section { paragraph "Enter your Hive credentials below to enable SmartThings and Hive integration." }
			section("Hive Credentials:") {
				input("username", "text", title: "Username", description: "Your Hive username (usually an email address)", required: true)
				input("password", "password", title: "Password", description: "Your Hive password", required: true, submitOnChange: true)
			}
		}
	} else {
		getBeekeeperAccessToken()
		dynamicPage(name: "loginPAGE", title: "Login", uninstall: false, install: false) {
			section { headerSECTION() }
			section { paragraph "Enter your Hive credentials below to enable SmartThings and Hive integration." }
			section("Hive Credentials:") {
				input("username", "text", title: "Username", description: "Your Hive username (usually an email address)", required: true)
				input("password", "password", title: "Password", description: "Your Hive password", required: true, submitOnChange: true)
			}
			if (stateTokenPresent()) {
				section {
					paragraph "You have successfully connected to Hive. Click 'Done' to select your Hive devices."
				}
			} else {
				section {
					paragraph "There was a problem connecting to Hive. Check your user credentials and error logs in SmartThings web console.\n\n${state.loginerrors}"
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
			input "selectedHeating", "enum", image: "https://raw.githubusercontent.com/alyc100/SmartThingsPublic/master/smartapps/alyc100/thermostat-frame-6c75d5394d102f52cb8cf73704855446.png", required:false, title:"Select Hive Heating Devices \n(${state.hiveHeatingDevices.size() ?: 0} found)", multiple:true, options:state.hiveHeatingDevices
			input "selectedHotWater", "enum", image: "https://raw.githubusercontent.com/alyc100/SmartThingsPublic/master/smartapps/alyc100/thermostat-frame-6c75d5394d102f52cb8cf73704855446.png", required:false, title:"Select Hive Hot Water Devices \n(${state.hiveHotWaterDevices.size() ?: 0} found)", multiple:true, options:state.hiveHotWaterDevices
            input "selectedBulb", "enum", image: "https://raw.githubusercontent.com/alyc100/SmartThingsPublic/master/smartapps/alyc100/hive-bulb.jpg", required:false, title:"Select Hive Light Dimmable Devices \n(${state.hiveBulbDevices.size() ?: 0} found)", multiple:true, options:state.hiveBulbDevices
			input "selectedTunableBulb", "enum", image: "https://raw.githubusercontent.com/alyc100/SmartThingsPublic/master/smartapps/alyc100/hive-tunablebulb.jpg", required:false, title:"Select Hive Light Tuneable Devices \n(${state.hiveTunableBulbDevices.size() ?: 0} found)", multiple:true, options:state.hiveTunableBulbDevices
            input "selectedColourBulb", "enum", image: "https://raw.githubusercontent.com/alyc100/SmartThingsPublic/master/smartapps/alyc100/hive-colouredbulb.jpg", required:false, title:"Select Hive Light Colour Devices \n(${state.hiveColourBulb.size() ?: 0} found)", multiple:true, options:state.hiveColourBulb
            input "selectedActivePlug", "enum", image: "https://raw.githubusercontent.com/alyc100/SmartThingsPublic/master/smartapps/alyc100/hive-activeplug.jpg", required:false, title:"Select Hive Plug Devices \n(${state.hiveActivePlugDevices.size() ?: 0} found)", multiple:true, options:state.hiveActivePlugDevices
		}
  	}
}

def preferencesPAGE() {
	dynamicPage(name: "preferencesPAGE", title: "Preferences", uninstall: false, install: false) {
    section {
    	input "sendPush", "bool", title: "Send as Push?", required: false, defaultValue: false
			input "sendSMS", "phone", title: "Send as SMS?", required: false, defaultValue: null
    }
    section("Thermostat Notifications:") {
			input "sendBoost", "bool", title: "Notify when mode is Boosting?", required: false, defaultValue: false
			input "sendOff", "bool", title: "Notify when mode is Off?", required: false, defaultValue: false
			input "sendManual", "bool", title: "Notify when mode is Manual?", required: false, defaultValue: false
      input "sendSchedule", "bool", title: "Notify when mode is Schedule?", required: false, defaultValue: false
		}
    section("Thermostat Max Temperature") {
    	input ("maxtemp", "number", title: "Alert when temperature is above this value", required: false, defaultValue: 25)
    }
    section("Thermostat Min Temperature") {
    	input ("mintemp", "number", title: "Alert when temperature is below this value", required: false, defaultValue: 10)
    }
    section("Thermostat Max Threshold Breach") {
    	input "sendMaxThresholdBreach", "bool", title: "Notify when max temp threshold has been breached?", required: false, defaultValue: false
    }
  }
}

def tmaPAGE() {
	dynamicPage(name: "tmaPAGE", title: "", nextPage: !parent ? "startPage" : "tmaPAGE", install: false) {
		def tmaApp = findChildAppByName( appName() )
		if(tmaApp) {
			section("Configured Hive Mode Automations...") { }
		} else {
			section("") {
				paragraph "Create New Hive Mode Automation to get Started..."
			}
		}
		section("Add a new Automation:") {
			app(name: "tmaApp", appName: appName(), namespace: "alyc100", multiple: true, title: "Create New Mode Automation...")
			def rText = "NOTICE:\nIntegrated Hive Mode Automations is in BETA\n"
			paragraph "${rText}"//, required: true, state: null
		}
	}
}

//Auto Thermostat Mode Pages
def tmaConfigurePAGE() {
	dynamicPage(name: "tmaConfigurePAGE", title: "Hive Mode Automation", install: true, uninstall: true) {
		section {
    		input ("thermostats", "capability.thermostat", title: "For these thermostats",  multiple: true, required: true)
  	}
    section {
        input(name: "modeTrigger", title: "Set the trigger to",
              description: null, multiple: false, required: true, submitOnChange: true, type: "enum",
              options: ["true": "Mode Change", "false": "Switches"])
    }
    if (modeTrigger == "true") {
      // Do something here like update a message on the screen,
      // or introduce more inputs. submitOnChange will refresh
      // the page and allow the user to see the changes immediately.
      // For example, you could prompt for the level of the dimmers
      // if dimmers have been selected:
      section {
 				input ("modes", "mode", title:"When SmartThings enters these modes", multiple: true, required: true)
			}
    } else if (modeTrigger == "false") {
    	section {
    		input ("theSwitch", "capability.switch", title:"When this switch is activated", multiple: false, required: true)
      }
    }
		section {
			input ("alteredThermostatMode", "enum", multiple: false, title: "Set thermostats to this mode",
             options: ["Set To Schedule", "Boost", "Turn Off", "Set to Manual"], required: true, defaultValue: 'Turn Off')
		}
    section {
    	input ("resetThermostats", "enum", title: "Reset thermostats after trigger turns off?",
             options: ["true": "Yes","false": "No"], required: true, submitOnChange: true)
  	}
    if (resetThermostats == "true") {
      section {
			input ("resumedThermostatMode", "enum", multiple: false, title: "Reset thermostats back to this mode", submitOnChange: true,
            	options: ["Set To Schedule", "Boost", "Turn Off", "Set to Manual"], required: true, defaultValue: 'Set To Schedule')
		  }
      if (resumedThermostatMode == "Boost") {
      	section {
					input ("thermostatModeAfterBoost", "enum", multiple: false, title: "What to do when Boost has finished",
        	 				options: ["Set To Schedule", "Turn Off", "Set to Manual"], required: true, defaultValue: 'Set To Schedule')
				}
      }
    }
		section( "Additional configuration" ) {
      input ("days", "enum", title: "Only on certain days of the week", multiple: true, required: false,
		         options: ["Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"])
      href "timeIntervalInput", title: "Only during a certain time", description: getTimeLabel(starting, ending), state: greyedOutTime(starting, ending), refreshAfterSelection:true
		  input ("temp", "decimal", title: "If setting to Manual, set the temperature to this", required: false, defaultValue: 21)
    }
		section( "Notifications" ) {
    	input ("sendPushMessage", "enum", title: "Send a push notification?",
             options: ["Yes", "No"], required: true)
    	input ("phone", "phone", title: "Send a Text Message?", required: false)
		}
    section {
    	label title: "Assign a name", required: true
  	}
	}
}

page(name: "timeIntervalInput", title: "Only during a certain time", refreshAfterSelection:true) {
	section {
		input "starting", "time", title: "Starting", required: false
		input "ending", "time", title: "Ending", required: false
	}
}


// App lifecycle hooks

def installed() {
	if(parent) { installedChild() } // This will handle all of the install functions when the child app is installed
	else { installedParent() } // This will handle all of the install functions when the parent app is installed
}

def updated() {
	if(parent) { updatedChild() } // This will handle all of the install functions when the child app is updated
	else { updatedParent() } // This will handle all of the install functions when the parent app is updated
}

def uninstalled() {
	if(parent) { } // This will handle all of the install functions when the child app is uninstalled
	else { uninstalledParent() } // This will handle all of the install functions when the parent app is uninstalled
}

def installedParent() {
	log.debug "installed"
	initialize()
	// Check for new devices every 3 hours
	runEvery3Hours('updateDevices')
	// execute handlerMethod every 10 minutes.
	runEvery10Minutes('refreshDevices')
}

// called after settings are changed
def updatedParent() {
	log.debug "updated"
	unsubscribe()
	initialize()
	unschedule('refreshDevices')
	runEvery10Minutes('refreshDevices')
}

def uninstalledParent() {
	log.info("Uninstalling, removing child devices...")
	unschedule()
	removeChildDevices(getChildDevices())
}

private removeChildDevices(devices) {
	devices.each {
		deleteChildDevice(it.deviceNetworkId) // 'it' is default
	}
}

def installedChild() {
  log.debug "Installed with settings: ${settings}"
  //set up initial thermostat state and force thermostat into correct mode
  state.thermostatAltered = false
  state.boostingReset = false

  //Flags to stop possible infinite loop scenarios when handlers create events
  state.internalThermostatEvent = false
  state.internalSwitchEvent = false

  subscribe(thermostats, "thermostatMode", thermostateventHandlerForTMA, [filterEvents: false])
  //Check if mode or switch is the trigger and run initialisation
  if (modeTrigger == "true") {
  	def currentMode = location.mode
  	log.debug "currentMode = $currentMode"
  	if (currentMode in modes) {
      	takeActionForMode(currentMode)
  	}
  	subscribe(location, "mode", modeeventHandlerForTMA, [filterEvents: false])
  }
  else {
  	if (theSwitch.currentSwitch == "on") {
      	takeActionForSwitch(theSwitch.currentSwitch)
      }
  	subscribe(theSwitch, "switch", switcheventHandlerForTMA, [filterEvents: false])
  }
}

def updatedChild() {
  log.debug "Updated with settings: ${settings}"
  unsubscribe()
  //set up initial thermostat state and force thermostat into correct mode
  state.thermostatAltered = false
  state.boostingReset = false
  state.internalThermostatEvent = false
  state.internalSwitchEvent = false
  subscribe(thermostats, "thermostatMode", thermostateventHandlerForTMA, [filterEvents: false])
  //Check if mode or switch is the trigger and run initialisation
  if (modeTrigger == "true") {
  	def currentMode = location.mode
  	log.debug "currentMode = $currentMode"
  	if (currentMode in modes) {
      	takeActionForMode(currentMode)
  	}
  	subscribe(location, "mode", modeeventHandlerForTMA, [filterEvents: false])
  } else {
  	if (theSwitch.currentSwitch == "on") {
      	takeActionForSwitch(theSwitch.currentSwitch)
    }
  	subscribe(theSwitch, "switch", switcheventHandlerForTMA, [filterEvents: false])
  }
}

// called after Done is hit after selecting a Location
def initialize() {
	if (parent) { }
    else {
		log.debug "initialize"

  	if (selectedHeating) {
			addHeating()
		}
		if (selectedHotWater) {
			addHotWater()
		}
		if(selectedBulb) {
        	addBulb()
        }
        if(selectedTunableBulb) {
        	addTunableBulb()
        }
        if(selectedActivePlug) {
        	addActivePlug()
        }
		if(selectedColourBulb) {
			addColourBulb()
		}
 	 	runIn(10, 'refreshDevices') // Asynchronously refresh devices so we don't block

  	//subscribe to events for notifications if activated
  	if (preferencesSelected() == "complete") {
  		getChildDevices().each { childDevice ->
  			if (childDevice.typeName == "Hive Heating V2.0" || childDevice.typeName == "Hive Hot Water V2.0") {
  				subscribe(childDevice, "thermostatMode", modeHandler, [filterEvents: false])
    		}
    		if (childDevice.typeName == "Hive Heating V2.0") {
    			subscribe(childDevice, "temperature", tempHandler, [filterEvents: false])
                subscribe(childDevice, "maxtempthresholdbreach", evtHandler, [filterEvents: false])
    		}
  		}
  	}
  	state.maxNotificationSent = false
  	state.minNotificationSent = false
  }
}

//Event Handler for Connect App
def evtHandler(evt) {
	def msg
    if (evt.name == "maxtempthresholdbreach") {
    	msg = "Auto adjusting set temperature of ${evt.displayName} as current set temperature of ${evt.value}°C is above maximum threshold."
    	if (settings.sendMaxThresholdBreach) generateNotification(msg)    
    }
}

def tempHandler(evt) {
	def msg
    log.trace "temperature: $evt.value, $evt"

    if (settings.maxtemp != null) {
    	def maxTemp = settings.maxtemp
        if (evt.doubleValue >= maxTemp) {
        	msg = "${evt.displayName} temperature reading is very hot."
            if (state.maxNotificationSent == null || state.maxNotificationSent == false) {
            	generateNotification(msg)
                //Avoid constant messages
            	state.maxNotificationSent = true
            }
        }
        else {
        	//Reset if temperature falls back to normal levels
            state.maxNotificationSent = false
        }
    }
    else if (settings.mintemp != null) {
    	def minTemp = settings.mintemp
        if (evt.doubleValue <= minTemp) {
        	msg = "${evt.displayName} temperature reading is very cold."
            if (state.minNotificationSent == null || state.minNotificationSent == false) {
            	generateNotification(msg)
                //Avoid constant messages
            	state.minNotificationSent = true
            }
        }
        else {
        	//Reset if temperature falls back to normal levels
        	state.minNotificationSent = false
        }
    }
}

def modeHandler(evt) {
	def msg
    	if (evt.value == "heat") {
    		msg = "${evt.displayName} is set to Manual"
        	if (settings.sendSchedule) generateNotification(msg)
    	}
		else if (evt.value == "off") {
    		msg = "${evt.displayName} is turned Off"
        	if (settings.sendOff) generateNotification(msg)
    	}
    	else if (evt.value == "auto") {
    		msg = "${evt.displayName} is set to Schedule"
        	if (settings.sendManual) generateNotification(msg)
    	}
    	else if (evt.value == "emergency heat") {
    		msg = "${evt.displayName} is in Boost mode"
       	 	if (settings.sendBoost) generateNotification(msg)
    	}

}

//Event Handlers for Thermostat Mode Automation

def modeeventHandlerForTMA(evt) {
    if(allOk) {
    	log.debug "evt.value: $evt.value"
    	takeActionForMode(evt.value)
    }
}

//Handler and action for switch detection
def switcheventHandlerForTMA(evt) {
	if(allOk) {
		log.debug "evt.value: $evt.value"
    	log.debug "state.internalSwitchEvent: $state.internalSwitchEvent"
    	if (state.internalSwitchEvent == false) {
    		takeActionForSwitch(evt.value)
    	}
    	state.internalSwitchEvent = false
    }
}

def thermostateventHandlerForTMA(evt) {
	log.debug "evt.name: $evt.value"
    log.debug "state.thermostatAltered: $state.thermostatAltered"
    log.debug "alteredThermostatMode: $alteredThermostatMode"
    log.debug "state.boostingReset: $state.boostingReset"
    //If boost mode is selected as the trigger, turn switch off if boost mode finishes...
 	if (state.internalThermostatEvent == false) {
    	if (modeTrigger == "false") {
    		//if the switch is currently on, check the new mode of the thermostat and set switch to off if necessary
        	if (alteredThermostatMode == "Boost") {
            	state.internalSwitchEvent = true
        		if (evt.value != "emergency heat") {
                	//Switching the switch to off should trigger an event that resets app state
        			theSwitch.off()
        		}
            	else {
            		//Switching the switch to on so it can't be boost again
            		theSwitch.on()
                }
            }
       	 }

    	//If boost mode is selected as resumed state, need to set thermostat mode as per preference
    	if (state.boostingReset) {
    		if (evt.value != "emergency heat") {
            	state.internalThermostatEvent = true
        		changeAllThermostatsModes(thermostats, thermostatModeAfterBoost, "Boost has now finished")
            	//Reset boosting reset flag
            	state.boostingReset = false
        	}
    	}
    }
    state.internalThermostatEvent = false
}

// Thermostat Auto Mode Methods
def takeActionForSwitch(switchState) {
	// Is incoming switch is on
    if (switchState == "on")
    {
    	//Check thermostat is not already altered
    	if (!state.thermostatAltered)
        {
        	//Turn selected thermostats into selected mode

            //Add detail to push message if set to Manual is specified
        	log.debug "$theSwitch.label is on, turning thermostats to $alteredThermostatMode"
            state.internalThermostatEvent = true
            changeAllThermostatsModes(thermostats, alteredThermostatMode, "$theSwitch.label has turned on")
            //Only if reset action is specified, set the thermostatAltered state.
            if (resetThermostats == "true")
            {
        		state.thermostatAltered = true
            }
        }
    }
    else {
        log.debug "$theSwitch.label is off"
        //Check if thermostats have previously been altered
        if (state.thermostatAltered)
        {
        	//Check if user wants to reset thermostats
        	if (resetThermostats == "true")
 			{
            	log.debug "Thermostats have been altered, turning back to $resumedThermostatMode"
                //Turn selected thermostats into selected mode
                state.internalThermostatEvent = true
            	changeAllThermostatsModes(thermostats, resumedThermostatMode, "$theSwitch.label has turned off")

                //Set flag if boost mode is selected as reset state so it can be set back to desired mode in 'thermostatModeAfterBoost'
                if (resumedThermostatMode == "Boost") {
                	state.boostingReset = true
                }

            }
            //Reset app state
            state.thermostatAltered = false
        }
        else
     	{
        	log.debug "Thermostats were not altered. No action taken."
        }
    }
}

def takeActionForMode(mode) {
	// Is incoming mode in the event input enumeration
    if (mode in modes)
    {
    	//Check thermostat is not already altered
    	if (!state.thermostatAltered)
        {
        	//Turn selected thermostats into selected mode

            //Add detail to push message if set to Manual is specified
        	log.debug "$mode in selected modes, turning thermostats to $alteredThermostatMode"
            state.internalThermostatEvent = true
            changeAllThermostatsModes(thermostats, alteredThermostatMode, "mode has changed to $mode")

        	//Only if reset action is specified, set the thermostatAltered state.
            if (resetThermostats == "true")
            {
        		state.thermostatAltered = true
            }
        }
    }
    else {
        log.debug "$mode is not in select modes"
        //Check if thermostats have previously been altered
        if (state.thermostatAltered)
        {
        	//Check if user wants to reset thermostats
        	if (resetThermostats == "true")
 			{
            	log.debug "Thermostats have been altered, turning back to $resumedThermostatMode"

            	//Turn each thermostat to selected mode
                state.internalThermostatEvent = true
            	changeAllThermostatsModes(thermostats, resumedThermostatMode, "mode has changed to $mode")

 				//Set flag if boost mode is selected as reset state so it can be set back to desired mode in 'thermostatModeAfterBoost'
                if (resumedThermostatMode == "Boost") {
                	state.boostingReset = true
                }

            }
            //Reset app state
            state.thermostatAltered = false
        }
        else
     	{
        	log.debug "Thermostats were not altered. No action taken."
        }
    }
}

//Helper method for thermostat mode change
private changeAllThermostatsModes(thermostats, newThermostatMode, reason) {
	//Add detail to push message if set to Manual is specified
    def thermostatModeDetail = newThermostatMode
    if (newThermostatMode == "Set to Manual") {
    	thermostatModeDetail = thermostatModeDetail + " at $temp°C"
    }
	for (thermostat in thermostats) {
    	def message = ''
        message = "SmartThings has reset $thermostat.label to $thermostatModeDetail because $reason."
        log.info message
        send(message)
        log.debug "Setting $thermostat.label to $thermostatModeDetail"
		if (newThermostatMode == "Set to Manual") {
    		thermostat.heat()
        	thermostat.setHeatingSetpoint(temp)
    	}
    	else if (newThermostatMode == "Turn Off") {
    		thermostat.off()
    	}
    	else if (newThermostatMode == "Boost") {
    		thermostat.emergencyHeat()
    	}
    	else {
    		thermostat.auto()
		}
    }
}

private send(msg) {
    if ( sendPushMessage != "No" ) {
        log.debug( "sending push message" )
        sendPush( msg )
    }

    if ( phone ) {
        log.debug( "sending text message" )
        sendSms( phone, msg )
    }

    log.debug msg
}

private getAllOk() {
	daysOk && timeOk
}

private getDaysOk() {
	def result = true
	if (days) {
		def df = new java.text.SimpleDateFormat("EEEE")
		if (location.timeZone) {
			df.setTimeZone(location.timeZone)
		}
		else {
			df.setTimeZone(TimeZone.getTimeZone("Europe/London"))
		}
		def day = df.format(new Date())
		result = days.contains(day)
	}
	log.trace "daysOk = $result"
	result
}

private getTimeOk() {
	def result = true
	if (starting && ending) {
		def currTime = now()
		def start = timeToday(starting).time
		def stop = timeToday(ending).time
		result = start < stop ? currTime >= start && currTime <= stop : currTime <= stop || currTime >= start
	}
	log.trace "timeOk = $result"
	result
}

private hhmm(time, fmt = "h:mm a") {
	def t = timeToday(time, location.timeZone)
	def f = new java.text.SimpleDateFormat(fmt)
	f.setTimeZone(location.timeZone ?: timeZone(time))
	f.format(t)
}

def getTimeLabel(starting, ending){

	def timeLabel = "Tap to set"

    if(starting && ending){
    	timeLabel = "Between" + " " + hhmm(starting) + " "  + "and" + " " +  hhmm(ending)
    }
    else if (starting) {
		timeLabel = "Start at" + " " + hhmm(starting)
    }
    else if(ending){
    timeLabel = "End at" + hhmm(ending)
    }
	timeLabel
}

private hideOptionsSection() {
	(starting || ending || days || modes) ? false : true
}

def greyedOutSettings(){
	def result = ""
    if (starting || ending || days || falseAlarmThreshold) {
    	result = "complete"
    }
    result
}

def greyedOutTime(starting, ending){
	def result = ""
    if (starting || ending) {
    	result = "complete"
    }
    result
}

def generateNotification(msg) {
	if (settings.sendSMS != null) {
		sendSms(sendSMS, msg)
	}
	if (settings.sendPush == true) {
		sendPush(msg)
	}
}

def updateDevices() {
	if (!state.devices) {
		state.devices = [:]
	}
	def devices = devicesList()
  state.hiveHeatingDevices = [:]
  state.hiveHotWaterDevices = [:]
  state.hiveBulbDevices = [:]
  state.hiveTunableBulbDevices = [:]
  state.hiveActivePlugDevices = [:]
  state.hiveColourBulb = [:]
  
  def selectors = []
	devices.each { device ->
        selectors.add("${device.id}")
        //Heating
        if (device.type == "heating" || device.type == "trvcontrol") {
        	def suffix = device.type == "heating" ? "Hive Heating" : "Hive TRV"
            //Heating Control
            log.debug "Identified: ${device.state.name} ${suffix}"
            def value = "${device.state.name} ${suffix}"
            def key = device.type + "/" + device.id
	        selectors.add("${key}")
            state.hiveHeatingDevices["${key}"] = value

            //Update names of devices with Hive
                def childDevice = getChildDevice("${key}")
                if (childDevice) {
                    //Update name of device if different.
                    if(childDevice.name != device.state.name + " ${suffix}") {
                            childDevice.name = device.state.name + " ${suffix}"
                            log.debug "Device's name has changed."
                    }
                }
        // Water Control
        } else if (device.type == "hotwater") {
        	log.debug "Identified: ${device.state.name} Hive Hot Water"
            def value = "${device.state.name} Hive Hot Water"
            def key = device.id
            state.hiveHotWaterDevices["${key}"] = value

            //Update names of devices
            def childDevice = getChildDevice("${device.id}")
            if (childDevice) {
            	//Update name of device if different.
                    if(childDevice.name != device.state.name + " Hive Hot Water") {
                            childDevice.name = device.state.name + " Hive Hot Water"
                            log.debug "Device's name has changed."
                    }
            }
        //Dimmable Bulb
        } else if (device.type == "tuneablelight") {
			log.debug "Identified: ${device.state.name} Hive Light Tunable"
            def value = "${device.state.name} Hive Light Tunable"
                def key = device.id
                state.hiveTunableBulbDevices["${key}"] = value
                //Update names of devices
            	def childDevice = getChildDevice("${device.id}")
            	if (childDevice) {
                	//Update name of device if different.
                	if(childDevice.name != device.state.name) {
                            childDevice.name = device.state.name
                            log.debug "Device's name has changed."
                    }
            	}
        //Colour Bulb
        } else if (device.type == "colourtuneablelight") {
			log.debug "Identified: ${device.state.name} Hive Colour Bulb"
            def value = "${device.state.name} Hive Colour Bulb"
                def key = device.id
                state.hiveColourBulb["${key}"] = value
                //Update names of devices
            	def childDevice = getChildDevice("${device.id}")
            	if (childDevice) {
                	//Update name of device if different.
                	if(childDevice.name != device.state.name) {
                            childDevice.name = device.state.name
                            log.debug "Device's name has changed."
                    }
            	}
        //White Active Light Bulb
        } else if (device.type == "warmwhitelight") {
			log.debug "Identified: ${device.state.name} Hive Light Dimmable"
            def value = "${device.state.name} Hive Light Dimmable"
                def key = device.id
                state.hiveBulbDevices["${key}"] = value
                //Update names of devices
            	def childDevice = getChildDevice("${device.id}")
            	if (childDevice) {
                	//Update name of device if different.
                	if(childDevice.name != device.state.name) {
                            childDevice.name = device.state.name
                            log.debug "Device's name has changed."
                    }
            	}
        // Active Plug            
        } else if (device.type == "activeplug") {
				log.debug "Identified: ${device.state.name} Hive Plug"
            	def value = "${device.state.name} Hive Plug"
                def key = device.id
                state.hiveActivePlugDevices["${key}"] = value
                //Update names of devices
            	def childDevice = getChildDevice("${device.id}")
            	if (childDevice) {
                	//Update name of device if different.
                	if(childDevice.name != device.state.name) {
                        childDevice.name = device.state.name
                        log.debug "Device's name has changed."
                    }
            	}
          }	 
	}
  //Remove devices if does not exist on the Hive platform
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

def addHeating() {
	updateDevices()

	selectedHeating.each { device ->

        def childDevice = getChildDevice("${device}")

        if (!childDevice) {
    		log.info("Adding Hive Heating device ${device}: ${state.hiveHeatingDevices[device]}")

        	def data = [
                name: state.hiveHeatingDevices[device],
				label: state.hiveHeatingDevices[device],
			]
            childDevice = addChildDevice(app.namespace, "Hive Heating", "$device", null, data)
            childDevice.refresh()

			log.debug "Created ${state.hiveHeatingDevices[device]} with id: ${device}"
		} else {
			log.debug "found ${state.hiveHeatingDevices[device]} with id ${device} already exists"
		}

	}
}

def addHotWater() {
	updateDevices()

	selectedHotWater.each { device ->

        def childDevice = getChildDevice("${device}")

        if (!childDevice) {
    		log.info("Adding Hive Hot Water device ${device}: ${state.hiveHotWaterDevices[device]}")

        	def data = [
                name: state.hiveHotWaterDevices[device],
				label: state.hiveHotWaterDevices[device],
			]
            childDevice = addChildDevice(app.namespace, "Hive Hot Water", "$device", null, data)
            childDevice.refresh()
			log.debug "Created ${state.hiveHotWaterDevices[device]} with id: ${device}"
		} else {
			log.debug "found ${state.hiveHotWaterDevices[device]} with id ${device} already exists"
		}

	}
}

def addBulb() {
	updateDevices()

	selectedBulb.each { device ->

        def childDevice = getChildDevice("${device}")

        if (!childDevice) {
    		log.debug "Adding Hive Light Dimmable device ${device}: ${state.hiveBulbDevices[device]}"

        	def data = [
                name: state.hiveBulbDevices[device],
				label: state.hiveBulbDevices[device],
			]
            
            log.debug data
            
            childDevice = addChildDevice(app.namespace, "Hive Active Light", "$device", null, data)
            childDevice.refresh()
            
			log.debug "Created ${state.hiveBulbDevices[device]} with id: ${device}"
		} else {
			log.debug "found ${state.hiveBulbDevices[device]} with id ${device} already exists"
		}

	}
}

def addTunableBulb() {
	updateDevices()

	selectedTunableBulb.each { device ->

        def childDevice = getChildDevice("${device}")

        if (!childDevice) {
    		log.debug "Adding Hive Light Tuneable device ${device}: ${state.hiveTunableBulbDevices[device]}"

        	def data = [
                name: state.hiveTunableBulbDevices[device],
				label: state.hiveTunableBulbDevices[device],
			]
            
            log.debug data
            
            childDevice = addChildDevice(app.namespace, "Hive Active Light Tuneable", "$device", null, data)
            childDevice.refresh()
            
			log.debug "Created ${state.hiveTunableBulbDevices[device]} with id: ${device}"
		} else {
			log.debug "found ${state.hiveTunableBulbDevices[device]} with id ${device} already exists"
		}

	}
}

def addColourBulb() {
	updateDevices()

	selectedColourBulb.each { device ->

        def childDevice = getChildDevice("${device}")

        if (!childDevice) {
    		log.debug "Adding Hive Light Colour device ${device}: ${state.hiveBulbDevices[device]}"

        	def data = [
                name: state.hiveColourBulb[device],
				label: state.hiveColourBulb[device],
			]
            
            log.debug data
            
            childDevice = addChildDevice(app.namespace, "Hive Active Light Colour Tuneable", "$device", null, data)
            childDevice.refresh()
            
			log.debug "Created ${state.hiveColourBulb[device]} with id: ${device}"
            
		} else {
			log.debug "found ${state.hiveColourBulb[device]} with id ${device} already exists"
		}

	}
}

def addActivePlug() {
	updateDevices()

	selectedActivePlug.each { device ->

        def childDevice = getChildDevice("${device}")

        if (!childDevice) {
    		log.debug "Adding Hive Plug device ${device}: ${state.hiveActivePlugDevices[device]}"

        	def data = [
                name: state.hiveActivePlugDevices[device],
				label: state.hiveActivePlugDevices[device],
			]
            
            log.debug data
            
            childDevice = addChildDevice(app.namespace, "Hive Active Plug", "$device", null, data)
            childDevice.refresh()
            
			log.debug "Created ${state.hiveActivePlugDevices[device]} with id: ${device}"
		} else {
			log.debug "found ${state.hiveActivePlugDevices[device]} with id ${device} already exists"
		}

	}
}

def refreshDevices() {
	log.info("Refreshing all devices...")
	getChildDevices().each { device ->
		device.refresh()
	}
}

def devicesList() {
	logErrors([]) {
		def resp = apiGET("/products")
		if (resp.status == 200) {
			return resp.data
		} else {
			log.error("Non-200 from device list call. ${resp.status} ${resp.data}")
			return []
		}
	}
}

def getDeviceStatus(id) {
	def retVal = []
	def resp = apiGET("/products")
	if (resp.status == 200) {
		resp.data.eachWithIndex { currentDevice, i ->
        	if(currentDevice.id == id || (currentDevice.type + "/" + currentDevice.id) == id) { 
                retVal = resp.data[i]
            }
        }
                
	} else {
		log.error("Non-200 from device list call. ${resp.status} ${resp.data}")
	}
    return retVal
}

def apiGET(path, body = [:]) {
	try {
    	if(!isLoggedIn()) {
			log.debug "Need to login"
			getBeekeeperAccessToken()
		}
        log.debug("Beginning API GET: ${apiBeekeeperUKURL(path)}, ${apiRequestHeaders()}")

        httpGet(uri: apiBeekeeperUKURL(path), contentType: 'application/json', headers: apiRequestHeaders()) {response ->
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
    	if(!isLoggedIn()) {
			log.debug "Need to login"
			getBeekeeperAccessToken()
		}
		log.debug("Beginning API POST: ${path}, ${body}")

		httpPostJson(uri: apiBeekeeperUKURL(path), body: body, headers: apiRequestHeaders() ) {response ->
			logResponse(response)
			return response
		}
	} catch (groovyx.net.http.HttpResponseException e) {
		logResponse(e.response)
		return e.response
	}
}

def getBeekeeperAccessToken() {
	try {
    	def params = [
			uri: apiBeekeeperURL('/global/login'),
        	contentType: 'application/json',
        	headers: [
              'Content-Type': 'application/json'
        	],
        	body: [
        		username: settings.username,
                password: settings.password,
                devices: false,
                products: false     	
    		]
        ]

		state.cookie = ''

		httpPostJson(params) {response ->
			log.debug "Request was successful, $response.status"
			log.debug response.headers

        	state.cookie = response?.headers?.'Set-Cookie'?.split(";")?.getAt(0)
			log.debug "Adding cookie to collection: $cookie"
        	log.debug "auth: $response.data"
			log.debug "cookie: $state.cookie"
        	log.debug "sessionid: ${response.data.token}"

        	state.beekeeperAccessToken = response.data.token
        	// set the expiration to 5 minutes
			state.beekeeperAccessToken_expires_at = new Date().getTime() + 300000
            state.loginerrors = null
		}
    } catch (groovyx.net.http.HttpResponseException e) {
    	state.beekeeperAccessToken = null
        state.beekeeperAccessToken_expires_at = null
   		state.loginerrors = "Error: ${e.response.status}: ${e.response.data}"
    	logResponse(e.response)
		return e.response
    }
}

def apiRequestHeaders() {
	return [
        'authorization': "${state.beekeeperAccessToken}"
    ]
}

def isLoggedIn() {
	state.remove("hiveAccessToken")
	log.debug "Calling isLoggedIn()"
	log.debug "isLoggedIn state $state.beekeeperAccessToken"
	if(!state.beekeeperAccessToken) {
		log.debug "No state.beekeeperAccessToken"
		return false
	}

	def now = new Date().getTime()
    return state.beekeeperAccessToken_expires_at > now
}


def isTmaAppInst() {
	def chldCnt = 0
	childApps?.each { cApp ->
//        if(cApp?.name != getWatchdogAppChildName()) { chldCnt = chldCnt + 1 }
		chldCnt = chldCnt + 1
	}
	return (chldCnt > 0) ? true : false
}

def logResponse(response) {
	log.info("Status: ${response.status}")
	log.info("Body: ${response.data}")
}

def logErrors(options = [errorReturn: null, logObject: log], Closure c) {
	try {
		return c()
	} catch (groovyx.net.http.HttpResponseException e) {
		log.error("got error: ${e}, body: ${e.getResponse().getData()}")
		if (e.statusCode == 401) { // token is expired
			state.remove("beekeeperAccessToken")
			log.warn "Access token is not valid"
		}
		return options.errorReturn
	} catch (java.net.SocketTimeoutException e) {
		log.warn "Connection timed out, not much we can do here"
		return options.errorReturn
	}
}

def appName() 		{ return "${parent ? "Hive Mode Automation" : "Hive (Connect)"}" }