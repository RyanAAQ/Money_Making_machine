package org.Money_Making_Machine;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HomeController {

    @GetMapping("/")
    public String homeController() {
        return "Welcome to Quick Invoice";
    }
}
