-- 数据库性能优化配置
-- 用于优化MySQL性能参数

-- 1. 启用慢查询日志
SET GLOBAL slow_query_log = 'ON';
SET GLOBAL long_query_time = 1;
SET GLOBAL log_queries_not_using_indexes = 'ON';

-- 2. 优化查询缓存
SET GLOBAL query_cache_size = 268435456; -- 256MB
SET GLOBAL query_cache_type = 1;
SET GLOBAL query_cache_limit = 2097152; -- 2MB

-- 3. 优化连接数
SET GLOBAL max_connections = 200;
SET GLOBAL max_connect_errors = 1000;

-- 4. 优化临时表
SET GLOBAL tmp_table_size = 134217728; -- 128MB
SET GLOBAL max_heap_table_size = 134217728; -- 128MB

-- 5. 分析表统计信息
ANALYZE TABLE patients;
ANALYZE TABLE medication_plan;
ANALYZE TABLE today_task;
ANALYZE TABLE med_checkin;
ANALYZE TABLE med_adherence_stats;
ANALYZE TABLE med_report;
ANALYZE TABLE med_notification;
ANALYZE TABLE patient_audit_record;
ANALYZE TABLE patient_profile_template;

-- 6. 创建性能监控视图
CREATE OR REPLACE VIEW v_performance_stats AS
SELECT 
    'patients' as table_name,
    COUNT(*) as total_records,
    COUNT(CASE WHEN deleted = 0 THEN 1 END) as active_records,
    COUNT(CASE WHEN deleted = 1 THEN 1 END) as deleted_records,
    MAX(create_time) as latest_create_time,
    MAX(update_time) as latest_update_time
FROM patients
UNION ALL
SELECT 
    'medication_plan' as table_name,
    COUNT(*) as total_records,
    COUNT(CASE WHEN deleted = 0 THEN 1 END) as active_records,
    COUNT(CASE WHEN deleted = 1 THEN 1 END) as deleted_records,
    MAX(create_time) as latest_create_time,
    MAX(update_time) as latest_update_time
FROM medication_plan
UNION ALL
SELECT 
    'today_task' as table_name,
    COUNT(*) as total_records,
    COUNT(CASE WHEN deleted = 0 THEN 1 END) as active_records,
    COUNT(CASE WHEN deleted = 1 THEN 1 END) as deleted_records,
    MAX(create_time) as latest_create_time,
    MAX(update_time) as latest_update_time
FROM today_task
UNION ALL
SELECT 
    'med_checkin' as table_name,
    COUNT(*) as total_records,
    COUNT(CASE WHEN deleted = 0 THEN 1 END) as active_records,
    COUNT(CASE WHEN deleted = 1 THEN 1 END) as deleted_records,
    MAX(create_time) as latest_create_time,
    MAX(update_time) as latest_update_time
FROM med_checkin;

-- 7. 创建索引使用情况监控
CREATE OR REPLACE VIEW v_index_usage AS
SELECT 
    TABLE_SCHEMA,
    TABLE_NAME,
    INDEX_NAME,
    CARDINALITY,
    SUB_PART,
    PACKED,
    NULLABLE,
    INDEX_TYPE,
    COMMENT
FROM information_schema.STATISTICS 
WHERE TABLE_SCHEMA = 'medicine'
ORDER BY TABLE_NAME, INDEX_NAME;

-- 完成性能优化配置
SELECT 'Database performance optimization completed' as status;
