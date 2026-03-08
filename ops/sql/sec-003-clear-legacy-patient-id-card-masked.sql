SELECT COUNT(1) AS remaining_before
FROM patient
WHERE deleted = 0
  AND id_card_masked IS NOT NULL;

UPDATE patient
SET id_card_masked = NULL,
    updater = 'sec-003-cleanup',
    update_time = UTC_TIMESTAMP()
WHERE deleted = 0
  AND id_card_masked IS NOT NULL;

SELECT COUNT(1) AS remaining_after
FROM patient
WHERE deleted = 0
  AND id_card_masked IS NOT NULL;
