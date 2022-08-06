# store-inventory
A store management system where multiple stores can be created and their inventory managed

## Dependencies
For convenience this repository contains the jar files for: 
* mysql-connector-java-8.0.29.jar


## Environment Variables
Need to set four environment variables for this code to work:

* dbHostname - A string representing the host computer where mySQL is installed
* dbUsername - A string representing the authorized username
* dbPassword - A string representing the password of the mySQL user
* dbDatabase - A string representing the name of the database

access to the database assumes the default mySQL port

## Network Connection
The code assumes it can directly connect to the computer identified by dbHostname. May be necessary to run a VPN to connect based on host computer's security.

