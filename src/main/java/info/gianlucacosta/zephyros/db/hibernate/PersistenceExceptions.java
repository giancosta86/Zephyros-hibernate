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

import info.gianlucacosta.zephyros.db.sql.SqlState;
import org.hibernate.JDBCException;

import javax.persistence.PersistenceException;
import java.util.Optional;


public interface PersistenceExceptions {
    /**
     * Analyzes the given PersistenceException and tries to return its SQL state
     * as an SqlState enum value.
     *
     * @param persistenceException The PersistenceException to analyze
     * @return The SqlState, if its code is found and known; empty otherwise.
     */
    static Optional<SqlState> getSqlState(PersistenceException persistenceException) {
        if (persistenceException.getCause() instanceof JDBCException) {
            JDBCException jdbcException =
                    (JDBCException) persistenceException.getCause();

            return SqlState.getByCode(jdbcException.getSQLState());
        }

        return Optional.empty();
    }
}
