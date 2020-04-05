/**
 *  Individual Change Thermostat
 *  Allows you to set single/multiple thermostats to different temp during different days of the week unlimited number of times (one app instance for each change)
 *
 *  Taken from : Samer Theodossy
 *  Update - 2014-11-25
 */

// Automatically generated. Make future change here.
definition(
    name: "Individual Change Thermostat",
    namespace: "rboy",
    author: "RBoy",
    description: "Setup unlimited adjustments to the thermostat(s), install this app once for each change you want to make",
    category: "Green Living",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/GreenLiving/Cat-GreenLiving.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/GreenLiving/Cat-GreenLiving@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/GreenLiving/Cat-GreenLiving@3x.png"
    )

preferences {
    section("Set these thermostats") {
        input "thermostat", "capability.thermostat", title: "Which?", multiple:true

    }

    section("To these temperatures") {
        input "heatingSetpoint", "decimal", title: "When Heating"
        input "coolingSetpoint", "decimal", title: "When Cooling"
    }

    section("Configuration") {
        input "dayOfWeek", "enum",
                title: "Which day of the week?",
                required: true,
                multiple: true,
                metadata: [
                    values: [
                        'All Week',
                        'Monday to Friday',
                        'Saturday & Sunday',
                        'Monday',
                        'Tuesday',
                        'Wednesday',
                        'Thursday',
                        'Friday',
                        'Saturday',
                        'Sunday'
                        ]
                    ],
                defaultValue: 'All Week'
        input "time", "time", title: "At this time"
    }

    section( "Notifications" ) {
        input "sendPushMessage", "enum", title: "Send a push notification?", metadata:[values:["Yes", "No"]], required: false
        input "phoneNumber", "phone", title: "Send a text message?", required: false
    }
}

def installed() {
    // subscribe to these events
    log.debug "Installed called with $settings"
    initialize()
}

def updated() {
    // we have had an update
    // remove everything and reinstall
    log.debug "Updated called with $settings"
    initialize()
}

def initialize() {
    unschedule() // bug in ST platform, doesn't clear on running
    def scheduleTime = timeToday(time, location.timeZone)
    def timeNow = now() + (2*1000) // ST platform has resolution of 1 minutes, so be safe and check for 2 minutes) 
    log.debug "Current time is ${(new Date(timeNow)).format("EEE MMM dd yyyy HH:mm z", location.timeZone)}, scheduled time is ${scheduleTime.format("EEE MMM dd yyyy HH:mm z", location.timeZone)}"
    if (scheduleTime.time < timeNow) { // If we have passed current time we're scheduled for next day
        log.debug "Current scheduling check time $scheduleTime has passed, scheduling check for tomorrow"
        scheduleTime = scheduleTime + 1 // Next day schedule
    }
    log.debug "Scheduling Temp change for " + scheduleTime.format("EEE MMM dd yyyy HH:mm z", location.timeZone)
    schedule(scheduleTime, setTheTemp)
}

def setTheTemp() {
    def doChange = false
    Calendar localCalendar = Calendar.getInstance(TimeZone.getDefault());
    int currentDayOfWeek = localCalendar.get(Calendar.DAY_OF_WEEK);

    // Check the condition under which we want this to run now
    // This set allows the most flexibility.
    if(dayOfWeek.contains('All Week')) {
            doChange = true
    }
    else if((dayOfWeek.contains('Monday') || dayOfWeek.contains('Monday to Friday')) && currentDayOfWeek == Calendar.instance.MONDAY) {
            doChange = true
    }

    else if((dayOfWeek.contains('Tuesday') || dayOfWeek.contains('Monday to Friday')) && currentDayOfWeek == Calendar.instance.TUESDAY) {
            doChange = true
    }

    else if((dayOfWeek.contains('Wednesday') || dayOfWeek.contains('Monday to Friday')) && currentDayOfWeek == Calendar.instance.WEDNESDAY) {
            doChange = true
    }

    else if((dayOfWeek.contains('Thursday') || dayOfWeek.contains('Monday to Friday')) && currentDayOfWeek == Calendar.instance.THURSDAY) {
            doChange = true
    }

    else if((dayOfWeek.contains('Friday') || dayOfWeek.contains('Monday to Friday')) && currentDayOfWeek == Calendar.instance.FRIDAY) {
            doChange = true
    }

    else if((dayOfWeek.contains('Saturday') || dayOfWeek.contains('Saturday & Sunday')) && currentDayOfWeek == Calendar.instance.SATURDAY) {
            doChange = true
    }

    else if((dayOfWeek.contains('Sunday') || dayOfWeek.contains('Saturday & Sunday')) && currentDayOfWeek == Calendar.instance.SUNDAY) {
            doChange = true
    }

    // some debugging in order to make sure things are working correclty
    log.debug "Calendar DOW: " + currentDayOfWeek
    log.debug "Configured DOW(s): " + dayOfWeek

    // If we have hit the condition to schedule this then lets do it
    if(doChange == true){
        log.debug "setTheTemp, location.mode = $location.mode, newMode = $newMode, location.modes = $location.modes"
        thermostat.setHeatingSetpoint(heatingSetpoint)
        thermostat.setCoolingSetpoint(coolingSetpoint)
        send "$thermostat heat set to '${heatingSetpoint}' and cool to '${coolingSetpoint}'"
    }
    else {
        log.debug "Temp change not scheduled for today."
    }
    
    log.debug "Scheduling next check"
    
    initialize() // Setup the next check schedule
}

private send(msg) {
        if (sendPushMessage == "Yes") {
            log.debug "sending push message"
            sendPush(msg)
        }

        if (phoneNumber) {
            log.debug "sending text message"
            sendSms(phoneNumber, msg)
        }

        log.debug msg
}