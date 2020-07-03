package pl.agawrysiuk.connection;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.net.Socket;

@Getter
@AllArgsConstructor
public class Messenger {
    private final Socket socket;
    private final PrintWriter clientSender;
    private final BufferedReader clientReceiver;
}
