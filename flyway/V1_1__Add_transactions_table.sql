CREATE TABLE public.transactions
(
    id        serial      NOT NULL,
    datetime  timestamptz NOT NULL,
    created_at timestamptz NOT NULL DEFAULT NOW(),
    amount    NUMERIC     NOT NULL
);