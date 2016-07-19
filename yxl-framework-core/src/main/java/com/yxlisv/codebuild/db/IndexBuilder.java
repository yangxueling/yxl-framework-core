package com.yxlisv.codebuild.db;

import java.io.IOException;

import com.yxlisv.codebuild.CodeBuildCache;
import com.yxlisv.codebuild.CodeBuilder;
import com.yxlisv.codebuild.Constant;
import com.yxlisv.codebuild.entry.Entry;
import com.yxlisv.codebuild.entry.Property;
import com.yxlisv.util.file.FileUtil;
import com.yxlisv.util.math.NumberUtil;

/**
 * <p>数据库索引生成器</p>
 * <p>生成单个字段索引</p>
 * @author 杨雪令
 * @time 2016年3月24日下午2:55:04
 * @version 1.0
 */
public class IndexBuilder extends CodeBuilder {

	@Override
	public void build() {}

	/**
	 * <p>生成简单索引</p> 
	 * @author 杨雪令
	 * @time 2016年3月24日下午2:55:54
	 * @version 1.0
	 */
	public static void buildSimple() {
		StringBuffer sb = new StringBuffer();
		for (Entry entry : CodeBuildCache.entryList) {
			if(sb.length()>0) sb.append("\n");
			Property idProp = entry.getIdProperty();
			for (Property property : entry.getPropertyList()) {
				if (property.name.equals(idProp.name)) continue;
				if (NumberUtil.parseInt(property.size)>50) continue;
				sb.append("ALTER TABLE `"+ entry.tableName +"` ADD INDEX INDEX_"+ entry.tableName +"_"+ property.tName +"(`"+ property.tName +"`);\n");
			}
		}

		try {
			FileUtil.write(Constant.baseDir, "dbIndex.sql", sb.toString());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}