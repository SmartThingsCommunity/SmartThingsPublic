/**
 *  Spruce Controller zone child *
 *  Copyright 2017 Plaid Systems
 *
 *	Author: NC
 *	Date: 2017-6
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
 -------------6-2017 update---------------
 * child for composite Spruce Controller
 * 
 */
 
 metadata {
	definition (name: 'Spruce zone', namespace: 'plaidsystems', author: 'Plaid Systems') {
		capability 'Switch'
        capability 'Actuator'
        capability 'Valve'
        
        command 'on'
        command 'off'
	}    
    tiles {
        standardTile('switch', 'switch', inactiveLabel: false) {		
            state 'off', label: 'off', action: 'on', icon: 'st.valves.water.closed', backgroundColor: '#ffffff'
            state 'on', label: 'on', action: 'off', icon: 'st.valves.water.open', backgroundColor: '#00A0DC'
        }
        main "switch"
        details (["switch"])
    }
}

def installed(){
	
}

void on(){	
	parent.zoneon(device.deviceNetworkId)    
}

void off(){	
    parent.zoneoff(device.deviceNetworkId)    
}