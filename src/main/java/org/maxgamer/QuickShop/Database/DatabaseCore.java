package org.maxgamer.QuickShop.Database;

import java.sql.Connection;

public interface DatabaseCore {
    Connection getConnection();

    void queue(BufferStatement bs);

    void flush();

    void close();
}