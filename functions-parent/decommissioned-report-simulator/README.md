# Decommissioned Report Simulator

## Requirements

- Generates xlsx report once per day.
- Puts report object to the special bucket.
- The report object key must match the general rule for placing objects
  in this bucket: `{store_id}/{year}/{month}/{day}`.

## Algorithm

At the beginning of each day do the following:

### Preparation

1. Receive all stores.

### Simulation

1. While next store exists do:
    1. Generate the report object key for store by template
       `{store_id}/{year}/{month}/{day}/report.xlsx`.
    2. Generate list of commodities for decommission with max length of
       `MAX_COMMODITIES_FOR_DECOMMISSION_COUNT` using predefined static
       list of commodities names.
    3. Generate xlsx report object content using the generated list of
       commodities.
    4. Upload report object to the bucket.
    5. Save the relevant decommissioned report entity to the database.
2. END.
