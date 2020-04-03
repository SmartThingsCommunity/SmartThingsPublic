/**
 *  Qater Quality Alert
 *
 *  Author: SmartThings
 */
definition(
    name: "Water Quality Change Notification",
    namespace: "JohnRucker",
    author: "John.Rucker@Solar-current.com",
    description: "Get a push notifications and text messages when your home water quality changes.",
    category: "My Apps",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Meta/water_moisture.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Meta/water_moisture@2x.png"
)

preferences {
    page(name: "pageOne", title: "TDS Sensor", nextPage: "pageTwo", uninstall: true) {
        section("Water Quality Monitoring") {
        paragraph "A Total Dissolved Solids (TDS) Sensor measures dissolved solids in Parts Per Million (PPM) in drinking water. A TDS Sensor can detect changes in your home water supply but this sensor should not be relied on as a sole source of water quality. There are several harmful items in water a TDS sensor cannot detect. Although we have worked hard to make this sensor reliable, it or the network it is connected to may malfunction and give false readings."
        }
    }
    page(name: "pageTwo", title: "Setup Change Notificaiton", install: true, uninstall: true) {
        section("When there's a water quality change detected by...") {
            input "Quality", "capability.testCapability", title: "Pick a TDS Water Quality Sensor"
        }
        section("Send a notification to...") {
            input("recipients", "contact", title: "Recipients", description: "Send notifications to") {
                input "phone", "phone", title: "Phone number?", required: false
        }
		}
	}
}

def installed() {
	subscribe(Quality, "Quality", waterQualityHandler)
}

def updated() {
	unsubscribe()
	subscribe(Quality, "Quality", waterQualityHandler)
}

def waterQualityHandler(evt) {
	def deltaSeconds = 60

	def timeAgo = new Date(now() - (1000 * deltaSeconds))
	def recentEvents = Quality.eventsSince(timeAgo)
	log.debug "Found ${recentEvents?.size() ?: 0} events in the last $deltaSeconds seconds"

	def alreadySentSms = recentEvents.count { it.value && it.value == "wet" } > 1

	if (alreadySentSms) {
		log.debug "SMS already sent to $phone within the last $deltaSeconds seconds"
	} else {
		def msg = "${Quality.displayName} water quality has changed to ${evt.value}!"
		log.debug "${Quality.displayName} water quality has changed to ${evt.value}, texting $phone"

		if (location.contactBookEnabled) {
			sendNotificationToContacts(msg, recipients)
		}
		else {
			sendPush(msg)
			if (phone) {
				sendSms(phone, msg)
			}
		}
	}
}