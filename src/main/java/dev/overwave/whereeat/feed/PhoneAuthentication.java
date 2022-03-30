package dev.overwave.whereeat.feed;

import it.tdlight.client.AuthenticationData;

public record PhoneAuthentication(long phone) implements AuthenticationData {
    @Override
    public boolean isQrCode() {
        return false;
    }

    @Override
    public boolean isBot() {
        return false;
    }

    @Override
    public long getUserPhoneNumber() {
        return phone;
    }

    @Override
    public String getBotToken() {
        return null;
    }
}
