# Overview - Is SG on Holiday?

A small NodeJS application to find out if a given date is a Singapore public holiday, accounting for holiday-in-lieus due to stated holidays being on a Sunday. An example deployment can be found at [https://d1a3gb9gl1.execute-api.ap-southeast-1.amazonaws.com/dev/isHoliday?date=2023-01-02](https://d1a3gb9gl1.execute-api.ap-southeast-1.amazonaws.com/dev/isHoliday?date=2023-01-02).

## Use Cases

- Cron jobs that need to not be run on public holidays

## Usage

### As an import

The `isPublicHoliday` function can be imported and called with an ISO 8601 date/time string or a date string of the format 'yyyy-MM-dd'.

### As an AWS Lambda Function

The `handler` function can be invoked by AWS Lambda to call the above `isPublicHoliday` function with input from the query parameter `date`. See the example in the overview.

#### Deploying as a Lambda Function

The zipped artifact should contain the following files/folders:

- `./vendor`
    - To use Ical.js as a Lambda Layer, the datasources.js file will need to be edited to import it appropriately
- `index.js`
- `constants.js`
- `datasources.js`

The actual deployment method, e.g. Terraform / AWS Console / Cloudformation, is left to the user's choice.

### Response

The response from the function is of the form 

```
{
    isHoliday: boolean,
    day: string,
    reason: string
}
```

- `isHoliday` is true if the given date (or the current date) is a holiday
- `day` is the day of the week, e.g. Monday, if the given date is a holiday
- `reason` is the name of the holiday if the given date is a holiday

### Remarks

Not passing in any input to the `isPublicHoliday` function will cause it to use the current date instead.

## Data Sources

- MOM: iCal files are downloaded from [https://www.mom.gov.sg/employment-practices/public-holidays](https://www.mom.gov.sg/employment-practices/public-holidays). Provides data between 2018 and 2023 (inclusive)
- data.gov.sg: Public holiday data between 2020 and 2023 (inclusive)

## Dependencies

- Ical.js v1.5.0: Used to parse iCal files
    - [Snyk.io Package Health Score](https://snyk.io/advisor/npm-package/ical.js)

