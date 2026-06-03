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
        validateCategory(dto.getCategoryId(), dto.getType(), userId);
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
        validateCategory(dto.getCategoryId(), dto.getType(), userId);
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

    /** 校验分类：当前用户可见、必须是二级(叶子)、收支类型与账单一致 */
    private void validateCategory(Long categoryId, Integer type, Long userId) {
        Category category = categoryMapper.findVisible(categoryId, userId);
        if (category == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "分类不存在");
        }
        if (category.getParentId() == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "请选择具体的二级分类");
        }
        if (!category.getType().equals(type)) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "分类与收支类型不一致");
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
