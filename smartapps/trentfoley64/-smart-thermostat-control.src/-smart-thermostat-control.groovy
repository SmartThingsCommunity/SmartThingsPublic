/********

*/

definition (
	name: "_Smart Thermostat Control",
	namespace: "trentfoley64",
	author: "A. Trent Foley, Sr.",
	description: "Child SmartApp for Smart Thermostat Controls.",
	category: "My Apps",
	parent: "trentfoley64:Smart Thermostat Controls",
	iconUrl: "http://www.trentfoley.com/ST/icons/thermostat.png",
	iconX2Url: "http://www.trentfoley.com/ST/icons/thermostat@2x.png",
	iconX3Url: "http://www.trentfoley.com/ST/icons/thermostat@3x.png",
)

preferences {
    page(name: "setPrefs")
    page(name: "setName")
}

def setPrefs() {

	dynamicPage(name: "setPrefs", title: "Smart Thermostat Control", nextPage: "setName", uninstall: true) {
		section("Set these thermostats") {
			input "thermostats", "capability.thermostat", title: "Which?", multiple: true
		}

		section("To these set points") {
			input "heatSetpoint", "decimal", title: "for Heating"
			input "coolSetpoint", "decimal", title: "for Cooling"
		}

		section("for Days of Week") {
			input "dayOfWeek", "enum",
				title: "Which days?",
				required: true,
				multiple: true,
				options: [
					'All Week',
					'Saturday & Sunday',
					'Monday to Friday',
					'Sunday to Thursday',
					'Monday',
					'Tuesday',
					'Wednesday',
					'Thursday',
					'Friday',
					'Saturday',
					'Sunday'
				]

			input "time", "time", title: "At this time of day"
		}

		section( "Presences") {
			input "anyMustBePresent", "capability.presenceSensor", title: "At least one must be present", multiple: true, required: false
			input "allMustBePresent", "capability.presenceSensor", title: "All must be present", multiple: true, required: false
			input "anyMustBeAbsent", "capability.presenceSensor", title: "At least one must be absent", multiple: true, required: false
            input "allMustBeAbsent", "capability.presenceSensor", title: "All must be absent", multiple: true, required: false
        }

		section( "Notifications" ) {
			input "sendPushMessage", "enum", title: "Send a push notification?", options:["Yes", "No"], required: false
			input "phoneNumber", "phone", title: "Send a text message?", required: false
		}
	}
}

def setName() {

	dynamicPage(name: "setName", title: "Smart Thermostat Control", install: true, uninstall: true) {
		section("Control name") {
            label title: "Assign a name", required: true, default: getDefaultName()
            mode title: "Set for specific mode(s)", required: false
        }
    }
}

private getDefaultName() {

	return "$thermostats $time $dayOfWeek $anyMustBePresent $allMustBepresent $anyMustBeAbsent $allMustBeAbsent"
    
}

def installed() {
	log.debug "Installed with $settings"
	initialize()
}

def updated() {
	log.debug "Updated with $settings"
	initialize()
}

def initialize() {
	unschedule()
	def scheduleTime = timeToday(time, location.timeZone)
	def timeNow = now() + (2*1000) // ST platform has resolution of 1 minutes, so be safe and check for 2 minutes)

	// If it is past current time, schedule for next day
    // need to make this smarter. Use dow data to compute next date
	if (scheduleTime.time < timeNow) {
		scheduleTime = scheduleTime + 1 // Adding one adds a day
	}

	log.debug "Scheduling change for " + scheduleTime.format("EEE MMM dd yyyy HH:mm z", location.timeZone)
	schedule(scheduleTime, processScheduledEvent)
}

def processScheduledEvent() {
	def passesChecks = false
	Calendar localCalendar = Calendar.getInstance()
	localCalendar.setTimeZone(location.timeZone)
	int currentDayOfWeek = localCalendar.get(Calendar.DAY_OF_WEEK)

	log.debug "Calendar DOW: " + currentDayOfWeek
	log.debug "Configured DOW(s): " + dayOfWeek

	// Check the current day of week against the list of days of the week for scheduling
	if(dayOfWeek.contains('All Week')) {
		passesChecks = true
	}
	else if((dayOfWeek.contains('Monday') || dayOfWeek.contains('Monday to Friday') || dayOfWeek.contains('Sunday to Thursday')) && currentDayOfWeek == Calendar.instance.MONDAY) {
		passesChecks = true
	}
	else if((dayOfWeek.contains('Tuesday') || dayOfWeek.contains('Monday to Friday') || dayOfWeek.contains('Sunday to Thursday')) && currentDayOfWeek == Calendar.instance.TUESDAY) {
		passesChecks = true
	}
	else if((dayOfWeek.contains('Wednesday') || dayOfWeek.contains('Monday to Friday') || dayOfWeek.contains('Sunday to Thursday')) && currentDayOfWeek == Calendar.instance.WEDNESDAY) {
		passesChecks = true
	}
	else if((dayOfWeek.contains('Thursday') || dayOfWeek.contains('Monday to Friday') || dayOfWeek.contains('Sunday to Thursday')) && currentDayOfWeek == Calendar.instance.THURSDAY) {
		passesChecks = true
	}
	else if((dayOfWeek.contains('Friday') || dayOfWeek.contains('Monday to Friday')) && currentDayOfWeek == Calendar.instance.FRIDAY) {
		passesChecks = true
	}
	else if((dayOfWeek.contains('Saturday') || dayOfWeek.contains('Saturday & Sunday')) && currentDayOfWeek == Calendar.instance.SATURDAY) {
		passesChecks = true
	}
	else if((dayOfWeek.contains('Sunday') || dayOfWeek.contains('Saturday & Sunday') || dayOfWeek.contains('Sunday to Thursday')) && currentDayOfWeek == Calendar.instance.SUNDAY) {
		passesChecks = true
	}

	// If day of week checks out, check presences
	if (passesChecks) {

		// If defined, check anyMustBePresent
		if (anyMustBePresent) {
			// If anyMustBePresent does not contain anyone present, do not change thermostats
			if (!anyMustBePresent.contains('present')) {
				log.debug "Scheduled thermostat change cancelled due to all of ${anyMustBePresent} being absent."
				passesChecks = false
			}
		}

		// If defined, check allMustBePresent
		if (allMustBePresent) {
			// If allMustBePresent contains anyone not present, do not change thermostats
			if (allMustBePresent.contains('not present')) {
				log.debug "Scheduled thermostat change cancelled due to one of ${allMustBePresent} being absent."
				passesChecks = false
			}
		}

		// If defined, check anyMustBeAbsent
		if (anyMustBeAbsent) {
			// If anyMustBeAbsent does not contain anyone not present, do not change thermostats
			if (!anyMustBeAbsent.contains('not present')) {
				log.debug "Scheduled thermostat change cancelled due to all of ${anyMustBeAbsent} being present."
				passesChecks = false
			}
		}

		// If defined, check allMustBeAbsent
		if (allMustBeAbsent) {
			// If allMustBeAbsent contains anyone present, do not change thermostats
			if (allMustBeAbsent.contains('present')) {
				log.debug "Scheduled thermostat change cancelled due to one of ${allMustBeAbsent} being present."
				passesChecks = false
			}
		}

	}

	// If we have hit the condition to schedule this then lets do it
	if (passesChecks){
		def msg = "$thermostats heat setpoint to '${heatSetpoint}' and cool setpoint to '${coolSetpoint}'"
		log.debug msg
		thermostats.setHeatingSetpoint(heatSetpoint)
		thermostats.setCoolingSetpoint(coolSetpoint)
		send msg
	}

	log.debug "Scheduling next check"

	// reinitialize to set next time schedule
	initialize()
}

private send(msg) {
	if (sendPushMessage == "Yes") {
		sendPush(msg)
	}

	if (phoneNumber) {
		sendSms(phoneNumber, msg)
	}
}