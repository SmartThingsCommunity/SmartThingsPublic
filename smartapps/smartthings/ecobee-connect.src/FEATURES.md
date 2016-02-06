# Feature Description for the Ecobee (Connect) SmartApp and Related Components

## Introduction
This document describes the various features related to the Ecobee (Connect) Open Source SmartApp and the related compoenents.

The following components are part of the solution:
- **Ecobee (Connect) SmartApp**: This SmartApp provides a single interface for Ecobee Authorization, Device Setup (both Thermostats **and** Sensors), Behavioral Settings and even a Debug Dashboard. Additional features can be added over time as well thanks to built in support for Child SmartApps, keeping everything nicely integrated into one app.
- **ecobee Routines Child SmartApp**: Child app that lets you trigger settings changes on your Ecobee thermostats based on the SmartThings Hello Modes. Settings include the Ecobee Program (Comfort Settings), Fan Modes and Hold Types. In additional to SmartThings Hello Modes, sunrise/sunset triggers are also support. Multiple instances of the SmartApp are also supported for maximum flexibility.
- **Ecobee Thermostat Device Handler**: This implements the Device Handler for the Ecobee Thermostat functions and attributes.
- **Ecobee Sensor Device Handler**: This implements the Device Handler for the Ecobee Sensor attributes.



