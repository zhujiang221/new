package com.phms.shiro;

import com.phms.mapper.UserMapper;
import com.phms.pojo.User;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.*;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.session.Session;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.subject.Subject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class CustomRealm extends AuthorizingRealm {
	/** 用户信息service */
	private final UserMapper userMapper;
	/** logback日志记录 */
	private final Logger logger = LoggerFactory.getLogger(CustomRealm.class);

	private static Map<String, Session> sessionMap = new HashMap<>();

	public CustomRealm(UserMapper userMapper) {
		this.userMapper = userMapper;
	}


	/**
	 * @Override
	 * @see org.apache.shiro.realm.AuthenticatingRealm#doGetAuthenticationInfo(AuthenticationToken)
	 *      <BR>
	 *      Method name: doGetAuthenticationInfo <BR>
	 *      获取身份验证信息 Description: Shiro中，最终是通过 Realm 来获取应用程序中的用户、角色及权限信息的。 <BR>
	 * @param authenticationToken 用户身份信息 token
	 * @return 返回封装了用户信息的 AuthenticationInfo 实例
	 * @throws AuthenticationException <BR>
	 */
	@Override
	protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken authenticationToken)
			throws AuthenticationException {
		// 获取token令牌
		UsernamePasswordToken token = (UsernamePasswordToken) authenticationToken;
		// 从数据库根据username字段获取对应用户名密码的用户
		User user = userMapper.getByUsername(token.getUsername());
		if (null == user) {
			logger.warn("{}---用户不存在", token.getUsername());
			// 向前台抛出用户不存在json对象
			throw new AccountException("USERNAME_NOT_EXIST");
		}
		String password = user.getPassword();
		if (null == password) {
			logger.warn("{}---用户不存在", token.getUsername());
			// 向前台抛出用户不存在json对象
			throw new AccountException("USERNAME_NOT_EXIST");
		} else if (!password.equals(new String((char[]) token.getCredentials()))) {
			logger.warn("{}---输入密码错误", token.getUsername());
			// 向前台抛出输入密码错误json对象
			throw new AccountException("PASSWORD_ERR");
		}
		logger.info("{}---身份认证成功", user.getName());
		
		// 先返回认证信息，让 Shiro 完成认证流程
		SimpleAuthenticationInfo authInfo = new SimpleAuthenticationInfo(user, password, getName());
		
		// 在认证成功后，异步处理 session 管理（避免在认证过程中访问 session 导致的问题）
		try {
			Subject subject = SecurityUtils.getSubject();
			if (subject != null) {
				// 确保 session 已创建
				Session session = subject.getSession(false);
				if (session != null) {
					// 设置shiro session过期时间(单位是毫秒!) 2小时
					session.setTimeout(7_200_000);
					
					String uid = user.getId() + "";
					
					// 处理同一用户多次登录的情况：清除旧 session
					Session oldSession = sessionMap.get(uid);
					if (oldSession != null) {
						try {
							oldSession.setTimeout(0);
							oldSession.stop();
						} catch (Exception e) {
							logger.warn("清除旧 session 失败: {}", e.getMessage());
						}
						sessionMap.remove(uid);
					}
					
					// 保存当前 session 到全局变量
					sessionMap.put(uid, session);
				}
			}
		} catch (Exception e) {
			logger.warn("处理 session 时出现异常，但不影响登录: {}", e.getMessage());
		}
		
		return authInfo;
	}

	/**
	 * @Override
	 * @see AuthorizingRealm#doGetAuthorizationInfo(PrincipalCollection)
	 *      <BR>
	 *      Method name: doGetAuthorizationInfo <BR>
	 *      Description: 获取授权信息 <BR>
	 * @param principalCollection
	 * @return <BR>
	 */
	@Override
	protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principalCollection) {
		// 从shro里面获取用户对象
		User user = (User) SecurityUtils.getSubject().getPrincipal();
		SimpleAuthorizationInfo info = new SimpleAuthorizationInfo();
		// 角色列表
		Set<String> roles = new HashSet<>();
		// 获得该用户角色（从user表的role字段获取）
		if (null != user && user.getRole() != null) {
			// role: 1=管理员, 2=医生, 3=用户
			Integer role = user.getRole();
			if (role == 1) {
				roles.add("admin");
			} else if (role == 2) {
				roles.add("doctor");
			} else if (role == 3) {
				roles.add("user");
			}
		} else {
			logger.warn("用户session失效或角色信息为空!");
		}
		// 设置该用户拥有的角色
		info.setRoles(roles);
		return info;
	}
}