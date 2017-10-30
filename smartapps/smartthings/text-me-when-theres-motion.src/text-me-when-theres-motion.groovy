/**
 *  Text Me When There's Motion
 *
 *  Author: SmartThings
 */
definition(
    name: "Text Me When There's Motion",
    namespace: "smartthings",
    author: "SmartThings",
    description: "Get a text message sent to your phone when motion is detected.",
    category: "Convenience",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Meta/intruder_motion.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Meta/intruder_motion@2x.png"
)

preferences {
	section("When there's movement..."){
		input "motion1", "capability.motionSensor", title: "Where?"
	}
	section("Text me at..."){
		input "phone1", "phone", title: "Phone number?"
	}
}

def installed()
{
	subscribe(motion1, "motion.active", motionActiveHandler)
}

def updated()
{
	unsubscribe()
	subscribe(motion1, "motion.active", motionActiveHandler)
}

def motionActiveHandler(evt) {
	log.trace "$evt.value: $evt, $settings"
	log.debug "$motion1 detected motion, texting $phone1"

	sendSms(phone1, "${motion1.label ?: motion1.name} detected motion")
}