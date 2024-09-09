package org.example.worktest.entity;


import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;
import lombok.EqualsAndHashCode;


@EqualsAndHashCode(callSuper = true)
@DS("datax_test")
@TableName("`order`")
@Data
public class Order extends Model<Order> {

    private Integer id;

    private String orderNumber;

    private Integer number;

    private Integer status;

}
