package com.phms.controller.user;

import com.phms.pojo.Standard;
import com.phms.service.StandardService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * 监控标准
 */
@Controller("UserStandardController")
@RequestMapping("/user/standard")
public class UserStandardController {
    @Autowired
    private StandardService standardService;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    /**
     * 医生列表页面user/standardListDoctor.html
     * 注意：已移除HTML前端，Vue前端使用自己的路由系统，此方法已注释
     */
    // @RequestMapping("/standardListDoctor")
    // public String standardListDoctor() {
    //     return "user/standardListDoctor";
    // }
    /**
     * 普通用户页面user/standardList.html
     * 注意：已移除HTML前端，Vue前端使用自己的路由系统，此方法已注释
     */
    // @RequestMapping("/standardList")
    // public String standardList() {
    //     return "user/standardList";
    // }
    /**
     * 普通用户返回查询数据
     */
    @RequestMapping("/getAllByLimit")
    @ResponseBody
    public Object getAllByLimit(Standard pojo) {
        return standardService.getAllByLimit(pojo);
    }
    /**
     * 医生返回查询数据
     */
    @RequestMapping("/getAllByLimitDoctor")
    @ResponseBody
    public Object getAllByLimitBaoJie(Standard pojo) {
        return standardService.getAllByLimit(pojo);
    }

    /**
     * 根据id删除
     */
    @RequestMapping(value = "/del")
    @ResponseBody
    @Transactional
    public String del(Long id) {
        try {
            standardService.deleteById(id);
            return "SUCCESS";
        } catch (Exception e) {
            logger.error("删除异常", e);
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return "ERROR";
        }
    }

    /**
     * 添加页面 user/standardAdd.html
     * 注意：已移除HTML前端，Vue前端使用自己的路由系统，此方法已注释
     */
    // @RequestMapping(value = "/add")
    // public String add() {
    //     return "user/standardAdd";
    // }

    /**
     * 插入数据库
     */
    @RequestMapping(value = "/doAdd")
    @ResponseBody
    @Transactional
    public String doAdd(Standard pojo) {
        try {
            standardService.add(pojo);
            return "SUCCESS";
        } catch (Exception e) {
            logger.error("添加异常", e);
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return "ERROR";
        }
    }
}
