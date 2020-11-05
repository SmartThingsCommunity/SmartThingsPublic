/**
 *  HousePanel
 *
 *  Copyright 2016 to 2020 by Kenneth Washington
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
 * This is a SmartThings and Hubitat app that works with the HousePanel smart dashboard platform
 * 
 * Revision history:
 * 10/24/2020 - clean up logger for tracking hub push changes
              - remove routines since they are depracated and classic app is gone
 * 09/12/2020 - check for valid IP and port value entries for HP hub pushes
 * 07/20/2020 - add a status field for device items using getStatus() - thanks @phil_c
 * 07/18/2020 - include level for others and actuators so window shades work
                also fix bug to include actuators in the event handler logic
 * 07/13/2020 - fix bug in setOther where an invalid attr was passed - thanks @phil_c
 * 07/10/2020 - fix button rule callback to send scalar instead of object
 *            - remove all manual log.info and log.debug and user logger everywhere
 * 06/16/2020 - fix mode and momentary buttons that I broke in a prior update
 * 06/12/2020 - add toggle as a command for all light types
 * 06/09/2020 - remove dynamic pages since they mess up OAUTH from HP side
 * 05/29/2020 - added button and actuator support, remove depracated Lights type
 * 05/17/2020 - overhaul the way we do push notices via registration
 * 05/10/2020 - tidy up switch reg, power reporting, and add patch for audio
 * 04/09/2020 - mode change fix from 03/29 update
 * 04/02/2020 - fix bug in mode and return only one now
 * 03/29/2020 - clean up mode and fix hub pushes to be more reliable
 * 02/02/2020 - added secondary hub push and include hub id in the push
 * 01/10/2020 - add mode chenge hub push and clean up code
 * 12/20/2019 - bug fixes and cleanup
 * 12/19/2019 - update to include new Sonos audioNotification capability
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

public static String handle() { return "HousePanel" }
definition(
    name: "${handle()}",
    namespace: "kewashi",
    author: "Kenneth Washington",
    description: "Tap here to install ${handle()} - a highly customizable smarthome dashboard. ",
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
        paragraph "Enable Pistons? You must have WebCore installed for this to work. Beta feature for Hubitat hubs."
        input (name: "usepistons", type: "bool", multiple: false, title: "Use Pistons?", required: false, defaultValue: false)
        paragraph "Timezone and Format for event time fields; e.g., America/Detroit, Europe/London, or America/Los_Angeles"
        input (name: "timezone", type: "text", multiple: false, title: "Timezone Name:", required: false, defaultValue: "America/Detroit")
        input (name: "dateformat", type: "text", multiple: false, title: "Date Format:", required: false, defaultValue: "M/dd h:mm")
        paragraph "Specify these parameters to enable your panel to stay in sync with things when they change in your home."
        input "webSocketHost", "text", title: "Host IP", defaultValue: "192.168.11.32", required: false
        input "webSocketPort", "text", title: "Port", defaultValue: "3080", required: false
        paragraph "The Alt Host IP and Port values are used to send hub pushes to two distinct installations of HousePanel. " +
                  "If left blank a secondary hub push will not occur. Only use this if you are hosting two versions of HP " +
                  "that both need to stay in sync with your smart home hubs."
        input "webSocketHost2", "text", title: "Alt Host IP", defaultValue: "192.168.11.20", required: false
        input "webSocketPort2", "text", title: "Alt Port", defaultValue: "3180", required: false
        input (
            name: "configLogLevel",
            title: "IDE Live Logging Level:\nMessages with this level and higher will be logged to the IDE.",
            type: "enum",
            options: ["0" : "None", "1" : "Error", "2" : "Warning", "3" : "Info", "4" : "Debug", "5" : "Trace"],
            defaultValue: "3",
            displayDuringSetup: true,
            required: false
        )
    }
    section("Switches, Dimmers and Buttons") {
            input "myswitches", "capability.switch", multiple: true, required: false, title: "Switches"
            input "mydimmers", "capability.switchLevel", hideWhenEmpty: true, multiple: true, required: false, title: "Switch Level Dimmers"
            input "mymomentaries", "capability.momentary", hideWhenEmpty: true, multiple: true, required: false, title: "Momentary Switches"
            input "mybuttons", "capability.button", hideWhenEmpty: true, multiple: true, required: false, title: "Buttons"
            input "mybulbs", "capability.colorControl", hideWhenEmpty: true, multiple: true, required: false, title: "Color Control Bulbs"
            input "mypowers", "capability.powerMeter", hideWhenEmpty: true, multiple: true, required: false, title: "Power Meters"
    }
    section ("Motion and Presence") {
            input "mypresences", "capability.presenceSensor", hideWhenEmpty: true, multiple: true, required: false, title: "Presence"
            input "mymotions", "capability.motionSensor", multiple: true, required: false, title: "Motion"
    }
    section ("Doors, Contacts, and Locks") {
            input "mycontacts", "capability.contactSensor", hideWhenEmpty: true, multiple: true, required: false, title: "Contact Sensors"
            input "mydoors", "capability.doorControl", hideWhenEmpty: true, multiple: true, required: false, title: "Garage Doors"
            input "mylocks", "capability.lock", hideWhenEmpty: true, multiple: true, required: false, title: "Locks"
    }
    section ("Thermostats and Weather") {
            input "mythermostats", "capability.thermostat", hideWhenEmpty: true, multiple: true, required: false, title: "Thermostats"
            input "mytemperatures", "capability.temperatureMeasurement", hideWhenEmpty: true, multiple: true, required: false, title: "Temperature Measures"
            input "myilluminances", "capability.illuminanceMeasurement", hideWhenEmpty: true, multiple: true, required: false, title: "Illuminance Measurements"
            input "myweathers", "device.smartweatherStationTile", hideWhenEmpty: true, multiple: true, required: false, title: "Weather tile"
            input "myaccuweathers", "device.accuweatherDevice", hideWhenEmpty: true, multiple: true, required: false, title: "AccuWeather tile"
    }
    section ("Water, Sprinklers and Smoke") {
            input "mywaters", "capability.waterSensor", hideWhenEmpty: true, multiple: true, required: false, title: "Water Sensors"
            input "myvalves", "capability.valve", hideWhenEmpty: true, multiple: true, required: false, title: "Sprinklers"
            input "mysmokes", "capability.smokeDetector", hideWhenEmpty: true, multiple: true, required: false, title: "Smoke Detectors"
    }
    section ("Music and Audio") {
            paragraph "Music things use the legacy Sonos device handler. Audio things use the new Audio handler that works with multiple audio device types including Sonos."
            input "mymusics", "capability.musicPlayer", hideWhenEmpty: true, multiple: true, required: false, title: "Music Players"
            input "myaudios", "capability.audioNotification", hideWhenEmpty: true, multiple: true, required: false, title: "Audio Devices"
    }
    section ("Other Sensors and Actuators") {
            paragraph "Any thing can be added as an Other sensor or actuator. Other sensors and actuators bring in ALL fields and commands supported by the device."
            input "myothers", "capability.sensor", multiple: true, required: false, title: "Which Other Sensors"
            input "myactuators", "capability.actuator", multiple: true, required: false, title: "Which Other Actuators"
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
    state.directIP = state.directIP.trim()
    state.directPort = settings?.webSocketPort ?: "3080"
    state.directPort = state.directPort.trim()
    state.directIP2 = settings?.webSocketHost2 ?: ""
    state.directIP2 = state.directIP2.trim()
    state.directPort2 = settings?.webSocketPort2 ?: "3180"
    state.directPort2 = state.directPort2.trim()
    state.tz = settings?.timezone ?: "America/Detroit"
    state.prefix = settings?.hubprefix ?: getPrefix()
    state.dateFormat = settings?.dateformat ?: "M/dd h:mm"
    state.powervals = [test: "test"]

    configureHub();
    if ( state.usepistons ) {
        webCoRE_init()
    }
    state.loggingLevelIDE = settings.configLogLevel?.toInteger() ?: 3
    logger("Installed ${hubtype} hub with settings: ${settings} ", "info")
    
    def pattern = ~/\d{1,3}\.\d{1,3}\.\d{1,3}\.\d{1,3}/
    def portpatt = ~/\d+/
    if ( state.directIP ==~ pattern && state.directPort ==~ portpatt ) {
        postHub(state.directIP, state.directPort, "initialize", "", "", "", "", "")
        logger("state changes will be posted to HP at IP: ${state.directIP}:${state.directPort} ", "info")

        if ( state.directIP2 ==~ pattern && state.directPort2 ==~ portpatt ) {
            postHub(state.directIP2, state.directPort2, "initialize", "", "", "", "", "")
            logger("state changes will also be posted to HP at IP: ${state.directIP2}:${state.directPort2} ", "info")
        } else {
            logger("state changes will not be posted to a secondary HP server", "info")
        }
        registerAll()
    } else {
        logger("state changes will not be posted to an HP server", "info")
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
    def cloudhubip
    def cloudendpt
    
    def firmware = hub?.firmwareVersionString ?: "unknown"

    if ( !isHubitat() ) {
        state.hubid = hub.id
        hubip = hub.localIP
        logger("You must go through the OAUTH flow to obtain a proper SmartThings AccessToken", "info")
        logger("You must go through the OAUTH flow to obtain a proper SmartThings EndPoint", "info")
    } else {
        state.hubid = hubUID
        cloudhubip = "https://oauth.cloud.hubitat.com";
        cloudendpt = "${cloudhubip}/${hubUID}/apps/${app.id}/"
        hubip = hub.localIP
        endpt = "${hubip}/apps/api/${app.id}/"
        logger("Hubitat AccessToken = ${state.accessToken}", "info")
        logger("Hubitat EndPoint = ${endpt}", "info")
        logger("Hubitat Cloud Hub = ${cloudhubip}", "info")
        logger("Hubitat Cloud EndPoint = ${cloudendpt}", "info")
    }
    
    logger("Use this information on the Auth page of HousePanel.", "info")
    logger("Hub Platform = ${getPlatform()}", "info")
    logger("Hub IP = ${hubip}", "info")
    logger("Hub ID = ${state.hubid}", "info")
    logger("Hub Firmware = ${firmware}", "info")
    logger("rPI IP Address = ${state.directIP}", "info")
    logger("rPI webSocket Port = ${state.directPort}", "info")
    logger("Alt IP Address = ${state.directIP2}", "info")
    logger("Alt webSocket Port = ${state.directPort2}", "info")
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
                if ( it.value!=priorval && it.value?.length()<120 ) {
                    i++
                    def evtvalue = it.value + " " + it.date.format(state.dateFormat ?: "M/dd h:mm", tz)
                    resp.put("event_${i}", evtvalue )
                    priorval = it.value
                }
            }
        } catch (e) {
            logger("Cannot retrieve history for device ${item.displayName}", "info")
        }

        // also add in a status
        try {
            resp.put("status", item.getStatus())
        } catch(e) {
            log.error e
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
    getThing(myswitches, swid, item)
    // def resp = false
    // item = item ? item : myswitches.find {it.id == swid }
    // if ( item ) {
    //     resp = [name: item.displayName]
    //     resp = addBattery(resp, item)
    //     resp.put("switch", item.currentValue("switch"))
    //     resp = addHistory(resp, item)
    // }
    // return resp
}

def getBulb(swid, item=null) {
    getThing(mybulbs, swid, item)
}

// def getLight(swid, item=null) {
//     getThing(mylights, swid, item)
// }

// handle momentary devices as push buttons
def getMomentary(swid, item=null) {
    def resp = false
    item = item ? item : mymomentaries.find {it.id == swid }
    if ( item ) {
        resp = [name: item.displayName]
        resp = addBattery(resp, item)
        if ( item.hasCapability("Switch") || item.hasCommand("push") ) {
            def curval = item.currentValue("switch")
            if (curval!="on" && curval!="off") { curval = "off" }
            resp.put("momentary", curval)
        }
        resp = addHistory(resp, item)
    }
    return resp
}

def getActuator(swid, item=null) {
    getThing(myactuators, swid, item)
}

def getButton(swid, item=null) {
    getThing(mybuttons, swid, item)
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

def getLock(swid, item=null) {
    getThing(mylocks, swid, item)
}

// this was updated to use the real key names so that push updates work
// note -changes were also made in housepanel.php and elsewhere to support this
def getMusic(swid, item=null) {
    getThing(mymusics, swid, item)
}

def getAudio(swid, item=null) {
    def raw = getThing(myaudios, swid, item)
    logger(raw, "debug" )
    
    // lets put it in the desired order
    def resp = [name: raw.name, audioTrackData: raw.audioTrackData,
        _rewind: raw._rewind, _previousTrack: raw._previousTrack,
        _pause: raw._pause, _play: raw._play, _stop: raw._stop,
        _nextTrack: raw._nextTrack, _fastForward: raw._fastForward,
        playbackStatus: raw.playbackStatus,
        _mute: raw._mute, _unmute: raw._unmute, 
        _muteGroup: raw._muteGroup, _unmuteGroup: raw._unmuteGroup, 
        _volumeDown: raw._volumeDown, _volumeUp: raw._volumeUp,
        _groupVolumeDown: raw._groupVolumeDown, _groupVolumeUp: raw._groupVolumeUp,
        volume: raw.volume,
        mute: raw.mute, groupRole: raw.groupRole]

    resp = addHistory(resp, item)
    return resp
}

// this was updated to use the real key names so that push updates work
// note -changes were also made in housepanel.php and elsewhere to support this
def getThermostat(swid, item=null) {
    getThing(mythermostats, swid, item)
    // item = item ? item : mythermostats.find {it.id == swid }
    // def resp = false
    // if ( item ) {
    //     resp = [name: item.displayName, 
    //         temperature: item.currentValue("temperature"),
    //         heatingSetpoint: item.currentValue("heatingSetpoint"),
    //         coolingSetpoint: item.currentValue("coolingSetpoint"),
    //         thermostatFanMode: item.currentValue("thermostatFanMode"),
    //         thermostatMode: item.currentValue("thermostatMode"),
    //         thermostatOperatingState: item.currentValue("thermostatOperatingState")
    //     ]
    //     resp = addBattery(resp, item)
    //     resp = addAttr(resp, item, "humidity")
    //     resp = addHistory(resp, item)
    // }
    // return resp
}

// use absent instead of "not present" for absence state
def getPresence(swid, item=null) {
    getThing(mypresences, swid, item)
    // item = item ? item : mypresences.find {it.id == swid }
    // def resp = false
    // if ( item ) {
    //     resp = [name: item.displayName]
    //     resp = addBattery(resp, item)
    //     def pval = (item.currentValue("presence")=="present") ? "present" : "absent"
    //     resp.put("presence", pval)
    //     resp = addHistory(resp, item)
    // }
    // return resp
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
        // resp = addAttr(resp, item, "humidity")
        resp = addHistory(resp, item)
    }
    return resp
}

def getWeather(swid, item=null) {
    def resp = getDevice(myweathers, swid, item)
    if ( !resp ) {
        resp = getDevice(myaccuweathers, swid, item)
    }
    return resp
}

def getOther(swid, item=null) {
    getThing(myothers, swid, item)
}

def getPower(swid, item=null) {
    def resp = getThing(mypowers, swid, item)
    try {
        state.powervals[swid] = Float.valueOf(resp.power)
    } catch (e) {
        state.powervals[swid] = 0.0
    }
    return resp
}

def getMyMode(swid, item=null) {
    def curmode = location.getCurrentMode()
    def resp = [ name: "Mode ${swid}", sitename: location.getName(), themode: curmode?.getName() ];
    def allmodes = location.getModes()
    for (defcmd in allmodes) {
        def modecmd = defcmd.getName()
        resp.put("_${modecmd}",modecmd)
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

// routines are depracated
// def getRoutine(swid, item=null) {
//     def routines = location.helloHome?.getPhrases()
//     def routine = item ? item : routines.find{it.id == swid}
//     def resp = routine ? [name: routine.label, label: routine.label] : false
//     return resp
// }

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
                    logger("Attempt to read device attribute for ${swid} failed ${e}", "error")
                }
            }
    	}

        def reserved = ignoredCommands()
        item.supportedCommands.each { comm ->
            try {
                def comname = comm?.getName()
                def args = comm?.getArguments()
                def arglen = args?.size()
                logger("Command for ${swid} = $comname with $arglen args = $args ", "trace")
                if ( arglen==0 && !reserved.contains(comname) ) {
                    resp.put( "_"+comname, comname )
                }
            } catch (ex) {
                logger("Attempt to read device command for ${swid} failed ${ex}", "error")
            }
        }
        resp = addHistory(resp, item)
    }
    return resp
}

def ignoredAttributes() {
    // thanks to the authors of HomeBridge for this list
    def ignore = [
        'DeviceWatch-DeviceStatus', 'DeviceWatch-Enroll', 'checkInterval', 'healthStatus', 'devTypeVer', 'dayPowerAvg', 'apiStatus', 'yearCost', 'yearUsage','monthUsage', 'monthEst', 'weekCost', 'todayUsage',
        'supportedPlaybackCommands', 'groupPrimaryDeviceId', 'groupId', 'supportedTrackControlCommands', 'presets',
        'maxCodeLength', 'maxCodes', 'readingUpdated', 'maxEnergyReading', 'monthCost', 'maxPowerReading', 'minPowerReading', 'monthCost', 'weekUsage', 'minEnergyReading',
        'codeReport', 'scanCodes', 'verticalAccuracy', 'horizontalAccuracyMetric', 'altitudeMetric', 'latitude', 'distanceMetric', 'closestPlaceDistanceMetric',
        'closestPlaceDistance', 'leavingPlace', 'currentPlace', 'codeChanged', 'codeLength', 'lockCodes', 'horizontalAccuracy', 'bearing', 'speedMetric',
        'speed', 'verticalAccuracyMetric', 'altitude', 'indicatorStatus', 'todayCost', 'longitude', 'distance', 'previousPlace','closestPlace', 'places', 'minCodeLength',
        'arrivingAtPlace', 'lastUpdatedDt'
    ]
    return ignore
}

def ignoredCommands() {
    // "groupVolumeUp", "groupVolumeDown", "muteGroup", "unmuteGroup",
    // "indicatorWhenOn","indicatorWhenOff","indicatorNever", "stopLevelChange",
    def ignore = ["setLevel","setHue","setSaturation","setColorTemperature","setColor","setAdjustedColor",
                  "enrollResponse","stopLevelChange","poll","ping","configure",
                  "reloadAllCodes","unlockWithTimeout","markDeviceOnline","markDeviceOffline"
                  ]
    return ignore
}

// make a generic thing getter to streamline the code
def getThing(things, swid, item=null) {
    item = item ? item : things?.find {it.id == swid }
    def resp = item ? [:] : false
    def reservedcap = ignoredAttributes()
    if ( item ) {
        resp.put("name",item.displayName)
        item.capabilities.each {cap ->
            cap.attributes?.each {attr ->
                try {
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
        // add commands other than the ones that require parameters
        def reserved = ignoredCommands()
        item.supportedCommands.each { comm ->
            try {
                def comname = comm?.getName()
                def args = comm?.getArguments()
                def arglen = args?.size()
                logger("Command for ${swid} = $comname with $arglen args = $args ", "trace")
                if ( arglen==0 && !reserved.contains(comname) ) {
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

    state.powervals = [test: "test"]
    def resp = []
    def run = 1
    run = logStepAndIncrement(run)
    resp = getSwitches(resp)
    run = logStepAndIncrement(run)
    resp = getDimmers(resp)
    run = logStepAndIncrement(run)
    resp = getMomentaries(resp)
    run = logStepAndIncrement(run)
    resp = getButtons(resp)
    // run = logStepAndIncrement(run)
    // resp = getLights(resp)
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
    resp = getAudios(resp)
    run = logStepAndIncrement(run)
    resp = getSmokes(resp)
    run = logStepAndIncrement(run)
    resp = getModes(resp)
    if ( isST() ) {
        run = logStepAndIncrement(run)
        resp = getSHMStates(resp)
        // run = logStepAndIncrement(run)
        // resp = getRoutines(resp)
    }
    if ( isHubitat() ) {
        run = logStepAndIncrement(run)
        resp = getHSMStates(resp)
    }
    run = logStepAndIncrement(run)
    resp = getOthers(resp)
    run = logStepAndIncrement(run)
    resp = getActuators(resp)
    run = logStepAndIncrement(run)
    resp = getPowers(resp)

    // optionally include pistons based on user option
    if (state.usepistons) {
        run = logStepAndIncrement(run)
        resp = getPistons(resp)
    }

    return resp
}

// modified to only return one mode tile
def getModes(resp) {
    logger("Getting mode tile", "info")
    // def vals = ["m1x1","m1x2","m2x1","m2x2"]
    def vals = ["mode"]
    try {
        vals.each {
            def modeid = "${state.prefix}${it}"
            def val = getMyMode(modeid)
            resp << [name: val.name, id: modeid, value: val, type: "mode"]
        }
    } catch (e) {
        log.debug e
    }
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

// def getLights(resp) {
//     getThings(resp, mylights, "light")
// }

def getDimmers(resp) {
    getThings(resp, mydimmers, "switchlevel")
}

def getMotions(resp) {
    getThings(resp, mymotions, "motion")
}

def getContacts(resp) {
    getThings(resp, mycontacts, "contact")
}

def getButtons(resp) {
    getThings(resp, mybuttons, "button")
}

def getActuators(resp) {
    getThings(resp, myactuators, "actuator")
}

def getMomentaries(resp) {
    try {
        mymomentaries?.each {
            if ( it.hasCapability("Switch") || it.hasCommand("push") ) {
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

def getAudios(resp) {
    try {
        myaudios?.each {
            def multivalue = getAudio(it.id, it)
            resp << [name: it.displayName, id: it.id, value: multivalue, type: "audio"]
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
            def multivalue = getDevice(myweathers, it.id, it)
            resp << [name: it.displayName, id: it.id, value: multivalue, type: "weather"]
        }
        myaccuweathers?.each {
            def accuresp = getDevice(myaccuweathers, it.id, it)
            // log.info "AccuWeather tile loaded..."
            // log.info accuresp
            resp << [name: it.displayName, id: it.id, value: accuresp, type: "weather"]
        }
    } catch (e) {}
    return resp
}

// routines are depracated
// get hellohome routines - thanks to ady264 for the tip
// def getRoutines(resp) {
//     try {
//         def routines = location.helloHome?.getPhrases()
//         def n  = routines ? routines.size() : 0
//         if ( n > 0 ) { logger("Number of routines = ${n}","debug"); }
//         routines?.each {
//             def multivalue = getRoutine(it.id, it)
//             resp << [name: it.label, id: it.id, value: multivalue, type: "routine"]
//         }
//     } catch (e) {}
//     return resp
// }

def getOthers(resp) {
    getThings(resp, myothers, "other")
}

def getPowers(resp) {
    try {
        def n  = mypowers ? mypowers.size() : 0
        if ( n > 0 ) { logger("Number of selected power things = ${n}","debug"); }
        mypowers?.each {
            def multivalue = getPower(it.id, it)
            resp << [name: it.displayName, id: it.id, value: multivalue, type: "power"]
        }
    } catch (e) {
        log.error e
    }
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
    // else if ( mylights?.find {it.id == swid } ) { swtype= "light" }
    else if ( mybulbs?.find {it.id == swid } ) { swtype= "bulb" }
    else if ( myswitches?.find {it.id == swid } ) { swtype= "switch" }
    else if ( mybuttons?.find {it.id == swid } ) { swtype= "button" }
    else if ( mylocks?.find {it.id == swid } ) { swtype= "lock" }
    else if ( mymusics?.find {it.id == swid } ) { swtype= "music" }
    else if ( myaudios?.find {it.id == swid } ) { swtype= "audio" }
    else if ( mythermostats?.find {it.id == swid} ) { swtype = "thermostat" }
    else if ( mypresences?.find {it.id == swid } ) { swtype= "presence" }
    else if ( myweathers?.find {it.id == swid } ) { swtype= "weather" }
    else if ( myaccuweathers?.find {it.id == swid } ) { swtype= "weather" }
    else if ( mymotions?.find {it.id == swid } ) { swtype= "motion" }
    else if ( mydoors?.find {it.id == swid } ) { swtype= "door" }
    else if ( mycontacts?.find {it.id == swid } ) { swtype= "contact" }
    else if ( mywaters?.find {it.id == swid } ) { swtype= "water" }
    else if ( myvalves?.find {it.id == swid } ) { swtype= "valve" }
    else if ( myilluminances?.find {it.id == swid } ) { swtype= "illuminance" }
    else if ( mysmokes?.find {it.id == swid } ) { swtype= "smoke" }
    else if ( mytemperatures?.find {it.id == swid } ) { swtype= "temperature" }
    else if ( mypowers?.find {it.id == swid } ) { swtype= "power" }
    else if ( myothers?.find {it.id == swid } ) { swtype= "other" }
    else if ( myactuators?.find {it.id == swid } ) { swtype= "actuator" }
    else if ( swid=="${state.prefix}shm" ) { swtype= "shm" }
    else if ( swid=="${state.prefix}hsm" ) { swtype= "hsm" }
    else if ( swid=="${state.prefix}m1x1" || swid=="${state.prefix}m1x2" || swid=="${state.prefix}m2x1" || swid=="${state.prefix}m2x2" || swid=="${state.prefix}mode" ) { swtype= "mode" }
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
    } else if ( swid=="" || swid==null || swid==false ) {
        return false
    }

    switch (swtype) {
      case "audio" :
        cmdresult = setAudio(swid, cmd, swattr, subid)
        break
        
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

    // routines are depracated
    //   case "routine" :
    //     cmdresult = setRoutine(swid, cmd, swattr, subid)
    //     break
        
      case "water" :
        cmdresult = setWater(swid, cmd, swattr, subid)
        break

      case "button":
        cmdresult = setButton(swid, cmd, swattr, subid)
        break

      case "actuator":
        def item = myactuators.find {it.id == swid }
        cmdresult = setOther(swid, cmd, swattr, subid, item)
        break

      case "other" :
        cmdresult = setOther(swid, cmd, swattr, subid)
        break
        
      case "power":
        cmdresult = getPower(swid)
        break

      case "weather":
        def item = myweathers.find {it.id == swid }
        if ( cmd && item?.hasCommand(cmd) ) {
            item."$cmd"()
        }
        cmdresult = getWeather(swid)

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
    } else if ( swid=="" || swid==null || swid==false ) {
        return false
    }

    switch(swtype) {

    // special case to return an array of all things
    // each case below also now includes multi-item options for the API
    case "all" :
        cmdresult = getAllThings()
        break

    case "actuator" :
        cmdresult = getActuator(swid)
        break

    case "audio" :
        cmdresult = getAudio(swid)
        break
         
    case "button" :
    	cmdresult = getButton(swid)
        break
        
    case "switch" :
        cmdresult = getSwitch(swid)
        break
         
    case "bulb" :
        cmdresult = getBulb(swid)
        break
         
    case "light" :
        cmdresult = getLight(swid)
        break
         
    case "switchlevel" :
        cmdresult = getDimmer(swid)
        break
         
    case "momentary" :
        cmdresult = getMomentary(swid)
        break
        
    case "motion" :
        cmdresult = getMotion(swid)
        break
        
    case "contact" :
        cmdresult = getContact(swid)
        break
      
    case "lock" :
        cmdresult = getLock(swid)
        break
         
    case "thermostat" :
        cmdresult = getThermostat(swid)
        break
         
    case "music" :
        cmdresult = getMusic(swid)
        break
        
    case "presence" :
        cmdresult = getPresence(swid)
        break
         
    case "water" :
        cmdresult = getWater(swid)
        break
         
    case "valve" :
        cmdresult = getValve(swid)
        break
        
    case "door" :
        cmdresult = getDoor(swid)
        break
        
    case "illuminance" :
        cmdresult = getIlluminance(swid)
        break
        
    case "smoke" :
        cmdresult = getSmoke(swid)
        break
        
    case "temperature" :
        cmdresult = getTemperature(swid)
        break
        
    case "weather" :
        cmdresult = getWeather(swid)
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
        
    // case "routine" :
    //     cmdresult = getRoutine(swid)
    //     break

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
    } else if (item) {
        resp = getMotion(item)
    }
    return resp
}

// replaced this code to treat bulbs as Hue lights with color controls
def setBulb(swid, cmd, swattr, subid) {
    def resp = setGenericLight(mybulbs, swid, cmd, swattr, subid)
    return resp
}

// treat just like bulbs - note: light types are deprecated
// def setLight(swid, cmd, swattr, subid) {
//     def resp = setGenericLight(mylights, swid, cmd, swattr, subid)
//     return resp
// }

def setButton(swid, cmd, swattr, subid) {
    def item  = mybuttons.find {it.id == swid }
    def resp = false
    if ( item ) {
        if (cmd) {
            resp  = [button:  cmd]

            // emulate event callback
            postHub(state.directIP, state.directPort, "update", item.displayName, swid, "button", "button", cmd)
            if (state.directIP2) {
                postHub(state.directIP2, state.directPort2, "update", item.displayName, swid, "button", "button", cmd)
            }
        }

        // if trigger was not button invoke it too
        // if ( subid!="button") {
        //     resp[subid] = cmd;            
        //     postHub(state.directIP, state.directPort, "update", item.displayName, swid, subid, "button", cmd)
        //     if (state.directIP2) {
        //         postHub(state.directIP2, state.directPort2, "update", item.displayName, swid, subid, "button", cmd)
        //     }
        // }
    }
    return resp
}

// other types have actions starting with _ 
// and we accommodate switches and api calls with valid cmd values
def setOther(swid, cmd, swattr, subid, item=null ) {
    def resp = false
    def newsw
    item  = item ? item : myothers.find {it.id == swid }
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
        else if ( lightflags.contains(subid) && item.hasAttribute("switch") ) {
            resp = setGenericLight(myothers, swid, cmd, swattr, subid)
        }
        else if ( subid=="level" && cmd.isNumber()  && item.hasAttribute("level") ) {
            newsw = cmd.toInteger()
            newsw = (newsw > 100) ? 100 : newsw
            newsw = (newsw < 0) ? 0 : newsw
            item.setLevel(newsw)
            resp = [:]
            resp.put("level", newsw)

            if ( item.hasAttribute("hue") && item.hasAttribute("saturation") ) {
                def h = item.currentValue("hue").toInteger()
                def s = item.currentValue("saturation").toInteger()
                newcolor = hsv2rgb(h, s, newsw)
                resp.put("color", newcolor)
            }

        }
        else if ( item.hasCommand(cmd) ) {
            item."$cmd"()
            resp = getOther(swid, item)

        } else if ( item.hasAttribute("button") && cmd ) {
            resp = [button: cmd]
            // emulate event callback
            postHub(state.directIP, state.directPort, "update", item.displayName, item.id, "button", "button", cmd)
            if (state.directIP2) {
                postHub(state.directIP2, state.directPort2, "update", item.displayName, item.id, "button", "button", cmd)
            }
            // if trigger was not button invoke it too
            // if ( subid!="button") {
            //     resp[subid] = cmd;            
            //     postHub(state.directIP, state.directPort, "update", item.displayName, swid, subid, "button", cmd)
            //     if (state.directIP2) {
            //         postHub(state.directIP2, state.directPort2, "update", item.displayName, swid, subid, "button", cmd)
            //     }
            // }

        } else {
            resp = getOther(swid, item)
        }
    }
    return resp
}

// control audio devices
// much like music but the buttons are real commands
def setAudio(swid, cmd, swattr, subid) {
    def resp = false
    def item  = myaudios.find {it.id == swid }
    
    if ( item ) {
        logcaller(item.getDisplayName(), swid, cmd, swattr, subid)
        resp = getAudio(swid, item)

        if ( (subid=="_mute" || subid=="mute") && swattr.contains(" unmuted" ) ) {
            item.mute()
            resp["mute"] = "muted"
        } else if ( (subid=="_unmute" || subid=="mute") && swattr.contains(" muted" ) ) {
            item.unmute()
            resp["mute"] = "unmuted"

        // handle volume and group volume up specially because their cmd ops don't work
        // down odly enough works but I put it here too just to make them consistent
        // note that this workaround only changes this item not the whole group
        } else if ( subid=="_groupVolumeUp" || subid=="_volumeUp" ) {
            def grpvol = item.currentValue("volume")
            grpvol = (grpvol > 95) ? 100 : grpvol + 5
            item.setVolume(grpvol)
            resp["volume"] = grpvol

        } else if ( subid=="_groupVolumeDown" || subid=="_volumeDown" ) {
            def grpvol = item.currentValue("volume")
            grpvol = (grpvol < 5) ? 0 : grpvol - 5
            item.setVolume(grpvol)
            resp["volume"] = grpvol

        } else if ( subid=="volume" ) {
            def newvol = item.currentValue("volume")
            try {
                newvol = cmd.toInteger()
            } catch (e) {
            }
            item.setVolume(newvol)
            resp["volume"] = newvol
        } else if (subid.startsWith("_")) {
            subid = subid.substring(1)
            if ( item.hasCommand(subid) ) {
                item."$subid"()
                resp = getAudio(swid, item)
                resp = getAudio(swid, item)
            }
        }
        else if ( item.hasCommand(cmd) ) {
            item."$cmd"()
            resp = getAudio(swid, item)
            resp = getAudio(swid, item)
        }
    }
    return resp
}

// handle water devices
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
    resp =  [ themode: newsw ];
    
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

// def hsmStatusHandler(evt) {
//     log.info "HSM state set to ${evt.value}" + (evt.value=="rule" ? $evt.descriptionText : "" )
// }
// def hsmAlertHandler(evt) {
//     log.info "HSM alert: ${evt.value}"
// }

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
            } else if (cmd=="toggle") {
                newonoff = newonoff=="off" ? "on" : "off"
            } else if (subid.startsWith("_")) {
                cmd = subid.substring(1)
                if ( item.hasCommand(cmd) ) {
                    item."$cmd"()
                    newonoff = item.currentValue("switch")
                }
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
        if ( item.hasCommand(newonoff) && !subid?.startsWith("_") ) {
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
    def item  = mymomentaries?.find {it.id == swid }
    if ( item ) {
        if ( (subid=="momentary" || subid=="_push") && item.hasCommand("push") ) {
            item.push()
        } else if ( subid=="switch" && item.hasCommand(cmd) ) {
            item."$cmd"()
        }
        resp = getMomentary(swid, item)
    }
    return resp
}

def setLock(swid, cmd, swattr, subid) {
    logcaller("setLock", swid, cmd, swattr, subid)
    def resp = false
    def newsw
    def item  = mylocks?.find {it.id == swid }
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
        if ( subid=="mute" && swattr.contains(" unmuted" )) {
            newsw = "muted"
            item.mute()
            resp['mute'] = newsw
        } else if ( subid=="mute" && swattr.contains(" muted" )) {
            newsw = "unmuted"
            item.unmute()
            resp['mute'] = newsw
        } else if ( subid=="level-up" || swattr.contains(" level-up") ) {
            newsw = cmd.toInteger()
            newsw = (newsw >= 95) ? 100 : newsw - (newsw % 5) + 5
            item.setLevel(newsw)
            resp['level'] = newsw
        } else if ( subid=="level-dn" || swattr.contains(" level-dn") ) {
            newsw = cmd.toInteger()
            def del = (newsw % 5) == 0 ? 5 : newsw % 5
            newsw = (newsw <= 5) ? 0 : newsw - del
            item.setLevel(newsw)
            resp['level'] = newsw
        } else if ( subid=="level" || swattr.contains(" level") ) {
            newsw = cmd.toInteger()
            item.setLevel(newsw)
            resp['level'] = newsw
        } else if ( subid=="music-play" || swattr.contains(" music-play") ) {
            newsw = "playing"
            item.play()
            resp['status'] = newsw
            // resp['trackDescription'] = item.currentValue("trackDescription")
        } else if ( subid=="music-stop" || swattr.contains(" music-stop") ) {
            newsw = "stopped"
            item.stop()
            resp['status'] = newsw
            // resp['trackDescription'] = ""
        } else if ( subid=="music-pause" || swattr.contains(" music-pause") ) {
            newsw = "paused"
            item.pause()
            resp['status'] = newsw
        } else if ( subid=="music-previous" || swattr.contains(" music-previous") ) {
            item.previousTrack()
            resp = getMusic(swid, item)
            // resp['trackDescription'] = item.currentValue("trackDescription")
        } else if ( subid=="music-next" || swattr.contains(" music-next") ) {
            item.nextTrack()
            resp = getMusic(swid, item)
            // resp['trackDescription'] = item.currentValue("trackDescription")
        } else if ( subid.startsWith("_") ) {
            subid = subid.substring(1)
            if ( item.hasCommand(subid) ) {
                item."$subid"()
                resp = getMusic(swid, item)
                // resp['trackDescription'] = item.currentValue("trackDescription")
            }
        } else if ( cmd && item.hasCommand(cmd) ) {
            item."$cmd"()
            resp = getMusic(swid, item)
            // resp['trackDescription'] = item.currentValue("trackDescription")
        }
    }
    return resp
}

// routines are depracated
// def setRoutine(swid, cmd, swattr, subid) {
//     logcaller("setRoutine", swid, cmd, swattr, subid)
//     def routine = location.helloHome?.getPhrases().find{ it.id == swid }
//     if (subid=="label" && routine) {
//         try {
//             location.helloHome?.execute(routine.label)
//         } catch (e) {}
//     } else if (cmd) {
//         try {
//             location.helloHome?.execute(cmd)
//         } catch (e) {}
//     }
//     return routine
// }

def registerAll() {
    List mydevices = ["myswitches", "mydimmers", "mybulbs", "mypresences", "mybuttons",
                      "mymotions", "mycontacts", "mydoors", "mylocks", "mythermostats",
                      "mytemperatures", "myilluminances", "myweathers", "myaccuweathers",
                      "mywaters", "mysmokes", "mymusics", "myaudios", "mypowers", "myothers", "myactuators"]

    // register mode changes
    registerLocations()

    // register all the devices in time steps
    def delay = 5
    mydevices.each { item -> 
        if ( settings[item]?.size() > 0 ) {
            logger("registering ${item} ", "info")
            runIn(delay, "register_${item}", [overwrite: true])
            delay = delay + 5
        }
    }
}

def registerLocations() {
    // lets subscribe to mode changes
    subscribe(location, "mode", modeChangeHandler)

    // TODO 
    // if ( isHubitat() ) {
    //     subscribe(location, "hsmStatus", hsmStatusHandler)
    //     subscribe(location, "hsmAlerts", hsmAlertHandler)
    // }
}

def register_myswitches() {
    registerChangeHandler(settings?.myswitches)
}
def register_mydimmers() {
    registerChangeHandler(settings?.mydimmers)
}
def register_mybulbs() {
    registerChangeHandler(settings?.mybulbs)
}
def register_mypresences() {
    registerChangeHandler(settings?.mypresences)
}
def register_mymotions() {
    registerChangeHandler(settings?.mymotions)
}
def register_mycontacts() {
    registerChangeHandler(settings?.mycontacts)
}
def register_mydoors() {
    registerChangeHandler(settings?.mydoors)
}
def register_mylocks() {
    registerChangeHandler(settings?.mylocks)
}
def register_mythermostats() {
    registerChangeHandler(settings?.mythermostats)
}
def register_mytemperatures() {
    registerChangeHandler(settings?.mytemperatures)
}
def register_myilluminances() {
    registerChangeHandler(settings?.myilluminances)
}
def register_myweathers() {
    registerChangeHandler(settings?.myweathers)
}
def register_mywaters() {
    registerChangeHandler(settings?.mywaters)
}
def register_mysmokes() {
    registerChangeHandler(settings?.mysmokes)
}
def register_mymusics() {
    registerChangeHandler(settings?.mymusics)
}
def register_myaudios() {
    registerChangeHandler(settings?.myaudios)
}
def register_mypowers() {
    registerChangeHandler(settings?.mypowers)
}
def register_myothers() {
    registerChangeHandler(settings?.myothers)
}
def register_myactuators() {
    registerChangeHandler(settings?.myactuators)
}
def register_myaccuweathers() {
    registerChangeHandler(settings?.myaccuweathers)
}
def register_mybuttons() {
    registerChangeHandler(settings?.mybuttons)
}

def registerCapabilities(devices, capability) {
    subscribe(devices, capability, changeHandler)
    logger("Registering ${capability} for ${devices?.size() ?: 0} things", "info")
}

def registerChangeHandler(devices) {
    devices?.each { device ->
        List theAtts = device?.supportedAttributes?.collect { it?.name as String }?.unique()
        logger("atts: ${theAtts}", "debug")
        theAtts?.each {att ->
            Boolean skipAtt = false
            if(!(ignoredAttributes().contains(att))) {
                subscribe(device, att, "changeHandler")
                logger("Registering ${device?.displayName}.${att}", "info")
            }
        }
    }
}

def changeHandler(evt) {
    def src = evt?.source
    def deviceid = evt?.deviceId
    def deviceName = evt?.displayName
    def attr = evt?.name
    def value = evt?.value
    def skip = false
    
    def devtype = autoType(deviceid)
    logger("handling id = ${deviceid} type = ${devtype}", "debug")

    // handle power changes to skip if not changed by at least 15%
    // this value was set by trial and error for my particular plug
    if ( attr=="power" ) {
        try {
            // log.info state.powervals
            def delta = 0.0
            def oldpower = state.powervals[deviceid] ?: 0.0
            oldpower = Float.valueOf(oldpower)
            state.powervals[deviceid] = Float.valueOf(value)
            if ( oldpower==0.0 && state.powervals[deviceid] < 1.0 ) {
                skip = true
            } else if ( oldpower==0.0 ) {
                skip = false
            } else {
                delta = (state.powervals[deviceid]- oldpower) / oldpower 
                if ( delta < 0.0 ) {
                    delta = 0.0 - delta
                }
                skip = (delta < 0.15)
            }
            logger("delta = ${delta} skip = ${skip}", "debug")
            
        } catch (e) {
            skip= false
            logger("problem in change handler for power device. oldpower: ${oldpower} error msg: ${e}", "error")
        }
    }

    
    // log.info "Sending ${src} Event ( ${deviceName}, ${deviceid}, ${attr}, ${value} ) to HousePanel clients  log = ${state.loggingLevelIDE}"
    if ( !skip && state?.directIP && state?.directPort && deviceName && deviceid && attr && value) {

        // fix color bulbs - force attr to color if hue, saturation, or level changes
        def item = mybulbs?.find{it.id == deviceid}
        if ( item && (attr=="hue" || attr=="saturation" || attr=="level") && item.hasAttribute("color") ) {

            def h = attr=="hue" ? value.toInteger() : item.currentValue("hue").toInteger()
            def s = attr=="saturation" ? value.toInteger() : item.currentValue("saturation").toInteger()
            def v = attr=="level" ? value.toInteger() : item.currentValue("level").toInteger()
            def color = hsv2rgb(h, s, v)

            // make the original attribute change
            postHub(state.directIP, state.directPort, "update", deviceName, deviceid, attr, "bulb", value)
            if (state.directIP2) {
                postHub(state.directIP2, state.directPort2, "update", deviceName, deviceid, attr, "bulb", value)
            }

            // set it to change color based on attribute change
            logger("color of device ${deviceName} changed to ${color} by the ${attr} attribute changing to ${value}", "debug")
            attr = "color"
            value = color
        }
        postHub(state.directIP, state.directPort, "update", deviceName, deviceid, attr, devtype, value)
        if (state.directIP2) {
            postHub(state.directIP2, state.directPort2, "update", deviceName, deviceid, attr, devtype, value)
        }
    }
}

def modeChangeHandler(evt) {
    // modified to simplify modes to only deal with one tile
    // send group of hub actions for mode changes
    def themode = evt?.value
    def deviceName = evt?.displayName
    def attr = evt?.name
    logger("New mode= ${themode} with attr= ${attr} and name= ${deviceName} to HousePanel clients", "debug")
    if (themode && deviceName && state?.directIP && state?.directPort) {
        def modeid = "${state.prefix}mode"
        logger("Sending new mode= ${themode} with id= ${modeid} to HousePanel clients", "info")
        postHub(state.directIP, state.directPort, "update", deviceName, modeid, "themode", "mode", themode)
        if (state.directIP2) {
            postHub(state.directIP2, state.directPort2, "update", deviceName, modeid, "themode", "mode", themode)
        }
    }
}

def postHub(ip, port, msgtype, name, id, attr, type, value) {

    
    if ( msgtype && ip!="0" & ip!=0 && port!="0" & port!=0 ) {
        logger("HousePanel postHub ${msgtype} to IP= ${ip}:${port} name= ${name} id= ${id} attr= ${attr} type= ${type} value= ${value}", "info")

        // set a hub action - include the access token so we know which hub this is
        def params = [
            method: "POST",
            path: "/",
            headers: [
                HOST: "${ip}:${port}",
                'Content-Type': 'application/json'
            ],
            body: [
                msgtype: msgtype,
                hubid: state.hubid,
                change_name: name,
                change_device: id,
                change_attribute: attr,
                change_type: type,
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

// Wrapper function for all logging.
private logcaller(caller, swid, cmd, swattr, subid, level="debug") {
    logger("${caller}: swid= $swid cmd= $cmd swattr= $swattr subid= $subid", level)
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
