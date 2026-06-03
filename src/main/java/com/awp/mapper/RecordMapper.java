package com.awp.mapper;

import com.awp.dto.RecordQuery;
import com.awp.dto.RecordVO;
import com.awp.entity.Record;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 账单流水数据访问。所有操作均按 user_id 隔离。
 */
public interface RecordMapper {

    /** 分页查询（带分类名） */
    List<RecordVO> page(@Param("q") RecordQuery query, @Param("userId") Long userId);

    /** 查询条数（与 page 同条件） */
    long count(@Param("q") RecordQuery query, @Param("userId") Long userId);

    /** 按 id + userId 查询 */
    Record findByIdAndUser(@Param("id") Long id, @Param("userId") Long userId);

    int insert(Record record);

    int update(Record record);

    int deleteByIdAndUser(@Param("id") Long id, @Param("userId") Long userId);
}
