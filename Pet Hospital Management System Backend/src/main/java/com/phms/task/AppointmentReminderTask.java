package com.phms.task;

import com.alibaba.fastjson.JSON;
import com.phms.mapper.AppointmentMapper;
import com.phms.mapper.UserMapper;
import com.phms.pojo.Appointment;
import com.phms.pojo.NotificationMessage;
import com.phms.pojo.User;
import com.phms.service.NotificationMessageService;
import com.phms.websocket.NotificationWebSocketHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 预约提醒定时任务
 * 在预约时间前1小时发送提醒通知给用户
 */
@Component
public class AppointmentReminderTask {
    private static final Logger logger = LoggerFactory.getLogger(AppointmentReminderTask.class);
    
    @Autowired
    private AppointmentMapper appointmentMapper;
    
    @Autowired
    private NotificationMessageService notificationMessageService;
    
    @Autowired
    private NotificationWebSocketHandler notificationWebSocketHandler;
    
    @Autowired
    private UserMapper userMapper;
    
    /**
     * 每分钟执行一次，检查即将在1小时内开始的预约
     * 使用cron表达式：秒 分 时 日 月 周
     */
    @Scheduled(cron = "0 * * * * ?")
    public void checkAndSendReminders() {
        try {
            Date now = new Date();
            // 计算1小时后的时间
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(now);
            calendar.add(Calendar.HOUR_OF_DAY, 1);
            Date oneHourLater = calendar.getTime();
            
            // 查询即将在1小时内开始的预约（状态为1=待就诊或2=已确认）
            // SQL查询已经包含了状态1和2，所以只需要查询一次
            List<Appointment> allAppointments = appointmentMapper.selectUpcomingAppointments(
                now, oneHourLater, 1); // status参数在这里不再使用，SQL中已经硬编码了1和2
            
            if (allAppointments.isEmpty()) {
                logger.debug("没有需要提醒的预约");
                return;
            }
            
            logger.info("找到 {} 个需要提醒的预约", allAppointments.size());
            
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            
            for (Appointment appointment : allAppointments) {
                try {
                    // 获取用户信息
                    User user = userMapper.selectByPrimaryKey(appointment.getUserId());
                    if (user == null) {
                        logger.warn("用户不存在，跳过提醒 - 预约ID: {}, 用户ID: {}", 
                            appointment.getId(), appointment.getUserId());
                        continue;
                    }
                    
                    // 构建通知内容
                    Map<String, Object> contentMap = new HashMap<>();
                    contentMap.put("appointmentId", appointment.getId());
                    contentMap.put("appointmentTime", dateFormat.format(appointment.getAppTime()));
                    contentMap.put("timeSlot", appointment.getTimeSlot() != null ? appointment.getTimeSlot() : "");
                    contentMap.put("doctorName", appointment.getDoctorName() != null ? appointment.getDoctorName() : "医生");
                    contentMap.put("appointmentTypeName", appointment.getAppointmentTypeName() != null ? 
                        appointment.getAppointmentTypeName() : "预约");
                    contentMap.put("message", "您的预约将在1小时后开始，请准时到达医院");
                    
                    String contentJson = JSON.toJSONString(contentMap);
                    
                    // 创建通知消息
                    NotificationMessage notification = new NotificationMessage();
                    notification.setReceiverId(appointment.getUserId()); // 接收者是用户
                    notification.setSenderId(0L); // 系统发送
                    notification.setAppointmentId(appointment.getId());
                    notification.setType("APPOINTMENT_REMINDER");
                    notification.setTitle("预约提醒");
                    notification.setContent(contentJson);
                    notification.setIsRead(0); // 未读
                    notification.setCreateTime(new Date());
                    
                    // 保存通知消息
                    notificationMessageService.createMessage(notification);
                    logger.info("预约提醒消息已创建 - 预约ID: {}, 用户ID: {}", 
                        appointment.getId(), appointment.getUserId());
                    
                    // 通过WebSocket推送通知给用户
                    Map<String, Object> wsMessage = new HashMap<>();
                    wsMessage.put("type", "notification");
                    wsMessage.put("data", notification);
                    String wsMessageJson = JSON.toJSONString(wsMessage);
                    notificationWebSocketHandler.sendMessageToUser(appointment.getUserId(), wsMessageJson);
                    logger.info("预约提醒已通过WebSocket推送 - 用户ID: {}", appointment.getUserId());
                    
                } catch (Exception e) {
                    logger.error("发送预约提醒失败 - 预约ID: {}, 用户ID: {}", 
                        appointment.getId(), appointment.getUserId(), e);
                }
            }
            
        } catch (Exception e) {
            logger.error("检查预约提醒异常", e);
        }
    }
}
