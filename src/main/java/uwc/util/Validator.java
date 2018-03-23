package uwc.util;

import java.util.regex.Pattern;

/**
 * 登录工具类
 * Created by dengyulin on 2017/5/3.
 */

public class Validator {
    private static final String numberRule="^1[0-9]{10}$";
    private static final Pattern pattern = Pattern.compile(numberRule);
    /**
     * 检查号码是否符合正确
     * */
    public static final boolean checkPhoneNumber(String number){
        return pattern.matcher(number).matches();
    }

}
