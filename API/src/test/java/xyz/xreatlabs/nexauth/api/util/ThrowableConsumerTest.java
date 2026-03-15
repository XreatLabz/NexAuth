/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package xyz.xreatlabs.nexauth.api.util;

import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ThrowableConsumerTest {

    @Test
    void acceptsValue() throws Throwable {
        var seen = new AtomicReference<String>();
        ThrowableConsumer<String, Exception> consumer = seen::set;

        consumer.accept("value");

        assertEquals("value", seen.get());
    }
}
