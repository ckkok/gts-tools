# Overview - Is that SG phone number sus?

A simple REST API wrapper over Google's libphonenumber library that validates whether a given phone number conforms to the [InfoComm Media Development Authority (IMDA)'s National Numbering Plan](https://www.imda.gov.sg/regulations-and-licensing-listing/numbering/national-numbering-plan-and-allocation-process#:~:text=The%20National%20Numbering%20Plan%20provides,other%20Internet%20Protocol%20(IP)%20based). This application is meant to be deployed as an AWS Lambda Function. This does not validate whether the number is in use or belongs to an actual entity.

## Use Cases

- Check whether a citizen's number submitted via a form might be valid 

## Why Java for an AWS Lambda Function?

The libphonenumber library's Java version is the primary version and receives updates first. By wrapping this version of the library as a REST API, applications written in other languages can quickly benefit from updated validation rules without waiting for community ports to be updated. The cold start penalty is minimal, typically at less than 1s, as few other libraries are present and no application start-up tasks are needed. Warm starts are much quicker, typically sub-10ms. If your application is already written in Java, you probably do not need to call this API.

## Building and Deploying as an AWS Lambda Function

- Run `mvn clean package shade:shade`
- The artifact can then be found at `target/sgphonenumbers-1.0.0.jar`
- Configure the lambda runtime to be Java 11 (Corretto) and the lambda handler to be `com.govtech.commons.Main::handleRequest`
- Configure the AWS API Gateway Lambda Proxy integration as desired
  - The handler will try to search for the key `number` in both the query parameters and the request body, so both GET and POST requests are supported

Note that once the artifact is built, the actual deployment and configuration steps, e.g. Terraform / AWS Console / Cloudformation are left to the user's choice.

## Calling the AWS Lambda Function

- GET request example: [https://d1a3gb9gl1.execute-api.ap-southeast-1.amazonaws.com/dev/phoneNumber/isValid?number=91234567](https://d1a3gb9gl1.execute-api.ap-southeast-1.amazonaws.com/dev/phoneNumber/isValid?number=91234567)
- POST request example: [https://d1a3gb9gl1.execute-api.ap-southeast-1.amazonaws.com/dev/phoneNumber/isValid]() with a request body of `{"number":"91234567"}`

### API Response

```
{
  "phoneNumber": string,
  "isValid": boolean
}
```

- `phoneNumber`: the given phone number string to be validated
- `isValid`: true if the phone number conforms to IMDA's National Numbering Plan, and false otherwise. This does not guarantee that the number is in use.

## Dependencies

- Java 11
- com.googlecode.libphonenumber:libphonenumber:8.12.57
- org.json:json:20220924