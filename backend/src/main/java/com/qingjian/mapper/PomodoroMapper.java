package com.qingjian.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.qingjian.entity.Pomodoro;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface PomodoroMapper extends BaseMapper<Pomodoro> {
}
