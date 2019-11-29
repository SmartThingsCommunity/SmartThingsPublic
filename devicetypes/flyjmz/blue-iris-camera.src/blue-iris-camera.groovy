/*
Blue Iris Camera Device Type Handler

Copyright 2017 FLYJMZ (flyjmz230@gmail.com)

Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
in compliance with the License. You may obtain a copy of the License at:

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
for the specific language governing permissions and limitations under the License.
*/


//////////////////////////////////////////////////////////////////////////////////////////////
///										App Info											//
//////////////////////////////////////////////////////////////////////////////////////////////
/*
This is the Camera device for Blue Iris Software, and must be used with the BI Fusion smartapp and Blue Iris Server DTH (see below).  
It cannot function on its own.

SmartThings Community Thread:   
		https://community.smartthings.com/t/release-bi-fusion-v3-0-adds-blue-iris-device-type-handler-blue-iris-camera-dth-motion-sensing/103032

Github Code: 
		https://github.com/flyjmz/jmzSmartThings/tree/master/devicetypes/flyjmz/blue-iris-camera.src

Version History:
v1.0 26Oct17	Initial commit
v1.1 2Nov17		BI Fusion updated to allow user to change Camera Device Names after installation, removed warnings in this DTH.
				(Must change in devices' settings page, the change in BI Fusion preferences is irrelevant unless the shortname changes as well).
                Beta - Added Video Live Stream, but doesn't seem to work
v1.2 26Nov17	Video did work! Only change for v1.2 is updated log.info/trace/debug to not post passwords all the time.
v1.3 5Mar18		Tried Image capture, but it gets weird because the captured image would go to the BI Server DTH's parse, 
				so you'd either have to go there to see it, or figure out how to send it back here...Abandoned
                Added "Sensor" and "actuator" Capability
                Added "moveToPreset" command so webcore (and others) can call "moveToPreset.cameraPreset(#)" to move camera to specific presets. -requested by @jrfarrar
v1.4 17Apr18    Allows user to change the icon per @jasonrwise77 request
v1.5 5Feb19		Added Camera name to notifications. Added some todos.
v1.5.1 6Feb19	Changed notifications to display name instead of shortname.


ToDo:
- add code for new smartthings app to work (mnmn: “SmartThings”, vid: “generic-motion-3”) from post, but whole thread on all of them.
*/

def appVersion() {"1.5.1"}

metadata {
    definition (name: "Blue Iris Camera", namespace: "flyjmz", author: "flyjmz230@gmail.com") {
        capability "Motion Sensor"  //To treat cameras as a motion sensor for other apps (e.g. BI camera senses motion, setting this device to active so an alarm can subscribe to it and go off
        capability "Switch"  //To trigger camera recording for other smartapps that may not accept momentary
        capability "Momentary" //To trigger camera recording w/momentary on
        capability "Video Camera"
		capability "Refresh" //todo - if you pull down for a refresh it looks for the command for this, except it isn't defined below!
        capability "Sensor"
        capability "Actuator"
        attribute "cameraShortName", "string"
        attribute "errorMessage", "String"
       	//attribute "image", "string"
        attribute "cameraPreset", "Number"
        command "active"
        command "inactive"
        command "on"
        command "off"
        //command "take"
        command "start"
        command "initializeCamera"
        command "moveToPreset", ["number"]
    }


    simulator {
    }

    tiles (scale: 2) {
        multiAttributeTile(name: "videoPlayer", type: "videoPlayer", width: 6, height: 4) {
            tileAttribute("device.errorMessage", key: "CAMERA_ERROR_MESSAGE") {
                attributeState("errorMessage", label: "", value: "", defaultState: true)
            }

            tileAttribute("device.camera", key: "PRIMARY_CONTROL") {
                attributeState("on", label: "Active", backgroundColor: "#79b821", defaultState: true)
                attributeState("off", label: "Inactive", backgroundColor: "#ffffff")
                attributeState("restarting", label: "Connecting", backgroundColor: "#53a7c0")
                attributeState("unavailable", label: "Unavailable", backgroundColor: "#F22000")
            }

            tileAttribute("device.startLive", key: "START_LIVE") {
                attributeState("live", action: "start", defaultState: true)
            }

            tileAttribute("device.stream", key: "STREAM_URL") {
                attributeState("activeURL", defaultState: true)
            }
        }

        standardTile("motion", "device.motion", width: 4, height: 2, canChangeIcon: true, canChangeBackground: true) {
            state "inactive", label: 'No Motion', icon:"st.motion.motion.inactive", backgroundColor:"#ffffff"
            state "active", label: 'Motion', icon:"st.motion.motion.active", backgroundColor:"#53a7c0"
        }
        
        standardTile("button", "device.switch", width: 2, height: 2, canChangeIcon: false, canChangeBackground: true) {
            state "off", label: 'Record', action: "switch.on", icon: "st.switch.switch.off", backgroundColor: "#ffffff"
            state "on", label: 'Recording', icon: "st.switch.switch.on", backgroundColor: "#53a7c0"  //no action because you can't untrigger a camera
        }
        main (["motion"])
        details(["videoPlayer","motion","button"])
    }
    preferences {
    }
}

def initializeCamera(cameraSettings) {
	state.cameraSettings = cameraSettings
    log.info "state.cameraSettings is: ${state.cameraSettings}"
    sendEvent(name: "motion", value: "inactive", descriptionText: "${state.cameraSettings.displayName} Camera Motion Inactive", displayed: false)  //initializes camera motion state
    log.info "${state.cameraSettings.shortName} camera DTH initialized"
}

def parse(String description) {  //Don't need to parse anything because it's all to/from server device then service manager app.
	log.info "Parsing '${description}'"
}

def on() {   //Trigger to start recording with BI Camera
	log.info "${state.cameraSettings.shortName} Executing 'on'."
    sendEvent(name: "switch", value: "on", descriptionText: "${state.cameraSettings.displayName.replaceAll("\\s","")} Recording Triggered", displayed: true)  //NOTE: "state.cameraSettings.displayName" doesn't work within descriptionText when the displayName contains spaces.  Also "device.displayName", "device.label", and "label" do not work within descriptionText.   
    runIn(10,off)
}

def off() {  //Can't actually turn off recording, the trigger is for a defined period in Blue Iris Settings for each camera and profile, this just puts the tile back to normal.
	log.info "${state.cameraSettings.shortName} Executing 'off'"
    sendEvent(name: "switch", value: "off", descriptionText: "${state.cameraSettings.displayName.replaceAll("\\s","")} Recording Trigger Ended", displayed: true)
}

def active() {  //BI Camera senses motion
	log.info "${state.cameraSettings.shortName} Motion 'active'"
	sendEvent(name: "motion", value: "active", descriptionText: "${state.cameraSettings.displayName.replaceAll("\\s","")} Camera Motion Active", displayed: true)
}

def inactive() {  //BI Camera no longer senses motion
	log.info "${state.cameraSettings.shortName} Motion 'inactive'"
    sendEvent(name: "motion", value: "inactive", descriptionText: "${state.cameraSettings.displayName.replaceAll("\\s","")} Camera Motion Inactive", displayed: true)
}

def push() {
	log.info "${state.cameraSettings.shortName} Executing 'push'"
	on()
}

def moveToPreset(preset) {
	def receivedPreset = preset
	log.info "${state.cameraSettings.shortName} commanded to move to preset '${receivedPreset}'"
    sendEvent(name: "cameraPreset", value: "$receivedPreset", descriptionText: "${state.cameraSettings.displayName.replaceAll("\\s","")} Camera Commanded to Preset $receivedPreset", displayed: true)
}

def start() {
	log.info "${state.cameraSettings.shortName} start()"
   	def cameraStreamPath = "http://${state.cameraSettings.username}:${state.cameraSettings.password}@${state.cameraSettings.host}:${state.cameraSettings.port}/mjpg/${state.cameraSettings.shortName}"  //todo 
    //todo - (continued) if the user is using external and enters "http://" in the host entry like they should, then you get http:// twice and it breaks.  
    //I don't think this is the right path to use for true external anyway...look that up.
    def dataLiveVideo = [
		OutHomeURL  : cameraStreamPath,
		InHomeURL   : cameraStreamPath,
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
	sendEvent(event)
}