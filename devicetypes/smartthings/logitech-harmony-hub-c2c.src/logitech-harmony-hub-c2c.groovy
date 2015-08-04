/**
 *  Logitech Harmony Hub
 *
 *  Author: SmartThings
 */
metadata {
	definition (name: "Logitech Harmony Hub C2C", namespace: "smartthings", author: "SmartThings") {
		capability "Media Controller"
        capability "Refresh"
        
        command "activityoff"   
        command "alloff" 
        command "refresh"          
	}

	simulator {
	}

	tiles {
		standardTile("icon", "icon", width: 1, height: 1, canChangeIcon: false, inactiveLabel: true, canChangeBackground: false) {
			state "default", label: "Harmony", action: "", icon: "st.harmony.harmony-hub-icon", backgroundColor: "#FFFFFF"
		}
		valueTile("currentActivity", "device.currentActivity", decoration: "flat", height: 1, width: 3, inactiveLabel: false) {
			state "default", label:'${currentValue}'
		}
		standardTile("huboff", "device.switch", inactiveLabel: false, decoration: "flat") {
			state "default", label:'End Activity', action:"activityoff", icon:"st.harmony.harmony-hub-icon"
		}   
		standardTile("alloff", "device.switch", inactiveLabel: false, decoration: "flat") {
			state "default", label:'All Activities', action:"alloff", icon:"st.secondary.off"
		}            
		standardTile("refresh", "device.power", inactiveLabel: false, decoration: "flat") {
			state "default", label:'', action:"refresh.refresh", icon:"st.secondary.refresh"
		}

		main (["icon"])
		details(["currentActivity", "huboff", "refresh"])
	}
}

def startActivity(String activityId) {
	log.debug "Executing 'Start Activity'"
	log.trace parent.activity("$device.deviceNetworkId-$activityId","start")    
}

def activityoff() {
	log.debug "Executing 'Activity Off'"
    log.trace parent.activity(device.deviceNetworkId,"hub")    
}

def alloff() {
	log.debug "Executing 'All Off'"
    log.trace parent.activity("all","end")    
}

def poll() {
	log.debug "Executing 'Poll'"
	log.trace parent.poll()
}

def refresh() {
	log.debug "Executing 'Refresh'"
	log.trace parent.poll()
}
