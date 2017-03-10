/**
 *  Light Color and Brightness
 *
 *  Copyright 2016 Sam Storino
 *
 */
definition(
    name: "Lighting Control",
    namespace: "JeorgeLeatherly",
    author: "Sam Storino",
    description: "Control the color and brightness of one or more lights, either by selecting from the predefined color choices, or providing the hue and saturation values yourself.",
    category: "Convenience",
    iconUrl: "http://cdn.device-icons.smartthings.com/Lighting/light20-icn.png",
    iconX2Url: "http://cdn.device-icons.smartthings.com/Lighting/light20-icn@2x.png",
    iconX3Url: "http://cdn.device-icons.smartthings.com/Lighting/light20-icn@2x.png")


preferences {
    page(name: "mainPage", title: "Light Color and Brightness", install: true, uninstall: true, submitOnChange: true) {
        section {
            app(name: "lightAndColorControl", appName: "Light Color and Brightness", namespace: "JeorgeLeatherly/ColorControl", title: "Create new lighting color control", multiple: true)
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
    initialize()
}

def initialize() {
    // nothing needed here, since the child apps will handle preferences/subscriptions
    // this just logs some messages for demo/information purposes
    log.debug "there are ${childApps.size()} child smartapps"
    childApps.each {child ->
        log.debug "child app: ${child.label}"
    }
}