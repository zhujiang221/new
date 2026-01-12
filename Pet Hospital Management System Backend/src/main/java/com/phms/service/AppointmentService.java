package com.phms.service;

import com.phms.pojo.Appointment;

import java.util.List;
import java.util.Map;

public interface AppointmentService {
    Object getAllByLimit(Appointment appointment);

    void deleteById(Long id);

    void add(Appointment appointment);

    void update(Appointment appointment);

    Appointment getById(Long id);

    List<Map<String, Object>> getFreeTimeById(Long docId, String s);

    /**
     * 判断某个医生在指定日期和时间段是否还有可预约名额
     *
     * @param doctorId  医生ID
     * @param appDate   预约日期（yyyy-MM-dd）
     * @param timeSlot  时间段（如 09:00-10:00）
     * @return true=可以预约，false=已满或无排班
     */
    boolean canBook(Long doctorId, String appDate, String timeSlot);

    /**
     * 获取某个医生在指定日期的可用时间段列表
     * 返回每个时间段及其已预约数量
     *
     * @param doctorId  医生ID
     * @param appDate   预约日期（yyyy-MM-dd）
     * @return 时间段列表，每个包含：timeSlot（时间段）、used（已预约数）、canBook（是否可预约）
     */
    List<Map<String, Object>> getAvailableTimeSlots(Long doctorId, String appDate);

    /**
     * 带行锁检查并添加预约（原子操作）
     * 在事务中使用 SELECT FOR UPDATE 锁定记录，防止并发超售
     *
     * @param appointment 预约信息
     * @return true=预约成功，false=已满或无排班
     */
    boolean addWithLockCheck(Appointment appointment);
}
