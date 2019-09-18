# Distributed Banking System
A Distributed Banking System implimented with java Remote Method Invocation (RMI), and Java Sockets.

# Client Portal
    ATM Like User Interface with the following options:
    1. Deposit
    2. widthdraw 
    3. Check current balance
    4. Check overdraft 
    5. Get Transactions
    6. Make Bill Payment
All the features above are envoked by implimented web portal, the requests are then forwarded to the Transaction Processing Monitor (TPM).

# Transaction Processing Monitor (TPM)
The TPM acts as the buffer for the client and the server, the client portal communicates only with the TPM, and it is responsible for communication with the bank server, which includes syncronization and validation, the connection between the web portal and the TPM is Stateless and implimented through sending HTTP requests to the TPM that has exposed an "API Like" interface through a Socket, that send and revices JSON data. The Service Rutine performs all the overhead operations to prevent bottle neck's on the server.

# Server
The server is responsible for final proessing of transactions after the TPM performs its validations, on its own the server performs no validation on the provided feilds as it is throughly done by the TPM. The connection between the Service Rutine and the Server is stateful for security purposes. The server is responsible for managing the Database, and the server alone communicates with the database directly.

# Database
Design and construction of the database is done with SQLite, and intergrated at server side.
