package com.awp.mapper;

import com.awp.dto.CategoryStatVO;
import com.awp.dto.SummaryVO;
import com.awp.dto.TrendVO;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;

/**
 * 统计相关查询。均按 user_id 隔离，日期范围可选。
 */
public interface StatMapper {

    /** 收支汇总 */
    SummaryVO summary(@Param("userId") Long userId,
                      @Param("startDate") LocalDate startDate,
                      @Param("endDate") LocalDate endDate);

    /** 按分类汇总金额（指定类型：0 支出 / 1 收入） */
    List<CategoryStatVO> categoryStat(@Param("userId") Long userId,
                                      @Param("type") Integer type,
                                      @Param("startDate") LocalDate startDate,
                                      @Param("endDate") LocalDate endDate);

    /** 按月汇总收入与支出 */
    List<TrendVO> trend(@Param("userId") Long userId,
                        @Param("startDate") LocalDate startDate,
                        @Param("endDate") LocalDate endDate);
}
