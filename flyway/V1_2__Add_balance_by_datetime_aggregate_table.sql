CREATE SCHEMA aggregates;
CREATE TABLE aggregates.balance_hourly
(
    datetime timestamptz NOT NULL,
    balance  NUMERIC     NOT NULL
);

CREATE UNIQUE INDEX balance_hourly_datetime_idx ON aggregates.balance_hourly (datetime);