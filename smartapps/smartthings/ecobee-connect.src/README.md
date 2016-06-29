Ecobee Thermostat SmartApp(s) and related Device Types for use with the SmartThings Hub
======================================================

## <a name="top">Table Of Contents</a>
- [Introduction](#intro)
- [Motivation](#motivation)
- [Quick Links](#quicklinks)
- [Features](#features)
	- [Thermostat and Sensor Device User Interfaces](#features-therm-ui)
	- [Ecobee (Connect) SmartApp](#features-connect-sa)
    - [ecobee Routines SmartApp](#features-routines-sa)
    - [ecobee Open Contacts SmartApp](#features-opencontact-sa)
- [Installation](#installation)
  - [Install Device Handlers](#install-device-types)
  - [Install SmartApp in IDE](#install-smartapp)
  - [Install SmartApp on Device](#install-smartapp-phone)
- [Updating](#updating)
- [Troubleshooting](#troubleshooting)
- [Reporting Issues](#reporting-issues)
- [Open Items](#open-items--to-dos)
- [Contributors](#contributors)
- [License](#license)
- [Appendix - SmartThings Capabilities Supported](#smartthings-capabilities-supported)

## <a name="intro">Introduction</a>
This document describes the various features related to the Open Source Ecobee (Connect)  SmartApp and the related compoenents. This SmartApp suite and the related Device Handlers are intended to be used with [Ecobee thermostats](http://www.ecobee.com/) with the [SmartThings](https://www.smartthings.com/) platform. 

The following components are part of the solution:
- **Ecobee (Connect) SmartApp**: This SmartApp provides a single interface for Ecobee Authorization, Device Setup (both Thermostats **and** Sensors), Behavioral Settings and even a Debug Dashboard. Additional features can be added over time as well thanks to built in support for Child SmartApps, keeping everything nicely integrated into one app.
- **ecobee Routines Child SmartApp**: Child app that lets you trigger settings changes on your Ecobee thermostats based on the SmartThings Hello Modes. Settings include the Ecobee Program (Comfort Settings), Fan Modes and Hold Types. In additional to SmartThings Hello Modes, sunrise/sunset triggers are also support. Multiple instances of the SmartApp are also supported for maximum flexibility.
- **Ecobee Thermostat Device Handler**: This implements the Device Handler for the Ecobee Thermostat functions and attributes.
- **Ecobee Sensor Device Handler**: This implements the Device Handler for the Ecobee Sensor attributes. This is also used to expose the internal sensors on the Thermostat to allow the actual temperature values (instead of only the average) to also be available. This is critically important for some applications such as smart vents.

Here are links to the working version of the repository being developed and maintained by Sean Schneyer [(on GitHub)](https://github.com/StrykerSKS) [(on SmartThings Community)](https://community.smartthings.com/users/strykersks/).

## <a name="motivation">Motivation</a>

The intent is to provide an Open Source Licensed ecobee-to-SmartThings implementation that can be used by the SmartThings community of users free of charge and without fear of the device disappearing in the future. This will help ensure accessibility for all users and provide for an easy mechanism for the community to help maintain/drive the functionality.

The ultimate goal would be to have these capabilities become part of the stock drivers on the "master" branch. But until that time they will be maintained as a fork providing good visibility to any changes upstream.

>NOTE: I've tried submitting several enhancements back upstream but they have simply sat in their queue and have been ignored, even after being reviewed by the ST engineers. Until SmartThings has a better process for accepting fixes/enhancements I will simply focus on continuing to improve the codebase. Once they are ready then I can start to backport fixes.

## <a name="quicklinks">Quick Links</a>
- README.md (this file): <https://github.com/StrykerSKS/SmartThingsPublic/blob/StrykerSKS-Ecobee3/smartapps/smartthings/ecobee-connect.src/README.md>
- Ecobee (Connect) SmartApp: <https://github.com/StrykerSKS/SmartThingsPublic/tree/StrykerSKS-Ecobee3/smartapps/smartthings/ecobee-connect.src>
- ecobee Routines Child SmartApp: <https://github.com/StrykerSKS/SmartThingsPublic/tree/StrykerSKS-Ecobee3/smartapps/smartthings/ecobee-routines.src>
- ecobee Open Contacts Child SmartApp: <https://github.com/StrykerSKS/SmartThingsPublic/tree/StrykerSKS-Ecobee3/smartapps/smartthings/ecobee-open-contacts.src>
- Ecobee Thermostat Device: <https://github.com/StrykerSKS/SmartThingsPublic/tree/StrykerSKS-Ecobee3/devicetypes/smartthings/ecobee-thermostat.src>
- Ecobee Sensor Device: <https://github.com/StrykerSKS/SmartThingsPublic/tree/StrykerSKS-Ecobee3/devicetypes/smartthings/ecobee-sensor.src>
- SmartThings IDE: <https://graph.api.smartthings.com>


-----------------------------
# <a name="features">Features</a>
## General
This collection of SmartApps and Device Handlers has been designed for simple installation, flexibile configuration options and easy operation. It is also extensible through the use of Child SmartApps that can easily be added to the configuration. **And it fully implements the related [SmartThings Capabilities](http://docs.smartthings.com/en/latest/capabilities-reference.html).**

Key Highlights include:
- **Open Source Implementation!** Free as in beer AND speech. No donations or purchase needed to use.
- Single installation SmartApp, `Ecobee (Connect)` used for installing both Thermostats **and** Sensors. No need for multiple apps just for installation! In fact, the `Ecobee (Connect)` SmartApp is the only SmartApp interface you'll need to access all available functions, including those provided by Child SmartApps (if installed).
- Sophisticated User Interface: Uses custom Ecobee icons throughout the design to provide a more polished look and feel.
- Display of current weather with support for separate day and night icons (just like on the front of your Ecobee thermostat)!
- Robust watchdog handling to minimize API Connectivity issues, but also includes an API Status Tile to quickly identify if there is an ongoing problem. No more guessing if you are still connected or not.
- Included Child SmartApp (`ecobee Routines`) for automating settings changes based on SmartThings Hello Modes being activated (such as through a Routine)
- Full support for both Fahrenheit and Celsius

## <a name="features-therm-ui">Thermostat and Sensor Device User Interfaces</a>
The primary user interface on a day-to-day basis will be two different types of Device Handlers that are shown on the `Things` list under `My Home` in the mobile app. Screenshots of both the `Ecobee Thermostat` and the `Ecobee Sensor` are shown below. 

`Ecobee Thermostat` Device |  `Ecobee Thermostat` Device w/ Annotation
:-------------------------:|:-------------------------:
<img src="https://raw.githubusercontent.com/StrykerSKS/SmartThings/master/smartapp-icons/ecobee/documentation/current_thermo_screenshot.jpg" border="1" width="250" /> |  <img src="https://raw.githubusercontent.com/StrykerSKS/SmartThings/master/smartapp-icons/ecobee/documentation/thermostat_annotation_current.png" width="775" />

`Ecobee Sensor` Device |  `Ecobee Sensor` Device w/ Annotation
:-------------------------:|:-------------------------:
<img src="https://raw.githubusercontent.com/StrykerSKS/SmartThings/master/smartapp-icons/ecobee/documentation/current_sensor_screenshot.png" border="1" width="250" /> |  <img src="https://raw.githubusercontent.com/StrykerSKS/SmartThings/master/smartapp-icons/ecobee/documentation/sensor_annotation_current.png" width="400" />

Rich set of Ecobee related icons:
<img src="https://raw.githubusercontent.com/StrykerSKS/SmartThings/master/smartapp-icons/ecobee/documentation/ST_ecobee_icons_thumbs.png" border="1" />

## <a name="features-connect-sa">`Ecobee (Connect)` SmartApp</a>
The `Ecobee (Connect)` SmartApp provides a single SmartApp interface for accessing installation, updates, Child SmartApps and even debugging tools. The interface uses dynamic pages to guide the user through the installation process in order simplify the steps as much as possible.

The SmartApp provides the following capabilities:
- Perform Ecobee API Authorization (OAuth)
- Select Thermostats from account to use (dynamic list, so any future Thermostats can easily be added at a later date)
- Select Sensors to use (dynamic list, will only show sensors associated with the previously selected Thermostats)
- Access Child SmartApps (such as `ecobee Routines`)
- Set various Preferences:
  - Set default Hold Type ("Until Next Program" or "Until I Change")
  - Allow changes to temperature setpoint via arrows when in auto mode ("Smart Auto Temperature Adjust")
  - Polling Interval
  - Debugging Level
  - Include Thermostats as a separate Ecobee Sensor (useful in order to expose the true temperature reading and not just the average temperature shown on the thermostat, e.g. for Smart Vent input)
  - Monitor external devices to drive additional polling and watchdog events
  - Delay timer value after pressing setpoint arrows (to allow multiple arror presses before calling the Ecobee APIs)
- Select Polling and Watchdog Devices (if enabled in Preferences)
- Debug Dashboard (if Debug Level is set to 5)



## <a name="features-routines-sa">`ecobee Routines` SmartApp</a>
The `ecobee Routines` SmartApp provides the ability to change the running Program (Comfort Setting) when a SmartThings Mode is changed (for example, by running a Routine) or a Routine is run. 

Features include:
- Change one or multiple thermostats
- Trigger based on Mode Change or Routine Execution
- Choose any (including custom) Ecobee Programs to switch to. Or can even choose to Resume Program instead
- Change the Fan Mode (Optional)
- Set the Fan Minimum Runtime (Optional)
- Also execute at Sunrise or Sunset (Optional)
- Temporarily Disable app without having to delete and recreate!

## <a name="features-opencontact-sa">`Open Contacts` SmartApp</a>
The `Open Contacts` SmartApp can detect when one (or more) contact sensors (such as doors and windows) are left open for a configurable amount of time and can automatically turn off the HVAC and/or send a notification when it is detected.

Features include:
- Change one or multiple thermostats
- Trigger based on one or multiple contact sensors
- Configurable delay timers (for trigger and reset)
- Configurable actions: Notify Only, HVAC Only or Both
- Support for Contact Book or simply SMS for notifications
- Temporarily Disable app without having to delete and recreate!


-----------------------------
# <a name="installation">Installation</a>

## General
> **NOTE**: While I have tested this on my system and believe it to be working (and we have over 200 user now). I have not run a full battery of tests that can include all possible corner cases or configurations. It is possible, perhaps even _likely_, that there are still bugs or platform specific issues in this code. If you do run into an issue, the best option is to report it using the [Issues](https://github.com/StrykerSKS/SmartThingsPublic/issues) tab within the GitHub repository. I will do my best to quickly address any issues that are found. 

It is highly recommended that you use the GitHub Integration that SmartThings offers with their IDE. This will make it **much** easier to keep up to date with changes over time. For the general steps needed for setting up GitHub IDE integration, please visit <http://docs.smartthings.com/en/latest/tools-and-ide/github-integration.html> and follow the steps for performing the setup.

## Install Preparation
The first step is to ensure that you delete any existing Ecobee related devices and SmartApps that you may have from other sources. They are likely not compatible with this codebase and are almost certain to cause problems down the road. 

If you are not familiar with adding your own custom devices, then be sure to familiarize yourself with the [SmartThings IDE](https://graph.api.smartthings.com/) before you begin the installation process.

You will also need to make sure that you remember your Ecobee username and password. You should login to <http://www.ecobee.com/> now to ensure you have your credentials.

## <a name="install-device-type">Install Device Handlers</a>
Here we will install two (2) different Device Handlers:
- `Ecobee Thermostat`
- `Ecobee Sensor`

Follow the steps for _either_ the GitHub Integration or the Manual method below. Do **not** try to do both methods.

### Install Using GitHub Integration (Recommended Method)
Follow these steps (all within the SmartThings IDE):
- Click on the `My Device Handlers` tab
- Click `Settings`
- Click `Add new repository` and use the following parameters:
  - Owner: `StrykerSKS`
  - Name: `SmartThingsPublic`
  - Branch: `StrykerSKS-Ecobee3`
- Click `Save`
- Click `Update from Repo` and select the repository we just added above
- Find and Select `ecobee-sensor.groovy` and `ecobee-thermostat.groovy`
- Select `Publish`(bottom right of screen near the `Cancel` button)
- Click `Execute Update`
- Note the response at the top. It should be something like "`Updated 0 devices and created 2 new devices, 2 published`"
- Verify that the two devices show up in the list and are marked with Status `Published` (NOTE: You may have to reload the `My Device Handlers` screen for the devices to show up properly.)


### Install Manually from Code
For this method you will need to have one browser window open on GitHub and another on the IDE.

Follow these steps to install the `Ecobee Sensor`:
- [IDE] Click on the `My Device Handlers` tab
- [IDE] Click `New Device Type` (top right corner)
- [IDE] Click `From Code`
- [GitHub] Go to the respository for the Ecobee Sensor: <https://github.com/StrykerSKS/SmartThingsPublic/blob/StrykerSKS-Ecobee3/devicetypes/smartthings/ecobee-sensor.src/ecobee-sensor.groovy>
- [GitHub] Click `Raw`
- [GitHub] Select all of the text in the window (use Ctrl-A if using Windows)
- [GitHub] Copy all of the selected text to the Clipboard (use Ctrl-C if using Windows)
- [IDE] Click inside the text box
- [IDE] Paste all of the previously copied text (use Ctrl-V if using Windows)
- [IDE] Click `Create`
- [IDE] Click `Save`
- [IDE] Click `Publish` --> `For Me`

Follow these steps to install the `Ecobee Thermostat`:
- [IDE] Click on the `My Device Handlers` tab
- [IDE] Click `New Device Type` (top right corner)
- [IDE] Click `From Code`
- [GitHub] Go to the respository for the Ecobee Thermostat: <https://github.com/StrykerSKS/SmartThingsPublic/blob/StrykerSKS-Ecobee3/devicetypes/smartthings/ecobee-thermostat.src/ecobee-thermostat.groovy>
- [GitHub] Click `Raw`
- [GitHub] Select all of the text in the window (use Ctrl-A if using Windows)
- [GitHub] Copy all of the selected text to the Clipboard (use Ctrl-C if using Windows)
- [IDE] Click inside the text box
- [IDE] Paste all of the previously copied text (use Ctrl-V if using Windows)
- [IDE] Click `Create`
- [IDE] Click `Save`
- [IDE] Click `Publish` --> `For Me`


## <a name="install-smartapp">Install SmartApps in IDE</a>
Here we will install the following SmartApps:
- `Ecobee (Connect)`
- `ecobee Routines` (Child SmartApp)

Follow the steps for _either_ the GitHub Integration or the Manual method below. Do **not** try to do both methods.

### Install Using GitHub Integration
Follow these steps to install the `Ecobee (Connect)` SmartApp (all within the SmartThings IDE):
- Click on the `My SmartApps` tab
- Click `Settings`
- Click `Add new repository` and use the following parameters:
  - Owner: `StrykerSKS`
  - Name: `SmartThingsPublic`
  - Branch: `StrykerSKS-Ecobee3`
- Click `Save`
- Click `Update from Repo` and select the repository we just added above
- Find and Select `ecobee-connect.groovy` and `ecobee-routines.groovy`
- Select `Publish`(bottom right of screen near the `Cancel` button)
- Click `Execute Update`
- Note the response at the top. It should be something like "`Updated 0 and created 2 SmartApps, 2 published`"
- Verify that the SmartApps shows up in the list and is marked with Status `Published`
- Locate the `Ecobee (Connect)` SmartApp from the list and Click on the `Edit Properties` button to the left of the SmartApp that we just added (looks like pencil on a paper)
- Click on the `OAuth` tab (**NOTE: This is a commonly missed set of steps, but failing to enable OAuth will generate cryptic errors later when you try to use the SmartApp. So please don't skip these steps.**)
- Click `Enable OAuth in Smart App`
- Click `Update` (bottom left of screen)
- Verify that `Updated SmartApp` appears at the top of the screen

### Install Manually from Code
For this method you will need to have one browser window open on GitHub and another on the IDE.

Follow these steps to install the `Ecobee (Connect)` SmartApps:
- [IDE] Click on the `My SmartApps` tab
- [IDE] Click `New SmartApp` (top right corner)
- [IDE] Click `From Code`
- [GitHub] Go to the respository for the `Ecobee (Connect)` SmartApp: <https://github.com/StrykerSKS/SmartThingsPublic/blob/StrykerSKS-Ecobee3/smartapps/smartthings/ecobee-connect.src/ecobee-connect.groovy>
- [GitHub] Click `Raw`
- [GitHub] Select all of the text in the window (use Ctrl-A if using Windows)
- [GitHub] Copy all of the selected text to the Clipboard (use Ctrl-C if using Windows)
- [IDE] Click inside the text box
- [IDE] Paste all of the previously copied text (use Ctrl-V if using Windows)
- [IDE] Click `Create`
- [IDE] Click `Save`
- [IDE] Click `Publish` --> `For Me`
- [IDE] Click on the `My SmartApps` tab
- [IDE] Verify that the SmartApp shows up in the list and is marked with Status `Published`
- [IDE] Click on the `Edit Properties` button to the left of the SmartApp that we just added (looks like pencil on a paper)
- [IDE] Click on the `OAuth` tab
- [IDE] Click `Enable OAuth in Smart App`
- [IDE] Click `Update` (bottom left of screen)
- [IDE] Verify that `Updated SmartApp` appears at the top of the screen

Follow these steps to install the `ecobee Routines` SmartApps:
- [IDE] Click on the `My SmartApps` tab
- [IDE] Click `New SmartApp` (top right corner)
- [IDE] Click `From Code`
- [GitHub] Go to the respository for the `Ecobee (Connect)` SmartApp: <https://github.com/StrykerSKS/SmartThingsPublic/blob/StrykerSKS-Ecobee3/smartapps/smartthings/ecobee-routines.src/ecobee-routines.groovy>
- [GitHub] Click `Raw`
- [GitHub] Select all of the text in the window (use Ctrl-A if using Windows)
- [GitHub] Copy all of the selected text to the Clipboard (use Ctrl-C if using Windows)
- [IDE] Click inside the text box
- [IDE] Paste all of the previously copied text (use Ctrl-V if using Windows)
- [IDE] Click `Create`
- [IDE] Click `Save`
- [IDE] Click `Publish` --> `For Me` (Optional)
- [IDE] Click on the `My SmartApps` tab
- [IDE] Verify that the SmartApp shows up in the list



## <a name="install-smartapp-phone">Install and Run `Ecobee (Connect) `SmartApp on Phone/Tablet</a>
> **NOTE**: I have only tested this on an iPhone 6 as I do not have access to Android device. Feedback and bug reports are welcome if any issues are found on any platform. There are already some known issues with platforms behaving differently due to differences in the SmartThings apps on those platforms.

The SmartApp will guide you through the basic installation and setup process. It includes the following aspects:
- Authentication with Ecobee to allow API Calls for your thermostat(s) (and connected sensors)
- Discover and selection of Thermostats
- Discover and selection of Remote Sensors (if there are any)
- Setup of option features/parameters such as Smart Auto Temp Control, Polling Intervals, etc

Follow these steps for the SmartApp on your mobile device:
- Open the SmartThings app
- Open the `Marketplace`
- Click the `SmartApps` tab
- Select `My Apps` (all the way at the bottom of the list)
- Click `Ecobee (Connect)` (NOTE: If the app simply returns back to the `My Apps` screen try clicking again. If this still does not work after several tries, please verify all install steps from above are completed.)
- Click (as indicated on the screen) to enter your Ecobee Credentials
- Enter your Ecobee Email and Password
- Click `Accept`
- You should receive a message indicating `Your ecobee Account is now connected to SmartThings!`
- Click `Done` (or `Next` depending on your device OS)
- Click `Done` (or `Next` depending on your device OS) again to save the credentials and prepare for the next steps.
- You should receive a small green popup at the top stating "`Ecobee (Connect) is now installed and automating`"
- Go to the `My Home` screen and slect the `SmartApps` tab
- Click on the `Ecobee (Connect)` SmartApp
- Work through the various option screens to select thermostats and sensors. (NOTE: The options are dynamic and will change/appear based on other selections such as selecting a thermostat will reveal the sensors option screen.)
- You can also go into the `Preferences` section to set various preferences such as `Hold Type`, `Smart Auto Temperature`, `Polling Interval`, `Debug Level`, and wether to create separate sensor objects for thermostats.
- After making all selections, Click `Done` to save your preferences and exit the SmartApp

At this point, the SmartApp will automatically create all of the new devices, one for each thermostat and sensor. These will show up in your regular `Things` list within the app. 

> **NOTE 1**: It may take a few minutes for the new devices to show up in the list. You should try refreshing the list if they are not there (pull down on the list). In extreme cases, you may have to restart the SmartThings app on your phone to update the list. You should only have to do this once.

<br/>

> **NOTE 2**: If you uninstall the SmartApp it will automatically remove all of the thermostats and sensors that it previously installed. This is necessary (and expected) as those devices are "children" of the SmartApp.

<br/>

> There is currently a lot of debug information that can be generated from the app (which is configurable). If you need to do any kind of troubleshooting, you can see the current information flowing through in the `Live Logging` tab of the SmartThings IDE. You will also need this information if you open an `Issue` since it will be needed to track down what is going on. ** Please ensure that you do not include any personal information from the logs in an `Issue` report. **

-------------------------
## Updating
If you have enabled GitHub integration with the SmartThings IDE, then updates are a breeze. Otherwise the steps are a bit more manual but not too complicated.

### Updating with GitHub Integration
The IDE provides visual cues to alert you that any device types or SmartApps have been updated in their upstream repositories. See the [GitHub/IDE integration guide](http://docs.smartthings.com/en/latest/tools-and-ide/github-integration.html) for more details on the different colors.

Once you have determined that an update is available, follow these steps:
- Login to the SmartThings IDE
- Go to either the `My Device Handlers` or `My SmartApps` tabs to see if there are updates (the color of the item will be purple)
- Click the `Update from Repo` button (top right)
- Select the repository and branch you want to update `SmartThingsPublic (StrykerSKS-Ecobee3)`
- The item should show up in the `Obsolete (updated in GitHub)` column and automatically be selected
- Select `Publish` (bottom right)
- Click `Execute Update` (bottom right)
- You should receive a confirmation message such as this example: `Updated 1 and created 0 SmartApps, 1 published`
- (Optional, but recommended) Rerun the `Ecobee (Connect)` SmartApp. This seems to eleviate any residual issues that may occur due to the update

You should now be running on the updated code. Be sure that you check for both updates of the SmartApp **and** the Device Type. Updating one but not the other could cause compatibility problems.

### Updating manually (without GitHub Integration)

To update manually, you will need to "cut & paste" the raw code from GitHub into the SmartThings IDE, Save and Publish the code. I will leave it to the reader to work through the full individual steps, but the links to the code are the same as those that were used during the initial install process.

-------------------------
## Troubleshooting

| Symptom 	| Possible Solution 	|
|---------	|-------------------	|
| The devices are not showing up in the Things tab after installation    	|  It can take several minutes for things to show up properly. If you don't want to wait then simply kill the SmartThings app and reload it.              	|
| Receive error similar to "error java.lang.NullPointerException: Cannot get property 'authorities' on null object"        	| This indicates that you have not turned on OAuth for the SmartApp. Please review the installation instructions and complete the OAuth steps.                  	|
| "You are not authorized to perform the requested operation."        	|  This indicates that you have not turned on OAuth for the SmartApp. Please review the installation instructions and complete the OAuth steps.                 	|
| Irregular behavior after an update to the SmartApp or Device Handler code| It is possible that after updating the codebase that you may experience strange behavior, including possible app crashes. This seems to be a general issue with updates on SmartThings. Try the following steps: <br/> 1) Re-run the `Ecobee (Connect)` SmartApp to re-initialize the devices <br/> 2) If that does not solve the problem, remove and re-install the SmartApp |
|         	|                   	|
|         	|                   	|

"You are not authorized to perform the requested operation."

### Debug Level in SmartApp
The `Ecobee (Connect)` SmartApp allows the end user to config the Debug Level they wish to use (ranging from 1-5). The higher the level the more debug information is fed into the `Live Logging` on the SmartThings IDE.

Also, if a user chooses Debug Level 5, then a new `Debug Dashboard` will appear within the SmartApp. This dashboard gives direct access to various state information of the app as well as a few helper functions that can be used to manaually trigger actions that are normally timer based.

### Live Logging on IDE
The `Live Logging` feature on the SmartThings IDE is an essential tool in the debugging process of any issues that may be encountered.

To access the `Live Logging` feature, follow these steps:
- Go the SmartThings IDE (<https://graph.api.smartthings.com/>) and log in
- click `Live Logging`

### Installed SmartApps Info on IDE
The SmartThings IDE also provides helpful insights related to the current state of any SmartApp running on the system. To access this information, follow the follwing steps:
- Go the SmartThings IDE (<https://graph.api.smartthings.com/>) and log in
- Click `My Locations` (select your location if you have more than one)
- Scroll down and click `List SmartApps`
- Find the `Ecobee (Connect)` SmartApp and click the link


-------------------------
## <a name="reporting-issues">Reporting Issues</a>
All issues or feature requests should be submitted via the GitHub issue capability. It can be found on the [Issues](https://github.com/StrykerSKS/SmartThingsPublic/issues) tab within the GitHub repository.

You are also welcome to engage in discussions using the [SmartThings Community](https://community.smartthings.com/).

## <a name="open-items">Open Items / To Dos</a>

Please visit the GitHub page to see any open [Issues](https://github.com/StrykerSKS/SmartThingsPublic/issues).


## <a name="contributors">Contributors</a>

The easiest way to track the contributors to the project will be to check the contributors listed on GitHub.

[Readme file edited using prose.io]

## <a name="license">License<a/>

Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at:
      http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.

## Apendices

## SmartThings Capabilities Supported
In order to support the broadest set of interactions with other parts of the SmartThings ecosystem, the below SmartThings Capabilities have been implemented. (More information about SmartThings Capabilities can be found [here](http://docs.smartthings.com/en/latest/capabilities-reference.html).

Capabilities are important as it allows the device to be used and selected in other usecases (such as from other SmartApps) in a standard, interoperable way.


### Device Handler: Ecobee Thermostat
Supports the following capabilities (tagging capabilities not listed). Unless noted, each capability is Fully Implemented. Follow the links for more details on each capabability:
* capability ["Thermostat"](http://docs.smartthings.com/en/latest/capabilities-reference.html#thermostat)
* capability "Thermostat Cooling Setpoint"
* capability "Thermostat Fan Mode"
* capability "Thermostat Heating Setpoint"
* capability "Thermostat Mode"
* capability "Thermostat Operating State"
* capability "Thermostat Setpoint"
* capability ["Temperature Measurement"](http://docs.smartthings.com/en/latest/capabilities-reference.html#temperature-measurement)
* capability ["Motion Sensor"](http://docs.smartthings.com/en/latest/capabilities-reference.html#motion-sensor)
* capability ["Relative Humidity Measurement"](http://docs.smartthings.com/en/latest/capabilities-reference.html#relative-humidity-measurement)

* capability "Polling"
* capability "Refresh"


        
#### Capability: [Thermostat](http://docs.smartthings.com/en/latest/capabilities-reference.html#thermostat) (_capability.thermostat_)

Follow the above link to the capability for more details on the standard attributes and commands.

**Compliance:** Fully Implemented

**Additional Attributes:**

**Attribute**		| **Type** 		| **Possible Values**
:------------------|:-------------|:-------------------------------------------------:
temperatureScale	| String		| "C" "F"
thermostatStatus	|				|
apiConnected		| String		| "full" "warn" "lost"
currentProgram		| String		| 	 
currentProgramId	| String		| 
weatherSymbol		| String		| 

- _temperatureScale_: Indicates if Fahrenheit of Celsius is being used
- _thermostatStatus_: Used to provide status information to the UI such as "Resuming schedule..."
- _apiConnected_: Indicates the current state of the API connection to the Ecobee servers
- _currentProgram_: The string representation of the program (Comfort Setting) currently being executed.
- _currentProgramId_: The ID of the current running program
- _weatherSymbol_: Indicates the current weather pattern currently in effect according to the Ecobee forecast information (e.g. Partly Cloudy, Sunny, Fog, etc)


**Additional Commands:**
- _setTemperature(number)_
- _auxHeatOnly()_
- _raiseSetpoint()_
- _lowerSetpoint()_
- _resumeProgram()_
- _setThermostatProgram(String)_
- _home()_
- _sleep()_
- _away()_

#### Capability: [Temperature Measurement](http://docs.smartthings.com/en/latest/capabilities-reference.html#temperature-measurement) (capability.temperatureMeasurement)
Follow the above link to the capability for more details on the standard attributes and commands.

**Compliance:** Fully Implemented

#### Capability: [Motion Sensor](http://docs.smartthings.com/en/latest/capabilities-reference.html) (capability.motionSensor)
Follow the above link to the capability for more details on the standard attributes and commands.
 
**Compliance:** Fully Implemented
 
 
 #### Capability: [Relative Humidity Measurement](http://docs.smartthings.com/en/latest/capabilities-reference.html) (capability.motionSensor)
Follow the above link to the capability for more details on the standard attributes and commands.
 
**Compliance:** Fully Implemented



### Device Handler: Ecobee Sensor
Supports the following capabilities (tagging capabilities not listed). Follow the links for more details on each capabability:
* capability ["Temperature Measurement"](http://docs.smartthings.com/en/latest/capabilities-reference.html#temperature-measurement)
* capability ["Motion Sensor"](http://docs.smartthings.com/en/latest/capabilities-reference.html#motion-sensor)
* capability "Polling"
* capability "Refresh"
