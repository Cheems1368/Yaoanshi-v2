package com.sqlapk.medicine.module.infra.dal.dataobject.file;

import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.sqlapk.medicine.framework.file.core.client.FileClientConfig;
import com.sqlapk.medicine.framework.file.core.enums.FileStorageEnum;
import com.sqlapk.medicine.framework.mybatis.core.dataobject.BaseDO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * 文件配置表
 *
 * @author 芋道源码
 */
@TableName(value = "infra_file_config", autoResultMap = true)
@KeySequence("infra_file_config_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
public class FileConfigDO extends BaseDO {

    /**
     * 配置编号，数据库自增
     */
    private Long id;
    /**
     * 配置名
     */
    private String name;
    /**
     * 存储器
     *
     * 枚举 {@link FileStorageEnum}
     */
    private Integer storage;
    /**
     * 备注
     */
    private String remark;
    /**
     * 是否为主配置
     *
     * 由于我们可以配置多个文件配置，默认情况下，使用主配置进行文件的上传
     */
    private Boolean master;

    /**
     * 支付渠道配置
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private FileClientConfig config;

    // 手写 getter/setter，避免 Lombok 编译未生效时导致方法缺失
    public Long getId() {
        return id;
    }
    public FileConfigDO setId(Long id) {
        this.id = id;
        return this;
    }
    public String getName() {
        return name;
    }
    public FileConfigDO setName(String name) {
        this.name = name;
        return this;
    }
    public Integer getStorage() {
        return storage;
    }
    public FileConfigDO setStorage(Integer storage) {
        this.storage = storage;
        return this;
    }
    public String getRemark() {
        return remark;
    }
    public FileConfigDO setRemark(String remark) {
        this.remark = remark;
        return this;
    }
    public Boolean getMaster() {
        return master;
    }
    public FileConfigDO setMaster(Boolean master) {
        this.master = master;
        return this;
    }
    public FileClientConfig getConfig() {
        return config;
    }
    public FileConfigDO setConfig(FileClientConfig config) {
        this.config = config;
        return this;
    }
}
