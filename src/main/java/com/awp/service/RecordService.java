package com.awp.service;

import com.awp.common.PageResult;
import com.awp.dto.RecordDTO;
import com.awp.dto.RecordImage;
import com.awp.dto.RecordQuery;
import com.awp.dto.RecordVO;

/**
 * 账单业务接口。
 */
public interface RecordService {

    PageResult<RecordVO> page(RecordQuery query);

    void create(RecordDTO dto);

    void update(Long id, RecordDTO dto);

    void delete(Long id);

    /** 取某账单的截图（校验归属，无图返回 null） */
    RecordImage getImage(Long id);
}
