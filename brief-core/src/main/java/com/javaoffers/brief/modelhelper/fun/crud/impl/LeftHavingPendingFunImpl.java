package com.javaoffers.brief.modelhelper.fun.crud.impl;

import com.javaoffers.brief.modelhelper.fun.Condition;
import com.javaoffers.brief.modelhelper.fun.ConditionTag;
import com.javaoffers.brief.modelhelper.fun.GGetterFun;
import com.javaoffers.brief.modelhelper.fun.GetterFun;
import com.javaoffers.brief.modelhelper.fun.condition.where.LeftGroupByWordCondition;
import com.javaoffers.brief.modelhelper.fun.condition.where.LimitWordCondition;
import com.javaoffers.brief.modelhelper.fun.condition.where.OrderWordCondition;
import com.javaoffers.brief.modelhelper.fun.crud.LeftHavingPendingFun;
import com.javaoffers.brief.modelhelper.fun.crud.OrderFun;
import com.javaoffers.brief.modelhelper.utils.TableHelper;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @Description:
 * @Auther: create by cmj on 2022/6/5 19:42
 */
public class LeftHavingPendingFunImpl<M, M2, C extends GetterFun<M,?>, C2 extends GGetterFun<M2,?>, V, V2, THIS extends LeftHavingPendingFunImpl<M, M2,C,C2,V,V2, THIS>> implements
        LeftHavingPendingFun<M, M2, C, C2, V, LeftHavingFunImpl<M,M2,C,C2,V,V2, ?>>,
        OrderFun<M,C,V,LeftHavingPendingFunImpl<M,M2,C,C2,V,V2, THIS>> {

    private LinkedList<Condition> conditions;

    private WhereSelectFunImpl whereSelectFun;

    public LeftHavingPendingFunImpl(LinkedList<Condition> conditions) {
        this.conditions = conditions;
        this.whereSelectFun = new WhereSelectFunImpl(this.conditions,false);
    }

    @Override
    public M ex() {
        List<M> exs = exs();
        if (exs != null && exs.size() > 0) {
            return exs.get(0);
        }
        return null;
    }

    /**
     * 主表分组
     * @param c 主表分组字段
     * @return (THIS) this
     */
    @SafeVarargs
    public final THIS groupBy(GetterFun<M, V>... c) {
        conditions.add(new LeftGroupByWordCondition(c, ConditionTag.GROUP_BY));
        return (THIS) this;
    }

    /**
     * 分组，支持元SQL。 比如 groupBy( " left(colName, 10) ")
     * @param sqlCol col
     * @return (THIS) this
     */
    public THIS groupBy(String sqlCol) {
        conditions.add(new LeftGroupByWordCondition(new String[]{sqlCol}, ConditionTag.GROUP_BY));
        return (THIS) this;
    }

    /**
     * 子表分组
     * @param c 子表分组字段
     * @return
     */
    @SafeVarargs
    public final THIS groupBy(GGetterFun<M2, V>... c) {
        conditions.add(new LeftGroupByWordCondition(c, ConditionTag.GROUP_BY));
        return (THIS) this;
    }

    /**
     * 分页
     * @return
     */
    public THIS limitPage(int pageNum, int size) {
        this.conditions.add(new LimitWordCondition(pageNum, size));
        return (THIS) this;
    }


    @Override
    public LeftHavingFunImpl<M,M2,C,C2,V,V2,?>  having() {
        LeftHavingFunImpl mcvHavingFun = new LeftHavingFunImpl<>(this.conditions);
        return mcvHavingFun;
    }


    @Override
    @SafeVarargs
    public final THIS orderA(C... cs) {
        List<String> clos = Arrays.stream(cs).map(getterFun -> {
            String cloName = TableHelper.getColNameAndAliasName(getterFun).getLeft();
            return cloName;
        }).collect(Collectors.toList());
        conditions.add(new OrderWordCondition(ConditionTag.ORDER, clos,true));
        return (THIS) this;
    }

    @Override
    @SafeVarargs
    public final THIS orderA(boolean condition, C... cs) {
        if(condition){
            orderA(cs);
        }
        return (THIS) this;
    }

    @Override
    @SafeVarargs
    public final  THIS orderD(C... cs) {
        List<String> clos = Arrays.stream(cs).map(getterFun -> {
            String cloName = TableHelper.getColNameAndAliasName(getterFun).getLeft();
            return cloName;
        }).collect(Collectors.toList());
        conditions.add(new OrderWordCondition(ConditionTag.ORDER, clos,false));
        return (THIS) this;
    }

    @Override
    @SafeVarargs
    public final  THIS orderD(boolean condition, C... cs) {
        if(condition){
            orderD(cs);
        }
        return (THIS) this;
    }

    @SafeVarargs
    public final  THIS orderA(C2... cs) {
        List<String> clos = Arrays.stream(cs).map(getterFun -> {
            String cloName = TableHelper.getColNameAndAliasName(getterFun).getLeft();
            return cloName;
        }).collect(Collectors.toList());
        conditions.add(new OrderWordCondition(ConditionTag.ORDER, clos,true));
        return (THIS) this;
    }

    @SafeVarargs
    public final  THIS orderA(boolean condition, C2... cs) {
        if(condition){
            orderA(cs);
        }
        return (THIS) this;
    }

    @SafeVarargs
    public final  THIS orderD(C2... cs) {
        List<String> clos = Arrays.stream(cs).map(getterFun -> {
            String cloName = TableHelper.getColNameAndAliasName(getterFun).getLeft();
            return cloName;
        }).collect(Collectors.toList());
        conditions.add(new OrderWordCondition(ConditionTag.ORDER, clos,false));
        return (THIS) this;
    }

    @SafeVarargs
    public final THIS orderD(boolean condition, C2... cs) {
        if(condition){
            orderD(cs);
        }
        return (THIS) this;
    }

    @Override
    public List<M> exs() {
        return whereSelectFun.exs();
    }
}
