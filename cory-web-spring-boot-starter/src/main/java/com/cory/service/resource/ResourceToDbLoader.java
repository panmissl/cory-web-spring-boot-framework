package com.cory.service.resource;

import com.cory.enums.ResourceType;
import com.cory.model.Resource;
import com.cory.service.ResourceService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.Set;

/**
 * Created by Cory on 2017/5/21.
 */
@Slf4j
@Repository
public class ResourceToDbLoader {

    @Autowired
    private ResourceService resourceService;

    public int loadToDb(Set<String> urls) {
        if (CollectionUtils.isEmpty(urls)) {
            return 0;
        }

        int count = 0;
        for (String url : urls) {
            //单独处理首页的url，因为这个不能加通配符，否则就和全局的那个重复了
            if (!"/".equals(url)) {
                url = url + "*";
            }
            //如果查不到，那么插入，如果查到了就跳过
            if (null == resourceService.getByValue(url)) {
                Resource r = new Resource();
                r.setDescription(url);
                r.setType(ResourceType.URL);
                r.setValue(url);
                r.setCreator(1);
                r.setModifier(1);
                try {
                    resourceService.add(r);
                } catch (Throwable t) {
                    log.error("add resource fail, url: {}", url, t);
                    //ignore
                }
                count ++;
            }
        }
        return count;
    }
}
