package com.yuan.api.utils;

import com.yupi.yuapicommon.model.vo.InterfaceInfoVO;


/**
 * 代码生成工具类
 */
public class GenerateCodeUtils {

    private static final String JAVA_EXAMPLE = "    @Resource\n" +
            "    private Config config; // baseurl ak sk\n" +
            "\n" +
            "    public void invokeApi() {\n" +
            "        // 非SpringBoot项目可以在代码中注入\n" +
            "        /*Config config = new Config.Builder()\n" +
            "                .setBaseurl(\"${host}\")\n" +
            "                .setAccessKey(\"your accessKey\")\n" +
            "                .setSecretKey(\"your secretKey\")\n" +
            "                .build();*/\n" +
            "\n" +
            "        // 创建client客户端/\n" +
            "        ${client} ${client} = new ${client}(config);\n" +
            "\n" +
            "        // todo 参照开发者文档设置请求必要的参数(doc.ymcapi.xyz)\n" +
            "        // Map<String, Object> paramMap = new HashMap<>();\n" +
            "\n" +
            "        // 注入请求参数\n" +
            "        ${request} ${request} = new ${request}();\n" +
            "        // ${request}.setRequestParams(paramMap);\n" +
            "        \n" +
            "         // 发起请求并获取响应\n" +
            "        try {\n" +
            "            String s = ${client}.${clientMethod}(${request});\n" +
            "            System.err.println(s);\n" +
            "        } catch (YuanapiSdkException e) {\n" +
            "            System.err.println(e.getMessage());\n" +
            "        }\n" +
            "    }";


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

        String javaCode = JAVA_EXAMPLE.replace("${host}", interfaceInfoVO.getHost())
                .replace("${request}", interfaceInfoVO.getRequest())
                .replace("${client}", interfaceInfoVO.getClient())
                .replace("${clientMethod}", interfaceInfoVO.getClientMethod())
                ;

        return javaCode;
    }
}
