/**
 *  Smart Heat Tape Controller 
 *
 *  Copyright 2015 Edward Robertshaw
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

    name: "Smart Heat Tape Controller V2.1",
    namespace: "erobertshaw",
    author: "Aperattions.com llc",
    description: "Smart heat tape controller. Reduced energy usage by checking weather forecast.",
    category: "Green Living",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png") {
    appSetting "zipcode"
}


preferences { 
  section("Smart heat tape setup") {
    input "heattape", "capability.switch", title: "Heat tape switch", required: true, multiple: true
    input "zipcode", "text", title: "Zipcode", required: true
  }
}




def installed() {
	log.debug "Installed with settings: ${settings}"

	initialize()
}

def updated() {
	log.debug "Updated with settings: ${settings}"

	unsubscribe()
	initialize()
}

def initialize() {
	runEvery1Hour(updateHeatTape)	
}

def setupInitialConditions(){
   if (state.snowOnRoof == null){
    	state.snowOnRoof = false 
    } 
    
    if(state.melt_point_score_since_snow == null){
    	state.melt_point_score_since_snow = 0
    }
}

def updateHeatTape() {
    log.debug "updateHeatTape"  
    
 	setupInitialConditions()
       
    Map currentConditions =  getWeatherFeature("conditions" , zipcode)
    
   	//currentConditions.current_observation.each { k ,v -> log.debug(k + ": " + v) }

	recordWeatherStats(currentConditions.current_observation)

   	log.debug "zipcode:" + zipcode
    log.debug "aboveMeltTemperature:" + state.aboveMeltTemperature
    log.debug "----melt_point_score_since_snow:" + state.melt_point_score_since_snow
    
 
    state.snowOnRoof = (state.melt_point_score_since_snow < 0 )
    
    log.debug "snowOnRoof:" + state.snowOnRoof
    
    setHeatTape()
   
}

def caculateNewSnow(observation){

	if( !( isSnowing(observation) )){
		return;
    }

    if( observation.precip_1hr_metric > 40 ){
        state.melt_point_score_since_snow = -150 + state.melt_point_score_since_snow  
    }else if( observation.precip_1hr_metric > 30 ){
        state.melt_point_score_since_snow = -100 + state.melt_point_score_since_snow  
    } else if(observation.precip_1hr_metric > 25 ){
        state.melt_point_score_since_snow = -60 + state.melt_point_score_since_snow 
    } else if( observation.precip_1hr_metric > 10 ){
        state.melt_point_score_since_snow = -30 + state.melt_point_score_since_snow 
    } else if( observation.precip_1hr_metric > 5 ){
        state.melt_point_score_since_snow = -4 + state.melt_point_score_since_snow 
    } else if( observation.precip_1hr_metric > 3 ){
        state.melt_point_score_since_snow = -2 + state.melt_point_score_since_snow  
    } else if( observation.precip_1hr_metric > 0 ){
        state.melt_point_score_since_snow = -1 + state.melt_point_score_since_snow  
    }
    
    if(state.melt_point_score_since_snow < -550){
    	// Max out as snow at this stage will either be falling off the roof or the property getting damaged
    	state.melt_point_score_since_snow = -550 	
    }
    
}

def calculateSnowMeltScore(observation){
    if( observation.temp_c > 20 ){
        state.melt_point_score_since_snow = 100 + state.melt_point_score_since_snow  
    } else if(observation.temp_c > 15 ){
        state.melt_point_score_since_snow = 70 + state.melt_point_score_since_snow 
    } else if( observation.temp_c > 10 ){
        state.melt_point_score_since_snow = 50 + state.melt_point_score_since_snow 
    } else if( observation.temp_c > 5 ){
        state.melt_point_score_since_snow = 4 + state.melt_point_score_since_snow 
    } else if( observation.temp_c > 0 ){
        state.melt_point_score_since_snow = 2 + state.melt_point_score_since_snow  
    } else if( observation.temp_c > -3 ){
        state.melt_point_score_since_snow = 1 + state.melt_point_score_since_snow  
    }

    if(state.melt_point_score_since_snow > 10){
        // No more snow to melt
        state.melt_point_score_since_snow = 0
    }   
}

def isSnowing(observation){
	return isFreezing(observation) && observation.precip_1hr_metric > 0
}

def isFreezing(observation){
	return observation.temp_c < 0
}

def isMelting(observation){
	return observation.temp_c > -3
}

def isRaining(observation){
	return observation.precip_today_metric > 0 && ! isFreezing(observation)
}

def recordWeatherStats(observation){
	log.debug "temp_c:" + observation.temp_c
	if( isFreezing(observation)){
		state.lastFreeze = new Date()
    }
    log.debug "lastFreeze:" + state.lastFreeze

    log.debug "precip_1hr_metric:" + observation.precip_1hr_metric
    if( isRaining(observation) ){
		state.lastPrecipitation = new Date()
    }
    log.debug "lastPrecipitation:" + state.lastPrecipitation
    
    if( isSnowing(observation) ){
		state.lastFreezingPrecipitation = new Date()
    }
    log.debug "lastFreezingPrecipitation:" + state.lastFreezingPrecipitation
    
    calculateSnowMeltScore(observation)
    caculateNewSnow(observation)
    
    if( isMelting(observation) ){
    	state.aboveMeltTemperature = true
    } else {
    	state.aboveMeltTemperature = false		
    }
    
}


def setHeatTape(){
	if(state.snowOnRoof && state.aboveMeltTemperature){
        heattape.each { 
            if( it.currentValue("switch") != "on"){
                it.on()
            }
        }
    }else{
    	 heattape.each { 
            if( it.currentValue("switch") == "on"){
                it.off()
            }
        }
    }
}