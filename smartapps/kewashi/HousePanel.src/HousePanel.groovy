/**
 *  HousePanel
 *
 *  Copyright 2016 to 2019 Kenneth Washington
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
 * This app started life displaying the history of various ssmartthings
 * but it has morphed into a full blown smart panel web application
 * it displays and enables interaction with switches, dimmers, locks, etc
 * 
 * Revision history:
 * 08/25/2019 - bugfix water leak to prevent error if wet and dry not supported
 *              update switches to include name
 * 08/18/2019 - added water action for setting dry and wet
 * 07/29/2019 - change Hubitat HubId to the actual Hub UID instead of App Id
 * 07/03/2019 - added DeviceWatch-Enroll new ignore field and fixed comment above
 *              work on fixing color reporting for bulbs - still not quite right
 * 05/27/2019 - remove blanks and images from groovy
 * 05/14/2019 - add native music artist, album, art fields when present
 * 05/11/2019 - clean up and tweak music; longer delays in subscribes
 * 05/03/2019 - user option to specify format of event time fields
 * 05/01/2019 - add try/catch for most things to prevent errors and more cleanup
 * 04/30/2019 - clean up this groovy file
 * 04/22/2019 - clean up SHM and HSM to return similar display fields
 *              - mimic Night setting in SHM to match how HSM works
 * 04/21/2019 - deal with missing prefix and other null protections
 * 04/18/2019 - add direct mode change for SHM and HSM (HE bug in HSM)
 * 04/17/2019 - merge groovy files with detector for hub type
 * 04/09/2019 - add history fields
 * 03/15/2019 - fix names of mode, blank, and image, and add humidity to temperature
 * 03/14/2019 - exclude fields that are not interesting from general tiles
 * 03/02/2019 - added motion sensors to subscriptions and fix timing issue
 * 02/26/2019 - add hubId to name query
 * 02/15/2019 - change hubnum to use hubId so we can remove hubs without damage
 * 02/10/2019 - redo subscriptions for push to make more efficient by group
 * 02/07/2019 - tweak to ignore stuff that was blocking useful push updates
 * 02/03/2019 - switch thermostat and music tiles to use native key field names
 * 01/30/2019 - implement push notifications and logger
 * 01/27/2019 - first draft of direct push notifications via hub post
 * 01/19/2019 - added power and begin prepping for push notifications
 * 01/14/2019 - fix bonehead error with switches and locks not working right due to attr
 * 01/05/2019 - fix music controls to work again after separating icons out
 * 12/01/2018 - hub prefix option implemented for unique tiles with multiple hubs
 * 11/21/2018 - add routine to return location name
 * 11/19/2018 - thermostat tweaks to support new custom tile feature 
 * 11/18/2018 - fixed mode names to include size cues
 * 11/17/2018 - bug fixes and cleanup to match Hubitat update
 * 10/30/2018 - fix thermostat bug
 * 08/20/2018 - fix another bug in lock that caused render to fail upon toggle
 * 08/11/2018 - miscellaneous code cleanup
 * 07/24/2018 - fix bug in lock opening and closing with motion detection
 * 06/11/2018 - added mobile option to enable or disable pistons and fix debugs
 * 06/10/2018 - changed icons to amazon web services location for https
 * 04/18/2018 - Bugfix curtemp in Thermostat, thanks to @kembod for finding this
 * 04/08/2018 - Important bug fixes for thermostat and music tiles
 * 03/11/2018 - Added Smart Home Monitor from Chris Hoadley
 * 03/10/2018 - Major speedup by reading all things at once
 * 02/25/2018 - Update to support sliders and color hue picker
 * 01/04/2018 - Fix bulb bug that returned wrong name in the type
 * 12/29/2017 - Changed bulb to colorControl capability for Hue light support
 *              Added support for colorTemperature in switches and lights
 * 12/10/2017 - Added name to each thing query return
 *            - Remove old code block of getHistory code
 * 
 */

public static String version() { return "V2.064" }
public static String handle() { return "HousePanel" }
definition(
    name: "${handle()}",
    namespace: "kewashi",
    author: "Kenneth Washington",
    description: "Tap here to install ${handle()} ${version()} - a highly customizable dashboard smart app. ",
    category: "Convenience",
    iconUrl: "https://s3.amazonaws.com/kewpublicicon/smartthings/hpicon1x.png",
    iconX2Url: "https://s3.amazonaws.com/kewpublicicon/smartthings/hpicon2x.png",
    iconX3Url: "https://s3.amazonaws.com/kewpublicicon/smartthings/hpicon3x.png",
    oauth: [displayName: "HousePanel", displayLink: ""])


preferences {
    section("HousePanel Configuration") {
        paragraph "Welcome to HousePanel. Below you will authorize your things for HousePanel."
        paragraph "prefix to uniquely identify certain tiles for this hub. " +
                  "If left blank, hub type will determine prefix; e.g., st_ for SmartThings or h_ for Hubitat"
        input (name: "hubprefix", type: "text", multiple: false, title: "Hub Prefix:", required: false, defaultValue: "st_")
        paragraph "Set Hubitat Cloud Calls option to True if your HousePanel app is NOT on your local LAN. " +
                  "When this is true the cloud URL will be shown for use in HousePanel. When calls are through the Cloud endpoint " +
                  "actions will be slower than local installations. This only applies to Hubitat. SmartThings always uses Cloud calls."
        input (name: "cloudcalls", type: "bool", title: "Cloud Calls", defaultValue: false, required: false, displayDuringSetup: true)
        paragraph "Enable Pistons? You must have WebCore installed for this to work. Beta feature for Hubitat hubs."
        input (name: "usepistons", type: "bool", multiple: false, title: "Use Pistons?", required: false, defaultValue: false)
        paragraph "Timezone and Format for event time fields; e.g., America/Detroit, Europe/London, or America/Los_Angeles"
        input (name: "timezone", type: "text", multiple: false, title: "Timezone Name:", required: false, defaultValue: "America/Detroit")
        input (name: "dateformat", type: "text", multiple: false, title: "Date Format:", required: false, defaultValue: "M/dd h:mm")
        paragraph "Specify these parameters to enable direct and instant hub pushes when things change in your home."
        input "webSocketHost", "text", title: "Host IP", defaultValue: "192.168.11.20", required: false
        input "webSocketPort", "text", title: "Port", defaultValue: "19234", required: false
    }
    section("Lights and Switches") {
        input "myswitches", "capability.switch", multiple: true, required: false, title: "Switches"
        input "mydimmers", "capability.switchLevel", hideWhenEmpty: true, multiple: true, required: false, title: "Dimmers"
        input "mymomentaries", "capability.momentary", hideWhenEmpty: true, multiple: true, required: false, title: "Momentary Buttons"
        input "mylights", "capability.light", hideWhenEmpty: true, multiple: true, required: false, title: "Lights"
        input "mybulbs", "capability.colorControl", hideWhenEmpty: true, multiple: true, required: false, title: "Bulbs"
    }
    section ("Motion and Presence") {
    	input "mypresences", "capability.presenceSensor", hideWhenEmpty: true, multiple: true, required: false, title: "Presence"
    	input "mymotions", "capability.motionSensor", multiple: true, required: false, title: "Motion"
    }
    section ("Door and Contact Sensors") {
    	input "mycontacts", "capability.contactSensor", hideWhenEmpty: true, multiple: true, required: false, title: "Contact Sensors"
    	input "mydoors", "capability.doorControl", hideWhenEmpty: true, multiple: true, required: false, title: "Doors"
    	input "mylocks", "capability.lock", hideWhenEmpty: true, multiple: true, required: false, title: "Locks"
    }
    section ("Thermostat & Environment") {
    	input "mythermostats", "capability.thermostat", hideWhenEmpty: true, multiple: true, required: false, title: "Thermostats"
    	input "mytemperatures", "capability.temperatureMeasurement", hideWhenEmpty: true, multiple: true, required: false, title: "Temperature Measures"
    	input "myilluminances", "capability.illuminanceMeasurement", hideWhenEmpty: true, multiple: true, required: false, title: "Illuminances"
    	input "myweathers", "device.smartweatherStationTile", hideWhenEmpty: true, title: "Weather tile", multiple: true, required: false
    }
    section ("Water, Sprinklers & Smoke") {
    	input "mywaters", "capability.waterSensor", hideWhenEmpty: true, multiple: true, required: false, title: "Water Sensors"
    	input "myvalves", "capability.valve", hideWhenEmpty: true, multiple: true, required: false, title: "Sprinklers"
    	input "mysmokes", "capability.smokeDetector", hideWhenEmpty: true, multiple: true, required: false, title: "Smoke Detectors"
    }
    section ("Music & Other Sensors") {
        paragraph "Any thing can be added as an Other sensor. Other sensors bring in ALL fields supported by the device handler."
    	input "mymusics", "capability.musicPlayer", hideWhenEmpty: true, multiple: true, required: false, title: "Music Players"
        input "mypowers", "capability.powerMeter", multiple: true, required: false, title: "Power Meters"
    	input "myothers", "capability.sensor", multiple: true, required: false, title: "Other and Virtual Sensors"
    }
    section("Logging") {
        input (
            name: "configLogLevel",
            title: "IDE Live Logging Level:\nMessages with this level and higher will be logged to the IDE.",
            type: "enum",
            options: [
                "0" : "None",
                "1" : "Error",
                "2" : "Warning",
                "3" : "Info",
                "4" : "Debug",
                "5" : "Trace"
            ],
            defaultValue: "3",
            displayDuringSetup: true,
            required: false
        )
    }   

}

mappings {
  
  path("/getallthings") {
     action: [       POST: "getAllThings"     ]
  }
  
  path("/doaction") {
     action: [       POST: "doAction"     ]
  }
  
  path("/doquery") {
     action: [       POST: "doQuery"     ]
  }
  
  path("/gethubinfo") {
     action: [       POST: "getHubInfo"     ]
  }

}

def installed() {
    initialize()
}

def updated() {
    unsubscribe()
    initialize()
}

def initialize() {
    def hubtype = getPlatform()
    state.usepistons = settings?.usepistons ?: false
    state.directIP = settings?.webSocketHost ?: ""
    state.directPort = settings?.webSocketPort ?: "19234"
    state.tz = settings?.timezone ?: "America/Detroit"
    state.prefix = settings?.hubprefix ?: getPrefix()
    state.dateFormat = settings?.dateformat ?: "M/dd h:mm"

    configureHub();
    if ( state.usepistons ) {
        webCoRE_init()
    }
    state.loggingLevelIDE = settings.configLogLevel?.toInteger() ?: 3
    logger("Installed ${hubtype} hub with settings: ${settings} ", "debug")
    
    if ( isHubitat() ) {
        subscribe(location, "hsmStatus", hsmStatusHandler)
        subscribe(location, "hsmAlerts", hsmAlertHandler)
    }
    
    if (state.directIP)
    {
        postHub("initialize");
        runIn(10, "registerAll");
    }
}

// detection routines - the commented line is more efficient but less foolproof
private Boolean isHubitat() {
    def istrue = physicalgraph?.device?.HubAction ? false : true
    // def istrue = hubUID ? true : false
    return istrue
}
private Boolean isST() {
    return ( ! isHubitat() )
}
private String getPlatform() {
    def hubtype = isHubitat() ? 'Hubitat' : 'SmartThings'
    return hubtype
}
private String getPrefix() {
    def hubpre = isHubitat() ? 'h_' : 'st_'
    return hubpre
}

def configureHub() {
    def hub = location.hubs[0]
    def hubid
    def hubip
    def endpt
    
    def firmware = hub?.firmwareVersionString ?: "unknown"

    if ( !isHubitat() ) {
        state.hubid = hub.id
        hubip = hub.localIP
        logger("You must go through the OAUTH flow to obtain a proper SmartThings AccessToken", "info")
        logger("You must go through the OAUTH flow to obtain a proper SmartThings EndPoint", "info")
    } else {
        // state.hubid = app.id
        state.hubid = hubUID
        if ( cloudcalls ) {
            hubip = "https://oauth.cloud.hubitat.com";
            endpt = "${hubip}/${hubUID}/apps/${app.id}/"
            logger("Cloud installation was requested and is reflected in the hubip and endpt info", "info")
        } else {
            hubip = hub.localIP
            endpt = "${hubip}/apps/api/${app.id}/"
        }
        logger("Hubitat AccessToken = ${state.accessToken}", "info")
        logger("Hubitat EndPoint = ${endpt}", "info")
    }
    
    logger("Use this information on the Auth page of HousePanel.", "info")
    logger("Hub Platform = ${getPlatform()}", "info")
    logger("Hub IP = ${hubip}", "info")
    logger("Hub ID = ${state.hubid}", "info")
    logger("Hub Firmware = ${firmware}", "info")
    logger("rPI IP Address = ${state.directIP}", "info")
    logger("webSocket Port = ${state.directPort}", "info")
    logger("date Timezone for events = ${state.tz}", "info")
    logger("date Format for events = ${state.dateFormat}", "info")
}

def addHistory(resp, item) {
    if (resp && item ) {
        
        try {
            def start = new Date() - 7
            def thestates = item.eventsSince(start,["max":4])
            def i = 0;
            def priorval = ""
            def tz = TimeZone.getTimeZone( state.tz ?: "America/Detroit" )
            thestates.each {
                if ( it.value!=priorval ) {
                    i++
                    def evtvalue = it.value + " " + it.date.format(state.dateFormat ?: "M/dd h:mm", tz)
                    resp.put("event_${i}", evtvalue )
                    priorval = it.value
                }
            }
        } catch (e) {
            logger("Cannot retrieve history for device ${item.displayName}", "info")
        }
    }
    return resp
}

def addBattery(resp, item) {
    addAttr(resp, item, "battery")
}

def addAttr(resp, item, attr) {
    if ( resp && item && item.hasAttribute(attr) ) {
        resp.put(attr, item.currentValue(attr))
    }
    return resp
}

def getSwitch(swid, item=null) {
    def resp = false
    item = item ? item : myswitches.find {it.id == swid }
    if ( item ) {
        resp = [name: item.displayName]
        resp = addBattery(resp, item)
        resp.put("switch", item.currentValue("switch"))
        resp = addHistory(resp, item)
    }
    return resp
}

def getBulb(swid, item=null) {
    getThing(mybulbs, swid, item)
}

def getLight(swid, item=null) {
    getThing(mylights, swid, item)
}

def getMomentary(swid, item=null) {
    def resp = false
    item = item ? item : mymomentaries.find {it.id == swid }
    if ( item ) {
        resp = [name: item.displayName]
        resp = addBattery(resp, item)
        if ( item.hasCapability("Switch") ) {
            def curval = item.currentValue("switch")
            if (curval!="on" && curval!="off") { curval = "off" }
            resp.put("momentary", curval)
        }
        resp = addHistory(resp, item)
    }
    return resp
}

def getDimmer(swid, item=null) {
    getThing(mydimmers, swid, item)
}

def getMotion(swid, item=null) {
    getThing(mymotions, swid, item)
}

def getContact(swid, item=null) {
    getThing(mycontacts, swid, item)
}

// change to only return lock status and battery
def getLock(swid, item=null) {
    item = item? item : mylocks.find {it.id == swid }
    def resp = false
    if ( item ) {
        resp = [name: item.displayName]
        resp = addBattery(resp, item)
        resp.put("lock", item.currentValue("lock"))
        resp = addHistory(resp, item)
    }
    return resp
}

// this was updated to use the real key names so that push updates work
// note -changes were also made in housepanel.php and elsewhere to support this
def getMusic(swid, item=null) {
    // def resp = getThing(mymusics, swid, item)
    item = item? item : mymusics.find {it.id == swid }
    def resp = false
    if ( item ) {
        resp = [
            name: item.displayName, 
            trackDescription: item.currentValue("trackDescription"),
            status: item.currentValue("status"),
            level: item.currentValue("level"),
            mute: item.currentValue("mute")
        ]
        
        // add native track information if attributes exist
        // this code is from @rsb in the ST forum
        resp = addAttr(resp, item, "currentArtist")
        resp = addAttr(resp, item, "currentAlbum")
        resp = addAttr(resp, item, "trackImage")
        resp = addBattery(resp, item)
        resp = addHistory(resp, item)
    }
    // log.debug resp
    return resp
}

// this was updated to use the real key names so that push updates work
// note -changes were also made in housepanel.php and elsewhere to support this
def getThermostat(swid, item=null) {
    item = item? item : mythermostats.find {it.id == swid }
    def resp = false
    if ( item ) {
        resp = [name: item.displayName, 
            temperature: item.currentValue("temperature"),
            heatingSetpoint: item.currentValue("heatingSetpoint"),
            coolingSetpoint: item.currentValue("coolingSetpoint"),
            thermostatFanMode: item.currentValue("thermostatFanMode"),
            thermostatMode: item.currentValue("thermostatMode"),
            thermostatOperatingState: item.currentValue("thermostatOperatingState")
        ]
        resp = addBattery(resp, item)
        resp = addAttr(resp, item, "humidity")
        resp = addHistory(resp, item)
    }
    return resp
}

// use absent instead of "not present" for absence state
def getPresence(swid, item=null) {
    item = item ? item : mypresences.find {it.id == swid }
    def resp = false
    if ( item ) {
        resp = [name: item.displayName]
        resp = addBattery(resp, item)
        def pval = (item.currentValue("presence")=="present") ? "present" : "absent"
        resp.put("presence", pval)
        resp = addHistory(resp, item)
    }
    return resp
}

def getWater(swid, item=null) {
    getThing(mywaters, swid, item)
}

def getValve(swid, item=null) {
    getThing(myvalves, swid, item)
}
def getDoor(swid, item=null) {
    getThing(mydoors, swid, item)
}

def getIlluminance(swid, item=null) {
    item = item ? item : myilluminances.find {it.id == swid }
    def resp = false
    if ( item ) {
        resp = [name: item.displayName]
        resp = addBattery(resp, item)
        resp.put("illuminance", item.currentValue("illuminance") )
        resp = addHistory(resp, item)
    }
    return resp
}
def getSmoke(swid, item=null) {
    getThing(mysmokes, swid, item)
}

// return temperature for this capability and humidity if it has one
def getTemperature(swid, item=null) {
    item = item ? item : mytemperatures.find {it.id == swid }
    def resp = false
    if ( item ) {
        resp = [name: item.displayName]
        resp = addBattery(resp, item)
        resp.put("temperature", item.currentValue("temperature") )
        resp = addAttr(resp, item, "humidity")
        resp = addHistory(resp, item)
    }
    return resp
}

def getWeather(swid, item=null) {
    getDevice(myweathers, swid, item)
}

def getOther(swid, item=null) {
    getThing(myothers, swid, item)
}

def getPower(swid, item=null) {
    getThing(mypowers, swid, item)
}

def extractName(swid, prefix) {
    def postfix = swid ?: ""
    if ( state.prefix && swid && swid.startsWith(state.prefix) ) {
        def k = state.prefix.length()
        postfix = swid.substring(k)
    }
    def thename = "$prefix $postfix"
}

def getMyMode(swid, item=null) {
    def allmodes = location.getModes()
    def curmode = location.getCurrentMode()
    def resp = [ name: extractName(swid, "Mode"), 
        sitename: location.getName(), themode: curmode?.getName() ];
    for (defcmd in allmodes) {
        def modename = defcmd.getName()
        resp.put("_${modename}",modename)
    }
    return resp
}

def getSHMState(swid, item=null){
    def cmds = ["away", "stay", "night", "off"]
    def keynames = ["Away", "Home", "Night", "Disarmed"]
    def key = location.currentState("alarmSystemStatus")?.value

    // mimic the states provided by HSM
    def i = cmds.findIndexOf{it == key}
    if ( i<0 ) { i = 3 }
    
    // if mode is night and set to stay change mode to fake night setting
    if ( i==1 ) {
        def curmode = location.getCurrentMode()?.getName()
        if ( curmode == "Night" ) { i= 2 }
    }
    
    def statusname = keynames[i]
    def resp = [name : "Smart Home Monitor", state: statusname]
    for (defcmd in cmds) {
        resp.put("_${defcmd}",defcmd)
    }
    return resp
}

def getHSMState(swid, item=null) {
    // uses Hubitat specific call for HSM per 
    // https://community.hubitat.com/t/hubitat-safety-monitor-api/934/11
    def cmds = ["armAway", "armHome", "armNight", "disarm", "armRules", "disarmRules", "disarmAll", "armAll", "cancelAlerts"]
    def keys = ["armedAway", "armedHome", "armedNight", "disarmed"]
    def keynames = ["Away", "Home", "Night", "Disarmed"]
    
    def key = location.hsmStatus ?: false
    def resp
    
    if ( !key ) {
        resp = [name : "Hubitat Safety Monitor", state: "Not Installed"]
        logger("location hsmStatus is invalid; it is not installed","info")
    } else {
        def i = keys.findIndexOf{ it == key }
        def statusname = (i >= 0) ? keynames[i] : "Disarmed"
        resp = [name : "Hubitat Safety Monitor", state: statusname]
        logger("location hsmStatus returned: ${key} as name: ${statusname}","debug")
        for (defcmd in cmds) {
            resp.put("_${defcmd}",defcmd)
        }
    }
    return resp
}

def getBlank(swid, item=null) {
    def resp = [name: extractName(swid, "Blank")]
    return resp
}

def getImage(swid, item=null) {
    def resp = [name: extractName(swid, "Image"), url: "${swid}"]
    return resp
}

def getRoutine(swid, item=null) {
    def routines = location.helloHome?.getPhrases()
    def routine = item ? item : routines.find{it.id == swid}
    def resp = routine ? [name: routine.label, label: routine.label] : false
    return resp
}

// change pistonName to name to be consistent
// but retain original for backward compatibility reasons
def getPiston(swid, item=null) {
    item = item ? item : webCoRE_list().find {it.id == swid}
    def resp = [name: item.name, pistonName: "idle"]
    return resp
}

// a generic device getter to streamline code
def getDevice(mydevices, swid, item=null) {
    def resp = false
    if ( mydevices ) {
    	item = item ? item : mydevices.find {it.id == swid }
    	if (item) {
            resp = [:]
            def attrs = item.getSupportedAttributes()
            attrs.each {att ->
                try {
                    def attname = att.name
                    def attval = item.currentValue(attname)
                    resp.put(attname,attval)
                } catch (e) {
                    logger("Attempt to read device for ${swid} failed ${e}", "error")
                }
            }
    	}
    }
    return resp
}

// make a generic thing getter to streamline the code
def getThing(things, swid, item=null) {
    item = item ? item : things.find {it.id == swid }
    def resp = item ? [:] : false
    if ( item ) {
        resp.put("name",item.displayName)
        
        item.capabilities.each {cap ->
            // def capname = cap.getName()
            cap.attributes?.each {attr ->
                try {
                    def reservedcap = ["DeviceWatch-DeviceStatus", "DeviceWatch-Enroll", "checkInterval", "healthStatus"]
                    def othername = attr.getName()
                    def othervalue = item.currentValue(othername)
                    if ( !reservedcap.contains(othername) ) {
                        resp.put(othername,othervalue)
                    }
                } catch (ex) {
                    logger("Attempt to read attribute for ${swid} failed ${ex}", "error")
                } 
            }
        }
        // add commands other than standard ones
        item.supportedCommands.each { comm ->
            try {
                def reserved = ["setLevel","setHue","on","off","open","close",\
                                "setSaturation","setColorTemperature","setColor","setAdjustedColor",\
                                "indicatorWhenOn","indicatorWhenOff","indicatorNever",\
                                "enrollResponse","stopLevelChange","poll","ping","configure","refresh"]
                def comname = comm.getName()
                def args = comm.getArguments()
                def arglen = 0
                if (args != null)
                    arglen = args.size()
                logger("Command for ${swid} = $comname with $arglen args = $args ", "trace")
                if ( arglen==0 && ! reserved.contains(comname) ) {
                    resp.put( "_"+comname, comname )
                }
            } catch (ex) {
                logger("Attempt to read command for ${swid} failed ${ex}", "error")
            }
        }
        resp = addHistory(resp, item)
    }
    
    // fix color
    if ( resp["hue"] && resp["saturation"] && resp["level"]) {
        def h = resp["hue"].toInteger()
        def s = resp["saturation"].toInteger()
        def v = resp["level"].toInteger()
        def newcolor = hsv2rgb(h, s, v)
        resp["hue"] = h
        resp["saturation"] = s
        resp["level"] = v
        resp["color"] ? resp["color"] = newcolor : resp.put("color", newcolor)
    }
    
    return resp
}

// make a generic thing list getter to streamline the code
def getThings(resp, things, thingtype) {
    def n  = things ? things.size() : 0
    logger("Number of things of type ${thingtype} = ${n}", "debug")
    things?.each {
        try {
            def val = getThing(things, it.id, it)
            resp << [name: it.displayName, id: it.id, value: val, type: thingtype]
        } catch (e) {}
    }
    return resp
}

def logStepAndIncrement(step)
{
    logger("Debug ${step}", "trace")
    return step+1
}
// This retrieves and returns all things
// used up front or whenever we need to re-read all things
def getAllThings() {

    def resp = []
    def run = 1
    run = logStepAndIncrement(run)
    resp = getSwitches(resp)
    run = logStepAndIncrement(run)
    resp = getDimmers(resp)
    run = logStepAndIncrement(run)
    resp = getMomentaries(resp)
    run = logStepAndIncrement(run)
    resp = getLights(resp)
    run = logStepAndIncrement(run)
    resp = getBulbs(resp)
    run = logStepAndIncrement(run)
    resp = getContacts(resp)
    run = logStepAndIncrement(run)
    resp = getDoors(resp)
    run = logStepAndIncrement(run)
    resp = getLocks(resp)
    run = logStepAndIncrement(run)
    resp = getMotions(resp)
    run = logStepAndIncrement(run)
    resp = getPresences(resp)
    run = logStepAndIncrement(run)
    resp = getThermostats(resp)
    run = logStepAndIncrement(run)
    resp = getTemperatures(resp)
    run = logStepAndIncrement(run)
    resp = getIlluminances(resp)
    if ( isST() ) {
        run = logStepAndIncrement(run)
        resp = getWeathers(resp)
    }
    run = logStepAndIncrement(run)
    resp = getValves(resp)
    run = logStepAndIncrement(run)
    resp = getWaters(resp)
    run = logStepAndIncrement(run)
    resp = getMusics(resp)
    run = logStepAndIncrement(run)
    resp = getSmokes(resp)
    run = logStepAndIncrement(run)
    resp = getModes(resp)
    if ( isST() ) {
        run = logStepAndIncrement(run)
        resp = getSHMStates(resp)
        run = logStepAndIncrement(run)
        resp = getRoutines(resp)
    }
    if ( isHubitat() ) {
        run = logStepAndIncrement(run)
        resp = getHSMStates(resp)
    }
    run = logStepAndIncrement(run)
    resp = getOthers(resp)
    // run = logStepAndIncrement(run)
    // resp = getBlanks(resp)
    // run = logStepAndIncrement(run)
    // resp = getImages(resp)
    run = logStepAndIncrement(run)
    resp = getPowers(resp)

    // optionally include pistons based on user option
    if (state.usepistons) {
        run = logStepAndIncrement(run)
        resp = getPistons(resp)
    }

    return resp
}

def getModes(resp) {
    logger("Getting 4 mode tiles","debug");
    def vals = ["m1x1","m1x2","m2x1","m2x2"]
    try {
        vals.each {
            def val = getMyMode(it)
            resp << [name: val.name, id: "${state.prefix}${it}", value: val, type: "mode"]
        }
    } catch (e) {}
    return resp
}

def getSHMStates(resp) {
    logger("Getting Smart Home Monitor state for SmartThings Hub","debug");
    try {
        def val = getSHMState("${state.prefix}shm")
        resp << [name: "Smart Home Monitor", id: "${state.prefix}shm", value: val, type: "shm"]
    } catch (e) {}
    return resp
}

def getHSMStates(resp) {
    logger("Getting Hubitat Safety Monitor state for Hubitat Hub","debug");
    try{
        def val = getHSMState("${state.prefix}hsm")
        if ( val ) {
            resp << [name: "Hubitat Safety Monitor", id: "${state.prefix}hsm", value: val, type: "hsm"]
        }
    } catch (e) {}
    return resp
}

def getBlanks(resp) {
    def vals = ["b1x1","b1x2","b2x1","b2x2"]
    try {
        vals.each {
            def val = getBlank("${state.prefix}${it}")
            resp << [name: val.name, id: "${state.prefix}${it}", value: val, type: "blank"]
        }
    } catch (e) {}
    return resp
}

def getImages(resp) {
    def vals = ["img1","img2","img3","img4"]
    try {
        vals.each {
            def val = getImage("${state.prefix}${it}")
            resp << [name: val.name, id: "${state.prefix}${it}", value: val, type: "image"]
        }
    } catch (e) {}
    return resp
}

def getPistons(resp) {
    def plist = webCoRE_list()
    logger("Number of pistons = " + plist?.size() ?: 0, "debug")
    try {
        plist?.each {
            def val = getPiston(it.id, it)
            resp << [name: it.name, id: it.id, value: val, type: "piston"]
        }
    } catch (e) {}
    return resp
}

def getSwitches(resp) {
    try {
        myswitches?.each {
            def multivalue = getSwitch(it.id, it)
            resp << [name: it.displayName, id: it.id, value: multivalue, type: "switch" ]
        }
    } catch (e) {}
    return resp
}

def getBulbs(resp) {
    getThings(resp, mybulbs, "bulb")
}

def getLights(resp) {
    getThings(resp, mylights, "light")
}

def getDimmers(resp) {
    getThings(resp, mydimmers, "switchlevel")
}

def getMotions(resp) {
    getThings(resp, mymotions, "motion")
}

def getContacts(resp) {
    getThings(resp, mycontacts, "contact")
}

def getMomentaries(resp) {
    try {
        mymomentaries?.each {
            if ( it.hasCapability("Switch") ) {
                def val = getMomentary(it.id, it)
                resp << [name: it.displayName, id: it.id, value: val, type: "momentary" ]
            }
        }
    } catch (e) {}
    return resp
}

def getLocks(resp) {
    try {
        mylocks?.each {
            def multivalue = getLock(it.id, it)
            resp << [name: it.displayName, id: it.id, value: multivalue, type: "lock"]
        }
    } catch (e) {}
    return resp
}

def getMusics(resp) {
    try {
        mymusics?.each {
            def multivalue = getMusic(it.id, it)
            resp << [name: it.displayName, id: it.id, value: multivalue, type: "music"]
        }
    } catch (e) {}
    
    return resp
}

def getThermostats(resp) {
    try {
        mythermostats?.each {
            def multivalue = getThermostat(it.id, it)
            resp << [name: it.displayName, id: it.id, value: multivalue, type: "thermostat" ]
        }
    } catch (e) {}
    return resp
}

def getPresences(resp) {
    try {
        mypresences?.each {
            def multivalue = getPresence(it.id, it)
            resp << [name: it.displayName, id: it.id, value: multivalue, type: "presence"]
        }
    } catch (e) {}
    return resp
}
def getWaters(resp) {
    getThings(resp, mywaters, "water")
}
def getValves(resp) {
    getThings(resp, myvalves, "valve")
}
def getDoors(resp) {
    getThings(resp, mydoors, "door")
}
def getIlluminances(resp) {
    getThings(resp, myilluminances, "illuminance")
}
def getSmokes(resp) {
    getThings(resp, mysmokes, "smoke")
}
def getTemperatures(resp) {
    try {
        mytemperatures?.each {
            def val = getTemperature(it.id, it)
            resp << [name: it.displayName, id: it.id, value: val, type: "temperature"]
        }
    } catch (e) {}
    return resp
}

def getWeathers(resp) {
    try {
        myweathers?.each {
            def multivalue = getWeather(it.id, it)
            resp << [name: it.displayName, id: it.id, value: multivalue, type: "weather"]
        }
    } catch (e) {}
    return resp
}

// get hellohome routines - thanks to ady264 for the tip
def getRoutines(resp) {
    try {
        def routines = location.helloHome?.getPhrases()
        def n  = routines ? routines.size() : 0
        if ( n > 0 ) { logger("Number of routines = ${n}","debug"); }
        routines?.each {
            def multivalue = getRoutine(it.id, it)
            resp << [name: it.label, id: it.id, value: multivalue, type: "routine"]
        }
    } catch (e) {}
    return resp
}

def getOthers(resp) {
    try {
        def n  = myothers ? myothers.size() : 0
        if ( n > 0 ) { logger("Number of other sensors = ${n}","debug"); }
        myothers?.each {
            def thatid = it.id;
            def multivalue = getThing(myothers, thatid, it)
            resp << [name: it.displayName, id: thatid, value: multivalue, type: "other"]
        }
    } catch (e) {}
    return resp
}

def getPowers(resp) {
    try {
        def n  = mypowers ? mypowers.size() : 0
        if ( n > 0 ) { logger("Number of selected power things = ${n}","debug"); }
        mypowers?.each {
            def thatid = it.id;
            def multivalue = getThing(mypowers, thatid, it)
            resp << [name: it.displayName, id: thatid, value: multivalue, type: "power"]
        }
    } catch (e) {}
    return resp
}

def getHubInfo() {
    def resp =  [ sitename: location.getName(),
                  hubId: state.hubid,
                  hubtype: getPlatform() ]
    return resp
}

def autoType(swid) {
    def swtype
    if ( mydimmers?.find {it.id == swid } ) { swtype= "switchlevel" }
    else if ( mymomentaries?.find {it.id == swid } ) { swtype= "momentary" }
    else if ( mylights?.find {it.id == swid } ) { swtype= "light" }
    else if ( mybulbs?.find {it.id == swid } ) { swtype= "bulb" }
    else if ( myswitches?.find {it.id == swid } ) { swtype= "switch" }
    else if ( mylocks?.find {it.id == swid } ) { swtype= "lock" }
    else if ( mymusics?.find {it.id == swid } ) { swtype= "music" }
    else if ( mythermostats?.find {it.id == swid} ) { swtype = "thermostat" }
    else if ( mypresences?.find {it.id == swid } ) { swtype= "presence" }
    else if ( myweathers?.find {it.id == swid } ) { swtype= "weather" }
    else if ( mymotions?.find {it.id == swid } ) { swtype= "motion" }
    else if ( mydoors?.find {it.id == swid } ) { swtype= "door" }
    else if ( mycontacts?.find {it.id == swid } ) { swtype= "contact" }
    else if ( mywaters?.find {it.id == swid } ) { swtype= "water" }
    else if ( myvalves?.find {it.id == swid } ) { swtype= "valve" }
    else if ( myilluminances?.find {it.id == swid } ) { swtype= "illuminance" }
    else if ( mysmokes?.find {it.id == swid } ) { swtype= "smoke" }
    else if ( mytemperatures?.find {it.id == swid } ) { swtype= "temperature" }
    else if ( myothers?.find {it.id == swid } ) { swtype= "other" }
    else if ( mypowers?.find {it.id == swid } ) { swtype= "power" }
    else if ( swid=="${state.prefix}shm" ) { swtype= "shm" }
    else if ( swid=="${state.prefix}hsm" ) { swtype= "hsm" }
    else if ( swid=="${state.prefix}m1x1" || swid=="${state.prefix}m1x2" || swid=="${state.prefix}m2x1" || swid=="${state.prefix}m2x2" ) { swtype= "mode" }
    // else if ( swid=="${state.prefix}b1x1" || swid=="${state.prefix}b1x2" || swid=="${state.prefix}b2x1" || swid=="${state.prefix}b2x2" ) { swtype= "blank" }
    // else if ( swid=="${state.prefix}img1" || swid=="${state.prefix}img2" || swid=="${state.prefix}img3" || swid=="${state.prefix}img4" ) { swtype= "image" }
    else if ( state.usepistons && webCoRE_list().find {it.id == swid} ) { swtype= "piston" }
    else { swtype = "" }
    return swtype
}

// this performs ajax action for clickable tiles
def doAction() {
    // returns false if the item is not found
    // otherwise returns a JSON object with the name, value, id, type
    def cmd = params.swvalue
    def swid = params.swid
    def swtype = params.swtype
    def swattr = params.swattr
    def subid = params.subid
    def cmdresult = false
    logger("doaction params: cmd= $cmd type= $swtype id= $swid subid= $subid attr= $swattr", "debug")
   
    // get the type if auto is set
    if ( (swtype=="auto" || swtype=="none" || !swtype) && swid ) {
        swtype = autoType(swid)
    }

    switch (swtype) {
      case "switch" :
        cmdresult = setSwitch(swid, cmd, swattr, subid)
        break

      case "bulb" :
        cmdresult = setBulb(swid, cmd, swattr, subid)
        break

      case "light" :
        cmdresult = setLight(swid, cmd, swattr, subid)
        break

      case "switchlevel" :
        cmdresult = setDimmer(swid, cmd, swattr, subid)
        break

      case "momentary" :
        cmdresult = setMomentary(swid, cmd, swattr, subid)
        break

      case "lock" :
        cmdresult = setLock(swid, cmd, swattr, subid)
        break

      case "thermostat" :
        cmdresult = setThermostat(swid, cmd, swattr, subid)
        break

      case "music" :
        cmdresult = setMusic(swid, cmd, swattr, subid)
        break

      // note: this requires a special handler for motion to manually set it
      case "motion" :
        cmdresult = setMotion(swid, cmd, swattr, subid)
        break

      case "mode" :
        cmdresult = setMode(swid, cmd, swattr, subid)
        break

      case "shm" :
        cmdresult = setSHMState(swid, cmd, swattr, subid)
        break

      case "hsm":
        cmdresult = setHSMState(swid, cmd, swattr, subid)
        break;
 
      case "valve" :
        cmdresult = setValve(swid, cmd, swattr, subid)
        break

      case "door" :
      	 cmdresult = setDoor(swid, cmd, swattr, subid)
         break

      case "piston" :
        if ( state.usepistons ) {
            webCoRE_execute(swid)
            cmdresult = getPiston(swid)
        }
        break
      
      case "routine" :
        cmdresult = setRoutine(swid, cmd, swattr, subid)
        break
        
      case "water" :
        cmdresult = setWater(swid, cmd, swattr, subid)
        break
        
      case "other" :
        cmdresult = setOther(swid, cmd, swattr, subid)
        break
    }
    logger("doAction results: " + cmdresult.toString() , "debug");
    return cmdresult
}

def doQuery() {
    def swid = params.swid
    def swtype = params.swtype
    def cmdresult = false

    // get the type if auto is set
    if ( swid=="all" ) {
        swtype = "all"
    } else if ( (swtype=="auto" || swtype=="none" || !swtype) && swid ) {
        swtype = autoType(swid)
    }

    switch(swtype) {

    // special case to return an array of all things
    // each case below also now includes multi-item options for the API
    case "all" :
        cmdresult = getAllThings()
        break

    case "switch" :
        cmdresult = swid ? getSwitch(swid) : getSwitches( [] )
        break
         
    case "bulb" :
        cmdresult = swid ? getBulb(swid) : getBulbs( [] )
        break
         
    case "light" :
        cmdresult = swid ? getLight(swid) : getLights( [] )
        break
         
    case "switchlevel" :
        cmdresult = swid ? getDimmer(swid) : getDimmers( [] )
        break
         
    case "momentary" :
        cmdresult = swid ? getMomentary(swid) : getMomentaries( [] )
        break
        
    case "motion" :
        cmdresult = swid ? getMotion(swid) : getMotions( [] )
        break
        
    case "contact" :
        cmdresult = swid ? getContact(swid) : getContacts( [] )
        break
      
    case "lock" :
        cmdresult = swid ? getLock(swid) : getLocks( [] )
        break
         
    case "thermostat" :
        cmdresult = swid ? getThermostat(swid) : getThermostats( [] )
        break
         
    case "music" :
        cmdresult = swid ? getMusic(swid) : getMusics( [] )
        break
        
    case "presence" :
        cmdresult = swid ? getPresence(swid) : getPresences( [] )
        break
         
    case "water" :
        cmdresult = swid ? getWater(swid) : getWaters( [] )
        break
         
    case "valve" :
        cmdresult = swid ? getValve(swid) : getValves( [] )
        break
        
    case "door" :
        cmdresult = swid ? getDoor(swid) : getDoors( [] )
        break
        
    case "illuminance" :
        cmdresult = swid ? getIlluminance(swid) : getIlluminances( [] )
        break
        
    case "smoke" :
        cmdresult = swid ? getSmoke(swid) : getSmokes( [] )
        break
        
    case "temperature" :
        cmdresult = swid ? getTemperature(swid) : getTemperatures( [] )
        break
        
    case "weather" :
        cmdresult = swid ? getWeather(swid) : getWeathers( [] )
        break
        
    case "other" :
    	cmdresult = getOther(swid)
        break
        
    case "power":
        cmdresult = getPower(swid)
        
    case "mode" :
        cmdresult = getMyMode(swid)
        break
        
    case "shm" :
        cmdresult = getSHMState(swid)
        break
        
    case "hsm" :
        cmdresult = getHSMState(swid)
        break
        
    case "routine" :
        cmdresult = swid ? getRoutine(swid) : getRoutines( [] )
        break

    }
   
    logger("doQuery: type= $swtype id= $swid result= $cmdresult", "debug");
    return cmdresult
}

def setSwitch(swid, cmd, swattr, subid) {
    def resp = setGenericLight(myswitches, swid, cmd, swattr, subid)
    return resp
}

def setDoor(swid, cmd, swattr, subid) {
    logcaller("setDoor", swid, cmd, swattr, subid)
    def newonoff
    def resp = false
    def item  = mydoors.find {it.id == swid }
    if (item) {
        if ( subid=="door" && ( swattr.endsWith("closed") || swattr.endsWith("closing") ) ) {
            newonoff = "open";
        } else if ( subid=="door" && ( swattr.endsWith("open") || swattr.endsWith("opening") ) ) {
            newonoff = "closed";
        } else if (cmd=="open") {
            newonoff = cmd
        } else if (cmd=="close") {
            newonoff = "closed"
        } else {
            newonoff = (item.currentValue("door")=="closed" ||
                        item.currentValue("door")=="closing" )  ? "open" : "closed"
        }
        newonoff=="open" ? item.open() : item.close()
        resp = [door: newonoff]
        resp = addAttr(resp, item, "contact")
        resp = addBattery(resp, item)
    }
    return resp
}

// special function to set motion status
def setMotion(swid, cmd, swattr, subid) {
    def resp = false
    def newsw
    def item  = mymotions.find {it.id == swid }
    logcaller("setMotion", swid, cmd, swattr, subid)
    // anything but active will set the motion to inactive
    if (item && item.hasCommand("startmotion") && item.hasCommand("stopmotion") ) {
        if (cmd=="active" || cmd=="move") {
            item.startmotion()
            newsw = "active"
        } else {
            item.stopmotion()
            newsw = "inactive"
        }
        resp = [motion: newsw]
        resp = addBattery(resp, item)
    }
    return resp
}

// replaced this code to treat bulbs as Hue lights with color controls
def setBulb(swid, cmd, swattr, subid) {
    def resp = setGenericLight(mybulbs, swid, cmd, swattr, subid)
    return resp
}

// treat just like bulbs - note: light types are deprecated
def setLight(swid, cmd, swattr, subid) {
    def resp = setGenericLight(mylights, swid, cmd, swattr, subid)
    return resp
}

// other types have actions starting with _ 
// and we accommodate switches and api calls with valid cmd values
def setOther(swid, cmd, attr, subid ) {
    def resp = false
    def item  = myothers.find {it.id == swid }
    def lightflags = ["switch","level","hue","saturation","colorTemperature"]
    
    if ( item ) {
        logcaller(item.getDisplayName(), swid, cmd, swattr, subid)
        if (subid.startsWith("_")) {
            subid = subid.substring(1)
            resp = [:]
            if ( item.hasCommand(subid) ) {
                item."$subid"()
                resp = getOther(swid, item)
            }
        }
        else if ( lightflags.contains(subid) ) {
            resp = setGenericLight(myothers, swid, cmd, swattr, subid)
        }
        else if ( item.hasCommand(cmd) ) {
            item."$cmd"()
            resp = getOther(swid, item)
        }
    }
    return resp
}

// new setAny routine that could replace all the other stuff above
// pending testing and prove out
def setWater(swid, cmd, swattr, subid) {
    logcaller("setWater", swid, cmd, swattr, subid)
    def resp = false
    def item  = mywaters.find {it.id == swid }
    if (item) {
        def newsw = item.currentValue
        if ( subid=="water" && swattr.endsWith(" dry") && item.hasCommand("wet") ) {
            item.wet()
        } else if ( subid=="water" && swattr.endsWith(" wet") && item.hasCommand("dry") ) {
            item.dry()
        } else if ( subid.startsWith("_") ) {
            subid = subid.substring(1)
            if ( item.hasCommand(subid) ) {
                item."$subid"()
            }
        } else if ( item.hasCommand(cmd) ) {
            item."$cmd"()
        }
        resp = getThing(mywaters, swid, item)
    }
    return resp

}

def setMode(swid, cmd, swattr, subid) {
    logcaller("setMode", swid, cmd, swattr, subid)
    def resp = false
    def newsw
    def idx
    def allmodes = location.getModes()
    
    if ( subid=="themode" ) {
        def themode = swattr.substring(swattr.lastIndexOf(" ")+1)
        idx=allmodes.findIndexOf{it.name == themode}

        if (idx!=null) {
            idx = idx+1
            if (idx == allmodes.size() ) { idx = 0 }
            newsw = allmodes[idx].getName()
        } else {
            newsw = allmodes[0].getName()
        }
    // handle commands sent by GUI or user
    } else if (subid.startsWith("_")) {
        cmd = subid.substring(1)
        idx=allmodes.findIndexOf{it.name == cmd}
        newsw = (idx!=null) ? cmd : allmodes[0].getName()
    } else if ( cmd ) {
        idx=allmodes.findIndexOf{it.name == cmd}
        newsw = (idx!=null) ? cmd : allmodes[0].getName()
    } else {
        newsw = allmodes[0].getName()
    }

    logger("Mode changed to $newsw", "info");
    location.setMode(newsw);
    resp =  [   sitename: location.getName(),
                themode: newsw
            ];
    
    return resp
}

def setSHMState(swid, cmd, swattr, subid){
    def cmds = ["away", "stay", "night", "off"]
    def keynames = ["Away", "Home", "Night", "Disarmed"]
    logcaller("setSHMState", swid, cmd, swattr, subid)

    // first handle toggling on icon in gui using attr information
    // default is disarm if something isn't understood
    if ( subid=="state" && swattr && swattr.startsWith("shm") ) {
        cmd = false
        def i = 0
        for (defkey in keynames) {
            i++
            if ( swattr.endsWith(defkey) ) {
                if ( i >= keynames.size() ) { i = 0 }
                cmd = cmds[i]
                break
            }
        }
        
        if ( !cmd ) {
            i = 0
            for (defkey in cmds) {
                i++
                if ( swattr.endsWith(defkey) ) {
                    if ( i >= cmds.size() ) { i = 0 }
                    cmd = cmds[i]
                    break
                }
            }
        }
        
    // handle commands sent by GUI
    } else if (subid.startsWith("_")) {
        cmd = subid.substring(1)
    }
    
    // mimic night by setting mode to night and changing to away
    def k = cmds.findIndexOf{ it == cmd }
    if ( k < 0 ) { k = 3 }
    def statusname = keynames[k]
    
    if ( cmd=="night" ) {
        cmd = "stay"
        // location.setMode("Night");
        logger("SHM Night mode set which acts just like Stay", "info");
    }

    // handle API commands sent by name
    if ( cmd && cmds.contains(cmd) ) {
        sendLocationEvent(name: "alarmSystemStatus" , value : cmd )
    }

    
    logger("SHM state set to $cmd and given name= $statusname", "info")
    def resp = [name : "Smart Home Monitor", state: statusname]
    for (defcmd in cmds) {
        resp.put("_${defcmd}",defcmd)
    }
    return resp
}

def hsmStatusHandler(evt) {
    log.info "HSM state set to ${evt.value}" + (evt.value=="rule" ? $evt.descriptionText : "" )
}
def hsmAlertHandler(evt) {
    log.info "HSM alert: ${evt.value}"
}

def setHSMState(swid, cmd, swattr, subid){

    logcaller("setHSMState", swid, cmd, swattr, subid)
    def key = location.hsmStatus?: false
    if ( !key ) {
        def noresp = [name : "Hubitat Safety Monitor", state: "Not Installed"]
        return noresp
    }
    
    def cmds = ["armAway", "armHome", "armNight", "disarm", "armRules", "disarmRules", "disarmAll", "armAll", "cancelAlerts"]
    def keys = ["Away", "Home", "Night", "Disarmed"]
    // def keynames = ["Armed Away", "Armed Home", "Armed Night", "Disarmed"]

    // first handle toggling on icon in gui using attr information
    // use the keys array to accomodate friendly and native names of modes
    def i = 0
    if ( subid=="state" && swattr && swattr.startsWith("hsm") ) {
        cmd = "disarm"
        for (defkey in keys) {
            i++
            if ( swattr.endsWith(defkey) ) {
                if ( i >= keys.size() ) { i = 0 }
                cmd = cmds[i]
                break
            }
        }
        
    // handle commands sent by GUI or user
    } else if (subid.startsWith("_")) {
        cmd = subid.substring(1)
    }

    // deal with invalid names a user might give using api
    // the gui would never send anything invalid
    if ( !cmd || !cmds.contains(cmd) ) {
        cmd = "disarm"
    }
    
    // send command to change the alarm mode
    sendLocationEvent(name: "hsmSetArm", value: cmd)
    logger("HSM arm set with cmd= ${cmd}", "info")
    
    def k = cmds.findIndexOf{ it == cmd }
    if ( k<0 || k>3 ) { k = 3 }
    def keyname = keys[k]
    def resp = [name : "Hubitat Safety Monitor", state: keyname]
    for (defcmd in cmds) {
        resp.put("_${defcmd}",defcmd)
    }
    // def resp = getHSMState(swid)
    return resp
}

def setDimmer(swid, cmd, swattr, subid) {
    def resp = setGenericLight(mydimmers, swid, cmd, swattr, subid)
    return resp
}

// handle functions for bulbs, dimmers, and lights
// hardened this to also handle regular switches
// this is way more complex than I like but it has to handle a bunch
// of potential GUI options for processing dimmers and colors
// up and down arrows, sliders, etc. 
// and it has to handle API calls that could have all sorts of options
def setGenericLight(mythings, swid, cmd, swattr, subid) {
    def resp = false
    def item  = mythings.find {it.id == swid }
    def newsw = false
    def hue = false
    def saturation = false
    def temperature = false
    def newcolor = false
    
    if (item ) {
        
        logcaller(item.getDisplayName(), swid, cmd, swattr, subid)
        def newonoff = item.currentValue("switch")
        logger("setGenericLight: swid = $swid cmd = $cmd swattr = $swattr subid = $subid", "debug");
        // bug fix for grabbing right swattr when long classes involved
        // note: sometime swattr has the command and other times it has the value
        //       just depends. This is a legacy issue when classes were the command
        // we start by handling base GUI case with attr ending in on or off or flash
        if ( subid=="switch" ) {
            if ( swattr.endsWith(" on" ) ) {
                swattr = "on"
            } else if ( swattr.endsWith(" flash" ) ) {
                swattr = "on"
            } else if ( swattr.endsWith(" off" ) ) {
                swattr = "off"
            }
        } else if ( subid=="name" ) {
            swattr = "name"
        }
        
        switch(swattr) {
        
        // this branch is legacy - gui no longer sends toggle as attr
        // i left this in code since API could still send toggle as attr
        case "toggle":
            newonoff = newonoff=="off" ? "on" : "off"
            break
        
        // explicitly toggle light if clicked on name
        // the commented code will leave it unchanged
        case "name":
            // newonoff = item.currentValue("switch")
            newonoff = newonoff=="off" ? "on" : "off"
            break
         
        case "level-up":
            if ( item.hasAttribute("level") ) {
                newsw = item.currentValue("level")
                newsw = newsw.toInteger()
                newsw = (newsw >= 95) ? 100 : newsw - (newsw % 5) + 5
                item.setLevel(newsw)
                if ( item.hasAttribute("hue") ) {
                    def h = item.currentValue("hue").toInteger()
                    def s = item.currentValue("saturation").toInteger()
                    newcolor = hsv2rgb(h, s, newsw)
                }
            }
            newonoff = "on"
            break
              
        case "level-dn":
            if ( item.hasAttribute("level") ) {
                newsw = item.currentValue("level")
                newsw = newsw.toInteger()
                def del = (newsw % 5) == 0 ? 5 : newsw % 5
                newsw = (newsw <= 5) ? 5 : newsw - del
                item.setLevel(newsw)
                if ( item.hasAttribute("hue") ) {
                    def h = item.currentValue("hue").toInteger()
                    def s = item.currentValue("saturation").toInteger()
                    newcolor = hsv2rgb(h, s, newsw)
                }
                newonoff = "on"
            } else {
                newonoff = "off"
            }
            break
         
        case "level":
            if ( cmd.isNumber() && item.hasAttribute("level") ) {
                newsw = cmd.toInteger()
                newsw = (newsw >100) ? 100 : newsw
                item.setLevel(newsw)
                if ( item.hasAttribute("hue") ) {
                    def h = item.currentValue("hue").toInteger()
                    def s = item.currentValue("saturation").toInteger()
                    newcolor = hsv2rgb(h, s, newsw)
                }
                newonoff = (newsw == 0) ? "off" : "on"
            }
            break
         
        case "hue-up":
                hue = item.currentValue("hue").toInteger()
                hue = (hue >= 95) ? 100 : hue - (hue % 5) + 5
                item.setHue(hue)
                def s = item.currentValue("saturation").toInteger()
                def v = item.currentValue("level").toInteger()
                newcolor = hsv2rgb(hue, s, v)
                newonoff = "on"
            break
              
        case "hue-dn":
                hue = item.currentValue("hue").toInteger()
                def del = (hue % 5) == 0 ? 5 : hue % 5
                hue = (hue <= 5) ? 5 : hue - del
                item.setHue(hue)
                def s = item.currentValue("saturation").toInteger()
                def v = item.currentValue("level").toInteger()
                newcolor = hsv2rgb(hue, s, v)
                newonoff = (v == 0) ? "off" : "on"
            break
              
        case "saturation-up":
                saturation = item.currentValue("saturation").toInteger()
                saturation = (saturation >= 95) ? 100 : saturation - (saturation % 5) + 5
                item.setSaturation(saturation)
                def h = item.currentValue("hue").toInteger()
                def v = item.currentValue("level").toInteger()
                newcolor = hsv2rgb(h, saturation, v)
                newonoff = "on"
            break
              
        case "saturation-dn":
                saturation = item.currentValue("saturation").toInteger()
                def del = (saturation % 5) == 0 ? 5 : saturation % 5
                saturation = (saturation <= 5) ? 5 : saturation - del
                item.setSaturation(saturation)
                def h = item.currentValue("hue").toInteger()
                def v = item.currentValue("level").toInteger()
                newcolor = hsv2rgb(h, saturation, v)
                newonoff = (v == 0) ? "off" : "on"
            break
              
        case "colorTemperature-up":
                temperature = item.currentValue("colorTemperature").toInteger()
                temperature = (temperature >= 6500) ? 6500 : temperature - (temperature % 100) + 100
                item.setColorTemperature(temperature)
                newonoff = "on"
            break
              
        case "colorTemperature-dn":
                temperature = item.currentValue("colorTemperature").toInteger()
                /* temperature drifts up so we cant use round down method */
                def del = 100
                temperature = (temperature <= 2700) ? 2700 : temperature - del
                temperature = (temperature >= 6500) ? 6500 : temperature - (temperature % 100)
                item.setColorTemperature(temperature)
                newonoff = "on"
            break
              
        case "colorTemperature":
                temperature = item.currentValue("colorTemperature").toInteger()
                /* temperature drifts up so we cant use round down method */
                if ( cmd.isNumber() ) {
                    temperature = cmd.toInteger()
                    item.setColorTemperature(temperature)
                }
                newonoff = "on"
            break

        // if the middle value is clicked on we toggle the light
        // this is a legacy behavior from the very old HP days
        case "level-val":
        case "hue-val":
        case "saturation-val":
        case "colorTemperature-val":
            newonoff = newonoff=="off" ? "on" : "off"
            break

        // we also use opposite for GUI sending state in attr variable
        case "on":
            newonoff = "off"
            break
              
        case "off":
            newonoff = "on"
            break

        // this supports api calls and clicking on color circle
        // the level is not returned to prevent slider from moving around
        case "color":
            if (cmd.startsWith("hsl(") && cmd.length()==16) {
                hue = cmd.substring(4,7).toInteger()
                saturation = cmd.substring(8,11).toInteger()
                def v = cmd.substring(12,15).toInteger()
                item.setHue(hue)
                item.setSaturation(saturation)
                item.setLevel(v)
                newcolor = hsv2rgb(hue, saturation, v)
                newonoff = "on"
            }
            break
              
        default:
            if (cmd=="on" || cmd=="off" || cmd=="flash") {
                newonoff = cmd
            } else if (subid.startsWith("_")) {
                newonoff = subid.substring(1)
            } else if ( subid=="switch" ) {
                newonoff = newonoff=="off" ? "on" : "off"
            }

            // set the level if a number is given for attr
            if ( swattr.isNumber() && item.hasAttribute("level") ) {
                newsw = swattr.toInteger()
                item.setLevel(newsw)
            }
            break               
              
        }

        // execute the new command
        if ( item.hasCommand(newonoff) ) {
            item."$newonoff"()
        }

        // return the fields that were changed
        if ( subid=="name" && cmd ) { 
            resp = ["name": cmd]
        } else {
            resp = ["name": item.displayName]
        }
        resp.put("switch", newonoff)
        if ( newsw ) { resp.put("level", newsw) }
        if ( newcolor ) { resp.put("color", newcolor) }
        if ( hue ) { resp.put("hue", hue) }
        if ( saturation ) { resp.put("saturation", saturation) }
        if ( temperature ) { resp.put("colorTemperature", temperature) }
    }
    return resp
}

def hsv2rgb(h, s, v) {
  def r, g, b
  
  h /= 100.0
  s /= 100.0
  v /= 100.0
  

  def i = Math.floor(h * 6);
  def f = h * 6 - i;
  def p = v * (1 - s)
  def q = v * (1 - f * s)
  def t = v * (1 - (1 - f) * s);

  switch (i % 6) {
    case 0: r = v; g = t; b = p; break;
    case 1: r = q; g = v; b = p; break;
    case 2: r = p; g = v; b = t; break;
    case 3: r = p; g = q; b = v; break;
    case 4: r = t; g = p; b = v; break;
    case 5: r = v; g = p; b = q; break;
  }
  
    r = Math.floor(r*255).toInteger()
    g = Math.floor(g*255).toInteger()
    b = Math.floor(b*255).toInteger()

  def rhex = Integer.toHexString(r)
  def ghex = Integer.toHexString(g)
  def bhex = Integer.toHexString(b)
  
    rhex = rhex == "0" ? "00" : rhex
    ghex = ghex == "0" ? "00" : ghex
    bhex = bhex == "0" ? "00" : bhex
  return "#"+rhex+ghex+bhex
}

def setMomentary(swid, cmd, swattr, subid) {
    logcaller("setMomentary", swid, cmd, swattr, subid)
    def resp = false
    def item  = mymomentaries.find {it.id == swid }
    if (item) {
        item.push()
        resp = getMomentary(swid, item)
    }
    return resp
}

def setLock(swid, cmd, swattr, subid) {
    logcaller("setLock", swid, cmd, swattr, subid)
    def resp = false
    def newsw
    def item  = mylocks.find {it.id == swid }
    if (item) {
        if (cmd=="toggle") {
            newsw = item.currentLock=="locked" ? "unlocked" : "locked"
            if ( newsw=="locked" ) {
               item.lock()
            } else {
               item.unlock()
            }
        } else if ( subid=="lock" && swattr.endsWith("unlocked") ) {
            item.lock()
            newsw = "locked";
        } else if ( subid=="lock" && swattr.endsWith("locked") ) {
            item.unlock()
            newsw = "unlocked";
        } else if ( cmd=="unknown" ) {
            newsw = item.currentLock
        } else if ( cmd=="move" ) {
            newsw = item.currentLock
        } else if (cmd=="unlock") {
            item.unlock()
            newsw = "unlocked"
        } else if (cmd=="lock") {
            item.lock()
            newsw = "locked"
        }
        resp = [lock: newsw]
        if ( item.hasCapability("Battery") ) {
            resp.put("battery", item.currentValue("battery"))
        }
    }
    return resp
}

def setValve(swid, cmd, swattr, subid) {
    logcaller("setValve", swid, cmd, swattr, subid)
    def resp = false
    def item  = myvalves.find {it.id == swid }
    if (item) {
        def newsw = item.currentValue
        if ( subid=="valve" && swattr.endsWith(" open") ) {
            item.close()
        } else if ( subid=="valve" && swattr.endsWith(" closed") ) {
            item.open()
        } else if ( subid=="switch" && swattr.endsWith(" on") ) {
            item.off()
        } else if ( subid=="switch" && swattr.endsWith(" off") ) {
            item.on()
        } else if ( subid.startsWith("_") ) {
            subid = subid.substring(1)
            if ( item.hasCommand(subid) ) {
                item."$subid"()
            }
        } else if ( item.hasCommand(cmd) ) {
            item."$cmd"()
        }
        resp = getThing(myvalves, swid, item)
    }
    return resp
}

def setThermostat(swid, cmd, swattr, subid) {
    logcaller("setThermostat", swid, cmd, swattr, subid)
    def resp = false
    def newsw = 72
    def tempint
    def item  = mythermostats.find {it.id == swid }
    if (item) {
        
          resp = getThermostat(swid, item)
          // switch (swattr) {
          // case "heat-up":
          if ( subid=="heatingSetpoint-up" || swattr.contains("heatingSetpoint-up") ) {
              newsw = cmd.toInteger() + 1
              if (newsw > 85) newsw = 85
              // item.heat()
              item.setHeatingSetpoint(newsw.toString())
              resp['heatingSetpoint'] = newsw
              // break
          }
          
          // case "cool-up":
          else if ( subid=="coolingSetpoint-up" || swattr.contains("coolingSetpoint-up") ) {
              newsw = cmd.toInteger() + 1
              if (newsw > 85) newsw = 85
              // item.cool()
              item.setCoolingSetpoint(newsw.toString())
              resp['coolingSetpoint'] = newsw
              // break
          }

          // case "heat-dn":
          else if ( subid=="heatingSetpoint-dn" || swattr.contains("heatingSetpoint-dn")) {
              newsw = cmd.toInteger() - 1
              if (newsw < 50) newsw = 50
              // item.heat()
              item.setHeatingSetpoint(newsw.toString())
              resp['heatingSetpoint'] = newsw
              // break
          }
          
          // case "cool-dn":
          else if ( subid=="coolingSetpoint-dn" || swattr.contains("coolingSetpoint-dn")) {
              newsw = cmd.toInteger() - 1
              if (newsw < 60) newsw = 60
              // item.cool()
              item.setCoolingSetpoint(newsw.toString())
              resp['coolingSetpoint'] = newsw
              // break
          }
          
          // case "thermostat thermomode heat":
          else if ( swattr.contains("emergency")) {
              item.heat()
              newsw = "heat"
              resp['thermostatMode'] = newsw
              // break
          }
          
          // case "thermostat thermomode heat":
          else if ( swattr.contains("thermostatMode") && (cmd=="heat" || cmd=="heatingSetpoint" || swattr.contains("heat")) ) {
              item.cool()
              newsw = "cool"
              resp['thermostatMode'] = newsw
              // break
          }
          
          // case "thermostat thermomode cool":
          else if ( swattr.contains("thermostatMode") && (cmd=="cool" || cmd=="coolingSetpoint" || swattr.contains("cool")) ) {
              item.auto()
              newsw = "auto"
              resp['thermostatMode'] = newsw
              // break
          }
          
          // case "thermostat thermomode auto":
          else if ( swattr.contains("thermostatMode") && (cmd=="auto" || swattr.contains("auto")) ) {
              item.off()
              newsw = "off"
              resp['thermostatMode'] = newsw
              // break
          }
          
          // case "thermostat thermomode off":
          else if ( swattr.contains("thermostatMode") && (cmd=="off" || swattr.contains("off")) ) {
              item.heat()
              newsw = "heat"
              resp['thermostatMode'] = newsw
              // break
          }
          
          // case "thermostat thermofan fanOn":
          else if ( swattr.contains("thermostatFanMode") && (cmd=="on" || swattr.contains("on")) ) {
              item.fanAuto()
              newsw = "auto"
              resp['thermostatFanMode'] = newsw
              // break
          }
          
          // case "thermostat thermofan fanAuto":
          else if ( swattr.contains("thermostatFanMode") && (cmd=="auto" || swattr.contains("auto")) ) {
              if ( item.hasCommand("fanCirculate") ) {
                item.fanCirculate()
                newsw = "circulate"
              } else {
                  item.fanOn()
                  newsw = "on"
              }
              resp['thermostatFanMode'] = newsw
              // break
          }
          
          // case "thermostat thermofan fanAuto":
          else if ( swattr.contains("thermostatFanMode") && (cmd=="circulate" || swattr.contains("circulate")) ) {
              item.fanOn()
              newsw = "on"
              resp['thermostatFanMode'] = newsw
              // break
          }

        else if ( subid=="temperature" ) {
            def subidval = resp[subid]
            resp = [temperature: subidval]
        }
          
        else if ( subid=="heatingSetpoint" ) {
            def subidval = resp[subid]
            resp = [heatingSetpoint: subidval]
        }
          
        else if ( subid=="coolingSetpoint" ) {
            def subidval = resp[subid]
            resp = [coolingSetpoint: subidval]
        }
          
        else if ( subid=="state" || subid=="thermostatFanMode" ) {
            def subidval = resp[subid]
            resp = [state: subidval]
        }
          
        else if ( subid=="humidity" ) {
            def subidval = resp[subid]
            resp = [humidity: subidval]
        }
           
          // define actions for python end points  
        else {
          // default:
              if ( (cmd=="heat" || cmd=="heatingSetpoint" || cmd=="emergencyHeat") && swattr.isNumber()) {
                  item.setHeatingSetpoint(swattr)
                  resp['heatingSetpoint'] = swattr
              }
              else if ( (cmd=="cool" || cmd=="coolingSetpoint") && swattr.isNumber()) {
                  item.setCoolingSetpoint(swattr)
                  resp['coolingSetpoint'] = swattr
              }
              else if (cmd=="auto" && swattr.isNumber() && item.hasCapability("thermostatSetpoint")) {
                  item.thermostatSetpoint(swattr)
              } else if ( item.hasCommand(cmd) ) {
                  item."$cmd"()
              }

            // break
        }
      
    }
    return resp
}

def setMusic(swid, cmd, swattr, subid) {
    logcaller("setMusic", swid, cmd, swattr, subid)
    def resp = false
    def item  = mymusics.find {it.id == swid }
    def newsw
    if (item) {
        resp = getMusic(swid, item)
        
        // fix old bug from addition of extra class stuff
        // had to fix this for all settings
        if ( subid=="mute" && swattr.contains("unmuted" )) {
            newsw = "muted"
            item.mute()
            resp['mute'] = newsw
        } else if ( subid=="mute" && swattr.contains(" muted" )) {
            newsw = "unmuted"
            item.unmute()
            resp['mute'] = newsw
        } else if ( subid=="level-up" || swattr.contains("level-up") ) {
            newsw = cmd.toInteger()
            newsw = (newsw >= 95) ? 100 : newsw - (newsw % 5) + 5
            item.setLevel(newsw)
            resp['level'] = newsw
        } else if ( subid=="level-dn" || swattr.contains("level-dn") ) {
            newsw = cmd.toInteger()
            def del = (newsw % 5) == 0 ? 5 : newsw % 5
            newsw = (newsw <= 5) ? 5 : newsw - del
            item.setLevel(newsw)
            resp['level'] = newsw
        } else if ( subid=="level" || swattr.contains("level") ) {
            newsw = cmd.toInteger()
            item.setLevel(newsw)
            resp['level'] = newsw
        } else if ( subid=="music-play" || swattr.contains("music-play") ) {
            newsw = "playing"
            item.play()
            resp['status'] = newsw
            resp['trackDescription'] = item.currentValue("trackDescription")
        } else if ( subid=="music-stop" || swattr.contains("music-stop") ) {
            newsw = "stopped"
            item.stop()
            resp['status'] = newsw
            resp['trackDescription'] = ""
        } else if ( subid=="music-pause" || swattr.contains("music-pause") ) {
            newsw = "paused"
            item.pause()
            resp['status'] = newsw
        } else if ( subid=="music-previous" || swattr.contains("music-previous") ) {
            item.previousTrack()
            resp['trackDescription'] = item.currentValue("trackDescription")
        } else if ( subid=="music-next" || swattr.contains("music-next") ) {
            item.nextTrack()
            resp['trackDescription'] = item.currentValue("trackDescription")
        } else if ( subid.startsWith("_") ) {
            subid = subid.substring(1)
            if ( item.hasCommand(subid) ) {
                item."$subid"()
                resp['trackDescription'] = item.currentValue("trackDescription")
            }
        } else if ( cmd && item.hasCommand(cmd) ) {
            item."$cmd"()
            resp['trackDescription'] = item.currentValue("trackDescription")
        }
    }
    return resp
}

def setRoutine(swid, cmd, swattr, subid) {
    logcaller("setRoutine", swid, cmd, swattr, subid)
    def routine = location.helloHome?.getPhrases().find{ it.id == swid }
    if (subid=="label" && routine) {
        try {
            location.helloHome?.execute(routine.label)
        } catch (e) {}
    } else if (cmd) {
        try {
            location.helloHome?.execute(cmd)
        } catch (e) {}
    }
    return routine
}

def registerAll() {
    runIn(200, "registerLights");
    runIn(200, "registerBulbs");
    runIn(200, "registerDoors");
    runIn(200, "registerMotions");
    runIn(200, "registerOthers");
    runIn(200, "registerThermostats");
    runIn(200, "registerTracks");
    runIn(200, "registerMusics");
    
    // skip these on purpose because they change slowly and report often
    // runIn(10, "registerSlows");
}

def registerLights() {
    registerCapabilities(myswitches,"switch")
    registerCapabilities(mydimmers,"switch")
    registerCapabilities(mydimmers,"level")
    registerCapabilities(mylights,"switch")
}

def registerBulbs() {
    registerCapabilities(mybulbs,"switch")
    registerCapabilities(mybulbs,"hue")
    registerCapabilities(mybulbs,"saturation")
    registerCapabilities(mybulbs,"level")
    registerCapabilities(mybulbs,"color")
}

def registerDoors() {
    registerCapabilities(mycontacts,"contact")
    registerCapabilities(mydoors,"door")
    registerCapabilities(mylocks,"lock")
}

def registerMotions() {
    registerCapabilities(mymotions,"motion")
}

def registerOthers() {
    registerCapabilities(myvalves,"valve")
    registerCapabilities(mywaters,"water")
    registerCapabilities(mypresences,"presence")
    registerCapabilities(mysmokes,"smoke")
}

def registerSlows() {
     registerCapabilities(mytemperatures, "temperature")
     registerCapabilities(myilluminances, "illuminance")
     registerCapabilities(mypowers, "power")
     registerCapabilities(mypowers, "energy")
}

def registerThermostats() {
    registerCapabilities(mythermostats, "heatingSetpoint")
    registerCapabilities(mythermostats, "coolingSetpoint")
    registerCapabilities(mythermostats, "thermostatFanMode")
    registerCapabilities(mythermostats, "thermostatMode")
    registerCapabilities(mythermostats, "thermostatSetpoint")
    registerCapabilities(mythermostats, "temperature")
}

def registerMusics() {
    registerCapabilities(mymusics, "status")
    registerCapabilities(mymusics, "level")
    registerCapabilities(mymusics, "mute")
}

def registerTracks() {
    registerCapabilities(mymusics, "trackDescription")
}

def registerCapabilities(devices, capability) {
    subscribe(devices, capability, changeHandler)
    logger("Registering ${capability} for ${devices?.size() ?: 0} things", "trace")
}

def changeHandler(evt) {
    def src = evt?.source
    def deviceid = evt?.deviceId
    def deviceName = evt?.displayName
    def attr = evt?.name
    def value = evt?.value

    // handle special case of hsm
//    if ( attr=="hsmStatus " || attr=="alarmSystemStatus" ) {
//        deviceid = "alarmSystemStatus_${location.id}"
//        attr = "alarmSystemStatus"
//    }
    
    logger("Sending ${src} Event ( ${deviceName}, ${deviceid}, ${attr}, ${value} ) to Websocket at (${state.directIP}:${state.directPort})", "info")
    if (state.directIP && state.directPort && deviceName && deviceid && attr && value) {

        // fix bulbs
        if ( (mybulbs?.find {it.id == deviceid}) && (attr=="hue" || attr=="saturation" || attr=="level" || attr=="color") ) {
            def h = value["hue"]?.toInteger()
            def s = value["saturation"]?.toInteger()
            def v = value["level"]?.toInteger()
            if ( h && s && v ) {
                def newcolor = hsv2rgb(h, s, v)
                value["hue"] = h
                value["saturation"] = s
                value["level"] = v
                value["color"] = newcolor
            }
            
        }
        
        
        // set a hub action - include the access token so we know which hub this is
        def params = [
            method: "POST",
            path: "/",
            headers: [
                HOST: "${state.directIP}:${state.directPort}",
                'Content-Type': 'application/json'
            ],
            body: [
                msgtype: "update",
                change_name: deviceName,
                change_device: deviceid,
                change_attribute: attr,
                change_value: value
            ]
        ]
        def result
        if ( isST() ) {
            result = physicalgraph.device.HubAction.newInstance(params)
        } else {
            result = hubitat.device.HubAction.newInstance(params)
        }
        sendHubCommand(result)
    }
}

def postHub(message) {

    logger("ip= ${state.directIP} port= ${state.directPort} message= ${message}", "info" )
    
    if ( message && state?.directIP && state?.directPort ) {
        // Send Using the Direct Mechanism
        logger("Sending ${message} to Websocket at ${state.directIP}:${state.directPort}", "info")

        // set a hub action - include the access token so we know which hub this is
        def params = [
            method: "POST",
            path: "/",
            headers: [
                HOST: "${state.directIP}:${state.directPort}",
                'Content-Type': 'application/json'
            ],
            body: [
                msgtype: "initialize",
                message: message,
            ]
        ]
        def result
        if ( isST() ) {
            result = physicalgraph.device.HubAction.newInstance(params)
        } else {
            result = hubitat.device.HubAction.newInstance(params)
        }
        sendHubCommand(result)
        
    }
    
}

// Wrapper function for all logging.
private logcaller(caller, swid, cmd, swattr, subid) {
    logger("${caller}: swid= $swid cmd= $cmd swattr= $swattr subid= $subid", "debug")
}

private logger(msg, level = "debug") {

    switch(level) {
        case "error":
            if (state.loggingLevelIDE >= 1) log.error msg
            break

        case "warn":
            if (state.loggingLevelIDE >= 2) log.warn msg
            break

        case "info":
            if (state.loggingLevelIDE >= 3) log.info msg
            break

        case "debug":
            if (state.loggingLevelIDE >= 4) log.debug msg
            break

        case "trace":
            if (state.loggingLevelIDE >= 5) log.trace msg
            break

        default:
            log.debug msg
            break
    }
}

/*************************************************************************/
/* webCoRE Connector v0.2                                                */
/*************************************************************************/
/*  Copyright 2016 Adrian Caramaliu <ady624(at)gmail.com>                */
/*                                                                       */
/*  This program is free software: you can redistribute it and/or modify */
/*  it under the terms of the GNU General Public License as published by */
/*  the Free Software Foundation, either version 3 of the License, or    */
/*  (at your option) any later version.                                  */
/*                                                                       */
/*  This program is distributed in the hope that it will be useful,      */
/*  but WITHOUT ANY WARRANTY; without even the implied warranty of       */
/*  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the         */
/*  GNU General Public License for more details.                         */
/*                                                                       */
/*  You should have received a copy of the GNU General Public License    */
/*  along with this program.  If not, see <http://www.gnu.org/licenses/>.*/
/*************************************************************************/
private webCoRE_handle(){return'webCoRE'}
private webCoRE_init(pistonExecutedCbk)
{
    state.webCoRE=(state.webCoRE instanceof Map?state.webCoRE:[:])+(pistonExecutedCbk?[cbk:pistonExecutedCbk]:[:]);
    subscribe(location,"${webCoRE_handle()}.pistonList",webCoRE_handler);
    if(pistonExecutedCbk)subscribe(location,"${webCoRE_handle()}.pistonExecuted",webCoRE_handler);webCoRE_poll();
}
private webCoRE_poll(){sendLocationEvent([name: webCoRE_handle(),value:'poll',isStateChange:true,displayed:false])}
public  webCoRE_execute(pistonIdOrName,Map data=[:]){def i=(state.webCoRE?.pistons?:[]).find{(it.name==pistonIdOrName)||(it.id==pistonIdOrName)}?.id;if(i){sendLocationEvent([name:i,value:app.label,isStateChange:true,displayed:false,data:data])}}
public  webCoRE_list(mode)
{
    def p=state.webCoRE?.pistons;
    if(p)p.collect{
        mode=='id'?it.id:(mode=='name'?it.name:[id:it.id,name:it.name])
        logger("Reading piston: ${it}", "debug");
    }
    return p
}
public  webCoRE_handler(evt){switch(evt.value){case 'pistonList':List p=state.webCoRE?.pistons?:[];Map d=evt.jsonData?:[:];if(d.id&&d.pistons&&(d.pistons instanceof List)){p.removeAll{it.iid==d.id};p+=d.pistons.collect{[iid:d.id]+it}.sort{it.name};state.webCoRE = [updated:now(),pistons:p];};break;case 'pistonExecuted':def cbk=state.webCoRE?.cbk;if(cbk&&evt.jsonData)"$cbk"(evt.jsonData);break;}}
