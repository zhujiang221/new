package com.phms.service.impl;

import com.phms.mapper.MedicineRecordMapper;
import com.phms.model.MMGridPageVoBean;
import com.phms.pojo.MedicineRecord;
import com.phms.pojo.Pet;
import com.phms.pojo.User;
import com.phms.pojo.Medicine;
import com.phms.service.MedicineRecordService;
import com.phms.service.PetService;
import com.phms.service.UserService;
import com.phms.service.MedicineService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@Service
public class MedicineRecordServiceImpl implements MedicineRecordService {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    @Resource
    MedicineRecordMapper medicineRecordMapper;
    @Resource
    UserService userService;
    @Resource
    PetService petService;
    @Resource
    MedicineService medicineService;

    @Override
    public Object getAllByLimit(MedicineRecord medicineRecord) {
        int size = 0;
        // 计算分页
        Integer begin = (medicineRecord.getPage() - 1) * medicineRecord.getLimit();
        medicineRecord.setPage(begin);

        List<MedicineRecord> rows = new ArrayList<>();
        List<MedicineRecord> result = new ArrayList<>();
        try {
            rows = medicineRecordMapper.getAllByLimit(medicineRecord);
            size = medicineRecordMapper.countAllByLimit(medicineRecord);
            
            // 关联查询相关信息
            for (MedicineRecord mr : rows) {
                if (mr.getPetId() != null) {
                    Pet pet = petService.selectByPrimaryKey(mr.getPetId());
                    if (pet != null) {
                        mr.setPetName(pet.getName());
                    }
                }
                if (mr.getUserId() != null) {
                    User user = userService.selectByPrimaryKey(mr.getUserId());
                    if (user != null) {
                        mr.setUserName(user.getName());
                    }
                }
                if (mr.getDoctorId() != null) {
                    User doctor = userService.selectByPrimaryKey(mr.getDoctorId());
                    if (doctor != null) {
                        mr.setDoctorName(doctor.getName());
                    }
                }
                if (mr.getMedicineId() != null) {
                    Medicine medicine = medicineService.getById(mr.getMedicineId());
                    if (medicine != null) {
                        mr.setMedicineName(medicine.getName());
                        mr.setMedicinePrice(medicine.getPrice());
                    }
                }
                result.add(mr);
            }
        } catch (Exception e) {
            logger.error("根据条件查询异常", e);
        }
        MMGridPageVoBean<MedicineRecord> vo = new MMGridPageVoBean<>();
        vo.setTotal(size);
        vo.setRows(result);

        return vo;
    }

    @Override
    public void deleteById(Long id) {
        medicineRecordMapper.deleteByPrimaryKey(id);
    }

    @Override
    public void add(MedicineRecord medicineRecord) {
        medicineRecordMapper.insert(medicineRecord);
    }

    @Override
    public void update(MedicineRecord medicineRecord) {
        medicineRecordMapper.updateByPrimaryKeySelective(medicineRecord);
    }

    @Override
    public List<MedicineRecord> getByDiagnosisId(Long diagnosisId) {
        List<MedicineRecord> records = medicineRecordMapper.getByDiagnosisId(diagnosisId);
        // 关联查询相关信息
        for (MedicineRecord mr : records) {
            if (mr.getPetId() != null) {
                Pet pet = petService.selectByPrimaryKey(mr.getPetId());
                if (pet != null) {
                    mr.setPetName(pet.getName());
                }
            }
            if (mr.getUserId() != null) {
                User user = userService.selectByPrimaryKey(mr.getUserId());
                if (user != null) {
                    mr.setUserName(user.getName());
                }
            }
            if (mr.getDoctorId() != null) {
                User doctor = userService.selectByPrimaryKey(mr.getDoctorId());
                if (doctor != null) {
                    mr.setDoctorName(doctor.getName());
                }
            }
            if (mr.getMedicineId() != null) {
                Medicine medicine = medicineService.getById(mr.getMedicineId());
                if (medicine != null) {
                    mr.setMedicineName(medicine.getName());
                    mr.setMedicinePrice(medicine.getPrice());
                }
            }
        }
        return records;
    }
}

