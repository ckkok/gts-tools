const { getActualHolidays } = require('./datasources');
const { SGT_OFFSET_HOURS_FROM_UTC, INVALID_REQUEST_MESSAGES } = require('./constants');

const cachedResponses = {};

const isPublicHoliday = async dateStr => {
  const date = dateStr ? new Date(dateStr) : new Date(); // always returns UTC date/time
  date.setHours(date.getHours() + SGT_OFFSET_HOURS_FROM_UTC) // manually add the SGT offset timezone. This is no longer true UTC
  const year = date.getFullYear();
  const dateOnlyStr = `${year}-${(date.getMonth() + 1).toString().padStart(2, '0')}-${date.getDate().toString().padStart(2, '0')}`;
  if (cachedResponses[dateOnlyStr]) {
    return cachedResponses[dateOnlyStr];
  }
  const actualHolidays = await getActualHolidays(year);
  const dateOnly = new Date(dateOnlyStr);
  const holiday = actualHolidays.find(record => !(record.date < dateOnly) && !(record.date > dateOnly));
  const isHoliday = typeof holiday !== 'undefined' && holiday !== null;
  cachedResponses[dateOnlyStr] = { isHoliday, day: isHoliday ? holiday.day : '', reason: isHoliday ? holiday.holiday : '' };
  return cachedResponses[dateOnlyStr];
}

const main = async (event) => {
  const dateStr = event.queryStringParameters ? event.queryStringParameters.date.trim() : null;
  console.log('Checking holiday status for date:', dateStr);
  try {
    const holidayQueryResult = await isPublicHoliday(dateStr);
    return {
      statusCode: 200,
      body: JSON.stringify(holidayQueryResult)
    };
  } catch (e) {
    return {
      statusCode: INVALID_REQUEST_MESSAGES.has(e.message) ? 400 : 500,
      body: JSON.stringify({ error: e.message })
    }
  }
}

exports.handler = main;
exports.isPublicHoliday = isPublicHoliday;