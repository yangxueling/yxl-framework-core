package com.yxlisv.codebuild.validation;

import java.io.IOException;

import com.yxlisv.codebuild.CodeBuildCache;
import com.yxlisv.codebuild.CodeBuilder;
import com.yxlisv.codebuild.Constant;
import com.yxlisv.codebuild.entry.Entry;
import com.yxlisv.codebuild.entry.Property;
import com.yxlisv.util.date.DateUtil;
import com.yxlisv.util.file.FileUtil;
import com.yxlisv.util.string.StringUtil;

/**
 * <p>数据校验相关资源生成器</p>
 * @author 杨雪令
 * @time 2016年3月23日下午12:01:18
 * @version 1.0
 */
public class ValidationBuilder extends CodeBuilder {

	/**
	 * 生成文件
	 * @param fileDir 文件目录
	 * 
	 * @autor yxl
	 */
	@Override
	public void build() {}

	/**
	 * <p>生成实体类I18N校验文件</p>
	 * @author 杨雪令
	 * @time 2016年3月23日下午12:02:23
	 * @version 1.0
	 */
	public static void buildEntryValidationI18n() {

		StringBuffer sb = new StringBuffer();
		String value = "";
		for (Entry entry : CodeBuildCache.entryList) {
			value = entry.getSimpleCmt();
			if (value == null || value.trim().length() == 0) value = entry.name;
			if (sb.length() > 0) sb.append("\n");
			sb.append(entry.name + "=" + StringUtil.toUnicode(value) + "\n");
			for (Property property : entry.getPropertyList()) {
				value = property.getSimpleCmt();
				if (value == null || value.trim().length() == 0) value = property.name;
				sb.append(entry.name + "." + property.name + "=" + StringUtil.toUnicode(value) + "\n");
			}
		}

		try {
			FileUtil.write(Constant.baseDir, "validation.properties", sb.toString());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * <p>生成实体类校验文件</p>
	 * @author 杨雪令
	 * @time 2016年3月23日下午12:02:23
	 * @version 1.0
	 */
	public static void buildEntryValidation() {

		StringBuffer sb = new StringBuffer();
		for (Entry entry : CodeBuildCache.entryList) {
			if (sb.length() > 0) sb.append("\n");
			Property idProp = entry.getIdProperty();
			for (Property property : entry.getPropertyList()) {
				if (property.name.equals(idProp.name)) continue;
				if (property.isClass()) continue;
				sb.append(entry.name + "." + property.name + "=");
				sb.append("0,");
				sb.append(property.size + ",");
				sb.append(property.notnull);
				sb.append("\n");
			}
		}
		sb.insert(0, "#类名.属性名=最小长度,最大长度,不能为空,正则表达式\n");
		try {
			FileUtil.write(Constant.baseDir, "validation.config", sb.toString());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}