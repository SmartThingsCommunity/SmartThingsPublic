/**
 *  Knockerz
 *
 *  Auther: paul.knight@delmarvacomputer.com
 *  Date: 6/17/17
 *
 *  Based on the work of brian@bevey.org in 2013.
 *
 *  Notifies when someone knocks on a door, but does not open it.
 *  Alerts are by push, SMS, PushBullet, audio, and/or by
 *  turning on a switch and/or dimming the device.
 */

definition(
    name: "Knockerz",
    namespace: "dca",
    author: "paul.knight@delmarvacomputer.com",
    description: "Alerts when there is a knock at a door.",
    category: "Convenience",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience%402x.png"
)

preferences {
  page name:"pageSetup"
  page name:"pageDoors"
  page name:"pageNotifications"
  page name:"pageAbout"
}

// Show setup page
def pageSetup() {
  LOG("pageSetup()")

  def pageProperties = [
    name:       "pageSetup",
    //title:      "Status",
    nextPage:   null,
    install:    true,
    uninstall:  state.installed
  ]

  return dynamicPage(pageProperties) {
    section("Setup Menu") {
      href "pageDoors", title:"Add/Remove Doors", description:"Tap to open"
      href "pageNotifications", title:"Notification Options", description:"Tap to open"
      href "pageAbout", title:"About Knockerz", description:"Tap to open"
    }
    section([title:"Options", mobileOnly:true]) {
      label title:"Assign a name", required:false
    }
  }
}

// Show "About" page
def pageAbout() {
  LOG("pageAbout()")

  def textAbout =
    "Version ${getVersion()}\n${textCopyright()}\n\n" +
    "You can contribute to the development of this app by making a" +
    "donation to paul.knight@delmarvacomputer.com via PayPal."

  def hrefInfo = [
    url:        "http://delmarvacomputer.github.io/knockerz/",
    style:      "embedded",
    title:      "Tap here for more information...",
    description:"http://delmarvacomputer.github.io/knockerz/",
    required:   false
  ]

  def pageProperties = [
    name:       "pageAbout",
    //title:      "About",
    nextPage:   "pageSetup",
    uninstall:  false
  ]

  return dynamicPage(pageProperties) {
    section("About") {
      paragraph textAbout
      //href hrefInfo
    }
    section("License") {
      paragraph textLicense()
    }
  }
}

// Show "Doors" page
def pageDoors() {
  LOG("pageDoors()")

  def helpAbout =
    "Select acceleration and contact sensors. If " +
    "these don't use the same names, turn indicator " +
    "off. Set delay after knock to see if door opens."
    
  def inputAccelerationSensors = [
    name:           "accelerationSensors",
    title:          "Listen For Knocks At",
    type:           "capability.accelerationSensor",
    multiple:       true,
    required:       true
  ]
  
  def inputContactSensors = [
    name:           "contactSensors",
    title:          "See If These Doors Open",
    type:           "capability.contactSensor",
    multiple:       true,
    required:       true
  ]
  
  def inputUseMultiSensors = [
    name:           "useMultiSensors",
    title:          "Sensors share same names",
    type:           "bool",
    defaultValue:   true,
    required:       false
  ]
  
  def inputKnockDelay = [
    name:           "knockDelay",
    title:          "Knock Delay (default 5s)",
    type:           "number",
    required:       false
  ]
  
  def pageProperties = [
    name:           "pageDoors",
    //title:          "Doors",
    nextPage:       "pageSetup",
    uninstall:      false
  ]

  return dynamicPage(pageProperties) {
    section("Add/Remove Doors") {
      paragraph helpAbout
    }
    section("Select Doors") {
      input inputAccelerationSensors
      input inputContactSensors
      input inputUseMultiSensors
      input inputKnockDelay
    }
  }
}

// Show "Notification Options" page
def pageNotifications() {
  LOG("pageNotifications()")
  
  def helpAbout =
    "How do you want to be notified of a knock at a" +
    "door? Turn on a switch, a chime, or dim a light. " +
    "Send a push or SMS message. Use PushBullet " +
    "or an audio announcement."

  def inputSwitches = [
    name:           "switches",
    type:           "capability.switch",
    title:          "Set these switches",
    multiple:       true,
    required:       false
  ]
  
  def inputDimmerLevel = [
    name:           "dimmerLevel",
    type:           "enum",
    metadata:       [values:["10%","20%","30%","40%","50%","60%","70%","80%","90%","100%"]],
    title:          "Dimmer Level",
    defaultValue:   "40%",
    required:       false
  ]
  
  def inputPushMessage = [
    name:           "pushMessage",
    type:           "bool",
    title:          "Notify on Knock",
    defaultValue:   true
  ]

  def inputPhone1 = [
    name:           "phone1",
    type:           "phone",
    title:          "Send to this number",
    required:       false
  ]

  def inputPhone2 = [
    name:           "phone2",
    type:           "phone",
    title:          "Send to this number",
    required:       false
  ]

  def inputPhone3 = [
    name:           "phone3",
    type:           "phone",
    title:          "Send to this number",
    required:       false
  ]

  def inputPhone4 = [
    name:           "phone4",
    type:           "phone",
    title:          "Send to this number",
    required:       false
  ]

  def inputPushbulletDevice = [
    name:           "pushbullet",
    type:           "device.pushbullet",
    title:          "Which Pushbullet devices?",
    multiple:       true,
    required:       false
  ]

  def inputAudioPlayers = [
    name:           "audioPlayer",
    type:           "capability.musicPlayer",
    title:          "Which audio players?",
    multiple:       true,
    required:       false
  ]

  def inputSpeechText = [
    name:           "speechText",
    type:           "text",
    title:          "Knock Phrase",
    defaultValue:   "There is a knock at the %door",
    required:       false
  ]

  def pageProperties = [
    name:           "pageNotifications",
    //title:          "Notification Options",
    nextPage:       "pageSetup",
    uninstall:      false
  ]

  return dynamicPage(pageProperties) {
    section("Notification Options") {
      paragraph helpAbout
    }
    section("Turn On Switches") {
      input inputSwitches
      input inputDimmerLevel
    }
    section("Push Notifications") {
      input inputPushMessage
    }
    section("Text Messages (SMS)") {
      input inputPhone1
      input inputPhone2
      input inputPhone3
      input inputPhone4
    }
    section("Pushbullet Notifications") {
      input inputPushbulletDevice
    }
    section("Audio Notifications") {
      input inputAudioPlayers
      input inputSpeechText
    }
  }
}

def installed() {
    LOG("installed()")

    initialize()
    state.installed = true
}

def updated() {
    LOG("updated()")

    //unschedule()
    unsubscribe()
    initialize()
}

def initialize() {
  log.info "Knockerz. Version ${getVersion()}. ${textCopyright()}"
  LOG("settings: ${settings}")

  state.lastClosed = 0
  subscribe(settings.accelerationSensors, "acceleration.active", onMovement)
  subscribe(settings.contactSensors, "contact.closed", onContact)

  STATE()
}

def onContact(evt) {
  LOG("onContact(${evt.displayName})")
  state.lastClosed = now()
}

def checkMultiSensor(data) {
  LOG("checkMultiSensor(${data.name})")
  
  def contactSensor = settings.contactSensors.find{ it.label == "${data.name}" || it.name == "${data.name}" }
  LOG("Using ${contactSensor?.label ?: contactSensor?.name} contact sensor")
  if ((contactSensor?.latestValue("contact") == "closed") && (now() - (60 * 1000) > state.lastClosed)) {
    LOG("${data.name} detected a knock.")
    send("${data.name}")
  } else {
    LOG("${data.name} detected acceleration, but appears to be just someone opening the door.")
  }
}

def checkAnySensor(data) {
  LOG("checkAnySensor(${data.name})")
  
  if (settings.contactSensors.any { it.latestValue("contact") == "open" }) {
    LOG("${data.name} knocked, but a door is open.")
  } else {
    if (now() - (60 * 1000) > state.lastClosed) {
      LOG("${data.name} detected a knock.")
      send("${data.name}")
    } else {
      LOG("${data.name} detected acceleration, but appears to be just someone opening the door.")
    }
  }
}

def onMovement(evt) {
  LOG("onMovement(${evt.displayName})")
  
  def delay = settings.knockDelay ?: 5
  def accelerationSensor = settings.accelerationSensors.find{ it.label == "${evt.displayName}" || it.name == "${evt.displayName}" }
  if (settings.useMultiSensors || accelerationSensor.name == "Multipurpose Sensor") {
    runIn(delay, "checkMultiSensor", [data: [name: "${evt.displayName}"]])
  } else {
    LOG("${evt.displayName} is a ${accelerationSensor.name}")
    runIn(delay, "checkAnySensor", [data: [name: "${evt.displayName}"]])
  }
}

private send(name) {
  LOG("send(${name})")
  
  def msg = "${name} detected a knock."

  // Only turn on those switches that are currently off
  def switchesOn = settings.switches?.findAll { it?.currentSwitch == "off" }
  LOG("switchesOn: ${switchesOn}")
  if (switchesOn) {
    switchesOn*.on()
    state.offSwitches = switchesOn.collect { it.id }
  }

  settings.cameras*.take()

  notify(msg)
  notifyVoice(name)
}

private def notify(msg) {
  LOG("notify(${msg})")

  if (settings.pushMessage) {
    mySendPush(msg)
  } else {
    sendNotificationEvent(msg)
  }

  if (settings.phone1) {
    sendSms(phone1, msg)
  }

  if (settings.phone2) {
    sendSms(phone2, msg)
  }

  if (settings.phone3) {
    sendSms(phone3, msg)
  }

  if (settings.phone4) {
    sendSms(phone4, msg)
  }

  if (settings.pushbullet) {
    settings.pushbullet*.push(location.name, msg)
  }   
}

private def notifyVoice(name) {
  LOG("notifyVoice(${name})")

  if (!settings.audioPlayer) {
    return
  }

  // Replace %door with name
  def phrase = settings.speechText.replaceAll('%door', name)

  if (phrase) {
    settings.audioPlayer*.playText(phrase)
  }
}

private def myRunIn(delay_s, func) {
  if (delay_s > 0) {
    def date = new Date(now() + (delay_s * 1000))
    runOnce(date, func)
    LOG("scheduled '${func}' to run at ${date}")
  }
}

private def mySendPush(msg) {
  // sendPush can throw an exception
  try {
    sendPush(msg)
  } catch (e) {
    log.error e
  }
}

private def getVersion() {
  return "2.0.0"
}

private def textCopyright() {
  def text = "Copyright © 2017 Delmarva Computer Associates LLC"
}

private def textLicense() {
  def text =
    "This program is free software: you can redistribute it and/or " +
    "modify it under the terms of the GNU General Public License as " +
    "published by the Free Software Foundation, either version 3 of " +
    "the License, or (at your option) any later version.\n\n" +
    "This program is distributed in the hope that it will be useful, " +
    "but WITHOUT ANY WARRANTY; without even the implied warranty of " +
    "MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU " +
    "General Public License for more details.\n\n" +
    "You should have received a copy of the GNU General Public License " +
    "along with this program. If not, see <http://www.gnu.org/licenses/>."
}

private def LOG(message) {
  log.trace message
}

private def STATE() {
  log.trace "state: ${state}"
}