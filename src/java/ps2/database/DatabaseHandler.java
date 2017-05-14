/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ps2.database;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import ps2.objects.DataSet;

/**
 *
 * @author Bang Pham Huu mailto: b.phamhuu@jacobs-univeristy.de
 */
public class DatabaseHandler {        

    private Connection conn;

    // Always query with all the fields from dataset table        
    private final String SELECT_QUERY = "SELECT coverageid, type, easternmost_longitude, maximum_latitude, minimum_latitude, \n"
            + "westernmost_longitude, centroid_longitude, centroid_latitude, width, height, resolution,"
            + "minimum_east, minimum_north, maximum_east, maximum_north,"
            + "ST_AsText(ST_FlipCoordinates(shape)) as polygon\n"
            + " from dataset ";

    public DatabaseHandler() throws RuntimeException {
        try {
            connect();
        } catch(Exception ex) {
            throw new RuntimeException("Cannot write log file: ", ex);
        }
    }

    public void connect() throws IOException {
        try {
            Class.forName("org.postgresql.Driver");
            conn = DriverManager.getConnection("jdbc:postgresql://localhost:5432/ps2", "ps2user", "ps2user");
        } catch (Exception e) {
            MessageLog.write(e.getMessage());
            e.printStackTrace();
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
        }
        System.out.println("Opened database successfully");
    }

    /**
     * Get all coverages to load footprints by type
     *
     * @param type
     * @return
     */
    public List<DataSet> getAllCoverages(String type) throws IOException {
        List<DataSet> dataSetList = new ArrayList<DataSet>();
        try {
            String query = SELECT_QUERY + "where type='" + type + "'";
            MessageLog.write(query);
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery(query);

            // returns the dataset footprints conting point
            while (rs.next()) {
                DataSet dataSet = this.buildDataSet(rs);

                dataSetList.add(dataSet);
            }
            rs.close();
            st.close();
        } catch (SQLException se) {
            System.err.println("Threw a SQLException creating the list of layers.");
            System.err.println(se.getMessage());
        }

        return dataSetList;
    }

    /**
     * Query metadata for coverageID
     *
     * @param coverageID
     * @return
     */
    public List<DataSet> getCoverageByCoverageID(String coverageID) throws IOException {
        List<DataSet> dataSetList = new ArrayList<DataSet>();
        try {
            String query = SELECT_QUERY + "where coverageid='" + coverageID + "'";
            MessageLog.write(query);
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery(query);

            // returns the dataset footprints conting point
            while (rs.next()) {
                DataSet dataSet = this.buildDataSet(rs);

                dataSetList.add(dataSet);
            }
            rs.close();
            st.close();
        } catch (SQLException se) {
            System.err.println("Threw a SQLException creating the list of layers.");
            System.err.println(se.getMessage());
        }

        return dataSetList;
    }

    /**
     * Get all coverages containing clicked point
     *
     * @param latPoint
     * @param longPoint
     * @param type
     * @return
     */
    public List<DataSet> getCoverageContainingPoint(String latPoint, String longPoint, String type) throws IOException {
        List<DataSet> dataSetList = new ArrayList<DataSet>();
        try {
            String query = SELECT_QUERY + "where type='" + type + "' and st_contains(shape, ST_GeomFromText('POINT($longPoint $latPoint)')) = 't'";
            query = query.replace("$longPoint", longPoint);
            query = query.replace("$latPoint", latPoint);
            MessageLog.write(query);

            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery(query);

            // returns the dataset footprints conting point
            while (rs.next()) {
                DataSet dataSet = this.buildDataSet(rs);

                dataSetList.add(dataSet);
            }
            rs.close();
            st.close();
        } catch (SQLException se) {
            System.err.println("Threw a SQLException creating the list of layers.");
            System.err.println(se.getMessage());
        }

        return dataSetList;
    }

    /**
     * Get all coverages intersecting with bounding box by type
     *
     * @param minLat
     * @param minLong
     * @param maxLat
     * @param maxLong
     * @param type
     * @return
     */
    public List<DataSet> getCoveragesIntersectBoundingBox(String minLat, String minLong, String maxLat, String maxLong, String type) throws IOException {
        List<DataSet> dataSetList = new ArrayList<DataSet>();
        try {
            String query = SELECT_QUERY + " where type='" + type + "' and st_intersects(shape, ST_GeomFromText('POLYGON(($minLong $minLat, $maxLong $minLat, $maxLong $maxLat, $minLong $maxLat, $minLong $minLat))')) = 't'";
            query = query.replace("$minLong", minLong);
            query = query.replace("$minLat", minLat);
            query = query.replace("$maxLong", maxLong);
            query = query.replace("$maxLat", maxLat);
            MessageLog.write(query);

            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery(query);

            // returns the dataset footprints conting point
            while (rs.next()) {
                DataSet dataSet = this.buildDataSet(rs);

                dataSetList.add(dataSet);
            }
            rs.close();
            st.close();
        } catch (SQLException se) {
            System.err.println("Threw a SQLException creating the list of layers.");
            System.err.println(se.getMessage());
        }

        return dataSetList;
    }

    private DataSet buildDataSet(ResultSet rs) throws SQLException {
        DataSet dataSet = new DataSet();
        dataSet.coverageID = rs.getString("coverageid").trim();
        dataSet.type = rs.getString("type");
        dataSet.easternmost_longitude = rs.getDouble("easternmost_longitude");
        dataSet.maximum_latitude = rs.getDouble("maximum_latitude");
        dataSet.minimum_latitude = rs.getDouble("minimum_latitude");
        dataSet.westernmost_longitude = rs.getDouble("westernmost_longitude");
        dataSet.centroid_longitude = rs.getDouble("centroid_longitude");
        dataSet.centroid_latitude = rs.getDouble("centroid_latitude");
        dataSet.width = rs.getInt("width");
        dataSet.height = rs.getInt("height");        
        dataSet.resolution = rs.getDouble("resolution");
        dataSet.minimum_east = rs.getDouble("minimum_east");
        dataSet.minimum_north = rs.getDouble("minimum_north");
        dataSet.maximum_east = rs.getDouble("maximum_east");
        dataSet.maximum_north = rs.getDouble("maximum_north");
        
        

        List<ArrayList<Double>> list = getLatLongList(rs.getString("polygon"));
        dataSet.latList = list.get(0);
        dataSet.longList = list.get(1);

        return dataSet;
    }

    private List<ArrayList<Double>> getLatLongList(String polygon) {
        List<ArrayList<Double>> list = new ArrayList<ArrayList<Double>>();
        String tmp = polygon;
        tmp = tmp.substring(tmp.lastIndexOf("(") + 1, tmp.lastIndexOf(")") - 1);
        String[] points = tmp.split(",");

        ArrayList<Double> longList = new ArrayList<Double>();
        ArrayList<Double> latList = new ArrayList<Double>();
        // Parse polygon and get the latitude, longtitude                
        for (String point : points) {
            String[] tmpPoint = point.split(" ");
            // Already flip in postgreql then it is lat long
            latList.add(Double.parseDouble(tmpPoint[0].trim()));
            longList.add(Double.parseDouble(tmpPoint[1].trim()));
        }
        list.add(latList);
        list.add(longList);

        return list;
    }
}
