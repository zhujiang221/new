package com.phms.mapper;

import com.phms.pojo.Medicine;

import java.util.List;

public interface MedicineMapper {
    /**
     * 根据主键删除
     */
    int deleteByPrimaryKey(Long id);

    /**
     * 插入记录
     */
    int insert(Medicine record);

    /**
     * 选择性插入记录
     */
    int insertSelective(Medicine record);

    /**
     * 根据主键查询
     */
    Medicine selectByPrimaryKey(Long id);

    /**
     * 选择性更新
     */
    int updateByPrimaryKeySelective(Medicine record);

    /**
     * 根据主键更新
     */
    int updateByPrimaryKey(Medicine record);

    /**
     * 分页查询
     */
    List<Medicine> getAllByLimit(Medicine medicine);

    /**
     * 分页查询总数
     */
    int countAllByLimit(Medicine medicine);
}

