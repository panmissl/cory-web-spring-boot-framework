package com.cory.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 忽略属性，只读内容，里面的内容可以嵌套，比如：<ROOT><NAME>ZHANGSAN</NAME><AGE>30</AGE></ROOT>
 * 当一个属性只有一个值时，它是一个值，但当有重复的属性时，它的值是一个list，所以调用方要自行处理
 * 只提供了xml2json，如果需要反过来，直接用velocity模板渲染即可，可以使用VelocityService
 * @author cory
 * @date 2023/12/27
 */
@Slf4j
public class XmlUtil {

    /**
     *
     * @param xml
     * @return
     */
    public static Map<String, Object> xml2Json(final String xml) {
        SAXReader reader = new SAXReader();
        try {
            String xml0 = xml.replace("<?XML version=\"1.0\" encoding=\"UTF-8\"?>", "");
            xml0 = xml0.trim();

            ByteArrayInputStream is = new ByteArrayInputStream(xml0.getBytes(StandardCharsets.UTF_8));

            Document document = reader.read(is);
            Element root = document.getRootElement();

            Node rootNode = parseElement(root);
            Map<String, Object> result = new HashMap<>();

            if (null != rootNode.value) {
                result.put(rootNode.name, rootNode.value);
            }

            return result;
        } catch (DocumentException e) {
            log.error("xml 2 json fail, xml: {}", xml, e);
            throw new RuntimeException(e);
        }
    }

    private static Node parseElement(Element element) {
        String name = element.getName();
        List<Element> children = element.elements();

        //目前只处理嵌套节点和直接内容，不支持属性

        if (CollectionUtils.isNotEmpty(children)) {
            Map<String, Object> result = new HashMap<>();
            for (Element child : children) {
                Node childNode = parseElement(child);
                if (null == childNode.value) {
                    continue;
                }

                //如果key在valueMap里还没有，直接设置进去即可
                //如果key在valueMap里已经有了，那么看已有的值是不是list，如果已经是list了，直接添加进去即可，否则把值初始化为list，并且把之前的也加到list里
                Object valueInMap = result.get(childNode.name);
                if (null == valueInMap) {
                    result.put(childNode.name, childNode.value);
                } else {
                    if (valueInMap instanceof List) {
                        ((List) valueInMap).add(childNode.value);
                    } else {
                        List<Object> valueList = new ArrayList<>();
                        valueList.add(valueInMap);
                        valueList.add(childNode.value);
                        result.put(childNode.name, valueList);
                    }
                }
            }
            return new Node(name, result);
        }

        String text = element.getTextTrim();
        if (StringUtils.isNotBlank(text)) {
            return new Node(name, text);
        }

        return new Node(name, null);
    }

    private static class Node {
        String name;
        Object value;

        public Node(String name, Object value) {
            this.name = name;
            this.value = value;
        }
    }
}
