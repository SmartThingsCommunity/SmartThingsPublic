/**
 *  TotalConnect
 *
 *  Copyright 2014 Yogesh Mhatre
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
 Version: v0.3.1
 Changes [November 12th, 2015]
 	- Added ability to select Total Connect Location for users with multiple Locations. For sigle location users, it will default to your location
 	- Implemented logic to successfully message. Though there is room for improvement
 	
 Version: v0.3.
 Changes [August 30th, 2015]
 	- Logic to check if the Arm/DisArm signal actually implemented or not.
    - User dont have to input LocationID & DeviceID. Its been capatured from the response now.
 
 */
 
definition(
    name: "TotalConnect v0.3.1",
    namespace: "Security",
    author: "Yogesh Mhatre",
    description: "Total Connect App to lock/unlock your home based on your location and mode",
    category: "My Apps",
    iconUrl: "https://s3.amazonaws.com/yogi/TotalConnect/150.png",
    iconX2Url: "https://s3.amazonaws.com/yogi/TotalConnect/300.png")

preferences {
    page(name: "credentials", title: "Total Connect Login", nextPage: "locationList", uninstall: false) {
        section ("Give your Total Connect credentials. Recommended to make another user for SmartThings") {
    		input("userName", "text", title: "Username", description: "Your username for TotalConnect")
    		input("password", "password", title: "Password", description: "Your Password for TotalConnect")
    		input("applicationId", "text", title: "Application ID - It is '14588' currently", description: "Application ID")
    		input("applicationVersion", "text", title: "Application Version", description: "Application Version")
			}
    	}
    page(name: "locationList", title: "Select the Total Connection Location for this App", nextPage: "success", content:"locationList")
    page(name: "success")
}

// Start of Page Functions
private locationList(params=[:]){
	def locations = locationFound()
    def options = locations.keySet() ?: []
	return dynamicPage(name:"locationList", title:"Pulling up the Location List!", install: true, uninstall: true) {
		section("Select from the following Locations for Total Connect.") {
			input "selectedLocation", "enum", required:true, title:"Select the Location", multiple:false, options:options
		}
	}
}
Map locationFound() {
    log.debug "Executed location function during Setup"
    def token
	def paramsLogin = [
    	uri: "https://rs.alarmnet.com/TC21API/TC2.asmx/AuthenticateUserLogin",
    	body: [userName: settings.userName , password: settings.password, ApplicationID: settings.applicationId, ApplicationVersion: settings.applicationVersion]
    	]
			httpPost(paramsLogin) { responseLogin ->
    		token = responseLogin.data.SessionID 
       }
    log.debug "Smart Things has logged In to get Locations. SessionID: ${token}"   
    def locationId
    def locationName
    def locationMap = [:]
    def getSessionParams = [
    	uri: "https://rs.alarmnet.com/tc21api/tc2.asmx/GetSessionDetails",
        body: [ SessionID: token, ApplicationID: settings.applicationId, ApplicationVersion: settings.applicationVersion]
    					]
   		httpPost(getSessionParams) { responseSession -> 
        						 responseSession.data.Locations.LocationInfoBasic.each
        						 {
        						 	LocationInfoBasic ->
        						 	locationName = LocationInfoBasic.LocationName
                                    locationId = LocationInfoBasic.LocationID
        						 	locationMap["${locationName}"] = "${locationId}"
        						 }    							
    				}
	log.debug "This is map during Settings " + locationMap
    
    def paramsLogout = [
    			uri: "https://rs.alarmnet.com/TC21API/TC2.asmx/Logout",
    			body: [SessionID: token]
    			]
   				httpPost(paramsLogout) { responseLogout ->
        		log.debug "Smart Things has successfully logged out during settings"
        	}
    return locationMap
}
def success() {

    dynamicPage(name: "success") {
        section {
            image "https://s3.amazonaws.com/yogi/TotalConnect/success.jpg"
        }
    }
}
// End of Page Functions

// SmartThings defaults
def installed() {
	//log.debug "Installed with settings: ${settings}"
    subscribe(location, checkMode)

}

def updated() {
	//log.debug "Updated with settings: ${settings}"
	unsubscribe()
    subscribe(location, checkMode)
}

// Logic for Triggers based on mode change of SmartThings
def checkMode(evt) {
    	if (evt.value == "Away") {
            	log.debug "Mode is set to Away, Performing ArmAway"
            	armAway()   
            }
        else if (evt.value == "Night") {
            	log.debug "Mode is set to Night, Performing ArmStay"
            	armStay()
            }
        else if (evt.value == "Home") {
            	log.debug "Mode is set to Home, Performing Disarm"
            	disarm()
        }
}

// Login Function. Returns SessionID for rest of the functions
def login(token) {
	log.debug "Executed login"
	def paramsLogin = [
    	uri: "https://rs.alarmnet.com/TC21API/TC2.asmx/AuthenticateUserLogin",
    	body: [userName: settings.userName , password: settings.password, ApplicationID: settings.applicationId, ApplicationVersion: settings.applicationVersion]
    	]
		httpPost(paramsLogin) { responseLogin ->
    	token = responseLogin.data.SessionID 
       }
       log.debug "Smart Things has logged In. SessionID: ${token}" 
    return token
} // Returns token      

// Logout Function. Called after every mutational command. Ensures the current user is always logged Out.
def logout(token) {
		log.debug "During logout - ${token}"
   		def paramsLogout = [
    			uri: "https://rs.alarmnet.com/TC21API/TC2.asmx/Logout",
    			body: [SessionID: token]
    			]
   				httpPost(paramsLogout) { responseLogout ->
        		log.debug "Smart Things has successfully logged out"
        	}  
}

// Gets Panel Metadata. Takes token & location ID as an argument
Map panelMetaData(token, locationId) {
	def alarmCode
    def lastSequenceNumber
    def lastUpdatedTimestampTicks
    def partitionId
 	def getPanelMetaDataAndFullStatus = [
    									uri: "https://rs.alarmnet.com/TC21API/TC2.asmx/GetPanelMetaDataAndFullStatus",
        								body: [ SessionID: token, LocationID: locationId, LastSequenceNumber: 0, LastUpdatedTimestampTicks: 0, PartitionID: 1]
    ]
   	httpPost(getPanelMetaDataAndFullStatus) {	response -> 
        										lastUpdatedTimestampTicks = response.data.PanelMetadataAndStatus.'@LastUpdatedTimestampTicks'
        										lastSequenceNumber = response.data.PanelMetadataAndStatus.'@ConfigurationSequenceNumber'
        										partitionId = response.data.PanelMetadataAndStatus.Partitions.PartitionInfo.PartitionID
        										alarmCode = response.data.PanelMetadataAndStatus.Partitions.PartitionInfo.ArmingState
                                                
    }
	//log.debug "AlarmCode is " + alarmCode
  return [alarmCode: alarmCode, lastSequenceNumber: lastSequenceNumber, lastUpdatedTimestampTicks: lastUpdatedTimestampTicks]
} //Should return alarmCode, lastSequenceNumber & lastUpdateTimestampTicks

// Get LocationID & DeviceID map
Map getSessionDetails(token) {
	def locationId
    def deviceId
    def locationName
    Map locationMap = [:]
    Map deviceMap = [:]
	def getSessionParams = [
    					uri: "https://rs.alarmnet.com/tc21api/tc2.asmx/GetSessionDetails",
        				body: [ SessionID: token, ApplicationID: settings.applicationId, ApplicationVersion: settings.applicationVersion]
    					]
   	httpPost(getSessionParams) { responseSession -> 
        						 responseSession.data.Locations.LocationInfoBasic.each
        						 {
        						 	LocationInfoBasic ->
        						 	locationName = LocationInfoBasic.LocationName
        						 	locationId = LocationInfoBasic.LocationID
        						 	deviceId = LocationInfoBasic.DeviceList.DeviceInfoBasic.DeviceID
        						 	locationMap["${locationName}"] = "${locationId}"
                                    deviceMap["${locationName}"] = "${deviceId}"
        						 }    							
    				}
	// log.debug "Location map is " + locationMap + "& Devie ID map is " + deviceMap
  	return [locationMap: locationMap, deviceMap: deviceMap]
} // Should return Map of Locations

// Arm Function. Performs arming function
def armAway() {        
        	def token = login(token)
    	def details = getSessionDetails(token) // Gets Map of Location
            // log.debug "This was Selected " + settings.selectedLocation
    	def locationName = settings.selectedLocation
    	def locationId = details.locationMap[locationName]
            // log.debug "ArmAway Function. Location ID is " + locationId
    	def deviceId = details.deviceMap[locationName]
            // log.debug "ArmAway Function. Device ID is " + deviceId
            
    	def paramsArm = [
    			uri: "https://rs.alarmnet.com/TC21API/TC2.asmx/ArmSecuritySystem",
    			body: [SessionID: token, LocationID: locationId, DeviceID: deviceId, ArmType: 0, UserCode: '-1']
    			]
   			httpPost(paramsArm) // Arming Function in away mode
        def metaData = panelMetaData(token, locationId) // Get AlarmCode
            while( metaData.alarmCode != 10201 ){ 
                pause(3000) // 3 Seconds Pause to relieve number of retried on while loop
                metaData = panelMetaData(token, locationId)
            }  
            //log.debug "Home is now Armed successfully" 
            sendPush("Home is now Armed successfully")     
   logout(token)
}

def armStay() {        
	def token = login(token)
    	def details = getSessionDetails(token) // Gets Map of Location
            // log.debug "This was Selected " + settings.selectedLocation
    	def locationName = settings.selectedLocation
    	def locationId = details.locationMap[locationName]
            // log.debug "ArmStay Function. Location ID is " + locationId
    	def deviceId = details.deviceMap[locationName]
            // log.debug "ArmStay Function. Device ID is " + deviceId
            
    	def paramsArm = [
    			uri: "https://rs.alarmnet.com/TC21API/TC2.asmx/ArmSecuritySystem",
    			body: [SessionID: token, LocationID: locationId, DeviceID: deviceId, ArmType: 1, UserCode: '-1']
    			]
   			httpPost(paramsArm) // Arming function in stay mode
    	def metaData = panelMetaData(token, locationId) // Gets AlarmCode
            while( metaData.alarmCode != 10203 ){ 
                pause(3000) // 3 Seconds Pause to relieve number of retried on while loop
                metaData = panelMetaData(token, locationId)
            } 
            //log.debug "Home is now Armed for Night successfully"     
 			sendPush("Home is armed in Night mode successfully")
    logout(token)
}

def disarm() {
	def token = login(token)
        def details = getSessionDetails(token) // Get Location & Device ID
            // log.debug "This was Selected " + settings.selectedLocation
        def locationName = settings.selectedLocation
    	def deviceId = details.deviceMap[locationName]
            // log.debug "DisArm Function. Device ID is " + deviceId
    	def locationId = details.locationMap[locationName]
            // log.debug "DisArm Function. Location ID is " + locationId

        	def paramsDisarm = [
    			uri: "https://rs.alarmnet.com/TC21API/TC2.asmx/DisarmSecuritySystem",
    			body: [SessionID: token, LocationID: locationId, DeviceID: deviceId, UserCode: '-1']
    			]
   			httpPost(paramsDisarm)  
   	def metaData = panelMetaData(token, locationId) // Gets AlarmCode
        	while( metaData.alarmCode != 10200 ){ 
                pause(3000) // 3 Seconds Pause to relieve number of retried on while loop
                metaData = panelMetaData(token, locationId)
            }
           // log.debug "Home is now Disarmed successfully"   
           sendPush("Home is now Disarmed successfully")
	logout(token)
         
}