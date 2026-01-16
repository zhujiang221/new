package com.phms.model;

import com.phms.pojo.User;
import org.springframework.stereotype.Component;

import java.util.HashMap;

/**
 * class name:ResultMap <BR>
 * class description: 返回前台结果 <BR>
 */
@Component
public class ResultMap extends HashMap<String, Object> {
	/**
	 * define a field serialVersionUID which type is long
	 */
	private static final long serialVersionUID = 1L;

	public ResultMap() {
	}

	/**
	 * Method name: success <BR>
	 * Description: 操作成功 <BR>
	 * Remark: <BR>
	 * 
	 * @return ResultMap<BR>
	 */
	public ResultMap success() {
		this.put("result", "success");
		return this;
	}

	/**
	 * Method name: fail <BR>
	 * Description: 操作失败 <BR>
	 * Remark: <BR>
	 * 
	 * @return ResultMap<BR>
	 */
	public ResultMap fail() {
		this.put("result", "fail");
		return this;
	}

	/**
	 * Method name: message <BR>
	 * Description: 返回前台信息 <BR>
	 * Remark: <BR>
	 * 
	 * @param message
	 * @return ResultMap<BR>
	 */
	public ResultMap message(Object message) {
		this.put("message", message);
		return this;
	}

	/**
	 * Method name: data <BR>
	 * Description: 返回数据 <BR>
	 * Remark: <BR>
	 * 
	 * @param user 用户对象
	 * @return ResultMap<BR>
	 */
	public ResultMap data(User user) {
		this.put("data", user);
		return this;
	}

	/**
	 * Method name: data <BR>
	 * Description: 返回数据（通用方法，支持任意类型） <BR>
	 * Remark: <BR>
	 * 
	 * @param data 数据对象（可以是Map、List、或其他任意类型）
	 * @return ResultMap<BR>
	 */
	public ResultMap data(Object data) {
		this.put("data", data);
		return this;
	}
}