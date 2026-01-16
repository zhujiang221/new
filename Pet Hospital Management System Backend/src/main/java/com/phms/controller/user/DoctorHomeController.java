package com.phms.controller.user;

import com.phms.mapper.AppointmentMapper;
import com.phms.mapper.MedicineRecordMapper;
import com.phms.model.ResultMap;
import com.phms.pojo.Appointment;
import com.phms.pojo.AppointmentExample;
import com.phms.pojo.MedicineRecord;
import com.phms.pojo.User;
import com.phms.utils.UserContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.*;

/**
 * 医生主页控制器
 */
@Controller("DoctorHomeController")
@RequestMapping("/doctor/home")
public class DoctorHomeController {
    private final Logger logger = LoggerFactory.getLogger(DoctorHomeController.class);
    
    @Autowired
    private ResultMap resultMap;
    
    @Autowired
    private UserContext userContext;
    
    @Autowired
    private AppointmentMapper appointmentMapper;
    
    @Autowired
    private MedicineRecordMapper medicineRecordMapper;

    /**
     * 获取医生主页统计数据
     */
    @GetMapping("/statistics")
    @ResponseBody
    public ResultMap getHomeStatistics() {
        try {
            User user = userContext.getCurrentUser();
            if (user == null) {
                return resultMap.fail().message("用户未登录");
            }
            
            // 验证是否为医生
            if (user.getRole() == null || user.getRole() != 2) {
                return resultMap.fail().message("只有医生可以访问此接口");
            }
            
            Long doctorId = user.getId();
            Date now = new Date();
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(now);
            
            // 1. 查询待处理预约数（status=1或2，不限制日期，因为可能有过期未处理的预约）
            AppointmentExample appointmentExample = new AppointmentExample();
            AppointmentExample.Criteria criteria = appointmentExample.createCriteria();
            criteria.andDoctorIdEqualTo(doctorId);
            criteria.andStatusIn(Arrays.asList(1, 2)); // 1=待就诊, 2=已确认
            // 移除日期限制，统计所有待处理的预约
            long pendingAppointments = appointmentMapper.countByExample(appointmentExample);
            
            // 计算今天开始和结束时间（用于后续查询）
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);
            Date todayStart = calendar.getTime();
            
            calendar.set(Calendar.HOUR_OF_DAY, 23);
            calendar.set(Calendar.MINUTE, 59);
            calendar.set(Calendar.SECOND, 59);
            calendar.set(Calendar.MILLISECOND, 999);
            Date todayEnd = calendar.getTime();
            
            // 2. 查询今日预约总数（所有状态，预约时间在今天）
            AppointmentExample todayTotalExample = new AppointmentExample();
            AppointmentExample.Criteria todayTotalCriteria = todayTotalExample.createCriteria();
            todayTotalCriteria.andDoctorIdEqualTo(doctorId);
            todayTotalCriteria.andAppTimeBetween(todayStart, todayEnd);
            long todayTotalAppointments = appointmentMapper.countByExample(todayTotalExample);
            
            // 3. 查询今日已完成预约数（status=3，且预约时间在今天）
            
            AppointmentExample todayCompletedExample = new AppointmentExample();
            AppointmentExample.Criteria todayCompletedCriteria = todayCompletedExample.createCriteria();
            todayCompletedCriteria.andDoctorIdEqualTo(doctorId);
            todayCompletedCriteria.andStatusEqualTo(3); // 3=已完成
            todayCompletedCriteria.andAppTimeBetween(todayStart, todayEnd);
            long todayCompleted = appointmentMapper.countByExample(todayCompletedExample);
            
            // 4. 查询在诊患者数（status=2，且预约时间在今天）
            AppointmentExample inClinicExample = new AppointmentExample();
            AppointmentExample.Criteria inClinicCriteria = inClinicExample.createCriteria();
            inClinicCriteria.andDoctorIdEqualTo(doctorId);
            inClinicCriteria.andStatusEqualTo(2); // 2=已确认（进行中）
            inClinicCriteria.andAppTimeBetween(todayStart, todayEnd);
            long inClinicPatients = appointmentMapper.countByExample(inClinicExample);
            
            // 5. 查询本周开药数
            calendar.setTime(now);
            calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);
            Date weekStart = calendar.getTime();
            
            MedicineRecord medicineRecord = new MedicineRecord();
            medicineRecord.setDoctorId(doctorId);
            medicineRecord.setPage(0);
            medicineRecord.setLimit(10000); // 获取足够多的数据
            List<MedicineRecord> allRecords = medicineRecordMapper.getAllByLimit(medicineRecord);
            
            long weekPrescriptions = 0;
            for (MedicineRecord record : allRecords) {
                if (record.getCreateTime() != null && record.getCreateTime().after(weekStart)) {
                    weekPrescriptions++;
                }
            }
            
            // 构建返回数据
            Map<String, Object> data = new HashMap<>();
            data.put("pendingAppointments", pendingAppointments);
            data.put("todayTotalAppointments", todayTotalAppointments); // 今日预约总数
            data.put("todayCompleted", todayCompleted);
            data.put("inClinicPatients", inClinicPatients);
            data.put("weekPrescriptions", weekPrescriptions);
            
            return resultMap.success().data(data);
        } catch (Exception e) {
            logger.error("获取医生主页统计数据失败", e);
            return resultMap.fail().message("获取统计数据失败");
        }
    }

    /**
     * 获取今日排班列表
     */
    @GetMapping("/todaySchedule")
    @ResponseBody
    public ResultMap getTodaySchedule(@RequestParam(defaultValue = "10") Integer limit) {
        try {
            User user = userContext.getCurrentUser();
            if (user == null) {
                return resultMap.fail().message("用户未登录");
            }
            
            // 验证是否为医生
            if (user.getRole() == null || user.getRole() != 2) {
                return resultMap.fail().message("只有医生可以访问此接口");
            }
            
            Long doctorId = user.getId();
            Date now = new Date();
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(now);
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);
            Date todayStart = calendar.getTime();
            
            calendar.set(Calendar.HOUR_OF_DAY, 23);
            calendar.set(Calendar.MINUTE, 59);
            calendar.set(Calendar.SECOND, 59);
            calendar.set(Calendar.MILLISECOND, 999);
            Date todayEnd = calendar.getTime();
            
            // 查询今日的预约
            Appointment appointment = new Appointment();
            appointment.setDoctorId(doctorId);
            appointment.setPage(0);
            appointment.setLimit(100);
            
            List<Appointment> appointments = appointmentMapper.getAllByLimit(appointment);
            
            // 过滤今日的预约并按时间排序
            List<Map<String, Object>> result = new ArrayList<>();
            
            for (Appointment apt : appointments) {
                if (apt.getAppTime() != null) {
                    Date appDate = apt.getAppTime();
                    if (appDate.after(todayStart) && appDate.before(todayEnd)) {
                        Map<String, Object> item = new HashMap<>();
                        item.put("id", apt.getId());
                        item.put("petName", apt.getPetName() != null ? apt.getPetName() : "");
                        item.put("petBreed", apt.getPetName() != null ? "" : ""); // 可以从pet表关联查询
                        item.put("appointmentTypeName", apt.getAppointmentTypeName() != null ? apt.getAppointmentTypeName() : "");
                        item.put("timeSlot", apt.getTimeSlot() != null ? apt.getTimeSlot() : "");
                        item.put("status", apt.getStatus());
                        item.put("statusText", getStatusText(apt.getStatus()));
                        result.add(item);
                    }
                }
            }
            
            // 按时间段排序
            result.sort((a, b) -> {
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
            logger.error("获取今日排班列表失败", e);
            return resultMap.fail().message("获取排班列表失败");
        }
    }

    /**
     * 获取工作统计
     */
    @GetMapping("/workStatistics")
    @ResponseBody
    public ResultMap getWorkStatistics() {
        try {
            User user = userContext.getCurrentUser();
            if (user == null) {
                return resultMap.fail().message("用户未登录");
            }
            
            // 验证是否为医生
            if (user.getRole() == null || user.getRole() != 2) {
                return resultMap.fail().message("只有医生可以访问此接口");
            }
            
            Long doctorId = user.getId();
            Date now = new Date();
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(now);
            
            // 本周接诊量
            calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);
            Date weekStart = calendar.getTime();
            
            AppointmentExample weekAppointmentExample = new AppointmentExample();
            AppointmentExample.Criteria weekCriteria = weekAppointmentExample.createCriteria();
            weekCriteria.andDoctorIdEqualTo(doctorId);
            weekCriteria.andStatusEqualTo(3); // 已完成
            weekCriteria.andAppTimeGreaterThanOrEqualTo(weekStart);
            long weekConsultations = appointmentMapper.countByExample(weekAppointmentExample);
            
            // 本月开药量
            calendar.setTime(now);
            calendar.set(Calendar.DAY_OF_MONTH, 1);
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);
            Date monthStart = calendar.getTime();
            
            MedicineRecord monthMedicineRecord = new MedicineRecord();
            monthMedicineRecord.setDoctorId(doctorId);
            monthMedicineRecord.setPage(0);
            monthMedicineRecord.setLimit(10000); // 获取足够多的数据
            List<MedicineRecord> allMonthRecords = medicineRecordMapper.getAllByLimit(monthMedicineRecord);
            
            long monthPrescriptions = 0;
            for (MedicineRecord record : allMonthRecords) {
                if (record.getCreateTime() != null && record.getCreateTime().after(monthStart)) {
                    monthPrescriptions++;
                }
            }
            
            // 患者满意度（这里简化处理，实际应该从评价表中查询）
            // 假设满意度为98%，实际应该从评价表计算
            int patientSatisfaction = 98;
            
            // 构建返回数据
            Map<String, Object> data = new HashMap<>();
            data.put("weekConsultations", weekConsultations);
            data.put("weekConsultationsTarget", 50); // 目标值
            data.put("patientSatisfaction", patientSatisfaction);
            data.put("monthPrescriptions", monthPrescriptions);
            
            return resultMap.success().data(data);
        } catch (Exception e) {
            logger.error("获取工作统计失败", e);
            return resultMap.fail().message("获取工作统计失败");
        }
    }
    
    /**
     * 获取状态文本
     */
    private String getStatusText(Integer status) {
        if (status == null) return "未知";
        switch (status) {
            case 1:
                return "待就诊";
            case 2:
                return "进行中";
            case 3:
                return "已完成";
            case 4:
                return "已取消";
            default:
                return "未知";
        }
    }
}
