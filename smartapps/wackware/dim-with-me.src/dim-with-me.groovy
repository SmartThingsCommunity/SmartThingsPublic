/**
 *  dim-with-me.app.groovy
 *  Dim With Me
 *
 *  Author: todd@wackford.net
 *  Date: 2013-11-12
 */
/**
 *  App Name:   Dim With Me
 *
 *  Author: 	Todd Wackford
 *		twack@wackware.net
 *  Date: 	2013-11-12
 *  Version: 	0.2
 *  
 *  Use this program with a virtual dimmer as the master for best results.
 *
 *  This app lets the user select from a list of dimmers to act as a triggering
 *  master for other dimmers or regular switches. Regular switches come on
 *  anytime the master dimmer is on or dimmer level is set to more than 0%.
 *  of the master dimmer.
 *
******************************************************************************
 *                                Changes
 ******************************************************************************
 *
 *  Change 1:	2014-10-22 (wackford)
 *		Fixed bug in setlevelwhen on/off was coming in
 *
 *  Change 2:	2014-11-01 (wackford)
 *		added subscription to switch.level event. Shouldn't change much
 *		but some devices only sending level event and not setLevel.
 *
 ******************************************************************************
                
  Other Info:	Special thanks to Danny Kleinman at ST for helping me get the
				state stuff figured out. The Android state filtering had me 
                stumped.
 *
 ******************************************************************************
 */


// Automatically generated. Make future change here.
definition(
    name: "Dim With Me",
    namespace: "wackware",
    author: "todd@wackford.net",
    description: "Follows the dimmer level of another dimmer",
    category: "My Apps",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience%402x.png"
)

preferences {
	section("When this...") { 
		input "masters", "capability.switchLevel", 
			multiple: false, 
			title: "Master Dimmer Switch...", 
			required: true
	}

	section("Then these will follow with on/off...") {
		input "slaves2", "capability.switch", 
			multiple: true, 
			title: "Slave On/Off Switch(es)...", 
			required: false
	}
    
	section("And these will follow with dimming level...") {
		input "slaves", "capability.switchLevel", 
			multiple: true, 
			title: "Slave Dimmer Switch(es)...", 
			required: true
	}
}

def installed()
{
	subscribe(masters, "switch.on", switchOnHandler)
	subscribe(masters, "switch.off", switchOffHandler)
	subscribe(masters, "switch.setLevel", switchSetLevelHandler)
	subscribe(masters, "switch", switchSetLevelHandler)
}

def updated()
{
	unsubscribe()
	subscribe(masters, "switch.on", switchOnHandler)
	subscribe(masters, "switch.off", switchOffHandler)
	subscribe(masters, "switch.setLevel", switchSetLevelHandler)
	subscribe(masters, "switch", switchSetLevelHandler)
	log.info "subscribed to all of switches events"
}

def switchSetLevelHandler(evt)
{	
	
	if ((evt.value == "on") || (evt.value == "off" ))
		return
	def level = evt.value.toFloat()
	level = level.toInteger()
	log.info "switchSetLevelHandler Event: ${level}"
	slaves?.setLevel(level)
}

def switchOffHandler(evt) {
	log.info "switchoffHandler Event: ${evt.value}"
	slaves?.off()
	slaves2?.off()
}

def switchOnHandler(evt) {
	log.info "switchOnHandler Event: ${evt.value}"
	def dimmerValue = masters.latestValue("level") //can be turned on by setting the level
	slaves?.on()
	slaves2?.on()
}