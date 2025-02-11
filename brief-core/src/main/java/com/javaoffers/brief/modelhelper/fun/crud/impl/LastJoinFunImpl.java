package com.javaoffers.brief.modelhelper.fun.crud.impl;

import com.javaoffers.brief.modelhelper.fun.AggTag;
import com.javaoffers.brief.modelhelper.fun.Condition;
import com.javaoffers.brief.modelhelper.fun.ConditionTag;
import com.javaoffers.brief.modelhelper.fun.GetterFun;
import com.javaoffers.brief.modelhelper.fun.condition.JoinTableCondition;
import com.javaoffers.brief.modelhelper.fun.condition.select.SelectColumnCondition;
import com.javaoffers.brief.modelhelper.fun.crud.LastJoinFun;
import com.javaoffers.brief.modelhelper.fun.crud.LastOnFun;
import com.javaoffers.brief.modelhelper.utils.TableHelper;
import org.apache.commons.lang3.tuple.Pair;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Stream;

/**
 * @author mingJie
 */
public class LastJoinFunImpl<M1,M2, M3, C3 extends GetterFun<M3,Object>, V> implements LastJoinFun<M1,M2,M3,C3,V> {

    private LinkedList<Condition> conditions;
    private Class<M1> m1Class;
    private Class<M2> m2Class;
    private Class<M3> m3Class;
    private String table3Name;

    public LastJoinFunImpl(Class<M1> m1Class, Class<M2> m2Class, Class<M3> m3Class, LinkedList<Condition> conditions,  ConditionTag tag) {
        this.conditions = conditions;
        this.m1Class = m1Class;
        this.m2Class = m2Class;
        this.m3Class = m3Class;
        this.table3Name = TableHelper.getTableName(m3Class);
        this.conditions.add(new JoinTableCondition(this.table3Name,tag));
    }

    @Override
    @SafeVarargs
    public final LastJoinFunImpl<M1, M2, M3, C3, V> col(C3... cols) {
        Stream.of(cols).forEach(col->{conditions.add(new SelectColumnCondition( col));});
        return this;
    }

    @Override
    @SafeVarargs
    public final LastJoinFun<M1, M2, M3, C3, V> col(boolean condition, C3... cols) {
        if(condition){
            col(cols);
        }
        return this;
    }

    @Override
    @SafeVarargs
    public final LastJoinFun<M1, M2, M3, C3, V> col(AggTag aggTag, C3... cols) {
        Stream.of(cols).forEach(col->{
            Pair<String, String> colAgg = TableHelper.getSelectAggrColStatement(col);
            this.conditions.add(new SelectColumnCondition(aggTag.name()+"("+colAgg.getLeft()+") as " + colAgg.getRight()));
        });
        return this;
    }

    @Override
    @SafeVarargs
    public final LastJoinFun<M1, M2, M3, C3, V> col(boolean condition, AggTag aggTag, C3... cols) {
        if(condition){
            col(aggTag,cols);
        }
        return this;
    }

    @Override
    public LastJoinFun<M1, M2, M3, C3, V> col(AggTag aggTag, C3 col, String asName) {
        Pair<String, String> colNameAndAliasName = TableHelper.getColNameAndAliasName(col);
        String colName = colNameAndAliasName.getLeft();
        conditions.add(new SelectColumnCondition(aggTag.name()+"("+colName+") as "
                //join table need SimpleName+asName for diff with main table.
                + this.m3Class.getSimpleName() + asName));
        return this;
    }

    @Override
    public LastJoinFun<M1, M2, M3, C3, V> col(boolean condition, AggTag aggTag, C3 col, String asName) {
        if(condition){
            col(aggTag, col, asName);
        }
        return this;
    }

    @Override
    public LastJoinFun<M1, M2, M3, C3, V> colAll() {
        List<SelectColumnCondition> colAll = TableHelper.getColAllForSelect(m3Class, SelectColumnCondition::new);
        conditions.addAll(colAll);
        return this;
    }

    @Override
    @SafeVarargs
    public final LastJoinFun<M1, M2, M3, C3, V> col(String... colSql) {
        Stream.of(colSql).forEach(col->{conditions.add(new SelectColumnCondition( col));});
        return this;
    }

    @Override
    public <C2 extends GetterFun<M2, Object>> LastOnFun<M1, M2, M3, C2, C3, V> on() {
        return new LastOnFunImpl(this.conditions);
    }
}
