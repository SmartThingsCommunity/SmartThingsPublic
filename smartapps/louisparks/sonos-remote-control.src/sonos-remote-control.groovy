/**
 *  Sonos Remote Control
 *
 *  Copyright 2014 Louis Parks
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
definition(
    name: "Sonos Remote Control",
    namespace: "louisparks",
    author: "Louis Parks",
    description: "Control your sonos with a button remote.",
    category: "Convenience",
    iconUrl: "https://dl.dropboxusercontent.com/u/6948445/icons/remote.png",
    iconX2Url: "https://dl.dropboxusercontent.com/u/6948445/icons/remotex2.png")


preferences {
       	page(name:"chooseDevices",uninstall:true,nextPage:"configureButtons"){
            section("Remote") {
                    input "buttonDevice", "capability.button", title: "Please select your remote", multiple: false, required: true
                    input "buttonCount", "number", title: "How many Buttons?", required: true
            }
            section("Sonos") {
                    input "sonos", "capability.musicPlayer", title: "Will control this Sonos", multiple:false, required: true
            }
			section("More options", hideable: true, hidden: true) {
				input "volumeIncrement", "number", title: "Set the volume increment", description: "0-20", required: false
            }
			section([mobileOnly:true]) {
				label title: "Assign a name", required: false
			}            
        }
		page(name: "configureButtons", title: "Configure Buttons", nextPage:"chooseTracks")
		page(name: "chooseTracks", title: "Select stations for you favorties",install:true)
}

def configureButtons() {
	dynamicPage(name: "configureButtons", title: "Please configure buttons"){
    	for(int buttonNumber=1; buttonNumber<buttonCount+1;buttonNumber++){
            section{
                input "button${buttonNumber}pushed","enum",title:"When button ${buttonNumber} is pushed", required: false,options:getButtonOptions()
                input "button${buttonNumber}held","enum",title:"When button ${buttonNumber} is held", required: false,options:getButtonOptions()
            }
        }
    }
}

def chooseTracks(){
	dynamicPage(name: "chooseTracks",install:true) {
        section{
             input "tracks","enum",title:"Cycle through these favorite tracks", required:false, multiple: true, options: favoriteSelections()
        }        
    }
}

def getButtonOptions() {
	log.trace 'sonos options'
	def options = new LinkedHashSet()
	options.addAll(sonosCommands().collect{["${it.id}":it.desc]})
	return options
}

def sonosCommands(){
	def sonosCommandOptions=
    	[[id:'DO_NOTHING',desc:'Do Nothing',funct:null],
         [id:'VOLUME_UP',desc:'Volume Up',funct:volumeUp],
    	 [id:'VOLUME_DOWN',desc:'Volume Down',funct:volumeDown],
     	 [id:'TOGGLEPLAY',desc:'Toggle Pause/Play',funct:toggleStopPlay],
     	 [id:"CYCLE_SELECTED",desc:'Cycle through my selected tracks',funct:nextFavorite],
     	 [id:"SKIP_NEXT",desc:'Skip to next Track',funct:skipNext],
     	 [id:"PREVIOUS_TRACK",desc:'Play previous track',funct:playPrevious],
     	 [id:"TOGGLE_MUTE",desc:'Toggle Mute',funct:toggleMute],
         [id:"REFRESH",desc:'Refresh Sonos State',funct:refreshSonos],
        ]
         
    return sonosCommandOptions
}

def installed() {
	log.debug "Installed with settings: ${settings}"
	initialize()
}

def updated() {
	log.debug "Updated with settings: ${settings}"
	unsubscribe()
	initialize()
}

def initialize() {
    subscribe(buttonDevice, "button", buttonEvent)
    state.currentFavoriteIndex = 0
    if(state.favorites==null){state.favorites=[]}
	if(state.sonosFavorites==null){state.sonosFavorites=[]}
    state.volumeIncrement = volumeIncrement?:5
    subscribe(sonos, "trackData", sonosEventHandler)
    subscribe(sonos, "level", sonosEventHandler)
    subscribe(sonos, "mute", sonosEventHandler)
    subscribe(sonos, "status", sonosEventHandler)
    saveFavoriteSelectionsToStateFavorites()
    log.debug "Initialization Complete"
}

def sonosEventHandler(evt){
	log.debug "sonos event $evt.name = $evt.value ($evt.data)" 
}


def buttonEvent(evt){
	log.debug "buttonEvent: $evt.name = $evt.value ($evt.data)"
	def value = evt.value
	def json = toJson(evt.data)
    executeButtonCommand(json.buttonNumber,evt.value)
}

def executeButtonCommand(buttonNumber,action){
	def eventName = "button${buttonNumber}${action}"
    def command = settings[eventName]
	if(command != null) {
        log.debug "Found: $command for $eventName"
		def commandFunction = sonosCommands().find{it.id==command}.funct
        if(commandFunction){
        	"$commandFunction"()
        }
	}
}


def saveFavoriteSelectionsToStateFavorites(){
	def newFavoritesList = []
    if(tracks){
        tracks.each{selectedStation->
        	newFavoritesList << state.favorites.find{it.station==selectedStation}
        }
        state.favorites = newFavoritesList
     }
}

def favoriteSelections(){
    def newOptions = state.favorites.findAll{it.selected==true}
    def states = sonos.statesSince("trackData", new Date(0), [max:30])
    states?.each{
    	def stationData = it.jsonValue
        if(!newOptions.find{fav->fav.station==stationData.station}){
   			newOptions << [uri:stationData.uri,metaData:stationData.metaData,station:stationData.station,selected:false]
        }
    }
    def options = newOptions.collect{it.station?:"N/A"}.sort()
    state.favorites = newOptions
	return options
}

def isPlaylistOrAlbum(trackData){
	trackData.uri.startsWith('x-rincon-cpcontainer') ? true : false
}

def refreshSonos(){
	sonos.refresh()
}

def playStation(trackData){
    if(isPlaylistOrAlbum(trackData)){
		log.debug "Working around some sonos device handler playlist wierdness ${trackData.station}. This seems to work"
        trackData.transportUri=trackData.uri
        trackData.enqueuedUri="savedqueues"
        trackData.trackNumber="1"
        sonos.setTrack(trackData)
        pause(1500)
        sonos.play()
    }
    else{
    	sonos.playTrack(trackData.uri,trackData.metaData)
    }
}

def nextFavorite(){
    def favs = state.favorites
    if(favs.size==0){
		log.debug "No Favorites Found"
    }
    else{
		state.currentFavoriteIndex =  state.currentFavoriteIndex+1 >= favs.size()?0:state.currentFavoriteIndex+1
    	def fav = favs[state.currentFavoriteIndex]
    	playStation(fav)
    }
}


def volumeUp(){
    def currentVolume = sonos.currentValue("level")
	log.debug "Raising volume from $currentVolume"
	sonos.setLevel(currentVolume+state.volumeIncrement)
}

def volumeDown(){
	def currentVolume = sonos.currentValue("level")
    log.debug "Lowering volume from $currentVolume"
    sonos.setLevel(currentVolume-state.volumeIncrement)
}

def toggleStopPlay(){
    def muteState=sonos.currentValue("mute")
   	def playingState=sonos.currentValue("status")
	log.debug("toggle status:$playingState")
    playingState=="playing"?sonos.pause():sonos.play()
}

def toggleMute(){
    def muteState=sonos.currentValue("mute")
    muteState=="muted"?sonos.unmute():sonos.mute()
}

def skipNext(){
    sonos.nextTrack()
}

def playPrevious(){
    sonos.previousTrack()
}

def toJson(str){
    def slurper = new groovy.json.JsonSlurper()
    def json = slurper.parseText(str)
}