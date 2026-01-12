package com.phms.controller.user;

import com.phms.pojo.Medicine;
import com.phms.pojo.User;
import com.phms.service.MedicineService;
import com.phms.service.UserService;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Date;


/**
 * 药品管理Controller
 * 医生和管理员可以管理药品
 */
@Controller("UserMedicineController")
@RequestMapping("/user/medicine")
public class MedicineController {
    @Autowired
    private MedicineService medicineService;
    @Autowired
    private UserService userService;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    /**
     * 分页查询药品列表
     */
    @RequestMapping("/getAllByLimit")
    @ResponseBody
    public Object getAllByLimit(Medicine medicine) {
        return medicineService.getAllByLimit(medicine);
    }

    /**
     * 根据ID获取药品详情
     */
    @RequestMapping("/getById")
    @ResponseBody
    public Object getById(Long id) {
        return medicineService.getById(id);
    }

    /**
     * 添加药品（医生或管理员权限）
     */
    @RequestMapping(value = "/doAdd")
    @ResponseBody
    @Transactional
    public String doAdd(Medicine medicine) {
        Subject subject = SecurityUtils.getSubject();
        User user = (User) subject.getPrincipal();
        
        // 验证权限：医生(roleId=2)或管理员(roleId=1)
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
            medicine.setCreateTime(new Date());
            medicine.setCreateBy(user.getId());
            // 默认状态为上架
            if (medicine.getStatus() == null) {
                medicine.setStatus(1);
            }
            medicineService.add(medicine);
            return "SUCCESS";
        } catch (Exception e) {
            logger.error("添加药品异常", e);
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return "ERROR";
        }
    }

    /**
     * 更新药品信息（医生或管理员权限）
     */
    @RequestMapping(value = "/update")
    @ResponseBody
    @Transactional
    public String update(Medicine medicine) {
        Subject subject = SecurityUtils.getSubject();
        User user = (User) subject.getPrincipal();
        
        // 验证权限：医生(roleId=2)或管理员(roleId=1)
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
            medicineService.update(medicine);
            return "SUCCESS";
        } catch (Exception e) {
            logger.error("更新药品异常", e);
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return "ERROR";
        }
    }

    /**
     * 删除药品（医生或管理员权限）
     */
    @RequestMapping(value = "/del")
    @ResponseBody
    @Transactional
    public String del(Long id) {
        Subject subject = SecurityUtils.getSubject();
        User user = (User) subject.getPrincipal();
        
        // 验证权限：医生(roleId=2)或管理员(roleId=1)
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
            medicineService.deleteById(id);
            return "SUCCESS";
        } catch (Exception e) {
            logger.error("删除药品异常", e);
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return "ERROR";
        }
    }
}

