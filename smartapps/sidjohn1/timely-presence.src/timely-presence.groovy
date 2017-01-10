/**
 *  Timely Presence
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
 *	Version: 1.0.1 - Added dynamic API URL
 *
 */
definition(
    name: "Timely Presence",
    namespace: "sidjohn1",
    author: "Sidney Johnson",
    description: "Timely Presence, a SmartThings web client",
    category: "Convenience",
    iconUrl: "http://cdn.device-icons.smartthings.com/Home/home4-icn.png",
    iconX2Url: "http://cdn.device-icons.smartthings.com/Home/home4-icn@2x.png",
    iconX3Url: "http://cdn.device-icons.smartthings.com/Home/home4-icn@3x.png",
    oauth: true)

preferences {
	page(name: "selectDevices", install: true, uninstall: true) {
	    section("About") {
			paragraph "Timely Presence displays the current local time and the presence of the members of the house as the background."
			paragraph "${textVersion()}\n${textCopyright()}"
 	   }
		section("Select Family Members...") {
			input "presenceFm", "capability.presenceSensor", title: "Who...", multiple: true, required: true
			input "linkFm", "text", title: "Picture Folder URL", required: true
		}
		section(hideable: true, hidden: true,"Timely Presence Advanced Options") {
			input "fontTo", "enum", title:"Select Font Size", required: true, multiple:false, defaultValue: "Medium", metadata: [values: ['Small','Medium','Large']]
			input "colorTo", "bool", title: "Font Color White", required: false
            input "formatTo", "bool", title: "Time Format: 24hr", required: false
            input "ampmTo", "bool", title: "Hide AM/PM", required: false
            input "timeTo", "bool", title: "Hide Time", required: false
		}
		section() {
			href "viewURL", title: "View URL"
		}
	}
    page(name: "viewURL")
}

def viewURL() {
	dynamicPage(name: "viewURL", title: "${title ?: location.name} Timely Presence URL", install:false) {
		section() {
			paragraph "Copy the URL below to any modern browser to view your ${title ?: location.name}s' Timely Presence. Add a shortcut to home screen of your mobile device to run as a native app."
			input "timelyUrl", "text", title: "URL",defaultValue: "${generateURL("html")}", required:false
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
	unsubscribe()
	initialize()
}

def initialize() {
	log.info "Timely Presence ${textVersion()} ${textCopyright()}"
    generateURL()
    subscribe(presence, "presence", generateJson)
}


def generateHtml() {
	render contentType: "text/html", data: """<!DOCTYPE html>\n<html>\n<head>${head()}</head>\n<body onload="startTime()">\n${body()}\n</body>\n</html>"""
}

def generateJson() {
	render contentType: "application/json", data: "${jsonData()}"
}

def head() {

def font1 = ""
def font2 = ""
def color1 = ""
def color2 = ""
def ampm = ""
def format = ""
def time = ""

switch (settings.fontTo) {
	case "Large":
	font1 = "58"
	font2 = "12"
	break;
	case "Medium":
	font1 = "55"
	font2 = "10"
	break;
	case "Small":
	font1 = "53"
	font2 = "9"
	break;
}

if (settings.colorTo) {
	color1 = "black"
	color2 = "white"
} else {
	color1 = "white"
	color2 = "black"
}

if (settings.ampmTo) {
	ampm = ""
} else {
	ampm = """document.getElementById('ampm').innerHTML = ap;"""
}

if (settings.formatTo) {
	format = ""
    ampm = ""
} else {
	format = """var ap = "A M";\n\tif (h > 11) { ap = "P M";}\n\tif (h > 12) { h = h - 12;}\n\tif (h === 0) { h = 12;}"""
}

if (settings.timeTo) {
	time = ""
    ampm = ""
} else {
	time = """document.getElementById('time').innerHTML = h+":"+m;"""
}
"""<!-- Meta Data -->
	<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
	<meta name="Description" content="Timely Presence" />
	<meta name="application-name" content="Timely Presence" />
	<meta name="apple-mobile-web-app-title" content="Timely Presence">
	<meta name="keywords" content="time,presence,smartthings" />
	<meta name="Author" content="sidjohn1" />
<!-- Apple Web App -->
	<meta name="apple-mobile-web-app-capable" content="yes" />
	<meta name="mobile-web-app-capable" content="yes" />
	<meta name="apple-mobile-web-app-status-bar-style" content="black-translucent" />
	<meta name="viewport" content = "width = device-width, initial-scale = 1.0, maximum-scale=1.0, user-scalable=0" />
	<link rel="apple-touch-icon-precomposed" href="https://sidjohn1.github.io/smartthings/TimelyPresence/index.png" />
<!-- Stylesheets -->
<style type="text/css">
body{
    background-color: ${color1};
	margin: 0 0;
    overflow: hidden;
}
div{
	background: transparent;
	margin:0 0;
	width: 100%;
	height: 100%;
}
img{
	width: 100%;
	height: 100%;
}
.away {
	filter: grayscale(100%);
	-webkit-filter: grayscale(100%);
}
.home {
	filter: grayscale(0%);
	-webkit-filter: grayscale(0%);
}
.time {
	opacity: 0.9;
	position: absolute;
	left: 0;
	width: 100%;
	height: 100%;
	text-align: center;
	font-family:Gotham, "Helvetica Neue", Helvetica, Arial, sans-serif;
	color: ${color2};
	text-shadow: 2px 1px 0px ${color1};
    font-weight: bold;
}
#data {
	display: flex;
    display: -webkit-flex;
    flex-direction: row;
    -webkit-flex-direction: row;
	width: 100vw;
	height: 100vh;
}
#time {
	top: 30%;
	font-size: ${font1}vh;
    font-weight: 500;
	transform:scale(1,1.7);
    -webkit-transform:scale(1,1.7);
}
#ampm {
	top: 85%;
	font-size: ${font2}vh;
}
</style>
    <!-- Page Title -->
    <title>Timely Presence</title>
  	<!-- Javascript -->
<script type="text/javascript" charset="utf-8" src="https://sidjohn1.github.io/smartthings/TimelyPresence/index.js"></script>
<script type="text/javascript">
function startTime() {
    var today = new Date();
    var h = today.getHours();
    var m = today.getMinutes();
	${format}
	if (h < 10) { h = "&nbsp" + h;}
	if (m < 10) { m = "0" + m;}
    ${time}
	${ampm}
    var t = setTimeout(function(){startTime()},500);
}
</script>
<script type="text/javascript">
\$(document).ready(function(){
	presenceData = function () {
		\$.getJSON("${generateURL("json")}",function(presence){
		var content = '';
			\$.each(presence.data, function(i,item){
				\$.each(this, function( key, value ) {
					\$("#" + key).attr("class",value);
                });
    		});
    	});
    	setTimeout(presenceData, 30000);
	}
	presenceData();
});
</script>
"""
}

def body() {

def presenceData = ""
		settings.presenceFm.each() {
            try {
				def presenceStatus = ["not present" : "away","present" : "home"]
				def presenceShort = presenceStatus[it.currentPresence]
				presenceData += """\n<div id="${it.displayName}" class="${presenceShort}">\n<img src="${settings.linkFm}${it.displayName}.jpg"/>\n</div>"""              
            } catch (e) {
                log.trace "Error checking status."
                log.trace e
            }
        }
"""<div id="data">\n${presenceData.trim()}\n</div>\n<div id="time" class="time"></div>\n<div id="ampm" class="time"></div>"""
}

def jsonData(){

def presenceData = ""
		settings.presenceFm.each() {
            try {
				def presenceStatus = ["not present" : "away","present" : "home"]
				def presenceShort = presenceStatus[it.currentPresence]
				presenceData += "\"$it.displayName\":\"$presenceShort\","
            } catch (e) {
                log.trace "Error checking status."
                log.trace e
            }
        }
        presenceData = presenceData[0..-2]

"""{"data": [{${presenceData.trim()}}]}"""
}

private def generateURL(data) {    
	if (!state.accessToken) {
		try {
			createAccessToken()
			log.debug "Creating new Access Token: $state.accessToken"
		} catch (ex) {
			log.error "Enable OAuth in SmartApp IDE settings for Timely Presence"
			log.error ex
		}
    }
	def url = "${getApiServerUrl()}/api/smartapps/installations/${app.id}/${data}?access_token=${state.accessToken}"
    return "$url"
}

private def textVersion() {
    def text = "Version 1.0.1"
}

private def textCopyright() {
    def text = "Copyright © 2015 Sidjohn1"
}