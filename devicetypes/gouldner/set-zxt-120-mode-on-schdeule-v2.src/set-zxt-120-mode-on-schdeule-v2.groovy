/**
 *  Set ZXT-120 Mode on Schedule
 *
 *  Author: Ronald Gouldner
 */
definition(
    name: "Set ZXT-120 Mode on schdeule V2",
    namespace: "gouldner",
    author: "Ronald Gouldner",
    description: "Set ZXT-120 Mode based on time setting.",
    category: "My Apps",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Meta/temp_thermo-switch.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Meta/temp_thermo-switch@2x.png"
)

preferences {
	section("Select the ZXT-120 Device... "){
		input "thermostat", "capability.Thermostat", title: "ZXT-120"
	}
	section("Select time of day Mon-Fri...") {
		input "time1", "time", title: "Time Mon-Fri ?"
	}
	section("Select time of day Sat-Sun...") {
		input "time2", "time", title: "Time Sat-Sun ?"
	}
	section("Set Mode"){
		input "mode", "enum", title: "Mode?", options: ["heat","cool","off","dry"]
	}
    section("Notify with Push Notification"){
		input "pushNotify", "enum", title: "Send Push notification ?", options: ["yes","no"]
	}
}

def installed()
{
    log.debug("installed called time1=${time1}")
	schedule(time1, "scheduleCheck")
}

def updated()
{
    setNextSchedule()
}

def scheduleCheck()
{
	log.trace "schedule check"
	changeMode()
	sendNotificationWithMode()
	setNextSchedule()
}

def setNextSchedule()
{
	unschedule()    
	def now=new Date()
	def tz = location.timeZone
	def dayString = now.format("EEE",tz)
    def schedTime
    def schedDays
	if (dayString.equals('Fri') || dayString.equals('Sat')) {
        // Next event will be Sat or Sunday
        log.debug "Using Sat/Sun Schedule $time2"
        schedTime = timeToday(time2,tz)
        schedDays = "SAT,SUN"
	} else {
        log.debug "Using M-F Schedule $time1"
        schedTime = timeToday(time1,tz)
        schedDays = "MON-FRI"
	}
    def hour = schedTime.format("H",tz)
    def min = schedTime.format("m",tz)
    log.debug "$dayString: Scheduling $schedDays $hour:$min"
    // Quartz crontab format Sec Min Hour DayOfMonth Month DayOfWeek 
    // example 0 30 9 ? * SAT,SUN  for 9:30am Saturday and Sunday
	schedule("0 $min $hour ? * $schedDays", "scheduleCheck")
}

private changeMode()
{
	log.debug "Scheduled Mode Change to $mode"
	
	if (mode == "cool") {
		log.debug "Turning on cool mode"
		thermostat.cool()
	}
	if (mode == "heat") {
		log.debug "Turning on cool mode"
		thermostat.heat()
	}
	if (mode == "off") {
		log.debug "Turning on cool mode"
		thermostat.off()
	}
	if (mode == "dry") {
		log.debug "Turning on cool mode"
		thermostat.dry()
	}
}

private sendNotificationWithMode() {
	if (pushNotify == "yes") {
		sendPush("${thermostat.displayName} mode changed to $mode")
	}
}