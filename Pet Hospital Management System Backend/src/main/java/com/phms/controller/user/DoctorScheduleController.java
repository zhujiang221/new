package com.phms.controller.user;

import com.phms.pojo.DoctorSchedule;
import com.phms.pojo.User;
import com.phms.service.DoctorScheduleService;
import com.phms.utils.UserContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

/**
 * 医生排班管理
 * 医生可以管理自己的排班，管理员可以管理所有医生的排班
 */
@Controller("DoctorScheduleController")
@RequestMapping("/doctor/schedule")
public class DoctorScheduleController {
    @Autowired
    private DoctorScheduleService doctorScheduleService;
    
    @Autowired
    private UserContext UserContext;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    /**
     * 获取医生的排班列表（支持分页）
     * 医生只能看自己的，管理员可以指定doctorId查看任意医生的
     */
    @RequestMapping("/list")
    @ResponseBody
    public Object getScheduleList(Long doctorId, Integer page, Integer limit) {
        User user = UserContext.getCurrentUser();
        if (user == null) {
            logger.error("获取当前用户失败，用户未登录");
            return "NOT_LOGIN";
        }
        
        // 如果不是管理员，只能看自己的排班
        if (user != null && user.getRole() != null && user.getRole() != 1 && doctorId == null) {
            doctorId = user.getId();
        }
        
        // 如果提供了分页参数，使用分页查询
        if (page != null && limit != null && page > 0 && limit > 0) {
            try {
                return doctorScheduleService.getByDoctorIdWithLimit(doctorId, page, limit);
            } catch (Exception e) {
                logger.error("获取排班列表失败", e);
                return "ERROR";
            }
        }
        
        // 否则返回全部数据（兼容旧接口）
        try {
            List<DoctorSchedule> list = doctorScheduleService.getByDoctorId(doctorId);
            return list;
        } catch (Exception e) {
            logger.error("获取排班列表失败", e);
            return "ERROR";
        }
    }

    /**
     * 添加排班
     */
    @RequestMapping("/add")
    @ResponseBody
    @Transactional
    public String addSchedule(DoctorSchedule schedule) {
        User user = UserContext.getCurrentUser();
        if (user == null) {
            logger.error("获取当前用户失败，用户未登录");
            return "NOT_LOGIN";
        }
        
        // 如果不是管理员，只能给自己添加排班
        if (user.getRole() != 1) {
            schedule.setDoctorId(user.getId());
        }
        
        if (schedule.getDoctorId() == null) {
            return "noDoctorId";
        }
        
        try {
            doctorScheduleService.add(schedule);
            return "SUCCESS";
        } catch (Exception e) {
            logger.error("添加排班失败", e);
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return "ERROR";
        }
    }

    /**
     * 更新排班
     */
    @RequestMapping("/update")
    @ResponseBody
    @Transactional
    public String updateSchedule(DoctorSchedule schedule) {
        User user = UserContext.getCurrentUser();
        if (user == null) {
            logger.error("获取当前用户失败，用户未登录");
            return "NOT_LOGIN";
        }
        
        // 检查权限：如果不是管理员，只能修改自己的排班
        if (user.getRole() != 1) {
            DoctorSchedule old = doctorScheduleService.getById(schedule.getId());
            if (old == null || !old.getDoctorId().equals(user.getId())) {
                return "NO_PERMISSION";
            }
        }
        
        try {
            doctorScheduleService.update(schedule);
            return "SUCCESS";
        } catch (Exception e) {
            logger.error("更新排班失败", e);
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return "ERROR";
        }
    }

    /**
     * 删除排班
     */
    @RequestMapping("/delete")
    @ResponseBody
    @Transactional
    public String deleteSchedule(Long id) {
        User user = UserContext.getCurrentUser();
        if (user == null) {
            logger.error("获取当前用户失败，用户未登录");
            return "NOT_LOGIN";
        }
        
        // 检查权限
        if (user.getRole() != 1) {
            DoctorSchedule old = doctorScheduleService.getById(id);
            if (old == null || !old.getDoctorId().equals(user.getId())) {
                return "NO_PERMISSION";
            }
        }
        
        try {
            doctorScheduleService.deleteById(id);
            return "SUCCESS";
        } catch (Exception e) {
            logger.error("删除排班失败", e);
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return "ERROR";
        }
    }

    /**
     * 批量保存排班（用于一次性设置一周的排班）
     */
    @RequestMapping(value = "/batchSave", method = {RequestMethod.POST, RequestMethod.GET})
    @ResponseBody
    @Transactional
    public String batchSave(List<DoctorSchedule> schedules) {
        User user = UserContext.getCurrentUser();
        if (user == null) {
            logger.error("获取当前用户失败，用户未登录");
            return "NOT_LOGIN";
        }
        
        if (schedules == null || schedules.isEmpty()) {
            return "EMPTY";
        }
        
        // 如果不是管理员，确保所有排班都是自己的
        if (user.getRole() != 1) {
            for (DoctorSchedule schedule : schedules) {
                schedule.setDoctorId(user.getId());
            }
        }
        
        try {
            doctorScheduleService.batchSave(schedules);
            return "SUCCESS";
        } catch (Exception e) {
            logger.error("批量保存排班失败", e);
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return "ERROR";
        }
    }

    /**
     * 应用模板：先删除医生的所有排班，再批量添加新排班
     */
    @RequestMapping(value = "/applyTemplate", method = RequestMethod.POST)
    @ResponseBody
    @Transactional
    public String applyTemplate(@RequestParam(required = false) Long doctorId, 
                                @RequestBody List<DoctorSchedule> schedules) {
        User user = UserContext.getCurrentUser();
        if (user == null) {
            logger.error("获取当前用户失败，用户未登录");
            return "NOT_LOGIN";
        }
        
        // 如果不是管理员，只能给自己应用模板
        if (user.getRole() == null || user.getRole() != 1) {
            doctorId = user.getId();
        }
        
        if (doctorId == null) {
            logger.error("doctorId 为空");
            return "noDoctorId";
        }
        
        if (schedules == null || schedules.isEmpty()) {
            logger.error("排班数据为空");
            return "EMPTY";
        }
        
        // 确保所有排班的 doctorId 都设置为正确的值
        for (DoctorSchedule schedule : schedules) {
            if (schedule != null) {
                schedule.setDoctorId(doctorId);
            }
        }
        
        try {
            doctorScheduleService.applyTemplate(doctorId, schedules);
            return "SUCCESS";
        } catch (Exception e) {
            logger.error("应用模板失败", e);
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return "ERROR";
        }
    }
}
