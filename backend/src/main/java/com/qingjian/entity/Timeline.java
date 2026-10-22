package com.qingjian.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("timeline")
public class Timeline {

    @TableId(type = IdType.AUTO)
    private Integer id;

    /** 饮食 / 娱乐 / 记账 / 随手记 */
    private String type;

    private String title;

    private String detail;

    private String url;

    private BigDecimal amount;

    /** 标签：歌手 / 平台 / 游戏名等 */
    private String tag;

    private LocalDateTime happenedAt;

    /** 关联的详细笔记 */
    private Integer noteId;
}
