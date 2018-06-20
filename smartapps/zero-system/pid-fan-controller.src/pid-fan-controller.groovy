/**
 *  PID Fan Controller
 *
 *  Copyright 2018 Zero_System
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
		name: "PID Fan Controller" ,
		namespace: "zero-system" ,
		author: "Zero_System" ,
		description: "PID Fan Control. \nThis app varies a fan(s) speed, by using a dimmer. \nThe app is able to maintain a constant room temperature even if a heat source present, e.g. a computer." ,
		category: "My Apps" ,
		iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png" ,
		iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png" ,
		iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png" )

// @formatter:off
preferences
{
    section()
    {
        paragraph "Choose a temperature sensor. If multiple temp sensors are choosen, the average will be taken"
        input( title: "Temperature Sensor(s)" , name: "tempSensors" , type: "capability.temperatureMeasurement" , multiple: true , required: true )

        paragraph "Select the cooling fan controller (dimmer)"
        input( title: "Fan Controller(s)" , name: "fans" , type: "capability.switchLevel" , multiple: true , required: true )

        paragraph "Set the desired target temperature"
        input( title: "Target Temp" , name: "targetTemp" , type: "decimal" , required: true , description: "70 (deg)" , defaultValue: 70 )
	
	    paragraph "Set the minimum temperature. If the temperature reaches the set minimum, the fan will be turned off, overriding the minimum fan speed"
	    input( title: "Minimum Temperature" , name: "minTemp" , type: "decimal" , required: true , description: "70 (deg)" , defaultValue: 65 )

        paragraph "Set the minimum fan speed. This prevents the fan from turning on and off at low speeds"
        input( title: "Minimum Fan Speed" , name: "minFanLevel" , type: "decimal" , required: true , description: "10%" , range: "0..100" , defaultValue: 10 )

        paragraph "Sampling Time. It is the time between each measurement. Lower time means a faster rate of adjustment"
        input( title: "Sampling Time" , name: "samplingTime" , type: "enum" , required: true , options: ["1-Minute" , "5-Minutes" , "10-Minutes" , "15-Minutes"] , defaultValue: "1-Minute" )
        
        paragraph "Reverse control direction. Enable to reverse the direction of control. For example, heating."
        input( title: "Reverse Control Direction" , name: "reverseDirection" , type: "bool" , required: true , defaultValue: false )
        
        paragraph "Enable PID Control during time frame. Set time frame below. For example, 09:00 - 17:00"
        input( title: "Enable Time Frame" , name: "enableTimeFrame" , type: "bool" , required: true , defaultValue: false )
    }

    section( "Time frame." , hideable: true , hidden: true )
    {
        input( title: "Start Time" , name: "startTime" , type: "time" , required: false , discription: "09:00" , defaultValue: "09:00" )
        input( title: "Stop Time" , name: "stopTime" , type: "time" , required: false , discription: "17:00" , defaultValue: "17:00")
    }

    section( "Set PID variables." , hideable: true , hidden: true )
    {
        input( title: "P Variable" , name: "pVar" , type: "decimal" , required: false , description: "20" , defaultValue: 20 )
        input( title: "I Variable" , name: "iVar" , type: "decimal" , required: false , description: "1" , defaultValue: 1 )
        input( title: "D Variable" , name: "dVar" , type: "decimal" , required: false , description: "10" , defaultValue: 10 )
    }
}
// @formatter:on

def installed()
{
	log.debug "Installed with settings: ${settings}"
	
	initialize()
}

def updated()
{
	log.debug "Updated with settings: ${settings}"
	
	unsubscribe()
	initialize()
}

def initialize()
{
	/*working variables*/
	state.numTempSensors = settings.tempSensors.size()
	
	state.iValue = 0.0
	state.lastTemp = getTemp()
	state.lastTime = getTime()
	
	state.fanState = enableFan()
	state.lastFanLevel = setFan( 0.0 )
	
	setPID()
	
	runPID()
}

void runPID()
{
	switch ( samplingTime ) // Weird case values are smartthings enum weirdness
	{
		case "1-Minute":
			runEvery1Minute( scheduledHandler )
			break
		
		case "1":
			runEvery5Minutes( scheduledHandler )
			break
		
		case "2":
			runEvery10Minutes( scheduledHandler )
			break
		
		case "3":
			runEvery15Minutes( scheduledHandler )
			break
		
		default:
			log.error "runPID: switch($samplingTime) - Unmached Case."
			runEvery1Minute( scheduledHandler )
			break
	}
}

void scheduledHandler()
{
	log.debug "=========================================="
	
	log.debug "timeFrameEnabled: $settings.enableTimeFrame"
	if ( settings.enableTimeFrame )
	{
		Date currentTime = new Date()
		boolean withinTimeFrame = timeOfDayIsBetween( settings.startTime , settings.stopTime , currentTime , location.timeZone )
		
		log.debug "scheduledHandler: TIME( start: $startTime, stop: $stopTime, time: $currentTime, value: $withinTimeFrame )"
		if ( withinTimeFrame )
		{
			calculatePID()
		}
		else
		{
			state.iValue = 0.0
			state.lastTemp = getTemp()
			state.lastTime = getTime()
			setFan( 0.0 )
		}
	}
	else
	{
		calculatePID()
	}
}

void calculatePID()
{
	double currentTemp = getTemp( true )
	
	/*How long since we last calculated*/
	long currentTime = getTime()
	long timeChange = ( currentTime - state.lastTime ) / 1000.0
	log.debug "PID: TIME( timeChange: $timeChange (sec) )"
	
	/*Compute all the working error variables*/
	double pValue = settings.targetTemp - currentTemp
	
	state.iValue = ( state.iValue + ( state.ki * pValue ) )
	
	// @formatter:off //Windup elimination. Clamps I value between min and 100
	if ( state.iValue < settings.minFanLevel )	state.iValue = minFanLevel
	else if ( state.iValue > 100 )		        state.iValue = 100
	// @formatter:on
	
	double dValue = currentTemp - state.lastTemp
	
	log.debug "PID: ERROR( pValue: $pValue , iValue: $state.iValue , dValue: $dValue )"
	
	/*Compute PID Output*/
	double p = state.kp * pValue
	double i = state.iValue
	double d = state.kd * dValue
	double level = p + i - d
	log.debug "PID: COMPUTE( P: $p , I: $i , D: $d )"
	
	setFan( level , true )
	
	/*Remember some variables for next time*/
	state.lastTemp = currentTemp
	state.lastTime = currentTime
}

// @formatter:off
int setFan( double rawLevel , boolean logging = false)
{
	int boundedLevel
	
	if      ( getTemp() < settings.minTemp )    boundedLevel = 0    // Min temp cutoff
	else if ( fanState() ) 			            boundedLevel = 0    // Sentry value. If fan needs to be turned off
	else if ( rawLevel < settings.minFanLevel )	boundedLevel = minFanLevel  // Min
	else if ( rawLevel > 100 ) 				    boundedLevel = 100          // Max
	else 										boundedLevel = ( int ) Math.round( rawLevel ) // Calculated
	
	//	fans.setLevel( boundedLevel) // TODO: see if it sets all fan levels
	
	if ( boundedLevel != state.lastFanLevel )   // Prevent const commands being sent to controller if no change is detected.
		for ( fan in settings.fans )
			fan.setLevel( boundedLevel )
	
	
	if ( logging ) log.debug "OUTPUT: ( rawLevel: $rawLevel , boundedLevel: $boundedLevel )"
	state.lastFanLevel = boundedLevel
	return boundedLevel
}
// @formatter:on

// @formatter:off
double getTemp( boolean logging = false )
{
	double temp
	
	if ( state.numTempSensors == 1 ) temp = settings.tempSensors.get( 0 ).currentValue( "temperature" )
	else
	{
		double sum = 0.0
		
		
		for ( sensor in settings.tempSensors )
			sum += sensor.currentValue( "temperature" )
		

		temp = sum / state.numTempSensors
	}
	
	if ( logging ) log.debug "TEMP: ( temp: $temp )"
	return temp
}
// @formatter:on

long getTime( boolean logging = false )
{
	long currentTime = now()
	if ( logging ) log.debug( "getTime: $currentTime" )
	return currentTime
}

void setPID()
{
	if ( settings.reverseDirection )
	{
		state.kp = settings.pVar
		state.ki = settings.iVar
		state.kd = settings.dVar
	}
	else
	{
		state.kp = 0 - settings.pVar
		state.ki = 0 - settings.iVar
		state.kd = 0 - settings.dVar
	}
}

boolean disableFan() {return state.fanState = true}

boolean enableFan() {return state.fanState = false}

boolean fanState()
{
	if ( state.fanState ) log.debug( "FAN_STATE: OFF" )
	return state.fanState
}