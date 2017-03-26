/**
 *  SimpliSafe Monitor
 *
 *  Author: toby@cth3.com
 *  Date: 3/5/16
 *
 *  Monitors and controlls the state of a SimpliSafe alarm system, syncs with Smart Home Monitor and can turn on/off switchs based on SimpliSafe state.
 *  Works in conjunction with SimpliSafe Alarm Integration device type.
 */


// Automatically generated. Make future change here.
definition(
    name: "SimpliSafe Monitor",
    namespace: "tobycth3",
    author: "toby@cth3.com",
    description: "Monitors and controlls the state of a SimpliSafe alarm system, syncs with Smart Home Monitor and can turn on/off switchs based on SimpliSafe state. Works in conjunction with SimpliSafe Alarm Integration device type.",
    category: "Safety & Security",
    iconUrl: "https://pbs.twimg.com/profile_images/594250179215241217/LOjVA4Yf.jpg",
    iconX2Url: "https://pbs.twimg.com/profile_images/594250179215241217/LOjVA4Yf.jpg")

preferences {
  section("Monitor and control this SimpliSafe alarm system") {
    input "alarmsystem", "capability.alarm", title: "Select alarm system"
  }
  
  section("Control these switchs") {
	input "alarmtile", "capability.switch", title: "Select switches", multiple: true, required: false  } 
  
  section("Turn on switchs when SimpliSafe state matches") {
    input "alarmon", "enum", title: "Select on state", multiple: true, required: false, metadata:[values:["off", "away", "home"]]
  }
  
  section("Turn off switchs when SimpliSafe state matches") {
    input "alarmoff", "enum", title: "Select off state", multiple: true, required: false, metadata:[values:["off", "away", "home"]]
  }
   
  section("Notifications") {
    input "sendPushMessage", "enum", title: "Send a push notification?", metadata:[values:["Yes","No"]], required: false
   }
  }

def installed() {
  init()
  }

def updated() {
  unsubscribe()
  unschedule()
  init()
  }
  
def init() {
  subscribe(alarmsystem, "alarm", alarmstate)
  subscribe(location, "alarmSystemStatus", shmaction)
  }
  
def updatestate() {
	log.info("Checking SimpliSafe and Smart Home Monitor state")
	state.alarmstate = alarmsystem.currentState("alarm").value.toLowerCase()
	state.shmstate = location.currentState("alarmSystemStatus").value.toLowerCase()
	log.debug("SimpliSafe: '$state.alarmstate', Smart Home Monitor: '$state.shmstate'")
	
}

def shmaction(evt) {
log.info "Smart Home Monitor: $evt.displayName - $evt.value"
state.shmstate = evt.value

  if(shmOff && !alarmOff) {
    log.debug("Smart Home Monitor: '$state.shmstate', SimpliSafe: '$state.alarmstate'")
     setalarmoff()
  }
 else {
  if(shmAway && !alarmAway) {
    log.debug("Smart Home Monitor: '$state.shmstate', SimpliSafe: '$state.alarmstate'")
     setalarmaway()
  }
 else {
  if(shmStay && !alarmHome) {
    log.debug("Smart Home Monitor: '$state.shmstate', SimpliSafe: '$state.alarmstate'")
     setalarmhome()
  }
  else {
    log.debug("No condition match Smart Home Monitor: '$state.shmstate', SimpliSafe: '$state.alarmstate'")
   }  
  }
 }
}

def alarmstate(evt) {
log.info "SimpliSafe Alarm: $evt.displayName - $evt.value"
state.alarmstate = evt.value

  if (alarmOff && !shmOff) {
    log.debug("SimpliSafe: '$state.alarmstate', Smart Home Monitor: '$state.shmstate'")
     setshmoff()
  }
 else {
  if (alarmAway && !shmAway) {
    log.debug("SimpliSafe: '$state.alarmstate', Smart Home Monitor: '$state.shmstate'")
     setshmaway()
  }
 else {
  if (alarmHome && !shmStay) {
    log.debug("SimpliSafe: '$state.alarmstate', Smart Home Monitor: '$state.shmstate'")
     setshmstay()
  }
  else {
    log.debug("No condition match SimpliSafe: '$state.alarmstate', Smart Home Monitor: '$state.shmstate'")
  }
 }
}
  
  if (evt.value in alarmon) {
    log.debug("SimpliSafe state: $state.alarmstate")
     alarmstateon()
  }
 else {
  if (evt.value in alarmoff) {
    log.debug("SimpliSafe state: $state.alarmstate")
     alarmstateoff()
  }
 else {
    log.debug("No switch actions set for SimpliSafe state '${state.alarmstate}'")
    }
   }  
  }

def setalarmoff() {
	updatestate()
      log.debug("SimpliSafe: '$state.alarmstate'")
      if (!alarmOff) {
      def message = "Setting SimpliSafe to Off"
      log.info(message)
      send(message)
      alarmsystem.off()
  }
  else {
	 if (alarmOff) {  
     log.debug("SimpliSafe already set to '$state.alarmstate'")
  }
 }
}
  
def setalarmaway() {
	updatestate()
      log.debug("SimpliSafe: '$state.alarmstate'")
      if (!alarmAway) {
      def message = "Setting SimpliSafe to Away"
      log.info(message)
      send(message)
      alarmsystem.away()
  }
  else {
	 if (alarmAway) {  
     log.debug("SimpliSafe already set to '$state.alarmstate'")
  }
 }
}
  
def setalarmhome() {
	updatestate()
      log.debug("SimpliSafe: '$state.alarmstate'")
      if (!alarmHome) {
      def message = "Setting SimpliSafe to Home"
      log.info(message)
      send(message)
      alarmsystem.home()
  }
  else {
	 if (alarmHome) {  
     log.debug("SimpliSafe already set to '$state.alarmstate'")
  }
 }
}

def setshmoff() {
	updatestate()
      log.debug("Smart Home Monitor: '$state.shmstate'")
      if (!shmOff) {
      def message = "Setting Smart Home Monitor to Off"
      log.info(message)
      send(message)
      sendLocationEvent(name: "alarmSystemStatus", value: "off")
  }
  else {
	 if (shmOff) {  
     log.debug("Smart Home Monitor already set to '$state.shmstate'")
  }
 }
}
  
def setshmaway() {
	updatestate()
      log.debug("Smart Home Monitor: '$state.shmstate'")
      if (!shmAway) {
      def message = "Setting Smart Home Monitor to Away"
      log.info(message)
      send(message)
      sendLocationEvent(name: "alarmSystemStatus", value: "away")
  }
  else {
	 if (shmAway) {  
     log.debug("Smart Home Monitor already set to '$state.shmstate'")
  }
 }
}
  
def setshmstay() {
	updatestate()
      log.debug("Smart Home Monitor: '$state.shmstate'")
      if (!shmStay) {
      def message = "Setting Smart Home Monitor to Stay"
      log.info(message)
      send(message)
      sendLocationEvent(name: "alarmSystemStatus", value: "stay")
  }
  else {
	 if (shmStay) {  
     log.debug("Smart Home Monitor already set to '$state.shmstate'")
  }
 }
}

def alarmstateon() {
    log.debug ("Setting switches to on")
      settings.alarmtile.on()
  }
  
def alarmstateoff() {
    log.debug ("Setting switches to off")
      settings.alarmtile.off()
  } 
  
// TODO - centralize somehow
private getalarmOff() {
	def result = false
	if (state.alarmstate == "off") {
	result = true }
	log.trace "alarmOff = $result"
	result
}

private getalarmAway() {
	def result = false
	if (state.alarmstate == "away") {
	result = true }
	log.trace "alarmAway = $result"
	result
}

private getalarmHome() {
	def result = false
	if (state.alarmstate == "home") {
	result = true }
	log.trace "alarmHome = $result"
	result
}

private getshmOff() {
	def result = false
	if (state.shmstate == "off") {
	result = true }
	log.trace "shmOff = $result"
	result
}

private getshmAway() {
	def result = false
	if (state.shmstate == "away") {
	result = true }
	log.trace "shmAway = $result"
	result
}

private getshmStay() {
	def result = false
	if (state.shmstate == "stay") {
	result = true }
	log.trace "shmStay = $result"
	result
}
  
private send(msg) {
  if(sendPushMessage != "No") {
    log.debug("Sending push message")
    sendPush(msg)
   }
  log.debug(msg)
  }