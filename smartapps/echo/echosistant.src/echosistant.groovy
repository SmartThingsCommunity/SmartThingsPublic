/* 
 * EchoSistant - The Ultimate Voice and Text Messaging Assistant Using Your Alexa Enabled Device.
 
 
 ************************************ FOR INTERNAL USE ONLY ******************************************************
							
 								DON'T FORGET TO UPDATE RELEASE NUMBER!!!!!
 
 ************************************ FOR INTERNAL USE ONLY ******************************************************
 *		7/14/2017		Version:4.0 R.0.3.4 	Bug fix for UK		
 *		4/05/2017		Version:4.0 R.0.3.3e	Minor UI changes & added "cut on/cut off" commands
 *		4/03/2017		Version:4.0 R.0.3.3c 	Bug Fixes and various other things
 *		3/29/2017		Version:4.0 R.0.3.3b	change to virtual person commands
 *		3/28/2017		Version:4.0 R.0.3.3		minor bug fixes
 *		3/21/2017		Version:4.0 R.0.3.2		minor bug fixes
 *		3/18/2017		Version:4.0 R.0.3.1c	Addition of the Zwave Thermostat Manager Add-On Module and feedback bug fix
 *		3/14/2017		Version:4.0 R.0.3.0  	Enabled running Reporting Profile, Bug fix for windows, doors, and lights feedback/ reconfigured / improved responses and commands
 *		2/17/2017		Version:4.0 R.0.0.0		Public Release 
 *
 *  Copyright 2016 Jason Headley & Bobby Dobrescu
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 * //UPDATE VERSION
/**********************************************************************************************************************************************/
definition(
	name			: "EchoSistant",
    namespace		: "Echo",
    author			: "JH/BD",
	description		: "The Ultimate Voice Controlled Assistant Using Alexa Enabled Devices.",
	category		: "My Apps",
    singleInstance	: true,
	iconUrl			: "https://raw.githubusercontent.com/BamaRayne/Echosistant/master/smartapps/bamarayne/echosistant.src/app-Echosistant.png",
	iconX2Url		: "https://raw.githubusercontent.com/BamaRayne/Echosistant/master/smartapps/bamarayne/echosistant.src/app-Echosistant@2x.png",
	iconX3Url		: "https://raw.githubusercontent.com/BamaRayne/Echosistant/master/smartapps/bamarayne/echosistant.src/app-Echosistant@2x.png")
/**********************************************************************************************************************************************
	UPDATE LINE 38 TO MATCH RECENT RELEASE
**********************************************************************************************************************************************/
private def textVersion() {
	def text = "4.0"
}
private release() {
    def text = "R.0.3.4"
}
/**********************************************************************************************************************************************/
preferences {   
    page name: "mainParentPage"
    		page name: "mIntent"				
            	page name: "mDevices"
                page name: "mDefaults" 
            	page name: "mSHMSec"
                	page name: "mSecuritySuite" // links Parent to Security Add-ON
    				page name: "mNotifyProfile" // links Parent to Notification Add-ON
                    page name: "mThermoManager" // links Parent to Thermostat Manager Add-ON
            		page name: "mProfiles" // links Parent to Profiles Add-ON 
            page name: "mSupport"
            page name: "mSettings"
           		page name: "mSkill"
                    page name: "mControls"
            		page name: "mDeviceDetails" 
                page name: "mTokens"
                    page name: "mConfirmation"            
                    	page name: "mTokenReset"
            page name: "mBonus"
            	page name: "mDashboard"
                	page name: "mDashConfig"
                    page name: "pageTwo"
                    page name: "mWeatherConfig"
                    page name: "scheduled"
}            
//dynamic page methods
page name: "mainParentPage"
    def mainParentPage() {	
       dynamicPage(name: "mainParentPage", title:"", install: true, uninstall:false) {
       		section ("") {
                href "mIntent", title: "Main Home Control", description: mIntentD(), state: mIntentS(),
                	image: "https://raw.githubusercontent.com/BamaRayne/Echosistant/master/smartapps/bamarayne/echosistant.src/Echosistant_Routines.png"    
				href "mProfiles", title: "Configure Profiles", description: mRoomsD(), state: mRoomsS(),
                	image: "https://raw.githubusercontent.com/BamaRayne/Echosistant/master/smartapps/bamarayne/echosistant.src/Echosistant_msg.png"
				href "mSettings", title: "General Settings", description: mSettingsD(), state: mSettingsS(),
                	image: "https://raw.githubusercontent.com/BamaRayne/Echosistant/master/smartapps/bamarayne/echosistant.src/Echosistant_Config.png"
				href "mSupport", title: "Install and Support", description: mSupportD(), state: mSupportS(),
					image: "https://raw.githubusercontent.com/BamaRayne/Echosistant/master/smartapps/bamarayne/echosistant.src/Echosistant_About.png"                               
                    if (activateDashboard) {
                        href "mDashboard", title: "Dashboard", description: mDashboardD(), state: mDashboardS(),
                            image: "https://raw.githubusercontent.com/BamaRayne/Echosistant/master/smartapps/bamarayne/echosistant.src/Echosistant_Dash.png"
                    }
                        href "mBonus", title: "The Current Mode is: ${location.currentMode}" + "\n"  +
                        "Smart Home Monitor Status is: ${location.currentState("alarmSystemStatus")?.value}", description: none
            }
		}
	}           
page name: "mIntent"
    def mIntent() {
    	dynamicPage (name: "mIntent", title: "", install: false, uninstall: false) {
			section("Devices used by EchoSistant") {
	            href "mDevices", title: "Select Devices", description: mDevicesD(), state: mDevicesS()
			}               
            section ("System and Device Control Defaults") {
                href "mDefaults", title: "Change Defaults", description: mDefaultsD(), state: mDefaultsS()
			}
            section ("EchoSistant Security") {
            	href "mSecurity", title: "Configure EchoSistant Security Options", description: mSecurityD(), state: mSecurityS()
            }   
		}
	}
    page name: "mDevices"    
        def mDevices(){
            dynamicPage(name: "mDevices", title: "",install: false, uninstall: false) {
                section ("Select devices", hideWhenEmpty: true){ }
                section ("Lights and Switches", hideWhenEmpty: true){  
                    input "cSwitch", "capability.switch", title: "Allow These Switch(es)...", multiple: true, required: false, submitOnChange: true                   
                    input "cFan", "capability.switchLevel", title: "Allow These Fan(s)...", multiple: true, required: false
                }     
                section ("Garage Doors, Window Coverings and Locks", hideWhenEmpty: true){ 
                	input "cLock", "capability.lock", title: "Allow These Lock(s)...", multiple: true, required: false, submitOnChange: true
                	input "cWindowCover", "capability.windowShade", title: "Allow These Window Covering Devices...", multiple: true, required: false, submitOnChange: true              
                    input "cDoor", "capability.garageDoorControl", title: "Allow These Garage Door(s)...", multiple: true, required: false, submitOnChange: true
					input "cRelay", "capability.switch", title: "Allow These Garage Door Relay(s)...", multiple: false, required: false, submitOnChange: true
                    if (cRelay) input "cContactRelay", "capability.contactSensor", title: "Allow This Contact Sensor to Monitor the Garage Door Relay(s)...", multiple: false, required: false                
                }    
                section ("Climate Control", hideWhenEmpty: true){ 
                 	input "cTstat", "capability.thermostat", title: "Allow These Thermostat(s)...", multiple: true, required: false
                    input "cIndoor", "capability.temperatureMeasurement", title: "Allow These Device(s) to Report the Indoor Temperature...", multiple: true, required: false
                 	input "cOutDoor", "capability.temperatureMeasurement", title: "Allow These Device(s) to Report the Outdoor Temperature...", multiple: true, required: false
                    input "cVent", "capability.switchLevel", title: "Allow These Smart Vent(s)...", multiple: true, required: false
                } 
                section ("Sensors", hideWhenEmpty: true) {
                 	input "cMotion", "capability.motionSensor", title: "Allow These Motion Sensor(s)...", multiple: true, required: false
                    input "cWindow", "capability.contactSensor", title: "Allow These Contact Sensor(s) that are used on a Window(s)...", multiple: true, required: false      
                    input "cDoor1", "capability.contactSensor", title: "Allow These Contact Sensor(s) that are used on a Door(s)...", multiple: true, required: false                     
                    input "cContact", "capability.contactSensor", title: "Allow These Contact Sensor(s) that are NOT used on Doors or Windows...", multiple: true, required: false      
            		input "cWater", "capability.waterSensor", title: "Allow These Water Sensor(s)...", multiple: true, required: false                       
                    input "cPresence", "capability.presenceSensor", title: "Allow These Presence Sensors(s)...", multiple: true, required: false
                }
                section ("Media" , hideWhenEmpty: true){
                    input "cSpeaker", "capability.musicPlayer", title: "Allow These Media Player Type Device(s)...", required: false, multiple: true
                    input "cSynth", "capability.speechSynthesis", title: "Allow These Speech Synthesis Capable Device(s)", multiple: true, required: false
                    input "cMedia", "capability.mediaController", title: "Allow These Media Controller(s)", multiple: true, required: false
                     if (cMedia?.size() > 1) {
                     paragraph "NOTE: only the fist selected device is used by the Main intent. The additional devices MUST be used by Profiles"
                     }
                } 
                section ("Batteries", hideWhenEmpty: true ){
                    input "cBattery", "capability.battery", title: "Allow These Device(s) with Batteries...", required: false, multiple: true
                } 
         }
    }   
    page name: "mDefaults"
        def mDefaults(){
                dynamicPage(name: "mDefaults", title: "", uninstall: false){
                    section ("General Control") {            
                        input "cLevel", "number", title: "Alexa Adjusts Light Levels by using a scale of 1-10 (default is +/-3)", defaultValue: 3, required: false
                        input "cVolLevel", "number", title: "Alexa Adjusts the Volume Level by using a scale of 1-10 (default is +/-2)", defaultValue: 2, required: false
                        input "cTemperature", "number", title: "Alexa Automatically Adjusts temperature by using a scale of 1-10 (default is +/-1)", defaultValue: 1, required: false						
                    }
                    section ("Fan Control") {            
                        input "cHigh", "number", title: "Alexa Adjusts High Level to 99% by default", defaultValue: 99, required: false
                        input "cMedium", "number", title: "Alexa Adjusts Medium Level to 66% by default", defaultValue: 66, required: false
                        input "cLow", "number", title: "Alexa Adjusts Low Level to 33% by default", defaultValue: 33, required: false
                        input "cFanLevel", "number", title: "Alexa Automatically Adjusts Ceiling Fans by using a scale of 1-100 (default is +/-33%)", defaultValue: 33, required: false
                    }
                    section ("Activity Defaults") {            
                        input "cLowBattery", "number", title: "Alexa Provides Low Battery Feedback when the Bettery Level falls below... (default is 25%)", defaultValue: 25, required: false
                        input "cInactiveDev", "number", title: "Alexa Provides Inactive Device Feedback when No Activity was detected for... (default is 24 hours) ", defaultValue: 24, required: false
                    }
					section ("Alexa Voice Settings") {            
                        input "pDisableContCmds", "bool", title: "Disable Conversation (Alexa no longer prompts for additional commands except for 'try again' if an error ocurs)?", required: false, defaultValue: false
                        input "pEnableMuteAlexa", "bool", title: "Disable Feedback (Silence Alexa - it no longer provides any responses)?", required: false, defaultValue: false
                        input "pUseShort", "bool", title: "Use Short Alexa Answers (Alexa provides quick answers)?", required: false, defaultValue: false
                    }
                    section ("HVAC Filters Replacement Reminders", hideWhenEmpty: true, hideable: true, hidden: false) {
						input "cFilterReplacement", "number", title: "Alexa Automatically Schedules HVAC Filter Replacement in this number of days (default is 90 days)", defaultValue: 90, required: false                        
                        input "cFilterSynthDevice", "capability.speechSynthesis", title: "Send Audio Notification when due, to this Speech Synthesis Type Device(s)", multiple: true, required: false
                        input "cFilterSonosDevice", "capability.musicPlayer", title: "Send Audio Notification when due, to this Sonos Type Device(s)", required: false, multiple: true   
                        if (cFilterSonosDevice) {
                            input "volume", "number", title: "Temporarily change volume", description: "0-100%", required: false
                        }
						if (location.contactBookEnabled){
                        	input "recipients", "contact", title: "Send Text Notification when due, to this recipient(s) ", multiple: true, required: false
           				}
                        else {      
                            input name: "sms", title: "Send Text Notification when due, to this phone(s) ", type: "phone", required: false
                        		paragraph "You may enter multiple phone numbers separated by comma (E.G. 8045551122,8046663344)"
                            input "push", "bool", title: "Send Push Notification too?", required: false, defaultValue: false
                        }
                     }
                    section ("Weather Settings") {
                        href "mWeatherConfig", title: "Tap here to configure the Weather defaults", description: "", state: complete
                    }                     
                }
        }
        page name: "mSecurity"    
            def mSecurity(){
                dynamicPage(name: "mSecurity", title: "",install: false, uninstall: false) {
                section ("Set PIN Number to Unlock Security Features") {
                    input "cPIN", "password", title: "Use this PIN for ALL Alexa Controlled Controls", default: false, required: false, submitOnChange: true
                    //input "cTempPIN", "password", title: "Guest PIN (expires in 24 hours)", default: false, required: false, submitOnChange: true
                }
				if (cPIN){
                	section("PIN Number Restrictions") {
            			href "pRestrict", title: "Only prompt for PIN number when...", description: pRestrictComplete(), state: pRestrictSettings()
					}
                    section ("Configure Security Options for Alexa") {
                    	def routines = location.helloHome?.getPhrases()*.label.sort()
                        input "cMiscDev", "capability.switch", title: "Allow these Switches to be PIN Protected...", multiple: true, required: false, submitOnChange: true
                        input "cRoutines", "enum", title: "Allow these Routines to be PIN Protected...", options: routines, multiple: true, required: false
                        input "uPIN_SHM", "bool", title: "Enable PIN for Smart Home Monitor?", default: false, submitOnChange: true
                            if(uPIN_SHM == true)  {paragraph "You can also say: Alexa enable/disable the pin number for Security"} 
                        input "uPIN_Mode", "bool", title: "Enable PIN for Location Modes?", default: false, submitOnChange: true
                            if(uPIN_Mode == true)  {paragraph "You can also say: Alexa enable/disable the pin number for Location Modes"} 
							if (cMiscDev) 			{input "uPIN_S", "bool", title: "Enable PIN for Switch(es)?", default: false, submitOnChange: true}
                            	if(uPIN_S == true)  {paragraph "You can also say: Alexa enable/disable the pin number for Switches"} 
                            if (cTstat) 			{input "uPIN_T", "bool", title: "Enable PIN for Thermostats?", default: false, submitOnChange: true}
                            	if(uPIN_T == true)  {paragraph "You can also say: Alexa enable/disable the pin number for Thermostats"}                             
                            if (cDoor || cRelay) 	{input "uPIN_D", "bool", title: "Enable PIN for Doors?", default: false, submitOnChange: true}
                            	if(uPIN_D == true)  {paragraph "You can also say: Alexa enable/disable the pin number for Doors"}                             
                            if (cLock) 				{input "uPIN_L", "bool", title: "Enable PIN for Locks?", default: false, submitOnChange: true}
                            	if(uPIN_L == true)  {paragraph "You can also say: Alexa enable/disable the pin number for Locks"}                             
                    }
                }
// Moved to                   section ("Access Security Suite") {
// location of                       href "mSecuritySuite", title: "Tap to configure your Home Security Suite module", description: ""
// other profiles                   } 
                        	
                section ("Smart Home Monitor Status Change Feedback", hideWhenEmpty: true, hideable: true, hidden: true){
                    input "fSecFeed", "bool", title: "Activate SHM status change announcements.", default: false, submitOnChange: true
                    if (fSecFeed) {    
                        input "shmSynthDevice", "capability.speechSynthesis", title: "On this Speech Synthesis Type Devices", multiple: true, required: false
                        input "shmSonosDevice", "capability.musicPlayer", title: "On this Sonos Type Devices", required: false, multiple: true, submitOnChange: true    
                        }
                        if (shmSonosDevice) {
                            input "volume", "number", title: "Temporarily change volume", description: "0-100%", required: false
                            }
                    }
                }
        	}
		
            page name: "pRestrict"
                def pRestrict(){
                    dynamicPage(name: "pRestrict", title: "", uninstall: false) {
                        section ("Mode Restrictions") {
                            input "modes", "mode", title: "Only when mode is", multiple: true, required: false
                        }        
                        section ("Days - only on these days"){	
                            input "days", title: "Only on certain days of the week", multiple: true, required: false, submitOnChange: true,
                                "enum", options: ["Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"]
                        }
                        section ("Time - only during these times"){
                            href "certainTime", title: "Only during a certain time", description: timeIntervalLabel ?: "Tap to set", state: timeIntervalLabel ? "complete" : null
                        }
                        section ("State - only when opening/unlocking "){
                            input "pinOnOpen", "bool", title: "Only when opening/unlocking a door", default: false, submitOnChange: true
                        }
                    }
                }        
        page name: "mSecuritySuite"    
                    def mSecuritySuite() {
                        dynamicPage (name: "mSecuritySuite", title: "", install: true, uninstall: false) {
                            if (childApps?.size()) {  
                                section("Security Suite",  uninstall: false){
                                    app(name: "security", appName: "SecuritySuite", namespace: "Echo", title: "Configure Security Suite", multiple: true,  uninstall: false)
                                }
                            }
                            else {
                                section("Security Suite",  uninstall: false){
                                    paragraph "NOTE : Looks like you haven't created any Profiles yet.\n \nPlease make sure you have installed the Rooms Smart App Add-on before creating a new Room!"
                                    app(name: "security", appName: "SecuritySuite", namespace: "Echo", title: "Configure Security Suite", multiple: true,  uninstall: false)
                                }
                            }
                       }
                    }
	page name: "mProfiles"    
        def mProfiles() {
            dynamicPage(name: "mProfiles", title:"", install: true, uninstall: false) {
				
                section ("Messaging & Control (${getChildSize("Profiles")})") {
                	href "mMainProfile", title: "View and Create Messaging & Control Profiles...", description: none
                    }
				if (notifyOn) {
        			section ("Notifications & Reporting (${getChildSize("NotificationProfile")})") {
  						href "mNotifyProfile", title: "View and Create Notification & Reporting Profiles...", description: none
					}
                }               
                if (thermoOn) {
                	section ("Climate Control (${getChildSize("ThermoManager")})") {
                    	href "mThermoManager", title: "View and Create Climate Control Profiles...", description: none
                    }    
				}
				if (remindOn) {
        			section ("Reminders & Events (${getChildSize("Reminders")})") {
  						app(name: "reminder", appName: "Reminders", namespace: "Echo", title: "Access Reminders & Events...", multiple: false,  uninstall: false)
					}
                }    
                if (securityOn) {
                	section ("Security Suite (${getChildSize("SecuritySuite")})") {
                    	href "mSecuritySuite", title: "View and Configure the Security Suite Profiles...", description: ""
                    }    
                }                   
            }            
		}
        page name: "mMainProfile"    
            def mMainProfile() {
                dynamicPage (name: "mMainProfile", title: "", install: true, uninstall: false) {
                    if (childApps?.size()>0) {  
                        section("Messaging & Control",  uninstall: false){
                            app(name: "Profiles", appName: "Profiles", namespace: "Echo", title: "Create a New Messaging & Control Profile", multiple: true,  uninstall: false)
                        }
                    }
                    else {
                        section("Messaging & Control",  uninstall: false){
                            paragraph "NOTE: Looks like you haven't created any Profiles yet.\n \nPlease make sure you have installed the  Echo : Profiles Add-on before creating a new Profile!"
                            app(name: "Profiles", appName: "Profiles", namespace: "Echo", title: "Create a New Messaging & Control Profile", multiple: true,  uninstall: false)
						}
					}
				}
            }  
		page name: "mNotifyProfile"    
            def mNotifyProfile() {
                dynamicPage (name: "mNotifyProfile", title: "", install: true, uninstall: false) {
                    if (childApps?.size()) {  
                        section("Notifications & Reporting",  uninstall: false){
                            app(name: "notification", appName: "NotificationProfile", namespace: "Echo", title: "Create a new Notification & Reporting Profile", multiple: true,  uninstall: false)
                        }
                    }
                    else {
                        section("Notifications & Reporting",  uninstall: false){
                            paragraph "NOTE: Looks like you haven't created any Notifications yet.\n \nPlease make sure you have installed the Echo : NotificationProfile Add-on before creating a new Room!"
                            app(name: "notification", appName: "NotificationProfile", namespace: "Echo", title: "Create a new Notification & Reporting Profile", multiple: true,  uninstall: false)
                        }
                    }
             	}
	        }       
    page name: "mThermoManager"
    		def mThermoManager() {
            	dynamicPage (name: "mThermoManager", title: "", install: true, uninstall: false) {
                	if (childApps?.size()) {
                    	section("Climate Control", uninstall: false){
                        	app(name: "ZWave Thermostat Manager", appName: "ThermoManager", namespace: "Echo", title: "View and Create Climate Control Profiles...", multiple: true, uninstall: false)
                            }
                        }
                        else {
                        	section("Climate Control", uninstall: false){
                            paragraph "NOTE: Looks like you haven't initialized the Thermostat Manager Add-on yet.\n \nPlease make sure you have installed the Echo : Thermostat Manager Add-on before creating a new Room!"
                        	app(name: "ZWave Thermostat Manager", appName: "ThermoManager", namespace: "Echo", title: "View and Create Climate Control Profiles...", multiple: true, uninstall: false)
                        }
                    }
             	}
	        }
        
page name: "mSettings"  
	def mSettings(){
        dynamicPage(name: "mSettings", uninstall: true) {
                section("Debugging") {
                    input "debug", "bool", title: "Enable Debug Logging", default: true, submitOnChange: true 
                    }
                section ("Apache License"){
                    input "ShowLicense", "bool", title: "Show License", default: false, submitOnChange: true
                    def msg = textLicense()
                        if (ShowLicense) paragraph "${msg}"
                    }
                section ("Security Token", hideable: true, hidden: true) {
                	paragraph ("Log into the IDE on your computer and navigate to the Live Logs tab. Leave that window open, come back here, and open this section")
                    paragraph "The Security Tokens are now displayed in the Live Logs section of the IDE"
    				log.trace 	"\nLAMBDA CREDENTIALS (copy/paste in Lambda code (between the breaks): \n" +
                    			"\n---------------------------------------------------------------------------------------\n" +
                    			"\nvar STappID = '${app.id}' \n var STtoken = '${state.accessToken}'\n" +
                   				"var url= '${apiServerUrl("/api/smartapps/installations/")}' + STappID + '/' ;\n" +
                                "\n---------------------------------------------------------------------------------------"
                    paragraph 	"Access token:\n"+
                                                "${state.accessToken}\n"+
                                                "Application ID:\n"+
                                                "${app.id}"
                    href "mTokens", title: "Revoke/Reset Security Access Token", description: none
                }
                section("Tap below to remove the ${textAppName()} application.  This will remove ALL Profiles and the App from the SmartThings mobile App."){
                }	
			}             
		}
    page name: "mSkill"
        def mSkill(){
			dynamicPage(name: "mSkill", uninstall: false) {
                section ("List of Devices") {
                    href "mDeviceDetails", title: "View your List of Devices for copy & paste to the AWS Skill...", description: "", state: "complete" 
                }
                section ("List of Controls") {
                    href "mControls", title: "View your List of Controls for copy & paste to the AWS Skill...", description: "", state: "complete" 
                }                
            }
      }    
        page name: "mDeviceDetails"
            def mDeviceDetails(){
                    dynamicPage(name: "mDeviceDetails", uninstall: false) {
                        section ("LIST_OF_DEVICES") { 
                            def DeviceList = getDeviceDetails()
                            def url = "${getApiServerUrl()}/api/smartapps/installations/${app.id}/devList?access_token=${state.accessToken}"
                                paragraph ("${DeviceList}")
                                log.info "\nLIST_OF_DEVICES \ncopy/paste this link in a browser: " + url +
                                "\n${DeviceList}"	
                                href "", title: "Open LIST_OF_DEVICES in a Browser", style: "external", url: url, required: false, 
                                description: "Click here"
                         }
                	}
         }
         page name: "mControls"
            def mControls(){
                    dynamicPage(name: "mControls", uninstall: false) {
                    section ("LIST_OF_SYSTEM_CONTROLS") { 
                        def DeviceList = getControlDetails()
                        def url = "${getApiServerUrl()}/api/smartapps/installations/${app.id}/cntrlList?access_token=${state.accessToken}"
                            paragraph ("${DeviceList}")
                            log.info "\nLIST_OF_SYSTEM_CONTROLS \ncopy/paste this link in a browser: " + url +
                            "\n${DeviceList}"
                            href "", title: "Open LIST_OF_SYSTEM_CONTROLS in a Browser", style: "external", url: url, required: false, 
                            description: "Click here"                            
                                }
                            }
                        }
        page name: "mTokens"
            def mTokens(){
                    dynamicPage(name: "mTokens", title: "Security Tokens", uninstall: false){
                        section(""){
                            paragraph "Tap below to Reset/Renew the Security Token. You must log in to the IDE and open the Live Logs tab before tapping here. "+
                            "Copy and paste the displayed tokens into your Amazon Lambda Code."
                            if (!state.accessToken) {
                                OAuthToken()
                                paragraph "You must enable OAuth via the IDE to setup this app"
                                }
                            }
                                def msg = state.accessToken != null ? state.accessToken : "Could not create Access Token. "+
                                "OAuth may not be enabled. Go to the SmartApp IDE settings to enable OAuth."
                        section ("Reset Access Token / Application ID"){
                            href "mConfirmation", title: "Reset Access Token and Application ID", description: none
                            }
                        }
                    } 
            page name: "mConfirmation"
                def mConfirmation(){
                        dynamicPage(name: "mConfirmation", title: "Reset/Renew Access Token Confirmation", uninstall: false){
                            section {
                                href "mTokenReset", title: "Reset/Renew Access Token", description: "Tap here to confirm action - READ WARNING BELOW"
                                paragraph "PLEASE CONFIRM! By resetting the access token you will disable the ability to interface this SmartApp with your Amazon Echo."+
                                "You will need to copy the new access token to your Amazon Lambda code to re-enable access." +
                                "Tap below to go back to the main menu with out resetting the token. You may also tap Done above."
                                }
                            section(" "){
                                href "mainParentPage", title: "Cancel And Go Back To Main Menu", description: none 
                                }
                            }
                        }
                    page name: "mTokenReset"
                        def mTokenReset(){
                                dynamicPage(name: "mTokenReset", title: "Access Token Reset", uninstall: false){
                                    section{
                                        revokeAccessToken()
                                        state.accessToken = null
                                        OAuthToken()
                                        def msg = state.accessToken != null ? "New access token:\n${state.accessToken}\n\n" : "Could not reset Access Token."+
                                        "OAuth may not be enabled. Go to the SmartApp IDE settings to enable OAuth."
                                        paragraph "${msg}"
                                        paragraph "The new access token and app ID are now displayed in the Live Logs tab of the IDE."
                                        log.info "New IDs: STappID = '${app.id}' , STtoken = '${state.accessToken}'"
                                    }
                                    section(" "){ 
                                        href "mainParentPage", title: "Tap Here To Go Back To Main Menu", description: none 
                                        }
                                    }
                                }
page name: "mSupport"  
 def mSupport(){
        dynamicPage(name: "mSupport", uninstall: false) {
        	section ("EchoSistant Modules") {
            	paragraph "For the notifications and room feedback to be operational, they must be installed in the ST IDE and the toggles below must be activated"
                input "notifyOn", "bool", title: "Is the Notifications Module Installed? ", required: true, defaultValue: false
                input "remindOn", "bool", title: "Is the Reminders Module Installed? ", required: true, defaultValue: false
                input "securityOn", "bool", title: "Is the Security Suite Module Installed?", required: true, defaultValue: false
                input "thermoOn", "bool", title: "Is the Climate Control Module Installed?", required: true, defaultValue: false
                }
                section ("Amazon AWS Skill Details") {
					href "mSkill", title: "Tap to view setup data for the AWS Main Intent Skill...", description: ""
            		}
                section ("Directions, How-to's, and Troubleshooting") { 
 					href url:"http://thingsthataresmart.wiki/index.php?title=EchoSistant", title: "Tap to go to the EchoSistant Wiki", description: none,
                		image: "https://raw.githubusercontent.com/BamaRayne/Echosistant/master/smartapps/bamarayne/echosistant.src/wiki.png"
                	}   
            	section ("AWS Lambda website") {
            		href url:"https://aws.amazon.com/lambda/", title: "Tap to go to the AWS Lambda Website", description: none,
                		image: "https://raw.githubusercontent.com/BamaRayne/Echosistant/master/smartapps/bamarayne/echosistant.src/Echosistant_aws.png"
                	}
            	section ("Amazon Developer website") {    
   					href url:"https://developer.amazon.com/", title: "Tap to go to Amazon Developer website", description: none,
                		image: "https://raw.githubusercontent.com/BamaRayne/Echosistant/master/smartapps/bamarayne/echosistant.src/Echosistant_Skills.png"
					}
                section ("Developers", hideWhenEmpty: true){  
            		paragraph ("You can reach out to the Echosistant Developers with the following information: \n" + 
                	"Jason Headley \n"+
                	"Forum user name @bamarayne \n" +
                	"Bobby Dobrescu \n"+
                	"Forum user name @SBDobrescu")
                	}
                }	            	
            }   
page name: "mBonus"    
    def mBonus(){
        dynamicPage(name: "mBonus", title: "EchoSistant Bonus Features",install: false, uninstall: false) {
        section ("Home Status Dashboard") {
        	input "activateDashboard", "bool", title: "Activate the DashBoard on the Home Page", required: false, default: false, submitOnChange: true
        	}
        if (activateDashboard) {
		section ("Configure the DashBoard") {
        	href "mDashConfig", title: "Tap here to configure Dashboard", description: "", state: complete
			}
        }
	}
}        
page name: "mDashboard"
	def mDashboard(){
        dynamicPage(name: "mDashboard", uninstall: false) {
        section("Scheduled Reminders and Events"){
        	href "scheduled", title: "Tap here to view scheduled reminders and events", description: "", state: complete
            }
        if (mLocalWeather) {
            section("Today's Forecast"){
                paragraph (mGetWeather())
        }
        }
		def activeAlert = mGetWeatherAlerts()
		if (activeAlert){
        	section("Active Weather Alerts"){
            	paragraph (activeAlert)
        	}
        }
        section ("ThermoStats and Temperature") {
        	def tStat1 = ThermoStat1
            def temp1 = (tStat1?.currentValue("temperature"))
            def setPC1 = (tStat1?.currentValue("coolingSetpoint"))
            def setPH1 = (tStat1?.currentValue("heatingSetpoint"))
            def mode1 = (tStat1?.currentValue("thermostatMode"))
            def oper1 = (tStat1?.currentValue("thermostatOperatingState"))
            def tStat2 = ThermoStat2
            def temp2 = (tStat2?.currentValue("temperature"))
            def setPC2 = (tStat2?.currentValue("coolingSetpoint"))
            def setPH2 = (tStat2?.currentValue("heatingSetpoint"))
            def mode2 = (tStat2?.currentValue("thermostatMode"))
            def oper2 = (tStat2?.currentValue("thermostatOperatingState"))
		if ("${mode1}" == "auto") 
        	paragraph "The ${tStat1} is ${temp1}°. The thermostat is in ${mode1} mode, the heat is set to ${setPH1}°, the cooling is set to ${setPC1}°, and it is currently ${oper1}."
        if ("${mode1}" == "cool")
            paragraph "The ${tStat1} is ${temp1}°. The thermostat is set to ${setPC1}°, is in ${mode1} mode and is currently ${oper1}."
        if ("${mode1}" == "heat")
            paragraph "The ${tStat1} is ${temp1}°. The thermostat is set to ${setPH1}°, is in ${mode1} mode and is currently ${oper1}."
        if ("${mode1}" == "off")
        	paragraph "The ${tStat1} thermostat is currently ${mode1}, and the temperature is ${temp1}°." 
		if ("${mode2}" == "auto") 
        	paragraph "The ${tStat2} is ${temp2}°. The thermostat is in ${mode2} mode, the heat is set to ${setPH2}°, the cooling is set to ${setPC2}°, and it is currently ${oper2}."
        if ("${mode2}" == "cool")
            paragraph "The ${tStat2} is ${temp2}°. The thermostat is set to ${setPC2}°, is in ${mode2} mode and is currently ${oper2}."
        if ("${mode2}" == "heat")
            paragraph "The ${tStat2} is ${temp2}°. The thermostat is set to ${setPH2}°, is in ${mode2} mode and is currently ${oper2}."
        if ("${mode2}" == "off")
        	paragraph "The ${tStat2} thermostat is currently ${mode2}, and the temperature is ${temp2}°." 
		}
		section ("Temperature Sensors") {
        	def Sens1temp = (tempSens1?.currentValue("temperature"))
            def Sens2temp = (tempSens2?.currentValue("temperature"))
            def Sens3temp = (tempSens3?.currentValue("temperature"))
            def Sens4temp = (tempSens4?.currentValue("temperature"))
            def Sens5temp = (tempSens5?.currentValue("temperature"))
            if (tempSens1)
            	paragraph "The temperature of the ${tempSens1} is ${Sens1temp}°."
            if (tempSens2)
            	paragraph "The temperature of the ${tempSens2} is ${Sens2temp}°."
            if (tempSens3)
            	paragraph "The temperature of the ${tempSens3} is ${Sens3temp}°."
            if (tempSens4)
            	paragraph "The temperature of the ${tempSens4} is ${Sens4temp}°."
            if (tempSens5)
            	paragraph "The temperature of the ${tempSens5} is ${Sens5temp}°."
			}
		} 
	}
page name: "scheduled"  // display scheduled events on the dashboard add by JH 3/26/2017
	def scheduled(){
    	dynamicPage(name: "scheduled", uninstall: false) {
    	def remMsg = state.esEvent.eText
        def remDate = state.esEvent.eStartingDate
        def remTime = state.esEvent.eStartingTime
    	section ("Scheduled Events") {
        	if (state.filterNotif != null) {
            paragraph "${state.filterNotif}"
            }
        }
        section ("Upcoming Reminders") {
			if (remMsg != null) {
			paragraph "Reminder Scheduled: $remMsg on $remDate at $remTime"
            }
    	}
    }    
}
page name: "mDashConfig"
	def mDashConfig(){
        dynamicPage(name: "mDashConfig", uninstall: false) {
        section ("Local Weather") {
        	input "mLocalWeather", "bool", title: "Display local weather conditions on Dashboard", required: false, default: false, submitOnChange: true
            }
        if (mLocalWeather) {
		section ("Local Weather Information") {
            href "mWeatherConfig", title: "Tap here to configure Weather information on Dashboard", description: "", state: complete
			}
        }            
		section ("Thermoststats") {
        	input "ThermoStat1", "capability.thermostat", title: "First ThermoStat", required: false, default: false, submitOnChange: true 
        	input "ThermoStat2", "capability.thermostat", title: "Second ThermoStat", required: false, default: false, submitOnChange: true 
            }
        section ("Temperature Sensors") {
        	input "tempSens1", "capability.temperatureMeasurement", title: "First Temperature Sensor", required: false, default: false, submitOnChange: true 
            input "tempSens2", "capability.temperatureMeasurement", title: "Second Temperature Sensor", required: false, default: false, submitOnChange: true 
            input "tempSens3", "capability.temperatureMeasurement", title: "Third Temperature Sensor", required: false, default: false, submitOnChange: true 
            input "tempSens4", "capability.temperatureMeasurement", title: "Fourth Temperature Sensor", required: false, default: false, submitOnChange: true 
            input "tempSens5", "capability.temperatureMeasurement", title: "Fifth Temperature Sensor", required: false, default: false, submitOnChange: true 
        }
    }
}
def mWeatherConfig() {
	dynamicPage(name: "mWeatherConfig", title: "Weather Settings") {
		section {
    		input "wMetric", "bool", title: "Report Weather In Metric Units\n(°C / km/h)", defaultValue: false, required: false, submitOnChange: true 
            input "wZipCode", "text", title: "Zip Code (If Location Not Set)", required: "false"
		}
	}
}                   
/*************************************************************************************************************
   CREATE INITIAL TOKEN
************************************************************************************************************/
def OAuthToken(){
	try {
		createAccessToken()
		log.debug "Creating new Access Token"
	} catch (e) {
		log.error "Access Token not defined. OAuth may not be enabled. Go to the SmartApp IDE settings to enable OAuth."
	}
}
/*************************************************************************************************************
   LAMBDA DATA MAPPING
************************************************************************************************************/
mappings {
	path("/cntrlList") {action: [GET: "controlList"]}	
    path("/devList") {action: [GET: "deviceList"]}
    path("/b") { action: [GET: "processBegin"] }
	path("/c") { action: [GET: "controlDevices"] }
	path("/f") { action: [GET: "feedbackHandler"] }
	path("/r") { action: [GET: "remindersHandler"] }
	path("/s") { action: [GET: "controlSecurity"] }
	path("/t") { action: [GET: "processTts"] }
}
/*************************************************************************************************************
   LIST OF ITEMS FOR LAMBDA
************************************************************************************************************/
def deviceList() {
	def DeviceList = getDeviceHtml()
	def html = """
			<!DOCTYPE HTML>
				<html>
					<head><title>LIST_OF_DEVICES</title></head>
						<body><p>${DeviceList}</p></body>
				</html>
		"""
	render contentType: "text/html", data: html                             
}
def controlList() {
	def cntrlList = getControlHtml()
    def html = """
		<!DOCTYPE HTML>
				<html>
					<head><title>LIST_OF_DEVICES</title></head>
						<body><p>${cntrlList}</p></body>
				</html>
		"""
	render contentType: "text/html", data: html                             
}
/************************************************************************************************************
		Base Process
************************************************************************************************************/
def installed() {
	if (debug) log.debug "Installed with settings: ${settings}"
    state.ParentRelease = release()
    runEvery1Hour(mGetWeatherUpdates)
    //Reminders
    state.esEvent = [:]
}
def updated() { 
	if (debug) log.debug "Updated with settings: ${settings}"
    unsubscribe()
    state.esEvent = [:]
    initialize()
}
def initialize() {
        webCoRE_init()
        //WEATHER UPDATES
        runEvery1Hour(mGetWeatherUpdates)
        state.lastWeatherCheck
        state.lastWeatherUpdate
        //CoRE and other 3rd party apps
        sendLocationEvent(name: "echoSistant", value: "refresh", data: [profiles: getProfileList()] , isStateChange: true, descriptionText: "echoSistant Profile list refresh")
        def children = getChildApps()
    	if (debug) log.debug "Refreshing Profiles for 3rd party apps, ${getChildApps()*.label}"
        if (!state.accessToken) {
        	if (debug) log.error "Access token not defined. Attempting to refresh. Ensure OAuth is enabled in the SmartThings IDE."
                OAuthToken()
			}
        //SHM status change and keypad initialize
    		subscribe(location, locationHandler)
            subscribe(location, "alarmSystemStatus",alarmStatusHandler) //used for ES speaker feedback
        	subscribe(location, "remindR", runReport) //used for running ES Profiles from RemindR app
        	state.esProfiles = state.esProfiles ? state.esProfiles : []
        //State Variables            
//            state.lastMessage = null
//            state.lastIntent  = null
//            state.lastTime  = null
            state.lambdaReleaseTxt = "Not Set"
            state.lambdaReleaseDt = "Not Set" 
            state.lambdatextVersion = "Not Set"
        //Alexa Responses
			state.pTryAgain = false
        	state.pContCmds = settings.pDisableContCmds == false ? true : settings.pDisableContCmds == true ? false : true
            state.pMuteAlexa = settings.pEnableMuteAlexa
			state.pShort = settings.pUseShort
            state.pContCmdsR = "init"       
        //PIN Settings
            state.usePIN_T = settings.uPIN_T
            state.usePIN_L = settings.uPIN_L
            state.usePIN_D = settings.uPIN_D
            state.usePIN_S = settings.uPIN_S             
			state.usePIN_SHM = settings.uPIN_SHM
            state.usePIN_Mode = settings.uPIN_Mode
            state.savedPINdata = null
            state.pinTry = null
        //Other Settings
            state.scheduledHandler
//            state.filterNotif = null
//            state.lastAction = null
//			state.lastActivity = null
			state.pendingConfirmation = false
            unschedule("startLoop")
            unschedule("continueLoop")

            
	}
/************************************************************************************************************
		CoRE Integration
************************************************************************************************************/
def listEchoSistantProfiles() {
log.warn "child requesting esProfiles"
	return state.esProfiles = state.esProfiles ? state.esProfiles : []
}

def getProfileList(){
		return getChildApps()*.label
		if (debug) log.debug "Refreshing Profiles for CoRE, ${getChildApps()*.label}"
}
def childUninstalled() {
	if (debug) log.debug "Profile has been deleted, refreshing Profiles for CoRE, ${getChildApps()*.label}"
    sendLocationEvent(name: "echoSistant", value: "refresh", data: [profiles: getProfileList()] , isStateChange: true, descriptionText: "echoSistant Profile list refresh")
} 

def getChildSize(child) {
	def childList = []
	def childMasterApp
    childApps.each {ch ->
        	childMasterApp = ch.app.name
		if (childMasterApp == child) {
        	String children  = (String) ch.label
            childList += children
      	}
 	}
    return childList.size()
}
def remindrHandler(evt) {
	if (!evt) return
    log.warn "received event from RemindR with data: $evt.data"
	switch (evt.value) {
		case "refresh":
		state.esProfiles = evt.jsonData && evt.jsonData?.profiles ? evt.jsonData.profiles : []
			break
	}
}

//NEW webCoRE Integration
private webCoRE_handle(){return'webCoRE'}
private webCoRE_init(pistonExecutedCbk){state.webCoRE=(state.webCoRE instanceof Map?state.webCoRE:[:])+(pistonExecutedCbk?[cbk:pistonExecutedCbk]:[:]);subscribe(location,"${webCoRE_handle()}.pistonList",webCoRE_handler);if(pistonExecutedCbk)subscribe(location,"${webCoRE_handle()}.pistonExecuted",webCoRE_handler);webCoRE_poll();}
private webCoRE_poll(){sendLocationEvent([name: webCoRE_handle(),value:'poll',isStateChange:true,displayed:false])}
public  webCoRE_execute(pistonIdOrName,Map data=[:]){def i=(state.webCoRE?.pistons?:[]).find{(it.name==pistonIdOrName)||(it.id==pistonIdOrName)}?.id;if(i){sendLocationEvent([name:i,value:app.label,isStateChange:true,displayed:false,data:data])}}
public  webCoRE_list(mode){def p=state.webCoRE?.pistons;if(p)p.collect{mode=='id'?it.id:(mode=='name'?it.name:[id:it.id,name:it.name])}}
public  webCoRE_handler(evt){switch(evt.value){case 'pistonList':List p=state.webCoRE?.pistons?:[];Map d=evt.jsonData?:[:];if(d.id&&d.pistons&&(d.pistons instanceof List)){p.removeAll{it.iid==d.id};p+=d.pistons.collect{[iid:d.id]+it}.sort{it.name};state.webCoRE = [updated:now(),pistons:p];};break;case 'pistonExecuted':def cbk=state.webCoRE?.cbk;if(cbk&&evt.jsonData)"$cbk"(evt.jsonData);break;}}

/************************************************************************************************************
		Begining Process - Lambda via page b
************************************************************************************************************/
def processBegin(){ 
    def versionTxt  = params.versionTxt 		
    def versionDate = params.versionDate
    def releaseTxt = params.releaseTxt
    def event = params.intentResp
        state.lambdaReleaseTxt = releaseTxt
        state.lambdaReleaseDt = versionDate
        state.lambdatextVersion = versionTxt
    def versionSTtxt = textVersion()
    def releaseSTtxt = release()
    def pPendingAns = false 
    def pContinue = state.pMuteAlexa
    def pShort = state.pShort
    def String outputTxt = (String) null 
    	state.pTryAgain = false

    if (debug) log.debug "^^^^____LAUNCH REQUEST___^^^^" 
    if (debug) log.debug "Launch Data: (event) = '${event}', (Lambda version) = '${versionTxt}', (Lambda release) = '${releaseTxt}', (ST Main App release) = '${releaseSTtxt}'"

//try {
    if (event == "noAction") {//event == "AMAZON.NoIntent" removed 1/20/17
    	state.pinTry = null
        state.savedPINdata = null
        state.pContCmdsR = null // added 1/20/2017
        state.pTryAgain = false
    }
// >>> NO Intent <<<<    
    if (event == "AMAZON.NoIntent"){
    	if(state.pContCmdsR == "level" || state.pContCmdsR == "repeat"){
            if (state.lastAction != null) {
            	if (state.pContCmdsR == "level") {state.pContCmdsR = "repeat"}
                def savedData = state.lastAction
                outputTxt = controlHandler(savedData) 
                pPendingAns = "level"
            }
            else {
                state.pContCmdsR = null
                pPendingAns = null
            }
        }
        if( state.pContCmdsR == "door"){
            if (state.lastAction != null) {
                state.lastAction = null
                state.pContCmdsR = null 
                pPendingAns = null 
            }
        }
        if( state.pContCmdsR == "feedback" ||  state.pContCmdsR == "bat" || state.pContCmdsR == "act" ){
            if (state.lastAction != null) {
                state.lastAction = null
                state.pContCmdsR = null 
                pPendingAns = null 
            }
        }
        if( state.pContCmdsR == "init" || state.pContCmdsR == "undefined"){
        	state.pTryAgain = false
        }
        if( state.pContCmdsR == null){
        	state.pTryAgain = false
        }
    }
// >>> YES Intent <<<<     
    if (event == "AMAZON.YesIntent") {
        if (state.pContCmdsR == "level" || state.pContCmdsR == "repeat") {
            state.pContCmdsR = null
            state.lastAction = null
            pPendingAns = "level"
        }
        else {
        	state.pTryAgain = false
        }
        if(state.pContCmdsR == "door"){
            if (state.lastAction != null) {
                def savedData = state.lastAction
 				//NEW PIN VALIDATION!!!!! ///// ADD THE THE usePIN variable below to run the PIN VALIDATION
 				if(state.usePIN_D == true) {
     				//RUN PIN VALIDATION PROCESS
                	def pin = "undefined"
               		def command = "validation"
                	def num = 0
                	def unit = "doors"
                	outputTxt = pinHandler(pin, command, num, unit)
                    pPendingAns = "pin"
                    if (state.pinTry == 3) {pPendingAns = "undefined"}
                    log.warn "try# ='${state.pinTry}'"
					return ["outputTxt":outputTxt, "pContinue":pContinue, "pShort":pShort, "pPendingAns":pPendingAns, "versionSTtxt":versionSTtxt]
            	}
                else {
                outputTxt = controlHandler(savedData) 
                pPendingAns = "door"
            	}
        	}
        }
        if(state.pContCmdsR == "feedback"){
            if (state.lastAction != null) {
                def savedData = state.lastAction
                outputTxt = getMoreFeedback(savedData) 
                pPendingAns = "feedback"
				return ["outputTxt":outputTxt, "pContinue":pContinue,  "pShort":pShort, "pPendingAns":pPendingAns, "versionSTtxt":versionSTtxt]
            }
         }
         if(state.pContCmdsR == "bat" || state.pContCmdsR == "act"){
            if (state.lastAction != null) {
                def savedData = state.lastAction
                outputTxt = savedData
                pPendingAns = "feedback"
                state.pContCmdsR = null
				return ["outputTxt":outputTxt, "pContinue":pContinue,  "pShort":pShort, "pPendingAns":pPendingAns, "versionSTtxt":versionSTtxt]
            }
       }
       if(state.pContCmdsR == "caps"){
            if (state.lastAction!= null) {
                outputTxt = state.lastAction
                pPendingAns = "caps"
				state.pContCmdsR = null 
				state.lastAction = null
                return ["outputTxt":outputTxt, "pContinue":pContinue, "pShort":pShort, "pPendingAns":pPendingAns, "versionSTtxt":versionSTtxt]
            }
        }        
     }
// >>> Handling a Profile Intent <<<<      
     if (!event.startsWith("AMAZON") && event != "main" && event != "security" && event != "feedback" && event != "profile" && event != "noAction"){
		childApps?.each {child ->
			if (child?.label.toLowerCase() == event?.toLowerCase()) { 
                pContinue = child?.checkState()  
            }
       	}
        //if Alexa is muted from the child, then mute the parent too / MOVED HERE ON 2/9/17
        pContinue = pContinue == true ? true : state.pMuteAlexa == true ? true : pContinue
		return ["outputTxt":outputTxt, "pContinue":pContinue, "pShort":pShort, "pPendingAns":pPendingAns, "versionSTtxt":versionSTtxt]	     
	}
	if (debug){
    	log.debug "Begining Process data: (event) = '${event}', (ver) = '${versionTxt}', (date) = '${versionDate}', (release) = '${releaseTxt}'"+ 
      	"; data sent: pContinue = '${pContinue}', pShort = '${pShort}',  pPendingAns = '${pPendingAns}', versionSTtxt = '${versionSTtxt}', releaseSTtxt = '${releaseSTtxt}' outputTxt = '${outputTxt}' ; "+
        "other data: pContCmdsR = '${state.pContCmdsR}', pinTry'=${state.pinTry}' "
	}
    return ["outputTxt":outputTxt, "pContinue":pContinue, "pShort":pShort, "pPendingAns":pPendingAns, "versionSTtxt":versionSTtxt]	 

} 
/*catch (Throwable t) {
        log.error t
        outputTxt = "Oh no, something went wrong. If this happens again, please reach out for help!"
        state.pTryAgain = true
        return ["outputTxt":outputTxt, "pContinue":pContinue, "pShort":pShort, "pPendingAns":pPendingAns, "versionSTtxt":versionSTtxt]
	}
}   */
/************************************************************************************************************
		FEEDBACK - from Lambda via page f
************************************************************************************************************/
def feedbackHandler() {
    //LAMBDA
	def fDevice = params.fDevice
   	def fQuery = params.fQuery
    def fOperand = params.fOperand 
    def fCommand = params.fCommand 
    def fIntentName = params.intentName
    def pPIN = false
    //OTHER 
    def String deviceType = (String) null
    def String outputTxt = (String) null
    def String result = (String) null
    def String deviceM = (String) null
	def currState
    def stateDate
    def stateTime
	def data = [:]
    	
        
        fDevice = fDevice == "null" ? "undefined" : fDevice
        def nDevice = fDevice == "null" ? "undefined" : fDevice
        log.warn "nDevice is $nDevice"
        fDevice = fDevice.replaceAll("[^a-zA-Z0-9 ]", "") 
		fQuery = fQuery == "null" ? "undefined" : fQuery
        fOperand = fOperand == "null" ? "undefined" : fOperand
        fCommand = fCommand == "null" ? "undefined" : fCommand
    
    if (debug){
        log.debug 	"Feedback data: (fDevice) = '${fDevice}', "+
    				"(fQuery) = '${fQuery}', (fOperand) = '${fOperand}', (fCommand) = '${fCommand}', (fIntentName) = '${fIntentName}'"}
	def fProcess = true
    state.pTryAgain = false
		
	fOperand = fOperand == "lights on" ? "lights" : fOperand == "switches on" ? "lights" : fOperand == "switches" ? "lights" : fOperand
	fCommand = fOperand == "lights on" ? "on" : fOperand == "switches on" ? "on" : fCommand
    
	if (ctCommand == "this is a test"){
		outputTxt = "Congratulations! Your EchoSistant is now setup properly" 
		return ["outputTxt":outputTxt, "pContCmds":state.pContCmds, "pShort":state.pShort, "pContCmdsR":state.pContCmdsR, "pTryAgain":state.pTryAgain, "pPIN":pPIN]       
    }
//try {
    if (fDevice == "undefined" && fQuery == "undefined" && fOperand == "undefined" && fCommand == "undefined") {
		outputTxt = "Sorry, I didn't get that, "
        state.pTryAgain = true
        state.pContCmdsR = "clear"
        state.lastAction = null
        return ["outputTxt":outputTxt, "pContCmds":state.pContCmds, "pShort":state.pShort, "pContCmdsR":state.pContCmdsR, "pTryAgain":state.pTryAgain, "pPIN":pPIN]
	}    
    else {
  		if (fDevice != "undefined" && (fQuery == "get" ||  fQuery == "create" || fQuery == "generate" || fQuery == "give")){
            def pintentName
           		childApps.each {child ->
                        def ch = child.label
                        	ch = ch.replaceAll("[^a-zA-Z0-9 ]", "")
                		if (ch.toLowerCase() == fDevice?.toLowerCase()) { 
                    		if (debug) log.debug "Found a profile"
                            pintentName = child.label
                            def dataSet = [ptts:ptts, pintentName:pintentName] 
                            def childRelease = child?.checkRelease()
                            log.warn "childRelease = $childRelease"
                            outputTxt = child.runProfile(pintentName)
						}
            	}
                if(!outputTxt)outputTxt = "Sorry I wasn't able to find a report named " + fDevice
                return ["outputTxt":outputTxt, "pContCmds":state.pContCmds, "pShort":state.pShort, "pContCmdsR":state.pContCmdsR, "pTryAgain":state.pTryAgain, "pPIN":pPIN]	
         }
         if (fDevice != "undefined" && fQuery != "undefined" && fOperand != "undefined") {
            if (fQuery.contains ("is ") || fQuery.contains ("if ") || fQuery == "is" || fQuery == "if" || fQuery == "is the") {
                def deviceMatch = cRelay?.find {d -> d.label.toLowerCase() == fDevice?.toLowerCase()}
                    if(deviceMatch && cContactRelay) {// changed by Jason 2/24/2017
                        outputTxt =  cContactRelay.latestValue("contact").contains(fOperand) ? "yes, the ${deviceMatch} is ${fOperand}" : "no, the ${deviceMatch} is not ${fOperand}"
                        return ["outputTxt":outputTxt, "pContCmds":state.pContCmds, "pShort":state.pShort, "pContCmdsR":state.pContCmdsR, "pTryAgain":state.pTryAgain, "pPIN":pPIN]
                    }
                    else {
                        if (deviceMatch == null && cDoor1) {// changed by Jason 2/24/2017
                            deviceMatch = cDoor1?.find {d -> d.label.toLowerCase() == fDevice?.toLowerCase()}
                             if(deviceMatch) outputTxt =  deviceMatch.latestValue("contact").contains(fOperand) ? "yes, the ${deviceMatch} is ${fOperand}" : "no, the ${deviceMatch} is not ${fOperand}"
                        }
                        if (deviceMatch == null && cContact) {// changed by Jason 2/24/2017
                            deviceMatch = cContact?.find {d -> d.label.toLowerCase() == fDevice?.toLowerCase()}
                            if(deviceMatch) outputTxt =  deviceMatch.latestValue("contact").contains(fOperand) ? "yes, the ${deviceMatch} is ${fOperand}" : "no, the ${deviceMatch} is not ${fOperand}"
                        } 
                    	if (deviceMatch == null && cLock) {// changed by Jason 2/24/2017
                        	deviceMatch = cLock?.find {d -> d.label.toLowerCase() == fDevice?.toLowerCase()}
                        	if(deviceMatch) {
                            	currState = deviceMatch.latestValue("lock")
                            	currState = currState == "${fOperand}" ? "yes, the ${deviceMatch} is ${fOperand}" : "no, the ${deviceMatch} is not ${fOperand}"
                            	outputTxt =  currState
                        	}
                    	}                        
                        if (deviceMatch == null && cSwitch) {// changed by Jason 2/24/2017
                            deviceMatch = cSwitch?.find {d -> d.label.toLowerCase() == fDevice?.toLowerCase()} 
                            if(deviceMatch) outputTxt = deviceMatch.latestValue("switch").contains(fOperand) ? "yes, the ${deviceMatch} is ${fOperand}" : "no, the ${deviceMatch} is not ${fOperand}"
                        }
                        if (deviceMatch == null && cMotion) {// changed by Jason 2/24/2017
                            deviceMatch = cMotion?.find {d -> d.label.toLowerCase() == fDevice?.toLowerCase()}
                            if(deviceMatch) {
                                currState = deviceMatch.currentValue("motion")
                                currState = currState == "active" ? "yes, the ${deviceMatch} is ${fOperand}" : "no, the ${deviceMatch} is not ${fOperand}"
                                outputTxt =  currState
                            }
                        }
						if (deviceMatch == null && cPresence) {  // changed by Jason 2/24/2017
                            deviceMatch = cPresence.find {d -> d.label.toLowerCase() == fDevice?.toLowerCase()}  	
                               	if(fOperand == "home" || fOperand == "here" || fOperand == "present" || fOperand == "in" || fOperand == "at home") {
                                	outputTxt = deviceMatch.latestValue("presence")?.contains("not") ? "no, ${deviceMatch} is not ${fOperand}" : "yes, ${deviceMatch} is ${fOperand}"
									}
                                }    
                        if (deviceMatch == null && cWater) {// changed by Jason 2/24/2017
                            deviceMatch = cWater?.find {d -> d.label.toLowerCase() == fDevice?.toLowerCase()}	
                            if(deviceMatch) outputTxt =  deviceMatch.latestValue("water").contains(fOperand) ? "yes, the ${deviceMatch} is ${fOperand}" : "no, the ${deviceMatch} is not ${fOperand}"
                        }
                        if (deviceMatch == null && cSpeaker) {// changed by Jason 2/24/2017
                            deviceMatch = cSpeaker?.find {d -> d.label.toLowerCase() == fDevice?.toLowerCase()}	
                            if(deviceMatch) outputTxt =  deviceMatch.latestValue("mute").contains(fOperand) ? "yes, the ${deviceMatch} is ${fOperand}" : "no, the ${deviceMatch} is not ${fOperand}"
                        }
                        if (deviceMatch == null && fOperand == "running" && cTstat ) {// changed by Jason 2/24/2017
                            deviceMatch = cTstat?.find {d -> d.label.toLowerCase() == fDevice?.toLowerCase()}
                            if(deviceMatch) {
                                currState = deviceMatch.latestValue("thermostatOperatingState")
                                currState = currState == "cooling" ? "yes, ${deviceMatch} is ${fOperand}" : currState == "heating" ? "yes, the ${deviceMatch} is ${fOperand}" : "no, the ${deviceMatch} is not ${fOperand}"
                                outputTxt =  currState
                            }
                        }
                        if(outputTxt) return ["outputTxt":outputTxt, "pContCmds":state.pContCmds, "pShort":state.pShort, "pContCmdsR":state.pContCmdsR, "pTryAgain":state.pTryAgain, "pPIN":pPIN]
                   }
            }
        }
        if (fDevice != "undefined" && fQuery != "undefined" && fOperand == "undefined" && fQuery != "about" && fQuery != "get" ) {            
		def dMatch = deviceMatchHandler(fDevice)
      	if (dMatch?.deviceMatch == null) { 				
        	outputTxt = "Sorry, I couldn't find any details about " + fDevice
            state.pTryAgain = true
        	return ["outputTxt":outputTxt, "pContCmds":state.pContCmds, "pShort":state.pShort, "pContCmdsR":state.pContCmdsR, "pTryAgain":state.pTryAgain, "pPIN":pPIN]
      	}
        else {
        	def dDevice = dMatch?.deviceMatch
            def dType = dMatch?.deviceType
            def dState = dMatch?.currState
            def dMainCap = dMatch?.mainCap
            def dCapCount = getCaps(dDevice,dType, dMainCap, dState)
            state.pContCmdsR = "caps"
                	
                    if (state.pShort != true){ 
                    outputTxt = "I couldn't quite get that, but " + fDevice +  " has " + dCapCount + " capabilities. Would you like to hear more about this device?"         
                	}
                    else {outputTxt = "I didn't catch that, but " + fDevice +  " has " + dCapCount + " capabilities. Want to hear more?"} 
        			return ["outputTxt":outputTxt, "pContCmds":state.pContCmds, "pShort":state.pShort, "pContCmdsR":state.pContCmdsR, "pTryAgain":state.pTryAgain, "pPIN":pPIN]
            }
        }   
        if (fOperand == "undefined" && fQuery != "undefined" && fQuery != "who" && !fQuery.contains ("when")) {        
                def deviceMatch=cTstat?.find {d -> d.label.toLowerCase() == fDevice?.toLowerCase()}
                    if(deviceMatch)	{
                            deviceType = "cTstat"
                            def currentMode = deviceMatch.latestValue("thermostatMode")
                            def currentHSP = deviceMatch.latestValue("heatingSetpoint") 
                            def currentCSP = deviceMatch.latestValue("coolingSetpoint") 
                            def currentTMP = deviceMatch.latestValue("temperature")
                            int temp = currentTMP
                            int hSP = currentHSP
                            int cSP = currentCSP
                            stateDate = deviceMatch.currentState("temperature").date
                            stateTime = deviceMatch.currentState("temperature").date.time
                            def timeText = getTimeVariable(stateTime, deviceType)            
                            outputTxt = "The " + fDevice + " temperature is " + temp + " degrees and the current mode is " + currentMode + " , with set points of " + cSP + " for cooling and " + hSP + " for heating"
        					return ["outputTxt":outputTxt, "pContCmds":state.pContCmds, "pShort":state.pShort, "pContCmdsR":state.pContCmdsR, "pTryAgain":state.pTryAgain, "pPIN":pPIN]
                    }
                    else {
                        if (fDevice != "undefined") {
                             if (fDevice != "undefined"){
                                def rSearch = deviceMatchHandler(fDevice)
                                    if (rSearch?.deviceMatch == null) { 
                                        outputTxt = "Sorry, I couldn't find any details about " + fDevice
                                        state.pTryAgain = true
        								return ["outputTxt":outputTxt, "pContCmds":state.pContCmds, "pShort":state.pShort, "pContCmdsR":state.pContCmdsR, "pTryAgain":state.pTryAgain, "pPIN":pPIN]
                                    }
                                    else {
                                        deviceM = rSearch?.deviceMatch
                                        outputTxt = deviceM + " has been " + rSearch?.currState + " since " + rSearch?.tText
                                        return ["outputTxt":outputTxt, "pContCmds":state.pContCmds, "pShort":state.pShort, "pContCmdsR":state.pContCmdsR, "pTryAgain":state.pTryAgain, "pPIN":pPIN]	
                                    }                  
                                if (rSearch.deviceType == "cBattery") {
                                    outputTxt = "The battery level for " + deviceM + " is " + rSearch.currState + " and was last recorded " + rSearch.tText
                                	return ["outputTxt":outputTxt, "pContCmds":state.pContCmds, "pShort":state.pShort, "pContCmdsR":state.pContCmdsR, "pTryAgain":state.pTryAgain, "pPIN":pPIN]
                                }
                                if (rSearch.deviceType == "cMedia") {
                                    outputTxt = rSearch.currState + " since " + rSearch.tText
                                    return ["outputTxt":outputTxt, "pContCmds":state.pContCmds, "pShort":state.pShort, "pContCmdsR":state.pContCmdsR, "pTryAgain":state.pTryAgain, "pPIN":pPIN]
                                }
                            }
                        }
                        else {
                            outputTxt = "Sorry, I didn't get that, "
                            state.pTryAgain = true
                            state.pContCmdsR = "clear"
                            state.lastAction = null
                            return ["outputTxt":outputTxt, "pContCmds":state.pContCmds, "pShort":state.pShort, "pContCmdsR":state.pContCmdsR, "pTryAgain":state.pTryAgain, "pPIN":pPIN]
                        }
					}  
        }
        else {
//>>> Temp >>>>      
            if(fOperand == "temperature") {
                if(cTstat){
                    cTstat?.find {s -> 
                        if(s.label.toLowerCase() == fDevice?.toLowerCase()){
                            deviceType = "cTstat"
                            def currentTMP = s.latestValue("temperature")
                            int temp = currentTMP
                            stateDate = s.currentState("temperature").date
                            stateTime = s.currentState("temperature").date.time
                            def timeText = getTimeVariable(stateTime, deviceType)            
                            outputTxt = "The temperature " + fDevice + " is " + temp + " degrees and was recorded " + timeText.currDate + " at " + timeText.currTime
                        }
                    }
                    if (outputTxt != null) {
                    return ["outputTxt":outputTxt, "pContCmds":state.pContCmds, "pShort":state.pShort, "pContCmdsR":state.pContCmdsR, "pTryAgain":state.pTryAgain, "pPIN":pPIN]
                    }            
                }
                if(cMotion){
                    cMotion.find {s -> 
                        if(s.label.toLowerCase() == fDevice?.toLowerCase()){
                            deviceType = "cTstat"
                            def currentTMP = s.latestValue("temperature")
                            int temp = currentTMP
                            stateDate = s.currentState("temperature").date
                            stateTime = s.currentState("temperature").date.time
                            def timeText = getTimeVariable(stateTime, deviceType)
                            outputTxt = "The temperature in the " + fDevice + " is " + temp + " degrees and was recorded " + timeText.currDate + " at " + timeText.currTime
                        }
                    }
                    if (outputTxt != null) {
                    return ["outputTxt":outputTxt, "pContCmds":state.pContCmds, "pShort":state.pShort, "pContCmdsR":state.pContCmdsR, "pTryAgain":state.pTryAgain, "pPIN":pPIN]
                    }            
                }
                if(cWater){
                    cWater.find {s -> 
                        if(s.label.toLowerCase() == fDevice?.toLowerCase()){
                            deviceType = "cWater"
                            def currentTMP = s.latestValue("temperature")
                            int temp = currentTMP
                            stateDate = s.currentState("temperature").date
                            stateTime = s.currentState("temperature").date.time
                            def timeText = getTimeVariable(stateTime, deviceType)            
                            outputTxt = "The temperature of " + fDevice + " is " + temp + " degrees and was recorded " + timeText.currDate + " at " + timeText.currTime
                        }
                    }
                    if (outputTxt != null) {
                    return ["outputTxt":outputTxt, "pContCmds":state.pContCmds, "pShort":state.pShort, "pContCmdsR":state.pContCmdsR, "pTryAgain":state.pTryAgain, "pPIN":pPIN]
                    }
                }            
                if (outputTxt == null && fDevice != "undefined") { 
                    outputTxt = "Device named " + fDevice + " doesn't have a temperature sensor" 
                    return ["outputTxt":outputTxt, "pContCmds":state.pContCmds, "pShort":state.pShort, "pContCmdsR":state.pContCmdsR, "pTryAgain":state.pTryAgain, "pPIN":pPIN]
                }
                else {
                    if(cIndoor){
                        def sensors = cIndoor?.size()
                        def tempAVG = cIndoor ? getAverage(cIndoor, "temperature") : "undefined device"          
                        def currentTemp = tempAVG
                        outputTxt = "The indoor temperature is " + currentTemp
                        return ["outputTxt":outputTxt, "pContCmds":state.pContCmds, "pShort":state.pShort, "pContCmdsR":state.pContCmdsR, "pTryAgain":state.pTryAgain, "pPIN":pPIN]
                    }
                    else {
                    	if(state.pShort != true) {
                     		outputTxt = "Sorry, I couldn't quite get that, what device would you like to use to get the indoor temperature?"
                        }
                        else {outputTxt = "Oops, I didn't get that, what device?"}
                		return ["outputTxt":outputTxt, "pContCmds":state.pContCmds, "pShort":state.pShort, "pContCmdsR":state.pContCmdsR, "pTryAgain":state.pTryAgain, "pPIN":pPIN]
                    }
                } 
            }
//>>> Temp >>>>>
            if (fOperand == "temperature inside" || fOperand == "indoor temperature" || fOperand == "temperature is inside"){
                if(cIndoor){
                    def sensors = cIndoor?.size()
                    def tempAVG = cIndoor ? getAverage(cIndoor, "temperature") : "undefined device"          
                    def currentTemp = tempAVG
                    outputTxt = "The indoor temperature is " + currentTemp
                    return ["outputTxt":outputTxt, "pContCmds":state.pContCmds, "pShort":state.pShort, "pContCmdsR":state.pContCmdsR, "pTryAgain":state.pTryAgain, "pPIN":pPIN]
                }
                else {
                    outputTxt = "There are no indoor sensors selected, please go to the SmartThings app and select one or more sensors"
                    return ["outputTxt":outputTxt, "pContCmds":state.pContCmds, "pShort":state.pShort, "pContCmdsR":state.pContCmdsR, "pTryAgain":state.pTryAgain, "pPIN":pPIN]
                }                            
            }
//>>> Temp >>>>
            if (fOperand == "temperature outside" || fOperand == "outdoor temperature" || fOperand == "temperature is outside" || fOperand == "hot outside" || fOperand == "cold outside"){
                if(cOutDoor){
                    def sensors = cOutDoor?.size()
                    def tempAVG = cOutDoor ? getAverage(cOutDoor, "temperature") : "undefined device"          
                    def currentTemp = tempAVG
                    def forecastT = mGetWeatherTemps()
                    outputTxt = forecastT + ",. The current temperature is " + currentTemp
                    return ["outputTxt":outputTxt, "pContCmds":state.pContCmds, "pShort":state.pShort, "pContCmdsR":state.pContCmdsR, "pTryAgain":state.pTryAgain, "pPIN":pPIN]
                }
                else {
                    outputTxt = "There are no outdoor sensors selected, go to the SmartThings app and select one or more sensors"
                    return ["outputTxt":outputTxt, "pContCmds":state.pContCmds, "pShort":state.pShort, "pContCmdsR":state.pContCmdsR, "pTryAgain":state.pTryAgain, "pPIN":pPIN]		
                }                            
            }
//>>> Weather >>>>
            if (fOperand.contains("weather") || fOperand.contains("forecast")){
            //Full forecast
            if (fOperand == "forecast" || fOperand == "weather" || fOperand == "weather forecast" || fOperand.contains("outside") || fOperand== "current forecast" || fOperand == "current weather" ){
				outputTxt = mGetWeather()
            }
            if (fOperand.contains("today") || fOperand.contains("tonight") || fOperand.contains("tomorrow") ) {
                def period = fOperand.contains("today") ? "today" : fOperand.contains("tonight") ? "tonight" : fOperand.contains("tomorrow") ? "tomorrow" : null
				outputTxt = mGetWeatherShort(period)       
            }
			if (fOperand.contains("update") ||fOperand.contains("change") || fCommand.contains("change") ){
				outputTxt = mGetWeatherUpdates()
            }
            if (fOperand.contains("alert") || fOperand.contains("warning")){
				outputTxt = mGetWeatherAlerts()
            }
            return ["outputTxt":outputTxt, "pContCmds":state.pContCmds, "pShort":state.pShort, "pContCmdsR":state.pContCmdsR, "pTryAgain":state.pTryAgain, "pPIN":pPIN]	
            }
			if (fOperand.contains("wind ") || fOperand == "windy" || fOperand.contains("rain") || fOperand == "precipitation" || fOperand.contains("UV ") || fOperand.contains("condition")){
				def wElement = fOperand.contains("wind") ? "wind" : fOperand.contains("rain") ? "rain" : fOperand == "precipitation" ? "precip" : fOperand.contains("UV ") ? "uv" : fOperand == "weather conditions"? "cond" : null
				outputTxt = mGetWeatherElements(wElement)
				return ["outputTxt":outputTxt, "pContCmds":state.pContCmds, "pShort":state.pShort, "pContCmdsR":state.pContCmdsR, "pTryAgain":state.pTryAgain, "pPIN":pPIN]		
            }
			if (fOperand == "outside humidity" || fOperand== "humid is outside" || fOperand== "outside humidity" || fOperand== "current conditions"){
				def wElement = fOperand.contains("humid") ? "humid" : fOperand.contains("current ") ? "cond" : null
				outputTxt = mGetWeatherElements(wElement)
				return ["outputTxt":outputTxt, "pContCmds":state.pContCmds, "pShort":state.pShort, "pContCmdsR":state.pContCmdsR, "pTryAgain":state.pTryAgain, "pPIN":pPIN]		
            } 
//>>> Mode >>>>
            if (fOperand == "mode" ){
                    outputTxt = "The Current Mode is " + location.currentMode      
                    return ["outputTxt":outputTxt, "pContCmds":state.pContCmds, "pShort":state.pShort, "pContCmdsR":state.pContCmdsR, "pTryAgain":state.pTryAgain, "pPIN":pPIN]			
            }
//>>> Profile Messages >>>>
            if (fOperand == "messages" && fDevice != "undefined"){		
            def pintentName
            def ptts
           		childApps.each {child ->
                        def ch = child.label
                        	ch = ch.replaceAll("[^a-zA-Z0-9]", "")
                		if (ch.toLowerCase() == fDevice?.toLowerCase()) { 
                    		if (debug) log.debug "Found a profile"
                            pintentName = child.label
                    		if(fCommand == "delete") ptts = "delete all messages "
                            else ptts = "how many messages "
							def childRelease = child?.checkRelease()
                            log.warn "childRelease = $childRelease"
                            def dataSet = [ptts:ptts, pintentName:pintentName] 
                    		def pResponse = child.profileEvaluate(dataSet)
                            	outputTxt = pResponse?.outputTxt
						}
            	}
                return ["outputTxt":outputTxt, "pContCmds":state.pContCmds, "pShort":state.pShort, "pContCmdsR":state.pContCmdsR, "pTryAgain":state.pTryAgain, "pPIN":pPIN]	
            }
//>>> Security >>>>
            //TO DO: restrict security based on command
            if (fOperand == "smart home monitor" || fOperand == "alarm system" ){
                    def sSHM = location.currentState("alarmSystemStatus")?.value       
                    sSHM = sSHM == "off" ? "disabled" : sSHM == "away" ? "Armed Away" : sSHM == "stay" ? "Armed Home" : "unknown"
                    outputTxt = "Your Smart Home Monitor Status is " +  sSHM
                    return ["outputTxt":outputTxt, "pContCmds":state.pContCmds, "pShort":state.pShort, "pContCmdsR":state.pContCmdsR, "pTryAgain":state.pTryAgain, "pPIN":pPIN]				
            }
//>>> Lights >>>>            
            if(fOperand.contains("lights") || fOperand.contains("anything") || fOperand.contains("on") || fOperand.contains("off") || fCommand.contains("on")) { 
            	if(fOperand == "on" && fCommand == "undefined") {
                	fCommand = "on" }
                if(fOperand == "off" && fCommand == "undefined") {
                	fCommand = "off" }
                    if(cSwitch){
                    def devList = []
                    if (cSwitch?.latestValue("switch")?.contains(fCommand)) {
                        cSwitch?.each { deviceName ->
                                    if (deviceName.latestValue("switch")=="${fCommand}") {
                                        String device  = (String) deviceName
                                        devList += device
                                    }
                        		}
							}
                    if (fQuery == "what's" || fQuery == "what is" || fQuery == "what" || fQuery == "which" || fQuery == "any" || fQuery == "is") { // removed fQuery == "undefined" 2/13
                        if (devList?.size() > 0) {
                            if (devList?.size() == 1) {
                                outputTxt = "There is one light " + fCommand + " , would you like to know which one? "                           			
                            }
                            else {
                                outputTxt = "There are " + devList?.size() + " lights " + fCommand + " , would you like to know which lights? "
                            }
						data.devices = devList
                        data.cmd = fCommand
                        data.deviceType = "cSwitch"
                        state.lastAction = data
                        state.pContCmdsR = "feedback"
                        }
                        else {outputTxt = "There are no lights " + fCommand}
                        return ["outputTxt":outputTxt, "pContCmds":state.pContCmds, "pShort":state.pShort, "pContCmdsR":state.pContCmdsR, "pTryAgain":state.pTryAgain, "pPIN":pPIN]	
                    	}
                    }
				}
//>>> Doors & Windows >>>>     // Added by Jason to ask "is anything open" on 2/27/2017         
            if(fOperand.contains("anything") || fOperand.contains("open") || fOperand.contains("closed")) {
            	if(fOperand == "open" && fCommand == "undefined") {
                	fCommand = "open" }
                if(fOperand == "closed" && fCommand == "undefined") {
                	fCommand = "closed" }   
                    def devListDoors = []
                    def devListWindows = []
                    if(cDoor1){
                        if (cDoor1?.currentValue("contact").contains(fCommand)) {
                            cDoor1?.each { deviceName ->
                                        if (deviceName.currentValue("contact")=="${fCommand}") {
                                            String device  = (String) deviceName
                                            devListDoors += device
                                        	}
                                    	}
                                	}
                    			}
                    if(cWindow) {
                        if (cWindow?.currentValue("contact").contains(fCommand)) {
                            cWindow?.each { deviceName ->
                                        if (deviceName.currentValue("contact")=="${fCommand}") {
                                            String device  = (String) deviceName
                                            devListWindows += device
                                        	}
                                    	}
                                	}
                    			}
                    if (fQuery == "what's" || fQuery == "what is" || fQuery == "what" || fQuery == "which" || fQuery == "any" || fQuery.contains ("is")) { // removed fQuery == "undefined" 2/13
                        if (devListDoors?.size() == 1 && devListWindows?.size() == 1) {
                            outputTxt = "The following " + devListDoors?.size() + " door is open, " + devListDoors + " , as well as the following " + devListWindows?.size() + " window, " + devListWindows
                            }
                            else if (devListDoors?.size() > 0 && devListWindows?.size() == 0) {
                            	outputTxt = "The following " + devListDoors?.size() + " doors are open, " + devListDoors
                                }
                            else if (devListDoors?.size() == 0 && devListWindows?.size() > 0) {
                            	outputTxt = "The following " + devListWindows?.size() + " windows are open, " + devListWindows  
                                }
                            else if (devListDoors?.size() == 0 && devListWindows?.size() > 0) {
                            	outputTxt = "There are no doors or windows open"
                                }
                            else {
                            outputTxt = "The following " + devListDoors?.size() + " doors are open, " + devListDoors + " , as well as the following " + devListWindows?.size() + " windows, " + devListWindows                      
                      		}
                        data.cmd = fCommand
                        data.deviceTypeDoors = "cDoor1"
                        data.deviceTypeWindows = "cWindow"
                        data.deviceDoors = devListDoors
                        data.deviceWindows = devListWindows
                        state.lastAction = data
                        state.pContCmdsR = "feedback"
                        	}
                        else {outputTxt = "There are no doors or windows " + fCommand}
                        return ["outputTxt":outputTxt, "pContCmds":state.pContCmds, "pShort":state.pShort, "pContCmdsR":state.pContCmdsR, "pTryAgain":state.pTryAgain, "pPIN":pPIN]	
                    	}
//>>> Doors >>>>     // Mod'd by Jason to ask "which windows are open" on 2/27/2017         
            if(fOperand.contains("door")) { 
                    def devList = []
                    if (cDoor1?.latestValue("contact")?.contains(fCommand)) {
                        cDoor1?.each { deviceName ->
                                    if (deviceName.latestValue("contact")=="${fCommand}") {
                                        String device  = (String) deviceName
                                        devList += device
                                    }
                        		}
							}
                    if (fQuery == "how" || fQuery== "how many" || fQuery == "are there" || fQuery == "any" || fQuery.contains ("if")) { // removed fQuery == "undefined" 2/13
                        if (devList?.size() > 0) {
                            if (devList?.size() == 1) {
                                outputTxt = "There is one door " + fCommand + " , would you like to know which one? "                           			
                            }
                            else {
                                outputTxt = "There are " + devList?.size() + " doors " + fCommand + " , would you like to know which doors? "
                            }
                        data.devices = devList
                        data.cmd = fCommand
                        data.deviceType = "cDoor1"
                        state.lastAction = data
                        state.pContCmdsR = "feedback"
                        }
                        else {outputTxt = "There are no doors " + fCommand}
                        return ["outputTxt":outputTxt, "pContCmds":state.pContCmds, "pShort":state.pShort, "pContCmdsR":state.pContCmdsR, "pTryAgain":state.pTryAgain, "pPIN":pPIN]	
                    }
                    else if (fQuery.contains ("what") || fQuery.contains ("which") || fQuery == "what's") {
                        def devNames = []
                        fOperand = fOperand.contains("close") ? "closed" : fOperand.contains("open") ? "open" : fOperand 
                        fCommand = fCommand.contains("close") ? "closed" : fCommand
                        fCommand = fOperand == "closed" ? "closed" : fOperand == "open" ? "open" : fCommand                  
                            if (cDoor1?.latestValue("contact")?.contains(fCommand)) {
                                cDoor1?.each { deviceName ->
                                            if (deviceName.latestValue("contact")=="${fCommand}") {
                                                String device  = (String) deviceName
                                                devNames += device
                                            }
                                }
                                cDoor1?.each { deviceName ->
                                            if (deviceName.latestValue("contact")=="${fCommand}") {
                                                String device  = (String) deviceName
                                                devNames += device
                                            }
                                }
                            outputTxt = "The following doors are " + fCommand + "," + devNames.sort().unique()
							}
                            else {outputTxt = "There are no doors " + fCommand}
                            return ["outputTxt":outputTxt, "pContCmds":state.pContCmds, "pShort":state.pShort, "pContCmdsR":state.pContCmdsR, "pTryAgain":state.pTryAgain, "pPIN":pPIN]	
                    	}
            		}                  
//>>> Windows >>>>       // Added by Jason to ask "which windows are open" on 2/27/2017     
            if(fOperand.contains("window")) { 
                    def devList = []
                    fCommand = fOperand.contains("open") ? "open" : fOperand.contains("close") ? "closed" : fCommand // to avoid misplaced command as operand 3/3/17 Bobby
                    if (cWindow?.latestValue("contact")?.contains(fCommand)) {
                        cWindow?.each { deviceName ->
                                    if (deviceName.latestValue("contact")=="${fCommand}") {
                                        String device  = (String) deviceName
                                        devList += device
                                    }
                        		}
							}
                    if (fQuery == "any" || fQuery == "how" || fQuery== "how many" || fQuery == "are there" || fQuery.contains ("if")) { // removed fQuery == "undefined" 2/13
                        if (devList?.size() > 0) {
                            if (devList?.size() == 1) {
                                outputTxt = "There is one window " + fCommand + " , would you like to know which one? "                           			
                            }
                            else {
                                outputTxt = "There are " + devList?.size() + " windows " + fCommand + " , would you like to know which windows? "
                            }
                        data.devices = devList
                        data.cmd = fCommand
                        data.deviceType = "cWindow"
                        state.lastAction = data
                        state.pContCmdsR = "feedback"
                        }
                        else {outputTxt = "There are no windows " + fCommand}
                        return ["outputTxt":outputTxt, "pContCmds":state.pContCmds, "pShort":state.pShort, "pContCmdsR":state.pContCmdsR, "pTryAgain":state.pTryAgain, "pPIN":pPIN]	
                    }
                    else if (fQuery.contains ("what") || fQuery.contains ("which") || fQuery == "what's") {
                        def devNames = []
                        fOperand = fOperand.contains("close") ? "closed" : fOperand.contains("open") ? "open" : fOperand 
                        fCommand = fCommand.contains("close") ? "closed" : fCommand
                        fCommand = fOperand == "closed" ? "closed" : fOperand == "open" ? "open" : fCommand                  
                            if (cWindow?.latestValue("contact")?.contains(fCommand)) {
                                cWindow?.each { deviceName ->
                                            if (deviceName.latestValue("contact")=="${fCommand}") {
                                                String device  = (String) deviceName
                                                devNames += device
                                            }
                                }
                                cWindow?.each { deviceName ->
                                            if (deviceName.latestValue("contact")=="${fCommand}") {
                                                String device  = (String) deviceName
                                                devNames += device
                                            }
                                }
                            outputTxt = "The following windows are " + fCommand + "," + devNames.sort().unique()
							}
                            else {outputTxt = "There are no windows " + fCommand}
                            return ["outputTxt":outputTxt, "pContCmds":state.pContCmds, "pShort":state.pShort, "pContCmdsR":state.pContCmdsR, "pTryAgain":state.pTryAgain, "pPIN":pPIN]	
                    	}
            		}    
//>>> Battery Level >>>>                        
            if(fOperand == "batteries" || fOperand == "battery level" || fOperand == "battery" ) {
            	def cap = "bat"
            	def dMatch = deviceMatchHandler(fDevice)	
                if (dMatch?.deviceMatch == null) { 		
                def devList = getCapabilities(cap)
					if(devList instanceof String){
                	outputTxt = devList
                	log.error " devList = ${devList}"
					
                    return ["outputTxt":outputTxt, "pContCmds":state.pContCmds, "pShort":state.pShort, "pContCmdsR":state.pContCmdsR, "pTryAgain":state.pTryAgain, "pPIN":pPIN]	
                	}
                	else {
                   		if (fQuery == "how" || fQuery== "how many" || fQuery == "undefined" || fQuery == "are there" || fCommand == "low" || fQuery == "give" || fQuery == "get") {
                            if (devList.listSize > 0) {
                                if (devList.listSize == 1) {
                                    outputTxt = "There is one device with low battery level , would you like to know which one"                           			
                                }
                                else {
                                    outputTxt = "There are " + devList.listSize + " devices with low battery levels, would you like to know which devices"
                                }
                            def sdevices = devList?.listBat
                            def devListString = sdevices.join(",")
                            data.list = devListString
                            state.lastAction = devListString
                            state.pContCmdsR = "bat"
                            return ["outputTxt":outputTxt, "pContCmds":state.pContCmds, "pShort":state.pShort, "pContCmdsR":state.pContCmdsR, "pTryAgain":state.pTryAgain, "pPIN":pPIN]	
                            }
                            else {outputTxt = "There are no devices with low battery levels"}
                            return ["outputTxt":outputTxt, "pContCmds":state.pContCmds, "pShort":state.pShort, "pContCmdsR":state.pContCmdsR, "pTryAgain":state.pTryAgain, "pPIN":pPIN]	
                        }
                        else if (fQuery.contains ("what") || fQuery.contains ("which")) {
                            if (devList.listSize > 0) {
                            outputTxt = "The following devices have low battery levels " + devList.listBat.sort()//.unique()
                            }
                            else {outputTxt = "There are no devices with low battery levels "
                            } 
                            return ["outputTxt":outputTxt, "pContCmds":state.pContCmds, "pShort":state.pShort, "pContCmdsR":state.pContCmdsR, "pTryAgain":state.pTryAgain, "pPIN":pPIN]	
                        }
                    }
                 }
                 else {
					device = dMatch.deviceMatch
                    currState = device.currentState("battery").value
					stateTime = device.currentState("battery").date.time
                	def timeText = getTimeVariable(stateTime, deviceType)
					outputTxt = "The battery level of " + fDevice + " is " + currState + " percent and was registered " + timeText.currDate + " at " + timeText.currTime
                  	return ["outputTxt":outputTxt, "pContCmds":state.pContCmds, "pShort":state.pShort, "pContCmdsR":state.pContCmdsR, "pTryAgain":state.pTryAgain, "pPIN":pPIN]                    
 				}                   
            }
//>>> Inactive Devices >>>>                               
            if(fOperand == "inactive" || fOperand.contains("inactive") ||  fCommand == "inactive" || fCommand == "enacted" ) { //devices inactive
            	def cap = "act"
            	def devList = getCapabilities(cap)
                if(devList instanceof String){
                	outputTxt = devList
                	log.error " devList = ${devList}"
                    state.pTryAgain = true
					return ["outputTxt":outputTxt, "pContCmds":state.pContCmds, "pShort":state.pShort, "pContCmdsR":state.pContCmdsR, "pTryAgain":state.pTryAgain, "pPIN":pPIN]
                }
                else{
                if (fQuery == "how" || fQuery== "how many" || fQuery == "undefined" || fQuery == "are there" || fQuery == "give" || fQuery == "get") {
                        if (devList?.listSize > 0) {
                            if (devList?.listSize == 1) {
                                outputTxt = "There is one inactive device, would you like to know which one?"                           			
                            }
                            else {
                                outputTxt = "There are " + devList.listSize + " inactive devices, would you like to know which devices"
                            }
                        def sdevices = devList?.listDev
                        def devListString = sdevices.join(",")
                        data.list = devListString
                        state.lastAction = devListString
                        state.pContCmdsR = "act"
                        }
                        else {outputTxt = "There are no inactive devices"}
                    }
                    else if (fQuery.contains ("what") || fQuery.contains ("which")) {
                        	if (devList?.listSize > 0) {
                        		outputTxt = "The following devices have been inactive for more than " + cInactiveDev + " hours " + devList.listDev.sort()
                        	}
                        	else {outputTxt = "There are no inactive devices"
                        	}
                    }
					return ["outputTxt":outputTxt, "pContCmds":state.pContCmds, "pShort":state.pShort, "pContCmdsR":state.pContCmdsR, "pTryAgain":state.pTryAgain, "pPIN":pPIN]
                }
            }       
//>>> Settings >>>>                                    
            if(fOperand == "settings") {
                def pCmds = state.pContCmds == true ? "enabled" : "disabled"
                def pCmdsR = state.pContCmdsR //last continuation response
                def pMute = state.pMuteAlexa == true ? "Alexa voice is disabled" : "Alexa voice is active"
                //state.scheduledHandler
                def pin_D = state.usePIN_D 			== true ? "active" : "inactive"
                def pin_L = state.usePIN_L 			== true ? "active" : "inactive"
                def pin_T = state.usePIN_T 			== true ? "active" : "inactive"
                def pin_S = state.usePIN_S 			== true ? "active" : "inactive"
                def pin_SHM = state.usePIN_SHM 		== true ? "active" : "inactive"
                def pin_Mode = state.usePIN_Mode 	== true ? "active" : "inactive" 
                def activePin 	= pin_D 	== "active" ? "doors" : null
                    activePin  	= pin_L 	== "active" ? activePin + ", locks" : activePin
                    activePin  	= pin_S 	== "active" ? activePin + ", switches" : activePin
                    activePin  	= pin_T 	== "active" ? activePin + ", thermostats"  : activePin
                    activePin  	= pin_SHM 	== "active" ? activePin + ", smart security"  : activePin
                    activePin  	= pin_Mode 	== "active" ? activePin + ", modes"  : activePin
                if (activePin == null) { activePin = "no groups"}                
                def inactivePin = pin_D 	== "inactive" ? "doors" : null
                    inactivePin  = pin_L 	== "inactive" ? inactivePin + ", locks" : inactivePin
                    inactivePin  = pin_S 	== "inactive" ? inactivePin + ", switches" : inactivePin
                    inactivePin  = pin_T 	== "inactive" ? inactivePin + ", thermostats" : inactivePin
                    inactivePin  = pin_SHM	== "inactive" ? inactivePin + ", smart security" : inactivePin
                    inactivePin  = pin_Mode	== "inactive" ? inactivePin + ", location modes" : inactivePin
                if (inactivePin == null) {inactivePin = "no groups"}
  
                outputTxt = pMute + " and the conversational module is " + pCmds + ". The pin number is active for: " +  activePin + " and inactive for: " + inactivePin
                return ["outputTxt":outputTxt, "pContCmds":state.pContCmds, "pShort":state.pShort, "pContCmdsR":state.pContCmdsR, "pTryAgain":state.pTryAgain, "pPIN":pPIN]
            }
//>>> Presence >>>>                                    
            if (fQuery == "who" ) {
                if(cPresence){
                        def devListP = []
                        def devListNP = []
                        if (cPresence?.latestValue("presence").contains("present")) {
                            cPresence?.each { deviceName ->
                                        if (deviceName.latestValue("presence")=="present") {
                                            String device  = (String) deviceName
                                            devListP += device
                                        }
                            }
                        }
                        if (cPresence?.latestValue("presence").contains("not present")) {
                            cPresence?.each { deviceName ->
                                        if (deviceName.latestValue("presence")=="not present") {
                                            String device  = (String) deviceName
                                            devListNP += device
                                        }
                            }
                        }
                    if (fOperand == "here" || fOperand == "at home" || fOperand == "present" || fOperand == "home" ) {
                            if (devListP?.size() > 0) {
                                if (devListP?.size() == 1) {
                                    outputTxt = "Only" + devListP + "is at home"                         			
                                }
                                else {
                                    outputTxt = "The following " + devListP?.size() + " people are at home: " + devListP
                                }

                            }
                            else outputTxt = "No one is home"
                            return ["outputTxt":outputTxt, "pContCmds":state.pContCmds, "pShort":state.pShort, "pContCmdsR":state.pContCmdsR, "pTryAgain":state.pTryAgain, "pPIN":pPIN]
                    }
                    else if (fOperand.contains("not")) {
                        if (devListNP?.size() > 0) {
                            if (devListNP?.size() == 1) {
                                    outputTxt = "Only" + devListNP + "is not home"                         			
                            }
                            else {
                                    outputTxt = "The following " + devListNP?.size() + " people are not at home: " + devListNP
                            }
                        }
                        else outputTxt = "Everyone is at home"
                        return ["outputTxt":outputTxt, "pContCmds":state.pContCmds, "pShort":state.pShort, "pContCmdsR":state.pContCmdsR, "pTryAgain":state.pTryAgain, "pPIN":pPIN]
                    }
                }
            }
//>>> Events >>>>                                    
            if (fQuery.contains ("when")) {
            	fCommand = fCommand == "changed" ? "change" : fCommand
            	if (fCommand == "change" && state.filterNotif !=null ) {
                	outputTxt = state.filterNotif
                	//return ["outputTxt":outputTxt, "pContCmds":state.pContCmds, "pShort":state.pShort, "pContCmdsR":state.pContCmdsR, "pTryAgain":state.pTryAgain, "pPIN":pPIN]
                }
                else {
                	def deviceData = deviceMatchHandler(fDevice)
                	deviceM  = deviceData?.deviceMatch  
                	outputTxt = deviceM + " was last " + fOperand + " " + deviceData.tText
            	}
                return ["outputTxt":outputTxt, "pContCmds":state.pContCmds, "pShort":state.pShort, "pContCmdsR":state.pContCmdsR, "pTryAgain":state.pTryAgain, "pPIN":pPIN]
            }      
            
            def hText = fDevice != "undefined" ? " a device named " + fDevice : " something "           
                if (state.pShort != true){ 
					outputTxt = "Sorry, I heard that you were looking for feedback on " + hText + " but Echosistant wasn't able to help, "        
                }
                else {outputTxt = "I've heard " + hText +  " but I wasn't able to provide any feedback "} 
            	state.pTryAgain = true
            	return ["outputTxt":outputTxt, "pContCmds":state.pContCmds, "pShort":state.pShort, "pContCmdsR":state.pContCmdsR, "pTryAgain":state.pTryAgain, "pPIN":pPIN]
        }
    } 

}
/*catch (Throwable t) {
        log.error t
        outputTxt = "Oh no, something went wrong. If this happens again, please reach out for help!"
        state.pTryAgain = true
        return ["outputTxt":outputTxt, "pContCmds":state.pContCmds, "pShort":state.pShort, "pContCmdsR":state.pContCmdsR, "pTryAgain":state.pTryAgain, "pPIN":pPIN]
	}
}	*/
/************************************************************************************************************
   DEVICE CONTROL - from Lambda via page c
************************************************************************************************************/
def controlDevices() {
		//FROM LAMBDA
        def ctCommand = params.cCommand
        def ctNum = params.cNum
        def ctPIN = params.cPIN
        def ctDevice = params.cDevice
        def ctUnit = params.cUnit
        def ctGroup = params.cGroup       
		def ctIntentName = params.intentName
        //OTHER VARIABLES
		def String outputTxt = (String) null 
		def pPIN = false
        def String deviceType = (String) null
        def String command = (String) null
		def String numText = (String) null
        def String result = (String) null
        def String activityId = (String) "undefined"
        def delay = false
        def data
        def device
     
        ctCommand = ctCommand == "null" ? "undefined" : ctCommand
		ctNum = ctNum == "null" ? "undefined" : ctNum
        ctPIN = ctPIN == "null" ? "undefined" : ctPIN
        ctDevice = ctDevice == "null" ? "undefined" : ctDevice
        ctUnit = ctUnit == "null" ? "undefined" : ctUnit
		ctGroup = ctGroup == "null" ? "undefined" : ctGroup
        ctPIN = ctPIN == "null" ? "undefined" : ctPIN  
        
        ctDevice = ctDevice.replaceAll("[^a-zA-Z0-9 ]", "")

        if (debug) log.debug "Control Data: (ctCommand)= ${ctCommand}',(ctNum) = '${ctNum}', (ctPIN) = '${ctPIN}', "+
                             "(ctDevice) = '${ctDevice}', (ctUnit) = '${ctUnit}', (ctGroup) = '${ctGroup}', (ctIntentName) = '${ctIntentName}'"
	def ctProcess = true	
    state.pTryAgain = false 

//try {

    if (ctIntentName == "main") {
    	if (ctCommand == "this is a test"){
			outputTxt = "Congratulations! Your EchoSistant is now setup properly" 
			return ["outputTxt":outputTxt, "pContCmds":state.pContCmds, "pShort":state.pShort, "pContCmdsR":state.pContCmdsR, "pTryAgain":state.pTryAgain, "pPIN":pPIN]       
    	}
        ctPIN = ctPIN == "?" ? "undefined" : ctPIN
        if (ctNum == "undefined" || ctNum =="?"  || ctNum == "null") {ctNum = 0 } 
        if (ctCommand =="?") {ctCommand = "undefined"} 
        ctNum = ctNum as int
    	if (ctCommand == "undefined" || ctNum == "undefined" || ctPIN == "undefined" || ctDevice == "undefined" || ctUnit == "undefined" || ctGroup == "undefined") {        
            if (ctUnit =="?" || ctUnit == "undefined") {
                def String unit =  (String) "undefined"
            }    
            else {
                if (ctNum>0){
                    def getTxt = getUnitText(ctUnit, ctNum)     
                    numText = getTxt.text
                    ctUnit = getTxt.unit
                }
            }   
            if (ctNum > 0 && ctDevice != "undefined" && ctCommand == "undefined") {
                ctCommand = "set"
            }
            if (state.pinTry != null) {
                if (ctCommand == "undefined" && ctDevice == "undefined") {
                    outputTxt = pinHandler(ctPIN, ctCommand, ctNum, ctUnit)
                    return ["outputTxt":outputTxt, "pContCmds":state.pContCmds, "pShort":state.pShort, "pContCmdsR":state.pContCmdsR, "pTryAgain":state.pTryAgain, "pPIN":pPIN]
                }
                else {
                state.pinTry = null
                state.pTryAgain = false  
                }
            }           
            if (ctCommand != "undefined") {
                if (ctCommand.contains ("try again") && state.lastAction != null ) {
                        def savedData = state.lastAction
                        outputTxt = controlHandler(savedData)
                }       
                else {
                    outputTxt = getCustomCmd(ctCommand, ctUnit, ctGroup, ctNum) //added ctNum 1/27/2017
                    if (ctCommand.contains ("try again")) {
                        state.pContCmdsR = "clear"
                        state.savedPINdata = null
                        state.pinTry = null
                        outputTxt = " I am sorry for the trouble. I am getting my act together now, so you can continue enjoing your Echosistant app"
                        return ["outputTxt":outputTxt, "pContCmds":state.pContCmds, "pShort":state.pShort, "pContCmdsR":state.pContCmdsR, "pTryAgain":state.pTryAgain, "pPIN":pPIN]
                        }
                }
                if (outputTxt!= null ) {
                        if (ctUnit == "pin number" || ctUnit == "pin") {
                            if (ctGroup == "thermostats" || ctGroup == "locks" || ctGroup == "doors" || ctGroup == "security" || ctGroup == "switches") {
                                state.pTryAgain = false
                            }
                            else {
                                state.pTryAgain = true
                            }
                        }
                        if (outputTxt == "Pin number please") {pPIN = true}
                        return ["outputTxt":outputTxt, "pContCmds":state.pContCmds, "pShort":state.pShort, "pContCmdsR":state.pContCmdsR, "pTryAgain":state.pTryAgain, "pPIN":pPIN]
                }
                else {
                    def  getCMD = getCommand(ctCommand, ctUnit) 
                    deviceType = getCMD.deviceType
                    command = getCMD.command
                    if(debug) log.info "deviceType = ${deviceType} , command = ${command}"
                }
            }
            if (ctUnit != "undefined" &&  ctDevice != "undefined" && ctCommand == "undefined"){
				def  getCMD = getCommand(ctCommand, ctUnit) 
                deviceType = getCMD.deviceType
                command = getCMD.command
            }
    		//>>> MAIN PROCESS STARTS <<<<        
             if (ctCommand == "run" && ctDevice != "undefined" && ctGroup != "undefined"){
              	def String pintentName = (String) null
                def String pContCmdsR = (String) null
                def String ptts = (String) null
        		def pContCmds = false
        		def pTryAgain = false
        		def dataSet = [:]
           		childApps.each {child ->
                        def ch = child.label
                        	ch = ch.replaceAll("[^a-zA-Z0-9]", "")
                		if (ch.toLowerCase() == ctDevice?.toLowerCase()) { 
                    		if (debug) log.debug "Found a profile"
                            pintentName = child.label
                    		ptts = "Running Profile actions from the main home"
                            dataSet = [ptts:ptts, pintentName:pintentName] 
							def childRelease = child.checkRelease()
                            log.warn "childRelease = $childRelease"
                            def pResponse = child.profileEvaluate(dataSet)
                            	outputTxt = pResponse?.outputTxt
                                pContCmds = pResponse?.pContCmds
                                pContCmdsR = pResponse?.pContCmdsR
                                pTryAgain = pResponse?.pTryAgain
						}
            	}
            	if (outputTxt?.size()>0){
                	outputTxt = "Executed actions for " + pintentName + " profile"
                    
                	return ["outputTxt":outputTxt, "pContCmds":state.pContCmds, "pShort":state.pShort, "pContCmdsR":state.pContCmdsR, "pTryAgain":pTryAgain, "pPIN":pPIN]
            	}
            	else {
                	if (state.pShort != true){
                		outputTxt = "I wish I could help, but EchoSistant couldn't find a Profile named " + pintentName + " or the command may not be supported"
                	}
                	else {outputTxt = "I've heard " + pintentName + " , but I wasn't able to take any actions "} 
                		pTryAgain = true
                		return ["outputTxt":outputTxt, "pContCmds":pContCmds, "pShort":state.pShort, "pContCmdsR":pContCmdsR, "pTryAgain": pTryAgain, "pPIN":pPIN]
                }                
            }
            if (deviceType == "color"){
            def color = command == "read"  ? "Warm White" : command == "concentrate" ? "Daylight White" : command == "relax" ? "Very Warm White" : command
                if(ctDevice != "undefined" && ctGroup != "profile"){
                    def deviceMatch = cSwitch.find {s -> s.label?.toLowerCase() == ctDevice.toLowerCase()}
                    if(deviceMatch){
                    	def capMatch = deviceMatch.capabilities.name.contains("Color Control") ?: null
                        def availableCommands = deviceMatch.supportedCommands.contains("colorloop") ?: null
                        if(debug) log.info "availableCommands = ${availableCommands}"
                        if(capMatch){
                            if (color != "random" && color != "colorloopOn" && color != "colorloopOff" ){
                           		def hueSetVals = getColorName("${color}",level)
                                deviceMatch?.setColor(hueSetVals)
                                outputTxt =  "Ok, changing the color to " + color 
                                return ["outputTxt":outputTxt, "pContCmds":state.pContCmds, "pContCmdsR":pContCmdsR, "pTryAgain":pTryAgain, "pPIN":pPIN]
                            }
                            if (color == "random") {
								int hueLevel = !level ? 100 : level
								int hueHue = Math.random() *100 as Integer
								def randomColor = [hue: hueHue, saturation: 100, level: hueLevel]
        						deviceMatch.setColor(randomColor)
                                outputTxt =  "Ok, changing to a random color"
                                return ["outputTxt":outputTxt, "pContCmds":state.pContCmds, "pContCmdsR":pContCmdsR, "pTryAgain":pTryAgain, "pPIN":pPIN]
                            }
                            if (color == "colorloopOn" || color == "colorloopOff") {
								if(availableCommands){
                                	if (color == "colorloopOn") {deviceMatch.colorloopOn()}
                                    else deviceMatch.colorloopOff()
									outputTxt =  "Ok, turning the " + color
                                	return ["outputTxt":outputTxt, "pContCmds":state.pContCmds, "pContCmdsR":pContCmdsR, "pTryAgain":pTryAgain, "pPIN":pPIN]
                                }
                                else {
                                def loopOn = color == "colorloopOn" ? true : color == "colorloopOff" ? false : null
                                    if(loopOn == true){
                                        int hueLevel = !level ? 100 : level
                                        int hueHue = Math.random() *100 as Integer
                                        def randomColor = [hue: hueHue, saturation: 100, level: hueLevel]
                                        deviceMatch.setColor(randomColor)
                                        state.lastDevice = deviceMatch.label
                                        runIn(60, "startLoop")
                                        outputTxt =  "Ok, turning the " + color
                                        return ["outputTxt":outputTxt, "pContCmds":state.pContCmds, "pContCmdsR":pContCmdsR, "pTryAgain":pTryAgain, "pPIN":pPIN]
                                  	}
                                    else { 
                                    	unschedule("startLoop")
                                        unschedule("continueLoop")
                                        state.lastDevice = null
										outputTxt =  "Ok, turning the " + color
                                        return ["outputTxt":outputTxt, "pContCmds":state.pContCmds, "pContCmdsR":pContCmdsR, "pTryAgain":pTryAgain, "pPIN":pPIN]
                                    }
                                }
                            }                            
                    	}
                    }
                }
                if(ctDevice != "undefined" && ctGroup == "profile"){
            		childApps.each {child ->
                    	def profile
                        def ch = child.label
                        	ch = ch.replaceAll("[^a-zA-Z0-9]", "")
                            profile  = ctDevice.replaceAll("[^a-zA-Z0-9]", "")
                		if (ch.toLowerCase() == profile.toLowerCase()) { 
                    		if (debug) log.debug "Found a profile"
                    		//def capMatch = deviceMatch.capabilities.name.contains("Color Control") ?: null
                        	//def availableCommands = deviceMatch.supportedCommands
                        	//log.warn "availableCommands = ${availableCommands}"
                        	if(child.gHues){
                            	if (color != "random" && color != "colorloopOn" && color != "colorloopOff" ){
                           			def hueSetVals = getColorName("${color}",level)
                                	child.gHues.setColor(hueSetVals)
                                	outputTxt =  "Ok, changing the color to " + color + " in the " + ctDevice
                            	}
                            	if (color == "random") {
									int hueLevel = !level ? 100 : level
									int hueHue = Math.random() *100 as Integer
									def randomColor = [hue: hueHue, saturation: 100, level: hueLevel]
                                	child.gHues.setColor(randomColor)
                                	outputTxt =  "Ok, changing the " + ctDevice +  " to random colors"
                                }
                            	 if (color == "colorloopOn" || color == "colorloopOff") {
                                 	def loopOn = color == "colorloopOn" ? true : color == "colorloopOff" ? false : null
                                 	if(loopOn == true){
                                        outputTxt = child.profileLoop(child.label)
                                    }
									else { 
                                    	outputTxt = child.profileLoopCancel(child.label)
                                    }
                                }                                
                            }
                    	}
                    }
                    return ["outputTxt":outputTxt, "pContCmds":state.pContCmds, "pContCmdsR":pContCmdsR, "pTryAgain":pTryAgain, "pPIN":pPIN]
                }
            }
            if (deviceType == "volume" || deviceType == "general" || deviceType == "light") {      
                        def deviceMatch = null
                        //def activityId = null 2/11/2017 moved as global variable
                        def dType = null
                            if (settings.cSpeaker?.size()>0) {
                                deviceMatch = cSpeaker?.find {s -> s.label?.toLowerCase() == ctDevice.toLowerCase()}
                                if(deviceMatch) {
                                if (debug) log.debug "found a speaker "
                                dType = "v"}
                            }
                            if (deviceMatch == null && settings.cSynth?.size()>0) {
                                deviceMatch = cSynth.find {s -> s.label?.toLowerCase() == ctDevice.toLowerCase()}                 
                                if(deviceMatch) {dType = "v"}
                            }
							//HARMONY PROCESS//
							if (deviceMatch == null && settings.cMedia?.size()>0) {
                                //deviceMatch = cMedia.first()
                                deviceMatch = cMedia.find {s -> s.label?.toLowerCase() == ctDevice.toLowerCase()}                 
                                if(deviceMatch) {
                                    dType = "m"
                                }
                                else {
                                    //cMedia.each {a -> //disabled 2/13/2017 to ONLY use Main Hub        
                                        //def activities = a.currentState("activities").value //disabled 2/13/2017 to ONLY use Main Hub
                                        def harmonyMain = cMedia.first()
                                        def activities = harmonyMain.currentState("activities").value
                                        def activityList = new groovy.json.JsonSlurper().parseText(activities)
                                            activityList.each { it ->  
                                                def activity = it
                                                    if(activity.name.toLowerCase() == ctDevice.toLowerCase()) {
                                                    dType = "m"
                                                    deviceMatch = harmonyMain //a //disabled 2/13/2017 to ONLY use Main Hub
                                                    activityId = activity.id
                                                    }    	
                                            }
                                  	//}   //disabled 2/13/2017 to ONLY use Main Hub
                                }
                            }
                            //Personal Preference to use the Harmony Hub for TV off (works only with first Hub selected 2/10/17 Bobby
                            if (ctDevice == "TV" && command != "mute" && command != "unmute" && command != "setLevel" && command != "decrease" && command != "increase" && settings.cMedia?.size()>0) {
                            	dType = "m"
                                deviceMatch = cMedia?.first()                   
                            }    
                            if (deviceMatch == null && settings.cSwitch?.size()>0 && state.pinTry == null) {
                                def switchProblem
                                deviceMatch = cSwitch.find {s -> s.label?.toLowerCase() == ctDevice?.toLowerCase()}     
                                if(deviceMatch) {dType = "s"}
                            }
                            if (deviceMatch == null && settings.cMiscDev?.size()>0 && state.pinTry == null) {
                                deviceMatch = cMiscDev.find {s -> s.label?.toLowerCase() == ctDevice.toLowerCase()}                 
                                if(deviceMatch) { 
                            //>>>>>>>  CHECK FOR ENABLED PIN <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
                                if(cPIN && state.usePIN_S == true && deviceMatch) {
                                    if (debug) log.warn "PIN enabled for Switch '${deviceMatch}'"
                                    device = deviceMatch.label
                                    if (command == "disable" || command == "deactivate"|| command == "stop") {command = "off"}
                                    if (command == "enable" || command == "activate"|| command == "start") {command = "on"}	                        
                                    ctUnit = ctUnit == "minute" ? "minutes" : ctUnit
                                    delay = true
                                    data = [type: "cMiscDev", command: command, device: device, unit: ctUnit, num: ctNum, delay: delay]
                                    state.lastAction = data
                                    state.pContCmdsR = "cMiscDev"
                            //>>>>>>>  RUN PIN VALIDATION PROCESS <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
                                    def pin = "undefined"
                                    command = "validation"
                                    def unit = "cMiscDev"
                                    outputTxt = pinHandler(pin, command, ctNum, unit)
                                    pPIN = true
                                    if (state.pinTry == 3) {pPIN = false}
                                    log.warn "try# ='${state.pinTry}'"
                                    return ["outputTxt":outputTxt, "pContCmds":state.pContCmds, "pShort":state.pShort, "pContCmdsR":state.pContCmdsR, "pTryAgain":state.pTryAgain, "pPIN":pPIN]
                                }
                                else {               
                                    if (ctNum > 0 && ctUnit == "minutes") {
                                        runIn(ctNum*60, controlHandler, [data: data])
                                        if (command == "on" || command == "off" ) {outputTxt = "Ok, turning " + ctDevice + " " + command + ", in " + numText}
                                        return ["outputTxt":outputTxt, "pContCmds":state.pContCmds, "pShort":state.pShort, "pContCmdsR":state.pContCmdsR, "pTryAgain":state.pTryAgain, "pPIN":pPIN]
                                    }
                                    else {
                                        delay = true
                                        data = [type: "cSwitch", command: command, device: device, unit: ctUnit, num: ctNum, delay: delay]
                                        outputTxt = controlHandler(data)
                                        return ["outputTxt":outputTxt, "pContCmds":state.pContCmds, "pShort":state.pShort, "pContCmdsR":state.pContCmdsR, "pTryAgain":state.pTryAgain, "pPIN":pPIN]
                                    }
                                } 
                            }
                        }
                        if (deviceMatch && dType == "v") {
                            device = deviceMatch
                            delay = false
                            data = [type: "cVolume", command: command, device: device, unit: ctUnit, num: ctNum, delay: delay]
                            outputTxt = controlHandler(data)
                            return ["outputTxt":outputTxt, "pContCmds":state.pContCmds, "pShort":state.pShort, "pContCmdsR":state.pContCmdsR, "pTryAgain":state.pTryAgain, "pPIN":pPIN]
                        }
    					//HARMONY CONTROL//                  
                        if (deviceMatch && dType == "m") {
                            device = deviceMatch
                            if (ctNum > 0 && ctUnit == "minutes") {
                                device = device.label
                                delay = true
                                data = [type: "cHarmony", command: command, device: device, unit: activityId, num: ctNum, delay: delay]
                                runIn(ctNum*60, controlHandler, [data: data])
                                outputTxt = "Ok, turning " +  deviceMatch + " activity " + command + ", in " + numText
                                return ["outputTxt":outputTxt, "pContCmds":state.pContCmds, "pShort":state.pShort, "pContCmdsR":state.pContCmdsR, "pTryAgain":state.pTryAgain, "pPIN":pPIN]
                            }
                            else{                        
                            delay = false
                            data = [type: "cHarmony", command: command, device: device, unit: activityId, num: ctNum, delay: delay]
                            outputTxt = controlHandler(data)
                            return ["outputTxt":outputTxt, "pContCmds":state.pContCmds, "pShort":state.pShort, "pContCmdsR":state.pContCmdsR, "pTryAgain":state.pTryAgain, "pPIN":pPIN]
                            }
                        }
                        //Switch Control
                        if (deviceMatch && dType == "s") {
                            device = deviceMatch
                            if (command == "cut off" || command == "disable" || command == "deactivate"|| command == "stop") {command = "off"}
                            if (command == "cut on" || command == "enable" || command == "activate"|| command == "start") {command = "on"}    
                            if (ctNum > 0 && ctUnit == "minutes") {
                                device = device.label
                                delay = true
                                data = [type: "cSwitch", command: command, device: device, unit: ctUnit, num: ctNum, delay: delay]
                                runIn(ctNum*60, controlHandler, [data: data])
                                if (command == "on" && ctCommand != "cut on") {outputTxt = "Ok, turning " + ctDevice + " " + command + ", in " + numText}
                                else if (command == "off" && ctCommand != "cut off") {outputTxt = "Ok, turning " + ctDevice + " " + command + ", in " + numText}
                                else if (command == "decrease") {outputTxt = "Ok, decreasing the " + ctDevice + " level in " + numText}
                                else if (command == "increase") {outputTxt = "Ok, increasing the " + ctDevice + " level in " + numText}
                                else if (ctCommand == "cut on") {outputTxt = "Ok, cutting on the " + ctDevice + ", in " + numText}
                                else if (ctCommand == "cut off") {outputTxt = "Ok, cutting off the " + ctDevice + ", in " + numText}
                                return ["outputTxt":outputTxt, "pContCmds":state.pContCmds, "pShort":state.pShort, "pContCmdsR":state.pContCmdsR, "pTryAgain":state.pTryAgain, "pPIN":pPIN]
                            }
                            else {
                                delay = false
                                data = [type: "cSwitch", command: command, device: device, unit: ctUnit, num: ctNum, delay: delay]
                                outputTxt = controlHandler(data)
                                if (command == "decrease" || command == "increase") {state.pContCmdsR = "level"}
                                return ["outputTxt":outputTxt, "pContCmds":state.pContCmds, "pShort":state.pShort, "pContCmdsR":state.pContCmdsR, "pTryAgain":state.pTryAgain, "pPIN":pPIN]
                            }
                        }         
            }
    // >>>> THERMOSTAT CONTROL <<<<
            else if (deviceType == "temp") {
                    if (settings.cTstat?.size() > 0) {           
                        def deviceMatch = cTstat.find {t -> t.label?.toLowerCase() == ctDevice.toLowerCase()}
                        if (deviceMatch) {
                            device = deviceMatch 
                            if(state.usePIN_T == true) { // (THIS PIN VALIDATION HAS BEEN Deprecated as of 1/23/2017)
                                if (debug) log.warn "PIN protected device type - '${deviceType}'"
                                delay = false
                                data = ["type": "cTstat", "command": command , "device": ctDevice, "unit": ctUnit, "num": ctNum, delay: delay]
                                state.savedPINdata = data
                                outputTxt = "Pin number please"
                                pPIN = true
                                state.pinTry = 0
                                return ["outputTxt":outputTxt, "pContCmds":state.pContCmds, "pShort":state.pShort, "pContCmdsR":state.pContCmdsR, "pTryAgain":state.pTryAgain, "pPIN":pPIN]
                            }
                            else {                       
                                if (ctNum && ctUnit == "minutes") {
                                    delay = true
                                    data = [type: "cTstat", command: command, device: device, unit: ctUnit, num: ctNum, delay: delay]
                                    runIn(ctNum*60, delayHandler, [data: data])
                                    if (command == "decrease") {outputTxt = "Ok, decreasing the " + ctDevice + " temperature in " + numText}
                                    else if (command == "increase") {outputTxt = "Ok, increasing the " + ctDevice + " temperature in " + numText}
                                    return ["outputTxt":outputTxt, "pContCmds":state.pContCmds, "pShort":state.pShort, "pContCmdsR":state.pContCmdsR, "pTryAgain":state.pTryAgain, "pPIN":pPIN]
                                }
                                else {
                                    delay = false
                                    data = [type: "cTstat", command: command, device: device, unit: ctUnit, num: ctNum, delay: delay]
                                    outputTxt = controlHandler(data)
                                    return ["outputTxt":outputTxt, "pContCmds":state.pContCmds, "pShort":state.pShort, "pContCmdsR":state.pContCmdsR, "pTryAgain":state.pTryAgain, "pPIN":pPIN]
                                }
                            }
                       }
                    }
             }
    // >>>> LOCKS CONTROL <<<<
            else if (deviceType == "lock") {
                if (settings.cLock?.size()>0) {   
                    def deviceMatch = cLock.find {l -> l.label?.toLowerCase() == ctDevice.toLowerCase()}             
                    if (deviceMatch) {
                        device = deviceMatch
                    //Check Status
                        def deviceR = device.label
                        def cLockStatus = device.lockState.value
                        def pinCheck = (pinOnOpen == true && cLockStatus == "locked") ? true : (pinOnOpen == false || pinOnOpen == null) ? true : false 
                            if ((command == "lock" && cLockStatus == "locked") || (command == "unlock" && cLockStatus == "unlocked")) {
                            outputTxt = "The " + device + " is already ${cLockStatus}"
                            return ["outputTxt":outputTxt, "pContCmds":state.pContCmds, "pShort":state.pShort, "pContCmdsR":state.pContCmdsR, "pTryAgain":state.pTryAgain, "pPIN":pPIN]
                        }
                        if(state.usePIN_L == true && pinCheck == true) { // (THIS PIN VALIDATION HAS BEEN Deprecated as of 1/23/2017)
                            if (debug) log.warn "PIN protected device type - '${deviceType}'"               		
                            delay = false
                            data = [type: "cLock", "command": command , "device": ctDevice, "unit": ctUnit, "num": ctNum, delay: delay]
                            state.savedPINdata = data
                            outputTxt = "Pin number please"
                            pPIN = true
                            state.pinTry = 0
                            return ["outputTxt":outputTxt, "pContCmds":state.pContCmds, "pShort":state.pShort, "pContCmdsR":state.pContCmdsR, "pTryAgain":state.pTryAgain, "pPIN":pPIN]
                        }
                        else {
                            if (ctNum > 0 && ctUnit == "minutes") {
                                device = device.label
                                delay = true
                                data = [type: "cLock", command: command, device: device, unit: ctUnit, num: ctNum, delay: delay]
                                runIn(ctNum*60, controlHandler, [data: data])
                                if (command == "lock") {outputTxt = "Ok, locking the " + ctDevice + " in " + numText}
                                else if (command == "unlock") {outputTxt = "Ok, unlocking the " + ctDevice + " in " + numText}
                                return ["outputTxt":outputTxt, "pContCmds":state.pContCmds, "pShort":state.pShort, "pContCmdsR":state.pContCmdsR, "pTryAgain":state.pTryAgain, "pPIN":pPIN]
                            }
                            else {
                                delay = false
                                data = [type: "cLock", command: command, device: device, unit: ctUnit, num: ctNum, delay: delay]
                                outputTxt = controlHandler(data)
                                return ["outputTxt":outputTxt, "pContCmds":state.pContCmds, "pShort":state.pShort, "pContCmdsR":state.pContCmdsR, "pTryAgain":state.pTryAgain, "pPIN":pPIN]
            				}
                        }
                    }
                }
            }
    // >>>> FANS CONTROL <<<<        
            else if (deviceType == "fan") {
                if (settings.cFan?.size()>0) {     
                    def deviceMatch = cFan.find {f -> f.label?.toLowerCase() == ctDevice.toLowerCase()}
                    if (deviceMatch) {
                            device = deviceMatch
                            if (ctNum && ctUnit == "minutes") {
                                delay = true
                                data = [type: "cFan", command: command, device: device, unit: ctUnit, num: ctNum, delay: delay]
                                runIn(ctNum*60, delayHandler, [data: data])
                                if (command == "decrease") {outputTxt = "Ok, decreasing the " + ctDevice + " temperature in " + numText}
                                else if (command == "increase") {outputTxt = "Ok, increasing the " + ctDevice + " temperature in " + numText}
                                return ["outputTxt":outputTxt, "pContCmds":state.pContCmds, "pShort":state.pShort, "pContCmdsR":state.pContCmdsR, "pTryAgain":state.pTryAgain, "pPIN":pPIN]
                            }
                            else {
                                delay = false
                                data = [type: "cFan", command: command, device: device, unit: ctUnit, num: ctNum, delay: delay]
                                outputTxt = controlHandler(data)
                                return ["outputTxt":outputTxt, "pContCmds":state.pContCmds, "pShort":state.pShort, "pContCmdsR":state.pContCmdsR, "pTryAgain":state.pTryAgain, "pPIN":pPIN]
                            }
                    }
                }
            }
    // >>>> PRESENCE CHECKIN/CHECKOUT CONTROL <<<<        
            else if (deviceType == "cPresence") {
                if (settings.cPresence?.size()>0) {     
                    def deviceMatch = cPresence.find {f -> f.label?.toLowerCase() == ctDevice.toLowerCase()}
                    if (deviceMatch) {
                            device = deviceMatch
                                delay = false
                                data = [type: "cPresence", command: command, device: device, unit: ctUnit, num: ctNum, delay: delay]
                                outputTxt = controlHandler(data)
                                return ["outputTxt":outputTxt, "pContCmds":state.pContCmds, "pShort":state.pShort, "pContCmdsR":state.pContCmdsR, "pTryAgain":state.pTryAgain, "pPIN":pPIN]
                            }
                       }
                  }     
                            
// >>>> DOORS CONTROL <<<<        
            else if (deviceType == "door") {
                if (settings.cDoor?.size()>0 || cWindowCover) {
                	def devMatchWin = null
                    def deviceMatch = cDoor.find {d -> d.label?.toLowerCase() == ctDevice.toLowerCase()}
                    	if (!deviceMatch) devMatchWin = cWindowCover.find {d -> d.label?.toLowerCase() == ctDevice.toLowerCase()}
                    //if (deviceMatch || devMatchWin) {
                        if (deviceMatch){
                            device = deviceMatch
                        //Check Status
                            def deviceR = device?.label
                            def cDoorStatus = device.contactState.value
                            def pinCheck = (pinOnOpen == true && cDoorStatus == "closed") ? true : (pinOnOpen == false || pinOnOpen == null) ? true : false 
                            log.warn " pinCheck = ${pinCheck}"
                                if (command == "open" && cDoorStatus == "open") {
                                outputTxt = "The " + device + " is already open, would you like to close it instead?"
                                state.pContCmdsR = "door"
                                def actionData = ["type": "cDoor", "command": "close" , "device": deviceR, "unit": ctUnit, "num": ctNum, delay: delayD]
                                state.lastAction = actionData
                                return ["outputTxt":outputTxt, "pContCmds":state.pContCmds, "pShort":state.pShort, "pContCmdsR":state.pContCmdsR, "pTryAgain":state.pTryAgain, "pPIN":pPIN]
                            }
                            if (command == "close" && cDoorStatus =="closed") {
                                outputTxt = "The " + device + " is already closed, would you like to open it instead? "
                                state.pContCmdsR = "door"
                                def actionData = ["type": "cDoor", "command": "open" , "device": deviceR, "unit": ctUnit, "num": ctNum, delay: delayD]
                                state.lastAction = actionData
                                return ["outputTxt":outputTxt, "pContCmds":state.pContCmds, "pShort":state.pShort, "pContCmdsR":state.pContCmdsR, "pTryAgain":state.pTryAgain, "pPIN":pPIN]
                            }
                            if(state.usePIN_D == true && pinCheck == true) {
                                //PIN VALIDATION PROCESS (Deprecated code as of 1/23/2017)
                                if (debug) log.warn "PIN protected device type - '${deviceType}'"
                                delay = false
                                data = [type: "cDoor", "command": command , "device": ctDevice, "unit": ctUnit, "num": ctNum, delay: delay]
                                state.savedPINdata = data
                                outputTxt = "Pin number please"
                                pPIN = true
                                state.pinTry = 0
                                return ["outputTxt":outputTxt, "pContCmds":state.pContCmds, "pShort":state.pShort, "pContCmdsR":state.pContCmdsR, "pTryAgain":state.pTryAgain, "pPIN":pPIN]
                            }
                        }
                        if (deviceMatch || devMatchWin) {
                        //else { 
                        	device = devMatchWin ? devMatchWin : deviceMatch
                            log.warn "converted device = ${device}"
                            if (ctNum && ctUnit == "minutes") {
                                delay = true
                                data = [type: "cDoor", command: command, device: device, unit: ctUnit, num: ctNum, delay: delay]
                                runIn(ctNum*60, delayHandler, [data: data])
                                if (command == "open") {outputTxt = "Ok, opening " + ctDevice + " in " + numText}
                                else if (command == "close") {outputTxt = "Ok, closing " + ctDevice + " in " + numText}
                                return ["outputTxt":outputTxt, "pContCmds":state.pContCmds, "pShort":state.pShort, "pContCmdsR":state.pContCmdsR, "pTryAgain":state.pTryAgain, "pPIN":pPIN]
                            }
                            else {
                                delay = false
                                data = [type: "cDoor", command: command, device: device, unit: ctUnit, num: ctNum, delay: delay]
                                outputTxt = controlHandler(data)
                                return ["outputTxt":outputTxt, "pContCmds":state.pContCmds, "pShort":state.pShort, "pContCmdsR":state.pContCmdsR, "pTryAgain":state.pTryAgain, "pPIN":pPIN]
                            }
                        }
                    }
                //}
    	// >>>> RELAYS CONTROL <<<<            
                if (cRelay !=null) {
                //this is needed for Garage Doors that are set up as relays
                    def deviceMatch = cRelay.find {s -> s.label?.toLowerCase() == ctDevice.toLowerCase()}             
                    if (deviceMatch) {
                        device = deviceMatch
                        def pinCheck
                        def cCRelayValue
                        if (cContactRelay) {
                            if (debug) log.debug "Garage Door has a contact sensor"
                            def deviceR = device.label
                            	cCRelayValue = cContactRelay.contactState.value
                            	pinCheck = (pinOnOpen == true && cCRelayValue == "closed") ? true : (pinOnOpen == false || pinOnOpen == null) ? true : false 
                                if (command == "open" && cCRelayValue == "open") {
                                    outputTxt = "The " + device + " is already open, would you like to close it instead?"
                                    state.pContCmdsR = "door"
                                    def actionData = ["type": "cRelay", "command": "close" , "device": deviceR, "unit": unitU, "num": newLevel, delay: delayD]
                                    state.lastAction = actionData
                                    return ["outputTxt":outputTxt, "pContCmds":state.pContCmds, "pShort":state.pShort, "pContCmdsR":state.pContCmdsR, "pTryAgain":state.pTryAgain, "pPIN":pPIN]
                                }
                                if (command == "close" && cCRelayValue =="closed") {
                                    outputTxt = "The " + device + " is already closed, would you like to open it instead? "
                                    state.pContCmdsR = "door"
                                    def actionData = ["type": "cRelay", "command": "open" , "device": deviceR, "unit": ctUnit, "num": ctNum, delay: delay]
                                    state.lastAction = actionData
                                    return ["outputTxt":outputTxt, "pContCmds":state.pContCmds, "pShort":state.pShort, "pContCmdsR":state.pContCmdsR, "pTryAgain":state.pTryAgain, "pPIN":pPIN]
                                 }
                             }
                            //PIN VALIDATION PROCESS (Deprecated code as of 1/23/2017)
                            log.warn " pinCheck = ${pinCheck}, cCRelayValue = ${cCRelayValue} "
                            if(state.usePIN_D == true && pinCheck == true) {
                                if (debug) log.warn "PIN protected device type - '${deviceType}'"
                                delay = false
                                data = [type: "cRelay", "command": command , "device": ctDevice, "unit": ctUnit, "num": ctNum, delay: delay]
                                state.savedPINdata = data
                                outputTxt = "Pin number please"
                                pPIN = true
                                state.pinTry = 0
                                return ["outputTxt":outputTxt, "pContCmds":state.pContCmds, "pShort":state.pShort, "pContCmdsR":state.pContCmdsR, "pTryAgain":state.pTryAgain, "pPIN":pPIN]
                            }
                            else {                 
                            //END PIN VALIDATION                                       
                                if (ctNum > 0 && ctUnit == "minutes") {
                                    device = device.label
                                    delay = true
                                    data = [type: "cRelay", command: command, device: device, unit: ctUnit, num: ctNum, delay: delay]
                                    runIn(ctNum*60, controlHandler, [data: data])
                                    if (ctCommand == "open") {outputTxt = "Ok, opening the " + ctDevice + " in " + numText}
                                    else if (command == "close") {outputTxt = "Ok, closing the " + ctDevice + " in " + numText}
                                    return ["outputTxt":outputTxt, "pContCmds":state.pContCmds, "pShort":state.pShort, "pContCmdsR":state.pContCmdsR, "pTryAgain":state.pTryAgain, "pPIN":pPIN]
                                }
                                else {
                                    delay = false
                                    data = [type: "cRelay", "command": command, "device": device, unit: ctUnit, num: ctNum, delay: delay]
                                    outputTxt = controlHandler(data)
                                    return ["outputTxt":outputTxt, "pContCmds":state.pContCmds, "pShort":state.pShort, "pContCmdsR":state.pContCmdsR, "pTryAgain":state.pTryAgain, "pPIN":pPIN]
                               }
                         }
                    }
                }
    	// >>>> VENTS CONTROL <<<<            
                if (settings.cVent?.size()>0) {
                //this is needed to enable open/close command for Vents group
                    def deviceMatch = cVent.find {s -> s.label?.toLowerCase() == ctDevice.toLowerCase()}             
                    if (deviceMatch) {
                        if (command == "open") {command = "onD"}
                        if (command == "close") {command = "offD"}
                        device = deviceMatch
                            if (ctNum > 0 && ctUnit == "minutes") {
                                device = device.label
                                delay = true
                                data = [type: "cSwitch", command: command, device: device, unit: ctUnit, num: ctNum, delay: delay]
                                runIn(ctNum*60, controlHandler, [data: data])
                                if (ctCommand == "open") {outputTxt = "Ok, opening the " + ctDevice + " in " + numText}
                                else if (command == "close") {outputTxt = "Ok, closing the " + ctDevice + " in " + numText}
                                return ["outputTxt":outputTxt, "pContCmds":state.pContCmds, "pShort":state.pShort, "pContCmdsR":state.pContCmdsR, "pTryAgain":state.pTryAgain, "pPIN":pPIN]
                            }
                            else {
                                delay = false
                                data = [type: "cSwitch", command: command, device: device, unit: ctUnit, num: ctNum, delay: delay]
                                controlHandler(data)
                                if (ctCommand == "open") {outputTxt = "Ok, opening the " + ctDevice}
                                else if (ctCommand == "close") {outputTxt = "Ok, closing the " + ctDevice}
                                return ["outputTxt":outputTxt, "pContCmds":state.pContCmds, "pShort":state.pShort, "pContCmdsR":state.pContCmdsR, "pTryAgain":state.pTryAgain, "pPIN":pPIN]
                            }
                    }
                }
            }
            ctCommand = ctCommand == "on" ? "turn on" : ctCommand == "off" ? "turn off" : ctCommand
            def hText = ctDevice != "undefined" && ctCommand != "undefined" ? ctCommand + " the " + ctDevice :  ctDevice != "undefined" && ctCommand == "undefined" ? " control " + ctDevice : ctDevice == "undefined"  && ctCommand != "undefined" ? ctCommand + " something" : "control something" 
			def sText = ctDevice != "undefined" && ctCommand != "undefined" ? "the command " + ctCommand + " and device " + ctDevice : ctDevice != "undefined" && ctCommand == "undefined" ? " device named " + ctDevice : ctDevice == "undefined" && ctCommand != "undefined" ? " command named " + ctCommand : " something " 
            if (state.pShort != true){ 
            		outputTxt = "Sorry, I heard that you were looking to " + hText + " but Echosistant wasn't able to take any actions "
                }
                else {outputTxt = "I've heard " + sText +  " but I wasn't able to take any actions "} 
            state.pTryAgain = true
            return ["outputTxt":outputTxt, "pContCmds":state.pContCmds, "pShort":state.pShort, "pContCmdsR":state.pContCmdsR, "pTryAgain":state.pTryAgain, "pPIN":pPIN]
        }
    	outputTxt = "Sorry, I didn't get that, "
		state.pTryAgain = true
		return ["outputTxt":outputTxt, "pContCmds":state.pContCmds, "pShort":state.pShort, "pContCmdsR":state.pContCmdsR, "pTryAgain":state.pTryAgain, "pPIN":pPIN]
    }
} 
       /*catch (Throwable t) {
        log.error t
        outputTxt = "Oh no, something went wrong. If this happens again, please reach out for help!"
        state.pTryAgain = true
        return ["outputTxt":outputTxt, "pContCmds":state.pContCmds, "pShort":state.pShort, "pContCmdsR":state.pContCmdsR, "pTryAgain":state.pTryAgain, "pPIN":pPIN]
	}    
}	*/
/************************************************************************************************************
   DEVICE CONTROL HANDLER
************************************************************************************************************/      
def controlHandler(data) {   
    def deviceType = data.type
    def deviceCommand = data.command
   	def deviceD = data.device
    def unitU = data.unit
    def numN = data.num
    def delayD = data.delay
	def result = " "
    def actionData
    
        deviceType = deviceType == "null" ? "undefined" : deviceType
        deviceCommand = deviceCommand == "null" ? "undefined" : deviceCommand
        deviceD = deviceD == "null" ? "undefined" : deviceD
		unitU = unitU == "null" ? "undefined" : unitU
        numN = numN == "null" ? "undefined" : numN
        delayD = delayD == "null" ? "undefined" : delayD      

    if (debug) log.debug 	"Received device control handler data: " +
        					" (deviceType)= ${deviceType}',(deviceCommand) = '${deviceCommand}', (deviceD) = '${deviceD}', " +
                            "(unitU) = '${unitU}', (numN) = '${numN}', (delayD) = '${delayD}'"  
	state.pTryAgain = false
	if (deviceType == "cSwitch" || deviceType == "cMiscDev") {
    	if (deviceCommand == "on" || deviceCommand == "off") {
            if (delayD == true ) {
                if(deviceType == "cSwitch") {deviceD = cSwitch?.find {s -> s.label?.toLowerCase() == deviceD.toLowerCase()}}
                if (deviceType == "cMiscDev") {
                	deviceD = cMiscDev?.find {s -> s.label?.toLowerCase() == deviceD.toLowerCase()}            
                	deviceD."${deviceCommand}"()
                    result = "Ok, turning " + deviceD + " " + deviceCommand 
                	return result          
				}
                else {
                	deviceD."${deviceCommand}"()
                }
			}
            else {
            	deviceD."${deviceCommand}"()            	
                result = "Ok, turning " + deviceD + " " + deviceCommand 
                return result
            }
        }
        else if (deviceCommand == "onD") {
        		deviceD.on()
                deviceD.setLevel(100)
                
        }
        else if (deviceCommand == "offD") {
        		deviceD.off()
        }        
        else if (deviceCommand == "increase" || deviceCommand == "decrease" || deviceCommand == "setLevel" || deviceCommand == "set") {
 			if (delayD == true) {
            	if(deviceType == "cSwitch") {deviceD = cSwitch?.find {s -> s.label?.toLowerCase() == deviceD.toLowerCase()}}
                else if (deviceType == "cMiscDev") {deviceD = cMiscDev?.find {s -> s.label?.toLowerCase() == deviceD.toLowerCase()}}            
            }            
            if(state.pContCmdsR == "repeat") {state.pContCmdsR == "level"}
            def currLevel = deviceD.latestValue("level")
            def currState = deviceD.latestValue("switch")
            def newLevel = cLevel*10
            if (unitU == "percent") newLevel = numN      
            if (deviceCommand == "increase") {
            	if (unitU == "percent") {
                	newLevel = numN
                }   
                else {
                	if (currLevel == null){
                    deviceD.on()
                    result = "Ok, turning " + deviceD + " on"
            		return result    
                    }
                    else {
                	newLevel =  currLevel + newLevel
            		newLevel = newLevel < 0 ? 0 : newLevel >100 ? 100 : newLevel
            		}
                }
            }
            if (deviceCommand == "decrease") {
            	if (unitU == "percent") {
                	newLevel = numN
                }   
                else {
                	if (currLevel == null) {
                    deviceD.off()
                    result = "Ok, turning " + deviceD + " off"
            		return result                    
                    }
                    else {
                	newLevel =  currLevel - newLevel
            		newLevel = newLevel < 0 ? 0 : newLevel >100 ? 100 : newLevel
                    }
                }            
            }
            if (deviceCommand == "setLevel") {
            	if (unitU == "percent") {
                	newLevel = numN
                }   
                else {
                	newLevel =  numN*10
            		newLevel = newLevel < 0 ? 0 : newLevel >100 ? 100 : newLevel
                }            
            }
            if (newLevel > 0 && currState == "off") {
            	deviceD.on()
            	deviceD.setLevel(newLevel)
            }
            else {                                    
            	if (newLevel == 0 && currState == "on") {deviceD.off()}
                else {deviceD.setLevel(newLevel)}
            } 
            def device = deviceD.label
            def delayL = true
            actionData = ["type": deviceType, "command": deviceCommand , "device": device, "unit": unitU, "num": newLevel, delay: delayL]
            state.lastAction = actionData
            result = "Ok, setting  " + deviceD + " to " + newLevel + " percent"            
            if (delayD == false || deviceType == "cMiscDev"  || state.pContCmdsR == "repeat") { return result } 
    	}
		else { 
			if(debug) log.error "deviceD: ${deviceD.label}, deviceCommand:${deviceCommand}"
            def cmd = deviceCommand != "undefined" ? deviceCommand : unitU != "undefined" ? unitU : "undefined"
            def devTrue = deviceD != "undefined" ? " device name " + deviceD : "undefined" 
            def devFalse = deviceD == "undefined" ? " didn't catch the device name " : "undefined"
			if (devTrue != "undefined") result = "I heard the command " + cmd + " and " + dev +  " but I wasn't able to take any actions"
            else result = "Sorry, I wasn't able to take any actions, becase " + devFalse + " but I've heard the command " + cmd
            state.pTryAgain = true
			return result
		}        
	}
	else if (deviceType == "cTstat") {
 		if (delayD == true || state.pinTry != null) {  
                deviceD = cTstat.find {t -> t.label?.toLowerCase() == deviceD.toLowerCase()} 
        }
        state.pinTry = null
    	def currentMode = deviceD.latestValue("thermostatMode")
    	def currentHSP = deviceD.latestValue("heatingSetpoint") 
        def currentCSP = deviceD.latestValue("coolingSetpoint") 
    	def currentTMP = deviceD.latestValue("temperature") 
    	def newSetPoint = currentTMP
		numN = numN < 60 ? 60 : numN >85 ? 85 : numN
        if (unitU == "degrees") {
    		newSetPoint = numN
            int cNewSetPoint = newSetPoint
    		if (newSetPoint > currentTMP) {
    			if (currentMode == "off") { // currentMode == "cool" || removed so it does't change Modes as frequently 2/16/2017
    				deviceD?."heat"()
					if (debug) log.trace 	"Turning heat on because requested temperature of '${newSetPoint}' "+
                    						"is greater than current temperature of '${currentTMP}' " 
    			}
				deviceD?.setHeatingSetpoint(newSetPoint)
                result = "Ok, setting " + deviceD + " heating to " + cNewSetPoint 
                    if (delayD == false) { 
                    	state.pinTry = null
                    	return result 
                    }
            }
 			else if (newSetPoint < currentTMP) {
				if (currentMode == "off") { //currentMode == "heat" || removed so it does't change Modes as frequently 2/16/2017
					deviceD?."cool"()
					if (debug) log.trace "Turning AC on because requested temperature of '${newSetPoint}' is less than current temperature of '${currentTMP}' "    
				}
				deviceD?.setCoolingSetpoint(newSetPoint)                 
				if (debug) log.trace "Adjusting Cooling Set Point to '${newSetPoint}' because requested temperature is less than current temperature of '${currentTMP}'"
                result = "Ok, setting " + deviceD + " cooling to " + cNewSetPoint + " degrees "
                    if (delayD == false) { 
                    	return result 
                    }                       
            }
            else result = "Your room temperature is already " + cNewSetPoint + " degrees "
                    if (delayD == false) { 
                    	return result 
                    }
		}
        if (deviceCommand == "cut off") {
        	deviceD?."off"()
            result = "Ok, cutting off the " + deviceD
            return result
            }
        if (deviceCommand == "cut on") {
        	deviceD?."auto"()
            result = "Ok, setting the " + deviceD + " to auto mode"
            return result
            }
        if (deviceCommand == "off") {
        	deviceD?."off"()
            result = "Ok, turning off the " + deviceD
            return result
            }
        if (deviceCommand == "on") {
        	deviceD?."auto"()
            result = "Ok, turning the " + deviceD + " to auto mode"
            return result
            }    
		if (deviceCommand == "increase") {
			newSetPoint = currentTMP + cTemperature
			newSetPoint = newSetPoint < 60 ? 60 : newSetPoint >85 ? 85 : newSetPoint
            int cNewSetPoint = newSetPoint
			if (currentMode == "cool" || currentMode == "off") {
				deviceD?."heat"()
                deviceD?.setHeatingSetpoint(newSetPoint)
                if (debug) log.trace "Turning heat on because requested command asked for heat to be set to '${newSetPoint}'"
                result = "Ok, turning the heat mode on " + deviceD + " and setting heating to " + cNewSetPoint + " degrees "
                return result 
			}
			else {
				if  (currentHSP < newSetPoint) {
					deviceD?.setHeatingSetpoint(newSetPoint)
					thermostat?.poll()
					if (debug) log.trace "Adjusting Heating Set Point to '${newSetPoint}'"
                    result = "Ok, setting " + deviceD + " heating to " + cNewSetPoint + " degrees "
                        if (delayD == false) { 
                            return result 
                        }    
                }
                else {
                   	if (debug) log.trace "Not taking action because heating is already set to '${currentHSP}', which is higher than '${newSetPoint}'" 
                    result = "Your heating set point is already higher than  " + cNewSetPoint + " degrees "
                    if (delayD == false) { 
                    	return result 
                    }    
               	}  
            }
       	}
        if (deviceCommand == "decrease") {
        	newSetPoint = currentTMP - cTemperature
        	newSetPoint = newSetPoint < 60 ? 60 : newSetPoint >85 ? 85 : newSetPoint     
            int cNewSetPoint = newSetPoint
            if (currentMode == "heat" || currentMode == "off") {
        		deviceD?."cool"()
                deviceD?.setCoolingSetpoint(newSetPoint)
        		if (debug) log.trace "Turning AC on because requested command asked for cooling to be set to '${newSetPoint}'"
                result = "Ok, turning the AC mode on " + deviceD + " and setting cooling to " + cNewSetPoint + " degrees "
                return result                 
        	}   	
        	else {
        		if (currentCSP > newSetPoint) {
        			deviceD?.setCoolingSetpoint(newSetPoint)
        			thermostat?.poll()
        			if (debug) log.trace "Adjusting Cooling Set Point to '${newSetPoint}'"
        			result = "Ok, setting " + deviceD + " cooling to " + cNewSetPoint + " degrees "
                    if (delayD == false) { 
                    	return result 
                    }
                }
        		else {
        			if (debug) log.trace "Not taking action because cooling is already set to '${currentCSP}', which is lower than '${newSetPoint}'"  
                    result = "Your cooling set point is already lower than  " + cNewSetPoint + " degrees "
                    if (delayD == false) { 
                    	return result 
                    }
                } 
        	}  
        }
    }
	else if (deviceType == "cLock") {
    	if (delayD == true || state.pinTry != null) {  
        	deviceD = cLock.find {l -> l.label.toLowerCase() == deviceD.toLowerCase()} 
        }
        state.pinTry = null
   		deviceD."${deviceCommand}"()
        if (deviceCommand == "lock") result = "Ok, locking " + deviceD
        else if (deviceCommand == "unlock") result = "Ok, unlocking the  " + deviceD                    
        if (delayD == false) {return result}  
	}
	else if (deviceType == "cPresence") {
    	if (delayD == true || state.pinTry != null) {  
        	deviceD = cPresence.find {l -> l.label.toLowerCase() == deviceD.toLowerCase()} 
        }
        state.pinTry = null
   		deviceD."${deviceCommand}"()
        if (deviceCommand == "arrived") result = "Ok, checking in " + deviceD
        else if (deviceCommand == "departed") result = "Ok, checking out  " + deviceD                    
        if (delayD == false) {return result}  
	}
    
    else if (deviceType == "cDoor" || deviceType == "cRelay" ) {
    	def cmd = deviceCommand
        if(degug) log.warn "pinTry = ${state.pinTry}"
        if (delayD == true || state.pinTry != null || state.pContCmdsR == "door" ) {  
            def deviceR = cRelay.find {r -> r.label.toLowerCase() == deviceD.toLowerCase()}
            def deviceW = cWindowCover.find {w -> w.label.toLowerCase() == deviceD.toLowerCase()}
            deviceD = cDoor.find {d -> d.label.toLowerCase() == deviceD.toLowerCase()}   
            if (deviceR || deviceW ) {deviceD = deviceR ?: deviceW}
        }
        if (deviceType == "cRelay") {
		     cmd = "on"
        }
        state.pinTry = null
        deviceD."${cmd}"()
        state.pContCmdsR = null //"reverse"
        if (deviceCommand == "open") {result = "Ok, opening the " + deviceD}
        if (deviceCommand == "close") {result = "Ok, closing the  " + deviceD}                   
        if (delayD == false || delayD == null) {return result}  
	}
    else if (deviceType == "cFan") {
		if (cHigh == null) cHigh = 99
		if (cMedium == null) cMedium = 66
        if (cLow == null) cLow = 33
        if (cFanLevel == null) cFanLevel = 33
		if (delayD == true) {  
        	deviceD = cFan.find {f -> f.label == deviceD}   
        }
		def currLevel = deviceD.latestValue("level")
		def currState = deviceD.latestValue("switch")
		def newLevel = cFanLevel     
        	if (deviceCommand == "increase") {
            	newLevel =  currLevel + newLevel
            	newLevel = newLevel < 0 ? 0 : newLevel >100 ? 100 : newLevel
                deviceD.setLevel(newLevel)
                result = "Ok, increasing  " + deviceD + " to " + newLevel + " percent"
       				if (delayD == false) { return result }
            }
            else if (deviceCommand == "decrease") {
               	newLevel =  currLevel - newLevel
            	newLevel = newLevel < 0 ? 0 : newLevel >100 ? 100 : newLevel
                deviceD.setLevel(newLevel)
                result = "Ok, decreasing  " + deviceD + " to " + newLevel + " percent"
       				if (delayD == false) { return result }        
            }
            else {
                if (deviceCommand == "high") {newLevel = cHigh}
                if (deviceCommand == "medium") {newLevel = cMedium}
                if (deviceCommand == "low") {newLevel = cLow}
                    deviceD.setLevel(newLevel)
                    result = "Ok, setting  " + deviceD + " to " + newLevel + " percent"
                    if (delayD == false) {return result} 
           }           
	}
	if (deviceType == "cVolume" || deviceType == "cHarmony"  ) {
   		if (deviceCommand == "increase" || deviceCommand == "decrease" || deviceCommand == "setLevel" || deviceCommand == "mute" || deviceCommand == "unmute"){
            def currLevel = deviceD.latestValue("level")
            def currState = deviceD.latestValue("switch")
            if (cVolLevel == null) {cVolLevel = 2}
            def newLevel = cVolLevel*10
			if (unitU == "percent") newLevel = numN      
            if (deviceCommand == "mute" || deviceCommand == "unmute") {
				deviceD."${deviceCommand}"()
            	def volText = deviceCommand == "mute" ? "muting" : deviceCommand == "unmute" ? "unmuting" : "adjusting" 
                result = "Ok, " + volText + " the " + deviceD
            	return result
            }
            if (deviceCommand == "increase") {
            	if (unitU == "percent") {
                	newLevel = numN
                }   
                else {
                	newLevel =  currLevel + newLevel
            		newLevel = newLevel < 0 ? 0 : newLevel >100 ? 100 : newLevel
            	}
            }
            if (deviceCommand == "decrease") {
            	if (unitU == "percent") {
                	newLevel = numN
                }   
                else {
                	newLevel =  currLevel - newLevel
            		newLevel = newLevel < 0 ? 0 : newLevel >100 ? 100 : newLevel
                }            
            }
            if (deviceCommand == "setLevel") {
            	if (unitU == "percent") {
                	newLevel = numN
                }   
                else {
                	newLevel =  numN*10
            		newLevel = newLevel < 0 ? 0 : newLevel >100 ? 100 : newLevel
                }            
            }
            if (newLevel > 0 && currState == "off") {
            	deviceD.on()
            	deviceD.setLevel(newLevel)
            }
            else {                                    
            	if (newLevel == 0 && currState == "on") {deviceD.off()}
                else {deviceD.setLevel(newLevel)}
            } 
            result = "Ok, setting  " + deviceD + " volume to " + newLevel + " percent"
            return result
    	}
    	else {
		//HARMONY ACTIONS
			if (deviceCommand == "start" || deviceCommand == "switch" || deviceCommand == "on" || deviceCommand == "off" || deviceCommand == "end" || deviceCommand == "set" ) {
                if(deviceType == "cHarmony") {     	
                	if(delayD == true){deviceD = cMedia.first()} 
                    	if (deviceCommand == "start" || deviceCommand == "switch" || deviceCommand == "on" || deviceCommand == "set"){
							deviceCommand = "startActivity"
                            if (unitU != "undefined"){
                            	deviceD."${deviceCommand}"(unitU)
                        		deviceD.refresh() 
                        		if(debug) log.debug "starting - deviceD: ${deviceD.label}, deviceCommand:${deviceCommand}, unitU:${unitU}"
                                result = "Ok, starting " + deviceD + " activity"
                        		return result
                            }
                            else {
                                if(state.lastActivity != null){
                                    def activityId = null
                                    def sMedia = cMedia.first()
                                    def activities = sMedia.currentState("activities").value
                                    def activityList = new groovy.json.JsonSlurper().parseText(activities)
                                    activityList.each { it ->  
                                        def activity = it
                                        if(activity.name == state.lastActivity) {
                                            activityId = activity.id
                                        }    	
                                    }
                                    deviceD."${deviceCommand}"(activityId)
                                    deviceD.refresh() 
                                    result = "Ok, starting " + deviceD + " activity"
                                    return result
                                }
                                else { 
                                    if(debug) log.error "last activity must be saved - deviceD: ${deviceD.label}, deviceCommand:${deviceCommand}, activityId:${activityId}"
                                    result = "Sorry for the trouble, but in order for EchoSistant to be able to start where you left off, the last activity must be saved"
                                    return result
                                }
                         	}
                    }
                    else{
                    	def activityId = null
                        def currState = deviceD.currentState("currentActivity").value
						def activities = deviceD.currentState("activities").value
						def activityList = new groovy.json.JsonSlurper().parseText(activities)
                		if (currState != "--"){
							activityList.each { it ->  
								def activity = it
                                if(activity.name == currState) {
                                    activityId = activity.id
								}    	
                            }
                        	deviceCommand = "activityoff"
							state.lastActivity = currState
                            //deviceCommand =  "alloff" // 2/10/2017 changed to turn off current activity to avoid turning OFF all hubs
							if(debug) log.debug "ending - deviceD: ${deviceD.label}, deviceCommand:${deviceCommand}, activityId:${activityId}"
                            //deviceD."${deviceCommand}"(activityId)
                            deviceD."${deviceCommand}"()
                            deviceD.refresh()
                        	result = "Ok, turning off " + currState
                        	return result
                        }
                        else {
                        	result = "${deviceD} is already off"
                            state.pTryAgain = true
                            return result
                        }
                   }
                }
            }
       }
    }
}
/************************************************************************************************************
   SECURITY CONTROL - from Lambda via page s
************************************************************************************************************/
def controlSecurity(param) {
		//FROM LAMBDA
        def command = params.sCommand
        def num = params.sNum
        def sPIN = params.sPIN
        def type = params.sType
        def control = params.sControl       
		def pintentName = params.intentName
        //FROM CONTROL MODULE 
        def cCommand = param?.command
        def cNum = param?.num      
		def cPintentName = param?.pintentName        
        log.warn "cCommand = ${cCommand},cNum = ${cNum}, cPintentName = ${cPintentName}"
        
		command = command == "null" ? "undefined" : command      
        num = num == "null" ? "undefined" : num      
        sPIN = sPIN == "null" ? "undefined" : sPIN      
        type = type == "null" ? "undefined" : type      
        control = control == "null" ? "undefined" : control   

        if(cCommand){
        	command = cCommand
            num = cNum
            pintentName = cPintentName
        }
		log.warn "command = ${command},num = ${num}, pintentName = ${pintentName}"
        //OTHER VARIABLES
        def String outputTxt = (String) null 
		def pPIN = false
        def String secCommand = (String) null
        def delay = false
        def data = [:]
        	control = control?.replaceAll("[^a-zA-Z0-9]", "")
        	sPIN = sPIN == "?" ? "undefined" : sPIN
        if (num == "undefined" || num =="?") {num = 0 } 
        	num = num as int
        
        if (debug) log.debug "System Control Data: (sCommand)= ${command},(sNum) = '${num}', (sPIN) = '${sPIN}'," +
        					 " (type) = '${type}', (sControl) = '${control}',(pintentName) = '${pintentName}'"
	def sProcess = true
    state.pTryAgain = false
//try {   
    if (pintentName == "security") { 
    log.warn "security intent"
		if (ptts == "this is a test"){
			outputTxt = "Congratulations! Your EchoSistant is now setup properly" 
			return ["outputTxt":outputTxt, "pContCmds":state.pContCmds, "pShort":state.pShort, "pContCmdsR":state.pContCmdsR, "pTryAgain":state.pTryAgain, "pPIN":pPIN]       
    	}
        def modes = location.modes.name
    	def currMode = location.currentMode
    	def routines = location.helloHome?.getPhrases()*.label
    	def currentSHM = location.currentState("alarmSystemStatus")?.value
    	// HANDLING SHM
        if (type != "mode") { 
            if (control == "status") {      
                    currentSHM = currentSHM == "off" ? "disabled" : currentSHM == "away" ? "Armed Away" : currentSHM == "stay" ? "Armed Home" : "unknown"
                    outputTxt = "Your Smart Home Monitor Status is " +  currentSHM
                    state.pTryAgain = false
                    return ["outputTxt":outputTxt, "pContCmds":state.pContCmds, "pShort":state.pShort, "pContCmdsR":state.pContCmdsR, "pTryAgain":state.pTryAgain, "pPIN":pPIN]
            }
            if (command == "cut off" || command == "cancel" || command == "stop" || command == "disable" || command == "deactivate" || command == "off" || command == "disarm") {
            log.warn "command disarm"
                secCommand = currentSHM == "off" ? null : "off"
                    if (secCommand == "off"){
                    delay = false
                    data = [command: secCommand, delay: delay]
                    if(cPIN && state.usePIN_SHM == true){
                        state.lastAction = data
                        state.pContCmdsR = "security"
                        //RUN PIN VALIDATION PROCESS
                        def pin = "undefined"
                        command = "validation"
                        def unit = "security"
                        outputTxt = pinHandler(pin, command, num, unit)
                        pPIN = true
                        if (state.pinTry == 3) {pPIN = false}
                        log.warn "try# ='${state.pinTry}'"
                        return ["outputTxt":outputTxt, "pContCmds":state.pContCmds, "pShort":state.pShort, "pContCmdsR":state.pContCmdsR, "pTryAgain":state.pTryAgain, "pPIN":pPIN]
                    }
                    else {               
                        outputTxt = securityHandler(data)
                        return ["outputTxt":outputTxt, "pContCmds":state.pContCmds, "pShort":state.pShort, "pContCmdsR":state.pContCmdsR, "pTryAgain":state.pTryAgain, "pPIN":pPIN]
                    }
                }
                else {
                outputTxt = "The Smart Home Monitor is already set to " + command
                state.pContCmdsR = "undefined"
                return ["outputTxt":outputTxt, "pContCmds":state.pContCmds, "pShort":state.pShort, "pContCmdsR":state.pContCmdsR, "pTryAgain":state.pTryAgain, "pPIN":pPIN]
                }
            }
            if (command == "start" || command == "enable" || command == "activate" || command == "schedule" || command == "arm" || command == "on") {
            log.warn "command arm"
                if(control == "stay" || control == "away") {  
                    secCommand = control == "stay" ? "stay" : control == "away" ? "away" : control
                    def process = true
                }
                else {
                    outputTxt = "Are you staying home or leaving?"
                    state.pContCmdsR = "stayORleave"
                    def process = false
                    state.lastAction = num
                    return ["outputTxt":outputTxt, "pContCmds":state.pContCmds, "pShort":state.pShort, "pContCmdsR":state.pContCmdsR, "pTryAgain":state.pTryAgain, "pPIN":pPIN]
                }     
            }	
            if(process == true || control.contains("staying") || control == "leaving" || control == "stay" || control == "away") {
                secCommand = control.contains("staying") ? "stay" : control == "leaving" ? "away" : control == "stay" ? "stay" : control == "away" ? "away" : secCommand
                if (state.pContCmdsR == "stayORleave") {num = state.lastAction}           
                if (num > 0) {               
                    def numText = getUnitText ("minute", num)
                    delay = true
                    data = [command: secCommand, delay: delay]
                    runIn(num*60, securityHandler, [data: data])
                    outputTxt = "Ok, changing the Smart Home Monitor to armed stay in " + numText.text
                    state.pContCmdsR = "undefined"
                    state.pTryAgain = false
                    return ["outputTxt":outputTxt, "pContCmds":state.pContCmds, "pShort":state.pShort, "pContCmdsR":state.pContCmdsR, "pTryAgain":state.pTryAgain, "pPIN":pPIN]
                }            
                else {
                    delay = false
                    data = [command: secCommand, delay: delay]			
                    outputTxt = securityHandler(data)
                    state.pContCmdsR = "undefined"
                    return ["outputTxt":outputTxt, "pContCmds":state.pContCmds, "pShort":state.pShort, "pContCmdsR":state.pContCmdsR, "pTryAgain":state.pTryAgain, "pPIN":pPIN]
               }
            }
        }
        else {
        	if(currMode != control){
                modes?.find { m -> 
					def mMatch = m.replaceAll("[^a-zA-Z0-9]", "")
                    if(mMatch.toLowerCase() == control.toLowerCase()) {
                        if(currMode !=  m) {
                            if(cPIN && state.usePIN_Mode == true) {
                                delay = false
                                data = [command: m, delay: delay]
                                state.lastAction = data
                                state.pContCmdsR = "mode"
                                //RUN PIN VALIDATION PROCESS
                                def pin = "undefined"
                                command = "validation"
                                def unit = "mode"
                                outputTxt = pinHandler(pin, command, num, unit)
                                pPIN = true
                                if (state.pinTry == 3) {pPIN = false}
                                log.warn "try# ='${state.pinTry}'"                        
                            }
                            else { 
                                location.setMode(m)
                                outputTxt = "I changed your location mode to " + control
                            }
                        }
                        else {
                            outputTxt = "Your location mode is already " + control
                            return ["outputTxt":outputTxt, "pContCmds":state.pContCmds, "pShort":state.pShort, "pContCmdsR":state.pContCmdsR, "pTryAgain":state.pTryAgain, "pPIN":pPIN]
                        }
               		}
               }
               if (outputTxt != null) {
               return ["outputTxt":outputTxt, "pContCmds":state.pContCmds, "pShort":state.pShort, "pContCmdsR":state.pContCmdsR, "pTryAgain":state.pTryAgain, "pPIN":pPIN]
            	} 
            }
            else {
                outputTxt = "Your location mode is already " + control
                return ["outputTxt":outputTxt, "pContCmds":state.pContCmds, "pShort":state.pShort, "pContCmdsR":state.pContCmdsR, "pTryAgain":state.pTryAgain, "pPIN":pPIN]
        	}
        }
		if (control != "undefined") {	
            routines?.find {r -> 
                def rMatch = r.replaceAll("[^a-zA-Z0-9]", "")
            	if(rMatch.toLowerCase() == control.toLowerCase()){
                   		if(cPIN && cRoutines) {
                         	def pinRoutine =  cRoutines.find {r1 -> r1 == r}  
                            if (pinRoutine) {
                                delay = false
                                data = [command: r, delay: delay]
                                state.lastAction = data
                                state.pContCmdsR = "routine"
                                //RUN PIN VALIDATION PROCESS
                                def pin = "undefined"
                                command = "validation"
                                def unit = "routine"
                                outputTxt = pinHandler(pin, command, num, unit)
                                pPIN = true
                                if (state.pinTry == 3) {pPIN = false}
                                log.warn "try# ='${state.pinTry}'"                        
                            }
                            /* faluty logic ... removed 2/20/17 Bobby
                            else {
                            	log.warn "running routine = ${r}"
                                location.helloHome?.execute(r)
                                outputTxt = "Ok, I am running the " + control + " routine"
                                log.warn " outputTxt = ${outputTxt}"
                            }
                            */
                    	}
                        else {
                                location.helloHome?.execute(r)
                                outputTxt = "Ok, I am running the " + control + " routine"
                    	}
            	}
			}
         	if (outputTxt != null) {
            	return ["outputTxt":outputTxt, "pContCmds":state.pContCmds, "pShort":state.pShort, "pContCmdsR":state.pContCmdsR, "pTryAgain":state.pTryAgain, "pPIN":pPIN]
           }   
    	}
        def hText = type != "undefined" ? "control " + type : control != "undefined" ? "manage " + control +  " as a system control" : "manage your system controls" 
        def sText = type != "undefined" ? type : control != "undefined" ? control : "something"  
			if (state.pShort != true){ 
				outputTxt = "Sorry, I heard that you were looking to " + hText + " but Echosistant wasn't able to take any actions"
			}
			else {outputTxt = "I've heard " + sText +  " but I wasn't able to manage your system controls "} 
            state.pTryAgain = true
            return ["outputTxt":outputTxt, "pContCmds":state.pContCmds, "pShort":state.pShort, "pContCmdsR":state.pContCmdsR, "pTryAgain":state.pTryAgain, "pPIN":pPIN]
    }

    } 
    /*catch (Throwable t) {
        log.error t
        outputTxt = "Oh no, something went wrong. If this happens again, please reach out for help!"
        state.pTryAgain = true
        return ["outputTxt":outputTxt, "pContCmds":state.pContCmds, "pShort":state.pShort, "pContCmdsR":state.pContCmdsR, "pTryAgain":state.pTryAgain, "pPIN":pPIN]
	}
}	*/
/************************************************************************************************************
	SECURITY CONTROL HANDLER
************************************************************************************************************/ 
def securityHandler(data) {
	def sCommand = data.command
    def sDelay = data.delay
    def String result = (String) "Command " + sCommand + " is not supported by Smart Home Monitor"
	def currentSHM = location.currentState("alarmSystemStatus")?.value
    if (sCommand == "stay" || sCommand == "away" || sCommand == "off"){
        if (sCommand != currentSHM) {
            sendLocationEvent(name: "alarmSystemStatus", value: sCommand)
            if (sDelay == false) {
                if(sCommand == "away" || sCommand == "stay") 	{result = "I changed the Smart Home Monitor to " + sCommand }
                if(sCommand == "off") 	{result = "I disarmed the Smart Home Monitor" }
                return result
            }
        }
        else {
            result = "The Smart Home Monitor is already set to " + sCommand
            state.pContCmdsR = "undefined"
            return result
        }
    }
    return result
}
/************************************************************************************************************
   TEXT TO SPEECH PROCESS - Lambda via page t
************************************************************************************************************/
def processTts() {
		//LAMBDA VARIABLES
		def ptts = params.ttstext 
        def pintentName = params.intentName
        //OTHER VARIABLES
        def String outputTxt = (String) null 
 		def String pContCmdsR = (String) null
        def pContCmds = false
        def pTryAgain = false
        def pPIN = false
        def dataSet = [:]
        if (debug) log.debug "Messaging Profile Data: (ptts) = '${ptts}', (pintentName) = '${pintentName}'"   
                
        pContCmdsR = "profile"
		def tProcess = true
//try {
        
	if (ptts == "this is a test"){
		outputTxt = "Congratulations! Your EchoSistant is now setup properly" 
		return ["outputTxt":outputTxt, "pContCmds":state.pContCmds, "pShort":state.pShort, "pContCmdsR":state.pContCmdsR, "pTryAgain":state.pTryAgain, "pPIN":pPIN]       
    }
        
        if(ptts.contains("no ") || ptts == "no" || ptts == "stop" || ptts == "cancel" || ptts == "kill it" || ptts == "zip it" || ptts == "yes" && state.pContCmdsR != "wrongIntent"){
        	if(ptts == "no" || ptts == "stop" || ptts == "cancel" || ptts == "kill it" || ptts == "zip it" || ptts.contains("thank")){
                outputTxt = "ok, I am here if you need me"
                pContCmds = false
                return ["outputTxt":outputTxt, "pContCmds":pContCmds, "pShort":state.pShort, "pContCmdsR":pContCmdsR, "pTryAgain":state.pTryAgain, "pPIN":pPIN]
        	}
			else {
                outputTxt = "ok, please continue, "
                pContCmds = false
                return ["outputTxt":outputTxt, "pContCmds":pContCmds, "pShort":state.pShort, "pContCmdsR":pContCmdsR, "pTryAgain":state.pTryAgain, "pPIN":pPIN]
        	}        
        }
        else{
            childApps.each {child ->
                if (child.label.toLowerCase() == pintentName.toLowerCase()) { 
                    if (debug) log.debug "Found a profile: '${pintentName}'"
                    pintentName = child.label
                    // recording last message
                    state.lastMessage = ptts
                    state.lastIntent = pintentName
                    state.lastTime = new Date(now()).format("h:mm aa", location.timeZone)
                    dataSet = [ptts:ptts, pintentName:pintentName] 
					def childRelease = child.checkRelease()
					log.warn "childRelease = $childRelease"
                    def pResponse = child.profileEvaluate(dataSet)
                    outputTxt = pResponse?.outputTxt
                    pContCmds = pResponse?.pContCmds
                    pContCmdsR = pResponse?.pContCmdsR
                    pTryAgain = pResponse?.pTryAgain
                }
            }
            if (outputTxt?.size()>0){
                return ["outputTxt":outputTxt, "pContCmds":pContCmds, "pShort":state.pShort, "pContCmdsR":pContCmdsR, "pTryAgain":pTryAgain, "pPIN":pPIN]
            }
            else {
                if (state.pShort != true){
                	outputTxt = "I wish I could help, but EchoSistant couldn't find a Profile named " + pintentName + " or the command may not be supported"
                }
                else {outputTxt = "I've heard " + pintentName + " , but I wasn't able to take any actions "} 
                pTryAgain = true
                return ["outputTxt":outputTxt, "pContCmds":pContCmds, "pShort":state.pShort, "pContCmdsR":pContCmdsR, "pTryAgain": pTryAgain, "pPIN":pPIN]
            }
        	
            def hText = "run a messaging and control profile"
			if (state.pShort != true){ 
				outputTxt = "Sorry, I heard that you were looking to " + hText + " but Echosistant wasn't able to take any actions "
			}
			else {outputTxt = "I've heard " + pintentName + " , but I wasn't able to take any actions "}         
			pTryAgain = true
			return ["outputTxt":outputTxt, "pContCmds":pContCmds, "pShort":state.pShort, "pContCmdsR":pContCmdsR, "pTryAgain":pTryAgain, "pPIN":pPIN]              
    	}

} 
/*catch (Throwable t) {
        log.error t
        outputTxt = "Oh no, something went wrong. If this happens again, please reach out for help!"
        state.pTryAgain = true
        return ["outputTxt":outputTxt, "pContCmds":pContCmds, "pShort":state.pShort, "pContCmdsR":pContCmdsR, "pTryAgain":state.pTryAgain, "pPIN":pPIN]
    } 
}	*/
/************************************************************************************************************
   REMINDERS AND EVENTS PROCESS - Lambda via page r
************************************************************************************************************/
def remindersHandler() {
		//LAMBDA VARIABLES
	def rCalendarName = params.rCalendarName 
	def rProfile = params.rProfile   
	def rType = params.rType //type of event
	def rFrequency = params.rFrequency //units/frequency 
	def rStartingDate = params.rStartingDate 
	def rStartingTime = params.rStartingTime 
	def rDuration = params.rDuration // number
	def rMessage = params.rMessage  
        //OTHER VARIABLES
	def variable = variable == null ? "undefined" : variable
        def String outputTxt = (String) null 
 		def String pContCmdsR = (String) null
        def pContCmds = false
        def pTryAgain = false
        def pPIN = false
        def String messageType  = state.esEvent.eType //(String) null
		def multiCalendar = false
		def String calendar = (String) null
        String newTime
        String newDate
        def data = [:]
//try {
        if (debug) log.debug 	"Reminders & Events Profile Data: (rCalendarName) = $rCalendarName,(rType) = $rType, (rFrequency) = $rFrequency, (rStartingDate) = $rStartingDate," +
        						" (rStartingTime) = $rStartingTime,(rDuration) = $rDuration,(rMessage) = $rMessage"
	
    if(!state.esEvent.eStartingDate && rStartingDate != "undefined" &&  rStartingDate != null) {
		state.esEvent.eStartingDate = rStartingDate
	}

//WHEN TYPE COMES IN    
    if (rType != "undefined" &&  rType != null){
    def String missingField = (String) null 
    	rType = rType.contains("event") ? "event" : rType.contains("recurring") ? "recurring" : rType.contains("a reminder") ? "reminder" : rType
        state.esEvent.eType = rType
        if(rStartingDate && !state.esEvent.eStartingDate && rStartingDate != "undefined" ) state.esEvent.eStartingDate = rStartingDate
        if(rStartingTime && !state.esEvent.eStartingTime && rStartingTime != "undefined") state.esEvent.eStartingTime = rStartingTime
			if(state.esEvent.eStartingDate && state.esEvent.eStartingTime){
                def olddate = state.esEvent.eStartingDate + " " + state.esEvent.eStartingTime
                Date date = Date.parse("yyyy-MM-dd HH:mm",olddate)
                newTime = date.format( "h:mm aa" )
                newDate = date.format( 'MM/dd/yyyy' )
        	}
		if(state.esEvent.eType == "event"){
        	if (state.esEvent.eText && state.esEvent.eStartingDate && state.esEvent.eStartingTime && state.esEvent.eCalendar && state.esEvent.eDuration){
            	outputTxt = "Ok, scheduling event to $state.esEvent.eText on $newDate at $newTime, is that correct?"
        	}
        	else missingField = !state.esEvent.eText ? "What is the event?" : !state.esEvent.eStartingDate ? "Starting on what date?" : !state.esEvent.eStartingTime ? "Starting at what time?" : !state.esEvent.eCalendar ? "Which calendar?" : !state.esEvent.eDuration ? "For fow long?" : null
		}
        if(state.esEvent.eType == "reminder") {
			if (state.esEvent.eText && state.esEvent.eStartingDate && state.esEvent.eStartingTime){
					outputTxt = "Ok, scheduling reminder to $state.esEvent.eText on $newDate at $newTime, is that correct?"
            }
            else missingField = !state.esEvent.eText ? "What is the event?" : !state.esEvent.eStartingDate ? "Starting on what date?" : !state.esEvent.eStartingTime ? "Starting at what time?" : null 
		}
        if(state.esEvent.eType == "recurring") {
			if (state.esEvent.eText && state.esEvent.eStartingDate && state.esEvent.eStartingTime && state.esEvent.eFrequency && state.esEvent.eDuration){	            
				def repeatUnit = rFrequency == "hourly" ? "hours" : rFrequency == "daily" ? "days" : rFrequency == "weekly" ? "days" : rFrequency == "monthly" ? "months" : rFrequency == "yearly" ? "months" : null                    
                outputTxt = "Ok, scheduling $state.esEvent.eFrequency reminder to $state.esEvent.eText every $state.esEvent.eDuration"+
                    			" $repeatUnit, starting on $newDate at $newTime, is that correct?"            
			}
			else missingField = !state.esEvent.eText ? "What is the event?" : !state.esEvent.eStartingDate ? "Starting on what date?" : !state.esEvent.eStartingTime ? "Starting at what time?" : !state.esEvent.eDuration ? "For fow long?" : null
    	}
		if(missingField) {
        	log.warn "missingField = $missingField"
			outputTxt = missingField
		}
        pContCmdsR = "feedback" 
		return ["outputTxt":outputTxt, "pContCmds":state.pContCmds, "pShort":state.pShort, "pContCmdsR":pContCmdsR, "pTryAgain":pTryAgain, "pPIN":pPIN] 
    }
    if(state.esEvent.eText && state.esEvent.eType == "quickReminder" && !state.esEvent.eDuration){
//WHEN DURATION COMES IN
        if(rDuration != null && rDuration != "undefined" && rFrequency != "undefined" && rFrequency != null){
			state.esEvent.eDuration = rDuration
			state.esEvent.eFrequency = rFrequency
            outputTxt = "Ok, scheduling quick reminder to $state.esEvent.eText$state.esEvent.eDuration $state.esEvent.eFrequency is that correct?"
			pContCmdsR = "feedback"            
    	}
        else {
            outputTxt = "Sorry, I still didn't get the number, "
            pTryAgain = true
        }
		return ["outputTxt":outputTxt, "pContCmds":state.pContCmds, "pShort":state.pShort, "pContCmdsR":pContCmdsR, "pTryAgain":pTryAgain, "pPIN":pPIN] 
	}
//WHEN MESSAGE COMES IN
    if (rMessage != "undefined" &&  rMessage != null){
    	def tts = rMessage
        def quickMessage
        int iLength
        def test = tts.contains("this is a test") ? true : tts.contains("a test") ? true : false 
        if (test){
			outputTxt = "Congratulations! Your EchoSistant is now setup properly" 
			return ["outputTxt":outputTxt, "pContCmds":state.pContCmds, "pContCmdsR":pContCmdsR, "pTryAgain":pTryAgain, "pPIN":pPIN]       
   		}
        def reminder = tts.startsWith("set a reminder to") ? "set a reminder to " : tts.startsWith("set reminder to") ? "set reminder to " : null
        if (reminder == null) reminder = tts.startsWith("remind me to") ? "remind me to " : tts.startsWith("set the reminder to") ? "set the reminder to " : null 
        if (reminder == null) reminder = tts.startsWith("i need to") ? "i need to " : tts.startsWith("need to") ? "need to " : tts.startsWith("I need to") ? "I need to " : null 
		if (reminder == null) reminder = tts.startsWith("add a reminder to") ? "add a reminder to " : tts.startsWith("add reminder to") ? "add reminder to " : null         
       	if (reminder == null) reminder = tts.startsWith("schedule reminder to") ? "schedule reminder to " : tts.startsWith("add the reminder to") ? "add the reminder to " : null 	
        if (reminder == null) reminder = tts.startsWith("schedule a reminder to") ? "schedule a reminder to " : tts.startsWith("schedule the reminder to") ? "schedule the reminder to " : null
        if (reminder == null) reminder = tts.startsWith("set a reminder") ? "set a reminder " : tts.startsWith("set reminder") ? "set reminder " : null
        if (reminder == null) reminder = tts.startsWith("remind me") ? "remind me " : tts.startsWith("set the reminder") ? "set the reminder " : null 
		if (reminder == null) reminder = tts.startsWith("add a reminder") ? "add a reminder " : tts.startsWith("add reminder") ? "add reminder " : null         
       	if (reminder == null) reminder = tts.startsWith("schedule reminder") ? "schedule reminder " : tts.startsWith("add the reminder") ? "add the reminder " : null 	
        if (reminder == null) reminder = tts.startsWith("schedule a reminder") ? "schedule a reminder " : tts.startsWith("schedule the reminder") ? "schedule the reminder " : null           
		//QUICK REMINDERS
        def quickReminder = tts.endsWith("minute") ? "minutes" : tts.endsWith("minutes") ? "minutes" : tts.endsWith("hours") ? "hours" : tts.endsWith("hour") ? "hours" : tts.endsWith("day") ? "days" : tts.endsWith("days") ? "days" : "undefined"
        def quickReplace = tts.endsWith("minute") ? "minute" : tts.endsWith("minutes") ? "minutes" : tts.endsWith("hours") ? "hours" : tts.endsWith("hour") ? "hour" : tts.endsWith("day") ? "day" : tts.endsWith("days") ? "days" : "undefined"
        tts = tts.replace("one", "1").replace("two", "2").replace("three", "3").replace("four", "4").replace("five", "5").replace("six", "6").replace("seven", "7").replace("eight", "8").replace("nine", "9")
        def length = tts.findAll( /\d+/ )*.toInteger()
			if(length[0] !=null && quickReminder !="undefined") {
            	iLength = (int)length.get(0)                    
                if(reminder){
                	quickMessage = tts.replace("${reminder}", "").replace("in ${iLength}", "").replace("${quickReplace}", "")
                }
                else{
               		quickMessage = tts ? tts.replace("in ${iLength}", "").replace("${quickReplace}", "") : null
            	}
                if(quickMessage) {
                    state.esEvent.eText = quickMessage
                    state.esEvent.eDuration = length[0]
                    state.esEvent.eFrequency = quickReminder
                    state.esEvent.eType = "quickReminder"
                }
                else {
                    outputTxt = "sorry, I was unable to get the number,  "
                    pTryAgain = true
                    return ["outputTxt":outputTxt, "pContCmds":state.pContCmds, "pShort":state.pShort, "pContCmdsR":pContCmdsR, "pTryAgain":pTryAgain, "pPIN":pPIN] 
                }
            }
            if(quickReminder !="undefined" && iLength != null){ 
                outputTxt = "Ok, scheduling quick reminder to $state.esEvent.eText in $state.esEvent.eDuration $state.esEvent.eFrequency, is that correct?"
				pContCmdsR = "feedback"            
				return ["outputTxt":outputTxt, "pContCmds":state.pContCmds, "pShort":state.pShort, "pContCmdsR":pContCmdsR, "pTryAgain":pTryAgain, "pPIN":pPIN] 
            }   
		//recurring reminders - fields required: rStartingDate & rStartingTime & rFrequency & rMessage & rDuration 
            /*
            What is the reminder? (rMessage)
            How often? - rFrequency
            What is the number of X to repeat the reminder? rDuration + rFrequency
            Starting on what day and time? (rStartingDate & rStartingTime) 
            */
        def recurring = tts.startsWith("set a recurring reminder to") ? "set a recurring reminder to " : tts.startsWith("set recurring reminder to") ? "set recurring reminder to " : null
        if (recurring == null) recurring = tts.startsWith("set the recurring reminder to") ? "set the recurring reminder to " : null 
		if (recurring == null) recurring = tts.startsWith("add a recurring reminder to") ? "add a recurring reminder to " : tts.startsWith("add recurring reminder to") ? "add recurring reminder to " : null         
       	if (recurring == null) recurring = tts.startsWith("schedule recurring reminder to") ? "schedule recurring reminder to " : tts.startsWith("add the recurring reminder to") ? "add the recurring reminder to " : null 	
        if (recurring == null) recurring = tts.startsWith("schedule a recurring reminder to") ? "schedule a recurring reminder to " : tts.startsWith("schedule the recurring reminder to") ? "schedule the recurring reminder to " : null
        if (recurring == null) recurring = tts.startsWith("set a recurring reminder") ? "set a recurring reminder " : tts.startsWith("set recurring reminder") ? "set recurring reminder " : null
        if (recurring == null) recurring = tts.startsWith("set the recurring reminder") ? "set the recurring reminder " : null 
		if (recurring == null) recurring = tts.startsWith("add a recurring reminder") ? "add a recurring reminder " : tts.startsWith("add recurring reminder") ? "add recurring reminder " : null         
       	if (recurring == null) recurring = tts.startsWith("schedule recurring reminder") ? "schedule recurring reminder " : tts.startsWith("add the recurring reminder") ? "add the recurring reminder " : null 	
        if (recurring == null) recurring = tts.startsWith("schedule a recurring reminder") ? "schedule a recurring reminder " : tts.startsWith("schedule the recurring reminder") ? "schedule the recurring reminder " : null       
		//event - fields required: rStartingDate & rStartingTime & rFrequency & rMessage & rDuration (rCalendarName) 
            /*
            What is the reminder? (rMessage)
            Starting on what day and time? (rStartingDate & rStartingTime)
            For how long? - rDuration + rFrequency
            Which Calendar? (rStartingDate & rStartingTime) 
            */        
        def event = tts.startsWith("add an event to my calendar") ? "add an event to my calendar" : null
        if (event == null) event = tts.startsWith("add event to my calendar") ? "add event to my calendar" : null
        if (event == null) event = tts.startsWith("set an event to") ? "set an event to " : tts.startsWith("set event to") ? "set event to " : null
        if (event == null) event = tts.startsWith("set the event to") ? "set the event to " : null 
		if (event == null) event = tts.startsWith("add an event to") ? "add an event to " : tts.startsWith("add event to") ? "add event to " : null         
       	if (event == null) event = tts.startsWith("schedule event to") ? "schedule event to " : tts.startsWith("add the event to") ? "add the event to " : null 	
        if (event == null) event = tts.startsWith("schedule an event to") ? "schedule an event to " : tts.startsWith("schedule the event to") ? "schedule the event to " : null 
        if (event == null) event = tts.startsWith("set an event ") ? "set an event" : tts.startsWith("set event ") ? "set event" : null
        if (event == null) event = tts.startsWith("set the event") ? "set the event " : null 
		if (event == null) event = tts.startsWith("add an event") ? "add an event " : tts.startsWith("add event") ? "add event " : null         
       	if (event == null) event = tts.startsWith("schedule event") ? "schedule event " : tts.startsWith("add the event") ? "add the event " : null 	
        if (event == null) event = tts.startsWith("schedule an event") ? "schedule an event " : tts.startsWith("schedule the event") ? "schedule the event " : null         
        def message = reminder ? tts.replace("${reminder}", "") : recurring ? tts.replace("${recurring}", "") : event ? tts.replace("${event}", "") : null
        messageType = messageType ?: reminder ? "reminder" : recurring ? "recurring" : event ? "event" : null
		log.warn "message type from state: $messageType"
        if(messageType == "event"){
            childApps.each { child ->
            	log.warn " label = $child.label"
                if(child.label == "Reminders") {
                def calendars = child.listGCalendars()
                    multiCalendar = calendars.size() > 1 ? true : false
                    if(multiCalendar == false){
                    	state.esEvent.eCalendar = calendars
                    } 
                }
            }
        }
        log.warn "multiCalendar = $multiCalendar"
        log.warn "messageType = $messageType, rMessage = $rMessage, reminder = $reminder, recurring = $recurring,  event = $event, message = $message"
		state.esEvent.eType = state.esEvent.eType ?: messageType
		if (message == null) message = rMessage
            if(!state.esEvent.eStartingDate && !state.esEvent.eStartingTime) {
                state.esEvent.eText = message
                outputTxt = "Starting on what day and time?"
                pContCmdsR = "feedback"
            }
            else {
                if(!state.esEvent.eStartingDate && state.esEvent.eStartingTime ) {
                    state.esEvent.eText = message
                    outputTxt = "Starting on what date?"
                    pContCmdsR = "feedback" 
                }
                if (state.esEvent.eStartingDate && !state.esEvent.eStartingTime ) {
                state.esEvent.eText = message
                outputTxt = "At what time?"
                pContCmdsR = "feedback" 
                }
                if(state.esEvent.eStartingDate && state.esEvent.eStartingTime && !state.esEvent.eDuration && messageType != "reminder") {
                    state.esEvent.eText = message
                    pContCmdsR = "feedback"
                    if(messageType == "event"){
                    	outputTxt = "For fow long?"
                    }
                    else if (messageType == "recurring"){
                    	outputTxt = "How often?"
                    }
                }
                if(state.esEvent.eStartingDate && state.esEvent.eStartingTime && state.esEvent.eDuration && !state.esEvent.eCalendar && multiCalendar && messageType == "event") {
                    state.esEvent.eText = message
                    outputTxt = "Which Calendar?"
                    pContCmdsR = "feedback"
                }
                if(state.esEvent.eStartingDate && state.esEvent.eStartingTime && messageType == "reminder") {
                    state.esEvent.eText = message
                    def olddate = state.esEvent.eStartingDate + " " + state.esEvent.eStartingTime
                    Date date = Date.parse("yyyy-MM-dd HH:mm",olddate)
                    newTime = date.format( "h:mm aa" )
                    newDate = date.format( 'MM/dd/yyyy' )
            		outputTxt = "Ok, scheduling reminder to $state.esEvent.eText on $newDate at $newTime, is that correct?"
                    pContCmdsR = "feedback"
                }                
                if(!state.esEvent.eType) {
                    state.esEvent.eText = message
                	outputTxt = "Sorry, I didn't catch the type of event, is this a reminder, a recurring reminder or, an event?"
                	pContCmdsR = "feedback"
                }                   
                
            }
            return ["outputTxt":outputTxt, "pContCmds":state.pContCmds, "pShort":state.pShort, "pContCmdsR":pContCmdsR, "pTryAgain":pTryAgain, "pPIN":pPIN] 
    }
//WHEN STARTING DATE & STARTING TIME COMES IN
	if (rStartingDate != "undefined" && rStartingTime != "undefined" && rStartingDate != null && rStartingTime != null){
		state.esEvent.eStartingDate = rStartingDate
        state.esEvent.eStartingTime = rStartingTime
        if (messageType == "reminder" && state.esEvent.eStartingDate && state.esEvent.eStartingTime && state.esEvent.eText){
			if(state.esEvent.eStartingDate && state.esEvent.eStartingTime){
                def olddate = state.esEvent.eStartingDate + " " + state.esEvent.eStartingTime
                Date date = Date.parse("yyyy-MM-dd HH:mm",olddate)
                newTime = date.format( "h:mm aa" )
                newDate = date.format( 'MM/dd/yyyy' )
        	}
            outputTxt = "Ok, scheduling reminder to $state.esEvent.eText on $newDate at $newTime, is that correct?"
			pContCmdsR = "feedback"
    	} 
        if(!state.esEvent.eText) {
        	outputTxt = "What is the event?"
        	pContCmdsR = "feedback"
		}
        else {
            if (!state.esEvent.eDuration && messageType != "reminder" && state.esEvent.eType){
                    pContCmdsR = "feedback"
                    if(messageType == "event"){
                    	outputTxt = "For fow long?"
                    }
                    else if (messageType == "recurring"){
                    	outputTxt = "How often?"
                    }
            }
            else{
            log.warn "eType = $eType"
            	if(multiCalendar && !state.esEvent.eCalendar && messageType == "event"){
                	outputTxt = "Which calendar?"
        			pContCmdsR = "feedback"
            	}
                
                else if (!state.esEvent.eType || state.esEvent.eType == null ){
                	outputTxt = "Sorry, I didn't catch the type of event, is this a reminder, a recurring reminder or an event?"
                	pContCmdsR = "feedback"
               }                 
           	}
      	}
    	return ["outputTxt":outputTxt, "pContCmds":state.pContCmds, "pShort":state.pShort, "pContCmdsR":pContCmdsR, "pTryAgain":pTryAgain, "pPIN":pPIN]     
 	}
    if(rFrequency != "undefined" && rFrequency != null && messageType == "recurring"){
    	if(rFrequency == "hourly" || rFrequency == "daily" || rFrequency == "monthly" || rFrequency == "weekly"){
            def repeatUnit = rFrequency == "hourly" ? "hours" : rFrequency == "daily" ? "days" : rFrequency == "weekly" ? "days" : rFrequency == "monthly" ? "months" : rFrequency == "yearly" ? "months" : null
            log.warn "repeatUnit = $repeatUnit, rFrequency =  $rFrequency, state eDuration = state.esEvent.eDuration"
            if(!state.esEvent.eDuration){
                state.esEvent.eFrequency = rFrequency
            	outputTxt = "What is the number of $repeatUnit to repeat the reminder"
        		pContCmdsR = "feedback"	
  				return ["outputTxt":outputTxt, "pContCmds":state.pContCmds, "pShort":state.pShort, "pContCmdsR":pContCmdsR, "pTryAgain":pTryAgain, "pPIN":pPIN]     
            }
    	}
  	}
	if (rDuration != "undefined" && rDuration != null) {
    	state.esEvent.eDuration = rDuration
        	if(state.esEvent.eText && state.esEvent.eStartingDate && state.esEvent.eStartingTime && !state.esEvent.eCalendar && multiCalendar && messageType == "event"){
                	outputTxt = "Which calendar?"
        			pContCmdsR = "feedback"           
            }
            else {
            	if(messageType == "recurring"){
                	def frequency = state.esEvent.eFrequency
                    def repeatUnit = frequency == "hourly" ? "hours" : frequency == "daily" ? "days" : frequency == "weekly" ? "days" : frequency == "monthly" ? "months" : frequency == "yearly" ? "months" : null
                    if(state.esEvent.eStartingDate && state.esEvent.eStartingTime){
                        def olddate = state.esEvent.eStartingDate + " " + state.esEvent.eStartingTime
                        Date date = Date.parse("yyyy-MM-dd HH:mm",olddate)
                        newTime = date.format( "h:mm aa" )
                        newDate = date.format( 'MM/dd/yyyy' )
                    }
                    outputTxt = "Ok, scheduling $state.esEvent.eFrequency reminder to $state.esEvent.eText every $state.esEvent.eDuration"+
                    			" $repeatUnit, starting on $newDate at $newTime, is that correct?"
            		pContCmdsR = "feedback"
                }
                else {
                	if(messageType == "event"){
                        if (state.esEvent.eText && state.esEvent.eStartingDate && state.esEvent.eStartingTime && state.esEvent.eCalendar && messageType == "event"){
                            if(state.esEvent.eStartingDate && state.esEvent.eStartingTime){
                                def olddate = state.esEvent.eStartingDate + " " + state.esEvent.eStartingTime
                                Date date = Date.parse("yyyy-MM-dd HH:mm", olddate)
                                newTime = date.format( "h:mm aa" )
                                newDate = date.format( 'MM/dd/yyyy' )
                            }
                            outputTxt = "Ok, scheduling event to $state.esEvent.eText on $newDate at $newTime, is that correct?"
                            pContCmdsR = "feedback"                
                        }
                        else {
							def missingField = !state.esEvent.eText ? "What is the event?" : !state.esEvent.eStartingDate ? "Starting on what date?" : !state.esEvent.eStartingTime ? "Starting at what time?" : !state.esEvent.eCalendar ? "Which calendar?" : !state.esEvent.eDuration ? "For fow long?" : null
							log.warn "missingField = $missingField"
            				outputTxt = missingField
							pContCmdsR = "feedback" 			
            			}
            		}
                }
            }
  	return ["outputTxt":outputTxt, "pContCmds":state.pContCmds, "pShort":state.pShort, "pContCmdsR":pContCmdsR, "pTryAgain":pTryAgain, "pPIN":pPIN]     
    }
    if(rCalendarName != "undefined" && rCalendarName != null && messageType == "event"){
    	state.esEvent.eCalendar = rCalendarName
        	if(state.esEvent.eText && state.esEvent.eStartingDate && state.esEvent.eStartingTime && state.esEvent.eDuration){
                if(state.esEvent.eStartingDate && state.esEvent.eStartingTime){
                    def olddate = state.esEvent.eStartingDate + " " + state.esEvent.eStartingTime
                    Date date = Date.parse("yyyy-MM-dd HH:mm",olddate)
                    newTime = date.format( "h:mm aa" )
                    newDate = date.format( 'MM/dd/yyyy' )
                }
            	outputTxt = "Ok, scheduling event to $state.esEvent.eText on $newDate at $newTime, is that correct?"
            	pContCmdsR = "feedback"         
            }
            else {
				def missingField = !state.esEvent.eText ? "What is the event?" : !state.esEvent.eStartingDate ? "Starting on what date?" : !state.esEvent.eStartingTime ? "Starting at what time?" : !state.esEvent.eCalendar ? "Which calendar?" : !state.esEvent.eDuration ? "For fow long?" : null
				log.warn "missingField = $missingField"
            	outputTxt = missingField
				pContCmdsR = "feedback" 
 			}
  	return ["outputTxt":outputTxt, "pContCmds":pContCmds, "pShort":state.pShort, "pContCmdsR":pContCmdsR, "pTryAgain":pTryAgain, "pPIN":pPIN]     
    }
    if (rFrequency == "yes" || rFrequency == "yup" || rFrequency == "yeah" || rFrequency == "you got it" || rFrequency == "no" || rFrequency == "cancel" || rFrequency == "neh" || rFrequency == "nope"){
		if (rFrequency == "yes" || rFrequency == "yup" || rFrequency == "yeah" || rFrequency == "you got it" ){
        	def event = messageType == "recurring" ? "${state.esEvent.eFrequency} reminder" : messageType
            log.warn "event = $event"
			if(event){
                if(state.esEvent.eStartingDate && state.esEvent.eStartingTime){
                    def olddate = state.esEvent.eStartingDate + " " + state.esEvent.eStartingTime
                    Date date = Date.parse("yyyy-MM-dd HH:mm",olddate)
                    newTime = date.format( "h:mm aa" )
                    newDate = date.format( 'MM/dd/yyyy' )
                }
                if(event == "event"){
                	data = ["eCalendar": state.esEvent.eCalendar, "eStartingDate": state.esEvent.eStartingDate , "eStartingTime": state.esEvent.eStartingTime, "eDuration": state.esEvent.eDuration, "eText": state.esEvent.eText]
                    sendLocationEvent(name: "echoSistant", value: "addEvent", data: data, displayed: true, isStateChange: true, descriptionText: "echoSistant add event request")
                    outputTxt = "Great! I sent the event to G Cal to be added on your calendar"
                }
                else {
					data = ["eStartingDate": state.esEvent.eStartingDate , "eStartingTime": state.esEvent.eStartingTime, "eDuration": state.esEvent.eDuration, "eFrequency": state.esEvent.eFrequency, "eText": state.esEvent.eText, "eType": state.esEvent.eType]
            		def sendingTo
                    childApps.each { child ->
            			sendingTo = child.label
                		if(child.label == "Reminders") {
                			outputTxt = child.profileEvaluate(data)
						}
                     }
					state.pendingConfirmation = true
				}
                pContCmds = state.pContCmds
                state.esEvent = [:]
                return ["outputTxt":outputTxt, "pContCmds":pContCmds, "pShort":state.pShort, "pContCmdsR":pContCmdsR, "pTryAgain":pTryAgain, "pPIN":pPIN] 
            }
            else {
				def missingField = !state.esEvent.eText ? "What is the event?" : !state.esEvent.eStartingDate ? "Starting on what date?" : !state.esEvent.eStartingTime ? "Starting at what time?" : !state.esEvent.eCalendar ? "Which calendar?" : !state.esEvent.eDuration ? "For fow long?" : null
				log.warn "missingField = $missingField"
            	outputTxt = missingField
				pContCmdsR = "feedback" 
        	}
        }
		else {
        	if(state.pendingConfirmation == true){
            	outputTxt = "Ok, I am here when you need me"
				state.pendingConfirmation = false
            }
            else outputTxt = "Ok, canceling"
            	state.esEvent = [:]
				pContCmds = false
            	return ["outputTxt":outputTxt, "pContCmds":pContCmds, "pShort":state.pShort, "pContCmdsR":pContCmdsR, "pTryAgain":pTryAgain, "pPIN":pPIN] 
    	}
    } 
    else {
		if (messageType == "reminder" && state.esEvent.eStartingDate && state.esEvent.eStartingTime && state.esEvent.eText){
			outputTxt = "Ok, scheduling reminder to $state.esEvent.eText on $state.esEvent.eStartingDate at $state.esEvent.eStartingTime, is that correct?"
			pContCmdsR = "feedback"
    	}
        else {
    		outputTxt = "Sorry, I didn't get that"
    		pTryAgain = true 
   		}
    }
	if(state.pendingConfirmation == true){
    	if(rProfile != "undefined" && rProfile != null) {
    		outputTxt = "Ok, forwarding reminder to $rProfile"
		}
    	else state.pendingConfirmation = false
	}
    return ["outputTxt":outputTxt, "pContCmds":state.pContCmds, "pShort":state.pShort, "pContCmdsR":pContCmdsR, "pTryAgain":pTryAgain, "pPIN":pPIN] 


} 
/*catch (Throwable t) {
        log.error t
        outputTxt = "Oh no, something went wrong. If this happens again, please reach out for help!"
        state.pTryAgain = true
        return ["outputTxt":outputTxt, "pContCmds":pContCmds, "pShort":state.pShort, "pContCmdsR":pContCmdsR, "pTryAgain":state.pTryAgain, "pPIN":pPIN]
    } 
}	*/
/***********************************************************************************************************
		SMART HOME MONITOR STATUS AND KEYPAD HANDLER
***********************************************************************************************************/
// ALARM STATUS CHANGE FEEDBACK TO SPEAKERS
def alarmStatusHandler(evt) {
	if (fSecFeed) {
	def curEvtValue = evt.value
	log.info "Smart Home Monitor status changed to: ${curEvtValue}"
		if (shmSynthDevice || shmSonosDevice) {
			if (evt.value == "away") {
            	sendAwayCommand
            	if(shmSynthDevice) shmSynthDevice?.speak("Attention, The alarm system has changed status to armed away")
            	if (shmSonosDevice) 
             	shmSonosDevice?.playTextAndRestore("Attention, The alarm system has changed status to armed away")
            	}
                else if (evt.value == "stay") {
                	if(shmSynthDevice) shmSynthDevice?.speak("Attention, The alarm system has changed status to armed home'")
            		if (shmSonosDevice) 
             		shmSonosDevice?.playTextAndRestore("Attention, The alarm system has changed status to armed home")
            		}
                    else if(evt.value == "off") {
                    	if(shmSynthDevice) shmSynthDevice?.speak("Attention, The alarm system has changed status to disarmed")
            			if (shmSonosDevice) 
             			shmSonosDevice?.playTextAndRestore("Attention, The alarm system has changed status to disarmed")
            			}
					}
       			}
			}
/*^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
X 																											X
X                       					PRIVATE FUNCTIONS												X
X                        																					X
/*^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
/***********************************************************************************************************************
    RESTRICTIONS HANDLER
***********************************************************************************************************************/
private getAllOk() {
	modeOk && daysOk && timeOk
}
private getModeOk() {
    def result = !modes || modes?.contains(location.mode)
	if(parent.debug) log.debug "modeOk = $result"
    result
} 
private getDayOk() {
    def result = true
if (days) {
		def df = new java.text.SimpleDateFormat("EEEE")
		if (location.timeZone) {
			df.setTimeZone(location.timeZone)
		}
		else {
			df.setTimeZone(TimeZone.getTimeZone("America/New_York"))
		}
		def day = df.format(new Date())
		result = days.contains(day)
	}
	if(parent.debug) log.debug "daysOk = $result"
	result
}
private getTimeOk() {
	def result = true
	if ((starting && ending) ||
	(starting && endingX in ["Sunrise", "Sunset"]) ||
	(startingX in ["Sunrise", "Sunset"] && ending) ||
	(startingX in ["Sunrise", "Sunset"] && endingX in ["Sunrise", "Sunset"])) {
		def currTime = now()
		def start = null
		def stop = null
		def s = getSunriseAndSunset(zipCode: zipCode, sunriseOffset: startSunriseOffset, sunsetOffset: startSunsetOffset)
		if(startingX == "Sunrise") start = s.sunrise.time
		else if(startingX == "Sunset") start = s.sunset.time
		else if(starting) start = timeToday(starting,location.timeZone).time
		s = getSunriseAndSunset(zipCode: zipCode, sunriseOffset: endSunriseOffset, sunsetOffset: endSunsetOffset)
		if(endingX == "Sunrise") stop = s.sunrise.time
		else if(endingX == "Sunset") stop = s.sunset.time
		else if(ending) stop = timeToday(ending,location.timeZone).time
		result = start < stop ? currTime >= start && currTime <= stop : currTime <= stop || currTime >= start
	if (parent.debug) log.trace "getTimeOk = $result."
    }
    return result
}
private hhmm(time, fmt = "h:mm a") {
	def t = timeToday(time, location.timeZone)
	def f = new java.text.SimpleDateFormat(fmt)
	f.setTimeZone(location.timeZone ?: timeZone(time))
	f.format(t)
}
private offset(value) {
	def result = value ? ((value > 0 ? "+" : "") + value + " min") : ""
}
private timeIntervalLabel() {
	def result = ""
	if      (startingX == "Sunrise" && endingX == "Sunrise") result = "Sunrise" + offset(startSunriseOffset) + " to Sunrise" + offset(endSunriseOffset)
	else if (startingX == "Sunrise" && endingX == "Sunset") result = "Sunrise" + offset(startSunriseOffset) + " to Sunset" + offset(endSunsetOffset)
	else if (startingX == "Sunset" && endingX == "Sunrise") result = "Sunset" + offset(startSunsetOffset) + " to Sunrise" + offset(endSunriseOffset)
	else if (startingX == "Sunset" && endingX == "Sunset") result = "Sunset" + offset(startSunsetOffset) + " to Sunset" + offset(endSunsetOffset)
	else if (startingX == "Sunrise" && ending) result = "Sunrise" + offset(startSunriseOffset) + " to " + hhmm(ending, "h:mm a z")
	else if (startingX == "Sunset" && ending) result = "Sunset" + offset(startSunsetOffset) + " to " + hhmm(ending, "h:mm a z")
	else if (starting && endingX == "Sunrise") result = hhmm(starting) + " to Sunrise" + offset(endSunriseOffset)
	else if (starting && endingX == "Sunset") result = hhmm(starting) + " to Sunset" + offset(endSunsetOffset)
	else if (starting && ending) result = hhmm(starting) + " to " + hhmm(ending, "h:mm a z")
}



/******************************************************************************
	 FEEDBACK SUPPORT - GET AVERAGE										
******************************************************************************/
def getAverage(device,type){
	def total = 0
		if(debug) log.debug "calculating average temperature"  
    device.each {total += it.latestValue(type)}
    return Math.round(total/device?.size())
}
/******************************************************************************
	 FEEDBACK SUPPORT - ADDITIONAL FEEDBACK	
     
                        data.deviceTypeDoors = "cDoor1"
                        data.deviceTypeWindows = "cWindow"
                        data.deviceDoors = devListDoors
                        data.deviceWindows = devListWindows
                        state.lastAction = data
                        state.pContCmdsR = "feedback"
    
******************************************************************************/
def getMoreFeedback(data) {
    def devices = data.devices
    def deviceType = data.deviceType
    def deviceDoors = data.deviceDoors
    def deviceTypeDoors = data.deviceTypeDoors
    def deviceWindows = data.deviceWindows
    def deviceTypeWindows = data.deviceTypeWindows
    def command = data.cmd
    def outputTxt = ""
	if ( deviceType == "cSwitch") {
    	outputTxt = "The following lights are " + command + "," + devices.sort().unique()
    }
	if ( deviceType == "cDoor1") {    // Added by Jason to ask "are doors open" on 2/27/2017
    	if (devices?.size() == 1) {
    	outputTxt = "The following door, " + devices + " is " + command 
        }
        else if (devices?.size() > 1) { 
        outputTxt = "The following doors are " + command + "," + devices.sort().unique()
    	}
    }
	if (deviceType == "cWindow") {    // Added by Jason to ask "are doors open" on 2/27/2017
    	if (devices?.size() == 1) {
    	outputTxt = " The following window, " + devices + " is " + command 
    	}
        else { 
        outputTxt = "The following windows are " + command + "," + devices.sort().unique()
    	}
    }
    if ( deviceTypeDoors == "cDoor1" && deviceTypeWindows == "cWindow") {
    	if (data.deviceTypeWindows?.size() == 0 && data.deviceTypeDoors?.size() == 1) {
        	outputTxt = "The following door is " + command + "," + deviceTypeDoors.sort().unique()
            }
    	if (data.deviceTypeWindows?.size() == 1 && data.deviceDoors?.size() == 0) {
            outputTxt = "The following window is " + command + "," + deviceTypeWindows.sort().unique()
            }
        if (data.deviceTypeWindows?.size() == 0 && data.deviceDoors?.size() > 0) {
        	outputTxt = "The following doors are " + command + "," + deviceTypeDoors.sort().unique()
            }
    	else if (data.deviceTypeWindows?.size() > 1 && data.deviceDoors?.size() == 0) { 
            outputTxt = "The following windows are " + command + "," + deviceTypeWindows.sort().unique()
            }
        else if (data.deviceTypeWindows?.size() > 0 && data.deviceDoors?.size() > 0) {    
            outputTxt = "The following doors are " + command + "," + deviceDoors.sort().unique() + " , and the following windows are " + command + "," + deviceWindows.sort().unique() 
   			}
    state.pContCmdsR = null 
	state.lastAction = null
	}
//    return ["outputTxt":outputTxt, "pContCmds":state.pContCmds, "pShort":state.pShort, "pContCmdsR":state.pContCmdsR, "pTryAgain":state.pTryAgain, "pPIN":pPIN]	

	return outputTxt  //":outputTxt, "pContCmds":state.pContCmds, "pShort":state.pShort, "pContCmdsR":state.pContCmdsR, "pTryAgain":state.pTryAgain, "pPIN":pPIN	
}    
/******************************************************************************
	 FEEDBACK SUPPORT - DEVICE MATCH											
******************************************************************************/
private deviceMatchHandler(fDevice) {
    def pPIN = false
    def String deviceType = (String) null
	def currState
    def stateDate
    def stateTime
	def deviceMatch
    def result
    	state.pTryAgain = false
		if(cTstat){
           deviceMatch = cTstat?.find {d -> d.label.toLowerCase() == fDevice?.toLowerCase()}
			if(deviceMatch){
				deviceType = "cTstat"
                currState = deviceMatch.currentState("thermostatOperatingState").value
                stateDate = deviceMatch.currentState("thermostatOperatingState").date
                stateTime = deviceMatch.currentState("thermostatOperatingState").date.time
                def timeText = getTimeVariable(stateTime, deviceType)            
            	return ["deviceMatch" : deviceMatch, "deviceType": deviceType, "currState": currState, "tText": timeText.tText, "mainCap": "thermostatOperatingState" ]
            }
        }
        if (cSwitch){
		deviceMatch = cSwitch?.find {d -> d.label.toLowerCase() == fDevice?.toLowerCase()}
			if(deviceMatch){
				deviceType = "cSwitch" 
				currState = deviceMatch.currentState("switch").value
				stateDate = deviceMatch.currentState("switch").date
				stateTime = deviceMatch.currentState("switch").date.time
				def timeText = getTimeVariable(stateTime, deviceType)
            	return ["deviceMatch" : deviceMatch, "deviceType": deviceType, "currState": currState, "tText": timeText.tText, "mainCap": "switch"]
        	}
        }
        if (cContact){
        deviceMatch =cContact?.find {d -> d.label.toLowerCase() == fDevice?.toLowerCase()}
            if(deviceMatch)	{
                deviceType = "cContact" 
				currState = deviceMatch.currentState("contact").value
				stateDate = deviceMatch.currentState("contact").date
				stateTime = deviceMatch.currentState("contact").date.time
				def timeText = getTimeVariable(stateTime, deviceType)
                return ["deviceMatch" : deviceMatch, "deviceType": deviceType, "currState": currState, "tText": timeText.tText, "mainCap": "contact"]
            }
        }
        if (cMotion){
        deviceMatch =cMotion?.find {d -> d.label.toLowerCase() == fDevice?.toLowerCase()}
            if(deviceMatch)	{
                deviceType = "cMotion" 
                currState = deviceMatch.currentState("motion").value 
                stateDate = deviceMatch.currentState("motion").date
                stateTime = deviceMatch.currentState("motion").date.time
                def timeText = getTimeVariable(stateTime, deviceType)
                return ["deviceMatch" : deviceMatch, "deviceType": deviceType, "currState": currState, "tText": timeText.tText, "mainCap": "motion"]
        	}
        } 
        if (cLock){
        deviceMatch =cLock?.find {d -> d.label.toLowerCase() == fDevice?.toLowerCase()}
            if(deviceMatch)	{
                deviceType = "cLock"
                currState = deviceMatch.currentState("lock").value 
                stateDate = deviceMatch.currentState("lock").date
                stateTime = deviceMatch.currentState("lock").date.time
                def timeText = getTimeVariable(stateTime, deviceType)
                return ["deviceMatch" : deviceMatch, "deviceType": deviceType, "currState": currState, "tText": timeText.tText, "mainCap": "lock"]
        	}
        }        
        if (cPresence){
        deviceMatch =cPresence?.find {d -> d.label.toLowerCase() == fDevice?.toLowerCase()}
            if(deviceMatch)	{
                deviceType = "cPresence"
                currState = deviceMatch.currentState("presence")?.value 
                stateDate = deviceMatch.currentState("presence")?.date
                stateTime = deviceMatch.currentState("presence")?.date.time
                def timeText = getTimeVariable(stateTime, deviceType)
                return ["deviceMatch" : deviceMatch, "deviceType": deviceType, "currState": currState, "tText": timeText.tText, , "mainCap": "presence"]
        	}
        }  
        if (cDoor){
        deviceMatch =cDoor?.find {d -> d.label.toLowerCase() == fDevice?.toLowerCase()}
            if(deviceMatch)	{
                deviceType = "cDoor"
                currState = deviceMatch.currentState("door").value 
                stateDate = deviceMatch.currentState("door").date
                stateTime = deviceMatch.currentState("door").date.time
                def timeText = getTimeVariable(stateTime, deviceType)
                return ["deviceMatch" : deviceMatch, "deviceType": deviceType, "currState": currState, "tText": timeText.tText, "mainCap": "door"]
        	}
        }  
        if (cVent){
		deviceMatch =cVent?.find {d -> d.label.toLowerCase() == fDevice?.toLowerCase()}
            if(deviceMatch)	{
                deviceType = "cVent"
                currState = deviceMatch.currentState("switch").value 
                currState = currState == "on" ? "open" : currState == "off" ? "closed" : "unknown"
                stateDate = deviceMatch.currentState("switch").date
                stateTime = deviceMatch.currentState("switch").date.time
                def timeText = getTimeVariable(stateTime, deviceType)
                return ["deviceMatch" : deviceMatch, "deviceType": deviceType, "currState": currState, "tText": timeText.tText, , "mainCap": "switch"]
        	}
        }
        if (cWater){
		deviceMatch =cWater?.find {d -> d.label.toLowerCase() == fDevice?.toLowerCase()}
            if(deviceMatch)	{
                deviceType = "cWater"
                currState = deviceMatch.currentState("water").value 
                stateDate = deviceMatch.currentState("water").date
                stateTime = deviceMatch.currentState("water").date.time
                def timeText = getTimeVariable(stateTime, deviceType)
                return ["deviceMatch" : deviceMatch, "deviceType": deviceType, "currState": currState, "tText": timeText.tText,  "mainCap": "water"]
        	}
        }        
        if (cMedia){
            if (fDevice == "TV") {
                deviceMatch = cMedia.first()
            }
            else {
                deviceMatch =cMedia?.find {d -> d.label.toLowerCase() == fDevice?.toLowerCase()}
            }   
            if(deviceMatch)	{
                deviceType = "cMedia"
                currState = deviceMatch.currentState("currentActivity").value 
                currState = currState == "--" ? " off " : " running the " + currState + " activity "
                stateDate = deviceMatch.currentState("currentActivity").date
                stateTime = deviceMatch.currentState("currentActivity").date.time
                def timeText = getTimeVariable(stateTime, deviceType)
                return ["deviceMatch" : deviceMatch, "deviceType": deviceType, "currState": currState, "tText": timeText.tText,  "mainCap": "currentActivity"]
            }
        }        
        if (cFan){
		deviceMatch =cFan?.find {d -> d.label.toLowerCase() == fDevice?.toLowerCase()}
            if(deviceMatch)	{
                deviceType = "cFan"
                currState = deviceMatch.currentState("switch").value 
                stateDate = deviceMatch.currentState("switch").date
                stateTime = deviceMatch.currentState("switch").date.time
                def timeText = getTimeVariable(stateTime, deviceType)
                return ["deviceMatch" : deviceMatch, "deviceType": deviceType, "currState": currState, "tText": timeText.tText, "mainCap": "switch"] 
            }
        }         
        if (cRelay){
		deviceMatch =cRelay?.find {d -> d.label.toLowerCase() == fDevice?.toLowerCase()}
            if(deviceMatch)	{
				deviceType == "cRelay"
                if (cContactRelay) {
                currState = cContactRelay.currentState("contact").value 
                stateDate = cContactRelay.currentState("contact").date
                stateTime = cContactRelay.currentState("contact").date.time
                def timeText = getTimeVariable(stateTime, deviceType)
                return ["deviceMatch" : deviceMatch, "deviceType": deviceType, "currState": currState, "tText": timeText.tText,  "mainCap": "contact"] 
                }
			}
        }
        if (cBattery){
        deviceMatch =cBattery?.find {d -> d.label.toLowerCase() == fDevice?.toLowerCase()}
            if(deviceMatch)	{
                deviceType = "cBattery"
                currState = cBattery.currentState("battery").value
				stateTime = cBattery.currentState("battery").date.time
                def timeText = getTimeVariable(stateTime, deviceType)  
                return ["deviceMatch" : deviceMatch, "deviceType": deviceType, "currState": "", "tText": timeText.tText,  "mainCap": "battery"]
            } 
     	}
}
/******************************************************************************
	 FEEDBACK SUPPORT - DEVICE CAPABILITIES											
******************************************************************************/
private getCaps(capDevice,capType, capMainCap, capState) {
    def deviceName = capDevice
    def deviceType = capType
    def deviceCap = capMainCap
    def deviceState = capState
    def result
    def attr = [:]
    	state.pContCmdsR = "caps"
	def supportedCaps = deviceName.capabilities
    supportedCaps.each { c ->
		def capName = c.name
            c.attributes.each {a ->
        		def attrName = a.name
                 def attrValue = deviceName.latestValue(attrName)               
                 if (a.name != null && a.name !=checkInterval && a.name !=polling  && a.name !=refresh && attrValue != null ) {
                    if (a.name == "temperature") 		{ result = "The " + attrName + " is " + attrValue + " degrees, " }
                    if (a.name == "motion") 			{ result = result + attrName + " is " + attrValue +", " }
                    if (a.name == "contact") 			{ result = result + attrName + " is " + attrValue +", " }                    
                    if (a.name == "humidity") 			{ result = result + attrName + " is " + attrValue + ", " }
                    if (a.name == "illuminance") 		{ result = result + "lux level is " + attrValue + ", " }
                    if (a.name == "water") 				{ result = result + attrName + " is " + attrValue +", " }                    
                    if (a.name == "switch") 			{ result = result + attrName + " is " + attrValue +", " } 
					if (a.name == "presence") 			{ result = result + attrName + " is " + attrValue + ", " }                    
                    if (a.name == "heatingSetpoint") 	{ result = result + "Heating Set Point is " + attrValue + " degrees, " }
                    if (a.name == "coolingSetpoint") 	{ result = result + "Cooling Set Point is" + attrValue + " degrees, " }
                    if (a.name == "thermostatMode") 	{ result = result + "The thermostat Mode is " + attrValue + ", " }
                    if (a.name == "thermostatFanMode") 	{ result = result + "The Fan Mode is " + attrValue + ", " }
					if (a.name == "battery") 			{ result = result + attrName + " level is " + attrValue + " percent, " }
                        attr << ["${attrName}": attrValue]
            	}
           }
     }
	result = result.replace("null", "")
    state.lastAction = result
    state.pContCmdsR = "caps"
    result = attr?.size()
    return result
}
/******************************************************************************
	 FEEDBACK SUPPORT - CAPABILITIES GROUP											
****************************************************************************/
private getCapabilities(cap) {
    def DeviceDetails = [] 
    def batDetails = [] 
    def result = [:] 
    	state.pTryAgain = false	
//try {
//batteries
	if (cap == "bat") {
        cMotion?.each 	{ d ->
        def attrValue = d.latestValue("battery") 
        	if (attrValue < cLowBattery) {
        		batDetails << d.displayName         
             }     
         }
        cContact?.each 	{ d ->
        def attrValue = d.latestValue("battery") 
        	if (attrValue < cLowBattery) {
        		batDetails << d.displayName         
             }
         }
         
        cPresence?.each 	{ d ->
        def attrValue = d.latestValue("battery") 
        	if (attrValue < cLowBattery) {
        		batDetails << d.displayName         
             }
         }
        cWater?.each 	{ d ->
        def attrValue = d.latestValue("battery") 
        	if (attrValue < cLowBattery) {
        		batDetails << d.displayName         
             }
         }
        cVent?.each 	{ d ->
        def attrValue = d.latestValue("battery") 
        	if (attrValue < cLowBattery) {
        		batDetails << d.displayName        
             }
         }
        cLock.each 	{ d ->
        def attrValue = d.latestValue("battery") 
        	if (attrValue < cLowBattery) {
        		batDetails << d.displayName         
             }
         }    
        cBattery?.each 	{ d ->
        def attrValue = d.latestValue("battery") 
        	if (attrValue < cLowBattery) {
        		batDetails << d.displayName     
             }
         }
        def dUniqueList = batDetails.unique (false)
        dUniqueList = dUniqueList.sort()       
        def listSize = dUniqueList?.size()
        def listBat = dUniqueList
        result = [listSize: listSize, listBat: listBat]
        return result //dUniqueListString
	}
	 
    /*catch (Throwable t) {
        log.error t
        result = "Oh no, something went wrong. If this happens again, please reach out for help!"
        state.pTryAgain = true
        return result
	}
}	*/    
//activity	
//try{
    if (cap == "act") {    
        cMotion?.each 	{ d ->
        	def stateTime = d.currentState("motion").date.time
			def endTime = now() + location.timeZone.rawOffset
    		def startTimeAdj = new Date(stateTime + location.timeZone.rawOffset)
    			startTimeAdj = startTimeAdj.getTime()
    		int hours = (int)((endTime - startTimeAdj) / (1000 * 60 * 60) )
    		//int minutes = (int)((endTime - startTime) / ( 60 * 1000))
                if ( hours > cInactiveDev ) {
                    DeviceDetails << d.displayName 
                }
        }
        cContact?.each 	{ d ->
        def attrValue = d.latestValue("contact") 
        	def stateTime = d.currentState("contact").date.time
			def endTime = now() + location.timeZone.rawOffset
    		def startTimeAdj = new Date(stateTime + location.timeZone.rawOffset)
    			startTimeAdj = startTimeAdj.getTime()
    		int hours = (int)((endTime - startTimeAdj) / (1000 * 60 * 60) )
    		//int minutes = (int)((endTime - startTime) / ( 60 * 1000))
                if ( hours > cInactiveDev ) {
                    DeviceDetails << d.displayName 
                }
        }
        cWindow?.each 	{ d ->
        def attrValue = d.latestValue("contact") 
        	def stateTime = d.currentState("contact").date.time
			def endTime = now() + location.timeZone.rawOffset
    		def startTimeAdj = new Date(stateTime + location.timeZone.rawOffset)
    			startTimeAdj = startTimeAdj.getTime()
    		int hours = (int)((endTime - startTimeAdj) / (1000 * 60 * 60) )
    		//int minutes = (int)((endTime - startTime) / ( 60 * 1000))
                if ( hours > cInactiveDev ) {
                    DeviceDetails << d.displayName 
                }
        }
        cDoor1?.each 	{ d ->
        def attrValue = d.latestValue("contact") 
        	def stateTime = d.currentState("contact").date.time
			def endTime = now() + location.timeZone.rawOffset
    		def startTimeAdj = new Date(stateTime + location.timeZone.rawOffset)
    			startTimeAdj = startTimeAdj.getTime()
    		int hours = (int)((endTime - startTimeAdj) / (1000 * 60 * 60) )
    		//int minutes = (int)((endTime - startTime) / ( 60 * 1000))
                if ( hours > cInactiveDev ) {
                    DeviceDetails << d.displayName 
                }
        }
        cPresence?.each 	{ d ->
        def attrValue = d.latestValue("presence") 
        	def stateTime = d.currentState("presence").date.time
			def endTime = now() + location.timeZone.rawOffset
    		def startTimeAdj = new Date(stateTime + location.timeZone.rawOffset)
    			startTimeAdj = startTimeAdj.getTime()
    		int hours = (int)((endTime - startTimeAdj) / (1000 * 60 * 60) )
    		//int minutes = (int)((endTime - startTime) / ( 60 * 1000))
                if ( hours > cInactiveDev ) {
                    DeviceDetails << d.displayName 
                }
        }
        cWater?.each 	{ d ->
        def attrValue = d.latestValue("water") 
        	def stateTime = d.currentState("water").date.time
			def endTime = now() + location.timeZone.rawOffset
    		def startTimeAdj = new Date(stateTime + location.timeZone.rawOffset)
    			startTimeAdj = startTimeAdj.getTime()
    		int hours = (int)((endTime - startTimeAdj) / (1000 * 60 * 60) )
    		//int minutes = (int)((endTime - startTime) / ( 60 * 1000))
                if ( hours > cInactiveDev ) {
                    DeviceDetails << d.displayName 
                }
        }
        cVent?.each 	{ d ->
        def attrValue = d.latestValue("switch") 
        	def stateTime = d.currentState("switch").date.time
			def endTime = now() + location.timeZone.rawOffset
    		def startTimeAdj = new Date(stateTime + location.timeZone.rawOffset)
    			startTimeAdj = startTimeAdj.getTime()
    		int hours = (int)((endTime - startTimeAdj) / (1000 * 60 * 60) )
    		//int minutes = (int)((endTime - startTimeAdj) / ( 60 * 1000))
                if ( hours > cInactiveDev ) {
                    DeviceDetails << d.displayName 
                }
        }
        cLock?.each 	{ d ->
        def attrValue = d.latestValue("lock") 
			def stateTime = d.currentState("lock").date.time
            def endTime = now() + location.timeZone.rawOffset
            def startTimeAdj = new Date(stateTime + location.timeZone.rawOffset)
    			startTimeAdj = startTimeAdj.getTime()
            int hours = (int)((endTime - startTimeAdj) / (1000 * 60 * 60) )
            //int minutes = (int)((endTime - startTime) / ( 60 * 1000))
                if ( hours > cInactiveDev ) {
                    DeviceDetails << d.displayName 
                }
        }
         log.warn "locks devices = $DeviceDetails"
        cSwitch?.each 	{ d ->
        	def attrValue = d.latestValue("switch") 
            if (d?.currentState("switch") != null) {
            def stateTime = d?.currentState("switch").date.time
			def endTime = now() + location.timeZone.rawOffset
    		def startTimeAdj = new Date(stateTime + location.timeZone.rawOffset)
    			startTimeAdj = startTimeAdj.getTime()
    		int hours = (int)((endTime - startTimeAdj) / (1000 * 60 * 60) )
    		//int minutes = (int)((endTime - startTime) / ( 60 * 1000))
                if ( hours > cInactiveDev ) {
                    DeviceDetails << d.displayName 
                }
           	}
        } 
         log.warn "switch devices = $DeviceDetails"
        def dUniqueList = DeviceDetails.unique (false)
        dUniqueList = dUniqueList.sort()       
        def listSize = dUniqueList?.size()
        def listDev = dUniqueList
        result = [listSize: listSize, listDev: listDev]
        return result //dUniqueListString
	}
		}
        /*catch(Exception ex) {
         log.error "exception: $ex"
		 result = "Looks like you might have an improper built device type that is missing a standard filed."
      	}
		catch (Throwable t) {
        log.error t
        result = "Looks like you might have an improper built device type that is missing a standard filed."
        state.pTryAgain = true
        return result
		}
}	*/
/************************************************************************************************************
	CONTROL SUPPORT - PIN HANDLER
************************************************************************************************************/ 
private pinHandler(pin, command, num, unit) {
	def result
        def String pinNum = (String) null
		pinNum = num	
    if (command == "validation") {
		state.savedPINdata = state.lastAction
        state.lastAction = null
		result = "Pin number please"
		state.pinTry = 0
		if (debug) log.warn "PIN response pending - '${state.pinTry}'"  
        return result
	}        
    if (pin == cPIN || command == cPIN || pinNum == cPIN || unit == cPIN ) {
		def data = state.savedPINdata != null ? state.savedPINdata : lastAction
        state.pTryAgain = false
        //state.pinTry = null /// 2/8/2017
            if (data == "disablelocks" || data == "disablethermostats" || data == "disabledoors" || data == "disableswitches" || data == "disablesecurity" || data == "disablemodes"){ 
                if 		(data == "disablelocks")		{state.usePIN_L = false}
                else if (data == "disablethermostats") 	{state.usePIN_T = false}
                else if (data == "disabledoors") 		{state.usePIN_D = false}  
                else if (data == "disableswitches") 	{state.usePIN_S = false} 
                else if (data == "disablesecurity") 	{state.usePIN_SHM = false}
                else if (data == "disablemodes") 		{state.usePIN_Mode = false} 
                state.pinTry = null /// 2/8/2017
                result = "Ok, pin number for " + data.replace("disable", "") + " has been disabled.  To activate it again, just say enable the PIN number for " + data.replace("disable", "")   
            	return result
            }
            else {
				def pNum = data.num
        		def pUnit = data.unit           
                if(state.pContCmdsR == "security"){
                    result = securityHandler(data)
                    state.pinTry = null /// 2/8/2017
                    return result
                }
                if(state.pContCmdsR == "mode"){
                	def cmd = state.savedPINdata.command
                	location.setMode(cmd)
                	result = "I changed your location mode to " + cmd
                	state.pinTry = null /// 2/8/2017
                    return result
                }
                if(state.pContCmdsR == "routine"){ 
                	def cmd = state.savedPINdata.command
                	location.helloHome.execute(cmd)
                	result = "I executed your routine, " + cmd
                	state.pinTry = null /// 2/8/2017
                    return result
                }
                else {
                	if (state.pContCmdsR == "cMiscDev" && pNum > 0 && pUnit == "minutes") {
                		runIn(pNum*60, controlHandler, [data: data])
                        def getTxt = getUnitText(pUnit, pNum)     
        				def numText = getTxt.text
                        if (data.command == "on" || data.command == "off" ) {result = "Ok, turning " + data.device + " " + data.command + ", in " + numText}
						else if (data.command == "decrease") {result = "Ok, decreasing the " + data.device + " level in " + numText}
						else if (data.command == "increase") {result = "Ok, increasing the " + data.device + " level in " + numText}                        
                    	state.pContCmdsR = "undefined"
                        state.savedPINdata = null
                        state.pinTry = null /// 2/8/2017
                        return result
                	}
                	else {
                	result = controlHandler(data)
                    state.pinTry = null /// 2/8/2017
                    return result
                	}
              	}
            }
            //state.pinTry = null /// 2/8/2017
            state.savedPINdata = null
            state.pContCmdsR = "undefined"
            return result
	}
	else {
		state.pinTry = state.pinTry + 1
			if (state.pinTry < 4){
				result = "I'm sorry, that is incorrect "
				if (debug) log.debug "PIN NOT Matched! PIN = '${cPIN}', ctPIN= '${ctPIN}', ctNum= '${num}', ctCommand ='${command}', try# ='${state.pinTry}'"
				state.pTryAgain = true
                return result
			}
			else { 
				state.pinTry = null
                state.savedPINdata = null
                state.pTryAgain = false
				result = "I'm sorry, that is incorrect. Please check your pin number and try again later"
                return result
			}
	} 
}
/************************************************************************************************************
	CONTROL SUPPORT - UNIT CONVERSIONS
************************************************************************************************************/ 
private getUnitText (unit, num) {     
    def String text = (String) unit
    def String nUnit = (String) num
    if (unit == "minute" || unit == "minutes" || unit.contains ("minutes") || unit.contains ("minute")) {
    	nUnit = "minutes"
        text = num == 1 ? num + " minute" : num + " minutes" 
        return ["text": text, "unit": nUnit]
    } 
	if (unit == "degrees"  || unit.contains ("degrees")) {
		nUnit = "degrees"
        int tNum = num
        text = tNum + " degrees"
        return ["text":text, "unit":nUnit]
    }             
	if (unit == "percent" || unit.contains ("percent")) {
		nUnit = "percent"
		text = num + " percent" 
        return ["text":text, "unit":nUnit]
    }
		return ["text":text, "unit":nUnit]

}   
/***********************************************************************************************************************
    CONTROL SUPPORT - COMMANDS HANDLER
***********************************************************************************************************************/
private getCommand(command, unit) {
	def deviceType = " "
	if (command && unit) {
	//case "General Commands":
    		deviceType = "general"
        if (unit == "undefined") {
            if (command == "decrease" || command == "down") {
                command = "decrease" 
                deviceType = "general"
            }
            if (command == "increase" || command == "up") {
                command = "increase"
                deviceType = "general"
            }
            if (command == "set" || command == "set level"){
                command = "setLevel"
                deviceType = "general"
            }
            if (command == "cut on"){
            	command = "cut on"
                deviceType = "general"
            }
            if (command == "cut off"){
            	command = "cut off"
                deviceType = "general"
            }    
       }     
   //case "Virtual Person     
            if (command == "check in" || command == "checking in"|| command == "checked in" || command == "arrived" || command == "arriving"){
            	command = "arrived"
                deviceType = "cPresence" //"virtualPerson"
            }
            if (command == "check out" || command == "checking out"|| command == "checked out"){
            	command = "departed"
                deviceType = "cPresence"
            }    
	//case "Color Commands": 
    		if (command == "reading" || command == "read" || command == "studying"){ 
            	command = "read" 
                deviceType = "color"
            }
			if (command == "feeling lucky" || command.contains("random") || command == "different colors" || unit == "random"){ 
            	command = "random" 
                deviceType = "color"
            }
    		if (command == "cleaning" || command == "working" || command == "concentrating" || command == "concentrate" || command == "cooking"){ 
    			command = "concentrate"
                deviceType = "color"
           	}
    		if (command == "relax" || command == "relaxing" || command == "chilling" || command == "watching"){
            	command = "relax"
                deviceType = "color"
            }
			if (unit.contains("green") || unit.contains("blue") || unit.contains("red") || unit == "yellow" || unit == "pink" || unit == "orange" || unit.contains("white") || unit == "daylight"){
                //command = unit
				command = unit.replace("color", "")
                log.warn "command = ${command}"
                //command = unit.contains(fillColorSettings().name) ? fillColorSettings().name : unit
                deviceType = "color"
            }
            if (unit.contains("loop")){
                if (command == "start" || command == "play" || command == "on"){
                    command = "colorloopOn"
                    deviceType = "color"
                } 
                if (command == "stop" || command == "cancel" || command == "off"){
                    command = "colorloopOff"
                    deviceType = "color"
                }
           }
	//case "Temperature Commands":
    	if (command == "off") {
        	if (unit =="heat" || unit =="AC" || unit =="cooling" || unit =="heating") {
            command = "off"
            deviceType = "temp"
        	}
        }
        if (command == "on") {
        	if (unit =="heat" || unit =="AC" || unit =="cooling" || unit =="heating") {
            command = "on"
            deviceType = "temp"
        	}
        }
        if (command == "colder" || command =="not cold enough" || command =="too hot" || command == "too warm") {
            command = "decrease"
            deviceType = "temp"
        }     
        else if (command == "freezing" || command =="not hot enough" || command == "too chilly" || command == "too cold" || command == "warmer") {
            command = "increase"
            deviceType = "temp"
        }
        else if (unit == "degrees" || unit =="heat" || unit =="AC" || unit =="cooling" || unit =="heating") {
            if (command == "up") {
           		command = "increase"
        	}
            else if (command == "down") {
            	command = "decrease"
            }
            deviceType = "temp"
        }
        else if (unit=="degrees" || unit.contains("degrees") ||  unit.contains("heat") ||  unit.contains("AC")) {
           deviceType = "temp"    
        }       
    //case "Dimmer Commands":
        if (command == "darker" || command == "too bright" || command == "dim" || command == "dimmer") {
            command = "decrease" 
            deviceType = "light"
        }
        else if  (command == "not bright enough" || command == "brighter" || command == "too dark" || command == "brighten") {
            command = "increase" 
            deviceType = "light"     
        } 
        else if (unit == "percent") {
        	deviceType = "light"
        }
    //case "Volume Commands":
        if  (command == "mute" || command == "quiet" || command == "unmute" ) {
            deviceType = "volume" 
        }
        else if  (command == "too loud" || command == "down" ) {
            command = "decrease"
            deviceType = "volume" 
        }
        else if  (command == "not loud enough" || command == "too quiet" || command == "up") {
            command = "increase"
            deviceType = "volume"
        }
    //case "Fan Control Commands":
        if  (command == "slow down" || command == "too fast" ) {
            command = "decrease"
            deviceType = "fan" 
        }
        else if  (command == "speed up" || command == "too slow") {
            command = "increase"
            deviceType = "fan" 
        }
		else if (command == "high" || command == "medium"|| command == "low") {
			deviceType = "fan"                  
		}
	//case "Other Commands":           
        if (command == "lock" || command == "unlock") {
			deviceType = "lock"                  
		}
        else if (command == "open" || command == "close") {
			deviceType = "door"                  
		}
    }
    return ["deviceType":deviceType, "command":command ]                          
}
/************************************************************************************************************
	CONTROL SUPPORT - CUSTOM CONTROL COMMANDS
************************************************************************************************************/ 
private getCustomCmd(command, unit, group, num) {
    def result
    if (command == "repeat") {
		result = getLastMessageMain()
		return result
    }
	if (command == "change" || command == "changed" || command == "replace" || command == "replaced") {
		if (unit=="filters") {
        result = scheduleHandler(unit)
      	}
		return result
    } 
    if (command == "cancel" || command == "stop" || command == "disable" || command == "deactivate" || command == "unschedule" || command == "disarm") {
    	if(group == "security"){
        	def param = [:]
        		param.command = command
        		param.num = num
				param.pintentName = group
        		result = controlSecurity(param)
                log.warn "security result = ${result}"
                return result.outputTxt
        }
        if (unit == "reminder" || unit == "reminders" || unit == "timer" || unit == "timers" || unit.contains ("reminder") || unit.contains ("timer") || unit.contains ("schedule") ) {
        	if (unit.contains ("reminder") || unit.contains ("schedule")) {
            	if (state.scheduledHandler != null) {
                	if (state.scheduledHandler == "filters") {
                    	unschedule("filtersHandler")
                        state.scheduledHandler = null
		                result = "Ok, canceling reminder to replace HVAC filters"
                    }
                    else {
                    state.pTryAgain = true
                    result = "Sorry, I couldn't find any scheduled reminders"// for " + state.scheduledHandler
                    }
                    return result
            	}
				else {
                	state.pTryAgain = true
					result = "Sorry, I couldn't find any scheduled reminders"// for " + state.scheduledHandler
				}
				return result
            }
            else {
                if (unit.contains ("timer") || unit.contains ("delay")) {
                    unschedule("controlHandler")
                    unschedule("securityHandler")
                    result = "Ok, canceling timer"
                    return result
                }
            }
        }
		if (unit == "conversation" || unit.contains ("conversation")) {
			state.pContCmds = false
            result = "Ok, disabling conversational features. To activate just say, start the conversation"
			return result
        }
		if (unit == "pin number" || unit == "pin") {
			if (state.usePIN_T == true || state.usePIN_D == true || state.usePIN_L == true || state.usePIN_S == true || state.usePIN_SHM == true || state.usePIN_Mode == true) {
			state.lastAction = "disable" + group
			command = "validation"
			num = 0
			def secUnit = group
			def process = false
				if (state.usePIN_T == true && group == "thermostats") 		{process = true}
				else if (state.usePIN_L == true && group == "locks") 		{process = true}
                else if (state.usePIN_D == true && group == "doors") 		{process = true}
                else if (state.usePIN_S == true && group == "switches") 	{process = true}                              
                else if (state.usePIN_SHM == true && group == "security") 	{process = true} 
                else if (state.usePIN_Mode == true && group == "modes") 	{process = true} 
				if(process == true) {
                		result = pinHandler(pin, command, num, secUnit)
                		return result
                    }
                    else {
                    	result = "The pin number for " + group + " is not active"
                        return result
                    }
            }
            else{
            	result = "The pin number for " + group + " is not enabled"
				return result
			}         
		}
        if (unit == "feedback") {
        	state.pMuteAlexa = true
            result = "Ok, disabling Alexa feedback. To activate just say, activate the feedback"
            return result
		}
		if (unit == "short answer" || unit == "short answers") {
        	state.pShort = false
            result = "Ok, disabling short answers. To activate just say, enable the short answers"
            return result
		}        
        if (unit == "undefined" && group == "undefined" ) {
        	state.pContCmdsR = "clear" 
            result = "Ok, I am here when you need me "
            return result
		}        
    }
	if (command == "start" || command == "enable" || command == "activate" || command == "schedule" || command == "arm") {
		if(group == "security"){
        	def param = [:]
        		param.command = command
        		param.num = num
				param.pintentName = group
        		result = controlSecurity(param)
                log.warn "security arm result = ${result}"
                return result.outputTxt
        }
        if (unit == "reminder" || unit == "reminders" || unit == "timer" || unit == "timers" || unit.contains ("reminder") || unit.contains ("timer") ) {
        	state.scheduledHandler = "reminder"
            result = "Ok, reminder scheduled"
           	return result
    	}
		if (unit == "conversation" || unit.contains ("conversation")) {
           state.pContCmds = true        
           result = "Ok, activating conversational features. To disable just say, stop the conversation"
            return result
        }
        if (unit == "feedback") {
        	state.pMuteAlexa = false
            result = "Ok, activating Alexa feedback. To disable just say, stop the feedback"
            return result
		}
		if (unit == "short answer" || unit == "short answers") {
        	state.pShort = true
            result = "Ok, short answers on"
            return result
		}
        if (unit == "pin number" || unit == "pin") {		
			if (group == "thermostats" || group == "locks" || group == "doors" || group == "switches" || group == "security" ) {
				if (group == "thermostats") {state.usePIN_T 	= true}
                else if (group == "locks") 		{state.usePIN_L 	= true}
                else if (group == "doors") 		{state.usePIN_D 	= true}
                else if (group == "switches") 	{state.usePIN_S 	= true}                              
                else if (group == "security") 	{state.usePIN_SHM 	= true} 
                else if (group == "modes") 		{state.usePIN_Mode 	= true} 
                	state.pTryAgain = false
                    result = "Ok, the pin has been activated for " + group + ".  To disable, just say disable the PIN number for " + group
            		return result
            	}
           		else {
                	result = "Sorry, the pin number cannot be enabled for " + group
            		return result
            	}
           }      
	}
}
/******************************************************************************
	 CONTROL SUPPORT - DATE & TIME FUNCTIONS											
******************************************************************************/
private getTimeVariable(date, type) {
	def currTime
    def currDate
    def currDateShort
    def String tText = (String) null    
    def String duration = (String) null
    def today = new Date(now()).format("EEEE, MMMM d, yyyy", location.timeZone) // format("EEEE, MMMM d, yyyy") REMOVED YEAR 2/8/2017
    def yesterday = new Date(today -0.1).format("EEEE, MMMM d, yyyy", location.timeZone)
	def time = new Date(now()).format("h:mm aa", location.timeZone)
    
    currTime = new Date(date + location.timeZone.rawOffset).format("hh:mm aa")                       
	currDate = new Date(date + location.timeZone.rawOffset).format("EEEE, MMMM d, yyyy")
	currDateShort = new Date(date + location.timeZone.rawOffset).format("EEEE, MMMM d")
    currDate = today == currDate ? "today" : yesterday == currDate ? "yesterday" : currDateShort
		def endTime = now() + location.timeZone.rawOffset
    	def startTime = new Date(date + location.timeZone.rawOffset)
    	startTime = startTime.getTime()
    int hours = (int)((endTime - startTime) / (1000 * 60 * 60) )
    int minutes = (int)((endTime - startTime) / ( 60 * 1000))
    duration = minutes < 60 ? minutes + " minutes" : hours + " hours"
    tText = currDate + " at " + currTime
  	return ["currTime":currTime, "currDate":currDate, "tText":tText, "duration": duration] 
 
}
/***********************************************************************************************************************
    MISC. - SCHEDULE HANDLER
***********************************************************************************************************************/
private scheduleHandler(unit) {
    def rowDate = new Date(now())
    def cDay = rowDate.date
    def cHour= rowDate.hours
	def cMin = rowDate.minutes   
    def result
    if (unit == "filters") {
    	if (debug) log.debug "Received filter replacement request"
        state.scheduledHandler = "filters"
        def xDays = settings.cFilterReplacement
        def tDays = new Date(now() + location.timeZone.rawOffset) + xDays 
        def schTime = tDays.format("h:mm aa")                       
		def schDate = tDays.format("EEEE, MMMM d")
       		runOnce(new Date() + xDays , "filtersHandler")
        	result = "Ok, scheduled reminder to replace the filters on " + schDate + " at " + schTime
        	state.filterNotif = "The filters need to be changed on  ${schDate}"
    		return result
    }
}
/***********************************************************************************************************************
    MISC. - FILTERS REMINDER
***********************************************************************************************************************/
private filtersHandler() {
    def tts = "It's time to replace your HVAC filters"
	if (synthDevice) {
    	synthDevice?.speak(tts) 
    }
    if (sonosDevice){
    	state.sound = textToSpeech(tts instanceof List ? tts[0] : tts)
        def currVolLevel = sonosDevice.latestValue("level")
        def newVolLevel = volume //-(volume*10/100)
        sonosDevice.setLevel(newVolLevel)
        sonosDevice.playTrackAndResume(state.sound.uri, state.sound.duration, volume)
    }
	if(recipients?.size()>0 || sms?.size()>0){        
    	sendtxt(tts)
    }
}
/***********************************************************************************************************************
    MISC. - RUN REPORT FROM PROFILE
***********************************************************************************************************************/
def runReport(profile) {
def result
           		childApps.each {child ->
                        def ch = child.label
                		if (ch == profile) { 
                    		if (debug) log.debug "Found a profile, $profile"
                            result = child.runProfile(ch)
						}
            	}
                return result
}
/***********************************************************************************************************************
    SMS HANDLER
***********************************************************************************************************************/
private void sendtxt(message) {
    if (recipients?.size()>0) { 
            sendNotificationToContacts(message, recipients)
    } 
    else {
        if (push) { 
                sendPush message
        }
        if (sms) {
            processSms(sms, message)
        }
    }
}
private void processSms(number, message) {
    if (sms) {
        def phones = sms.split("\\,")
        for (phone in phones) {
            sendSms(phone, message)
        }
    }
}
/************************************************************************************************************
   Custom Color Filter
************************************************************************************************************/       
private startLoop() {
	def device =  state.lastDevice
    def deviceMatch = cSwitch.find {s -> s.label.toLowerCase() == device.toLowerCase()}	
    int hueLevel = !level ? 100 : level
	int hueHue = Math.random() *100 as Integer
	def randomColor = [hue: hueHue, saturation: 100, level: hueLevel]
	deviceMatch.setColor(randomColor)
    runIn(60, "continueLoop")
}
private continueLoop() {
	def device =  state.lastDevice
    def deviceMatch = cSwitch.find {s -> s.label.toLowerCase() == device.toLowerCase()}	
    int hueLevel = !level ? 100 : level
	int hueHue = Math.random() *100 as Integer
	def randomColor = [hue: hueHue, saturation: 100, level: hueLevel]
	deviceMatch.setColor(randomColor)
    runIn(60, "startLoop")
}

private getColorName(cName, level) {
	if (cName == "random") {
    def randomColor = [:]
    def bulbName
        child?.gHues.each { bulb ->
            int hueLevel = color.l
            int hueHue = Math.random() *100 as Integer
            bulbName = bulb.name
            randomColor = [hue: hueHue, saturation: 100, level: hueLevel]
            if(debug) log.info "setting ${bulbName} to ${randomColor}"
            bulb.setColor(randomColor)
       	}
        return "executed"
	}    
    for (color in fillColorSettings()) {
		if (color.name.toLowerCase() == cName.toLowerCase()) {
        log.warn "found a color match"
        	int hueVal = Math.round(color.h / 3.6)
            int hueLevel = !level ? color.l : level
			def hueSet = [hue: hueVal, saturation: color.s, level: hueLevel]
            return hueSet
		}
	}
	if (debug) log.debug "Color Match Not Found"
}
def fillColorSettings() {
	return [
		[ name: "Soft White",				rgb: "#B6DA7C",		h: 83,		s: 44,		l: 67,	],
		[ name: "Warm White",				rgb: "#DAF17E",		h: 51,		s: 20,		l: 100,	],
        [ name: "Very Warm White",			rgb: "#DAF17E",		h: 51,		s: 60,		l: 51,	],
		[ name: "Daylight White",			rgb: "#CEF4FD",		h: 191,		s: 9,		l: 99,	],
		[ name: "Cool White",				rgb: "#F3F6F7",		h: 187,		s: 19,		l: 100,	],
		[ name: "White",					rgb: "#FFFFFF",		h: 0,		s: 0,		l: 100,	],
		[ name: "Alice Blue",				rgb: "#F0F8FF",		h: 208,		s: 100,		l: 97,	],
		[ name: "Antique White",			rgb: "#FAEBD7",		h: 34,		s: 78,		l: 91,	],
		[ name: "Aqua",						rgb: "#00FFFF",		h: 180,		s: 100,		l: 50,	],
		[ name: "Aquamarine",				rgb: "#7FFFD4",		h: 160,		s: 100,		l: 75,	],
		[ name: "Azure",					rgb: "#F0FFFF",		h: 180,		s: 100,		l: 97,	],
		[ name: "Beige",					rgb: "#F5F5DC",		h: 60,		s: 56,		l: 91,	],
		[ name: "Bisque",					rgb: "#FFE4C4",		h: 33,		s: 100,		l: 88,	],
		[ name: "Blanched Almond",			rgb: "#FFEBCD",		h: 36,		s: 100,		l: 90,	],
		[ name: "Blue",						rgb: "#0000FF",		h: 240,		s: 100,		l: 50,	],
		[ name: "Blue Violet",				rgb: "#8A2BE2",		h: 271,		s: 76,		l: 53,	],
		[ name: "Brown",					rgb: "#A52A2A",		h: 0,		s: 59,		l: 41,	],
		[ name: "Burly Wood",				rgb: "#DEB887",		h: 34,		s: 57,		l: 70,	],
		[ name: "Cadet Blue",				rgb: "#5F9EA0",		h: 182,		s: 25,		l: 50,	],
		[ name: "Chartreuse",				rgb: "#7FFF00",		h: 90,		s: 100,		l: 50,	],
		[ name: "Chocolate",				rgb: "#D2691E",		h: 25,		s: 75,		l: 47,	],
		[ name: "Coral",					rgb: "#FF7F50",		h: 16,		s: 100,		l: 66,	],
		[ name: "Corn Flower Blue",			rgb: "#6495ED",		h: 219,		s: 79,		l: 66,	],
		[ name: "Corn Silk",				rgb: "#FFF8DC",		h: 48,		s: 100,		l: 93,	],
		[ name: "Crimson",					rgb: "#DC143C",		h: 348,		s: 83,		l: 58,	],
		[ name: "Cyan",						rgb: "#00FFFF",		h: 180,		s: 100,		l: 50,	],
		[ name: "Dark Blue",				rgb: "#00008B",		h: 240,		s: 100,		l: 27,	],
		[ name: "Dark Cyan",				rgb: "#008B8B",		h: 180,		s: 100,		l: 27,	],
		[ name: "Dark Golden Rod",			rgb: "#B8860B",		h: 43,		s: 89,		l: 38,	],
		[ name: "Dark Gray",				rgb: "#A9A9A9",		h: 0,		s: 0,		l: 66,	],
		[ name: "Dark Green",				rgb: "#006400",		h: 120,		s: 100,		l: 20,	],
		[ name: "Dark Khaki",				rgb: "#BDB76B",		h: 56,		s: 38,		l: 58,	],
		[ name: "Dark Magenta",				rgb: "#8B008B",		h: 300,		s: 100,		l: 27,	],
		[ name: "Dark Olive Green",			rgb: "#556B2F",		h: 82,		s: 39,		l: 30,	],
		[ name: "Dark Orange",				rgb: "#FF8C00",		h: 33,		s: 100,		l: 50,	],
		[ name: "Dark Orchid",				rgb: "#9932CC",		h: 280,		s: 61,		l: 50,	],
		[ name: "Dark Red",					rgb: "#8B0000",		h: 0,		s: 100,		l: 27,	],
		[ name: "Dark Salmon",				rgb: "#E9967A",		h: 15,		s: 72,		l: 70,	],
		[ name: "Dark Sea Green",			rgb: "#8FBC8F",		h: 120,		s: 25,		l: 65,	],
		[ name: "Dark Slate Blue",			rgb: "#483D8B",		h: 248,		s: 39,		l: 39,	],
		[ name: "Dark Slate Gray",			rgb: "#2F4F4F",		h: 180,		s: 25,		l: 25,	],
		[ name: "Dark Turquoise",			rgb: "#00CED1",		h: 181,		s: 100,		l: 41,	],
		[ name: "Dark Violet",				rgb: "#9400D3",		h: 282,		s: 100,		l: 41,	],
		[ name: "Deep Pink",				rgb: "#FF1493",		h: 328,		s: 100,		l: 54,	],
		[ name: "Deep Sky Blue",			rgb: "#00BFFF",		h: 195,		s: 100,		l: 50,	],
		[ name: "Dim Gray",					rgb: "#696969",		h: 0,		s: 0,		l: 41,	],
		[ name: "Dodger Blue",				rgb: "#1E90FF",		h: 210,		s: 100,		l: 56,	],
		[ name: "Fire Brick",				rgb: "#B22222",		h: 0,		s: 68,		l: 42,	],
		[ name: "Floral White",				rgb: "#FFFAF0",		h: 40,		s: 100,		l: 97,	],
		[ name: "Forest Green",				rgb: "#228B22",		h: 120,		s: 61,		l: 34,	],
		[ name: "Fuchsia",					rgb: "#FF00FF",		h: 300,		s: 100,		l: 50,	],
		[ name: "Gainsboro",				rgb: "#DCDCDC",		h: 0,		s: 0,		l: 86,	],
		[ name: "Ghost White",				rgb: "#F8F8FF",		h: 240,		s: 100,		l: 99,	],
		[ name: "Gold",						rgb: "#FFD700",		h: 51,		s: 100,		l: 50,	],
		[ name: "Golden Rod",				rgb: "#DAA520",		h: 43,		s: 74,		l: 49,	],
		[ name: "Gray",						rgb: "#808080",		h: 0,		s: 0,		l: 50,	],
		[ name: "Green",					rgb: "#008000",		h: 120,		s: 100,		l: 25,	],
		[ name: "Green Yellow",				rgb: "#ADFF2F",		h: 84,		s: 100,		l: 59,	],
		[ name: "Honeydew",					rgb: "#F0FFF0",		h: 120,		s: 100,		l: 97,	],
		[ name: "Hot Pink",					rgb: "#FF69B4",		h: 330,		s: 100,		l: 71,	],
		[ name: "Indian Red",				rgb: "#CD5C5C",		h: 0,		s: 53,		l: 58,	],
		[ name: "Indigo",					rgb: "#4B0082",		h: 275,		s: 100,		l: 25,	],
		[ name: "Ivory",					rgb: "#FFFFF0",		h: 60,		s: 100,		l: 97,	],
		[ name: "Khaki",					rgb: "#F0E68C",		h: 54,		s: 77,		l: 75,	],
		[ name: "Lavender",					rgb: "#E6E6FA",		h: 240,		s: 67,		l: 94,	],
		[ name: "Lavender Blush",			rgb: "#FFF0F5",		h: 340,		s: 100,		l: 97,	],
		[ name: "Lawn Green",				rgb: "#7CFC00",		h: 90,		s: 100,		l: 49,	],
		[ name: "Lemon Chiffon",			rgb: "#FFFACD",		h: 54,		s: 100,		l: 90,	],
		[ name: "Light Blue",				rgb: "#ADD8E6",		h: 195,		s: 53,		l: 79,	],
		[ name: "Light Coral",				rgb: "#F08080",		h: 0,		s: 79,		l: 72,	],
		[ name: "Light Cyan",				rgb: "#E0FFFF",		h: 180,		s: 100,		l: 94,	],
		[ name: "Light Golden Rod Yellow",	rgb: "#FAFAD2",		h: 60,		s: 80,		l: 90,	],
		[ name: "Light Gray",				rgb: "#D3D3D3",		h: 0,		s: 0,		l: 83,	],
		[ name: "Light Green",				rgb: "#90EE90",		h: 120,		s: 73,		l: 75,	],
		[ name: "Light Pink",				rgb: "#FFB6C1",		h: 351,		s: 100,		l: 86,	],
		[ name: "Light Salmon",				rgb: "#FFA07A",		h: 17,		s: 100,		l: 74,	],
		[ name: "Light Sea Green",			rgb: "#20B2AA",		h: 177,		s: 70,		l: 41,	],
		[ name: "Light Sky Blue",			rgb: "#87CEFA",		h: 203,		s: 92,		l: 75,	],
		[ name: "Light Slate Gray",			rgb: "#778899",		h: 210,		s: 14,		l: 53,	],
		[ name: "Light Steel Blue",			rgb: "#B0C4DE",		h: 214,		s: 41,		l: 78,	],
		[ name: "Light Yellow",				rgb: "#FFFFE0",		h: 60,		s: 100,		l: 94,	],
		[ name: "Lime",						rgb: "#00FF00",		h: 120,		s: 100,		l: 50,	],
		[ name: "Lime Green",				rgb: "#32CD32",		h: 120,		s: 61,		l: 50,	],
		[ name: "Linen",					rgb: "#FAF0E6",		h: 30,		s: 67,		l: 94,	],
		[ name: "Maroon",					rgb: "#800000",		h: 0,		s: 100,		l: 25,	],
		[ name: "Medium Aquamarine",		rgb: "#66CDAA",		h: 160,		s: 51,		l: 60,	],
		[ name: "Medium Blue",				rgb: "#0000CD",		h: 240,		s: 100,		l: 40,	],
		[ name: "Medium Orchid",			rgb: "#BA55D3",		h: 288,		s: 59,		l: 58,	],
		[ name: "Medium Purple",			rgb: "#9370DB",		h: 260,		s: 60,		l: 65,	],
		[ name: "Medium Sea Green",			rgb: "#3CB371",		h: 147,		s: 50,		l: 47,	],
		[ name: "Medium Slate Blue",		rgb: "#7B68EE",		h: 249,		s: 80,		l: 67,	],
		[ name: "Medium Spring Green",		rgb: "#00FA9A",		h: 157,		s: 100,		l: 49,	],
		[ name: "Medium Turquoise",			rgb: "#48D1CC",		h: 178,		s: 60,		l: 55,	],
		[ name: "Medium Violet Red",		rgb: "#C71585",		h: 322,		s: 81,		l: 43,	],
		[ name: "Midnight Blue",			rgb: "#191970",		h: 240,		s: 64,		l: 27,	],
		[ name: "Mint Cream",				rgb: "#F5FFFA",		h: 150,		s: 100,		l: 98,	],
		[ name: "Misty Rose",				rgb: "#FFE4E1",		h: 6,		s: 100,		l: 94,	],
		[ name: "Moccasin",					rgb: "#FFE4B5",		h: 38,		s: 100,		l: 85,	],
		[ name: "Navajo White",				rgb: "#FFDEAD",		h: 36,		s: 100,		l: 84,	],
		[ name: "Navy",						rgb: "#000080",		h: 240,		s: 100,		l: 25,	],
		[ name: "Old Lace",					rgb: "#FDF5E6",		h: 39,		s: 85,		l: 95,	],
		[ name: "Olive",					rgb: "#808000",		h: 60,		s: 100,		l: 25,	],
		[ name: "Olive Drab",				rgb: "#6B8E23",		h: 80,		s: 60,		l: 35,	],
		[ name: "Orange",					rgb: "#FFA500",		h: 39,		s: 100,		l: 50,	],
		[ name: "Orange Red",				rgb: "#FF4500",		h: 16,		s: 100,		l: 50,	],
		[ name: "Orchid",					rgb: "#DA70D6",		h: 302,		s: 59,		l: 65,	],
		[ name: "Pale Golden Rod",			rgb: "#EEE8AA",		h: 55,		s: 67,		l: 80,	],
		[ name: "Pale Green",				rgb: "#98FB98",		h: 120,		s: 93,		l: 79,	],
		[ name: "Pale Turquoise",			rgb: "#AFEEEE",		h: 180,		s: 65,		l: 81,	],
		[ name: "Pale Violet Red",			rgb: "#DB7093",		h: 340,		s: 60,		l: 65,	],
		[ name: "Papaya Whip",				rgb: "#FFEFD5",		h: 37,		s: 100,		l: 92,	],
		[ name: "Peach Puff",				rgb: "#FFDAB9",		h: 28,		s: 100,		l: 86,	],
		[ name: "Peru",						rgb: "#CD853F",		h: 30,		s: 59,		l: 53,	],
		[ name: "Pink",						rgb: "#FFC0CB",		h: 350,		s: 100,		l: 88,	],
		[ name: "Plum",						rgb: "#DDA0DD",		h: 300,		s: 47,		l: 75,	],
		[ name: "Powder Blue",				rgb: "#B0E0E6",		h: 187,		s: 52,		l: 80,	],
		[ name: "Purple",					rgb: "#800080",		h: 300,		s: 100,		l: 25,	],
		[ name: "Red",						rgb: "#FF0000",		h: 0,		s: 100,		l: 50,	],
		[ name: "Rosy Brown",				rgb: "#BC8F8F",		h: 0,		s: 25,		l: 65,	],
		[ name: "Royal Blue",				rgb: "#4169E1",		h: 225,		s: 73,		l: 57,	],
		[ name: "Saddle Brown",				rgb: "#8B4513",		h: 25,		s: 76,		l: 31,	],
		[ name: "Salmon",					rgb: "#FA8072",		h: 6,		s: 93,		l: 71,	],
		[ name: "Sandy Brown",				rgb: "#F4A460",		h: 28,		s: 87,		l: 67,	],
		[ name: "Sea Green",				rgb: "#2E8B57",		h: 146,		s: 50,		l: 36,	],
		[ name: "Sea Shell",				rgb: "#FFF5EE",		h: 25,		s: 100,		l: 97,	],
		[ name: "Sienna",					rgb: "#A0522D",		h: 19,		s: 56,		l: 40,	],
		[ name: "Silver",					rgb: "#C0C0C0",		h: 0,		s: 0,		l: 75,	],
		[ name: "Sky Blue",					rgb: "#87CEEB",		h: 197,		s: 71,		l: 73,	],
		[ name: "Slate Blue",				rgb: "#6A5ACD",		h: 248,		s: 53,		l: 58,	],
		[ name: "Slate Gray",				rgb: "#708090",		h: 210,		s: 13,		l: 50,	],
		[ name: "Snow",						rgb: "#FFFAFA",		h: 0,		s: 100,		l: 99,	],
		[ name: "Spring Green",				rgb: "#00FF7F",		h: 150,		s: 100,		l: 50,	],
		[ name: "Steel Blue",				rgb: "#4682B4",		h: 207,		s: 44,		l: 49,	],
		[ name: "Tan",						rgb: "#D2B48C",		h: 34,		s: 44,		l: 69,	],
		[ name: "Teal",						rgb: "#008080",		h: 180,		s: 100,		l: 25,	],
		[ name: "Thistle",					rgb: "#D8BFD8",		h: 300,		s: 24,		l: 80,	],
		[ name: "Tomato",					rgb: "#FF6347",		h: 9,		s: 100,		l: 64,	],
		[ name: "Turquoise",				rgb: "#40E0D0",		h: 174,		s: 72,		l: 56,	],
		[ name: "Violet",					rgb: "#EE82EE",		h: 300,		s: 76,		l: 72,	],
		[ name: "Wheat",					rgb: "#F5DEB3",		h: 39,		s: 77,		l: 83,	],
		[ name: "White Smoke",				rgb: "#F5F5F5",		h: 0,		s: 0,		l: 96,	],
		[ name: "Yellow",					rgb: "#FFFF00",		h: 60,		s: 100,		l: 50,	],
		[ name: "Yellow Green",				rgb: "#9ACD32",		h: 80,		s: 61,		l: 50,	],
	]
}
/***********************************************************************************************************************
    MISC. - LAST MESSAGE HANDLER
***********************************************************************************************************************/
private getLastMessageMain() {
	def outputTxt = "The last message sent was," + state.lastMessage + ", and it was sent to, " + state.lastIntent + ", at, " + state.lastTime
    return  outputTxt 
}
/***********************************************************************************************************************
 		WEATHER FORECAST (DASH + FULL)
 ***********************************************************************************************************************/
def private mGetWeather(){
	state.pTryAgain = false
    def result ="Today's weather is not available at the moment, please try again later"
//	try {
    	//daily forecast text
        def weather = getWeatherFeature("forecast", settings.wZipCode)
        def todayWeather = 	weather.forecast.txt_forecast.forecastday[0].fcttext 
        def tonightWeather = weather.forecast.txt_forecast.forecastday[1].fcttext 
		def tomorrowWeather = weather.forecast.txt_forecast.forecastday[2].fcttext 
        //simple forecast 
        def sTodayWeather = weather.forecast.simpleforecast.forecastday[0]
        def sTonightWeather = weather.forecast.simpleforecast.forecastday[1]
		def sTomorrowWeather = weather.forecast.simpleforecast.forecastday[2]
        def sHumidity = sTodayWeather.avehumidity + " for " + sTodayWeather.date.weekday + ", " + sTodayWeather.date.monthname + ", " + sTodayWeather.date.day
		def sHumidityTomorrow = sTonightWeather.avehumidity + " for " + sTonightWeather.date.weekday + ", " + sTonightWeather.date.monthname + ", " + sTonightWeather.date.day
		def sLow = sTodayWeather.low.fahrenheit
        def sHigh = sTodayWeather.high.fahrenheit
        def sRainFall = sTodayWeather.qpf_day.in + " inches"
        def sPrecip = sTodayWeather.pop + "percent"
        //conditions
		def condWeather = getWeatherFeature("conditions", settings.wZipCode)
        def condTodayWeather = 	condWeather.current_observation.weather
		def condTodayhumidity = condWeather.current_observation.relative_humidity
		def condTodayUV = condWeather.current_observation.UV
		def condTodayZip = condWeather.current_observation.display_location.zip
        log.warn "reporting zip code: zip ${condTodayZip}"
        if(wMetric){
			result = "Today's forecast is " + weather.forecast.txt_forecast.forecastday[0].fcttext_metric + " Tonight it will be " + weather.forecast.txt_forecast.forecastday[1].fcttext_metric
        	result = result?.toString()
            result = result?.replaceAll(/([0-9]+)C/,'$1 degrees')
            // clean up wind direction (South)
            result = result?.replaceAll(~/ SSW /, " South-southwest ").replaceAll(~/ SSE /, " South-southeast ").replaceAll(~/ SE /, " Southeast ").replaceAll(~/ SW /, " Southwest ")
            // clean up wind direction (North)
            result = result?.replaceAll(~/ NNW /, " North-northwest ").replaceAll(~/ NNE /, " North-northeast ").replaceAll(~/ NE /, " Northeast ").replaceAll(~/ NW /, " Northwest ")
            // clean up wind direction (West)
            result = result?.replaceAll(~/ WNW /, " West-northwest ").replaceAll(~/ WSW /, " West-southwest ")
            // clean up wind direction (East)
            result = result?.replaceAll(~/ ENE /, " East-northeast ").replaceAll(~/ ESE /, " East-southeast ")
            //result = result + " humidity " + sHumidity /// simple weather example
            result = result?.toLowerCase()
        }
        else {
    		result = "Today's forecast is: " + weather.forecast.txt_forecast.forecastday[0].fcttext   + " Tonight it will be " + weather.forecast.txt_forecast.forecastday[1].fcttext
        	result = result?.toString()
            //clean up wind and temps units
            result = result?.replaceAll(/([0-9]+)F/,'$1 degrees').replaceAll(~/mph/, " miles per hour")
            // clean up wind direction (South)
            result = result?.replaceAll(~/ SSW /, " South-southwest ").replaceAll(~/ SSE /, " South-southeast ").replaceAll(~/ SE /, " Southeast ").replaceAll(~/ SW /, " Southwest ")
            // clean up wind direction (North)
            result = result?.replaceAll(~/ NNW /, " North-northwest ").replaceAll(~/ NNE /, " North-northeast ").replaceAll(~/ NE /, " Northeast ").replaceAll(~/ NW /, " Northwest ")
            // clean up wind direction (West)
            result = result?.replaceAll(~/ WNW /, " West-northwest ").replaceAll(~/ WSW /, " West-southwest ")
            // clean up wind direction (East)
            result = result?.replaceAll(~/ ENE /, " East-northeast ").replaceAll(~/ ESE /, " East-southeast ")
            //result = result + " humidity " + sHumidity /// simple weather example
            result = result?.toLowerCase()
        }
        log.info "returning Today's forecast result"
        return result
	}
  /*  catch (Throwable t) {
		log.error t
        state.pTryAgain = true
        return result
	}
}*/
/***********************************************************************************************************************
    WEATHER FORECAST (SHORT)
***********************************************************************************************************************/
def private mGetWeatherShort(period){
	state.pTryAgain = false
    def result ="The weather service is not available at the moment, please try again later"
//	try {
    	//daily forecast text
        def weather = getWeatherFeature("forecast", settings.wZipCode)
        def todayWeather = 	weather.forecast.txt_forecast.forecastday[0].fcttext 
        def tonightWeather = weather.forecast.txt_forecast.forecastday[1].fcttext 
		def tomorrowWeather = weather.forecast.txt_forecast.forecastday[2].fcttext 
        def forecast = period == "today" ? todayWeather : period == "tonight" ? tonightWeather :  period == "tomorrow" ? tomorrowWeather : null
        
        if(wMetric){
        	def todayWeather_m = 	weather.forecast.txt_forecast.forecastday[0].fcttext_metric 
        	def tonightWeather_m = weather.forecast.txt_forecast.forecastday[1].fcttext_metric 
			def tomorrowWeather_m = weather.forecast.txt_forecast.forecastday[2].fcttext_metric 
        	def forecast_metric_m = period == "today" ? todayWeather_m : period == "tonight" ? tonightWeather_m :  period == "tomorrow" ? tomorrowWeather_m : null

			result = period + "'s forecast is " + tomorrowWeather_m
        	result = result.toString()
            result = result.replaceAll(/([0-9]+)C/,'$1 degrees')
            // clean up wind direction (South)
            result = result.replaceAll(~/ SSW /, " South-southwest ").replaceAll(~/ SSE /, " South-southeast ").replaceAll(~/ SE /, " Southeast ").replaceAll(~/ SW /, " Southwest ")
            // clean up wind direction (North)
            result = result.replaceAll(~/ NNW /, " North-northwest ").replaceAll(~/ NNE /, " North-northeast ").replaceAll(~/ NE /, " Northeast ").replaceAll(~/ NW /, " Northwest ")
            // clean up wind direction (West)
            result = result.replaceAll(~/ WNW /, " West-northwest ").replaceAll(~/ WSW /, " West-southwest ")
            // clean up wind direction (East)
            result = result.replaceAll(~/ ENE /, " East-northeast ").replaceAll(~/ ESE /, " East-southeast ")
            //result = result + " humidity " + sHumidity /// simple weather example
            result = result.toLowerCase()
        }
        else {
    		result = period + "'s forecast is " + forecast
        	result = result.toString()
            //clean up wind and temps units
            result = result.replaceAll(/([0-9]+)F/,'$1 degrees').replaceAll(~/mph/, " miles per hour")
            // clean up wind direction (South)
            result = result.replaceAll(~/ SSW /, " South-southwest ").replaceAll(~/ SSE /, " South-southeast ").replaceAll(~/ SE /, " Southeast ").replaceAll(~/ SW /, " Southwest ")
            // clean up wind direction (North)
            result = result.replaceAll(~/ NNW /, " North-northwest ").replaceAll(~/ NNE /, " North-northeast ").replaceAll(~/ NE /, " Northeast ").replaceAll(~/ NW /, " Northwest ")
            // clean up wind direction (West)
            result = result.replaceAll(~/ WNW /, " West-northwest ").replaceAll(~/ WSW /, " West-southwest ")
            // clean up wind direction (East)
            result = result.replaceAll(~/ ENE /, " East-northeast ").replaceAll(~/ ESE /, " East-southeast ")
            //result = result + " humidity " + sHumidity /// simple weather example
            result = result.toLowerCase()
        }
        log.info "returning Today's forecast result"
        return result
	}
/*    catch (Throwable t) {
		log.error t
        state.pTryAgain = true
        return result
	}
}*/
/***********************************************************************************************************************
    WEATHER ELEMENTS
***********************************************************************************************************************/
def private mGetWeatherElements(element){
	state.pTryAgain = false
    def result ="Current weather is not available at the moment, please try again later"
//   	try {
        //hourly updates
        def cWeather = getWeatherFeature("hourly", settings.wZipCode)
        def cWeatherCondition = cWeather.hourly_forecast[0].condition
        def cWeatherPrecipitation = cWeather.hourly_forecast[0].pop + " percent"
        def cWeatherWind = cWeather.hourly_forecast[0].wspd.english + " miles per hour"
        def cWeatherHum = cWeather.hourly_forecast[0].humidity + " percent"
        def cWeatherUpdate = cWeather.hourly_forecast[0].FCTTIME.civil
        
        def condWeather = getWeatherFeature("conditions", settings.wZipCode)
        def condTodayUV = condWeather.current_observation.UV
        
        if(debug) log.debug "cWeatherUpdate = ${cWeatherUpdate}, cWeatherCondition = ${cWeatherCondition}, " +
        					"cWeatherPrecipitation = ${cWeatherPrecipitation}, cWeatherWind = ${cWeatherWind},  cWeatherHum = ${cWeatherHum}, cWeatherHum = ${condTodayUV}  "    

        if(wMetric){
        //hourly metric updates
        def cWeatherWind_m = cWeather.hourly_forecast[0].wspd.metric + " kilometers per hour"        
        	if		(element == "precip" || element == "rain") {result = "The chance of precipitation is " + cWeatherPrecipitation }
        	else if	(element == "wind") {result = "The wind intensity is " + cWeatherWind_m }
        	else if	(element == "uv") {result = "The UV index is " + condTodayUV }
			else if	(element == "hum") {result = "The relative humidity is " + cWeatherHum }        
			else if	(element == "cond") {result = "The current weather condition is " + cWeatherCondition }
        }
        else{
        	if		(element == "precip" || element == "rain") {result = "The chance of precipitation is " + cWeatherPrecipitation }
        	else if	(element == "wind") {result = "The wind intensity is " + cWeatherWind }
        	else if	(element == "uv") {result = "The UV index is " + condTodayUV }
			else if	(element == "hum") {result = "The relative humidity is " + cWeatherHum }        
			else if	(element == "cond") {result = "The current weather condition is " + cWeatherCondition }        
		}        
        return result
	}
/*	catch (Throwable t) {
		log.error t
        state.pTryAgain = true
        return result
	} 
}*/
/***********************************************************************************************************************
    WEATHER TEMPS
***********************************************************************************************************************/
def private mGetWeatherTemps(){
	state.pTryAgain = false
    def result ="Today's temperatures not available at the moment, please try again later"
//	try {
		def weather = getWeatherFeature("forecast", settings.wZipCode)
        def sTodayWeather = weather.forecast.simpleforecast.forecastday[0]
        def tHigh = sTodayWeather.high.fahrenheit//.toInteger()
        def tLow = sTodayWeather.low.fahrenheit//.toInteger()
        	if(wMetric){
                def tHighC = weather.forecast.simpleforecast.forecastday[0].high.celsius//.toInteger()
                def tLowC = weather.forecast.simpleforecast.forecastday[0].low.celsius//.toInteger()
                result = "Today's low temperature is: " + tLowC  + ", with a high of " + tHighC
            }
            else {
                result = "Today's low temperature is: " + tLow  + ", with a high of " + tHigh
        	}
            return result
	}
/*	catch (Throwable t) {
        log.error t
        state.pTryAgain = true
        return result
    }
}   */
/***********************************************************************************************************************
    WEATHER ALERTS
***********************************************************************************************************************/
def private mGetWeatherAlerts(){
	def result = "There are no weather alerts for your area"
//	try {
		def weather = getWeatherFeature("alerts", settings.wZipCode)
        def alert = weather.alerts.description[0]
        def expire = weather.alerts.expires[0]
        	expire = expire?.replaceAll(~/ EST /, " ")?.replaceAll(~/ CST /, " ")?.replaceAll(~/ MST /, " ")?.replaceAll(~/ PST /, " ")
        	log.warn "alert = ${alert} , expire = ${expire}"   	
            if(alert != null) {
                result = alert  + " is in effect for your area, that expires at " + expire            
            }
        return result
    }
/*	catch (Throwable t) {
	log.error t
	return result
	}
}*/
/***********************************************************************************************************************
    HOURLY FORECAST
***********************************************************************************************************************/
def mGetWeatherUpdates(){
    def weatherData = [:]
    def data = [:]
   	def result
    //try {
        //hourly updates
            def cWeather = getWeatherFeature("hourly", settings.wZipCode)
            def cWeatherCondition = cWeather.hourly_forecast[0].condition
            def cWeatherPrecipitation = cWeather.hourly_forecast[0].pop + " percent"
            def cWeatherWind = cWeather.hourly_forecast[0].wspd.english + " miles per hour"
            def cWeatherHum = cWeather.hourly_forecast[0].humidity + " percent"
            def cWeatherUpdate = cWeather.hourly_forecast[0].FCTTIME.civil
            //past hour's forecast
            def pastWeather = state.lastWeatherUpdate
            def lastCheck = state.lastWeatherCheck
            //current forecast
				weatherData.wCond = cWeatherCondition
                weatherData.wWind = cWeatherWind
                weatherData.wHum = cWeatherHum
                weatherData.wPrecip = cWeatherPrecipitation
            def lastUpdated = new Date(now()).format("h:mm aa", location.timeZone)
                if(pastWeather == null) {
                    state.lastWeatherUpdate = weatherData
                    state.lastWeatherCheck = lastUpdated
                }
                else {
                    def wUpdate = pastWeather.wCond != cWeatherCondition ? "current weather condition" : pastWeather.wWind != cWeatherWind ? "wind intensity" : pastWeather.wHum != cWeatherHum ? "humidity" : pastWeather.wPrecip != cWeatherPrecipitation ? "chance of precipitation" : null
                    def wChange = wUpdate == "current weather condition" ? cWeatherCondition : wUpdate == "wind intensity" ? cWeatherWind  : wUpdate == "humidity" ? cWeatherHum : wUpdate == "chance of precipitation" ? cWeatherPrecipitation : null                    
                    //something has changed
                    if(wUpdate != null){
                        // saving update to state
                        state.lastWeatherUpdate = weatherData
                        state.lastWeatherCheck = lastUpdated
                        	def condChanged = pastWeather.wCond != cWeatherCondition
                            def windChanged = pastWeather.wWind != cWeatherWind
                            def humChanged = pastWeather.wHum != cWeatherHum
                            def precChanged = pastWeather.wPrecip != cWeatherPrecipitation
							if(condChanged){
                            	result = "Yes, the weather forecast was last updated at " + lastCheck + ". The weather condition has been changed to "  + cWeatherCondition
                            }
                            if(windChanged){
                            	if(result) {result = result +  " , the wind intensity to "  + cWeatherWind }
                            	else result = "Yes, the weather forecast was last updated at " + lastCheck + ". The wind intensity has been changed to "  + cWeatherWind
							}
                            if(humChanged){
                            	if(result) {result = result +  " , the humidity to "  + cWeatherHum }
                            	else result = "Yes, the weather forecast was last updated at " + lastCheck + ". The humidity has been changed to "  + cWeatherHum
							}
                            if(precChanged){
                            	if(result) {result = result + " , the chance of rain to "  + cWeatherPrecipitation }
                            	else result = "Yes, the weather forecast was last updated at " + lastCheck + ". The chance of rain has been changed to "  + cWeatherPrecipitation
                            }
							return result
                        }
                        else {
                        	result = "No, there have been no updates to the forecast since " + lastCheck
                            return result
                        }
                }
                log.info "refreshed hourly weather forecast: past forecast = ${pastWeather}; new forecast = ${weatherData}"  
    /*
    }
	catch (Throwable t) {
	log.error t
	return result
	}
    */
}
/*^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
X 																											X
X                       					UI FUNCTIONS													X
X                        																					X
/*^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
/************************************************************************************************************
   		UI - Version/Copyright/Information/Help
************************************************************************************************************/
private def textAppName() {
	def text = app.label // Parent Name
}
private def textLicense() {
	def text =
	"Licensed under the Apache License, Version 2.0 (the 'License'); "+
	"you may not use this file except in compliance with the License. "+
	"You may obtain a copy of the License at"+
	" \n"+
	" http://www.apache.org/licenses/LICENSE-2.0"+
	" \n"+
	"Unless required by applicable law or agreed to in writing, software "+
	"distributed under the License is distributed on an 'AS IS' BASIS, "+
	"WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. "+
	"See the License for the specific language governing permissions and "+
	"limitations under the License."
}
/***********************************************************************************************************************
 		UI - SKILL DETAILS
 ***********************************************************************************************************************/
private getControlDetails() {
	def sec = [] 
	def modes = location.modes.name.sort()
        modes?.each { m -> 
		sec +=m +"\n" } 
	def routines = location.helloHome?.getPhrases()*.label.sort()
    	routines?.each { p -> 
			sec +=p +"\n" } 	
	def security = ["off", "away", "stay", "staying", "leaving","status"]
    	security?.each { s -> 
			sec +=s +"\n" }     
    	
        def dUniqueList = sec.unique (false)
        dUniqueList = dUniqueList.sort()
        def dUniqueListString = dUniqueList.join("")
        return dUniqueListString
}
private getControlHtml() {
	def sec = [] 
	def modes = location.modes.name.sort()
        modes?.each { m -> 
		sec +=m +"<br>" } 
	def routines = location.helloHome?.getPhrases()*.label.sort()
    	routines?.each { p -> 
			sec +=p +"<br>" } 	
	def security = ["off", "away", "stay", "staying", "leaving","status"]
    	security?.each { s -> 
			sec +=s +"<br>" }     
    	
        def dUniqueList = sec.unique (false)
        dUniqueList = dUniqueList.sort()
        def dUniqueListString = dUniqueList.join("")
        return dUniqueListString
}


private getDeviceDetails() {
	def DeviceDetails = [] 
        //switches
        cSwitch?.each 		{DeviceDetails << it.displayName +"\n"}
        cTstat?.each 		{DeviceDetails << it.displayName +"\n"}
        cLock?.each 		{DeviceDetails << it.displayName +"\n"}     
        cMotion?.each 		{DeviceDetails << it.displayName +"\n"}
        cContact?.each 		{DeviceDetails << it.displayName +"\n"}
        cPresence?.each 	{DeviceDetails << it.displayName +"\n"}
        cDoor?.each 		{DeviceDetails << it.displayName +"\n"}
        cWater?.each 		{DeviceDetails << it.displayName +"\n"}
        cSpeaker?.each 		{DeviceDetails << it.displayName +"\n"}
        cIndoor?.each 		{DeviceDetails << it.displayName +"\n"}
        cOutDoor?.each 		{DeviceDetails << it.displayName +"\n"}
        cVent?.each 		{DeviceDetails << it.displayName +"\n"}
        cFan?.each 			{DeviceDetails << it.displayName +"\n"}
		cVent?.each			{DeviceDetails << it.displayName +"\n"}
    	cRelay?.each		{DeviceDetails << it.displayName +"\n"}
        cSynth?.each		{DeviceDetails << it.displayName +"\n"}
        cMedia?.each		{DeviceDetails << it.displayName +"\n"} 
        cBattery?.each		{DeviceDetails << it.displayName +"\n"}
        cMiscDev?.each		{DeviceDetails << it.displayName +"\n"} // added 2/1/2017 BD
        childApps?.each 	{DeviceDetails << it.label +"\n"} 		// added 2/23/2017 BD
        
        if(cMedia) {
            cMedia?.each {a ->         
                def activities = a.currentState("activities").value
                def activityList = new groovy.json.JsonSlurper().parseText(activities)
                    activityList.each { it ->  
                    	def activity = it
                            DeviceDetails << activity.name +"\n"
                    }
            }
        }   
        def dUniqueList = DeviceDetails.unique(false)
        dUniqueList = dUniqueList.sort()
        def dUniqueListString = dUniqueList.join("")
        return dUniqueListString
}
private getDeviceHtml() {
	def DeviceDetails = [] 
        //switches
        cSwitch?.each 		{DeviceDetails << it.displayName +"<br>"}
        cTstat?.each 		{DeviceDetails << it.displayName +"<br>"}
        cLock?.each 		{DeviceDetails << it.displayName +"<br>"}     
        cMotion?.each 		{DeviceDetails << it.displayName +"<br>"}
        cContact?.each 		{DeviceDetails << it.displayName +"<br>"}
        cPresence?.each 	{DeviceDetails << it.displayName +"<br>"}
        cDoor?.each 		{DeviceDetails << it.displayName +"<br>"}
        cWater?.each 		{DeviceDetails << it.displayName +"<br>"}
        cSpeaker?.each 		{DeviceDetails << it.displayName +"<br>"}
        cIndoor?.each 		{DeviceDetails << it.displayName +"<br>"}
        cOutDoor?.each 		{DeviceDetails << it.displayName +"<br>"}        
		cVent?.each 		{DeviceDetails << it.displayName +"<br>"}
        cFan?.each 			{DeviceDetails << it.displayName +"<br>"}
		cVent?.each			{DeviceDetails << it.displayName +"<br>"}
    	cRelay?.each		{DeviceDetails << it.displayName +"<br>"}
        cSynth?.each		{DeviceDetails << it.displayName +"<br>"}
        cMedia?.each		{DeviceDetails << it.displayName +"<br>"}
        cBattery?.each		{DeviceDetails << it.displayName +"<br>"}
        cMiscDev?.each		{DeviceDetails << it.displayName +"<br>"} // added 2/1/2017 BD
        childApps?.each 	{DeviceDetails << it.label +"<br>"}	// added 2/23/2017 BD
        
        if(cMedia) {
            cMedia?.each {a ->         
                def activities = a.currentState("activities").value
                def activityList = new groovy.json.JsonSlurper().parseText(activities)
                    activityList.each { it ->  
                    	def activity = it
                            DeviceDetails << activity.name +"<br>"
                    }
            }
        }   
        def dUniqueList = DeviceDetails.unique(false)
        dUniqueList = dUniqueList.sort()
        def dUniqueListString = dUniqueList.join("")
        return dUniqueListString
}


/************************************************************************************************************
   Page status and descriptions 
************************************************************************************************************/       
//	Naming Conventions: 
// 	description = pageName + D (E.G: description: mIntentD())
// 	state = pageName + S (E.G: state: mIntentS(),
/************************************************************************************************************/       
def pRestrictSettings(){ def result = "" 
	if (modes || runDay || hues ||startingX || endingX) {
    	result = "complete"}
        result}
def pRestrictComplete() {def text = "Tap here to configure" 
    if (modes || runDay || hues ||startingX || endingX) {
    	text = "Configured"}
    	else text = "Tap here to Configure"
        text}
/** Main Profiles Page **/
def mIntentS(){
	def result = ""
    def IntentS = ""
    if (cSwitch || cFan || cDoor || cRelay || cTstat || cIndoor || cOutDoor || cVent || cMotion || cContact || cWater || cPresence || cSpeaker || cSynth || cMedia || cBattery) {
    	IntentS = "comp"
        result = "complete"
    }    	
    	result
}
def mIntentD() {
    def text = "Tap here to Configure"
	def mIntentS = mIntentS()
    if (mIntentS) 
    {
        text = "Configured"
    }
    else text = "Tap here to Configure"
	    text
}  
/** Configure Profiles Pages **/
def mRoomsS(){
    def result = ""
    if (childApps?.size()) {
    	result = "complete"	
    }
    result
}
def mRoomsD() {
    def text = "No Profiles have been configured. Tap here to begin"
    def ch = childApps?.size()     
    if (ch == 1) {
        text = "One profile has been configured. Tap here to view and change"
    }
    else {
    	if (ch > 1) {
        text = "${ch} Profiles have been configured. Tap here to view and change"
     	}
    }
    text
}
/** General Settings Page **/
def mSettingsS() {
    def result = ""
    if (ShowLicense || debug) {
    	result = "complete"	
    }
    result
}
def mSettingsD() {
    def text = "Tap here to Configure"
    if (ShowLicense || debug) { 
            text = "Configured"
    }
    text
}
/** Install and Support Page **/
def mSupportS() {
    def result = ""
    if (notifyOn || securityOn) {
    	result = "complete"	
    }
    result
}
def mSupportD() {
    def text = "There are no modules installed"
    if (notifyOn || securityOn) { 
            text = "Modules are Installed"
    }
    text
}
/** Dashboard **/
mDashboardD
def mDashboardS() {
    def result = ""
    if (mLocalWeather || mWeatherConfig || ThermoStat1 || ThermoStat2 || tempSens1 || tempSens2 || tempSens3 || tempSens4 || tempSens5) {
    	result = "complete"	
    }
    result
}
def mDashboardD() {
    def text = "The Dashboard is not Configured"
    if (mLocalWeather || mWeatherConfig || ThermoStat1 || ThermoStat2 || tempSens1 || tempSens2 || tempSens3 || tempSens4 || tempSens5) { 
            text = "Tap here to view the Dashboard"
    }
    text
}
/** Main Intent Page **/
def mDevicesS() {def result = ""
    if (cSwitch || cFan || cDoor || cRelay || cTstat || cIndoor || cOutDoor || cVent || cMotion || cContact || cWater || cPresence || cSpeaker || cSynth || cMedia || cBattery) {
    	result = "complete"}
   		result}
def mDevicesD() {def text = "Tap here to configure settings" 
    if (cSwitch || cFan || cDoor || cRelay || cTstat || cIndoor || cOutDoor || cVent || cMotion || cContact || cWater || cPresence || cSpeaker || cSynth || cMedia || cBattery) {
    	text = "Configured"}
    	else text = "Tap to Configure"
		text}  
def mDefaultsS() {def result = ""
    if (cLevel || cVolLevel || cTemperature || cHigh || cMedium || cLow || cFanLevel || cLowBattery || cInactiveDev || cFilterReplacement || cFilterSynthDevice || cFilterSonosDevice) {
    	result = "complete"}
   		result}
def mDefaultsD() {def text = "Tap here to configure settings" 
    if (cLevel || cVolLevel || cTemperature || cHigh || cMedium || cLow || cFanLevel || cLowBattery || cInactiveDev || cFilterReplacement || cFilterSynthDevice || cFilterSonosDevice) {
    	text = "Configured"}
    	else text = "Tap to Configure"
		text}         
def mSecurityS() {def result = ""
    if (cMiscDev || cRoutines || uPIN_SHM || uPIN_Mode || fSecFeed || shmSynthDevice || shmSonosDevice || volume) {
    	result = "complete"}
   		result}
def mSecurityD() {def text = "Tap here to configure settings" 
    if (cMiscDev || cRoutines || uPIN_SHM || uPIN_Mode || fSecFeed || shmSynthDevice || shmSonosDevice || volume) {
    	text = "Configured"}
    	else text = "Tap to Configure"
		text}