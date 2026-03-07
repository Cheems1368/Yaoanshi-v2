-- 工单系统权限配置

-- 1. 添加工单管理菜单
INSERT INTO system_menu (name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted, tenant_id) VALUES
('工单管理', '', 1, 10, 0, '/help-order', 'ep:document', NULL, NULL, 0, true, true, true, 'admin', NOW(), 'admin', NOW(), false, 1),
('工单列表', '', 2, 1, (SELECT id FROM system_menu WHERE name = '工单管理' AND parent_id = 0), 'help-order', 'ep:list', 'system/help-order/index', 'HelpOrder', 0, true, true, true, 'admin', NOW(), 'admin', NOW(), false, 1);

-- 2. 添加工单相关权限
INSERT INTO system_menu (name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted, tenant_id) VALUES
('工单查询', 'system:help-order:query', 3, 1, (SELECT id FROM system_menu WHERE name = '工单列表'), '', '', '', '', 0, true, true, true, 'admin', NOW(), 'admin', NOW(), false, 1),
('工单创建', 'system:help-order:create', 3, 2, (SELECT id FROM system_menu WHERE name = '工单列表'), '', '', '', '', 0, true, true, true, 'admin', NOW(), 'admin', NOW(), false, 1),
('工单更新', 'system:help-order:update', 3, 3, (SELECT id FROM system_menu WHERE name = '工单列表'), '', '', '', '', 0, true, true, true, 'admin', NOW(), 'admin', NOW(), false, 1),
('工单删除', 'system:help-order:delete', 3, 4, (SELECT id FROM system_menu WHERE name = '工单列表'), '', '', '', '', 0, true, true, true, 'admin', NOW(), 'admin', NOW(), false, 1),
('工单分配', 'system:help-order:assign', 3, 5, (SELECT id FROM system_menu WHERE name = '工单列表'), '', '', '', '', 0, true, true, true, 'admin', NOW(), 'admin', NOW(), false, 1),
('工单升级', 'system:help-order:escalate', 3, 6, (SELECT id FROM system_menu WHERE name = '工单列表'), '', '', '', '', 0, true, true, true, 'admin', NOW(), 'admin', NOW(), false, 1),
('工单驳回', 'system:help-order:reject', 3, 7, (SELECT id FROM system_menu WHERE name = '工单列表'), '', '', '', '', 0, true, true, true, 'admin', NOW(), 'admin', NOW(), false, 1),
('工单导出', 'system:help-order:export', 3, 8, (SELECT id FROM system_menu WHERE name = '工单列表'), '', '', '', '', 0, true, true, true, 'admin', NOW(), 'admin', NOW(), false, 1);

-- 3. 为管理员角色分配工单权限
INSERT INTO system_role_menu (role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id)
SELECT 
    1 as role_id, 
    id as menu_id, 
    'admin' as creator, 
    NOW() as create_time, 
    'admin' as updater, 
    NOW() as update_time, 
    false as deleted, 
    1 as tenant_id
FROM system_menu 
WHERE permission LIKE 'system:help-order:%' OR name IN ('工单管理', '工单列表');
