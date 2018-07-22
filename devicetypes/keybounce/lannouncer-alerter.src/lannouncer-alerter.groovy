/**
 *  LANnouncer Alerter (Formerly LANdroid - but Google didn't like that much.)
 *
 *  Requires the LANnouncer android app; https://play.google.com/store/apps/details?id=com.keybounce.lannouncer
 *  See http://www.keybounce.com/LANdroidHowTo/LANdroid.html for full downloads and instructions.
 *  SmartThings thread: https://community.smartthings.com/t/android-as-a-speech-alarm-device-released/30282/12
 *  
 * Note: Only Siren and Strobe from the U.I. or Alarm capabilities default to continuous.
 *
 *  Version 1.25 22 July 2016
 * 
 *
 *  Copyright 2015-2016 Tony McNamara
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
 * To Do: Add string return to Image Capture attribute
 *
 */

metadata {
    definition (name: "LANnouncer Alerter", namespace: "KeyBounce", author: "Tony McNamara") {
        capability "Alarm"
        capability "Speech Synthesis"
        capability "Notification"
        capability "Tone"
        capability "Image Capture"
        attribute  "LANdroidSMS","string"
        /* Per http://docs.smartthings.com/en/latest/device-type-developers-guide/overview.html#actuator-and-sensor */
        capability "Sensor"
        capability "Actuator"

        // Custom Commands
        /** Retrieve image, formatted for SmartThings, from camera by name. */
        command "chime"
        command "doorbell"
        command "ipCamSequence", ["number"]
        command "retrieveAndWait", ["string"]
        command "retrieveFirstAndWait"
        command "retrieveSecondAndWait"
    }
    preferences {
        input("DeviceLocalLan", "string", title:"Android IP Address", description:"Please enter your tablet's I.P. address", defaultValue:"" , required: false, displayDuringSetup: true)
        input("DevicePort", "string", title:"Android Port", description:"Port the Android device listens on", defaultValue:"1035", required: false, displayDuringSetup: true)
        input("ReplyOnEmpty", "bool", title:"Say Nothing", description:"When no speech is found, announce LANdroid?  (Needed for the speech and notify tiles to work)", defaultValue: true, displayDuringSetup: true)
        input("AlarmContinuous", "bool", title:"Continuous Alarm (vs 10 sec.)", description: "When on, the alarm will sound until Stop is issued.", defaultValue: false, displayDuringSetup: true)
    }

    simulator {
        // reply messages
        ["strobe","siren","both","off"].each 
            {
                reply "$it": "alarm:$it"
            }
    }

    tiles {
        standardTile("alarm", "device.alarm", width: 2, height: 2) {
            state "off", label:'off', action:'alarm.both', icon:"st.alarm.alarm.alarm", backgroundColor:"#ffffff"
            state "strobe", label:'strobe!', action:'alarm.off', icon:"st.Lighting.light11", backgroundColor:"#e86d13"
            state "siren", label:'siren!', action:'alarm.off', icon:"st.alarm.alarm.alarm", backgroundColor:"#e86d13"
            state "both", label:'alarm!', action:'alarm.off', icon:"st.alarm.alarm.alarm", backgroundColor:"#e86d13"
        }
        standardTile("strobe", "device.alarm", inactiveLabel: false, decoration: "flat") {
            state "default", label:'', action:"alarm.strobe", icon:"st.secondary.strobe"
        }
        
        standardTile("siren", "device.alarm", inactiveLabel: false, decoration: "flat") {
            state "default", label:'', action:"alarm.siren", icon:"st.secondary.siren"
        }
        standardTile("off", "device.alarm", inactiveLabel: false, decoration: "flat") {
            state "default", label:'Off', action:"alarm.off", icon:"st.quirky.spotter.quirky-spotter-sound-off"
        }       
        
        /* Apparently can't show image attributes on tiles. */
        standardTile("take", "device.image", width: 1, height: 1, canChangeIcon: false, canChangeBackground: false) 
        {
            state "take", label: "Take", action: "Image Capture.take", icon: "st.camera.camera", backgroundColor: "#FFFFFF", nextState:"taking"
            state "taking", label:'Taking', action: "", icon: "st.camera.take-photo", backgroundColor: "#53a7c0"
            state "image", label: "Take", action: "Image Capture.take", icon: "st.camera.camera", backgroundColor: "#FFFFFF", nextState:"taking"
        }

        
        standardTile("speak", "device.speech", inactiveLabel: false, decoration: "flat") 
        {
            state "default", label:'Speak', action:"Speech Synthesis.speak", icon:"st.Electronics.electronics13"
        }
        standardTile("toast", "device.notification", inactiveLabel: false, decoration: "flat") {
            state "default", label:'Notify', action:"notification.deviceNotification", icon:"st.Kids.kids1"
        }
        standardTile("beep", "device.tone", inactiveLabel: false, decoration: "flat") {
            state "default", label:'Tone', action:"tone.beep", icon:"st.Entertainment.entertainment2"
        }
        carouselTile("cameraDetails", "device.image", width: 3, height: 2) { }

        main (["alarm", "take"]);
        details(["alarm","strobe","siren","off","speak", "take","toast","beep", "cameraDetails"]);
    }
}

/** Generally matches TTSServer/app/build.gradle */
String getVersion() {return "24 built July 2016";}


/** Alarm Capability, Off.
 *  Turns off both the strobe and the alarm.
 *  Necessary when in continuous mode. 
 */
def off() {
    log.debug "Executing 'off'"
    sendEvent(name:"alarm", value:"off")
    def command="&ALARM=STOP&FLASH=STOP&"+getDoneString();
    return sendCommands(command)
}

/** Alarm Capability: Strobe
 *  Flashes the camera light, if any.
 */
def strobe() {
    log.debug "Executing 'strobe'"
    // For illustration, switch to siren and sendEvent after.
    def command= (AlarmContinuous?"&FLASH=CONTINUOUS&":"&FLASH=STROBE&")+getDoneString();
    // def command=(AlarmContinuous?"&ALARM=SIREN:CONTINUOUS&":"&ALARM=SIREN&")+getDoneString();
    def hubAction = sendCommands(command)
    sendEvent(name:"alarm", value:"strobe")
    return hubAction;
}

/** Alarm Capability: Siren 
 *  Sounds the siren, either continuous or for a brief period, depending on setting.
 *  If continuous, Stop should be called later.
 */
def siren() {
    log.debug "Executing 'siren'"
    sendEvent(name:"alarm", value:"siren")
    def command=(AlarmContinuous?"&ALARM=SIREN:CONTINUOUS&":"&ALARM=SIREN&")+getDoneString();
    return sendCommands(command);
}

/** Tone Capability: Beep
 *  Sounds a short beep
 */
def beep() {
    log.debug "Executing 'beep'"
    def command="&ALARM=CHIME&"+getDoneString()
    return sendCommands(command);
}

def both() {
    log.debug "Executing 'both'"
    sendEvent(name:"alarm", value:"both")
    def command="&ALARM=ON&FLASH=ON&"+getDoneString()
    if (AlarmContinuous)
    {
        command="&ALARM=SIREN:CONTINUOUS&FLASH=CONTINUOUS&"+getDoneString()
    }
    return sendCommands(command);
}

/** speechSynthesis Capability: Speak
 */
def speak(toSay) {
    log.debug "Executing 'speak'"
    if (!toSay?.trim()) {
        if (ReplyOnEmpty) {
            toSay = "LANnouncer Version ${version}"
        }
    }

    if (toSay?.trim()) {
        def command="&SPEAK="+toSay+"&"+getDoneString()
        return sendCommands(command)
    }
}

/** Notification capability: deviceNotification
 */
def deviceNotification(toToast) {
    log.debug "Executing notification with "+toToast
    if (!toToast?.trim()) {
        if (ReplyOnEmpty) {
            toToast = "LANnouncer Version ${version}";
        }
    }
    if (toToast?.trim()) {
        def command="&TOAST="+toToast+"&"+getDoneString()
        return sendCommands(command)
    }
}    

def chime() {
    log.debug "Executing 'chime'"
    // TODO: handle 'siren' command
    def command="&ALARM=CHIME&"+getDoneString()
    return sendCommands(command)
}

def doorbell() {
    log.debug "Executing 'doorbell'"
    // TODO: handle 'siren' command
    def command="&ALARM=DOORBELL&"+getDoneString()
    return sendCommands(command)
}

def ipCamSequence(cameraNumber) {
    def camera = (cameraNumber==1?"FIRST":"SECOND");
    def command="&RETRIEVESEQ="+cameraNumber+"&"+getDoneString()
    return sendIPCommand(command, true)
}


def retrieveFirstAndWait() {
    retrieveAndWait("FIRST");
}
def retrieveSecondAndWait() {
    retrieveAndWait("SECOND");
}
def retrieveAndWait(cameraName) {
    log.info("Requesting image from camera ${cameraName}");
    def command="&RETRIEVE="+cameraName+"&STSHRINK=TRUE&"+getDoneString()
    return sendIPCommand(command, true)
}


def take() {
    // This won't result in received file. Can't handle large or binaries in hub.
    log.debug "Executing 'take'"
    def command="&PHOTO=BACK&STSHRINK=TRUE&"+getDoneString()
    return sendIPCommand(command, true)
}

/** Send to IP and to SMS as appropriate 
 *  The caller MUST return the value, which is the hubAction.
 *  As of version 1.25, does not "send" the command so much as 
 *  request that the calling service send it.
 */
private sendCommands(command) {
    log.info "Command request: "+command
    sendSMSCommand(command)
    return sendIPCommand(command)
}

/** Prepares the hubAction to be executed.
 *  Pre-V25, this was executed in-line.  
 *  Now it is returned, not executed, and must be returned up the calling chain.
 */
private sendIPCommand(commandString, sendToS3 = false) {
    log.info "Sending command "+ commandString+" to "+DeviceLocalLan+":"+DevicePort
    if (DeviceLocalLan?.trim()) {
        def hosthex = convertIPtoHex(DeviceLocalLan)
        def porthex = convertPortToHex(DevicePort)
        device.deviceNetworkId = "$hosthex:$porthex"

        def headers = [:] 
        headers.put("HOST", "$DeviceLocalLan:$DevicePort")

        def method = "GET"

        def hubAction = new physicalgraph.device.HubAction(
            method: method,
            path: "/"+commandString,
            headers: headers
            );
        if (sendToS3 == true)
        {
            hubAction.options = [outputMsgToS3:true];
        }
        log.debug hubAction
        return hubAction;
    }
}

private sendSMSCommand(commandString) {
    def preface = "+@TTSSMS@+"
    def smsValue = preface+"&"+commandString
    state.lastsmscommand = smsValue
    sendEvent(name: "LANdroidSMS", value: smsValue, isStateChange: true)
    /*
    if (SMSPhone?.trim()) {
        sendSmsMessage(SMSPhone, preface+"&"+commandString)
    }
    */
}

private String getDoneString() {
    return "@DONE@"
}

def parse(String description) {
    log.debug "Parsing '${description}'"
    def map = parseLanMessage(description);
    log.debug "As LAN: " + map;
    if ((map.headers) && (map.headers.'Content-Type' != null) && (map.headers.'Content-Type'.contains("image/jpeg")) )
    {   //  Store the file
      if(map.body) 
      {
            storeImage(getPictureName(), map.body);
      }
    }
/* 'index:0F, mac:0073E023A13A, ip:C0A80114, port:040B, requestId:f9036fb2-9637-40b8-b2c5-71ba5a09fd3e, bucket:smartthings-device-conn-temp, key:fc8e3dfd-5035-40a2-8adc-a312926f9034' */

    else if (map.bucket && map.key)
    { //    S3 pointer; retrieve image from it to store.
        try {
            def s3ObjectContent; // Needed for scope of try-catch
            def imageBytes = getS3Object(map.bucket, map.key + ".jpg")

            if(imageBytes)
            {
                log.info ("Got image bytes; saving them.")
                s3ObjectContent = imageBytes.getObjectContent()
                def bytes = new ByteArrayInputStream(s3ObjectContent.bytes)
                storeImage(getPictureName(), bytes)
            }
        }
        catch(Exception e) 
        {
            log.error e
        }
        finally {
            //explicitly close the stream
            if (s3ObjectContent) { s3ObjectContent.close() }
        }        
    }
}


// Image Capture handling
/* Note that images are stored in https://graph.api.smartthings.com/api/s3/smartthings-smartsense-camera/[IMAGE-ID4], 
 * where [IMAGE-ID] is listed in the IDE under My Devices > [Camera] > Current States: Image. That page is updated as pictures are taken.
 */


private getPictureName() {
    def pictureUuid = java.util.UUID.randomUUID().toString().replaceAll('-', '')
    log.debug pictureUuid
    def picName = device.deviceNetworkId.replaceAll(':', '') + "_$pictureUuid" + ".jpg"
    return picName
}


private String convertIPtoHex(ipAddress) { 
    String hex = ipAddress.tokenize( '.' ).collect {  String.format( '%02X', it.toInteger() ) }.join()
    log.debug "IP address entered is $ipAddress and the converted hex code is $hex"
    return hex

}

private String convertPortToHex(port) {
    String hexport = port.toString().format( '%04X', port.toInteger() )
    log.debug hexport
    return hexport
}