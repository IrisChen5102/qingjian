package com.qingjian.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.qingjian.entity.Note;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface NoteMapper extends BaseMapper<Note> {
}
