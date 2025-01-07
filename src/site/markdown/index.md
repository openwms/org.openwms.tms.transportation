## Purpose
**TMS Transportation** allows to create `TransportOrders` in automated warehouses. A `TransportOrder` is used to move `TransportUnits` (e.g.
pallets, boxes, cartons) between warehouse `Locations`. `TransportOrders` are created when a customer order is received or goods enter the
warehouse.
 
These kinds of `TransportOrders` are meant to be used in automated warehouses only. Other concepts with other types of transports, like
`Movements`, do better apply for manual warehouses (see the [Movements Service](https://openwms.github.io/org.openwms.wms.movements) for manual warehouses).

## Deployment
This component is a crucial part of the TMS (Transport Management System) and is required
across projects - perhaps in different flavour. The microservice is deployed as RTU
(ready-to-use) box in following environments:

| Endpoint                                          | Billed | SLA.   |
| ------------------------------------------------- | ------ | ------ |
| https://gateway.demo.openwms.cloud/transportation | no     | No SLA |

## Release

```
$ mvn deploy -Prelease,gpg
```

### Release Documentation

```
$ mvn package -DsurefireArgs=-Dspring.profiles.active=ASYNCHRONOUS,TEST -Psonar
$ mvn site scm-publish:publish-scm
```
