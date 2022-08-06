# store-inventory
A store management system where multiple stores can be created and their inventory managed

## Dependencies
For convenience this repository contains the jar files for: 
* mysql-connector-java-8.0.29.jar

## Database Schemas

```
+----------------+
| Tables_in_simd |
+----------------+
| Inventory      |
| Item           |
| Locations      |
| Overstock      |
| Stores         |
+----------------+
```

### Database Construction

```
CREATE TABLE `Inventory` (
`id` int(11) NOT NULL AUTO_INCREMENT,
`sku` int(11) DEFAULT NULL,
`location` int(11) DEFAULT NULL,
`quantity` int(11) DEFAULT NULL,
`maxQuantity` int(11) DEFAULT NULL,
PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=latin1;

CREATE TABLE `Locations` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `storeid` int(11) DEFAULT NULL,
  `aisle` varchar(16) DEFAULT NULL,
  `shelf` varchar(16) DEFAULT NULL,
  `section` varchar(16) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=latin1;


CREATE TABLE `Overstock` (
  `id` int(11) DEFAULT NULL,
  `sku` int(11) DEFAULT NULL,
  `quantity` int(11) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;


CREATE TABLE `Stores` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(64) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `name` (`name`)
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=latin1;
```


## Environment Variables
Need to set four environment variables for this code to work:

* dbHostname - A string representing the host computer where mySQL is installed
* dbUsername - A string representing the authorized username
* dbPassword - A string representing the password of the mySQL user
* dbDatabase - A string representing the name of the database

access to the database assumes the default mySQL port

## Network Connection
The code assumes it can directly connect to the computer identified by dbHostname. May be necessary to run a VPN to connect based on host computer's security.

