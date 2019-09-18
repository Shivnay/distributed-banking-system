
package somebank;

import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.format.DateTimeFormatter;  
import java.time.LocalDateTime;    
import java.util.Random;

/*
    Operations for somebank's SQLite Database
*/
public class Database {
    //Database Attributes
    private Connection db_connection = null;
    private Statement db_statment  = null;
    private ResultSet db_result = null;
    private String db_query = null;
    private DateTimeFormatter Date = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    //Account Attributes
    private Integer CurrentBalance = 0;
    private Integer previousAmount = 0;
    //Transaction Log Attributes
    private LocalDateTime DateStamp;  
    /*
     * connect to database
     */
    public Database() {
        // SQLite connection string
        String url = "jdbc:sqlite:somebank.sqlite";
        try {
            db_connection = DriverManager.getConnection(url);
        } catch (SQLException e) {
            System.out.println("Database Could Not Connected");
        }
    }
     /*
        Execute update query with specified conditions
        @param column; set of columns selected
        @param value; set of values for corisponding attributes
        @param table; name of table in database
        @param condition; set of conditions in query
    */
    private void Create(String[] column, String[] value, String table) throws SQLException {
        //Generate Query
        //assign table to query
        db_query = "INSERT INTO " + table; 
        //assign first attribute to query
        db_query += " (" + column[0];
        //assign remaing attribute's (if any)
        if (column.length > 1)
            for (int index=1; index < column.length; index++) 
                db_query += (", " + column[index]);
        db_query += ") VALUES ('";
        //assign first value to query
        db_query += value[0] + "'";
        //assign remaing values's (if any)
        if (value.length > 1)
            for (int index=1; index < value.length; index++) 
                db_query += (", '" + value[index])+ "'";
        db_query += ")";
        //Create statment and execute query
        db_statment  = db_connection.createStatement();
        db_statment.execute(db_query);
    }
    /*
        Execute read query with specified conditions
        @param column; set of columns selected
        @param table; name of table in database
        @param condition; set of conditions in query
    */
    private ResultSet Read(String[] column, String table, String condition) throws SQLException{
        //Generate Query
        db_query = "SELECT " + column[0]; //assign the first attribute to query
        //assign remaing attributes
        if (column.length > 1)
            for (String attribute : column) 
                db_query += (", " + attribute);
        //assign table and condition to query
        db_query += " FROM " + table + " WHERE " + condition;
        //Create statment and execute query
        db_statment  = db_connection.createStatement();
        db_result = db_statment.executeQuery(db_query);
        //return resultset
        return db_result; 
    }

    /*
        Execute update query with specified conditions
        @param column; set of columns selected
        @param value; set of values for corisponding attributes
        @param table; name of table in database
        @param condition; set of conditions in query
    */
    private void Update(String[] column, String[] value, String table, String condition) throws SQLException {
        //Generate Query
        //assign table to query
        db_query = "UPDATE " + table; 
        //assign first attribute to query
        db_query += " SET " + column[0] + "='" + value[0] + "'";
        //assign remaing attributes (if any)
        if (column.length > 1)
            for (int index=1; index < column.length; index++) 
                db_query += (", " + column[index] + "='" + value[index] + "'");
        //assign condition to query
        db_query += " WHERE " + condition;
        //Create statment and execute query
        db_statment  = db_connection.createStatement();
        db_statment.execute(db_query);
    }
    /**
     * Check if account is registered
     * @param account Account id
     */
    public boolean CheckAccount(int account) {
        //Query attributes
        String[] column = {"ACC_ID"};
        String table = "Account";
        String condition = "ACC_ID="+Integer.toString(account);
        try {
            //Get Current Balance
            db_result = Read(column,table,condition);
            String Temp = db_result.getString(1);
            return true;
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            return false;
        }
    }
    /**
     * process deposit transactions
     * @param account Account id
     * @param amount Deposit Amount
     */
    public void Deposit(int account, int amount) {
        //reset balance attributes
        previousAmount = 0;
        CurrentBalance = 0;
        //Query attributes
        String[] column = {"ACC_BALANCE"};
        String table = "Account";
        String condition = "ACC_ID="+Integer.toString(account);
        try {
            //Get Current Balance
            db_result = Read(column,table,condition);
            //update balance
            previousAmount = db_result.getInt("ACC_BALANCE");
            CurrentBalance = previousAmount + amount;
            //update record
            String[] value = {CurrentBalance.toString()};
            Update(column,value,table,condition);
            //log transaction
            DateStamp = LocalDateTime.now(); //get date and time of processed transaction
            log_transaction(account,"Deposit", "SomeBank Branch");
            
        } catch (SQLException e) {
            System.out.println("Transaction Failed: Stack Trace: " + e.toString());
        }
    }

    /**
     * process withdraw transactions
     * @param account Account id
     * @param amount Deposit Amount
     */
    public void Withdraw(int account, int amount) {
        //reset balance attributes
        previousAmount = 0;
        CurrentBalance = 0;
        //Query attributes
        String[] column = {"ACC_BALANCE"};
        String table = "Account";
        String condition = "ACC_ID="+ Integer.toString(account);
        try {
            //get current balance
            db_result = Read(column,table,condition);
            //update balance
            previousAmount = db_result.getInt("ACC_BALANCE");
            CurrentBalance = previousAmount - amount;
            //update record
            String[] value = {CurrentBalance.toString()};
            Update(column,value,table,condition);
            //log transaction
            DateStamp = LocalDateTime.now(); //get date and time of processed transaction
            log_transaction(account,"Withdrawal", "SomeBank Branch");
        } catch (SQLException e) {
            System.out.println("Transaction Failed: Stack Trace: " + e.getMessage());
        }
    }

    /**
     * process withdraw transactions
     * @param account; Account id
     */
    public int CheckBalance(int account) {
        //reset balance attributes
        previousAmount = 0;
        CurrentBalance = 0;
        //Query attributes
        String[] column = {"ACC_BALANCE"};
        String table = "Account";
        String condition = "ACC_ID="+ Integer.toString(account);
        try {
            //Update Current Balance
            db_result = Read(column,table,condition);
            CurrentBalance = db_result.getInt("ACC_BALANCE");
            return CurrentBalance;
        } catch (SQLException e) {
            System.out.println("Transaction Failed: Stack Trace: " + e.getMessage());
        }
        return -1; 
    }

    /**
     * process withdraw transactions
     * @param account Account id
     * @param amount Deposit Amount
     */
    public boolean CheckOverdraft(int account, int amount) {
        //temporary attribute
        int balance = 0;
        //Query attributes
        String[] column = {"ACC_BALANCE"};
        String table = "Account";
        String condition = "ACC_ID="+ Integer.toString(account);
        try {
            //get current balance
            db_result = Read(column,table,condition);
            //perform transaction
            balance = db_result.getInt("ACC_BALANCE");
            if ((balance - amount) >= 0) {
                //update balance attributes for future transaction
                previousAmount = balance;
                CurrentBalance = balance - amount;
                return false;
            }
        } catch (SQLException e) {
            System.out.println("Transaction Failed: Stack Trace: " + e.getMessage());
        }
        return true;
    }
    /*
        get list of transactions within specified date and time
    */
    public ResultSet getTransactions(int account,  LocalDateTime startDate, LocalDateTime endDate) {
        //reset result
        db_result = null;
        //Query attributes
        String[] column = {"*"};
        String table = "Transaction_Log";
        String condition = "ACC_ID='"+Integer.toString(account)+"' AND "+
                        "LOG_DATE_TIME BETWEEN '"+Date.format(startDate) +
                        "' AND '"+Date.format(endDate)+"'";
         try {
            //get current balance
            db_result = Read(column,table,condition);
         } catch (SQLException e) {
             System.out.println("Transaction Failed: Stack Trace: " + e.getMessage());
         }
         return db_result;
    }
    /*
        log transaction into database
        @param account; Account id
        @param transaction; nature of transaction
    */
    private void log_transaction(Integer account, String transaction, String vendor){
        //Query attributes
        String[] column = {"ACC_ID","LOG_TRANSACTION_TYPE","LOG_DATE_TIME",
                            "LOG_PREVIOUS_AMOUNT","LOG_NEW_AMOUNT","LOG_TRANSACTION_VENDOR","LOG_RECEIPT_CODE"};
        String table = "Transaction_Log";
        //generate pure random number
        Random rand = new Random(System.currentTimeMillis());
        Integer Recepit_code = rand.nextInt();
        String[] value = {account.toString(),transaction,Date.format(DateStamp),
                          previousAmount.toString(),CurrentBalance.toString(), vendor, 
                          (!"Payment".equals(transaction) ? "SMB-" + Recepit_code.toString() : "EFL-" + Recepit_code.toString())};
        try {
            //get current balance
            Create(column,value,table);
        } catch (SQLException e) {
            System.out.println("Transaction Failed: Stack Trace: " + e.getMessage());
        }
    }
    
    public void makePayment(int account, int amount, String meterNumber, String paymentVendor) {
        //Query attributes
        String table;
        String condition = null;
        db_result = null;
        int EFL_balance = 0;
        try {
            //veryfy bank account
            if (CheckAccount(account)) {
                //Withdraw amount from bank account
                Withdraw(account, amount);
                table = "EFL_Account";
                String[] column = {"*"};
                condition = "ACC_ID="+account;
                db_result = Read(column, table, condition);
                //Veryfy meter number
                if (db_result.getString("EFL_METER_NUMBER").equals(meterNumber)) {
                    EFL_balance = db_result.getInt("EFL_BALANCE");
                    if (EFL_balance - amount >= 0) {
                        //Deposit Amount into EFL account
                        String[] EFL_Account_column = {"EFL_BALANCE"};
                        Integer balance = EFL_balance - amount;
                        String[] value = {balance.toString()};
                        condition = "ACC_ID="+account+" AND EFL_METER_NUMBER="+meterNumber;
                        Update(EFL_Account_column,value,table,condition);
                        //Log transaction
                        log_transaction(account, "Payment", paymentVendor);
                        System.out.println("EFL Payment Complete");
                    } else 
                        System.out.println("Payment Overdraft");
                } else 
                    System.out.println("Meter Number Not Found");
            } 
        } catch (SQLException ex) {
            System.out.println("Query Failed: " + ex.getMessage());
        }
    }
}
