package com.ignacio.galvez.choose.your.own.adventure.cotroller;

import com.ignacio.galvez.choose.your.own.adventure.dto.StoryResponseDTO;
import com.ignacio.galvez.choose.your.own.adventure.service.ChooseYourOwnAdventureService;
import com.ignacio.galvez.choose.your.own.adventure.service.ImageAndAudioUploadService;
import jakarta.websocket.server.PathParam;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController("/v1/choose-your-own-adventure/")
@RequiredArgsConstructor
public class ChooseYourOwnAdventureController {

    private final ChooseYourOwnAdventureService chooseYourOwnAdventureService;
    private final ImageAndAudioUploadService imageAndAudioUploadService;

    @GetMapping("/story")
    String createStory(@RequestParam("title")
                                      String title,
                                       @RequestParam(value = "character_name",defaultValue = "") String characterName,
                                       @RequestParam(value = "character_description",defaultValue = "") String characterDescription,
                                       @RequestParam("secondary_character_count") Integer secondaryCharacterCount,
                                       @RequestParam("place") String place,
                                       @RequestParam("genre") String genre,
                                       @RequestParam("complexity") String complexity,
                                       @RequestParam("duration") String duration,
                                       @RequestParam(name = "language",defaultValue = "") String language,
                                       @RequestParam("media_type") String mediaType,
                                       @RequestParam(value = "option",defaultValue = "1")  Integer option) throws IOException {
        StoryResponseDTO storyResponseDTO = this.chooseYourOwnAdventureService
                        .createStory(title, characterName,characterDescription,secondaryCharacterCount,place,genre,
                                complexity,duration,language,option,mediaType);
        return storyResponseDTO.content();
    }

    @PostMapping("/character/audio")
    ResponseEntity<String> uploadMainCharacterAudio(@RequestParam("audio") MultipartFile file,@RequestParam("title") String title,Model model) throws IOException {
        model.addAttribute("title audio file uploaded");
        return ResponseEntity.ok(this.imageAndAudioUploadService.uploadAudio(file,title));
    }

    @PostMapping("/character/image")
    ResponseEntity<String> uploadMainCharacterImage(@RequestParam("image") MultipartFile file,@RequestParam("title") String title,Model model) throws IOException {
        model.addAttribute("title image file uploaded");
        return  ResponseEntity.ok(this.imageAndAudioUploadService.uploadImage(file,title));

    }

}
