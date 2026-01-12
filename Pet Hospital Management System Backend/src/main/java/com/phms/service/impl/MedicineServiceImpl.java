package com.phms.service.impl;

import com.phms.mapper.MedicineMapper;
import com.phms.model.MMGridPageVoBean;
import com.phms.pojo.Medicine;
import com.phms.service.MedicineService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@Service
public class MedicineServiceImpl implements MedicineService {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    @Resource
    MedicineMapper medicineMapper;

    @Override
    public Object getAllByLimit(Medicine medicine) {
        int size = 0;
        // 计算分页
        Integer begin = (medicine.getPage() - 1) * medicine.getLimit();
        medicine.setPage(begin);

        List<Medicine> rows = new ArrayList<>();
        try {
            rows = medicineMapper.getAllByLimit(medicine);
            size = medicineMapper.countAllByLimit(medicine);
        } catch (Exception e) {
            logger.error("根据条件查询异常", e);
        }
        MMGridPageVoBean<Medicine> vo = new MMGridPageVoBean<>();
        vo.setTotal(size);
        vo.setRows(rows);

        return vo;
    }

    @Override
    public void deleteById(Long id) {
        medicineMapper.deleteByPrimaryKey(id);
    }

    @Override
    public void add(Medicine medicine) {
        medicineMapper.insert(medicine);
    }

    @Override
    public void update(Medicine medicine) {
        medicineMapper.updateByPrimaryKeySelective(medicine);
    }

    @Override
    public Medicine getById(Long id) {
        return medicineMapper.selectByPrimaryKey(id);
    }
}

