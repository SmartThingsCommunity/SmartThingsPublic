/**
 *  FortrezZ Water Consumption Metering
 *
 *  Copyright 2016 FortrezZ, LLC
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
    name: "FortrezZ Water Consumption Metering",
    namespace: "FortrezZ",
    author: "FortrezZ, LLC",
    description: "Use the FortrezZ Water Meter to efficiently use your homes water system.",
    category: "Green Living",
    iconUrl: "http://swiftlet.technology/wp-content/uploads/2016/05/logo-square-200-1.png",
    iconX2Url: "http://swiftlet.technology/wp-content/uploads/2016/05/logo-square-500.png",
    iconX3Url: "http://swiftlet.technology/wp-content/uploads/2016/05/logo-square.png")


preferences {
	page(name: "page2", title: "Select device and actions", install: true, uninstall: true)
}

def page2() {
    dynamicPage(name: "page2") {
        section("Choose a water meter to monitor:") {
            input(name: "meter", type: "capability.energyMeter", title: "Water Meter", description: null, required: true, submitOnChange: true)
        }

        if (meter) {
            section {
                app(name: "childRules", appName: "Consumption Metering", namespace: "FortrezZ", title: "Create New Water Consumption Goal", multiple: true)
            }
        }
        
        section("Start/End time of all water usage goal periods") {
        	input(name: "alertTime", type: "time", required: true)
            }
        
        section("Billing info") {
        	input(name: "unitType", type: "enum", title: "Water unit used in billing", description: null, defaultValue: "Gallons", required: true, submitOnChange: true, options: waterTypes())
            input(name: "costPerUnit", type: "decimal", title: "Cost of water unit in billing", description: null, defaultValue: 0, required: true, submitOnChange: true)
        	input(name: "fixedFee", type: "decimal", title: "Add a Fixed Fee?", description: null, defaultValue: 0, submitOnChange: true)}
        
        section("Send notifications through...") {
        	input(name: "pushNotification", type: "bool", title: "SmartThings App", required: false)
        	input(name: "smsNotification", type: "bool", title: "Text Message (SMS)", submitOnChange: true, required: false)
            if (smsNotification)
            {
            	input(name: "phone", type: "phone", title: "Phone number?", required: true)
            }
            //input(name: "hoursBetweenNotifications", type: "number", title: "Hours between notifications", required: false)
        }
        



		log.debug "there are ${childApps.size()} child smartapps"
        
        
        def childRules = []
        childApps.each {child ->
            log.debug "child ${child.id}: ${child.settings()}"
            childRules << [id: child.id, rules: child.settings()] //this section of code stores the long ID and settings (which contains several variables of the individual goal such as measurement type, water consumption goal, start cumulation, current cumulation.) into an array
        }
        
        def match = false
        def changeOfSettings = false
        for (myItem in childRules) {
        	def q = myItem.rules
        	for (item2 in state.rules) {
                def r = item2.rules
                log.debug(r.alertType)
            	if (myItem.id == item2.id) { //I am comparing the previous array to current array and checking to see if any new goals have been made.  
                	match = true
                    if (q.type == r.type){
                    	changeOfSettings = true}
                }                
            }
            if (match == false) { // if a new goal has been made, i need to do some first time things like set up a recurring schedule depending on goal duration
                state["NewApp${myItem.id}"] = true
                log.debug "Created a new ${q.type} with an ID of ${myItem.id}"}
    
            match = false
        }
        
        for (myItem in childRules) {
            if (state["NewApp${myItem.id}"] == true){
                state["NewApp${myItem.id}"] = false
                state["currentCumulation${myItem.id}"] = 0 // we create another object attached to our new goal called 'currentCumulation' which should hold the value for how much water has been used since the goal period has started
                state["oneHundred${myItem.id}"] = false
                state["ninety${myItem.id}"] = false
                state["seventyFive${myItem.id}"] = false
                state["fifty${myItem.id}"] = false
                state["endOfGoalPeriod${myItem.id}"]  = false
            }   
        }

        state.rules = childRules // storing the array we just made to state makes it persistent across the instances this smart app is used and global across the app ( this value cannot be implicitely shared to any child app unfortunately without making it a local variable FYI
        log.debug "Parent Settings: ${settings}"
             
        if (costPerUnit != 0 && costPerUnit != null){//we ask the user in the main page for billing info which includes the price of the water and what water measurement unit is used. we combine convert the unit to gallons (since that is what the FMI uses to tick water usage) and then create a ratio that can be converted to any water measurement type
            state.costRatio = costPerUnit/(convertToGallons(unitType))
        	state.fixedFee = fixedFee
        }
    }
}

def parseAlerTimeAndStartNewSchedule(myAlert)
        {
        	def endTime = myAlert.split("T")
            def endHour = endTime[1].split(":")[0] // parsing the time stamp which is of this format: 2016-12-13T16:25:00.000-0500
            def endMinute = endTime[1].split(":")[1]
            schedule("0 ${endMinute} ${endHour} 1/1 * ? *", goalSearch) // creating a schedule to launch goalSearch every day at a user defined time - default is at midnight
            log.debug("new schedule created at ${endHour} : ${endMinute}")
        }

def convertToGallons(myUnit) // does what title says - takes whatever unit in string form and converts it to gallons to create a ratio. the result is returned
{
	switch (myUnit){
    	case "Gallons":
        	return 1
            break
        case "Cubic Feet":
        	return 7.48052
            break
        case "Liters":
        	return 0.264172
            break
        case "Cubic Meters":
        	return 264.172
            break
        default:
        	log.debug "value for water measurement doesn't fit into the 4 water measurement categories"
            return 1
            break
       }
}


def goalSearch(){

    def dateTime = new Date() // this section is where we get date in time within our timezone and split it into 2 arrays which carry the date and time
    def fullDateTime = dateTime.format("yyyy-MM-dd HH:mm:ss", location.timeZone)
    def mySplit = fullDateTime.split()

	log.debug("goalSearch: ${fullDateTime}") // 2016-12-09 14:59:56

	// ok, so I ran into a problem here. I wanted to simply do | state.dateSplit = mySplit[0].split("-") | but I kept getting this error in the log "java.lang.UnsupportedOperationException" So I split it to variables and then individually placed them into the state array
    def dateSplit = mySplit[0].split("-")
    def timeSplit = mySplit[1].split(":")
    state.dateSplit = []
    state.timeSplit = []
    for (i in dateSplit){
    	state.dateSplit << i} // unnecessary?
    for (g in timeSplit){
    	state.timeSplit << g}  
    def dayOfWeek = Date.parse("yyyy-MM-dd", mySplit[0]).format("EEEE")
    state.debug = false
    dailyGoalSearch(dateSplit, timeSplit)
    weeklyGoalSearch(dateSplit, timeSplit, dayOfWeek)
    monthlyGoalSearch(dateSplit, timeSplit)

}
    
    

def dailyGoalSearch(dateSplit, timeSplit){ // because of our limitations of schedule() we had to create 3 separate methods for the existing goal period of day, month, and year. they are identical other than their time periods.
	def myRules = state.rules // also, these methods are called when our goal period ends. we filter out the goals that we want and then invoke a separate method called schedulGoal to inform the user that the goal ended and produce some results based on their water usage.
        for (it in myRules){
            def r = it.rules
            if (r.type == "Daily Goal") {
				scheduleGoal(r.measurementType, it.id, r.waterGoal, r.type, 0.03333)
            }
        }    	
}
def weeklyGoalSearch(dateSplit, timeSplit, dayOfWeek){
    def myRules = state.rules // also, these methods are called when our goal period ends. we filter out the goals that we want and then invoke a separate method called schedulGoal to inform the user that the goal ended and produce some results based on their water usage.
        for (it in myRules){
            def r = it.rules
            if (r.type == "Weekly Goal") {
            	if (dayOfWeek == "Sunday" || state.debug == true){ 
                	scheduleGoal(r.measurementType, it.id, r.waterGoal, r.type, 0.23333)}
            }
        }    	
}
def monthlyGoalSearch(dateSplit, timeSplit){  
	def myRules = state.rules // also, these methods are called when our goal period ends. we filter out the goals that we want and then invoke a separate method called schedulGoal to inform the user that the goal ended and produce some results based on their water usage.
        for (it in myRules){
            def r = it.rules
            if (r.type == "Monthly Goal") {
				if (dateSplit[2] == "01" || state.debug == true){ 
                	scheduleGoal(r.measurementType, it.id, r.waterGoal, r.type, 0.23333)}
            }
        }    
}
def scheduleGoal(measureType, goalID, wGoal, goalType, fixedFeeRatio){ // this is where the magic happens. after a goal period has finished this method is invoked and the user gets a notification of the results of the water usage over their period.
	def cost = 0
    def f = 1.0f
    def topCumulative = meter.latestValue("cumulative") // pulling the current cumulative value from the FMI for calculating  how much water we have used since starting the goal.
     if (state["Start${goalID}"] == null){state["Start${goalID}"] = topCumulative} // we create another object attached to our goal called 'start' and store the existing cumulation on the FMI device so we know at what mileage we are starting at for this goal. this is useful for determining how much water is used during the goal period.
    def curCumulation = waterConversionPreference(topCumulative, measureType) - waterConversionPreference(state["Start${goalID}"], measureType)
	
    
	if (state.costRatio){
    	cost = costConversionPreference(state.costRatio,measureType) * curCumulation * f + (state.fixedFee * fixedFeeRatio)// determining the cost of the water that they have used over the period ( i had to create a variable 'f' and make it a float and multiply it to make the result a float. this is because the method .round() requires it to be a float for some reasons and it was easier than typecasting the result to a float.
    }
    def percentage = (curCumulation / wGoal) * 100 * f
    if (costPerUnit != 0) {
        notify("Your ${goalType} period has ended. You have used ${(curCumulation * f).round(2)} ${measureType} of your goal of ${wGoal} ${measureType} (${(percentage * f).round(1)}%). Costing \$${cost.round(2)}")// notifies user of the type of goal that finished, the amount of water they used versus the goal of water they used, and the cost of the water used
        log.debug "Your ${goalType} period has ended. You have used ${(curCumulation * f).round(2)} ${measureType} of your goal of ${wGoal} ${measureType} (${(percentage * f).round(1)}%). Costing \$${cost.round(2)}"
        
    }
    if (costPerUnit == 0) // just in case the user didn't add any billing info, i created a second set of notification code to not include any billing info.
    {
    	notify("Your ${goalType} period has ended. You have you have used ${(curCumulation * f).round(2)} ${measureType} of your goal of ${wGoal} ${measureType} (${percentage.round(1)}%).")
        log.debug "Your ${goalType} period has ended. You have you have used ${(curCumulation * f).round(2)} ${measureType} of your goal of ${wGoal} ${measureType} (${percentage.round(1)}%)."
     }
    state["Start${goalID}"] = topCumulative;
    state["oneHundred${goalID}"] = false
    state["ninety${goalID}"] = false
    state["seventyFive${goalID}"] = false
    state["fifty${goalID}"] = false
    state["endOfGoalPeriod${goalID}"] = true // telling the app that the goal period is over.
}
	
	

def waterTypes() // holds the types of water measurement used in the main smartapp page for billing info and for setting goals
{
	def watertype = []
    
    watertype << "Gallons"
    watertype << "Cubic Feet"
    watertype << "Liters"
    watertype << "Cubic Meters"
    return watertype
}

def installed() { // when the app is first installed - do something
	log.debug "Installed with settings: ${settings}"

	initialize()
}

def updated() { // whevenever the app is updated in any way by the user and you press the 'done' button on the top right of the app - do something
	log.debug "Updated with settings: ${settings}"
    
    if (alertTime != state.alertTime) // we created this 'if' statement to prevent another schedule being made whenever the user opens the smartapp
    {
        unschedule() //unscheduling is a good idea here because we don't want multiple schedules happening and this function cancles all schedules
        parseAlerTimeAndStartNewSchedule(alertTime) // we use cron scheduling to use the function 'goalSearch' every minute
        state.alarmTime = alarmTime // setting state.alarmTime prevents a new schedule being made whenever the user opens the smartapp
    }

	unsubscribe()
	initialize()
    //unschedule()
}

def initialize() { // whenever you open the smart app - do something
	subscribe(meter, "cumulative", cumulativeHandler)
	//subscribe(meter, "gpm", gpmHandler)
    log.debug("Subscribing to events")
}

def cumulativeHandler(evt) { // every time a tick on the FMI happens this method is called. 'evt' contains the cumulative value of every tick that has happened on the FMI since it was last reset. each tick represents 1/10 of a gallon
    def f = 1.0f
	def gpm = meter.latestValue("gpm") // storing the current gallons per minute value
    def cumulative = new BigDecimal(evt.value) // storing the current cumulation  value
    log.debug "Cumulative Handler: [gpm: ${gpm}, cumulative: ${cumulative}]"
    def rules = state.rules //storing the array of child apps to 'rules'
    rules.each { it -> // looping through each app in the array but storing each app into the variable 'it'
        def r = it.rules // each child app has a 2 immediate properties, one called 'id' and one called 'rules' - so 'r' contains the values of 'rules' in the child app
        def childAppID = it.id // storing the child app ID to a variable 
		if (state["Start${childAppID}"] == null) {state["Start${childAppID}"] = cumulative}// just for the first run of the app... start should be null. so we have to change that for the logic to work.


        def newCumulative = waterConversionPreference(cumulative, r.measurementType) //each goal allows the user to choose a water measurement type. here we convert the value of 'cumulative' to whatever the user prefers for display and logic purposes
        def goalStartCumulative = waterConversionPreference(state["Start${childAppID}"], r.measurementType)
        
        
        def DailyGallonGoal = r.waterGoal // 'r.waterGoal' contains the number of units of water the user set as a goal. we then save that to 'DailyGallonGoal'
        state.DailyGallonGoal = DailyGallonGoal // and then we make that value global and persistent for logic reasons
        def currentCumulation = newCumulative - goalStartCumulative // earlier we created the value 'currentCumulation' and set it to 0, now we are converting both the 'cumulative' value and what 'cumulative' was when the goal perio was made and subtracting them to discover how much water has been used since the creation of the goal in the users prefered water measurement unit.
		state["currentCumulation${childAppID}"] = currentCumulation
        log.debug("Threshold:${DailyGallonGoal}, Value:${(currentCumulation * f).round(2)}")

        if ( currentCumulation >= (0.5 * DailyGallonGoal) && currentCumulation < (0.75 * DailyGallonGoal) && state["fifty${childAppID}"] == false) // tell the user if they break certain use thresholds
        {
            notify("You have reached 50% of your ${r.type} use limit. (${(currentCumulation * f).round(2)} of ${DailyGallonGoal} ${r.measurementType})")
            log.debug "You have reached 50% of your ${r.type} use limit. (${(currentCumulation * f).round(2)} of ${DailyGallonGoal} ${r.measurementType})"
            state["fifty${childAppID}"] = true
        }
        if ( currentCumulation >= (0.75 * DailyGallonGoal) && currentCumulation < (0.9 * DailyGallonGoal) && state["seventyFive${childAppID}"] == false)
        {
            notify("You have reached 75% of your ${r.type} use limit. (${(currentCumulation * f).round(2)} of ${DailyGallonGoal} ${r.measurementType})")
            log.debug "You have reached 75% of your ${r.type} use limit. (${(currentCumulation * f).round(2)} of ${DailyGallonGoal} ${r.measurementType})"
            state["seventyFive${childAppID}"] = true
        }
        if ( currentCumulation >= (0.9 * DailyGallonGoal) && currentCumulation < (DailyGallonGoal) && state["ninety${childAppID}"] == false)
        {
            notify("You have reached 90% of your ${r.type} use limit. (${(currentCumulation * f).round(2)} of ${DailyGallonGoal} ${r.measurementType})")
            log.debug "You have reached 90% of your ${r.type} use limit. (${(currentCumulation * f).round(2)} of ${DailyGallonGoal} ${r.measurementType})"
            state["ninety${childAppID}"] = true
        }
        if (currentCumulation >= DailyGallonGoal && state["oneHundred${childAppID}"] == false)
        {
            notify("You have reached 100% of your ${r.type} use limit. (${(currentCumulation * f).round(2)} of ${DailyGallonGoal} ${r.measurementType})")
            log.debug "You have reached 100% of your ${r.type} use limit. (${(currentCumulation * f).round(2)} of ${DailyGallonGoal} ${r.measurementType})"
            state["oneHundred${childAppID}"] = true
            //send command here like shut off the water
            

            
        }
        if (state["endOfGoalPeriod${childAppID}"] == true) // changing the start value to the most recent cumulative value for goal reset.
            {state["Start${childAppID}"] = cumulative
             state["endOfGoalPeriod${childAppID}"] = false
            }
    }
}

def waterConversionPreference(cumul, measurementType1) // convert the current cumulation to one of the four options below - since cumulative is initially in gallons, then the options to change them is easy
{
	switch (measurementType1)
    {
            case "Cubic Feet":
            	return (cumul * 0.133681)
            break
            
            case "Liters":
            	return (cumul * 3.78541)
            break
            
            case "Cubic Meters":
            	return (cumul * 0.00378541)
            break
            
            case "Gallons":
            	return cumul
            break
        
    }
}

def costConversionPreference(cumul, measurementType1) // convert the current cumulation to one of the four options below - since cumulative is initially in gallons, then the options to change them is easy
{
	switch (measurementType1)
    {
            case "Cubic Feet":
            	return (cumul / 0.133681)
            break
            
            case "Liters":
            	return (cumul / 3.78541)
            break
            
            case "Cubic Meters":
            	return (cumul / 0.00378541)
            break
            
            case "Gallons":
            	return cumul
            break
        
    }
}

def notify(myMsg) // method for both push notifications and for text messaging.
{
	log.debug("Sending Notification")
    if (pushNotification)	{sendPush(myMsg)} else {sendNotificationEvent(myMsg)}
    if (smsNotification)	{sendSms(phone, myMsg)}
}

def uninstalled() {
    // external cleanup. No need to unsubscribe or remove scheduled jobs
    unsubscribe()
    unschedule()
}