package com.github.qwazer.markdown.confluence.core.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

public class ConfluenceSpace {

    private final Long id;
    private final String key;
    private final String name;
    private final String type;

    public ConfluenceSpace(String key) {
        this(null, key, null, null);
    }

    @JsonCreator
    public ConfluenceSpace(
        @JsonProperty("id") Long id,
        @JsonProperty("key") String key,
        @JsonProperty("name") String name,
        @JsonProperty("type") String type
    ) {
        this.id = id;
        this.key = key;
        this.name = name;
        this.type = type;
    }

    public Long getId() {
        return id;
    }

    public String getKey() {
        return key;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ConfluenceSpace that = (ConfluenceSpace) o;
        return Objects.equals(id, that.id) && Objects.equals(key, that.key) && Objects.equals(name, that.name) && Objects.equals(type, that.type);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, key, name, type);
    }

    @Override
    public String toString() {
        return "ConfluenceSpace{" +
            "id=" + id +
            ", key='" + key + '\'' +
            ", name='" + name + '\'' +
            ", type='" + type + '\'' +
            '}';
    }
}
