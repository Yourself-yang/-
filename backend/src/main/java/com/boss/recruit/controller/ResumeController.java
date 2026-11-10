package com.boss.recruit.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.boss.recruit.common.Result;
import com.boss.recruit.entity.Job;
import com.boss.recruit.entity.Resume;
import com.boss.recruit.entity.User;
import com.boss.recruit.mapper.JobMapper;
import com.boss.recruit.mapper.ResumeMapper;
import com.boss.recruit.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 简历投递接口
 */
@RestController
@RequestMapping("/api/resume")
public class ResumeController {

    @Autowired private ResumeMapper resumeMapper;
    @Autowired private JobMapper jobMapper;
    @Autowired private UserMapper userMapper;

    @PostMapping("/deliver")
    public Result<Resume> deliver(@RequestBody Resume resume) {
        Job job = jobMapper.selectById(resume.getJobId());
        if (job == null) return Result.error("岗位不存在");
        resume.setJobTitle(job.getTitle());
        resume.setCompany(job.getCompany());
        resume.setStatus("PENDING");
        // 自动带出求职者信息
        User u = userMapper.selectById(resume.getUserId());
        if (u != null) {
            resume.setName(u.getRealName());
            resume.setPhone(u.getPhone());
            if (resume.getSkills() == null) resume.setSkills(u.getSkills());
        }
        resumeMapper.insert(resume);
        return Result.success(resume);
    }

    @GetMapping("/mine/{userId}")
    public Result<List<Resume>> mine(@PathVariable Long userId) {
        QueryWrapper<Resume> qw = new QueryWrapper<>();
        qw.eq("user_id", userId).orderByDesc("create_time");
        return Result.success(resumeMapper.selectList(qw));
    }

    @GetMapping("/list")
    public Result<List<Resume>> list(@RequestParam(required = false) Long hrId) {
        QueryWrapper<Resume> qw = new QueryWrapper<>();
        if (hrId != null) {
            qw.inSql("job_id", "select id from job where hr_id=" + hrId);
        }
        qw.orderByDesc("create_time");
        return Result.success(resumeMapper.selectList(qw));
    }

    @PutMapping("/status/{id}")
    public Result<Object> updateStatus(@PathVariable Long id, @RequestParam String status) {
        Resume r = resumeMapper.selectById(id);
        if (r == null) return Result.error("记录不存在");
        r.setStatus(status);
        resumeMapper.updateById(r);
        return Result.success();
    }
}
