package com.sqlapk.medicine.module.system.convert.user;

import com.sqlapk.medicine.module.system.api.user.dto.AdminUserRespDTO;
import com.sqlapk.medicine.module.system.controller.admin.user.vo.profile.UserProfileRespVO;
import com.sqlapk.medicine.module.system.controller.admin.user.vo.profile.UserProfileUpdatePasswordReqVO;
import com.sqlapk.medicine.module.system.controller.admin.user.vo.profile.UserProfileUpdateReqVO;
import com.sqlapk.medicine.module.system.controller.admin.user.vo.user.UserCreateReqVO;
import com.sqlapk.medicine.module.system.controller.admin.user.vo.user.UserExcelVO;
import com.sqlapk.medicine.module.system.controller.admin.user.vo.user.UserImportExcelVO;
import com.sqlapk.medicine.module.system.controller.admin.user.vo.user.UserPageItemRespVO;
import com.sqlapk.medicine.module.system.controller.admin.user.vo.user.UserSimpleRespVO;
import com.sqlapk.medicine.module.system.controller.admin.user.vo.user.UserUpdateReqVO;
import com.sqlapk.medicine.module.system.dal.dataobject.dept.DeptDO;
import com.sqlapk.medicine.module.system.dal.dataobject.dept.PostDO;
import com.sqlapk.medicine.module.system.dal.dataobject.permission.RoleDO;
import com.sqlapk.medicine.module.system.dal.dataobject.user.AdminUserDO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper
public interface UserConvert {

    UserConvert INSTANCE = Mappers.getMapper(UserConvert.class);

    UserPageItemRespVO convert(AdminUserDO bean);

    UserPageItemRespVO.Dept convert(DeptDO bean);

    AdminUserDO convert(UserCreateReqVO bean);

    AdminUserDO convert(UserUpdateReqVO bean);

    UserExcelVO convert02(AdminUserDO bean);

    AdminUserDO convert(UserImportExcelVO bean);

    UserProfileRespVO convert03(AdminUserDO bean);

    List<UserProfileRespVO.Role> convertList(List<RoleDO> list);

    UserProfileRespVO.Dept convert02(DeptDO bean);

    AdminUserDO convert(UserProfileUpdateReqVO bean);

    AdminUserDO convert(UserProfileUpdatePasswordReqVO bean);

    List<UserProfileRespVO.Post> convertList02(List<PostDO> list);



    List<UserSimpleRespVO> convertList04(List<AdminUserDO> list);

    AdminUserRespDTO convert4(AdminUserDO bean);

    List<AdminUserRespDTO> convertList4(List<AdminUserDO> users);

}
