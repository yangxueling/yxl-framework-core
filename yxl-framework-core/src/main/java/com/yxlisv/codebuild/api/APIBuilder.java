package com.yxlisv.codebuild.api;

import java.io.IOException;

import com.yxlisv.codebuild.CodeBuilder;
import com.yxlisv.codebuild.Constant;
import com.yxlisv.codebuild.entry.Entry;
import com.yxlisv.util.date.DateUtil;
import com.yxlisv.util.file.FileUtil;

/**
 * <p>API 代码生成器</p>
 * @author 杨雪令
 * @time 2016年3月15日下午5:23:56
 * @version 1.0
 */
public class APIBuilder extends CodeBuilder{
	
	//实体
	private Entry entry;
	
	/**
	 * 构造实体生成器
	 * @param entry	实体
	 */
	public APIBuilder(Entry entry){
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
		sb.append("package "+ Constant.apiPackage +";\n\n");
		
		//import
		sb.append("import com.yxlisv.service.IBaseService;\n");
		sb.append("import "+ Constant.entryPackage +"."+ entry.name +";\n\n");
		
		//类注释
		sb.append("/**\n");
		sb.append(" * <p>"+ entry.getSimpleCmt() +"API</p>\n");
		sb.append(" * @author "+ Constant.author +"\n");
		sb.append(" * @time "+ DateUtil.toTime(System.currentTimeMillis()) +"\n");
		sb.append(" * @version "+ Constant.version +"\n");
		sb.append(" */\n");
		sb.append("public interface "+ entry.name +"API extends IBaseService<"+ entry.name +"> {}");
		
		try {
			FileUtil.write(getFileDir(Constant.apiPackage, Constant.baseDir), entry.name + "API.java", sb.toString());
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
}