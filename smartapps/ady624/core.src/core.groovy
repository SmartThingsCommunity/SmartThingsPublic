/**
 *  CoRE - Community's own Rule Engine
 *
 *  Copyright 2016 Adrian Caramaliu <ady624("at" sign goes here)gmail.com>
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 *  Version history
 */
def version() {	return "v0.3.16d.20170828" }
/*
 *	08/28/2017 >>> v0.3.16d.20170828 - RC - Fixed a problem where the value for emergencyHeat() was mistakenly set to "emergencyHeat" instead of "emergency heat" - thanks @RBoy
 *	06/07/2017 >>> v0.3.16c.20170607 - RC - Extended setVideoLength to 120s for Blink cameras
 *	05/15/2017 >>> v0.3.16b.20170515 - RC - Disable running/paused piston counts on main page to speed up load process
 *	04/17/2017 >>> v0.3.16a.20170417 - RC - Fixed a problem with internal HTTP requests passing query strings instead of body - thank you @destructure00
 *	01/04/2017 >>> v0.3.169.20170104 - RC - Moved colors() Map into core ST color utility to reduce Class file size and avoid Class file too large errors
 *	12/20/2016 >>> v0.3.168.20161220 - RC - Fixed a bug with loading attributes coolingSetpoint and heatingSetpoint from variables, thank you @bridaus for pointing it out, also extended conditional time options to 360 minutes
 *	12/06/2016 >>> v0.3.167.20161206 - RC - Added some capabilities back - Light Bulb - removed Step Sensor as there is no more room :(
 *	11/21/2016 >>> v0.3.166.20161120 - RC - Added some capabilities back - had to remove some to make room for EchoSistant - CoRE is now reaching the max code base limit
 *	11/20/2016 >>> v0.3.165.20161120 - RC - DO NOT UPGRADE TO THIS UNLESS REQUESTED TO - Added support for EchoSistant, also fixed some bug with httpRequest (and added some extra logs)
 *	11/18/2016 >>> v0.3.164.20161118 - RC - Fixed a loose type casting causing Android ST 2.2.2 to fail - thank you @rappleg for the fix, also now encoding uri for web requests - may break things
 *	11/02/2016 >>> v0.3.163.20161102 - RC - Adjustments to better fit the Ring integration - assuming 1 button if no numberOfButtons (may break other DTH implementations), assuming button #1 pushed if no buttonNumber is provided
 *	10/28/2016 >>> v0.3.162.20161028 - RC - Minor speed improvement for getNextConditionId()
 *	10/27/2016 >>> v0.3.161.20161027 - RC - Fixed a bug affecting the queueAskAlexaMessage virtual command task
 *	10/14/2016 >>> v0.3.160.20161014 - RC - Fixed a bug not allowing Set color to work when using HSL instead of a simple color. Compliments to @simonselmer
 *	10/04/2016 >>> v0.3.15f.20161004 - RC - Code trim down to avoid "Class file too large!" error in JVM
 *	10/03/2016 >>> v0.3.15e.20161003 - RC - Fixed a problem where latching pistons would not allow both conditional blocks to run for Simulate, Execute, Follow Up
 *	10/02/2016 >>> v0.3.15d.20161002 - RC - Added some logging for LIFX integration
 *	10/01/2016 >>> v0.3.15c.20161001 - RC - Added LIFX integration
 *	 9/28/2016 >>> v0.3.15b.20160928 - RC - Fix for internal web requests - take 2
 *	 9/28/2016 >>> v0.3.15a.20160928 - RC - Fix for internal web requests
 *	 9/28/2016 >>> v0.3.159.20160928 - RC - Added low(), med(), and high() support (standard command instead of custom) for the zwave fan speed control
 *	 9/28/2016 >>> v0.3.158.20160928 - RC - Minor fixes where state.app or state.config.app was not yet initialized - though I could not replicate the issue
 *	 9/28/2016 >>> v0.3.157.20160928 - RC - Added support for local http requests - simply use a local IP in the HTTP request and CoRE will use the hub for that request - don't expect any results back yet :(
 *	 9/27/2016 >>> v0.3.156.20160927 - RC - Fixed a bug that was bleeding the time from offset into the time to for piston restrictions
 *	 9/26/2016 >>> v0.3.155.20160926 - RC - Added lock user codes support and cancel on condition state change
 *	 9/21/2016 >>> v0.3.154.20160921 - RC - DO NOT UPDATE TO THIS UNLESS REQUESTED TO - Lock user codes tested OK, adding "Cancel on condition state change", testing
 *	 9/21/2016 >>> v0.3.153.20160921 - RC - DO NOT UPDATE TO THIS UNLESS REQUESTED TO - Improved support for lock user codes
 *	 9/21/2016 >>> v0.3.152.20160921 - RC - DO NOT UPDATE TO THIS UNLESS REQUESTED TO - Added support for lock user codes
 *	 9/20/2016 >>> v0.3.151.20160920 - RC - Release Candidate is here! Added Pause/Resume Piston tasks
 */

/******************************************************************************/
/*** CoRE DEFINITION														***/
/******************************************************************************/

definition(
	name: "CoRE${parent ? " - Piston" : ""}",
	namespace: "ady624",
	author: "Adrian Caramaliu",
	description: "CoRE - Community's own Rule Engine",
	singleInstance: true,
	parent: parent ? "ady624.CoRE" : null,
	category: "Convenience",
	iconUrl: "https://cdn.rawgit.com/ady624/CoRE/master/resources/images/app-CoRE.png",
	iconX2Url: "https://cdn.rawgit.com/ady624/CoRE/master/resources/images/app-CoRE@2x.png",
	iconX3Url: "https://cdn.rawgit.com/ady624/CoRE/master/resources/images/app-CoRE@2x.png"
)

preferences {
	//common pages
	page(name: "pageMain")
	page(name: "pageViewVariable")
	page(name: "pageDeleteVariable")
	page(name: "pageRemove")

	//CoRE pages
	page(name: "pageInitializeDashboard")
	page(name: "pageStatistics")
	page(name: "pagePistonStatistics")
	page(name: "pageChart")
	page(name: "pageGlobalVariables")
	page(name: "pageGeneralSettings")
	page(name: "pageDashboardTaps")
	page(name: "pageDashboardTap")
	page(name: "pageIntegrateIFTTT")
	page(name: "pageIntegrateIFTTTConfirm")
	page(name: "pageIntegrateLIFX")
	page(name: "pageIntegrateLIFXConfirm")
	page(name: "pageResetSecurityToken")
	page(name: "pageResetSecurityTokenConfirm")
	page(name: "pageRecoverAllPistons")
	page(name: "pageRebuildAllPistons")

	//Piston pages
	page(name: "pageIf")
	page(name: "pageIfOther")
	page(name: "pageThen")
	page(name: "pageElse")
	page(name: "pageCondition")
	page(name: "pageConditionGroupL1")
	page(name: "pageConditionGroupL2")
	page(name: "pageConditionGroupL3")
	page(name: "pageConditionGroupL4")
	page(name: "pageConditionGroupL5")
	page(name: "pageActionGroup")
	page(name: "pageAction")
	page(name: "pageActionDevices")
	page(name: "pageVariables")
	page(name: "pageSetVariable")
	page(name: "pageSimulate")
	page(name: "pageRebuild")
	page(name: "pageToggleEnabled")
	page(name: "pageInitializeVariable")
	page(name: "pageInitializedVariable")
	page(name: "pageInitializeVariable")
	page(name: "pageInitializedVariable")
}

/******************************************************************************/
/*** CoRE CONSTANTS															***/
/******************************************************************************/

private triggerPrefix() { return "● " }

private conditionPrefix() {	return "◦ " }

private virtualCommandPrefix() { return "● " }

private customAttributePrefix() { return "⌂ " }

private customCommandPrefix() { return "⌂ " }

private customCommandSuffix() { return "(..)" }

/******************************************************************************/
/*** 																		***/
/*** CONFIGURATION PAGES													***/
/*** 																		***/
/******************************************************************************/

/******************************************************************************/
/*** COMMON PAGES															***/
/******************************************************************************/
def pageMain() {
	parent ? pageMainCoREPiston() : pageMainCoRE()
}

def pageViewVariable(params) {
	def var = params?.var
	dynamicPage(name: "pageViewVariable", title: "", uninstall: false, install: false) {
		if (var) {
			section() {
				paragraph var, title: "Variable name", required: false
				def value = getVariable(var)
				if (value == null) {
					paragraph "Undefined value (null)", title: "Oh-oh", required: false
				} else {
					def type = "string"
					if (value instanceof Boolean) {
						type = "boolean"
					} else if ((value instanceof Long) && (value >= 999999999999)) {
						type = "time"
					} else if ((value instanceof Float) || ((value instanceof String) && value.isFloat())) {
						type = "decimal"
					} else if ((value instanceof Integer) || ((value instanceof String) && value.isInteger())) {
						type = "number"
					}
					paragraph "$type", title: "Data type", required: false
					paragraph "$value", title: "Raw value", required: false
					value = getVariable(var, true)
					paragraph "$value", title: "Display value", required: false
				}
				if (!var.startsWith("\$")) {
					href "pageDeleteVariable", title: "Delete variable", description: "CAUTION: Tapping here will delete this variable and its value", params: [var: var], required: false
				}
			}
		} else {
			section() {
				paragraph "Sorry, variable not found.", required: false
			}
		}
	}
}

def pageDeleteVariable(params) {
	def var = params?.var
	dynamicPage(name: "pageInitializedVariable", title: "", uninstall: false, install: false) {
		if (var != null) {
			section() {
				deleteVariable(var)
				paragraph "Variable {$var} was successfully deleted.\n\nPlease tap < or Done to continue.", title: "Success", required: false
			}
		} else {
			section() {
				paragraph "Sorry, variable not found.", required: false
			}
		}
	}
}

def pageRemove() {
	dynamicPage(name: "pageRemove", title: "", install: false, uninstall: true) {
		section() {
			paragraph parent ? "CAUTION: You are about to remove the '${app.label}' piston. This action is irreversible. If you are sure you want to do this, please tap on the Remove button below." : "CAUTION: You are about to completely remove CoRE and all of its pistons. This action is irreversible. If you are sure you want to do this, please tap on the Remove button below.", required: true, state: null
		}
	}
}

/******************************************************************************/
/*** CoRE PAGES																***/
/******************************************************************************/
private pageMainCoRE() {
	initializeCoREStore()
	rebuildTaps()
	//CoRE main page
	dynamicPage(name: "pageMain", title: "", install: true, uninstall: false) {
		section() {
			if (!state.endpoint) {
				href "pageInitializeDashboard", title: "CoRE Dashboard", description: "Tap here to initialize the CoRE dashboard", image: "https://cdn.rawgit.com/ady624/CoRE/master/resources/images/icons/dashboard.png", required: false
			} else {
				//reinitialize endpoint
				initializeCoREEndpoint()
				def url = "${state.endpoint}dashboard"
				debug "Dashboard URL: $url *** DO NOT SHARE THIS LINK WITH ANYONE ***", null, "info"
				href "", title: "CoRE Dashboard", style: "external", url: url, image: "https://cdn.rawgit.com/ady624/CoRE/master/resources/images/icons/dashboard.png", required: false
			}
		}
		/* removed to allow execution with large number of pistons - ST lowered the timeout from 60s to 20s, causing this to fail. A LOT.
		section() {
			def apps = getChildApps().sort{ it.label }
			def running = apps.findAll{ it.getPistonEnabled() }.size()
			def paused = apps.size - running
			if (running + paused == 0) {
				paragraph "You have not created any pistons yet.", required: false
			} else {
				paragraph "You have ${running ? running + ' running ' + (paused ? ' and ' : '') : ''}${paused ? paused + ' paused ' : ''}piston${running + paused > 0 ? 's' : ''}.", required: false
			}
		}
        */
		section() {
			app( name: "pistons", title: "Add a CoRE piston...", appName: "CoRE", namespace: "ady624", multiple: true, uninstall: false, image: "https://cdn.rawgit.com/ady624/CoRE/master/resources/images/icons/piston.png")
		}

		section(title:"Application Info") {
			href "pageGlobalVariables", title: "Global Variables", image: "https://cdn.rawgit.com/ady624/CoRE/master/resources/images/icons/variables.png", required: false
			href "pageStatistics", title: "Runtime Statistics", image: "https://cdn.rawgit.com/ady624/CoRE/master/resources/images/icons/statistics.png", required: false
		}

		section(title:"") {
			href "pageGeneralSettings", title: "Settings", image: "https://cdn.rawgit.com/ady624/CoRE/master/resources/images/icons/settings.png", required: false
		}

	}
}

private pageInitializeDashboard() {
	//CoRE Dashboard initialization
	def success = initializeCoREEndpoint()
	dynamicPage(name: "pageInitializeDashboard", title: "") {
		section() {
			if (success) {
				paragraph "Success! Your CoRE dashboard is now enabled. Tap Done to continue", required: false
			} else {
				paragraph "Please go to your SmartThings IDE, select the My SmartApps section, click the 'Edit Properties' button of the CoRE app, open the OAuth section and click the 'Enable OAuth in Smart App' button. Click the Update button to finish.\n\nOnce finished, tap Done and try again.", title: "Please enable OAuth for CoRE", required: true, state: null
			}
		}
	}
}

def pageGeneralSettings(params) {
	dynamicPage(name: "pageGeneralSettings", title: "General Settings", install: false, uninstall: false) {
		section("About") {
			paragraph app.version(), title: "CoRE Version", required: false
			label name: "name", title: "Name", state: (name ? "complete" : null), defaultValue: app.name, required: false
		}

		section(title: "Dashboard") {
			href "pageDashboardTaps", title: "Taps", description: "Edit the list of taps on the dashboard", required: false, image: "https://cdn.rawgit.com/ady624/CoRE/master/resources/images/tap.png"
			input "dashboardTheme", "enum", options: ["Classic", "Experimental"], title: "Dashboard theme", defaultValue: "Experimental", required: false
		}

		section(title: "Expert Features") {
			input "expertMode", "bool", title: "Expert Mode", defaultValue: false, submitOnChange: true, required: false
		}

		section(title: "Debugging") {
			input "debugging", "bool", title: "Enable debugging", defaultValue: false, submitOnChange: true, required: false
			def debugging = settings.debugging
			if (debugging) {
				input "log#info", "bool", title: "Log info messages", defaultValue: true, required: false
				input "log#trace", "bool", title: "Log trace messages", defaultValue: true, required: false
				input "log#debug", "bool", title: "Log debug messages", defaultValue: false, required: false
				input "log#warn", "bool", title: "Log warning messages", defaultValue: true, required: false
				input "log#error", "bool", title: "Log error messages", defaultValue: true, required: false
			}
		}

		section("CoRE Integrations") {
			def iftttConnected = state.modules && state.modules["IFTTT"] && settings["iftttEnabled"] && state.modules["IFTTT"].connected
			href "pageIntegrateIFTTT", title: "IFTTT", description: iftttConnected ? "Connected" : "Not configured", state: (iftttConnected ? "complete" : null), submitOnChange: true, required: false
			def lifxConnected = state.modules && state.modules["LIFX"] && settings["lifxEnabled"] && state.modules["LIFX"].connected
			href "pageIntegrateLIFX", title: "LIFX", description: lifxConnected ? "Connected" : "Not configured", state: (lifxConnected ? "complete" : null), submitOnChange: true, required: false
		}

		section("Piston Recovery") {
			paragraph "Recovery allows pistons that have been left behind by missed ST events to recover and resume their work", required: false
			input "recovery#1", "enum", options: ["Disabled", "Every 1 hour", "Every 3 hours"], title: "Stage 1 recovery", defaultValue: "Every 3 hours", required: false
			input "recovery#2", "enum", options: ["Disabled", "Every 2 hours", "Every 4 hours", "Every 6 hours", "Every 12 hours", "Every 1 day", "Every 2 days", "Every 3 days"], title: "Stage 2 recovery", defaultValue: "Every 1 day", required: false
			input "recoveryNotifications", "bool", title: "Send recovery notifications via ST UI", required: false
			input "recoveryPushNotifications", "bool", title: "Send recovery notifications via PUSH", required: false
			href "pageRecoverAllPistons", title: "Recover all pistons", description: "Use this option when you have pistons displaying large 'past due' times in the dashboard.", required: false
			href "pageRebuildAllPistons", title: "Rebuild all pistons", description: "Use this option if there is a problem with your pistons, including when the dashboard is no longer working (blank).", required: false
		}

		section("Security") {
			href "pageResetSecurityToken", title: "", description: "Reset security token", required: false
		}

		section("Remove CoRE") {
			href "pageRemove", title: "", description: "Remove CoRE", required: false
		}

	}
}

def pageDashboardTaps() {
	rebuildTaps()
	dynamicPage(name: "pageDashboardTaps", title: "Dashboard Taps", install: false, uninstall: false) {
		def taps = state.taps
		section("") {
			href "pageDashboardTap", title: "Add a new tap", required: false, params: [id: 0], image: "https://cdn.rawgit.com/ady624/CoRE/master/resources/images/tap.png"
		}
		if (taps.size()) {
			section("Taps") {
				for (tap in taps) {
					href "pageDashboardTap", title: tap.n, description: "Runs ${buildNameList(tap.p, "and")}", required: false, params: [id: tap.i], image: "https://cdn.rawgit.com/ady624/CoRE/master/resources/images/tap.png"
				}
			}
		}
	}
}

def pageDashboardTap(params) {
	def tapId = (int) (params?.id != null ? params.id : state.tapId)
	if (!tapId) {
		//generate new tap id
		tapId = 1
		def existingTaps = settings.findAll{ it.key.startsWith("tapName") }
		for (tap in existingTaps) {
			def id = tap.key.replace("tapName", "")
			if (id.isInteger()) {
				id = id.toInteger()
				if (id >= tapId) tapId = (int) (id + 1)
			}
		}
	}
	state.tapId = tapId
	dynamicPage(name: "pageDashboardTap", title: "Dashboard Tap", install: false, uninstall: false) {
		section("") {
			input "tapName${tapId}", "string", title: "Name", description: "Enter a name for this tap", required: false, defaultValue: "Tap #${tapId}"
			input "tapPistons${tapId}", "enum", title: "Pistons", options: listPistons(), description: "Select the pistons to be executed when tapped", required: false, multiple: true
		}
		section("") {
			paragraph "NOTE: To delete this dashboard tap, clear its name and list of pistons and then tap Done"
		}
	}
}

def pageGlobalVariables() {
	dynamicPage(name: "pageGlobalVariables", title: "Global Variables", install: false, uninstall: false) {
		section("Initialize variables") {
			href "pageInitializeVariable", title: "Initialize variables", required: false
		}
		section() {
			def cnt = 0
			//initialize the store if it doesn't yet exist
			if (!state.store) state.store = [:]
			for (def variable in state.store.sort{ it.key }) {
				def value = getVariable(variable.key, true)
				href "pageViewVariable", description: "$value", title: "${variable.key}", params: [var: variable.key], required: false
				cnt++
			}
			if (!cnt) {
				paragraph "No global variables yet", required: false
			}
		}
	}
}

def pageStatistics() {
	dynamicPage(name: "pageStatistics", title: "", install: false, uninstall: false) {
		def apps = getChildApps().sort{ it.label }
		def running = apps.findAll{ it.getPistonEnabled() }.size()
		section(title: "CoRE") {
			paragraph mem(), title: "Memory Usage", required: false
			paragraph "${running}", title: "Running pistons", required: false
			paragraph "${apps.size - running}", title: "Paused pistons", required: false
			paragraph "${apps.size}", title: "Total pistons", required: false
		}

		updateChart("delay", null)
		section(title: "Event delay (15 minute average, last 2h)") {
			def text = ""
			def chart = state.charts["delay"]
			def totalAvg = 0
			for (def i = 0; i < 8; i++) {
				def value = Math.ceil((chart["$i"].c ? chart["$i"].t / chart["$i"].c : 0) / 100) / 10
				def time = chart["$i"].q
				def hour = time.mod(3600000) == 0 ? formatLocalTime(time, "h a") : "\t"
				def avg = Math.ceil(value / 1)
				totalAvg += avg
				if (avg > 10) {
					avg = 10
				}
				def graph = avg == 0 ? "□" : "".padLeft(avg, "■") + " ${value}s"
				text += "$hour\t${graph}\n"
			}
			totalAvg = totalAvg / 8
			href "pageChart", params: [chart: "delay", title: "Event delay"], title: "", description: text, required: true, state: totalAvg < 5 ? "complete" : null
		}

		updateChart("exec", null)
		section(title: "Execution time (15 minute average, last 2h)") {
			def text = ""
			def chart = state.charts["exec"]
			def totalAvg = 0
			for (def i = 0; i < 8; i++) {
				def value = Math.ceil((chart["$i"].c ? chart["$i"].t / chart["$i"].c : 0) / 100) / 10
				def time = chart["$i"].q
				def hour = time.mod(3600000) == 0 ? formatLocalTime(time, "h a") : "\t"
				def avg = Math.ceil(value / 1)
				totalAvg += avg
				if (avg > 10) avg = 10
				def graph = avg == 0 ? "□" : "".padLeft(avg, "■") + " ${value}s"
				text += "$hour\t${graph}\n"
			}
			totalAvg = totalAvg / 8
			href "pageChart", params: [chart: "exec", title: "Execution time"], title: "", description: text, required: true, state: totalAvg < 5 ? "complete" : null
		}

		def i = 0
		if (apps && apps.size()) {
			section("Pistons") {
				for (app in apps.sort{ it.label }) {
					href "pagePistonStatistics", params: [pistonId: app.id], title: app.label ?: app.name, required: false
				}
			}
		} else {
			section() {
				paragraph "No pistons running", required: false
			}
		}
	}
}

def pagePistonStatistics(params) {
	def pistonId = params?.pistonId ?: state.pistonId
	state.pistonId = pistonId
	dynamicPage(name: "pagePistonStatistics", title: "", install: false, uninstall: false) {
		def app = getChildApps().find{ it.id == pistonId }
		if (app) {
			def mode = app.getMode()
			def version = app.version()
			def currentState = app.getCurrentState()
			def stateSince = app.getCurrentStateSince()
			def runStats = app.getRunStats()
			def conditionStats = app.getConditionStats()
			def subscribedDevices = app.getDeviceSubscriptionCount()
			stateSince = stateSince ? formatLocalTime(stateSince) : null
			def description = "Piston mode: ${mode ? mode : "unknown"}"
			description += "\nPiston version: $version"
			description += "\nSubscribed devices: $subscribedDevices"
			description += "\nCondition count: ${conditionStats.conditions}"
			description += "\nTrigger count: ${conditionStats.triggers}"
			description += "\n\nCurrent state: ${currentState == null ? "unknown" : currentState}"
			description += "\nSince: " + (stateSince ?  stateSince : "(never run)")
			description += "\n\nMemory usage: " + app.mem()
			if (runStats) {
				def executionSince = runStats.executionSince ? formatLocalTime(runStats.executionSince) : null
				description += "\n\nEvaluated: ${runStats.executionCount} time${runStats.executionCount == 1 ? "" : "s"}"
				description += "\nSince: " + (executionSince ?  executionSince : "(unknown)")
				description += "\n\nTotal evaluation time: ${Math.round(runStats.executionTime / 1000)}s"
				description += "\nLast evaluation time: ${runStats.lastExecutionTime}ms"
				if (runStats.executionCount > 0) {
					description += "\nMin evaluation time: ${runStats.minExecutionTime}ms"
					description += "\nAvg evaluation time: ${Math.round(runStats.executionTime / runStats.executionCount)}ms"
					description += "\nMax evaluation time: ${runStats.maxExecutionTime}ms"
				}
				if (runStats.eventDelay) {
					description += "\n\nLast event delay: ${runStats.lastEventDelay}ms"
					if (runStats.executionCount > 0) {
						description += "\nMin event delay time: ${runStats.minEventDelay}ms"
						description += "\nAvg event delay time: ${Math.round(runStats.eventDelay / runStats.executionCount)}ms"
						description += "\nMax event delay time: ${runStats.maxEventDelay}ms"
					}
				}
			}
			section(app.label ?: app.name) {
				paragraph description, required: currentState != null, state: currentState ? "complete" : null
			}
		} else {
			section() {
				paragraph "Sorry, the piston you selected cannot be found", required: false
			}
		}
	}
}

def pageChart(params) {
	def chartName = params?.chart ?: state.chartName
	def chartTitle = params?.title ?: state.chartTitle
	state.chartName = chartName
	state.chartTitle = chartTitle
	dynamicPage(name: "pageChart", title: "", install: false, uninstall: false) {
		if (chartName) {
			updateChart(chartName, null)
			section(title: "$chartTitle (15 minute average, last 24h)\nData is calculated across all pistons") {
				def text = ""
				def chart = state.charts[chartName]
				def totalAvg = 0
				for (def i = 0; i < 96; i++) {
					def value = Math.ceil((chart["$i"].c ? chart["$i"].t / chart["$i"].c : 0) / 100) / 10
					def time = chart["$i"].q
					def hour = time.mod(3600000) == 0 ? formatLocalTime(time, "h a") : "\t"
					def avg = Math.ceil(value / 1)
					totalAvg += avg
					if (avg > 10) avg = 10
					def graph = avg == 0 ? "□" : "".padLeft(avg, "■") + " ${value}s"
					text += "$hour\t${graph}\n"
				}
				totalAvg = totalAvg / 96
				paragraph text, required: true, state: totalAvg < 5 ? "complete" : null
			}
		}
	}
}

def pageIntegrateIFTTT() {
	return dynamicPage(name: "pageIntegrateIFTTT", title: "IFTTT Integration", nextPage: settings.iftttEnabled ? "pageIntegrateIFTTTConfirm" : null) {
		section() {
			paragraph "CoRE can optionally integrate with IFTTT (IF This Then That) via the Maker channel, triggering immediate events to IFTTT. To enable IFTTT, please login to your IFTTT account and connect the Maker channel. You will be provided with a key that needs to be entered below", required: false
			input "iftttEnabled", "bool", title: "Enable IFTTT", submitOnChange: true, required: false
			if (settings.iftttEnabled) href name: "", title: "IFTTT Maker channel", required: false, style: "external", url: "https://www.ifttt.com/maker", description: "tap to go to IFTTT and connect the Maker channel"
		}
		if (settings.iftttEnabled) {
			section("IFTTT Maker key"){
				input("iftttKey", "string", title: "Key", description: "Your IFTTT Maker key", required: false)
			}
		}
	}
}

def pageIntegrateIFTTTConfirm() {
	if (testIFTTT()) {
		return dynamicPage(name: "pageIntegrateIFTTTConfirm", title: "IFTTT Integration") {
			section(){
				paragraph "Congratulations! You have successfully connected CoRE to IFTTT."
			}
		}
	} else {
		return dynamicPage(name: "pageIntegrateIFTTTConfirm",  title: "IFTTT Integration") {
			section(){
				paragraph "Sorry, the credentials you provided for IFTTT are invalid. Please go back and try again."
			}
		}
	}
}

def pageIntegrateLIFX() {
	return dynamicPage(name: "pageIntegrateLIFX", title: "LIFX Integration", nextPage: settings.lifxEnabled ? "pageIntegrateLIFXConfirm" : null) {
		section() {
			paragraph "CoRE can optionally integrate with LIFX, allowing you to run scenes directly into your LIFX environment. To enable LIFX, please login to your LIFX cloud account and go to Settings under your account. Tap on Generate New Token and copy the generated token into the field below", required: false
			input "lifxEnabled", "bool", title: "Enable LIFX", submitOnChange: true, required: false
			if (settings.lifxEnabled) href name: "", title: "LIFX Cloud Account", required: false, style: "external", url: "https://cloud.lifx.com", description: "tap to go to LIFX Cloud and generate an access token"
		}
		if (settings.lifxEnabled) {
			section("LIFX Access Token"){
				input("lifxToken", "string", title: "Token", description: "Your LIFX Access Token", required: false)
			}
		}
	}
}

def pageIntegrateLIFXConfirm() {
	if (testLIFX()) {
		return dynamicPage(name: "pageIntegrateLIFXConfirm", title: "LIFX Integration") {
			section(){
				paragraph "Congratulations! You have successfully connected CoRE to LIFX."
			}
		}
	} else {
		return dynamicPage(name: "pageIntegrateLIFXConfirm",  title: "LIFX Integration") {
			section(){
				paragraph "Sorry, the access token you provided for LIFX is invalid. Please go back and try again."
			}
		}
	}
}

def pageResetSecurityToken() {
	return dynamicPage(name: "pageResetSecurityToken", title: "CoRE Security Token") {
		section() {
			paragraph "CAUTION: Resetting the security token is an ireversible action. Once done, any integrations that rely on the security token, such as the CoRE Dashboard, the IFTTT Maker channel used as an action, etc. will STOP working and will require your attention. You will need to update the security token everywhere you are currently using it.", required: true
			href "pageResetSecurityTokenConfirm", title: "", description: "Reset security token", required: true
		}
	}
}

def pageResetSecurityTokenConfirm() {
	state.endpoint = null
	initializeCoREEndpoint()
	return dynamicPage(name: "pageResetSecurityTokenConfirm", title: "CoRE Security Token") {
		section() {
			paragraph "Your security token has been reset. Please make sure to update it wherever needed."
		}
	}
}

def pageRecoverAllPistons() {
	return dynamicPage(name: "pageRecoverAllPistons", title: "Recover all pistons") {
		section() {
			recoverPistons(true)
			paragraph "Done. All your pistons have been sent a recovery request."
		}
	}
}

def pageRebuildAllPistons() {
	return dynamicPage(name: "pageRebuildAllPistons", title: "Rebuild all pistons") {
		section() {
			rebuildPistons()
			paragraph "Done. All your pistons have been sent a rebuild request."
		}
	}
}

/******************************************************************************/
/*** CoRE PISTON PAGES														***/
/******************************************************************************/

private pageMainCoREPiston() {
	//CoRE Piston main page
	state.run = "config"
	configApp()
	cleanUpConditions(true)
	dynamicPage(name: "pageMain", title: "", install: true, uninstall: false) {
		def currentState = state.currentState
		section() {
			def enabled = !!state.config.app.enabled
			def pistonModes = ["Do", "Basic", "Simple", "Latching", "And-If", "Or-If"]
			if (!getConditionTriggerCount(state.config.app.otherConditions)) pistonModes += ["Then-If", "Else-If"]
			if (listActions(0).size() || getConditionCount(state.config.app)) pistonModes.remove("Do")
			if (listActions(-2).size()) pistonModes.remove("Basic")
			if (listActions(-1).size()) {
				pistonModes.remove("Do")
				pistonModes.remove("Basic")
				pistonModes.remove("Simple")
			} else pistonModes.add("Follow-Up")
			href "pageToggleEnabled", description: enabled ? "Current state: ${currentState == null ? "unknown" : currentState}\nCPU: ${cpu()}\t\tMEM: ${mem(false)}" : "", title: "Status: ${enabled ? "RUNNING" : "PAUSED"}", submitOnChange: true, required: false, state: "complete"
			input "mode", "enum", title: "Piston Mode", required: true, state: null, options: pistonModes, defaultValue: "Basic", submitOnChange: true
		}

		if (state.config.app.mode != "Do") {
			section() {
				href "pageIf", title: "If...", description: (state.config.app.conditions.children.size() ? "Tap here to add more conditions" : "Tap here to add a condition")
				buildIfContent()
			}

			section() {
				def actions = listActions(0)
				def desc = actions.size() ? "Tap here to add more actions" : "Tap here to add an action"
				href "pageActionGroup", params:[conditionId: 0], title: "Then...", description: desc, state: null, submitOnChange: false
				if (actions.size()) {
					for (action in actions) {
						href "pageAction", params:[actionId: action.id], title: "", description: getActionDescription(action), required: true, state: "complete", submitOnChange: true
					}
				}
			}

			def title = ""
			switch (settings.mode) {
				case "Latching":
					title = "But if..."
					break
				case "And-If":
					title = "And if..."
					break
				case "Or-If":
					title = "Or if..."
					break
				case "Then-If":
					title = "Then if..."
					break
				case "Else-If":
					title = "Else if..."
					break
			}
			if (title) {
				section() {
					href "pageIfOther", title: title, description: (state.config.app.otherConditions.children.size() ? "Tap here to add more conditions" : "Tap here to add a condition")
					buildIfOtherContent()
				}
				section() {
					def actions = listActions(-1)
					def desc = actions.size() ? "Tap here to add more actions" : "Tap here to add an action"
					href "pageActionGroup", params:[conditionId: -1], title: "Then...", description: desc, state: null, submitOnChange: false
					if (actions.size()) {
						for (action in actions) {
							href "pageAction", params:[actionId: action.id], title: "", description: getActionDescription(action), required: true, state: "complete", submitOnChange: true
						}
					}
				}
			}
		}
		if (!(state.config.app.mode in ["Basic", "Latching"])) {
			section() {
				def actions = listActions(-2)
				def desc = actions.size() ? "Tap here to add more actions" : "Tap here to add an action"
				href "pageActionGroup", params:[conditionId: -2], title: state.config.app.mode == "Do" ? "Do" : "Else...", description: desc, state: null, submitOnChange: false
				if (actions.size()) {
					for (action in actions) {
						href "pageAction", params:[actionId: action.id], title: "", description: getActionDescription(action), required: true, state: "complete", submitOnChange: true
					}
				}
			}
		}

		def hasRestrictions = settings["restrictionMode"] || settings["restrictionAlarm"] || settings["restrictionVariable"] || settings["restrictionDOW"] || settings["restrictionTimeFrom"] || settings["restrictionSwitchOn"] || settings["restrictionSwitchOff"]
		section(title: "Piston Restrictions", hideable: true, hidden: !hasRestrictions) {
			input "restrictionMode", "mode", title: "Only execute in these modes", description: "Any location mode", required: false, multiple: true
			input "restrictionAlarm", "enum", options: getAlarmSystemStatusOptions(), title: "Only execute during these alarm states", description: "Any alarm state", required: false, multiple: true
			input "restrictionVariable", "enum", options: listVariables(true), title: "Only execute when variable matches", description: "Tap to choose a variable", required: false, multiple: false, submitOnChange: true
			def rVar = settings["restrictionVariable"]
			if (rVar) {
				def options = ["is equal to", "is not equal to", "is less than", "is less than or equal to", "is greater than", "is greater than or equal to"]
				input "restrictionComparison", "enum", options: options, title: "Comparison", description: "Tap to choose a comparison", required: true, multiple: false
				input "restrictionValue", "string", title: "Value", description: "Tap to choose a value to compare", required: false, multiple: false, capitalization: "none"
			}
			input "restrictionDOW", "enum", options: timeDayOfWeekOptions(), title: "Only execute on these days", description: "Any week day", required: false, multiple: true
			def timeFrom = settings["restrictionTimeFrom"]
			input "restrictionTimeFrom", "enum", title: (timeFrom ? "Only execute if time is between" : "Only execute during this time interval"), options: timeComparisonOptionValues(false, false), required: false, multiple: false, submitOnChange: true
			if (timeFrom) {
				if (timeFrom.contains("custom")) {
					input "restrictionTimeFromCustom", "time", title: "Custom time", required: true, multiple: false
				} else {
					input "restrictionTimeFromOffset", "number", title: "Offset (+/- minutes)", range: "*..*", required: true, multiple: false, defaultValue: 0
				}
				def timeTo = settings["restrictionTimeTo"]
				input "restrictionTimeTo", "enum", title: "And", options: timeComparisonOptionValues(false, false), required: true, multiple: false, submitOnChange: true
				if (timeTo && (timeTo.contains("custom"))) {
					input "restrictionTimeToCustom", "time", title: "Custom time", required: true, multiple: false
				} else {
					input "restrictionTimeToOffset", "number", title: "Offset (+/- minutes)", range: "*..*", required: true, multiple: false, defaultValue: 0
				}
			}
			input "restrictionSwitchOn", "capability.switch", title: "Only execute when these switches are all on", description: "Always", required: false, multiple: true
			input "restrictionSwitchOff", "capability.switch", title: "Only execute when these switches are all off", description: "Always", required: false, multiple: true
			input "restrictionPreventTaskExecution", "bool", title: "Prevent already scheduled tasks from executing during restrictions", required: true, defaultValue: false
		}

		section() {
			href "pageSimulate", title: "Simulate", description: "Allows you to test the actions manually", state: complete
		}

		section(title:"Application Info") {
			label name: "name", title: "Name", required: true, state: (name ? "complete" : null), defaultValue: parent.generatePistonName()
			input "description", "string", title: "Description", required: false, state: (description ? "complete" : null), capitalization: "sentences"
			paragraph version(), title: "Version"
			paragraph mem(), title: "Memory Usage"
			href "pageVariables", title: "Local Variables"
		}
		section(title: "Advanced Options", hideable: !settings.debugging, hidden: true) {
			input "debugging", "bool", title: "Enable debugging", defaultValue: false, submitOnChange: true
			def debugging = settings.debugging
			if (debugging) {
				input "log#info", "bool", title: "Log info messages", defaultValue: true
				input "log#trace", "bool", title: "Log trace messages", defaultValue: true
				input "log#debug", "bool", title: "Log debug messages", defaultValue: false
				input "log#warn", "bool", title: "Log warning messages", defaultValue: true
				input "log#error", "bool", title: "Log error messages", defaultValue: true
			}
			input "disableCO", "bool", title: "Disable command optimizations", defaultValue: false
			href "pageRebuild", title: "Rebuild this CoRE piston", description: "Only use this option if your piston has been corrupted."
		}

		section("Rebuild or remove piston") {
			href "pageRemove", title: "", description: "Remove this CoRE piston"
		}
	}
}

def pageIf(params) {
	state.run = "config"
	cleanUpConditions(false)
	def condition = state.config.app.conditions
	dynamicPage(name: "pageIf", title: "Main Condition Group", uninstall: false, install: false) {
		getConditionGroupPageContent(params, condition)
	}
}

def pageIfOther(params) {
	state.run = "config"
	cleanUpConditions(false)
	def condition = state.config.app.otherConditions
	dynamicPage(name: "pageIfOther", title: "Main Group", uninstall: false, install: false) {
		getConditionGroupPageContent(params, condition)
	}
}

def pageConditionGroupL1(params) {
	pageConditionGroup(params, 1)
}

def pageConditionGroupL2(params) {
	pageConditionGroup(params, 2)
}

def pageConditionGroupL3(params) {
	pageConditionGroup(params, 3)
}

def pageConditionGroupL4(params) {
	pageConditionGroup(params, 4)
}

def pageConditionGroupL5(params) {
	pageConditionGroup(params, 5)
}

//helper function for condition group paging
def pageConditionGroup(params, level) {
	state.run = "config"
	cleanUpConditions(false)
	def condition = null
	if (params?.command == "add") {
		condition = createCondition(params?.parentConditionId, true)
	} else {
		condition = getCondition(params?.conditionId ? (int) params?.conditionId : state.config["conditionGroupIdL$level"])
	}
	if (condition) {
		def id = (int) condition.id
		state.config["conditionGroupIdL$level"] = id
		def pid = (int) condition.parentId
		dynamicPage(name: "pageConditionGroupL$level", title: "Group $id (level $level)", uninstall: false, install: false) {
			getConditionGroupPageContent(params, condition)
		}
	}
}

private getConditionGroupPageContent(params, condition) {
	try {
		if (condition) {
			def id = (int) condition.id
			def pid = (int) condition.parentId ? (int) condition.parentId : (int)condition.id
			def nextLevel = (int) (condition.level ? condition.level : 0) + 1
			def cnt = 0
			section() {
				if (settings["condNegate$id"]) {
					paragraph "NOT ("
				}
				for (c in condition.children) {
					if (cnt > 0) {
						if (cnt == 1) {
							input "condGrouping$id", "enum", title: "", description: "Choose the logical operation to be applied between all conditions in this group", options: groupOptions(), defaultValue: "AND", required: true, submitOnChange: true
						} else {
							paragraph settings["condGrouping$id"], state: "complete"
						}
					}
					def cid = c?.id
					def conditionType = (c.trg ? "trigger" : "condition")
					if (c.children != null) {
						href "pageConditionGroupL${nextLevel}", params: ["conditionId": cid], title: "Group #$cid", description: getConditionDescription(cid), state: "complete", required: false, submitOnChange: false
					} else {
						href "pageCondition", params: ["conditionId": cid], title: (c.trg ? "Trigger" : "Condition") + " #$cid", description: getConditionDescription(cid), state: "complete", required: false, submitOnChange: false
					}
					//when true - individual actions
					def actions = listActions(c.id)
					def sz = actions.size() - 1
					def i = 0
					def tab = "  "
					for (action in actions) {
						href "pageAction", params: ["actionId": action.id], title: "", description: (i == 0 ? "${tab}╠═(when true)══ {\n" : "") + "${tab}║ " + getActionDescription(action).trim().replace("\n", "\n${tab}║") + (i == sz ? "\n${tab}╚════════ }" : ""), state: null, required: false, submitOnChange: false
						i = i + 1
					}

					cnt++
				}
				if (settings["condNegate$id"]) {
					paragraph ")", state: "complete"
				}
			}
			section() {
				href "pageCondition", params:["command": "add", "parentConditionId": id], title: "Add a condition", description: "A condition watches the state of one or multiple similar devices", state: "complete", submitOnChange: true
				if (nextLevel <= 5) {
					href "pageConditionGroupL${nextLevel}", params:["command": "add", "parentConditionId": id], title: "Add a group", description: "A group is a container for multiple conditions and/or triggers, allowing for more complex logical operations, such as evaluating [A AND (B OR C)]", state: "complete", submitOnChange: true
				}
			}

			if (condition.children.size()) {
				section(title: "Group Overview") {
					def value = evaluateCondition(condition)
					paragraph getConditionDescription(id), required: true, state: ( value ? "complete" : null )
					paragraph "Current evaluation: $value", required: true, state: ( value ? "complete" : null )
				}
			}

			if (id > 0) {
				def actions = listActions(id)
				if (actions.size() || state.config.expertMode) {
					section(title: "Individual actions") {
						actions = listActions(id, true)
						def desc = actions.size() ? "" : "Tap to select actions"
						href "pageActionGroup", params:[conditionId: id, onState: true], title: "When true, do...", description: desc, state: null, submitOnChange: false
						if (actions.size()) {
							for (action in actions) {
								href "pageAction", params:[actionId: action.id], title: "", description: getActionDescription(action), required: true, state: "complete", submitOnChange: true
							}
						}
						actions = listActions(id, false)
						desc = actions.size() ? "" : "Tap to select actions"
						href "pageActionGroup", params:[conditionId: id, onState: false], title: "When false, do...", description: desc, state: null, submitOnChange: false
						if (actions.size()) {
							for (action in actions) {
								href "pageAction", params:[actionId: action.id], title: "", description: getActionDescription(action), required: true, state: "complete", submitOnChange: true
							}
						}
					}
				}
			}

			section(title: "Advanced options") {
				input "condNegate$id", "bool", title: "Negate Group", description: "Apply a logical NOT to the whole group", defaultValue: false, state: null, submitOnChange: true
			}
			if (state.config.expertMode) {
				section("Set variables") {
					input "condVarD$id", "string", title: "Save last evaluation date", description: "Enter a variable name to store the date in", required: false, capitalization: "none"
					input "condVarS$id", "string", title: "Save last evaluation result", description: "Enter a variable name to store the truth result in", required: false, capitalization: "none"
				}
				section("Set variables on true") {
					input "condVarT$id", "string", title: "Save event date on true", description: "Enter a variable name to store the date in", required: false, capitalization: "none"
					input "condVarV$id", "string", title: "Save event value on true", description: "Enter a variable name to store the value in", required: false, capitalization: "none"
				}
				section("Set variables on false") {
					input "condVarF$id", "string", title: "Save event date on false", description: "Enter a variable name to store the date in", required: false, capitalization: "none"
					input "condVarW$id", "string", title: "Save event value on false", description: "Enter a variable name to store the value in", required: false, capitalization: "none"
				}
			}

			if (id > 0) {
				section(title: "Required data - do not change", hideable: true, hidden: true) {
					input "condParent$id", "number", title: "Parent ID", description: "Value needs to be $pid, do not change", range: "-2..${pid+1}", defaultValue: pid
				}
			}
		}
	} catch(e) {
		debug "ERROR: Error while executing getConditionGroupPageContent: ", null, "error", e
	}
}

def pageCondition(params) {
	try {
		state.run = "config"
		//get the current edited condition
		def condition = null
		if (params?.command == "add") {
			condition = createCondition(params?.parentConditionId, false)
		} else {
			condition = getCondition(params?.conditionId ? params?.conditionId : state.config.conditionId)
		}
		if (condition) {
			updateCondition(condition)
			cleanUpActions()
			def id = (int) condition.id
			state.config.conditionId = id
			def pid = (int) condition.parentId
			def overrideAttributeType = null
			def showDateTimeFilter = false
			def showDateTimeRepeat = false
			def showParameters = false
			def recurring = false
			def trigger = false
			def validCondition = false
			def capability
			def branchId = getConditionMasterId(condition.id)
			def supportsTriggers = (settings.mode != "Follow-Up") && ((branchId == 0) || (settings.mode in ["Latching", "And-If", "Or-If"]))
			dynamicPage(name: "pageCondition", title: (condition.trg ? "Trigger" : "Condition") + " #$id", uninstall: false, install: false) {
				section() {
					if (!settings["condDevices$id"] || (settings["condDevices$id"].size() == 0)) {
						//only display capability selection if no devices already selected
						input "condCap$id", "enum", title: "Capability", options: listCapabilities(true, false), submitOnChange: true, required: false
					}
					if (settings["condCap$id"]) {
						//define variables
						def devices
						def attribute
						def attr
						def comparison
						def allowDeviceComparisons = true

						capability = getCapabilityByDisplay(settings["condCap$id"])
						if (capability) {
							if (capability.virtualDevice) {
								attribute = capability.attribute
								attr = getAttributeByName(attribute)
								if (attribute == "time") {
									//Date & Time support
									comparison = cleanUpComparison(settings["condComp$id"])
									input "condComp$id", "enum", title: "Comparison", options: listComparisonOptions(attribute, supportsTriggers), required: true, multiple: false, submitOnChange: true
									if (comparison) {
										def comp = getComparisonOption(attribute, comparison)
										if (attr && comp) {
											validCondition = true
											//we have a valid comparison object
											trigger = (comp.trigger == comparison)
											//if no parameters, show the filters
											def varList = listVariables(true)
											showDateTimeFilter = comp.parameters == 0
											for (def i = 1; i <= comp.parameters; i++) {
												input "condValue$id#$i", "enum", title: (comp.parameters == 1 ? "Value" : (i == 1 ? "Time" : "And")), options: timeComparisonOptionValues(trigger), required: true, multiple: false, submitOnChange: true
												def value = settings["condValue$id#$i"] ? "${settings["condValue$id#$i"]}" : ""
												if (value) {
													showDateTimeFilter = true
													if (value.contains("custom")) {
														//using a time offset
														input "condTime$id#$i", "time", title: "Custom time", required: true, multiple: false, submitOnChange: true
													}
													if (value.contains("variable")) {
														//using a time offset
														def var = settings["condVar$id#$i"]
														input "condVar$id#$i", "enum", options: varList, title: "Variable${ var ? " [${getVariable(var, true)}]" : ""}", required: true, multiple: false, submitOnChange: true
													}
													if (comparison && value && ((comparison.contains("around") || !(value.contains('every') || value.contains('custom'))))) {
														//using a time offset
														input "condOffset$id#$i", "number", title: (comparison.contains("around") ? "Give or take minutes" : "Offset (+/- minutes)"), range: (comparison.contains("around") ? "1..1440" : "-1440..1440"), required: true, multiple: false, defaultValue: (comparison.contains("around") ?  5 : 0), submitOnChange: true
													}

													if (value.contains("minute") || value.contains("date and time")) recurring = true

													if (value.contains("number")) {
														//using a time offset
														input "condEvery$id", "number", title: value.replace("every n", "N"), range: "1..*", required: true, multiple: false, defaultValue: 5, submitOnChange: true
														recurring = true
													}

													if (value.contains("hour")) {
														//using a time offset
														input "condMinute$id", "enum", title: "At this minute", options: timeMinuteOfHourOptions(), required: true, multiple: false, submitOnChange: true
														recurring = true
													}

												}
											}
											if (trigger && !recurring) showDateTimeRepeat = true
										}
									}
								} else {
									//Location Mode, Smart Home Monitor support
									validCondition = false
									if (attribute == "variable") {
										def dataType = settings["condDataType$id"]
										overrideAttributeType = dataType ? dataType : "string"
										input "condDataType$id", "enum", title: "Data Type", options: ["boolean", "string", "number", "decimal"], required: true, multiple: false, submitOnChange: true
										input "condVar$id", "enum", title: "Variable name", options: listVariables(true, overrideAttributeType) , required: true, multiple: false, submitOnChange: true
										def variable = settings["condVar$id"]
										if (!"$variable".startsWith("@")) supportsTriggers = false
									} else {
										//do not allow device comparisons for location related capabilities, except variables
										allowDeviceComparisons = false
									}
									if ((capability.name == "askAlexaMacro") && (!listAskAlexaMacros().size())) {
										paragraph "It looks like you don't have the Ask Alexa SmartApp installed, or you haven't created any macros yet. To use this capability, please install Ask Alexa or, if already installed, create some macros first, then try again.", title: "Oh-oh!"
										href "", title: "Ask Alexa", description: "Tap here for more information on Ask Alexa", style: "external", url: "https://community.smartthings.com/t/release-ask-alexa/46786"
										showParameters = false
									} else if ((capability.name == "echoSistantProfile") && (!listEchoSistantProfiles().size())) {
										paragraph "It looks like you don't have the EchoSistant SmartApp installed, or you haven't created any profiles yet. To use this capability, please install EchoSistant or, if already installed, create some profiles first, then try again.", title: "Oh-oh!"
										href "", title: "EchoSistant", description: "Tap here for more information on EchoSistant", style: "external", url: "https://community.smartthings.com/t/release-echosistant-version-1-2-0/62109"
										showParameters = false
									} else {
										def options = listComparisonOptions(attribute, supportsTriggers, overrideAttributeType)
										def defaultValue = (options.size() == 1 ? options[0] : null)
										input "condComp$id", "enum", title: "Comparison", options: options, defaultValue: defaultValue, required: true, multiple: false, submitOnChange: true
										comparison = cleanUpComparison(settings["condComp$id"] ?: defaultValue)
										if (comparison) {
											showParameters = true
											validCondition = true
										}
									}
								}
							} else {
								//physical device support
								validCondition = false
								devices = settings["condDevices$id"]
								input "condDevices$id", "capability.${capability.name}", title: "${capability.display} list", required: false, state: (devices ? "complete" : null), multiple: capability.multiple, submitOnChange: true
								if (devices && devices.size()) {
									if (!condition.trg && (devices.size() > 1)) {
										input "condMode$id", "enum", title: "Evaluation mode", options: ["Any", "All"], required: true, multiple: false, defaultValue: "All", submitOnChange: true
									}
									def evalMode = (settings["condMode$id"] == "All" && !condition.trg) ? "All" : "Any"

									//Attribute
									attribute = cleanUpAttribute(settings["condAttr$id"])
									if (attribute == null) attribute = capability.attribute
									//display the Attribute only in expert mode or in basic mode if it differs from the default capability attribute
									if ((attribute != capability.attribute) || capability.showAttribute || state.config.expertMode) {
										input "condAttr$id", "enum", title: "Attribute", options: listCommonDeviceAttributes(devices), required: true, multiple: false, defaultValue: capability.attribute, submitOnChange: true
									}

									if (capability.count && (attribute != "lock")) {
										def subDevices = capability.count && (attribute == capability.attribute) ? listCommonDeviceSubDevices(devices, capability.count, "") : []
										if (subDevices.size()) {
											input "condSubDev$id", "enum", title: "${capability.subDisplay ?: capability.display}(s)", options: subDevices, defaultValue: subDevices.size() ? subDevices[0] : null, required: true, multiple: true, submitOnChange: true
										}
									}
									if (attribute) {
										//Condition
										attr = getAttributeByName(attribute, devices && devices.size() ? devices[0] : null)
										comparison = cleanUpComparison(settings["condComp$id"])
										input "condComp$id", "enum", title: "Comparison", options: listComparisonOptions(attribute, supportsTriggers, attr.momentary ? "momentary" : null, devices && devices.size() ? devices[0] : null), required: true, multiple: false, submitOnChange: true
										if (comparison) {
											//Value
											showParameters = true
											validCondition = true
										}
									}
								}
							}
						}

						if (showParameters) {
							//build the parameters inputs for all physical capabilities and variables
							def comp = getComparisonOption(attribute, comparison, overrideAttributeType, devices && devices.size() ? devices[0] : null)
							if (attr && comp) {
								trigger = (comp.trigger == comparison)
								def extraComparisons = !comparison.contains("one of")
								def varList = (extraComparisons ? listVariables(true, overrideAttributeType) : [])
								def type = overrideAttributeType ? overrideAttributeType : (attr.valueType ? attr.valueType : attr.type)

								for (def i = 1; i <= comp.parameters; i++) {
									//input "condValue$id#1", type, title: "Value", options: attr.options, range: attr.range, required: true, multiple: comp.multiple, submitOnChange: true
									def value = settings["condValue$id#$i"]
									def device = settings["condDev$id#$i"]
									def variable = settings["condVar$id#$i"]
									if (variable) {
										value = null
										device = null
									}
									if (device) value = null
									if (!extraComparisons || ((device == null) && (variable == null))) {
										input "condValue$id#$i", type == "boolean" ? "enum" : type, title: (comp.parameters == 1 ? "Value" : "${i == 1 ? "From" : "To"} value"), options: type == "boolean" ? ["true", "false"] : attr.options, range: attr.range, required: true, multiple: type == "boolean" ? false : comp.multiple, submitOnChange: true
									}
									if (extraComparisons) {
										if ((value == null) && (device == null)) {
											input "condVar$id#$i", "enum", options: varList, title: (variable == null ? "... or choose a variable to compare ..." : (comp.parameters == 1 ? "Variable value${ variable ? " [${getVariable(variable, true)}]" : ""}" : "${i == 1 ? "From" : "To"} variable value${ variable ? " [${getVariable(variable, true)}]" : ""}")), required: true, multiple: comp.multiple, submitOnChange: true, capitalization: "none"
										}
										if ((value == null) && (variable == null) && (allowDeviceComparisons)) {
											input "condDev$id#$i", "capability.${capability && capability.name ? capability.name : (type == "boolean" ? "switch" : "sensor")}", title: (device == null ? "... or choose a device to compare ..." : (comp.parameters == 1 ? "Device value" : "${i == 1 ? "From" : "To"} device value")), required: true, multiple: false, submitOnChange: true
											if (device) {
												input "condAttr$id#$i", "enum", title: "Attribute", options: listCommonDeviceAttributes([device]), required: true, multiple: false, submitOnChange: true, defaultValue: attribute
											}
										}
										if (((variable != null) || (device != null)) && ((type == "number") || (type == "decimal"))) {
											input "condOffset$id#$i", type, range: "*..*", title: "Offset (+/-" + (attr.unit ? " ${attr.unit})" : ")"), required: true, multiple: false, defaultValue: 0, submitOnChange: true
										}
									}
								}

								if (comp.timed) {
									if (comparison.contains("change")) {
										input "condTime$id", "enum", title: "In the last", options: timeOptions(true), required: true, multiple: false, submitOnChange: true
									} else if (comparison.contains("stays")) {
										input "condTime$id", "enum", title: "For", options: timeOptions(true), required: true, multiple: false, submitOnChange: true
									} else {
										input "condFor$id", "enum", title: "Time restriction", options: ["for at least", "for less than"], required: true, multiple: false, submitOnChange: true
										input "condTime$id", "enum", title: "Interval", options: timeOptions(), required: true, multiple: false, submitOnChange: true
									}
								}

								if (trigger && attr.interactive) {
									//Interaction
									def interaction = settings["condInteraction$id"]
									def defaultInteraction = "Any"
									if (interaction == null) {
										interaction = defaultInteraction
									}
									//display the Interaction only in expert mode or in basic mode if it differs from the default capability attribute
									if ((interaction != defaultInteraction) || state.config.expertMode) {
										input "condInteraction$id", "enum", title: "Interaction", options: ["Any", "Physical", "Programmatic"], required: true, multiple: false, defaultValue: defaultInteraction, submitOnChange: true
									}
								}
								if (capability.count && (attribute == "lock") && (settings["condValue$id#1"] == "unlocked")) {
									def subDevices = capability.count && (attribute == capability.attribute) ? ["(none)"] + listCommonDeviceSubDevices(devices, capability.count, "") : []
									if (subDevices.size()) {
										input "condSubDev$id", "enum", title: "${capability.subDisplay ?: capability.display}(s)", options: subDevices, required: false, multiple: true, submitOnChange: false
									}
								}
							}
						}
					}
				}

				if (capability && (capability.name == "variable")) {
					section("Variables") {
						href "pageVariables", title: "View current variables"
						href "pageInitializeVariable", title: "Initialize a variable"
					}
				}

				if (showDateTimeRepeat) {
					section(title: "Repeat this trigger...") {
						input "condRepeat$id", "enum", title: "Repeat", options: timeRepeatOptions(), required: true, multiple: false, defaultValue: "every day", submitOnChange: true
						def repeat = settings["condRepeat$id"]
						if (repeat) {
							def incremental = repeat.contains("number")
							if (incremental) {
								//using a time offset
								input "condRepeatEvery$id", "number", title: repeat.replace("every n", "N"), range: "1..*", required: true, multiple: false, defaultValue: 2, submitOnChange: true
								recurring = true
							}
							def monthOfYear = null
							if (repeat.contains("week")) {
								input "condRepeatDayOfWeek$id", "enum", title: "Day of the week", options: timeDayOfWeekOptions(), required: true, multiple: false, submitOnChange: true
							}
							if (repeat.contains("month") || repeat.contains("year")) {
								//oh-oh, monthly
								input "condRepeatDay$id", "enum", title: "On", options: timeDayOfMonthOptions(), required: true, multiple: false, submitOnChange: true
								def dayOfMonth = settings["condRepeatDay$id"]
								def certainDay = false
								def dayOfWeek = null
								if (dayOfMonth) {
									if (dayOfMonth.contains("week")) {
										certainDay = true
										input "condRepeatDayOfWeek$id", "enum", title: "Day of the week", options: timeDayOfWeekOptions(), required: true, multiple: false, submitOnChange: true
										dayOfWeek = settings["condDOWOM$id"]
									}
								}
								if (repeat.contains("year")) {// && (dayOfMonth) && (!certainDay || dayOfWeek)) {
									//oh-oh, yearly
									input "condRepeatMonth$id", "enum", title: "Of", options: timeMonthOfYearOptions(), required: true, multiple: false, submitOnChange: true
									monthOfYear = settings["condRepeatMonth$id"]
								}
							}
						}
					}
				}

				if (validCondition) {
					section(title: (condition.trg ? "Trigger" : "Condition") + " Overview") {
						def value = evaluateCondition(condition)
						paragraph getConditionDescription(id), required: true, state: ( value ? "complete" : null )
						paragraph "Current evaluation: $value", required: true, state: ( value ? "complete" : null )
						if (condition.attr == "time") {
							def v = ""
							def nextTime = null
							def lastTime = null
							for (def i = 0; i < (condition.trg ? 3 : 1); i++) {
								nextTime = condition.trg ? getNextTimeTriggerTime(condition, nextTime) : getNextTimeConditionTime(condition, nextTime)
								if (nextTime) {
									if (lastTime && nextTime && (nextTime - lastTime < 5000)) {
										break
									}
									lastTime = nextTime
									v = v + ( v ? "\n" : "") + formatLocalTime(nextTime)
								} else {
									break
								}
							}

							paragraph v ? v : "(not happening any time soon)", title: "Next scheduled event${i ? "s" : ""}", required: true, state: ( v ? "complete" : null )
						}
					}

					if (showDateTimeFilter) {
						section(title: "Date & Time Filters", hideable: !state.config.expertMode, hidden: !(state.config.expertMode || settings["condMOH$id"] || settings["condHOD$id"] || settings["condDOW$id"] || settings["condDOM$id"] || settings["condMOY$id"] || settings["condY$id"])) {
							paragraph "But only on these..."
							input "condMOH$id", "enum", title: "Minute of the hour", description: 'Any minute of the hour', options: timeMinuteOfHourOptions(), required: false, multiple: true, submitOnChange: true
							input "condHOD$id", "enum", title: "Hour of the day", description: 'Any hour of the day', options: timeHourOfDayOptions(), required: false, multiple: true, submitOnChange: true
							input "condDOW$id", "enum", title: "Day of the week", description: 'Any day of the week', options: timeDayOfWeekOptions(), required: false, multiple: true, submitOnChange: true
							input "condDOM$id", "enum", title: "Day of the month", description: 'Any day of the month', options: timeDayOfMonthOptions2(), required: false, multiple: true, submitOnChange: true
							input "condWOM$id", "enum", title: "Week of the month", description: 'Any week of the month', options: timeWeekOfMonthOptions(), required: false, multiple: true, submitOnChange: true
							input "condMOY$id", "enum", title: "Month of the year", description: 'Any month of the year', options: timeMonthOfYearOptions(), required: false, multiple: true, submitOnChange: true
							input "condY$id", "enum", title: "Year", description: 'Any year', options: timeYearOptions(), required: false, multiple: true, submitOnChange: true
						}
					}

					if (id > 0) {
						def actions = listActions(id)
						if (actions.size() || state.config.expertMode) {
							section(title: "Individual actions") {
								actions = listActions(id, true)
								def desc = actions.size() ? "" : "Tap to select actions"
								href "pageActionGroup", params:[conditionId: id, onState: true], title: "When true, do...", description: desc, state: null, submitOnChange: false
								if (actions.size()) {
									for (action in actions) {
										href "pageAction", params:[actionId: action.id], title: "", description: getActionDescription(action), required: true, state: "complete", submitOnChange: true
									}
								}
								actions = listActions(id, false)
								desc = actions.size() ? "" : "Tap to select actions"
								href "pageActionGroup", params:[conditionId: id, onState: false], title: "When false, do...", description: desc, state: null, submitOnChange: false
								if (actions.size()) {
									for (action in actions) {
										href "pageAction", params:[actionId: action.id], title: "", description: getActionDescription(action), required: true, state: "complete", submitOnChange: true
									}
								}
							}
						}
					}
					section(title: "Advanced options") {
						input "condNegate$id", "bool", title: "Negate ${condition.trg ? "trigger" : "condition"}", description: "Apply a logical NOT to the ${condition.trg ? "trigger" : "condition"}", defaultValue: false, state: null, submitOnChange: true
					}
					if (state.config.expertMode) {
						section("Set variables") {
							input "condVarD$id", "string", title: "Save last evaluation date", description: "Enter a variable name to store the date in", required: false, capitalization: "none"
							input "condVarS$id", "string", title: "Save last evaluation result", description: "Enter a variable name to store the truth result in", required: false, capitalization: "none"
							input "condVarM$id", "string", title: "Save matching device list", description: "Enter a variable name to store the list of devices that match the condition", required: false, capitalization: "none"
							input "condVarN$id", "string", title: "Save non-matching device list", description: "Enter a variable name to store the list of devices that do not match the condition", required: false, capitalization: "none"
						}
						section("Set variables on true") {
							input "condVarT$id", "string", title: "Save event date on true", description: "Enter a variable name to store the date in", required: false, capitalization: "none"
							input "condVarV$id", "string", title: "Save event value on true", description: "Enter a variable name to store the value in", required: false, capitalization: "none"
							input "condImportT$id", "bool", title: "Import event data on true", required: false, submitOnChange: true
							if (settings["condImportT$id"]) input "condImportTP$id", "string", title: "Variables prefix for import", description: "Choose a prefix that you want to use for event data parameters", required: false
						}
						section("Set variables on false") {
							input "condVarF$id", "string", title: "Save event date on false", description: "Enter a variable name to store the date in", required: false, capitalization: "none"
							input "condVarW$id", "string", title: "Save event value on false", description: "Enter a variable name to store the value in", required: false, capitalization: "none"
							input "condImportF$id", "bool", title: "Import event data on false", required: false, submitOnChange: true
							if (settings["condImportF$id"]) input "condImportFP$id", "string", title: "Variables prefix for import", description: "Choose a prefix that you want to use for event data parameters", required: false
						}
					}
				}
				section() {
					paragraph (capability && capability.virtualDevice ? "NOTE: To delete this condition, unselect the ${capability.display} option from the Capability input above and tap Done" : "NOTE: To delete this condition, simply remove all the devices from the Device list above and tap Done")
				}

				section(title: "Required data - do not change", hideable: true, hidden: true) {
					input "condParent$id", "number", title: "Parent ID", description: "Value needs to be $pid, do not change condParent$id", range: "-2..${pid+1}", defaultValue: pid
				}
			}
		}
	} catch(e) {
		debug "ERROR: Error while executing pageCondition: ", null, "error", e
	}
}

def pageVariables() {
	state.run = "config"
	dynamicPage(name: "pageVariables", title: "", install: false, uninstall: false) {
		section("Initialize variables") {
			href "pageInitializeVariable", title: "Initialize a variable"
		}
		section("Local Variables") {
			def cnt = 0
			for (def variable in state.store.sort{ it.key }) {
				def value = getVariable(variable.key, true)
				href "pageViewVariable", description: "$value", title: "${variable.key}", params: [var: variable.key]
				cnt++
			}
			if (!cnt) {
				paragraph "No local variables yet"
			}
		}
		section("System Variables") {
			for (def variable in state.systemStore.sort{ it.key }) {
				def value = getVariable(variable.key, true)
				href "pageViewVariable", description: "$value", title: "${variable.key}", params: [var: variable.key]
			}
		}
	}
}

def pageActionGroup(params) {
	state.run = "config"
	def conditionId = params?.conditionId != null ? (int) params?.conditionId : (int) state.config.actionConditionId
	def onState = conditionId > 0 ? (params?.onState != null ? (boolean) params?.onState : (boolean) state.config.onState) : true
	state.config.actionConditionId = conditionId
	state.config.onState = (boolean) onState
	def value = conditionId < -1 ? false : true
	def block = conditionId > 0 ? "WHEN ${onState ? "TRUE" : "FALSE"}, DO ..." : "IF"
	if (conditionId < 0) {
		switch (settings.mode) {
			case "Do":
			case "Basic":
			case "Simple":
			case "Follow-Up":
				block = ""
				value = false
				break
			case "And-If":
				block = "AND IF"
				break
			case "Or-If":
				block = "OR IF"
				break
			case "Then-If":
				block = "THEN IF"
				break
			case "Else-If":
				block = "ELSE IF"
				break
			case "Latching":
				block = "BUT IF"
				break
		}
	}

	switch (conditionId) {
		case 0:
			block = "IF (condition) THEN ..."
			break
		case -1:
			block = "IF (condition) $block (condition) THEN ..."
			break
		case -2:
			block = "IF (condition) ${block ? "$block (condition) " : ""}ELSE ..."
			break
	}

	cleanUpActions()
	dynamicPage(name: "pageActionGroup", title: "$block", uninstall: false, install: false) {
		def actions = listActions(conditionId, onState)
		if (actions.size()) {
			section() {
				for(def action in actions) {
					href "pageAction", params:[actionId: action.id], title: "Action #${action.id}", description: getActionDescription(action), required: true, state: "complete", submitOnChange: true
				}
			}
		}

		section() {
			href "pageAction", params:[command: "add", conditionId: conditionId, onState: onState], title: "Add an action", required: !actions.size(), state: (actions.size() ? null : "complete"), submitOnChange: true
		}

	}
}

def pageAction(params) {
	state.run = "config"
	//this page has a dual purpose, either action wizard or task manager
	//if no devices have been previously selected, the page acts as a wizard, guiding the use through the selection of devices
	//if at least one device has been previously selected, the page will guide the user through setting up tasks for selected devices
	def action = null
	if (params?.command == "add") {
		action = createAction(params?.conditionId, params?.onState)
	} else {
		action = getAction(params?.actionId ? params?.actionId : state.config.actionId)
	}
	if (action) {
		updateAction(action)
		def id = action.id
		state.config.actionId = id
		def pid = action.pid

		dynamicPage(name: "pageAction", title: "Action #$id", uninstall: false, install: false) {
			def devices = []
			def usedCapabilities = []
			//did we get any devices? search all capabilities
			for(def capability in capabilities()) {
				if (capability.devices) {
					//only if the capability published any devices - it wouldn't be here otherwise
					def dev = settings["actDev$id#${capability.name}"]
					if (dev && dev.size()) {
						devices = devices + dev
						//add to used capabilities - needed later
						if (!(capability.name in usedCapabilities)) {
							usedCapabilities.push(capability.name)
						}
					}
				}
			}
			def locationAction = !!settings["actDev$id#location"]
			def deviceAction = !!devices.size()
			def actionUsed = deviceAction || locationAction
			if (!actionUsed) {
				//category selection page
				for(def category in listCommandCategories()) {
					section(title: category) {
						def options = []
						for(def command in listCategoryCommands(category)) {
							def option = getCommandGroupName(command)
							if (option && !(option in options)) {
								options.push option
								if (option.contains("location mode")) {
									def controlLocation = settings["actDev$id#location"]
									input "actDev$id#location", "bool", title: option, defaultValue: false, submitOnChange: true
								} else {
									href "pageActionDevices", params:[actionId: id, command: command], title: option, submitOnChange: true
								}
							}
						}
					}
				}
				section(title: "All devices") {
					href "pageActionDevices", params:[actionId: id, command: ""], title: "Control any device", submitOnChange: true
				}
			} else {
				//actual action page
				if (true || deviceAction) {
					section() {
						def names=[]
						if (deviceAction) {
							for(device in devices) {
								def label = getDeviceLabel(device)
								if (!(label in names)) {
									names.push(label)
								}
							}
							href "pageActionDevices", title: "Using...", params:[actionId: id, capabilities: usedCapabilities], description: "${buildNameList(names, "and")}", state: "complete", submitOnChange: true
						} else {
							names.push "location"
							input "actDev$id#location", "bool", title: "Using location...", state: "complete", defaultValue: true, submitOnChange: true
						}
					}
					def prefix = "actTask$id#"
					def tasks = settings.findAll{it.key.startsWith(prefix)}
					def maxId = 1
					def ids = []
					//we need to get a list of all existing ids that are used
					for (task in tasks) {
						if (task.value) {
							def tid = task.key.replace(prefix, "")
							if (tid.isInteger()) {
								tid = tid.toInteger()
								maxId = tid >= maxId ? tid + 1 : maxId
								ids.push(tid)
							}
						}
					}
					//sort the ids, we really want to have these in the proper order
					ids = ids.sort()
					def availableCommands = (deviceAction ? listCommonDeviceCommands(devices, usedCapabilities) : [])
					def flowCommands = []
					def cmds = virtualCommands()
					for (vcmd in cmds.sort{ it.display }) {
						if ((!(vcmd.display in availableCommands)) && (vcmd.location || deviceAction)) {
							def ok = true
							if (vcmd.requires && vcmd.requires.size()) {
								//we have requirements, let's make sure they're fulfilled
								for (device in devices) {
									for (cmd in vcmd.requires) {
										if (!device.hasCommand(cmd)) {
											ok = false
											break
										}
									}
									if (!ok) break
								}
							}
							//single device support - some virtual commands require only one device, can't handle more at a time
							if (ok && (!vcmd.singleDevice || (devices.size() == 1))) {
								if (vcmd.flow) {
									flowCommands.push(virtualCommandPrefix() + vcmd.display)
								} else {
									availableCommands.push(virtualCommandPrefix() + vcmd.display)
								}
							}
						}
					}
					if (state.config.expertMode) {
						availableCommands = availableCommands + flowCommands
					}
					def idx = 0
					if (ids.size()) {
						for (tid in ids) {
							section(title: idx == 0 ? "First," : "And then") {
								//display each
								input "$prefix$tid", "enum", options: availableCommands, title: "", required: true, state: "complete", submitOnChange: true
								//parameters
								def cmd = settings["$prefix$tid"]
								def virtual = (cmd && cmd.startsWith(virtualCommandPrefix()))
								def custom = (cmd && cmd.startsWith(customCommandPrefix()))
								cmd = cleanUpCommand(cmd)
								def command = null
								if (virtual) {
									//dealing with a virtual command
									command = getVirtualCommandByDisplay(cmd)
								} else {
									command = getCommandByDisplay(cmd)
								}
								if (command) {
									if (command.parameters) {
										def i = 0
										for (def parameter in command.parameters) {
											def param = parseCommandParameter(parameter)
											if (param) {
												if ((command.parameters.size() == 1) && (param.type == "var")) {
													def task = getActionTask(action, tid)
													//we don't need any indents
													state.taskIndent = 0
													def desc = getTaskDescription(task)
													desc = "$desc".tokenize("=")
													def title = desc && desc.size() == 2 ? desc[0].trim() : "Set variable..."
													def description = desc && desc.size() == 2 ? desc[1].trim() : null
													href "pageSetVariable", params: [actionId: id, taskId: tid], title: title, description: description, required: true, state: description ? "complete" : null, submitOnChange: true
													if (description) {
														def value = task_vcmd_setVariable(null, action, task, true)
														paragraph "Current evaluation: " + value
													}
													break
												}
												if (param.type == "attribute") {
													input "actParam$id#$tid-$i", "enum", options: listCommonDeviceAttributes(devices), title: param.title, required: param.required, submitOnChange: param.last, multiple: false
												} else if (param.type == "attributes") {
													input "actParam$id#$tid-$i", "enum", options: listCommonDeviceAttributes(devices), title: param.title, required: param.required, submitOnChange: param.last, multiple: true
												} else if (param.type == "contact") {
													input "actParam$id#$tid-$i", "contact",  title: param.title, required: param.required, submitOnChange: param.last, multiple: false
												} else if (param.type == "contacts") {
													input "actParam$id#$tid-$i", "contact",  title: param.title, required: param.required, submitOnChange: param.last, multiple: true
												} else if (param.type == "variable") {
													input "actParam$id#$tid-$i", "enum", options: listVariables(true), title: param.title, required: param.required, submitOnChange: param.last, multiple: false
												} else if (param.type == "variables") {
													input "actParam$id#$tid-$i", "enum", options:  listVariables(true), title: param.title, required: param.required, submitOnChange: param.last, multiple: true
												} else if (param.type == "stateVariable") {
													input "actParam$id#$tid-$i", "enum", options: listStateVariables(true), title: param.title, required: param.required, submitOnChange: param.last, multiple: false
												} else if (param.type == "stateVariables") {
													input "actParam$id#$tid-$i", "enum", options:  listStateVariables(true), title: param.title, required: param.required, submitOnChange: param.last, multiple: true
												} else if (param.type == "lifxScenes") {
													input "actParam$id#$tid-$i", "enum", options:  listLifxScenes(), title: param.title, required: param.required, submitOnChange: param.last, multiple: false
												} else if (param.type == "piston") {
													def pistons = parent.listPistons(state.config.expertMode || command.name.contains("follow") ? null : app.label)
													input "actParam$id#$tid-$i", "enum", options: pistons, title: param.title, required: param.required, submitOnChange: param.last, multiple: false
												} else if (param.type == "routine") {
													def routines = location.helloHome?.getPhrases()*.label
													input "actParam$id#$tid-$i", "enum", options: routines, title: param.title, required: param.required, submitOnChange: param.last, multiple: false
												} else if (param.type == "aggregation") {
													def aggregationOptions = ["First", "Last", "Min", "Avg", "Max", "Sum", "Count", "Boolean And", "Boolean Or", "Boolean True Count", "Boolean False Count"]
													input "actParam$id#$tid-$i", "enum", options: aggregationOptions, title: param.title, required: param.required, submitOnChange: param.last, multiple: false
												} else if (param.type == "dataType") {
													def dataTypeOptions = ["boolean", "decimal", "number", "string"]
													input "actParam$id#$tid-$i", "enum", options: dataTypeOptions, title: param.title, required: param.required, submitOnChange: param.last, multiple: false
												} else {
													input "actParam$id#$tid-$i", param.type, range: param.range, options: param.options, title: param.title, required: param.required, multiple: param.multiple, submitOnChange: param.last || (i == command.varEntry), capitalization: "none"
												}
												if (param.last && settings["actParam$id#$tid-$i"]) {
													//this is the last parameter, if filled in
													break
												}
											} else {
												paragraph "Invalid parameter definition for $parameter"
											}
											i += 1
										}
									}
									if (!command.flow) {
										input "actParamMode$id#$tid", "enum", options: getLocationModeOptions(), title: "Only during these modes", description: "Any", required: false, multiple: true
										input "actParamDOW$id#$tid", "enum", options: timeDayOfWeekOptions(), title: "Only on these days", description: "Any", required: false, multiple: true
									}
								} else if (custom) {
									//custom command parameters... complicated stuff
									def i = (int) 1
									while (true) {
										def type = settings["actParam$id#$tid-$i"]
										if (type && (!(type instanceof String) || !(type in ["boolean", "decimal", "number", "string"]))) {
											type = "string"
										}
										def j = (int) Math.floor((i - 1)/2) + 1
										input "actParam$id#$tid-$i", "enum", options: ["boolean", "decimal", "number", "string"], title: type ? "Parameter #$j type" : "Add a parameter", required: false, submitOnChange: true, multiple: false
										if (!type) break
										i += 1
										input "actParam$id#$tid-$i", type, range: "*..*", title: "Parameter #$j value", required: true, submitOnChange: true, multiple: false
										i += 1
									}
									input "actParamMode$id#$tid", "enum", options: getLocationModeOptions(), title: "Only during these modes", description: "Any", required: false, multiple: true
								}
								idx += 1
							}
						}
					}

					section() {
						input "$prefix$maxId", "enum", options: availableCommands, title: "Add a task", required: !ids.size(), submitOnChange: true
					}
				}
			}

			if (actionUsed) {
				section(title: "Action Restrictions") {
					input "actRStateChange$id", "bool", title: action.pid > 0 ? "Only execute on condition state change" : "Only execute on piston state change", required: false
					input "actRMode$id", "mode", title: "Only execute in these modes", description: "Any location mode", required: false, multiple: true
					input "actRAlarm$id", "enum", options: getAlarmSystemStatusOptions(), title: "Only execute during these alarm states", description: "Any alarm state", required: false, multiple: true
					input "actRVariable$id", "enum", options: listVariables(true), title: "Only execute when variable matches", description: "Tap to choose a variable", required: false, multiple: false, submitOnChange: true
					def rVar = settings["actRVariable$id"]
					if (rVar) {
						def options = ["is equal to", "is not equal to", "is less than", "is less than or equal to", "is greater than", "is greater than or equal to"]
						input "actRComparison$id", "enum", options: options, title: "Comparison", description: "Tap to choose a comparison", required: true, multiple: false
						input "actRValue$id", "string", title: "Value", description: "Tap to choose a value to compare", required: false, multiple: false, capitalization: "none"
					}
					input "actRDOW$id", "enum", options: timeDayOfWeekOptions(), title: "Only execute on these days", description: "Any week day", required: false, multiple: true
					def timeFrom = settings["actRTimeFrom$id"]
					input "actRTimeFrom$id", "enum", title: (timeFrom ? "Only execute if time is between" : "Only execute during this time interval"), options: timeComparisonOptionValues(false, false), required: false, multiple: false, submitOnChange: true
					if (timeFrom) {
						if (timeFrom.contains("custom")) {
							input "actRTimeFromCustom$id", "time", title: "Custom time", required: true, multiple: false
						} else {
							input "actRTimeFromOffset$id", "number", title: "Offset (+/- minutes)", range: "*..*", required: true, multiple: false, defaultValue: 0
						}
						def timeTo = settings["actRTimeTo$id"]
						input "actRTimeTo$id", "enum", title: "And", options: timeComparisonOptionValues(false, false), required: true, multiple: false, submitOnChange: true
						if (timeTo && (timeTo.contains("custom"))) {
							input "actRTimeToCustom$id", "time", title: "Custom time", required: true, multiple: false
						} else {
							input "actRTimeToOffset$id", "number", title: "Offset (+/- minutes)", range: "*..*", required: true, multiple: false, defaultValue: 0
						}
					}
					input "actRSwitchOn$id", "capability.switch", title: "Only execute when these switches are all on", description: "Always", required: false, multiple: true
					input "actRSwitchOff$id", "capability.switch", title: "Only execute when these switches are all off", description: "Always", required: false, multiple: true
					if (action.pid > 0) {
						input "actRState$id", "enum", options:["true", "false"], defaultValue: action.rs == false ? "false" : "true", title: action.pid > 0 ? "Only execute when condition state is" : "Only execute on piston state change", required: true
					}
				}

				section(title: "Advanced options") {
					paragraph "When an action schedules tasks for a certain device or devices, these new tasks may cause a conflict with pending future scheduled tasks for the same device or devices. The task override scope defines how these conflicts are handled. Depending on your choice, the following pending tasks are cancelled:\n ● None - no pending task is cancelled\n ● Action - only tasks scheduled by the same action are cancelled\n ● Local - only local tasks (scheduled by the same piston) are cancelled (default)\n ● Global - all global tasks (scheduled by any piston in the CoRE) are cancelled"
					input "actTOS$id", "enum", title: "Task override scope", options:["None", "Action", "Local", "Global"], defaultValue: "Local", required: true
					input "actTCP$id", "enum", title: "Task cancellation policy", options:["None", "Cancel on piston state change"] + (id > 0 ? ["Cancel on condition state change", "Cancel on condition or piston state change"] : []), defaultValue: "None", required: true
				}

				if (id) {
					section(title: "Required data - do not change", hideable: true, hidden: true) {
						input "actParent$id", "number", title: "Parent ID", description: "Value needs to be $pid, do not change", range: "-2..${pid+1}", defaultValue: pid
					}
				}
			}
		}
	}
}

def pageActionDevices(params) {
	state.run = "config"
	def actionId = params?.actionId
	if (!actionId) return
	//convert this to an int - Android thinks this is a float
	actionId = (int) actionId
	def command = params?.command
	def caps = params?.capabilities
	def capabilities = capabilities().findAll{ it.devices }
	if (caps && caps.size()) {
		capabilities = []
		//we don't have a list of capabilities to filter by, let's figure things out by using the command
		for(def cap in caps) {
			def capability = getCapabilityByName(cap)
			if (capability && !(capability in capabilities)) capabilities.push(capability)
		}
	} else {
		if (command) capabilities = listCommandCapabilities(command)
	}

	if (!capabilities) return
	dynamicPage(name: "pageActionDevices", title: "", uninstall: false, install: false) {
		caps = [:]
		//we got a list of capabilities to display
		def used = []
		for(def capability in capabilities.sort{ it.devices.toLowerCase() }) {
			//go through each and look for "devices" - the user-friendly name of what kind of devices the capability stands for
			if (capability.devices) {
				if (!(capability.devices in used)) {
					used.push capability.devices
					def cap = caps[capability.name] ? caps[capability.name] : []
					if (!(capability.devices in cap)) cap.push(capability.devices)
					caps[capability.name] = cap
				}
			}
		}
		if (caps.size()) {
			section() {
				paragraph "Please select devices from the list${caps.size() > 1 ? "s" : ""} below. When done, please tap the Done to continue"
			}
			for(cap in caps) {
				section() {
					input "actDev$actionId#${cap.key}", "capability.${cap.key}", title: "Select ${buildNameList(cap.value, "or")}", multiple: true, required: false
				}
			}
		}
	}
}

private pageSetVariable(params) {
	state.run = "config"
	def aid = params?.actionId ? (int) params?.actionId : (int) state.actionId
	def tid = params?.taskId ? (int) params?.taskId : (int) state.taskId
	state.actionId = aid
	state.taskId = tid
	if (!aid) return
	if (!tid) return
	dynamicPage(name: "pageSetVariable", title: "", uninstall: false, install: false) {
		section("Variable") {
			input "actParam$aid#$tid-0", "text", title: "Variable name", required: true, submitOnChange: true, capitalization: "none"
			input "actParam$aid#$tid-1", "enum", title: "Variable data type", options: ["boolean", "decimal", "number", "string", "time"], required: true, submitOnChange: true
			input "actParam$aid#$tid-2", "bool", title: "Execute during evaluation stage", required: true, defaultValue: false
			//input "actParam$aid#$tid-3", "text", title: "Formula", required: true, submitOnChange: true
		}
		def immediate = settings["actParam$aid#$tid-2"]
		def dataType = settings["actParam$aid#$tid-1"]
		def i = 1
		def operation = ""
		while (dataType) {
			def a1 = i * 4
			def a2 = a1 + 1
			def a3 = a2 + 1
			def op = a3 + 1
			def secondaryDataType = (i == 1 ? dataType : (dataType == "time" ? "decimal" : dataType))
			section(formatOrdinalNumberName(i).capitalize() + " operand") {
				def val = settings["actParam$aid#$tid-$a1"] != null
				def var = settings["actParam$aid#$tid-$a2"]
				if (val || (val == 0) || !var) {
					def inputType = secondaryDataType == "boolean" ? "enum" : secondaryDataType
					input "actParam$aid#$tid-$a1", inputType, range: (i == 1 ? "*..*" : "0..*"), title: "Value", options: ["false", "true"], required: dataType != "string", submitOnChange: true, capitalization: "none"
				}
				if (var || !val) {
					input "actParam$aid#$tid-$a2", "enum", options: listVariables(true, secondaryDataType), title: (var ? "Variable value" : "...or variable value...") + (var ? "\n[${getVariable(var, true)}]" : ""), required: dataType != "string", submitOnChange: true
				}
				if ((dataType == "time") && (i > 1) && !(operation.contains("*") || operation.contains("÷"))) {
					input "actParam$aid#$tid-$a3", "enum", options: ["milliseconds", "seconds", "minutes", "hours", "days", "weeks", "months", "years"], title: "Time unit", required: true, submitOnChange: true, defaultValue: "minutes"
				}
			}
			operation = settings["actParam$aid#$tid-$op"]
			if (operation) operation = "$operation"
			section(title: operation ? "" : "Add operation") {
				def opts = []
				switch (dataType) {
					case "boolean":
						opts += ["AND", "OR"]
						break
					case "string":
						opts += ["+ (concatenate)"]
						break
					case "number":
					case "decimal":
					case "time":
						opts += ["+ (add)", "- (subtract)", "* (multiply)", "÷ (divide)"]
						break
				}
				input "actParam$aid#$tid-$op", "enum", title: "Operation", options: opts, required: false, submitOnChange: true
			}
			i += 1
			if (!operation || i > 10) break
		}
		section("Initialize variables") {
			href "pageInitializeVariable", title: "Initialize a variable"
		}

	}
}

def pageSimulate() {
	state.run = "config"
	dynamicPage(name: "pageSimulate", title: "", uninstall: false, install: false) {
		section("") {
			paragraph "Preparing to simulate piston..."
			paragraph "Current piston state is: ${state.currentState}"
			if (!state.config.app.enabled) {
				paragraph "Piston is currently PAUSED", state: null, required: true
			}
		}
		state.sim = [ evals: [], cmds: [] ]
		def error

		//prepare some stuff
		state.debugLevel = 0
		state.globalVars = [:]
		state.tasker = state.tasker ? state.tasker : []

		def perf = now()
		try {
			broadcastEvent([name: "simulate", date: new Date(), deviceId: "time", conditionId: null], true, false)
			processTasks()
		} catch(all) {
			error = all
		}
		perf = now() - perf
		def evals = state.sim.evals
		def cmds = state.sim.cmds
		exitPoint(perf)

		section("") {
			paragraph "Simulation ended in ${perf}ms.", state: "complete"
			paragraph "New piston state is: ${state.currentState}"
			if (error) {
				paragraph error, required: true, state: null
			}
		}
		section("Evaluations performed") {
			if (evals.size()) {
				for(msg in evals) {
					paragraph msg, state: "complete"
				}
			} else {
				paragraph "No evaluations have been performed."
			}
		}
		section("Commands executed") {
			if (cmds.size()) {
				for(msg in cmds) {
					paragraph msg, state: "complete"
				}
			} else {
				paragraph "No commands have been executed."
			}
		}

		section("Scheduled ST job") {
			def time = getVariable("\$nextScheduledTime")
			paragraph time ? formatLocalTime(time) : "No ST job has been scheduled.", state: time ? "complete" : null
		}

		def tasks = atomicState.tasks
		tasks = tasks ? tasks : [:]
		section("Pending tasks") {
			if (!tasks.size()) {
				paragraph "No tasks are currently scheduled."
			} else {
				for(task in tasks.sort { it.value.time } ) {
					def time = formatLocalTime(task.value.time)
					if (task.value.type == "evt") {
						paragraph "EVENT - $time\n$task.value"
					} else {
						paragraph "COMMAND - $time\n$task.value"
					}
				}
			}
		}
	}
}

def pageRebuild() {
	dynamicPage(name: "pageRebuild", title: "", uninstall: false, install: false) {
		section("") {
			paragraph "Rebuilding piston..."
			rebuildPiston()
			configApp()
			state.run = "config"
			paragraph "Rebuilding is now finished. Please tap Done to go back."
		}
	}
}

def pageToggleEnabled() {
	state.config.app.enabled = !state.config.app.enabled
	if (state.app) state.app.enabled = !!state.config.app.enabled
	dynamicPage(name: "pageToggleEnabled", title: "", uninstall: false, install: false) {
		section() {
			paragraph "The piston is now ${state.config.app.enabled ? "running" : "paused"}."
		}
	}
}

def pageInitializeVariable() {
	dynamicPage(name: "pageInitializeVariable", title: "", uninstall: false, install: false) {
		section("Initialize variable") {
			input "varName", "string", title: "Variable to initialize", required: true, capitalization: "none"
			input "varValue", "string", title: "Initial value", required: true, capitalization: "none"
			href "pageInitializedVariable", title: "Initialize!"
		}
	}
}

def pageInitializedVariable() {
	dynamicPage(name: "pageInitializedVariable", title: "", uninstall: false, install: false) {
		section() {
			def var = settings.varName
			def val = settings.varValue
			if ((var != null) && (val != null)) {
				setVariable(var, val)
				paragraph "Variable {$var} successfully initialized to value '$val'.\n\nPlease tap < or Done to continue.", title: "Success"
			}
		}
	}
}

private buildIfContent() {
	buildIfContent(state.config.app.conditions.id, 0)
}

private buildIfOtherContent() {
	buildIfContent(state.config.app.otherConditions.id, 0)
}

private buildIfContent(id, level) {
	def condition = getCondition(id)
	if (!condition) {
		return null
	}
	def conditionGroup = (condition.children != null)
	def conditionType = (condition.trg ? "trigger" : "condition")
	level = (level ? level : 0)
	def pre = ""
	def preNot = ""
	def tab = ""
	def aft = ""
	switch (level) {
		case 1:
			pre = " ┌ ("
			preNot = " ┌ NOT ("
			tab = " │	"
			aft = " └ )"
			break;
		case 2:
			pre = " │ ┌ ["
			preNot = " │ ┌ NOT ["
			tab = " │ │	"
			aft = " │ └ ]"
			break;
		case 3:
			pre = " │ │ ┌ <"
			preNot = " │ │ ┌ NOT {"
			tab = " │ │ │	"
			aft = " │ │ └ >"
			break;
	}
	if (!conditionGroup) {
		href "pageCondition", params: ["conditionId": id], title: "", description: tab + getConditionDescription(id).trim(), state: "complete", required: false, submitOnChange: false
	} else {

		def grouping = settings["condGrouping$id"]
		def negate = settings["condNegate$id"]

		if (pre) {
			href "pageConditionGroupL${level}", params: ["conditionId": id], title: "", description: (negate? preNot : pre), state: "complete", required: true, submitOnChange: false
		}

		def cnt = 0
		for (child in condition.children) {
			buildIfContent(child.id, level + (child.children == null ? 0 : 1))
			cnt++
			if (cnt < condition.children.size()) {
				def page = (level ? "pageConditionGroupL${level}" : (id == 0 ? "pageIf" : "pageIfOther"))
				href page, params: ["conditionId": id], title: "", description: tab + grouping, state: "complete", required: true, submitOnChange: false
			}
		}

		if (aft) {
			href "pageConditionGroupL${level}", params: ["conditionId": id], title: "", description: aft, state: "complete", required: true, submitOnChange: false
		}
	}
	if (condition.id > 0) {
		//when true - individual actions
		def actions = listActions(id, true)
		def sz = actions.size() - 1
		def i = 0
		for (action in actions) {
			href "pageAction", params: ["actionId": action.id], title: "", description: (i == 0 ? "${tab}╠═(when true)══ {\n" : "") + "${tab}║ " + getActionDescription(action).trim().replace("\n", "\n${tab}║") + (i == sz ? "\n${tab}╚════════ }" : ""), state: null, required: false, submitOnChange: false
			i = i + 1
		}
		actions = listActions(id, false)
		sz = actions.size() - 1
		i = 0
		for (action in actions) {
			href "pageAction", params: ["actionId": action.id], title: "", description: (i == 0 ? "${tab}╠═(when false)══ {\n" : "") + "${tab}║ " + getActionDescription(action).trim().replace("\n", "\n${tab}║") + (i == sz ? "\n${tab}╚════════ }" : ""), state: null, required: false, submitOnChange: false
			i = i + 1
		}
	} else {
		def value = evaluateCondition(condition)
		paragraph "Current evaluation: $value", required: true, state: ( value ? "complete" : null )
	}
}

/********** COMMON INITIALIZATION METHODS **********/
def installed() {
	initialize()
	return true
}

def updated() {
	unsubscribe()
	initialize()
	return true
}

def initialize() {
	parent ? initializeCoREPiston() : initializeCoRE()
}

/******************************************************************************/
/*** 																		***/
/*** COMMON PUBLISHED METHODS												***/
/*** 																		***/
/******************************************************************************/

def mem(showBytes = true) {
	def bytes = state.toString().length()
	return Math.round(100.00 * (bytes/ 100000.00)) + "%${showBytes ? " ($bytes bytes)" : ""}"
}

def cpu() {
	if (state.lastExecutionTime == null) {
		return "N/A"
	} else {
		def cpu = Math.round(state.lastExecutionTime / 20000)
		if (cpu > 100) {
			cpu = 100
		}
		return "$cpu%"
	}
}

def getVariable(name, forDisplay) {
	def value = getVariable(name)
	if (forDisplay && (value instanceof Long) && (value >= 999999999999)) return formatLocalTime(value)
	return value
}

def getVariable(name) {
	name = sanitizeVariableName(name)
	switch (name) {
		case "\$now": return now()
		case "\$hour24": return adjustTime().hours
		case "\$hour":
			def h = adjustTime().hours
			return (h == 0 ? 12 : (h > 12 ? h - 12 : h))
		case "\$meridian":
			def h = adjustTime().hours
			return ( h < 12 ? "AM" : "PM")
		case "\$meridianWithDots":
			def h = adjustTime().hours
			return ( h <12 ? "A.M." : "P.M.")
		case "\$minute": return adjustTime().minutes
		case "\$second": return adjustTime().seconds
		case "\$time":
			def t = adjustTime()
			def h = t.hours
			def m = t.minutes
			return (h == 0 ? 12 : (h > 12 ? h - 12 : h)) + ":" + (m < 10 ? "0$m" : "$m") + " " + (h <12 ? "A.M." : "P.M.")
		case "\$time24":
			def t = adjustTime()
			def h = t.hours
			def m = t.minutes
			return h + ":" + (m < 10 ? "0$m" : "$m")
		case "\$day": return adjustTime().date
		case "\$dayOfWeek": return getDayOfWeekNumber()
		case "\$dayOfWeekName": return getDayOfWeekName()
		case "\$month": return adjustTime().month + 1
		case "\$monthName": return getMonthName()
		case "\$year": return adjustTime().year + 1900
		case "\$now": return now()
		case "\$random":
			def result = getRandomValue(name) ?: (float)Math.random()
			setRandomValue(name, result)
			return result
		case "\$randomColor":
			def result = getRandomValue(name) ?: getColorByName("Random").rgb
			setRandomValue(name, result)
			return result
		case "\$randomColorName":
			def result = getRandomValue(name) ?: getColorByName("Random").name
			setRandomValue(name, result)
			return result
		case "\$randomLevel":
			def result = getRandomValue(name) ?: (int)Math.round(100 * Math.random())
			setRandomValue(name, result)
			return result
		case "\$randomHue":
			def result = getRandomValue(name) ?: (int)Math.round(360 * Math.random())
			setRandomValue(name, result)
			return result
		case "\$randomSaturation":
			def result = getRandomValue(name) ?: (int)Math.round(50 + (50 * Math.random()))
			setRandomValue(name, result)
			return result
		case "\$midnight":
			def rightNow = adjustTime().time
			return convertDateToUnixTime(rightNow - rightNow.mod(86400000))
		case "\$nextMidnight":
			def rightNow = adjustTime().time
			return convertDateToUnixTime(rightNow - rightNow.mod(86400000) + 86400000)
		case "\$noon":
			def rightNow = adjustTime().time
			return convertDateToUnixTime(rightNow - rightNow.mod(86400000) + 43200000)
		case "\$nextNoon":
			def rightNow = adjustTime().time
			if (rightNow - rightNow.mod(86400000) + 43200000 < rightNow) rightNow += 86400000
			return convertDateToUnixTime(rightNow - rightNow.mod(86400000) + 43200000)
		case "\$sunrise":
			def sunrise = getSunrise()
			def rightNow = adjustTime().time
			return convertDateToUnixTime(rightNow - rightNow.mod(86400000) + sunrise.hours * 3600000 + sunrise.minutes * 60000)
		case "\$nextSunrise":
			def sunrise = getSunrise()
			def rightNow = adjustTime().time
			if (sunrise.time < rightNow) rightNow += 86400000
			return convertDateToUnixTime(rightNow - rightNow.mod(86400000) + sunrise.hours * 3600000 + sunrise.minutes * 60000)
		case "\$sunset":
			def sunset = getSunset()
			def rightNow = adjustTime().time
			return convertDateToUnixTime(rightNow - rightNow.mod(86400000) + sunset.hours * 3600000 + sunset.minutes * 60000)
		case "\$nextSunset":
			def sunset = getSunset()
			def rightNow = adjustTime().time
			if (sunset.time < rightNow) rightNow += 86400000
			return convertDateToUnixTime(rightNow - rightNow.mod(86400000) + sunset.hours * 3600000 + sunset.minutes * 60000)
		case "\$currentStateDuration":
			try {
				return state.systemStore["\$currentStateSince"] ? now() - (new Date(state.systemStore["\$currentStateSince"])).time : null
			} catch(all) {
				return null
			}
			return null
		case "\$locationMode":
			return location.mode
		case "\$shmStatus":
			return getAlarmSystemStatus()
	}
	if (!name) return null
	if (parent && name.startsWith("@")) {
		return parent.getVariable(name)
	} else {
		if (name.startsWith("\$")) {
			return state.systemStore[name]
		} else {
			if (parent) return state.store[name]
			return atomicState.store[name]
		}
	}
}

def setVariable(name, value, system = false, globalVars = null) {
	name = sanitizeVariableName(name)
	if (!name) return
	if (name.contains(",")) {
		//multi variables
		def vars = name.tokenize(",")
		for (var in vars) {
			setVariable(var, value, system, globalVars)
		}
		return
	}
	if (parent && name.startsWith("@")) {
		def gv = state.globalVars instanceof Map ? state.globalVars : [:]
		parent.setVariable(name, value, false, gv)
		state.globalVars = gv
	} else {
		if (name.startsWith("\$")) {
			if (system) {
				state.systemStore[name] = value
			}
		} else {
			debug "Storing variable $name with value $value"
			if (!parent) {
				//we're using atomic state in parent app
				def store = atomicState.store
				def oldValue = store[name]
				store[name] = value
				atomicState.store = store
				//save var name for broadcasting events
				if (globalVars.containsKey(name)) {
					globalVars[name].newValue = value
				} else {
					globalVars[name] = [oldValue: oldValue, newValue: value]
				}
			} else {
				state.store[name] = value
			}
		}
	}
}

def publishVariables() {
	if (!parent) return null
	//we're saving the atomic store to our regular store to prevent race conditions
	def globalVars = state.globalVars
	for (variable in globalVars) {
		def name = variable.key
		def oldValue = variable.value.oldValue
		def newValue = variable.value.newValue
		if (oldValue != newValue) {
			sendLocationEvent(name: "variable", value: name, displayed: true, linkText: "CoRE Global Variable", isStateChange: true, descriptionText: "Variable $name changed from '$oldValue' to '$newValue'", data: [app: "CoRE", oldValue: oldValue, value: newValue])
		}
	}
	state.globalVars = [:]
}

def deleteVariable(name) {
	//used during config, safe to use state
	name = sanitizeVariableName(name)
	if (!name) return
	if (parent && name.startsWith("@")) {
		parent.deleteVariable(name)
	} else {
		if (state.store) {
			state.store.remove(name)
		}
	}
}

def getStateVariable(name, global = false) {
	name = sanitizeVariableName(name)
	if (!name) return null
	if (parent && global) {
		return parent.getStateVariable(name)
	} else {
		if (parent) return state.stateStore[name]
		return atomicState.stateStore[name]
	}
}

def setStateVariable(name, value, global = false) {
	name = sanitizeVariableName(name)
	if (!name) {
		return
	}
	if (parent && global) {
		parent.setStateVariable(name, value)
	} else {
		debug "Storing state variable $name with value $value"
		if (parent) {
			def store = atomicState.stateStore
			store[name] = value
			atomicState.stateStore = store
		} else {
			//using atomic state for globals
			def store = atomicState.stateStore
			store[name] = value
			atomicState.stateStore = store
		}
	}
}

private getRandomValue(name) {
	state.temp = state.temp ?: [:]
	state.temp.randoms = state.temp.randoms ?: [:]
	return state.temp?.randoms[name]
}

private setRandomValue(name, value) {
	state.temp = state.temp ?: [:]
	state.temp.randoms = state.temp.randoms ?: [:]
	state.temp.randoms[name] = value
}

private resetRandomValues() {
	state.temp = state.temp ?: [:]
	state.temp.randoms = [:]
}

private testDataType(value, dataType) {
	if (!dataType || !value) return true
	switch (dataType) {
		case "bool":
		case "boolean":
		case "string":
			return true
		case "time":
			return (value instanceof Long) && (value > 999999999999)
		case "number":
		case "decimal":
			return !((value instanceof Long) && (value > 999999999999)) && ("$value".isInteger() || "$value".isFloat())
	}
	return false
}

def listVariablesInBulk() {
	def result = [:]
	for(variable in listVariables()) {
		result[variable] = getVariable(variable, true)
	}
	return result.sort{ it.key.substring(0, 1) in ["\$", "@"] ? it.key : "!${it.key}" }
}

def listVariables(config = false, dataType = null, listLocal = true, listGlobal = true, listSystem = true) {
	def result = []
	def parentResult = null
	def systemResult = []
	if (listLocal) {
		for (variable in state.store) {
			if (!dataType || testDataType(variable.value, dataType)) {
				result.push(variable.key)
			}
		}
	}
	if (parent && listSystem) {
		for (variable in state.systemStore) {
			if (!dataType || testDataType(variable.value, dataType)) {
				systemResult.push(variable.key)
			}
		}
	}
	if (listGlobal) {
		if (parent) {
			parentResult = parent.listVariables(config, dataType)
		}
	}
	if (parent && config) {
		//look for variables set during conditions
		def list = settings.findAll{it.key.startsWith("condVar") && !it.key.contains("#")}
		for (it in list) {
			if (it.value instanceof String) {
				def vars = sanitizeVariableName(it.value)
				if (vars instanceof String) vars = vars.tokenize(",")
				for (var in vars) {
					if (var.startsWith("@")) {
						//global
						if (listGlobal && !(var in parentResult)) {
							if (!dataType || testDataType(it.value, dataType)) {
								parentResult.push(var)
							}
						}
					} else {
						//local
						if (listLocal && !(var in result)) {
							if (!dataType || testDataType(it.value, dataType)) {
								result.push(var)
							}
						}
					}
				}
			}
		}
		//look for tasks that set variables...
		list = settings.findAll{it.key.startsWith("actTask")}
		for (it in list) {
			if (it.value instanceof String) {
				def virtualCommand = getVirtualCommandByDisplay(cleanUpCommand(it.value))
				if (virtualCommand && (virtualCommand.varEntry != null)) {
					def vars = sanitizeVariableName(settings[it.key.replace("actTask", "actParam") + "-${virtualCommand.varEntry}"])
					if (vars instanceof String) vars = vars.tokenize(",")
					for (var in vars) {
						if (var.startsWith("@")) {
							//global
							if (!(var in parentResult)) {
								parentResult.push(var)
							}
						} else {
							//local
							if (!(var in result)) {
								result.push(var)
							}
						}
					}
				}
			}
		}
	}
	return result.sort() + (parentResult ? parentResult.sort() : []) + systemResult.sort()
}

def listStateVariables(config = false, dataType = null, listLocal = true, listGlobal = true) {
	def result = []
	def parentResult = null
	if (listLocal) {
		for (variable in state.stateStore) {
			if (!variable.key.contains(":::")) {
				if (!dataType || testDataType(variable.value, dataType)) {
					result.push(variable.key)
				}
			}
		}
	}
	if (listGlobal) {
		if (parent) {
			parentResult = parent.listStateVariables(config, dataType)
		}
	}
	if (parent && config) {
		//look for variables set during conditions
		def list = settings.findAll{it.key.startsWith("actTask")}
		for (it in list) {
			if (it.value instanceof String) {
				def virtualCommand = getVirtualCommandByDisplay(cleanUpCommand(it.value))
				if (virtualCommand && (virtualCommand.stateVarEntry != null)) {
					def vars = sanitizeVariableName(settings[it.key.replace("actTask", "actParam") + "-${virtualCommand.stateVarEntry}"]).tokenize(",")
					for (var in vars) {
						if (var.startsWith("@")) {
							//global
							if (!(var in parentResult)) {
								parentResult.push(var)
							}
						} else {
							//local
							if (!(var in result)) {
								result.push(var)
							}
						}
					}
				}
			}
		}
	}
	return result.sort() + (parentResult ? parentResult.sort() : [])
}

/******************************************************************************/
/***																		***/
/*** CoRE CODE																***/
/***																		***/
/******************************************************************************/

/******************************************************************************/
/*** CoRE INITIALIZATION METHODS											***/
/******************************************************************************/

def initializeCoRE() {
	initializeCoREStore()
	refreshPistons()
	subscribe(location, "CoRE", coreHandler)
	subscribe(location, "askAlexa", askAlexaHandler)
	subscribe(location, "echoSistant", echoSistantHandler)
	subscribe(app, appTouchHandler)
	/* temporary - remove old handlers */
	unschedule(recovery1)
	unschedule(recovery2)
//    subscribe(null, "intrusion", intrusionHandler, [filterEvents: false])
//    subscribe(null, "newIncident", intrusionHandler, [filterEvents: false])
//    subscribe(null, "newMessage", intrusionHandler, [filterEvents: false])
	switch (settings["recovery#1"]) {
		case "Disabled":
			unschedule(recovery1Handler)
			break
		case "Every 1 hour":
			runEvery1Hour(recovery1Handler)
			break
		default:
			runEvery3Hours(recovery1Handler)
			break
	}

	def t = new Date(now())
	def sch = "${t.seconds} ${t.minutes}"
	def sch2 = "$sch ${t.hours}"
	switch (settings["recovery#2"]) {
		case "Disabled":
			unschedule(recovery2)
			break
		case "Every 2 hours":
			schedule("$sch 0/2 1/1 * ? *", recovery2Handler)
			break
		case "Every 4 hours":
			schedule("$sch 0/4 1/1 * ? *", recovery2Handler)
			break
		case "Every 6 hours":
			schedule("$sch 0/6 1/1 * ? *", recovery2Handler)
			break
		case "Every 12 hours":
			schedule("$sch 0/12 1/1 * ? *", recovery2Handler)
			break
		case "Every 2 days":
			schedule("$sch2 1/2 * ? *", recovery2Handler)
			break
		case "Every 3 days":
			schedule("$sch2 1/3 * ? *", recovery2Handler)
			break
		default:
			schedule("$sch2 1/1 * ? *", recovery2Handler)
			break
	}
}

def intrusionHandler(evt) {
	//not working yet
}

def initializeCoREStore() {
	state.store = state.store ? state.store : [:]
	state.modes = state.modes ? state.modes : [:]
	state.modules = state.modules ? state.modules : [:]
	state.stateStore = state.stateStore ? state.stateStore : [:]
	state.askAlexaMacros = state.askAlexaMacros ? state.askAlexaMacros : []
	state.echoSistantProfiles = state.echoSistantProfiles ? state.echoSistantProfiles : []
	state.globalVars = state.globalVars ? state.globalVars : []
}


def coreHandler(evt) {
	if (!evt) return
	switch (evt.value) {
		case "execute":
			if (evt.jsonData && evt.jsonData?.pistonName) {
				execute(evt.jsonData.pistonName)
			}
			break
	}
}

def askAlexaHandler(evt) {
	if (!evt) return
	switch (evt.value) {
		case "refresh":
			atomicState.askAlexaMacros = evt.jsonData && evt.jsonData?.macros ? evt.jsonData.macros : []
			break
	}
}

def echoSistantHandler(evt) {
	if (!evt) return
	switch (evt.value) {
		case "refresh":
			atomicState.echoSistantProfiles = evt.jsonData && evt.jsonData?.profiles ? evt.jsonData.profiles : []
			break
	}
}

def appTouchHandler(evt) {
	recoverPistons(true)
}

def childUninstalled() {
	refreshPistons()
}

private recoverPistons(recoverAll = false, excludeAppId = null) {
	if (recoverAll) debug "Piston recovery initiated...", null, "trace"
	int count = 0
	def recovery = atomicState.recovery
	if (!(recovery instanceof Map)) recovery = [:]
	def threshold = now() - 30000
	def apps = getChildApps()
	for(app in apps) {
		if ((recoverAll || (recovery[app.id] && (recovery[app.id] < threshold))) && (!excludeAppId || (excludeAppId != app.id))) {
			count += 1
			if (recoverAll || excludeAppId) {
				sendLocationEvent(name: "CoRE Recovery [${app.id}]", value: "", displayed: true, linkText: "CoRE/${app.label} Recovery", isStateChange: true)
			} else {
				def message = "Found CoRE Piston '${app.label ?: app.name}' about ${Math.round((now() - recovery[app.id])/ 1000)} seconds past due, attempting recovery"
				int n = (int) (settings["recoveryNotifications"] ? 1 : 0) + (int) (settings["recoveryPushNotifications"] ? 2 : 0)
				switch (n) {
					case 1:
						sendNotificationEvent(message)
						break
					case 2:
						sendPushMessage(message)
						break
					case 3:
						sendPush(message)
						break
				}
				app.recoveryHandler(null, false)
			}
			subscribeToRecovery(app.id, null)
		}
	}
	if (recoverAll || (count > 0)) debug "Piston recovery finished, $count piston${count == 1 ? " was" : "s were"} recovered.", null, "trace"
	if (recoverAll) refreshPistons(false)
	return true
}

def rebuildPistons() {
	debug "Initializing piston rebuild...", null, trace
	for(app in getChildApps()) {
		debug "Rebuilding piston ${app.label ?: app.name}", null, trace
		sendLocationEvent(name: "CoRE Recovery [${app.id}]", value: "", displayed: true, linkText: "CoRE/${app.label} Recovery", isStateChange: true, data: [rebuild: true])
		//app.rebuildPiston(true)
	}
	debug "Done rebuilding pistons.", null, trace
}

//temporary - to be removed after 2018/01/01
def recovery1() {
	recovery1Handler()
}
//temporary
def recovery2() {
	recovery2Handler()
}

def recovery1Handler() {
	debug "Received a recovery stage 1 event", null, "trace"
	recoverPistons(true)
}

def recovery2Handler() {
	debug "Received a recovery stage 2 event", null, "trace"
	recoverPistons(true)
}

private initializeCoREEndpoint() {
	if (!state.endpoint) {
		try {
			def accessToken = createAccessToken()
			if (accessToken) {
				state.endpoint = apiServerUrl("/api/token/${accessToken}/smartapps/installations/${app.id}/")
			}
		} catch(e) {
			state.endpoint = null
		}
	}
	return state.endpoint
}

mappings {
	path("/dashboard") {action: [GET: "api_dashboard"]}
	path("/getDashboardData") {action: [GET: "api_getDashboardData"]}
	path("/ifttt/:eventName") {action: [GET: "api_ifttt", POST: "api_ifttt"]}
	path("/execute") {action: [POST: "api_execute"]}
	path("/execute/:pistonName") {action: [GET: "api_execute", POST: "api_execute"]}
	path("/tap") {action: [POST: "api_tap"]}
	path("/tap/:tapId") {action: [GET: "api_tap"]}
	path("/pause") {action: [POST: "api_pause"]}
	path("/resume") {action: [POST: "api_resume"]}
	path("/piston") {action: [POST: "api_piston"]}
}

def api_dashboard() {
	def cdn = "https://core.caramaliu.com/dashboard"
	def theme = (settings["dashboardTheme"] ?: "experimental").toLowerCase()
	render contentType: "text/html", data: "<!DOCTYPE html><html lang=\"en\" ng-app=\"CoRE\"><base href=\"${state.endpoint}\"><head><meta charset=\"utf-8\"/><meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\"><link rel=\"stylesheet prefetch\" href=\"$cdn/static/$theme/css/components/components.min.css\"/><link rel=\"stylesheet prefetch\" href=\"$cdn/static/$theme/css/app.css\"/><script type=\"text/javascript\" src=\"$cdn/static/$theme/js/components/components.min.js\"></script><script type=\"text/javascript\" src=\"$cdn/static/$theme/js/app.js\"></script><script type=\"text/javascript\" src=\"$cdn/static/$theme/js/modules/dashboard.module.js\"></script></head><body><ng-view></ng-view></body></html>"
}

def api_getDashboardData() {
	def result = [ pistons: [] ]
	def pistons = atomicState.pistons
	if (!pistons) {
		refreshPistons(false)
		pistons = atomicState.pistons
	}
	for(piston in pistons) {
		result.pistons.push piston.value
	}
	//sort the pistons
	result.pistons = result.pistons.sort { it.l }
	result.variables = [:]
	for(variable in atomicState.store) {
		result.variables[variable.key] = getVariable(variable.key, true)
	}
	result.variables = result.variables.sort{ it.key }
	result.version = version()
	result.taps = state.taps
	result.now = now()
	return result
}

def api_pause() {
	def data = request?.JSON
	def pistonId = data?.pistonId
	if (pistonId) {
		def child = getChildApps()?.find { it.id == pistonId }
		if (child) {
			child.pause()
			def pistons = atomicState.pistons ?: [:]
			pistons[child.id] = child.getSummary()
			atomicState.pistons = pistons
		}
	}
	return api_getDashboardData()
}

def api_execute() {
	def data = request?.JSON
	def pistonName = params?.pistonName ?: data?.pistonName
	def result = "Sorry, piston $pistonName could not be found."
	def d = debug("Received an API execute request for piston '$pistonName' with data: $data")
	if (pistonName) {
		data.remove "pistonName"
		result = execute(pistonName, data)
		result = "Piston $pistonName is now being executed."
	}
	render contentType: "text/html", data: "<!DOCTYPE html><html lang=\"en\">$result<body></body></html>"
}

def api_tap() {
	def data = request?.JSON
	def tapId = params?.tapId ?: data?.tapId
	def tap = state.taps.find{ "${it.i}" == tapId }
	def result = ""
	if (tap && tap.p) {
		for(pistonName in tap.p) {
			execute(pistonName)
			result += "Piston $pistonName is now being executed.<br/>"
		}
	}
	def d = debug("Received an API tap request for tapID $tapId")
	render contentType: "text/html", data: "<!DOCTYPE html><html lang=\"en\">$result<body></body></html>"
}

def api_ifttt() {
	def data = request?.JSON
	def eventName = params?.eventName
	if (eventName) {
		sendLocationEvent([name: "ifttt", value: eventName, isStateChange: true, linkText: "IFTTT event", descriptionText: "CoRE has received an IFTTT event: $eventName", data: data])
	}
	render contentType: "text/html", data: "<!DOCTYPE html><html lang=\"en\">Received event $eventName.<body></body></html>"
}

def api_resume() {
	def data = request?.JSON
	def pistonId = data?.pistonId
	if (pistonId) {
		def child = getChildApps().find { it.id == pistonId }
		if (child) {
			child.resume()
			def pistons = atomicState.pistons ?: [:]
			pistons[child.id] = child.getSummary()
			atomicState.pistons = pistons
		}
	}
	return api_getDashboardData()
}

def api_piston() {
	def data = request?.JSON
	def pistonId = data?.pistonId
	if (pistonId) {
		def child = getChildApps().find { it.id == pistonId }
		if (child) {
			def result = [
				app: child.getPistonApp(),
				tasks: child.getPistonTasks(),
				summary: child.getSummary()
			]
			if (result.app.conditions) withEachCondition(result.app.conditions, "api_piston_prepare", child)
			if (result.app.otherConditions) withEachCondition(result.app.otherConditions, "api_piston_prepare", child)
			if (result.app.actions) {
				for(def action in result.app.actions) {
					action.desc = child.getActionDeviceList(action)
					state.taskIndent = 0
					if (action.t) {
						for(def task in action.t) {
							task.desc = getTaskDescription(task)
						}
						action.t = action.t.sort{ it.i }
					}
				}
				result.app.actions = result.app.actions.sort{ (it.rs == false ? -1 : 1) * it.id }
			}
			result.variables = child.listVariablesInBulk()
			return result
		}
	}
	return null
}

private api_piston_prepare(condition, child) {
	condition.desc = child.getPistonConditionDescription(condition)
}

/******************************************************************************/
/*** CoRE PUBLISHED METHODS													***/
/******************************************************************************/

def expertMode() {
	return !!settings["expertMode"]
}

def listPistons(excludeApp = null, type = null) {
	if (!type) {
		return getChildApps()*.label.findAll{ it != excludeApp }.sort { it }
	}
	def result = []
	def pistons = getChildApps()
	for (piston in pistons) {
		if ((piston.getPistonType() == type) && (piston.label != excludeApp)) {
			result.push piston.label
		}
	}
	return result.sort{ it }
}

def execute(pistonName, data = null) {
	if (parent) {
		//if a child executes a piston, we need to save the variables to the atomic state to make them show in the new piston execution
		//def store = state.store
		//state.store = store
		//atomicState.store = store
		return parent.execute(pistonName)
	} else {
		def piston = getChildApps().find{ it.label == pistonName }
		if (piston) {
			//fire up the piston
			return piston.executeHandler(data)
		}
		return null
	}
}

def updateChart(name, value) {
	def charts = atomicState.charts
	charts = charts ? charts : [:]
	def modified = false
	def lastQuarter = getPreviousQuarterHour()
	def chart = charts[name]
	if (!chart) {
		//create a log with that name
		chart = [:]
		//create the log for the last 96 quarter-hours
		def quarter = lastQuarter
		for (def i = 0; i < 96; i++) {
			chart["$i"] = [q: quarter, t: 0, c: 0]
			//chart["q$i"].q = quarter
			//chart["q$i"].t = 0
			//chart["q$i"].c = 0
			quarter = quarter - 900000
		}
		charts[name] = chart
		modified = true
	}
	if (lastQuarter != chart["0"].q) {
		//we need to advance the log
		def steps = Math.floor((lastQuarter - chart["0"].q) / 900000).toInteger()
		if (steps != 0) {
			modified = true
			//we need to shift the log, we're in a different current quarter
			if ((steps < 1) || (steps > 95)) {
				//in case of weird things, we reset the whole log
				steps = 96
			}
			if (steps < 96) {
				//reset the log as it seems we have a problem
				for (def i = 95; i >= steps; i--) {
					chart["$i"] = chart["${i-steps}"]
					//chart["q$i"].q = chart["q${i-steps}"].q
					//chart["q$i"].c = chart["q${i-steps}"].c
					//chart["q$i"].t = chart["q${i-steps}"].t
				}
			}
			//reset the new quarters
			def quarter = lastQuarter
			for (def i = 0; i < steps; i++) {
				chart["$i"] = [q: quarter, t: 0, c:0]
				//chart["q$i"].t = 0
				//chart["q$i"].c = 0
				quarter = quarter - 900000
			}
		}
	}
	if (value) {
		modified = true
		chart["0"].t = chart["0"].t + value
		chart["0"].c = chart["0"].c + 1
	}
	if (modified) {
		charts[name] = chart
		atomicState.charts = charts
	}
	return null
}

def subscribeToRecovery(appId, recoveryTime) {
	if (parent) {
		parent.subscribeToRecovery(appId, recoveryTime);
	} else {
		def recovery = atomicState.recovery
		if (!(recovery instanceof Map)) recovery = [:]
		if (recoveryTime) debug "Subscribing app $appId to recovery in about ${Math.round((recoveryTime - now() + 30000)/1000)} seconds"
		recovery[appId] = recoveryTime
		atomicState.recovery = recovery
		//kick start all other dead pistons, use location events...
		if (recoveryTime != null) recoverPistons(false, appId)
	}
}

private onChildExitPoint(piston, lastEvent, duration, nextScheduledTime, summary) {
	if (parent) {
		parent.onChildExitPoint(piston, lastEvent, duration, nextScheduledTime, summary)
	} else {
		if (lastEvent) updateChart("delay", lastEvent.delay)
		updateChart("exec", duration)
		subscribeToRecovery(piston.id, nextScheduledTime ?: 0)
		def pistons = atomicState.pistons ?: [:]
		pistons[piston.id] = summary
		atomicState.pistons = pistons
	}
}

def generatePistonName() {
	if (parent) {
		return null
	}
	def apps = getChildApps()
	def i = 1
	while (true) {
		def name = i == 5 ? "Mambo No. 5" : "CoRE Piston #$i"
		def found = false
		for (app in apps) {
			if (app.label == name) {
				found = true
				break
			}
		}
		if (found) {
			i++
			continue
		}
		return name
	}
}

def refreshPistons(event = true) {
	if (event) sendLocationEvent([name: "CoRE", value: "refresh", isStateChange: true, linkText: "CoRE Refresh", descriptionText: "CoRE has an updated list of pistons", data: [pistons: listPistons()]])
	def pistons = [:]
	for(app in getChildApps()) {
		pistons[app.id] = app.getSummary()
	}
	atomicState.pistons = pistons
}

def listAskAlexaMacros() {
	if (parent) return parent.listAskAlexaMacros()
	return state.askAlexaMacros ? state.askAlexaMacros : []
}

def listEchoSistantProfiles() {
	if (parent) return parent.listEchoSistantProfiles()
	return state.echoSistantProfiles ? state.echoSistantProfiles : []
}

def getIftttKey() {
	if (parent) return parent.getIftttKey()
	def module = atomicState.modules?.IFTTT
	return (module && module.connected ? module.key : null)
}

def getLifxToken() {
	if (parent) return parent.getLifxToken()
	def module = atomicState.modules?.LIFX
	return (module && module.connected ? module.token : null)
}

def listLifxScenes() {
	if (parent) return parent.listLifxScenes()
	def modules = atomicState.modules
	if (modules && modules["LIFX"] && modules["LIFX"].connected) {
		return modules["LIFX"]?.scenes*.name
	}
	return []
}

def getLifxSceneId(name) {
	if (parent) return parent.getLifxSceneId(name)
	def modules = atomicState.modules
	if (modules && modules["LIFX"] && modules["LIFX"].connected) {
		def scene = modules["LIFX"]?.scenes.find { it.name == name }
		if (scene) return scene.id
	}
	return null
}

/******************************************************************************/
/***																		***/
/*** CoRE PISTON CODE														***/
/***																		***/
/******************************************************************************/

/******************************************************************************/
/*** CoRE PISTON INITIALIZATION METHODS										***/
/******************************************************************************/

def initializeCoREPiston() {
	// TODO: subscribe to attributes, devices, locations, etc.
	//move app to production
	state.run = "config"
	state.debugLevel = 0
	debug "Initializing app...", 1
	cleanUpConditions(true)
	state.app = state.config ? state.config.app : state.app
	//save misc
	state.app.mode = settings.mode
	state.app.debugging = settings.debugging
	state.app.disableCO = settings.disableCO
	state.app.description = settings.description
	state.app.restrictions = cleanUpMap([
		a: settings["restrictionAlarm"],
		m: settings["restrictionMode"],
		v: settings["restrictionVariable"],
		vc: settings["restrictionComparison"],
		vv: settings["restrictionValue"] != null ? settings["restrictionValue"] : "",
		tf: settings["restrictionTimeFrom"],
		tfc: settings["restrictionTimeFromCustom"],
		tfo: settings["restrictionTimeFromOffset"],
		tt: settings["restrictionTimeTo"],
		ttc: settings["restrictionTimeToCustom"],
		tto: settings["restrictionTimeToOffset"],
		w: settings["restrictionDOW"],
		s1: buildDeviceNameList(settings["restrictionSwitchOn"], "and"),
		s0: buildDeviceNameList(settings["restrictionSwitchOff"], "and"),
		pe: settings["restrictionPreventTaskExecution"],
	])
	state.lastInitialized = now()
	setVariable("\$lastInitialized", state.lastInitialized, true)
	setVariable("\$currentState", state.currentState, true)
	setVariable("\$currentStateSince", state.currentStateSince, true)

	if (state.app.enabled) {
		resume()
	}

	state.remove("config")
	state.remove("temp")

	debug "Done", -1
	parent.refreshPistons()
	//we need to finalize to write atomic state
	//save all atomic states to state
	//to avoid race conditions
}

def initializeCoREPistonStore() {
	state.temp = state.temp ?: [:]
	state.cache = [:]
	state.tasks = state.tasks ? state.tasks : [:]
	state.store = state.store ? state.store : [:]
	state.stateStore = state.stateStore ? state.stateStore : [:]
	state.systemStore = state.systemStore ? state.systemStore : initialSystemStore()
	for (var in initialSystemStore()) {
		if (!state.containsKey(var.key)) {
			state.systemStore[var.key] = null
		}
	}
}

/* prepare configuration version of app */
private configApp() {
	initializeCoREPistonStore()
	if (!state.config) {
		//initiate config app, since we have no running version yet (not yet installed)
		state.config = [:]
		state.config.conditionId = 0
		state.config.app = state.app && (state.app.conditions != null) && (state.app.otherConditions != null) && (state.app.actions != null) ? state.app : null
		if (!state.config.app) {
			state.config.app = [:]
			//create the root condition
			state.config.app.conditions = createCondition(true)
			state.config.app.conditions.id = 0
			state.config.app.otherConditions = createCondition(true)
			state.config.app.otherConditions.id = -1
			state.config.app.actions = []
			state.config.app.enabled = true
			state.config.app.created = now()
			state.config.app.version = version()
			rebuildConditions()
			rebuildActions()
		}
	}
	//get expert savvy
	state.config.expertMode = parent.expertMode()
	state.config.app.mode = settings.mode ? settings.mode : "Basic"
	state.config.app.description = settings.description
	state.config.app.enabled = !!state.config.app.enabled
	if (!state.app) state.app = [:]
}

private subscribeToAll(appData) {
	debug "Initializing subscriptions...", 1
	state.deviceSubscriptions = 0
	def hasTriggers = getConditionHasTriggers(appData.conditions)
	def hasLatchingTriggers = false
	if (settings.mode in ["Latching", "And-If", "Or-If"]) {
		//we really get the count
		hasLatchingTriggers = getConditionHasTriggers(appData.otherConditions)
		//simulate subscribing to both lists
		def subscriptions = subscribeToDevices(appData.conditions, hasTriggers, null, null, null, null)
		def latchingSubscriptions = subscribeToDevices(appData.otherConditions, hasLatchingTriggers, null, null, null, null)
		//we now have the two lists that we'd be subscribing to, let's figure out the common elements
		def commonSubscriptions = [:]
		for (subscription in subscriptions) {
			if (latchingSubscriptions.containsKey(subscription.key)) {
				//found a common subscription, save it
				commonSubscriptions[subscription.key] = true
			}
		}
		//perform subscriptions
		subscribeToDevices(appData.conditions, false, bothDeviceHandler, null, commonSubscriptions, null)
		subscribeToDevices(appData.conditions, hasTriggers, deviceHandler, null, null, commonSubscriptions)
		subscribeToDevices(appData.otherConditions, hasLatchingTriggers, latchingDeviceHandler, null, null, commonSubscriptions)
	} else {
		//simple IF case, no worries here
		subscribeToDevices(appData.conditions, hasTriggers, deviceHandler, null, null, null)
	}
	subscribe(location, "CoRE Recovery [${app.id}]", recoveryHandler)
	debug "Finished subscribing", -1
}

private subscribeToDevices(condition, triggersOnly, handler, subscriptions, onlySubscriptions, excludeSubscriptions) {
	if (subscriptions == null) {
		subscriptions = [:]
	}
	def result = 0
	if (condition) {
		if (condition.children != null) {
			//we're dealing with a group
			for (child in condition.children) {
				subscribeToDevices(child, triggersOnly, handler, subscriptions, onlySubscriptions, excludeSubscriptions)
			}
		} else {
			if (condition.trg || !triggersOnly) {
				//get the details
				def capability = getCapabilityByDisplay(condition.cap)
				def devices = capability.virtualDevice ? (capability.attribute == "time" ? [] : [capability.virtualDevice]) : settings["condDevices${condition.id}"]
				def attribute = capability.virtualDevice ? capability.attribute : condition.attr
				def attr = getAttributeByName(attribute)
				if (attr && attr.subscribe) {
					attribute = attr.subscribe
				}
				if (capability && (capability.name == "variable") && (!condition.var || !condition.var.startsWith('@'))) {
					//we don't want to subscribe to local variables
					devices = null
				}
				if (devices) {
					for (device in devices) {
						def subscription = "${device.id}-${attribute}"
						if ((excludeSubscriptions == null) || !(excludeSubscriptions[subscription])) {
							//if we're provided with an exclusion list, we don't subscribe to those devices/attributes events
							if ((onlySubscriptions == null) || onlySubscriptions[subscription]) {
								//if we're provided with a restriction list, we use it
								if (!subscriptions[subscription]) {
									subscriptions[subscription] = true //[deviceId: device.id, attribute: attribute]
									if (handler) {
										//we only subscribe to the device if we're provided a handler (not simulating)
										debug "Subscribing to events from $device for attribute $attribute, handler is $handler", null, "trace"
										debug "Subscribing to events from $device for attribute $attribute, handler is $handler"
										subscribe(device, attribute, handler)
										state.deviceSubscriptions = state.deviceSubscriptions ? state.deviceSubscriptions + 1 : 1
										//initialize the cache for the device - this will allow the triggers to work properly on first firing
										state.cache[device.id + "-" + attribute] = [v: device.currentValue(attribute), t: now()]
									}
								}
							}
						}
					}
				} else {
					return
				}
			}
		}
	}
	return subscriptions
}

/******************************************************************************/
/*** CoRE PISTON CONFIGURATION METHODS										***/
/******************************************************************************/

def testIFTTT() {
	//setup our security descriptor
	state.modules["IFTTT"] = [
		key: settings.iftttKey,
		connected: false
	]
	if (settings.iftttKey) {
		//verify the key
		return httpGet("https://maker.ifttt.com/trigger/test/with/key/" + settings.iftttKey) { response ->
			if (response.status == 200) {
				if (response.data == "Congratulations! You've fired the test event")
					state.modules["IFTTT"].connected = true
				return true;
			}
			return false;
		}
	}
	return false
}

def testLIFX() {
	if ((!settings.lifxToken) || (!settings.lifxEnabled)) return false
	//setup our security descriptor
	state.modules["LIFX"] = [
		token: settings.lifxToken,
		connected: false
	]
	if (settings.lifxToken) {
		//verify the key
		def requestParams = [
			uri:  "https://api.lifx.com",
			path: "/v1/scenes",
			headers: [
				"Authorization": "Bearer ${settings.lifxToken}"
			],
			requestContentType: "application/json"
		]
		try {
			return httpGet(requestParams) { response ->
				if (response.status == 200) {
					if (response.data instanceof List) {
						state.modules["LIFX"].connected = true
						def ss = []
						for(scene in response.data) {
							def s = [
								id: scene.uuid,
								name: scene.name
							]
							ss.push(s)
						}
						state.modules["LIFX"].scenes = ss
					}
					return true;
				}
				return false;
			}
		}
		catch(all) {
			return false
		}
	}
	return false
}

//creates a condition (grouped or not)
private createCondition(group) {
	def condition = [:]
	//give the new condition an id
	condition.id = (int) getNextConditionId()
	//initiate the condition type
	if (group) {
		//initiate children
		condition.children = []
		condition.actions = []
	} else {
		condition.type = null
	}
	return condition
}

//creates a condition and adds it to a parent
private createCondition(parentConditionId, group, conditionId = null) {
	def parent = getCondition(parentConditionId)
	if (parent) {
		def condition = createCondition(group)
		if (conditionId != null) condition.id = conditionId
		//preserve the parentId so we can rebuild the app from settings
		condition.parentId = parent ? (int) parent.id : null
		//calculate depth for new condition
		condition.level = (parent.level ? parent.level : 0) + 1
		//add the new condition to its parent, if any
		//set the parent for upwards traversal
		//if (!parent.children) parent = getCondition(0)
		parent.children.push(condition)
		//return the newly created condition
		return condition
	}
	return null
}

//deletes a condition
private deleteCondition(conditionId) {
	def condition = getCondition(conditionId)
	if (condition) {
		def parent = getCondition(condition.parentId)
		if (parent) {
			parent.children.remove(condition);
		}
	}
}

private updateCondition(condition) {
	condition.cap = settings["condCap${condition.id}"]
	condition.dev = []
	condition.sdev = settings["condSubDev${condition.id}"]
	condition.attr = cleanUpAttribute(settings["condAttr${condition.id}"])
	condition.iact = settings["condInteraction${condition.id}"]
	switch (condition.cap) {
		case "Ask Alexa Macro":
			condition.attr = "askAlexaMacro"
			condition.dev.push "location"
			break
		case "EchoSistant Profile":
			condition.attr = "echoSistantProfile"
			condition.dev.push "location"
			break
		case "IFTTT":
			condition.attr = "ifttt"
			condition.dev.push "location"
			break
		case "Time":
		case "Date & Time":
			condition.attr = "time"
			condition.dev.push "time"
			break
		case "Mode":
		case "Location Mode":
			condition.attr = "mode"
			condition.dev.push "location"
			break
		case "Smart Home Monitor":
			condition.attr = "alarmSystemStatus"
			condition.dev.push "location"
			break
		case "CoRE Piston":
		case "Piston":
			condition.attr = "piston"
			condition.dev.push "location"
			break
		case "Routine":
			condition.attr = "routineExecuted"
			condition.dev.push "location"
			break
		case "Variable":
			condition.attr = "variable"
			condition.dev.push "location"
			break
	}
	if (!condition.attr) {
		def cap = getCapabilityByDisplay(condition.cap)
		if (cap && cap.attribute) {
			condition.attr = cap.attribute
			if (cap.virtualDevice) condition.dev.push(cap.virtualDevice)
		}
	}
	def dev
	for (device in settings["condDevices${condition.id}"])
	{
		//save the list of device IDs - we can't have the actual device objects in the state
		dev = device
		condition.dev.push(device.id)
	}
	condition.comp = cleanUpComparison(settings["condComp${condition.id}"])
	condition.var = settings["condVar${condition.id}"]
	condition.dt = settings["condDataType${condition.id}"]
	condition.trg = !!isComparisonOptionTrigger(condition.attr, condition.comp, condition.attr == "variable" ? condition.dt : null, dev)
	condition.mode = condition.trg ? "Any" : (settings["condMode${condition.id}"] ? settings["condMode${condition.id}"] : "Any")
	condition.var1 = settings["condVar${condition.id}#1"]
	condition.dev1 = condition.var1 ? null : settings["condDev${condition.id}#1"] ? getDeviceLabel(settings["condDev${condition.id}#1"]) : null
	condition.attr1 = condition.var1 ? null : settings["condAttr${condition.id}#1"] ? getDeviceLabel(settings["condAttr${condition.id}#1"]) : null
	condition.val1 = (condition.attr != "time") && (condition.var1 || condition.dev1) ? null : settings["condValue${condition.id}#1"]
	condition.var2 = settings["condVar${condition.id}#2"]
	condition.dev2 = condition.var2 ? null : settings["condDev${condition.id}#2"] ? getDeviceLabel(settings["condDev${condition.id}#2"]) : null
	condition.attr2 = condition.var2 ? null : settings["condAttr${condition.id}#2"] ? getDeviceLabel(settings["condAttr${condition.id}#2"]) : null
	condition.val2 = (condition.attr != "time") && (condition.var2 || condition.dev2) ? null : settings["condValue${condition.id}#2"]
	condition.for = settings["condFor${condition.id}"]
	condition.fort = settings["condTime${condition.id}"]
	condition.t1 = settings["condTime${condition.id}#1"]
	condition.t2 = settings["condTime${condition.id}#2"]
	condition.o1 = settings["condOffset${condition.id}#1"]
	condition.o2 = settings["condOffset${condition.id}#2"]
	condition.e = settings["condEvery${condition.id}"]
	condition.e = condition.e ? condition.e : 5
	condition.m = settings["condMinute${condition.id}"]
	//time repeat
	condition.r = settings["condRepeat${condition.id}"]
	condition.re = settings["condRepeatEvery${condition.id}"]
	condition.re = condition.re ? condition.re : 2
	condition.rd = settings["condRepeatDay${condition.id}"]
	condition.rdw = settings["condRepeatDayOfWeek${condition.id}"]
	condition.rm = settings["condRepeatMonth${condition.id}"]

	//time filters
	condition.fmh = settings["condMOH${condition.id}"]
	condition.fhd = settings["condHOD${condition.id}"]
	condition.fdw = settings["condDOW${condition.id}"]
	condition.fdm = settings["condDOM${condition.id}"]
	condition.fwm = settings["condWOM${condition.id}"]
	condition.fmy = settings["condMOY${condition.id}"]
	condition.fy = settings["condY${condition.id}"]

	condition.grp = settings["condGrouping${condition.id}"]
	condition.grp = condition.grp && condition.grp.size() ? condition.grp : "AND"
	condition.not = !!settings["condNegate${condition.id}"]

	//variables
	condition.vd = settings["condVarD${condition.id}"]
	condition.vs = settings["condVarS${condition.id}"]
	condition.vm = settings["condVarM${condition.id}"]
	condition.vn = settings["condVarN${condition.id}"]
	condition.vt = settings["condVarT${condition.id}"]
	condition.vv = settings["condVarV${condition.id}"]
	condition.vf = settings["condVarF${condition.id}"]
	condition.vw = settings["condVarW${condition.id}"]

	condition.it = settings["condImportT${condition.id}"]
	condition.itp = settings["condImportTP${condition.id}"]
	condition.if = settings["condImportF${condition.id}"]
	condition.ifp = settings["condImportFP${condition.id}"]

	condition = cleanUpMap(condition)
	return null
}

//used to get the next id for a condition, action, etc - looks into settings to make sure we're not reusing a previously used id
private getNextConditionId() {
	def nextId = getLastConditionId(state.config.app.conditions) + 1
	def otherNextId = getLastConditionId(state.config.app.otherConditions) + 1
	nextId = nextId > otherNextId ? nextId : otherNextId
	def keys = settings.findAll { it.key.startsWith("condParent") }
	while (keys.find { it.key == "condParent" + nextId }) {
		nextId++
	}
	return (int) nextId
}

//helper function for getNextId
private getLastConditionId(parent) {
	if (!parent) return -1
	def lastId = parent?.id
	for (child in parent.children) {
		def childLastId = getLastConditionId(child)
		lastId = lastId > childLastId ? lastId : childLastId
	}
	return lastId
}

//creates a condition (grouped or not)
private createAction(parentId, onState = true, actionId = null) {
	def action = [:]
	//give the new condition an id
	action.id = (int) actionId == null ? getNextActionId() : actionId
	action.pid = (int) parentId
	action.rs = !!onState
	state.config.app.actions.push(action)
	return action
}

private getNextActionId() {
	def nextId = 1
	for(action in state.config.app.actions) {
		if (action.id > nextId) {
			nextId = action.id + 1
		}
	}
	while (settings.findAll { it.key == "actParent" + nextId }) {
		nextId++
	}
	return (int) nextId
}

private updateAction(action) {
	if (!action) return null
	def id = action.id
	def devices = []
	def usedCapabilities = []
	//did we get any devices? search all capabilities
	for(def capability in capabilities()) {
		if (capability.devices) {
			//only if the capability published any devices - it wouldn't be here otherwise
			def dev = settings["actDev$id#${capability.name}"]
			if (dev && dev.size()) {
				devices = devices + dev
				//add to used capabilities - needed later
				if (!(capability.name in usedCapabilities)) {
					usedCapabilities.push(capability.name)
				}
			}
		}
	}
	action.d = []
	for(device in devices) {
		if (!(device.id in action.d)) {
			action.d.push(device.id)
		}
	}
	action.l = settings["actDev$id#location"]

	//restrictions
	action.rc = settings["actRStateChange$id"]
	action.rs = cast(action.pid > 0 ? (settings["actRState$id"] != null ? settings["actRState$id"] : (action.rs == null ? true : action.rs)) : true, "boolean")
	action.ra = settings["actRAlarm$id"]
	action.rm = settings["actRMode$id"]
	action.rv = settings["actRVariable$id"]
	action.rvc = settings["actRComparison$id"]
	action.rvv = settings["actRValue$id"] != null ? settings["actRValue$id"] : ""
	action.rw = settings["actRDOW$id"]
	action.rtf = settings["actRTimeFrom$id"]
	action.rtfc = settings["actRTimeFromCustom$id"]
	action.rtfo = settings["actRTimeFromOffset$id"]
	action.rtt = settings["actRTimeTo$id"]
	action.rttc = settings["actRTimeToCustom$id"]
	action.rtto = settings["actRTimeToOffset$id"]
	action.rs1 = []
	for (device in settings["actRSwitchOn$id"]) { action.rs1.push(device.id) }
	action.rs0 = []
	for (device in settings["actRSwitchOff$id"]) { action.rs0.push(device.id) }
	action.tos = settings["actTOS$id"]
	action.tcp = settings["actTCP$id"]

	//look for tasks
	action.t = []
	def prefix = "actTask$id#"
	def tasks = settings.findAll{it.key.startsWith(prefix)}
	def ids = []
	//we need to get a list of all existing ids that are used
	for (item in tasks) {
		if (item.value) {
			def tid = item.key.replace(prefix, "")
			if (tid.isInteger()) {
				tid = tid.toInteger()
				def task = [ i: tid + 0 ]
				//get task data
				//get command
				def cmd = settings["$prefix$tid"]
				task.c = cmd
				task.p = []
				task.m = settings["actParamMode$id#$tid"]
				task.d = settings["actParamDOW$id#$tid"]
				def virtual = (cmd && cmd.startsWith(virtualCommandPrefix()))
				def custom = (cmd && cmd.startsWith(customCommandPrefix()))
				cmd = cleanUpCommand(cmd)
				def command = null
				if (virtual) {
					//dealing with a virtual command
					command = getVirtualCommandByDisplay(cmd)
				} else {
					command = getCommandByDisplay(cmd)
				}
				if (command) {
					if (command.name == "setVariable") {
						//setVariable is different, we've got a variable number of parameters...
						//variable name
						task.p.push([i: 0, t: "variable", d: settings["actParam$id#$tid-0"], v: 1])
						//data type
						def dataType = settings["actParam$id#$tid-1"]
						task.p.push([i: 1, t: "text", d: dataType])
						//immediate
						task.p.push([i: 2, t: "bool", d: !!settings["actParam$id#$tid-2"]])
						//formula
						task.p.push([i: 3, t: "text", d: settings["actParam$id#$tid-3"]])
						def i = 4
						while (true) {
							//value
							def val = settings["actParam$id#$tid-$i"]
							def var = settings["actParam$id#$tid-${i + 1}"]
							if ((dataType == "string") && (val == null) && (var == null)) val = ""
							task.p.push([i: i, t: dataType, d: val])
							//variable name
							task.p.push([i: i + 1, t: "text", d: var])
							//variable name
							task.p.push([i: i + 2, t: "text", d: settings["actParam$id#$tid-${i + 2}"]])
							//next operation
							def operation = settings["actParam$id#$tid-${i + 3}"]
							if (!operation) break
							task.p.push([i: i + 3, t: "text", d: operation])
							if (dataType == "time") dataType = "decimal"
							i = i + 4
						}
					} else if (command.parameters) {
						def i = 0
						for (def parameter in command.parameters) {
							def param = parseCommandParameter(parameter)
							if (param) {
								def type = param.type
								def data = settings["actParam$id#$tid-$i"]
								//so ST silently!!! fails if we're having a list and that list contains wrappers (like contacts!)
								if ((data instanceof ArrayList)) {
									def items = []
									for(it in data) {
										items.push("$it")
									}
									data = items
								}
								def var = (command.varEntry == i)
								if (var) {
									task.p.push([i: i, t: type, d: data, v: 1])
								} else {
									task.p.push([i: i, t: type, d: data])
								}
							}
							i++
						}
					}
				} else if (custom) {
					//custom parameters
					def i = 1
					while (true) {
						//value
						def type = settings["actParam$id#$tid-$i"]
						if (type) {
							//parameter type
							task.p.push([i: i, t: "string", d: settings["actParam$id#$tid-$i"]])
							//parameter value
							task.p.push([i: i + 1, t: type, d: settings["actParam$id#$tid-${i + 1}"]])
						} else {
							break
						}
						i += 2
					}

				}
				action.t.push(task)
			}
		}
	}
	//clean up for memory optimization
	action = cleanUpMap(action)
}

private cleanUpActions() {
	for(action in state.config.app.actions) {
		updateAction(action)
	}
	def washer = []
	for(action in state.config.app.actions) {
		if (!((action.d && action.d.size()) || action.l)) {
			washer.push(action)
		}
	}
	for (action in washer) {
		state.config.app.actions.remove(action)
	}
	washer = null

	/*
	def dirty = true
	while (dirty) {
		dirty = false
		for(action in state.config.app.actions) {
			if (!((action.d && action.d.size()) || action.l)) {
				state.config.app.actions.remove(action)
				dirty = true
				break
			}
		}
	}
	*/
}

private listActionDevices(actionId) {
	def devices = []
	//did we get any devices? search all capabilities
	for(def capability in capabilities()) {
		if (capability.devices) {
			//only if the capability published any devices - it wouldn't be here otherwise
			def dev = settings["actDev$actionId#${capability.name}"]
			for (d in dev) {
				if (!(d in devices)) {
					devices.push(d)
				}
			}
		}
	}
	return devices
}

private getActionDescription(action) {
	if (!action) return null
	def devices = (action.l ? ["location"] : listActionDevices(action.id))
	def result = ""
	if (action.rc) {
		result += "® If ${action.pid > 0 ? "condition" : "piston"} state changes...\n"
	}
	if (action.rm) {
		result += "® If mode is ${buildNameList(action.rm, "or")}...\n"
	}
	if (action.ra) {
		result += "® If alarm is ${buildNameList(action.ra, "or")}...\n"
	}
	if (action.rv) {
		result += "® If {${action.rv}} ${action.rvc} ${action.rvv}...\n"
	}
	if (action.rw) {
		result += "® If day is ${buildNameList(action.rw, "or")}...\n"
	}
	if (action.rtf && action.rtt) {
		result += "® If time is between ${action.rtf == "custom time" ? formatTime(action.rtfc) : (action.rtfo ? (action.rtfo < 0 ? "${-action.rtfo} minutes before " : "${action.rtfo} minutes after ") : "") + action.rtf} and ${action.rtt == "custom time" ? formatTime(action.rttc) : (action.rtto ? (action.rtto < 0 ? "${-action.rtto} minutes before " : "${action.rtto} minutes after ") : "") + action.rtt}...\n"
	}
	if (action.rs1) {
		result += "® If each of ${buildDeviceNameList(settings["actRSwitchOn${action.id}"], "and")} is on"
	}
	if (action.rs0) {
		result += "® If each of ${buildDeviceNameList(settings["actRSwitchOff${action.id}"], "and")} is off"
	}
	result += (result ? "\n" : "") + "Using " + buildDeviceNameList(devices, "and")+ "..."
	state.taskIndent = 0
	def tasks = action.t.sort{it.i}
	for (task in tasks) {
		def t = cleanUpCommand(task.c)
		if (task.p && task.p.size()) {
			t += " ["
			def i = 0
			for(param in task.p.sort{ it.i }) {
				t += (i > 0 ? ", " : "") + (param.v ? "{${param.d}}" : "${param.d}")
				i++
			}
			t += "]"

		}
		result += "\n " + getTaskDescription(task, '► ')
	}
	return result
}

def getActionDeviceList(action) {
	if (!action) return null
	def devices = (action.l ? ["location"] : listActionDevices(action.id))
	return buildDeviceNameList(devices, "and")
}

private getTaskDescription(task, prfx = '') {
	if (!task) return "[ERROR]"
	state.taskIndent = state.taskIndent ? state.taskIndent : 0
	def virtual = (task.c && task.c.startsWith(virtualCommandPrefix()))
	def custom = (task.c && task.c.startsWith(customCommandPrefix()))
	def command = cleanUpCommand(task.c)

	def selfIndent = 0
	def indent = 0

	def result = ""
	if (custom) {
		result = task.c.replace(customCommandSuffix(), "") + "("
		for (int i=0; i < task.p.size() / 2; i++) {
			if (i > 0) result += ", "
			int j = i * 2 + 1
			if (task.p[j].t == "string") {
				result += "\"${task.p[j].d}\""
			} else {
				result += "${task.p[j].d}"
			}
		}
		result = result + ")"
	} else {
		def cmd = (virtual ? getVirtualCommandByDisplay(command) : getCommandByDisplay(command))
		if (!cmd) {
			result = "[ERROR]"
		} else {
			indent = cmd.indent ? cmd.indent : 0
			selfIndent = cmd.selfIndent ? cmd.selfIndent : 0
			if (cmd.name == "setVariable") {
				if (task.p.size() < 7) return "[ERROR]"
				def name = task.p[0].d
				def dataType = task.p[1].d
				def immediate = !!task.p[2].d
				if (!name || !dataType) return "[ERROR]"
				result = "${immediate ? "Immediately set" : "Set"} $dataType variable {$name} = "
				def i = 4
				def grouping = false
				def groupingUnit = ""
				while (true) {
					def value = task.p[i].d
					//null strings are really blanks
					if ((dataType == "string") && (value == null)) value = ""
					if ((dataType == "time") && (i == 4) && (value != null)) value = formatTime(value)
					def variable = value != null ? (dataType == "string" ? "\"$value\"" : "$value") : "${task.p[i + 1].d}"
					def unit = (dataType == "time" ? task.p[i + 2].d : null)
					def operation = task.p.size() > i + 3 ? "${task.p[i + 3].d} ".tokenize(" ")[0] : null
					def needsGrouping = (operation == "*") || (operation == "÷") || (operation == "AND")
					if (needsGrouping) {
						//these operations require grouping i.e. (a * b * c) seconds
						if (!grouping) {
							grouping = true
							groupingUnit = unit
							result += "("
						}
					}
					//add the value/variable
					result += variable + (!grouping && unit ? " $unit" : "")
					if (grouping && !needsGrouping) {
						//these operations do NOT require grouping
						grouping = false
						result += ")${groupingUnit ? " $groupingUnit" : ""}"
					}
					if (!operation) break
					result += " $operation "
					i += 4
				}
			} else if (cmd.name == "setColor") {
				result = "Set color to "
				if (task.p[0].d) {
					result = result + "\"${task.p[0].d}\""
				} else if (task.p[1].d) {
					result = result + "RGB(${task.p[1].d})"
				} else {
					result = result + "HSL(${task.p[2].d}°, ${task.p[3].d}%, ${task.p[4].d}%)"
				}
			} else {
				result = formatMessage(cmd.description ?: cmd.display, task.p)
			}
		}
	}
	def currentIndent = state.taskIndent + selfIndent
	def prefix = "".padLeft(currentIndent > 0 ? currentIndent * 3 : 0, "│  ")
	state.taskIndent = state.taskIndent + indent
	return prefix + (prfx ?: '') + result + (task.m && task.m.size() ? " (only for ${buildNameList(task.m, "or")})" : "") + (task.d && task.d.size() ? " (only on ${buildNameList(task.d, "or")})" : "")
}

/******************************************************************************/
/*** ENTRY AND EXIT POINT HANDLERS											***/
/******************************************************************************/

def deviceHandler(evt) {
	entryPoint()
	if (!preAuthorizeEvent(evt)) return
	//executes whenever a device in the primary if block has an event
	//starting primary IF block evaluation
	def perf = now()
	debug "Received a primary block device event", 1, "trace"
	broadcastEvent(evt, true, false)
	//process tasks
	processTasks()
	exitPoint(now() - perf)
	perf = now() - perf
	debug "Piston done in ${perf}ms", -1, "trace"
}

def latchingDeviceHandler(evt) {
	entryPoint()
	if (!preAuthorizeEvent(evt)) return
	//executes whenever a device in the primary if block has an event
	//starting primary IF block evaluation
	def perf = now()
	debug "Received a secondary block device event", 1, "trace"
	broadcastEvent(evt, false, true)
	//process tasks
	processTasks()
	exitPoint(now() - perf)
	perf = now() - perf
	debug "Piston done in ${perf}ms", -1, "trace"
}

def bothDeviceHandler(evt) {
	entryPoint()
	if (!preAuthorizeEvent(evt)) return
	//executes whenever a common use device has an event
	//broadcast to both IF blocks
	def perf = now()
	debug "Received a dual block device event", 1, "trace"
	broadcastEvent(evt, true, true)
	//process tasks
	processTasks()
	exitPoint(now() - perf)
	perf = now() - perf
	debug "Piston done in ${perf}ms", -1, "trace"
}

def timeHandler() {
	entryPoint()
	//executes whenever a device in the primary if block has an event
	//starting primary IF block evaluation
	def perf = now()
	debug "Received a time event", 1, "trace"
	processTasks()
	exitPoint(now() - perf)
	perf = now() - perf
	debug "Piston done in ${perf}ms", -1, "trace"
}

def recoveryHandler(evt = null, showWarning = true) {
	if (evt) {
		if (evt.jsonData && evt.jsonData.rebuild) {
			debug "Received a REBUILD request...", null, "info"
			rebuildPiston(true)
			return
		} else {
			debug "Received a RECOVER request...", null, "info"
		}
	}
	entryPoint()
	//executes whenever a device in the primary if block has an event
	//starting primary IF block evaluation
	def perf = now()
	debug "Received a recovery request", 1, "trace"
	if (!evt && showWarning) debug "CAUTION: Received a recovery event", 1, "warn"
	//reset markers for all tasks, the owner of the task probably crashed :)
	def tasks = atomicState.tasks
	for(task in tasks.findAll{ it.value.marker != null }) {
		task.value.marker = null
	}
	atomicState.tasks = tasks
	processTasks()
	exitPoint(now() - perf)
	perf = now() - perf
	debug "Piston done in ${perf}ms", -1, "trace"
}

def executeHandler(data = null) {
	entryPoint()
	//executes whenever a device in the primary if block has an event
	//starting primary IF block evaluation
	def perf = now()
	if (data instanceof Map) {
		for(item in data) {
			setVariable(item.key, item.value)
		}
	}
	debug "Received an execute request", 1, "trace"
	broadcastEvent([name: "execute", date: new Date(), deviceId: "time", conditionId: null], true, false)
	processTasks()
	exitPoint(now() - perf)
	perf = now() - perf
	debug "Piston done in ${perf}ms", -1, "trace"
	return state.currentState
}

private preAuthorizeEvent(evt) {
	if (!(evt.name in ["piston", "routineExecuted", "askAlexaMacro", "echoSistantProfile", "ifttt", "variable"])) return true
	//prevent one piston from retriggering itself
	if (evt && (evt.name == "piston") && (evt.value == app.label)) return false
	state.filterEvent = true
	if (evt.name == "variable") {
		withEachCondition(state.app.conditions, "preAuthorizeTrigger", evt)
		if (state.filterEvent) withEachCondition(state.app.otherConditions, "preAuthorizeTrigger", evt)
	} else {
		withEachTrigger(state.app.conditions, "preAuthorizeTrigger", evt)
		if (state.filterEvent) withEachTrigger(state.app.otherConditions, "preAuthorizeTrigger", evt)
	}
	if (state.filterEvent) debug "Received a '${evt.name}' event, but no trigger matches it, so we're not going to execute at this time."
	return !state.filterEvent
}

private preAuthorizeTrigger(condition, evt) {
	if (!state.filterEvent) return
	def attribute = evt.name
	def value = evt.value
	switch (evt.name) {
		case "routineExecuted":
			value = evt.displayName
			break
	}
	if ((condition.attr == attribute) && (attribute == "variable" ? condition.var == value : ((condition.var1 ? getVariable(condition.var1) : condition.val1) == value))) state.filterEvent = false
	return
}

private entryPoint() {
	//initialize whenever app runs
	//use the "app" version throughout
	state.run = "app"
	state.sim = null
	state.debugLevel = 0
	state.globalVars = [:]
	state.tasker = []
	//state.tasker = state.tasker ? state.tasker : []
}

private exitPoint(milliseconds) {
	def perf = now()
	def appData = state.run == "config" ? state.config.app : state.app
	def runStats = atomicState.runStats
	if (runStats == null) runStats = [:]
	runStats.executionSince = runStats.executionSince ? runStats.executionSince : now()
	runStats.executionCount = runStats.executionCount ? runStats.executionCount + 1 : 1
	runStats.executionTime = runStats.executionTime ? runStats.executionTime + milliseconds : milliseconds
	runStats.minExecutionTime = runStats.minExecutionTime && runStats.minExecutionTime < milliseconds ? runStats.minExecutionTime : milliseconds
	runStats.maxExecutionTime = runStats.maxExecutionTime && runStats.maxExecutionTime > milliseconds ? runStats.maxExecutionTime : milliseconds
	runStats.lastExecutionTime = milliseconds

	def lastEvent = state.lastEvent
	if (lastEvent && lastEvent.delay) {
		runStats.eventDelay = runStats.eventDelay ? runStats.eventDelay + lastEvent.delay : lastEvent.delay
		runStats.minEventDelay = runStats.minEventDelay && runStats.minEventDelay < lastEvent.delay ? runStats.minEventDelay : lastEvent.delay
		runStats.maxEventDelay = runStats.maxEventDelay && runStats.maxEventDelay > lastEvent.delay ? runStats.maxEventDelay : lastEvent.delay
		runStats.lastEventDelay = lastEvent.delay
	}
	setVariable("\$previousEventExecutionTime", milliseconds, true)
	state.lastExecutionTime = milliseconds

	try {
		state.nextScheduledTime = atomicState.nextScheduledTime
		parent.onChildExitPoint(app, lastEvent, milliseconds, state.nextScheduledTime, getSummary())
	} catch(e) {
		debug "ERROR: Could not update parent app: ", null, "error", e
	}
	atomicState.runStats = runStats

	if (lastEvent && lastEvent.event) {
		if (lastEvent.event.name != "piston") {
			sendLocationEvent(name: "piston", value: "${app.label}", displayed: true, linkText: "CoRE/${app.label}", isStateChange: true, descriptionText: "${appData.mode} piston executed in ${milliseconds}ms", data: [app: "CoRE", state: state.currentState, restricted: state.restricted, executionTime: milliseconds, event: lastEvent])
		}
	}

	//give a chance to variable events
	publishVariables()

	//save all atomic states to state
	//to avoid race conditions
	state.cache = atomicState.cache
	state.tasks = atomicState.tasks
	state.stateStore = atomicState.stateStore
	state.runStats = atomicState.runStats
	state.currentState = atomicState.currentState
	state.currentStateSince = atomicState.currentStateSince
	state.temp = null
	state.sim = null

}

/******************************************************************************/
/*** EVENT MANAGEMENT FUNCTIONS												***/
/******************************************************************************/

private broadcastEvent(evt, primary, secondary) {
	//filter duplicate events and broadcast event to proper IF blocks
	def perf = now()
	def delay = perf - evt.date.getTime()
	def app = state.run == "config" ? state.config.app : state.app
	debug "Processing event ${evt.name}${evt.device ? " for device ${evt.device}" : ""}${evt.deviceId ? " with id ${evt.deviceId}" : ""}${evt.value ? ", value ${evt.value}" : ""}, generated on ${evt.date}, about ${delay}ms ago (${version()})", 1, "trace"
	def allowed = true
	def restriction
	def initialState = atomicState.currentState
	def initialStateSince = atomicState.currentStateSince
	if (evt && app.restrictions) {
		//check restrictions
		restriction = checkPistonRestriction()
		allowed = (restriction == null)
	}
	//save previous event
	setVariable("\$previousEventReceived", getVariable("\$currentEventReceived"), true)
	setVariable("\$previousEventDevice", getVariable("\$currentEventDevice"), true)
	setVariable("\$previousEventDeviceIndex", getVariable("\$currentEventDeviceIndex"), true)
	setVariable("\$previousEventDevicePhysical", getVariable("\$currentEventDevicePhysical"), true)
	setVariable("\$previousEventAttribute", getVariable("\$currentEventAttribute"), true)
	setVariable("\$previousEventValue", getVariable("\$currentEventValue"), true)
	setVariable("\$previousEventDate", getVariable("\$currentEventDate"), true)
	setVariable("\$previousEventDelay", getVariable("\$currentEventDelay"), true)
	def lastEvent = [
		event: [
			device: evt.device ? "${evt.device}" : evt.deviceId,
			name: evt.name,
			value: evt.value,
			date: evt.date
		],
		delay: delay
	]
	state.lastEvent = lastEvent
	setVariable("\$currentEventReceived", perf, true)
	setVariable("\$currentEventDevice", lastEvent.event.device, true)
	setVariable("\$currentEventDeviceIndex", 0, true)
	setVariable("\$currentEventDevicePhysical", 0, true)
	setVariable("\$currentEventAttribute", lastEvent.event.name, true)
	setVariable("\$currentEventValue", lastEvent.event.value, true)
	setVariable("\$currentEventDate", lastEvent.event.date && lastEvent.event.date instanceof Date ? lastEvent.event.date.time : null, true)
	setVariable("\$currentEventDelay", lastEvent.delay, true)
	if (!(evt.name in ["askAlexaMacro", "echoSistantProfile", "ifttt", "piston", "routineExecuted", "variable", "time"])) {
		def cache = atomicState.cache
		cache = cache ? cache : [:]
		def deviceId = evt.deviceId ? evt.deviceId : location.id
		def cachedValue = cache[deviceId + '-' + evt.name]
		def eventTime = evt.date.getTime()
		cache[deviceId + '-' + evt.name] = [o: cachedValue ? cachedValue.v : null, v: evt.value, q: cachedValue ? cachedValue.p : null, p: !!evt.physical, t: eventTime ]
		if (evt.name == "threeAxis") {
			cachedValue = cache[deviceId + '-orientation']
			cache[deviceId + '-orientation'] = [o: cachedValue ? cachedValue.v : null, v: getThreeAxisOrientation(evt.xyzValue), q: cachedValue ? cachedValue.p : null, p: !!evt.physical, t: eventTime ]
		}
		atomicState.cache = cache
		state.cache = cache
		if (cachedValue) {
			if ((cachedValue.v == evt.value) && (!evt.jsonData) && (/*(cachedValue.v instanceof String) || */(eventTime < cachedValue.t) || (cachedValue.t + 1000 > eventTime))) {
				//duplicate event
				debug "WARNING: Received duplicate event for device ${evt.device}, attribute ${evt.name}='${evt.value}', ignoring...", null, "warn"
				evt = null
			}
		}
	}
	if (allowed) {
		try {
			resetConditionState(app.conditions)
			resetConditionState(app.otherConditions)
			if (evt) {
				//broadcast to primary IF block
				def result1 = null
				def result2 = null
				//some piston modes require evaluation of secondary conditions regardless of eligibility - we use force then
				def force = false
				def mode = app.mode
				switch (mode) {
					case "And-If":
					case "Or-If":
					case "Latching":
						//these three modes always evaluate both blocks
						primary = true
						secondary = true
						force = true
						break
					case "Do":
						primary = false
						secondary = false
						force = false
						result1 = false
						result2 = false
						break
				}
				//override eligibility concerns when dealing with Follow-Up pistons, or when dealing with "execute" and "simulate" events
				force = force || app.mode == "Follow-Up" || (evt && evt.name in ["execute", "simulate", "time"])
				if (primary) {
					result1 = !!evaluateConditionSet(evt, true, force)
					state.lastPrimaryEvaluationResult = result1
					state.lastPrimaryEvaluationDate = now()
					def msg = "Primary IF block evaluation result is $result1"
					if (state.sim) state.sim.evals.push(msg)
					debug msg

					switch (mode) {
						case "Then-If":
							//execute the secondary branch if the primary one is true
							secondary = result1
							force = true
							break
						case "Else-If":
							//execute the second branch if the primary one is false
							secondary = !result1
							force = true
							break
					}
				}

				//broadcast to secondary IF block
				if (secondary) {
					result2 = !!evaluateConditionSet(evt, false, force)
					state.lastSecondaryEvaluationResult = result2
					state.lastSecondaryEvaluationDate = now()
					def msg = "Secondary IF block evaluation result is $result2"
					if (state.sim) state.sim.evals.push(msg)
					debug msg
				}
				def currentState = initialState
				def currentStateSince = initialStateSince

				def stateMsg = null

				switch (mode) {
					case "Latching":
						if (initialState in [null, false]) {
							if (result1) {
								//flip on
								currentState = true
								currentStateSince = now()
								stateMsg = "♦ Latching Piston changed state to true ♦"
							}
						}
						if (initialState in [null, true]) {
							if (result2) {
								//flip off
								currentState = false
								currentStateSince = now()
								stateMsg = "♦ Latching Piston changed state to false ♦"
							}
						}
						break
					case "Do":
						currentState = false
						currentStateSince = now()
						stateMsg = "♦ $mode Piston changed state to $result1 ♦"
						break
					case "Basic":
					case "Simple":
					case "Follow-Up":
						result2 = !result1
						if (initialState != result1) {
							currentState = result1
							currentStateSince = now()
							stateMsg = "♦ $mode Piston changed state to $result1 ♦"
						}
						break
					case "And-If":
						def newState = result1 && result2
						if (initialState != newState) {
							currentState = newState
							currentStateSince = now()
							stateMsg = "♦ And-If Piston changed state to $newState ♦"
						}
						break
					case "Or-If":
						def newState = result1 || result2
						if (initialState != newState) {
							currentState = newState
							currentStateSince = now()
							stateMsg = "♦ Or-If Piston changed state to $newState ♦"
						}
						break
					case "Then-If":
						def newState = result1 && result2
						if (initialState != newState) {
							currentState = newState
							currentStateSince = now()
							stateMsg = "♦ Then-If Piston changed state to $newState ♦"
						}
						break
					case "Else-If":
						def newState = result1 || result2
						if (initialState != newState) {
							currentState = newState
							currentStateSince = now()
							stateMsg = "♦ Else-If Piston changed state to $newState ♦"
						}
						break
				}
				if (stateMsg) {
					if (state.sim) state.sim.evals.push stateMsg
					debug stateMsg, null, "info"
				}
				def stateChanged = false
				if (currentState != initialState) {
					stateChanged = true
					//we have a state change
					setVariable("\$previousState", initialState, true)
					setVariable("\$previousStateSince", initialStateSince, true)
					setVariable("\$previousStateDuration", initialStateSince && currentStateSince ? currentStateSince - initialStateSince : null, true)
					setVariable("\$currentState", currentState, true)
					setVariable("\$currentStateSince", currentStateSince, true)
					//new state
					atomicState.currentState = currentState
					atomicState.currentStateSince = currentStateSince
					state.currentState = currentState
					state.currentStateSince = currentStateSince
					//resume all tasks that are waiting for a state change
					cancelTasks(currentState)
					resumeTasks(currentState)
				}
				//execute the DO EVERY TIME actions
				if (mode != "Do") {
					if (result1) scheduleActions(0, stateChanged)
					if (result2) scheduleActions(-1, stateChanged)
				}
				if (!(mode in ["Basic", "Latching"]) && (!currentState)) {
					//execute the else branch
					scheduleActions(-2, stateChanged)
				}
			}
		} catch(e) {
			debug "ERROR: An error occurred while processing event $evt: ", null, "error", e
		}
	} else {
		def msg = "Piston evaluation was prevented by ${restriction}."
		if (state.sim) state.sim.evals.push(msg)
		debug msg, null, "trace"
	}
	perf = now() - perf
	if (evt) debug "Event processing took ${perf}ms", -1, "trace"
}

private checkPistonRestriction() {
	def restriction
	def app = state.run == "config" ? state.config.app : state.app

	if (app.restrictions.m && app.restrictions.m.size() && !(location.mode in app.restrictions.m)) {
		restriction = "a mode mismatch"
	} else if (app.restrictions.a && app.restrictions.a.size() && !(getAlarmSystemStatus() in app.restrictions.a)) {
		restriction = "an alarm status mismatch"
	} else if (app.restrictions.v && !(checkVariableCondition(app.restrictions.v, app.restrictions.vc, app.restrictions.vv))) {
		restriction = "variable condition {${app.restrictions.v}} ${app.restrictions.vc} '${app.restrictions.vv}'"
	} else if (app.restrictions.w && app.restrictions.w.size() && !(getDayOfWeekName() in app.restrictions.w)) {
		restriction = "a day of week mismatch"
	} else if (app.restrictions.tf && app.restrictions.tt && !(checkTimeCondition(app.restrictions.tf, app.restrictions.tfc, app.restrictions.tfo, app.restrictions.tt, app.restrictions.ttc, app.restrictions.tto))) {
		restriction = "a time of day mismatch"
	} else {
		if (settings["restrictionSwitchOn"]) {
			for(sw in settings["restrictionSwitchOn"]) {
				if (sw.currentValue("switch") != "on") {
					restriction = "switch ${sw} being ${sw.currentValue("switch")}"
					break
				}
			}
		}
		if (!restriction && settings["restrictionSwitchOff"]) {
			for(sw in settings["restrictionSwitchOff"]) {
				if (sw.currentValue("switch") != "off") {
					restriction = "switch ${sw} being ${sw.currentValue("switch")}"
					break
				}
			}
		}
	}
	return restriction
}

private checkEventEligibility(condition, evt) {
	//we have a quad-state result
	// -2 means we're using triggers and the event does not match any of the used triggers
	// -1 means we're using conditions only and the event does not match any of the used conditions
	// 1 means we're using conditions only and the event does match at least one of the used conditions
	// 2 means we're using triggers and the event does match at least one of the used triggers
	// any positive value means the event is eligible for evaluation
	def result = -1 //assuming conditions only, no match
	if (condition) {
		if (condition.children != null) {
			//we're dealing with a group
			for (child in condition.children) {
				def v = checkEventEligibility(child, evt)
				switch (v) {
					case -2:
						result = v
						break
					case -1:
						break
					case  1:
						if (result == -1) {
							result = v
						}
						break
					case  2:
						//if we already found a matching trigger, we're out
						return v
				}
			}
		} else {
			if (condition.trg) {
				if (result < 2) {
					//if we haven't already found a trigger
					result = -2 // we are using triggers
				}
			}
			for (deviceId in condition.dev) {
				if ((evt.deviceId ? evt.deviceId : "location" == deviceId) && (evt.name == (condition.attr in ["orientation", "axisX", "axisY", "axisZ"] ? "threeAxis" : condition.attr))) {
					if (condition.trg) {
						//we found a trigger that matches the event, exit immediately
						return 2
					} else {
						if (result == -1) {
							//we found a condition that matches the event, still looking for triggers though
							result = 1
						}
					}
				}
			}
		}
	}
	return result
}

/******************************************************************************/
/*** CONDITION EVALUATION FUNCTIONS											***/
/******************************************************************************/

private evaluateConditionSet(evt, primary, force = false) {
	//executes whenever a device in the primary or secondary if block has an event
	def perf = now()
	//debug "Event received by the ${primary ? "primary" : "secondary"} IF block evaluation for device ${evt.device}, attribute ${evt.name}='${evt.value}', isStateChange=${evt.isStateChange()}, currentValue=${evt.device.currentValue(evt.name)}, determining eligibility"
	//check for triggers - if the primary IF block has triggers and the event is not related to any trigger
	//then we don't want to evaluate anything, as only triggers should be executed
	//this check ensures that an event that is used in both blocks, but as different types, one as a trigger
	//and one as a condition do not interfere with each other
	def app = state.run == "config" ? state.config.app : state.app
	//reset last condition state
	def eligibilityStatus = force ? 1 : checkEventEligibility(primary ? app.conditions: app.otherConditions , evt)
	def evaluation = null
	if (!force) {
		debug "Event eligibility for the ${primary ? "primary" : "secondary"} IF block is $eligibilityStatus  - ${eligibilityStatus > 0 ? "ELIGIBLE" : "INELIGIBLE"} (" + (eligibilityStatus == 2 ? "triggers required, event is a trigger" : (eligibilityStatus == 1 ? "triggers not required, event is a condition" : (eligibilityStatus == -2 ? "triggers required, but event is a condition" : "something is messed up"))) + ")"
	}
	if (eligibilityStatus > 0) {
		evaluation = evaluateCondition(primary ? app.conditions: app.otherConditions, evt)
	} else {
		//ignore the event
	}
	perf = now() - perf
	if (evaluation != null) {
		if (primary) {
			app.conditions.eval = evaluation
			app.conditions.state = evaluation
		} else {
			app.otherConditions.eval = evaluation
			app.otherConditions.state = evaluation
		}
	}
	return evaluation
}

private resetConditionState(condition) {
	if (!condition) return
	condition.eval = null
	if (condition.children) {
		for (cond in condition.children) resetConditionState(cond)
	}
}

private evaluateCondition(condition, evt = null) {
	try {
		//evaluates a condition
		def perf = now()
		def result = false
		if (condition.children == null) {
			//we evaluate a real condition here
			//several types of conditions, device, mode, SMH, time, etc.
			if (condition.attr == "time") {
				result = evaluateTimeCondition(condition, evt)
			} else {
				result = evaluateDeviceCondition(condition, evt)
			}
		} else {
			//we evaluate a group
			result = (condition.grp in ["AND", "THEN IF", "ELSE IF", "FOLLOWED BY"]) && (condition.children.size()) //we need to start with a true when doing AND or with a false when doing OR/XOR
			def i = 0
			def lastChild = condition.children.size() - 1
			def followedBy = (condition.grp == "FOLLOWED BY")
			def resetLadder = true
			for (child in condition.children.sort { it.id }) {
				def interrupt = false
				//evaluate the child
				//if we have a follwed by, we skip all conditions that are already true, step ladder...
				if (!followedBy || !child.state) {
					def subResult = evaluateCondition(child, evt)
					//apply it to the composite result
					switch (condition.grp) {
						case "AND":
							result = result && subResult
							break
						case "OR":
							result = result || subResult
							break
						case "XOR":
							result = result ^ subResult
							break
						case "THEN IF":
							result = result && subResult
							interrupt = !result
							break
						case "ELSE IF":
							result = subResult
							interrupt = result
							break
						case "FOLLOWED BY":
							//we're true when all children are true
							result = subResult && (i ==  lastChild)
							resetLadder = !subResult
							interrupt = true
							break
					}
				}
				i += 1
				if (interrupt) break
			}

			if (followedBy && (result || resetLadder)) {
				//we either completed the ladder or failed miserably, so let's reset it
				for (child in condition.children) child.state = false
			}
		}
		//apply the NOT, if needed
		result = condition.not ? !result : result
		def oldState = condition.state
		condition.eval = result
		condition.state = result

		//store variables (only if evt is available, i.e. not simulating)
		if (evt) {
			if (condition.vd) setVariable(condition.vd, now())
			if (condition.vs) setVariable(condition.vs, result)
			if (condition.vt && result) setVariable(condition.vt, evt.date.getTime())
			if (condition.vv && result) setVariable(condition.vv, evt.value)
			if (condition.vf && !result) setVariable(condition.vf, evt.date.getTime())
			if (condition.vw && !result) setVariable(condition.vw, evt.value)
			if (condition.it && result && evt.jsonData) {
				def prefix = condition.itp ?: ""
				if (evt.jsonData instanceof Map) {
					importVariables(evt.jsonData, prefix)
				}
			}
			if (condition.if && !result && evt.jsonData) {
				def prefix = condition.ifp ?: ""
				if (evt.jsonData instanceof Map) {
					importVariables(evt.jsonData, prefix)
				}
			}
			if (condition.id > 0) {
				if (oldState != result) {
					//cancel all actions that need to be canceled on condition state change
					unscheduleActions(condition.id)
				}
				scheduleActions(condition.id, oldState != result, result)
			}
		}
		perf = now() - perf
		return result
	} catch(e) {
		debug "ERROR: Error evaluating condition: ", null, "error", e
	}
	return false
}

private evaluateDeviceCondition(condition, evt) {
	//evaluates a condition
	//we need true when dealing with All
	def mode = condition.mode == "All" ? "All" : "Any"
	def result =  mode == "All" ? true : false
	def currentValue = null

	//get list of devices
	def devices = settings["condDevices${condition.id}"]
	def eventDeviceId = evt && evt.deviceId ? evt.deviceId : location.id
	def virtualCurrentValue = null
	def attribute = condition.attr
	switch (condition.cap) {
		case "Ask Alexa Macro":
			devices = [location]
			virtualCurrentValue = evt ? evt.value : "<<<unknown piston>>>"
			attribute = "askAlexaMacro"
			break
		case "EchoSistant Profile":
			devices = [location]
			virtualCurrentValue = evt ? evt.value : "<<<unknown piston>>>"
			attribute = "echoSistantProfile"
			break
		case "IFTTT":
			devices = [location]
			virtualCurrentValue = evt ? evt.value : "<<<unknown IFTTT event>>>"
			attribute = "ifttt"
			break
		case "Mode":
		case "Location Mode":
			devices = [location]
			virtualCurrentValue = location.mode
			attribute = "mode"
			break
		case "Smart Home Monitor":
			devices = [location]
			virtualCurrentValue = getAlarmSystemStatus()
			attribute = "alarmSystemStatus"
			break
		case "CoRE Piston":
		case "Piston":
			devices = [location]
			virtualCurrentValue = evt ? evt.value : "<<<unknown piston>>>"
			attribute = "piston"
			break
		case "Routine":
			devices = [location]
			virtualCurrentValue = evt ? evt.displayName : "<<<unknown routine>>>"
			attribute = "routineExecuted"
			break
		case "Variable":
			devices = [location]
			virtualCurrentValue = getVariable(condition.var)
			attribute = "variable"
			break
	}

	if (!devices) {
		//something went wrong
		return false
	}
	def attr = getAttributeByName(attribute)
	//get capability if the attribute suggests one
	def capability = attr && attr.capability ? getCapabilityByName(attr.capability) : null
	def hasSubDevices = false
	def matchesSubDevice = false
	if (evt && capability && capability.count && capability.data) {
		//at this point we won't evaluate this condition unless we have the right sub device below
		hasSubDevices = true
		def idx = cast(evt.jsonData ? evt.jsonData[capability.data] : 0, "number")
		//if button index is 0, make it 1
		if ((attr.name == "button") && (idx == 0)) idx = 1
		setVariable("\$currentEventDeviceIndex", idx, true)
		def subDeviceId = "#$idx".trim()
		def subDevices = condition.sdev ?: []
		if (subDeviceId == "#0") subDeviceId = "(none)"
		if (subDevices && subDevices.size()) {
			//are we expecting that button?
			//subDeviceId in subDevices didn't seem to work?!
			for(subDevice in subDevices) {
				if (subDevice == subDeviceId) {
					matchesSubDevice = true
					break
				}
			}
		} else {
			matchesSubDevice = true
		}
	}

	//is this a momentary event?
	def momentary = attr ? !!attr.momentary : false
	def physical = false
	def oldPhysical = false
	//if we're dealing with a momentary capability, we can only expect one of the devices to be true at any time
	if (momentary) {
		mode = "Any"
	}

	//matching devices list
	def vm = []
	//non-matching devices list
	def vn = []
	//the real deal goes here
	for (device in devices) {
		def comp = getComparisonOption(attribute, condition.comp, (attribute == "variable" ? condition.dt : null), device)
		if (comp) {
			//if event is about the same device/attribute, use the event's value as the current value, otherwise, fetch the current value from the device
			def deviceResult = false
			def ownsEvent = evt && (eventDeviceId == device.id) && ((evt.name == attribute) || ((evt.name == "time") && (condition.id == evt.conditionId)) || ((evt.name == "threeAxis") && (attribute == "orientation")))
			if (ownsEvent && (evt.name == "time") && (condition.id == evt.conditionId)) {
				//stays trigger, we need to use the current device value
				virtualCurrentValue = device.currentValue(attribute)
			}

			def oldValue = null
			def oldValueSince = null
			if (evt && !(evt.name in ["askAlexaMacro", "echoSistantProfile", "ifttt", "piston", "routineExecuted", "variable", "time"])) {
				def cache = state.cache ? state.cache : [:]
				def cachedValue = cache[device.id + "-" + attribute]
				if (cachedValue) {
					physical = cachedValue.p
					oldPhysical = cachedValue.q
					oldValue = cachedValue.o
					oldValueSince = cachedValue.t
				}
				//get the physical from the event, if that's related to this trigger
				if (ownsEvent) {
					physical = !!evt.physical
					setVariable("\$currentEventDevicePhysical", physical, true)

				}
			}

			//if we have a variable event and we're at a variable condition, let's get the old value
			if (evt && (evt.name == "variable") && (attr.name == "variable") && (evt.jsonData) && (evt.value == condition.var)) {
				oldValue = evt.jsonData.oldValue
			}
			def type = attr.name == "variable" ? (condition.dt ? condition.dt : attr.type) : attr.type
			//if we're dealing with an owned event, use that event's value
			//if we're dealing with a virtual device, get the virtual value
			oldValue = cast(oldValue, type)

			switch (attribute) {
				case "orientation":
					virtualCurrentValue = evt && ownsEvent ? evt.xyzValue : device.currentValue("threeAxis")
					setVariable("\$currentEventDeviceIndex", getThreeAxisOrientation(virtualCurrentValue, true), true)
					break
				case "axisX":
					virtualCurrentValue = evt && ownsEvent ? evt.xyzValue?.x : device.currentValue("threeAxis").x
					break
				case "axisY":
					virtualCurrentValue = evt && ownsEvent ? evt.xyzValue?.y : device.currentValue("threeAxis").y
					break
				case "axisZ":
					virtualCurrentValue = evt && ownsEvent ? evt.xyzValue?.z : device.currentValue("threeAxis").z
					break
			}

			currentValue = cast(virtualCurrentValue != null ? virtualCurrentValue : (evt && ownsEvent ? evt.value : device.currentValue(attribute)), type)
			def value1
			def offset1
			def value2
			def offset2
			if (comp.parameters > 0) {
				value1 = cast(condition.var1 ? getVariable(condition.var1) : (condition.dev1 && settings["condDev${condition.id}#1"] ? settings["condDev${condition.id}#1"].currentValue(condition.attr1 ? condition.attr1 : attribute) : condition.val1), type)
				offset1 = cast(condition.var1 || condition.dev1 ? condition.o1 : 0, type)
				if (comp.parameters > 1) {
					value2 = cast(condition.var2 ? getVariable(condition.var2) : (condition.dev2 && settings["condDev${condition.id}#2"] ? settings["condDev${condition.id}#2"].currentValue(condition.attr2 ? condition.attr2 : attribute) : condition.val2), type)
					offset2 = cast(condition.var1 || condition.dev1 ? condition.o2 : 0, type)
				}
			}
			switch (type) {
				case "number":
				case "decimal":
					if (comp.parameters > 0) {
						value1 += cast(condition.var1 || condition.dev1 ? condition.o1 : 0, type)
						if (comp.parameters > 1) {
							value2 += cast(condition.var1 || condition.dev1 ? condition.o2 : 0, type)
						}
					}
					break
			}

			def interactionMatched = true
			if (attr.interactive) {
				interactionMatched = (physical && (condition.iact != "Programmatic")) || (!physical && (condition.iact != "Physical"))
				if (!interactionMatched) {
					debug "Condition evaluation interrupted due to interaction method mismatch. Event is ${evt.physical ? "physical" : "programmatic"}, expecting ${condition.iact}."
				}
			}
			if ((condition.trg && !ownsEvent) || !interactionMatched) {
				//all triggers should own the event, otherwise be false
				deviceResult = false
			} else {
				def function = "eval_" + (condition.trg ? "trg" : "cond") + "_" + sanitizeCommandName(condition.comp)
				//if we have a momentary capability and the event is not owned, there's no need to evaluate the function
				//also, if there are subdevices and the one we're looking for does not match, no need to evaluate the function either
				if ((momentary && !ownsEvent) || (hasSubDevices && !matchesSubDevice)) {
					deviceResult = false
					def msg = "${deviceResult ? "♣" : "♠"} Evaluation for ${momentary ? "momentary " : ""}$device's ${attribute} [$currentValue] ${condition.comp} '$value1${comp.parameters == 2 ? " - $value2" : ""}' returned $deviceResult"
					if (state.sim) state.sim.evals.push(msg)
					debug msg
				} else {
					deviceResult = "$function"(condition, device, attribute, oldValue, oldValueSince, currentValue, value1, value2, ownsEvent ? evt : null, evt, momentary, type)
					def msg = "${deviceResult ? "♣" : "♠"} Function $function for $device's ${attribute} [$currentValue] ${condition.comp} '$value1${comp.parameters == 2 ? " - $value2" : ""}' returned $deviceResult"
					if (state.sim) state.sim.evals.push(msg)
					debug msg
				}

			}

			if (deviceResult) {
				if (condition.vm) vm.push "$device"
			} else {
				if (condition.vn) vn.push "$device"
			}

			//compound the result, depending on mode
			def finalResult = false
			switch (mode) {
				case "All":
					result = result && deviceResult
					finalResult = !result
					break
				case "Any":
					result = result || deviceResult
					finalResult = result
					break
			}
			//optimize the loop to exit when we find a result that's going to be the final one (AND encountered a false, or OR encountered a true)
			if (finalResult && !(condition.vm || condition.vn)) break
		}
	}

	if (evt) {
		if (condition.vm) setVariable(condition.vm, buildNameList(vm, "and"))
		if (condition.vn) setVariable(condition.vn, buildNameList(vn, "and"))
	}
	return  result
}

private evaluateTimeCondition(condition, evt = null, unixTime = null, getNextEventTime = false) {
	//we sometimes optimize this and sent the comparison text and object
	//no condition? not time condition? false!
	if (!condition || (condition.attr != "time")) {
		return false
	}
	//get UTC now if no unixTime is provided
	unixTime = unixTime ? unixTime : now()
	//convert that to location's timezone, for comparison
	def attr = getAttributeByName(condition.attr)
	def comparison = cleanUpComparison(condition.comp)
	def comp = getComparisonOption(condition.attr, comparison)
	//if we can't find the attribute (can't be...) or the comparison object, or we're dealing with a trigger, exit stage false
	if (!attr || !comp) {
		return false
	}

	if (comp.trigger == comparison) {
		if (evt) {
			//trigger
			if (evt && (evt.deviceId == "time") && (evt.conditionId == condition.id)) {
				condition.lt = evt.date.time
				//we have a time event returning as a result of a trigger, assume true
				return true
			} else {
				if (comparison.contains("stay")) {
					//we have a stay condition
				}
			}
		}
		return false
	}

	def time = adjustTime(unixTime)

	//check comparison
	def result = true
	if (comparison.contains("any")) {
		//we match any time
	} else {
		//convert times to number of minutes since midnight for easy comparison
		//add one minute if we're within 3 seconds of the next minute
		def m = time ? time.hours * 60 + time.minutes : 0 + (time.seconds >= 57 ? 1 : 0)
		def m1 = null
		def m2 = null
		//go through each parameter
		def o1 = condition.o1 ? condition.o1 : 0
		def o2 = condition.o2 ? condition.o2 : 0
		def useDate1 = false
		def useDate2 = false
		for (def i = 1; i <= comp.parameters; i++) {
			def val = i == 1 ? condition.val1 : condition.val2
			def t = null
			def v = 0
			def useDate = false
			switch (val) {
				case "custom time":
					t = (i == 1 ? (condition.t1 ? adjustTime(condition.t1) : null) : (condition.t2 ? adjustTime(condition.t2) : null))
					if (t) {
						v = t ? t.getHours() * 60 + t.getMinutes() : null
					}
					if (!comparison.contains("around")) {
						switch (i) {
							case 1:
								o1 = 0
								break
							case 2:
								o2 = 0
								break
						}
					}
					break
				case "midnight":
					v = (i == 1 ? 0 : 1440)
					break
				case "sunrise":
					t = getSunrise()
					v = t ? t.hours * 60 + t.minutes : null
					break
				case "noon":
					v = 12 * 60 //noon is 720 minutes away from midnight
					break
				case "sunset":
					t = getSunset()
					v = t ? t.hours * 60 + t.minutes : null
					break
				case "time of variable":
					t = adjustTime(getVariable(i == 1 ? condition.var1 : condition.var2))
					v = t ? t.hours * 60 + t.minutes : null
					break
				case "date and time of variable":
					t = adjustTime(getVariable(i == 1 ? condition.var1 : condition.var2))
					v = t ? t.hours * 60 + t.minutes : null
					useDate = true
					break
			}
			if (i == 1) {
				useDate1 = useDate
				m1 = useDate ? (t ? t.time - t.time.mod(60000) : 0) : v
			} else {
				useDate2 = useDate
				m2 = useDate ? (t ? t.time - t.time.mod(60000) : 0) : v
			}
		}

		//add one minute if we're within 3 seconds of the next minute
		def rightNow = adjustTime()
		rightNow = rightNow.time - rightNow.time.mod(60000) + (rightNow.seconds >= 57 ? 60000 : 0)
		def lastMidnight =  rightNow - rightNow.mod(86400000)
		def nextMidnight =  lastMidnight + 86400000

		//we need to ensure we have a full condition
		if (getNextEventTime) {
			if ((m1 == null) || ((comp.parameters == 2) && (m2 == null))) {
				return null
			}
		}
		switch (comparison) {
			case { comparison.contains("before") }:
				if ((m1 == null) || (useDate1 ? rightNow > m1 + o1 * 60000 : m >= addOffsetToMinutes(m1, o1))) {
					//m before m1?
					result = false
				}
				if (getNextEventTime) {
					if (result) {
						//we're looking for the next time when time is not before given amount, that's exactly the time we're looking at
						return convertDateToUnixTime(useDate1 ? m1 + o1 * 60000 : lastMidnight + addOffsetToMinutes(m1, o1) * 60000)
					} else {
						//the next time time is before a certain time is... next midnight...
						return useDate1 ? null : convertDateToUnixTime(nextMidnight)
					}
				}
				if (!result) return false
				break
			case { comparison.contains("after") }:
				if ((m1 == null) || (useDate1 ? rightNow < m1 + o1 * 60000 : m < addOffsetToMinutes(m1, o1))) {
					//m after m1?
					result = false
				}
				if (getNextEventTime) {
					if (result) {
						//we're looking for the next time when time is not after given amount, next midnight
						return useDate1 ? null : convertDateToUnixTime(nextMidnight)
					} else {
						//the next time time is before a certain time is... next midnight...
						return convertDateToUnixTime(useDate1 ? m1 + o1 * 60000 : lastMidnight + addOffsetToMinutes(m1, o1) * 60000)
					}
				}
				if (!result) return result
				break
			case { comparison.contains("around") }:
				//if no offset, we can't really match anything
				def a1 = useDate1 ? m1 - o1 * 60000 : addOffsetToMinutes(m1, -o1)
				def a2 = useDate1 ? m1 + o1 * 60000 : addOffsetToMinutes(m1, +o1)
				def mm = useDate1 ? rightNow : m
				if (a1 < a2 ? (mm < a1) || (mm >= a2) : (mm >= a2) && (mm < a1)) {
					result = false
				}
				if (getNextEventTime) {
					if (result) {
						//we're in between the +/- time, the a2 is the next time we are looking for
						return useDate1 ? null : convertDateToUnixTime(lastMidnight + a2 * 60000)
					} else {
						//return a1 time either today or tomorrow
						return convertDateToUnixTime(useDate1 ? (a1 > time.time ? a1 : null)  : (a1 < mm ? nextMidnight : lastMidnight) + a1 * 60000)
					}
				}
				if (!result) return result
				break
			case { comparison.contains("between") }:
				def a1 = useDate1 ? m1 + o1 * 60000 : (useDate2 ? m2 - m2.mod(86400000) : lastMidnight) + addOffsetToMinutes(m1, o1) * 60000
				def a2 = useDate2 ? m2 + o2 * 60000 : (useDate1 ? m1 - m1.mod(86400000) : lastMidnight) + addOffsetToMinutes(m2, o2) * 60000
				def mm = rightNow
				if ((a1 > a2) && (!useDate1 || !useDate2)) {
					//if a1 is after a2, and we haven't specified dates for both, increment a2 with 1 day to bring it after a1
					if ((mm < a2) || (useDate2)) {
						a1 = a1 - 86400000
					} else {
						a2 = a2 + 86400000
					}
				}
				def eval = (mm < a1) || (mm >= a2)
				if (getNextEventTime) {
					if (!eval) {
						//we're in between the a1 and a2
						return convertDateToUnixTime(a2)
					} else {
						//we're not in between the a1 and a2
						return convertDateToUnixTime(a1 <= mm ? (a2 <= mm ? (useDate1 ? null : a1 + 86400000) : a2) : a1)
					}
				}
				if (comparison.contains("not")) {
					eval = !eval
				}
				if (eval) {
					result = false
				}
				if (!result) return result
				break
		}
	}

	if (getNextEventTime) {
		return null
	}
	return result && testDateTimeFilters(condition, time)
}

private testDateTimeFilters(condition, now) {
	//if we made it this far, let's check on filters
	if (condition.fmh || condition.fhd || condition.fdw || condition.fdm || condition.fwm || condition.fmy || condition.fy) {
		//check minute filter
		if (condition.fmh) {
			def m = now.minutes.toString().padLeft(2, "0")
			if (!(m in condition.fmh)) {
				return false
			}
		}

		//check hour filter
		if (condition.fhd) {
			def h = formatHour(now.hours)
			if (!(h in condition.fhd)) {
				return false
			}
		}

		if (condition.fdw) {
			def dow = getDayOfWeekName(now)
			if (!(dow in condition.fdw)) {
				return false
			}
		}

		if (condition.fwm) {
			def weekNo = "the ${formatOrdinalNumberName(getWeekOfMonth(now))} week"
			def lastWeekNo = "the ${formatOrdinalNumberName(getWeekOfMonth(now, reverse))} week"
			if (!((weekNo in condition.fwm) || (lastWeekNo in condition.fwm))) {
				return false
			}
		}
		if (condition.fdm) {
			def dayNo = "the " + formatOrdinalNumber(getDayOfMonth(now))
			def lastDayNo = "the " + formatOrdinalNumberName(getDayOfMonth(now, true)) + " day of the month"
			if (!((dayNo in condition.fdm) || (lastDayNo in condition.fdm))) {
				return false
			}
		}

		if (condition.fmy) {
			if (!(getMonthName(now) in condition.fmy)) {
				return false
			}
		}

		if (condition.fy) {
			def year = now.year + 1900
			def yearOddEven = year.mod(2)
			def odd = "odd years" in condition.fy
			def even = "even years" in condition.fy
			def leap = "leap years" in condition.fy
			if (!(((yearOddEven == 0) && even) || ((yearOddEven == 1) && odd) || ((year.mod(4) == 0) && leap) || ("$year" in condition.fy))) {
				return false
			}
		}
	}
	return true
}

/* low-level evaluation functions */
private eval_cond_is_equal_to(condition, device, attribute, oldValue, oldValueSince, currentValue, value1, value2, evt, sourceEvt, momentary, dataType) {
	return currentValue == value1
}

private eval_cond_is_not_equal_to(condition, device, attribute, oldValue, oldValueSince, currentValue, value1, value2, evt, sourceEvt, momentary, dataType) {
	return currentValue != value1
}

private eval_cond_is(condition, device, attribute, oldValue, oldValueSince, currentValue, value1, value2, evt, sourceEvt, momentary, dataType) {
	return eval_cond_is_equal_to(condition, device, attribute, oldValue, oldValueSince, currentValue, value1, value2, evt, sourceEvt, momentary, dataType)
}

private eval_cond_is_not(condition, device, attribute, oldValue, oldValueSince, currentValue, value1, value2, evt, sourceEvt, momentary, dataType) {
	return eval_cond_is_not_equal_to(condition, device, attribute, oldValue, oldValueSince, currentValue, value1, value2, evt, sourceEvt, momentary, dataType)
}

private eval_cond_is_true(condition, device, attribute, oldValue, oldValueSince, currentValue, value1, value2, evt, sourceEvt, momentary, dataType) {
	return eval_cond_is_equal_to(condition, device, attribute, oldValue, oldValueSince, currentValue, true, value2, evt, sourceEvt, momentary, dataType)
}

private eval_cond_is_false(condition, device, attribute, oldValue, oldValueSince, currentValue, value1, value2, evt, sourceEvt, momentary, dataType) {
	return !eval_cond_is_true(condition, device, attribute, oldValue, oldValueSince, currentValue, value1, value2, evt, sourceEvt, momentary, dataType)
}

private eval_cond_is_one_of(condition, device, attribute, oldValue, oldValueSince, currentValue, value1, value2, evt, sourceEvt, momentary, dataType) {
	def v = "$currentValue".trim()
	for(def value in value1) {
		if ("$value".trim() == v)
			return true
	}
	return false
}

private eval_cond_is_not_one_of(condition, device, attribute, oldValue, oldValueSince, currentValue, value1, value2, evt, sourceEvt, momentary, dataType) {
	return !eval_cond_is_one_of(condition, device, attribute, oldValue, oldValueSince, currentValue, value1, value2, evt, sourceEvt, momentary, dataType)
}

private eval_cond_is_less_than(condition, device, attribute, oldValue, oldValueSince, currentValue, value1, value2, evt, sourceEvt, momentary, dataType) {
	return currentValue < value1
}

private eval_cond_is_less_than_or_equal_to(condition, device, attribute, oldValue, oldValueSince, currentValue, value1, value2, evt, sourceEvt, momentary, dataType) {
	return currentValue <= value1
}

private eval_cond_is_greater_than(condition, device, attribute, oldValue, oldValueSince, currentValue, value1, value2, evt, sourceEvt, momentary, dataType) {
	return currentValue > value1
}

private eval_cond_is_greater_than_or_equal_to(condition, device, attribute, oldValue, oldValueSince, currentValue, value1, value2, evt, sourceEvt, momentary, dataType) {
	return currentValue >= value1
}

private eval_cond_is_even(condition, device, attribute, oldValue, oldValueSince, currentValue, value1, value2, evt, sourceEvt, momentary, dataType) {
	try {
		return Math.round(currentValue).mod(2) == 0
	} catch(all) {}
	return false
}

private eval_cond_is_odd(condition, device, attribute, oldValue, oldValueSince, currentValue, value1, value2, evt, sourceEvt, momentary, dataType) {
	try {
		return Math.round(currentValue).mod(2) == 1
	} catch(all) {}
	return false
}

private eval_cond_is_inside_range(condition, device, attribute, oldValue, oldValueSince, currentValue, value1, value2, evt, sourceEvt, momentary, dataType) {
	if (value1 < value2) {
		return (currentValue >= value1) && (currentValue <= value2)
	} else {
		return (currentValue >= value2) && (currentValue <= value1)
	}
}

private eval_cond_is_outside_of_range(condition, device, attribute, oldValue, oldValueSince, currentValue, value1, value2, evt, sourceEvt, momentary, dataType) {
	if (value1 < value2) {
		return (currentValue < value1) || (currentValue > value2)
	} else {
		return (currentValue < value2) || (currentValue > value1)
	}
}

private listPreviousStates(device, attribute, currentValue, minutes, excludeLast) {
	def result = []
	if (!(device instanceof physicalgraph.app.DeviceWrapper)) return result
	def events = device.events([all: true, max: 100]).findAll{it.name == attribute}
	//if we got any events, let's go through them
	//if we need to exclude last event, we start at the second event, as the first one is the event that triggered this function. The attribute's value has to be different from the current one to qualify for quiet
	def value = currentValue
	def thresholdTime = now() - minutes * 60000
	def endTime = now()
	for(def i = 0; i < events.size(); i++) {
		def startTime = events[i].date.getTime()
		def duration = endTime - startTime
		if ((duration >= 1000) && ((i > 0) || !excludeLast)) {
			result.push([value: events[i].value, startTime: startTime, duration: duration])
		}
		if (startTime < thresholdTime)
			break
		endTime = startTime
	}
	return result
}

private eval_cond_changed(condition, device, attribute, oldValue, oldValueSince, currentValue, value1, value2, evt, sourceEvt, momentary, dataType) {
	def minutes = timeToMinutes(condition.fort)
	def events = device.eventsSince(new Date(now() - minutes * 60000)).findAll{it.name == attribute}
	return (events.size() > 0)
}

private eval_cond_did_not_change(condition, device, attribute, oldValue, oldValueSince, currentValue, value1, value2, evt, sourceEvt, momentary, dataType) {
	return !eval_cond_changed(condition, device, attribute, oldValue, oldValueSince, currentValue, value1, value2, evt, sourceEvt, momentary, dataType)
}

private eval_cond_was(condition, device, attribute, oldValue, oldValueSince, currentValue, value1, value2, evt, sourceEvt, momentary, dataType) {
	return eval_cond_was_equal_to(condition, device, attribute, oldValue, oldValueSince, currentValue, value1, value2, evt, sourceEvt, momentary, dataType)
}

private eval_cond_was_not(condition, device, attribute, oldValue, oldValueSince, currentValue, value1, value2, evt, sourceEvt, momentary, dataType) {
	eval_cond_was_not_equal_to(condition, device, attribute, oldValue, oldValueSince, currentValue, value1, value2, evt, sourceEvt, momentary, dataType)
}

private eval_cond_was_equal_to(condition, device, attribute, oldValue, oldValueSince, currentValue, value1, value2, evt, sourceEvt, momentary, dataType) {
	def time = timeToMinutes(condition.fort)
	def states = listPreviousStates(device, attribute, currentValue, time, evt ? 1 : 0)
	def thresholdTime = time * 60000
	def stableTime = 0
	for (state in states) {
		if (cast(state.value, dataType) == value1) {
			stableTime += state.duration
		} else {
			break
		}
	}
	return (stableTime > 0) && (condition.for == "for at least" ? stableTime >= thresholdTime : stableTime < thresholdTime)
}

private eval_cond_was_not_equal_to(condition, device, attribute, oldValue, oldValueSince, currentValue, value1, value2, evt, sourceEvt, momentary, dataType) {
	def time = timeToMinutes(condition.fort)
	def states = listPreviousStates(device, attribute, currentValue, time, evt ? 1 : 0)
	def thresholdTime = time * 60000
	def stableTime = 0
	for (state in states) {
		if (cast(state.value, dataType) != value1) {
			stableTime += state.duration
		} else {
			break
		}
	}
	return (stableTime > 0) && (condition.for == "for at least" ? stableTime >= thresholdTime : stableTime < thresholdTime)
}

private eval_cond_was_less_than(condition, device, attribute, oldValue, oldValueSince, currentValue, value1, value2, evt, sourceEvt, momentary, dataType) {
	def time = timeToMinutes(condition.fort)
	def states = listPreviousStates(device, attribute, currentValue, time, evt ? 1 : 0)
	def thresholdTime = time * 60000
	def stableTime = 0
	for (state in states) {
		if (cast(state.value, dataType) < value1) {
			stableTime += state.duration
		} else {
			break
		}
	}
	return (stableTime > 0) && (condition.for == "for at least" ? stableTime >= thresholdTime : stableTime < thresholdTime)
}

private eval_cond_was_less_than_or_equal_to(condition, device, attribute, oldValue, oldValueSince, currentValue, value1, value2, evt, sourceEvt, momentary, dataType) {
	def time = timeToMinutes(condition.fort)
	def states = listPreviousStates(device, attribute, currentValue, time, evt ? 1 : 0)
	def thresholdTime = time * 60000
	def stableTime = 0
	for (state in states) {
		if (cast(state.value, dataType) <= value1) {
			stableTime += state.duration
		} else {
			break
		}
	}
	return (stableTime > 0) && (condition.for == "for at least" ? stableTime >= thresholdTime : stableTime < thresholdTime)
}

private eval_cond_was_greater_than(condition, device, attribute, oldValue, oldValueSince, currentValue, value1, value2, evt, sourceEvt, momentary, dataType) {
	def time = timeToMinutes(condition.fort)
	def states = listPreviousStates(device, attribute, currentValue, time, evt ? 1 : 0)
	def thresholdTime = time * 60000
	def stableTime = 0
	for (state in states) {
		if (cast(state.value, dataType) > value1) {
			stableTime += state.duration
		} else {
			break
		}
	}
	return (stableTime > 0) && (condition.for == "for at least" ? stableTime >= thresholdTime : stableTime < thresholdTime)
}

private eval_cond_was_greater_than_or_equal_to(condition, device, attribute, oldValue, oldValueSince, currentValue, value1, value2, evt, sourceEvt, momentary, dataType) {
	def time = timeToMinutes(condition.fort)
	def states = listPreviousStates(device, attribute, currentValue, time, evt ? 1 : 0)
	def thresholdTime = time * 60000
	def stableTime = 0
	for (state in states) {
		if (cast(state.value, dataType) >= value1) {
			stableTime += state.duration
		} else {
			break
		}
	}
	return (stableTime > 0) && (condition.for == "for at least" ? stableTime >= thresholdTime : stableTime < thresholdTime)
}

private eval_cond_was_even(condition, device, attribute, oldValue, oldValueSince, currentValue, value1, value2, evt, sourceEvt, momentary, dataType) {
	def time = timeToMinutes(condition.fort)
	def states = listPreviousStates(device, attribute, currentValue, time, evt ? 1 : 0)
	def thresholdTime = time * 60000
	def stableTime = 0
	for (state in states) {
		if (cast(state.value, "number").mod(2) == 0) {
			stableTime += state.duration
		} else {
			break
		}
	}
	return (stableTime > 0) && (condition.for == "for at least" ? stableTime >= thresholdTime : stableTime < thresholdTime)
}

private eval_cond_was_odd(condition, device, attribute, oldValue, oldValueSince, currentValue, value1, value2, evt, sourceEvt, momentary, dataType) {
	def time = timeToMinutes(condition.fort)
	def states = listPreviousStates(device, attribute, currentValue, time, evt ? 1 : 0)
	def thresholdTime = time * 60000
	def stableTime = 0
	for (state in states) {
		if (cast(state.value, "number").mod(2) == 1) {
			stableTime += state.duration
		} else {
			break
		}
	}
	return (stableTime > 0) && (condition.for == "for at least" ? stableTime >= thresholdTime : stableTime < thresholdTime)
}

private eval_cond_was_inside_range(condition, device, attribute, oldValue, oldValueSince, currentValue, value1, value2, evt, sourceEvt, momentary, dataType) {
	def time = timeToMinutes(condition.fort)
	def states = listPreviousStates(device, attribute, currentValue, time, evt ? 1 : 0)
	def thresholdTime = time * 60000
	def stableTime = 0
	for (state in states) {
		def v = cast(state.value, dataType)
		if (value1 < value2 ? (v >= value1) && (v <= value2) : (v >= value2) && (v <= value1)) {
			stableTime += state.duration
		} else {
			break
		}
	}
	return (stableTime > 0) && (condition.for == "for at least" ? stableTime >= thresholdTime : stableTime < thresholdTime)
}

private eval_cond_was_outside_of_range(condition, device, attribute, oldValue, oldValueSince, currentValue, value1, value2, evt, sourceEvt, momentary, dataType) {
	def time = timeToMinutes(condition.fort)
	def states = listPreviousStates(device, attribute, currentValue, time, evt ? 1 : 0)
	def thresholdTime = time * 60000
	def stableTime = 0
	for (state in states) {
		def v = cast(state.value, dataType)
		if (value1 < value2 ? (v < value1) || (v > value2) : (v < value2) || (v > value1)) {
			stableTime += state.duration
		} else {
			break
		}
	}
	return (stableTime > 0) && (condition.for == "for at least" ? stableTime >= thresholdTime : stableTime < thresholdTime)
}

/* triggers */
private eval_trg_changes(condition, device, attribute, oldValue, oldValueSince, currentValue, value1, value2, evt, sourceEvt, momentary, dataType) {
	return true
}

private eval_trg_changes_to(condition, device, attribute, oldValue, oldValueSince, currentValue, value1, value2, evt, sourceEvt, momentary, dataType) {
	return	eval_cond_is_equal_to(condition, device, attribute, null, null, currentValue, value1, value2, evt, sourceEvt, momentary, dataType)
}

private eval_trg_changes_to_one_of(condition, device, attribute, oldValue, oldValueSince, currentValue, value1, value2, evt, sourceEvt, momentary, dataType) {
	return 	eval_cond_is_not_one_of(condition, device, attribute, null, null, oldValue, value1, value2, evt, sourceEvt, momentary, dataType) &&
		eval_cond_is_one_of(condition, device, attribute, null, null, currentValue, value1, value2, evt, sourceEvt, momentary, dataType)
}

private eval_trg_changes_away_from(condition, device, attribute, oldValue, oldValueSince, currentValue, value1, value2, evt, sourceEvt, momentary, dataType) {
	return	eval_cond_is_equal_to(condition, device, attribute, null, null, oldValue, value1, value2, evt, sourceEvt, momentary, dataType) &&
		eval_cond_is_not_equal_to(condition, device, attribute, null, null, currentValue, value1, value2, evt, sourceEvt, momentary, dataType)
}

private eval_trg_changes_away_from_one_of(condition, device, attribute, oldValue, oldValueSince, currentValue, value1, value2, evt, sourceEvt, momentary, dataType) {
	return	eval_cond_is_one_of(condition, device, attribute, null, null, oldValue, value1, value2, evt, sourceEvt, momentary, dataType) &&
		eval_cond_is_not_one_of(condition, device, attribute, null, null, currentValue, value1, value2, evt, sourceEvt, momentary, dataType)
}

private eval_trg_drops(condition, device, attribute, oldValue, oldValueSince, currentValue, value1, value2, evt, sourceEvt, momentary, dataType) {
	return currentValue < oldValue
}

private eval_trg_drops_below(condition, device, attribute, oldValue, oldValueSince, currentValue, value1, value2, evt, sourceEvt, momentary, dataType) {
	return !eval_cond_is_less_than(condition, device, attribute, null, null, oldValue, value1, value2, evt, sourceEvt, momentary, dataType) &&
		eval_cond_is_less_than(condition, device, attribute, null, null, currentValue, value1, value2, evt, sourceEvt, momentary, dataType)
}

private eval_trg_drops_to_or_below(condition, device, attribute, oldValue, oldValueSince, currentValue, value1, value2, evt, sourceEvt, momentary, dataType) {
	return !eval_cond_is_less_than_or_equal_to(condition, device, attribute, null, null, oldValue, value1, value2, evt, sourceEvt, momentary, dataType) &&
		eval_cond_is_less_than_or_equal_to(condition, device, attribute, null, null, currentValue, value1, value2, evt, sourceEvt, momentary, dataType)
}

private eval_trg_raises(condition, device, attribute, oldValue, oldValueSince, currentValue, value1, value2, evt, sourceEvt, momentary, dataType) {
	return currentValue > oldValue
}

private eval_trg_raises_above(condition, device, attribute, oldValue, oldValueSince, currentValue, value1, value2, evt, sourceEvt, momentary, dataType) {
	return !eval_cond_is_greater_than(condition, device, attribute, null, null, oldValue, value1, value2, evt, sourceEvt, momentary, dataType) &&
		eval_cond_is_greater_than(condition, device, attribute, null, null, currentValue, value1, value2, evt, sourceEvt, momentary, dataType)
}

private eval_trg_raises_to_or_above(condition, device, attribute, oldValue, oldValueSince, currentValue, value1, value2, evt, sourceEvt, momentary, dataType) {
	return !eval_cond_is_greater_than_or_equal_to(condition, device, attribute, null, null, oldValue, value1, value2, evt, sourceEvt, momentary, dataType) &&
		eval_cond_is_greater_than_or_equal_to(condition, device, attribute, null, null, currentValue, value1, value2, evt, sourceEvt, momentary, dataType)
}

private eval_trg_changes_to_even(condition, device, attribute, oldValue, oldValueSince, currentValue, value1, value2, evt, sourceEvt, momentary, dataType) {
	return !eval_cond_is_even(condition, device, attribute, null, null, oldValue, value1, value2, evt, sourceEvt, momentary, dataType) &&
		eval_cond_is_even(condition, device, attribute, null, null, currentValue, value1, value2, evt, sourceEvt, momentary, dataType)
}

private eval_trg_changes_to_odd(condition, device, attribute, oldValue, oldValueSince, currentValue, value1, value2, evt, sourceEvt, momentary, dataType) {
	return !eval_cond_is_odd(condition, device, attribute, null, null, oldValue, value1, value2, evt, sourceEvt, momentary, dataType) &&
		eval_cond_is_odd(condition, device, attribute, null, null, currentValue, value1, value2, evt, sourceEvt, momentary, dataType)
}

private eval_trg_enters_range(condition, device, attribute, oldValue, oldValueSince, currentValue, value1, value2, evt, sourceEvt, momentary, dataType) {
	return !eval_cond_is_inside_range(condition, device, attribute, null, null, oldValue, value1, value2, evt, sourceEvt, momentary, dataType) &&
		eval_cond_is_inside_range(condition, device, attribute, null, null, currentValue, value1, value2, evt, sourceEvt, momentary, dataType)
}

private eval_trg_exits_range(condition, device, attribute, oldValue, oldValueSince, currentValue, value1, value2, evt, sourceEvt, momentary, dataType) {
	return !eval_cond_is_outside_of_range(condition, device, attribute, null, null, oldValue, value1, value2, evt, sourceEvt, momentary, dataType) &&
		eval_cond_is_outside_of_range(condition, device, attribute, null, null, currentValue, value1, value2, evt, sourceEvt, momentary, dataType)
}

private eval_trg_executed(condition, device, attribute, oldValue, oldValueSince, currentValue, value1, value2, evt, sourceEvt, momentary, dataType) {
	return (currentValue == value1)
}

private eval_trg_stays(condition, device, attribute, oldValue, oldValueSince, currentValue, value1, value2, evt, sourceEvt, momentary, dataType) {
	return eval_trg_stays_common("is", condition, device, attribute, oldValue, oldValueSince, currentValue, value1, value2, evt, sourceEvt, momentary, dataType)
}

private eval_trg_stays_away_from(condition, device, attribute, oldValue, oldValueSince, currentValue, value1, value2, evt, sourceEvt, momentary, dataType) {
	return eval_trg_stays_common("is_not", condition, device, attribute, oldValue, oldValueSince, currentValue, value1, value2, evt, sourceEvt, momentary, dataType)
}

private eval_trg_stays_equal_to(condition, device, attribute, oldValue, oldValueSince, currentValue, value1, value2, evt, sourceEvt, momentary, dataType) {
	return eval_trg_stays_common("is_equal_to", condition, device, attribute, oldValue, oldValueSince, currentValue, value1, value2, evt, sourceEvt, momentary, dataType)
}

private eval_trg_stays_not_equal_to(condition, device, attribute, oldValue, oldValueSince, currentValue, value1, value2, evt, sourceEvt, momentary, dataType) {
	return eval_trg_stays_common("is_not_equal_to", condition, device, attribute, oldValue, oldValueSince, currentValue, value1, value2, evt, sourceEvt, momentary, dataType)
}

private eval_trg_stays_less_than(condition, device, attribute, oldValue, oldValueSince, currentValue, value1, value2, evt, sourceEvt, momentary, dataType) {
	return eval_trg_stays_common("is_less_than", condition, device, attribute, oldValue, oldValueSince, currentValue, value1, value2, evt, sourceEvt, momentary, dataType)
}

private eval_trg_stays_less_than_or_equal_to(condition, device, attribute, oldValue, oldValueSince, currentValue, value1, value2, evt, sourceEvt, momentary, dataType) {
	return eval_trg_stays_common("is_less_than_or_equal_to", condition, device, attribute, oldValue, oldValueSince, currentValue, value1, value2, evt, sourceEvt, momentary, dataType)
}

private eval_trg_stays_greater_than(condition, device, attribute, oldValue, oldValueSince, currentValue, value1, value2, evt, sourceEvt, momentary, dataType) {
	return eval_trg_stays_common("is_greater_than", condition, device, attribute, oldValue, oldValueSince, currentValue, value1, value2, evt, sourceEvt, momentary, dataType)
}

private eval_trg_stays_greater_than_or_equal_to(condition, device, attribute, oldValue, oldValueSince, currentValue, value1, value2, evt, sourceEvt, momentary, dataType) {
	return eval_trg_stays_common("is_greater_than_or_equal_to", condition, device, attribute, oldValue, oldValueSince, currentValue, value1, value2, evt, sourceEvt, momentary, dataType)
}

private eval_trg_stays_in_range(condition, device, attribute, oldValue, oldValueSince, currentValue, value1, value2, evt, sourceEvt, momentary, dataType) {
	return eval_trg_stays_common("is_in_range", condition, device, attribute, oldValue, oldValueSince, currentValue, value1, value2, evt, sourceEvt, momentary, dataType)
}

private eval_trg_stays_outside_of_range(condition, device, attribute, oldValue, oldValueSince, currentValue, value1, value2, evt, sourceEvt, momentary, dataType) {
	return eval_trg_stays_common("is_outside_of_range", condition, device, attribute, oldValue, oldValueSince, currentValue, value1, value2, evt, sourceEvt, momentary, dataType)
}

private eval_trg_stays_even(condition, device, attribute, oldValue, oldValueSince, currentValue, value1, value2, evt, sourceEvt, momentary, dataType) {
	return eval_trg_stays_common("is_even", condition, device, attribute, oldValue, oldValueSince, currentValue, value1, value2, evt, sourceEvt, momentary, dataType)
}

private eval_trg_stays_odd(condition, device, attribute, oldValue, oldValueSince, currentValue, value1, value2, evt, sourceEvt, momentary, dataType) {
	return eval_trg_stays_common("is_odd", condition, device, attribute, oldValue, oldValueSince, currentValue, value1, value2, evt, sourceEvt, momentary, dataType)
}

private eval_trg_stays_common(func, condition, device, attribute, oldValue, oldValueSince, currentValue, value1, value2, evt, sourceEvt, momentary, dataType) {
	def result = "eval_cond_$func"(condition, device, attribute, oldValue, oldValueSince, currentValue, value1, value2, evt, sourceEvt, momentary, dataType)
	if (evt.name == attribute) {
		//initial event
		if (result) {
			//true, let's schedule time...
			//if there is no event task currently scheduled for this, so we need to schedule one, but wait...
			//was the old value not matching? because if it was, then we need to inhibit this...
			def oldResult = "eval_cond_$func"(condition, device, attribute, oldValue, oldValueSince, oldValue, value1, value2, evt, sourceEvt, momentary, dataType)
			if (oldResult != result) {
				def tasks = state.tasks
				if (!tasks || !tasks.find{ (it.value?.type == "evt") && (it.value?.ownerId == condition.id) && (it.value?.deviceId == device.id) }) {
					def time = now() + timeToMinutes(condition.fort) * 60000
					scheduleTask("evt", condition.id, device.id, null, time)
				}
			}
		} else {
			unscheduleTask("evt", condition.id, device.id)
		}
		return false
	}
	//timed event
	return result
}

/******************************************************************************/
/*** SCHEDULER FUNCTIONS - TIMING BELT										***/
/******************************************************************************/

private scheduleTimeTriggers() {
	debug "Rescheduling time triggers", null, "trace"
	//remove all pending events
	unscheduleTask("evt", null, "time")
	def app = state.run == "config" ? state.config.app : state.app
	if (getTriggerCount(app) > 0) {
		withEachTrigger(app.conditions, "scheduleTimeTrigger")
		if (app.mode in ["Latching", "And-If", "Or-If"]) {
			withEachTrigger(app.otherConditions, "scheduleTimeTrigger")
		}
	} else {
		//we're not using triggers, let's mess up with time conditions
		withEachCondition(app.conditions, "scheduleTimeTrigger")
		if (app.mode in ["Latching", "And-If", "Or-If"]) {
			withEachCondition(app.otherConditions, "scheduleTimeTrigger")
		}
	}
}

private scheduleTimeTrigger(condition, data = null) {
	if (!condition || !(condition.attr) || (condition.attr != "time")) return
	def time = condition.trg ? getNextTimeTriggerTime(condition, condition.lt) : getNextTimeConditionTime(condition, condition.lt)
	condition.nt = time
	if ((time instanceof Long) && (time > 0)) {
		scheduleTask("evt", condition.id, "time", null, time)
	}
}

private scheduleActions(conditionId, stateChanged = false, currentState = true) {
	//debug "Scheduling actions for condition #${conditionId}. State did${stateChanged ? "" : " NOT"} change."
	def actions = listActions(conditionId).sort{ it.id }
	for (action in actions) {
		//restrict on state changed
		if (action.rc && !stateChanged) continue
		if ((action.pid > 0) && ((action.rs != false ? true : false) != currentState)) continue
		if (action.rm && action.rm.size() && !(location.mode in action.rm)) continue
		if (action.ra && action.ra.size() && !(getAlarmSystemStatus() in action.ra)) continue
		if (action.rv && !(checkVariableCondition(action.rv, action.rvc, action.rvv))) continue
		if (action.rw && action.rw.size() && !(getDayOfWeekName() in action.rw)) continue
		if (action.rtf && action.rtt && !(checkTimeCondition(action.rtf, action.rtfc, action.rtfo, action.rtt, action.rttc, action.rtto))) continue
		if (action.rs1) {
			def r = false
			for(sw in settings["actRSwitchOn${action.id}"]) {
				if (sw.currentValue("switch") != "on") {
					r = true
					break
				}
			}
			if (r) continue
		}
		if (action.rs0) {
			def r = false
			for(sw in settings["actRSwitchOff${action.id}"]) {
				if (sw.currentValue("switch") != "off") {
					r = true
					break
				}
			}
			if (r) continue
		}
		//we survived all restrictions, pfew
		scheduleAction(action)
	}
}

private unscheduleActions(conditionId) {
	def tasks = atomicState.tasks
	tasks = tasks ? tasks : [:]
	while (true) {
		def item = tasks.find{ (it.value.type == "cmd") && (it.value.data && it.value.data.cc == conditionId)}
		if (item) {
			tasks.remove(item.key)
		} else {
			break
		}
	}
	atomicState.tasks = tasks
}

private scheduleAction(action) {
	if (!action) return null
	def deviceIds = action.l ? ["location"] : (action.d ? action.d : [])
	def tos = action.tos ? action.tos : "Action"
	if (tos != "None") {
		def aid = (tos == "Action") ? action.id : null
		unscheduleTask("cmd", action.id, null)
		for (deviceId in deviceIds) {
			//remove all tasks for all involved devices
			unscheduleTask("cmd", aid, deviceId)
		}
		if (tos == "Global") {
			debug "WARNING: Task override policy for Global is not yet implemented", null, "warn"
		}
	}
	def rightNow = now()
	def time = rightNow
	def waitFor = null
	def waitSince = null
	def flowChart
	if (action.t && action.t.size() && deviceIds.size() ) {
		def tasks = action.t.sort{ it.i }
		def x = 0
		def cnt = 0
		while (true) {
			resetRandomValues()
			//make sure x is within task list
			if ((x == null) || (x < 0) || (x >= tasks.size())) break
			cnt += 1
			def task = tasks[x]
			def cmd = task.c
			def virtual = (cmd && cmd.startsWith(virtualCommandPrefix()))
			def custom = (cmd && cmd.startsWith(customCommandPrefix()))
			cmd = cleanUpCommand(cmd)
			def command = null
			if (virtual) {
				//dealing with a virtual command
				command = getVirtualCommandByDisplay(cmd)
				if (command && command.flow) {
					//build the flowchart
					if (!flowChart) flowChart = buildFlowChart(tasks)
					//flow control logic
					def flow = flowChart[x]
					if (flow) {
						switch (flow.action) {
							case "begin":
								switch (flow.mode) {
									case "if":
									case "else":
										//begin an if block
										if (flow.isElse) {
											//if we're dealing with an Else If, we need to figure out if the true side executed, or the else if can run
											def startFlow = (flow.startIdx != null ? flowChart[flow.startIdx] : null)
											if (startFlow) {
												if (startFlow.eval) {
													//pretend we're true, so that if there's a next Else If it skips too
													flow.eval = true
													//the true side of the previous IF just finished, jump to end
													x = flow.endIdx
													continue
												}
											}
										}
										//if it's an else, we go through it, the previous IF was false
										if (flow.mode == "else") {
											x += 1
											continue
										}
										//if (condition)
										flow.eval = checkFlowCondition(task)
										if (flow.eval) {
											//continue to next line
											x += 1
											continue
										} else {
											//move on to the else or move to the end, if no else is present
											def newX = flow.elseIdx ? flow.elseIdx : flow.endIdx
											if (newX) {
												x = newX + 1
												continue
											}
										}
										break
									case "switch":
										if (flow.caseIdxs) {
											def val = getVariable(task.p[0].d)
											def found = false
											for(def y = 0; y < flow.caseIdxs.size(); y++) {
												//get the index of the next case
												def xx = flow.caseIdxs[y]
												//little Windex here
												def newFlow = flowChart[xx]
												//check to see if the case matches
												newFlow.eval = checkFlowCaseCondition(val, tasks[xx].p[0].d)
												if (newFlow.eval) {
													//if it matches, go there
													x = xx + 1
													found = true
													break
												}
											}
											if (found) continue
										}
										//no case found, skip
										x = flow.endIdx + 1
										continue
									case "case":
										//if we got here, we need to skip to the end
										//a matching case probably just finished
										x = flow.endIdx
										continue
									case "loop":
										if (flow.isWhile) {
											//while loops are simple :)
											flow.eval = checkFlowCondition(task)
											if (flow.eval) {
												x += 1
											} else {
												x = flow.endIdx + 1
											}
											continue
										}
										if (!flow.active) {
											def start
											def end
											def step
											if (flow.isSimple) {
												//initialize the simple loop
												flow.varName = null
												flow.start = 0
												flow.end = Math.abs(cast(formatMessage(task.p[0].d), "number")) - 1
												setVariable("\$index", flow.start, true)
												if (flow.end < flow.start) {
													flow.active = false
													x = flow.endIdx + 1
													continue
												}
											} else {
												flow.varName = task.p[0].d
												flow.start = cast(formatMessage(task.p[1].d), "number")
												flow.end = cast(formatMessage(task.p[2].d), "number")
												//set the variable
												setVariable(flow.varName, flow.start)
											}
											flow.step = (flow.end >= flow.start ? 1 : -1)
											flow.pos = flow.start
											//start the loop
											flow.active = true
											scheduleTask("cmd", action.id, deviceId, task.i, command.delay ? command.delay : time, [variable: flow.varName, value: flow.pos])
											x += 1
											continue
										} else {
											//loop is already in progress
											//if we're using a variable, get its current value
											if (flow.varName) flow.pos = getVariable(flow.varName)
											//then increment int
											flow.pos = flow.pos + flow.step
											//if we're using a variable, update it
											if (flow.varName) setVariable(flow.varName, flow.pos)
											setVariable("\$index", flow.pos, true)
											scheduleTask("cmd", action.id, deviceId, task.i, command.delay ? command.delay : time, [variable: flow.varName, value: flow.pos])
											if (flow.step > 0 ? (flow.pos > flow.end) : (flow.pos < flow.end)) {
												//loop ended, jump over the end
												//jmp endIdx + 0x0001 :D
												flow.active = null
												flow.varName = null
												x = flow.endIdx + 1
												continue
											}
											//another loop cycle, moving on...
											x = x + 1
											continue
										}
										break
								}
								break
							case "break":
								//we need to find the closest earlier loop or switch and get out of it
								if (flow.isIf) {
									flow.eval = checkFlowCondition(task)
									if (!flow.eval) {
										//if the break condition is not met, we skip that
										x += 1
										continue
									}
								}
								for (def y = x - 1; y >= 0; y--) {
									def startFlow = flowChart[y]
									if ((startFlow.action == "begin") && (startFlow.isLoop || (startFlow.isSwitch && !startFlow.isCase))) {
										startFlow.active = null
										startFlow.varName = null
										x = startFlow.endIdx + 1
										continue
									}
								}
								break
							case "end":
								if (flow.isLoop) {
									if (task.p && (task.p.size() == 1)) {
										//delay the loop
										time = time + cast(task.p[0].d, "number") * 1000
									}
									//if this is the end of a loop, we cycle back to the start
									//that loop start will automatically jump over the end if the loop is finished
									x = flow.startIdx
									continue
								}
								x = x + 1
								continue
								break
							case "exit":
								//we need to find the closest earlier loop or switch and get out of it
								x = null
								continue
						}
					}

					//ignore the command
					command = null
					x += 1
					continue
				} else if (command && command.immediate) {
					//only execute task in certain modes?
					//only execute task on certain days?
					def restricted = (task.m && !(location.mode in task.m)) || (task.d && task.d.size() && !(getDayOfWeekName() in task.d))
					def function = "cmd_${sanitizeCommandName(command.name)}"
					def result = "$function"(action, task, time)
					if (!restricted) {
						time = (result && result.time) ? result.time : time
						command.delay = (result && result.delay) ? result.delay : 0
						if (result && result.waitFor) {
							waitFor = result.waitFor
							waitSince = time
						}
					}
					if (!result.schedule) {
						command = null
					}
				}
			} else {
				if (custom) {
					command = [name: cmd]
				} else {
					command = getCommandByDisplay(cmd)
				}
			}
			if (command) {
				for (deviceId in deviceIds) {
					def data = task.p && task.p.size() ? [p: task.p] : null
					if (waitFor) {
						data = data ? data : [:]
						data.w = waitFor //what to wait for
						data.o = time - waitSince //delay after state change
					}
					if (action.tcp && action.tcp != "None") {
						data = data ? data : [:]
						data.c = action.tcp.contains("piston")
						data.cc = action.tcp.contains("condition") ? action.pid : null
					}
					if (command.aggregated) {
						//an aggregated command schedules one command task for the whole group
						deviceId = null
					}
					def restricted = (task.m && !(location.mode in task.m)) || (task.d && task.d.size() && !(getDayOfWeekName() in task.d))
					if (!restricted && (!command.delay) && (time == rightNow) && (command.name == "setVariable") && (data.p) && (data.p.size() >= 3) && (data.p[2].d)) {
						//due to popular demand, we need to execute setVariable right during the condition evaluation so that subsequent evaluations can use the new values
						task_vcmd_setVariable(null, action, [data: data])
					} else {
						scheduleTask("cmd", action.id, deviceId, task.i, command.delay ? command.delay : time, data)
					}
					//an aggregated command schedules one command task for the whole group, so there's only one scheduled task, exit
					if (command.aggregated) break
				}
			}
			x += 1
			//exit when we reached the end
			if (x >= tasks.size()) {
				break
			}
		}
	}
}

private checkFlowCondition(task) {
	if (task.p && (task.p.size() == 3)) {
		def variable = task.p[0].d
		def comparison = task.p[1].d
		def value = formatMessage(task.p[2].d)
		return checkVariableCondition(variable, comparison, value)
	}
	return false
}

private checkVariableCondition(variable, comparison, value) {
	def varValue = getVariable(variable)
	return checkValueCondition(varValue, comparison, value)
}

private checkValueCondition(value1, comparison, value2) {
	value2 = formatMessage(value2)
	if (value1 instanceof String) {
		value2 = cast(value2, "string")
	} else if (value1 instanceof Boolean) {
		value2 = cast(value2, "boolean")
	} else if (value1 instanceof Integer) {
		value2 = cast(value2, "number")
	} else if (value1 instanceof Float) {
		value2 = cast(value2, "decimal")
	} else {
		value1 = cast(value1, "string")
		value2 = cast(value2, "string")
	}

	def func = "eval_cond_${sanitizeCommandName(comparison)}"
	def result = false
	try {
		result = "$func"(null, null, null, null, null, value1, value2, null, null, null, null, null)
	} catch (all) {
		result = false
	}
	return result
}

private checkFlowCaseCondition(value, caseValue) {
	return checkValueCondition(value, "is_equal_to", caseValue)
}

private checkTimeCondition(timeFrom, timeFromCustom, timeFromOffset, timeTo, timeToCustom, timeToOffset) {
	def time = adjustTime()
	//convert to minutes since midnight
	def tc = time.hours * 60 + time.minutes
	def tf
	def tt
	def i = 0
	while (i < 2) {
		def t = null
		def h = null
		def m = null
		switch(i == 0 ? timeFrom : timeTo) {
			case "custom time":
				t = adjustTime(i == 0 ? timeFromCustom : timeToCustom)
				if (i == 0) {
					timeFromOffset = 0
				} else {
					timeToOffset = 0
				}
				break
			case "sunrise":
				t = getSunrise()
				break
			case "sunset":
				t = getSunset()
				break
			case "noon":
				h = 12
				break
			case "midnight":
				h = (i == 0 ? 0 : 24)
				break
		}
		if (h != null) {
			m = 0
		} else {
			h = t.hours
			m = t.minutes
		}
		switch (i) {
			case 0:
				tf = h * 60 + m + cast(timeFromOffset, "number")
				break
			case 1:
				tt = h * 60 + m + cast(timeToOffset, "number")
				break
		}
		i += 1
	}
	//due to offsets, let's make sure all times are within 0-1440 minutes
	while (tf < 0) tf += 1440
	while (tf > 1440) tf -= 1440
	while (tt < 0) tt += 1440
	while (tt > 1440) tt -= 1440
	if (tf < tt) {
		return (tc >= tf) && (tc < tt)
	} else {
		return (tc < tt) || (tc >= tf)
	}
}

private buildFlowChart(tasks) {
	def result = []
	def indent = 0
	def idx = 0
	for (task in tasks) {
		def cmd = task.c
		def flow = [:]
		def found = false
		if (cmd && cmd.startsWith(virtualCommandPrefix())) {
			def command = getVirtualCommandByDisplay(cleanUpCommand(cmd))
			if (command && command.flow) {
				def c = command.name.toLowerCase()
				flow.c = c
				flow.action = (c.startsWith("begin") ? "begin" : (c.startsWith("end") ? "end" : (c.startsWith("break") ? "break" : (c.startsWith("exit") ? "exit" : null))))
				if (flow.action) {
					flow.isFor = c.contains("for")
					flow.isSimple = c.contains("simple")
					flow.isWhile = c.contains("while")
					flow.isLoop = c.contains("loop")
					flow.isIf = c.contains("if")
					flow.isElse = c.contains("else")
					flow.isSwitch = c.contains("switch")
					flow.isCase = c.contains("case")
					flow.loopType = (flow.isFor ? "for" : (flow.isWhile ? "while" : null))
					flow.ifType = (flow.isElse ? "else" : (flow.isIf ? "if" : null))
					flow.mode = (flow.isLoop ? "loop" : (flow.isIf ? "if" : (flow.isCase ? "case" : (flow.isSwitch ? "switch" : null))))
					if (flow.mode) {
						//ending flows need the indent applied before
						indent = indent + (command.indent && (command.indent < 0) ? command.indent : 0)
						flow.indent = indent - (flow.isElse ? 1 : (flow.isCase ? 2 : 0))
						flow.taskId = task.i
						flow.idx = idx
						found = true
						//beginning flows need the indent applied after
						indent = indent + (command.indent && (command.indent > 0) ? command.indent : 0)
					}
				}
			}
		}
		result.push(found ? flow : null)
		idx += 1
	}
	for (flow in result) {
		//initialize case array
		if (flow) {
			if (flow.isSwitch && !(flow.isCase) && (flow.action == "begin")) flow.caseIdxs = []
			for (def i = flow.idx + 1; i < result.size(); i++) {
				if (result[i] && (result[i].indent == flow.indent)) {
					def endFlow = result[i]
					def breakFor = false
					switch (flow.action) {
						case "begin":
							switch (flow.mode) {
								case "if":
									if (endFlow.isElse) {
										flow.elseIdx = i
										endFlow.startIdx = flow.idx
									}
									if (endFlow.isIf) {
										flow.endIdx = i
										endFlow.startIdx = flow.idx
									}
									break
								case "loop":
									if (endFlow.mode == "loop") {
										flow.endIdx = i
										endFlow.startIdx = flow.idx
									}
									break
								case "case":
									endFlow.startIdx = flow.idx
									flow.endIdx = i
									break
								case "switch":
									if ((flow.caseIdxs != null) && (endFlow.isCase)) {
										endFlow.startIdx = flow.idx
										flow.caseIdxs.push i
									} else if (endFlow.mode == "switch") {
										endFlow.startIdx = flow.idx
										flow.endIdx = i
									}
									break
							}
						case "break":
							break
					}
				}
				if (flow.endIdx) break
			}
		}
	}
	return result
}

private cmd_followUp(action, task, time) {
	def result = cmd_wait(action, task, time)
	result.schedule = true
	result.delay = result.time
	result.time = null
	return result
}

private cmd_wait(action, task, time) {
	def result = [:]
	if (task && task.p && task.p.size() >= 2) {
		def unit = 60000
		switch (task.p[1].d) {
			case "seconds":
				unit = 1000
				break
			case "minutes":
				unit = 60000
				break
			case "hours":
				unit = 3600000
				break
		}
		def offset = task.p[0].d * unit
		result.time = time + offset
	}
	return result
}

private cmd_waitVariable(action, task, time) {
	def result = [:]
	if (task && task.p && task.p.size() >= 2) {
		def unit = 60000
		switch (task.p[1].d) {
			case "seconds":
				unit = 1000
				break
			case "minutes":
				unit = 60000
				break
			case "hours":
				unit = 3600000
				break
		}
		def offset = (int) Math.round(cast(getVariable(task.p[0].d), "decimal") * unit)
		result.time = time + offset
	}
	return result
}

private cmd_waitRandom(action, task, time) {
	def result = [:]
	if (task && task.p && task.p.size() == 3) {
		def unit = 60000
		switch (task.p[2].d) {
			case "seconds":
				unit = 1000
				break
			case "minutes":
				unit = 60000
				break
			case "hours":
				unit = 3600000
				break
		}
		def min = task.p[0].d * unit
		def max = task.p[1].d * unit
		if (min > max) {
			//swap the numbers
			def x = min
			min = max
			max = x
		}
		def offset = (long)(min + Math.round(Math.random() * (max - min)))
		result.time = time + offset
	}
	return result
}

private cmd_waitTime(action, task, time) {
	def result = [time: time]
	if (task && task.p && task.p.size() == 3) {
		def t = cast(task.p[0].d, "string")
		def offset = cast(task.p[1].d, "number")
		def days = task.p[2].d
		if (!days || !days.size()) {
			return result
		}
		def newTime = getVariable("\$next" + t.capitalize())
		def rightNow = now()
		newTime += offset * 60000
		def date = adjustTime(newTime)
		def count = 10
		while ((newTime < rightNow) || (newTime < time) || !(getDayOfWeekName(date) in days)) {
			newTime += 86400000
			date = adjustTime(newTime)
			count -= 1
			if (count == 0) {
				return result
			}
		}
		result.time = newTime
	}
	return result
}

private cmd_waitCustomTime(action, task, time) {
	def result = [time: time]
	if (task && task.p && task.p.size() == 2) {
		def newTime = convertDateToUnixTime(adjustTime(task.p[0].d))
		def days = task.p[1].d
		if (!days || !days.size()) {
			return result
		}
		def date = adjustTime(newTime)
		def rightNow = now()
		def count = 10
		while ((newTime < rightNow) || (newTime < time) || !(getDayOfWeekName(date) in days)) {
			newTime += 86400000
			date = adjustTime(newTime)
			count -= 1
			if (count == 0) {
				return result
			}
		}
		result.time = newTime
	}
	return result
}

private cmd_waitState(action, task, time) {
	def result = [:]
	if (task && task.p && task.p.size() == 1) {
		def state = "${task.p[0].d}"
		if (state.contains("any")) {
			result.waitFor = "a"
		}
		if (state.contains("true")) {
			result.waitFor = "t"
		}
		if (state.contains("false")) {
			result.waitFor = "f"
		}
	}
	return result
}

private scheduleTask(task, ownerId, deviceId, taskId, unixTime, data = null) {
	if (!unixTime) return false
	if (!state.tasker) {
		state.tasker = []
		state.taskerIdx = 0
	}
	//get next index for task ordering
	def idx = state.taskerIdx
	state.taskerIdx = idx + 1
	state.tasker.push([idx: idx, add: task, ownerId: ownerId, deviceId: deviceId, taskId: taskId, data: data, time: unixTime, created: now()])
	return true
}

private unscheduleTask(task, ownerId, deviceId) {
	if (!state.tasker) {
		state.tasker = []
		state.taskerIdx = 0
	}
	def idx = state.taskerIdx
	state.taskerIdx = idx + 1
	state.tasker.push([idx: idx, del: task, ownerId: ownerId, deviceId: deviceId, created: now()])
}

private getNextTimeConditionTime(condition, startTime = null) {
	def perf = now()

	//no condition? not time condition? false!
	if (!condition || (condition.attr != "time")) {
		return null
	}
	//get UTC now if no unixTime is provided
	def unixTime = startTime ? startTime : now()
	//remove the seconds...
	unixTime = unixTime - unixTime.mod(60000)
	//we give it up to 25 hours to find the next time when the condition state would change
	//optimized procedure - limitations : this will only trigger on strict condition times, without actually accounting for time restrictions...
	return evaluateTimeCondition(condition, null, unixTime, true)
}

private getNextTimeTriggerTime(condition, startTime = null) {
	//no condition? not time condition? false!
	if (!condition || (condition.attr != "time")) {
		return null
	}
	//get UTC now if no unixTime is provided
	def unixTime = startTime ? startTime : now()
	//convert that to location's timezone, for comparison
	def currentTime = adjustTime()
	def now = adjustTime(unixTime)
	def attr = getAttributeByName(condition.attr)
	def comparison = cleanUpComparison(condition.comp)
	def comp = getComparisonOption(condition.attr, comparison)
	//if we can't find the attribute (can't be...) or the comparison object, or we're not dealing with a trigger, exit stage null
	if (!attr || !comp || comp.trigger != comparison) {
		return null
	}

	def val1 = "${condition.val1}"
	def repeat = (condition.val1 && val1.contains("every") ? val1 : "${condition.r}")
	if (!repeat) {
		return null
	}
	def interval = cast((repeat.contains("number") ? (condition.val1 && "${condition.val1}".contains("every") ? condition.e : condition.re) : 1), "number")
	if (!interval) {
		return null
	}
	repeat = repeat.replace("every ", "").replace("number of ", "").replace("s", "")

	//do the work
	def maxCycles = null
	while ((maxCycles == null) || (maxCycles > 0)) {
		def cycles = 1
		def repeatCycle = false
		if (repeat == "minute") {
			//increment minutes
			//we need to catch up with the present
			def pastMinutes = (long) (Math.floor((currentTime.time - now.time) / 60000))
			if (pastMinutes > interval) {
				if (interval > 0) {
					now = new Date(now.time + interval * (long) Math.floor(pastMinutes / interval) * 60000)
				} else {
					now = new Date(now.time + pastMinutes * 60000)
				}
			}
			now = new Date(now.time + interval * 60000)
			cycles = 1500 //up to 25 hours
		} else if (repeat == "hour") {
			//increment hours
			def m = now.minutes
			def rm = (condition.m ? condition.m : "0").toInteger()
			def pastHours = (long) (Math.floor((currentTime.time - now.time) / 3600000))
			if (pastHours > interval) {
				if (interval > 0) {
					now = new Date(now.time + interval * (long) Math.floor(pastHours / interval) * 60000)
				} else {
					now = new Date(now.time + pastHours * 60000)
				}
			}
			now = new Date(now.time + (m < rm ? interval - 1 : interval) * 3600000)
			now = new Date(now.year, now.month, now.date, now.hours, rm, 0)
			cycles = 744
		} else {
			//we're repeating at a granularity larger or equal to a day
			//we need the time of the day at which things happen
			def h = 0
			def m = 0
			def offset = 0
			def customTime = null
			def useDate = false
			switch (val1) {
				case "custom time":
					if (!condition.t1) {
						return null
					}
					customTime = adjustTime(condition.t1)
					break
				case "sunrise":
					customTime = getSunrise()
					offset = condition.o1 ? condition.o1 : 0
					break
				case "sunset":
					customTime = getSunset()
					offset = condition.o1 ? condition.o1 : 0
					break
				case "noon":
					h = 12
					offset = condition.o1 ? condition.o1 : 0
					break
				case "midnight":
					offset = condition.o1 ? condition.o1 : 0
					break
				case "time of variable":
					customTime = adjustTime(getVariable(condition.var1))
					offset = condition.o1 ? condition.o1 : 0
					break
				case "date and time of variable":
					customTime = adjustTime(getVariable(condition.var1))
					offset = condition.o1 ? condition.o1 : 0
					useDate = true
					repeat = "none"
					break
			}

			if (customTime) {
				h = customTime.hours
				m = customTime.minutes
			}
			//we now have the time of the day
			//let's figure out the next day

			//we need a - one day offset if now is before the required time
			//since today could still be a candidate
			now = (now.hours * 60 - h * 60 + now.minutes - m - offset < 0) ? now - 1 : now
			now = useDate ? customTime : new Date(now.year, now.month, now.date, h, m, 0)

			//apply the offset
			if (offset) {
				now = new Date(now.time + offset * 60000)
			}

			if (useDate && (now < currentTime)) {
				//using date and that date is past...
				return null
			}

			switch (repeat) {
				case "day":
					now = now + interval
					cycles = 1095
					break
				case "week":
					def dow = now.day
					def rdow = getDayOfWeekNumber(condition.rdw)
					if (rdow == null) {
						return null
					}
					now = now + (rdow <= dow ? rdow + 7 - dow : rdow - dow) + (interval - 1) * 7
					cycles = 520
					break
				case "month":
					def day = condition.rd
					if (!day) {
						return null
					}
					if (day.contains("week")) {
						def rdow = getDayOfWeekNumber(condition.rdw)
						if (rdow == null) {
							return null
						}
						//we're using Nth week day of month
						def week = 1
						if (day.contains("first")) {
							week = 1
						} else if (day.contains("second")) {
							week = 2
						} else if (day.contains("third")) {
							week = 3
						} else if (day.contains("fourth")) {
							week = 4
						} else if (day.contains("fifth")) {
							week = 5
						}
						if (day.contains("last")) {
							week = -week
						}
						def intervalOffset = 0
						def d = getDayInWeekOfMonth(now, week, rdow)
						//get a possible date this month
						if (d && (new Date(now.year, now.month, d, now.hours, now.minutes, 0) > now)) {
							//at this point, the next month is this month (lol), we need to remove one from the interval
							intervalOffset = 1
						}

						//get the day of the next required month
						d = getDayInWeekOfMonth(new Date(now.year, now.month + interval - intervalOffset, 1, now.hours, now.minutes, 0), week, rdow)
						if (d) {
							now = new Date(now.year, now.month + interval - intervalOffset, d, now.hours, now.minutes, 0)
						} else {
							now = new Date(now.year, now.month + interval - intervalOffset, 1, now.hours, now.minutes, 0)
							repeatCycle = true
						}
					} else {
						//we're specifying a day
						def d = 1
						if (day.contains("last")) {
							//going backwards
							if (day.contains("third")) {
								d = -2
							} else if (day.contains("third")) {
								d = -1
							} else {
								d = 0
							}
							def intervalOffset = 0
							//get the last day of this month
							def dd = (new Date(now.year, now.month + 1, d)).date
							if (new Date(now.year, now.month, dd, now.hours, now.minutes, 0) > now) {
								//at this point, the next month is this month (lol), we need to remove one from the interval
								intervalOffset = 1
							}
							//get the day of the next required month
							d = (new Date(now.year, now.month + interval - intervalOffset + 1, d)).date
							now = new Date(now.year, now.month + interval - intervalOffset, d, now.hours, now.minutes, 0)
						} else {
							//the day is in the string
							day = day.replace("on the ", "").replace("st", "").replace("nd", "").replace("rd", "").replace("th", "")
							if (!day.isInteger()) {
								//error
								return null
							}
							d = day.toInteger()
							now = new Date(now.year, now.month + interval - (d > now.date ? 1 : 0), d, now.hours, now.minutes, 0)
							if (d > now.date) {
								//we went overboard, this month does not have so many days, repeat the cycle to move on to the next month that does
								repeatCycle = true
							}
						}
					}
					cycles = 36
					break
				case "year":
					def day = condition.rd
					if (!day) {
						return null
					}
					if (!condition.rm) {
						return null
					}
					def mo = getMonthNumber(condition.rm)
					if (mo == null) {
						return null
					}
					mo--
					if (day.contains("week")) {
						def rdow = getDayOfWeekNumber(condition.rdw)
						if (rdow == null) {
							return null
						}
						//we're using Nth week day of month
						def week = 1
						if (day.contains("first")) {
							week = 1
						} else if (day.contains("second")) {
							week = 2
						} else if (day.contains("third")) {
							week = 3
						} else if (day.contains("fourth")) {
							week = 4
						} else if (day.contains("fifth")) {
							week = 5
						}
						if (day.contains("last")) {
							week = -week
						}
						def intervalOffset = 0
						def d = getDayInWeekOfMonth(new Date(now.year, mo, now.date, now.hours, now.minutes, 0), week, rdow)
						//get a possible date this year
						if (d && (new Date(now.year, mo, d, now.hours, now.minutes, 0) > now)) {
							//at this point, the next month is this month (lol), we need to remove one from the interval
							intervalOffset = 1
						}

						//get the day of the next required month
						d = getDayInWeekOfMonth(new Date(now.year + interval - intervalOffset, mo, 1, now.hours, now.minutes, 0), week, rdow)
						if (d) {
							now = new Date(now.year + interval - intervalOffset, mo, d, now.hours, now.minutes, 0)
						} else {
							now = new Date(now.year + interval - intervalOffset, mo, 1, now.hours, now.minutes, 0)
							repeatCycle = true
						}
					} else {
						//we're specifying a day
						def d = 1
						if (day.contains("last")) {
							//going backwards
							if (day.contains("third")) {
								d = -2
							} else if (day.contains("third")) {
								d = -1
							} else {
								d = 0
							}
							def intervalOffset = 0
							//get the last day of specified month
							def dd = (new Date(now.year, mo + 1, d)).date
							if (new Date(now.year, mo, dd, now.hours, now.minutes, 0) > now) {
								//at this point, the next month is this month (lol), we need to remove one from the interval
								intervalOffset = 1
							}
							//get the day of the next required month
							d = (new Date(now.year + interval - intervalOffset, mo + 1, d)).date
							now = new Date(now.year + interval - intervalOffset, mo, d, now.hours, now.minutes, 0)
						} else {
							//the day is in the string
							day = day.replace("on the ", "").replace("st", "").replace("nd", "").replace("rd", "").replace("th", "")
							if (!day.isInteger()) {
								//error
								return null
							}
							d = day.toInteger()
							now = new Date(now.year + interval - ((d > now.date) && (now.month == mo) ? 1 : 0), mo, d, now.hours, now.minutes, 0)
							if (d > now.date) {
								//we went overboard, this month does not have so many days, repeat the cycle to move on to the next month that does
								if (d > 29) {
									//no year ever will have this day on the selected month
									return null
								}
								repeatCycle = true
							}
						}
					}
					cycles = 10
					break
			}
		}

		//check if we have to repeat or exit
		if ((!repeatCycle) && testDateTimeFilters(condition, now)) {
			//make it UTC Unix Time
			def result = convertDateToUnixTime(now)
			//we only provide a time in the future
			//if we weren't, we'd be hogging everyone trying to keep up
			if (result >= (new Date()).time + 2000) {
				return result
			}
		}
		maxCycles = (maxCycles == null ? cycles : maxCycles) - 1
	}
}

def keepAlive() {
	state.run = "app"
	processTasks()
}

private processTasks() {
	//pfew, off to process tasks
	//first, we make a variable to help us pick up where we left off
	state.rerunSchedule = false
	def appData = state.run == "config" ? state.config.app : state.app
	def tasks = null
	def perf = now()
	def marker = now()
	debug "Processing tasks (${version()})", 1, "trace"
	try {

		def safetyNet = false

		//find out if we need to execute the tasks
		def restricted = (checkPistonRestriction() != null)
		state.restricted = restricted
		def executeTasks = !appData.restrictions?.pe || !restricted

		//let's give now() a 2s bump up so that if anything is due within 2s, we do it now rather than scheduling ST
		def threshold = 2000

		//we're off to process any pending immediate EVENTS ONLY
		//we loop a seemingly infinite loop
		//no worries, we'll break out of it, maybe :)
		while (true) {
			//we need to read the list every time we get here because the loop itself takes time.
			//we always need to work with a fresh list.
			tasks = atomicState.tasks
			tasks = tasks ? tasks : [:]
			for (item in tasks.findAll{it.value?.type == "evt"}.sort{ it.value?.time }) {
				def task = item.value
				if (task.time <= now() + threshold) {
					//remove from tasks
					tasks.remove(item.key)
					atomicState.tasks = tasks
					//throw away the task list as this procedure below may take time, making our list stale
					//not to worry, we'll read it again on our next iteration
					tasks = null
					//since we may timeout here, install the safety net
					if (!safetyNet) {
						safetyNet = true
						debug "Installing ST safety net", null, "trace"
						runIn(90, recoveryHandler)
					}
					//trigger an event
					if (!restricted) {
						if (getCondition(task.ownerId, true)) {
							//look for condition in primary block
							debug "Broadcasting time event for primary IF block, condition #${task.ownerId}, task = $task", null, "trace"
							broadcastEvent([name: "time", date: new Date(task.time), deviceId: task.deviceId ? task.deviceId : "time", conditionId: task.ownerId], true, false)
						} else if (getCondition(task.ownerId, false)) {
							//look for condition in secondary block
							debug "Broadcasting time event for secondary IF block, condition #${task.ownerId}", null, "trace"
							broadcastEvent([name: "time", date: new Date(task.time), deviceId: task.deviceId ? task.deviceId : "time", conditionId: task.ownerId], false, true)
						} else {
							debug "ERROR: Time event cannot be processed because condition #${task.ownerId} does not exist", null, "error"
						}
					} else {
						debug "Not broadcasting event due to restrictions"
					}
					//continue the loop
					break
				}
			}
			//well, if we got here, it means there's nothing to do anymore
			if (tasks != null) break
		}

		//okay, now let's give the time triggers a chance to readjust
		if (state.app?.enabled && (state.app?.mode != "Follow-Up")) {
			scheduleTimeTriggers()
		}

		//read the tasks
		tasks = atomicState.tasks
		tasks = tasks ? tasks : [:]
		def idx = 1
		//find the last index
		for(task in tasks) {
			if ((task.value?.idx) && (task.value?.idx >= idx)) {
				idx = task.value?.idx + 1
			}
		}

		def repeatCount = 0

		while (repeatCount < 2) {
			//we allow some tasks to rerun this code because they're altering our task list...
			//then if there's any pending tasks in the tasker, we look them up too and merge them to the task list
			tasks = atomicState.tasks
			tasks = tasks ? tasks : [:]
			if (state.tasker && state.tasker.size()) {
				for (task in state.tasker.sort{ it.idx }) {
					if (task.add) {
						def t = cleanUpMap([type: task.add, idx: idx, ownerId: task.ownerId, deviceId: task.deviceId, taskId: task.taskId, time: task.time, created: task.created, data: task.data, marker: (task.time < now() + threshold ? marker : null)])
						//def n = "${task.add}:${task.ownerId}${task.deviceId ? ":${task.deviceId}" : ""}${task.taskId ? "#${task.taskId}" : ""}:${task.idx}:$idx"
						def n = "t$idx"
						idx += 1
						tasks[n] = t
					} else if (task.del) {
						//delete a task
						def washer = []
						for (it in tasks) {
							if (
							(it.value?.type == task.del) &&
								(!task.ownerId || (it.value?.ownerId == task.ownerId)) &&
								//(task.ownerId || (task.deviceId != "location")) && //do not unschedule location commands unless an action Id is provided
								(!task.deviceId || (task.deviceId == it.value?.deviceId)) &&
								(!task.taskId || (task.taskId == it.value?.taskId))
							) {
								washer.push(it.key)
							}
						}
						for (it in washer) {
							tasks.remove(it)
						}
						washer = null
						/*
						def dirty = true
						while (dirty) {
							dirty = false
							for (it in tasks) {
								if (
										(it.value?.type == task.del) &&
										(!task.ownerId || (it.value?.ownerId == task.ownerId)) &&
										//(task.ownerId || (task.deviceId != "location")) && //do not unschedule location commands unless an action Id is provided
										(!task.deviceId || (task.deviceId == it.value?.deviceId)) &&
										(!task.taskId || (task.taskId == it.value?.taskId))
								) {
									tasks.remove(it.key)
									dirty = true
									break
								}
							}
						}
                        */
					}
				}
				//we save the tasks list atomically, ouch
				//this is to avoid spending too much time with the tasks list on our hands and having other instances
				//running and modifying the old list that we picked up above
				state.tasksProcessed = now()
				atomicState.tasks = tasks
				//state.tasks = tasks
				state.tasker = null
			}

			//time to see if there is any ST schedule needed for the future
			def nextTime = null
			def immediateTasks = 0
			def thresholdTime = now() + threshold
			for (item in tasks) {
				def task = item.value
				//if a command task is waiting, we ignore it
				if (!task.data || !task.data.w) {
					//if a task is already due, we keep track of it
					if (task.time <= thresholdTime) {
						if (task.marker in [null, marker]) {
							//we only handle our own tasks or no ones tasks
							immediateTasks += 1
						}
					} else {
						//we try to get the nearest time in the future
						nextTime = (nextTime == null) || (nextTime > task.time) ? task.time : nextTime
					}
				}
			}
			//if we found a time that's after
			if (nextTime) {
				def seconds = Math.ceil((nextTime - now()) / 1000)
				runIn(seconds, timeHandler)
				atomicState.nextScheduledTime = nextTime
				state.nextScheduledTime = nextTime
				setVariable("\$nextScheduledTime", nextTime, true)
				debug "Scheduling ST job to run in ${seconds}s, at ${formatLocalTime(nextTime)}", null, "info"
			} else {
				setVariable("\$nextScheduledTime", null, true)
				atomicState.nextScheduledTime = null
				state.nextScheduledTime = nextTime
				unschedule(timeHandler)
			}

			//we're done with the scheduling, let's do some real work, if we have any
			if (immediateTasks) {
				if (!safetyNet) {
					//setup a safety net ST schedule to resume the process if we fail
					safetyNet = true
					debug "Installing ST safety net", null, "trace"
					runIn(90, recoveryHandler)
				}

				debug "Found $immediateTasks task${immediateTasks > 1 ? "s" : ""} due at this time"
				//we loop a seemingly infinite loop
				//no worries, we'll break out of it, maybe :)
				def found = true
				while (found) {
					found = false
					//we need to read the list every time we get here because the loop itself takes time.
					//we always need to work with a fresh list. Using a ? would not read the list the first time around (optimal, right?)
					tasks = atomicState.tasks
					tasks = tasks ? tasks : [:]
					def firstTask = tasks.sort{ it.value.time }.find{ (it.value.type == "cmd") && (!it.value.data || !it.value.data.w) && (it.value.time <= (now() + threshold)) && (it.value.marker in [null, marker]) }
					if (firstTask) {
						def firstSubTask = tasks.sort{ it.value.idx }.find{ (it.value.type == "cmd") && (!it.value.data || !it.value.data.w) && (it.value.time == firstTask.value.time) && (it.value.marker in [null, marker]) }
						if (firstSubTask) {
							def task = firstSubTask.value
							//remove from tasks
							tasks = atomicState.tasks
							tasks.remove(firstSubTask.key)
							atomicState.tasks = tasks
							//throw away the task list as this procedure below may take time, making our list stale
							//not to worry, we'll read it again on our next iteration
							tasks = null
							//do some work


							def enabled = (state.app && (state.app.enabled != null) ? !!state.app.enabled : true) && executeTasks

							if (enabled && (task.type == "cmd")) {
								debug "Processing command task $task"
								try {
									processCommandTask(task)
								} catch (e) {
									debug "ERROR: Error while processing command task: ", null, "error", e
								}
							}
							//repeat the while since we just modified the task
							found = true
						}
					}
				}
			}
			if (!state.rerunSchedule) break
			repeatCount += 1
		}
		//would you look at that, we finished!
		//remove the safety net, wasn't worth the investment

		//remove the markers
		tasks = atomicState.tasks
		def found = false
		for(it in tasks.findAll{ it.value.marker == marker }) {
			def task = it.value
			task.marker = null
			tasks[it.key] = task
			found = true
		}
		if (found) atomicState.tasks = tasks

		//DO NOT REMOVE THE NEXT LINE - we need this line for instances that do not run the exitPoint()
		state.tasks = tasks

		debug "Removing any existing ST safety nets", null, "trace"
		unschedule(recoveryHandler)
	} catch (e) {
		debug "ERROR: Error while executing processTasks: ", null, "error", e
	}
	state.tasker = null
	//end of processTasks
	perf = now() - perf
	debug "Task processing took ${perf}ms", -1, "trace"
	return true
}

private cancelTasks(state) {
	def tasks = atomicState.tasks
	tasks = tasks ? tasks : [:]
	//debug "Resuming tasks on piston state change, resumable states are $resumableStates", null, "trace"
	while (true) {
		def item = tasks.find{ (it.value.type == "cmd") && (it.value.data && it.value.data.c)}
		if (item) {
			tasks.remove(item.key)
		} else {
			break
		}
	}
	atomicState.tasks = tasks
}

private resumeTasks(state) {
	def tasks = atomicState.tasks
	tasks = tasks ? tasks : [:]
	def resumableStates = ["a", (state ? "t" : "f")]
	//debug "Resuming tasks on piston state change, resumable states are $resumableStates", null, "trace"
	def time = now()
	def list = tasks.findAll{ (it.value.type == "cmd") && (it.value.data && (it.value.data.w in resumableStates))}
	//todo: support for multiple wait for state commands during same action
	if (list.size()) {
		for (item in list) {
			tasks[item.key].time = time + (tasks[item.key].data.o ? tasks[item.key].data.o  : 0)
			tasks[item.key].data.w = null
			tasks[item.key].data.o = null
		}
		atomicState.tasks = tasks
	}
}

//the heavy lifting of commands
//this executes each and every single command we have to give
private processCommandTask(task) {
	def action = getAction(task.ownerId)
	if (!action) return false
	if (!action.t) return false
	def devices = listActionDevices(action.id)
	def device = devices.find{ it.id == task.deviceId }
	def t = action.t.find{ it.i == task.taskId }
	if (!t) return false
	//only execute task in certain modes?
	if (t.m && !(location.mode in t.m)) return false
	//only execute task on certain days?
	if (t.d && t.d.size() && !(getDayOfWeekName() in t.d)) return false
	//found the actual task, let's figure out what command we're running
	def cmd = t.c
	def virtual = (cmd && cmd.startsWith(virtualCommandPrefix()))
	def custom = (cmd && cmd.startsWith(customCommandPrefix()))
	cmd = cleanUpCommand(cmd)
	def command = null

	if (virtual) {
		//dealing with a virtual command
		command = getVirtualCommandByDisplay(cmd)
		if (command) {
			//we can't run immediate tasks here
			//execute the virtual task
			def cn = command.name
			def suffix = ""
			if (cn.contains("#")) {
				//multi command
				def parts = cn.tokenize("#")
				if (parts.size() == 2) {
					cn = parts[0]
					suffix = parts[1]
				}
			}
			def msg = "Executing virtual command ${cn}"
			def function = "task_vcmd_${sanitizeCommandName(cn)}"
			def perf = now()
			try {
				def result = "$function"(command.aggregated ? devices : device, action, task, suffix)
			} catch (all) {
				msg += " (ERROR EXECUTING TASK $task: $all)"
			}
			msg += " (${now() - perf}ms)"
			if (state.sim) state.sim.cmds.push(msg)
			debug msg, null, "info"
			return result
		}
	} else {
		if (custom) {
			def availableParams = t.p ? t.p.size() : 0
			def params = []
			if (availableParams && (availableParams.mod(2) == 0)) {
				for (def i = 0; i < Math.floor(availableParams / 2); i++) {
					def type = t.p[i * 2].d
					def value = t.p[i * 2 + 1].d
					params.push cast(value, type)
				}
			}
			def msg = "Executing custom command: [${device}].${cmd}(${params.size() ? params : ""})"
			def perf = now()
			try {
				if (params.size()) {
					device."${cmd}"(params as Object[])
				} else {
					device."${cmd}"()
				}
			} catch (all) {
				msg += " (ERROR EXECUTING TASK $task: $all)"
			}
			msg += " (${now() - perf}ms)"
			if (state.sim) state.sim.cmds.push(msg)
			debug msg, null, "info"
			return true
		}
		command = getCommandByDisplay(cmd)
		if (command) {
			def cn = command.name
			if (cn && cn.contains(".")) {
				def parts = cn.tokenize(".")
				cn = parts[parts.size() - 1]
			}
			if (device.hasCommand(cn)) {
				def requiredParams = command.parameters ? command.parameters.size() : 0
				def availableParams = t.p ? t.p.size() : 0
				if (requiredParams == availableParams) {
					def params = []
					t.p.sort{ it.i }.findAll() {
						params.push(it.d instanceof String ? formatMessage(it.d) : it.d)
					}
					if (params.size()) {
						if ((cn == "setColor") && (params.size() == 5)) {
							//using a little bit of a hack here
							//we should have 5 parameters:
							//color name
							//color rgb
							//hue
							//saturation
							//lightness
							def name = params[0]
							def hex = params[1]
							def hue = (int) Math.round(params[2] instanceof Integer ? params[2] / 3.6 : 0)
							def saturation = params[3]
							def lightness = params[4]
							def p = [:]
							if (name) {
								def color = getColorByName(name, task.ownerId, task.taskId)
								p.hue = (int) Math.round(color.h / 3.6)
								p.saturation = color.s
								//ST wrongly calls this level - it's lightness
								p.level = color.l
							} else if (hex) {
								p.hex = hex
							} else {
								p.hue = hue
								p.saturation = saturation
								p.level = lightness
							}
							def msg = "Executing command: [${device}].${cn}($p)"
							def perf = now()
							try {
								device."${cn}"(p)
							} catch(all) {
								msg += " (ERROR EXECUTING TASK $task: $all)"
							}
							msg += " (${now() - perf}ms)"
							if (state.sim) state.sim.cmds.push(msg)
							debug msg, null, "info"
						} else {
							def perf = now()
							if ((cn == "setHue") && (params.size() == 1)) {
								//ST expects hue in 0.100, in reality, it is 0..360
								params[0] = cast(params[0], "decimal") / 3.6
							}
							def doIt = true
							def msg
							if (!state.app?.disableCO && command.attribute && (params.size() == 1)) {
								//we may be able to avoid executing this command
								def currentValue = "${device.currentValue(command.attribute)}"
								if (cn == "setLevel") {
									//setLevel is handled differently. Even if we have the same value, but the switch would flip, we need to let it execute
									if (device.currentValue("switch") == (params[0] > 0 ? "off" : "on")) currentValue = null //we fake the current value to allow execution
								}
								if (currentValue == "${params[0]}") {
									doIt = false
									msg = "Preventing execution of command [${getDeviceLabel(device)}].${command.name}($params) because current value is the same"
								}
							}
							if (doIt) {
								msg = "Executing command: [${getDeviceLabel(device)}].${cn}($params)"
								try {
									device."${cn}"(params as Object[])
								} catch(all) {
									msg += " (ERROR EXECUTING TASK $task: $all)"
								}
								msg += " (${now() - perf}ms)"
							}
							if (state.sim) state.sim.cmds.push(msg)
							debug msg, null, "info"
						}
						return true
					} else {
						def doIt = true
						def msg
						if (!state.app?.disableCO && command.attribute && command.value) {
							//we may be able to avoid executing this command
							def currentValue = "${device.currentValue(command.attribute)}"
							if (currentValue == command.value) {
								doIt = false
								msg = "Preventing execution of command [${getDeviceLabel(device)}].${command.name}() because current value is the same"
							}
						}
						if (doIt) {
							msg = "Executing command: [${getDeviceLabel(device)}].${cn}()"
							def perf = now()
							try {
								device."${cn}"()
							} catch(all) {
								msg += " (ERROR EXECUTING TASK $task: $all)"
							}
							msg += " (${now() - perf}ms)"
						}
						if (state.sim) state.sim.cmds.push(msg)
						debug msg, null, "info"
						return true
					}
				}
			}
		}
	}
	return false
}

private task_vcmd_toggle(device, action, task, suffix = "") {
	if (!device || !device.hasCommand("on$suffix") || !device.hasCommand("off$suffix")) {
		//we need a device that has both on and off commands
		return false
	}
	if (device.currentValue("switch") == "on") {
		device."off$suffix"()
	} else {
		device."on$suffix"()
	}
	return true
}

private task_vcmd_toggleLevel(device, action, task, suffix = "") {
	def params = (task && task.data && task.data.p && task.data.p.size()) ? task.data.p : []
	if (!device || !device.hasCommand("on$suffix") || !device.hasCommand("off$suffix") || !device.hasCommand("setLevel") || (params.size() != 1)) {
		//we need a device that has both on and off commands
		return false
	}
	def level = params[0].d
	if (device.currentValue("switch") == "on") {
		device."off$suffix"()
	} else {
		device.setLevel(level)
		device."on$suffix"()
	}
	return true
}

private task_vcmd_delayedToggle(device, action, task, suffix = "") {
	def params = (task && task.data && task.data.p && task.data.p.size()) ? task.data.p : []
	if (!device || !device.hasCommand("on$suffix") || !device.hasCommand("off$suffix") || (params.size() != 1)) {
		//we need a device that has both on and off commands
		return false
	}
	def delay = params[0].d
	if (device.currentValue("switch") == "on") {
		device."off$suffix"([delay: delay])
	} else {
		device."on$suffix"([delay: delay])
	}
	return true
}

private task_vcmd_delayedOn(device, action, task, suffix = "") {
	def params = (task && task.data && task.data.p && task.data.p.size()) ? task.data.p : []
	if (!device || !device.hasCommand("on$suffix") || (params.size() != 1)) {
		//we need a device that has both on and off commands
		return false
	}
	def delay = params[0].d
	device."on$suffix"([delay: delay])
	return true
}

private task_vcmd_delayedOff(device, action, task, suffix = "") {
	def params = (task && task.data && task.data.p && task.data.p.size()) ? task.data.p : []
	if (!device || !device.hasCommand("off$suffix") || (params.size() != 1)) {
		//we need a device that has both on and off commands
		return false
	}
	def delay = params[0].d
	device."off$suffix"([delay: delay])
	return true
}

private task_vcmd_fadeLevelHW(device, action, task, suffix = "") {
	def params = (task && task.data && task.data.p && task.data.p.size()) ? task.data.p : []
	if (!device || !device.hasCommand("setLevel$suffix") || (params.size() != 2)) {
		return false
	}
	def level = cast(params[0].d, params[1].t)
	def duration = cast(params[1].d, params[1].t)
	//we're trying with a delay, not all devices support this
	try {
		device."setLevel$suffix"(level, duration)
	} catch(all) {
		//if not supported, we fallback onto the normal setLevel
		device."setLevel$suffix"(level)
	}
	return true
}

private task_vcmd_fadeLevelVariable(device, action, task, suffix = "") {
	return task_vcmd_fadeLevel(device, action, task, suffix, true)
}

private task_vcmd_fadeLevel(device, action, task, suffix = "", variables = false) {
	def params = (task && task.data && task.data.p && task.data.p.size()) ? task.data.p : []
	if (!device || !device.hasCommand("setLevel$suffix") || (params.size() != 3)) {
		return false
	}
	def currentLevel = variables ? (params[0].d ? cast(getVariable(params[0].d), "number") : null) : cast(params[0].d, params[0].t)
	if (currentLevel == null) currentLevel = cast(device.currentValue('level'), "number")
	def level = variables? cast(getVariable(params[1].d), "number") : cast(params[1].d, params[1].t)
	def duration = cast(params[2].d, params[2].t)
	def delta = level - currentLevel
	if (delta == 0) return
	//we try to achieve 10 steps
	def interval = Math.round(duration * 10)
	def minInterval = 1000 //min interval is 1s
	interval = interval > minInterval ? interval : minInterval
	def steps = Math.ceil(duration * 1000 / interval)
	//we're trying with a delay, not all devices support this
	if (steps > 1) {
		def oldLevel = currentLevel
		for(def i = 1; i <= steps; i++) {
			def newLevel = Math.round(currentLevel + delta * i / steps)
			if (oldLevel != newLevel) {
				device."setLevel$suffix"(newLevel, [delay: i * interval])
			}
			oldLevel = newLevel
		}
	}
	return true
}

private task_vcmd_setLevelIf(device, action, task, suffix = "") {
	def params = (task && task.data && task.data.p && task.data.p.size()) ? task.data.p : []
	if (!device || !device.hasCommand("setLevel$suffix") || (params.size() != 2)) {
		return false
	}
	def currentSwitch = cast(device.currentValue("switch"), "string")
	def level = cast(params[0].d, params[0].t)
	if (currentSwitch == cast(params[1].d, "string")) {
		device."setLevel$suffix"(level)
	}
	return true
}

private task_vcmd_adjustLevelVariable(device, action, task, suffix = "") {
	return task_vcmd_adjustLevel(device, action, task, suffix, true)
}

private task_vcmd_adjustLevel(device, action, task, suffix = "", variables = false) {
	def params = (task && task.data && task.data.p && task.data.p.size()) ? task.data.p : []
	if (!device || !device.hasCommand("setLevel$suffix") || (params.size() != 1)) {
		return false
	}
	def currentLevel = cast(device.currentValue('level'), "number")
	def level = currentLevel + (variables ? cast(getVariable(params[0].d), "number") : cast(params[0].d, params[0].t))
	level = (level < 0 ? 0 : (level > 100 ? 100 : level))
	if (level == currentLevel) return
	device."setLevel$suffix"(level)
	return true
}

private task_vcmd_setLevelVariable(device, action, task, suffix = "") {
	def params = (task && task.data && task.data.p && task.data.p.size()) ? task.data.p : []
	if (!device || !device.hasCommand("setLevel$suffix") || (params.size() != 1)) {
		return false
	}
	def level = cast(getVariable(params[0].d), "number")
	level = (level < 0 ? 0 : (level > 100 ? 100 : level))
	device."setLevel$suffix"(level)
	return true
}

private task_vcmd_setSaturationVariable(device, action, task, suffix = "") {
	def params = (task && task.data && task.data.p && task.data.p.size()) ? task.data.p : []
	if (!device || !device.hasCommand("setSaturation$suffix") || (params.size() != 1)) {
		return false
	}
	def saturation = cast(getVariable(params[0].d), "number")
	saturation = (saturation < 0 ? 0 : (saturation > 100 ? 100 : saturation))
	device."setSaturation$suffix"(level)
	return true
}

private task_vcmd_fadeSaturationVariable(device, action, task, suffix = "") {
	return task_vcmd_fadeSaturation(device, action, task, suffix, true)
}

private task_vcmd_fadeSaturation(device, action, task, suffix = "", variables = false) {
	def params = (task && task.data && task.data.p && task.data.p.size()) ? task.data.p : []
	if (!device || !device.hasCommand("setSaturation$suffix") || (params.size() != 3)) {
		return false
	}

	def currentSaturation = variables ? (params[0].d ? cast(getVariable(params[0].d), "number") : null) : cast(params[0].d, params[0].t)
	if (currentSaturation == null) currentSaturation = cast(device.currentValue('saturation'), "number")
	def saturation = variables? cast(getVariable(params[1].d), "number") : cast(params[1].d, params[1].t)
	def duration = cast(params[2].d, params[2].t)
	def delta = saturation - currentSaturation
	if (delta == 0) return
	//we try to achieve 10 steps
	def interval = Math.round(duration * 10)
	def minInterval = 1000 //min interval is 1s
	interval = interval > minInterval ? interval : minInterval
	def steps = Math.ceil(duration * 1000 / interval)
	//we're trying with a delay, not all devices support this
	if (steps > 1) {
		def oldSaturation = currentSaturation
		for(def i = 1; i <= steps; i++) {
			def newSaturation = Math.round(currentSaturation + delta * i / steps)
			if (oldSaturation != newSaturation) {
				device."setSaturation$suffix"(newSaturation, [delay: i * interval])
			}
			oldSaturation = newSaturation
		}
	}
	return true
}

private task_vcmd_adjustSaturationVariable(device, action, task, suffix = "") {
	return task_vcmd_adjustSaturation(device, action, task, suffix, true)
}

private task_vcmd_adjustSaturation(device, action, task, suffix = "", variables = false) {
	def params = (task && task.data && task.data.p && task.data.p.size()) ? task.data.p : []
	if (!device || !device.hasCommand("setSaturation$suffix") || (params.size() != 1)) {
		return false
	}
	def currentSaturation = cast(device.currentValue('saturation'), "number")
	def saturation = currentSaturation + (variables ? cast(getVariable(params[0].d), "number") : cast(params[0].d, params[0].t))
	saturation = (saturation < 0 ? 0 : (saturation > 100 ? 100 : saturation))
	if (saturation == currentSaturation) return
	device."setSaturation$suffix"(saturation)
	return true
}

private task_vcmd_fadeHueVariable(device, action, task, suffix = "") {
	return task_vcmd_fadeHue(device, action, task, suffix, true)
}

private task_vcmd_fadeHue(device, action, task, suffix = "", variables = false) {
	def params = (task && task.data && task.data.p && task.data.p.size()) ? task.data.p : []
	if (!device || !device.hasCommand("setHue$suffix") || (params.size() != 3)) {
		return false
	}
	def currentHue = variables ? (params[0].d ? cast(getVariable(params[0].d), "number") : null) : cast(params[0].d, params[0].t)
	if (currentHue == null) currentHue = (int) Math.round(cast(device.currentValue('hue'), "number") * 3.6)
	def hue = variables ? cast(getVariable(params[1].d), "number") : cast(params[1].d, params[1].t)
	def duration = cast(params[2].d, params[2].t)
	def delta = hue - currentHue
	if (delta == 0) return
	//we try to achieve 10 steps
	def interval = Math.round(duration * 10)
	def minInterval = 1000 //min interval is 1s
	interval = interval > minInterval ? interval : minInterval
	def steps = Math.ceil(duration * 1000 / interval)
	//we're trying with a delay, not all devices support this
	if (steps > 1) {
		def oldHue = currentHue
		for(def i = 1; i <= steps; i++) {
			def newHue = Math.round(currentHue + delta * i / steps)
			if (oldHue != newHue) {
				device."setHue$suffix"((int) Math.round(newHue / 3.6), [delay: i * interval])
			}
			oldHue = newHue
		}
	}
	return true
}

private task_vcmd_adjustHueVariable(device, action, task, suffix = "") {
	return task_vcmd_adjustHue(device, action, task, suffix, true)
}

private task_vcmd_adjustHue(device, action, task, suffix = "", variables = false) {
	def params = (task && task.data && task.data.p && task.data.p.size()) ? task.data.p : []
	if (!device || !device.hasCommand("setHue$suffix") || (params.size() != 1)) {
		return false
	}
	def currentHue = cast(device.currentValue('hue'), "decimal") * 3.6
	def hue = currentHue + (variables ? cast(getVariable(params[0].d), "number") : cast(params[0].d, params[0].t))
	while (hue < 0) hue += 360
	while (hue >= 360) hue -= 360
	if (hue == currentHue) return
	device."setHue$suffix"((int) Math.round(hue / 3.6))
	return true
}

private task_vcmd_setHueVariable(device, action, task, suffix = "") {
	def params = (task && task.data && task.data.p && task.data.p.size()) ? task.data.p : []
	if (!device || !device.hasCommand("setHue$suffix") || (params.size() != 1)) {
		return false
	}
	def hue = cast(getVariable(params[0].d), "number")
	while (hue < 0) hue += 360
	while (hue >= 360) hue -= 360
	device."setHue$suffix"((int) Math.round(hue / 3.6))
	return true
}

private task_vcmd_flash(device, action, task, suffix = "") {
	def params = (task && task.data && task.data.p && task.data.p.size()) ? task.data.p : []
	if (!device || !device.hasCommand("on$suffix") || !device.hasCommand("off$suffix") || (params.size() != 3)) {
		//we need a device that has both on and off commands
		//we also need three parameters
		//p[0] represents the on interval
		//p[1] represents the off interval
		//p[2] represents the number of flashes
		return false
	}
	def onInterval = params[0].d
	def offInterval = params[1].d
	def flashes = params[2].d
	def delay = 0
	def originalState = device.currentValue("switch")
	for (def i = 0; i < flashes; i++) {
		device."on$suffix"([delay: delay])
		delay = delay + onInterval
		device."off$suffix"([delay: delay])
		delay = delay + offInterval
	}
	if (originalState == "on") {
		device."on$suffix"([delay: delay])
	}
	return true
}

private task_vcmd_setLocationMode(devices, action, task, suffix = "") {
	def params = (task && task.data && task.data.p && task.data.p.size()) ? task.data.p : []
	if (params.size() != 1) {
		return false
	}
	def mode = params[0].d
	if (location.mode != mode) {
		location.setMode(mode)
		return true
	} else {
		debug "Not changing location mode because location is already in the $mode mode"
	}
	return false
}

private task_vcmd_setAlarmSystemStatus(devices, action, task, suffix = "") {
	def params = (task && task.data && task.data.p && task.data.p.size()) ? task.data.p : []
	if (params.size() != 1) {
		return false
	}
	def status = params[0].d
	if (getAlarmSystemStatus() != status) {
		setAlarmSystemStatus(status)
		return true
	} else {
		debug "WARNING: Not changing SHM's status because it already is $status", null, "warn"
	}
	return false
}

private task_vcmd_sendNotification(device, action, task, suffix = "") {
	def params = (task && task.data && task.data.p && task.data.p.size()) ? task.data.p : []
	if (params.size() != 1) {
		return false
	}
	def message = formatMessage(params[0].d)
	sendNotificationEvent(message)
}

private task_vcmd_sendPushNotification(device, action, task, suffix = "") {
	def params = (task && task.data && task.data.p && task.data.p.size()) ? task.data.p : []
	if (params.size() != 2) {
		return false
	}
	def message = formatMessage(params[0].d)
	def saveNotification = !!params[1].d
	if (saveNotification) {
		sendPush(message)
	} else {
		sendPushMessage(message)
	}
}

private task_vcmd_sendSMSNotification(device, action, task, suffix = "") {
	def params = (task && task.data && task.data.p && task.data.p.size()) ? task.data.p : []
	if (params.size() != 3) {
		return false
	}
	def message = formatMessage(params[0].d)
	def phones = "${params[1].d}".replace(" ", "").replace("-", "").replace("(", "").replace(")", "").tokenize(",;*|").unique()
	def saveNotification = !!params[2].d
	for(def phone in phones) {
		if (saveNotification) {
			sendSms(phone, message)
		} else {
			sendSmsMessage(phone, message)
		}
		//we only need one notification
		saveNotification = false
	}
}

private task_vcmd_sendNotificationToContacts(device, action, task, suffix = "") {
	def params = (task && task.data && task.data.p && task.data.p.size()) ? task.data.p : []
	if (params.size() != 3) {
		return false
	}
	def message = formatMessage(params[0].d)
	def recipients = settings["actParam${task.ownerId}#${task.taskId}-1"]
	def saveNotification = !!params[2].d
	try {
		sendNotificationToContacts(message, recipients, [event: saveNotification])
	} catch(all) {}
}

private task_vcmd_queueAskAlexaMessage(device, action, task, suffix = "") {
	def params = (task && task.data && task.data.p && task.data.p.size()) ? task.data.p : []
	if ((params.size() < 2) || (params.size() > 3)) {
		return false
	}
	def message = formatMessage(params[0].d)
	def unit = formatMessage(params[1].d)
	def appName = (params.size() == 3 ? formatMessage(params[2].d) : null) ?: (app.label ?: app.name)
	sendLocationEvent name: "AskAlexaMsgQueue", value: appName , isStateChange: true, descriptionText: message, unit: unit
}

private task_vcmd_deleteAskAlexaMessages(device, action, task, suffix = "") {
	def params = (task && task.data && task.data.p && task.data.p.size()) ? task.data.p : []
	if ((params.size() < 1) || (params.size() > 2)) {
		return false
	}
	def unit = formatMessage(params[0].d)
	def appName = (params.size() == 2 ? formatMessage(param[1].d) : null) ?: (app.label ?: app.name)
	sendLocationEvent name: "AskAlexaMsgQueueDelete", value: appName, isStateChange: true, unit: unit
}

private task_vcmd_executeRoutine(devices, action, task, suffix = "") {
	def params = (task && task.data && task.data.p && task.data.p.size()) ? task.data.p : []
	if (params.size() != 1) {
		return false
	}
	def routine = formatMessage(params[0].d)
	location.helloHome?.execute(routine)
	return true
}

private task_vcmd_followUp(devices, action, task, suffix = "") {
	def params = (task && task.data && task.data.p && task.data.p.size()) ? task.data.p : []
	if (params.size() != 4) {
		return false
	}
	def piston = params[2].d
	def result = execute(piston)
	//state.store = atomicState.store
	state.nextScheduledTime = atomicState.nextScheduledTime
	if (params[3].d) {
		setVariable(params[3].d, result)
	}
	return true
}

private task_vcmd_executePiston(devices, action, task, suffix = "") {
	def params = (task && task.data && task.data.p && task.data.p.size()) ? task.data.p : []
	if (params.size() != 2) {
		return false
	}
	def piston = params[0].d
	def result = execute(piston)
	//state.store = atomicState.store
	state.nextScheduledTime = atomicState.nextScheduledTime
	if (params[1].d) {
		setVariable(params[1].d, result)
	}
	return true
}

private task_vcmd_pausePiston(devices, action, task, suffix = "") {
	def params = (task && task.data && task.data.p && task.data.p.size()) ? task.data.p : []
	if (params.size() != 1) {
		return false
	}
	def piston = params[0].d
	def result = pausePiston(piston)
	return true
}

private task_vcmd_resumePiston(devices, action, task, suffix = "") {
	def params = (task && task.data && task.data.p && task.data.p.size()) ? task.data.p : []
	if (params.size() != 1) {
		return false
	}
	def piston = params[0].d
	def result = resumePiston(piston)
	return true
}

private task_vcmd_lifxScene(devices, action, task, suffix = "") {
	def params = (task && task.data && task.data.p && task.data.p.size()) ? task.data.p : []
	if (params.size() != 1) {
		return false
	}
	def sceneName = params[0].d
	def sceneId = getLifxSceneId(sceneName)
	def token = getLifxToken()
	if (sceneId != null) {
		def requestParams = [
			uri:  "https://api.lifx.com",
			path: "/v1/scenes/scene_id:${sceneId}/activate",
			headers: [
				"Authorization": "Bearer $token"
			]
		]
		try {
			return httpPut(requestParams) { response ->
				if (response.status == 200) {
					return true;
				}
				return false;
			}
		}
		catch(all) {
			return false
		}
	} else {
		debug "WARNING: LIFX Scene $sceneName could not be found", null, "warn"
	}
	return false
}

private task_vcmd_iftttMaker(devices, action, task, suffix = "") {
	def params = (task && task.data && task.data.p && task.data.p.size()) ? task.data.p : []
	if ((params.size() < 1) || (params.size() > 4)) {
		return false
	}
	def event = params[0].d
	def value1
	def value2
	def value3
	if (params.size() == 4) {
		value1 = formatMessage(params[1].d)
		value2 = formatMessage(params[2].d)
		value3 = formatMessage(params[3].d)
	}
	if (value1 || value2 || value3) {
		def requestParams = [
			uri:  "https://maker.ifttt.com/trigger/${event}/with/key/" + getIftttKey(),
			requestContentType: "application/json",
			body: [value1: value1, value2: value2, value3: value3]
		]
		httpPost(requestParams){ response ->
			setVariable("\$iftttStatusCode", response.status, true)
			setVariable("\$iftttStatusOk", response.status == 200, true)
		}
	} else {
		httpGet("https://maker.ifttt.com/trigger/${event}/with/key/" + getIftttKey()){ response ->
			setVariable("\$iftttStatusCode", response.status, true)
			setVariable("\$iftttStatusOk", response.status == 200, true)
		}
	}
	return true
}

private task_vcmd_httpRequest(devices, action, task, suffix = "") {
	def params = (task && task.data && task.data.p && task.data.p.size()) ? task.data.p : []
	if (params.size() != 6) return false
	def uri = formatMessage(params[0].d).replace(" ", "%20")
	def method = params[1].d
	def contentType = params[2].d
	def variables = params[3].d
	def importData = !!params[4].d
	def importPrefix = params[5].d ?: ""
	if (!uri) return false
	def protocol = "https"
	def uriParts = uri.split("://").toList()
	if (uriParts.size() > 2) {
		debug "Invalid URI for web request: $uri", null, "warn"
		return false
	}
	if (uriParts.size() == 2) {
		//remove the httpX:// from the uri
		protocol = uriParts[0].toLowerCase()
		uri = uriParts[1]
	}
	def internal = uri.startsWith("10.") || uri.startsWith("192.168.")
	if ((!internal) && uri.startsWith("172.")) {
		//check for the 172.16.x.x/12 class
		def b = uri.substring(4,2)
		if (b.isInteger()) {
			b = b.toInteger()
			internal = (b >= 16) && (b <= 31)
		}
	}
	def data = [:]
	for(variable in variables) {
		data[variable] = getVariable(variable)
	}
	if (internal) {
		try {
			debug "Sending internal web request to: $uri", null, "info"
			sendHubCommand(new physicalgraph.device.HubAction(
				method: method,
				path: (uri.indexOf("/") > 0) ? uri.substring(uri.indexOf("/")) : "",
				headers: [
					HOST: (uri.indexOf("/") > 0) ? uri.substring(0, uri.indexOf("/")) : uri,
				],
				query: method == "GET" ? data : null, //thank you @destructure00
				body: method != "GET" ? data : null //thank you @destructure00    
			))
		} catch (all) {
			debug "Error executing internal web request: $all", null, "error"
		}
	} else {
		try {
			debug "Sending external web request to: $uri", null, "info"
			def requestParams = [
				uri:  "${protocol}://${uri}",
				requestContentType: (method != "GET") && (contentType == "JSON") ? "application/json" : "application/x-www-form-urlencoded",
				query: method == "GET" ? data : null,
				body: method != "GET" ? data : null
			]
			def func = ""
			switch(method) {
				case "GET":
					func = "httpGet"
					break
				case "POST":
					func = "httpPost"
					break
				case "PUT":
					func = "httpPut"
					break
				case "DELETE":
					func = "httpDelete"
					break
				case "HEAD":
					func = "httpHead"
					break
			}
			if (func) {
				"$func"(requestParams) { response ->
					setVariable("\$httpStatusCode", response.status, true)
					setVariable("\$httpStatusOk", response.status == 200, true)
					if (importData && (response.status == 200) && response.data) {
						try {
							def jsonData = response.data instanceof Map ? response.data : new groovy.json.JsonSlurper().parseText(response.data)
							importVariables(jsonData, importPrefix)
						} catch (all) {
							debug "Error parsing JSON response for web request: $all", null, "error"
						}
					}
				}
			}
		} catch (all) {
			debug "Error executing external web request: $all", null, "error"
		}
	}
	return true
}

private task_vcmd_httpRequest_backup(devices, action, task, suffix = "") {
	def params = (task && task.data && task.data.p && task.data.p.size()) ? task.data.p : []
	if (params.size() != 6) return false
	def uri = params[0].d
	def method = params[1].d
	def contentType = params[2].d
	def variables = params[3].d
	def importData = !!params[4].d
	def importPrefix = params[5].d ?: ""
	if (!uri) return false
	def protocol = ""
	switch (uri.substring(0, 7).toLowerCase()) {
		case "http://":
			protocol = "http"
			break
		case "https:/":
			protocol = "https"
			break
		default:
			protocol = "https"
			uri = "https://" + uri
			break
	}
	def data = [:]
	for(variable in variables) {
		data[variable] = getVariable(variable)
	}
	def requestParams = [
		uri:  uri,
		query: method == "GET" ? data : null,
		requestContentType: (method != "GET") && (contentType == "JSON") ? "application/json" : "application/x-www-form-urlencoded",
		body: method != "GET" ? data : null
	]
	try {
		def func = ""
		switch(method) {
			case "GET":
				func = "httpGet"
				break
			case "POST":
				func = "httpPost"
				break
			case "PUT":
				func = "httpPut"
				break
			case "DELETE":
				func = "httpDelete"
				break
			case "HEAD":
				func = "httpHead"
				break
		}
		if (func) {
			"$func"(requestParams) { response ->
				setVariable("\$httpStatusCode", response.status, true)
				setVariable("\$httpStatusOk", response.status == 200, true)
				if (importData && (response.status == 200) && response.data) {
					try {
						def jsonData = response.data instanceof Map ? response.data : new groovy.json.JsonSlurper().parseText(response.data)
						importVariables(jsonData, importPrefix)
					} catch (all) {
						debug "Error parsing JSON response for web request: $all", null, "error"
					}
				}
			}
		}
	} catch (all) {
		debug "Error executing external web request: $all", null, "error"
	}
	return true
}

private task_vcmd_wolRequest(devices, action, task, suffix = "") {
	def params = (task && task.data && task.data.p && task.data.p.size()) ? task.data.p : []
	if (params.size() != 2) return false
	def mac = params[0].d ?: ""
	def secureCode = params[1].d
	mac = mac.replace(":", "").replace("-", "").replace(".", "").replace(" ", "").toLowerCase()
	return sendHubCommand(new physicalgraph.device.HubAction(
		"wake on lan $mac",
		physicalgraph.device.Protocol.LAN,
		null,
		secureCode ? [secureCode: secureCode] : [:]
	))
}

private task_vcmd_cancelPendingTasks(device, action, task, suffix = "") {
	state.rerunSchedule = true
	def params = (task && task.data && task.data.p && task.data.p.size()) ? task.data.p : []
	if (!device || (params.size() != 1)) {
		return false
	}
	unscheduleTask("cmd", null, device.id)
	if (params[0].d == "Global") {
		debug "WARNING: Global cancellation not yet implemented", null, "warn"
	}
	return true
}

private task_vcmd_beginSimpleForLoop(device, action, task, suffix = "") {
	if (task && task.data) {
		setVariable("\$index", task.data.value, true)
	}
}

private task_vcmd_beginForLoop(device, action, task, suffix = "") {
	if (task && task.data && task.data.variable) {
		setVariable(task.data.variable, task.data.value)
	}
}

private task_vcmd_loadAttribute(device, action, task, simulate = false) {
	def params = (task && task.data && task.data.p && task.data.p.size()) ? task.data.p : []
	if (!device || (params.size() != 4)) {
		return false
	}
	def attribute = cleanUpAttribute(params[0].d)
	def variable = params[1].d
	def allowTranslations = !!params[2].d
	def negateTranslations = !!params[3].d
	//work, work, work
	//get the real value
	def value = getVariable(variable)
	setAttributeValue(device, attribute, value, allowTranslations, negateTranslations)
	return true
}

private task_vcmd_loadState(device, action, task, simulate = false) {
	def params = (task && task.data && task.data.p && task.data.p.size()) ? task.data.p : []
	if (!device || (params.size() != 4)) {
		return false
	}
	def attributes = params[0].d
	def variable = params[1].d
	def values = getStateVariable(variable)
	def allowTranslations = !!params[2].d
	def negateTranslations = !!params[3].d
	//work, work, work
	//get the real value
	for(attribute in attributes.sort{ it }) {
		def cleanAttribute = cleanUpAttribute(attribute)
		def value = values[cleanAttribute]
		if (value != null) {
			setAttributeValue(device, cleanAttribute, value, allowTranslations, negateTranslations)
		}
	}
	return true
}

private task_vcmd_loadStateLocally(device, action, task, simulate = false, global = false) {
	def params = (task && task.data && task.data.p && task.data.p.size()) ? task.data.p : []
	if (!device || !device.id || (params.size() < 1) || (params.size() > 2)) {
		return false
	}
	def attributes = params[0].d
	def emptyState = params.size() == 2 ? !!params[1].d : false
	def values = getStateVariable("${ global ? "@" : "" }:::${device.id}:::")
	debug "Load from state: attributes are $attributes, values are $values"
	if (values instanceof Map) {
		for(attribute in attributes.sort { it }) {
			def cleanAttribute = cleanUpAttribute(attribute)
			def value = values[cleanAttribute]
			if (value != null) {
				setAttributeValue(device, cleanAttribute, value, false, false)
			}
		}
	}
	if (emptyState) {
		setStateVariable("${ global ? "@" : "" }:::${device.id}:::", null)
	}
	return true
}

private task_vcmd_loadStateGlobally(device, action, task, simulate = false) {
	return task_vcmd_loadStateLocally(device, action, task, simulate, true)
}

private setAttributeValue(device, attribute, value, allowTranslations, negateTranslations) {
	def commands = commands().findAll{ (it.attribute == attribute) && it.value }
	//oh boy, we can pick and choose...
	for (command in commands) {
		if (command.value.startsWith("*")) {
			if (command.parameters && (command.parameters.size() == 1)) {
				def parts = command.value.tokenize(":")
				def v = value
				if (parts.size() == 2) {
					v = cast(v, parts[1])
				} else {
					def attr = getAttributeByName(attribute)
					if (attr) {
						v = cast(v, attr.type)
					}
				}
				if (attribute == "hue") {
					v = cast(v, "decimal") / 3.6
				}
				if (device.hasCommand(command.name)) {
					def currentValue = "${device.currentValue(attribute)}"
					if (command.name == "setLevel") {
						//setLevel is handled differently. Even if we have the same value, but the switch would flip, we need to let it execute
						if (device.currentValue("switch") == (v > 0 ? "off" : "on")) currentValue = null //we fake the current value to allow execution
					}
					if (!state.app?.disableCO && (currentValue == "$v")) {
						debug "Preventing execution of [${getDeviceLabel(device)}].${command.name}($v) because current value is the same", null, "info"
					} else {
						debug "Executing [${getDeviceLabel(device)}].${command.name}($v)", null, "info"
						device."${command.name}"(v)
					}
					return true
				}
			}
		} else {
			if ((command.value == value) && (!command.parameters)) {
				//found an exact match, let's do it
				if (device.hasCommand(command.name)) {
					def currentValue = "${device.currentValue(attribute)}"
					if (!state.app?.disableCO && (currentValue == "$value")) {
						debug "Preventing execution of [${getDeviceLabel(device)}].${command.name}() because current value is the same", null, "info"
					} else {
						debug "Executing [${getDeviceLabel(device)}].${command.name}()", null, "info"
					}
					device."${command.name}"()
					return true
				}
			}
		}
	}
	//boolean stuff goes here
	if (!allowTranslations) return false
	def v = cast(value, "boolean")
	if (negateTranslations) v = !v
	for (command in commands) {
		if (!command.value.startsWith("*")) {
			if ((cast(command.value, "boolean") == v) && (!command.parameters)) {
				//found an exact match, let's do it
				if (device.hasCommand(command.name)) {
					debug "Executing [${getDeviceLabel(device)}].${command.name}() (boolean translation)", null, "info"
					device."${command.name}"()
					return true
				}
			}
		}
	}
}

private task_vcmd_saveAttribute(devices, action, task, simulate = false) {
	def params = (task && task.data && task.data.p && task.data.p.size()) ? task.data.p : []
	if (!devices || (params.size() != 4)) {
		return false
	}
	def attribute = cleanUpAttribute(params[0].d)
	def aggregation = params[1].d
	if (!aggregation) aggregation = "First"
	def dataType = params[2].d
	def variable = params[3].d
	//work, work, work
	def result = getAggregatedAttributeValue(devices, attribute, aggregation, dataType)
	setVariable(variable, result)
	return true
}

private task_vcmd_saveState(devices, action, task, simulate = false) {
	def params = (task && task.data && task.data.p && task.data.p.size()) ? task.data.p : []
	if (!devices || (params.size() != 4)) {
		return false
	}
	def attributes = params[0].d
	def aggregation = params[1].d
	def dataType = params[2].d
	def variable = params[3].d
	//work, work, work
	def values = [:]
	for (attribute in attributes) {
		def cleanAttribute = cleanUpAttribute(attribute)
		values[cleanAttribute] = getAggregatedAttributeValue(devices, cleanAttribute, aggregation, dataType)
	}
	setStateVariable(variable, values)
	return true
}

private task_vcmd_saveStateLocally(device, action, task, simulate = false, global = false) {
	def params = (task && task.data && task.data.p && task.data.p.size()) ? task.data.p : []
	if (!device || !device.id || (params.size() < 1) || (params.size() > 2)) {
		return false
	}
	def attributes = params[0].d
	def needsEmptyState = params.size() == 2 ? !!params[1].d : false
	if (needsEmptyState) {
		//check to ensure state is empty
		def values = getStateVariable("${ global ? "@" : "" }:::${device.id}:::")
		if (values != null) return false
	}
	def values = [:]
	for (attribute in attributes) {
		def cleanAttribute = cleanUpAttribute(attribute)
		values[cleanAttribute] = cleanAttribute == "hue" ? device.currentValue(cleanAttribute) * 3.6 : device.currentValue(cleanAttribute)
	}
	debug "Save to state: attributes are $attributes, values are $values"
	setStateVariable("${ global ? "@" : "" }:::${device.id}:::", values)
	return true
}

private task_vcmd_saveStateGlobally(device, action, task, simulate = false) {
	return task_vcmd_saveStateLocally(device, action, task, simulate, true)
}

private getAggregatedAttributeValue(devices, attribute, aggregation, dataType) {
	def result
	def attr = getAttributeByName(attribute)
	if (attr) {
		def type = attr.type
		result = cast("", attr.type)
		def values = []
		for (device in devices) {
			def val = cast(device.currentValue(attribute), type)
			if (attribute == "hue") {
				val = cast(val, "decimal") * 3.6
			}
			values.push val
		}
		if (values.size()) {
			switch (aggregation) {
				case "First":
					result = null
					for(value in values) {
						result = value
						break
					}
					break
				case "Last":
					result = null
					for(value in values) {
						result = value
					}
					break
				case "Min":
					result = null
					for(value in values) {
						if ((result == null) || (value < result)) result = value
					}
					break
				case "Max":
					result = null
					for(value in values) {
						if ((result == null) || (value > result)) result = value
					}
					break
				case "Avg":
					result = null
					if (attr.type in ["number", "decimal"]) {
						for(value in values) {
							result = result == null ? value : result + value
						}
						result = cast(result / values.size(), attr.type)
					} else {
						//average will act differently on strings and booleans
						//we look for the value that is used most and we consider that the average
						def map = [:]
						for (value in values) {
							map[value] = map[value] ? map[value] + 1 : 1
						}
						for (item in map.sort { - it.value }) {
							result = cast(item.key, attr.type)
							break
						}
					}
					break
				case "Sum":
					result = null
					if (attr.type in ["number", "decimal"]) {
						for(value in values) {
							result = result == null ? value : result + value
						}
					} else {
						//sum will act differently on strings and booleans
						result = buildNameList(values, "")
					}
					break
				case "Count":
					result = (int) values.size()
					break
				case "Boolean And":
					result = true
					for (value in values) {
						result = result && cast(value, "boolean")
						if (!result) break
					}
					break
				case "Boolean Or":
					result = false
					for (value in values) {
						result = result || cast(value, "boolean")
						if (result) break
					}
					break
				case "Boolean True Count":
					result = (int) 0
					for (value in values) {
						if (cast(value, "boolean")) result += 1
					}
					break
				case "Boolean True Count":
					result = (int) 0
					for (value in values) {
						if (!cast(value, "boolean")) result += 1
					}
					break
			}
		}
	}

	if (dataType) {
		//if user wants a certain data type, we comply
		result = cast(result, dataType)
	}

	return result
}

private task_vcmd_setVariable(devices, action, task, simulate = false) {
	def params = simulate ? ((task && task.p && task.p.size()) ? task.p : []) : ((task && task.data && task.data.p && task.data.p.size()) ? task.data.p : [])
	//we need at least 7 params
	if (params.size() < 7) {
		return simulate ? null : false
	}
	def name = params[0].d
	def dataType = params[1].d
	if (!name || !dataType) return simulate ? null : false
	def result = ""
	switch (dataType) {
		case "time":
			result = adjustTime()
			break
		case "number":
			//we need to use long numbers
			dataType = "long"
		case "long":
		case "decimal":
			result = 0
			break
	}
	def immediate = !!params[2].d
	try {
		def i = 4
		def grouping = false
		def groupingUnit = ""
		def groupingIndex = null
		def groupingResult = null
		def groupingOperation = null
		def previousOperation = null
		def operation = null
		def subDataType = dataType == "long" ? "decimal" : dataType
		def idx = 0
		while (true) {
			def value = params[i].d
			def variable = params[i + 1].d
			if (!value) {
				//we get the value of the variable
				if (subDataType in ["time"]) {
					value = adjustTime(getVariable(variable)).time
				} else {
					value = cast(getVariable(variable, dataType in ["string", "text"]), subDataType)
				}
			} else {
				if (subDataType in ["time"]) {
					//we need to bring the value to today
					def time = adjustTime(value)
					if (time) {
						def h = time.hours
						def m = time.minutes
						def lastMidnight = adjustTime().time
						lastMidnight = lastMidnight - lastMidnight.mod(86400000)
						value = lastMidnight + h * 3600000 + m * 60000

					}
				}
				value = cast(value, subDataType)
			}
			if (i == 4) {
				//initial values
				result = cast(value, dataType)
			}
			def unit = (dataType == "time" ? params[i + 2].d : null)
			previousOperation = operation
			operation = params.size() > i + 3 ? "${params[i + 3].d} ".tokenize(" ")[0] : null
			def needsGrouping = (operation == "*") || (operation == "÷") || (operation == "AND")
			def skip = idx == 0
			if (needsGrouping) {
				//these operations require grouping i.e. (a * b * c) seconds
				if (!grouping) {
					grouping = true
					groupingIndex = idx
					groupingUnit = unit
					groupingOperation = previousOperation
					groupingResult = value
					skip = true
				}
			}
			//add the value/variable
			subDataType = subDataType == "time" ? "long" : subDataType
			if (!skip) {
				def operand1 = grouping ? groupingResult : result
				def operand2 = value
				if (groupingUnit ? groupingUnit : unit) {
					switch (unit) {
						case "seconds":
							operand2 = operand2 * 1000
							break
						case "minutes":
							operand2 = operand2 * 60000
							break
						case "hours":
							operand2 = operand2 * 3600000
							break
						case "days":
							operand2 = operand2 * 86400000
							break
						case "weeks":
							operand2 = operand2 * 604800000
							break
						case "months":
							operand2 = operand2 * 2592000000
							break
						case "years":
							operand2 = operand2 * 31536000000
							break
					}
				}
				//reset the group unit - we only apply it once
				groupingUnit = null
				def res = null
				switch (previousOperation) {
					case "AND":
						res = cast(operand1 && operand2, subDataType)
						break
					case "OR":
						res = cast(operand1 || operand2, subDataType)
						break
					case "+":
						res = cast(operand1 + operand2, subDataType)
						break
					case "-":
						res = cast(operand1 - operand2, subDataType)
						break
					case "*":
						res = cast(operand1 * operand2, subDataType)
						break
					case "÷":
						if (!operand2) return null
						res = cast(operand1 / operand2, subDataType)
						break
				}
				if (grouping) {
					groupingResult = res
				} else {
					result = res
				}
			}
			skip = false
			if (grouping && !needsGrouping) {
				//these operations do NOT require grouping
				//ungroup
				if (!groupingOperation) {
					result = groupingResult
				} else {
					def operand1 = result
					def operand2 = groupingResult

					switch (groupingOperation) {
						case "AND":
							result = cast(operand1 && operand2, subDataType)
							break
						case "OR":
							result = cast(operand1 || operand2, subDataType)
							break
						case "+":
							result = cast(operand1 + operand2, subDataType)
							break
						case "-":
							result = cast(operand1 - operand2, subDataType)
							break
						case "*":
							result = cast(operand1 * operand2, subDataType)
							break
						case "÷":
							if (!operand2) return null
							result = cast(operand1 / operand2, subDataType)
							break
					}
				}
				grouping = false
			}
			if (!operation) break
			i += 4
			idx += 1
		}
	} catch (e) {
		return simulate ? null : false
	}
	if (dataType in ["string", "text"]) {
		result = formatMessage(result)
	} else if (dataType in ["time"]) {
		result = simulate ? formatLocalTime(convertTimeToUnixTime(result)) : convertTimeToUnixTime(result)
	} else {
		result = cast(result, dataType)
	}
	setVariable(name, result)
	if (simulate) {
		return result
	}
	return true
}

private cast(value, dataType) {
	def trueStrings = ["1", "on", "open", "locked", "active", "wet", "detected", "present", "occupied", "muted", "sleeping"]
	def falseStrings = ["0", "false", "off", "closed", "unlocked", "inactive", "dry", "clear", "not detected", "not present", "not occupied", "unmuted", "not sleeping"]
	switch (dataType) {
		case "string":
		case "text":
			if (value instanceof Boolean) {
				return value ? "true" : "false"
			}
			return value ? "$value" : ""
		case "number":
			if (value == null) return (int) 0
			if (value instanceof String) {
				if (value.isInteger())
					return value.toInteger()
				if (value.isFloat())
					return (int) Math.floor(value.toFloat())
				if (value in trueStrings)
					return (int) 1
			}
			def result = (int) 0
			try {
				result = (int) value
			} catch(all) {
				result = (int) 0
			}
			return result ? result : (int) 0
		case "long":
			if (value == null) return (long) 0
			if (value instanceof String) {
				if (value.isInteger())
					return (long) value.toInteger()
				if (value.isFloat())
					return (long) Math.round(value.toFloat())
				if (value in trueStrings)
					return (long) 1
			}
			def result = (long) 0
			try {
				result = (long) value
			} catch(all) {
			}
			return result ? result : (long) 0
		case "decimal":
			if (value == null) return (float) 0
			if (value instanceof String) {
				if (value.isFloat())
					return (float) value.toFloat()
				if (value.isInteger())
					return (float) value.toInteger()
				if (value in trueStrings)
					return (float) 1
			}
			def result = (float) 0
			try {
				result = (float) value
			} catch(all) {
			}
			return result ? result : (float) 0
		case "boolean":
			if (value instanceof String) {
				if (!value || (value in falseStrings))
					return false
				return true
			}
			return !!value
		case "time":
			return value instanceof String ? adjustTime(value).time : cast(value, "long")
		case "vector3":
			return value instanceof String ? adjustTime(value).time : cast(value, "long")
		case "orientation":
			return getThreeAxisOrientation(value)
	}
	//anything else...
	return value
}

/******************************************************************************/
/*** CoRE PISTON PUBLISHED METHODS											***/
/******************************************************************************/

def getLastPrimaryEvaluationDate() {
	return state.lastPrimaryEvaluationDate
}

def getLastPrimaryEvaluationResult() {
	return state.lastPrimaryEvaluationResult
}

def getLastSecondaryEvaluationDate() {
	return state.lastSecondaryEvaluationDate
}

def getLastSecondaryEvaluationResult() {
	return state.lastSecondaryEvaluationResult
}

def getCurrentState() {
	return state.currentState
}

def getMode() {
	return state.app  ? state.app.mode : null
}

def getDeviceSubscriptionCount() {
	return state.deviceSubscriptions ? state.deviceSubscriptions : 0
}

def getCurrentStateSince() {
	return state.currentStateSince
}

def getRunStats() {
	return state.runStats
}

def resetRunStats() {
	atomicState.runStats = null
	state.runStats = null
}

def getConditionStats() {
	return [
		conditions: getConditionCount(state.app),
		triggers: getTriggerCount(state.app)
	]
}

def getPistonApp() {
	return state.app
}

def getPistonType() {
	return state.app.mode
}

def getPistonTasks() {
	return atomicState.tasks
}

def getPistonEnabled() {
	return !!state.app?.enabled
}

def getPistonConditionDescription(condition) {
	return (condition ? getConditionDescription(condition.id) : null)
}

def getSummary() {
	if (!state.app) {
		log.warn "Piston ${app.label} is not complete, please open it and save it"
	}
	def stateApp = (state.app ?: state.config?.app)
	return [
		i: app.id,
		l: app.label,
		d: stateApp?.description,
		e: !!stateApp?.enabled,
		m: stateApp?.mode,
		s: state.currentState,
		ss: state.currentStateSince,
		n: state.nextScheduledTime,
		d: state.deviceSubscriptions ? state.deviceSubscriptions : 0,
		c: getConditionCount(stateApp),
		t: getTriggerCount(stateApp),
		le: state.lastEvent,
		lx: state.lastExecutionTime,
		cd: formatLocalTime(stateApp?.created),
		cv: stateApp?.version,
		md: formatLocalTime(state.lastInitialized),
	]
}

def pausePiston(pistonName) {
	if (parent) {
		return parent.pausePiston(pistonName)
	} else {
		def piston = getChildApps().find{ it.label == pistonName }
		if (piston) {
			//fire up the piston
			return piston.pause()
		}
		return null
	}
}

def pause() {
	if (!parent) return null
	if (!state.app) return null
	state.app.enabled = false
	if (state.config && state.config.app) state.config.app.enabled = false
	unsubscribe()
	state.tasks = [:]
}

def resumePiston(pistonName) {
	if (parent) {
		return parent.resumePiston(pistonName)
	} else {
		def piston = getChildApps().find{ it.label == pistonName }
		if (piston) {
			//fire up the piston
			return piston.resume()
		}
		return null
	}
}

def resume() {
	if (!parent) return null
	if (!state.app) return null
	state.app.enabled = true
	if (state.config && state.config.app) state.config.app.enabled = true
	state.run = "app"
	initializeCoREPistonStore()
	if (state.app.mode != "Follow-Up") {
		//follow-up pistons don't subscribe to anything
		subscribeToAll(state.app)
	}
	processTasks()
}

/******************************************************************************/
/***																		***/
/*** UTILITIES																***/
/***																		***/
/******************************************************************************/

/******************************************************************************/
/*** DEBUG FUNCTIONS														***/
/******************************************************************************/

private debug(message, shift = null, cmd = null, err = null) {
	def debugging = settings.debugging
	if (!debugging) {
		return
	}
	cmd = cmd ? cmd : "debug"
	if (!settings["log#$cmd"]) {
		return
	}
	//mode is
	// 0 - initialize level, level set to 1
	// 1 - start of routine, level up
	// -1 - end of routine, level down
	// anything else - nothing happens
	def maxLevel = 4
	def level = state.debugLevel ? state.debugLevel : 0
	def levelDelta = 0
	def prefix = "║"
	def pad = "░"
	switch (shift) {
		case 0:
			level = 0
			prefix = ""
			break
		case 1:
			level += 1
			prefix = "╚"
			pad = "═"
			break
		case -1:
			levelDelta = -(level > 0 ? 1 : 0)
			pad = "═"
			prefix = "╔"
			break
	}

	if (level > 0) {
		prefix = prefix.padLeft(level, "║").padRight(maxLevel, pad)
	}

	level += levelDelta
	state.debugLevel = level

	if (debugging) {
		prefix += " "
	} else {
		prefix = ""
	}

	if (cmd == "info") {
		log.info "$prefix$message", err
	} else if (cmd == "trace") {
		log.trace "$prefix$message", err
	} else if (cmd == "warn") {
		log.warn "$prefix$message", err
	} else if (cmd == "error") {
		log.error "$prefix$message", err
	} else {
		log.debug "$prefix$message", err
	}
}

/******************************************************************************/
/*** DATE & TIME FUNCTIONS													***/
/******************************************************************************/
private getPreviousQuarterHour(unixTime = now()) {
	return unixTime - unixTime.mod(900000)
}

//adjusts the time to local timezone
private adjustTime(time = null) {
	if (time instanceof String) {
		//get UTC time
		time = timeToday(time, location.timeZone).getTime()
	}
	if (time instanceof Date) {
		//get unix time
		time = time.getTime()
	}
	if (!time) {
		time = now()
	}
	if (time) {
		return new Date(time + location.timeZone.getOffset(time))
	}
	return null
}

private formatLocalTime(time, format = "EEE, MMM d yyyy @ h:mm a z") {
	if (time instanceof Long) {
		time = new Date(time)
	}
	if (time instanceof String) {
		//get UTC time
		time = timeToday(time, location.timeZone)
	}
	if (!(time instanceof Date)) {
		return null
	}
	def formatter = new java.text.SimpleDateFormat(format)
	formatter.setTimeZone(location.timeZone)
	return formatter.format(time)
}

private convertDateToUnixTime(date) {
	if (!date) {
		return null
	}
	if (!(date instanceof Date)) {
		date = new Date(date)
	}
	return date.time - location.timeZone.getOffset(date.time)
}

private convertTimeToUnixTime(time) {
	if (!time) {
		return null
	}
	return time - location.timeZone.getOffset(time)
}

private formatTime(time) {
	//we accept both a Date or a settings' Time
	return formatLocalTime(time, "h:mm a z")
}

private formatHour(h) {
	return (h == 0 ? "midnight" : (h < 12 ? "${h} AM" : (h == 12 ? "noon" : "${h-12} PM"))).toString()
}

private formatDayOfMonth(dom, dow) {
	if (dom) {
		if (dom.contains("week")) {
			//relative day of week
			return dom.replace("week", dow)
		} else {
			//dealing with a certain day of the month
			if (dom.contains("last")) {
				//relative day value
				return dom
			} else {
				//absolute day value
				def day = dom.replace("on the ", "").replace("st", "").replace("nd", "").replace("rd", "").replace("th", "").toInteger()
				return "on the ${formatOrdinalNumber(day)}"
			}
		}
	}
	return "[ERROR]"
}

//return the number of occurrences of same day of week up until the date or from the end of the month if backwards, i.e. last Sunday is -1, second-last Sunday is -2
private getWeekOfMonth(date = null, backwards = false) {
	if (!date) {
		date = adjustTime(now())
	}
	def day = date.date
	if (backwards) {
		def month = date.month
		def year = date.year
		def lastDayOfMonth = (new Date(year, month + 1, 0)).date
		return -(1 + Math.floor((lastDayOfMonth - day) / 7))
	} else {
		return 1 + Math.floor((day - 1) / 7) //1 based
	}
}

//returns the number of day in a month, 1 based, or -1 based if backwards (last day of the month)
private getDayOfMonth(date = null, backwards = false) {
	if (!date) {
		date = adjustTime(now())
	}
	def day = date.date
	if (backwards) {
		def month = date.month
		def year = date.year
		def lastDayOfMonth = (new Date(year, month + 1, 0)).date
		return day - lastDayOfMonth - 1
	} else {
		return day
	}
}

//for a given month, returns the Nth instance of a certain day of the week within that month. week ranges from 1 through 5 and -1 through -5
private getDayInWeekOfMonth(date, week, dow) {
	if (!date || (dow == null)) {
		return null
	}
	def lastDayOfMonth = (new Date(date.year, date.month + 1, 0)).date
	if (week > 0) {
		//going forward
		def firstDayOfMonthDOW = (new Date(date.year, date.month, 1)).day
		//find the first matching day
		def firstMatch = 1 + dow - firstDayOfMonthDOW + (dow < firstDayOfMonthDOW ? 7 : 0)
		def result = firstMatch + 7 * (week - 1)
		return result <= lastDayOfMonth ? result : null
	}
	if (week < 0) {
		//going backwards
		def lastDayOfMonthDOW = (new Date(date.year, date.month + 1, 0)).day
		//find the first matching day
		def firstMatch = lastDayOfMonth + dow - lastDayOfMonthDOW - (dow > lastDayOfMonthDOW ? 7 : 0)
		def result = firstMatch + 7 * (week + 1)
		return result >= 1 ? result : null
	}
	return null
}

private getDayOfWeekName(date = null) {
	if (!date) {
		date = adjustTime(now())
	}
	switch (date.day) {
		case 0: return "Sunday"
		case 1: return "Monday"
		case 2: return "Tuesday"
		case 3: return "Wednesday"
		case 4: return "Thursday"
		case 5: return "Friday"
		case 6: return "Saturday"
	}
	return null
}

private getDayOfWeekNumber(date = null) {
	if (!date) {
		date = adjustTime(now())
	}
	if (date instanceof Date) {
		return date.day
	}
	switch (date) {
		case "Sunday": return 0
		case "Monday": return 1
		case "Tuesday": return 2
		case "Wednesday": return 3
		case "Thursday": return 4
		case "Friday": return 5
		case "Saturday": return 6
	}
	return null
}

private getMonthName(date = null) {
	if (!date) {
		date = adjustTime(now())
	}
	def month = date.month + 1
	switch (month) {
		case  1: return "January"
		case  2: return "February"
		case  3: return "March"
		case  4: return "April"
		case  5: return "May"
		case  6: return "June"
		case  7: return "July"
		case  8: return "August"
		case  9: return "September"
		case 10: return "October"
		case 11: return "November"
		case 12: return "December"
	}
	return null
}

private getMonthNumber(date = null) {
	if (!date) {
		date = adjustTime(now())
	}
	if (date instanceof Date) {
		return date.month + 1
	}
	switch (date) {
		case "January": return 1
		case "February": return 2
		case "March": return 3
		case "April": return 4
		case "May": return 5
		case "June": return 6
		case "July": return 7
		case "August": return 8
		case "September": return 9
		case "October": return 10
		case "November": return 11
		case "December": return 12
	}
	return null
}

private getSunrise() {
	if (!(state.sunrise instanceof Date)) {
		def sunTimes = getSunriseAndSunset()
		state.sunrise = adjustTime(sunTimes.sunrise)
		state.sunset = adjustTime(sunTimes.sunset)
	}
	return state.sunrise
}

private getSunset() {
	if (!(state.sunset instanceof Date)) {
		def sunTimes = getSunriseAndSunset()
		state.sunrise = adjustTime(sunTimes.sunrise)
		state.sunset = adjustTime(sunTimes.sunset)
	}
	return state.sunset
}

private addOffsetToMinutes(minutes, offset) {
	if (minutes == null) {
		return null
	}
	if (offset == null) {
		return minutes
	}
	minutes = minutes + offset
	while (minutes >= 1440) {
		minutes -= 1440
	}
	while (minutes < 0) {
		minutes += 1440
	}
	return minutes
}

private timeComparisonOptionValues(trigger, supportVariables = true) {
	return ["custom time", "midnight", "sunrise", "noon", "sunset"] + (supportVariables ? ["time of variable", "date and time of variable"] : []) + (trigger ? ["every minute", "every number of minutes", "every hour", "every number of hours"] : [])
}

private groupOptions() {
	return ["AND", "OR", "XOR", "THEN IF", "ELSE IF", "FOLLOWED BY"]
}

private threeAxisOrientations() {
	return ["rear side up", "down side up", "left side up", "front side up", "up side up", "right side up"]
}

private threeAxisOrientationCoordinates() {
	return ["rear side up", "down side up", "left side up", "front side up", "up side up", "right side up"]
}

private getThreeAxisDistance(coord1, coord2) {
	if (coord1 && coord2){
		def dX = coord1.x - coord2.x
		def dY = coord1.y - coord2.y
		def dZ = coord1.z - coord2.z
		def s = Math.pow(dX,2) + Math.pow(dY,2) + Math.pow(dZ,2)
		def dist = Math.pow(s,0.5)
		return dist.toInteger()
	} else return null
}

private getThreeAxisOrientation(value, getIndex = false) {
	if (value instanceof Map) {
		if ((value.x != null) && (value.y != null) && (value.z != null)) {
			def orientations = threeAxisOrientations()
			def x = Math.abs(value.x)
			def y = Math.abs(value.y)
			def z = Math.abs(value.z)
			def side = (x > y ? (x > z ? 0 : 2) : (y > z ? 1 : 2))
			side = side + (((side == 0) && (value.x < 0)) || ((side == 1) && (value.y < 0)) || ((side == 2) && (value.z < 0)) ? 3 : 0)
			def result = getIndex ? side : orientations[side]
			return result
		}
	}
	return value
}

private timeOptions(trigger = false) {
	def result = ["1 minute"]
	for (def i =2; i <= (trigger ? 360 : 360); i++) {
		result.push("$i minutes")
	}
	return result
}

private timeRepeatOptions() {
	return ["every day", "every number of days", "every week", "every number of weeks", "every month", "every number of months", "every year", "every number of years"]
}

private timeMinuteOfHourOptions() {
	def result = []
	for (def i =0; i <= 59; i++) {
		result.push("$i".padLeft(2, "0"))
	}
	return result
}

private timeHourOfDayOptions() {
	def result = []
	for (def i =0; i <= 23; i++) {
		result.push(formatHour(i))
	}
	return result
}

private timeDayOfMonthOptions() {
	def result = []
	for (def i =1; i <= 31; i++) {
		result.push("on the ${formatOrdinalNumber(i)}")
	}
	return result + ["on the last day", "on the second-last day", "on the third-last day", "on the first week", "on the second week", "on the third week", "on the fourth week", "on the fifth week", "on the last week", "on the second-last week", "on the third-last week"]
}

private timeDayOfMonthOptions2() {
	def result = []
	for (def i =1; i <= 31; i++) {
		result.push("the ${formatOrdinalNumber(i)}")
	}
	return result + ["the last day of the month", "the second-last day of the month", "the third-last day of the month"]
}

private timeDayOfWeekOptions() {
	return ["Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"]
}

private timeWeekOfMonthOptions() {
	return ["the first week", "the second week", "the third week", "the fourth week", "the fifth week", "the last week", "the second-last week"]
}

private timeMonthOfYearOptions() {
	return ["January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"]
}

private timeYearOptions() {
	def result = ["even years", "odd years", "leap years"]
	def year = 1900 + (new Date()).getYear()
	for (def i = year; i <= 2099; i++) {
		result.push("$i")
	}
	for (def i = 2016; i < year; i++) {
		result.push("$i")
	}
	return result
}

private timeToMinutes(time) {
	if (!(time instanceof String)) return 0
	def value = time.replace(" minutes", "").replace(" minute", "")
	if (value.isInteger()) {
		return value.toInteger()
	}
	debug "ERROR: Time '$time' could not be parsed", null, "error"
	return 0
}

/******************************************************************************/
/*** NUMBER FUNCTIONS														***/
/******************************************************************************/

private formatOrdinalNumber(number) {
	def hm = number.mod(100)
	if ((hm < 10) || (hm > 20)) {
		switch (number.mod(10)) {
			case 1:
				return "${number}st"
			case 2:
				return "${number}nd"
			case 3:
				return "${number}rd"
		}
	}
	return "${number}th"
}

private formatOrdinalNumberName(number) {
	def prefix = ""
	if ((number >= 100) || (number <= -100)) {
		return "NOT_IMPLEMENTED"
	}
	if (number < -1) {
		return formatOrdinalNumberName(-number) + "-last"
	}
	if (number >= 20) {
		def tens = Math.floor(number / 10)
		switch (tens) {
			case 2:
				prefix = "twenty"
				break
			case 3:
				prefix = "thirty"
				break
			case 4:
				prefix = "fourty"
				break
			case 5:
				prefix = "fifty"
				break
			case 6:
				prefix = "sixty"
				break
			case 7:
				prefix = "seventy"
				break
			case 8:
				prefix = "eighty"
				break
			case 9:
				prefix = "ninety"
				break
		}
		if (prefix) {
			if (number.mod(10) > 0) {
				prefix = prefix + "-"
			}
			number = number - tens * 10
		}
	}
	switch (number) {
		case -1: return "${prefix}last"
		case 0: return prefix
		case 1: return "${prefix}first"
		case 2: return "${prefix}second"
		case 3: return "${prefix}third"
		case 4: return "${prefix}fourth"
		case 5: return "${prefix}fifth"
		case 6: return "${prefix}sixth"
		case 7: return "${prefix}seventh"
		case 8: return "${prefix}eighth"
		case 9: return "${prefix}nineth"
		case 10: return "${prefix}tenth"
		case 11: return "${prefix}eleventh"
		case 12: return "${prefix}twelveth"
		case 13: return "${prefix}thirteenth"
		case 14: return "${prefix}fourteenth"
		case 15: return "${prefix}fifteenth"
		case 16: return "${prefix}sixteenth"
		case 17: return "${prefix}seventeenth"
		case 18: return "${prefix}eighteenth"
		case 19: return "${prefix}nineteenth"
	}
}

/******************************************************************************/
/*** CONDITION FUNCTIONS													***/
/******************************************************************************/

//finds and returns the condition object for the given condition Id
//fixed by @rappleg to use strict type casting - fix for Android ST 2.2.2 app
private _traverseConditions(parent, conditionId) {
	Integer conditionIdInt = conditionId instanceof String ? Integer.valueOf(conditionId) : conditionId
	if (parent.id == conditionIdInt) {
		return parent
	}
	for (condition in parent.children) {
		def result = _traverseConditions(condition, conditionIdInt)
		if (result) {
			return result
		}
	}
	return null
}

//returns a condition based on its ID
private getCondition(conditionId, primary = null) {
	def result = null
	def parent = (state.run == "config" ? state.config : state)
	if (parent && (primary in [null, true]) && parent.app && parent.app.conditions) {
		result =_traverseConditions(parent.app.conditions, conditionId)
	}
	if (!result && parent && (primary in [null, false]) && parent.app && parent.app.otherConditions) {
		result = _traverseConditions(parent.app.otherConditions, conditionId)
	}
	return result
}

private getConditionMasterId(conditionId) {
	if (conditionId <= 0) return conditionId
	def condition = getCondition(conditionId)
	if (condition && (condition.parentId != null)) return getConditionMasterId(condition.parentId)
	return condition.id
}

//optimized version that returns true if any trigger is detected
private getConditionHasTriggers(condition) {
	def result = 0
	if (condition) {
		if (condition.children != null) {
			//we're dealing with a group
			for (child in condition.children) {
				if (getConditionHasTriggers(child)) {
					//if we detect a trigger we exit immediately
					return true
				}
			}
		} else {
			return !!condition.trg
		}
	}
	return false
}

private getConditionTriggerCount(condition) {
	def result = 0
	if (condition) {
		if (condition.children != null) {
			//we're dealing with a group
			for (child in condition.children) {
				result += getConditionTriggerCount(child)
			}
		} else {
			if (condition.trg) {
				def devices = settings["condDevices${condition.id}"]
				if (devices) {
					return devices.size()
				} else {
					return 1
				}
			}
		}
	}
	return result
}

private withEachCondition(condition, callback, data = null, includeGroups = false) {
	def result = 0
	if (condition) {
		if (condition.children != null) {
			//we're dealing with a group
			if (includeGroups) "$callback"(condition, data)
			for (child in condition.children) {
				withEachCondition(child, callback, data)
			}
		} else {
			"$callback"(condition, data)
		}
	}
	return result
}

private withEachTrigger(condition, callback, data = null) {
	def result = 0
	if (condition) {
		if (condition.children != null) {
			//we're dealing with a group
			for (child in condition.children) {
				withEachTrigger(child, callback, data)
			}
		} else {
			if (condition.trg) {
				"$callback"(condition, data)
			}
		}
	}
	return result
}

private getTriggerCount(app) {
	return app ? getConditionTriggerCount(app.conditions) + (settings.mode in ["Latching", "And-If", "Or-If"] ? getConditionTriggerCount(app.otherConditions) : 0) : 0
}

private getConditionConditionCount(condition) {
	def result = 0
	if (condition) {
		if (condition.children != null) {
			//we're dealing with a group
			for (child in condition.children) {
				result += getConditionConditionCount(child)
			}
		} else {
			if (!condition.trg) {
				def devices = settings["condDevices${condition.id}"]
				if (devices) {
					return devices.size()
				} else {
					return 1
				}
			}
		}
	}
	return result
}

private getConditionCount(app) {
	return app ? getConditionConditionCount(app.conditions) + (!(settings.mode in ["Basic", "Simple", "Follow-Up"]) ? getConditionConditionCount(app.otherConditions) : 0) : 0
}

def rebuildPiston(update = false) {
	configApp()
	state.config.app.conditions = createCondition(true)
	state.config.app.conditions.id = 0
	state.config.app.otherConditions = createCondition(true)
	state.config.app.otherConditions.id = -1
	state.config.app.actions = []
	rebuildConditions()
	rebuildActions()
	if (update) {
		debug "Finished rebuilding piston, updating SmartApp...", null, "trace"
		updated()
	}
}

private rebuildConditions() {
	def conditions = settings.findAll{it.key.startsWith("condParent")}.sort{ it.key.replace("condParent", "").toInteger() }
	boolean keepGoing = true
	while (keepGoing) {
		keepGoing = false
		for(condition in conditions) {
			if (condition.value != null) {
				int parentId = condition.value.toInteger()
				int conditionId = condition.key.replace("condParent", "").toInteger()
				parentId = conditionId == parentId ? 0 : parentId
				def parentCondition = getCondition(parentId)
				if (parentCondition != null) {
					//let's see if it's a group
					def c = null
					if (settings["condGrouping${conditionId}"] || conditions.find{ (it.key != "condParent${conditionId}") && it.value != null && (it.value.toInteger() == conditionId) }) {
						//group
						c = createCondition(parentId, true, conditionId)
					} else {
						//condition
						c = createCondition(parentId, false, conditionId)
					}
					if (c) updateCondition(c)
					keepGoing = true
					condition.value = null
				}
			}
		}
	}
	cleanUpConditions(true)
}

private rebuildActions() {
	def actions = settings.findAll{it.key.startsWith("actParent")}.sort{ it.key.replace("actParent", "").toInteger() }
	for(action in actions) {
		if (action.value != null) {
			def parentId = action.value.toInteger()
			def actionId = action.key.replace("actParent", "").toInteger()
			def rs = !!settings["actRState${actionId}"]
			def a = createAction(parentId, rs, actionId)
			if (a) updateAction(a)
		}
	}
	cleanUpActions()
}

private rebuildTaps() {
	def taps = settings.findAll{it.key.startsWith("tapName")}
	state.taps = []
	for(tap in taps) {
		def id = tap.key.replace("tapName", "")
		if (id.isInteger()) {
			if (tap.value != null) {
				def name = tap.value
				def pistons = settings["tapPistons${id}"]
				if (name || pistons) {
					def t = [
						i: id.toInteger(),
						n: name,
						p: settings["tapPistons${id}"]
					]
					state.taps.push t
				}
			}
		}
	}
}

//cleans up conditions - this may be replaced by a complete rebuild of the app object from the settings
private cleanUpConditions(deleteGroups) {
	//go through each condition in the state config and delete it if no associated settings exist
	if (!state.config || !state.config.app) return
	_cleanUpCondition(state.config.app.conditions, deleteGroups)
	_cleanUpCondition(state.config.app.otherConditions, deleteGroups)
	cleanUpActions()
}

//helper function for _cleanUpConditions
private _cleanUpCondition(condition, deleteGroups) {
	def perf = now()
	def result = false

	if (condition.children) {
		//we cannot use a for each due to concurrent modifications
		//we're using a while instead
		def deleted = true
		while (deleted) {
			deleted = false
			for (def child in condition.children) {
				deleted = _cleanUpCondition(child, deleteGroups)
				result = result || deleted
				if (deleted) {
					break
				}
			}
		}
	}

	//if non-root condition
	if (condition.id > 0) {
		if (condition.children == null) {
			//if regular condition
			if (!(condition.cap in ["Ask Alexa Macro", "EchoSistant Profile", "IFTTT", "Piston", "CoRE Piston", "Mode", "Location Mode", "Smart Home Monitor", "Date & Time", "Time", "Routine", "Variable"]) && settings["condDevices${condition.id}"] == null) {
				deleteCondition(condition.id);
				return true
				//} else {
				//	updateCondition(condition)
			}
		} else {
			//if condition group
			if (deleteGroups && (condition.children.size() == 0)) {
				deleteCondition(condition.id);
				return true
			}
		}
	}
	updateCondition(condition)
	return result
}

private getConditionDescription(id, level = 0) {
	def condition = getCondition(id)
	def pre = ""
	def preNot = ""
	def tab = ""
	def aft = ""
	def conditionGroup = (condition.children != null)
	switch (level) {
		case 1:
			pre = " ┌ ("
			preNot = " ┌ NOT ("
			tab = " │	"
			aft = " └ )"
			break;
		case 2:
			pre = " │ ┌ ["
			preNot = " │ ┌ NOT ["
			tab = " │ │	"
			aft = " │ └ ]"
			break;
		case 3:
			pre = " │ │ ┌ <"
			preNot = " │ │ ┌ NOT {"
			tab = " │ │ │	"
			aft = " │ │ └ >"
			break;
	}
	if (!conditionGroup) {
		//single condition
		if (condition.attr == "time") {
			return getTimeConditionDescription(condition)
		}
		def capability = getCapabilityByDisplay(condition.cap)
		def virtualDevice = capability ? capability.virtualDevice : null
		def devices = virtualDevice ? null : settings["condDevices$id"]
		if (virtualDevice || (devices && devices.size())) {
			def evaluation = (virtualDevice ? "" : (devices.size() > 1 ? (condition.mode == "All" ? "Each of " : "Any of ") : ""))
			def deviceList = (virtualDevice ? (capability.virtualDeviceName ? capability.virtualDeviceName : virtualDevice.name) : buildDeviceNameList(devices, "or")) + " "
			def attr
			//some conditions use virtual devices (mainly location)
			if (virtualDevice) {
				attr = getAttributeByName(capability.attribute)
			} else {
				attr = getAttributeByName(condition.attr)
			}
			def attribute = attr.name + " "
			def unit = (attr && attr.unit ? attr.unit : "")
			def comparison = cleanUpComparison(condition.comp)
			//override comparison option type if we're dealing with a variable - take the variable's data type
			def comp = getComparisonOption(condition.attr, comparison, attr.name == "variable" ? condition.dt : null, devices && devices.size() ? devices[0] : null)
			def subDevices = capability.count && attr && (attr.name == capability.attribute) ? buildNameList(condition.sdev, "or") + " " : ""
			def values = " [ERROR]"
			def time = ""
			if (comp) {
				switch (comp.parameters) {
					case 0:
						values = ""
						break
					case 1:
						def o1 = condition.o1 ? (condition.o1 < 0 ? " - " : " + ") + condition.o1.abs() : ""
						values = " ${(condition.var1 ? "{" + condition.var1 + o1 + "}$unit" : (condition.dev1 ? "{[" + condition.dev1 + "'s ${condition.attr1 ? condition.attr1 : attr.name}]" + o1 + "}$unit" : (comparison.contains("one of") ? '[ ' + buildNameList(condition.val1, "or") + " ]" : condition.val1) + unit))}"
						break
					case 2:
						def o1 = condition.o1 ? (condition.o1 < 0 ? " - " : " + ") + condition.o1.abs() : ""
						def o2 = condition.o2 ? (condition.o2 < 0 ? " - " : " + ") + condition.o2.abs() : ""
						values = " ${(condition.var1 ? "{" + condition.var1 + o1 + "}$unit" : (condition.dev1 ? "{[" + condition.dev1 + "'s ${condition.attr1 ? condition.attr1 : attr.name}]" + o1 + "}$unit" : condition.val1 + unit)) + " - " + (condition.var2 ? "{" + condition.var2 + o2 + "}$unit" : (condition.dev2 ? "{[" + condition.dev2 + "'s ${condition.attr2 ? condition.attr2 : attr.name}]" + o2 + "}$unit" : condition.val2 + unit))}"
						break
				}
				if (comp.timed) {
					time = " for [ERROR]"
					if (comparison.contains("change")) {
						time = " in the last " + (condition.fort ? condition.fort : "[ERROR]")
					} else if (comparison.contains("stays")) {
						time = " for " + (condition.fort ? condition.fort : "[ERROR]")
					} else if (condition.for && condition.fort) {
						time = " " + condition.for + " " + condition.fort
					}
				}
			}
			if (virtualDevice) {
				attribute = ""
			}

			//post formatting
			switch (capability.name) {
				case "askAlexaMacro":
				case "echoSistantProfile":
				case "piston":
				case "routine":
					deviceList = "${capability.display} '${values.trim()}' was "
					values = ""
					break
				case "ifttt":
					deviceList = "IFTTT event '${values.trim()}' was "
					values = ""
					break
				case "variable":
					deviceList = "Variable ${condition.var ? "{${condition.var}}" : ""} (as ${condition.dt}) "
					break
			}

			return tab + (condition.not ? "!" : "") + (condition.trg ? triggerPrefix() : conditionPrefix()) + evaluation + deviceList + attribute + subDevices + comparison + values + time
		}
		return "Sorry, incomplete rule"
	} else {
		//condition group
		def grouping = condition.grp
		def negate = condition.not
		def result = (negate ? preNot : pre) + "\n"
		def cnt = 1
		for (child in condition.children) {
			result += getConditionDescription(child.id, level + (child.children == null ? 0 : 1)) + "\n" + (cnt < condition.children.size() ? tab + grouping + "\n" : "")
			cnt++
		}
		result += aft
		return result
	}
}

private getTimeConditionDescription(condition) {
	if (condition.attr != "time") {
		return "[ERROR]"
	}
	def attr = getAttributeByName(condition.attr)
	def comparison = cleanUpComparison(condition.comp)
	def comp = getComparisonOption(condition.attr, comparison)
	def result = (condition.trg ? triggerPrefix() + "Trigger " : conditionPrefix() + "Time ") + comparison
	def val1 = condition.val1 ? condition.val1 : ""
	def val2 = condition.val2 ? condition.val2 : ""
	if (attr && comp) {
		//is the condition a trigger?
		def trigger = (comp.trigger == comparison)
		def repeating = trigger
		for (def i = 1; i <= comp.parameters; i++) {
			def val = "${i == 1 ? val1 : val2}"
			def recurring = false
			def preciseTime = false

			if (val.contains("custom")) {
				//custom time
				val = formatTime(i == 1 ? condition.t1 : condition.t2)
				preciseTime = true
				//def hour = condition.t1.getHour()
				//def minute = condition.t2.getMinute()
			} else if (val.contains("time of variable")) {
				//custom time
				val = "$val {${condition.var1}}"
				repeating = !val.contains("date and time")
				//def hour = condition.t1.getHour()
				//def minute = condition.t2.getMinute()
			} else if (val.contains("every")) {
				recurring = true
				repeating = false
				//take out the "happens at" and replace it with "happens "... every [something]
				result = result.replace("happens at", "happens")
				if (val.contains("number")) {
					//multiple minutes or hours
					val = "every ${condition.e} ${val.contains("minute") ? "minutes" : "hours"}"
				} else {
					//one minute or one hour
					//no change to val
				}
			} else {
				//simple, no change to val
			}

			if (comparison.contains("around")) {
				def range = i == 1 ? condition.o1 : condition.o2
				val += " ± $range minute${range > 1 ? "s" : ""}"
			} else {
				if ((!preciseTime) && (!recurring)) {
					def offset = i == 1 ? condition.o1 : condition.o2
					if (offset == null) {
						offset = 0
					}
					def after = offset >= 0
					offset = offset.abs()
					if (offset != 0) {
						result = result.replace("happens at", "happens")
						val = "${offset} minute${offset > 1 ? "s" : ""} ${after ? "after" : "before"} $val"
					}
				}
			}

			if (i == 1) {
				val1 = val
			} else {
				val2 = val
			}

		}

		switch (comp.parameters) {
			case 1:
				result += " $val1"
				break
			case 2:
				result += " $val1 and $val2"
				break
		}

		//repeat options
		if (repeating) {
			def repeat = condition.r
			if (repeat) {
				if (repeat.contains("day")) {
					//every day
					//every N days
					if (repeat.contains("number")) {
						result += ", ${repeat.replace("number of ", condition.re > 2 ? "${condition.re} " : (condition.re == 2 ? "other " : "")).replace("days", condition.re > 2 ? "days" : "day")}"
					} else {
						result += ", $repeat"
					}
				}
				if (repeat.contains("week")) {
					//every day
					//every N days
					def dow = condition.rdw ? condition.rdw : "[ERROR]"
					if (repeat.contains("number")) {
						result += ", ${repeat.replace("number of ", condition.re > 2 ? "${condition.re} " : (condition.re == 2 ? "other " : "")).replace("weeks", condition.re > 2 ? "weeks" : "week").replace("week", "${dow}")}"
					} else {
						result += ", every $dow"
					}
				}
				if (repeat.contains("month")) {
					//every Nth of the month
					//every Nth of every N months
					//every first/second/last [dayofweek] of the month
					//every first/second/last [dayofweek] of every N months
					if (repeat.contains("number")) {
						result += ", " + formatDayOfMonth(condition.rd, condition.rdw) + " of ${repeat.replace("number of ", condition.re > 2 ? "${condition.re} " : (condition.re == 2 ? "other " : "")).replace("months", condition.re > 2 ? "months" : "month")}"
					} else {
						result += ", " + formatDayOfMonth(condition.rd, condition.rdw).replace("the", "every")
					}
				}
				if (repeat.contains("year")) {
					//oh boy, we got years too!
					def month = condition.rm ? condition.rm : "[ERROR]"
					if (repeat.contains("number")) {
						result += ", " + formatDayOfMonth(condition.rd, condition.rdw) + " of ${month} of ${repeat.replace("number of ", condition.re > 2 ? "${condition.re} " : (condition.re == 2 ? "other " : "")).replace("years", condition.re > 2 ? "years" : "year")}"
					} else {
						result += ", " + formatDayOfMonth(condition.rd, condition.rdw).replace("the", "every") + " of ${month}"
					}
				}
			} else {
				result += " [REPEAT INCOMPLETE]"
			}
		}

		//filters
		if (condition.fmh || condition.fhd || condition.fdw || condition.fdm || condition.fwm || condition.fmy || condition.fy) {
			//we have some filters
			/*
				condition.fmh = settings["condMOH${condition.id}"]
				condition.fhd = settings["condHOD${condition.id}"]
				condition.fdw = settings["condDOW${condition.id}"]
				condition.fdm = settings["condDOM${condition.id}"]
				condition.fmy = settings["condMOY${condition.id}"]
				condition.fy = settings["condY${condition.id}"]
			*/
			result += ", but only if"
			def i = 0
			if (condition.fmh) {
				result += "${i > 0 ? ", and" : ""} the minute is ${buildNameList(condition.fmh, "or")}"
				i++
			}
			if (condition.fhd) {
				result += "${i > 0 ? ", and" : ""} the hour is ${buildNameList(condition.fhd, "or")}"
				i++
			}
			if (condition.fdw) {
				result += "${i > 0 ? ", and" : ""} the day of the week is ${buildNameList(condition.fdw, "or")}"
				i++
			}
			if (condition.fwm) {
				result += "${i > 0 ? ", and" : ""} the week is ${buildNameList(condition.fwm, "or")} of the month"
				i++
			}
			if (condition.fdm) {
				result += "${i > 0 ? ", and" : ""} the day is ${buildNameList(condition.fdm, "or")} of the month"
				i++
			}
			if (condition.fmy) {
				result += "${i > 0 ? ", and" : ""} the month is ${buildNameList(condition.fmy, "or")}"
				i++
			}
			if (condition.fy) {
				def odd = "odd years" in condition.fy
				def even = "even years" in condition.fy
				def leap = "leap years" in condition.fy
				def list = []
				//if we have both odd and even selected, that would match all years, so get out
				if (!(even && odd)) {
					if (odd || even || leap) {
						if (odd) list.push("odd")
						if (even) list.push("even")
						if (leap) list.push("leap")
					}
				}
				for(year in condition.fy) {
					if (!year.contains("year")) {
						list.push(year)
					}
				}
				if (list.size()) {
					result += "${i > 0 ? ", and" : ""} the year is ${buildNameList(list, "or")}"
				}
			}

		}
	}
	return result
}

/******************************************************************************/
/*** ACTION FUNCTIONS														***/
/******************************************************************************/

private getAction(actionId) {
	def parent = (state.run == "config" ? state.config : state)
	for(action in parent.app.actions) {
		if (action.id == actionId) {
			return action
		}
	}
	return null
}

private listActions(conditionId, onState = null) {
	def result = []
	def parent = (state.run == "config" ? state.config : state)

	//all actions for main groups
	if (conditionId <= 0) onState = null

	for(action in parent.app.actions) {
		if ((action.pid == conditionId) && ((onState == null) || ((action.rs == null ? true : action.rs) == onState))) {
			result.push(action)
		}
	}
	return result
}

private getActionTask(action, taskId) {
	if (!action) return null
	if (!(taskId instanceof Integer)) return null
	for (task in action.t) {
		if (task.i == taskId) {
			return task
		}
	}
	return null
}

/******************************************************************************/
/*** OTHER FUNCTIONS														***/
/******************************************************************************/

private sanitizeVariableName(name) {
	name = name ? "$name".trim().replace(" ", "_") : null
}

private sanitizeCommandName(name) {
	name = name ? "$name".trim().replace(" ", "_").replace("(", "_").replace(")", "_").replace("&", "_").replace("#", "_") : null
}

private importVariables(collection, prefix) {
	for(item in collection) {
		if (item.value instanceof Map) {
			importVariables(item.value, "${prefix}${item.key}.")
		} else {
			setVariable(prefix + item.key, item.value)
		}
	}
}

private cleanUpMap(map) {
	def washer = []
	//find dirty laundry
	for (item in map) {
		if (item.value == null) washer.push(item.key)
	}
	//clean it
	for (item in washer) {
		map.remove(item)
	}
	washer = null
	return map
}

private cleanUpAttribute(attribute) {
	if (attribute) {
		return attribute.replace(customAttributePrefix(), "")
	}
	return null
}

private cleanUpCommand(command) {
	if (command) {
		return command.replace(customCommandPrefix(), "").replace(virtualCommandPrefix(), "").replace(customCommandSuffix(), "")
	}
	return null
}

private cleanUpComparison(comparison) {
	if (comparison) {
		return comparison.replace(triggerPrefix(), "").replace(conditionPrefix(), "")
	}
	return null
}

private buildDeviceNameList(devices, suffix) {
	def cnt = 1
	def result = ""
	for (device in devices) {
		def label = getDeviceLabel(device)
		result += "$label" + (cnt < devices.size() ? (cnt == devices.size() - 1 ? " $suffix " : ", ") : "")
		cnt++
	}
	return result;
}

private buildNameList(list, suffix) {
	def cnt = 1
	def result = ""
	for (item in list) {
		result += item + (cnt < list.size() ? (cnt == list.size() - 1 ? "${list.size() > 2 ? "," : ""} $suffix " : ", ") : "")
		cnt++
	}
	return result;
}

private getDeviceLabel(device) {
	return device instanceof String ? device : (device ? ( device.label ? device.label : (device.name ? device.name : "$device")) : "Unknown device")
}

private getAlarmSystemStatus(value) {
	switch (value ? value : location.currentState("alarmSystemStatus")?.value) {
		case "off":
			return getAlarmSystemStatusOptions()[0]
		case "stay":
			return getAlarmSystemStatusOptions()[1]
		case "away":
			return getAlarmSystemStatusOptions()[2]
	}
	return null
}

private setAlarmSystemStatus(status) {
	def value = null
	def options = getAlarmSystemStatusOptions()
	switch (status) {
		case options[0]:
			value = "off"
			break
		case options[1]:
			value = "stay"
			break
		case options[2]:
			value = "away"
			break
	}
	if (value && (value != location.currentState("alarmSystemStatus")?.value)) {
		sendLocationEvent(name: 'alarmSystemStatus', value: value)
		return true
	}
	debug "WARNING: Could not set SHM status to '$status' because that status does not exist.", null, "warn"
	return false
}

private formatMessage(message, params = null) {
	if (message == null) {
		return message
	}
	message = "$message"
	def variables = message.findAll(/\{([^\{\}]*)?\}*/)
	def varMap = [:]
	for (variable in variables) {
		if (!(variable in varMap)) {
			def var = variable.replace("{", "").replace("}", "")
			def idx = var.isInteger() ? var.toInteger() : null
			def value = ""
			if (params && (idx >= 0) && (idx < params.size())) {
				value = "${params[idx].d != null ? params[idx].d : "(not set)"}"
			} else {
				value = getVariable(var, true)
			}
			varMap[variable] = value
		}
	}
	for(var in varMap) {
		if (var.value != null) {
			message = message.replace(var.key, "${var.value}")
		}
	}
	return message.toString().replace("|[", "{").replace("]|", "}")
}

/******************************************************************************/
/*** DATABASE FUNCTIONS														***/
/******************************************************************************/
//returns a list of all available capabilities
private listCapabilities(requireAttributes, requireCommands) {
	def result = []
	for (capability in capabilities()) {
		if ((requireAttributes && capability.attribute) || (requireCommands && capability.commands) || !(requireAttributes || requireCommands)) {
			result.push(capability.display)
		}
	}
	return result
}

//returns a list of all available attributes
private listAttributes() {
	def result = []
	for (attribute in attributes()) {
		result.push(attribute.name)
	}
	return result.sort()
}

//returns a list of possible comparison options for a selected attribute
private listComparisonOptions(attributeName, allowTriggers, overrideAttributeType = null, device = null) {
	def conditions = []
	def triggers = []
	def attribute = getAttributeByName(attributeName, device)
	def allowTimedComparisons = !(attributeName in ["askAlexaMacro", "echoSistantProfile", "mode", "ifttt", "alarmSystemStatus", "piston", "routineExecuted", "variable"])
	if (attribute) {
		def optionCount = attribute.options ? attribute.options.size() : 0
		def attributeType = overrideAttributeType ? overrideAttributeType : attribute.type
		for (comparison in comparisons()) {
			if (comparison.type == attributeType) {
				for (option in comparison.options) {
					if (option.condition && (!option.minOptions || option.minOptions <= optionCount) && (allowTimedComparisons || !option.timed)) {
						conditions.push(conditionPrefix() + option.condition)
					}
					if (allowTriggers && option.trigger && (!option.minOptions || option.minOptions <= optionCount) && (allowTimedComparisons || !option.timed)) {
						triggers.push(triggerPrefix() + option.trigger)
					}
				}
			}
		}
	}
	return conditions.sort() + triggers.sort()
}

//returns the comparison option object for the given attribute and selected comparison
private getComparisonOption(attributeName, comparisonOption, overrideAttributeType = null, device = null) {
	def attribute = getAttributeByName(attributeName, device)
	if (attribute && comparisonOption) {
		def attributeType = overrideAttributeType ? overrideAttributeType : (attributeName == "variable" ? "variable" : attribute.type)
		for (comparison in comparisons()) {
			if (comparison.type == attributeType) {
				for (option in comparison.options) {
					if (option.condition == comparisonOption) {
						return option
					}
					if (option.trigger == comparisonOption) {
						return option
					}
				}
			}
		}
	}
	return null
}

//returns true if the comparisonOption selected for the given attribute is a trigger-type condition
private isComparisonOptionTrigger(attributeName, comparisonOption, overrideAttributeType = null, device = null) {
	def attribute = getAttributeByName(attributeName, device)
	if (attribute) {
		def attributeType = overrideAttributeType ? overrideAttributeType : (attributeName == "variable" ? "variable" : attribute.type)
		for (comparison in comparisons()) {
			if (comparison.type == attributeType) {
				for (option in comparison.options) {
					if (option.condition == comparisonOption) {
						return false
					}
					if (option.trigger == comparisonOption) {
						return true
					}
				}
			}
		}
	}
	return false
}

//returns the list of attributes that exist for all devices in the provided list
private listCommonDeviceAttributes(devices) {
	def list = [:]
	def customList = [:]
	//build the list of standard attributes
	for (attribute in attributes()) {
		if (attribute.name.contains("*")) {
			for (def i = 1; i <= 32; i++) {
				list[attribute.name.replace("*", "$i")] = 0
			}
		} else {
			list[attribute.name] = 0
		}
	}

	for (device in devices) {
		if (device.hasCommand("describeAttributes")) {
			def payload = [attributes: null]
			device.describeAttributes(payload)
			if ((payload.attributes instanceof List) && payload.attributes.size()) {
				if (!state.customAttributes) state.customAttributes = [:]
				//save the custom attributes
				for( def customAttribute in payload.attributes) {
					if (customAttribute.name && customAttribute.type) {
						state.customAttributes[customAttribute.name] = customAttribute
					}
				}
			}
		}
	}

	//add known custom attributes to the standard list
	if (state.customAttributes) {
		for(def customAttribute in state.customAttributes) {
			list[customAttribute.key] = 0
		}
	}

	//get supported attributes
	for (device in devices) {
		def attrs = device.supportedAttributes
		for (attr in attrs) {
			if (list.containsKey(attr.name)) {
				//if attribute exists in standard list, increment its usage count
				list[attr.name] = list[attr.name] + 1
				if (attr.name == "threeAxis") {
					list["orientation"] = list["orientation"] + 1
					list["axisX"] = list["axisX"] + 1
					list["axisY"] = list["axisY"] + 1
					list["axisZ"] = list["axisZ"] + 1
				}
			} else {
				//otherwise increment the usage count in the custom list
				customList[attr.name] = customList[attr.name] ? customList[attr.name] + 1 : 1
			}
		}
	}
	def result = []
	//get all common attributes from the standard list
	for (item in list) {
		//ZWave Lock reports lock twice - others may do the same, so let's allow multiple instances
		if (item.value >= devices.size()) {
			result.push(item.key)
		}
	}
	//get all common attributes from the custom list
	for (item in customList) {
		//ZWave Lock reports lock twice - others may do the same, so let's allow multiple instances
		if (item.value >= devices.size()) {
			result.push(customAttributePrefix() + item.key)
		}
	}
	//return the sorted list
	return result.sort()
}

private listCommonDeviceSubDevices(devices, countAttributes, prefix = "") {
	def result = []
	def subDeviceCount = null
	def hasMainSubDevice = false
	//get supported attributes
	if (countAttributes) {
		countAttributes = "$countAttributes".tokenize(",")
	} else {
		countAttributes = []
	}
	for (device in devices) {
		def cnt = device.name.toLowerCase().contains("lock") ? 32 : 1
		switch (device.name) {
			case "Aeon Minimote":
			case "Aeon Key Fob":
			case "Simulated Minimote":
				cnt = 4
				break
		}
		if (countAttributes.size()) {
			for(countAttribute in countAttributes) {
				def c = cast(device.currentValue(countAttribute), "number")
				if (c) {
					cnt = c
					break
				}
			}
		}
		if (cnt instanceof String) {
			cnt = cnt.isInteger() ? cnt.toInteger() : 0
		}
		if (cnt instanceof Integer) {
			subDeviceCount = (subDeviceCount == null) || (cnt < subDeviceCount) ? (int) cnt : subDeviceCount
		}
	}
	if (subDeviceCount >= 2) {
		if (hasMainSubDevice) {
			result.push "Main ${prefix.toLowerCase()}"
		}
		for(def i = 1; i <= subDeviceCount; i++) {
			result.push "$prefix #$i".trim()
		}
	}
	//return the sorted list
	return result
}

private listCommonDeviceCommands(devices, capabilities) {
	def list = [:]
	def customList = [:]
	//build the list of standard attributes
	for (command in commands()) {
		list[command.name] = 0
	}
	//get supported attributes
	for (device in devices) {
		def cmds = device.supportedCommands
		for (cmd in cmds) {
			def found = false
			for (capability in capabilities) {
				def name = capability + "." + cmd.name
				if (list.containsKey(name)) {
					//if attribute exists in standard list, increment its usage count
					list[name] = list[name] + 1
					found = true
				} else {
					name = name.replaceAll("[\\d]", "") + "*"
					if (list.containsKey(name)) {
						list[name] = list[name] + 1
						found = true
					}
				}
			}
			if (!found && list.containsKey(cmd.name)) {
				//if attribute exists in standard list, increment its usage count
				list[cmd.name] = list[cmd.name] + 1
				found = true
			}
			if (!found) {
				//otherwise increment the usage count in the custom list
				customList[cmd.name] = customList[cmd.name] ? customList[cmd.name] + 1 : 1
			}
		}
	}

	def result = []
	//get all common attributes from the standard list
	for (item in list) {
		//ZWave Lock reports lock twice - others may do the same, so let's allow multiple instances
		if (item.value >= devices.size()) {
			def command = getCommandByName(item.key)
			if (command && command.display) {
				result.push(command.display)
			}
		}
	}
	//get all common attributes from the custom list
	for (item in customList) {
		//ZWave Lock reports lock twice - others may do the same, so let's allow multiple instances
		if (item.value >= devices.size()) {
			result.push(customCommandPrefix() + item.key + customCommandSuffix())
		}
	}
	//return the sorted list
	return result.sort()
}

private getCapabilityByName(name) {
	for (capability in capabilities()) {
		if (capability.name == name) {
			return capability
		}
	}
	return null
}

private getCapabilityByDisplay(display) {
	for (capability in capabilities()) {
		if (capability.display == display) {
			return capability
		}
	}
	return null
}

private getAttributeByName(name, device = null) {
	def name2 = name instanceof String ? name.replaceAll("[\\d]", "").trim() + "*" : null
	def attribute = attributes().find{ (it.name == name) || (name2 && (it.name == name2)) }
	if (attribute) return attribute
	if (state.customAttributes) {
		def item = state.customAttributes.find{ it.key == name }
		if (item) return item.value
	}
	//give up, return whatever...
	if (device) {
		def attr = device.supportedAttributes.find{ it.name == name }
		if (attr) {
			return [ name: attr.name, type: attr.dataType.toLowerCase(), range: null, unit: null, options: attr.values ]
		}
	}
	return [ name: name, type: "text", range: null, unit: null, options: null]
}

//returns all available command categories
private listCommandCategories() {
	def categories = []
	for(def command in commands()) {
		if (command.category && command.group && !(command.category in categories)) {
			categories.push(command.category)
		}
	}
	return categories
}

//returns all available commands in a category
private listCategoryCommands(category) {
	def result = []
	for(def command in commands()) {
		if ((command.category == category) && command.group && !(command.name in result)) {
			result.push(command)
		}
	}
	return result
}

//gets a category and command and returns the user friendly display name
private getCommand(category, name) {
	for(def command in commands()) {
		if ((command.category == category) && (command.name == name)) {
			return command
		}
	}
	return null
}

private getCommandByName(name) {
	for(def command in commands()) {
		if (command.name == name) {
			return command
		}
	}
	return null
}

private getVirtualCommandByName(name) {
	def cmds = virtualCommands()
	for(def command in cmds) {
		if (command.name == name) {
			return command
		}
	}
	return null
}

private getCommandByDisplay(display) {
	def cmds = commands()
	for(def command in cmds) {
		if (command.display == display) {
			return command
		}
	}
	return null
}

private getVirtualCommandByDisplay(display) {
	def cmds = virtualCommands()
	for(def command in cmds) {
		if (command.display == display) {
			return command
		}
	}
	return null
}

//gets a category and command and returns the user friendly display name
private getCommandGroupName(category, name) {
	def command = getCommand(category, name)
	return getCommandGroupName(command)
}

private getCommandGroupName(command) {
	if (!command) {
		return null
	}
	if (!command.group) {
		return null
	}
	if (command.group.contains("[devices]")) {
		def list = []
		for (capability in listCommandCapabilities(command)) {
			if ((capability.devices) && !(capability.devices in list)){
				list.push(capability.devices)
			}
		}
		return command.group.replace("[devices]", buildNameList(list, "or"))
	} else {
		return command.group
	}
}

//gets a category and command and returns the user friendly display name
private listCommandCapabilities(command) {
	//first off, find all commands that are capability-custom (i.e. name is of format <capability>.<name>)
	//we need to exclude these capabilities
	//if our name is of form <capability>.<name>
	if (command.name.contains(".")) {
		//easy, we only have one capability
		def cap = getCapabilityByName(command.name.tokenize(".")[0])
		if (!cap) {
			return []
		}
		return [cap]
	}
	def excludeList = []
	for(def c in commands()) {
		if (c.name.endsWith(".${command.name}")) {
			//get the capability and add it to an exclude list
			excludeList.push(c.name.tokenize(".")[0])
		}
	}
	//now get the capability names
	def result = []
	for(def c in capabilities()) {
		if (!(c.name in excludeList) && c.commands && (command.name in c.commands) && !(c in result)) {
			result.push(c)
		}
	}
	return result
}

private parseCommandParameter(parameter) {
	if (!parameter) {
		return null
	}

	def required = !(parameter && parameter.startsWith("?"))
	if (!required) {
		parameter = parameter.substring(1)
	}

	def last = (parameter && parameter.startsWith("*"))
	if (last) {
		parameter = parameter.substring(1)
	}

	//split by :
	def tokens = parameter.tokenize(":")
	if (tokens.size() < 2) {
		return [title: tokens[0], type: "text", required: required, last: last]
	}
	def title = ""
	def dataType = ""
	if (tokens.size() == 2) {
		title = tokens[0]
		dataType = tokens[1]
	} else {
		//title contains at least one :, so we rebuild it
		for(def i=0; i < tokens.size() - 1; i++) {
			title += (title ? ":" : "") + tokens[i]
		}
		dataType = tokens[tokens.size() - 1]
	}

	if (dataType in ["askAlexaMacro", "echoSistantProfile", "ifttt", "attribute", "attributes", "contact", "contacts", "variable", "variables", "lifxScenes", "stateVariable", "stateVariables", "routine", "piston", "aggregation", "dataType"]) {
		//special case handled internally
		return [title: title, type: dataType, required: required, last: last]
	}
	tokens = dataType.tokenize("[]")
	if (tokens.size()) {
		dataType = tokens[0]
		switch (tokens.size()) {
			case 1:
				switch (dataType) {
					case "string":
					case "text":
						return [title: title, type: "text", required: required, last: last]
					case "bool":
					case "email":
					case "time":
					case "phone":
					case "contact":
					case "number":
					case "decimal":
					case "var":
						return [title: title, type: dataType, required: required, last: last]
					case "color":
						return [title: title, type: "enum", options: colorOptions(), required: required, last: last]
				}
				break
			case 2:
				switch (dataType) {
					case "string":
					case "text":
						return [title: title, type: "text", required: required, last: last]
					case "bool":
					case "email":
					case "time":
					case "phone":
					case "contact":
					case "number":
					case "decimal":
						return [title: title, type: dataType, range: tokens[1], required: required, last: last]
					case "enum":
						return [title: title, type: dataType, options: tokens[1].tokenize(","), required: required, last: last]
					case "enums":
						return [title: title, type: "enum", options: tokens[1].tokenize(","), required: required, last: last, multiple: true]
				}
				break
		}
	}

	//check to see if dataType is an attribute, we use the attribute declaration then
	def attr = getAttributeByName(dataType)
	if (attr) {
		return [title: title + (attr.unit ? " (${attr.unit})" : ""), type: attr.type, range: attr.range, options: attr.options, required: required, last: last]
	}

	//give up
	return null
}

/******************************************************************************/
/*** DATABASE																***/
/******************************************************************************/

private capabilities() {
	return [
		[ name: "accelerationSensor",				display: "Acceleration Sensor",				attribute: "acceleration",				multiple: true,			devices: "acceleration sensors",	],
		[ name: "alarm",							display: "Alarm",							attribute: "alarm",						commands: ["off", "strobe", "siren", "both"],										multiple: true,			devices: "sirens",			],
		[ name: "askAlexaMacro",					display: "Ask Alexa Macro",					attribute: "askAlexaMacro",				commands: [],																		multiple: true,			virtualDevice: location,	virtualDeviceName: "Ask Alexa Macro"	],
		[ name: "audioNotification",				display: "Audio Notification",				commands: ["playText", "playSoundAndTrack", "playText", "playTextAndResume", "playTextAndRestore", "playTrack", "playTrackAndResume", "playTrackAndRestore", "playTrackAtVolume"],	multiple: true,			devices: "audio notification devices", ],
		[ name: "doorControl",						display: "Automatic Door",					attribute: "door",						commands: ["open", "close"],														multiple: true,			devices: "doors",			],
		[ name: "garageDoorControl",				display: "Automatic Garage Door",			attribute: "door",						commands: ["open", "close"],														multiple: true,			devices: "garage doors",		],
		[ name: "battery",							display: "Battery",							attribute: "battery",					multiple: true,			devices: "battery powered devices",	],
		[ name: "beacon",							display: "Beacon",							attribute: "presence",					multiple: true,			devices: "beacons",	],
		[ name: "switch",							display: "Bulb",							attribute: "switch",					commands: ["on", "off"],															multiple: true,			devices: "lights", 			],
		[ name: "button",							display: "Button",							attribute: "button",					multiple: true,			devices: "buttons",			count: "numberOfButtons,numButtons", data: "buttonNumber", momentary: true],
		[ name: "imageCapture",						display: "Camera",							attribute: "image",						commands: ["take"],																	multiple: true,			devices: "cameras",			],
		[ name: "carbonDioxideMeasurement",			display: "Carbon Dioxide Measurement",		attribute: "carbonDioxide",				multiple: true,			devices: "carbon dioxide sensors",	],
		[ name: "carbonMonoxideDetector",			display: "Carbon Monoxide Detector",		attribute: "carbonMonoxide",			multiple: true,			devices: "carbon monoxide detectors",	],
		[ name: "colorControl",						display: "Color Control",					attribute: "color",						commands: ["setColor", "setHue", "setSaturation"],									multiple: true,			devices: "RGB/W lights"		],
		[ name: "colorTemperature",					display: "Color Temperature",				attribute: "colorTemperature",			commands: ["setColorTemperature"],													multiple: true,			devices: "RGB/W lights",	],
		[ name: "configure",						display: "Configure",						commands: ["configure"],															multiple: true,			devices: "configurable devices",	],
		[ name: "consumable",						display: "Consumable",						attribute: "consumable",				commands: ["setConsumableStatus"],													multiple: true,			devices: "consumables",	],
		[ name: "contactSensor",					display: "Contact Sensor",					attribute: "contact",					multiple: true,			devices: "contact sensors",	],
		[ name: "piston",							display: "CoRE Piston",						attribute: "piston",					commands: ["executePiston"],														multiple: true,			virtualDevice: location,	virtualDeviceName: "Piston"	],
		[ name: "dateAndTime",						display: "Date & Time",						attribute: "time",						commands: null, /* wish we could control time */									multiple: true,			, virtualDevice: [id: "time", name: "time"],		virtualDeviceName: "Date & Time"	],
		[ name: "switchLevel",						display: "Dimmable Light",					attribute: "level",						commands: ["setLevel"],																multiple: true,			devices: "dimmable lights",	],
		[ name: "switchLevel",						display: "Dimmer",							attribute: "level",						commands: ["setLevel"],																multiple: true,			devices: "dimmable lights",	],
		[ name: "echoSistantProfile",				display: "EchoSistant Profile",				attribute: "echoSistantProfile",		commands: [],																		multiple: true,			virtualDevice: location,	virtualDeviceName: "EchoSistant Profile"	],
		[ name: "energyMeter",						display: "Energy Meter",					attribute: "energy",					multiple: true,			devices: "energy meters"],
		[ name: "ifttt",							display: "IFTTT",							attribute: "ifttt",						commands: [],																		multiple: false,		virtualDevice: location,	virtualDeviceName: "IFTTT"	],
		[ name: "illuminanceMeasurement",			display: "Illuminance Measurement",			attribute: "illuminance",				multiple: true,			devices: "illuminance sensors",	],
		[ name: "imageCapture",						display: "Image Capture",					attribute: "image",						commands: ["take"],																	multiple: true,			devices: "cameras"],
		[ name: "indicator",						display: "Indicator",						attribute: "indicatorStatus",			multiple: true,			devices: "indicator devices"],
		[ name: "waterSensor",						display: "Leak Sensor",						attribute: "water",						multiple: true,			devices: "leak sensors",	],
		[ name: "switch",							display: "Light Bulb",						attribute: "switch",					commands: ["on", "off"],															multiple: true,			devices: "lights", 			],
		[ name: "locationMode",						display: "Location Mode",					attribute: "mode",						commands: ["setMode"],																multiple: false,		devices: "location", virtualDevice: location	],
		[ name: "lock",								display: "Lock",							attribute: "lock",						commands: ["lock", "unlock"],						count: "numberOfCodes,numCodes", data: "usedCode", subDisplay: "By user code", multiple: true,			devices: "electronic locks", ],
		[ name: "mediaController",					display: "Media Controller",				attribute: "currentActivity",			commands: ["startActivity", "getAllActivities", "getCurrentActivity"],				multiple: true,			devices: "media controllers"],
		[ name: "locationMode",						display: "Mode",							attribute: "mode",						commands: ["setMode"],																multiple: false,		devices: "location", virtualDevice: location	],
		[ name: "momentary",						display: "Momentary",						commands: ["push"],																	multiple: true,			devices: "momentary switches"],
		[ name: "motionSensor",						display: "Motion Sensor",					attribute: "motion",					multiple: true,			devices: "motion sensors",	],
		[ name: "musicPlayer",						display: "Music Player",					attribute: "status",					commands: ["play", "pause", "stop", "nextTrack", "playTrack", "setLevel", "playText", "mute", "previousTrack", "unmute", "setTrack", "resumeTrack", "restoreTrack"],	multiple: true,			devices: "music players", ],
		[ name: "notification",						display: "Notification",					commands: ["deviceNotification"],													multiple: true,			devices: "notification devices",	],
		[ name: "pHMeasurement",					display: "pH Measurement",					attribute: "pH",						multiple: true,			devices: "pH sensors",	],
		[ name: "occupancy",						display: "Occupancy",						attribute: "occupancy",					multiple: true,			devices: "occupancy detectors",	],
		[ name: "piston",							display: "Piston",							attribute: "piston",					commands: ["executePiston"],														multiple: true,			virtualDevice: location,	virtualDeviceName: "Piston"	],
		[ name: "polling",							display: "Polling",							commands: ["poll"],																	multiple: true,			devices: "pollable devices",	],
		[ name: "powerMeter",						display: "Power Meter",						attribute: "power",						multiple: true,			devices: "power meters",	],
		[ name: "power",							display: "Power",							attribute: "powerSource",				multiple: true,			devices: "powered devices",	],
		[ name: "presenceSensor",					display: "Presence Sensor",					attribute: "presence",					multiple: true,			devices: "presence sensors",	],
		[ name: "refresh",							display: "Refresh",							commands: ["refresh"],																multiple: true,			devices: "refreshable devices",	],
		[ name: "relativeHumidityMeasurement",		display: "Relative Humidity Measurement",	attribute: "humidity",					multiple: true,			devices: "humidity sensors",	],
		[ name: "relaySwitch",						display: "Relay Switch",					attribute: "switch",					commands: ["on", "off"],															multiple: true,			devices: "relays",			],
		[ name: "routine",							display: "Routine",							attribute: "routineExecuted",			commands: ["executeRoutine"],														multiple: true,			virtualDevice: location,	virtualDeviceName: "Routine"	],
		[ name: "sensor",							display: "Sensor",							attribute: "sensor",					multiple: true,			devices: "sensors",	],
		[ name: "shockSensor",						display: "Shock Sensor",					attribute: "shock",						multiple: true,			devices: "shock sensors",	],
		[ name: "signalStrength",					display: "Signal Strength",					attribute: "lqi",						multiple: true,			devices: "wireless devices",	],
		[ name: "alarm",							display: "Siren",							attribute: "alarm",						commands: ["off", "strobe", "siren", "both"],										multiple: true,			devices: "sirens",			],
		[ name: "sleepSensor",						display: "Sleep Sensor",					attribute: "sleeping",					multiple: true,			devices: "sleep sensors",	],
		[ name: "smartHomeMonitor",					display: "Smart Home Monitor",				attribute: "alarmSystemStatus",			commands: ["setAlarmSystemStatus"],																		multiple: true,			, virtualDevice: location,	virtualDeviceName: "Smart Home Monitor"	],
		[ name: "smokeDetector",					display: "Smoke Detector",					attribute: "smoke",						multiple: true,			devices: "smoke detectors",	],
		[ name: "soundSensor",						display: "Sound Sensor",					attribute: "sound",						multiple: true,			devices: "sound sensors",	],
		[ name: "speechSynthesis",					display: "Speech Synthesis",				commands: ["speak"],																multiple: true,			devices: "speech synthesizers", ],
		[ name: "switch",							display: "Switch",							attribute: "switch",					commands: ["on", "off"],															multiple: true,			devices: "switches",			],
		[ name: "switchLevel",						display: "Switch Level",					attribute: "level",						commands: ["setLevel"],																multiple: true,			devices: "dimmers" ],
		[ name: "soundPressureLevel",				display: "Sound Pressure Level",			attribute: "soundPressureLevel",		multiple: true,			devices: "sound pressure sensors",	],
		[ name: "consumable",						display: "Stock Management",				attribute: "consumable",				multiple: true,			devices: "consumables",	],
		[ name: "tamperAlert",						display: "Tamper Alert",					attribute: "tamper",					multiple: true,			devices: "tamper sensors",	],
		[ name: "temperatureMeasurement",			display: "Temperature Measurement",			attribute: "temperature",				multiple: true,			devices: "temperature sensors",	],
		[ name: "thermostat",						display: "Thermostat",						attribute: "temperature",				commands: ["setHeatingSetpoint", "setCoolingSetpoint", "off", "heat", "emergencyHeat", "cool", "setThermostatMode", "fanOn", "fanAuto", "fanCirculate", "setThermostatFanMode", "auto"],	multiple: true,		devices: "thermostats",	showAttribute: true],
		[ name: "thermostatCoolingSetpoint",		display: "Thermostat Cooling Setpoint",		attribute: "coolingSetpoint",			commands: ["setCoolingSetpoint"],													multiple: true,			],
		[ name: "thermostatFanMode",				display: "Thermostat Fan Mode",				attribute: "thermostatFanMode",			commands: ["fanOn", "fanAuto", "fanCirculate", "setThermostatFanMode"],				multiple: true,			devices: "fans",	],
		[ name: "thermostatHeatingSetpoint",		display: "Thermostat Heating Setpoint",		attribute: "heatingSetpoint",			commands: ["setHeatingSetpoint"],													multiple: true,			],
		[ name: "thermostatMode",					display: "Thermostat Mode",					attribute: "thermostatMode",			commands: ["off", "heat", "emergencyHeat", "cool", "auto", "setThermostatMode"],	multiple: true,			],
		[ name: "thermostatOperatingState",			display: "Thermostat Operating State",		attribute: "thermostatOperatingState",	multiple: true,			],
		[ name: "thermostatSetpoint",				display: "Thermostat Setpoint",				attribute: "thermostatSetpoint",		multiple: true,			],
		[ name: "threeAxis",						display: "Three Axis Sensor",				attribute: "orientation",				multiple: true,			devices: "three axis sensors",	],
		[ name: "dateAndTime",						display: "Time",							attribute: "time",						multiple: true,			, virtualDevice: [id: "time", name: "time"],		virtualDeviceName: "Date & Time"	],
		[ name: "timedSession",						display: "Timed Session",					attribute: "sessionStatus",				commands: ["setTimeRemaining", "start", "stop", "pause", "cancel"],					multiple: true,			devices: "timed sessions"],
		[ name: "tone",								display: "Tone Generator",					commands: ["beep"],																	multiple: true,			devices: "tone generators",	],
		[ name: "touchSensor",						display: "Touch Sensor",					attribute: "touch",						multiple: true,			],
		[ name: "valve",							display: "Valve",							attribute: "contact",					commands: ["open", "close"],														multiple: true,			devices: "valves",			],
		[ name: "variable",							display: "Variable",						attribute: "variable",					commands: ["setVariable"],															multiple: true,			virtualDevice: location,	virtualDeviceName: "Variable"	],
		[ name: "voltageMeasurement",				display: "Voltage Measurement",				attribute: "voltage",					multiple: true,			devices: "volt meters",	],
		[ name: "waterSensor",						display: "Water Sensor",					attribute: "water",						multiple: true,			devices: "leak sensors",	],
		[ name: "windowShade",						display: "Window Shade",					attribute: "windowShade",				commands: ["open", "close", "presetPosition"],										multiple: true,			devices: "window shades",	],
	]
}

private commands() {
	def tempUnit = "°" + location.temperatureScale
	def defGroup = "Control [devices]"
	return [
		[ name: "locationMode.setMode",						category: "Location",					group: "Control location mode, Smart Home Monitor, routines, pistons, variables, and more...",		display: "Set location mode",			],
		[ name: "smartHomeMonitor.setAlarmSystemStatus",	category: "Location",					group: "Control location mode, Smart Home Monitor, routines, pistons, variables, and more...",		display: "Set Smart Home Monitor status",],
		[ name: "on",										category: "Convenience",				group: defGroup,			display: "Turn on", 						attribute: "switch",	value: "on",	],
		[ name: "on1",										display: "Turn on #1", 						attribute: "switch1",	value: "on",	],
		[ name: "on2",										display: "Turn on #2", 						attribute: "switch2",	value: "on",	],
		[ name: "on3",										display: "Turn on #3", 						attribute: "switch3",	value: "on",	],
		[ name: "on4",										display: "Turn on #4", 						attribute: "switch4",	value: "on",	],
		[ name: "on5",										display: "Turn on #5", 						attribute: "switch5",	value: "on",	],
		[ name: "on6",										display: "Turn on #6", 						attribute: "switch6",	value: "on",	],
		[ name: "on7",										display: "Turn on #7", 						attribute: "switch7",	value: "on",	],
		[ name: "on8",										display: "Turn on #8", 						attribute: "switch8",	value: "on",	],
		[ name: "off",										category: "Convenience",				group: defGroup,			display: "Turn off",						attribute: "switch",	value: "off",	],
		[ name: "off1",										display: "Turn off #1",						attribute: "switch1",	value: "off",	],
		[ name: "off2",										display: "Turn off #2",						attribute: "switch2",	value: "off",	],
		[ name: "off3",										display: "Turn off #3",						attribute: "switch3",	value: "off",	],
		[ name: "off4",										display: "Turn off #4",						attribute: "switch4",	value: "off",	],
		[ name: "off5",										display: "Turn off #5",						attribute: "switch5",	value: "off",	],
		[ name: "off6",										display: "Turn off #6",						attribute: "switch6",	value: "off",	],
		[ name: "off7",										display: "Turn off #7",						attribute: "switch7",	value: "off",	],
		[ name: "off8",										display: "Turn off #8",						attribute: "switch8",	value: "off",	],
		[ name: "toggle",									display: "Toggle",		],
		[ name: "toggle1",									display: "Toggle #1",	],
		[ name: "toggle2",									display: "Toggle #1",	],
		[ name: "toggle3",									display: "Toggle #1",	],
		[ name: "toggle4",									display: "Toggle #1",	],
		[ name: "toggle5",									display: "Toggle #1",	],
		[ name: "toggle6",									display: "Toggle #1",	],
		[ name: "toggle7",									display: "Toggle #1",	],
		[ name: "toggle8",									display: "Toggle #1",	],
		[ name: "setColor",									category: "Convenience",				group: defGroup,			display: "Set color",					parameters: ["?*Color:color","?*RGB:text","Hue:hue","Saturation:saturation","Lightness:level"], 	attribute: "color",		value: "*|color",	],
		[ name: "setLevel",									category: "Convenience",				group: defGroup,			display: "Set level",					parameters: ["Level:level"], description: "Set level to {0}%",		attribute: "level",		value: "*|number",	],
		[ name: "setHue",									category: "Convenience",				group: defGroup,			display: "Set hue",						parameters: ["Hue:hue"], description: "Set hue to {0}°",	attribute: "hue",		value: "*|number",	],
		[ name: "setSaturation",							category: "Convenience",				group: defGroup,			display: "Set saturation",				parameters: ["Saturation:saturation"], description: "Set saturation to {0}%",	attribute: "saturation",		value: "*|number",	],
		[ name: "setColorTemperature",						category: "Convenience",				group: defGroup,			display: "Set color temperature",		parameters: ["Color Temperature:colorTemperature"], description: "Set color temperature to {0}°K",	attribute: "colorTemperature",		value: "*|number",	],
		[ name: "open",										category: "Convenience",				group: defGroup,			display: "Open",						attribute: "door",		value: "open",	],
		[ name: "close",									category: "Convenience",				group: defGroup,			display: "Close",						attribute: "door",		value: "close",	],
		[ name: "windowShade.open",							category: "Convenience",				group: defGroup,			display: "Open fully",					],
		[ name: "windowShade.close",						category: "Convenience",				group: defGroup,			display: "Close fully",					],
		[ name: "windowShade.presetPosition",				category: "Convenience",				group: defGroup,			display: "Move to preset position",		],
		[ name: "lock",										category: "Safety and Security",		group: defGroup,			display: "Lock",						attribute: "lock",		value: "locked",	],
		[ name: "unlock",									category: "Safety and Security",		group: defGroup,			display: "Unlock",						attribute: "lock",		value: "unlocked",	],
		[ name: "take",										category: "Safety and Security",		group: defGroup,			display: "Take a picture",				],
		[ name: "alarm.off",								category: "Safety and Security",		group: defGroup,			display: "Stop",						attribute: "alarm",		value: "off",	],
		[ name: "alarm.strobe",								category: "Safety and Security",		group: defGroup,			display: "Strobe",						attribute: "alarm",		value: "strobe",	],
		[ name: "alarm.siren",								category: "Safety and Security",		group: defGroup,			display: "Siren",						attribute: "alarm",		value: "siren",	],
		[ name: "alarm.both",								category: "Safety and Security",		group: defGroup,			display: "Strobe and Siren",			attribute: "alarm",		value: "both",	],
		[ name: "thermostat.off",							category: "Comfort",					group: defGroup,			display: "Set to Off",					attribute: "thermostatMode",	value: "off",	],
		[ name: "thermostat.heat",							category: "Comfort",					group: defGroup,			display: "Set to Heat",					attribute: "thermostatMode",	value: "heat",	],
		[ name: "thermostat.cool",							category: "Comfort",					group: defGroup,			display: "Set to Cool",					attribute: "thermostatMode",	value: "cool",	],
		[ name: "thermostat.auto",							category: "Comfort",					group: defGroup,			display: "Set to Auto",					attribute: "thermostatMode",	value: "auto",	],
		[ name: "thermostat.emergencyHeat",					category: "Comfort",					group: defGroup,			display: "Set to Emergency Heat",		attribute: "thermostatMode",	value: "emergency heat",	],
		[ name: "thermostat.quickSetHeat",					category: "Comfort",					group: defGroup,			display: "Quick set heating point",		parameters: ["Desired temperature:thermostatSetpoint"], description: "Set quick heating point at {0}$tempUnit",	],
		[ name: "thermostat.quickSetCool",					category: "Comfort",					group: defGroup,			display: "Quick set cooling point",		parameters: ["Desired temperature:thermostatSetpoint"], description: "Set quick cooling point at {0}$tempUnit",	],
		[ name: "thermostat.setHeatingSetpoint",			category: "Comfort",					group: defGroup,			display: "Set heating point",			parameters: ["Desired temperature:thermostatSetpoint"], description: "Set heating point at {0}$tempUnit",	attribute: "heatingSetpoint",	value: "*|decimal",	],
		[ name: "thermostat.setCoolingSetpoint",			category: "Comfort",					group: defGroup,			display: "Set cooling point",			parameters: ["Desired temperature:thermostatSetpoint"], description: "Set cooling point at {0}$tempUnit",	attribute: "coolingSetpoint",	value: "*|decimal",	],
		[ name: "thermostat.setThermostatMode",				category: "Comfort",					group: defGroup,			display: "Set thermostat mode",			parameters: ["Mode:thermostatMode"], description: "Set thermostat mode to {0}",	attribute: "thermostatMode",	value: "*|string",	],
		[ name: "fanOn",									category: "Comfort",					group: defGroup,			display: "Set fan to On",				],
		[ name: "fanCirculate",								category: "Comfort",					group: defGroup,			display: "Set fan to Circulate",		],
		[ name: "fanAuto",									category: "Comfort",					group: defGroup,			display: "Set fan to Auto",				],
		[ name: "setThermostatFanMode",						category: "Comfort",					group: defGroup,			display: "Set fan mode",				parameters: ["Fan mode:thermostatFanMode"], description: "Set fan mode to {0}",	],
		[ name: "play",										category: "Entertainment",				group: defGroup,			display: "Play",	],
		[ name: "pause",									category: "Entertainment",				group: defGroup,			display: "Pause",	],
		[ name: "stop",										category: "Entertainment",				group: defGroup,			display: "Stop",	],
		[ name: "nextTrack",								category: "Entertainment",				group: defGroup,			display: "Next track",					],
		[ name: "previousTrack",							category: "Entertainment",				group: defGroup,			display: "Previous track",				],
		[ name: "mute",										category: "Entertainment",				group: defGroup,			display: "Mute",	],
		[ name: "unmute",									category: "Entertainment",				group: defGroup,			display: "Unmute",	],
		[ name: "musicPlayer.setLevel",						category: "Entertainment",				group: defGroup,			display: "Set volume",					parameters: ["Level:level"], description: "Set volume to {0}%",	],
		[ name: "playText",									category: "Entertainment",				group: defGroup,			display: "Speak text",					parameters: ["Text:string", "?Volume:level"], description: "Speak text \"{0}\" at volume {1}", ],
		[ name: "playTextAndRestore",	display: "Speak text and restore",		parameters: ["Text:string","?Volume:level"], 	description: "Speak text \"{0}\" at volume {1} and restore", ],
		[ name: "playTextAndResume",	display: "Speak text and resume",		parameters: ["Text:string","?Volume:level"], 	description: "Speak text \"{0}\" at volume {1} and resume", ],
		[ name: "playTrack",								category: "Entertainment",				group: defGroup,			display: "Play track",					parameters: ["Track URI:string","?Volume:level"],				description: "Play track \"{0}\" at volume {1}",	],
		[ name: "playTrackAtVolume",	display: "Play track at volume",		parameters: ["Track URI:string","Volume:level"],description: "Play track \"{0}\" at volume {1}",	],
		[ name: "playTrackAndRestore",	display: "Play track and restore",		parameters: ["Track URI:string","?Volume:level"], 	description: "Play track \"{0}\" at volume {1} and restore", ],
		[ name: "playTrackAndResume",	display: "Play track and resume",		parameters: ["Track URI:string","?Volume:level"], 	description: "Play track \"{0}\" at volume {1} and resume", ],
		[ name: "setTrack",									category: "Entertainment",				group: defGroup,			parameters: ["Track URI:string"],	display: "Set track to '{0}'",					],
		[ name: "setLocalLevel",display: "Set local level",				parameters: ["Level:level"],	description: "Set local level to {0}", ],
		[ name: "resumeTrack",								category: "Entertainment",				group: defGroup,			display: "Resume track",				],
		[ name: "restoreTrack",								category: "Entertainment",				group: defGroup,			display: "Restore track",				],
		[ name: "speak",									category: "Entertainment",				group: defGroup,			display: "Speak",						parameters: ["Message:string"], description: "Speak \"{0}\"", ],
		[ name: "startActivity",							category: "Entertainment",				group: defGroup,			display: "Start activity",				parameters: ["Activity:string"], description: "Start activity\"{0}\"",	],
		[ name: "getCurrentActivity",						category: "Entertainment",				group: defGroup,			display: "Get current activity",		],
		[ name: "getAllActivities",							category: "Entertainment",				group: defGroup,			display: "Get all activities",			],
		[ name: "push",										category: "Other",						group: defGroup,			display: "Push",	],
		[ name: "beep",										category: "Other",						group: defGroup,			display: "Beep",	],
		[ name: "timedSession.setTimeRemaining",			category: "Other",						group: defGroup,			display: "Set remaining time",			parameters: ["Remaining time [s]:number"], description: "Set remaining time to {0}s",	],
		[ name: "timedSession.start",						category: "Other",						group: defGroup,			display: "Start timed session",			],
		[ name: "timedSession.stop",						category: "Other",						group: defGroup,			display: "Stop timed session",			],
		[ name: "timedSession.pause",						category: "Other",						group: defGroup,			display: "Pause timed session",			],
		[ name: "timedSession.cancel",						category: "Other",						group: defGroup,			display: "Cancel timed session",		],
		[ name: "setConsumableStatus",						category: "Other",						group: defGroup,			display: "Set consumable status",		parameters: ["Status:consumable"], description: "Set consumable status to {0}",	],
		[ name: "configure",	display: "Configure",					],
		[ name: "poll",			display: "Poll",	],
		[ name: "refresh",		display: "Refresh",	],
		/* predfined commands below */
		//general
		[ name: "reset",		display: "Reset",	],
		//hue
		[ name: "startLoop",	display: "Start color loop",			],
		[ name: "stopLoop",		display: "Stop color loop",				],
		[ name: "setLoopTime",	display: "Set loop duration",			parameters: ["Duration [s]:number[1..*]"], description: "Set loop duration to {0}s"],
		[ name: "setDirection",	display: "Switch loop direction",		description: "Set loop duration to {0}s"],
		[ name: "alert",		display: "Alert with lights",			parameters: ["Method:enum[Blink,Breathe,Okay,Stop]"], description: "Alert with lights: {0}"],
		[ name: "setAdjustedColor",display: "Transition to color",			parameters: ["Color:color","Duration [s]:number[1..60]"], description: "Transition to color {0} in {1}s"],
		//harmony
		[ name: "allOn",		display: "Turn all on",					],
		[ name: "allOff",		display: "Turn all off",				],
		[ name: "hubOn",		display: "Turn hub on",					],
		[ name: "hubOff",		display: "Turn hub off",				],
		//blink camera
		[ name: "enableCamera",	display: "Enable camera",				],
		[ name: "disableCamera",display: "Disable camera",				],
		[ name: "monitorOn",	display: "Turn monitor on",				],
		[ name: "monitorOff",	display: "Turn monitor off",			],
		[ name: "ledOn",		display: "Turn LED on",					],
		[ name: "ledOff",		display: "Turn LED off",				],
		[ name: "ledAuto",		display: "Set LED to Auto",				],
		[ name: "setVideoLength",display: "Set video length",			parameters: ["Seconds:number[1..120]"],	description: "Set video length to {0}s", ],
		//dlink camera
		[ name: "pirOn",		display: "Enable PIR motion detection",	],
		[ name: "pirOff",		display: "Disable PIR motion detection",],
		[ name: "nvOn",			display: "Set Night Vision to On",		],
		[ name: "nvOff",		display: "Set Night Vision to Off",		],
		[ name: "nvAuto",		display: "Set Night Vision to Auto",	],
		[ name: "vrOn",			display: "Enable local video recording",],
		[ name: "vrOff",		display: "Disable local video recording",],
		[ name: "left",			display: "Pan camera left",				],
		[ name: "right",		display: "Pan camera right",			],
		[ name: "up",			display: "Pan camera up",				],
		[ name: "down",			display: "Pan camera down",				],
		[ name: "home",			display: "Pan camera to the Home",		],
		[ name: "presetOne",	display: "Pan camera to preset #1",		],
		[ name: "presetTwo",	display: "Pan camera to preset #2",		],
		[ name: "presetThree",	display: "Pan camera to preset #3",		],
		[ name: "presetFour",	display: "Pan camera to preset #4",		],
		[ name: "presetFive",	display: "Pan camera to preset #5",		],
		[ name: "presetSix",	display: "Pan camera to preset #6",		],
		[ name: "presetSeven",	display: "Pan camera to preset #7",		],
		[ name: "presetEight",	display: "Pan camera to preset #8",		],
		[ name: "presetCommand",display: "Pan camera to custom preset",	parameters: ["Preset #:number[1..99]"], description: "Pan camera to preset #{0}",	],
		//zwave fan speed control by @pmjoen
		[ name: "low",	display: "Set to Low"],
		[ name: "med",	display: "Set to Medium"],
		[ name: "high",	display: "Set to High"],
	]
}

private virtualCommands() {
	def cmds = [
		[ name: "wait",				display: "Wait",							parameters: ["Time:number[1..1440]","Unit:enum[seconds,minutes,hours]"],													immediate: true,	location: true,	description: "Wait {0} {1}",	],
		[ name: "waitVariable",		display: "Wait (variable)",					parameters: ["Time (variable):variable","Unit:enum[seconds,minutes,hours]"],													immediate: true,	location: true,	description: "Wait |[{0}]| {1}",	],
		[ name: "waitRandom",		display: "Wait (random)",					parameters: ["At least:number[1..1440]","At most:number[1..1440]","Unit:enum[seconds,minutes,hours]"],	immediate: true,	location: true,	description: "Wait {0}-{1} {2}",	],
		[ name: "waitState",		display: "Wait for piston state change",	parameters: ["Change to:enum[any,false,true]"],															immediate: true,	location: true,						description: "Wait for {0} state"],
		[ name: "waitTime",			display: "Wait for common time",			parameters: ["Time:enum[midnight,sunrise,noon,sunset]","?Offset [minutes]:number[-1440..1440]","Days of week:enums[Monday,Tuesday,Wednesday,Thursday,Friday,Saturday,Sunday]"],							immediate: true,	location: true,						description: "Wait for next {0} (offset {1} min), on {2}"],
		[ name: "waitCustomTime",	display: "Wait for custom time",			parameters: ["Time:time","Days of week:enums[Monday,Tuesday,Wednesday,Thursday,Friday,Saturday,Sunday]"],							immediate: true,	location: true,						description: "Wait for {0}, on {1}"],
		[ name: "toggle",				requires: ["on", "off"], 			display: "Toggle",		],
		[ name: "toggle#1",				requires: ["on1", "off1"], 			display: "Toggle #1",		],
		[ name: "toggle#2",				requires: ["on2", "off2"], 			display: "Toggle #2",		],
		[ name: "toggle#3",				requires: ["on3", "off3"], 			display: "Toggle #3",		],
		[ name: "toggle#4",				requires: ["on4", "off4"], 			display: "Toggle #4",		],
		[ name: "toggle#5",				requires: ["on5", "off5"], 			display: "Toggle #5",		],
		[ name: "toggle#6",				requires: ["on6", "off6"], 			display: "Toggle #6",		],
		[ name: "toggle#7",				requires: ["on7", "off7"], 			display: "Toggle #7",		],
		[ name: "toggle#8",				requires: ["on8", "off8"], 			display: "Toggle #8",		],
		[ name: "toggleLevel",			requires: ["on", "off", "setLevel"],display: "Toggle level",					parameters: ["Level:level"],																																	description: "Toggle level between 0% and {0}%",	],
		[ name: "delayedOn",			requires: ["on"], 					display: "Turn on (delayed)",				parameters: ["Delay (ms):number[1..60000]"],																													description: "Turn on after {0}ms",	],
		[ name: "delayedOn#1",			requires: ["on1"], 					display: "Turn on #1 (delayed)",			parameters: ["Delay (ms):number[1..60000]"],																													description: "Turn on #1 after {0}ms",	],
		[ name: "delayedOn#2",			requires: ["on2"], 					display: "Turn on #2 (delayed)",			parameters: ["Delay (ms):number[1..60000]"],																													description: "Turn on #2 after {0}ms",	],
		[ name: "delayedOn#3",			requires: ["on3"], 					display: "Turn on #3 (delayed)",			parameters: ["Delay (ms):number[1..60000]"],																													description: "Turn on #3 after {0}ms",	],
		[ name: "delayedOn#4",			requires: ["on4"], 					display: "Turn on #4 (delayed)",			parameters: ["Delay (ms):number[1..60000]"],																													description: "Turn on #4 after {0}ms",	],
		[ name: "delayedOn#5",			requires: ["on5"], 					display: "Turn on #5 (delayed)",			parameters: ["Delay (ms):number[1..60000]"],																													description: "Turn on #5 after {0}ms",	],
		[ name: "delayedOn#6",			requires: ["on6"], 					display: "Turn on #6 (delayed)",			parameters: ["Delay (ms):number[1..60000]"],																													description: "Turn on #6 after {0}ms",	],
		[ name: "delayedOn#7",			requires: ["on7"], 					display: "Turn on #7 (delayed)",			parameters: ["Delay (ms):number[1..60000]"],																													description: "Turn on #7 after {0}ms",	],
		[ name: "delayedOn#8",			requires: ["on8"], 					display: "Turn on #8 (delayed)",			parameters: ["Delay (ms):number[1..60000]"],																													description: "Turn on #8 after {0}ms",	],
		[ name: "delayedOff",			requires: ["off"], 					display: "Turn off (delayed)",				parameters: ["Delay (ms):number[1..60000]"],																													description: "Turn off after {0}ms",	],
		[ name: "delayedOff#1",			requires: ["off1"],					display: "Turn off #1 (delayed)",			parameters: ["Delay (ms):number[1..60000]"],																													description: "Turn off #1 after {0}ms",	],
		[ name: "delayedOff#2",			requires: ["off2"],					display: "Turn off #2 (delayed)",			parameters: ["Delay (ms):number[1..60000]"],																													description: "Turn off #2 after {0}ms",	],
		[ name: "delayedOff#3",			requires: ["off3"],					display: "Turn off #3 (delayed)",			parameters: ["Delay (ms):number[1..60000]"],																													description: "Turn off #3 after {0}ms",	],
		[ name: "delayedOff#4",			requires: ["off4"],					display: "Turn off #4 (delayed)",			parameters: ["Delay (ms):number[1..60000]"],																													description: "Turn off #4 after {0}ms",	],
		[ name: "delayedOff#5",			requires: ["off5"],					display: "Turn off #5 (delayed)",			parameters: ["Delay (ms):number[1..60000]"],																													description: "Turn off #5 after {0}ms",	],
		[ name: "delayedOff#6",			requires: ["off7"],					display: "Turn off #6 (delayed)",			parameters: ["Delay (ms):number[1..60000]"],																													description: "Turn off #6 after {0}ms",	],
		[ name: "delayedOff#7",			requires: ["off7"],					display: "Turn off #7 (delayed)",			parameters: ["Delay (ms):number[1..60000]"],																													description: "Turn off #7 after {0}ms",	],
		[ name: "delayedOff#8",			requires: ["off8"],					display: "Turn off #8 (delayed)",			parameters: ["Delay (ms):number[1..60000]"],																													description: "Turn off #8 after {0}ms",	],
		[ name: "delayedToggle",		requires: ["on", "off"], 			display: "Toggle (delayed)",				parameters: ["Delay (ms):number[1..60000]"],																													description: "Toggle after {0}ms",	],
		[ name: "delayedToggle#1",		requires: ["on1", "off1"], 			display: "Toggle #1 (delayed)",				parameters: ["Delay (ms):number[1..60000]"],																													description: "Toggle #1 after {0}ms",	],
		[ name: "delayedToggle#2",		requires: ["on2", "off2"], 			display: "Toggle #2 (delayed)",				parameters: ["Delay (ms):number[1..60000]"],																													description: "Toggle #2 after {0}ms",	],
		[ name: "delayedToggle#3",		requires: ["on3", "off3"], 			display: "Toggle #3 (delayed)",				parameters: ["Delay (ms):number[1..60000]"],																													description: "Toggle #3 after {0}ms",	],
		[ name: "delayedToggle#4",		requires: ["on4", "off4"], 			display: "Toggle #4 (delayed)",				parameters: ["Delay (ms):number[1..60000]"],																													description: "Toggle #4 after {0}ms",	],
		[ name: "delayedToggle#5",		requires: ["on5", "off5"], 			display: "Toggle #5 (delayed)",				parameters: ["Delay (ms):number[1..60000]"],																													description: "Toggle #5 after {0}ms",	],
		[ name: "delayedToggle#6",		requires: ["on6", "off6"], 			display: "Toggle #6 (delayed)",				parameters: ["Delay (ms):number[1..60000]"],																													description: "Toggle #6 after {0}ms",	],
		[ name: "delayedToggle#7",		requires: ["on7", "off7"], 			display: "Toggle #7 (delayed)",				parameters: ["Delay (ms):number[1..60000]"],																													description: "Toggle #7 after {0}ms",	],
		[ name: "delayedToggle#8",		requires: ["on8", "off8"], 			display: "Toggle #8 (delayed)",				parameters: ["Delay (ms):number[1..60000]"],																													description: "Toggle #8 after {0}ms",	],
		[ name: "setLevelVariable",		requires: ["setLevel"],				display: "Set level (variable)",						parameters: ["Level:variable"], description: "Set level to {0}%"],
		[ name: "setSaturationVariable",		requires: ["setSaturation"],				display: "Set saturation (variable)",						parameters: ["Saturation:variable"], description: "Set saturation to {0}%"],
		[ name: "setHueVariable",		requires: ["setHue"],				display: "Set hue (variable)",						parameters: ["Hue:variable"], description: "Set hue to {0}°"],
		[ name: "fadeLevelHW",			requires: ["setLevel"], 			display: "Fade to level (hardware)",		parameters: ["Target level:level","Duration (ms):number[1..60000]"],																							description: "Fade to {0}% in {1}ms",				],
		[ name: "fadeLevel",			requires: ["setLevel"], 			display: "Fade to level",					parameters: ["?Start level (optional):level","Target level:level","Duration (seconds):number[1..600]"],															description: "Fade level from {0}% to {1}% in {2}s",				],
		[ name: "fadeLevelVariable",	requires: ["setLevel"], 			display: "Fade to level (variable)",		parameters: ["?Start level (optional):variable","Target level:variable","Duration (seconds):number[1..600]"],															description: "Fade level from {0}% to {1}% in {2}s",				],
		[ name: "setLevelIf",			category: "Convenience",			group: "Control [devices]",					display: "Set level (advanced)",					parameters: ["Level:level","Only if switch state is:enum[on,off]"], description: "Set level to {0}% if switch is {1}",		attribute: "level",		value: "*|number",	],
		[ name: "adjustLevel",			requires: ["setLevel"], 			display: "Adjust level",					parameters: ["Adjustment (+/-):number[-100..100]"],																												description: "Adjust level by {0}%",	],
		[ name: "adjustLevelVariable",			requires: ["setLevel"], 			display: "Adjust level (variable)",					parameters: ["Adjustment (+/-):variable"],																												description: "Adjust level by {0}%",	],
		[ name: "fadeSaturation",		requires: ["setSaturation"],		display: "Fade to saturation",				parameters: ["?Start saturation (optional):saturation","Target saturation:saturation","Duration (seconds):number[1..600]"],											description: "Fade saturation from {0}% to {1}% in {2}s",				],
		[ name: "fadeSaturationVariable",requires: ["setSaturation"],		display: "Fade to saturation (variable)",				parameters: ["?Start saturation (optional):variable","Target saturation:variable","Duration (seconds):number[1..600]"],											description: "Fade saturation from {0}% to {1}% in {2}s",				],
		[ name: "adjustSaturation",		requires: ["setSaturation"],		display: "Adjust saturation",				parameters: ["Adjustment (+/-):number[-100..100]"],																												description: "Adjust saturation by {0}%",	],
		[ name: "adjustSaturationVariable",		requires: ["setSaturation"],		display: "Adjust saturation (variable)",				parameters: ["Adjustment (+/-):variable"],																												description: "Adjust saturation by {0}%",	],
		[ name: "fadeHue",				requires: ["setHue"], 				display: "Fade to hue",						parameters: ["?Start hue (optional):hue","Target hue:hue","Duration (seconds):number[1..600]"],																description: "Fade hue from {0}° to {1}° in {2}s",				],
		[ name: "fadeHueVariable",		requires: ["setHue"], 				display: "Fade to hue (variable)",			parameters: ["?Start hue (optional):variable","Target hue:variable","Duration (seconds):number[1..600]"],																description: "Fade hue from {0}° to {1}° in {2}s",				],
		[ name: "adjustHue",			requires: ["setHue"], 				display: "Adjust hue",						parameters: ["Adjustment (+/-):number[-360..360]"],																												description: "Adjust hue by {0}°",	],
		[ name: "adjustHueVariable",	requires: ["setHue"], 				display: "Adjust hue (variable)",			parameters: ["Adjustment (+/-):variable"],																												description: "Adjust hue by {0}°",	],
		[ name: "flash",				requires: ["on", "off"], 			display: "Flash",							parameters: ["On interval (milliseconds):number[250..5000]","Off interval (milliseconds):number[250..5000]","Number of flashes:number[1..10]"],					description: "Flash {0}ms/{1}ms for {2} time(s)",		],
		[ name: "flash#1",				requires: ["on1", "off1"], 			display: "Flash #1",						parameters: ["On interval (milliseconds):number[250..5000]","Off interval (milliseconds):number[250..5000]","Number of flashes:number[1..10]"],					description: "Flash #1 {0}ms/{1}ms for {2} time(s)",	],
		[ name: "flash#2",				requires: ["on2", "off2"], 			display: "Flash #2",						parameters: ["On interval (milliseconds):number[250..5000]","Off interval (milliseconds):number[250..5000]","Number of flashes:number[1..10]"],					description: "Flash #2 {0}ms/{1}ms for {2} time(s)",	],
		[ name: "flash#3",				requires: ["on3", "off3"], 			display: "Flash #3",						parameters: ["On interval (milliseconds):number[250..5000]","Off interval (milliseconds):number[250..5000]","Number of flashes:number[1..10]"],					description: "Flash #3 {0}ms/{1}ms for {2} time(s)",	],
		[ name: "flash#4",				requires: ["on4", "off4"], 			display: "Flash #4",						parameters: ["On interval (milliseconds):number[250..5000]","Off interval (milliseconds):number[250..5000]","Number of flashes:number[1..10]"],					description: "Flash #4 {0}ms/{1}ms for {2} time(s)",	],
		[ name: "flash#5",				requires: ["on5", "off5"], 			display: "Flash #5",						parameters: ["On interval (milliseconds):number[250..5000]","Off interval (milliseconds):number[250..5000]","Number of flashes:number[1..10]"],					description: "Flash #5 {0}ms/{1}ms for {2} time(s)",	],
		[ name: "flash#6",				requires: ["on6", "off6"], 			display: "Flash #6",						parameters: ["On interval (milliseconds):number[250..5000]","Off interval (milliseconds):number[250..5000]","Number of flashes:number[1..10]"],					description: "Flash #6 {0}ms/{1}ms for {2} time(s)",	],
		[ name: "flash#7",				requires: ["on7", "off7"], 			display: "Flash #7",						parameters: ["On interval (milliseconds):number[250..5000]","Off interval (milliseconds):number[250..5000]","Number of flashes:number[1..10]"],					description: "Flash #7 {0}ms/{1}ms for {2} time(s)",	],
		[ name: "flash#8",				requires: ["on8", "off8"], 			display: "Flash #8",						parameters: ["On interval (milliseconds):number[250..5000]","Off interval (milliseconds):number[250..5000]","Number of flashes:number[1..10]"],					description: "Flash #8 {0}ms/{1}ms for {2} time(s)",	],
		[ name: "setVariable",		display: "Set variable", 					parameters: ["Variable:var"],																				varEntry: 0, 						location: true,																	aggregated: true,	],
		[ name: "saveAttribute",	display: "Save attribute to variable", 		parameters: ["Attribute:attribute","Aggregation:aggregation","?Convert to data type:dataType","Save to variable:string"],					varEntry: 3,		description: "Save attribute '{0}' to variable |[{3}]|'",			aggregated: true,	],
		[ name: "saveState",		display: "Save state to variable",			parameters: ["Attributes:attributes","Aggregation:aggregation","?Convert to data type:dataType","Save to state variable:string"],			stateVarEntry: 3,	description: "Save state of attributes {0} to variable |[{3}]|'",	aggregated: true,	],
		[ name: "saveStateLocally",	display: "Capture state to local store",	parameters: ["Attributes:attributes","?Only if state is empty:bool"],																															description: "Capture state of attributes {0} to local store",		],
		[ name: "saveStateGlobally",display: "Capture state to global store",	parameters: ["Attributes:attributes","?Only if state is empty:bool"],																															description: "Capture state of attributes {0} to global store",	],
		[ name: "loadAttribute",	display: "Load attribute from variable",	parameters: ["Attribute:attribute","Load from variable:variable","Allow translations:bool","Negate translation:bool"],											description: "Load attribute '{0}' from variable |[{1}]|",	],
		[ name: "loadState",		display: "Load state from variable",		parameters: ["Attributes:attributes","Load from state variable:stateVariable","Allow translations:bool","Negate translation:bool"],								description: "Load state of attributes {0} from variable |[{1}]|"				],
		[ name: "loadStateLocally",	display: "Restore state from local store",	parameters: ["Attributes:attributes","?Empty the state:bool"],																															description: "Restore state of attributes {0} from local store",			],
		[ name: "loadStateGlobally",display: "Restore state from global store",	parameters: ["Attributes:attributes","?Empty the state:bool"],																															description: "Restore state of attributes {0} from global store",			],
		[ name: "setLocationMode",	display: "Set location mode",				parameters: ["Mode:mode"],																														location: true,	description: "Set location mode to '{0}'",		aggregated: true,	],
		[ name: "setAlarmSystemStatus",display: "Set Smart Home Monitor status",	parameters: ["Status:alarmSystemStatus"],																										location: true,	description: "Set SHM alarm to '{0}'",			aggregated: true,	],
		[ name: "sendNotification",	display: "Send notification",				parameters: ["Message:text"],																													location: true,	description: "Send notification '{0}' in notifications page",			aggregated: true,	],
		[ name: "sendPushNotification",display: "Send Push notification",			parameters: ["Message:text","Show in notifications page:bool"],																							location: true,	description: "Send Push notification '{0}'",		aggregated: true,	],
		[ name: "sendSMSNotification",display: "Send SMS notification",			parameters: ["Message:text","Phone number:phone","Show in notifications page:bool"],																		location: true, description: "Send SMS notification '{0}' to {1}",aggregated: true,	],
		[ name: "queueAskAlexaMessage",display: "Queue AskAlexa message",			parameters: ["Message:text", "?Unit:text", "?Application:text"],																		location: true, description: "Queue AskAlexa message '{0}' in unit {1}",aggregated: true,	],
		[ name: "deleteAskAlexaMessages",display: "Delete AskAlexa messages",			parameters: ["Unit:text", "?Application:text"],																	location: true, description: "Delete AskAlexa messages in unit {1}",aggregated: true,	],
		[ name: "executeRoutine",	display: "Execute routine",					parameters: ["Routine:routine"],																		location: true, 										description: "Execute routine '{0}'",				aggregated: true,	],
		[ name: "cancelPendingTasks",display: "Cancel pending tasks",			parameters: ["Scope:enum[Local,Global]"],																														description: "Cancel all pending {0} tasks",		],
		[ name: "followUp",				display: "Follow up with piston",			parameters: ["Delay:number[1..1440]","Unit:enum[seconds,minutes,hours]","Piston:piston","?Save state into variable:string"],	immediate: true,	varEntry: 3,	location: true,	description: "Follow up with piston '{2}' after {0} {1}",	aggregated: true],
		[ name: "executePiston",		display: "Execute piston",					parameters: ["Piston:piston","?Save state into variable:string"],																varEntry: 1,	location: true,	description: "Execute piston '{0}'",	aggregated: true],
		[ name: "pausePiston",			display: "Pause piston",					parameters: ["Piston:piston"],																location: true,	description: "Pause piston '{0}'",	aggregated: true],
		[ name: "resumePiston",			display: "Resume piston",					parameters: ["Piston:piston"],																location: true,	description: "Resume piston '{0}'",	aggregated: true],
		[ name: "httpRequest",			display: "Make a web request", parameters: ["URL:string","Method:enum[GET,POST,PUT,DELETE,HEAD]","Content Type:enum[JSON,FORM]","?Variables to send:variables","Import response data into variables:bool","?Variable import name prefix (optional):string"], location: true, description: "Make a {1} web request to {0}", aggregated: true],
		[ name: "wolRequest",			display: "Wake a LAN device", parameters: ["MAC address:string","?Secure code:string"], location: true, description: "Wake LAN device at address {0} with secure code {1}", aggregated: true],
		//flow control commands
		[ name: "beginSimpleForLoop",	display: "Begin FOR loop (simple)",			parameters: ["Number of cycles:string"],																																										location: true,		description: "FOR {0} CYCLES DO",			flow: true,					indent: 1,	],
		[ name: "beginForLoop",			display: "Begin FOR loop",					parameters: ["Variable to use:string","From value:string","To value:string"],																													varEntry: 0,	location: true,		description: "FOR {0} = {1} TO {2} DO",		flow: true,					indent: 1,	],
		[ name: "beginWhileLoop",		display: "Begin WHILE loop",				parameters: ["Variable to test:variable","Comparison:enum[is equal to,is not equal to,is less than,is less than or equal to,is greater than,is greater than or equal to]","Value:string"],						location: true,		description: "WHILE (|[{0}]| {1} {2}) DO",		flow: true,					indent: 1,	],
		[ name: "breakLoop",			display: "Break loop",						location: true,		description: "BREAK",						flow: true,			],
		[ name: "breakLoopIf",			display: "Break loop (conditional)",		parameters: ["Variable to test:variable","Comparison:enum[is equal to,is not equal to,is less than,is less than or equal to,is greater than,is greater than or equal to]","Value:string"],						location: true,		description: "BREAK IF ({0} {1} {2})",		flow: true,			],
		[ name: "exitAction",			display: "Exit Action",						location: true,		description: "EXIT",						flow: true,			],
		[ name: "endLoop",				display: "End loop",						parameters: ["Delay (seconds):number[0..*]"],																																									location: true,		description: "LOOP AFTER {0}s",				flow: true,	selfIndent: -1, indent: -1,	],
		[ name: "beginIfBlock",			display: "Begin IF block",					parameters: ["Variable to test:variable","Comparison:enum[is equal to,is not equal to,is less than,is less than or equal to,is greater than,is greater than or equal to]","Value:string"],						location: true,		description: "IF (|[{0}]| {1} {2}) THEN",		flow: true,					indent: 1,	],
		[ name: "beginElseIfBlock",		display: "Begin ELSE IF block",				parameters: ["Variable to test:variable","Comparison:enum[is equal to,is not equal to,is less than,is less than or equal to,is greater than,is greater than or equal to]","Value:string"],						location: true,		description: "ELSE IF (|[{0}]| {1} {2}) THEN",	flow: true,	selfIndent: -1,				],
		[ name: "beginElseBlock",		display: "Begin ELSE block",				location: true,		description: "ELSE",						flow: true,	selfIndent: -1,		 		],
		[ name: "endIfBlock",			display: "End IF block",					location: true,		description: "END IF",						flow: true,	selfIndent: -1, indent: -1,	],
		[ name: "beginSwitchBlock",		display: "Begin SWITCH block",				parameters: ["Variable to test:variable"],																																										location: true,		description: "SWITCH (|[{0}]|) DO",				flow: true,					indent: 2,	],
		[ name: "beginSwitchCase",		display: "Begin CASE block",				parameters: ["Value:string"],																																													location: true,		description: "CASE '{0}':",					flow: true,	selfIndent: -1,		],
		[ name: "endSwitchBlock",		display: "End SWITCH block",				location: true,		description: "END SWITCH",					flow: true,	selfIndent: -2,	indent: -2,	],
	]
	if (location.contactBookEnabled) {
		cmds.push([ name: "sendNotificationToContacts", display: "Send notification to contacts", parameters: ["Message:text","Contacts:contacts","Save notification:bool"], location: true, description: "Send notification '{0}' to {1}", aggregated: true])
	}
	if (getIftttKey()) {
		cmds.push([ name: "iftttMaker", display: "Send IFTTT Maker event", parameters: ["Event:text", "?Value1:string", "?Value2:string", "?Value3:string"], location: true, description: "Send IFTTT Maker event '{0}' with parameters '{1}', '{2}', and '{3}'", aggregated: true])
	}
	if (getLifxToken()) {
		cmds.push([ name: "lifxScene", display: "Activate LIFX scene", parameters: ["Scene:lifxScenes"], location: true, description: "Activate LIFX Scene '{0}'", aggregated: true])
	}
	return cmds
}

private attributes() {
	if (state.temp && state.temp.attributes) return state.temp.attributes
	def tempUnit = "°" + location.temperatureScale
	state.temp = state.temp ?: [:]
	state.temp.attributes = [
		[ name: "acceleration",				type: "enum",			options: ["active", "inactive"],	],
		[ name: "alarm",					type: "enum",			options: ["off", "strobe", "siren", "both"],	],
		[ name: "battery",					type: "number",			range: "0..100",		unit: "%",	],
		[ name: "beacon",					type: "enum",			options: ["present", "not present"],	],
		[ name: "button",					type: "enum",			options: ["held", "pushed"],	capability: "button",	momentary: true], //default capability so that we can figure out multi sub devices
		[ name: "carbonDioxide",			type: "decimal",		range: "0..*",	],
		[ name: "carbonMonoxide",			type: "enum",			options: ["clear", "detected", "tested"],	],
		[ name: "color",					type: "color",			unit: "#RRGGBB",	],
		[ name: "hue",						type: "number",			range: "0..360",		unit: "°",	],
		[ name: "saturation",				type: "number",			range: "0..100",		unit: "%",	],
		[ name: "hex",						type: "hexcolor",		],
		[ name: "saturation",				type: "number",			range: "0..100",		unit: "%",	],
		[ name: "level",					type: "number",			range: "0..100",		unit: "%",	],
		[ name: "switch",					type: "enum",			options: ["on", "off"],	interactive: true,	],
		[ name: "switch*",					type: "enum",			options: ["on", "off"],	interactive: true,	],
		[ name: "colorTemperature",			type: "number",			range: "1700..27000",	unit: "°K",	],
		[ name: "consumable",				type: "enum",			options: ["missing", "good", "replace", "maintenance_required", "order"],	],
		[ name: "contact",					type: "enum",			options: ["open", "closed"],	],
		[ name: "door",						type: "enum",			options: ["unknown", "closed", "open", "closing", "opening"],	interactive: true,	],
		[ name: "energy",					type: "decimal",		range: "0..*",			unit: "kWh",	],
		[ name: "energy*",					type: "decimal",		range: "0..*",			unit: "kWh",	],
		[ name: "indicatorStatus",			type: "enum",			options: ["when off", "when on", "never"],	],
		[ name: "illuminance",				type: "number",			range: "0..*",			unit: "lux",	],
		[ name: "image",					type: "image",			],
		[ name: "lock",						type: "enum",			options: ["locked", "unlocked"],	capability: "lock", interactive: true,	],
		[ name: "activities",				type: "string",			],
		[ name: "currentActivity",			type: "string",			],
		[ name: "motion",					type: "enum",			options: ["active", "inactive"],	],
		[ name: "status",					type: "string",			],
		[ name: "mute",						type: "enum",			options: ["muted", "unmuted"],	],
		[ name: "pH",						type: "decimal",		range: "0..14",	],
		[ name: "power",					type: "decimal",		range: "0..*",			unit: "W",	],
		[ name: "power*",					type: "decimal",		range: "0..*",			unit: "W",	],
		[ name: "occupancy",				type: "enum",			options: ["occupied", "not occupied"],	],
		[ name: "presence",					type: "enum",			options: ["present", "not present"],	],
		[ name: "humidity",					type: "number",			range: "0..100",		unit: "%",	],
		[ name: "shock",					type: "enum",			options: ["detected", "clear"],	],
		[ name: "lqi",						type: "number",			range: "0..255",	],
		[ name: "rssi",						type: "number",			range: "0..100",		unit: "%",	],
		[ name: "sleeping",					type: "enum",			options: ["sleeping", "not sleeping"],	],
		[ name: "smoke",					type: "enum",			options: ["clear", "detected", "tested"],	],
		[ name: "sound",					type: "enum",			options: ["detected", "not detected"], ],
		[ name: "steps",					type: "number",			range: "0..*",	],
		[ name: "goal",						type: "number",			range: "0..*",	],
		[ name: "soundPressureLevel",		type: "number",			range: "0..*",	],
		[ name: "tamper",					type: "enum",			options: ["clear", "detected"],	],
		[ name: "temperature",				type: "decimal",		range: "*..*",			unit: tempUnit,		],
		[ name: "thermostatMode",			type: "enum",			options: ["off", "auto", "cool", "heat", "emergency heat"],	],
		[ name: "thermostatFanMode",		type: "enum",			options: ["auto", "on", "circulate"],	],
		[ name: "thermostatOperatingState",	type: "enum",			options: ["idle", "pending cool", "cooling", "pending heat", "heating", "fan only", "vent economizer"],		],
		[ name: "coolingSetpoint",			type: "decimal",		range: "-127..127",		unit: tempUnit,	],
		[ name: "heatingSetpoint",			type: "decimal",		range: "-127..127",		unit: tempUnit,	],
		[ name: "thermostatSetpoint",		type: "decimal",		range: "-127..127",		unit: tempUnit,	],
		[ name: "sessionStatus",			type: "enum",			options: ["paused", "stopped", "running", "canceled"],	],
		[ name: "threeAxis",				type: "vector3",		],
		[ name: "orientation",				type: "orientation",	options: threeAxisOrientations(),	valueType: "enum",	subscribe: "threeAxis",	],
		[ name: "axisX",					type: "number",			range: "-1024..1024",	subscribe: "threeAxis",		],
		[ name: "axisY",					type: "number",			range: "-1024..1024",	subscribe: "threeAxis",		],
		[ name: "axisZ",					type: "number",			range: "-1024..1024",	subscribe: "threeAxis",		],
		[ name: "touch",					type: "enum",			options: ["touched"],		],
		[ name: "valve",					type: "enum",			options: ["open", "closed"],					],
		[ name: "voltage",					type: "decimal",		range: "*..*",			unit: "V",	],
		[ name: "water",					type: "enum",			options: ["dry", "wet"],	],
		[ name: "windowShade",				type: "enum",			options: ["unknown", "open", "closed", "opening", "closing", "partially open"],	],
		[ name: "mode",						type: "mode",			options: state.run == "config" ? getLocationModeOptions() : [],	],
		[ name: "alarmSystemStatus",		type: "enum",			options: state.run == "config" ? getAlarmSystemStatusOptions() : [],	],
		[ name: "routineExecuted",			type: "routine",		options: state.run == "config" ? location.helloHome?.getPhrases()*.label : [],	valueType: "enum",	],
		[ name: "piston",					type: "piston",			options: state.run == "config" ? parent.listPistons(state.config.expertMode ? null : app.label) : [],	valueType: "enum",	],
		[ name: "variable",					type: "enum",			options: state.run == "config" ? listVariables(true) : [],	valueType: "enum",	],
		[ name: "time",						type: "time",	],
		[ name: "askAlexaMacro",			type: "askAlexaMacro",	options: state.run == "config" ? listAskAlexaMacros() : [], valueType: "enum"],
		[ name: "echoSistantProfile",		type: "echoSistantProfile",	options: state.run == "config" ? listEchoSistantProfiles() : [], valueType: "enum"],
		[ name: "ifttt",					type: "ifttt",			valueType: "string"],
	]
	return state.temp.attributes
}

private comparisons() {
	def optionsEnum = [
		[ condition: "is", trigger: "changes to", parameters: 1, timed: false],
		[ condition: "is not", trigger: "changes away from", parameters: 1, timed: false],
		[ condition: "is one of", trigger: "changes to one of", parameters: 1, timed: false, multiple: true, minOptions: 2],
		[ condition: "is not one of", trigger: "changes away from one of", parameters: 1, timed: false, multiple: true, minOptions: 2],
		[ condition: "was", trigger: "stays", parameters: 1, timed: true],
		[ condition: "was not", trigger: "stays away from", parameters: 1, timed: true],
		[ trigger: "changes", parameters: 0, timed: false],
		[ condition: "changed", parameters: 0, timed: true],
		[ condition: "did not change", parameters: 0, timed: true],
	]

	def optionsMomentary = [
		[ condition: "is", trigger: "changes to", parameters: 1, timed: false],
	]

	def optionsBool = [
		[ condition: "is equal to", parameters: 1, timed: false],
		[ condition: "is not equal to", parameters: 1, timed: false],
		[ condition: "is true", parameters: 0, timed: false],
		[ condition: "is false", parameters: 0, timed: false],
	]
	def optionsEvents = [
		[ trigger: "executed", parameters: 1, timed: false],
	]
	def optionsNumber = [
		[ condition: "is equal to", trigger: "changes to", parameters: 1, timed: false],
		[ condition: "is not equal to", trigger: "changes away from", parameters: 1, timed: false],
		[ condition: "is less than", trigger: "drops below", parameters: 1, timed: false],
		[ condition: "is less than or equal to", trigger: "drops to or below", parameters: 1, timed: false],
		[ condition: "is greater than", trigger: "raises above", parameters: 1, timed: false],
		[ condition: "is greater than or equal to", trigger: "raises to or above", parameters: 1, timed: false],
		[ condition: "is inside range", trigger: "enters range", parameters: 2, timed: false],
		[ condition: "is outside of range", trigger: "exits range", parameters: 2, timed: false],
		[ condition: "is even", trigger: "changes to an even value", parameters: 0, timed: false],
		[ condition: "is odd", trigger: "changes to an odd value", parameters: 0, timed: false],
		[ condition: "was equal to", trigger: "stays equal to", parameters: 1, timed: true],
		[ condition: "was not equal to", trigger: "stays not equal to", parameters: 1, timed: true],
		[ condition: "was less than", trigger: "stays less than", parameters: 1, timed: true],
		[ condition: "was less than or equal to", trigger: "stays less than or equal to", parameters: 1, timed: true],
		[ condition: "was greater than", trigger: "stays greater than", parameters: 1, timed: true],
		[ condition: "was greater than or equal to", trigger: "stays greater than or equal to", parameters: 1, timed: true],
		[ condition: "was inside range",trigger: "stays inside range",  parameters: 2, timed: true],
		[ condition: "was outside of range", trigger: "stays outside of range", parameters: 2, timed: true],
		[ condition: "was even", trigger: "stays even", parameters: 0, timed: true],
		[ condition: "was odd", trigger: "stays odd", parameters: 0, timed: true],
		[ trigger: "changes", parameters: 0, timed: false],
		[ trigger: "raises", parameters: 0, timed: false],
		[ trigger: "drops", parameters: 0, timed: false],
		[ condition: "changed", parameters: 0, timed: true],
		[ condition: "did not change", parameters: 0, timed: true],
	]
	def optionsTime = [
		[ trigger: "happens at", parameters: 1],
		[ condition: "is any time of day", parameters: 0],
		[ condition: "is around", parameters: 1],
		[ condition: "is before", parameters: 1],
		[ condition: "is after", parameters: 1],
		[ condition: "is between", parameters: 2],
		[ condition: "is not between", parameters: 2],
	]
	return [
		[ type: "bool",					options: optionsBool,		],
		[ type: "boolean",				options: optionsBool,		],
		[ type: "vector3",				options: optionsEnum,		],
		[ type: "orientation",			options: optionsEnum,		],
		[ type: "string",				options: optionsEnum,		],
		[ type: "text",					options: optionsEnum,		],
		[ type: "enum",					options: optionsEnum,		],
		[ type: "mode",					options: optionsEnum,		],
		[ type: "alarmSystemStatus",	options: optionsEnum,		],
		[ type: "routine",				options: optionsEvents		],
		[ type: "piston",				options: optionsEvents		],
		[ type: "askAlexaMacro",		options: optionsEvents		],
		[ type: "echoSistantProfile",	options: optionsEvents		],
		[ type: "ifttt",				options: optionsEvents		],
		[ type: "number",				options: optionsNumber,		],
		[ type: "variable",				options: optionsNumber,		],
		[ type: "decimal",				options: optionsNumber		],
		[ type: "time",					options: optionsTime,		],
		[ type: "momentary",			options: optionsMomentary,	],
	]
}

private getLocationModeOptions() {
	def result = []
	for (mode in location.modes) {
		if (mode) result.push("$mode")
	}
	return result
}
private getAlarmSystemStatusOptions() {
	return ["Disarmed", "Armed/Stay", "Armed/Away"]
}

private initialSystemStore() {
	return [
		"\$currentEventAttribute": null,
		"\$currentEventDate": null,
		"\$currentEventDelay": 0,
		"\$currentEventDevice": null,
		"\$currentEventDeviceIndex": 0,
		"\$currentEventDevicePhysical": false,
		"\$currentEventReceived": null,
		"\$currentEventValue": null,
		"\$currentState": null,
		"\$currentStateDuration": 0,
		"\$currentStateSince": null,
		"\$currentStateSince": null,
		"\$nextScheduledTime": null,
		"\$now": 999999999999,
		"\$hour": 0,
		"\$hour24": 0,
		"\$minute": 0,
		"\$second": 0,
		"\$meridian": "",
		"\$meridianWithDots": "",
		"\$day": 0,
		"\$dayOfWeek": 0,
		"\$dayOfWeekName": "",
		"\$month": 0,
		"\$monthName": "",
		"\$index": 0,
		"\$year": 0,
		"\$meridianWithDots": "",
		"\$previousEventAttribute": null,
		"\$previousEventDate": null,
		"\$previousEventDelay": 0,
		"\$previousEventDevice": null,
		"\$previousEventDeviceIndex": 0,
		"\$previousEventDevicePhysical": 0,
		"\$previousEventExecutionTime": 0,
		"\$previousEventReceived": null,
		"\$previousEventValue": null,
		"\$previousState": null,
		"\$previousStateDuration": 0,
		"\$previousStateSince": null,
		"\$random": 0,
		"\$randomColor": "#FFFFFF",
		"\$randomColorName": "White",
		"\$randomLevel": 0,
		"\$randomSaturation": 0,
		"\$randomHue": 0,
		"\$midnight": 999999999999,
		"\$noon": 999999999999,
		"\$sunrise": 999999999999,
		"\$sunset": 999999999999,
		"\$nextMidnight": 999999999999,
		"\$nextNoon": 999999999999,
		"\$nextSunrise": 999999999999,
		"\$nextSunset": 999999999999,
		"\$time": "",
		"\$time24": "",
		"\$httpStatusCode": 0,
		"\$httpStatusOk": true,
		"\$iftttStatusCode": 0,
		"\$iftttStatusOk": true,
		"\$locationMode": "",
		"\$shmStatus": ""
	]
}

private List<String> colorOptions() {
	return allColors()*.name
}

private List<Map> allColors() {
	return [randomColor(), *colorUtil.ALL]
}

private Map randomColor() {
	[name: "Random", rgb: "#000000", h: 0, s: 0, l: 0]
}

private getColorByName(name, ownerId = null, taskId = null) {
	if (name == "Random") {
		//randomize the color
		String valName = "$ownerId-$taskId"
		def result = getRandomValue(valName) ?: colorUtil.RANDOM
		setRandomValue(valName, result)
		return result
	}
	return colorUtil.findByName(name) ?: colorUtil.WHITE
}

/******************************************************************************/
/*** DEVELOPMENT AREA														***/
/*** Write code here and then move it to its proper location				***/
/******************************************************************************/