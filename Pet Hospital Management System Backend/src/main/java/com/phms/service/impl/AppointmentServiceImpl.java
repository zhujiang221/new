package com.phms.service.impl;

import com.phms.mapper.AppointmentMapper;
import com.phms.mapper.DoctorScheduleMapper;
import com.phms.model.MMGridPageVoBean;
import com.phms.pojo.Appointment;
import com.phms.pojo.DoctorSchedule;
import com.phms.service.AppointmentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Service
public class AppointmentServiceImpl implements AppointmentService {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    @Resource
    AppointmentMapper appointmentMapper;
    @Resource
    DoctorScheduleMapper doctorScheduleMapper;
    @Override
    public Object getAllByLimit(Appointment appointment) {
        int size = 0;
        // 计算分页
        Integer begin = (appointment.getPage() - 1) * appointment.getLimit();
        appointment.setPage(begin);

        List<Appointment> rows = new ArrayList<>();
        try {
            rows = appointmentMapper.getAllByLimit(appointment);
            size = appointmentMapper.countAllByLimit(appointment);
        } catch (Exception e) {
            logger.error("根据条件查询异常", e);
        }
        MMGridPageVoBean<Appointment> vo = new MMGridPageVoBean<>();
        vo.setTotal(size);
        vo.setRows(rows);

        return vo;
    }

    @Override
    public void deleteById(Long id) {
        appointmentMapper.deleteByPrimaryKey(id);
    }

    @Override
    public void add(Appointment categorization) {
        appointmentMapper.insert(categorization);
    }

    /**
     * 带行锁检查并添加预约（原子操作）
     * 在事务中使用 SELECT FOR UPDATE 锁定记录，防止并发
     * 只检查排班是否存在且状态为启用，不再限制最大预约次数
     */
    @Override
    public boolean addWithLockCheck(Appointment appointment) {
        try {
            // 计算星期几（1-7，1=周一）
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            String appDate = sdf.format(appointment.getAppTime());
            Date date = sdf.parse(appDate);
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);
            int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
            // Java 周日是 1，周一是 2，这里转换为 1=周一 ... 7=周日
            int weekDay = dayOfWeek - 1;
            if (weekDay == 0) {
                weekDay = 7;
            }

            // 查询对应的排班记录（带行锁）
            DoctorSchedule schedule = doctorScheduleMapper.selectByDoctorWeekAndTimeSlot(
                appointment.getDoctorId(),
                weekDay,
                appointment.getTimeSlot()
            );
            
            if (schedule == null) {
                // 找不到对应的排班记录
                logger.warn("预约失败：找不到排班记录 - 医生ID={}, 星期={}, 时间段={}", 
                    appointment.getDoctorId(), weekDay, appointment.getTimeSlot());
                return false;
            }
            
            // 检查排班状态是否为停用
            if (schedule.getStatus() != null && schedule.getStatus() == 0) {
                logger.warn("预约失败：排班已停用 - 医生ID={}, 星期={}, 时间段={}", 
                    appointment.getDoctorId(), weekDay, appointment.getTimeSlot());
                return false;
            }

            // 排班存在且启用，插入预约记录
            appointmentMapper.insert(appointment);
            
            // 验证ID是否已生成
            if (appointment.getId() == null) {
                logger.error("预约插入失败：未获取到生成的ID - 医生ID={}, 日期={}, 时间段={}", 
                    appointment.getDoctorId(), appDate, appointment.getTimeSlot());
                return false;
            }
            
            logger.info("预约成功：预约ID={}, 医生ID={}, 日期={}, 时间段={}", 
                appointment.getId(), appointment.getDoctorId(), appDate, appointment.getTimeSlot());
            
            return true;
        } catch (Exception e) {
            logger.error("带锁检查并添加预约失败", e);
            return false;
        }
    }

    @Override
    public void update(Appointment appointment) {
        appointmentMapper.updateByPrimaryKeySelective(appointment);
    }

    @Override
    public Appointment getById(Long id) {
        return appointmentMapper.selectByPrimaryKey(id);
    }

    @Override
    public List<Map<String, Object>> getFreeTimeById(Long docId, String s) {
        return appointmentMapper.getFreeTimeById(docId, s);
    }

    /**
     * 判断某个医生在指定日期和时间段是否还有可预约名额
     */
    @Override
    public boolean canBook(Long doctorId, String appDate, String timeSlot) {
        try {
            // 计算星期几（1-7，1=周一）
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            Date date = sdf.parse(appDate);
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);
            int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
            // Java 周日是 1，周一是 2，这里转换为 1=周一 ... 7=周日
            int weekDay = dayOfWeek - 1;
            if (weekDay == 0) {
                weekDay = 7;
            }

            // 查询该医生在该星期几、该时间段的排班
            List<DoctorSchedule> schedules = doctorScheduleMapper.selectByDoctorAndWeek(doctorId, weekDay);
            DoctorSchedule target = null;
            if (schedules != null) {
                for (DoctorSchedule s : schedules) {
                    if (timeSlot.equals(s.getTimeSlot())) {
                        target = s;
                        break;
                    }
                }
            }
            if (target == null || target.getStatus() != null && target.getStatus() == 0) {
                // 没有排班或已停用 = 不可预约
                return false;
            }

            // 只要有排班且状态为启用，就可以预约（不再限制最大预约次数）
            return true;
        } catch (Exception e) {
            logger.error("检查医生排班及剩余名额失败", e);
            return false;
        }
    }

    @Override
    public List<Map<String, Object>> getAvailableTimeSlots(Long doctorId, String appDate) {
        List<Map<String, Object>> result = new ArrayList<>();
        try {
            // 计算星期几（1-7，1=周一）
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            Date date = sdf.parse(appDate);
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);
            int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
            // Java Calendar.DAY_OF_WEEK: 周日=1, 周一=2, ..., 周六=7
            // 转换为: 周一=1, 周二=2, ..., 周日=7
            int weekDay = dayOfWeek - 1;
            if (weekDay == 0) {
                weekDay = 7; // 周日
            }

            logger.info("获取可用时间段 - 医生ID: {}, 日期: {}, 星期几: {}", doctorId, appDate, weekDay);

            // 查询该医生在该星期几的所有排班
            List<DoctorSchedule> schedules = doctorScheduleMapper.selectByDoctorAndWeek(doctorId, weekDay);
            logger.info("查询到排班数量: {}", schedules != null ? schedules.size() : 0);
            
            if (schedules != null && !schedules.isEmpty()) {
                for (DoctorSchedule schedule : schedules) {
                    // 只返回启用的排班（status为null或status不为0表示启用）
                    if (schedule.getStatus() != null && schedule.getStatus() == 0) {
                        logger.debug("跳过停用的排班: timeSlot={}, status={}", schedule.getTimeSlot(), schedule.getStatus());
                        continue;
                    }
                    
                    String timeSlot = schedule.getTimeSlot();
                    Integer usedCount = appointmentMapper.countByDoctorAndTime(doctorId, appDate, timeSlot);
                    int used = (usedCount != null) ? usedCount : 0;
                    // 只要有排班且状态为启用，就可以预约（不再限制最大预约次数）
                    boolean canBook = true;

                    logger.info("获取可用时间段 - 医生ID: {}, 日期: {}, 时间段: {}, 已用: {}, 可预约: {}", 
                        doctorId, appDate, timeSlot, used, canBook);

                    Map<String, Object> slot = new java.util.HashMap<>();
                    slot.put("timeSlot", timeSlot);
                    slot.put("used", used);
                    slot.put("canBook", canBook);
                    result.add(slot);
                }
            } else {
                logger.warn("未找到医生 {} 在星期 {} 的排班数据", doctorId, weekDay);
            }
        } catch (Exception e) {
            logger.error("获取可用时间段失败 - 医生ID: {}, 日期: {}", doctorId, appDate, e);
        }
        return result;
    }
}
