package com.phms.controller.open;


import com.phms.model.ResultMap;
import com.phms.pojo.User;
import com.phms.service.EmailCodeService;
import com.phms.service.UserService;
import com.phms.utils.CaptchaUtil;
import com.phms.utils.JwtUtil;
import com.phms.utils.MD5;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import org.springframework.data.redis.core.StringRedisTemplate;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * 登录控制类
 */
@Controller("OpenLogin")
@RequestMapping()
public class LoginController {
	@Autowired
	private ResultMap resultMap;
	@Autowired
	private UserService userService;
	@Autowired
	private EmailCodeService emailCodeService;
	@Autowired
	private StringRedisTemplate stringRedisTemplate;
	@Autowired
	private JwtUtil jwtUtil;

	private final Logger logger = LoggerFactory.getLogger(LoginController.class);
	
	// Redis中验证码存储的key前缀
	private static final String CAPTCHA_KEY_PREFIX = "captcha:";

	/**
	 * 返回 尚未登陆信息
	 */
	@RequestMapping(value = "/notLogin", method = RequestMethod.GET)
	@ResponseBody
	public ResultMap notLogin() {
		logger.warn("尚未登陆！");
		return resultMap.success().message("您尚未登陆！");
	}

	/**
	 * 返回 没有权限
	 */
	@RequestMapping(value = "/notRole", method = RequestMethod.GET)
	@ResponseBody
	public ResultMap notRole() {
		Subject subject = SecurityUtils.getSubject();
		User user = (User) subject.getPrincipal();
		if (user != null) {
			logger.info("{}---没有权限！", user.getName());
		}
		return resultMap.success().message("您没有权限！");
	}
/**演示页面** - 已移除HTML前端，这些方法已注释
	@RequestMapping(value = "/demo/table", method = RequestMethod.GET)
	public String demoTable() {
		return "table";
	}

	@RequestMapping(value = "/demo/tu", method = RequestMethod.GET)
	public String demoTu() {
		return "tu";
	}

	@RequestMapping(value = "/demo/tu1", method = RequestMethod.GET)
	public String tu1() {
		return "tu1";
	}

	@RequestMapping(value = "/demo/tu2", method = RequestMethod.GET)
	public String tu2() {
		return "tu2";
	}

	@RequestMapping(value = "/demo/tu3", method = RequestMethod.GET)
	public String tu3() {
		return "tu3";
	}
/**演示页面**/
	/**
	 * Method name: logout <BR>
	 * Description: 退出登录 <BR>
	 * 注意：已移除HTML前端，此方法改为返回JSON响应，Vue前端使用此接口
	 * 清除Redis中的Token，实现单设备登录
	 * @return ResultMap<BR>
	 */
	@RequestMapping(value = "/logout", method = RequestMethod.GET)
	@ResponseBody
	public ResultMap logout() {
		Subject subject = SecurityUtils.getSubject();
		User user = (User) subject.getPrincipal();
		if (null != user) {
			logger.info("{}---退出登录！", user.getName());
			// 清除Redis中的Token，使Token失效
			jwtUtil.invalidateToken(user.getUsername());
		}
		return resultMap.success().message("退出登录成功");
	}

	/**
	 * 生成验证码图片
	 * 
	 * @return ResultMap 包含验证码图片base64编码和唯一标识
	 */
	@RequestMapping(value = "/captcha", method = RequestMethod.GET)
	@ResponseBody
	public ResultMap getCaptcha() {
		try {
			// 生成验证码
			CaptchaUtil.CaptchaResult captchaResult = CaptchaUtil.generateCaptcha();
			String code = captchaResult.getCode();
			BufferedImage image = captchaResult.getImage();
			
			// 生成验证码唯一标识
			String captchaId = UUID.randomUUID().toString().replace("-", "");
			
			// 将验证码存储到Redis，设置5分钟过期
			String captchaKey = CAPTCHA_KEY_PREFIX + captchaId;
			stringRedisTemplate.opsForValue().set(captchaKey, code, 5, TimeUnit.MINUTES);
			
			// 将图片转换为base64编码
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ImageIO.write(image, "jpeg", baos);
			byte[] imageBytes = baos.toByteArray();
			String imageBase64 = Base64.getEncoder().encodeToString(imageBytes);
			
			// 返回验证码图片base64编码和唯一标识
			ResultMap result = resultMap.success();
			result.put("captchaId", captchaId);
			result.put("image", "data:image/jpeg;base64," + imageBase64);
			
			return result;
		} catch (IOException e) {
			logger.error("生成验证码失败", e);
			return resultMap.fail().message("生成验证码失败");
		}
	}

	/**
	 * Method name: login <BR>
	 * Description: 登录验证 <BR>
	 * Remark: <BR>
	 * 
	 * @param username 用户名
	 * @param password 密码
	 * @param captcha 验证码
	 * @param captchaId 验证码唯一标识
	 * @return ResultMap<BR>
	 */
	@RequestMapping(value = "/login")
	@ResponseBody
	public ResultMap login(String username, String password, String captcha, String captchaId) {
		// 验证验证码
		if (captchaId == null || captchaId.trim().isEmpty()) {
			return resultMap.fail().message("验证码标识不能为空");
		}
		
		String captchaKey = CAPTCHA_KEY_PREFIX + captchaId;
		String storedCaptcha = stringRedisTemplate.opsForValue().get(captchaKey);
		
		// 检查验证码是否存在
		if (storedCaptcha == null) {
			return resultMap.fail().message("验证码已过期，请刷新后重试");
		}
		
		// 验证验证码（不区分大小写）
		if (captcha == null || !storedCaptcha.equalsIgnoreCase(captcha.trim())) {
			// 验证失败后清除验证码，需要重新获取
			stringRedisTemplate.delete(captchaKey);
			return resultMap.fail().message("验证码错误");
		}
		
		// 验证码正确，清除Redis中的验证码（一次性使用）
		stringRedisTemplate.delete(captchaKey);
		
		// 执行登录逻辑
		ResultMap loginResult = userService.login(username, password);
		
		// 如果登录成功，生成JWT Token并返回
		if ("success".equals(loginResult.get("result"))) {
			// 生成JWT Token
			String token = jwtUtil.generateToken(username);
			// 创建新的ResultMap，避免修改共享的resultMap导致并发问题
			ResultMap response = new ResultMap();
			response.putAll(loginResult);
			response.put("token", token);
			return response;
		}
		
		return loginResult;
	}

	/**
	 * Method name: login <BR>
	 * Description: 登录页面 <BR>
	 * 注意：已移除HTML前端，Vue前端使用自己的路由系统，此方法已注释
	 * 
	 * @return String login.html<BR>
	 */
	// @RequestMapping(value = "/index")
	// public String login() {
	// 	return "login";
	// }

	/**
	 * 注册页面 regist.html
	 * 注意：已移除HTML前端，Vue前端使用自己的路由系统，此方法已注释
	 */
	// @RequestMapping(value = "/regist")
	// public String regist() {
	// 	return "regist";
	// }

	/**
	 * 发送邮箱验证码
	 */
	@RequestMapping(value = "/sendEmailCode", method = RequestMethod.POST)
	@ResponseBody
	public ResultMap sendEmailCode(String email) {
		logger.info("发送验证码请求: email={}", email);
		
		// 验证邮箱格式
		if (email == null || email.trim().isEmpty()) {
			logger.warn("发送验证码失败: 邮箱为空");
			return resultMap.fail().message("邮箱不能为空");
		}
		
		String emailPattern = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
		if (!email.trim().matches(emailPattern)) {
			logger.warn("发送验证码失败: 邮箱格式不正确, email={}", email);
			return resultMap.fail().message("邮箱格式不正确，请检查后重新输入");
		}
		
		// 检查邮箱是否已被注册
		User existingUserByEmail = userService.getUserByEmail(email.trim());
		if (existingUserByEmail != null) {
			logger.warn("发送验证码失败: 邮箱已被注册, email={}", email);
			return resultMap.fail().message("该邮箱已被注册请直接登录");
		}
		
		// 发送验证码
		boolean sendSuccess = emailCodeService.sendCode(email.trim());
		
		if (sendSuccess) {
			logger.info("验证码发送成功: email={}", email);
			return resultMap.success().message("验证码已发送");
		} else {
			logger.error("验证码发送失败: email={}", email);
			return resultMap.fail().message("验证码发送失败，请稍后重试");
		}
	}

	/**
	 * 注册
	 */
	@RequestMapping(value = "/doRegist", method = RequestMethod.POST)
	@ResponseBody
	public ResultMap doRegist(String username, String name, String phone, String email, String password, String password2, String emailCode) {
		logger.info("注册请求接收: username={}, name={}, phone={}, email={}, password长度={}", 
				username, name, phone, email, password != null ? password.length() : 0);
		
		// 参数验证
		if (username == null || username.trim().isEmpty()) {
			logger.warn("注册失败: 用户名为空");
			return resultMap.fail().message("用户名不能为空");
		}
		if (name == null || name.trim().isEmpty()) {
			logger.warn("注册失败: 姓名为空");
			return resultMap.fail().message("姓名不能为空");
		}
		if (phone == null || phone.trim().isEmpty()) {
			logger.warn("注册失败: 电话号为空");
			return resultMap.fail().message("电话号不能为空");
		}
		if (email == null || email.trim().isEmpty()) {
			logger.warn("注册失败: 邮箱为空");
			return resultMap.fail().message("邮箱不能为空");
		}
		if (password == null || password.trim().isEmpty()) {
			logger.warn("注册失败: 密码为空");
			return resultMap.fail().message("密码不能为空");
		}
		if (password2 == null || password2.trim().isEmpty()) {
			logger.warn("注册失败: 确认密码为空");
			return resultMap.fail().message("确认密码不能为空");
		}
		
		// 验证密码长度
		if (password.length() < 6) {
			logger.warn("注册失败: 密码长度不足，当前长度={}", password.length());
			return resultMap.fail().message("密码长度不能小于6位");
		}
		
		// 验证两次密码是否一致
		if (!password.equals(password2)) {
			logger.warn("注册失败: 两次密码不一致");
			return resultMap.fail().message("两次输入的密码不一致，请重新输入");
		}
		
		// 验证邮箱格式
		String emailPattern = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
		if (!email.trim().matches(emailPattern)) {
			logger.warn("注册失败: 邮箱格式不正确, email={}", email);
			return resultMap.fail().message("邮箱格式不正确，请检查后重新输入");
		}
		
		// 验证邮箱验证码
		if (emailCode == null || emailCode.trim().isEmpty()) {
			logger.warn("注册失败: 邮箱验证码为空");
			return resultMap.fail().message("请输入邮箱验证码");
		}
		
		String verifyResult = emailCodeService.verifyCode(email.trim(), emailCode.trim());
		if (verifyResult != null) {
			logger.warn("注册失败: 邮箱验证码验证失败, email={}, error={}", email, verifyResult);
			return resultMap.fail().message(verifyResult);
		}
		
		// 验证电话号格式（简单验证：11位数字）
		String phonePattern = "^1[3-9]\\d{9}$";
		if (!phone.trim().matches(phonePattern)) {
			logger.warn("注册失败: 电话号格式不正确, phone={}", phone);
			return resultMap.fail().message("电话号格式不正确，请输入11位手机号码");
		}
		
		// 检查用户名是否已存在
		User existingUserByUsername = userService.getUserByUsername(username.trim());
		if (existingUserByUsername != null) {
			logger.warn("注册失败: 用户名已存在, username={}", username);
			return resultMap.fail().message("用户名已被注册");
		}
		
		// 检查邮箱是否已存在
		User existingUserByEmail = userService.getUserByEmail(email.trim());
		if (existingUserByEmail != null) {
			logger.warn("注册失败: 邮箱已存在, email={}", email);
			return resultMap.fail().message("该邮箱已被注册请直接登录");
		}
		
		try {
			// 创建用户对象
			User user = new User();
			user.setUsername(username.trim());
			user.setName(name.trim());
			user.setPhone(phone.trim());
			user.setEmail(email.trim());
			user.setAddress(""); // 注册时不需要地址，设置为空字符串
			user.setPassword(MD5.md5(password));
			user.setCreateTime(new Date());
			user.setRole(3); // 注册时默认设置为普通用户角色（3=用户）
			
			// 保存用户
			userService.save(user);
			
			// 注册成功后清除验证码
			emailCodeService.clearCode(email.trim());
			
			logger.info("用户注册成功: username={}, name={}, phone={}, email={}, id={}, role={}", 
					user.getUsername(), user.getName(), user.getPhone(), user.getEmail(), user.getId(), user.getRole());
			return resultMap.success().message("注册成功");
		} catch (Exception e) {
			logger.error("注册失败: username={}, name={}, phone={}, email={}", username, name, phone, email, e);
			e.printStackTrace();
			return resultMap.fail().message("注册失败: " + (e.getMessage() != null ? e.getMessage() : "系统异常"));
		}
	}

	/**
	 * Method name: index <BR>
	 * Description: 登录页面 <BR>
	 * 注意：已移除HTML前端，Vue前端使用自己的路由系统，此方法已注释
	 * 
	 * @return String login.html<BR>
	 */
	// @RequestMapping(value = "/")
	// public String index(Model model) {
	// 	Subject subject = SecurityUtils.getSubject();
	// 	User user = (User) subject.getPrincipal();
	//
	// 	if (null != user) {
	// 		model.addAttribute("user", user);
	// 		return "index";
	// 	} else {
	// 		return "login";
	// 	}
	// }

	/**
	 * Method name: main <BR>
	 * Description: 进入主页面 <BR>
	 * 注意：已移除HTML前端，Vue前端使用自己的路由系统，此方法已注释
	 * index.html
	 */
	// @RequestMapping(value = "/main")
	// public String main(Model model) {
	// 	Subject subject = SecurityUtils.getSubject();
	// 	User user = (User) subject.getPrincipal();
	// 	if (null != user) {
	// 		model.addAttribute("user", user);
	// 	} else {
	// 		return "login";
	// 	}
	// 	return "index";
	// }

	/**
	 * Method name: checkUserPassword <BR>
	 * Description: 检测旧密码是否正确 <BR>
	 * 
	 * @param password 旧密码
	 * @return boolean 是否正确<BR>
	 */
	@RequestMapping(value = "/user/checkUserPassword")
	@ResponseBody
	public boolean checkUserPassword(String password) {
		return userService.checkUserPassword(password);
	}

	/**
	 * Method name: updatePassword <BR>
	 * Description: 更新密码 <BR>
	 * 
	 * @param password 旧密码
	 * @return String 是否成功<BR>
	 */
	@RequestMapping(value = "/user/updatePassword")
	@ResponseBody
	public String updatePassword(String password) {
		return userService.updatePassword(password);
	}

	/**
	 * 找回密码页面
	 * 注意：已移除HTML前端，Vue前端使用自己的路由系统，此方法已注释
	 */
	// @RequestMapping(value = "/forgetPassword")
	// public String forgetPassword() {
	// 	return "forgetPassword";
	// }

	/**
	 * 发送找回密码验证码
	 */
	@RequestMapping(value = "/sendResetPasswordCode", method = RequestMethod.POST)
	@ResponseBody
	public ResultMap sendResetPasswordCode(String email) {
		logger.info("发送找回密码验证码请求: email={}", email);
		
		// 验证邮箱格式
		if (email == null || email.trim().isEmpty()) {
			logger.warn("发送找回密码验证码失败: 邮箱为空");
			return resultMap.fail().message("邮箱不能为空");
		}
		
		String emailPattern = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
		if (!email.trim().matches(emailPattern)) {
			logger.warn("发送找回密码验证码失败: 邮箱格式不正确, email={}", email);
			return resultMap.fail().message("邮箱格式不正确，请检查后重新输入");
		}
		
		// 检查邮箱是否已注册
		User existingUserByEmail = userService.getUserByEmail(email.trim());
		if (existingUserByEmail == null) {
			logger.warn("发送找回密码验证码失败: 邮箱未注册, email={}", email);
			return resultMap.fail().message("该邮箱尚未注册");
		}
		
		// 发送验证码
		boolean sendSuccess = emailCodeService.sendCode(email.trim());
		
		if (sendSuccess) {
			logger.info("找回密码验证码发送成功: email={}", email);
			return resultMap.success().message("验证码已发送");
		} else {
			logger.error("找回密码验证码发送失败: email={}", email);
			return resultMap.fail().message("验证码发送失败，请稍后重试");
		}
	}

	/**
	 * 重置密码
	 */
	@RequestMapping(value = "/resetPassword", method = RequestMethod.POST)
	@ResponseBody
	public ResultMap resetPassword(String email, String emailCode, String newPassword, String newPassword2) {
		logger.info("重置密码请求: email={}", email);
		
		// 参数验证和清理（去除前后空白字符，包括换行符）
		if (email == null || email.trim().isEmpty()) {
			logger.warn("重置密码失败: 邮箱为空");
			return resultMap.fail().message("邮箱不能为空");
		}
		email = email.trim();
		
		if (emailCode == null || emailCode.trim().isEmpty()) {
			logger.warn("重置密码失败: 验证码为空");
			return resultMap.fail().message("请输入验证码");
		}
		emailCode = emailCode.trim();
		
		if (newPassword == null || newPassword.trim().isEmpty()) {
			logger.warn("重置密码失败: 新密码为空");
			return resultMap.fail().message("新密码不能为空");
		}
		newPassword = newPassword.trim();
		
		if (newPassword2 == null || newPassword2.trim().isEmpty()) {
			logger.warn("重置密码失败: 确认密码为空");
			return resultMap.fail().message("确认密码不能为空");
		}
		newPassword2 = newPassword2.trim();
		
		// 验证密码长度
		if (newPassword.length() < 6) {
			logger.warn("重置密码失败: 密码长度不足，当前长度={}", newPassword.length());
			return resultMap.fail().message("密码长度不能小于6位");
		}
		
		// 验证两次密码是否一致
		if (!newPassword.equals(newPassword2)) {
			logger.warn("重置密码失败: 两次密码不一致");
			return resultMap.fail().message("两次输入的密码不一致，请重新输入");
		}
		
		// 验证邮箱格式
		String emailPattern = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
		if (!email.matches(emailPattern)) {
			logger.warn("重置密码失败: 邮箱格式不正确, email={}", email);
			return resultMap.fail().message("邮箱格式不正确，请检查后重新输入");
		}
		
		// 验证邮箱验证码
		String verifyResult = emailCodeService.verifyCode(email, emailCode);
		if (verifyResult != null) {
			logger.warn("重置密码失败: 验证码验证失败, email={}, error={}", email, verifyResult);
			return resultMap.fail().message(verifyResult);
		}
		
		// 检查邮箱是否已注册
		User user = userService.getUserByEmail(email);
		if (user == null) {
			logger.warn("重置密码失败: 邮箱未注册, email={}", email);
			return resultMap.fail().message("该邮箱尚未注册");
		}
		
		try {
			// 更新密码（传入明文密码，由Service层统一进行MD5加密）
			user.setPassword(newPassword);
			String updateResult = userService.updateUser(user);
			
			if ("SUCCESS".equalsIgnoreCase(updateResult)) {
				// 清除验证码
				emailCodeService.clearCode(email);
				logger.info("密码重置成功: email={}, userId={}", email, user.getId());
				return resultMap.success().message("密码重置成功，请使用新密码登录");
			} else {
				logger.error("重置密码失败: email={}, error={}", email, updateResult);
				return resultMap.fail().message("密码重置失败: " + updateResult);
			}
		} catch (Exception e) {
			logger.error("重置密码异常: email={}", email, e);
			e.printStackTrace();
			return resultMap.fail().message("密码重置失败: " + (e.getMessage() != null ? e.getMessage() : "系统异常"));
		}
	}
}
