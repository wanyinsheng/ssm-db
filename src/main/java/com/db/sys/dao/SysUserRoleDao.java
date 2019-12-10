package com.db.sys.dao;

import java.util.List;

import org.apache.ibatis.annotations.Param;

/**
 * 借助此dao操作用户和角色关系表
 */
public interface SysUserRoleDao {
	/**
	 * 将用户角色关系数据写入用户角色关系表
	 * 
	 * @param userId
	 * @param roleIds
	 * @return
	 */
	int insertObjects(@Param("userId") Integer userId, @Param("roleIds") Integer[] roleIds);

	/**
	 * 基于角色id删除角色和用户关系数据
	 * 
	 * @param roleId
	 * @return
	 */
	int deleteObjectsByRoleId(Integer roleId);

	/**
	 * 基于用户id查询角色id
	 * 
	 * @param userId
	 * @return
	 */
	List<Integer> findRoleIdsByUserId(@Param("userId") Integer userId);

	/**
	 * 基于用户id删除用户和角色关系数据
	 * 
	 * @param userId
	 * @return
	 */
	int deleteObjectsByUserId(@Param("userId") Integer userId);
}
