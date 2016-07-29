/**
 *  Ask Alexa - Lambda Code
 *
 *  Version 1.1.7 - 7/29/16 Copyright © 2016 Michael Struck
 *  Special thanks for Keith DeLong for code and assistance 
 *  
 *  Version 1.0.0 - Initial release
 *  Version 1.0.1 - Removed dedicated status operation; added version code
 *  Version 1.1.0 - Added two addition intent types for groups and macros
 *  Version 1.1.1 - Added messages to indicate bad OAuth or Application ID
 *  Version 1.1.2 - Fixed small syntax error in a couple responses
 *  Version 1.1.3 - Fixed additional syntax items; added stop/cancel/end/yes options
 *  Version 1.1.4 - Added some randomization to responses
 *  Version 1.1.5 - Code optimization, more responses
 *  Version 1.1.6 - Minor code/syntax changes. Organized code to allow for more custom responses
 *  Version 1.1.7 - Code reorganization to allow for future functions
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
'use strict';
exports.handler = function( event, context ) {
    var versionTxt = '1.1.7';
    var versionDate= '07/29/2016';
    var versionNum = '117';
    var https = require( 'https' );
    // Paste app code here between the breaks------------------------------------------------
    
    
    
    //---------------------------------------------------------------------------------------
    var cardName ="";
    var endSession = true;
    var processedText;
    //Get SmartThings parameters
    var beginURL = url + 'b?Ver=' + versionTxt + '&Date=' + versionDate + '&lVer=' + versionNum + '&access_token=' + STtoken;
    https.get( beginURL, function( response ) {
        response.on( 'data', function( data ) {
            var beginJSON = JSON.parse(data);
            var contOptions = beginJSON.continue; 
            var personality = beginJSON.personality;
            var STver = beginJSON.SmartAppVer;
            var IName = beginJSON.IName;
            if (beginJSON.OOD) { amzRespose("OutOfDate", context, IName, versionTxt, personality, STver, contOptions); }
            if (beginJSON.error) { output("There was an error with the Ask Alexa SmartApp execution. If this continues, please contact the author of the SmartApp. ", context, "Lambda Error", endSession); }
            if (beginJSON.error === "invalid_token" || beginJSON.type === "AccessDenied") {
                output("There was an error accessing the SmartThings cloud environment. Please check your security token and application ID and try again. ", context, "Lambda Error", endSession); 
            }
            if (event.request.type == "LaunchRequest") { amzRespose( "LaunchRequest", context, IName, versionTxt, personality, STver, contOptions); }
            else if (event.request.type == "SessionEndedRequest"){}
            else if (event.request.type == "IntentRequest") {
                var process = false;
                var intentName = event.request.intent.name;
                if (intentName.startsWith("AMAZON") && intentName.endsWith("Intent")) { amzRespose( intentName, context, IName, versionTxt, personality, STver, contOptions); }
                else if (intentName == "VersionOperation") { amzRespose( "VersionOperation", context, IName, versionTxt, personality, STver, contOptions); }
                else if (intentName == "DeviceOperation") {
                    var Operator = event.request.intent.slots.Operator.value;
                    var Device = event.request.intent.slots.Device.value;
                    var Num = event.request.intent.slots.Num.value;
                    var Param = event.request.intent.slots.Param.value;
                    url += 'd?Device=' + Device + '&Operator=' + Operator + '&Num=' + Num + '&Param=' + Param; 
                    process = true;
                    cardName = "SmartThings Devices";
                } 
                else if (intentName == "ListOperation") {
                    var Type = event.request.intent.slots.Type.value;
                    url += 'l?Type=' + Type;
                    process = true;
                    cardName = "SmartThings Help";
                }
                else if (intentName == "MacroOperation") {
                    var Macro = event.request.intent.slots.Macro.value;
                    var MNum = event.request.intent.slots.MNum.value;
                    var MCmd = event.request.intent.slots.MCmd.value;
                    var MParam = event.request.intent.slots.MParam.value;
                    var Cancel = event.request.intent.slots.Cancel.value;
                    if (Cancel) {MNum = 9999}
                    url += 'm?Macro=' + Macro + '&Param=' + MParam + '&Cmd=' + MCmd + '&Num=' + MNum;
                    process = true;
                    cardName = "SmartThings Voice Macro";
                }
                else if (intentName == "SmartHomeOperation") {
                    var SHCmd = event.request.intent.slots.SHCmd.value;
                    var SHParam = event.request.intent.slots.SHParam.value;
                    url += 'h?SHCmd=' + SHCmd + '&SHParam=' + SHParam;
                    process = true;
                    cardName = "SmartThings Home Operation";
                }
                if (!process) { output(getResponse("respError"), context, "Ask Alexa Error", false); }
                else {
                    url += '&access_token=' + STtoken;
                    https.get( url, function( response ) {
                        response.on( 'data', function( data ) {
                        var resJSON = JSON.parse(data);
                        var speechText;
                        if (resJSON.voiceOutput) { 
                            speechText = resJSON.voiceOutput; 
                            if (speechText.endsWith("%")){
                                processedText = processOutput(speechText, personality, contOptions);
                                speechText = processedText[0];
                                endSession = processedText[1];
                            }
                        }
                        console.log(speechText);
                        output(speechText, context, cardName, endSession);
                        } );
                    } );
                }
            }
        } );
    } );
};

function processOutput(speechText, personality, contOptions){
    var endSession = true;
    if (speechText.endsWith("%1%")) { 
        if (contOptions.startsWith("1")){
            speechText = speechText.slice(0, -3);
            speechText += getResponse("appError", personality);
            endSession = false;
        }
        else { speechText = speechText.slice(0, -3); }
    }
    if (speechText.endsWith("%2%")) { 
        if (contOptions.slice(1,-2)=="1"){
            speechText = speechText.slice(0, -3);
        	speechText += getResponse("Ending", personality);
            endSession = false;
        }
        else { speechText = speechText.slice(0, -3); }
    }
    if (speechText.endsWith("%3%")) { 
        if (contOptions.slice(2,-1)=="1"){
            speechText = speechText.slice(0, -3);
            speechText += getResponse("Ending", personality);
            endSession = false;
        }
        else { speechText = speechText.slice(0, -3); }
    }
    if (speechText.endsWith("%4%")) { 
        if (contOptions.endsWith("1")){
        	speechText = speechText.slice(0, -3);
        	speechText += getResponse("Ending", personality);
            endSession = false;
        }
        else { speechText = speechText.slice(0, -3); }
    }
    return [speechText,endSession];
}

function amzRespose(type, context, IName, versionTxt, personality, STver, contOptions){
    var resText;
    var processedText;
    if (type == "AMAZON.YesIntent") { output(getResponse("Yes",personality), context, "SmartThings Alexa Yes Command", false); }
    else if (type == "OutOfDate") { output(getResponse("OOD",personality), context, "Lambda Code Version Error", true); }
    else if (type == "AMAZON.NoIntent") { output(getResponse("No",personality), context, "SmartThings Alexa End Command", true); }
    else if (type == "AMAZON.StopIntent") { output(getResponse("Stop",personality), context, "Amazon Stop", true); }
    else if (type == "AMAZON.CancelIntent") { output(getResponse("Cancel",personality), context, "Amazon Cancel", true); }
    else if (type == "LaunchRequest") { output(getResponse("Launch", personality), context, "Ask Alexa Help", false); }
    else if (type == "AMAZON.HelpIntent") {
        resText = "With the Ask Alexa SmartApp, you can integrate your SmartThings household with me; this will allow you to give me commands "+
        "to turn off a light, or unlock a door. For example, you can simply say, 'tell "+ IName +" to turn off the living room', and I'll turn off that device. " +
        "In addition, you can query your devices to get information such as open or close status, or find out the temperature in a room. To use this function, just give "+
        "me the name of the device. For example, you could say, 'ask "+ IName +" about the patio', and I'll give you all of the common attributes with that device, including battery levels. "+
        "For those who are curious, you are running SmartApp version " + STver + ", and version "+ versionTxt +" of the Lambda code, both written by Michael Struck. %2%";
        processedText = processOutput(resText, personality, contOptions);
        output(processedText[0], context, "Ask Alexa Help", processedText[1]);
    }
    else if (type == "VersionOperation") {
        resText = "The Ask Alexa SmartApp was developed by Michael Struck to intergrate the SmartThings platform with the Amazon Echo. The SmartApp version is: "  +  STver + ". And the Amazon Lambda code version is: " + versionTxt + ". %2%";
        processedText = processOutput(resText, personality, contOptions);
        output(processedText[0], context, "SmartThings Version Help", processedText[1]);
    }
}

function getResponse(respType, style){
    var response;
    if (style == "Normal") { response = responseNormal(respType); }
    return response;
}

function output( text, context, card, complete ) { 
   var response = {
      outputSpeech: {
         type: "PlainText",
         text: text
      },
      card: {
         type: "Simple",
         title: card,
         content: text
      },
   shouldEndSession: complete
   };
   context.succeed( { response: response } );
}

function responseNormal(respType){
    var responses;
    if (respType == "OOD"){
        responses = ["I am unable to complete your request. The version of the Lambda code you are using is out-of-date. Please install the latest code and try again. "
                    , "The version of the Lambda code you are using is out-of-date. Please install the latest code and try again. "];    
    }
    if (respType == "appError"){
        responses = ["Would you like to try again? ","Want to try again? ","Do you want to try again? "];    
    }
    else if (respType == "Launch"){
        responses= ["Simply give me a device and a command, or ask me the status of a device, and I will carry out your request. Would you like to try something? " 
                    ,"What would you like to do with your SmartThings environment? ","Would you like to try something? "];
    }
    else if (respType == "respError"){
        responses = [" I am not sure what you are asking. Would you like to try again? "
                    , " Sorry, I didn't understand. Want to try again? "
                    , " I did not understand what you want me to do. Like to try again? "];
    }
    else if (respType =="Cancel"){
        responses = [" Cancelling. ", " Ok. Cancelling. ", "Ok. ", " "];
    }
    else if (respType == "Stop"){
        responses = [" Stopping. ", " Ok. Stopping. ", "Ok. ", " "];
    }
    else if (respType == "No"){
        responses = [" Ok. ", " Ok. Maybe later. ", " Ok. I am here if you need me. " 
                    ," Ok. Let me know if you need anything later. ", " "];
    }
    else if (respType == "Yes" ){
        responses = [" Ok. Simply say what you want me to do with your SmartThings devices or macros. "
                    , " Ok. What would you like to do? ", " Ok. Ready for your commands. ", " Ok. Go ahead. "];       
    }
    else if (respType == "Ending") {
        responses = ["Would you like to do something else? ","Want anything else? ","Do you want anything else? " 
                    ,"Need anything else? ","Do you need anything else? ", "Want to do something else? "
                    ,"Do you want to do anything else? ", "Can I help with anything else? ", "Anything else? "];
    }
    var response = responses[Math.floor(Math.random() * responses.length)];
    return response;
}
