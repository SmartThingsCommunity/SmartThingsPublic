/**
 *  Two Way Switch Sync
 *
 *  Current Version: 1.0
 *
 *
 *
 *  Copyright 2015 Tim Slagle
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *	The original licensing applies, with the following exceptions:
 *		1.	These modifications may NOT be used without freely distributing all these modifications freely
 *			and without limitation, in source form.	 The distribution may be met with a link to source code
 *			with these modifications.
 *		2.	These modifications may NOT be used, directly or indirectly, for the purpose of any type of
 *			monetary gain.	These modifications may not be used in a larger entity which is being sold,
 *			leased, or anything other than freely given.
 *		3.	To clarify 1 and 2 above, if you use these modifications, it must be a free project, and
 *			available to anyone with "no strings attached."	 (You may require a free registration on
 *			a free website or portal in order to distribute the modifications.)
 *		4.	The above listed exceptions to the original licensing do not apply to the holder of the
 *			copyright of the original work.	 The original copyright holder can use the modifications
 *			to hopefully improve their original work.  In that event, this author transfers all claim
 *			and ownership of the modifications to "SmartThings."
 *
 *	Original Copyright information:
 *
 *	Copyright 2015 SmartThings
 *
 *	Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *	in compliance with the License. You may obtain a copy of the License at:
 *
 *		http://www.apache.org/licenses/LICENSE-2.0
 *
 *	Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *	on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *	for the specific language governing permissions and limitations under the License.
 *
 */
definition(
	name: "Two Way Switch Sync",
	namespace: "tslagle13",
	author: "Tim Slagle",
	description: "Syncs two swtich states between each other.",
	category: "Convenience",
	iconUrl: "https://s3.amazonaws.com/smartapp-icons/Meta/light_outlet.png",
	iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Meta/light_outlet@2x.png"
)

preferences {
	section("Switch 1") {
		input "switch1", "capability.contactSensor", required: true
	}
	section("Switch 2") {
		input "switch2", "capability.contactSensor", required: true
	}  
}

def installed()
{   
	subscribe(switch1, "switch.on", onHandler1)
	subscribe(switch1, "switch.off", offHandler1)
	subscribe(switch2, "switch.on", onHandler2)
    subscribe(switch2, "switch.off", offHandler2)
}

def updated()
{
	subscribe(switch1, "switch.on", onHandler1)
	subscribe(switch1, "switch.off", offHandler1)
	subscribe(switch2, "switch.on", onHandler2)
    subscribe(switch2, "switch.off", offHandler2)  
}

def onHandler1(evt) {
	if(switch1.latestValue("switch").contains("on")){
		switch2.on()
    }
}

def offHandler1(evt) {
	if(switch1.latestValue("switch").contains("off")){
		switch2.off()
    }
}

def onHandler2(evt) {
	if(switch2.latestValue("switch").contains("on")){
    	switch1.on()
    }
}

def offHandler2(evt) {
	if(switch2.latestValue("switch").contains("off")){
    	switch1.off()
    }
}