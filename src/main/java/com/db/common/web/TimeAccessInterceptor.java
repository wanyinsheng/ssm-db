package com.db.common.web;

import java.util.Calendar;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.expression.AccessException;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import com.db.common.exception.ServiceException;

/**
 * 时间访问拦截器
 * 
 * @author 000
 *
 */
@Component
public class TimeAccessInterceptor extends HandlerInterceptorAdapter {
	/**
	 * 在进入后端控制器之前拦截
	 */
	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
			throws Exception {
		// 获取日历对象(通过此对象设置时间)
		System.out.println("请求被拦截");
		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.HOUR_OF_DAY, 7);
		//设置开始允许访问时间
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		long start = calendar.getTimeInMillis();
		calendar.set(Calendar.HOUR_OF_DAY, 23);
		//设置允许访问时间
		long end = calendar.getTimeInMillis();
		long time = System.currentTimeMillis();
		//基于时间拦截和放行
		if (time < start || time > end) {
			throw new ServiceException("此时间点不可访问");
		}
		return true;
	}
	@Override
	public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
			ModelAndView modelAndView) throws Exception {
		super.postHandle(request, response, handler, modelAndView);
	}
	@Override
	public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex)
			throws Exception {
		super.afterCompletion(request, response, handler, ex);
	}
	
	
}
