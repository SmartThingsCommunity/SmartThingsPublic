/**
 *  Weather Underground
 *
 *  Copyright 2018 Philippe PORTES
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
 
import groovy.json.JsonSlurper 

metadata {
	definition (name: "Weather Underground Web", namespace: "philippeportesppo", author: "Philippe PORTES") {
        //capability "polling"
        capability "Sensor"
        capability "Temperature Measurement"
        capability "Relative Humidity Measurement"
        capability "Ultraviolet Index"
        capability "Refresh"
	}

	tiles(scale: 2) {

	standardTile("UGW_web", "device.UGW_web",  width: 6, height: 2,  canChangeIcon: false ) {
            state "default", icon: "http://icons.wxug.com/graphics/wu2/logo_130x80.png"      }   
             
    standardTile("temperature", "device.temperature", width: 2, height: 1,  canChangeIcon: false) { //decoration: "flat",
            state "default", label: 'Temperature ${currentValue}º',unit:'${currentValue}', icon: "st.Weather.weather2" }  //,backgroundColor:"#e5e9ea"
        
	standardTile("humidity", "device.humidity", width: 2, height: 1, canChangeIcon: false) {
            state "default", label: 'Humidity ${currentValue}', icon: "st.Weather.weather12"} //, backgroundColor:"#e5e9ea"      
            
	standardTile("UGWFeelsLikelevel", "device.UGWFeelsLikelevel",  width: 2, height: 1,  canChangeIcon: false) { //decoration: "flat",
            state "default",  label: 'Feels Like Temp ${currentValue}º',unit:'${currentValue}',icon: "https://raw.githubusercontent.com/philippeportesppo/AirMentorPro2_SmartThings/master/images/realfeel.png"} //, backgroundColor:"#e5e9ea"

	standardTile("UGWdewpointlevel", "device.UGWdewpointlevel",  width: 2, height: 1, canChangeIcon: false) {
            state "default", label: 'Dewpoint ${currentValue}º',unit:'${currentValue}',icon: "https://raw.githubusercontent.com/philippeportesppo/AirMentorPro2_SmartThings/master/images/dewpoint.png"} //, backgroundColor:"#e5e9ea"
  
    standardTile("UGW_Icon_UrlIcon", "device.UGW_Icon_UrlIcon", decoration: "flat",   width: 2, height: 1) {
                state "chancerain",		icon: "st.custom.wuk.chancerain"		//"https://raw.githubusercontent.com/philippeportesppo/AirMentorPro2_SmartThings/master/images/chancerain.png"
                state "chancesleet",	icon: "st.custom.wuk.chancesleet"		//"https://raw.githubusercontent.com/philippeportesppo/AirMentorPro2_SmartThings/master/images/chancesleet.png"
                state "chancesnow",		icon: "st.custom.wuk.chancesnow"		//"https://raw.githubusercontent.com/philippeportesppo/AirMentorPro2_SmartThings/master/images/chancesnow.png"
                state "chancetstorms",	icon: "st.custom.wuk.chancetstorms"		//"https://raw.githubusercontent.com/philippeportesppo/AirMentorPro2_SmartThings/master/images/chancetstorms.png"
                state "clear",			icon: "st.custom.wuk.clear"				//"https://raw.githubusercontent.com/philippeportesppo/AirMentorPro2_SmartThings/master/images/clear.png"
                state "cloudy", 		icon: "st.custom.wuk.cloudy"			//"https://raw.githubusercontent.com/philippeportesppo/AirMentorPro2_SmartThings/master/images/cloudy.png"
                state "flurries",		icon: "st.custom.wuk.flurries"			//"https://raw.githubusercontent.com/philippeportesppo/AirMentorPro2_SmartThings/master/images/flurries.png"
                state "fog",			icon: "st.custom.wuk.fog"				//"https://raw.githubusercontent.com/philippeportesppo/AirMentorPro2_SmartThings/master/images/fog.png"
                state "hazy",			icon: "st.custom.wuk.hazy"				//"https://raw.githubusercontent.com/philippeportesppo/AirMentorPro2_SmartThings/master/images/hazy.png"
                state "mostlycloudy",	icon: "st.custom.wuk.mostlycloudy"		//"https://raw.githubusercontent.com/philippeportesppo/AirMentorPro2_SmartThings/master/images/mostlycloudy.png"
                state "mostlysunny",	icon: "st.custom.wuk.mostlysunny"		//"https://raw.githubusercontent.com/philippeportesppo/AirMentorPro2_SmartThings/master/images/mostlysunny.png"
                state "partlycloudy", 	icon: "st.custom.wuk.partlycloudy"		//"https://raw.githubusercontent.com/philippeportesppo/AirMentorPro2_SmartThings/master/images/partlycloudy.png"
                state "partlysunny",	icon: "st.custom.wuk.partlysunny"		//"https://raw.githubusercontent.com/philippeportesppo/AirMentorPro2_SmartThings/master/images/partlysunny.png"
                state "sleet",			icon: "st.custom.wuk.sleet"				//"https://raw.githubusercontent.com/philippeportesppo/AirMentorPro2_SmartThings/master/images/sleet.png"
                state "rain",			icon: "st.custom.wuk.rain"				//"https://raw.githubusercontent.com/philippeportesppo/AirMentorPro2_SmartThings/master/images/rain.png"
                state "snow",			icon: "st.custom.wuk.snow"				//"https://raw.githubusercontent.com/philippeportesppo/AirMentorPro2_SmartThings/master/images/snow.png"
                state "sunny",			icon: "st.custom.wuk.sunny"				//"https://raw.githubusercontent.com/philippeportesppo/AirMentorPro2_SmartThings/master/images/sunny.png"
                state "tstorms",		icon: "st.custom.wuk.tstorms"			//"https://raw.githubusercontent.com/philippeportesppo/AirMentorPro2_SmartThings/master/images/tstorms.png"
                state "unknown",		icon: "st.unknown.unknown.unknown"		//"https://raw.githubusercontent.com/philippeportesppo/AirMentorPro2_SmartThings/master/images/unknown.png"
                state "nt_chanceflurries",	icon: "https://raw.githubusercontent.com/philippeportesppo/AirMentorPro2_SmartThings/master/images/nt_chanceflurries.png"	
                state "nt_chancerain",		icon: "https://raw.githubusercontent.com/philippeportesppo/AirMentorPro2_SmartThings/master/images/nt_chancerain.png"
                state "nt_chancesleet",	icon:"https://raw.githubusercontent.com/philippeportesppo/AirMentorPro2_SmartThings/master/images/nt_chancesleet.png"
                state "nt_chancesnow",icon:"https://raw.githubusercontent.com/philippeportesppo/AirMentorPro2_SmartThings/master/images/nt_chancesnow.png"
                state "nt_chancetstorms",icon:"https://raw.githubusercontent.com/philippeportesppo/AirMentorPro2_SmartThings/master/images/nt_chancetstorms.png"
                state "nt_clear",		icon: "st.custom.wuk.nt_clear"			//"https://raw.githubusercontent.com/philippeportesppo/AirMentorPro2_SmartThings/master/images/nt_clear.png"
                state "nt_cloudy",		icon: "st.custom.wuk.nt_cloudy"			//"https://raw.githubusercontent.com/philippeportesppo/AirMentorPro2_SmartThings/master/images/nt_cloudy.png"
                state "nt_flurries",	icon: "st.custom.wuk.nt_flurries"		//"https://raw.githubusercontent.com/philippeportesppo/AirMentorPro2_SmartThings/master/images/nt_flurries.png"
                state "nt_fog",			icon: "st.custom.wuk.nt_fog"			//"https://raw.githubusercontent.com/philippeportesppo/AirMentorPro2_SmartThings/master/images/nt_fog.png"
                state "nt_hazy",		icon: "st.custom.wuk.nt_fog"			//"https://raw.githubusercontent.com/philippeportesppo/AirMentorPro2_SmartThings/master/images/nt_hazy.png"
                state "nt_mostlycloudy",icon: "st.custom.wuk.nt_mostlycloudy"	//"https://raw.githubusercontent.com/philippeportesppo/AirMentorPro2_SmartThings/master/images/nt_mostlycloudy.png"
                state "nt_mostlysunny",	icon: "st.custom.wuk.nt_mostlycloudy"	//"https://raw.githubusercontent.com/philippeportesppo/AirMentorPro2_SmartThings/master/images/nt_mostlysunny.png"
                state "nt_partlycloudy",icon: "st.custom.wuk.nt_mostlycloudy"	//"https://raw.githubusercontent.com/philippeportesppo/AirMentorPro2_SmartThings/master/images/nt_partlycloudy.png"
                state "nt_sleet",		icon: "st.custom.wuk.nt_sleet"			//"https://raw.githubusercontent.com/philippeportesppo/AirMentorPro2_SmartThings/master/images/nt_sleet.png"
                state "nt_rain",		icon: "st.custom.wuk.nt_rain"			//https://raw.githubusercontent.com/philippeportesppo/AirMentorPro2_SmartThings/master/images/nt_rain.png"
                state "nt_snow",		icon: "st.custom.wuk.nt_snow"			//"https://raw.githubusercontent.com/philippeportesppo/AirMentorPro2_SmartThings/master/images/nt_snow.png"
                state "nt_sunny",		icon: "st.custom.wuk.nt_sunny"			//"https://raw.githubusercontent.com/philippeportesppo/AirMentorPro2_SmartThings/master/images/nt_sunny.png"
                state "nt_tstorms",		icon: "st.custom.wuk.nt_tstorms"		//"https://raw.githubusercontent.com/philippeportesppo/AirMentorPro2_SmartThings/master/images/nt_tstorms.png"
	}
            
    standardTile("refresh", "device.refresh", decoration: "flat", width: 1, height: 1) {
 		state "default", action:"refresh", icon:"st.secondary.refresh"
 		} 
	
    standardTile("weather", "device.weather", width: 5, height: 1) {
 		state "default", label:'${currentValue}'
 		} 

    standardTile("wu_main", "device.wu_main", decoration: "flat", width: 6, height: 4) 
    	{
                state "chancerain",		icon: "https://raw.githubusercontent.com/philippeportesppo/AirMentorPro2_SmartThings/master/images/chancerain.png"
                state "chancesleet",	icon:"https://raw.githubusercontent.com/philippeportesppo/AirMentorPro2_SmartThings/master/images/chancesleet.png"
                state "chancesnow",		icon:"https://raw.githubusercontent.com/philippeportesppo/AirMentorPro2_SmartThings/master/images/chancesnow.png"
                state "chancetstorms",	icon:"https://raw.githubusercontent.com/philippeportesppo/AirMentorPro2_SmartThings/master/images/chancetstorms.png"
                state "clear",			icon:"st.Weather.weather4" 				//https://raw.githubusercontent.com/philippeportesppo/AirMentorPro2_SmartThings/master/images/clear.png"
                state "cloudy",			icon:"st.Weather.weather13" 			//https://raw.githubusercontent.com/philippeportesppo/AirMentorPro2_SmartThings/master/images/cloudy.png"
                state "flurries",		icon:"st.Weather.weather6" 				//https://raw.githubusercontent.com/philippeportesppo/AirMentorPro2_SmartThings/master/images/flurries.png"
                state "fog",			icon:"st.Weather.weather8"				//"https://raw.githubusercontent.com/philippeportesppo/AirMentorPro2_SmartThings/master/images/fog.png"
                state "hazy",			icon:"st.Weather.weather8"				//"https://raw.githubusercontent.com/philippeportesppo/AirMentorPro2_SmartThings/master/images/hazy.png"
                state "mostlycloudy", 	icon:"st.Weather.weather13" 			//https://icons.wxug.com/i/c/i/mostlycloudy.gif" //https://raw.githubusercontent.com/philippeportesppo/AirMentorPro2_SmartThings/master/images/mostlycloudy.png"
                state "mostlysunny",	icon:"st.Weather.weather11" 			//https://raw.githubusercontent.com/philippeportesppo/AirMentorPro2_SmartThings/master/images/mostlysunny.png"
                state "partlycloudy", 	icon:"st.Weather.weather15" 			//https://raw.githubusercontent.com/philippeportesppo/AirMentorPro2_SmartThings/master/images/partlycloudy.png"
                state "partlysunny",	icon:"st.Weather.weather11" 			//https://raw.githubusercontent.com/philippeportesppo/AirMentorPro2_SmartThings/master/images/partlysunny.png"
                state "sleet",			icon:"st.Weather.weather6" 				//https://raw.githubusercontent.com/philippeportesppo/AirMentorPro2_SmartThings/master/images/sleet.png"
                state "rain",			icon:"st.Weather.weather10" 			//https://raw.githubusercontent.com/philippeportesppo/AirMentorPro2_SmartThings/master/images/rain.png"
                state "snow",			icon:"st.Weather.weather7" 				//https://raw.githubusercontent.com/philippeportesppo/AirMentorPro2_SmartThings/master/images/snow.png"
                state "sunny",			icon:"st.Weather.weather14" 			//https://raw.githubusercontent.com/philippeportesppo/AirMentorPro2_SmartThings/master/images/sunny.png"
                state "tstorms",		icon:"st.Weather.weather1" 				//https://raw.githubusercontent.com/philippeportesppo/AirMentorPro2_SmartThings/master/images/tstorms.png"
                state "unknown",		icon:"https://raw.githubusercontent.com/philippeportesppo/AirMentorPro2_SmartThings/master/images/unknown.png"
                state "nt_chanceflurries",	icon: "https://raw.githubusercontent.com/philippeportesppo/AirMentorPro2_SmartThings/master/images/nt_chanceflurries.png"	
                state "nt_chancerain",	icon: "https://raw.githubusercontent.com/philippeportesppo/AirMentorPro2_SmartThings/master/images/nt_chancerain.png"
                state "nt_chancesleet",	icon:"https://raw.githubusercontent.com/philippeportesppo/AirMentorPro2_SmartThings/master/images/nt_chancesleet.png"
                state "nt_chancesnow",	icon:"https://raw.githubusercontent.com/philippeportesppo/AirMentorPro2_SmartThings/master/images/nt_chancesnow.png"
                state "nt_chancetstorms",	icon:"https://raw.githubusercontent.com/philippeportesppo/AirMentorPro2_SmartThings/master/images/nt_chancetstorms.png"
                state "nt_clear",		icon:"st.Weather.weather4" 				//"https://raw.githubusercontent.com/philippeportesppo/AirMentorPro2_SmartThings/master/images/nt_clear.png"
                state "nt_cloudy",		icon:"st.Weather.weather13" 			//"https://raw.githubusercontent.com/philippeportesppo/AirMentorPro2_SmartThings/master/images/nt_cloudy.png"
                state "nt_flurries",	icon:"st.Weather.weather6" 				//"https://raw.githubusercontent.com/philippeportesppo/AirMentorPro2_SmartThings/master/images/nt_flurries.png"
                state "nt_fog",			icon:"st.Weather.weather8"				//"https://raw.githubusercontent.com/philippeportesppo/AirMentorPro2_SmartThings/master/images/nt_fog.png"
                state "nt_hazy",		icon:"st.Weather.weather8"				//"https://raw.githubusercontent.com/philippeportesppo/AirMentorPro2_SmartThings/master/images/nt_hazy.png"
                state "nt_mostlycloudy",icon:"st.Weather.weather13" 			//"https://raw.githubusercontent.com/philippeportesppo/AirMentorPro2_SmartThings/master/images/nt_mostlycloudy.png"
                state "nt_mostlysunny",	icon:"st.Weather.weather11" 			//"https://raw.githubusercontent.com/philippeportesppo/AirMentorPro2_SmartThings/master/images/nt_mostlysunny.png"
                state "nt_partlycloudy",icon:"st.Weather.weather15" 			//"https://raw.githubusercontent.com/philippeportesppo/AirMentorPro2_SmartThings/master/images/nt_partlycloudy.png"
                state "nt_sleet",		icon:"st.Weather.weather6" 				//"https://raw.githubusercontent.com/philippeportesppo/AirMentorPro2_SmartThings/master/images/nt_sleet.png"
                state "nt_rain",		icon:"st.Weather.weather10" 			//"https://raw.githubusercontent.com/philippeportesppo/AirMentorPro2_SmartThings/master/images/nt_rain.png"
                state "nt_sleet",		icon:"st.Weather.weather6" 				//"https://raw.githubusercontent.com/philippeportesppo/AirMentorPro2_SmartThings/master/images/nt_sleet.png"
                state "nt_snow",		icon:"st.Weather.weather7" 				//"https://raw.githubusercontent.com/philippeportesppo/AirMentorPro2_SmartThings/master/images/nt_snow.png"
                state "nt_sunny",		icon:"st.Weather.weather14"				//"https://raw.githubusercontent.com/philippeportesppo/AirMentorPro2_SmartThings/master/images/nt_sunny.png"
                state "nt_tstorms",		icon:"st.Weather.weather1" 				//"https://raw.githubusercontent.com/philippeportesppo/AirMentorPro2_SmartThings/master/images/nt_tstorms.png"
   		}
        
    standardTile("wind_gust_mph", "device.wind_gust_mph", width: 1, height: 1, canChangeIcon: false) {
            state "default", label: 'Wind Gusts ${currentValue} mph'} 
    standardTile("wind_mph", "device.wind_mph", width: 1, height: 1, canChangeIcon: false) {
            state "default", label: 'Wind Speed ${currentValue} mph'}
    standardTile("wind_dir", "device.wind_dir", width: 1, height: 1, canChangeIcon: false) {
            state "default", label: 'Wind Direction ${currentValue}'}
    standardTile("wind_string", "device.wind_string", width: 3, height: 1, canChangeIcon: false) {
            state "default", label: 'Wind ${currentValue}'}
    
    standardTile("pressure_mb", "device.pressure_mb", width: 3, height: 1, canChangeIcon: false) {
            state "default", label: 'Pressure ${currentValue} mb'}
    standardTile("pressure_trend", "device.pressure_trend", width: 3, height: 1, canChangeIcon: false) {
            state "default", label: 'Pressure Trend ${currentValue}'}
    
    standardTile("UV", "device.ultravioletIndex", width: 2, height: 1, canChangeIcon: false) {
            state "default", label: 'UV ${currentValue}'}        
  	standardTile("visibility_mi", "device.visibility_mi", width: 2, height: 1, canChangeIcon: false) {
            state "default", label: 'Visibility ${currentValue} mi'}                 
    
    standardTile("precip_1hr_metric", "device.precip_1hr_metric", width: 2, height: 1, canChangeIcon: false) {
            state "default", label: 'Rain rate ${currentValue} mm/hr'}
    standardTile("precip_today_metric", "device.precip_today_metric", width: 2, height: 1, canChangeIcon: false) {
            state "default", label: 'Rain today ${currentValue} mm'}
            
	standardTile("observation_time", "device.observation_time", width: 3, height: 1, canChangeIcon: false) {
            state "default", label: 'Observation Time ${currentValue}'}
    standardTile("station_id", "device.station_id", width: 3, height: 1, canChangeIcon: false) {
            state "default", label: 'Station id ${currentValue}'}
            
    standardTile("forcast", "device.forcast", width: 6, height: 2, canChangeIcon: false) {
            state "default", label: '${currentValue}'}
    
    
   
	main("wu_main")
	details(["UGW_web","temperature", "UGWFeelsLikelevel", "UGW_Icon_UrlIcon", "weather", "refresh", "wind_gust_mph", "wind_mph", "wind_dir", "wind_string","pressure_trend","pressure_mb","precip_1hr_metric","precip_today_metric","UV","visibility_mi","humidity","UGWdewpointlevel","observation_time","station_id","forcast"])
 	}
    preferences {
        input name: "postcode", type: "text", title: "Post Code", description: "leave blank for default hub location", required: false
        input "wusnowaction", "bool", title: "Snow action"
        input "wustormaction", "bool", title: "Storm action" 
       	input "wurainaction", "bool", title: "Rain action"
		input "wulowtempaction", "number", title: "Low temperature action (C or F)", required: false
 		input "wuhightempaction", "number", title: "High temperature action (C or F)", required: false
		input "wulowhumidityaction", "decimal", title: "Low humidity action (0-100)", required: false
        input "wuhighhumidityaction", "decimal", title: "High humidity action (0-100)", required: false 
	}
}

def installed() {
    log.debug "Executing 'installed'"
    updated()
}

def updated() {
	unschedule(refresh)
	log.debug "Executing 'updated'"
    state.snowalert=false
    state.stormalert=false
    state.rainalert=false
	state.lowtempalert=false
	state.hightempalert=false
    state.lowhumidityalert=false
    state.highhumidityalert=false
    refresh()
   runEvery5Minutes(refresh)
   //runEvery1Minute(refresh)
}

def poll(){
	log.debug "Executing 'poll'"
    refresh()
}

String convertTemperature( float temperatureCelcius, unit){
	float value = temperatureCelcius
    if (unit =="F")
    {
       value = temperatureCelcius * 1.8 + 32.0
    }
    return value.toString().format(java.util.Locale.US,"%.1f", value)
}

// parse events into attributes
def parse(String description) {
	log.warn "Executing 'parse'"
}

// handle commands
def refresh() {
//log.debug "Executing 'refresh'"
    def mymap = getWeatherFeature("conditions", settings.postcode)
	def formap = getWeatherFeature("forecast", settings.postcode)
    def alemap = getWeatherFeature("alerts", settings.postcode) /// works  "zmw:00000.1.16172" , nope zmw:00000.20.03318, nope zmw:00000.53.03334(manchester), zmw:00000.1.07093, zmw:00000.40.03779
    
    //getWeatherFeature("alerts", "Penwortham United Kingdom") //zmw:00000.71.03858, pws:ILOSTOCK2, Walton le Dale GB, Penwortham United Kingdom, United Kingdom(kinda works) , city:Penwortham, country:United Kingdom
 
//log.debug "${mymap}"
//log.debug "${formap.forecast.txt_forecast.forecastday}"
//log.debug "${formap.forecast.txt_forecast.forecastday['title']}"
	def focastdata = "@ " + formap?.forecast?.txt_forecast?.date + " " + formap?.forecast?.txt_forecast?.forecastday[0]?.title + " chance of rain " + formap?.forecast?.txt_forecast?.forecastday[0]?.pop + "%" + " Forcast  " + formap?.forecast?.txt_forecast?.forecastday[0]?.fcttext_metric
//log.debug "forcastdata ${focastdata} - ${formap.forecast.txt_forecast}"
	log.debug "${alemap}"
//	log.info "Response \nLocation: ${mymap['current_observation']['station_id']}, \ntemp oC: ${mymap['current_observation']['temp_c']}, \nFeelsLike oC: ${mymap['current_observation']['feelslike_c']}, \nDewpoint oC: ${mymap['current_observation']['dewpoint_c']}, \nRelative Humidity: ${mymap['current_observation']['relative_humidity']}, \nweather: ${mymap['current_observation']['weather']}"

// -- Rain ---- notification --- action ----
	if (mymap.current_observation.icon_url.contains("rain") && state.rainalert == false){
        //if (state.rainalert == false){
			if (getDataValue("wurainalert")=="true"){
            	sendEvent(name:"Alert", value: "WUW Rain Alert!", displayed:true)
        	}
        	if (settings.wurainaction == true && parent.wurainon!=null){ //put with other
            	log.info "rain on on"
               	parent.wurainon.on()
            }    
        	if (settings.wurainaction == true && parent.wurainoff!=null){
                log.info " rain off off"
                parent.wurainoff.off()
        	}
        state.rainalert = true
        }
	//}
	else {
    	if ( state.rainalert == true) {
            state.rainalert=false
            if (getDataValue("wurainalert")=="true"){
    			sendEvent(name:"Alert", value: "No Alert", displayed:true)
            	}
            if (settings.wurainaction == true && parent.wurainon!=null){
    			log.info " rain on off"
                parent.wurainon.off()
            }
            if (settings.wurainaction == true && parent.wurainoff!=null){
            	log.info " rain off on"
                parent.wurainoff.on()
            	}
			}
	}
// ---- Rain --- end ------
   

// -- Send all events data ------

	def temperatureScale = getTemperatureScale()
    sendEvent(name: "UGWFeelsLikelevel", value: convertTemperature(mymap.current_observation.feelslike_c.toFloat(),temperatureScale), unit: temperatureScale, displayed:false) //mymap['current_observation']['feelslike_c'].toFloat(),temperatureScale)
    sendEvent(name: "UGWdewpointlevel", value: convertTemperature(mymap.current_observation.dewpoint_c.toFloat(),temperatureScale), unit: temperatureScale, displayed:false)
    sendEvent(name: "humidity", value:  mymap.current_observation.relative_humidity, displayed:false) //.substring(0, mymap['current_observation']['relative_humidity'].length()-1)
    sendEvent(name: "temperature", value: convertTemperature(mymap.current_observation.temp_c.toFloat(),temperatureScale), unit: temperatureScale, displayed:false)
    sendEvent(name: "UGW_Icon_UrlIcon", value: mymap.current_observation.icon_url.substring(28,mymap.current_observation.icon_url.length()-4), displayed:false)
    sendEvent(name: "wu_main", value: mymap.current_observation.icon_url.substring(28,mymap.current_observation.icon_url.length()-4), displayed:false)
    sendEvent(name:"weather", value: mymap.current_observation.weather)
    sendEvent(name:"wind_gust_mph", value: mymap.current_observation.wind_gust_mph, displayed:false)
    sendEvent(name:"wind_mph", value: mymap.current_observation.wind_mph, displayed:false)
    sendEvent(name:"wind_dir", value: mymap.current_observation.wind_dir, displayed:false)
    sendEvent(name:"wind_string", value: mymap.current_observation.wind_string, displayed:false)
    sendEvent(name:"precip_1hr_metric", value: mymap.current_observation.precip_1hr_metric, displayed:false)
    sendEvent(name:"precip_today_metric", value: mymap.current_observation.precip_today_metric, displayed:false)
    sendEvent(name:"pressure_mb", value: mymap.current_observation.pressure_mb, displayed:false)
    sendEvent(name:"pressure_trend", value: mymap.current_observation.pressure_trend, displayed:false)
    sendEvent(name:"UV", value: mymap.current_observation.UV, displayed:false)
    sendEvent(name:"visibility_mi", value: mymap.current_observation.visibility_mi, displayed:false)
    sendEvent(name:"observation_time", value: mymap.current_observation.observation_time, displayed:false)
    sendEvent(name:"station_id", value: mymap.current_observation.station_id, displayed:false)
    sendEvent(name:"forcast", value: focastdata, displayed:false)
    
    

	log.info "Observation ${mymap.current_observation.observation_time}"   
	log.info "Forcast @ ${formap.forecast.txt_forecast.date}, ${formap.forecast.txt_forecast?.forecastday[0].title}, Percipitation = ${formap.forecast.txt_forecast?.forecastday[0].pop}%"

// --- Snow ----    
    if (getDataValue("wusnowalert")=="True" && mymap.current_observation.icon_url.contains("snow"))
    {
        if ( state.snowalert == false) {
            sendEvent(name:"Alert", value: "WUW Snow Alert!", displayed:true, isStateChange: true)
            state.snowalert=true  }
    }
    else
        state.snowalert=false
        

	
//log.info "end - state rain aleart = $state.rainalert, do action = $settings.wurainaction, send rain aleart = ${getDataValue("wurainalert")}"
/**
    if (getDataValue("wurainalert")=="true" && mymap['current_observation']['icon_url'].contains("rain")) // || mymap['current_observation']['weather'].contains("Drizzle"))
    {
    log.debug "raining"
        if ( state.rainalert == false) {
            sendEvent(name:"Alert", value: "WUW Rain Alert!", displayed:true) //, isStateChange: true
            state.rainalert = true
		}
    }
    else {
    log.debug "not raining"
    	if ( state.rainalert == true) {
        log.debug " not raining but state.rainalert is $state.rainalert"
    		sendEvent(name:"Alert", value: "No Alert", displayed:true)
    		state.rainalert=false
             if (parent.wurainon!=null)
    				parent.wurainon.off()
            if (parent.wurainoff!=null)
            parent.wurainoff.on()
		}
	}
**/
//======

    if (getDataValue("wustormalert")=="True" && mymap['current_observation']['icon_url'].contains("rain"))
    {
        if ( state.stormalert == false) {
            sendEvent(name:"Alert", value: "WUW Storm Alert!", displayed:true, isStateChange: true)
            state.stormalert=true  }
    }
    else
        state.stormalert=false

    if (getDataValue("wulowtempalert")!="null") {
        if (getDataValue("wulowtempalert").toFloat() >= convertTemperature(mymap['current_observation']['icon_url'].toFloat(),temperatureScale).toFloat())
        {

            if ( state.lowtempalert == false) {
                sendEvent(name:"Alert", value: "WUW Low Temperature Alert!", displayed:true, isStateChange: true)
                state.lowtempalert=True }
        }
        else
            state.lowtempalert=false
    }

    if (getDataValue("wuhightempalert")!="null") {
        if (getDataValue("wuhightempalert").toFloat() <= convertTemperature(mymap['current_observation']['icon_url'].toFloat(),temperatureScale).toFloat())
        {

            if ( state.hightempalert == false) {
                sendEvent(name:"Alert", value: "WUW High Temperature Alert!", displayed:true, isStateChange: true)
                state.hightempalert=frue }
        }
        else
            state.hightempalert=false
    }

    if (getDataValue("wulowhumidityalert")!="null") {
        if (getDataValue("wulowhumidityalert").toFloat() >= mymap['current_observation']['relative_humidity'].substring(0, mymap['current_observation']['relative_humidity'].length()-1).toFloat())
        {

            if ( state.lowhumidityalert == false) {

                sendEvent(name:"Alert", value: "WUW Low Humidity Alert!", displayed:true, isStateChange: true)
                state.lowhumidityalert=true }
        }
        else
        {
            state.lowhumidityalert=false

        }
    }

    if (getDataValue("wuhighhumidityalert")!="null") {

        if (getDataValue("wuhighhumidityalert").toFloat() <= mymap['current_observation']['relative_humidity'].substring(0, mymap['current_observation']['relative_humidity'].length()-1).toFloat())
        {
            if ( state.highhumidityalert == false) {
                sendEvent(name:"Alert", value: "WUW High Humidity Alert!", displayed:true, isStateChange: true)
                state.highhumidityalert=true }
        }
        else
            state.highhumidityalert=false
    }

}    