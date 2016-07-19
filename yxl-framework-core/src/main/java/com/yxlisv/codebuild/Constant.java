package com.yxlisv.codebuild;

import java.util.ArrayList;
import java.util.List;

import com.yxlisv.codebuild.control.ControlPackage;

/**
 * 常量类
 * @author yxl
 */
public class Constant {

	// 作者
	public static String author = "YCB";
	// 版本
	public static String version = "1.1";
	
	//sql文件所在目录
	public static String baseDir = "";
	
	//表数量
	public static int tableCount = 0;
	
	//表名是否大写
	public static boolean tableUpperCase = false;
	
	//字段是否大写
	public static boolean columnUpperCase = false;

	// 各种包名
	public static String entryPackage = "com.yxlisv.entry";
	public static String daoBasePackage = "com.yxlisv.dao";
	public static String daoPackage = "com.yxlisv.dao";
	public static String serviceBasePackage = "com.yxlisv.service";
	public static String servicePackage = "com.yxlisv.service";
	public static String apiBasePackage = "com.yxlisv.api";
	public static String apiPackage = "com.yxlisv.api";

	// 是否使用hibernate延迟加载
	public static boolean hibernateLazy = true;
	// 是否生成hibernate OneToMany 映射
	public static boolean hibernateOneToMany = true;

	// control 层的包，可能有多个Control层
	public static List<ControlPackage> controlPackages;
	
	// 实体类名称忽略字符串
	public static String entryNameIgnore;
	
	// 实体类字段名称忽略字符串
	public static String entryPropNameIgnore;

	// control 默认包
	static {
		controlPackages = new ArrayList<ControlPackage>();
		ControlPackage controlPackage = new ControlPackage();
		controlPackage.packageName = "com.yxlisv.admin.control";
		controlPackage.pathQz = "admin";
		controlPackages.add(controlPackage);
	}
}