package com.cory.dto;

import lombok.Data;

/**
 * @author cory
 * @date 2022/2/28
 */
@Data
public class AccessCountStatDTO extends BaseDTO {

    private String uri;
    private String count;
}
