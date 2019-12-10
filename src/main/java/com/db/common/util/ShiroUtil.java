package com.db.common.util;

import org.apache.shiro.SecurityUtils;

import com.db.sys.entity.SysUser;

public abstract class ShiroUtil {
    
	public static SysUser getUser() {
		return (SysUser)
		SecurityUtils.getSubject().getPrincipal();
	}
	
}
