/**
 *V1 to fun with smartapp
 */
preferences {
	input description: "Once you have filled in your details \nUse “Switch off” to Disarm in any mode \nUse “Lock” to Home Arm (Arm Stay) \nUse “Switch on” to Fully Arm (Arm away).", title: "Guide", displayDuringSetup: false, type: "paragraph", element: "paragraph"
	//input ("sendPushMessage", "text", title: "Send a push notification?", description: "type yes to recive push messages")
}

metadata {
	definition (name: "Yale Alarm pannel", namespace: "smartthings", author: "Tapion1ives/Mark-C-uk/foyst") {

		capability "Refresh"
		capability "Switch"
		capability "lock"
		capability "Polling"
		command "datain"
        command "postcmd"
	}
	tiles {
		standardTile("Amode", "device.Amode", inactiveLabel: false, width: 2, height: 2) {
			state ("default", label:'${currentValue}', defaultState: true, action: "refresh", icon:"st.security.alarm.alarm", backgroundColor:"#e86d13")
			state ("Armed-Stay", label:'${name}', action: "switch.off", icon:"st.Home.home4", backgroundColor:"#00a0dc", nextState:"Disarming")
			state ("Disarmed", label:'${name}', action: "lock", icon:"st.Home.home2", backgroundColor:"#ffffff", nextState:"Arming")
			state ("Armed-Away", label:'${name}', action: "switch.off", icon:"st.Home.home3", backgroundColor:"#00a0dc", nextState:"Disarming")
			state ("Arming", label:'${name}', icon:"st.Home.home4", backgroundColor:"#cccccc")
			state ("Disarming", label:'${name}', icon:"st.Home.home2", backgroundColor:"#cccccc")
		}
		standardTile("statusstay", "device.lock", inactiveLabel: false, decoration: "flat") {
			state "default", label:'Arm Stay', action:"lock.lock", icon:"st.Home.home4"
		}
		standardTile("statusaway", "device.switch", inactiveLabel: false, decoration: "flat") {
			state "default", label:'Arm Away', action:"switch.on", icon:"st.Home.home3"
		}
		standardTile("statusdisarm", "device.switch", inactiveLabel: false, decoration: "flat") {
			state "default", label:'Disarm', action:"switch.off", icon:"st.Home.home2"
		}
		standardTile("refresh", "device.refresh", inactiveLabel: false, decoration: "flat") {
			state "default", label:'', action:"refresh.refresh", icon:"st.secondary.refresh"
		}

		main (["Amode"])
		details(["Amode", "statusaway", "statusstay", "statusdisarm", "refresh"])
	}
}

def updated() {
	log.debug "updated"
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
            state.mode = 'Armed-Away'
            state.errorCount = 0
        }
        else if (resp.equals("home")) {
            state.mode = 'Armed-Stay'
            state.errorCount = 0
        }
        else if (resp.equals("disarm")) {
            state.mode = 'Disarmed'
            state.errorCount = 0
        }
        else { //if (YaleAlarmState.mode.contains("system.permission_denied")) {
            state.errorCount = state.errorCount +1
            log.warn "system off line / Error, response= '${data?.data[0]}' or Resp $resp"
            state.mode = 'default' //"${data?.mode} - ${data?.message}"
            if (state.errorCount < 5){ runIn(30,refresh)}
        }
        if (dmsg != "OK!"){
        	state.errorCount = state.errorCount +1
        	log.warn "$dmsg"
            state.mode = 'default'
            if (state.errorCount < 5){runIn(20,refresh)}
        }
        log.info "Refresh (datain) state is ${state.mode}, error are '${state.errorCount}'" //${data?.message}
		sendEvent(name: "Amode", value: state.mode, displayed: true, descriptionText: "Refresh - response '$dmsg'") //isStateChange: false,

}

// ==================== Buttons / voice comands ==========================
def lock() {
	armStay()
}
def unlock() {
	disarm()
}
def on() {
	armAway()
}
def off() {
	disarm()
}
// ===================   Buttons / voice comands end == in to Modes ====================
def armAway(mode) {
	mode = "arm"
    state.mode = 'arm'
	//log.debug "armaway mode ${mode.value}"
	postcmd(mode)
}
def armStay(mode) {
	mode = "home"
    state.mode = 'home'
	//log.debug "arm stay mode ${mode.value}"
	postcmd(mode)
}
def disarm(mode) {
	mode = "disarm"
    state.mode = 'disarm'
    //endp = endpointMode()
	//log.debug "disarm mode ${mode.value} "
	postcmd(mode)
}
// ===================   Modes end    ==  in to post CMDs ====================
def postcmd(mode){
	log.trace "outgoing Mode CMD $mode "
    def data = parent.ArmDisRef(mode)
    //log.debug "POSTCMD $data"
    def dmsg = ''
    if (data != "error"){
    	dmsg = data?.message
    }
    else {
    	dmsg = data
    }
	if (dmsg == 'OK!'){
    	if (mode == 'arm'){
        	state.mode = 'Armed-Away'
        }
        else if (mode == 'disarm'){
        	state.mode = 'Disarmed'
        }
        else if (mode == 'home'){
        	state.mode = 'Armed-Stay'
        }
    }
	else {
    	state.mode = 'default'
    }
    log.info "Mode Change state is ${state.mode}, $dmsg, errors are ${state.errorCount}"
	sendEvent(name: "Amode", value: state.mode, displayed: true, descriptionText: "Mode Change to ${state.mode} - $dmsg") //isStateChange: false,
}

// parse events into attributes
def parse(String description) {
	log.debug "Parsing '${description}'"
}