/**
 *  Copyright 2015 SmartThings
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
 *  BlueIris (LocalConnect2)
 *
 *  Author: Nicolas Neverov
 *  Date: 2017-04-30
 */

 metadata {
   definition (name: "blueiris2", namespace: "df", author: "df") {
      capability "Sensor"
      capability "Actuator"
        capability "Configuration"
        capability "Refresh"
        attribute "state", "enum", ["disarmed", "arming", "armed", "disarming", "unknown"]
        attribute "status", "string"
        command "arm"
        command "disarm"
        command "location" "STRING"
        command "retry"
        command "timeout"
   }

   // simulator metadata
   simulator {
   }

   preferences {
      input name:"username", type:"text", title: "Username", description: "BlueIris Username", required: true
      input name:"password", type:"password", title: "Password", description: "BlueIris Password", required: true
   }

   // UI tile definitions
   tiles(scale: 2) {
       multiAttributeTile(name:"bi_detail_tile", type:"generic", width:6, height:4) {
         tileAttribute("device.state", key: "PRIMARY_CONTROL") {
               attributeState "disarmed", label:"Disarmed", action:"arm", icon:"st.locks.lock.unlocked", backgroundColor:"#ffffff"
               attributeState "arming", label:"Arming...", action:"arm", icon:"st.locks.lock.locked", backgroundColor:"#79b821"
               attributeState "armed", label:"Armed", action:"disarm", icon:"st.locks.lock.locked", backgroundColor:"#79b821"
               attributeState "disarming", label:"Disarming...", action:"disarm", icon:"st.locks.lock.unlocked", backgroundColor:"#ffffff"
               attributeState "unknown", label:"Unknown", action:"retry", icon:"st.locks.lock.unknown", backgroundColor:"#ff0000"
               attributeState "refreshing", action:"Refresh.refresh", icon:"st.secondary.refresh", backgroundColor:"#ffffff"
            }
            tileAttribute("device.status", key: "SECONDARY_CONTROL") {
            attributeState("default", label:'${currentValue}', defaultState:true)
            }
      }

      standardTile("bi_refresh_tile", "device.refresh", decoration: "flat", width: 2, height: 2) {
         state "default", action:"Refresh.refresh", icon:"st.secondary.refresh"
      }

      main "bi_detail_tile"
      details(["bi_detail_tile", "bi_refresh_tile"])
   }
}

def fsmExecInternal(fsmDefinition, stateId, Map params)
{
   def actionResult = null;

   def fsmState = fsmDefinition[stateId?.toString()]
   if(fsmState == null || fsmState.isFinal) {
      return [error:"fsmExecInternal: state [$stateId] ${fsmState == null ? 'does not exist' : 'is final'} and cannot be actioned upon"]
   }

	while(!fsmState.isFinal && !actionResult?.isAsync) {
		actionResult = fsmState.action(params)
		fsmState = fsmDefinition[actionResult.nextStateId?.toString()]
		if(fsmState == null) {
			return [error:"fsmExecInternal: state [${actionResult.nextStateId}] does not exist and cannot be actioned upon"]
		}
		log.debug("fsmExecInternal: transitioned state [$stateId] to [$actionResult.nextStateId] (isFinal:${fsmState.isFinal?:false}); result isAsync:${actionResult.isAsync?:false}")
      	stateId = actionResult.nextStateId
	}
   	return [actionResult:actionResult]
}

def fsmGetStateId(Map persistentStg)
{
	persistentStg.fsmState
}

def fsmExec(Map persistentStg, fsmDefinition, Map params = null)
{
   def stateId = fsmGetStateId(persistentStg) ?: params.fsmInitialState
   if(!stateId) {
      return [error: "fsmExec: cannot determine initial state, must be specified via params.fsmInitialState"]
   }

	log.debug("fsmExec: ${persistentStg.fsmState ? 'proceeding' : 'starting'} with fsm state [$stateId]")
	params = (params != null ? params : [:])
	params.persistentStg = (params.persistentStg != null ? params.persistentStg : persistentStg)

   def rc = fsmExecInternal(fsmDefinition, stateId, params)
   if(rc.actionResult) {
      persistentStg.fsmState = rc.actionResult.nextStateId
   }
   return rc
}


def getBlueIrisHubAction(Map body)
{
	final host = getHostAddress();
	final path = "/json"

	def hubAction = new physicalgraph.device.HubAction(
		method: "POST",
		path: path,
		headers: [HOST:host],
		body: body
	)
	log.info "getBlueIrisHubAction: prepared hubaction $hubAction, requestId: $hubAction.requestId"

	hubAction
}

def sendError(statusMsg)
{
	sendEvent(name:"status", value: statusMsg)
	sendEvent(name:"state", value: "unknown")
	parent.onNotification("$device.displayName: $statusMsg");
}

def sendEventInit(cmd)
{
	def state;
	switch(cmd.id) {
    	case 'status': 
        	state = 'unknown'
            break
        case 'set': 
        	state = (cmd.workflow == 'arm' ? 'arming' : 'disarming')
            break
        default: 
        	state = 'unknown' //TODO: error
  	}
	sendEvent(name:"state", value: state)
	sendEvent(name:"status", value: "Logging in...")
}

def sendEventLogin(cmd)
{
	sendEvent(name:"status", value: "Opening session...")
}

def sendEventStatus(cmd)
{
	sendEvent(name:"status", value: "Getting status...")
}

def sendEventSet(cmd)
{
	log.debug("sendEventSet: executing ${cmd.workflow} workflow, ${cmd.context ? ('context: ' + cmd.context + ',') : ''} signal:${cmd.signal}, profile:${cmd.profile}")
	sendEvent(name:"status", value: "Executing '${cmd.workflow.toString() == 'arm' ? 'arm' : 'disarm'}${cmd.context ? ' ' + cmd.context : ''}' command")
}

def sendEventFinalize(cmd, signal, profile, workflow)
{
	def isArmed = (workflow.toString() == 'arm')
	def statusMsg = null
    def statusDetails = null
	if(cmd.id == 'set') {
        statusMsg = "Successfully ${isArmed ? 'armed' : 'disarmed'}${cmd.context ? ' \'' + cmd.context + '\'' : ''} ..."
        statusDetails = "Successfully ${isArmed ? 'armed' : 'disarmed'} ${cmd.context ? '\'' + cmd.context + '\'' : ''}"
  	} else if(cmd.id == 'status') {
		statusMsg = "Status is '${isArmed ? 'Armed' : 'Disarmed'}'"
    	statusDetails = "Current status is '${isArmed ? 'Armed' : 'Disarmed'}' [signal:${signal ? 'green' : 'red'}, profile:${profile}]"
	}
    
	sendEvent(name:"state", value: isArmed ? "armed" : "disarmed")
	sendEvent(name:"status", value: statusMsg)
	parent.onNotification("${device.displayName}: ${statusDetails}");
}


def getBIfFsmDef()
{
	[
		init: [
			action: {Map params ->
            	def cmd = params.cmd
   				params.persistentStg.cmd = params.cmd
				sendEventInit(cmd)
				parent.onBeginAsyncOp(getOperationTimeoutMs())
				def haction = getBlueIrisHubAction([cmd: 'login']);
				cmd.requestId = haction.requestId

				[nextStateId:'login', isAsync:true, hubAction:haction]
			}
		],
		login: [
			action: {Map params ->
            	def cmd = params.persistentStg.cmd            
                def respData = params.respMsg.data

				if(respData?.result == 'fail' && respData.session) {
                	sendEventLogin(cmd)
					
					final u = settings.username
    				final p = settings.password
					final token = "$u:${respData.session}:$p"
                    log.debug("getBIfFsmDef[login]: logging in user \"$u\"")
					def haction = getBlueIrisHubAction([cmd: 'login', session: "${respData.session}", response: "${token.encodeAsMD5()}"]);
					cmd.requestId = haction.requestId
                    cmd.session = respData.session
					[nextStateId: (cmd.id == 'set' ? 'set' : 'status'), isAsync:true, hubAction: haction]
				} else {
					log.error("getBIfFsmDef[login]: error: unexpected result from login call: $params.respMsg.data")
					parent.onEndAsyncOp()
					sendError("Error logging in: unexpected result $params.respMsg.data?.result")
					[nextStateId: 'error']
				}
			}
		],
		status: [
			action: {Map params ->
            	def cmd = params.persistentStg.cmd
                def respData = params.respMsg.data
				if(respData?.result == 'success') {
					sendEventStatus(cmd)
					def haction = getBlueIrisHubAction([cmd: 'status', session: "${cmd.session}"]);
					cmd.requestId = haction.requestId
					[nextStateId: 'finalize', isAsync:true, hubAction: haction]
				} else {
					parent.onEndAsyncOp()
					log.error("getBIfFsmDef[status]: error creating session: unsuccessful result from session login call: ${respData}");
					sendError("Error establishing session: ${respData?.data?.reason}")
					[nextStateId:'error']
				}
			}
		],
		set: [
			action: {Map params ->
            	def cmd = params.persistentStg.cmd
                def respData = params.respMsg.data
				if(respData?.result == 'success') {
					sendEventSet(cmd)
                    def hubActionParams = [cmd: 'status', session: "${cmd.session}"]
                    if(cmd.signal != null) {
                    	hubActionParams.signal = cmd.signal ? 1 : 0
                    }
                    if(cmd.profile != null) {
                    	hubActionParams.profile = cmd.profile
                    }
					def haction = getBlueIrisHubAction(hubActionParams);
					cmd.requestId = haction.requestId
					[nextStateId:'finalize', isAsync:true, hubAction: haction]
				} else {
					parent.onEndAsyncOp()
					log.error("getBIfFsmDef[action]: error creating session: unsuccessful result from session login call: ${respData}");
					sendError("Error establishing session: ${respData.data?.reason}")
					[nextStateId:'error']
				}
			}
		],
		finalize: [
			action: {Map params ->
              	boolean success = false
                final cmd = params.persistentStg.cmd
                final respData = params.respMsg.data
                
				if(respData?.result == 'success') {
                
                	def respSignal = (respData.data?.signal != null ? respData.data.signal == '1' :  null);
                    def respProfile = respData.data?.profile

					log.debug("getBIfFsmDef[finalize]: received 'success' response status, finalizing command ${cmd}, "
                    			+ "response: [signal:${respSignal}, profile:${respProfile}]")

					if( cmd.id == 'set'
                       	&& (cmd.signal == null || cmd.signal == respSignal)
                       	&& (cmd.profile == null || respProfile == cmd.profile.toString())) {

							sendEventFinalize(cmd, respSignal, respProfile, cmd.workflow)
                            success = true
                            
                 	} else if(cmd.id == 'status' && respSignal != null && respProfile != null) {

						def config = parent.onGetConfig()
						def workflow = deduceWorkflow(respSignal, respProfile, config)
                     	sendEventFinalize(cmd, respSignal, respProfile, workflow)
 						success = true
                   	} 
              	}

				parent.onEndAsyncOp()
                if(success) {
                	[nextStateId:'success']
              	} else {
					log.error("getBIfFsmDef[finalize]: error setting status: unsuccessful result from setting status/signal: $params.respMsg.data");
					sendError("Error setting status/signal: ${respData.data?.reason ?: 'signal mismatch...' }")
					[nextStateId:'error']
				}
			}
		],
		timeout: [
			action: {Map params ->
				log.error("getBIfFsmDef[timeout]: operation timed out")
				sendError("Operation timed out...")
				[nextStateId:'error']
			}
		],
		success: [
			isFinal:true
		],
		error: [
			isFinal:true
		]
	]
}


def configure()
{
	log.debug("configure: reseting states")
	sendEvent(name:"status", value: ".")
	sendEvent(name:"state", value: "disarmed") //TODO: initial state calc
 }


private getBIStg(boolean reset = false)
{
	if (state.biStg == null || reset) {
		state.biStg = [:]
	}
	state.biStg
}

private Map getBICommands()
{
	return [
    	set: {Map p ->
			def rc = [
            	id: 'set',
        		signal: p.signal,
        		profile: p.profile,
                workflow: p.workflow	//one of ['arm', 'disarm']
 			]
            if (p.context != null) {
            	rc.context = p.context	//optional (location mode name)
            }
            
            rc
        },
    	status: {->
			return [
            	id: 'status',
                status: true
 			]
        }

        
    ]
}

//signal: null (N/A), true(green), false(red)
//profile: null (N/A), number
private deduceWorkflow(Boolean signal, String profile, final config)
{
	if ((signal != null && !signal) || 
    	(config.arming?.disarm?.signal == signal && config.arming?.disarm?.profile.toString() == profile)) {

		"disarm"
  	} else {
    	"arm"
    }
}

def arm()
{
	log.debug('arm: running fsm')

	def config = parent.onGetConfig()
    log.debug("arm: retrieved config: $config")

	def armConfig = config.arming?.arm
    if(armConfig) {

		def rc = fsmExec(getBIStg(true), getBIfFsmDef(), [fsmInitialState:'init', cmd:getBICommands().set(
        					signal:armConfig.signal, profile:armConfig.profile, workflow:'arm')])
    
		if(rc.error || !rc.actionResult.isAsync) {
			log.error("arm: error executing fsm: ${rc.error ?: 'expected asynchronious action result'}")
		} else {
			return rc.actionResult.hubAction
		}
    } else {
    	log.error("arm: missing configuration (arming/arm) in $config")
	    // TODO: check return
    }
    
}

def disarm()
{
	log.debug('disarm: running fsm')
    
	def config = parent.onGetConfig();
    log.debug("disarm: retrieved config: $config")

	def disarmConfig = config.arming?.disarm
    if(disarmConfig) {
		def rc = fsmExec(getBIStg(true), getBIfFsmDef(), [fsmInitialState:'init', cmd:getBICommands().set(
    		     			signal:disarmConfig.signal, profile:disarmConfig.profile, workflow:'disarm')])

		if(rc.error || !rc.actionResult.isAsync) {
			log.error("disarm: error executing fsm: ${rc.error ?: 'expected asynchronious action result'}")
		} else {
			return rc.actionResult.hubAction
		}
   	} else {
    	log.error("disarm: missing configuration (arming/arm) in $config")
	    // TODO: check return
    }
}

def location(locationId)
{
	def config = parent.onGetConfig()
    log.debug("location: retrieved config: $config, processing location $locationId")
    
    def locationConfig = config.location ? config.location[locationId] : null
    if(locationConfig) {

		def rc = fsmExec(getBIStg(true), getBIfFsmDef(), [fsmInitialState:'init', cmd:getBICommands().set(
        			signal: locationConfig.signal, profile: locationConfig.profile, 
                    workflow: deduceWorkflow(locationConfig.signal, locationConfig.profile, config), context: locationConfig.name)])

		if(rc.error || !rc.actionResult.isAsync) {
			log.error("location: error executing fsm: ${rc.error ?: 'expected asynchronious action result'}")
		} else {
			return rc.actionResult.hubAction
		}
	
    } else {
    	log.debug("location: no active configuration found for locationId:${locationId}' in configuration [$config]")
	    // TODO: check return
	}
	
}

def retry()
{
	def stg = getBIStg()

	if(stg.cmd != null) {
		log.debug("retry: running fsm with cmd:$stg.cmd")
		def rc = fsmExec(getBIStg(true), getBIfFsmDef(), [fsmInitialState:'init', cmd:stg.cmd])

		if(rc.error || !rc.actionResult.isAsync) {
			log.error("retry: error executing fsm: ${rc.error ?: 'expected asynchronious action result'}")
		} else {
			return rc.actionResult.hubAction
		}
	} else {
      log.debug("retry: no state to retry")
    }
}

def timeout()
{
	def stg = getBIStg()
	log.debug("timeout: running fsm with cmd:${stg.cmd}")

	def rc = fsmExec(getBIStg(true), getBIfFsmDef(), [fsmInitialState:'timeout', cmd:stg.cmd])

	if(rc.error || rc.actionResult.isAsync) {
		log.error("timeout: error executing fsm: ${rc.error ?: 'expected synchronious action result'}")
	}
}

def refresh()
{
	log.debug('refresh: running fsm: status command')

	def rc = fsmExec(getBIStg(true), getBIfFsmDef(), [fsmInitialState:'init', cmd:getBICommands().status()])

	if(rc.error || !rc.actionResult.isAsync) {
		log.error("refresh: error executing fsm: ${rc.error ?: 'expected asynchronious action result'}")
	} else {
		return rc.actionResult.hubAction
	}
}

def parse(msg)
{
	def lanMsg = parseLanMessage(msg)
	log.info "parse: parsed lan message: $lanMsg"


	if (lanMsg && lanMsg.headers && lanMsg.body) {
    	log.info "parse: parsed lan message requestId:$lanMsg.requestId, body:$lanMsg.body"

		def stg = getBIStg()

		if(fsmGetStateId(stg) && stg.cmd.requestId == lanMsg.requestId) {
			log.debug("parse: received expected response mesage; requestId:$lanMsg.requestId, state:${fsmGetStateId(stg)}")

			def rc = fsmExec(stg, getBIfFsmDef(), [respMsg:lanMsg])

			if(rc.error) {
				log.error("parse: error executing fsm: ${rc.error ?: 'expected asynchronious action result'}")
			} else if(rc.actionResult.isAsync) {
				return [rc.actionResult.hubAction]
			}

		} else {
			log.error("parse: skipping message: request id does not match: stg.request_id:$stg.cmd.requestId, lanMsg.requestId:$lanMsg.requestId")
		}

	} else {
		log.error("parse: skipping message: unrecognized lan message")
	}
}


private getOperationTimeoutMs()
{
   10 * 1000;
}

private Integer convertHexToInt(hex) {
   Integer.parseInt(hex,16)
}

private String convertHexToIP(hex) {
   [convertHexToInt(hex[0..1]),convertHexToInt(hex[2..3]),convertHexToInt(hex[4..5]),convertHexToInt(hex[6..7])].join(".")
}

private getHostAddress() {
   def parts = device.deviceNetworkId.split(":")
   def ip = convertHexToIP(parts[0])
   def port = convertHexToInt(parts[1])
   return ip + ":" + port
}

private hashMD5(String somethingToHash) {
   java.security.MessageDigest.getInstance("MD5").digest(somethingToHash.getBytes("UTF-8")).encodeHex().toString()
}

private calcDigestAuth(String method, String uri) {
   def HA1 =  hashMD5("${getUsername}::${getPassword}")
   def HA2 = hashMD5("${method}:${uri}")
   def response = hashMD5("${HA1}::::auth:${HA2}")

   'Digest username="'+ getUsername() + '", realm="", nonce="", uri="'+ uri +'", qop=auth, nc=, cnonce="", response="' + response + '", opaque=""'
}
