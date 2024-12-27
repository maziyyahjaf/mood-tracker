package com.example.maziyyah.mood_tracker.constant;

public class Constant {
    public static final String linkingCodes = "linkingCodes";
    public static final String telegramUserIds = "telegramUserIds";
    public static final String userChatIds = "userChatIds";
 
    public static final String LOVED_ONE_CHAT_ID_FIELD = "lovedOnesChatId";
    public static final String LOVED_ONE_TELEGRAM_STATUS_FIELD = "telegram_status";
    public static final String STREAK_COUNT_FIELD = "streak_count";
    public static final String STREAK_START_DATE_FIELD = "streak_start_date";
    public static final String ALERT_FIELD = "alertThreshold";
    public static final String ENCOURAGEMENT_OPT_IN_FIELD = "encouragementOptIn";

    public static final String USER_KEY_PREFIX = "user:";
    public static final String LOVED_ONE_KEY_PREFIX = "lovedOne:";
    public static final String INVITE_KEY_PREFIX = "invite:";
    public static final String PROCESSED_UPDATE_KEY_PREFIX = "processed_updateID:";
    public static final String USER_REDIS_KEY_LIST = "user:redis_keys";

    public static final String QUEUE_KEY = "encouragement_job_queue";
    public static final int LLM_REQUEST_DAILY_LIMIT = 50;
    
}
