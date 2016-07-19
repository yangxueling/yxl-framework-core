package com.yxlisv.codebuild.dao;

import java.io.IOException;

import com.yxlisv.codebuild.CodeBuilder;
import com.yxlisv.codebuild.Constant;
import com.yxlisv.codebuild.entry.Entry;
import com.yxlisv.util.date.DateUtil;
import com.yxlisv.util.file.FileUtil;


/**
 * <p>Dao层生成器</p>
 * @author 杨雪令
 * @time 2016年3月17日上午10:08:10
 * @version 1.0
 */
public class DaoBuilder extends CodeBuilder{
	
	//实体
	private Entry entry;
	
	/**
	 * 构造实体生成器
	 * @param packageName 包名
	 * @param baseDir	根目录
	 * @param entry	实体
	 */
	public DaoBuilder(Entry entry){
		this.entry = entry;
	}
	
	/**
	 * 生成文件
	 * @param fileDir 文件目录
	 * 
	 * @autor yxl
	 */
	@Override
	public void build(){
		
		StringBuffer sb = new StringBuffer();
		
		//包
		sb.append("package "+ Constant.daoPackage +";\n\n");
		
		//import
		sb.append("import org.springframework.stereotype.Repository;\n\n");
		
		sb.append("import "+ Constant.daoBasePackage +".BaseDao;\n");
		sb.append("import "+ Constant.entryPackage +"."+ entry.name +";\n\n");
		
		//类注释
		sb.append("/**\n");
		sb.append(" * <p>"+ entry.getSimpleCmt() +"Dao层</p>\n");
		sb.append(" * @author "+ Constant.author +"\n");
		sb.append(" * @time "+ DateUtil.toTime(System.currentTimeMillis()) +"\n");
		sb.append(" * @version "+ Constant.version +"\n");
		sb.append(" */\n");
		sb.append("@Repository\n");
		sb.append("public class "+ entry.name +"Dao extends BaseDao<"+ entry.name +"> {}");
		
		try {
			FileUtil.write(getFileDir(Constant.daoPackage, Constant.baseDir), entry.name + "Dao.java", sb.toString());
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	
	/**
	 * 生成父类
	 * @param fileDir 文件目录
	 * 
	 * @autor yxl
	 */
	public static void buildParentClass(String packageName, String baseDir){
		
		StringBuffer sb = new StringBuffer();
		
		//包
		sb.append("package "+ packageName +";\n\n");
		
		sb.append("import com.yxlisv.dao.BaseHibernateEntryDao;\n\n");
		
		//类注释
		sb.append("/**\n");
		sb.append(" * <p>项目所有Dao层的父类</p>\n");
		sb.append(" * @author "+ Constant.author +"\n");
		sb.append(" * @time "+ DateUtil.toTime(System.currentTimeMillis()) +"\n");
		sb.append(" * @version "+ Constant.version +"\n");
		sb.append(" */\n");
		sb.append("public class BaseDao<T> extends BaseHibernateEntryDao<T> {}");
		
		try {
			FileUtil.write(CodeBuilder.getFileDir(packageName, baseDir), "BaseDao.java", sb.toString());
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
}