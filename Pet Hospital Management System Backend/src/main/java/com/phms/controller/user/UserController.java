package com.phms.controller.user;

import com.phms.model.ResultMap;
import com.phms.pojo.User;
import com.phms.service.UserService;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * 用户控制
 */
@Controller("User")
@RequestMapping("/user")
public class UserController {
    private final Logger logger = LoggerFactory.getLogger(UserController.class);
    private final ResultMap resultMap;
    
    private final UserService userService;

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
            Subject subject = SecurityUtils.getSubject();
            User user = (User) subject.getPrincipal();
            if (user == null) {
                return resultMap.fail().message("用户未登录");
            }
            
            // 获取用户角色（从user表的role字段获取）
            Integer roleId = userService.getUserRoleId(user.getId());
            if (roleId == null) {
                roleId = 3; // 默认普通用户
            }
            
            // 构建返回数据，直接添加到ResultMap中
            resultMap.put("id", user.getId());
            // 使用username字段，如果为空则使用phone作为fallback
            resultMap.put("username", user.getUsername() != null && !user.getUsername().trim().isEmpty() 
                ? user.getUsername() 
                : (user.getPhone() != null ? user.getPhone() : String.valueOf(user.getId())));
            resultMap.put("name", user.getName());
            resultMap.put("phone", user.getPhone());
            resultMap.put("email", user.getEmail());
            resultMap.put("address", user.getAddress());
            resultMap.put("roleId", roleId);
            
            return resultMap.success();
        } catch (Exception e) {
            logger.error("获取当前用户信息失败", e);
            return resultMap.fail().message("获取用户信息失败");
        }
    }
}
