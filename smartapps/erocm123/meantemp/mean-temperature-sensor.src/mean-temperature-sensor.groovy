definition(
    name: "Mean Temperature Sensor",
    namespace: "erocm123/MeanTemp",
    author: "Eric Maycock",
    description: "A simple app to average the temperature across multiple temperature sensors.",
    category: "My Apps",

    parent: "erocm123/MeanTemp:Mean Temperature Sensor Manager",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png")


preferences {
    page name: "mainPage", title: "Combine Temperature Sensors", install: false, uninstall: true, nextPage: "namePage"
    page name: "namePage", title: "Combine Temperature Sensors", install: true, uninstall: true
}

def installed() {
    log.debug "Installed with settings: ${settings}"
    initialize()
}

def updated() {
    log.debug "Updated with settings: ${settings}"
    unschedule()
    initialize()
}

def initialize() {
    if (!overrideLabel) {
        app.updateLabel(defaultLabel())
    }
    subscribe(temperatures, "temperature", temperatureHandler)
    if(!getChildDevices()) {
        def newDevice = addChildDevice("erocm123", "Mean Temperature Sensor", "${app.id}", null, [
                "label": app.label,
            ])
    } 
    if(getChildDevices()) {
        getChildDevice("${app.id}").label = app.label
    }
    temperatureHandler()
}

def mainPage() {
    dynamicPage(name: "mainPage") {
        section {
            sensorInputs()
        }
    }
}

def namePage() {
    if (!overrideLabel) {
        def l = defaultLabel()
        log.debug "will set default label of $l"
        app.updateLabel(l)
    }
    if (getChildDevice("${app.id}")) app.updateLabel(getChildDevice("${app.id}").label)
    dynamicPage(name: "namePage") {
        if (overrideLabel) {
            section("Sensor Name") {
                label title: "Enter custom name", defaultValue: app.label, required: false
            }
        } else {
            section("Sensor Name") {
                paragraph app.label
            }
        }
        section {
            input "overrideLabel", "bool", title: "Edit sensor name", defaultValue: "false", required: "false", submitOnChange: true
        }
    }
}

def temperatureHandler(evt = null) {
    
    def sum     = 0
    def count   = 0
    Float average = 0

    for (sensor in settings.temperatures) {
        count += 1
        sum   += sensor.currentTemperature
    }

    average = (sum/count).toFloat().round(1)
    
    log.debug "average: $average"
    
    sendEvent(getChildDevice("${app.id}"), [name:"temperature", value:average])
}

def defaultLabel() {
    def sensorsLabel
    switch (settings.temperatures.size()) {
        case 1:
            sensorsLabel = temperatures[0].displayName + " Average Sensor"
        break
        default:
            sensorsLabel = temperatures[0].displayName + ", etc... Average Sensor"
        break
    }
    return sensorsLabel
}

def sensorInputs() {
    input "temperatures", "capability.temperatureMeasurement", multiple: true, required: false
}