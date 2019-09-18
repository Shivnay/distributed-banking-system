
package somebank;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.sql.ResultSet;
import Interface.Server;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;


public class SomeBank extends UnicastRemoteObject implements Server, Serializable {

    //Database Attributes
    private static Database Bank = new Database(); 
    //RMI Attributes
    private static Registry registration;
    private static final int AccessPort = 1399;
    private static SomeBank BankServer;
    
    //empty constructor
    public SomeBank() throws RemoteException { }
    
    @Override
    public void deposit(int account, int amount) throws RemoteException {
        Bank.Deposit(account,amount);
    }
    
    @Override
    public void widthdraw(int account, int amount) throws RemoteException {
        Bank.Withdraw(account, amount);
    }
    
    //Method to check balance of an account
    @Override
    public int CheckBalance(int account) throws RemoteException {
        return Bank.CheckBalance(account);
    }
    
    //Method to check overdraft of the account
    @Override
    public boolean CheckOverdraft(int account, int amount) throws RemoteException {
        return Bank.CheckOverdraft(account, amount);
    }
    
    //Method to check if account exists
    @Override
    public boolean CheckAccount(int account) throws RemoteException {
        return Bank.CheckAccount(account);
    }
    
    //Method to get transaction histroy from date and time range
    public static void getTransactions(int account,  LocalDateTime startDate, LocalDateTime endDate) throws RemoteException {
        //get transaction log
        ResultSet transaction_log = Bank.getTransactions(account, startDate, endDate);
    }
    
    /**
     * Method to make payment on account with a specified amount
     * @param account
     * @param amount
     * @param meterNumber
     * @param paymentVendor
     */
    @Override
    public void makePayment(int account, int amount, String meterNumber, String paymentVendor) throws RemoteException {
        Bank.makePayment(account, amount, meterNumber, paymentVendor);
    }
    
    public static void main(String[] args) {  
        //convert string to LocalDateTime
        String start = "2019-09-01 00:00";
        String end = "2019-09-05 00:00";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        LocalDateTime startDate = LocalDateTime.parse(start, formatter);
        LocalDateTime endDate = LocalDateTime.parse(end, formatter);
        
        try {
            BankServer = new SomeBank();
            //startup registery and server
            registration = LocateRegistry.createRegistry(AccessPort); //use default port for connection
            registration.rebind(Server.class.getSimpleName(), BankServer);
            System.out.println("SomeBank Server Started on Port: " + AccessPort);

        } catch (RemoteException ex) {
            System.out.println(ex.getMessage());
        }
    }
}
