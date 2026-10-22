package com.qingjian.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("daily_summary")
public class DailySummary {

    @TableId(value = "log_date", type = IdType.INPUT)
    private LocalDate logDate;

    /** 好 / 一般 / 差 */
    private String mood;

    private String tomorrowFocus;

    private LocalDateTime updatedAt;
}
