package com.example.goplot;

import android.net.Uri;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(RobolectricTestRunner.class)
public class UriConverterTest {

    @Test
    public void testToUri() {
        Uri expectedUri = Uri.parse(GoPlotContract.CONTENT_TYPE_SINGLE);
        Uri actualUri = UriConverter.toUri(GoPlotContract.CONTENT_TYPE_SINGLE);
        assertNotNull(expectedUri);
        assertNotNull(actualUri);
        assertEquals(expectedUri, actualUri);
    }

    @Test
    public void testFromUri() {
        String expectedString = GoPlotContract.CONTENT_TYPE_SINGLE;
        String actualString = UriConverter.fromUri(Uri.parse(GoPlotContract.CONTENT_TYPE_SINGLE));
        assertNotNull(expectedString);
        assertNotNull(actualString);
        assertEquals(expectedString, actualString);
    }
}