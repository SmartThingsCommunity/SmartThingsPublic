/**
 *  KODI Manager
 *
 *  forked from a plex version: https://github.com/iBeech/SmartThings/tree/master/PlexManager
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
definition(
    name: "KODI Manager - Callbacks",
    namespace: "toliver182",
    author: "toliver182",
    description: "Add kodi endpoints",
    category: "Safety & Security",
    iconUrl: "https://raw.githubusercontent.com/xbmc/xbmc/master/media/icon48x48.png",
    iconX2Url: "https://raw.githubusercontent.com/xbmc/xbmc/master/media/icon120x120.png",
    iconX3Url: "https://raw.githubusercontent.com/xbmc/xbmc/master/media/icon256x256.png",
    oauth: true)


preferences {
	page(name: "pgSettings")
    page(name: "pgURL") 
    page(name: "pgLights") 
   }
   
   //PAGES
///////////////////////////////
def pgSettings() {
    dynamicPage(name: "pgSettings", title: "Settings",uninstall: true, install: true) {
      section("Kodi Client"){
      	input "clientName", "text", "title": "Client Name", multiple: false, required: true
  		input "kodiIp", "text", "title": "Kodi IP", multiple: false, required: true
        input "kodiPort", "text", "title": "Kodi port", multiple: false, required: true
    	input "kodiUsername", "text", "title": "Kodi Username", multiple: false, required: false
    	input "kodiPassword", "password", "title": "Kodi Password", multiple: false, required: false
    	input "theHub", "hub", title: "On which hub?", multiple: false, required: true
        }
          section("Configure Lights"){
        	href( "pgLights", description: "Configure lights based on Kodi state", title: "")
        }
        section("View URLs"){
        	href( "pgURL", description: "Click here to view URLs", title: "")
        }
        section("Name")
        {                    label title: "Assign a name", required: false

        }
    }
}

def pgURL(){
    dynamicPage(name: "pgURL", title: "URLs" , uninstall: false, install: true) {
    	if (!state.accessToken) {
        	createAccessToken() 
    	}
    	def url = apiServerUrl("/api/token/${state.accessToken}/smartapps/installations/${app.id}/")
    	section("Instructions") {
            paragraph "This app is designed to work with the xbmc.callbacks2 plugin for Kodi. Please download and install callbacks2 and in its settings assign the following URLs for corresponding events:"
            input "playvalue", "text", title:"Web address to copy for play command:", required: false, defaultValue:"${url}play"
            input "stopvalue", "text", title:"Web address to copy for stop command:", required: false, defaultValue:"${url}stop"
            input "pausevalue", "text", title:"Web address to copy for pause command:", required: false, defaultValue:"${url}pause"
            input "resumevalue", "text", title:"Web address to copy for resume command:", required: false, defaultValue:"${url}resume"
            input "allvalue", "text", title:"All Commands", required: false, defaultValue:"Play:${url}play###Stop:${url}stop###Pause:${url}pause###Resume:${url}resume"


            paragraph "If you have more than one Kodi install, you may install an additional copy of this app for unique addresses specific to each room."
        }
    }
}
def pgLights(){
    dynamicPage(name: "pgLights", title: "Lights", install: false) {
    section("Enable Light Control?") {
        	input "shouldControlLights", "bool", title: "Enable light control?", multiple: false, required: true, submitOnChange: true, defaultValue: false
}
if(shouldControlLights){
        section("Lights to Control") {
            input "switches", "capability.switch", required: true, title: "Which Switches?", multiple: true
        }
        section("Level to set Lights to (101 for last known level):") {
            input "playLevel", "number", required: true, title: "On Playback", defaultValue:"0"
            input "pauseLevel", "number", required: true, title: "On Pause", defaultValue:"40"
            input "resumeLevel", "number", required: true, title: "On Resume", defaultValue:"0"
            input "stopLevel", "number", required: true, title: "On Stop", defaultValue:"101"
        }
        }
        }
        }
//END PAGES
/////////////////////////





def installed() {
	
	log.debug "Installed with settings: ${settings}"
	initialize()
    
}

def initialize() {
checkKodi();

//if the ip changes we need to remove the old device id.
getChildDevices().each { childDevice ->
def deviceNetID = childDevice.deviceNetworkId
log.debug "my id: "+ NetworkDeviceId()
log.debug "net id: " + deviceNetID
if(deviceNetID != NetworkDeviceId()){
log.debug "removing: " + deviceNetID
deleteChildDevice(deviceNetID)
}
}

}

def updated() {
unsubscribe();
/*
getChildDevices().each { childDevice ->
def deviceNetID = childDevice.deviceNetworkId
log.debug "my id: "+ NetworkDeviceId()
log.debug "net id: " + deviceNetID
if(deviceNetID != NetworkDeviceId()){
log.debug "removing: " + deviceNetID
deleteChildDevice(deviceNetID)
}
}*/

initialize()

}

//Incoming state changes from kodi

mappings {

	path("/play") {
		action: [
			GET: "stateIsPlay"
		]
	}
	path("/stop") {
		action: [
			GET: "stateIsStop"
		]
	}
	path("/pause") {
		action: [
			GET: "stateIsPause"
		]
	}
    path("/resume") {
        action: [
            GET: "stateIsResume"
        ]
	}  
}
void stateIsPlay() {
if("$settings.shouldControlLights" == "true"){
    RunCommand(playLevel)
}

	//Code to execute when playback started in KODI
    log.debug "Play command started"
	//Find client
    def children = getChildDevices()
    def KodiClient = children.find{ d -> d.deviceNetworkId.contains(NetworkDeviceId()) }  
    //Set State
    KodiClient.setPlaybackState("playing")
    getPlayingtitle()
}
void stateIsStop() {
if("$settings.shouldControlLights" == "true"){
    RunCommand(stopLevel)
}
	//Code to execute when playback stopped in KODI
    log.debug "Stop command started"
   	//Find client
    def children = getChildDevices()
    def KodiClient = children.find{ d -> d.deviceNetworkId.contains(NetworkDeviceId()) }  
    //Set State
    KodiClient.setPlaybackState("stopped")
 }
void stateIsPause() {
if("$settings.shouldControlLights" == "true"){
    RunCommand(pauseLevel)
}
	//Code to execute when playback paused in KODI
    log.debug "Pause command started"
   	//Find client
    def children = getChildDevices()
    def KodiClient = children.find{ d -> d.deviceNetworkId.contains(NetworkDeviceId()) }  
    //Set State
    KodiClient.setPlaybackState("paused")
    getPlayingtitle()
}
void stateIsResume() {
if("$settings.shouldControlLights" == "true"){
    RunCommand(resumeLevel)
}
	//Code to execute when playback resumed in KODI
    log.debug "Resume command started"
	//Find client
    def children = getChildDevices()
    def KodiClient = children.find{ d -> d.deviceNetworkId.contains(NetworkDeviceId()) }  
    //Set State
    KodiClient.setPlaybackState("playing")
    getPlayingtitle()
}



def response(evt) {	 
    def msg = parseLanMessage(evt.description);
}


//Incoming command handler
def switchChange(evt) {

    // We are only interested in event data which contains 
    if(evt.value == "on" || evt.value == "off") return;   
    
	//log.debug "Kodi event received: " + evt.value;

    def kodiIP = getKodiAddress(evt.value);
    
    // Parse out the new switch state from the event data
    def command = getKodiCommand(evt.value);
   
    //log.debug "state: " + state
    
    switch(command) {
    	case "next":
        	log.debug "Sending command 'next' to " + kodiIP
            next(kodiIP);
        break;
        
        case "previous":
        	log.debug "Sending command 'previous' to " + kodiIP
            previous(kodiIP);
        break;
        
        case "play":
        case "pause":
        	playpause(kodiIP);
        break;
        case "stop":
    		stop(kodiIP);
        break;
        case "scanNewClients":
        	getClients();
            
        case "setVolume":
        	def vol = getKodiVolume(evt.value);
            log.debug "Vol is: " + vol
        	setVolume(kodiIP, vol);
        break;
    }
    
    return;
}



//Child device setup
def checkKodi() {

		log.debug "Checking to see if the client has been added"
    
    	def children = getChildDevices()  ;
  		def childrenEmpty = children.isEmpty();  
      
        
     	def KodiClient = children.find{ d -> d.deviceNetworkId.contains(NetworkDeviceId()) }  
     
        if(!KodiClient){
        log.debug "No Devices found, adding device"
		KodiClient = addChildDevice("toliver182", "Kodi Client", NetworkDeviceId() , theHub.id, [label:"$settings.clientName", name:"$settings.clientName"])
        log.debug "Added Device"
        }
        else
        {
        log.debug "Device Already Added"
        }
        subscribe(KodiClient, "switch", switchChange)
}



//Commands to kodi
def playpause(kodiIP) {
	log.debug "playpausehere"
	def command = "{\"jsonrpc\": \"2.0\", \"method\": \"Player.PlayPause\", \"params\": { \"playerid\": 1 }, \"id\": 1}"
	executeRequest("/jsonrpc", "POST",command);
}

def next(kodiIP) {
	log.debug "Executing 'next'"
	def command = "{\"jsonrpc\": \"2.0\", \"method\": \"Player.GoTo\", \"params\": { \"playerid\": 1, \"to\": \"next\" }, \"id\": 1}"
    executeRequest("/jsonrpc", "POST",command)
}

def stop(kodiIP){
	def command = "{ \"id\": 1, \"jsonrpc\": \"2.0\", \"method\": \"Player.Stop\", \"params\": { \"playerid\": 1 } }"
    executeRequest("/jsonrpc", "POST",command)
}

def previous(kodiIP) {
	log.debug "Executing 'next'"
	def command = "{\"jsonrpc\": \"2.0\", \"method\": \"Player.GoTo\", \"params\": { \"playerid\": 1, \"to\": \"previous\" }, \"id\": 1}"
    executeRequest("/jsonrpc", "POST",command)
}

def setVolume(kodiIP, level) {
//TODO
	def command = "{\"jsonrpc\": \"2.0\", \"method\": \"Application.SetVolume\", \"params\": { \"volume\": "+ level + "}, \"id\": 1}"
    executeRequest("/jsonrpc", "POST",command)
}
def getPlayingtitle(){
def command = "{\"jsonrpc\": \"2.0\", \"method\": \"Player.GetItem\", \"params\": { \"properties\": [\"title\", \"album\", \"artist\", \"season\", \"episode\", \"duration\", \"showtitle\", \"tvshowid\", \"thumbnail\", \"file\", \"fanart\", \"streamdetails\"], \"playerid\": 1 }, \"id\": \"VideoGetItem\"}"
	executeRequest("/jsonrpc", "POST",command);

}

//main command handler
def executeRequest(Path, method, command) {
    log.debug "Sending command to $settings.kodiIp"
	def headers = [:] 
    
	headers.put("HOST", "$settings.kodiIp:$settings.kodiPort")
    if("$settings.kodiUsername" !="" ){
    def basicAuth = basicAuthBase64();
    headers.put("Authorization", "Basic " + basicAuth )
    }else{
    log.debug "No Auth needed"
    }
    headers.put("Content-Type", "application/json")
	try {    
		def actualAction = new physicalgraph.device.HubAction(
		    method: method,
		    path: Path,
            body: command,
		    headers: headers)
			
		sendHubCommand(actualAction)        
	}
	catch (Exception e) {
		log.debug "Hit Exception $e on $hubAction"
	}
}




// Helpers
private String convertIPtoHex(ipAddress) { 
    String hex = ipAddress.tokenize( '.' ).collect {  String.format( '%02x', it.toInteger() ) }.join()
    //log.debug "IP address entered is $ipAddress and the converted hex code is $hex"
    return hex

}

private String convertPortToHex(port) {
    String hexport = port.toString().format( '%04x', port.toInteger() )
    //log.debug hexport
    return hexport
}

def String getKodiCommand(deviceNetworkId) {
	def parts = deviceNetworkId.tokenize('.');
	return parts[1];
}
def String getKodiVolume(evt) {
	def parts = evt.tokenize('.');
	return parts[2];
}
private String NetworkDeviceId(){
    def iphex = convertIPtoHex(settings.kodiIp).toUpperCase()
    def porthex = convertPortToHex(settings.kodiPort).toUpperCase()
    return "$iphex:$porthex" 
}

//Method for encoding username and password in base64
def basicAuthBase64() {
def s ="$settings.kodiUsername:$settings.kodiPassword"
def encoded = s.bytes.encodeBase64();
return encoded
}

def String getKodiAddress(deviceNetworkId) {
def ip = deviceNetworkId.replace("KodiClient:", "");
	def parts = ip.tokenize('.');
	return parts[0] + "." + parts[1] + "." + parts[2] + "." + parts[3];
}

//Lighting control

private void RunCommand(level){
	//Check to see if current mode is in white/black list before we do anything
	if((!onlyModes || location.currentMode in onlyModes) && !(location.currentMode in neverModes)){
    	//Mode is good, go ahead with commands
    	if (level == 101){
            log.debug "Restoring Last Known Light Levels"
            restoreLast(switches)
        }else if (level <= 100){
            log.debug "Setting lights to ${level} %"
            SetLight(switches,level)
    	}
    }
}

private void restoreLast(switchList){
	//This will look for the last external (not from this app) setting applied to each switch and set the switch back to that
	//Look at each switch passed
	switchList.each{sw ->
    	def lastState = LastState(sw) //get Last State
        if (lastState){   //As long as we found one, set it    
            SetLight(sw,lastState)
    	}else{ //Otherwise assume it was off
        	SetLight(sw, "off") 
        }
    }
}

private def LastState(device){
	//Get events for this device in the last day
	def devEvents = device.eventsSince(new Date() - 1, [max: 1000])
    //Find Last Event Where switch was turned on/off/level, but not changed by this app
    //Oddly not all events properly contain the "installedSmartAppId", particularly ones that actual contain useful values
    //In order to filter out events created by this app we have to find the set of events for the app control and the actual action
    //the first 8 char of the event ID seem to be unique much of the time, but the rest seems to be the same for any grouping of events, so match on that (substring)
    //In case the substring fails we will also check for events with similar timestamp (within 8 sec)
    def last = devEvents.find {
        (it.name == "level" || it.name == "switch") && (devEvents.find{it2 -> it2.installedSmartAppId == app.id && (it2.id.toString().substring(8) == it.id.toString().substring(8) || Math.sqrt((it2.date.getTime() - it.date.getTime())**2) < 6000 )} == null)
        }
    //If we found one return the stringValue
    if (last){
    	log.debug "Last External Event - Date: ${last.date} | Event ID: ${last.id} | AppID: ${last.installedSmartAppId} | Description: ${last.descriptionText} | Name: ${last.displayName} (${last.name}) | App: ${last.installedSmartApp} | Value: ${last.stringValue} | Source: ${last.source} | Desc: ${last.description}"
    	//if event is "on" find last externally set level as it could be in an older event
        if(last.stringValue == "on"){
        	devEvents = device.eventsSince(new Date() - 7, [max: 1000]) //Last level set command could have been awhile back, look in last 7 days
            def lastLevel = devEvents.find {
            (it.name == "level") && (devEvents.find{it2 -> it2.installedSmartAppId == app.id && (it2.id.toString().substring(8) == it.id.toString().substring(8) || Math.sqrt((it2.date.getTime() - it.date.getTime())**2) < 6000 )} == null)
            }
        	if(lastLevel){
            	return lastLevel.stringValue 
            }
        }
		return last.stringValue
    }else{
    	return null
    }
}

private void SetLight(switches,value){
	//Set value for one or more lights, translates dimmer values to on/off for basic switches

	//Fix any odd values that could be passed
	if(value.toString().isInteger()){
    	if(value.toInteger() < 0){value = 0}
        if(value.toInteger() > 100){value = 100}
    }else if(value.toString() != "on" && value.toString() != "off"){
    	return //ABORT! Lights do not support commands like "Hamster"
    }
	switches.each{sw ->
    	log.debug "${sw.name} |  ${value}"
    	if(value.toString() == "off" || value.toString() == "0"){ //0 and off are the same here, turn the light off
        	sw.off()
        }else if(value.toString() == "on"  || value.toString() == "100"){ //As stored light level is not really predictable, on should mean 100% for now
        	if(sw.hasCommand("setLevel")){ //setlevel for dimmers, on for basic
            	sw.setLevel(100)
            }else{
            	sw.on()
            }
        }else{ //Otherwise we should have a % value here after cleanup above, use ir or just turn a basic switch on
        	if(sw.hasCommand("setLevel")){//setlevel for dimmers, on for basic
            	sw.setLevel(value.toInteger())
            }else{
            	sw.on()
            }
        }
    }
}