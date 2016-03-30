/**
 *  Pushbullet Notifier 1.2.0
 *  2/12/2015
 *
 *  Copyright 2015 Eric Roberts
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
    name: "Pushbullet Notifier 1.2.0",
    namespace: "baldeagle072",
    author: "Eric Roberts",
    description: "For use with @625alex's pushbullet device: https://github.com/625alex/SmartThings/blob/master/devices/Pushbullet.groovy It will push a message to the pushbullet device you specify.",
    category: "My Apps",
    iconUrl: "https://baldeagle072.github.io/pushbullet_notifier/images/PushbulletLogo@1x.png",
    iconX2Url: "https://baldeagle072.github.io/pushbullet_notifier/images/PushbulletLogo@2x.png",
    iconX3Url: "https://baldeagle072.github.io/pushbullet_notifier/images/PushbulletLogo@3x.png")


preferences {
    page name: "init"
    page name: "capabilityChooser"
    page name: "deviceChooser"
    page name: "pushbulletSetup"
    page name: "appInstalled"
}

def init() {
    TRACE("init()")
    if (state.installed) {
        return appInstalled()
    } else {
        return capabilityChooser()
    }
}

def capabilityChooser() {
    TRACE("capabilityChooser()")
    def pageProperties = [
        name: "capabilityChooser",
        title: "Choose a capability",
        nextPage: "deviceChooser",
        uninstall: true
    ]
    
    return dynamicPage(pageProperties) {
        section("Choose a capability to monitor") {
            //TODO: input for list of device types
            input "theCapability", "enum", options: getCapabilities(), title: "Capability", required: true
        }
    }
}

def deviceChooser() {
    TRACE("deviceChooser()")
    def pageProperties = [
        name: "deviceChooser",
        title: "Choose a device",
        nextPage: "pushbulletSetup",
        install: state.installed,
        uninstall: state.installed
    ]
    
    def onlyAttributeRequired = false
    
    if (settings.oneAttribute) { onlyAttributeRequired = settings.oneAttribute }
    
    return dynamicPage(pageProperties) {
        section("Device(s) to monitor") {
            input "device", "capability.${theCapability}", title: "Device(s)", required: true, multiple: true
        }
        
        if (theCapability == "temperatureMeasurement") {
            section("Temperature Settings") {
                paragraph "Choose if it goes above or bellow the chosen temperature"
                input "aboveOrBelow", "enum", title: "Above or Below?", options: ["Above", "Below"], required: true
                input "temperatureThreshold", "decimal", title: "Temperature Threshold"
            }
        }
        
        section("Monitor one attribute?") {
            paragraph "Turn it on and you will only monitor one attribute. Be sure to specify which one. If it is off, you will get notifications for every state change"
            input "oneAttribute", "bool", title: "One attribute?", required: true, default: false, refreshAfterSelection: true
            input "onlyAttribute", "enum", title: "Just this attribute", required: onlyAttributeRequired, options: attributeValues(theCapability)
        }
    }
}

def pushbulletSetup() {
    TRACE("pushbulletSetup()")
    def pageProperties = [
        name: "pushbulletSetup",
        title: "Choose a device type",
        install: true,
        uninstall: state.installed
    ]
    
    return dynamicPage(pageProperties) {
        section("Pushbullet options") {
            input "pushbullets", "device.pushbullet", title: "Pushbullet Device(s)", multiple: true, required: true
        }
        
        section("Message options") {
            paragraph "The standard message is \"\${evt.displayName} is \${evt.value}\""
            input "userMsg", "text", title: "User defined message (optional)", required: false
            paragraph "You can have the message displayed in the title so it shows up on the lock screen. Default is \"Smartthings: \${app.label}\". This will show \"ST: \${message}\""
            input "showOnTitle", "bool", title: "Show message in title?"
            paragraph "You can specify the minimum number of minutes between notifications for each device. It defaults to every message"
            if (theCapability == "temperatureMeasurement") { paragraph "Tip: Indicate a number of minutes, otherwise you will get a notification every time the temperature changes." }
            input "frequency", "decimal", title: "Minutes (optional)", required: false
        }
        
        section([mobileOnly:true]) {
            label title: "Assign a name", required: false
            mode title: "Set for specific mode(s)", required: false
        }
    }
}

def appInstalled() {
    TRACE("appInstalled()")
    def pageProperties = [
        name: "appInstalled",
        title: "Setup Pages",
        install: true,
        uninstall: true
    ]
    
    return dynamicPage(pageProperties) {
        section("Setup Pages") {
            href(name: "hrefCapability", title: "Capability", description:"Change capability of device and device", page: "capabilityChooser")
            href(name: "hrefDevice", title: "Device", description:"Change the device", page: "deviceChooser")
            href(name: "hrefPushbullet", title: "Pushbullet", description:"Change pushbullet options", page: "pushbulletSetup")
        }
    }
}

def installed() {
    TRACE("installed()")
    log.debug "Installed with settings: ${settings}"

    initialize()
}

def updated() {
    TRACE("updated()")
    log.debug "Updated with settings: ${settings}"

    unsubscribe()
    initialize()
}

def initialize() {
    TRACE("initialize()")
    def subscribeCapability = ""
    if (oneAttribute) {
        subscribeCapability = capabilityAttribute(theCapability) + "." + onlyAttribute
    } else {
        subscribeCapability = capabilityAttribute(theCapability)
    }
    TRACE("subscribeCapability: $subscribeCapability, device: $settings.device")
    subscribe(settings.device, subscribeCapability, "handler")
    state.installed = true
}

def handler(evt) {
    TRACE("handler(evt: $evt)")
    if (theCapability == "temperatureMeasurement") {
        if (aboveOrBelow == "Above" && evt.numericValue > temperatureThreshold) {
            checkFrequency(evt)
        } else if  (aboveOrBelow == "Below" && evt.numericValue < temperatureThreshold){
            checkFrequency(evt)
        }
    } else {
        checkFrequency(evt)
    }
}

def checkFrequency(evt) {
    TRACE("checkFrequency(evt: $evt), frequency: $frequency")
    if (frequency) {
        def lastTime = state[evt.deviceId]
        TRACE("lastTime: $lastTime")
        if (lastTime) { TRACE("lastTime - now(): ${now() - lastTime}") }
        if (lastTime == null || now() - lastTime >= frequency * 60000) {
            sendMessage(getMessage(evt))
            state[evt.deviceId] = now()
        }
    } else {
        sendMessage(getMessage(evt))
    }
}

private getCapabilities() {
    TRACE("getCapabilities()")
    
    def allCapabilities = [
        "accelerationSensor": "Acceleration", 
        "contactSensor": "Contact", 
        "motionSensor": "Motion", 
        "presenceSensor": "Presence", 
        "smokeDetector": "Smoke Detector", 
        "switch": "Swtich", 
        "waterSensor": "Water Sensor", 
        "temperatureMeasurement": "Temperature", 
        "sleepSensor": "Sleep",
        "button": "Button"
    ]
    return allCapabilities
}

private capabilityAttribute(theCapability) {
    TRACE("capabilityAttribute(theCapability: $theCapability)")
    switch(theCapability) {
        case "accelerationSensor":
            return "acceleration"
        case "contactSensor":
            return "contact"
        case "motionSensor":
            return "motion"
        case "presenceSensor":
            return "presence"
        case "smokeDetector":
            return "smoke"
        case "switch":
            return "switch"
        case "waterSensor":
            return "water"
        case "temperatureMeasurement":
            return "temperature"
        case "sleepSensor":
            return "sleeping"
        case "button":
            return "button"
        default:
            return "UNDEFINED"
    }
}

private attributeValues(theCapability) {
    TRACE("attributeValues(theCapability: $theCapability)")
    switch(theCapability) {
        case "accelerationSensor":
            return ["inactive", "active"]
        case "contactSensor":
            return ["open","closed"]
        case "motionSensor":
            return ["active","inactive"]
        case "presenceSensor":
            return ["present", "not present"]
        case "smokeDetector":
            return ["clear", "detected", "tested"]
        case "switch":
            return ["on","off"]
        case "waterSensor":
            return ["wet","dry"]
        case "temperatureMeasurement":
            return ["temperature"]
        case "sleepSensor":
            return ["not sleeping", "sleeping"]
        case "button":
            return ["held", "pushed"]
        default:
            return ["UNDEFINED"]
    }
}

private getMessage(evt) {
    TRACE("getMessage(evt - displayName: $evt.displayName, value = $evt.value)")
    if (userMsg) {
        return userMsg
    } else if (theCapability == "smokeDetector") {
        return "${evt.displayName}: Smoke is ${evt.value}"
    } else if (theCapability == "button") {
        return "${evt.displayName} was ${evt.value}"
    } else {
        return "${evt.displayName} is ${evt.value}"
    }
}

private sendMessage(msg) { 
    TRACE("sendMessage(msg: $msg)")
    log.debug "send message: ${msg}"
    def title = "SmartThings: ${app.label}"
    if (showOnTitle) { title = "ST: ${msg}" }
    pushbullets.each() {it ->
        it.push(title, msg)
    }
}

private TRACE(msg) {
    log.debug(msg)
}