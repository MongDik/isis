package test.org.nakedobjects.utility;

import java.util.Vector;

import junit.framework.Assert;

public class ExpectedSet {
    private Vector expectedObjects = new Vector();
    private Vector actualObjects = new Vector();
    
    public void addActual(Object object) {
        actualObjects.addElement(object);
        Assert.assertTrue("More actuals than expected; only expected "+ expectedObjects.size(), actualObjects.size() <= expectedObjects.size());
        Assert.assertEquals("Actual does not match expected.\n", expectedObjects.elementAt(actualObjects.size() - 1), object);
    }

    public void addExpected(Object object) {
        expectedObjects.addElement(object);
    }

    public void verify() {
        Assert.assertTrue("Too few actuals added\n  Expected " + expectedObjects + "\n  but got " + actualObjects, actualObjects.size() == expectedObjects.size());
    }
}


/*
Naked Objects - a framework that exposes behaviourally complete
business objects directly to the user.
Copyright (C) 2000 - 2005  Naked Objects Group Ltd

This program is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 2 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA

The authors can be contacted via www.nakedobjects.org (the
registered address of Naked Objects Group is Kingsway House, 123 Goldworth
Road, Woking GU21 1NR, UK).
*/