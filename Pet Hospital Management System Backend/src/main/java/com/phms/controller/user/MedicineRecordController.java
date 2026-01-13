package com.phms.controller.user;

import com.phms.pojo.Medicine;
import com.phms.pojo.MedicineRecord;
import com.phms.pojo.User;
import com.phms.service.MedicineRecordService;
import com.phms.service.MedicineService;
import com.phms.service.UserService;
import com.phms.utils.UserContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Date;
import java.util.List;

/**
 * 开药记录Controller
 * 只有医生可以开具药品
 */
@Controller("UserMedicineRecordController")
@RequestMapping("/user/medicineRecord")
public class MedicineRecordController {
    @Autowired
    private MedicineRecordService medicineRecordService;
    @Autowired
    private MedicineService medicineService;
    @Autowired
    private UserService userService;
    
    @Autowired
    private UserContext userContext;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    /**
     * 普通用户查询开药记录
     */
    @RequestMapping("/getAllByLimit")
    @ResponseBody
    public Object getAllByLimit(MedicineRecord medicineRecord) {
        User user = userContext.getCurrentUser();
        if (user == null) {
            logger.error("获取当前用户失败，用户未登录");
            return "NOT_LOGIN";
        }
        if (user != null) {
            medicineRecord.setUserId(user.getId());
        }
        return medicineRecordService.getAllByLimit(medicineRecord);
    }

    /**
     * 医生查询开药记录
     */
    @RequestMapping("/getAllByLimitDoctor")
    @ResponseBody
    public Object getAllByLimitDoctor(MedicineRecord medicineRecord) {
        return medicineRecordService.getAllByLimit(medicineRecord);
    }

    /**
     * 根据诊断ID查询开药记录
     */
    @RequestMapping("/getByDiagnosisId")
    @ResponseBody
    public Object getByDiagnosisId(Long diagnosisId) {
        List<MedicineRecord> records = medicineRecordService.getByDiagnosisId(diagnosisId);
        return records;
    }

    /**
     * 开具药品（只有医生权限）
     */
    @RequestMapping(value = "/doAdd")
    @ResponseBody
    @Transactional
    public String doAdd(MedicineRecord medicineRecord) {
        User user = userContext.getCurrentUser();
        if (user == null) {
            logger.error("获取当前用户失败，用户未登录");
            return "NOT_LOGIN";
        }
        
        // 验证权限：只有医生(roleId=2)可以开药
        if (user == null) {
            return "LGINOUT";
        }
        Integer roleId = userService.getUserRoleId(user.getId());
        if (roleId == null) {
            roleId = 3; // 默认普通用户
        }
        if (roleId != 2) { // 2=医生
            return "ERROR:只有医生可以开具药品";
        }
        
        try {
            // 验证药品库存
            if (medicineRecord.getMedicineId() != null && medicineRecord.getQuantity() != null) {
                Medicine medicine = medicineService.getById(medicineRecord.getMedicineId());
                if (medicine == null) {
                    return "ERROR:药品不存在";
                }
                if (medicine.getStatus() == null || medicine.getStatus() != 1) {
                    return "ERROR:药品已下架";
                }
                if (medicine.getStock() == null || medicine.getStock() < medicineRecord.getQuantity()) {
                    return "ERROR:药品库存不足";
                }
                
                // 减少库存
                medicine.setStock(medicine.getStock() - medicineRecord.getQuantity());
                medicineService.update(medicine);
            }
            
            // 设置开药记录信息
            medicineRecord.setDoctorId(user.getId());
            medicineRecord.setCreateTime(new Date());
            medicineRecordService.add(medicineRecord);
            
            return "SUCCESS";
        } catch (Exception e) {
            logger.error("开具药品异常", e);
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return "ERROR";
        }
    }

    /**
     * 删除开药记录（只有医生权限）
     */
    @RequestMapping(value = "/del")
    @ResponseBody
    @Transactional
    public String del(Long id) {
        User user = userContext.getCurrentUser();
        if (user == null) {
            logger.error("获取当前用户失败，用户未登录");
            return "NOT_LOGIN";
        }
        
        // 验证权限：医生(roleId=2)或管理员(roleId=1)可以删除
        if (user == null) {
            return "LGINOUT";
        }
        Integer roleId = userService.getUserRoleId(user.getId());
        if (roleId == null) {
            roleId = 3; // 默认普通用户
        }
        if (roleId != 1 && roleId != 2) { // 1=管理员, 2=医生
            return "ERROR:无权限";
        }
        
        try {
            medicineRecordService.deleteById(id);
            return "SUCCESS";
        } catch (Exception e) {
            logger.error("删除开药记录异常", e);
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return "ERROR";
        }
    }
}

