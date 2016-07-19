package com.yxlisv.autoset;

import java.lang.reflect.Field;
import java.util.Date;
import java.util.Properties;

import javax.servlet.http.HttpSession;

import org.aspectj.lang.ProceedingJoinPoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.yxlisv.control.AbstractBaseControl;
import com.yxlisv.util.reflect.ReflectionUtils;
import com.yxlisv.util.resource.PropertiesUtil;

/**
 * <p>自动设置值Aspect</p>
 * <p>自动设置值文件位于classpath下的validation目录，可以任意命名，可以多个</p>
 * @author 杨雪令
 * @time 2016年3月23日上午9:12:01
 * @version 1.0
 */
public class ControlAutosetAspect {

	/** 定义一个全局的记录器，通过LoggerFactory获取  */
	protected Logger logger = LoggerFactory.getLogger(getClass());

	/** 校验配置文件 */
	private static Properties config = null;

	// 添加方法要自动注入的参数
	private String[] addArgs;
	// 修改方法要自动注入的参数
	private String[] updateArgs;

	/**
	 * <p>初始化</p>
	 * <p>加载配置文件</p>
	 * @author 杨雪令
	 * @time 2016年3月23日下午4:52:42
	 * @version 1.0
	 */
	public synchronized void init() {
		if (config != null) return;
		config = PropertiesUtil.readProperties("autoset/control.config", ControlAutosetAspect.class);
		addArgs = config.get("add").toString().split(",");
		updateArgs = config.get("update").toString().split(",");
	}

	/**
	 * <p>添加数据拦截</p>
	 * @param jp spring aop 切入点
	 * @author 杨雪令
	 * @time 2016年3月23日上午9:12:21
	 * @version 1.0
	 */
	public Object add(ProceedingJoinPoint jp) throws Throwable {
		String methodName = jp.getSignature().getName();
		logger.debug("ControlAutosetAspect add [method=" + methodName + "]");
		if (config == null) init();
		Object methodArgsObj[] = jp.getArgs();
		if (methodArgsObj != null && methodArgsObj.length > 0) {
			HttpSession httpSession = AbstractBaseControl.getRequest().getSession();
			for (Object methodArgObj : methodArgsObj) {
				if (methodArgObj == null) continue;
				for (String autosetArg : addArgs) {
					if (httpSession.getAttribute(autosetArg) != null) {
						ReflectionUtils.setFieldValue(methodArgObj, autosetArg, httpSession.getAttribute(autosetArg));
					} else if (autosetArg.endsWith("Time")) {
						Field timeField = ReflectionUtils.getField(methodArgObj, autosetArg);
						if (timeField != null && timeField.getType().toString().contains("java.util.Date")) ReflectionUtils.setFieldValue(methodArgObj, autosetArg, new Date());
					}
				}
			}
		}

		return jp.proceed();
	}

	/**
	 * <p>修改方法拦截</p>
	 * @param jp spring aop 切入点
	 * @author 杨雪令
	 * @time 2016年3月23日上午9:12:21
	 * @version 1.0
	 */
	public Object update(ProceedingJoinPoint jp) throws Throwable {
		String methodName = jp.getSignature().getName();
		logger.debug("ControlAutosetAspect update [method=" + methodName + "]");
		if (config == null) init();
		Object methodArgsObj[] = jp.getArgs();
		if (methodArgsObj != null && methodArgsObj.length > 0) {
			HttpSession httpSession = AbstractBaseControl.getRequest().getSession();
			for (Object methodArgObj : methodArgsObj) {
				if (methodArgObj == null) continue;
				for (String autosetArg : updateArgs) {
					if (httpSession.getAttribute(autosetArg) != null) {
						ReflectionUtils.setFieldValue(methodArgObj, autosetArg, httpSession.getAttribute(autosetArg));
					} else if (autosetArg.endsWith("Time")) {
						Field timeField = ReflectionUtils.getField(methodArgObj, autosetArg);
						if (timeField != null && timeField.getType().toString().contains("java.util.Date")) ReflectionUtils.setFieldValue(methodArgObj, autosetArg, new Date());
					}
				}
			}
		}

		return jp.proceed();
	}
}