package com.yxlisv.control;

import java.util.Date;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.yxlisv.util.data.Page;
import com.yxlisv.util.map.MapUtil;
import com.yxlisv.util.security.SecurityHttpServletRequestWrapper;
import com.yxlisv.util.spring.propertyeditors.CustomDateEditor;
import com.yxlisv.util.spring.propertyeditors.CustomNumberEditor;

/**
 * <p>最基础的控制器</p>
 * @author 杨雪令
 * @time 2016年3月9日下午12:43:27
 * @version 1.0
 */
public abstract class AbstractBaseControl {

	/** 定义一个全局的记录器，通过LoggerFactory获取  */
	protected Logger logger = LoggerFactory.getLogger(getClass());

	@InitBinder
	protected void initBinder(WebDataBinder binder) {
		binder.registerCustomEditor(Date.class, new CustomDateEditor());
		binder.registerCustomEditor(Integer.class, new CustomNumberEditor("int"));
		binder.registerCustomEditor(Long.class, new CustomNumberEditor("long"));
		binder.registerCustomEditor(Float.class, new CustomNumberEditor("float"));
		binder.registerCustomEditor(Double.class, new CustomNumberEditor("double"));
	}

	/**
	 * <p>获取查询条件Map，不是原生的 ParameterMap，此Map没有被锁定</p>
	 * @return Map 
	 * @author 杨雪令
	 * @time 2016年3月9日下午12:44:28
	 * @version 1.0
	 */
	@SuppressWarnings("rawtypes")
	protected Map getParameterMap() {
		return MapUtil.parse(getRequest().getParameterMap());
	}

	/**
	 * <p>重定向</p>
	 * @param url 重定向地址
	 * @return String 拼接后的重定向字符串
	 * @author 杨雪令
	 * @time 2016年3月9日下午12:46:09
	 * @version 1.0
	 */
	protected String redirect(String url) {
		return "redirect:" + url;
	}

	/**
	 * <p>获取 HttpServletRequest 对象</p>
	 * @return HttpServletRequest 对象
	 * @author 杨雪令
	 * @time 2016年3月9日下午12:46:53
	 * @version 1.0
	 */
	public static HttpServletRequest getRequest() {
		return new SecurityHttpServletRequestWrapper(((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest());
	}

	/**
	 * <p>获取 HttpServletResponse 对象</p>
	 * @return HttpServletResponse 对象
	 * @author 杨雪令
	 * @time 2016年3月9日下午12:46:53
	 * @version 1.0
	 */
	public static HttpServletResponse getResponse() {
		return ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getResponse();
	}

	/**
	 * <p>获取 HttpSession 对象</p>
	 * @return HttpSession 对象
	 * @author 杨雪令
	 * @time 2016年3月9日下午12:47:07
	 * @version 1.0
	 */
	public static HttpSession getSession() {
		return getRequest().getSession();
	}

	/**
	 * <p>获取请求REFERER</p>
	 * @param request HttpServletRequest 对象
	 * @return String REFERER
	 * @author 杨雪令
	 * @time 2016年3月9日下午12:48:23
	 * @version 1.0
	 */
	public static String getReferer(HttpServletRequest request) {
		String referer = request.getHeader("REFERER");
		referer = referer.substring(referer.indexOf(request.getContextPath()) + request.getContextPath().length());

		return referer;
	}

	/**
	 * <p>绑定分页对象</p>
	 * <p>缓存最后一次使用的分页对象</p>
	 * <p>如果没有传递页码，使用上一次查询的页码</p>
	 * @param page 新的分页对象 
	 * @author 杨雪令
	 * @time 2016年3月22日下午2:24:31
	 * @version 1.0
	 */
	protected void bindPage(Page page) {
		HttpSession session = getSession();
		String cacheKey = "cache.page." + this.getClass().getName();
		if (session.getAttribute(cacheKey) != null) {
			Page cachePage = (Page) session.getAttribute(cacheKey);
			if (page.getPn() == Page.default_page_pn) page.setPn(cachePage.getPn());
			if (page.getFuzzy() == null && cachePage.getFuzzy() != null) page.setFuzzy(cachePage.getFuzzy());
			if (page.getOrderBy() == null) page.setOrderBy(cachePage.getOrderBy());
		}
		
		session.setAttribute(cacheKey, page);
	}

	/**
	 * <p>删除分页缓存</p>
	 * @author 杨雪令
	 * @time 2016年3月22日下午6:03:13
	 * @version 1.0
	 */
	protected void removePageCache() {
		HttpSession session = getSession();
		String cacheKey = "cache.page." + this.getClass().getName();
		if (session.getAttribute(cacheKey) != null) session.removeAttribute(cacheKey);
	}

	/**
	 * <p>加载查询条件</p>
	 * @return Map<String, String> 查询条件 
	 * @author 杨雪令
	 * @time 2016年3月22日下午2:54:17
	 * @version 1.0
	 */
	@SuppressWarnings({ "unchecked" })
	protected Map<String, String> loadQueryParam() {
		//HttpSession session = getSession();
		Map<String, String> paramMap = getParameterMap();
		/*String cacheKey = "cache.query.param." + this.getClass().getName();
		if (session.getAttribute(cacheKey) != null) {
			Map<String, String> cacheParam = (Map<String, String>) session.getAttribute(cacheKey);
			cacheParam.putAll(paramMap);
			paramMap = cacheParam;
		}*/

		//session.setAttribute(cacheKey, paramMap);
		return paramMap;
	}

	/**
	 * <p>删除查询参数缓存</p>
	 * @author 杨雪令
	 * @time 2016年3月22日下午6:03:13
	 * @version 1.0
	 */
	protected void removeQueryParamCache() {
		HttpSession session = getSession();
		String cacheKey = "cache.query.param." + this.getClass().getName();
		if (session.getAttribute(cacheKey) != null) session.removeAttribute(cacheKey);
	}
}