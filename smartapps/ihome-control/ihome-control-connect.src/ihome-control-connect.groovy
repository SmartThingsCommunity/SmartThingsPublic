/**
 *  iHome (Connect)
 *
 *  Copyright 2016 EVRYTHNG LTD.
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
 *  Last reviewed: 11.08.2017
 *   - Use of variable serverUrl in the URLs. 
 *  Reviewed:20.07.2017
 *   - Merged content with the old version modified by SmartThings
 *   - Removed selection of plugs, all available plugs are imported by default
 *   - Added location selection
 *   - Added DeviceWatch-DeviceStatus event
 *   - Added unschedule call on initialising
 *   - Changed from schedule to runEvery5Minutes
 *   - Updated refreshThngs method to support add/delete plugs automatically when they are added/removed in iHome app
 *	Reviewed: 04.07.2017
 *   - Added support for iSP6X
 *   - Reimplemented the import with filtering using the new tag "Active" (removed serial and use thngId)
 *	Review: 20.04.2017
 *   - Added filter by deactive property
 *   - Removed duplicates by creation date
 *
 */
include 'localization'

definition(
    name: "iHome Control (Connect)",
    namespace: "ihome_control",
    author: "iHome",
    description: "Control your iHome Control devices within the SmartThings app!",
    category: "Convenience",
    iconUrl: "https://www.ihomeaudio.com/media/uploads/product/logos/iH_iHomeControlicon.png",
    iconX2Url: "https://www.ihomeaudio.com/media/uploads/product/logos/iH_iHomeControlicon.png",
    iconX3Url: "https://www.ihomeaudio.com/media/uploads/product/logos/iH_iHomeControlicon.png",
    singleInstance: true
)
{
	appSetting "clientId"       //Client Id of the SmartThings App in the iHome System
	appSetting "clientSecret"   //Client Secret of the SmartThings app in the iHome System
    appSetting "iHomeServer"    //URL of the iHome API
    appSetting "serverUrl"   	//Base URL of the server hosting the redirection URI
    appSetting "evrythngServer" //URL of the EVRYTHNG API (cloud control)
}

preferences {
	page(name: "iHomeAuth", content:"authenticationPage", install: false)
	page(name: "iHomeConnectDevices", title: "Import your iHome devices", content:"connectPage", install:false)
}

private getVendorName() { "iHome" }

/**********************************************************************************************
*
* AUTHENTICATION
*
* This block contains all the functions needed to carry out the OAuth Authentication
*
**********************************************************************************************/

/*
 * Authentication endpoints (needed for OAuth)
*/
mappings {
    path("/oauth/initialize") {action: [GET: "oauthInitUrl"]}
    path("/oauth/callback") {action: [GET: "callback"]}
}

/*
 * Authentication Page
 * Implements OAuth authentication with the Authorization Code Grant flow
*/
def authenticationPage()
{
	log.debug "Checking authorisation..."

	//Check first if the authorisation was already done before
    if(state.iHomeAccessToken == null)
	{
    	log.debug "iHome token not found, starting authorisation request"

    	//Check if the internal OAuth tokens have been created already
    	if (!state.accessToken){
           	log.debug "Creating access token for the callback"
			createAccessToken()
        }

        //Create the OAuth URL of the authorisation server
	    def redirectUrl = "${appSettings.serverUrl}/oauth/initialize?appId=${app.id}&access_token=${state.accessToken}&apiServerUrl=${getApiServerUrl()}"
        log.debug "Redirecting to OAuth URL initializer: ${redirectUrl}"

        //Display the connect your account section, it will redirect to the OAuth Authentication Server
        return dynamicPage(name: "iHomeAuth", title:"iHome Control", install:false) {
    		section ("") {

				paragraph "Welcome! In order to connect SmartThings to your ${vendorName} devices, you need to have already set up your devices using the ${vendorName} app."
        		href (url:redirectUrl,
                		style:"embedded",
                		required:true,
                        image:"https://www.ihomeaudio.com/media/uploads/product/logos/iH_iHomeControl_icon.png",
                		title:"Connect your iHome Account",
                        description:""
                		)
    		}
    	}
	}
    else
    {
    	log.debug "iHome token found. Loading connect page"
        loadThngs()
	   	return connectPage()
    }
}

/*
 * Authentication OAuth URL
 *   Creates the OAuth compliant URL to the Authorisation Server
 */
def oauthInitUrl() {

    log.debug "Creating OAuth URL..."

    // Generate a random ID to use as a our state value. This value will be used to verify the response we get back from the 3rd party service.
    state.oauthState = UUID.randomUUID().toString()

    def oauthParams = [
    		response_type: "code",
            client_id: appSettings.clientId,
    		state: state.oauthState,
            redirect_uri: "${appSettings.serverUrl}/oauth/callback"
    ]

    redirect(location: "${appSettings.iHomeServer}/oauth/authorize?${toQueryString(oauthParams)}")
}

/*
 * Helper class to provide feedback to the user about the authentication process
 *
 */
def connectionStatus(message, redirectUrl = null) {
	def redirectHtml = ""
	if (redirectUrl) {
		redirectHtml = """
			<meta http-equiv="refresh" content="3; url=${redirectUrl}" />
		"""
	}

    def html = """
<!DOCTYPE html>
<html>
<head>
    <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0">
    <title>SmartThings Connection</title>
    <style type="text/css">
        @font-face {
            font-family: 'Swiss 721 W01 Thin';
            src: url('https://s3.amazonaws.com/smartapp-icons/Partner/fonts/swiss-721-thin-webfont.eot');
            src: url('https://s3.amazonaws.com/smartapp-icons/Partner/fonts/swiss-721-thin-webfont.eot?#iefix') format('embedded-opentype'),
            url('https://s3.amazonaws.com/smartapp-icons/Partner/fonts/swiss-721-thin-webfont.woff') format('woff'),
            url('https://s3.amazonaws.com/smartapp-icons/Partner/fonts/swiss-721-thin-webfont.ttf') format('truetype'),
            url('https://s3.amazonaws.com/smartapp-icons/Partner/fonts/swiss-721-thin-webfont.svg#swis721_th_btthin') format('svg');
            font-weight: normal;
            font-style: normal;
        }
        @font-face {
            font-family: 'Swiss 721 W01 Light';
            src: url('https://s3.amazonaws.com/smartapp-icons/Partner/fonts/swiss-721-light-webfont.eot');
            src: url('https://s3.amazonaws.com/smartapp-icons/Partner/fonts/swiss-721-light-webfont.eot?#iefix') format('embedded-opentype'),
            url('https://s3.amazonaws.com/smartapp-icons/Partner/fonts/swiss-721-light-webfont.woff') format('woff'),
            url('https://s3.amazonaws.com/smartapp-icons/Partner/fonts/swiss-721-light-webfont.ttf') format('truetype'),
            url('https://s3.amazonaws.com/smartapp-icons/Partner/fonts/swiss-721-light-webfont.svg#swis721_lt_btlight') format('svg');
            font-weight: normal;
            font-style: normal;
        }
        .container {
         /*   width: 440px;
            padding: 40px;
            /!*background: #eee;*!/*/
            text-align: center;
        }
        img {
            vertical-align: middle;
        }
        img:nth-child(2) {
            margin: 0 30px;
        }
        p {
            font-size: 2em;
            font-family: 'Swiss 721 W01 Thin';
            text-align: center;
            color: #666666;
            padding: 0 40px;
            margin-bottom: 0;
        }
        /*
            p:last-child {
                margin-top: 0px;
            }
        */
        span {
            font-family: 'Swiss 721 W01 Light';
        }
        .image{
            width: 20%;
            /*height: 70px;*/
        }
    </style>
</head>
<body>
<div class="container">
    <img class="image" src="https://www.ihomeaudio.com/media/uploads/product/logos/iH_iHomeControlicon.png" alt="iHome icon" />
    <img src="https://s3.amazonaws.com/smartapp-icons/Partner/support/connected-device-icn%402x.png" alt="connected device icon" />
    <img class="image" src="https://s3.amazonaws.com/smartapp-icons/Partner/support/st-logo%402x.png" alt="SmartThings logo" />
    ${message}
</div>
</body>
</html>
	"""
	render contentType: 'text/html', data: html
}


/*
 * Handler of the OAuth redirection URI
 *
 */
def callback() {

	log.debug "OAuth callback received..."

    //OAuth server returns a code in the URL as a parameter
    state.iHomeAccessCode = params.code
    log.debug "Received authorization code ${state.iHomeAccessCode}"

    def oauthState = params.state

    def successMessage = """
			<p>Your iHome Account is now connected to SmartThings!</p>
            <p>Click 'Done' in the top corner to complete the setup.</p>
	"""

    def errorMessage = """
			<p>Your iHome Account couldn't be connected to SmartThings!</p>
            <p>Click 'Done' in the top corner and try again.</p>
	"""

    // Validate the response from the 3rd party by making sure oauthState == state.oauthInitState as expected
    if (oauthState == state.oauthState){

    	if (state.iHomeAccessCode == null) {
            log.debug "OAuth error: Access code is not present"
			connectionStatus(errorMessage)
    	}
    	else {
       		getAccessToken();
	       	if (state.iHomeAccessToken){
    	   		getEVTApiKey();
        	    if(state.evtApiKey){
       				 connectionStatus(successMessage)
            	}
            	else{
                     log.debug "OAuth error: EVT API KEY could not be retrieved"
                	 connectionStatus(errorMessage)
            	}
       		}
       		else {
                log.debug "OAuth error: Access Token could not be retrieved"
		       	connectionStatus(errorMessage)
       		}
    	}
	}
    else{
        log.debug "OAuth error: initial state does not match"
		connectionStatus(errorMessage)
    }
}

/**
* Exchanges the authorization code for an access token
*/
def getAccessToken(){
	log.debug "Getting iHome access token..."

    def tokenParams = [
        	grant_type: "authorization_code",
            code: state.iHomeAccessCode,
            client_id: appSettings.clientId,
            client_secret: appSettings.clientSecret,
            redirect_uri: "${appSettings.serverUrl}/oauth/callback"
	]
    def tokenUrl = "${appSettings.iHomeServer}/oauth/token/?" + toQueryString(tokenParams)

    log.debug "Invoking token URL: ${tokenUrl}"

    try{
        def jsonMap
        httpPost(uri:tokenUrl) { resp ->
            if(resp.status == 200)
            {
                jsonMap = resp.data
                if (resp.data) {
                    state.iHomeRefreshToken = resp?.data?.refresh_token
                    state.iHomeAccessToken = resp?.data?.access_token
                    log.debug "Access token received ${state.iHomeAccessToken}"
            	}
            }
        }
    }catch (groovyx.net.http.HttpResponseException e) {
    	log.warn "Error! Status Code was: ${e}"
    } catch (java.net.SocketTimeoutException e) {
        log.warn "Connection timed out, not much we can do here."
    }
}

def getEVTApiKey() {

	log.debug "Getting api key from the cloud"

    def apiKeyParams = [
        uri: "${appSettings.iHomeServer}/v3/evrythng/",
        headers: [
        	"Accept": "application/json",
            "Authorization": "Bearer ${state.iHomeAccessToken}"]
	]

    try {
        def jsonMap
        httpGet(apiKeyParams)
        { resp ->
            if(resp.status == 200)
            {
                jsonMap = resp.data
                if (resp.data)
                {
                    state.evtUserId = resp?.data?.evrythng_user_id
                    state.evtApiKey = resp?.data?.evrythng_api_key
                    log.debug "Api key received: ${state.evtUserId}/${state.evtApiKey}"

                    //Preload thngs after getting the api key
                    loadThngs()
	            }
    	    }
    	}
  	}catch (groovyx.net.http.HttpResponseException e) {
    	log.warn "Error! Status Code was: ${e.statusCode}"
    } catch (java.net.SocketTimeoutException e) {
        log.warn "Connection timed out, not much we can do here"
    }
}

/*
 * Maps the map to query parameters for the URL
 *
 */
def toQueryString(Map m)
{
	return m.collect { k, v -> "${k}=${URLEncoder.encode(v.toString())}" }.sort().join("&")
}

/**********************************************************************************************
* IMPORT
*  This block contains all the functions needed to import the plugs from the cloud into SmartThings
**********************************************************************************************/
def loadThngs()
{
    //Products in production account
    state.product = [:]
    state.product["UCfXBRHnse5Rpw7PySPYNq7b"] = "iHomeSmartPlug-iSP5"
    state.product["Ugrtyq8pAFVEqGSAAptgtqkc"] = "iHomeSmartPlug-iSP6"
    state.product["UXNtyNexVyRrWpAQeNHq9xad"] = "iHomeSmartPlug-iSP8"
    state.product["UF4NsmAEM3PhY6wwRgehdg5n"] = "iHomeSmartPlug-iSP6X"

	//Save the all the plugs in the state for later use
    state.thngs = [:]

	log.debug "Loading available devices..."
	def thngs = getThngs()

	thngs.each { thng ->
        //Check that the plug is compatible with a Device Type
        log.debug "Checking if ${thng.id} is a compatible Device Type"
        if (state.product[thng.product])
        {
        	thng.st_devicetype = state.product[thng.product]
   	  		state.thngs["${thng.id}"] = thng
   			log.info "Found compatible device ${state.thngs["${thng.id}"].name}"
        }
	}
}

/*
 * Import thngs page
 *  Loads the thngs available from the user, checks that they have a DeviceType associated
 *  and presents a list to the user
 *
 */
def connectPage()
{
	return dynamicPage(name: "iHomeConnectDevices", uninstall: true, install:true) {
        section(""){
           	input "selectedLocationId", "enum", required:false, title:"", multiple:false, options:["Default Location"], defaultValue: "Default Location", submitOnChange: true
           	paragraph "Devices will be added automatically from your ${vendorName} account. To add or delete devices please use the Official ${vendorName} App."
		}
	}
}

/*
 *  Gets the thngs from the cloud
 *  This is used as the discovery process
 */
def getThngs(){

    log.debug "Getting available devices..."

    def url = "${appSettings.evrythngServer}/thngs?filter=tags=Active"

    try {

    	httpGet(uri: url, headers: ["Accept": "application/json", "Authorization": state.evtApiKey]) {response ->
            if (response.status == 200) {
                log.debug "GET on /thngs was succesful"
                log.debug "Response to GET /thngs ${response.data}"
                return response.data
            }
        }
    } catch (groovyx.net.http.HttpResponseException e) {
    	log.warn "Error! Status Code was: ${e.statusCode}"
    } catch (java.net.SocketTimeoutException e) {
        log.warn "Connection timed out, not much we can do here"
    }
}

/*
 *  Gets a thng by id from EVRYTHNG
 *    Used for updates
 */
def getThng(thngId){

    log.trace "Getting device information..."

    def url = "${appSettings.evrythngServer}/thngs/" + thngId

    try {
        httpGet(uri: url, headers: ["Accept": "application/json", "Authorization": state.evtApiKey]) {response ->
            if (response.status == 200) {
                log.debug "GET on /thngs was succesful: ${response.data}"

				def isAlive = response.data.properties["~connected"]
				def d = getChildDevice(thngId)
				d?.sendEvent(name: "DeviceWatch-DeviceStatus", value: isAlive? "online":"offline", displayed: false, isStateChange: true)

                return response.data
            }
            else{
            	log.warn "Error! Status Code was: ${response.status}"
            }
        }
    } catch (groovyx.net.http.HttpResponseException e) {
    	log.warn "Error! Status Code was: ${e.statusCode}"
    } catch (java.net.SocketTimeoutException e) {
        log.warn "Connection timed out, not much we can do here"
    }
}

/*
 *  Adds all the available devices to SmartThings
 *  Invoked by the lifecycle initialise
 */
def importThngs() {

    def thngsToImport = []

    state.thngs.each { thng ->
    	thngsToImport.add(thng.key)
	}
	log.debug "Adding all available plugs...${thngsToImport}"

    //Remove unselected plugs
    log.debug "Checking to delete ${state.imported}"
    state.imported.each{ id ->
        if(thngsToImport){
        	if (thngsToImport.contains(id)){
   	        	log.debug "${id} is already imported"
        	} else{
	            log.debug "Removing device not longer available: ${id}"
				// Error can occur if device has already been deleted or is in-use by SmartApps. Should it be force-deleted?		 +        		deleteChildDevice(thng)
				try {
					deleteChildDevice(id)
				} catch (Exception e) {
					log.error "Error deleting device with DNI $thng: $e"
				}
            }
      	} else {
           	log.trace "Removing unselected device with id: ${id}"
        	try {
                deleteChildDevice(id)
            }
            catch(Exception error){
                log.error "Error deleting device with id -> ${id}: $error"
            }
        }
    }

    state.imported = [];

    thngsToImport.each { id ->
	   	log.debug "Importing plug with id: ${id} and serial: ${state.thngs["${id}"].identifiers.serial_num}"
		def smartThing = getChildDevice(id)

	    if(!smartThing) {
           	def newSmartThing = state.thngs.find { it.key == id }
	    	log.debug "Creating SmartThing: ${newSmartThing}"

			smartThing = addChildDevice("ihome_devices",
            	newSmartThing.value.st_devicetype,
                newSmartThing.value.id,
                null,
                [label:"${newSmartThing.value.name}"])

	        log.info "Created ${smartThing.displayName} with id ${smartThing.deviceNetworkId}"
    	}
        else {
            log.trace "${smartThing.displayName} with id ${id} already exists, skipping creation"
        }

        //save plug in state
        state.imported.add(id);

        //We need to get the current status of the plug
    	pollChildren(smartThing.deviceNetworkId)
	}
}


/**********************************************************************************************
*
* LIFECYCLE
*
**********************************************************************************************/

def installed() {
	log.debug "Application installed..."
	initialize()
}

def updated() {
	log.debug "Application updated..."
	unsubscribe()
    initialize()
}

def initialize() {
	log.debug "Application initialising..."
    importThngs()
	unschedule()
    //Refresh every five minutes for external changes in the thngs
    runEvery5Minutes("refreshThngs")
}

def uninstalled() {
	log.debug "Removing installed plugs..."
	getChildDevices().each {
    	log.debug "Deleting ${it.deviceNetworkId}"
        try {
       		deleteChildDevice(it.deviceNetworkId)
            }
        catch (e) {
        	log.warn "Error deleting device, ignoring ${e}"
    	}
    }
}

/**********************************************************************************************
* Properties and Actions UPDATES
*	This block contains the functionality to update property values and send actions in EVRYTHNG cloud
*   This methods are generic based on the EVRYTHNG API specification and are invoked from
*   the specific Device Type that handles the properties and action types
**********************************************************************************************/

/*
 *  Updates a property in EVRYTHNG
 */
def propertyUpdate(thngId, propertyUpdate){

	def url = "${appSettings.evrythngServer}/thngs/${thngId}/properties"

    def params = [
       	uri: url,
       	headers: [
            "Authorization": state.evtApiKey
       ],
      	body: propertyUpdate
    ]

	log.debug "Sending property update to the cloud: ${params}"

    try {
    	httpPutJson(params) { resp ->
        	if (resp.status == 200) {
	         	log.debug "Response from the cloud: ${resp}"
    			return true
    		}
            else {
	         	log.debug "Response status from the cloud not valid: ${resp}"
	            return false
            }
        }
    }
    catch (e) {
        log.debug "Something went wrong with the property update: ${e}"
        return false
    }
}

/*
 *  Sends an action to EVRYTHNG
 */
def sendAction(actionType, actionPayload){

	def url = "${appSettings.evrythngServer}/actions/${actionType}"

    def params = [
       	uri: url,
       	headers: [
            "Authorization": state.evtApiKey
       ],
       body: actionPayload
    ]

	log.debug "Sending action to the cloud: ${params}"

    try {
    	httpPostJson(params) { resp ->
        	if (resp.status == 201) {
	         	log.debug "Response from the cloud: ${resp}"
    			return true
    		}
            else {
	         	log.debug "Response status from the cloud not valid: ${resp}"
	            return false
            }
      }
    }
    catch (e) {
        log.debug "Something went wrong with sending the action: ${e}"
        return false
    }
}

/**
* Handler of the refreshing of all the imported things
*/
def refreshThngs(){
	log.debug "Refreshing thngs"

	//loading thngs to get plugs recently added or removed
	loadThngs()

	//import the plugs into SmartThings
	importThngs()
}

/*
 *  Utility function to poll for a Thng and update its properties
 */
def pollChildren(thngId){
    //get plug device
    def smartThing = getChildDevice(thngId)

	if (smartThing){
		//Get plug's latest state from the cloud
    	log.debug "Getting updates for ${thngId}"
    	def plug = getThng(thngId)

        if (plug == null){
        	smartThing.pollError()
        }
        else
        {
        	//Update name
        	smartThing.label = plug.name
        	smartThing.name = plug.name

 		   	//Update properties
			smartThing.updateProperties(plug.properties)
        }
    }
}

/* Status messages for all types of plugs */
def getConnectedMessage(){
	return "Connected"
}
def getConnectionErrorMessage(){
	return "Connection error. Please try again."
}
def getPlugNotConnectedMessage(){
	return "Your plug seems to be disconnected."
}