package com.yxlisv.codebuild.entry;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

import com.yxlisv.codebuild.CodeBuilder;
import com.yxlisv.codebuild.Constant;
import com.yxlisv.util.date.DateUtil;
import com.yxlisv.util.file.FileUtil;
import com.yxlisv.util.string.StringUtil;

/**
 * 实体类生成器
 * @author john Local
 */
public class EntryBuilder extends CodeBuilder {

	// 实体
	private Entry entry;
	// OneToMany关系生成器
	public static OneToManyBuilder oneToManyBuilder = new OneToManyBuilder();
	// i18n
	public static StringBuffer i18nStr = new StringBuffer();

	/**
	 * 构造实体生成器
	 * @param packageName 包名
	 * @param baseDir	根目录
	 * @param entry	实体
	 */
	public EntryBuilder(Entry entry) {
		this.entry = entry;
	}

	/**
	 * 生成文件
	 * @param fileDir 文件目录
	 * 
	 * @autor yxl
	 */
	@SuppressWarnings("rawtypes")
	@Override
	public void build() {

		StringBuffer sb = new StringBuffer();

		// 包
		sb.append("package " + Constant.entryPackage + ";");
		sb.append("\n\n");

		// import
		sb.append("import java.io.Serializable;\n");
		sb.append("import java.util.Date;\n");
		sb.append("import java.util.List;\n\n");
		sb.append("import javax.persistence.Column;\n");
		sb.append("import javax.persistence.Entity;\n");
		sb.append("import javax.persistence.FetchType;\n");
		sb.append("import javax.persistence.GeneratedValue;\n");
		sb.append("import javax.persistence.Id;\n");
		sb.append("import javax.persistence.OneToMany;\n");
		sb.append("import javax.persistence.Table;\n");
		sb.append("import javax.persistence.JoinColumn;\nimport javax.persistence.OneToOne;\nimport javax.persistence.ManyToOne;\nimport org.hibernate.annotations.Fetch;\nimport org.hibernate.annotations.FetchMode;\n\n");
		sb.append("import org.hibernate.annotations.Cache;\n");
		sb.append("import org.hibernate.annotations.CacheConcurrencyStrategy;\n");
		sb.append("import org.hibernate.annotations.GenericGenerator;\n\n");

		// 类注释
		sb.append("/**\n");
		sb.append(" * <p>" + entry.tableCmt + "实体类</p>\n");
		sb.append(" * @author " + Constant.author + "\n");
		sb.append(" * @time " + DateUtil.toTime(System.currentTimeMillis()) + "\n");
		sb.append(" * @version " + Constant.version + "\n");
		sb.append(" */\n");
		sb.append("@SuppressWarnings(\"serial\")\n");
		sb.append("@Entity\n");
		sb.append("@Table(name = \"" + entry.tableName + "\")\n");
		sb.append("@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE, region=\"hibernateEntryCache\")\n");
		sb.append("public class " + entry.name + " implements Serializable {\n");
		sb.append("\n");

		// 生成属性
		for (Property property : entry.getPropertyList()) {
			if (property.comment != null) sb.append("\t/** " + property.comment + " */\n");
			// if(property.notnull && !property.name.equals("id"))
			// sb.append("\t@NotNull(message=\""+ property.getSimpleCmt()
			// +"不能为空\")\n");
			// if(property.type.equals("String") && !property.name.equals("id"))
			// sb.append("\t@Size(max="+ property.size +", message=\""+
			// property.getSimpleCmt() +"不能超过"+ property.size +"个字符\")\n");
			// if(property.tType.equals("tinyint")) sb.append("\t@Max(value=127,
			// message=\""+ property.getSimpleCmt()
			// +"不能超过127\")\n\t@Min(value=-128, message=\""+
			// property.getSimpleCmt() +"不能小于-128\")\n");
			sb.append("\tprivate " + property.type + " " + property.name + ";\n\n");

			// in8n
			if (property.isI18n) {
				i18nStr.append("\n#" + entry.name + "\n");
				for (Iterator itI18n = property.i18nMap.entrySet().iterator(); itI18n.hasNext();) {
					Map.Entry entryMap = (Map.Entry) itI18n.next();
					String key = entry.name + "." + property.name + "_" + entryMap.getKey();
					String val = entryMap.getValue().toString();
					i18nStr.append(key + "=" + StringUtil.toUnicode(val) + "\n");
				}
			}
		}

		sb.append("\n\n");

		// 生成get,set方法
		for (Iterator it = entry.getPropertyList().iterator(); it.hasNext();) {
			Property property = (Property) it.next();

			// get 方法
			sb.append("\t/**\n");
			sb.append("\t * <p>获取" + property.getSimpleCmt() + "</p>\n");
			if (property.comment2 != null) sb.append("\t * <p>" + property.comment2 + "</p>\n");
			if (property.comment != null) sb.append("\t * @return " + property.type + " " + property.comment + "\n");
			else sb.append("\t * @return " + property.type + " " + property.name + "\n");
			sb.append("\t * @author " + Constant.author + "\n");
			sb.append("\t * @time " + DateUtil.toTime(System.currentTimeMillis()) + "\n");
			sb.append("\t * @version " + Constant.version + "\n");
			sb.append("\t */\n");

			if (property.name.equals("id")) {
				if (property.type.equals("String")) {
					sb.append("\t@Id\n\t@GenericGenerator(name=\"idGenerator\", strategy=\"com.yxlisv.util.hibernate.UUIDGenerator\")\n\t@GeneratedValue(generator=\"idGenerator\")\n");
				} else {
					sb.append("\t@Id\n\t@GeneratedValue\n");
				}
			}

			if (property.isClass()) {
				String lazyProp = "";
				if (Constant.hibernateLazy) lazyProp = ", fetch=FetchType.LAZY";
				if (property.isMainClass()) {
					sb.append("\t@OneToOne(cascade={javax.persistence.CascadeType.ALL}" + lazyProp + ", optional=true)\n");
				}
				else {
					sb.append("\t@ManyToOne(cascade={javax.persistence.CascadeType.REFRESH}" + lazyProp + ", optional=true)\n");
				}
				if (!Constant.hibernateLazy) {
					sb.append("\t@Fetch(FetchMode.JOIN)\n");
				}
				sb.append("\t@JoinColumn(name=\"" + property.tName + "\")\n");

				// 添加OneToMany关系
				if (!property.isBigString()) {
					oneToManyBuilder.put(property.type, entry.name, entry.getSimpleCmt());
				}
			} else {
				if (property.tType.toLowerCase().equals("char")) {
					sb.append("\t@Column(name=\"" + property.tName + "\", length=" + property.size + ", columnDefinition=\"char(" + property.size + ")\")\n");
				} else if (property.type.equals("String")) {
					sb.append("\t@Column(name=\"" + property.tName + "\", length=" + property.size + ")\n");
				} else {
					sb.append("\t@Column(name=\"" + property.tName + "\")\n");
				}
			}

			sb.append("\tpublic " + property.type + " get" + StringUtil.toUpper4FirstWord(property.name) + "() {\n");
			sb.append("\t\treturn " + property.name + ";\n");
			sb.append("\t}\n\n");

			// set 方法
			sb.append("\t/**\n");
			sb.append("\t * <p>设置" + property.getSimpleCmt() + "</p>\n");
			if (property.comment2 != null) sb.append("\t * <p>" + property.comment2 + "</p>\n");
			if (property.comment != null) sb.append("\t * @param " + property.name + " " + property.comment + "\n");
			else sb.append("\t * @param " + property.name + " " + property.name + "\n");
			sb.append("\t * @author " + Constant.author + "\n");
			sb.append("\t * @time " + DateUtil.toTime(System.currentTimeMillis()) + "\n");
			sb.append("\t * @version " + Constant.version + "\n");
			sb.append("\t */\n");
			sb.append("\tpublic void set" + StringUtil.toUpper4FirstWord(property.name) + "(" + property.type + " " + property.name + ") {\n");
			sb.append("\t\tthis." + property.name + " = " + property.name + ";\n");
			sb.append("\t}\n\n\n");
		}

		// 类结束
		sb.append("}");

		// System.out.println(sb.toString());
		try {
			FileUtil.write(getFileDir(Constant.entryPackage, Constant.baseDir), entry.name + ".java", sb.toString());
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
	public static void buildI18n(String baseDir) {

		try {
			FileUtil.write(baseDir, "dataDictionary.properties", i18nStr.toString());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}