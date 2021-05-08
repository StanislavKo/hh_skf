package com.skf.rediscrud.service;

import com.skf.rediscrud.pojo.Visit;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

public interface AntiFloodService {

    boolean isOk(HttpServletRequest req, Map<String, Visit> visits);

}
