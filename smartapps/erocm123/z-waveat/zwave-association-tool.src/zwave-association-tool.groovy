definition(
    name: "Z-Wave Association Tool",
    namespace: "erocm123/Z-WaveAT",
    author: "Eric Maycock",
    description: "Create direct associations from one Z-Wave device to another",
    category: "My Apps",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png")

preferences {
    page(name: "mainPage", title: "Associations", install: true, uninstall: true,submitOnChange: true) {
        section {
            app(name: "association", appName: "Z-Wave Association", namespace: "erocm123/Z-WaveAT", title: "Create New Association", multiple: true)
            }
        section {
            paragraph "This tool allows you to create direct associations between multiple Z-Wave devices. In order for it to create the association, the source device handler needs to have support for this tool added to it. Some examples of handlers that have support are the Inovelli Plug handlers, Inovelli Dimmer & Switch handlers, and the Inovelli Door/Window Sensor handler. The destination does not need anything added to it's handler, but does need to be a Z-Wave device.\n\nFor more information on how this SmartApp works click the link below."
            href(name: "hrefNotRequired",
             title: "Z-Wave Association Tool on Github",
             required: false,
             style: "external",
             url: "https://github.com/erocm123/SmartThingsPublic/tree/master/smartapps/erocm123/z-waveat",
             description: "Tap to view information about this SmartApp")
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