package org.example.worktest.mapper;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.example.worktest.entity.Order;

@DS("datax_test")
@Mapper
public interface OrderMapper extends BaseMapper<Order> {
}
