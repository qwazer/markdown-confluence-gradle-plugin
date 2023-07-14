package com.github.qwazer.markdown.confluence.core;

import com.github.qwazer.markdown.confluence.core.ConfluenceException;

public class NotFoundException extends ConfluenceException {


    public NotFoundException(String message) {
        super(message);
    }

    public NotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
