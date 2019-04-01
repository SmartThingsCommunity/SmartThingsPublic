/**

 *  TotalConnect

 *

 *  Copyright 2017 Yogesh Mhatre, Oendaril, Jeremy Stroebel

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

 /*

Version: v0.3.4

 Changes [July 24th, 2017]

 	- Few bugs smashed. UnAuthenticated DisArm functionality was broken (copy/paste error), now fixed.

 	- Notifications were broken. Now fixed. Instead of polling on TotalConnect API, We are waiting for 70 seconds before notifying.



Version: v0.3.3

 Changes [July 23rd, 2017]

 	- SmartThings/TotalConnect (not sure who?) broke Synchronous API calls, so implemented Asynchronous calls. Major Asynchronous code references are from Oendaril - https://github.com/Oendaril/TotalConnectAsync/blob/master/TCAsync.groovy

 	- Hardcoded backend values so users dont have to implement them during setup, but have flexiblity to change if needed. Code: jhstroebel@github



Version: v0.3.2

 Changes [June 10th, 2017]

 	- Location listing correction.



Version: v0.3.1

 Changes [November 12th, 2015]

 	- Added ability to select Total Connect Location for users with multiple Locations. For sigle location users, it will default to your location

 	- Implemented logic to successfully message. Though there is room for improvement



Version: v0.3.

 Changes [August 30th, 2015]

 	- Logic to check if the Arm/DisArm signal actually implemented or not.

  	- User dont have to input LocationID & DeviceID. Its been capatured from the response now.



 */



include 'asynchttp_v1'



definition(

    name: "TotalConnect",

    namespace: "Security",

    author: "Yogesh Mhatre",

    description: "Total Connect App to lock/unlock your home based on your location and mode",

    category: "My Apps",

    iconUrl: "https://s3.amazonaws.com/yogi/TotalConnect/150.png",

    iconX2Url: "https://s3.amazonaws.com/yogi/TotalConnect/300.png")



preferences {

	page(name: "authenticate", content: "authenticate")

  page(name: "selectLocation", content: "selectLocation")

  page(name: "confirmation", content: "confirmation")

}



// First Page for authentication

def authenticate() {

	dynamicPage(name: "authenticate", title: "Total Connect Login", nextPage: "selectLocation",uninstall: true, install: false){

    section("Give your TotalConnect credentials") {

      paragraph "It is recommended to make another total connect user for SmartThings. This user should have a passcode SET"

      input("userName", "text", title: "Username", description: "Your username for TotalConnect")

      input("password", "password", title: "Password", description: "Your Password for TotalConnect", submitOnChange:true)

    }

    section("Backend TotalConnect Values - DO NOT CHANGE", hideable: true, hidden: true) {

      paragraph "These are required for login. They typically do not change"

      input("applicationId", "text", title: "Application ID currently - 14588", description: "Application ID", defaultValue: "14588")

      input("applicationVersion", "text", title: "Application Version currently - 3.24.1", description: "Application Version", defaultValue: "3.24.1")

    }

  }



}



// Second Page that pulls Location Map to select a Location for multiLocation users of TotalConnect

def selectLocation() {

  atomicState.applicationId = settings.applicationId

  atomicState.applicationVersion = settings.applicationVersion

  //log.debug "During authentication page applicationId is $atomicState.applicationId & applicationVersion is $atomicState.applicationVersion"

  getLocations()

  while (atomicState.locationMap == null)

    {

      pause(1000)

    } // This while loop is "bit disturbing, but needed. Until atomicState.locationMap does not have any value, we are making the app pause for a seconds. Typically in my observance, the while loop is only executed thrice or less"

  //log.debug "During selectLocation Page, LocationMap is $atomicState.locationMap"

  def locations = atomicState.locationMap

  def optionList = locations.keySet() as List

  //log.debug "OptionList are " + optionList

  dynamicPage(name: "selectLocation", title: "Select the Location of your Total Connect Alarm",nextPage: "confirmation", uninstall: true, install: false) {

    section("Select from the following Locations for Total Connect.") {

      input(name: "selectLocation", type: "enum", required: true, title: "Select the Location", options:optionList, multiple: false, submitOnChange:true)

    }

  }

}



// Third page to store LocationID & DeviceID for SmartApp use

def confirmation(){

  //log.debug "Option selected is $settings.selectLocation"

  def selectedLocation = settings.selectLocation

  log.debug "During Confirmation Page, LocationMap: $atomicState.locationMap, DeviceMap: $atomicState.deviceMap"

  def locations = atomicState.locationMap

  def devices = atomicState.deviceMap

  def deviceId = devices["${selectedLocation}"]

  def locationId = locations["${selectedLocation}"]

  log.debug "DeviceId is $deviceId & LocationId is $locationId"

  dynamicPage(name: "confirmation", title: "Selected LocationID & DeviceID", uninstall: true, install: true) {

    section("DO NOT CHANGE - These values are fetched from your selected Location.") {

      paragraph "If your DeviceID is larger than 6 digits, please use the first 6 digits and remove the rest."

      input(name: "locationId", type: "text", required: true, title: "LocationID", defaultValue: locationId)

      input(name: "deviceId", type: "text", required: true, title: "DeviceID", defaultValue: deviceId)

    }

  }

}



// SmartThings defaults

def installed() {

	  log.debug "Installed with settings: Username: $settings.userName, AplicationId: $settings.applicationId & ApplicationVersion: $settings.applicationVersion, DeviceId: $settings.deviceId & LocationId: $settings.locationId"

    subscribe(location, checkMode)

}



def updated() {

  log.debug "Updated with settings: Username: $settings.userName, AplicationId: $settings.applicationId & ApplicationVersion: $settings.applicationVersion, DeviceId: $settings.deviceId & LocationId: $settings.locationId"

	unsubscribe()

    subscribe(location, checkMode)

}



// Logic for Triggers based on mode change of SmartThings

def checkMode(evt) {

    	if (evt.value == "Away") {

            	log.info "Mode is set to Away, TotalConnect is now performing ArmAway"

            	armAway()

            }

        else if (evt.value == "Night") {

            	log.info "Mode is set to Night, TotalConnect is now performing ArmStay"

            	armStay()

            }

        else if (evt.value == "Home") {

            	log.info "Mode is set to Home, TotalConnect is now performing Disarm"

            	disarm()

        }

}



// disarm Function

def disarm() {

  log.debug "disarm is executed"

	if(isTokenValid())

    	disarmAuthenticated()

    else {

      log.debug "disarm is executed with login"

		login(disarmAuthenticated)

    }

}



def disarmAuthenticated() {

	tcCommandAsync("DisarmSecuritySystem", [SessionID: state.token, LocationID: settings.locationId, DeviceID: settings.deviceId, UserCode: '-1'], 0, "disarm")

}



// armStay Function

def armStay() {

	if(isTokenValid())

    	armStayAuthenticated()

    else {

		login(armStayAuthenticated)

    }

}



def armStayAuthenticated() {

	tcCommandAsync("ArmSecuritySystem", [SessionID: state.token, LocationID: settings.locationId, DeviceID: settings.deviceId, ArmType: 1, UserCode: '-1'], 0, "armStay")

}



// armAway Function

def armAway() {

	if(isTokenValid())

    	armAwayAuthenticated()

    else {

		login(armAwayAuthenticated)

    }

}



def armAwayAuthenticated() {

	tcCommandAsync("ArmSecuritySystem", [SessionID: state.token, LocationID: settings.locationId, DeviceID: settings.deviceId, ArmType: 0, UserCode: '-1'], 0 , "armAway")

}



// Login Function.

def login(callback) {

	//log.debug "Executed login"

    tcCommandAsync("AuthenticateUserLogin",  [userName: settings.userName , password: settings.password, ApplicationID: settings.applicationId, ApplicationVersion: settings.applicationVersion], 0, callback)

}



def loginResponse(token, callback) {

  //log.debug "loginResponse with callback: ${callback} is Executed"

  if(token != null) {

    //log.debug "new token is ${token}"

      state.token = "${token}"

      state.tokenRefresh = now()

  }

    switch(callback) {

      case "refresh":

          refresh()

          break

        case "refreshAuthenticated":

          refreshAuthenticated()

          break

        case "getSessionDetails":

        	getSessionDetails()

          break

        case "armAway":

        	armAway()

        	break

        case "armAwayAuthenticated":

        	armAwayAuthenticated()

        	break

        case "armStay":

        	armStay()

        	break

        case "armStayAuthenticated":

        	armStayAuthenticated()

        	break

        case "getPanelMetadata":

        	getPanelMetadata()

        	break

        case "disarm":

        	disarm()

        	break

        case "disarmAuthenticated":

          disarmAuthenticated()

            break

        default:

            return

        break

    }

}



def logout() {

    tcCommandAsync("Logout",  [SessionID: state.token], 0, "logout")

} //Takes token as argument



// Asyn APIs

def tcCommandAsync(path, body, retry, callback) {

  //log.debug "tcCommandAsync was Executed"

	String stringBody = ""



    body.each { k, v ->

    	if(!(stringBody == "")) {

        	stringBody += "&" }

        stringBody += "${k}=${v}"

    }//convert Map to String



	//log.debug "stringBody: ${stringBody}"



    def params = [

		uri: "https://rs.alarmnet.com/TC21API/TC2.asmx/",

		path: path,

    	body: stringBody,

        requestContentType: "application/x-www-form-urlencoded",

        contentType: "application/xml"

    ]



    def handler



    switch(path) {

        case "GetPanelMetaDataAndFullStatusEx":

        	handler = "panel"

            break

        case "GetZonesListInStateEx":

        	handler = "zone"

            break

        case "AuthenticateUserLogin":

        	handler = "login"

            break

        case "GetSessionDetails":

        	handler = "details"

            break

        case "ArmSecuritySystem":

          handler = "refresh"

            break

        case "DisarmSecuritySystem":

        	handler = "refresh"

            break

        default:

        	handler = "none"

            break

    }//define handler based on method called



    def data = [

    	path: path,

        body: stringBody,

        handler: handler,

        callback: callback,

        retry: retry

    ] //Data for Async Command.  Params to retry, handler to handle, and retry count if needed



    try {

    	asynchttp_v1.post('asyncResponse', params, data)

        //log.debug "Sent asynchhttp_v1.post(responseHandler, ${params}, ${data})"

    } catch (e) {

    	log.error "Something unexpected went wrong in tcCommandAsync: ${e}"

	}//try / catch for asynchttpPost

}//async post command



def asyncResponse(response, data) {

    //log.debug "asyncresponse was Executed"

    if (response.hasError()) {

        log.debug "error response data: ${response.errorData}"

        try {

            // exception thrown if xml cannot be parsed from response

            log.debug "error response xml: ${response.errorXml}"

        } catch (e) {

            log.warn "error parsing xml: ${e}"

        }

        try {

            // exception thrown if json cannot be parsed from response

            log.debug "error response json: ${response.errorJson}"

        } catch (e) {

            log.warn "error parsing json: ${e}"

        }

    }



    response = response.getXml()

 	//log.debug "data:  ${data}"

  //log.debug "response received: ${response}"

    try {

    	def handler = data.get('handler')

        def callback = data.get('callback')



        if(handler == "login") {

            if(response.ResultCode == "0") {

            	loginResponse(response.SessionID, callback)

            }

            else {

                log.error "Command Type: ${data} failed with ResultCode: ${resultCode} and ResultData: ${resultData}"

            }

        }

        else {

            //validate response

            def resultCode = response.ResultCode

            def resultData = response.ResultData



            switch(resultCode) {

                case "0": //Successful Command

                case "4500": //Successful Command for Arm Action

                    state.tokenRefresh = now() //we ran a successful command, that will keep the token alive

                      //log.debug "Handler: ${data.get('handler')}"

                      switch(handler) {

                          case "details":

                              //details handler would be executed only when you need LocationID & DeviceID, that is during initial Setup & Updates.

                              def locationId

                              def deviceId

                              def locationName

                              Map locationMap = [:]

                              Map deviceMap = [:]

                              response.Locations.LocationInfoBasic.each

                              {

                                  LocationInfoBasic ->

                                  locationName = LocationInfoBasic.LocationName

                                  locationId = LocationInfoBasic.LocationID

                                  deviceId = LocationInfoBasic.DeviceList.DeviceInfoBasic.DeviceID

                                  locationMap["${locationName}"] = "${locationId}"

                                  deviceMap["${locationName}"] = "${deviceId}"

                              }

                              atomicState.locationMap = locationMap

                              atomicState.deviceMap = deviceMap

                              log.debug "During 'details' handler, LocationMap is $atomicState.locationMap & DeviceMap is $atomicState.deviceMap"

                              break

                          case "panel":

                              updateAlarmStatus(getAlarmStatus(response))

                              break

                          case "refresh":

                              runIn(70,refresh)

                              break

                          default:

                              return

                              break

                      }//switch(data)

                    break

                case "-102":

                    //this means the Session ID is invalid, needs to login and try again

                    log.error "Command Type: ${data} failed with ResultCode: ${resultCode} and ResultData: ${resultData}"

                    log.debug "Attempting to refresh token and try again for method ${callback}"

                    state.token = null

                    if(state.loginRetry == null || state.loginRetry == 0) {

                      state.loginRetry = 1;

                      state.token = null

                      login(callback)

                    }

                    else {

                      state.loginRetry = 0;

                    }

                    break

                case "4101": //We are unable to connect to the security panel. Please try again later or contact support

                case "4108": //Panel not connected with Virtual Keypad. Check Power/Communication failure

                case "-4002": //The specified location is not valid

                case "-4108": //Cannot establish a connection at this time. Please contact your Security Professional if the problem persists.

                default: //Other Errors

                    log.error "Command Type: ${data} failed with ResultCode: ${resultCode} and ResultData: ${resultData}"

                    break

            }//switch

        }

	} catch (SocketTimeoutException e) {

        //identify a timeout and retry?

		log.error "Timeout Error: $e"

    } catch (e) {

    	log.error "Something unexpected went wrong in asyncResponse: $e"

	}//try / catch for httpPost

}//asyncResponse



// Other necessary functions.

def isTokenValid() {

	def isValid = true

    if(state.token == null) {

    	isValid = false

    }

    else {

        Long timeSinceRefresh = now() - (state.tokenRefresh != null ? state.tokenRefresh : 0)



        //return false if time since refresh is over 4 minutes (likely timeout)

        if(timeSinceRefresh > 240000) {

            state.token = null

            isValid = false

        }

    }



    return isValid

} // This is a logical check only, assuming known timeout values and clearing token on loggout.  This method does no testing of the actua





def getSessionDetails() {

	tcCommandAsync("GetSessionDetails", [SessionID: state.token, ApplicationID: settings.applicationId, ApplicationVersion: settings.applicationVersion], 0, "getSessionDetails") //This updates panel status

}



def getLocations() {

	login(getSessionDetails)

}



def getPanelMetadata() {

	tcCommandAsync("GetPanelMetaDataAndFullStatusEx", [SessionID: state.token, LocationID: settings.locationId, LastSequenceNumber: 0, LastUpdatedTimestampTicks: 0, PartitionID: 1], 0, "getPanelMetadata") //This updates panel status

}



def refresh() {

	if(isTokenValid()) {

    	refreshAuthenticated()

    }

    else {

    	login(refreshAuthenticated)

    }

}



def refreshAuthenticated() {

	getPanelMetadata() // Gets AlarmCode

}



def updateAlarmStatus(alarmCode) {

    if(state.alarmCode != alarmCode) {

        if (alarmCode == "10200") {

            log.debug "Status is: Disarmed"

            sendNotification("TotalConnect has Disarmed the ${settings.selectLocation} successfully", [method: "push"])

            //sendEvent(name: "lock", value: "unlocked", displayed: "true", description: "Disarming")

            //sendEvent(name: "switch", value: "off", displayed: "true", description: "Disarming")

            //sendEvent(name: "status", value: "Disarmed", displayed: "true", description: "Refresh: Alarm is Disarmed")

        } else if (alarmCode == "10203") {

            log.debug "Status is: Armed Stay"

            sendNotification("TotalConnect has Armed Stay the ${settings.selectLocation} successfully", [method: "push"])

            //sendEvent(name: "status", value: "Armed Stay", displayed: "true", description: "Refresh: Alarm is Armed Stay")

            //sendEvent(name: "switch", value: "on", displayed: "true", description: "Arming Stay")

        } else if (alarmCode =="10201") {

            log.debug "Status is: Armed Away"

            sendNotification("TotalConnect has Armed Away the ${settings.selectLocation} successfully", [method: "push"])

        }

    }

	//logout(token)

    state.alarmCode = alarmCode

}



// Gets Panel Metadata.

def getAlarmStatus(response) {

	String alarmCode

	alarmCode = response.PanelMetadataAndStatus.Partitions.PartitionInfo.ArmingState

	state.alarmStatusRefresh = now()

	return alarmCode

} //returns alarmCode