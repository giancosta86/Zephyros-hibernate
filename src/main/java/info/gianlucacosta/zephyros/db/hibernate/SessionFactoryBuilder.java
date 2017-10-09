/*^
  ===========================================================================
  Zephyros - Hibernate
  ===========================================================================
  Copyright (C) 2017 Gianluca Costa
  ===========================================================================
  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
  ===========================================================================
*/

package info.gianlucacosta.zephyros.db.hibernate;

import org.hibernate.SessionFactory;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.AvailableSettings;
import org.hibernate.service.ServiceRegistry;

import java.net.URL;

/**
 * Simplified builder for creating a SessionFactory.
 * <p>
 * Most of its methods support fluent method chaining.
 */
public class SessionFactoryBuilder {
    private final MetadataSources metadataSources;

    /**
     * @param connectionString The connection string to the target database
     */
    public SessionFactoryBuilder(String connectionString) {
        ServiceRegistry standardRegistry =
                new StandardServiceRegistryBuilder()
                        .applySetting(AvailableSettings.URL, connectionString)
                        .build();

        metadataSources =
                new MetadataSources(standardRegistry);
    }


    /**
     * Adds the given JPA-annotated classes to the metadata source
     *
     * @param annotatedClasses The classes to register
     * @return The builder itself, for fluent method chaining
     */
    public SessionFactoryBuilder addAnnotatedClasses(Class<?>... annotatedClasses) {
        for (Class<?> annotatedClass : annotatedClasses) {
            metadataSources.addAnnotatedClass(annotatedClass);
        }

        return this;
    }


    /**
     * Adds the given resources (for example, .hbm.xml files containing mappings and/or
     * named queries) to the metadata
     *
     * @param resourceUrls The URLs of the resources to register
     * @return The builder itself, for fluent method chaining
     */
    public SessionFactoryBuilder addResources(URL... resourceUrls) {
        for (URL resourceUrl : resourceUrls) {
            metadataSources.addResource(
                    resourceUrl.toExternalForm()
            );
        }

        return this;
    }

    /**
     * @return The internal MetadataSources object,
     * later used to create the SessionFactory
     */
    public MetadataSources getMetadataSources() {
        return metadataSources;
    }

    /**
     * Builds a Hibernate session factory using the cumulated metadata
     *
     * @return The session factory
     */
    public SessionFactory buildSessionFactory() {
        Metadata metadata =
                metadataSources
                        .buildMetadata();

        return metadata
                .buildSessionFactory();
    }
}
