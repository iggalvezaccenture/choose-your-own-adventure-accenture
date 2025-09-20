package com.ignacio.galvez.choose.your.own.adventure.service.impl;

import com.ignacio.galvez.choose.your.own.adventure.dto.StoryResponseDTO;
import com.ignacio.galvez.choose.your.own.adventure.service.ChooseYourOwnAdventureService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.ollama.api.OllamaApi;
import org.springframework.ai.ollama.api.OllamaOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import javax.swing.plaf.IconUIResource;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;


@Slf4j
@Service
@RequiredArgsConstructor
public class ChooseYourOwnAdventureServiceImpl implements ChooseYourOwnAdventureService {

    private final OllamaChatModel ollamaChatModel;
    private final ChatMemory chatMemory;
    private final OllamaApi ollamaApi;
    //private final OpenAiAudioTranscriptionModel openAiAudioTranscriptionModel;


    @Value("classpath:image")
    private Resource imageResource;


    @Value("classpath:audio")
    private Resource audioResource;


    @Value("classpath:templates/storyPage.st")
    private Resource storyPageResource;


    @Value("classpath:templates/decisionParams.st")
    private Resource decisionsParamsResource;


    @Value("classpath:templates/nextPageNumber.st")
    private Resource nextPageResource;


    @Value("classpath:templates/storySynopsis.st")
    private Resource storySynopsisResource;


    @Value("classpath:templates/ending.st")
    private Resource endingResource;


    private final ConcurrentHashMap<String, String> synopsisCache = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, String> pageCache = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, String> decisionParamsCache = new ConcurrentHashMap<>();
    private Map<String, List<Message>> messagesMap = new HashMap<>();
    private static final Map<String, Integer> optionsPerPage;

    private static final Map<String, Integer> totalOptionsCount;

    static {

        totalOptionsCount = new HashMap<>();

        totalOptionsCount.put("short", 5);
        totalOptionsCount.put("medium", 10);
        totalOptionsCount.put("large", 20);

        optionsPerPage = new HashMap<>();
        optionsPerPage.put("low", 2);
        optionsPerPage.put("medium", 3);
        optionsPerPage.put("high", 5);

    }


    @Override
    public StoryResponseDTO createStory(String title,
                                        String characterName,
                                        String characterDescription,
                                        Integer secondaryCharacterCount,
                                        String genre,
                                        String place,
                                        String complexity,
                                        String duration,
                                        String language,
                                        Integer option,
                                        String mediaType) throws IOException {


        PromptTemplate synopsisPromptTemplate = new PromptTemplate(storySynopsisResource);

        String mainCharacter = this.createCharacter(title, characterName, characterDescription, mediaType);
        Prompt synopsisPrompt = synopsisPromptTemplate.create(Map.of("title", title, "main_character", mainCharacter,
                "secondary_character_count", secondaryCharacterCount, "genre", genre, "place", place));

//        String synopsis = "Here's a synopsis for \"The Secret Temple\", a choose your own adventure like novel set in Benares, India:\n" +
//                "\n" +
//                "**Synopsis:**\n" +
//                "\n" +
//                "In the mystical city of Varanasi, where the Ganges River flows and the whispers of ancient secrets linger in the air, Jonas, a young, tall, and thin man with broader intelligence, stumbles upon an enigmatic map that hints at the existence of a hidden temple. This mysterious structure is said to hold the key to unlocking the city's deepest mysteries.\n" +
//                "\n" +
//                "As Jonas sets out to uncover the truth about the secret temple, he meets three individuals who will aid him on his quest:\n" +
//                "\n" +
//                "**Secondary Characters:**\n" +
//                "\n" +
//                "1. **Rajni**, a wise and enigmatic elderly woman who possesses ancient knowledge of Benares' mystical forces. Her eyes seem to hold secrets and her words are laced with subtle hints.\n" +
//                "2. **Kavi**, a charming and quick-witted street artist, whose vibrant paintings may hide more than meets the eye. His carefree nature belies a sharp mind that can decipher hidden symbols.\n" +
//                "3. **Professor Mishra**, a renowned scholar of Benares' history and mythology. His keen insight into ancient texts might prove invaluable in deciphering the secrets of the secret temple.\n" +
//                "\n" +
//                "As Jonas navigates the winding streets of Varanasi, he encounters puzzles, cryptic messages, and eerie omens that draw him closer to the mysterious temple. With the help of his new allies, he must solve the enigmas and unravel the threads of a centuries-old mystery hidden within the city's ancient walls.\n" +
//                "\n" +
//                "Will Jonas uncover the secrets of the secret temple? The journey begins here...\n" +
//                "\n" +
//                "**Choose Your Own Adventure:**\n" +
//                "\n" +
//                "* Explore the winding streets of Varanasi with Jonas\n" +
//                "* Uncover the truth about Rajni, Kavi, and Professor Mishra\n" +
//                "* Decipher the cryptic messages and puzzles leading to the secret temple\n" +
//                "* Make choices that shape the story and its outcome\n" +
//                "\n" +
//                "Embark on this thrilling adventure and discover the secrets hidden within Benares' mystical heart!";
        String synopsis = synopsisCache.computeIfAbsent(title, key ->
                ChatClient.builder(ollamaChatModel)
                        .defaultOptions(OllamaOptions
                                .builder()
                                .temperature(1.0)
                                .build())
                        .build()
                        .prompt(synopsisPrompt)
                        .call()
                        .content());


        if (!messagesMap.containsKey(title)) {
            messagesMap.put(title, new ArrayList<>());
        }


        String currentPage = "";
        if (chatMemory.get(title).isEmpty() || chatMemory.get(title).size() < totalOptionsCount.get(duration)) {

            PromptTemplate nextPagePromptTemplate = new PromptTemplate(storyPageResource);


            Prompt pagePrompt = nextPagePromptTemplate.create(Map
                    .of("title", title,
                            "language", language,
                            "synopsis", synopsis
                            , "option", option, "total_page_count", (long) chatMemory.get(title).size() + 1
                            , "options", optionsPerPage.get(complexity.toLowerCase())));


            String nextPage = ChatClient
                    .builder(ollamaChatModel)
                    .defaultOptions(OllamaOptions
                            .builder()
                            .temperature(
                                    0.7
                            )
                            .build()).defaultAdvisors(MessageChatMemoryAdvisor
                            .builder(chatMemory)
                            .build())
                    .build()
                    .prompt(pagePrompt)
                    .advisors(MessageChatMemoryAdvisor
                            .builder(chatMemory)
                            .build())
                    .call()
                    .content();

            currentPage = nextPage;

            chatMemory.add(title, Collections.singletonList(pagePrompt.getUserMessage()));
            return new StoryResponseDTO(nextPage); //synopsis);
        }

        PromptTemplate endingPromptTemplate = new PromptTemplate(endingResource);

        Prompt endPrompt = endingPromptTemplate.create(Map.
                of("title", title,
                "synopsis", synopsis,
                "story_page", currentPage,
                "language", language));
        return new StoryResponseDTO(ChatClient.builder(ollamaChatModel)
                .defaultOptions(OllamaOptions
                        .builder()
                        .temperature(0.7)
                        .build())
                .build()
                .prompt(
                        endPrompt
                ).call().content());

    }


    private String createCharacter(String title,
                                   String name,
                                   String description,
                                   String mediaType) throws IOException {
        Map<String, Object> map = new HashMap<>();

        if ("text".equalsIgnoreCase(mediaType)) {
            return STR."\{name},\{description}";
        }
//        if ("image".equalsIgnoreCase(mediaType)) {
//            Prompt imagePrompt = getPrompt(title);
//
//            return ChatClient.builder(OpenAiChatModel.builder().openAiApi(openAiApi)
//                    .defaultOptions(
//                            OpenAiChatOptions
//                                    .builder()
//                                    .model("dall-e-3")
//                                    .build())
//                    .build()
//            ).build()
//                    .prompt(imagePrompt)
//                    .call()
//                    .content();
//        }
//        if ("audio".equalsIgnoreCase(mediaType)) {
//
//            FileSystemResource resource = new FileSystemResource(STR."\{audioResource.getFile().getPath()}/\{title}");
//            AudioTranscriptionPrompt audioTranscriptionPrompt = new AudioTranscriptionPrompt(resource, OpenAiAudioTranscriptionOptions
//                    .builder()
//                    .prompt(
//                    "Extract the person name and description from this audio in the following format: name,description")
//                    .build());
//            return this.openAiAudioTranscriptionModel
//                    .call(audioTranscriptionPrompt)
//                    .getResult()
//                    .getOutput();
//        }
        return null;

    }

//    private Prompt getPrompt(String title) throws IOException {
//        FileSystemResource image = new FileSystemResource(Path.of(STR."\{imageResource.getFile().getPath()}/\{title}"));
//        String imageExtensionFile = StringUtils.getFilenameExtension(imageResource.getFilename());
//
//
//        UserMessage userMessage = new UserMessage(
//                "write the description of the character of the image and the name in the top of the image " +
//                        "in the following format: name,description",
//                List.of(new Media(MimeTypeUtils.parseMimeType(Objects.requireNonNull(imageExtensionFile)), image))
//        );
//        return new Prompt(userMessage);
//    }
}
