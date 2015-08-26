/**
 *  Ring Lock
 *
 *  Copyright 2015 Logbar Inc.
 */
 
definition(
    name: "Ring Lock",
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
      input "locks", "capability.lock", title: "Which Locks?", multiple: true, required: true
    }
}

mappings {
    path("/locks") {
		action: [
			GET: "listLocks",
			PUT: "updateLocks"
		]
	}
	path("/locks/:id") {
		action: [
			GET: "showLock",
			PUT: "updateLock"
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

def listLocks() {
	locks.collect { device(it, "lock") }
}
void updateLocks() {
	updateAll(locks)
}
def showLock() {
	show(locks, "lock")
}
void updateLock() {
	update(locks)
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