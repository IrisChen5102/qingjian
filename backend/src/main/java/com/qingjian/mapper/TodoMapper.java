package com.qingjian.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.qingjian.entity.Todo;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface TodoMapper extends BaseMapper<Todo> {
}
