/**
* Garden Hue v 1.0 
* by lg. kahn
*
* This SmartApp will turn on if enabled after sunset and run to
* sunrise.. It will change the hue color of your garden hue every xx
* minutes based on a schedule.
*
* Version 1.0: April 28, 2016 - Initial release
*
* Version 2.0: add app name to push messages.
* also dont schedule anything if disabled is set.
* clean up and remove some debugging.
* add run every 5 minute option.
* Turn off all hues when saving app.
*
* Version 2.1:
* add a switch/virtual switch option, which if selected the enable/disable of the app will follow.
*
* The latest version of this file can be found at
* https://github.com/lgkapps/SmartThingsPublic/gardenhue
*
*/

definition(
name: "Garden Hue",
namespace: "lgkapps",
author: "lgkahn kahn-st@lgk.com",
description: "Change hue color of lights based on schedule sunset to sunrise.",
category: "Convenience",
iconUrl: "http://mail.lgk.com/huetree1.png",
iconX2Url:"http://mail.lgk.com/huetree2.png",
iconX3Url: "http://mail.lgk.com/huetree2.png",
)

preferences {

	section("Choose hue lights you wish to control?") {
            input "hues", "capability.colorControl", title: "Which Color Changing Bulbs?", multiple:true, required: true
        	input "brightnessLevel", "number", title: "Brightness Level (1-100)?", required:false, defaultValue:100 //Select brightness
	}

	section("Choose cycle time between color changes? ") {
            input "cycletime", "enum", title: "Cycle time in minutes?" , options: [
                "5",
				"10", 
				"15", 
				"30", 
				"1 hour", 
				"3 hours"
			], required: true, defaultValue: "30"
	}

     section( "Turn Off How Many Hours before Sunrise?") {
        input "offset", "enum", title: "Turn Off How many hours before sunrise?",options: ["0", "-1", "-2", "-3", "-4", "-5"],
        required: true, defaultValue: "0"
     }
     
     section( "Enabled/Disabled" ) {
        input "enabled","bool", title: "Enabled?", required: true, defaultValue: true
        input "enableswitch", "capability.switch", title: "Optional: Do you have a switch/virtual switch which the application enable/disable functionality should follow? If you do not want this feature leave blank.", multiple:false, required: false
        input "randomMode","bool", title: "Enable Random Mode?", required: true, defaultValue: false
     }
    
     section( "Notifications" ) {
        input("recipients", "contact", title: "Send notifications to") {
            input "sendPushMessage", "enum", title: "Send a push notification?", options: ["Yes", "No"], required: false
            input "phone1", "phone", title: "Send a Text Message?", required: false
    }
  }
}
	
def installed() {
    unsubscribe()
    unschedule()
    
    if (hues)
    {
     TurnOff()
    }
    
    if (settings.enabled == true)
    {
      initialize()
    }  
    
   // do event listening on switch whether enabled or not 
     if ((enableswitch) && (hues))
     {
       subscribe(enableswitch,"switch",EnableSwitchHandler)
     }
}

def updated() {

    unsubscribe()
    unschedule()
    
   if (hues)
    {
     // must always turn off here even if disabled, because this gets called when the switch disables and turns stuff off.
     TurnOffAlways()
    }
    
   if (settings.enabled == true)
    {
      initialize()
    }
    
  // do event listening on switch whether enabled or not 
     if ((enableswitch) && (hues))
     {
       subscribe(enableswitch,"switch",EnableSwitchHandler)
     }
     
    if (hues)
    {
        def currSwitches = hues.currentSwitch    
        def onHues = currSwitches.findAll { switchVal -> switchVal == "on" ? true : false }
        def numberon = onHues.size();
        def onstr = numberon.toString() 
        log.debug "in updated on = $onstr"  
    }
}

def EnableSwitchHandler(evt)
{
    log.debug "In Switch Handler: Switch changed state to: ${evt.value}"
    if (evt.value == "on")
     {
       log.debug "Enabling App!"
       settings.enabled = true
       updated()
       // updated turns off so need to turn back on when switch tripped.. but also need to renable scheduling.
       TurnOn()
      }
    else
     {
       log.debug "Disabling App!"
       settings.enabled = false
       updated()
     }
}


private def initialize() {
    log.debug(" in initialize() for $app.label with settings: ${settings}")

    if(hues) 
    {
        subscribe(hues, "switch.on", changeHandler)    
        subscribe(location, "sunset", SunsetHandler)
        subscribe(location, "sunrise", SunriseHandler)
    
// uses the run every instead of direct schedule to take load off of fixed times on hub
    switch (settings.cycletime)
    {
     case "5":
     log.debug "switching color every 5 minutes."
     runEvery5Minutes(changeHandler)
     break;
      
     case "10":
     log.debug "switching color every 10 minutes."
     runEvery10Minutes(changeHandler)
     break;
    
     case "15":
     log.debug "switching color every 15 minutes."
     runEvery15Minutes(changeHandler)
     break;
    
     case "30":
     log.debug "switching color every 30 minutes."
     runEvery30Minutes(changeHandler)
     break;
    
     case "1 hour":
     log.debug "switching color every hour."
     runEvery1Hour(changeHandler)
     break;
    
     case  "3 hours":
     log.debug "switching color every 3 hours"
     runEvery3Hours(changeHandler)
     break;
      
     default:
     log.debug "switching color every 30 minutes."
     runEvery30Minutes(changeHandler)
     break;
     
    }
   // schedule("0 */15 * * * ?", changeHandler)
    // if selectd app it will run
    subscribe(app,changeHandler)
    
    state.nextOnTime = 0
    state.nextOffTime = 0

   // subscribe(location, "sunsetTime", scheduleNextSunset)
   // sunset handled automaticall need next sunrise to handle offset
    subscribe(location, "sunriseTime", scheduleNextSunrise)
    scheduleNextSunrise()
    // rather than schedule a cron entry, fire a status update a little bit in the future recursively
   // scheduleNext()
    state.currentColor = "None"
    
    }
}

def SunriseHandler(evt)
{
 TurnOff()
 scheduleNextSunrise()
}
 
def SunsetHandler(evt)
{
 TurnOn()
 scheduleNextSunrise()
 }
 
def TurnOff()
{
    //log.debug "In turn off"
   if (settings.enabled == true)
    {
      mysend("$app.label: Turning Off!")
	}
	hues.off()
}    


 
def TurnOffAlways()
{
      mysend("$app.label: Turning Off!")
	  hues.off()
}    

def TurnOn()
{
   // log.debug "In turn on"
    if (settings.enabled == true)
    {
     mysend("$app.label: Turning On!")
     hues.on()
    }
}

def scheduleNextSunrise(evt) {

   log.debug "In schedule next sunrise"

    def int sunriseoffset = settings.offset.toInteger()
    log.debug "got sunrise offset = $sunriseoffset"

    // get sunrise and sunset times
    def sunRiseSet = getSunriseAndSunset()
    def sunriseTime = sunRiseSet.sunrise
    def sunsetTime = sunRiseSet.sunset
        
    log.debug "sunrise time ${sunriseTime}"
    log.debug "sunset time ${sunsetTime}"
    
    //get the Date value for the string
    //def sunriseTime = Date.parse("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", sunriseTime)

    // sunrise returns today so double check we are not getting todays time but tomarrow
    // if we are before sunset time we are too early
    
    // bug in get sunrise time somethigs shows todays even though we are passed it.. sometimes
    // shows the next one.. Think it has to do with timezone offset.. so compare to local time to see
    // which case we have and if we need to add 24 hours or not
    
     def currentTime = new Date(now())
     //log.debug "current time = $currentTime, sunrisetime = $sunriseTime, sunsettime = $sunsetTime"
     
     // if current time is greater than sunsirse we are today no tomorrow so add 24 hours.
     if(currentTime.time > sunriseTime.time) {
        log.debug "Adding a day as sunrise time is still today!"
        sunriseTime = new Date(sunriseTime.time + (24 * 60 * 60 * 1000))
    }
   
    //calculate the offset
    def timeBeforeSunrise = new Date(sunriseTime.time + (sunriseoffset * 60 * 60 * 1000))
    log.debug "Scheduling for: $timeBeforeSunrise (sunrise is $sunriseTime)"

    //schedule this to run one time
    if (state.nextOffTime != timeBeforeSunrise)
    {
      log.debug "Scheduling it!"
      runOnce(timeBeforeSunrise, TurnOff)
      state.nextOffTime = timeBeforeSunrise
    }
}

def changeHandler(evt) {

	log.debug "in change handler"
  	// only do stuff if either switch is on (turns off at sunrise) or turned on manually
    if (hues)
    {
    	 def currSwitches = hues.currentSwitch
         def onHues = currSwitches.findAll { switchVal -> switchVal == "on" ? true : false }
         def numberon = onHues.size();
         def onstr = numberon.toString() 
         
       log.debug "found $onstr that were on!"
    
    if ((onHues.size() > 0) && (settings.enabled == true))
    {
      def newColor = ""
      if (settings.randomMode == true)
       {
        def int nextValue = new Random().nextInt(16)
        def colorArray = ["Red","Brick Red","Safety Orange","Orange","Amber","Yellow","Green","Turquoise","Aqua","Navy Blue","Blue","Indigo","Purple","Pink","Rasberry","White"]
             
        log.debug "Random Number = $nextValue"
        newColor = colorArray[nextValue]    
       }
       
      else
      
      { // not random
      
	  def currentColor = state.currentColor
      
    log.debug " in changeHandler got current color = $currentColor"

       switch(currentColor) {
    
		case "Red":
			newColor="Brick Red"
			break;
		case "Brick Red":
			newColor = "Safety Orange"
			break;
		case "Safety Orange":
			newColor = "Orange"
			break;
		case "Orange":
			newColor = "Amber"
			break;
		case "Amber":
			newColor = "Yellow"
			break;
		case "Yellow":
			newColor = "Green"
			break;
		case "Green":
			newColor = "Turquoise"
			break;
        case "Turquoise":
			newColor = "Aqua"
			break;
		case "Aqua":
			newColor = "Navy Blue"
			break;
        case "Navy Blue":
			newColor = "Blue"
			break;
		case "Blue":
			newColor = "Indigo"
			break;
		case "Indigo":
			newColor = "Purple"
			break;
        case "Purple":
			newColor = "Pink"
			break;
    	case "Pink":
			newColor = "Rasberry"
			break;
        case "Rasberry":
			newColor = "White"
			break;
        case "White":
			newColor = "Red"
			break;
        default:
        	//log.debug "in default"
             newColor = "Red"
			break;
	}
    } // end random or not
    
      log.debug "After Check new color = $newColor"

      hues.on()
      sendcolor(newColor)
      }
   }
}


def sendcolor(color)
{
log.debug "In send color"
	//Initialize the hue and saturation
	def hueColor = 0
	def saturation = 100

	//Use the user specified brightness level. If they exceeded the min or max values, overwrite the brightness with the actual min/max
	if (brightnessLevel<1) {
		brightnessLevel=1
	}
    else if (brightnessLevel>100) {
		brightnessLevel=100
	}

	//Set the hue and saturation for the specified color.
	switch(color) {
		case "White":
			hueColor = 0
			saturation = 0
			break;
		case "Daylight":
			hueColor = 53
			saturation = 91
			break;
		case "Soft White":
			hueColor = 23
			saturation = 56
			break;
		case "Warm White":
			hueColor = 20
			saturation = 80 
			break;
        case "Navy Blue":
            hueColor = 61
            break;
		case "Blue":
			hueColor = 65
			break;
		case "Green":
			hueColor = 33
			break;
        case "Turquoise":
        	hueColor = 47
            break;
        case "Aqua":
            hueColor = 50
            break;
        case "Amber":
            hueColor = 13
            break;
		case "Yellow":
			//hueColor = 25
            hueColor = 17
			break; 
        case "Safety Orange":
            hueColor = 7
            break;
		case "Orange":
			hueColor = 10
			break;
        case "Indigo":
            hueColor = 73
            break;
		case "Purple":
			hueColor = 82
			saturation = 100
			break;
		case "Pink":
			hueColor = 90.78
			saturation = 67.84
			break;
        case "Rasberry":
            hueColor = 94
            break;
		case "Red":
			hueColor = 0
			break;
         case "Brick Red":
            hueColor = 4
            break;                
	}

	//Change the color of the light
	def newValue = [hue: hueColor, saturation: saturation, level: brightnessLevel]  
	hues*.setColor(newValue)
        state.currentColor = color
        mysend("$app.label: Setting Color = $color")
        log.debug "$app.label: Setting Color = $color"

}

private mysend(msg) {

    if (location.contactBookEnabled) {
        log.debug("sending notifications to: ${recipients?.size()}")
        sendNotificationToContacts(msg, recipients)
    }
    else {
        if (sendPushMessage != "No") {
            //log.debug("sending push message")
            sendPush(msg)
        }

        if (phone1) {
            //log.debug("sending text message")
            sendSms(phone1, msg)
        }
    }

   // log.debug msg
}
