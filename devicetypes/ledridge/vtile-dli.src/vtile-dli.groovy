/**
 *  V Tile device-type for Digital Loggers Inc.
 *
 *	Needed for Multi Switch with Virtual Tiles to create virtual switch tiles in ST for devices that have multiple "switch[x]"
 *     attributes within them and have on[x], off[x], and cycle[x] commands for each.
 *     Also has support for device-label inside the name when on or off and polling occurs
 *
 */
metadata {
	definition (name: "vTile_DLI", namespace: "Ledridge", author: "Ledridge") {
		capability "Switch"
		capability "relaySwitch"
		capability "Polling"
		capability "Refresh"

		attribute "lastEvent",  "string"
        
        command "cycle"
	}
}

preferences {
	tiles {
		standardTile("switch", "device.switch", width: 2, height: 2, canChangeIcon: true, canChangeBackground: true) {
			state "off",		label:'${name}', action: "switch.on",	icon:"st.switches.switch.off",	backgroundColor: "#DDDDff", nextState: "turningOn"
			state "on",			label:'${name}', action: "switch.off",	icon:"st.switches.switch.on",	backgroundColor: "#0088ff", nextState: "turningOff"
			state "turningOff",	label:'${name}', action: "switch.on",	icon:"st.switches.switch.off",	backgroundColor: "#FA5882", nextState: "off"
			state "turningOn",	label:'${name}', action: "switch.on",	icon:"st.switches.switch.on",	backgroundColor: "#F3F781", nextState: "on"
            state "cyclingOff",	label:"Turning Off",	icon:"st.switches.switch.off",	backgroundColor: "#FA5882", nextState: "cyclingOn"
			state "cyclingOn",	label:"Turning On",		icon:"st.switches.switch.on",	backgroundColor: "#F3F781", nextState: "on"
		}
        
        standardTile("Cycle", "device.switch", width: 1, height: 2, canChangeIcon: true) {
			state "default", action: "cycle", icon: "st.secondary.refresh-icon", backgroundColor: "#0088ff"
		}

	    valueTile("lastEvent", "device.lastEvent", inactiveLabel: false, width: 3, height: 1, canChangeIcon: false, decoration:"flat") {
    	state "default", label: 'Last Event: ${currentValue}'}  

		main "switch"
		details(["switch", "Cycle", "lastEvent"])
	}
}

def parse(desc) {
	def results = []
    log.debug desc
    if(desc=="updated") { log.debug "Device $device.label has been UPDATED"; poll() }
}

def on() {
	sendEvent([name: "switch", value: "on"])
    parent.OutletAction(this,"ON")
	sendEvent([name: "lastEvent", value: "${df(now())}"])
  	log.debug "$device.label is On" 
}

def off() {
	sendEvent([name: "switch", value: "off"])
    parent.OutletAction(this,"OFF")
	sendEvent([name: "switch", value: "$device.label"])
	sendEvent([name: "lastEvent", value: "${df(now())}"])
  	log.debug "$device.label is Off" 
}

def cycle() {
	log.debug "$device.label is Cycling"
    parent.OutletAction(this,"CCL")
    
    sendEvent([name: "switch", value: "cyclingOff"])
    pause(6000)
    
    sendEvent([name: "switch", value: "cyclingOn"])
    pause(5000)
    
    sendEvent([name: "switch", value: "on"])
    
	sendEvent([name: "lastEvent", value: "${df(now())}"])
  	//log.debug "$device.label is Off" 
}

def poll() {
	def current = device.currentValue("switch")
    log.debug "Polling - $device.label is $current"
    
    log.debug "This - $this"
    
    def outletStatus = parent.OutletStatus(this)
	log.debug "Polling - Status is $outletStatus"

    def OutletName = parent.OutletName(this)
	log.debug "Polling - Name is $OutletName"
    
	if(!current || current=="off") { sendEvent(name:"switch", value:"$device.label", isStateChange:true, displayed:false) }
}

def pause(millis) {
   def passed = 0
   def now = new Date().time
   log.debug "pausing... at Now: $now"
   /* This loop is an impolite busywait. We need to be given a true sleep() method, please. */
   while ( passed < millis ) {
       passed = new Date().time - now
   }
   log.debug "... DONE pausing."
}

def df(e) {
	//  *  df(e) - Date Format "E"
	//  *     Takes epoch time format and returns Date formatted in current timezone
	def locale = getWeatherFeature("geolookup", zip); 
	def tz = TimeZone.getTimeZone(locale.location.tz_long);
	def formatted
	if(e) { formatted = new Date(e).format("EEE, MMM d, 'at' hh:mm aaa", tz); return formatted }
}