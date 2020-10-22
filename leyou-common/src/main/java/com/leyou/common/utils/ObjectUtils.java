package com.leyou.common.utils;

import java.lang.reflect.Field;

public class ObjectUtils {

    private ObjectUtils() {
    }

    /**
     * 判断类中每个属性是否都为空
     *
     * @param o
     * @return
     */
    public static boolean allFieldIsNULL(Object o){
        try {
            for (Field field : o.getClass().getDeclaredFields()) {
                field.setAccessible(true);

                Object object = field.get(o);
                if (object instanceof CharSequence) {
                    if (!org.springframework.util.ObjectUtils.isEmpty(object)) {
                        return false;
                    }
                } else {
                    if (null != object) {
                        return false;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("ObjectUtils工具类异常");
        }
        return true;
    }

}