/**
 *  Host Pinger - SmartThings
 *
 *  Copyright 2016 Jake Tebbett
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
 * Icon By: Maxim Basinski: https://www.iconfinder.com/vasabii
 * 
 * VERSION CONTROL
 * ###############
 *
 *	28/10/16	1.0		Release Version
 *  29/10/16	1.1		Added child app and triggering of external switches
 *	30/10/16	1.2		Removed direct triggering of child devices, Child app now includes delay on 'Offline'
 *	01/12/16	1.3		Untested fix for null value logging error
 * 	19/01/17	1.4		Added setup instrucions in app and cosmetic changes
 *	24/02/17	1.5		Added exact config details for EXE in to live logging
 *	25/02/17	1.6		Fixed bug in last event logging
 *
 */

definition(
    name: "Host Pinger${parent ? " - Child" : ""}",
    namespace: "jebbett",
    author: "Jake Tebbett",
    description: "Capture ping result events to control virtual switches",
    category: "My Apps",
    iconUrl: "https://raw.githubusercontent.com/jebbett/STHostPinger/master/icons/icon.png",
    iconX2Url: "https://raw.githubusercontent.com/jebbett/STHostPinger/master/icons/icon.png",
    iconX3Url: "https://raw.githubusercontent.com/jebbett/STHostPinger/master/icons/icon.png",
    parent: parent ? "jebbett.Host Pinger" : null,
    singleInstance: true,
    oauth: [displayName: "HostPingState", displayLink: ""])


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
    if(parent){
	    if(state.hostState == null){
            state.hostState = "unknown"
        }
        app.updateLabel("${settings.appName} [${state.hostState}]")
    }else{
    	generateAccessToken()
    	logWriter("URL FOR USE IN PLEX2SMARTTHINGS EXE:\n"+
        		"<!ENTITY accessToken '${state.accessToken}'>\n"+
				"<!ENTITY appId '${app.id}'>\n"+
				"<!ENTITY ide '${getApiServerUrl()}'>")
    	if(state.lastEvent == null){state.lastEvent = "No event recieved, please ensure that config.config is setup correctly"}
   	}
}

def generateAccessToken() {
	if (!state.accessToken) {
		try {
        	createAccessToken()
		} catch(e) {
        	state.installedOK = "No"
        	log.error "Error: $e"
			return false
		}
	}
    state.installedOK = "Yes"
    return true
}

preferences {
	page(name: "pageMain")
    page(name: "pageChild")
	page(name: "mainMenu")
    page(name: "lastEvt")
    page(name: "EndPointInfo")
    page(name: "pageDevice")
    page(name: "pageDevDetails")
    page(name: "pageDevDelete")
    page(name: "pageDevAdd")
    page(name: "pageTrigger")
    
}

// MAIN PAGE
def pageMain() {
	parent ? pageChild() : mainMenu()
}

def mainMenu() {

    dynamicPage(name: "mainMenu", title: "", install: true, uninstall: true, submitOnChange: true) {              
       	section("Host Setup") {
            href(name: "pageTrigger", title: "Hosts", required: false, page: "pageTrigger", description: "")
    	}
        section("Virtual Devices (optional)"){
        	href(name: "pageDevice", title: "Create & Manage Virtual Devices", required: false, page: "pageDevice", description: "")
        }
        section("Instructions"){
            paragraph "1. Either create a virtual device in this app using a link above or decide to control an existing switch"
            paragraph "2. Add a new host under the hosts section and then set the trigger as either the virtual device you created or an existing switch"
        }
	    section(title: "ADVANCED") {
           	href(name: "LastEvent", title: "Events Recieved", required: false, page: "lastEvt", description: "")
           	href(name: "Setup Details", title: "Endpoint Setup Details", required: false, page: "EndPointInfo", description: "")
            input "debugLogging", "bool", title: "Debug Logging", required: false, defaultValue: false, submitOnChange: true
    	}
	}
}

def lastEvt() {

    dynamicPage(name: "lastEvt", title: "Last Event", install: false, uninstall: false) {        
        section(title: "Details of Last Event Recieved") {
            input "evtLogNum", "number", title: "Number Of Rows To Log", required: true, defaultValue: 20, submitOnChange: false
        	paragraph "${updateLog("get", "Status", settings?.evtLogNum ?:0, null)}"
            logWriter(updateLog("get", "Status", settings?.evtLogNum, null))
        }
    }
}

def EndPointInfo() {

    dynamicPage(name: "EndPointInfo", title: "End Point Information", install: false, uninstall: false) {
    	section(title: "App ID") {
        	paragraph "$app.id"
        }
        section(title: "Access Token") {
        	if(!state.accessToken){
        		paragraph("You will need to enable OAuth in IDE, this can be found here:\n'My SmartApps' > App Settings", title: "OAuth Not Enabled", required: true, state: null)
        	}else{
        		paragraph "$state.accessToken"
        	}
        }
    }
}

def updateLog(command, name, length, event){

    def logName = "log$name" as String									// Add log prefix
    if(settings?.length == null || length == 0){state.remove(logName); return "No Data"}		// If length set to 0, delete state
	if(!state."$logName"){state."$logName" = []}						// If list state new, create blank list
	def tempList = state."$logName"										// Create a temp List
	
	// SET OR GET
    switch(command) {    
       	case "set":
        	if(!length || tempList.size() < length){length = tempList.size()+1}	// Get valid trim length if short
            tempList.add(0,"${new Date(now()).format("dd MMM HH:mm", location.timeZone)} - ${event}") // Add to top of tempList
            state."$logName" = tempList.subList(0, length)
        break;
        
        case "get":
        	if(!length || tempList.size() < length){length = tempList.size()}	// Get valid trim length if short
        	def formattedList = ""
            tempList = tempList.subList(0, length)
            tempList.each { item ->
            	if(formattedList == ""){
                	formattedList = item
                }else{
            		formattedList = formattedList + "\n" + item
                }
            }
            return formattedList
        break;
    }
}



//// TRIGGER CHILD APP

def pageTrigger() {
	dynamicPage(name: "pageTrigger", title: "Hosts", install: true, uninstall: false) {
		section() {
    		app(name:"HostPingerChild", title:"Add Host..", appName: "Host Pinger", namespace: "jebbett", multiple: true, uninstall: true, image: "https://raw.githubusercontent.com/jebbett/STHostPinger/master/icons/add_48.png")
        }
  	}
}


def pageChild() {
	dynamicPage(name: "pageChild", title: "Host Details", install: true, uninstall: true) {
		section() {
            input "appName", type: "text", title: "Name", required:true, submitOnChange: true
            input "hostName", type: "text", title: "IP, Host or URL", required:false
            input "hostSwitch", "capability.switch", title:"Turn This Device On/Off With Status", multiple: true, required: false
            input "hostDelay", type: "number", title: "Delay going offline (seconds)", required:true, defaultValue: 0
            paragraph "Offline delay can help to avoid a false negative or where a device briefly disconnects from the network, this delay should either be 0 to report actual results or should exceed your polling interval to handle errors"
		}
  	}
}


def AppCommandRecieved(command, host){

	if (settings?.hostName == host){
    	if(command == "online"){
            hostSwitch?.on()
            unschedule()
            logWriter("Is Online")
        }else{
            if (settings?.hostDelay == "0"){
            	commandOffline()
            }else{
            	def theDelay = settings.hostDelay as int
            	runIn(theDelay, commandOffline)
                logWriter("Delayed Off Line Request Recieved")
            }
        }    	
        state.hostState = command
    	app.updateLabel("${appName} [${command}]")
    }
}

def commandOffline(){
    hostSwitch?.off()
    logWriter("Is Offline")
}

//// VIRTUAL DEVICE

def pageDevice() {

    dynamicPage(name: "pageDevice", title: "Create Device", install: false, uninstall: false) {        
	section() {
        
        def greenOrRed = ""
        def i = 1 as int
        
        getSortedDevices().each { dev ->            

        	if(dev.switchState?.value == null){greenOrRed = "https://raw.githubusercontent.com/jebbett/STHostPinger/master/icons/unknown.png"}
            else if(dev.switchState.value == "on"){greenOrRed = "https://raw.githubusercontent.com/jebbett/STHostPinger/master/icons/circle_green.png"}
            else{greenOrRed = "https://raw.githubusercontent.com/jebbett/STHostPinger/master/icons/circle_red.png"}
            
        	href(name: "pageDevDetails$i", title:"$dev.label", description: "", params: [devi: dev.deviceNetworkId, devstate: dev.switchState?.value], page: "pageDevDetails", required: true, state: "complete", image: "$greenOrRed")
            i++
            }
        }
        section(title: "Please ensure that the custom device type is installed!"){
        	href(name: "pageDevDetails", title:"Create New Device", description: "", params: [devi: false], page: "pageDevDetails", required: true, state: "complete", image: "https://raw.githubusercontent.com/jebbett/STHostPinger/master/icons/add_48.png")
    	}
	}
}

private getSortedDevices() {
	return getChildDevices().sort{it.displayName}
}

def pageDevDetails(params) {    
    dynamicPage(name: "pageDevDetails", title: "Device Details", install: false, uninstall: false) {
		if(params.devi){
			section("Status") {                
                if(params.devstate == null){paragraph("Device Status: No Status Recieved - Check config.config", required: false)}
            	else if(params.devstate == "on"){paragraph("Device Status: Online", required: true, state: "complete")}
            	else{paragraph("Device Status: Offline", required: true)}
            }
            section("DELETE") {
            	href(name: "pageDevDelete", title:"DELETE DEVICE", description: "ONLY PRESS IF YOU ARE SURE!", params: [devi: "$params.devi"], page: "pageDevDelete", required: true, state: null, image: "https://raw.githubusercontent.com/jebbett/STHostPinger/master/icons/delete_48.png")
        	}
   		}else{
       		section() {
        		paragraph("Create a new Host Ping Device")
                input "devName", type: "text", title: "Name", required:false, submitOnChange: true
            	href(name: "pageDevAdd", title:"Create Device", description: "", params: [devi: "$params.devi"], page: "pageDevAdd", required: true, state: null, image: "https://raw.githubusercontent.com/jebbett/STHostPinger/master/icons/add_48.png")
        	}
		}    
  	}   
}


def pageDevAdd(params) {
	if(settings.devName){
    	def DeviceID = "HostPingDev:"+settings.devName
		def existingDevice = getChildDevice(DeviceID)
		if(!existingDevice) {
        	def newTrigger = addChildDevice("jebbett", "Host Ping Device", DeviceID, null, [name: "PING: ${settings.devName}", label: "PING: ${settings.devName}"])
		}
        pageDevice()
	}else{
    	dynamicPage(name: "pageDevAdd", title: "Device Details", install: false, uninstall: false) {        
			section() {
            	paragraph("Name not set")
        	}
		}
	}
}

def pageDevDelete(params) {
   	deleteChildDevice(params.devi)
	pageDevice()
}


//// EVENT HANDLING

mappings {
  path("/statechanged/:command") 	{ action: [ GET: "OnCommandRecieved" ] }
}

def OnCommandRecieved() {
	def command = params.command
    def host = params.ipadd
    
    logWriter("Event Recieved: ${command} ${host}")
    updateLog("set", "Status", settings?.evtLogNum, "${host} [${command}]")
        
    childApps.each { child ->
    	child.AppCommandRecieved(command, host)
    }
    
    return
}

////GENERIC

// Debug Logging
private def logWriter(value) {
	if(parent){
    	if(parent.debugLogging) {log.debug "${app.label} >> ${value}"}
    }else{
		if(debugLogging) {log.debug "${app.label} >> ${value}"}
    }
}