/**
 *  Ask Alexa Schedule Extension
 *
 *  Copyright © 2018 Michael Struck
 *  Version 1.0.4a 3/11/18
 * 
 *  Version 1.0.0 (6/1/17) - Initial release
 *  Version 1.0.1 (6/8/17) - Fixed custom schedule issue. Added %age% variable for birthdays/anniversaries
 *  Version 1.0.2 (6/15/17) - Added %age% variable for any text field
 *  Version 1.0.3c (7/6/17) - Added code for additional text field variables, keep 'blank' messages from going to the message queue.
 *  Version 1.0.4a (3/11/18) - Updated to 2018 version, modified "remove" button
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
    name: "Ask Alexa Schedule",
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
        page name:"toggleStatus"
        page name:"pageMQ"
        page name:"pageCronExpression"
        page name:"pageReminder"
        page name:"pageStartDate"
        page name:"pageEndDate"
        page name:"pageCronExpressionRemind"
}
//Show main page
def mainPage() {
    dynamicPage(name: "mainPage", title:"Ask Alexa Schedule Options", install: true, uninstall: true) {
        section { label title:"Schedule Name (Required)", required: true, image: parent.imgURL() + "schedule.png" }
        section ("Status"){
            if (state.status==null) state.status=true
            def imageFile=state.status ? parent.imgURL() + "on.png" : parent.imgURL() + "off.png" 
            def dateTime= schEnd() ?  "${schYearEnd}-${schMonthEnd}-${schDayEnd}${schTimeEnd[10..27]}" : null
            def schStatus = getStatus()
            if (schStatus=~/Incomplete|Invalid|Expired/) {
            	paragraph "This schedule: ${schStatus}", image: parent.imgURL() +"warning.png"
            	if (schStatus=="Invalid") paragraph "You entered a date that is invalid, such as a month with only 30 days or February 29th outside of a leap year. Please correct this to proceed."
            	else if (schStatus=="Expired" && schType=="single") paragraph "The trigger date has already passed. Please correct this to proceed."
                else if (schStatus=="Expired" && schType=="complex") paragraph "The end date has already passed. Please correct this to proceed."
                else if (schStatus=~/Invalid start/) paragraph "Your end time/date is before the start time/date. Please correct this to proceed."
            }
            else href "toggleStatus", title: "This schedule: ${schStatus}", description: "Tap to toggle status", image: imageFile
        }
        section ("Schedule settings"){ input "schType", "enum", title: "Type Of Schedule", options:schTypes(), required: false, submitOnChange: true }
        if (schType=="single"){
			section ("One-time event date"){	
            	def imageFile=(checkDate("end").expired || checkDate("end").warning) ?  parent.imgURL()+"caution.png" : null
            	href "pageEndDate", title: "Time / Date", description: endDateDesc(), state: (schEnd() ? "complete" : null), image: imageFile , submitOnChange: true
                if (schEnd()) input "deleteExpired", "bool", title: "Auto Delete This Schedule After Event Time/Date", defaultValue: false, submitOnChange: true
            }
        }
        else if (schType==~/simple|complex/) {
        	section("Recurring options"){ 
                input "schReType", "enum", title: "Recurrence Interval", required: false, options: reType(), submitOnChange: true
                if (schReType=="custom") input "schComplexTime", "number", title: "Recurrence Time (Minutes)", range:"0..*", required: false
                input "schCount","number", title: "Maximum Number Of Recurrences", range:"1..*", required:false, description: "Leave blank to allow to run continuously", submitOnChange: true
                if (schCount) input "deleteExpired", "bool", title: "Auto Delete This Schedule After Count Reaches Maximum", defaultValue: false, submitOnChange: true
            }
        	if (schType =="complex"){
            	section("Recurrence start and end time/date (optional)"){
                	def imageFileStart= (checkDate("start").warning || checkDate("start").expired) && schReType ==~/1|5|10|15|30|60|180|360|720/ ?  parent.imgURL()+"caution.png" : null
                    def imageFileEnd=(checkDate("end").expired || checkDate("end").warning) ?  parent.imgURL()+"caution.png" : null
                	href "pageStartDate", title: "Start Time / Date", description: startDateDesc(), state: (schStart() ? "complete" : null), image: imageFileStart
                    def whenTxt = schReType==~/day|week|month|year/ && schStart() ? "Please note, the action will trigger (and schedule subsequent recurrences) based on the start time/date above." : 
                    	"Please note that since the start time/date above has past or is empty, the schedule will be based on the current time/date when you press \"Done\""
                    paragraph "${whenTxt}" , image: parent.imgURL()+"info.png"
                	href "pageEndDate", title: "End Time / Date", description: endDateDesc(), state: (schEnd() ? "complete" : null)	, image: imageFileEnd
                    if (schEnd()) input "deleteExpired", "bool", title: "Auto Delete This Schedule After End Time/Date", defaultValue: false, submitOnChange: true
                }
            }
        }
        else if (schType =="bd"){
        	Integer age = ageCalc()
			def schAge = age > 1 ? "\nSchedule's age this year: ${age} years old" : age==1 ? "\nSchedule's age this year: One year" : ""
            section("Recurrence start time/date"){
            	def imageFile=checkDate("start").warning ? parent.imgURL()+"caution.png" : null
				href "pageStartDate", title: "Event Start Time / Date", description: startDateDesc(), state: (schStart() ? "complete" : null), image: imageFile
				paragraph "Please note, the annual action will trigger (and schedule subsequent recurrences) based on the start time/date above, even if in the past. ${schAge}" , image: parent.imgURL() + "info.png"
        	}
        }
        else if (schType=="custom"){
        	section("Custom Cron Expression"){
            	href "pageCronExpression", title: "Cron Expression", description: cronDesc() , state: cronValidate() ? "complete" : null, image: state.cronCheck ? null :parent.imgURL()+"caution.png"
                input "schCount","number", title: "Maximum Number Of Recurrences", range:"1..*", required:false, description: "Leave blank to allow to run within other limits (if any)", submitOnChange: true
            	if (schCount) input "deleteExpired", "bool", title: "Auto Delete This Schedule After Count Reaches Maximum", defaultValue: false, submitOnChange: true
                if (state.cronCheck && cronValidate()) paragraph "Your cron expression will be evaluated after you tap \"Done\". Be sure to check the status of this schedule after returning to the main schedules page.", image: parent.imgURL()+"info.png"
            }	
        }
        section("Scheduled action"){
            input "schAction", "enum", title: "Action To Take...", options: actionList(), submitOnChange: true, required: false
            if (schAction=="msg") {
                input "schMsgTxt", "text", title: "Message To Send", required: false, capitalization: "sentences", submitOnChange: true
                href "pageMQ", title: "Message Queue Options", description: mqDesc(), state: schMsgQue ? "complete" : null, image: parent.imgURL()+"mailbox.png"
           	}
            if (schAction =="VR"){
                input "schVR", "enum", title: "Choose Voice Reports", options: parent.getMacroList("schedV",""), required:false, submitOnChange: true, multiple:true
            	href "pageMQ", title: "Message Queue Options", description: mqDesc(), state: schMsgQue ? "complete" : null, image: parent.imgURL()+"mailbox.png"
            }
            if (schAction =="WR"){
                input "schWR", "enum", title: "Choose Weather Reports", options: parent.getMacroList("schedW",""), required:false, submitOnChange: true,multiple:true
            	href "pageMQ", title: "Message Queue Options", description: mqDesc(), state: schMsgQue ? "complete" : null, image: parent.imgURL()+"mailbox.png"
                input "schWeather", "bool", title: "Turn On Weather Advisory Notification Feature", defaultValue:false, submitOnChange: true
            }
            if (schAction =="purge"){
            	input "schMsgQue", "enum", title: "Message Queue(s) To Purge...", options: parent.getMQListID(true), multiple:true, required: false, submitOnChange: true
                if (schMsgQue) paragraph "Please note: Purging message queues will run without output or notification.", image: parent.imgURL()+"info.png"
            }
            if (schAction =="macro"){
            	input "schMacro", "enum", title: "Macro To Run", options: parent.getMacroList("schedM",""), required: false, submitOnChange: true
                if (schMacro) paragraph "Please note: Macros will run without output or notification", image: parent.imgURL()+"info.png"
			}
		}	
		section("Other options", hideWhenEmpty: true){
            if (schType && schAction && schAction !="macro" && schAction !="purge"){
				input "schAppendPre", "text", title: "Append Text To Beginning Of Output", capitalization: "sentences", required:false, description: "You may use variables like %time% and %date%"
				input "schAppendPost", "text", title: "Append Additional Text To End Of Output", capitalization: "sentences", required:false, description: "You may use variables like %time% and %date%"
            }
            if (remindersOK()) href "pageReminder", title: "Action Reminders", description: remindDesc(), state: schRemind() ? "complete" : null, required:false, image: checkDate("remind").expired || checkDate("remind").warning ? parent.imgURL() + "caution.png" : null
        }
        if (schType ==~/complex|custom/){
        	section("Schedule restrictions", hideable: true, hidden: !(runDay || timeStart || timeEnd || runMode || runPeople || runSwitchActive || runSwitchNotActive)) {            
				input "runDay", "enum", options: parent.dayOfWeek(), title: "Only Certain Days Of The Week...",  multiple: true, required: false, image: parent.imgURL() + "calendar.png", submitOnChange: true
				href "timeIntervalInput", title: "Only During Certain Times...", description: parent.getTimeLabel(timeStart, timeEnd), state: (timeStart || timeEnd ? "complete":null), image: parent.imgURL() + "clock.png", submitOnChange: true
				input "runMode", "mode", title: "Only In The Following Modes...", multiple: true, required: false, image: parent.imgURL() + "modes.png", submitOnChange: true
                input "runPeople", "capability.presenceSensor", title: "Only When Present...", multiple: true, required: false, submitOnChange: true, image: parent.imgURL() + "people.png"
				if (runPeople && runPeople.size()>1) input "runPresAll", "bool", title: "Off=Any Present; On=All Present", defaultValue: false
            	input "runSwitchActive", "capability.switch", title: "Only When Switches Are On...", multiple: true, required: false, image: parent.imgURL() + "on.png"
				input "runSwitchNotActive", "capability.switch", title: "Only When Switches Are Off...", multiple: true, required: false, image: parent.imgURL() + "off.png"
            }
		}
		if ((schType ==~/single|bd/) ||  (schType =="simple" && schReType) || (schType =="complex" && schReType) || (schType=="custom" && cronValidate())) { 
            section ("Help"){
            	if (schType =="single"){
            		def extraTxtEnd1 = deleteExpired ? " In addition, this schedule will auto delete after the expiration time.":""
					paragraph "This schedule will trigger at the time and date you specify above. After that, the schedule will expire. ${extraTxtEnd1}"
        		}	
                else if (schType =="simple" && schReType){
                    def extraTxtEnd1 = schCount && deleteExpired ? " This schedule will auto delete after the maximum count is reached.":""
                    paragraph "This schedule will begin the recurrence interval you specified as soon as you tap \"Done\" above (if the status is \"On\"). Please note that if you are re-editing this schedule the old "+
                        "recurrence interval (and the current run count) will be reset upon tapping \"Done\" and the new schedule will be established.${extraTxtEnd1}"
                }
                else if (schType =="complex" && schReType){
                    def extraTxtStart = schStart() ? "after the \"Start Date\" you entered above" : "as soon as you tap \"Done\" above"
                    def extraTxtEnd1 = schEnd() ? "the time you specified in the \"End Date\" ": "you stop it "
                    if (schCount) extraTxtEnd1 +="or until you reach the maxium recurrence count of ${schCount}"
                    def extraTxtEnd2 = (schEnd() || schCount) && deleteExpired ? " This schedule will auto delete after the maximum count (if set) or the \"End Time/Date\" is reached.":""
                    def restrictions = runDay || timeStart || timeEnd || runMode || runPeople || runSwitchActive || runSwitchNotActive ? " and only within the parameters set in the restrictions area.": "."
                    paragraph "This schedule will begin the recurrence interval ${extraTxtStart} (if the status is \"On\"). The action will continue to recur until ${extraTxtEnd1}${restrictions}${extraTxtEnd2}"
                }
                else if (schType =="bd"){
                    def extraTxtStart = schStart() ? "after the \"Start Date\" you entered above" : "as soon as you tap \"Done\" above"
                    paragraph "This schedule will begin the annual recurrence ${extraTxtStart} (if the status is \"On\"). The action will continue to recur until you stop it."
                }
                else if (schType=="custom" && cronValidate()){
                    def extraTxtEnd1 = schCount ? " or until you reach the maxium recurrence count of ${schCount}":""
                    def extraTxtEnd2 = schCount ? " Please note that if you are re-editing this schedule the current run count will be reset upon tapping \"Done\".":""
                    def restrictions = runDay || timeStart || timeEnd || runMode || runPeople || runSwitchActive || runSwitchNotActive? " and only within the parameters set in the restrictions area.": "."
                    def extraTxtEnd3 = schCount && deleteExpired ? " This schedule will auto delete 2 minutes after the maximum count is reached.":""
                    paragraph "This custom schedule will recur at the interval in your cron expression${extraTxtEnd1}${restrictions}${extraTxtEnd2}" +
                        " For short recurrence cron intervals SmartThings may start the first occurance after a random time before repeating the subsequent intervals.${extraTxtEnd3}"
                }
        	}
        }
        section("Tap below to remove this schedule"){ }
        remove("Remove Schedule" + (app.label ? ": ${app.label}" : ""),"PLEASE NOTE","This action will only remove this schedule. Ask Alexa, other macros and extensions will remain.")
	
	}
}
page(name: "timeIntervalInput", title: "Only during a certain time") {
	section {
		input "timeStart", "time", title: "Starting", required: false
		input "timeEnd", "time", title: "Ending", required: false
	}
}
def pageEndDate(){
    dynamicPage(name: "pageEndDate", title: "Enter the full time and date"){
        section {
            def month=new Date(now()).format("M", location.timeZone)
            def day =new Date(now()).format("d", location.timeZone)
            def year = new Date(now()).format("yyyy", location.timeZone)
            input "schTimeEnd", "time", title: "Time Of Day", required: false
            input "schMonthEnd", "enum", title: "Month", options: monthList(), required: false, defaultValue: "${month}"
            input "schDayEnd", "number", title: "Day", range: "1..31", description: "Enter a day of the month between 1 and 31", required: false, defaultValue: day
            input "schYearEnd", "number", title: "Year", range: "2017..2022", description: "Enter a year between 2017 and 2022", required: false, defaultValue: year
        }
    }
}
def pageStartDate(){
    dynamicPage(name: "pageStartDate", title: "Enter the full time and date"){
        section {
            def month=new Date(now()).format("M", location.timeZone)
            def day =new Date(now()).format("d", location.timeZone)
            def year = new Date(now()).format("yyyy", location.timeZone)
            input "schTimeStart", "time", title: "Time Of Day", required: false
            input "schMonthStart", "enum", title: "Month", options: monthList(), required: false, defaultValue: "${month}"
            input "schDayStart", "number", title: "Day", range: "1..31", description: "Enter a day of the month between 1 and 31", required: false, defaultValue: day
            input "schYearStart", "number", title: "Year", range: "1900..2022", description: "Enter a year between 1900 and 2022", required: false, defaultValue: year
        }
    }
}
def pageCronExpression(){
    dynamicPage(name: "pageCronExpression", title: "Enter your cron parameters"){
        section {
            state.cronCheck = true
            input "cronSec", "text", title: "Second Parameter", required: false, description: "0-59 and * wildcards allowed", defaultValue: "0"
            input "cronMin", "text", title: "Minutes Parameter", required: false, description: "0-59 and , - * / wildcards allowed", defaultValue: "0"
            input "cronHour", "text", title: "Hours Parameter", required: false, description: "0-23 and , - * / wildcards allowed", defaultValue:"1/1"
            input "cronDayMon", "text", title: "Day-Of-Month Parameter", required: false, description: "1-31 and , - * ? / L W wildcards allowed",  defaultValue:"*"
            input "cronMon", "text", title: "Month Parameter", required: false, description: "1-12 or JAN-DEC and , - * /  wildcards allowed", defaultValue:"*"
            input "cronDayWeek", "text", title: "Day-Of-The-Week Parameter", required: false, description: "1-7 or SUN-SAT and , - * ? / L wildcards allowed", defaultValue:"?"
            input "cronYear", "number", title:"Year Parameter (Optional)", range:"1900..2022", required: false, description: "1900-2022 and , - * / wildcards allowed"
        }
    }
}
def pageCronExpressionRemind(){
    dynamicPage(name: "pageCronExpressionRemind", title: "Enter your cron parameters"){
        section {
            state.cronCheckRemind = true
            input "cronSecRemind", "text", title: "Second Parameter", required: false, description: "0-59 and * wildcards allowed", defaultValue: "0"
            input "cronMinRemind", "text", title: "Minutes Parameter", required: false, description: "0-59 and , - * / wildcards allowed", defaultValue: "0"
            input "cronHourRemind", "text", title: "Hours Parameter", required: false, description: "0-23 and , - * / wildcards allowed", defaultValue:"1/1"
            input "cronDayMonRemind", "text", title: "Day-Of-Month Parameter", required: false, description: "1-31 and , - * ? / L W wildcards allowed",  defaultValue:"*"
            input "cronMonRemind", "text", title: "Month Parameter", required: false, description: "1-12 or JAN-DEC and , - * /  wildcards allowed", defaultValue:"*"
            input "cronDayWeekRemind", "text", title: "Day-Of-The-Week Parameter", required: false, description: "1-7 or SUN-SAT and , - * ? / L wildcards allowed", defaultValue:"?"
            input "cronYearRemind", "number", title:"Year Parameter (Optional)", range:"1900..2022", required: false, description: "1900-2022 and , - * / wildcards allowed"
        }
    }
}
def pageMQ(){
    dynamicPage(name:"pageMQ"){
        section {
        	paragraph "Message Queue Configuration", image:parent.imgURL()+"mailbox.png"
        }
        section (" "){
            input "schMsgQue", "enum", title: "Message Queue Recipient(s)...", options: parent.getMQListID(true), multiple:true, required: false, submitOnChange: true
            input "schMQNotify", "bool", title: "Notify Only Mode (Not Stored In Queue)", defaultValue: false, submitOnChange: true
            if (!schMQNotify) input "schMQExpire", "number", title: "Message Expires (Minutes)", range: "1..*", required: false, submitOnChange: true
            if (!schMQNotify && !schMQExpire) input "schMQOverwrite", "bool", title: "Overwrite Other Schedule Messages", defaultValue: false
            if (!schMQNotify) input "schSuppressTD", "bool", title: "Suppress Time/Date From Alexa Playback", defaultValue: false
        }
    }
}
def pageReminder(){
    dynamicPage(name: "pageReminder", title: "Enter Reminder Schedule"){
        def extraTxt = schAction ==~/purge|macro/ ? " (Required)" : "\n(If empty will default to action's message queue(s))"
        def hidden =(schAction !="purge" && schAction !="macro") && (!(schMsgQueRemind || schMQNotifyRemind || (!schMQNotifyRemind && schMQExpireRemind) || !schMQNotifyRemind && !schMQExpireRemind && schMQOverwriteRemind)) ? true : false
        section {
            if (schType != "custom"){
                input "schTimingRemind", "enum", title: "Remind Prior To Action",required: false, description: "Choose the time prior to the action to be reminded", options: reTime(), submitOnChange: true 
                if (schTimingRemind && (schTimingRemind as int) < 15 || schTimingRemind=="99" ){
                    input "schTimeRemind", "time", title: "Reminder Time Of Day", required: false, description: "Default is current time the schedule is saved"
                }
                if (schTimingRemind && (schTimingRemind as int) < 15){
                    input "schRemindInterval", "enum", title: "Reminder Interval (After Initial Reminder)", options: ["None", "Hourly", "Daily at the same time"], required: false, defaultValue: "None"
                }
            }
            else href "pageCronExpressionRemind", title: "Cron Expression Reminder", description: cronDescRemind() , state: cronValidateRemind() ? "complete" : null, image: state.cronCheckRemind ? null :parent.imgURL()+"caution.png"
            
            input "schRemindText", "text", title: "Reminder Text", required: false, description: "If blank, a default will be used", capitalization: "sentences"
			input "schRemindFollow", "bool", title: "Reminder Utilizes Action's On/Off Status & Restrictions", defaultValue: false        
        }
        section ("Reminder message queue${extraTxt}", hideable:true, hidden: hidden ){
			input "schMsgQueRemind", "enum", title: "Message Queue Recipient(s)...", options: parent.getMQListID(true), multiple:true, required: false, submitOnChange: true
			input "schMQNotifyRemind", "bool", title: "Notify Only Mode (Not Stored In Queue)", defaultValue: false, submitOnChange: true
			if (!schMQNotifyRemind) input "schMQExpireRemind", "number", title: "Message Expires (Minutes)", range: "1..*", required: false, submitOnChange: true
			if (!schMQNotifyRemind && !schMQExpireRemind) input "schMQOverwriteRemind", "bool", title: "Overwrite Other Reminder Messages", defaultValue: false
            if (!schMQNotifyRemind) input "schSuppressTDRemind", "bool", title: "Suppress Time/Date From Alexa Reminder Playback", defaultValue: false
        }
	}
}
def toggleStatus(){
	state.status = state.status ? false : true
    mainPage()
}
def installed() {
    initialize()
}
def updated() {
	unsubscribe()
    unschedule()
    initialize()
}
def initialize() {
	state.runCount = 0
    state.runDate = ""
    state.reminderInterval=false
    state.deleteSch = false
    fillStartTime()
    if (getStatus()==~/On|Off/){
        if (schType=="single"){
        	def dateTime="${schYearEnd}-${schMonthEnd}-${schDayEnd}${schTimeEnd[10..27]}"
        	runOnce (dateTime, doAction)
            if (remindersOK() && checkDate("remind").result) scheduleRemindTime()
    	}
        if (schType==~/simple|bd/ || (schType=="complex" && (!schStart() || (schStart() && checkDate("start").expired)))) {
        	createSchd()
            if (remindersOK() && checkDate("remind").result) scheduleRemindTime()
        }
        else if (schType=="complex" && schStart() && !checkDate("start").expired){
        	runOnce ("20${state.startTimeDate.Year}-${state.startTimeDate.Month}-${state.startTimeDate.Day}${schTimeStart[10..27]}", runFirstTime)
            if (remindersOK() && !checkDate("remind").expired && checkDate("remind").result && schTimingRemind!="99"){
                def epDay = (schTimingRemind as int) < 15 ? 86400000 * (schTimingRemind as int) : 60000 * (schTimingRemind as int)
    			def epDT= new Date().parse("yyyy-MM-dd'T'HH:mm:ss.SSSZ", "20${state.startTimeDate.Year}-${state.startTimeDate.Month}-${state.startTimeDate.Day}${schTimeRemind ? schTimeRemind[10..27] : schTimeStart[10..27]}").getTime()
    			def remindTime = epDT-epDay
                runOnce (new Date(remindTime).format("yyyy-MM-dd'T'HH:mm:ss.SSSZ", location.timeZone), remindFirstTime)
    		}
            else if ((schTimingRemind=="99" && remindersOK() && schRemind()) || checkDate("remind").warning) scheduleRemindTime()
        }
		if (schType=="complex" && schEnd()){
            if (!checkDate("end").expired) runOnce ("${schYearEnd}-${schMonthEnd}-${schDayEnd}${schTimeEnd[10..27]}", unschRecur)
            else unschedule()
        }
        if (schType=="custom") {
        	try { 
            	if (cronValidate()) schedule (cronDesc(), doAction) 
                state.cronCheck = true
            }
            catch (e) {
            	state.cronCheck=false
                log.warn "Your cron expression is invalid. No schedules were set"
			}
            if (cronValidateRemind() && remindersOK() && schRemind()){
                try { 
                    schedule (cronDescRemind(), doPrimaryReminder) 
                    state.cronCheckRemind = true
                }
                catch (e) {
                    state.cronCheckRemind =false
                    log.warn "Your cron expression is invalid. No reminder schedules were set"
                }
            }	
        }
	} 
}
def runFirstTime(){
	createSchd()
    doAction()
}
def remindFirstTime(){
    scheduleRemindTime()
    if (schTimingRemind != "99") doPrimaryReminder()
}
def doAction(){
    if (getStatus()=="On" && ((schType==~/complex|custom/ && getOkToRun()) || schType==~/simple|single|bd/) ){
		String outputTxt
        if (schAction=="msg") {
        	outputTxt = schAppendPre ? schAppendPre + " ": ""
            outputTxt += schAppendPost ? schMsgTxt + schAppendPost: schMsgTxt
            sendMsg(outputTxt)
        }
         if (schAction=="macro") {
			parent.processMacroAction(schMacro.toLowerCase().replaceAll("[^a-zA-Z0-9 ]", ""), "undefined", "undefined", false,"undefined")
        }
        if (schAction=="VR") {
        	outputTxt = schAppendPre ? schAppendPre + " " : ""
            outputTxt += parent.processOtherRpt(schVR)
            if (outputTxt.endsWith("%")) result = result[0..-4]
            outputTxt += schAppendPost ? schAppendPost : ""
        	sendMsg(outputTxt)
        }
        if (schAction=="WR") {
        	outputTxt = schAppendPre ? schAppendPre+ " ": ""
        	outputTxt += processOtherRpt(schWR)
            if (outputTxt.endsWith("%")) result = result[0..-4]
            outputTxt += schAppendPost ? schAppendPost: ""
            if ((schWeather && outputTxt.contains("ALERT!")) || !schWeather) sendMsg(outputTxt)
        }
        if (schAction=="purge") {
        	if (schMsgQue.contains("Primary Message Queue")) parent.qDelete()
            parent.childQDelete(schMsgQue)
        }
        log.info  "Schedule '${app.label}' was triggered."
        if (schCount){
            def currCount=state.runCount as int
            currCount ++
            state.runCount = currCount
    	}
	}
    else log.warn "Schedule '${app.label}' did not run because it is off, invalid, expired, has used up its allocated run counts, or had restrictions placed on it."
    if (checkDate("endTrue").expired || getStatus()=="Expired") unschRecur()
    state.reminderInterval=false
}
def doPrimaryReminder(){
	if (schType !="custom"){
    	def epDay =(schTimingRemind as int) < 15 ? 86400000 * (schTimingRemind as int) : 60000 * (schTimingRemind as int)
        def epDate = now() + epDay
        def month  =new Date(epDate).format("M", location.timeZone) 
        def day = new Date(epDate).format("d", location.timeZone)
        def year = new Date(epDate).format("yy", location.timeZone)
        state.runDate = "${month}/${day}/${year}"
        if (schRemindInterval=="None" || (schTimingRemind as int) >14 || ((schTimingRemind as int) <15 && schRemindInterval=="None") ) { 
            def runTime = schType =="single" ? "${parent.timeParse("${schTimeEnd}", "h:mm a")}" : "${parent.parseDate("${state.startTimeDate.Epoch}", "h:mm a")}"
			def reminderTxt = schRemindText ? schRemindText : "This is a reminder that the schedule, '${app.label}, will run at ${runTime} on ${schMonthEnd}/${schDayEnd}/${schYearEnd.toString()[2..3]}. "  
            sendReminder(reminderTxt)
        }
        else {
            state.reminderInterval = true
            doReminderInt()
        }
	}
    else{
    	def reminderTxt = schRemindText ? schRemindText : "This is a reminder that the schedule, '${app.label}', is scheduled to run on the cron schedule set up within the SmartApp. "
        sendReminder(reminderTxt)
    }
}
def progressiveReminderWeek(){
	def runTime = schType =="single" ? "${parent.timeParse("${schTimeEnd}", "h:mm a")}" : "${parent.parseDate("${state.startTimeDate.Epoch}", "h:mm a")}"
    def reminderTxt = schRemindText ? schRemindText : "This is a reminder that the schedule, '${app.label}', will run next ${state.startTimeDate.DayFull} at ${runTime}. "
    sendReminder(reminderTxt)
}
def progressiveReminderDay(){
	def runTime = schType =="single" ? "${parent.timeParse("${schTimeEnd}", "h:mm a")}" : "${parent.parseDate("${state.startTimeDate.Epoch}", "h:mm a")}"
    def reminderTxt = schRemindText ? schRemindText : "This is a reminder that the schedule, '${app.label}', will run in tomorrow at ${runTime}. "
    sendReminder(reminderTxt)
}
def progressiveReminderHour(){
	def reminderTxt = schRemindText ? schRemindText : "This is a reminder that the schedule, '${app.label}', will run in one hour. "
    sendReminder(reminderTxt)
}
def progressiveReminderQtrHr(){
	def reminderTxt = schRemindText ? schRemindText: "This is a reminder that the schedule, '${app.label}', will run in 15 min. "
    sendReminder(reminderTxt)
}
def doReminderInt(){
	if (state.reminderInterval){
    	def reminderTxt = schRemindText ? schRemindText : "This is a reminder that the schedule, '${app.label}', will run on ${state.runDate} at ${parent.parseDate("${state.startTimeDate.Epoch}", "h:mm a")}. "
    	sendReminder(reminderTxt)
        def runInMin = schRemindInterval=="Hourly" ? 3600 : 86400
    	runIn (runInMin, doReminderInt)
 	}       
}
def unschRecur(){ 
	unschedule() 
	log.info "The schedule, '${app.label}', has expired."
    if (deleteExpired) delete()
}
//Create recurring schedules
def createSchd(){	
    if (schType !="bd"){
        if (schReType ==~/1|5|10|15|30|60|180|360|720/){
            def min =schReType as int
            if (min< 60) runEveryXMinutes(min, doAction)
            else runEveryXHours(min/60 as int, doAction)
        }
        else {
            if (schReType=="day") { schedule("0 ${state.startTimeDate.Min} ${state.startTimeDate.Hour} 1/1 * ?", doAction) }
            else if (schReType=="week") { schedule("0 ${state.startTimeDate.Min} ${state.startTimeDate.Hour} ? * ${state.startTimeDate.DayNum}", doAction) }
            else if (schReType=="month") { schedule("0 ${state.startTimeDate.Min} ${state.startTimeDate.Hour} ${state.startTimeDate.Day} 1/1 ?", doAction) }
            else if (schReType=="year") { schedule("0 ${state.startTimeDate.Min} ${state.startTimeDate.Hour} ${state.startTimeDate.Day} ${state.startTimeDate.Month} ? *", doAction) }
        }
    }
    else if (schType=="bd")  { schedule("0 ${state.startTimeDate.Min} ${state.startTimeDate.Hour} ${state.startTimeDate.Day} ${state.startTimeDate.Month} ? *", doAction) }
}
def scheduleRemindTime(){
	def epDT, epDay, remindTime
    if (schTimingRemind != "99") {
        epDay = (schTimingRemind as int) < 15 ? 86400000 * (schTimingRemind as int) : 60000 * (schTimingRemind as int)
        if (schType=="single") epDT= schTimeRemind ? new Date().parse("yyyy-MM-dd'T'HH:mm:ss.SSSZ", "${schYearEnd}-${schMonthEnd}-${schDayEnd}${schTimeRemind[10..27]}").getTime():
        	new Date().parse("yyyy-MM-dd'T'HH:mm:ss.SSSZ", "${schYearEnd}-${schMonthEnd}-${schDayEnd}${schTimeEnd[10..27]}").getTime()
        else if (schType==~/simple|complex|bd/) {
            epDT=schTimeRemind ? new Date().parse("yyyy-MM-dd'T'HH:mm:ss.SSSZ", "20${state.startTimeDate.Year}-${state.startTimeDate.Month}-${state.startTimeDate.Day}${schTimeRemind[10..27]}").getTime() : 
            	state.startTimeDate.Epoch
        }
        remindTime = epDT-epDay
        if (schType=="single") runOnce (new Date(remindTime).format("yyyy-MM-dd'T'HH:mm:ss.SSSZ", location.timeZone), doPrimaryReminder)
        else if (schType ==~/simple|complex|bd/){
            def hour=new Date(remindTime).format("HH", location.timeZone)
            def minutes=new Date(remindTime).format("mm", location.timeZone)
            def month=new Date(remindTime).format("MM", location.timeZone) 
            def day =new Date(remindTime).format("dd", location.timeZone)
            def dayNum=new Date(remindTime).format("u", location.timeZone) as int
            def year = new Date(remindTime).format("yy", location.timeZone)
            if (schReType=="week") { schedule("0 ${minutes} ${hour} ? * ${dayNum<7 ? dayNum+1 : 1}", doPrimaryReminder) }
            else if (schReType=="month") { schedule("0 ${minutes} ${hour} ${day} 1/1 ?", doPrimaryReminder) }
            else if (schReType=="year" || schType=="bd") { schedule("0 ${minutes} ${hour} ${day} ${month} ? *", doPrimaryReminder) }
        }
    }
    else {
    	if (schType=="single") remindTime=schTimeRemind ? "${schYearEnd}-${schMonthEnd}-${schDayEnd}${schTimeRemind[10..27]}" :"${schYearEnd}-${schMonthEnd}-${schDayEnd}${schTimeEnd[10..27]}"
        def dateTime = schType=="single" ? new Date().parse("yyyy-MM-dd'T'HH:mm:ss.SSSZ",remindTime).getTime() : state.startTimeDate.Epoch
        def epWeek=dateTime - (7 * 86400000)
        epDay=dateTime - 86400000
        def epHour=dateTime - (60000 * 60)
        def epQHr=dateTime - (60000 * 15)
        epDT= schTimeRemind ? new Date().parse("yyyy-MM-dd'T'HH:mm:ss.SSSZ", schTimeRemind).getTime() : state.startTimeDate.Epoch
        if (schType=="single"){
        	if (schTimeRemind) {
            	epHour= new Date().parse("yyyy-MM-dd'T'HH:mm:ss.SSSZ","${schYearEnd}-${schMonthEnd}-${schDayEnd}${schTimeEnd[10..27]}").getTime() - (60000 * 60)
            	epQHr=new Date().parse("yyyy-MM-dd'T'HH:mm:ss.SSSZ","${schYearEnd}-${schMonthEnd}-${schDayEnd}${schTimeEnd[10..27]}").getTime() - (60000 * 15)
            }
        	if (epWeek>now()) runOnce (new Date(epWeek).format("yyyy-MM-dd'T'HH:mm:ss.SSSZ", location.timeZone), progressiveReminderWeek)
            if (epDay>now()) runOnce (new Date(epDay).format("yyyy-MM-dd'T'HH:mm:ss.SSSZ", location.timeZone), progressiveReminderDay)
            if (epHour>now())runOnce (new Date(epHour).format("yyyy-MM-dd'T'HH:mm:ss.SSSZ", location.timeZone), progressiveReminderHour)
            if (epQHr>now())runOnce (new Date(epQHr).format("yyyy-MM-dd'T'HH:mm:ss.SSSZ", location.timeZone), progressiveReminderQtrHr)
        }
        else{
            if (schReType=="year"){
                schedule("0 ${new Date(epDT).format("mm", location.timeZone)} ${new Date(epDT).format("HH", location.timeZone)} ${new Date(epWeek).format("dd", location.timeZone)} ${new Date(epWeek).format("MM", location.timeZone)} ? *", progressiveReminderWeek)
                schedule("0 ${new Date(epDT).format("mm", location.timeZone)} ${new Date(epDT).format("HH", location.timeZone)} ${new Date(epDay).format("dd", location.timeZone)} ${new Date(epDay).format("MM", location.timeZone)} ? *", progressiveReminderDay)
                schedule("0 ${new Date(epHour).format("mm", location.timeZone)} ${new Date(epHour).format("HH", location.timeZone)} ${new Date(epHour).format("dd", location.timeZone)} ${new Date(epHour).format("MM", location.timeZone)} ? *", progressiveReminderHour)
                schedule("0 ${new Date(epQHr).format("mm", location.timeZone)} ${new Date(epQHr).format("HH", location.timeZone)} ${new Date(epQHr).format("dd", location.timeZone)} ${new Date(epQHr).format("MM", location.timeZone)} ? *", progressiveReminderQtrHr)
            }
            else if (schReType=="month"){
                schedule("0 ${new Date(epDT).format("mm", location.timeZone)} ${new Date(epDT).format("HH", location.timeZone)} ${new Date(epWeek).format("dd", location.timeZone)} 1/1 ?", progressiveReminderWeek)
                schedule("0 ${new Date(epDT).format("mm", location.timeZone)} ${new Date(epDT).format("HH", location.timeZone)} ${new Date(epDay).format("dd", location.timeZone)} 1/1 ?", progressiveReminderDay)
                schedule("0 ${new Date(epHour).format("mm", location.timeZone)} ${new Date(epHour).format("HH", location.timeZone)} ${new Date(epHour).format("dd", location.timeZone)} 1/1 ?", progressiveReminderHour)
                schedule("0 ${new Date(epQHr).format("mm", location.timeZone)} ${new Date(epQHr).format("HH", location.timeZone)} ${new Date(epQHr).format("dd", location.timeZone)} 1/1 ?", progressiveReminderQtrHr)
            }
            else if (schReType=="week") { 
                schedule("0 ${new Date(epDT).format("mm", location.timeZone)} ${new Date(epDT).format("HH", location.timeZone)} ? * ${(new Date(epWeek).format("u", location.timeZone) as int) <7 ? (new Date(epWeek).format("u", location.timeZone) as int) +1 : 1}", progressiveReminderWeek)
                schedule("0 ${new Date(epDT).format("mm", location.timeZone)} ${new Date(epDT).format("HH", location.timeZone)} ? * ${(new Date(epWeek).format("u", location.timeZone) as int) <7 ? (new Date(epWeek).format("u", location.timeZone) as int) +1 : 1}", progressiveReminderDay)
                schedule("0 ${new Date(epHour).format("mm", location.timeZone)} ${new Date(epHour).format("HH", location.timeZone)} ? * ${(new Date(epWeek).format("u", location.timeZone) as int) <7 ? (new Date(epWeek).format("u", location.timeZone) as int) +1 : 1}", progressiveReminderHour)
                schedule("0 ${new Date(epQHr).format("mm", location.timeZone)} ${new Date(epQHr).format("HH", location.timeZone)} ? * ${(new Date(epWeek).format("u", location.timeZone) as int) <7 ? (new Date(epWeek).format("u", location.timeZone) as int) +1 : 1}", progressiveReminderQtrHr)
            }
        }
    }
}
//Actions
def sendMsg(outputTxt){
	if (outputTxt){
        def msgTxt = parent.replaceVoiceVar(outputTxt, "", "", "Schedules", app.label,ageCalc(),"")
        def expireMin=schMQExpire ? schMQExpire as int : 0, expireSec=expireMin*60
        def overWrite =!schMQNotify && !schMQExpire && schMQOverwrite
        sendLocationEvent(
            name: "AskAlexaMsgQueue", 
            value: "Ask Alexa Schedule, '${app.label}'",
            unit: "${app.id}",
            isStateChange: true, 
            descriptionText: msgTxt, 
            data:[
                queues:schMsgQue,
                overwrite: overWrite,
                notifyOnly: schMQNotify,
                expires: expireSec,
                suppressTimeDate:schSuppressTD   
            ]
        )
	}
    else log.info "The scheduled task produced no output. Nothing sent to the message queue(s)."
}
def sendReminder(outputTxt){
	if (!schRemindFollow || (schRemindFollow && (getStatus()=="On" && ((schType==~/complex|custom/ && getOkToRun()) || schType==~/simple|single|bd/)))){
        def msgTxt = parent.replaceVoiceVar(outputTxt, "", "", "Schedules", app.label,ageCalc(),"")
        def expireMin=schMQExpireRemind ? schMQExpireRemind as int : 0, expireSec=expireMin*60
        def overWrite =!schMQNotifyRemind && !schMQExpireRemind && schMQOverwriteRemind
        def remindMQ = schMsgQueRemind ?: schMsgQue
        sendLocationEvent(
            name: "AskAlexaMsgQueue", 
            value: "Ask Alexa Schedule Reminder, '${app.label}'",
            unit: "${app.id}",
            isStateChange: true, 
            descriptionText: msgTxt, 
            data:[
                queues:remindMQ,
                overwrite:overWrite,
                notifyOnly:schMQNotifyRemind,
                expires:expireSec,
                suppressTimeDate:schSuppressTDRemind
            ]
        )
	}
    else log.warn "Reminders for the schedule, '${app.label}', did not send output because the main schedule is off, invalid, expired, has used up its allocated run counts, or had restrictions placed on it."
}
//Called from parent app
def getSchdDesc(){
	def result ="The schedule, '${app.label}', ", mqCount = schMsgQue?.size()>1 ? "queues" : "queue", schStatus = getStatus(), reType= reType()[schReType]?:schReType
    def DOW=schReType=="week" ? " on ${state.startTimeDate.DayFull}s" : "", sTOD=" at ${new Date().parse("HHmm", "${state.startTimeDate.Hour}${state.startTimeDate.Min}").format("h:mm a")}"
    def DOM=schReType==~/month|year/ ? " on the ${dayInd(state.startTimeDate.Day)}" :"", month=schReType=="year" ? " of ${state.startTimeDate.MonthFull}" : ""
    if (schStatus==~/On|Off/) result += schType=="single" ? 
    	"is a single-occurance event " : schType=="simple" ? 
        	"is a simple-recurrence " : schType=="complex" ? 
            	"is a complex-recurrence event " : schType=="custom" ?
                	"is a custom cron schedule " : "is a birthday or anniversary event "
    if (schStatus=="On"){
        if (schType=="single") result += "and is schedule to run at ${parent.timeParse("${schTimeEnd}", "h:mm a")} on ${schMonthEnd}/${schDayEnd}/${schYearEnd.toString()[2..3]}. At this time, "
        else if (schType=="simple") result += "running ${reType.toLowerCase()}${DOM}${month}${DOW}${sTOD}. When the schedule triggers, "
        else if (schType=="complex"){
            def starts = "This schedule started"
            if (schStart() && !checkDate("start").expired) starts=" This schedule will begin"
            result += "that runs ${reType.toLowerCase()}${DOW}. ${starts} ${sTOD} on ${state.startTimeDate.Month}/${state.startTimeDate.Day}/${state.startTimeDate.Year}, "
            result += schEnd() ? "and will end after ${parent.timeParse("${schTimeEnd}", "h:mm a")} on ${schMonthEnd}/${schDayEnd}/${schYearEnd.toString()[2..3]}. " : ""
            result += "At the scheduled time, "
        }
        else if (schType=="bd") result +="that runs every year${sTOD} on ${state.startTimeDate.MonthFull} ${dayInd(state.startTimeDate.Day)}. At the scheduled time, "
        else if (schType=="custom") result +="that recurs based on a cron expression you entered. When the schedule triggers, "
        result += schAction=="macro" ? "the macro, '${schMacro}' will run. " : ""
        result += schAction=="msg" ? "a text message will be sent to the message ${mqCount}. " : ""
        result += schAction=="VR" ? "a voice report will be sent to the message ${mqCount}. ":""
        result += schAction=="WR" ? "a weather report will be sent to the message ${mqCount}. ":""
        result += schAction=="WR" && schWeather ? "The weather advisory feature is turned on for this report, and will only notify when the report is an advisory. " : ""
        result += schAction=="purge" ? "the message ${schMsgQue} will purge. ":""
        if (schType=="complex" && (runDay || timeStart || timeEnd || runMode || runPeople || runSwitchActive || runSwitchNotActive)) result += "In addition, this schedule has restrictions applied that will limit when it recurs. "
    	if (schType==~/simple|complex/ && schCount) result +="Finally, there is a maximum number of times this schedule will run. Right now you have used ${state.runCount} of ${schCount}. "
    }
    else if (schStatus=="Off") result += "and is currently off. "
    else if (schStatus=="Incomplete") result += "is incomplete. Use your SmartApp to complete the configuration of this schedule. "
    else if (schStatus=="Invalid") result += "is invalid as it has a date associated with it that is incorrect. Use your smart app to complete the configuration of this schedule. "
    else if (schStatus=="Expired" && ((schCount && state.runCount < schCount) || schType=="single")) result += "expired at ${parent.timeParse("${schTimeStart}", "h:mm a")} on ${monthEnd} ${schDayEnd}, ${schYearEnd}. "
    else if (schStatus=="Expired" && schCount && state.runCount >= schCount) result += "expired after reaching the maximum run count of ${schCount}. "
    else if (schStatus=~/Invalid start/) result += "is invalid because your end time and date is before the start time and date. Please correct this within your Ask Alexa SmartApp. "
	if (deleteExpired && !state.deleteSch) result += "Please note: This schedule will auto-delete when it expires. "
    if (state.deleteSch) result += "Please note: This schedule is going to be deletes soon unless you cancel that action. "
    result +="%2%"
    return result
}
def toggle(){
	def schStatus = getStatus(), result
    if (schStatus==~/On|Off/) {
    	state.status = state.status ? false : true
        result = "I have toggled the status of the schedule, '${app.label}', to ${state.status ? "on" : "off"}. %4%"
	}
    else result = "I am unable to change the status of the schedule as it is expired, invalid or incomplete. Use the Ask Alexa SmartApp to fix the schedule. %1%"
}
def onOff(cmd){
	String result = cmd=="on" && state.status  ? "The schedule, '${app.label}', is already on. %1%":  cmd=="off" && !state.status ? "The schedule, '${app.label}', is already off. %1%" : ""
    if (!result) {
    	state.status = cmd=="on"
        result = "I have turned ${cmd} the schedule, '${app.label}'. %4%"
    }
    return result
}
def getShortDesc(){
	def result, reType= reType()[schReType]?:schReType, sTOD=" at ${new Date().parse("HHmm", "${state.startTimeDate.Hour}${state.startTimeDate.Min}").format("h:mm a")}"
	if (schType=="single") {
    	def extra = getStatus()=="Expired " ? "" : "once "
    	result ="${extra}on ${schMonthEnd}/${schDayEnd}/${schYearEnd.toString()[2..3]} at ${parent.timeParse("${schTimeEnd}", "h:mm a")}"
    }
    if (schType==~/simple|complex/) {
        result = "${reType.toLowerCase()}"
     	if (schReType=="week") result += " on ${state.startTimeDate.DayFull}s"
        if (schReType=="month") result +=" on the ${dayInd(state.startTimeDate.Day)}"
        if (schType=="simple" && schReType=="year") result+=" on the ${dayInd(state.startTimeDate.Day)} of ${state.startTimeDate.MonthFull}"
    	if (schType=="simple") result += "${sTOD}"
    }
    if (schType=="complex"){
        result += " starting on ${state.startTimeDate.Month}/${state.startTimeDate.Day}/${state.startTimeDate.Year}${sTOD}"
		result += schEnd() ? " and ends at ${parent.timeParse("${schTimeEnd}", "h:mm a")} on ${schMonthEnd}/${schDayEnd}/${schYearEnd.toString()[2..3]}" : ""
	}
    if (schType=="bd") result = "annually starting on ${state.startTimeDate.Month}/${state.startTimeDate.Day}/${state.startTimeDate.Year}${sTOD}"
    if (schType=="custom"){
    	result ="on a cron expression of ${cronSec} ${cronMin} ${cronHour} ${cronDayMon} ${cronMon} ${cronDayWeek}"
        if (cronYear) result +=" ${cronYear}"
	}
    if (result) result +=". " 
    if (remindersOK() && !checkDate("remind").expired && checkDate("remind").result) result +="Reminders for this action are active. " 
    if (getStatus()=="Expired" && schCount && state.runCount >= schCount) result = "after reaching the maximum run count of ${schCount}. "
    else if (getStatus() =="On" && schCount && state.runCount < schCount) result += "Schedule has run ${state.runCount} of ${schCount} times. "
    if ((runDay || timeStart || timeEnd || runMode || runPeople || runSwitchActive || runSwitchNotActive) &&  (schType==~/complex|custom/)) result +="Restrictions applied that may affect recurrance. "
    if (deleteExpired) result += "Will auto-delete when it expires. "
    return result
}
def deleteSch(){
    def deleteTime = parent.schDeleteTime ? parent.schDeleteTime as int : 2
    def result = "Scheduling the deletion of, '${app.label}'. You have ${deleteTime} minutes to cancel this command. Simply edit the schedule via the SmartApp and press 'Done' to cancel the deletion, or say 'cancel ${app.label} deletion.'. "
    log.info result
    state.deleteSch = true
    runIn(deleteTime*60, deleteMe)
    return result + "%2%"
}
def deleteMe(){ parent.deleteChild(app.id) }
def notDeleteSch(){
	def result = "The schedule, '${app.label}', is not slated for deletion. No action is being taken. %1%"
    if (state.deleteSch){
    	result = "I am unscheduling, '${app.label}', from deletion. %2%"
        unschedule(deleteMe)
        state.deleteSch = false
    }
	return result
}
//Common Code
def ageCalc(){
	Integer year = new Date(now()).format("yyyy", location.timeZone) as int
    return schYearStart && (year - (schYearStart as int)) > 0 ? year - (schYearStart as int) : 0 
}
def dayInd(num){
	def result
	if (num=="1") result ="1st"
    else if (num=="2") result ="2nd"
    else if (num=="3") result ="3rd"
    else result = "${num}th"
    return result
}
def fillStartTime(){
	def epDT, year, month, day, dayNum, hour, minutes, dayFull, monthFull
	if ((schType=="complex" && schStart() && schReType==~/day|week|month|year/) || schType=="bd") {
		def dateTime="${schYearStart}-${schMonthStart}-${schDayStart}${schTimeStart[10..27]}"
        epDT=new Date().parse("yyyy-MM-dd'T'HH:mm:ss.SSSZ", dateTime).getTime()
	}
	else epDT = now()
	hour=new Date(epDT).format("HH", location.timeZone)
    minutes=new Date(epDT).format("mm", location.timeZone)
    month=new Date(epDT).format("M", location.timeZone)
    monthFull=new Date(epDT).format("MMMM", location.timeZone)
    day =new Date(epDT).format("d", location.timeZone)
    dayNum= new Date(epDT).format("u", location.timeZone) as int
    dayFull = new Date(epDT).format("EEEE", location.timeZone)
    year = new Date(epDT).format("yy", location.timeZone) 
    state.startTimeDate =["Epoch":epDT,"Year":year, "Month":month, "MonthFull":monthFull,"Day":day, "DayNum":dayNum<7 ? dayNum+1: 1, "DayFull": dayFull, "Hour":hour, "Min": minutes]
}
def getOkToRun(){ def result = (!runMode || runMode.contains(location.mode)) && parent.getDayOk(runDay) && parent.getTimeOk(timeStart,timeEnd) && parent.getPeopleOk(runPeople,runPresAll && switchesOnStatus() && switchesOffStatus()) }
private switchesOnStatus(){ return runSwitchActive && runSwitchActive.find{it.currentValue("switch") == "off"} ? false : true }
private switchesOffStatus(){ return runSwitchNotActive && runSwitchNotActive.find{it.currentValue("switch") == "on"} ? false : true }
def remindersOK() {return getStatus()==~/On|Off/ && ((schType==~/simple|complex/ && schReType==~/week|month|year/) || (schType=="single" && checkDate("end").result && !checkDate("end").expired) || schType==~/custom|bd/) } 
def checkDate(type){
	def result=true, warning=false, expired = false, dateOrder=true
    if (type=="end" || type=="endTrue"){
        if (!schEnd()) result = false
        else{
            def day=schDayEnd as int, month = schMonthEnd as int, year = schYearEnd as int, dateTime="${schYearEnd}-${schMonthEnd}-${schDayEnd}${schTimeEnd[10..27]}"
            if (day == 31 && month ==~/2|4|6|9|11/) { result = false; warning=true}
            if ((day >28 && month ==2 && year  !=2020) || (day >29 && month ==2 && year ==2020))  { result = false; warning=true}
            if (now() > (new Date().parse("yyyy-MM-dd'T'HH:mm:ss.SSSZ", dateTime).getTime())+30000) expired = true
            if (type=="endTrue" && now() > (new Date().parse("yyyy-MM-dd'T'HH:mm:ss.SSSZ", dateTime).getTime())) expired = true 
    	}
    }
    else if (type=="start"){
    	if (!schStart()) result = false
        else{
            def day=schDayStart as int, month = schMonthStart as int, year = schYearStart as int, dateTime="${schYearStart}-${schMonthStart}-${schDayStart}${schTimeStart[10..27]}"
            if (day == 31 && month ==~/2|4|6|9|11/) { result = false; warning=true}
            if ((day >28 && month ==2 && year  !=2020) || (day >29 && month ==2 && year ==2020))  { result = false; warning=true}
            if (now() > new Date().parse("yyyy-MM-dd'T'HH:mm:ss.SSSZ", dateTime).getTime()) expired = true
        }
    } 
    else if (type=="remind" && schType != "custom"){
        if (!schRemind()) result = false
        else{
        	def epDay=86400000, epDT, epTime
            if (schTimingRemind != "99") epDay=(schTimingRemind as int) < 15 ? 86400000 * (schTimingRemind as int) : 60000 * (schTimingRemind as int)
            else epDay = 60000 * 15
            if (((schTimingRemind as int) >15 && schTimingRemind !="99") || (schTimingRemind =="99" && !schTimeRemind) || !schTimeRemind) epTime=new Date(now()).format("yyyy-MM-dd'T'HH:mm:ss.SSSZ", location.timeZone)
            else epTime =schTimeRemind
            if (schType=="single") epDT=new Date().parse("yyyy-MM-dd'T'HH:mm:ss.SSSZ", "${schYearEnd}-${schMonthEnd}-${schDayEnd}${schTimeEnd[10..27]}").getTime()
        	else if (schType=="simple" || (schType=="complex" && !schStart())) epDT=now()
        	else if (schType==~/complex|bd/ && schStart()) epDT=new Date().parse("yyyy-MM-dd'T'HH:mm:ss.SSSZ", "${schYearStart}-${schMonthStart}-${schDayStart}${epTime[10..27]}").getTime()
            if (epDT){
        		def epRemind = epDT-epDay
                if (epRemind < now() && schType==~/complex/) warning = true
        		if (epRemind < now() && schType=="single") expired = true
                if (schTimingRemind =="99" && !expired) epRemind = epDT - (60000 * 60 * 24 * 7) 
                if (epRemind < now() && ((schType !="custom" && schReType =="Week") || schType=="single")) warning = true 
                if (schTimingRemind =="99" && !expired) epRemind = epDT -(60000 * 60 * 24)
                if (epRemind < now() && ((schType !="custom" && schReType =="Week") || schType=="single")) warning = true
                if (schTimingRemind =="99" && !expired) epRemind = epDT - (60000 * 60)
                if (epRemind < now() && ((schType !="custom" && schReType =="Week") || schType=="single")) warning = true
                if (schType=="single" && !expired) result=true
                if (schType=="complex" && (expired || warning) && schReType==~~/1|5|10|15|30|60|180|360|720/) result=false
                else if (schType=="simple" && (expired || warning)) result=false
        	}    
        }
        if (schTimingRemind ==~/99|14|7/ && schReType=="week") {
        	warning = true
            result = false
        }
    }
    return ["result":result, "warning":warning, "expired":expired]
}
def monthList(){ return ["1": "January", "2":"February", "3":"March", "4":"April", "5":"May", "6":"June", "7":"July", "8":"August", "9":"September", "10":"October", "11":"November", "12":"December"] }	
def actionList(){ return ["msg":"Send Free-Text To Message Queue(s)", "macro":"Run Macro (Control, WebCoRE or Extension Group)","VR":"Send/Play Voice Report", "WR":"Send/Play Weather Report/Advisories","purge":"Purge Message Queue(s)"] }
def reType(){
	return ["1":"Every minute", "5":"Every 5 minutes", "10":"Every 10 minutes", "15": "Every 15 minutes", "30": "Every 1/2 hour", "60": "Every hour", "180": "Every 3 hours", "360": "Every 6 hours",
    	"720": "Every 12 hours", "day": "Daily", "week":"Weekly", "month": "Monthly", "year": "Annually"]   	
}
def reTime(){
	return ["15":"15 Minutes", "30":"30 Minutes", "60":"1 Hour", "1":"1 Day", "2" : "2 Days", "3":"3 Days","4":"4 Days", "5":"5 Days", "6":"6 Days", "7":"1 Week", "14":"2 Weeks", "99": "Progressively (1 week, 1 day, 1 hour then 15 min)"]
}    
def schTypes(){ return ["single":"One-Time Event", "simple":"Simple Recurring (Starts Immediately)","bd":"Annual Birthday/Anniversary","complex":"Complex Recurring (Restrictions/Limits)","custom":"Custom Cron Schedule (Advanced)"] }
def getStatus(){
	def schStatus = state.status ? "On" : "Off", dateTime
    if (app.label && schType && checkAction()){
        if ((schType=="simple" && !schReType) || (schType=="single" && !schEnd()) || (schType=="complex" && !schReType) ||  (schType =="custom" && !cronValidate()) || (schType=="bd" && !schStart()))  schStatus = "Incomplete"  
        if (schType ==~/single|complex/) {
            if (checkDate("end").warning || checkDate("start").warning) schStatus="Invalid"
            else if (checkDate("end").expired) schStatus="Expired"
        }
        if (schType=="bd" && checkDate("start").warning) schStatus="Invalid"
        if (schType=="complex" && schStart() && schEnd()){
            def start = "${schYearStart}-${schMonthStart}-${schDayStart}${schTimeStart[10..27]}", end="${schYearEnd}-${schMonthEnd}-${schDayEnd}${schTimeEnd[10..27]}"
            if (new Date().parse("yyyy-MM-dd'T'HH:mm:ss.SSSZ", start).getTime() > new Date().parse("yyyy-MM-dd'T'HH:mm:ss.SSSZ", end).getTime()){
                schStatus="Invalid start/end date combination"
            }     
        }
        if (schType=="custom" && !state.cronCheck) schStatus="Invalid cron expression"
        if (schType !="single" && (schCount && state.runCount >= schCount)) schStatus="Expired"   
	}
	else schStatus = "Incomplete"
    return schStatus
}
def schStart(){ return (schTimeStart && schMonthStart && schDayStart && schYearStart) }
def schEnd(){ return (schTimeEnd && schMonthEnd && schDayEnd && schYearEnd) }
def schRemind(){
	def result = false
    result = (schType!="custom" && (schTimingRemind && (schTimingRemind as int) <15 || schTimingRemind=="99" && schTimeRemind || schTimingRemind && (schTimingRemind as int) >14)) || (schType=="custom" && cronValidateRemind())
	if (schAction==~/purge|macro/ && !schMsgQueRemind) result = false
    return result
}
def checkAction(){ return (!schAction || (schAction=="msg" && (!schMsgTxt  || !schMsgQue)) || (schAction=="VR" && (!schVR || !schMsgQue)) || (schAction=="WR" && (!schWR || !schMsgQue))|| (schAction =="purge" && !schMsgQue) || (schAction =="macro" && !schMacro)) ?  false : true }
def startDateDesc(){
	def monthStart=monthList()[schMonthStart]?:schMonthStart, result
    if (!schStart()) result = "Tap to enter time and date"
    else if ((schReType ==~/day|week|month|year/) || (!checkDate("start").warning && !checkDate("start").expired && schType!="bd" ) || schType=="bd") result= "${parent.timeParse("${schTimeStart}", "h:mm a")} on ${monthStart} ${schDayStart}, ${schYearStart}"
    else if (checkDate("start").warning) result = "Invalid date - Tap to edit"
    else if (checkDate("start").expired && schType != "bd") result = "Expired date - Tap to edit"
    return result
}
def endDateDesc(){
	def monthEnd=monthList()[schMonthEnd]?:schMonthEnd, result
    if (!schEnd()) result = "Tap to enter time and date"
    else if (!checkDate("end").expired && !checkDate("end").warning) result= "${parent.timeParse("${schTimeEnd}", "h:mm a")} on ${monthEnd} ${schDayEnd}, ${schYearEnd}" 
    else if (checkDate("end").warning) result = "Invalid date - Tap to edit"
    else if (checkDate("end").expired) result = "Expired date - Tap to edit"
    return result
}
def remindDesc(){
	String result
    if (!schRemind()) result = "Tap to set action reminder"
    else{
    	if (schType !="custom"){
            if (checkDate("remind").expired && schTimingRemind !="99" && schType=="single") result ="The reminder time for this one-time event has already past. No reminder will be given."
            else if (checkDate("remind").warning && !checkDate("remind").expired && schTimingRemind =="99" && schType=="single") result ="One or more of the progressive reminders for this one-time event has already past. Any remaining reminders will activate at the appropriate time."
            else if (checkDate("remind").expired && schTimingRemind =="99" && schType=="single") result ="All of the progressive reminders for this one-time event have already past. No reminders will be given."	
            if ((schTimingRemind ==~/99|14|7/ && schReType=="week")) result ="The reminder time you entered is invalid. Choose a reminder time than is less than the event recurrance interval." 
            if (!result){
                result = "Remind ${reTime()[schTimingRemind].toLowerCase()?:schTimingRemind} prior to action${schTimeRemind ? " at " + parent.timeParse("${schTimeRemind}", "h:mm a") : ""}."
                if (schRemindInterval && schRemindInterval !="None" && (schTimingRemind as int) <15) result += "\nReminders happen ${schRemindInterval.toLowerCase()} on the "
                else result += "\nReminder happens on the "
                result += schMsgQueRemind && schMsgQueRemind.size()==1 ? "message queue: " : "message queues: "
                result += schMsgQueRemind ? translateMQid(schMsgQueRemind) : "Same as action message queue(s)"
                result += schMQNotifyRemind ? "\nNotification Mode Only" : ""
                result += schMQExpireRemind ? "\nExpires in ${schMQExpireRemind} minutes" : ""
                result += schMQOverwriteRemind ? "\nOverwrite all previous reminder messages" : ""
                result += schSuppressTDRemind ? "\nSuppress time and date from Alexa playback" : ""
            	result += schRemindFollow ? "\nReminders utlize action's on/off status & restrictions" : ""
                result += checkDate("remind").warning && schType==~/complex/ ? "\nYour first reminder time has already past. Subsequent reminders will be scheduled." : ""
            }
		}
        else {
        	if (cronValidateRemind()) result = cronDescRemind()
            if (result && !result.contains("Invalid")){
                result += "\nReminder happens on the "
                result += schMsgQueRemind && schMsgQueRemind.size()==1 ? "message queue: " : "message queues: "
                result += schMsgQueRemind ? translateMQid(schMsgQueRemind) : "Same as action message queue(s)"
                result += schMQNotifyRemind ? "\nNotification Mode Only" : ""
                result += schMQExpireRemind ? "\nExpires in ${schMQExpireRemind} minutes" : ""
                result += schMQOverwriteRemind ? "\nOverwrite all previous reminder messages" : ""
                result += schSuppressTDRemind ? "\nSuppress Time and Date from Alexa Playback" : ""
        	}
        }
    }
    return result
}
def mqDesc(){
    def result = "Tap to add/edit the message queue options"
    if (schMsgQue){
    	result = "Send to: ${translateMQid(schMsgQue)}"
        result += schMQNotify ? "\nNotification Mode Only" : ""
        result += schMQExpire ? "\nExpires in ${schMQExpire} minutes" : ""
        result += schMQOverwrite ? "\nOverwrite all previous schedule messages" : ""
        result += schSuppressTDRemind ? "\nSuppress Time and Date from Alexa Playback" : ""
	}
    return result
}
def cronDesc(){
	def result ="Tap to enter your cron parameters"
    if (cronValidate()) {
    	result = "${cronSec} ${cronMin} ${cronHour} ${cronDayMon} ${cronMon} ${cronDayWeek}"
        if (cronYear) result +=" ${cronYear}"
    }
    if (!state.cronCheck) result = "Invalid cron expression - Tap to edit"
	return result	
}
def cronDescRemind(){
	def result ="Tap to enter your cron reminder parameters"
    if (cronValidateRemind()) {
    	result = "${cronSecRemind} ${cronMinRemind} ${cronHourRemind} ${cronDayMonRemind} ${cronMonRemind} ${cronDayWeekRemind}"
        if (cronYearRemind) result +=" ${cronYearRemind}"
    }
    if (!state.cronCheckRemind) result = "Invalid cron expression - Tap to edit"
	return result	
}
def cronValidate(){ return cronSec && cronMin && cronHour && cronDayMon && cronMon && cronDayWeek }	
def cronValidateRemind(){ return cronSecRemind && cronMinRemind  && cronHourRemind  && cronDayMonRemind  && cronMonRemind  && cronDayWeekRemind  }	
def translateMQid(mqIDList){
	def result=mqIDList.contains("Primary Message Queue")?["Primary Message Queue"]:[], qName
	mqIDList.each{qID->
    	qName = parent.getAAMQ().find{it.id == qID}	
    	if (qName) result += qName.label
    }
    return parent.getList(result)
}
//Versions
private versionInt(){ return 104 }
private def textAppName() { return "Ask Alexa Schedules" }	
private def textVersion() { return "Schedules Version: 1.0.4a (03/11/2018)" }