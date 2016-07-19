package com.yxlisv.dao;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import org.hibernate.Cache;
import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.jdbc.Work;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import com.yxlisv.util.data.Page;
import com.yxlisv.util.datasource.DataSourceBean;
import com.yxlisv.util.datasource.DynamicDataSource;
import com.yxlisv.util.hibernate.SessionManager;

/**
 * <p>hibernate 基础dao</p>
 * @author 杨雪令
 * @time 2016年3月8日下午5:22:54
 * @version 1.0
 */
@Repository
public abstract class BaseHibernateDao {

	// 定义一个全局的记录器，通过LoggerFactory获取
	protected Logger logger = LoggerFactory.getLogger(getClass());

	// 是否开启查询缓存
	protected static boolean queryCache = true;

	/**
	 * <p>设置是否开启查询缓存</p>
	 * @param queryCache 是否开启查询缓存 
	 * @author 杨雪令
	 * @time 2016年3月13日下午11:17:43
	 * @version 1.0
	 */
	public static void setQueryCache(boolean queryCache) {
		BaseHibernateDao.queryCache = queryCache;
	}

	/**
	 * <p>执行Query前的处理</p>
	 * @param query Hibernate Query 
	 * @author 杨雪令
	 * @time 2016年3月13日下午11:16:23
	 * @version 1.0
	 */
	protected void prepQuery(Query query) {
		query.setCacheable(queryCache);
		DataSourceBean dataSourceBean = DynamicDataSource.getCurrentDataSourceBean();
		if (dataSourceBean != null) query.setCacheRegion(dataSourceBean.getIdentifierKey());
	}

	/**
	 * <p>执行Criteria前的处理</p>
	 * @param query Hibernate Criteria 
	 * @author 杨雪令
	 * @time 2016年3月13日下午11:16:23
	 * @version 1.0
	 */
	protected void prepCriteria(Criteria criteria) {
		criteria.setCacheable(queryCache);
		DataSourceBean dataSourceBean = DynamicDataSource.getCurrentDataSourceBean();
		if (dataSourceBean != null) criteria.setCacheRegion(dataSourceBean.getIdentifierKey());
	}

	/**
	 * <p>创建Hibernate Query</p>
	 * @param hql hql语句
	 * @return Query Hibernate Query
	 * @author 杨雪令
	 * @time 2016年3月13日下午11:11:34
	 * @version 1.0
	 */
	protected Query createQuery(String hql) {
		Query query = SessionManager.getSession().createQuery(hql);
		prepQuery(query);
		return query;
	}

	/**
	 * <p>创建Hibernate SQL Query</p>
	 * <p>此方法可以编写SQL语句，通常情况不允许使用此方法</p>
	 * @param sql sql语句
	 * @return Query Hibernate Query
	 * @author 杨雪令
	 * @time 2016年3月13日下午11:12:25
	 * @version 1.0
	 */
	protected Query createSQLQuery(String sql) {
		Query query = SessionManager.getSession().createSQLQuery(sql);
		prepQuery(query);
		return query;
	}

	/**
	 * <p>创建Hibernate Criteria</p>
	 * @param entryClass 实体类Class
	 * @return Criteria Hibernate Criteria
	 * @author 杨雪令
	 * @time 2016年3月13日下午11:13:44
	 * @version 1.0
	 */
	@SuppressWarnings("rawtypes")
	protected Criteria createCriteria(Class entryClass) {
		Criteria criteria = SessionManager.getSession().createCriteria(entryClass);
		prepCriteria(criteria);
		return criteria;
	}

	/**
	 * <p>创建Hibernate Criteria</p>
	 * @param entityName 实体类名称
	 * @return Criteria Hibernate Criteria
	 * @author 杨雪令
	 * @time 2016年3月13日下午11:14:32
	 * @version 1.0
	 */
	protected Criteria createCriteria(String entityName) {
		Criteria criteria = SessionManager.getSession().createCriteria(entityName);
		prepCriteria(criteria);
		return criteria;
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
	public <T> T get(Class<T> entityType, Serializable id) {
		return SessionManager.getSession().get(entityType, id);
	}

	/**
	 * <p>分页查询</p>
	 * <p>hql中包含查询条件，使用名称占位：如userId=:userId，占位符名称和paramMap中的key对应</p>
	 * @param hql hql查询语句
	 * @param paramMap 查询条件
	 * @param page 分页对象
	 * @return Page 分页对象
	 * @author 杨雪令
	 * @time 2016年3月15日上午9:54:35
	 * @version 1.0
	 */
	public Page page(String hql, Page page) {

		// 查询总页数
		if (page.isGetTotalRows()) {
			Query countQuery = createQuery("select count(1) " + hql.substring(hql.indexOf("from")));
			Object count = countQuery.uniqueResult();
			if (count != null) page.setTotalRows((Long) count);
			if (page.getTotalRows() <= 0) return page;
		}

		// 查询数据
		Query query = createQuery(hql.toString());
		query.setFirstResult(page.getStartRow());
		query.setMaxResults(page.getPageSize());
		page.setResult(query.list());

		return page;
	}

	/**
	 * <p>执行 Insert SQL Query</p>
	 * <p>此方法可以编写SQL语句，通常情况不允许使用此方法</p>
	 * @param sql sql语句
	 * @return Query Hibernate Query
	 * @author 杨雪令
	 * @time 2016年3月13日下午11:12:25
	 * @version 1.0
	 */
	protected void excuteInsertSQL(final String sql) {
		final Session session = SessionManager.getSession();
		session.doWork(new Work() {

			@Override
			public void execute(Connection connection) throws SQLException {
				Statement stmt = null;
				try {
					stmt = connection.createStatement();
					stmt.executeUpdate(sql);
					Cache cache = session.getSessionFactory().getCache();
					if(cache!=null) cache.evictQueryRegions();
				} finally {
					if(stmt!=null) stmt.close();
				}
			}
		});
	}
}