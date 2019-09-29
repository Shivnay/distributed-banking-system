/*
    Author: Shivnay Swamy

    All communication between the service rutine is done
    following RESTful type Architecture style through HTTP and JSON.
*/
var service_status = false; //Conection State of service
var attempt_connection;
var ServiceRutine; 
//perform handshake protocal with service rutine on initial load
window.onload = Send();
/*
    Check Service Rutine Availibility
    @paramerters JSON formatted parameters to send to service rutine
        if no parameters provided assume handshake protocal
*/
function Send(parameters = "Request: {\"Action\":\"init\"}\n") {
    //Make Sure Service is Online
    //number of unsuccesful attempts made to connect to service rutine
    var error_count = 1;
    Request(parameters); //initial request
    //max 3 attempt's to connnect to service delay of 1 seconds between each attempt
    attempt_connection = setInterval(function() {
        // if (error_count == 0)
        //     Request(parameters); //initial request
        //only 3 attempts
        if (error_count > 2) {
            this.loader(false, "ATM Out of Service. Service Routine Unreachable. <br /> We regret any inconvinence caused");
            clearInterval(attempt_connection);
        } else {
            //perform connection validation after initial request complete's
            if(error_count > 0) {
                //toggle error modal accordingly
                if (this.service_status) {
                    this.loader(true); //connected to service
                    clearInterval(attempt_connection);
                } else {
                    this.loader(false, "Connection To Service Refused <br /> Re-attempting.....");
                    //attempt another request
                    Request(parameters); 
                }       
            }
            //unsuccessful attempt
            error_count++; 
        }
        //attempt another request after 1 second
    }, 3000);
}
/*
    Communicate With Service Rutine through HTTP
    @paramerters JSON formatted parameters to send to service rutine
        if no parameters provided assume handshake protocal
*/
function Request(parameters) {
    ServiceRutine = new XMLHttpRequest();
    //execute function on state change between service
    ServiceRutine.onreadystatechange = function() {
        if (this.status == 200) {
            if (this.readyState == 1) {
                //server connection established
                console.log("server connection established");
            }
            if (this.readyState == 2) {
                //request received
                console.log("request received");
            }
            if (this.readyState == 3) {
                //processing request
                console.log("processing request");
                //service rutine replied to request
                Response(this.response);                
            }
            if (this.readyState == 4) {
                //request finished and response is ready
                console.log("request finished and response is ready");
            }
        }
    };
    ServiceRutine.open("POST", "http://localhost:5000");
    ServiceRutine.send(parameters);
}
/*
    Convert response to JSON
    @parameters reply from servce rutine
*/
function Response(parameters) {
    //format parameters into JSON for processing
    var div = document.createElement("div"); 
    div.innerHTML = parameters;
    parameters = div.innerText;
    parameters = JSON.parse(parameters);
    console.log(parameters);
    //service rutine replied stop re-attempt loop
    clearInterval(attempt_connection);
    var attempt_server = setInterval(() => {
        //check server status
        if (parameters.Server.Status === "OK") {
            //handshake protocal complete, load UI
            if (!service_status) {
                this.loader(true);
                service_status = true;
            } else {
                switch (parameters.Transaction.Status) {
                    case "Proccessed":
                        notification(true, parameters.Transaction.Response, parameters.Transaction.Status);
                        break;
                    default:
                        notification(false, parameters.Transaction.Response, parameters.Transaction.Status);
                        break;
                }
            }
        } else {
            //handshake failed
            service_status = false;
            this.loader(false, "SomeBank Server Unreachable. <br /> We Regret any inconvinence caused");
        }
        clearInterval(attempt_server);
    }, 1000);
}

