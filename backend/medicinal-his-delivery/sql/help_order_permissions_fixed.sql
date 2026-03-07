-- 工单系统权限配置

-- 1. 添加工单管理菜单
INSERT INTO system_menu (name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES
('工单管理', '', 1, 10, 0, '/help-order', 'ep:document', NULL, NULL, 0, true, true, true, 'admin', NOW(), 'admin', NOW(), false);

-- 获取刚插入的工单管理菜单ID
SET @help_order_menu_id = LAST_INSERT_ID();

-- 2. 添加工单列表子菜单
INSERT INTO system_menu (name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES
('工单列表', '', 2, 1, @help_order_menu_id, 'help-order', 'ep:list', 'system/help-order/index', 'HelpOrder', 0, true, true, true, 'admin', NOW(), 'admin', NOW(), false);

-- 获取工单列表菜单ID
SET @help_order_list_id = LAST_INSERT_ID();

-- 3. 添加工单相关权限
INSERT INTO system_menu (name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES
('工单查询', 'system:help-order:query', 3, 1, @help_order_list_id, '', '', '', '', 0, true, true, true, 'admin', NOW(), 'admin', NOW(), false),
('工单创建', 'system:help-order:create', 3, 2, @help_order_list_id, '', '', '', '', 0, true, true, true, 'admin', NOW(), 'admin', NOW(), false),
('工单更新', 'system:help-order:update', 3, 3, @help_order_list_id, '', '', '', '', 0, true, true, true, 'admin', NOW(), 'admin', NOW(), false),
('工单删除', 'system:help-order:delete', 3, 4, @help_order_list_id, '', '', '', '', 0, true, true, true, 'admin', NOW(), 'admin', NOW(), false),
('工单分配', 'system:help-order:assign', 3, 5, @help_order_list_id, '', '', '', '', 0, true, true, true, 'admin', NOW(), 'admin', NOW(), false),
('工单升级', 'system:help-order:escalate', 3, 6, @help_order_list_id, '', '', '', '', 0, true, true, true, 'admin', NOW(), 'admin', NOW(), false),
('工单驳回', 'system:help-order:reject', 3, 7, @help_order_list_id, '', '', '', '', 0, true, true, true, 'admin', NOW(), 'admin', NOW(), false),
('工单导出', 'system:help-order:export', 3, 8, @help_order_list_id, '', '', '', '', 0, true, true, true, 'admin', NOW(), 'admin', NOW(), false);

-- 4. 为管理员角色分配工单权限
INSERT INTO system_role_menu (role_id, menu_id, creator, create_time, updater, update_time, deleted)
SELECT 
    1 as role_id, 
    id as menu_id, 
    'admin' as creator, 
    NOW() as create_time, 
    'admin' as updater, 
    NOW() as update_time, 
    false as deleted
FROM system_menu 
WHERE permission LIKE 'system:help-order:%' OR name IN ('工单管理', '工单列表');
