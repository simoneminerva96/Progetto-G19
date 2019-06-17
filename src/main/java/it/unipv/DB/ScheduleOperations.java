package it.unipv.DB;

import it.unipv.gui.common.MovieSchedule;
import it.unipv.utils.ApplicationException;
import it.unipv.utils.DataReferences;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ScheduleOperations {
    private DBConnection dbConnection;

    public ScheduleOperations(DBConnection dbConnection) { this.dbConnection = dbConnection; }

    public List<MovieSchedule> retrieveMovieSchedules() {
        return doRetrieveMovieSchedules();
    }

    public void insertNewMovieSchedule(MovieSchedule toInsert) {
        doInsertNewMovieSchedule(toInsert);
    }

    public void deleteMovieSchedule(MovieSchedule toDelete) {
        doDeleteMovieSchedule(toDelete);
    }

    private void doDeleteMovieSchedule(MovieSchedule toDelete) {
        String query = "DELETE FROM "+ DataReferences.DBNAME + ".PROGRAMMAZIONIFILM where CODICE_FILM = ? AND DATA = ? AND ORA = ? AND SALA = ?";
        try (PreparedStatement ps = dbConnection.getPreparedStatementFromQuery(query)) {
            ps.setString(1, toDelete.getMovieCode());
            ps.setString(2, toDelete.getDate());
            ps.setString(3, toDelete.getTime());
            ps.setString(4, toDelete.getHallName());
            ps.execute();
        } catch (SQLException e) {
            throw new ApplicationException(e);
        }
    }

    private void doInsertNewMovieSchedule(MovieSchedule toInsert) {
        String query = "INSERT INTO " + DataReferences.DBNAME + ".PROGRAMMAZIONIFILM (CODICE_FILM, DATA, ORA, SALA) values (?,?,?,?)";
        try (PreparedStatement ps = dbConnection.getPreparedStatementFromQuery(query)) {
            ps.setString(1, toInsert.getMovieCode());
            ps.setString(2, toInsert.getDate());
            ps.setString(3, toInsert.getTime());
            ps.setString(4, toInsert.getHallName());
            ps.execute();
        } catch (SQLException e) {
            throw new ApplicationException(e);
        }
    }

    private List<MovieSchedule> doRetrieveMovieSchedules() {
        try {
            return getMovieSchedulesFromResultSet(dbConnection.getResultFromQuery("SELECT * FROM " + DataReferences.DBNAME + ".PROGRAMMAZIONIFILM"));
        } catch (SQLException e) {
            throw new ApplicationException(e);
        }
    }

    private List<MovieSchedule> getMovieSchedulesFromResultSet(ResultSet resultSet) throws SQLException {
        try {
            List<MovieSchedule> res = new ArrayList<>();
            while(resultSet.next()) {
                MovieSchedule toAdd = new MovieSchedule();
                toAdd.setMovieCode(resultSet.getString("CODICE_FILM"));
                toAdd.setDate(resultSet.getString("DATA"));
                toAdd.setTime(resultSet.getString("ORA"));
                toAdd.setHallName(resultSet.getString("SALA"));
                res.add(toAdd);
            }
            return res;
        } finally {
            resultSet.close();
        }
    }
}
