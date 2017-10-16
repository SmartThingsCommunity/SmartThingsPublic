/**
 *  Ask Alexa Weather Report Extension
 *  Special thanks to Barry Burke for Weather Underground Integration
 *
 *  Copyright © 2017 Michael Struck
 *  Version 1.0.5a 8/03/17
 * 
 *  Version 1.0.0 - Initial release
 *  Version 1.0.1 - Updated icon, added restrictions
 *  Version 1.0.2a (6/17/17) - Deprecated send to notification feed. Will add message queue functionality if feedback is given
 *  Version 1.0.3 - (6/28/17) Replaced notifications with Message Queue
 *  Version 1.0.4 - (7/11/17) Allow suppression of continuation messages.
 *  Version 1.0.5a - (8/3/17) Fixed issue due to changes in Weather Undergroud API
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 */
definition(
    name: "Ask Alexa Weather Report",
    namespace: "MichaelStruck",
    author: "Michael Struck",
    description: "Extension Application of Ask Alexa. Do not install directly from the Marketplace",
    category: "My Apps",
    parent: "MichaelStruck:Ask Alexa",
    iconUrl: "https://raw.githubusercontent.com/MichaelStruck/SmartThingsPublic/master/img/ext.png",
    iconX2Url: "https://raw.githubusercontent.com/MichaelStruck/SmartThingsPublic/master/img/ext@2x.png",
    iconX3Url: "https://raw.githubusercontent.com/MichaelStruck/SmartThingsPublic/master/img/ext@2x.png",
    )
preferences {
    page name:"mainPage"
    page name:"pageWeatherCurrent"
    page name:"pageWeatherForecast"
    page name:"pageMQ"
}
//Show main page
def mainPage() {
	dynamicPage(name: "mainPage", title: "Ask Alexa Weather Report Options", install: true, uninstall: true){
        section {
        	label title:"Weather Report Name (Required)", required: true, image: parent.imgURL() + "weather.png"
            href "pageExtAliases", title: "Weather Report Aliases", description: extAliasDesc(), state: extAliasState()
        }
        section("Weather reporting") {
        	 href "pageWeatherCurrent", title: "Current Weather Report Options", description: none, state: (currWeatherSel() ? "complete" : null)	
             href "pageWeatherForecast", title: "Weather Forecast Options", description: none, state: (foreWeatherSel() ? "complete" : null)	
        }
        section ("Sunrise / Sunset"){    
            input "voiceSunrise", "bool", title: "Speak Today's Sunrise", defaultValue: false
    		input "voiceSunset", "bool", title: "Speak Today's Sunset", defaultValue: false	
        }
        section ("Other Weather Underground information"){
        	input "voiceMoon", "bool", title: "Lunar Rise/Set/Phases", defaultValue:false
            input "voiceTide", "bool", title: "Tide Information", defaultValue: false
            input "voiceWeatherWarnFull", "bool", title: "Give Full Weather Advisories (If Present)", defaultValue: false
        }
        section ("Location") {
        	if (currWeatherSel() || foreWeatherSel() || voiceSunset || voiceSunrise || voiceMoon || voiceTide) input "voiceWeatherLoc", "bool", title: "Speak Location Of Weather Report/Forecast", defaultValue: false
            input "zipCode", "text", title: "Zip Code", required: false
            paragraph "Please Note:\nYour SmartThings location is currently set to: ${location.zipCode}. If you leave the area above blank the report will use your SmartThings location. " +
            	"Enter a zip code above if you want to report on a different location.\n\nData obtained from Weather Underground.", image: parent.imgURL() + "info.png"
		}
        section ("Output options"){
        	href "pageMQ", title: "Send Output To Message Queue(s)", description: mqDesc(), state: wrMsgQue ? "complete" : null, image: parent.imgURL()+"mailbox.png"
            if (parent.contMacro) {
				input "overRideMsg", "bool", title: "Override Continuation Commands (Except Errors)", defaultValue: false, submitOnChange: true
				if (!overRideMsg) input "suppressCont", "bool", title:"Suppress Continuation Messages (But Still Allow Continuation Commands)", defaultValue: false 
            }
        }
        section("Restrictions", hideable: true, hidden: !(runDay || timeStart || timeEnd || runMode || runPeople)) {            
			input "runDay", "enum", options: ["Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"], title: "Only Certain Days Of The Week...",  multiple: true, required: false, image: parent.imgURL() + "calendar.png", submitOnChange: true
			href "timeIntervalInput", title: "Only During Certain Times...", description: parent.getTimeLabel(timeStart, timeEnd), state: (timeStart || timeEnd ? "complete":null), image: parent.imgURL() + "clock.png", submitOnChange: true
			input "runMode", "mode", title: "Only In The Following Modes...", multiple: true, required: false, image: parent.imgURL() + "modes.png", submitOnChange: true
            input "runPeople", "capability.presenceSensor", title: "Only When Present...", multiple: true, required: false, submitOnChange: true, image: parent.imgURL() + "people.png"
			if (runPeople && runPeople.size()>1) input "runPresAll", "bool", title: "Off=Any Present; On=All Present", defaultValue: false
            input "muteRestrictions", "bool", title: "Mute Restriction Messages In Extension Group", defaultValue: false
        }
        section("Tap below to remove this message queue"){ }
	}
}
def pageMQ(){
    dynamicPage(name:"pageMQ"){
        section {
        	paragraph "Message Queue Configuration", image:parent.imgURL()+"mailbox.png"
        }
        section (" "){
            input "wrMsgQue", "enum", title: "Message Queue Recipient(s)...", options: parent.getMQListID(true), multiple:true, required: false, submitOnChange: true
            input "wrMQNotify", "bool", title: "Notify Only Mode (Not Stored In Queue)", defaultValue: false, submitOnChange: true
            if (!wrMQNotify) input "wrMQExpire", "number", title: "Message Expires (Minutes)", range: "1..*", required: false, submitOnChange: true
            if (!wrMQNotify && !wrMQExpire) input "wrMQOverwrite", "bool", title: "Overwrite Other Voice Report Messages", defaultValue: false
            if (!wrMQNotify) input "wrSuppressTD", "bool", title: "Suppress Time/Date From Alexa Playback", defaultValue: false
        }
    }
}
page(name: "timeIntervalInput", title: "Only during a certain time") {
	section {
		input "timeStart", "time", title: "Starting", required: false
		input "timeEnd", "time", title: "Ending", required: false
	}
}
page(name: "pageExtAliases", title: "Enter alias names for this weather report"){
	section {
    	for (int i = 1; i < extAliasCount()+1; i++){
        	input "extAlias${i}", "text", title: "Weather Report Alias Name ${i}", required: false
		}
    }
}
def pageWeatherCurrent(){
	dynamicPage(name: "pageWeatherCurrent", install: false, uninstall: false) {
        section ("Items to include in current weather report") {
            input "voiceWeatherTemp", "bool", title: "Temperature (With Conditions)", defaultValue: false
            input "voiceWeatherHumid", "bool", title: "Humidity (With Winds And Pressure)", defaultValue: false
            input "voiceWeatherDew", "bool", title: "Dew Point", defaultValue: false
            input "voiceWeatherSolar", "bool", title: "Solar and UV Radiation", defaultValue: false
            input "voiceWeatherVisiblity", "bool", title: "Visibility", defaultValue: false
            input "voiceWeatherPrecip", "bool", title: "Precipitation", defaultValue: false
        }
    }
}
def pageWeatherForecast(){
	dynamicPage(name: "pageWeatherForecast", install: false, uninstall: false) {
        section ("Weather forecast options") {
        	input "voiceWeatherToday", "bool", title: "Speak Today's Weather Forecast", defaultValue: false
            input "voiceWeatherTonight", "bool", title: "Speak Tonight's Weather Forecast", defaultValue: false
            input "voiceWeatherTomorrow", "bool", title: "Speak Tomorrow's Weather Forecast", defaultValue: false
        }
    }
}
def installed() {
    initialize()
}
def updated() {
	unsubscribe() 
    initialize()
}
def initialize() {
	sendLocationEvent(name: "askAlexa", value: "refresh", data: [macros: parent.getExtList()] , isStateChange: true, descriptionText: "Ask Alexa extension list refresh")
}
//Main Handlers
def getOutput(){
	String outputTxt = "", feedData = ""
    def playContMsg, suppressContMsg 
	if (currWeatherSel() || foreWeatherSel() || voiceSunset || voiceSunrise || voiceMoon || voiceTide ){
		if (location.timeZone || zipCode){
			Map cond = getWeatherFeature("conditions", zipCode)
			if ((cond == null) || cond.response.containsKey("error")) outputTxt += "Your hub location or supplied Zip Code is unrecognized by Weather Underground. %1%"
			else {
				if (voiceWeatherLoc){
					def type = currWeatherSel() || voiceSunset || voiceSunrise || voiceMoon || voiceTide ? "report" : ""
					if (foreWeatherSel() && type) type += " and forecast"
					else if (foreWeatherSel() && !type) type = "forecast"
					if (type) outputTxt += "The following weather ${type} comes from " + cond.current_observation.observation_location.full.replaceAll(',', '') + ": "
				}
				outputTxt += currWeatherSel() || foreWeatherSel() || voiceSunset || voiceSunrise ? weatherAlerts() : ""
				outputTxt += currWeatherSel() ? getWeatherReport() : ""
				outputTxt += foreWeatherSel() || voiceSunset || voiceSunrise ? getWeatherForecast() : ""
				outputTxt += voiceMoon ? getMoonInfo() : ""
				outputTxt += voiceTide ? tideInfo(): ""
				playContMsg = overRideMsg ? false : true 
                suppressContMsg = suppressCont && !overRideMsg && parent.contMacro
            }
		}
	else outputTxt += "Please set the location of your hub with the SmartThings mobile app, or enter a zip code to receive weather reports. %1%"
	} 
    else outputTxt +="You don't have any weather reporting options set up within the '${app.label}'. %1%"
    if (outputTxt && !outputTxt.endsWith("%") && !outputTxt.endsWith(" ")) outputTxt += " "
    if (outputTxt && !outputTxt.endsWith("%") && playContMsg && !suppressContMsg ) outputTxt += "%4%"
    else if (outputTxt && !outputTxt.endsWith("%") && suppressContMsg ) outputTxt += "%X%"
	if (wrMsgQue){
        def expireMin=wrMQExpire ? wrMQExpire as int : 0, expireSec=expireMin*60
        def overWrite =!wrMQNotify && !wrMQExpire && wrMQOverwrite
        def msgTxt = outputTxt.endsWith("%") ? outputTxt[0..-4] : outputTxt
        sendLocationEvent(
            name: "AskAlexaMsgQueue", 
            value: "Ask Alexa Weather Report, '${app.label}'",
            unit: "${app.id}",
            isStateChange: true, 
            descriptionText: msgTxt, 
            data:[
                queues:wrMsgQue,
                overwrite: overWrite,
                notifyOnly: wrMQNotify,
                expires: expireSec,
                suppressTimeDate:wrSuppressTD   
            ]
        )
    }
    return outputTxt
}
//Main Menus
def extAliasDesc(){
	def result =""
	for (int i= 1; i<extAliasCount()+1; i++){
		result += settings."extAlias${i}" ? settings."extAlias${i}" : ""
		result += (result && settings."extAlias${i+1}") ? "\n" : ""
	}
    result = result ? "Alias Names currently configured; Tap to edit:\n"+result :"Tap to add alias names to this weather report"
    return result
}
def extAliasState(){
	def count = 0
    for (int i= 1; i<extAliasCount()+1; i++){
    	if (settings."extAlias${i}") count ++
    }
    return count ? "complete" : null
}
//Child Common modules
def mqDesc(){
    def result = "Tap to add/edit the message queue options"
    if (wrMsgQue){
    	result = "Send to: ${translateMQid(wrMsgQue)}"
        result += wrMQNotify ? "\nNotification Mode Only" : ""
        result += wrMQExpire ? "\nExpires in ${wrMQExpire} minutes" : ""
        result += wrMQOverwrite ? "\nOverwrite all previous voice report messages" : ""
        result += wrSuppressTDRemind ? "\nSuppress Time and Date from Alexa Playback" : ""
	}
    return result
}
def translateMQid(mqIDList){
	def result=mqIDList.contains("Primary Message Queue")?["Primary Message Queue"]:[], qName
	mqIDList.each{qID->
    	qName = parent.getAAMQ().find{it.id == qID}	
    	if (qName) result += qName.label
    }
    return parent.getList(result)
}
def getOkToRun(){ def result = (!runMode || runMode.contains(location.mode)) && parent.getDayOk(runDay) && parent.getTimeOk(timeStart,timeEnd) && parent.getPeopleOk(runPeople,runPresAll) }
private currWeatherSel() { return voiceWeatherTemp || voiceWeatherHumid || voiceWeatherDew || voiceWeatherSolar || voiceWeatherVisiblity || voiceWeatherPrecip }
private foreWeatherSel() { return voiceWeatherToday || voiceWeatherTonight || voiceWeatherTomorrow}
def extAliasCount() { return 3 }
private getWeatherReport(){
	def temp, sb = new StringBuilder(), isMetric = location.temperatureScale == 'C'
	Map conditions = getWeatherFeature('conditions', zipCode)
    if ((conditions == null) || conditions.response.containsKey('error')) return "There was an error in the weather data received Weather Underground. "
	def cond = conditions.current_observation        
	if (voiceWeatherTemp){
        sb << 'The current temperature is '
        temp = isMetric ? parent.roundValue(cond.temp_c) : Math.round(cond.temp_f)
		sb << temp + ' degrees with ' + cond.weather
        if (cond.weather =~/Overcast|Clear|Cloudy/) sb << " skies. "
        else if (cond.weather =~/Unknown|storm/) sb << " conditions. "
        else sb << ". "
	}
    if (voiceWeatherHumid){
    	sb << 'The relative humidity is ' + cond.relative_humidity + ' and the winds are '
        if ((cond.wind_kph.toFloat() + cond.wind_gust_kph.toFloat()) == 0.0) sb << 'calm. '
        else if (isMetric) {
        	sb << 'from the ' + cond.wind_dir + ' at ' + cond.wind_kph + ' km/h'
            if (cond.wind_gust_kph.toFloat() > 0) sb << ' gusting to ' + cond.wind_gust_kph + ' km/h. '
            else sb << '. '
		}
		else sb << cond.wind_string + '. '
		sb << 'The barometric pressure is '
        def pressure = isMetric ? cond.pressure_mb + ' millibars' : cond.pressure_in + ' inches'
        if (cond.pressure_trend=="+") sb << pressure + " and rising. "
        else if (cond.pressure_trend=="1") sb << pressure + " and falling. "
        else sb << "steady at " + pressure + ". "
	}
    if (voiceWeatherDew){
        temp = isMetric ? parent.roundValue(cond.dewpoint_c) : Math.round(cond.dewpoint_f)
        def flTemp = isMetric ? parent.roundValue(cond.feelslike_c) : Math.round(cond.feelslike_f.toFloat())
        if (temp == flTemp) sb << "The dewpoint and 'feels like' temperature are both ${temp} degrees. "
        else sb << "The dewpoint is ${temp} degrees, and the 'feels like' temperature is ${flTemp} degrees. "
	}
    if (voiceWeatherSolar){
        if (cond.solarradiation != '--' && cond.UV != '') sb << 'The solar radiation level is ' + cond.solarradiation + ', and the UV index is ' + cond.UV + '. '
        if (cond.solarradiation != '--' && cond.UV == '') sb << 'The solar radiation level is ' + cond.solarradiation + '. '
        if (cond.solarradiation == '--' && cond.UV != '')  sb << 'The UV index is ' + cond.UV + '. '
	}
	if (voiceWeatherVisiblity) {
        sb << 'Visibility is '
        def visibility = isMetric ? cond.visibility_km.toFloat() : cond.visibility_mi.toFloat()
        String t = visibility as String
        if (visibility >1  && t.endsWith('.0')) t = t - '.0'
        else if (visibility < 1 ) t=t.toFloat()
        if (visibility == 1) if (isMetric) sb << t + ' kilometer. ' else sb << t + ' mile. ' 
        else if (isMetric) sb << t + ' kilometers. ' else sb << t + ' miles. '
	}
    if (voiceWeatherPrecip) {    
		sb << 'There has been '
        def precip = isMetric ? cond.precip_today_metric : cond.precip_today_in, p = 'no'
        if (!precip.isNumber()) {
        	if (precip == 'T') p = 'a trace of'
        } else if (precip) {
			if (precip.toFloat() > 0.0) {
				p = precip as String
				if (p.endsWith('.0')) p = p - '.0'
   			}
		} else precip = 0.0		
		sb << p
    	if ( (p != 'no') && (p != 'a trace of') ) {
			if (precip.toFloat() != 1.0) {
    			if (isMetric) sb << ' millimeters of' else sb << ' inches of'
    		}
            else {
              	if (isMetric) sb << ' millimeter of' else sb << ' inch of'
    		}
    	}
		sb << ' precipitation today. '
	}
	return sb.toString()
}
private getWeatherForecast(){
    def msg = "", isMetric = location.temperatureScale == 'C'
	Map weather = getWeatherFeature('forecast', zipCode)
    if ((weather == null) || weather.response.containsKey('error')) return "There was an error in the weather data received Weather Underground. "
    if (foreWeatherSel()){
		if (voiceWeatherToday){
			msg += "${weather.forecast.txt_forecast.forecastday[0].title}'s forecast calls for "
			msg += isMetric ? weather.forecast.txt_forecast.forecastday[0].fcttext_metric + " " : weather.forecast.txt_forecast.forecastday[0].fcttext + " "
        }
        if (voiceWeatherTonight){
			msg += "For ${weather.forecast.txt_forecast.forecastday[1].title}'s forecast you can expect "
			msg += isMetric ? weather.forecast.txt_forecast.forecastday[1].fcttext_metric + " " : weather.forecast.txt_forecast.forecastday[1].fcttext + " "
        }
        if (voiceWeatherTomorrow){
			msg += "Your forecast for ${weather.forecast.txt_forecast.forecastday[2].title} is "
			msg += isMetric ? weather.forecast.txt_forecast.forecastday[2].fcttext_metric + " " : weather.forecast.txt_forecast.forecastday[2].fcttext + " "
        }
		msg = msg.replaceAll( /(Cloudy|Overcast|Sunny) skies|(Cloudy|Overcast|Sunny)|(cloudy) skies|(cloudy)/, /$1 $2 $3 $4/ + " skies ")
    }
    if (voiceSunrise || voiceSunset){
    	Map astronomy = getWeatherFeature('astronomy', zipCode)
        if ((astronomy == null) || astronomy.response.containsKey('error')) return "There was an error in the sunrise or sunset data received Weather Underground. "
		Integer cur_hour = astronomy.moon_phase.current_time.hour.toInteger()				// get time at requested location
		Integer cur_min = astronomy.moon_phase.current_time.minute.toInteger()				// may not be the same as the SmartThings hub location
		Integer cur_mins = (cur_hour * 60) + cur_min
        Integer rise_hour = astronomy.moon_phase.sunrise.hour.toInteger()
        Integer rise_min = astronomy.moon_phase.sunrise.minute.toInteger()
        Integer rise_mins = (rise_hour * 60) + rise_min
        Integer set_hour = astronomy.moon_phase.sunset.hour.toInteger()
        Integer set_min = astronomy.moon_phase.sunset.minute.toInteger()
        Integer set_mins = (set_hour * 60) + set_min
        def verb1 = cur_mins >= rise_mins ? 'rose' : 'will rise'
        def verb2 = cur_mins >= set_mins ? 'set' : 'will set'
        if (rise_hour == 0) rise_hour = 12            
        if (set_hour > 12) set_hour = set_hour - 12
        String rise_minTxt = rise_min < 10 ? '0'+rise_min : rise_min
        String set_minTxt = set_min < 10 ? '0'+set_min : set_min
        if (voiceSunrise && voiceSunset) msg += "The sun ${verb1} this morning at ${rise_hour}:${rise_minTxt} am and ${verb2} tonight at ${set_hour}:${set_minTxt} pm. "
        else if (voiceSunrise && !voiceSunset) msg += "The sun ${verb1} this morning at ${rise_hour}:${rise_minTxt} am. "
        else if (!voiceSunrise && voiceSunset) msg += "The sun ${verb2} tonight at ${set_hour}:${set_minTxt} pm. "
    }
    return msg
}
def getMoonInfo(){
	def msg = "", dir, nxt, days, sss =""
   	Map astronomy = getWeatherFeature( 'astronomy', zipCode )
    if ((astronomy == null) || astronomy.response.containsKey('error')) return "There was an error in the lunar data received Weather Underground. "
	Integer cur_hour = astronomy.moon_phase.current_time.hour.toInteger()				// get time at requested location
	Integer cur_min = astronomy.moon_phase.current_time.minute.toInteger()				// may not be the same as the SmartThings hub location
	Integer cur_mins = (cur_hour * 60) + cur_min
    Integer rise_hour = astronomy.moon_phase.moonrise.hour.isInteger()? astronomy.moon_phase.moonrise.hour.toInteger() : -1
	Integer rise_min = astronomy.moon_phase.moonrise.minute.isInteger()? astronomy.moon_phase.moonrise.minute.toInteger() : -1
	Integer rise_mins = (rise_hour * 60) + rise_min
	Integer set_hour = astronomy.moon_phase.moonset.hour.isInteger()? astronomy.moon_phase.moonset.hour.toInteger() : -1
	Integer set_min = astronomy.moon_phase.moonset.minute.isInteger()? astronomy.moon_phase.moonset.minute.toInteger() : -1
    Integer set_mins = (set_hour * 60) + set_min
    String verb1 = cur_mins >= rise_mins ? 'rose' : 'will rise'
    String verb2 = cur_mins >= set_mins ? 'set' : 'will set'
    String rise_ampm = rise_hour >= 12 ? "pm" : "am"
    String set_ampm = set_hour >= 12 ? "pm" : "am"
    if (rise_hour == 0) rise_hour = 12
    if (set_hour == 0) set_hour = 12
    if (rise_hour > 12) rise_hour = rise_hour - 12
    if (set_hour > 12) set_hour = set_hour - 12
    String rise_minTxt = rise_min < 10 ? '0'+rise_min : rise_min
    String set_minTxt = set_min < 10 ? '0'+set_min : set_min
    String riseTxt = "${verb1} at ${rise_hour}:${rise_minTxt} ${rise_ampm}"
    String setTxt =  "${verb2} at ${set_hour}:${set_minTxt} ${set_ampm}"
    msg += 'The moon '
	if (set_mins < 0) msg += "${riseTxt}. "  
	else if (rise_mins < 0) msg += "${setTxt}. "
	else msg += rise_mins < set_mins ? "${riseTxt} and ${setTxt}. " : "${setTxt} and ${riseTxt}. "    
    def moon = astronomy.moon_phase
    def m = moon.ageOfMoon.toInteger()
    sss = m == 1 ? "" : "s"
	msg += "The moon is ${m} day${sss} old at ${moon.percentIlluminated}%, "
    if (m < 8) {
            dir = 'Waxing' 
            nxt = 'First Quarter'
            days = 8 - m
    } else if (m < 15) {
        	dir = 'Waxing'
            nxt = 'Full'
            days = 15 - m
    } else if (m < 23) {
        	dir = 'Waning'
            nxt = 'Third Quarter'
			days = 22 - m
    } else {
            dir = 'Waning'
            nxt = 'New'
            days = 29 - m
    }
	sss = days.toInteger() != 1 ? "s" : ""
    switch (moon.percentIlluminated.toInteger()) {
    	case 0:
            msg += 'New Moon, and the First Quarter moon is in 7 days. '
            break
        case 1..49:
            msg += "${dir} Crescent, and the ${nxt} moon is "
            if (days == 0) msg+= "later today. " else msg+= "in ${days} day${sss}. "               
            break
		case 50:
            if (dir == "Waxing") msg += "First Quarter, " else msg += "Third Quarter, "
            msg += "and the ${nxt} Moon is in ${days} day${sss}. "
            break
        case 51..99:
            msg += "${dir} Gibbous, and the ${nxt} moon is "
            if (days == 0) msg += "later today. " else msg += "in ${days} day${sss}. "
            break
        case 100:
            msg += 'Full Moon, and the Third Quarter moon is in 7 days. '
            break
        default:
			msg += '. '
	}
    return msg
}
def weatherAlerts(){
	String msg = ""
    def brief = false
    Map advisories = getWeatherFeature('alerts', zipCode)
    if ((advisories == null) || advisories.response.containsKey('error')) return "There was an error in the weather alerts data received Weather Underground. "
	def alerts = advisories.alerts
    if ((alerts != null) && (alerts.size() > 0)) {
		msg += alerts.size() == 1 ? "ALERT! There is one active advisory for this area. " : "ALERT! There are ${alerts.size()} active advisories for this area. "
        def warn
        if (voiceWeatherWarnFull) {
			if (alerts[0].date_epoch == 'NA') {
				def explained = []
                alerts.each {
					msg += "${it.wtype_meteoalarm_name} Advisory"
                    if (it.level_meteoalarm != "") msg += ", level ${it.level_meteoalarm}"
                    if (it.level_meteoalarm_name != "") msg += ", color ${it.level_meteoalarm_name}"
                    msg += ". "
                    if (brief) warn = " ${it.description} Advisory issued ${it.date}, expires ${it.expires}. "
                    else {
                    	if (it.level_meteoalarm == "") {
                        if (it.level_meteoalarm_description != "") msg += "${it.level_meteoalarm_description} "
                        } else if (!explained.contains(it.level_meteoalarm)) {
                        if (it.level_meteoalarm_description != "") msg += "${it.level_meteoalarm_description} "                       	
                        	explained.add(it.level_meteoalarm)
                        }
						warn = "${it.description} This advisory was issued on ${it.date} and it expires on ${it.expires}. "
					}
                    warn = warn.replaceAll("kn\\, ", ' knots, ').replaceAll('Bft ', ' Beaufort level ').replaceAll("\\s+", ' ').trim()
                    if (!warn.endsWith(".")) warn += '.'
                    msg += "${warn} " 
                }
			} else {
            	alerts.each { alert ->
                    def desc = alert.description.startsWith("A") ? "An" : "A"
                    msg += "${desc} ${alert.description} is in effect from ${alert.date} until ${alert.expires}. "
                    if ( !brief ) {
                        warn = alert.message.replaceAll("\\.\\.\\.", ', ').replaceAll("\\* ", ' ') 				// convert "..." and "* " to a single space (" ")
                        def warnings = [] 																		// See if we need to split up the message (multiple warnings are separated by a date stamp)
                        def i = 0
                        while ( warn != "" ) {
                            def ddate = warn.replaceFirst(/(?i)(.+?)(\d{3,4} (am|pm) .{3} .{3} .{3} \d+ \d{4})(.*)/, /$2/)
                            if ( ddate && (ddate.size() != warn.size())) {
                                def d = warn.indexOf(ddate)
                                warnings[i] = warn.take(d-1)
                                warn = warn.drop(d+ddate.size())
                                i ++
                            } else {
                                warnings[i] = warn
                                warn = ""
                            }
                        }
                        def headline = ""
                        warnings.each { warning ->
                            def b = 1
                            def e = warning.indexOf(',', b+1)
                            if (e>b) {
                                def head = warning.substring(b, e)												// extract the advisory headline 
                                if (head.startsWith( ', ')) head = head - ', '
                                if (i!=0) {																		// if more than one warning, check for repeats.
                                    if (headline == "") {
                                        headline = head															// first occurance
                                        warn = head + '. '
                                        warning = warning.drop( e+2 )											// drop the headline 
                                    } else if (head != headline) {												// different headline
                                        warn = head + '. '
                                        warning = warning.drop( e+2 )											// drop the headline 
                                    } else { 
                                        warn = ""
                                    }																			// headlines are the same, drop this warning[]
                                } else {
                                    warn = head + '. '															// only 1 warning in this Advisory
                                    warning = warning.drop( e+2 )												// drop the headline 
                                }
                            } 
                            else warn = " "
                            if (warn != "") {																	// good warning - let's clean it up
                                def m
                                warning = warning.replaceAll(/(?i) (\d{1,2})(\d{2}) (am|pm) /, / $1:$2 $3 / )	// fix time for Alexa to read 
                                warn = warn.replaceAll(/(?i) (\d{1,2})(\d{2}) (am|pm) /, / $1:$2 $3 / )
                                def table = warning.replaceFirst("(?i).*(Fld\\s+observed\\s+forecast).*", /$1/)
                                if (table && (table.size() != warning.size())) {
                                    m = warning.indexOf( table )
                                    if (m>0) warning = warning.take(m-1)
                                }
                                def latlon = warning.replaceFirst("(?i).+(Lat, Lon).+", /$1/)
                                if (latlon && (latlon.size() != warning.size())) {
                                    m = warning.indexOf( latlon )
                                    if (m>0) warning = warning.take(m-1)
                                }
                                warning = warning.replaceFirst("(.+\\.)(.*)", /$1/)								// strip off Alert author, if present
                                warning = warning.replaceAll(/\/[sS]/, /\'s/).trim()							// fix escaped plurals, and trim excess whitespace
                                if (!warning.endsWith('.')) warning += '.'										// close off this warning with a period                            			
                                msg += warn + warning + ' '
                                warn = ""
							}	
                    	}
                	}
            	} 	
        	}
		}
		else msg += 'Configure your SmartApp to give you full advisory information. '
	}
	return msg
}
private tideInfo() {
	String msg = ""
    Map tData = getWeatherFeature("tide", zipcode)
    Map aData = getWeatherFeature("astronomy", zipcode)
    if (tData == null || aData == null || tData.response.containsKey('error') || aData.response.containsKey('error')) return "There was an error in the tide data received Weather Underground. "
	def tideSite = tData.tide.tideInfo.tideSite.join(",").replaceAll(',', '' )
	if (tideSite == "") {
		msg = "No tide station found near this location"
		if (zipCode) msg += " (${zipCode}). " else msg+= '. '
		return msg
	}        
	Integer cur_hour = aData.moon_phase.current_time.hour.toInteger()				// get time at requested location
	Integer cur_minute = aData.moon_phase.current_time.minute.toInteger()			// may not be the same as the SmartThings hub location
	Integer cur_mins = (cur_hour * 60) + cur_minute
	String timeZoneTxt = tData.tide.tideSummary[0].date.pretty.replaceAll(/\d+:\d+ .{2} (.{3}) .*/, /$1/)
	Integer count = 0, index = 0
	while (count < 4) {	
		def tide = tData.tide.tideSummary[index]
        index ++
		if ((tide.data.type == 'High Tide') || (tide.data.type == 'Low Tide')) {
			count ++
			Integer tide_hour = tide.date.hour.toInteger()
			Integer tide_min = tide.date.min.toInteger()
			Integer tide_mins = (tide_hour * 60) + tide_min	               
			String dayTxt = 'this'
			if (tide_mins < cur_mins) {														// tide event is tomorrow
				dayTxt = 'tomorrow'
				tide_mins = tide_mins + 1440
			}
			Integer minsUntil = tide_mins - cur_mins
			Integer whenHour = minsUntil / 60
			Integer whenMin = minsUntil % 60				
            String ampm = 'am'
			String whenTxt = 'morning'
			if (tide_hour > 11) {
                ampm = 'pm'
				if ( tide_hour < 18) whenTxt = 'afternoon'                       
				else if (tide_hour < 20) whenTxt = 'evening'
				else {
					if (dayTxt == 'this') {
						whenTxt = 'tonight' 
						dayTxt = ''
					} else whenTxt = 'night'
				}
			}                
			if (count <= 2) msg += 'The next '
			else if (count == 3) msg += 'Then '
			else msg += 'followed by '	
			msg += tide.data.type + ' '
			if (tide_hour > 12) tide_hour = tide_hour - 12
            if (tide_hour==0) tide_hour = 12
            String tide_minTxt
            if (tide_min < 10) tide_minTxt = '0'+tide_min else tide_minTxt = tide_min                
			if (count == 1) {
				msg += "at ${tideSite} will be in "
				if (whenHour > 0) {
					msg += "${whenHour} hour"
					if (whenHour > 1) msg +='s'
					if (whenMin > 0) msg += ' and'
				}
				if (whenMin > 0) {
					msg += " ${whenMin} minute"
					if (whenMin > 1) msg +='s'
				}
				msg += " at ${tide_hour}:${tide_minTxt} ${ampm} ${dayTxt} ${whenTxt} (all times ${timeZoneTxt}). "
			} else if (count == 2) msg += "will be ${dayTxt} ${whenTxt} at ${tide_hour}:${tide_minTxt} ${ampm}. "
			else if (count == 3) msg += "again ${dayTxt} ${whenTxt} at ${tide_hour}:${tide_minTxt} ${ampm}, "
			else msg += "at ${tide_hour}:${tide_minTxt} ${ampm} ${dayTxt} ${whenTxt}. "
		}
	}
    return msg		
}
//Version/Copyright/Information/Help
private versionInt(){ return 105 }
private def textAppName() { return "Ask Alexa Weather Report" }	
private def textVersion() { return "Weather Report Version: 1.0.5a (08/03/2017)" }