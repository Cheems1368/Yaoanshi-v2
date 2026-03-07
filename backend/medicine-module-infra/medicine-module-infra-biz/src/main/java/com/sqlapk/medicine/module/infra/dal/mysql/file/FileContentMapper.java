package com.sqlapk.medicine.module.infra.dal.mysql.file;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sqlapk.medicine.module.infra.dal.dataobject.file.FileContentDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface FileContentMapper extends BaseMapper<FileContentDO> {
}
