package work.work4.common;

import lombok.Data;

@Data
public class Result {
    private String code;
    private String msg;
    private Object data;
    public Result() {
    }
//    请求成功
    public static Result success() {
        Result result = new Result();
        result.setCode("200");
        result.setMsg("请求成功");
        return result;
    }
    public static Result success(Object data) {
        Result result = success();
        result.setData(data);
        return result;
    }
//    请求失败
    public static Result error() {
        Result result = new Result();
        result.setCode("500");
        result.setMsg("请求失败");
        return result;
    }
    public static Result error(String msg) {
        Result result = new Result();
        result.setCode("500");
        result.setMsg(msg);
        return result;
    }
}
