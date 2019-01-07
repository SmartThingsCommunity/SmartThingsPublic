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
	definition (name: "YALE ALARM SWITCH", namespace: "smartthings", author: "Tapion1ives",ocfDeviceType: "oic.d.switch", mnmn: "SmartThings", vid: "generic-switch") {

		capability "Refresh"
		capability "Switch"
		capability "lock"
		capability "Polling"
    	capability "Health Check"
	
    	attribute "status", "string"

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

// ================================== Login/out Function. Returns cookie for rest of the functions =========
def login(token) {
	def paramsLogin = [
		uri: "https://www.yalehomesystem.co.uk/homeportal/api/login/check_login/",
		body: [id:settings.userName , password: settings.password]
	]
	httpPost(paramsLogin) { responseLogin ->
			token = responseLogin.headers?.'Set-Cookie'?.split(";")?.getAt(0)
	}
    log.info "'$device' Logged in"
	return token
    
}

def logout(token) {
	def paramsLogout = [
		uri: "https://www.yalehomesystem.co.uk/homeportal/api/logout/",
		headers: ['Cookie' : "${token}"]
	]
	httpPost(paramsLogout) { responseLogout ->
	}
    log.info "'$device' Logged out"
}
// ================================================ Login /out end ========================

def poll() {
	log.debug "poll"
	refresh()
}

def ping() {
	log.debug "ping"
	refresh()
}

def refresh(YaleAlarmState) {
	def token = login(token)
	def getPanelMetaDataAndFullStatus = [
		uri: "https://www.yalehomesystem.co.uk/homeportal/api/panel/get_panel_mode",
		body: [id:settings.userName , password: settings.password],
		headers: ['Cookie' : "${token}"]
		]
	try {
    	httpPost(getPanelMetaDataAndFullStatus) {	response -> 
    		YaleAlarmState = response.data.message
		log.debug "'$device' REFRESH - response = '$response.data.message' & $response"
        }
    }
    catch (e) {
    	log.error " refersh response $e or $response"
    }
	if (YaleAlarmState.mode.contains("arm")) {
        state.mode = 'Armed-Away'
	} 
	else if (YaleAlarmState.mode.contains("home")) {
        state.mode = 'Armed-Stay'
	}
	else if (YaleAlarmState.mode.contains("disarm")) {
    	state.mode = 'Disarmed'
  	}
  	else { //if (YaleAlarmState.mode.contains("system.permission_denied")) {
  		log.warn "system off line / Error, response= '$YaleAlarmState'"
  		sendEvent(name: "status", value: "offline", displayed: false)
        state.mode = YaleAlarmState
        runIn(30,refresh)
 	}
  	logout(token)
    log.info "'$device' REFRESH - Mode is '$state.mode', Response- '$YaleAlarmState' complete"
	sendEvent(name: "mode", value: state.mode, displayed: true, descriptionText: "Refresh - mode is '$state.mode', response '$YaleAlarmState'")
  	sendEvent(name: "refresh", value: YaleAlarmState, displayed: true, isStateChange:true)
    sendEvent(name: "status", value: "online", displayed: false)
    runEvery3Hours(refresh)
    return YaleAlarmState
}

// ===================  Arm Function. Performs arming function ====================
def armAway() {
	def reply = ''
	def token = login(token)
	def paramsArm = [
		uri: "https://www.yalehomesystem.co.uk/homeportal/api/panel/set_panel_mode?area=1&mode=arm",
		body: [id:settings.userName , password: settings.password],
		headers: ['Cookie' : "${token}"]
	]
        httpPost(paramsArm) {	response -> // Arming Function in away mode
        	reply = response.data.message
            log.debug "AA - response = '$reply'"
		}
        if (reply != 'OK'){
        	log.warn "$device - AA - Status '$reply'"
            state.mode = reply
            runIn(60,refresh)
        }
        else {
        	state.mode = 'Armed-Away'
        }
    logout(token)
    log.info "AA- Status is: '$reply' - mode '$state.mode', "
    sendEvent(name: "mode", value: state.mode, displayed: true, descriptionText: "Arm(Away) System - '$reply', mode - '$state.mode'")
}

def armStay() {
	def reply = ''
	def token = login(token)
	def paramsArm = [
		uri: "https://www.yalehomesystem.co.uk/homeportal/api/panel/set_panel_mode?area=1&mode=home",
		body: [id:settings.userName , password: settings.password],
		headers: ['Cookie' : "${token}"]
	]
    httpPost(paramsArm) {	response -> // Arming Function in away mode
		reply = response.data.message
        log.debug "AS - response '$response.data.message'"
	}
        if (reply != 'OK'){
        	log.warn "AS - response '$reply'"
            state.mode = reply
            
            runIn(60,refresh)
        }
        else {
        	state.mode = 'Armed-Stay'
        }
    logout(token)
    log.info "'$device' AS - Status is: '$reply' - mode '$state.mode'"
	sendEvent(name: "mode", value: state.mode, displayed: true, descriptionText: "Arm(Home) System - '$reply', mode - '$state.mode'") //state: state.mode, 
}

def disarm() {
	def reply = ''
	def token = login(token)
	def paramsDisarm = [
		uri: "https://www.yalehomesystem.co.uk/homeportal/api/panel/set_panel_mode?area=1&mode=disarm",
		body: [id:settings.userName , password: settings.password],
		headers: ['Cookie' : "${token}"]
	]
	httpPost(paramsDisarm) {	response -> 
        reply = response.data.message
// log.debug "$device - DA - response '$response.data.message'"
	}
	if (reply != 'OK'){
        	log.warn "$device - DA - response '$reply'"
            state.mode = reply
            runIn(10,refresh)
        }
    else {
    	state.mode = 'Disarmed'
    }
    logout(token)
    log.info "$device DA - Status is: '$reply' -'$state.mode'"
	sendEvent(name: "mode", value: state.mode, displayed: true, descriptionText: "Disarm - '$reply', mode - '$state.mode'")
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