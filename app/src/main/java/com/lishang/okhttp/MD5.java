package com.lishang.okhttp;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class MD5 {

    /**
     * 利用MD5进行加密
     *
     * @param str 待加密的字符串
     * @return 加密后的字符串
     */
    public static String encoderByMd5(String str) {
        try {
            // 创建加密对象
            MessageDigest digest = MessageDigest.getInstance("md5");

            // 调用加密对象的方法，加密的动作已经完成
            byte[] bs = digest.digest(str.getBytes());
            // 接下来，我们要对加密后的结果，进行优化，按照mysql的优化思路走
            // mysql的优化思路：
            // 第一步，将数据全部转换成正数：
            String hexString = "";
            for (byte b : bs) {
                // 第一步，将数据全部转换成正数：
                // 解释：为什么采用b&255
                                 /*
33                  * b:它本来是一个byte类型的数据(1个字节) 255：是一个int类型的数据(4个字节)
34                  * byte类型的数据与int类型的数据进行运算，会自动类型提升为int类型 eg: b: 1001 1100(原始数据)
35                  * 运算时： b: 0000 0000 0000 0000 0000 0000 1001 1100 255: 0000
36                  * 0000 0000 0000 0000 0000 1111 1111 结果：0000 0000 0000 0000
37                  * 0000 0000 1001 1100 此时的temp是一个int类型的整数
38                  */
                int temp = b & 255;
                // 第二步，将所有的数据转换成16进制的形式
                // 注意：转换的时候注意if正数>=0&&<16，那么如果使用Integer.toHexString()，可能会造成缺少位数
                // 因此，需要对temp进行判断
                if (temp < 16 && temp >= 0) {
                    // 手动补上一个“0”
                    hexString = hexString + "0" + Integer.toHexString(temp);
                } else {
                    hexString = hexString + Integer.toHexString(temp);
                }
            }
            return hexString;
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return "";
    }

    //    public static final boolean DEBUG = BuildConfig.API_MODE != 4 || BuildConfig.API_MODE != 3;
    public static final boolean DEBUG = true;

    //测试环境
    //    public static String appsecret = "2c7995db7dedb37b5ade59fefac2545c";
    //    正式环境

    public static String appsecret = DEBUG ? "2c7995db7dedb37b5ade59fefac2545c" : "88c9319b82ffb558177acca59767fdea";

    public static String encode = "?appversion="
            + "3.3.0"
            + "&os=android";

    public static String encoderByUrl(String str) {

        String Sign = encoderByMd5(str + encode + appsecret);
        return Sign;
    }

}
