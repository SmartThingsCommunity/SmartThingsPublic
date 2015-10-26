/**
 *  Mode change notifier
 *
 *  Sends a notification anytime a mode change occurs.
 *
 *  --------------------------------------------------------------------------
 *
 *  Copyright (c) 2014 Rayzurbock.com
 *
 *  This program is free software: you can redistribute it and/or modify it
 *  under the terms of the GNU General Public License as published by the Free
 *  Software Foundation, either version 3 of the License, or (at your option)
 *  any later version.
 *
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *  or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 *  for more details.
 *
 *  You should have received a copy of the GNU General Public License along
 *  with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 *  --------------------------------------------------------------------------
 *
 *  The latest version of this file can be found on GitHub at:
 *  http://github.com/rayzurbock/SmartThings-ModeChangeNotify
 * 
 *  Version 1.0.1 (2014-11-10)
 */
 
definition(
    name: "Mode Change Notify",
    namespace: "rayzurbock",
    author: "brian@rayzurbock.com",
    description: "Sends a notification when a mode change occurs",
    category: "My Apps",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png"
) 

preferences {
	section("Setup") {
		def pageProperties = [
			name:	"pageSetup",
			title:	"Configuration",
			install:	true
		]
		def inputEnableAlerts = [
			name:	"enableAlerts",
			type:	"bool",
			title:  "Enable mode change alerts?",
			defaultValue:	true
		]
		input inputEnableAlerts
		paragraph "Mode Change Notify Version 1.0.1"
		paragraph "http://github.com/rayzurbock"
		
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
  subscribe(location, onLocation)
  state.lastmode = location.mode
  LOGMESSAGE("ModeChangeNotify: Initialized. Notifications: ${settings.enableAlerts}. Current Mode: ${state.lastmode}.")
}

def onLocation(evt) {
  def msg = "${location.name} mode changed from ${state.lastmode} to ${evt.value}"
  LOGMESSAGE("ModeChangeNotify: ${msg}")
  if (settings.enableAlerts){
	sendPush(msg)
	sendNotificationEvent(msg)
  } else {
	LOGMESSAGE("ModeChangeNotify: Push notifications are disabled in SmartApp settings.")
  }
  state.lastmode = location.mode
}

private def LOGMESSAGE(message){
	log.debug message
}
