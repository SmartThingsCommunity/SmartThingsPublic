/**
 *Version 0.2 - Added guide to use on settings page
 *Version 0.1 - First version arms/home/disarm the alarm
 */
preferences {
	input("userName", "text", title: "Username", description: "Your username for Yale Home System")
	input("password", "password", title: "Password", description: "Your Password for Yale Home System")
	input description: "Once you have filled in your details \nUse “Switch off” to Disarm in any mode \nUse “Lock” to Home Arm (Arm Stay) \nUse “Switch on” to Fully Arm (Arm away).", title: "Guide", displayDuringSetup: false, type: "paragraph", element: "paragraph"
	input ("sendPushMessage", "text", title: "Send a push notification?", description: "type yes to recive push messages")
}

metadata {
	definition (name: "Yale Alarm Switch", namespace: "smartthings", author: "Tapion1ives/Mark-C-uk/foyst") {

		capability "Refresh"
		capability "Switch"
		capability "lock"
		capability "Polling"

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
unschedule()
    login()
    runEvery3Hours(refresh)
}
def installed() {
	login() 
    runEvery3Hours(refresh)
}

def baseUrl() {
	return "https://mob.yalehomesystem.co.uk/yapi/"
}
def endpointToken() {
	return "o/token/"
}
def endpointMode() {
	return "api/panel/mode/"
}
def yaleAuthToken () {
	return "VnVWWDZYVjlXSUNzVHJhcUVpdVNCUHBwZ3ZPakxUeXNsRU1LUHBjdTpkd3RPbE15WEtENUJ5ZW1GWHV0am55eGhrc0U3V0ZFY2p0dFcyOXRaSWNuWHlSWHFsWVBEZ1BSZE1xczF4R3VwVTlxa1o4UE5ubGlQanY5Z2hBZFFtMHpsM0h4V3dlS0ZBcGZzakpMcW1GMm1HR1lXRlpad01MRkw3MGR0bmNndQ=="
}

// ================================== Login/out Function. Returns cookie for rest of the functions =========
def login() {
	log.debug "Attempting to login for new token"
	def paramsLogin = [
			uri: baseUrl() + endpointToken(),
			body: [grant_type: "password", username:settings.userName , password: settings.password],
			headers: ['Authorization' : "Basic ${yaleAuthToken()}"],
			requestContentType: "application/x-www-form-urlencoded",
			contentType: "application/json"
	]
	httpPost(paramsLogin) { responseLogin ->
		log.debug "Login response is $responseLogin.data"
		state.accessToken = responseLogin.data?.access_token
		state.refreshToken = responseLogin.data?.refresh_token
	}
	log.info "'$device' Logged in for new token ${state.accessToken}"
    sendEvent(name: "mode", value: "default", displayed: true, descriptionText: "token updated")
}

// ================================================ Login /out end ========================

def poll() {
	log.debug "poll"
	refresh()
}

def refresh() {
	//login()
	def YaleAlarmState
    def responsecode
	def getPanelMetaDataAndFullStatus = [
			uri: baseUrl() + endpointMode(),
			headers: ['Authorization' : "Bearer ${state.accessToken}"]
	]
	httpGet(getPanelMetaDataAndFullStatus) {	response ->
		//log.debug "'$device' REFRESH - response = '${response.data.data[0].mode}, ${response.status}"
        YaleAlarmState = response.data.data.getAt(0)
        responsecode = response.status
        }
        if (responsecode != 200) {
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
/*
// ============================	devices
		def DDetails
    	def DDetailsresponsecode
		def getDetails = [
			uri: baseUrl() + "api/panel/device_status/",
			headers: ['Authorization' : "Bearer ${state.accessToken}"]
		]
        //log.debug "REFRESH DD - '$getDetails'"
    	httpGet(getDetails) {	response ->
		//log.debug "REFRESH DD - response = '${response.data.data} " //${response.data} //${response.data.data[0]} - [0] is first item
        	//DDetails = response.data.data.getAt(0)
        	//DDetailsresponsecode = response.status
        response.data.data.each {
           log.debug "${it?.type} - ${it?.name} - ${it?.status_fault[0]} - ${it?.status_open[0]}"
        }
        }
// ==============================    devices
*/
	return YaleAlarmState
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
	def reply = ''
    def responsecode
	def paramsMode = [
			uri: baseUrl() + endpointMode(),
			body: [area: 1, mode: "${mode.value}"],
			headers: ['Authorization' : "Bearer ${state.accessToken}"],
			requestContentType: "application/x-www-form-urlencoded",
			contentType: "application/json"
	]
	httpPost(paramsMode) {	response ->
		reply = response.data.message
        responsecode = response.status
		log.debug "Mode change Request Response - $device - $responsecode - '${response.data}' - ${response}"
	}
    
    //// insert parent
    if (responsecode != 200) { //bad
        state.errorcount = state.errorcount + 1
    	log.warn "${responsecode}' - error count is ${state.errorcount}"
   		if (state.errorcount > 2){
   			log.error "too many errors"
       		state.errorcount = 0
            state.mode = responsecode
		}
   		else {
        	log.warn "${responsecode}' - try getting new token, error count is ${state.errorcount}"
			login()
         	runIn(05, disarm)
            state.mode = "Mod change - ${responsecode} - error count ${state.errorcount}"
		}
	} //bad end
    else { //good
       	state.errorcount = 0
		if (reply != 'OK!'){ //other bad response
			log.warn "$device response '$reply'"
			state.mode = reply
            send("Command issue ${state.mode}, messageing $reply")
			runIn(10,refresh)
		} // othe bad end
		else { //still good
        	if(mode == "disarm"){
				state.mode = 'Disarmed'
            }
            else if(mode == "home"){
            	state.mode = 'Armed-Stay'
            }
            else if(mode == "arm"){
            	state.mode = 'Armed-Away'
            }
            else{
            	state.mode = 'error'
            }
		} //still good end
	} //good end
    log.info "$device PostCMD- Status is: '$reply' -'$state.mode'"
	sendEvent(name: "mode", value: state.mode, displayed: true, descriptionText: "Mode Command - '$reply', mode - '$state.mode'")
}

private send(msg) {
    if ( sendPushMessage == "Yes" ) {
        log.debug "sending push message" 
        //sendPush(msg)
    }
}
// parse events into attributes
def parse(String description) {
	log.debug "Parsing '${description}'"
}