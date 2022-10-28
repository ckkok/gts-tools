const https = require('https');
const { MAX_TRIES, SERVER_ERROR_MESSAGE, YEAR_OUT_OF_RANGE_MESSAGE, 
  DATA_NOT_FOUND_MESSAGE, dataSources: _dataSources, daysOfWeek } = require('./constants');
const { dataGovSg, mom } = _dataSources;
const dataGovSgResourceIds = {
  "2020": "6228c3c5-03bd-4747-bb10-85140f87168b",
  "2021": "550f6e9e-034e-45a7-a003-cf7f7e252c9a",
  "2022": "04a78f5b-2d12-4695-a6cd-d2b072bc93fe",
  "2023": "98aa24ef-954d-4f76-b733-546e0fcf1d0a"
};

const dataSources = {
  [dataGovSg]: {
    minYear: 2020,
    maxYear: 2023,
    getUrl: year => {
      if (year >= dataSources[dataGovSg].minYear && year <= dataSources[dataGovSg].maxYear) {
        return `https://data.gov.sg/api/action/datastore_search?resource_id=${dataGovSgResourceIds[year]}`;
      }
      return null;
    },
    parseData: JSON.parse
  },
  [mom]: {
    minYear: 2018,
    maxYear: 2023,
    getUrl: year => {
      if (year >= dataSources[mom].minYear && year <= dataSources[mom].maxYear) {
        return `https://www.mom.gov.sg/-/media/mom/documents/employment-practices/public-holidays/public-holidays-sg-${year}.ics`;
      }
      return null;
    },
    parseData: rawData => {
      const ICAL = require('./vendor/ical.min.js');
      const component = new ICAL.Component(ICAL.parse(rawData));
      const events = component.getAllSubcomponents('vevent').map(item => new ICAL.Event(item));
      return {
        result: {
          records: events.map((event, idx) => {
            return {
              date: event.startDate.toString(),
              holiday: event.summary,
              _id: idx + 1,
              day: daysOfWeek[new Date(event.startDate.toString()).getDay()]
            }
          })
        }
      }
    }
  }
}

const cachedRawData = {};
const cachedActualHolidays = {};

const _doFetch = (url, resolve, reject, year, source, tryCount) => {
  https.get(url, res => {
    if (res.statusCode !== 200) {
      if (res.statusCode >= 500 && tryCount < MAX_TRIES) {
        if (tryCount < MAX_TRIES) {
          return _doFetch(url, resolve, reject, year, source, tryCount + 1);
        }
        return reject(new Error(SERVER_ERROR_MESSAGE));
      }
      return reject(new Error(DATA_NOT_FOUND_MESSAGE));
    }
    let rawData = '';
    res.setEncoding('utf-8');
    res.on('data', chunk => {
      rawData += chunk;
    });
    res.on('end', () => {
      cachedRawData[year] = dataSources[source].parseData(rawData);
      cachedRawData[year];
      resolve(cachedRawData[year]);
    })
  })
}

const fetchData = year => {
  if (cachedRawData[year]) {
    return Promise.resolve(cachedRawData[year]);
  }
  let source = dataGovSg;
  let url = dataSources[source].getUrl(year);
  if (url === null) {
    source = mom;
    url = dataSources[source].getUrl(year);
  }
  if (url === null) {
    return Promise.reject(new Error(YEAR_OUT_OF_RANGE_MESSAGE));
  }
  return new Promise((resolve, reject) => {
    _doFetch(url, resolve, reject, year, source, 1);
  })
}

const getActualHolidays = (year, rawData) => {
  if (cachedActualHolidays[year]) {
    return cachedActualHolidays[year];
  }
  if (typeof rawData === 'undefined' || rawData === null) {
    return null;
  }
  const statedHolidays = rawData.result.records.map(record => {
    return {
      date: new Date(record.date),
      holiday: record.holiday,
      day: record.day
    }
  });
  statedHolidays.sort((a, b) => a.date < b.date);
  const actualHolidays = [];
  for (let i = 0; i < statedHolidays.length; i++) {
    actualHolidays.push({ ...statedHolidays[i] });
    if (statedHolidays[i].day === 'Sunday') {
      const actualHoliday = {
        date: new Date(statedHolidays[i].date),
        holiday: statedHolidays[i].holiday,
        day: statedHolidays[i].day
      };
      actualHoliday.date.setDate(actualHoliday.date.getDate() + 1);
      let j = i + 1;
      while (j < statedHolidays.length) {
        const nextHoliday = statedHolidays[j];
        if (!(actualHoliday.date < nextHoliday.date) && !(actualHoliday.date > nextHoliday.date)) {
          actualHoliday.date.setDate(actualHoliday.date.getDate() + 1);
        } else {
          break;
        }
        j++;
      }
      actualHoliday.day = daysOfWeek[actualHoliday.date.getDay()];
      actualHolidays.push(actualHoliday);
    }
  }
  cachedActualHolidays[year] = actualHolidays;
  return cachedActualHolidays[year];
}

module.exports = {
  fetchData,
  getActualHolidays
}