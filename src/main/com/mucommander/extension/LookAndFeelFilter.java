/*
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2012 Maxence Bernard
 *
 * muCommander is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * muCommander is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.mucommander.extension;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;

import javax.swing.LookAndFeel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class IMAGE_FILTER for look and feels.
 * <p>
 * This IMAGE_FILTER will only accept classes if:
 * <ul>
 *   <li>They subclass <code>javax.swing.LookAndFeel</code>.</li>
 *   <li>They are public and not abstract.</li>
 *   <li>They have a public, no-arg constructor.</li>
 *   <li>Their <code>isSupportedLookAndFeel</code> method returns <code>true</code>.</li>
 *   <li>They are not an inner class.</li>
 * </ul>
 *
 * @author Nicolas Rinaudo
 */
public class LookAndFeelFilter implements ClassFilter {
	private static final Logger LOGGER = LoggerFactory.getLogger(LookAndFeelFilter.class);
	
	/**
     * Creates a new instance of <code>LookAndFeelFilter</code>.
     */
    public LookAndFeelFilter() {}

    /**
     * Filters out everything but available look and feels.
     * @param c class to check.
     * @return <code>true</code> if c is an available look and feel, <code>false</code> otherwise.
     */
    public boolean accept(Class<?> c) {
        // Ignores inner classes.
        if (c.getDeclaringClass() != null) {
            return false;
        }
        return isPublicAndNotAbstract(c) && hasPublicDefaultConstructor(c) && isAvailableLookAndFeel(c);
    }

    private static boolean isPublicAndNotAbstract(Class<?> c) {
        // Makes sure the class is public and non-abstract.
        int modifiers = c.getModifiers();
        return Modifier.isPublic(modifiers) && !Modifier.isAbstract(modifiers);
    }


    private static boolean hasPublicDefaultConstructor(Class<?> c) {
        // Makes sure the class has a public, no-arg constructor.
        try {
            Constructor<?> constructor = c.getDeclaredConstructor();
            return Modifier.isPublic(constructor.getModifiers());
        } catch(Exception e) {
            return false;
        }
    }

    private static boolean isAvailableLookAndFeel(Class<?> c) {
        // Makes sure the class extends javax.swing.LookAndFeel and that if it does,
        // it's supported by the system.
        Class<?> buffer = c;
        while (buffer != null) {
            // c is a LookAndFeel, makes sure it's supported.
            if (buffer.equals(LookAndFeel.class)) {
                return isSupportedLookAndFeel(c);
            }
            buffer = buffer.getSuperclass();
        }
        return false;
    }

    private static boolean isSupportedLookAndFeel(Class<?> c) {
        try {
            return ((LookAndFeel) c.newInstance()).isSupportedLookAndFeel();
        } catch(Throwable e) {
            LOGGER.debug("Class " + c + " caught exception", e);
            return false;
        }
    }

}
