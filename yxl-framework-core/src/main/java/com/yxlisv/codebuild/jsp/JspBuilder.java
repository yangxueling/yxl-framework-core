package com.yxlisv.codebuild.jsp;

import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.yxlisv.codebuild.CodeBuildCache;
import com.yxlisv.codebuild.Constant;
import com.yxlisv.codebuild.control.ControlPackage;
import com.yxlisv.codebuild.entry.Entry;
import com.yxlisv.codebuild.entry.Property;
import com.yxlisv.util.file.FileUtil;
import com.yxlisv.util.math.NumberUtil;

/**
 * <p>
 * JSP相关资源生成器
 * </p>
 * 
 * @author 杨雪令
 * @time 2016年3月23日下午12:01:18
 * @version 1.0
 */
public class JspBuilder {

	public static void build() {
		buildIndexPage(Constant.controlPackages);
		for (ControlPackage controlPackage : Constant.controlPackages) {
			for (Entry entry : CodeBuildCache.entryList) {
				buildPage(controlPackage, entry);
			}
		}
	}

	/**
	 * 生成页面
	 * 
	 * @param cPackage
	 *            control包
	 * @author yxl
	 */
	@SuppressWarnings({ "unused", "rawtypes", "unchecked" })
	private static void buildPage(ControlPackage cPackage, Entry entry) {
		boolean itemCountOverflow = false;// 数量太多，溢出
		int maxItemCount = 2;// 最多允许几个
		int i = 0;// 计数用
		StringBuffer sb = new StringBuffer();

		Map<String, Property> propertyMap = new LinkedHashMap();
		for (Iterator it = entry.getPropertyList().iterator(); it.hasNext();) {
			Property property = (Property) it.next();
			if (property.name.equals("id")) continue;
			if (property.isMainClass()) continue;
			propertyMap.put(property.name, property);
		}
		for (Iterator it = entry.getPropertyList().iterator(); it.hasNext();) {
			Property property = (Property) it.next();
			if (property.isMainClass()) {
				Entry propEntry = CodeBuildCache.entryMap.get(property.type);
				if (propEntry == null) continue;
				for (Iterator it2 = propEntry.getPropertyList().iterator(); it2.hasNext();) {
					Property pp = (Property) it2.next();
					if (pp.name.equals("id")) continue;
					propertyMap.put(property.name + "." + pp.name, pp);
				}
			}
		}

		// page 页面
		// 只有一个父类，把它提出到左侧，生成一个select索引页面
		if (entry.onlyOneParentClass()) {
			Property property = entry.getFirstParentClassProperty();
			Entry propEntry = CodeBuildCache.entryMap.get(property.type);
			// 属性注释
			String pCmt = property.getSimpleCmt();
			if (!property.entry.equals(entry)) {// 附属类的属性
				Property prop = entry.findPropByAffiliated(property.entry);
				if (property.entry.getViewProp().equals(property.name)) pCmt = prop.getSimpleCmt();
			}

			sb.setLength(0);
			sb.append("<%@ page language=\"java\" import=\"java.util.*\" pageEncoding=\"UTF-8\"%>\n");
			sb.append("<%@ include file=\"/common/jsp/jstl.jsp\" %>\n");
			sb.append("<%@ include file=\"/common/jsp/Style_Resources.jsp\"%>\n");
			sb.append("<%@ include file=\"/common/jsp/Script_Resources.jsp\"%>\n");
			sb.append("<%@ include file=\"/common/jsp/validation.jsp\"%>\n");
			sb.append("<%@ include file=\"/common/jsp/datepicker.jsp\"%>\n");
			sb.append("<table class=\"searchContainer\" style=\"width: 100%;height:100%; border: 0;\" cellpadding=\"0\" cellspacing=\"1\">\n");
			sb.append("\t<tr>\n");
			sb.append("\t\t<td style=\"min-width: 10px; border-right: 1px #ccc solid; width: 145px;\">\n");
			sb.append("\t\t\t<table style=\"width: 100%;height:100%; border: 0;\" cellpadding=\"0\" cellspacing=\"0\">\n");
			sb.append("\t\t\t\t<tr>\n");
			sb.append("\t\t\t\t\t<td>\n");
			sb.append("\t\t\t\t\t\t<div class=\"leftSearchHeader\">\n");
			sb.append("\t\t\t\t\t\t\t<form action=\"${pageContext.request.contextPath }/" + (cPackage.pathQz.length() > 0 ? cPackage.pathQz + "/" : "") + entry.getLowerName() + ".html?pageName=Index\" method=\"post\">\n");
			sb.append("\t\t\t\t\t\t\t\t<input type=\"hidden\" name=\"" + property.name + ".id\" value=\"\">\n");
			sb.append("\t\t\t\t\t\t\t\t<input type=\"text\" placeholder=\"" + pCmt + "\" style=\"width: 112px; margin: 0;\" name=\"" + property.name + propEntry.getViewPropUp() + "\" value=\"<c:out value=\"${paramMap['" + property.name + propEntry.getViewPropUp() + "'] }\"/>\"/>\n");
			sb.append("\t\t\t\t\t\t\t\t<a class=\"mini-button\" style=\"margin-top: -6px; width: 25px; height: 24px;  margin: 0;\"><span class=\"mini-button-text icon-search submit\" style=\"margin-top: 2px; margin-left: -2px;\">&nbsp;</span></a>\n");
			sb.append("\t\t\t\t\t\t\t</form>\n");
			sb.append("\t\t\t\t\t\t</div>\n");
			sb.append("\t\t\t\t\t</td>\n");
			sb.append("\t\t\t\t</tr>\n");
			sb.append("\t\t\t\t<tr>\n");
			sb.append("\t\t\t\t\t<td style=\"vertical-align: top; min-width: 10px; border-right: 0;\">\n");
			sb.append("\t\t\t\t\t\t<div class=\"leftSearchDiv autoHeight\" style=\"width:145px; height:415px; overflow-x:auto; overflow-y: auto;\">\n");
			sb.append("\t\t\t\t\t\t\t<table border=\"0\" cellpadding=\"0\" cellspacing=\"0\" class=\"categorys\" style=\"width: 100%\">\n");
			sb.append("\t\t\t\t\t\t\t\t<tr>\n");
			sb.append("\t\t\t\t\t\t\t\t\t<td class=\"itemtd <c:if test=\"${fn:length(paramMap['" + property.name + ".id'])<1 }\">current</c:if>\">\n");
			sb.append("\t\t\t\t\t\t\t\t\t\t<input type=\"hidden\" class=\"dataIpt\" target=\"" + property.name + "Id\" name=\"" + property.name + ".id\" value=\"\">\n");
			sb.append("\t\t\t\t\t\t\t\t\t\t<p><span>全部</span></p>\n");
			sb.append("\t\t\t\t\t\t\t\t\t</td>\n");
			sb.append("\t\t\t\t\t\t\t\t</tr>\n");
			sb.append("\t\t\t\t\t\t\t\t<c:forEach items=\"${" + property.name + "List}\" var=\"" + property.name + "\">\n");
			sb.append("\t\t\t\t\t\t\t\t\t<tr>\n");
			sb.append("\t\t\t\t\t\t\t\t\t\t<td class=\"itemtd <c:if test=\"${paramMap['" + property.name + ".id']==" + property.name + ".id }\">current</c:if>\">\n");
			sb.append("\t\t\t\t\t\t\t\t\t\t\t<input type=\"hidden\" class=\"dataIpt\" target=\"" + property.name + "Id\" name=\"" + property.name + ".id\" value=\"<c:out value=\"${" + property.name + ".id }\"/>\">\n");
			sb.append("\t\t\t\t\t\t\t\t\t\t\t<p><span><c:out value=\"${" + property.name + "." + propEntry.getViewProp() + " }\"/></span></p>\n");
			sb.append("\t\t\t\t\t\t\t\t\t\t</td>\n");
			sb.append("\t\t\t\t\t\t\t\t\t</tr>\n");
			sb.append("\t\t\t\t\t\t\t\t</c:forEach>\n");
			sb.append("\t\t\t\t\t\t\t</table>\n");
			sb.append("\t\t\t\t\t\t</div>\n");
			sb.append("\t\t\t\t\t</td>\n");
			sb.append("\t\t\t\t</tr>\n");
			sb.append("\t\t\t</table>\n");
			sb.append("\t\t</td>\n");
			sb.append("\t\t<td style=\"vertical-align: top;\">\n");
			sb.append("\t\t\t<div class=\"pageResult\"><%@ include file=\"page.jsp\" %></div>\n");
			sb.append("\t\t</td>\n");
			sb.append("\t</tr>\n");
			sb.append("</table>\n");

			try {
				FileUtil.write(Constant.baseDir + "/jsp/" + cPackage.pathQz + "/" + entry.getLowerName() + "/", "pageIndex.jsp", sb.toString());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		sb.setLength(0);
		sb.append("<%@ page language=\"java\" import=\"java.util.*\" pageEncoding=\"UTF-8\"%>\n");
		sb.append("<%@ include file=\"/common/jsp/jstl.jsp\" %>\n");
		if (!entry.onlyOneParentClass()) {
			// sb.append("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0
			// Transitional//EN\"
			// \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">\n");
			// sb.append("<html>\n");
			// sb.append("<head>\n");
			// sb.append("<meta http-equiv=\"Content-Type\" content=\"text/html;
			// charset=utf-8\" />\n");
			// sb.append("<title>"+ entry.getSimpleCmt() +"列表-<fmt:message
			// key=\"info.project.name\"/></title>\n");
			sb.append("<%@ include file=\"/common/jsp/Style_Resources.jsp\"%>\n");
			sb.append("<%@ include file=\"/common/jsp/Script_Resources.jsp\"%>\n");
			sb.append("<%@ include file=\"/common/jsp/validation.jsp\"%>\n");
			sb.append("<%@ include file=\"/common/jsp/datepicker.jsp\"%>\n");
			// sb.append("</head>\n");
			// sb.append("<body>\n");
		}
		sb.append("<div id=\"container\">\n");
		sb.append("\t<%--工具栏开始 --%>\n");
		sb.append("\t<div class=\"tool\">\n");
		sb.append("\t\t<p><a class=\"ajaxLink\" onSuccess=\"openWindow\" title=\"添加" + entry.getSimpleCmt() + "\" url=\"${pageContext.request.contextPath }/" + (cPackage.pathQz.length() > 0 ? cPackage.pathQz + "/" : "") + entry.getLowerName() + "/add\" method=\"get\"><strong class=\"add\"></strong><span>新增</span></a></p>\n");
		sb.append("\t\t<p><a href=\"javascript:void(0);\" url=\"${pageContext.request.contextPath }/" + (cPackage.pathQz.length() > 0 ? cPackage.pathQz + "/" : "") + entry.getLowerName() + "/delete\"><strong class=\"remove\"></strong><span>删除</span></a></p>\n");
		sb.append("\t</div>\n");
		sb.append("\t<%--工具栏结束 --%>\n\n");
		sb.append("\t<%--搜索栏开始 --%>\n");
		sb.append("\t<div class=\"searchBox\">\n");
		if (entry.onlyOneParentClass()) sb.append("\t<form class=\"ajaxForm searchForm\" waitDiv=\"查询中，请稍等...\" targetDiv=\"pageResult\" action=\"${pageContext.request.contextPath }/" + (cPackage.pathQz.length() > 0 ? cPackage.pathQz + "/" : "") + entry.getLowerName() + ".html\" method=\"post\">\n");
		else sb.append("\t<form class=\"searchForm\" action=\"${pageContext.request.contextPath }/" + (cPackage.pathQz.length() > 0 ? cPackage.pathQz + "/" : "") + entry.getLowerName() + ".html\" method=\"post\">\n");
		// 只有一个父类
		if (entry.onlyOneParentClass()) {
			Property property = entry.getFirstParentClassProperty();
			sb.append("\t<input type=\"hidden\" class=\"" + property.name + "Id\" name=\"" + property.name + ".id\" value=\"<c:out value=\"${paramMap['" + property.name + ".id'] }\"/>\">\n");
		}
		sb.append("\t\t<input type=\"hidden\" name=\"pn\" value=\"${page.pn }\"/>\n");
		sb.append("\t\t<input type=\"hidden\" name=\"orderBy\" value=\"${page.orderBy }\" />\n");
		sb.append("\t\t<ul class=\"wrapfix\">\n");

		i = 0;// 计数用
		itemCountOverflow = false;// 数量太多，溢出
		maxItemCount = 5;// 最多允许几个
		for (Map.Entry mentry : propertyMap.entrySet()) {
			Property property = (Property) mentry.getValue();
			if (property.name.equals("id")) continue;
			if (property.isMainClass()) continue;
			if (NumberUtil.parseInt(property.size) > 100) continue;
			if (i == maxItemCount) {
				itemCountOverflow = true;
				sb.append("\t\t\t<%--\n");
			}
			if (entry.onlyOneParentClass() && property.isClass() && !property.isMainClass()) sb.append("\t\t\t<%--\n");// 如果当前类只有一个类属性，那么这个属性就是，已经提取到了左侧，先注释掉
			sb.append("\t\t\t<li>\n");
			// sb.append("\t\t\t\t<label class=\"searchItemLabel\">"+ pCmt
			// +"：</label>\n");
			// 属性名称
			String pName = mentry.getKey().toString();

			// 属性注释
			String pCmt = property.getSimpleCmt();
			if (!property.entry.equals(entry)) {// 附属类的属性
				Property prop = entry.findPropByAffiliated(property.entry);
				if (property.entry.getViewProp().equals(property.name)) pCmt = prop.getSimpleCmt();
			}

			// 如果是类
			if (property.isClass() && !property.isMainClass()) {
				Entry propEntry = CodeBuildCache.entryMap.get(property.type);
				String onlyOneParentClassCode = "";// 是否只有一个父类属性
				if (propEntry.onlyOneParentClass()) onlyOneParentClassCode = "?pageName=Index";
				sb.append("\t\t\t\t<div class=\"f7 selectIpt\" style=\"width:105px; height:24px;\" onmouseover=\"onSelectMouseover(this);\" onmouseout=\"onSelectMouseout(this);\">\n");
				sb.append("\t\t\t\t\t<input type=\"text\" placeholder=\"" + pCmt + "\" name=\"" + pName + "." + propEntry.getViewProp() + "\" class=\"input4 selectName " + property.name + propEntry.getViewPropUp() + "\" value=\"<c:out value=\"${paramMap['" + pName + "." + propEntry.getViewProp() + "'] }\"/>\"/>\n");
				sb.append("\t\t\t\t\t<strong class=\"clearSelect\" onclick=\"onSelectClear(this);\" style=\"display: none;\"></strong>\n");
				sb.append("\t\t\t\t\t<span class=\"p_hov ajaxLink\" padding=\"0\" nobutton=\"true\" href=\"${pageContext.request.contextPath }/" + (cPackage.pathQz.length() > 0 ? cPackage.pathQz + "/" : "") + propEntry.getLowerName() + "/select.html" + onlyOneParentClassCode + "\" title=\"选择" + pCmt + "\" method=\"get\" onSuccess=\"openWindow\"></span>\n");
				sb.append("\t\t\t\t</div>\n");
				i++;
			} else if (property.isTime()) {
				sb.append("\t\t\t\t&nbsp;&nbsp;" + pCmt + "：<input type=\"text\" placeholder=\"开始时间\" name=\"" + pName + "_S\" class=\"Wdate\" onClick=\"WdatePicker()\" onFocus=\"WdatePicker()\" readonly=\"readonly\" value=\"<c:out value=\"${paramMap['" + pName + "_S'] }\"/>\"/>- <input type=\"text\" placeholder=\"结束时间\" name=\"" + pName + "_E\" class=\"Wdate\" onClick=\"WdatePicker()\" onFocus=\"WdatePicker()\" readonly=\"readonly\" value=\"<c:out value=\"${paramMap['" + pName + "_E'] }\"/>\"/>\n");
				i++;
				i++;
			} else if (property.isI18n) {
				sb.append("\t\t\t\t<select name=\"" + pName + "\">\n");
				sb.append("\t\t\t\t\t<option value=\"\">" + pCmt + "</option>\n");
				for (Iterator itI18n = property.i18nMap.entrySet().iterator(); itI18n.hasNext();) {
					Map.Entry entryMap = (Map.Entry) itI18n.next();
					sb.append("\t\t\t\t\t<option <c:if test=\"${paramMap['" + pName + "']=='" + entryMap.getKey() + "' }\"> selected=\"selected\"</c:if> value=\"" + entryMap.getKey() + "\"><fmt:message key=\"" + property.entry.name + "." + property.name + "_" + entryMap.getKey() + "\"/></option>\n");
				}
				sb.append("\t\t\t\t</select>\n");
				i++;
			} else {
				sb.append("\t\t\t\t<input type=\"text\" placeholder=\"" + pCmt + "\" name=\"" + pName + "\" value=\"<c:out value=\"${paramMap['" + pName + "'] }\"/>\"/>\n");
				i++;
			}
			sb.append("\t\t\t</li>\n");
			if (entry.onlyOneParentClass() && property.isClass() && !property.isMainClass()) sb.append("\t\t\t--%>\n");// 如果当前类只有一个类属性，那么这个属性就是，已经提取到了左侧，先注释掉
		}
		if (itemCountOverflow) sb.append("\t\t\t--%>\n");
		sb.append("\t\t\t<li class=\"btns\">\n");
		sb.append("\t\t\t\t<a href=\"javascript:void(0);\" class=\"mini-button importantBtn\"><span class=\"mini-button-text submit\">查询</span></a>\n");
		sb.append("\t\t\t\t<a href=\"javascript:void(0);\" class=\"mini-button btnClear\"><span class=\"mini-button-low-text\">清除</span></a>\n");
		sb.append("\t\t\t</li>\n");
		sb.append("\t\t</ul>\n");
		sb.append("\t</form>\n");
		sb.append("\t</div>\n");
		sb.append("\t<%--搜索栏结束 --%>\n\n");

		sb.append("\t<div class=\"searchResult\">\n");
		sb.append("\t\t<table width=\"100%\" border=\"0\" cellspacing=\"0\" cellpadding=\"0\" class=\"table_white\">\n");
		sb.append("\t\t\t<thead>\n");
		sb.append("\t\t\t\t<tr>\n");
		sb.append("\t\t\t\t\t<th width=\"30\"><input class=\"checkAll checkbox\" type=\"checkbox\"/></th>\n");

		i = 0;// 计数用
		itemCountOverflow = false;// 数量太多，溢出
		maxItemCount = 8;// 最多允许几个
		for (Map.Entry mentry : propertyMap.entrySet()) {
			Property property = (Property) mentry.getValue();
			String pName = mentry.getKey().toString();
			// 属性注释
			String pCmt = property.getSimpleCmt();
			if (!property.entry.equals(entry)) {// 附属类的属性
				Property prop = entry.findPropByAffiliated(property.entry);
				if (property.entry.getViewProp().equals(property.name)) pCmt = prop.getSimpleCmt();
			}
			if (pName.equals("id")) continue;
			if (property.isMainClass()) continue;
			if (NumberUtil.parseInt(property.size) > 100) continue;
			if (i == maxItemCount) {
				itemCountOverflow = true;
				if (itemCountOverflow) sb.append("\t\t\t\t\t<%--\n");
			}
			if (property.isClass()) sb.append("\t\t\t\t\t<th><div class=\"sort${page.orderMap['" + pName + "."+ CodeBuildCache.entryMap.get(property.getClassName()).getIdProperty() +"']}\" name=\"" + pName + ".id\">" + pCmt + "</div></th>\n");
			else sb.append("\t\t\t\t\t<th><div class=\"sort${page.orderMap['" + pName + "']}\" name=\"" + pName + "\">" + pCmt + "</div></th>\n");
			i++;
		}
		if (itemCountOverflow) sb.append("\t\t\t\t\t--%>\n");
		sb.append("\t\t\t\t\t<th colspan=\"2\">操作</th>\n");
		sb.append("\t\t\t\t</tr>\n");
		sb.append("\t\t\t</thead>\n");
		sb.append("\t\t\t<tbody>\n");
		sb.append("\t\t\t\t<c:if test=\"${fn:length(page.result)<1 }\">\n");
		if (i > maxItemCount) i = maxItemCount;
		sb.append("\t\t\t\t\t<tr><td colspan=\"" + (i + 2) + "\">没有找到符合条件的数据</td><td class=\"endTd\"></td></tr>\n");
		sb.append("\t\t\t\t</c:if>\n");
		sb.append("\t\t\t\t<c:forEach items=\"${page.result}\" var=\"" + entry.getLowerName() + "\">\n");
		sb.append("\t\t\t\t\t<tr class=\"odd\">\n");
		// 复选框
		sb.append("\t\t\t\t\t\t<td class=\"center\"><input class=\"record checkbox\" type=\"checkbox\" value=\"<c:out value=\"${" + entry.getLowerName() + ".id }\"/>\"/></td>\n");

		i = 0;// 计数用
		itemCountOverflow = false;// 数量太多，溢出
		maxItemCount = 8;// 最多允许几个
		for (Map.Entry mentry : propertyMap.entrySet()) {
			Property property = (Property) mentry.getValue();
			if (property.name.equals("id")) continue;
			if (property.isMainClass()) continue;
			if (NumberUtil.parseInt(property.size) > 100) continue;

			if (i == maxItemCount) {
				itemCountOverflow = true;
				if (itemCountOverflow) sb.append("\t\t\t\t\t\t<%--\n");
			}

			// 属性名称
			String pName = mentry.getKey().toString();
			// 大字符字段分离时，关联的类的值
			if (property.isClass()) {
				Entry propEntry = CodeBuildCache.entryMap.get(property.type);
				if (propEntry == null) continue;
				pName += "." + propEntry.getViewProp();
			}

			// 如果是double，则格式化显示
			if (property.type.equals("double") || property.type.equals("float") || property.type.equals("decimal")) sb.append("\t\t\t\t\t\t<td><fmt:formatNumber pattern=\"#.##\" value=\"${" + entry.getLowerName() + "." + pName + " }\"/></td>\n");
			else if (property.isDate()) {
				sb.append("\t\t\t\t\t\t<td><fmt:formatDate pattern=\"yyyy-MM-dd HH:mm:ss\" type=\"date\" value=\"${" + entry.getLowerName() + "." + pName + " }\"/></td>\n");
			} else if (property.isTime()) {
				sb.append("\t\t\t\t\t\t<td><fmt:formatDate pattern=\"yyyy-MM-dd HH:mm:ss\" value=\"${" + entry.getLowerName() + "." + pName + " }\"/></td>\n");
			} else if (property.isI18n) {
				sb.append("\t\t\t\t\t\t<td><fmt:message key=\"" + property.entry.name + "." + property.name + "_${" + entry.getLowerName() + "." + pName + " }\"/></td>\n");
			} else if (property.type.equals("String")) {
				sb.append("\t\t\t\t\t\t<td>");
				if (Integer.parseInt(property.size) > 50) {
					sb.append("\n\t\t\t\t\t\t\t<c:out value=\"${fn:substring(" + entry.getLowerName() + "." + pName + ", 0, 20)}\"/>\n");
					sb.append("\t\t\t\t\t\t\t<c:if test=\"${fn:length(" + entry.getLowerName() + "." + pName + ")>20 }\">...</c:if>\n\t\t\t\t\t\t</td>\n");
				} else {
					sb.append("<c:out value=\"${" + entry.getLowerName() + "." + pName + " }\"/></td>\n");
				}
			} else {
				sb.append("\t\t\t\t\t\t<td><c:out value=\"${" + entry.getLowerName() + "." + pName + " }\"/></td>\n");
			}
			i++;
		}
		if (itemCountOverflow) sb.append("\t\t\t\t\t\t--%>\n");
		sb.append("\t\t\t\t\t\t<td class=\"oparate\">\n");
		sb.append("\t\t\t\t\t\t\t<a class=\"ajaxLink mini-button mini-button-plain\" onSuccess=\"openWindow\" nobutton=\"true\"  title=\"查看" + entry.getSimpleCmt() + "\" href=\"${pageContext.request.contextPath }/" + (cPackage.pathQz.length() > 0 ? cPackage.pathQz + "/" : "") + entry.getLowerName() + "/${" + entry.getLowerName() + ".id}.html\" method=\"get\"><span class=\"mini-button-text  mini-button-icon icon-zoomout\">查看</span></a>\n");
		sb.append("\t\t\t\t\t\t\t<a class=\"ajaxLink mini-button mini-button-plain\" onSuccess=\"openWindow\" title=\"修改" + entry.getSimpleCmt() + "\" href=\"${pageContext.request.contextPath }/" + (cPackage.pathQz.length() > 0 ? cPackage.pathQz + "/" : "") + entry.getLowerName() + "/update/${" + entry.getLowerName() + ".id}\" method=\"get\"><span class=\"mini-button-text  mini-button-icon icon-edit\">修改</span></a>\n");
		sb.append("\t\t\t\t\t\t\t<a class=\"btn_del mini-button mini-button-plain\" href=\"${pageContext.request.contextPath }/" + (cPackage.pathQz.length() > 0 ? cPackage.pathQz + "/" : "") + entry.getLowerName() + "/delete/${" + entry.getLowerName() + ".id}\"><span class=\"mini-button-text  mini-button-icon icon-remove\">删除</span></a>\n");
		sb.append("\t\t\t\t\t\t</td>\n");
		sb.append("\t\t\t\t\t\t<td class=\"endTd\"></td>\n");
		sb.append("\t\t\t\t\t</tr>\n");
		sb.append("\t\t\t\t</c:forEach>\n");
		sb.append("\t\t\t</tbody>\n");
		sb.append("\t\t</table>\n");
		if (!entry.onlyOneParentClass()) sb.append("\t\t<div class=\"pageDiv\"><%@ include file=\"/ui/components/page/default/default.jsp\"%></div>\n");
		else sb.append("\t\t<div class=\"pageDiv\"><%@ include file=\"/ui/components/page/ajax/default.jsp\"%></div>\n");
		sb.append("\t</div>\n");
		sb.append("</div>\n");
		if (!entry.onlyOneParentClass()) {
			// sb.append("</body>\n");
			// sb.append("</html>");
		}
		try {
			FileUtil.write(Constant.baseDir + "/jsp/" + cPackage.pathQz + "/" + entry.getLowerName() + "/", "page.jsp", sb.toString());
		} catch (IOException e1) {
			e1.printStackTrace();
		}

		// edit页面
		sb.setLength(0);
		sb.append("<%@ page language=\"java\" import=\"java.util.*\" pageEncoding=\"UTF-8\"%>\n");
		sb.append("<%@ include file=\"/common/jsp/jstl.jsp\" %>\n");
		sb.append("<%@ include file=\"/common/jsp/spring.jsp\" %>\n");
		// sb.append("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0
		// Transitional//EN\"
		// \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">\n");
		// sb.append("<html>\n");
		// sb.append("<head>\n");
		// sb.append("<meta http-equiv=\"Content-Type\" content=\"text/html;
		// charset=utf-8\" />\n");
		// sb.append("<title>${update!=null ? '修改' : '添加' }"+
		// entry.getSimpleCmt() +"-<fmt:message
		// key=\"info.project.name\"/></title>\n");
		// sb.append("</head>\n");
		// sb.append("<body>\n");
		sb.append("<form class=\"ajaxForm\" targetDiv=\"body\" action=\"${pageContext.request.contextPath }/" + (cPackage.pathQz.length() > 0 ? cPackage.pathQz + "/" : "") + entry.getLowerName() + "/${"+ entry.getLowerName() +"!=null ? 'update' : 'add' }\" method=\"post\">\n");
		sb.append("\t<c:if test=\"${"+ entry.getLowerName() +"!=null }\"><input type=\"hidden\" name=\""+ entry.getIdProperty().name +"\" value=\"${"+ entry.getLowerName() +"."+ entry.getIdProperty().name +" }\"/></c:if>\n");
		sb.append("\t<input type=\"hidden\" name=\"_r\" value=\"${_r }\"/>\n");
		sb.append("\t<fieldset style=\"margin: 10px 0 10px 0;\">\n");
		sb.append("\t<legend>" + entry.getSimpleCmt() + "信息</legend>\n");
		sb.append("\t<table class=\"marBottom10px\" style=\"min-width: 500px;\">\n");

		// 生成普通字段
		// 012 123
		// 345 456
		// 678 789
		i = 0;
		for (Map.Entry mentry : propertyMap.entrySet()) {
			Property property = (Property) mentry.getValue();
			if (property.name.equals("id")) continue;
			if (property.isProt()) continue;

			// 属性名称
			String pName = mentry.getKey().toString();
			if (property.isClass()) pName += ".id";

			// 属性注释
			String pCmt = property.getSimpleCmt();
			if (!property.entry.equals(entry)) {// 附属类的属性
				Property prop = entry.findPropByAffiliated(property.entry);
				if (property.entry.getViewProp().equals(property.name)) pCmt = prop.getSimpleCmt();
			}

			String requiredStyleCode = "<span class=\"red\">*</span>";// 非空样式
			String requiredClass = " required";// 非空class
			if (pName.contains(".") && !property.isClass()) {// 附属表字段可以为空
				requiredStyleCode = "";
				requiredClass = "";
			}

			if ((i + 1) % 3 == 1) sb.append("\t\t<tr>\n");

			// 如果是类
			if (property.isClass() && !property.isMainClass()) {
				i++;
				Entry propEntry = CodeBuildCache.entryMap.get(property.type);
				String onlyOneParentClassCode = "";// 是否只有一个父类属性
				if (propEntry.onlyOneParentClass()) onlyOneParentClassCode = "?pageName=Index";
				sb.append("\t\t\t<th>" + pCmt + "：" + requiredStyleCode + "</th>\n");
				sb.append("\t\t\t<td>\n");
				sb.append("\t\t\t\t<div class=\"f7 selectIpt\" style=\"width:105px; height:24px;\" onmouseover=\"onSelectMouseover(this);\" onmouseout=\"onSelectMouseout(this);\">\n");
				sb.append("\t\t\t\t\t<input type=\"hidden\" class=\"selectId " + property.name + "Id\" name=\"" + property.name + ".id\" value=\"${" + entry.getLowerName() + "." + property.name + ".id!=null?" + entry.getLowerName() + "." + property.name + ".id:paramMap['" + property.name + ".id'] }\" />\n");
				sb.append("\t\t\t\t\t<input type=\"text\" readonly=\"readonly\" autocomplete=\"off\" placeholder=\"" + pCmt + "\" name=\"" + property.name + "." + propEntry.getViewProp() + "\" class=\"input4 required ajaxLink selectName " + property.name + propEntry.getViewPropUp() + "\" padding=\"0\" nobutton=\"true\" onStart=\"startLoading\" href=\"${pageContext.request.contextPath }/" + (cPackage.pathQz.length() > 0 ? cPackage.pathQz + "/" : "") + propEntry.getLowerName() + "/select.html"
					+ onlyOneParentClassCode + "\" title=\"选择" + pCmt + "\" method=\"get\" onSuccess=\"openWindow\" value=\"${" + entry.getLowerName() + "." + property.name + "." + propEntry.getViewProp() + "!=null?" + entry.getLowerName() + "." + property.name + "." + propEntry.getViewProp() + ":paramMap['" + property.name + "." + propEntry.getViewProp() + "'] }\" />\n");
				sb.append("\t\t\t\t\t<strong class=\"clearSelect\" onclick=\"onSelectClear(this);\" style=\"display: none;\"></strong>\n");
				sb.append("\t\t\t\t\t<span class=\"p_hov ajaxLink\" padding=\"0\" nobutton=\"true\" href=\"${pageContext.request.contextPath }/" + (cPackage.pathQz.length() > 0 ? cPackage.pathQz + "/" : "") + propEntry.getLowerName() + "/select.html" + onlyOneParentClassCode + "\" title=\"选择" + pCmt + "\" method=\"get\" onSuccess=\"openWindow\"></span>\n");
				sb.append("\t\t\t\t</div>\n");
				sb.append("\t\t\t</td>\n");
			} else if (property.isTime()) {
				i++;
				sb.append("\t\t\t<th>" + pCmt + "：" + requiredStyleCode + "</th>\n");
				sb.append("\t\t\t<td>\n");
				String formatDate = "time";
				if (property.isDate()) formatDate = "date";
				sb.append("\t\t\t\t<input type=\"text\" name=\"" + pName + "\" class=\"input4 Wdate" + requiredClass + "\" onClick=\"WdatePicker()\" readonly=\"readonly\" value=\"<fmt:formatDate pattern=\"yyyy-MM-dd\" type=\"" + formatDate + "\" value=\"${" + entry.getLowerName() + "." + pName + " }\" />\"/>\n");
				sb.append("\t\t\t</td>\n");
			} else if (property.isI18n) {
				i++;
				sb.append("\t\t\t<th>" + pCmt + "：" + requiredStyleCode + "</th>\n");
				sb.append("\t\t\t<td>\n");
				sb.append("\t\t\t\t<select name=\"" + pName + "\">\n");
				for (Iterator itI18n = property.i18nMap.entrySet().iterator(); itI18n.hasNext();) {
					Map.Entry entryMap = (Map.Entry) itI18n.next();
					sb.append("\t\t\t\t\t<option <c:if test=\"${" + entry.getLowerName() + "." + pName + "=='" + entryMap.getKey() + "' || paramMap['" + pName + "']=='" + entryMap.getKey() + "'}\"> selected=\"selected\"</c:if> value=\"" + entryMap.getKey() + "\"><fmt:message key=\"" + property.entry.name + "." + property.name + "_" + entryMap.getKey() + "\"/></option>\n");
				}
				sb.append("\t\t\t\t</select>\n");
				sb.append("\t\t\t</td>\n");
			} else {
				// 普通属性：不是text，不是大字符串，长度小于100
				if (!property.tType.equals("text") && !property.isBigString() && com.yxlisv.util.math.NumberUtil.parseInt(property.size) < 100) {
					i++;
					sb.append("\t\t\t<th>" + pCmt + "：" + requiredStyleCode + "</th>\n");
					sb.append("\t\t\t<td>\n");
					sb.append("\t\t\t\t<input type=\"text\" name=\"" + pName + "\" class=\"input4" + requiredClass + "");

					// 为不同类型的自动添加验证
					if (property.type.equals("int") || property.type.equals("tinyint") || property.type.equals("bit")) sb.append(" int");
					else if (property.type.equals("double") || property.type.equals("float") || property.type.equals("decimal")) sb.append(" number");
					sb.append("\"");

					// 添加长度限制
					// 默认限制9位数字（int）
					String maxlength = "9";
					if (property.size != null && !property.size.equals("")) maxlength = property.size;
					sb.append(" maxlength=\"" + maxlength + "\"");

					if (property.tType.equals("tinyint")) sb.append(" min=\"-128\" max=\"127\"");

					// 如果是double，则格式化显示
					if (property.type.equals("double") || property.type.equals("float") || property.type.equals("decimal")) sb.append(" value=\"<fmt:formatNumber pattern=\"#.##\" value=\"${" + entry.getLowerName() + "." + pName + " }\"/>\"/>\n");
					else sb.append(" value=\"<c:out value=\"${" + entry.getLowerName() + "." + pName + " }\"/>\"/>\n");
					sb.append("\t\t\t</td>\n");
				}
			}
			if (i % 3 == 0) sb.append("\t\t</tr>\n");
		}
		if (i % 3 != 0) {
			int chazhi = i % 3;
			for (int j = 1; j <= (3 - chazhi); j++) {
				sb.append("\t\t\t<td></td><td></td>\n");
			}
			sb.append("\t\t</tr>\n");
		}

		// textarea
		for (Map.Entry mentry : propertyMap.entrySet()) {
			Property property = (Property) mentry.getValue();
			if (property.name.equals("id")) continue;
			if (property.isProt()) continue;

			// 属性名称
			String pName = mentry.getKey().toString();
			if (property.isBigString()) pName += ".val";
			else if (property.isClass()) pName += ".id";

			// 属性注释
			String pCmt = property.getSimpleCmt();
			if (!property.entry.equals(entry)) {// 附属类的属性
				Property prop = entry.findPropByAffiliated(property.entry);
				if (property.entry.getViewProp().equals(property.name)) pCmt = prop.getSimpleCmt();
			}

			// 如果数据库类型是 text，或者长度大于100就用textarea
			if (property.tType.equals("text") || property.isBigString() || (property.type.equals("String") && Integer.parseInt(property.size) >= 100)) {
				sb.append("\t\t<tr>\n");

				sb.append("\t\t\t<th>" + pCmt + "：</th>\n");
				sb.append("\t\t\t<td colspan=\"5\">");

				// 最大长度
				String maxlength = "5000";
				if (property.size != null && !property.size.equals("")) maxlength = property.size;
				// 行数
				int row = 5;
				// 大于500个字，行数设置为15行
				try {
					if (Integer.parseInt(maxlength) > 500) row = 10;
				} catch (Exception e) {}
				sb.append("<textarea style=\"width: 480px; height: 80px;\" name=\"" + pName + "\" maxlength=\"" + maxlength + "\"><c:out value=\"${" + entry.getLowerName() + "." + pName + " }\"/></textarea>");
				sb.append("</td>\n");
				sb.append("\t\t</tr>\n");
			}
		}

		sb.append("\t</table>\n");
		sb.append("\t</fieldset>\n");
		sb.append("</form>\n");
		// sb.append("</body>\n");
		// sb.append("</html>");
		String htmlCode = sb.toString();
		htmlCode = htmlCode.replaceAll("\\s*<tr>\\s*</tr>", "");
		try {
			FileUtil.write(Constant.baseDir + "/jsp/" + cPackage.pathQz + "/" + entry.getLowerName() + "/", "edit.jsp", htmlCode);
		} catch (IOException e) {
			e.printStackTrace();
		}

		// select页面
		// 只有一个父类，把它提出到左侧，生成一个select索引页面
		if (entry.onlyOneParentClass()) {
			Property property = entry.getFirstParentClassProperty();
			Entry propEntry = CodeBuildCache.entryMap.get(property.type);
			// 属性注释
			String pCmt = property.getSimpleCmt();
			if (!property.entry.equals(entry)) {// 附属类的属性
				Property prop = entry.findPropByAffiliated(property.entry);
				if (property.entry.getViewProp().equals(property.name)) pCmt = prop.getSimpleCmt();
			}

			sb.setLength(0);
			sb.append("<%@ page language=\"java\" import=\"java.util.*\" pageEncoding=\"UTF-8\"%>\n");
			sb.append("<%@ include file=\"/common/jsp/jstl.jsp\" %>\n");
			sb.append("<table class=\"searchContainer\" style=\"width: 100%;height:100%; border: 0;\" cellpadding=\"0\" cellspacing=\"0\">\n");
			sb.append("\t<tr>\n");
			sb.append("\t\t<td style=\"min-width: 10px; border-right: 1px #ccc solid; width: 145px;\">\n");
			sb.append("\t\t\t<table style=\"width: 100%;height:100%; border: 0;\" cellpadding=\"0\" cellspacing=\"0\">\n");
			sb.append("\t\t\t\t<tr>\n");
			sb.append("\t\t\t\t\t<td>\n");
			sb.append("\t\t\t\t\t\t<div class=\"leftSearchHeader\">\n");
			sb.append("\t\t\t\t\t\t\t<form class=\"ajaxForm\" action=\"${pageContext.request.contextPath }/" + (cPackage.pathQz.length() > 0 ? cPackage.pathQz + "/" : "") + entry.getLowerName() + "/select.html?pageName=Index\" targetDiv=\"pageResult\" method=\"post\">\n");
			sb.append("\t\t\t\t\t\t\t\t<input type=\"hidden\" name=\"" + property.name + ".id\" value=\"\">\n");
			sb.append("\t\t\t\t\t\t\t\t<input type=\"text\" placeholder=\"" + pCmt + "\" style=\"width: 112px; margin: 0;\" name=\"" + property.name + propEntry.getViewPropUp() + "\" value=\"<c:out value=\"${paramMap['" + property.name + propEntry.getViewPropUp() + "'] }\"/>\"/>\n");
			sb.append("\t\t\t\t\t\t\t\t<a class=\"mini-button\" style=\"margin-top: -6px; width: 25px; height: 24px;  margin: 0;\"><span class=\"mini-button-text icon-search submit\" style=\"margin-top: 2px; margin-left: -2px;\">&nbsp;</span></a>\n");
			sb.append("\t\t\t\t\t\t\t</form>\n");
			sb.append("\t\t\t\t\t\t</div>\n");
			sb.append("\t\t\t\t\t</td>\n");
			sb.append("\t\t\t\t</tr>\n");
			sb.append("\t\t\t\t<tr>\n");
			sb.append("\t\t\t\t\t<td style=\"vertical-align: top; min-width: 10px; border-right: 0;\">\n");
			sb.append("\t\t\t\t\t\t<div class=\"leftSearchDiv autoHeight\" style=\"width:145px; height:415px; overflow-x:auto; overflow-y: auto;\">\n");
			sb.append("\t\t\t\t\t\t\t<table border=\"0\" cellpadding=\"0\" cellspacing=\"0\" class=\"categorys\" style=\"width: 100%\">\n");
			sb.append("\t\t\t\t\t\t\t\t<tr>\n");
			sb.append("\t\t\t\t\t\t\t\t\t<td class=\"itemtd <c:if test=\"${fn:length(paramMap['" + property.name + ".id'])<1 }\">current</c:if>\">\n");
			sb.append("\t\t\t\t\t\t\t\t\t\t<input type=\"hidden\" class=\"dataIpt\" target=\"" + property.name + "Id\" name=\"" + property.name + ".id\" value=\"\">\n");
			sb.append("\t\t\t\t\t\t\t\t\t\t<p><span>全部</span></p>\n");
			sb.append("\t\t\t\t\t\t\t\t\t</td>\n");
			sb.append("\t\t\t\t\t\t\t\t</tr>\n");
			sb.append("\t\t\t\t\t\t\t\t<c:forEach items=\"${" + property.name + "List}\" var=\"" + property.name + "\">\n");
			sb.append("\t\t\t\t\t\t\t\t\t<tr>\n");
			sb.append("\t\t\t\t\t\t\t\t\t\t<td class=\"itemtd <c:if test=\"${paramMap['" + property.name + ".id']==" + property.name + ".id }\">current</c:if>\">\n");
			sb.append("\t\t\t\t\t\t\t\t\t\t\t<input type=\"hidden\" class=\"dataIpt\" target=\"" + property.name + "Id\" name=\"" + property.name + ".id\" value=\"<c:out value=\"${" + property.name + ".id }\"/>\">\n");
			sb.append("\t\t\t\t\t\t\t\t\t\t\t<p><span><c:out value=\"${" + property.name + "." + propEntry.getViewProp() + " }\"/></span></p>\n");
			sb.append("\t\t\t\t\t\t\t\t\t\t</td>\n");
			sb.append("\t\t\t\t\t\t\t\t\t</tr>\n");
			sb.append("\t\t\t\t\t\t\t\t</c:forEach>\n");
			sb.append("\t\t\t\t\t\t\t</table>\n");
			sb.append("\t\t\t\t\t\t</div>\n");
			sb.append("\t\t\t\t\t</td>\n");
			sb.append("\t\t\t\t</tr>\n");
			sb.append("\t\t\t</table>\n");
			sb.append("\t\t</td>\n");
			sb.append("\t\t<td style=\"vertical-align: top;\">\n");
			sb.append("\t\t\t<div class=\"pageResult\"><%@ include file=\"" + entry.getLowerName() + ".jsp\" %></div>\n");
			sb.append("\t\t</td>\n");
			sb.append("\t</tr>\n");
			sb.append("</table>\n");

			try {
				FileUtil.write(Constant.baseDir + "/jsp/select/", entry.getLowerName() + "Index.jsp", sb.toString());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		sb.setLength(0);
		sb.append("<%@ page language=\"java\" import=\"java.util.*\" pageEncoding=\"UTF-8\"%>\n");
		sb.append("<%@ include file=\"/common/jsp/jstl.jsp\" %>\n");
		sb.append("<div class=\"selectSearchBox\">\n");
		sb.append("\t<div class=\"searchBox\">\n");
		sb.append("\t\t<form class=\"ajaxForm searchForm\" waitStr=\"查询中，请稍等...\" targetDiv=\"pageResult\" action=\"${lastUrl }\" method=\"post\">\n");
		// 只有一个父类
		if (entry.onlyOneParentClass()) {
			Property property = entry.getFirstParentClassProperty();
			Entry propEntry = CodeBuildCache.entryMap.get(property.type);
			sb.append("\t\t\t<input type=\"hidden\" class=\"" + property.name + "Id\" name=\"" + property.name + ".id\" value=\"<c:out value=\"${paramMap['" + property.name + ".id'] }\"/>\">\n");
		}
		sb.append("\t\t\t<ul class=\"wrapfix\">\n");
		i = 0;// 计数用
		itemCountOverflow = false;// 数量太多，溢出
		maxItemCount = 3;// 最多允许几个
		for (Property property : entry.getPropertyList()) {
			String pName = property.name;
			// 属性注释
			String pCmt = property.getSimpleCmt();
			if (!property.entry.equals(entry)) {// 附属类的属性
				Property prop = entry.findPropByAffiliated(property.entry);
				if (property.entry.getViewProp().equals(property.name)) pCmt = prop.getSimpleCmt();
			}
			if (property.name.equals("id")) continue;
			if (property.isMainClass()) continue;
			if (entry.onlyOneParentClass() && property.isClass()) continue;
			if (NumberUtil.parseInt(property.size) > 100) continue;
			if (property.isProt()) continue;
			if (i == maxItemCount) {
				itemCountOverflow = true;
				if (itemCountOverflow) sb.append("\t\t\t\t<%--\n");
			}
			sb.append("\t\t\t\t<li>\n");
			// 如果是类
			if (property.isClass()) {
				Entry propEntry = CodeBuildCache.entryMap.get(property.type);
				if (!entry.onlyOneParentClass()) {
					sb.append("\t\t\t\t\t<input type=\"text\" placeholder=\"" + pCmt + "\" name=\"" + pName + "." + propEntry.getViewProp() + "\" value=\"<c:out value=\"${paramMap['" + pName + "." + propEntry.getViewProp() + "'] }\"/>\"/>\n");
					i++;
				}
			} else if (property.isTime()) {
				sb.append("\t\t\t\t\t&nbsp;&nbsp;" + pCmt + "：<input type=\"text\" placeholder=\"开始时间\" name=\"" + pName + "_S\" class=\"Wdate\" onClick=\"WdatePicker()\" onFocus=\"WdatePicker()\" readonly=\"readonly\" value=\"<c:out value=\"${paramMap['" + pName + "1'] }\"/>\"/>- <input type=\"text\" placeholder=\"结束时间\" name=\"" + pName + "_E\" class=\"Wdate\" onClick=\"WdatePicker()\" onFocus=\"WdatePicker()\" readonly=\"readonly\" value=\"<c:out value=\"${paramMap['" + pName + "2'] }\"/>\"/>\n");
				i++;
				i++;
			} else if (property.isI18n) {
				sb.append("\t\t\t\t\t<select name=\"" + pName + "\">\n");
				sb.append("\t\t\t\t\t\t<option value=\"\">" + pCmt + "</option>\n");
				for (Iterator itI18n = property.i18nMap.entrySet().iterator(); itI18n.hasNext();) {
					Map.Entry entryMap = (Map.Entry) itI18n.next();
					sb.append("\t\t\t\t\t\t<option <c:if test=\"${paramMap['" + pName + "']=='" + entryMap.getKey() + "' }\"> selected=\"selected\"</c:if> value=\"" + entryMap.getKey() + "\"><fmt:message key=\"" + property.entry.name + "." + property.name + "_" + entryMap.getKey() + "\"/></option>\n");
				}
				sb.append("\t\t\t\t\t</select>\n");
				i++;
			} else {
				sb.append("\t\t\t\t\t<input type=\"text\" placeholder=\"" + pCmt + "\" name=\"" + pName + "\" value=\"<c:out value=\"${paramMap['" + pName + "'] }\"/>\"/>\n");
				i++;
			}
			sb.append("\t\t\t\t</li>\n");
		}
		if (itemCountOverflow) sb.append("\t\t\t\t--%>\n");
		sb.append("\t\t\t\t<li class=\"btns\">\n");
		sb.append("\t\t\t\t\t<a href=\"javascript:void(0);\" class=\"mini-button\"><span class=\"mini-button-text submit\">查询</span></a>\n");
		sb.append("\t\t\t\t</li>\n");
		sb.append("\t\t\t</ul>\n");
		sb.append("\t\t</form>\n");
		sb.append("\t</div>\n");
		sb.append("\t\n\n");

		sb.append("\t<div class=\"selectResult\" onselectstart=\"return false;\">\n");
		sb.append("\t\t<table width=\"100%\" border=\"0\" cellspacing=\"0\" cellpadding=\"0\" class=\"itemlist table_white\">\n");
		sb.append("\t\t\t<thead>\n");
		sb.append("\t\t\t\t<tr>\n");
		i = 0;// 计数用
		itemCountOverflow = false;// 数量太多，溢出
		maxItemCount = 6;// 最多允许几个
		for (Property property : entry.getPropertyList()) {
			String pName = property.name;
			// 属性注释
			String pCmt = property.getSimpleCmt();
			if (!property.entry.equals(entry)) {// 附属类的属性
				Property prop = entry.findPropByAffiliated(property.entry);
				if (property.entry.getViewProp().equals(property.name)) pCmt = prop.getSimpleCmt();
			}
			if (pName.equals("id")) continue;
			if (property.isMainClass()) continue;
			if (NumberUtil.parseInt(property.size) > 100) continue;
			if (i == maxItemCount) {
				itemCountOverflow = true;
				if (itemCountOverflow) sb.append("\t\t\t\t\t<%--\n");
			}
			if (property.isClass()) sb.append("\t\t\t\t\t<th>" + pCmt + "</th>\n");
			else sb.append("\t\t\t\t\t<th>" + pCmt + "</th>\n");
			i++;
		}
		if (itemCountOverflow) sb.append("\t\t\t\t\t--%>\n");
		sb.append("\t\t\t\t\t<th class=\"oparate\"></th>\n");
		sb.append("\t\t\t\t</tr>\n");
		sb.append("\t\t\t</thead>\n");
		sb.append("\t\t\t<tbody>\n");
		sb.append("\t\t\t\t<c:if test=\"${fn:length(page.result)<1 }\">\n");
		if (i > maxItemCount) i = maxItemCount;
		sb.append("\t\t\t\t\t<tr><td colspan=\"" + (i + 1) + "\">没有找到符合条件的数据</td></tr>\n");
		sb.append("\t\t\t\t</c:if>\n");
		sb.append("\t\t\t\t<c:forEach items=\"${page.result}\" var=\"" + entry.getLowerName() + "\">\n");
		sb.append("\t\t\t\t\t<tr class=\"dataRow\">\n");

		for (Property property : entry.getPropertyList()) {
			if (property.isClass()) continue;
			if (NumberUtil.parseInt(property.size) > 5000) continue;
			sb.append("\t\t\t\t\t\t<input type=\"hidden\" class=\"dataIpt\" name=\"" + entry.getLowerName() + property.getUpName() + "\" value=\"<c:out value=\"${" + entry.getLowerName() + "." + property.name + " }\"/>\">\n");
		}

		i = 0;// 计数用
		itemCountOverflow = false;// 数量太多，溢出
		maxItemCount = 6;// 最多允许几个
		for (Property property : entry.getPropertyList()) {
			String pName = property.name;
			if (property.name.equals("id")) continue;
			if (property.isMainClass()) continue;
			if (NumberUtil.parseInt(property.size) > 100) continue;

			if (i == maxItemCount) {
				itemCountOverflow = true;
				if (itemCountOverflow) sb.append("\t\t\t\t\t\t<%--\n");
			}

			// 大字符字段分离时，关联的类的值
			if (property.isClass()) {
				Entry propEntry = CodeBuildCache.entryMap.get(property.type);
				if (propEntry == null) continue;
				pName += "." + propEntry.getViewProp();
			}

			// 如果是double，则格式化显示
			if (property.type.equals("double") || property.type.equals("float") || property.type.equals("decimal")) sb.append("\t\t\t\t\t\t<td><fmt:formatNumber pattern=\"#.##\" value=\"${" + entry.getLowerName() + "." + pName + " }\"/></td>\n");
			else if (property.isDate()) {
				sb.append("\t\t\t\t\t\t<td><fmt:formatDate pattern=\"yyyy-MM-dd HH:mm:ss\" type=\"date\" value=\"${" + entry.getLowerName() + "." + pName + " }\"/></td>\n");
			} else if (property.isTime()) {
				sb.append("\t\t\t\t\t\t<td><fmt:formatDate pattern=\"yyyy-MM-dd HH:mm:ss\" value=\"${" + entry.getLowerName() + "." + pName + " }\"/></td>\n");
			} else if (property.isI18n) {
				sb.append("\t\t\t\t\t\t<td><fmt:message key=\"" + property.entry.name + "." + property.name + "_${" + entry.getLowerName() + "." + pName + " }\"/></td>\n");
			} else if (property.type.equals("String")) {
				sb.append("\t\t\t\t\t\t<td>");
				if (Integer.parseInt(property.size) > 50) {
					sb.append("\n\t\t\t\t\t\t\t\t<c:out value=\"${fn:substring(" + entry.getLowerName() + "." + pName + ", 0, 20)}\"/>\n");
					sb.append("\t\t\t\t\t\t\t<c:if test=\"${fn:length(" + entry.getLowerName() + "." + pName + ")>20 }\">...</c:if>\n\t\t\t\t\t\t\t</td>\n");
				} else {
					sb.append("<c:out value=\"${" + entry.getLowerName() + "." + pName + " }\"/></td>\n");
				}
			} else {
				sb.append("\t\t\t\t\t\t<td><c:out value=\"${" + entry.getLowerName() + "." + pName + " }\"/></td>\n");
			}
			i++;
		}
		if (itemCountOverflow) sb.append("\t\t\t\t\t\t--%>\n");
		sb.append("\t\t\t\t\t\t<td class=\"oparate\"><a href=\"javascript:void(0);\" class=\"mini-button\"><span class=\"mini-button-low-text submit\">选择</span></a></td>\n");
		sb.append("\t\t\t\t\t</tr>\n");
		sb.append("\t\t\t\t</c:forEach>\n");
		sb.append("\t\t\t</tbody>\n");
		if (i > maxItemCount) i = maxItemCount;
		sb.append("\t\t\t<tfoot><tr><td colspan=\"" + (i + 1) + "\"></td></tr></tfoot>\n");
		sb.append("\t\t</table>\n");
		sb.append("\t\t<div class=\"selectPageDiv\"><%@ include file=\"/ui/components/page/ajax/default.jsp\"%></div>\n");
		sb.append("\t</div>\n");
		sb.append("</div>\n");
		try {
			FileUtil.write(Constant.baseDir + "/jsp/select/", entry.getLowerName() + ".jsp", sb.toString());
		} catch (IOException e) {
			e.printStackTrace();
		}
		/*
		 * sb.setLength(0); sb.append(
		 * "<%@ page language=\"java\" import=\"java.util.*\" pageEncoding=\"UTF-8\"%>\n"
		 * ); sb.append("<%@ include file=\"/common/jsp/jstl.jsp\" %>\n");
		 * sb.append("<select class=\"input4 required\" id=\""+
		 * entry.getLowerName() +"Id\" name=\""+ entry.getLowerName() +
		 * ".id\" title=\"请选择"+ entry.getSimpleCmt() +"\">\n"); sb.append(
		 * "\t<option value=\"\">"+ entry.getSimpleCmt() +"</option>\n");
		 * sb.append("\t<c:forEach items=\"${list"+ entry.name +" }\" var=\""+
		 * entry.getLowerName() +"Item\">\n"); sb.append(
		 * "\t\t<option <c:if test=\"${paramMap['"+ entry.getLowerName()
		 * +".id']=="+ entry.getLowerName() +"Item.id || "+ entry.getLowerName()
		 * +".id=="+ entry.getLowerName() +
		 * "Item.id }\"> selected=\"selected\"</c:if> value=\"${"+
		 * entry.getLowerName() +"Item.id }\"><c:out value=\"${"+
		 * entry.getLowerName() +"Item."+ entry.getViewProp() +
		 * " }\"/></option>\n"); sb.append("\t</c:forEach>\n");
		 * sb.append("</select>\n"); try { FileUtil.write(Constant.baseDir +
		 * "/jsp/select/", entry.getLowerName() + ".jsp", sb.toString()); }
		 * catch (IOException e) { e.printStackTrace(); }
		 */

		// view页面
		sb.setLength(0);
		sb.append("<%@ page language=\"java\" import=\"java.util.*\" pageEncoding=\"UTF-8\"%>\n");
		sb.append("<%@ include file=\"/common/jsp/jstl.jsp\" %>\n");
		sb.append("<%@ include file=\"/common/jsp/spring.jsp\" %>\n");
		// sb.append("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0
		// Transitional//EN\"
		// \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">\n");
		// sb.append("<html>\n");
		// sb.append("<head>\n");
		// sb.append("<meta http-equiv=\"Content-Type\" content=\"text/html;
		// charset=utf-8\" />\n");
		// sb.append("<title>查看"+ entry.getSimpleCmt() +"-<fmt:message
		// key=\"info.project.name\"/></title>\n");
		// sb.append("</head>\n");
		// sb.append("<body>\n");
		sb.append("<fieldset style=\"margin: 10px 0 10px 0;\">\n");
		sb.append("<legend>" + entry.getSimpleCmt() + "信息</legend>\n");
		sb.append("<table class=\"marBottom10px\" style=\"min-width: 500px;\">\n");

		// 生成普通字段
		i = 0;
		for (Map.Entry mentry : propertyMap.entrySet()) {
			Property property = (Property) mentry.getValue();
			if (property.name.equals("id")) continue;

			// 属性名称
			String pName = mentry.getKey().toString();

			// 属性注释
			String pCmt = property.getSimpleCmt();
			if (!property.entry.equals(entry)) {// 附属类的属性
				Property prop = entry.findPropByAffiliated(property.entry);
				if (property.entry.getViewProp().equals(property.name)) pCmt = prop.getSimpleCmt();
			}

			if ((i + 1) % 3 == 1) sb.append("\t<tr>\n");

			// 如果是类
			if (property.isClass() && !property.isBigString()) {
				i++;
				sb.append("\t\t<th>" + pCmt + "：</th>\n");
				sb.append("\t\t<td>\n");
				Entry propEntry = CodeBuildCache.entryMap.get(property.type);
				if (propEntry == null) continue;
				sb.append("\t\t\t${" + entry.getLowerName() + "." + property.name + "." + propEntry.getViewProp() + "}\n");
				sb.append("\t\t</td>\n");
			} else if (property.isI18n) {
				i++;
				sb.append("\t\t<th>" + pCmt + "：</th>\n");
				sb.append("\t\t<td>\n");
				sb.append("\t\t\t<fmt:message key=\"" + property.entry.name + "." + property.name + "_${" + entry.getLowerName() + "." + pName + " }\"/>\n");
				sb.append("\t\t</td>\n");
			} else {
				// 普通属性：不是text，不是大字符串，长度小于100
				if (!property.tType.equals("text") && !property.isBigString() && com.yxlisv.util.math.NumberUtil.parseInt(property.size) < 100) {
					i++;
					sb.append("\t\t<th>" + pCmt + "：</th>\n");
					sb.append("\t\t<td>\n");
					sb.append("\t\t\t");

					// 如果是double，则格式化显示
					if (property.type.equals("double") || property.type.equals("float") || property.type.equals("decimal")) sb.append(" <fmt:formatNumber pattern=\"#.##\" value=\"${" + entry.getLowerName() + "." + pName + " }\"/>\n");
					else if (property.isDate()) {
						sb.append(" <fmt:formatDate pattern=\"yyyy-MM-dd HH:mm:ss\" type=\"date\" value=\"${" + entry.getLowerName() + "." + pName + " }\"/>\n");
					} else if (property.isTime()) {
						sb.append(" <fmt:formatDate pattern=\"yyyy-MM-dd HH:mm:ss\" value=\"${" + entry.getLowerName() + "." + pName + " }\"/>\n");
					} else sb.append(" <c:out value=\"${" + entry.getLowerName() + "." + pName + " }\"/>\n");
					sb.append("\t\t</td>\n");
				}
			}
			if (i % 3 == 0) sb.append("\t</tr>\n");
		}
		if (i % 3 != 0) {
			int chazhi = i % 3;
			for (int j = 1; j <= (3 - chazhi); j++) {
				sb.append("\t\t<th></th><td></td>\n");
			}
			sb.append("\t</tr>\n");
		}

		// textarea
		for (Map.Entry mentry : propertyMap.entrySet()) {
			Property property = (Property) mentry.getValue();
			if (property.name.equals("id")) continue;

			// 属性名称
			String pName = mentry.getKey().toString();

			// 属性注释
			String pCmt = property.getSimpleCmt();
			if (!property.entry.equals(entry)) {// 附属类的属性
				Property prop = entry.findPropByAffiliated(property.entry);
				if (property.entry.getViewProp().equals(property.name)) pCmt = prop.getSimpleCmt();
			}

			if (property.isBigString()) pName += ".val";
			else if (property.isClass()) {
				Entry propEntry = CodeBuildCache.entryMap.get(property.type);
				if (propEntry == null) continue;
				pName += "." + propEntry.getViewProp();
			}

			// 如果数据库类型是 text，或者长度大于100就用textarea
			if (property.tType.equals("text") || property.isBigString() || (property.type.equals("String") && Integer.parseInt(property.size) >= 100)) {
				sb.append("\t<tr>\n");

				sb.append("\t\t<th>" + pCmt + "：</th>\n");
				sb.append("\t\t<td colspan=\"5\">");

				// 最大长度
				String maxlength = "5000";
				if (property.size != null && !property.size.equals("")) maxlength = property.size;
				// 行数
				int row = 5;
				// 大于500个字，行数设置为15行
				try {
					if (Integer.parseInt(maxlength) > 500) row = 10;
				} catch (Exception e) {}
				sb.append("<yxl:stringUtil type=\"desc\" value=\"${" + entry.getLowerName() + "." + pName + " }\" />");
				sb.append("</td>\n");
				sb.append("\t</tr>\n");
			}
		}

		sb.append("</table>\n");
		sb.append("</fieldset>\n");
		// sb.append("</body>\n");
		// sb.append("</html>");
		htmlCode = sb.toString();
		htmlCode = htmlCode.replaceAll("\\s*<tr>\\s*</tr>", "");
		try {
			FileUtil.write(Constant.baseDir + "/jsp/" + cPackage.pathQz + "/" + entry.getLowerName() + "/", "view.jsp", htmlCode);
		} catch (IOException e) {
			e.printStackTrace();
		}

		// 文件夹说明
		sb.setLength(0);
		sb.append(" ");
		try {
			FileUtil.write(Constant.baseDir + "/jsp/" + cPackage.pathQz + "/" + entry.getLowerName() + "/", entry.getSimpleCmt(), sb.toString());
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	/**
	 * 生成索引页面
	 * 
	 * @autor yxl
	 */
	@SuppressWarnings({ "rawtypes" })
	public static void buildIndexPage(List<ControlPackage> packages) {

		StringBuffer sb = new StringBuffer();
		for (Iterator it = packages.iterator(); it.hasNext();) {
			sb.delete(0, sb.length());
			ControlPackage cPackage = (ControlPackage) it.next();

			// 映射路径
			String path = "";
			if (cPackage.pathQz != null && !cPackage.pathQz.equals("")) path = "/" + cPackage.pathQz;

			// index页面
			sb.setLength(0);
			sb.append("<%@ page language=\"java\" import=\"java.util.*\" pageEncoding=\"UTF-8\"%>\n");
			sb.append("<%@ include file=\"/common/jsp/jstl.jsp\" %>\n");
			sb.append("<%@ include file=\"/common/jsp/spring.jsp\" %>\n");
			sb.append("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">\n");
			sb.append("<html>\n");
			sb.append("<head>\n");
			sb.append("<title>" + cPackage.pathQz + "|<fmt:message key=\"info.project.name\"/></title>\n");
			sb.append("<link rel=\"SHORTCUT ICON\" href=\"${pageContext.request.contextPath }/images/logo.ico\" />\n");
			sb.append("<%@ include file=\"/common/jsp/jquery.jsp\"%>\n");
			sb.append("<link href=\"${pageContext.request.contextPath }/css/global/base.css\" rel=\"stylesheet\" type=\"text/css\" />\n");
			sb.append("<link href=\"${pageContext.request.contextPath }/css/global/general.css\" rel=\"stylesheet\" type=\"text/css\" />\n");
			sb.append("<link href=\"${pageContext.request.contextPath }/css/global/layout.css\" rel=\"stylesheet\" type=\"text/css\" />\n");
			sb.append("<link href=\"${pageContext.request.contextPath }/js/plugins/ligerUI/skins/Aqua/css/ligerui-all.css\" rel=\"stylesheet\" type=\"text/css\" />\n");
			sb.append("<link href=\"${pageContext.request.contextPath }/js/plugins/ligerUI/skins/Gray/css/all.css\" rel=\"stylesheet\" type=\"text/css\" />\n");
			sb.append("<script src=\"${pageContext.request.contextPath }/js/plugins/ligerUI/js/ligerui.all.js\" type=\"text/javascript\"></script>\n");
			sb.append("<script src=\"${pageContext.request.contextPath }/js/index.js\" type=\"text/javascript\"></script>\n");
			sb.append("</head>\n");
			sb.append("<body>\n");
			sb.append("\t<div id=\"topmenu\" class=\"header\">\n");
			sb.append("\t\t<div id=\"header\">\n");
			sb.append("\t\t\t<div class=\"header_top\">\n");
			sb.append("\t\t\t\t<a href=\"javascript:void(0);\" class=\"logo fl\"></a>\n");
			sb.append("\t\t\t\t<div class=\"fr top_nav mr10\">\n");
			sb.append("\t\t\t\t\t<a id=\"personCenter\" href=\"javascript:void(0);\" class=\"per_center\">个人中心</a>\n");
			sb.append("\t\t\t\t\t<a id=\"changePwd\" href=\"javascript:void(0);\" class=\"account\">密码设置</a>\n");
			sb.append("\t\t\t\t\t<a id=\"feedbackCenter\" href=\"javascript:void(0);\" class=\"feedback\">建议</a>\n");
			sb.append("\t\t\t\t\t<a id=\"helpCenter\" href=\"javascript:void(0);\" class=\"help\">帮助</a>\n");
			sb.append("\t\t\t\t\t<a id=\"loginOut\" href=\"${pageContext.request.contextPath }/loginOut\" class=\"quit\">退出</a>\n");
			sb.append("\t\t\t\t</div>\n");
			sb.append("\t\t\t</div>\n");
			sb.append("\t\t</div>\n");
			sb.append("\t</div>\n");
			sb.append("\t<div id=\"layout1\" style=\"width:99.2%; margin:0 auto; margin-top:4px;\">\n");
			sb.append("\t\t<%-- 菜单頁面 --%>\n");
			sb.append("\t\t<div position=\"left\" title=\"${loginCompany.name }\" id=\"accordion1\" style=\"background-color: #e3e3e3;\">\n");
			sb.append("\t\t\t<%@ include file=\"menu.jsp\" %>\n");
			sb.append("\t\t</div>\n");
			sb.append("\t\t<%-- right頁面 --%>\n");
			sb.append("\t\t<div position=\"center\" id=\"framecenter\">\n");
			sb.append("\t\t\t<div tabid=\"home\" title=\"主页\">\n");
			sb.append("\t\t\t\t<iframe frameborder=\"0\" style=\"overflow-x:none;\" name=\"home\" id=\"home\" src=\"${pageContext.request.contextPath }" + path + "/welcome\"></iframe>\n");
			sb.append("\t\t\t</div>\n");
			sb.append("\t\t</div>\n");
			sb.append("\t</div>\n");
			sb.append("</body>\n");
			sb.append("</html>");
			try {
				FileUtil.write(Constant.baseDir + "/jsp/" + cPackage.pathQz + "/", "index.jsp", sb.toString());
			} catch (IOException e) {
				e.printStackTrace();
			}

			// 菜单页面
			sb.setLength(0);
			sb.append("<%@ page language=\"java\" import=\"java.util.*\" pageEncoding=\"UTF-8\"%>\n");
			sb.append("<script type=\"text/javascript\" src=\"${pageContext.request.contextPath }/js/jquery/plugins/menu/default/menu.js\"></script>\n");
			sb.append("<ul id=\"leftMenu\">\n");
			sb.append("\t<div class=\"menuList\">\n");
			sb.append("\t\t<h2 class=\"modleName\">\n");
			sb.append("\t\t\t<a><span>系统生成</span></a>\n");
			sb.append("\t\t</h2>\n");
			sb.append("\t\t<ul>\n");

			// 生成菜单
			for (Iterator itEt = CodeBuildCache.entryList.iterator(); itEt.hasNext();) {
				Entry et = (Entry) itEt.next();
				String onlyOneParentClassCode = "";// 是否只有一个父类属性
				if (et.onlyOneParentClass()) onlyOneParentClassCode = "?pageName=Index";
				sb.append("\t\t\t<li><a href=\"javascript:f_addTab('" + et.getLowerName() + "','" + et.getSimpleCmt() + "管理','${pageContext.request.contextPath }" + path + "/" + et.getLowerName() + ".html" + onlyOneParentClassCode + "')\"><i></i>" + et.getSimpleCmt() + "</a></li>\n");
			}
			sb.append("\t\t</ul>\n");
			sb.append("\t</div>\n");
			sb.append("\t<div class=\"menuList\">\n");
			sb.append("\t\t<h2 class=\"modleName\">\n");
			sb.append("\t\t\t<a><span>系统设置</span></a>\n");
			sb.append("\t\t</h2>\n");
			sb.append("\t\t<ul>\n");
			sb.append("\t\t\t<li><a href=\"javascript:f_addTab('修改密码','${pageContext.request.contextPath }/updatePass')\"><i></i>修改密码</a></li>\n");
			sb.append("\t\t</ul>\n");
			sb.append("\t</div>\n");
			sb.append("</ul>");
			try {
				FileUtil.write(Constant.baseDir + "/jsp/" + cPackage.pathQz + "/", "menu.jsp", sb.toString());
			} catch (IOException e) {
				e.printStackTrace();
			}

			// 欢迎页面
			/*
			 * try { FileUtil.copy(FilePathUtil.getWebRoot() + "/ui/test.jsp",
			 * Constant.baseDir + "/jsp/" + cPackage.pathQz + "/welcome.jsp"); }
			 * catch (IOException e) { e.printStackTrace(); }
			 */
		}
	}
}