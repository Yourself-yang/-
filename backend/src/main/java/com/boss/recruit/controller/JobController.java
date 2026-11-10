package com.boss.recruit.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.boss.recruit.common.Result;
import com.boss.recruit.entity.Job;
import com.boss.recruit.mapper.JobMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 岗位接口：列表、详情、发布、搜索
 */
@RestController
@RequestMapping("/api/job")
public class JobController {

    @Autowired
    private JobMapper jobMapper;

    @GetMapping("/list")
    public Result<List<Job>> list(@RequestParam(required = false) String keyword,
                                  @RequestParam(required = false) String city) {
        QueryWrapper<Job> qw = new QueryWrapper<>();
        qw.eq("status", "OPEN");
        if (keyword != null && !keyword.isEmpty()) {
            qw.and(w -> w.like("title", keyword).or().like("tags", keyword).or().like("company", keyword));
        }
        if (city != null && !city.isEmpty()) {
            qw.eq("city", city);
        }
        qw.orderByDesc("create_time");
        return Result.success(jobMapper.selectList(qw));
    }

    @GetMapping("/{id}")
    public Result<Job> detail(@PathVariable Long id) {
        return Result.success(jobMapper.selectById(id));
    }

    @PostMapping("/publish")
    public Result<Job> publish(@RequestBody Job job) {
        job.setStatus("OPEN");
        jobMapper.insert(job);
        return Result.success(job);
    }

    @GetMapping("/my/{hrId}")
    public Result<List<Job>> myJobs(@PathVariable Long hrId) {
        QueryWrapper<Job> qw = new QueryWrapper<>();
        qw.eq("hr_id", hrId).orderByDesc("create_time");
        return Result.success(jobMapper.selectList(qw));
    }
}
