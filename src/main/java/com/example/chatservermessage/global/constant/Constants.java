package com.example.chatservermessage.global.constant;

public final class Constants {
    private Constants() {
    }

    public static final String CHAT_DESTINATION = "/sub/chat/";
    public static final String REDIS_CHAT_PREFIX = "chat_";
    public static final String COOKIE_AUTH_HEADER = "Authorization";
    public static final String REDIS_REFRESH_KEY = "REFRESH_TOKEN:";
    public static final String REDIS_ACCESS_KEY = "ACCESS_TOKEN:";
    public static final String REDIS_SUBSCRIBE_KEY = "SUBSCRIBE:";
    public static final String REDIS_MAX_PERSONNEL_KEY = "MAX_PERSONNEL:";
    public static final String BEARER_PREFIX = "Bearer ";

    // kafka 상수
    public static final String KAFKA_USER_TO_CHAT_TOPIC = "email";  // chat 인스턴스에 전파하기 위한 토픽
    public static final String KAFKA_USER_DATA_KEY = "username"; // 동시성 해시 맵의 key 값
    public static final String KAFKA_USER_TO_CHAT_GROUP_ID = "dev";
    public static final String KAFKA_OTHER_TO_USER_TOPIC = "authorization";
}
