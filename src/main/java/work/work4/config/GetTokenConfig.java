package work.work4.config;

import jakarta.websocket.HandshakeResponse;
import jakarta.websocket.server.HandshakeRequest;
import jakarta.websocket.server.ServerEndpointConfig;

import java.util.List;
import java.util.Map;

public class GetTokenConfig extends ServerEndpointConfig.Configurator {
    @Override
    public void modifyHandshake(ServerEndpointConfig sec, HandshakeRequest request, HandshakeResponse response) {
        if (!sec.getUserProperties().containsKey("token")) {
            Map<String, List<String>> params = request.getParameterMap();
            if (params.containsKey("token")) {
                String token = params.get("token").getFirst();
                sec.getUserProperties().put("token", token);
            }
        }
    }
}
