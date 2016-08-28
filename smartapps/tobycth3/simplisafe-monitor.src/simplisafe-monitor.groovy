/**
 *  SimpliSafe Monitor
 *
 *  Author: toby@cth3.com
 *  Date: 2/15/16
 *
 *  Monitors and controlls the state of a SimpliSafe alarm system, syncs with Smart Home Monitor, sets SimpliSafe based on location mode and can turn on/off switchs based on SimpliSafe state.
 *  Works in conjunction with SimpliSafe Alarm Integration device type.
 */


// Automatically generated. Make future change here.
definition(
    name: "SimpliSafe Monitor",
    namespace: "tobycth3",
    author: "toby@cth3.com",
    description: "Monitors and controlls the state of a SimpliSafe alarm system, syncs with Smart Home Monitor, sets SimpliSafe based on location mode and can turn on/off switchs based on SimpliSafe state. Works in conjunction with SimpliSafe Alarm Integration device type.",
    category: "Safety & Security",
    iconUrl: "https://pbs.twimg.com/profile_images/594250179215241217/LOjVA4Yf.jpg",
    iconX2Url: "https://pbs.twimg.com/profile_images/594250179215241217/LOjVA4Yf.jpg")

preferences {
  section("Monitor and control this SimpliSafe alarm system") {
    input "alarmsystem", "capability.alarm", title: "Select alarm system"
  }
  
  section("Set SimpliSafe to Off when mode matches") {
    input "modealarmoff", "mode", title: "Select off mode", multiple: true, required: false  }
  
  section("Set SimpliSafe to Away when mode matches") {
    input "modealarmaway", "mode", title: "Select away mode", multiple: true, required: false  }
  
  section("Set SimpliSafe to Home when mode matches") {
    input "modealarmhome", "mode", title: "Select home mode", multiple: true, required: false  }
  
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
  subscribe(location, "mode", modeaction)
  subscribe(alarmsystem, "alarm", alarmstate)
  subscribe(location, "alarmSystemStatus", shmaction)
  }
  
def modeaction(evt) {
state.locationmode = evt.value

  if(evt.value in modealarmoff && state.alarmstate !="off") {
    log.debug("Location mode: $state.locationmode")
     setalarmoff()
  }
 else {
  if(evt.value in modealarmaway && state.alarmstate !="away") {
    log.debug("Location mode: $state.locationmode")
     setalarmaway()
  }
 else {
  if(evt.value in modealarmhome && state.alarmstate !="home") {
    log.debug("Location mode: $state.locationmode")
     setalarmhome()
  }
  else {
    log.debug("No actions set for location mode ${state.locationmode} or SimpliSafe already set to ${state.alarmstate} - aborting")
    }
   }  
  }
}


def shmaction(evt) {
state.shmstate = evt.value

  if(evt.value == "off" && state.alarmstate !="off") {
    log.debug("Smart Home Monitor state: $state.shmstate")
     setalarmoff()
  }
 else {
  if(evt.value == "away" && state.alarmstate !="away") {
    log.debug("Smart Home Monitor state: $state.shmstate")
     setalarmaway()
  }
 else {
  if(evt.value == "stay" && state.alarmstate !="home") {
    log.debug("Smart Home Monitor state: $state.shmstate")
     setalarmhome()
  }
  else {
    log.debug("No actions set for Smart Home Monitor state ${state.shmstate} or SimpliSafe already set to ${state.alarmstate} - aborting")
    }
   }  
  }
}

def setalarmoff() {
      def message = "${app.label} set SimpliSafe to Off"
      log.info(message)
      send(message)
      alarmsystem.off()
  }
  
def setalarmaway() {
      def message = "${app.label} set SimpliSafe to Away"
      log.info(message)
      send(message)
      alarmsystem.away()
  }
  
def setalarmhome() {
      def message = "${app.label} set SimpliSafe to Home"
      log.info(message)
      send(message)
      alarmsystem.home()
  }

def setshmoff() {
      def message = "${app.label} set Smart Home Monitor to Off"
      log.info(message)
      send(message)
      sendLocationEvent(name: "alarmSystemStatus", value: "off")
  }
  
def setshmaway() {
      def message = "${app.label} set Smart Home Monitor to Away"
      log.info(message)
      send(message)
     sendLocationEvent(name: "alarmSystemStatus", value: "away")
  }
  
def setshmstay() {
      def message = "${app.label} set Smart Home Monitor to Stay"
      log.info(message)
      send(message)
    sendLocationEvent(name: "alarmSystemStatus", value: "stay")
  }

def alarmstate(evt) {
state.alarmstate = evt.value

  if(evt.value == "off" && state.shmstate !="off") {
    log.debug("Smart Home Monitor state: $state.shmstate")
     setshmoff()
  }
 else {
  if(evt.value == "away" && state.shmstate !="away") {
    log.debug("Smart Home Monitor state: $state.shmstate")
     setshmaway()
  }
 else {
  if(evt.value == "home" && state.shmstate !="stay") {
    log.debug("Smart Home Monitor state: $state.shmstate")
     setshmstay()
  }
  else {
    log.debug("No actions set for SimpliSafe state ${state.alarmstate} or Smart Home Monitor already set to ${state.shmstate} - aborting")
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
    log.debug("No actions set for SimpliSafe state ${state.alarmstate} - aborting")
    }
   }  
  }

def alarmstateon() {
    log.debug ("${app.label} set switches to on")
      settings.alarmtile.on()
  }
  
def alarmstateoff() {
    log.debug ("${app.label} set switches to off")
      settings.alarmtile.off()
  } 
  
private send(msg) {
  if(sendPushMessage != "No") {
    log.debug("Sending push message")
    sendPush(msg)
   }
  log.debug(msg)
  }