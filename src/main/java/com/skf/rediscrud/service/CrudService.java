package com.skf.rediscrud.service;

import com.skf.rediscrud.pojo.ContentSlice;

public interface CrudService {

    void putContent(String content);

    void putLabel(Long ts);

    String getLast();

    ContentSlice getByTime(Long start, Long end);

}
