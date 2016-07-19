package com.yxlisv.codebuild.entry;

import java.util.ArrayList;
import java.util.List;

import com.yxlisv.codebuild.Constant;
import com.yxlisv.util.string.StringUtil;

/**
 * <p>实体类</p>
 * @author 杨雪令
 * @time 2016年3月17日上午9:25:50
 * @version 1.0
 */
public class Entry{
	
	//实体名称
	public String name;
	
	//表名
	public String tableName;
	
	//表注释
	public String tableCmt;
	
	//是否为附属表
	public boolean isAffiliated = false;
	
	//表字段集合
	private List<Property> propertyList = new ArrayList<Property>();
	
	
	/**
	 * <p>初始化实体</p>
	 * @param tableName 表名
	 * @param tableCmt 表注释
	 * @author 杨雪令
	 * @time 2016年3月17日上午9:32:34
	 * @version 1.0
	 */
	public Entry(String tableName, String tableCmt){
		this.tableName = tableName;
		if(Constant.tableUpperCase) this.tableName = tableName.toUpperCase();
		this.tableCmt = tableCmt;
		
		//清除实体名称忽略的字符
		String entryName = tableName;
		entryName = entryName.replaceAll(Constant.entryNameIgnore.toUpperCase(), "");
		entryName = entryName.replaceAll(Constant.entryNameIgnore.toLowerCase(), "");
		
		this.name = this.fmtTbName(entryName);
		
		//是否是附属表
		if(tableName.toUpperCase().endsWith("_INFO")) isAffiliated = true;
	}
	
	/**
	 * <p>得到表字段集合</p>
	 * @return List<Property> 表字段集合
	 * @author 杨雪令
	 * @time 2016年3月17日上午9:27:29
	 * @version 1.0
	 */
	public List<Property> getPropertyList(){
		return this.propertyList;
	}
	
	/**
	 * <p>获取ID属性</p>
	 * @return Property ID属性
	 * @author 杨雪令
	 * @time 2016年3月17日上午9:27:48
	 * @version 1.0
	 */
	public Property getIdProperty(){
		for(Property property : propertyList){
			return property;
			//if(property.name.equals("id")) return property;
		}
		return null;
	}
	
	
	/**
	 * <p>获取属性类别为其他实体类的属性</p>
	 * @author 杨雪令
	 * @time 2016年3月17日上午9:29:06
	 * @version 1.0
	 */
	public List<Property> getClassProperty(){
		List<Property> listClassProperty = new ArrayList<Property>();
		for(Property property : propertyList){
			if(property.isClass()) listClassProperty.add(property);
		}
		return listClassProperty;
	}
	
	/**
	 * <p>是否只有一个属性关联其他实体</p>
	 * @author 杨雪令
	 * @time 2016年3月17日上午9:29:58
	 * @version 1.0
	 */
	public boolean onlyOneParentClass(){
		List<Property> propList = getClassProperty();
		int count = 0;
		for(Property property : propList){
			if(property.isClass() && !property.isMainClass()) count++;
		}
		if(count==1) return true;
		return false;
	}
	
	
	/**
	 * <p>获取第一个关联其他实体的属性</p>
	 * @author 杨雪令
	 * @time 2016年3月17日上午9:31:05
	 * @version 1.0
	 */
	public Property getFirstParentClassProperty(){
		List<Property> propList = getClassProperty();
		for(Property property : propList){
			if(property.isClass() && !property.isMainClass()) return property;
		}
		return null;
	}
	
	/**
	 * 获取展示的属性
	 */
	public String getViewProp(){
		String viewProp = "id";
		for(Property property : getPropertyList()){
			if(property.name.equals("name")) viewProp = property.name;
		}
		if(viewProp.equals("id")){
			for(Property property : getPropertyList()){
				if(property.name.equals("val")) viewProp = property.name;
			}
		}
		if(viewProp.equals("id")){
			for(Property property : getPropertyList()){
				if(property.name.toLowerCase().endsWith("name")) viewProp = property.name;
			}
		}
		if(viewProp.equals("id")){
			for(Property property : getPropertyList()){
				if(property.name.toLowerCase().contains("name")) viewProp = property.name;
			}
		}
		return viewProp;
	}
	
	/**
	 * 获取展示的属性（大写）
	 */
	public String getViewPropUp(){
		String viewProp = getViewProp();
		return StringUtil.toUpper4FirstWord(viewProp);
	}
	
	/**
	 * 获取简洁的表名
	 * @autor yxl
	 */
	public String getSimpleCmt(){
		String cmt = tableCmt;
		
		//不需要的字符串
		String []badStr = {"(", "（"};
		
		for(String bs : badStr){
			if(cmt.indexOf(bs) != -1) cmt = cmt.substring(0, cmt.indexOf(bs));
		}
		
		return cmt;
	}
	
	
	/**
	 * <p>添加一个属性</p>
	 * @param name 属性名称
	 * @param type 属性类型
	 * @param size 属性字段长度
	 * @param comment 注释
	 * @param notnull 不允许为空
	 * @author 杨雪令
	 * @time 2016年3月17日上午9:34:49
	 * @version 1.0
	 */
	public void addProperty(String name, String type, String size, String comment, boolean notnull){
		Property  property = new Property(name, type, size, comment, notnull, this);
		propertyList.add(property);
	}
	
	/**
	 * 格式化表名，如 customer_need --> CustomerNeed
	 * @param tableStr
	 * @autor yxl
	 */
	public String fmtTbName(String tableStr){
		tableStr = StringUtil.toUpperBh(tableStr, "_", 1);
		tableStr = StringUtil.toUpper4FirstWord(tableStr);
		
		return tableStr;
	}
	
	/**
	 * 得到小写的名称
	 * @autor yxl
	 */
	public String getLowerName(){
		return StringUtil.toLower4FirstWord(this.name);
	}
	
	/**
	 * 根据附属类查找属性
	 * @return
	 */
	public Property findPropByAffiliated(Entry propEntry){
		for(Property property : getPropertyList()){
			if(property.type.equals(propEntry.name)) return property;
		}
		return null;
	}
}
