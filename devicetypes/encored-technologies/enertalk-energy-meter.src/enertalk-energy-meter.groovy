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
        
        htmlTile(name:"deepLink", action:"linkApp", whiltelist:["code.jquery.com", "ajax.googleapis.com", "code.highcharts.com", "enertalk-card.encoredtech.com", "s3-ap-northeast-1.amazonaws.com"], width:2, height:2){}
        
        main (["view"])
        details (["month", "real", "tier", "plan", "deepLink"])
	}
}

mappings {
	
      path("/linkApp") {action: [ GET: "getLinkedApp" ]}
}

def getLinkedApp() {
	renderHTML() {
        head {
           """
        	<meta name="viewport" content="initial-scale=1, maximum-scale=1, user-scalable=no, width=device-width, height=device-height">
           
        """ 
        }
        body {
        """
         	<div id="container">
            	<a id=\'st-deep-link\' href=\'market://details?id=com.ionicframework.enertalkhome874425\'><img alt='EnerTalk' src='https://s3-ap-northeast-1.amazonaws.com/smartthings-images/btn_setup_normal.svg' /></a>
          	</div>
        """
        }
	}
}