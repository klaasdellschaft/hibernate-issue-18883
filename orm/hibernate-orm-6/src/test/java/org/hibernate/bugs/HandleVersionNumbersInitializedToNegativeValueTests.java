/*
 * Copyright 2014 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.hibernate.bugs;

import jakarta.persistence.*;
import org.hibernate.Hibernate;
import org.hibernate.cfg.AvailableSettings;
import org.hibernate.testing.orm.junit.*;
import org.junit.jupiter.api.Test;

import java.util.Objects;
import java.util.UUID;

import static jakarta.persistence.FetchType.LAZY;
import static org.assertj.core.api.Assertions.assertThat;

@DomainModel(
        annotatedClasses = {
                HandleVersionNumbersInitializedToNegativeValueTests.RootEntity.class,
                HandleVersionNumbersInitializedToNegativeValueTests.ChildEntity.class
        }
)
@ServiceRegistry(
        settings = {
                // For your own convenience to see generated queries:
                @Setting(name = AvailableSettings.SHOW_SQL, value = "true"),
                @Setting(name = AvailableSettings.FORMAT_SQL, value = "true"),
        }
)
@SessionFactory
class HandleVersionNumbersInitializedToNegativeValueTests {

    @Test
    void hhh18883Test(SessionFactoryScope scope) {
        var id = UUID.randomUUID();
        scope.inTransaction(session -> {
            RootEntity rootEntity = new RootEntity(id, new ChildEntity());
            session.persist(rootEntity);
        });

        scope.inTransaction(session -> {
            RootEntity rootEntity = session.find(RootEntity.class, id);
            assertThat(rootEntity).isNotNull();
            assertThat(rootEntity.getChildEntity()).isNotNull();
        });
    }


    @Entity
    @Table
    public static class RootEntity {

        @Id
        private UUID id;

        @OneToOne(mappedBy = "rootEntity", cascade = CascadeType.ALL)
        @PrimaryKeyJoinColumn
        private ChildEntity childEntity;

        @Version
        private int version = -1;

        public RootEntity() {
        }

        public RootEntity(UUID id, ChildEntity childEntity) {
            setId(id);
            setChildEntity(childEntity);
        }

        public UUID getId() {
            return id;
        }

        public void setId(UUID id) {
            this.id = id;
        }

        public ChildEntity getChildEntity() {
            return childEntity;
        }

        public void setChildEntity(ChildEntity childEntity) {
            this.childEntity = childEntity;
            if (childEntity != null) {
                childEntity.setRootEntity(this);
            }
        }

        public int getVersion() {
            return version;
        }

        public void setVersion(int version) {
            this.version = version;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) {
                return false;
            }
            RootEntity event = (RootEntity) o;
            return Objects.equals(id, event.id);
        }

        @Override
        public int hashCode() {
            return 0;
        }
    }

    @Entity
    @Table
    public static class ChildEntity {

        @Id
        private UUID id;

        @OneToOne(fetch = LAZY)
        @MapsId
        private RootEntity rootEntity;

        @Version
        private int version = -1;

        public ChildEntity() {
        }

        public UUID getId() {
            return id;
        }

        public void setId(UUID id) {
            this.id = id;
        }

        public RootEntity getRootEntity() {
            return rootEntity;
        }

        public void setRootEntity(RootEntity rootEntity) {
            this.rootEntity = rootEntity;
        }

        public int getVersion() {
            return version;
        }

        public void setVersion(int version) {
            this.version = version;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) {
                return false;
            }
            ChildEntity event = (ChildEntity) o;
            return Objects.equals(id, event.id);
        }

        @Override
        public int hashCode() {
            return 0;
        }
    }

}
