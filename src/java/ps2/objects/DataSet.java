/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ps2.objects;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Bang Pham Huu mailto: b.phamhuu@jacobs-univeristy.de
 */
public class DataSet {

    public String coverageID = "";
    // mars_mrdr, mars_trdr, moon
    public String type = "";
    public List<Double> latList = new ArrayList<Double>();
    public List<Double> longList = new ArrayList<Double>();
    public double Easternmost_longitude = 0;
    public double Maximum_latitude = 0;
    public double Minimum_latitude = 0;
    public double Westernmost_longitude = 0;
    public double resolution = 0;
    public double width = 0;
    public double height = 0;
    public double centroid_latitude = 0;
    public double centroid_longitude = 0;

}
