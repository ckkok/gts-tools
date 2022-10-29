if (typeof process.argv[2] === 'undefined' || process.argv[2] === null) {
  console.log('Pass a year as an argument to fetch and process the data');
}

const { getActualHolidays } = require('./datasources');

const year = process.argv[2];
console.log('Fetching and updating data for year', year);
getActualHolidays(year, null, true)
  .then(_ => console.log(`Data for ${year} successfully updated`))
  .catch(err => console.error(`Unable to fetch and update data for ${year}:`, err));