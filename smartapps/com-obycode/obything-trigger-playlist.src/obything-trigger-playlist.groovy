/**
*  obything Trigger Playlist
*
*  Author: obycode
*  Date: 2015-11-05
*/

import groovy.json.JsonSlurper

definition(
  name: "obything Trigger Playlist",
  namespace: "com.obycode",
  author: "obycode, based on Sonos Mood Music by SmartThings",
  description: "Plays a selected playlist on your Mac with obything",
  category: "Convenience",
  iconUrl: "http://obything.obycode.com/icons/obything-device.png",
  iconX2Url: "http://obything.obycode.com/icons/obything-device.png"
)

preferences {
  page(name: "mainPage", title: "Choose the trigger(s)", nextPage: "chooseTrackAndSpeakers", uninstall: true)
  page(name: "chooseTrackAndSpeakers", title: "Choose the playlist and speaker(s)", install: true, uninstall: true)
  page(name: "timeIntervalInput", title: "Only during a certain time") {
    section {
      input "starting", "time", title: "Starting", required: false
      input "ending", "time", title: "Ending", required: false
    }
  }
}

def mainPage() {
  dynamicPage(name: "mainPage") {
    def anythingSet = anythingSet()
    if (anythingSet) {
      section("Play music when..."){
        ifSet "motion", "capability.motionSensor", title: "Motion Here", required: false, multiple: true
        ifSet "contact", "capability.contactSensor", title: "Contact Opens", required: false, multiple: true
        ifSet "contactClosed", "capability.contactSensor", title: "Contact Closes", required: false, multiple: true
        ifSet "acceleration", "capability.accelerationSensor", title: "Acceleration Detected", required: false, multiple: true
        ifSet "mySwitch", "capability.switch", title: "Switch Turned On", required: false, multiple: true
        ifSet "mySwitchOff", "capability.switch", title: "Switch Turned Off", required: false, multiple: true
        ifSet "arrivalPresence", "capability.presenceSensor", title: "Arrival Of", required: false, multiple: true
        ifSet "departurePresence", "capability.presenceSensor", title: "Departure Of", required: false, multiple: true
        ifSet "smoke", "capability.smokeDetector", title: "Smoke Detected", required: false, multiple: true
        ifSet "water", "capability.waterSensor", title: "Water Sensor Wet", required: false, multiple: true
        ifSet "button1", "capability.button", title: "Button Press", required:false, multiple:true
        ifSet "triggerModes", "mode", title: "System Changes Mode", required: false, multiple: true
        ifSet "timeOfDay", "time", title: "At a Scheduled Time", required: false
      }
    }

    def hideable = anythingSet || app.installationState == "COMPLETE"
    def sectionTitle = anythingSet ? "Select additional triggers" : "Play music when..."

    section(sectionTitle, hideable: hideable, hidden: true){
      ifUnset "motion", "capability.motionSensor", title: "Motion Here", required: false, multiple: true
      ifUnset "contact", "capability.contactSensor", title: "Contact Opens", required: false, multiple: true
      ifUnset "contactClosed", "capability.contactSensor", title: "Contact Closes", required: false, multiple: true
      ifUnset "acceleration", "capability.accelerationSensor", title: "Acceleration Detected", required: false, multiple: true
      ifUnset "mySwitch", "capability.switch", title: "Switch Turned On", required: false, multiple: true
      ifUnset "mySwitchOff", "capability.switch", title: "Switch Turned Off", required: false, multiple: true
      ifUnset "arrivalPresence", "capability.presenceSensor", title: "Arrival Of", required: false, multiple: true
      ifUnset "departurePresence", "capability.presenceSensor", title: "Departure Of", required: false, multiple: true
      ifUnset "smoke", "capability.smokeDetector", title: "Smoke Detected", required: false, multiple: true
      ifUnset "water", "capability.waterSensor", title: "Water Sensor Wet", required: false, multiple: true
      ifUnset "button1", "capability.button", title: "Button Press", required:false, multiple:true
      ifUnset "triggerModes", "mode", title: "System Changes Mode", required: false, multiple: true
      ifUnset "timeOfDay", "time", title: "At a Scheduled Time", required: false
    }
    section {
      input "obything", "capability.musicPlayer", title: "On this obything music player", required: true
    }
    section("More options", hideable: true, hidden: true) {
      input "volume", "number", title: "Set the volume", description: "0-100%", required: false
      input "frequency", "decimal", title: "Minimum time between actions (defaults to every event)", description: "Minutes", required: false
      href "timeIntervalInput", title: "Only during a certain time", description: timeLabel ?: "Tap to set", state: timeLabel ? "complete" : "incomplete"
      input "days", "enum", title: "Only on certain days of the week", multiple: true, required: false,
      options: ["Sunday","Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"]
      if (settings.modes) {
        input "modes", "mode", title: "Only when mode is", multiple: true, required: false
      }
      input "oncePerDay", "bool", title: "Only once per day", required: false, defaultValue: false
    }
  }
}

def chooseTrackAndSpeakers() {
  dynamicPage(name: "chooseTrackAndSpeakers") {
    section {
      input "playlist", "enum", title:"Play this playlist", required:true, multiple: false, options: playlistOptions()
      input "speakers", "enum", title:"On these speakers", required:false, multiple: true, options: speakerOptions()
    }
    section([mobileOnly:true]) {
      label title: "Assign a name", required: false
      mode title: "Set for specific mode(s)", required: false
    }
  }
}

private playlistOptions() {
  def playlistString = obything.currentValue("playlists")
  log.debug "Playlists are $playlistString"
  def jsonList = new JsonSlurper().parseText(playlistString)
  log.debug("jsonList is $jsonList")
  jsonList.collect {
    it.name
  }
}

def chooseSpeakers() {
  dynamicPage(name: "chooseSpeakers") {
    section{
      input "speakers", "enum", title:"Play on these speakers", required:false, multiple: true, options: speakerOptions()
    }
    section([mobileOnly:true]) {
      label title: "Assign a name", required: false
      mode title: "Set for specific mode(s)", required: false
    }
  }
}

private speakerOptions() {
  def speakersString = obything.currentValue("speakers")
  log.debug "Speakers are $speakersString"
  def slurper = new JsonSlurper()
  slurper.parseText(speakersString)
}

private anythingSet() {
  for (name in ["motion","contact","contactClosed","acceleration","mySwitch","mySwitchOff","arrivalPresence","departurePresence","smoke","water","button1","timeOfDay","triggerModes","timeOfDay"]) {
    if (settings[name]) {
      return true
    }
  }
  return false
}

private ifUnset(Map options, String name, String capability) {
  if (!settings[name]) {
    input(options, name, capability)
  }
}

private ifSet(Map options, String name, String capability) {
  if (settings[name]) {
    input(options, name, capability)
  }
}

def installed() {
  log.debug "Installed with settings: ${settings}"
  subscribeToEvents()
}

def updated() {
  log.debug "Updated with settings: ${settings}"
  unsubscribe()
  unschedule()
  subscribeToEvents()
}

def subscribeToEvents() {
  log.trace "subscribeToEvents()"

  subscribe(app, appTouchHandler)
  subscribe(contact, "contact.open", eventHandler)
  subscribe(contactClosed, "contact.closed", eventHandler)
  subscribe(acceleration, "acceleration.active", eventHandler)
  subscribe(motion, "motion.active", eventHandler)
  subscribe(mySwitch, "switch.on", eventHandler)
  subscribe(mySwitchOff, "switch.off", eventHandler)
  subscribe(arrivalPresence, "presence.present", eventHandler)
  subscribe(departurePresence, "presence.not present", eventHandler)
  subscribe(smoke, "smoke.detected", eventHandler)
  subscribe(smoke, "smoke.tested", eventHandler)
  subscribe(smoke, "carbonMonoxide.detected", eventHandler)
  subscribe(water, "water.wet", eventHandler)
  subscribe(button1, "button.pushed", eventHandler)

  if (triggerModes) {
    subscribe(location, modeChangeHandler)
  }

  if (timeOfDay) {
    runDaily(timeOfDay, scheduledTimeHandler)
  }
}

def eventHandler(evt) {
  log.debug "In eventHandler"
  if (allOk) {
    if (frequency) {
      def lastTime = state[frequencyKey(evt)]
      if (lastTime == null || now() - lastTime >= frequency * 60000) {
        takeAction(evt)
      }
    }
    else {
      takeAction(evt)
    }
  }
}

def modeChangeHandler(evt) {
  log.trace "modeChangeHandler $evt.name: $evt.value ($triggerModes)"
  if (evt.value in triggerModes) {
    eventHandler(evt)
  }
}

def scheduledTimeHandler() {
  eventHandler(null)
}

def appTouchHandler(evt) {
  takeAction(evt)
}

private takeAction(evt) {
  log.info "Playing '$playlist'"
  def speakerString
  if (speakers) {
    speakerString = ""
    speakers.each {
      speakerString += "\"$it\","
    }
    // remove the last comma and encode
    speakerString = encode(speakerString[0..-2])
  }
  obything.playPlaylist(encode(playlist), speakerString, volume)

  if (frequency || oncePerDay) {
    state[frequencyKey(evt)] = now()
  }
}

private frequencyKey(evt) {
  "lastActionTimeStamp"
}

private encode(text) {
  return URLEncoder.encode(text).replaceAll("\\+", "%20")
}

private dayString(Date date) {
  def df = new java.text.SimpleDateFormat("yyyy-MM-dd")
  if (location.timeZone) {
    df.setTimeZone(location.timeZone)
  }
  else {
    df.setTimeZone(TimeZone.getTimeZone("America/New_York"))
  }
  df.format(date)
}

private oncePerDayOk(Long lastTime) {
  def result = lastTime ? dayString(new Date()) != dayString(new Date(lastTime)) : true
  log.trace "oncePerDayOk = $result"
  result
}

// TODO - centralize somehow
private getAllOk() {
  modeOk && daysOk && timeOk
}

private getModeOk() {
  def result = !modes || modes.contains(location.mode)
  log.trace "modeOk = $result"
  result
}

private getDaysOk() {
  def result = true
  if (days) {
    def df = new java.text.SimpleDateFormat("EEEE")
    if (location.timeZone) {
      df.setTimeZone(location.timeZone)
    }
    else {
      df.setTimeZone(TimeZone.getTimeZone("America/New_York"))
    }
    def day = df.format(new Date())
    result = days.contains(day)
  }
  log.trace "daysOk = $result"
  result
}

private getTimeOk() {
  def result = true
  if (starting && ending) {
    def currTime = now()
    def start = timeToday(starting).time
    def stop = timeToday(ending).time
    result = start < stop ? currTime >= start && currTime <= stop : currTime <= stop || currTime >= start
  }
  log.trace "timeOk = $result"
  result
}

private hhmm(time, fmt = "h:mm a")
{
  def t = timeToday(time, location.timeZone)
  def f = new java.text.SimpleDateFormat(fmt)
  f.setTimeZone(location.timeZone ?: timeZone(time))
  f.format(t)
}

private timeIntervalLabel()
{
  (starting && ending) ? hhmm(starting) + "-" + hhmm(ending, "h:mm a z") : ""
}
