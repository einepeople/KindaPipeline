# KindaPipeline
Ex-YAWS - a multi-project system for scalable medical data processing

## Structure

General idea is [FHIR](https://en.wikipedia.org/wiki/Fast_Healthcare_Interoperability_Resources)-compliant system for processing medical data.
Pipeline consists of 4 services:
1. WebIn
2. Validation
3. DbOut
4. RestAccess

## Data flow

1. `WebIn`: Takes PUT/DELETE requests from outside of the system via REST API, validates requests, publishes valid ones to Kafka, sends a response
2. `Validation`: Takes data from Kafka, checks if it is a valid FHIR resource, publishes to Kafka
3. `DbOut`: Takes FHIR resources from Kafka, puts them into the database
4. `RestAccess`: Takes GET requests from outside of the system via REST API, takes information from the DB and sends a response
