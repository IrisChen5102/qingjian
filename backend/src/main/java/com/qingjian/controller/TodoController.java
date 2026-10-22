package com.qingjian.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.qingjian.entity.Todo;
import com.qingjian.mapper.TodoMapper;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/todos")
public class TodoController {

    private final TodoMapper todoMapper;

    public TodoController(TodoMapper todoMapper) {
        this.todoMapper = todoMapper;
    }

    @GetMapping
    public List<Todo> list(@RequestParam(required = false) String date) {
        LambdaQueryWrapper<Todo> query = new LambdaQueryWrapper<>();
        if (date != null && !date.isBlank()) {
            query.eq(Todo::getDueDate, LocalDate.parse(date));
        }
        query.orderByAsc(Todo::getDueDate).orderByDesc(Todo::getId);
        return todoMapper.selectList(query);
    }

    @PostMapping
    public Todo create(@RequestBody Todo todo) {
        if (todo.getStatus() == null) {
            todo.setStatus("待办");
        }
        if (todo.getDueDate() == null) {
            todo.setDueDate(LocalDate.now());
        }
        if (todo.getPriority() == null) {
            todo.setPriority("中");
        }
        todo.setId(null);
        todo.setCreatedAt(LocalDateTime.now());
        if ("已办".equals(todo.getStatus()) && todo.getCompletedAt() == null) {
            todo.setCompletedAt(LocalDateTime.now());
        }
        todoMapper.insert(todo);
        return todo;
    }

    @PutMapping("/{id}")
    public Todo update(@PathVariable Integer id, @RequestBody Todo todo) {
        todo.setId(id);
        todoMapper.updateById(todo);
        return todoMapper.selectById(id);
    }

    @DeleteMapping("/{id}")
    public Map<String, Boolean> delete(@PathVariable Integer id) {
        todoMapper.deleteById(id);
        return Map.of("ok", true);
    }

    @PutMapping("/{id}/status")
    public Todo setStatus(@PathVariable Integer id, @RequestBody Map<String, String> body) {
        String status = body.getOrDefault("status", "已办");
        boolean done = "已办".equals(status);
        todoMapper.update(null, new LambdaUpdateWrapper<Todo>()
                .eq(Todo::getId, id)
                .set(Todo::getStatus, status)
                .set(Todo::getCompletedAt, done ? LocalDateTime.now() : null));
        return todoMapper.selectById(id);
    }

    @PutMapping("/{id}/postpone")
    public Todo postpone(@PathVariable Integer id, @RequestBody Map<String, String> body) {
        todoMapper.update(null, new LambdaUpdateWrapper<Todo>()
                .eq(Todo::getId, id)
                .set(Todo::getDueDate, LocalDate.parse(body.get("date"))));
        return todoMapper.selectById(id);
    }
}
