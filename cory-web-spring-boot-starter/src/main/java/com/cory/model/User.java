package com.cory.model;

import com.cory.db.annotations.Field;
import com.cory.db.annotations.Model;
import com.cory.db.enums.CoryDbType;
import com.cory.enums.UserLevel;
import com.cory.enums.UserStatus;
import com.cory.enums.UserType;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.List;

/**
 * generated by CodeGenerator on 2017/5/10.
 */
@Data
@Model(name = "用户", module = "base")
public class User extends BaseModel {

    @Field(label = "用户名", type = CoryDbType.VARCHAR, len = 100, nullable = true, desc = "可以用来登录系统的，是英文、数字或下划线")
    private String userName;

    @NotEmpty
    @Field(label = "密码", type = CoryDbType.VARCHAR, desc = "密码不能更新（因为是加密的），必须由登录用户自己修改密码，添加用户时默认密码为：123456", showable = false)
    private String password;

    @Field(label = "昵称", type = CoryDbType.VARCHAR, len = 100, nullable = true, desc = "显示用的，一般是中文")
    private String nickName;

    @Field(label = "电话", type = CoryDbType.VARCHAR, len = 100, nullable = true, desc = "也可以用来登录系统")
    private String phone;

    @Field(label = "邮箱", type = CoryDbType.VARCHAR, len = 100, nullable = true, desc = "也可以用来登录系统")
    private String email;

    @Field(label = "第三方账号ID", type = CoryDbType.VARCHAR, len = 200, nullable = true, desc = "没有可以留空")
    private String thirdpartyId;

    @Field(label = "第三方账号类型", type = CoryDbType.VARCHAR, len = 100, nullable = true, desc = "没有可以留空")
    private String thirdpartyType;

    @NotNull
    @Field(label = "类型", type = CoryDbType.ENUM, len = 50)
    private UserType type;

    @Field(label = "状态", type = CoryDbType.ENUM, len = 50)
    private UserStatus status;

    @Field(label = "级别", type = CoryDbType.ENUM, len = 50)
    private UserLevel level;

    @Field(label = "额外信息", type = CoryDbType.VARCHAR, showable = false, len = 20480, nullable = true)
    private String extraInfo;

    @Field(label = "最近登录时间", type = CoryDbType.DATETIME, nullable = true)
    private Date lastLogonTime;

    @Field(label = "最近登录IP", type = CoryDbType.VARCHAR, nullable = true, len = 100)
    private String lastLogonIp;

    @Field(label = "最近登录是否成功", type = CoryDbType.BOOLEAN, defaultValue = "1")
    private Boolean lastLogonSuccess;

    private List<Role> roles;

    public String getLogonId() {
        if (UserType.QQ.equals(this.getType())
                || UserType.WEIBO.equals(this.getType())
                || UserType.WEIXIN.equals(this.getType())) {
            return thirdpartyId;
        }
        return phone;
    }

    public boolean isDisabled() {
        if (UserStatus.NORMAL.equals(this.getStatus())) {
            return false;
        } else {
            return true;
        }
    }
}
