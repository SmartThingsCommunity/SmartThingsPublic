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
 * Media Renderer Events
 *
 *  Author: SmartThings-Ule
 *  Date: 2015-10-09
 *
 *  Date: 2015-10-09
 */
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidKeyException; 
 
definition(
	name: "Media Renderer Events ",
	namespace: "smartthings",
	author: "SmartThings",
	description: "Play a sound,Radionomy station or custom message through your MediaRenderer,Sonos when the mode changes or other events occur.",
	category: "SmartThings Labs",
	iconUrl: "http://urbansa.com/icons/mr.png",
	iconX2Url: "http://urbansa.com/icons/mr@2x.png"
)

preferences {
	page(name: "mainPage", title: "Play or Stop message, sount, track or station on your Player when something happens", install: true, uninstall: true)
	page(name: "triggersPlay", title: "Play When ...")
    page(name: "triggersStop", title: "Stop When ...")
    page(name: "chooseTrack", title: "Select a track or station")
    page(name: "addMessage", title: "Add the message to play")
    page(name: "ttsKey", title: "Add the Text for Speach Key")
    page(name: "ttsSettings", title: "Text for Speach Settings")
	page(name: "timeIntervalInput", title: "Only during a certain time") {
		section {
			input "starting", "time", title: "Starting", required: false
			input "ending", "time", title: "Ending", required: false
		}
	}
}

def mainPage() {
	dynamicPage(name: "mainPage") {
		section{
        	href "triggersPlay", title: "Select play triggers?",required: flase, description: anythingSet()?"Change triggers":"Tap to set"
            href "triggersStop", title: "Select stop triggers?",required: flase, description: anythingSet("Stop")?"Change triggers":"Tap to set"
        }

        def radionomyOptions = ["70s 4u","80s 4u","A 1 All Disney On Wide Radius","A Better Christian Station","A Better Classic Blues Vintage Station","A Better Classic Rock Station","A Better Classical Station","A Better Love Songs Station","A Blues Dream Classic & New Blues 24h","A I R Radio Freestyle Slow","A Jazz Dream Classic & New Jazz 24h","A Lounge Dream Relax 24h","A Slow Dream Rnb Soul 24h","Aaa Soul","Abaco Libros Y Cafe Radio","Abacus Fm Bach","Abacus Fm Beethoven One","Abacus Fm Chopin","Abacus Fm Mozart Piano","Abacus Fm Mozart Symphony","Abacus Fm Nature","Abacus Fm Smooth Jazz","Abacus Fm Vintage Jazz","Abalone","Abc Classic","Abc Jazz","Abc Lounge","Abc Love","Abc Piano","Abc Relax","Abc Symphony","Abonni Cafe","Absolute Chillout","Aclassicfm Web","Acoustic Fm","Acoustic Guitar","Adorale","Adventistinternetradio","Air Jazz","Air Lounge","Alabanza Digital","All 60s All The Time Keepfreemusic Com","All Beethoven On Wide Radius","All Jazz Radio","All Piano Radio","All Smooth Jazz","Allegro Jazz","Alpenradio|volksmusik","Ambiance Classique","Ambiance Lounge","Ambient And Lounge","American Roots","Anime Extremo Net","Animusique","Anunciandoelreinoradiocatolica Raphy Rey","Atomik Radio","Azur Blues","Bachata Dominicana","Baladas Romanticas Radio","Barock Music","Beautiful Music 101","Beethoven Radio","Best Blues I Know","Best Of Slow","Bestclubdance","Big Band Magic","Bob Marley","Boozik Jazzy","Cafe Romantico Radio","Cantico Nuevo Stereo","Chill 100","Chill Out Radio Gaia","Chillout Classics","Chocolat Radio","Christmas 1000","Christmas Wonderland Radio","Chronicles Christian Radio","Cinemix","Cinescore","Classic Rock #1","Classical Hits 1000","Classical Jazz Radio","Classicalmusicamerica Com","Classicalways","Cocktelera Blues","Colombiabohemia","Colombiacrossover","Colombiaromantica","Colombiasalsarosa","Con Alma De Blues","Cool Kids Radio","Cristalrelax","Cumbias Inmortales","Dance90","Dis Cover Radio","Dlrpradio","Dream Baby","Elium Rock","Enchantedradio","Energiafmonline","Enjoystation","Esperanza 7","Eure Du Blues","F A Radio Animes","Feeling Love","Feeling The Blues","Feevermix","Frequences Relaxation","Funadosong","Funky Blues","Generikids","Gupshupcorner Com","Halloweenradio Net Kids","Healing Music Radio","Hippie Soul Radio","Hit Radio 100","Hit Radio Rocky Schlager","Hits #1","Hits 70s #1","Hits 80s #1","Hits Classical Music 1000","Hits My Music Zen","Hits Sweet Radio 1000","Hot 40 Music","Hotmixradio 80","Hotmixradio 90","Hotmixradio Baby","Hotmixradio Hits","Hotmixradio Rock","Illayarajavin Thevitatha Padalgal!","Instrumental Hits","Instrumentals Forever","Intimisima Radio","Jamaican Roots Radio","Jazz Light","Jazz Lovers Radio","Jazz Swing Manouche Radio","Jazz Vespers Radio","Jazzclub","K Easy","Kalasam Com","Lagujawa","Las Grandes Bandas Radio","Latina 104 Web","Ledjam Radio","Les Grands Fan De Disney Radio","Los Grandes Grupos Radio","Love Love Radio 2","Love Radio","Love Radio","Love Radio 2","Lovehitsradio","Lovemixradio","Made In Classic","Martini In The Morning","Martini In The Morning (64k)","Martini In The Morning (mp3)","Mocha","Mozart","Mozartiana","Musiconly Fm","Ocean Breeze","Oldies 1000","Ourworld Pop","Panda Pop Radio","Panda Show Radio","Pop Y Rock En Español","Powerclub Station","Que Viva Mexico","Radio Animex (musica Anime Y Mucho Mas)","Radio Beats Fm","Radio Cristiana Online","Radio Dance #1","Radio Hunter The Hitz Channel","Radio Junior","Radio Love 141","Radio Mozart","Radio Nostalgia","Radio Otakus Dream","Radio Plenitude","Radio Stonata","Radio Tango Velours","Radiomyme Tv","Radionomix","Radiosky Music","Radiounoplus","Revolution Fm","Rey De Corazones","Rjm Jazzy","Rjm Lounge","Romance Vos Plus Belle Chanson Damour","Romantica Digital","Sertanejo","Slowradio Com","Smooth Jazz 101","Smooth Jazz 247","Smooth Jazz 4u","Smooth Riviera","Soft Riw Love Channel 100%","Sophisticated Easy Sounds","Sorcerer Radio Disney Park Music","The Great 80s","Theneosoulcafe","Topclub","Trop Beautiful","Trova Radio El Sentimiento Hecho Música","True Oldies Channel","Webradio Tirol","Wine Farm And Tourist Radio","Zen For You"]
 		section{
            input "actionType", "enum", title: "Action?", required: true, defaultValue: "Message",submitOnChange:true, options: ["Message","Sound","Track","Radionomy","Multiple Radionomy"]
        }

        section{  
        	href "addMessage", title: "Play this message?",required: actionType == "Message"? true:flase, description: message ? message : "Tap to set", state: message ? "complete" : "incomplete"
        	input "sound", "enum", title: "Play this Sound?", required: actionType == "Sound"? true:flase, defaultValue: "Bell 1", options: ["Bell 1","Bell 2","Dogs Barking","Fire Alarm","Piano","Lightsaber","Noise"]
			href "chooseTrack", title: "Play this track",required: actionType == "Track"? true:flase, description: song ? (song?:state.selectedSong?.station) : "Tap to set", state: song ? "complete" : "incomplete"
	        input "radionomy", "enum", title: "Play this Radionomy Station?", required: actionType == "Radionomy"? true:flase, options: radionomyOptions
            input "radionomyM", "enum", title: "Play Multiple Radionomy Station?", required: actionType == "Multiple Radionomy"? true:flase, multiple:true, options: radionomyOptions
		}
        section("Radionomy settings", hideable: (actionType == "Radionomy" || actionType == "Multiple Radionomy") && !RTKey ? flase:true, hidden: true) {
            input "RdoMode", "enum", title: "Multiple Mode?", required: true, defaultValue: "Shuffle", options: ["Loop","Random","Shuffle"]
        }
        section{  
        	href "ttsSettings", title: "Text for Speach Settings",required:flase, description:ttsMode
        }
		section {
			input "sonos", "capability.musicPlayer", title: "On this Speaker", required: true,multiple:true
		}
		section("More options", hideable: true, hidden: true) {
			input "onPlay", "bool", title: "When turned On, Play if No Media", required: false, defaultValue: flase
            input "resumePlaying", "bool", title: "Resume currently playing music after notification", required: false, defaultValue: true
			input "volume", "number", title: "Temporarily change volume", description: "0-100%", required: false
			input "party", "capability.musicPlayer", title: "Party on Speakers", required: false,multiple:true
            input "frequency", "decimal", title: "Minimum time between actions (defaults to every event)", description: "Minutes", required: false
			href "timeIntervalInput", title: "Only during a certain time", description: timeLabel ?: "Tap to set", state: timeLabel ? "complete" : "incomplete"
			input "days", "enum", title: "Only on certain days of the week", multiple: true, required: false,
				options: ["Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"]
			if (settings.modes) {
            	input "modes", "mode", title: "Only when mode is", multiple: true, required: false
            }
			input "oncePerDay", "bool", title: "Only once per day", required: false, defaultValue: false
		}
		section([mobileOnly:true]) {
			label title: "Assign a name", required: false
			mode title: "Set for specific mode(s)", required: false
		}
	}
}

def triggersPlay(){
	dynamicPage(name: "triggersPlay") {
	    triggers("")
    }
}
def triggersStop(){
        dynamicPage(name: "triggersStop") {
        triggers("Stop")
    }
}

def triggers(command=""){
    	def anythingSet = anythingSet(command)
        if (anythingSet) {
			section(){
				ifSet "motion$command", "capability.motionSensor", title: "Motion Here", required: false, multiple: true
				ifSet "contact$command", "capability.contactSensor", title: "Contact Opens", required: false, multiple: true
				ifSet "contactClosed$command", "capability.contactSensor", title: "Contact Closes", required: false, multiple: true
				ifSet "acceleration$command", "capability.accelerationSensor", title: "Acceleration Detected", required: false, multiple: true
				ifSet "mySwitch$command", "capability.switch", title: "Switch Turned On", required: false, multiple: true
				ifSet "mySwitchOff$command", "capability.switch", title: "Switch Turned Off", required: false, multiple: true
				ifSet "arrivalPresence$command", "capability.presenceSensor", title: "Arrival Of", required: false, multiple: true
				ifSet "departurePresence$command", "capability.presenceSensor", title: "Departure Of", required: false, multiple: true
				ifSet "smoke$command", "capability.smokeDetector", title: "Smoke Detected", required: false, multiple: true
				ifSet "water$command", "capability.waterSensor", title: "Water Sensor Wet", required: false, multiple: true
				ifSet "lock$command", "capability.lock", title: "Lock locks", required: false, multiple: true
				ifSet "lockLocks$command", "capability.lock", title: "Lock unlocks", required: false, multiple: true
				ifSet "button1$command", "capability.button", title: "Button Press", required:false, multiple:true //remove from production
				//ifSet "touchSensorPlay$command", "capability.touchSensor", title: "Play Touched", required:false, multiple:true
                ifSet "triggerModes$command", "mode", title: "System Changes Mode", required: false, multiple: true
				ifSet "timeOfDay$command", "time", title: "At a Scheduled Time", required: false
			}
		}
		def hideable = anythingSet || app.installationState == "COMPLETE"
		def sectionTitle = anythingSet ? "Select additional triggers" : "Select triggers..."

		section(sectionTitle, hideable: hideable, hidden: true){
			ifUnset "motion$command", "capability.motionSensor", title: "Motion Here", required: false, multiple: true
			ifUnset "contact$command", "capability.contactSensor", title: "Contact Opens", required: false, multiple: true
			ifUnset "contactClosed$command", "capability.contactSensor", title: "Contact Closes", required: false, multiple: true
			ifUnset "acceleration$command", "capability.accelerationSensor", title: "Acceleration Detected", required: false, multiple: true
			ifUnset "mySwitch$command", "capability.switch", title: "Switch Turned On", required: false, multiple: true
			ifUnset "mySwitchOff$command", "capability.switch", title: "Switch Turned Off", required: false, multiple: true
			ifUnset "arrivalPresence$command", "capability.presenceSensor", title: "Arrival Of", required: false, multiple: true
			ifUnset "departurePresence$command", "capability.presenceSensor", title: "Departure Of", required: false, multiple: true
			ifUnset "smoke$command", "capability.smokeDetector", title: "Smoke Detected", required: false, multiple: true
			ifUnset "water$command", "capability.waterSensor", title: "Water Sensor Wet", required: false, multiple: true
			ifUnset "lock$command", "capability.lock", title: "Lock locks", required: false, multiple: true
			ifUnset "lock$command", "capability.lock", title: "Lock unlocks", required: false, multiple: true
			ifUnset "button1$command", "capability.button", title: "Button Press", required:false, multiple:true //remove from production
			//ifUnset "touchSensorPlay$command", "capability.touchSensor", title: "Play Touched", required:false, multiple:true
            ifUnset "triggerModes$command", "mode", title: "System Changes Mode", description: "Select mode(s)", required: false, multiple: true
			ifUnset "timeOfDay$command", "time", title: "At a Scheduled Time", required: false
		}
}

def addMessage() {
	dynamicPage(name: "addMessage") {
		section{
			 input "message","text",title:"Play this message?", required:actionType == "Message"? true:flase
             paragraph "You can use wilcard with your message"
             paragraph "#name = Device Name ex. Kitchen Light, Speaker Room"
             paragraph "#type = Device Type ex. Motion, Temperature"
             paragraph "#value = New Device State ex. On, Off, Active"
             paragraph "#mode = Location Mode ex. Stay, Away"
             paragraph "#location = Location Name ex. Home, Office"
             paragraph "#s = Alexa says the message instead answerd"
		}
	}
}
def ttsSettings() {
	dynamicPage(name: "ttsSettings") {
        def languageOptions = ["ca-es":"Catalan","zh-cn":"Chinese (China)","zh-hk":"Chinese (Hong Kong)","zh-tw":"Chinese (Taiwan)","da-dk":"Danish","nl-nl":"Dutch","en-au":"English (Australia)","en-ca":"English (Canada)","en-gb":"English (Great Britain)","en-in":"English (India)","en-us":"English (United States)","fi-fi":"Finnish","fr-ca":"French (Canada)","fr-fr":"French (France)","de-de":"German","it-it":"Italian","ja-jp":"Japanese","ko-kr":"Korean","nb-no":"Norwegian","pl-pl":"Polish","pt-br":"Portuguese (Brazil)","pt-pt":"Portuguese (Portugal)","ru-ru":"Russian","es-mx":"Spanish (Mexico)","es-es":"Spanish (Spain)","sv-se":"Swedish (Sweden)"]
 		section() {
	        //input "externalTTS", "bool", title: "Force Only External Text to Speech", required: false, defaultValue: false
            input "ttsMode", "enum", title: "Mode?", required: true, defaultValue: "Vioce RSS",submitOnChange:true, options: ["SmartTings","Vioce RSS","Google","Alexa"]
            input "stLanguage", "enum", title: "SmartThings Voice?", required: true, defaultValue: "en-US Salli", options: ["da-DK Naja","da-DK Mads","de-DE Marlene","de-DE Hans","en-US Salli","en-US Joey","en-AU Nicole","en-AU Russell","en-GB Amy","en-GB Brian","en-GB Emma","en-GB Gwyneth","en-GB Geraint","en-IN Raveena","en-US Chipmunk","en-US Eric","en-US Ivy","en-US Jennifer","en-US Justin","en-US Kendra","en-US Kimberly","es-ES Conchita","es-ES Enrique","es-US Penelope","es-US Miguel","fr-CA Chantal","fr-FR Celine","fr-FR Mathieu","is-IS Dora","is-IS Karl","it-IT Carla","it-IT Giorgio","nb-NO Liv","nl-NL Lotte","nl-NL Ruben","pl-PL Agnieszka","pl-PL Jacek","pl-PL Ewa","pl-PL Jan","pl-PL Maja","pt-BR Vitoria","pt-BR Ricardo","pt-PT Cristiano","pt-PT Ines","ro-RO Carmen","ru-RU Tatyana","ru-RU Maxim","sv-SE Astrid","tr-TR Filiz"]
            input "googleLanguage", "enum", title: "Google Voice?", required: true, defaultValue: "en", options: ["af":"Afrikaans","sq":"Albanian","ar":"Arabic","hy":"Armenian","ca":"Catalan","zh-CN":"Mandarin (simplified)","zh-TW":"Mandarin (traditional)","hr":"Croatian","cs":"Czech","da":"Danish","nl":"Dutch","en":"English","eo":"Esperanto","fi":"Finnish","fr":"French","de":"German","el":"Greek","ht":"Haitian Creole","hi":"Hindi","hu":"Hungarian","is":"Icelandic","id":"Indonesian","it":"Italian","ja":"Japanese","ko":"Korean","la":"Latin","lv":"Latvian","mk":"Macedonian","no":"Norwegian","pl":"Polish","pt":"Portuguese","ro":"Romanian","ru":"Russian","sr":"Serbian","sk":"Slovak","es":"Spanish","sw":"Swahili","sv":"Swedish","ta":"Tamil","th":"Thai","tr":"Turkish","vi":"Vietnamese","cy":"Welsh"]
            input "ttsLanguage", "enum", title: "RSS Language?", required: true, defaultValue: "en-us",options: languageOptions
            href "ttsKey", title: "Voice RSS Key", description: ttsApiKey, state: ttsApiKey ? "complete" : "incomplete", required: ttsMode == "Vioce RSS"?true:false
            input "alexaApiKey", "text", title: "Alexa Access Key", required: ttsMode == "Alexa" ? true:false,  defaultValue:"millave"
        }
	}
}
def chooseTrack() {
	dynamicPage(name: "chooseTrack") {
		section{
			input "song","enum",title:"Play this track", required:true, multiple: false, options: songOptions()
		}
	}
}

def ttsKey() {
	dynamicPage(name: "ttsKey") {
		section{
			input "ttsApiKey", "text", title: "TTS Key", required: false, defaultValue:""
		}
        section ("Voice RSS provides free Text-to-Speech API as WEB service, allows 350 free request per day with high quality voice") {
            href(name: "hrefRegister",
                 title: "Register",
                 required: false,
                 style: "external",
                 url: "http://www.voicerss.org/registration.aspx",
                 description: "Register and obtain you TTS Key")
            href(name: "hrefKnown",
                 title: "Known about Voice RSS",
                 required: false,
                 style: "external",
                 url: "http://www.voicerss.org/",
                 description: "Go to www.voicerss.org")
        }
    }
}


private songOptions() {
	// Make sure current selection is in the set
	log.trace "size ${sonos?.size()}"
	def options = new LinkedHashSet()
	if (state.selectedSong?.station) {
		options << state.selectedSong.station
	}
	else if (state.selectedSong?.description) {
		// TODO - Remove eventually? 'description' for backward compatibility
		options << state.selectedSong.description
	}
	// Query for recent tracks
	def dataMaps
	sonos.each {
            dataMaps = it.statesSince("trackData", new Date(0), [max:30]).collect{it.jsonValue}
            options.addAll(dataMaps.collect{it.station})
    }
	log.trace "${options.size()} songs in list"
	options.take(30 * (sonos?.size()?:0)) as List
}

private saveSelectedSong() {
	try {
        if (song == state.selectedSong?.station){
        	log.debug "Selected song $song"
        }
        else{
            def dataMaps
            def data
            log.info "Looking for $song"
            
            sonos.each {
                dataMaps = it.statesSince("trackData", new Date(0), [max:30]).collect{it.jsonValue}
                data = dataMaps.find {s -> s.station == song}
                log.info "Found ${data?.station?:"None"}"
                if (data) {
                    state.selectedSong = data
                    log.debug "Selected song = $state.selectedSong"
                }
                else if (song == state.selectedSong?.station) {
                    log.debug "Selected song not found"
                }
             }
        }
	}
	catch (Throwable t) {
		log.error t
	}
}

private anythingSet(command="") {
	for (name in ["motion","contact","contactClosed","acceleration","mySwitch","mySwitchOff","arrivalPresence","departurePresence","smoke","water","lock","lockLocks","button1","timeOfDay","triggerModes","timeOfDay"]) {
    //"touchSensorPlay",
        if (settings["$name$command"]) {
			return true
		}
	}
	return false
}

private ifUnset(Map options, String name, String capability) {
	if (!settings[name]) {
		input(options, name, capability)
	}
}

private ifSet(Map options, String name, String capability) {
    if (settings[name]) {
		input(options, name, capability)
	}
}

def installed() {
	log.debug "Installed with settings: ${settings}"

}

def updated() {
	log.debug "Updated with settings: ${settings}"
	unsubscribe()
	unschedule()
	subscribeToEvents()
    subscribeToEvents("Stop")
}

def subscribeToEvents(command="") {
	subscribe(settings["contact$command"], "contact.open",  "eventHandler$command")
	subscribe(settings["contactClosed$command"], "contact.closed",  "eventHandler$command")
	subscribe(settings["acceleration$command"], "acceleration.active",  "eventHandler$command")
	subscribe(settings["motion$command"], "motion.active",  "eventHandler$command")
	subscribe(settings["mySwitch$command"], "switch.on", "eventHandler$command")
	subscribe(settings["mySwitchOff$command"], "switch.off", "eventHandler$command")
	subscribe(settings["arrivalPresence$command"], "presence.present",  "eventHandler$command")
	subscribe(settings["departurePresence$command"], "presence.not present",  "eventHandler$command")
	subscribe(settings["smoke$command"], "smoke.detected",  "eventHandler$command")
	subscribe(settings["smoke$command"], "smoke.tested",  "eventHandler$command")
	subscribe(settings["smoke$command"], "carbonMonoxide.detected",  "eventHandler$command")
	subscribe(settings["water$command"], "water.wet",  "eventHandler$command")
	subscribe(settings["lock$command"], "lock.lock",  "eventHandler$command")
	subscribe(settings["lockLock$command"], "lock.unlock",  "eventHandler$command")
	subscribe(settings["button1$command"], "button.pushed",  "eventHandler$command")
    
 

	if (settings["timeOfDay$command"]) {
        schedule(settings["timeOfDay$command"], "eventHandler$command")
    }
	if (settings["triggerModes$command"]) {
        subscribe(location, "eventHandler$command")
    }

    if (command == "")
    {
    	
        if (onPlay && sonos){
            subscribe(sonos, "touch.play",  "eventHandler$command")
        }
        subscribe(app, appTouchHandler)

		if (song) {
            saveSelectedSong()
        }

        loadText()

        if (radionomyM) {
            state.radionomyM = radionomyM.sort()
            state.lastRTS = state.radionomyM[-1]
        }
    }
}

def eventHandler(evt=null) {
    def modeOk = true
    if(evt?.name == "mode"){
    	if (!(evt.value in triggerModes)) {
			modeOk = false
		}
    }
    log.trace "modeOk $modeOk"
    
	if (allOk && modeOk ) {
		log.trace "allOk"
		def lastTime = state[frequencyKey(evt)]
		if (oncePerDayOk(lastTime)) {
			if (frequency) {
				if (lastTime == null || now() - lastTime >= frequency * 60000) {
					takeAction(evt)
				}
				else {
					log.debug "Not taking action because $frequency minutes have not elapsed since last action"
				}
			}
			else {
				takeAction(evt)
			}
		}
		else {
			log.debug "Not taking action because it was already taken today"
		}
	}
}

def eventHandlerStop(evt) {
	def modeOk = true
    if(evt?.name == "mode"){
    	if (!(evt.value in triggerModesStop)) {
			modeOk = false
		}
    }
    
    if (allOk && modeOk) {
		log.trace "allOk"
				sonos.stop()
	}
}


def appTouchHandler(evt) {
	takeAction(evt)
}

private takeAction(evt) {

    def radionomyStations = [["70s 4u":["key":"4u-70s","artURI":"https://i3.radionomy.com/radios/400/e9fa1d18-12eb-4202-b2b5-5bb9de4d7110.jpg","description":"70s 4u"]],["80s 4u":["key":"4u-80s","artURI":"https://i3.radionomy.com/radios/400/6d704e4a-3648-46c5-99e2-c09dad8a4b6b.jpg","description":"80s 4u"]],["A 1 All Disney On Wide Radius":["key":"a-1alldisneyonwideradius","artURI":"https://i3.radionomy.com/radios/400/7b0422c9-b5fe-43ab-ab88-d4bc7a8e12bf.jpg","description":"A 1 All Disney On Wide Radius"]],["A Better Christian Station":["key":"a-better-christian-station","artURI":"https://i3.radionomy.com/radios/400/e070105a-b8c4-46cd-a862-443eed752131.png","description":"A Better Christian Station"]],["A Better Classic Blues Vintage Station":["key":"a-better-classic-blues-vintage-station","artURI":"https://i3.radionomy.com/radios/400/e0c2baeb-a182-449c-bc40-033605b778b2.png","description":"A Better Classic Blues Vintage Station"]],["A Better Classic Rock Station":["key":"a-better-classic-rock-station","artURI":"https://i3.radionomy.com/radios/400/a7b7e31d-dfb3-41d6-95c0-2d4618ad94a0.png","description":"A Better Classic Rock Station"]],["A Better Classical Station":["key":"a-better-classical-station","artURI":"https://i3.radionomy.com/radios/400/b642f1d1-ff97-435b-b491-b72bd14101ed.png","description":"A Better Classical Station"]],["A Better Love Songs Station":["key":"a-better-love-songs-station","artURI":"https://i3.radionomy.com/radios/400/7a5a93a5-7c55-46dd-be18-7f2a602d95fa.png","description":"A Better Love Songs Station"]],["A Blues Dream Classic & New Blues 24h":["key":"abluesdream-classic-newblues24h","artURI":"https://i3.radionomy.com/radios/400/173e9156-fe49-4b1d-ab0b-c57033643c65.jpg","description":"A Blues Dream Classic & New Blues 24h"]],["A I R Radio Freestyle Slow":["key":"airradiofreestyleslow","artURI":"https://i3.radionomy.com/radios/400/72c08557-46ae-458d-8b73-fa7269c923c5.jpg","description":"A I R Radio Freestyle Slow"]],["A Jazz Dream Classic & New Jazz 24h":["key":"ajazzdream-classic-newjazz24h","artURI":"https://i3.radionomy.com/radios/400/ce2b5d0a-8e29-449c-ae63-2ec766a0beea.jpg","description":"A Jazz Dream Classic & New Jazz 24h"]],["A Lounge Dream Relax 24h":["key":"aloungedream-relax24h","artURI":"https://i3.radionomy.com/radios/400/3062eddb-fa11-437e-b087-307f18c9e72c.jpg","description":"A Lounge Dream Relax 24h"]],["A Slow Dream Rnb Soul 24h":["key":"aslowdream-rnbsoul24h","artURI":"https://i3.radionomy.com/radios/400/e3d49554-3298-4208-a29c-729b7da35203.jpg","description":"A Slow Dream Rnb Soul 24h"]],["Aaa Soul":["key":"aaa-soul","artURI":"https://i3.radionomy.com/radios/400/143dff74-42a8-43a8-aab4-6416f5d7bfd4.jpg","description":"Aaa Soul"]],["Abaco Libros Y Cafe Radio":["key":"abaco-libros-y-cafe-radio","artURI":"https://i3.radionomy.com/radios/400/6198c640-54d7-4158-8ed2-2910af286308.png","description":"Abaco Libros Y Cafe Radio"]],["Abacus Fm Bach":["key":"abacusfm-bach","artURI":"https://i3.radionomy.com/radios/400/df2e0bc8-43c7-4766-926a-37f2a66ab527.jpg","description":"Abacus Fm Bach"]],["Abacus Fm Beethoven One":["key":"abacusfm-beethoven-one","artURI":"https://i3.radionomy.com/radios/400/24a34658-a384-4d47-9512-180c855388c9.jpg","description":"Abacus Fm Beethoven One"]],["Abacus Fm Chopin":["key":"abacusfmchopin","artURI":"https://i3.radionomy.com/radios/400/543bd505-d716-45ab-8618-4e97bfb1083f.jpg","description":"Abacus Fm Chopin"]],["Abacus Fm Mozart Piano":["key":"abacusfm-mozart-piano","artURI":"https://i3.radionomy.com/radios/400/554cf332-7363-4629-a786-cf02290feb52.jpg","description":"Abacus Fm Mozart Piano"]],["Abacus Fm Mozart Symphony":["key":"abacusfm-mozart-symphony","artURI":"https://i3.radionomy.com/radios/400/9fe3af87-3c25-49fa-b324-76b2faa268fe.jpg","description":"Abacus Fm Mozart Symphony"]],["Abacus Fm Nature":["key":"abacusfm-nature","artURI":"https://i3.radionomy.com/radios/400/04041c84-31af-45d2-ac67-f8778352fed7.png","description":"Abacus Fm Nature"]],["Abacus Fm Smooth Jazz":["key":"abacusfmsmoothjazz","artURI":"https://i3.radionomy.com/radios/400/6f2050e2-b3b2-4246-8a95-29e4bab0e43c.png","description":"Abacus Fm Smooth Jazz"]],["Abacus Fm Vintage Jazz":["key":"abacusfm-vintage-jazz","artURI":"https://i3.radionomy.com/radios/400/d86503b6-5f94-425f-b258-a75686610051.jpg","description":"Abacus Fm Vintage Jazz"]],["Abalone":["key":"abalone","artURI":"https://i3.radionomy.com/radios/400/0cc98719-3a9b-4166-b477-70922abe8d3a.jpg","description":"Abalone"]],["Abc Classic":["key":"abc-classic","artURI":"https://i3.radionomy.com/radios/400/6fa656c3-1d22-4e0f-a269-72ca7c71b385.jpg","description":"Abc Classic"]],["Abc Jazz":["key":"abc-jazz","artURI":"https://i3.radionomy.com/radios/400/07bf66bf-fe82-4586-aa93-b53466827b45.jpeg","description":"Abc Jazz"]],["Abc Lounge":["key":"abc-lounge","artURI":"https://i3.radionomy.com/radios/400/e4e1f437-4350-4d5d-a0c2-a93b4d23f3fd.png","description":"Abc Lounge"]],["Abc Love":["key":"abc-love","artURI":"https://i3.radionomy.com/radios/400/697a534e-a139-45e1-8bca-b1108be32a5d.jpg","description":"Abc Love"]],["Abc Piano":["key":"abc-piano","artURI":"https://i3.radionomy.com/radios/400/bff35c96-8e0e-4c1b-9206-7ed3c5a9921e.jpg","description":"Abc Piano"]],["Abc Relax":["key":"abcrelax","artURI":"https://i3.radionomy.com/radios/400/ae8b5709-c336-4404-a5de-cfe3503dfa73.png","description":"Abc Relax"]],["Abc Symphony":["key":"abc-symphony","artURI":"https://i3.radionomy.com/radios/400/149454ad-e5bd-4a73-8adc-470eaf30994d.jpg","description":"Abc Symphony"]],["Abonni Cafe":["key":"abonnicafe","artURI":"https://i3.radionomy.com/radios/400/c654ca2e-7103-4863-83eb-fa9e50d26204.jpg","description":"Abonni Cafe"]],["Absolute Chillout":["key":"absolutechillout","artURI":"https://i3.radionomy.com/radios/400/ab56377f-4bfc-4efe-9959-5f59dd992048.jpg","description":"Absolute Chillout"]],["Aclassicfm Web":["key":"aclassicfm-web","artURI":"https://i3.radionomy.com/radios/400/c5f20273-70ff-48d3-b4cb-d4e17576d687.jpg","description":"Aclassicfm Web"]],["Acoustic Fm":["key":"acoustic-fm","artURI":"https://i3.radionomy.com/radios/400/3fa9cbc2-b21d-4ad7-9a5e-b7c60837e3eb.jpg","description":"Acoustic Fm"]],["Acoustic Guitar":["key":"acoustic-guitar","artURI":"https://i3.radionomy.com/radios/400/0b3ca480-a9c3-4e32-aa3b-09c497b6d37f.jpg","description":"Acoustic Guitar"]],["Adorale":["key":"adorale","artURI":"https://i3.radionomy.com/radios/400/41d94867-0227-469b-8120-8e92e97fd90e.jpg","description":"Adorale"]],["Adventistinternetradio":["key":"adventistinternetradio","artURI":"https://i3.radionomy.com/radios/400/895d5d33-5885-4e9c-aa3a-8d92e271c7ee.jpg","description":"Adventistinternetradio"]],["Air Jazz":["key":"air-jazz","artURI":"https://i3.radionomy.com/radios/400/3dfa939a-1477-4e53-a058-7e01342fb162.jpg","description":"Air Jazz"]],["Air Lounge":["key":"air-lounge","artURI":"https://i3.radionomy.com/radios/400/46facdd7-38aa-441b-9f4d-7287ff80cb19.jpg","description":"Air Lounge"]],["Alabanza Digital":["key":"alabanza-digital","artURI":"https://i3.radionomy.com/radios/400/d8ff6acb-da59-4346-a411-00a1a187d862.png","description":"Alabanza Digital"]],["All 60s All The Time Keepfreemusic Com":["key":"all60sallthetime-keepfreemusiccom","artURI":"https://i3.radionomy.com/radios/400/359538cf-c5a9-435f-abdf-5cd1b5c75b62.jpg","description":"All 60s All The Time Keepfreemusic Com"]],["All Beethoven On Wide Radius":["key":"allbeethovenonwideradius","artURI":"https://i3.radionomy.com/radios/400/854445cb-29d9-4739-861d-fbb0864da3af.jpg","description":"All Beethoven On Wide Radius"]],["All Jazz Radio":["key":"all-jazz-radio","artURI":"https://i3.radionomy.com/radios/400/dd1c1e7b-70a1-4909-a122-73b5ac32fb77.jpg","description":"All Jazz Radio"]],["All Piano Radio":["key":"all-piano-radio","artURI":"https://i3.radionomy.com/radios/400/f1b866cc-df0d-40fb-89b1-9eb4b8e46b4d.jpg","description":"All Piano Radio"]],["All Smooth Jazz":["key":"all-smooth-jazz","artURI":"https://i3.radionomy.com/radios/400/2f9ed072-d68f-484c-b31f-230d284ecdcb.jpg","description":"All Smooth Jazz"]],["Allegro Jazz":["key":"allegro-jazz","artURI":"https://i3.radionomy.com/radios/400/23f5ab68-531b-41a6-b6c0-0302472ece5c.jpg","description":"Allegro Jazz"]],["Alpenradio|volksmusik":["key":"alpenradio-volksmusik","artURI":"https://i3.radionomy.com/radios/400/4d1e1e24-6b78-44af-a7af-60dabd97b81d.png","description":"Alpenradio|volksmusik"]],["Ambiance Classique":["key":"ambiance-classique","artURI":"https://i3.radionomy.com/radios/400/9a7bc2ca-674d-4ce0-aa40-bdf81db040f4.jpg","description":"Ambiance Classique"]],["Ambiance Lounge":["key":"ambiance-lounge","artURI":"https://i3.radionomy.com/radios/400/605a9db6-19f1-4c14-8e6d-bb72758800c2.jpg","description":"Ambiance Lounge"]],["Ambient And Lounge":["key":"ambient-and-lounge","artURI":"https://i3.radionomy.com/radios/400/46ba1ce3-ab6f-4775-aec3-376b6a6b6829.jpg","description":"Ambient And Lounge"]],["American Roots":["key":"americanroots","artURI":"https://i3.radionomy.com/radios/400/fc6c6c3d-bf1c-43cd-8092-817e84dc9bcb.jpg","description":"American Roots"]],["Anime Extremo Net":["key":"animeextremonet","artURI":"https://i3.radionomy.com/radios/400/e1a274e2-fffc-400f-b862-b8f3c94072b9.png","description":"Anime Extremo Net"]],["Animusique":["key":"animusique","artURI":"https://i3.radionomy.com/radios/400/0ddf9941-70a6-48b4-a33a-4cca4271cc17.jpg","description":"Animusique"]],["Anunciandoelreinoradiocatolica Raphy Rey":["key":"anunciandoelreinoradiocatolica-raphy-rey","artURI":"https://i3.radionomy.com/radios/400/c8f940b2-717e-49ff-9423-81efd2833866.jpg","description":"Anunciandoelreinoradiocatolica Raphy Rey"]],["Atomik Radio":["key":"atomik_radio","artURI":"https://i3.radionomy.com/radios/400/fce739b2-1ec7-4556-aaad-f15c8c9fffbd.jpg","description":"Atomik Radio"]],["Azur Blues":["key":"azur-blues","artURI":"https://i3.radionomy.com/radios/400/31a8581f-f000-489a-9aaf-11ea86f374c0.jpg","description":"Azur Blues"]],["Bachata Dominicana":["key":"bachata-dominicana","artURI":"https://i3.radionomy.com/radios/400/0b119796-693d-44c4-97e7-748443d2d07d.jpg","description":"Bachata Dominicana"]],["Baladas Romanticas Radio":["key":"baladasromanticasradio","artURI":"https://i3.radionomy.com/radios/400/ab8031cd-6fec-4b7a-91ae-4cdb2aabbaef.jpg","description":"Baladas Romanticas Radio"]],["Barock Music":["key":"barock-music","artURI":"https://i3.radionomy.com/radios/400/e7427c6f-c075-4b79-8242-4ca41fa92e2a.jpg","description":"Barock Music"]],["Beautiful Music 101":["key":"beautifulmusic101","artURI":"https://i3.radionomy.com/radios/400/932214a6-5b9b-4817-8844-e7b9227452a2.jpg","description":"Beautiful Music 101"]],["Beethoven Radio":["key":"beethoven-radio","artURI":"https://i3.radionomy.com/radios/400/3f6b0238-bf48-4f38-a27c-191ed5c2f3b1.jpg","description":"Beethoven Radio"]],["Best Blues I Know":["key":"bestbluesiknow","artURI":"https://i3.radionomy.com/radios/400/236063d1-17ab-4b88-b825-874b78b539fb.jpg","description":"Best Blues I Know"]],["Best Of Slow":["key":"best-of-slow","artURI":"https://i3.radionomy.com/radios/400/1fc36fdc-efb9-48e3-83b5-fd209deea70d.jpg","description":"Best Of Slow"]],["Bestclubdance":["key":"bestclubdance","artURI":"https://i3.radionomy.com/radios/400/11f6de98-3f15-4245-91b7-cd984b5161a9.jpg","description":"Bestclubdance"]],["Big Band Magic":["key":"bigbandmagic","artURI":"https://i3.radionomy.com/radios/400/717fa084-749e-4640-8371-063504bd065a.jpg","description":"Big Band Magic"]],["Bob Marley":["key":"bob-marley","artURI":"https://i3.radionomy.com/radios/400/240d9432-d8d8-4444-b4c7-f55697c5f503.png","description":"Bob Marley"]],["Boozik Jazzy":["key":"boozik-jazzy","artURI":"https://i3.radionomy.com/radios/400/09c2fafd-1186-4aaf-b1c0-7b5e4c5a6d31.png","description":"Boozik Jazzy"]],["Cafe Romantico Radio":["key":"cafe-romantico-radio","artURI":"https://i3.radionomy.com/radios/400/4ca4c2c3-50e7-4410-9875-8c6c7a422014.jpg","description":"Cafe Romantico Radio"]],["Cantico Nuevo Stereo":["key":"canticonuevostereo","artURI":"https://i3.radionomy.com/radios/400/3711fe74-8079-47fe-a2ed-112967ba0518.png","description":"Cantico Nuevo Stereo"]],["Chill 100":["key":"100-chill","artURI":"https://i3.radionomy.com/radios/400/27d6729d-ca2a-4f93-9464-0034aa8b3c16.jpg","description":"Chill 100"]],["Chill Out Radio Gaia":["key":"chill-out-radio-gaia","artURI":"https://i3.radionomy.com/radios/400/11fbb7fe-a986-4832-a9af-f2bcb745dfd9.jpg","description":"Chill Out Radio Gaia"]],["Chillout Classics":["key":"chillout-classics","artURI":"https://i3.radionomy.com/radios/400/5341f69b-120e-4989-adac-49eeb09bcef2.png","description":"Chillout Classics"]],["Chocolat Radio":["key":"chocolat-radio","artURI":"https://i3.radionomy.com/radios/400/54e83c76-2bc4-49fa-814d-140fe9380a34.png","description":"Chocolat Radio"]],["Christmas 1000":["key":"1000christmas","artURI":"https://i3.radionomy.com/radios/400/69cd8c97-d19c-485b-9751-f46b7fd85a8e.jpg","description":"Christmas 1000"]],["Christmas Wonderland Radio":["key":"christmaswonderlandradio","artURI":"https://i3.radionomy.com/radios/400/4e2b9376-e115-4657-ad33-250f117c3069.png","description":"Christmas Wonderland Radio"]],["Chronicles Christian Radio":["key":"chronicles-christian-radio","artURI":"https://i3.radionomy.com/radios/400/fc3dc765-b64b-4822-837d-7e93e910745e.jpg","description":"Chronicles Christian Radio"]],["Cinemix":["key":"cinemix","artURI":"https://i3.radionomy.com/radios/400/e32b5e31-0ebe-42e8-bb90-6aa0dff1f787.jpg","description":"Cinemix"]],["Cinescore":["key":"cinescore","artURI":"https://i3.radionomy.com/radios/400/0f8ea7f3-8fd2-4a56-85e3-18bf80506c26.jpg","description":"Cinescore"]],["Classic Rock #1":["key":"-1classicrock","artURI":"https://i3.radionomy.com/radios/400/09b89235-5a9f-4e54-aa37-a55b43e28294.png","description":"Classic Rock #1"]],["Classical Hits 1000":["key":"1000classicalhits","artURI":"https://i3.radionomy.com/radios/400/dbeca4c5-99f6-480c-a1f3-39b4a44f9c8a.jpg","description":"Classical Hits 1000"]],["Classical Jazz Radio":["key":"classicaljazzradio","artURI":"https://i3.radionomy.com/radios/400/19585ea0-3c67-4858-8d18-4c583121e15e.png","description":"Classical Jazz Radio"]],["Classicalmusicamerica Com":["key":"classicalmusicamericacom","artURI":"https://i3.radionomy.com/radios/400/0d33ac4e-688d-479a-8709-54a1f4dd1fb2.png","description":"Classicalmusicamerica Com"]],["Classicalways":["key":"classicalways","artURI":"https://i3.radionomy.com/radios/400/072f5bc6-3932-435e-ad79-af1f5308b236.jpg","description":"Classicalways"]],["Cocktelera Blues":["key":"cocktelera-blues","artURI":"https://i3.radionomy.com/radios/400/561586b6-1eac-4dac-a094-1a030fffb378.jpg","description":"Cocktelera Blues"]],["Colombiabohemia":["key":"colombiabohemia","artURI":"https://i3.radionomy.com/radios/400/e7937666-062a-4044-97e5-155eb29bc29e.jpg","description":"Colombiabohemia"]],["Colombiacrossover":["key":"colombiacrossover","artURI":"https://i3.radionomy.com/radios/400/0b21c519-f70f-4a93-b278-4c39f98ac7a5.jpg","description":"Colombiacrossover"]],["Colombiaromantica":["key":"colombiaromantica","artURI":"https://i3.radionomy.com/radios/400/50c3a2a1-1728-4967-a0f4-ba2e294bfe52.jpg","description":"Colombiaromantica"]],["Colombiasalsarosa":["key":"colombiasalsarosa","artURI":"https://i3.radionomy.com/radios/400/3dc39dc4-181d-41f8-993e-14612d37c9bd.jpg","description":"Colombiasalsarosa"]],["Con Alma De Blues":["key":"con-alma-de-blues","artURI":"https://i3.radionomy.com/radios/400/3769d208-fc93-4ade-ba6c-7d19dba7a5a9.jpg","description":"Con Alma De Blues"]],["Cool Kids Radio":["key":"coolkidsradio","artURI":"https://i3.radionomy.com/radios/400/5d33658c-f48e-4bc1-a34c-d0d776869466.jpg","description":"Cool Kids Radio"]],["Cristalrelax":["key":"cristalrelax","artURI":"https://i3.radionomy.com/radios/400/9467e7e9-9ee6-48b7-90b2-b4d832563d84.jpg","description":"Cristalrelax"]],["Cumbias Inmortales":["key":"cumbias-inmortales","artURI":"https://i3.radionomy.com/radios/400/391d4165-ee5b-4447-a970-9f0713c1bef7.png","description":"Cumbias Inmortales"]],["Dance90":["key":"dance90","artURI":"https://i3.radionomy.com/radios/400/a24fd78c-3512-417d-83aa-67f87401762d.jpg","description":"Dance90"]],["Dis Cover Radio":["key":"dis--cover-radio","artURI":"https://i3.radionomy.com/radios/400/5fee7213-893d-4dce-8d35-707439e4fb16.jpg","description":"Dis Cover Radio"]],["Dlrpradio":["key":"dlrpradio","artURI":"https://i3.radionomy.com/radios/400/9b7a0890-b0d8-4438-bc30-7b44942b7cee.jpg","description":"Dlrpradio"]],["Dream Baby":["key":"dreambaby","artURI":"https://i3.radionomy.com/radios/400/e9aa4c40-64af-4a9e-bb2c-7b0731efa2b0.png","description":"Dream Baby"]],["Elium Rock":["key":"elium-rock","artURI":"https://i3.radionomy.com/radios/400/fbea3889-959a-4735-86ba-8446d1c5ce20.png","description":"Elium Rock"]],["Enchantedradio":["key":"enchantedradio","artURI":"https://i3.radionomy.com/radios/400/83c2bff8-ddb9-4720-9b08-a86baa38fa77.png","description":"Enchantedradio"]],["Energiafmonline":["key":"energiafmonline","artURI":"https://i3.radionomy.com/radios/400/22dbd426-c106-4ca4-9741-8de96e6c89fa.jpg","description":"Energiafmonline"]],["Enjoystation":["key":"EnjoyStation","artURI":"https://i3.radionomy.com/radios/400/96f07fcc-5b34-4afd-94ff-0148eff61c5a.jpg","description":"Enjoystation"]],["Esperanza 7":["key":"esperanza-7","artURI":"https://i3.radionomy.com/radios/400/f7456f4b-ed61-43d7-9baa-7009beff924b.jpg","description":"Esperanza 7"]],["Eure Du Blues":["key":"euredublues","artURI":"https://i3.radionomy.com/radios/400/04e20969-e960-4df2-914b-6ea88ba1356f.jpg","description":"Eure Du Blues"]],["F A Radio Animes":["key":"-f-a-radio-animes","artURI":"https://i3.radionomy.com/radios/400/42aa3bbe-7b37-4bc7-83fa-3523cddb434d.png","description":"F A Radio Animes"]],["Feeling Love":["key":"feeling-love","artURI":"https://i3.radionomy.com/radios/400/5983468e-ed0b-497c-b75f-cb62266394ba.jpg","description":"Feeling Love"]],["Feeling The Blues":["key":"feelingtheblues","artURI":"https://i3.radionomy.com/radios/400/dfdaf3ce-e15b-477e-8d9c-636620b089cd.jpg","description":"Feeling The Blues"]],["Feevermix":["key":"feevermix","artURI":"https://i3.radionomy.com/radios/400/3bcdd76a-9277-4a84-bbfb-0c462a92be88.jpg","description":"Feevermix"]],["Frequences Relaxation":["key":"frequences-relaxation","artURI":"https://i3.radionomy.com/radios/400/6f82e397-3cf8-4242-9be0-aa2b9cf23c2a.jpg","description":"Frequences Relaxation"]],["Funadosong":["key":"funadosong","artURI":"https://i3.radionomy.com/radios/400/a8dddb18-5c97-4a63-9aec-53ff9a10204b.jpg","description":"Funadosong"]],["Funky Blues":["key":"funkyblues","artURI":"https://i3.radionomy.com/radios/400/f605dafe-ff75-4388-a41b-a03e0ee44a9a.png","description":"Funky Blues"]],["Generikids":["key":"generikids","artURI":"https://i3.radionomy.com/radios/400/a9e9597d-5d91-48ec-a68a-6c5e7dc0a728.jpg","description":"Generikids"]],["Gupshupcorner Com":["key":"gupshupcornercom","artURI":"https://i3.radionomy.com/radios/400/a06b6831-2466-4d2a-b1f0-4a081d534270.png","description":"Gupshupcorner Com"]],["Halloweenradio Net Kids":["key":"halloweenradionet-kids","artURI":"https://i3.radionomy.com/radios/400/2752fb12-3a8d-486d-bcc9-59ea7ef969a9.jpg","description":"Halloweenradio Net Kids"]],["Healing Music Radio":["key":"healing-music-radio","artURI":"https://i3.radionomy.com/radios/400/a46a8f01-94ec-4a5f-8183-5d2bdb64d3b0.jpg","description":"Healing Music Radio"]],["Hippie Soul Radio":["key":"hippiesoulradio","artURI":"https://i3.radionomy.com/radios/400/ef6675f6-eb43-4414-a556-ffd48e476a2a.png","description":"Hippie Soul Radio"]],["Hit Radio 100":["key":"100-hit-radio","artURI":"https://i3.radionomy.com/radios/400/e6b3f820-49ce-4145-a74b-fd006cd7dfb0.jpg","description":"Hit Radio 100"]],["Hit Radio Rocky Schlager":["key":"hitradiorockyschlager","artURI":"https://i3.radionomy.com/radios/400/1e1fa0a1-599f-44ec-8b20-970fb80fc5f8.jpg","description":"Hit Radio Rocky Schlager"]],["Hits #1":["key":"-1hits","artURI":"https://i3.radionomy.com/radios/400/6382c82e-fc7f-46d3-b8ed-f679f653ba4f.jpg","description":"Hits #1"]],["Hits 70s #1":["key":"1hits70s","artURI":"https://i3.radionomy.com/radios/400/91cad0d3-649b-40ea-be1c-5ff6b341e187.jpg","description":"Hits 70s #1"]],["Hits 80s #1":["key":"1hits80s","artURI":"https://i3.radionomy.com/radios/400/cf0801cb-5de7-4037-be65-55b69842029f.jpg","description":"Hits 80s #1"]],["Hits Classical Music 1000":["key":"1000hitsclassicalmusic","artURI":"https://i3.radionomy.com/radios/400/c8fcd024-7e46-4b27-86fc-40ad3e0450e4.png","description":"Hits Classical Music 1000"]],["Hits My Music Zen":["key":"hit-s-my-music-zen","artURI":"https://i3.radionomy.com/radios/400/c9070eac-c93a-49a0-bb16-ead3aea656d9.jpg","description":"Hits My Music Zen"]],["Hits Sweet Radio 1000":["key":"1000-hits-sweet-radio","artURI":"https://i3.radionomy.com/radios/400/8f0a2e89-42a8-48bf-a629-1665c501b2bb.jpg","description":"Hits Sweet Radio 1000"]],["Hot 40 Music":["key":"hot40music","artURI":"https://i3.radionomy.com/radios/400/5158453a-6eb7-4307-9395-2c0079f9a3dd.png","description":"Hot 40 Music"]],["Hotmixradio 80":["key":"hotmixradio-80-128","artURI":"https://i3.radionomy.com/radios/400/2bf6e407-d028-4621-bc64-8964fa5e1d91.png","description":"Hotmixradio 80"]],["Hotmixradio 90":["key":"hotmixradio-90-128","artURI":"https://i3.radionomy.com/radios/400/b47f1666-c838-4231-ac36-ed1317d91d95.png","description":"Hotmixradio 90"]],["Hotmixradio Baby":["key":"hotmixradio-baby-128","artURI":"https://i3.radionomy.com/radios/400/f67022c7-e395-4678-929c-cd8ad90d4c91.png","description":"Hotmixradio Baby"]],["Hotmixradio Hits":["key":"hotmixradio-hits-128","artURI":"https://i3.radionomy.com/radios/400/8c5336b5-4818-4c22-80bc-874581587d1c.png","description":"Hotmixradio Hits"]],["Hotmixradio Rock":["key":"hotmixradio-rock-128","artURI":"https://i3.radionomy.com/radios/400/e53f5398-83cb-413f-a217-7588914a710c.png","description":"Hotmixradio Rock"]],["Illayarajavin Thevitatha Padalgal!":["key":"illayarajavinthevitathapadalgal-","artURI":"https://i3.radionomy.com/radios/400/d56cd94c-f969-4b6e-9c5a-74d37ffd849b.jpg","description":"Illayarajavin Thevitatha Padalgal!"]],["Instrumental Hits":["key":"instrumental-hits","artURI":"https://i3.radionomy.com/radios/400/d7d34d0a-3e01-4f2e-a4f9-ff0e9d9f33bd.jpg","description":"Instrumental Hits"]],["Instrumentals Forever":["key":"instrumentals-forever","artURI":"https://i3.radionomy.com/radios/400/352d97f2-3795-4920-864d-873bba4f759b.jpg","description":"Instrumentals Forever"]],["Intimisima Radio":["key":"intimisimaradio","artURI":"https://i3.radionomy.com/radios/400/5e8ce94c-fecc-4fca-b853-39a634408bc5.jpg","description":"Intimisima Radio"]],["Jamaican Roots Radio":["key":"jamaican-roots-radio","artURI":"https://i3.radionomy.com/radios/400/9e37a37b-80d8-4bdd-9407-f21ddbaaff87.png","description":"Jamaican Roots Radio"]],["Jazz Light":["key":"jazz-light","artURI":"https://i3.radionomy.com/radios/400/de35e244-b444-4df6-9b6e-d5b7b7c5a5c4.jpg","description":"Jazz Light"]],["Jazz Lovers Radio":["key":"jazzlovers","artURI":"https://i3.radionomy.com/radios/400/e30d917c-6914-41fa-bb81-a8aae9cf3267.png","description":"Jazz Lovers Radio"]],["Jazz Swing Manouche Radio":["key":"jazz-swing-manouche-radio","artURI":"https://i3.radionomy.com/radios/400/693ff831-ed35-4898-8cbb-2b6df182297a.jpg","description":"Jazz Swing Manouche Radio"]],["Jazz Vespers Radio":["key":"jazzvespersradio","artURI":"https://i3.radionomy.com/radios/400/1ae278f6-ea2f-4298-9f7e-5ecac3dc2cb3.png","description":"Jazz Vespers Radio"]],["Jazzclub":["key":"jazzclub","artURI":"https://i3.radionomy.com/radios/400/a1719ba1-ca0b-4149-8612-e79ef1cf9b0a.jpg","description":"Jazzclub"]],["K Easy":["key":"k-easy","artURI":"https://i3.radionomy.com/radios/400/94816175-e081-405f-a8d3-07a393398645.jpg","description":"K Easy"]],["Kalasam Com":["key":"kalasamcom","artURI":"https://i3.radionomy.com/radios/400/78f60f02-cffb-4a3b-973f-4b6df66dea0c.png","description":"Kalasam Com"]],["Lagujawa":["key":"lagujawa","artURI":"https://i3.radionomy.com/radios/400/b47b8539-1756-4406-8b86-6c2cfb4e085f.jpg","description":"Lagujawa"]],["Las Grandes Bandas Radio":["key":"lasgrandesbandasradio","artURI":"https://i3.radionomy.com/radios/400/f915b2fa-a0d0-474f-ae97-2f6586ce70cc.jpg","description":"Las Grandes Bandas Radio"]],["Latina 104 Web":["key":"latina-104web","artURI":"https://i3.radionomy.com/radios/400/d94e6eee-8712-489c-8d43-98322e265766.jpg","description":"Latina 104 Web"]],["Ledjam Radio":["key":"ledjamradio.mp3","artURI":"https://i3.radionomy.com/radios/400/704f3bbf-3500-4ae9-91da-984653ed5984.jpg","description":"Ledjam Radio"]],["Les Grands Fan De Disney Radio":["key":"lesgrandsfandedisneyradio","artURI":"https://i3.radionomy.com/radios/400/904dd749-7b72-415b-92f0-f43fc9613934.png","description":"Les Grands Fan De Disney Radio"]],["Los Grandes Grupos Radio":["key":"losgrandesgruposradio","artURI":"https://i3.radionomy.com/radios/400/865fa1ad-4471-4207-a183-42cbc780d3b6.jpg","description":"Los Grandes Grupos Radio"]],["Love Love Radio 2":["key":"love-2-love-radio","artURI":"https://i3.radionomy.com/radios/400/7b39d66d-9d0b-4855-b337-bb8bd6d30b89.jpg","description":"Love Love Radio 2"]],["Love Radio":["key":"love-radio","artURI":"https://i3.radionomy.com/radios/400/23d4546f-34a8-4d24-be01-00a88fb471f3.png","description":"Love Radio"]],["Love Radio":["key":"-loveradio","artURI":"https://i3.radionomy.com/radios/400/c3fb5ea3-f19d-40c6-bdf9-9da54de71cb0.jpg","description":"Love Radio"]],["Love Radio 2":["key":"2loveradio","artURI":"https://i3.radionomy.com/radios/400/f5b2ccea-dfe8-4ddd-9b24-5a72e2272349.png","description":"Love Radio 2"]],["Lovehitsradio":["key":"lovehitsradio","artURI":"https://i3.radionomy.com/radios/400/d1a22037-6db1-4991-8c18-e0bd4ee3d449.png","description":"Lovehitsradio"]],["Lovemixradio":["key":"LOVEMIXRADIO","artURI":"https://i3.radionomy.com/radios/400/9b0a3c68-d676-4259-8a42-cb5da9fe5cdb.jpg","description":"Lovemixradio"]],["Made In Classic":["key":"made-in-classic","artURI":"https://i3.radionomy.com/radios/400/6f10f0f5-081c-4745-adbf-f4408d1f7037.png","description":"Made In Classic"]],["Martini In The Morning":["key":"MartiniintheMorning","artURI":"https://i3.radionomy.com/radios/400/8b5cec18-2fcb-4276-9e67-4e19c85fab77.png","description":"Martini In The Morning"]],["Martini In The Morning (64k)":["key":"martiniinthemorning-64k-","artURI":"https://i3.radionomy.com/radios/400/bf8ec572-0d80-402c-8dbc-a7688878e409.png","description":"Martini In The Morning (64k)"]],["Martini In The Morning (mp3)":["key":"martiniinthemorning-mp3-","artURI":"https://i3.radionomy.com/radios/400/b83b9333-6fbc-472e-ac6b-502a5ac5b2dc.png","description":"Martini In The Morning (mp3)"]],["Mocha":["key":"mocha","artURI":"https://i3.radionomy.com/radios/400/90a8bc99-0411-46c0-8953-ab2a855fe4f7.jpg","description":"Mocha"]],["Mozart":["key":"mozart","artURI":"https://i3.radionomy.com/radios/400/6e3674e5-fa32-4d49-8fd1-0c6de28303f7.jpg","description":"Mozart"]],["Mozartiana":["key":"mozartiana","artURI":"https://i3.radionomy.com/radios/400/c6f09745-88b2-4697-bfed-41a14a08fce0.jpg","description":"Mozartiana"]],["Musiconly Fm":["key":"musiconly-fm","artURI":"https://i3.radionomy.com/radios/400/3ec58c83-f373-42a7-9d8a-a411da651990.png","description":"Musiconly Fm"]],["Ocean Breeze":["key":"oceanbreeze","artURI":"https://i3.radionomy.com/radios/400/db1e4df4-f662-41fc-aeaa-b2c6f4aadba5.jpg","description":"Ocean Breeze"]],["Oldies 1000":["key":"1000oldies","artURI":"https://i3.radionomy.com/radios/400/29b9457b-ba68-4f21-89b0-0ee470d42677.png","description":"Oldies 1000"]],["Ourworld Pop":["key":"ourworld-pop","artURI":"https://i3.radionomy.com/radios/400/8ded59c2-86ba-4610-babb-c797bbe24063.png","description":"Ourworld Pop"]],["Panda Pop Radio":["key":"pandapopradio","artURI":"https://i3.radionomy.com/radios/400/c8ec486d-52f6-4ce7-81b1-ca9a0104679e.jpg","description":"Panda Pop Radio"]],["Panda Show Radio":["key":"pandashowradio","artURI":"https://i3.radionomy.com/radios/400/a4770fa6-32a3-4a12-b785-edbcbf27d468.jpg","description":"Panda Show Radio"]],["Pop Y Rock En Español":["key":"popyrockenespanol","artURI":"https://i3.radionomy.com/radios/400/f19042fa-6270-477c-8f9e-6a10c8009104.jpg","description":"Pop Y Rock En Español"]],["Powerclub Station":["key":"powerclub-station","artURI":"https://i3.radionomy.com/radios/400/8d5b7483-8216-444b-b102-6293b165bb23.png","description":"Powerclub Station"]],["Que Viva Mexico":["key":"quevivamexico","artURI":"https://i3.radionomy.com/radios/400/7c075522-ce77-48c9-ad5e-be51f339618a.jpg","description":"Que Viva Mexico"]],["Radio Animex (musica Anime Y Mucho Mas)":["key":"radioanimex-musicaanimeymuchomas-","artURI":"https://i3.radionomy.com/radios/400/f1833d4a-bdbf-4c69-8281-2386f2ec4859.png","description":"Radio Animex (musica Anime Y Mucho Mas)"]],["Radio Beats Fm":["key":"radiobeatsfm","artURI":"https://i3.radionomy.com/radios/400/819a70bd-3a8c-4f8e-85ec-87e1015d3512.jpg","description":"Radio Beats Fm"]],["Radio Cristiana Online":["key":"radio-cristiana-online","artURI":"https://i3.radionomy.com/radios/400/ad09d178-e581-455b-bbc5-85db6144d458.png","description":"Radio Cristiana Online"]],["Radio Dance #1":["key":"1-radio-dance","artURI":"https://i3.radionomy.com/radios/400/15e76aee-7d15-41b8-b332-7a78c93d155e.jpg","description":"Radio Dance #1"]],["Radio Hunter The Hitz Channel":["key":"radiohunter-thehitzchannel","artURI":"https://i3.radionomy.com/radios/400/423ab2d6-d0ed-4060-9aef-a61be8a077f2.png","description":"Radio Hunter The Hitz Channel"]],["Radio Junior":["key":"radio-junior","artURI":"https://i3.radionomy.com/radios/400/17f8091f-b27e-4981-908a-b0e9800893c7.gif","description":"Radio Junior"]],["Radio Love 141":["key":"141radiolove","artURI":"https://i3.radionomy.com/radios/400/e6a4c728-396f-4c9f-a040-0af7954fb3f3.jpg","description":"Radio Love 141"]],["Radio Mozart":["key":"radio-mozart","artURI":"https://i3.radionomy.com/radios/400/0be77c4c-1f16-4eb2-8b8e-bef9be469c22.jpg","description":"Radio Mozart"]],["Radio Nostalgia":["key":"radio-nostalgia","artURI":"https://i3.radionomy.com/radios/400/93883daa-4b9a-4784-8108-05e7081d0bbc.png","description":"Radio Nostalgia"]],["Radio Otakus Dream":["key":"radio-otakus-dream","artURI":"https://i3.radionomy.com/radios/400/6f9e2777-52cb-491a-9ab7-966c40d72bbf.png","description":"Radio Otakus Dream"]],["Radio Plenitude":["key":"radio-plenitude","artURI":"https://i3.radionomy.com/radios/400/5312f4b9-7cff-4c88-8ede-1325c29d122a.jpg","description":"Radio Plenitude"]],["Radio Stonata":["key":"radio-stonata","artURI":"https://i3.radionomy.com/radios/400/e04950d7-27d8-45ab-9cc9-ec44a940b477.png","description":"Radio Stonata"]],["Radio Tango Velours":["key":"radio-tango-velours","artURI":"https://i3.radionomy.com/radios/400/1dd65dca-c1d5-4764-97bd-934f50c0b79e.jpg","description":"Radio Tango Velours"]],["Radiomyme Tv":["key":"radiomyme-tv","artURI":"https://i3.radionomy.com/radios/400/f9db390a-41f9-4e6d-8662-faa069aeb984.png","description":"Radiomyme Tv"]],["Radionomix":["key":"RadionoMiX","artURI":"https://i3.radionomy.com/radios/400/b596f9e0-e86b-44a8-a34c-9fc4eb790916.jpg","description":"Radionomix"]],["Radiosky Music":["key":"radiosky-music","artURI":"https://i3.radionomy.com/radios/400/d43c9a34-838d-4f60-bb61-a17b41488275.jpg","description":"Radiosky Music"]],["Radiounoplus":["key":"radiounoplus","artURI":"https://i3.radionomy.com/radios/400/d178a8a4-51fc-40f5-ba50-7fabf887c20b.jpg","description":"Radiounoplus"]],["Revolution Fm":["key":"revolution-fm","artURI":"https://i3.radionomy.com/radios/400/5e8fe722-3f30-45d7-ab7d-9993cd637343.png","description":"Revolution Fm"]],["Rey De Corazones":["key":"rey-de-corazones","artURI":"https://i3.radionomy.com/radios/400/d9294d11-b87f-4aa1-a796-5811d6ce8885.jpg","description":"Rey De Corazones"]],["Rjm Jazzy":["key":"rjm-jazzy","artURI":"https://i3.radionomy.com/radios/400/c27eae59-c53e-43ef-9ea0-27e43408dd34.jpg","description":"Rjm Jazzy"]],["Rjm Lounge":["key":"rjm-lounge","artURI":"https://i3.radionomy.com/radios/400/cde9d7e3-2b79-4dab-ab66-a99ab24dbc7c.jpg","description":"Rjm Lounge"]],["Romance Vos Plus Belle Chanson Damour":["key":"romance-vos-plus-belle-chanson-d-amour","artURI":"https://i3.radionomy.com/radios/400/a9d53e42-d38a-4177-b813-b3d5927ea2e1.png","description":"Romance Vos Plus Belle Chanson Damour"]],["Romantica Digital":["key":"romanticadigital","artURI":"https://i3.radionomy.com/radios/400/c5e8e9da-db8b-4f2c-a9ac-3b84435a7319.jpg","description":"Romantica Digital"]],["Sertanejo":["key":"-sertanejo","artURI":"https://i3.radionomy.com/radios/400/1fdb9fef-e0fa-4b4b-bb78-b5318405fcce.jpg","description":"Sertanejo"]],["Slowradio Com":["key":"slowradiocom","artURI":"https://i3.radionomy.com/radios/400/113409e1-7e4b-4fa8-a036-e57a9e974e10.jpg","description":"Slowradio Com"]],["Smooth Jazz 101":["key":"101smoothjazz","artURI":"https://i3.radionomy.com/radios/400/98489309-a9cf-4d10-a4fb-9e744f10e764.jpg","description":"Smooth Jazz 101"]],["Smooth Jazz 247":["key":"smoothjazz247","artURI":"https://i3.radionomy.com/radios/400/84e397e7-fdc2-4afe-bb70-d23b658708b1.jpg","description":"Smooth Jazz 247"]],["Smooth Jazz 4u":["key":"4u-smooth-jazz","artURI":"https://i3.radionomy.com/radios/400/6890d244-c476-49fe-9c6e-a504d2a06fcb.jpg","description":"Smooth Jazz 4u"]],["Smooth Riviera":["key":"smooth-riviera","artURI":"https://i3.radionomy.com/radios/400/3ac5a46b-a7fd-41ff-9217-51934076fc67.jpg","description":"Smooth Riviera"]],["Soft Riw Love Channel 100%":["key":"100-softriwlovechannel","artURI":"https://i3.radionomy.com/radios/400/38d8fac9-f169-4a6b-bbe9-5766f1e894c5.jpg","description":"Soft Riw Love Channel 100%"]],["Sophisticated Easy Sounds":["key":"sophisticated-easy-sounds","artURI":"https://i3.radionomy.com/radios/400/2085171d-c082-4809-80c1-1ec0635bed77.jpeg","description":"Sophisticated Easy Sounds"]],["Sorcerer Radio Disney Park Music":["key":"sorcererradio-disneyparkmusic","artURI":"https://i3.radionomy.com/radios/400/ca54f75e-1111-4fa7-85bc-55fefcc04c35.jpg","description":"Sorcerer Radio Disney Park Music"]],["The Great 80s":["key":"thegreat80s","artURI":"https://i3.radionomy.com/radios/400/048b1026-0978-4a5d-9e75-000536891282.jpg","description":"The Great 80s"]],["Theneosoulcafe":["key":"theneosoulcafe","artURI":"https://i3.radionomy.com/radios/400/0570e783-7561-4853-9c61-a2845862cbac.png","description":"Theneosoulcafe"]],["Topclub":["key":"topclub","artURI":"https://i3.radionomy.com/radios/400/29eea19a-2b84-4da5-99f0-3628b74c40f9.jpg","description":"Topclub"]],["Trop Beautiful":["key":"trop-beautiful","artURI":"https://i3.radionomy.com/radios/400/c059c321-b5b4-42e5-8ce8-ac1b00be0f93.jpg","description":"Trop Beautiful"]],["Trova Radio El Sentimiento Hecho Música":["key":"trova-radio---el-sentimiento-hecho-musica","artURI":"https://i3.radionomy.com/radios/400/26967808-f30d-4135-9e3f-63ba750ff06e.jpg","description":"Trova Radio El Sentimiento Hecho Música"]],["True Oldies Channel":["key":"trueoldieschannel","artURI":"https://i3.radionomy.com/radios/400/af4fdbde-1068-4c01-96d5-af5fcbfddc0b.jpg","description":"True Oldies Channel"]],["Webradio Tirol":["key":"webradio-tirol","artURI":"https://i3.radionomy.com/radios/400/e8641ee3-0f36-42af-85e6-e9446aeb6434.png","description":"Webradio Tirol"]],["Wine Farm And Tourist Radio":["key":"winefarmandtouristradio","artURI":"https://i3.radionomy.com/radios/400/ae13a8af-b363-4ddf-bdb7-e342117ffc7d.jpg","description":"Wine Farm And Tourist Radio"]],["Zen For You":["key":"zen-for-you","artURI":"https://i3.radionomy.com/radios/400/4f2f24e2-d4bd-4ad0-956c-26045dbcb338.jpg","description":"Zen For You"]]]
    
    
    def speech
    def domain
    def partyList = ""

    party.each{
    	if(it?.currentUdn) partyList += (partyList?",":"")+ it.currentUdn
    }

    
    if (partyList){
    sonos[0].party(partyList)
    pause(1500)
	}

    
    switch(actionType) {
        case "Message":
			speech = safeTextToSpeech(normalizeMessage(message,evt))
            if(ttsMode == "Alexa" && !message.contains("#s")){
                sonos.playTrack(speech.uri)
            }else{
            	sonos.playTrackAndResume(speech.uri, speech.duration, volume)
            }
            break
        case "Sound":
			sonos.playTrackAndResume(state.sound.uri, state.sound.duration, volume)
            break
		case "Track":
        	if(state.selectedSong){
	            if (volume != null) {
			sonos.stop()
			pause(500)
			sonos.setLevel(volume)
			pause(500)
	            }
	           sonos.playTrack(state.selectedSong)
            }else{
            	speech = safeTextToSpeech("No Track was Selected")
				sonos.playTrackAndResume(speech.uri, speech.duration, volume)
            }
            break
        case "Radionomy":
            if (volume != null) {
		sonos.stop()
		pause(500)
		sonos.setLevel(volume)
		pause(500)
            }
            sonos.playTrack("x-rincon-mp3radio://listen.radionomy.com/${radionomyStations[radionomy].key[0]}?d=dHNVaWQ9ODIyM2JlMjItOTg0My00MTU3LWJjNjYtM2Q4NjhmNWI3MTM5JnVzZXJyZWY9ODIyM2JlMjItOTg0My00MTU3LWJjNjYtM2Q4NjhmNWI3MTM5JmFwcE5hbWU9d2Vic2l0ZSZhZD1yYWRpb25vd2Vi","<DIDL-Lite xmlns=\"urn:schemas-upnp-org:metadata-1-0/DIDL-Lite/\" xmlns:dc=\"http://purl.org/dc/elements/1.1/\" xmlns:upnp=\"urn:schemas-upnp-org:metadata-1-0/upnp/\" xmlns:dlna=\"urn:schemas-dlna-org:metadata-1-0/\"><item id=\"1\" parentID=\"1\" restricted=\"1\"><upnp:class>object.item.audioItem.audioBroadcast</upnp:class><upnp:album>Radionomy</upnp:album><upnp:artist>${groovy.xml.XmlUtil.escapeXml(radionomyStations[radionomy].description[0])}</upnp:artist><upnp:albumArtURI>${groovy.xml.XmlUtil.escapeXml(radionomyStations[radionomy].artURI[0])}</upnp:albumArtURI><dc:title>${groovy.xml.XmlUtil.escapeXml(radionomy)}</dc:title><res protocolInfo=\"http-get:*:audio/mpeg:DLNA.ORG_PN=MP3;DLNA.ORG_OP=01;DLNA.ORG_CI=0;DLNA.ORG_FLAGS=01500000000000000000000000000000\" >${groovy.xml.XmlUtil.escapeXml("x-rincon-mp3radio://listen.radionomy.com/${radionomyStations[radionomy].key[0]}")} </res></item> </DIDL-Lite>")

            break
        case "Multiple Radionomy":
			switch(RdoMode){
            	 case "Random": 
                    state.radionomyM.sort{Math.random()}
                    break
                case "Shuffle":
                    if (state.radionomyM[-1] == state.lastRTS) {
                        state.radionomyM.sort{Math.random()}
                        state.lastRTS = state.radionomyM[-1]
                    }
                    break
            }
            Collections.rotate(state.radionomyM, -1)
            if (volume != null) {
		sonos.stop()
		pause(500)
		sonos.setLevel(volume)
		pause(500)
            }

            sonos.playTrack("x-rincon-mp3radio://listen.radionomy.com/${radionomyStations[state.radionomyM[-1]].key[0]}?d=dHNVaWQ9ODIyM2JlMjItOTg0My00MTU3LWJjNjYtM2Q4NjhmNWI3MTM5JnVzZXJyZWY9ODIyM2JlMjItOTg0My00MTU3LWJjNjYtM2Q4NjhmNWI3MTM5JmFwcE5hbWU9d2Vic2l0ZSZhZD1yYWRpb25vd2Vi","<DIDL-Lite xmlns=\"urn:schemas-upnp-org:metadata-1-0/DIDL-Lite/\" xmlns:dc=\"http://purl.org/dc/elements/1.1/\" xmlns:upnp=\"urn:schemas-upnp-org:metadata-1-0/upnp/\" xmlns:dlna=\"urn:schemas-dlna-org:metadata-1-0/\"><item id=\"1\" parentID=\"1\" restricted=\"1\"><upnp:class>object.item.audioItem.audioBroadcast</upnp:class><upnp:album>Radionomy</upnp:album><upnp:artist>${groovy.xml.XmlUtil.escapeXml(radionomyStations[state.radionomyM[-1]].description[0])}</upnp:artist><upnp:albumArtURI>${groovy.xml.XmlUtil.escapeXml(radionomyStations[state.radionomyM[-1]].artURI[0])}</upnp:albumArtURI><dc:title>${groovy.xml.XmlUtil.escapeXml(state.radionomyM[-1])}</dc:title><res protocolInfo=\"http-get:*:audio/mpeg:DLNA.ORG_PN=MP3;DLNA.ORG_OP=01;DLNA.ORG_CI=0;DLNA.ORG_FLAGS=01500000000000000000000000000000\" >${groovy.xml.XmlUtil.escapeXml("x-rincon-mp3radio://listen.radionomy.com/${radionomyStations[state.radionomyM[-1]].key[0]}")} </res></item> </DIDL-Lite>")
            break
	}

	if (frequency || oncePerDay) {
		state[frequencyKey(evt)] = now()
	}
	log.trace "Exiting takeAction()"
}

private frequencyKey(evt) {
	"lastActionTimeStamp"
}

private dayString(Date date) {
	def df = new java.text.SimpleDateFormat("yyyy-MM-dd")
	if (location.timeZone) {
		df.setTimeZone(location.timeZone)
	}
	else {
		df.setTimeZone(TimeZone.getTimeZone("America/New_York"))
	}
	df.format(date)
}

private oncePerDayOk(Long lastTime) {
	def result = true
	if (oncePerDay) {
		result = lastTime ? dayString(new Date()) != dayString(new Date(lastTime)) : true
		log.trace "oncePerDayOk = $result"
	}
	result
}

// TODO - centralize somehow
private getAllOk() {
	modeOk && daysOk && timeOk
}

private getModeOk() {
	def result = !modes || modes.contains(location.mode)
	log.trace "modeOk = $result"
	result
}

private getDaysOk() {
	def result = true
	if (days) {
		def df = new java.text.SimpleDateFormat("EEEE")
		if (location.timeZone) {
			df.setTimeZone(location.timeZone)
		}
		else {
			df.setTimeZone(TimeZone.getTimeZone("America/New_York"))
		}
		def day = df.format(new Date())
		result = days.contains(day)
	}
	log.trace "daysOk = $result"
	result
}

private getTimeOk() {
	def result = true
	if (starting && ending) {
		def currTime = now()
		def start = timeToday(starting, location?.timeZone).time
		def stop = timeToday(ending, location?.timeZone).time
		result = start < stop ? currTime >= start && currTime <= stop : currTime <= stop || currTime >= start
	}
	log.trace "timeOk = $result"
	result
}

private hhmm(time, fmt = "h:mm a")
{
	def t = timeToday(time, location.timeZone)
	def f = new java.text.SimpleDateFormat(fmt)
	f.setTimeZone(location.timeZone ?: timeZone(time))
	f.format(t)
}

private getTimeLabel()
{
	(starting && ending) ? hhmm(starting) + "-" + hhmm(ending, "h:mm a z") : ""
}
// TODO - End Centralize

private loadText() {
	switch (sound) {
		case "Bell 1":
			state.sound = [uri: "http://s3.amazonaws.com/smartapp-media/sonos/bell1.mp3", duration: "10"]
			break;
		case "Bell 2":
			state.sound = [uri: "http://s3.amazonaws.com/smartapp-media/sonos/bell2.mp3", duration: "10"]
			break;
		case "Dogs Barking":
			state.sound = [uri: "http://s3.amazonaws.com/smartapp-media/sonos/dogs.mp3", duration: "10"]
			break;
		case "Fire Alarm":
			state.sound = [uri: "http://s3.amazonaws.com/smartapp-media/sonos/alarm.mp3", duration: "17"]
			break;
		case "Piano":
			state.sound = [uri: "http://s3.amazonaws.com/smartapp-media/sonos/piano2.mp3", duration: "10"]
			break;
		case "Lightsaber":
			state.sound = [uri: "http://s3.amazonaws.com/smartapp-media/sonos/lightsaber.mp3", duration: "10"]
			break;
		case "Noise":
			state.sound = [uri: "http://mc2method.org/white-noise/download.php?file=03-White-Noise&length=60", duration: "216000"]
			break;
		default:
			 state.sound = externalTTS ? textToSpeechT("You selected the sound option but did not enter a sound in the $app.label Smart App") : textToSpeech("You selected the sound option but did not enter a sound in the $app.label Smart App")
			break;
	}

}

private textToSpeechT(message){
    if (message) {
		if (ttsApiKey){
            [uri: "x-rincon-mp3radio://api.voicerss.org/" + "?key=$ttsApiKey&hl="+ttsLanguage+"&r=0&f=48khz_16bit_mono&src=" + URLEncoder.encode(message, "UTF-8").replaceAll(/\+/,'%20') +"&sf=//s3.amazonaws.com/smartapp-" , duration: "${5 + Math.max(Math.round(message.length()/12),2)}"]
        }
        else{
        	message = message.length() >100 ? message[0..90] :message
        	//[uri: "x-rincon-mp3radio://www.translate.google.com/translate_tts?tl=en&client=t&q=" + URLEncoder.encode(message, "UTF-8").replaceAll(/\+/,'%20') +"&sf=//s3.amazonaws.com/smartapp-", duration: "${5 + Math.max(Math.round(message.length()/12),2)}"]
            [uri: "x-rincon-mp3radio://code.responsivevoice.org/getvoice.php?tl="+googleLanguage+"&sv=&vn=&pitch=0.5&rate=0.5&vol=1&t=" + URLEncoder.encode(message, "UTF-8").replaceAll(/\+/,'%20') +"&sf=//s3.amazonaws.com/smartapp-", duration: "${5 + Math.max(Math.round(message.length()/12),2)}"]
     	}
    }else{
    	[uri: "https://s3.amazonaws.com/smartapp-media/tts/633e22db83b7469c960ff1de955295f57915bd9a.mp3", duration: "10"]
    }
}

private safeTextToSpeech(message) {
	message = message?:"You selected the Text to Speach Function but did not enter a Message"
    switch(ttsMode){
        case "Vioce RSS":
        	[uri: "x-rincon-mp3radio://api.voicerss.org/" + "?key=$ttsApiKey&hl="+ttsLanguage+"&r=0&f=48khz_16bit_mono&src=" + URLEncoder.encode(message, "UTF-8").replaceAll(/\+/,'%20') +"&sf=//s3.amazonaws.com/smartapp-" , duration: "${5 + Math.max(Math.round(message.length()/12),2)}"]
        break
        case "Alexa":
        	[uri: "x-rincon-mp3radio://tts.freeoda.com/alexa.php/" + "?key=$alexaApiKey&text=" + URLEncoder.encode(message, "UTF-8").replaceAll(/\+/,'%20') +"&sf=//s3.amazonaws.com/smartapp-" , duration: "${5 + Math.max(Math.round(message.length()/12),2)}"]
        break
        case "Google":
        	message = message.length() >100 ? message[0..90] :message
            [uri: "x-rincon-mp3radio://code.responsivevoice.org/getvoice.php?tl="+googleLanguage+"&sv=&vn=&pitch=0.5&rate=0.5&vol=1&t=" + URLEncoder.encode(message, "UTF-8").replaceAll(/\+/,'%20') +"&sf=//s3.amazonaws.com/smartapp-", duration: "${5 + Math.max(Math.round(message.length()/12),2)}"]
        break
        default:
            try {
            	textToSpeech(message,stLanguage.substring(6))
            }
            catch (Throwable t) {
                log.error t
                textToSpeechT(message)
            }
         break
    }
}

def normalizeMessage(message, evt){
	if (evt && message){
        message = message.replace("#name", evt?.displayName?:"") 
        message = message.replace("#type", evt?.name?:"")
        message = message.replace("#value", evt?.value?:"") 
    }
    if (message){
    	message = message.replace("#mode", location.mode)
    	message = message.replace("#location", location.name)
        message = message.replace("#s", ttsMode == "Alexa" ? "Alexa, Simon says":"")

    }
return message
}

private cleanUri(uri) {
    if (uri){
       	uri = uri.replace("x-rincon-mp3radio:","http:")
    }
    return uri
}