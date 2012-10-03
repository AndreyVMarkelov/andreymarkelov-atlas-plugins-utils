/*
 * Created by Andrey Markelov 02-10-2012.
 * Copyright Mail.Ru Group 2012. All rights reserved.
 */
package ru.mail.jira.plugins;

public class Utils
{
    /**
     * Private constructor.
     */
    private Utils() {}

    public static boolean isValidStr(String str)
    {
        return (str != null && str.length() > 0);
    }
}
