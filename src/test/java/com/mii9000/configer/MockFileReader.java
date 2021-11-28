package com.mii9000.configer;

import java.io.IOException;

public class MockFileReader implements IFileReader {

    private final String mockContentReturn;

    public MockFileReader(String MockContentReturn) {
        mockContentReturn = MockContentReturn;
    }

    @Override
    public String Read(String FileName) throws IOException {
        return mockContentReturn;
    }
}
