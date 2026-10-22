package com.qingjian.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("pomodoro")
public class Pomodoro {

    @TableId(type = IdType.AUTO)
    private Integer id;

    private Integer minutes;

    private LocalDateTime startedAt;

    private LocalDateTime endedAt;

    private String note;
}
