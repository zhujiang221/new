package com.phms.controller.user;


import com.phms.pojo.Notice;
import com.phms.service.NoticeService;
import com.phms.utils.MyUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 健康指南
 */
@Controller("UserNoticeController")
@RequestMapping("/user/notice")
public class UserNoticeController {
    @Autowired
    private NoticeService noticeService;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    /**
     * 查看详情页面/user/xqWord.html
     * 注意：已移除HTML前端，Vue前端使用getById接口获取详情，此方法已注释
     */
    // @RequestMapping("/xq")
    // public String xq(Long id, Model model) {
    //     noticeService.view(id);
    //     Notice word = noticeService.getById(id);
    //     System.out.println(word.getContent());
    //     model.addAttribute("c", word.getContent());
    //     model.addAttribute("title", word.getTitle());
    //     model.addAttribute("view", word.getViewCount());
    //     model.addAttribute("time", MyUtils.getDate2String(word.getCreateTime()));
    //     return "/user/xqWord";
    // }

    /**
     * 获取指南详情（JSON，用于Vue前端弹窗展示）
     */
    @RequestMapping("/getById")
    @ResponseBody
    public Object getById(Long id) {
        if (id == null) {
            return "ERROR";
        }
        // 同时增加浏览量
        noticeService.view(id);
        Notice word = noticeService.getById(id);
        if (word == null) {
            logger.warn("指南不存在, id={}", id);
            return "ERROR";
        }
        Map<String, Object> result = new HashMap<>();
        result.put("id", word.getId());
        result.put("title", word.getTitle());
        result.put("viewCount", word.getViewCount());
        result.put("createTime", word.getCreateTime() == null ? "" : MyUtils.getDate2String(word.getCreateTime()));
        result.put("content", word.getContent());
        return result;
    }

    /**
     * 普通用户查看列表/user/wordList.html
     * 注意：已移除HTML前端，Vue前端使用自己的路由系统，此方法已注释
     */
    // @RequestMapping("/list")
    // public String list() {
    //     return "/user/wordList";
    // }

    /**
     * 医生查看列表/user/wordListDoctor.html
     * 注意：已移除HTML前端，Vue前端使用自己的路由系统，此方法已注释
     */
    // @RequestMapping("/listDoctor")
    // public String listDoctor() {
    //     return "/user/wordListDoctor";
    // }

    /**
     * 医生发布指南页面/user/word.html
     * 注意：已移除HTML前端，Vue前端使用自己的路由系统，此方法已注释
     */
    // @RequestMapping("/publish")
    // public String publish() {
    //     return "/user/word";
    // }

    /**
     * 添加到数据库
     */
    @ResponseBody
    @RequestMapping("/addWord")
    public String addWord(Notice notice) {
        try {
            notice.setCreateTime(new Date());
            notice.setViewCount(0L);
            noticeService.add(notice);
            return "SUCCESS";
        } catch (Exception e) {
            e.printStackTrace();
            return "ERR";
        }
    }

    /**
     * 获取所有指南数据
     */
    @RequestMapping("/getAllWordByLimit")
    @ResponseBody
    public Object getAllWordByLimit(Notice word) {
        return noticeService.getAllByLimit(word);
    }

    /**
     * 删除指南
     */
    @ResponseBody
    @RequestMapping("/delWord")
    public String delWord(String[] ids) {
        try {
            for (String id : ids){
                noticeService.deleteById(Long.parseLong(id));
            }
            return "SUCCESS";
        } catch (Exception e) {
            e.printStackTrace();
            return "ERR";
        }
    }
}
