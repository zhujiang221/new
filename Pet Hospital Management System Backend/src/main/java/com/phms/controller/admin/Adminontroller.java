package com.phms.controller.admin;

import com.phms.mapper.UserMapper;
import com.phms.model.ResultMap;
import com.phms.pojo.Page;
import com.phms.pojo.Role;
import com.phms.pojo.User;
import com.phms.pojo.UserParameter;
import com.phms.service.*;
import com.phms.service.NotificationMessageService;
import com.phms.utils.MD5;
import com.phms.utils.UserContext;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * 管理员权限控制类
 */
@Controller("Admin")
@RequestMapping("/admin")
public class Adminontroller {

	@Autowired
	private PageService pageService;
	@Autowired
	private RoleService roleService;
	@Autowired
	private PageRoleService pageRoleService;
	@Autowired
	private UserRoleService userRoleService;
	@Autowired
	private UserService userService;
	@Autowired
	private UserMapper userMapper;
	@Autowired
	private ResultMap resultMap;
	@Autowired
	private NotificationMessageService notificationMessageService;
	@Autowired
	private UserContext userContext;

	private final Logger logger = LoggerFactory.getLogger(Adminontroller.class);

	/**
	 * Method name: page <BR>
	 * Description: 跳转到页面设置页面 <BR>
	 * 
	 * @param model
	 * @return String<BR>
	 */
	// 已移除HTML前端，Vue前端使用自己的路由系统
	// @RequestMapping("/page")
	// public String page(Model model) {
	// 	List<Page> pageList = pageService.getAllPage();
	// 	model.addAttribute("pageList", pageList);
	// 	return "sa/page";
	// }

	/**
	 * Method name: role <BR>
	 * Description: 跳转到角色设置页面 <BR>
	 * 注意：已移除HTML前端，Vue前端使用自己的路由系统，此方法已注释
	 * 
	 * @param model
	 * @return String<BR>
	 */
	// @RequestMapping("/role")
	// public String role(Model model) {
	// 	return "sa/role";
	// }

	/**
	 * Method name: getAllRole <BR>
	 * Description: 获取所有权限 <BR>
	 * 
	 * @return List<Role><BR>
	 */
	@RequestMapping("/getAllRole")
	@ResponseBody
	public List<Role> getAllRole() {
		return roleService.getAllRole();
	}

	/**
	 * Method name: getAllPage <BR>
	 * Description: 获取所有页面 <BR>
	 * 
	 * @return List<Page><BR>
	 */
	@RequestMapping("/getAllPage")
	@ResponseBody
	public List<Page> getAllPage() {
		return pageService.getAllPage();
	}

	/**
	 * Method name: getPageByRole <BR>
	 * Description: 获取某个角色的权限页面 <BR>
	 */
	@RequestMapping("/getPageByRole")
	@ResponseBody
	public Object getPageByRole(Integer roleId) {
		return pageService.getAllPageByRoleId(roleId);
	}

	/**
	 * Method name: updatePageById <BR>
	 * Description: 根据页面id更新页面 <BR>
	 * 
	 * @param page
	 * @return ResultMap<BR>
	 */
	@RequestMapping("/updatePageById")
	@ResponseBody
	public ResultMap updatePageById(Page page) {
		return pageService.updatePageById(page);
	}

	/**
	 * Method name: addPage <BR>
	 * Description: 添加页面 <BR>
	 * 
	 * @param page
	 * @return Page<BR>
	 */
	@RequestMapping("/addPage")
	@ResponseBody
	public Page addPage(Page page) {
		return pageService.addPage(page);
	}

	/**
	 * Method name: delPageById <BR>
	 * Description: 根据页面id删除页面 <BR>
	 * 
	 * @param id
	 * @return ResultMap<BR>
	 */
	@RequestMapping("/delPageById")
	@ResponseBody
	public ResultMap delPageById(Integer id) {
		if (null == id) {
			return new ResultMap().fail().message("参数错误");
		}
		return pageService.delPageById(id);
	}

	/**
	 * Method name: addRole <BR>
	 * Description: 增加角色 <BR>
	 * 
	 * @param name
	 * @return String<BR>
	 */
	@RequestMapping("/addRole")
	@ResponseBody
	public String addRole(String name) {
		return roleService.addRole(name);
	}

	/**
	 * Method name: delManageRole <BR>
	 * Description: 根据角色id删除角色 <BR>
	 * 
	 * @param id
	 * @return String<BR>
	 */
	@RequestMapping("/delRole")
	@ResponseBody
	public String delRole(int id) {
		// 删除角色
		boolean flag1 = roleService.delRoleById(id);
		// 删除角色_权限表
		boolean flag2 = pageRoleService.delPageRoleByRoleId(id);
		// 删除某个角色的所有用户
		boolean flag3 = userRoleService.delUserRoleByRoleId(id);

		if (flag1 && flag2 && flag3) {
			return "SUCCESS";
		}
		return "ERROR";
	}

	/**
	 * Method name: updateRole <BR>
	 * Description: 根据权限id修改权限信息 <BR>
	 * 
	 * @param id
	 * @param name
	 * @return String<BR>
	 */
	@RequestMapping("/updateRole")
	@ResponseBody
	public String updateRole(Integer id, String name) {
		int n = roleService.updateRoleById(id, name);
		if (n != 0) {
			return "SUCCESS";
		}
		return "ERROR";
	}

	/**
	 * Method name: addPageRoleByRoleId <BR>
	 * Description: 增加某个角色的权限页面 <BR>
	 * 
	 * @param roleId
	 * @param pageIds
	 * @return String<BR>
	 */
	@RequestMapping("/addPageRoleByRoleId")
	@ResponseBody
	public String addPageRoleByRoleId(Integer roleId, Integer[] pageIds) {

		if (null == roleId) {
			return "ERROR";
		}

		// 先删除老的权限
		boolean flag1 = pageRoleService.delPageRoleByRoleId(roleId);
		boolean flag2 = pageRoleService.addPageRoles(roleId, pageIds);

		if (flag1 && flag2) {
			return "SUCCESS";
		}
		return "ERROR";
	}

	/**
	 * Method name: getAllUserByMap <BR>
	 * Description: 根据角色查询下面所有的人员/非角色下所有人员 <BR>
	 */
	@RequestMapping("/getAllUserByRoleId")
	@ResponseBody
	public Object getAllUserByRoleId(Integer roleId, String roleNot, Integer page, Integer limit) {
		if (null == roleNot) {
			return userService.getAllUserByRoleId(roleId, page, limit);
		}
		return userService.getAllUserByNotRoleId(roleId, page, limit);
	}

	/**
	 * Method name: delUserRoleByUserIdAndRoleId <BR>
	 * Description: 根据用户id权限id删除用户权限表 <BR>
	 * 
	 * @param userId
	 * @param roleId
	 * @return ResultMap<BR>
	 */
	@RequestMapping("/delUserRoleByUserIdAndRoleId")
	@ResponseBody
	public ResultMap delUserRoleByUserIdAndRoleId(String userId, Integer roleId) {
		// 移除用户角色：将用户角色设置为null或默认用户角色(3)
		try {
			Long uid = Long.parseLong(userId);
			String result = userService.updateUserRole(uid, 3); // 设置为普通用户
			if ("SUCCESS".equals(result)) {
				return resultMap.success().message("移除用户角色成功");
			} else {
				return resultMap.fail().message(result);
			}
		} catch (NumberFormatException e) {
			return resultMap.fail().message("无效的用户ID");
		}
	}

	/**
	 * Method name: selectUserRole <BR>
	 * Description: 跳转到选择用户角色页面 <BR>
	 * 
	 * @return String<BR>
	 */
	// 已移除HTML前端，Vue前端使用自己的路由系统
	// @RequestMapping("/selectUserRole")
	// public String selectUserRole() {
	// 	return "sa/selectUserRole";
	// }

	/**
	 * Method name: addUserRole <BR>
	 * Description: 增加用户的角色 <BR>
	 * 
	 * @param roleId
	 * @param userIds
	 * @return String<BR>
	 */
	@RequestMapping("/addUserRole")
	@ResponseBody
	public String addUserRole(Integer roleId, String[] userIds) {
		return userService.updateUsersRole(userIds, roleId);
	}

	/**
	 * Method name: userAddPage <BR>
	 * Description: 用户添加页面 <BR>
	 * 
	 * @return String<BR>
	 */
	@RequestMapping(value = "/userAddPage")
	public String userAddPage() {
		return "sa/userAdd";
	}

	/**
	 * Method name: userPage <BR>
	 * Description: 用户管理页面 <BR>
	 * 
	 * @return String<BR>
	 */
	@RequestMapping(value = "/userPage")
	public String userPage() {
		return "sa/userList";
	}

	/**
	 * Method name: getAllUserByLimit <BR>
	 * Description: 根据条件获取所有用户 <BR>
	 * 
	 * @param userParameter
	 * @return Object<BR>
	 */
	@RequestMapping("/getAllUserByLimit")
	@ResponseBody
	public Object getAllUserByLimit(UserParameter userParameter) {
		return userService.getAllUserByLimit(userParameter);
	}

	/**
	 * Method name: getAllDelUserByLimit <BR>
	 * Description: 获取所有删除用户 <BR>
	 * 
	 * @param userParameter
	 * @return Object<BR>
	 */
	@RequestMapping("/getAllDelUserByLimit")
	@ResponseBody
	public Object getAllDelUserByLimit(UserParameter userParameter) {
		return userService.getAllDelUserByLimit(userParameter);
	}

	/**
	 * Method name: delUser <BR>
	 * Description: 批量删除用户 <BR>
	 * 
	 * @param ids
	 * @return String<BR>
	 */
	@RequestMapping(value = "delUser")
	@ResponseBody
	@Transactional
	public String delUser(@RequestParam("ids") Long[] ids) {
		// 检查参数是否为空
		if (ids == null || ids.length == 0) {
			logger.warn("删除用户时参数为空");
			return "ERROR";
		}
		
		Subject subject = SecurityUtils.getSubject();
		User user = null;
		
		try {
			user = (User) subject.getPrincipal();
		} catch (Exception e) {
			logger.error("获取当前用户信息异常", e);
		}
		
		// 检查当前用户是否为空
		if (user == null || user.getId() == null) {
			logger.error("当前用户信息为空，无法执行删除操作。Subject: {}, Principal: {}", 
					subject != null ? "存在" : "为空", 
					subject != null && subject.getPrincipal() != null ? subject.getPrincipal().getClass().getName() : "无");
			// 如果用户信息为空，仍然允许删除操作，但不进行自我删除检查
			logger.warn("用户信息为空，跳过自我删除检查，继续执行删除操作");
		}
		
		try {
			for (Long id : ids) {
				// 检查id是否为空
				if (id == null) {
					logger.warn("删除用户时遇到空的用户ID，跳过");
					continue;
				}
				// 如果用户信息存在，检查是否尝试删除自己
				if (user != null && user.getId() != null) {
					if (Objects.equals(id, user.getId())) {
						return "DontOP";
					}
				}
				userService.delUserById(id);
			}
			return "SUCCESS";
		} catch (Exception e) {
			logger.error("根据用户id删除用户异常", e);
			TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
			return "ERROR";
		}
	}

	/**
	 * Method name: addUserPage <BR>
	 * Description: 增加用户界面 <BR>
	 * 注意：已移除HTML前端，Vue前端使用自己的路由系统，此方法已注释
	 * 
	 * @return String<BR>
	 */
	// @RequestMapping(value = "/addUserPage")
	// public String addUserPage(Long userId, Model model) {
	// 	model.addAttribute("manageUser", userId);
	// 	if (null != userId) {
	// 		User user = userService.selectByPrimaryKey(userId);
	// 		model.addAttribute("manageUser", user);
	// 	}
	// 	return "sa/userAdd";
	// }

	/**
	 * Method name: checkUserId <BR>
	 * Description: 检测用户账号是否存在 <BR>
	 * 
	 * @param userId
	 * @return User<BR>
	 */
	@ResponseBody
	@RequestMapping("/checkUserId")
	public User checkUserId(Long userId) {
		return userService.selectByPrimaryKey(userId);
	}

	/**
	 * Method name: addUser <BR>
	 * Description: 用户添加 <BR>
	 * 
	 * @param user
	 * @return String<BR>
	 */
	@ResponseBody
	@RequestMapping("/addUser")
	public String addUser(User user) {
		try {
			// 验证必填字段
			if (user.getPhone() == null || user.getPhone().trim().isEmpty()) {
				return "ERR:电话不能为空";
			}
			if (user.getName() == null || user.getName().trim().isEmpty()) {
				return "ERR:用户名不能为空";
			}
			if (user.getPassword() == null || user.getPassword().trim().isEmpty()) {
				return "ERR:密码不能为空";
			}
			
			// 检查用户名是否已存在（使用username字段）
			if (user.getUsername() != null && !user.getUsername().trim().isEmpty()) {
				User existingUser = userMapper.getByUsername(user.getUsername().trim());
			if (existingUser != null) {
				return "ERR:用户名已存在，请使用其他用户名";
				}
			} else {
				return "ERR:用户名不能为空";
			}
			
			user.setPassword(MD5.md5(user.getPassword()));
			user.setCreateTime(new Date());
			userService.addUser(user);

			User u = userService.getUserByPhoneAndName(user.getPhone(), user.getName());
			if (u == null || u.getId() == null) {
				logger.error("添加用户后无法获取用户信息，phone: {}, name: {}", user.getPhone(), user.getName());
				return "ERR:添加用户后无法获取用户信息";
			}

			// 设置医生角色 (role=2表示医生)
			userService.updateUserRole(u.getId(), 2);
			return "SUCCESS";
		} catch (Exception e) {
			logger.error("添加用户异常", e);
			return "ERR:" + e.getMessage();
		}
	}

	/**
	 * Method name: updateUser <BR>
	 * Description: 更新用户 <BR>
	 * 
	 * @param user
	 * @return String<BR>
	 */
	@ResponseBody
	@RequestMapping("/updateUser")
	public String updateUser(User user, Long oldId) {
		return userService.updateUser(oldId, user);
	}

	/**
	 * Method name: delUserPage <BR>
	 * Description: 已删除用户列表 <BR>
	 * 注意：已移除HTML前端，Vue前端使用自己的路由系统，此方法已注释
	 * 
	 * @return String<BR>
	 */
	// @RequestMapping("/delUserPage")
	// public String delUserPage() {
	// 	return "sa/userDelPage";
	// }

	/**
	 * Method name: resetPassword <BR>
	 * Description: 重置用户密码为用户名 <BR>
	 * 
	 * @param userId 用户ID
	 * @return String<BR>
	 */
	@ResponseBody
	@RequestMapping("/resetPassword")
	@Transactional
	public String resetPassword(Long userId) {
		if (userId == null) {
			logger.warn("重置密码失败: 用户ID为空");
			return "ERR:用户ID不能为空";
		}
		
		try {
			return userService.resetPassword(userId);
		} catch (Exception e) {
			logger.error("重置密码异常: userId={}", userId, e);
			TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
			return "ERR:" + (e.getMessage() != null ? e.getMessage() : "系统异常");
		}
	}

	/**
	 * 发送全局通知
	 * @param title 通知标题
	 * @param content 通知内容
	 * @param roleIds 角色ID列表（1=管理员, 2=医生, 3=用户），可以传多个，用逗号分隔，如"2,3"表示发送给医生和用户
	 * @return ResultMap
	 */
	@ResponseBody
	@RequestMapping(value = "/sendBroadcastNotification", method = org.springframework.web.bind.annotation.RequestMethod.POST)
	public ResultMap sendBroadcastNotification(
			@RequestParam("title") String title, 
			@RequestParam("content") String content, 
			@RequestParam("roleIds") String roleIds) {
		try {
			// 验证当前用户是否为管理员
			User currentUser = userContext.getCurrentUser();
			if (currentUser == null) {
				return resultMap.fail().message("用户未登录");
			}
			if (currentUser.getRole() == null || currentUser.getRole() != 1) {
				return resultMap.fail().message("只有管理员可以发送全局通知");
			}
			
			// 验证参数
			if (title == null || title.trim().isEmpty()) {
				return resultMap.fail().message("通知标题不能为空");
			}
			if (content == null || content.trim().isEmpty()) {
				return resultMap.fail().message("通知内容不能为空");
			}
			if (roleIds == null || roleIds.trim().isEmpty()) {
				return resultMap.fail().message("请至少选择一个角色");
			}
			
			// 解析角色ID列表
			String[] roleIdArray = roleIds.split(",");
			java.util.List<Integer> roleIdList = new java.util.ArrayList<>();
			for (String roleIdStr : roleIdArray) {
				try {
					Integer roleId = Integer.parseInt(roleIdStr.trim());
					if (roleId >= 1 && roleId <= 3) {
						roleIdList.add(roleId);
					} else {
						logger.warn("无效的角色ID: {}", roleId);
					}
				} catch (NumberFormatException e) {
					logger.warn("无法解析角色ID: {}", roleIdStr);
				}
			}
			
			if (roleIdList.isEmpty()) {
				return resultMap.fail().message("请至少选择一个有效的角色（1=管理员, 2=医生, 3=用户）");
			}
			
			// 发送全局通知
			int successCount = notificationMessageService.sendBroadcastNotification(
				roleIdList, title.trim(), content.trim(), currentUser.getId());
			
			logger.info("管理员 {} 发送全局通知成功 - 角色: {}, 成功数量: {}", 
				currentUser.getId(), roleIds, successCount);
			
			return resultMap.success().message("全局通知发送成功，共发送给 " + successCount + " 个用户");
			
		} catch (Exception e) {
			logger.error("发送全局通知异常", e);
			return resultMap.fail().message("发送全局通知失败: " + e.getMessage());
		}
	}
}
