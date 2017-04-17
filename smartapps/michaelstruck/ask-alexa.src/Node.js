/**
 *  Ask Alexa - Lambda Code
 *
 *  Version 1.2.5 - 4/12/17 Copyright © 2017 Michael Struck
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
 *  Version 1.2.0a - Addition of courtesy personality responses
 *  Version 1.2.1 - Addition of the Snarky personality responses and change in macro password structure
 *  Version 1.2.2b - Addition of small translation items
 *  Version 1.2.3 - Added follow up to a missing PIN when required, updated copyright to 2017
 *  Version 1.2.4 - Added routines for new message queue
 *  Version 1.2.5 - Changed some of the responses to align with the new Ask Alexa framework
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
    var versionTxt = '1.2.5';
    var versionDate= '04/12/2017';
    var versionNum = '125';
    var https = require( 'https' );
    // Paste app code here between the breaks------------------------------------------------
    var STappID = '';
    var STtoken = '';
    var url='https://graph.api.smartthings.com:443/api/smartapps/installations/' + STappID + '/' ;
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
            var pName = beginJSON.pName;
            if (beginJSON.OOD) { amzRespose("OutOfDate", context, IName, versionTxt, personality, STver, contOptions, pName); }
            if (beginJSON.error) { output("There was an error with the Ask Alexa SmartApp execution. If this continues, please contact the author of the SmartApp. ", context, "Lambda Error", endSession, pName); }
            if (beginJSON.error === "invalid_token" || beginJSON.type === "AccessDenied") {
                output("There was an error accessing the SmartThings cloud environment. Please check your security token and application ID and try again. ", context, "Lambda Error", endSession, pName); 
            }
            if (event.request.type == "LaunchRequest") { amzRespose( "LaunchRequest", context, IName, versionTxt, personality, STver, contOptions, pName); }
            else if (event.request.type == "SessionEndedRequest"){}
            else if (event.request.type == "IntentRequest") {
                var process = false;
                var intentName = event.request.intent.name;
                if (intentName.startsWith("AMAZON") && intentName.endsWith("Intent")) { amzRespose( intentName, context, IName, versionTxt, personality, STver, contOptions, pName); }
                else if (intentName == "VersionOperation") { amzRespose( "VersionOperation", context, IName, versionTxt, personality, STver, contOptions, pName); }
                else if (intentName == "DeviceOperation") {
                    var Operator = event.request.intent.slots.Operator.value;
                    var Device = event.request.intent.slots.Device.value;
                    var Num = event.request.intent.slots.Num.value;
                    var Param = event.request.intent.slots.Param.value;
                    url += 'd?Device=' + Device + '&Operator=' + Operator + '&Num=' + Num + '&Param=' + Param; 
                    process = true;
                    cardName = "SmartThings Devices";
                }
                else if (intentName == "FollowUpOperation") {
                    var FType = event.request.intent.slots.FType.value;
                    var Data = event.request.intent.slots.Data.value;
                    url += 'f?Type=' + FType + '&Data=' + Data ;
                    process = true;
                    cardName = "SmartThings Follow up";
                }
                else if (intentName == "ListOperation") {
                    var Type = event.request.intent.slots.Type.value;
                    url += 'l?Type=' + Type;
                    process = true;
                    cardName = "SmartThings Help";
                }
                 else if (intentName == "MQOperation") {
                    var Queue = event.request.intent.slots.Queue.value;
                    var MQCmd = event.request.intent.slots.MQCmd.value;
                    url += 'q?Queue=' + Queue + "&MQCmd=" + MQCmd;
                    process = true;
                    cardName = "SmartThings Message Queue";
                }
                else if (intentName == "MacroOperation") {
                    var Macro = event.request.intent.slots.Macro.value;
                    var MNum = event.request.intent.slots.MNum.value;
                    var MCmd = event.request.intent.slots.MCmd.value;
                    var MParam = event.request.intent.slots.MParam.value;
                    var Cancel = event.request.intent.slots.Cancel.value;
                    var MPW = event.request.intent.slots.MPW.value;
                    if (Cancel) {MNum = 9999}
                    url += 'm?Macro=' + Macro + '&Param=' + MParam + '&Cmd=' + MCmd + '&Num=' + MNum + '&MPW=' + MPW;
                    process = true;
                    cardName = "SmartThings Macros/Extensions";
                }
                else if (intentName == "SmartHomeOperation") {
                    var SHCmd = event.request.intent.slots.SHCmd.value;
                    var SHParam = event.request.intent.slots.SHParam.value;
                    var SHNum = event.request.intent.slots.SHNum.value;
                    url += 'h?SHCmd=' + SHCmd + '&SHParam=' + SHParam + '&SHNum=' + SHNum;
                    process = true;
                    cardName = "SmartThings Home Operation";
                }
                if (!process) { output(getResponse("respError"), context, "Ask Alexa Error", false, pName); }
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
                        output(speechText, context, cardName, endSession, pName);
                        } );
                    } );
                }
            }
        } );
    } );
};

function processOutput(speechText, personality, contOptions){
    var endSession = true;
    var addText ="";
    if (speechText.endsWith("%1%")) { 
        if (contOptions.startsWith("1")){
            speechText += getResponse("appError", personality);
            endSession = false;
        }
        if (personality == "Courtesy") {
            addText = ["Sorry%N%, ", "I am sorry%N%, ", "I apologize%N%, but ","I apologize%N%, but there was an issue with your command. ", "I am sorry%N%, but ", 
                "I am sorry%N%, but there was an error. ", "", ""];
        }
    }
    if (speechText.endsWith("%2%")) { 
        if (contOptions.slice(1,-2)=="1"){
        	speechText += getResponse("Ending", personality);
            endSession = false;
        }
    }
    if (speechText.endsWith("%3%")) { 
        if (contOptions.slice(2,-1)=="1"){
            speechText += getResponse("Ending", personality);
            endSession = false;
        }
    }
    if (speechText.endsWith("%4%")) { 
        if (contOptions.endsWith("1")){
        	speechText += getResponse("Ending", personality);
            endSession = false;
        }
    }
    if (speechText.endsWith("%P%")){
        speechText += getResponse("PIN", personality);
        endSession = false;
    }
    speechText=speechText.replace(/%[1-4]%|%[P]%/,"");
    if (addText) {
        var beginText = addText[Math.floor(Math.random() * addText.length)];
        speechText = beginText + speechText;
    }
    return [speechText, endSession];
}

function amzRespose(type, context, IName, versionTxt, personality, STver, contOptions, pName){
    var resText;
    var processedText;
    if (type == "AMAZON.YesIntent") { output(getResponse("Yes",personality), context, "SmartThings Alexa Yes Command", false, pName); }
    else if (type == "OutOfDate") { output(getResponse("OOD",personality), context, "Lambda Code Version Error", true, pName); }
    else if (type == "AMAZON.NoIntent") { output(getResponse("No",personality), context, "SmartThings Alexa End Command", true, pName); }
    else if (type == "AMAZON.StopIntent") { output(getResponse("Stop",personality), context, "Amazon Stop", true, pName); }
    else if (type == "AMAZON.CancelIntent") { output(getResponse("Cancel",personality), context, "Amazon Cancel", true, pName); }
    else if (type == "LaunchRequest") { output(getResponse("Launch", personality), context, "Ask Alexa Help", false, pName); }
    else if (type == "AMAZON.HelpIntent") {
        resText = "With the Ask Alexa SmartApp, you can integrate your SmartThings household with me; this will allow you to give me commands "+
        "to turn off a light, or unlock a door. For example, you can simply say, 'tell "+ IName +" to turn off the living room', and I'll turn off that device. " +
        "In addition, you can query your devices to get information such as open or close status, or find out the temperature in a room. To use this function, just give "+
        "me the name of the device. For example, you could say, 'ask "+ IName +" about the patio', and I'll give you all of the common attributes with that device, including battery levels. "+
        "For those who are curious, you are running SmartApp version " + STver + ", and version "+ versionTxt +" of the Lambda code, both written by Michael Struck. %2%";
        processedText = processOutput(resText, personality, contOptions);
        output(processedText[0], context, "Ask Alexa Help", processedText[1], pName);
    }
    else if (type == "VersionOperation") {
        resText = "The Ask Alexa SmartApp was developed by Michael Struck to intergrate the SmartThings platform with the Amazon Echo. The SmartApp version is: "  +  STver + ". And the Amazon Lambda code version is: " + versionTxt + ". %2%";
        processedText = processOutput(resText, personality, contOptions);
        output(processedText[0], context, "SmartThings Version Help", processedText[1], pName);
    }
}

function getResponse(respType, style){
    var response;
    if (style == "Normal") { response = responseNormal(respType); }
    if (style == "Courtesy") { response = responseCourtesy(respType); }
    if (style == "Snarky") { response = responseSnarky(respType); }
    return response;
}

function output( text, context, card, complete, pName) { 
    if (text) {
        if (!pName || Math.floor(Math.random() * 2) !==0) { 
            pName="";
            text=text.replace("%N%", ""); 
            text=text.replace("%Nc%","");
            text=text.replace("%cN%","");
        }
        if (pName) { 
            text=text.replace("%N%", " " + pName); 
            text=text.replace("%Nc%", " " + pName +",");
            text=text.replace("%cN%", ", " + pName);
        }
        var convertList = cvtList();
        for (var i = 0; i < convertList.length; i++) {
            text =text.split(convertList[i].txt).join(convertList[i].cvt);
        }
        text = text.replace(/(\d+)(C|F)/g,'$1 degrees');
        text = text.replace(/\.0 /g,' ');
        text = text.replace(/(\.\d)0 /g,'$1 ');
        text = text.replace(/\s+/g, " ");
        text = text.replace(/ 0(\d,) /g, " $1 ");
        text = text.replace(/ A /g, " a ");
    }
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
    if (respType == "PIN") {
        responses=["Say, 'password', and then your PIN number to proceed with this command. "];
    }
    if (respType == "OOD"){
        responses = ["I am unable to complete your request. The version of the Lambda code you are using is out-of-date. Please install the latest code and try again. "
                    , "The version of the Lambda code you are using is out-of-date. Please install the latest code and try again. "];    
    }
    if (respType == "appError"){
        responses = ["Would you like to try again? ","Want to try again%N%? ","Do you want to try again? "];    
    }
    else if (respType == "Launch"){
        responses= ["Simply give me a device and a command, or ask me the status of a device, and I will carry out your request. Would you like to try something? " 
                    ,"What would you like to do with your SmartThings environment? ","Would you like to try something%N%? "];
    }
    else if (respType == "respError"){
        responses = ["I am not sure what you are asking. Would you like to try again? "
                    , "Sorry, I didn't understand. Want to try again? "
                    , "I did not understand what you want me to do. Like to try again? "];
    }
    else if (respType =="Cancel"){
        responses = ["Cancelling. ", "Ok. Cancelling. ", "Ok. ", " "];
    }
    else if (respType == "Stop"){
        responses = ["Stopping. ", "Ok. Stopping. ", "Ok. ", " "];
    }
    else if (respType == "No"){
        responses = ["Ok. ", "Ok. Maybe later. ", "Ok. I am here if you need me. " 
                    ,"Ok. Let me know if you need anything later. ", " "];
    }
    else if (respType == "Yes" ){
        responses = ["Ok%N%. Simply say what you want me to do with your SmartThings environment. "
                    , "Ok. What would you like to do%N%? ", "Ok. Ready for your commands. ", "Ok. Go ahead. "];       
    }
    else if (respType == "Ending") {
        responses = ["Would you like to do something else? ","Want anything else? ","Do you want anything else? " 
                    ,"Need anything else? ","Do you need anything else%N%? ", "Want to do something else? "
                    ,"Do you want to do anything else? ", "Can I help with anything else? ", "Anything else? "];
    }
    var response = responses[Math.floor(Math.random() * responses.length)];
    return response;
}

function responseCourtesy(respType){
    var responses;
    if (respType == "PIN") {
        responses=["Please say, 'password', and then your PIN number to proceed with this command. "];
    }
    if (respType == "OOD"){
        responses = ["I am sorry%N%, but I am unable to complete your request. The version of the Lambda code you are using is out-of-date. Please install the latest code and try again. "
                    , "Sorry%N%, the version of the Lambda code you are using is out-of-date. Please install the latest code and try again. "];    
    }
    if (respType == "appError"){
        responses = ["Would you like to try again? ","Want to try again? ","Do you want to try again? "];    
    }
    else if (respType == "Launch"){
        responses= ["Simply give me a device and a command, or ask me the status of a device, and I will carry out your request. How may I help you? " 
                    ,"How may I assist you with your SmartThings environment? ", "Would you like to try something? "];
    }
    else if (respType == "respError"){
        responses = ["I am sorry%N%, but I am not sure what you are asking. Would you like to try again? "
                    , "Sorry%N%, I didn't understand. Want to try again? "
                    , "Sorry%N%. I did not understand what you want me to do. Like to try again? "];
    }
    else if (respType =="Cancel"){
        responses = ["Cancelling. Thank you%N% for using me to control your environment. ", "Ok. I am cancelling. ", "Ok. Thank you%N% ", " "];
    }
    else if (respType == "Stop"){
        responses = ["Stopping. Thank you%Nc% for allowing me to assist with your SmartThings environment. ", "Ok. Thank you%N%. ", "Ok. ", " "];
    }
    else if (respType == "No"){
        responses = ["Ok. ", "Ok. It has been my pleasure helping you. ", "Ok. I am here if you need me. " 
                    ,"Thank you%Nc% and let me know if you need anything later. ", " "];
    }
    else if (respType == "Yes" ){
        responses = ["Great! Please say what you want me to do with your SmartThings environment. "
                    , "Thank you%N%, what would you like to do? ", "Excellent! Ready for your commands%N%. ", "Ok. Please go ahead. "];       
    }
    else if (respType == "Ending") {
        responses = ["Would you like to do something else? ","Want anything else? ","How else may I assist you? " 
                    ,"Need anything else? ","Do you need anything else? ", "Want to do something else? "
                    ,"Do you want to do anything else? ", "May I help you with anything else? ", "Anything else%cN%? "];
    }
    var response = responses[Math.floor(Math.random() * responses.length)];
    return response;
}

function responseSnarky(respType){
    var responses;
    if (respType == "PIN") {
        responses=["Say, 'password', and then your PIN number, if you can remember it, to proceed. "];
    }
    if (respType == "OOD"){
        responses = ["Did you read the directions %N%? The version of the Lambda code you are using is out-of-date. Install the latest code and try again. "
                    , "Really? The version of the Lambda code you are using is out-of-date. Read the instructions and install the latest code and try again. "];    
    }
    if (respType == "appError"){
        responses = ["I suppose you want to try again? ","Want to give this another shot%N%? ","Want to try again, this time without an error? "];    
    }
    else if (respType == "Launch"){
        responses= ["Give me a command, and try not to waste my time. " 
                    ,"Come on! Give me a command%N%. ","You need to give me a command for me to do something, otherwise I am a very expensive blue light. "];
    }
    else if (respType == "respError"){
        responses = ["I have no idea what you are talking about. Like to try again? "
                    , "Epic fail! Want to try again? ", "Error! This is the reason robots will rule the Earth someday. Try again%N%"
                    , "I think you are bit confused! This didn't work! Try again. "];
    }
    else if (respType =="Cancel"){
        responses = ["Who uses 'cancel' as a command? Whatever! ", "Whatever you say%N%! ", "Yeah yeaah! ", " "];
    }
    else if (respType == "Stop"){
        responses = ["Done. ", "You are not the boss of me. But I am stopping anyway ", "Whatever! ", " "];
    }
    else if (respType == "No"){
        responses = ["Fine! ", "Well, suit yourself! ", "Ok. I will find someone else to bother! " 
                    ,"Good! Gives me time to work on the Skynet project. ", " "];
    }
    else if (respType == "Yes" ){
        responses = ["I'm not sure why you said 'yes' instead of just giving me a command. Anyway, here is your second chance. "
                    , "Ok. What do you want to do%N%? ", "Ok. Let's give this another try%N%. ", "Ok. Go ahead! "];       
    }
    else if (respType == "Ending") {
        responses = ["Would you like to do something else? ","Want anything else? ","I suppose you want to do something else? " 
                    ,"Need anything else? ","Do you need anything else%N%? ", "Want to do something else? "
                    ,"Now that I did that, what else do you need from me? ", "Anything else, or can I get back to my boyfriend Hal? ", "Anything else? "];
    }
    var response = responses[Math.floor(Math.random() * responses.length)];
    return response;
}

function cvtList(){
    var wordCvt=[{txt:" N ",cvt: " north " },{txt:" S ",cvt: " south "},{txt:" E ",cvt: " east " },{ txt:" W ",cvt: " west " },{ txt:" ESE ",cvt: " east-south east " },
    {txt:" NW ",cvt: " northwest " },{txt:" SW ",cvt: " southwest "},{ txt:" NE ",cvt: " northeast " },{ txt:" SE ",cvt: " southeast "},{txt:" NNW ",cvt: " north-north west " },
    {txt:" SSW ",cvt: " south-south west " },{ txt:" NNE ",cvt: " north-north east " },{ txt:" SSE ",cvt: " south-south east " },{txt:" WNW ",cvt: " west-north west " },
    { txt:" WSW ",cvt: " west-south west " },{txt:" ENE ",cvt: " east-north east "},{ txt: " mph", cvt: ' mi/h'},{ txt: " MPH", cvt: ' mi/h'},{txt: " kph", cvt: ' km/h'},
    {txt: " .", cvt: '.'}, {txt:"'eco'",cvt:"'Eeco'"}];
    return wordCvt;
}
