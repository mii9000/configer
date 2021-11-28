package com.mii9000.configer;

import org.apache.commons.configuration2.ex.ConfigurationException;

import java.io.IOException;

public interface IFileModifier {
    void Modify(String MessageEventBody, String WatchEvent) throws IOException, ConfigurationException;
}
