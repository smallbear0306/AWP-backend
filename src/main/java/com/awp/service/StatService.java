package com.awp.service;

import com.awp.dto.CategoryStatVO;
import com.awp.dto.SummaryVO;
import com.awp.dto.TrendVO;

import java.time.LocalDate;
import java.util.List;

/**
 * 统计业务接口。
 */
public interface StatService {

    SummaryVO summary(LocalDate startDate, LocalDate endDate);

    List<CategoryStatVO> categoryStat(Integer type, LocalDate startDate, LocalDate endDate);

    List<TrendVO> trend(LocalDate startDate, LocalDate endDate);
}
