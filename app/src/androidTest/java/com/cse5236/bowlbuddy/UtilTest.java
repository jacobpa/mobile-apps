package com.cse5236.bowlbuddy;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import com.cse5236.bowlbuddy.util.APIService;
import com.cse5236.bowlbuddy.util.APISingleton;
import com.cse5236.bowlbuddy.util.BuildingDBSingleton;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class UtilTest {
    @Test
    public void testUseAppContext() {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getTargetContext();

        assertEquals("com.cse5236.bowlbuddy", appContext.getPackageName());
    }

    @Test
    public void testGetAPISingleton() {
        APIService singleton = APISingleton.getInstance();
        assertNotNull(singleton);
    }

    @Test
    public void testGetDBSingleton() {
        assertNotNull(BuildingDBSingleton.getDatabase(InstrumentationRegistry.getTargetContext()));
    }

}
