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
package org.apache.isis.core.commons.debug;

public class DebugTee implements DebugBuilder {

    private final DebugBuilder builder1;
    private final DebugBuilder builder2;

    public DebugTee(DebugBuilder builder1, DebugBuilder builder2) {
        this.builder1 = builder1;
        this.builder2 = builder2;
    }

    @Override
    public void append(int number, int width) {
        builder1.append(number, width);
        builder2.append(number, width);
    }

    @Override
    public void append(Object object) {
        builder1.append(object);
        builder2.append(object);
    }

    @Override
    public void append(Object object, int width) {
        builder1.append(object, width);
        builder2.append(object, width);
    }

    @Override
    public void appendAsHexln(String label, long value) {
        builder1.appendAsHexln(label, value);
        builder2.appendAsHexln(label, value);
    }

    @Override
    public void appendException(Throwable e) {
        builder1.appendException(e);
        builder2.appendException(e);
    }

    @Override
    public void appendln() {
        builder1.appendln();
        builder2.appendln();
    }

    @Override
    public void appendln(String text) {
        builder1.appendln(text);
        builder2.appendln(text);
    }

    @Override
    public void appendln(String label, boolean value) {
        builder1.appendln(label, value);
        builder2.appendln(label, value);
    }

    @Override
    public void appendln(String label, double value) {
        builder1.appendln(label, value);
        builder2.appendln(label, value);
    }

    @Override
    public void appendln(String label, long value) {
        builder1.appendln(label, value);
        builder2.appendln(label, value);
    }

    @Override
    public void appendln(String label, Object object) {
        builder1.appendln(label, object);
        builder2.appendln(label, object);
    }

    @Override
    public void appendln(String label, Object[] objects) {
        builder1.appendln(label, objects);
        builder2.appendln(label, objects);
    }

    @Override
    public void appendTitle(String title) {
        builder1.appendTitle(title);
        builder2.appendTitle(title);
    }

    @Override
    public void startSection(String title) {
        builder1.startSection(title);
        builder2.startSection(title);
    }

    @Override
    public void endSection() {
        builder1.endSection();
        builder2.endSection();
    }

    @Override
    public void blankLine() {
        builder1.blankLine();
        builder2.blankLine();
    }

    @Override
    public void indent() {
        builder1.indent();
        builder2.indent();
    }

    @Override
    public void unindent() {
        builder1.unindent();
        builder2.unindent();
    }
    
    @Override
    public void close() {
        builder1.close();
        builder2.close();        
    }
}