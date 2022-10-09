/*
 * Copyright 2022 Centre for Computational Geography.
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
package uk.ac.leeds.ccg.r3d.io;

import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import uk.ac.leeds.ccg.v3d.core.V3D_Environment;
import uk.ac.leeds.ccg.v3d.geometry.V3D_Triangle;
import uk.ac.leeds.ccg.v3d.geometry.V3D_Vector;

/**
 * For reading STL Files.
 *
 * @author Andy Turner
 */
public class STL_Reader {

    public ArrayList<V3D_Triangle> triangles;
    public Stats stats;
            
    /**
     * Create a new instance.
     */
    public STL_Reader(){
        triangles = new ArrayList<>();
    }
    
    public static void main(String[] args) {
        Path p = Paths.get("data", "Utah_teapot_(solid).stl");
        try {
            STL_Reader s = new STL_Reader();
            s.readBinary(p);
            //System.out.println(ts.get(0));
            System.out.println("minx=" + s.stats.minx);
            System.out.println("maxx=" + s.stats.maxx);
            System.out.println("miny=" + s.stats.miny);
            System.out.println("maxy=" + s.stats.maxy);
            System.out.println("minz=" + s.stats.minz);
            System.out.println("maxz=" + s.stats.maxz);
        } catch (IOException ex) {
            Logger.getLogger(STL_Reader.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Read the
     * <a href="https://en.wikipedia.org/wiki/STL_(file_format)#Binary_STL">binary
     * STL file</a> at the given Path. Everything is assumed to be little
     * endian. Report the min and max of the x, y, and z values.
     *
     * @param p The file to read.
     * @return An ArrayList of triangles read from the file.
     */
    public void readBinary(Path p) throws IOException {
        V3D_Environment e = new V3D_Environment();
        DataInputStream dis = new DataInputStream(new FileInputStream(p.toFile()));
        // Skip 80 bytes
        dis.read(new byte[80]);
        int nTriangles = Integer.reverseBytes(dis.readInt());
        System.out.println("Reading " + nTriangles + " triangles.");
        // read triangles
        float x, y, z, minx, maxx, miny, maxy, minz, maxz;
        // Read the first triangle
        x = readFloat(dis);
        y = readFloat(dis);
        z = readFloat(dis);
        V3D_Vector n = new V3D_Vector(x, y, z);
        x = readFloat(dis);
        y = readFloat(dis);
        z = readFloat(dis);
        stats = new Stats(x, y, z);
        V3D_Vector pv = new V3D_Vector(x, y, z);
        x = readFloat(dis);
        y = readFloat(dis);
        z = readFloat(dis);
        stats.update(x, y, z);
        V3D_Vector qv = new V3D_Vector(x, y, z);
        x = readFloat(dis);
        y = readFloat(dis);
        z = readFloat(dis);
        stats.update(x, y, z);
        V3D_Vector rv = new V3D_Vector(x, y, z);
        triangles.add(new V3D_Triangle(e, pv, qv, rv));
        // The attribute is not currently used.
        short attribute = Short.reverseBytes(dis.readShort());
        while (dis.available() > 0) {
            // The normal to the triangle is not currently used.
            n = new V3D_Vector(readFloat(dis), readFloat(dis), readFloat(dis));
            x = readFloat(dis);
            y = readFloat(dis);
            z = readFloat(dis);
            stats.update(x, y, z);
            pv = new V3D_Vector(x, y, z);
            x = readFloat(dis);
            y = readFloat(dis);
            z = readFloat(dis);
            stats.update(x, y, z);
            qv = new V3D_Vector(x, y, z);
            x = readFloat(dis);
            y = readFloat(dis);
            z = readFloat(dis);
            rv = new V3D_Vector(x, y, z);
            stats.update(x, y, z);
            // The attribute is not currently used.
            attribute = Short.reverseBytes(dis.readShort());
            triangles.add(new V3D_Triangle(e, pv, qv, rv));
        }
    }

    float readFloat(DataInputStream dis) throws IOException {
        return Float.intBitsToFloat(Integer.reverseBytes(dis.readInt()));
    }

    public class Stats {

        float minx, maxx, miny, maxy, minz, maxz;

        Stats(float x, float y, float z) {
            minx = maxx = x;
            miny = maxy = y;
            minz = maxz = z;
        }

        void update(float x, float y, float z) {
            minx = Math.min(x, minx);
            maxx = Math.max(x, maxx);
            miny = Math.min(y, miny);
            maxy = Math.max(y, maxy);
            minz = Math.min(z, minz);
            maxz = Math.max(z, maxz);
        }
    }
}
