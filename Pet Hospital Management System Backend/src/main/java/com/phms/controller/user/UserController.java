package com.phms.controller.user;

import com.phms.mapper.AppointmentMapper;
import com.phms.mapper.PetDailyMapper;
import com.phms.mapper.PetMapper;
import com.phms.model.ResultMap;
import com.phms.pojo.Appointment;
import com.phms.pojo.AppointmentExample;
import com.phms.pojo.PetDaily;
import com.phms.pojo.PetDailyExample;
import com.phms.pojo.User;
import com.phms.service.NotificationMessageService;
import com.phms.service.UserService;
import com.phms.utils.UserContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 用户控制
 */
@Controller("User")
@RequestMapping("/user")
public class UserController {
    private final Logger logger = LoggerFactory.getLogger(UserController.class);
    private final ResultMap resultMap;
    
    private final UserService userService;
    
    @Autowired
    private UserContext userContext;
    
    @Autowired
    private AppointmentMapper appointmentMapper;
    
    @Autowired
    private PetDailyMapper petDailyMapper;
    
    @Autowired
    private PetMapper petMapper;
    
    @Autowired
    private NotificationMessageService notificationMessageService;
    
    public UserController(ResultMap resultMap, UserService userService) {
        this.resultMap = resultMap;
        this.userService = userService;
    }

    /**
     * 返回有权限信息
     */
    @RequestMapping(value = "/getMessage", method = RequestMethod.GET)
    public ResultMap getMessage() {
        return resultMap.success().message("您拥有用户权限，可以获得该接口的信息！");
    }

    /**
     * 修改用户信息页面user/userEdit.html
     * 注意：已移除HTML前端，Vue前端使用自己的路由系统，此方法已注释
     */
    // @RequestMapping(value = "/editUserPage")
    // public String editUserPage(Long userId, Model model) {
    //     model.addAttribute("manageUser", userId);
    //     if (null != userId) {
    //         User user = userService.selectByPrimaryKey(userId);
    //         model.addAttribute("manageUser", user);
    //     }
    //     return "user/userEdit";
    // }

    /**
     * 更新数据库
     */
    @ResponseBody
    @RequestMapping("/updateUser")
    public String updateUser(User user) {
        return userService.updateUser(user);
    }

    /**
     * 检查邮箱是否已被其他用户使用
     */
    @ResponseBody
    @RequestMapping("/checkEmail")
    public ResultMap checkEmail(String email, Long userId) {
        try {
            if (email == null || email.trim().isEmpty()) {
                return resultMap.fail().message("邮箱不能为空");
            }
            
            User existingUser = userService.getByEmail(email.trim());
            if (existingUser != null && (userId == null || !existingUser.getId().equals(userId))) {
                return resultMap.fail().message("该邮箱已被其他用户使用");
            }
            
            return resultMap.success().message("邮箱可用");
        } catch (Exception e) {
            logger.error("检查邮箱失败", e);
            return resultMap.fail().message("检查邮箱失败");
        }
    }

    /**
     * 获取当前登录用户信息（包括角色）
     */
    @ResponseBody
    @RequestMapping("/getCurrentUser")
    public ResultMap getCurrentUser() {
        try {
            User user = userContext.getCurrentUser();
            if (user == null) {
                return resultMap.fail().message("用户未登录");
            }
            
            // 创建新的ResultMap，避免修改共享的resultMap导致并发问题
            ResultMap response = new ResultMap();
            response.put("result", "success");
            
            // 获取用户角色（从user表的role字段获取）
            Integer roleId = user.getRole();
            if (roleId == null) {
                roleId = 3; // 默认普通用户
            }
            
            // 构建返回数据
            response.put("id", user.getId());
            response.put("username", user.getUsername() != null && !user.getUsername().trim().isEmpty() 
                ? user.getUsername() 
                : (user.getPhone() != null ? user.getPhone() : String.valueOf(user.getId())));
            response.put("name", user.getName());
            response.put("phone", user.getPhone());
            response.put("email", user.getEmail());
            response.put("address", user.getAddress());
            response.put("role", roleId); // 使用role字段，与前端保持一致
            response.put("roleId", roleId); // 兼容旧代码
            
            return response;
        } catch (Exception e) {
            logger.error("获取当前用户信息失败", e);
            return resultMap.fail().message("获取用户信息失败");
        }
    }

    /**
     * 获取主页统计数据
     */
    @GetMapping("/home/statistics")
    @ResponseBody
    public ResultMap getHomeStatistics() {
        try {
            User user = userContext.getCurrentUser();
            if (user == null) {
                return resultMap.fail().message("用户未登录");
            }

            Long userId = user.getId();

            // 1. 查询待就诊预约数（status=1或2）
            AppointmentExample appointmentExample = new AppointmentExample();
            AppointmentExample.Criteria criteria = appointmentExample.createCriteria();
            criteria.andUserIdEqualTo(userId);
            criteria.andStatusIn(Arrays.asList(1, 2)); // 1=待就诊, 2=已确认
            long pendingAppointments = appointmentMapper.countByExample(appointmentExample);

            // 2. 查询宠物日志总数
            // 先获取用户的所有宠物ID
            List<Long> petIds = petMapper.getPetIdsByUserId(userId);
            long petLogs = 0;
            if (petIds != null && !petIds.isEmpty()) {
                PetDailyExample petDailyExample = new PetDailyExample();
                PetDailyExample.Criteria petDailyCriteria = petDailyExample.createCriteria();
                petDailyCriteria.andPetIdIn(petIds);
                petLogs = petDailyMapper.countByExample(petDailyExample);
            }

            // 3. 查询未读消息数
            int unreadMessages = notificationMessageService.getUnreadCount(userId);

            // 构建返回数据
            Map<String, Object> data = new HashMap<>();
            data.put("pendingAppointments", pendingAppointments);
            data.put("petLogs", petLogs);
            data.put("unreadMessages", unreadMessages);

            return resultMap.success().data(data);
        } catch (Exception e) {
            logger.error("获取主页统计数据失败", e);
            return resultMap.fail().message("获取统计数据失败");
        }
    }

    /**
     * 获取近期预约列表
     */
    @GetMapping("/home/recentAppointments")
    @ResponseBody
    public ResultMap getRecentAppointments(@RequestParam(defaultValue = "3") Integer limit) {
        try {
            User user = userContext.getCurrentUser();
            if (user == null) {
                return resultMap.fail().message("用户未登录");
            }

            Long userId = user.getId();

            // 查询所有预约（需要关联查询预约类型和医生名称）
            Appointment appointment = new Appointment();
            appointment.setUserId(userId);
            appointment.setPage(0);
            appointment.setLimit(100); // 先获取更多数据，然后过滤

            // 获取预约列表（需要关联查询预约类型和医生名称）
            List<Appointment> appointments = appointmentMapper.getAllByLimit(appointment);

            // 转换为前端需要的格式，只返回未来30天内的预约
            List<Map<String, Object>> result = new ArrayList<>();
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            Date now = new Date();
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(now);
            calendar.add(Calendar.DAY_OF_MONTH, 30);
            Date futureDate = calendar.getTime();

            for (Appointment apt : appointments) {
                // 只返回未来或最近的预约
                if (apt.getAppTime() != null) {
                    Date appDate = apt.getAppTime();
                    // 只返回未来30天内的预约，且状态为待就诊或已确认
                    if (appDate.after(now) && appDate.before(futureDate) 
                        && apt.getStatus() != null && (apt.getStatus() == 1 || apt.getStatus() == 2)) {
                        Map<String, Object> item = new HashMap<>();
                        item.put("id", apt.getId());
                        item.put("appointmentTypeName", apt.getAppointmentTypeName() != null ? apt.getAppointmentTypeName() : "");
                        item.put("doctorName", apt.getDoctorName() != null ? apt.getDoctorName() : "");
                        item.put("appDate", dateFormat.format(appDate));
                        item.put("timeSlot", apt.getTimeSlot() != null ? apt.getTimeSlot() : "");
                        item.put("status", apt.getStatus());
                        result.add(item);
                    }
                }
            }

            // 按日期和时间段排序
            result.sort((a, b) -> {
                String dateA = (String) a.get("appDate");
                String dateB = (String) b.get("appDate");
                int dateCompare = dateA.compareTo(dateB);
                if (dateCompare != 0) {
                    return dateCompare;
                }
                String timeA = (String) a.get("timeSlot");
                String timeB = (String) b.get("timeSlot");
                if (timeA == null) timeA = "";
                if (timeB == null) timeB = "";
                return timeA.compareTo(timeB);
            });

            // 限制返回数量
            if (result.size() > limit) {
                result = result.subList(0, limit);
            }

            return resultMap.success().data(result);
        } catch (Exception e) {
            logger.error("获取近期预约列表失败", e);
            return resultMap.fail().message("获取近期预约失败");
        }
    }

    /**
     * 获取健康趋势数据
     */
    @GetMapping("/home/healthTrends")
    @ResponseBody
    public ResultMap getHealthTrends() {
        try {
            User user = userContext.getCurrentUser();
            if (user == null) {
                return resultMap.fail().message("用户未登录");
            }

            Long userId = user.getId();

            // 获取用户的所有宠物ID
            List<Long> petIds = petMapper.getPetIdsByUserId(userId);
            if (petIds == null || petIds.isEmpty()) {
                // 如果没有宠物，返回默认值
                Map<String, Object> defaultData = new HashMap<>();
                defaultData.put("weightStatus", "normal");
                defaultData.put("activityStatus", "normal");
                defaultData.put("dietStatus", "normal");
                defaultData.put("weightProgress", 0);
                defaultData.put("activityProgress", 0);
                defaultData.put("dietProgress", 0);
                return resultMap.success().data(defaultData);
            }

            // 查询最近30天的宠物日志数据
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.DAY_OF_MONTH, -30);
            Date thirtyDaysAgo = calendar.getTime();

            PetDailyExample petDailyExample = new PetDailyExample();
            PetDailyExample.Criteria criteria = petDailyExample.createCriteria();
            criteria.andPetIdIn(petIds);
            criteria.andCreateTimeGreaterThanOrEqualTo(thirtyDaysAgo);
            List<PetDaily> petDailies = petDailyMapper.selectByExample(petDailyExample);

            // 计算健康趋势
            Map<String, Object> trends = calculateHealthTrends(petDailies);

            return resultMap.success().data(trends);
        } catch (Exception e) {
            logger.error("获取健康趋势失败", e);
            return resultMap.fail().message("获取健康趋势失败");
        }
    }

    /**
     * 计算健康趋势
     */
    private Map<String, Object> calculateHealthTrends(List<PetDaily> petDailies) {
        Map<String, Object> trends = new HashMap<>();

        if (petDailies == null || petDailies.isEmpty()) {
            trends.put("weightStatus", "normal");
            trends.put("activityStatus", "normal");
            trends.put("dietStatus", "normal");
            trends.put("weightProgress", 0);
            trends.put("activityProgress", 0);
            trends.put("dietProgress", 0);
            return trends;
        }

        // 计算体重趋势
        double totalWeight = 0;
        int weightCount = 0;
        for (PetDaily daily : petDailies) {
            if (daily.getWeight() != null && daily.getWeight() > 0) {
                totalWeight += daily.getWeight();
                weightCount++;
            }
        }
        double avgWeight = weightCount > 0 ? totalWeight / weightCount : 0;
        // 简化的体重状态判断（实际应该与健康标准对比）
        String weightStatus = "normal";
        int weightProgress = 75;
        if (avgWeight > 0) {
            // 这里可以根据实际健康标准来判断
            weightProgress = Math.min(100, (int) (avgWeight / 10 * 10)); // 简化计算
        }

        // 计算活动量趋势（使用appetite字段作为活动量指标）
        double totalActivity = 0;
        int activityCount = 0;
        for (PetDaily daily : petDailies) {
            if (daily.getAppetite() != null && daily.getAppetite() > 0) {
                totalActivity += daily.getAppetite();
                activityCount++;
            }
        }
        double avgActivity = activityCount > 0 ? totalActivity / activityCount : 0;
        String activityStatus = "normal";
        int activityProgress = 70;
        if (avgActivity > 0) {
            if (avgActivity >= 8) {
                activityStatus = "excellent";
                activityProgress = 90;
            } else if (avgActivity >= 6) {
                activityStatus = "good";
                activityProgress = 75;
            } else if (avgActivity >= 4) {
                activityStatus = "normal";
                activityProgress = 60;
            } else {
                activityStatus = "poor";
                activityProgress = 40;
            }
        }

        // 计算饮食规律（使用status字段作为饮食规律指标，或者根据记录频率）
        int dietCount = petDailies.size();
        String dietStatus = "normal";
        int dietProgress = 60;
        // 根据记录频率判断饮食规律
        if (dietCount >= 25) {
            dietStatus = "excellent";
            dietProgress = 90;
        } else if (dietCount >= 15) {
            dietStatus = "good";
            dietProgress = 75;
        } else if (dietCount >= 10) {
            dietStatus = "normal";
            dietProgress = 60;
        } else {
            dietStatus = "poor";
            dietProgress = 40;
        }

        trends.put("weightStatus", weightStatus);
        trends.put("activityStatus", activityStatus);
        trends.put("dietStatus", dietStatus);
        trends.put("weightProgress", weightProgress);
        trends.put("activityProgress", activityProgress);
        trends.put("dietProgress", dietProgress);

        return trends;
    }
}
