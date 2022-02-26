package com.cory.vo;

import com.cory.context.CorySystemContext;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * Created by Cory on 2017/5/22.
 */
@Data
@Builder
public class UserVO implements Serializable {

    private Integer id;
    private String logonId;
    private String userName;
    private String nickName;
    private String phone;
    private String email;
    private String role;
    private String avatar;
    private Set<String> resources;
    private List<CorySystemContext.ModelMeta> modelMetaList;
    private Set<CorySystemContext.EnumMeta> enumMetaSet;
    private Date lastLogonTime;
    private String lastLogonIp;
    private Boolean lastLogonSuccess;
}
