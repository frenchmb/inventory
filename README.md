# Inventory Service

## Build
To build the Inventory service type the following command :

Linux/Macos
```$xslt
mvnw clean install
```

Windoze
```$xslt
mvnw.cmd clean install
```

## Run Inventory Service
The inventory service can be run using either of the following commands:

```
java -jar target\inventory-1.0-SNAPSHOT.jar
```

OR

```
mvnw spring-boot:run
```

The service will not start and listen on localhost port 8080.
