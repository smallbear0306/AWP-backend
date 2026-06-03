package com.awp.service.impl;

import com.awp.common.UserContext;
import com.awp.dto.CategoryStatVO;
import com.awp.dto.SummaryVO;
import com.awp.dto.TrendVO;
import com.awp.mapper.StatMapper;
import com.awp.service.StatService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

/**
 * 统计业务实现。
 */
@Service
public class StatServiceImpl implements StatService {

    private final StatMapper statMapper;

    public StatServiceImpl(StatMapper statMapper) {
        this.statMapper = statMapper;
    }

    @Override
    public SummaryVO summary(LocalDate startDate, LocalDate endDate) {
        SummaryVO vo = statMapper.summary(UserContext.getUserId(), startDate, endDate);
        // 结余 = 收入 - 支出
        vo.setBalance(vo.getIncome().subtract(vo.getExpense()));
        return vo;
    }

    @Override
    public List<CategoryStatVO> categoryStat(Integer type, LocalDate startDate, LocalDate endDate) {
        // 默认统计支出占比
        int t = (type == null) ? 0 : type;
        return statMapper.categoryStat(UserContext.getUserId(), t, startDate, endDate);
    }

    @Override
    public List<TrendVO> trend(LocalDate startDate, LocalDate endDate) {
        return statMapper.trend(UserContext.getUserId(), startDate, endDate);
    }
}
