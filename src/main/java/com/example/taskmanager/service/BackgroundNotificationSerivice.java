package com.example.taskmanager.service;

import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;

import org.springframework.stereotype.Service;

@Service
public class BackgroundNotificationSerivice {
    private Map<Long, List<String>> messagesMap = new HashMap<>();

    public void addMessage(Long userId, String message) {
        if (!messagesMap.containsKey(userId)) {
            messagesMap.put(userId, new ArrayList<>());
        }
        messagesMap.get(userId).add(message);
    }

    public List<String> popMessagesByUserId(Long userId) {
        List<String> messages = messagesMap.getOrDefault(userId, new ArrayList<>());
        messagesMap.remove(userId);
        return messages;
    }

    public void removeMessagesByUserId(Long userId) {
        messagesMap.remove(userId);
    }
    
    public void clearMessages() {
        messagesMap.clear();
    }
}