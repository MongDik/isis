package org.nakedobjects.object.persistence;

import org.nakedobjects.object.Version;
import org.nakedobjects.utility.ToString;

import java.util.Date;

public class LongNumberVersion extends AbstractVersion {
    private final long versionNumber;

    public LongNumberVersion(long number, String user, Date time) {
        super(user, time);
        versionNumber = number;
    }

    public boolean different(Version version) {
        if(version instanceof LongNumberVersion) {
            LongNumberVersion other = (LongNumberVersion) version;
            return versionNumber != other.versionNumber;
        } else {
            return false;
        }
    }

    protected AbstractVersion next() {
        return new LongNumberVersion(versionNumber + 1, null, null);
    }

    public String toString() {
        return "LongNumberVersion#" + versionNumber + " " + ToString.timestamp(time);
    }
    
}


/*
 * Naked Objects - a framework that exposes behaviourally complete business objects directly to the user.
 * Copyright (C) 2000 - 2005 Naked Objects Group Ltd
 * 
 * This program is free software; you can redistribute it and/or modify it under the terms of the GNU General
 * Public License as published by the Free Software Foundation; either version 2 of the License, or (at your
 * option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program; if not, write to
 * the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 * 
 * The authors can be contacted via www.nakedobjects.org (the registered address of Naked Objects Group is
 * Kingsway House, 123 Goldworth Road, Woking GU21 1NR, UK).
 */