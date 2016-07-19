package com.yxlisv.codebuild.dubbo;

import java.io.IOException;

import com.yxlisv.codebuild.CodeBuildCache;
import com.yxlisv.codebuild.Constant;
import com.yxlisv.codebuild.entry.Entry;
import com.yxlisv.util.file.FileUtil;

/**
 * <p>Dubbo相关代码生成器</p>
 * @author 杨雪令
 * @time 2016年3月15日下午5:23:34
 * @version 1.0
 */
public class DubboBuilder{
	
	/**
	 * <p>生成配置文件</p>
	 * @author 杨雪令
	 * @time 2016年3月15日下午5:24:53
	 * @version 1.0
	 */
	public static void builderConfig(){
		builderProviderConfig();
		builderConsumerConfig();
	}
	
	/**
	 * <p>生成提供者配置文件</p>
	 * @author 杨雪令
	 * @time 2016年3月15日下午5:24:53
	 * @version 1.0
	 */
	public static void builderProviderConfig(){
		StringBuilder sb = new StringBuilder();
		sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
		sb.append("<beans xmlns=\"http://www.springframework.org/schema/beans\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:dubbo=\"http://code.alibabatech.com/schema/dubbo\" xsi:schemaLocation=\"http://www.springframework.org/schema/beans\n");
		sb.append("\thttp://www.springframework.org/schema/beans/spring-beans.xsd\n");
		sb.append("\thttp://code.alibabatech.com/schema/dubbo\n");
		sb.append("\thttp://code.alibabatech.com/schema/dubbo/dubbo.xsd\">\n\n");
		sb.append("\t<!-- dubbo 服务提供者配置 -->\n");
		
		for(Entry entry : CodeBuildCache.entryList){
			sb.append("\n\t<!-- "+ entry.getSimpleCmt() +" 服务声明 -->\n");
			sb.append("\t<dubbo:service interface=\""+ Constant.apiPackage +"."+ entry.name +"API\" ref=\""+ entry.getLowerName() +"Service\" version=\"${dubbo.version}\"/>\n");
		}
		sb.append("</beans>");
		
		try {
			FileUtil.write(Constant.baseDir, "dubbo-provider.xml", sb.toString());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
	/**
	 * <p>生成消费者配置文件</p>
	 * @param baseDir 根目录
	 * @author 杨雪令
	 * @time 2016年3月15日下午5:24:53
	 * @version 1.0
	 */
	public static void builderConsumerConfig(){
		StringBuilder sb = new StringBuilder();
		sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
		sb.append("<beans xmlns=\"http://www.springframework.org/schema/beans\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:dubbo=\"http://code.alibabatech.com/schema/dubbo\" xsi:schemaLocation=\"http://www.springframework.org/schema/beans\n");
		sb.append("\thttp://www.springframework.org/schema/beans/spring-beans.xsd\n");
		sb.append("\thttp://code.alibabatech.com/schema/dubbo\n");
		sb.append("\thttp://code.alibabatech.com/schema/dubbo/dubbo.xsd\">\n\n");
		sb.append("\t<!-- dubbo 服务消费者配置 -->\n");
		
		for(Entry entry : CodeBuildCache.entryList){
			sb.append("\n\t<!-- "+ entry.getSimpleCmt() +" 服务引用 -->\n");
			sb.append("\t<dubbo:reference id=\""+ entry.getLowerName() +"API\" interface=\""+ Constant.apiPackage +"."+ entry.name +"API\" version=\"${dubbo.version}\"/>\n");
		}
		sb.append("</beans>");
		
		try {
			FileUtil.write(Constant.baseDir, "dubbo-consumer.xml", sb.toString());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}