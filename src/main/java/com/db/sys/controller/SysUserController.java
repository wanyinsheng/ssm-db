package com.db.sys.controller;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.apache.shiro.subject.Subject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.db.common.annotation.RequiredLog;
import com.db.common.vo.JsonResult;
import com.db.sys.entity.SysUser;
import com.db.sys.service.SysUserService;

@RequestMapping("/user/")
@Controller
public class SysUserController {
	@Autowired
	private SysUserService sysUserService;

	@RequestMapping("doUserListUI")
	public String doUserListUI() {
		return "sys/user_list";
	}

	@RequestMapping("doUserEditUI")
	public String doUserEditListUI() {
		return "sys/user_edit";
	}

	/**
	 * 登录
	 * 
	 * @param username
	 * @param password
	 * @return
	 */
	@RequestMapping("doLogin")
	@ResponseBody
	public JsonResult doLogin(boolean rememberMe, String username, String password) {// 用户名,密码,记住我

		// 2.通过Subject提交用户信息,交给shiro框架进行认证操作
		// 2.1对用户进行封装
		UsernamePasswordToken token = new UsernamePasswordToken(username, // 身份信息
				password);// 凭证信息
		// 是否记住我
		if (rememberMe) {// 记住我
			token.setRememberMe(true);
		}
		// 1.获取Subject对象
		Subject subject = SecurityUtils.getSubject();
		// 2.2对用户信息进行身份认证
		subject.login(token);
		// 分析:
		// 1)token会传给shiro的SecurityManager
		// 2)SecurityManager将token传递给认证管理器
		// 3)认证管理器会将token传递给realm
		return new JsonResult("login ok");
	}

	@RequestMapping("doUpdatePasswordUI")
	public String doUpdatePasswordUI() {
		return "sys/pwd_edit";
	}

	@RequestMapping("doUpdatePassword")
	@ResponseBody
	public JsonResult doUpdatePassword(String pwd, String newPwd, String cfgPwd) {
		sysUserService.updatePassword(pwd, newPwd, cfgPwd);
		return new JsonResult("update password ok");
	}

	@RequestMapping("doSaveObject")
	@ResponseBody
	public JsonResult doSaveObject(SysUser entity, Integer[] roleIds) {
		sysUserService.saveObject(entity, roleIds);
		return new JsonResult("save ok");
	}

	@RequiredLog("禁用启用")
//	@RequiresPermissions("user:sys:valid") // 权限标识
	@RequestMapping("doValidById")
	@ResponseBody
	public JsonResult doValidById(Integer id, Integer valid) {
		SysUser user = (SysUser) SecurityUtils.getSubject().getPrincipal();
		sysUserService.validById(id, valid, user.getUsername());
		return new JsonResult("update ok");
	}

	@RequestMapping("doFindPageObjects")
	@ResponseBody
	public JsonResult doFindPageObjects(Integer pageCurrent, String username) {
		return new JsonResult(sysUserService.findPageObjects(pageCurrent, username));
	}

	@RequestMapping("doFindObjectById")
	@ResponseBody
	public JsonResult doFindObjectById(Integer id) {
		return new JsonResult(sysUserService.findObjectById(id));
	}

	@RequestMapping("doUpdateObject")
	@ResponseBody
	public JsonResult doUpdateObject(SysUser entity, Integer[] roleIds) {
		sysUserService.updateObject(entity, roleIds);
		return new JsonResult();
	}
}
