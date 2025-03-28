package com.yuan.api.model.dto.apicallhistory;

import lombok.Data;

@Data
public class LoggingStatusRequest {

    private Long userId;

    private boolean loggingEnabled;
}
