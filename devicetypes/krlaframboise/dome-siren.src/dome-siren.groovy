/**

* Dome Siren v1.2.2

* (Model: DMS01)

*

* Author: 

* Kevin LaFramboise (krlaframboise)

*

* URL to documentation: https://community.smartthings.com/t/release-dome-siren-official/75499?u=krlaframboise

* 

*

* Changelog:

*

* 1.2.2 (12/07/2017)

* 	- Fixed beeping during delayed alarm because SmartThings broke something during a recent update.

*

* 1.2.1 (12/02/2017)

* 	- Fixed bug with siren play until depleted setting.

*

* 1.2 (10/12/2017)

* 	- SmartThings broke the ability to set a state value to null which prevented the resetting of sirenStartTime.

*

* 1.1.6 (04/27/2017)

* 	- Bug fix for siren not turning on.

*

* 1.1.5 (04/23/2017)

* 	- SmartThings broke parse method response handling so switched to sendhubaction.

* 	- Bug fix for location timezone issue.

*

* 1.1.4 (03/21/2017)

* 	- Fix for SmartThings TTS url changing.

*

* 1.1.3 (03/11/2017)

* - Cleaned code for publication.

*

* 1.1.2 (03/09/2017)

* - Added health check and switch capabilities.

* - Added self polling functionality.

* - Removed polling capability.

*

* 1.0 (01/25/2017)

* - Initial Release

*

*

* Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except

* in compliance with the License. You may obtain a copy of the License at:

*

* http://www.apache.org/licenses/LICENSE-2.0

*

* Unless required by applicable law or agreed to in writing, software distributed under the License is distributed

* on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License

* for the specific language governing permissions and limitations under the License.

*

*/

metadata {

	definition (

		name: "Dome Siren", 

		namespace: "krlaframboise", 

		author: "Kevin LaFramboise"

	) {

		capability "Actuator"

		capability "Alarm"

		capability "Battery"

		capability "Configuration"

		capability "Refresh"

		capability "Tone"

		capability "Speech Synthesis"

		capability "Audio Notification"

		capability "Music Player"

		capability "Switch"

		capability "Health Check"


		attribute "lastCheckin", "string"

		attribute "status", "enum", ["alarm", "pending", "off", "chime"]


		// Required for Speaker notify with sound

		command "playSoundAndTrack"

		command "playTrackAtVolume"		


		command "on"

		command "bell1"

		command "bell2"

		command "bell3"

		command "bell4"

		command "bell5"

		command "chime1"

		command "chime2"

		command "chime3"

		command "siren1"

		command "siren2"


		fingerprint deviceId: "0x1005", inClusters: "0x25, 0x59, 0x5A, 0x5E, 0x70, 0x71, 0x72, 0x73, 0x80, 0x85, 0x86, 0x87"


		fingerprint mfr:"021F", prod:"0003", model:"0088"

	}


	simulator { }


	preferences {

		input "sirenSound", "enum",

			title: "Alarm Sound:",

			displayDuringSetup: true,

			required: false,

			defaultValue: sirenSoundSetting,

			options: sirenSoundOptions.collect { it.name }

		input "sirenVolume", "enum",

			title: "Alarm Volume:",

			required: false,

			defaultValue: sirenVolumeSetting,			

			displayDuringSetup: true,

			options: sirenVolumeOptions.collect { it.name }

		input "sirenLength", "enum",

			title: "Alarm Duration:",

			defaultValue: sirenLengthSetting,

			required: false,

			displayDuringSetup: true,

			options: sirenLengthOptions.collect { it.name }

		input "sirenLED", "enum",

			title: "Alarm LED:",

			defaultValue: sirenLEDSetting,

			required: false,

			displayDuringSetup: true,

			options: ledOptions.collect { it.name }

		input "sirenDelay", "enum",

			title: "Alarm Delay:",

			defaultValue: sirenDelaySetting,

			required: false,

			displayDuringSetup: true,

			options: sirenDelayOptions.collect { it.name }

		input "sirenDelayBeep", "enum",

			title: "Alarm Delay Beep:",

				defaultValue: sirenDelayBeepSetting,

				required: false,

				displayDuringSetup: true,

				options: sirenDelayBeepOptions.collect { it.name }

		input "chimeVolume", "enum",

			title: "Chime Volume:",

			required: false,

			defaultValue: chimeVolumeSetting,

			displayDuringSetup: true,

			options: chimeVolumeOptions.collect { it.name }

		input "chimeRepeat", "enum",

			title: "Chime Repeat:",

			required: false,

			displayDuringSetup: true,

			defaultValue: chimeRepeatSetting,

			options: chimeRepeatOptions.collect { it.name }

		input "chimeLED", "enum",

			title: "Chime LED:",

			defaultValue: chimeLEDSetting,

			required: false,

			displayDuringSetup: true,

			options: ledOptions.collect { it.name }

		input "checkinInterval", "enum",

			title: "Checkin Interval:",

			defaultValue: checkinIntervalSetting,

			required: false,

			displayDuringSetup: true,

			options: checkinIntervalOptions.collect { it.name }

		input "batteryReportingInterval", "enum",

			title: "Battery Reporting Interval:",

			defaultValue: batteryReportingIntervalSetting,

			required: false,

			displayDuringSetup: true,

			options: checkinIntervalOptions.collect { it.name }

		input "debugOutput", "bool", 

			title: "Enable debug logging?", 

			defaultValue: true, 

			required: false

	}


	tiles(scale: 2) {

		multiAttributeTile(name:"status", type: "generic", width: 6, height: 4, canChangeIcon: true){

			tileAttribute ("device.status", key: "PRIMARY_CONTROL") {

				attributeState "off", 

					label:'Off', 

					action: "siren", 

					icon: "st.security.alarm.clear",

					backgroundColor:"#ffffff"

				attributeState "alarm", 

					label:'Alarm!', 

					action: "off", 

					icon:"st.alarm.alarm.alarm", 

					backgroundColor:"#ff9999"

				attributeState "pending", 

					label:'Alarm Pending!', 

					action: "off", 

					icon:"st.alarm.alarm.alarm", 

					backgroundColor:"#ff9999"

				attributeState "chime", 

					label:'Chime!', 

					action: "off", 

					icon:"st.Entertainment.entertainment2", 					

					backgroundColor: "#cc99cc"

			}

		}


		standardTile("playAlarm", "device.alarm", width: 2, height: 2) {

			state "default", 

				label:'Alarm', 

				action:"alarm.siren", 

				icon:"st.security.alarm.clear", 

				backgroundColor:"#ff9999"

			state "siren",

				label:'Turn Off',

				action:"alarm.off",

				icon:"st.alarm.alarm.alarm", 

				background: "#ffffff"	

		}


		standardTile("turnOff", "device.off", width: 2, height: 2) {

			state "default", 

				label:'Off', 

				action:"alarm.off", 

				icon:"st.security.alarm.clear",

				backgroundColor: "#ffffff"			

		}


		standardTile("playBell1", "device.status", width: 2, height: 2) {

			state "default", label:'Bell 1', action:"bell1", icon:"st.Seasonal Winter.seasonal-winter-002",backgroundColor: "#99FF99"

		}


		standardTile("playBell2", "device.status", width: 2, height: 2) {

			state "default", label:'Bell 2', action:"bell2", icon:"st.Seasonal Winter.seasonal-winter-002",backgroundColor: "#99FF99"

		}


		standardTile("playBell3", "device.status", width: 2, height: 2) {

			state "default", label:'Bell 3', action:"bell3", icon:"st.Seasonal Winter.seasonal-winter-002",backgroundColor: "#99FF99"

		}


		standardTile("playBell4", "device.status", width: 2, height: 2) {

			state "default", label:'Bell 4', action:"bell4", icon:"st.Seasonal Winter.seasonal-winter-002",backgroundColor: "#99FF99"

		}


		standardTile("playBell5", "device.status", width: 2, height: 2) {

			state "default", label:'Bell 5', action:"bell5", icon:"st.Seasonal Winter.seasonal-winter-002",backgroundColor: "#99FF99"

		}


		standardTile("playChime1", "device.status", width: 2, height: 2) {

			state "default", label:'Chime 1', action:"chime1", icon:"st.Entertainment.entertainment2",backgroundColor: "#CC99CC"

		}


		standardTile("playChime2", "device.status", width: 2, height: 2) {

			state "default", label:'Chime 2', action:"chime2", icon:"st.Entertainment.entertainment2",backgroundColor: "#CC99CC"

		}


		standardTile("playChime3", "device.status", width: 2, height: 2) {

			state "default", label:'Chime 3', action:"chime3", icon:"st.Entertainment.entertainment2",backgroundColor: "#CC99CC"

		}


		standardTile("playSiren1", "device.status", width: 2, height: 2) {

			state "default", label:'Siren 1', action:"siren1", icon:"st.security.alarm.clear",backgroundColor: "#ff9999"

		}


		standardTile("playSiren2", "device.status", width: 2, height: 2) {

			state "default", label:'Siren 2', action:"siren2", icon:"st.security.alarm.clear",backgroundColor: "#ff9999"

		}


		standardTile("refresh", "device.refresh", width: 2, height: 2) {

			state "refresh", label:'Refresh', action: "refresh", icon:"st.secondary.refresh-icon"

		}


		valueTile("battery", "device.battery", decoration: "flat", width: 2, height: 2){

			state "battery", label:'${currentValue}% battery', unit:""

		}		


		main "status"

		details(["status", "playAlarm", "turnOff", "refresh", "playSiren1", "playSiren2", "playChime1", "playBell1", "playBell2", "playChime2", "playBell3", "playBell4", "playChime3", "playBell5", "battery"])

	}

}


// Initializes the health check interval and sends configuration to device the first time it runs.

def updated() {	

	// This method always gets called twice when preferences are saved.

	if (!isDuplicateCommand(state.lastUpdated, 3000)) {		

		state.lastUpdated = new Date().time

		state.activeEvents = []

		logTrace "updated()"


		initializeCheckin()


		if (state.firstUpdate == false) {

			def result = []

			result += configure()

			if (result) {

				return sendResponse(result)

			}

		}

		else {

			// Skip first time updating because it calls the configure method while it's already running.

			state.firstUpdate = false

		}

	}		

}


private initializeCheckin() {

	// Set the Health Check interval so that it can be skipped once plus 2 minutes.

	def checkInterval = ((checkinIntervalSettingMinutes * 2 * 60) + (2 * 60))


	sendEvent(name: "checkInterval", value: checkInterval, displayed: false, data: [protocol: "zwave", hubHardwareId: device.hub.hardwareID])


	startHealthPollSchedule()

}


private startHealthPollSchedule() {

	unschedule(healthPoll)

	switch (checkinIntervalSettingMinutes) {

		case 5:

			runEvery5Minutes(healthPoll)

			break

		case 10:

			runEvery10Minutes(healthPoll)

			break

		case 15:

			runEvery15Minutes(healthPoll)

			break

		case 30:

			runEvery30Minutes(healthPoll)

			break

		case [60, 120]:

			runEvery1Hour(healthPoll)

			break

		default:

			runEvery3Hours(healthPoll)			

	}

}


// Executed by internal schedule and requests a report from the device to determine if it's still online.