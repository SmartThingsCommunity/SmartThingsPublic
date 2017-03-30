/**
 *  Test Simulate App with Schedule
 *
 *  Author: ryan
 *  Date: 2015-07-16
 */

// Automatically generated. Make future change here.
definition(
    name: "Test Simulate App with Schedule",
    namespace: "rappleg",
    author: "ryan",
    description: "Test Simulate Scheduled Bug",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience%402x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience%402x.png")

preferences {
	section("Title") {
		// TODO: put inputs here
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
	schedule("0 */1 * * * ?", doNothing)
}

def doNothing() {
	log.debug "Executing schedule to do nothing"
}