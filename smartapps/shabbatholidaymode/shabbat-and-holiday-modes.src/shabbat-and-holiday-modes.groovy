/**
 *  HebcalModes
 *
 *  Author: danielbarak@live.com
 *  Date: 2014-02-21
 */

// Automatically generated. Make future change here.
definition(
    name: "Shabbat and Holiday Modes",
    namespace: "ShabbatHolidayMode",
    author: "danielbarak@live.com",
    description: "Changes the mode at candle lighting and back after havdalah.  Uses the HebCal.com API to look for days that are shabbat or chag and pull real time candle lighting and havdalah times to change modes automatically",
    category: "My Apps",
    iconUrl: "http://upload.wikimedia.org/wikipedia/commons/thumb/4/49/Star_of_David.svg/200px-Star_of_David.svg.png",
    iconX2Url: "http://upload.wikimedia.org/wikipedia/commons/thumb/4/49/Star_of_David.svg/200px-Star_of_David.svg.png",
    iconX3Url: "http://upload.wikimedia.org/wikipedia/commons/thumb/4/49/Star_of_David.svg/200px-Star_of_David.svg.png",
    pausable: true
)

preferences {
	
	section("At Candlelighting Change Mode To:") 
    {
		input "startMode", "mode", title: "Mode?"
	}
    section("At Havdalah Change Mode To:") 
    {
		input "endMode", "mode", title: "Mode?"
	}
	section("Havdalah Offset (Usually 50 or 72)") {
		input "havdalahOffset", "number", title: "Minutes After Sundown", required:true
	} 
	section("Your ZipCode") {
		input "zipcode", "text", title: "ZipCode", required:true
	}
    section( "Notifications" ) {
        input "sendPushMessage", "enum", title: "Send a push notification?", metadata:[values:["Yes","No"]], required:false
        input "phone", "phone", title: "Send a Text Message?", required: false
    }
    /**/
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
    poll();
    schedule("0 0 8 1/1 * ? *", poll) 
}

//Check hebcal for today's candle lighting or havdalah
def poll()
{
	
    unschedule("endChag")
    unschedule("setChag")
	Hebcal_WebRequest()

}//END def poll()



/**********************************************
// HEBCAL FUNCTIONS
-----------------------------------------------*/

//This function is the web request and response parse
def Hebcal_WebRequest(){

def today = new Date().format("yyyy-MM-dd")
//def today = "2014-11-14"
def zip = settings.zip as String
def locale = getWeatherFeature("geolookup", zip)
def timezone = TimeZone.getTimeZone(locale.location.tz_long)
def hebcal_date
def hebcal_category
def hebcal_title
def candlelighting
def candlelightingLocalTime
def havdalah
def havdalahLocalTime
def pushMessage
def testmessage
def urlRequest = "http://www.hebcal.com/hebcal/?v=1&cfg=json&nh=off&nx=off&year=now&month=now&mf=off&c=on&zip=${zipcode}&m=${havdalahOffset}&s=off&D=off&d=off&o=off&ss=off"
log.trace "${urlRequest}"

def hebcal = { response ->
	hebcal_date = response.data.items.date
	hebcal_category = response.data.items.category
	hebcal_title = response.data.items.title
    
    for (int i = 0; i < hebcal_date.size; i++) 
    {
    	if(hebcal_date[i].split("T")[0]==today)
        {
        	if(hebcal_category[i]=="candles")
        	{
    			candlelightingLocalTime = HebCal_GetTime12(hebcal_title[i])
                pushMessage = "Candle Lighting is at ${candlelightingLocalTime}"
                candlelightingLocalTime = HebCal_GetTime24(hebcal_date[i])
				candlelighting = timeToday(candlelightingLocalTime, timezone)  
               
				sendMessage(pushMessage)
    			schedule(candlelighting, setChag)     
                log.debug pushMessage
    		}//END if(hebcal_category=="candles")
    
    		else if(hebcal_category[i]=="havdalah")
        	{
    			havdalahLocalTime = HebCal_GetTime12(hebcal_title[i])
                pushMessage = "Havdalah is at ${havdalahLocalTime}"
                havdalahLocalTime = HebCal_GetTime24(hebcal_date[i])
				havdalah = timeToday(havdalahLocalTime, timezone)
                testmessage = "Scheduling for ${havdalah}"
    			schedule(havdalah, endChag)      
                log.debug pushMessage
                log.debug testmessage
    		}//END if(hebcal_category=="havdalah"){
        }//END if(hebcal_date[i].split("T")[0]==today)
    	
    }//END for (int i = 0; i < hebcal_date.size; i++)
 }//END def hebcal = { response ->
httpGet(urlRequest, hebcal);
}//END def queryHebcal()


//This function gets candle lighting time
def HebCal_GetTime12(hebcal_title){
def returnTime = hebcal_title.split(":")[1] + ":" + hebcal_title.split(":")[2] + " "
return returnTime
}//END def HebCal_GetTime12()

//This function gets candle lighting time
def HebCal_GetTime24(hebcal_date){
def returnTime = hebcal_date.split("T")[1]
returnTime = returnTime.split("-")[0]
return returnTime
}//END def HebCal_GetTime12()

/*-----------------------------------------------
 END OF HEBCAL FUNCTIONS
-----------------------------------------------*/
def setChag()
{
	
	if (location.mode != startMode) 
	{
		if (location.modes?.find{it.name == startMode}) 
        {
			setLocationMode(startMode)
			//sendMessage("Changed the mode to '${startMode}'")
            def dayofweek = new Date().format("EEE")
    		if(dayofweek=='Fri'){
				sendMessage("Shabbat Shalom!")
    		}
    		else{
    			sendMessage("Chag Sameach!")
    		}
            
		}//END if (location.modes?.find{it.name == startMode})
		else 
        {
			sendMessage("Tried to change to undefined mode '${startMode}'")
		}//END else
	}//END if (location.mode != newMode)  
    
    unschedule("setChag")
}//END def setChag()


def endChag()
{
	
	if (location.mode != endMode) 
	{
		if (location.modes?.find{it.name == endMode}) 
        {
			setLocationMode(endMode)
			sendMessage("Changed the mode to '${endMode}'")
		}//END if (location.modes?.find{it.name == endMode})
		else 
        {
			sendMessage("Tried to change to undefined mode '${endMode}'")
		}//END else
	}//END if (location.mode != endMode)
    
	//sendMessage("Shavuah Tov!")
    unschedule("endChag")
}//END def setChag()

def sendMessage(msg){
if ( sendPushMessage != "No" ) {
        log.debug( "sending push message" )
        //sendPush( msg )
    }

    if ( phone ) {
        log.debug( "sending text message" )
        sendSms( phone, msg )
    }
}//END def sendMessage(msg)
