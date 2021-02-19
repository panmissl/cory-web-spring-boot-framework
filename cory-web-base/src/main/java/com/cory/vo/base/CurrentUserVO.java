package com.cory.vo.base;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.util.List;
import java.util.Set;

/**
 * Created by Cory on 2021/2/19.
 */
@Data
@Builder
public class CurrentUserVO implements Serializable {

    private String phone;
    private String email;
    private String thirdpartyId;
    private String thirdpartyType;
    private String type;
    private String status;
    private String level;

    private List<String> roles;
    private Set<String> resources;
}
