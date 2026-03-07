package com.sqlapk.medicine.module.system.convert.dept;

import com.sqlapk.medicine.framework.common.pojo.PageResult;
import com.sqlapk.medicine.module.system.controller.admin.dept.vo.post.PostCreateReqVO;
import com.sqlapk.medicine.module.system.controller.admin.dept.vo.post.PostExcelVO;
import com.sqlapk.medicine.module.system.controller.admin.dept.vo.post.PostRespVO;
import com.sqlapk.medicine.module.system.controller.admin.dept.vo.post.PostSimpleRespVO;
import com.sqlapk.medicine.module.system.controller.admin.dept.vo.post.PostUpdateReqVO;
import com.sqlapk.medicine.module.system.dal.dataobject.dept.PostDO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper
public interface PostConvert {

    PostConvert INSTANCE = Mappers.getMapper(PostConvert.class);

    List<PostSimpleRespVO> convertList02(List<PostDO> list);

    PageResult<PostRespVO> convertPage(PageResult<PostDO> page);

    PostRespVO convert(PostDO id);

    PostDO convert(PostCreateReqVO bean);

    PostDO convert(PostUpdateReqVO reqVO);

    List<PostExcelVO> convertList03(List<PostDO> list);

}
