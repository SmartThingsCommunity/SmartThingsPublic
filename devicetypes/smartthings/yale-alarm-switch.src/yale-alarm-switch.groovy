/**
*  Version 0.1 - First version arms/home/disarm the alarm
*/
preferences {

input("userName", "text", title: "Username", description: "Your username for Yale Home System")
input("password", "password", title: "Password", description: "Your Password for Yale Home System")


}
metadata {
definition (name: "YALE ALARM SWITCH", namespace: "smartthings", author: "Tapion1ives") {

capability "Refresh"
capability "Switch"
attribute "status", "string"
capability "lock"
}


// UI tile definitions
tiles {
	standardTile("toggle", "device.status", width: 2, height: 2) {
		state("unknown", label:'${name}', action:"device.refresh", icon:"st.Office.office9", backgroundColor:"#ffa81e")
		state("Armed Stay", label:'${name}', action:"switch.off", icon:"st.Home.home4", backgroundColor:"#79b821", nextState:"Disarmed")
		state("Disarmed", label:'${name}', action:"lock.lock", icon:"st.Home.home2", backgroundColor:"#a8a8a8", nextState:"Armed Away")
		state("Armed Away", label:'${name}', action:"switch.off", icon:"st.Home.home3", backgroundColor:"#79b821", nextState:"Disarmed")
        state("Arming", label:'${name}', icon:"st.Home.home4", backgroundColor:"#ffa81e")
		state("Disarming", label:'${name}', icon:"st.Home.home2", backgroundColor:"#ffa81e")
     	}
	standardTile("statusstay", "device.status", inactiveLabel: false, decoration: "flat") {
		state "default", label:'Arm Stay', action:"switch.on", icon:"st.Home.home4"
	}
	standardTile("statusaway", "device.status", inactiveLabel: false, decoration: "flat") {
		state "default", label:'Arm Away', action:"lock.lock", icon:"st.Home.home3"
	}
	standardTile("statusdisarm", "device.status", inactiveLabel: false, decoration: "flat") {
		state "default", label:'Disarm', action:"switch.off", icon:"st.Home.home2"
	}
	standardTile("refresh", "device.status", inactiveLabel: false, decoration: "flat") {
		state "default", label:'', action:"refresh.refresh", icon:"st.secondary.refresh"
	}

	main "toggle"
	details(["toggle", "statusaway", "statusstay", "statusdisarm", "refresh"])
}
}



 // Login Function. Returns cookie for rest of the functions
 def login(token) {
log.debug "Executed login"
def paramsLogin = [
	uri: "https://www.yalehomesystem.co.uk/homeportal/api/login/check_login/",
	body: [id:settings.userName , password: settings.password]
]
httpPost(paramsLogin) { responseLogin ->
	token = responseLogin.headers?.'Set-Cookie'?.split(";")?.getAt(0)
} 
return token
 } // Returns cookie as token		

 // Logout Function. Called after every mutational command. Ensures the current user is always       logged Out.
def logout(token) {
//log.debug "During logout - ${token}"
def paramsLogout = [
	uri: "https://www.yalehomesystem.co.uk/homeportal/api/logout/",
	headers: ['Cookie' : "${token}"]
]
httpPost(paramsLogout) { responseLogout ->
	log.debug "Smart Things has successfully logged out"
}  
 }

//Should get the panel mode so we know what its current status is

def refresh(YaleAlarmState) {		   
def token = login(token)
def getPanelMetaDataAndFullStatus = [
	uri: "https://www.yalehomesystem.co.uk/homeportal/api/panel/get_panel_mode",
	body: [id:settings.userName , password: settings.password],
	headers: ['Cookie' : "${token}"]
]
httpPost(getPanelMetaDataAndFullStatus) {	response -> 
    YaleAlarmState = response.data.message
    }
 // Gets Information
if (YaleAlarmState.mode.contains("arm")) {
	log.debug "Alarm is ${YaleAlarmState.mode}ed"
    sendEvent(name: "status", value: "Armed Away", displayed: "true", description: "Refresh: Alarm is Armed Away")
} else if (YaleAlarmState.mode.contains("home")) {
	log.debug "Alarm is set to ${YaleAlarmState.mode}"
   sendEvent(name: "status", value: "Armed Stay", displayed: "true", description: "Refresh: Alarm is Armed Stay") 
} else if (YaleAlarmState.mode.contains("disarm")) {
	log.debug "Alarm is ${YaleAlarmState.mode}ed"
	sendEvent(name: "status", value: "Disarmed", displayed: "true", description: "Refresh: Alarm is Disarmed") 
 } 
  logout(token)
sendEvent(name: "refresh", value: "true", displayed: "true", description: "Refresh Successful") 
  return YaleAlarmState
}

// Arm Function. Performs arming function
def armAway() {		   
def token = login(token)
def paramsArm = [
	uri: "https://www.yalehomesystem.co.uk/homeportal/api/panel/set_panel_mode?area=1&mode=arm",
	body: [id:settings.userName , password: settings.password],
	headers: ['Cookie' : "${token}"]
]
//httpPost(paramsArm) // Arming Function in away mode
def metaData = refresh(YaleAlarmState) // Get AlarmCode
  if (metaData.mode.contains("arm")) {
	log.debug "Status is: Already Armed Away"
	sendEvent(name: "status", value: "Armed Away", displayed: "true", description: "Refresh: Alarm is Armed Away") 
} else if (metaData.mode.contains("home")) {
	log.debug "Status is: Armed Stay - Please Disarm First"
	sendEvent(name: "status", value: "Armed Stay", displayed: "true", description: "Refresh: Alarm is Armed Stay") 
} else {
	log.debug "Status is: Arming ${metaData.mode}"
    httpPost(paramsArm) // Arming Function in away mode
}


 }

def armStay() {		   
def token = login(token)
def paramsArm = [
	uri: "https://www.yalehomesystem.co.uk/homeportal/api/panel/set_panel_mode?area=1&mode=home",
	body: [id:settings.userName , password: settings.password],
	headers: ['Cookie' : "${token}"]
]
//httpPost(paramsArm) // Arming function in stay mode
def metaData = refresh(YaleAlarmState) // Gets AlarmCode
  if (metaData.mode.contains("arm")) {
	log.debug "Status is: Already Armed Away"
	sendEvent(name: "status", value: "Armed Away", displayed: "true", description: "Refresh: Alarm is Armed Away") 
} else if (metaData.mode.contains("home")) {
	log.debug "Status is: Armed Stay - Please Disarm First"
	sendEvent(name: "status", value: "Armed Stay", displayed: "true", description: "Refresh: Alarm is Armed Stay") 
} else {
	log.debug "Status is: Arming"
    httpPost(paramsArm) // Arming function in stay mode
  }

   }

 def disarm() {
def token = login(token)
def paramsDisarm = [
	uri: "https://www.yalehomesystem.co.uk/homeportal/api/panel/set_panel_mode?area=1&mode=disarm",
			body: [id:settings.userName , password: settings.password],
	headers: ['Cookie' : "${token}"]
]
//httpPost(paramsDisarm)	
def metaData = refresh(YaleAlarmState) // Gets AlarmCode
if (metaData.mode.contains("disarm")) {
	log.debug "Status is: Already Disarmed"
	sendEvent(name: "status", value: "Disarmed", displayed: "true", description: "Refresh: Alarm is Disarmed") 
} else {
	log.debug "Status is: Disarming"
	httpPost(paramsDisarm)	
} 

}


  // parse events into attributes
 def parse(String description) {
   log.debug "Parsing '${description}'"
 }

// handle commands
def lock() {
log.debug "Executing 'Arm Stay'"
armStay()
sendEvent(name: "lock", value: "lock", displayed: "true", description: "Arming Stay") 
sendEvent(name: "status", value: "Arming", displayed: "true", description: "Updating Status: Arming System")
runIn(10,refresh)
  }

def unlock() {
log.debug "Executing 'Disarm'"
disarm()
sendEvent(name: "unlock", value: "unlock", displayed: "true", description: "Disarming") 
sendEvent(name: "status", value: "Disarming", displayed: "true", description: "Updating Status: Disarming System") 
runIn(10,refresh)
   }

 def on() {
log.debug "Executing 'Arm Away'"
armAway()
sendEvent(name: "switch", value: "on", displayed: "true", description: "Arming Away") 
sendEvent(name: "status", value: "Arming", displayed: "true", description: "Updating Status: Arming System") 
runIn(10,refresh)
  }

  def off() {
log.debug "Executing 'Disarm'"
disarm()
sendEvent(name: "switch", value: "off", displayed: "true", description: "Disarming") 
sendEvent(name: "status", value: "Disarmed", displayed: "true", description: "Updating Status: Disarming System") 
runIn(10,refresh)
 }
