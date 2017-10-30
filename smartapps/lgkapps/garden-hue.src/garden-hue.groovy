/**
* Garden Hue v 3.0 
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
* Add a switch/virtual switch option, which if selected the enable/disable of the app will follow.
*
* Version 3:
* Add option to control each light separately, so each gets its own color.
* Add Holiday Mode option where you can individually control what colors are enabled.
* Related, Add options to turn on/off each color.
* Add every 1 minute option. Use Sparingly can overload back end Scheduling
* Added better logging, so in notification window you can see current color set. This is in order to help debug for when lights stop responding.
*
* The latest version of this file can be found at
*
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
                                "1",
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
        input "individualControl","bool", title: "Control Each light Color Individually?", required: false, defaultValue: false
     }
    

    section (" Colors Enabled") {
        input "holidayMode","bool", title: "Allow Enable/Disable for Each Color/Holiday Mode?", required: false, defaultValue: false

        input "redEnabled", "bool", title: "Enable Red?", required: false, defaultValue: true    
        input "brickRedEnabled", "bool", title: "Enable Brick Red?", required: false, defaultValue: true    
        input "safetyOrangeEnabled", "bool", title: "Enable Safety Orange?", required: false, defaultValue: true    
        input "orangeEnabled", "bool", title: "Enable Orange?", required: false, defaultValue: true    
        input "amberEnabled", "bool", title: "Enable Amber?", required: false, defaultValue: true    
        input "yellowEnabled", "bool", title: "Enable Yellow?", required: false, defaultValue: true    
        input "greenEnabled", "bool", title: "Enable Green?", required: false, defaultValue: true    
        input "turquoiseEnabled", "bool", title: "Enable Turquoise?", required: false, defaultValue: true    
        input "aquaEnabled", "bool", title: "Enable Aqua?", required: false, defaultValue: true    
        input "navyBlueEnabled", "bool", title: "Enable Navy Blue?", required: false, defaultValue: true    
        input "blueEnabled", "bool", title: "Enable Blue?", required: false, defaultValue: true    
        input "indigoEnabled", "bool", title: "Enable Indigo?", required: false, defaultValue: true    
        input "purpleEnabled", "bool", title: "Enable Purple?", required: false, defaultValue: true    
        input "pinkEnabled", "bool", title: "Enable Pink?", required: false, defaultValue: true    
        input "rasberryEnabled", "bool", title: "Enable Rasberry?", required: false, defaultValue: true    
        input "whiteEnabled", "bool", title: "Enable White?", required: false, defaultValue: true    

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
     case "1":
     log.debug "Switching color every minute."
     schedule("1 * * * * ?",changeHandler)
     break;

     case "5":
     log.debug "Switching color every 5 minutes."
     runEvery5Minutes(changeHandler)
     break;
      
     case "10":
     log.debug "Switching color every 10 minutes."
     runEvery10Minutes(changeHandler)
     break;
    
     case "15":
     log.debug "Switching color every 15 minutes."
     runEvery15Minutes(changeHandler)
     break;
    
     case "30":
     log.debug "Switching color every 30 minutes."
     runEvery30Minutes(changeHandler)
     break;
    
     case "1 hour":
     log.debug "Switching color every hour."
     runEvery1Hour(changeHandler)
     break;
    
     case  "3 hours":
     log.debug "Switching color every 3 hours"
     runEvery3Hours(changeHandler)
     break;
      
     default:
     log.debug "Switching color every 30 minutes."
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
    
    if ((numberon > 0) && (settings.enabled == true))
    {
      def newColor = ""
      if (settings.randomMode == true)
       {

       // lgk define alternate color array if holiday mode enabled
       if (settings.holidayMode == true)
         {
            log.debug "Holiday Mode = On, getting count of colors Enabled."
            
         // now define new array
         def enabledColorArray = []

           if (redEnabled == true)
               enabledColorArray <<  "Red"
           if (brickRedEnabled == true)
               enabledColorArray <<  "Brick Red"
           if (safetyOrangeEnabled == true)
               enabledColorArray <<  "Safety Orange"
           if (orangeEnabled == true)
               enabledColorArray <<  "Orange"
           if (amberEnabled == true)
               enabledColorArray <<  "Amber"
           if (yellowEnabled == true)
               enabledColorArray <<  "Yellow"
           if (greenEnabled == true)
               enabledColorArray <<  "Green"
           if (turquoiseEnabled == true)
               enabledColorArray <<  "Turquoise"
           if (aquaEnabled == true)
               enabledColorArray <<  "Aqua"
           if (navyBlueEnabled == true)
               enabledColorArray <<  "Navy Blue"
           if (blueEnabled == true)
               enabledColorArray <<  "Blue"
           if (indigoEnabled == true)
               enabledColorArray <<  "Indigo"
           if (purpleEnabled == true)
               enabledColorArray <<  "Purple"
           if (pinkEnabled == true)
               enabledColorArray <<  "Pink"
           if (rasberryEnabled == true)
               enabledColorArray <<  "Rasberry"
           if (whiteEnabled == true)
               enabledColorArray <<  "White"



           def int colorsEnabled = enabledColorArray.size
           log.debug "Enabled Color Count = $colorsEnabled"
           log.debug "Enabled Color Array = $enabledColorArray"

           if ((numberon > 1) && (settings.individualControl = true))
             {

                 for (def i=0; i<numberon; i++) 
                      {
                            def int nextValue = new Random().nextInt(colorsEnabled)
                            log.debug "Hue: $i, Random Number = $nextValue"
                            newColor = enabledColorArray[nextValue]

                            sendNotificationEvent("$app.label: Hue:$i - Setting Color to $newColor.")
                            sendcolor(newColor,i)
                     }
             } // do each individually
        
           else
               {

                def int nextValue = new Random().nextInt(colorsEnabled)
                log.debug "Random Number = $nextValue"
                newColor = enabledColorArray[nextValue]

                sendNotificationEvent("$app.label: Setting Color to $newColor.")
                hues.on()
                sendcolor(newColor)

              }

       } // holiday mode on


     else {
        // not holiday mode

        def int nextValue = new Random().nextInt(16)
        def colorArray = ["Red","Brick Red","Safety Orange","Orange","Amber","Yellow","Green","Turquoise","Aqua","Navy Blue","Blue","Indigo","Purple","Pink","Rasberry","White"]

        log.debug "Random Number = $nextValue"
        newColor = colorArray[nextValue]    

        sendNotificationEvent("$app.label: Setting Color to $newColor.")
        hues.on()
        sendcolor(newColor)
  
        }

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

      log.debug "After Check new color = $newColor"
      hues.on()
      sendcolor(newColor)

    } // end random or not
    
      }
   }
}


def sendcolor(color) { log.debug "In send color"
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


def sendcolor(color,which)
{
        log.debug "In send color for hue $which"
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
	hues[which].setColor(newValue)
        state.currentColor = color
        mysend("$app.label: hue: $which, Setting Color = $color")
        log.debug "$app.label: hue: $which Setting Color = $color"

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