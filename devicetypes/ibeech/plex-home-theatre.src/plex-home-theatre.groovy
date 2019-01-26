metadata {
	definition (name: "Plex Home Theatre", namespace: "ibeech", author: "ibeech") {
    	capability "Switch"
	capability "musicPlayer"
	capability "Sensor"
	capability "Actuator"
        
        command "scanNewClients"
        command "setPlaybackIcon", ["string"]
        command "setPlaybackTitle", ["string"]
        command "setVolumeLevel", ["number"]     
		command "setPlaybackPosition", ["number"]     
		command "setPlaybackDuration", ["number"]     
		command "playbackType", ["string"]
		command "stepBack"
		command "stepForward"
		command "home"
		command "moveUp"
		command "music"
		command "moveLeft"
		command "select"
		command "moveRight"
		command "back"
		command "moveDown"
        
        input name: "CommandTarget", type: "enum", title: "Command Target", options: ["Server", "Client", "ServerProxy"], description: "Select Command Target", required: true, defaultValue: "Client"
        input name: "TimelineStatus", type: "enum", title: "Timeline Status", options: ["None", "Subscribe", "Poll", "ServerSubscribe"], description: "Select How To Get Status", required: true, defaultValue: "Subscribe"
        }

	simulator {
		// TODO: define status and reply messages here
	}

	tiles(scale: 2) {
        
        multiAttributeTile(name:"status", type: "generic", width: 6, height: 4, canChangeIcon: true){
            tileAttribute ("device.status", key: "PRIMARY_CONTROL") {
            attributeState "playing", label:'Playing', action:"music Player.pause", icon:"st.Electronics.electronics16", nextState:"paused", backgroundColor:"#00a0dc"
            attributeState "stopped", label:'Stopped', action:"music Player.play", icon:"st.Electronics.electronics16", backgroundColor:"#ffffff"
            attributeState "paused", label:'Paused', action:"music Player.play", icon:"st.Electronics.electronics16", nextState:"playing", backgroundColor:"#e86d13"
        }        
            tileAttribute ("device.trackDescription", key: "SECONDARY_CONTROL") {
                attributeState "trackDescription", label:'${currentValue}'
            }

            tileAttribute ("device.level", key: "SLIDER_CONTROL") {
                attributeState "level", action: "setVolumeLevel"
            }
        }
        
        standardTile("previous", "device.status", width: 1, height: 1, decoration: "flat") {
        	state "previous", label:'', action:"music Player.previousTrack", icon:"st.sonos.previous-btn", backgroundColor:"#ffffff"
        }	
        
        standardTile("stepBack", "device.status", width: 1, height: 1, decoration: "flat") {
        	state "stepBack", label:'<10', action:"stepBack", icon:"", backgroundColor:"#ffffff"
        }	
        
        standardTile("stop", "device.status", width: 2, height: 1, decoration: "flat") {
            state "default", label:'', action:"music Player.stop", icon:"st.sonos.stop-btn", backgroundColor:"#ffffff"
        }
        
        standardTile("stepForward", "device.status", width: 1, height: 1, decoration: "flat") {
        	state "stepForward", label:'>30', action:"stepForward", icon:"", backgroundColor:"#ffffff"
        }	
        
        standardTile("next", "device.status", width: 1, height: 1, decoration: "flat") {
        	state "next", label:'', action:"music Player.nextTrack", icon:"st.sonos.next-btn", backgroundColor:"#ffffff"
        }
        
        valueTile("playbackType", "device.playbackType", decoration: "flat", width: 6, height: 1) {
            state "playbackType", label:'Playing: ${currentValue}', defaultState: true
        }
        
        controlTile("playbackPosition", "device.playbackPosition", "slider", width: 5, height: 1, range:"(0..100)") {
            state "playbackPosition", label:'Position', action:"setPlaybackPosition"
        }
        
        valueTile("playbackDuration", "device.playbackDuration", width: 1, height: 1) {
            state "playbackDuration", label:'${currentValue}', defaultState: 0
        }

        standardTile("home", "device.status", width: 2, height: 2, decoration: "flat") {
            state "default", label:'', action:"home", icon:"st.Home.home2", backgroundColor:"#ffffff"
        }
		
		standardTile("moveUp", "device.status", width: 2, height: 2, decoration: "flat") {
            state "default", label:'', action:"moveUp", icon:"st.thermostat.thermostat-up", backgroundColor:"#ffffff"
        }
		
		standardTile("music", "device.status", width: 2, height: 2, decoration: "flat") {
            state "default", label:'music', action:"music", icon:"", backgroundColor:"#ffffff"
        }
        
        standardTile("moveLeft", "device.status", width: 2, height: 2, decoration: "flat") {
            state "default", label:'', action:"moveLeft", icon:"st.thermostat.thermostat-left", backgroundColor:"#ffffff"
        }
		
		standardTile("select", "device.status", width: 2, height: 2, decoration: "flat") {
            state "default", label:'select', action:"select", icon:"", backgroundColor:"#ffffff"
        }
        
        standardTile("moveRight", "device.status", width: 2, height: 2, decoration: "flat") {
            state "default", label:'', action:"moveRight", icon:"st.thermostat.thermostat-right", backgroundColor:"#ffffff"
        }
		
		standardTile("back", "device.status", width: 2, height: 2, decoration: "flat") {
            state "default", label:'back', action:"back", icon:"", backgroundColor:"#ffffff"
        }
		
		standardTile("moveDown", "device.status", width: 2, height: 2, decoration: "flat") {
            state "default", label:'', action:"moveDown", icon:"st.thermostat.thermostat-down", backgroundColor:"#ffffff"
        }
		
		standardTile("scanNewClients", "device.status", width: 2, height: 2, decoration: "flat") {
            state "default", label:'New Clients', action:"scanNewClients", icon:"st.secondary.refresh", backgroundColor:"#ffffff"
        }        
		
	main "status"
    }
}

// parse events into attributes
def parse(String description) {
	log.debug "Virtual switch parsing '${description}'"
}

def play() {
	log.debug "Executing 'on'"	     
    
    
    sendEvent(name: "switch", value: device.deviceNetworkId + ".play");    
    sendEvent(name: "switch", value: "on");    
    sendEvent(name: "status", value: "playing");
}

def pause() {
	log.debug "Executing 'pause'"
	    
	sendEvent(name: "switch", value: device.deviceNetworkId + ".pause");     
    sendEvent(name: "switch", value: "off");
    sendEvent(name: "status", value: "paused");
}

def stop() {
	log.debug "Executing 'off'"
	    
	sendEvent(name: "switch", value: device.deviceNetworkId + ".stop");     
    sendEvent(name: "switch", value: "off");
    sendEvent(name: "status", value: "stopped");
    //setPlaybackTitle("Stopped");
}

def previousTrack() {
	log.debug "Executing 'previous': "
    
    //setPlaybackTitle("Skipping previous");
    sendCommand("previous");    
}

def nextTrack() {
	log.debug "Executing 'next'"

	setPlaybackTitle("Skipping next");
	sendCommand("next");
}

def scanNewClients() {
	log.debug "Executing 'scanNewClients'"        
    sendCommand("scanNewClients");
}

def setVolumeLevel(level) {
	log.debug "Executing 'setVolumeLevel(" + level + ")'"
    sendEvent(name: "level", value: level);
    sendCommand("setVolume." + level);
}

def setPlaybackPosition(level) {
	log.debug "Executing 'setPlaybackPosition(" + level + ")'"
    sendEvent(name: "playbackPosition", value: level);
    sendCommand("setPosition." + level);
}

def setPlaybackDuration(level) {
	log.debug "Executing 'setPlaybackDuration(" + level + ")'"
    sendEvent(name: "playbackDuration", value: level);
}

def stepBack() {
	log.debug "Executing 'stepBack'"

	//setPlaybackTitle("Jumping back 10s");
	sendCommand("stepBack");
}

def stepForward() {
	log.debug "Executing 'stepForward'"

	//setPlaybackTitle("Jumping up 30s");
	sendCommand("stepForward");
}

def home() {
	log.debug "Executing 'home'"

	sendCommand("home");
}

def moveUp() {
	log.debug "Executing 'moveUp'"

	sendCommand("moveUp");
}

def music() {
	log.debug "Executing 'music'"

	sendCommand("music");
}

def moveLeft() {
	log.debug "Executing 'moveLeft'"

	sendCommand("moveLeft");
}

def select() {
	log.debug "Executing 'select'"

	sendCommand("select");
}

def moveRight() {
	log.debug "Executing 'moveRight'"

	sendCommand("moveRight");
}

def back() {
	log.debug "Executing 'back'"

	sendCommand("back");
}

def moveDown() {
	log.debug "Executing 'moveDown'"

	sendCommand("moveDown");
}

def sendCommand(command) {
	
    def lastState = device.currentState('switch').getValue();
    sendEvent(name: "switch", value: device.deviceNetworkId + "." + command);
    sendEvent(name: "switch", value: lastState);
}

def setPlaybackState(state) {

	log.debug "Executing 'setPlaybackState' to state $state"
    switch(state) {
        case "stopped":
        sendEvent(name: "switch", value: "off");
        sendEvent(name: "status", value: "stopped");
        break;

        case "playing":
        sendEvent(name: "switch", value: "on");
        sendEvent(name: "status", value: "playing");
        break;

        case "paused":
        sendEvent(name: "switch", value: "off");
        sendEvent(name: "status", value: "paused");
    }
}

def setPlaybackTitle(text) {
	log.debug "Executing 'setPlaybackTitle'"
    
    sendEvent(name: "trackDescription", value: text)
}

def setPlaybackIcon(iconUrl) {
	log.debug "Executing 'setPlaybackIcon'"
    
    state.icon = iconUrl;
    
    //sendEvent(name: "scanNewClients", icon: iconUrl)
    //sendEvent(name: "scanNewClients", icon: iconUrl)
    
    log.debug "Icon set to " + state.icon
}

def playbackType(type) {
	sendEvent(name: "playbackType", value: type);
}

def volumeLevelIn(level) {
	sendEvent(name: "level", value: level);
}

def playbackPositionIn(level) {
	sendEvent(name: "playbackPosition", value: level);
}

def playbackDurationIn(level) {
	sendEvent(name: "playbackDuration", value: level);
}