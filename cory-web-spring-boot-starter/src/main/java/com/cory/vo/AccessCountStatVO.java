package com.cory.vo;

import com.cory.dto.AccessCountStatDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * Created by Cory on 2017/5/22.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccessCountStatVO implements Serializable {

    private static final long serialVersionUID = -4619816468274153356L;

    //今天访问统计 top10
    //昨天访问统计 top10
    //历史访问统计 top10

    private List<AccessCountStatDTO> today;
    private List<AccessCountStatDTO> yesterday;
    private List<AccessCountStatDTO> total;
}
