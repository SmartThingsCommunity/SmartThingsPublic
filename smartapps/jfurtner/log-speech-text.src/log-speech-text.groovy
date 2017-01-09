include 'asynchttp_v1'

/**
 *  Log Speech Text
 *
 *  Copyright 2016 Jamie Furtner
 *
 */
definition(
    name: "Log Speech Text",
    namespace: "jfurtner",
    author: "Jamie Furtner",
    description: "Log speech text",
    category: "Convenience",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png")


preferences {
	section("Devices") {
		input "logEvent", "capability.speechRecognition", title:'Select log input devices', multiple: true
	}
}

def installed() {
	log.debug "Installed with settings: ${settings}"

	initialize()
}

def updated() {
	log.debug "Updated with settings: ${settings}"

	unsubscribe()
	initialize()
}

def initialize() {
	subscribe logEvent, 'phraseSpoken', speech
}

def speech(evt) {
	def params = [
    	uri: 'https://www.furtner.ca/log.php',
        body: [
        	device: evt.displayName,
            message: evt.value
        ]
    ]
    //log.debug params
    asynchttp_v1.post('responseHandlerMethod', params)
}

def responseHandlerMethod(response, data) {
	if (response.hasError()) {
		log.debug "response error: ${response.errorData}"
    }
}