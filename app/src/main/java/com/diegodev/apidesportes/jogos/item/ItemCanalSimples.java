package com.diegodev.apidesportes.jogos.item;

import com.google.gson.annotations.SerializedName;

/**
 * Canal simples vindo da API /api/jogos (campo "canais_simples").
 * Cada item é um objeto com nome, logo e votos — não apenas uma String.
 */
public class ItemCanalSimples {

    @SerializedName("channel_id")
    private long channelId;

    @SerializedName("channel_name")
    private String channelName;

    @SerializedName("channel_logo")
    private String channelLogo;

    @SerializedName("upvotes")
    private int upvotes;

    @SerializedName("downvotes")
    private int downvotes;

    @SerializedName("updated_at")
    private String updatedAt;

    public long getChannelId() { return channelId; }
    public void setChannelId(long channelId) { this.channelId = channelId; }

    public String getChannelName() { return channelName; }
    public void setChannelName(String channelName) { this.channelName = channelName; }

    public String getChannelLogo() { return channelLogo; }
    public void setChannelLogo(String channelLogo) { this.channelLogo = channelLogo; }

    public int getUpvotes() { return upvotes; }
    public void setUpvotes(int upvotes) { this.upvotes = upvotes; }

    public int getDownvotes() { return downvotes; }
    public void setDownvotes(int downvotes) { this.downvotes = downvotes; }

    public String getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }
}
