package com.example.campusfind;

import org.junit.Test;
import static org.junit.Assert.*;

import com.example.campusfind.models.Item;
import com.example.campusfind.models.User;
import java.util.Map;

public class ModelUnitTest {

    @Test
    public void testItemModel() {
        Item item = new Item("1", "Wallet", "Accessories", "Lost", "Library", "Black leather wallet");
        item.setLatitude(23.0);
        item.setLongitude(90.0);
        
        assertEquals("1", item.getId());
        assertEquals("Wallet", item.getTitle());
        assertEquals(23.0, item.getLatitude(), 0.001);
        
        Map<String, Object> map = item.toMap();
        assertEquals("Wallet", map.get("title"));
        assertEquals(23.0, map.get("latitude"));
    }

    @Test
    public void testUserModel() {
        User user = new User("u1", "test@test.com", "Test User");
        user.setStudentId("12345");
        
        assertEquals("u1", user.getUserId());
        assertEquals("12345", user.getStudentId());
        
        Map<String, Object> map = user.toMap();
        assertEquals("Test User", map.get("name"));
        assertEquals("12345", map.get("studentId"));
    }
}
