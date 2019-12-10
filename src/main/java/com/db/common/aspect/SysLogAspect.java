package com.db.common.aspect;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Date;

import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.crypto.hash.format.Shiro1CryptFormat;
import org.aspectj.internal.lang.annotation.ajcDeclareAnnotation;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.w3c.dom.ls.LSProgressEvent;

import com.db.common.annotation.RequiredLog;
import com.db.common.util.IPUtils;
import com.db.common.util.ShiroUtil;
import com.db.sys.dao.SysLogDao;
import com.db.sys.entity.SysLog;

/**
 * 通过此类为系统中的某些业务添加日志扩展功能
 * 
 * @Aspect 描述的类为一个切面对象.这样的类通常由量大部分构成 pointcut 切入点 advice 增强
 * @author 000
 *
 */
//@Order(1)
@Aspect
@Service
public class SysLogAspect {
	@Autowired
	private SysLogDao sysLogDao;
	
	/**
	 * 定义切入点,借助@Pointcut bean(bean名字) 如bean(sysUserServiceImpl) bean(表达式)
	 * 如bean(*ServiceImpl)
	 */
//	@Pointcut("bean(sysUserServiceImpl)") // 下边引用注入该方法就会注入切点
	@Pointcut("@annotation(com.db.common.annotation.RequiredLog)")
	public void doLog() {
	}

	/**
	 * 环绕通知
	 * 
	 * @param point
	 * @return
	 * @throws Throwable
	 */
	@Around("doLog()")
	public Object around(ProceedingJoinPoint point) throws Throwable {
		long start = System.currentTimeMillis();
		Object result = point.proceed();
		long end = System.currentTimeMillis();
		Method targetMethod = getTargetMethod(point);
		String methodName = getTargetMethodName(targetMethod);
		System.err.println(methodName + " 执行了" + (end - start) + "毫秒");
		saveObject(point, end - start);
		return result;
	}

	/**
	 * 获取目标方法
	 * 
	 * @return
	 * @throws SecurityException
	 * @throws NoSuchMethodException
	 */
	private Method getTargetMethod(ProceedingJoinPoint point) throws NoSuchMethodException, SecurityException {
		// 获取目标类的字节码对象
		Class<?> class1 = point.getTarget().getClass();
		// 获取方法签名信息(方法名,参数列表)
		MethodSignature signature = (MethodSignature) point.getSignature();
		Method method = class1.getDeclaredMethod(signature.getName(), signature.getParameterTypes());
		// System.out.println(signature.getName());//返回findPageObjects
		return method;
	}

	/**
	 * 获取目标方法名(类全名+方法名)
	 * 
	 * @return
	 */
	private String getTargetMethodName(Method method) {
		return new StringBuilder(method.getDeclaringClass().getName()).append(".").append(method.getName()).toString();
	}

	private void saveObject(ProceedingJoinPoint point, long time) throws NoSuchMethodException, SecurityException {
		// 获取用户行为日志信息
		String username = ShiroUtil.getUser().getUsername();
		Method targetMethod = getTargetMethod(point);
		String method = getTargetMethodName(targetMethod);
		String params = Arrays.toString(point.getArgs());
		String operation = "operation";
		RequiredLog requiredLog = targetMethod.getDeclaredAnnotation(RequiredLog.class);
		if (requiredLog != null && !StringUtils.isEmpty(requiredLog.value())) {// 不为空时使用注解上的操作名称
			operation = requiredLog.value();
		}
		String ip = IPUtils.getIpAddr();
		System.out.println(ip);
		// 封装用户行为日志信息
		SysLog log = new SysLog();
		log.setUsername(username);
		log.setOperation(operation);
		log.setMethod(method);
		log.setParams(params);
		log.setIp(ip);
		log.setTime(time);
		log.setCreatedTime(new Date());
		// 将日志信息信息持久化
		sysLogDao.insertObject(log);
	}
}
