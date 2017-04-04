/**
 *  Poll with HTTP GET
 */

definition(
    name: "Poll - with HTTP GET",
    namespace: "midyear66",
    author: "b.sanford",
    description: "Calls poll() for selected devices when triggered by HTTP GET.",
    category: "Convenience",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png")

preferences {
    section("About") {
        paragraph "Pollster is a polling smartapp that calls the poll() function " +
            "when it receives a HTTP GET for a specific group."
        paragraph "The phone number is required to text the complete HTTP string to "+
	    "be used in a script to be called from a cron job"
    }
	section("SMS to recieve HTTP Get command"){
    	input "phone", "phone", title: "Phone Number (for SMS, required)", required: true
    }
    for (int n = 1; n <= 4; n++) {
        section("Polling Group ${n}") {
            input "group_${n}", "capability.polling", title:"Select devices to be polled", multiple:true, required:false
        }
    }
}

def snd_sms(){
    DEBUG("sending SMS")
    sendSms(phone, "https://graph.api.smartthings.com/api/smartapps/installations/${app.id}/poll/group1?access_token=${state.accessToken}")
    DEBUG("https://graph.api.smartthings.com/api/smartapps/installations/${app.id}/poll/group1?access_token=${state.accessToken}")
}


def installed() {
	createAccessToken()
	getToken()
	DEBUG("Installed Phone with rest api: $app.id")
    DEBUG("Installed Phone with token: $state.accessToken")
}

def updated() {
	DEBUG("Updated Phone with rest api: $app.id")
    	DEBUG("Updated Phone with token: $state.accessToken")
}

def getToken(){
	if (!state.accessToken) {
		try {
			getAccessToken()
			DEBUG("Creating new Access Token: $state.accessToken")
		} catch (ex) {
			DEBUG("Did you forget to enable OAuth in SmartApp IDE settings for SmartTiles?")
            DEBUG(ex)
		}
	}
    snd_sms()
}

mappings {
  path("/poll/group1") {
    action: [
      GET: "pollTask1"
    ]
  }
  path("/poll/group2") {
    action: [
      GET:"pollTask2"
    ]
  }
   path("/poll/group3") {
    action: [
      GET:"pollTask3"
    ]
  }
   path("/poll/group4") {
    action: [
      GET:"pollTask4"
    ]
  }
}

def pollTask1() {
    DEBUG("pollTask1()")
    settings.group_1*.poll()
}

def pollTask2() {
    DEBUG("pollTask2()")
    settings.group_2*.poll()
}

def pollTask3() {
    DEBUG("pollTask3()")
    settings.group_3*.poll()
}

def pollTask4() {
    DEBUG("pollTask4()")
    settings.group_4*.poll()
}

private def DEBUG(message) {
    log.debug message
}