definition(
name: "Stateless Scene via Echo",
namespace: "sticks18/parent",
author: "sticks18",
description: "Control panel for triggering Hue scenes with Echo",
category: "My Apps",
iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png")

preferences {
// The parent app preferences are pretty simple: just use the app input for the child app.
page(name: "mainPage", title: "Hue Scenes", install: true, uninstall: true,submitOnChange: true) {
section {
app(name: "momScene", appName: "Momentary To Trigger On/Off Scenes", namespace: "sticks18/echo", title: "Create New On/Off Scene", multiple: true)
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
  childApps.each { child -> log.debug "child app: ${child.label}" }
}