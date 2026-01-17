package com.phms.controller.admin;

import com.phms.model.ResultMap;
import com.phms.pojo.ApiLog;
import com.phms.service.ApiLogService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Date;
import java.util.Map;

/**
 * API日志管理控制器（仅管理员可访问）
 */
@Controller("ApiLog")
@RequestMapping("/admin/apiLog")
public class ApiLogController {

    @Autowired
    private ApiLogService apiLogService;

    @Autowired
    private ResultMap resultMap;


    private final Logger logger = LoggerFactory.getLogger(ApiLogController.class);

    /**
     * 分页查询API日志列表（支持多条件搜索）
     */
    @RequestMapping("/getAllByLimit")
    @ResponseBody
    public ResultMap getAllByLimit(
            @RequestParam(value = "userId", required = false) Long userId,
            @RequestParam(value = "requestUrl", required = false) String requestUrl,
            @RequestParam(value = "requestMethod", required = false) String requestMethod,
            @RequestParam(value = "ipAddress", required = false) String ipAddress,
            @RequestParam(value = "status", required = false) Integer status,
            @RequestParam(value = "startTime", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date startTime,
            @RequestParam(value = "endTime", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date endTime,
            @RequestParam(value = "page", defaultValue = "1") Integer page,
            @RequestParam(value = "limit", defaultValue = "10") Integer limit
    ) {
        try {
            com.phms.model.MMGridPageVoBean<ApiLog> data = apiLogService.getApiLogList(
                    userId, requestUrl, requestMethod, ipAddress, status,
                    startTime, endTime, page, limit
            );
            return resultMap.success().data(data);
        } catch (Exception e) {
            logger.error("查询API日志列表失败: {}", e.getMessage(), e);
            return resultMap.fail().message("查询失败: " + e.getMessage());
        }
    }

    /**
     * 根据ID查询日志详情
     */
    @RequestMapping("/getById")
    @ResponseBody
    public Map<String, Object> getById(@RequestParam("id") Long id) {
        try {
            ApiLog apiLog = apiLogService.getApiLogById(id);
            if (apiLog == null) {
                return resultMap.fail().message("日志不存在");
            }
            return resultMap.success().data(apiLog);
        } catch (Exception e) {
            logger.error("查询API日志详情失败: id={}, error={}", id, e.getMessage(), e);
            return resultMap.fail().message("查询失败: " + e.getMessage());
        }
    }

    /**
     * 删除指定日期之前的日志
     */
    @RequestMapping("/deleteBeforeDate")
    @ResponseBody
    public Map<String, Object> deleteBeforeDate(
            @RequestParam("date") @DateTimeFormat(pattern = "yyyy-MM-dd") Date date
    ) {
        try {
            int count = apiLogService.deleteLogsBeforeDate(date);
            return resultMap.success().message("成功删除 " + count + " 条日志");
        } catch (Exception e) {
            logger.error("删除API日志失败: date={}, error={}", date, e.getMessage(), e);
            return resultMap.fail().message("删除失败: " + e.getMessage());
        }
    }
}
