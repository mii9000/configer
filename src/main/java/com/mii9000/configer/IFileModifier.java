package com.mii9000.configer;

import org.apache.commons.configuration2.ex.ConfigurationException;

import java.io.IOException;
import java.util.HashSet;

public interface IFileModifier {
    void Modify(String MessageEventBody) throws IOException, ConfigurationException;
}
