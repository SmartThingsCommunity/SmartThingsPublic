/**
*  obything Notify with Sound
*
*  Author: obycode
*  Date: 2015-08-30
*/

import groovy.json.JsonSlurper

definition(
  name: "obything Notify with Sound",
  namespace: "com.obycode",
  author: "obycode, based on 'Sonos Notify with Sound' by SmartThings",
  description: "Play a sound or custom message through your Mac with obything when the mode changes or other events occur.",
  category: "Convenience",
  iconUrl: "http://obything.obycode.com/icons/obything-device.png",
  iconX2Url: "http://obything.obycode.com/icons/obything-device.png",
  parent: "com.obycode:obything Connect"
)

preferences {
  page(name: "mainPage", title: "Play a message over your speakers when something happens", install: true, uninstall: true)
  page(name: "chooseTrack", title: "Select a playlist")
  page(name: "chooseSpeakers", title: "Select speakers")
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
      section("Play message when"){
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
        ifSet "button1", "capability.button", title: "Button Press", required:false, multiple:true //remove from production
        ifSet "triggerModes", "mode", title: "System Changes Mode", required: false, multiple: true
        ifSet "timeOfDay", "time", title: "At a Scheduled Time", required: false
      }
    }
    def hideable = anythingSet || app.installationState == "COMPLETE"
    def sectionTitle = anythingSet ? "Select additional triggers" : "Play message when..."

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
      ifUnset "button1", "capability.button", title: "Button Press", required:false, multiple:true //remove from production
      ifUnset "triggerModes", "mode", title: "System Changes Mode", description: "Select mode(s)", required: false, multiple: true
      ifUnset "timeOfDay", "time", title: "At a Scheduled Time", required: false
    }
    section{
      input "actionType", "enum", title: "Action?", required: true, defaultValue: "Custom Message", options: [
      "Custom Message",
      "Custom URL",
      "Bell 1",
      "Bell 2",
      "Dogs Barking",
      "Fire Alarm",
      "The mail has arrived",
      "A door opened",
      "There is motion",
      "Smartthings detected a flood",
      "Smartthings detected smoke",
      "Someone is arriving",
      "Piano",
      "Lightsaber"]
      input "message","text",title:"Play this message", required:false, multiple: false
      input "url","text",title:"Play a sound at this URL", required:false, multiple: false
    }
    section {
      input "obything", "capability.musicPlayer", title: "On this obything iTunes device", required: true
    }
    section {
      href "chooseSpeakers", title: "With these speakers", description: speakers ? speakers : "Tap to set", state: speakers ? "complete" : "incomplete"
    }
    section("More options", hideable: true, hidden: true) {
      input "resumePlaying", "bool", title: "Resume currently playing music after notification", required: false, defaultValue: true
      href "chooseTrack", title: "Or play this music or radio station", description: playlist ? playlist : "Tap to set", state: playlist ? "complete" : "incomplete"

      input "volume", "number", title: "Temporarily change volume", description: "0-100%", required: false
      input "frequency", "decimal", title: "Minimum time between actions (defaults to every event)", description: "Minutes", required: false
      href "timeIntervalInput", title: "Only during a certain time", description: timeLabel ?: "Tap to set", state: timeLabel ? "complete" : "incomplete"
      input "days", "enum", title: "Only on certain days of the week", multiple: true, required: false,
      options: ["Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"]
      if (settings.modes) {
        input "modes", "mode", title: "Only when mode is", multiple: true, required: false
      }
      input "oncePerDay", "bool", title: "Only once per day", required: false, defaultValue: false
    }
    section([mobileOnly:true]) {
      label title: "Assign a name", required: false
      mode title: "Set for specific mode(s)", required: false
    }
  }
}

def chooseTrack() {
  dynamicPage(name: "chooseTrack") {
    section{
      input "playlist", "enum", title:"Play this playlist", required:true, multiple: false, options: playlistOptions()
    }
  }
}

private playlistOptions() {
  def playlistString = obything.currentValue("playlists")
  log.debug "Playlists are $playlistString"
  def jsonList = new JsonSlurper().parseText(playlistString)
  jsonList.collect {
    it.Name
  }
}

def chooseSpeakers() {
  dynamicPage(name: "chooseSpeakers") {
    section{
      input "speakers", "enum", title:"Play on these speakers", required:false, multiple: true, options: speakerOptions()
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

  loadText()
}

def eventHandler(evt) {
  log.trace "eventHandler($evt?.name: $evt?.value)"
  if (allOk) {
    log.trace "allOk"
    def lastTime = state[frequencyKey(evt)]
    if (oncePerDayOk(lastTime)) {
      if (frequency) {
        if (lastTime == null || now() - lastTime >= frequency * 60000) {
          takeAction(evt)
        }
        else {
          log.debug "Not taking action because $frequency minutes have not elapsed since last action"
        }
      }
      else {
        takeAction(evt)
      }
    }
    else {
      log.debug "Not taking action because it was already taken today"
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
  log.trace "takeAction()"

  def speakerString
  if (speakers) {
    speakerString = ""
    speakers.each {
      speakerString += "\"$it\","
    }
    // remove the last comma and encode
    speakerString = encode(speakerString[0..-2])
  }

  if (playlist) {
    obything.playTrack(state.sound.uri, speakerString, volume, resumePlaying, playlist)
  }
  else {
    obything.playTrack(state.sound.uri, speakerString, volume, resumePlaying)
  }

  if (frequency || oncePerDay) {
    state[frequencyKey(evt)] = now()
  }
  log.trace "Exiting takeAction()"
}

private frequencyKey(evt) {
  "lastActionTimeStamp"
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
  def result = true
  if (oncePerDay) {
    result = lastTime ? dayString(new Date()) != dayString(new Date(lastTime)) : true
    log.trace "oncePerDayOk = $result"
  }
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

private getTimeLabel()
{
  (starting && ending) ? hhmm(starting) + "-" + hhmm(ending, "h:mm a z") : ""
}
// TODO - End Centralize

private loadText() {
  switch ( actionType) {
    case "Bell 1":
    state.sound = [uri: "http://s3.amazonaws.com/smartapp-media/sonos/bell1.mp3", duration: "10"]
    break;
    case "Bell 2":
    state.sound = [uri: "http://s3.amazonaws.com/smartapp-media/sonos/bell2.mp3", duration: "10"]
    break;
    case "Dogs Barking":
    state.sound = [uri: "http://s3.amazonaws.com/smartapp-media/sonos/dogs.mp3", duration: "10"]
    break;
    case "Fire Alarm":
    state.sound = [uri: "http://s3.amazonaws.com/smartapp-media/sonos/alarm.mp3", duration: "17"]
    break;
    case "The mail has arrived":
    state.sound = [uri: "http://s3.amazonaws.com/smartapp-media/sonos/the+mail+has+arrived.mp3", duration: "1"]
    break;
    case "A door opened":
    state.sound = [uri: "http://s3.amazonaws.com/smartapp-media/sonos/a+door+opened.mp3", duration: "1"]
    break;
    case "There is motion":
    state.sound = [uri: "http://s3.amazonaws.com/smartapp-media/sonos/there+is+motion.mp3", duration: "1"]
    break;
    case "Smartthings detected a flood":
    state.sound = [uri: "http://s3.amazonaws.com/smartapp-media/sonos/smartthings+detected+a+flood.mp3", duration: "2"]
    break;
    case "Smartthings detected smoke":
    state.sound = [uri: "http://s3.amazonaws.com/smartapp-media/sonos/smartthings+detected+smoke.mp3", duration: "1"]
    break;
    case "Someone is arriving":
    state.sound = [uri: "http://s3.amazonaws.com/smartapp-media/sonos/someone+is+arriving.mp3", duration: "1"]
    break;
    case "Piano":
    state.sound = [uri: "http://s3.amazonaws.com/smartapp-media/sonos/piano2.mp3", duration: "10"]
    break;
    case "Lightsaber":
    state.sound = [uri: "http://s3.amazonaws.com/smartapp-media/sonos/lightsaber.mp3", duration: "10"]
    break;
    case "Custom Message":
    if (message) {
      log.debug "message is $message"
      state.sound = textToSpeech(message) // instanceof List ? message[0] : message) // not sure why this is (sometimes) needed)
    }
    else {
      state.sound = textToSpeech("You selected the custom message option but did not enter a message in the $app.label Smart App")
    }
    break;
    case "Custom URL":
    if (url) {
      state.sound = [uri: url, duration: "0"]
    }
    else {
      state.sound = textToSpeech("You selected the custom URL option but did not enter a URL in the $app.label Smart App")
    }
    break;
    default:
    log.debug "Invalid selection."
    break;
  }
}
