package com.cory.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Map;

/**
 * Created by Cory on 2017/5/22.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccessCountStatVO implements Serializable {

    //今天访问统计
    //昨天访问统计
    //历史访问统计
    //key: url, value: count

    private Map<String, Integer> today;
    private Map<String, Integer> yesterday;
    private Map<String, Integer> total;
}
