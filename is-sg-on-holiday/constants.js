const SGT_OFFSET_HOURS_FROM_UTC = 8;
const YEAR_OUT_OF_RANGE_MESSAGE = 'Unsupported year';
const DATA_NOT_FOUND_MESSAGE = 'No data available';
const SERVER_ERROR_MESSAGE = 'Server error';
const INVALID_REQUEST_MESSAGES = new Set([YEAR_OUT_OF_RANGE_MESSAGE, DATA_NOT_FOUND_MESSAGE]);
const MAX_TRIES = 3;
const dataSources = {
  dataGovSg: Symbol('data.gov.sg'),
  mom: Symbol('MOM')
};
const daysOfWeek = ['Sunday', 'Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday', 'Saturday'];

module.exports = {
  SGT_OFFSET_HOURS_FROM_UTC,
  YEAR_OUT_OF_RANGE_MESSAGE,
  DATA_NOT_FOUND_MESSAGE,
  SERVER_ERROR_MESSAGE,
  INVALID_REQUEST_MESSAGES,
  MAX_TRIES,
  daysOfWeek,
  dataSources
}