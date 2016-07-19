package com.yxlisv.codebuild.entry;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import com.yxlisv.codebuild.CodeBuildCache;
import com.yxlisv.codebuild.Constant;
import com.yxlisv.util.math.NumberUtil;
import com.yxlisv.util.string.StringUtil;

/**
 * <p>表属性</p>
 * @author 杨雪令
 * @time 2016年3月17日上午9:36:44
 * @version 1.0
 */
public class Property {

	/** 名称 */
	public String name;

	/** 类型 */
	public String type;

	/** 长度 */
	public String size;

	/** 注释 */
	public String comment;
	
	/** 注释第二部分，括号内部的注释 */
	public String comment2;

	/** 不允许为空 */
	public boolean notnull;

	/** 在table中的名称 */
	public String tName;

	/** 在table中的类型 */
	public String tType;

	/** 实体类 */
	public Entry entry;

	/** 是否是i18n字段 */
	public boolean isI18n = false;

	/** i18n映射 */
	public Map<String, String> i18nMap = null;

	/** 警告 */
	public static String waring = "";

	/** 字段类型映射 */
	public static Map<String, String> propertyTypeMap;

	static {
		propertyTypeMap = new HashMap<String, String>();
		propertyTypeMap.put("int", "int");
		propertyTypeMap.put("smallint", "int");
		propertyTypeMap.put("tinyint", "int");
		propertyTypeMap.put("bit", "int");
		propertyTypeMap.put("bigint", "long");
		propertyTypeMap.put("varchar", "String");
		propertyTypeMap.put("char", "String");
		propertyTypeMap.put("float", "float");
		propertyTypeMap.put("double", "double");
		propertyTypeMap.put("decimal", "double");
		propertyTypeMap.put("timestamp", "String");
		propertyTypeMap.put("text", "String");
		propertyTypeMap.put("longtext", "String");
		propertyTypeMap.put("boolean", "boolean");
		propertyTypeMap.put("datetime", "Date");
	}

	/**
	 * <p>初始化属性</p>
	 * @param name 属性名称
	 * @param type 属性类型
	 * @param size 属性字段长度
	 * @param comment 注释
	 * @param notnull 不允许为空
	 * @param entry 所属实体类
	 * @author 杨雪令
	 * @time 2016年3月17日上午9:34:49
	 * @version 1.0
	 */
	public Property(String name, String type, String size, String comment, boolean notnull, Entry entry) {
		
		//处理字段长度
		if (size == null || size.equals("")) {
			size = "50";
			if (type.equals("text")) size = "20000";
			else if(type.equals("datetime")) size = "19";
			else if(type.equals("boolean")) size = "5";
			else if(type.equals("tinyint")) size = "3";
			else if(type.equals("int")) size = "10";
			else if(type.equals("long")) size = "19";
			else if(type.equals("float")) size = "39";
			else if(type.equals("double")) size = "39";
			else if(type.equals("decimal")) size = "39";
		}

		this.tName = name;
		if (Constant.columnUpperCase) this.tName = name.toUpperCase();
		this.tType = type;
		this.size = size;
		this.comment = comment;
		this.notnull = notnull;
		this.entry = entry;

		// 清除实体名称忽略的字符
		String propName = this.tName;
		propName = propName.replaceAll(Constant.entryPropNameIgnore.toUpperCase(), "");
		propName = propName.replaceAll(Constant.entryPropNameIgnore.toLowerCase(), "");

		this.name = this.fmtTbProp(propName);
		this.type = propertyTypeMap.get(this.tType).toString();

		// 是类属性
		if (this.isBigString()) {
			this.type = this.getClassName();
		}

		// i18n
		if (comment != null) {
			comment = comment.replaceAll("（", "(").replaceAll("）", ")").replaceAll("：", ":").replaceAll("，", ",");
			if (comment.contains("(") && comment.contains(")")) {
				comment2 = comment.substring(comment.indexOf("(") + 1);
				comment2 = comment2.substring(0, comment2.indexOf(")"));
				if (comment2.contains(":")) {
					isI18n = true;
					i18nMap = new LinkedHashMap<String, String>();
					for (String i18nObjStr : comment2.split(",")) {
						String[] i18nObj = i18nObjStr.split(":");
						i18nMap.put(i18nObj[0], i18nObj[1]);
					}
				}
			}
		}
	}

	/**
	 * 检查大文本字段
	 */
	public void checkBigText() {
		if (entry.name.equals("TextSmall")) return;
		if (entry.name.equals("TextMiddle")) return;
		if (entry.name.equals("TextBig")) return;
		if (entry.isAffiliated) return;
		if (!this.isBigString() && NumberUtil.parseInt(this.size) > 100 && NumberUtil.parseInt(this.size) <= 500) {
			waring += "*优化建议：表 '" + entry.tableName + "' 字段 '" + tName + "' 长度超过100，建议使用：text_small，修改字段注释为：" + comment + "（text_small），type改为char(32)，如果此表不做分页查询或数据统计，则无需优化\n";
		} else if (!this.isBigString() && NumberUtil.parseInt(this.size) > 500 && NumberUtil.parseInt(this.size) <= 5000) {
			waring += "*优化建议：表 '" + entry.tableName + "' 字段 '" + tName + "' 长度超过500，强烈建议使用：text_middle，修改字段注释为：" + comment + "（text_middle），type改为char(32)，此字段不优化，后期数据量大时将造成查询缓慢！！！如果此表不做分页查询或数据统计，则无需优化\n";
		} else if (!this.isBigString() && (NumberUtil.parseInt(this.size) > 5000 || type.equals("text"))) {
			waring += "*优化建议：表 '" + entry.tableName + "' 字段 '" + tName + "' 长度超过5000，强烈建议使用：text_big，修改字段注释为：" + comment + "（text_big），type改为char(32)，此字段不优化，后期数据量大时将造成查询缓慢！！！如果此表不做分页查询或数据统计，则无需优化\n";
		}
	}

	/**
	 * 判断是不是类属性（某个类的外键，判断条件结尾是_id）
	 * @autor yxl
	 */
	public boolean isClass() {
		if (this.tName.length() > 3) {
			String lastStr = this.tName.substring(this.tName.length() - 3).toUpperCase();
			if (lastStr.equals("_ID")) {
				String tableName = tName.toUpperCase().replaceAll("_ID", "").replace(Constant.entryNameIgnore.toUpperCase(), "");
				if (CodeBuildCache.tableNameCache.contains(tableName.toUpperCase())) return true;
				if (CodeBuildCache.tableNameCache.contains(tableName.toLowerCase())) return true;
				return false;
			}
		}
		// 大字符字段分离
		if (isBigString()) return true;
		return false;
	}

	/**
	 * 判断是不是主控类属性（此字段可以完全控制关联数据的增删改）
	 * @autor yxl
	 */
	public boolean isMainClass() {
		if (!isClass()) return false;

		Entry entry = CodeBuildCache.entryMap.get(this.type);
		if (entry == null) return false;
		if (entry.isAffiliated) return true; 

		return false;
	}

	/**
	 * 判断是不是大字符串
	 * @autor yxl
	 */
	public boolean isBigString() {
		if (comment != null && comment.toLowerCase().contains("text_small")) return true;
		if (comment != null && comment.toLowerCase().contains("text_middle")) return true;
		if (comment != null && comment.toLowerCase().contains("text_big")) return true;
		return false;
	}

	/**
	 * 判断是不是时间格式
	 * @autor yxl
	 */
	public boolean isTime() {
		if ((type.equals("long") || type.equals("Date")) && (name.endsWith("Time") || comment.contains("日期") || comment.contains("时间") || comment.contains("生日"))) return true;
		return false;
	}

	/**
	 * 判断是不是日期格式
	 * @autor yxl
	 */
	public boolean isDate() {
		if (isTime() && (comment.contains("日期") || comment.endsWith("日"))) return true;
		return false;
	}

	/**
	 * 判断是不是被保护的属性
	 * @autor yxl
	 */
	public boolean isProt() {
		if (name.equals("createTime")) return true;
		if (name.equals("updateTime")) return true;
		return false;
	}

	/**
	 * 如果是类属性，获取类名
	 * 如：product_id   --> Product
	 * @autor yxl
	 */
	public String getClassName() {
		String className = StringUtil.toUpper4FirstWord(this.getClassPname());
		// 大字符字段分离
		if (comment != null && comment.toLowerCase().contains("text_small")) return "TextSmall";
		if (comment != null && comment.toLowerCase().contains("text_middle")) return "TextMiddle";
		if (comment != null && comment.toLowerCase().contains("text_big")) return "TextBig";
		return className;
	}

	/**
	 * 如果是类属性，获取类的属性名
	 * 如：product_id   --> product
	 * @autor yxl
	 */
	public String getClassPname() {
		String pName = tName.toLowerCase();
		pName = pName.replaceAll(Constant.entryPropNameIgnore.toLowerCase(), "");
		if (pName.endsWith("_id")) pName = this.fmtTbProp(pName.substring(0, pName.length() - 3));
		// 大字符字段分离
		if (comment != null && comment.toLowerCase().contains("text_small")) return "textSmall";
		if (comment != null && comment.toLowerCase().contains("text_middle")) return "textMiddle";
		if (comment != null && comment.toLowerCase().contains("text_big")) return "textBig";
		return pName;
	}

	/**
	 * 获取简洁的属性注释
	 * @autor yxl
	 */
	public String getSimpleCmt() {
		String cmt = comment;
		if (cmt == null || cmt.equals("")) return this.getClassPname();

		// 不需要的字符串
		String[] badStr = { "(", "（" };

		for (String bs : badStr) {
			if (cmt.indexOf(bs) != -1) cmt = cmt.substring(0, cmt.indexOf(bs));
		}

		// 替换掉注释最后的ID
		if (cmt.length() > 2) {
			String tStr = cmt.substring(cmt.length() - 2);
			if (tStr.toLowerCase().equals("id")) cmt = cmt.substring(0, cmt.length() - 2);
		}

		return cmt;
	}

	/**
	 * 格式化表属性（自动），如 customer_need --> customerNeed
	 * @param propertyStr
	 * @autor yxl
	 */
	public String fmtTbProp(String propertyStr) {
		propertyStr = propertyStr.toLowerCase();
		propertyStr = StringUtil.toUpperBh(propertyStr, "_", 1);

		return propertyStr;
	}

	/**
	 * 得到首字母大写的属性名称
	 * @autor yxl
	 */
	public String getUpName() {
		return StringUtil.toUpper4FirstWord(this.name);
	}

	public static void main(String[] args) {
		Property property = new Property("i_test_if_id", "char", "30", "性别（0：女，1：男）", true, null);
		System.out.println(property.getClassPname());
	}
}
