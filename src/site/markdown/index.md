## Purpose
**TMS Transportation** allows to create Transport Orders in automated warehouses. A
Transport Order is used to transport pallets, boxes, cartons or other types of Transport
Units between warehouse Locations. Usually more then one Transport Order are created
when a customer order is received or when a lorry of goods arrives at the warehouse.
 
This kind of Transport Orders are meant to be used in automated warehouses only. Other
concepts with other types of transports, like Movements, do better apply for manual
warehouses.

## Deployment
This component is a crucial part of the TMS (Transport Management System) and is required
across projects - perhaps in different flavor. The microservice is deployed as RTU
(ready-to-use) box in following environments:

| endpoints | billed | SLA |
| --------- | ------ | --- |
| https://openwms-tms-transportation.herokuapp.com
  https://openwms.org/tms/transportation | no | Heroku SLA for Europe region depends on AWS Europe region |

