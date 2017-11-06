/**
*  NHL Goal Notifications
*
*  Copyright 2017 Eric Luttmann
*
*  Description:
*  Control lights, buttons, switches, sirens, and/or play your teams goal scoring horn when your 
*  NHL team scores a goal.  In addition, you can get text messages and/or push notifications for 
*  status updates on game day... like notifications of game day, pregame start, game in process, score
*  updates, and final scores.
*
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

include 'asynchttp_v1'
import groovy.json.JsonSlurper
import groovy.json.JsonOutput

definition(
    name: "NHL Game Notifications",
    namespace: "ejluttmann",
    author: "Eric Luttmann",
    description: "Get game notifications for any NHL team.",
    category: "My Apps",
    iconUrl: "https://cloud.githubusercontent.com/assets/2913371/22167524/3390bf68-df24-11e6-94c6-b099063842df.png",
    iconX2Url: "https://cloud.githubusercontent.com/assets/2913371/22167524/3390bf68-df24-11e6-94c6-b099063842df.png",
    iconX3Url: "https://cloud.githubusercontent.com/assets/2913371/22167524/3390bf68-df24-11e6-94c6-b099063842df.png")


preferences {

    page(name: "startPage", title: "NHL Game Notifications", install: true, uninstall: true) {
        section() {
            input "nhlTeam", "enum", title: "Select NHL Team", required: true, displayDuringSetup: true, options: getTeamEnums()

            href(name: "goals",
                 title:"Goal Scoring", description:"Tap to setup goal scoring",
                 required: false,
                 page: "goalsPage")

            href(name: "notify",
                 title:"Game Notifications", description:"Tap to setup game notifications",
                 required: false,
                 page: "notifyPage")

            href(name: "game",
                 title:"Game Actions", description:"Tap to setup game state actions",
                 required: false,
                 page: "gamePage")

            //            href(name: "debug",
            //                 title:"Debug", description:"Tap to setup debugging options",
            //                 required: false,
            //                 page: "debugPage")
        }

        section([mobileOnly:true]) {
            label title: "Assign a name", required: false
        }

        section("About") {
            paragraph versionParagraph()
        }        
    }

    page(name: "goalsPage") {
        section("Momentary Buttons (ie. Doorbell, Alarm)"){
            input "buttonGoals", "capability.momentary", title: "Devices Selection", required: false, multiple: true, displayDuringSetup: true
            input "buttonDelay", "number", title: "Delay after goal (in seconds)", description: "1-120 seconds", required: false, range: "1..120"
        }

        section("Turn On/Off Lights"){
            input "switchLights", "capability.switch", title: "Select Lights", required: false, multiple: true, displayDuringSetup: true
            input "switchOnFor", "number", title: "Turn Off After", description: "1-120 seconds", required: false, multiple: false, displayDuringSetup: true, range: "1..120"
            input "switchDelay", "number", title: "Delay after goal (in seconds)", description: "1-120 seconds", required: false, range: "1..120"
        }

        section("Flashing Lights"){
            input "flashLights", "capability.switch", title: "Select Lights", multiple: true, required: false
            input "numFlashes", "number", title: "Number Of Times To Flash", description: "1-50 times", required: false, range: "1..50"
            input "flashOnFor", "number", title: "On For (default 1000ms)", description: "milliseconds", required: false
            input "flashOffFor", "number", title: "Off For (default 1000ms)", description: "milliseconds", required: false
            input "flashDelay", "number", title: "Delay After Goal (in seconds)", description: "1-120 seconds", required: false, range: "1..120"
            input "lightColor", "enum", title: "Flashing Light Color?", required: false, multiple:false, options: ["White", "Red","Green","Blue","Yellow","Orange","Purple","Pink"]
            input "lightLevel", "enum", title: "Flashing Light Level?", required: false, options: [[10:"10%"],[20:"20%"],[30:"30%"],[40:"40%"],[50:"50%"],[60:"60%"],[70:"70%"],[80:"80%"],[90:"90%"],[100:"100%"]]
        }

        section("Sirens To Trigger"){
            input "sirens", "capability.alarm", title: "Sirens Selection", required: false, multiple: true
            input "sirensOnly", "bool", title: "Don't Use The Strobe", defaultValue: "false", displayDuringSetup: true, required:false
            input "sirensOnFor", "number", title: "Turn Off After", description: "1-10 seconds", required: false, multiple: false, displayDuringSetup: true, range: "1..10"
            input "sirenDelay", "number", title: "Delay After Goal (in seconds)", description: "1-120 seconds", required: false, range: "1..120"
        }

        section ("Speaker Used To Play Goal Scoring Horn"){
            input "sound", "capability.musicPlayer", title: "Speaker Selection", required: false, displayDuringSetup: true
            input "volume", "number", title: "Speaker Volume", description: "1-100%", required: false, range: "1..100"
            input "soundDuration", "number", title: "Duration To Play (in seconds)", description: "1-120 seconds", required: false, range: "1..120"
            input "soundDelay", "number", title: "Delay After Goal (in seconds)", description: "1-120 seconds", required: false, range: "1..120"
        }
    }

    page(name: "notifyPage") {
        section( "Enable Notifications" ) {
            input "sendGoalMessage", "bool", title: "Enable Goal Score Notifications?", defaultValue: "true", displayDuringSetup: true, required:false
            input "sendGameDayMessage", "bool", title: "Enable Game Day Status Notifications?", defaultValue: "false", displayDuringSetup: true, required:false
            input "notificationSwitch", "capability.switch", title: "Use Switch To Enable/Disable Goal Notifications", required: false, multiple: false, displayDuringSetup: true
            input "manualGoalTrigger", "capability.button", title: "Manual Goal Trigger", required: false, multiple: false, displayDuringSetup: true
            input "goalDelay", "number", title: "Notification Delay After Goal (in seconds)", description: "1-120 seconds", required: false, range: "1..120"
        }

        section( "Push And Text Notifications" ) {
            input "sendPushMessage", "bool", title: "Send Push Notifications?", defaultValue: "false", displayDuringSetup: true, required:false
            input "sendPhoneMessage", "phone", title: "Send Phone Texts?", description: "phone number", required: false
        }

        section( "Number Of Hours Prior To Game Before Sending Status Notifications" ) {
            input "hoursBeforeStart", "number", title: "Hours Before Game Start", description: "0-12 hours", required: false, multiple: false, displayDuringSetup: true, range: "0..12"
        }
    }

    page(name: "gamePage") {
        section("Turn On At Start Of Game"){
            input "gameSwitches", "capability.switch", title: "Select Switches", required: false, multiple: true, displayDuringSetup: true
            input "gameSwitchOff", "bool", title: "Turn Off After Game?", defaultValue: "true", displayDuringSetup: true, required:false
        }
    }

    page(name: "debugPage", title: "Name app and configure modes", install: true, uninstall: true) {
        section("Debug") {
            input "debugCheckDate", "date", title: "Override game day check date", description: "yyyy-mm-dd", displayDuringSetup: true, required:false
        }
    }
}

private getTeamEnums() {
    return ["Devils", "Islanders", "Rangers", "Flyers", "Penguins", "Bruins", "Sabres", "Canadiens", "Senators", "Maple Leafs", "Hurricanes", "Panthers", "Lightning", "Capitals", "Blackhawks", "Red Wings", "Predators", "Blues", "Flames", "Avalanche", "Oilers", "Canucks", "Ducks", "Stars", "Kings", "Sharks", "Blue Jackets", "Wild", "Jets", "Coyotes"]
}

def installed() {
    log.debug "Installed with settings: ${settings}"

    initialize()
}

def updated() {
    log.debug "Updated with settings: ${settings}"

    unschedule()
    unsubscribe()
    initialize()
}

def initialize() {
    log.info "NHL Game Notifications. Version ${version()}"

    state.NHL_API_URL = "http://statsapi.web.nhl.com/api/v1"
    state.HORN_URL = "http://wejustscored.com/audio/"

    state.GAME_STATUS_SCHEDULED            = '1'
    state.GAME_STATUS_PREGAME              = '2'
    state.GAME_STATUS_IN_PROGRESS          = '3'
    state.GAME_STATUS_IN_PROGRESS_CRITICAL = '4'
    state.GAME_STATUS_UNKNOWN              = '5'
    state.GAME_STATUS_FINAL6               = '6'
    state.GAME_STATUS_FINAL7               = '7'

    state.enableGameNotifications = true

    state.previous = [:]
    state.capabilities = [:]

    switchLights.each {
        if (it.hasCapability("Color Control")) {
            state.capabilities[it.id] = "color"
        }
        else if (it.hasCapability("Switch Level")) {
            state.capabilities[it.id] = "level"
        }
        else {
            state.capabilities[it.id] = "switch"
        }
    }

    flashLights.each {
        if (it.hasCapability("Color Control")) {
            state.capabilities[it.id] = "color"
        }
        else if (it.hasCapability("Switch Level")) {
            state.capabilities[it.id] = "level"
        }
        else {
            state.capabilities[it.id] = "switch"
        }
    }

    // schedule to run every day at 1am
    def start = timeToday("1:00AM", location.timeZone)
    def startTime = start.format('h:mm a',location.timeZone)
    log.debug "Scheduling once per day at ${startTime}"

    // setup schedule
    schedule(start, setupForGameDay)

    // setup subscriptions
    subscribe(manualGoalTrigger, "button.pushed", manualGoalHandler)
    subscribe(notificationSwitch, "switch", notificationSwitchHandler)

    if (notificationSwitch && notificationSwitch.currentSwitch == "off") {
        state.enableGameNotifications = false
    }

    // and run now to check for today
    setupForGameDay()
}

def setupForGameDay() {
    state.teamScore = 0
    state.opponentScore = 0
    state.Team = null
    state.Game = null
    state.currentGameStatus = state.GAME_STATUS_UNKNOWN
    state.notifiedGameStatus = state.GAME_STATUS_UNKNOWN
    state.gameDate = null
    state.gameTime = null
    state.gameStations = null
    state.gameLocation = null
    state.gameStarted = false

    getTeam()
}

def manualGoalHandler(evt) {
    try {
        if (state.enableGameNotifications) {
            teamGoalScored()
        } else {
            log.debug "Game Notifications has been disabled, ignore manual goal"
        }
    } catch (e) {
        log.error "something went wrong: $e"
    }
}

def notificationSwitchHandler(evt) {
    try {
        if (evt.value == "on") {
            log.debug "Re-enabling Game Notifications"
            state.enableGameNotifications = true
            setupForGameDay()
        } else if (evt.value == "off") {
            log.debug "Disabling game notifications"
            state.enableGameNotifications = false
        }
    } catch (e) {
        log.error "something went wrong: $e"
    }
}

def getTeamHandler(resp, data) {
    def found = false
    def jsonData = resp.getData()

    if (resp.status == 200) {
        def slurper = new groovy.json.JsonSlurper()
        def result = slurper.parseText(jsonData)

        def teams = null
        for (rec in result.teams) {
            if (teams) {
                teams = teams + ", \"${rec.teamName}\""
            } else {
                teams = "[\"${rec.teamName}\""
            }
        } 
        teams = teams + "]"

        log.debug "Teams: ${teams}"

        for (rec in result.teams) {
            if (settings.nhlTeam == rec.teamName) {
                state.Team = rec
                log.debug "Found info on team ${state.Team.teamName}, id=${state.Team.id}"
                found = true
                break
            }
        } 

    }
    else {
        log.debug "Request Failed To Return Data"
    }

    if (found == false) {
        log.debug "Unable to locate info on team ${settings.nhlTeam}, trying again in 30 seconds..."

        def now = new Date()
        def runTime = new Date(now.getTime() + (30 * 1000))
        runOnce(runTime, getTeam)
    }
    else {
        // now check if it is a game day
        checkIfGameDay()
    }
}

def getTeam() {
    log.debug "Setup for team ${settings.nhlTeam}"
    try {
        def params = [uri: "${state.NHL_API_URL}/teams"] 
        asynchttp_v1.get(getTeamHandler, params)
    } catch (e) {
        log.error "something went wrong: $e"
    }
}

def checkIfGameDayHandler(resp, data) {
    def now = new Date()
    def nextRunTime = new Date(now.getTime() + (30 * 1000))

    if (resp.status == 200) {
        def slurper = new groovy.json.JsonSlurper()
        def result = slurper.parseText(resp.getData())
        def isGameDay = false

        for (date in result.dates) {
            for (game in date.games)
            {
                isGameDay = true

                log.debug "A game is scheduled for today - ${game.teams.away.team.name} vs ${game.teams.home.team.name}"

                // set current game info
                state.Game = game

                // set game day location and date info
                state.gameDate = Date.parse("yyyy-MM-dd'T'HH:mm:ss'Z'", game.gameDate)
                state.gameTime = state.gameDate.format("h:mm a", location.timeZone)
                state.gameStations = getBroadcastStations(game)
                state.gameLocation = getLocation(game)

                log.debug "Game status = ${game.status.statusCode}"

                switch (game.status.statusCode) {
                    case state.GAME_STATUS_SCHEDULED:
                    // schedule game day checks to start 'x' hour(s) prior to game
                    def hoursBefore = settings.hoursBeforeStart ?: 1
                    nextRunTime = new Date(state.gameDate.getTime() - (hoursBefore * ((60 * 60) * 1000)))
                    if (nextRunTime <= now) {
                        nextRunTime = new Date(now.getTime() + (10 * 1000))
                    }
                    break

                    case state.GAME_STATUS_PREGAME:
                    // Currently in pregame, schedule next run time in 30 seconds
                    nextRunTime = new Date(now.getTime() + (30 * 1000))
                    break

                    case state.GAME_STATUS_IN_PROGRESS:
                    case state.GAME_STATUS_IN_PROGRESS_CRITICAL:
                    // Game currently in progress, schedule next run time in 10 seconds
                    nextRunTime = new Date(now.getTime() + (10 * 1000))
                    break

                    case state.GAME_STATUS_FINAL6:
                    case state.GAME_STATUS_FINAL7:
                    // game over, no more game day status checkds required for the day
                    log.debug "Todays game is over - ${game.teams.away.team.name} vs ${game.teams.home.team.name}"
                    isGameDay = false
                    break

                    case state.GAME_STATUS_UNKNOWN:
                    default:
                        // Check again in 15 seconds if game day status is unknown
                        nextRunTime = new Date(now.getTime() + (15 * 1000))
                    break
                }

                // break out of loop
                break
            }
        }

        if (isGameDay) {
            def runTimeDate = nextRunTime.format('yyyy-MM-dd h:mm:ss a',location.timeZone)
            log.debug "Checking game status again at ${runTimeDate}."

            runOnce(nextRunTime, checkGameStatus)
        } else {
            log.debug "Not a game day, no more checks for today."
        }

    } else {
        log.debug "Request Failed!"
        log.debug "Response: $resp.errorData"

        def runTimeDate = nextRunTime.format('yyyy-MM-dd h:mm:ss',location.timeZone)
        log.debug "Trying again at ${runTimeDate}..."
        runOnce(nextRunTime, checkIfGameDay)
    }
}

def checkIfGameDay() {
    try {
        if (state.enableGameNotifications == false) {
            log.debug "Game Notifications has been disabled, ignore Game Day checks"
            return
        }

        def todaysDate = new Date().format('yyyy-MM-dd',location.timeZone)
        if (settings.debugCheckDate) {
            todaysDate = settings.debugCheckDate
        }
        def params = [uri: "${state.NHL_API_URL}/schedule?teamId=${state.Team.id}&date=${todaysDate}&expand=schedule.teams,schedule.broadcasts.all"] 

        log.debug "Determine if it is game day for team ${settings.nhlTeam}, requesting game day schedule for ${todaysDate}"
        asynchttp_v1.get(checkIfGameDayHandler, params)
    } catch (e) {
        log.error "something went wrong: $e"
    }
}

def checkGameStatusHandler(resp, data) {

    // check for valid response
    if (resp.status == 200) {
        def slurper = new groovy.json.JsonSlurper()
        def result = slurper.parseText(resp.getData())
        def gamveOver = true
        def runDelay = 30

        for (date in result.dates) {
            for (game in date.games)
            {
                // update game and status
                state.Game = game
                state.currentGameStatus = game.status.statusCode
                gamveOver = false

                log.debug "Current game status = ${state.currentGameStatus}"
                switch (state.currentGameStatus) {
                    case state.GAME_STATUS_SCHEDULED:
                    log.debug "${game.teams.away.team.name} vs ${game.teams.home.team.name}  - scheduled for today!"

                    // delay for 2 minutes before checking game day status again
                    runDelay = (2 * 60)

                    //done
                    break

                    case state.GAME_STATUS_PREGAME:
                    log.debug "${game.teams.away.team.name} vs ${game.teams.home.team.name} - pregame!"

                    // start checking every 30 seconds now that it is pregame status
                    runDelay = 30

                    //done                    
                    break

                    case state.GAME_STATUS_IN_PROGRESS:
                    case state.GAME_STATUS_IN_PROGRESS_CRITICAL:
                    log.debug "${game.teams.away.team.name} vs ${game.teams.home.team.name} - game is on!!!"

                    // check every 5 seconds when game is active, looking for score changes asap
                    runDelay = 5
                    
                    // check for new goal
                    checkForGoal()

                    //done
                    break

                    case state.GAME_STATUS_FINAL6:
                    case state.GAME_STATUS_FINAL7:
                    log.debug "${game.teams.away.team.name} vs ${game.teams.home.team.name} - game is over!"
                    
                    // check for overtime score
                    checkForGoal()

                    // game over, no more game day status checks required for the day
                    gamveOver = true
                    state.gameStarted = false
                    
                    //done
                    break

                    case state.GAME_STATUS_UNKNOWN:
                    default:
                        log.debug "${game.teams.away.team.name} vs ${game.teams.home.team.name} game status is unknown!"

                    // check again in 15 seconds if game day status is unknown
                    runDelay = 15

                    //done
                    break
                }

                if (state.currentGameStatus != state.GAME_STATUS_UNKNOWN && state.notifiedGameStatus != state.currentGameStatus) {
                    runIn(0, triggerStatusNotifications)
                }

                // break out of loop
                break
            }
        }

        if (gamveOver) {
            log.debug "Game is over, no more game status checks required for today."
        } else {
            def now = new Date()
            def runTime = new Date(now.getTime() + (runDelay * 1000))

            log.debug "Checking game status again in ${runDelay} seconds..."
            runOnce(runTime, checkGameStatus)
        }

    } else {
        log.debug "Request Failed!"
        log.debug "Response: $resp.errorData"

        def now = new Date()
        def runTime = new Date(now.getTime() + (15 * 1000))

        log.debug "Trying again in 15 seconds..."
        runOnce(runTime, checkGameStatus)
    }
}

def checkGameStatus() {
    try {

        if (state.enableGameNotifications == false) {
            log.debug "Game Notifications has been disabled, ignore Game Status checks."
            return
        }

        def todaysDate = new Date().format('yyyy-MM-dd',location.timeZone)
        if (settings.debugCheckDate) {
            todaysDate = settings.debugCheckDate
        }
        def params = [uri: "${state.NHL_API_URL}/schedule?teamId=${state.Team.id}&date=${todaysDate}"] 

        log.debug "Requesting ${settings.nhlTeam} game schedule for ${todaysDate}"
        asynchttp_v1.get(checkGameStatusHandler, params)
    } catch (e) {
        log.error "something went wrong: $e"
    }
}

def checkForGoal() {

    def game = state.Game
    if (game) {
        def teamScore = getTeamScore(game.teams)
        def opponentScore = getOpponentScore(game.teams)

        // first time just initialize the scores - this is preventing the issue of sending 
        // goal notifications when app is started in the middle of the game after scores have 
        // occurred.
        if (state.gameStarted == false) {
            log.debug "Game started, initialize scores..."
            state.teamScore = teamScore
            state.opponentScore = opponentScore
        }

        // indicate game has started
        state.gameStarted = true

        // check for change in scores
        def delay = settings.goalDelay ?: 0
        if (teamScore > state.teamScore) {
            log.debug "Change in team score"
            state.teamScore = teamScore
            runIn(delay, teamGoalScored)
        } 
        if (opponentScore > state.opponentScore) {
            log.debug "Change in opponent score"
            state.opponentScore = opponentScore
            runIn(delay, opponentGoalScored)
        } 
    } else {
    	log.debug "No game setup!"
    }
}

def getHornURL(team) {
    def hornURL = null

    try {
        def audio = null

        switch (team.teamName) {
            case "Devils":
            audio = "njd.mp3"
            break

            case "Islanders":
            audio = "nyi.mp3"
            break

            case "Rangers":
            audio = "nyr.mp3"
            break

            case "Flyers":
            audio = "phi.mp3"
            break

            case "Penguins":
            audio = "pit.mp3"
            break

            case "Bruins":
            audio = "bos.mp3"
            break

            case "Sabres":
            audio = "buf.mp3"
            break

            case "Canadiens":
            audio = "mon.mp3"
            break

            case "Senators":
            audio = "ott.mp3"
            break

            case "Maple Leafs":
            audio = "tor.mp3"
            break

            case "Hurricanes":
            audio = "car.mp3"
            break

            case "Panthers":
            audio = "fla.mp3"
            break

            case "Lightning":
            audio = "tbl.mp3"
            break

            case "Capitals":
            audio = "wsh.mp3"
            break

            case "Blackhawks":
            audio = "chi.mp3"
            break

            case "Red Wings":
            audio = "det.mp3"
            break

            case "Predators":
            audio = "nsh.mp3"
            break

            case "Blues":
            audio = "stl.mp3"
            break

            case "Flames":
            audio = "cgy.mp3"
            break

            case "Avalanche":
            audio = "col.mp3"
            break

            case "Oilers":
            audio = "edm.mp3"
            break

            case "Canucks":
            audio = "van.mp3"
            break

            case "Ducks":
            audio = "ana.mp3"
            break

            case "Stars":
            audio = "dal.mp3"
            break

            case "Kings":
            audio = "lak.mp3"
            break

            case "Sharks":
            audio = "sjs.mp3"
            break

            case "Blue Jackets":
            audio = "cbj.mp3"
            break

            case "Wild":
            audio = "min.mp3"
            break

            case "Jets":
            audio = "wpg.mp3"
            break

            case "Coyotes":
            audio = "ari.mp3"
            break

            default:
                break
        }

        if (audio) {
            hornURL = state.HORN_URL + audio
        }

    } catch(ex) {
        log.debug "Error getting Horn URL"
        hornURL = null
    }

    return hornURL
}

def getBroadcastStations(game) {
    def stations = null

    try {
        def broadcasts = game.broadcasts

        if (broadcasts) {
            for (station in broadcasts) {
                if (station.name) {
                    if (stations == null) {
                        stations = station.name
                    } else {
                        stations = stations + ", " + station.name
                    }
                }
            } 
        }
    } catch(ex) {
        log.error "Error getting broadcasts: $ex"
        stations = null
    }

    return stations
}

def getLocation(game) {
    def location = null

    try {
        def team = game.teams.home.team
        location = team.venue.name + ", " + team.venue.city
    } catch(ex) {
        log.error "Error getting location: $ex"
        location = null
    }

    return location
}

def getTeamScore(teams) {
    return getScore(teams, false)
}

def getOpponentScore(teams) {
    return getScore(teams, true)
}

def getScore(teams, opponent) {
    log.debug "Getting current score"

    def score = 0

    if (state.Team.id == teams.away.team.id) {
        if (opponent) {
            score = teams.home.score
        } else {
            score = teams.away.score
        }
    } else {
        if (opponent) {
            score = teams.away.score
        } else {
            score = teams.home.score
        }
    }

    if (opponent) {
        log.debug "found opponent score ${score}"
    } else {
        log.debug "found team score ${score}"
    }

    return score
}

def getTeamName(teams) {
    return getName(teams, false)
}

def getOpponentName(teams) {
    return getName(teams, true)
}

def getName(teams, opponent) {
    def name = "unknown"

    if (state.Team.id == teams.away.team.id) {
        if (opponent) {
            return teams.home.team.name
        } else {
            return teams.away.team.name
        }
    } else {
        if (opponent) {
            return teams.away.team.name
        } else {
            return teams.home.team.name
        }
    }

    return name
}

def teamGoalScored() {
    log.debug "GGGOOOAAALLL!!!"

    triggerTeamGoalNotifications()

    triggerButtons()
    triggerSwitches()
    triggerSirens()
    triggerHorn()
    triggerFlashing()
}

def opponentGoalScored() {
    log.debug "BOOOOOOO!!!"

    triggerTeamOpponentNotifications()
}

def setSwitches(switches, on) {
    switches.eachWithIndex {s, i ->
    	if (on) {
            s.on()
            log.debug "Switch=$s.id on"
        } else {
            s.off()
            log.debug "Switch=$s.id off"
        }
    }
}

def triggerButtons() {
    try {
        def delay = settings.buttonDelay ?: 0
        if (settings.buttonGoals) {
            runIn(delay, triggerButtonsPush)
        }
    } catch(ex) {
        log.error "Error triggering buttons: $ex"
    }
}

def triggerButtonsPush() {
    try {
        if (settings.buttonGoals) {
            settings.buttonGoals.eachWithIndex {b, i ->
                b.push()
                log.debug "Buttton=$b.id pushed"
            }
        }
    } catch(ex) {
        log.error "Error pushing buttons: $ex"
    }
}

def triggerSwitches() {
    try {
        def delay = settings.switchDelay ?: 0
        if (settings.switchLights) {
            runIn(delay, triggerSwitchesOn)
        }
    } catch(ex) {
        log.error "Error triggering switches: $ex"
    }
}

def triggerSwitchesOn() {
    try {
        def switchOffSecs = settings.switchOnFor ?: 5

		setSwitches(settings.switchLights, true)

        runIn(switchOffSecs, triggerSwitchesOff)
    } catch(ex) {
        log.error "Error turning on switches: $ex"
    }
}

def triggerSwitchesOff() {
    try {        
        log.debug "turn switches off"
        
		setSwitches(settings.switchLights, false)
    } catch(ex) {
        log.error "Error turning off switches: $ex"
    }
}

def triggerSirens() {
    try {
        def delay = settings.sirenDelay ?: 0
        if (settings.sirens) {
            runIn(delay, triggerSirensOn)
        }
    } catch(ex) {
        log.error "Error triggering sirens: $ex"
    }
}

def triggerSirensOn() {
    try {
        def sirensOffSecs = settings.sirensOnFor ?: 3

        settings.sirens.eachWithIndex {s, i ->
            if (settings.sirensOnly) {
                s.siren()
            } else {
                s.both()
            }
            log.debug "Siren=$s.id on"
        }

        runIn(sirensOffSecs, triggerSirensOff)
    } catch(ex) {
        log.error "Error turning on sirens: $ex"
    }
}

def triggerSirensOff() {
    try {
        log.debug "turn sirens off"
        settings.sirens.eachWithIndex {s, i ->
            s.off()
            log.debug "Siren=$s.id off"
        }
    } catch(ex) {
        log.error "Error turning off sirens: $ex"
    }
}

def triggerHorn() {
    try {
        def delay = settings.soundDelay ?: 0
        if (settings.sound) {
            runIn(delay, playHorn)
        }
    } catch(ex) {
        log.error "Error running horn: $ex"
    }
}

def playHorn() {
    try {
        def hornURI = getHornURL(state.Team)

        log.debug "play horn"
        if (hornURI) {
            if (settings.soundDuration) {
                if (settings.volume) {
                    settings.sound.playTrackAndResume(hornURI, settings.soundDuration, settings.volume)
                } else {
                    settings.sound.playTrackAndResume(hornURI, settings.soundDuration)
                }
            } else {
                if (settings.volume) {
                    settings.sound.playTrackAtVolume(hornURI, settings.volume)
                } else {
                    settings.sound.playTrack(hornURI)
                }
            }
        } else {
            log.debug "Error, could not get horn URI"
        }
    } catch(ex) {
        log.error "Error playing horn: $ex"
    }
}

def triggerFlashing() {
    try {
        def delay = settings.flashingDelay ?: 0
        if (settings.flashLights) {
            runIn(delay, flashingLights)
        }
    } catch(ex) {
        log.error "Error playing horn: $ex"
    }
}

def flashingLights() {
    try {
        def doFlash = true
        def numFlash = settings.numFlashes ?: 3
        def onFor = settings.flashOnFor ?: 1000
        def offFor = settings.flashOffFor ?: 1000

        setLightOptions(settings.flashLights)

        log.debug "LAST ACTIVATED IS: ${state.lastActivated}"
        if (state.lastActivated) {
            def elapsed = now() - state.lastActivated
            def sequenceTime = (numFlash + 1) * (onFor + offFor)
            doFlash = elapsed > sequenceTime
            log.debug "DO FLASH: $doFlash, ELAPSED: $elapsed, LAST ACTIVATED: ${state.lastActivated}"
        }

        if (doFlash) {
            log.debug "FLASHING $numFlash times"
            state.lastActivated = now()
            log.debug "LAST ACTIVATED SET TO: ${state.lastActivated}"
            def initialActionOn =  settings.flashLights.collect{it.currentSwitch != "on"}
            def delay = 0L
            numFlash.times {
                log.debug "Switch on after  $delay msec"
                settings.flashLights.eachWithIndex {s, i ->
                    if (initialActionOn[i]) {
                        s.on(delay:delay)
                    }
                    else {
                        s.off(delay:delay)
                    }
                }
                delay += onFor
                log.debug "Switch off after $delay msec"
                settings.flashLights.eachWithIndex {s, i ->
                    if (initialActionOn[i]) {
                        s.off(delay:delay)
                    }
                    else {
                        s.on(delay:delay)
                    }
                }
                delay += offFor
            }

            def restoreDelay = (delay/1000) + 1
            log.debug "restore flash devices after $restoreDelay seconds"
            runIn(restoreDelay, flashRestoreLights)
        }

    } catch(ex) {
        log.error "Error Flashing Lights: $ex"
    }
}

def flashRestoreLights() {
    try {
        log.debug "restoring flash devices"
        restoreLightOptions(settings.flashLights)
    } catch(ex) {
        log.error "Error restoring flashing lights: $ex"
    }
}

def setLightPrevious(lights) {
    lights.each {
        if (it.hasCapability("Color Control")) {
            log.debug "save light color values"
            state.previous[it.id] = [
                "switch": it.currentValue("switch"),
                "level" : it.currentValue("level"),
                "hue": it.currentValue("hue"),
                "saturation": it.currentValue("saturation")
            ]
        } else if (it.hasCapability("Switch Level")) {
            log.debug "save light level"
            state.previous[it.id] = [
                "switch": it.currentValue("switch"),
                "level" : it.currentValue("level"),
            ]
        } else {
            log.debug "save light switch"
            state.previous[it.id] = [
                "switch": it.currentValue("switch"),
            ]
        }
    }
}

def setLightOptions(lights) {
    def color = settings.lightColor
    def level = (settings.lightLevel as Integer) ?: 100

    // default to Red
    def hueColor = 100
    def saturation = 100

    if (color) {
        switch(color) {
            case "White":
            hueColor = 52
            saturation = 19
            break;
            case "Blue":
            hueColor = 70
            break;
            case "Green":
            hueColor = 39
            break;
            case "Yellow":
            hueColor = 25
            break;
            case "Orange":
            hueColor = 10
            break;
            case "Purple":
            hueColor = 75
            break;
            case "Pink":
            hueColor = 83
            break;
            case "Red":
            hueColor = 100
            break;
        }
    }

    setLightPrevious(lights)

    lights.each {
        if (settings.lightColor && it.hasCapability("Color Control")) {
            def newColorValue = [hue: hueColor, saturation: saturation, level: level]
            log.debug "$it.id - new light color values = $newColorValue"
            it.setColor(newColorValue)
        } 

        if (settings.lightLevel && it.hasCapability("Switch Level")) {
            log.debug "$it.id - new light level = $level"
            it.setLevel(level)
        } 
    }
}

def restoreLightOptions(lights) {
    lights.each {
        if (settings.lightColor && it.hasCapability("Color Control")) {
            log.debug "$it.id - restore light color"
            it.setColor(state.previous[it.id]) 
        } 

        if (settings.lightLevel && it.hasCapability("Switch Level")) {
            def level = state.previous[it.id].level ?: 100
            log.debug "$it.id - restore light level = $level"
            it.setLevel(level) 
        }

        def lightSwitch = state.previous[it.id].switch ?: "off"
        log.debug "$it.id - turn light $lightSwitch"
        if (lightSwitch == "on") {
            it.on()
        } else {
            it.off()
        }
    }
}

def triggerTeamGoalNotifications() {
    if (sendGoalMessage) {
        def game = state.Game
        def msg = null

        if (game) {           
            def goals = getTeamScore(game.teams)

            if (goals == 1) {
                msg = getTeamName(game.teams) + " scored their first goal!"
            } else {
                msg = getTeamName(game.teams) + " have scored ${goals} goals!"
            }
            msg = msg + "\n${game.teams.away.team.name} ${game.teams.away.score}, ${game.teams.home.team.name} ${game.teams.home.score}"
        } else {
            msg = "${settings.nhlTeam} just Scored!"
        }

        triggerNotifications(msg)
    }
}

def triggerTeamOpponentNotifications() {
    if (sendGoalMessage) {
        def game = state.Game
        def msg = null

        if (game) {           
            def goals = getOpponentScore(game.teams)

            msg = getOpponentName(game.teams) + " scored."
            msg = msg + "\n${game.teams.away.team.name} ${game.teams.away.score}, ${game.teams.home.team.name} ${game.teams.home.score}"
        } else {
            msg = "Opponent Scored."
        }

        triggerNotifications(msg)
    }
}

def triggerStatusNotifications() {
    if (sendGameDayMessage) {
        def game = state.Game
        def msg = null
        def msg2 = null

        if (game) {
            switch (state.currentGameStatus) {
                case state.GAME_STATUS_SCHEDULED:
                msg = "${game.teams.away.team.name} vs ${game.teams.home.team.name}"
                if (state.gameTime) {
                    if (state.gameStations) {
                        msg = msg + "\nToday, ${state.gameTime} on ${state.gameStations}"
                    } else {
                        msg = msg + "\nToday, ${state.gameTime}"
                    }
                    if (state.gameLocation) {
                        msg = msg + "\n${state.gameLocation}"
                    }
                }
                break

                case state.GAME_STATUS_PREGAME:
                msg = "Pregame for ${game.teams.away.team.name} vs ${game.teams.home.team.name} is starting soon, game is at ${state.gameTime}!"
                break

                case state.GAME_STATUS_IN_PROGRESS:
                msg = "${game.teams.away.team.name} vs ${game.teams.home.team.name} is now in progress!"
                // game start
                if (settings.gameSwitches) {
                	setSwitches(settings.gameSwitches, true)
                }
                break

                case state.GAME_STATUS_IN_PROGRESS_CRITICAL:
                msg = "${game.teams.away.team.name} vs ${game.teams.home.team.name} is in critial last minutes of the game, Go " + getTeamName(game.teams) + "!"
                break

                case state.GAME_STATUS_FINAL6:
                case state.GAME_STATUS_FINAL7:
                msg = "Final Score:\n${game.teams.away.team.name} ${game.teams.away.score}\n${game.teams.home.team.name} ${game.teams.home.score}"

                if (getTeamScore(game.teams) > getOpponentScore(game.teams)) {
                    msg2 =  getTeamName(game.teams) + " win!!!"
                } else if (getTeamScore(game.teams) < getOpponentScore(game.teams)) {
                    msg2 =  getTeamName(game.teams) + " lost."
                } else {
                    msg2 = "Tie game!"
                }
                
                if (settings.gameSwitches) {
                	if (settings.gameSwitchOff) {
	                	setSwitches(settings.gameSwitches, false)
                	}
                }
                break

                case state.GAME_STATUS_UNKNOWN:
                default:
                    break
            }

            if (msg) {
                if (triggerNotifications(msg)) {
                    if (msg2) {
                        triggerNotifications(msg2)
                    }
                }
            }
        } else {
            log.debug( "invalid game object")
        }
    }
    else {
    }

    //  set game status notified
    state.notifiedGameStatus = state.currentGameStatus    	    	
}

def triggerNotifications(msg) {
    try {
        if (msg == null) {
            log.debug( "No message to send" )
        } else {
            if ( sendPushMessage == true ) {
                log.debug( "Sending push message" )
                log.debug( "msg: ${msg}" )
                sendPush( msg )
            }

            if ( sendPhoneMessage ) {
                log.debug( "Sending text message to: ${sendPhoneMessage}" )
                log.debug( "msg: ${msg}" )
                sendSms( sendPhoneMessage, msg )
            }

        }
    } catch(ex) {
        log.error "Error sending notifications: $ex"
        return false
    }

    return true
}

private def versionParagraph() {
    def text = "NHL Game Notifications\nVersion ${version()}"
}

private def version() {
    return "0.9.4"
}