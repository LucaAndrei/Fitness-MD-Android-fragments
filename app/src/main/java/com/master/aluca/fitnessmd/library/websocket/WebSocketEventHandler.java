package com.master.aluca.fitnessmd.library.websocket;


public interface WebSocketEventHandler {
    public void onOpen();

    public void onMessage(WebSocketMessage message);

    public void onClose();

    public void onError(WebSocketException e);

    public void onLogMessage(String msg);
}
