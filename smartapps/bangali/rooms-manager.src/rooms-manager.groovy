/***********************************************************************************************************************
*
*  A SmartThings smartapp to create/view rooms created with rooms occupancy DTH.
*
*  Copyright (C) 2017 bangali
*
*  License:
*  This program is free software: you can redistribute it and/or modify it under the terms of the GNU
*  General Public License as published by the Free Software Foundation, either version 3 of the License, or
*  (at your option) any later version.
*
*  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the
*  implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
*  for more details.
*
*  You should have received a copy of the GNU General Public License along with this program.
*  If not, see <http://www.gnu.org/licenses/>.
*
*  Name: Rooms Manager
*  Source: https://github.com/adey/bangali/blob/master/smartapps/bangali/rooms-manager.src/rooms-manager.groovy
*
***********************************************************************************************************************/

public static String version()		{  return "v0.99.5"  }
private static boolean isDebug()	{  return false  }

import groovy.transform.Field

@Field final String msgSeparator   = '/'

@Field final String _SmartThings = 'ST'
@Field final String _Hubitat     = 'HU'

@Field final int    pauseMSecST = 10
@Field final int    pauseMSecHU = 50

@Field final String _ImgSize = '36'

@Field final String on	= 'on'
@Field final String off	= 'off'

@Field final List   _healthCheck = [1, 2, 3, 6, 12, 24]

definition (
	name: "rooms manager",
	namespace: "bangali",
	author: "bangali",
	description: "Create rooms",
	category: "My Apps",
	singleInstance: true,
	iconUrl: "https://cdn.rawgit.com/adey/bangali/master/resources/icons/roomOccupancy.png",
	iconX2Url: "https://cdn.rawgit.com/adey/bangali/master/resources/icons/roomOccupancy@2x.png",
	iconX3Url: "https://cdn.rawgit.com/adey/bangali/master/resources/icons/roomOccupancy@3x.png"
)

preferences	{
	page(name: "mainPage", content: "mainPage")
	page(name: "pageSpeakerSettings", content: "pageSpeakerSettings")
	page(name: "pagePersonNameSettings", content: "pagePersonNameSettings")
	page(name: "pagePersonColorSettings", content: "pagePersonColorSettings")
	page(name: "pageAnnouncementSpeakerTimeSettings", content: "pageAnnouncementSpeakerTimeSettings")
	page(name: "pageAnnouncementColorTimeSettings", content: "pageAnnouncementColorTimeSettings")
	page(name: "pageArrivalDepartureSettings", content: "pageArrivalDepartureSettings")
	page(name: "pageAnnouncementTextHelp", content: "pageAnnouncementTextHelp")
	page(name: "pageSunAnnouncementSettings", content: "pageSunAnnouncementSettings")
	page(name: "pageBatteryAnnouncementSettings", content: "pageBatteryAnnouncementSettings")
	page(name: "pageDeviceConnectivitySettings", content: "pageDeviceConnectivitySettings")
	page(name: "pageGithubSettings", content: "pageGithubSettings")
	page(name: "pageSMSSettings", content: "pageSMSSettings")
	page(name: "pageModeSettings", content: "pageModeSettings")
}

def mainPage()	{
	def appChildren = app.getChildApps().sort { it.label }
	def hT = getHubType()
	if (batteryCheckDevices)	batteryCheck([fromUI:true]);
	dynamicPage(name: "mainPage", title: (hT != _SmartThings ? 'Installed Rooms' : ''), install: false, uninstall: true, submitOnChange: true, nextPage: "pageSpeakerSettings")	{
		if ((batteryTime && state.batteryLowLevels) || (checkHealth && state.deviceConnectivity))
			section(hT == _Hubitat ? '<FONT COLOR="cc3333">ALERTS:</FONT>' : 'ALERTS:') {
				if (checkHealth && state.deviceConnectivity)	{
					paragraph "DEVICES THAT HAVE NOT CONNECTED WITHIN LAST $eventHours HOURS:"
					paragraph state.deviceConnectivity
				}
				if (batteryTime && state.batteryLowLevels)	{
					paragraph "DEVICES WITH BATTERY LEVEL BELOW $batteryLevel%:"
					paragraph state.batteryLowLevels
				}
			}
		section() {
			app(name: "rooms manager", appName: "rooms child app", namespace: "bangali", title: "New Room", multiple: true)
		}
		if (hT != 'HU')
			section {
				app(name: "rooms manager", appName: "rooms vacation", namespace: "bangali", title: "Vacation Manager", multiple: false)
			}
	}
}

def pageSpeakerSettings()	{
	def hT = getHubType()
	def playerDevice = (speakerDevices || speechDevices || musicPlayers || (hT == _SmartThings && (listOfMQs || echoSpeaksDevices)) ? true : false)
	def colorsList = colorsRGB.collect { [(it.key):it.value[1]] }
	def nameString = []
	def colorString = []
	for (def pit : presenceSensors)		{
		if (pit)	{
			nameString << (settings["${pit.getId()}Name"] ?: '')
			colorString << (settings["${pit.getId()}Color"] ? colorsRGB[settings["${pit.getId()}Color"]][1] : '')
		}
	}
	dynamicPage(name: "pageSpeakerSettings", title: "MAIN SETTINGS PAGE", install: true, uninstall: true)	{
		section()	{
			href "pageAnnouncementSpeakerTimeSettings", title: "${addImage(_SpokenImage)}Spoken announcement settings", description: (playerDevice ? "Tap to change existing settings" : "Tap to configure"), image: (hT != _Hubitat ? _SpokenImage : null)
		}
		section()	{
			href "pageAnnouncementColorTimeSettings", title: "${addImage(_ColorImage)}Color announcement settings", description: (announceSwitches ? "Tap to change existing settings" : "Tap to configure"), image: (hT != _Hubitat ? _ColorImage : null)
		}
		section()	{
			href "pageSMSSettings", title: "${addImage(_SMSImage)}SMS announcement settings", description: (startHHSms || endHHSms ? "Tap to change existing settings" : "Tap to configure"), image: (hT != _Hubitat ? _SMSImage : null)
		}
		section()	{
			href "pageModeSettings", title: "${addImage(_ModeImage)}Mode announcement settings", description: (speakModes || modeColor || announceInModes ? "Tap to change existing settings" : "Tap to configure"), image: (hT != _Hubitat ? _ModeImage : null)
		}
		section()	{
			href "pageArrivalDepartureSettings", title: "${addImage(_PresenceImage)}Arrival and departure settings", description: (speakerAnnounce || speakerAnnounceColor ? "Tap to change existing settings" : "Tap to configure"), image: (hT != _Hubitat ? _PresenceImage : null)
		}
		section()	{
			if (playerDevice)
				input "timeAnnounce", "enum", title: "${addImage(_TimeImage)}Announce time?", required: false, multiple: false, options: [[1:"Every 15 minutes"], [2:"Every 30 minutes"], [3:"Every hour"], [4:"No"]], image: (hT != _Hubitat ? _TimeImage : null)
			else
				paragraph "${addImage(_TimeImage)}Announce time?\nselect speaker devices to set."
		}
		section()	{
			href "pageSunAnnouncementSettings", title: "${addImage(_SunImage)}Sun announcement settings", description: (sunAnnounce ? "Tap to change existing settings" : "Tap to configure"), image: (hT != _Hubitat ? _SunImage : null)
		}
		section()	{
			href "pageBatteryAnnouncementSettings", title: "${addImage(_BatteryImage)}Battery level check settings", description: (batteryTime ? "Tap to change existing settings" : "Tap to configure"), image: (hT != _Hubitat ? _BatteryImage : null)
		}
		section()	{
			href "pageDeviceConnectivitySettings", title: "${addImage(_HealthImage)}Device connectivity check settings", description: (checkHealth ? "Tap to change existing settings" : "Tap to configure"), image: (hT != _Hubitat ? _HealthImage : null)
		}
		section()	{
			href "pageGithubSettings", title: "${addImage(_GitUpdateImage)}Github check settings", description: (gitTime ? "Tap to change existing settings" : "Tap to configure"), image: (hT != _Hubitat ? _GitUpdateImage : null)
		}
		section()	{
			href "", title: "${addImage(_GHimage)}Detailed readme on Github", style: "external", url: _gitREADME, description: "Click link to open in browser", image: (hT != _Hubitat ? _GHimage : null), required: false
		}
	}
}

private addImage(text)	{
	return (getHubType() == _Hubitat ? "<img src=$text height=$_ImgSize width=$_ImgSize>  " : '')
}

def pagePersonNameSettings()	{
	def namesList = []
	dynamicPage(name: "pagePersonNameSettings", title: "Presence sensor names:", install: false, uninstall: false)	{
		for (def pit : presenceSensors)		{
			def itID = pit.getId(), itDN = pit.getDisplayName()
			section()	{
				input "${itID}Name", "text", title: "$itDN Name?", required: true, multiple: false
			}
		}
	}
}

def pagePersonColorSettings()	{
	def colorsList = colorsRGB.collect { [(it.key):it.value[1]] }
	dynamicPage(name: "pagePersonColorSettings", title: "Choose notification color:", install: false, uninstall: false)		{
		for (def pit : presenceSensors)		{
			def itID = pit.getId(), itDN = pit.getDisplayName()
			section()	{
				input "${itID}Color", "enum", title: "$itDN Color?", required: true, multiple: false, options: colorsList
			}
		}
	}
}

def pageAnnouncementTextHelp()	{
	dynamicPage(name: "pageAnnouncementTextHelp", title: "ANNOUNCEMENT TEXT FORMAT", install: false, uninstall: false)	{
		section()	{
			paragraph "For announcement text all occurances of '&' is replaced with persons name(s)."
			paragraph "If there are multiple '$msgSeparator' separated strings in each announcement text input, then a random string will be used from that list of strings when announcing each time."
			paragraph "Similarly, all occurances of '&is' will be replaced with persons name(s) + ' is' or ' are' and '&has' with persons name(s) + ' has' or ' have'."
			paragraph "Choice of 'is' or 'are' and 'has' or 'have' is based on the number of person name(s) in the list for that announcement."
		}
	}
}

def pageAnnouncementSpeakerTimeSettings()	{
	def hT = getHubType()
	def playerDevice = (speakerDevices || speechDevices || musicPlayers || (hT == _SmartThings && (listOfMQs || echoSpeaksDevices)) ? true : false)
	dynamicPage(name: "pageAnnouncementSpeakerTimeSettings", title: "ANNOUNCE BY SPEAKER SETTINGS", install: false, uninstall: false)	{
		section()	{
			input "speakerDevices", "capability.audioNotification", title: "Which speakers?", required:false, multiple:true, submitOnChange:true
			input "speechDevices", "capability.speechSynthesis", title: "Which speech devices?", required:false, multiple:true, submitOnChange:true
			input "musicPlayers", "capability.musicPlayer", title: "Which media players?", required:false, multiple:true, submitOnChange:true
			if (ht == _SmartThings)		{
				input "listOfMQs", "enum", title:"Select Ask Alexa Message Queues", options:state.askAlexaMQ, multiple:true, required:false
				input "echoSpeaksDevices", "device.echoSpeaksDevice", title:"Echo Speaks?", multiple:true, required:false
			}
		}
		section()		{
			if (hT == _Hubitat)		paragraph subHeaders("Announcement Volume");
			if (playerDevice)	{
				input "speakerVolume", "number", title:"Speaker volume?", required:false, multiple:false, defaultValue:33, range:"1..100"
				input "useVariableVolume", "bool", title:"Use variable volume?", required:true, multiple:false, defaultValue:false
			}
			else		{
				paragraph "Speaker volume?\nselect any speaker to set"
				paragraph "Use variable volume?"
			}
		}
		section()	{
			if (hT == _Hubitat)		paragraph subHeaders("Announcement hours");
			if (playerDevice)	{
				input "startHH", "number", title: "Announce from hour?", description: "0..${(endHH < 24 ? endHH : 23)}", required: true, multiple: false, defaultValue: 7, range: "0..${(endHH < 24 ? endHH : 23)}", submitOnChange: true
				input "endHH", "number", title: "Announce to hour?", description: "${(startHH ? startHH + 1 : 0)}..24", required: true, multiple: false, defaultValue: 23, range: "${(startHH ? startHH + 1 : 0)}..24", submitOnChange: true
			}
			else		{
				paragraph "Announce from hour?\nselect speaker to set"
				paragraph "Announce to hour?"
			}
		}
	}
}

def pageAnnouncementColorTimeSettings()	{
	def hT = getHubType()
	dynamicPage(name: "pageAnnouncementColorTimeSettings", title: "ANNOUNCE WITH COLOR SETTINGS", install: false, uninstall: false)	{
		section()	{
			input "announceSwitches", "capability.switch", title: "Lights for announcement?", required: false, multiple: true, submitOnChange: true
		}
		section()	{
			if (announceSwitches)	{
				input "startHHColor", "number", title: "Announce from hour?", description: "0..${(endHHColor < 24 ? endHHColor : 23)}", required: true, multiple: false, defaultValue: 18, range: "0..${(endHHColor < 24 ? endHHColor : 23)}", submitOnChange: true
				input "endHHColor", "number", title: "Announce to hour?", description: "${(startHHColor ? startHHColor + 1 : 0)}..24", required: true, multiple: false, defaultValue: 23, range: "${(startHHColor ? startHHColor + 1 : 0)}..24", submitOnChange: true
			}
			else		{
				paragraph "Announce from hour?\nselect announce switches to set"
				paragraph "Announce to hour?"
			}
		}
	}
}

def pageSMSSettings()	{
	dynamicPage(name: "pageSMSSettings", title: "ANNOUNCE WITH SMS", install: false, uninstall: false)	{
		section()	{
			input "startHHSms", "number", title: "SMS from hour?", description: "0..${(endHHSms < 24 ? endHHSms : 23)}", required: true, multiple: false, defaultValue: 9, range: "0..${(endHHSms < 24 ? endHHSms : 23)}", submitOnChange: true
			input "endHHSms", "number", title: "SMS to hour?", description: "${(startHHSms ? startHHSms + 1 : 0)}..24", required: true, multiple: false, defaultValue: 21, range: "${(startHHSms ? startHHSms + 1 : 0)}..24", submitOnChange: true
			if (startHHSms && endHHSms)
				input "phoneNumber", "phone", title: "Phone number for SMS?", required: true
			else
				paragraph "Phone number for SMS?\nselect from and to hours to set"
		}
	}
}

def pageModeSettings()	{
	def hT = getHubType()
	def colorsList = colorsRGB.collect { [(it.key):it.value[1]] }
	def playerDevice = (speakerDevices || speechDevices || musicPlayers || (hT == _SmartThings && (listOfMQs || echoSpeaksDevices)) ? true : false)
	dynamicPage(name: "pageModeSettings", title: "ANNOUNCE MODE SETTINGS", install: false, uninstall: false)	{
		section()	{
			if (playerDevice)
				input "speakModes", "bool", title: "Announce mode changes with speaker?", required: false, defaultValue: false
			else
				paragraph "Announce mode changes with speaker?\nselect speaker to set"
			if (announceSwitches)
				input "modeColor", "enum", title: "Announce mode changes with color?", required: false, multiple: false, options: colorsList
			else
				paragraph "Announce mode changes with light?\nselect light to set"
		}
		section()	{
			if (playerDevice || announceSwitches)
				input "announceInModes", "mode", title: "Announcements only in modes?", required: false, multiple: true
			else
				paragraph "Announcements only in modes?\nselect speaker or light to set."
		}
	}
}

def pageArrivalDepartureSettings()	{
	def nameString = []
	def colorString = []
	for (def pit : presenceSensors)		{
		if (pit)	{
			nameString << (settings["${pit.getId()}Name"] ?: '')
			colorString << (settings["${pit.getId()}Color"] ? colorsRGB[settings["${pit.getId()}Color"]][1] : '')
		}
	}
	def hT = getHubType()
	def playerDevice = (speakerDevices || speechDevices || musicPlayers || (hT == _SmartThings && (listOfMQs || echoSpeaksDevices)) ? true : false)
	dynamicPage(name: "pageArrivalDepartureSettings", title: "PRESENCE ANNOUNCEMENT SETTINGS", install: false, uninstall: false)	{
		section()	{
			if (playerDevice)
				input "speakerAnnounce", "bool", title: "Announce presence with speaker?", required: false, multiple: false, defaultValue: false, submitOnChange: true
			else
				paragraph "Announce presence with speaker?\nselect speaker to set"
			if (announceSwitches)
				input "speakerAnnounceColor", "bool", title: "Announce presence with color?", required: false, multiple: false, defaultValue: false, submitOnChange: true
			else
				paragraph "Announce presence with color??\nselect announce with color light to set"
		}
		section()	{
			if (hT == _Hubitat)		paragraph subHeaders("Presences to announce");
			if (speakerAnnounce || speakerAnnounceColor)
				input "presenceSensors", "capability.presenceSensor", title: "Which presence sensors?", required: true, multiple: true, submitOnChange: true
			else
				paragraph "Which presence sensors?\nselect either announce with speaker or color to set"
			if (presenceSensors)
				href "pagePersonNameSettings", title: "Names for presence sensor(s)", description: "$nameString"
			else
				paragraph "Names for presence sensor(s)\nselect presence sensors to set"
			if (presenceSensors && speakerAnnounceColor)
				href "pagePersonColorSettings", title: "Colors for presence sensors", description: "$colorString"
			else
				paragraph "Colors for presence sensors\nselect announce with color to set"
		}
		section()	{
			if (hT == _Hubitat)		paragraph subHeaders("Announcement text");
			if (playerDevice && speakerAnnounce)	{
				href "pageAnnouncementTextHelp", title: "Announcement text format help:", description: "Click to read"
				input "welcomeHome", "text", title: "Welcome home greeting?", required: true, multiple: false, defaultValue: 'Welcome home &.'
				input "welcomeHomeCloser", "text", title: "Welcome home greeting closer?", required: false, multiple: false
				input "leftHome", "text", title: "Left home announcement?", required: true, multiple: false, defaultValue: '&has left home.'
				input "leftHomeCloser", "text", title: "Left home announcement closer?", required: false, multiple: false
			}
			else	{
				paragraph "Welcome home greeting?\nselect speaker announce to set."
				paragraph "Welcome home greeting closer?"
				paragraph "Left home announcement?"
				paragraph "Left home announcement closer?"
			}
		}
		section()	{
			if (hT == _Hubitat)		paragraph subHeaders("Announcement triggers");
			if (speakerAnnounce || speakerAnnounceColor)	{
				input "contactSensors", "capability.contactSensor", title: "Welcome home greeting when which contact sensors close?", required: (motionSensors ? false : true), multiple: true, submitOnChange: true
				input "motionSensors", "capability.motionSensor", title: "Welcome home greeting with motion on which motion sensors?", required: (contactSensors ? false : true), multiple: true, submitOnChange: true
				input "secondsAfter", "number", title: "Left home announcement how many seconds after?", required: true, multiple: false, defaultValue: 15, range: "5..100"
			}
			else	{
				paragraph "Welcome home greeting when which contact sensors close?\nselect announce to set"
				paragraph "Welcome home greeting with motion on which motion sensors?"
				paragraph "Left home announcement how many seconds after?"
			}
		}
	}
}

def pageSunAnnouncementSettings()	{
	def hT = getHubType()
	def colorsList = colorsRGB.collect { [(it.key):it.value[1]] }
	def playerDevice = (speakerDevices || speechDevices || musicPlayers || (hT == _SmartThings && (listOfMQs || echoSpeaksDevices)) ? true : false)
	dynamicPage(name: "pageSunAnnouncementSettings", title: "SUN ANNOUNCEMENT SETTINGS", install: false, uninstall: false)	{
		section()	{
			if (playerDevice || announceSwitches)
				input "sunAnnounce", "enum", title: "Sunrise/sunset announcement?", required: false, multiple: false, defaultValue: null, submitOnChange: true, options: [[null:"Neither"],[1:"Sunrise"],[2:"Sunset"],[3:"Both"]]
			else
				paragraph "Sunrise/sunset announcement?\nselect speaker or lights to set"
		}
		section()	{
			if (['1', '2', '3'].contains(sunAnnounce))
				input "speakSun", "bool", title: "Spoken announcement?", required: true, multiple: false, defaultValue: true
			else
				paragraph "Sunset color?\nset sunset announcement to set"
			if (['1', '3'].contains(sunAnnounce))
				input "sunriseColor", "enum", title: "Sunrise color?", required: false, multiple: false, options: colorsList
			else
				paragraph "Sunrise color?\nset sunrise announcement to set"
			if (['2', '3'].contains(sunAnnounce))
				input "sunsetColor", "enum", title: "Sunset color?", required: false, multiple: false, options: colorsList
			else
				paragraph "Sunset color?\nset sunset announcement to set"
		}
	}
}

def pageBatteryAnnouncementSettings()	{
	def colorsList = colorsRGB.collect { [(it.key):it.value[1]] }
	def hT = getHubType()
	def playerDevice = (speakerDevices || speechDevices || musicPlayers || (hT == _SmartThings && (listOfMQs || echoSpeaksDevices)) ? true : false)
	dynamicPage(name: "pageBatteryAnnouncementSettings", title: "BATTERY LEVEL CHECK SETTINGS", install: false, uninstall: false)	{
		section()	{
			if (playerDevice || announceSwitches)
				input "batteryTime", "time", title: "Annouce battery status when?", required: false, multiple: false, submitOnChange: true
			else
				paragraph "Annouce battery status when?\nselect speakers or switches to set"
		}
		section()	{
			if (batteryTime)
				input "batteryLevel", "number", title: "Battery level below which to include in status?", required: true, multiple: false, defaultValue: 33, range: "1..100"
			else
				paragraph "Battery level to include in status?\nselect battery time to set"
			if (batteryTime && announceSwitches)	{
				input "batteryOkColor", "enum", title: "Battery all OK color?", required: false, multiple: false, options: colorsList
				input "batteryLowColor", "enum", title: "Battery low color?", required: true, multiple: false, options: colorsList
			}
			else		{
				paragraph "Battery all OK warning color?\nselect battery time to set"
				paragraph "Battery low warning color?\nselect battery time to set"
			}
			if (batteryTime && phoneNumber)
				input "batterySms", "bool", title: "Send SMS notification on low battery?", required: true, defaultValue: false
			else
				paragraph "Send SMS notification on low battery?\nselect announce battery status time to set"
		}
		section()	{
			if (hT == _Hubitat)		paragraph subHeaders("Battery Devices to check");
			if (batteryTime)	{
				input "batteryCheckDevices", "capability.battery", title: "Check which battery devices?", required:false, multiple:true, submitOnChange:true
				if (batteryCheckDevices)	paragraph (state.batteryLowLevels ?: state.batteryLevels);
			}
			else
				paragraph "Check which battery devices?\nselect announce battery status time to set"
		}
	}
}

def pageDeviceConnectivitySettings()	{
	def colorsList = colorsRGB.collect { [(it.key):it.value[1]] }
	def hT = getHubType()
	def playerDevice = (speakerDevices || speechDevices || musicPlayers || (hT == _SmartThings && (listOfMQs || echoSpeaksDevices)) ? true : false)
	dynamicPage(name: "pageDeviceConnectivitySettings", title: "DEVICE CONNECTIVITY CHECK SETTINGS", install: false, uninstall: false)	{
		section()		{
			input "checkHealth", "bool", title: "Device connectivity check?", required: false, defaultValue: false, submitOnChange: true
			if (checkHealth)		{
				input "healthMinsPast", "enum", title: "At which minute past the hour?", required: true, defaultValue: 37, options: [[2:"02 past hour"], [3:"03 past hour"], [5:"05 past hour"], [7:"07 past hour"], [11:"11 past hour"], [13:"13 past hour"], [17:"17 past hour"], [19:"19 past hour"], [23:"23 past hour"], [29:"29 past hour"], [31:"31 past hour"], [37:"37 past hour"], [41:"41 past hour"], [43:"43 past hour"], [47:"47 past hour"], [53:"53 past hour"], [59:"59 past hour"]]
				input "eventHours", "enum", title: "Device event within how many hours?", required: true, multiple: false, options: [12:"12 hours", 24:"24 hours", 48:"48 hours", 72:"72 hours"]
			}
			else		{
				paragraph "At which minute past the hour?\nselect connectivity check to set"
				paragraph "Device event within how many hours?"
			}
		}
		section()	{
			if (checkHealth && (playerDevice || announceSwitches))
				input "healthEvery", "enum", title: "Spoken announcement: Every how many hours?", required: true, multiple: false, defaultValue: 0, options: [0:"No spoken announcement", 1:"1 hour", 2:"2 hours", 3:"3 hours", 6:"6 hours", 12:"12 hours", 24:"24 hours"]
			else
				paragraph "Spoken announcement: Every how many hours?\nselect connectivity check and notification devices to set"
		}
		section()	{
			if (checkHealth && announceSwitches)	{
				input "healthOkColor", "enum", title: "Color announcement: Device connectivity OK color?", required: false, multiple: false, options: colorsList
				input "healthWarnColor", "enum", title: "Color announcement: Device connectivity warning color?", required: false, multiple: false, options: colorsList
			}
			else		{
				paragraph "Color announcement: Device connectivity OK color?\nselect connectivity check and lights to set"
				paragraph "Color announcement: Device connectivity warning color?"
			}
		}
		section()	{
			if (hT == _Hubitat)		paragraph subHeaders("Devices to check");
			if (checkHealth)
				input "healthAddDevices", "capability.${(hT == _Hubitat ? '*' : 'sensor')}", title: "Check which devices?", required: true, multiple: true
			else
				paragraph "Check which devices?\nselect device connectivity to set"
		}
		section()	{
			if (hT == _Hubitat)		paragraph subHeaders("Health critical");
			if (checkHealth && phoneNumber)
				input "healthCriticalNotification", "bool", title: "Critical devices: Send SMS if connectivity check fails?", required: false, defaultValue: false, submitOnChange: true
			else
				paragraph "Critical devices: Send SMS if check fails?\nselect device connectivity to set"
			if (checkHealth && phoneNumber && healthCriticalNotification)
				input "healthCriticalDevices", "capability.${(hT == _Hubitat ? '*' : 'sensor')}", title: "Critical devices: Which critical devices?", required: (!!healthCriticalNotification), multiple: true
			else
				paragraph "Critical devices: Which critical devices?${(!(checkHealth && phoneNumber) ? '' : "\nselect device connectivity to set" )}"
		}
	}
}

def pageGithubSettings()	{
	dynamicPage(name: "pageGithubSettings", title: "GITHUB UPDATE NOTIFICATION", install: false, uninstall: false)	{
		section()	{
			if (phoneNumber)	{
				input "gitTime", "time", title: "At time of day?", required: false, submitOnChange: true
				input "gitRepeat", "bool", title: "Repeat GITHUB updated notification?", required: !!gitTime
			}
			else		{
				paragraph "At time of day?\nselect phone number to set"
				paragraph "Repeat GITHUB updated notification?"
			}
		}
	}
}

private subHeaders(str)		{
	if (str.size() > 50)	str = str.substring(0, 50);
	return "<div style='text-align:center;background-color:#bbbbbb;color:#ffffff;'>${str.toUpperCase().center(50)}</div>"
}

def installed()		{ initialize() }

def updated()		{
	def nowTime = now()
	ifDebug("updated", 'info')
	initialize()
	app.updateLabel(app.name + ' - ' + version())
	def hT = getHubType()
	announceSetup()
	if (hT == _SmartThings)		{
		subscribeToRoomsP()
		subscribe(location, "askAlexaMQ", askAlexaMQHandler)
	}
	ifDebug("there are ${childApps.size()} rooms.", 'info')
	updateVaca()
	spawnChildSettings()
	if (announceSwitches && ['1', '3'].contains(sunAnnounce))		subscribe(location, "sunrise", sunriseEventHandler);
	if (announceSwitches && ['2', '3'].contains(sunAnnounce))		subscribe(location, "sunset", sunsetEventHandler);
	if (speakerDevices)	{
		subscribe(speakerDevices, "status", speakerStoppedEventHandler)
		subscribe(speakerDevices, "status", speakerStoppedEventHandler)
	}
	if (musicPlayers)		{
		subscribe(musicPlayers, "status", musicStoppedEventHandler);
		subscribe(musicPlayers, "status", musicStoppedEventHandler);
	}

	githubUpdated(true)
	state.healthHours = 0
	scheduleNext()
	subscribe(location, "mode", modeEventHandler)
	state.connected = [devices:[], inRun:false, runNumber:0]
	state.rSH = null
//log.debug "perf updated: ${now() - nowTime} ms"
}

private subscribeToRoomsP()	{
	def nowTime = now()
	def rDOs = getChildRoomOccupancyDeviceObjects()
	ifDebug("there are ${rDOs.size()} occupancy devices.", 'info')
	for (def rD : rDOs)		subscribe(rD, "occupancy", forSTRelatedSmartApps);
//log.debug "perf subscribeToRoomsP: ${now() - nowTime} ms"
}

def forSTRelatedSmartApps(evt)		{}

def updateVaca()	{
	def vacaApp = spawnVacationApp()
	vacaApp.subscribeToRooms()
}

def triggerSubscribeToVaca()	{
	runOnce(new Date(now() + 10), updateVaca)
}

def initialize()	{
	ifDebug("initialize", 'info')
	unsubscribe()
	unschedule()
	if (settings["healthNoCheckDevices"])		app.updateSetting("healthNoCheckDevices", []);
	if (state.healthNoCheckDevices)			state.remove('healthNoCheckDevices');
	if (settings["noBatteryCheckDevices"])		app.updateSetting("noBatteryCheckDevices", []);
	if (state.noBatteryCheckDevices)		state.remove('noBatteryCheckDevices');
	if (settings["noCheckDevices"])				app.updateSetting("noCheckDevices", []);
	if (state.noCheckDevices)				state.remove('noCheckDevices');
	if (settings["noHealthCheckDevices"])		app.updateSetting("noHealthCheckDevices", []);
	if (state.noHealthCheckDevices)			state.remove('noHealthCheckDevices');
	if (state.rSH)		state.remove('rSH');
	state.colorsRotating = false
	state.colorNotificationColor = null
	state.colorNotificationColorStack = []
	state.lastBatteryUpdate = ''
	state.audioData = [:]
	state.speaking = false
	state.rSH = null
	sendLocationEvent(name: "AskAlexaMQRefresh", isStateChange: true)
}

def scheduleNext(data)		{
	def nowTime = now()
	if (data?.option)	{
log.debug data
		if (data.option.contains('time'))		runIn(0, tellTime);
		if (data.option.contains('battery'))	runIn(0, batteryCheck);
		if (data.option.contains('health'))		runIn(0, checkDeviceHealth);
		if (data.option.contains('git'))		runIn(0, githubUpdated);
	}
	def nowDate = new Date(now())
	def cHH = nowDate.format("HH", location.timeZone).toInteger()
	def cMM = nowDate.format("mm", location.timeZone).toInteger()

	state.schedules = []
	if (batteryTime)	{
		def x = timeToday(batteryTime, location.timeZone)
//		def bTime = timeTodayA(nowDate, timeToday(String.format("%02d:%02d", x.format("HH", location.timeZone).toInteger(), x.format("mm", location.timeZone).toInteger()), location.timeZone), location.timeZone)
		def bTime = timeToday(String.format("%02d:%02d", x.format("HH", location.timeZone).toInteger(), x.format("mm", location.timeZone).toInteger()), location.timeZone)
		state.schedules << [type:'battery', time:bTime, timeS:(bTime.format("HH:mm", location.timeZone))]
	}
	if (checkHealth)	{
		def hMP = healthMinsPast.toInteger()
//		def hTime = timeTodayA(nowDate, timeToday(String.format("%02d:%02d", cHH, hMP), location.timeZone), location.timeZone)
		def hTime = timeToday(String.format("%02d:%02d", cHH, hMP), location.timeZone)
//		def hTime = timeTodayA(nowDate, timeToday(String.format("%02d:%02d", (hMP > cMM ? cHH : (cHH == 23 ? 0 : cHH + 1)), hMP), location.timeZone), location.timeZone)
		state.schedules << [type:'health', time:hTime, timeS:(hTime.format("HH:mm", location.timeZone))]
	}
	if (gitTime)	{
		def x = timeToday(gitTime, location.timeZone)
		def gTime = timeTodayA(nowDate, timeToday(String.format("%02d:%02d", x.format("HH", location.timeZone).toInteger(), x.format("mm", location.timeZone).toInteger()), location.timeZone), location.timeZone)
		state.schedules << [type:'git', time:gTime, timeS:(gTime.format("HH:mm", location.timeZone))]
	}
	def tTM = (cMM >= 45 ? 0 : (((cMM / 15).intValue() * 15) + 15))
	def	tTime = timeTodayA(nowDate, timeToday(String.format("%02d:%02d", (tTM == 0 ? (cHH == 23 ? 0 : cHH + 1) : cHH), tTM), location.timeZone), location.timeZone)
	state.schedules << [type:'time', time:tTime, timeS:(tTime.format("HH:mm", location.timeZone))]
//log.debug state.schedules

	def rTime = false
	def rTimeS
	def rOpts = []
	def nD = new Date(now() + (30 * 1000L))
	for (def schedule : state.schedules)	{
		if (schedule.time.before(nD))	continue;
		if (!rTime)		{
			rTime = schedule.time
			rTimeS = schedule.timeS
			rOpts << schedule.type;
			continue
		}
		if (rTimeS == schedule.timeS)	{
			rOpts << schedule.type
			continue
		}
		if (schedule.time.before(rTime))	{
			rTime = schedule.time
			rTimeS = schedule.timeS
			rOpts = [(schedule.type)];
		}
	}
//log.debug state.schedules
//log.debug "rTime: $rTime | rTimeS: $rTimeS | rOpts : $rOpts"
	if (rTime)	{
		state.schedules << [type:'next', time:rTime, timeS:rTimeS, options:"$rOpts"]
log.debug state.schedules
		runOnce(new Date(rTime.getTime() + new Random().nextInt(3000)), scheduleNext, [data: [option: "$rOpts"]])
	}
log.debug "perf scheduleNext: ${now() - nowTime} ms"
}

// since hubitat does not support timeTodayAfter(...) 2018-04-08
private timeTodayA(whichDate, thisDate, timeZone)	{
	def newDate
	if (thisDate.before(whichDate))	{
		newDate = thisDate.plus(((whichDate.getTime() - thisDate.getTime()) / 86400000L).intValue() + 1)
	}
	else
		newDate = thisDate
	return newDate
}

private getHubType()	{
	if (!state.hubId)	state.hubId = location.hubs[0].id.toString();
	return (state.hubId.length() > 5 ? _SmartThings : _Hubitat)
}

def askAlexaMQHandler(evt)	{
	if (evt)
		switch (evt.value)	{
			case "refresh":	(state.askAlexaMQ = evt.jsonData && evt.jsonData?.queues ? evt.jsonData.queues : []);	break;
		}
}

def unsubscribeChildRoomDevice(appChildDevice)	{
	ifDebug("unsubcribe: room: ${appChildDevice.label} id: ${appChildDevice.id}", 'info')
	if (getHubType() == _Hubitat)
		ifDebug("Hubitat does not yet support unsubscribing to a single device so removing a room requires a manual step.\n\
                 From Hubitat portal please go to devices and find the corresponding rooms occupancy device and remove it.\n\
                 Once the device is removed, from rooms manager app remove the room to complete uninstallation of the room.")
	else
		unsubscribe(appChildDevice)
}

private announceSetup()	{
	state.whoCameHome = [:]
	state.whoCameHome.personsIn = []
	state.whoCameHome.personsOut = []
	state.whoCameHome.personNames = [:]
	state.personsColors = [:]
	state.colorsToRotate = [:]
	if (!speakerAnnounce && !speakerAnnounceColor)		return;
	for (def pit : presenceSensors)		{
		def itID = pit.getId()
		state.whoCameHome.personNames << [(itID):settings["${itID}Name"]]
		if (speakerAnnounceColor)	{
			def hue = convertRGBToHueSaturation(settings["${itID}Color"])
			state.personsColors << [(itID):hue]
		}
	}
	if (presenceSensors)	{
		subscribe(presenceSensors, "presence.present", presencePresentEventHandler)
		subscribe(presenceSensors, "presence.not present", presenceNotPresentEventHandler)
	}
	if (contactSensors)		subscribe(contactSensors, "contact.closed", contactClosedEventHandler);
	if (motionSensors)		subscribe(motionSensors, "motion.active", contactClosedEventHandler);
	state.welcomeHome = splitStr(welcomeHome)
	state.welcomeHomeCloser = splitStr(welcomeHomeCloser)
	state.leftHome = splitStr(leftHome)
	state.leftHomeCloser = splitStr(leftHomeCloser)
}

private splitStr(str)	{
	def map = [:]
	if (str)	{
		def split = str.split(msgSeparator)
		def i = 0
		for (def s : split)		{
			map[i] = s; i = i + 1;
		}
	}
	return map
}

def getRoomDevices(rooms)	{
	def roomsDevices = [:]
	for (def c : childApps)		{
		if (rooms.contains(c.id.toString()) && c.name != 'rooms vacation' && c.name != 'rooms child settings')
			roomsDevices << [(c.getChildRoomOccupancyDeviceC().deviceNetworkId):[id:(c.id), label:(c.label)]]
	}
	return roomsDevices
}

def getChildRoomOccupancyDeviceObject(childID)	{
	def roomDeviceObject = null
	for (def c : childApps)		{
		if (childID == c.id)		{
			roomDeviceObject = c.getChildRoomOccupancyDeviceC()
			break
		}
	}
//    ifDebug("getChildRoomOccupancyDeviceObject: childID: $childID | roomDeviceObject: $roomDeviceObject")
	return roomDeviceObject
}

def getChildRoomOccupancyDeviceObjects()		{
	def roomDeviceObjects = []
	for (def c : childApps)	{
		if (c.name != 'rooms vacation' && c.name != 'rooms child settings')
			roomDeviceObjects << c.getChildRoomOccupancyDeviceC()
	}
	return roomDeviceObjects
}

def checkThermostatValid(childID, checkThermostat)	{
	ifDebug("checkThermostatValid: $checkThermostat", 'info')
	if (!checkThermostat)		return null;
	def otherRoom = null
	for (def child : childApps)		{
		if (childID != child.id && (child.name != 'rooms vacation' && child.name != 'rooms child settings'))	{
			def thermo = child.getChildRoomThermostat()
			if (thermo && checkThermostat.getId() == thermo.thermostat.getId())		otherRoom = thermo.name;
		}
	}
	return otherRoom
}

def	presencePresentEventHandler(evt)	{  whoCameHome(evt.device)  }

def	presenceNotPresentEventHandler(evt)	{  whoCameHome(evt.device, true)  }

def contactClosedEventHandler(evt = null)	{
	if ((evt && !state.whoCameHome.personsIn) || (!evt && !state.whoCameHome.personsOut))		return;
	def rand = new Random()
	def k = (state.welcomeHome ? Math.abs(rand.nextInt() % state.welcomeHome.size()) : 0) + ''
	def k2 = (state.welcomeHomeCloser ? Math.abs(rand.nextInt() % state.welcomeHomeCloser.size()) : 0) + ''
	def l = (state.leftHome ? Math.abs(rand.nextInt() % state.leftHome.size()) : 0) + ''
	def l2 = (state.leftHomeCloser ? Math.abs(rand.nextInt() % state.leftHomeCloser.size()) : 0) + ''
	def persons = ''
	def str = (evt ? state.whoCameHome.personsIn : state.whoCameHome.personsOut)
	def i = str.size()
	def multiple = (i > 1 ? true : false)
	def j = 1
	for (def s : str)	{
		persons = persons + (j != 1 ? (j == i ? ' and ' : ', ') : '') + s
		j = j + 1
	}
	str = (evt ? (state.welcomeHome[(k)] ?: '') : (state.leftHome[(l)] ?: '')) + ' ' +
			(evt ? (state.welcomeHomeCloser[(k2)] ?: '') : (state.leftHomeCloser[(l2)] ?: ''))
	for (special in ['&is', '&are', '&has', '&have', '&'])	{
		def str2 = str.split(special)
		str = ''
		for (i = 0; i < str2.size(); i++)	{
			def replaceWith
			switch(special)	{
				case '&':       replaceWith = persons;		break
				case ['&is', '&are']:     replaceWith = persons + (multiple ? ' are' : ' is');		break
				case ['&has', '&have']:   replaceWith = persons + (multiple ? ' have' : ' has');	break
				default:        replaceWith = 'unknown';	break
			}
			str = str + str2[i] + (i != (str2.size() -1) ? ' ' + replaceWith : '')
		}
		if (!str)	str = str2;
	}
	speakIt(str)
	if (evt)	state.whoCameHome.personsIn = [];
	else		state.whoCameHome.personsOut = [];
	if (speakerAnnounceColor)	setupColorRotation();
}

def speakerStoppedEventHandler(evt)	{
log.debug evt.value
log.debug state.audioData
	if (evt.value != 'stopped')		return;
	def dNI = evt.device.deviceNetworkId.toString()
	if (!state.audioData?."$dNI")		return;
	def sC = state.audioData."$dNI".stopCount
	if (sC > 0)		{
		state.audioData."$dNI".stopCount = sC - 1
		return
	}
	def hT = getHubType()
	evt.device.setLevel(state.audioData."$dNI".volume)
	if (state.audioData."$dNI".trackData?.trackUri && state.audioData."$dNI".status == 'playing')
		evt.device.playTrack(state.audioData."$dNI".trackData.trackUri)
	if (state.audioData."$dNI".mute != 'unmuted')		evt.device.mute();
	state.audioData.remove(dNI)
}

def musicStoppedEventHandler(evt)	{
log.debug evt.value
log.debug state.audioData
	if (evt.value != 'stopped')		return;
	def dNI = evt.device.deviceNetworkId.toString()
	if (!state.audioData."$dNI")		return;
	def sC = state.audioData."$dNI".stopCount
	if (sC > 0)		{
		state.audioData."$dNI".stopCount = sC - 1
		return
	}
	def hT = getHubType()
	evt.device.setLevel(state.audioData."$dNI".volume)
	if (state.audioData."$dNI".trackData?.trackUri && state.audioData."$dNI".status == 'playing')
		evt.device.playTrack(state.audioData."$dNI".trackData.trackUri)
	if (state.audioData."$dNI".mute != 'unmuted')		evt.device.mute();
	state.audioData.remove(dNI)
}

private speakIt(str)	{
	ifDebug("speakIt", 'info')
	if (announceInModes && !(announceInModes.contains(location.currentMode.toString())))	return false;
	def hT = getHubType()
	if (!(speakerDevices || speechDevices || musicPlayers || (hT == _SmartThings && (listOfMQs || echoSpeaksDevices))))		return;
	def nowDate = new Date(now())
	def intCurrentHH = nowDate.format("HH", location.timeZone) as Integer
	def intCurrentMM = nowDate.format("mm", location.timeZone) as Integer
	ifDebug("$intCurrentHH | $intCurrentMM | $startHH | $endHH")
	if (intCurrentHH < startHH || (intCurrentHH > endHH || (intCurrentHH == endHH && intCurrentMM != 0)))		return;
	if (state.speaking)		{
		runOnce(new Date(now() + 10000), speakRetry, [overwrite: false, data: [str: str]])
		return
	}
	state.speaking = true
	def vol = speakerVolume
	if (useVariableVolume)	{
		int x = startHH + ((endHH - startHH) / 4)
		int y = endHH - ((endHH - startHH) / 3)
		if (intCurrentHH <= x || intCurrentHH >= y)		vol = (speakerVolume - (speakerVolume / 3)).toInteger();
	}
	state.audioData = [:]
	if (speakerDevices)	{
		saveAudioData(speakerDevices)
		speakerDevices.unmute()
		if (hT != _Hubitat)
			speakerDevices.playTextAndResume(str, vol)
		else	{
			speakerDevices.setLevel(vol)
			speakerDevices.speak(str)
		}
	}
	if (speechDevices)		speechDevices.speak(str);
	if (musicPlayers)	{
		saveAudioData(musicPlayers)
		musicPlayers.unmute()
		if (hT != _Hubitat)
			musicPlayers.playTrackAndResume(str, vol)
		else	{
			musicPlayers.setLevel(vol)
			musicPlayers.speak(str)
		}
	}
	if (hT == _SmartThings)		{
		if (listOfMQs)
			sendLocationEvent(name: "AskAlexaMsgQueue", value: "Rooms Occupancy", isStateChange: true,  descriptionText: "$str", data:[queues:listOfMQs, expires: 30, notifyOnly: true, suppressTimeDate: true])
		if (echoSpeaksDevices)
			echoSpeaksDevices.speak(str)
	}
	state.speaking = false
}

def speakRetry(data)	{
	if (data.str)		speakIt(data.str);
}

private saveAudioData(players)	{
	def hT = getHubType()
	for (def p : players)	{
		def pDNI = p.deviceNetworkId.toString()
		def pS = p.currentStatus
		state.audioData."$pDNI" = [status:pS]
		def sC = (pS == 'playing' ? (hT == _SmartThings ? 2 : 3) : (hT == _SmartThings ? 0 : 1))
		state.audioData."$pDNI" << [stopCount:sC]
		if (hT == _Hubitat)		{
			def aT = p.currentTrackData
			if (aT)		{
				def tD = new groovy.json.JsonSlurper().parseText(aT)
				if (tD)		{
					assert tD instanceof Map;
					state.audioData."$pDNI".trackData = tD
				}
				else
					state.audioData."$pDNI".trackData = null
			}
			else
				state.audioData."$pDNI".trackData = null
		}
		state.audioData."$pDNI" << [volume:(p.currentLevel)]
		state.audioData."$pDNI" << [mute:(p.currentMute)]
	}
}

private setupColorRotation()	{
	if (!announceSwitches || (announceInModes && !(announceInModes.contains(location.currentMode.toString()))))		return false;
	state.colorsRotateSeconds = state.colorsToRotate.size() * 30
	if (!state.colorsRotating)	{
		state.colorsRotating = true
		saveAnnounceSwitches()
		state.colorsIndex = 0
		announceSwitches.on()
//        announceSwitches.setLevel(99)
		rotateColors()
	}
}

def rotateColors()	{
	ifDebug("rotateColors", 'info')
	ifDebug("$state.colorsIndex | $state.colorsRotateSeconds | ${state.colorsToRotate."$state.colorsIndex"}")
	announceSwitches.setColor(state.colorsToRotate."$state.colorsIndex")
	state.colorsRotateSeconds = (state.colorsRotateSeconds >= 5 ? state.colorsRotateSeconds - 5 : 0)
	state.colorsIndex = (state.colorsIndex < (state.colorsToRotate.size() -1) ? state.colorsIndex + 1 : 0)
	if (state.colorsRotateSeconds > 0)
		runOnce(new Date(now() + 5000), rotateColors)
	else		{
		restoreAnnounceSwitches()
		state.colorsRotating = false
		state.colorsToRotate = [:]
	}
}

private whoCameHome(presenceSensor, left = false)	{
	if (!presenceSensor)	return;
	def pID = presenceSensor.getId()
	def presenceName = state.whoCameHome.personNames[(pID)]
	if (!presenceName)		return;
	ifDebug("presenceName: $presenceName | left: $left", 'info')
	if (!left)	{
		long nowTime = now()
		if (state.whoCameHome.personsIn)	{
			long howLong = nowTime - state.whoCameHome.lastOne
			if (howLong > 300000L)		state.whoCameHome.personsIn = [];
		}
		state.whoCameHome.lastOne = nowTime
		if (!state.whoCameHome.personsIn || !(state.whoCameHome.personsIn.contains(presenceName)))
			state.whoCameHome.personsIn << presenceName
	}
	else	{
		if (!state.whoCameHome.personsOut || !(state.whoCameHome.personsOut.contains(presenceName)))
			state.whoCameHome.personsOut << presenceName
		runOnce(new Date(now() + (secondsAfter * 1000)), contactClosedEventHandler)
	}
	if (speakerAnnounceColor)		state.colorsToRotate << [(state.colorsToRotate.size()):state.personsColors[(pID)]];
}

def getRoomNames(childID)	{
	def roomNames = [:]
	for (def c : childApps)
		if (childID != c.id && c.name != 'rooms vacation' && c.name != 'rooms child settings')		roomNames << [(c.id):(c.label)];
	return (roomNames.sort { it.value })
}

def handleAdjRooms()	{
	ifDebug("handleAdjRooms >>>>>", 'info')
	def time = now()
	for (def c : childApps)		{
		if (c.name == 'rooms vacation' || c.name == 'rooms child settings')		continue;
		def childID = c.id
		def adjRooms = c.getAdjRoomsSetting()
		def adjMotionSensors = []
		if (adjRooms)	{
			def adjMotionSensorsIds = []
			for (def c2 : childApps)		{
				if (c2.name == 'rooms vacation' || c2.name == 'rooms child settings')		continue;
				if (adjRooms.grep{it.toString() == c2.id.toString()})	{
					def mS = c2.getAdjMotionSensors()
					for (def m : mS)	{
						def motionSensorId = m.getId()
						if (!adjMotionSensorsIds.contains(motionSensorId))	{
							adjMotionSensors << m
							adjMotionSensorsIds << motionSensorId
						}
					}
				}
			}
		}
		ifDebug("rooms manager: updating room $c.label with adjacent motion sensors: $adjMotionSensors | ${now() - time} ms", 'info')
		c.updateRoomAdjMS(adjMotionSensors)
	}
	ifDebug("handleAdjRooms <<<<<", 'info')
//	return true
}

def getLastStateDate(childID)	{
	def lastStateDate = [:]
	if (childID)
		for (def child : childApps)
			if (childID.toString() == child.id.toString())	{
				lastStateDate = child.getLastStateChild()
				break
			}
	return lastStateDate
}

def getCurrentState(childID)	{
	def currentState = null
	if (childID)
		for (def child : childApps)
			if (childID.toString() == child.id.toString())	{
				currentState = child.getLastStateChild()?.state
				break
			}
	return currentState
}

def batteryCheck(fromUI = [:])	{
	ifDebug("batteryCheck", 'info')
	def nowTime = now()
	state.batteryLevels = ''
	state.batteryLowLevels = ''

	def bat
	def batteryNames = ''
	def batteryLow = 0
	def cnt = batteryCheckDevices.size()
	for (def bit : batteryCheckDevices)	{
		bat = bit.currentBattery
		if (bat < batteryLevel)		{
			batteryLow = batteryLow + 1
			batteryNames = batteryNames + (batteryNames ? ', ' : '') + (bit.displayName ?: bit.name) + ":${(bat ?: 0)}"
		}
		state.batteryLevels = state.batteryLevels + (state.batteryLevels ? ', ' : '') + (bit.displayName ?: bit.name) + ":${(bat ?: 0)}"
	}
	if (batteryNames)		{
		batteryNames = addAnd(batteryNames)
		state.batteryLowLevels = batteryNames
	}
	if (state.batteryLevels)	state.batteryLevels = addAnd(state.batteryLevels);
	if (fromUI)		return;

	if (announceSwitches && ((batteryLow == 0 && batteryOkColor) || (batteryLow > 0 && batteryLowColor)))
		setupColorNotification(convertRGBToHueSaturation((colorsRGB[(batteryLow > 0 ? batteryLowColor : batteryOkColor)][1])))
	state.lastBatteryUpdate = ( batteryNames?.trim() ? "the following battery devices are below $batteryLevel percent $batteryNames." : "no device battery below $batteryLevel percent.")
	speakIt(state.lastBatteryUpdate);
	if (batterySms && batteryLow > 0)		roomsSMS("Rooms Manager: $batteryLow ${(batteryLow > 1 ? 'batteries are' : 'battery is')} low.");
//log.debug "perf batteryCheck: ${now() - nowTime} ms, checked $cnt devices"
}

def modeEventHandler(evt)	{
	ifDebug("modeEventHandler", 'info')
	if (modeColor)	{
////		state.colorNotificationColor = convertRGBToHueSaturation((colorsRGB[modeColor][1]))
		setupColorNotification(convertRGBToHueSaturation((colorsRGB[modeColor][1])))
	}
	if (speakModes)		speakIt(' Location mode changed to ' + location.currentMode.toString() + '. ')
	scheduleNext()
}

def sunriseEventHandler(evt = null)	{
	ifDebug("sunriseEventHandler", 'info')
////	state.colorNotificationColor = convertRGBToHueSaturation((colorsRGB[sunriseColor][1]))
	setupColorNotification(convertRGBToHueSaturation((colorsRGB[sunriseColor][1])))
	if (speakSun)		speakIt(' Sun rise, time is ' + format24hrTime() + ' hours. ')
	scheduleNext()
}

def sunsetEventHandler(evt = null)	{
	ifDebug("sunsetEventHandler", 'info')
////	state.colorNotificationColor = convertRGBToHueSaturation((colorsRGB[sunsetColor][1]))
	setupColorNotification(convertRGBToHueSaturation((colorsRGB[sunsetColor][1])))
	if (speakSun)		speakIt(' Sun set, time is ' + format24hrTime() + ' hours. ');
	scheduleNext()
}

private format24hrTime(timeToFormat = new Date(now()), format = "HH:mm")		{
	return timeToFormat.format("HH:mm", location.timeZone)
}

def setupColorNotification(color = null)	{
	ifDebug("setupColorNotification", 'info')
	unschedule('setupColorNotification')
	def nowDate = new Date(now())
	def intCurrentHH = nowDate.format("HH", location.timeZone) as Integer
	def intCurrentMM = nowDate.format("mm", location.timeZone) as Integer
	if ((!announceSwitches || (announceInModes && !(announceInModes.contains(location.currentMode.toString())))) ||
		(intCurrentHH < startHHColor || (intCurrentHH > endHHColor || (intCurrentHH == endHHColor && intCurrentMM != 0))))	{
		state.colorNotificationColorStack = []
		return false
	}
	if (!state.colorsRotating && state.colorNotifyTimes <= 0)	{
		state.colorNotifyTimes = 9
		saveAnnounceSwitches()
		if (!color)		{
			if (state.colorNotificationColorStack)		{
				color = state.colorNotificationColorStack[0]
				state.colorNotificationColorStack.remove(0)
			}
		}
		if (color)		{
			state.colorNotificationColor = color
			notifyWithColor()
		}
	}
	else	{
		if (color)		state.colorNotificationColorStack << color;
		runOnce(new Date(now() + 10000), setupColorNotification)
	}
}

def notifyWithColor()	{
//    ifDebug("notifyWithColor", 'info')
	if (state.colorNotifyTimes % 2)	{
		announceSwitches.on()
		announceSwitches.setColor(state.colorNotificationColor)
	}
	else
		announceSwitches.off()
	unschedule('notifyWithColor')
	state.colorNotifyTimes = state.colorNotifyTimes - 1
	if (state.colorNotifyTimes > 0)
		runOnce(new Date(now() + 1000), notifyWithColor)
	else	{
		restoreAnnounceSwitches()
		if (state.colorNotificationColorStack)
			runOnce(new Date(now() + (getHubType() == _SmartThings ? 500 : 5000)), setupColorNotification)
	}
}

private saveAnnounceSwitches()		{
	unschedule('setAnnounceSwitches')
	state.colorSwitchSave = []
	state.colorColorSave = []
	state.colorColorTemperatureSave = []
	state.colorColorTemperatureTrueSave = []
	for (def swt : announceSwitches)	{
		state.colorSwitchSave << swt.currentSwitch
		state.colorColorSave << [hue: swt.currentHue, saturation: swt.currentSaturation, level: swt.currentLevel]
		def evts = swt.events(max: 250)
		def foundValue = false
		def keepSearching = true
		for (def evt : evts)	{
			if (!foundValue && keepSearching)	{
				if (evt.value == 'setColorTemperature')
					foundValue = true
				else if (['hue', 'saturation'].contains(evt.name))
					keepSearching = false
			}
			else
				break
		}
		state.colorColorTemperatureTrueSave << (foundValue ? true : false)
		state.colorColorTemperatureSave << swt.currentColorTemperature
	}
}

private restoreAnnounceSwitches()	{
	def i = 0
	for (def swt : announceSwitches)	{
		ifDebug("$swt | ${state.colorColorSave[i]} | ${state.colorSwitchSave[(i)]} | ${state.colorColorTemperatureTrueSave[i]} | ${state.colorColorTemperatureSave[i]}")
		if (state.colorColorTemperatureTrueSave[(i)] == true)
			swt.setColorTemperature(state.colorColorTemperatureSave[(i)])
		else
			swt.setColor(state.colorColorSave[(i)])
		swt.setLevel(state.colorColorSave[(i)].level)
		i = i + 1
	}
	runOnce(new Date(now() + (getHubType() == _SmartThings ? 250 : 2500)), setAnnounceSwitches)
}

def setAnnounceSwitches()	{
	def i = 0
	for (def swt : announceSwitches)	{
		swt."${(state.colorSwitchSave[(i)] == off ? off : on)}"()
		i = i + 1
	}
	scheduleNext()
}

def tellTime()	{
	ifDebug("tellTime", 'info')
	def nowDate = new Date(now())
	def intCurrentMM = nowDate.format("mm", location.timeZone) as Integer
	def intCurrentHH = nowDate.format("HH", location.timeZone) as Integer
// TODO
	def timeString = 'time is ' + (intCurrentMM == 0 ? '' : intCurrentMM + ' minutes past ') + intCurrentHH + (intCurrentHH < 12 ? ' oclock.' : ' hundred hours.')
	if ((timeAnnounce == '1' || (timeAnnounce == '2' && (intCurrentMM == 0 || intCurrentMM == 30)) ||
		(timeAnnounce == '3' && intCurrentMM == 0)))
		speakIt(timeString)
}

def githubUpdated(storeCommitTimestamp = false)	{
	ifDebug('githubUpdated', 'info')
	def params = [uri: "https://api.github.com/repos/adey/bangali/commits${(state.githubUpdate ? '?since=' + state.githubUpdate : '')}"]
	def result = null
	def githubText = false
	def latestCommitDate
	try	{
		httpGet(params)	{ response ->
			if (response.status == 200)		result = response;
		}
		if (result)	{
			latestCommitDate = result.data.commit.committer.date.max()
//            ifDebug("last github update: $latestCommitDate")
			if (latestCommitDate)	{
				if (storeCommitTimestamp)	{
					if (!state.githubUpdate || !state.version || state.version != version())	{
						state.githubUpdate = latestCommitDate
						state.version = version()
					}
				}
				else if (state.githubUpdate != latestCommitDate || state.version != version())
					githubText = true
			}
		}
	}   catch (e)	{}
	if (githubText)	{
		def str = "Rooms Manager: Code has been updated on Github. Thank you."
		ifDebug(str, 'trace')
		if (phoneNumber)		roomsSMS(str);
		if (!gitRepeat)	{
			state.githubUpdate = latestCommitDate
			state.version = version()
		}
	}
}

private roomsSMS(str)	{
	ifDebug('roomsSMS', 'info')
	if (!phoneNumber || !str)		return;
	def nowDate = new Date(now())
	def intCurrentHH = nowDate.format("HH", location.timeZone) as Integer
	def intCurrentMM = nowDate.format("mm", location.timeZone) as Integer
	if (intCurrentHH < startHHSms || (intCurrentHH > endHHSms || (intCurrentHH == endHHSms && intCurrentMM != 0)))		return;
	sendSms(phoneNumber, str)
}

def checkDeviceHealth()	{
	ifDebug("checkDeviceHealth", 'info')
	if (!state.connected)		state.connected = [:];
	if (!state.connected.runNumber)		state.connected.runNumber = 0;
	if (state.connected?.inRun == true)		return;
	state.connected.inRun = true
	def nowTime = now()
	if (state.connected.runNumber == 10)		{
		state.connected.runNumber = 0
		state.connected.devices = []
	}
	if (!state.connected.devices)	{
		state.connected.devices = healthAddDevices.id
		state.deviceConnectivity = ''
	}
	def cDT = new Date(nowTime - (eventHours.toInteger() * 3600000l))
	def hT = getHubType()

	def timer = (hT == _SmartThings ? 10000l : 1000l)
	def cnt = healthAddDevices.size()
	for (def dit : healthAddDevices)		{
		def nowTime2 = now() - nowTime
		if (nowTime2 > timer)	break;
		if (!state.connected.devices.contains(dit.id))	continue;
		def deviceEventFound = false
		if (hT == _SmartThings)		{
			def lastEvents
			for (def x = 1; x <= 3; x++)		{
				def noOfEvents = Math.pow(5, x).toInteger()
				lastEvents = dit.events(max: noOfEvents).findAll	{ it.eventSource == 'DEVICE' && it.date.after(cDT) }
				if (lastEvents)	{
					deviceEventFound = true
					break
				}
			}
		}
		else		{
			for (def cS : dit.currentStates)	{
				if (Date.parse("yyyy-MM-dd HH:mm:ss.SSS", cS.date.toString()).after(cDT))	{
					deviceEventFound =  true
					break
				}
			}
		}
		if (!deviceEventFound)		state.deviceConnectivity = state.deviceConnectivity + (state.deviceConnectivity ? ', ' : '') + dit.displayName;
		state.connected.devices = state.connected.devices - dit.id
	}
	if (state.connected.devices)	{
		runOnce(new Date(now() + ((hT == _SmartThings ? 10 : 2) * 1000)), deviceAgain)
		state.connected.runNumber = state.connected.runNumber + 1
	}
	else	{
		state.connected.runNumber = 0

		if (state.deviceConnectivity)	state.deviceConnectivity = addAnd(state.deviceConnectivity);

		def cD = (healthCriticalNotification && healthCriticalDevices && tD ? healthCriticalDevices.id.findAll { tD.id.contains(it) } : [])
		if (cD)		roomsSMS("Rooms Manager: ${cD.size()} critical devices failed connectivity check.")

		state.lastDeviceHealthUpdate = (state.deviceConnectivity ? "$state.deviceConnectivity devices have not checked in last $eventHours hours." : "device connectivity is ok.")
		if (announceSwitches && ((!state.deviceConnectivity && healthOkColor) || (state.deviceConnectivity && healthWarnColor)))	{
			def color = convertRGBToHueSaturation(colorsRGB[(state.deviceConnectivity ?  healthWarnColor : healthOkColor)][1])
////			state.colorNotificationColor = color
			setupColorNotification(color)
		}
		if (_healthCheck.contains(healthEvery?.toInteger()))	{
//			ifDebug("_healthCheck: $_healthCheck | healthEvery: $healthEvery | state.healthHours: $state.healthHours")
			if ((new Date(now())).format("HH", location.timeZone).toInteger() == startHH)		state.healthHours = 0;
			if (state.healthHours == 0 && state.deviceConnectivity)
				speakIt(state.lastDeviceHealthUpdate)
			state.healthHours = (state.healthHours == 0 ? healthEvery.toInteger() : state.healthHours) - 1
		}
	}
//log.debug "perf checkDeviceHealth: ${now() - nowTime} ms, checked $cnt devices"
	state.connected.inRun = false
}

def deviceAgain()	{  checkDeviceHealth()  }

private addAnd(str)	{
	if (!str)	return '';
	def lio = str.lastIndexOf(',')
	return (lio == -1 ? str : (str.substring(0, lio) + " and " + str.substring(lio + 1)))
}

// for vacation mode only
def setRoomState(childID)	{
	def cOD = getChildRoomOccupancyDeviceObject(childID)
	if (cOD)	cOD."$roomState"(true, true);
}

private ifDebug(msg = null, level = null)	{  if (msg && (isDebug() || level == 'error'))  log."${level ?: 'debug'}" " $app.label: " + msg  }

private convertRGBToHueSaturation(setColorTo)	{
	def str, rgb
	if (setColorTo)
		str = setColorTo.replaceAll("\\s","").toLowerCase()
	rgb = (colorsRGB[str][0] ?: colorsRGB['white'][0])
	ifDebug("$str | $rgb")
	float r = rgb[0] / 255
	float g = rgb[1] / 255
	float b = rgb[2] / 255
	float max = Math.max(Math.max(r, g), b)
	float min = Math.min(Math.min(r, g), b)
	float h, s, l = (max + min) / 2

	if (max == min)
		h = s = 0 // achromatic
	else	{
		float d = max - min
		s = l > 0.5 ? d / (2 - max - min) : d / (max + min)
		switch (max)	{
			case r:    h = (g - b) / d + (g < b ? 6 : 0);  break
			case g:    h = (b - r) / d + 2;                break
			case b:    h = (r - g) / d + 4;                break
		}
		h /= 6
	}
	return [hue: Math.round(h * 100), saturation: Math.round(s * 100), level: Math.round(l * 100)]
}

private spawnVacationApp()	{
	ifDebug("spawnChildApp")
	def app = getVacationApp()
	if (!app)	app = addChildApp("bangali", "rooms vacation", "# rooms vacation", [completedSetup: true]);
	return app
}

private getVacationApp()	{
	def app = null
	for (def a : childApps)		if (a.name == 'rooms vacation')		{  app = a; break;  }
	return app
}

def pageAllSettings(setings, allRules, childCreated, onlyHas, anonIt)	{
	def settingsApp = spawnChildSettings()
	return	(settingsApp.allSettings(setings, allRules, childCreated, onlyHas, anonIt))
}

def pageRuleDelete(allRules)	{
	def settingsApp = spawnChildSettings()
	return	(settingsApp.ruleDelete(allRules))
}

private spawnChildSettings()	{
	ifDebug("spawnChildSettingsApp")
	def app = getChildSettingsApp()
	if (!app)		app = addChildApp("bangali", "rooms child settings", "# rooms child settings", [completedSetup: true]);
	return app
}

private getChildSettingsApp()	{
	def app = null
	for (def a : childApps)		if (a.name == 'rooms child settings')		{  app = a; break  }
	return app
}

//------------------------------------------------------------------------------------------------------------------------//

@Field final Map    colorsRGB = [
	aliceblue: [[240, 248, 255], 'Alice Blue'],
	antiquewhite: [[250, 235, 215], 'Antique White'],
	aqua: [[0, 255, 255], 'Aqua'],
	aquamarine: [[127, 255, 212], 'Aquamarine'],
	azure: [[240, 255, 255], 'Azure'],
	beige: [[245, 245, 220], 'Beige'],
	bisque: [[255, 228, 196], 'Bisque'],
	black: [[0, 0, 0], 'Black'],
	blanchedalmond: [[255, 235, 205], 'Blanched Almond'],
	blue: [[0, 0, 255], 'Blue'],
	blueviolet: [[138, 43, 226], 'Blue Violet'],
	brown: [[165, 42, 42], 'Brown'],
	burlywood: [[222, 184, 135], 'Burly Wood'],
	cadetblue: [[95, 158, 160], 'Cadet Blue'],
	chartreuse: [[127, 255, 0], 'Chartreuse'],
	chocolate: [[210, 105, 30], 'Chocolate'],
	coral: [[255, 127, 80], 'Coral'],
	cornflowerblue: [[100, 149, 237], 'Corn Flower Blue'],
	cornsilk: [[255, 248, 220], 'Corn Silk'],
	crimson: [[220, 20, 60], 'Crimson'],
	cyan: [[0, 255, 255], 'Cyan'],
	darkblue: [[0, 0, 139], 'Dark Blue'],
	darkcyan: [[0, 139, 139], 'Dark Cyan'],
	darkgoldenrod: [[184, 134, 11], 'Dark Golden Rod'],
	darkgray: [[169, 169, 169], 'Dark Gray'],
	darkgreen: [[0, 100, 0], 'Dark Green'],
	darkgrey: [[169, 169, 169], 'Dark Grey'],
	darkkhaki: [[189, 183, 107], 'Dark Khaki'],
	darkmagenta: [[139, 0, 139],  'Dark Magenta'],
	darkolivegreen: [[85, 107, 47], 'Dark Olive Green'],
	darkorange: [[255, 140, 0], 'Dark Orange'],
	darkorchid: [[153, 50, 204], 'Dark Orchid'],
	darkred: [[139, 0, 0], 'Dark Red'],
	darksalmon: [[233, 150, 122], 'Dark Salmon'],
	darkseagreen: [[143, 188, 143], 'Dark Sea Green'],
	darkslateblue: [[72, 61, 139], 'Dark Slate Blue'],
	darkslategray: [[47, 79, 79], 'Dark Slate Gray'],
	darkslategrey: [[47, 79, 79], 'Dark Slate Grey'],
	darkturquoise: [[0, 206, 209], 'Dark Turquoise'],
	darkviolet: [[148, 0, 211], 'Dark Violet'],
	deeppink: [[255, 20, 147], 'Deep Pink'],
	deepskyblue: [[0, 191, 255], 'Deep Sky Blue'],
	dimgray: [[105, 105, 105], 'Dim Gray'],
	dimgrey: [[105, 105, 105], 'Dim Grey'],
	dodgerblue: [[30, 144, 255], 'Dodger Blue'],
	firebrick: [[178, 34, 34], 'Fire Brick'],
	floralwhite: [[255, 250, 240], 'Floral White'],
	forestgreen: [[34, 139, 34], 'Forest Green'],
	fuchsia: [[255, 0, 255], 'Fuchsia'],
	gainsboro: [[220, 220, 220], 'Gainsboro'],
	ghostwhite: [[248, 248, 255], 'Ghost White'],
	gold: [[255, 215, 0], 'Gold'],
	goldenrod: [[218, 165, 32], 'Golden Rod'],
	gray: [[128, 128, 128], 'Gray'],
	green: [[0, 128, 0], 'Green'],
	greenyellow: [[173, 255, 47], 'Green Yellow'],
	grey: [[128, 128, 128], 'Grey'],
	honeydew: [[240, 255, 240], 'Honey Dew'],
	hotpink: [[255, 105, 180], 'Hot Pink'],
	indianred: [[205, 92, 92], 'Indian Red'],
	indigo: [[75, 0, 130], 'Indigo'],
	ivory: [[255, 255, 240], 'Ivory'],
	khaki: [[240, 230, 140], 'Khaki'],
	lavender: [[230, 230, 250], 'Lavender'],
	lavenderblush: [[255, 240, 245], 'Lavender Blush'],
	lawngreen: [[124, 252, 0], 'Lawn Green'],
	lemonchiffon: [[255, 250, 205], 'Lemon Chiffon'],
	lightblue: [[173, 216, 230], 'Light Blue'],
	lightcoral: [[240, 128, 128], 'Light Coral'],
	lightcyan: [[224, 255, 255], 'Light Cyan'],
	lightgoldenrodyellow: [[250, 250, 210], 'Light Golden Rod Yellow'],
	lightgray: [[211, 211, 211], 'Light Gray'],
	lightgreen: [[144, 238, 144], 'Light Green'],
	lightgrey: [[211, 211, 211], 'Light Grey'],
	lightpink: [[255, 182, 193], 'Light Pink'],
	lightsalmon: [[255, 160, 122], 'Light Salmon'],
	lightseagreen: [[32, 178, 170], 'Light Sea Green'],
	lightskyblue: [[135, 206, 250], 'Light Sky Blue'],
	lightslategray: [[119, 136, 153], 'Light Slate Gray'],
	lightslategrey: [[119, 136, 153], 'Light Slate Grey'],
	lightsteelblue: [[176, 196, 222], 'Ligth Steel Blue'],
	lightyellow: [[255, 255, 224], 'Light Yellow'],
	lime: [[0, 255, 0], 'Lime'],
	limegreen: [[50, 205, 50], 'Lime Green'],
	linen: [[250, 240, 230], 'Linen'],
	magenta: [[255, 0, 255], 'Magenta'],
	maroon: [[128, 0, 0], 'Maroon'],
	mediumaquamarine: [[102, 205, 170], 'Medium Aquamarine'],
	mediumblue: [[0, 0, 205], 'Medium Blue'],
	mediumorchid: [[186, 85, 211], 'Medium Orchid'],
	mediumpurple: [[147, 112, 219], 'Medium Purple'],
	mediumseagreen: [[60, 179, 113], 'Medium Sea Green'],
	mediumslateblue: [[123, 104, 238], 'Medium Slate Blue'],
	mediumspringgreen: [[0, 250, 154], 'Medium Spring Green'],
	mediumturquoise: [[72, 209, 204], 'Medium Turquoise'],
	mediumvioletred: [[199, 21, 133], 'Medium Violet Red'],
	midnightblue: [[25, 25, 112], 'Medium Blue'],
	mintcream: [[245, 255, 250], 'Mint Cream'],
	mistyrose: [[255, 228, 225], 'Misty Rose'],
	moccasin: [[255, 228, 181], 'Moccasin'],
	navajowhite: [[255, 222, 173], 'Navajo White'],
	navy: [[0, 0, 128], 'Navy'],
	oldlace: [[253, 245, 230], 'Old Lace'],
	olive: [[128, 128, 0], 'Olive'],
	olivedrab: [[107, 142, 35], 'Olive Drab'],
	orange: [[255, 165, 0], 'Orange'],
	orangered: [[255, 69, 0], 'Orange Red'],
	orchid: [[218, 112, 214], 'Orchid'],
	palegoldenrod: [[238, 232, 170], 'Pale Golden Rod'],
	palegreen: [[152, 251, 152], 'Pale Green'],
	paleturquoise: [[175, 238, 238], 'Pale Turquoise'],
	palevioletred: [[219, 112, 147], 'Pale Violet Red'],
	papayawhip: [[255, 239, 213], 'Papaya Whip'],
	peachpuff: [[255, 218, 185], 'Peach Cuff'],
	peru: [[205, 133, 63], 'Peru'],
	pink: [[255, 192, 203], 'Pink'],
	plum: [[221, 160, 221], 'Plum'],
	powderblue: [[176, 224, 230], 'Powder Blue'],
	purple: [[128, 0, 128], 'Purple'],
	rebeccapurple: [[102, 51, 153], 'Rebecca Purple'],
	red: [[255, 0, 0], 'Red'],
	rosybrown: [[188, 143, 143], 'Rosy Brown'],
	royalblue: [[65, 105, 225], 'Royal Blue'],
	saddlebrown: [[139, 69, 19], 'Saddle Brown'],
	salmon: [[250, 128, 114], 'Salmon'],
	sandybrown: [[244, 164, 96], 'Sandy Brown'],
	seagreen: [[46, 139, 87], 'Sea Green'],
	seashell: [[255, 245, 238], 'Sea Shell'],
	sienna: [[160, 82, 45], 'Sienna'],
	silver: [[192, 192, 192], 'Silver'],
	skyblue: [[135, 206, 235], 'Sky Blue'],
	slateblue: [[106, 90, 205], 'Slate Blue'],
	slategray: [[112, 128, 144], 'Slate Gray'],
	slategrey: [[112, 128, 144], 'Slate Grey'],
	snow: [[255, 250, 250], 'Snow'],
	springgreen: [[0, 255, 127], 'Spring Green'],
	steelblue: [[70, 130, 180], 'Steel Blue'],
	tan: [[210, 180, 140], 'Tan'],
	teal: [[0, 128, 128], 'Teal'],
	thistle: [[216, 191, 216], 'Thistle'],
	tomato: [[255, 99, 71], 'Tomato'],
	turquoise: [[64, 224, 208], 'Turquoise'],
	violet: [[238, 130, 238], 'Violet'],
	wheat: [[245, 222, 179], 'Wheat'],
	white: [[255, 255, 255], 'White'],
	whitesmoke: [[245, 245, 245], 'White Smoke'],
	yellow: [[255, 255, 0], 'Yellow'],
	yellowgreen: [[154, 205, 50], 'Yellow Green']
]

@Field final String _SpokenImage = "https://cdn.rawgit.com/adey/bangali/master/resources/icons/roomsManagerSpoken.png"
@Field final String _ColorImage = 'https://cdn.rawgit.com/adey/bangali/master/resources/icons/roomsManagerColor.png'
@Field final String _PresenceImage = 'https://cdn.rawgit.com/adey/bangali/master/resources/icons/roomsManagerPresence.png'
@Field final String _ModeImage = 'https://cdn.rawgit.com/adey/bangali/master/resources/icons/roomsManagerModes.png'
@Field final String _TimeImage = 'https://cdn.rawgit.com/adey/bangali/master/resources/icons/roomsManagerTime.png'
@Field final String _SunImage = 'https://cdn.rawgit.com/adey/bangali/master/resources/icons/roomsManagerSun2.png'
@Field final String _BatteryImage = 'https://cdn.rawgit.com/adey/bangali/master/resources/icons/roomsManagerBattery.png'
@Field final String _HealthImage = 'https://cdn.rawgit.com/adey/bangali/master/resources/icons/roomsManagerHealth.png'
@Field final String _ProcessImage = 'https://cdn.rawgit.com/adey/bangali/master/resources/icons/roomsManagerProcess.png'
@Field final String _GHimage = 'https://cdn.rawgit.com/adey/bangali/master/resources/icons/roomOccupancySettings.png'
@Field final String _GitUpdateImage = 'https://cdn.rawgit.com/adey/bangali/master/resources/icons/roomsManagerGithub.png'
@Field final String _SMSImage = 'https://cdn.rawgit.com/adey/bangali/master/resources/icons/roomsManagerSMS.png'
@Field final String _gitREADME = 'https://github.com/adey/bangali/blob/master/README.md'