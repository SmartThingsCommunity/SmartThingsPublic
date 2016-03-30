/**
 *  Copyright 2015 Chuck Norris
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
 *  Unlock It When I Approach
 *
 *  Author: Norris
 *  Date: 2015-07-20
 */

definition(
    name: "Unlock It When I Aproach",
    namespace: "smartthings",
    author: "Norris",
    description: "Unlocks the door when you aproach. An attempt at seeing what it feels like to be Chuck Norris. Also helps if you flex your guns",
    category: "Safety & Security",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience%402x.png",
    oauth: true
)

preferences {
    section("When I arrive..."){
        input "motionSensor", "capability.motionSensor", title: "Where?", multiple: true
    }
    section("Unlock the lock..."){
        input "lock", "capability.lock", multiple: true
    }
}

def installed()
{
    subscribe(motionSensor, "motion", motionHandler)
}

def updated()
{
    unsubscribe()
    subscribe(motionSensor, "motion", motionHandler)
}

def motionHandler(evt)
{
    def anyLocked = lock.count{it.currentLock == "unlocked"} != lock.size()
    if (anyLocked) {
        sendPush "Unlocked door due to AWESOMENESS"
        lock.unlock()
    }
}