/**
 *  SmartThings service for Prempoint
 *
 *  Author: Prempoint Inc. (c) 2016
 *   
 */
definition(
    name: "Prempoint",
    namespace: "prempoint.com",
    author: "Prempoint Inc.",
    description: "SmartThings service for Prempoint",
    category: "Connections",
    iconUrl: "http://www.prempoint.com/images/social_app_emblem_50x50.png",
    iconX2Url: "http://www.prempoint.com/images/social_app_emblem_100x100.png",
    iconX3Url: "http://www.prempoint.com/images/social_app_emblem_150x150.png",
    oauth: [displayName: "Prempoint", displayLink: "http://www.prempoint.com/"])

preferences {
	section("Allow Prempoint to Control & Access These Things...") {
		input "switches", "capability.switch", title: "Which Switches?", multiple: true, required: false
        input "locks", "capability.lock", title: "Which Locks?", multiple: true, required: false
        input "garagedoors", "capability.garageDoorControl", title: "Which Garage Doors?", multiple: true, required: false
        //input "doors", "capability.doorControl", title: "Which Doors?", multiple: true, required: false
        input "cameras", "capability.imageCapture", title: "Which Cameras?", multiple: true, required: false
	}
}

mappings {
	path("/list") {
		action: [
			GET: "listDevices"
		]
	}
	path("/switches") {
		action: [
			GET: "listSwitches"
		]
	}
	path("/switches/:id") {
		action: [
			GET: "showSwitch"
		]
	}
    path("/switches/:id/:command") {
		action: [
			GET: "updateSwitch"
		]
	}
	path("/switches/:id/:command/:level") {
		action: [
			GET: "updateSwitch"
		]
	}
	path("/locks") {
		action: [
			GET: "listLocks"
		]
	}
	path("/locks/:id") {
		action: [
			GET: "showLock"
		]
	}
	path("/locks/:id/:command") {
		action: [
			GET: "updateLock"
		]
	}
    path("/doors/:id") {
		action: [
			GET: "showDoor"
		]
	}
   	path("/doors/:id/:command") {
		action: [
			GET: "updateDoor"
		]
	}
	path("/garagedoors/:id") {
		action: [
			GET: "showGarageDoor"
		]
	}
   	path("/garagedoors/:id/:command") {
		action: [
			GET: "updateGarageDoor"
		]
	}
    path("/cameras/:id") {
		action: [
			GET: "showCamera"
		]
	}  
    path("/cameras/:id/:command") {
		action: [
			GET: "updateCamera"
		]
	}  
}

def installed() {}

def updated() {}

def listDevices() {
	log.debug "entering listDevices"
    //return listSwitches() + listLocks() + listGarageDoors() + listDoors() + listCameras()
    return listSwitches() + listLocks() + listGarageDoors() + listCameras()
}

//switches
def listSwitches() {
    log.debug "entering listSwitches"
	switches.collect{showDevice(it,"switch")}
}

def showSwitch() {
	log.debug "entering showSwitches"
	show(switches, "switch")
}

def updateSwitch() {
	log.debug "entering updateSwitches"
	update(switches, "switch")
}

//locks
def listLocks() {
	log.debug "entering listLocks"
	locks.collect{showDevice(it,"lock")}
}

def showLock() {
	log.debug "entering showLock"
	show(locks, "lock")
}

def updateLock() {
	log.debug "entering updateLock"
	update(locks, "lock")
}

//doors
def listDoors() {
	log.debug "entering listDoors"
	locks.collect{showDevice(it,"door")}
}

def showDoor() {
	log.debug "entering showDoors"
	show(doors, "door")
}

def updateDoor() {
	log.debug "entering updateDoor"
	update(doors, "door")
}

//garagedoors
def listGarageDoors() {
	log.debug "entering listGarageDoors"
	locks.collect{showDevice(it,"garagedoor")}
}

def showGarageDoor() {
	log.debug "entering showGarageDoors"
	show(garagedoors, "garagedoor")
}

def updateGarageDoor() {
	log.debug "entering updateGarageDoor"
	update(gargedoors, "garagedoor")
}

//cameras
def listCameras() {
	log.debug "entering listCameras"
	cameras.collect{showDevice(it,"image")}
}

def showCamera() {
	log.debug "entering showCameras"
	show(cameras, "camera")
}

def updateCamera() {
	log.debug "entering updateCamera"
	update(cameras, "camera")
}

def deviceHandler(evt) {}

private update(devices, type) {
	def rc = null

	//def command = request.JSON?.command
    def command = params.command
    
    log.debug "update, request: params: ${params}, devices: $devices.id type=$type command=$command"
    
    // Process the command.
	if (command) 
    {
		def dev = devices.find { it.id == params.id }
		if (!dev) {
			httpError(404, "Device not found: $params.id")
		} else if (type == "switch") {
			switch(command) {
			    case "on":
			    	rc = dev.on()
			    	break
			    case "off":
			    	rc = dev.off()
			    	break
			    default:
			    	httpError(400, "Device command=$command is not a valid for device=$it.id $dev")
			}
		} else if (type == "lock") {
			switch(command) {
			    case "lock":
			    	rc = dev.lock()
			    	break
			    case "unlock":
			    	rc = dev.unlock()
			    	break
			    default:
			    	httpError(400, "Device command=$command is not a valid for device:=$it.id $dev")
			}
		} else if (type == "door") {
			switch(command) {
			    case "open":
			    	rc = dev.open()
			    	break
			    case "close":
			    	rc = dev.close()
			    	break
			    default:
			    	httpError(400, "Device command=$command is not a valid for device=$it.id $dev")
			}
		} else if (type == "garagedoor") {
			switch(command) {
			    case "open":
			    	rc = dev.open()
			    	break
			    case "close":
			    	rc = dev.close()
			    	break
			    default:
			    	httpError(400, "Device command=$command is not a valid for device=$it.id $dev")
			}
		} else if (type == "camera") {
			switch(command) {
			    case "take":
			    	rc = dev.take()
			    	log.debug "Device command=$command device=$it.id $dev current image=$it.currentImage"
			    	break
			    default:
			    	httpError(400, "Device command=$command is not a valid for device=$it.id $dev")
			}
		}

		log.debug "executed device=$it.id $dev command=$command rc=$rc"
        
        // Check that the device is a switch that is currently on, supports 'setLevel"
        // and that a level was specified.
        int level = params.level ? params.level as int : -1;
        if ((type == "switch") && (dev.currentValue('switch') == "on") && hasLevel(dev) && (level != -1)) {            
            log.debug "device about to setLevel=$level"
            dev.setLevel(level);
        }
        
        // Show the device info if necessary.
        if (rc == null) {
        	rc = showDevice(dev, type)
        }
	}
    
    return rc
}

private show(devices, type) {
	def dev = devices.find { it.id == params.id }
	if (!dev) {
		httpError(404, "Device not found")
	} else { 
        // Show the device info.
        showDevice(dev, type)
	}
}

private showDevice(it, type) {
	def props = null

	// Get the current state for the device type.
	def state = [it.currentState(type)]
    
	// Check that whether the a switch device with level support is located and update the returned device type.
    def devType =  type
    
    if (type == "switch" && hasLevel(it)) {
    	// Assign "switchWithLevel" to device type.
    	devType = "switchWithLevel"
        // Add the level state.
        def levelState = it.currentState("level")
        if (levelState) {
        	state.add(levelState)
        }
    }
    
    log.debug "device label=$it.label type=$devType"
    
    // Assign the device item properties if appropriate.
    if (it) {
    	props = [id: it.id, label: it.label, type: devType, state: state]
        // Add the hub information to the device properties
        // if appropriate.
        if (it.hub) {
        	props.put("location", it.hub.hub.location)
        }
        if (it.currentImage) {
        	props.put("currentImage", it.currentImage)
        }
    }
    
    return props
}

private hasLevel(device) {
    // Default return value.
    def rc = false;

	// Get the device supported commands.
    def supportedCommands = device.supportedCommands
    
    // Check to see if the "setLevel" was found and assign
    // the appropriate return value.
    if (supportedCommands) {
        // Find the "setLevel" command. 
        rc = supportedCommands.toString().indexOf("setLevel") != -1
    }
    
    log.debug "hasLevel device label=$device.label supportedCommands=$supportedCommands rc=$rc"
            
    return rc
}