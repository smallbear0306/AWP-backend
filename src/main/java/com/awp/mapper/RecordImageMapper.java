package com.awp.mapper;

import com.awp.dto.RecordImage;
import org.apache.ibatis.annotations.Param;

/**
 * 账单截图数据访问。
 */
public interface RecordImageMapper {

    int insert(@Param("recordId") Long recordId,
               @Param("content") byte[] content,
               @Param("contentType") String contentType);

    RecordImage findByRecordId(@Param("recordId") Long recordId);

    int deleteByRecordId(@Param("recordId") Long recordId);
}
