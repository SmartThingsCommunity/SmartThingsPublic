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
 *  Sonos Weather Forecast
 *
 *  Author: SmartThings - Ule
 *  Date: 2016-2-04
 */
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidKeyException; 


definition(
    name: "Weather Report",
    namespace: "mujica",
    author: "SmartThings-ule",
    description: "Play a weather report through your speaker when the mode changes or other events occur, multilanguage",
    category: "SmartThings Labs",
    iconUrl: "http://tts.urbansa.com/icons/weather.png",
    iconX2Url: "http://tts.urbansa.com/icons/weather@2x.png"
)

preferences {
	page(name: "mainPage", title: "Play the weather report on your speaker", install: true, uninstall: true)
	page(name: "triggersPlay")
    page(name: "chooseTrack", title: "Select a song or station")
    page(name: "ttsKey", title: "Add the Text for Speach Key")
    page(name: "ttsSettings", title: "Text for Speach Settings")
    page(name: "ttsKeyIvona", title: "Add the Ivona Key")
    page(name: "moreOptions")
	page(name: "timeIntervalInput", title: "Only during a certain time") {
		section {
			input "starting", "time", title: "Starting", required: false
			input "ending", "time", title: "Ending", required: false
		}
	}
}

def mainPage() {
	dynamicPage(name: "mainPage") {
		def languageOptions = ["ca-es":"Catalan","zh-cn":"Chinese (China)","zh-hk":"Chinese (Hong Kong)","zh-tw":"Chinese (Taiwan)","da-dk":"Danish","nl-nl":"Dutch","en-au":"English (Australia)","en-ca":"English (Canada)","en-gb":"English (Great Britain)","en-in":"English (India)","en-us":"English (United States)","fi-fi":"Finnish","fr-ca":"French (Canada)","fr-fr":"French (France)","de-de":"German","it-it":"Italian","ja-jp":"Japanese","ko-kr":"Korean","nb-no":"Norwegian","pl-pl":"Polish","pt-br":"Portuguese (Brazil)","pt-pt":"Portuguese (Portugal)","ru-ru":"Russian","es-mx":"Spanish (Mexico)","es-es":"Spanish (Spain)","sv-se":"Swedish (Sweden)"]
 		def languageGoogleOptions = ["ca":"Catalan","zh-CN":"Chinese (Simplified)","zh-TW":"Chinese (Traditional)","da":"Danish","nl":"Dutch","en":"English","fi":"Finnish","fr":"French","de":"German","it":"Italian","no":"Norwegian","pl":"Polish","pt-BR":"Portuguese (Brazil)","pt-PT":"Portuguese (Portugal)","ro":"Romanian","ru":"Russian","es":"Spanish","es-419":"Spanish (Latino)","sv":"Swedish","tr":"Turkish"]
        section{
        	href "triggersPlay", title: "Select report triggers?",required: flase, description: anythingSet()?"Change triggers":"Tap to set"
        }
		section {
			input("forecastOptions", "enum", defaultValue: "0", title: "Weather report options", description: "Select one or more", multiple: true,
				options: [
					["0": "Current Conditions"],
					["1": "Today's Forecast"],
					["2": "Tonight's Forecast"],
					["3": "Tomorrow's Forecast"],
				]
			)
		}
		section {
			input "sonos", "capability.musicPlayer", title: "On this speaker player", required: true,multiple: true
		}
        section{  
        	href "ttsSettings", title: "Text for Speach Settings",required:flase, description:ttsMode + " - " + (ttsMode == "SmartThings"? "English":"") + (ttsMode == "Voice RSS"? languageOptions[ttsLanguage]:"") + (ttsMode == "Google"? languageGoogleOptions[ttsGoogleLanguage]:"") + (ttsMode == "Ivona"? voiceIvona:"") 
        }
		section{
        	href "moreOptions", title: "More Options",required: flase, description:"Tap to set" 
        }
		section([mobileOnly:true]) {
			label title: "Assign a name", required: false
			mode title: "Set for specific mode(s)"
		}
	}
}

def moreOptions(){
	dynamicPage(name: "moreOptions") {
		section("More options") {
			input "resumePlaying", "bool", title: "Resume currently playing music after weather report finishes", required: false, defaultValue: true
			href "chooseTrack", title: "Or play this music or radio station", description: song ? state.selectedSong?.station : "Tap to set", state: song ? "complete" : "incomplete"

			input "zipCode", "text", title: "Zip Code", required: false
			input "volume", "number", title: "Temporarily change volume", description: "0-100%", required: false
			input "frequency", "decimal", title: "Minimum time between actions (defaults to every event)", description: "Minutes", required: false
			href "timeIntervalInput", title: "Only during a certain time", description: timeLabel ?: "Tap to set", state: timeLabel ? "complete" : "incomplete"
			input "days", "enum", title: "Only on certain days of the week", multiple: true, required: false,
				options: ["Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"]
			if (settings.modes) {
            	input "modes", "mode", title: "Only when mode is", multiple: true, required: false
            }
			input "oncePerDay", "bool", title: "Only once per day", required: false, defaultValue: false
		}
    }
}


def triggersPlay(){
	dynamicPage(name: "triggersPlay") {
       	triggers()
    }
}


def triggers(){
    	def anythingSet = anythingSet()
        
        if (anythingSet) {
			section("Report When"){
				ifSet "motion", "capability.motionSensor", title: "Motion Here", required: false, multiple: true
				ifSet "contact", "capability.contactSensor", title: "Contact Opens", required: false, multiple: true
				ifSet "contactClosed", "capability.contactSensor", title: "Contact Closes", required: false, multiple: true
				ifSet "acceleration", "capability.accelerationSensor", title: "Acceleration Detected", required: false, multiple: true
				ifSet "mySwitch", "capability.switch", title: "Switch Turned On", required: false, multiple: true
				ifSet "mySwitchOff", "capability.switch", title: "Switch Turned Off", required: false, multiple: true
				ifSet "arrivalPresence", "capability.presenceSensor", title: "Arrival Of", required: false, multiple: true
				ifSet "departurePresence", "capability.presenceSensor", title: "Departure Of", required: false, multiple: true
				ifSet "smoke", "capability.smokeDetector", title: "Smoke Detected", required: false, multiple: true
				ifSet "water", "capability.waterSensor", title: "Water Sensor Wet", required: false, multiple: true
				ifSet "lock", "capability.lock", title: "Lock locks", required: false, multiple: true
				ifSet "lockLocks", "capability.lock", title: "Lock unlocks", required: false, multiple: true
				ifSet "button1", "capability.button", title: "Button Press", required:false, multiple:true //remove from production
                ifSet "triggerModes", "mode", title: "System Changes Mode", required: false, multiple: true
				ifSet "timeOfDay", "time", title: "At a Scheduled Time", required: false
			}
		}
		def sectionTitle = anythingSet ? "Select additional triggers" : "Report When..."

		section(sectionTitle){
			ifUnset "motion", "capability.motionSensor", title: "Motion Here", required: false, multiple: true
			ifUnset "contact", "capability.contactSensor", title: "Contact Opens", required: false, multiple: true
			ifUnset "contactClosed", "capability.contactSensor", title: "Contact Closes", required: false, multiple: true
			ifUnset "acceleration", "capability.accelerationSensor", title: "Acceleration Detected", required: false, multiple: true
			ifUnset "mySwitch", "capability.switch", title: "Switch Turned On", required: false, multiple: true
			ifUnset "mySwitchOff", "capability.switch", title: "Switch Turned Off", required: false, multiple: true
			ifUnset "arrivalPresence", "capability.presenceSensor", title: "Arrival Of", required: false, multiple: true
			ifUnset "departurePresence", "capability.presenceSensor", title: "Departure Of", required: false, multiple: true
			ifUnset "smoke", "capability.smokeDetector", title: "Smoke Detected", required: false, multiple: true
			ifUnset "water", "capability.waterSensor", title: "Water Sensor Wet", required: false, multiple: true
			ifUnset "lock", "capability.lock", title: "Lock locks", required: false, multiple: true
			ifUnset "lock", "capability.lock", title: "Lock unlocks", required: false, multiple: true
			ifUnset "button1", "capability.button", title: "Button Press", required:false, multiple:true //remove from production
            ifUnset "triggerModes", "mode", title: "System Changes Mode", description: "Select mode(s)", required: false, multiple: true
			ifUnset "timeOfDay", "time", title: "At a Scheduled Time", required: false
		}
}

def chooseTrack() {
	dynamicPage(name: "chooseTrack") {
		section{
			input "song","enum",title:"Play this track", required:true, multiple: false, options: songOptions()
		}
	}
}

def ttsSettings() {
	dynamicPage(name: "ttsSettings") {
        def languageOptions = ["ca-es":"Catalan","zh-cn":"Chinese (China)","zh-hk":"Chinese (Hong Kong)","zh-tw":"Chinese (Taiwan)","da-dk":"Danish","nl-nl":"Dutch","en-au":"English (Australia)","en-ca":"English (Canada)","en-gb":"English (Great Britain)","en-in":"English (India)","en-us":"English (United States)","fi-fi":"Finnish","fr-ca":"French (Canada)","fr-fr":"French (France)","de-de":"German","it-it":"Italian","ja-jp":"Japanese","ko-kr":"Korean","nb-no":"Norwegian","pl-pl":"Polish","pt-br":"Portuguese (Brazil)","pt-pt":"Portuguese (Portugal)","ru-ru":"Russian","es-mx":"Spanish (Mexico)","es-es":"Spanish (Spain)","sv-se":"Swedish (Sweden)"]
 		def languageGoogleOptions = ["ca":"Catalan","zh-CN":"Chinese (Simplified)","zh-TW":"Chinese (Traditional)","da":"Danish","nl":"Dutch","en":"English","fi":"Finnish","fr":"French","de":"German","it":"Italian","no":"Norwegian","pl":"Polish","pt-BR":"Portuguese (Brazil)","pt-PT":"Portuguese (Portugal)","ro":"Romanian","ru":"Russian","es":"Spanish","es-419":"Spanish (Latino)","sv":"Swedish","tr":"Turkish"]
        section() {
	        //input "externalTTS", "bool", title: "Force Only External Text to Speech", required: false, defaultValue: false
            
            input "ttsMode", "enum", title: "Mode?", required: true, defaultValue: "SmartThings",submitOnChange:true, options: ["SmartThings","Ivona","Voice RSS","Google"]
            input "ttsGoogleLanguage","enum",title:"Google Language", required:true, multiple: false, defaultValue: "en", options: languageGoogleOptions
            href "ttsKey", title: "Voice RSS Key", description: ttsApiKey, state: ttsApiKey ? "complete" : "incomplete", required: ttsMode == "Voice RSS"?true:false
            input "ttsLanguage","enum",title:"Voice RSS Language", required:true, multiple: false, defaultValue: "en-us", options: languageOptions
            href "ttsKeyIvona", title: "Ivona Access Key", description: "${ttsAccessKey?:""}-${ttsSecretKey?:""}" ,state: ttsAccessKey && ttsSecretKey ? "complete" : "incomplete",  required: ttsMode == "Ivona" ? true:false
            input "voiceIvona", "enum", title: "Ivona Voice?", required: true, defaultValue: "en-US Salli", options: ["cy-GB Gwyneth","cy-GB Geraint","da-DK Naja","da-DK Mads","de-DE Marlene","de-DE Hans","en-US Salli","en-US Joey","en-AU Nicole","en-AU Russell","en-GB Amy","en-GB Brian","en-GB Emma","en-GB Gwyneth","en-GB Geraint","en-IN Raveena","en-US Chipmunk","en-US Eric","en-US Ivy","en-US Jennifer","en-US Justin","en-US Kendra","en-US Kimberly","es-ES Conchita","es-ES Enrique","es-US Penelope","es-US Miguel","fr-CA Chantal","fr-FR Celine","fr-FR Mathieu","is-IS Dora","is-IS Karl","it-IT Carla","it-IT Giorgio","nb-NO Liv","nl-NL Lotte","nl-NL Ruben","pl-PL Agnieszka","pl-PL Jacek","pl-PL Ewa","pl-PL Jan","pl-PL Maja","pt-BR Vitoria","pt-BR Ricardo","pt-PT Cristiano","pt-PT Ines","ro-RO Carmen","ru-RU Tatyana","ru-RU Maxim","sv-SE Astrid","tr-TR Filiz"]
        }
        section("Google do not requiere API KEY but is limited to 200 Chars and could be blocked any time.") {}
	}
}

def ttsKey() {
	dynamicPage(name: "ttsKey") {
		section{
			input "ttsApiKey", "text", title: "TTS Key", required: false,  defaultValue:""
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

def ttsKeyIvona() {
	dynamicPage(name: "ttsKeyIvona") {
		section{
			input "ttsAccessKey", "text", title: "Ivona Access Key", required: false,  defaultValue:""
            input "ttsSecretKey", "text", title: "Ivona Secret Key", required: false, defaultValue:""
		}
        section ("Ivona provides free Text-to-Speech API as WEB service, allows 50K free request per month with high quality voice") {
            href(name: "hrefRegisterIvona",
                 title: "Register",
                 required: false,
                 style: "external",
                 url: "https://www.ivona.com/us/for-business/speech-cloud/",
                 description: "Register and obtain you Access and Secret Key")
            href(name: "hrefKnownIvona",
                 title: "Known about Ivona",
                 required: false,
                 style: "external",
                 url: "https://www.ivona.com/us/",
                 description: "Go to www.ivona.com")
        }
    }
}

private anythingSet() {
	for (name in ["motion","contact","contactClosed","acceleration","mySwitch","mySwitchOff","arrivalPresence","departurePresence","smoke","water","button1","timeOfDay","triggerModes"]) {
		if (settings[name]) {
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
	subscribeToEvents()
}

def updated() {
	log.debug "Updated with settings: ${settings}"
	unsubscribe()
	unschedule()
	subscribeToEvents()
}

def subscribeToEvents() {
	subscribe(app, appTouchHandler)
	subscribe(contact, "contact.open", eventHandler)
	subscribe(contactClosed, "contact.closed", eventHandler)
	subscribe(acceleration, "acceleration.active", eventHandler)
	subscribe(motion, "motion.active", eventHandler)
	subscribe(mySwitch, "switch.on", eventHandler)
	subscribe(mySwitchOff, "switch.off", eventHandler)
	subscribe(arrivalPresence, "presence.present", eventHandler)
	subscribe(departurePresence, "presence.not present", eventHandler)
	subscribe(smoke, "smoke.detected", eventHandler)
	subscribe(smoke, "smoke.tested", eventHandler)
	subscribe(smoke, "carbonMonoxide.detected", eventHandler)
	subscribe(water, "water.wet", eventHandler)
	subscribe(button1, "button.pushed", eventHandler)

	if (triggerModes) {
		subscribe(location,modeChangeHandler)
	}

	if (timeOfDay) {
		schedule(timeOfDay, scheduledTimeHandler)
	}

	if (song) {
		saveSelectedSong()
	}
}

def eventHandler(evt) {
	if (allOk) {
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

def modeChangeHandler(evt) {
	if (evt.value in triggerModes) {
		eventHandler(evt)
	}
}

def scheduledTimeHandler() {
	eventHandler(null)
}

def appTouchHandler(evt) {
	takeAction(evt)
}

private takeAction(evt) {
	loadText()
	if (song) {
		sonos.each {
           it.playSoundAndTrack(cleanUri(state.sound.uri, it?.currentModel), state.sound.duration, state.selectedSong, volume)
    	}
	}
	else if (resumePlaying){
		sonos.each {
           it.playTrackAndResume(cleanUri(state.sound.uri, it?.currentModel), state.sound.duration, volume)
    	}
	}
	else if (volume) {
		sonos.each {
           it.playTrackAtVolume(cleanUri(state.sound.uri, it?.currentModel), volume)
    	}
	}
	else {
		sonos.each {
           it.playTrack(cleanUri(state.sound.uri, it?.currentModel))
    	}
	}
	if (frequency || oncePerDay) {
		state[frequencyKey(evt)] = now()
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
                log.info "Searching ${dataMaps.size()} records"
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
	if (location.timeZone || zipCode) {
		def weather
        def current
		def isMetric = location.temperatureScale == "C"
		def delim = ". "
		def sb = new StringBuilder()
        def languages = ["ca-es":"CA","zh-cn":"CN","zh-hk":"CN","zh-tw":"TW","da-dk":"DK","nl-nl":"NL","en-au":"EN","en-ca":"EN","en-gb":"LI","en-in":"EN","en-us":"EN","fi-fi":"FI","fr-ca":"FC","fr-fr":"FR","de-de":"DL","it-it":"IT","ja-jp":"JP","ko-kr":"KR","nb-no":"NO","pl-pl":"PL","pt-br":"BR","pt-pt":"BR","ru-ru":"RU","es-mx":"SP","es-es":"SP","sv-se":"SW","cy-GB Gwyneth":"LI","cy-GB Geraint":"LI","da-DK Naja":"DK","da-DK Mads":"DK","de-DE Marlene":"DL","de-DE Hans":"DL","en-US Salli":"EN","en-US Joey":"EN","en-AU Nicole":"EN","en-AU Russell":"EN","en-GB Amy":"LI","en-GB Brian":"LI","en-GB Emma":"LI","en-GB Gwyneth":"LI","en-GB Geraint":"LI","en-IN Raveena":"EN","en-US Chipmunk":"EN","en-US Eric":"EN","en-US Ivy":"En","en-US Jennifer":"EN","en-US Justin":"EN","en-US Kendra":"EN","en-US Kimberly":"EN","es-ES Conchita":"SP","es-ES Enrique":"SP","es-US Penelope":"SP","es-US Miguel":"SP","fr-CA Chantal":"FC","fr-FR Celine":"FR","fr-FR Mathieu":"FR","is-IS Dora":"IS","is-IS Karl":"IS","it-IT Carla":"IT","it-IT Giorgio":"IT","nb-NO Liv":"NO","nl-NL Lotte":"NL","nl-NL Ruben":"NL","pl-PL Agnieszka":"PL","pl-PL Jacek":"PL","pl-PL Ewa":"PL","pl-PL Jan":"PL","pl-PL Maja":"PL","pt-BR Vitoria":"BR","pt-BR Ricardo":"BR","pt-PT Cristiano":"BR","pt-PT Ines":"BR","ro-RO Carmen":"RO","ru-RU Tatyana":"RU","ru-RU Maxim":"RU","sv-SE Astrid":"SW","tr-TR Filiz":"TR","ca":"CA","zh-CN":"CN","zh-TW":"CN","da":"DK","nl":"NL","en":"EN","fi":"FI","fr":"FR","de":"DL","it":"IT","no":"NO","pl":"PL","pt-BR":"BR","pt-PT":"BR","ro":"RO","ru":"RU","es":"SP","es-419":"SP","sv":"SW","tr":"TR"]
        state.language ="EN"

        def titles = [
        	"CA": ["La temperatura actual és","graus","Avui dia, la previsió és","aquesta Nit serà","Demà serà"],
            "DK": ["Den aktuelle temperatur er","grader","Today' s prognose er","i Aften vil være","i Morgen vil være"],
            "NL": ["Het huidige temperatuur is","graden","Vandaag is de prognose is","Tonight","Morgen"],
            "LI": ["The current temperature is","degrees","Today's forecast is","Tonight will be","Tomorrow will be"],
            "FI": ["Lämpötila","astetta","Tänään on ennuste on","Tänään","Huomenna"],
            "FC": ["La température actuelle est","degrés","aujourd'Hui, les prévisions de l'est","ce Soir sera","Demain, ce sera"],
            "FR": ["La température actuelle est","degrés","aujourd'Hui, les prévisions de l'est","ce Soir sera","Demain, ce sera"],
            "DL": ["Aktuelle Temperatur","Grad","die heutige Prognose ist","Heute","Morgen"],
            "IT": ["La temperatura attuale è","gradi","Oggi previsioni","Stasera sarà","Domani sarà"],
            "NO": ["Dagens temperatur er","grader","Dagens prognose er","i Kveld vil være","i Morgen vil være"],
            "PL": ["Aktualna temperatura","stopień","prognoza pogody","dziś","jutro będzie"],
            "RU": ["Текущая температура","градусы","прогноз","сегодня будет","завтра будет"],
            "SP": ["La Temperatura actual es de","grados","Se pronostica para hoy","Para esta Noche","Mañana "],
            "SW": ["Den nuvarande temperaturen är","grader","Dagens prognos är","i Kväll kommer att vara","i Morgon kommer att vara"],
            "IS": ["Núverandi hitinn er","gráður","í Dag er spá er","í Kvöld verður það","á Morgun verður"],
            "RO": ["Temperatura curentă","grade","prognoza","azi","mâine va fi"],
            "TR": ["Geçerli sıcaklık","derece","Bugün hava","Akşam","Yarın olacak"],
            "BR": ["A temperatura atual é","graus","Hoje a previsão é","hoje a Noite vai ser","Amanhã vai ser"],
            "EN": ["The current temperature is","degrees","Today's forecast is","Tonight will be","Tomorrow will be"],
            "TW": ["","","","",""],
            "CN": ["","","","",""]
            ]

        switch (ttsMode){
            case "Ivona": 
                state.language = languages[voiceIvona]
                delim = " ... "
            break
            case "Voice RSS": 
                state.language = languages[ttsLanguage]
                delim = ". "
            break
            case "Google": 
                state.language = languages[ttsGoogleLanguage]
                delim = " ... "
            break
        }
        ttsGoogleLanguage
        def language =  state.language ? "/lang:${state.language}":""
        
        weather = getWeatherFeature("forecast$language", zipCode)
		current = getWeatherFeature("conditions$language", zipCode)
        
        list(forecastOptions).sort().each {opt ->
			if (opt == "0") {
				if (isMetric) {
                	sb << "${titles[state.language][0]} ${Math.round(current?.current_observation?.temp_c?:0)} ${titles[state.language][1]}."
                }
                else {
                	sb << "${titles[state.language][0]} ${Math.round(current?.current_observation?.temp_f?:0)} ${titles[state.language][1]}."
                }
				//delim = "   "
			}
			else if (opt == "1" && weather.forecast) {
				sb << delim
				sb << "${titles[state.language][2]} "
				if (isMetric) {
                	sb << weather.forecast.txt_forecast.forecastday[0].fcttext_metric
                }
                else {
                	sb << weather.forecast.txt_forecast.forecastday[0].fcttext
                }
			}
			else if (opt == "2" && weather.forecast) {
				sb << delim
				sb << "${titles[state.language][3]} "
				if (isMetric) {
                	sb << weather.forecast.txt_forecast.forecastday[1].fcttext_metric 
                }
                else {
                	sb << weather.forecast.txt_forecast.forecastday[1].fcttext
                }
			}
			else if (opt == "3" && weather.forecast) {
				sb << delim
				sb << "${titles[state.language][4]} "
				if (isMetric) {
                	sb << weather.forecast.txt_forecast.forecastday[2].fcttext_metric 
                }
                else {
                	sb << weather.forecast.txt_forecast.forecastday[2].fcttext
                }
			}
		}

		def msg = sb.toString()
        msg = msg.replaceAll(/([0-9]+)C/,'$1 degrees') // TODO - remove after next release
		state.sound = safeTextToSpeech(normalize(msg))
	}
	else {
		state.sound = safeTextToSpeech("Please set the location of your hub with the SmartThings mobile app, or enter a zip code to receive weather forecasts.")
	}
    log.trace "state.sound ${state.sound}"
}

private list(String s) {
	[s]
}
private list(l) {
	l
}


private textToSpeechT(message){
   if (message) {
        if(ttsAccessKey && ttsSecretKey){
        	[uri: ttsIvona(message), duration: "${5 + Math.max(Math.round(message.length()/12),2)}"]
        }
		else if (ttsApiKey){
            [uri: "x-rincon-mp3radio://api.voicerss.org/" + "?key=$ttsApiKey&hl="+ ttsLanguage +"&r=0&f=48khz_16bit_mono&src=" + URLEncoder.encode(message, "UTF-8").replaceAll(/\+/,'%20') +"&sf=//s3.amazonaws.com/smartapp-" , duration: "${5 + Math.max(Math.round(message.length()/12),2)}"]
        }
        else{
        	message = message.length() >195 ? message[0..195] :message
        	[uri: "x-rincon-mp3radio://www.translate.google.com/translate_tts?tl="+ttsGoogleLanguage+"&client=tw-ob&q=" + URLEncoder.encode(message, "UTF-8").replaceAll(/\+/,'%20') +"&sf=//s3.amazonaws.com/smartapp-", duration: "${5 + Math.max(Math.round(message.length()/12),2)}"]
     	}
    }else{
    	[uri: "https://s3.amazonaws.com/smartapp-media/tts/633e22db83b7469c960ff1de955295f57915bd9a.mp3", duration: "10"]
    }
}

private safeTextToSpeech(message) {

    message = message?:"You selected the Text to Speach Function but did not enter a Message"
    switch(ttsMode){
        case "Ivona":
        	[uri: ttsIvona(message), duration: "${5 + Math.max(Math.round(message.length()/12),2)}"]
        break
        case "Voice RSS":
        	[uri: "x-rincon-mp3radio://api.voicerss.org/" + "?key=$ttsApiKey&hl="+ ttsLanguage +"&r=0&f=48khz_16bit_mono&src=" + URLEncoder.encode(message, "UTF-8").replaceAll(/\+/,'%20') +"&sf=//s3.amazonaws.com/smartapp-" , duration: "${5 + Math.max(Math.round(message.length()/12),2)}"]
        break
        case "Google":
        	message = message.length() >195 ? message[0..195] :message
        	[uri: "x-rincon-mp3radio://www.translate.google.com/translate_tts?tl="+ttsGoogleLanguage+"&client=tw-ob&q=" + URLEncoder.encode(message, "UTF-8").replaceAll(/\+/,'%20') +"&sf=//s3.amazonaws.com/smartapp-", duration: "${5 + Math.max(Math.round(message.length()/12),2)}"]
        break
        default:
            try {
            	textToSpeech(message)
            }
            catch (Throwable t) {
                log.error t
                textToSpeechT(message)
            }
         break
    }
}

private normalize(message){
	def abbreviations = [
		"CA": ["C\\.":" Centígrads","F\\.":" Fahrenheit"],
		"DK": ["ºC\\.":"grader celsius","ºF\\.":"grader Fahrenheit"],
		"NL": ["ºC\\.":"Celsius","ºF\\.":"Fahrenheit"],
		"LI": ["ºC\\.":"Celsius","ºF\\.":"Fahrenheit"],
		"FI": ["ºC\\.":"Celsius","ºF\\.":"Fahrenheit"],
		"FC": ["ºC\\.":"Celsius","ºF\\.":"Fahrenheit"],
		"FR": ["ºC\\.":"degrés","ºF\\.":"degrés","km/h":" Kilomètres par heure","mi/h":" Miles par heure"],
		"DL": ["ºC\\.":"Celsius","ºF\\.":"Fahrenheit"],
		"IT": ["ºC\\.":"Celsius","ºF\\.":"Fahrenheit"],
		"NO": ["ºC\\.":"Celsius","ºF\\.":"Fahrenheit"],
		"PL": ["ºC\\.":"Celsjusz","ºF\\.":"Fahrenheita"],
		"RU": ["ºС\\.":"Цельсий","ºF\\.":"По Фаренгейту"],
		"SP": [" C\\.":" grados"," F\\.":" grados", "km/h":" Kilometros por hora","milla/h":" Millas por hora"," E ":" Este "," N ":" Norte "," S ":" Sur "," W ":" Oeste ", " O ":" Oeste ","NNE":"Nor Nordeste","NE":"Nordeste","ENE":"Este Nordeste","ESE":"Este Sudeste","SE":"Sudeste","SSE":"Sud Sudeste","SSO":"Sud Sudoeste","SSW":"Sud Sudoeste","SO":"Sudoeste","SW":"Sudoeste","OSO":"Oeste Sudoeste","WSW":"Oeste Sudoeste","ONO":"Oesnoroeste","WNW":"Oesnoroeste","NO":"Noroeste","NW":"Noroeste","NNO":"Nornoroeste","NNW":"Nornoroeste"],
		"SW": ["ºC\\.":"Celsius","ºF\\.":"Fahrenheit"],
		"IS": ["ºC\\.":"Celsius","ºF\\.":"Fahrenheit"],
		"RO": ["ºC\\.":"Celsius","ºF\\.":"Fahrenheit"],
		"TR": ["°C\\.":" Selsius","°F\\.":" Fahrenhayt"],
		"BR": ["ºC\\.":"Celsius","ºF\\.":"Fahrenheit"," E ":" Este "," N ":" Norte "," S ":" Sul "," W ":" Oeste ", " O ":" Oeste ","NNE":"Nor Nordeste","NE":"Nordeste","ENE":"Lés Nordeste","ESE":"Lés Sudeste","SE":"Sudeste","SSE":"Su Sudeste","SSO":"Su Sudoeste","SSW":"Su Sudoeste","SO":"Sudoeste","SW":"Sudoeste","OSO":"Oés Sudoeste","WSW":"Oés Sudoeste","ONO":"Oés Noroeste","WNW":"Oés Noroeste","NO":"Noroeste","NW":"Noroeste","NNO":"Nor Noroeste","NNW":"Nor Noroeste"],
		"EN": ["C\\.":" Celsius","F\\.":" Fahrenheit","km/h":" Kilometers per hour","mph":" Miles per hour"," E ":" East "," N ":" North "," S ":" South "," W ":" West ", " O ":" West " ,"ENE":"East-northeast","ESE":"East-southeast","NE":"Northeast","NNE":"North-northeast","NNW":"North-northwest","NW":"Northwest","SE":"Southeast","SSE":"South-southeast","SSW":"South-southwest","SW":"Southwest","WNW":"West-northwest","WSW":"West-southwest"],
       	"TW": ["ºC\\.":"Celsius","ºF\\.":"Fahrenheit"],
		"CN": ["ºC\\.":"Celsius","ºF\\.":"Fahrenheit"]
		]
   //  " N " : " North ","NbE" : "North by east","NNE" : "North-northeast","NEbN" : "Northeast by north"," NE " : " Northeast ","NEbE" : "Northeast by east","ENE" : "East-northeast","EbN" : "East by north"," E " : " East ","EbS" : "East by south","ESE" : "East-southeast","SEbE" : "Southeast by east"," SE " : " Southeast ","SEbS" : "Southeast by south","SSE" : "South-southeast","SbE" : "South by east"," S " : " South ","SbW" : "South by west","SSW" : "South-southwest","SWbS" : "Southwest by south"," SW " : " Southwest ","SWbW" : "Southwest by west","WSW" : "West-southwest","WbS" : "West by south"," W " : " West ","WbN" : "West by north","WNW" : "West-northwest","NWbW" : "Northwest by west"," NW " : " Northwest ","NWbN" : "Northwest by north","NNW" : "North-northwest","NbW" : "North by west"],
	
    def map = abbreviations[state.language]

    if (message){
        map.each{ k, v ->  message = message.replaceAll("(?)"+k,v) }
    }
    message
}

private cleanUri(uri,model="") {
    if (uri){
        uri = uri.replace("https:","http:")
        if (model?.toLowerCase()?.contains("sonos")){
        	uri = uri.replace("http:","x-rincon-mp3radio:")
        }else{
        	uri = uri.replace("x-rincon-mp3radio:","http:")
        }
    }
    return uri
}


def ttsIvona(message){
    def regionName = "us-east-1";
    def df = new java.text.SimpleDateFormat("yyyyMMdd'T'HHmmss'Z'")
    df.setTimeZone(TimeZone.getTimeZone("UTC"))
    def amzdate = df.format(new Date())
    def canonicalQueryString = "${URLEncoder.encode(message, "UTF-8").replaceAll(/\+/,'%20')}%3F&Input.Type=text%2Fplain&OutputFormat.Codec=MP3&OutputFormat.SampleRate=22050&Parameters.Rate=medium&Voice.Language=${voiceIvona.getAt(0..4)}&Voice.Name=${voiceIvona.getAt(6..-1)}&X-Amz-Algorithm=AWS4-HMAC-SHA256&X-Amz-Credential=$ttsAccessKey%2F${amzdate.getAt(0..7)}%2F$regionName%2Ftts%2Faws4_request&X-Amz-Date=$amzdate&X-Amz-SignedHeaders=host";  
    "http://tts.freeoda.com/tts.php?${now()}=${URLEncoder.encode("$canonicalQueryString&X-Amz-Signature=${hmac_sha256(hmac_sha256(hmac_sha256(hmac_sha256(hmac_sha256("AWS4$ttsSecretKey".bytes,amzdate.getAt(0..7)),regionName),"tts"),"aws4_request"), "AWS4-HMAC-SHA256\n$amzdate\n${amzdate.getAt(0..7)}/$regionName/tts/aws4_request\n${sha256Hash("GET\n/CreateSpeech\nInput.Data=$canonicalQueryString\nhost:tts.${regionName}.ivonacloud.com\n\nhost\ne3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855")}").collect { String.format("%02x", it) }.join('')}")}"
}

def sha256Hash(text) {
    java.security.MessageDigest.getInstance("SHA-256").digest(text.bytes).collect { String.format("%02x", it) }.join('')
}

def hmac_sha256(byte[] secretKey, String data) {
	try {
        Mac mac = Mac.getInstance("HmacSHA256")
        SecretKeySpec secretKeySpec = new SecretKeySpec(secretKey, "HmacSHA256")
        mac.init(secretKeySpec)
        byte[] digest = mac.doFinal(data.bytes)
        return digest
	}
	catch (InvalidKeyException e) {
   		log.error "Invalid key exception while converting to HMac SHA256"	
    }
}