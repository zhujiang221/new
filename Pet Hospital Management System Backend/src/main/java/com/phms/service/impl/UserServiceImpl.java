package com.phms.service.impl;

import com.phms.mapper.UserMapper;
import com.phms.model.MMGridPageVoBean;
import com.phms.model.ResultMap;
import com.phms.pojo.*;
import com.phms.service.UserService;
import com.phms.utils.MD5;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.subject.Subject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import java.util.ArrayList;
import java.util.List;

@Service
public class UserServiceImpl implements UserService {

	@Autowired
	private UserMapper userMapper;
	@Autowired
	private ResultMap resultMap;
	private final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

	/**
	 *      Description: 登录server <BR>
	 *      Remark: <BR>
	 * @param username 用户名
	 * @param password 密码
	 * @return <BR>
	 */
	@Override
	public ResultMap login(String username, String password) {
		// 从SecurityUtils里边创建一个 subject
		Subject subject = SecurityUtils.getSubject();
		// 在认证提交前准备 token（令牌）
		UsernamePasswordToken token = new UsernamePasswordToken(username, MD5.md5(password));
		// 执行认证登陆
		try {
			subject.login(token);
			
			// 登录成功后，从数据库重新获取用户信息（避免 session 问题）
			User user = userMapper.getByUsername(username);
			if (user == null) {
				logger.error("登录成功但无法获取用户信息: username={}", username);
				return resultMap.fail().message("获取用户信息失败");
			}
			
			// 根据权限，指定返回数据（从user表的role字段获取）
			if (user.getRole() != null) {
				Integer role = user.getRole();
				String roleName = role == 1 ? "管理员" : (role == 2 ? "医生" : "用户");
				logger.info("欢迎登录------您的权限是{}", roleName);
				// 返回用户信息，包括角色
				return resultMap.success().message("欢迎登陆").data(user);
			}
			return resultMap.fail().message("权限错误！");
		} catch (Exception e) {
			logger.error("登录失败: {}", e.getMessage(), e);
			// 返回更友好的错误信息
			String errorMsg = e.getMessage();
			if (errorMsg == null || errorMsg.isEmpty()) {
				errorMsg = "登录失败，请检查用户名和密码";
			}
			return resultMap.fail().message(errorMsg);
		}
	}

	/**
	 *      Description: 检测用户旧密码是否正确 <BR>
	 *      Remark: <BR>
	 * @param password
	 * @return <BR>
	 */
	@Override
	public boolean checkUserPassword(String password) {
		Subject subject = SecurityUtils.getSubject();
		User user = (User) subject.getPrincipal();
		// 防止session失效报错
		if (null != user) {
			String pass = user.getPassword();
			if (pass != null && pass.equals(MD5.md5(password))) {
				return true;
			}
		}
		return false;
	}

	/**
	 *      Description: 修改密码 <BR>
	 *      Remark: <BR>
	 * @param password
	 * @return <BR>
	 */
	@Override
	@Transactional // 事物注解
	public String updatePassword(String password) {
		Subject subject = SecurityUtils.getSubject();
		User user = (User) subject.getPrincipal();
		int n = 0;
		// 防止session失效报错
		if (null != user) {
			User upUser = new User();
			upUser.setPassword(MD5.md5(password));
			UserExample userExample = new UserExample();
			try {
				userExample.createCriteria().andIdEqualTo(user.getId());
				n = userMapper.updateByExampleSelective(upUser, userExample);
			} catch (Exception e) {
				logger.error("修改密码异常", e);
				// 事物回滚
				TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
				return "ERROR";
			}
		}
		if (n != 0) {
			return "SUCCESS";
		}
		return "ERROR";
	}

	/**
	 *      Description: 根据用户id查询用户信息 <BR>
	 *      Remark: <BR>
	 * @param userId
	 * @return <BR>
	 */
	@Override
	public User selectUserByUserId(Long userId) {
		try {
			return userMapper.selectByPrimaryKey(userId);
		} catch (Exception e) {
			logger.error("根据用户id查询用户信息异常", e);
			return null;
		}
	}

	/**
	 *      Description: 根据权限id获取权限下所有用户 <BR>
	 *      Remark: <BR>
	 * @param roleId
	 * @return <BR>
	 */
	@Override
	public Object getAllUserByRoleId(Integer roleId, Integer page, Integer limit) {
		int size = userMapper.countAllUserByRoleId(roleId);

		Integer begin = (page - 1) * limit;
		Integer count = limit;

		List<User> rows = new ArrayList<>();
		try {
			rows = userMapper.getAllUserByRoleId(roleId, begin, count);
		} catch (Exception e) {
			logger.error("根据权限id获取权限下所有用户异常", e);
		}

		MMGridPageVoBean<User> vo = new MMGridPageVoBean<>();
		vo.setTotal(size);
		vo.setRows(rows);

		return vo;
	}

	/**
	 *      Description: 根据权限id获取不是这个权限所有用户 <BR>
	 *      Remark: <BR>
	 * @param roleId
	 * @return <BR>
	 */
	@Override
	public Object getAllUserByNotRoleId(Integer roleId, Integer page, Integer limit) {
		int size = userMapper.countAllUserByNotRoleId(roleId);

		Integer begin = (page - 1) * limit;
		Integer count = limit;

		List<User> rows = new ArrayList<>();
		try {
			rows = userMapper.getAllUserByNotRoleId(roleId, begin, count);
		} catch (Exception e) {
			logger.error("根据权限id获取不是这个权限所有用户异常", e);
		}

		MMGridPageVoBean<User> vo = new MMGridPageVoBean<>();
		vo.setTotal(size);
		vo.setRows(rows);

		return vo;
	}

	/**
	 *      Description: 根据用户id查看用户是否存在 <BR>
	 *      Remark: <BR>
	 * @param userId
	 * @return <BR>
	 */
	@Override
	public User selectByPrimaryKey(Long userId) {
		return userMapper.selectByPrimaryKey(userId);
	}

	@Override
	public List<User> getAdmins() {
		// 使用 getAllUserByRoleId 方法查询管理员（role=1）
		// 传入 null 作为分页参数，获取所有管理员
		return userMapper.getAllUserByRoleId(1, null, null);
	}

	/**
	 *      Description: 根据条件查询用户 <BR>
	 *      Remark: <BR>
	 * @param userParameter
	 * @return <BR>
	 */
	@Override
	public Object getAllUserByLimit(UserParameter userParameter) {
		int size = 0;

		Integer begin = (userParameter.getPage() - 1) * userParameter.getLimit();
		userParameter.setPage(begin);

		List<User> rows = new ArrayList<>();
		try {
			rows = userMapper.getAllUserByLimit(userParameter);
			size = userMapper.countAllUserByLimit(userParameter);
			
			// 保护用户隐私：清空电话和地址信息
			for (User user : rows) {
				user.setPhone(null);
				user.setAddress(null);
			}
		} catch (Exception e) {
			logger.error("根据条件查询用户 异常", e);
		}
		MMGridPageVoBean<User> vo = new MMGridPageVoBean<>();
		vo.setTotal(size);
		vo.setRows(rows);

		return vo;
	}

	/**
	 *      Description: 删除用户 <BR>
	 *      Remark: <BR>
	 * @param id <BR>
	 */
	@Override
	@Transactional
	public void delUserById(Long id) {
		try {
			// 改变用户状态位为删除
			UserExample userExample = new UserExample();
			userExample.createCriteria().andIdEqualTo(id);
			userMapper.deleteByExample(userExample);// 删除用户
		} catch (Exception e) {
			logger.error("删除用户出现异常", e);
			TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
		}
	}

	/**
	 *      Description: 增加单个用户,抛出异常 <BR>
	 *      Remark: <BR>
	 * @param user
	 * @throws Exception <BR>
	 */
	@Override
	public void addUser(User user) throws Exception {
		userMapper.insert(user);
	}

	/**
	 *      Description: 根据用户id更新用户 <BR>
	 *      Remark: <BR>
	 * @param user
	 * @return <BR>
	 */
	@Override
	@Transactional
	public String updateUser(User user) {
		try {
			// 如果更新了邮箱，检查邮箱是否已被其他用户使用
			if (user.getEmail() != null && !user.getEmail().trim().isEmpty()) {
				User existingUser = userMapper.getByEmail(user.getEmail().trim());
				if (existingUser != null && !existingUser.getId().equals(user.getId())) {
					return "EMAIL_EXISTS"; // 邮箱已被其他用户使用
				}
			}
			
			String pass = user.getPassword();
			if (pass != null && !pass.trim().equals("")) {
				// 去除密码前后空白字符（包括换行符）后再进行MD5加密
				user.setPassword(MD5.md5(pass.trim()));
			}
			userMapper.updateByPrimaryKeySelective(user);
			return "SUCCESS";
		} catch (Exception e) {
			logger.error("根据用户id更新用户异常", e);
			TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
			return "ERR";
		}
	}

	@Override
	public List<User> getAllUser() {
		// 直接查询所有用户（不再通过role表过滤）
		return userMapper.selectAllUser();
	}

	/**
	 * Description: 根据id获取管理员 <BR>
	 *
	 * @param userId
	 * @return
	 */
	@Override
	public User getAdminById(Long userId) {
		return userMapper.selectByPrimaryKey(userId);
	}

	@Override
	public Object getAllDelUserByLimit(UserParameter userParameter) {
		int size = 0;

		Integer begin = (userParameter.getPage() - 1) * userParameter.getLimit();
		userParameter.setPage(begin);

		List<User> rows = new ArrayList<>();
		try {
			rows = userMapper.getAllDelUserByLimit(userParameter);
			size = userMapper.countAllDelUserByLimit(userParameter);
		} catch (Exception e) {
			logger.error("根据条件查询用户 异常", e);
		}
		MMGridPageVoBean<User> vo = new MMGridPageVoBean<>();
		vo.setTotal(size);
		vo.setRows(rows);

		return vo;
	}


	/**
	 *      Description: 根据旧用户id更改用户信息 <BR>
	 *      Remark: <BR>
	 * @param oldId
	 * @param user
	 * @return <BR>
	 */
	@Override
	public String updateUser(Long oldId, User user) {
		UserExample example = new UserExample();
		example.createCriteria().andIdEqualTo(oldId);
		try {
			String pass = user.getPassword();
			boolean xgpass = false;
			User oldUser = userMapper.selectByPrimaryKey(oldId);
			if (pass != null && !pass.trim().equals("")) {
				// 去除密码前后空白字符（包括换行符）后再进行MD5加密
				user.setPassword(MD5.md5(pass.trim()));
				xgpass = true;
			} else {
				user.setPassword(oldUser.getPassword());
			}
			user.setCreateTime(oldUser.getCreateTime());
			
			// 如果role字段为空，保持原有role
			if (user.getRole() == null) {
				user.setRole(oldUser.getRole());
			}

			userMapper.updateByExample(user, example);

			// 系统管理员自己修改自己
			Subject subject = SecurityUtils.getSubject();
			User ad = (User) subject.getPrincipal();
			// 修改自己账号要重新登录
			// 修改自己密码要重新登录
			if (!oldId.equals(user.getId()) && oldId.equals(ad.getId())) {
				return "LGINOUT";
			} else if (oldId.equals(ad.getId()) && xgpass) {
				return "LGINOUT";
			}
			return "SUCCESS";
		} catch (Exception e) {
			logger.error("根据用户id更新用户异常", e);
			TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
			return "ERR";
		}
	}

	@Override
	public List<User> selectAllUser() {
		
		return userMapper.selectAllUser();
	}

	@Override
	public User getUserByPhoneAndName(String phone, String name) {
		return userMapper.getUserByPhoneAndName(phone, name);
	}

	@Override
	public User getUserByUsername(String username) {
		// 根据username字段查询用户
		return userMapper.getByUsername(username);
	}

	@Override
	public User getUserByEmail(String email) {
		// 根据邮箱查询用户
		return userMapper.getByEmail(email);
	}

	@Override
	public User getByEmail(String email) {
		if (email == null || email.trim().isEmpty()) {
			return null;
		}
		return userMapper.getByEmail(email.trim());
	}

	@Override
	public void save(User user) {
		userMapper.insert(user);
	}

	@Override
	public User getByIdCard(String idCard) {
		return userMapper.selectByIdCard(idCard);
	}

	@Override
	public List<User> listDoctor() {
		UserExample example = new UserExample();
		example.createCriteria().andDepartmentIsNotNull();
		return userMapper.selectByExample(example);
	}

	@Override
	@Transactional
	public String resetPassword(Long userId) {
		try {
			// 获取用户信息
			User user = userMapper.selectByPrimaryKey(userId);
			if (user == null) {
				logger.warn("重置密码失败: 用户不存在, userId={}", userId);
				return "ERR:用户不存在";
			}
			
			// 检查邮箱是否存在
			if (user.getEmail() == null || user.getEmail().trim().isEmpty()) {
				logger.warn("重置密码失败: 邮箱为空, userId={}", userId);
				return "ERR:邮箱为空，无法重置密码";
			}
			
			// 获取邮箱前6位作为新密码
			String email = user.getEmail().trim();
			String newPassword = email.length() >= 6 ? email.substring(0, 6) : email;
			
			// 将密码重置为邮箱前6位（MD5加密）
			User updateUser = new User();
			updateUser.setPassword(MD5.md5(newPassword));
			UserExample userExample = new UserExample();
			userExample.createCriteria().andIdEqualTo(userId);
			
			int n = userMapper.updateByExampleSelective(updateUser, userExample);
			if (n > 0) {
				logger.info("重置密码成功: userId={}, username={}", userId, user.getUsername());
				return "SUCCESS";
			} else {
				logger.warn("重置密码失败: 更新失败, userId={}", userId);
				return "ERR:更新失败";
			}
		} catch (Exception e) {
			logger.error("重置密码异常: userId={}", userId, e);
			TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
			return "ERR:" + (e.getMessage() != null ? e.getMessage() : "系统异常");
		}
	}

	@Override
	@Transactional
	public String updateUserRole(Long userId, Integer roleId) {
		try {
			User user = userMapper.selectByPrimaryKey(userId);
			if (user == null) {
				logger.warn("更新用户角色失败: 用户不存在, userId={}", userId);
				return "ERR:用户不存在";
			}
			
			User updateUser = new User();
			updateUser.setRole(roleId);
			UserExample userExample = new UserExample();
			userExample.createCriteria().andIdEqualTo(userId);
			
			int n = userMapper.updateByExampleSelective(updateUser, userExample);
			if (n > 0) {
				logger.info("更新用户角色成功: userId={}, roleId={}", userId, roleId);
				return "SUCCESS";
			} else {
				logger.warn("更新用户角色失败: 更新失败, userId={}", userId);
				return "ERR:更新失败";
			}
		} catch (Exception e) {
			logger.error("更新用户角色异常: userId={}, roleId={}", userId, roleId, e);
			TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
			return "ERR:" + (e.getMessage() != null ? e.getMessage() : "系统异常");
		}
	}

	@Override
	@Transactional
	public String updateUsersRole(String[] userIds, Integer roleId) {
		try {
			if (userIds == null || userIds.length == 0) {
				return "ERR:用户ID数组为空";
			}
			
			int successCount = 0;
			for (String userIdStr : userIds) {
				try {
					Long userId = Long.parseLong(userIdStr);
					String result = updateUserRole(userId, roleId);
					if ("SUCCESS".equals(result)) {
						successCount++;
					}
				} catch (NumberFormatException e) {
					logger.warn("无效的用户ID: {}", userIdStr);
				}
			}
			
			if (successCount > 0) {
				logger.info("批量更新用户角色成功: 成功更新{}个用户, roleId={}", successCount, roleId);
				return "SUCCESS";
			} else {
				return "ERR:没有成功更新任何用户";
			}
		} catch (Exception e) {
			logger.error("批量更新用户角色异常: roleId={}", roleId, e);
			TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
			return "ERR:" + (e.getMessage() != null ? e.getMessage() : "系统异常");
		}
	}

	@Override
	public Integer getUserRoleId(Long userId) {
		try {
			User user = userMapper.selectByPrimaryKey(userId);
			if (user != null) {
				return user.getRole();
			}
			return null;
		} catch (Exception e) {
			logger.error("获取用户角色异常: userId={}", userId, e);
			return null;
		}
	}
}
