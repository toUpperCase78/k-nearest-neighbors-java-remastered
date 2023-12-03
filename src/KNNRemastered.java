import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.util.Random;
import javax.swing.JFrame;
import javax.swing.JPanel;
import java.util.ArrayList;
import java.util.Vector;

/**
 * @title K Nearest Neighbors Simulation in Java (REMASTERED)
 * @author Dogan Yigit Yenigun (a.k.a. toUpperCase78)
 * @version 2.0.3
 */

// The main starting point of the KNN simulation
public class KNNRemastered {
    public static void main(String[] args) {
        JFrame frame = new JFrame();
        KNN knn = new KNN();
        frame.add(knn);
        frame.setVisible(true);
        frame.setSize(knn.Hsize+16, knn.Vsize+90);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(false);
        frame.setTitle("K Nearest Neighbors Simulation REMASTERED");
    }
}

// Create a point class to hold (x,y) coordinates of each data object
class Point {
    int x, y;
    Point (int x, int y) {
        this.x = x;
        this.y = y;
    }
}

// The actual KNN class to perform all the functionalities
// As it can be noticed below, the hole code has many Arraylist data types for dynamic arrays during the KNN calculations.
class KNN extends JPanel implements KeyListener, MouseListener {
    boolean showCoords, showNumbers, showDistance, showConnection, showConvexHull, darkMode;
    int qx, qy, k, dataObject, Hsize, Vsize, qSpeed;
    double shortestDmax;
    Point[] points;
    Random rand = new Random();
    ArrayList<Double> distQ, distDO, shortestD, shortestD_DO;
    ArrayList<Integer> shortestDS, dataObject_ID, shortestDS_DO;
    ArrayList<ArrayList<Integer>> dataObject_KNN;
    ArrayList<Boolean> isNearest;
    Vector<Point> hullPoints;
    BufferedImage I;
    static Graphics2D g;

    public KNN() {
        addKeyListener(this);
        addMouseListener(this);
        setFocusable(true);
        setFocusTraversalKeysEnabled(true);
        // These boolean variables below are for controlling the visibility of the features that can be turned on/off
        showCoords = false;    showNumbers = false;    showDistance = false;
        showConnection = false;    showConvexHull = false;    darkMode = false;
        Hsize = 960;  Vsize = 700;  // Resolution of the applet window (minimum 800 x 600 recommended)
        k = 3;                      // Number k to determine the amount of the nearest data objects to be chosen (must be between 1 and 10)
        dataObject = 40;            // Number of data objects to take into account for all operations (must be between 10 and 100)
        qSpeed = 8;                 // Movement speed of the query object per stroke to arrow keys (must be between 1 and 20)
        points = new Point[100];    // Maximum capacity of data objects
        // These following four arrays below are for the query object
        distQ = new ArrayList<>();        // The distances of all data objects against the query object
        shortestD = new ArrayList<>();    // The shortest k distances to the query object
        shortestDS = new ArrayList<>();   // The data object numbers that are the k nearest to the query object
        isNearest = new ArrayList<>();    // Determine whether each data object is one of the k nearest
        // These following five arrays are for the data objects
        dataObject_ID = new ArrayList<>();    // Data object numbers consecutively
        dataObject_KNN = new ArrayList<>();   // The shortest k distances for each data object against the others
        distDO = new ArrayList<>();           // The distance of all data objects against one data object
        shortestD_DO = new ArrayList<>();     // The shortest k distances to one data object
        shortestDS_DO = new ArrayList<>();    // The data object numbers that are the k nearest to one data object
        hullPoints = new Vector<>();    // This vector array is for the convex hull of data objects being used
        I = new BufferedImage(Hsize, Vsize, BufferedImage.TYPE_INT_RGB);
        g = I.createGraphics();
        System.out.println("### STARTED KNN REMASTERED ###");
        doEverything();
    }

    public void doEverything() {
        createDataObjects();
        createQueryObject();
        calculateKNN_Query();
        calculateKNN_DataObjects();
        getConvexHull();
        showObjects();
    }

    // The method which is responsible for performing distance calculations (Euclidean)
    public double distance(int x1, int x2, int y1, int y2) {
        return Math.sqrt((x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2));
    }

    // Create up to 100 data objects by randomly assigning (x,y) values on the applet window
    public void createDataObjects() {
        for(int i = 0; i < 100; i++){
            int x = 15 + rand.nextInt(Hsize-30);
            int y = 15 + rand.nextInt(Vsize-50);
            points[i] = new Point(x, y);
        }
    }

    // Create the query object by randomly assigning (x,y) value on the applet window
    public void createQueryObject() {
        qx = 25 + rand.nextInt(Hsize-50);
        qy = 25 + rand.nextInt(Vsize-50);
    }

    // Perform distance calculations for the query object
    // Clearance is needed when new set of data objects is generated from the beginning or the query object was moved
    public void calculateKNN_Query() {
        distQ.clear();
        shortestD.clear();
        shortestDS.clear();
        // Put initial values (maximum distances & non-existent objects) to these two arrays
        for (int i = 0; i < k; i++) {
            shortestD.add(Double.MAX_VALUE - k + i + 1);
            shortestDS.add(dataObject+i);
        }

        isNearest.clear();
        // Initialize the status of nearest for all data objects to query as false
        // Then, get the distances against the query object
        for (int i = 0; i < dataObject; i++) {
            isNearest.add(false);
            distQ.add(distance(points[i].x, qx, points[i].y, qy));
            // If the distance for the data object is one of the k shortest so far, add it to the shortest distance
            // as well as its number to the related lists, and so remove the rightmost one
            for (int j = 0; j < k; j++) {
                if (distQ.get(i) < shortestD.get(j)){
                    shortestD.add(j, distQ.get(i));
                    shortestD.remove(k);
                    shortestDS.add(j, i);
                    shortestDS.remove(k);
                    break;
                }
            }
        }
        // Mark all k nearest objects as true to be shown properly on the applet window
        for (int i = 0; i < k; i++)
            isNearest.set(shortestDS.get(i), true);

        // Get the maximum distance among all k nearest data object, for determining the radius of query range
        shortestDmax = 0.0;
        for (int a = 0; a < k; a++) {
            if(shortestD.get(a) > shortestDmax)
                shortestDmax = shortestD.get(a);
        }

        // If showDistance is true, output the k shortest distances on the console
        // Format: [Data object number, distance to the query object]
        if (showDistance) {
            System.out.print("SHORTEST DISTANCES TO QUERY = {");
            for (int a = 0; a < k; a++)
                System.out.printf(a != k-1 ? "[%d, %.3f], " : "[%d, %.3f]", shortestDS.get(a), shortestD.get(a));
            System.out.println("}");
        }
    }

    // Perform distance calculations for all data objects
    // Every time the method is called, clear the values for the corresponding array lists below
    // The results are always shown on the console whenever new set of data objects were generated or the k value was changed
    public void calculateKNN_DataObjects() {
        dataObject_ID.clear();
        dataObject_KNN.clear();
        for (int i = 0; i < dataObject; i++) {
            distDO.clear();
            shortestD_DO.clear();
            shortestDS_DO.clear();
            // Put initial values (maximum distances & non-existent objects) to these two arrays
            for (int j = 0; j < k; j++) {
                shortestD_DO.add(Double.MAX_VALUE - k + j + 1);
                shortestDS_DO.add(dataObject+j);
            }
            // The distance calculations are roughly the same as those for the query object
            for (int j = 0; j < dataObject; j++) {
                if (j == i)
                    distDO.add(Double.MAX_VALUE);
                else {
                    distDO.add(distance(points[j].x, points[i].x, points[j].y, points[i].y));
                    for (int m = 0; m < k; m++) {
                        if (distDO.get(j) < shortestD_DO.get(m)) {
                            shortestD_DO.add(m, distDO.get(j));
                            shortestD_DO.remove(k);
                            shortestDS_DO.add(m, j);
                            shortestDS_DO.remove(k);
                            break;
                        }
                    }
                }
            }
            // When the kNN result is ready for the related data object, add them to more generic arrays.
            ArrayList<Integer> getResult = new ArrayList<>();
            for (int j = 0; j < shortestDS_DO.size(); j++)
                getResult.add(shortestDS_DO.get(j));
            dataObject_ID.add(i);
            dataObject_KNN.add(getResult);
        }
        // Show the necessary output for each data object
        System.out.print("DATA OBJECTS KNN = {");
        for (int i = 0; i < dataObject; i++) {
            System.out.print("[" + dataObject_ID.get(i) + ", " + dataObject_KNN.get(i) + "] ");
            if (i % 6 == 5)  System.out.println();
        }
        System.out.println("}");
    }

    // Orientation is used for the convex hull
    public int orientation(Point p, Point q, Point r) {
        int val = (q.y - p.y) * (r.x - q.x) - (q.x - p.x) * (r.y - q.y);
        if (val == 0)
            return 0;               // 0 = collinear
        return (val > 0) ? 1 : 2;   // 1 = clockwise    2 = counterclockwise
    }

    // Get the convex hull shape that is derived from data objects; at least 3 data objects required to operate
    public void getConvexHull() {
        if (dataObject < 3)
            return;
        hullPoints.clear();
        int l = 0;      // Get the leftmost data object
        for (int i = 1; i < dataObject; i++) {
            if (points[i].x < points[l].x)
                l = i;
        }
        int p = l, q;
        do {
            hullPoints.add(points[p]);   // Add the current data object to the convex hull points set
            q = (p + 1) % dataObject;
            for (int i = 0; i < dataObject; i++) {
                if (orientation(points[p], points[i], points[q]) == 2)
                    q = i;      // When a data object is more counterclockwise than current q, then update q
            }
            p = q;          // Set p to iterate
        } while (p != l);   // Continue until the starting point is reached
        // Show the output for the convex hull
        System.out.print("CONVEX HULL POINTS = ");
        for (Point temp : hullPoints)
            System.out.print("(" + temp.x + ", " + temp.y + ") ");
        System.out.println();
    }

    // Now the visual part, show all objects and drawings on the applet window
    public void showObjects() {
        // Set the background with white color first (dark grey for dark mode)
        for (int i = 0; i < Hsize; i++) {
            for (int j = 0; j < Vsize; j++) {
                I.setRGB(i, j, darkMode ? 1644825 : 16777215);
            }
        }
        g.setStroke(new BasicStroke(1));
        // Show the query object in red color
        g.setColor(Color.RED);
        g.fill(new Ellipse2D.Double(qx - 5, qy - 5, 10, 10));
        // If showCoords is true, display (x,y) coordinate of the query object on the applet window
        // The position is dependent on the coordinate values
        if (showCoords) {
            if (qy > Vsize - 25 && qx > Hsize - 70)
                g.drawString("(" + qx + ", " + qy + ")", qx-55, qy-8);
            else if (qy > Vsize - 25)
                g.drawString("(" + qx + ", " + qy + ")", qx+3, qy-8);
            else if (qx > Hsize - 70)
                g.drawString("(" + qx + ", " + qy + ")", qx-55, qy+16);
            else  g.drawString("(" + qx + ", " + qy + ")", qx+3, qy+16);
        }

        // Show all available data objects
        for (int i = 0; i < dataObject; i++) {
            // If the data object is marked as one of the k nearest to the query object, show in blue color
            if (isNearest.get(i)) {
                g.setColor(new Color(0, 125, 255));
                g.fill(new Ellipse2D.Double(points[i].x - 3.5, points[i].y - 3.5, 7, 7));
                g.drawLine(points[i].x, points[i].y, qx, qy);
            }
            // Otherwise, show in black color (white for dark mode)
            else {
                g.setColor(darkMode ? Color.WHITE : Color.BLACK);
                g.fill(new Ellipse2D.Double(points[i].x - 3.5, points[i].y - 3.5, 7, 7));
            }
            // If showCoords is true, display (x,y) coordinates of all data objects on the applet window
            if (showCoords) {
                if (points[i].x < Hsize - 70)
                    g.drawString("(" + points[i].x + ", " + points[i].y + ")", points[i].x+3, points[i].y+15);
                else  g.drawString("(" + points[i].x + ", " + points[i].y + ")", points[i].x-55, points[i].y+15);
            }
            // If showNumbers is true, display data objects' numbers on the applet window
            if (showNumbers) {
                if (i < 10)
                    g.drawString(i + "", points[i].x-11, points[i].y-1);
                else  g.drawString(i + "", points[i].x-18, points[i].y-1);
            }
        }
        // Draw the query range circle in red color (radius is equal to the maximum distance among all k nearest data objects)
        int shortestDm = (int) shortestDmax;
        g.setColor(Color.RED);
        g.drawOval(qx-shortestDm, qy-shortestDm, shortestDm*2, shortestDm*2);

        // If showConnection is true, show kNN connections for all data objects to each other, in green color
        if (showConnection) {
            g.setColor(new Color(0, 180, 0));
            for (int i = 0; i < dataObject_KNN.size(); i++) {
                ArrayList<Integer> getResult = new ArrayList<>();
                getResult = dataObject_KNN.get(i);
                for (int j = 0; j < getResult.size(); j++)
                    g.drawLine(points[i].x, points[i].y, points[getResult.get(j)].x, points[getResult.get(j)].y);
            }
        }

        // If showConvexHull is true, show the convex hull derived from the data objects, in purple color
        if (showConvexHull) {
            int h = hullPoints.size();
            g.setStroke(new BasicStroke(2));
            g.setColor(new Color(182, 77, 185));
            for (int i = 0; i < h; i++) {
                if (i == 0)
                    g.drawLine(hullPoints.get(i).x, hullPoints.get(i).y, hullPoints.get(h-1).x, hullPoints.get(h-1).y);
                else  g.drawLine(hullPoints.get(i).x, hullPoints.get(i).y, hullPoints.get(i-1).x, hullPoints.get(i-1).y);
            }
        }
    }

    // Show the necessary texts at the bottom of the applet window
    public void paint(Graphics g) {
        g.drawImage(I, 0, 0, this);
        g.setColor(darkMode ? new Color(50, 50, 50) : new Color(220, 220, 220));
        g.fillRect(0, Vsize, Hsize, 52);
        g.setColor(darkMode ? Color.WHITE : Color.BLACK);
        g.setFont(new Font("Arial", Font.BOLD, 15));
        g.drawString("k = " + k + " | Q. Speed = " + qSpeed, 15, Vsize+20);
        g.drawString("Data Objects = " + dataObject, 15, Vsize+42);
        g.setFont(new Font("Arial", Font.PLAIN, 15));
        g.drawString("Space = Create New  |  Arrow Keys = Move Query  |  Z / X = Change k  |  C / V = Change Q. Speed  |  B / N = Change DO", 170, Vsize+20);
        g.drawString("A = Numbers  |  S = Coords  |  D = Distance  |  F = Connections  |  G = Convex Hull  |  Q = Light / Dark Mode", 170, Vsize+42);
    }

    // These boolean switch methods are needed for turning on/off the functionalities on the applet window
    public void setCoords() {showCoords = !showCoords;}
    public void setNumbers() {showNumbers = !showNumbers;}
    public void setDistance() {showDistance = !showDistance;}
    public void setConnection() {showConnection = !showConnection;}
    public void setConvexHullFlag() {showConvexHull = !showConvexHull;}
    public void setDarkMode() {darkMode = !darkMode;}

    // Mandatory methods for key listener
    public void keyPressed(KeyEvent e) {
        int code = e.getKeyCode();
        if (code == KeyEvent.VK_A) {    // A = Show/hide each data object's number
            setNumbers();
            showObjects();
            repaint();
        }
        if (code == KeyEvent.VK_S) {    // S = Show/hide each data object's coordinates
            setCoords();
            showObjects();
            repaint();
        }
        if (code == KeyEvent.VK_D) {    // D = Show/hide distances of the k nearest data objects on the console
            setDistance();
            if (showDistance)
                System.out.println("ENABLED DISTANCE OUTPUT");
            else  System.out.println("DISABLED DISTANCE OUTPUT");
        }
        if (code == KeyEvent.VK_F ){    // F = Show/hide connections for all data objects based on their kNN results
            setConnection();
            showObjects();
            repaint();
        }
        if (code == KeyEvent.VK_G) {    // G = Show/hide the convex hull that is derived from selected data objects
            setConvexHullFlag();
            showObjects();
            repaint();
        }
        if (code == KeyEvent.VK_Q) {    // Q = Toggle the dark mode on/off
            setDarkMode();
            showObjects();
            repaint();
        }
        if (code == KeyEvent.VK_SPACE) {    // SPACE = Generate new set of data objects and the query object (completely randomized)
            System.out.println("### CREATED NEW SET OF DATA OBJECTS ###");
            doEverything();
            repaint();
        }
        if (code == KeyEvent.VK_UP) {    // UP ARROW = Move the query object up by speed value
            if (qy > 0) {
                qy -= qSpeed;
                if (qy < 0)    qy = 0;
                calculateKNN_Query();
                showObjects();
                repaint();
            }
        }
        if (code == KeyEvent.VK_DOWN) {    // DOWN ARROW = Move the query object down by speed value
            if(qy < Vsize){
                qy += qSpeed;
                if (qy > Vsize)    qy = Vsize;
                calculateKNN_Query();
                showObjects();
                repaint();
            }
        }
        if (code == KeyEvent.VK_LEFT) {    // LEFT ARROW = Move the query object to the left by speed value
            if (qx > 0) {
                qx -= qSpeed;
                if (qx < 0)    qx = 0;
                calculateKNN_Query();
                showObjects();
                repaint();
            }
        }
        if (code == KeyEvent.VK_RIGHT) {    // RIGHT ARROW = Move the query object to the right by speed value
            if(qx < Hsize){
                qx += qSpeed;
                if (qx > Hsize)    qx = Hsize;
                calculateKNN_Query();
                showObjects();
                repaint();
            }
        }
        if (code == KeyEvent.VK_Z) {    // Z = Decrease k value (minimum = 1)
            if (k > 1) {
                k--;
                System.out.println("NEW k VALUE = " + k);
                calculateKNN_Query();
                calculateKNN_DataObjects();
                showObjects();
                repaint();
            }
        }
        if (code == KeyEvent.VK_X) {    // X = Increase k value (maximum = 10)
            if (k < 10) {
                k++;
                System.out.println("NEW k VALUE = " + k);
                calculateKNN_Query();
                calculateKNN_DataObjects();
                showObjects();
                repaint();
            }
        }
        if (code == KeyEvent.VK_C) {    // C = Decrease the query object's movement speed (minimum = 1)
            if (qSpeed > 1) {
                qSpeed -= 1;
                System.out.println("QUERY OBJECT SPEED = " + qSpeed + " px");
                repaint();
            }
        }
        if (code == KeyEvent.VK_V) {    // V = Increase the query object's movement speed (maximum = 20)
            if (qSpeed < 20) {
                qSpeed += 1;
                System.out.println("QUERY OBJECT SPEED = " + qSpeed + " px");
                repaint();
            }
        }
        if (code == KeyEvent.VK_B) {    // B = Decrease the number of data objects (minimum = 10)
            if (dataObject > 10) {
                dataObject -= 5;
                System.out.println("THERE ARE NOW " + dataObject + " DATA OBJECTS IN THE AREA");
                calculateKNN_Query();
                calculateKNN_DataObjects();
                getConvexHull();
                showObjects();
                repaint();
            }
        }
        if (code == KeyEvent.VK_N) {    // N = Increase the number of data objects (maximum = 100)
            if (dataObject < 100) {
                dataObject += 5;
                System.out.println("THERE ARE NOW " + dataObject + " DATA OBJECTS IN THE AREA");
                calculateKNN_Query();
                calculateKNN_DataObjects();
                getConvexHull();
                showObjects();
                repaint();
            }
        }
    }
    public void keyReleased(KeyEvent e) {}
    public void keyTyped(KeyEvent e) {}

    // Mandatory methods for mouse listener
    // The query object will be moved directly to the (x,y) coordinate where the mouse is pointing at,
    // upon the press of a mouse button (be it left, middle or right) on the valid area of the applet window
    public void mousePressed(MouseEvent e) {
        if (e.getY() <= Vsize) {
            qx = e.getX();
            qy = e.getY();
            calculateKNN_Query();
            showObjects();
            repaint();
        }
    }
    public void mouseClicked(MouseEvent e) {}
    public void mouseEntered(MouseEvent e) {}
    public void mouseExited(MouseEvent e) {}
    public void mouseReleased(MouseEvent e) {}
}
