package com.mii9000.configer;

import java.io.IOException;

public interface IFileModifier {
    void Modify(String MessageEventBody, String WatchEvent) throws IOException;
}
