/**
 *  Ask Alexa - Lambda Code
 *
 *  Version 1.1.2 - 6/9/16 Copyright © 2016 Michael Struck
 *  Special thanks for Keith DeLong for code and assistance
 *  
 *  Version 1.0.0 - Initial release
 *  Version 1.0.1 - Removed dedicated status operation; added version code
 *  Version 1.1.0 - Added two addition intent types for groups and macros
 *  Version 1.1.1 - Added messages to indicate bad OAuth or Application ID
 *  Version 1.1.2 - Fixed small syntax error in a couple responses
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
   var versionTxt = '1.1.2';
   var versionDate= '06/09/2016';
   var versionNum = '112';
   var https = require( 'https' );
   // Paste app code here between the breaks------------------------------------------------
    var IName = 'SmartThings';
    var STappID = '';
    var STtoken = '';
    var url='https://graph.api.smartthings.com:443/api/smartapps/installations/' + STappID + '/' ;
   //---------------------------------------------------------------------------------------
   var cardName ="";
   if (event.request.type == "LaunchRequest") {
        var speech = "Simply give me a device and a command, or ask me the status of a device, and I will carry out your request.";
        cardName = "Ask Alexa Help";
        output(speech, context, cardName);
   }
   else if (event.request.type === "SessionEndedRequest"){
   }
   else if (event.request.type == "IntentRequest") {
        var process = false;
        var intentName = event.request.intent.name;
        if (intentName == "DeviceOperation") {
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
        else if (intentName == "VersionOperation") {
            url += 'v?Ver=' + versionTxt + '&Date=' + versionDate;
            process = true;
            cardName = "SmartThings Version Help";
        }
        else if (intentName == "SmartHomeOperation") {
            var SHCmd = event.request.intent.slots.SHCmd.value;
            var SHParam = event.request.intent.slots.SHParam.value;
            url += 'h?SHCmd=' + SHCmd + '&SHParam=' + SHParam;
            process = true;
            cardName = "SmartThings Home Operation";
        }
        else if (intentName == "AMAZON.HelpIntent") {
            var help = "With the Ask Alexa SmartApp, you can interface your "+
            "SmartThings household with me. This will allow you to give me commands "+
            "to turn off a light, or unlock a door. As an example you can simply say, "+
            "'tell "+ IName +" to turn off the living room', and I'll turn off that device. " +
            "In addition, you can query your devices to get information such as open or "+
            "close status, or find out the temperature in a room. To use this function, just give "+
            "me the device name. For example, you could say, 'ask "+ IName +" about the patio'. and I will "+
            "give you all of the common attributes I find with that device, including battery levels. "+
            "For those who are curious, this is version" + versionTxt +" of the Lambda code, written by Michael Struck. ";
            output(help, context, "Ask Alexa Help");
        }
        if (!process) {
            output("I am not sure what you are asking. Please try again", context, "Ask Alexa Error");   
        }
        else {
            url += '&lVer=' + versionNum + '&access_token=' + STtoken;
            https.get( url, function( response ) {
                response.on( 'data', function( data ) {
                var resJSON = JSON.parse(data);
                var speechText;
                if (resJSON.voiceOutput) { speechText = resJSON.voiceOutput; }
                if (resJSON.error) speechText = "There was an error with the Ask Alexa SmartApp execution. If this continues, please contact the author of the SmartApp. ";
                if (resJSON.error === "invalid_token" || resJSON.type === "AccessDenied") {
                    speechText = "There was an error accessing the SmartThings cloud environment. Please check your security token and application ID and try again. ";
                }
                console.log(speechText);
                output(speechText, context, cardName);
                } );
            } );
        }
    }
};

function output( text, context, card ) { 
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
   shouldEndSession: true
   };
   context.succeed( { response: response } );
}
