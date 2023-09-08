package com.example.onlinetrivia;

import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;


public class IRCClient implements Runnable {

    private static IRCClient instance = null;

    private String serverAddress;
    private int port;
    private String username;
    private String nickname;
    private String ident;
    private Socket socket;
    private PrintWriter writer;
    private MainActivity mainActivity;
    private List<String> joinedChannels = new ArrayList<>();
    private List<String> privateChats = new ArrayList<>();

    private ChannelMessageListener channelMessageListener;



    public interface OnConnectionEstablishedListener {
        void onConnectionEstablished();
    }

    private OnConnectionEstablishedListener connectionListener;

    public interface OnNamesReceivedListener {
        void onNamesReceived(List<String> names);
    }

    private OnNamesReceivedListener namesListener;

    public void setOnConnectionEstablishedListener(OnConnectionEstablishedListener listener) {
        this.connectionListener = listener;
    }

    public void setNamesListener(OnNamesReceivedListener listener) {
        this.namesListener = listener;
        Log.d("IRCClient", "namesListener has been set.");
    }

    private IRCClient() {}

    public static synchronized IRCClient getInstance() {
        if (instance == null) {
            instance = new IRCClient();
        }
        return instance;
    }

    public void initialize(MainActivity activity, String serverAddress, int port, String username, String nickname, String ident) {
        this.mainActivity = activity;
        this.serverAddress = serverAddress;
        this.port = port;
        this.username = username;
        this.nickname = nickname;
        this.ident = ident;
    }

    public interface OnMessageReceivedListener {
        void onMessageReceived(String channel, String sender, String message);
    }

    private OnMessageReceivedListener messageListener;

    public void setMessageListener(OnMessageReceivedListener listener) {
        this.messageListener = listener;
    }




    @Override
    public void run() {
        try {
            socket = new Socket(serverAddress, port);
            if (socket.isConnected()) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                writer = new PrintWriter(socket.getOutputStream(), true);
                sendRawCommand("NICK " + nickname);
                sendRawCommand("USER " + ident + " 0 * :" + username);
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (connectionListener != null) {
                    connectionListener.onConnectionEstablished();
                }
                String line;
                while ((line = reader.readLine()) != null) {
                    handleServerResponse(line);
                }
            } else {
                Log.e("IRCClient", "Failed to connect to the server.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void joinChannel(String channel) {
        new Thread(() -> {
            sendRawCommand("JOIN " + channel);
            sendRawCommand("NAMES " + channel);

            // Add the channel to the joined channels list
            if (!joinedChannels.contains(channel)) {
                joinedChannels.add(channel);
            }
            updateChatList(joinedChannels);  // notify the listeners that the chat list has been updated
        }).start();
    }

    public void requestNamesForChannel(String channel) {
        sendRawCommand("NAMES " + channel);
    }


    public void partChannel (String channel){
        new Thread(() -> {
            sendRawCommand("PART " + channel);

            // Remove the channel from the joined channels list
            joinedChannels.remove(channel);
            updateChatList(joinedChannels);  // notify the listeners that the chat list has been updated
        }).start();
    }


    public void changeNickname (String newNickname){
        this.nickname = newNickname;
        new Thread(() -> sendRawCommand("NICK " + newNickname)).start();
    }

    public void sendMessageToChannel (String channel, String message){
        new Thread(() -> sendRawCommand("PRIVMSG " + channel + " :" + message)).start();
    }

    public String getNickname () {
        return nickname;
    }

    private void handleServerResponse (String line){
        Log.d("IRCClient", "Server Response: " + line);
        mainActivity.updateServerResponse(line);
        if (line.startsWith("PING")) {
            sendRawCommand("PONG " + line.substring(5));
        } else if (line.contains("PRIVMSG")) {
            handlePrivMsg(line);
        } else if (line.contains("JOIN ")) {
            handleNamesResponse(line);
        } else if (line.matches(".*\\s353\\s.*")) {
            handleNamesResponse(line);
        } else if (line.matches(".*\\s473\\s.*")) {
            Log.e("IRCClient", "Error: Cannot join channel (+i) - must be invited.");
        }
    }

    private boolean isPrivateMessage(String line, String channel) {
        // In most IRC servers, channels start with # or &
        return !(channel.startsWith("#") || channel.startsWith("&"));
    }

    private void handlePrivMsg(String line) {
        String sender = line.split("!")[0].substring(1);
        String message = line.split("PRIVMSG")[1].split(":")[1];
        String channelOrUser = line.split("PRIVMSG")[1].split(" ")[1];

        Log.d("IRCClient", "Parsed Message: Channel/User - " + channelOrUser + ", Sender - " + sender + ", Message - " + message);

        if (isPrivateMessage(line, channelOrUser)) {
            handlePrivateMessage(sender, message);
        } else {
            if (messageListener != null) {
                messageListener.onMessageReceived(channelOrUser, sender, message);

                if (!privateChats.contains(sender) && !joinedChannels.contains(sender)) {
                    privateChats.add(sender);
                    updateChatList(getCurrentChatList());
                }
            }
        }
    }

    private void handlePrivateMessage(String sender, String message) {
        // Do what you want with private messages here. For instance:
        if (privateMessageListener != null) {
            privateMessageListener.onPrivateMessageReceived(sender, message);
        }
    }

    public interface OnPrivateMessageReceivedListener {
        void onPrivateMessageReceived(String sender, String message);
    }

    private OnPrivateMessageReceivedListener privateMessageListener;

    public void setPrivateMessageListener(OnPrivateMessageReceivedListener listener) {
        this.privateMessageListener = listener;
    }

    public interface ChannelMessageListener {
        void onChannelMessage(String channel, String sender, String message);
    }

    public void setChannelMessageListener(ChannelMessageListener listener) {
        this.channelMessageListener = listener;
    }





    private void handleNamesResponse(String line) {
        String[] parts = line.split(":");
        if (parts.length > 2) {
            List<String> namesList = Arrays.asList(parts[2].trim().split("\\s+"));
            if (namesListener != null) {
                namesListener.onNamesReceived(namesList);
                Log.d("IRCClient", "namesListener used to send names.");
            } else {
                Log.e("IRCClient", "namesListener is null. Cannot send names.");
            }
        }
    }

    private void sendRawCommand (String command){
        Log.d("IRCClient", "Sending raw command: " + command);
        if (writer != null) {
            writer.println(command);
        }


    }
    public interface OnChatListUpdatedListener {
        void onChatListUpdated(List<String> chatList);
    }

    private OnChatListUpdatedListener chatListUpdatedListener;

    public void setChatListUpdatedListener(OnChatListUpdatedListener listener) {
        this.chatListUpdatedListener = listener;
    }

    // You should call this method from within IRCClient whenever a new chat or channel gets added or removed
    public void updateChatList(List<String> chatList) {
        if (chatListUpdatedListener != null) {
            chatListUpdatedListener.onChatListUpdated(chatList);
        }
    }

    // Also, provide a method to get the current list of chats
    public List<String> getCurrentChatList() {
        List<String> combinedList = new ArrayList<>();
        combinedList.addAll(joinedChannels);
        combinedList.addAll(privateChats);
        Collections.sort(combinedList);
        return combinedList;
    }
}