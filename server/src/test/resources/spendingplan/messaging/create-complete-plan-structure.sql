INSERT INTO server.spending_plans(id,
                                  creation_timestamp,
                                  account_id,
                                  name,
                                  amount,
                                  currency,
                                  description)
VALUES ('3b257779-a5db-4e87-9365-72c6f8d4977d',
        '2024-03-23T10:00:04.224870Z',
        'e2709aa2-7907-4f78-98b6-0f36a0c1b5ca',
        'My basic plan',
        1000.0,
        'EUR',
        'My basic plan for tracking expenses');

INSERT INTO server.spending_jars(id,
                                 creation_timestamp,
                                 name,
                                 amount_to_reach,
                                 currency,
                                 percentage,
                                 description,
                                 plan_id)
VALUES ('e2709aa2-7907-4f78-98b6-0f36a0c1b5ca',
        '2024-03-23T10:00:04.224870Z',
        'Necessities',
        550.0,
        'EUR',
        55,
        'Necessities spending: Rent, Food, Bills etc.',
        '3b257779-a5db-4e87-9365-72c6f8d4977d'),
       ('a6993312-2e45-43e4-b965-9edc88da7a00',
        '2024-03-23T10:00:04.224870Z',
        'Long Term Savings',
        450.0,
        'EUR',
        45,
        'Long Term Savings: Investigations, Medical etc.',
        '3b257779-a5db-4e87-9365-72c6f8d4977d');

INSERT INTO server.spending_jar_expense_category(id,
                                                 jar_id,
                                                 category_id,
                                                 creation_timestamp)
VALUES (1,
        'e2709aa2-7907-4f78-98b6-0f36a0c1b5ca',
        'e2709aa2-7907-4f78-98b6-0f36a0c1b5ca',
        '2024-03-23T10:00:04.224870Z'),
       (2,
        'a6993312-2e45-43e4-b965-9edc88da7a00',
        'ee0a4cdc-84f0-4f81-8aea-224dad4915e7',
        '2024-03-23T10:00:04.224870Z'),
       (3,
        'a6993312-2e45-43e4-b965-9edc88da7a00',
        '8a366e74-b4e3-4e64-a2a6-dce273ce332a',
        '2024-03-23T10:00:04.224870Z');
