//prevent content slide from cycling
$('.carousel').carousel('pause'); 
/*
    Animation and Prompt handler
    @status current state of handshake protocal
    @prompt prompt of state
    @layout the UI nature of the prompt (based on status)
*/
function loader(status, prompt = null, layout = "alert-danger"){
    //keep loading screen until connection is made or max attempt is reached
    if (status) {
        $("body > .container").show();
        $(".modal-content").css("height","0px");
    } else {
        $(".modal-content").css("height","100vh");
        $("body > .container").hide();
        $(".prompt h5").addClass(layout);
        $(".prompt h5").html(prompt).delay(1500);
        $(".prompt").css("opacity","1");
    }
}
//update from feilds based on selected trasnaction type
$(".from-control-transaction-type button").on("click", (event) => {
    //prevent form from submiting
    event.preventDefault();
    //toggle controls
    if (event.currentTarget.value == "Deposit" || event.currentTarget.value == "Withdrawal") {
        if ($(".form-control-meternumber").css("display") != "none")
            $(".form-control-meternumber").hide(500);
        if ($(".form-control-paymentvendor").css("display") != "none")
            $(".form-control-paymentvendor").hide(500);
        $(".form-control-amount").show(500);
        
    }
    if (event.currentTarget.value == "Balance") {
        if ($(".form-control-amount").css("display") != "none")
            $(".form-control-amount").hide(500);
        if ($(".form-control-meternumber").css("display") != "none")
            $(".form-control-meternumber").hide(500);
        if ($(".form-control-paymentvendor").css("display") != "none")
            $(".form-control-paymentvendor").hide(500);
    } 
    if (event.currentTarget.value == "Payment") {
        $(".form-control-amount").show(500);
        $(".form-control-meternumber").show(500);
        $(".form-control-paymentvendor").show(500);
    }
    //bind transaction type to form submission
    $("button[type=\"submit\"]").attr("title",event.currentTarget.value);
});

//Clear All Input Feilds
$("#Clear").on("click", (event) => {
    event.preventDefault();
    $("input").val('');
}); 

/*
    Notification Modal
*/
var animate;
function notification(state = true, prompt = "", prompt_state = "Proccessed") {
    var notificaton_prompt;
    if (state) {
        $(".status-success").show();
        $(".status-failed").hide();
        notificaton_prompt = "<b>Trasnaction " + prompt_state + "</b> <br />";
        notificaton_prompt += prompt != null ? ("Your Current Balance Is: <b>$"+prompt + "</b><br />") : "";
        notificaton_prompt += "Thank You for Choosing SomeBank.";
    } else {
        $(".status-success").hide();
        $(".status-failed").show();
        notificaton_prompt = "<b>Trasnaction " + prompt_state + "</b> <br />" + prompt;
    }

    $(".notification-body").html(notificaton_prompt);
    $(".notification-modal").animate({
        "left" : "69vw"
    },250, () => {
        animate = setTimeout(() => {
            $(".notification-modal").css("left", "100vw");
            clearTimeout(animate);
        }, 3000);
    });
}
//clear auto close on hover
$(".notification-modal").hover(()=> {
    //mouse enter
    clearTimeout(this.animate);
}, ()=> {
    //mouse leave
    animate = setTimeout(() => {
        $(".notification-modal").css("left", "100vw");
        clearTimeout(this.animate);
    }, 3000);
});
//close notification
$(".btn-close span").on("click", ()=> {
    clearTimeout(this.animate);
    $(".notification-modal").css("left", "100vw");
});

/*
    CTA(Call To Action) Event Hanlder for Submission of trasactions
*/
$("button[type=\"submit\"]").on("click", async (event)=> {
    //prevent default submision of the form
    event.preventDefault();
    //define constants
    var Action = event.target.title;
    //check if user has selected a transaction type
    if (Action != "") {
        var Account_id = $("input[name=\"account\"]").val(); //required in on any transaction type
        var Amount = null;
        var Meter_number = null;
        var Payment_vendor = null;
        //Identify Trasnaction TYpe and extract input accordingly
        if (Action != "Balance")
            Amount = $("input[name=\"amount\"").val();
        
        if (Action == "Payment") {
            Meter_number = $("input[name=\"meterNumber\"]").val();
            Payment_vendor = $("input[name=\"paymentVendor\"]").val();
        }
        //generate JSON formatted string
        var transaction = JSON.stringify({Action,Account_id,Amount,Meter_number,Payment_vendor});
        //send to service rutine
        await Request("Request: " + transaction + "\n");
    } else
        notification(false, "Please Select Your <i>Trasaction Type</i>. <br /> Refer to the <i>instrcutions page</i> if unclear.", "Failed");
    
});
