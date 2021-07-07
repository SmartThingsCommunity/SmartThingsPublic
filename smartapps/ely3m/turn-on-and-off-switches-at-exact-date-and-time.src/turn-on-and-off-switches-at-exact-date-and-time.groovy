/**
 *  turn on at exact date and time
 *
 *  Copyright 2017 ELY M.
 *
if you wish to use the APIs with string inputs directly, you will need to understand their expected format. 
SmartThings uses the Java standard format of “yyyy-MM-dd’T’HH:mm:ss.SSSZ”. 
More technical readers may recognize this format as ISO-8601 (Java does not fully conform to this format, but it is very similar). 
Full discussion of this format is beyond the scope of this documentation, but 
a few examples may help: “January 09, 2015 3:50:32 GMT-6 (Central Standard Time)” converts to “2015-01-09T15:50:32.000-0600”, 
and “February 09, 2015 3:50:32:254 GMT-6 (Central Standard Time)” converts to “2015-02-09T15:50:32.254-0600” 
For more information about date formatting, you can review the SimpleDateFormat JavaDoc.

new Date().format("yyyy-MM-dd'T'HH:mm:ss'Z'", TimeZone.getTimeZone("UTC")) 
Date.parse("yyyy-MM-dd'T'HH:mm:ss'Z'", dateString)


Updated with settings: [starttime:2017-12-10T02:20:00.000-0600, endtime:2017-12-10T02:21:00.000-0600]
Installed with settings: [starttime:2017-12-10T02:20:00.000-0600, endtime:2017-12-10T02:21:00.000-0600]
 *
 */
definition(
    name: "turn on and off switches at exact date and time",
    namespace: "ELY3M",
    author: "ELY M.",
    description: "turn on and off switches at exact date and time.",
    category: "My Apps",
    iconUrl: "http://cdn.device-icons.smartthings.com/Office/office6-icn.png",
    iconX2Url: "http://cdn.device-icons.smartthings.com/Office/office6-icn@2x.png",
    iconX3Url: "http://cdn.device-icons.smartthings.com/Office/office6-icn@2x.png")



preferences {

	section("Select switches to control...") {
		input name: "switches", type: "capability.switch", multiple: true
	}

	def nowdate = new Date(now())
    def nowmonth = nowdate.format('MM')
	def nowday = nowdate.format('dd') 
    def nowyear = nowdate.format('yyyy')
    log.debug "date on pref: ${nowmonth}-${nowday}-${nowyear}"


    section("Start Date") {
      input name: "startMonth", type: "number", title: "Month", required: false, defaultValue: nowmonth
      input name: "startDay", type: "number", title: "Day", required: false, defaultValue: nowday
      input name: "startYear", type: "number", description: "Format(yyyy)", title: "Year", required: false, defaultValue: nowyear
      input name: "calStartTime", type: "time", title: "Start Time", description: "Time", required: true
    }
    section("End Date") {
      input name: "endMonth", type: "number", title: "Month", required: false, defaultValue: nowmonth
      input name: "endDay", type: "number", title: "Day", required: false, defaultValue: nowday
      input name: "endYear", type: "number", description: "Format(yyyy)", title: "Year", required: false, defaultValue: nowyear
      input name: "calEndTime", type: "time", title: "End Time", description: "Time", required: true
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

/*
  	def nowdate = new Date(now())
   	log.debug "Now Date: ${nowdate}"
    def formatted = nowdate.format("yyyy-MM-dd'T'HH:mm:ss.SSSZ", location.getTimeZone())
    log.debug "formatted date: ${formatted}"
*/

    def stime = new Date().parse(smartThingsDateFormat(), calStartTime).format("'T'HH:mm:ss.SSSZ", timeZone(calStartTime))
    def startdate = Date.parse("yyyy-MM-dd'T'HH:mm:ss.SSSZ", "${startYear}-${startMonth}-${startDay}${stime}")
    log.debug "startdate: ${startdate}"
    log.debug "stime: ${stime}"
    
    def etime = new Date().parse(smartThingsDateFormat(), calEndTime).format("'T'HH:mm:ss.SSSZ", timeZone(calEndTime))
    def enddate = Date.parse("yyyy-MM-dd'T'HH:mm:ss.SSSZ", "${endYear}-${endMonth}-${endDay}${etime}")
    log.debug "enddate: ${enddate}"  
    log.debug "etime: ${etime}"
     
    log.debug "startDateTime(): ${startDateTime()}"
    log.debug "endDateTime(): ${endDateTime()}"
    
    def starttimest = "${startDateTime().format(smartThingsDateFormat())}"
    def endtimest = "${endDateTime().format(smartThingsDateFormat())}" 
    log.debug "starttimest: ${starttimest}"  
    log.debug "endtimest: ${endtimest}" 
    
	runOnce(starttimest, start)
    runOnce(endtimest, end)
}


def start() {
switches.on()
log.debug "ON! at ${startDateTime()}"
}

def end() {
switches.off()
log.debug "OFF! at ${endDateTime()}"
}


public smartThingsDateFormat() { "yyyy-MM-dd'T'HH:mm:ss.SSSZ" }

def timeZone() {
  def zone
  if(location.timeZone) {
    zone = location.timeZone
  } else {
    zone = TimeZone.getDefault()
  }
  return zone
}

def startDateTime() {
  if (startDay && startMonth && startYear && calStartTime) {
    def time = new Date().parse(smartThingsDateFormat(), calStartTime).format("'T'HH:mm:ss.SSSZ", timeZone(calStartTime))
    return Date.parse("yyyy-MM-dd'T'HH:mm:ss.SSSZ", "${startYear}-${startMonth}-${startDay}${time}")
  } else {
    // Start Date Time not set
    return false
  }
}

def endDateTime() {
  if (endDay && endMonth && endYear && calEndTime) {
    def time = new Date().parse(smartThingsDateFormat(), calEndTime).format("'T'HH:mm:ss.SSSZ", timeZone(calEndTime))
    return Date.parse("yyyy-MM-dd'T'HH:mm:ss.SSSZ", "${endYear}-${endMonth}-${endDay}${time}")
  } else {
    // End Date Time not set
    return false
  }
}
