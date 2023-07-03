/**
 *  Foscam Universal Device
 *
 *  Copyright 2014 skp19
 *
 *  modified 2015-06-04 :  thrash99er  - changed bool comparsions from string to bool  i.e.  "true" to true
 *
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
	definition (name: "Foscam Universal Device", namespace: "smartthings", author: "skp19") {
		capability "Polling"
		capability "Image Capture"
        
        attribute "alarmStatus", "string"
        attribute "ledStatus",   "string"
        attribute "hubactionMode", "string"
    
		command "alarmOn"
		command "alarmOff"
		command "toggleAlarm"
		command "toggleLED"
        
		command "ledOn"
		command "ledOff"
		command "ledAuto"
        
		command "left"
		command "right"
		command "up"
		command "down"
        
		command "cruisemap1"
		command "cruisemap2"
		command "stopCruise"
        
		command "preset1"
		command "preset2"
		command "preset3"
	}
    
    preferences {
        input("ip", "string", title:"Camera IP Address", description: "Camera IP Address", required: true, displayDuringSetup: true)
        input("port", "string", title:"Camera Port", description: "Camera Port", defaultValue: 80 , required: true, displayDuringSetup: true)
        input("username", "string", title:"Camera Username", description: "Camera Username", required: true, displayDuringSetup: true)
        input("password", "password", title:"Camera Password", description: "Camera Password", required: true, displayDuringSetup: true)
        input("hdcamera", "bool", title:"HD Foscam Camera? (9xxx Series)", description: "Type of Foscam Camera", required: true, displayDuringSetup: true)
        input("mirror", "bool", title:"Mirror? (Not required for HD cameras)", description: "Camera Mirrored?")
        input("flip", "bool", title:"Flip? (Not required for HD cameras)", description: "Camera Flipped?")
		input("preset1", "text", title: "Preset 1 (For HD cameras only)", description: "Name of your first preset position")
		input("preset2", "text", title: "Preset 2 (For HD cameras only)", description: "Name of your second preset position")
		input("preset3", "text", title: "Preset 3 (For HD cameras only)", description: "Name of your third preset position")
		input("cruisemap1", "text", title: "Cruise Map 1 (For HD cameras only. Non-HD cameras will default to Horizontal.)", description: "Name of your first cruise map", defaultValue: "Horizontal")
		input("cruisemap2", "text", title: "Cruise Map 2 (For HD cameras only. Non-HD cameras will default to Vertical.)", description: "Name of your second cruise map", defaultValue: "Vertical")
	}

	tiles {
        carouselTile("cameraDetails", "device.image", width: 3, height: 2) { }

        standardTile("camera", "device.alarmStatus", width: 1, height: 1, canChangeIcon: true, inactiveLabel: true, canChangeBackground: true) {
          state "off", label: "off", action: "toggleAlarm", icon: "st.camera.dropcam-centered", backgroundColor: "#FFFFFF"
          state "on", label: "on", action: "toggleAlarm", icon: "st.camera.dropcam-centered",  backgroundColor: "#53A7C0"
        }

		standardTile("take", "device.image", width: 1, height: 1, canChangeIcon: false, inactiveLabel: true, canChangeBackground: false) {
			state "take", label: "Take", action: "Image Capture.take", icon: "st.camera.camera", backgroundColor: "#FFFFFF", nextState:"taking"
			state "taking", label:'Taking', action: "", icon: "st.camera.take-photo", backgroundColor: "#53a7c0"
			state "image", label: "Take", action: "Image Capture.take", icon: "st.camera.camera", backgroundColor: "#FFFFFF", nextState:"taking"
		}

        standardTile("alarmStatus", "device.alarmStatus", width: 1, height: 1, canChangeIcon: false, inactiveLabel: true, canChangeBackground: false) {
          state "off", label: "off", action: "toggleAlarm", icon: "st.quirky.spotter.quirky-spotter-sound-off", backgroundColor: "#FFFFFF"
          state "on", label: "on", action: "toggleAlarm", icon: "st.quirky.spotter.quirky-spotter-sound-on",  backgroundColor: "#53A7C0"
        }
        
        standardTile("ledStatus", "device.ledStatus", width: 1, height: 1, canChangeIcon: false, inactiveLabel: true, canChangeBackground: false) {
          state "auto", label: "auto", action: "toggleLED", icon: "st.Lighting.light13", backgroundColor: "#53A7C0"
          state "off", label: "off", action: "toggleLED", icon: "st.Lighting.light13", backgroundColor: "#FFFFFF"
          state "on", label: "on", action: "toggleLED", icon: "st.Lighting.light11", backgroundColor: "#FFFF00"
          state "manual", label: "manual", action: "toggleLED", icon: "st.Lighting.light13", backgroundColor: "#FFFF00"
        }
        
        standardTile("ledAuto", "device.ledStatus", width: 1, height: 1, canChangeIcon: false, inactiveLabel: true, canChangeBackground: false) {
          state "auto", label: "auto", action: "ledAuto", icon: "st.Lighting.light11", backgroundColor: "#53A7C0"
          state "off", label: "auto", action: "ledAuto", icon: "st.Lighting.light13", backgroundColor: "#FFFFFF"
          state "on", label: "auto", action: "ledAuto", icon: "st.Lighting.light13", backgroundColor: "#FFFFFF"
          state "manual", label: "auto", action: "ledAuto", icon: "st.Lighting.light13", backgroundColor: "#FFFFFF"
        }

        standardTile("ledOn", "device.ledStatus", width: 1, height: 1, canChangeIcon: false, inactiveLabel: true, canChangeBackground: false) {
          state "auto", label: "on", action: "ledOn", icon: "st.Lighting.light11", backgroundColor: "#FFFFFF"
          state "off", label: "on", action: "ledOn", icon: "st.Lighting.light11", backgroundColor: "#FFFFFF"
          state "on", label: "on", action: "ledOn", icon: "st.Lighting.light11", backgroundColor: "#FFFF00"
          state "manual", label: "on", action: "ledOn", icon: "st.Lighting.light11", backgroundColor: "#00FF00"
        }
        
        standardTile("ledOff", "device.ledStatus", width: 1, height: 1, canChangeIcon: false, inactiveLabel: true, canChangeBackground: false) {
          state "auto", label: "off", action: "ledOff", icon: "st.Lighting.light13", backgroundColor: "#FFFFFF"
          state "off", label: "off", action: "ledOff", icon: "st.Lighting.light13", backgroundColor: "#53A7C0"
          state "on", label: "off", action: "ledOff", icon: "st.Lighting.light13", backgroundColor: "#FFFFFF"
          state "manual", label: "off", action: "ledOff", icon: "st.Lighting.light13", backgroundColor: "#00FF00"
        }
        
		standardTile("preset1", "device.image", width: 1, height: 1, canChangeIcon: false, canChangeBackground: false, decoration: "flat") {
			state "preset1", label: "preset 1", action: "preset1", icon: ""
		}

		standardTile("preset2", "device.image", width: 1, height: 1, canChangeIcon: false, canChangeBackground: false, decoration: "flat") {
			state "preset2", label: "preset 2", action: "preset2", icon: ""
		}

		standardTile("preset3", "device.image", width: 1, height: 1, canChangeIcon: false, canChangeBackground: false, decoration: "flat") {
			state "preset3", label: "preset 3", action: "preset3", icon: ""
		}
        
		standardTile("cruisemap1", "device.image", width: 1, height: 1, canChangeIcon: false, canChangeBackground: false, decoration: "flat") {
			state "cruisemap1", label: "Cruise Map 1", action: "cruisemap1", icon: ""
		}

		standardTile("cruisemap2", "device.image", width: 1, height: 1, canChangeIcon: false, canChangeBackground: false, decoration: "flat") {
			state "cruisemap2", label: "Cruise Map 2", action: "cruisemap2", icon: ""
		}
 
 		standardTile("stopcruise", "device.image", width: 1, height: 1, canChangeIcon: false, canChangeBackground: false, decoration: "flat") {
			state "stopcruise", label: "Stop Cruise", action: "stopCruise", icon: ""
		}

		standardTile("left", "device.image", width: 1, height: 1, canChangeIcon: false,  canChangeBackground: false, decoration: "flat") {
			state "left", label: "left", action: "left", icon: ""
		}

		standardTile("right", "device.image", width: 1, height: 1, canChangeIcon: false,  canChangeBackground: false, decoration: "flat") {
			state "right", label: "right", action: "right", icon: ""
		}

		standardTile("up", "device.image", width: 1, height: 1, canChangeIcon: false, canChangeBackground: false, decoration: "flat") {
			state "up", label: "up", action: "up", icon: "st.thermostat.thermostat-up"
		}

		standardTile("down", "device.image", width: 1, height: 1, canChangeIcon: false, canChangeBackground: false, decoration: "flat") {
			state "down", label: "down", action: "down", icon: "st.thermostat.thermostat-down"
		}

		standardTile("stop", "device.image", width: 1, height: 1, canChangeIcon: false,  canChangeBackground: false, decoration: "flat") {
			state "stop", label: "", action: "stopCruise", icon: "st.sonos.stop-btn"
		}

        standardTile("refresh", "device.alarmStatus", inactiveLabel: false, decoration: "flat") {
          state "refresh", action:"polling.poll", icon:"st.secondary.refresh"
        }
        
        standardTile("blank", "device.image", width: 1, height: 1, canChangeIcon: false,  canChangeBackground: false, decoration: "flat") {
          state "blank", label: "", action: "", icon: "", backgroundColor: "#FFFFFF"
        }

        main "camera"
			//details(["cameraDetails", "take", "blank", "alarmStatus", "ledAuto", "ledOn", "ledOff", "refresh"]) //**Uncomment this line and comment out the next line to hide the PTZ controls
			details(["cameraDetails", "take", "blank", "alarmStatus", "ledAuto", "ledOn", "ledOff", "preset1", "preset2", "preset3", "cruisemap1", "cruisemap2", "stopcruise", "blank", "up", "blank", "left", "stop", "right", "blank", "down", "blank", "refresh"])
	}
}

//TAKE PICTURE
def take() {
	log.debug("Taking Photo")
	sendEvent(name: "hubactionMode", value: "s3");
    if(hdcamera == true) {
		hubGet("cmd=snapPicture2")
    }
    else {
    	hubGet("/snapshot.cgi?")
    }
}
//END TAKE PICTURE

//ALARM ACTIONS
def toggleAlarm() {
	log.debug "Toggling Alarm"
	if(device.currentValue("alarmStatus") == "on") {
    	alarmOff()
  	}
	else {
    	alarmOn()
	}
}

def alarmOn() {
	log.debug "Enabling Alarm"
    sendEvent(name: "alarmStatus", value: "on");
    if(hdcamera == true) {
       //hubGet("cmd=setMotionDetectConfig&isEnable=1")
       hubGet("cmd=setMotionDetectConfig&isEnable=1&isMovAlarmEnable=1&isPirAlarmEnable=0&snapInterval=2&sensitivity=0&linkage=134&triggerInterval=10&schedule0=281474976710655&schedule1=281474976710655&schedule2=281474976710655&schedule3=281474976710655&schedule4=281474976710655&schedule5=281474976710655&schedule6=281474976710655&area0=1023&area1=1023&area2=1023&area3=1023&area4=1023&area5=1023&area6=1023&area7=1023&area8=1023&area9=1023:")
    }
    else {
    	hubGet("/set_alarm.cgi?motion_armed=1&")
    }
}

def alarmOff() {
	log.debug "Disabling Alarm"
    sendEvent(name: "alarmStatus", value: "off");
    if(hdcamera == true) {
		hubGet("cmd=setMotionDetectConfig&isEnable=0")
    }
    else {
    	hubGet("/set_alarm.cgi?motion_armed=0&")
    }
}
//END ALARM ACTIONS

//LED ACTIONS
//Toggle LED's
def toggleLED() {
  log.debug("Toggle LED")

  if(device.currentValue("ledStatus") == "auto") {
    ledOn()
  }

  else if(device.currentValue("ledStatus") == "on") {
    ledOff()
  }
  
  else {
    ledAuto()
  }
}

def ledOn() {
    log.debug("LED changed to: on")
    sendEvent(name: "ledStatus", value: "on");
    if(hdcamera == true) {
	    delayBetween([hubGet("cmd=setInfraLedConfig&mode=1"), hubGet("cmd=openInfraLed")])
    }
    else {
    	hubGet("/decoder_control.cgi?command=95&")
    }
}

def ledOff() {
    log.debug("LED changed to: off")
    sendEvent(name: "ledStatus", value: "off");
    if(hdcamera == "true") {
    	delayBetween([hubGet("cmd=setInfraLedConfig&mode=1"), hubGet("cmd=closeInfraLed")])
    }
    else {
    	hubGet("/decoder_control.cgi?command=94&")
    }
}

def ledAuto() {
    log.debug("LED changed to: auto")
    sendEvent(name: "ledStatus", value: "auto");
	if(hdcamera == true) {
		hubGet("cmd=setInfraLedConfig&mode=0")
    }
    else {
    	hubGet("/decoder_control.cgi?command=95&")
    }
}
//END LED ACTIONS

//PRESET ACTIONS
def preset1() {
	log.debug("Preset 1 Selected - ${preset1}")
	if(hdcamera == true) {
		hubGet("cmd=ptzGotoPresetPoint&name=${preset1}")
    }
    else {
    	hubGet("/decoder_control.cgi?command=31&")
    }
}

def preset2() {
	log.debug("Preset 2 Selected - ${preset2}")
	if(hdcamera == true) {
		hubGet("cmd=ptzGotoPresetPoint&name=${preset2}")
    }
    else {
    	hubGet("/decoder_control.cgi?command=33&")
    }
}

def preset3() {
	log.debug("Preset 3 Selected - ${preset3}")
	if(hdcamera == true) {
		hubGet("cmd=ptzGotoPresetPoint&name=${preset3}")
    }
    else {
    	hubGet("/decoder_control.cgi?command=35&")
    }
}
//END PRESET ACTIONS

//CRUISE ACTIONS
def cruisemap1() {
	log.debug("Cruise Map 1 Selected - ${cruisemap1}")
	if(hdcamera == true) {
		hubGet("cmd=ptzStartCruise&mapName=${cruisemap1}")
    }
    else {
    	hubGet("/decoder_control.cgi?command=28&")
    }
}

def cruisemap2() {
	log.debug("Cruise Map 2 Selected - ${cruisemap2}")
	if(hdcamera == true) {
		hubGet("cmd=ptzStartCruise&mapName=${cruisemap2}")
    }
    else {
    	hubGet("/decoder_control.cgi?command=26&")
    }
}

def stopCruise() {
	log.debug("Stop Cruise")
	if(hdcamera == true) {
		hubGet("cmd=ptzStopRun")
    }
    else {
    	delayBetween([hubGet("/decoder_control.cgi?command=29&"), hubGet("/decoder_control.cgi?command=27&")])
    }
}
//END CRUISE ACTIONS

//PTZ CONTROLS
def left() {
	if(hdcamera == "true") {
		delayBetween([hubGet("cmd=ptzMoveLeft"), hubGet("cmd=ptzStopRun")])
    }
    else {
    	if(mirror == "true") {
	    	hubGet("/decoder_control.cgi?command=4&onestep=1&")
        }
        else {
        	hubGet("/decoder_control.cgi?command=6&onestep=1&")
        }
    }
}

def right() {
	if(hdcamera == "true") {
		delayBetween([hubGet("cmd=ptzMoveRight"), hubGet("cmd=ptzStopRun")])
    }
    else {
    	if(mirror == "true") {
	    	hubGet("/decoder_control.cgi?command=6&onestep=1&")
        }
        else {
        	hubGet("/decoder_control.cgi?command=4&onestep=1&")
        }
    }
}

def up() {
	if(hdcamera == true) {
        delayBetween([hubGet("cmd=ptzMoveUp"), hubGet("cmd=ptzStopRun")])
    }
    else {
    	if(flip == true) {
	    	hubGet("/decoder_control.cgi?command=2&onestep=1&")
        }
        else {
        	hubGet("/decoder_control.cgi?command=0&onestep=1&")
        }
    }
}

def down() {
	if(hdcamera == true) {
        delayBetween([hubGet("cmd=ptzMoveDown"), hubGet("cmd=ptzStopRun")])
    }
    else {
    	if(flip == true) {
    		hubGet("/decoder_control.cgi?command=0&onestep=1&")
        }
        else {
        	hubGet("/decoder_control.cgi?command=2&onestep=1&")
        }
    }
}
//END PTZ CONTROLS

def poll() {

	sendEvent(name: "hubactionMode", value: "local");
    //Poll Motion Alarm Status and IR LED Mode
    if(hdcamera == true) {
		delayBetween([hubGet("cmd=getMotionDetectConfig"), hubGet("cmd=getInfraLedConfig")])
    }
    else {
    	hubGet("/get_params.cgi?")
    }
}

private getLogin() {
	if(hdcamera == true) {
    	return "usr=${username}&pwd=${password}&"
    }
    else {
    	return "user=${username}&pwd=${password}"
    }
}

private hubGet(def apiCommand) {
	//Setting Network Device Id
    def iphex = convertIPtoHex(ip)
    def porthex = convertPortToHex(port)
    device.deviceNetworkId = "$iphex:$porthex"
    log.debug "Device Network Id set to ${iphex}:${porthex}"

	log.debug("Executing hubaction on " + getHostAddress())
    def uri = ""
    if(hdcamera == true) {
    	uri = "/cgi-bin/CGIProxy.fcgi?" + getLogin() + apiCommand
	}
    else {
    	uri = apiCommand + getLogin()
    }
    def hubAction = new physicalgraph.device.HubAction(
    	method: "GET",
        path: uri,
        headers: [HOST:getHostAddress()]
    )
    if(device.currentValue("hubactionMode") == "s3") {
        hubAction.options = [outputMsgToS3:true]
        sendEvent(name: "hubactionMode", value: "local");
    }
	return hubAction
}

//Parse events into attributes
def parse(String description) {
	log.debug "Parsing '${description}'"
    def map = [:]
    def retResult = []
    def descMap = parseDescriptionAsMap(description)
        
    //Image
	if (descMap["bucket"] && descMap["key"]) {
		putImageInS3(descMap)
	}

	//Status Polling
    else if (descMap["headers"] && descMap["body"]) {
        def body = new String(descMap["body"].decodeBase64())
        if(hdcamera == true) {
            log.debug("####!!!: ${body}")
            def parser=new XmlSlurper()
            parser.setFeature("http://apache.org/xml/features/disallow-doctype-decl", false) 
            parser.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
            def langs = parser.parseText(body)
            //def langs = new XmlSlurper().parseText(body)

            def motionAlarm = "$langs.isEnable"
            def ledMode = "$langs.mode"

            //Get Motion Alarm Status
            if(motionAlarm == "0") {
                log.info("Polled: Alarm Off")
                sendEvent(name: "alarmStatus", value: "off");
            }
            else if(motionAlarm == "1") {
                log.info("Polled: Alarm On")
                sendEvent(name: "alarmStatus", value: "on");
            }

            //Get IR LED Mode
            if(ledMode == "0") {
                log.info("Polled: LED Mode Auto")
                sendEvent(name: "ledStatus", value: "auto")
            }
            else if(ledMode == "1") {
                log.info("Polled: LED Mode Manual")
                sendEvent(name: "ledStatus", value: "manual")
            }
    	}
        else {
        	if(body.find("alarm_motion_armed=0")) {
				log.info("Polled: Alarm Off")
                sendEvent(name: "alarmStatus", value: "off")
            }
        	else if(body.find("alarm_motion_armed=1")) {
				log.info("Polled: Alarm On")
                sendEvent(name: "alarmStatus", value: "on")
            }
            //The API does not provide a way to poll for LED status on 8xxx series at the moment
        }
	}
}

def parseDescriptionAsMap(description) {
	description.split(",").inject([:]) { map, param ->
		def nameAndValue = param.split(":")
		map += [(nameAndValue[0].trim()):nameAndValue[1].trim()]
	}
}

def putImageInS3(map) {

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

private getPictureName() {
  def pictureUuid = java.util.UUID.randomUUID().toString().replaceAll('-', '')
  "image" + "_$pictureUuid" + ".jpg"
}

private getHostAddress() {
	return "${ip}:${port}"
}

private String convertIPtoHex(ipAddress) { 
    String hex = ipAddress.tokenize( '.' ).collect {  String.format( '%02x', it.toInteger() ) }.join()
    return hex

}

private String convertPortToHex(port) {
	String hexport = port.toString().format( '%04x', port.toInteger() )
    return hexport
}