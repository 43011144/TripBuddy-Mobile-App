package com.example.tripbuddy.model;

import java.util.Objects;

public final class Memory {
    public final long id;
    public final long userId;
    public final String content;
    public final long createdAt;

    public Memory(long id, long userId, String content, long createdAt) {
        this.id = id;
        this.userId = userId;
        this.content = content;
        this.createdAt = createdAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Memory)) return false;
        Memory m = (Memory) o;
        return id == m.id && Objects.equals(content, m.content);
    }

    @Override
    public int hashCode() { return Objects.hash(id); }
}
