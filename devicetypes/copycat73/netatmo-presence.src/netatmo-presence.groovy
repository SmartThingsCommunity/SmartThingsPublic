/*
 *  Netatmo Presence
 *
 *  Copyright 2018 Nick Veenstra
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
	definition (name: "Netatmo Presence", namespace: "CopyCat73", author: "Nick Veenstra") {
		capability "Image Capture"
		capability "Motion Sensor"
		capability "Refresh"
        capability "Switch"
        
        command "motion"
        command "human"
        command "vehicle"
        command "animal"
       	attribute "homeName", "string"


	}


	simulator {
		// TODO: define status and reply messages here
	}
    
    preferences {
    	section ("Snapshots") {
            input("cameraIP", "text", title: "Local IP for camera", required: true, displayDuringSetup: false, defaultValue: "", description: "The address of the camera in your local network")  
            input("cameraSecret", "text", title: "Access key for camera", required: true, displayDuringSetup: false, defaultValue: "", description: "Key to access the snapshot")
            input("motionHumans", "bool", title: "Humans detected count as motion", required: true, displayDuringSetup: false)  
            input("motionVehicles", "bool", title: "Vehicles detected count as motion", required: true, displayDuringSetup: false)  
            input("motionAnimals", "bool", title: "Animals detected count as motion", required: true, displayDuringSetup: false)  
			input("motionTimeout", "number", title: "Motion, human, vehicle and pet detection times out after how many seconds", required: true, displayDuringSetup: false)  
			input("scheduledTake", "enum", title: "Take a snapshot every:",  options: ["disabled": "No snapshots", "every1Minute" : "Every minute", "every5Minutes" : "Every 5 minutes", "every10Minutes" : "Every 10 minutes", "every15Minutes" : "Every 15 minutes", "every30Minutes" : "Every 30 minutes", "every1Hour" : "Every hour", "Every3Hours" : "every 3 hours"],required: true, displayDuringSetup: false)  
        }
    }    

    tiles {
        standardTile("image", "device.image", width: 1, height: 1, canChangeIcon: false, inactiveLabel: true, canChangeBackground: true) {
            state "default", label: "", action: "", icon: "st.camera.dropcam-centered", backgroundColor: "#FFFFFF"
        }

        carouselTile("cameraDetails", "device.image", width: 3, height: 2) { }

        standardTile("take", "device.image", width: 1, height: 1, canChangeIcon: false, inactiveLabel: true, canChangeBackground: false) {
            state "take", label: "Take", action: "Image Capture.take", icon: "st.camera.dropcam", backgroundColor: "#FFFFFF", nextState:"taking"
            state "taking", label:'Taking', action: "", icon: "st.camera.dropcam", backgroundColor: "#00A0DC"
            state "image", label: "Take", action: "Image Capture.take", icon: "st.camera.dropcam", backgroundColor: "#FFFFFF", nextState:"taking"
        }
       standardTile("motion", "device.motion", width: 1, height: 1, canChangeIcon: false) {
			state "inactive", label: 'NO MOTION', action: "", icon: "st.motion.motion.inactive", backgroundColor: "#ffffff"
    		state "active", label: 'MOTION', action: "", icon: "st.motion.motion.active", backgroundColor: "#ffffff"           
		}
        standardTile("switch", "device.switch", width: 1, height: 1, canChangeIcon: true) {
            state "off", label: '${currentValue}', action: "", icon: "st.switches.switch.off", backgroundColor: "#ffffff"
            state "on", label: '${currentValue}', action: "", icon: "st.switches.switch.on", backgroundColor: "#00a0dc"
        } 
       standardTile("human", "device.human", width: 1, height: 1, canChangeIcon: false) {
			state "inactive", label: 'Human', action: "", icon: "st.Health & Wellness.health12", backgroundColor: "#ffffff"
  			state "active", label: 'DETECTED', action: "", icon: "st.Health & Wellness.health12", backgroundColor: "#00a0dc"            
		}
       standardTile("vehicle", "device.vehicle", width: 1, height: 1, canChangeIcon: false) {
			state "inactive", label: 'Vehicle', action: "", icon: "st.Transportation.transportation2", backgroundColor: "#ffffff"
     		state "active", label: 'DETECTED', action: "", icon: "st.Transportation.transportation2", backgroundColor: "#00a0dc"           
		}
       standardTile("animal", "device.animal", width: 1, height: 1, canChangeIcon: false) {
			state "inactive", label: 'Animal', action: "", icon: "st.Kids.kids20", backgroundColor: "#ffffff"
      		state "active", label: 'DETECTED', action: "", icon: "st.Kids.kids20", backgroundColor: "#00a0dc"        
		}   
        standardTile("homeName", "device.homeName", width: 1, height: 1, canChangeIcon: false, inactiveLabel: true, canChangeBackground: false) {
            state "homeName", label: '${currentValue}', action: "", icon: "st.Home.home2", backgroundColor: "#FFFFFF"
        }            
        main "motion"
        details(["cameraDetails", "take", "motion", "switch", "human", "vehicle", "animal","homeName"])
    }
}

def parse(String description) {
	log.debug "Parsing '${description}'"

	def map = stringToMap(description)

    if (map.tempImageKey) {
        try {
            storeTemporaryImage(map.tempImageKey, getPictureName())
        } catch (Exception e) {
            log.error e
        }
    } else if (map.error) {
        log.error "Error: ${map.error}"
    }
}

private getPictureName() {
    return java.util.UUID.randomUUID().toString().replaceAll('-', '')
}


def updated() {
	log.debug "updated()"
	initialize()
}

def uninstalled() {
	log.debug "uninstalled()"
	unschedule()
}

def initialize() {

	unschedule()
	if (scheduledTake) {
    	switch (scheduledTake) {
        	case "every1Minute":
            	runEvery1Minute("take")
                break
        	case "every5Minutes":
            	runEvery5Minutes("take")
                break
        	case "every10Minutes":
            	runEvery10Minutes("take")
                break
        	case "every15Minutes":
            	runEvery15Minutes("take")
                break
        	case "every30Minutes":
            	runEvery30Minutes("take")
                break
        	case "every1Hour":
            	runEvery1Hour("take")
                break
        	case "every3Hours":
            	runEvery3Hours("take")
                break
			default:
             	break
		}
	}
}   

def setHome(homeID,homeName) {
	state.homeID = homeID
    sendEvent(name: "homeName", value: homeName)
}

def human() {

	sendEvent(name: "human", value: "active")
    if (motionHumans) {
    	motion()
    }
    if (motionTimeout) {
    	startTimer(motionTimeout, cancelHuman)
    }
    else {
    	log.debug "Motion timeout has not been set in preferences, using 10 second default"
    	startTimer(10, cancelHuman)
	}
}

def vehicle() {

	sendEvent(name: "vehicle", value: "active")
    if (motionVehicles) {
    	motion()
    }
    if (motionTimeout) {
    	startTimer(motionTimeout, cancelVehicle)
    }
    else {
    	log.debug "Motion timeout has not been set in preferences, using 10 second default"
    	startTimer(10, cancelVehicle)
	}
}

def animal() {

	sendEvent(name: "animal", value: "active")
    if (motionAnimals) {
    	motion()
    }    
    if (motionTimeout) {
    	startTimer(motionTimeout, cancelAnimal)
    }
    else {
    	log.debug "Motion timeout has not been set in preferences, using 10 second default"
    	startTimer(10, cancelAnimal)
	}
}

def on() {

	sendEvent(name: "switch", value: "on")
} 

def off() {

	sendEvent(name: "switch", value: "off")
}  

def motion() {

	sendEvent(name: "motion", value: "active")
    
    if (motionTimeout) {
    	startTimer(motionTimeout, cancelMotion)
    }
    else {
    	log.debug "Motion timeout has not been set in preferences, using 10 second default"
    	startTimer(10, cancelMotion)
	}
}

def cancelHuman() {

	sendEvent(name: "human", value: "inactive")
}

def cancelVehicle() {

	sendEvent(name: "vehicle", value: "inactive")
}

def cancelAnimal() {

	sendEvent(name: "animal", value: "inactive")
}

def cancelMotion() {

	sendEvent(name: "motion", value: "inactive")
}

def startTimer(seconds, function) {
    def now = new Date()
	def runTime = new Date(now.getTime() + (seconds * 1000))
	runOnce(runTime, function) // runIn isn't reliable, use runOnce instead
}

def take() {
	if (cameraSecret == null || cameraIP == null) {
    	showAlert("Please set camera ip and secret in preferences first","Missing preferences","Secret + IP")
        sendEvent(name: "take", value: "take")
        return
	}
    log.debug("Taking Photo")
	def port = 80
    def path = "/${cameraSecret}/live/snapshot_720.jpg"
    def iphex = convertIPtoHex(cameraIP).toUpperCase()
    def porthex = convertPortToHex(port).toUpperCase()
    log.debug "The device id before update is: $device.deviceNetworkId"
    device.deviceNetworkId = "$iphex:$porthex" 
 	def hostAddress = "$cameraIP:$port"
    log.debug "hostAddress set to $hostAddress"
    def headers = [:] 
    headers.put("HOST", hostAddress)
	log.debug("Executing hubaction on " + hostAddress + path)


    def hubAction = new physicalgraph.device.HubAction(
        method: "GET",
        path: path,
        headers: headers,
        device.deviceNetworkId,
        [callback: cmdResponse]
    )
    hubAction.options = [outputMsgToS3:true]
    //log.debug ("hubaction" + hubAction)
    sendHubCommand(hubAction)
}



private String convertIPtoHex(ipAddress) { 
    String hex = ipAddress.tokenize( '.' ).collect {  String.format( '%02x', it.toInteger() ) }.join()
    return hex

}

private String convertPortToHex(port) {
	String hexport = port.toString().format( '%04x', port.toInteger() )
    return hexport
}



private getHostAddress() {
	return "${ip}:${port}"
}

private showAlert(text,name,value) {
    sendEvent(
        descriptionText: text,
        eventType: "ALERT",
        name: name,
        value: value,
        displayed: true,
    )
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