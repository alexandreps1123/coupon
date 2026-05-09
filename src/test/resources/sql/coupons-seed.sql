INSERT INTO coupons (
    id,
    code,
    description,
    discount_value,
    expiration_date,
    published,
    deleted,
    created_at,
    deleted_at
) VALUES
(
    '11111111-1111-1111-1111-111111111111',
    'ACT001',
    'Active coupon seed',
    10.00,
    TIMESTAMP '2030-01-01 00:00:00',
    TRUE,
    FALSE,
    TIMESTAMP '2026-01-01 10:00:00',
    NULL
),
(
    '22222222-2222-2222-2222-222222222222',
    'DEL001',
    'Deleted coupon seed',
    20.00,
    TIMESTAMP '2030-01-01 00:00:00',
    FALSE,
    TRUE,
    TIMESTAMP '2026-01-02 10:00:00',
    TIMESTAMP '2026-01-05 15:00:00'
);
