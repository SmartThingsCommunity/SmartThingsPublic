/********

*/

definition(
	name: "New Brighter Thermostat Control",
	namespace: "trentfoley64",
	author: "A. Trent Foley, Sr.",
	description: "Child SmartApp for Brighter Thermostat Controls.",
	category: "My Apps",
	parent: "trentfoley64:Brighter Thermostat Controls",
	iconUrl: "http://www.trentfoley.com/ST/icons/thermostat.png",
	iconX2Url: "http://www.trentfoley.com/ST/icons/thermostat@2x.png",
	iconX3Url: "http://www.trentfoley.com/ST/icons/thermostat@3x.png",
)

preferences {
    page(name: "setPrefs")
    page(name: "setName")
}

def setPrefs() {
	dynamicPage(name: "setPrefs", title: "Brighter Thermostat Control", nextPage: "setName", uninstall: true) {
    	// Let use pick thermostats
		section("Set these thermostats") {
			input "thermostats", "capability.thermostat", title: "Which?", multiple: true
		}
		// Let user pick set points
		section("To these set points") {
			input "heatSetpoint", "decimal", title: "for Heating", default:70
			input "coolSetpoint", "decimal", title: "for Cooling", default:80
		}
		// Let user pick which days of week
		section("for Days of Week") {
			input "dayOfWeek", "enum",
				title: "Which days?",
				required: true,
				multiple: true,
				options: [
					'Monday',
					'Tuesday',
					'Wednesday',
					'Thursday',
					'Friday',
					'Saturday',
					'Sunday'
				]
		}
		// Let user specify Time
		section("Time of day") {
			input "time", "time", title: "At this time of day"
		}
		// Let user specify presence rules
		section( "Presences") {
			input "anyMustBePresent", "capability.presenceSensor", title: "At least one must be present", multiple: true, required: false
			input "allMustBePresent", "capability.presenceSensor", title: "All must be present", multiple: true, required: false
			input "anyMustBeAbsent", "capability.presenceSensor", title: "At least one must be absent", multiple: true, required: false
            input "allMustBeAbsent", "capability.presenceSensor", title: "All must be absent", multiple: true, required: false
        }

		section( "Notifications" ) {
			input "sendPushMessage", "enum", title: "Send a push notification?", options:["Yes", "No"], required: false, default: "No"
			input "phoneNumber", "phone", title: "Send a text message?", required: false
		}
	}
}

def setName() {
	dynamicPage(name: "setName", title: "Brighter Thermostat Control", install: true, uninstall: true) {
		section("Control name") {
            label title: "Assign a name", required: true, description: getDefaultName(), default: "$thermostats"
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
	def timeNow = now() + 2000

	// If it is past current time, schedule for next day
    // need to make this smarter. Use dow data to compute next date
    // Also, don't use "schedule" which runs every day.  instead
    // use runOnce
	if (scheduleTime.time < timeNow) {
		scheduleTime = scheduleTime + 1 // Adding one adds a day
	}

	log.debug "Scheduling next run for " + scheduleTime // .format("EEE MMM dd yyyy HH:mm z", location.timeZone)
	schedule(scheduleTime, runThermostatControl)
}

def runThermostatControl() {
	def passesChecks = false
	Calendar localCalendar = Calendar.getInstance()
	localCalendar.setTimeZone(location.timeZone)
	int currentDayOfWeek = localCalendar.get(Calendar.DAY_OF_WEEK)

	// Check the current day of week against the list of days of the week for scheduling
	if (dayOfWeek.contains('Monday') && currentDayOfWeek == Calendar.instance.MONDAY) {
		passesChecks = true
	}
	else if (dayOfWeek.contains('Tuesday') && currentDayOfWeek == Calendar.instance.TUESDAY) {
		passesChecks = true
	}
	else if (dayOfWeek.contains('Wednesday') && currentDayOfWeek == Calendar.instance.WEDNESDAY) {
		passesChecks = true
	}
	else if (dayOfWeek.contains('Thursday') && currentDayOfWeek == Calendar.instance.THURSDAY) {
		passesChecks = true
	}
	else if (dayOfWeek.contains('Friday') && currentDayOfWeek == Calendar.instance.FRIDAY) {
		passesChecks = true
	}
	else if (dayOfWeek.contains('Saturday') && currentDayOfWeek == Calendar.instance.SATURDAY) {
		passesChecks = true
	}
	else if (dayOfWeek.contains('Sunday') && currentDayOfWeek == Calendar.instance.SUNDAY) {
		passesChecks = true
	}
	// If day of week checks out, check presences
	if (passesChecks) {
		// If defined, check anyMustBePresent
		if (anyMustBePresent) {
			// If anyMustBePresent does not contain anyone present, do not change thermostats
			if (!anyMustBePresent.currentValue('presence').contains('present')) {
				log.debug "Scheduled thermostat change cancelled due to all of ${anyMustBePresent} being absent."
				passesChecks = false
			}
		}
		// If defined, check allMustBePresent
		if (passesChecks && allMustBePresent) {
			// If allMustBePresent contains anyone not present, do not change thermostats
			if (allMustBePresent.currentValue('presence').contains('not present')) {
				log.debug "Scheduled thermostat change cancelled due to one of ${allMustBePresent} being absent."
				passesChecks = false
			}
		}
		// If defined, check anyMustBeAbsent
		if (passesChecks && anyMustBeAbsent) {
			// If anyMustBeAbsent does not contain anyone not present, do not change thermostats
			if (!anyMustBeAbsent.currentValue('presence').contains('not present')) {
				log.debug "Scheduled thermostat change cancelled due to all of ${anyMustBeAbsent} being present."
				passesChecks = false
			}
		}
		// If defined, check allMustBeAbsent
		if (passesChecks && allMustBeAbsent) {
			// If allMustBeAbsent contains anyone present, do not change thermostats
			if (allMustBeAbsent.currentValue('presence').contains('present')) {
				log.debug "Scheduled thermostat change cancelled due to one of ${allMustBeAbsent} being present."
				passesChecks = false
			}
		}
	}

	// If we have hit the condition to schedule this then lets do it
	if (passesChecks) {
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