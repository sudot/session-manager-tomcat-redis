package net.sudot.sessionmanager.serialization;

import net.sudot.sessionmanager.tomcat.redis.RedisSession;
import net.sudot.sessionmanager.tomcat.redis.SessionSerializationMetadata;

import java.io.IOException;

public interface Serializer {
    void setClassLoader(ClassLoader loader);

    byte[] attributesHashFrom(RedisSession session) throws IOException;

    byte[] serializeFrom(RedisSession session, SessionSerializationMetadata metadata) throws IOException;

    void deserializeInto(byte[] data, RedisSession session, SessionSerializationMetadata metadata) throws IOException, ClassNotFoundException;
}
