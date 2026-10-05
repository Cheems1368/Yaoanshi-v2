package com.medicine.repository.notification;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface NotificationCommitGateMapper {
    @Select("SELECT id FROM notification_commit_gate WHERE id = 1 FOR UPDATE")
    Integer lock();
}
