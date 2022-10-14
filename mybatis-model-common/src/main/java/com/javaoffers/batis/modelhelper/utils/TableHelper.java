package com.javaoffers.batis.modelhelper.utils;

import com.javaoffers.batis.modelhelper.anno.BaseModel;
import com.javaoffers.batis.modelhelper.anno.BaseUnique;
import com.javaoffers.batis.modelhelper.anno.ColName;
import com.javaoffers.batis.modelhelper.anno.fun.parse.FunAnnoParser;
import com.javaoffers.batis.modelhelper.anno.fun.parse.ParseSqlFunResult;
import com.javaoffers.batis.modelhelper.exception.FindColException;
import com.javaoffers.batis.modelhelper.exception.ParseTableException;
import com.javaoffers.batis.modelhelper.fun.ConstructorFun;
import com.javaoffers.batis.modelhelper.fun.GetterFun;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.util.Assert;

import javax.sql.DataSource;
import java.lang.invoke.SerializedLambda;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Description: 表信息辅助类
 * @Auther: create by cmj on 2022/5/2 02:05
 */
public class TableHelper {

    /**
     * 数据库
     */
    private static DataSource dataSource;

    private static Map<Class, TableInfo> tableInfoMap = new ConcurrentHashMap<>();

    private static Map<String, Class> modelClass = new ConcurrentHashMap<>();

    private static Map<Class, Boolean> modelIsParse = new ConcurrentHashMap<>();

    public TableHelper(DataSource dataSource) {
        TableHelper.dataSource = dataSource;
    }

    /**
     * 获取 Model 对应的全部字段
     * @param modelClss
     * @return
     */
    public static List<String> getColAll(Class<?> modelClss){
        String name = modelClss.getName();
        String implClass = name.replaceAll("\\.","/");
        parseTableInfo(implClass);
        List<String> colAll = new LinkedList<>();
        TableInfo tableInfo = tableInfoMap.get(modelClss);
        String tableName = tableInfo.getTableName();
        tableInfo.getFieldNameColNameOfModel().forEach((fieldName,colName)->{
            if(tableInfo.isSqlFun(colName)){
                colAll.add(colName+" as "+fieldName);
            }else{
                colAll.add(tableName+"."+colName+" as "+fieldName);
            }

        });
        return colAll;
    }

    public static List<Pair<String, String>> getColAllAndAliasNameOnly(Class<?> modelClss){
        String name = modelClss.getName();
        String implClass = name.replaceAll("\\.","/");
        parseTableInfo(implClass);
        List<Pair<String, String>> colAll = new LinkedList<>();
        TableInfo tableInfo = tableInfoMap.get(modelClss);
        tableInfo.getFieldNameColNameOfModel().forEach((colName,fieldName)->{
            colAll.add(Pair.of(colName,fieldName));
        });
        return colAll;
    }

    public static Map<String, List<Field>> getColAllAndFieldOnly(Class<?> modelClss){
        String name = modelClss.getName();
        String implClass = name.replaceAll("\\.","/");
        parseTableInfo(implClass);
        List<Pair<String, String>> colAll = new LinkedList<>();
        TableInfo tableInfo = tableInfoMap.get(modelClss);
        Map<String, List<Field>> colNameOfModelField = tableInfo.getColNameAndFieldOfModel();
        return colNameOfModelField;
    }

    public static Map<String, List<Field>> getNColAllAndFieldOnly(Class<?> modelClss){
        String name = modelClss.getName();
        String implClass = name.replaceAll("\\.","/");
        parseTableInfo(implClass);
        List<Pair<String, String>> colAll = new LinkedList<>();
        TableInfo tableInfo = tableInfoMap.get(modelClss);
        Map<String, List<Field>> colNameOfModelField = tableInfo.getOriginalColNameOfModelField();
        return colNameOfModelField;
    }

    public static String getColName(GetterFun myFun){
        String methodName = StringUtils.EMPTY;
        String colName = StringUtils.EMPTY;
        try {
            // 直接调用writeReplace
            Method writeReplace = myFun.getClass().getDeclaredMethod("writeReplace");
            writeReplace.setAccessible(true);
            Object sl = writeReplace.invoke(myFun);
            SerializedLambda serializedLambda = (SerializedLambda) sl;
            methodName = serializedLambda.getImplMethodName();
            String implClass = serializedLambda.getImplClass();
            parseTableInfo(implClass);
            TableInfo tableInfo = tableInfoMap.get(modelClass.get(implClass));
            Map<String, String> fieldNameOfGetter = tableInfo.getMethodNameMappingFieldNameOfGetter();
            fieldNameOfGetter.computeIfAbsent(methodName, k -> {
                k = k.startsWith("get")?k.substring(3): k.startsWith("is")?k.substring(2):k;
                k = k.substring(0, 1).toLowerCase()+k.substring(1);
                return k;
            });
            String fieldName = fieldNameOfGetter.get(methodName);
            colName = tableInfo.getFieldNameColNameOfModel().get(fieldName);
            if(tableInfo.isSqlFun(colName)){
                colName = colName+" as "+ fieldName;
            }else{
                colName = tableInfo.getTableName()+"."+colName+" as "+ fieldName;
            }
            return colName;
        }catch (Exception e){
            e.printStackTrace();
        }
        return colName;
    }

    public static String getColNameNotAs(GetterFun myFun){
        String methodName = StringUtils.EMPTY;
        String colName = StringUtils.EMPTY;
        try {
            // 直接调用writeReplace
            Method writeReplace = myFun.getClass().getDeclaredMethod("writeReplace");
            writeReplace.setAccessible(true);
            Object sl = writeReplace.invoke(myFun);
            SerializedLambda serializedLambda = (SerializedLambda) sl;
            methodName = serializedLambda.getImplMethodName();
            String implClass = serializedLambda.getImplClass();
            parseTableInfo(implClass);
            TableInfo tableInfo = tableInfoMap.get(modelClass.get(implClass));
            Map<String, String> fieldNameOfGetter = tableInfo.getMethodNameMappingFieldNameOfGetter();
            fieldNameOfGetter.computeIfAbsent(methodName, k->{
                k = k.startsWith("get")?k.substring(3): k.startsWith("is")?k.substring(2):k;
                k = k.substring(0,1).toLowerCase()+k.substring(1);
                return k;
            });
            String fieldName = fieldNameOfGetter.get(methodName);
            colName = tableInfo.getFieldNameColNameOfModel().get(fieldName);
            if(!tableInfo.isSqlFun(colName)){
                colName = tableInfo.getTableName()+"."+colName;
            }
            return colName;
        }catch (Exception e){
            e.printStackTrace();
        }
        return colName;
    }


    public static Pair<String,String> getColNameAndAliasName(GetterFun myFun){
        String methodName = StringUtils.EMPTY;
        try {
            // 直接调用writeReplace
            Method writeReplace = myFun.getClass().getDeclaredMethod("writeReplace");
            writeReplace.setAccessible(true);
            Object sl = writeReplace.invoke(myFun);
            SerializedLambda serializedLambda = (SerializedLambda) sl;
            methodName = serializedLambda.getImplMethodName();
            String implClass = serializedLambda.getImplClass();
            parseTableInfo(implClass);
            TableInfo tableInfo = tableInfoMap.get(modelClass.get(implClass));
            Map<String, String> colNameOfGetter = tableInfo.getMethodNameMappingFieldNameOfGetter();
            colNameOfGetter.computeIfAbsent(methodName, k->{
                k = k.startsWith("get")?k.substring(3): k.startsWith("is")?k.substring(2):k;
                k = k.substring(0,1).toLowerCase()+k.substring(1);
                return k;
            });
            String fieldName = colNameOfGetter.get(methodName);;
            String colName = tableInfo.getFieldNameColNameOfModel().get(fieldName);
            if(!tableInfo.isSqlFun(colName)){
                colName =  tableInfo.getTableName()+"."+colName;
            }
            return Pair.of(colName, fieldName);
        }catch (Exception e){
            e.printStackTrace();
        }
        throw new FindColException("解析sql字段出错 ： "+myFun.toString());
    }

    public static String getColNameOnly(GetterFun myFun){
        String methodName = StringUtils.EMPTY;
        try {
            // 直接调用writeReplace
            Method writeReplace = myFun.getClass().getDeclaredMethod("writeReplace");
            writeReplace.setAccessible(true);
            Object sl = writeReplace.invoke(myFun);
            SerializedLambda serializedLambda = (SerializedLambda) sl;
            methodName = serializedLambda.getImplMethodName();
            String implClass = serializedLambda.getImplClass();
            parseTableInfo(implClass);
            TableInfo tableInfo = tableInfoMap.get(modelClass.get(implClass));
            Map<String, String> colNameOfGetter = tableInfo.getMethodNameMappingFieldNameOfGetter();
            colNameOfGetter.computeIfAbsent(methodName, k->{
                k = k.startsWith("get")?k.substring(3): k.startsWith("is")?k.substring(2):k;
                k = k.substring(0,1).toLowerCase()+k.substring(1);
                return k;
            });
            String fieldName = colNameOfGetter.get(methodName);;
            String colName =  tableInfo.getFieldNameColNameOfModel().get(fieldName);
            return colName;
        }catch (Exception e){
            e.printStackTrace();
        }
        throw new FindColException("解析sql字段出错 ： "+myFun.toString());
    }

    /**
     * Parse table information.
     * @param implClass
     */
    private static void parseTableInfo(String implClass) {
        modelClass.computeIfAbsent(implClass, k->{
            try {
                Class<?> modelClazz = Class.forName(implClass.replaceAll("/","\\."));
                //解析model class
                parseModelClass(modelClazz);
                return modelClazz;
            }catch (Exception e){
                e.printStackTrace();
            }
            return null;
        });
    }

    /**
     * Parse the model
     * @param modelClazz Classes marked with the annotation @BaseModel
     */
    private static void parseModelClass(Class<?> modelClazz) {
        Boolean isParse = modelIsParse.getOrDefault(modelClazz, false);
        if(!isParse){
            synchronized (modelClazz){
                isParse = modelIsParse.getOrDefault(modelClazz, false);
                if(!isParse){
                    BaseModel table = modelClazz.getDeclaredAnnotation(BaseModel.class);
                    Assert.isTrue(table != null, " base model is null. please use @BaseModel on class");
                    String tableName = table.value();
                    if(StringUtils.isBlank(tableName)){
                        String simpleName = modelClazz.getSimpleName();
                        tableName = conLine(simpleName);
                    }
                    Connection connection = null;
                    try {
                        TableInfo tableInfo = new TableInfo(tableName);
                        connection = dataSource.getConnection();
                        DatabaseMetaData metaData = connection.getMetaData();

                        ResultSet tableResultSet = metaData.getTables(connection.getCatalog(),connection.getSchema(),tableName,null);
                        ResultSet primaryKeys = metaData.getPrimaryKeys(connection.getCatalog(), connection.getSchema(), tableName);
                        LinkedList<String> primaryKeyList = new LinkedList<>();
                        while (primaryKeys.next()){
                            String primaryColName = primaryKeys.getString(ColumnLabel.COLUMN_NAME);
                            primaryKeyList.add(primaryColName);
                        }
                        while (tableResultSet.next()) {
                            // Get table field structure
                            ResultSet columnResultSet = metaData.getColumns(dataSource.getConnection().getCatalog(), "", tableName, "%");
                            while (columnResultSet.next()) {
                                // Field Name
                                String columnName = columnResultSet.getString(ColumnLabel.COLUMN_NAME);
                                // type of data
                                String columnType = columnResultSet.getString(ColumnLabel.TYPE_NAME);
                                //the default value of the field
                                Object defaultValue = columnResultSet.getString(ColumnLabel.COLUMN_DEF);
                                //Whether to auto increment
                                boolean isAutoincrement = "YES".equalsIgnoreCase(columnResultSet.getString(ColumnLabel.IS_AUTOINCREMENT));
                                ColumnInfo columnInfo = new ColumnInfo(columnName, columnType, isAutoincrement,defaultValue);
                                tableInfo.getColumnInfos().add(columnInfo);
                                tableInfo.getColNames().put(columnName,columnInfo);
                                if(primaryKeyList.contains(columnName)){
                                    tableInfo.getPrimaryColNames().put(columnName, columnInfo);
                                }
                            }
                        }
                        tableInfoMap.put(modelClazz,tableInfo);

                        Field[] colFs= modelClazz.getDeclaredFields();
                        for(Field colF : colFs){
                            colF.setAccessible(true);
                            String colName = conLine(colF.getName());
                            BaseUnique baseUnique = colF.getDeclaredAnnotation(BaseUnique.class);
                            if(baseUnique != null ){
                                if(StringUtils.isNotBlank(baseUnique.value())){
                                    colName = baseUnique.value();
                                }
                            }
                            //parse @ColName and @funAnno
                            ParseSqlFunResult parseColName = FunAnnoParser.parse(modelClazz, colF, colName);
                            if(parseColName != null ){
                                colName = parseColName.getSqlFun();
                                tableInfo.putSqlFun(colName, parseColName.isFun());
                            }else{
                                // Indicates that this field does not have any annotation information.
                                // then the field must belong to a field in the original table
                                // Otherwise skip parsing of this field
                                if(!tableInfo.getColNames().containsKey(colName)){
                                    continue;
                                }
                            }

                            String fieldName = colF.getName();
                            // original table fields and sql-fun fields
                            tableInfo.putFieldNameColNameOfModel(fieldName, colName);
                            tableInfo.putColNameAndFieldOfModel(colName, colF);

                            if( tableInfo.getColNames().containsKey(colName)){
                                //original table fields
                                tableInfo.putOriginalColNameAndFieldOfModelField(colName,colF);
                            }
                        }
                    }catch (Exception e){
                        e.printStackTrace();
                        throw new ParseTableException(e.getMessage(),e);
                    }finally {
                        if(connection != null){
                            try {
                                connection.close();
                            }catch (Exception e){
                                e.printStackTrace();
                            }
                        }
                    }
                    modelIsParse.put(modelClazz, true);
                }
            }
        }
    }

    /**
     * turn underscore
     * @param info
     * @return
     */
    private static String conLine(String info) {
        if(info.contains("_")){
            return info;
        }

        String[] split = info.split("");
        StringBuilder builder = new StringBuilder();
        String last = null;
        for(String s : split){
            if(StringUtils.isAllUpperCase(s)){
                if(builder.length()!=0 && StringUtils.isAllLowerCase(last)){
                    s = s.toLowerCase();
                    s="_"+s;
                }
            }
            builder.append(s);
            last = s;
        }
        info = builder.toString();
        String defaultIgnoreCase = "YES";
        String ignoreCase = System.getProperty("IgnoreCase", defaultIgnoreCase);
        if(defaultIgnoreCase.equalsIgnoreCase(ignoreCase)){
            info = info.toLowerCase();
        }
        return info;
    }

    /**
     * Parse the class corresponding to the constructor
     * @param constructorFun
     * @param <M2>
     * @return
     */
    public static <M2> Class<M2> getClassFromConstructorFun(ConstructorFun<M2> constructorFun) {
        // 直接调用writeReplace
        String implClass = StringUtils.EMPTY;
        try {
            Method method = constructorFun.getClass().getDeclaredMethods()[0];
            method.setAccessible(true);
            Object sl = method.invoke(constructorFun);
            String lamdaName = sl.getClass().getName();
            implClass = lamdaName.replaceAll("\\.","/");
            parseTableInfo(implClass);

        }catch (Exception e){
            e.printStackTrace();
        }
        return modelClass.get(implClass);
    }

    /**
     * get tableName by Class
     * @param m2c
     * @param <M2>
     * @return
     */
    public static <M2> String getTableName(Class<M2> m2c) {
        return getTableInfo(m2c).getTableName();
    }

    /**
     * get TableInfo by class
     * @param m2c
     * @return
     */
    public static TableInfo getTableInfo(Class<?> m2c){
        TableInfo tableInfo = tableInfoMap.get(m2c);
        if(tableInfo == null){
            synchronized (m2c){
                tableInfo = tableInfoMap.get(m2c);
                if(tableInfo == null){
                    parseModelClass(m2c);
                    tableInfo = tableInfoMap.get(m2c);
                }
            }
        }
        return tableInfo;
    }
}
