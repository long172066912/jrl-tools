package org.jrl.tools.event;


/**
 * @author JerryLong
 * 事件数据
 */
public class JrlEventData<T> {
    /**
     * 如果指定监听器名称，则只通知该监听器
     */
    private String listenerName;
    /**
     * 数据
     */
    private T data;

    public JrlEventData(String listenerName, T data) {
        this.listenerName = listenerName;
        this.data = data;
    }

    public String getListenerName() {
        return listenerName;
    }

    public void setListenerName(String listenerName) {
        this.listenerName = listenerName;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}
