package com.example.system.service.iml;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.system.configuration.constants.Constants;
import com.example.system.domain.DictData;
import com.example.system.dto.DictDataDto;
import com.example.system.mapper.DictDataMapper;
import com.example.system.service.DictDataService;
import com.example.system.vo.DataGridView;
import com.example.system.service.DictTypeService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

/**
* @Author:
*/

@Service
public class DictDataServiceImpl implements DictDataService {

    @Autowired
    private DictDataMapper dictDataMapper;

    @Autowired
    private DictTypeService dictTypeService;


    @Override
    public DataGridView listPage(DictDataDto dictDataDto) {
        Page<DictData> page=new Page<>(dictDataDto.getPageNum(),dictDataDto.getPageSize());
        QueryWrapper<DictData> qw=new QueryWrapper<>();
        qw.eq(StringUtils.isNotBlank(dictDataDto.getDictType()),DictData.COL_DICT_TYPE,dictDataDto.getDictType());
        qw.like(StringUtils.isNotBlank(dictDataDto.getDictLabel()),DictData.COL_DICT_LABEL,dictDataDto.getDictLabel());
        qw.eq(StringUtils.isNotBlank(dictDataDto.getStatus()),DictData.COL_STATUS,dictDataDto.getStatus());

        this.dictDataMapper.selectPage(page,qw);
        return new DataGridView(page.getTotal(),page.getRecords());
    }

    @Override
    public int insert(DictDataDto dictDataDto) {
        DictData dictData=new DictData();
        BeanUtil.copyProperties(dictDataDto,dictData);
        //设置创建者，创建时间
        dictData.setCreateBy(dictDataDto.getSimpleUser().getUserName());
        dictData.setCreateTime(DateUtil.date());
        return this.dictDataMapper.insert(dictData);
    }

    @Override
    public int update(DictDataDto dictDataDto) {
        DictData dictData=new DictData();
        BeanUtil.copyProperties(dictDataDto,dictData);
        //设置修改人
        dictData.setUpdateBy(dictDataDto.getSimpleUser().getUserName());
        return this.dictDataMapper.updateById(dictData);
    }

    @Override
    public int deleteDictDataByIds(Long[] dictCodeIds) {
        List<Long> ids= Arrays.asList(dictCodeIds);
        if(null!=ids&&ids.size()>0){
            return this.dictDataMapper.deleteBatchIds(ids);
        }else{
            return -1;
        }
    }

    /**
     * 之前是从数据库里面查询
     * 因为我们做到redis的缓存，所以 现在要去redis里面去查询
     * @param dictType
     * @return
     */
    @Override
    public List<DictData> selectDictDataByDictType(String dictType) {
        String key=Constants.DICT_REDIS_PROFIX+dictType;
        List<DictData> dictDatas= dictTypeService.dictCacheAsync().get(key);
        return dictDatas;
    }

    @Override
    public DictData selectDictDataById(Long dictCode) {
        return this.dictDataMapper.selectById(dictCode);
    }
}
