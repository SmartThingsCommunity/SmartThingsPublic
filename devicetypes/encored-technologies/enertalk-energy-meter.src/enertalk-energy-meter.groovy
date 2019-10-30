/**
 *  EnerTalk Energy Meter
 *
 *  Copyright 2015 hyeon seok yang
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
metadata {
	definition (name: "EnerTalk Energy Meter", namespace: "Encored Technologies", author: "hyeon seok yang") {
	}

	simulator {
		// TODO: define status and reply messages here
	}

	tiles(scale:2) {
    	valueTile("view", "device.view", decoration: "flat") {
       		state "view", label:' ${currentValue} kWh'
		}
		valueTile("month", "device.month", width: 6, height : 3, decoration: "flat") {
       		state "month", label:' ${currentValue}'
		}
        valueTile("real", "device.real", width: 2, height : 2, decoration: "flat") {
       		state "real", label:' ${currentValue}'
		}
        valueTile("tier", "device.tier", width: 2, height : 2, decoration: "flat") {
       		state "tier", label:' ${currentValue}'
		}
        valueTile("plan", "device.plan", width: 2, height : 2, decoration: "flat") {
       		state "plan", label:' ${currentValue}'
		}
        
        htmlTile(name:"deepLink", action:"linkApp", whitelist:["code.jquery.com", 
    	"ajax.googleapis.com", 
        "fonts.googleapis.com",
    	"code.highcharts.com", 
    	"enertalk-card.encoredtech.com", 
    	"s3-ap-northeast-1.amazonaws.com",
        "s3.amazonaws.com", 
    	"ui-hub.encoredtech.com",
        "enertalk-auth.encoredtech.com",
        "api.encoredtech.com",
        "cdnjs.cloudflare.com",
        "encoredtech.com",
        "itunes.apple.com"], width:2, height:2){}
        
        main (["view"])
        details (["month", "real", "tier", "plan", "deepLink"])
	}
}

mappings {
	
      path("/linkApp") {action: [ GET: "getLinkedApp" ]}
}

def getLinkedApp() {
	def lang = clientLocale?.language
    if ("${lang}" == "ko") {
    	lang = "<p style=\'margin-left:15vw; color: #aeaeb0;\'>기기 설정</p>"
    } else {
    	lang = "<p style=\'margin-left:5vw; color: #aeaeb0;\'>Setup Device</p>"
    }
	renderHTML() {
        head {
           """
			<meta name="viewport" content="initial-scale=1, maximum-scale=1, user-scalable=no, width=device-width, height=device-height">
           	<style>
            	#레이어_1 { margin-left : 17vw; width : 50vw; height : 50vw;}
                .st0{fill:#B5B6BB;}
            </style>
        """ 
        }
        body {
        """
            <div id="container">
            	<a id="st-deep-link" href="#">
                	<svg version="1.1" id="레이어_1" xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink" x="0px" y="0px" viewBox="0 0 40 40" style="enable-background:new 0 0 40 40;" xml:space="preserve"><path class="st0" d="M20,0C9,0,0,9,0,20C0,30.5,8,39,18.2,40l3.8-4.8l-3.9-4.8c-4.9-0.9-8.6-5.2-8.6-10.4c0-5.8,4.7-10.5,10.5-10.5
					S30.5,14.2,30.5,20c0,5.1-3.7,9.4-8.5,10.3l3.7,4.5L21.8,40C32,39.1,40,30.5,40,20C40,9,31,0,20,0z"/></svg>
                </a>
                ${lang} 
            </div>
            <script src="https://ajax.googleapis.com/ajax/libs/jquery/1.11.3/jquery.min.js"></script>
            <script>
           		var ua = navigator.userAgent.toLowerCase();
                var isAndroid = ua.indexOf("android") > -1;
                if(!isAndroid) { 
                	\$("#st-deep-link").attr("href", "https://itunes.apple.com/kr/app/enertalk-for-home/id1024660780?mt=8");
                } else {
                	\$("#st-deep-link").attr("href", "market://details?id=com.ionicframework.enertalkhome874425");
                }
            </script>
        """
        }
	}
}