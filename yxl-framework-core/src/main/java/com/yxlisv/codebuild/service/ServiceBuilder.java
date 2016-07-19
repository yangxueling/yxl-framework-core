package com.yxlisv.codebuild.service;

import java.io.IOException;

import com.yxlisv.codebuild.CodeBuilder;
import com.yxlisv.codebuild.Constant;
import com.yxlisv.codebuild.entry.Entry;
import com.yxlisv.util.date.DateUtil;
import com.yxlisv.util.file.FileUtil;

/**
 * service层生成器
 * @author john Local
 */
public class ServiceBuilder extends CodeBuilder{
	
	//实体
	private Entry entry;
	
	/**
	 * 构造实体生成器
	 * @param entry	实体
	 */
	public ServiceBuilder(Entry entry){
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
		sb.append("package "+ Constant.servicePackage +";\n\n");
		
		//import
		sb.append("import javax.annotation.Resource;\n");
		sb.append("import org.springframework.stereotype.Service;\n\n");
		
		sb.append("import "+ Constant.entryPackage +"."+ entry.name +";\n");
		sb.append("import "+ Constant.daoPackage +"."+ entry.name +"Dao;\n");
		sb.append("import "+ Constant.apiPackage +"."+ entry.name +"API;\n");
		sb.append("import "+ Constant.serviceBasePackage +".BaseService;\n\n");
		
		//类注释
		sb.append("/**\n");
		sb.append(" * <p>"+ entry.getSimpleCmt() +"Service层</p>\n");
		sb.append(" * @author "+ Constant.author +"\n");
		sb.append(" * @time "+ DateUtil.toTime(System.currentTimeMillis()) +"\n");
		sb.append(" * @version "+ Constant.version +"\n");
		sb.append(" */\n");
		sb.append("@Service\n");
		sb.append("public class "+ entry.name +"Service extends BaseService<"+ entry.name +"> implements "+ entry.name +"API {\n\n");
		
		sb.append("\t/** "+ entry.getSimpleCmt() +"Dao */\n");
		sb.append("\t@Resource\n");
		sb.append("\tprivate "+ entry.name +"Dao "+ entry.getLowerName() +"Dao;\n");
		
		sb.append("}");
		
		try {
			FileUtil.write(getFileDir(Constant.servicePackage, Constant.baseDir), entry.name + "Service.java", sb.toString());
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
		sb.append("import com.yxlisv.service.AbstractBaseService;\n\n");
		
		//类注释
		sb.append("/**\n");
		sb.append(" * <p>项目所有Service的父类</p>\n");
		sb.append(" * @author "+ Constant.author +"\n");
		sb.append(" * @time "+ DateUtil.toTime(System.currentTimeMillis()) +"\n");
		sb.append(" * @version "+ Constant.version +"\n");
		sb.append(" */\n");
		sb.append("public class BaseService<T> extends AbstractBaseService<T> {}");
		
		try {
			FileUtil.write(CodeBuilder.getFileDir(packageName, baseDir), "BaseService.java", sb.toString());
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
}