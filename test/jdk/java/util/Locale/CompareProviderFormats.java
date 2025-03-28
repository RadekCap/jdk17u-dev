/*
 * Copyright (c) 2016, 2023, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

/*
 * @test
 * @bug 8154797
 * @modules java.base/sun.util.locale.provider
 *          java.base/sun.util.resources
 *          jdk.localedata
 * @summary Test for checking HourFormat and GmtFormat resources are retrieved from
 * COMPAT and CLDR Providers.
 * @run junit CompareProviderFormats
*/

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.stream.Stream;

import sun.util.locale.provider.LocaleProviderAdapter.Type;
import sun.util.locale.provider.LocaleProviderAdapter;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

public class CompareProviderFormats {
    static Map<String, String> expectedResourcesMap = new HashMap<>();
    static final String GMT_RESOURCE_KEY = "timezone.gmtFormat";
    static final String HMT_RESOURCE_KEY = "timezone.hourFormat";
    static final String GMT = "Gmt";
    static final String HMT = "Hmt";

    /**
     * Fill the expectedResourcesMap with the desired key / values
     */
    @BeforeAll
    static void populateResourcesMap() {
        expectedResourcesMap.put("FR" + GMT, "UTC{0}");
        expectedResourcesMap.put("FR" + HMT, "+HH:mm;\u2212HH:mm");
        expectedResourcesMap.put("FI" + HMT, "+H.mm;-H.mm");
        expectedResourcesMap.put("FI" + GMT, "UTC{0}");
        /* For root locale, en_US, de_DE, hi_IN, ja_JP, Root locale resources
         * should be returned.
         */
        expectedResourcesMap.put(GMT, "GMT{0}"); // Root locale resource
        expectedResourcesMap.put(HMT, "+HH:mm;-HH:mm"); // Root locale resource
    }

    /**
     * For each locale, ensure that the returned resources for gmt and hmt match
     * the expected resources for both COMPAT and CLDR
     */
    @ParameterizedTest
    @MethodSource("localeProvider")
    public void compareResourcesTest(Locale loc) {
        compareResources(loc);
    }

    private void compareResources(Locale loc) {
        String mapKeyHourFormat = HMT, mapKeyGmtFormat = GMT;
        ResourceBundle compatBundle, cldrBundle;
        compatBundle = LocaleProviderAdapter.forJRE().getLocaleResources(loc)
                .getJavaTimeFormatData();
        cldrBundle = LocaleProviderAdapter.forType(Type.CLDR)
                .getLocaleResources(loc).getJavaTimeFormatData();

        if (loc.getCountry().equals("FR") || loc.getCountry().equals("FI")) {
            mapKeyHourFormat = loc.getCountry() + HMT;
            mapKeyGmtFormat = loc.getCountry() + GMT;
        }

        if (!(expectedResourcesMap.get(mapKeyGmtFormat)
                .equals(compatBundle.getString(GMT_RESOURCE_KEY))
                && expectedResourcesMap.get(mapKeyHourFormat)
                .equals(compatBundle.getString(HMT_RESOURCE_KEY))
                && expectedResourcesMap.get(mapKeyGmtFormat)
                .equals(cldrBundle.getString(GMT_RESOURCE_KEY))
                && expectedResourcesMap.get(mapKeyHourFormat)
                .equals(cldrBundle.getString(HMT_RESOURCE_KEY)))) {
            throw new RuntimeException("Retrieved resource does not match with "
                    + "  expected string for Locale " + compatBundle.getLocale());
        }
    }

    private static Stream<Locale> localeProvider() {
        return Stream.of(
                new Locale("hi", "IN"),
                Locale.UK, new Locale("fi", "FI"),
                Locale.ROOT, Locale.GERMAN, Locale.JAPANESE,
                Locale.ENGLISH, Locale.FRANCE
        );
    }
}
