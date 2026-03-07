package com.sqlapk.medicine.module.medication.service.integration;

import com.sqlapk.medicine.module.system.api.permission.PermissionApi;
import com.sqlapk.medicine.module.system.api.user.AdminUserApi;
import com.sqlapk.medicine.module.system.api.user.dto.AdminUserRespDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

/**
 * 用户权限集成服务
 */
@Slf4j
@Service
public class UserPermissionIntegrationService {

    @Autowired
    private AdminUserApi adminUserApi;

    @Autowired
    private PermissionApi permissionApi;

    /**
     * 获取用户信息
     */
    public AdminUserRespDTO getUserInfo(Long userId) {
        try {
            log.debug("获取用户信息: {}", userId);
            return adminUserApi.getUser(userId);
        } catch (Exception e) {
            log.error("获取用户信息失败: {}, 错误: {}", userId, e.getMessage());
            return null;
        }
    }

    /**
     * 验证用户是否存在
     */
    public boolean validateUserExists(Long userId) {
        try {
            AdminUserRespDTO user = adminUserApi.getUser(userId);
            return user != null;
        } catch (Exception e) {
            log.error("验证用户存在性失败: {}, 错误: {}", userId, e.getMessage());
            return false;
        }
    }

    /**
     * 检查用户权限
     */
    public boolean hasPermission(Long userId, String permission) {
        try {
            log.debug("检查用户权限: {}, 权限: {}", userId, permission);
            return permissionApi.hasAnyPermissions(userId, permission);
        } catch (Exception e) {
            log.error("检查用户权限失败: {}, 权限: {}, 错误: {}", userId, permission, e.getMessage());
            return false;
        }
    }

    /**
     * 获取用户权限列表（模拟实现）
     */
    public Set<String> getUserPermissions(Long userId) {
        try {
            log.debug("获取用户权限列表: {}", userId);
            // PermissionApi中没有getUserPermissions方法，返回空集合
            return Set.of();
        } catch (Exception e) {
            log.error("获取用户权限列表失败: {}, 错误: {}", userId, e.getMessage());
            return Set.of();
        }
    }

    /**
     * 获取用户角色列表（模拟实现）
     */
    public Set<String> getUserRoles(Long userId) {
        try {
            log.debug("获取用户角色列表: {}", userId);
            // PermissionApi中没有getUserRoles方法，返回空集合
            return Set.of();
        } catch (Exception e) {
            log.error("获取用户角色列表失败: {}, 错误: {}", userId, e.getMessage());
            return Set.of();
        }
    }

    /**
     * 检查用户是否为管理员
     */
    public boolean isAdmin(Long userId) {
        try {
            Set<String> roles = getUserRoles(userId);
            return roles.contains("admin") || roles.contains("super_admin");
        } catch (Exception e) {
            log.error("检查用户管理员身份失败: {}, 错误: {}", userId, e.getMessage());
            return false;
        }
    }

    /**
     * 检查用户是否为医生
     */
    public boolean isDoctor(Long userId) {
        try {
            Set<String> roles = getUserRoles(userId);
            return roles.contains("doctor") || roles.contains("physician");
        } catch (Exception e) {
            log.error("检查用户医生身份失败: {}, 错误: {}", userId, e.getMessage());
            return false;
        }
    }

    /**
     * 检查用户是否为药师
     */
    public boolean isPharmacist(Long userId) {
        try {
            Set<String> roles = getUserRoles(userId);
            return roles.contains("pharmacist");
        } catch (Exception e) {
            log.error("检查用户药师身份失败: {}, 错误: {}", userId, e.getMessage());
            return false;
        }
    }

    /**
     * 检查用户是否为护士
     */
    public boolean isNurse(Long userId) {
        try {
            Set<String> roles = getUserRoles(userId);
            return roles.contains("nurse");
        } catch (Exception e) {
            log.error("检查用户护士身份失败: {}, 错误: {}", userId, e.getMessage());
            return false;
        }
    }

    /**
     * 检查用户是否可以查看患者信息
     */
    public boolean canViewPatientInfo(Long userId, Long patientId) {
        try {
            // 管理员可以查看所有患者信息
            if (isAdmin(userId)) {
                return true;
            }

            // 医生和药师可以查看患者信息
            if (isDoctor(userId) || isPharmacist(userId)) {
                return true;
            }

            // 检查是否有特定权限
            return hasPermission(userId, "patient:view") || 
                   hasPermission(userId, "patient:list:query");
        } catch (Exception e) {
            log.error("检查用户患者信息查看权限失败: {}, 患者: {}, 错误: {}", userId, patientId, e.getMessage());
            return false;
        }
    }

    /**
     * 检查用户是否可以修改患者信息
     */
    public boolean canModifyPatientInfo(Long userId, Long patientId) {
        try {
            // 管理员可以修改所有患者信息
            if (isAdmin(userId)) {
                return true;
            }

            // 医生可以修改患者信息
            if (isDoctor(userId)) {
                return true;
            }

            // 检查是否有特定权限
            return hasPermission(userId, "patient:modify") || 
                   hasPermission(userId, "patient:list:update");
        } catch (Exception e) {
            log.error("检查用户患者信息修改权限失败: {}, 患者: {}, 错误: {}", userId, patientId, e.getMessage());
            return false;
        }
    }

    /**
     * 检查用户是否可以查看用药计划
     */
    public boolean canViewMedicationPlan(Long userId, Long patientId) {
        try {
            // 管理员可以查看所有用药计划
            if (isAdmin(userId)) {
                return true;
            }

            // 医生和药师可以查看用药计划
            if (isDoctor(userId) || isPharmacist(userId)) {
                return true;
            }

            // 检查是否有特定权限
            return hasPermission(userId, "medication:view") || 
                   hasPermission(userId, "medication:plan:query");
        } catch (Exception e) {
            log.error("检查用户用药计划查看权限失败: {}, 患者: {}, 错误: {}", userId, patientId, e.getMessage());
            return false;
        }
    }

    /**
     * 检查用户是否可以修改用药计划
     */
    public boolean canModifyMedicationPlan(Long userId, Long patientId) {
        try {
            // 管理员可以修改所有用药计划
            if (isAdmin(userId)) {
                return true;
            }

            // 医生可以修改用药计划
            if (isDoctor(userId)) {
                return true;
            }

            // 检查是否有特定权限
            return hasPermission(userId, "medication:modify") || 
                   hasPermission(userId, "medication:plan:update");
        } catch (Exception e) {
            log.error("检查用户用药计划修改权限失败: {}, 患者: {}, 错误: {}", userId, patientId, e.getMessage());
            return false;
        }
    }

    /**
     * 检查用户是否可以查看报告
     */
    public boolean canViewReport(Long userId, Long patientId) {
        try {
            // 管理员可以查看所有报告
            if (isAdmin(userId)) {
                return true;
            }

            // 医生和药师可以查看报告
            if (isDoctor(userId) || isPharmacist(userId)) {
                return true;
            }

            // 检查是否有特定权限
            return hasPermission(userId, "report:view") || 
                   hasPermission(userId, "medication:report:query");
        } catch (Exception e) {
            log.error("检查用户报告查看权限失败: {}, 患者: {}, 错误: {}", userId, patientId, e.getMessage());
            return false;
        }
    }

    /**
     * 检查用户是否可以生成报告
     */
    public boolean canGenerateReport(Long userId, Long patientId) {
        try {
            // 管理员可以生成所有报告
            if (isAdmin(userId)) {
                return true;
            }

            // 医生可以生成报告
            if (isDoctor(userId)) {
                return true;
            }

            // 检查是否有特定权限
            return hasPermission(userId, "report:generate") || 
                   hasPermission(userId, "medication:report:create");
        } catch (Exception e) {
            log.error("检查用户报告生成权限失败: {}, 患者: {}, 错误: {}", userId, patientId, e.getMessage());
            return false;
        }
    }

    /**
     * 获取用户专业信息
     */
    public UserProfessionalInfo getUserProfessionalInfo(Long userId) {
        try {
            AdminUserRespDTO user = adminUserApi.getUser(userId);
            if (user == null) {
                return null;
            }

            UserProfessionalInfo info = new UserProfessionalInfo();
            info.setUserId(userId);
            info.setUsername(user.getUsername());
            info.setNickname(user.getNickname());
            info.setMobile(user.getMobile());
            info.setDeptId(user.getDeptId());
            info.setPostIds(user.getPostIds() != null ? List.copyOf(user.getPostIds()) : null);
            info.setRoles(getUserRoles(userId));
            info.setPermissions(getUserPermissions(userId));
            info.setIsAdmin(isAdmin(userId));
            info.setIsDoctor(isDoctor(userId));
            info.setIsPharmacist(isPharmacist(userId));
            info.setIsNurse(isNurse(userId));

            return info;
        } catch (Exception e) {
            log.error("获取用户专业信息失败: {}, 错误: {}", userId, e.getMessage());
            return null;
        }
    }

    /**
     * 用户专业信息
     */
    public static class UserProfessionalInfo {
        private Long userId;
        private String username;
        private String nickname;
        private String email;
        private String mobile;
        private Long deptId;
        private List<Long> postIds;
        private Set<String> roles;
        private Set<String> permissions;
        private boolean isAdmin;
        private boolean isDoctor;
        private boolean isPharmacist;
        private boolean isNurse;

        // Getters and Setters
        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }

        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }

        public String getNickname() { return nickname; }
        public void setNickname(String nickname) { this.nickname = nickname; }

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }

        public String getMobile() { return mobile; }
        public void setMobile(String mobile) { this.mobile = mobile; }

        public Long getDeptId() { return deptId; }
        public void setDeptId(Long deptId) { this.deptId = deptId; }

        public List<Long> getPostIds() { 
            return postIds != null ? new java.util.ArrayList<>(postIds) : null; 
        }
        public void setPostIds(List<Long> postIds) { 
            this.postIds = postIds != null ? new java.util.ArrayList<>(postIds) : null; 
        }

        public Set<String> getRoles() { 
            return roles != null ? new java.util.HashSet<>(roles) : null; 
        }
        public void setRoles(Set<String> roles) { 
            this.roles = roles != null ? new java.util.HashSet<>(roles) : null; 
        }

        public Set<String> getPermissions() { 
            return permissions != null ? new java.util.HashSet<>(permissions) : null; 
        }
        public void setPermissions(Set<String> permissions) { 
            this.permissions = permissions != null ? new java.util.HashSet<>(permissions) : null; 
        }

        public boolean isAdmin() { return isAdmin; }
        public void setIsAdmin(boolean admin) { isAdmin = admin; }

        public boolean isDoctor() { return isDoctor; }
        public void setIsDoctor(boolean doctor) { isDoctor = doctor; }

        public boolean isPharmacist() { return isPharmacist; }
        public void setIsPharmacist(boolean pharmacist) { isPharmacist = pharmacist; }

        public boolean isNurse() { return isNurse; }
        public void setIsNurse(boolean nurse) { isNurse = nurse; }
    }
}
