package com.phms.service;

import com.phms.pojo.MedicineRecord;

import java.util.List;

public interface MedicineRecordService {
    void update(MedicineRecord medicineRecord);

    void add(MedicineRecord medicineRecord);

    void deleteById(Long id);

    Object getAllByLimit(MedicineRecord medicineRecord);

    List<MedicineRecord> getByDiagnosisId(Long diagnosisId);
}

