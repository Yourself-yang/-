package com.boss.recruit.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("resume")
public class Resume {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Long jobId;
    private String jobTitle;
    private String company;
    private String status;   // PENDING / INTERVIEW / PASSED / REJECTED
    private String name;
    private String phone;
    private String skills;
    private String selfIntro;
    private LocalDateTime createTime;
}
