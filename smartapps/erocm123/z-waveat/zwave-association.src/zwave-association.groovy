definition(
    name: "Z-Wave Association",
    namespace: "erocm123/Z-WaveAT",
    author: "Eric Maycock",
    description: "An app to create direct associations between two Z-Wave devices.",
    category: "My Apps",

    parent: "erocm123/Z-WaveAT:Z-Wave Association Tool",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png")


preferences {
    page name: "mainPage", title: "Associate Z-Wave Devices", install: false, uninstall: true, nextPage: "namePage"
    page name: "namePage", title: "Associate Z-Wave Devices", install: true, uninstall: true
}

def installed() {
    log.debug "Installed with settings: ${settings}"
    initialize()
}

def uninstalled() {
    settings."s${settings.sCapability}".setAssociationGroup(groupNumber, settings."d${settings.dCapability}"? settings."d${settings.dCapability}".deviceNetworkId : [], 0, settings.endpoint)
    settings."s${settings.sCapability}".configure()
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

    def addNodes = ((settings."d${settings.dCapability}"?.deviceNetworkId)?:[]) - (state.previousNodes? state.previousNodes : [])
    def delNodes = (state.previousNodes? state.previousNodes : []) - ((settings."d${settings.dCapability}"?.deviceNetworkId)?:[])
    if (addNodes)
        settings."s${settings.sCapability}".setAssociationGroup(groupNumber, addNodes, 1, settings.endpoint)
    if (delNodes)
        settings."s${settings.sCapability}".setAssociationGroup(groupNumber, delNodes, 0, settings.endpoint)
    settings."s${settings.sCapability}".configure()
        
    state.previousNodes = (settings."d${settings.dCapability}"?.deviceNetworkId)?:[]
}


def mainPage() {
    dynamicPage(name: "mainPage") {
        associationInputs()
    }
}

def namePage() {
    if (!overrideLabel) {
        def l = defaultLabel()
        log.debug "will set default label of $l"
        app.updateLabel(l)
    }
    //log.debug settings."s${settings.sCapability}".getDeviceDataByName("firmware")
    //log.debug getDeviceById(settings."s${settings.sCapability}".deviceNetworkId)
    //log.debug settings."s${settings.sCapability}".getDataValue("firmware")

    dynamicPage(name: "namePage") {
        if (overrideLabel) {
            section("Association Name") {
                label title: "Enter custom name", defaultValue: app.label, required: false
            }
        } else {
            section("Association Name") {
                paragraph app.label
            }
        }
        section {
            input "overrideLabel", "bool", title: "Edit association name", defaultValue: "false", required: "false", submitOnChange: true
        }
    }
}

def defaultLabel() {
    def associationLabel
    associationLabel = settings."s${settings.sCapability}".displayName + " Association Group ${groupNumber}"
    return associationLabel
}

def associationInputs() {
    section("Source Device") {
        input "sCapability", "enum", title: "Which capability?", multiple: false, required: true, submitOnChange: true, options: capabilities()
        if (settings.sCapability) {
            input "s${settings.sCapability}", "capability.${settings.sCapability.toLowerCase()}", title: "${settings.sCapability}", multiple: false, required: false
        }
    }
    section("Destination Device") {
        input "dCapability", "enum", title: "Which capability?", multiple: false, required: true, submitOnChange: true, options: capabilities()
        if (settings.dCapability) {
            input "d${settings.dCapability}", "capability.${toCamelCase(settings.dCapability)}", title: "${settings.dCapability}", multiple: true, required: false
        }
    }
    section("Options") {
        input "groupNumber", "enum", title: "Which group number?", multiple: false, required: true, options: returnGroups()
        //input "multiChannel", "bool", title: "MultiChannel Association?", required: false, submitOnChange: true
        //if (multiChannel) {
        //    input "endpoint", "number", title: "Endpoint ID", required: multiChannel
        //}
    }
}

def capabilities() {
    return ["Actuator", "Sensor", "Switch", "Motion Sensor", "Relative Humidity Measurement", "Water Sensor", 
    "Thermostat", "Temperature Measurement", "Contact Sensor", "Lock", "Alarm", "Presence Sensor", "Smoke Detector", "Valve", "Button" ]
}

def returnGroups() {
    def groups = settings."s${settings.sCapability}"?.currentValue("groups")? settings."s${settings.sCapability}"?.currentValue("groups") : 5
    def groupings = []
    for (int i = 1; i <= groups.toInteger(); i++){
        groupings += i
    }
   return groupings
}

def toCamelCase( def text ) {
    def camelCase = ""
    def counter = 0
    text.split().each() {
        if (counter > 0) {
            camelCase += it.capitalize()
        } else {
            camelCase += it.toLowerCase()
        }
        counter += 1
    }
    return camelCase
}