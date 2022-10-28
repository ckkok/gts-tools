const { expect } = require("chai");
const { getActualHolidays } = require('../datasources');

describe('Datasources', function () {
  it('getActualHolidays should return null when no rawData is given', function () {
    const actualHolidays = getActualHolidays(2000);
    expect(actualHolidays).to.be.null;
  })
})