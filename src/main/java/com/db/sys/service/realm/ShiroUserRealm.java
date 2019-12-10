package com.db.sys.service.realm;

import java.util.HashSet;
import java.util.List;

import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.LockedAccountException;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authc.UnknownAccountException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.authc.credential.CredentialsMatcher;
import org.apache.shiro.authc.credential.HashedCredentialsMatcher;
import org.apache.shiro.authz.AuthorizationException;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.util.ByteSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.db.sys.dao.SysMenuDao;
import com.db.sys.dao.SysRoleMenuDao;
import com.db.sys.dao.SysUserDao;
import com.db.sys.dao.SysUserRoleDao;
import com.db.sys.entity.SysUser;

@Service
public class ShiroUserRealm extends AuthorizingRealm {
	@Autowired
	private SysUserDao sysUserDao;
	@Autowired
	private SysUserRoleDao sysUserRoleDao;
	@Autowired
	private SysRoleMenuDao sysRoleMenuDao;
	@Autowired
	private SysMenuDao sysMenuDao;

	@Override
	/**
	 * 设置凭证匹配器(与用户添加操作使用相同的加密算法)设置算法和加密次数
	 */
	public void setCredentialsMatcher(CredentialsMatcher credentialsMatcher) {
		// 构建凭证匹配对象
		HashedCredentialsMatcher hashedCredentialsMatcher = new HashedCredentialsMatcher();
		// 设置加密算法
		hashedCredentialsMatcher.setHashAlgorithmName("MD5");
		// 设置加密次数
		hashedCredentialsMatcher.setHashIterations(1);
		// 使用子类的凭证匹配器
		super.setCredentialsMatcher(hashedCredentialsMatcher);
	}

	/**
	 * 通过此方法完成数据的获取及封装,系统底层会将认证数据传递给 认证管理器,由认证管理器完成认证操作
	 */
	@Override
	protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) throws AuthenticationException {
		// 从token中获取用户信息,获取用户名(页面输入)
		UsernamePasswordToken uToken = (UsernamePasswordToken) token;
		String username = uToken.getUsername();
		// 基于用户名查询用户信息(从数据库查询)
		SysUser user = sysUserDao.findUserByUserName(username);
		// 判定用户是否存在
		if (user == null) {
			throw new UnknownAccountException();
		}
		// 判定用户是否禁用
		if (user.getValid() == 0) {
			throw new LockedAccountException();
		}
		// 封装用户信息
		ByteSource credentialsSalt = ByteSource.Util.bytes(user.getSalt());
		SimpleAuthenticationInfo info = new SimpleAuthenticationInfo(user, // principal(身份)
				user.getPassword(), // hashCredentials,已加密的密码
				credentialsSalt, // credentialSalt,
				getName());// realName("ShiroUserRealm")
		// 返回封装结果
		return info;// 返回值会传递给认证管理器,认证管理器通过此信息完成认证操作
	}

	@Override
	protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
		System.out.println("每次都会执行");
		// 获取登录用户的id
		SysUser user = (SysUser) principals.getPrimaryPrincipal();
		Integer id = user.getId();

		// 基于用户id获取对应角色id并判定
		List<Integer> roleIds = sysUserRoleDao.findRoleIdsByUserId(id);
		if (roleIds == null || roleIds.size() == 0) {
			throw new AuthorizationException();
		}
		// 基于角色id查找对应的菜单id
		Integer[] array = {};
		List<Integer> menuIds = sysRoleMenuDao.findMenuIdsByRoleIds(roleIds.toArray(array));// 将list转成array
		if (menuIds == null || menuIds.size() == 0) {
			throw new AuthorizationException();
		}
		// 基于菜单id查找对应权限
		List<String> permissions = sysMenuDao.findPermissions(menuIds.toArray(array));
		if (permissions == null || permissions.isEmpty()) {
			throw new AuthorizationException();
		}
		// 对权限标识信息进行封装并返回
		HashSet<String> set = new HashSet<>();
		for (String permisson : permissions) {
			if (!StringUtils.isEmpty(permisson)) {
				set.add(permisson);
			}
		}
		SimpleAuthorizationInfo info = new SimpleAuthorizationInfo();
		info.setStringPermissions(set);
		return info;// 返回给授权管理器
	}

}
