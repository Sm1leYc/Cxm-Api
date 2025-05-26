package com.yuan.api.utils;

import com.yupi.yuapicommon.model.vo.InterfaceInfoVO;


/**
 * 代码生成工具类
 */
public class GenerateCodeUtils {

    private static final String JAVA_EXAMPLE = """
        @Resource
        private Config config; // baseurl ak sk

        public void invokeApi() {
            // 非SpringBoot项目可以在代码中注入
            /*Config config = new Config.Builder()
                    .setBaseurl("${host}")
                    .setAccessKey("your accessKey")
                    .setSecretKey("your secretKey")
                    .build();*/

            // 创建client客户端/
            ${client} ${client} = new ${client}(config);

            // todo 参照开发者文档设置请求必要的参数(doc.ymcapi.xyz)
            // Map<String, Object> paramMap = new HashMap<>();

            // 注入请求参数
            ${request} ${request} = new ${request}();
            // ${request}.setRequestParams(paramMap);
            
             // 发起请求并获取响应
            try {
                String s = ${client}.${clientMethod}(${request});
                System.err.println(s);
            } catch (YuanapiSdkException e) {
                System.err.println(e.getMessage());
            }
        }
    """;


    public static String generateJavaCode(InterfaceInfoVO interfaceInfoVO){
        if ("HTTP".equals(interfaceInfoVO.getType()) || "http".equals(interfaceInfoVO.getType())){
            return generateHttpCode(interfaceInfoVO);
        } else {
            return generateSoapCode(interfaceInfoVO);
        }
    }

    public static String generateSoapCode(InterfaceInfoVO interfaceInfoVO){


        return "";
    }

    public static String generateHttpCode(InterfaceInfoVO interfaceInfoVO){

        return JAVA_EXAMPLE.replace("${host}", interfaceInfoVO.getHost())
                .replace("${request}", interfaceInfoVO.getRequest())
                .replace("${client}", interfaceInfoVO.getClient())
                .replace("${clientMethod}", interfaceInfoVO.getClientMethod());
    }
}
