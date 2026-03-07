USE medicine;

UPDATE infra_file_config 
SET config = REPLACE(
    config,
    '"@class":"db.client.core.com.sqlapk.medicine.framework.file.DBFileClientConfig"',
    '"@class":"com.sqlapk.medicine.framework.file.core.client.db.DBFileClientConfig"'
)
WHERE config LIKE '%db.client.core.com.sqlapk.medicine.framework.file.DBFileClientConfig%';

UPDATE infra_file_config 
SET config = REPLACE(
    config,
    '"@class":"local.client.core.com.sqlapk.medicine.framework.file.LocalFileClientConfig"',
    '"@class":"com.sqlapk.medicine.framework.file.core.client.local.LocalFileClientConfig"'
)
WHERE config LIKE '%local.client.core.com.sqlapk.medicine.framework.file.LocalFileClientConfig%';

UPDATE infra_file_config 
SET config = REPLACE(
    config,
    '"@class":"s3.client.core.com.sqlapk.medicine.framework.file.S3FileClientConfig"',
    '"@class":"com.sqlapk.medicine.framework.file.core.client.s3.S3FileClientConfig"'
)
WHERE config LIKE '%s3.client.core.com.sqlapk.medicine.framework.file.S3FileClientConfig%';

