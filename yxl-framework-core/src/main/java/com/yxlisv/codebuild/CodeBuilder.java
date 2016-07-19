package com.yxlisv.codebuild;

/**
 * 代码生成器
 * @author john Local
 */
public abstract class CodeBuilder {
	
	/**
	 * 得到文件路径
	 * @param packageName 包名
	 * @param baseDir 根目录
	 * @autor yxl
	 */
	public static String getFileDir(String packageName, String baseDir) {
		return baseDir += "/" + packageName.replaceAll("\\.", "/") + "/";
	}

	/**
	 * 生成
	 * 
	 * @autor yxl
	 */
	public abstract void build();
}
