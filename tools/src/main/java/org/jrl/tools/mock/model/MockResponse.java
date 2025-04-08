package org.jrl.tools.mock.model;

import java.io.Serializable;

/**
 * mock返回对象
 *
 * @author JerryLong
 */
public class MockResponse<T> implements Serializable {
    private final Boolean isMock;
    private final T result;
    private final Throwable throwable;

    public MockResponse(Boolean isMock, T result, Throwable throwable) {
        this.isMock = isMock;
        this.result = result;
        this.throwable = throwable;
    }

    public Boolean isMock() {
        return isMock;
    }

    public T getResult() {
        if (null != throwable) {
            throw new RuntimeException(throwable);
        }
        return result;
    }

    public Throwable getThrowable() {
        return throwable;
    }

    public static <T> MockResponse<T> noMock() {
        return new MockResponse<>(false, null, null);
    }

    public static <T> MockResponse<T> mock(T result) {
        return new MockResponse<>(true, result, null);
    }

    public static <T> MockResponse<T> mock(Throwable throwable) {
        return new MockResponse<>(true, null, throwable);
    }
}
