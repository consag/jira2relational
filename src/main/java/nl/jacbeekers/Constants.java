/*
 * MIT License
 *
 * Copyright (c) 2019 JacTools
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */

package nl.jacbeekers;

import java.util.HashMap;
import java.util.Map;

public class Constants {
    //generic result codes
    public static final String OK ="OK";
    public static final String NOT_IMPLEMENTED ="GEN-001";
    public static final String UNKNOWN ="GEN-002";
    public static final String NOT_PROVIDED ="GEN-003";
    public static final String NOT_FOUND = "GEN-004";

    //api call codes
    public static final String LOGIN_FAILED ="API-001";
    public static final String POST_FAILED ="API-002";
    public static final String QUERY_FAILED ="API-003";
    public static final String DATA_STRUCTURE_ERROR ="API-004";

    Map results = new HashMap();
    public static Map<String, String> result;
    static {
        result = new HashMap<>();
        result.put(OK,"No errors encountered");
        result.put(UNKNOWN, "Internal error. No result message found. Contact the developer.");
        result.put(NOT_IMPLEMENTED, "Not implemented");
        result.put(NOT_PROVIDED, "Not provided");
        result.put(LOGIN_FAILED, "Login API call failed");
        result.put(POST_FAILED, "POST request failed");
        result.put(NOT_FOUND, "Item could not be found");
        result.put(QUERY_FAILED, "Query failed");
        result.put(DATA_STRUCTURE_ERROR, "Received data does not match received fiels.");
    }

    //true/false
    public static final String NO ="N";
    public static final String YES ="Y";

    //defaults

    // result message
    public static String getResultMessage(String resultCode){
        return result.getOrDefault(resultCode, result.get(UNKNOWN));
    }
}
