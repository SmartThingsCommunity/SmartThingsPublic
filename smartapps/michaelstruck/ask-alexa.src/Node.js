/**
 * ---------------------------------------------------------------
 * Instructions:
 * Simply copy this page of code to your AWS account
 * ---------------------------------------------------------------
 * 
 *  Ask Alexa - Lambda Code 
 * 
 *  Version 1.3.1 - 3/13/18 Copyright © 2018 Michael Struck 
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
 *  Version 1.2.6 - Added icon to skill's display card for Show device 
 *  Version 1.2.7 - Added in option for 'whisper' mode, changed structure to SSML output. Allow for speed/pitch adjustments 
 *  Version 1.2.8 - Added dynamic icons for skill's card. Ready for the Amazon Show! 
 *  Version 1.2.9 - Added advanced featured to WebCoRE macros, added variables for whipser and emphasis, updated brief replies 
 *  Version 1.3.0 - Added compound commands 
 *  Version 1.3.1 - Added device indentification and enhanced mute/disable options throughout the skill
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
    var versionTxt = '1.3.1'; 
    var versionDate= '03/13/2018'; 
    var versionNum = '131'; 
    var https = require( 'https' ); 
    // Paste app code here between the breaks------------------------------------------------ 
    var STappID = '';
    var STtoken = '';
    var url=''; 
    //--------------------------------------------------------------------------------------- 
    var cardName =""; 
    var endSession = true; 
    var processedText; 
    var icon;
    var echoDev = event.context.System.device.deviceId;
    //Get SmartThings parameters 
    var beginURL = url + 'b?Ver=' + versionTxt + '&Date=' + versionDate + '&lVer=' + versionNum + '&access_token=' + STtoken+ "&echoID="+ echoDev ;
    https.get( beginURL, function( response ) { 
        response.on( 'data', function( data ) { 
            var beginJSON = JSON.parse(data); 
            var contOptions = beginJSON.continue;  
            var personality = beginJSON.personality; 
            var STver = beginJSON.SmartAppVer; 
            var IName = beginJSON.IName; 
            var pName = beginJSON.pName; 
            var whisper = beginJSON.whisper; 
            var speed = beginJSON.speed; 
            var pitch = beginJSON.pitch; 
            var mute = beginJSON.mute;
            var disabled = beginJSON.disabled; 
            if (beginJSON.OOD) { amzRespose("OutOfDate", context, IName, versionTxt, personality, STver, contOptions, pName, whisper, speed, pitch, mute, disabled); } 
            if (beginJSON.error) { 
                icon ="warning"; 
                output("There was an error with the Ask Alexa SmartApp execution. If this continues, please contact the author of the SmartApp. ", context, "Lambda Error", endSession, pName, whisper, speed, pitch, icon, mute, disabled); 
            } 
            if (beginJSON.error === "invalid_token" || beginJSON.type === "AccessDenied") { 
                icon ="warning"; 
                output("There was an error accessing the SmartThings cloud environment. Please check your security token and application ID and try again. ", context, "Lambda Error", endSession, pName, whisper, speed, pitch, icon, mute, disabled); 
            } 
            if (event.request.type == "LaunchRequest") { amzRespose( "LaunchRequest", context, IName, versionTxt, personality, STver, contOptions, pName, whisper, speed, pitch, mute, disabled); } 
            else if (event.request.type == "SessionEndedRequest"){} 
            else if (event.request.type == "IntentRequest") { 
                var process = false; 
                var intentName = event.request.intent.name;
                if (intentName.startsWith("AMAZON") && intentName.endsWith("Intent")) { amzRespose( intentName, context, IName, versionTxt, personality, STver, contOptions, pName, whisper, speed, pitch, mute, disabled); } 
                else if (intentName == "VersionOperation") { amzRespose( "VersionOperation", context, IName, versionTxt, personality, STver, contOptions, pName, whisper, speed, pitch, mute, disabled); } 
                else if (intentName == "RoomOperation") { 
                    var RNum = event.request.intent.slots.MNum.value; 
                    var RCmd = event.request.intent.slots.MCmd.value; 
                    var RParam = event.request.intent.slots.MParam.value;
                    var RType = event.request.intent.slots.MType.value;
                    var RName = RCmd && RCmd.match(/associate|link|sync|setup/) ? event.request.intent.slots.MRoom.value : "here";
                    url += 'm?Macro=' + RName + '&Param=' + RParam + '&Cmd=' + RCmd + '&Num=' + RNum + "&Type=" + RType + "&echoID="+ echoDev ; 
                    process = true; 
                    cardName = "SmartThings Room Operation";  
                } 
                else if (intentName == "DeviceOperation") { 
                    var Operator = event.request.intent.slots.Operator.value; 
                    var Device = event.request.intent.slots.Device.value; 
                    var Num = event.request.intent.slots.Num.value; 
                    var Param = event.request.intent.slots.Param.value; 
                    url += 'd?Device=' +  Device  + '&Operator=' + Operator + '&Num=' + Num + '&Param=' + Param + "&echoID="+ echoDev ;   
                    process = true; 
                    cardName = "SmartThings Devices"; 
                } 
                else if (intentName == "ObjectOperation") { 
                    var Operator1 = event.request.intent.slots.OperatorA.value; 
                    var Object1 = event.request.intent.slots.DeviceA.value; 
                    var Ext1 = event.request.intent.slots.ExtA.value; 
                    var Num1 = event.request.intent.slots.NumA.value; 
                    var Param1 = event.request.intent.slots.ParamA.value; 
                    var Operator2 = event.request.intent.slots.OperatorB.value; 
                    var Object2 = event.request.intent.slots.DeviceB.value; 
                    var Ext2 = event.request.intent.slots.ExtB.value; 
                    var Num2 = event.request.intent.slots.NumB.value; 
                    var Param2 = event.request.intent.slots.ParamB.value;
                    var Type1 = event.request.intent.slots.OTypeA.value;
                    var Type2 = event.request.intent.slots.OTypeB.value;
                    if (!Object1) Object1=Ext1; 
                    if (!Object2) Object2=Ext2; 
                    url += 'o?Object1=' + Object1 + '&Operator1=' + Operator1 + '&Num1=' + Num1 + '&Param1=' + Param1 + '&Object2=' + Object2 + '&Operator2=' + Operator2 + '&Num2=' + Num2 + '&Param2=' + Param2 + "&Type1=" + Type1 + "&Type2=" + Type2 + "&echoID="+ echoDev ;  
                    process = true; 
                    cardName = "SmartThings Compound Command"; 
                } 
                else if (intentName == "FollowUpOperation") { 
                    var FType = event.request.intent.slots.FType.value; 
                    var Data = event.request.intent.slots.Data.value; 
                    url += 'f?Type=' + FType + '&Data=' + Data + "&echoID="+ echoDev ; 
                    process = true; 
                    cardName = "SmartThings Follow up"; 
                } 
                else if (intentName == "ListOperation") { 
                    var Type = event.request.intent.slots.Type.value; 
                    url += 'l?Type=' + Type + "&echoID="+ echoDev ; 
                    process = true; 
                    cardName = "SmartThings List Command"; 
                } 
                 else if (intentName == "MQOperation") { 
                    var Queue = event.request.intent.slots.Queue.value; 
                    var MQCmd = event.request.intent.slots.MQCmd.value; 
                    url += 'q?Queue=' + Queue + "&MQCmd=" + MQCmd + "&echoID="+ echoDev ; 
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
                    var MType = event.request.intent.slots.MType.value;
                    var xParam = event.request.intent.slots.xParam.value; 
                    if (Cancel) {MNum = 9999} 
                    url += 'm?Macro=' + Macro + '&Param=' + MParam + '&Cmd=' + MCmd + '&Type=' + MType + '&Num=' + MNum + '&MPW=' + MPW + "&xParam="+xParam + "&echoID="+ echoDev ; 
                    process = true; 
                    cardName = "SmartThings Macros/Extensions"; 
                } 
                else if (intentName == "SmartHomeOperation") { 
                    var SHCmd = event.request.intent.slots.SHCmd.value; 
                    var SHParam = event.request.intent.slots.SHParam.value; 
                    var SHNum = event.request.intent.slots.SHNum.value; 
                    url += 'h?SHCmd=' + SHCmd + '&SHParam=' + SHParam + '&SHNum=' + SHNum + "&echoID="+ echoDev ; 
                    process = true; 
                    cardName = "SmartThings Home Operation"; 
                } 
                if (!process) {  
                    icon="caution"; 
                    output(getResponse("respError"), context, "Application Error", false, pName, whisper, speed, pitch, icon, mute, disabled);  
                }
                else if (disabled){
                     output(" ", context, "Disabled", false, pName, whisper, speed, pitch, icon, mute, disabled);  
                }    
                else {
                        url += '&access_token=' + STtoken; 
                        https.get( url, function( response ) { 
                            response.on( 'data', function( data ) { 
                            var resJSON = JSON.parse(data); 
                            var speechText; 
                            icon = resJSON.icon; 
                            if (resJSON.voiceOutput) {  
                                speechText = resJSON.voiceOutput;  
                                if (speechText.endsWith("%")){ 
                                    processedText = processOutput(speechText, personality, contOptions); 
                                    speechText = processedText[0]; 
                                    endSession = processedText[1]; 
                                } 
                            }
                           
                        output(speechText, context, cardName, endSession, pName, whisper, speed, pitch, icon, mute, disabled); 
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
    if (speechText.endsWith("%7%") && contOptions.slice(2,-1)=="1") endSession = false; 
    if (speechText.endsWith("%X%") && contOptions.endsWith("1")) endSession = false; 
    if (speechText.endsWith("%P%")){ 
        speechText += getResponse("PIN", personality); 
        endSession = false; 
    } 
    speechText=speechText.replace(/%[1-8]%|%[P]%|%[X]%/,""); 
    if (addText) { 
        var beginText = addText[Math.floor(Math.random() * addText.length)]; 
        speechText = beginText + speechText; 
    } 
    return [speechText, endSession]; 
} 

function amzRespose(type, context, IName, versionTxt, personality, STver, contOptions, pName, whisper, speed, pitch, mute, disabled){ 
    var resText; 
    var processedText; 
    var icon; 
    if (type == "AMAZON.YesIntent") { 
        icon ="ok"; 
        output(getResponse("Yes",personality), context, "Yes Command", false, pName, whisper, speed, pitch, icon, mute, disabled);  
    } 
    else if (type == "OutOfDate") { 
        icon ="caution"; 
        output(getResponse("OOD",personality), context, "Lambda Code Version Error", true, pName, whisper, speed, pitch, icon, mute, disabled);  
    } 
    else if (type == "AMAZON.NoIntent") {  
        icon ="stop"; 
        output(getResponse("No",personality), context, "End Command", true, pName, whisper, speed, pitch, icon, mute, disabled);  
    } 
    else if (type == "AMAZON.StopIntent") {  
        icon ="stop"; 
        output(getResponse("Stop",personality), context, "Stop", true, pName, whisper, speed, pitch, icon, mute, disabled);  
    } 
    else if (type == "AMAZON.CancelIntent") { 
        icon ="stop"; 
        output(getResponse("Cancel",personality), context, "Cancel", true, pName, whisper, speed, pitch, icon, mute, disabled);  
    } 
    else if (type == "LaunchRequest") {  
        icon = "questionmark"; 
        output(getResponse("Launch", personality), context, "Help", false, pName, whisper, speed, pitch, icon, mute, disabled);  
    } 
    else if (type == "AMAZON.HelpIntent") { 
        resText = "With the Ask Alexa SmartApp, you can integrate your SmartThings household with me; this will allow you to give me commands "+ 
        "to turn off a light, or unlock a door. For example, you can simply say, 'tell "+ IName +" to turn off the living room', and I'll turn off that device. " + 
        "In addition, you can query your devices to get information such as open or close status, or find out the temperature in a room. To use this function, just give "+ 
        "me the name of the device. For example, you could say, 'ask "+ IName +" about the patio', and I'll give you all of the common attributes with that device, including battery levels. "+ 
        "For those who are curious, you are running SmartApp version " + STver + ", and version "+ versionTxt +" of the Lambda code, both written by Michael Struck. %2%"; 
        processedText = processOutput(resText, personality, contOptions); 
        icon = "info"; 
        output(processedText[0], context, "Help", processedText[1], pName, whisper, speed, pitch, icon, mute, disabled); 
    } 
    else if (type == "VersionOperation") { 
        resText = "The Ask Alexa SmartApp was developed by Michael Struck to intergrate the SmartThings platform with the Amazon Echo. The SmartApp version is: "  +  STver + ". And the Amazon Lambda code version is: " + versionTxt + ". %2%";
        processedText = processOutput(resText, personality, contOptions); 
        icon = "info"; 
        output(processedText[0], context, "Application Version Help", processedText[1], pName, whisper, speed, pitch, icon, mute, disabled); 
    } 
} 

function getResponse(respType, style){ 
    var response; 
    if (style == "Normal") { response = responseNormal(respType); } 
    if (style == "Courtesy") { response = responseCourtesy(respType); } 
    if (style == "Snarky") { response = responseSnarky(respType); } 
    return response; 
} 

function output( text, context, card, complete, pName, whisper, speed, pitch, icon, mute, disabled) {  
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
    var SSMLtext; 
    if (whisper) SSMLtext = "<amazon:effect name='whispered'>" + text + "</amazon:effect>"; 
    else { 
        if (!text) SSMLtext=" "; 
        else { 
            SSMLtext=text.replace(/<w>/g,"<amazon:effect name='whispered'>"); 
            SSMLtext=SSMLtext.replace(/<\/w>/g,"</amazon:effect>"); 
            SSMLtext=SSMLtext.replace(/<eH>/g,"<emphasis level='strong'>"); 
            SSMLtext=SSMLtext.replace(/<eL>/g,"<emphasis level='reduced'>"); 
            SSMLtext=SSMLtext.replace(/<\/e>/g,"</emphasis>"); 
        } 
    } 
    if (pitch !="medium") SSMLtext ="<prosody pitch='" + pitch + "'>" + SSMLtext +"</prosody>"; 
    if (speed !="medium") SSMLtext ="<prosody rate='" + speed + "'>" + SSMLtext +"</prosody>";
    var cardText = text ? text :"No output given"; 
    cardText=cardText.replace(/<w>|<\/w>|<eH>|<eL>|<\/e>/g,"");
    if (mute || disabled) {
            icon="mute";
            if (mute) cardText +="\n\n* Please note: verbal output has been disabled within the SmartApp";
            if (disabled) cardText ="Please note: Ask Alexa is disabled within the SmartApp. No action was taken.";
            SSMLtext = " ";
    }
    var imgURL = "https://raw.githubusercontent.com/MichaelStruck/SmartThingsPublic/master/img/AmazonICO/"; 
    if (!icon) icon = "AskAlexa"; 
    var response = { 
        outputSpeech: { 
            type: "SSML", 
            ssml: "<speak>" + SSMLtext + "</speak>" 
        }, 
        card: { 
            type: "Standard", 
            title: "Ask Alexa - " + card, 
            text: cardText, 
            image: { 
                largeImageUrl: imgURL + icon +".png" 
                } 
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
        responses = ["I am unable to complete your request. The version of the Lambda code you are using is out-of-sync with your Ask Alexa application. Please install the latest versions of the entire application and try again. " 
                    , "The version of the Lambda code you are using is incorrect. Please install the latest code and try again. "];     
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
        responses = ["I am sorry%N%, but I am unable to complete your request. The version of the Lambda code you are using is out-of-sync with your Ask Alexa application.  Please install the latest versions of the entire application and try again. "  
                    , "Sorry%N%, the version of the Lambda code you are using is incorrect. Please install the latest code and try again. "];     
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
        responses = ["Did you read the directions %N%? The version of the Lambda code you are using is out-of-sync with your Ask Alexa application.  Install the latest versions of the entire application and try again. " 
                    , "Really? The version of the Lambda code you are using is incorrect. Read the instructions, install the latest code and try again. "];     
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
