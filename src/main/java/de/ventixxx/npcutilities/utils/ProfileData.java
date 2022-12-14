package de.ventixxx.npcutilities.utils;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.Getter;
import lombok.Setter;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.UUID;

@Getter @Setter
public final class ProfileData
{

    private final String UUID_OF_NAME = "https://api.mojang.com/users/profiles/minecraft/%name%";
    private final String PROPERTIES_OF_UUID = "https://sessionserver.mojang.com/session/minecraft/profile/%uuid%?unsigned=false";

    private String name, value, signature;
    private String uuid;


    public ProfileData(String name)
    {
        this.name = name;

        // Default Value, Signature (Skin == Ventixxx)
        this.value = "ewogICJ0aW1lc3RhbXAiIDogMTY3MDcxMzk2MzM4MiwKICAicHJvZmlsZUlkIiA6ICIwYzgzNGQ0Njk0MDU0ZDRlOGZiMjcxODBhNzAxZmJiNyIsCiAgInByb2ZpbGVOYW1lIiA6ICJTWUtPTiIsCiAgInNpZ25hdHVyZVJlcXVpcmVkIiA6IHRydWUsCiAgInRleHR1cmVzIiA6IHsKICAgICJTS0lOIiA6IHsKICAgICAgInVybCIgOiAiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS9jNGZlMzc5N2FiNGJkZmViNzNkMmFmYThkODhhZGIxOTJjNDVkMmQ4OTI2YmUxNWZhMzlmZGE3YjU2ZmQ1NWJkIgogICAgfSwKICAgICJDQVBFIiA6IHsKICAgICAgInVybCIgOiAiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS8yMzQwYzBlMDNkZDI0YTExYjE1YThiMzNjMmE3ZTllMzJhYmIyMDUxYjI0ODFkMGJhN2RlZmQ2MzVjYTdhOTMzIgogICAgfQogIH0KfQ==";
        this.signature = "xQ2SCbVPjVm8+Jf2hJAAXNlk+4shkL68+9q45jrrAZnjmo/BD1Kj9MtPWDkWbYEpMplxf70zdoH083xQlaG56k4H+AUoeFaBdQmKd52cKwNUWBbfldPBHlhQr5uNvyY8BSaJbM3mZwsAH4SBc/hIX2axpFNd9kbKdzVSzrFXyslEcBCH+Eq1WtjJoXk3Yee2jFlI1KZwo1OX0CeZawYaVL1Md1YVCc+fyc2jpZ3maLNUdfmtS+tqVJAT74UNsQbSGbkVZ9hcBCi+RGVwKo8r30bHT3ad5hDZVKCIaR1St9uVnLG55kLdNS9nhRBkmY4A3/fz0MsWHexyZ0s+imZd7FlhxufYc7vIJ7iYAXlRcTXMxiJmTMAaFVa6KWA9XDytwa0ejo18Lmu+YO4RaaAelTHHRPA0wlXKjwn0BlbqZJxCO+YGRroRUdUq2W/F/SepPIPSeqPPgb8Gl08b4gVBLGFg3J2+oBDZheLVkdxSnm3NfY2OriuzSsd5PojReLyUutm3Wp1T2Y8Ft2TitDlBX0/TeqHpFQx7qcPYdwOHJWUrF8TOePyOMFE6B+cNTXJxrd1ElOP3iG0AK6EKRNaLiiAgSvxOKNSvj6EMwVUuqeJnrp9yXoZmtGGyNKJ385zudmFCYyBvTZ7obbetKqPFjWo3/e1tgtXXqXKz1vzWmyc=";
    }

    public boolean hasProfile()
    {
        // try to get JsonElement of name
        JsonElement uuidElement = getResult(UUID_OF_NAME.replace("%name%", this.name));
        if(uuidElement == null) return false;
        // get JsonObject from JsonElement
        JsonObject uuidObject = uuidElement.getAsJsonObject();
        // if "id" not exist in JsonObject return false
        if(!uuidObject.has("id")) return false;
        // if "id" exist in JsonObject -> get uuid
        this.uuid = uuidObject.get("id").getAsString();
        // try to get JsonElement of uuid
        JsonElement profileElement = getResult(PROPERTIES_OF_UUID.replace("%uuid%", this.uuid));
        if(profileElement == null) return false;
        // get JsonObject from JsonElement
        JsonObject profileObject = profileElement.getAsJsonObject();
        // if "properties" not exist in JsonObject return false
        if(!profileObject.has("properties")) return false;
        // if "properties" exist in JsonObject -> load JsonObject of "properties"
        JsonObject propertiesObject = profileObject.getAsJsonArray("properties").get(0).getAsJsonObject();
        this.value = propertiesObject.get("value").getAsString();
        this.signature = propertiesObject.get("signature").getAsString();
        return true;
    }

    private JsonElement getResult(String url)
    {

        // try to connect to url -> if connect successfully return jsonElement

        try
        {
            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setReadTimeout(5000);
            connection.setConnectTimeout(5000);
            connection.setUseCaches(true);
            connection.connect();
            if(connection.getResponseCode() == 200) return new JsonParser().parse(new InputStreamReader(connection.getInputStream()));
        } catch (IOException e)
        {
            throw new RuntimeException(e);
        }
        return null;
    }

}
