package com.javaoffers.brief.modelhelper.config;

import com.javaoffers.brief.modelhelper.constants.ConfigPropertiesConstants;
import com.javaoffers.brief.modelhelper.exception.BriefException;
import com.javaoffers.brief.modelhelper.filter.Filter;
import com.javaoffers.brief.modelhelper.jdbc.JdbcExecutorFactory;
import com.javaoffers.brief.modelhelper.utils.ReflectionUtils;
import com.javaoffers.brief.modelhelper.utils.Utils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @description: brief 初始化配置信息.
 * @author: create by cmj on 2023/8/5 13:20
 */
public class BriefProperties {

    private final Map<String, Object> properties = new ConcurrentHashMap<>();
    private final Properties bp = System.getProperties();
    private volatile List<Filter> jqlChainFilterList = new ArrayList<>();
    private volatile long showLogTime = 10;
    private volatile JdbcExecutorFactory jdbcExecutorFactory;
    private volatile boolean isPrintSql;
    private volatile boolean isPrintSqlCost;

    /**
     * 添加配置信息
     *
     * @param key
     * @param value
     * @return
     */
    public BriefProperties put(String key, String value) {
        bp.setProperty(key, value);
        properties.put(key, value);
        return this;
    }

    /**
     * 是否打印sql
     *
     * @param isPrintSql
     */
    public BriefProperties setIsPrintSql(String isPrintSql) {
        put(ConfigPropertiesConstants.IS_PRINT_SQL, isPrintSql.trim().toLowerCase());
        return this;
    }

    /**
     * 是否打印sql耗时
     *
     * @param isPrintSqlCost
     */
    public BriefProperties setIsPrintSqlCost(String isPrintSqlCost) {
        put(ConfigPropertiesConstants.IS_PRINT_SQL_COST, isPrintSqlCost.trim().toLowerCase());
        return this;
    }

    //加载自定义jdbc处理器.
    public BriefProperties setJdbcExecutorFactory(String jdbcExecutorFactoryClassName) {
        put(ConfigPropertiesConstants.JDBC_EXECUTOR_FACTORY, jdbcExecutorFactoryClassName);
        return this;
    }

    public BriefProperties setSlowSqlLogTime(String slowLogTime) {
        put(ConfigPropertiesConstants.SLOW_LOG_TIME, slowLogTime);
        return this;
    }

    public JdbcExecutorFactory getJdbcExecutorFactory() {
        return jdbcExecutorFactory;
    }

    /**
     * 获取慢sql日志时间
     *
     * @return
     */
    public long getSlowSqlLogTime() {
        return showLogTime;
    }

    /**
     * 获取jql过滤器
     *
     * @return
     */
    public List<Filter> getJqlFilters() {
        return jqlChainFilterList;
    }

    public boolean isPrintSql() {
        return isPrintSql;
    }

    public boolean isPrintSqlCost() {
        return isPrintSqlCost;
    }

    public void initIsPrintSql() {
        isPrintSql = Boolean.parseBoolean(bp.getProperty(ConfigPropertiesConstants.IS_PRINT_SQL, "true"));
    }

    public void initIsPrintSqlCost() {
        isPrintSqlCost = Boolean.parseBoolean(bp.getProperty(ConfigPropertiesConstants.IS_PRINT_SQL_COST, "true"));
    }

    public void initJdbcExecutorFactory() {
        String jdbcExecutorFactoryClassName = bp.getProperty(ConfigPropertiesConstants.JDBC_EXECUTOR_FACTORY,
                "com.javaoffers.brief.modelhelper.jdbc.BriefJdbcExecutorFactory");
        try {
            Class<?> jef = Class.forName(jdbcExecutorFactoryClassName);
            jdbcExecutorFactory = (JdbcExecutorFactory) jef.newInstance();
        } catch (Exception e) {
            e.printStackTrace();
            throw new BriefException(e.getMessage());
        }
    }

    //初始化慢sql时间
    public void initShowLogTime() {
        String mls = bp.getProperty(ConfigPropertiesConstants.SLOW_LOG_TIME);
        if (StringUtils.isNotBlank(mls)) {
            showLogTime = Long.parseLong(mls.trim());
        }
    }

    //初始化jql过滤器
    public void initJqlFilters() {
        List<Filter> jqlChainFilterList = new ArrayList<>();
        String jqlFilters = bp.getProperty(ConfigPropertiesConstants.JQL_FILTER);
        if (jqlFilters != null) {

            String[] jqlFilterArray = jqlFilters.replaceAll(" ", "").split(",");
            for (String jqlFilterClassName : jqlFilterArray) {
                try {
                    Class<?> jqlFilterClass = Class.forName(jqlFilterClassName);
                    Filter jqlChainFilter = (Filter) jqlFilterClass.newInstance();
                    jqlChainFilterList.add(jqlChainFilter);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        String jqlClassName = "com.javaoffers.brief.modelhelper.filter.JqlChainFilter";
        Set<Class<? extends Filter>> childs = ReflectionUtils.getChilds(Utils.getClass(jqlClassName));
        if (CollectionUtils.isNotEmpty(childs)) {
            for (Class clazz : childs) {
                try {
                    Filter o1 = (Filter) clazz.newInstance();
                    jqlChainFilterList.add(o1);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        this.jqlChainFilterList = jqlChainFilterList;
    }
}
