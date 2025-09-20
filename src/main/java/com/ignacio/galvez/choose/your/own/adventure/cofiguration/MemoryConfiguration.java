package com.ignacio.galvez.choose.your.own.adventure.cofiguration;

import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.context.annotation.Bean;

public class MemoryConfiguration {

    @Bean
    public ChatMemory chatMemory() {
        // Use in-memory storage for short‑term memory
        return  MessageWindowChatMemory.builder()
                .maxMessages(20)  // window of most recent 20 messages (default)
                .build();
    }
}
