package com.diegodev.apidesportes.jogos.item;

import com.google.gson.annotations.SerializedName;

/**
 * Link de transmissão vindo da API /api/jogos (campo "canais_links").
 * Cada item é um objeto com dados do servidor e a URL de transmissão.
 */
public class ItemCanalLink {

    @SerializedName("external_channel_id")
    private String externalChannelId;

    @SerializedName("channel_name")
    private String channelName;

    @SerializedName("channel_logo")
    private String channelLogo;

    @SerializedName("server_id")
    private long serverId;

    @SerializedName("server_name")
    private String serverName;

    @SerializedName("server_slug")
    private String serverSlug;

    @SerializedName("transmission_url")
    private String transmissionUrl;

    public String getExternalChannelId() { return externalChannelId; }
    public void setExternalChannelId(String externalChannelId) { this.externalChannelId = externalChannelId; }

    public String getChannelName() { return channelName; }
    public void setChannelName(String channelName) { this.channelName = channelName; }

    public String getChannelLogo() { return channelLogo; }
    public void setChannelLogo(String channelLogo) { this.channelLogo = channelLogo; }

    public long getServerId() { return serverId; }
    public void setServerId(long serverId) { this.serverId = serverId; }

    public String getServerName() { return serverName; }
    public void setServerName(String serverName) { this.serverName = serverName; }

    public String getServerSlug() { return serverSlug; }
    public void setServerSlug(String serverSlug) { this.serverSlug = serverSlug; }

    public String getTransmissionUrl() { return transmissionUrl; }
    public void setTransmissionUrl(String transmissionUrl) { this.transmissionUrl = transmissionUrl; }
}
