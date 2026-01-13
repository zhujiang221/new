package com.phms.controller.user;

import com.phms.pojo.Appointment;
import com.phms.pojo.User;
import com.phms.service.AppointmentService;
import com.phms.utils.JwtUtil;
import com.phms.mapper.UserMapper;
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
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 用户预约
 */
@Controller("UserApplyController")
@RequestMapping("/user/apply")
public class UserApplyController {
    @Autowired
    private AppointmentService appointmentService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserMapper userMapper;


    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    /**
     * 医生管理预约页面
     * user/applyListDoctor.html
     * 注意：已移除HTML前端，Vue前端使用自己的路由系统，此方法已注释
     */
    // @RequestMapping("/applyListDoctor")
    // public String applyListDoctor(Long petId, Model model) {
    //     if (petId!=null){
    //         model.addAttribute("petId", petId);
    //     }
    //     return "user/applyListDoctor";
    // }

    /**
     * 普通用户预约页面
     * user/applyList.html
     * 注意：已移除HTML前端，Vue前端使用自己的路由系统，此方法已注释
     */
    // @RequestMapping("/applyList")
    // public String applyList(Long petId, Model model) {
    //     if (petId!=null){
    //         model.addAttribute("petId", petId);
    //     }
    //     return "user/applyList";
    // }
    /**
     * 普通用户返回查询数据渲染表格
     */
    @RequestMapping("/getAllByLimit")
    @ResponseBody
    public Object getAllByLimit(Appointment appointment) {
        Subject subject = SecurityUtils.getSubject();
        User user = (User) subject.getPrincipal();
        appointment.setUserId(user.getId());
        return appointmentService.getAllByLimit(appointment);
    }
    /**
     * 医生角色返回查询数据渲染表格
     * 医生只能看到预约给自己的预约信息
     */
    @RequestMapping("/getAllByLimitDoctor")
    @ResponseBody
    public Object getAllByLimitBaoJie(Appointment appointment) {
        // 获取当前登录用户（医生）
        Subject subject = SecurityUtils.getSubject();
        Object principal = subject.getPrincipal();

        User user = null;
        if (principal instanceof User) {
            user = (User) principal;
        } else {
            // 兜底：从Authorization的Bearer Token中解析用户名再查库，避免principal为null导致NPE
            try {
                ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
                if (attrs != null) {
                    String authHeader = attrs.getRequest().getHeader("Authorization");
                    if (authHeader != null && !authHeader.isEmpty()) {
                        String token = authHeader.startsWith("Bearer ") ? authHeader.substring(7) : authHeader;
                        String username = jwtUtil.getUsernameFromToken(token);
                        if (username != null) {
                            user = userMapper.getByUsername(username);
                            logger.info("通过Token兜底获取用户信息成功，username: {}", username);
                        } else {
                            logger.error("通过Token解析用户名失败，authHeader前20: {}", authHeader.length() > 20 ? authHeader.substring(0, 20) + "..." : authHeader);
                        }
                    } else {
                        logger.error("Authorization头为空，无法兜底获取用户信息");
                    }
                } else {
                    logger.error("RequestContextHolder未获取到请求上下文，无法兜底获取用户信息");
                }
            } catch (Exception e) {
                logger.error("兜底解析Token获取用户失败", e);
            }
        }

        if (user == null) {
            logger.error("当前会话未获取到有效用户对象，principal: {}", principal);
            return "LGINOUT";
        }

        // 防御性编程：确保 appointment 不为 null，避免 NPE
        if (appointment == null) {
            appointment = new Appointment();
        }

        // 设置医生ID，只查询预约给当前医生的记录
        appointment.setDoctorId(user.getId());
        return appointmentService.getAllByLimit(appointment);
    }

    /**
     * 根据id取消预约（将状态更新为5-已取消，而不是删除记录）
     */
    @RequestMapping(value = "/del")
    @ResponseBody
    @Transactional
    public String delUser(Long id) {
        try {
            Appointment appointment = appointmentService.getById(id);
            if (appointment == null) {
                return "NOT_FOUND";
            }
            // 将状态更新为5（已取消），而不是删除记录
            appointment.setStatus(5);
            appointmentService.update(appointment);
            return "SUCCESS";
        } catch (Exception e) {
            logger.error("取消预约异常", e);
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return "ERROR";
        }
    }

    /**
     * 添加预约页面 user/applyAdd.html
     * 注意：已移除HTML前端，Vue前端使用自己的路由系统，此方法已注释
     */
    // @RequestMapping(value = "/add")
    // public String addUserPage(Long id, Model model) {
    //     model.addAttribute("petId", id);
    //     return "user/applyAdd";
    // }

    /**
     * 预约信息插入数据库
     * 使用数据库行锁确保并发安全，防止超售
     */
    @RequestMapping(value = "/doAdd")
    @ResponseBody
    @Transactional
    public String doAdd(Appointment appointment) {
        Subject subject = SecurityUtils.getSubject();
        User user = (User) subject.getPrincipal();
        if (appointment.getPetId() == null){
            return "noPetId";
        }
        try {
            // 当前预约人的id
            appointment.setUserId(user.getId());
            // 预约时间：如果前端未显式传入，默认使用当前时间
            Date now = new Date();
            if (appointment.getAppTime() == null) {
                appointment.setAppTime(now);
            }
            // 校验必须选择医生、预约类型和时间段
            if (appointment.getDoctorId() == null
                    || appointment.getAppointmentTypeId() == null
                    || appointment.getTimeSlot() == null) {
                return "noDoctorOrTypeOrSlot";
            }
            
            // 先进行一次快速检查（非锁定），避免不必要的数据库锁
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            String appDate = sdf.format(appointment.getAppTime());
            boolean canBook = appointmentService.canBook(appointment.getDoctorId(), appDate, appointment.getTimeSlot());
            if (!canBook) {
                // 该时间段已约满，前端可根据该返回值提示用户选择其他时间段
                return "FULL";
            }

            // 设置预约信息
            appointment.setCreateTime(new Date());
            // 状态:1申请中,2申请通过,3不通过,4已完成
            appointment.setStatus(1);
            
            // 使用带行锁的方法添加预约（原子操作，防止并发超售）
            // 预约成功后，如果可用数量减到0，会自动将排班状态设为停用
            boolean success = appointmentService.addWithLockCheck(appointment);
            if (!success) {
                // 在锁定检查时发现已满（可能被其他用户抢先预约）
                return "FULL";
            }
            
            return "SUCCESS";
        } catch (RuntimeException e) {
            // 业务异常（如找不到排班记录）
            logger.error("添加预约业务异常", e);
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return "ERROR: " + e.getMessage();
        } catch (Exception e) {
            logger.error("添加预约异常", e);
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return "ERROR";
        }
    }

    /**
     * 改变预约状态
     */
    @RequestMapping(value = "/chStatus")
    @ResponseBody
    @Transactional
    public String chStatus(Appointment appointment) {
        Subject subject = SecurityUtils.getSubject();
        User user = (User) subject.getPrincipal();


        try {
            appointment.setDoctorId(user.getId());
            appointmentService.update(appointment);
            // 就诊
            if (appointment.getStatus() == 4){
                return "jz";
            }
            return "SUCCESS";
        } catch (Exception e) {
            logger.error("添加异常", e);
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return "ERROR";
        }
    }

    /**
     * 获取医生在指定日期的可用时间段列表
     */
    @RequestMapping(value = "/getAvailableSlots")
    @ResponseBody
    public Object getAvailableSlots(Long doctorId, String appDate) {
        if (doctorId == null || appDate == null || appDate.trim().isEmpty()) {
            logger.warn("获取可用时间段参数错误 - doctorId: {}, appDate: {}", doctorId, appDate);
            return "noDoctorOrDate";
        }
        try {
            logger.info("获取可用时间段 - doctorId: {}, appDate: {}", doctorId, appDate);
            List<Map<String, Object>> result = appointmentService.getAvailableTimeSlots(doctorId, appDate);
            logger.info("返回时间段数量: {}", result != null ? result.size() : 0);
            return result;
        } catch (Exception e) {
            logger.error("获取可用时间段失败 - doctorId: {}, appDate: {}", doctorId, appDate, e);
            return "ERROR";
        }
    }
}
