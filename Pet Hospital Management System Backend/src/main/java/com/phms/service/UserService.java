package com.phms.service;

import com.phms.model.ResultMap;
import com.phms.pojo.User;
import com.phms.pojo.UserParameter;

import java.util.List;

public interface UserService {
	User selectUserByUserId(Long userId);

	public ResultMap login(String username, String password);

	public boolean checkUserPassword(String password);

	public String updatePassword(String password);

	Object getAllUserByRoleId(Integer roleId, Integer page, Integer limit);

	Object getAllUserByNotRoleId(Integer roleId, Integer page, Integer limit);

	User selectByPrimaryKey(Long userId);

	List<User> getAdmins();

	Object getAllUserByLimit(UserParameter userParameter);

	void delUserById(Long id);

	void addUser(User user) throws Exception;

	String updateUser(Long oldId, User user);

	String updateUser(User user);

	List<User> getAllUser();

	User getAdminById(Long userId);

	Object getAllDelUserByLimit(UserParameter userParameter);

	List<User> selectAllUser();

	User getUserByPhoneAndName(String phone, String name);

	User getUserByUsername(String username);

	/**
	 * 根据邮箱查询用户
	 * @param email 邮箱
	 * @return 用户对象
	 */
	User getUserByEmail(String email);

	/**
	 * 根据邮箱查询用户（使用Mapper方法）
	 * @param email 邮箱
	 * @return 用户对象
	 */
	User getByEmail(String email);

    void save(User user);

	User getByIdCard(String idCard);

    List<User> listDoctor();

	/**
	 * 重置用户密码为用户名
	 * @param userId 用户ID
	 * @return 操作结果
	 */
	String resetPassword(Long userId);

	/**
	 * 更新用户的角色
	 * @param userId 用户ID
	 * @param roleId 角色ID (1=管理员, 2=医生, 3=用户)
	 * @return 操作结果
	 */
	String updateUserRole(Long userId, Integer roleId);

	/**
	 * 批量更新用户的角色
	 * @param userIds 用户ID数组
	 * @param roleId 角色ID (1=管理员, 2=医生, 3=用户)
	 * @return 操作结果
	 */
	String updateUsersRole(String[] userIds, Integer roleId);

	/**
	 * 获取用户的角色ID
	 * @param userId 用户ID
	 * @return 角色ID，如果用户不存在或没有角色则返回null
	 */
	Integer getUserRoleId(Long userId);
}
