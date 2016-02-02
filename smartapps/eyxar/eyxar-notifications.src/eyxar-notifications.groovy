definition(
    name: "EyXAr Notifications",
    namespace: "eyxar",
    author: "EyXAr",
    description: "Phone and Voice notification of your door sensor status and phone presence sensor autonitification.",
    category: "SmartThings Labs",
    iconUrl: "https://s3.amazonaws.com/for-st/For_ST_60px.png",
    iconX2Url: "https://s3.amazonaws.com/for-st/For_ST_120px.png",
    iconX3Url: "https://s3.amazonaws.com/for-st/For_ST_256px.png"
    )

/* For ST will only work if EyXAr Notification is installed and set-up first. */

preferences {
     section("EyXAr Auto Notifications - For Voice Notification, Install the app 'FOR ST' in Google Play")
      {
        input "door", "capability.contactSensor", title: "Select Door/Contact", required: false, multiple: true
    }   

    section("Send Notifications by Text or use below option?") {
        input("recipients", "contact", title: "Send notifications to"){
        input "phone", "phone", title: "Phone Number (optional, text charges may apply)",
                description: "Phone Number", required: false
        }
    }

 section("If combine with 'For ST' android app, this will add features of voice notifications.") {
        input "sendPush", "bool", required: false,
              title: "Phone/Tablet Auto Notification (Must be set to On =>>)"
        }   
    }

/* Presense */    

 section("Phone Presence Auto Notifications - For Voice notifications install 'FOR ST' in Google Play") {
    input "presence", "capability.presenceSensor", title: "Select Phone/Tablet to Detect: (mandatory & rest below are optional)", required: false, multiple: true
      }

def installed() {
    initialize()
/* Presense */     
    subscribe(door, "contact.open", doorOpenHandler)
    subscribe(door, "contact.closed", doorClosedHandler)
    subscribe(presence, "presence", myHandler)
    subscribe(presence, "presence", presenceHandler)
}

def updated() {
    initialize()
}

def initialize() {
    subscribe(door, "contact.open", doorOpenHandler)
    subscribe(door, "contact.closed", doorClosedHandler)
    subscribe(presence, "presence", myHandler)
    subscribe(presence, "presence", presenceHandler)
    
}

def doorOpenHandler(evt) {
    def message = "EyXAr Detected the ${evt.displayName} is ${evt.value}!"
    if (sendPush) {
        sendPush(message)
    }
    if (phone) {
        sendSms(phone, message)
    }
}

def doorClosedHandler(evt) {
    def message = "EyXAr Detected the ${evt.displayName} is ${evt.value}!"
    if (sendPush) {
        sendPush(message)
    }
    if (phone) {
        sendSms(phone, message)
    }
}

def contactHandler(evt) {
  if("open" == evt.value)

    // contact was opened, turn on a light maybe?
    log.debug "Contact is in ${evt.value} state"

  if("closed" == evt.value)
    // contact was closed, turn off the light?
    log.debug "Contact is in ${evt.value} state"
}

/* Presense */  
def myHandler(evt) {
  if("present" == evt.value)
    def message = "EyXAr Detected ${evt.displayName} is ${evt.value}!"
    if (sendPush) {
        sendPush(message)
    }
    if (phone) {
        sendSms(phone, message)
    }
}

def presenceHandler(evt) {

    if (evt.value == "present") {
        log.debug "EyXAr ${evt.displayName} has arrived at the ${location}!"
        sendPush("EyXAr ${evt.displayName} has arrived at the ${location}!")
    } else if (evt.value == "not present") {

        log.debug "EyXAr ${evt.displayName} has left the ${location}!"

        sendPush("EyXAr ${evt.displayName} has left the ${location}!")

    }
}


