/**
 * HubConnect Remote Hub for SmartThings
 *
 * Copyright 2019-2020 Steve White, Retail Media Concepts LLC.
 *
 * HubConnect for Hubitat is a software package created and licensed by Retail Media Concepts LLC.
 * HubConnect, along with associated elements, including but not limited to online and/or electronic documentation are
 * protected by international laws and treaties governing intellectual property rights.
 *
 * This software has been licensed to you. All rights are reserved. You may use and/or modify the software.
 * You may not sublicense or distribute this software or any modifications to third parties in any way.
 *
 * By downloading, installing, and/or executing this software you hereby agree to the terms and conditions set forth in the HubConnect license agreement.
 * <https://hubconnect.to/knowledgebase/5/HubConnect-License-Agreement.html>
 *
 * Hubitat is the trademark and intellectual property of Hubitat, Inc. Retail Media Concepts LLC has no formal or informal affiliations or relationships with Hubitat.
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License Agreement
 * for the specific language governing permissions and limitations under the License.
 *
 */
Map getDriverVersion() {[platform: "SmartThings", major: 2, minor:0, build: 9600]}

import groovy.transform.Field
import groovy.json.JsonOutput

metadata
{
	definition(name: "HubConnect Remote Hub for SmartThings", namespace: "shackrat", author: "Steve White")
	{
		capability "Presence Sensor"
		capability "Switch"

		attribute "connectionType", "string"
		attribute "version", "string"
		attribute "hsmStatus", "string"
		attribute "modeStatus", "string"

		preferences
		{
		}
	}

	tiles(scale: 2)
	{
		multiAttributeTile(name:"switch", type: "lighting", width: 6, height: 4, canChangeIcon: true)
		{
			tileAttribute ("device.switch", key: "PRIMARY_CONTROL")
			{
				attributeState "on", label: 'On', action: "switch.off", icon: "st.switches.switch.on", backgroundColor: "#00A0DC", nextState: "turningOff"
				attributeState "off", label: 'Off', action: "switch.on", icon: "st.switches.switch.off", backgroundColor: "#ffffff", nextState: "turningOn"
				attributeState "turningOn", label: 'Turning On', action: "switch.off", icon: "st.switches.switch.on", backgroundColor: "#00A0DC", nextState: "turningOff"
				attributeState "turningOff", label: 'Turning Off', action: "switch.on", icon: "st.switches.switch.off", backgroundColor: "#ffffff", nextState: "turningOn"
			}
		}
        standardTile("presence", "device.presence", width: 2, height: 2, canChangeBackground: true)
		{
            state "present", labelIcon:"st.presence.tile.present", backgroundColor:"#00a0dc"
            state "not present", labelIcon:"st.presence.tile.not-present", backgroundColor:"#ffffff"
        }
		valueTile("hsmStatus", "hsmStatus", inactiveLabel: false, decoration: "flat", width: 2, height: 2)
		{
			state "default", label: '${currentValue}'
		}
		valueTile("modeStatus", "modeStatus", inactiveLabel: false, decoration: "flat", width: 2, height: 2)
		{
			state "default", label: '${currentValue}'
		}
		valueTile("connectionType", "connectionType", inactiveLabel: false, decoration: "flat", width: 3, height: 1)
		{
			state "default", label: 'Connection Type\n${currentValue}'
		}
		valueTile("version", "version", inactiveLabel: false, decoration: "flat", width: 3, height: 1)
		{
			state "default", label: '${currentValue}'
		}

		main "switch"
		details(["switch", "presence", "hsmStatus", "modeStatus", "connectionType", "version"])
	}
}


// Mappings
@Field static Map<String, Map> DEVICE_MAPPINGS =
[
	remoteDeviceCommand:		[GET: "/event/:deviceId/:deviceCommand/:commandParams"],
    getAllModes:				[GET: "/modes/get"],
    remoteModeChange:			[GET: "/modes/set/:name"],
    getAllHSMStates:			[GET: "/hsm/get"],
    remoteHSMChange:			[GET: "/hsm/set/:name"],
    systemSetCommStatus:		[GET: "/system/setCommStatus/:status"],
    systemSaveCustomDrivers:	[POST: "/system/drivers/save"],
    systemGetVersions:			[GET: "/system/versions/get"],
    systemRemoteInitialize:		[GET: "/system/initialize"],
    systemRemoteUpdate:			[GET: "/system/update"],
    systemGetTSReport:			[GET: "/system/tsreport/get"],
    systemRemoteDisconnect:		[GET: "/system/disconnect"],
    devicesSaveAll:				[POST: "/devices/save"],
    deviceSendEvent:			[GET: "/device/:deviceId/event/:event"],
    getDeviceSync:				[GET: "/device/:deviceId/sync/:type"],
    appHealth:					[GET: "/smartthings/discover"]
]


/*
	parse

	Purpose: 	This performs the equivilent of how the platform calls URL mappings for oAuth endpoints

    Notes:		This is for handling unsolicited (i.e. non-hubaction) notifications from the remote hub.
    			** parseLANMessage CANNOT be used because it returns URI in all lower case **
*/
def parse(String description)
{
	// Tokenize the lan message, then parse it into a Map.
	String[] parsedData = description.toString().split(", ")
    Map<String, String> httpResponseRaw = (Map) [:]
    parsedData.each
    {
		String[] parts = it.split(":")
        if (parts?.size() < 2) return // Shouldn't happen
		httpResponseRaw["${parts[0]}"] = parts[1]
    }

    // Quick sanity check
    if (httpResponseRaw?.headers != null)
    {
        // Headers are base64 encoded, so decode them, then split the request/response header into its 3 component parts
        List headersDecoded = new String(httpResponseRaw.headers.decodeBase64())?.split("\n")
        List httpRequest = headersDecoded[0]?.split(" ")

        // There must be three parts to any request or response..  If something is missing discard the message
        if (httpRequest?.size() == 3)
        {
           // Chunk up the request path, dropping the leading slash
            List requestPath = httpRequest[1].substring(1).split("/")
            if (httpRequest[0] == "GET" || httpRequest[0] == "POST")
            {
                Map parentAction = DEVICE_MAPPINGS.findResult
                {
                    if (it.value."${httpRequest[0]}" == null) return

                    // Chunk up the mapping path, dropping the leading slash
                    List parts = it.value."${httpRequest[0]}".substring(1).split("/")

                    // Simple match - numberof URL parts must be equal
                    if (parts.size() != requestPath.size()) return

                    // Match URL parts and map URL parameters
                    Map params = [:]
                    Integer matches = 0
                    parts.size().times
                    {
                      loop ->
                        if (parts[loop].startsWith(":")) params."${parts[loop].substring(1)}" = requestPath[loop]
                        if (parts[loop].startsWith(":") || parts[loop] == requestPath[loop]) matches++
                    }

                    // 100% matches means we found the correct mapping
                    if (matches == requestPath.size())
                    {
                        [callback: it.key, params: params]
                    }
                }

                // Call the handler method, if one was found
                if (parentAction != null)
                {
                	if (httpRequest[0] == "POST")
					{
						Map postReq = parseLanMessage(description)
                        parentAction.params << postReq.json
					}

                	def returnData
                	if (parentAction.params?.size() == 0)
                    {
                    	returnData = parent."${parentAction.callback}"()
                    }
                    else returnData = parent."${parentAction.callback}"(parentAction.params)

					// Since data cannot be retured from parse() to the server hub, we have to make a post back instead.
					physicalgraph.device.HubAction hubAction = (physicalgraph.device.HubAction) null
					if (returnData != null)
					{
                        String remoteIP = getDataValue("remoteIP")
                        String remotePort = getDataValue("remotePort")

						// Initiate POST request
                        hubAction = new physicalgraph.device.HubAction
                        (
                            method: "POST",
                            path:	"${parent.state.clientURI}/smartthings/callback/http${httpRequest[0].toLowerCase()}",
                            headers:
                            [
                                HOST:			"${remoteIP}:${remotePort}",
                                "Content-Type":	"application/json",
                                Accept: 		"*/*",
								Authorization: "Bearer ${state.clientToken}"
                            ],
                            body: returnData
                        )
                        sendHubCommand(hubAction)
                        return
					}
                }
                else log.error "There is no route to the handler available for the command or event received."
                return
			}
		}
    }
    else
    {
    	log.error "An invalid HTTP request or response has been received.  Please check the configuration of the remote hub device"
    }
}


/*
	httpGetWithReturn

	Purpose:	Helper function to format GET requests with the proper oAuth token.

	Notes:		Command is absolute and must begin with '/'
*/
def httpGetWithReturn(Map requestParams)
{
	String remoteIP = getDataValue("remoteIP")
	String remotePort = getDataValue("remotePort")

	// Initiate local LAN request
	physicalgraph.device.HubAction hubAction = new physicalgraph.device.HubAction
    (
    	[
            method: "GET",
            path:	requestParams.uri,
            headers:
            [
                HOST:			"${remoteIP}:${remotePort}",
                "Content-Type":	requestParams.requestContentType,
                Accept: 		"*/*"
            ] + requestParams.headers,
     	],
     	null,
        [callback: "httpGetWithReturnResponse"]
	)
	log.debug "httpGetWithReturn() Sending ${hubAction}"
	sendHubCommand(hubAction)
}


/*
	httpGetWithReturnResponse

	Purpose:	Helper function to process responses from httpGetWithReturnResponse.

	Notes:		Calls sendHTTPResponse in the RemoteClient SmartApp.
*/
void httpGetWithReturnResponse(physicalgraph.device.HubResponse response)
{
    log.debug "httpGetWithReturnResponse(): ${response.body}"

    Map jsonResponse = (Map) [:]
    if (response?.body != null)
    {
        try
        {
			jsonResponse = parseJson(response.body)
		}
		catch (errorException)
		{
			String errorMsg = "Error parsing JSON response from remote hub: ${errorException}."
            log.error errorMsg
            jsonResponse = [status: "error", message: errorMsg]
		}
    }
    else jsonResponse = [status: "warning", message: "No body received."]
	parent.httpGetWithReturnResponse(jsonResponse)
}


/*
	sendGetCommand

	Purpose:	Helper function to format GET requests with the proper oAuth token.

	Notes:		Executes async http request and does not return data.
*/
void sendGetCommand(Map requestParams)
{
	String remoteIP = getDataValue("remoteIP")
	String remotePort = getDataValue("remotePort")

	// Initiate local LAN request
	physicalgraph.device.HubAction hubAction = new physicalgraph.device.HubAction
    (
    	[
            method: "GET",
            path:	requestParams.uri,
            headers:
            [
                HOST:			"${remoteIP}:${remotePort}",
                "Content-Type":	requestParams.requestContentType,
                Accept: 		"*/*"
            ] + requestParams.headers,
     	],
     	null,
        [callback: "sendGetCommandResponse"]
	)
    log.debug "sendGetCommand(): Sending ${requestParams.uri}"
 	sendHubCommand(hubAction)
}


/*
	sendGetCommandResponse

	Purpose:	Helper function to process responses from sendGetCommand.

	Notes:		We don't actually do anything with this data, it just captures the return instead of parse().
*/
void sendGetCommandResponse(physicalgraph.device.HubResponse response)
{
}


/*
	httpPostWithReturn

	Purpose:	Helper function to format POST requests with the proper oAuth token.

	Notes:		Command is absolute and must begin with '/'
*/
void httpPostWithReturn(Map requestParams)
{
	String remoteIP = getDataValue("remoteIP")
	String remotePort = getDataValue("remotePort")

	// Initiate local LAN request
	physicalgraph.device.HubAction hubAction = new physicalgraph.device.HubAction
    (
    	[
            method: "POST",
            path:	requestParams.uri,
            headers:
            [
                HOST:			"${remoteIP}:${remotePort}",
                "Content-Type":	requestParams.requestContentType,
                Accept: 		"*/*"
            ] + requestParams.headers,
            body: requestParams.body,
     	],
     	null,
        [callback: "httpPostWithReturnResponse"]
	)
	log.debug "httpPostWithReturn() Sending ${requestParams.uri}"
	sendHubCommand(hubAction)
}


/*
	httpPostWithReturnResponse

	Purpose:	Helper function to process responses from httpPostWithReturn.

	Notes:		Calls sendPostCommandResponse in the RemoteClient SmartApp.
*/
void httpPostWithReturnResponse(physicalgraph.device.HubResponse response)
{
    Map jsonResponse = (Map) [:]
    if (response?.body != null)
    {
        try
        {
			jsonResponse = parseJson(response.body)
		}
		catch (errorException)
		{
			String errorMsg = "Error parsing JSON response from remote hub: ${errorException}."
            log.error errorMsg
            jsonResponse = [status: "error", message: errorMsg]
		}
    }
    else jsonResponse = [status: "warning", message: "No body received."]
	parent.httpPostWithReturnResponse(jsonResponse)
}


/*
	sendPostCommand

	Purpose:	Helper function to format POST requests with the proper oAuth token.

	Notes:		Returns JSON Map if successful.
*/
void sendPostCommand(Map requestParams)
{
	String remoteIP = getDataValue("remoteIP")
	String remotePort = getDataValue("remotePort")

	// Initiate local LAN request
	physicalgraph.device.HubAction hubAction = new physicalgraph.device.HubAction
    (
    	[
            method: "POST",
            path:	requestParams.uri,
            headers:
            [
                HOST:			"${remoteIP}:${remotePort}",
                "Content-Type":	requestParams.requestContentType,
                Accept: 		"*/*"
            ] + requestParams.headers,
            body: requestParams.body,
     	],
     	null
	)
	log.debug "sendPostCommand() Sending ${requestParams.uri}"
	sendHubCommand(hubAction)
}


/*
	sendPostCommandResponse

	Purpose:	Helper function to process responses from sendPostCommand.

	Notes:		We don't actually do anything with this data, it just captures the return instead of parse().
*/
void sendPostCommandResponse(physicalgraph.device.HubResponse response)
{
}


/*
	installed

	Doesn't do much other than call initialize().
*/
void installed()
{
	sendEvent([name: "switch", value: "off"])
	state.connectionType = "http"
	state.useProxy = false
	initialize()
}


/*
	updated

	Doesn't do much other than call initialize().
*/
void updated()
{
	initialize()
}


/*
	initialize

	Doesn't do much other than call refresh().
*/
void initialize()
{
	log.trace "Initialize virtual Hub device..."

	sendEvent([name: "connectionType", value: state.connectionType])
	sendEvent([name: "version", value: "v${driverVersion.major}.${driverVersion.minor}.${driverVersion.build}"])
    sendEvent([name: "switch", value: "on"])
}


/*
	uninstalled

	Reports to the remote that this device is being uninstalled.
*/
void uninstalled()
{
	// Disable communications
	parent?.setCommStatus(true)
}


/*
	setConnectionType

	Called by Server Instance or Remote Client to set the connection type.
*/
void setConnectionType(String connType, String remoteIP, String remotePort, String proxyIP, String proxyPort, Boolean useProxy)
{
	state.connectionType = connType
	updateDataValue("remoteIP", remoteIP)
	updateDataValue("remotePort", remotePort)

	// Switch connections
	if (connType == "http")
	{
		sendEvent([name: "connectionType", value: "http"])
	}
	else if (connType == "hubaction")
	{
		sendEvent([name: "connectionType", value: "hubaction"])
	}

	initialize()
	log.info "Switching connection to ${device.label ?: device.name} to ${connType}"
}


/*
	on

	Enable communications from the remote hub.
*/
void on()
{
	parent.setCommStatus(false)

	sendEvent([name: "switch", value: "on"])
	sendEvent([name: "presence", value: "present"])
	initialize()
}


/*
	off

	Disable communications from the remote hub.
*/
void off()
{
	parent.setCommStatus(true)

	sendEvent([name: "switch", value: "off"])
	sendEvent([name: "presence", value: "not present"])
    initialize()
}


/*
	pushCurrentMode

	Pushes the current mode of the server hub to the remote hub.
*/
void pushCurrentMode()
{
	parent.pushCurrentMode()
}


/*
	updateClientToken

	Saves the access token for the remote hub to state.
*/
void updateClientToken(String clientToken)
{
	state.clientToken = clientToken
}
def getPref(setting) {return settings."${setting}"}