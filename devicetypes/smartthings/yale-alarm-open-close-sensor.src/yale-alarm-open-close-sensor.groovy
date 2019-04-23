/**
*  Version 0.1 -  
*/
preferences {
	input("userName", "text", title: "Username", description: "Your username for Yale Home System")
	input("password", "password", title: "Password", description: "Your Password for Yale Home System")
	input("zonenumber", type: "number", title: "Row Number - Put 0 in here and open a window/door, refresh and see if the status changes", description: "Sensor ID")
}

metadata {
	definition (name: "Yale Alarm Open Close Sensor", namespace: "smartthings", author: "Tapion1ives") {
		capability "Contact Sensor"
		capability "Sensor"
		capability "Refresh"
		capability "Polling"
    	capability "Health Check"

		attribute "status", "string"
}

// UI tile definitions
tiles {
	standardTile("contact", "device.contact", width: 2, height: 2) {
		
	
		state "open", label:'${name}', icon:"st.contact.contact.open", backgroundColor:"#ffa81e"
        state "closed", label: '${name}', icon: "st.contact.contact.closed", backgroundColor: "#79b821"
		state "Failed", label: '${name}', icon: "st.contact.contact.open", backgroundColor: "#e5e500"
	}
	standardTile("refresh", "device.status", inactiveLabel: false, decoration: "flat") {
		state "default", label:'', action:"refresh.refresh", icon:"st.secondary.refresh"

	}
	main "contact"
	details ("contact", "refresh", "bypass")
	}
    
    preferences {
   //     input "yaleref", "capability.refresh"
		input "devid", "number", title: "device id number find from logging", required: false
   }
//}
}

// Zone status Information is below
// '' – Closed
// device_status.dc_open – Open

def updated() {
	log.warn " updated"
    unsubscribe()
	//subscribe(yaleref, "refresh", subHandle)
}
def subHandle() {
	log.warn " im dooing someting"
}

// Login Function. Returns SessionID for rest of the functions
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


// Logout Function. Called after every mutational command. Ensures the current user is always logged Out.
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

 // Gets Panel Metadata. Takes token & location ID as an argument
Map panelMetaData(token) {

def tczones

def getPanelMetaDataAndFullStatus = [
	uri: "https://www.yalehomesystem.co.uk/homeportal/api/panel/get_devices/",
	body: [id:settings.userName , password: settings.password],
	headers: ['Cookie' : "${token}"]
	]

httpPost(getPanelMetaDataAndFullStatus) {	response -> 
    tczones = response.data.message
    //log.debug "$response.data.message"
	log.debug "name - ${response.data.message?.name}"
    log.debug "status1 - ${response.data.message?.status1}"
    log.debug "status2 - ${response.data.message?.status2}"
    log.debug "type - ${response.data.message?.type}"
    //log.debug "all - ${response.data.message}"
    log.debug "backdoor ${response.data.message['back door'].status1}"
    log.debug "0 name ${response.data.message[0].name}, ${response.data.message[0].status1}, ${response.data.message[0].status2}"
    log.debug "1 name ${response.data.message[1].name}, ${response.data.message[1].status1}, ${response.data.message[1].status2}"
    log.debug "2 name ${response.data.message[2].name}, ${response.data.message[2].status1}, ${response.data.message[2].status2}"
    log.debug "5 name ${response.data.message[5].name}, ${response.data.message[5].status1}, ${response.data.message[5].status2}"
}
return [tczones: tczones]
} //Should return Sesor and description Information

def poll(){
	refresh()
}
def ping() {
	log.debug "ping"
	refresh()
}

def refresh() {
	//if (settings.devid == null){
    
    def token = login(token)
	def zname = device.name
	def zonenumber = settings.zonenumber as int
	def metaData = panelMetaData(token) // Gets Information
	log.debug "Doing zone refresh"
	if (metaData.tczones.contains("system.permission_denied")) {
		log.debug "Zone ${metaData.tczones} is Fault"
    	sendEvent(name: "contact", value:"Failed", displayed: "true", descriptionText: "Refresh: Zone is Faulted , Zone  ${zname} faulted")
	} 
	else if (metaData.tczones[zonenumber].status1.contains('device_status.dc_open')) {
		log.debug "'${metaData.tczones[zonenumber].name}', Zone '${zonenumber}', '${metaData.tczones[zonenumber].status1}' is OPEN"
    	sendEvent(name: "contact", value:"open", displayed: "true", descriptionText: "'${metaData.tczones[zonenumber].name}' is Open, '${metaData.tczones[zonenumber].status1}' - '${zonenumber}'")
	}
 	else if (metaData.tczones[zonenumber].status1.contains('')) {
		log.debug "${metaData.tczones[zonenumber].name},  Zone '${zonenumber}', '${metaData.tczones[zonenumber].status1}' is OK"
   	sendEvent(name: "contact", value:"closed", displayed: "true", descriptionText: "'${metaData.tczones[zonenumber].name}' is closed,'${metaData.tczones[zonenumber].status1}'- ${zonenumber}")
	}  
	logout(token)
	sendEvent(name: "refresh", value: "true", displayed: "true", description: "Refresh Successful") 
}

// parse events into attributes
def parse(String description) {
	log.debug "Parsing '${description}'"
}