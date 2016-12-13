/**
 * 
 *  This is the Total Connect Open Close Sensor Zone and Row Grabber
 *  A big thanks to @fordcrews for helping me along the way with this.
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
preferences {
	// See above ST thread above on how to configure the user/password.	 Make sure the usercode is configured
	// for whatever account you setup. That way, arming/disarming/etc can be done without passing a user code.
	input("userName", "text", title: "Username", description: "Your username for TotalConnect")
	input("password", "password", title: "Password", description: "Your Password for TotalConnect")
	// get this info by using https://github.com/mhatrey/TotalConnect/blob/master/TotalConnectTester.groovy 
	input("deviceId", "text", title: "Device ID - You'll have to look up", description: "Device ID")
	// get this info by using https://github.com/mhatrey/TotalConnect/blob/master/TotalConnectTester.groovy 
	input("locationId", "text", title: "Location ID - You'll have to look up", description: "Location ID")
	input("applicationId", "text", title: "Application ID - It is '14588' currently", description: "Application ID")
	input("applicationVersion", "text", title: "Application Version - use '3.0.32'", description: "Application Version")
    input("zonebegin", "text", title: "Row to start with", description: "Start with 0 the first time")
    input("zoneend", "text", title: "Row to end with", description: "Start with 5 the first time")
}
metadata {
	definition (name: "TotalConnect Open Close Sensor Info Grabber", namespace: "QCCowboy", author: "QCCowboy") {
	capability "Refresh"
	attribute "status", "string"
}


// UI tile definitions
	tiles {
		standardTile("refresh", "device.status", inactiveLabel: false, decoration: "flat") {
			state "default", label:"ZoneRow", action:"refresh.refresh", icon:"st.Office.office19"
}
		main "refresh"
		details ("refresh")
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
	return token
} // Returns token		

// Logout Function. Called after every mutational command. Ensures the current user is always logged Out.
def logout(token) {
	def paramsLogout = [
		uri: "https://rs.alarmnet.com/TC21API/TC2.asmx/Logout",
		body: [SessionID: token]
	]
	httpPost(paramsLogout) { responseLogout ->
		log.debug "Smart Things has successfully logged out"
	}  
}

// Gets Zone Metadata. Takes token & location ID as an argument
Map zoneMetaData(token, locationId) {
	def partitionId
    def tczones
	def tczoneID
    def tczoneStatus
    def tczoneDescription
	def getGetZonesListInStateEx = [
		uri: "https://rs.alarmnet.com/TC21API/TC2.asmx/GetZonesListInStateEx",
		body: [ SessionID: token, LocationID: locationId, PartitionID: 1, ListIdentifierID: 0]
	]
	httpPost(getGetZonesListInStateEx) {	response -> 
        tczoneID = response.data.ZoneStatus.Zones.ZoneStatusInfoEx
        }
	return [tczoneID: tczoneID]
} //Should return Zone ID Info

// Gets Panel Metadata. Takes token & location ID as an argument
Map panelMetaData(token, locationId) {
	def lastSequenceNumber
	def lastUpdatedTimestampTicks
	def partitionId
    def tczones
	def getPanelMetaDataAndFullStatus = [
		uri: "https://rs.alarmnet.com/TC21API/TC2.asmx/GetPanelMetaDataAndFullStatus",
		body: [ SessionID: token, LocationID: locationId, LastSequenceNumber: 0, LastUpdatedTimestampTicks: 0, PartitionID: 1]
	]
	httpPost(getPanelMetaDataAndFullStatus) {	response -> 
        tczones = response.data.PanelMetadataAndStatus.Zones.ZoneInfo
	}
	return [tczones: tczones]
} //Should return zone ID and description Information


def refresh() {		   
	def token = login(token)
	def zname = device.name
    def i = 0
    def zonebegin = settings.zonebegin as int
    def zoneend = settings.zoneend as int
    def locationId = settings.locationId
	def metaData = zoneMetaData(token, locationId) // Gets Information
    def zmetaData = panelMetaData(token, locationId) // Gets Information
    log.debug "Please copy and save list for future use"
	sendEvent(name: "refresh", value: "true", displayed: "true", description: "Zone Info Grab Completed") 
    log.debug "Zone ID and Description List"
    for (i = zonebegin; i <zoneend; i++) {
               log.debug "zone: ${zmetaData.tczones[i].'@ZoneID'} -- ${zmetaData.tczones[i].'@ZoneDescription'}"
               log.debug "zoneID: ${metaData.tczoneID[i].'@ZoneID'}  -- Row: ${[i]}"
	} 
log.debug "Please copy and save list for future use - run again for another zone range"
logout(token)
	sendEvent(name: "refresh", value: "true", displayed: "true", description: "Zone Info Grab Completed for this range") 
}

// parse events into attributes
def parse(String description) {
	log.debug "Parsing '${description}'"
}