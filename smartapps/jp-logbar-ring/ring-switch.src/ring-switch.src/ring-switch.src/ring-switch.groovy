/**
 *  Ring Switch
 *
 *  Copyright 2015 Logbar Inc.
 */
 
definition(
    name: "Ring Switch",
    namespace: "jp.logbar.ring",
    author: "Logbar Inc.",
    description: "This action will be triggered by Ring gesture action and will access other devices.",
    category: "My Apps",
    iconUrl: "http://app.logbar.jp/public/st/st_icon.png",
    iconX2Url: "http://app.logbar.jp/public/st/st_icon@2x.png",
    iconX3Url: "http://app.logbar.jp/public/st/st_icon@2x.png",
    oauth: [displayName: "Ring by Logbar Inc.", displayLink: "http://logbar.jp/ring"])


preferences {
    section("Allow Ring to Control These Things...") {
      input "switches", "capability.switch", title: "Which Switches?", multiple: true, required: true
	}
}

mappings {
	path("/switches") {
		action: [
			GET: "listSwitches",
			PUT: "updateSwitches"
		]
	}
	path("/switches/:id") {
		action: [
			GET: "showSwitch",
			PUT: "updateSwitch"
		]
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
	// TODO: subscribe to attributes, devices, locations, etc.
}

def listSwitches() {
	switches.collect { device(it,"switch") }
}
void updateSwitches() {
	updateAll(switches)
}
def showSwitch() {
	show(switches, "switch")
}
void updateSwitch() {
	update(switches)
}

private void updateAll(devices) {
	def command = request.JSON?.command
	if (command) {
		devices."$command"()
	}
}

private void update(devices) {
	log.debug "update, request: ${request.JSON}, params: ${params}, devices: $devices.id"
	def command = request.JSON?.command
    log.debug("command=${command}");
	if (command) {
		def device = devices.find { it.id == params.id }
		if (!device) {
			httpError(404, "Device not found")
		} else {
			device."$command"()
		}
	} else {
        log.debug("command not found");
    }
}

private show(devices, name) {
	def device = devices.find { it.id == params.id }
	if (!device) {
		httpError(404, "Device not found")
	}
	else {
		def s = device.currentState(name)
		[id: device.id, label: device.displayName, name: device.displayName, state: s]
	}
}

private device(it, name) {
	if (it) {
		def s = it.currentState(name)
		[id: it.id, label: it.displayName, name: it.displayName, state: s]
    }
}
