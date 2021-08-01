package com.example.goplot;

import org.junit.Test;

import java.util.Date;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;

public class DateConverterTest {

    @Test
    public void testToDate() {
        Date expectedDate = new Date(1617270240000L);
        Date actualDate = DateConverter.toDate(1617270240000L);
        assertNotNull(expectedDate);
        assertNotNull(actualDate);
        assertEquals(expectedDate, actualDate);
    }

    @Test
    public void testFromDate() {
        long expectedLong = 1617270240000L;
        long actualLong = DateConverter.fromDate(new Date(1617270240000L));
        assertNotEquals(expectedLong, 0);
        assertNotEquals(actualLong, 0);
        assertEquals(expectedLong, actualLong);
    }
}