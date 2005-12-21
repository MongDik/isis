package org.nakedobjects.example.movie.objectstore;

import org.nakedobjects.application.value.Date;
import org.nakedobjects.example.movie.bom.Person;
import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.NakedObjects;
import org.nakedobjects.object.Oid;
import org.nakedobjects.object.ResolveState;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;

import org.apache.log4j.Logger;


public class PersonMapper implements SqlMapper {
    private static final Logger LOG = Logger.getLogger(PersonMapper.class);

    public PersonMapper() {
        super();
    }

    public NakedObject[] getInstances(Connection connection) throws SQLException {
        NakedObject[] instances = new NakedObject[45];
        int i = 0;
        String query = "select * from person";
        Statement s = connection.createStatement();
        ResultSet rs = s.executeQuery(query);
        while (rs.next() && i < instances.length) {
            Oid oid = new SqlOid(rs.getInt(1));
            Person person;
            if(NakedObjects.getObjectLoader().isIdentityKnown(oid)) {
                instances[i++] = NakedObjects.getObjectLoader().getAdapterFor(oid);
                person = (Person) NakedObjects.getObjectLoader().getAdapterFor(oid).getObject();
            } else {
                NakedObject instance = NakedObjects.getObjectLoader().recreateAdapterForPersistent(oid, NakedObjects.getSpecificationLoader().loadSpecification(Person.class));
                NakedObjects.getObjectLoader().start(instance, ResolveState.RESOLVING);
                person = (Person) instance.getObject();
                person.setName(rs.getString(2));
                NakedObjects.getObjectLoader().end(instance);
                instances[i++] = instance;
            }
        }
        rs.close();
        s.close();
        NakedObject[] results = new NakedObject[i];
        System.arraycopy(instances, 0, results, 0, i);
        return results;
    }


    public void insert(Connection connection, NakedObject object, int id) throws SQLException {
        PreparedStatement s = connection.prepareStatement("insert into person (name, dob, PKid) values(?, ?, ?)");
        setParameters(object, id, s);
        execute(s);
    }

    private void setParameters(NakedObject object, int id, PreparedStatement s) throws SQLException {
        Person person = (Person) object.getObject();
        s.setString(1, person.getName());
        Date date = person.getDate();
        if(date == null) {
            s.setNull(2, Types.DATE);
        } else {
            s.setDate(2, new java.sql.Date(date.longValue()));
        }
        s.setInt(3, id);
    }



    public void update(Connection connection, NakedObject object, int id) throws SQLException {
        PreparedStatement s = connection.prepareStatement("update person set name = ?, dob = ? where PKid = ?");
        setParameters(object, id, s);
        execute(s);
    }

    private void execute(PreparedStatement s) throws SQLException {
        LOG.debug(s);
        s.execute();
        s.close();
    }

    public void delete(Connection connection, int id) throws SQLException {
        PreparedStatement s = connection.prepareStatement("delete from person where PKid= ?");
        s.setInt(1, id);
        execute(s);
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