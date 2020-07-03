package pl.agawrysiuk.connection;

/**
 * Responsible for clear communication between the server and a client.
 */

import lombok.Getter;

@Getter
public enum MessageCode {
    OK,
    CHECK_CARDS,
    MISSING,

    DATABASE_ISSUE;
}

