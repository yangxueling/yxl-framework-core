package com.yxlisv.codebuild;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.yxlisv.codebuild.api.APIBuilder;
import com.yxlisv.codebuild.control.ControlBuilder;
import com.yxlisv.codebuild.control.ControlPackage;
import com.yxlisv.codebuild.dao.DaoBuilder;
import com.yxlisv.codebuild.db.IndexBuilder;
import com.yxlisv.codebuild.dubbo.DubboBuilder;
import com.yxlisv.codebuild.entry.Entry;
import com.yxlisv.codebuild.entry.EntryBuilder;
import com.yxlisv.codebuild.entry.Property;
import com.yxlisv.codebuild.i18n.I18nBuilder;
import com.yxlisv.codebuild.jsp.JspBuilder;
import com.yxlisv.codebuild.service.ServiceBuilder;
import com.yxlisv.codebuild.validation.ValidationBuilder;
import com.yxlisv.util.file.FilePathUtil;
import com.yxlisv.util.file.FileUtil;

/**
 * <pre>
 * 分析sql文件，生成代码(mysqldump 5.0 生成的mysql文件)
 * 
 * sql文件格式：
 * 1、表和字段命名规则：不同单词用下划线隔开 如：“par_type”
 * 2、表名和字段用单引号或者是"`"包含起来
 * 3、每个字段结束后，必须在后面加逗号 ","
 * 4、参考下面的例子
 * 
	CREATE 	TABLE `aitem_sort` (
	  `id` int(11) NOT NULL auto_increment,
	  `details_name` varchar(20) NOT NULL COMMENT '明细信息',
	  `type` int(11) NOT NULL COMMENT '类型(0资产,1负债)',
	  `par_type` int(11) NOT NULL COMMENT '父类型(1流动资产,2长期投资,3固定资产,4无形资产及其他资产,5流动负债,6长期负债,7所有者（或股东）权益)',
	  PRIMARY KEY  (`id`)
	) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='资产负债信息';
 * </pre>
 * 
 * @author 杨雪令
 * @time 2016年3月16日下午5:58:19
 * @version 1.0
 */
@SuppressWarnings({ "unchecked", "rawtypes" })
public class MysqlBulider {

	// 定义一个全局的记录器，通过LoggerFactory获取
	protected static Logger logger = LoggerFactory.getLogger(MysqlBulider.class);

	/**
	 * 正则表达式 http://www.cnblogs.com/deerchao/archive/2006/08/24/
	 * zhengzhe30fengzhongjiaocheng.html
	 * 
	 * 点的转义：. ==> u002E 美元符号的转义：$ ==> u0024 乘方符号的转义：^ ==> u005E 左大括号的转义：{ ==>
	 * u007B 左方括号的转义：[ ==> u005B 左圆括号的转义：( ==> u0028 竖线的转义：| ==> u007C 右圆括号的转义：)
	 * ==> u0029 星号的转义：* ==> u002A 加号的转义：+ ==> u002B 问号的转义：? ==> u003F 反斜杠的转义：
	 * ==> u005C 汉字：\u4e00-\u9fa5
	 */

	// 创建表的正则表达式
	private static Pattern tbPt = Pattern.compile("(\\s*create\\s+table\\s+[`']([a-zA-Z0-9_`]+)[`']\\s*\\u0028" + // CREATE
	// TABLE
	// `aitem_sort`
	// (
	// "([a-z\\d\\u0028\\u0029^_']+,)+" + //筛选属性
	"(\\s*[`'][a-zA-Z0-9_`]+[`']\\s+[^;]*,)+" + // 筛选属性
			"[^;]*comment\\s*=\\s*'([^']*)';" + // 表注释
			")");
	// 表的属性（字段）正则表达式
	private static Pattern tbpPt = Pattern.compile(
			"(\\s*[`']([a-zA-Z0-9_`]+)[`']\\s+([a-zA-Z]+)\\u0028?([0-9]*)\\u0029?[a-zA-Z\\s_'0-9]*(comment\\s+'([^']*)')?\\s*,)" // 筛选属性
	);

	/**
	 * 
	 * <p>
	 * 根据sql文件生成
	 * </p>
	 * 
	 * @param sqlFilePath
	 *            sql文件路径
	 * @author 杨雪令
	 * @time 2016年3月16日下午5:59:51
	 * @version 1.0
	 */
	public static void buildFromSqlFile(String sqlFilePath) {

		// 获取sql文件所在目录
		Constant.baseDir = FilePathUtil.getFileDir(sqlFilePath);
		Constant.baseDir += FilePathUtil.getFileName(sqlFilePath);

		// 读取文件内容到list中
		String sqlStr = "";
		try {
			sqlStr = FileUtil.read(sqlFilePath);
			System.out.println(sqlStr);
		} catch (IOException e) {
			logger.error("读取Sql文件出错", e);
		}
		
		//替换非法字符
		Matcher specCharMatcher = Pattern.compile("'[^']*'").matcher(sqlStr);
		while(specCharMatcher.find()){
			String str = specCharMatcher.group(0);
			String newStr = str.replaceAll(";", ",");
			sqlStr = sqlStr.replace(str, newStr);
		}
		System.out.println(sqlStr);

		// table create 语句 正则表达式解析器
		Matcher tbMatcher = tbPt.matcher(sqlStr);

		// 表的数量
		while (tbMatcher.find()) {
			Constant.tableCount++;

			// 表名
			String tableName = tbMatcher.group(2);
			// 表注释
			String tableCmt = tbMatcher.group(4);
			// 初始化实体
			Entry entry = new Entry(tableName, tableCmt);

			System.out.print("(" + tbMatcher.groupCount() + ")");
			System.out.print(tableName);
			System.out.print("[" + tableCmt + "]");

			// 从Sql语句中解析属性
			String pStr = tbMatcher.group(3);
			Matcher pMatcher = tbpPt.matcher(pStr);
			while (pMatcher.find()) {
				String pName = pMatcher.group(2);// 字段名称
				String pType = pMatcher.group(3);// 字段类型
				String pSize = pMatcher.group(4);// 字段长度
				String pCmt = pMatcher.group(6);// 字段注释
				boolean notnull = false;
				if (pMatcher.group(0).toLowerCase().contains("not null"))
					notnull = true;
				System.out.print("(" + pName + "#" + pType + "#" + pSize + "#" + pCmt + ")");
				entry.addProperty(pName, pType, pSize, pCmt, notnull);
			}
			System.out.println();
			CodeBuildCache.entryMap.put(entry.name, entry);// 缓存实体类
			CodeBuildCache.entryList.add(entry);// 缓存实体类
			CodeBuildCache.tableNameCache
					.add(entry.tableName.toUpperCase().replaceAll(Constant.entryNameIgnore.toUpperCase(), ""));// 缓存表名，去掉忽略的字符
		}

		// 判断属性是否是class
		for (Entry entry : CodeBuildCache.entryList) {
			for (Property property : entry.getPropertyList()) {
				if (property.isClass()) {
					property.name = property.getClassPname();
					property.type = property.getClassName();
				}
			}
		}

		// 检查大文本字段
		for (Entry entry : CodeBuildCache.entryList) {
			for (Property property : entry.getPropertyList()) {
				property.checkBigText();
			}
		}

		// 生成数据
		for (Map.Entry mentry : CodeBuildCache.entryMap.entrySet()) {
			Entry entry = (Entry) mentry.getValue();
			// 生成实体类
			EntryBuilder entryBuilder = new EntryBuilder(entry);
			entryBuilder.build();

			if (entry.name.equals("TextSmall"))
				continue;
			if (entry.name.equals("TextMiddle"))
				continue;
			if (entry.name.equals("TextBig"))
				continue;

			// 生成dao层
			DaoBuilder daoBuilder = new DaoBuilder(entry);
			daoBuilder.build();

			// 生成service层
			ServiceBuilder serviceBuilder = new ServiceBuilder(entry);
			serviceBuilder.build();

			// 生成api层
			APIBuilder apiBuilder = new APIBuilder(entry);
			apiBuilder.build();

			if (entry.isAffiliated)
				continue;// 附属表中断

			// 生成control层
			ControlBuilder controlBuilder = new ControlBuilder(entry);
			controlBuilder.build();
		}

		// 生成一对多关系
		if (Constant.hibernateOneToMany)
			EntryBuilder.oneToManyBuilder.build(CodeBuilder.getFileDir(Constant.entryPackage, Constant.baseDir));

		System.out.println("\n\n=========> 一共为 " + Constant.tableCount + " 张表生成了代码。");

		// 生成dubbo配置文件
		DubboBuilder.builderConfig();

		// 生成父类
		// 生成dao层
		DaoBuilder.buildParentClass(Constant.daoBasePackage, Constant.baseDir);
		ServiceBuilder.buildParentClass(Constant.serviceBasePackage, Constant.baseDir);
		ControlBuilder.buildParentClass(Constant.controlPackages, Constant.baseDir);

		// 生成I18N文件
		EntryBuilder.buildI18n(Constant.baseDir);
		// 生成实体类校验文件
		ValidationBuilder.buildEntryValidationI18n();
		ValidationBuilder.buildEntryValidation();
		// 生成实体类I18N配置文件
		I18nBuilder.buildEntryValidationI18n();
		// 生成数据库索引
		IndexBuilder.buildSimple();
		// 生成JSP页面
		JspBuilder.build();
		System.err.println(Property.waring);
		
		//检查表名和字段长度
		for (Entry entry : CodeBuildCache.entryList) {
			if(entry.tableName.length()>30) System.err.println("table ["+ entry.tableName +"] length more than 30");
			for(Property property : entry.getPropertyList()){
				if(property.tName.length()>30) System.err.println("table ["+ entry.tableName +"."+ property.tName +"] length more than 30");
			}
		}
	}

	/**
	 * @param args
	 * @autor yxl
	 */
	public static void main(String[] args) {
		String sqlFilePath = "F://codebuild/d3.sql";

		List<ControlPackage> packages = new ArrayList();

		// 测试，生成4份control代码
		ControlPackage controlPackage = new ControlPackage();
		controlPackage.packageName = "com.yxlisv.admin.control";
		controlPackage.pathQz = "admin";
		packages.add(controlPackage);

		controlPackage = new ControlPackage();
		controlPackage.packageName = "com.yxlisv.teacher.control";
		controlPackage.pathQz = "teacher";
		packages.add(controlPackage);

		controlPackage = new ControlPackage();
		controlPackage.packageName = "com.yxlisv.student.control";
		controlPackage.pathQz = "student";
		packages.add(controlPackage);

		controlPackage = new ControlPackage();
		controlPackage.packageName = "com.yxlisv.control";
		packages.add(controlPackage);

		Constant.controlPackages = packages;
		MysqlBulider.buildFromSqlFile(sqlFilePath);
	}

}
