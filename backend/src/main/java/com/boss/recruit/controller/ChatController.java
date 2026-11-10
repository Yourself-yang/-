package com.boss.recruit.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.boss.recruit.common.Result;
import com.boss.recruit.entity.Message;
import com.boss.recruit.mapper.MessageMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 在线对话（聊天）接口
 */
@RestController
@RequestMapping("/api/chat")
public class ChatController {

    @Autowired private MessageMapper messageMapper;

    @GetMapping("/history/{sessionId}")
    public Result<List<Message>> history(@PathVariable String sessionId) {
        QueryWrapper<Message> qw = new QueryWrapper<>();
        qw.eq("session_id", sessionId).orderByAsc("create_time");
        return Result.success(messageMapper.selectList(qw));
    }

    @PostMapping("/send")
    public Result<Message> send(@RequestBody Message msg) {
        // sessionId 用两人 id 小-大拼接，保证双向一致
        Long a = msg.getFromUser(), b = msg.getToUser();
        msg.setSessionId(Math.min(a, b) + "_" + Math.max(a, b));
        msg.setIsRead(0);
        messageMapper.insert(msg);
        return Result.success(msg);
    }

    /** 会话列表：按当前用户聚合最后一条消息 */
    @GetMapping("/sessions/{userId}")
    public Result<List<Message>> sessions(@PathVariable Long userId) {
        QueryWrapper<Message> qw = new QueryWrapper<>();
        qw.and(w -> w.eq("from_user", userId).or().eq("to_user", userId));
        qw.orderByDesc("create_time");
        return Result.success(messageMapper.selectList(qw));
    }
}
