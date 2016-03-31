
 var APP = {

  init: function() {
    ST.request("getInitialData").success(function(data) {
      APP.log("initializing...");
    APP.log(data);
        
        APP.render();
        APP.addBindings(data.auth, data.displayUnit, data.language, data.deviceState, data.deviceId);
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

  addBindings: function(auth, displayUnit, bilingual, deviceState, dId) {
    APP.log(window.navigator.language)
    APP.log("In addBindings");
    
    var UI = new Encored.UI({env:"development"});
    
        $("#content1").click(function(){
        $("#real-time").slideUp("slow");
        $("#this-month").slideDown("slow");

        UI.renderCard({
          cards:
          [{
            "id": "ui:h:thismonth:v1",
            "params": 
            {
              "lang": bilingual,
              "useDemoLabel": 1,
              "displayUnit": displayUnit
            }
          },
          {
            "id": "ui:h:thismonthchart:v1",
            "params": 
            {
              "lang": bilingual,
              "useDemoLabel": 1,
              "displayUnit": displayUnit
            }
          }],
          accessToken: auth,
          target: document.querySelector('#my-card2')
        });
        $(window).scrollTop(0);
      });
      
      $("#show").click(function(){
        $("#real-time").slideDown("slow");
        $("#this-month").slideUp("slow");
      });

      $("#content2").click(function(){
        $("#real-time").slideUp("slow");
        $("#progressive-step").slideDown("slow");

        UI.renderCard({
          cards: 
          [{
            "id": "ui:h:stepchart:v1",
            "params": {
              "lang": bilingual,
              "useDemoLabel": 1
            }
          }],
          accessToken: auth,
          target: document.querySelector('#my-card5')
        });
        $(window).scrollTop(0);
      });
      
      $("#show3").click(function(){
        $("#real-time").slideDown("slow");
        $("#progressive-step").slideUp("slow")
      });

      $("#content3").click(function(){
        $("#real-time").slideUp("slow");
        $("#plan").slideDown("slow");

        UI.renderCard({
          cards: 
          [{
            "id": "ui:h:planmain:v1",
            "params": {
              "lang": bilingual,
              "useDemoLabel": 1,
              "displayUnit": displayUnit,
              "disableChangeButton": 1
            }
          },
          {
            "id": "ui:h:planaddition:v1",
            "params": 
             {
               "lang": bilingual,
               "useDemoLabel": 1,
               "displayUnit": displayUnit
             }
          }],
          accessToken: auth,
          target: document.querySelector('#my-card7')
        });
        $(window).scrollTop(0);
      });
      
      $("#show5").click(function(){
        $("#real-time").slideDown("slow");
        $("#plan").slideUp("slow")
      });

      $("#content4").click(function(){
        $("#real-time").slideUp("slow");
        $("#last-month").slideDown("slow");

        UI.renderCard({
          cards: 
          [{
            "id": "ui:h:usagekeepingbook:v1",
            "params": 
            {
              "lang": bilingual,
              "useDemoLabel": 1,
              "displayUnit": displayUnit
            }
          }],
          accessToken: auth,
          target: document.querySelector('#my-card4')
        });
        $(window).scrollTop(0);
      });
      
      $("#show2").click(function(){
        $("#real-time").slideDown("slow");
        $("#last-month").slideUp("slow");
      });
      
      $("#content5").click(function(){
        $("#real-time").slideUp("slow");
        $("#ranking").slideDown("slow");

        UI.renderCard({
          cards: 
          [{
            "id": "ui:h:overview:v1",
            "params": 
            {
              "lang": bilingual,
              "useDemoLabel": 1,
              "showGuide": 1
            }
          }],
          accessToken: auth,
          target: document.querySelector('#my-card6')
        });
        $(window).scrollTop(0);
      });
      
      $("#show4").click(function(){
        $("#real-time").slideDown("slow");
        $("#ranking").slideUp("slow");
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
              "lang": bilingual,
              "useDemoLabel": 1
            }
          }],
          accessToken: auth,
          target: document.querySelector('#my-card8')
        });
        $(window).scrollTop(0);
      });
      
      $("#show6").click(function(){
        $("#real-time").slideDown("slow");
        $("#standby").slideUp("slow");
      });

      if (!deviceState) { 
        var statusElement = document.querySelector(".circle");
        statusElement.style.backgroundColor = "#D93600";
        $("#value-ON-OFF").html("OFF")
      }
      
      $("#content7").on("click", function() {
      // when the device details is clicked, we want to take
      // the user to the device details view.
      // We get the device-id out of the element, and use it
      // with ST.loadDevice().
      APP.log("will try and go to device details with id: " + dId);
      ST.loadDevice(dId).error(function(error) {
        APP.log("error loading device: " + error);
      }).EXECUTE();
    });
    
    $("show").click(function() {
        $(".show").style.color = "#4C4C4E";
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
  $(this).scrollTop(0);
  APP.log("initial");
  APP.init();
});