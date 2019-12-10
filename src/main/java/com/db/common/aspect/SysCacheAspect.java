package com.db.common.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitterReturnValueHandler;

/**
 * 注解版使用
 * 
 * @author 000
 *
 */
@Aspect
@Service
public class SysCacheAspect {
	@Pointcut("@annotation(com.db.common.annotation.RequiredCache)") // 为使用该注解的方法添加业务增强
	public void doCache() {
	}

	@Around("doCache()")
	public Object around(ProceedingJoinPoint point) throws Throwable {
		System.out.println("query  from  cache");
		System.out.println("execute query from database ");
		Object result = point.proceed();
		System.out.println("put data to  cache");
		return result;
	}

}
