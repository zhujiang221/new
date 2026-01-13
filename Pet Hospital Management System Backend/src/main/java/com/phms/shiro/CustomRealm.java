package com.phms.shiro;

import com.phms.mapper.UserMapper;
import com.phms.pojo.User;
import com.phms.utils.JwtUtil;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.*;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

@Component
public class CustomRealm extends AuthorizingRealm {
	/** 用户信息service */
	private final UserMapper userMapper;
	/** logback日志记录 */
	private final Logger logger = LoggerFactory.getLogger(CustomRealm.class);
	
	@Autowired
	private JwtUtil jwtUtil;

	public CustomRealm(UserMapper userMapper) {
		this.userMapper = userMapper;
	}
	
	/**
	 * 指定此Realm只支持JwtToken
	 */
	@Override
	public boolean supports(AuthenticationToken token) {
		return token instanceof JwtToken;
	}


	/**
	 * @Override
	 * @see org.apache.shiro.realm.AuthenticatingRealm#doGetAuthenticationInfo(AuthenticationToken)
	 *      <BR>
	 *      Method name: doGetAuthenticationInfo <BR>
	 *      获取身份验证信息 Description: Shiro中，最终是通过 Realm 来获取应用程序中的用户、角色及权限信息的。 <BR>
	 * @param authenticationToken 用户身份信息 token（JwtToken）
	 * @return 返回封装了用户信息的 AuthenticationInfo 实例
	 * @throws AuthenticationException <BR>
	 */
	@Override
	protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken authenticationToken)
			throws AuthenticationException {
		// 获取JwtToken
		JwtToken jwtToken = (JwtToken) authenticationToken;
		String token = jwtToken.getToken();
		
		// 从Token中解析用户名
		String username = jwtUtil.getUsernameFromToken(token);
		if (username == null) {
			logger.warn("JWT Token解析失败，无法获取用户名");
			throw new AuthenticationException("Token无效");
		}
		
		// 从数据库根据username字段获取对应用户
		User user = userMapper.getByUsername(username);
		if (null == user) {
			logger.warn("{}---用户不存在", username);
			throw new AccountException("USERNAME_NOT_EXIST");
		}
		
		logger.info("{}---JWT身份认证成功", user.getName());
		
		// 返回认证信息（JWT不需要密码验证，Token已经验证过了）
		SimpleAuthenticationInfo authInfo = new SimpleAuthenticationInfo(user, token, getName());
		
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
			logger.warn("用户信息或角色信息为空!");
		}
		// 设置该用户拥有的角色
		info.setRoles(roles);
		return info;
	}
}