// jquery functions to do Ajax on housemap.php

// the main jquery setup function that is called initially
// this is now dynamically created within the php file
//$(document).ready(function(event){
//
//    // activate click for all the up/down buttons for quantity
//    // setupButtons();
//    // setupMotions();
//    setupPage("kitchen");
//    setupPage("living");
//    setupPage("garage");
//    setupPage("office");
//    setupPage("bedroom");
//
//    // activate tabs
//    $( "#tabs" ).tabs();
//
//});

// this hides address bar
function hideAddressBar()
{
  if(!window.location.hash)
  {
      if(document.height < window.outerHeight)
      {
          document.body.style.height = (window.outerHeight + 50) + 'px';
      }
      setTimeout( function(){ window.scrollTo(0, 1); }, 50 );
  }
}

window.addEventListener("load", function(){ if(!window.pageYOffset){ hideAddressBar(); } } );
window.addEventListener("orientationchange", hideAddressBar );

var setupPage = function(sensortype) {

    // setup trigger for clicking on data load portion
    var jqdata = "#" + sensortype + "-data";
    var jqflag = "table." + sensortype + " " + "td[name^=status-]";
    $(jqflag).click(function() {
        var theid= $(this).attr("id");
        
        // parse out the switch id number
        var bid = theid.substr(2);
        
    	var thename = encodeURIComponent($(this).html());
        var typename = 'type-' + bid;
        var thetype = $('input[type="hidden"][name='+typename+']');

        // $("td[name^=contact-]").attr("class","sensoroff");
        $(jqflag).removeClass("sensorpick");
    	$(this).addClass("sensorpick");

        // alert(" sensor name = " + thename + "\n sensor type = " + thetype.val() + "\n id = " + bid );
    	
    	// load the history
    	$(jqdata).load("housemap5.php",
            {id: bid, sensorajax: "1", value: thename, type: thetype.val()});
    });
        
    // setup trigger for clicking on the action portion of this thing
    var actionid = "table." + sensortype + " " + "td[name^=action-]";
    $(actionid).click(function() {

        var theid = $(this).attr("id");
        var bid = theid.substr(2);
        
    	var thename = encodeURIComponent($(this).html());
        var typename = 'type-' + bid;
        var thetype = $('input[type="hidden"][name='+typename+']');
        var theclass = $(this).attr("class");
        var idindex = theclass.indexOf('-');
        var classroot = theclass.substr(0,idindex);
        var classtail = theclass.substr(idindex+1);
        
        // $("#s-"+bid).addClass("sensorpick");

        // these two lines enable a delayed button press effect
        // the parameters are enclosed within the this of the classarray
        // and the jquery $(this) is passed as first array argument
        var classarray = [$(this), theclass];
        classarray.myMethod = function() {this[0].attr("class", this[1]);};
        
        var theval = $(this).html();
        var theattr = sensortype;
        
        if (classtail=="on") {
            theclass = classroot + "-off";
        } else {
            theclass = classroot + "-on";
        }
        
        if (thetype.val()=="momentary") {
            theval = "push";
            $.post("housemap.php", 
                {useajax: "1", id: bid, attr: theattr, value: theval, type: thetype.val()});
            $(this).attr("class",theclass);
            setTimeout(function(){classarray.myMethod();}, 1000);
        } else {
            $(this).load("housemap.php", 
                {useajax: "1", id: bid, attr: theattr, value: theval, type: thetype.val()});
            $(this).attr("class",theclass);
        }
                            
    });
    
   
};


