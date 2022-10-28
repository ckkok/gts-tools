const { expect } = require("chai");
const { handler } = require('../index');
const CONSTANTS = require('../constants');

describe('Integration test', function () {

  this.timeout(3000);

  const createEventForDateStr = dateStr => {
    return {
      queryStringParameters: {
        date: dateStr
      }
    }
  };

  it('should use current date when no date string is given', async function() {
    const response = await handler({});
    expect(response.statusCode).to.equal(200);
  });

  it('should accept ISO 8601 date-time string', async function() {
    const response = await handler(createEventForDateStr(new Date().toISOString()));
    expect(response.statusCode).to.equal(200);
  });

  [
    '2018-01-01',
    '2023-01-01',
    '2023-01-02'
  ].forEach(dateStr => {
    it(`should return isHoliday as true for ${dateStr}`, async function () {
      const response = await handler(createEventForDateStr(dateStr));
      expect(response.statusCode).to.equal(200);
      const body = JSON.parse(response.body);
      expect(body.isHoliday).to.be.true;
    })
  });

  [
    '2023-01-03',
    '2023-02-05'
  ].forEach(dateStr => {
    it(`should return isHoliday as false for ${dateStr}`, async function () {
      const response = await handler(createEventForDateStr(dateStr));
      expect(response.statusCode).to.equal(200);
      const body = JSON.parse(response.body);
      expect(body.isHoliday).to.be.false;
    })
  });

  it('should return statusCode 400 and unsupported year error for 2017-01-01', async function () {
    const response = await handler(createEventForDateStr('2017-01-01'));
    expect(response.statusCode).to.equal(400);
    const body = JSON.parse(response.body);
    expect(body.error).to.equal(CONSTANTS.YEAR_OUT_OF_RANGE_MESSAGE);
  })
})