package org.nakedobjects.reflector.java.reflect;

import org.nakedobjects.object.Action;
import org.nakedobjects.object.ActionParameterSet;
import org.nakedobjects.object.Naked;
import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.NakedObjectSpecification;
import org.nakedobjects.object.control.Consent;
import org.nakedobjects.object.reflect.ActionPeer;
import org.nakedobjects.object.reflect.MemberIdentifier;

class DummyAction implements Action {

    private final ActionPeer peer;

    public DummyAction(ActionPeer peer) {
        this.peer = peer;
    }

    public int getParameterCount() {
        return 0;
    }

    public Type getType() {
        return peer.getType();
    }

    public Target getTarget() {
        return peer.getTarget();
    }

    public Object getExtension(Class cls) {
        return null;
    }
    
    public Class[] getExtensions() {
        return new Class[0];
    }

    public boolean hasReturn() {
        return false;
    }

    public NakedObjectSpecification[] getParameterTypes() {
        return null;
    }

    public Naked[] parameterStubs() {
        return null;
    }

    public NakedObjectSpecification getReturnType() {
        return null;
    }

    public ActionParameterSet getParameterSet(NakedObject object) {
        return null;
    }

    public Naked execute(NakedObject object, Naked[] parameters) {
        return null;
    }

    public MemberIdentifier getIdentifier() {
        return null;
    }

    public String getName() {
        return null;
    }

    public String getId() {
        return peer.getIdentifier().getName();
    }

    public Consent isUsable(NakedObject target) {
        return null;
    }

    public Consent hasValidParameters(NakedObject object, Naked[] parameters) {
        return null;
    }

    public String getDescription() {
        return null;
    }

    public Consent isVisible(NakedObject target) {
        return null;
    }

    public boolean isAuthorised() {
        return false;
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