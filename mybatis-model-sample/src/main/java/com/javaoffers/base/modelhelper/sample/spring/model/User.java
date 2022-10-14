package com.javaoffers.base.modelhelper.sample.spring.model;

import com.javaoffers.batis.modelhelper.anno.BaseModel;
import com.javaoffers.batis.modelhelper.anno.BaseUnique;
import com.javaoffers.batis.modelhelper.anno.ColName;
import com.javaoffers.batis.modelhelper.anno.fun.noneparam.time.Now;
import com.javaoffers.batis.modelhelper.anno.fun.params.IfGt;
import com.javaoffers.batis.modelhelper.anno.fun.params.IfLt;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@BaseModel
@Data
@Builder
@AllArgsConstructor
public class User {

    /**
     * You can use id to do count(id). Use this object to get the result of count.
     */
    @ColName("id")
    private Long countId;

    @BaseUnique
    private Long id;

    private String name;

    private String birthday;

    private String createTime;

    private String money;

    @Now
    private String now;

    @ColName("money")
    @IfGt(gt = "5000", ep1 = "money", ep2 = "'poor'")
    @IfLt(lt = "10000",ep1 = "'moderately rich'", ep2 = "'very rich'")
    private String moneyDesc;


    private List<UserOrder> orders;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBirthday() {
        return birthday;
    }

    public void setBirthday(String birthday) {
        this.birthday = birthday;
    }

    public List<UserOrder> getOrders() {
        return orders;
    }

    public void setOrders(List<UserOrder> orders) {
        this.orders = orders;
    }

    public User() {
    }

}
