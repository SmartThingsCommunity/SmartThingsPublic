Ecobee Thermostat SmartApp(s) and related Device Types for use with the SmartThings Hub
======================================================

## <a name="top">Table Of Contents</a>
- [Introduction](#intro)
- [Motivation](#motivation)
- [Quick Links](#quicklinks)
- [Installation](#installation)
  - [Install Device Types](#install-device-types)
  - [Install SmartApp in IDE](#install-smartapp)
  - [Install SmartApp on Device](#install-smartapp-phone)
- [Updating](#updating)
- [Troubleshooting](#troubleshooting)
- [Reporting Issues](#reporting-issues)
- [Open Items](#open-items--to-dos)
- [Contributors](#contributors)
- [License](#license)

## <a name="intro">Introduction</a>

This SmartApp and the related Device types are intended to be used with the [Ecobee thermostats](http://www.ecobee.com/). 

Here are links to the working version of the repository being developed and maintained by Sean Schneyer [(on GitHub)](https://github.com/StrykerSKS) [(on SmartThings Community)](https://community.smartthings.com/users/strykersks/).

## <a name="motivation">Motivation</a>

The intent is to provide an Open Source Licensed implementation that can be used by the SmartThings community of users. This will help ensure accessibility for all users and provide for an easy mechanism for the community to help maintain/drive the functionality.

The ultimate goal would be to have these capabilities become part of the stock drivers on the "master" branch. But until that time they will be maintained as a fork providing good visibility to any changes upstream.

## <a name="quicklinks">Quick Links</a>
- README.md (this file): <https://github.com/StrykerSKS/SmartThingsPublic/blob/StrykerSKS-Ecobee3/smartapps/smartthings/ecobee-connect.src/README.md>
- Ecobee (Connect) SmartApp: <https://github.com/StrykerSKS/SmartThingsPublic/tree/StrykerSKS-Ecobee3/smartapps/smartthings/ecobee-connect.src>
- Ecobee Thermostat Device: <https://github.com/StrykerSKS/SmartThingsPublic/tree/StrykerSKS-Ecobee3/devicetypes/smartthings/ecobee-thermostat.src>
- Ecobee Sensor Device: <https://github.com/StrykerSKS/SmartThingsPublic/tree/StrykerSKS-Ecobee3/devicetypes/smartthings/ecobee-sensor.src>
- SmartThings IDE: <https://graph.api.smartthings.com>

-----------------------------
# <a name="installation">Installation</a>

## General
> **NOTE**: While I have tested this on my system and believe it to be working. I have not run a full battery of tests that can include all possible corner cases or configurations. It is possible, perhaps even _likely_, that there are still bugs in this code. If you do run into an issue, the best option is to report it using the [Issues](https://github.com/StrykerSKS/SmartThingsPublic/issues) tab within the GitHub repository.

It is highly recommended that you use the GitHub Integration that SmartThings offers with their IDE. This will make it **much** easier to keep up to date with changes over time. For the general steps needed for setting up GitHub IDE integration, please visit <http://docs.smartthings.com/en/latest/tools-and-ide/github-integration.html> and follow the steps for performing the setup.

## Install Preparation
The first step is to ensure that you delete any existing Ecobee related devices and SmartApps. They are likely not compatible with this codebase and are almost certain to cause problems down the road. 

If you are not familiar with adding your own custom devices, then be sure to familiarize yourself with the [SmartThings IDE](https://graph.api.smartthings.com/) before you begin the installation process.

You will also need to make sure that you remember your Ecobee username and password. You should login to <http://www.ecobee.com/> now to ensure you have your credentials.

## <a name="install-device-type">Install Device Types</a>
Here we will install two (2) different Device Types:
- `Ecobee Thermostat`
- `Ecobee Sensor`

Follow the steps for _either_ the GitHub Integration or the Manual method below. Do **not** try to do both methods.

### Install Using GitHub Integration
Follow these steps (all within the SmartThings IDE):
- Click on the `My Device Types` tab
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
- Verify that the two devices show up in the list and are marked with Status `Published`



### Install Manually from Code
For this method you will need to have one browser window open on GitHub and another on the IDE.

Follow these steps to install the Ecobee Sensor:
- [IDE] Click on the `My Device Types` tab
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

Follow these steps to install the Ecobee Thermostat:
- [IDE] Click on the `My Device Types` tab
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


## <a name="install-smartapp">Install SmartApp in IDE</a>
Here we will install the following SmartApp:
- `Ecobee (Connect)`

Follow the steps for _either_ the GitHub Integration or the Manual method below. Do **not** try to do both methods.

### Install Using GitHub Integration
Follow these steps (all within the SmartThings IDE):
- Click on the `My SmartApps` tab
- Click `Settings`
- Click `Add new repository` and use the following parameters:
  - Owner: `StrykerSKS`
  - Name: `SmartThingsPublic`
  - Branch: `StrykerSKS-Ecobee3`
- Click `Save`
- Click `Update from Repo` and select the repository we just added above
- Find and Select `ecobee-connect.groovy`
- Select `Publish`(bottom right of screen near the `Cancel` button)
- Click `Execute Update`
- Note the response at the top. It should be something like "`Updated 0 and created 1 SmartApps, 1 published`"
- Verify that the SmartApp shows up in the list and is marked with Status `Published`
- Click on the `Edit Properties` button to the left of the SmartApp that we just added (looks like pencil on a paper)
- Click on the `OAuth` tab
- Click `Enable OAuth in Smart App`
- Click `Update` (bottom left of screen)
- Verify that `Updated SmartApp` appears at the top of the screen

### Install Manually from Code
For this method you will need to have one browser window open on GitHub and another on the IDE.

Follow these steps to install the Ecobee Sensor:
- [IDE] Click on the `My SmartApps` tab
- [IDE] Click `New SmartApp` (top right corner)
- [IDE] Click `From Code`
- [GitHub] Go to the respository for the Ecobee Connect SmartApp: <https://github.com/StrykerSKS/SmartThingsPublic/blob/StrykerSKS-Ecobee3/smartapps/smartthings/ecobee-connect.src/ecobee-connect.groovy>
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


## <a name="install-smartapp-phone">Install and Run SmartApp on Phone/Tablet</a>
> **NOTE**: I have only tested this on an iPhone 6 as I do not have access to Android device. Feedback and bug reports are welcome if any issues are found.

The SmartApp will guide you through the basic installation and setup process. It includes the following aspects:
- Authentication with Ecobee to allow API Calls for your thermostat(s) (and connected sensors)
- Discover and selection of Thermostats
  - Setup of option features/parameters such as Smart Auto Temp Control, Polling Intervals, etc
- Discover and selection of Remote Sensors (if there are any)

Follow these steps for the SmartApp on your mobile device:
- Open the SmartThings app
- Open the `Marketplace`
- Click the `SmartApps` tab
- Select `My Apps` (all the way at the bottom of the list)
- Click `Ecobee (Connect)`
- Click (as indicated on the screen) to enter your Ecobee Credentials
- Enter your Ecobee Email and Password
- Click `Accept`
- You should receive a message indicating `Your ecobee Account is now connected to SmartThings!`
- Click `Done` 
- Click `Next` (top right corner)
- Select which thermostats you want connected
- Optionally, set the other settings:
  - Select Hold Type: This determines how a hold will behave. Either Permanent (until changed) or Temporary (until next program engages)
  - Use Smart Auto Temperature Adjust: This allows you to adjust the temperature using the `Up` and `Down` arrows within SmartThings even when the device is in mode `Auto`. 
  - Polling Interval: Determines how often to poll (via API calls) for fresh thermostat/sensor data
- Click `Next`
- Select which Sensors you want connected. NOTE: Only sensors associated with a thermostat from the previous step will be shown.
- Click `Done` (to close the selection window)
- Click `Done` (to initiate the SmartApp installation)
- You should receive a small green popup at the top stating "`Ecobee (Connect) is now installed and automating`"


At this point, the SmartApp will automatically create all of the new devices, one for each thermostat and sensor. These will show up in your regular `Things` list within the app. 

> **NOTE**: It may take a few minutes for the new devices to show up in the list. You should try refreshing the list (pull down on the list). In extreme cases, you may have to restart the SmartThings app on your phone to update the list. You should only have to do this once.

<br/>

> **NOTE 2**: If you uninstall the SmartApp it will automatically remove all of the thermostats and sensors that it previously installed. This is necessary as those devices are "children" of the SmartApp.

<br/>

> There is currently a lot of debug information generate from the app. If you need to do any kind of troubleshooting, you can see the current information flowing through in the `Live Logging` tab of the SmartThings IDE. You will also need this information if you open an `Issue` since it will be needed to track down what is going on. ** Please ensure that you do not include any personal information from the logs in an `Issue` report. **

-------------------------
## Updating
If you have enabled GitHub integration with the SmartThings IDE, then updates are a breeze. Otherwise the steps are a bit more manual but not too complicated.

### Updating with GitHub Integration
The IDE provides visual cues to alert you that any device types or SmartApps have been updated in their upstream repositories. See the [GitHub/IDE integration guide](http://docs.smartthings.com/en/latest/tools-and-ide/github-integration.html) for more details on the different colors.

Once you have determined that an update is available, follow these steps:
- Login to the SmartThings IDE
- Go to either the `My Device Types` or `My SmartApps` tabs to see if there are updates (the color of the item will be purple)
- Click the `Update from Repo` button (top right)
- Select the repository and branch you want to update `SmartThingsPublic (StrykerSKS-Ecobee3)`
- The item should show up in the `Obsolete (updated in GitHub)` column and automatically be selected
- Select `Publish` (bottom right)
- Click `Execute Update` (bottom right)
- You should receive a confirmation message such as this example: `Updated 1 and created 0 SmartApps, 1 published`
- (Optional, but recommended) Rerun the `Ecobee (Connect)` SmartApp. This seems to eleviate any residual issues that may occur due to the update

You should now be running on the updated code. Be sure that you check for both updates of the SmartApp **and** the Device Type. Updating one but not the other could cause compatibility problems.

### Updating manually (without GitHub Integration

> TODO: Fill in the directions for manual upgrade

-------------------------
## Troubleshooting

| Symptom 	| Possible Solution 	|
|---------	|-------------------	|
| The devices are not showing up in the Things tab after installation    	|  It can take several minutes for things to show up properly. If you don't want to wait then simply kill the SmartThings app and reload it.              	|
| Receive error similar to "error java.lang.NullPointerException: Cannot get property 'authorities' on null object"        	| This indicates that you have not turned on OAuth for the SmartApp. Please review the installation instructions and complete the OAuth steps.                  	|
| Irregular behavior after an update to the SmartApp or Device Handler code| It is possible that after updating the codebase that you may experience strange behavior, including possible app crashes. This seems to be a general issue with updates on SmartThings. Try the following steps: <br/> 1) Re-run the `Ecobee (Connect)` SmartApp to re-initialize the devices <br/> 2) If that does not solve the problem, remove and re-install the SmartApp |
|         	|                   	|
|         	|                   	|

### Live Logging on IDE
TODO: Put information on getting to the Live Logging on the IDE

### Installed SmartApps Info on IDE
TODO: Put information about accessing the Installed SmartApps screens to get more information about the state of the SmartApp. In particular, can be used to determine if the poll handlers are still running.


-------------------------
## <a name="reporting-issues">Reporting Issues</a>
All issues or feature requests should be submitted via the GitHub issue capability. It can be found on the [Issues](https://github.com/StrykerSKS/SmartThingsPublic/issues) tab within the GitHub repository.

You are also welcome to engage in discussions using the [SmartThings Community](https://community.smartthings.com/).

## <a name="open-items">Open Items / To Dos</a>

There is still plenty of work left to do and items to work on. Here is a list of some of the items. This may also serve as a possible wishlist in the future:
- Celsius: So far I have only tested this with Farhenheit. I plan to also test Celsius but haven't had a chance to do so. From working with the code, I suspect that there **will** be issues found here.
- Additional SmartApps for things like Mode changes, etc.
- Need to test the setting of the holdType at the individual thermostats to override the value set at the SmartApp level. (It should be working but has not been fully tested.)


## <a name="contributors">Contributors</a>

The easiest way to track the contributors to the project will be to check the contributors listed on GitHub.

[Readme file edited using prose.io]

## <a name="license">License<a/>

Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at:
      http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
