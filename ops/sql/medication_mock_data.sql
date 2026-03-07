-- 用药打卡系统 Mock 数据脚本
-- 用于测试和演示

-- 清理现有数据（谨慎使用）
-- DELETE FROM med_checkins WHERE id > 0;
-- DELETE FROM med_today_tasks WHERE id > 0;
-- DELETE FROM med_plan_items WHERE id > 0;
-- DELETE FROM med_plans WHERE id > 0;

-- 插入用药计划 Mock 数据
INSERT INTO med_plans (id, patient_id, plan_name, start_date, end_date, status, version, description, instructions, creator, create_time, updater, update_time, deleted, tenant_id) VALUES
(1, 1, '高血压用药计划', '2025-01-01', '2025-04-01', 1, 1, '高血压患者用药计划', '按时服药，注意监测血压', 'admin', NOW(), 'admin', NOW(), 0, 0),
(2, 2, '糖尿病用药计划', '2025-01-01', '2025-04-01', 1, 1, '糖尿病患者用药计划', '按时服药，注意监测血糖', 'admin', NOW(), 'admin', NOW(), 0, 0),
(3, 3, '心脏病用药计划', '2025-01-01', '2025-04-01', 1, 1, '心脏病患者用药计划', '按时服药，注意监测心率', 'admin', NOW(), 'admin', NOW(), 0, 0);

-- 插入用药计划项 Mock 数据
INSERT INTO med_plan_items (id, plan_id, drug_name, dosage, frequency, time_points, meal_relation, instructions, sort_order, creator, create_time, updater, update_time, deleted, tenant_id) VALUES
-- 高血压用药计划项
(1, 1, '氨氯地平', '5mg', '每日一次', '["08:00"]', 1, '餐前服用', 1, 'admin', NOW(), 'admin', NOW(), 0, 0),
(2, 1, '美托洛尔', '25mg', '每日两次', '["08:00", "20:00"]', 1, '餐前服用', 2, 'admin', NOW(), 'admin', NOW(), 0, 0),
(3, 1, '氢氯噻嗪', '12.5mg', '每日一次', '["08:00"]', 2, '餐后服用', 3, 'admin', NOW(), 'admin', NOW(), 0, 0),

-- 糖尿病用药计划项
(4, 2, '二甲双胍', '500mg', '每日两次', '["08:00", "18:00"]', 2, '餐后服用', 1, 'admin', NOW(), 'admin', NOW(), 0, 0),
(5, 2, '格列齐特', '80mg', '每日一次', '["08:00"]', 1, '餐前服用', 2, 'admin', NOW(), 'admin', NOW(), 0, 0),

-- 心脏病用药计划项
(6, 3, '阿司匹林', '100mg', '每日一次', '["08:00"]', 2, '餐后服用', 1, 'admin', NOW(), 'admin', NOW(), 0, 0),
(7, 3, '硝酸甘油', '0.5mg', '按需服用', '[]', 0, '胸痛时舌下含服', 2, 'admin', NOW(), 'admin', NOW(), 0, 0);

-- 插入今日待办任务 Mock 数据（最近7天）
INSERT INTO med_today_tasks (id, plan_item_id, patient_id, task_date, scheduled_time, status, completed_time, notes, creator, create_time, updater, update_time, deleted, tenant_id) VALUES
-- 患者1的高血压用药任务（最近7天）
(1, 1, 1, '2025-01-06', '08:00:00', 0, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 0),
(2, 2, 1, '2025-01-06', '08:00:00', 0, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 0),
(3, 2, 1, '2025-01-06', '20:00:00', 0, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 0),
(4, 3, 1, '2025-01-06', '08:00:00', 0, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 0),

-- 患者1的高血压用药任务（昨天）
(5, 1, 1, '2025-01-05', '08:00:00', 1, '2025-01-05 08:05:00', '按时服药', 'admin', NOW(), 'admin', NOW(), 0, 0),
(6, 2, 1, '2025-01-05', '08:00:00', 1, '2025-01-05 08:03:00', '按时服药', 'admin', NOW(), 'admin', NOW(), 0, 0),
(7, 2, 1, '2025-01-05', '20:00:00', 1, '2025-01-05 20:02:00', '按时服药', 'admin', NOW(), 'admin', NOW(), 0, 0),
(8, 3, 1, '2025-01-05', '08:00:00', 1, '2025-01-05 08:04:00', '按时服药', 'admin', NOW(), 'admin', NOW(), 0, 0),

-- 患者1的高血压用药任务（前天）
(9, 1, 1, '2025-01-04', '08:00:00', 1, '2025-01-04 08:10:00', '按时服药', 'admin', NOW(), 'admin', NOW(), 0, 0),
(10, 2, 1, '2025-01-04', '08:00:00', 1, '2025-01-04 08:08:00', '按时服药', 'admin', NOW(), 'admin', NOW(), 0, 0),
(11, 2, 1, '2025-01-04', '20:00:00', 2, NULL, '忘记服药', 'admin', NOW(), 'admin', NOW(), 0, 0),
(12, 3, 1, '2025-01-04', '08:00:00', 1, '2025-01-04 08:12:00', '按时服药', 'admin', NOW(), 'admin', NOW(), 0, 0),

-- 患者2的糖尿病用药任务（最近7天）
(13, 4, 2, '2025-01-06', '08:00:00', 0, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 0),
(14, 4, 2, '2025-01-06', '18:00:00', 0, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 0),
(15, 5, 2, '2025-01-06', '08:00:00', 0, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 0),

-- 患者2的糖尿病用药任务（昨天）
(16, 4, 2, '2025-01-05', '08:00:00', 1, '2025-01-05 08:15:00', '按时服药', 'admin', NOW(), 'admin', NOW(), 0, 0),
(17, 4, 2, '2025-01-05', '18:00:00', 1, '2025-01-05 18:05:00', '按时服药', 'admin', NOW(), 'admin', NOW(), 0, 0),
(18, 5, 2, '2025-01-05', '08:00:00', 1, '2025-01-05 08:12:00', '按时服药', 'admin', NOW(), 'admin', NOW(), 0, 0),

-- 患者3的心脏病用药任务（最近7天）
(19, 6, 3, '2025-01-06', '08:00:00', 0, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 0),

-- 患者3的心脏病用药任务（昨天）
(20, 6, 3, '2025-01-05', '08:00:00', 1, '2025-01-05 08:20:00', '按时服药', 'admin', NOW(), 'admin', NOW(), 0, 0);

-- 插入打卡记录 Mock 数据
INSERT INTO med_checkins (id, todo_id, patient_id, checkin_time, status, notes, evidence, client_nonce, creator, create_time, updater, update_time, deleted, tenant_id) VALUES
-- 患者1的打卡记录
(1, 5, 1, '2025-01-05 08:05:00', 1, '按时服药', '{"images": ["medication1.jpg"], "audio": null}', 'client-nonce-001', 'admin', NOW(), 'admin', NOW(), 0, 0),
(2, 6, 1, '2025-01-05 08:03:00', 1, '按时服药', '{"images": ["medication2.jpg"], "audio": null}', 'client-nonce-002', 'admin', NOW(), 'admin', NOW(), 0, 0),
(3, 7, 1, '2025-01-05 20:02:00', 1, '按时服药', '{"images": ["medication3.jpg"], "audio": null}', 'client-nonce-003', 'admin', NOW(), 'admin', NOW(), 0, 0),
(4, 8, 1, '2025-01-05 08:04:00', 1, '按时服药', '{"images": ["medication4.jpg"], "audio": null}', 'client-nonce-004', 'admin', NOW(), 'admin', NOW(), 0, 0),

-- 患者1的打卡记录（前天）
(5, 9, 1, '2025-01-04 08:10:00', 1, '按时服药', '{"images": ["medication5.jpg"], "audio": null}', 'client-nonce-005', 'admin', NOW(), 'admin', NOW(), 0, 0),
(6, 10, 1, '2025-01-04 08:08:00', 1, '按时服药', '{"images": ["medication6.jpg"], "audio": null}', 'client-nonce-006', 'admin', NOW(), 'admin', NOW(), 0, 0),
(7, 12, 1, '2025-01-04 08:12:00', 1, '按时服药', '{"images": ["medication7.jpg"], "audio": null}', 'client-nonce-007', 'admin', NOW(), 'admin', NOW(), 0, 0),

-- 患者2的打卡记录
(8, 16, 2, '2025-01-05 08:15:00', 1, '按时服药', '{"images": ["medication8.jpg"], "audio": null}', 'client-nonce-008', 'admin', NOW(), 'admin', NOW(), 0, 0),
(9, 17, 2, '2025-01-05 18:05:00', 1, '按时服药', '{"images": ["medication9.jpg"], "audio": null}', 'client-nonce-009', 'admin', NOW(), 'admin', NOW(), 0, 0),
(10, 18, 2, '2025-01-05 08:12:00', 1, '按时服药', '{"images": ["medication10.jpg"], "audio": null}', 'client-nonce-010', 'admin', NOW(), 'admin', NOW(), 0, 0),

-- 患者3的打卡记录
(11, 20, 3, '2025-01-05 08:20:00', 1, '按时服药', '{"images": ["medication11.jpg"], "audio": null}', 'client-nonce-011', 'admin', NOW(), 'admin', NOW(), 0, 0);

-- 插入依从统计 Mock 数据
INSERT INTO med_adherence_stats (id, patient_id, period, start_date, end_date, total_tasks, completed_tasks, missed_tasks, delayed_tasks, adherence_rate, top_medications, statistics_date, creator, create_time, updater, update_time, deleted, tenant_id) VALUES
-- 患者1的7天依从统计
(1, 1, 7, '2025-01-01', '2025-01-07', 28, 25, 2, 1, 89.3, '[{"drugName": "氨氯地平", "completionRate": 95.0}, {"drugName": "美托洛尔", "completionRate": 85.7}, {"drugName": "氢氯噻嗪", "completionRate": 100.0}]', '2025-01-07', 'admin', NOW(), 'admin', NOW(), 0, 0),

-- 患者1的30天依从统计
(2, 1, 30, '2024-12-08', '2025-01-06', 120, 108, 8, 4, 90.0, '[{"drugName": "氨氯地平", "completionRate": 92.0}, {"drugName": "美托洛尔", "completionRate": 88.0}, {"drugName": "氢氯噻嗪", "completionRate": 90.0}]', '2025-01-06', 'admin', NOW(), 'admin', NOW(), 0, 0),

-- 患者2的7天依从统计
(3, 2, 7, '2025-01-01', '2025-01-07', 21, 19, 1, 1, 90.5, '[{"drugName": "二甲双胍", "completionRate": 90.5}, {"drugName": "格列齐特", "completionRate": 90.5}]', '2025-01-07', 'admin', NOW(), 'admin', NOW(), 0, 0),

-- 患者3的7天依从统计
(4, 3, 7, '2025-01-01', '2025-01-07', 7, 6, 1, 0, 85.7, '[{"drugName": "阿司匹林", "completionRate": 85.7}]', '2025-01-07', 'admin', NOW(), 'admin', NOW(), 0, 0);

-- 插入报告 Mock 数据
INSERT INTO med_reports (id, patient_id, report_type, title, start_date, end_date, content, adherence_summary, medication_summary, health_indicators, generation_time, creator, create_time, updater, update_time, deleted, tenant_id) VALUES
-- 患者1的季度报告
(1, 1, 3, '患者1 - 2025年第一季度用药报告', '2025-01-01', '2025-03-31', '{"summary": "患者用药依从性良好，血压控制稳定", "recommendations": "继续按计划服药，定期监测血压"}', '{"adherenceRate": 90.0, "totalTasks": 360, "completedTasks": 324, "missedTasks": 36}', '{"totalMedications": 3, "topMedication": "氨氯地平", "averageCompletionRate": 90.0}', '{"bloodPressure": "120/80", "heartRate": "72", "weight": "70kg"}', '2025-01-06 10:00:00', 'admin', NOW(), 'admin', NOW(), 0, 0),

-- 患者2的月度报告
(2, 2, 2, '患者2 - 2025年1月用药报告', '2025-01-01', '2025-01-31', '{"summary": "患者用药依从性良好，血糖控制稳定", "recommendations": "继续按计划服药，定期监测血糖"}', '{"adherenceRate": 88.5, "totalTasks": 62, "completedTasks": 55, "missedTasks": 7}', '{"totalMedications": 2, "topMedication": "二甲双胍", "averageCompletionRate": 88.5}', '{"bloodSugar": "6.5", "weight": "65kg", "bmi": "22.5"}', '2025-01-06 10:00:00', 'admin', NOW(), 'admin', NOW(), 0, 0);

-- 插入通知 Mock 数据
INSERT INTO med_notifications (id, patient_id, title, content, type, status, send_time, read_time, creator, create_time, updater, update_time, deleted, tenant_id) VALUES
-- 患者1的通知
(1, 1, '用药提醒', '您的高血压用药时间到了，请按时服药', 1, 0, '2025-01-06 08:00:00', NULL, 'admin', NOW(), 'admin', NOW(), 0, 0),
(2, 1, '用药提醒', '您的美托洛尔用药时间到了，请按时服药', 1, 0, '2025-01-06 20:00:00', NULL, 'admin', NOW(), 'admin', NOW(), 0, 0),
(3, 1, '报告生成', '您的季度用药报告已生成，请查看', 2, 1, '2025-01-06 10:00:00', '2025-01-06 10:05:00', 'admin', NOW(), 'admin', NOW(), 0, 0),

-- 患者2的通知
(4, 2, '用药提醒', '您的二甲双胍用药时间到了，请按时服药', 1, 0, '2025-01-06 08:00:00', NULL, 'admin', NOW(), 'admin', NOW(), 0, 0),
(5, 2, '用药提醒', '您的二甲双胍用药时间到了，请按时服药', 1, 0, '2025-01-06 18:00:00', NULL, 'admin', NOW(), 'admin', NOW(), 0, 0),

-- 患者3的通知
(6, 3, '用药提醒', '您的阿司匹林用药时间到了，请按时服药', 1, 0, '2025-01-06 08:00:00', NULL, 'admin', NOW(), 'admin', NOW(), 0, 0);

-- 更新自增ID序列（如果需要）
-- ALTER TABLE med_plans AUTO_INCREMENT = 4;
-- ALTER TABLE med_plan_items AUTO_INCREMENT = 8;
-- ALTER TABLE med_today_tasks AUTO_INCREMENT = 21;
-- ALTER TABLE med_checkins AUTO_INCREMENT = 12;
-- ALTER TABLE med_adherence_stats AUTO_INCREMENT = 5;
-- ALTER TABLE med_reports AUTO_INCREMENT = 3;
-- ALTER TABLE med_notifications AUTO_INCREMENT = 7;
