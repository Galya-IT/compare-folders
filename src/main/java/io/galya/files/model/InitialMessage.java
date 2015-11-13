package io.galya.files.model;


public class InitialMessage extends Message {
    
    public InitialMessage() {
        super();
        
        messageStringBuilder.append("-------------------------------------------------");
        messageStringBuilder.append(NEW_LINE);
        messageStringBuilder.append(" * * * * Folder Comparison Application * * * *");
        messageStringBuilder.append(NEW_LINE);
        messageStringBuilder.append("-------------------------------------------------");
        messageStringBuilder.append(NEW_LINE);
        messageStringBuilder.append("Choose the folders you want to compare!");
        messageStringBuilder.append(NEW_LINE);
        messageStringBuilder.append("Separate the folder paths on different lines.");
        messageStringBuilder.append(NEW_LINE);
        messageStringBuilder.append("Type 'compare' without quotes to start comparing.");
        messageStringBuilder.append(NEW_LINE);
        messageStringBuilder.append("-------------------------------------------------");
    }
    
    @Override
    public String getAsString() {
        return messageStringBuilder.toString();
    }
}
