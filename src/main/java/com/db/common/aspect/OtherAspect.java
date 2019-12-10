package com.db.common.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;
@Order
@Service
@Aspect
public class OtherAspect {
	@Pointcut("bean(sysUserServiceImpl)")
	public void doAspect() {
	}

	@Before("doAspect()")
	public void before() {
		System.out.println("这里是前置通知");
	}

	@After("doAspect()")
	public void after() {
		System.out.println("这里是后置通知");
	}

	@AfterReturning("doAspect()")
	public void afterReturning() {
		System.out.println("这里是返回通知");
	}

	@AfterThrowing("doAspect()")
	public void afterThrowing() {
		System.out.println("这里是异常通知");
	}

	@Around("doAspect()")
	public Object around(ProceedingJoinPoint point) throws Throwable {
		System.out.println("这里是前环绕通知");
		Object proceed = point.proceed();
		System.err.println("这里是后环绕通知");
		return proceed;
	}
}
