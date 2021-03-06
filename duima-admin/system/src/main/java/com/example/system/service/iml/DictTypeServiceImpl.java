package com.example.system.service.iml;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.system.configuration.constants.Constants;
import com.example.system.domain.DictData;
import com.example.system.domain.DictType;
import com.example.system.dto.DictTypeDto;
import com.example.system.mapper.DictDataMapper;
import com.example.system.mapper.DictTypeMapper;
import com.example.system.service.DictTypeService;
import com.example.system.vo.DataGridView;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.cache.annotation.Cacheable;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Author:
 */

@Service
public class DictTypeServiceImpl implements DictTypeService {

    @Autowired
    private DictTypeMapper dictTypeMapper;

    @Autowired
    private DictDataMapper dictDataMapper;


    @Override
    public DataGridView listPage(DictTypeDto dictTypeDto) {
        Page<DictType> page = new Page<>(dictTypeDto.getPageNum(), dictTypeDto.getPageSize());
        QueryWrapper<DictType> qw = new QueryWrapper<>();
        qw.like(StringUtils.isNotBlank(dictTypeDto.getDictName()), DictType.COL_DICT_NAME, dictTypeDto.getDictName());
        qw.like(StringUtils.isNotBlank(dictTypeDto.getDictType()), DictType.COL_DICT_TYPE, dictTypeDto.getDictType());
        qw.eq(StringUtils.isNotBlank(dictTypeDto.getStatus()), DictType.COL_STATUS, dictTypeDto.getStatus());

        qw.ge(dictTypeDto.getBeginTime() != null, DictType.COL_CREATE_TIME, dictTypeDto.getBeginTime());
        qw.le(dictTypeDto.getEndTime() != null, DictType.COL_CREATE_TIME, dictTypeDto.getEndTime());
        this.dictTypeMapper.selectPage(page, qw);
        return new DataGridView(page.getTotal(), page.getRecords());
    }

    @Override
    public DataGridView list() {
        QueryWrapper<DictType> qw = new QueryWrapper<>();
        qw.eq(DictType.COL_STATUS, Constants.STATUS_TRUE);
        return new DataGridView(null, this.dictTypeMapper.selectList(qw));
    }

    @Override
    public Boolean checkDictTypeUnique(Long dictId, String dictType) {
        dictId = (dictId == null) ? -1L : dictId;
        QueryWrapper<DictType> qw = new QueryWrapper<>();
        qw.eq(DictType.COL_DICT_TYPE, dictType);
        DictType sysDictType = this.dictTypeMapper.selectOne(qw);
        if (null != sysDictType && dictId.longValue() != sysDictType.getDictId().longValue()) {
            return true; //???????????????
        }
        return false; //????????????
    }

    @Override
    public int insert(DictTypeDto dictTypeDto) {
        DictType dictType = new DictType();
        BeanUtil.copyProperties(dictTypeDto, dictType);
        //?????????????????????????????????
        dictType.setCreateBy(dictTypeDto.getSimpleUser().getUserName());
        dictType.setCreateTime(DateUtil.date());
        return this.dictTypeMapper.insert(dictType);
    }

    @Override
    public int update(DictTypeDto dictTypeDto) {
        DictType dictType = new DictType();
        BeanUtil.copyProperties(dictTypeDto, dictType);
        //???????????????
        dictType.setUpdateBy(dictTypeDto.getSimpleUser().getUserName());
        return this.dictTypeMapper.updateById(dictType);
    }

    @Override
    public int deleteDictTypeByIds(Long[] dictIds) {
        List<Long> ids = Arrays.asList(dictIds);
        if (null != ids && ids.size() > 0) {
            return this.dictTypeMapper.deleteBatchIds(ids);
        } else {
            return -1;
        }
    }

    @Override
    public DictType selectDictTypeById(Long dictId) {
        return this.dictTypeMapper.selectById(dictId);
    }

    /**
     * ?????????????????????
     * 1?????????????????????????????????????????????
     * 2?????????????????????????????????????????????
     * 3????????????????????????json??????redis
     * ??????key
     * dict:dictType
     * ???dict:sys_user_sex --->[{},{},{}]
     */
    @Cacheable(value = "dict", key = "'dict'")
    @Override
    public Map<String, List<DictData>> dictCacheAsync() {
        //??????????????????????????????????????????
        QueryWrapper<DictType> qw = new QueryWrapper<>();
        qw.eq(DictType.COL_STATUS, Constants.STATUS_TRUE);
        List<DictType> dictTypes = this.dictTypeMapper.selectList(qw);
        Map<String, List<DictData>> map = new HashMap<>();
        for (DictType dictType : dictTypes) {
            QueryWrapper<DictData> qdw = new QueryWrapper<>();
            qdw.eq(DictData.COL_STATUS, Constants.STATUS_TRUE);
            qdw.eq(DictData.COL_DICT_TYPE, dictType.getDictType());
            qdw.orderByAsc(DictData.COL_DICT_SORT);
            List<DictData> dictDataList = dictDataMapper.selectList(qdw);
            map.put(Constants.DICT_REDIS_PROFIX + dictType.getDictType(), dictDataList);
        }
        return map;
    }
}
