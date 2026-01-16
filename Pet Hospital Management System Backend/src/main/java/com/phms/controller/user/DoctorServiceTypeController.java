package com.phms.controller.user;

import com.phms.pojo.DoctorServiceType;
import com.phms.pojo.User;
import com.phms.service.DoctorServiceTypeService;
import com.phms.utils.UserContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

/**
 * 医生服务类型管理
 */
@Controller("DoctorServiceTypeController")
@RequestMapping("/doctor/serviceType")
public class DoctorServiceTypeController {
    @Autowired
    private DoctorServiceTypeService doctorServiceTypeService;
    
    @Autowired
    private UserContext UserContext;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    /**
     * 获取医生的服务类型列表
     */
    @RequestMapping("/list")
    @ResponseBody
    public Object getServiceTypes(Long doctorId) {
        User user = UserContext.getCurrentUser();
        if (user == null) {
            logger.error("获取当前用户失败，用户未登录");
            return "NOT_LOGIN";
        }
        
        // 如果不是管理员，只能看自己的
        if (user.getRole() != 1 && doctorId == null) {
            doctorId = user.getId();
        }
        
        try {
            List<DoctorServiceType> list = doctorServiceTypeService.getByDoctorId(doctorId);
            return list;
        } catch (Exception e) {
            logger.error("获取服务类型列表失败", e);
            return "ERROR";
        }
    }

    /**
     * 设置医生的服务类型
     * 参数：doctorId（可选，管理员可以指定，医生只能设置自己的）
     *      typeIds（数组，预约类型ID列表）
     */
    @RequestMapping(value = "/set", method = {RequestMethod.POST, RequestMethod.GET})
    @ResponseBody
    @Transactional
    public String setServiceTypes(@RequestParam(required = false) Long doctorId, 
                                   @RequestParam(required = false) Long[] typeIds) {
        User user = UserContext.getCurrentUser();
        if (user == null) {
            logger.error("获取当前用户失败，用户未登录");
            return "NOT_LOGIN";
        }
        
        // 如果不是管理员，只能设置自己的
        if (user.getRole() == null || user.getRole() != 1) {
            doctorId = user.getId();
        }
        
        if (doctorId == null) {
            logger.warn("设置服务类型失败：doctorId为空");
            return "noDoctorId";
        }
        
        // 将数组转换为List
        List<Long> typeIdList = null;
        if (typeIds != null && typeIds.length > 0) {
            typeIdList = new java.util.ArrayList<>();
            for (Long typeId : typeIds) {
                if (typeId != null) {
                    typeIdList.add(typeId);
                }
            }
        } else {
            typeIdList = new java.util.ArrayList<>();
        }
        
        try {
            logger.info("设置服务类型，doctorId: {}, typeIds: {}", doctorId, typeIdList);
            doctorServiceTypeService.setDoctorServiceTypes(doctorId, typeIdList);
            return "SUCCESS";
        } catch (Exception e) {
            logger.error("设置服务类型失败", e);
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return "ERROR: " + e.getMessage();
        }
    }
}
