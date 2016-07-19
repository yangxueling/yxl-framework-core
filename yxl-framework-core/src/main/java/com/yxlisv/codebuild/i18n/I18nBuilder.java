package com.yxlisv.codebuild.i18n;

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
 * <p>I18n相关资源生成器</p>
 * @author 杨雪令
 * @time 2016年3月23日下午12:01:18
 * @version 1.0
 */
public class I18nBuilder extends CodeBuilder {

	/**
	 * 生成文件
	 * @param fileDir 文件目录
	 * 
	 * @autor yxl
	 */
	@Override
	public void build() {}

	/**
	 * <p>生成实体类I18N配置文件</p>
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
			FileUtil.write(Constant.baseDir, "i18n.properties", sb.toString());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}