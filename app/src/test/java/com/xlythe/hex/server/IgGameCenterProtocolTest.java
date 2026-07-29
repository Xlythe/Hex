package com.xlythe.hex.server;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.hex.core.Point;

import org.junit.Test;

public class IgGameCenterProtocolTest {
    @Test
    public void moveCodecMatchesProductionAndroidFormat() {
        assertEquals("A1", IgGameCenterProtocol.encodeMove(new Point(0, 0), 11));
        assertEquals("C7", IgGameCenterProtocol.encodeMove(new Point(6, 2), 11));
        assertEquals("S19", IgGameCenterProtocol.encodeMove(new Point(18, 18), 19));

        Point decoded = IgGameCenterProtocol.decodeMove("c7", 11);
        assertEquals(6, decoded.x);
        assertEquals(2, decoded.y);
        assertTrue(IgGameCenterProtocol.isSwap(
                IgGameCenterProtocol.decodeMove("SWAP", 11)));
        assertNull(IgGameCenterProtocol.decodeMove("6-2", 11));
        assertNull(IgGameCenterProtocol.decodeMove("L1", 11));
    }

    @Test
    public void hashesUtf8PasswordsForLegacyLogin() {
        assertEquals("d41d8cd98f00b204e9800998ecf8427e", IgGameCenterProtocol.md5(""));
        assertEquals("2ab96390c7dbe3439de74d0c9b0b1767", IgGameCenterProtocol.md5("hunter2"));
        assertEquals("c0e89a293bd36c7a768e4e9d2c5475a8", IgGameCenterProtocol.md5("こんにちは"));
    }

    @Test
    public void validatesServerRoutingAndPollingCadence() {
        assertTrue(IgGameCenterProtocol.isValidServerName("gc1"));
        assertFalse(IgGameCenterProtocol.isValidServerName("../gc1"));
        assertFalse(IgGameCenterProtocol.isValidServerName("gc1.example.com"));
        assertEquals(15_000L, IgGameCenterProtocol.POLL_INTERVAL_MS);
    }
}
