definition(
    name: "Mean Temperature Sensor Manager",
    namespace: "erocm123/MeanTemp",
    author: "Eric Maycock",
    description: "Combine multiple temperature sensors into a single virtual sensor",
    category: "My Apps",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png")


preferences {
    page(name: "mainPage", title: "Temperature Sensors", install: true, uninstall: true,submitOnChange: true) {
        section {
            app(name: "meanTempSensor", appName: "Mean Temperature Sensor", namespace: "erocm123/MeanTemp", title: "Create New Sensor", multiple: true)
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
    log.debug "there are ${childApps.size()} child smartapps"
    childApps.each {child ->
        log.debug "child app: ${child.label}"
    }
}