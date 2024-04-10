package WebsocketServer.websocket.handler;

import WebsocketServer.services.GenerateJSONObjectService;
import WebsocketServer.services.LobbyService;
import WebsocketServer.services.UserClientService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;
import org.springframework.web.socket.*;


public class WebSocketHandlerImpl implements WebSocketHandler {

    private final LobbyService lobbyService;
    private UserClientService userClientService;
    private static final Logger logger = LogManager.getLogger(WebSocketHandlerImpl.class);


    public WebSocketHandlerImpl(){
        this.lobbyService = new LobbyService();
        this.userClientService = new UserClientService();
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        logger.info("Verbindung erfolgreich hergestellt: " + session.getId());
    }

    @Override
    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception {
        logger.info("Nachricht erhalten: " + message.getPayload());

        if (message.getPayload().equals("Test message")) {
            session.sendMessage(new TextMessage("echo from handler: " + message.getPayload()));
        } else {
            JSONObject messageJson = new JSONObject(message.getPayload().toString());

            String username = messageJson.getString("username");
            String action = messageJson.getString("action");

            //Checks which action was requested by client.
            switch (action) {
                case "registerUser":
                    logger.info("Setting Username...");
                    String resp = this.userClientService.registerUser(session, messageJson);

                    JSONObject responseMessage = GenerateJSONObjectService.generateJSONObject("registeredUser", username, true, "", "");
                    switch(resp){
                        case "Username set.":
                            responseMessage.put("message", "Username set");
                            logger.info("Username set.");
                            break;

                        case "Username already in use, please take another one.":
                            responseMessage.put("message", "Username in use");
                            logger.info("Username already in use, please take another one.");
                            break;

                        case "No username passed, please provide an username.":
                            responseMessage.put("message", "No username passed");
                            logger.info("No username passed, please provide an username.");
                            break;

                        default:
                            responseMessage.put("error", "An error occurred.");
                            break;
                    }
                    session.sendMessage(new TextMessage(responseMessage.toString()));
                    break;
                case "joinLobby":
                    logger.info("Case joinLobby: " + username );
                    lobbyService.handleJoinLobby(session, messageJson);
                    break;
                default:
                    JSONObject response = new JSONObject();
                    response.put("error", "Unbekannte Aktion");
                    response.put("action", action);
                    session.sendMessage(new TextMessage(response.toString()));
                    System.out.println("Unbekannte Aktion erhalten: " + action + ", Username: " + username);
                    break;
            }
        }
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {

    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception {
        logger.info("Verbindung getrennt: "+ session.getId());
    }

    @Override
    public boolean supportsPartialMessages() {
        return false;
    }
}