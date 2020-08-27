/**
 *  TV Channel Creator
 *
 *  Copyright 2017 Rebecca Onuschak
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
definition(
    name: "TV Channel Creator",
    namespace: "beckyricha",
    author: "Rebecca Onuschak",
    description: "Simple app to map TV channel names to numbers.",
    category: "Convenience",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png")


preferences {
	   page( name:"adder", title:"Add channel, then press done. Remove uninstalls this app.", uninstall:true, install:true ) 
    {
    section() 
        {input "channelname", "text", title: "Channel Name (as you want to say/see it)",defaultValue:""
        input "channelnum", "text", title: "Channel Number (as your remote enters it)",defaultValue:""}
    }
}

def installed() {
	initialize()
}

def updated() {
	initialize()
}

def initialize() {
def DevID = "channel$channelnum"
def existing = getChildDevice(DevID)
def i=0
while(existing){
	DevID = "channel$channelnum${i}"
	existing = getChildDevice(DevID)
    i=i+1
}
def mydevice = addChildDevice("beckyricha", "dummySwitch", DevID, location.hubs[0].id,["label":"$channelname","name":"$channelname"])
mydevice.sendEvent(name:"guidenum", value:"$channelnum")
}