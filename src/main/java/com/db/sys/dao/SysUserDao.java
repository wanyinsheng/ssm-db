package com.db.sys.dao;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Param;

import com.db.sys.entity.SysUser;
import com.db.sys.vo.SysUserDeptVo;
import com.sun.org.glassfish.gmbal.ParameterNames;

public interface SysUserDao {
	/**
	 * 修改用户密码
	 * 
	 * @param id
	 * @param password 新的已经加密的密码
	 * @param salt     对应的盐值
	 * @return
	 */
	int updatePassword(@Param("id") Integer id, @Param("password") String password, @Param("salt") String salt);

	/***
	 * 保存用户自身信息
	 * 
	 * @param entity
	 * @return
	 */
	int insertObject(SysUser entity);

	/**
	 * 禁用或启用用户信息
	 * 
	 * @param id           用户id
	 * @param valid        状态
	 * @param modifiedUser 修改用户
	 * @return 修改的行数
	 */
	int validById(@Param("id") Integer id, @Param("valid") Integer valid, @Param("modifiedUser") String modifiedUser);

	/**
	 * 按条件查询总记录数
	 * 
	 * @param username
	 * @return
	 */
	int getRowCount(@Param("username") String username);

	/**
	 * 按条件查询当前页记录
	 * 
	 * @param username
	 * @param startIndex
	 * @param pageSize
	 * @return
	 */
	List<SysUserDeptVo> findPageObjects(@Param("username") String username, @Param("startIndex") Integer startIndex,
			@Param("pageSize") Integer pageSize);

	/**
	 * 基于用户id查询用户部门信息
	 * 
	 * @param id
	 * @return
	 */
	SysUserDeptVo findObjectById(Integer id);

	/**
	 * 新增
	 * 
	 * @param entity
	 * @return
	 */
	int updateObject(SysUser entity);

	/**
	 * 根据用户名查找用户,用于登录授权
	 * 
	 * @param username
	 * @return
	 */
	SysUser findUserByUserName(String username);

}
