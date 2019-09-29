package somebank_atm;

import Interface.Server;
import java.io.DataOutputStream;
import java.net.Socket;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Scanner;
import org.json.simple.JSONObject;
import static org.json.simple.JSONValue.parse;

/**
 * @author shivnay
 */
public class Client implements Runnable {
    //Client Socket Attributes
    private final Socket ClientConnection;
    //Server RMI Attributes
    private Server Server;
    private Registry registration;
    private final String ServerDomain = "localhost";
    private final int ServerPort = 1399;
    private boolean ServerStatus = false;
    private final int SocketPort;
    //intilize socket on current thread
    Client(Socket ClientConnection, int SocketPort) {  
        this.ClientConnection = ClientConnection;
        this.SocketPort = SocketPort;
    }
    /*
        intilize connection to RMI server
    */
    private void ConnectToServer() throws Exception {        
        try {
            registration = LocateRegistry.getRegistry(ServerDomain, ServerPort);
            Server = (Server) registration.lookup(Server.class.getSimpleName());
            System.out.println("Server Connected");
            ServerStatus = true;
        } catch (RemoteException ex) { System.out.println("Server Offline"); }
        
    }
    /*
        Process client request and generate response
    */
    private void httpRequest() throws Exception {
        String line;
        Scanner requestStream = new Scanner(ClientConnection.getInputStream());
        String requestParameters = "";
        //get request parameters
        while(true)  {
            line = requestStream.next();
            if (line.equalsIgnoreCase("Request:")) {
                requestParameters = requestStream.next();
                break;
            }
            requestParameters = line;
        }
        //process request parameters
        processRequest(requestParameters);
    }
    /*
        Process Client Request and generate JSON response
    */
    private void processRequest(String requestParameters) throws Exception {
        String serverStatus = this.ServerStatus ? "OK" : "offline";
        JSONObject Response = new JSONObject();
        Map JSONObj = new LinkedHashMap(2); 
        //Add Service Status to response
        JSONObj.put("Status","OK");
        JSONObj.put("Port", SocketPort);
        Response.put("Service", JSONObj);
        //Add Server status to response
        JSONObj = new LinkedHashMap(1);
        JSONObj.put("Status", serverStatus);
        Response.put("Server", JSONObj);
        if (this.ServerStatus) {
             //convert request to JSON
            JSONObject Request = (JSONObject) parse(requestParameters);
            String requestDomain = (String) Request.get("Action");
            if (!"init".equals(requestDomain)) {
                //Intilize request attributes
                int accountID = Integer.parseInt((String)Request.get("Account_id"));
                int amount = 0;
                String meterNumber = null, paymentVendor = null;
                JSONObj = new LinkedHashMap(2);
                String transactionStatus = "Proccessed";
                String transactionResponse = null;
                //Extract Request Parameters and perform transaction
                try {
                    if (Server.CheckAccount(accountID)) {
                        switch (requestDomain) {
                            case "Withdrawal":
                                amount = Integer.parseInt((String)Request.get("Amount"));
                                if (!Server.CheckOverdraft(accountID, amount))
                                    Server.widthdraw(accountID, amount);
                                else {
                                    transactionStatus = "Failed";
                                    transactionResponse = "Overdraft State Reached";
                                }
                                break;
                            case "Deposit":
                                amount = Integer.parseInt((String)Request.get("Amount"));
                                Server.deposit(accountID, amount);
                                break;
                            case "Balance":
                                Integer currentBalance = Server.CheckBalance(accountID);
                                transactionResponse = currentBalance.toString();
                                break;
                            case "Payment":
                                amount = Integer.parseInt((String)Request.get("Amount"));
                                meterNumber = (String)Request.get("Meter_number");
                                paymentVendor = (String)Request.get("Payment_vendor");
                                Server.makePayment(accountID, amount, meterNumber, paymentVendor);
                                break;
                            default:
                                transactionStatus = "Rejected";
                                transactionResponse = "Invalid Request";
                                System.out.println("inside default");
                                break;
                        }
                        JSONObj.put("Status",transactionStatus);
                        JSONObj.put("Response", transactionResponse);
                    } else {
                        JSONObj.put("Status","Rejected");
                        JSONObj.put("Response", "Account Does Not Exist");
                    }
                } catch (NumberFormatException ex) {
                    JSONObj.put("Status","Rejected");
                    JSONObj.put("Response", "Invalied Values");
                } catch (RemoteException ex) {
                    JSONObj.put("Status","Failed");
                    JSONObj.put("Response", "Server Error");
                }
                Response.put("Transaction", JSONObj);
            }
        }
        //send response to client
        httpResponse(Response.toJSONString());
    }
    /*
        Structure Response to JSON
    */
    private void httpResponse(String clientResponse) throws Exception { 
        String Response = "HTTP/1.1 200 OK\nAccess-Control-Allow-Origin: *\n\n" + clientResponse;
        DataOutputStream responseStream = new DataOutputStream(this.ClientConnection.getOutputStream());
        //Respond to request
        responseStream.writeBytes(Response);
        responseStream.flush();
    }
    
    /**
     * Execute each request on separate thread
     */
    @Override
    public void run() {
        try {
            ConnectToServer();
            httpRequest();
            ClientConnection.close();
        } catch (Exception CON_EX) {
            System.out.println(CON_EX.getMessage());
        }
    }
}
