package com.boss.recruit.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.boss.recruit.common.Result;
import com.boss.recruit.entity.Job;
import com.boss.recruit.entity.User;
import com.boss.recruit.mapper.JobMapper;
import com.boss.recruit.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * 智能匹配助手：基于技能标签的关键词权重匹配算法（协同过滤思想简化版）
 * 评分 = 技能标签命中数 * 20 + 城市匹配 * 10 + 经验匹配 * 10
 */
@RestController
@RequestMapping("/api/match")
public class MatchController {

    @Autowired private JobMapper jobMapper;
    @Autowired private UserMapper userMapper;

    @GetMapping("/jobs/{userId}")
    public Result<List<Map<String, Object>>> matchJobs(@PathVariable Long userId) {
        User seeker = userMapper.selectById(userId);
        if (seeker == null) return Result.error("用户不存在");
        Set<String> seekerSkills = split(seeker.getSkills());

        QueryWrapper<Job> qw = new QueryWrapper<>();
        qw.eq("status", "OPEN");
        List<Job> jobs = jobMapper.selectList(qw);

        List<Map<String, Object>> result = new ArrayList<>();
        for (Job job : jobs) {
            Set<String> jobTags = split(job.getTags());
            // 命中的技能数
            int hit = 0;
            List<String> hitTags = new ArrayList<>();
            for (String t : seekerSkills) {
                for (String jt : jobTags) {
                    if (jt.toLowerCase().contains(t.toLowerCase()) || t.toLowerCase().contains(jt.toLowerCase())) {
                        hit++; hitTags.add(jt); break;
                    }
                }
            }
            int score = hit * 20;
            // 经验匹配：应届/实习岗位加分
            if (job.getExperience() != null && job.getExperience().contains(seeker.getExperience() != null ? seeker.getExperience() : "")) {
                score += 10;
            }
            if (score <= 0 && hit == 0) continue;
            Map<String, Object> item = new HashMap<>();
            item.put("job", job);
            item.put("score", Math.min(score, 100));
            item.put("hitTags", hitTags);
            result.add(item);
        }
        result.sort((a, b) -> (Integer) b.get("score") - (Integer) a.get("score"));
        return Result.success(result);
    }

    private Set<String> split(String s) {
        Set<String> set = new HashSet<>();
        if (s == null || s.isEmpty()) return set;
        for (String t : s.split("[,，、\\s]+")) {
            if (!t.trim().isEmpty()) set.add(t.trim());
        }
        return set;
    }
}
