## Purpose
**TMS Transportation** allows to create `TransportOrders` in automated warehouses. A `TransportOrder` is used to transport pallets, boxes,
cartons or other types of `TransportUnits` between warehouse `Locations`. Usually more than one `TransportOrder` is created when a customer
order is received or when a lorry of goods arrives at the warehouse.
 
These kinds of `TransportOrders` are meant to be used in automated warehouses only. Other concepts with other types of transports, like
`Movements`, do better apply for manual warehouses (see the Movements Service for manual warehouses).

## Deployment
This component is a crucial part of the TMS (Transport Management System) and is required
across projects - perhaps in different flavor. The microservice is deployed as RTU
(ready-to-use) box in following environments:

| endpoints | billed | SLA |
| --------- | ------ | --- |
| https://openwms-tms-transportation.herokuapp.com | no | Heroku SLA for Europe region depends on AWS Europe region | 
  https://openwms.org/tms/transportation | no | No SLA |

## Release

```
$ mvn deploy -Prelease,gpg
```

### Release Documentation

```
$ mvn package -DsurefireArgs=-Dspring.profiles.active=ASYNCHRONOUS,TEST -Psonar
$ mvn site scm-publish:publish-scm
```
