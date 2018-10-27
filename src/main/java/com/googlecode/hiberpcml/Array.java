/*
 * The MIT License
 *
 * Copyright 2011 John Arevalo <johnarevalo@gmail.com>.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.googlecode.hiberpcml;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a field as a significant array object for pcml invocation.
 * This annotation should be applied to elements which have "count" value
 * grater than zero.
 * 
 * @author John Arevalo <johnarevalo@gmail.com>
 * @see <a href="http://publib.boulder.ibm.com/infocenter/iseries/v5r4/index.jsp?topic=%2Frzahh%2Fpcmldttg.htm">PCML data tag</a>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Array {

    /**
     * 
     * @return The length of this array. Given by the attribute "count" in data 
     * element.
     */
    int size();

    /**
     * 
     * @return name given to this array in the .pcml file.
     */
    String pcmlName();

    Class<?> type();

    /**
     * Specifies the usage of this param in the pcml. 
     * Default value is {@link UsageType#INPUTOUTPUT}
     * @return Usage of this param. 
     * @see UsageType
     */
    public UsageType usage() default UsageType.INPUTOUTPUT;
}
