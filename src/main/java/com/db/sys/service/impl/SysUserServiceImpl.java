package com.db.sys.service.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.log4j.varia.FallbackErrorHandler;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.crypto.hash.SimpleHash;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.db.common.annotation.RequiredLog;
import com.db.common.exception.ServiceException;
import com.db.common.util.PageUtil;
import com.db.common.vo.PageObject;
import com.db.sys.dao.SysUserDao;
import com.db.sys.dao.SysUserRoleDao;
import com.db.sys.entity.SysUser;
import com.db.sys.service.SysUserService;
import com.db.sys.vo.SysUserDeptVo;

@Transactional(rollbackFor = Throwable.class, isolation = Isolation.READ_COMMITTED, timeout = 10) // 超时时间默认-1,不超时

@Service
public class SysUserServiceImpl implements SysUserService {
	@Autowired
	private SysUserDao sysUserDao;

	@Autowired
	private SysUserRoleDao sysUserRoleDao;

	@Override
	public int saveObject(SysUser entity, Integer[] roleIds) {
		// 1.参数校验
		if (entity == null)
			throw new IllegalArgumentException("保存对象不能为空");
		if (StringUtils.isEmpty(entity.getUsername()))
			throw new IllegalArgumentException("用户名不能为空");
		if (StringUtils.isEmpty(entity.getPassword()))
			throw new IllegalArgumentException("密码不能为空");
		// ....
		if (roleIds == null || roleIds.length == 0)
			throw new ServiceException("必须为用户分配角色");
		// 2.保存用户自身信息
		// 2.1构建一个盐值对象
		String salt = UUID.randomUUID().toString();
		// 2.2对密码进行加密
		// String password1=
		// DigestUtils.md5DigestAsHex((salt+entity.getPassword()).getBytes());
		// System.out.println("password1="+password1);
		SimpleHash sh = new SimpleHash(// Shiro框架提供
				"MD5", // algorithmName 算法名称
				entity.getPassword(), // source要加密的对象
				salt, // 盐值
				1);// hashIterations 加密次数
		String password2 = sh.toHex();
		System.out.println("password2=" + password2);
		entity.setPassword(password2);
		entity.setSalt(salt);
		int rows = sysUserDao.insertObject(entity);
		// 3.保存用户和角色关系数据
		sysUserRoleDao.insertObjects(entity.getId(), roleIds);
		// 4.返回结果
		return rows;
	}

	@RequiredLog("禁用启用")
	@Override
	public int validById(Integer id, Integer valid, String modifiedUser) {
		// 1.参数校验
		if (id == null || id < 1)
			throw new IllegalArgumentException("id值无效");
		if (valid == null || (valid != 0 && valid != 1))
			throw new IllegalArgumentException("valid值无效");
		// 2.修改状态
		int rows = sysUserDao.validById(id, valid, modifiedUser);
		if (rows == 0)
			throw new ServiceException("记录可能已经不存在");
		// 3.返回结果
		return rows;
	}

	@Override
	public PageObject<SysUserDeptVo> findPageObjects(Integer pageCurrent, String username) {
		// 1.参数校验
		if (pageCurrent == null || pageCurrent < 1)
			throw new IllegalArgumentException("页码值无效");
		// 2.查询总记录数，并进行校验
		int rowCount = sysUserDao.getRowCount(username);
		if (rowCount == 0)
			throw new ServiceException("没有对应记录");
		// 3.查询当前页要呈现的记录
		int pageSize = 3;
		int startIndex = (pageCurrent - 1) * pageSize;
		List<SysUserDeptVo> records = sysUserDao.findPageObjects(username, startIndex, pageSize);
		// 4.封装数据并返回
		return PageUtil.newInstance(pageCurrent, rowCount, pageSize, records);
	}

	@Transactional(readOnly = false, propagation = Propagation.REQUIRED)
	@Override
	public Map<String, Object> findObjectById(Integer id) {
		if (id == null || id < 0) {
			throw new IllegalArgumentException("id不正确");
		}
		// 2.业务查询
		SysUserDeptVo user = sysUserDao.findObjectById(id);
		if (user == null)
			throw new ServiceException("此用户已经不存在");
		List<Integer> roleIds = sysUserRoleDao.findRoleIdsByUserId(id);
		// 3.数据封装
		Map<String, Object> map = new HashMap<>();
		map.put("user", user);
		map.put("roleIds", roleIds);
		return map;
	}

	@Override
	public int updateObject(SysUser entity, Integer[] roleIds) {
		// 1.参数有效性验证
		if (entity == null)
			throw new IllegalArgumentException("保存对象不能为空");
		if (StringUtils.isEmpty(entity.getUsername()))
			throw new IllegalArgumentException("用户名不能为空");
		if (roleIds == null || roleIds.length == 0)
			throw new IllegalArgumentException("必须为其指定角色");
		// 其它验证自己实现，例如用户名已经存在，密码长度，...
		// 2.更新用户自身信息
		int rows = sysUserDao.updateObject(entity);
		// 3.保存用户与角色关系数据(先删除,后添加)
		sysUserRoleDao.deleteObjectsByUserId(entity.getId());
		int row = sysUserRoleDao.insertObjects(entity.getId(), roleIds);
		if (row > 0) {
			// 出异常回滚
			throw new ServiceException("insert error");
		}
		// 4.返回结果
		return rows;
	}

	@Override
	public int updatePassword(String password, String newPassword, String cfgPassword) {
		// 1.判定新密码与密码确认是否相同
		if (StringUtils.isEmpty(newPassword))
			throw new IllegalArgumentException("新密码不能为空");
		if (StringUtils.isEmpty(cfgPassword))
			throw new IllegalArgumentException("确认密码不能为空");
		if (!newPassword.equals(cfgPassword))
			throw new IllegalArgumentException("两次输入的密码不相等");
		// 2.判定原密码是否正确
		if (StringUtils.isEmpty(password))
			throw new IllegalArgumentException("原密码不能为空");
		// 获取登陆用户
		SysUser user = (SysUser) SecurityUtils.getSubject().getPrincipal();
		SimpleHash sh = new SimpleHash("MD5", password, user.getSalt(), 1);
		if (!user.getPassword().equals(sh.toHex()))
			throw new IllegalArgumentException("原密码不正确");
		// 3.对新密码进行加密
		String salt = UUID.randomUUID().toString();
		sh = new SimpleHash("MD5", newPassword, salt, 1);
		// 4.将新密码加密以后的结果更新到数据库
		int rows = sysUserDao.updatePassword(user.getId(), sh.toHex(), salt);
		if (rows == 0)
			throw new ServiceException("修改失败");
		return rows;
	}

}
