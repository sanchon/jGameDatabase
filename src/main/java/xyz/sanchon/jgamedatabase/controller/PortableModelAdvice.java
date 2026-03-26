package xyz.sanchon.jgamedatabase.controller;

import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
@Profile("portable")
public class PortableModelAdvice {

    @ModelAttribute("portableMode")
    public boolean portableMode() {
        return true;
    }
}