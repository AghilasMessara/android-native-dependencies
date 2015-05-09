package com.nabilhachicha.nativedependencies.utils;

import org.junit.rules.ErrorCollector;

/**
 * Created by Nabil on 12/05/15.
 * Expose {@link ErrorCollector#verify()} method as public
 */
public class AccessibleErrorCollector extends ErrorCollector {
    @Override
    public void verify() throws Throwable {
        super.verify();
    }
}