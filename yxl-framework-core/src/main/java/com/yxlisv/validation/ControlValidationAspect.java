package com.yxlisv.validation;

import java.io.File;
import java.lang.reflect.Field;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.servlet.http.HttpServletRequest;

import org.aspectj.lang.ProceedingJoinPoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.yxlisv.control.AbstractBaseControl;
import com.yxlisv.util.exception.SimpleMessageException;
import com.yxlisv.util.file.FilePathUtil;
import com.yxlisv.util.math.NumberUtil;
import com.yxlisv.util.resource.PropertiesUtil;

/**
 * <p>数据校验Aspect</p>
 * <p>数据校验文件位于classpath下的validation目录，可以任意命名，可以多个</p>
 * @author 杨雪令
 * @time 2016年3月23日上午9:12:01
 * @version 1.0
 */
public class ControlValidationAspect {

	/** 定义一个全局的记录器，通过LoggerFactory获取  */
	protected Logger logger = LoggerFactory.getLogger(getClass());

	/** 校验配置文件 */
	private static Properties config = null;
	
	/**
	 * <p>初始化</p>
	 * <p>加载配置文件</p>
	 * @author 杨雪令
	 * @time 2016年3月23日下午4:52:42
	 * @version 1.0
	 */
	public synchronized void init(){
		if(config != null) return;
		config = new Properties();
		String validationPath = FilePathUtil.getWebRoot() + "/WEB-INF/classes/validation";
		File validationDir = new File(validationPath);
		if (validationDir.exists() && validationDir.isDirectory()) {
			for(String configFile : validationDir.list()){
				Properties properties = PropertiesUtil.readProperties("validation/" + configFile, ControlValidationAspect.class);
				config.putAll(properties);
			}
		}
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
		logger.debug("ValidationAspect add [method=" + methodName + "]");
		Object args[] = jp.getArgs();
		if (args == null || args.length < 1) throw new SimpleMessageException("数据校验未通过：对象不能为空");
		Map<String, String> validationErrorMap = validation(args, true, true, true);

		HttpServletRequest request = AbstractBaseControl.getRequest();
		if (validationErrorMap != null && !validationErrorMap.isEmpty()) {
			logger.error("DataValidationError : " + args[0] + " - " + validationErrorMap.toString());
			request.getSession().setAttribute("validationErrorMap", validationErrorMap);
			return "redirect:" + AbstractBaseControl.getReferer(request);
		} else {
			if (request.getSession().getAttribute("validationErrorMap") != null) request.getSession().removeAttribute("validationErrorMap");
			return jp.proceed();
		}
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
		logger.debug("ValidationAspect update [method=" + methodName + "]");
		Object args[] = jp.getArgs();
		if (args == null || args.length < 1) throw new SimpleMessageException("数据校验未通过：对象不能为空");
		Map<String, String> validationErrorMap = validation(args, false, true, true);

		HttpServletRequest request = AbstractBaseControl.getRequest();
		if (validationErrorMap != null && !validationErrorMap.isEmpty()) {
			logger.error("DataValidationError : " + args[0] + " - " + validationErrorMap.toString());
			request.getSession().setAttribute("validationErrorMap", validationErrorMap);
			return "redirect:" + AbstractBaseControl.getReferer(request);
		} else {
			if (request.getSession().getAttribute("validationErrorMap") != null) request.getSession().removeAttribute("validationErrorMap");
			return jp.proceed();
		}
	}

	/**
	 * <p>校验数据</p>
	 * @param args 要校验的对象参数
	 * @param validNotNull	是否做非空校验
	 * @param validMinLength	是否做最小长度校验
	 * @param validMaxLength	是否做最大长度校验
	 * @return	Map<String, String> <key, message>
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException Map<String,String> 
	 * @author 杨雪令
	 * @time 2016年3月23日下午3:06:18
	 * @version 1.0
	 */
	public Map<String, String> validation(Object args[], boolean validNotNull, boolean validMinLength, boolean validMaxLength) throws IllegalArgumentException, IllegalAccessException {
		if(config == null) init();
		if(config.isEmpty()) return null;
		Map<String, String> validationErrorMap = new HashMap<String, String>();
		for (Object obj : args) {
			if(obj == null) continue;
			Field[] fields = obj.getClass().getDeclaredFields();
			if (fields.length <= 0) continue;
			for (Field field : fields) {
				String validKey = obj.getClass().getSimpleName() + "." + field.getName();
				if (!config.containsKey(validKey)) continue;

				// 解析校验配置
				String[] valids = config.getProperty(validKey).split(",");
				int minLength = NumberUtil.parseInt(valids[0]);
				int maxLength = NumberUtil.parseInt(valids[1]);
				boolean notNull = false;
				if (valids[2].equals("true")) notNull = true;

				// 获取属性值
				field.setAccessible(true);
				Object valueObj = field.get(obj);
				if(valueObj instanceof Date) continue;
				String value = null;
				if (valueObj != null) value = valueObj.toString();
				if (value != null && value.length() == 0) value = null;

				// 校验数据
				if (value == null && notNull) validationErrorMap.put(field.getName(), "不能为空");
				if (value == null) continue;
				if (value.length() < minLength) validationErrorMap.put(field.getName(), "长度不能小于" + minLength + "个字符");
				if (value.length() > maxLength) validationErrorMap.put(field.getName(), "长度不能超过" + maxLength + "个字符");
			}
		}
		return validationErrorMap;
	}
}