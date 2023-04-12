/**
 *  Smart Home Entry and Exit Delay, Internet Keypad SmartApp 
 *  Functions: 
 *		Acts as a container/controller for Internet Keypad simulation device: arnb.org/keypad.html
 * 
 *  Copyright 2018 Arn Burkhoff
 * 
 * 	Changes to Apache License
 *	4. Redistribution. Add paragraph 4e.
 *	4e. This software is free for Private Use. All derivatives and copies of this software must be free of any charges,
 *	 	and cannot be used for commercial purposes.
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
 *	Created from Ecobee Suite Service Manager,  Original Author: scott Date: 2013
 *	Updates by Barry A. Burke (storageanarchy@gmail.com) 2016, 2017, & 2018
 *	All of the unused coded was removed, not much left. It was going to be a service manager, but ended up as a Smartapp
 *  
 *	Jun 02, 2018 v1.0.1 Add function getAtomic used by parent to get atomicState information, otherwise not possible 
 *	May 25, 2018 v1.0.0 Convert to SHMDelay child app 
 *	May 23, 2018 v1.0.0 Strip out all unused code, set version to 1.0.0
 *						prepare for initial Beta release
 *  May 08, 2018 v0.0.0 Create
 */  
import groovy.json.JsonOutput

def version()
	{
	return "1.0.1";
	}
def VersionTitle() 
	{
	return "(${version()}) Connect Internet Keypad to SmartThings"
	}

definition(
	name: "SHM Delay Simkypd Child",
    namespace: "arnbme",
	author: "Arn Burkhoff",
	description: "${VersionTitle()}",
    parent: "arnbme:SHM Delay",
	category: "My Apps",
    iconUrl: "https://www.arnb.org/IMAGES/hourglass.png",
    iconX2Url: "https://www.arnb.org/IMAGES/hourglass@2x.png",
    iconX3Url: "https://www.arnb.org/IMAGES/hourglass@2x.png",
    singleInstance: false)

preferences 
	{
	page(name: "pageZeroVerify")
	page(name: "pageZero", nextPage: "pageZeroVerify")
	page(name: "pageOne", nextPage: "pageOneVerify")
	page(name: "pageTwo", nextPage: "pageTwo")		//recap page when everything is valid. No changes allowed.
    page(name: "removePage")
	}

mappings
	{
//	path("/oauth/initialize") {action: [GET: "oauthInitUrl"]}
//	path("/oauth/callback") {action: [GET: "callback"]}
	path("/keypost/:command") {action: [GET: "api_pinpost"]}
    }

//	This is the entry point for phoneapp and browser posting and panic requests
def api_pinpost() 
	{
    def String cmd = params.command
//	log.debug "api-keypost command: $cmd"
	if (cmd.matches("([0-3][0-9]{4})"))		
		{
		def keycode = cmd.substring(1)
		def armMode = cmd.substring(0,1)
		log.debug "valid command: ${armMode} and pin received"
		simkeypad.deviceNotification(keycode, armMode)		//create an event in simulated keypad DTH	
//		log.debug "returned from simkeypad"
		}
	else	
	if (cmd=='1')
		{
		log.debug "kypd svcmgr panic received"
//		simkeypad.devicePanic()								//for reasons unknown, this fails with an error	
		simkeypad.deviceNotification('panic',1)				//create a Panic Event in simulated keypad DTH	
		}
	else		
		httpError(400, "$cmd is not a valid data")
	}

def pageZeroVerify()
//	Verify this is installed as a child
	{
	if (parent && parent.getInstallationState()=='COMPLETE')
		{
		pageOne()
		}
	else
		{
		pageZero()
		}
	}	

def pageZero()
	{
	dynamicPage(name: "pageZero", title: "This App cannot be installed", uninstall: true, install:false)
		{
		section
			{
			paragraph "This SmartApp, SHM Delay User, must be used as a child app of SHM Delay."
			}
		}
	}	
	
def pageOne() 
	{	
	dynamicPage(name: "pageOne", title: "${VersionTitle()}", install: false, uninstall: false) 
		{
		if (state.error_data)
			{
			section ("${state.error_data}")
			state.remove("error_data")
			}
		
//		verify oauth is enabled for this app
		if (!atomicState.accessToken)
			{
			try 
				{
				createAccessToken()		//seems to store into atomicState.accessToken along with state.accessToken
				revokeAccessToken()		//something weird going on with this
				atomicState.accessToken=null
				state.remove("accessToken")	//do this or the atomicState.accessToken test works below. WTF
				}
			catch(Exception e)
				{
				section ("Please verify that OAuth has been enabled in " +
					"the SmartThings IDE for the 'SHM Delay Simkypd Child' SmartApp, scroll down, click 'Update', go back and try again.")
				section ("$e")
				}
			}
		
		if(atomicState.accessToken)
			{
			section("ID is\n${atomicState.accessToken.substring(0,8)}") 
			section()
				{
				href ("removePage", description: "Tap to revoke ID", title: "Revoke ID")
				}
			}
		else	
			section("ID is generated when this profile is saved.") 

		section
			{
			input "simkeypad", "device.InternetKeypad", multiple: false, required:true, submitOnChange: true, 
				title: "Simulated Keypad Device"
			}
		
		if (simkeypad)
			{
			section([mobileOnly:true]) 
				{
				label title: "Profile name", defaultValue: "Profile: Ikpd: ${simkeypad}", required: false
				}
			}	
		else	
			{
			section([mobileOnly:true]) 
				{
				label title: "Profile name", required: false
				}
			}	
		}
	}	

//	Verify the device and profile name are unique
page(name: "pageOneVerify")
def pageOneVerify() 				//edit page one info, go to pageTwo when valid
	{
	def error_data = ""
	error_data=isunique()
	if (error_data!="")
		{
		state.error_data=error_data.trim()
		pageOne()
		}
	else
		{
		pageTwo()
		}
	}	

def isunique()
	{
	def unique = ""
	def children = parent?.getChildApps()
	children.each
		{ child ->
		if (child.getName()=="SHM Delay Simkypd Child")		//process only simulated keypad profiles	
			{
//			log.debug "${child.simkeypad} ${child.getLabel()} ${child.getId()} ${simkeypad} ${app.getLabel()} ${app.getId()}"
//			verify unique keypad id without as String it fails dont know why
			if (child.simkeypad as String == simkeypad as String && child.getId() != app.getId())
				{
				unique+='Simulated Keypad Device is already in use\n\n'
				}
//			verify unique label (profile name)
			if (child.getLabel() == app.getLabel() && child.getId() != app.getId())
				{
				unique+='Duplicate Profile Name\n'
				}
			}
		}
	return unique
	}

//	This page summarizes the data prior to save	
def pageTwo()
	{
	dynamicPage(name: "pageTwo", title: "Verify settings then tap Save, or tap < (back) to change settings", nextPage: "pageTwo", install: true, uninstall: false)
		{
		section
			{
			if(atomicState.accessToken)
				{
				paragraph "ID is\n${atomicState.accessToken.substring(0,8)}"
				}
			else
				paragraph "ID will be generated when profile is saved. View profile after save to get the ID" 
			paragraph "Simulated Keypad Device is ${simkeypad}"
			paragraph "Name: ${app.getLabel()}\nModule SHM Delay Simkypd Child ${version()}"
			}	
		remove("Go back to page One to Remove")
		}
	}	

def removePage()
	{
	dynamicPage(name: "removePage", title: "Remove Keypad Authorization", install: false, uninstall: true) 
		{
		if (atomicState.accessToken)
			{
			def b64= atomicState.accessToken.encodeAsBase64()
			revokeAccessToken()
			atomicState.accessToken=null
			try {
				def url='https://www.arnb.org/shmdelay/oauthkill_st.php'
				url+='?k='+b64   			
				include 'asynchttp_v1'
				asynchttp_v1.get('getResponseHandler', [uri: url])
				}
			catch (e)
				{
				section()
					{
					paragraph ("Error initializing SHM Delay Keypad Kill: unable to connect to the database.\n\nIf this error persists, view Live Logging in the IDE for " +
								"additional error information.")
					paragraph ("Detailed Error: ${e}")			
					}
				}
			}
		section ("ID was deactivacted and no longer useable. Tap Remove to delete")
		}
	}	

def create_oauth()
	{
	try 
		{
		atomicState.accessToken = createAccessToken()
		}
	catch(Exception e)
		{
		if (atomicState.accessToken)
			{
			revokeAccessToken()
			atomicState.accessToken=null
			}
		}
	if (atomicState.accessToken)
		{
		def redirectUrl = buildRedirectUrl //"${serverUrl}/oauth/initialize?appId=${app.id}&access_token=${atomicState.accessToken}"
//		log.debug ("${redirectUrl}")
		def b64= redirectUrl.encodeAsBase64()
//		log.debug ("${b64.size()} ${redirectUrl.encodeAsBase64()}")
		try {
			def url='https://www.arnb.org/shmdelay/oauthinit_st.php'
			url+='?i='+b64   			//stop this data from interacting 
			include 'asynchttp_v1'
			asynchttp_v1.get('getResponseHandler', [uri: url])
			}
		catch (e)
			{
			revokeAccessToken()
			atomicState.accessToken=null
/*			section()
				{
				paragraph ("Error initializing SHM Delay Keypad Svcmgr Authentication: unable to connect to the database.\n\nIf this error persists, view Live Logging in the IDE for " +
							"additional error information.")
				paragraph ("Detailed Error: ${e}")			
				}
*/			}
		}
	}

def installed() {
    log.debug "Installed with settings: ${settings}"
	create_oauth()
    initialize()
}

def updated() {
    log.debug "Updated with settings: ${settings}"
    unsubscribe()
    initialize()
}


def initialize()
	{
	subscribe (simkeypad, 'codeEntered',  keypadCodeHandler)
	subscribe (simkeypad, 'contact.open', keypadPanicHandler)
	}

def keypadCodeHandler(evt)
	{
	log.debug 'simkeypad enter keypadCodeHandler'
	parent.keypadCodeHandler(evt)
	}
	
def keypadPanicHandler(evt)
	{
	log.debug 'simkeypad enter keypadPanicHandler'
	parent.keypadPanicHandler(evt)
	}
	

//	Process response from async execution 
def getResponseHandler(response, data)
	{
    if(response.getStatus() == 200)
    	{
		def results = response.getJson()
		log.debug "SHM Delay response ${results.msg}"
		if (results.msg != 'OK')
    		sendNotificationEvent("${results.msg}")
        }
    else
    	sendNotificationEvent("SHM Delay, HTTP Error = ${response.getStatus()}")
    }	

private def getServerUrl()          { return "https://graph.api.smartthings.com" }
private def getShardUrl()           { return getApiServerUrl() }
private def getCallbackUrl()        { return "${serverUrl}/oauth/callback" }
private def getBuildRedirectUrl() 	{ return "${serverUrl}/oauth/initialize?appId=${app.id}&access_token=${atomicState.accessToken}&apiServerUrl=${shardUrl}"}
def getAtomic(field_name)
	{return atomicState[field_name]}	//allow parent to get atomic data from child