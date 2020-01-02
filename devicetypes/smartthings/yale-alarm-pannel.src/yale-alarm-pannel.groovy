/**
 *V1 to fun with smartapp
 *///, ocfDeviceType:"oic.d.securityPanel" // Required Resource name = Mode , Required Resource Type = oic.r.mode
 //vid: "generic-siren-8", ) no battery
 //vid: "SmartThings-smartthings-Z-Wave_Siren" no battery
 //vid: "generic-siren-11" temp and battery 
 
preferences {
	input description: "Once you have filled in your details \nUse “Alarm off” to Disarm in any mode \nUse “siren” to Home Arm (Arm Stay) \nUse “both” or “siren and strobe”  to Fully Arm (Arm away).", title: "Guide", displayDuringSetup: false, type: "paragraph", element: "paragraph"
	//input ("sendPushMessage", "text", title: "Send a push notification?", description: "type yes to recive push messages")
}

metadata {
	definition (name: "Yale Alarm pannel", namespace: "smartthings", author: "Tapion1ives/Mark-C-uk/foyst", ocfDeviceType: "x.com.st.d.siren", vid: "generic-siren-2") { 
    
		capability "Alarm"
		//capability "Lock"
        capability "Refresh"
		//capability "Switch"
		capability "Polling"
		
        command "datain"
        command "postcmd"
        
        attribute "supportedModes", "string"
        attribute "modes", "string"
	}
    
	tiles {
		standardTile("modes", "device.modes", inactiveLabel: false, width: 2, height: 2) {
			state ("default", label:'${currentValue}', defaultState: true, action: "refresh", icon:"st.security.alarm.alarm", backgroundColor:"#e86d13")
			state ("armedStay", label:'Armed-Stay', action: "alarm.off", icon:"st.Home.home4", backgroundColor:"#00a0dc", nextState:"Disarming")
			state ("disarm", label:'Disarmed', action: "siren", icon:"st.Home.home2", backgroundColor:"#ffffff", nextState:"Arming")
			state ("armedAway", label:'Armed-Away', action: "alarm.off", icon:"st.Home.home3", backgroundColor:"#00a0dc", nextState:"Disarming")
			state ("Arming", label:'${name}', icon:"st.Home.home4", backgroundColor:"#cccccc")
			state ("Disarming", label:'${name}', icon:"st.Home.home2", backgroundColor:"#cccccc")
		}
		standardTile("statusstay", "device.alarm", inactiveLabel: false, decoration: "flat") {
			state "default", label:'Arm Stay', action:"alarm.siren", icon:"st.Home.home4"
		}
		standardTile("statusaway", "device.alarm", inactiveLabel: false, decoration: "flat") {
			state "default", label:'Arm Away', action:"alarm.both", icon:"st.Home.home3"
		}
		standardTile("statusdisarm", "device.alarm", inactiveLabel: false, decoration: "flat") {
			state "default", label:'Disarm', action:"alarm.off", icon:"st.Home.home2"
		}
		standardTile("refresh", "device.refresh", inactiveLabel: false, decoration: "flat") {
			state "default", label:'', action:"refresh.refresh", icon:"st.secondary.refresh"
		}

		main (["modes"])
		details(["modes", "statusaway", "statusstay", "statusdisarm", "refresh"])
	}
}

def updated() {
	log.debug "updated"
    def supportedModes = []
	supportedModes << "active" 
	supportedModes << "armedAway" 
	supportedModes << "armedStay" 
	supportedModes << "disarm" 
    supportedModes << "disabled"
	
	state.supportedModes = supportedModes
    log.info "Supportedmodes - $supportedModes"
	sendEvent(name: "supportedModes", value: supportedModes, displayed: false)
	unschedule()
}
def installed() {
	log.debug "installed"
}

def poll() {
	log.debug "poll"
	refresh()
}

def refresh() {
	log.debug "Refresh"
    try {
    parent.getDeviceData()
    }
    catch (e){
    log.warn "refresh error $e"
    }
}
def datain(data) {
	//log.debug "Datain $data"
    //log.debug "Datain ${data?.message} ,  ${data?.data}" //${data?.data[0].mode},
    	def dmsg = data?.message
    	def resp = data?.data[0]?.mode
    	if (resp.equals("arm")) {
            state.errorCount = 0
            state.modes = "armedAway"
            state.lock = "unlocked"
            state.switch = "on"
            state.alarm = "both"
        }
        else if (resp.equals("home")) {
            state.errorCount = 0
            state.modes = "armedStay"
            state.lock = "locked"
            state.switch = "off"
            state.alarm = "siren"
        }
        else if (resp.equals("disarm")) {
            state.errorCount = 0
            state.modes = "disarm"
            state.lock = "unlocked"
            state.switch = "off"
            state.alarm = "off"
        }
        else { //if (YaleAlarmState.mode.contains("system.permission_denied")) {
            state.errorCount = state.errorCount +1
            log.warn "system off line / Error, response= '${data?.data[0]}' or Resp $resp"
            state.modes = "disabled" //"${data?.mode} - ${data?.message}"
            state.alarm = "disabled"
            if (state.errorCount < 5){ runIn(30,refresh)}
        }
        if (dmsg != "OK!"){
        	state.errorCount = state.errorCount +1
        	log.warn "$dmsg"
            state.modes = "disabled"
            state.alarm = "disabled"
            if (state.errorCount < 5){runIn(20,refresh)}
        }
        log.info "Data pushed in, state is ${state.modes}, error are '${state.errorCount}'" //${data?.message}
		sendEvent(name: "modes", value: state.modes, displayed: true, descriptionText: "Refresh - response '$dmsg'") //isStateChange: false,
		//sendEvent(name: "lock", value: state.lock, displayed: false) //isStateChange: false,
		//sendEvent(name: "switch", value: state.switch, displayed: false) //isStateChange: false,
        sendEvent(name: "alarm", value: state.alarm, displayed: false,, descriptionText: "Refresh - response '$dmsg'") //isStateChange: false,
}

// ==================== Buttons / voice comands ==========================
def lock() { armStay() }
def siren() {armStay() }
def strobe() {armStay() }

def on() {	armAway() }
def both() {armAway() }

def off() {	disarm()}
def unlock() { disarm() }
// ===================   Buttons / voice comands end == in to Modes ====================

def armAway(mode) {
	mode = "arm"
    state.modes = "armedAway"
	postcmd(mode)
}
def armStay(mode) {
	mode = "home"
    state.modes = "armedStay"
	postcmd(mode)
}
def disarm(mode) {
	mode = "disarm"
    state.modes = "disarm"
	postcmd(mode)
}
def modes (mode) {
	log.debug "direct modes command $mode"
	if (mode == "active") { log.warn "not handled $mode"}
	if (mode == "armedAway") {armAway()}
	if (mode == "armedStay") {armStay()}
	if (mode == "disarm") {disarm()}
}
def setAlertState(Astate){
log.debug " Aleart state is $Astate"
}
// ===================   Modes end    ==  in to post CMDs ====================
def postcmd(mode){
	log.trace "postcmd outgoing Mode CMD $mode "
    def data = parent.ArmDisRef(mode)
    log.debug "POSTCMD $data"
    def dmsg = ''
    if (data != "error"){
    	dmsg = data?.message
    }
    else {
    	dmsg = data
    }
	if (dmsg == 'OK!'){
    	state.errorCount = 0
    	if (mode == 'arm'){
        	state.modes = "armedAway"
            state.lock = "unlocked"
            state.switch = "on"
            state.alarm = "both"
        }
        else if (mode == 'disarm'){
        	state.modes = "disarm"
            state.lock = "unlocked"
            state.switch = "off"
            state.alarm = "off"
        }
        else if (mode == 'home'){
        	state.modes = "armedStay"
            state.lock = "locked"
            state.switch = "off"
            state.alarm = "siren"
        }
    }
	else {
    	state.errorCount = state.errorCount +1
    	state.modes = "disabled"
        state.alarm = "disabled"
    }
    log.info "Mode Change state is ${state.modes}, $dmsg, errors are ${state.errorCount}"
	sendEvent(name: "modes", value: state.modes, displayed: true, descriptionText: "Mode Change to ${state.modes} - $dmsg") //isStateChange: false,
    //sendEvent(name: "lock", value: state.lock, displayed: false) //isStateChange: false,
	//sendEvent(name: "switch", value: state.switch, displayed: false) //isStateChange: false,
    sendEvent(name: "alarm", value: state.alarm, displayed: false, descriptionText: "Mode Change to ${state.modes} - $dmsg") //isStateChange: false,
}

// parse events into attributes
def parse(String description) {
	log.debug "Parsing '${description}'"
}