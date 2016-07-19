package com.yxlisv.codebuild.control;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import com.yxlisv.codebuild.CodeBuilder;
import com.yxlisv.codebuild.Constant;
import com.yxlisv.codebuild.entry.Entry;
import com.yxlisv.util.date.DateUtil;
import com.yxlisv.util.file.FileUtil;
import com.yxlisv.util.string.StringUtil;

/**
 * control层生成器
 * @author john Local
 */
public class ControlBuilder extends CodeBuilder {

	// 实体
	private Entry entry;

	/**
	 * 构造实体生成器
	 * @param entry	实体
	 */
	public ControlBuilder(Entry entry) {
		this.entry = entry;
	}

	/**
	 * 生成文件
	 * @param fileDir 文件目录
	 * @autor yxl
	 */
	@Override
	public void build() {
		StringBuffer sb = new StringBuffer();
		for (ControlPackage controlPackage : Constant.controlPackages) {
			sb.delete(0, sb.length());
			// 根据package名称重新设置路径
			String packageName = controlPackage.packageName;

			// control 的默认访问路径
			String qzPath = (controlPackage.pathQz.equals("") ? "" : "/" + controlPackage.pathQz);// 前缀转换成path
			String defaultPath = qzPath + "/" + entry.getLowerName();
			String className = controlPackage.getClassName(entry.name);
			// 包
			sb.append("package " + packageName + ";\n\n");

			// import

			sb.append("import java.util.Map;\n\n");
			sb.append("import javax.annotation.Resource;\n\n");
			sb.append("import org.springframework.stereotype.Controller;\n");
			sb.append("import org.springframework.ui.ModelMap;\n");
			sb.append("import org.springframework.web.bind.annotation.PathVariable;\n");
			sb.append("import org.springframework.web.bind.annotation.RequestMapping;\n");
			sb.append("import org.springframework.web.bind.annotation.RequestMethod;\n\n");
			sb.append("import " + Constant.apiPackage + "." + entry.name + "API;\n");
			sb.append("import " + Constant.entryPackage + "." + entry.name + ";\n");
			if(!controlPackage.controlBasePackage.equals(controlPackage.packageName)) sb.append("import " + controlPackage.controlBasePackage + ".BaseControl;\n");
			sb.append("import com.yxlisv.util.data.Page;\n\n");

			// 类注释
			sb.append("/**\n");
			sb.append(" * <p>" + entry.getSimpleCmt() + "Control层</p>\n");
			sb.append(" * @author " + Constant.author + "\n");
			sb.append(" * @time " + DateUtil.toTime(System.currentTimeMillis()) + "\n");
			sb.append(" * @version " + Constant.version + "\n");
			sb.append(" */\n");

			if (controlPackage.pathQz.length() < 1) sb.append("@Controller\n");
			else sb.append("@Controller(\"" + controlPackage.getControlId(entry.name) + "\")\n");// 如果实体类的control层有带其他前缀，那么给这个bean设置一个id
			
			sb.append("public class " + className + " extends " + StringUtil.toUpper4FirstWord("BaseControl {\n\n"));

			// 注入service层
			sb.append("\t/** " + entry.getSimpleCmt() + "Service */\n");
			sb.append("\t@Resource\n");
			sb.append("\tprivate " + entry.name + "API " + entry.getLowerName() + "API;\n\n");

			// 分页查询（默认方法，包含模糊查询）
			sb.append("\t/**\n");
			sb.append("\t * <p>分页查询" + entry.getSimpleCmt() + "</p>\n");
			sb.append("\t * @param page 分页对象\n");
			sb.append("\t * @param modelMap 传递变量到view层\n");
			sb.append("\t * @return String 分页查询页面\n");
			sb.append("\t * @author " + Constant.author + "\n");
			sb.append("\t * @time " + DateUtil.toTime(System.currentTimeMillis()) + "\n");
			sb.append("\t * @version " + Constant.version + "\n");
			sb.append("\t */\n");
			sb.append("\t@SuppressWarnings({ \"rawtypes\" })\n");
			sb.append("\t@RequestMapping(value={\"" + defaultPath + ".html\", \"" + defaultPath + "_{pn}.html\"})\n");
			sb.append("\tpublic String page(Page page, ModelMap modelMap) {\n\n");
			sb.append("\t\tbindPage(page);// 绑定分页对象\n");
			sb.append("\t\tMap paramMap = loadQueryParam();// 加载查询条件\n");
			sb.append("\t\tmodelMap.put(\"paramMap\", paramMap);// 传递查询条件到view层\n\n");
			sb.append("\t\t// 执行分页查询\n");
			sb.append("\t\tpage = " + entry.getLowerName() + "API.page(page, paramMap);\n");
			sb.append("\t\tmodelMap.put(\"page\", page);\n");

			sb.append("\t\treturn \"" + qzPath + "/" + entry.getLowerName() + "/page\";\n");
			sb.append("\t}\n\n");

			// 查看
			sb.append("\t/**\n");
			sb.append("\t * <p>查看" + entry.getSimpleCmt() + "</p>\n");
			sb.append("\t * @param id " + entry.getSimpleCmt() + "ID\n");
			sb.append("\t * @param modelMap 传递变量到view层\n");
			sb.append("\t * @return String view页面\n");
			sb.append("\t * @author " + Constant.author + "\n");
			sb.append("\t * @time " + DateUtil.toTime(System.currentTimeMillis()) + "\n");
			sb.append("\t * @version " + Constant.version + "\n");
			sb.append("\t */\n");
			sb.append("\t@RequestMapping(value={\"" + defaultPath + "/{id}.html\"}, method=RequestMethod.GET)\n");
			sb.append("\tpublic String view(@PathVariable " + entry.getIdProperty().type + " id, ModelMap modelMap) {\n\n");
			sb.append("\t\t" + entry.name + " " + entry.getLowerName() + " = " + entry.getLowerName() + "API.get(id);\n");
			sb.append("\t\tmodelMap.put(\"" + entry.getLowerName() + "\", " + entry.getLowerName() + ");\n");
			sb.append("\t\treturn \"" + qzPath + "/" + entry.getLowerName() + "/view\";\n");
			sb.append("\t}\n\n");

			// 跳转到添加
			sb.append("\t/**\n");
			sb.append("\t * <p>跳转到添加页面</p>\n");
			sb.append("\t * @param modelMap 传递变量到view层\n");
			sb.append("\t * @return String 编辑页面\n");
			sb.append("\t * @author " + Constant.author + "\n");
			sb.append("\t * @time " + DateUtil.toTime(System.currentTimeMillis()) + "\n");
			sb.append("\t * @version " + Constant.version + "\n");
			sb.append("\t */\n");
			sb.append("\t@RequestMapping(value={\"" + defaultPath + "/add\"}, method=RequestMethod.GET)\n");
			sb.append("\tpublic String toAdd(ModelMap modelMap) {\n\n");

			sb.append("\t\treturn \"" + qzPath + "/" + entry.getLowerName() + "/edit\";\n");
			sb.append("\t}\n\n");
			
			// 添加
			sb.append("\t/**\n");
			sb.append("\t * <p>添加" + entry.getSimpleCmt() + "</p>\n");
			sb.append("\t * @param " + entry.getLowerName() + " " + entry.getSimpleCmt() + " 对象，自动注入参数\n");
			sb.append("\t * @return String 重定向到分页查询\n");
			sb.append("\t * @author " + Constant.author + "\n");
			sb.append("\t * @time " + DateUtil.toTime(System.currentTimeMillis()) + "\n");
			sb.append("\t * @version " + Constant.version + "\n");
			sb.append("\t */\n");
			sb.append("\t@RequestMapping(value=\"" + defaultPath + "/add\", method=RequestMethod.POST)\n");
			sb.append("\tpublic String add(" + entry.name + " " + entry.getLowerName() + ") {\n\n");
			sb.append("\t\tlogger.info(\"添加" + entry.getSimpleCmt() + "\");\n");

			sb.append("\t\t" + entry.getLowerName() + "API.save(" + entry.getLowerName() + ");\n\n");
			sb.append("\t\t//移除分页缓存和查询缓存\n");
			sb.append("\t\tremovePageCache();\n");
			sb.append("\t\tremoveQueryParamCache();\n");
			sb.append("\t\treturn redirect(\"" + qzPath + "/" + entry.getLowerName() + ".html" + "\");\n");
			sb.append("\t}\n\n");

			// 转到修改
			sb.append("\t/**\n");
			sb.append("\t * <p>跳转到修改页面</p>\n");
			sb.append("\t * @param id " + entry.getSimpleCmt() + "ID\n");
			sb.append("\t * @param modelMap 传递变量到编辑页面\n");
			sb.append("\t * @return String 编辑页面\n");
			sb.append("\t * @author " + Constant.author + "\n");
			sb.append("\t * @time " + DateUtil.toTime(System.currentTimeMillis()) + "\n");
			sb.append("\t * @version " + Constant.version + "\n");
			sb.append("\t */\n");
			sb.append("\t@RequestMapping(value=\"" + defaultPath + "/update/{id}\", method=RequestMethod.GET)\n");
			sb.append("\tpublic String toUpdate(@PathVariable " + entry.getIdProperty().type + " id, ModelMap modelMap) {\n\n");
			sb.append("\t\t" + entry.name + " " + entry.getLowerName() + " = " + entry.getLowerName() + "API.get(id);\n");
			sb.append("\t\tmodelMap.put(\"" + entry.getLowerName() + "\", " + entry.getLowerName() + ");\n");

			sb.append("\t\treturn \"" + qzPath + "/" + entry.getLowerName() + "/edit\";\n");
			sb.append("\t}\n\n");

			// 修改
			sb.append("\t/**\n");
			sb.append("\t * <p>修改" + entry.getSimpleCmt() + "</p>\n");
			sb.append("\t * @param " + entry.getLowerName() + " " + entry.getSimpleCmt() + "对象，自动注入要修改的参数\n");
			sb.append("\t * @return String 重定向到分页查询\n");
			sb.append("\t * @author " + Constant.author + "\n");
			sb.append("\t * @time " + DateUtil.toTime(System.currentTimeMillis()) + "\n");
			sb.append("\t * @version " + Constant.version + "\n");
			sb.append("\t */\n");
			sb.append("\t@RequestMapping(value=\"" + defaultPath + "/update\", method=RequestMethod.POST)\n");
			sb.append("\tpublic String update(" + entry.name + " " + entry.getLowerName() + ") {\n\n");
			sb.append("\t\tlogger.info(\"修改" + entry.getSimpleCmt() + "\");\n");

			sb.append("\t\t" + entry.getLowerName() + "API.update(" + entry.getLowerName() + ");\n");
			sb.append("\t\treturn redirect(\"" + qzPath + "/" + entry.getLowerName() + ".html" + "\");\n");
			sb.append("\t}\n\n");

			// 删除
			sb.append("\t/**\n");
			sb.append("\t * <p>删除" + entry.getSimpleCmt() + "</p>\n");
			sb.append("\t * @param id " + entry.getSimpleCmt() + "ID\n");
			sb.append("\t * @return String 重定向到分页查询\n");
			sb.append("\t * @author " + Constant.author + "\n");
			sb.append("\t * @time " + DateUtil.toTime(System.currentTimeMillis()) + "\n");
			sb.append("\t * @version " + Constant.version + "\n");
			sb.append("\t */\n");
			sb.append("\t@RequestMapping(\"" + defaultPath + "/delete/{id}\")\n");
			sb.append("\tpublic String delete(@PathVariable String id) {\n\n");
			sb.append("\t\tlogger.info(\"删除" + entry.getSimpleCmt() + "：\" + id);\n");
			sb.append("\t\tboolean status = " + entry.getLowerName() + "API.delete(id);\n");
			sb.append("\t\tif(status) logger.info(\"删除" + entry.getSimpleCmt() + "成功，id=\" + id);\n");
			sb.append("\t\treturn redirect(\"" + qzPath + "/" + entry.getLowerName() + ".html\");\n");
			sb.append("\t}\n\n");

			// 批量删除
			sb.append("\t/**\n");
			sb.append("\t * <p>批量删除" + entry.getSimpleCmt() + "</p>\n");
			sb.append("\t * @param id " + entry.getSimpleCmt() + "ID数组，传入多个id参数，自动组装为一个数组\n");
			sb.append("\t * @return String 重定向到分页查询\n");
			sb.append("\t * @author " + Constant.author + "\n");
			sb.append("\t * @time " + DateUtil.toTime(System.currentTimeMillis()) + "\n");
			sb.append("\t * @version " + Constant.version + "\n");
			sb.append("\t */\n");
			sb.append("\t@RequestMapping(\"" + defaultPath + "/delete\")\n");
			sb.append("\tpublic String delete(String[] id) {\n\n");
			sb.append("\t\tlogger.info(\"批量删除" + entry.getSimpleCmt() + "\");\n");
			sb.append("\t\tint count = " + entry.getLowerName() + "API.delete(\"" + entry.getIdProperty().name + "\", id);\n");
			sb.append("\t\tlogger.info(\"删除了\" + count + \"条记录\");\n");
			sb.append("\t\treturn redirect(\"" + qzPath + "/" + entry.getLowerName() + ".html\");\n");
			sb.append("\t}\n\n");
			
			
			//选择
			sb.append("\t/**\n");
			sb.append("\t * <p>选择"+ entry.getSimpleCmt() +"</p>\n");
			sb.append("\t * @return String 重定向到分页查询\n");
			sb.append("\t * @author " + Constant.author + "\n");
			sb.append("\t * @time " + DateUtil.toTime(System.currentTimeMillis()) + "\n");
			sb.append("\t * @version " + Constant.version + "\n");
			sb.append("\t */\n");
			sb.append("\t@RequestMapping(value={\""+ defaultPath +"/select.html\", \""+ defaultPath +"/select_{pn}.html\"})\n");
			sb.append("\tpublic String select(Page page, ModelMap modelMap) {\n\n");
			sb.append("\t\tMap paramMap = loadQueryParam();// 加载查询条件\n");
			sb.append("\t\tmodelMap.put(\"paramMap\", paramMap);// 传递查询条件到view层\n\n");
			sb.append("\t\t// 执行分页查询\n");
			sb.append("\t\tpage = " + entry.getLowerName() + "API.page(page, paramMap);\n");
			sb.append("\t\tmodelMap.put(\"page\", page);// 传递查询条件到view层\n\n");
			sb.append("\t\treturn \"/select/"+ entry.getLowerName() +"\";\n");
			sb.append("\t}\n");

			// 类结束
			sb.append("}");

			// System.out.println(sb.toString());
			try {
				FileUtil.write(getFileDir(packageName, Constant.baseDir), className + ".java", sb.toString());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * 生成父类
	 * @param fileDir 文件目录
	 * 
	 * @autor yxl
	 */
	public static void buildParentClass(List packages, String baseDir) {

		StringBuffer sb = new StringBuffer();
		for (Iterator it = packages.iterator(); it.hasNext();) {
			sb.delete(0, sb.length());
			ControlPackage controlPackage = (ControlPackage) it.next();

			String packageName = controlPackage.controlBasePackage;

			// 包
			sb.append("package " + packageName + ";\n\n");

			// import
			sb.append("import com.yxlisv.control.AbstractBaseControl;\n\n");

			// 类注释
			sb.append("/**\n");
			sb.append(" * <p>" + StringUtil.toUpper4FirstWord(StringUtil.toUpperBh(controlPackage.pathQz, "/", 1)) + "Control层父类</p>\n");
			sb.append(" * @author " + Constant.author + "\n");
			sb.append(" * @time " + DateUtil.toTime(System.currentTimeMillis()) + "\n");
			sb.append(" * @version " + Constant.version + "\n");
			sb.append(" */\n");
			sb.append("public class BaseControl extends AbstractBaseControl {");

			// 类结束
			sb.append("}");

			try {
				FileUtil.write(CodeBuilder.getFileDir(packageName, baseDir), "BaseControl.java", sb.toString());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}