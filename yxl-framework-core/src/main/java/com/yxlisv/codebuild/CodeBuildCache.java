package com.yxlisv.codebuild;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.yxlisv.codebuild.entry.Entry;

/**
 * <p>代码控制器缓存</p>
 * @author 杨雪令
 * @time 2016年3月17日上午9:08:33
 * @version 1.0
 */
public class CodeBuildCache {

	/** 缓存表名 */
	public static List<String> tableNameCache = new ArrayList<String>();

	/** 缓存实体类数据：类名，实体类 */
	public static Map<String, Entry> entryMap = new LinkedHashMap<String, Entry>();

	/** 缓存实体类数据 */
	public static List<Entry> entryList = new ArrayList<Entry>();
}