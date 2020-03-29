package org.gradle;

import org.junit.Test;

import static org.junit.Assert.*;

public class PersonIntTest {
    @Test
    public void canConstructAPersonWithAName() {
        Person person = new Person("Larry");
        assertEquals("Larry", person.getName());
    }
}
