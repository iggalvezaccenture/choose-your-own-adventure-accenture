package com.ignacio.galvez.choose.your.own.adventure.service;

import com.ignacio.galvez.choose.your.own.adventure.dto.StoryResponseDTO;

import java.io.IOException;

public interface ChooseYourOwnAdventureService {


    StoryResponseDTO createStory(String title,
                                 String characterName,
                                 String characterDescription,
                                 Integer secondaryCharacterCount,
                                 String genre,
                                 String place,
                                 String complexity,
                                 String duration,
                                 String language,
                                 Integer option,
                                 String mediaType) throws IOException;
}
