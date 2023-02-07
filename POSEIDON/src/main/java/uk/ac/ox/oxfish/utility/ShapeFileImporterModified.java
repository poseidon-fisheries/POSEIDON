package uk.ac.ox.oxfish.utility;

import com.vividsolutions.jts.algorithm.CGAlgorithms;
import com.vividsolutions.jts.geom.*;
import sim.field.geo.GeomVectorField;
import sim.util.Bag;
import sim.util.geo.AttributeValue;
import sim.util.geo.MasonGeometry;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ShapeFileImporterModified {


    // Shape types included in ESRI Shapefiles. Not all of these are currently supported.

    final static int NULL_SHAPE = 0;
    final static int POINT = 1;
    final static int POLYLINE = 3;
    final static int POLYGON = 5;
    final static int MULTIPOINT = 8;
    final static int POINTZ = 11;
    final static int POLYLINEZ = 13;
    final static int POLYGONZ = 15;
    final static int MULTIPOINTZ = 18;
    final static int POINTM = 21;
    final static int POLYLINEM = 23;
    final static int POLYGONM = 25;
    final static int MULTIPOINTM = 28;
    final static int MULTIPATCH = 31;

    public static boolean isSupported(int shapeType)
    {
        switch (shapeType)
        {
            case POINT:
                return true;
            case POLYLINE:
                return true;
            case POLYGON:
                return true;
            default:
                return false;	// no other types are currently supported
        }
    }

    /** Create a polygon from an array of LinearRings.
     *
     * If there is only one ring the function will create and return a simple
     * polygon. If there are multiple rings, the function checks to see if any
     * of them are holes (which are in counter-clockwise order) and if so, it
     * creates a polygon with holes.  If there are no holes, it creates and
     * returns a multi-part polygon.
     *
     */
    private static Geometry createPolygon(LinearRing[] parts)
    {
        GeometryFactory geomFactory = new GeometryFactory();

        if (parts.length == 1)
        {
            return geomFactory.createPolygon(parts[0], null);
        }

        ArrayList<LinearRing> shells = new ArrayList<LinearRing>();
        ArrayList<LinearRing> holes = new ArrayList<LinearRing>();

        for (int i = 0; i < parts.length; i++)
        {
            if (CGAlgorithms.isCCW(parts[i].getCoordinates()))
            {
                holes.add(parts[i]);
            } else
            {
                shells.add(parts[i]);
            }
        }

        if (holes.size() > 0)
        {
            // Create a polygon with holes
            LinearRing[] holesArray = new LinearRing[holes.size()];
            holes.toArray(holesArray);
            return geomFactory.createPolygon(shells.get(0), holesArray);
        }

        // Create multi-polygon
        Polygon[] poly = new Polygon[shells.size()];
        for (int i = 0; i < shells.size(); i++)
        {
            poly[i] = geomFactory.createPolygon(parts[i], null);
        }
        return geomFactory.createMultiPolygon(poly);
    }



    /**
     * Wrapper function which creates a new array of LinearRings and calls
     * the other function.
     */
    private static Geometry createPolygon(Geometry[] parts)
    {
        LinearRing[] rings = new LinearRing[parts.length];
        for (int i = 0; i < parts.length; i++)
        {
            rings[i] = (LinearRing) parts[i];
        }

        return createPolygon(rings);
    }


    public static void read(final URL shpFile, GeomVectorField field, final Bag masked, Class<?> masonGeometryClass) throws FileNotFoundException
    {
        if (shpFile == null)
        {
            throw new IllegalArgumentException("shpFile is null; likely file not found");
        }

        if (! MasonGeometry.class.isAssignableFrom(masonGeometryClass))
        {
            throw new IllegalArgumentException("masonGeometryClass not a MasonGeometry class or subclass");
        }


        try
        {
            FileInputStream shpFileInputStream = new FileInputStream(shpFile.getFile());

            if (shpFileInputStream == null)
            {
                throw new FileNotFoundException(shpFile.getFile());
            }


            FileChannel channel = shpFileInputStream.getChannel();
            ByteBuffer byteBuf = channel.map(FileChannel.MapMode.READ_ONLY, 0, (int) channel.size());
            channel.close();
            shpFileInputStream.close();

            // Database file name is same as shape file name, except with '.dbf' extension
            String dbfFilename = shpFile.getFile().substring(0, shpFile.getFile().lastIndexOf('.')) + ".dbf";

            FileInputStream dbFileInputStream = new FileInputStream(dbfFilename);

            FileChannel dbChannel = dbFileInputStream.getChannel();
            ByteBuffer dbBuffer = dbChannel.map(FileChannel.MapMode.READ_ONLY, 0, (int) dbChannel.size());
            dbChannel.close();

            dbBuffer.order(ByteOrder.LITTLE_ENDIAN);
            int headerSize = dbBuffer.getShort(8);
            int recordSize = dbBuffer.getShort(10);

            int fieldCnt = (short) ((headerSize - 1) / 32 - 1);

            // Corresponds to a dBase field directory entry
            class FieldDirEntry
            {
                public String name;
                public int fieldSize;
            }

            FieldDirEntry fields[] = new FieldDirEntry[fieldCnt];

            RandomAccessFile inFile = new RandomAccessFile(dbfFilename, "r");

            if (inFile == null)
            {
                throw new FileNotFoundException(dbfFilename);
            }

            inFile.seek(32);

            byte c[] = new byte[32];
            char type[] = new char[fieldCnt];
            int length;

            for (int i = 0; i < fieldCnt; i++)
            {
                inFile.readFully(c, 0, 11);

                int j = 0;

                for (j = 0; j < 12 && c[j] != 0; j++);

                String name = new String(c, 0, j);

                type[i] = (char) inFile.readByte();

                fields[i] = new FieldDirEntry();

                fields[i].name = name;

                inFile.read(c, 0, 4);  // data address

                byte b = inFile.readByte();

                if (b > 0)
                {
                    length = (int) b;
                } else
                {
                    length = 256 + (int) b;
                }

                fields[i].fieldSize = length;

                inFile.skipBytes(15);
            }

            inFile.seek(0);
            inFile.skipBytes(headerSize);


            GeometryFactory geomFactory = new GeometryFactory();


            // advance to the first record
            byteBuf.position(100);

            while (byteBuf.hasRemaining())
            {
                // advance past two int: recordNumber and recordLength
                byteBuf.position(byteBuf.position() + 8);

                byteBuf.order(ByteOrder.LITTLE_ENDIAN);

                int recordType = byteBuf.getInt();

                if (!isSupported(recordType))
                {
                    return;		// all shapes are the same type so don't bother reading any more
                }

                // Read the attributes

                byte r[] = new byte[recordSize];
                inFile.read(r);

                int start1 = 1;

                // Contains all the attribute values keyed by name that will eventually
                // be copied over to a corresponding MasonGeometry wrapper.
                Map<String, AttributeValue> attributes = new HashMap<String, AttributeValue>(fieldCnt);

                //attributeInfo = new ArrayList<AttributeValue>();

                for (int k = 0; k < fieldCnt; k++)
                {
                    // It used to be that we'd just flag attributes not in
                    // the mask Bag as hidden; however, now we just don't
                    // bother adding it to the MasonGeometry.  If the user
                    // really wanted that attribute, they'd have added it to
                    // the mask in the first place
//                    if (masked != null && ! masked.contains(fields[k].name))
//                    {
//                        fld.setHidden(true);
//                    } else
//                    {
//                        fld.setHidden(false);
//                    }

                    // If the user bothered specifying a mask and the current
                    // attribute, as indexed by 'k', is NOT in the mask, then
                    // merrily skip on to the next attribute
                    if (masked != null && ! masked.contains(fields[k].name))
                    {
                        // But before we skip, ensure that we wind the pointer
                        // to the start of the next attribute value.
                        start1 += fields[k].fieldSize;

                        continue;
                    }

                    String rawAttributeValue = new String(r, start1, fields[k].fieldSize);
                    rawAttributeValue = rawAttributeValue.trim();

                    AttributeValue attributeValue = new AttributeValue();

                    if ( rawAttributeValue.isEmpty() )
                    {
                        // If we've gotten no data for this, then just add the
                        // empty string.
                        attributeValue.setString(rawAttributeValue);
                    }
                    else if (type[k] == 'N') // Numeric
                    {
                        if (rawAttributeValue.length() == 0)
                        {
                            attributeValue.setString("0");
                        }
                        if (rawAttributeValue.indexOf('.') != -1)
                        {
                            attributeValue.setDouble(Double.valueOf(rawAttributeValue));
                        } else
                        {
                            attributeValue.setInteger(Integer.valueOf(rawAttributeValue));
                        }
                    } else if (type[k] == 'L') // Logical
                    {
                        attributeValue.setValue(Boolean.valueOf(rawAttributeValue));
                    } else if (type[k] == 'F') // Floating point
                    {
                        attributeValue.setValue(Double.valueOf(rawAttributeValue));
                    }
                    else
                    {
                        attributeValue.setString(rawAttributeValue);
                    }

                    attributes.put(fields[k].name, attributeValue);

                    start1 += fields[k].fieldSize;
                }

                inFile.close();
                // Read the shape

                Geometry geom = null;

                if (recordType == POINT)
                {
                    Coordinate pt = new Coordinate(byteBuf.getDouble(), byteBuf.getDouble());
                    geom = geomFactory.createPoint(pt);
                } else if (recordType == POLYLINE || recordType == POLYGON)
                {
                    // advance past four doubles: minX, minY, maxX, maxY
                    byteBuf.position(byteBuf.position() + 32);

                    int numParts = byteBuf.getInt();
                    int numPoints = byteBuf.getInt();

                    // get the array of part indices
                    int partIndicies[] = new int[numParts];
                    for (int i = 0; i < numParts; i++)
                    {
                        partIndicies[i] = byteBuf.getInt();
                    }

                    // get the array of points
                    Coordinate pointsArray[] = new Coordinate[numPoints];
                    for (int i = 0; i < numPoints; i++)
                    {
                        pointsArray[i] = new Coordinate(byteBuf.getDouble(), byteBuf.getDouble());
                    }

                    Geometry[] parts = new Geometry[numParts];

                    for (int i = 0; i < numParts; i++)
                    {
                        int start = partIndicies[i];
                        int end = numPoints;
                        if (i < numParts - 1)
                        {
                            end = partIndicies[i + 1];
                        }
                        int size = end - start;
                        Coordinate coords[] = new Coordinate[size];

                        for (int j = 0; j < size; j++)
                        {
                            coords[j] = new Coordinate(pointsArray[start + j]);
                        }

                        if (recordType == POLYLINE)
                        {
                            parts[i] = geomFactory.createLineString(coords);
                        } else
                        {
                            parts[i] = geomFactory.createLinearRing(coords);
                        }
                    }
                    if (recordType == POLYLINE)
                    {
                        LineString[] ls = new LineString[numParts];
                        for (int i = 0; i < numParts; i++)
                        {
                            ls[i] = (LineString) parts[i];
                        }
                        if (numParts == 1)
                        {
                            geom = parts[0];
                        } else
                        {
                            geom = geomFactory.createMultiLineString(ls);
                        }
                    } else	// polygon
                    {
                        geom = createPolygon(parts);
                    }
                } else
                {
                    System.err.println("Unknown shape type in " + recordType);
                }

                if (geom != null)
                {
                    try
                    {
                        // The user *may* have created their own MasonGeometry
                        // class, so use the given masonGeometry class; by
                        // default it's MasonGeometry.
                        MasonGeometry masonGeometry =  (MasonGeometry) masonGeometryClass.newInstance();
                        masonGeometry.geometry = geom;

                        if (!attributes.isEmpty())
                        {
                            masonGeometry.addAttributes(attributes);
                        }

                        field.addGeometry(masonGeometry);
                    } catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                }
            }
        }
        catch (IOException e)
        {
            System.out.println("Error in ShapeFileImporter!!");
            System.out.println("SHP filename: " + shpFile);
            e.printStackTrace();
        }
    }
}
