/**
 *  Weather Panel
 *
 *  Copyright 2015 Sidney Johnson
 *  If you like this code, please support the developer via PayPal: https://www.paypal.com/cgi-bin/webscr?cmd=_s-xclick&hosted_button_id=XKDRYZ3RUNR9Y
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
 *	Version: 1.0 - Initial Version
 *	Version: 1.1 - Fixed font size not changing the font size
 *	Version: 1.2 - Decoupled weather data refresh from wallpaper refresh
 *	Version: 1.3 - Minor formating tweaks, removed all static data from json
 *	Version: 2.0 - Addeded 3 day forcast and more formating and presentation tweaks. Removed weather station requirement
 *	Version: 2.1 - Preloads images for smoother transitions
 *	Version: 2.1.1 - Added dynamic API URL
 *	Version: 2.2 - Added support for user selectable Station ID
 *
 */
definition(
    name: "Weather Panel",
    namespace: "sidjohn1",
    author: "Sidney Johnson",
    description: "Weather Panel, a SmartThings web client",
    category: "Convenience",
    iconUrl: "http://cdn.device-icons.smartthings.com/Weather/weather14-icn.png",
    iconX2Url: "http://cdn.device-icons.smartthings.com/Weather/weather14-icn@2x.png",
    iconX3Url: "http://cdn.device-icons.smartthings.com/Weather/weather14-icn@3x.png",
    oauth: true)

preferences {
    page(name: "selectDevices")
    page(name: "viewURL")
}

def selectDevices() {
	dynamicPage(name: "selectDevices", install: true, uninstall: true) {
	    section("About") {
			paragraph "Weather Panel displays inside and outside temp and weather infomation as a web page. Also has a random customizable background serviced by Dropbox public folders."
			paragraph "${textVersion()}\n${textCopyright()}"
 	   }
		section("Select...") {
			input "insideTemp", "capability.temperatureMeasurement", title: "Inside Tempature...", multiple: false, required: true
			input "showForcast", "bool", title:"Show Forcast", required: false, multiple:false
            input "stationID", "text", title:"Station ID (Optional)", required: false, multiple:false
		}
		section(hideable: true, hidden: true, "Optional Settings") {
        	input "fontColor", "bool", title: "Font Color Black", required: false
			input "fontSize", "enum", title:"Select Font Size", required: true, multiple:false, defaultValue: "Medium", metadata: [values: ['Small','Medium','Large']]
			input "outsideWeather", "capability.temperatureMeasurement", title: "Clear to free weather device", multiple: true, required: false
		}
		section("Dropbox Wallpaper") {
			input "dbuid", "number", title: "Dropbox Public UID",defaultValue: "57462297", required:false
		}
        section() {
			href "viewURL", title: "View URL"
		}
	}
}

def viewURL() {
	dynamicPage(name: "viewURL", title: "${title ?: location.name} Weather Pannel URL", install:false) {
		section() {
			paragraph "Copy the URL below to any modern browser to view your ${title ?: location.name}s' Weather Panel. Add a shortcut to home screen of your mobile device to run as a native app."
			input "weatherUrl", "text", title: "URL",defaultValue: "${generateURL("html")}", required:false
			href url:"${generateURL("html")}", style:"embedded", required:false, title:"View", description:"Tap to view, then click \"Done\""
		}
	}
}

mappings {
    path("/html") {
		action: [
			GET: "generateHtml",
		]
	}
	path("/json") {
		action: [
			GET: "generateJson",
		]
	}
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
	log.info "Weather Panel ${textVersion()} ${textCopyright()}"
	generateURL()
}

def generateHtml() {
	render contentType: "text/html", data: "<!DOCTYPE html>\n<html>\n<head>${head()}</head>\n<body>\n${body()}\n</body></html>"
}

def generateJson() {
	render contentType: "application/json", data: "${jsonData()}"
}

def head() {

def color1
def color2
def font1
def font2
def font3
def iconW
def temp1TA
def temperatureScale = getTemperatureScale()
def weatherDataContent

switch (fontSize) {
	case "Large":
	font1 = "50"
	font2 = "20"
	font3 = "10"
	break;
	case "Medium":
	font1 = "48"
	font2 = "18"
	font3 = "10"
	break;
	case "Small":
	font1 = "46"
	font2 = "16"
	font3 = "10"
	break;
}

if (settings.fontColor) {
	color1 = "0,0,0"
	color2 = "255,255,255"
}
else {
	color1 = "255,255,255"
	color2 = "0,0,0"
}


if (showForcast == true) {
	iconW = "47"
	temp1TA = "right"
	weatherDataContent = """	    		content += '<div id="icon"><i class="wi wi-' + item.icon + '"></i></div>';
	    		content += '<div id="temp1" class="text3"><p>' + item.temp1 + '°<b>${temperatureScale}<br>Inside</b><br>' + item.temp2 + '°<b>${temperatureScale}<br>Outside</b><br></p></div>';
    			content += '<div id="cond" class="text2"><p>' + item.cond + '&nbsp;</p></div>';
    			content += '<div id="forecast" class="text3"><p>' + item.forecastDay + '<br><i class="wi wi-' + item.forecastIcon + '"></i>&nbsp;' + item.forecastDayHigh + '<br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<u>' + item.forecastDayLow + '</u></p><br></div>';
    			content += '<div id="forecast" class="text3"><p>' + item.forecastDay1 + '<br><i class="wi wi-' + item.forecastIcon1 + '"></i>&nbsp;' + item.forecastDayHigh1 + '<br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<u>' + item.forecastDayLow1 + '</u></p><br></div>';
    			content += '<div id="forecast" class="text3"><p>' + item.forecastDay2 + '<br><i class="wi wi-' + item.forecastIcon2 + '"></i>&nbsp;' + item.forecastDayHigh2 + '<br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<u>' + item.forecastDayLow2 + '</u></p><br></div>';
    			content += '<div id="forecast" class="text3"><p>' + item.forecastDay3 + '<br><i class="wi wi-' + item.forecastIcon3 + '"></i>&nbsp;' + item.forecastDayHigh3 + '<br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<u>' + item.forecastDayLow3 + '</u></p><br></div>';"""
}
   else {
	iconW = "100"
	temp1TA = "left"
   	weatherDataContent = """	    		content += '<div id="icon"><i class="wi wi-' + item.icon + '"></i></div>';
	    		content += '<div id="temp1" class="text1"><p>' + item.temp1 + '°<b>${temperatureScale}<br>Inside</b></p></div>';
	    		content += '<div id="temp2" class="text1"><p>' + item.temp2 + '°<b>${temperatureScale}<br>Outside</b></p></div>';
    			content += '<div id="cond" class="text1"><p>' + item.cond + '&nbsp;</p></div>';"""
}

"""<!-- Meta Data -->
	<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
	<meta name="Description" content="Weather Panel" />
	<meta name="application-name" content="Weather Panel" />
	<meta name="apple-mobile-web-app-title" content="Weather Panel">
	<meta name="keywords" content="weather,panel,smartthings" />
	<meta name="Author" content="sidjohn1" />
<!-- Apple Web App -->
	<meta name="apple-mobile-web-app-capable" content="yes" />
	<meta name="mobile-web-app-capable" content="yes" />
	<meta name="apple-mobile-web-app-status-bar-style" content="black-translucent" />
	<meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=0" />
	<link rel="apple-touch-icon-precomposed" href="https://sidjohn1.github.io/smartthings/WeatherPanel/index.png" />
<!-- Stylesheets -->
<style type="text/css">
body{
	background-size: cover;
	background-attachment: fixed;
	background-color: rgb(${color2});
	background-position: center;
	overflow: hidden;
	margin: 0 0;
	width: 100%;
	height: 100%;
}
b{
	font-size: 20px;
	font-size: ${font3}vh;
	vertical-align: super;
}
p{
	font-family:Gotham, "Helvetica Neue", Helvetica, Arial, sans-serif;
	color: rgb(${color1});
	text-shadow: 2px 1px 0px rgb(${color2});
	margin:0 0;
	opacity: 0.9;
}
i{
	color: rgb(${color1});
	text-shadow: 2px 1px 0px rgb(${color2});
	vertical-align: middle;
	opacity: 0.9;
}
div{
	background: transparent;
}
u{
	text-decoration: overline;
}
.text1 {
	font-weight: bold;
	vertical-align: text-top;
	margin-top: -3%;
}
.text2 {
	font-weight: bold;
	vertical-align: super;
	margin-top: -3%;
	margin-bottom: 1%;
}
.text3 {
	font-weight: bold;
	vertical-align: super;
}
#data {
	display: flex;
	display: -webkit-flex;
	flex-direction: row;
	-webkit-flex-direction: row;
	flex-wrap: wrap;
	-webkit-flex-wrap: wrap;
}
#icon{
	margin: 2% 1%;
	font-size: 20px;
	font-size: ${font1}vh;
	text-align: center;
	width: ${iconW}%;
}
#temp1{
	text-align: ${temp1TA};
	float: left;
	width: 48%;
	margin-left: 2%;
	font-size: 20px;
	font-size: ${font2}vh;
	line-height: 13vh;
}
#temp2{
	text-align: right;
	float: right;
	width: 48%;
	margin-right: 2%;
	font-size: 20px;
	font-size: ${font2}vh;
	line-height: 13vh;
}
#cond{
	white-space: nowrap;
	text-align: right;
	width: 100%;
	font-size: 20px;
	font-size: ${font3}vh;
}
#forecast{
	white-space: nowrap;
	text-align: center;
	width: 25%;
	font-size: 20px;
	font-size: 7vh;
	background: rgba(${color2},.5);
	vertical-align: middle;
}
</style>
<link type="text/css" rel="stylesheet" href="https://sidjohn1.github.io/smartthings/WeatherPanel/index.css"/>
<link rel="shortcut icon" type="image/png" href="https://sidjohn1.github.io/smartthings/WeatherPanel/index.png"/>
<link rel="manifest" href="https://sidjohn1.github.io/smartthings/WeatherPanel/manifest.json">
    <!-- Page Title -->
    <title>Weather Panel</title>
  	<!-- Javascript -->
<script type="text/javascript" charset="utf-8" src="https://sidjohn1.github.io/smartthings/WeatherPanel/index.js"></script>
<script type="text/javascript">
\$(window).load(function(){
	var bg = '';
	var tImage = new Image();
	\$("#data").click(function(){
		var path="https://dl.dropboxusercontent.com/u/${dbuid}/Wallpaper/";
		var fileList = "index.json";
		\$.getJSON(path+fileList,function(list,status){
			var mime = '*';
			while (mime.search('image')){
				obj = list[Math.floor(Math.random()*list.length)];
				mime=obj.mime;
			}
			bg = path+obj.path;
			bg = bg.replace('#','%23');
            \$('<img src="'+bg+'"/>');
            setTimeout(function(){
				document.body.background = bg;
			},3000);
		});
        setTimeout('\$("#data").click()', 1800000);
	});
	\$("#data").click();
});
</script>
<script type="text/javascript">
\$(document).ready(function(){
	weatherData = function () {
		\$.getJSON("${generateURL("json")}",function(weather){
		var content = '';
			\$.each(weather.data, function(i,item){
${weatherDataContent}
				\$("#data").empty();
    			\$(content).appendTo("#data");
    		});
    	});
    	setTimeout(weatherData, 240000);
	}
	weatherData();
});
</script>
"""
}

def body() {  
"""<div id="data"></div>"""
}

def jsonData(){
log.debug "refreshing weather"
sendEvent(linkText:app.label, name:"weatherRefresh", value:"refreshing weather", descriptionText:"weatherRefresh is refreshing weather", eventType:"SOLUTION_EVENT", displayed: true)

def current
def currentTemp
def forecast
def forecastDayHigh
def forecastDayHigh1
def forecastDayHigh2
def forecastDayHigh3
def forecastDayLow
def forecastDayLow1
def forecastDayLow2
def forecastDayLow3
def temperatureScale = getTemperatureScale()

def weatherIcons = []

if (settings.stationID) {
	forecast = getWeatherFeature("forecast", "pws:"+settings.stationID)
	current = getWeatherFeature("conditions", "pws:"+settings.stationID)
}
else if (settings.zipcode) {
	forecast = getWeatherFeature("forecast", settings.zipcode)
	current = getWeatherFeature("conditions", settings.zipcode)
}
else {
	forecast = getWeatherFeature("forecast")
	current = getWeatherFeature("conditions")
}
if (temperatureScale == "F") {
	currentTemp  = Math.round(current.current_observation.temp_f)
	forecastDayHigh = forecast.forecast.simpleforecast.forecastday[0].high.fahrenheit
	forecastDayHigh1 = forecast.forecast.simpleforecast.forecastday[1].high.fahrenheit
	forecastDayHigh2 = forecast.forecast.simpleforecast.forecastday[2].high.fahrenheit
	forecastDayHigh3 = forecast.forecast.simpleforecast.forecastday[3].high.fahrenheit
	forecastDayLow = forecast.forecast.simpleforecast.forecastday[0].low.fahrenheit
	forecastDayLow1 = forecast.forecast.simpleforecast.forecastday[1].low.fahrenheit
	forecastDayLow2 = forecast.forecast.simpleforecast.forecastday[2].low.fahrenheit
	forecastDayLow3 = forecast.forecast.simpleforecast.forecastday[3].low.fahrenheit
}
else {
	currentTemp  = Math.round(current.current_observation.temp_c)
	forecastDayHigh = forecast.forecast.simpleforecast.forecastday[0].high.celsius
	forecastDayHigh1 = forecast.forecast.simpleforecast.forecastday[1].high.celsius
	forecastDayHigh2 = forecast.forecast.simpleforecast.forecastday[2].high.celsius
	forecastDayHigh3 = forecast.forecast.simpleforecast.forecastday[3].high.celsius
	forecastDayLow = forecast.forecast.simpleforecast.forecastday[0].low.celsius
	forecastDayLow1 = forecast.forecast.simpleforecast.forecastday[1].low.celsius
	forecastDayLow2 = forecast.forecast.simpleforecast.forecastday[2].low.celsius
	forecastDayLow3 = forecast.forecast.simpleforecast.forecastday[3].low.celsius
}

weatherIcons = ["chanceflurries" : "day-snow", "chancerain" : "day-rain", "chancesleet" : "day-rain-mix", "chancesnow" : "day-snow", "chancetstorms" : "day-thunderstorm", "clear" : "day-sunny", "cloudy" : "day-cloudy", "flurries" : "day-snow", "fog" : "day-fog", "hazy" : "day-haze", "mostlycloudy" : "day-cloudy", "mostlysunny" : "day-sunny", "partlycloudy" : "day-cloudy", "partlysunny" : "day-cloudy", "rain" : "day-rain", "sleet" : "day-sleet", "snow" : "day-snow", "sunny" : "day-sunny", "tstorms" : "day-thunderstorm", "nt_chanceflurries" : "night-alt-snow", "nt_chancerain" : "night-alt-rain", "nt_chancesleet" : "night-alt-hail", "nt_chancesnow" : "night-alt-snow", "nt_chancetstorms" : "night-alt-thunderstorm", "nt_clear" : "night-clear", "nt_cloudy" : "night-alt-cloudy", "nt_flurries" : "night-alt-snow", "nt_fog" : "night-fog", "nt_hazy" : "dust", "nt_mostlycloudy" : "night-alt-cloudy", "nt_mostlysunny" : "night-alt-cloudy", "nt_partlycloudy" : "night-alt-cloudy", "nt_partlysunny" : "night-alt-cloudy", "nt_sleet" : "night-alt-rain-mix", "nt_rain" : "night-alt-rain", "nt_snow" : "night-alt-snow", "nt_sunny" : "night-clear", "nt_tstorms" : "night-alt-thunderstorm"]

def forecastNow = weatherIcons[current.current_observation.icon]
def forecastDayIcon = weatherIcons[forecast.forecast.simpleforecast.forecastday[0].icon]
def forecastDay1Icon = weatherIcons[forecast.forecast.simpleforecast.forecastday[1].icon]
def forecastDay2Icon = weatherIcons[forecast.forecast.simpleforecast.forecastday[2].icon]
def forecastDay3Icon = weatherIcons[forecast.forecast.simpleforecast.forecastday[3].icon]

"""{"data": [{"icon":"${forecastNow}","cond":"${current.current_observation.weather}","temp1":"${insideTemp.currentValue("temperature")}","temp2":"${currentTemp}"
,"forecastDay":"${forecast.forecast.simpleforecast.forecastday[0].date.weekday_short}","forecastIcon":"${forecastDayIcon}","forecastDayHigh":"${forecastDayHigh}","forecastDayLow":"${forecastDayLow}"
,"forecastDay1":"${forecast.forecast.simpleforecast.forecastday[1].date.weekday_short}","forecastIcon1":"${forecastDay1Icon}","forecastDayHigh1":"${forecastDayHigh1}","forecastDayLow1":"${forecastDayLow1}"
,"forecastDay2":"${forecast.forecast.simpleforecast.forecastday[2].date.weekday_short}","forecastIcon2":"${forecastDay2Icon}","forecastDayHigh2":"${forecastDayHigh2}","forecastDayLow2":"${forecastDayLow2}"
,"forecastDay3":"${forecast.forecast.simpleforecast.forecastday[3].date.weekday_short}","forecastIcon3":"${forecastDay3Icon}","forecastDayHigh3":"${forecastDayHigh3}","forecastDayLow3":"${forecastDayLow3}"}]}"""
}

private def generateURL(data) {    
	if (!state.accessToken) {
		try {
			createAccessToken()
			log.debug "Creating new Access Token: $state.accessToken"
		} catch (ex) {
			log.error "Enable OAuth in SmartApp IDE settings for Weather Panel"
			log.error ex
		}
    }
	def url = "${getApiServerUrl()}/api/smartapps/installations/${app.id}/${data}?access_token=${state.accessToken}"
return "$url"
}

private def textVersion() {
    def text = "Version 2.2"
}

private def textCopyright() {
    def text = "Copyright © 2015 Sidjohn1"
}