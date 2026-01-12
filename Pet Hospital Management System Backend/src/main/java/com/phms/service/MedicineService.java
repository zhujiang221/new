package com.phms.service;

import com.phms.pojo.Medicine;

public interface MedicineService {
    void update(Medicine medicine);

    void add(Medicine medicine);

    void deleteById(Long id);

    Object getAllByLimit(Medicine medicine);

    Medicine getById(Long id);
}

