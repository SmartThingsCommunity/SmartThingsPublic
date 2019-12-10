/**
 *  Generic Video Camera
 *
 *  Copyright 2016 Patrick Stuart
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *ocfdevicetype: "oic.d.camera"
 */
metadata {
	definition (name: "Generic Video Camera", namespace: "pstuart", author: "Patrick Stuart") {
		capability "Configuration"
		capability "Video Camera"
		capability "Video Capture"
		capability "Refresh"
		capability "Switch"
        capability "Video Clips" //new
        capability "Image Capture" //new
        capability "Health Check"
        
        attribute "hubactionMode", "string"

		// custom commands
		command "start"
        command "presetCommand"
        command "presetOne"
        command "presetOne"
		command "presetTwo"
		command "presetThree"
        command "burst"
	}

	simulator {
		// TODO: define status and reply messages here
	}

	tiles(scale: 2) {
		multiAttributeTile(name: "videoPlayer", type: "videoPlayer", width: 6, height: 4) {
			tileAttribute("device.switch", key: "CAMERA_STATUS") {
				attributeState("on", label: "Active", icon: "st.camera.dlink-indoor", action: "switch.off", backgroundColor: "#79b821", defaultState: true)
				attributeState("off", label: "Inactive", icon: "st.camera.dlink-indoor", action: "switch.on", backgroundColor: "#ffffff")
				attributeState("restarting", label: "Connecting", icon: "st.camera.dlink-indoor", backgroundColor: "#53a7c0")
				attributeState("unavailable", label: "Unavailable", icon: "st.camera.dlink-indoor", action: "refresh.refresh", backgroundColor: "#F22000")
			}

			tileAttribute("device.errorMessage", key: "CAMERA_ERROR_MESSAGE") {
				attributeState("errorMessage", label: "", value: "", defaultState: true)
			}

			tileAttribute("device.camera", key: "PRIMARY_CONTROL") {
				attributeState("on", label: "Active", icon: "st.camera.dlink-indoor", backgroundColor: "#79b821", defaultState: true)
				attributeState("off", label: "Inactive", icon: "st.camera.dlink-indoor", backgroundColor: "#ffffff")
				attributeState("restarting", label: "Connecting", icon: "st.camera.dlink-indoor", backgroundColor: "#53a7c0")
				attributeState("unavailable", label: "Unavailable", icon: "st.camera.dlink-indoor", backgroundColor: "#F22000")
			}

			tileAttribute("device.startLive", key: "START_LIVE") {
				attributeState("live", action: "start", defaultState: true)
			}

			tileAttribute("device.stream", key: "STREAM_URL") {
				attributeState("activeURL", defaultState: true)
			}
            }
            //new
			carouselTile("cameraDetails", "device.image", width: 3, height: 2) { }
            
        	standardTile("take", "device.image", width: 2, height: 2, canChangeIcon: false, inactiveLabel: true, canChangeBackground: false) {
            state "take", label: "Take", action: "Image Capture.take", icon: "st.camera.camera", backgroundColor: "#FFFFFF", nextState:"taking"
            state "taking", label:'Taking', action: "", icon: "st.camera.take-photo", backgroundColor: "#53a7c0"
            state "image", label: "Take", action: "Image Capture.take", icon: "st.camera.camera", backgroundColor: "#FFFFFF", nextState:"taking"
        }

		standardTile("presetOne", "device.switch6", width: 2, height: 2, canChangeIcon: false,	canChangeBackground: false) {
			state "presetOne", label: "1", action: "presetOne", nextState: "moving"
			state "moving", label: "moving", action:"", backgroundColor: "#53a7c0"
		}
   		standardTile("presetTwo", "device.switch6", width: 2, height: 2, canChangeIcon: false,	canChangeBackground: false) {
			state "presetTwo", label: "2", action: "presetTwo", nextState: "moving"
			state "moving", label: "moving", action:"", backgroundColor: "#53a7c0"
		}
		standardTile("presetThree", "device.switch6", width: 2, height: 2, canChangeIcon: false,	canChangeBackground: false) {
			state "presetThree", label: "3", action: "presetThree", nextState: "moving"
			state "moving", label: "moving", action:"", backgroundColor: "#53a7c0"
        }
    //standardTile("image", "device.image", width: 1, height: 1, canChangeIcon: false, inactiveLabel: true, canChangeBackground: true) {
    //    state "default", label: "", action: "", icon: "st.camera.dropcam-centered", backgroundColor: "#FFFFFF"
   // }
		main("videoPlayer")
		details(["videoPlayer", "cameraDetails", "take", "presetOne", "presetTwo", "presetThree"]) //"image",
}
preferences {
        input "ipport", "String", title: "IP:Port", description: "IP & Port numbers. for capture", required: false
        input "unamepass", "String", title: "username:password", description: "username and pasword for capture", required: false
    }
}

mappings {
   path("/getInHomeURL") {
       action:
       [GET: "getInHomeURL"]
   }
}

def captureClip(data) {
	log.debug "Executing captureClip $data - duration: , pre: , preFetch"
    }

def installed() {
	configure()
}

def updated() {
	configure()
}

def ping() {
	log.debug "ping"
}

// handle commands
def configure() {
	log.debug "Executing 'configure'"
    sendEvent(name:"switch", value: "on")
    sendEvent(name: "stream", value: "")
    sendEvent(name: "DeviceWatch-DeviceStatus", value: "online")
	sendEvent(name: "healthStatus", value: "online")
	sendEvent(name: "DeviceWatch-Enroll", value: "{\"protocol\": \"LAN\", \"scheme\":\"untracked\", \"hubHardwareId\": \"${device.hub.hardwareID}\"}", displayed: false)
}
def startStream(anyvar) {
	log.debug "startstream $anyvar"
    start()
}
def start() {
	/*   
	*/
	log.trace "start()"
	def dataLiveVideo = [
		OutHomeURL  : parent.state.CameraStreamPath,
		InHomeURL   : parent.state.CameraStreamPath,
		ThumbnailURL: "http://cdn.device-icons.smartthings.com/camera/dlink-indoor@2x.png",
		cookie      : [key: "key", value: "value"]
	]

	def event = [
		name           : "stream",
		value          : groovy.json.JsonOutput.toJson(dataLiveVideo).toString(),
		data		   : groovy.json.JsonOutput.toJson(dataLiveVideo),
		descriptionText: "Starting the livestream",
		eventType      : "VIDEO",
		displayed      : false,
		isStateChange  : true
	]
    log.trace "start() $event"
	sendEvent(event)
}

def getInHomeURL() {
	 [InHomeURL: parent.state.CameraStreamPath]
}

def parse(String description) {
log.debug "parsing $description"
    def map = stringToMap(description)
    log.debug "parsing $map"
    if (map.tempImageKey) {
        try {
        	log.info "map with temp Image Key  ${map.tempImageKey}"
            storeTemporaryImage(map.tempImageKey, getPictureName())
            createEvent (name:"image", value: map.tempImageKey)
		} 
        catch (Exception e) {
            log.error e
        }
    }
    else if (map.error) {
        log.error "Error: ${map.error}"
    }
	else { // parse other messages too
    	if (map.headers){ //html
            try { // for encoded html
    			def head = map.headers.decodeBase64()
    			def headdec = new String(head)
    			def body = map.body.decodeBase64()
    			def bodydec = new String(head)
				log.info "HTML encided response ${headdec} , ${bodydec}"
      	  	}
    		catch (Exception e) {
            	log.error e
        	}
        }
        log.warn " parsing something else - $description"
    }
    
}

private getPictureName() {
	log.debug "Used get picture name"
	return java.util.UUID.randomUUID().toString().replaceAll('-', '')
}
def doCmd(path, opt){
	def headers = [:]
	def host = 	settings.ipport
    def idhex = getHostAddress(host)
    device.deviceNetworkId = "$idhex" 
	def authorizationClear = settings.unamepass
    def authorizationEncoded = "Basic " + authorizationClear.encodeAsBase64().toString()
    
    headers.put("Authorization", authorizationEncoded)
    headers.put("HOST", host)
    
    def hubAction = new physicalgraph.device.HubAction(
        method: "POST",
        path: path ,
        headers: headers//,
        //cookie: [key: "key", value: "value"]
	)
	log.trace "doCmd - Hubaction is = $hubAction, OPT is = $opt"
    if(opt==true) { //if true ie from take command save image
    	hubAction.options = [outputMsgToS3:true] 
    } 
    if(opt==false) { // dont bother as no image returend
    	hubAction.options = [outputMsgToS3:false] 
    }
    //hubAction.options = [outputMsgToS3:true]
    return hubAction
}

def presetCommand(preset){
log.trace "presetCmd($preset)"
presetCmd(preset)
}

def presetCmd(preset) {
	log.trace "presetCmd($preset)"
    def parts = settings.unamepass.split(":")
    def uname = parts[0]
    def pass = parts[1]
    //TENVIS JPT3815, DericamP2, onvif, LoftekSentinel, Keekoon
   	def presetup = (preset*2) + 29 // Presets must be translated into values internal to the camera. Those values are: 31,33,35,37,39,41,43,45 for presets 1-8 respectively
    log.debug " tranformed $presetup"
    def path = "/decoder_control.cgi?user=${uname}&pwd=${pass}&command=${presetup}"
    //TENVIS JPT3815
    doCmd(path, false) //false and no image to return
}
def burst(){
	delayBetween([             
                take(),
                take(),             
                take(),             
                take(),            
                take(),
                take(),             
                take(),            
                take(), 
                take()     
    		], 2000)
}
def take() {
    
    def parts = settings.unamepass.split(":")
    def uname = parts[0]
    def pass = parts[1]
    
    //TENVIS JPT3815
    	//def path = "/media/?action=snapshot"
	def path = "/snapshot.cgi?user=${uname}&pwd=${pass}"
	//TENVIS JPT3815
	doCmd(path, true) //true to save image
}


def presetOne(){
	def preset = "1"
	log.trace "preset $preset: Moving to Preset position"
	presetCmd(preset.toInteger())
}

def presetTwo(){
    def preset = "2"
	log.trace "preset $preset: Moving to Preset position"
	presetCmd(preset.toInteger())
}
def presetThree(){
    def preset = "3"
	log.trace "preset $preset: Moving to Preset position"
	presetCmd(preset.toInteger())
}

private getHostAddress(data) {
    def parts = data.split(":")
    def ip = convertIPtoHex(parts[0])
    def port = convertPortToHex(parts[1])
//    log.debug "USED gethost $data converted it to $ip : $port"
    return ip + ":" + port
}
private String convertIPtoHex(ipAddress) { 
    String hex = ipAddress.tokenize( '.' ).collect {  String.format( '%02x', it.toInteger() ) }.join()
//	  log.debug "USED convert IP toHex IP address entered is $ipAddress and the converted hex code is $hex"
    return hex

}
private String convertPortToHex(port) {
	String hexport = port.toString().format( '%04x', port.toInteger() )
//	  log.debug "USED convert Port ToHex Port address entered is $port and the converted hex code is $hexport"
    return hexport
}