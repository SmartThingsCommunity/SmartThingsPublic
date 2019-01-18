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

	standardTile("UGW_web", "device.UGW_web",  width: 6, height: 3,  canChangeIcon: false ) {
            state "default", label: '${currentValue}', unit:'${currentValue}'     }  // , icon: "st.Weather.weather2" 
             
    standardTile("temperature", "device.temperature", width: 2, height: 1,  canChangeIcon: false) { //decoration: "flat",
            state "default", label: 'Temperature ${currentValue}º',unit:'${currentValue}', icon: "st.Weather.weather2" }  //,backgroundColor:"#e5e9ea"
        
	standardTile("humidity", "device.humidity", width: 2, height: 1, canChangeIcon: false) {
            state "default", label: 'Humidity: ${currentValue}%', icon: "st.Weather.weather12"} //, backgroundColor:"#e5e9ea"      
            
	standardTile("UGWFeelsLikelevel", "device.UGWFeelsLikelevel",  width: 2, height: 1,  canChangeIcon: false) { //decoration: "flat",
            state "default",  label: 'Feels Like Temp ${currentValue}º',unit:'${currentValue}',icon: "https://raw.githubusercontent.com/philippeportesppo/AirMentorPro2_SmartThings/master/images/realfeel.png"} //, backgroundColor:"#e5e9ea"

	standardTile("UGWdewpointlevel", "device.UGWdewpointlevel",  width: 2, height: 1, canChangeIcon: false) {
            state "default", label: 'Dewpoint ${currentValue}º',unit:'${currentValue}',icon: "https://raw.githubusercontent.com/philippeportesppo/AirMentorPro2_SmartThings/master/images/dewpoint.png"} //, backgroundColor:"#e5e9ea"
  
    standardTile("UGW_Icon_UrlIcon", "device.UGW_Icon_UrlIcon", decoration: "flat",   width: 3, height: 1) {
                state "00", icon:"https://smartthings-twc-icons.s3.amazonaws.com/00.png", label: ""
            state "01", icon:"https://smartthings-twc-icons.s3.amazonaws.com/01.png", label: ""
            state "02", icon:"https://smartthings-twc-icons.s3.amazonaws.com/02.png", label: ""
            state "03", icon:"https://smartthings-twc-icons.s3.amazonaws.com/03.png", label: ""
            state "04", icon:"https://smartthings-twc-icons.s3.amazonaws.com/04.png", label: ""
            state "05", icon:"https://smartthings-twc-icons.s3.amazonaws.com/05.png", label: ""
            state "06", icon:"https://smartthings-twc-icons.s3.amazonaws.com/06.png", label: ""
            state "07", icon:"https://smartthings-twc-icons.s3.amazonaws.com/07.png", label: ""
            state "08", icon:"https://smartthings-twc-icons.s3.amazonaws.com/08.png", label: ""
            state "09", icon:"https://smartthings-twc-icons.s3.amazonaws.com/09.png", label: ""
            state "10", icon:"https://smartthings-twc-icons.s3.amazonaws.com/10.png", label: ""
            state "11", icon:"https://smartthings-twc-icons.s3.amazonaws.com/11.png", label: ""
            state "12", icon:"https://smartthings-twc-icons.s3.amazonaws.com/12.png", label: ""
            state "13", icon:"https://smartthings-twc-icons.s3.amazonaws.com/13.png", label: ""
            state "14", icon:"https://smartthings-twc-icons.s3.amazonaws.com/14.png", label: ""
            state "15", icon:"https://smartthings-twc-icons.s3.amazonaws.com/15.png", label: ""
            state "16", icon:"https://smartthings-twc-icons.s3.amazonaws.com/16.png", label: ""
            state "17", icon:"https://smartthings-twc-icons.s3.amazonaws.com/17.png", label: ""
            state "18", icon:"https://smartthings-twc-icons.s3.amazonaws.com/18.png", label: ""
            state "19", icon:"https://smartthings-twc-icons.s3.amazonaws.com/19.png", label: ""
            state "20", icon:"https://smartthings-twc-icons.s3.amazonaws.com/20.png", label: ""
            state "21", icon:"https://smartthings-twc-icons.s3.amazonaws.com/21.png", label: ""
            state "22", icon:"https://smartthings-twc-icons.s3.amazonaws.com/22.png", label: ""
            state "23", icon:"https://smartthings-twc-icons.s3.amazonaws.com/23.png", label: ""
            state "24", icon:"https://smartthings-twc-icons.s3.amazonaws.com/24.png", label: ""
            state "25", icon:"https://smartthings-twc-icons.s3.amazonaws.com/25.png", label: ""
            state "26", icon:"https://smartthings-twc-icons.s3.amazonaws.com/26.png", label: ""
            state "27", icon:"https://smartthings-twc-icons.s3.amazonaws.com/27.png", label: ""
            state "28", icon:"https://smartthings-twc-icons.s3.amazonaws.com/28.png", label: ""
            state "29", icon:"https://smartthings-twc-icons.s3.amazonaws.com/29.png", label: ""
            state "30", icon:"https://smartthings-twc-icons.s3.amazonaws.com/30.png", label: ""
            state "31", icon:"https://smartthings-twc-icons.s3.amazonaws.com/31.png", label: ""
            state "32", icon:"https://smartthings-twc-icons.s3.amazonaws.com/32.png", label: ""
            state "33", icon:"https://smartthings-twc-icons.s3.amazonaws.com/33.png", label: ""
            state "34", icon:"https://smartthings-twc-icons.s3.amazonaws.com/34.png", label: ""
            state "35", icon:"https://smartthings-twc-icons.s3.amazonaws.com/35.png", label: ""
            state "36", icon:"https://smartthings-twc-icons.s3.amazonaws.com/36.png", label: ""
            state "37", icon:"https://smartthings-twc-icons.s3.amazonaws.com/37.png", label: ""
            state "38", icon:"https://smartthings-twc-icons.s3.amazonaws.com/38.png", label: ""
            state "39", icon:"https://smartthings-twc-icons.s3.amazonaws.com/39.png", label: ""
            state "40", icon:"https://smartthings-twc-icons.s3.amazonaws.com/40.png", label: ""
            state "41", icon:"https://smartthings-twc-icons.s3.amazonaws.com/41.png", label: ""
            state "42", icon:"https://smartthings-twc-icons.s3.amazonaws.com/42.png", label: ""
            state "43", icon:"https://smartthings-twc-icons.s3.amazonaws.com/43.png", label: ""
            state "44", icon:"https://smartthings-twc-icons.s3.amazonaws.com/44.png", label: ""
            state "45", icon:"https://smartthings-twc-icons.s3.amazonaws.com/45.png", label: ""
            state "46", icon:"https://smartthings-twc-icons.s3.amazonaws.com/46.png", label: ""
            state "47", icon:"https://smartthings-twc-icons.s3.amazonaws.com/47.png", label: ""
            state "na", icon:"https://smartthings-twc-icons.s3.amazonaws.com/na.png", label: ""
      }
            
    standardTile("refresh", "device.refresh", decoration: "flat", width: 1, height: 1) {
 		state "default", action:"refresh", icon:"st.secondary.refresh"
 		} 
	
    standardTile("weather", "device.weather", width: 2, height: 1) {
 		state "default", label:'${currentValue}'
 		} 

    standardTile("wu_main", "device.wu_main", decoration: "flat", width: 6, height: 4) {
            state "00", icon:"https://smartthings-twc-icons.s3.amazonaws.com/00.png", label:'${currentValue}'
            state "01", icon:"https://smartthings-twc-icons.s3.amazonaws.com/01.png"
            state "02", icon:"https://smartthings-twc-icons.s3.amazonaws.com/02.png"
            state "03", icon:"https://smartthings-twc-icons.s3.amazonaws.com/03.png"
            state "04", icon:"https://smartthings-twc-icons.s3.amazonaws.com/04.png"
            state "05", icon:"https://smartthings-twc-icons.s3.amazonaws.com/05.png"
            state "06", icon:"https://smartthings-twc-icons.s3.amazonaws.com/06.png"
            state "07", icon:"https://smartthings-twc-icons.s3.amazonaws.com/07.png"
            state "08", icon:"https://smartthings-twc-icons.s3.amazonaws.com/08.png"
            state "09", icon:"https://smartthings-twc-icons.s3.amazonaws.com/09.png"
            state "10", icon:"https://smartthings-twc-icons.s3.amazonaws.com/10.png"
            state "11", icon:"https://smartthings-twc-icons.s3.amazonaws.com/11.png", label: '${name}'
            state "12", icon:"https://smartthings-twc-icons.s3.amazonaws.com/12.png"
            state "13", icon:"https://smartthings-twc-icons.s3.amazonaws.com/13.png"
            state "14", icon:"https://smartthings-twc-icons.s3.amazonaws.com/14.png"
            state "15", icon:"https://smartthings-twc-icons.s3.amazonaws.com/15.png"
            state "16", icon:"https://smartthings-twc-icons.s3.amazonaws.com/16.png"
            state "17", icon:"https://smartthings-twc-icons.s3.amazonaws.com/17.png"
            state "18", icon:"https://smartthings-twc-icons.s3.amazonaws.com/18.png"
            state "19", icon:"https://smartthings-twc-icons.s3.amazonaws.com/19.png"
            state "20", icon:"https://smartthings-twc-icons.s3.amazonaws.com/20.png"
            state "21", icon:"https://smartthings-twc-icons.s3.amazonaws.com/21.png"
            state "22", icon:"https://smartthings-twc-icons.s3.amazonaws.com/22.png"
            state "23", icon:"https://smartthings-twc-icons.s3.amazonaws.com/23.png"
            state "24", icon:"https://smartthings-twc-icons.s3.amazonaws.com/24.png"
            state "25", icon:"https://smartthings-twc-icons.s3.amazonaws.com/25.png"
            state "26", icon:"https://smartthings-twc-icons.s3.amazonaws.com/26.png"
            state "27", icon:"https://smartthings-twc-icons.s3.amazonaws.com/27.png"
            state "28", icon:"https://smartthings-twc-icons.s3.amazonaws.com/28.png"
            state "29", icon:"https://smartthings-twc-icons.s3.amazonaws.com/29.png"
            state "30", icon:"https://smartthings-twc-icons.s3.amazonaws.com/30.png"
            state "31", icon:"https://smartthings-twc-icons.s3.amazonaws.com/31.png"
            state "32", icon:"https://smartthings-twc-icons.s3.amazonaws.com/32.png"
            state "33", icon:"https://smartthings-twc-icons.s3.amazonaws.com/33.png"
            state "34", icon:"https://smartthings-twc-icons.s3.amazonaws.com/34.png"
            state "35", icon:"https://smartthings-twc-icons.s3.amazonaws.com/35.png"
            state "36", icon:"https://smartthings-twc-icons.s3.amazonaws.com/36.png"
            state "37", icon:"https://smartthings-twc-icons.s3.amazonaws.com/37.png"
            state "38", icon:"https://smartthings-twc-icons.s3.amazonaws.com/38.png"
            state "39", icon:"https://smartthings-twc-icons.s3.amazonaws.com/39.png"
            state "40", icon:"https://smartthings-twc-icons.s3.amazonaws.com/40.png"
            state "41", icon:"https://smartthings-twc-icons.s3.amazonaws.com/41.png"
            state "42", icon:"https://smartthings-twc-icons.s3.amazonaws.com/42.png"
            state "43", icon:"https://smartthings-twc-icons.s3.amazonaws.com/43.png"
            state "44", icon:"https://smartthings-twc-icons.s3.amazonaws.com/44.png"
            state "45", icon:"https://smartthings-twc-icons.s3.amazonaws.com/45.png"
            state "46", icon:"https://smartthings-twc-icons.s3.amazonaws.com/46.png"
            state "47", icon:"https://smartthings-twc-icons.s3.amazonaws.com/47.png"
            state "na", icon:"https://smartthings-twc-icons.s3.amazonaws.com/na.png"
      }
        
    standardTile("wind_gust_mph", "device.wind_gust_mph", width: 1, height: 1, canChangeIcon: false, inactiveLabel: true){
            state "default", label: 'Wind Gusts ${currentValue} '} 
    standardTile("wind_mph", "device.wind_mph", width: 1, height: 1, canChangeIcon: false, inactiveLabel: true) {
            state "default", label: 'Wind Speed ${currentValue} mph'}
    standardTile("wind_dir", "device.wind_dir", width: 1, height: 1, canChangeIcon: false, inactiveLabel: true) {
            state "default", label: 'Wind Direction ${currentValue}'}
//    standardTile("wind_string", "device.wind_string", width: 3, height: 1, canChangeIcon: false) {
//            state "default", label: '${currentValue}'}
    
    standardTile("pressure_mb", "device.pressure_mb", width: 3, height: 1, canChangeIcon: false) {
            state "default", label: 'Pressure ${currentValue} mb'}
    standardTile("pressure_trend", "device.pressure_trend", width: 3, height: 1, canChangeIcon: false) {
            state "default", label: 'Pressure Trend ${currentValue}'}
    
    standardTile("UV", "device.ultravioletIndex", width: 2, height: 1, canChangeIcon: false) {
            state "default", label: 'UV level: ${currentValue}'}        
  	standardTile("visibility_mi", "device.visibility_mi", width: 2, height: 1, canChangeIcon: false) {
            state "default", label: 'Visability Distance: ${currentValue}'}                 
    
    standardTile("precip_1hr_metric", "device.precip_1hr_metric", width: 3, height: 1, canChangeIcon: false) {
            state "default", label: 'Rain past hour: ${currentValue}'}
    standardTile("precip_today_metric", "device.precip_today_metric", width: 3, height: 1, canChangeIcon: false) {
            state "default", label: 'Rain past 24H: ${currentValue}'}
            
	standardTile("observation_time", "device.observation_time", width: 3, height: 1, canChangeIcon: false) {
            state "default", label: 'Observation Time ${currentValue}'}
    
    standardTile("alert", "device.alert", width: 5, height: 1, canChangeIcon: false) {
            state "default", label: 'Alerts:${currentValue}'}
            
    standardTile("forcast", "device.forcast", width: 6, height: 3, inactiveLabel: true, canChangeIcon: false) { //decoration: "flat"
            state "default", label: 'FORCAST \n${currentValue}',defaultState: true} //,defaultState: true inactiveLabel: true,
       
	main("wu_main")
	details(["UGW_web","alert","temperature", "UGWFeelsLikelevel",  "weather", "refresh", "wind_gust_mph", "wind_mph", "wind_dir", "UGW_Icon_UrlIcon", "pressure_trend","pressure_mb","precip_1hr_metric","precip_today_metric","UV","visibility_mi","humidity","UGWdewpointlevel","observation_time","forcast"]) //"wind_string",
 	}
    preferences {
        input name: "postcode", type: "text", title: "US ZIP or Lat,Lon", description: "leave blank for default hub location 38.25,-76.45", required: false
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
	unschedule()
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
  
    def hublocation = getTwcLocation(settings.postcode)
    def newcurrent = getTwcConditions(settings.postcode)
    def newalemap = getTwcAlerts(settings.postcode)
    def newforcast = getTwcForecast(settings.postcode)
    def forcastdetail = "${newforcast.daypart[0].daypartName[0]} - ${newforcast.daypart[0].narrative[0]} \n${newforcast.daypart[0].daypartName[1]} - ${newforcast.daypart[0].narrative[1]}\n${newforcast.daypart[0].daypartName[2]} - ${newforcast.daypart[0].narrative[2]}\n${newforcast.daypart[0].daypartName[3]} - ${newforcast.daypart[0].narrative[3]}\n${newforcast.daypart[0].daypartName[4]} - ${newforcast.daypart[0].narrative[4]}"


//log.debug "\n HUB PosCode - $hublocation \n DEFALT - $hublocationfix"

//log.debug "CURRENT - ${newcurrent}"
log.debug "ALERTS ${newalemap}"
//log.debug "FORCAST ${newforcast.daypart[0].daypartName[1]} - ${newforcast.daypart[0].dayOrNight[1]} - ${newforcast.daypart[0].narrative[1]} "


//log.debug "FORCAST ${newforcast}"

// -- Rain ---- notification --- action ----

	if (newcurrent.wxPhraseLong.contains("Rain") || newcurrent.wxPhraseLong.contains("Showers")) { //if rain our showers
		log.debug "rain or showers"
        if (state.rainalert == false){ //if not already changed state
        	log.debug "NEW rain or showers"
			if (getDataValue("wurainalert")=="true"){ //send push
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
        else {log.debug "STILL rain or showers"}
}
	//}
 else { //else not raining
    	if ( state.rainalert == true) { //if was raining
        log.debug "WAS rain or showers"
            state.rainalert=false //state not raining
            if (getDataValue("wurainalert")=="true"){ //push
    			sendEvent(name:"Alert", value: "No Alert", displayed:true)
            	}
            if (settings.wurainaction == true && parent.wurainon!=null){ //action off
    			log.info " rain on off"
                parent.wurainon.off()
            }
            if (settings.wurainaction == true && parent.wurainoff!=null){ //action off
            	log.info " rain off on"
                parent.wurainoff.on()
            	}
			}
     }
// ---- Rain --- end ------

//log.debug "alert data $alert" //eventDescription
//def aldetails = getTwcAlertDetail(String alertId) //from alert

// -- Send all events data ------

	def temperatureScale = getTemperatureScale()
    
   sendEvent(name: "UGWFeelsLikelevel",	value: "${newcurrent.temperatureFeelsLike} ${temperatureScale}", unit: temperatureScale, displayed:false) //mymap['current_observation']['feelslike_c'].toFloat(),temperatureScale)
   sendEvent(name: "UGWdewpointlevel", 	value: "${newcurrent.temperatureDewPoint} ${temperatureScale}", unit: temperatureScale, displayed:false)
   sendEvent(name: "humidity", 			value: "${newcurrent.relativeHumidity}", displayed:false) //.substring(0, mymap['current_observation']['relative_humidity'].length()-1)
   sendEvent(name: "temperature", 		value: "${newcurrent.temperature} ${temperatureScale}", unit: temperatureScale, displayed:false)
   sendEvent(name: "UGW_Icon_UrlIcon", 	value: "${newcurrent.iconCode}", displayed:false)
   sendEvent(name: "wu_main", 			value: "${newcurrent.iconCode}", displayed:false)
   sendEvent(name:"weather", 			value: "${newcurrent.wxPhraseMedium}")
   sendEvent(name:"wind_gust_mph", 		value: "${newcurrent.windGust}", displayed:false)
   sendEvent(name:"wind_mph", 			value: "${newcurrent.windSpeed}", displayed:false)
   sendEvent(name:"wind_dir", 			value: "${newcurrent.windDirectionCardinal}", displayed:false)
   sendEvent(name:"wind_string", 		value: "${newcurrent.wxPhraseLong}", displayed:false) //now get wearther conditions
   sendEvent(name:"precip_1hr_metric", 	value: "${newcurrent.precip1Hour}", displayed:false)
   sendEvent(name:"precip_today_metric", value: "${newcurrent.precip24Hour}", displayed:false)
   sendEvent(name:"pressure_mb", 		value: "${newcurrent.pressureAltimeter}", displayed:false)
   sendEvent(name:"pressure_trend", 	value: "${newcurrent.pressureTendencyTrend}", displayed:false)
   sendEvent(name:"visibility_mi", 		value: "${newcurrent.visibility}", displayed:false)
   sendEvent(name:"observation_time", 	value: "${newcurrent.validTimeLocal}", displayed:false)
   sendEvent(name:"alert", 				value: "${newalemap.headlineText}", displayed:false)
   
//   sendEvent(name:"forcast", 			value: "${newforcast.daypart[0].daypartName[0]} - ${newforcast.daypart[0].narrative[0]} \n${newforcast.daypart[0].daypartName[1]} - ${newforcast.daypart[0].narrative[1]}\n${newforcast.daypart[0].daypartName[2]} - ${newforcast.daypart[0].narrative[2]}\n${newforcast.daypart[0].daypartName[3]} - ${newforcast.daypart[0].narrative[3]}\n${newforcast.daypart[0].daypartName[4]} - ${newforcast.daypart[0].narrative[4]}", displayed:false)
    sendEvent(name:"forcast", 			value: forcastdetail, displayed:false)
  
   sendEvent(name:"ultravioletIndex",	value: "${newcurrent.uvDescription}", displayed:false)
   sendEvent(name:"UGW_web", 			value: forcastdetail , displayed:false) //what to do with ?? "${newcurrent.temperatureFeelsLike}"
    
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

    if (getDataValue("wustormalert")=="True" && newcurrent.wxPhraseMedium.contains("rain"))
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