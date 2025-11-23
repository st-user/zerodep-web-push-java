package com.zerodeplibs.webpush.header;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;

import org.junit.jupiter.api.Test;


public class UrgencyTests {


    @Test
    public void shouldReturnAcceptableUrgencyHeaderFiledValue() {
        assertThat(Urgency.veryLow(), equalTo("very-low"));
        assertThat(Urgency.low(), equalTo("low"));
        assertThat(Urgency.normal(), equalTo("normal"));
        assertThat(Urgency.high(), equalTo("high"));
    }
}
