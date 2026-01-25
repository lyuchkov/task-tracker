package tracker.config;

import com.google.gson.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public final class GsonConfig {
    public static Gson getGson() {
        return new GsonBuilder()
                .registerTypeAdapter(LocalDateTime.class, (JsonSerializer<LocalDateTime>) (src, _, _) ->
                        new JsonPrimitive(src.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)))
                .registerTypeAdapter(LocalDateTime.class, (JsonDeserializer<LocalDateTime>) (json, _, _) ->
                        LocalDateTime.parse(json.getAsString(), DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                .registerTypeAdapter(Duration.class, (JsonSerializer<Duration>) (src, _, _) ->
                        new JsonPrimitive(src.toMinutes()))
                .registerTypeAdapter(Duration.class, (JsonDeserializer<Duration>) (json, _, _) ->
                        Duration.ofMinutes(json.getAsLong()))
                .create();
    }
}