/**
 *  Broadlink Service Manager for Smart Switch Devices v1.4
 *
 *  Copyright 2016 Neal Hinson
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
 *
 *  Purpose: Setup interface between RM Plugin and Broadlink switches via LAN
 *  Author:   D. Neal Hinson
 *  Date Started:   Dec 07, 2016
 *  Last Edited:    Dec 29, 2016
 *
 */

definition (
    name: "Broadlink Service Manager v1.4"  ,
    namespace: "DNH-Automation",
    author: "Neal Hinson",
    description: "Service Manager interface between Broadlink devices and RM Tasker plugin",
    category: "SmartThings Labs",
	singleInstance: true,
	iconUrl:   "http://app-icon-bucket.s3.amazonaws.com/Broadlink.png",
	iconX2Url: "http://app-icon-bucket.s3.amazonaws.com/Broadlink%402x.png",
    iconX3Url: "http://app-icon-bucket.s3.amazonaws.com/Broadlink%402x.png"	 )
	{
        appSetting "RM_URL"
        appSetting "RM_Port"
	}
    
import groovy.json.*

preferences {
	page(name:"page1", title: "Broadlink Service Manager", /*nextPage:"RMpluginPage",*/ install:true, uninstall:true) { 
			section {                       
				href (name:"Page-1", page:"RMpluginPage", description:"Set IP:Port for RM Plugin.",
                 			image:"http://app-icon-bucket.s3.amazonaws.com/Broadlink.png" )                        
			}
            section {
				href (name:"Page-2", page:"deviceDiscovery", description:"Import devs from \"RM Plugin\".",
                 			image:"http://app-icon-bucket.s3.amazonaws.com/Broadlink.png" )                        
			}
            section {
				href (name:"Page-3", page:"configDevice", description:"Add A Brand New Device!",
                 			image:"http://app-icon-bucket.s3.amazonaws.com/Broadlink.png" )                        
			}
		/*	section ( mobileOnly:true, hidden:true ) { 
			    input ("allSwitches", "capability.switch", required:false )                
			}   */
				//IMPORTANT NOTE: the "install/uninstall" settings belongs in the dynamicPage! Placing it in "page" WILL work, as long as they both match exactly.
    }			//IMPORTANT NOTE: if "install" is not set specifically, it defaults to "install:true" which prevents it's going to "nextPage"!!
	page(name:"RMpluginPage",    title: "Set RM Plugin Port" ) 
    page(name:"deviceDiscovery", title: "Import Broadlink Switches from \"RM\"" )  	//, refreshTimeout:5
    page(name:"configDevice",    title: "Add A New Device" ) 		
} //////////////////////////


def RMpluginPage() {
	log.debug "------------------------------------- RM Address Update ------------------------------------------------"
	state.Hidden = "false"
	return dynamicPage(name: "RMpluginPage", title: "Broadlink Service Manager", install:false, uninstall:false ) { 
            section("Entered the local IP:PORT from the \"RM Tasker plugin\", under \"HTTP Bridge -> Web Console\".")  {  
                input name:"RMip", type:"string", title: "RM Plugin - IP address", defaultValue: "", required: true 
                input name:"RMport", type:"string", title: "RM Plugin - Port",  defaultValue: "", required: true
			}
	}														//RMport will act as the key for whether or not to execute "updateRMaddress()" during initialize()
} ////////////////////////// 
def deviceDiscovery() { 	//IMPORTS NEW DEVICES FROM RM-plugin SO WE CAN ADD THEM IF WE WANT.  DEVICES ALREADY IN S.T. WILL BE IN "STATE.DEVICES". GETS BOTH TYPES.
	log.debug "------------------------------------ Device Discovery I  -----------------------------------------------"
    def options = deviceDiscovery_II()
	return dynamicPage(name:"deviceDiscovery", title:"Discovery Started!", hidden:"$state.Hidden", install:false, uninstall:false  ) {  //nextPage: "", refreshInterval:5, 
		section("Select devices to add to smartthings. If they are already added they will just be ignored." ) {
			input name:"selectedDevs", type:"enum", title:"Select Devices (${options.size() ?: 0} found)", required:false, multiple:true, options:options
		}
	}														//selectedDevs will act as the key for whether or not to execute "addMultiDevices()" during initialize()
} //////////////////////////
def configDevice () {
	log.debug "------------------------------------ Configure Device --------------------------------------------------"
	return dynamicPage(name:"configDevice", title: "Add A Brand New Device", hidden:"$state.Hidden", install:false, uninstall:false  )  {  
        	section( displayDuringSetup:true )  { 
            	paragraph "All fields required. Enter MAC address found in the \"e-Control\" app, you used for setup."
				input "dLabel", "string",						//dLabel will act as the key for whether or not to execute "addSingleDevice()" during initialize()
                    title: "Switch Nickname",
                    description: "This is the name that will be used by SmartThings and ultimately Alexa",
                    defaultValue: "",
                    required:true
				input "dMac", "string",
                    title: "MAC address",
                    description: "Enter MAC addr of new SP(10024) switch:",
                    defaultValue: "b4:43:0d:ef:xx:xx",
                    required:true
				input "dType", "string",
                    title: "BroadLink Device-Type Handler",
                    description: "Enter type of device handler:",
                    defaultValue: "$state.DevHandler"			//i.e. "Broadlink Switch v1.4"   
                    required:true 
            }
	}
} //////////////////////////



def installed() {
	log.debug "Installed with settings: ${settings}"
    state.Hidden = "true"								//Set to "true" ONLY ONCE!
   	initialize()
}

def updated() {	
	log.debug "Updated with settings: ${settings}"
	unsubscribe()
    initialize()
} //////////////////////////



/************************************************************************************************************************
 *									HERE IS WHERE THE ACTUAL PROGRAMMING BEGINS											*
 ************************************************************************************************************************/
def initialize() {
	log.debug "Entering initialize() --------------------------------------------"

	//Set up environment
	initEnvironment()

	//Check to see if we added anything new up in "Preverences and Settings".
	if ( settings.RMport )		 	updateRMaddress()				//Did we change the RM bridge address?
	if ( settings.selectedDevs ) 	addMultiDevices()				//Did we select any devices from RM to add?
    if ( settings.dLabel ) 		 	addSingleDevice()				//Have we created a brand new device to add?
	resetKeyVars()													//Clear the key variables.

    //SUBSCRIBING IN SMART APPS ONLY  (It's supposed to be automatic with parse() )
	subscribe( location,   null,		switchHandler,		[filterEvents:false] ) 
    
    log.debug "------------------------------- End of initialize ----------------------------------"
} //////////////////////////

def resetKeyVars() {
	settings.RMport = ""	
	settings.selectedDevs = ""
    settings.dLabel = ""
} //////////////////////////

def initEnvironment() {
	//Check to make certain the user has set the appsettings before continuing
	log.debug "Entering initEnvironment() --------------------------------------------"

    //Initialize "state" object for storing app-specific data.
    state.Namespace = "DNH-Automation"
    state.Name = "Broadlink SPMini (10024)"
    state.DevHandler = "Broadlink Switch v1.4"
    state.Debug  = "false" 		//Heavy weight debug - Very Verbose
    state.Debug2 = "false"		//Medium weight debug
    state.Debug3 = "false"		//Lite weight debug - Unobtrusive
    
	//Set the RM bridge IP address from AppSetting and save to state map, if not already done.
    if ( !state.RM_Bridge )  { state.RM_Bridge  = "$settings.RMip" + ":" + "$settings.RMport"  } 
    log.debug "RM_Bridge equals \"${state.RM_Bridge}\" --------------------- "
    
	//Get my Hub Name and save to state map    
    state.HubName = location.name
    log.debug "Hub Name equals  \"${state.HubName}\" --------------------- "

	//Get my Hub UUID and save to state map
    def HubId = location.hubs[0].id
    state.HubId = HubId
    if (Debug) log.debug "Hub UUID equals  \"${state.HubId}\" --------------------- "

	//Find ST_Hub IP to use for future Callbacks, then save to state map.
 	def callBackIP = getCallBackAddress()
	state.callBackIP = callBackIP					// sendEvent( name:"Callback_ID", value:"${state.callBackIP}" )
    if (Debug) log.debug "Callback_ID equals  \"${state.callBackIP}\"  --------------------- "
    
    //Find ApiServerURL to use for future ResponseHandler(), then save to state map.
    state.ApiServerURL = getApiServerUrl()
    if (Debug3) log.debug "ApiServerURL equals  \"${state.ApiServerURL}\"  --------------------- "

    if (Debug) state.each {  log.debug "\r\n--- state.Element ----> $it"  } 		//This is only here for debug...    
} //////////////////////////

def deviceDiscovery_II() { 	//IMPORTS NEW DEVICES FROM RM-plugin SO WE CAN ADD THEM IF WE WANT TO.  DEVICES ALREADY IN S.T. WILL BE IN "STATE.DEVICES" 
    log.debug "------------------------------------ Device Discovery II -----------------------------------------------"
	def options = [:]									//Initialze an empty array    

	def RMdevices = getRMdevices().findAll{ 			//Gathers pre-existing list from "getRMdevices()", (via "state" object), and iterates through populating "options" 
		it.value.verified == true	
        def label = it.value.label ?:"Broadlink SPMini"  //if "label" exist use it, if not use "Broadlink SPMini as generic "label"
		def key = it.value.mac							//Hash array with the mac as the key ... That was MY plan, hehe!
		options["${key}"] = label						//This tracks the previously found devices for us to add lastly in the program
	}
	getBroadlinkDevs()
    return options										//The only time this actually gets used is during setup
} //////////////////////////

void verifyDevices() {									//Not currently being used but I'm leaving this here for future cloud-based version.
	def devices = getDevices().findAll { it?.value?.verified != true }		//Grabs all devices that have not been verified
} //////////////////////////

private verifyDevice(String deviceNetworkId, String ip, int port, String devicessdpPath) {
	//We won't actually be using this part since all devices have already been verified by the RM plugin ... and we
    //couldn't do it if we wanted to since these devices don't respond directly anyways. They use a broadcast method.
    //However, I'm leaving this here for future cloud-based version.
    if(ip) {
        def address = ip + ":8090"
        sendHubCommand(new physicalgraph.device.HubAction( [
            method: "GET",
            path: "/info",
            headers: [ HOST: address, ]
            ] ) )
    } else {
        log.warn("verifyDevice() IP address was empty")
    }
} //////////////////////////

def getBroadlinkDevs() {
    log.debug "--------------- Get Broadlink Devices ------------------"
    def host = "${state.RM_Bridge}"
	def httpReqst = """GET /devices HTTP/1.1\r\nHOST: $state.RM_Bridge\r\n\r\n"""		//Setting up "callback" without using subscribe() - - AWESOME CAP!!
    def hubaction = new physicalgraph.device.HubAction( httpReqst, physicalgraph.device.Protocol.LAN, state.RM_Bridge, [callback: queryDeviceHandler] ) 
    try {	sendHubCommand( hubaction ) 
    } catch (e) {
        	log.debug "Something went wrong with HubAction, \r\nRM Tasker Plugin is probably offline. \nERROR:-----> $e "
    }
} //////////////////////////

void queryDeviceHandler(physicalgraph.device.HubResponse hubResponse) {
    log.debug "--------------- Query Device Handler ------------------"
	//Here we maunally process the query respone from the RM plugin or Broadlink HUB.  HUB is WIP. (Work In Process)
	def body = hubResponse.body
	if (!body) return  					//We don't want to try to process any null arrays
 
    deviceCollector( body )				//Divvy up all the data and add to structure.
    
    def i=0, RMdevices = getRMdevices()
	RMdevices.each {  ++i
    	if (Debug) log.debug "\r\n---> RMdevices.0${i} ---> $it" 
    	if (Debug) log.debug "----------------------------------------------------------------------------"  
	}
    i=0 		//<-- ensure counter is reset for next go around .... yes, it's buggy!
} //////////////////////////

def deviceCollector( jsonResponse ) { 							//THIS IS WHERE ALL THE WORK GETS DONE!
    log.debug "------------------- Device Collector ----------------------"	
	//Parse the json.Response
	def bodies = new JsonSlurper().parseText( jsonResponse )	
    def RMdevices = getRMdevices()											//Get fresh list of devices before we start.
	bodies.each {
       	def myMac = it.mac													//Using "def" ensures myMac doesn't retain the value from last loop.
        if ( getDevices()."${myMac}" ) {									//Confirming that this device doesn't already exist 
            if (Debug2) log.debug "---> ${myMac} Exists already ----\n"
            //def child = getChildDevice(it.mac)							//IP may already be correct but updating it anyways! ... or not! (TBD / WIP)
            //if (child)  child.sync(it.networkAddress, it.deviceAddress)	//FIX ME last ... low priority.
        }    																//If dev already exist, skip over the rest. "Continue" doesn't work here so using "else" instead.
        else if ( it.type.contains("SPMini") || it.typeCode == 10024 )  {	//Checking both values just incase, by some fluke, one value is missing.  (Sanity check)
            def d = [:]
            d.networkAddress = it.lanaddr.split(':')[0]						//Record all other properties from RM plugin .... you never know what you'll need later.
            d.deviceAddress  = it.lanaddr.split(':')[1]
            d.hub = "${location.name}"										//Use as many "state" vars as possible to decrease typo's from client user.
            d.mac = it.mac
            d.name = "${state.Name}"
            d.type  = "${state.DevHandler}"
            d.label  = it.name
            d.lanaddr = it.lanaddr
            d.typeCode = it.typeCode
            d.verified = "true" 
            RMdevices = getRMdevices()						// We need to regenerate this list, every loop, to ensure we are ...
            RMdevices << ["${myMac}":d]						// ... appending to the very bottom of the entire stack of devs.
		} 
	}
} //////////////////////////



/************************************************************************************************************************
 *									HERE IS WHERE WE COMPLETE OUR DYNAMIC PAGE METHODS									*
 ************************************************************************************************************************/
def updateRMaddress() {
    log.debug "------------------------------ UPDATING RM/HUB ADDRESSES --------------------------------"
	//TODO: Update the RM_Bridge in both the "SERVICE MANAGER" and the "DEVICE HANDLER"
    
	//Set the RM bridge IP address from AppSetting and save to state map.
    state.RM_Bridge = "$settings.RMip" + ":" + "$settings.RMport"
	if (Debug3) log.debug "\r\n--------> RM_Bridge equals \"${state.RM_Bridge}\" --------- "

	//Clear the var.  We're done with it now and it needs to be cleared in case we crash in the next step
    settings.RMport = ""				//HOUSE-KEEPING:  Clear the variable right away	so we don't return at next initialize()			//FIX ME - continues to show up anyways!
    state.Hidden = "false" 				//HOUSE-KEEPING:  For use in preference pages.  Not working yet.  (WIP)

	//Now we have to change the RM_bridge on all the Broadlink devices
    def childDevices = getChildDevices().findAll() { it.name.contains("Broadlink") }
	childDevices.each {
		try {   it.changedata( "RM_Bridge", "${state.RM_Bridge}" )					//it.sendEvent(name:"RM_Bridge", value: "${state.RM_Bridge}" )
        } catch (e) {  
      			log.debug "----- RM_Bridge update failed on device ${it.label} ----->>>" 
	}	}
    getBroadlinkDevs()			//<--- If I just updated the RM_Bridge address then the 'RMdevices' array is almost certainly not loaded. 
	if (Debug2) log.debug "------------------------------------------------------------- "
} //////////////////////////

def addSingleDevice() {		//PARAMETERS:	//Signature:  addChildDevice(String namespace, String typeName, String deviceNetworkId, hubId, Map properties)
    log.debug "----------------------- Installing Single Child Device Here!! ------------------------"
	def myMac 	= settings.dMac
    def myLabel = settings.dLabel
	settings.dLabel = ""					//Guarantees it won't return here, even if app crashes, by clearing the variable early.
	addNewDevice("$myMac", "$myLabel")  			//This will add a new device to the SmartThing Hub (Registered as a "child-device")
} //////////////////////////

def addMultiDevices() {
    log.debug "------------------------------------------ Installing Multiple Child Devices Now!! ------------------------------------------"

	def i=0, devlist = (selectedDevs instanceof List) ? selectedDevs : [selectedDevs] 	// If only one device is selected, we don't get a list (when using simulator)
    if (Debug) devlist.each { log.debug "-------> selectedDevs ------> $it" }
	devlist.each { 	++i;
    	def myLabel = getRMdevices()."$it".label
		if (Debug2) log.debug "The params I passed -OUT- are myMac: $it -- and -- myLabel: " + getRMdevices()."$it".label + " ----------<<<<<<<<<<<<<<<<<<<<<<<<<<<"
		addNewDevice("$it", "$myLabel")
	} 
    log.debug "${i} selectedDevs items processed";  i=0;
} //////////////////////////

void addNewDevice( myMac, myLabel ) {		//Signature:  See "addSingleDevice()"	//Adds a childDevice to a Smart Hub.  //FIELDS: Label-->>nickname; type-->>"Broadlink Switch v1.4"
 	log.debug "The params I passed ---IN--- are myMac: $myMac -- and -- myLabel: $myLabel  ----------<<<<<<<<<<<<<<<<<<<<<<<<<<<"

	def d = getDevices()."$myMac"			//Confirming that this device doesn't already exist 
    if (Debug3) log.debug "---> \"d\" ---> $d"
	if ( d ) {
    	log.debug "Device \"$myMac\" found; ... leaving install ...."
    	return								//If it already exist, BAIL OUT!!
    }

	log.debug "Device \"$myMac\" -NOT- found; ... continuing install ...."
   
    def existingChildDev = getChildDevice("$myMac")		//Make sure it wasn't previously added as a childDevice
	if ( !existingChildDev ) {					//This device was found in our app in "state.devices", but, doesn't exist as a real smartThings dev yet.
        def newd = [:]							//String namespace - the namespace for the device. <- (Mandatory)
        newd.hub = state.HubId					//hubId - (optional) The hub's UUID. 
        newd.mac = myMac						//String deviceNetworkId 	- the mac address of the device <- (Mandatory)
        newd.name = state.Name					// n/a  - (optional)  i.e. "Broadlink SAwitch (10024)"
        newd.type  = state.DevHandler			//String typeName  			- the Device-type Handler name <- (Mandatory)
        newd.label  = myLabel					// n/a	- (optional)  i.e. "Bedside Fan"		//Map properties (optional??) - A map with device properties. <- (Optional)
        newd.verified = "false"

    	try {	def successful = addChildDevice("${state.Namespace}", "${state.DevHandler}", "${myMac}", "${state.HubId}", [name:"${state.Name}", label:"${myLabel}"] )
            } 	catch (e)  {  log.debug "Something went wrong adding child device ... what's up with that?! \n--- ERROR: ---> ${e} " 
            }
		if ( successful ) {
				def devices = getDevices()				//We don't want to append our list if we didn't successfully add the child device.
				devices <<  ["${myMac}": newd ]			//We confirmed that it didn't previously exist but was added to SThub, so go ahead and append our device list. 

				//Finish off by setting the RM_Bridge on the new device.
                try {   
                		d.changedata( "RM_Bridge", "${state.RM_Bridge}" ) 
                    } 	catch (e)	{ 
                    log.debug "Oops, something went wrong with RM_Bridge update. \nError: $e " 
                }
                if (Debug3) log.debug "Child device created & RM_Bridge successfully updated!! <<<<<<------------->>>>>>> "
		}
        else {
            	if (Debug3) log.debug "Opps, Child device wasn't created!! <<<<<<------------->>>>>>> "
        }
	} else {     	
        if (Debug3) log.debug "Child device already exist on hub, so not added! ... \n ExistingChildDev -> $existingChildDev"
        return 
	} 
	if (Debug) log.debug "\"Devices\" ---------> \n\n"
	if (Debug) log.debug "\n Device is ---> $d" 
} //////////////////////////

def getVerifiedDevices() {
    log.debug "Entering ------------------------------ getVerifiedDevices() ------------------------" 
	getDevices().findAll{ it?.value?.verified == true }
} //////////////////////////

def switchHandler(evt) {
    log.debug "------------ Switch Handler -------------"	//This is "subscribed-to" and runs continually.  Honestly, it's not very useful except for monitoring status.
	def description = evt.description
	parse(description)
} //////////////////////////
                                        /********************************************************************************************************************************/
                                        /* json = [timestamp:1481188788884, status:ok, deviceMac:b4:43:0d:ef:73:31, on:true, uri:/send, msg:Code On successfully sent]  */
def	parse(description) {				/********************************************************************************************************************************/
	def msg = parseLanMessage(description)
	if ( !msg.json ) return 			//If the value is empty, let's not create a massive lot of null pointers please!
    
 	msg.json.each {
		if (Debug3) if ("$it.key" == "deviceMac")   log.debug "This device is -----------> $it.value <---  		//TODO: Update Device Handler's status" 
		log.debug "$it.key -:- $it.value"
	}
    log.debug " -------------------------------------------------------------------------------- " 
} //////////////////////////

def parse_2(description) {				// => This is primarily a debug tool for seeing what you are receiving back from BL-Hub/RM-plugin
	log.debug "Entering parse() -----------------"
    def msg = parseLanMessage(description)

    def headersAsString = msg.header	// => headers as a string
    def headerMap = msg.headers			// => headers as a Map
    def body = msg.body 				// => request body as a string
    def status = msg.status				// => http status code of the response
    def json = msg.json					// => any JSON included in response body, as a data structure of lists and maps
    def xml = msg.xml					// => any XML included in response body, as a document tree structure
    def data = msg.data					// => either JSON or XML in response body (whichever is specified by content-type header in response)
} //////////////////////////

def getCallBackAddress() {
    // Gets the "Callback" address:port of the hub        
 	def ip   = location.hubs[0].localIP
    def port = location.hubs[0].localSrvPortTCP
 	"$ip" + ":" + "$port"
} //////////////////////////

def getDevices() {						//A list intended to follow/match added Child-devices.
	if (!state.devices) {
		state.devices = [:]
	}
	state.devices
} //////////////////////////

def getRMdevices() {					//List of RM plugin devices.
	if (!state.RMdevices) {
		state.RMdevices = [:]
	}
	state.RMdevices
} //////////////////////////


/*****************************************************************************************************************
 *											T H E   E N D 
 *****************************************************************************************************************/
