/*^
  ===========================================================================
  Zephyros - Hibernate
  ===========================================================================
  Copyright (C) 2017-2018 Gianluca Costa
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

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

import javax.persistence.TypedQuery;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * Provides useful methods for dealing with sessions and transactions via
 * a functional approach.
 */
public class FunctionalSession {
    private final SessionFactory sessionFactory;

    /**
     * @param sessionFactory The backing, long-lived session factory
     */
    public FunctionalSession(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }


    /**
     * Executes a function in the context of a session, which gets automatically
     * opened before the function and closed after the function.
     *
     * @param function Session -&gt; T arbitrary function
     * @param <T>      The return type of the function and of the method itself
     * @return The function's return value
     */
    protected <T> T getInSession(Function<Session, T> function) {
        try (Session session = sessionFactory.openSession()) {
            return function.apply(session);
        }
    }


    /**
     * Executes actions in the context of a session, without returning values.
     * The session is automatically opened and closed by the underlying infrastructure.
     *
     * @param consumer Consumer operating on the session
     */
    protected void runInSession(Consumer<Session> consumer) {
        getInSession(session -> {
            consumer.accept(session);
            return null;
        });
    }


    /**
     * Executes a function in the context of a transaction automatically managed by
     * the infrastructure.
     * <p>
     * If the function does not perform commit/rollback, a
     * <b>commit</b> is executed if the function did not throw exceptions;
     * otherwise, a <b>rollback</b> is ensured.
     *
     * @param function (Session, Transaction) -&gt; T arbitrary function
     * @param <T>      The return type of the function and of the method itself
     * @return The function's result
     */
    protected <T> T getInTransaction(BiFunction<Session, Transaction, T> function) {
        return getInSession(session -> {
            Transaction transaction =
                    session.beginTransaction();

            try {
                T result =
                        function.apply(session, transaction);

                if (transaction.isActive()) {
                    transaction.commit();
                }

                return result;
            } finally {
                if (transaction.isActive()) {
                    transaction.rollback();
                }
            }
        });
    }


    /**
     * Executes actions in the context of a transaction automatically managed
     * by the underlying infrastructure, without returning values.
     * <p>
     * If the consumer does not perform commit/rollback, a
     * <b>commit</b> is executed if the consumer did not throw exceptions;
     * otherwise, a <b>rollback</b> is ensured.
     *
     * @param consumer Consumer accepting both the Session and the Transction (which
     *                 are automatically handled by the infrastructure)
     */
    protected void runInTransaction(BiConsumer<Session, Transaction> consumer) {
        getInTransaction((session, transaction) -> {
            consumer.accept(session, transaction);
            return null;
        });
    }


    /**
     * Runs a named query and returns its result set as a List
     *
     * @param queryName         The query name
     * @param resultElementType The type of the result element
     * @param <T>               The type of the result element
     * @return The list containing the query's result set
     */
    public <T> List<T> runNamedListQuery(String queryName, Class<T> resultElementType) {
        return getInSession(session -> {
            TypedQuery<T> query =
                    session.createNamedQuery(
                            queryName,
                            resultElementType
                    );

            return query.getResultList();
        });
    }



    /**
     * Runs a named query and returns its result set as a closeable stream
     *
     * @param queryName         The query name
     * @param resultElementType The type of the result element
     * @param <T>               The type of the result element
     * @return The stream containing the query's result set. It must be closed by the client
     * in order to close the underlying session object
     */
    public <T> Stream<T> runNamedStreamQuery(String queryName, Class<T> resultElementType) {
        Session session =
                sessionFactory.openSession();

        try {
            Stream<T> result =
                    session
                            .createNamedQuery(
                                    queryName,
                                    resultElementType
                            )
                            .stream();

            result.onClose(session::close);

            return result;
        } catch (Exception ex) {
            session.close();

            throw ex;
        }
    }


    /**
     * Saves / updates the given object, within a transaction block.
     *
     * @param object The object to save/update
     * @param <T>    The type of the object
     */
    public <T> void saveOrUpdateInTransaction(T object) {
        runInTransaction((session, transaction) ->
                session.saveOrUpdate(object)
        );
    }


    /**
     * Saves the given object, within a transaction block.
     *
     * @param object The object to save
     * @param <T>    The type of the object
     */
    public <T> void saveInTransaction(T object) {
        runInTransaction((session, transaction) ->
                session.save(object)
        );
    }


    /**
     * Updates the given object, within a transaction block.
     *
     * @param object The object to update
     * @param <T>    The type of the object
     */
    public <T> void updateInTransaction(T object) {
        runInTransaction((session, transaction) ->
                session.update(object)
        );
    }


    /**
     * Deletes the given object, within a transaction block
     *
     * @param object The object to delete
     * @param <T>    The type of the object
     */
    public <T> void deleteInTransaction(T object) {
        runInTransaction((session, transaction) ->
                session.delete(object)
        );
    }
}
