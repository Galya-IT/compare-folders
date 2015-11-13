package io.galya.files.model;

public abstract class Message {
    
    protected static final String NEW_LINE = System.getProperty("line.separator");
    protected StringBuilder messageStringBuilder;
    
    public Message () {
        messageStringBuilder = new StringBuilder();
    }
    
    public abstract String getAsString();
}
