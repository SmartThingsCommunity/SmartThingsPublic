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
	}
	tiles {
		standardTile("mode", "device.mode", inactiveLabel: false, width: 2, height: 2) {
			state ("default", label:'${currentValue}', defaultState: true, action: "device.refresh", icon:"st.security.alarm.alarm", backgroundColor:"#e86d13")
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

		main (["mode"])
		details(["mode", "statusaway", "statusstay", "statusdisarm", "refresh"])
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
    parent.getDeviceData()
}
def datain(data) {
	log.debug "Datain ${data?.message} , ${data?.data[0].mode}, ${data?.data[0]}"
    def resp = data?.data[0].mode
    	if (resp.equals("arm")) {
            state.mode = 'Armed-Away'
        }
        else if (resp.equals("home")) {
            state.mode = 'Armed-Stay'
        }
        else if (resp.equals("disarm")) {
            state.mode = 'Disarmed'
        }
        else { //if (YaleAlarmState.mode.contains("system.permission_denied")) {
            log.warn "system off line / Error, response= '${data?.data[0]}' or Resp $resp"
            state.mode = 'default' //"${data?.mode} - ${data?.message}"
            runIn(30,refresh)
        }
        log.info "datain state is ${state.mode}, ${data?.message}"
		sendEvent(name: "mode", value: state.mode, displayed: true, descriptionText: "Datain - response '${data?.message}'") //isStateChange: false,

}
/*if (responsecode != 200) {
        	state.errorcount = state.errorcount + 1
    		log.warn "${responsecode}' - try getting new token, error count is ${state.errorcount}"
    		if (state.errorcount > 2){
            	state.mode = "refersh error lots of errors"
    			log.error "too many errors"
                send("Refresh issue  '${responsecode}', error count '${state.errorcount}'")
        		state.errorcount = 0
			}
    		else {
				login()
            	runIn(05, refresh)
                state.mode = "refersh error count ${state.errorcount}"
            }
        }
        else {
        	state.errorcount = 0
			//YaleAlarmState = response.data.data.getAt(0)
		
        if (YaleAlarmState.mode.equals("arm")) {
            state.mode = 'Armed-Away'
        }
        else if (YaleAlarmState.mode.equals("home")) {
            state.mode = 'Armed-Stay'
        }
        else if (YaleAlarmState.mode.equals("disarm")) {
            state.mode = 'Disarmed'
        }
        else { //if (YaleAlarmState.mode.contains("system.permission_denied")) {
            log.warn "system off line / Error, response= '$YaleAlarmState'"
            state.mode = YaleAlarmState
            runIn(30,refresh)
		}
 	}

	log.info "'$device' REFRESH - Mode is '$state.mode', Response- '$YaleAlarmState' complete"
	sendEvent(name: "mode", value: state.mode, isStateChange: true, displayed: true, descriptionText: "Refresh - mode is '$state.mode', response '$YaleAlarmState'")

*/
	
//}
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
	//log.debug "armaway mode ${mode.value}"
	postcmd(mode)
}
def armStay(mode) {
	mode = "home"
	//log.debug "arm stay mode ${mode.value}"
	postcmd(mode)
}
def disarm(mode) {
	mode = "disarm"
    //endp = endpointMode()
	//log.debug "disarm mode ${mode.value} "
	postcmd(mode)
}
// ===================   Modes end    ==  in to post CMDs ====================
def postcmd(mode){
	log.debug "Incoming Mode CMD ${mode.value} "
    parent.ArmDisRef(mode)
}

// parse events into attributes
def parse(String description) {
	log.debug "Parsing '${description}'"
}