package com.phms.mapper;

import com.phms.pojo.MedicineRecord;

import java.util.List;

public interface MedicineRecordMapper {
    /**
     * 根据主键删除
     */
    int deleteByPrimaryKey(Long id);

    /**
     * 插入记录
     */
    int insert(MedicineRecord record);

    /**
     * 选择性插入记录
     */
    int insertSelective(MedicineRecord record);

    /**
     * 根据主键查询
     */
    MedicineRecord selectByPrimaryKey(Long id);

    /**
     * 选择性更新
     */
    int updateByPrimaryKeySelective(MedicineRecord record);

    /**
     * 根据主键更新
     */
    int updateByPrimaryKey(MedicineRecord record);

    /**
     * 分页查询
     */
    List<MedicineRecord> getAllByLimit(MedicineRecord medicineRecord);

    /**
     * 分页查询总数
     */
    int countAllByLimit(MedicineRecord medicineRecord);

    /**
     * 根据诊断ID查询开药记录
     */
    List<MedicineRecord> getByDiagnosisId(Long diagnosisId);
}

