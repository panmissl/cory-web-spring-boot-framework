package com.cory.dto;

import lombok.Data;

/**
 * @author cory
 * @date 2022/2/28
 */
@Data
public class AccessCountStatDTO extends BaseDTO {

    private static final long serialVersionUID = 1527236106080336008L;

    private String uri;
    private String count;
}
