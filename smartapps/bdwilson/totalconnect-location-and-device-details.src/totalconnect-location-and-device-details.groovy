/**
 *  TotalConnect Location and Device Details
 *
 *  Copyright 2015 Yogesh Mhatre, Brian Wilson
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

/** 
 * Most of this is borrowed from: https://github.com/mhatrey/TotalConnect/blob/master/TotalConnectTester.groovy
 * Goal if this is to return your Location ID and Device ID to use with my Total Connect Device located here:
 *  https://github.com/bdwilson/SmartThings-TotalConnect-Device
 *
 * To install, go to the IDE: https://graph.api.smartthings.com/ide/app/create,
 * Create a new SmartApp from Code, Save, Publish, Install at your location and
 * enter your credentials for your TotalConnect account. 
 */


definition(
   	 	name: "TotalConnect Location and Device Details",
    	namespace: "bdwilson",
    	author: "Brian Wilson",
    	description: "Total Connect App to show you your Location and Device ID's for use with Total Connect Device",
    	category: "My Apps",
   	 	iconUrl: "https://s3.amazonaws.com/yogi/TotalConnect/150.png",
    	iconX2Url: "https://s3.amazonaws.com/yogi/TotalConnect/300.png"
)

preferences {
        section ("Switch Function: ON = Arm"){
        	input("switch1", "capability.switch", multiple: false, require: true)
    		input("userName", "text", title: "Username", description: "Your username for TotalConnect")
    		input("password", "password", title: "Password", description: "Your Password for TotalConnect")
	}
}

Map locationFound() {
	def applicationId="14588"
	def applicationVersion="1.0.34"
    log.debug "Executed location function during Setup"
    def token
	def paramsLogin = [
    	uri: "https://rs.alarmnet.com/TC21API/TC2.asmx/AuthenticateUserLogin",
    	body: [userName: settings.userName , password: settings.password, ApplicationID: applicationId, ApplicationVersion: applicationVersion]
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
        body: [ SessionID: token, ApplicationID: applicationId, ApplicationVersion: applicationVersion]
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

// End of Page Functions

def installed(){
	getDetails()
}
def updated(){
	unsubscribe()
    getDetails()
}

// Login Function. Returns SessionID for rest of the functions
def login(token) {
	def applicationId="14588"
	def applicationVersion="1.0.34"
	log.debug "===== Executed login ====="
	def paramsLogin = [
    	uri: "https://rs.alarmnet.com/TC21API/TC2.asmx/AuthenticateUserLogin",
    	body: [userName: settings.userName , password: settings.password, ApplicationID: applicationId, ApplicationVersion: applicationVersion]
    	]
		httpPost(paramsLogin) { responseLogin ->
    	token = responseLogin.data.SessionID 
       }
       log.debug "Smart Things has logged In. SessionID: ${token}" 
    return token
}       // returns token

def logout(token) {
		log.debug "During logout - ${token}"
   		def paramsLogout = [
    			uri: "https://rs.alarmnet.com/TC21API/TC2.asmx/Logout",
    			body: [SessionID: token]
    			]
   				httpPost(paramsLogout) { responseLogout ->
        		log.debug "Smart Things has successfully logged out"
        	}  
} //Takes token as arguement
// Gets Panel Metadata
Map panelMetaData(token, locationId) {
	def applicationId="14588"
	def applicationVersion="1.0.34"
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
	log.debug "AlarmCode is " + alarmCode
  return [alarmCode: alarmCode, lastSequenceNumber: lastSequenceNumber, lastUpdatedTimestampTicks: lastUpdatedTimestampTicks]
} //Should return alarmCode, lastSequenceNumber & lastUpdateTimestampTicks

// Get LocationID & DeviceID map
Map getSessionDetails(token) {
	def applicationId="14588"
	def applicationVersion="1.0.34"
	def locationId
    def deviceId
    def locationName
    Map locationMap = [:]
    Map deviceMap = [:]
	def getSessionParams = [
    					uri: "https://rs.alarmnet.com/tc21api/tc2.asmx/GetSessionDetails",
        				body: [ SessionID: token, ApplicationID: applicationId, ApplicationVersion: applicationVersion]
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
	log.debug "Location ID map is " + locationMap + " & Device ID map is " + deviceMap
  	return [locationMap: locationMap, deviceMap: deviceMap]
} // Should return Map of Locations

def getDetails() {
			def token = login(token)
            def details = getSessionDetails(token) // Get Map of Location
			logout(token)
}