definition(
    name: "Big Switches",
    namespace: "smartthings/bigswitches",
    author: "SmartThings",
    description: "Turns on and off a collection of lights based on the state of a specific switch.",
    category: "Convenience",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Meta/light_outlet.png",
	iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Meta/light_outlet@2x.png",
    singleInstance: true
)

preferences {
    // The parent app preferences are pretty simple: just use the app input for the child app.
    page(name: "mainPage", title: "Big Switches", install: true, uninstall: true,submitOnChange: true) {
            section {
                    app(name: "aBigSwitch", appName: "A Big Switch", namespace: "smartthings/abigswitch", title: "Create a New Big Switch", multiple: true)
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