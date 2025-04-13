package com.yuan.api.constant;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@Data
@ConfigurationProperties(prefix = "task.cleanup")
public class TaskCleanupProperties {
    private boolean enabled; // 是否启用任务
    private int days;        // 删除几天前的数据

}
