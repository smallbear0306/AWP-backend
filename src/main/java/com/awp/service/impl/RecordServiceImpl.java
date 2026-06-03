package com.awp.service.impl;

import com.awp.common.BusinessException;
import com.awp.common.PageResult;
import com.awp.common.ResultCode;
import com.awp.common.UserContext;
import com.awp.dto.RecordDTO;
import com.awp.dto.RecordQuery;
import com.awp.dto.RecordVO;
import com.awp.entity.Category;
import com.awp.entity.Record;
import com.awp.mapper.CategoryMapper;
import com.awp.mapper.RecordMapper;
import com.awp.service.RecordService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 账单业务实现。所有操作绑定当前登录用户。
 */
@Service
public class RecordServiceImpl implements RecordService {

    private final RecordMapper recordMapper;
    private final CategoryMapper categoryMapper;

    public RecordServiceImpl(RecordMapper recordMapper, CategoryMapper categoryMapper) {
        this.recordMapper = recordMapper;
        this.categoryMapper = categoryMapper;
    }

    @Override
    public PageResult<RecordVO> page(RecordQuery query) {
        Long userId = UserContext.getUserId();
        long total = recordMapper.count(query, userId);
        List<RecordVO> list = total == 0 ? List.of() : recordMapper.page(query, userId);
        return new PageResult<>(total, list);
    }

    @Override
    public void create(RecordDTO dto) {
        Long userId = UserContext.getUserId();
        validateCategory(dto.getCategoryId(), userId);
        Record record = new Record();
        record.setUserId(userId);
        copy(dto, record);
        recordMapper.insert(record);
    }

    @Override
    public void update(Long id, RecordDTO dto) {
        Long userId = UserContext.getUserId();
        if (recordMapper.findByIdAndUser(id, userId) == null) {
            throw new BusinessException(ResultCode.NOT_FOUND);
        }
        validateCategory(dto.getCategoryId(), userId);
        Record record = new Record();
        record.setId(id);
        record.setUserId(userId);
        copy(dto, record);
        recordMapper.update(record);
    }

    @Override
    public void delete(Long id) {
        Long userId = UserContext.getUserId();
        if (recordMapper.findByIdAndUser(id, userId) == null) {
            throw new BusinessException(ResultCode.NOT_FOUND);
        }
        recordMapper.deleteByIdAndUser(id, userId);
    }

    /** 校验分类归属当前用户 */
    private void validateCategory(Long categoryId, Long userId) {
        Category category = categoryMapper.findByIdAndUser(categoryId, userId);
        if (category == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "分类不存在");
        }
    }

    private void copy(RecordDTO dto, Record record) {
        record.setCategoryId(dto.getCategoryId());
        record.setType(dto.getType());
        record.setAmount(dto.getAmount());
        record.setRemark(dto.getRemark());
        record.setRecordDate(dto.getRecordDate());
    }
}
