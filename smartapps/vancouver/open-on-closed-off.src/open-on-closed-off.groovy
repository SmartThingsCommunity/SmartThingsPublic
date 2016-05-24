/**
 *  Turn on when it opens, off when closed
 */
definition(
    name: "Open on Closed off",
    namespace: "vancouver",
    author: "SmartThings Samsung",
    description: "Turn on things when the contact opens and off when it closes",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png")
preferences {
	section("When the contact opens..."){
		input "contact1", "capability.contactSensor", title: "Pick a sensor:"
	}
	section("Pick a Light To Control..."){
		input "colorLights", "capability.colorControl", multiple: true
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
	subscribe(contact, "contact.open", contactOpenHandler) 
	subscribe(contact, "contact.close", contactCloseHandler) 
    log.info( "contact is currently ->: ${contact1}" )
}
/**
 * contact handler, subscribed for "open" events
 */
def contactOpenHandler(evt) { 
	colorLights.each { 
    	it.on() 
    }
}
def contactCloseHandler(evt) { 
	colorLights.each { 
    	it.off() 
    }
}