/**
 *  Sleep Rules
 *
 *  Copyright 2015 SmartThings
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
 
import java.text.DecimalFormat

definition(
    name: "Sleep Routine Automation",
    namespace: "smartsolutionsv2/sleeping",
    parent: "smartsolutionsv2/sleeping:Sleep Routine",
    author: "SmartThings",
    description: "Sleeping automation rule.",
    category: "My Apps",
    iconUrl: "https://s3.amazonaws.com/smartthings-plus/category-icons/sleepsense.png",
    iconX2Url: "https://s3.amazonaws.com/smartthings-plus/category-icons/sleepsense.png",
    iconX3Url: "https://s3.amazonaws.com/smartthings-plus/category-icons/sleepsense.png"
)


preferences {
    page(name: "mainPage", title: getLabel("str_MainPageTitle"), install: false, uninstall: true, nextPage: "namePage");
    page(name: "namePage", title: getLabel("str_NamePageTitle"), install: true, uninstall: true);
    page(name: "timeIntervalInput", title: getLabel("str_TimeSet"));
}



def updateSolutionSummary() {
}

def mainPage() {        
    dynamicPage(name: "mainPage") {
        section {
            sleepSensorInput();
            triggerInput();
        }
        controlType();
        controlledInput();
        otherInputs();
    }
}

//Sleep sensor input
def sleepSensorInput() {
    input(name: "sleepSensor", type: "capability.sleepSensor", title: getLabel("str_SelectSense"), multiple: false, submitOnChange: true);
}


//Sleep sensor action to trigger event
private actionMap() {
    state.sleepActionMap = ["sleeping": getLabel("str_sleep"), "not sleeping": getLabel("str_wake")];
    if(sleepSensor.find{it.hasAttribute("bedstate")} != null) {
        state.sleepActionMap.put("in bed", getLabel("str_inbed"));
        state.sleepActionMap.put("out of bed", getLabel("str_outofbed"));
    }
    return state.sleepActionMap;
}

private actionOptions() {
    actionMap().collect{[(it.key): it.value]}
}

def triggerInput() {
    def requiredInput = androidClient() || iosClient("1.7.0.RC1");
    if(sleepSensor) {
        input(name: "trigger", type: "enum", title: getLabel("str_SelectSleepTrigger"), options: actionOptions(), required: true, submitOnChange: true);
    } 
}

// What type should be controlled
private controlType() {
    def requiredInput = androidClient() || iosClient("1.7.0.RC1");
    def mapControlTypes 
//    if(clientLocale?.language == "ko")
//    {
//        mapControlTypes = [device: "${location.name}${getLabel("str_HubofDevice")}", homeWatch: getLabel("str_HubofHomeWatch"), mode: "${location.name}${getLabel("str_HubofMode")}", routine: "${location.name}${getLabel("str_HubofRoutines")}"];
//    }
//    else
//    {
        mapControlTypes = [device: "${getLabel("str_HubofDevice")}${location.name}", homeWatch: getLabel("str_HubofHomeWatch"), mode: "${getLabel("str_HubofMode")}${location.name}", routine: "${getLabel("str_HubofRoutines")}${location.name}"];
//    }

    

    if(settings.trigger) {
        section() {
            input(name: "controlType", type: "enum", title: getLabel("str_SelectLocation"), options: mapControlTypes, required: requiredInput, submitOnChange: true);
        }
    }
}

//Device to control with sleep sensor input
private lightMap() {
    def lightActionMap = [on: getLabel("str_DeviceTurnOn"), off: getLabel("str_DeviceTurnOff")];
    if (controlledDevice.find{it.hasCapability("Switch Level")} != null) {
        lightActionMap.level = getLabel("str_DeviceLevel")
    }
    if (controlledDevice.find{it.hasCapability("Color Control")} != null) {
        lightActionMap.color = getLabel("str_DeviceColor")
    }
    return lightActionMap;
}

private lightOptions() {
    lightMap().collect{[(it.key): it.value]}
}

def controlledInput() {    
    def requiredInput = androidClient() || iosClient("1.7.0.RC1");
    def mapControlledDeivces = [switch: getLabel("str_DeviceSwitch"), imageCapture: getLabel("str_DeviceCameras"), lock: getLabel("str_DeviceLocks"), thermostat: getLabel("str_DeviceThermostats"), alarm: getLabel("str_DeviceAlarms"), musicPlayer: getLabel("str_DeviceSpeaker")];
    
    log.debug "control type ${settings.controlType}";

    if(settings.controlType && settings.controlType == "device") {
        section(getLabel("str_DeviceTitle")) {
            input(name: "controlledDeviceType", type: "enum", title: getLabel("str_DeviceSelect"), options: mapControlledDeivces, required: requiredInput, submitOnChange: true);
        }
        
        if(controlledDeviceType) {
            def deviceType;
            
            section() {
                input(name: "controlledDevice", type: "capability.${controlledDeviceType}", required: true, multiple: true, submitOnChange: true);
            }

            if(controlledDevice) {
                def label = controlledDevice.size() == 1? "${controlledDevice[0].label !=null ? controlledDevice[0].label : controlledDevice[0]} Control" : "${controlledDevice[0].label != null ? controlledDevice[0].label : controlledDevice[0]}, etc Control";
                section("${label}") {
                    switch(controlledDeviceType) {
                        case "switch":                            
                            input(name: "actionsLights", type: "enum", title: getLabel("str_DeviceLightsTitle"), options: lightOptions(), multiple: false, required: requiredInput, submitOnChange: true);
                            if(actionsLights == "color") {
                                input(name: "color", type: "enum", title: "Color", required: true, multiple: false, options: [
                                    ["Soft White":"Soft White - Default"],
                                    ["White":"White - Concentrate"],
                                    ["Daylight":"Daylight - Energize"],
                                    ["Warm White":"Warm White - Relax"],
                                    "Red","Green","Blue","Yellow","Orange","Purple","Pink"]);
                            }
                            if(actionsLights == "level") {
                                input(name: "level", type: "number", title: "Dimmer Level", defaultValue: 50, required: true, submitOnChange: true);
                            }

                            def nonDimmableLights = "";
                            def nonColorControlLights = "";
                            def isareLevel = "";
                            def hasSwitchLevel = false;
                            def hasColorControl = false;
                            controlledDevice.eachWithIndex { item, index ->
                                if(item.hasCapability("Switch Level") == null) {
                                    if(nonDimmableLights == "") {
                                        nonDimmableLights += "${item.label != null ? item.label : item}" ;
                                        isareLevel = "is";
                                    } else {
                                        nonDimmableLights += ", ${item.label != null ? item.label : item}";
                                        if(isareLevel == "is") {
                                            isareLevel = "are";
                                        }
                                    }
                                } else {
                                    hasSwitchLevel = true;
                                }
                                if(item.hasCapability("Color Control") == null) {
                                    if(nonColorControlLights == "") {
                                        nonColorControlLights += "${item.label != null ? item.label : item}";
                                    } else {
                                        nonColorControlLights += ", ${item.label != null ? item.label : item}";
                                    }
                                } else {
                                    hasColorControl = true;
                                }
                            }

                            if(hasSwitchLevel && nonDimmableLights != "") {
                                paragraph "${nonDimmableLights} ${isareLevel} not dimmable. These will be turned on to 100%.";
                            }

                            if(hasColorControl && nonColorControlLights != "") {
                                paragraph "${nonColorControlLights} cannot set color.";
                            }

                            break;
                        case "imageCapture":
                            input(name: "imageCaptureAction", type: "enum", title: getLabel("str_CameraTitle"), options: [take: getLabel("str_CameraTake")], multiple: false, required:true, submitOnChange: true);
                            break;
                        case "lock":
                            input(name: "lockAction", type: "enum", title: getLabel("str_LocksTitle"), options: [lock: getLabel("str_LocksLock"), unlock: getLabel("str_LocksUnLock")], multiple: false, required: true, submitOnChange: true);
                        break;
                        case "thermostat":                            
                            input(name: "thermostatMode", type: "enum", title: getLabel("str_DeviceThermostatTitle"), options: [modeCool: getLabel("str_DeviceThermostatCool"), modeHeat: getLabel("str_DeviceThermostatHeat"), off: getLabel("str_DeviceTurnOff")], required: requiredInput, multiple: false, submitOnChange: true);
                            
                            switch(thermostatMode) {
                                case "modeCool":
                                    input(name: "thermostatCoolTemp", type: "number", title: getLabel("str_DeviceThermostatCoolSet"), defaultValue: 76, required: true, submitOnChange: true);
                                    break;
                                case "modeHeat":
                                    input(name: "thermostatHeatTemp", type: "number", title: getLabel("str_DeviceThermostatHeatSet"), defaultValue: 86, required: true, submitOnChange: true);
                                    break;
                            }
                            break;
                        case "alarm":
                            input(name: "alarmAction", type: "enum", title: getLabel("str_AlarmWill"), options: [strobe: "Strobe the lights", siren: "Sound the siren", both: "Strobe the lights & sound the alarm", off: "turn Off"], multiple: false, required: true, submitOnChange: true);
                            break;
                        case "musicPlayer":
                            input(name: "musicPlayerAction", type: "enum", title: getLabel("str_DeviceSpeakerTitle"), options: [play: getLabel("str_DeviceSpeakerPlae"), pause: getLabel("str_DeviceSpeakerPause"), stop: getLabel("str_DeviceSpeakerStop")], required: true, multiple: false, submitOnChange: true);
                            break;
                    }
                }
            }
        }
    }
    if(settings.controlType && settings.controlType == "homeWatch") {
        section(getLabel("str_HomeWatchTitle")) {
            input(name: "homeWatchAction", type: "enum", title: getLabel("str_HomeWatchSectionSelect"), required: true, multiple: false, options: [off: "Disarm", stay: "Arm(Stay)", arm: "Arm(Away)"], submitOnChange: true);
        }
    }
    if(settings.controlType && settings.controlType == "mode") {
        section(getLabel("strr_ModeTitle")) {
            input(name: "modeAction", type: "mode", title: getLabel("str_ModeSelect"), required: true, multiple: false, submitOnChange: true);
        }
    }
    if(settings.controlType && settings.controlType == "routine") {
        def routines = location.helloHome?.getPhrases()*.label;
        if(routines) {
            section(getLabel("str_RoutineTitle")) {
                input(name: "routineAction", type: "enum", title: getLabel("str_RoutineSelect"), options: routines, required: true, multiple: false, submitOnChange: true);
            }
        }
    }
}


def otherInputs() {
    if (settings.trigger) {
        def timeLabel = timeIntervalLabel()
        section(title: getLabel("str_Moreoption"), hidden: hideOptionsSection(), hideable: true) {
            def timeBasedTrigger = trigger in ["At Sunrise", "At Sunset", "At a Specific Time"]
            log.trace "timeBasedTrigger: $timeBasedTrigger"
            if (!timeBasedTrigger) {
                href "timeIntervalInput", title: getLabel("str_TimeSet"), description: timeLabel ?: getLabel("str_Taptoset"), state: timeLabel ? "complete" : "incomplete"
            }

            input "days", "enum", title: getLabel("str_WeekSet"), multiple: true, required: false,
                options: [getLabel("str_Monday"), getLabel("str_Tuesday"), getLabel("str_Wednesday"), getLabel("str_Thursday"), getLabel("str_Friday"), getLabel("str_Saturday"), getLabel("str_Sunday")]

            //input "modes", "mode", title: "Only when mode is", multiple: true, required: false
        }
    }
}

def timeIntervalInput() {
    dynamicPage(name: "timeIntervalInput") {
        section {
            input "startTimeType", "enum", title: getLabel("str_TimeStartingCondition"), options: [["time": getLabel("str_TimeSpecificTime")], ["sunrise": getLabel("str_TimeSunrise")], ["sunset": getLabel("str_TimeSunset")]], defaultValue: "time", submitOnChange: true
            if (startTimeType in ["sunrise","sunset"]) {
                input "startTimeOffset", "number", title: getLabel("str_TimeOffset"), range: "*..*", required: false
            }
            else {
                input "starting", "time", title: getLabel("str_TimeStart"), required: false
            }
        }
        section {
            input "endTimeType", "enum", title: getLabel("str_TimeEngingCondition"), options: [["time": getLabel("str_TimeSpecificTime")], ["sunrise": getLabel("str_TimeSunrise")], ["sunset": getLabel("str_TimeSunset")]], defaultValue: "time", submitOnChange: true
            if (endTimeType in ["sunrise","sunset"]) {
                input "endTimeOffset", "number", title: getLabel("str_TimeOffset"), range: "*..*", required: false
            }
            else {
                input "ending", "time", title: getLabel("str_TimeEnd"), required: false
            }
        }
    }
}

private hideOptionsSection() {
    (starting || startTimeType in ["sunrise","sunset"] || ending || endTimeType in ["sunrise","sunset"] || days || modes) ? false : true
}

private String generateDefaultLabel() {
    def sleepSensor = sleepSensor.label;
    def trigger = state.sleepActionMap[trigger];
    def device = "";
    
    if(controlledDevice) {
        device = controlledDevice.size() == 1 ? "${controlledDevice[0].label != null ? controlledDevice[0].label : controlledDevice[0]}": "${controlledDevice[0].label != null ? controlledDevice[0].label : controlledDevice[0]}, etc";
    } else if(modeAction) {
        device = "${modeAction}";
    } else if(homeWatchAction) {
        device = "Smart Home Monitor";
    }

    def defaultLabel = "${trigger}";
    
    if(controlType == "device") {
        switch(controlledDeviceType) {
            case "switch":
                switch(actionsLights) {
                case "on":
                    return "${defaultLabel} turn on ${device}";
                    break;
                case "off":
                    return "${defaultLabel} turn off ${device}";
                    break;
                case "color":
                    return "${defaultLabel} turn on ${device} and set color to ${color}";
                    break;
                case "level":
                    return "${defaultLabel} turn on ${device} and set level to ${level}";
                    break;
                }
                break;
    
            case "imageCapture":
                return "${defaultLabel} have ${device} capture Image"
                break;
    
            case "lock":
                return "${defaultLabel} ${lockAction[0].toLowerCase() + lockAction.substring(1)} ${device}";
                break;
    
            case "thermostat":
                switch(thermostatMode) {
                    case "modeCool":
                        return "${defaultLabel} turn ${device} on, change cooling setpoint to ${thermostatCoolTemp}, and set mode to cool."
                        break;
                    case "modeHeat":
                        return "${defaultLabel} turn ${device} on, change cooling setpoint to ${thermostatHeatTemp}, and set mode to heat."
                        break;
                    case "off":
                        return "${defaultLabel} turn ${device} off."
                        break;
                }
                break;
    
            case "alarm":
                return "${defaultLabel} ${alarmAction[0].toLowerCase() + alarmAction.substring(1)} ${device}";
                break;
    
            case "musicPlayer":
                return "${defaultLabel} ${musicPlayerAction[0].toLowerCase() + musicPlayerAction.substring(1)} ${device}"
                break;
        }
    } else if(controlType == "homeWatch") {
        return "${defaultLabel} ${homeWatchAction} Smart Home Monitor";
    } else if(controlType == "mode") {
        return "${defaultLabel} change mode to ${modeAction}";
    } else if(controlType == "routine") {
        return "${defaultLabel} run routine ${routineAction}";
    }
}

def namePage() {
    if(!overrideLabel) {
        app.updateLabel(generateDefaultLabel());
    }
    dynamicPage(name: "namePage") {
            section(title: getLabel("str_NameAutomation")) {
                label(title: getLabel("str_NameUser"), defaultValue: generateDefaultLabel(), required: false);
                }

        /*
		if(overrideLabel) {
            section(title: getLabel("str_NameAutomation")) {
                label(title: getLabel("str_NameUser"), defaultValue: generateDefaultLabel(), required: false);
            }
        } else {
            section(title: getLabel("str_NameAutomation")) {
                paragraph app.label;
            }
        }
        section {
            input(name: "overrideLabel", type: "bool", title: getLabel("str_NameChange"), required: false, submitOnChange: true);
        }*/
    }
}

def installed() {
    log.debug "Installed with settings: ${settings}"
    initialize()
}

def updated() {
    log.debug "Updated with settings: ${settings}"
    
    unsubscribe()
    unschedule()
    
    initialize()
}

def initialize() {
    if(!overrideLabel) {
        app.updateLabel(generateDefaultLabel());
    }
    
    subscribe(sleepSensor, "bedstate", eventHandler);
    subscribe(sleepSensor, "sleeping", eventHandler);
    
    updateSolutionSummary();
}

def eventHandler(evt) {
    if(evt.value == trigger) {
        if(getAllOk()) {
            log.debug "all ok";
            switch(controlType) {
                case "device":
                    if(controlledDevice) {
                        switch(controlledDeviceType) {
                            case "switch":                            
                                if(actionsLights == "on") {
                                    controlledDevice.on();
                                    sendMessage();
                                }
                                if(actionsLights == "off") {
                                    controlledDevice.off();
                                    sendMessage();
                                }
                                if(actionsLights == "color" || actionsLights == "level") {
                                    setColor();
                                }
                                break;
                            case "imageCapture":
                                controlledDevice."${imageCaptureAction}"();
                                sendMessage();
                                break;
                            case "lock":
                                controlledDevice."${lockAction}"();
                                sendMessage();
                                break;
                            case "thermostat":                       
                                if(thermostatMode == "modeCool") {
                                    controlledDevice.setCoolingSetpoint(thermostatCoolTemp);
                                    controlledDevice.cool();
                                    sendMessage();
                                }
                                if(thermostatMode == "modeHeat") {
                                    controlledDevice.setHeatingSetpoints(thermostatHeatTemp);
                                    controlledDevice.heat();
                                    sendMessage();
                                }
                                break;
                            case "alarm":
                                controlledDevice."${alarmAction}"();
                                sendMessage();
                                break;
                            case "musicPlayer":
                                controlledDevice."${musicPlayerAction}"();
                                sendMessage();
                                break;   
                        }
                    }
                    break;
                case "homeWatch":
                    log.debug "homeWatch";
                    sendLocationEvent(name: "alarmSystemStatus", value: "${homeWatchAction}");
                    sendMessage();
                    break;
                case "mode":
                    log.debug "mode";
                    location.setMode("${modeAction}");
                    sendMessage();
                    break;
                case "routine":
                    log.debug "routine";
                    location.helloHome?.execute("${routineAction}");
                    sendMessage();
                    break;
            }
        }
    }
}

private setColor() {

    def hueColor = 0
    def saturation = 100

    switch(color) {
        case "White":
            hueColor = 52
            saturation = 19
            break;
        case "Daylight":
            hueColor = 53
            saturation = 91
            break;
        case "Soft White":
            hueColor = 23
            saturation = 56
            break;
        case "Warm White":
            hueColor = 20
            saturation = 80
            break;
        case "Blue":
            hueColor = 70
            break;
        case "Green":
            hueColor = 39
            break;
        case "Yellow":
            hueColor = 25
            break;
        case "Orange":
            hueColor = 10
            break;
        case "Purple":
            hueColor = 75
            break;
        case "Pink":
            hueColor = 83
            break;
        case "Red":
            hueColor = 100
            break;
    }

    def value = [switch: "on", hue: hueColor, saturation: saturation, level: 100]

    controlledDevice.each {
        it.on()
        if (actionsLights.contains("color")) {
            if(it.hasCapability("Color Control"))
                it.setColor(value);
                sendMessage();
        }
        if (actionsLights.contains("level")) {
            if(it.hasCapability("Switch Level"))
                it.setLevel(level as Integer ?: 100);
                sendMessage();
        }
    }
}

private sendMessage(evt = [:]) {
    def color = "#79b821";
    def icon = "st.switches.light.on";
    def actionLabel = "";

    log.debug "control type: ${controlType}"

    def controlledDeviceLabel = controlledDevice.size() == 1 ? "${controlledDevice[0].label != null ? controlledDevice[0].label : controlledDevice[0]}" : "${controlledDevice[0].label != null ? controlledDevice[0].label : controlledDevice[0]}, ect"

    if(controlType == "device") {
        switch(controlledDeviceType) {
            case "switch":                            
                switch(actionLights) {
                    case "on":
                        icon = "st.switches.light.on";
                        actionLabel = "Turned on ${controlledDevice.label}";
                        break;
                    case "off":
                        color = "#878787";
                        icon = "st.switches.light.off";
                        actionLabel = "Turned off ${controlledDevice.label}";
                        break;
                    case "color":
                        icon = "st.switches.light.on";
                        actionLabel = "Turned on ${controlledDevice.label} and set color to ${color}";
                        break;
                    case "level":
                        icon = "st.switches.light.on";
                        actionLabel = "Turned on ${controlledDevice.label} and set level to ${level}";
                        break;
                }
                
                if (app.currentState("status")?.value != actionLights || app.currentState("status") == null) {
                    def text = "$app.label $actionLabel"
                    sendEvent(name: "status", value: actionLights, linkText: app.label,
                      descriptionText: text, eventType:"SOLUTION_EVENT", data: [icon: icon, backgroundColor: color])
                }
                break;
            case "imageCapture": 
                icon = "st.camera.take-photo";
                actionLabel = "${controlledDevice.label} captured image";
                sendEvent(name: "status", value: imageCaptureAction, linkText: app.label,
                    descriptionText: text, eventType: "SOLUTION_EVENT", data: [icon: icon, backgroundColor: color])
                break;
            case "lock":
                if(lockAction == "lock") {
                    icon = "st.locks.lock.locked";
                    actionLabel = "${controlledDevice.label} has been locked";
                    sendEvent(name: "status", value: lockAction, linkText: app.label,
                        descriptionText: text, eventType: "SOLUTION_EVENT", data: [icon: icon, backgroundColor: color])
                } else if(lockAction == "unlock") {
                    icon = "st.locks.lock.unlocked";
                    actionLabel = "${controlledDevice.label} has been unlocked";
                    sendEvent(name: "status", value: lockAction, linkText: app.label,
                        descriptionText: text, eventType: "SOLUTION_EVENT", data: [icon: icon, backgroundColor: color])
                }
                break;
            case "thermostat":                       
                if(thermostatMode == "modeCool") {
                    icon = "st.thermostat.cool";
                    actionLabel = "${controlledDevice.label} has cooling setpoint of ${thermostatCoolTemp} and is in cooling mode."
                }
                if(thermostatMode == "modeHeat") {
                    icon = "st.thermostat.heat";
                    actionLabel = "${controlledDevice.label} has heating setpoint of ${thermostatCoolTemp} and is in heating mode."
                }
                
                if (app.currentState("status")?.value != thermostatMode || app.currentState("status") == null) {
                    def text = "$app.label $actionLabel"
                    sendEvent(name: "status", value: thermostatMode, linkText: app.label,
                      descriptionText: text, eventType:"SOLUTION_EVENT", data: [icon: icon, backgroundColor: color])
                }
                break;
            case "alarm":
                switch(alarmAction) {
                    case "strobe":
                        icon = "st.security.alarm.alarm";
                        actionLabel = "Strobe the lights on ${controlledDevice.label}";
                        sendEvent(name: "status", value: alarmAction, linkText: app.label,
                            descriptionText: text, eventType: "SOLUTION_EVENT", data: [icon: icon, backgroundColor: color])
                        break;
                    case "siren":
                        icon = "st.security.alarm.alarm";
                        actionLabel = "Sound siren on ${controlledDevice.label}";
                        sendEvent(name: "status", value: alarmAction, linkText: app.label,
                            descriptionText: text, eventType: "SOLUTION_EVENT", data: [icon: icon, backgroundColor: color])
                        break;
                    case "both":
                        icon = "st.security.alarm.alarm";
                        actionLabel = "Strobe the lights and sound the siren on ${controlledDevice.label}";
                        sendEvent(name: "status", value: alarmAction, linkText: app.label,
                            descriptionText: text, eventType: "SOLUTION_EVENT", data: [icon: icon, backgroundColor: color])
                        break;
                    case "off":
                        icon = "st.security.alarm.clear";
                        color = "#878787";
                        actionLabel = "Turn ${controlledDevice.label} off";
                        sendEvent(name: "status", value: alarmAction, linkText: app.label,
                            descriptionText: text, eventType: "SOLUTION_EVENT", data: [icon: icon, backgroundColor: color])
                        break;
                }
                break;
            case "musicPlayer":
                switch(musicPlayerAction) {
                    case "play":
                        icon = "st.sonos.play-btn";
                        actionLabel = "Play music on ${controlledDevice.label}";
                        sendEvent(name: "status", value: musicPlayerAction, linkText: app.label,
                            descriptionText: text, eventType: "SOLUTION_EVENT", data: [icon: icon, backgroundColor: color])
                        break;
                    case "pause":
                        icon = "st.sonos.pause-btn";
                        color = "#878787";
                        actionLabel = "Pause music on ${controlledDevice.label}";
                        sendEvent(name: "status", value: musicPlayerAction, linkText: app.label,
                            descriptionText: text, eventType: "SOLUTION_EVENT", data: [icon: icon, backgroundColor: color])
                        break;
                    case "stop":
                        icon = "st.sonos.stop-btn";
                        color = "#878787";
                        actionLabel = "Stop music on ${controlledDevice.label}";
                        sendEvent(name: "status", value: musicPlayerAction, linkText: app.label,
                            descriptionText: text, eventType: "SOLUTION_EVENT", data: [icon: icon, backgroundColor: color])
                        break;
                }
                break;
        }
    } else if(controlType == "homeWatch") {
        switch(homeWatchAction) {
            case "off":
                icon = "st.security.alarm.off";
                color = "#878787";
                actionLabel = "Smart Home Monitoring is off";
                sendEvent(name: "status", value: homeWatchAction, linkText: app.label,
                    descriptionText: text, eventType: "SOLUTION_EVENT", data: [icon: icon, backgroundColor: color])
                break;
            case "stay":
                icon = "st.security.alarm.partial";
                color = "#F0F015";
                actionLabel = "Smart Home Monitoring is in arm";
                sendEvent(name: "status", value: homeWatchAction, linkText: app.label,
                    descriptionText: text, eventType: "SOLUTION_EVENT", data: [icon: icon, backgroundColor: color])
                break;
            case "arm":
                icon = "st.security.alarm.on";
                actionLabel = "Smart Home Monitoring is armed";
                sendEvent(name: "status", value: homeWatchAction, linkText: app.label,
                    descriptionText: text, eventType: "SOLUTION_EVENT", data: [icon: icon, backgroundColor: color])
                break;
        }
    } else if(controlType == "mode") {
        icon = "st.Home.home2";
        actionLabel = "${location.name} changed modes to ${modeAction}";
        sendEvent(name: "status", value: modeAction, linkText: app.label,
            descriptionText: text, eventType: "SOLUTION_EVENT", data: [icon: icon, backgroundColor: color])
    } else if(controlType == "routine") {
        icon = "st.Home.home2";
        actionLabel = "Execute ${routineAction} at ${location.name}";
        sendEvent(name: "status", value: routineAction, linkText: app.label,
            descriptionText: text, eventType: "SOLUTION_EVENT", data: [icon: icon, backgroundColor: color])
    }
}

// TODO - centralize somehow
private getAllOk() {
    modeOk && daysOk && timeOk
}

private getModeOk() {
    def result = !modes || modes.contains(location.mode)
    log.trace "modeOk = $result"
    result
}

private getDaysOk() {
    def result = true
    if (days) {
        def df = new java.text.SimpleDateFormat("EEEE")
        if (location.timeZone) {
            df.setTimeZone(location.timeZone)
        }
        else {
            df.setTimeZone(TimeZone.getTimeZone("America/Los_Angeles"))
        }
        def day = df.format(new Date())
        result = days.contains(day)
    }
    log.trace "daysOk = $result"
    result
}

private getTimeOk() {
    def result = true
    def start = timeWindowStart()
    def stop = timeWindowStop()
    if (start && stop) {
        result = timeOfDayIsBetween(start, stop, new Date(), location.timeZone)
    }
    log.trace "timeOk = $result"
    result
}

private timeWindowStart() {
    def result = null
    if (startTimeType == "sunrise") {
        result = location.currentState("sunriseTime")?.dateValue
        if (result && startTimeOffset) {
            result = new Date(result.time + Math.round(startTimeOffset * 60000))
        }
    }
    else if (startTimeType == "sunset") {
        result = location.currentState("sunsetTime")?.dateValue
        if (result && startTimeOffset) {
            result = new Date(result.time + Math.round(startTimeOffset * 60000))
        }
    }
    else if (starting) {
        result = timeToday(starting, location.timeZone)
    }
    log.trace "timeWindowStart = ${result}"
    result
}

private timeWindowStop() {
    def result = null
    if (endTimeType == "sunrise") {
        result = location.currentState("sunriseTime")?.dateValue
        if (result && endTimeOffset) {
            result = new Date(result.time + Math.round(endTimeOffset * 60000))
        }
    }
    else if (endTimeType == "sunset") {
        result = location.currentState("sunsetTime")?.dateValue
        if (result && endTimeOffset) {
            result = new Date(result.time + Math.round(endTimeOffset * 60000))
        }
    }
    else if (ending) {
        result = timeToday(ending, location.timeZone)
    }
    log.trace "timeWindowStop = ${result}"
    result
}

private hhmm(time, fmt = "h:mm a")
{
    def t = timeToday(time, location.timeZone)
    def f = new java.text.SimpleDateFormat(fmt)
    f.setTimeZone(location.timeZone ?: timeZone(time))
    f.format(t)
}

private timeIntervalLabel() {
    def start = ""
    switch (startTimeType) {
        case "time":
            if (ending) {
                start += hhmm(starting)
            }
            break
        case "sunrise":
        case "sunset":
            start += startTimeType[0].toUpperCase() + startTimeType[1..-1]
            if (startTimeOffset) {
                start += startTimeOffset > 0 ? "+${startTimeOffset} min" : "${startTimeOffset} min"
            }
            break
    }
    
    def finish = ""
    switch (endTimeType) {
        case "time":
            if (ending) {
                finish += hhmm(ending)
            }
            break
        case "sunrise":
        case "sunset":
            finish += endTimeType[0].toUpperCase() + endTimeType[1..-1]
            if (endTimeOffset) {
                finish += endTimeOffset > 0 ? "+${endTimeOffset} min" : "${endTimeOffset} min"
            }
            break
    }
    start && finish ? "${start} to ${finish}" : ""
}


def getLabel(value)
{
    //Name Page 
    def str_NameChange=[
            "kr": "이름을 변경 설정.",
            "us": "Do you want to allow custom automation name?"
    ]
    
    def str_NameAutomation=[
            "kr": "이름을 변경 설정.",
            "us": "Automation name"
    ]

    def str_NameUser=[
            "kr": "사용자 이름.",
            "us": "Enter custom name"
    ]

    def str_NamePageTitle=[
            "kr": "자동화 룰 이름 설정",
            "us": "Configure rule based on sleep sensor state"
    ]

    ///////////////////////////////////////////////////////////////////////////
    
    //Main Page 
    def str_MainPageTitle=[
            "kr": "슬립센서를 이용하여 자동화 룰을 만드세요.",
            "us": "Configure rule based on sleep sensor state"
    ]

    def str_SelectSense=[
            "kr": "슬립센서를 선택하세요.",
            "us": "Which sleep sensor?"
    ]
    
    def str_SelectSleepTrigger=[
            "kr": "트리거를 선택하세요.",
            "us": "Select trigger."
    ]

    def str_SelectLocation=[
            "kr": "제어 방식을 선택하세요.",
            "us": "What would you like to control?"
    ]

    def str_HubofHomeWatch=[
            "kr": "홈지키미의 상태",
            "us": "The state of Smart Home Monitoring"
    ]
    
    def str_HubofDevice=[
            "kr": "의 디바이스",
            "us": "The mode at  "
    ]
    
    def str_HubofMode=[
            "kr": "의 모드",
            "us": "A mode at "
    ]

    def str_HubofRoutines=[
            "kr": "의 루틴",
            "us": "A routine at  "
    ]

    
    ///////////////////////////////////////////////////////////////////////////

    
    def str_RoutineTitle=[
            "kr": "자동화 설정",
            "us": "Routine Control"
    ]

    def str_RoutineSelect=[
            "kr": "자동화 룰을 선택하세요",
            "us": "Run the routine..."
    ]
    
    ///////////////////////////////////////////////////////////////////////////

    def strr_ModeTitle=[
            "kr": "모드",
            "us": "mode"
    ]

    def str_ModeSelect=[
            "kr": "모드 선택",
            "us": "Change Mode to..."
    ]

    ///////////////////////////////////////////////////////////////////////////

    def str_HomeWatchTitle=[
            "kr": "홈지키미 모드 설정",
            "us": "Smart Home Monitor Control"
    ]

    def str_HomeWatchSectionSelect=[
            "kr": "홈지키미 모드 상태 선택",
            "us": "Set Smart Home Monitor status to..."
    ]

    ///////////////////////////////////////////////////////////////////////////

    def str_DeviceTitle=[
            "kr": "기기 제어",
            "us": "Device Control"
    ]

    def str_DeviceSelect=[
            "kr": "기기 선택",
            "us": "Select device to control"
    ]
    
    def str_DeviceSwitch=[
            "kr": "스위치 및 디머",
            "us": "Switches & Dimmers"
    ]
    def str_DeviceCameras=[
            "kr": "카메라",
            "us": "Cameras"
    ]
    def str_DeviceLocks=[
            "kr": "자동 잠금 기기",
            "us": "Locks"
    ]
    def str_DeviceThermostats=[
            "kr": "에어컨 및 온도 설정기",
            "us": "Thermostats"
    ]
    def str_DeviceAlarms=[
            "kr": "알림 센서",
            "us": "Alarms & Sirens"
    ]
    def str_DeviceSpeaker=[
            "kr": "스피커",
            "us": "Speakers"
    ]

    def str_DeviceLightsTitle=[
            "kr": "동작 선택",
            "us": "Lights will..."
    ]

    def str_DeviceTurnOn=[
            "kr": "켜기",
            "us": "Turn On"
    ]

    def str_DeviceTurnOff=[
            "kr": "끄기",
            "us": "Turn Off"
    ]

    def str_DeviceLevel=[
            "kr": "켜고 밝기 조정하기",
            "us": "Turn On & Set Level"
    ]

    def str_DeviceColor=[
            "kr": "켜고 색상 조정하기",
            "us": "Turn On & Set Color"
    ]

    def str_DeviceSpeakerTitle=[
            "kr": "동작 선택",
            "us": "Speaker will..."
    ]

    def str_AlarmWill=[
            "kr": "알람이 발생했을때...",
            "us": "Alarm will..."
    ]

    def str_DeviceSpeakerPlae=[
            "kr": "동작",
            "us": "Play"
    ]

    def str_DeviceSpeakerPause=[
            "kr": "일시 정지",
            "us": "Pause"
    ]

    def str_DeviceSpeakerStop=[
            "kr": "중지",
            "us": "Stop"
    ]


    def str_DeviceThermostatTitle=[
            "kr": "동작 선택",
            "us": "Thermostat will..."
    ]

    def str_DeviceThermostatCool=[
            "kr": "냉방 모드 설정",
            "us": "Set cooling point & set mode to cool"
    ]

    def str_DeviceThermostatHeat=[
            "kr": "난방 모드 설정",
            "us": "Set heating point &  set mode to heat"
    ]

    def str_DeviceThermostatCoolSet=[
            "kr": "냉방 모드 온도 설정",
            "us": "Set cooling point to..."
    ]

    def str_DeviceThermostatHeatSet=[
            "kr": "난방 모드 온도 설정",
            "us": "Set heating point to..."
    ]

    def str_CameraTitle=[
            "kr": "동작 설정",
            "us": "Camera will..."
    ]

    def str_CameraTake=[
            "kr": "사진 가져 오기",
            "us": "Take a photo"
    ]


    def str_LocksTitle=[
            "kr": "동작 설정",
            "us": "Lock will..."
    ]


    def str_LocksLock=[
            "kr": "잠그기",
            "us": "Lock the door"
    ]

    def str_LocksUnLock=[
            "kr": "잠금 풀기",
            "us": "Unlock the door"
    ]
    
    
    ///////////////////////////////////////////////////////////////////////////
    
    def str_MoreOption=[
            "kr": "추가 설정",
            "us": "More options"
    ]

    def str_TimeSet=[
            "kr": "동작 시간 설정",
            "us": "Only during a certain time"
    ]

    def str_TimeStartingCondition=[
            "kr": "시작 조건",
            "us": "Starting at"
    ]

    def str_TimeEngingCondition=[
            "kr": "종료 조건",
            "us": "Ending at"
    ]

    def str_TimeSunrise=[
            "kr": "해 질때",
            "us": "At Sunrise"
    ]

    def str_TimeSunset=[
            "kr": "해 뜰때",
            "us": "At Sunset"
    ]

    def str_TimeSpecificTime=[
            "kr": "사용자 설정",
            "us": "At a Specific Time"
    ]

    def str_TimeOffset=[
            "kr": "오차 범위를 설정하세요.",
            "us": "Offset in minutes (+/-)"
    ]

    def str_TimeStart=[
            "kr": "시작 시간",
            "us": "Start time"
    ]

    def str_TimeEnd=[
            "kr": "종료 시간",
            "us": "End time"
    ]

    def str_WeekSet=[
            "kr": "동작 요일을 선택하세요.",
            "us": "Only on certain days of the week"
    ]

    ///////////////////////////////////////////////////////////////////////////

    def str_Moreoption=[
            "kr": "추가 설정",
            "us": "More options"
    ]

    def str_Monday=[
            "kr": "월요일",
            "us": "Monday"
    ]

    def str_Tuesday=[
            "kr": "화요일",
            "us": "Tuesday"
    ]

    def str_Wednesday=[
            "kr": "수요일",
            "us": "Wednesday"
    ]

    def str_Thursday=[
            "kr": "목요일",
            "us": "Thursday"
    ]
    def str_Friday=[
            "kr": "금요일",
            "us": "Friday"
    ]
    def str_Saturday=[
            "kr": "토요일",
            "us": "Saturday"
    ]
    def str_Sunday=[
            "kr": "일요일",
            "us": "Sunday"
    ]

    ///////////////////////////////////////////////////////////////////////////

    def str_sleep=[
            "kr": "잠 들었을때",
            "us": "When I fall asleep"
    ]

    def str_wake=[
            "kr": "일어 났을때",
            "us": "When I wake up"
    ]

    def str_inbed=[
            "kr": "잠자리에 들었을때",
            "us": "When I get in bed"
    ]

    def str_outofbed=[
            "kr": "잠자리에서 나왔을때",
            "us": "When I get out of bed"
    ]

    def str_Taptoset=[
            "kr": "설정",
            "us": "Tap to set"
    ]



    def lang = "us"
    if(lang == "ko") //clientLocale?.language
    {
        switch(value)
        {
            case "str_MainPageTitle":
                return str_MainPageTitle["kr"]
            case "str_SelectSense":
                return str_SelectSense["kr"]
            case "str_SelectSleepTrigger":
                return str_SelectSleepTrigger["kr"]
            case "str_SelectLocation":
                return str_SelectLocation["kr"]
            case "str_HubofHomeWatch":
                return str_HubofHomeWatch["kr"]
            case "str_HubofMode":
                return str_HubofMode["kr"]
            case "str_HubofRoutines":
                return str_HubofRoutines["kr"]
            case "str_HubofDevice":
                return str_HubofDevice["kr"]
                
            case "str_RoutineTitle":
                return str_RoutineTitle["kr"]
            case "str_RoutineSelect":
                return str_RoutineSelect["kr"]
            case "str_TimeSet":
                return str_TimeSet["kr"]
            case "str_TimeStartingCondition":
                return str_TimeStartingCondition["kr"]
            case "str_TimeEngingCondition":
                return str_TimeEngingCondition["kr"]
            case "str_TimeSunrise":
                return str_TimeSunrise["kr"]
            case "str_TimeSunset":
                return str_TimeSunset["kr"]
            case "str_TimeSpecificTime":
                return str_TimeSpecificTime["kr"]
            case "str_TimeOffset":
                return str_TimeOffset["kr"]
            case "str_TimeStart":
                return str_TimeStart["kr"]
            case "str_TimeEnd":
                return str_TimeEnd["kr"]
            case "str_WeekSet":
                return str_WeekSet["kr"]
            case "str_NameChange":
                return str_NameChange["kr"]
            case "str_NameAutomation":
                return str_NameAutomation["kr"]
            case "str_NameUser":
                return str_NameUser["kr"]
            case "str_NamePageTitle":
                return str_NamePageTitle["kr"]
            case "str_DeviceTitle":
                return str_DeviceTitle["kr"]
            case "str_DeviceSelect":
                return str_DeviceSelect["kr"]
            case "str_DeviceLightsTitle":
                return str_DeviceLightsTitle["kr"]
            case "str_DeviceTurnOn":
                return str_DeviceTurnOn["kr"]
            case "str_DeviceTurnOff":
                return str_DeviceTurnOff["kr"]
            case "str_DeviceLevel":
                return str_DeviceLevel["kr"]
            case "str_DeviceColor":
                return str_DeviceColor["kr"]
            case "str_DeviceSpeakerTitle":
                return str_DeviceSpeakerTitle["kr"]
            case "str_AlarmWill":
                return str_AlarmWill["kr"]
            case "str_DeviceSpeakerPlae":
                return str_DeviceSpeakerPlae["kr"]
            case "str_DeviceSpeakerPause":
                return str_DeviceSpeakerPause["kr"]
            case "str_DeviceSpeakerStop":
                return str_DeviceSpeakerStop["kr"]
            case "str_DeviceSwitch":
                return str_DeviceSwitch["kr"]
            case "str_DeviceCameras":
                return str_DeviceCameras["kr"]
            case "str_DeviceLocks":
                return str_DeviceLocks["kr"]
            case "str_DeviceThermostats":
                return str_DeviceThermostats["kr"]
            case "str_DeviceAlarms":
                return str_DeviceAlarms["kr"]
            case "str_DeviceSpeaker":
                return str_DeviceSpeaker["kr"]
            case "str_DeviceThermostatTitle":
                return str_DeviceThermostatTitle["kr"]
            case "str_DeviceThermostatCool":
                return str_DeviceThermostatCool["kr"]
            case "str_DeviceThermostatHeat":
                return str_DeviceThermostatHeat["kr"]
            case "str_DeviceThermostatCoolSet":
                return str_DeviceThermostatCoolSet["kr"]
            case "str_DeviceThermostatHeatSet":
                return str_DeviceThermostatHeatSet["kr"]
            case "strr_ModeTitle":
                return strr_ModeTitle["kr"]
            case "str_ModeSelect":
                return str_ModeSelect["kr"]
            case "str_HomeWatchTitle":
                return str_HomeWatchTitle["kr"]
            case "str_HomeWatchSectionSelect":
                return str_HomeWatchSectionSelect["kr"]
            case "str_CameraTitle":
                return str_CameraTitle["kr"]
            case "str_CameraTake":
                return str_CameraTake["kr"]

            case "str_Moreoption":
                return str_Moreoption["kr"]
            case "str_Monday":
                return str_Monday["kr"]
            case "str_Tuesday":
                return str_Tuesday["kr"]
            case "str_Wednesday":
                return str_Wednesday["kr"]
            case "str_Thursday":
                return str_Thursday["kr"]
            case "str_Friday":
                return str_Friday["kr"]
            case "str_Saturday":
                return str_Saturday["kr"]
            case "str_Sunday":
                return str_Sunday["kr"]

            case "str_sleep":
                return str_sleep["kr"]
            case "str_wake":
                return str_wake["kr"]
            case "str_inbed":
                return str_inbed["kr"]
            case "str_outofbed":
                return str_outofbed["kr"]
            case "str_Taptoset":
                return str_Taptoset["kr"]

        }
    }
    else
    {
        switch(value)
        {
            case "str_MainPageTitle":
                return str_MainPageTitle["us"]
            case "str_SelectSense":
                return str_SelectSense["us"]
            case "str_SelectSleepTrigger":
                return str_SelectSleepTrigger["us"]
            case "str_SelectLocation":
                return str_SelectLocation["us"]
            case "str_HubofHomeWatch":
                return str_HubofHomeWatch["us"]
            case "str_HubofMode":
                return str_HubofMode["us"]
            case "str_HubofRoutines":
                return str_HubofRoutines["us"]
            case "str_HubofDevice":
                return str_HubofDevice["us"]
                
            case "str_RoutineTitle":
                return str_RoutineTitle["us"]
            case "str_RoutineSelect":
                return str_RoutineSelect["us"]
            case "str_TimeSet":
                return str_TimeSet["us"]
            case "str_TimeStartingCondition":
                return str_TimeStartingCondition["us"]
            case "str_TimeEngingCondition":
                return str_TimeEngingCondition["us"]
            case "str_TimeSunrise":
                return str_TimeSunrise["us"]
            case "str_TimeSunset":
                return str_TimeSunset["us"]
            case "str_TimeSpecificTime":
                return str_TimeSpecificTime["us"]
            case "str_TimeOffset":
                return str_TimeOffset["us"]
            case "str_TimeStart":
                return str_TimeStart["us"]
            case "str_TimeEnd":
                return str_TimeEnd["us"]
            case "str_WeekSet":
                return str_WeekSet["us"]
            case "str_NameChange":
                return str_NameChange["us"]
            case "str_NameAutomation":
                return str_NameAutomation["us"]
            case "str_NameUser":
                return str_NameUser["us"]
            case "str_NamePageTitle":
                return str_NamePageTitle["us"]
            case "str_DeviceTitle":
                return str_DeviceTitle["us"]
            case "str_DeviceSelect":
                return str_DeviceSelect["us"]
            case "str_DeviceLightsTitle":
                return str_DeviceLightsTitle["us"]
            case "str_DeviceTurnOn":
                return str_DeviceTurnOn["us"]
            case "str_DeviceTurnOff":
                return str_DeviceTurnOff["us"]
            case "str_DeviceLevel":
                return str_DeviceLevel["us"]
            case "str_DeviceColor":
                return str_DeviceColor["us"]
            case "str_DeviceSpeakerTitle":
                return str_DeviceSpeakerTitle["us"]
            case "str_AlarmWill":
                return str_AlarmWill["us"]
            case "str_DeviceSpeakerPlae":
                return str_DeviceSpeakerPlae["us"]
            case "str_DeviceSpeakerPause":
                return str_DeviceSpeakerPause["us"]
            case "str_DeviceSpeakerStop":
                return str_DeviceSpeakerStop["us"]
            case "str_DeviceSwitch":
                return str_DeviceSwitch["us"]
            case "str_DeviceCameras":
                return str_DeviceCameras["us"]
            case "str_DeviceLocks":
                return str_DeviceLocks["us"]
            case "str_DeviceThermostats":
                return str_DeviceThermostats["us"]
            case "str_DeviceAlarms":
                return str_DeviceAlarms["us"]
            case "str_DeviceSpeaker":
                return str_DeviceSpeaker["us"]
            case "str_DeviceThermostatTitle":
                return str_DeviceThermostatTitle["us"]
            case "str_DeviceThermostatCool":
                return str_DeviceThermostatCool["us"]
            case "str_DeviceThermostatHeat":
                return str_DeviceThermostatHeat["us"]
            case "str_DeviceThermostatCoolSet":
                return str_DeviceThermostatCoolSet["us"]
            case "str_DeviceThermostatHeatSet":
                return str_DeviceThermostatHeatSet["us"]
            case "strr_ModeTitle":
                return strr_ModeTitle["us"]
            case "str_ModeSelect":
                return str_ModeSelect["us"]
            case "str_HomeWatchTitle":
                return str_HomeWatchTitle["us"]
            case "str_HomeWatchSectionSelect":
                return str_HomeWatchSectionSelect["us"]
            case "str_CameraTitle":
                return str_CameraTitle["us"]
            case "str_CameraTake":
                return str_CameraTake["us"]

            case "str_Moreoption":
                return str_Moreoption["us"]
            case "str_Monday":
                return str_Monday["us"]
            case "str_Tuesday":
                return str_Tuesday["us"]
            case "str_Wednesday":
                return str_Wednesday["us"]
            case "str_Thursday":
                return str_Thursday["us"]
            case "str_Friday":
                return str_Friday["us"]
            case "str_Saturday":
                return str_Saturday["us"]
            case "str_Sunday":
                return str_Sunday["us"]

            case "str_sleep":
                return str_sleep["us"]
            case "str_wake":
                return str_wake["us"]
            case "str_inbed":
                return str_inbed["us"]
            case "str_outofbed":
                return str_outofbed["us"]
            case "str_Taptoset":
                return str_Taptoset["us"]

        }
    }
    return "Unknown"
}

