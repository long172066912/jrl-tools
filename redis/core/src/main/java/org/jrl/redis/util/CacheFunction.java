package org.jrl.redis.util;

import java.io.Serializable;
import java.lang.invoke.SerializedLambda;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
/**
* @Title: CacheFunction
* @Description: 自定义方法
* @author JerryLong
* @date 2021/2/23 3:06 PM
* @version V1.0
*/
@FunctionalInterface
public interface CacheFunction extends Serializable {

    /**
     * 这里只接收无参方法
     *
     * @return
     * @throws Exception
     */
    Object apply() throws Exception;

    /**
     * 这个方法返回的SerializedLambda是重点
     * @return
     * @throws Exception
     */
    default SerializedLambda getSerializedLambda() throws Exception {
        SerializedLambda obj = (SerializedLambda) CacheCommonUtils.cacheFunctionClassMap.get(this.getClass().getName());
        if(null == obj){
            //writeReplace改了好像会报异常
            Method write = this.getClass().getDeclaredMethod("writeReplace");
            write.setAccessible(true);
            CacheCommonUtils.cacheFunctionClassMap.put(this.getClass().getName(),(SerializedLambda) write.invoke(this));
        }
        return obj;
    }

    /**
     * getImplClass
     * @return
     */
    default String getImplClass() {
        try {
            return getSerializedLambda().getImplClass();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * getImplMethodName
     * @return
     */
    default String getImplMethodName() {
        try {
            return getSerializedLambda().getImplMethodName();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 获取方法名称
     * @return
     */
    default String fnToFnName() {
        try {
            String[] split = getSerializedLambda().getImplMethodName().split("\\$", 3);
            return split[1];
        } catch (Exception e) {
        }
        return "";
    }

    /**
     * 获取方法参数，反射已缓存，每次拿到的参数都是第一次调用时的
     * @return
     */
    @Deprecated
    default List<Object> getFuncFields() {
        try {
            SerializedLambda serializedLambda = getSerializedLambda();
            List<Object> list = new ArrayList<>(serializedLambda.getCapturedArgCount());
            for (int i = 0; i < serializedLambda.getCapturedArgCount(); i++) {
                if (!serializedLambda.getCapturedArg(i).toString().startsWith("com.")) {
                    list.add(serializedLambda.getCapturedArg(i));
                }
            }
            return list;
        } catch (Exception e) {
        }
        return new ArrayList<>();
    }
}
