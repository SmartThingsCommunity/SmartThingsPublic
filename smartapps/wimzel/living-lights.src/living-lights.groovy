/**
 *  Living Lights
 *
 *  Copyright 2015 Alain Mena Galindo
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
name: "Living Lights",
namespace: "wimzel",
author: "Alain Mena Galindo",
description: "Have your lights brighten when you walk into a room and have them revert when your walk out. It's all about the motion!",
category: "My Apps",
iconUrl: "https://cloud.wimzel.com/index.php/apps/files_sharing/ajax/publicpreview.php?x=76&y=76&a=true&file=76x76.png&t=BIIe0l9rpkFoT2s&scalingup=0",
iconX2Url: "https://cloud.wimzel.com/index.php/apps/files_sharing/ajax/publicpreview.php?x=120&y=120&a=true&file=120x120.png&t=mIpelIItFvjnD9j&scalingup=0",
iconX3Url: "https://cloud.wimzel.com/index.php/apps/files_sharing/ajax/publicpreview.php?x=152&y=152&a=true&file=152x152.png&t=3d7Rouj24XVkHOC&scalingup=0")

// ========================================================
// PAGES - PREFERENCES
// ========================================================

preferences {
	page(name: "firstPage");
	page(name: "secondPage");
	page(name: "thirdPage");
}

// ========================================================
// PAGES - FIRST - PAGE
// ========================================================

def firstPage() {

	def hrefParams = [
	install: checkSomeCustomLogic()];

	dynamicPage(name: "firstPage", title: "LETS DO THIS!", nextPage: "secondPage", uninstall: true) {

		section("Thing #1") {
			image "https://cloud.wimzel.com/index.php/apps/files_sharing/ajax/publicpreview.php?x=1913&y=543&a=true&file=photo-1445096641463-96cbb311f5a4.jpg&t=GMMwAjayUS52qpA&scalingup=0";
			input "motionSensor", "capability.motionSensor", title: "Which motion motion sensor will be the one setting the mood?",
			multiple: false;
		}

		section("Click NEXT near the top right.") {
			paragraph "Have an idea or need help? Shoot me an email at alainmenag@gmail.com.";
		}

	}

}

// ========================================================
// PAGES - SECOND - PAGE
// ========================================================

def secondPage() {

	dynamicPage(name: "secondPage", title: "ALMOSE DONE!", nextPage: "thirdPage") {

		section("All lights will be controlled evenly.") {
			image "https://cloud.wimzel.com/index.php/apps/files_sharing/ajax/publicpreview.php?x=1913&y=543&a=true&file=which.png&t=qiz2btXPTLgOwia&scalingup=0";
			input "theLights", "capability.switchLevel", required: true, multiple: true, title: "Pick a few living lights:";
		}

		section("How your lights will work:") {
			paragraph "If they are off, they will turn on, if they are on, they will grow brighter. After sometime, they will revert to their previous state.";
			image "https://cloud.wimzel.com/index.php/apps/files_sharing/ajax/publicpreview.php?x=1913&y=543&a=true&file=brightness.png&t=PThdzV9YGHvkGXP&scalingup=0";
			input "brightnessLevel", "number", title: "How bright should the lights go? 10 is the minimum and 100% is the max.",
			defaultValue: 80,
			required: true;
		}

	}

}

// ========================================================
// PAGES - THIRD - PAGE
// ========================================================

def thirdPage() {

	state.reachedThirdPage = true;

	dynamicPage(name: "thirdPage", title: "KEEPING TIME", install: true) {

		section("THE BIG REVERT") {
			image "https://cloud.wimzel.com/index.php/apps/files_sharing/ajax/publicpreview.php?x=1913&y=543&a=true&file=revert.png&t=5zeMWf9np8APP28&scalingup=0";
			input "delayMins", "number", title: "After how many minutes of inactivity should your living lights wait before reverting to their previous state?",
			defaultValue: 5,
			required: true;
		}

		section("THE TIMEFRAME OR CYCLE") {
			image "https://cloud.wimzel.com/index.php/apps/files_sharing/ajax/publicpreview.php?x=1913&y=543&a=true&file=cycle.png&t=CxAQ55jbN9loSpd&scalingup=0";
			input "startingAt", "enum", title: "Starting At:",
			required: false,
			options: ["Sunrise", "Sunset"];
			input "startingTime", "time", title: "or time to Start:",
			required: false;
			input "endingAt", "enum", title: "Ending At:",
			required: false,
			options: ["Sunrise", "Sunset"];
			input "endingTime", "time", title: "or time to End:",
			required: false;
		}

		section("#FINALLY") {
			image "https://cloud.wimzel.com/index.php/apps/files_sharing/ajax/publicpreview.php?x=1913&y=543&a=true&file=days.png&t=2F3g0O9gOx0hXFC&scalingup=0";
			input "days", "enum", title: "On which days of the week should your lights come alive? (no selection means everyday):",
			multiple: true,
			required: false,
			options: [
				"Monday",
				"Tuesday",
				"Wednesday",
				"Thursday",
				"Friday",
				"Saturday",
				"Sunday"];
		}

	}

}

// ========================================================
// HELPERS
// ========================================================

def checkSomeCustomLogic() {
	false;
}

// ========================================================
// HREF - STATE
// ========================================================

def hrefState() {

	/* 
	 * `state: "complete"` makes the right side of the href green. 
	 * It's a great way to show the user that they've already set up some stuff on that page. 
	 * In other words, it's a great way to show state ;) 
	 * If you're using hrefs, it's a good idea to set `state` when appropriate. 
	 */

	state.reachedThirdPage ? "complete" : "";

}

// ========================================================
// HANDLERS - INSTALLED
// ========================================================

def installed() {
	state.installedAt = now();
	initialize();
}

// ========================================================
// HANDLERS - UPDATE
// ========================================================

def updated() {
	state.updatedAt = now();
	state.did = false;
	unsubscribe();
	initialize();
}

// ========================================================
// HANDLERS - INIT
// ========================================================

def initialize() {

	log.debug "***** Living Lights: Initialize and subscribe.";

	subscribe(motionSensor, "motion", sensorToggled);
	//subscribe(theLights, "switch", lightsToggled);

	timeframeCheck([])

}

// ========================================================
// HANDLERS - SCHEDULE CHECK
// ========================================================

def scheduleCheck() {

	log.debug "***** Living Lights: Scheduled check.";

	def motionState = motionSensor.currentState("motion");

	if (motionState.value == "inactive") {

		def elapsed = now() - motionState.rawDateCreated.time;
		def threshold = 1000 * 60 * delayMins - 1000;

		if (elapsed >= threshold) {

			log.debug "***** Living Lights: Motion has stayed inactive since last check ($elapsed ms) and no presence:  reverting lights.";
			state.did = false;

			theLights.each {

				def rev = state.revertTo.findAll({
					revert ->
					if (revert.id == it.id) return revert;
				});

				if (!rev) return;

				// DO NOT REVERT IF SWITCH IS OFF
				if (it.currentSwitch == "off") return;

				it.setLevel(rev.level[0]);

			};

			state.revertTo = [];

		} else {
			log.debug "***** Living Lights: Motion has not stayed inactive long enough since last check ($elapsed ms): do nothing.";
		}

	} else {
		log.debug "***** Living Lights: Motion is active: do nothing.";
	}

}

// ========================================================
// HANDLERS - STORE STATE
// ========================================================

def storeState() {

	log.debug "***** Living Lights: Store revert state."

	state.revertTo = [];

	theLights.each {
		def level = it.currentLevel;
		if (it.currentSwitch == "off") level = 0;
		state.revertTo.push([id: it.id, level: level]);
	};

}

// ========================================================
// HANDLERS - LIGHTS TOGGLED
// ========================================================

/* Somehow when we store the current state after a light toggle,
we're able to revert back to the original without deconfiguring
each light's current state. */

def lightsToggled(evt) {

	def rev = state.revertTo.findAll({
		revert ->
		if (revert.id == evt.deviceId) return revert;
	});

	if (!rev) return;

	// IF LIGHTS ARE TURNED OFF WHILE BEING SENSORED
	if (evt.value == 'off') return storeState();

}

// ========================================================
// HANDLERS - TIME FRAME CHECK
// ========================================================

def timeframeCheck(evt) {

	// STOP BY DATE
	def today = new Date().format("EEEE", location.timeZone);

	if (days) {
		if (days.contains(today)) {} else {
			return false;
		}
	}

	// STOP BY TIME
	def s = getSunriseAndSunset(zipCode: location.zipCode, sunriseOffset: 0, sunsetOffset: 0);
	def now = new Date();
	def startTime = null;
	def endTime = null;

	// SET FROM SUNRISE/SUNSET
	if (startingAt) startTime = s[startingAt.toLowerCase()];
	if (endingAt) endTime = s[endingAt.toLowerCase()];

	// SET FROM TIME/DATE
	if (startingTime) startTime = timeToday(startingTime);
	if (endingTime) endTime = timeToday(endingTime);

	if (!startTime || !endTime) return true;

	log.debug "***** Living Lights: It's now: ${now} and Start Time: ${startTime} and End Time: ${endTime}.";

	// BETWEEN TIMEFRAMES
	if (now >= startTime && now <= endTime) {} else {
		return false;
	}

	return true;

}

// ========================================================
// HANDLERS - SENSOR TOGGLED
// ========================================================

def sensorToggled(evt) {

	if (!state.did && !timeframeCheck(evt)) return;

	// STORE REVERT TO
	if (!state.did) storeState();

	if (evt.value == "active" && theLights.currentSwitch != "on") {
		theLights.setLevel(brightnessLevel);
		state.did = true;
	}

	if (evt.value == "inactive" && theLights.currentSwitch != "off") {
		runIn(delayMins * 60, scheduleCheck, [overwrite: false]);
	}

}