/**
 *Version 0.2 - Added guide to use on settings page
 *Version 0.1 - First version arms/home/disarm the alarm
 */
preferences {
	input("userName", "text", title: "Username", description: "Your username for Yale Home System")
	input("password", "password", title: "Password", description: "Your Password for Yale Home System")
	input description: "Once you have filled in your details \nUse “Switch off” to Disarm in any mode \nUse “Lock” to Home Arm (Arm Stay) \nUse “Switch on” to Fully Arm (Arm away).", title: "Guide", displayDuringSetup: false, type: "paragraph", element: "paragraph"

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
	refresh()
}
def installed() {
	refresh() 
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
}
//

//def logout(token) {
//    def paramsLogout = [
//            uri: "https://www.yalehomesystem.co.uk/homeportal/api/logout/",
//            headers: ['Cookie' : "${token}"]
//    ]
//    httpPost(paramsLogout) { responseLogout ->
//    }
//    log.info "'$device' Logged out"
//}
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
    			log.error "too many errors"
        		state.errorcount = 0
			}
    		else {
				login()
            	runIn(05, refresh)
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
//    logout(token)
	log.info "'$device' REFRESH - Mode is '$state.mode', Response- '$YaleAlarmState' complete"
	sendEvent(name: "mode", value: state.mode, displayed: true, descriptionText: "Refresh - mode is '$state.mode', response '$YaleAlarmState'")
	runEvery3Hours(refresh)
	return YaleAlarmState
}

// ===================  Arm Function. Performs arming function ====================
def armAway() {
	def reply = ''
    def responsecode
	def paramsArm = [
			uri: baseUrl() + endpointMode(),
			body: [area: 1, mode: "arm"],
			headers: ['Authorization' : "Bearer ${state.accessToken}"],
			requestContentType: "application/x-www-form-urlencoded",
			contentType: "application/json"
	]
	httpPost(paramsArm) {	response -> // Arming Function in away mode
		reply = response.data.message
		log.debug "AA - response = '$reply'"
	}
	if (reply != 'OK!'){
		log.warn "$device - AA - Status '$reply'"
		state.mode = reply
		runIn(60,refresh)
	}
	else {
		state.mode = 'Armed-Away'
	}
//    logout(token)
	log.info "AA- Status is: '$reply' - mode '$state.mode', "
	sendEvent(name: "mode", value: state.mode, displayed: true, descriptionText: "Arm(Away) System - '$reply', mode - '$state.mode'")
}

def armStay() {
	def reply = ''
    def responsecode
	def paramsArm = [
			uri: baseUrl() + endpointMode(),
			body: [area: 1, mode: "home"],
			headers: ['Authorization' : "Bearer ${state.accessToken}"],
			requestContentType: "application/x-www-form-urlencoded",
			contentType: "application/json"
	]
	httpPost(paramsArm) {	response -> // Arming Function in away mode
		reply = response.data.message
        responsecode = response.status
		log.debug "AS - response '$response.data.message'"
	}
    if (responsecode != 200) {
        state.errorcount = state.errorcount + 1
    	log.warn "${responsecode}' - try getting new token, error count is ${state.errorcount}"
   		if (state.errorcount > 2){
   			log.error "too many errors"
       		state.errorcount = 0
		}
   		else {
			login()
         	runIn(10, armStay)
		}
	}
    else {
       	state.errorcount = 0
	if (reply != 'OK!'){
		log.warn "AS - response '$reply'"
		state.mode = reply

		runIn(60,refresh)
	}
	else {
		state.mode = 'Armed-Stay'
	}
//    logout(token)
	log.info "'$device' AS - Status is: '$reply' - mode '$state.mode'"
	sendEvent(name: "mode", value: state.mode, displayed: true, descriptionText: "Arm(Home) System - '$reply', mode - '$state.mode'") //state: state.mode,
}
}
def disarm() {
	def reply = ''
    def responsecode
	def paramsDisarm = [
			uri: baseUrl() + endpointMode(),
			body: [area: 1, mode: "disarm"],
			headers: ['Authorization' : "Bearer ${state.accessToken}"],
			requestContentType: "application/x-www-form-urlencoded",
			contentType: "application/json"
	]
	httpPost(paramsDisarm) {	response ->
		reply = response.data.message
        responsecode = response.status
		log.debug "$device - DA - response '$response.data.message' - $responsecode"
	}
    if (responsecode != 200) {
        state.errorcount = state.errorcount + 1
    	log.warn "${responsecode}' - try getting new token, error count is ${state.errorcount}"
   		if (state.errorcount > 2){
   			log.error "too many errors"
       		state.errorcount = 0
		}
   		else {
			login()
         	runIn(05, disarm)
		}
	}
    else {
       	state.errorcount = 0
		if (reply != 'OK!'){
			log.warn "$device - DA - response '$reply'"
			state.mode = reply
			runIn(10,refresh)
		}
		else {
			state.mode = 'Disarmed'
		}
//    logout(token)
		log.info "$device DA - Status is: '$reply' -'$state.mode'"
		sendEvent(name: "mode", value: state.mode, displayed: true, descriptionText: "Disarm - '$reply', mode - '$state.mode'")
	}
}
// handle commands
def lock() {
	log.debug "Executing 'Arm Stay'"
	armStay()
}
def unlock() {
	log.debug "Executing 'Disarm'"
	disarm()
}
def on() {
	log.debug "Executing 'Arm Away'"
	armAway()
}
def off() {
	log.debug "Executing 'Disarm'"
	disarm()
}
// parse events into attributes
def parse(String description) {
	log.debug "Parsing '${description}'"
}