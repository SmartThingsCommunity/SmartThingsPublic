/**
 *     Smarter garage door button with integrated sensor
 *     modified from Lights Off, When Closed
 *     implements a virtual switch that ties momentary to sensor
 *     based on the opening or closing of the contact sensor
 *     the door status is kept up to date and does the right thing
 *     so that when you say close it closes and open it opens
 *     if the door is already closed, and you pick close nothing happens
 *     likewise if it is already opened and you pick open nothing happens
 *     the only thing that doesn't work is to stop the garage in mid stream
 * 
 * Modified by Ken Washington to work with any switch and to ensure it goes off
 *
 * 2020=11-4 resync with the forked kewashi public repository
 * 
 *  Author: SmartThings
 *
 *  Date: 2013-05-01
 */

definition(
    name: "Status My Garage Door",
    namespace: "kewashi",
    author: "KenWashington",
    description: "Sets the status of a garage door open/close relay switch based on switch state",
    category: "Convenience",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Meta/light_contact-outlet.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Meta/light_contact-outlet@2x.png"
)


preferences {
    section("This garage door...") {
        input "theDoor", "capability.doorControl", multiple: false, required: true
    }
    section("is controlled by this switch...") {
        input "theOpener", "capability.switch", multiple: false, required: true
    }
    section("whose status is given by this sensor...") {
        input "theSensor", "capability.contactSensor", multiple: false, required: true
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
    subscribe(theDoor, "door", switchHit)
    subscribe(theSensor, "contact", statusChanged)
    subscribe(theOpener, "switch", buttonHit)
}

def buttonHit(evt) {
    def swval = theOpener.currentValue("switch")
    log.debug "Door switch: " + theOpener.name + ", generated value: " + evt.value + ", switch val: " + swval
    
    // turn button off is state is on after hit
    if ( swval == "on" ) {
       theOpener.off()
    }
}

def switchHit(evt) {
    log.debug "Virtual button: " + theDoor.name + ", generated value: " + evt.value
    def current = theSensor.contactState
    log.debug "Sensor: " + theSensor.name + " status = " + current.value

    // if the sensor state is closed and open button is pushed activate
    // it is more reliable to check the virtual opening state instead of open
    if (evt.value == "opening" && current.value == "closed") {
        if ( theOpener.hasCommand("push") ) { 
            theOpener.push()
        } else {
            delayBetween( [ theOpener.off(), theOpener.on(), theOpener.off() ], 200 )
        }
    }    
    // if sensor state is open and closed button is pushed then activate
    // it is more reliable to check the virtual closing state instead of close
     
    else if (evt.value == "closing" && current.value == "open" ) {
        if ( theOpener.hasCommand("push") ) { 
            theOpener.push()
        } else {
            delayBetween( [ theOpener.off(), theOpener.on(), theOpener.off() ], 200 )
        }
    } 
    

}

// handle the cases where sensor state changes for reasons other than button press
// such as manual wall press or the original button press tied to physical relay
// note that the physical button must still be installed - I just hide it in a group
def statusChanged(evt) {
    log.debug "Sensor [" + theSensor.name + "] status = " + evt.value + " door state = "+ theDoor.doorState.value
    if (evt.value == "open" && theDoor.doorState.value!="open") {
        theDoor.open()
    } else if ( evt.value == "closed" && theDoor.doorState.value!="closed" ) {
        theDoor.close()
    }
}
