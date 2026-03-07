// 权限检查工具
export class PermissionChecker {
  private static instance: PermissionChecker;
  private permissions: string[] = [];
  private roles: string[] = [];

  static getInstance(): PermissionChecker {
    if (!PermissionChecker.instance) {
      PermissionChecker.instance = new PermissionChecker();
    }
    return PermissionChecker.instance;
  }

  // 设置用户权限
  setPermissions(permissions: string[], roles: string[]) {
    this.permissions = permissions || [];
    this.roles = roles || [];
    console.log('权限检查器 - 设置权限:', { permissions, roles });
  }

  // 检查是否有特定权限
  hasPermission(permission: string): boolean {
    const hasPermission = this.permissions.includes(permission) || this.permissions.includes('*:*:*');
    console.log(`权限检查 - ${permission}:`, hasPermission);
    return hasPermission;
  }

  // 检查是否有任一权限
  hasAnyPermission(permissions: string[]): boolean {
    const hasAny = permissions.some(permission => this.hasPermission(permission));
    console.log(`权限检查 - 任一权限 ${permissions}:`, hasAny);
    return hasAny;
  }

  // 检查是否有特定角色
  hasRole(role: string): boolean {
    const hasRole = this.roles.includes(role);
    console.log(`角色检查 - ${role}:`, hasRole);
    return hasRole;
  }

  // 检查患者相关权限
  canAccessPatientProfile(): boolean {
    return this.hasAnyPermission([
      'patient:profile:query',
      'patient:list:query',
      'system:user:query'
    ]);
  }

  // 检查患者创建权限
  canCreatePatient(): boolean {
    return this.hasAnyPermission([
      'patient:list:create',
      'patient:profile:create',
      'system:user:create'
    ]);
  }

  // 获取当前权限状态
  getPermissionStatus() {
    return {
      permissions: this.permissions,
      roles: this.roles,
      canAccessPatientProfile: this.canAccessPatientProfile(),
      canCreatePatient: this.canCreatePatient()
    };
  }

  // 清除权限
  clearPermissions() {
    this.permissions = [];
    this.roles = [];
    console.log('权限检查器 - 清除权限');
  }
}

// 导出单例实例
export const permissionChecker = PermissionChecker.getInstance();

