package com.yxlisv.dao;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.JoinColumn;
import javax.persistence.Table;

import org.hibernate.Query;
import org.hibernate.criterion.Restrictions;

import com.yxlisv.util.data.Page;
import com.yxlisv.util.datasource.DataSourceBean;
import com.yxlisv.util.datasource.DynamicDataSource;
import com.yxlisv.util.datasource.DataSourceBean.DBTypes;
import com.yxlisv.util.date.DateUtil;
import com.yxlisv.util.hibernate.SessionManager;
import com.yxlisv.util.reflect.BeansUtils;
import com.yxlisv.util.reflect.GenericsUtil;
import com.yxlisv.util.reflect.ReflectionUtils;
import com.yxlisv.util.security.SecurityUtil;
import com.yxlisv.util.string.StringUtil;

/**
 * <p>实体类的基础dao</p>
 * 泛型T代表实体类，在子类中声明，如：UserDao extends BaseHibernateEntryDao<User>
 * @author 杨雪令
 * @time 2016年3月8日下午4:27:36
 * @version 1.0
 */
public abstract class BaseHibernateEntryDao<T> extends BaseHibernateDao {

	/** 通过反射机制读取泛型中的实体类 */
	@SuppressWarnings("unchecked")
	protected Class<T> entityClass = GenericsUtil.getClass(getClass());

	/** 通过反射机制读取实体类的字段名 */
	protected List<String> propNames = BeansUtils.getFieldsAndRefer(entityClass);

	/** 通过反射机制读取实体类的表名：Map<实体属性名：类型> */
	protected Map<String, PROP_TYPE> propTypes = null;

	/** 属性类别：数字，字符串，日期，实体类 */
	protected enum PROP_TYPE {
		Number, String, Date, Entry
	}

	/** 表名 */
	protected String tableName = ((Table) entityClass.getAnnotation(Table.class)).name();

	/** 表字段，类属性映射：Map<表字段名 ：实体属性名> */
	protected Map<String, String> tableColumnPropMap = null;

	/** 类属性，表字段映射：Map<实体属性名 ：表字段名> */
	protected Map<String, String> propTableColumnMap = null;

	/**
	 * <p>初始化</p>
	 * @author 杨雪令
	 * @time 2016年5月16日下午3:49:36
	 * @version 1.0
	 */
	public BaseHibernateEntryDao() {
		initPropTypes();
		initTableColumn();
	}

	/**
	 * <p>初始化表列名</p>
	 * @author 杨雪令
	 * @time 2016年5月16日下午1:48:43
	 * @version 1.0
	 */
	protected synchronized void initPropTypes() {
		if (propTypes != null) return;
		propTypes = new HashMap<String, PROP_TYPE>();
		for (Field field : entityClass.getDeclaredFields()) {
			String fieldTypeName = field.getType().getName();
			String fieldName = field.getName();
			if (fieldTypeName.contains("int")) propTypes.put(fieldName, PROP_TYPE.Number);
			else if (fieldTypeName.contains("float")) propTypes.put(fieldName, PROP_TYPE.Number);
			else if (fieldTypeName.contains("double")) propTypes.put(fieldName, PROP_TYPE.Number);
			else if (fieldTypeName.contains("shot")) propTypes.put(fieldName, PROP_TYPE.Number);
			else if (fieldTypeName.contains("Integer")) propTypes.put(fieldName, PROP_TYPE.Number);
			else if (fieldTypeName.contains("Float")) propTypes.put(fieldName, PROP_TYPE.Number);
			else if (fieldTypeName.contains("Double")) propTypes.put(fieldName, PROP_TYPE.Number);
			else if (fieldTypeName.contains("String")) propTypes.put(fieldName, PROP_TYPE.String);
			else if (fieldTypeName.contains("Date")) propTypes.put(fieldName, PROP_TYPE.Date);
			else if (!fieldTypeName.contains("List") && !fieldTypeName.contains("Set")) propTypes.put(fieldName, PROP_TYPE.Entry);
		}
	}

	/**
	 * <p>初始化表列名</p>
	 * @author 杨雪令
	 * @time 2016年5月16日下午1:48:43
	 * @version 1.0
	 */
	protected synchronized void initTableColumn() {
		if (tableColumnPropMap != null && propTableColumnMap != null) return;
		tableColumnPropMap = new HashMap<String, String>();
		propTableColumnMap = new HashMap<String, String>();
		for (Method method : entityClass.getMethods()) {
			String methodName = method.getName();
			if (!methodName.startsWith("get")) continue;
			String tableColumn = "";
			Column column = method.getAnnotation(Column.class);
			if (column != null) tableColumn = column.name();
			else {
				JoinColumn joinColumn = method.getAnnotation(JoinColumn.class);
				if (joinColumn == null) continue;
				tableColumn = joinColumn.name();
			}
			String propName = StringUtil.toLower4FirstWord(methodName.substring(3));
			tableColumnPropMap.put(tableColumn, propName);
			propTableColumnMap.put(propName, tableColumn);
		}
	}

	/**
	 * <p>校验属性名是否有效</p>
	 * @param propName	属性名
	 * @return boolean 是否有效
	 * @author 杨雪令
	 * @time 2016年3月14日下午2:31:57
	 * @version 1.0
	 */
	protected boolean validationPropName(String propName) {
		if (propNames.contains(propName)) return true;
		if (propName == null) return false;
		return false;
	}

	/**
	 * <p>设置创建时间</p> 
	 * @param object 要修改的实体类对象
	 * @author 杨雪令
	 * @time 2016年3月17日下午1:17:29
	 * @version 1.0
	 */
	private void setCreateTime(Object object) {
		if (propNames.contains("createTime")) ReflectionUtils.setFieldValue(object, "createTime", new Date());
	}

	/**
	 * <p>设置修改时间</p>
	 * @param object 要修改的实体类对象
	 * @author 杨雪令
	 * @time 2016年3月17日下午1:17:29
	 * @version 1.0
	 */
	private void setUpdateTime(Object object) {
		if (propNames.contains("updateTime")) ReflectionUtils.setFieldValue(object, "updateTime", new Date());
	}

	/**
	 * <p>保存一个对象</p>
	 * @param obj 要保存的对象
	 * @author 杨雪令
	 * @time 2016年3月8日下午3:32:11
	 * @version 1.0
	 */
	public T save(T obj) {
		setCreateTime(obj);
		setUpdateTime(obj);
		SessionManager.getSession().save(obj);
		return obj;
	}

	/**
	 * <p>批量保存对象</p>
	 * @param dataList 要保存的对象集合
	 * @author 杨雪令
	 * @time 2016年5月16日下午3:33:33
	 * @version 1.0
	 */
	public void save(final List<T> dataList) {
		save(dataList, null, 10000);
	}
	
	/**
	 * <p>批量保存对象</p>
	 * @param dataList 要保存的对象集合
	 * @param propNameList 要保存的字段集合
	 * @author 杨雪令
	 * @time 2016年5月16日下午3:33:33
	 * @version 1.0
	 */
	public void save(final List<T> dataList, List<String> propNameList) {
		save(dataList, propNameList, 10000);
	}

	/**
	 * <p>批量保存对象</p>
	 * @param dataList 要保存的对象集合
	 * @param propNameList 要保存的字段集合
	 * @param batchSize 一次批量保存数据量
	 * @author 杨雪令
	 * @time 2016年5月16日下午3:33:33
	 * @version 1.0
	 */
	public void save(final List<T> dataList, List<String> propNameList, int batchSize) {
		if (dataList == null || dataList.size() == 0) return;
		
		DataSourceBean dataSourceBean = DynamicDataSource.getCurrentDataSourceBean();
		
		//如果不是mysql，一条一条保存
		if(!dataSourceBean.getDbType().equals(DBTypes.MYSQL)){
			for(T obj : dataList){
				SessionManager.getSession().save(obj);
			}
			return;
		}

		//Mysql批量拼接InsertSql
		// 要插入的列，存放表字段
		List<String> insertColumnList = new ArrayList<String>();
		if (propNameList != null && propNameList.size() > 0) {
			for (String propName : propNameList) {
				if (validationPropName(propName)) insertColumnList.add(propTableColumnMap.get(propName));
			}
		} else {
			for (Map.Entry<String, String> colums : tableColumnPropMap.entrySet()) {
				insertColumnList.add(colums.getKey());
			}
		}

		// 拼接列名
		String columns = "";
		for (String colums : insertColumnList) {
			if (columns.length() > 0) columns += ",";
			columns += colums;
		}
		String sql = "insert into " + tableName + " (" + columns + ") values ${values}";
		StringBuilder values = new StringBuilder();

		// 已保存数量
		int count = 1;
		for (Object obj : dataList) {
			if (values.length() > 0) values.append(",");
			values.append("(");
			int index = 0;
			for (String colums : insertColumnList) {
				String propName = tableColumnPropMap.get(colums);
				index++;
				if (index > 1) values.append(",");
				Object value = ReflectionUtils.getFieldValue(obj, propName);
				if (colums.toUpperCase().equals("ID") && value == null) {
					values.append("'" + UUID.randomUUID().toString().replaceAll("-", "") + "'");
					continue;
				}
				if (value == null) {
					values.append("NULL");
					continue;
				}

				if (propTypes.get(propName).equals(PROP_TYPE.Number)) {
					values.append(value);
				} else if (propTypes.get(propName).equals(PROP_TYPE.String)) {
					values.append("'" + SecurityUtil.simpleClear(value.toString()) + "'");
				} else if (propTypes.get(propName).equals(PROP_TYPE.Date)) {
					values.append("'" + DateUtil.toTime(((Date) value).getTime()) + "'");
				} else if (propTypes.get(propName).equals(PROP_TYPE.Entry)) {
					Object entryId = ReflectionUtils.getFieldValue(obj, propName + ".id");
					if (entryId == null) values.append("NULL");
					else values.append("'" + entryId + "'");
				}
			}
			values.append(")");
			count++;

			// 达到批量保存数据上限，保存数据
			if (count % batchSize == 0 && values.length() > 0) {
				String batchSql = sql.replaceAll("\\$\\{values\\}", values.toString());
				// createSQLQuery(batchSql).executeUpdate();
				excuteInsertSQL(batchSql);
				values.setLength(0);
			}
		}

		// 保存最后一批数据
		if (values.length() > 0) {
			String batchSql = sql.replaceAll("\\$\\{values\\}", values.toString());
			// createSQLQuery(batchSql).executeUpdate();
			excuteInsertSQL(batchSql);
		}
	}

	/**
	 * <p>修改一个对象</p>
	 * @param obj 要修改的对象 
	 * @author 杨雪令
	 * @time 2016年3月8日下午4:24:50
	 * @version 1.0
	 */
	public T update(T obj) {
		setUpdateTime(obj);
		SessionManager.getSession().update(obj);
		return obj;
	}

	/**
	 * <p>增加或修改一个对象</p>
	 * @param obj 要保存或修改的对象 
	 * @author 杨雪令
	 * @time 2016年3月8日下午4:24:50
	 * @version 1.0
	 */
	public T saveOrUpdate(T obj) {
		SessionManager.getSession().saveOrUpdate(obj);
		return obj;
	}

	/**
	 * <p>删除一个对象</p>
	 * @param obj 要删除的对象 
	 * @author 杨雪令
	 * @time 2016年3月8日下午4:23:02
	 * @version 1.0
	 */
	public void delete(T obj) {
		SessionManager.getSession().delete(obj);
	}

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
	@SuppressWarnings({ "rawtypes" })
	public Page page(Page page, Map<String, Object> paramMap) {
		// 拼装hql
		StringBuilder hql = new StringBuilder();
		hql.append("from ");
		hql.append(entityClass.getName());

		// 有效的查询条件
		List<String> validParam = new ArrayList<String>();
		// 拼装where条件
		StringBuilder hqlWhere = new StringBuilder();

		// 拼装查询条件
		for (Map.Entry param : paramMap.entrySet()) {
			String propName = param.getKey().toString();
			// String placeholder = ":" + propName.replaceAll("\\.",
			// "");//占位符名称，替换hql占位符中敏感字符

			// 运算符
			String operator = " = ";
			if (page.isFuzzy()) operator = " like ";
			if (propName.endsWith("_S")) {
				propName = propName.replaceAll("_S", "");
				operator = " >= ";
			} else if (propName.endsWith("_E")) {
				propName = propName.replaceAll("_E", "");
				operator = " <= ";
			}

			// 检查查询条件和值是否有效
			if (!validationPropName(propName)) continue;// 过滤无效的条件
			if (param.getValue() == null) continue;
			String value = param.getValue().toString().trim();
			if (value.length() <= 0) continue;
			value = SecurityUtil.simpleClear(value);
			if (operator.contains("like")) value = "%" + value + "%";

			// 拼装查询条件
			if (hqlWhere.length() == 0) hqlWhere.append(" ");
			else hqlWhere.append(" and ");
			hqlWhere.append(propName);
			hqlWhere.append(operator);
			// hqlWhere.append(placeholder);
			hqlWhere.append("'" + value + "'");
			validParam.add(propName);
		}

		// 如果有拼接查询条件，拼接到hql语句
		if (hqlWhere.length() > 0) {
			hql.append(" where").append(hqlWhere);
		}

		// 拼装排序条件
		Map<String, String> orderMap = page.getOrderMap();
		if (orderMap != null && orderMap.size() > 0) {
			StringBuilder hqlOrder = new StringBuilder();
			for (Map.Entry entry : orderMap.entrySet()) {
				String key = entry.getKey().toString();
				if (!validationPropName(key)) continue;// 过滤无效的条件
				String value = entry.getValue().toString();
				value = SecurityUtil.simpleClear(value);
				if (hqlOrder.length() > 0) hqlOrder.append(", ");
				hqlOrder.append(key);
				hqlOrder.append(" ");
				hqlOrder.append(value);
			}
			if (hqlOrder.length() > 0) {
				hql.append(" order by ").append(hqlOrder);
			}
		}

		return page(hql.toString(), page);
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
		return get(entityClass, id);
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
	@SuppressWarnings("unchecked")
	public T get(String propName, Object value) {
		// 校验实体类属性是否有效
		if (!validationPropName(propName)) {
			logger.warn("无效的属性：" + propName + ", " + entityClass + ".get(" + propName + ", " + value + ")");
			return null;
		}
		if(value == null) return null;

		Query query = createQuery("from " + entityClass.getName() + " where "+ propName +"=?");
		if(value instanceof String) query.setString(0, value + "");
		else if(value instanceof Integer) query.setInteger(0, (int) value);
		else if(value instanceof Float) query.setFloat(0, (float) value);
		else if(value instanceof Double) query.setDouble(0, (double) value);
		else if(value instanceof Long) query.setLong(0, (long) value);
		else if(value instanceof Boolean) query.setBoolean(0, (boolean) value);
		else if(value instanceof Date) query.setDate(0, (Date) value);
		List<T> list = query.list();

		int resultCount = list.size();
		if (resultCount == 1) {// 如果只有一条记录，返回这条记录
			return list.get(0);
		} else if (resultCount > 1) {// 如果有多条记录，返回第一条记录
			logger.error("查询单条记录时数据库返回了 " + resultCount + " 条记录[entity:" + entityClass.getName() + ", propName:" + propName + ", value:" + value + "]");
			return list.get(0);
		}
		return null;
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
		// 校验实体类属性是否有效
		if (!validationPropName(propName)) {
			logger.warn("无效的属性：" + propName + ", " + entityClass + ".delete(" + propName + ", " + value + ")");
			return 0;
		}

		String placeholder = propName.replaceAll("\\.", "");
		return createQuery("delete from " + entityClass.getName() + " where " + propName + "=:" + placeholder).setParameter(placeholder, value).executeUpdate();
	}

	/**
	 * <p>批量删除数据</p>
	 * <p>根据某个属性批量删除数据</p>
	 * @param propName 属性名称
	 * @param values	属性值数组
	 * @return int 删除记录数量
	 * @author 杨雪令
	 * @time 2016年3月8日下午4:37:40
	 * @version 1.0
	 */
	public int delete(String propName, Serializable[] values) {
		// 校验实体类属性是否有效
		if (!validationPropName(propName)) {
			logger.warn("无效的属性：" + propName + ", " + entityClass + ".delete(" + propName + ", " + values + ")");
			return 0;
		}

		// 数据校验
		if (values == null || values.length < 1) return 0;

		String placeholder = propName.replaceAll("\\.", "");
		return createQuery("delete from " + entityClass.getName() + " where " + propName + " in (:" + placeholder + ")").setParameterList(placeholder, values).executeUpdate();
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
	@SuppressWarnings("unchecked")
	public List<T> find(String propName, Serializable value) {
		if (!validationPropName(propName)) {
			// 校验实体类属性是否有效
			logger.warn("无效的属性：" + propName + ", " + entityClass + ".find(" + propName + ", " + value + ")");
			return null;
		}

		return createQuery("from " + entityClass.getName() + " where " + propName + "='" + SecurityUtil.simpleClear(value.toString()) + "'").list();
	}

	/**
	 * <p>根据多个属性条件查找数据</p>
	 * @param paramMap<propName(属性名称), value(属性值)>
	 * @return List<T> 查询到的数据集合
	 * @author 杨雪令
	 * @time 2016年4月29日上午11:05:37
	 * @version 1.0
	 */
	@SuppressWarnings("unchecked")
	public List<T> find(Map<String, String> paramMap) {
		StringBuilder hql = new StringBuilder("from " + entityClass.getName());
		for (Map.Entry<String, String> param : paramMap.entrySet()) {
			String propName = param.getKey();
			if (!validationPropName(propName)) continue;
			String value = param.getValue();
			if (value != null) value = SecurityUtil.simpleClear(value);
			if (!hql.toString().contains("where")) hql.append(" where ");
			else hql.append(" and ");
			if (value != null) hql.append(propName + "='" + value + "'");
			else hql.append(propName + " is null");
		}
		return createQuery(hql.toString()).list();
	}

	/**
	 * <p>查找一个实体类的所有数据</p>
	 * @return List<T> 查询到的数据集合
	 * @author 杨雪令
	 * @time 2016年3月8日下午5:20:35
	 * @version 1.0
	 */
	@SuppressWarnings("unchecked")
	public List<T> findAll() {
		return createQuery("from " + entityClass.getName()).list();
	}
}