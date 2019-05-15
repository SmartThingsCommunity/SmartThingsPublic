/**
*  Version 2.0 - created to run with smart app
*	Version 0.1 -  
*/
preferences {
}

metadata {
	definition (name: "Yale Alarm Open Close Sensor", namespace: "smartthings", author: "Mark-C-UK") {
		capability "Contact Sensor"
		capability "Sensor"
		capability "Polling"
    	capability "Health Check"
        capability "Battery" 
		command "datain"
}

tiles {
	standardTile("contact", "device.contact", width: 2, height: 2) {
		
	
		state "open", label:'${name}', icon:"st.contact.contact.open", backgroundColor:"#ffa81e"
        state "closed", label: '${name}', icon: "st.contact.contact.closed", backgroundColor: "#79b821"
		state "Failed", label: '${name}', icon: "st.contact.contact.open", backgroundColor: "#e5e500"
	}
    valueTile("battery", "device.battery", inactiveLabel: false, decoration: "flat") {
        state "battery", label:'${currentValue}% battery', unit:""
    }
	main "contact"
	details ("contact", "battery")
	}
}    

def datain(data){
	//log.debug "data is $data"
	if (data.status_open[0] == 'device_status.dc_close'){
    	sendEvent(name: "contact", value: "closed")
    }
    else if (data.status_open[0] == 'device_status.dc_open'){
    	sendEvent(name: "contact", value: "open")
    }
    else {
    	sendEvent(name: "contact", value: "Failed", descriptionText: "Device is '${data.status_open[0]}'")
    	log.warn "Datain failed - $data"
    }
    
    if (data.status_fault[0] == 'device_status.low_battery'){
    	sendEvent(name:"battery", value:"1", descriptionText: "Low battery warning not 1%")
    }
    else {sendEvent(name:"battery", value:"100", descriptionText: "Battery OK not 100%")}
}
def updated() {
	log.warn " updated"
    //unsubscribe()
	//subscribe(yaleref, "refresh", subHandle)
}
def subHandle() {
	log.warn " im dooing someting"
}

def poll(){
	log.debug "poll"
}
def ping() {
	log.debug "ping"
}

def refresh() {
	log.debug "refresh"
}
// parse events into attributes
def parse(String description) {
	log.debug "Parsing '${description}'"
}