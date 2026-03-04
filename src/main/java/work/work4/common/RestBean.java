package work.work4.common;

import lombok.Data;

import java.io.Serializable;

@Data
public class RestBean<T> implements Serializable {
    private Base base;
    private T data; //数据

    public static <T> RestBean<T> success() {
        RestBean<T> result = new RestBean<>();
        Base base = new Base();
        base.setCode(10000);
        base.setMsg("success");
        result.setBase(base);
        return result;
    }

    public static <T> RestBean<T> success(T data) {
        RestBean<T> bean = new RestBean<>();
        Base base = new Base();
        base.setCode(10000);
        base.setMsg("success");
        bean.setBase(base);
        bean.setData(data);
        return bean;
    }

    public static <T> RestBean<T> error(String msg) {
        RestBean<T> result = new RestBean<>();
        Base base = new Base();
        base.setCode(-1);
        base.setMsg(msg);
        result.setBase(base);
        return result;
    }
}
