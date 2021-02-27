package com.cory.context;

import com.alibaba.ttl.TransmittableThreadLocal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Created by Cory on 2017/5/20.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CurrentUser implements Serializable {

    private static final long serialVersionUID = -7198447036068020479L;

    private static final TransmittableThreadLocal<CurrentUser> THREAD_LOCAL = new TransmittableThreadLocal<>();

    private static final CurrentUser EMPTY_USER = CurrentUser.builder().build();

    private Integer id = 0;
    private String principal;
    private boolean isAdmin = false;
    private boolean isRoot = false;

    public static CurrentUser get() {
        CurrentUser user = THREAD_LOCAL.get();
        return null == user ? EMPTY_USER : user;
    }

    public static void set(CurrentUser currentUser) {
        THREAD_LOCAL.set(currentUser);
    }

    public static void remove() {
        THREAD_LOCAL.remove();
    }

}
