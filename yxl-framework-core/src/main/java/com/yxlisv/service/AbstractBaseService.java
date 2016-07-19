package com.yxlisv.service;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.yxlisv.dao.BaseHibernateEntryDao;
import com.yxlisv.util.data.Page;
import com.yxlisv.util.reflect.BeansUtils;
import com.yxlisv.util.reflect.GenericsUtil;
import com.yxlisv.util.reflect.ReflectionUtils;

/**
 * <p>Service层的基础类</p>
 * @author 杨雪令
 * @time 2016年3月8日下午5:45:21
 * @version 1.0
 */
public abstract class AbstractBaseService<T> {
	// 定义一个全局的记录器，通过LoggerFactory获取
	protected Logger logger = LoggerFactory.getLogger(this.getClass());

	/** 注入BaseHibernateEntryDao */
	@Autowired
	protected BaseHibernateEntryDao<T> baseHibernateEntryDao;

	/** 通过反射机制读取泛型中的实体类 */
	@SuppressWarnings("unchecked")
	protected Class<T> entityClass = GenericsUtil.getClass(getClass());

	/** 通过反射机制读取实体类的字段名 */
	protected List<String> propNames = BeansUtils.getFieldsAndRefer(entityClass);

	/**
	 * <p>绑定数据到targetObj</p>
	 * @param tempObj 被绑定对象
	 * @param targetObj 被绑定对象
	 * @author 杨雪令
	 * @time 2016年3月17日下午2:40:40
	 * @version 1.0
	 */
	protected void bindValue(Object tempObj, Object targetObj) {
		bindValueIgnore(tempObj, targetObj);
	}

	/**
	 * <p>绑定数据到targetObj，忽略一些属性</p>
	 * @param tempObj 被绑定对象
	 * @param targetObj 被绑定对象
	 * @param ignoreArg 要忽略的属性
	 * @author 杨雪令
	 * @time 2016年3月17日下午2:40:40
	 * @version 1.0
	 */
	protected void bindValueIgnore(Object tempObj, Object targetObj, String... ignoreArgs) {

		// 要忽略的属性集合
		List<String> ignoreList = null;
		if (ignoreArgs != null) ignoreList = Arrays.asList(ignoreArgs);

		//循环设置属性值
		for (String propName : propNames) {
			if (ignoreList != null && ignoreList.contains(propName)) continue;
			Object value = ReflectionUtils.getFieldValue(tempObj, propName);
			if (value == null) continue;
			ReflectionUtils.setFieldValue(targetObj, propName, value);
		}
	}

	/**
	 * <p>从对象中获取主键的值</p>
	 * @param object
	 * @return Serializable 
	 * @author 杨雪令
	 * @time 2016年3月17日下午2:56:11
	 * @version 1.0
	 */
	private Serializable getPrimaryValue(Object object) {
		String primaryKey = propNames.get(0);
		return (Serializable) ReflectionUtils.getFieldValue(object, primaryKey);
	}

	/**
	 * <p>分页查询</p>
	 * <p>默认为精确查询，如果需要模糊查询，传递参数：fuzzy=true/on</p>
	 * <p>如果需要排序，传递参数：orderBy=字段名 asc/desc，如：orderBy=id desc</p>
	 * @param page 分页page对象
	 * @param paramMap 查询条件 
	 * @author 杨雪令
	 * @time 2016年3月9日下午1:14:39
	 * @version 1.0
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Page page(Page page, Map paramMap) {
		return baseHibernateEntryDao.page(page, paramMap);
	}

	/**
	 * <p>保存一个对象</p>
	 * @param obj 要保存的对象
	 * @return T 保存后的对象
	 * @author 杨雪令
	 * @time 2016年3月8日下午3:32:11
	 * @version 1.0
	 */
	public T save(T obj) {
		return baseHibernateEntryDao.save(obj);
	}

	/**
	 * <p>修改一个对象</p>
	 * <p>增量修改</p>
	 * @param tempObj 要修改的对象 
	 * @return T 修改后的对象
	 * @author 杨雪令
	 * @time 2016年3月8日下午4:24:50
	 * @version 1.0
	 */
	public T update(T tempObj) {

		// 如果没有主键，不能执行修改操作
		Serializable primaryValue = getPrimaryValue(tempObj);
		if (primaryValue == null) return null;

		// 从数据库中读取要修改的对象
		T persistentObj = baseHibernateEntryDao.get(primaryValue);
		if (persistentObj == null) return null;

		// 绑定修改后的值
		bindValue(tempObj, persistentObj);

		return baseHibernateEntryDao.update(persistentObj);
	}

	/**
	 * <p>保存或者修改一个对象</p>
	 * @param obj 要保存的对象
	 * @return T 修改后的对象
	 * @author 杨雪令
	 * @time 2016年3月8日下午3:32:11
	 * @version 1.0
	 */
	public T saveOrUpdate(T obj) {
		Serializable primaryValue = getPrimaryValue(obj);
		if (primaryValue == null) return save(obj);
		else return update(obj);
	}

	/**
	 * <p>获取一个对象</p>
	 * @param entityType 实体类别
	 * @param id 实体ID
	 * @return 实体类
	 * @author 杨雪令
	 * @time 2016年3月8日下午4:22:15
	 * @version 1.0
	 */
	public T get(Class<T> entityType, Serializable id) {
		return baseHibernateEntryDao.get(entityType, id);
	}

	/**
	 * <p>根据ID查询对象</p>
	 * @param id 对象ID
	 * @return T 查询的对象
	 * @author 杨雪令
	 * @time 2016年3月8日下午4:28:32
	 * @version 1.0
	 */
	public T get(Serializable id) {
		return baseHibernateEntryDao.get(entityClass, id);
	}

	/**
	 * <p>根据某个属性获取一个对象</p>
	 * @param propName 属性名称
	 * @param value	属性值
	 * @return T 查询到的对象
	 * @author 杨雪令
	 * @time 2016年3月8日下午5:06:31
	 * @version 1.0
	 */
	public T get(String propName, Object value) {
		return baseHibernateEntryDao.get(propName, value);
	}

	/**
	 * <p>删除一个对象</p>
	 * @param obj 要删除的对象 
	 * @return boolean 成功或失败
	 * @author 杨雪令
	 * @time 2016年3月8日下午4:23:02
	 * @version 1.0
	 */
	public boolean delete(T obj) {
		baseHibernateEntryDao.delete(obj);
		return true;
	}

	/**
	 * <p>根据ID删除数据</p>
	 * @param id 对象ID
	 * @return boolean 成功/失败
	 * @author 杨雪令
	 * @time 2016年3月8日下午4:37:40
	 * @version 1.0
	 */
	public boolean delete(String id) {
		T obj = get(id);
		if (obj == null) return false;
		baseHibernateEntryDao.delete(obj);
		return true;
	}

	/**
	 * <p>根据某个属性删除数据</p>
	 * @param propName 属性名称
	 * @param value	属性值
	 * @return int 删除记录数量
	 * @author 杨雪令
	 * @time 2016年3月8日下午4:37:40
	 * @version 1.0
	 */
	public int delete(String propName, Serializable value) {
		return baseHibernateEntryDao.delete(propName, value);
	}

	/**
	 * <p>根据某个属性删除数据</p>
	 * @param propName 属性名称
	 * @param value	属性值
	 * @return int 删除记录数量
	 * @author 杨雪令
	 * @time 2016年3月8日下午4:37:40
	 * @version 1.0
	 */
	public int delete(String propName, Serializable[] value) {
		return baseHibernateEntryDao.delete(propName, value);
	}

	/**
	 * <p>根据某个属性查找数据</p>
	 * @param propName 属性名称
	 * @param value	属性值
	 * @return List<T> 查询到的数据集合
	 * @author 杨雪令
	 * @time 2016年3月8日下午4:40:31
	 * @version 1.0
	 */
	public List<T> find(String propName, Serializable value) {
		return baseHibernateEntryDao.find(propName, value);
	}

	/**
	 * <p>根据多个属性条件查找数据</p>
	 * @param paramMap<propName(属性名称), value(属性值)>
	 * @return List<T> 查询到的数据集合
	 * @author 杨雪令
	 * @time 2016年4月29日上午11:05:37
	 * @version 1.0
	 */
	public List<T> find(Map<String, String> paramMap) {
		return baseHibernateEntryDao.find(paramMap);
	}

	/**
	 * <p>查找一个实体类的所有数据</p>
	 * @return List<T> 查询到的数据集合
	 * @author 杨雪令
	 * @time 2016年3月8日下午5:20:35
	 * @version 1.0
	 */
	public List<T> findAll() {
		return baseHibernateEntryDao.findAll();
	}
}