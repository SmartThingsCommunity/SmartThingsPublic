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
    name: "Smart Sleep Automation",
    namespace: "smartsolutionsv2/sleeping",
    parent: "smartsolutionsv2/sleeping:Smart Sleep",
    author: "SmartThings",
    description: "Sleeping automation rule.",
    category: "",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/ModeMagic/Cat-ModeMagic.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/ModeMagic/Cat-ModeMagic@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/ModeMagic/Cat-ModeMagic@3x.png"
)


preferences {
    page(name: "mainPage", title: "Configure rule based on sleep sensor state", install: false, uninstall: true, nextPage: "namePage");
    page(name: "namePage", title: "Configure rule based on sleep sensor state", install: true, uninstall: true);
    page(name: "timeIntervalInput", title: "Only during a certain time");
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
    input(name: "sleepSensor", type: "capability.sleepSensor", title: "Which sleep sensor?", multiple: false, submitOnChange: true);
}

//Sleep sensor action to trigger event
private actionMap() {
    state.sleepActionMap = ["sleeping": "When I fall asleep", "not sleeping": "When I wake"];
    if(sleepSensor.find{it.hasAttribute("bedstate")} != null) {
        state.sleepActionMap.put("in bed", "When I get in bed");
        state.sleepActionMap.put("out of bed", "When I get out of bed");
    }
    return state.sleepActionMap;
}

private actionOptions() {
    actionMap().collect{[(it.key): it.value]}
}

def triggerInput() {
    def requiredInput = androidClient() || iosClient("1.7.0.RC1");
    if(sleepSensor) {
        input(name: "trigger", type: "enum", title: "Select trigger", options: actionOptions(), required: true, submitOnChange: true);
    } 
}

// What type should be controlled
private controlType() {
    def requiredInput = androidClient() || iosClient("1.7.0.RC1");
    def mapControlTypes = [device: "A physical device at ${location.name}", homeWatch: "The state of Smart Home Monitoring", mode: "The mode at ${location.name}", routine: "A routine at ${location.name}"];

    if(settings.trigger) {
        section() {
            input(name: "controlType", type: "enum", title: "What would you like to control?", options: mapControlTypes, required: requiredInput, submitOnChange: true);
        }
    }
}

//Device to control with sleep sensor input
private lightMap() {
    def lightActionMap = [on: "Turn On", off: "Turn Off"];
    if (controlledDevice.find{it.hasCapability("Switch Level")} != null) {
        lightActionMap.level = "Turn On & Set Level"
    }
    if (controlledDevice.find{it.hasCapability("Color Control")} != null) {
        lightActionMap.color = "Turn On & Set Color"
    }
    return lightActionMap;
}

private lightOptions() {
    lightMap().collect{[(it.key): it.value]}
}

def controlledInput() {    
    def requiredInput = androidClient() || iosClient("1.7.0.RC1");
    def mapControlledDeivces = [switch: "Switches & Dimmers", imageCapture: "Cameras", lock: "Locks", thermostat: "Thermostats", alarm: "Alarms & Sirens", musicPlayer: "Speakers"];
    
    log.debug "control type ${settings.controlType}";

    if(settings.controlType && settings.controlType == "device") {
        section("Device Control") {
            input(name: "controlledDeviceType", type: "enum", title: "Select device to control", options: mapControlledDeivces, required: requiredInput, submitOnChange: true);
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
                            input(name: "actionsLights", type: "enum", title: "Lights will...", options: lightOptions(), multiple: false, required: requiredInput, submitOnChange: true);
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
                            input(name: "imageCaptureAction", type: "enum", title: "Camera will...", options: [take: "Take a photo"], multiple: false, required:true, submitOnChange: true);
                            break;
                        case "lock":
                    input(name: "lockAction", type: "enum", title: "Lock will...", options: [lock: "Lock the door", unlock: "Unlock the door"], multiple: false, required: true, submitOnChange: true);
                    break;
                        case "thermostat":                            
                            input(name: "thermostatMode", type: "enum", title: "Thermostat will...", options: [modeCool: "Set cooling point & set mode to cool", modeHeat: "Set heating point &  set mode to heat", off: "Turn Off"], required: requiredInput, multiple: false, submitOnChange: true);
                            
                            switch(thermostatMode) {
                                case "modeCool":
                                    input(name: "thermostatCoolTemp", type: "number", title: "Set cooling point to...", defaultValue: 76, required: true, submitOnChange: true);
                                    break;
                                case "modeHeat":
                                    input(name: "thermostatHeatTemp", type: "number", title: "Set heating point to...", defaultValue: 86, required: true, submitOnChange: true);
                                    break;
                            }
                            break;
                        case "alarm":
                            input(name: "alarmAction", type: "enum", title: "Alarm will...", options: [strobe: "Strobe the lights", siren: "Sound the siren", both: "Strobe the lights & sound the alarm", off: "turn Off"], multiple: false, required: true, submitOnChange: true);
                            break;
                        case "musicPlayer":
                            input(name: "musicPlayerAction", type: "enum", title: "Speaker will...", options: [play: "Play", pause: "Pause", stop: "Stop"], required: true, multiple: false, submitOnChange: true);
                            break;
                    }
                }
            }
        }
    }
    if(settings.controlType && settings.controlType == "homeWatch") {
        section("Home Watch Control") {
            input(name: "homeWatchAction", type: "enum", title: "Set HomeWatch status to...", required: true, multiple: false, options: [off: "Off", stay: "Stay", arm: "Arm"], submitOnChange: true);
        }
    }
    if(settings.controlType && settings.controlType == "mode") {
        section("Mode Control") {
            input(name: "modeAction", type: "mode", title: "Change Mode to...", required: true, multiple: false, submitOnChange: true);
        }
    }
    if(settings.controlType && settings.controlType == "routine") {
        def routines = location.helloHome?.getPhrases()*.label;
        if(routines) {
            section("Routine Control") {
                input(name: "routineAction", type: "enum", title: "Run the routine...", options: routines, required: true, multiple: false, submitOnChange: true);
            }
        }
    }
}

def otherInputs() {
    if (settings.trigger) {
        def timeLabel = timeIntervalLabel()
        section(title: "More options", hidden: hideOptionsSection(), hideable: true) {
            def timeBasedTrigger = trigger in ["At Sunrise", "At Sunset", "At a Specific Time"]
            log.trace "timeBasedTrigger: $timeBasedTrigger"
            if (!timeBasedTrigger) {
                href "timeIntervalInput", title: "Only during a certain time", description: timeLabel ?: "Tap to set", state: timeLabel ? "complete" : "incomplete"
            }

            input "days", "enum", title: "Only on certain days of the week", multiple: true, required: false,
                options: ["Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"]

            input "modes", "mode", title: "Only when mode is", multiple: true, required: false
        }
    }
}

def timeIntervalInput() {
    dynamicPage(name: "timeIntervalInput") {
        section {
            input "startTimeType", "enum", title: "Starting at", options: [["time": "A specific time"], ["sunrise": "Sunrise"], ["sunset": "Sunset"]], defaultValue: "time", submitOnChange: true
            if (startTimeType in ["sunrise","sunset"]) {
                input "startTimeOffset", "number", title: "Offset in minutes (+/-)", range: "*..*", required: false
            }
            else {
                input "starting", "time", title: "Start time", required: false
            }
        }
        section {
            input "endTimeType", "enum", title: "Ending at", options: [["time": "A specific time"], ["sunrise": "Sunrise"], ["sunset": "Sunset"]], defaultValue: "time", submitOnChange: true
            if (endTimeType in ["sunrise","sunset"]) {
                input "endTimeOffset", "number", title: "Offset in minutes (+/-)", range: "*..*", required: false
            }
            else {
                input "ending", "time", title: "End time", required: false
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
        device = "Home Watch";
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
                return "$defaultLabel} ${musicPlayerAction[0].toLowerCase() + musicPlayerAction.substring(1)} ${device}"
                break;
        }
    } else if(controlType == "homeWatch") {
        return "${defaultLabel} ${homeWatchAction} Home Watch";
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
        if(overrideLabel) {
            section(title: "Automation name") {
                label(title: "Enter custom name", defaultValue: generateDefaultLabel(), required: false);
            }
        } else {
            section(title: "Automation name") {
                paragraph app.label;
            }
        }
        section {
            input(name: "overrideLabel", type: "bool", title: "Do you want to allow custom automation name?", required: false, submitOnChange: true);
        }
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
                actionLabel = "Smart Home Monitoring is in stay";
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
        result = timeToday(starting)
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
        result = timeToday(ending)
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