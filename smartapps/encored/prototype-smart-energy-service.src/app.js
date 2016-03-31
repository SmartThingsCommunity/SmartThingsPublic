var APP = {

  init: function() {
     APP.log("initializing...");
    ST.request("getInitialData")
    .success(function(data) {
      APP.log("data from getInitialData:" );
          APP.render();
          APP.addBindings(data.auth);
    }).GET();
  },

  /**
  *Toggle cards down
  */
  render: function() {
      APP.log("in render, state");
    $("#this-month").slideUp();
        $("#last-month").slideUp();
        $("#progressive-step").slideUp();
        $("#ranking").slideUp();
        $("#plan").slideUp();
        $("#standby").slideUp();
  },

  addBindings: function(auth) {
    APP.log(window.navigator.language)
    APP.log("In addBindings");
    var UI = new Encored.UI();
    UI.renderCard({
        cards: 
        [{
          "id": "ui:h:realtime:v2",
          "params": 
          {
            "lang": "en",
            "useDemoLabel": 1,
            "displayUnit": "watt"
          }
        }],
        accessToken: auth,
        target: document.querySelector('#my-card')
      });
        $("#content1").click(function(){
        $("#real-time").slideUp("slow");
        $("#this-month").slideDown("slow");
        UI.renderCard({
          cards:
          [{
            "id": "ui:h:thismonth:v1",
            "params": 
            {
              "lang": "en",
              "useDemoLabel": 1,
              "displayUnit": "watt"
            }
          },
          {
            "id": "ui:h:thismonthchart:v1",
            "params": 
            {
              "lang": "en",
              "useDemoLabel": 1,
              "displayUnit": "watt"
            }
          }],
          accessToken: auth,
          target: document.querySelector('#my-card2')
        });
      });
      
      $("#show").click(function(){
        $("#real-time").slideDown("slow");
        $("#this-month").slideUp("slow");
      });
      
      $("#content2").click(function(){
        $("#real-time").slideUp("slow");
        $("#last-month").slideDown("slow");
        UI.renderCard({
          cards: 
          [{
            "id": "ui:h:usagekeepingbook:v1",
            "params": 
            {
              "lang": "en",
              "useDemoLabel": 1,
              "displayUnit": "watt"
            }
          }],
          accessToken: auth,
          target: document.querySelector('#my-card4')
        });
      });
      
      $("#show2").click(function(){
        $("#real-time").slideDown("slow");
        $("#last-month").slideUp("slow");
      });
      
      $("#content3").click(function(){
        $("#real-time").slideUp("slow");
        $("#progressive-step").slideDown("slow");
        UI.renderCard({
          cards: 
          [{
            "id": "ui:h:stepchart:v1",
            "params": {
              "lang": "en",
              "useDemoLabel": 1
            }
          }],
          accessToken: auth,
          target: document.querySelector('#my-card5')
        });
      });
      
      $("#show3").click(function(){
        $("#real-time").slideDown("slow");
        $("#progressive-step").slideUp("slow")
      });
      
      $("#content4").click(function(){
        $("#real-time").slideUp("slow");
        $("#ranking").slideDown("slow");
        UI.renderCard({
          cards: 
          [{
            "id": "ui:h:overview:v1",
            "params": 
            {
              "lang": "en",
              "useDemoLabel": 1,
              "showGuide": 1
            }
          }],
          accessToken: auth,
          target: document.querySelector('#my-card6')
        });
      });
      
      $("#show4").click(function(){
        $("#real-time").slideDown("slow");
        $("#ranking").slideUp("slow");
      });
      
      $("#content5").click(function(){
        $("#real-time").slideUp("slow");
        $("#plan").slideDown("slow");
        UI.renderCard({
          cards: 
          [{
            "id": "ui:h:planmain:v1",
            "params": {
              "lang": "ko",
              "useDemoLabel": 1,
              "displayUnit": "watt",
              "disableChangeButton": 0
            }
          },
          {
            "id": "ui:h:planaddition:v1",
            "params": 
             {
               "lang": "en",
               "useDemoLabel": 1,
               "displayUnit": "watt"
             }
          }],
          accessToken: auth,
          target: document.querySelector('#my-card7')
        });
      });
      
      $("#show5").click(function(){
        $("#real-time").slideDown("slow");
        $("#plan").slideUp("slow")
      });
      
      $("#content6").click(function(){
        $("#real-time").slideUp("slow");
        $("#standby").slideDown("slow");
        UI.renderCard({
          cards: 
          [{
            "id": "ui:h:standbychart:v1",
            "params": 
            {
              "lang": "en",
              "useDemoLabel": 1
            }
          }],
          accessToken: auth,
          target: document.querySelector('#my-card8')
        });
      });
      
      $("#show6").click(function(){
        $("#real-time").slideDown("slow");
        $("#standby").slideUp("slow");
      });
  },
    log: function(str) {
    // If we're passed an object, try to stringify it for better logging
    if (typeof(str) === 'object') {
      str = JSON.stringify(str);
    }
    // Make a POST request to the /consoleLog endpoint in our SmartApp,
    // with the message we want to log as the data on the request.
    ST.request("consoleLog").data({str: str}).POST();
  }
  
}

$('document').ready(function() {
  APP.log("initial");
    APP.init();
});