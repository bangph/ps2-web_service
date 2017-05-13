package ps2.webservices;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import ps2.database.DatabaseHandler;
import ps2.objects.DataSet;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author Bang Pham Huu
 * mailto: b.phamhuu@jacobs-univeristy.de
 */
public class DataSetEndPoint extends HttpServlet {

    private DatabaseHandler databaseHandler = new DatabaseHandler();

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // pattern URL/dataset?request=...&params=...
        response.addHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Origin", "*");

        String requestParam = request.getParameter("request");
        // The data type (mars_mrdr, mars_trdr, moon)
        String type = request.getParameter("type");

        // Get all coverages with out params
        if (requestParam.equals("getAllCoverages")) {
            // all coverages in JSON
            getAllCoveragess(response, type);

        } // Get all coverages containing point user clicked (params (latPoint=...&longPoint=...))
        else if (requestParam.equals("getCoveragesContainingPoint")) {
            String latPoint = request.getParameter("latPoint");
            String longPoint = request.getParameter("longPoint");

            // all coverages containing point in JSON
            getCoveragessContainingPointHandle(response, latPoint, longPoint, type);
        } else if (requestParam.equals("getCoveragesIntersectBoundingBox")) {
            String minLat = request.getParameter("minLat");
            String minLong = request.getParameter("minLong");
            String maxLat = request.getParameter("maxLat");
            String maxLong = request.getParameter("maxLong");

            // all coverages intersecting bounding box in XML
            getCoveragesIntersectBoundingBox(response, minLat, minLong, maxLat, maxLong, type);
        } else if (requestParam.equals("getCoverage")) {
            String coverageId = request.getParameter("coverageID");
            if (coverageId == null) {
                coverageId = request.getParameter("coverageId");
            }

            getCoverageByCoverageID(response, coverageId);

            // get the coverage information by coverageID
        }
    }

    /**
     * Query all the coverages by type of image
     *
     */
    private void getAllCoveragess(HttpServletResponse response, String type) {
        List<DataSet> coverages = databaseHandler.getAllCoverages(type);
        dumpJSON(response, coverages);
    }

    /**
     * Query all the coverage by bounding box (intersect or within)
     *
     * @param response
     * @param minLat
     * @param minLong
     * @param maxLat
     * @param maxLong
     */
    private void getCoveragesIntersectBoundingBox(HttpServletResponse response, String minLat, String minLong, String maxLat, String maxLong, String type) throws IOException {
        List<DataSet> coverages = databaseHandler.getCoveragesIntersectBoundingBox(minLat, minLong, maxLat, maxLong, type);
        dumpXML(response, coverages);
    }

    /**
     * Query only information for coverageID
     *
     * @param response
     * @param coverageID
     */
    private void getCoverageByCoverageID(HttpServletResponse response, String coverageID) throws IOException {
        List<DataSet> coverages = databaseHandler.getCoverageByCoverageID(coverageID);
        dumpXML(response, coverages);
    }

    /**
     * Query all the coverages containing the clicked coordinate
     *
     * @param latPoint
     * @param longPoint
     */
    private void getCoveragessContainingPointHandle(HttpServletResponse response, String latPoint, String longPoint, String type) {
        List<DataSet> coverages = databaseHandler.getCoverageContainingPoint(latPoint, longPoint, type);
        dumpJSON(response, coverages);
    }

    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Dump json data to client
     *
     * @param returnValue the real json data
     */
    private void dumpJSON(HttpServletResponse response, List<DataSet> object) {
        ObjectMapper mapper = new ObjectMapper();
        String returnJSON = "";
        try {
            //Object to JSON in String
            returnJSON = mapper.writeValueAsString(object);
            System.out.println(returnJSON);
        } catch (JsonProcessingException ex) {
            Logger.getLogger(DataSetEndPoint.class.getName()).log(Level.SEVERE, null, ex);
        }
        // Dump value to request
        response.setContentType("application/json");
        PrintWriter out;
        try {
            out = response.getWriter();
            out.println(returnJSON);
            out.flush();
        } catch (IOException ex) {
            Logger.getLogger(DataSetEndPoint.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Dump as XML to client
     *
     * @param response
     * @param object
     */
    private void dumpXML(HttpServletResponse response, List<DataSet> datasets) {
        String TEMPLATE = "<?xml version=\"1.0\" encoding=\"utf-8\"?>"
                + "<PS2Results>"
                + " <Status>Sucess</Status>"
                + " <Products>"
                + " $products"
                + " </Products>"
                + "</PS2Results>";

        String PRODUCT_TEMPLATE = " <Product>"
                + "  <pdsid>$coverageID</pdsid>"
                + "  <type>$type</type>"
                + "  <boundingbox>"
                + "   <minLat>$minLat</minLat>"
                + "   <minLong>$minLong</minLong>"
                + "   <maxLat>$maxLat</maxLat>"
                + "   <maxLong>$maxLong</maxLong>"
                + "  </boundingbox>"
                + "  <centroid>"
                + "   <centroidLat>$centroid_latitude</centroidLat>"
                + "   <centroidLong>$centroid_longitude</centroidLong>"
                + "  </centroid>"
                + "  <latList>$latList</latList>"
                + "  <longList>$longList</longList>"
                + "  <width>$width</width>"
                + "  <height>$height</height>"
                + "  <resolution>$resolution</resolution>"
                + " </Product>";

        String products = "";
        for (DataSet dataset : datasets) {
            String latList = this.listToString(dataset.latList);
            String longList = this.listToString(dataset.longList);
            String product = PRODUCT_TEMPLATE.replace("$coverageID", dataset.coverageID)
                    .replace("$type", dataset.type)
                    .replace("$minLat", String.valueOf(dataset.Minimum_latitude))
                    .replace("$minLong", String.valueOf(dataset.Westernmost_longitude))
                    .replace("$maxLat", String.valueOf(dataset.Maximum_latitude))
                    .replace("$maxLong", String.valueOf(dataset.Easternmost_longitude))
                    .replace("$centroid_latitude", String.valueOf(dataset.centroid_latitude))
                    .replace("$centroid_longitude", String.valueOf(dataset.centroid_longitude))
                    .replace("$latList", latList)
                    .replace("$longList", longList)
                    .replace("$width", String.valueOf(dataset.width))
                    .replace("$height", String.valueOf(dataset.height))
                    .replace("$resolution", String.valueOf(dataset.resolution));
            products = products + "\n" + product;
        }

        String returnXML = TEMPLATE.replace("$products", products);

        // Dump string to request
        response.setContentType("application/xml");
        PrintWriter out;
        try {
            out = response.getWriter();
            out.println(returnXML);
            out.flush();
        } catch (IOException ex) {
            Logger.getLogger(DataSetEndPoint.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Convert a list of strings to a string
     *
     * @param list
     * @return
     */
    private String listToString(List<Double> list) {
        StringBuilder sb = new StringBuilder();

        for (Double d : list) {
            sb.append(d).append(',');
        }

        sb.deleteCharAt(sb.length() - 1); //delete last comma
        String newString = sb.toString();
        return newString;
    }
}
