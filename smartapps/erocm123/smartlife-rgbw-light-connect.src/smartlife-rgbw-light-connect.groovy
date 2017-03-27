/**
 *  Copyright 2016 Eric Maycock
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
 *  SmartLife RGBW Light (Connect)
 *
 *  Author: Eric Maycock (erocm123)
 *  Date: 2016-06-23
 */

definition(
    name: "SmartLife RGBW Light (Connect)",
    namespace: "erocm123",
    author: "Eric Maycock (erocm123)",
    description: "Service Manager for SmartLife RGBW lights",
    category: "Convenience",
    iconUrl: "https://raw.githubusercontent.com/erocm123/SmartThingsPublic/master/smartapps/erocm123/smartlife-rgbw-light-connect.src/smartlife-rgbw-icon.png",
    iconX2Url: "https://raw.githubusercontent.com/erocm123/SmartThingsPublic/master/smartapps/erocm123/smartlife-rgbw-light-connect.src/smartlife-rgbw-icon-2x.png",
    iconX3Url: "https://raw.githubusercontent.com/erocm123/SmartThingsPublic/master/smartapps/erocm123/smartlife-rgbw-light-connect.src/smartlife-rgbw-icon-3x.png"
)

preferences {
	page(name: "mainPage")
    page(name: "configureProgramMain")
    page(name: "configureProgram")
    page(name: "exportProgram")
    page(name: "importProgram")
    page(name: "importProgramProcess")
    page(name: "configureAction")
    page(name: "configurePDevice")
    page(name: "deletePDevice")
    page(name: "createVirtual")
    page(name: "removeVirtual")
    page(name: "removalPage")
    page(name: "changeName")
    page(name: "createPage")
    page(name: "discoveryPage", title: "Device Discovery", content: "discoveryPage", refreshTimeout:5)
    page(name: "addDevices", title: "Add RGBW Devices", content: "addDevices")
    page(name: "deviceDiscovery")
    page(name: "manuallyAdd")
    page(name: "manuallyAddConfirm")
	page(name: "timeIntervalInput", title: "Only during a certain time") {
		section {
			input "starting", "time", title: "Starting", required: false
			input "ending", "time", title: "Ending", required: false
		}
	}
}

def mainPage() {
	dynamicPage(name: "mainPage", title: "Manage your RGBW devices", nextPage: null, uninstall: true, install: true) {
        section("Configure"){
           href "deviceDiscovery", title:"Discover Devices", description:""//, params: [pbutton: i]
           href "manuallyAdd", title:"Manually Add Device", description:""//, params: [pbutton: i]
        }
        section("Installed Devices"){
        getChildDevices().sort({ a, b -> a["deviceNetworkId"] <=> b["deviceNetworkId"] }).each {
               if(it.typeName != "SmartLife RGBW Virtual Switch"){
                  href "configurePDevice", title:"$it.label", description:"", params: [did: it.deviceNetworkId]
               }
              
        }
        }    
    }
}

def manuallyAdd(){
   dynamicPage(name: "manuallyAdd", title: "Manually add a SmartLife RGBW Controller", nextPage: "manuallyAddConfirm") {
		section {
			paragraph "This process will manually create a SmartLife RGBW Controller based on the entered IP address. The SmartApp needs to then communicate with the controller to obtain additional information from it. Make sure the device is on and connected to your wifi network."
            input "ipAddress", "text", title:"IP Address", description: "", required: false 
		}
    }
}

def manuallyAddConfirm(){
   if ( ipAddress =~ /^(?:[0-9]{1,3}\.){3}[0-9]{1,3}$/) {
       log.debug "Creating RGBW Controller device with dni: ${convertIPtoHex(ipAddress)}:${convertPortToHex("80")}"
       addChildDevice("erocm123", "SmartLife RGBW Controller", "${convertIPtoHex(ipAddress)}:${convertPortToHex("80")}", location.hubs[0].id, [
           "label": "SmartLife RGBW Controller (${ipAddress})",
           "data": [
           "ip": ipAddress,
           "port": "80" 
           ]
       ])
   
       app.updateSetting("ipAddress", "")
            
       dynamicPage(name: "manuallyAddConfirm", title: "Manually add a SmartLife RGBW Controller", nextPage: "mainPage") {
		   section {
			   paragraph "The controller has been added. Press next to return to the main page."
	    	}
       }
    } else {
        dynamicPage(name: "manuallyAddConfirm", title: "Manually add a SmartLife RGBW Controller", nextPage: "mainPage") {
		    section {
			    paragraph "The entered ip address is not valid. Please try again."
		    }
        }
    }
}

def configurePDevice(params){
   def currentDevice
   
   getChildDevices().each {
               if(it.deviceNetworkId == params.did){
                  state.currentDeviceId = it.deviceNetworkId
                  state.currentDisplayName = it.displayName
               }      
   }
   
   
   dynamicPage(name: "configurePDevice", title: "Configure RGBW Controllers created with this app", nextPage: null) {
        if ( state.currentDeviceId =~ /^([0-9A-F]{2}){6}$/) {
		section {
            app.updateSetting("${state.currentDeviceId}_label", getChildDevice(state.currentDeviceId).label)
            input "${state.currentDeviceId}_label", "text", title:"Device Name", description: "", required: false
            href "changeName", title:"Change Device Name", description: "Edit the name above and click here to change it", params: [did: state.currentDeviceId]
        }
        section {
            href "configureProgramMain", title:"Configure Programs", description:"Configure Programs", params: [did: state.currentDeviceId]
		}
        section("Virtual Switches"){
           input "${state.currentDeviceId}_prefix", "text", title: "Virtual Switch Prefix", description: "Prefix for virtual switch names", required: false, defaultValue: "RGBW"
           if(!isVirtualConfigured(state.currentDeviceId)){
              href "createVirtual", title:"Create Virtual Devices", description:"Create virtual devices", params: [did: state.currentDeviceId]
           }else{
              href "removeVirtual", title:"Remove Virtual Devices", description:"Remove virtual devices", params: [did: state.currentDeviceId]
           }
        }
        section {
              href "deletePDevice", title:"Delete $state.currentDisplayName", description: "", params: [did: state.currentDeviceId]
        }
        } else {
            if (getChildDevice(state.currentDeviceId) != null) getChildDevice(state.currentDeviceId).configure()
            section {
                paragraph "Device has not been fully configured. Please make sure the device is powered on and has the correct ip address. When confirmed, please come back to this page."
            }
            section {
              href "deletePDevice", title:"Delete $state.currentDisplayName", description: "", params: [did: state.currentDeviceId]
        }
        }
           
}
}

def deletePDevice(params){
    def childFound = false
    getChildDevices().each {
        if(it.deviceNetworkId != null){
            if(it.deviceNetworkId.startsWith("${state.currentDeviceId}/")){
                childFound = true
            }
        }
    }
    if ( childFound == false ){
    try {
        unsubscribe()
        deleteChildDevice(state.currentDeviceId)
        dynamicPage(name: "deletePDevice", title: "Deletion Summary", nextPage: "mainPage") {
            section {
                paragraph "The device has been deleted. Press next to continue"
            } 
        }
    
	} catch (e) {
        dynamicPage(name: "deletePDevice", title: "Deletion Summary", nextPage: "mainPage") {
            section {
                paragraph "Error: ${(e as String).split(":")[1]}."
            } 
        }
    
    }
    } else {
        dynamicPage(name: "deletePDevice", title: "Deletion Summary", nextPage: "mainPage") {
            section {
                paragraph "Error: there are still virtual switches associated with this device. Please remove them first."
            } 
        }
    }
}

def discoveryPage(){
   return deviceDiscovery()
}

def deviceDiscovery(params=[:])
{
	def devices = devicesDiscovered()
    
	int deviceRefreshCount = !state.deviceRefreshCount ? 0 : state.deviceRefreshCount as int
	state.deviceRefreshCount = deviceRefreshCount + 1
	def refreshInterval = 3
    
	def options = devices ?: []
	def numFound = options.size() ?: 0

	if ((numFound == 0 && state.deviceRefreshCount > 25) || params.reset == "true") {
    	log.trace "Cleaning old device memory"
    	state.devices = [:]
        state.deviceRefreshCount = 0
        app.updateSetting("selectedDevice", "")
    }

	ssdpSubscribe()

	//bridge discovery request every 15 //25 seconds
	if((deviceRefreshCount % 5) == 0) {
		discoverDevices()
	}

	//setup.xml request every 3 seconds except on discoveries
	if(((deviceRefreshCount % 3) == 0) && ((deviceRefreshCount % 5) != 0)) {
		verifyDevices()
	}

	return dynamicPage(name:"deviceDiscovery", title:"Discovery Started!", nextPage:"addDevices", refreshInterval:refreshInterval, uninstall: true) {
		section("Please wait while we discover your RGBW devices. Discovery can take five minutes or more, so sit back and relax! Select your device below once discovered.") {
			input "selectedDevices", "enum", required:false, title:"Select RGBW Device (${numFound} found)", multiple:true, options:options
		}
        section("Options") {
			href "deviceDiscovery", title:"Reset list of discovered devices", description:"", params: ["reset": "true"]
		}
	}
}

Map devicesDiscovered() {
	def vdevices = getVerifiedDevices()
	def map = [:]
	vdevices.each {
		def value = "${it.value.name}"
		def key = "${it.value.mac}"
		map["${key}"] = value
	}
	map
}

def getVerifiedDevices() {
	getDevices().findAll{ it?.value?.verified == true }
}


private discoverDevices() {
	sendHubCommand(new physicalgraph.device.HubAction("lan discovery urn:schemas-upnp-org:device:Basic:1", physicalgraph.device.Protocol.LAN))
}

def configureProgramMain(){
   dynamicPage(name: "configureProgramMain", title: "Choose which program you would like to configure", nextPage: null) {
		section {
   for (int i = 1; i <= 6; i++){
           def myDescription = ""
           if(settings["${state.currentDeviceId}_programs_${i}_name"] != null) myDescription = settings["${state.currentDeviceId}_programs_${i}_name"] 
              href "configureProgram", title:"Program $i", description: myDescription, params: [pnumber: i]
           }
           }
}}

def createVirtual(){
   dynamicPage(name: "createVirtual", title: "Associate your device's programs with virtual switches", nextPage: "createPage") {
		section {
            
			paragraph "This process will create six virtual switches and associate them with the LED strip's programs. You can then use the programs in other automations."
            def switchNames = ""
            for (int i = 1; i <= 6; i++){
               if(settings["${state.currentDeviceId}_programs_${i}_name"] == null || settings["${state.currentDeviceId}_programs_${i}_name"] == ""){
                  switchNames = switchNames + (settings["${state.currentDeviceId}_prefix"] != null ? settings["${state.currentDeviceId}_prefix"] : '') + " Program $i\r\n"
               }else{
                  switchNames = switchNames + (settings["${state.currentDeviceId}_prefix"] != null ? settings["${state.currentDeviceId}_prefix"] : '') + " " + settings["${state.currentDeviceId}_programs_${i}_name"] + "\r\n"
               }  
            }
            paragraph "The following switches will be created:\r\n\r\n" + switchNames
		}
    }
}

def createPage(){
   dynamicPage(name: "createPage", title: "Devices have been created", nextPage: "mainPage", createVirtualDevice())
}

def removeVirtual(){
   def switchNames = ""
   dynamicPage(name: "removeVirtual", title: "Remove the virtual switches created by this app", nextPage: "removalPage") {
		section {
			paragraph "This process will remove the virtual switches created by this program. Press next to continue"
            getChildDevices().each {
               if(it.deviceNetworkId != null){
               if(it.deviceNetworkId.startsWith("${state.currentDeviceId}/")){
               switchNames = switchNames + it.displayName + "\r\n"
               }
               }
            }
            paragraph "The following virtual switches will be removed:\r\n\r\n" + switchNames
		}
    }
}

def removalPage(){
   dynamicPage(name: "removalPage", title: "Devices have been removed", nextPage: "mainPage", removeVirtualDevice()) 
}

def createVirtualDevice() {
    if(!isVirtualConfigured(state.currentDeviceId)){
    def switchName
    for (int i = 1; i <= 6; i++){
       if(settings["${state.currentDeviceId}_programs_${i}_name"] == null || settings["${state.currentDeviceId}_programs_${i}_name"] == ""){
            switchName = (settings["${state.currentDeviceId}_prefix"] == null ? "" : settings["${state.currentDeviceId}_prefix"]) + " Program $i"
       }else{
            switchName = (settings["${state.currentDeviceId}_prefix"] == null ? "" : settings["${state.currentDeviceId}_prefix"]) + " " + settings["${state.currentDeviceId}_programs_${i}_name"]
       }  
       def child = addChildDevice("erocm123", "SmartLife RGBW Virtual Switch", getDeviceID(i), null, [name: getDeviceID(i), label: switchName, completedSetup: true])
    }
    return {
		section {
			paragraph "Devices have been configured. Press next to go to the main page."
		}
    }
    }else{
    return {
		section {
		   paragraph "Devices have already been configured."
		}
    }}
}

def configured() {
	return buttonDevice || buttonConfigured(1) || buttonConfigured(2) || buttonConfigured(3) || buttonConfigured(4) || buttonConfigured(5) || buttonConfigured(6) || buttonConfigured(7) || buttonConfigured(8)
}

def buttonConfigured(idx) {
	return settings["lights_$idx"]
}

def isConfigured(){
   if(getChildDevices().size() > 0) return true else return false
}

def isVirtualConfigured(did){ 
    //if(getChildDevices().findAll { !it?.contains(did) }.size() > 0) return true else return false
    def foundDevice = false
    getChildDevices().each {
       if(it.deviceNetworkId != null){
       if(it.deviceNetworkId.startsWith("${did}/")) foundDevice = true
       }
    }
    return foundDevice
}

def removeVirtualDevice() {
    try {
    unsubscribe()
    getChildDevices().each {
        if(it.deviceNetworkId.startsWith("${state.currentDeviceId}/")) deleteChildDevice(it.deviceNetworkId)
    }
    
    return {
    section {
			paragraph "Devices have been removed. Press next to go to the main page."
		}
    }
	} catch (e) {
    
    return {
    section {
			paragraph "Error: ${(e as String).split(":")[1]}."
		}
    }
    }
}

private virtualCreated(number) {
    if (getChildDevice(getDeviceID(number))) {
        return true
    } else {
        return false
    }
}

private getDeviceID(number) {
    return "${state.currentDeviceId}/${app.id}/${number}"
}

def configureProgram(params){
   if (params.pnumber != null) state.currentProgram = params.pnumber.toInteger() //log.debug "$params.pbutton"
   if (settings["importMe"] != "" && settings["importMe"] != null) {
      def t = settings["importMe"].split("_")
      def numberOfActions = 0
      app.updateSetting("${state.currentDeviceId}_programs_${state.currentProgram}_name", t[0].split(",")[0])
      app.updateSetting("${state.currentDeviceId}_programs_${state.currentProgram}_off", t[0].split(",")[1])
      app.updateSetting("${state.currentDeviceId}_programs_${state.currentProgram}_numberOfActions", t[0].split(",")[2])
      app.updateSetting("${state.currentDeviceId}_programs_${state.currentProgram}_repeat", t[0].split(",")[3])
      t[1].split(";").each() {
         numberOfActions++
         app.updateSetting("${state.currentDeviceId}_programs_${state.currentProgram}_${numberOfActions}_color", it.split("\\.")[0])
         if (it.split("\\.")[0] != "Custom") {
            app.updateSetting("${state.currentDeviceId}_programs_${state.currentProgram}_${numberOfActions}_lightLevel", it.split("\\.")[1])
         } else {
            app.updateSetting("${state.currentDeviceId}_programs_${state.currentProgram}_${numberOfActions}_custom", it.split("\\.")[1])
         }
         app.updateSetting("${state.currentDeviceId}_programs_${state.currentProgram}_${numberOfActions}_transition", it.split("\\.")[2])
         if (it.split("\\.")[3].indexOf("-") < 0) {
            app.updateSetting("${state.currentDeviceId}_programs_${state.currentProgram}_${numberOfActions}_random_duration", false)
            app.updateSetting("${state.currentDeviceId}_programs_${state.currentProgram}_${numberOfActions}_duration", it.split("\\.")[3])
         } else {
            app.updateSetting("${state.currentDeviceId}_programs_${state.currentProgram}_${numberOfActions}_random_duration", true)
            app.updateSetting("${state.currentDeviceId}_programs_${state.currentProgram}_${numberOfActions}_min_duration", it.split("\\.")[3].split("-")[0])
            app.updateSetting("${state.currentDeviceId}_programs_${state.currentProgram}_${numberOfActions}_max_duration", it.split("\\.")[3].split("-")[1])
         }
      }
   app.updateSetting("importMe", "")
   }
   dynamicPage(name: "configureProgram", title: "Configure the actions you would like the program to perform.", nextPage: null, uninstall: configured(), install: false) {
        section{
           input "${state.currentDeviceId}_programs_${state.currentProgram}_name", "text", title:"Program Name", required: false
           input "${state.currentDeviceId}_programs_${state.currentProgram}_off", "bool", title:"Power off when program is finished?", required: false, defaultValue: false
        }
        section("Actions") {
                input "${state.currentDeviceId}_programs_${state.currentProgram}_numberOfActions", "enum", title: "Number of Actions?", required: true, submitOnChange: true, options: [
                1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24]
                def configDescription = ""
                for (int i = 1; i <= (settings["${state.currentDeviceId}_programs_${state.currentProgram}_numberOfActions"] as Integer); i++){
                   configDescription = ""
                   if (settings["${state.currentDeviceId}_programs_${state.currentProgram}_${i}_transition"] != null) {
                      configDescription = (settings["${state.currentDeviceId}_programs_${state.currentProgram}_${i}_transition"].toInteger() == 1 ? 'Fade' : 'Flash') + " to "
                   }
                   if (settings["${state.currentDeviceId}_programs_${state.currentProgram}_${i}_color"] != null) { 
                      configDescription = configDescription + settings["${state.currentDeviceId}_programs_${state.currentProgram}_${i}_color"]
                   }
                   if (settings["${state.currentDeviceId}_programs_${state.currentProgram}_${i}_lightLevel"] != null) { 
                      configDescription = configDescription + " (" + settings["${state.currentDeviceId}_programs_${state.currentProgram}_${i}_lightLevel"] + "%) "
                   }
                   if (settings["${state.currentDeviceId}_programs_${state.currentProgram}_${i}_random_duration"]?.toBoolean()) { 
                       if (settings["${state.currentDeviceId}_programs_${state.currentProgram}_${i}_min_duration"] != null && settings["${state.currentDeviceId}_programs_${state.currentProgram}_${i}_max_duration"] != null){
                           configDescription = configDescription + "for a random time between " + settings["${state.currentDeviceId}_programs_${state.currentProgram}_${i}_min_duration"] + " & " + settings["${state.currentDeviceId}_programs_${state.currentProgram}_${i}_max_duration"] + " milliseconds"
                       }
                   } else {
                       if (settings["${state.currentDeviceId}_programs_${state.currentProgram}_${i}_duration"] != null) {
                           configDescription = configDescription + "for " + settings["${state.currentDeviceId}_programs_${state.currentProgram}_${i}_duration"] + " milliseconds"
                       }
                   }
                   if (configDescription == ""){
                      configDescription = "Click to configure"
                   }
                   href "configureAction", title:"Configure Action $i", description:"$configDescription", params: [pnumber: state.currentProgram, paction: i]
                }
        }
        section("Repeat"){
           input "${state.currentDeviceId}_programs_${state.currentProgram}_repeat", "enum", title: "Number of times to repeat?", required: false, options: [["-1":"Forever"],[0:"0"],[1:"1"],[2:"2"],[3:"3"],[4:"4"],[5:"5"],[6:"6"],[7:"7"],[8:"8"],[9:"9"]]
        }
        section("Import/Export"){
           href "exportProgram", title:"Export Program", description:"Export the program string"
           href "importProgram", title:"Import Program", description:"Import a program string"
           
        }
     }
}

def exportProgram(){
   dynamicPage(name: "exportProgram", title: "Export the string for this program", nextPage: null) {
		section {
            def programString = ""
			paragraph "Copy the string below to import into another program"
            programString += settings["${state.currentDeviceId}_programs_${state.currentProgram}_name"] + ","
            programString += settings["${state.currentDeviceId}_programs_${state.currentProgram}_off"]?.toString() + ","
            programString += settings["${state.currentDeviceId}_programs_${state.currentProgram}_numberOfActions"] + ","
            programString += settings["${state.currentDeviceId}_programs_${state.currentProgram}_repeat"] + "_"
            
            for (int i = 1; i <= (settings["${state.currentDeviceId}_programs_${state.currentProgram}_numberOfActions"] as Integer); i++){
               programString += settings["${state.currentDeviceId}_programs_${state.currentProgram}_${i}_color"] + "."
               if (settings["${state.currentDeviceId}_programs_${state.currentProgram}_${i}_color"] == "Custom") {
                  programString += settings["${state.currentDeviceId}_programs_${state.currentProgram}_${i}_custom"] + "."
               } else {
                  programString += settings["${state.currentDeviceId}_programs_${state.currentProgram}_${i}_lightLevel"] + "."
               }
               programString += settings["${state.currentDeviceId}_programs_${state.currentProgram}_${i}_transition"] + "."
               if (settings["${state.currentDeviceId}_programs_${state.currentProgram}_${i}_random_duration"]?.toBoolean()) {
                  programString += settings["${state.currentDeviceId}_programs_${state.currentProgram}_${i}_min_duration"] + "-"
                  programString += settings["${state.currentDeviceId}_programs_${state.currentProgram}_${i}_max_duration"] + ";"
               } else {
                  programString += settings["${state.currentDeviceId}_programs_${state.currentProgram}_${i}_duration"] + ";"
               }
            }
            programString = programString.substring(0, programString.length() - 1)
            
            app.updateSetting("exportMe", programString)
            input "exportMe", "text", title:"Program String", required: false
		}
    }
}

def importProgram(params){
   dynamicPage(name: "importProgram", title: "Import a program", nextPage: null) {
		section {
			paragraph "Paste the program string below and hit done"
            input "importMe", "text", title: "Import String", required: false
		}
    }
}

def configureAction(params) {
    if (params.paction != null) state.currentAction = params.paction.toInteger() //log.debug "$params.pbutton"
    dynamicPage(name: "configureAction", title: "Choose the actions for Program${state.currentAction}.",
	uninstall: configured(), getActionSections(state.currentProgram, state.currentAction))
}

def getActionSections(programNumber, actionNumber) {
	return {
		section("Color") {
			input "${state.currentDeviceId}_programs_${programNumber}_${actionNumber}_color", "enum", title: "Light Strip Color?", required: false, multiple:false, submitOnChange: true, options: [
					["Soft White":"Soft White - Default"],
					["White":"White - Concentrate"],
					["Daylight":"Daylight - Energize"],
					["Warm White":"Warm White - Relax"],
					"Red","Green","Blue","Yellow","Orange","Purple","Pink","Cyan","W1","W2","Random","Custom","Off"]
            if (settings["${state.currentDeviceId}_programs_${programNumber}_${actionNumber}_color"] == "Custom"){
                input "${state.currentDeviceId}_programs_${programNumber}_${actionNumber}_custom", "text", title: "Custom Color in Hex (ie ffffff)", submitOnChange: false, required: false
            }
		}
        if (settings["${state.currentDeviceId}_programs_${programNumber}_${actionNumber}_color"] != "Custom"){
            section("Level"){
                input "${state.currentDeviceId}_programs_${programNumber}_${actionNumber}_lightLevel", "number", title: "Light Level?", required: false, range: "1..100"
            }
        }
        section("Transition") {
			input "${state.currentDeviceId}_programs_${programNumber}_${actionNumber}_transition", "enum", title: "Which Transition?", required: false, options: [[1:"Fade"],[2:"Flash"]]
		}
        section ("Duration"){
            input "${state.currentDeviceId}_programs_${programNumber}_${actionNumber}_random_duration", "bool", title: "Random Duration", submitOnChange: true, required: false
            
            if (settings["${state.currentDeviceId}_programs_${programNumber}_${actionNumber}_random_duration"]?.toBoolean()){
                input "${state.currentDeviceId}_programs_${programNumber}_${actionNumber}_min_duration", "number", title: "Minimum Duration (milliseconds)", range: "100..1000000", submitOnChange: false, required: false
                input "${state.currentDeviceId}_programs_${programNumber}_${actionNumber}_max_duration", "number", title: "Maximum Duration (milliseconds)", range: "100..1000000", submitOnChange: false, required: false
            } else {
                input "${state.currentDeviceId}_programs_${programNumber}_${actionNumber}_duration", "number", title: "Duration (milliseconds)", range: "100..1000000", submitOnChange: false, required: false
            }
        }
	}
}

def installed() {
	initialize()
}

def updated() {
	unsubscribe()
    unschedule()
	initialize()
}

def initialize() {
    if(isConfigured()){
    getChildDevices().each {
        if(it.typeName == "SmartLife RGBW Virtual Switch"){
            subscribeToCommand(it, "on", virtualHandler)
            subscribeToCommand(it, "off", virtualHandler)
        }else{
        for (int i = 1; i <= 6; i++){
            subscribe(it, "switch${i}", physicalHandler)
        }}
        
        }
    }
    
    configurePrograms()
    renameSwitches()
    ssdpSubscribe()
    runEvery5Minutes("ssdpDiscover")
    
}

void ssdpSubscribe() {
	//subscribe(location, "ssdpTerm.urn:schemas-upnp-org:device:basic:1", ssdpHandler)
    subscribe(location, "ssdpTerm.urn:schemas-upnp-org:device:Basic:1", ssdpHandler)
}

void ssdpDiscover() {
    sendHubCommand(new physicalgraph.device.HubAction("lan discovery urn:schemas-upnp-org:device:Basic:1", physicalgraph.device.Protocol.LAN))
}

def ssdpHandler(evt) {
    def description = evt.description
    def hub = evt?.hubId

    //def parsedEvent = parseEventMessage(description)
    def parsedEvent = parseLanMessage(description)
    parsedEvent << ["hub":hub]
    
    //log.debug parsedEvent

    def devices = getDevices()
    
    String ssdpUSN = parsedEvent.ssdpUSN.toString()
    
    if (devices."${ssdpUSN}") {
        def d = devices."${ssdpUSN}"
        def child = getChildDevice(parsedEvent.mac)
        def childIP
        def childPort
        if (child) {
            childIP = child.getDeviceDataByName("ip")
            childPort = child.getDeviceDataByName("port").toString()
            log.debug "Device data: ($childIP:$childPort) - reporting data: (${convertHexToIP(parsedEvent.networkAddress)}:${convertHexToInt(parsedEvent.deviceAddress)})."
            if(childIP != convertHexToIP(parsedEvent.networkAddress) || childPort != convertHexToInt(parsedEvent.deviceAddress).toString()){
            //if(child.getDeviceDataByName("ip") != convertHexToIP(parsedEvent.networkAddress) || child.getDeviceDataByName("port") != convertHexToInt(parsedEvent.deviceAddress)){
               log.debug "Device data (${child.getDeviceDataByName("ip")}) does not match what it is reporting(${convertHexToIP(parsedEvent.networkAddress)}). Attempting to update."
               child.sync(convertHexToIP(parsedEvent.networkAddress), convertHexToInt(parsedEvent.deviceAddress).toString())
            }
        }

        if (d.networkAddress != parsedEvent.networkAddress || d.deviceAddress != parsedEvent.deviceAddress) {
            d.networkAddress = parsedEvent.networkAddress
            d.deviceAddress = parsedEvent.deviceAddress
        }
    } else {
        devices << ["${ssdpUSN}": parsedEvent]
    }
}

void verifyDevices() {
log.debug "verifyDevices()"
    def devices = getDevices().findAll { it?.value?.verified != true }
    devices.each {
        def ip = convertHexToIP(it.value.networkAddress)
        def port = convertHexToInt(it.value.deviceAddress)
        String host = "${ip}:${port}"
        sendHubCommand(new physicalgraph.device.HubAction("""GET ${it.value.ssdpPath} HTTP/1.1\r\nHOST: $host\r\n\r\n""", physicalgraph.device.Protocol.LAN, host, [callback: deviceDescriptionHandler]))
    }
}

def getDevices() {
    state.devices = state.devices ?: [:]
    //state.devices = [:] ?: [:]
}

void deviceDescriptionHandler(physicalgraph.device.HubResponse hubResponse) {
	log.trace "description.xml response (application/xml)"
	def body = hubResponse.xml
	if (body?.device?.modelName?.text().startsWith("SmartLife RGBW") || body?.device?.modelName?.text().startsWith("AriLux")) {
		def devices = getDevices()
		def device = devices.find {it?.key?.contains(body?.device?.UDN?.text())}
		if (device) {
			device.value << [name:body?.device?.friendlyName?.text() + " (" + convertHexToIP(hubResponse.ip) + ")", serialNumber:body?.device?.serialNumber?.text(), verified: true]
        } else {
			log.error "/description.xml returned a device that didn't exist"
		}
	}
}

def addDevices() {
    def devices = getDevices()
    def sectionText = ""
    
    selectedDevices.each { dni ->bridgeLinking
        def selectedDevice = devices.find { it.value.mac == dni }
        def d
        if (selectedDevice) {
            d = getChildDevices()?.find {
                it.deviceNetworkId == selectedDevice.value.mac
            }
        }
        
        if (!d && selectedDevice != null) {
            log.debug "Creating RGBW Controller device with dni: ${selectedDevice.value.mac}"
            addChildDevice("erocm123", "SmartLife RGBW Controller", selectedDevice.value.mac, selectedDevice?.value.hub, [
                "label": selectedDevice?.value?.name ?: "SmartLife RGBW Controller",
                "data": [
                    "mac": selectedDevice.value.mac,
                    "ip": convertHexToIP(selectedDevice.value.networkAddress),
                    "port": "" + Integer.parseInt(selectedDevice.value.deviceAddress,16)
                ]
            ])
            sectionText = sectionText + "Succesfully added RGBW device with ip address ${convertHexToIP(selectedDevice.value.networkAddress)} \r\n"
        }
        
	} 
        return dynamicPage(name:"addDevices", title:"Devices Added", nextPage:"mainPage",  uninstall: true) {
        if(sectionText != ""){
		section("Add RGBW Results:") {
			paragraph sectionText
		}
        }else{
        section("No devices added") {
			paragraph "All selected devices have previously been added"
		}
        }
}
    }
    
def changeName(params){
    def thisDevice = getChildDevice(state.currentDeviceId)
    thisDevice.label = settings["${state.currentDeviceId}_label"]

    dynamicPage(name: "changeName", title: "Change Name Summary", nextPage: "mainPage") {
	    section {
            paragraph "The device has been renamed. Press \"Next\" to continue"
        }
    }
}

def renameSwitches(){
    def switchNumber
    def switchName
    def physicalNetworkId
    getChildDevices().each {
       if(it.typeName == "SmartLife RGBW Virtual Switch"){
       physicalNetworkId = it.deviceNetworkId.split("/")[0]
       switchNumber = it.deviceNetworkId.split("/")[2]
       if(settings["${physicalNetworkId}_programs_${switchNumber}_name"] == null || settings["${physicalNetworkId}_programs_${switchNumber}_name"] == ""){
            switchName = (settings["${physicalNetworkId}_prefix"] == null ? "" : settings["${physicalNetworkId}_prefix"]) + " Program $switchNumber"
       }else{
            switchName = (settings["${physicalNetworkId}_prefix"] == null ? "" : settings["${physicalNetworkId}_prefix"]) + " " + settings["${physicalNetworkId}_programs_${switchNumber}_name"]
       }  
       if(it.displayName != switchName) it.displayName = switchName
       }
    }
}

def uninstalled() {
    unsubscribe()
    getChildDevices().each {
        deleteChildDevice(it.deviceNetworkId)
    }
}

def configurePrograms(){
   getChildDevices().each {
               if(it.typeName != "SmartLife RGBW Virtual Switch"){
                  
   for (int i = 1; i <= 6; i++){
   def programString = ""
   def transition = ""
      for (int j = 1; j <= (settings["${it.deviceNetworkId}_programs_${i}_numberOfActions"] as Integer); j++){
         def color
         if (settings["${it.deviceNetworkId}_programs_${i}_${j}_color"] != null && ((settings["${it.deviceNetworkId}_programs_${i}_${j}_color"] != "Custom" && settings["${it.deviceNetworkId}_programs_${i}_${j}_lightLevel"] != null) || (settings["${it.deviceNetworkId}_programs_${i}_${j}_color"] == "Custom" && settings["${it.deviceNetworkId}_programs_${i}_${j}_custom"] != null)) && 
             settings["${it.deviceNetworkId}_programs_${i}_${j}_transition"] != null && ((settings["${it.deviceNetworkId}_programs_${i}_${j}_min_duration"] != null && settings["${it.deviceNetworkId}_programs_${i}_${j}_max_duration"] != null) || settings["${it.deviceNetworkId}_programs_${i}_${j}_duration"] != null)
             ){
             if (settings["${it.deviceNetworkId}_programs_${i}_${j}_color"] == "Custom"){
                color = settings["${it.deviceNetworkId}_programs_${i}_${j}_custom"]
             } else if (getHexColor(settings["${it.deviceNetworkId}_programs_${i}_${j}_color"]) != "xxxxxx"){
                color = getDimmedColor(getHexColor(settings["${it.deviceNetworkId}_programs_${i}_${j}_color"]), settings["${it.deviceNetworkId}_programs_${i}_${j}_lightLevel"])
             } else {
                color = getHexColor(settings["${it.deviceNetworkId}_programs_${i}_${j}_color"])
             }
             if(settings["${it.deviceNetworkId}_programs_${i}_${j}_color"] == "Soft White" || settings["${it.deviceNetworkId}_programs_${i}_${j}_color"] == "Warm White" || settings["${it.deviceNetworkId}_programs_${i}_${j}_color"] == "W1") {
                transition = getTransition(settings["${it.deviceNetworkId}_programs_${i}_${j}_transition"] as Integer, "w1")
             }else if(settings["${it.deviceNetworkId}_programs_${i}_${j}_color"] == "W2") {
                transition = getTransition(settings["${it.deviceNetworkId}_programs_${i}_${j}_transition"] as Integer, "w2")
             }else{
                transition = getTransition(settings["${it.deviceNetworkId}_programs_${i}_${j}_transition"] as Integer, "rgb")
             }
             if (settings["${it.deviceNetworkId}_programs_${i}_${j}_random_duration"]?.toBoolean()) {
                 programString = programString + transition + color + "~" + settings["${it.deviceNetworkId}_programs_${i}_${j}_min_duration"] + "-" + settings["${it.deviceNetworkId}_programs_${i}_${j}_max_duration"] + "_"
             } else {
                 programString = programString + transition + color + "~" + settings["${it.deviceNetworkId}_programs_${i}_${j}_duration"] + "_"
             }
         } else {
            log.debug "Configuration for this action is incomplete"
         }
      }
      if(programString != ""){
         log.debug programString.substring(0, programString.length() - 1) + "&repeat=" + settings["${it.deviceNetworkId}_programs_${i}_repeat"] + "&off=" + settings["${it.deviceNetworkId}_programs_${i}_off"]
         getChildDevice(it.deviceNetworkId).setProgram(programString.substring(0, programString.length() - 1) + "&repeat=" + settings["${it.deviceNetworkId}_programs_${i}_repeat"], i) + "&off=" + settings["${it.deviceNetworkId}_programs_${i}_off"]
      } 
   }}
   }  
}

def virtualHandler(evt) {
  log.debug "virtualHandler called with event: deviceId ${evt.deviceId} name:${evt.name} source:${evt.source} value:${evt.value} isStateChange: ${evt.isStateChange()} isPhysical: ${evt.isPhysical()} isDigital: ${evt.isDigital()} data: ${evt.data} device: ${evt.device}"
  getChildDevices().each {
        if (evt.deviceId == it.id){
        if (evt.value == "off" && settings["${it.deviceNetworkId.split("/")[0]}_programs_${it.deviceNetworkId.split("/")[2]}_off"]?.toBoolean()){
            getChildDevice(it.deviceNetworkId.split("/")[0])."${evt.value}${it.deviceNetworkId.split("/")[2]}"(-1)
        } else {
            getChildDevice(it.deviceNetworkId.split("/")[0])."${evt.value}${it.deviceNetworkId.split("/")[2]}"()
        }
     }   
  }
}

def physicalHandler(evt) {
  log.debug "physicalHandler called with event:  name:${evt.name} source:${evt.source} value:${evt.value} isStateChange: ${evt.isStateChange()} isPhysical: ${evt.isPhysical()} isDigital: ${evt.isDigital()} data: ${evt.data} device: ${evt.device}"
  for (int i = 1; i <= 6; i++){
       if (evt.name == "switch${i}") {
                getChildDevices().each {
                if (evt.deviceId == it.id) {
                    if(getChildDevice("${it.deviceNetworkId}/${app.id}/${i}")){
                        sendEvent(getChildDevice("${it.deviceNetworkId}/${app.id}/${i}"), [name:"switch", value:"$evt.value", type:"physical"])
                    }
                }
                 
			}
       }
    
}
}

def huesatToRGB(float hue, float sat) {
	while(hue >= 100) hue -= 100
	int h = (int)(hue / 100 * 6)
	float f = hue / 100 * 6 - h
	int p = Math.round(255 * (1 - (sat / 100)))
	int q = Math.round(255 * (1 - (sat / 100) * f))
	int t = Math.round(255 * (1 - (sat / 100) * (1 - f)))
	switch (h) {
		case 0: return [255, t, p]
		case 1: return [q, 255, p]
		case 2: return [p, 255, t]
		case 3: return [p, q, 255]
		case 4: return [t, p, 255]
		case 5: return [255, p, q]
	}
}
def rgbToHex(rgb) {
    def r = hex(rgb.r)
    def g = hex(rgb.g)
    def b = hex(rgb.b)
    def hexColor = "#${r}${g}${b}"
    
    hexColor
}
private hex(value, width=2) {
	def s = new BigInteger(Math.round(value).toString()).toString(16)
	while (s.size() < width) {
		s = "0" + s
	}
	s
}

private getHexColor(value){
def color = ""
  switch(value){
    case "White":
    color = "ffffff"
    break;
    case "Daylight":
    color = "ffffff"
    break;
    case "Soft White":
    color = "ff"
    break;
    case "Warm White":
    color = "ff"
    break;
    case "W1":
    color = "ff"
    break;
    case "W2":
    color = "ff"
    break;
    case "Blue":
    color = "0000ff"
    break;
    case "Green":
    color = "00ff00"
    break;
    case "Yellow":
    color = "ffff00"
    break;
    case "Orange":
    color = "ff5a00"
    break;
    case "Purple":
    color = "5a00ff"
    break;
    case "Pink":
    color = "ff00ff"
    break;
    case "Cyan":
    color = "00ffff"
    break;
    case "Red":
    color = "ff0000"
    break;
    case "Off":
    color = "000000"
    break;
    case "Random":
    color = "xxxxxx"
    break; 
}
   return color
}

private getTransition(value, channel){
  def transition = ""
  if(channel == "w1"){
      switch(value){
        case 1:
        transition = "w~"
        break;
        case 2:
        transition = "x~"
        break;
      }
	} else if(channel == "w2") {
      switch(value){
        case 1:
        transition = "y~"
        break;
        case 2:
        transition = "z~"
        break;
      }
    } else if(channel == "rgb") {
      switch(value){
        case 1:
        transition = "f~"
        break;
        case 2:
        transition = "g~"
        break;
      }
    }
    return transition
}

private getDimmedColor(color, level) {
   if(color.size() > 2){
      def rgb = color.findAll(/[0-9a-fA-F]{2}/).collect { Integer.parseInt(it, 16) }
      def myred = rgb[0]
      def mygreen = rgb[1]
      def myblue = rgb[2]
    
      color = rgbToHex([r:myred, g:mygreen, b:myblue])
      def c = hexToRgb(color)
    
      def r = hex(c.r * (level.toInteger()/100))
      def g = hex(c.g * (level.toInteger()/100))
      def b = hex(c.b * (level.toInteger()/100))

      return "${r + g + b}"
   }else{
      color = Integer.parseInt(color, 16)
      return hex(color * (level.toInteger()/100))
   }

}

def hexToRgb(colorHex) {
	def rrInt = Integer.parseInt(colorHex.substring(1,3),16)
    def ggInt = Integer.parseInt(colorHex.substring(3,5),16)
    def bbInt = Integer.parseInt(colorHex.substring(5,7),16)
    
    def colorData = [:]
    colorData = [r: rrInt, g: ggInt, b: bbInt]
    colorData
}

private def parseEventMessage(String description)
{
	def event = [:]
	def parts = description.split(',')
    
	parts.each
    { part ->
		part = part.trim()
		if (part.startsWith('devicetype:'))
        {
			def valueString = part.split(":")[1].trim()
			event.devicetype = valueString
		}
		else if (part.startsWith('mac:'))
        {
			def valueString = part.split(":")[1].trim()
			if (valueString)
            {
				event.mac = valueString
			}
		}
		else if (part.startsWith('networkAddress:'))
        {
			def valueString = part.split(":")[1].trim()
			if (valueString)
            {
				event.ip = valueString
			}
		}
		else if (part.startsWith('deviceAddress:'))
        {
			def valueString = part.split(":")[1].trim()
			if (valueString)
            {
				event.port = valueString
			}
		}
		else if (part.startsWith('ssdpPath:'))
        {
			def valueString = part.split(":")[1].trim()
			if (valueString)
            {
				event.ssdpPath = valueString
			}
		}
		else if (part.startsWith('ssdpUSN:'))
        {
			part -= "ssdpUSN:"
			def valueString = part.trim()
			if (valueString)
            {
				event.ssdpUSN = valueString
                
                def uuid = getUUIDFromUSN(valueString)
                
                if (uuid)
                {
                	event.uuid = uuid
                }
			}
		}
		else if (part.startsWith('ssdpTerm:'))
        {
			part -= "ssdpTerm:"
			def valueString = part.trim()
			if (valueString)
            {
				event.ssdpTerm = valueString
			}
		}
		else if (part.startsWith('headers'))
        {
			part -= "headers:"
			def valueString = part.trim()
			if (valueString)
            {
				event.headers = valueString
			}
		}
		else if (part.startsWith('body'))
        {
			part -= "body:"
			def valueString = part.trim()
			if (valueString)
            {
				event.body = valueString
			}
		}
	}

	event
}

private String convertHexToIP(hex) {
	[convertHexToInt(hex[0..1]),convertHexToInt(hex[2..3]),convertHexToInt(hex[4..5]),convertHexToInt(hex[6..7])].join(".")
}

private Integer convertHexToInt(hex) {
	Integer.parseInt(hex,16)
}

private String convertIPtoHex(ipAddress) { 
    String hex = ipAddress.tokenize( '.' ).collect {  String.format( '%02x', it.toInteger() ) }.join()
    return hex
}

private String convertPortToHex(port) {
	String hexport = port.toString().format( '%04x', port.toInteger() )
    return hexport
}