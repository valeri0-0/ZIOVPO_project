package com.valeria.signature.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SignPayload {

    private String threatName;

    private String firstBytesHex;

    private String remainderHashHex;

    private long remainderLength;

    private String fileType;

    private long offsetStart;

    private long offsetEnd;

    private Object status;
}