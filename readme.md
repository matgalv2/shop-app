# Shop app
Simple CRUD REST-based application for managing online store. Database consists of 5 tables: clients, products, orders, addresses and ordersproducts, which resolves N:N relation between those two.

![image info](./icon.png)


### Progress
![](https://geps.dev/progress/100?dangerColor=800000&warningColor=ff9900&successColor=006600)

|                   | Customers | Products | Orders |
|-------------------|-----------|----------|--------|
| Implementation    | ✅         | ✅        | ✅      |
| Unit Tests        | ✅         | ✅        | ✅      |
| Integration Tests | ✅         | ✅        | ✅      |
| Acceptance Tests  | ✅         | ✅        | ✅      |

## Technology & tools:
Technology stack:
* Scala 2.13
* ZIO 2
* PostgreSQL
* Flyway
* Http4s
* Cats 2
* OpenAPI

Tools & libraries:
* Guardrail
* Quill
* Chimney
* Typesafe config


## API
All APIs are described here:
* [Customers API](./api/customerApi.yaml)
* [Products API](./api/productApi.yaml)
* [Orders API]()

## Design decisions:
### 1. REST API
* Based on OpenApi specifications Guardrail generates boilerplate code for controllers.

### 2. Database
* Database migrations - to ensure application works with latest version of database Flyway controls database migrations.
* DSL - to simplify work with database Quill provides QDSL for expressing queries in Scala.
* Queries implementations - due to fact that quill queries return effects that can fail with SQLError all effects should die with DatabaseCriticalFailure

### 3. Data transformation
* Mappers - Chimney library was utilized for transforming data types (Domain <-> DTO)

### 4. Domain
* Validation - to ensure all data flowing through app is valid, all the objects are validated before being used. 
* ErrorMessage - special type class for all domain errors to easily transform them into error responses

### 5. Data flow
* To allow fetching reasonable number of some type of data all repositories supports fetching with offset and limit.

## Possible future extensions
* Adding contact info to order