package dev.overwave.whereeat;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/")
public class MessagesController {

    public MessagesController() {
    }

    @GetMapping("/all")
    public String getAll() {
        return "asd";
//        return findBookById(id);
    }
}