package com.qingjian.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("todo")
public class Todo {

    @TableId(type = IdType.AUTO)
    private Integer id;

    private String title;

    /** 高 / 中 / 低 */
    private String priority;

    private LocalDate dueDate;

    /** 待办 / 已办 */
    private String status;

    private String note;

    private LocalDateTime createdAt;

    private LocalDateTime completedAt;
}
