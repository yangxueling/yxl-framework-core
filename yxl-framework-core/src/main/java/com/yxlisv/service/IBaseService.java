package com.yxlisv.service;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import com.yxlisv.util.data.Page;

/**
 * <p>Service 层基础接口</p>
 * @author 杨雪令
 * @time 2016年3月15日下午4:38:44
 * @version 1.0
 */
public interface IBaseService<T> {

	/**
	 * <p>分页查询</p>
	 * <p>默认为精确查询，如果需要模糊查询，page.setFuzzy=(true/on)</p>
	 * <p>如果需要排序，page.addOrderByAsc/addOrderByDesc，或者传递参数：orderBy=字段名[-]，如：orderBy=id-，默认升序，降序在字段名称后面加：-</p>
	 * <p>如果需要比较大小，传递参数：字段名_S，字段名_E (S:start-开始，E：end-结束)，会生成：字段名>=${字段名_S} and 字段名<=${字段名_E}</p>
	 * @param page 分页page对象
	 * @param paramMap 查询条件 
	 * @author 杨雪令
	 * @time 2016年3月9日下午1:14:39
	 * @version 1.0
	 */
	@SuppressWarnings("rawtypes")
	public Page page(Page page, Map paramMap);

	/**
	 * <p>保存一个对象</p>
	 * @param obj 要保存的对象
	 * @return T 保存后的对象
	 * @author 杨雪令
	 * @time 2016年3月8日下午3:32:11
	 * @version 1.0
	 */
	public T save(T obj);
	
	/**
	 * <p>修改一个对象</p>
	 * @param obj 要修改的对象 
	 * @return T 保存后的对象
	 * @author 杨雪令
	 * @time 2016年3月8日下午4:24:50
	 * @version 1.0
	 */
	public T update(T obj);

	/**
	 * <p>获取一个对象</p>
	 * @param entityType 实体类别
	 * @param id 实体ID
	 * @return 实体类
	 * @author 杨雪令
	 * @time 2016年3月8日下午4:22:15
	 * @version 1.0
	 */
	public T get(Class<T> entityType, Serializable id);

	/**
     * <p>根据ID查询对象</p>
     * @param id 对象ID
     * @return T 查询的对象
     * @author 杨雪令
     * @time 2016年3月8日下午4:28:32
     * @version 1.0
     */
	public T get(Serializable id);

	/**
	 * <p>根据某个属性获取一个对象</p>
	 * @param propName 属性名称
	 * @param value	属性值
	 * @return T 查询到的对象
	 * @author 杨雪令
	 * @time 2016年3月8日下午5:06:31
	 * @version 1.0
	 */
	public T get(String propName, Object value);
	
	/**
	 * <p>删除一个对象</p>
	 * @param obj 要删除的对象 
	 * @return boolean 成功或失败
	 * @author 杨雪令
	 * @time 2016年3月8日下午4:23:02
	 * @version 1.0
	 */
	public boolean delete(T obj);

	/**
	 * <p>根据ID删除数据</p>
	 * @param id 对象ID
	 * @return boolean 成功/失败
	 * @author 杨雪令
	 * @time 2016年3月8日下午4:37:40
	 * @version 1.0
	 */
	public boolean delete(String id);

	/**
	 * <p>根据某个属性删除数据</p>
	 * @param propName 属性名称
	 * @param value	属性值
	 * @return int 删除记录数量
	 * @author 杨雪令
	 * @time 2016年3月8日下午4:37:40
	 * @version 1.0
	 */
	public int delete(String propName, Serializable value);

	/**
	 * <p>根据某个属性删除数据</p>
	 * @param propName 属性名称
	 * @param value	属性值
	 * @return int 删除记录数量
	 * @author 杨雪令
	 * @time 2016年3月8日下午4:37:40
	 * @version 1.0
	 */
	public int delete(String propName, Serializable[] value);

	/**
	 * <p>根据某个属性查找数据</p>
	 * @param propName 属性名称
	 * @param value	属性值
	 * @return List<T> 查询到的数据集合
	 * @author 杨雪令
	 * @time 2016年3月8日下午4:40:31
	 * @version 1.0
	 */
	public List<T> find(String propName, Serializable value);
	
	/**
	 * <p>根据多个属性条件查找数据</p>
	 * @param paramMap<propName(属性名称), value(属性值)>
	 * @return List<T> 查询到的数据集合
	 * @author 杨雪令
	 * @time 2016年4月29日上午11:05:37
	 * @version 1.0
	 */
	public List<T> find(Map<String, String> paramMap);

	/**
	 * <p>查找一个实体类的所有数据</p>
	 * @return List<T> 查询到的数据集合
	 * @author 杨雪令
	 * @time 2016年3月8日下午5:20:35
	 * @version 1.0
	 */
	public List<T> findAll();
}