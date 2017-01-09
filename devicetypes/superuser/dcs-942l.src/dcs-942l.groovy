/**
 *	D-Link DCS-942L v1.0.2
 *  Modified from Generic Camera Device v1.0.07102014
 *
 *  Copyright 2014 patrick@patrickstuart.com
 *  Modified 2015 blebson
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
metadata {
	definition (name: "DCS-942L", author: "blebson") {
		capability "Image Capture"
		capability "Sensor"
		capability "Switch"
        capability "Switch Level"
        capability "Refresh"
        
		attribute "hubactionMode", "string"
        //attribute "switch1", "string"
        attribute "switch2", "string"
        attribute "switch3", "string"
        
        
        
        command "motionOn"
        command "motionOff"
        command "pirOn"
        command "pirOff"
        command "nvOn"
        command "nvOff"
        
	}

    preferences {
    input("CameraIP", "string", title:"Camera IP Address", description: "Please enter your camera's IP Address", required: true, displayDuringSetup: true)
    input("CameraPort", "string", title:"Camera Port", description: "Please enter your camera's Port", defaultValue: 80 , required: true, displayDuringSetup: true)
    input("CameraUser", "string", title:"Camera User", description: "Please enter your camera's username", required: false, displayDuringSetup: true)
    input("CameraPassword", "password", title:"Camera Password", description: "Please enter your camera's password", required: false, displayDuringSetup: true)
	}
    
	simulator {
    
	}

    tiles {
    	carouselTile("cameraDetails", "device.image", width: 3, height: 2) { }
        standardTile("take", "device.image", width: 1, height: 1, canChangeIcon: false, inactiveLabel: true, canChangeBackground: false) {
            state "take", label: "Take", action: "Image Capture.take", icon: "st.camera.camera", backgroundColor: "#FFFFFF", nextState:"taking"
            state "taking", label:'Taking', action: "", icon: "st.camera.take-photo", backgroundColor: "#53a7c0"
            state "image", label: "Take", action: "Image Capture.take", icon: "st.camera.camera", backgroundColor: "#FFFFFF", nextState:"taking"
        }

        standardTile("refresh", "command.refresh", inactiveLabel: false) {
        	state "default", label:'refresh', action:"refresh.refresh", icon:"st.secondary.refresh-icon"        
    	}
        standardTile("motion", "device.switch", width: 1, height: 1, canChangeIcon: false) {
			state "off", label: 'Motion Off', action: "switch.on", icon: "st.motion.motion.inactive", backgroundColor: "#ccffcc", nextState: "toggle"
            state "toggle", label:'toggle', action: "", icon: "st.motion.motion.inactive", backgroundColor: "#53a7c0"
			state "on", label: 'Motion On', action: "switch.off", icon: "st.motion.motion.active", backgroundColor: "#EE0000", nextState: "toggle"            
		}
         standardTile("PIR", "device.switch2", width: 1, height: 1, canChangeIcon: false) {
			state "off", label: 'PIR Off', action: "pirOn", icon: "st.custom.buttons.rec", backgroundColor: "#ccffcc", nextState: "toggle"
            state "toggle", label:'toggle', action: "", icon: "st.motion.buttons.rec", backgroundColor: "#53a7c0"
			state "on", label: 'PIR On', action: "pirOff", icon: "st.custom.buttons.rec", backgroundColor: "#EE0000", nextState: "toggle"
		}
        standardTile("nightVision", "device.switch3", width: 1, height: 1, canChangeIcon: false) {
			state "off", label: 'Night Vision Off', action: "nvOn", icon: "st.Weather.weather14", backgroundColor: "#ffff00", nextState: "toggle"
            state "toggle", label:'toggle', action: "", icon: "st.motion.motion.inactive", backgroundColor: "#53a7c0"
			state "on", label: 'Night Vision On', action: "nvOff", icon: "st.Weather.weather4", backgroundColor: "#4169E1", nextState: "toggle"            
		}
       controlTile("levelSliderControl", "device.level", "slider", height: 1, width: 1, inactiveLabel: false, range:"(0..100)") {
            state "level", action:"switch level.setLevel"
        }
        
        main "motion"
        details(["cameraDetails", "take", "motion", "PIR", "refresh", "nightVision", "levelSliderControl"])
    }
}

def parse(String description) {
    log.debug "Parsing '${description}'"
    def map = [:]
	def retResult = []
	def descMap = parseDescriptionAsMap(description)
    def msg = parseLanMessage(description)
    //log.debug "status ${msg.status}"
    //log.debug "data ${msg.data}"
    
	//Image
	if (descMap["bucket"] && descMap["key"]) {
		putImageInS3(descMap)
	}      
    else if (descMap["headers"] && descMap["body"]){
    	def body = new String(descMap["body"].decodeBase64())
        log.debug "Body: ${body}"
    }
    
    if (msg.body) {
    
    //log.debug "Motion Enabled: ${msg.body.contains("enable=yes")}"
    //log.debug "Motion Disabled: ${msg.body.contains("enable=no")}"
    //log.debug "PIR Enabled: ${msg.body.contains("pir=yes")}"
    //log.debug "PIR Disabled: ${msg.body.contains("pir=no")}"
    
        if (msg.body.contains("enable=yes")) {
            log.debug "Motion is on"
            sendEvent(name: "switch", value: "on");
        }
        else if (msg.body.contains("enable=no")) {
            log.debug "Motion is off"
            sendEvent(name: "switch", value: "off");
        }
        if (msg.body.contains("pir=yes"))
        {
        	log.debug "PIR is on"
        	sendEvent(name: "switch2", value: "on");
        }
        else if (msg.body.contains("pir=no"))
        {
        	log.debug "PIR is off"
        	sendEvent(name: "switch2", value: "off");
        }
        if(msg.body.contains("sensitivity="))
        {
        	//log.debug msg.body        
        	String[] lines = msg.body.split( '\n' )
        	//log.debug lines[2]
            String[] sensitivity = lines[2].split( '=' )
            //log.debug sensitivity[1]
            int[] senseValue = sensitivity[1].toInteger()
            //log.debug senseValue
            
            sendEvent(name: "level",  value: "${senseValue[0]}")
            //sendEvent(name: "switch.setLevel", value: "${senseValue}")
        }       
        
        if (msg.body.contains( "mode=night")) {
            log.debug "Night Vision is on"
            sendEvent(name: "switch3", value: "on");
        }
        else if (msg.body.contains("mode=day")) {
            log.debug "Night Vision is off"
            sendEvent(name: "switch3", value: "off");
        }
    }    
}

// handle commands
def take() {
	def userpassascii = "${CameraUser}:${CameraPassword}"
	def userpass = "Basic " + userpassascii.encodeAsBase64().toString()
    def host = CameraIP 
    def hosthex = convertIPtoHex(host)
    def porthex = convertPortToHex(CameraPort)
    device.deviceNetworkId = "$hosthex:$porthex" 
    
    log.debug "The device id configured is: $device.deviceNetworkId"
    
    def path = "/image/jpeg.cgi" 
    log.debug "path is: $path"
    
    def headers = [:] 
    headers.put("HOST", "$host:$CameraPort")
   	headers.put("Authorization", userpass)
    
    log.debug "The Header is $headers"
    
    def method = "GET"    
    
    log.debug "The method is $method"
    
    try {
    def hubAction = new physicalgraph.device.HubAction(
    	method: method,
    	path: path,
    	headers: headers
        )
        	
    hubAction.options = [outputMsgToS3:true]
    log.debug hubAction
    hubAction
    }
    catch (Exception e) {
    	log.debug "Hit Exception $e on $hubAction"
    }
    
}

def motionCmd(int motion, String attr)
{
	def userpassascii = "${CameraUser}:${CameraPassword}"
	def userpass = "Basic " + userpassascii.encodeAsBase64().toString()
    def host = CameraIP 
    def hosthex = convertIPtoHex(host)
    def porthex = convertPortToHex(CameraPort)
    device.deviceNetworkId = "$hosthex:$porthex" 
    
    log.debug "The device id configured is: $device.deviceNetworkId"
    
    def headers = [:] 
    headers.put("HOST", "$host:$CameraPort")
    headers.put("Authorization", userpass)
    
    log.debug "The Header is $headers"
    
    if (motion == 1){
 def path = "/config/motion.cgi?${attr}=yes"
 log.debug "path is: $path"
  try {
    def hubAction = new physicalgraph.device.HubAction(
    	method: "GET",
    	path: path,
    	headers: headers
        )
        	
   
    log.debug hubAction
    return hubAction
    
    }
    catch (Exception e) {
    	log.debug "Hit Exception $e on $hubAction"
    }
  }
  else
  {
  def path = "/config/motion.cgi?${attr}=no"
 log.debug "path is: $path"  
  try {
    def hubAction = new physicalgraph.device.HubAction(
    	method: "GET",
    	path: path,
    	headers: headers        
        )
     
    log.debug hubAction
    return hubAction
    
    }
    catch (Exception e) {
    	log.debug "Hit Exception $e on $hubAction"
    }
  
 
}
}
def sensitivityCmd(int percent)
{
	def userpassascii = "${CameraUser}:${CameraPassword}"
	def userpass = "Basic " + userpassascii.encodeAsBase64().toString()
    def host = CameraIP 
    def hosthex = convertIPtoHex(host)
    def porthex = convertPortToHex(CameraPort)
    device.deviceNetworkId = "$hosthex:$porthex" 
    
    log.debug "The device id configured is: $device.deviceNetworkId"
    
    def pir_percent = []
        
    if (percent <= 33)
    {
    pir_percent = 0
    }
    else if (percent > 33 && percent < 66)
    {
    pir_percent = 50
    }
    else if (percent >= 66)
    {
    pir_percent = 100
    }
    
    log.debug "Sensitivity is ${percent} and PIR Sensitivity is ${pir_percent}"
    
    def path = "/config/motion.cgi?sensitivity=${percent}&pir_sensitivity=${pir_percent}"
    log.debug "path is: $path"
        
    def headers = [:] 
    headers.put("HOST", "$host:$CameraPort")
    headers.put("Authorization", userpass)
    
    log.debug "The Header is $headers"
   
  try {
    def hubAction = new physicalgraph.device.HubAction(
    	method: "GET",
    	path: path,
    	headers: headers
        )
        	
   
    log.debug hubAction
    return hubAction
    
    }
    catch (Exception e) {
    	log.debug "Hit Exception $e on $hubAction"
    }
  
}

def nightCmd(String attr)
{
	def userpassascii = "${CameraUser}:${CameraPassword}"
	def userpass = "Basic " + userpassascii.encodeAsBase64().toString()
    def host = CameraIP 
    def hosthex = convertIPtoHex(host)
    def porthex = convertPortToHex(CameraPort)
    device.deviceNetworkId = "$hosthex:$porthex" 
    
    log.debug "The device id configured is: $device.deviceNetworkId"
    
    def headers = [:] 
    headers.put("HOST", "$host:$CameraPort")
    headers.put("Authorization", userpass)
    
    log.debug "The Header is $headers"
    
 def path = "/config/icr.cgi?mode=${attr}"
 log.debug "path is: $path"
  try {
    def hubAction = new physicalgraph.device.HubAction(
    	method: "GET",
    	path: path,
    	headers: headers
        )
        	
   
    log.debug hubAction
    return hubAction
    
    }
    catch (Exception e) {
    	log.debug "Hit Exception $e on $hubAction"
    }
  
 
}


def putImageInS3(map) {
	log.debug "firing s3"
    def s3ObjectContent
    try {
        def imageBytes = getS3Object(map.bucket, map.key + ".jpg")
        if(imageBytes)
        {
            s3ObjectContent = imageBytes.getObjectContent()
            def bytes = new ByteArrayInputStream(s3ObjectContent.bytes)
            storeImage(getPictureName(), bytes)
        }
    }
    catch(Exception e) {
        log.error e
    }
	finally {
    //Explicitly close the stream
		if (s3ObjectContent) { s3ObjectContent.close() }
	}
}

def parseDescriptionAsMap(description) {
	description.split(",").inject([:]) { map, param ->
		def nameAndValue = param.split(":")
		map += [(nameAndValue[0].trim()):nameAndValue[1].trim()]
	}
}

private getPictureName() {
	def pictureUuid = java.util.UUID.randomUUID().toString().replaceAll('-', '')
    log.debug pictureUuid
    def picName = device.deviceNetworkId.replaceAll(':', '') + "_$pictureUuid" + ".jpg"
	return picName
}

private String convertIPtoHex(ipAddress) { 
    String hex = ipAddress.tokenize( '.' ).collect {  String.format( '%02x', it.toInteger() ) }.join()
    log.debug "IP address entered is $ipAddress and the converted hex code is $hex"
    return hex

}

private String convertPortToHex(port) {
	String hexport = port.toString().format( '%04x', port.toInteger() )
    log.debug hexport
    return hexport
}

private Integer convertHexToInt(hex) {
	Integer.parseInt(hex,16)
}


private String convertHexToIP(hex) {
	log.debug("Convert hex to ip: $hex") 
	[convertHexToInt(hex[0..1]),convertHexToInt(hex[2..3]),convertHexToInt(hex[4..5]),convertHexToInt(hex[6..7])].join(".")
}

private getHostAddress() {
    def parts = device.deviceNetworkId.split(":")
    log.debug device.deviceNetworkId
    def ip = convertHexToIP(parts[0])
    def port = convertHexToInt(parts[1])
    return ip + ":" + port
}


def on() {
	log.debug "Enabling motion detection"
    return motionCmd(1, "enable")    
    
}

def off() {
	log.debug "Disabling motion detection"
    return motionCmd(0, "enable")    
    
}
def pirOn() {
	log.debug "Enabling PIR Sensor"
    return motionCmd(1, "pir")  
    
}

def pirOff() {
	log.debug "Disabling PIR Sensor"
    return motionCmd(0, "pir") 
    
}

def setLevel(percent) {
	log.debug "Executing 'setLevel'"
	return sensitivityCmd(percent)	
    
}
def nvOn() {
	log.debug "Enabling Night Vision"
    return nightCmd("night")   
    
}

def nvOff() {
	log.debug "Disabling Night Vision"
    return nightCmd("day")    
    
}

def refresh(){

	log.debug "Refresh"
	def userpassascii = "${CameraUser}:${CameraPassword}"
	def userpass = "Basic " + userpassascii.encodeAsBase64().toString()
    def host = CameraIP 
    def hosthex = convertIPtoHex(host)
    def porthex = convertPortToHex(CameraPort)
    device.deviceNetworkId = "$hosthex:$porthex" 
    
    log.debug "The device id configured is: $device.deviceNetworkId"
    def path = "/config/motion.cgi"
    log.debug "path is: $path"
    
    def headers = [:] 
    headers.put("HOST", "$host:$CameraPort")
    headers.put("Authorization", userpass)
    
    log.debug "The Header is $headers"
   
  try {
    def hubAction = new physicalgraph.device.HubAction(
    	method: "GET",
    	path: path,
    	headers: headers
        )
        	
   
    log.debug hubAction
    return hubAction
    
    }
    catch (Exception e) {
    	log.debug "Hit Exception $e on $hubAction"
    }
  
  
}