
package Interface;

import java.rmi.Remote;
import java.rmi.RemoteException;
/**
 *
 * @author shivn
 */
public interface Server extends Remote {
    //Method to depoit amount into account
    public void deposit(int account, int amount) throws RemoteException;
    //Method to widthdraw amount from account
    public void widthdraw(int account, int amount) throws RemoteException;
    //Method to check balance of an account
    public int CheckBalance(int account) throws RemoteException;
    //Method to check overdraft of the account
    public boolean CheckOverdraft(int account, int amount) throws RemoteException;
    //Method to make payment on acount with a specified amount
    public void makePayment(int account, int amount, String meterNumber, String paymentVendor) throws RemoteException;
    //Method to get a list of all registered accounts
    public boolean CheckAccount(int account) throws RemoteException;
}
