/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */


package org.apache.isis.extensions.dnd.awt;

import java.awt.Frame;
import java.awt.Image;
import java.net.URL;



public class AWTUtilities {

    public static void addWindowIcon(final Frame window, final String file) {
        final URL url = ViewerFrame.class.getResource("/" + "images/" + file);
        if (url != null) {
            final Image image = java.awt.Toolkit.getDefaultToolkit().getImage(url);
            if (image != null) {
                window.setIconImage(image);
            }
        }
    }
}
