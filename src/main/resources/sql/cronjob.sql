DELETE FROM chat_message
WHERE created < NOW() - INTERVAL '7 days';